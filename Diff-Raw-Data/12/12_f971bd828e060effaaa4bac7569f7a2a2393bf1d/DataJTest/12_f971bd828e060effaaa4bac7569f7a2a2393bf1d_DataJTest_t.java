 
 
 import Data.*;
 import static org.junit.Assert.*;
 
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 
import Remote.UserObject;
 public class DataJTest {
 	@Before
 	public void setUp() throws Exception {
 
 	}
 	@After
 	public void tearDown() throws Exception{
 		//just returning it to normal
 		UserObject user2 = new UserObject("test@test.ca", "12de1", "test", 500);
 		user2.setId(1);
 		assertTrue(UserData.updateUser(user2)!=-1);
 
 
 	}
 	@Ignore
 	@Test
 	public  void testCreate() throws SQLException {
 		//user is in DB already
 		//the number at the end actually will not have an impact, since its chip amount
 		UserObject user0 = new UserObject("test@test.ca", "pass", "madfyname2", 12);
 		UserObject user1 = new UserObject("test@t.ca", "pass", "test", 12);
 		assertTrue(UserData.createUser(user0)==-1);
 		assertTrue(UserData.createUser(user1)==-1);
 
 		//also user1 should be removed at the tear down
 		UserObject user2 = new UserObject("toBeRemoved@mail.ca", "pass", "displayname", -1);
 		assertTrue(UserData.createUser(user2)==0);
 
 	}
 	@Test
 	public  void testAuthenticate() throws SQLException {
 		//no password
 		int b = UserData.authenticate("test@test.ca", "");
 		assertTrue(b==-1);
 		//wrong password
 		int c = UserData.authenticate("test@test.ca", "234234");
 		assertTrue(c==-1);
 		//incomplete username
 		int d = UserData.authenticate("test@te", "1234");
 		assertTrue(d==-1);
 		//successful entry!
 		int e = UserData.authenticate("test@test.ca", "1234");
 		assertTrue(e==0);
 
 	}
 	@Test
 	public  void testGetID() throws SQLException {
 		//getID works for strings of email and strings of u_id
 		int id = UserData.getId("lulz");
 		assertTrue(id==-1);
 		id = UserData.getId("");
 		assertTrue(id==-1);
 
 		int id2 = UserData.getId("test@test.ca");
 		assertTrue(id2==1);
 		id2 = UserData.getId("test");
 		assertTrue(id2==1);
 	}
 	@Test
 	public  void testGetUserObj() throws SQLException {
 		UserObject user = UserData.getUserObject(99);
 		//should return null if user doesnt exist
 		assertTrue(user==null);
 		//this user exists 
 		UserObject user1=  UserData.getUserObject(1);
 		//System.out.println(user1.getName());
 		assertTrue(user1.getName().equals("test"));
 
 	}
 	@Test
 	public  void testUpdate() throws SQLException {
 		UserObject user = new UserObject("test12@test.ca", "12de", "name", 3);
 		user.setId(99);
 		//here is shouldnt work since the user id doesnt exist
 		assertTrue(UserData.updateUser(user)==-1);
 
 		UserObject user1 = new UserObject("test@test.ca", "12de", "name", 3);
 		user1.setId(1);
 		//should be true
 		assertTrue(UserData.updateUser(user1)!=-1);
 		//just returning it to normal
 		UserObject user2 = new UserObject("test@test.ca", "12de1", "test", 500);
 		user2.setId(1);
 		assertTrue(UserData.updateUser(user2)!=-1);
 
 
 	}
 	@Test
 	public  void testExist() throws SQLException {
 		assertFalse(UserData.exists(99));
 		assertFalse(UserData.exists(""));
 
 		assertTrue(UserData.exists(1));
 		assertTrue(UserData.exists("test@test.ca"));
 	}
 	@Test
 	public  void testAddF() throws SQLException {
 		assertTrue(UserData.addFriend(1, 99)==-1);
 		assertTrue(UserData.addFriend(1, 1)==-1);
 		assertTrue(UserData.addFriend(99, 1)==-1);
 		//should be success
 		assertTrue(UserData.addFriend(1, 10)==0);
 	}
 	@Test
 	public  void testIsF() throws SQLException{
 		assertFalse(UserData.isFriend(2,99));
 		assertFalse(UserData.isFriend(99,1));
 		//success 
 		assertTrue(UserData.isFriend(1,2));
 
 	}
 	@Test
 	public  void testGetF() throws SQLException{
 		ArrayList<UserObject> list =  UserData.getFriends(99);
 		assertTrue(list.size()==0);
 		ArrayList<UserObject> list1 = UserData.getFriends(1);
 		assertTrue(list1.size()!=0);
 	}
 	@Test
 	public  void testDeleteF() throws SQLException{
 		//this should be special since it will only return -1 if the sql server fails
 		assertTrue(UserData.deleteFriend(99,1)==0);//user DNE
 		assertTrue(UserData.deleteFriend(1,99)==0);//Friend DNE
 		assertTrue(UserData.deleteFriend(99,98)==0);//both
 
 		assertTrue(UserData.deleteFriend(1, 10)==0);
 	}
 
 	@Test
 	public void testCreateLeaderBoard() throws SQLException{
 		//just testing if a string[][] is produced and also if it is sorted
 		//only way to test it, unless u rig the db
 		
 		String testString[][] = Statistics.createLeaderBoard();
 		double higher =Double.parseDouble(testString[0][3]);
 		for(int i =0;i<25;i++){
 			if(testString[i]!=null){
 				break;
 
 			}
 			assertTrue(higher>=Double.parseDouble(testString[i][3]));
 
 		}
 	}
 	@Test
 	public void testUpdatePlayer() throws SQLException{
 		//u_id dne
 		assertTrue(Statistics.updateUserStatistics(0, 0, true)==-1);
 
 		
 		//success cases
 		assertTrue(Statistics.updateUserStatistics(1, 0, true)!=-1);
 		assertTrue(Statistics.updateUserStatistics(1, 0, false)!=-1);
 		
 		
 	}
 	
 	@Test
 	public void testGetPlayerStats() throws SQLException{
 		double stats []=Statistics.getPlayerStats(1);
 		//had to do it since its impossible to reduce the numbers using the pre-existing functions
 		assertTrue(stats[0]==0);
 		assertTrue(stats[1]>12);
 		assertTrue(stats[2]>3);
 		
 	}
 
 }
