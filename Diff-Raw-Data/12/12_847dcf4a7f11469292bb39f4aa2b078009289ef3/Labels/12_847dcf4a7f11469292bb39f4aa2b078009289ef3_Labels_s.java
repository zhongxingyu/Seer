 package delicious.pos.config;
 
 import java.util.HashMap;
 
 public class Labels {
 	private HashMap<String, String> labels;
 	
 	public Labels() {
 		this.labels = new HashMap<String, String>();
 		this.setup();
 	}
 	
 	public String get(String label) {
 		return this.labels.get(label);
 	}
 	
 	private void setup() {
 		this.labels.put("restaurant-name", "Greek Paradise");
 		this.labels.put("order-summary", "Order summary");
 		this.labels.put("customer-details", "Customer details:");
 		this.labels.put("ordered-items", "Ordered items:");
 		this.labels.put("items-for", "items for:");
 		this.labels.put("currency", "");
 		this.labels.put("delivery", "delivery");
 		this.labels.put("takeaway", "take away");
 		this.labels.put("eathere", "eat here");
 		this.labels.put("specify-order", "Select order type and customer");
 		this.labels.put("app-title", "Greek Paradise - a place greeks love!");
 		this.labels.put("admin-btn", "Admin");
 		this.labels.put("menu-btn", "Menu");
 		this.labels.put("home-h2", "...you're at home!");
 		this.labels.put("menu-h2", "The menu, place order...");
		this.labels.put("order-validation-h2", "Please verify order...");
 		this.labels.put("order-placement-h2", "Please verify si bloody order...");
 		this.labels.put("admin-h2", "Administer the shit out of YOUR data!");
 		this.labels.put("step-of", "of");
 		this.labels.put("firstName", "Firstname");
 		this.labels.put("lastName", "Surname");
 		this.labels.put("street", "Street");
 		this.labels.put("zip", "ZIP");
 		this.labels.put("city", "City");
 		this.labels.put("phone", "Phone");
 		this.labels.put("userName", "Username");
 		this.labels.put("salary", "Salary");
 		this.labels.put("position", "Position");
 		this.labels.put("name", "Name");
 		this.labels.put("description", "Description");
 		this.labels.put("orderTypeName", "Order type");
 		this.labels.put("employeUserName", "Employee's username");
 		this.labels.put("number", "Number");
 		this.labels.put("id", "ID");
 		this.labels.put("priceId", "Price ID");
 		this.labels.put("customerId", "Customer's ID");
 		this.labels.put("tableId", "Table ID");
 		this.labels.put("value", "Value");
 		this.labels.put("sizeValue", "Size");
 		this.labels.put("itemName", "Name");
 	}
 }
