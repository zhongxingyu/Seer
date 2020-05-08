 package assignments.FRS.chap2;
 
 
 /** The EmailAddress class represents an email address. Here only a
   very limited variant is used that allows an address to be defined
   and verified. A typical usage example is:
   EmailAddress ea = new EmailAddress("john@company.com");
   boolean isProper = ea.isValid();
 
   Please note that the implementation of isValid is on purpose WRONG!
  
    This source code is from the book 
      "Flexible, Reliable Software:
        Using Patterns and Agile Development"
      published 2010 by CRC Press.
    Author: 
      Henrik B Christensen 
      Computer Science Department
      Aarhus University
    
    This source code is provided WITHOUT ANY WARRANTY either 
    expressed or implied. You may study, use, modify, and 
    distribute it for non-commercial purposes. For any 
    commercial use, see http://www.baerbak.com/
 */
 public class EmailAddress {
   private String address;
   public EmailAddress(String address) {
     this.address = address;
   }
   /**
    * Verify that a string is an email address.
    * The string must obey the following (somewhat strict format)
    * <email>      ::= <identifier> @ <identifier> {. <identifier>}
    * <identifier> ::= letter { letter | digit }
    * 
    * Example: abc@somewhere12.mail.com is correct whereas the
    * following are incorrect: 
    *
    * 13a@s.m.com (identifier starting with digit)
    * a-c@s.m.com (non letter in identifier)
    * a.b.c (missing @)
    * abc@ (missing identifier after @)
    * @return true if the address obeys this format.
   */
   public boolean isValid() {
     // This implementation is wrong and could be expressed more
     // elegantly using regular expressions.
 	  
 //    if ( address == null ) return false;
 //    if ( address.length() == 0 ) return false;
 //    int atPosition = address.indexOf( '@' );
 //    if ( atPosition == -1 ) return false;
 //    int dotPosition = address.indexOf('.');
 //    if ( dotPosition < atPosition ) return false;
 	  
	  if(address.matches("([A-Za-z_])(\\w+)*@(\\w+\\.)(\\w+)(\\.\\w+)*")){
 		  return true;
 	  }
 	  else {
 		  return false;
 	  }
 	  
   }
 }
