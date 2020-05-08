 /**
  * 
  */
 package net.frontlinesms.data.repository.hibernate;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.ContactDao;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.data.repository.GroupMembershipDao;
 import net.frontlinesms.junit.HibernateTestCase;
 
 /**
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 public class HibernateGroupMembershipDaoTest extends HibernateTestCase {
 	/** The dao for managing group memberships. */
 	private GroupMembershipDao dao;
 	/** The dao for managing {@link Contact}s */
 	private ContactDao contactDao;
 	/** The dao for managing {@link Group}s */
 	private GroupDao groupDao;
 	
 //> TEST METHODS
 	/** Test the basics of adding and removing a single member to a top-level group. */
 	public void testBasics() throws DuplicateKeyException {
 		// Create a contact and a group, and test adding and removing the contact from the group
 		Contact contact = createContact("Test contact");
 		
 		Group group = createGroup("My Group");
 		
 		testRelationship(group);
 		
 		this.dao.addMember(group, contact);
 		testRelationship(group, contact);
 		
 		this.dao.removeMember(group, contact);
 		testRelationship(group);
 	}
 	
 	public void testFiltering() throws DuplicateKeyException {
 		// Create the groups
 		Group parent = createGroup("parent");
 		Group child1 = createGroup(parent, "child1");
 		Group child2 = createGroup(parent, "child2");
 
 		// Create the contacts
 		Contact alice = createContact("Alice", "+123456789", child1);
 		Contact arnold = createContact("Arnold", "0123456789", child1);
 		Contact brigitte = createContact("Brigitte", "+111555999", child2);
 		Contact brian = createContact("Brian", "0111555999", child2);
 		Contact caroline = createContact("Caroline", "+987654321", parent);
 		Contact charles = createContact("Charles", "0987654321", parent);
 		Contact xuxa = createContact("Xuxa", "+111555000");
 		Contact xavier = createContact("Xavier", "0111555000");
 
 		// Test empty filter
 		testFiltering(getRootGroup(), "",
 				alice, arnold, brigitte, brian, caroline, charles, xuxa, xavier);
 		testFiltering(parent, "",
 				alice, arnold, brigitte, brian, caroline, charles);
 		testFiltering(child1, "",
 				alice, arnold);
 		testFiltering(child2, "",
 				brigitte, brian);
 		
 		// Test filtering by phone number
 		testFiltering(getRootGroup(), "0",
 				arnold, brian, charles, xuxa, xavier);
 		testFiltering(parent, "0",
 				arnold, brian, charles);
 		testFiltering(child1, "0",
 				arnold);
 		testFiltering(child2, "0",
 				brian);
 		
 		testFiltering(getRootGroup(), "123",
 				alice, arnold);
 		testFiltering(parent, "123",
 				alice, arnold);
 		testFiltering(child1, "123",
 				alice, arnold);
 		testFiltering(child2, "123");
 		
 		// Test filtering by name
 		testFiltering(getRootGroup(), "a",
 				alice, arnold, brian, caroline, charles, xuxa, xavier);
 		testFiltering(parent, "a",
 				alice, arnold, brian, caroline, charles);
 		testFiltering(child1, "a",
 				alice, arnold);
 		testFiltering(child2, "a",
 				brian);
 
 		testFiltering(getRootGroup(), "li",
 				alice, caroline);
 		testFiltering(parent, "li",
 				alice, caroline);
 		testFiltering(child1, "li",
 				alice);
 		testFiltering(child2, "li");
 
 		testFiltering(getRootGroup(), "xuxa", 
 				xuxa);
 		testFiltering(parent, "xuxa");
 		testFiltering(child1, "xuxa");
 		testFiltering(child2, "xuxa");
 	}
 	
 	private void testFiltering(Group group, String filterString, Contact... expectedContacts) {
 		List<Contact> actualResults = this.dao.getFilteredMembers(group, filterString);
 		assertEquals("Incorrect number of contacts retrieved for filter '" + filterString + "' on group '" + group.getPath() + "'", 
 				expectedContacts.length, actualResults.size());
 		
 		Set<Contact> expectedResults = new HashSet<Contact>();
 		for(Contact expected : expectedContacts) expectedResults.add(expected);
 		for(Contact actual : actualResults) {
 			assertTrue("Unexpected contact in results: " + actual.getName(), expectedResults.remove(actual));
 		}
 		if(expectedResults.size() > 0) {
 			String missingContacts = "";
 			for(Contact c : expectedResults) missingContacts += ", " + c.getName();
 			fail("Expected results where not found in retrieved results: " + missingContacts.substring(2));
 		}
 	}
 	
 	public void testSubgroups() throws DuplicateKeyException {
 		// Create the groups
 		Group parent = createGroup("parent");
 		Group child1 = createGroup(parent, "child1");
 		Group child2 = createGroup(parent, "child2");
 
 		// Check they are all empty
 		testRelationship(parent);
 		testRelationship(child1);
 		testRelationship(child2);
 
 		// Create the contacts
 		Contact alice = createContact("Alice", child1);
 		Contact arnold = createContact("Arnold", child1);
 		Contact brigitte = createContact("Brigitte", child2);
 		Contact brian = createContact("Brian", child2);
 		Contact caroline = createContact("Caroline", parent);
 		Contact charles = createContact("Charles", parent);
 		Contact xuxa = createContact("Xuxa");
 		Contact xavier = createContact("Xavier");
 		
 		// Check the groups now have the expected members
 		testRelationship(child1, alice, arnold);
 		testRelationship(child2, brigitte, brian);
 		testRelationship(parent, alice, arnold, brigitte, brian, caroline, charles);
 		testRelationship(getRootGroup(), alice, arnold, brigitte, brian, caroline, charles, xuxa, xavier);
 	}
 	
 //> INSTANCE HELPER METHODS
 	/** Creates a contact with specified name. 
 	 * @throws DuplicateKeyException */
 	private Contact createContact(String name, Group... groups) throws DuplicateKeyException {
 		// Make a phone number up that should be unique
 		String phoneNumber = Integer.toString(name.hashCode());
 		return createContact(name, phoneNumber, groups);
 	}
 	
 	/** Creates a contact with specified name and phone number. 
 	 * @throws DuplicateKeyException */
 	private Contact createContact(String name, String phoneNumber, Group... groups) throws DuplicateKeyException {
 		// Make a phone number up that should be unique
 		Contact contact = new Contact(name, phoneNumber, null, null, null, true);
 		this.contactDao.saveContact(contact);
 		
 		for(Group group : groups) {
 			this.dao.addMember(group, contact);
 		}
 		
 		return contact;
 	}
 	
 	/** Create a top-level group. */
 	private Group createGroup(String name, Contact... contacts) throws DuplicateKeyException {
 		return createGroup(getRootGroup(), name, contacts);
 	}
 	
 	/** Create a group with specified parent. */
 	private Group createGroup(Group parent, String name, Contact... contacts) throws DuplicateKeyException {
 		Group group = new Group(parent, name);
 		this.groupDao.saveGroup(group);
 		
 		for(Contact contact : contacts) {
 			this.dao.addMember(group, contact);
 		}
 		
 		return group;
 	}
 	
 	/**
 	 * Test that all relationships between a group and its expected members are sound.
 	 * @param group
 	 * @param members
 	 */
 	private void testRelationship(Group group, Contact... members) {
 		assertEquals("Incorrect members count for group: " + group.getPath(), members.length, this.dao.getMemberCount(group));
 		assertEquals("groupMembershipDao.getMembers(Group) for group " + group.getPath(), members, this.dao.getMembers(group));
 		assertEquals("groupMembershipDao.getMembers(Group, int, int) for group " + group.getPath(), members, this.dao.getMembers(group, 0, members.length));
 		
 		for(Contact contact : members) {
 //			assertTrue(dao.getGroups(contact).contains(group));
 			assertTrue(dao.isMember(group, contact));
 		}
 		
 		// Check that active membership fetching is behaving itself
 		List<Contact> expectedActiveMembers = new ArrayList<Contact>(members.length);
 		for(Contact member : members) {
 			if(member.isActive()) expectedActiveMembers.add(member);
 		}
 		assertEquals(expectedActiveMembers, dao.getActiveMembers(group));
 	}
 	
 	private Group getRootGroup() {
 		return new Group(null, null);
 	}
 	
 //> ACCESSORS
 	/**  */
 	public void setGroupMembershipDao(GroupMembershipDao dao) {
 		this.dao = dao;
 	}
 	
 	public void setGroupDao(GroupDao groupDao) {
 		this.groupDao = groupDao;
 	}
 	
 	public void setContactDao(ContactDao contactDao) {
 		this.contactDao = contactDao;
 	}
 	
 //> STATIC HELPER METHODS
 	static final <T> void assertEquals(String name, T[] expected, List<T> actual) {
 		assertEquals("Incorrect object count in " + name, expected.length, actual.size());
 		for (int i = 0; i < expected.length; i++) {
 			assertEquals("Incorrect value in " + name + " at index " + i, expected[i], actual.get(i));
 		}
 	}
 }
