 package models;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.modules.siena.SienaFixtures;
 import play.test.UnitTest;
 
 public class UserTest extends UnitTest {
 
 	@Before
 	public void setUp() {
 		SienaFixtures.deleteDatabase();
 	    SienaFixtures.loadModels("users.yml");
 	}
 
 	@Test
 	public void getMember1() {
 		Member member = Member.all().filter("name", "Dayan").get();
 		assertNotNull(member);
 		assertEquals(1, member.users.count());
 		assertEquals("[ben@gmail.com (Dayan)]", member.users.fetch().toString());
 	}
 
 	@Test
 	public void getMember2() {
 		Member member = Member.all().filter("name", "Rosen").get();
 		assertNotNull(member);
 		assertEquals(2, member.users.count());
 		assertEquals("[bob@gmail.com (Rosen), dude@gmail.com (Rosen)]", member.users.fetch().toString());
 	}
 
 	@Test
 	public void count() {
 		assertEquals(2, Member.all().count());
 		assertEquals(3, User.all().count());
 	}
 
 	@Test
 	public void fetchUsers() {
 		assertEquals("[bob@gmail.com (Rosen), dude@gmail.com (Rosen), ben@gmail.com (Dayan)]", User.all().fetch().toString());  
 	}
 }
