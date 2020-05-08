 package org.clubrockisen.dao.mysql;
 
 import static org.clubrockisen.common.ConfigurationKeys.KEY;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.nio.file.Paths;
 import java.util.Set;
 
 import org.clubrockisen.common.ConfigurationKeys;
 import org.clubrockisen.entities.EntryMemberParty;
 import org.clubrockisen.entities.Member;
 import org.clubrockisen.entities.Party;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
import org.junit.Ignore;
 import org.junit.Test;
 
 import com.alexrnl.commons.database.dao.AbstractDAOFactory;
 import com.alexrnl.commons.database.dao.DataSourceConfiguration;
 import com.alexrnl.commons.utils.Configuration;
 
 /**
  * Test suite for the {@link MySQLEntryMemberPartyDAO} class.
  * @author Alex
  */
 public class MySQLEntryMemberPartyDAOTest {
 	/** The factory */
 	private static MySQLDAOFactory			factory;
 	/** Backup for the entries */
 	private static Set<EntryMemberParty>	entriesBackup;
 	/** The DAO for the entries */
 	private MySQLEntryMemberPartyDAO		entriesDAO;
 	
 	/**
 	 * Create the factory.
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass () {
 		final Configuration config = new Configuration(Paths.get(ConfigurationKeys.FILE));
 		final DataSourceConfiguration dbInfos = new DataSourceConfiguration(config.get(KEY.db().url()),
 				config.get(KEY.db().username()), config.get(KEY.db().password()),
 				Paths.get(config.get(KEY.db().creationFile())));
 		factory = AbstractDAOFactory.buildFactory(MySQLDAOFactory.class.getName(), dbInfos, MySQLDAOFactory.class);
		entriesBackup = factory.getEntryMemberPartyDAO().retrieveAll();
 	}
 	
 	/**
 	 * Close the resources.
 	 */
 	@AfterClass
 	public static void tearDownAfterClass () {
 		try {
			assertEquals(entriesBackup, factory.getEntryMemberPartyDAO().retrieveAll());
 			factory.close();
 		} catch (final IOException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Set up the attribute required for testing.
 	 */
 	@Before
 	public void setUp () {
 		entriesDAO = (MySQLEntryMemberPartyDAO) factory.getEntryMemberPartyDAO();
 	}
 	
 	/**
 	 * Test method for {@link org.clubrockisen.dao.mysql.MySQLEntryMemberPartyDAO#getEntitySample()}.
 	 */
 	@Test
 	public void testGetEntitySample () {
 		assertNotNull(entriesDAO.getEntitySample());
 	}
 	
 	/**
 	 * Test method for {@link org.clubrockisen.dao.mysql.MySQLEntryMemberPartyDAO#create(org.clubrockisen.entities.EntryMemberParty)}.
 	 */
 	@Test
 	public void testCreate () {
 		// Null case
 		assertNull(entriesDAO.create(null));
 		
 		// Regular cases
 		// Prepare reference member
 		boolean deleteM = false;
 		Member member = factory.getMemberDAO().find(1);
 		if (member == null) {
 			member = new Member();
 			member.setIdMember(1);
 			member = factory.getMemberDAO().create(member);
 			deleteM = true;
 		}
 		// Prepare reference party
 		boolean deleteP = false;
 		Party party = factory.getPartyDAO().find(1);
 		if (party == null) {
 			party = new Party();
 			party.setIdParty(1);
 			party = factory.getPartyDAO().create(party);
 			deleteP = true;
 		}
 		
 		final EntryMemberParty entry = new EntryMemberParty(-1, member.getIdMember(), party.getIdParty());
 		final EntryMemberParty found = entriesDAO.create(entry);
 		if (found == null) {
 			fail("Failed to create entry, check error log");
 			return;
 		}
 		assertEquals(member.getIdMember(), found.getIdMember());
 		assertEquals(party.getIdParty(), found.getIdParty());
 		
		assertTrue(entriesDAO.delete(found));
 		if (deleteM) {
 			assertTrue(factory.getMemberDAO().delete(member));
 		}
 		if (deleteP) {
 			assertTrue(factory.getPartyDAO().delete(party));
 		}
 	}
 	
 	/**
 	 * Test method for {@link org.clubrockisen.dao.mysql.MySQLEntryMemberPartyDAO#update(org.clubrockisen.entities.EntryMemberParty)}.
 	 */
	@Ignore
 	@Test
 	public void testUpdate () {
 		fail("Not yet implemented"); // TODO
 	}
 	
 	/**
 	 * Test method for {@link org.clubrockisen.dao.mysql.MySQLDAO#find(int)}.
 	 */
	@Ignore
 	@Test
 	public void testFind () {
 		fail("Not yet implemented"); // TODO
 	}
 	
 	/**
 	 * Test method for {@link org.clubrockisen.dao.mysql.MySQLDAO#delete(org.clubrockisen.entities.Entity)}.
 	 */
	@Ignore
 	@Test
 	public void testDelete () {
 		fail("Not yet implemented"); // TODO
 	}
 	
 	/**
 	 * Test method for {@link org.clubrockisen.dao.mysql.MySQLDAO#retrieveAll()}.
 	 */
	@Ignore
 	@Test
 	public void testRetrieveAll () {
 		fail("Not yet implemented"); // TODO
 	}
 	
 	/**
 	 * Test method for {@link org.clubrockisen.dao.mysql.MySQLDAO#search(org.clubrockisen.entities.Column, java.lang.String)}.
 	 */
	@Ignore
 	@Test
 	public void testSearch () {
 		fail("Not yet implemented"); // TODO
 	}
 }
