 package app;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 public class Customer implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6899766195311964778L;
 	public static final int SURNAME = 1;
 	public static final int PHONENO = 2;
 	private ArrayList<Booking> bookings;
 	private boolean active;
 	
 	String firstname, surname, address, phoneNo, secretAnswer;
 
 	public Customer(String firstname, String surname, String address,
 			String phoneNo, String secretAnswer) {
 		super();
 		this.firstname = firstname;
 		this.surname = surname;
 		this.address = address;
 		this.phoneNo = phoneNo;
 		this.secretAnswer = secretAnswer;
 		bookings = new ArrayList<Booking>();
 		this.active = true;
 	}
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 
 	public String getSurname() {
 		return surname;
 	}
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getPhoneNo() {
 		return phoneNo;
 	}
 
 	public void setPhoneNo(String phoneNo) {
 		this.phoneNo = phoneNo;
 	}
 
 	public String getSecretAnswer() {
 		return secretAnswer;
 	}
 
 	public void setSecretAnswer(String secretAnswer) {
 		this.secretAnswer = secretAnswer;
 	}
 
 	@Override
 	public String toString() {
 		return surname + " " + firstname + " : " +address;
 	}
 	
 	public static ArrayList<Customer> customerSearch(ArrayList<Customer> customers, String searchString, int searchType){
 		ArrayList<Customer> returnList = new ArrayList<Customer>();
 		
 		switch(searchType){
 		
 			case Customer.SURNAME:
 				for (Customer customer : customers){
					if(customer.getSurname().contains(searchString) && customer.isActive())  
 						returnList.add(customer);
 				}
 			break;
 			
 			case Customer.PHONENO:
 				for (Customer customer : customers){
					if(customer.getPhoneNo().contains(searchString) && customer.isActive())  
 						returnList.add(customer);
 				}
 			break;
 		}
 		return returnList;
 	}
 	
	public boolean isActive() {
		return active;
	}

 	public void addBooking(Booking booking){
 		bookings.add(booking);
 	}
 	
 	public void removeBooking(Booking booking){
 		bookings.remove(booking);
 	}
 
 	public ArrayList<Booking> getBookings() {
 		return bookings;
 	}
 	
 	public void deactivate(){
		ArrayList<Booking> eraseList = new ArrayList<Booking>();
 		this.active = false;
		
		//copy contents from list into remove list to over come concurrency problem when removing object by iterator contained in list. 
		for(Booking booking : bookings){
			eraseList.add(booking);		
		}
		
		//here we can call remove function from this list as this list is not altered by remove function
		//contents of this list will disappear at end of method due to method scope 
		for(Booking booking : eraseList)
			booking.remove();
		//booking objects will be garbage collected as they are no longer referenced 
 	}
 	
 	
 }
