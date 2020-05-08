 package test;
 
import view.AddressListView;
 import view.decorator.AddressDecorator;
import model.AddressList;
 import model.EmailOnlyAddress;
 import model.PostalAddress;
 import junit.framework.TestCase;
 
 public class ModelTest extends TestCase {
 	
 	private EmailOnlyAddress emailAddress; 
  	private PostalAddress postalAddress; 
 
 	protected void setUp() throws Exception {	    
 		super.setUp(); 
 	 	                 
 	    emailAddress = new EmailOnlyAddress(); 
 	 	emailAddress.setName( "emailname" ); 
 	    emailAddress.setEmailaddress( "email" ); 
 	 	                 
 	    postalAddress = new PostalAddress(); 
 	 	postalAddress.setName( "postalname" ); 
 	 	postalAddress.setEmailaddress( "email" ); 
 		postalAddress.setStraße( "strasse" ); 
 		postalAddress.setHausnummer( "42" ); 
 	 	postalAddress.setPlz( "54321" ); 
 	 	postalAddress.setOrt( "Milkyway" ); 
	 	
	 	AddressList.getInstance().add(emailAddress);
	 	AddressList.getInstance().add(postalAddress);
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		
 	}
 	
 	public void testWithDecorator() { 
 		assertFalse( "Address is marked dirty", emailAddress.toString().startsWith(">") ); 
 		assertFalse( "Address is marked dirty", postalAddress.toString().startsWith(">") ); 
 	            
 		AddressDecorator decoratedEmail = new AddressDecorator(emailAddress); 
 		AddressDecorator decoratedPostal = new AddressDecorator(postalAddress); 
 	      
 		assertTrue( "Address is marked dirty", decoratedEmail.toString().startsWith(">")); 
 		assertTrue( "Address is marked dirty", decoratedPostal.toString().startsWith(">") ); 
     } 
 	
 	public void testWithoutDecorator() { 
 		 assertFalse( "Address is marked dirty", emailAddress.toString().startsWith(">") ); 
 		 assertFalse( "Address is marked dirty", postalAddress.toString().startsWith(">") ); 
      } 
 	 
 	 
 
 }
