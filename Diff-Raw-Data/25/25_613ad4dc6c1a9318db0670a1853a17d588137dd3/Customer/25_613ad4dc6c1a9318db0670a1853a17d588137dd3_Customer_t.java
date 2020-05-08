 package onlinestore;
 
 public class Customer extends ThirdParty {
 	
     private String firstName;
    private String lastName;
 
     public Customer(String firstName, String lastName, String phoneNumber)
     {
     	super(phoneNumber);
         this.firstName = firstName;
        this.lastName = lastName;
     }
 
 	public String getFirstName() {
 		return firstName;
 	}
 
	public String getLastName() {
		return lastName;
 	}
 }
