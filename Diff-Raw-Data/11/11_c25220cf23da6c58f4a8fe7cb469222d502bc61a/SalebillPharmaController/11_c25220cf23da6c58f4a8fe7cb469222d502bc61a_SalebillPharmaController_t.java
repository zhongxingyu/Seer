 package innuinfocomm;
 
 import java.io.IOException;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListView;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableColumn.CellEditEvent;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import pojos.ItemsPharma;
 import pojos.SaleBillPharmaItem;
 import utils.EntityManagerHelper;
 import javafx.scene.control.cell.TextFieldTableCell;
 import javafx.scene.input.MouseEvent;
 import javafx.stage.Popup;
 import javafx.stage.Stage;
 import pojos.Customer;
 import pojos.SaleBillPharma;
 import utils.MyLogger;
 import utils.PrintInvoice;
 
 
 public class SalebillPharmaController {
 
     @FXML
     private ResourceBundle resources;
 
     @FXML
     private URL location;
 
     @FXML
     private TableColumn<SaleBillPharmaItem,Float> AmountTableColumn ;
 
     @FXML
     private TextField CDamtTextBox;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, String> batchTableColumn;
 
     @FXML
     private TextField billNoTextbox;
 
     @FXML
     private TextField custLicTextBox;
 
     @FXML
     private TextField customerTextBox;
 
     @FXML
     private TextField dateTextBox;
 
     @FXML
     private TextArea deliveryTextArea;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, String> descTableColumn;
 
     @FXML
     private TextField chequeTextBox;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, String> dmTableColumn;
 
     @FXML
     private Label errorLabel;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, String> expTableColumn;
 
     @FXML
     private TextField finalAmtTextBox;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, String> makeTableColumn;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, Float> mrpTableColumn;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, String> packTableColumn;
 
     @FXML
     private ComboBox<String> paymentComboBox;
 
     @FXML
     private Button printBtn;
 
     @FXML
     private TableColumn<SaleBillPharmaItem,String> qntyTableColumn ;
 
     @FXML
     private TableColumn<SaleBillPharmaItem, Float> rateTableColumn;
 
     @FXML
     private Button resetBtn;
 
     @FXML
     private TableView<SaleBillPharmaItem> saleItemTableview;
 
     @FXML
     private Button saveBtn;
 
     @FXML
     private Label successLabel;
 
     @FXML
     private TextField totalTextBox;
 
     @FXML
      private Label vat125AmtLabel;
 
     @FXML
     private Label vat5AmtLabel;
 
     @FXML
     private TextField vatTextBox;
     
        @FXML
     private ListView<SaleBillPharmaItem> searchResultListView;
        
        @FXML
        private ListView<SaleBillPharma> saleSearchListView;
        
            @FXML
     private TextField itemSearchTextBox;
            
            
     @FXML
     private Label errLabel;
     
     @FXML
     private RadioButton searchSaleBillRadioBtn;
       
     @FXML
     private RadioButton searchItemRadioBtn;
     
     @FXML
     private Label searchLabel;
       
      private  final ToggleGroup radioToggle = new ToggleGroup() ;
       
 
     private final static Logger LOGGER = Logger.getLogger(SalebillPharmaController.class.getName());
 
     private  int cash_discount = 0;
     
    SaleBillPharma salebill = new SaleBillPharma();
     
     DecimalFormat f = new DecimalFormat("##.00");
     private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
     
     private ObservableList<SaleBillPharmaItem> data = FXCollections.observableArrayList();
     
     private Popup popup;
     private ListView<Customer> customerListView = new ListView<>();
     
      Stage stage = new Stage();
      
      //variable to check which state user is in
      private boolean isSearchingSaleBill = false;
      private Customer custFromSearchSaleBill = null;
      
     
     @FXML
     void initialize() {
         try {
             MyLogger.setup();
         } catch (IOException ex) {
             Logger.getLogger(SalebillPharmaController.class.getName()).log(Level.SEVERE, null, ex);
         }
         assert AmountTableColumn != null : "fx:id=\"AmountTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert CDamtTextBox != null : "fx:id=\"CDamtTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert batchTableColumn != null : "fx:id=\"batchTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert billNoTextbox != null : "fx:id=\"billNoTextbox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert custLicTextBox != null : "fx:id=\"custLicTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert customerTextBox != null : "fx:id=\"customerTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert dateTextBox != null : "fx:id=\"dateTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert deliveryTextArea != null : "fx:id=\"deliveryTextArea\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert descTableColumn != null : "fx:id=\"descTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert chequeTextBox != null : "fx:id=\"discountTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert dmTableColumn != null : "fx:id=\"dmTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert errorLabel != null : "fx:id=\"errorLabel\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert expTableColumn != null : "fx:id=\"expTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert finalAmtTextBox != null : "fx:id=\"finalAmtTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert makeTableColumn != null : "fx:id=\"makeTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert mrpTableColumn != null : "fx:id=\"mrpTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert packTableColumn != null : "fx:id=\"packTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert paymentComboBox != null : "fx:id=\"paymentComboBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert printBtn != null : "fx:id=\"printBtn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert qntyTableColumn != null : "fx:id=\"qntyTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert rateTableColumn != null : "fx:id=\"rateTableColumn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert resetBtn != null : "fx:id=\"resetBtn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert saleItemTableview != null : "fx:id=\"saleItemTableview\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert saveBtn != null : "fx:id=\"saveBtn\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert successLabel != null : "fx:id=\"successLabel\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert totalTextBox != null : "fx:id=\"totalTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert vat125AmtLabel != null : "fx:id=\"vat125AmtLabel\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert vat5AmtLabel != null : "fx:id=\"vat5AmtLabel\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         assert vatTextBox != null : "fx:id=\"vatTextBox\" was not injected: check your FXML file 'SalebillPharma.fxml'.";
         
 
         
 
         mrpTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem, Float>("mrp"));
         dmTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,String>("DM"));
         makeTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,String>("make"));
         packTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem, String>("pack"));
         rateTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,Float>("itemRate"));
         descTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,String>("description"));
         qntyTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,String>("qnty"));
         AmountTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,Float>("amt"));
         batchTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,String>("batch"));
         expTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,String>("expDate"));
         descTableColumn.setCellValueFactory(new PropertyValueFactory<SaleBillPharmaItem,String>("itemName"));
         
        
            qntyTableColumn.setCellFactory(TextFieldTableCell.<SaleBillPharmaItem>forTableColumn());
        
        
           qntyTableColumn.setOnEditCommit(new EventHandler<CellEditEvent<SaleBillPharmaItem, String>>() {
 
            @Override
            public void handle(CellEditEvent<SaleBillPharmaItem, String> t) {
                System.out.println("on edit commit called" + t.getNewValue() + " " + t.getOldValue());
               ((SaleBillPharmaItem)t.getTableView().getItems().get(t.getTablePosition().getRow())).setQnty(t.getNewValue());
               updateAmount();
               saleItemTableview.getColumns().get(0).setVisible(false);
               saleItemTableview.getColumns().get(0).setVisible(true);
            }
        });
 
        initializeSaleBill();
         
     }
     
  @FXML
     void addItemToTable(MouseEvent event) {
      
      SaleBillPharmaItem item = searchResultListView.getSelectionModel().getSelectedItem();
      item.setSaleBillNo(salebill);
      item.setQnty(""+1);
      data.add(item);
      updateAmount();
          
     }
  
      @FXML
     void searchItems(KeyEvent event) {
          //change here to search items or Salebill
             if(searchItemRadioBtn.isSelected())
                 searchItemsCode();
             else if(searchSaleBillRadioBtn.isSelected())
                 searchSaleBills();
   
     }
      
      void searchItemsCode(){
                   searchResultListView.getItems().clear();
          String text = itemSearchTextBox.getText().trim();
               text = "%"+text+"%";
             EntityManager em = EntityManagerHelper.getInstance().getEm();
             Query q  = null;
             if(text.length() ==0 )
                 q = em.createNamedQuery("Items.findAll");
             else
             {q= em.createNamedQuery("Items.findByItemsPharmaNameLike");
                 q.setParameter("description", text);
             }
             ArrayList<ItemsPharma> list = new ArrayList(q.getResultList());
             System.out.println(list.size()+ " items found");
             
             for(ItemsPharma i : list){
                 SaleBillPharmaItem convItem = new SaleBillPharmaItem(i);
                 searchResultListView.getItems().add(convItem);
             }
             
      }
  
      void searchSaleBills(){
          saleSearchListView.getItems().clear();
          String text = itemSearchTextBox.getText().trim();
               text = "%"+text+"%";
             EntityManager em = EntityManagerHelper.getInstance().getEm();
             Query q  = null;
             if(text.length() ==0 )
                 q = em.createNamedQuery("SaleBillPharma.findAll");
             else
             {q= em.createNamedQuery("SaleBillPharma.findByCustomerId");
                 q.setParameter("customerId", custFromSearchSaleBill);
             }
             ArrayList<SaleBillPharma> list = new ArrayList(q.getResultList());
             System.out.println(list.size()+ " items found");
             if(list == null || list.size() < 1){
                 errorLabel.setText("No sale bill found for customer  " + custFromSearchSaleBill.getCompanyName());
             }
             
             saleSearchListView.getItems().addAll(list);
             
      }
     
     public void updateAmount(){
         
         float vat5amount = 0.0f;
         float vat125amount= 0.0f;
         float vatTotalAmt = 0.0f;
         float totalamount= 0.0f;
         float totalpayable= 0.0f;
         float cdAmount= 0.0f;
        System.out.println(data.size() + " items in the data list");
         for(int i = 0;i<data.size();i++){
             SaleBillPharmaItem item = data.get(i);
             if(Math.abs(item.getItemVatPerc() - 5) < 0.01){
                 vat5amount += item.getItemVatRs();
             }else{
                 vat125amount += item.getItemVatRs();
             }
            
             totalamount += item.getAmt();
            System.out.println(item.getAmt());
         }
         
         vatTotalAmt = vat5amount + vat125amount;
         
         if(cash_discount < 2)
             cdAmount = 0.0f;
         else
             cdAmount =(float) 0.02*totalamount;
         
         totalpayable = totalamount - cdAmount;
         
         vat5AmtLabel.setText( " Vat Amount on 5% Product :  Rs " + f.format(vat5amount));
         vat125AmtLabel.setText(" Vat Amount on 12.5% Product :  Rs " + f.format(vat125amount));
         totalTextBox.setText(""+f.format(totalamount));
         finalAmtTextBox.setText(""+f.format(totalpayable));
         CDamtTextBox.setText("" + f.format(cdAmount));
         vatTextBox.setText(""+f.format(vatTotalAmt));
         
         
     }
     
     
         @FXML
     void handlePaymentModeChanged(MouseEvent event) {
             if(paymentComboBox.getSelectionModel().isSelected(0))
                 salebill.setMode("CASH");
             else if(paymentComboBox.getSelectionModel().isSelected(1))
                 salebill.setMode("");
             
     }
 
     //code to update stocks on click save btn
         public void updateStocks(){
             
             for(int i = 0 ;i<data.size();i++){
                 SaleBillPharmaItem item = data.get(i);
                 EntityManager em = EntityManagerHelper.getInstance().getEm();
                 ItemsPharma itemP = em.find(ItemsPharma.class, item.getItemPharmaId().getId());
                 itemP.setStockStrips(itemP.getStockStrips()-parseQnty(item.getQnty()));
                 
                 try{
                 em.getTransaction().begin();
                 em.merge(itemP);
                  em.getTransaction().commit();
                 }catch(Exception e){
                     e.printStackTrace();
                     //System.out.println("error in ");
                     LOGGER.setLevel(Level.SEVERE);
                     LOGGER.info("error in update Stocks.");
                     LOGGER.info(e.getMessage());
                                     
                     errLabel.setVisible(true);
                 }
                 
             }
             
             
         }
     
     
         @FXML
     void handlePrintBtn(ActionEvent event) {
             PrintInvoice printer = new PrintInvoice(salebill);
             printer.getDocument();
     }
 
     @FXML
     void handleResetBtn(ActionEvent event) {
             data.remove(0, data.size());
             customerTextBox.setText(null);
             custLicTextBox.setText(null);
             vat5AmtLabel.setText("Vat amt on 5% Product");
             vat125AmtLabel.setText("Vat amt on 12.5% Product");
             deliveryTextArea.setText(null);
             vatTextBox.setText(null);
             totalTextBox.setText(null);
             CDamtTextBox.setText(null);
             chequeTextBox.setText(null);
             finalAmtTextBox.setText(null);
             errLabel.setVisible(false);
             successLabel.setVisible(false);
             updateAmount();
                     
      
     }
 
     @FXML
     void handleSaveBtn(ActionEvent event) {
         successLabel.setVisible(false);
         errLabel.setVisible(false);
          EntityManager em = EntityManagerHelper.getInstance().getEm();
         
          salebill.setSaleBillPharmaItemList(data.subList(0, data.size()));
          salebill.setDeliveryAddress(deliveryTextArea.getText());
          salebill.setDiscount(Float.parseFloat(CDamtTextBox.getText()));
          salebill.setFinalAmt(Float.parseFloat(finalAmtTextBox.getText()));
          salebill.setTotalAmt(Float.parseFloat(totalTextBox.getText()));
          salebill.setTotalVat(Float.parseFloat(vatTextBox.getText()));
          salebill.setMode("CASH");
          if(paymentComboBox.getSelectionModel().isSelected(1)){
              salebill.setMode(chequeTextBox.getText());
          }
          try{
              em.getTransaction().begin();
              em.persist(salebill);
              em.getTransaction().commit();
              updateStocks();
              
              successLabel.setVisible(true);
              
          }catch(Exception e){
              LOGGER.setLevel(Level.SEVERE);
              LOGGER.info(e.getMessage());
              errLabel.setVisible(true);
              //System.out.println("error in saving items");
              e.printStackTrace();
          }
   
         
     }
     
     private void initializeSaleBill() {
         
           try {
       MyLogger.setup();
     } catch (IOException e) {
       e.printStackTrace();
       throw new RuntimeException("Problems with creating the log files");
     }
 
          searchItemRadioBtn.setToggleGroup(radioToggle);
          searchSaleBillRadioBtn.setToggleGroup(radioToggle);
          
         saleItemTableview.setOnKeyPressed(new EventHandler<KeyEvent>(){
 
             @Override
             public void handle(KeyEvent t) {
                 if(t.getCode().equals(KeyCode.DELETE)){
                     data.remove(saleItemTableview.getSelectionModel().getSelectedIndex());
                     updateAmount();
                 }
               
             }
         });
         
         
         paymentComboBox.setOnAction(new EventHandler<ActionEvent>() {
 
             @Override
             public void handle(ActionEvent t) {
                 int val = paymentComboBox.getSelectionModel().getSelectedIndex();
                 if(val ==  0){
                     cash_discount = 2;
                     
                 }
                 else
                     cash_discount = 0;
                 
                 updateAmount();
             }
             
         });
         
        saleItemTableview.setItems(data);    
        searchItemsCode();
        
        //set the bill Number
        EntityManager em = EntityManagerHelper.getInstance().getEm();
         Query q = em.createNamedQuery("SaleBillPharma.findNextId");
       
         int nextId = 0;
         if(q.getSingleResult() == null )
             nextId = 1;
         else
         {
             nextId = Integer.parseInt(""+q.getSingleResult());
             nextId++;
         }
         
         billNoTextbox.setText(""+nextId);
         salebill.setId(nextId);
 
        //set the billDate
         salebill.setBillDate(new Date());
         dateTextBox.setText(dateFormatter.format(new Date()));
         
         
         customerTextBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
 
             @Override
             public void handle(KeyEvent t) {
                  if (KeyCode.TAB.equals(t.getCode())) {
                                        hidePopup();
                                         return;
                               }
                  initiatePopUp(customerTextBox.getText());
                  if(KeyCode.ENTER.equals(t.getCode())){
                      showPopup();
                  }
                  
       
             }
         });
         
         customerListView.setOnKeyPressed(new EventHandler<KeyEvent>() {
 
             @Override
             public void handle(KeyEvent t) {
                 System.out.println("key prese:");
                      if(KeyCode.ENTER.equals(t.getCode())){
                               chooseCustomerNow();
                  }
             }
         });
         
         customerListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
 
             @Override
             public void handle(MouseEvent t) {
                 chooseCustomerNow();
             }
         });
         
         itemSearchTextBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
 
               @Override
               public void handle(KeyEvent t) {
                      if (KeyCode.TAB.equals(t.getCode())) {
                                        hidePopup();
                                         return;
                               }
                  initiatePopUp(customerTextBox.getText());
                  if(KeyCode.ENTER.equals(t.getCode())){
                      showPopup();
                  }
               }
           });
         
         searchItemRadioBtn.setSelected(true);
     }
     
     private int parseQnty(String qnty){
         
         int total = 0;
         
         int plusLoc = qnty.indexOf("+");
         if(plusLoc == -1){
             total = Integer.parseInt(qnty);
         }else
         {
             int mainQty = Integer.parseInt(qnty.substring(0,plusLoc));
             int freeQty = Integer.parseInt(qnty.substring(plusLoc+1, qnty.length()));
             total = mainQty + freeQty;
          }
         System.out.println(total +  " : total qnty");
         return total;
     }
     
     public void chooseCustomerNow(){
         Customer c = customerListView.getSelectionModel().getSelectedItem();
         
         if(!isSearchingSaleBill)
         {
             customerTextBox.setText(c.toString());
             salebill.setCustomerId(c);
             custLicTextBox.setText(c.getLicenceNo());
             hidePopup();
         }
         else{
             custFromSearchSaleBill = c;
             searchSaleBills();
             hidePopup(  );
         }
     }
 
     
     
       private void initiatePopUp(String text){
         
            customerListView.getItems().clear();
          
             //write the code to get the items List from the db and then 
             //show it here 
            EntityManager em = EntityManagerHelper.getInstance().getEm();
            Query q  = null;
            if(text == null || text.length() == 0)
                q = em.createNamedQuery("Customer.findAll");
            else{
             text = text.trim();
             text = "%"+text+"%";
             q= em.createNamedQuery("Customer.findByCustomersNameLike");
                 q.setParameter("companyName", text);
            }
             
             ArrayList<Customer> list = new ArrayList(q.getResultList());
             System.out.println(list.size()+ " items found");
             customerListView.getItems().addAll(list);
    }
             private void showPopup(){
         stage.setScene(new Scene(new Group(customerListView)));
         stage.show();
             }
             
      private void hidePopup(){
         stage.close();
     }
      
      private void fillSaleBillFormFromData(SaleBillPharma sFromDB){
         try {
             Thread.sleep(100);
         } catch (InterruptedException ex) {
             Logger.getLogger(SalebillPharmaController.class.getName()).log(Level.SEVERE, null, ex);
         }
          salebill.setCustomerId(sFromDB.getCustomerId());
          customerTextBox.setText(sFromDB.getCustomerId().getCompanyName());
          custLicTextBox.setText(sFromDB.getCustomerId().getLicenceNo());
          billNoTextbox.setText(sFromDB.getId() +"");
          data.clear();
          
          ArrayList<SaleBillPharmaItem> itemList = new ArrayList(sFromDB.getSaleBillPharmaItemList());
          ArrayList<SaleBillPharmaItem> showList = new ArrayList(   );
          for (SaleBillPharmaItem i : itemList){
             SaleBillPharmaItem item = new SaleBillPharmaItem(i.getItemPharmaId());
             item.setQnty(i.getQnty());
             showList.add(item);
          }
          data.addAll(showList);
          
          //update Amount
          updateAmount();;
          
          
      }
      
      @FXML
      private void handleItemRadioBtnClick(){
          saleSearchListView.getItems().clear(); // to clear the items in the list view
          isSearchingSaleBill = false;
          searchLabel.setText("Click to add  Items to Sale Bill");
          itemSearchTextBox.setPromptText("Search by Item name");
          saleSearchListView.setVisible(false);
          searchResultListView.setVisible(true);
      }
      
      @FXML
      private void handleSaleRadioBtnClick(){
          isSearchingSaleBill = true;
          itemSearchTextBox.setPromptText("Search by customer Name to view Sale Bill");
          searchLabel.setText("Click to view the Sale Bill");
          searchResultListView.setVisible(false);
          saleSearchListView.setVisible(true);
          
      }
 
      @FXML
       void showSaleBill(MouseEvent event){
          SaleBillPharma sb = saleSearchListView.getSelectionModel().getSelectedItem();
          System.out.println(sb);
          fillSaleBillFormFromData(sb);
      }
     
      
   
 }
 
