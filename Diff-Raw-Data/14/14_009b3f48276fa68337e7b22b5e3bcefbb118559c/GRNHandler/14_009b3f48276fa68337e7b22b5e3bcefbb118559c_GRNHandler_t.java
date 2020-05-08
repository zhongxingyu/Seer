 
 package com.AlphaDevs.Web.JSFBeans;
 
 import com.AlphaDevs.Web.Entities.*;
 import com.AlphaDevs.Web.Enums.BillStatus;
 import com.AlphaDevs.Web.Enums.TransactionTypes;
 import com.AlphaDevs.Web.Helpers.EntityHelper;
 import com.AlphaDevs.Web.Helpers.MessageHelper;
 import com.AlphaDevs.Web.SessionBean.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import org.primefaces.event.RowEditEvent;
 import org.primefaces.event.SelectEvent;
 /**
  *
  * @author Mihindu Gajaba Karunarathne 
  * 
  * Alpha Development Team ( Pvt ) Ltd
  * www.AlphaDevs.com
  * Info@AlphaDevs.com
  * 
  */
 
 @ManagedBean
 @SessionScoped
 public class GRNHandler 
 {
     @EJB
     private PropertiesController propertiesController;
     
     @EJB
     private PropertyManagerController propertyManagerController;
     @EJB
     private GRNPaymentDetailsController gRNPaymentDetailsController;
     @EJB
     private CashBookBalanceController cashBookBalanceController;
     @EJB
     private CustomerBalanceController customerBalanceController;
     @EJB
     private LoggerController loggerController;
     @EJB
     private CashbookController cashbookController;
     @EJB
     private ItemBincardController itemBincardController;
     @EJB
     private CustomerTransactionController customerTransactionController;
     @EJB
     private StockController stockController;
     @EJB
     private GrnDetailsController grnDetailsController;
     @EJB
     private SystemsController systemsController;
     @EJB
     private GRNController gRNController;
     
     
     
     private double cashAmount;
     
     private GRN current;
     private GRNDetails currentDetails;
     private List<GRNDetails> VirtualList;
     private GRNDetails selectedGrnData;
     private GRNPaymentDetails paymentDetails;
     
     
     public GRNHandler() {
         
                 
         if (current == null)
         {
             current = new GRN();
             System.out.println("Property Handler Created");
             System.out.println("Constructor GRN");
         }
         if(currentDetails == null)
         {
             currentDetails = new GRNDetails();
         }
         if(selectedGrnData == null)
         {
             selectedGrnData = new GRNDetails();
         }
         if(paymentDetails == null)
         {
             paymentDetails = new GRNPaymentDetails();
         }
         VirtualList = new ArrayList<GRNDetails>();
         
         cashAmount = 0;
         
         
     }
 
     public GRNPaymentDetails getPaymentDetails() {
         return paymentDetails;
     }
 
     public void setPaymentDetails(GRNPaymentDetails paymentDetails) {
         this.paymentDetails = paymentDetails;
     }
     
 
     public GRNDetails getSelectedGrnData() {
         return selectedGrnData;
     }
 
     public void setSelectedGrnData(GRNDetails selectedGrnData) {
         this.selectedGrnData = selectedGrnData;
     }
     
     
 
     public GRN getCurrent() {
         return current;
     }
 
     public void setCurrent(GRN current) {
         this.current = current;
     }
 
     public double getCashAmount() {
         return cashAmount;
     }
 
     public void setCashAmount(double cashAmount) {
         this.cashAmount = cashAmount;
     }
     
     public void handleSelect(SelectEvent event) 
     {   
         currentDetails.setGrnItemCost(currentDetails.getGrnItem().getItemCost());
     }  
     
     public String addTEst()
     {
         //GRN grn = new GRN();
         
         if(getVirtualList().isEmpty())
         {
             MessageHelper.addErrorMessage("Error", "No Items Found!");
             return "";
         }
         
         Logger log = EntityHelper.createLogger("Good Received Note" , current.getGrnNo(), TransactionTypes.GRN);
         loggerController.create(log);
         
         
         current.setBillStatus(BillStatus.TAX);
         
         
         for(Properties properties : current.getExtraz()){
             System.out.println("Trying Saving ... " + properties.getPropertyName() + " : " + properties.getPropertyValue());
            //properties.setRelatedGRNs(current);
             propertiesController.create(properties);
             System.out.println("Done");
         }
         
         current.setgRNDetailss(VirtualList);
         current.setLogger(log);
         gRNController.create(current);
         
         for(GRNDetails grnDet : getVirtualList())
         {
            grnDet.setRelatedGRN(current);
            //grnDetailsController.create(grnDet); 
            
            Stock  stock = stockController.findSpecific(grnDet.getGrnItem());
            stock.setStockQty(stock.getStockQty() + grnDet.getGrnQty());
            stockController.edit(stock);
            
            ItemBincard itemBin = new ItemBincard();
            itemBin.setDescription("Good Received Note - " + current.getGrnNo());
            itemBin.setItem(grnDet.getGrnItem());
            itemBin.setTrnNumber(current.getInvNo());
            itemBin.setQty(grnDet.getGrnQty());
            itemBin.setLog(log);
            itemBin.setBalance(stock.getStockQty());
            itemBincardController.create(itemBin);
            
         }
         
         //Customer Transaction
         CustomerTransaction custTran = new CustomerTransaction();
         custTran.setDescription("GRN - " + current.getGrnNo());
         custTran.setSupplier(current.getSupplier());
         custTran.setDR(current.getTotalAmount());
         custTran.setCR(0);
         
         //Getting Cust Balance
         CustomerBalance Balance = customerBalanceController.getCustomerBalanceObject(current.getSupplier());
         if(Balance != null)
         {
             Balance.setBalance(Balance.getBalance() + current.getTotalAmount() );
             customerBalanceController.edit(Balance);
             custTran.setBalance(Balance.getBalance());
             
         }
         else
         {
             custTran.setBalance(paymentDetails.getCashAmount());
         }
         
         custTran.setLogger(log);
         customerTransactionController.create(custTran);
         
         paymentDetails.setTotalAmount(current.getTotalAmount());
         paymentDetails.setLogger(log);
         paymentDetails.setRelatedGrn(current);
         gRNPaymentDetailsController.create(paymentDetails);
         if ( paymentDetails.getCashAmount() > 0 )
         {
             CustomerTransaction custTranPaid = new CustomerTransaction();
             custTranPaid.setDescription("PAID - " + current.getGrnNo());
             custTranPaid.setSupplier(current.getSupplier());
             custTranPaid.setDR(0);
             custTranPaid.setCR(paymentDetails.getCashAmount());
 
             Balance.setBalance(Balance.getBalance() - paymentDetails.getCashAmount());
             customerBalanceController.edit(Balance);
             custTranPaid.setBalance(Balance.getBalance());
             custTranPaid.setLogger(log);
             customerTransactionController.create(custTranPaid);
             
             //Cashbook
             CashBook cashBook = new CashBook();
             cashBook.setDescription("Purchase - " + current.getGrnNo() );
             cashBook.setCR(paymentDetails.getCashAmount());
             cashBook.setDR(0);
             cashBook.setLocation(current.getLocation());
             cashBook.setLogger(log);
 
             CashBookBalance cashBalance = cashBookBalanceController.getCashBookBalanceObject(current.getLocation(), current.getBillStatus());
             if(cashBalance != null)
             {
                 cashBalance.setCashBalance(cashBalance.getCashBalance() - paymentDetails.getCashAmount() );
                 cashBookBalanceController.edit(cashBalance);
                 cashBook.setBalance(cashBalance.getCashBalance());            
             }
             else
             {
                 cashBook.setBalance(paymentDetails.getCashAmount());
             }
 
             cashbookController.create(cashBook);
             
             
         }
         
         
        
         printReportDownload();
         current = new GRN();
         currentDetails = new GRNDetails();
         VirtualList = new ArrayList<GRNDetails>();
         setCashAmount(0);
         return "Home";
     }
     
     
     
     
     public List<GRN> getGrnList()
     {
         return gRNController.findAll();
     }
     
     public void printReportDownload() 
     {
         try
         {
             List<GRN> GrnList = new ArrayList<GRN>();
             GRN nn = (GRN) gRNController.find(current.getId());
             System.out.println("Grn : re d " + nn.getGrnNo() + " With :" + nn.getId());
             GrnList.add(nn);
             JRBeanCollectionDataSource beanCollection = new JRBeanCollectionDataSource(GrnList);
             String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/Reports/GrnReport.jasper");
             System.out.println("Path : " + reportPath);
             JasperPrint jasPrint =  JasperFillManager.fillReport(reportPath, new HashMap(), beanCollection);
             HttpServletResponse responce = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
             responce.setContentType("application/pdf");
             String filename = new StringBuffer(reportPath).append(".pdf").toString();  
             ServletOutputStream output = responce.getOutputStream();
             JasperExportManager.exportReportToPdfStream(jasPrint, output);
             responce.addHeader("Content-Disposition", "inline; filename="+ filename);  
             //responce.addHeader("Content-disposition", "attachment; filename=CashBook.pdf");
             FacesContext.getCurrentInstance().responseComplete();
             System.out.println("Report Done");
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         
     }
     
     public String removeItem(GRNDetails details)
     {
         
         for(int a=0;a<getVirtualList().size();a++)
         {
         
             if(getVirtualList().get(a) == details )
             {
                 getVirtualList().remove(a);
 
                 break;
             }
         }
         current.setTotalAmount(getTotal());
         
         return "#";
     }
     
     public String removeSelectedItem()
     {
         if ( getSelectedGrnData() ==null)
         {
             MessageHelper.addErrorMessage("Selection ","No item Selected ");
             return "";
         }
         else
         {
             for(int a=0;a<getVirtualList().size();a++)
             {
 
                 if(getVirtualList().get(a) == getSelectedGrnData() )
                 {
                     getVirtualList().remove(a);
 
                     break;
                 }
             }
             current.setTotalAmount(getTotal());
         }
         return "#";
     }
     
     public String loadRTE()
     {
         Systems sys = new Systems("Header");
         sys.setId(1L);
         systemsController.create(sys);
         System.out.println("Done");
         return "#";
     }
 
     public GRNDetails getCurrentDetails() {
         return currentDetails;
     }
 
     public void setCurrentDetails(GRNDetails currentDetails) {
         this.currentDetails = currentDetails;
     }
 
     public List<GRNDetails> getVirtualList() {
         return VirtualList;
     }
 
     public void setVirtualList(List<GRNDetails> VirtualList) {
         this.VirtualList = VirtualList;
     }
     
     public void addItem()
     {
         boolean isFound =false;
         
         if(currentDetails.getGrnItem() == null || currentDetails.getGrnItemCost() ==0 || currentDetails.getGrnQty() == 0)
         {
             MessageHelper.addErrorMessage("Error", "Please Fill All Details");
             return;
         }
         
         currentDetails.setRelatedGRN(current);
         
         for(GRNDetails det : getVirtualList())
         {
             if(det.getGrnItem().getId() == currentDetails.getGrnItem().getId())
             {
                 det.setGrnQty(det.getGrnQty() + currentDetails.getGrnQty());
                 isFound = true;
                 break;
             }
             
         }
         if (!isFound)
         {
             getVirtualList().add(currentDetails);
         }
         
         currentDetails = new GRNDetails();
         
         System.out.println("Total : " + getTotal());
         current.setTotalAmount(getTotal());
         currentDetails = new GRNDetails();
     }
    
     
     public Double getTotal()
     {
         double TotalValue=0;
         for(GRNDetails details : getVirtualList())
         {
             //System.out.println("Det :  " + details.getGrnItem().getItemDescription() + " : " + details.getGrnItem().getItemCost());
             TotalValue = TotalValue +  (details.getGrnItemCost() * details.getGrnQty());
         }
         return TotalValue;
     }
     
     public void onEdit(RowEditEvent event) 
     {   
         
         MessageHelper.addSuccessMessage(((GRNDetails) event.getObject()).getGrnItem().getItemCode() + " Updated!");
         for(GRNDetails det: getVirtualList())
         {
             if(det.getGrnItem().getId() == ((GRNDetails)event.getObject()).getGrnItem().getId() )
             {
                 
                 det.setGrnQty(((GRNDetails)event.getObject()).getGrnQty());
                 det.setGrnItemCost(((GRNDetails)event.getObject()).getGrnItemCost());
                 det.setGrnItemDiscountPerc(((GRNDetails)event.getObject()).getGrnItemDiscountPerc());
             }
         }
         
         current.setTotalAmount(getTotal());
     }  
     public void onCancel(RowEditEvent event) 
     {  
         MessageHelper.addSuccessMessage(((GRNDetails) event.getObject()).getGrnItem().getItemCode() + "Not Updated!");
         
     }
      
     public void validateItems(FacesContext context, UIComponent component, Object value) 
     {
         
          
         if(getVirtualList().isEmpty()) 
         {
             FacesMessage message = new FacesMessage();
             message.setSeverity(FacesMessage.SEVERITY_ERROR);
             message.setSummary("Item Empty");
             message.setDetail("Item is Empty");
             context.addMessage(null, message);
             throw new ValidatorException(message);
         }
     } 
      
     public String preapareList(GRN grn)
     {
         setCurrent(grn);
         System.out.println("Grn Set " + grn.getGrnNo());
         return "#";      
     }
     
     public List<GRNDetails> getSpecificGrnList()
     {
         System.out.println("Got List");
         return gRNController.findSpecific(getCurrent());
         
     }
     
     public List<Properties> getPropListTest(){
         
         if(current.getExtraz() == null && propertyManagerController != null && propertyManagerController.findAll() != null){
             List<Properties> propertyList = new ArrayList<Properties>();
             for(PropertyManager propertyMng : propertyManagerController.findAll()){
                 Properties property = new Properties(propertyMng.getFieldName(), null);
                 propertyList.add(property);
                 System.out.println("Property added : " + property.getPropertyName());
             }
             current.setExtraz(propertyList);
             System.out.println("Properties set");
         }
         
         return current.getExtraz();
     }
     
    
 }
