 package eu.choreos;
 
 import java.util.List;
 
 import eu.choreos.vv.clientgenerator.Item;
 import eu.choreos.vv.clientgenerator.ItemImpl;
 
 
 public class PurchaseInfo {
 	
 	private String id;
 	
 	private String sellerEndpoint;
 	
 	private String[] products;
 	
 	private Double value;
 	
 	private CustomerInfo customer;
 
 	
 	
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getSellerEndpoint() {
 		return sellerEndpoint;
 	}
 
 	public void setSellerEndpoint(String sellerEndpoint) {
 		this.sellerEndpoint = sellerEndpoint;
 	}
 
 	public String[] getProducts() {
 		return products;
 	}
 
 	public void setProducts(List<String> products) {
 		this.products = (String[]) products.toArray();
 	}
 	
 	public void setProducts(String[] products) {
 		this.products = products;
 	}
 
 	public Double getValue() {
 		return value;
 	}
 
 	public void setValue(Double value) {
 		this.value = value;
 	}
 
 	public CustomerInfo getCustomerInfo() {
 		return customer;
 	}
 
 	public void setCustomerInfo(CustomerInfo customer) {
 		this.customer = customer;
 	}
 	
 	public Item getItem(String tagName) {
 		Item item = new ItemImpl(tagName);
 		
 		Item i = new ItemImpl("id");
 		i.setContent(id);
 		item.addChild(i);
 		
 		i = new ItemImpl("sellerEndpoint");
 		i.setContent(sellerEndpoint);
 		item.addChild(i);
 		
 		i = new ItemImpl("value");
 		i.setContent(value.toString());
 		item.addChild(i);
 		
 		for(String p: products) {
 			i = new ItemImpl("products");
 			i.setContent(p);
 			item.addChild(i);
 		}
 			
 		
		item.addChild(customer.getItem("customerInfo"));
 		
 		return item;
 	}
 
 }
