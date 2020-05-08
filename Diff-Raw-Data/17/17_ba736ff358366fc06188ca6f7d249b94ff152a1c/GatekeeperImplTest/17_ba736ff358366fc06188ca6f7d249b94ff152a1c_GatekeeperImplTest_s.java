 package deus.core.gatekeeper.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import javax.annotation.Resource;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import deus.core.User;
 import deus.core.UserRegistry;
 import deus.core.gatekeeper.Gatekeeper;
 import deus.core.gatekeeper.soul.LoginCredentials;
 import deus.model.user.id.UserUrl;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/deus/context.xml", "/deus/model/attention/attentionList.xml", "/deus/storage/daos.xml",
 		"/deus/core/core.xml", "/deus/core/core-test.xml" })
 public class GatekeeperImplTest {
 
 	@Resource(name = "gatekeeperTarget")
 	private Gatekeeper gatekeeper;
 
 	@Autowired
 	private UserRegistry userRegistry;
 
 	private User user;
 	
 
 	@Test
 	public void testLoginLogout() {
 		LoginCredentials credentials = new LoginCredentials("alice", "password");
 
 		// LOGIN
 		user = gatekeeper.login(credentials);
		assertEquals(new UserUrl("username", "deus.org"), user.getUserId());
 		assertTrue(userRegistry.hasUser(user.getUserId()));
 		assertTrue(user.isLoggedIn());
 		
 		// LOGOUT
 		gatekeeper.logout(user);
 		assertFalse(userRegistry.hasUser(user.getUserId()));
 		assertFalse(user.isLoggedIn());
 	}
 
 }
