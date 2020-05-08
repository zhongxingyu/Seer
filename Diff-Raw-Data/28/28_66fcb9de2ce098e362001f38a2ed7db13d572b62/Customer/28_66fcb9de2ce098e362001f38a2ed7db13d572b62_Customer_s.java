 package streamfish;
 
 public class Customer {
 
 	private int CUSTOMER_ID;
 	private int priceReduction = 0;
 	private double percentageReduction = 1.0;
 	private String customerName;
 	private String phone;
 	private boolean business = false;
 
 	public Customer(int customer_id, String customerName, String phone, boolean business, int priceReduction) {
 		if (customerName == null || customerName.trim().equals("")) {
 			throw new IllegalArgumentException("Customer's name is required.");
 		}
 		this.CUSTOMER_ID = customer_id;
 		this.customerName = customerName;
 		this.phone = phone;
 		this.business = business;
 		this.priceReduction = priceReduction;
 	}
 
 	public Customer(String customerName, String phone, boolean business, int priceReduction) {
 		if (customerName == null || customerName.trim().equals("")) {
 			throw new IllegalArgumentException("Customer's name is required.");
 		}
 		customerName = customerName.replace(";", "");
 		this.customerName = customerName;
 		this.phone = phone;
 		this.business = business;
 		this.priceReduction = priceReduction;
 	}
 
 	public Customer(int customer_id, String customerName, String phone) {
 		if (customerName == null || customerName.trim().equals("")) {
 			throw new IllegalArgumentException("Customer's name is required.");
 		}
 		this.CUSTOMER_ID = customer_id;
 		this.customerName = customerName;
 		this.phone = phone;
 	}
 
 	public int getCustomerID() {
 		return CUSTOMER_ID;
 	}
 
 	public String getCustomerName() {
 		return customerName;
 	}
 
 	public String getPhoneNumber() {
 		return phone;
 	}
 
 	public boolean isBusiness() {
 		return business;
 	}
 
 	public int getPriceReduction() {
 		return priceReduction;
 	}
         
         public double getPercentageReduction() {
            double reduction = (100 - (100 * getPriceReduction()));
             return reduction;
         }
 
 	public void setCustomerName(String newName) {
 		if (newName == null || newName.trim().equals("")) {
 			throw new IllegalArgumentException("Customer's name is required.");
 		}
 		customerName = newName;
 	}
 
 	public void setPhoneNumber(String newPhone) {
 		phone = newPhone;
 	}
 
 	public void setBusiness(boolean change) {
 		if (business != change) {
 			business = change;
 		}
 	}
 
 	public void setPercentageReduction(double newPercentage) {
                 double percentage = 1 - (newPercentage / 100);
 		if (percentage > 0.0 && percentage < 1.0) {
 			percentageReduction = percentage;
 		}
 	}
 
 	@Override
 	public String toString() {
 		String info = "Customer ID: " + CUSTOMER_ID + ", name: " + customerName
 				+ ", phonenumber: " + phone;
 		info += (business) ? ", bedrift med "
 				+ getPercentageReduction() + "% rabatt" : "";
 		return info;
 	}
 
 	public static void main(String[] args) {
 		Customer priv1 = new Customer(1, "Håkon Jarle Hassel", "92881009");
 		Customer priv2 = new Customer(2, "Per Persen", "10120455");
 		Customer bedr1 = new Customer(3, "Evry", "99778866", true, 10);
 
 		System.out.println(priv1);
 		System.out.println(bedr1);
 		System.out.println(priv2);
 
 		bedr1.setPercentageReduction(15.0);
                 System.out.println(bedr1.getPercentageReduction() + "%");
 		System.out.println(bedr1);
 	}
 }
