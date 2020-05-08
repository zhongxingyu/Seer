 package de.kile.zapfmaster2000.rest.core.box;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.kile.zapfmaster2000.rest.AbstractDatabaseTest;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.core.configuration.ConfigurationConstants;
 import de.kile.zapfmaster2000.rest.impl.core.box.DrawServiceImpl;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Box;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Drawing;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Keg;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.UserType;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Factory;
 
 public class TestDrawService extends AbstractDatabaseTest {
 
 	private static final long RFID_TAG_1 = 123;
 
 	private static final long RFID_TAG_2 = 234;
 
 	private Box box;
 
 	@Before
 	public void createUsers() {
 		User user1 = Zapfmaster2000Factory.eINSTANCE.createUser();
 		user1.setName("Harry");
 		user1.setRfidTag(RFID_TAG_1);
 
 		User user2 = Zapfmaster2000Factory.eINSTANCE.createUser();
 		user2.setName("Ron");
 		user2.setRfidTag(RFID_TAG_2);
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 		session.save(user1);
 		session.save(user2);
 		tx.commit();
 	}
 
 	@Before
 	public void createAccount() {
 		Keg keg1 = Zapfmaster2000Factory.eINSTANCE.createKeg();
 		keg1.setStartDate(new GregorianCalendar(2012, 10, 17).getTime());
 		Keg keg2 = Zapfmaster2000Factory.eINSTANCE.createKeg();
 		keg2.setStartDate(new GregorianCalendar(2012, 11, 03).getTime());
 
 		box = Zapfmaster2000Factory.eINSTANCE.createBox();
 		box.getKegs().add(keg1);
 		box.getKegs().add(keg2);
 		
 		Account account = Zapfmaster2000Factory.eINSTANCE.createAccount();
 		account.getBoxes().add(box);
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 		session.save(account);
 		tx.commit();
 	}
 
 	@After
 	public void waitForAutoLogout() throws InterruptedException {
 		int time = Zapfmaster2000Core.INSTANCE.getConfigurationService()
 				.getInt(ConfigurationConstants.BOX_LOGIN_AUTO_LOGOUT);
 		Thread.sleep((long) (time * 1.1)); // +10% to have time to write stuff
 											// to the database
 	}
 
 	@Test
 	public void testLoginSimple() {
 		DrawServiceImpl mgr = new DrawServiceImpl(
 				Zapfmaster2000Factory.eINSTANCE.createBox());
 		User user = mgr.login(RFID_TAG_1);
 		assertNotNull(user);
 		assertEquals("Harry", user.getName());
 	}
 
 	@Test
 	public void testLoginTwice() {
 		DrawServiceImpl mgr = new DrawServiceImpl(
 				Zapfmaster2000Factory.eINSTANCE.createBox());
 
 		User user = mgr.login(RFID_TAG_1);
 		assertNotNull(user);
 		assertEquals("Harry", user.getName());
 
 		user = mgr.login(RFID_TAG_1);
 		assertNotNull(user);
 		assertEquals("Harry", user.getName());
 	}
 
 	@Test
 	public void testLoginOtherUserAlreadyLoggedIn() {
 		DrawServiceImpl mgr = new DrawServiceImpl(
 				Zapfmaster2000Factory.eINSTANCE.createBox());
 
 		User user = mgr.login(RFID_TAG_1);
 		assertNotNull(user);
 		assertEquals("Harry", user.getName());
 
 		user = mgr.login(RFID_TAG_2);
 		assertNull(user); // harry is still logge in
 	}
 
 	@Test
 	public void testAutoLogout() throws InterruptedException {
 		DrawServiceImpl mgr = new DrawServiceImpl(box);
 
 		User user = mgr.login(RFID_TAG_1);
 		assertNotNull(user);
 		assertEquals("Harry", user.getName());
 
 		user = mgr.login(RFID_TAG_2);
 		assertNull(user); // harry is still logged in
 
 		// wait for auto log-off
 		waitForAutoLogout();
 
 		// ron logs in one more, should succeed now
 		user = mgr.login(RFID_TAG_2);
 		assertNotNull(user);
 		assertEquals("Ron", user.getName());
 	}
 
 	@Test
 	public void testUserDoesNotExist() {
 		DrawServiceImpl mgr = new DrawServiceImpl(
 				Zapfmaster2000Factory.eINSTANCE.createBox());
 
 		User user = mgr.login(-1);
 		assertNull(user);
 	}
 
 	@Test
 	public void testSimpleDraw() throws InterruptedException {
 		DrawServiceImpl mgr = new DrawServiceImpl(box);
 		mgr.login(RFID_TAG_1);
 		mgr.draw(2000);
 
 		// wait for auto log-off
 		waitForAutoLogout();
 
 		// check if drawing was added to database
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 		@SuppressWarnings("unchecked")
 		List<Drawing> drawings = session.createQuery("FROM Drawing").list();
 
 		assertEquals(1, drawings.size());
 		Drawing drawing = drawings.get(0);
		int ticksPerLiter = Zapfmaster2000Core.INSTANCE
				.getConfigurationService().getInt(
						ConfigurationConstants.BOX_DRAW_TICKS_PER_LITER);
 		assertEquals((double) 2000 / ticksPerLiter, drawing.getAmount(), 1);
 
 		// check if right user was chosen
 		assertNotNull(drawing.getUser());
 		assertEquals(RFID_TAG_1, drawing.getUser().getRfidTag());
 
 		// check if right keg was chosen
 		assertNotNull(drawing.getKeg());
 		assertEquals(new GregorianCalendar(2012, 11, 03).getTime(), drawing
 				.getKeg().getStartDate()); // this is keg2
 
 		tx.commit();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testDrawGuestUser() throws InterruptedException {
 		// note: there is no guest user in the database yet
 		DrawServiceImpl mgr = new DrawServiceImpl(box);
 		mgr.draw(2000); // drawing without login
 		waitForAutoLogout();
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		// check that guest user was created
 		List<User> users = session
 				.createQuery("FROM User u WHERE u.type=:guest")
 				.setParameter("guest", UserType.GUEST).list();
 		assertEquals(1, users.size());
 
 		// check if drawing was added to database
 		List<Drawing> drawings = session.createQuery(
 				"SELECT d FROM Drawing d JOIN d.user").list();
 
 		assertEquals(1, drawings.size());
 		Drawing drawing = drawings.get(0);
 		assertEquals(users.get(0), drawing.getUser());
 		tx.commit();
 
 		// draw once more as guest. no new user should be created now!
 		mgr.draw(2000); // drawing without login
 		waitForAutoLogout();
 
 		session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		tx = session.beginTransaction();
 
 		// check that there is still only one guest user
 		users = session.createQuery("FROM User u WHERE u.type=:guest")
 				.setParameter("guest", UserType.GUEST).list();
 		assertEquals(1, users.size());
 
 		// check if drawing was added to database
 		drawings = session.createQuery("SELECT d FROM Drawing d JOIN d.user")
 				.list();
 
 		assertEquals(2, drawings.size());
 		tx.commit();
 
 	}
 }
