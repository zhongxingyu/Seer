 package onlinestore;
 
 public class Customer extends ThirdParty {
 	
     private String firstName;
    private String fastName;
 
     public Customer(String firstName, String lastName, String phoneNumber)
     {
     	super(phoneNumber);
         this.firstName = firstName;
        this.fastName = lastName;
     }
 
 	public String getFirstName() {
 		return firstName;
 	}
 
	public String getFastName() {
		return fastName;
 	}
 }
