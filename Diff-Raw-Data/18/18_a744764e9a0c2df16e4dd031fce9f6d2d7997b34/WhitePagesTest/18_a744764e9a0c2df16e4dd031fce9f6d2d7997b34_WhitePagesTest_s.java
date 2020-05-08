 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ws.whitepages.test;
 
 import org.junit.Test;
 import ws.whitepages.AddPersonFault;
 import ws.whitepages.PersonType;
 import ws.whitepages.WhitePagesWSDLPortType;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author kucharekm
  */
 public class WhitePagesTest {
 
     WhitePagesWSDLPortType port = (new ws.whitepages.WhitePagesWSDLService()).getWhitePagesWSDLPortTypeBindingPort();
 
     @Test
     public void testAddPersonNull() {
         testPerson(null, port);
 
     }
 
     @Test
     public void testAddPersonValid() {
         ws.whitepages.PersonType person = new ws.whitepages.PersonType();
         person.setFirstName("Jimmy");
         person.setLastName("Kimmel");
         person.setPhone("123");
 
         testPerson(person, port);
     }
 
     @Test
     public void testAddPersonNoPhone() {
         ws.whitepages.PersonType person = new ws.whitepages.PersonType();
         person.setFirstName("Jimmy");
         person.setLastName("Kimmel");
 
         testPerson(person, port);
     }
 
     @Test
     public void testAddPersonExisting() {
         for (int i = 0; i < 2; ++i) {
             ws.whitepages.PersonType person = new ws.whitepages.PersonType();
             person.setFirstName("Tony");
             person.setLastName("Hawk");
             person.setPhone("1233");
 
             testPerson(person, port);
         }
     }
 
     @Test
     public void testFindPersonExisiting() {
 
         ws.whitepages.PersonType person = new ws.whitepages.PersonType();
         person.setFirstName("Karol");
         person.setLastName("Nowak");
 
         testPerson(person, port);
 
        // TODO initialize WS operation arguments here
         ws.whitepages.PersonType input1 = new ws.whitepages.PersonType();
 
         input1.setFirstName("Karol");
 
        // TODO process result here
         ws.whitepages.PersonArrayType result = port.findPerson(input1);
         assertEquals(1, result.getPerson().size());
         assertEquals("Karol", result.getPerson().get(0).getFirstName());
         assertEquals("Nowak", result.getPerson().get(0).getLastName());

     }
 
     private void testPerson(PersonType person, WhitePagesWSDLPortType port) {
         try {
             java.lang.String result = port.addPerson(person);
             assertEquals("done", result);
 
         } catch (AddPersonFault e) {
             assertEquals("error", e.getMessage());
 
             if (person == null) {
                 assertEquals("Person cannot be null", e.getFaultInfo().getErrorMessage());
                 assertNull(e.getFaultInfo().getPerson());
 
             } else {
 
                 assertEquals(person.getFirstName(), e.getFaultInfo().getPerson().getFirstName());
                 assertEquals(person.getLastName(), e.getFaultInfo().getPerson().getLastName());
 
                 if (person.getPhone() == null) {
                     assertEquals("Phone cannot be null", e.getFaultInfo().getErrorMessage());
 
                 } else {
                     assertEquals("Person already exists", e.getFaultInfo().getErrorMessage());
 
                     assertEquals(person.getPhone(), e.getFaultInfo().getPerson().getPhone());
 
                     if (e.getFaultInfo().getPerson().getAddress() != null) {
                         assertEquals(person.getAddress().getCity(), e.getFaultInfo().getPerson().getAddress().getCity());
                         assertEquals(person.getAddress().getStreet(), e.getFaultInfo().getPerson().getAddress().getStreet());
                         assertEquals(person.getAddress().getPostcode(), e.getFaultInfo().getPerson().getAddress().getPostcode());
                     }
                 }
             }
         }
     }
 }
