 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Before;
 
 import edu.berkeley.cs.cs162.ChatServer;
 import junit.framework.TestCase;
 import edu.berkeley.cs.cs162.*;
 import org.junit.*;
 import static org.junit.Assert.* ;
 
 
 
 public class ChatGroupTest {
 
 	ChatServer server;
 	
 	@Before
 	public void beforeEachTest() {
 		server = new ChatServer();
 		server.login("A");
 		server.login("B");
 		server.login("C");
 	}
 
 	@After
 	public void afterEachTest() {
 		server = null; 
 	}
 	
 	@Test
 	public void TestCreateGroup() {
 		server.joinGroup(server.getUser("A"), "newGroup");
 		Set<String> groups = server.getGroups();
 		ChatGroup newGroup = server.getGroup("newGroup");
 		HashMap<String, User> userlist = newGroup.getUserList();
 		assertTrue(groups.contains("newGroup") && userlist.size() == 1);
 	}
 	
 	@Test
 	public void TestJoinGroup() {
 		server.joinGroup(server.getUser("A"), "newGroup");
 		server.joinGroup(server.getUser("B"), "newGroup");
 		Set<String> groups = server.getGroups();
 		ChatGroup newGroup = server.getGroup("newGroup");
 		HashMap<String, User> userlist = newGroup.getUserList();
 		assertTrue(userlist.keySet().contains("A") && userlist.keySet().contains("B")
 				&& userlist.keySet().size() == 2);
 	}
 	
 	@Test
 	public void TestFullGroup() {
 		for (int i=0; i<10; i++) {
 			server.login(Integer.toString(i));
 			server.joinGroup(server.getUser(Integer.toString(i)), "newGroup");
 		}
 		Set<String> groups = server.getGroups();
 		ChatGroup newGroup = server.getGroup("newGroup");
 		HashMap<String, User> userlist = newGroup.getUserList();
 		assertTrue(server.joinGroup(server.getUser("A"), "newGroup") == false &&
 				userlist.size() == 10);	
 	}
 	
 	@Test
 	public void TestLeaveGroupNotLastUser() {
 		server.joinGroup(server.getUser("A"), "newGroup");
 		server.joinGroup(server.getUser("B"), "newGroup");
 		server.leaveGroup(server.getUser("A"), "newGroup");
 		Set<String> groups = server.getGroups();
 		ChatGroup newGroup = server.getGroup("newGroup");
 		HashMap<String, User> userlist = newGroup.getUserList();
 		assertTrue(userlist.size()==1);
 	}
 	
 	@Test
 	public void TestLeaveGroupLastUser() {
 		server.joinGroup(server.getUser("A"), "newGroup");
 		server.leaveGroup(server.getUser("A"), "newGroup");
 		Set<String> groups = server.getGroups();
 		assertFalse(groups.contains("newGroup"));
 	}
 	
 	@Test
 	public void TestLeaveNonexistantGroup() {
 		assertFalse(server.leaveGroup(server.getUser("A"), "doesn't Exist"));
 	}
 	
 	@Test
 	public void TestSendMessageToGroup() throws InterruptedException {
 		server.joinGroup(server.getUser("A"), "newGroup");
 		server.joinGroup(server.getUser("B"), "newGroup");
 		server.joinGroup(server.getUser("C"), "newGroup");
 		User a = (User) server.getUser("A");
 		a.send("newGroup", "Hello B and C");
 		Thread.sleep(4000);
 		User b = (User) server.getUser("B");
 		User c = (User) server.getUser("C");
		Message messageB = b.getLog("newGroup").getLog().get(0);
		Message messageC = c.getLog("newGroup").getLog().get(0);
 		assertTrue(messageB.getContent() == "Hello B and C" 
 			&& messageC.getContent() == "Hello B and C");
			
 	}
 }
