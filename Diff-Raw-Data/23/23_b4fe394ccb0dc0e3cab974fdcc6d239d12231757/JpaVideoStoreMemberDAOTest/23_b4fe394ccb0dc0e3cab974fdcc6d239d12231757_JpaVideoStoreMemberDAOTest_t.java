 package com.cit.eugene.service.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.NoResultException;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.cit.eugene.model.Authorities;
 import com.cit.eugene.model.User;
 import com.cit.eugene.model.VideoStoreMember;
 
@ContextConfiguration("file:src/test/resources/test-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 public class JpaVideoStoreMemberDAOTest {
 
 	private User u = null;
 	
 	@Autowired
 	private JpaVideoStoreMemberDAO jpaVideoStoreMember;
 	
 	@Before
 	public void setUp() throws Exception {
 		u = new User();
 		u.setEnabled(true);
 		u.setUsername("bob");
 		u.setPassword("password");
 		Authorities a = new Authorities();
 		a.setAuthority("ROLE_USER");
 		Set<Authorities> l = new HashSet<Authorities>();
 		l.add(a);
 		u.setAuthoritieses(l);
 	}
 
 	@Test
 	public void testGetAllVideoStoreMembers() {
 		List<VideoStoreMember> l = jpaVideoStoreMember.getAllVideoStoreMembers();
 		assertNotNull(l);
 		assertEquals(1, l.size());
 	}
 
 	@Test
 	public void testGetVideoStoreMemberByID() {
 		VideoStoreMember vsm = jpaVideoStoreMember.getVideoStoreMemberByID(1l);
 		assertNotNull(vsm);
 		assertEquals("keri", vsm.getName());
 		assertNotNull(vsm.getUser());
 		assertEquals("keri", vsm.getUser().getUsername());
 		assertEquals("Cork", vsm.getLocation());
 		assertEquals("122345456", vsm.getMemebershipNumber());
 	}
 
 	@Test
 	public void testStoreVideoStoreMember() {
 		VideoStoreMember vsm = new VideoStoreMember();
 		vsm.setLocation("location");
 		vsm.setName("bob");
 		vsm.setVideoStoreMemberID(1111112l);
 		vsm.setUser(u);
 		VideoStoreMember expected = jpaVideoStoreMember.storeVideoStoreMember(vsm);
 		assertNotNull(expected);
 		assertNotNull(expected.getVideoStoreMemberID());
 		assertEquals("location", vsm.getLocation());
 		assertEquals("bob", vsm.getName());
 	}
 	
 	@Test
 	public void testStoreVideoStoreMember2() {
 		VideoStoreMember vsm = new VideoStoreMember();
 		VideoStoreMember expected = jpaVideoStoreMember.storeVideoStoreMember(vsm);
 		assertNotNull(expected);
 		assertNull(expected.getVideoStoreMemberID());
 	}
 
 	@Test
 	public void testGetVideoStoreMemberByName() {
 		VideoStoreMember vsm = jpaVideoStoreMember.getVideoStoreMemberByName("keri");
 		assertNotNull(vsm);
 		assertEquals("keri", vsm.getName());
 		assertNotNull(vsm.getUser());
 		assertEquals("keri", vsm.getUser().getUsername());
 		assertEquals("keri", vsm.getUser().getPassword());
 		assertEquals(true, vsm.getUser().isEnabled());
 		assertEquals("Cork", vsm.getLocation());
 		assertEquals("122345456", vsm.getMemebershipNumber());
 	}
 
 	@Test
 	public void testDeleteVideoStoreMember() {
 		VideoStoreMember vsm = new VideoStoreMember();
 		vsm.setLocation("location");
 		vsm.setName("bob");
 		vsm.setVideoStoreMemberID(1111112l);
 		vsm.setUser(u);
 		VideoStoreMember expected = jpaVideoStoreMember.storeVideoStoreMember(vsm);
 		assertNotNull(expected);
 		assertNotNull(expected.getVideoStoreMemberID());
 		assertEquals("location", vsm.getLocation());
 		assertEquals("bob", vsm.getName());
 		jpaVideoStoreMember.deleteVideoStoreMember(expected.getVideoStoreMemberID());
 		try {
 			VideoStoreMember expected2 = jpaVideoStoreMember.getVideoStoreMemberByID(expected.getVideoStoreMemberID());
 			assertNotNull(expected2);
 			assertNull(expected2.getVideoStoreMemberID());
 			} catch (NoResultException e) {
 			assertTrue(true);
 			return;
 		}
 		//if we get here we have failed.
 		fail();
 	}
 }
