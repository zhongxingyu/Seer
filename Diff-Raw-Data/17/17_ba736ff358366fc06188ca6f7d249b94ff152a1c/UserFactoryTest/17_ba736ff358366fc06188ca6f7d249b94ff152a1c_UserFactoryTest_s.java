 package deus.core;
 
 
 import static org.junit.Assert.assertEquals;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import deus.model.attention.AttentionList;
 import deus.model.user.UserMetadata;
 import deus.model.user.id.UserId;
 import deus.model.user.id.UserUrl;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/deus/model/attention/attentionList.xml", "/deus/storage/daos.xml",
 		"/deus/core/core.xml", "/deus/core/core-test.xml", "/deus/context.xml" })
 public class UserFactoryTest {
 
 	@Autowired
 	private UserFactory userFactory;
 
 	@Resource(name="unnoticedAttentionList")
 	private AttentionList attentionList;
 	
 	
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 
 	@Test
 	public void testCreateUser() {
 		UserId userId = new UserUrl("username", "server");
 		User user = userFactory.createUser(userId);
		assertEquals(attentionList, user.barker.getUnnoticedAttentionList());
 		
 		UserMetadata userMetadata = user.getUserMetadata();
 		assertEquals("full name", userMetadata.getFullName());
 		assertEquals(userId, userMetadata.getUserId());
 		
 		// TODO: extend use case
 	}
 
 }
