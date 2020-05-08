 package util;
 
 import org.junit.Test;
 
 public class AddressParserTest {
 
    private AddressParser addressParser = new AddressParser();
 
     /*
     @Test
     public void parse_simpleMisspellingsAreTolerated() {
         addressParser.parse("Sesame streat", 1);
     }
     */
 
     @Test
     public void parse_simpleMisspellingsAreTolerated() {
         String misspelledStreet = "Sesame streat";
        int toleratedNumberOfErrors = 1;
         addressParser.parse(misspelledStreet,
                toleratedNumberOfErrors);
     }
 }
