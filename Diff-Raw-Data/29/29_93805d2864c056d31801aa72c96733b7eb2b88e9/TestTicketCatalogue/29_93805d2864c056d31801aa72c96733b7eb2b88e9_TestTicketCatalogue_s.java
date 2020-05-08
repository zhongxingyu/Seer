 package com.mycompany.booking;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import static com.mycompany.booking.TestTicketCatalogue.booking;
 import com.mycompany.booking.core.IBooking;
 import com.mycompany.booking.core.ITicketCatalogue;
 import com.mycompany.booking.core.JPABookingFactory;
 import com.mycompany.booking.core.Ticket;
 import java.util.List;
 import javax.persistence.Query;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test for Ticket catalog.
  *
  * NOTES: - Possible to run with embedded Derby (no server needed) - Possible to
  * run with server and create tables
  *
  * NOTE: Must have Table generation Strategy to Create (or Drop and Create)
  * NOTE: equals and hashCode can (should) possible be implemented in some other
  * way, though no common accepted way..
  *
  * @author hajo
  * Edited to fit our project
  */
 public class TestTicketCatalogue {
 
     static IBooking booking;
     final static String PU = "booking_pu";
     
 
     @Before // Run before each test
     public void before() {
         booking = JPABookingFactory.getBooking(PU);  
     }

     @Test
     public void testGetRange() {
         ITicketCatalogue tc = booking.getTicketCatalogue();
 
         for (int i = 0; i < 10; i++) {
             tc.add(new Ticket("Tåg",String.valueOf(i),String.valueOf(i+1), i));
         }
         List<Ticket> ps = tc.getRange(0, 2);
         assertTrue(ps.size() == 2);
     }
 
     @Test
     public void testAddUpdateAndRemoveTicket() {
         ITicketCatalogue tc = booking.getTicketCatalogue();
         System.out.println(tc.toString());
         
         //TODO fixa
        Ticket p1 = new Ticket("Tåg","Göteborg", "Kungsbacka", 57);
         tc.add(p1);
         assertTrue(tc.getCount() == 1);
 
         Ticket p2 = tc.find(p1.getId());
         // Not same transaction
         assertTrue(p2 != p1);
         // Equal by value
         
         assertTrue(p2.equals(p1));
 
         Ticket p = new Ticket(p1.getId(), "updated_med_swing", p1.getTicketPrice());
         tc.update(p);
         p1 = tc.find(p1.getId());
         /*
          * Id NOT changed here we have two Tickets with
          * same id but different state! 
          * Seems to be no single simple solution to this..?!
          */

         assertTrue(p2.equals(p1));
         assertFalse(p2.getDestination().equals(p1.getDestination()));
         //assertTrue(tc.getCount() == 1);
 
         tc.remove(p1.getId());
         assertTrue(tc.getCount() == 0);
         assertTrue(tc.find(p1.getId()) == null);
 
         // No change in program (but deleted from database)
         assertTrue(p2.equals(p1));
         assertFalse(p2.getDestination().equals(p1.getDestination()));
 
     }
 
     @Test
     public void testGetByDestination() {
         ITicketCatalogue tc = booking.getTicketCatalogue();
         Ticket p = new Ticket("Tåg","Kungsbacka", "Göteborg", 11.11);
         tc.add(p);
 
         List<Ticket> ps = tc.getByDestination("Kungsbacka");
         assertTrue(ps.size() == 1);
     }
     
     //@After
     public void after(){
         EntityManagerFactory emf = Persistence.createEntityManagerFactory(PU);
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         Query q2 = em.createNativeQuery("DELETE FROM APP.TICKET");
         q2.executeUpdate();
         em.getTransaction().commit();
         em.close();
    }
 }
