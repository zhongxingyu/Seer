package VideoRentalHouse;
 
 public class Customer 
 {
 	private int id;
 	private String name;
 	private String ssn;
 	private Item[] rentedItems;
 	private String address;
 	private String preference;
 	
 	public Customer() 
 	{
 		super();
 		this.id = 0;
 		this.name = "";
 		this.ssn = "";
 		this.rentedItems = null;
 		this.address = "";
 		this.preference = "";
 	}
 
 	public Customer(int id, String name, String ssn, Item[] rentedItems,
 			String address, String preference) 
 	{
 		super();
 		this.id = id;
 		this.name = name;
 		this.ssn = ssn;
 		this.rentedItems = rentedItems;
 		this.address = address;
 		this.preference = preference;
 	}
 	
 	public void changeInfo(int id, String name, String ssn, Item[] rentedItems,
 			String address, String preference)
 	{
 		this.id = id;
 		this.name = name;
 		this.ssn = ssn;
 		this.rentedItems = rentedItems;
 		this.address = address;
 		this.preference = preference;
 	}
 	
 }
