 package ss.bshop.mbeans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 
 import org.primefaces.event.RowEditEvent;
 
 import ss.bshop.domain.SupplierOrder;
 import ss.bshop.service.ISupplierOrderService;
 
 
 @ManagedBean(name="suplOrderMB")
 @RequestScoped
 public class SuplOrderManagedBean  implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	@ManagedProperty(value="#{supplierOrderService}")
 	ISupplierOrderService supplierOrderService;
 	SupplierOrder selected;
 	List<SupplierOrder> supplierOrderList;
 	
 	@ManagedProperty(value = "#{SuplOrderDataModel}")
     private SuplOrderDataModel model;
 	
 	@PostConstruct
     protected void postConstruct() {
 		supplierOrderList = new ArrayList<SupplierOrder>();
 		supplierOrderList.addAll(getSupplierOrderService().getAll()); 
     	model = new SuplOrderDataModel(supplierOrderList);
     }  
         
     public void editRow(RowEditEvent event) {
     	SupplierOrder rowItem = (SupplierOrder) event.getObject();
        getSupplierOrderService().update(rowItem); 
        selected=null;
     }
     
     public String createNew() {
     	SupplierOrder order=new SupplierOrder();
     	//order.setName("default");
     	supplierOrderList.add(order);
     	getSupplierOrderService().add(order);  	
         return "";
     }
 
      public String delete() {
      	if(selected!=null){
      		getSupplierOrderService().remove(selected.getId());
      		supplierOrderList.remove(selected);
      	}
      	return "";
      }
    
 	
 	public SuplOrderDataModel getModel() {
 		return model;
 	}
 
 	public void setModel(SuplOrderDataModel model) {
 		this.model = model;
 	}
 
 	public SupplierOrder getSelected() {
 		return selected;
 	}
 
 	public void setSelected(SupplierOrder selected) {
 		this.selected = selected;
 	}
 
 	public ISupplierOrderService getSupplierOrderService() {
 		return supplierOrderService;
 	}
 
 	public void setSupplierOrderService(ISupplierOrderService supplierOrderService) {
 		this.supplierOrderService = supplierOrderService;
 	}
 
 	public List<SupplierOrder> getSupplierOrderList() {
 		return supplierOrderList;
 	}
 
 	public void setSupplierOrderList(List<SupplierOrder> supplierOrderList) {
 		this.supplierOrderList = supplierOrderList;
 	}
 
 	
 
 	
 	
    }
