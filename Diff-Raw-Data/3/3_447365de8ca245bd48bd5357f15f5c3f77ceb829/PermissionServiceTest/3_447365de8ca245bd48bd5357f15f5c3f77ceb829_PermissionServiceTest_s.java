 package dbs.project.service;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.LinkedList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import dbs.project.dao.ActorDao;
 import dbs.project.dao.PermissionDao;
 import dbs.project.dao.ResourceDao;
 import dbs.project.dao.TournamentDao;
 import dbs.project.entity.Country;
 import dbs.project.entity.Tournament;
 import dbs.project.entity.permission.Actor;
 import dbs.project.entity.permission.Permission;
 import dbs.project.entity.permission.Resource;
 
 public class PermissionServiceTest {
 
 	Actor a;
 	Permission p;
 	Resource r;
 	Tournament t;
 
 	@Before
 	public void setUp() throws Exception {
 
 		final String plaintextPW = "plaintext";
 
 		a = new Actor();
 		a.setEmail("jogi@baer.org");
 		a.setPassword(plaintextPW);
 
 		t = new Tournament();
 		t.setYear(1990);
 		t.setName("Turkmenistan");
 
 		p = new Permission();
 		r = new Resource();
 		r.setName(Tournament.class.getName());
 		r.setKey(1990);
 		p.setTypeOfAccess(Permission.AccessType.READ);
 		p.setResource(r);
 
 		LinkedList<Permission> permissions = new LinkedList<Permission>();
 		permissions.add(p);
 		a.setPermissions(permissions);
 
 		TournamentDao.save(t);
 		ResourceDao.save(r);
 		ActorDao.save(a);
 		PermissionDao.save(p);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		TournamentDao.delete(t);
		ResourceDao.delete(r);
 		ActorDao.delete(a);
 		PermissionDao.delete(p);
 	}
 
 	@Test
 	public void testIsAllowed() {
 
 		Permission reqPerm = new Permission();
 		reqPerm.setResource(r);
 
 		reqPerm.setTypeOfAccess(Permission.AccessType.READ);
 		assertTrue(PermissionService.isAllowed(a, t, reqPerm));
 
 		reqPerm.setTypeOfAccess(Permission.AccessType.UPDATE);
 		assertFalse(PermissionService.isAllowed(a, t, reqPerm));
 
 		reqPerm.setTypeOfAccess(Permission.AccessType.READ);
 		reqPerm.getResource().setKey(1991);
 		assertFalse(PermissionService.isAllowed(a, t, reqPerm));
 
 		reqPerm.getResource().setKey(1990);
 		assertTrue(PermissionService.isAllowed(a, t, reqPerm));
 
 		r.setName(Country.class.getName());
 		assertFalse(PermissionService.isAllowed(a, t, reqPerm));
 	}
 }
