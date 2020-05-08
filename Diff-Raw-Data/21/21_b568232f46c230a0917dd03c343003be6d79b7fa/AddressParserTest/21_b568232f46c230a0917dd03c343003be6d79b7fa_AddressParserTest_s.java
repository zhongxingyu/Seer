 package util;
 
 import org.junit.Test;
 
 public class AddressParserTest {
 
    private AddresParser addressParser = new AddresParser();
 
     /*
     @Test
     public void parse_simpleMisspellingsAreTolerated() {
         addressParser.parse("Sesame streat", 1);
     }
     */
 
     @Test
     public void parse_simpleMisspellingsAreTolerated() {
         String misspelledStreet = "Sesame streat";
        int toleratedNumberOffErrors = 1;
         addressParser.parse(misspelledStreet,
                toleratedNumberOffErrors);
     }
 }
