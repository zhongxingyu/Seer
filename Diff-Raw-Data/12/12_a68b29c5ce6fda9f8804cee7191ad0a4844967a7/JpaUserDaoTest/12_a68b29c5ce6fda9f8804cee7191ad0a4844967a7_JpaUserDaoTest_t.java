 package com.supinfo.ticketmanager.dao.jpa;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.hibernate.Session;
 import org.hibernate.ejb.EntityManagerImpl;
 import org.hibernate.jdbc.Work;
 import org.jboss.arquillian.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.supinfo.ticketmanager.dao.UserDao;
 import com.supinfo.ticketmanager.entity.Developer;
 import com.supinfo.ticketmanager.entity.ProductOwner;
 import com.supinfo.ticketmanager.entity.User;
 
 import fr.bargenson.util.test.dbunit.DbUnitUtils;
 
 @RunWith(Arquillian.class)
 public class JpaUserDaoTest {
 	
 	@Deployment
 	public static JavaArchive createTestArchive() {
 		return ShrinkWrap.create(JavaArchive.class)
 				.addClasses(UserDao.class, JpaUserDao.class)
 				.addPackage(User.class.getPackage())
 				.addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
 				.addAsResource(new File("src/test/resources/META-INF/persistence.xml"), "META-INF/persistence.xml");
 	}
 	
 	
 	@EJB UserDao userDao;
 	
 	@PersistenceContext EntityManager em;
 
 	
 	@Before
 	public void setUp() {
 		final Session session = ((EntityManagerImpl)em.getDelegate()).getSession();
 		session.doWork(new Work() {	
 			@Override
 			public void execute(Connection connection) throws SQLException {
 				DbUnitUtils.loadDataset(connection, "src/test/resources/db/insert-data.xml");
 			}
 		});
 	}
 	
 	@Test
 	public void testAddProductOwner() {
 		final ProductOwner persistedUser = userDao.addUser(getSimpleProductOwner());
 		assertNotNull(persistedUser);
 		assertNotNull(persistedUser.getId());
 		
 		final ProductOwner retrievedUser = em.find(ProductOwner.class, persistedUser.getId());
 		assertEquals(persistedUser, retrievedUser);
 	}
 	
 	@Test
 	public void testAddDeveloper() {
 		final Developer persistedUser = userDao.addUser(getSimpleDeveloper());
 		assertNotNull(persistedUser);
 		assertNotNull(persistedUser.getId());
 		
 		final Developer retrievedUser = em.find(Developer.class, persistedUser.getId());
 		assertEquals(persistedUser, retrievedUser);
 	}
 	
 	@Test
 	public void testFindUserByUsername() {
 		final String productOwnerUsername = "ProductOwner";
 		final String developerUsername = "Developer";
 		
 		final User productOwner = userDao.findUserByUsername(productOwnerUsername);
 		assertNotNull(productOwner);
 		assertTrue(productOwner instanceof ProductOwner);
 		
 		final User developer = userDao.findUserByUsername(developerUsername);
 		assertNotNull(developer);
 		assertTrue(developer instanceof Developer);
 	}
 	
 	@Test
 	public void testGetAllUsers() {
 		final List<User> usersBeforeAdd = userDao.getAllUsers();
 		assertNotNull(usersBeforeAdd);
 		assertEquals(2, usersBeforeAdd.size());
 		
 		userDao.addUser(getSimpleDeveloper());
 		
 		final List<User> usersAfterAdd = userDao.getAllUsers();
 		assertNotNull(usersAfterAdd);
 		assertEquals(3, usersAfterAdd.size());
 	}
 	
 	@Test
 	public void testGetAllDevelopers() {
 		final List<Developer> usersBeforeAdd = userDao.getAllDevelopers();
 		assertNotNull(usersBeforeAdd);
 		assertEquals(1, usersBeforeAdd.size());
 		
 		userDao.addUser(getSimpleDeveloper());
 		userDao.addUser(getSimpleProductOwner());
 		
 		final List<Developer> usersAfterAdd = userDao.getAllDevelopers();
 		assertNotNull(usersAfterAdd);
 		assertEquals(2, usersAfterAdd.size());
 	}
 	
 	@Test
 	public void testGetAllProductOwners() {
 		final List<ProductOwner> usersBeforeAdd = userDao.getAllProductOwners();
 		assertNotNull(usersBeforeAdd);
 		assertEquals(1, usersBeforeAdd.size());
 		
 		userDao.addUser(getSimpleDeveloper());
 		userDao.addUser(getSimpleProductOwner());
 		
 		final List<ProductOwner> usersAfterAdd = userDao.getAllProductOwners();
 		assertNotNull(usersAfterAdd);
 		assertEquals(2, usersAfterAdd.size());
 	}
 	
 	private static Developer getSimpleDeveloper() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(1987, 8, 16, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
 		
 		return new Developer(
 				"username", "password", "firstName", 
				"lastName", "email@email.fr", calendar.getTime()
 		);
 	}
 	
 	private static ProductOwner getSimpleProductOwner() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(1987, 8, 16, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
 		return new ProductOwner(
 				"username", "password", "firstName", 
				"lastName", "email@email.fr", calendar.getTime()
 		);
 	}
 
 }
