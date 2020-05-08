 package modelTests;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import models.ESEEvent;
 import models.ESEFactory;
 import models.ESEGroup;
 import models.ESEUser;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.data.validation.Required;
 import play.test.Fixtures;
 import play.test.UnitTest;
 
 public class ESEGroupTest extends UnitTest{
 	
 	private ESEGroup testGroup;
 	
 	@Before
 	public void setUp() {
		//added argument "null" to constructor
 		this.testGroup = new ESEGroup("TestGroupName1", null);
 	}
 	
 	@Test
 	public void shouldReturnCorrectGroupName(){
 		assertEquals(testGroup.groupName,"TestGroupName1");
 	}
 	
 	@Test
 	public void shouldEditGroupNameCorrectly(){
 		testGroup.editGroupName("TestGroupName1NewName");
 		assertEquals(testGroup.groupName,"TestGroupName1NewName");
 		
 		//TODO: What happens if empty string? Will this be handled in the model or in the view?
 	}
 	
 	@Test
 	public void shouldAddUsers(){
 		
 		Fixtures.loadModels("data.yml");
 		assertEquals(testGroup.userList.size(),0);
 		this.testGroup.addUser("bob");
 		assertEquals(testGroup.userList.size(),1);
 		this.testGroup.addUser("bill");
 		assertEquals(testGroup.userList.size(),2);
 	}
 	
 	@Test
 	public void shouldOnlyAddExistingUsers(){
 		
 		//TODO: This test fails, because a null object is added, if a username
 		// that does not exist is sent to #addUser. - How is this problem handled?
 		// Is the view responsible that no non-existing users are sent to
 		// addUser, or is addUser responsible that non-existing users are ignored?
 		
 		Fixtures.deleteDatabase();
 		Fixtures.loadModels("data.yml");
 		assertEquals(testGroup.userList.size(),0);
 		this.testGroup.addUser("nouser");
 		assertEquals(testGroup.userList.size(),0);
 		this.testGroup.addUser("bill");
 		assertEquals(testGroup.userList.size(),1);
 	}
 	
 	@Test
 	public void shouldNotAddUserIfAlreadyInList(){
 		
 		assertEquals(testGroup.userList.size(),0);
 		this.testGroup.addUser("bob");
 		assertEquals(testGroup.userList.size(),1);
 		this.testGroup.addUser("bob");
 		assertEquals(testGroup.userList.size(),1);
 	}
 	
 	@Test
 	public void shouldReturnListOfAllUsers(){
 		
 		Fixtures.deleteDatabase();
 		Fixtures.loadModels("data.yml");
 		this.testGroup.addUser("bob");
 		this.testGroup.addUser("bill");
 		
 		assertTrue(testGroup.getAllUser().size()==2);
 		assertTrue(testGroup.getAllUser().get(0).username.equals("bob"));
 		assertTrue(testGroup.getAllUser().get(1).username.equals("bill"));
 	}
 	
 	@Test
 	public void shouldRemoveCorrectUser(){
 		
 		Fixtures.deleteDatabase();
 		Fixtures.loadModels("data.yml");
 		this.testGroup.addUser("bob");
 		this.testGroup.addUser("bill");
 		
 		assertTrue(testGroup.getAllUser().size()==2);
 		
 		this.testGroup.removeUser("bob");
 		
 		assertTrue(testGroup.getAllUser().size()==1);
 		
 		assertTrue(testGroup.getAllUser().get(0).username.equals("bill"));
 		assertTrue(testGroup.isUserInGroup("bill"));
 		assertFalse(testGroup.isUserInGroup("bob"));
 		assertFalse(testGroup.isUserInGroup("nouser"));
 		
 	}
 	
 	@Test
 	public void shouldReturnWheterUserIsInList(){
 		Fixtures.deleteDatabase();
 		Fixtures.loadModels("data.yml");
 		this.testGroup.addUser("bob");
 		
 		assertTrue(testGroup.isUserInGroup("bob"));
 		assertFalse(testGroup.isUserInGroup("bill"));
 		
 	}
 	
 }
