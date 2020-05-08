 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
 
 public class AvailableTicketsTest
 {
     /* Objects used for executing the test suite */
     private AvailableTickets availableTickets;
     
     /* Parameters used for the tests */
     private static final String event = "The Godfather II";
     private static final String invalidEvent = "Invalid Movie";
     private static final String seller = "seller";
     private static final Integer volume = 25;
     private static final Double price = 10.00;
     
     @Rule
     public final StandardErrorStreamLog log = new StandardErrorStreamLog();
     
     /**
      * Test method for {@link AvailableTickets#addTicket(Ticket)}.
      * 
      * Tests the addTicket method in the AvailableTickets class, by executing 
      * the method and adding a random ticket to the class’ internal collection. 
      * The method is a simple one-line mutator method.
      * @throws IOException 
      * @throws FatalError 
      * @throws NumberFormatException 
      */
     @Test
     public void testAddTicket() throws NumberFormatException, FatalError, IOException
     {
         availableTickets = new AvailableTickets("files/AvailableTicketsTestAddTicket.atf");
         
         assertFalse(availableTickets.hasTicket(event, seller));
         
         /* Add a ticket and verify that it was added */
         availableTickets.addTicket(new Ticket(event, seller, volume, price));
         assertTrue(availableTickets.hasTicket(event, seller));
     }
 
     /**
      * Test method for {@link AvailableTickets#getTicket(java.lang.String, java.lang.String)}.
      * 
      * Tests the getTicket method in the AvailableTickets class, by executing 
      * the method with a valid ticket name and seller. The Ticket object 
      * corresponding to that name and seller must be returned.
      * @throws IOException 
      * @throws FatalError 
      * @throws NumberFormatException 
      */
     @Test
     public void testGetTicket1() throws NumberFormatException, FatalError, IOException
     {
         availableTickets = new AvailableTickets("files/AvailableTicketsTestGetTicket1.atf");
         
         /* Verify that the ticket for the event and seller exists */
         assertEquals(event, availableTickets.getTicket(event, seller).getEvent());
         assertEquals(seller, availableTickets.getTicket(event, seller).getSeller());
     }
 
     /**
      * Test method for {@link AvailableTickets#getTicket(java.lang.String, java.lang.String)}.
      * 
      * Tests the getTicket method in the AvailableTickets class, by executing the 
      * method with an invalid ticket name and seller. Null must be returned.
      * @throws IOException 
      * @throws FatalError 
      * @throws NumberFormatException 
      */
     @Test
     public void testGetTicket2() throws NumberFormatException, FatalError, IOException
     {
         availableTickets = new AvailableTickets("files/AvailableTicketsTestGetTicket2.atf");
         
         /* Verify that the ticket for the invalid event and seller does NOT exist */
         assertEquals(null, availableTickets.getTicket(invalidEvent, seller));
     }
     
     /**
      * Test method for {@link AvailableTickets#hasTicket(java.lang.String, java.lang.String)}.
      * 
      * Tests the getTicket method in the AvailableTickets class, by executing 
      * the method with a valid ticket name and seller. True must be returned.
      * @throws IOException 
      * @throws FatalError 
      * @throws NumberFormatException 
      */
     @Test
     public void testHasTicket1() throws NumberFormatException, FatalError, IOException
     {
         availableTickets = new AvailableTickets("files/AvailableTicketsTestHasTicket1.atf");
         
         /* Verify that the event is found */
         assertTrue(availableTickets.hasTicket(event, seller));
     }
     
     /**
      * Test method for {@link AvailableTickets#hasTicket(java.lang.String, java.lang.String)}.
      * 
      * Tests the getTicket method in the AvailableTickets class, by executing 
      * the method with an invalid ticket name and seller. False must be returned.
      * @throws IOException 
      * @throws FatalError 
      * @throws NumberFormatException 
      */
     @Test
     public void testHasTicket2() throws NumberFormatException, FatalError, IOException
     {
         availableTickets = new AvailableTickets("files/AvailableTicketsTestHasTicket2.atf");
         
         /* Verify that the invalid event is NOT found */
         assertFalse(availableTickets.hasTicket(invalidEvent, seller));
     }
 
     /**
      * Test method for {@link AvailableTickets#write()}.
      * 
      * Tests the write method in the AvailableTickets class, by executing 
      * the method, providing a filename that exists. The output must be 
      * written to the file, and the file closed.
      * @throws IOException 
      * @throws FatalError 
      * @throws NumberFormatException 
      */
     @Test
     public void testWrite() throws IOException, NumberFormatException, FatalError
     {
         /* Execute the write method with a valid blank ATF file */
         availableTickets = new AvailableTickets("files/AvailableTicketsTestWrite.atf");
         availableTickets.write();
     }
 
     /**
      * Test method for {@link AvailableTickets#parse()}.
      * 
      * Tests the parse method in the AvailableTickets class, by executing 
      * the the AvailableTickets constructor, using an invalid path to the 
      * available tickets file. A Fatal Error exception must be thrown.
      * @throws IOException 
      * @throws NumberFormatException 
      */
     public void testParse1() throws NumberFormatException, IOException
     {
         /* Verify that a fatal error ExceptionCodes.ATF_NOT_FOUND is thrown */
         try
         {
             availableTickets = new AvailableTickets("files/InvalidFileName.atf");
         }
         catch (FatalError e)
         {
             assertEquals(ExceptionCodes.ATF_NOT_FOUND.toString(), e.getMessage());
         }
     }
     
     /**
      * Test method for {@link AvailableTickets#parse()}.
      * 
      * Tests the parse method in the AvailableTickets class, by executing the 
      * AvailableTickets constructor, using a valid path to the available 
      * tickets file. The contents of the available tickets file must be 
      * corrupted. A Fatal Error exception must be thrown.
      * @throws IOException 
      * @throws NumberFormatException 
      */
     @Test
     public void testParse2() throws NumberFormatException, IOException
     {
         /* Verify that a fatal error ExceptionCodes.CORRUPT_ATF is thrown */
         try
         {
             availableTickets = new AvailableTickets("files/AvailableTicketsTestParse2.atf");
         }
         catch (FatalError e)
         {
             assertEquals(ExceptionCodes.CORRUPT_ATF.toString(), e.getMessage());
         }
     }
     
     /**
      * Test method for {@link AvailableTickets#parse()}.
      * 
      * Tests the parse method in the AvailableTickets class, by executing the
      * AvailableTickets constructor, using a valid path to the available tickets 
      * file. The contents of the available tickets file must be valid. Verify that 
      * a ticket has been added to the collection.
      * @throws IOException 
      * @throws FatalError 
      * @throws NumberFormatException 
      */
     @Test
     public void testParse3() throws NumberFormatException, FatalError, IOException
     {
         availableTickets = new AvailableTickets("files/AvailableTicketsTestParse3.atf");
         
         /* Verify that the ticket for the event and seller exists */
         assertEquals(event, availableTickets.getTicket(event, seller).getEvent());
         assertEquals(seller, availableTickets.getTicket(event, seller).getSeller());
     }    
 }
