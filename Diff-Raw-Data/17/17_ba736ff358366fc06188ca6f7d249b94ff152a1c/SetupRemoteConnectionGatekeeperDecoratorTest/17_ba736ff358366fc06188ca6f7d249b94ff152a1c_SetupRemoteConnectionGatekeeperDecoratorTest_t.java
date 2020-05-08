 package deus.core.gatekeeper.impl;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
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
 import deus.model.user.transportid.TransportIdType;
 import deus.remoting.state.RemotingState;
 import deus.remoting.state.RemotingStateRegistry;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/deus/context.xml", "/deus/model/attention/attentionList.xml",
 		"/deus/storage/daos.xml", "/deus/core/core.xml", "/deus/core/core-test.xml" })
 public class SetupRemoteConnectionGatekeeperDecoratorTest {
 
 	@Autowired
 	private UserRegistry userRegistry;
 
 	
 	@Resource(name = "gatekeeper")
 	private Gatekeeper gatekeeper;
 
 	private User user;
 
 
 	@Before
 	public void setUp() throws Exception {
 
 	}
 
 
 	@Test
 	public void testLogin() {
 		LoginCredentials credentials = new LoginCredentials("alice", "password");
 		user = gatekeeper.login(credentials);
 		
 		RemotingStateRegistry registry = user.getRemotingStateRegistry();
 		assertTrue(user.getUserId().hasTransportId(TransportIdType.local));
 		for(TransportIdType type : user.getUserId().getSupportedTransports()) {
 			System.out.println("testing added remoting state for transport protocol '" + type + "'");
 			RemotingState remotingState = registry.getRemotingState(type);
 			assertNotNull(remotingState);
 			assertEquals(type, remotingState.getType());
 		}
 		
 		gatekeeper.logout(user);
 		
 		for(TransportIdType type : user.getUserId().getSupportedTransports()) {
 			System.out.println("testing removed remoting state for transport protocol '" + type + "'");
 			RemotingState remotingState = registry.getRemotingState(type);
 			assertNull(remotingState);
 		}
 	}
 
 	@Test
 	public void testLoginLogout() {
 		LoginCredentials credentials = new LoginCredentials("alice", "password");
 
 		// LOGIN
 		user = gatekeeper.login(credentials);
		assertEquals(new UserUrl("alice", "deus.org"), user.getUserId());
 		assertTrue(userRegistry.hasUser(user.getUserId()));
 		assertTrue(user.isLoggedIn());
 		
 		// LOGOUT
 		gatekeeper.logout(user);
 		assertFalse(userRegistry.hasUser(user.getUserId()));
 		assertFalse(user.isLoggedIn());
 	}
 
 }
