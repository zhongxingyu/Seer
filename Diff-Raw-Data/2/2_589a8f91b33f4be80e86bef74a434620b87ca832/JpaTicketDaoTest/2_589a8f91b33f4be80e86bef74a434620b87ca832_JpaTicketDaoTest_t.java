 package com.supinfo.ticketmanager.dao.jpa;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.EJBException;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import com.supinfo.ticketmanager.entity.*;
 import org.dbunit.DatabaseUnitException;
 import org.hibernate.Session;
 import org.hibernate.ejb.EntityManagerImpl;
 import org.hibernate.jdbc.Work;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.supinfo.ticketmanager.dao.TicketDao;
 import com.supinfo.ticketmanager.exception.UnknownTicketException;
 
 import fr.bargenson.util.test.dbunit.DbUnitUtils;
 
 @RunWith(Arquillian.class)
 public class JpaTicketDaoTest {
 
 	@Deployment
 	public static JavaArchive createTestArchive() {
 		return ShrinkWrap.create(JavaArchive.class)
 				.addClasses(TicketDao.class, JpaTicketDao.class)
 				.addPackage(Ticket.class.getPackage())
 				.addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
 				.addAsResource(new File("src/test/resources/META-INF/persistence.xml"), "META-INF/persistence.xml");
 	}
 
 	
 	@Inject TicketDao ticketDao;
 
 	@PersistenceContext EntityManager em;
 
 	
 	@Before
 	public void setUp() throws DatabaseUnitException, SQLException {
 		final Session session = ((EntityManagerImpl)em.getDelegate()).getSession();
 		session.doWork(new Work() {	
 			@Override
 			public void execute(Connection connection) throws SQLException {
 				DbUnitUtils.loadDataset(connection, "src/test/resources/db/insert-data.xml");
 			}
 		});
 	}
 
 	@Test
 	public void testAddTicket() {
 		final Date beforePersistDate = new Date();
 
         final ProductOwner po = new ProductOwner();
 		po.setId(1L);
 		
 		final Ticket ticket = new Ticket(
 				"summary", "description", TicketPriority.CRITICAL, 
 				TicketStatus.NEW, new Date(), po
 		);
 		
 		final Ticket persistedTicket = ticketDao.addTicket(ticket);
 		assertNotNull(persistedTicket);
 		assertNotNull(persistedTicket.getId());
         assertFalse(persistedTicket.getCreatedAt().before(beforePersistDate));
         assertFalse(persistedTicket.getCreatedAt().after(new Date()));
 
 		final Ticket retrievedTicket = em.find(Ticket.class, persistedTicket.getId());
 		assertEquals(persistedTicket, retrievedTicket);
 	}
 
     @Test
     public void testUpdateTicket() {
         final long ticketId = 1L;
         final String summary = "New Summary";
         final TicketStatus status = TicketStatus.DONE;
 
         final Ticket ticket = em.find(Ticket.class, ticketId);
         ticket.setSummary(summary);
         ticket.setStatus(status);
 
         ticketDao.updateTicket(ticket);
 
         final Ticket updatedTicket = em.find(Ticket.class, ticketId);
         assertNotNull(updatedTicket);
         assertEquals(summary, updatedTicket.getSummary());
         assertEquals(status, updatedTicket.getStatus());
     }
 
 	@Test
 	public void testGetAllTickets() {
 		final List<Ticket> tickets = ticketDao.getAllTickets();
 		assertNotNull(tickets);
		assertEquals(3, tickets.size());
 	}
 
 	@Test
 	public void testGetAllTicketsByStatus() {
 		final List<Ticket> newTickets = ticketDao.getAllTickets(TicketStatus.NEW);
 		assertNotNull(newTickets);
 		assertEquals(2, newTickets.size());
 
 		final List<Ticket> inProgressTickets = ticketDao.getAllTickets(TicketStatus.IN_PROGRESS);
 		assertNotNull(inProgressTickets);
 		assertEquals(1, inProgressTickets.size());
 	}
 	
 	@Test
 	public void testFindTicketById() {
 		final Long ticketId = 1L;
 		
 		Ticket ticket = ticketDao.findTicketWithCommentsById(ticketId);
 		
 		assertNotNull(ticket);
 		assertEquals(ticketId, ticket.getId());
 		assertNotNull(ticket.getComments());
 		assertFalse(ticket.getComments().isEmpty());
 		assertEquals(1, ticket.getComments().size());
 	}
 	
 	@Test(expected=UnknownTicketException.class)
 	public void testFindUnknownTicketById() {
 		final Long ticketId = 999L;
 		try {
 			ticketDao.findTicketWithCommentsById(ticketId);
 		} catch (EJBException e) {
 			assertTrue(e.getCause() instanceof UnknownTicketException);
 			throw (UnknownTicketException) e.getCause();
 		}
 	}
 
     @Test
     public void testGetTicketsByDeveloper() {
         final Developer developer = em.find(Developer.class, 2L);
         final List<Ticket> tickets = ticketDao.getTicketsByDeveloper(developer);
         assertNotNull(tickets);
         assertEquals(2, tickets.size());
     }
 
 }
