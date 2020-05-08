 package ch.hszt.mdp.dao;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import ch.hszt.mdp.domain.Friendship;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:mdp-test-daos.xml" })
 public class FriendshipDaoTest {
 
 	private FriendshipDao friendshipDao;
 
 	private Friendship friendship;
 
 	@Autowired
 	public void setFriendshipDao(FriendshipDao friendshipDao) {
 		this.friendshipDao = friendshipDao;
 	}
 
 	@Before
 	public void setup() {
 		friendship = new Friendship();
		friendship.setPrimary_user(1);
		friendship.setSecondary_user(2);
		friendship.setAccepted(1);
 	}
 
 	@Test
 	public void testFriendship() {
 
 	}
 }
