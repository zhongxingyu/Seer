 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.authorization.manager;
 
 import static br.octahedron.figgo.modules.authorization.data.Role.createRoleKey;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.octahedron.figgo.modules.DataAlreadyExistsException;
 import br.octahedron.figgo.modules.DataDoesNotExistsException;
 import br.octahedron.figgo.modules.authorization.data.Role;
 import br.octahedron.figgo.modules.authorization.data.RoleDAO;
 
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
 
 /**
  * @author Danilo Queiroz
  */
 public class AuthorizationManagerTest {
 
 	private RoleDAO roleDAO;
 	private AuthorizationManager authManager;
 	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvIsLoggedIn(true);
 
 	@Before
 	public void setUp() {
 		this.helper.setUp();
 		this.roleDAO = createMock(RoleDAO.class);
 		this.authManager = new AuthorizationManager();
 		this.authManager.setRoleDAO(this.roleDAO);
 	}
 
 	@Test(expected = DataAlreadyExistsException.class)
 	public void createRoleTest() {
 		// setup mock
 		String key = createRoleKey("domain", "role");
 		expect(this.roleDAO.exists(key)).andReturn(false).times(2);
 		this.roleDAO.save(new Role("domain", "role"));
 		expect(this.roleDAO.exists(key)).andReturn(true).times(2);
 		replay(this.roleDAO);
 		try {
 			// test
 			assertFalse(this.authManager.existsRole("domain", "role"));
 			this.authManager.createRole("domain", "role");
 			assertTrue(this.authManager.existsRole("domain", "role"));
 			this.authManager.createRole("domain", "role");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void deleteRoleTest() {
 		// setup mock
 		String key = createRoleKey("domain", "role");
 		expect(this.roleDAO.exists(key)).andReturn(true).times(1);
 		this.roleDAO.delete(key);
 		expect(this.roleDAO.exists(key)).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			this.authManager.removeRole("domain", "role");
 			this.authManager.removeRole("domain", "role");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void getTest() {
 		// setup mock
 		String key = createRoleKey("domain", "role");
 		expect(this.roleDAO.exists(key)).andReturn(true).times(1);
 		expect(this.roleDAO.get(key)).andReturn(new Role("domain", "role")).times(1);
 		expect(this.roleDAO.exists(key)).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			Role role = this.authManager.getRole("domain", "role");
 			assertNotNull(role);
 			assertEquals(new Role("domain", "role"), role);
 			this.authManager.getRole("domain", "role");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void addUserTest() {
 		// setup mock
 		Role role = new Role("domain", "role");
 		String key = createRoleKey("domain", "role");
 		expect(this.roleDAO.exists(key)).andReturn(true).times(1);
 		expect(this.roleDAO.get(key)).andReturn(role).times(1);
 		expect(this.roleDAO.exists(key)).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			assertEquals(0, role.getUsers().size());
 			this.authManager.addUsersToRole("domain", "role", "user1", "user2");
 			assertEquals(2, role.getUsers().size());
 			this.authManager.addUsersToRole("domain", "role", "user1");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void addActivitiesTest() {
 		// setup mock
 		Role role = new Role("domain", "role");
 		String key = createRoleKey("domain", "role");
 		expect(this.roleDAO.exists(key)).andReturn(true).times(1);
 		expect(this.roleDAO.get(key)).andReturn(role).times(1);
 		expect(this.roleDAO.exists(key)).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			assertEquals(0, role.getActivities().size());
 			this.authManager.addActivitiesToRole("domain", "role", "activity1", "activity2");
 			assertEquals(2, role.getActivities().size());
 			this.authManager.addActivitiesToRole("domain", "role", "activity1");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void removeUserFromRole() {
 		// setup mock
 		Role role = new Role("domain", "role");
 		role.addUsers("user1", "user2");
 		String key = createRoleKey("domain", "role");
 		expect(this.roleDAO.exists(key)).andReturn(true).times(1);
 		expect(this.roleDAO.get(key)).andReturn(role).times(1);
 		expect(this.roleDAO.exists(key)).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			assertEquals(2, role.getUsers().size());
 			this.authManager.removeUserFromRole("domain", "role", "user1");
 			assertEquals(1, role.getUsers().size());
 			this.authManager.removeUserFromRole("domain", "role", "user1");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test
 	public void removeUserFromRoles() {
 		// setup mock
 		Role role = new Role("domain", "role");
 		role.addUsers("user1", "user2");
 		Role role2 = new Role("domain", "role2");
 		role2.addUsers("user1", "user2");
 		List<Role> roles = new LinkedList<Role>();
 		roles.add(role);
 		roles.add(role2);
 		expect(this.roleDAO.getUserRoles("domain", "user1")).andReturn(roles);
 		replay(this.roleDAO);
 		try {
 			// test
 			this.authManager.removeUserFromRoles("domain", "user1");
 			assertEquals(1, role.getUsers().size());
 			assertEquals(1, role2.getUsers().size());
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test
 	public void updateRoleActivities() {
 		// setup mock
 		Role role = new Role("domain", "role");
 		role.addUsers("user1");
 		role.addActivities("activity1", "activity2", "activity3");
 		String key = createRoleKey("domain", "role");
 		expect(this.roleDAO.exists(key)).andReturn(true).times(1);
 		expect(this.roleDAO.get(key)).andReturn(role).times(1);
		this.roleDAO.save(role);
 		replay(this.roleDAO);
 		try {
 			// test
 			this.authManager.updateRoleActivities("domain", "role", Arrays.asList("activity1"));
 			assertEquals(1, role.getActivities().size());
 			assertEquals("[activity1]", role.getActivities().toString());
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void getUserDomains() {
 		// setup mock
 		List<Role> roles = new LinkedList<Role>();
 		Role role = new Role("domain1", "user");
 		role.addUsers("tester");
 		roles.add(role);
 		role = new Role("domain2", "admin");
 		role.addUsers("tester");
 		roles.add(role);
 		role = new Role("domain2", "user");
 		role.addUsers("tester");
 		roles.add(role);
 		expect(this.roleDAO.getUserRoles("tester")).andReturn(roles);
 		expect(this.roleDAO.getUserRoles("newuser")).andReturn(Collections.EMPTY_LIST);
 		replay(this.roleDAO);
 		// test
 		Collection<String> domains = this.authManager.getUserDomains("tester");
 		assertEquals(2, domains.size());
 		domains = this.authManager.getUserDomains("newuser");
 		assertTrue(domains.isEmpty());
 		// verify
 		verify(this.roleDAO);
 	}
 
 	@Test
 	public void authorizeTest() {
 		// setup mock
 		expect(this.roleDAO.existsRoleFor("domain1", "reviewer", "commit_code")).andReturn(false);
 		expect(this.roleDAO.existsRoleFor("domain1", "reviewer", "pull_code")).andReturn(true);
 		expect(this.roleDAO.existsRoleFor("domain2", "tester", "commit_code")).andReturn(true);
 		expect(this.roleDAO.existsRoleFor("domain1", "developer", "commit_code")).andReturn(true);
 		replay(this.roleDAO);
 		// test
 		assertFalse(this.authManager.isAuthorized("domain1", "reviewer", "commit_code"));
 		assertTrue(this.authManager.isAuthorized("domain1", "reviewer", "pull_code"));
 		assertTrue(this.authManager.isAuthorized("domain2", "tester", "commit_code"));
 		assertTrue(this.authManager.isAuthorized("domain1", "developer", "commit_code"));
 
 		// verify
 		verify(this.roleDAO);
 	}
 }
