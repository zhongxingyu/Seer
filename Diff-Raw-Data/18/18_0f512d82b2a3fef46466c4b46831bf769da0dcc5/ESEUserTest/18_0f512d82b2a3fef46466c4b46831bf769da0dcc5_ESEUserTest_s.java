 package modelTests;
 
 import java.util.List;
 
 import models.ESECalendar;
 import models.ESEFactory;
 import models.ESEGroup;
 import models.ESEUser;
 import net.sf.oval.constraint.AssertNullCheck;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.db.jpa.GenericModel.JPAQuery;
 import play.test.Fixtures;
 import play.test.UnitTest;
 
 /**
  * @author judith
  * 
  * last update: 24. okt 2011
  * 
  */
 public class ESEUserTest extends UnitTest {
 
 	@Before
 	public void setup() {
 		//set up database		
 	    Fixtures.deleteDatabase();
 	    Fixtures.loadModels("data.yml");
 	}
 	
 	
 	@Test
 	public void createUserWithFacotry2Para(){
 
 		ESEUser hansi = ESEFactory.createUser("hansi", "geheim");
 		assertNotNull(hansi);
 		assertNotNull(hansi.calendarList);
 		assertEquals(0,hansi.calendarList.size());
 		assertNotNull(hansi.groupList);
 		assertEquals(1,hansi.groupList.size());
 		assertEquals("hansi", hansi.username);
 		assertEquals("geheim", hansi.password);
 	}
 	
 	@Test
 	public void createUserWithFacotry4Para(){
 
 		ESEUser hansi = ESEFactory.createUser("hansi", "sehrgeheim", "Hans", "Müller");
 		assertNotNull(hansi);
 		assertNotNull(hansi.calendarList);
 		assertEquals(0,hansi.calendarList.size());
 		assertNotNull(hansi.groupList);
 		assertEquals(1,hansi.groupList.size());
 		assertEquals("hansi", hansi.username);
 		assertEquals("sehrgeheim", hansi.password);
 		assertEquals("Hans", hansi.firstName);
 		assertEquals("Müller", hansi.familyName);
 	}
 		
 	@Test
 	public void shouldCreatCalendarNewUser(){
 		/*TODO use method to get Calendar by String name instead of
 		 using index as soon as it is available
 		*/
 		
 		//Test with new user
 		ESEUser hansi = ESEFactory.createUser("hansi", "sehrgeheim", "Hans", "Müller");
 		
 		//add by using method
 		hansi.createCalendar("Hausaufgaben");
 		assertNotNull(hansi.calendarList.get(0));		
 		assertEquals(1, hansi.calendarList.size());
 		assertEquals("Hausaufgaben", hansi.calendarList.get(0).name);
 			
 		//add by using factory
 		hansi.calendarList.add(ESEFactory.createCalendar("Wichtige Sachen", hansi));
 		assertEquals(2, hansi.calendarList.size());
 		assertEquals("Wichtige Sachen", hansi.calendarList.get(1).name);
 		assertEquals("Hausaufgaben", hansi.calendarList.get(0).name);
 
 	}
 	
 	@Test
 	public void shouldCreatCalendarDBUser(){		
 		/*TODO use method to get Calendar by String name instead of
 		 using index as soon as it is available
 		*/
 		
 		//Test with new user
 		ESEUser bill = ESEUser.find("byUsername", "bill").first();
 		assertEquals(1, bill.calendarList.size());
 		assertNotNull(bill.calendarList.get(0));		
 		
 		//add by using method
 		bill.createCalendar("Hausaufgaben");
 		assertNotNull(bill.calendarList.get(1));
 		assertEquals(2, bill.calendarList.size());
 		assertEquals("Hausaufgaben", bill.calendarList.get(1).name);
 			
 		//add by using factory
 		bill.calendarList.add(ESEFactory.createCalendar("Wichtige Sachen", bill));
 		assertEquals(3, bill.calendarList.size());
 		assertNotNull(bill.calendarList.get(2));
 		assertEquals("Wichtige Sachen", bill.calendarList.get(2).name);
 	}
 	
 	@Test
 	public void shouldRemoveCalendarNewUser(){
 		//set up
 		ESEUser hansi = ESEFactory.createUser("hansi", "sehrgeheim", "Hans", "Müller");
 		hansi.createCalendar("Hausaufgaben");
 		hansi.createCalendar("Shopping Liste");
 		assertEquals(2,hansi.calendarList.size());
 		
		ESECalendar husi = ESECalendar.find("byName", "Hausaufgaben").first();		
 		ESECalendar shop = ESECalendar.find("byName", "Shopping Liste").first();
 		assertNotNull(husi);
 		assertNotNull(shop);
 		
 		//remove one
 		hansi.removeCalendar(husi.id);
 		assertEquals(1, hansi.calendarList.size());
 		assertEquals(shop, hansi.calendarList.get(0));
 		
 		//remove last
 		hansi.removeCalendar(shop.id);
 		assertEquals(0, hansi.calendarList.size());
 
 	}
 	
 	@Test
 	public void shouldRemoveCalendarDBUser(){
 		//set up
 		ESEUser bob = ESEUser.find("byUsername", "bob").first();
 		assertEquals(3,bob.calendarList.size());
 		
 		ESECalendar cal1 = ESECalendar.find("byName", "BobsCalendarOne").first();		
 		ESECalendar cal2 = ESECalendar.find("byName", "BobsCalendarTwo").first();
 		ESECalendar cal3 = ESECalendar.find("byName", "BobsCalendarThree").first();
 		
 		assertNotNull(cal1);
 		assertNotNull(cal2);
 		assertNotNull(cal3);
 		
 		//remove one
 		bob.removeCalendar(cal1.id);
 		assertEquals(2, bob.calendarList.size());
 
 		//remove second
 		bob.removeCalendar(cal2.id);
 		assertEquals(1, bob.calendarList.size());
 		
 		//remove last
 		bob.removeCalendar(cal3.id);
 		assertEquals(0, bob.calendarList.size());
 
 	}
 	
 	@Test
 	public void shouldCreateGroupNewUser(){
 		/*TODO use method to get Calendar by String name instead of
 		 using index as soon as it is available
 		*/
 		
 		//TODO DB need Group entries!
 		
 		ESEUser hansi = ESEFactory.createUser("hansi", "sehrgeheim", "Hans", "Müller");
 		
 		assertEquals(1,hansi.groupList.size());
 		assertEquals("Friends", hansi.groupList.get(0).groupName);
 		
 		//add by using method
 		hansi.createGroup("studies");
 		assertEquals(2, hansi.groupList.size());
 		assertEquals("studies", hansi.groupList.get(1).groupName);
 		
 		//add by using Factory
 		hansi.groupList.add(ESEFactory.createGroup("best buddies", hansi));	
 		assertEquals(3,hansi.groupList.size());
 		assertEquals("best buddies", hansi.groupList.get(2).groupName);
 
 	}
 
 	@Test
 	public void shouldCreateGroupDBUser(){
 		/*TODO use method to get Calendar by String name instead of
 		 using index as soon as it is available
 		*/
 		
 		//TODO DB need Group entries!
 		
 		ESEUser jack = ESEUser.find("byUsername", "jack").first();
 		assertNotNull(jack);
 		
 		//TODO frien ground is not autogenerated
 		assertEquals(1,jack.groupList.size());
 		assertEquals("JackFriends", jack.groupList.get(0).groupName);
 		
 		//add by using method
 		jack.createGroup("studies");
 		assertEquals(2, jack.groupList.size());
 		assertEquals("studies", jack.groupList.get(1).groupName);
 		
 		//add by using Factory
 		jack.groupList.add(ESEFactory.createGroup("best buddies", jack));	
 		assertEquals(3,jack.groupList.size());
 		assertEquals("best buddies", jack.groupList.get(2).groupName);
 
 	}
 	@Test
 	public void shouldRemoveGroupeNewUser(){
 		//set up
 		ESEUser hansi = ESEFactory.createUser("hansi", "sehrgeheim", "Hans", "Müller");
 		hansi.groupList.add(ESEFactory.createGroup("studies", hansi));	
 		hansi.groupList.add(ESEFactory.createGroup("best buddies", hansi));	
 		
 		assertEquals(3, hansi.groupList.size());
 		ESEGroup studies = ESEGroup.find("byGroupName", "studies").first();
 		assertNotNull(studies);
 		
 		//remove one
 		//TODO method does not exist yet
 	}
 	
 	@Test
 	public void shouldTestEditsNewUser(){
 		ESEUser hansi = ESEFactory.createUser("hansi", "sehrgeheim", "Hans", "Müller");
 		
 		hansi.editPassword("nochvielgeheimer");
 		assertEquals("nochvielgeheimer", hansi.password);
 		
 		hansi.editFamilyName("Müller");
 		assertEquals("Müller", hansi.familyName);
 		
 		hansi.editFirstName("Hans");
 		assertEquals("Hans", hansi.firstName);
 		
 	}
 
 	@Test
 	public void shouldTestEditsDBUser(){
 		//set up
 		ESEUser jack = ESEUser.find("byUsername", "jack").first();
 		assertEquals("pw2", jack.password);
 		assertEquals("Jack", jack.firstName);
 		assertEquals("Sparrow", jack.familyName);
 		
 		jack.editPassword("nochvielgeheimer");
 		assertEquals("nochvielgeheimer", jack.password);
 		
 		jack.editFamilyName("Müller");
 		assertEquals("Müller", jack.familyName);
 		
 		jack.editFirstName("Hans");
 		assertEquals("Hans", jack.firstName);
 		
 	}
 }
