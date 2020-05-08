 package org.lifecycle.dao;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.lifecycle.domain.Ticket;
 import org.lifecycle.persistence.TicketDao;
 import org.lifecycle.transaction.TestUnderTransaction;
 
 import java.util.List;
 import java.util.Set;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 public class TicketDaoTest extends TestUnderTransaction{
 
     TicketDao dao;
 
     @BeforeClass
     public static void init() throws Exception{
         initDB("conf/lifecycle-test.json",  asList(Ticket.class));
     }
 
     @Before
     public void setUp(){
         startSession();
         cleanTables(asSet("TICKETS", "LABELS"));
         dao = new TicketDao(getSessionFactory());
     }
 
     @Test
     public void givenTicketWithNotUniqueLabes_whenSaveTicket_LabelsAreUniqueInTicket() {
         dao.saveOrUpdate(newTicket(asSet("sprint 1", "sprint 1", "sprint 2", "sprint 1", "sprint 2")));
         List<Ticket> all = dao.findAll();
         Ticket ticket = all.get(0);
         assertThat(ticket.getLabels().size()).isEqualTo(2);
     }
 
     @Test
     public void givenTicketWithTwoLabels_afterSecondSaveWithOneLabel_WillHasOnlyOneLabel(){
         Ticket ticket = dao.saveOrUpdate(newTicket(asSet("A", "B")));
         commitAndOpenNewSession();
         ticket = dao.saveOrUpdate(new Ticket(ticket.getId(), "B Ticket", 1, asSet("A")));
         assertThat(ticket.getLabels().size()).isEqualTo(1);
     }
 
     @Test
     public void findTicketsByLabel(){
         dao.saveOrUpdate(newTicket(asSet("AAA")));
         dao.saveOrUpdate(newTicket(asSet("AAA")));
         dao.saveOrUpdate(newTicket(asSet("BBB")));
         commitAndBeginNewTransaction();
         List<Ticket> aTickets = dao.findByLabel("AAA");
         assertThat(aTickets.size()).isEqualTo(2);
     }
 
     private Ticket newTicket() {
         return new Ticket(null, "A", 1, null);
     }
 
     private Ticket newTicket(Set<String> labels) {
         return new Ticket(null, "default title", 1, labels);
     }
 
     @After
     public void tearDown(){
         rollbakAndCloseSession();
     }
 
 }
