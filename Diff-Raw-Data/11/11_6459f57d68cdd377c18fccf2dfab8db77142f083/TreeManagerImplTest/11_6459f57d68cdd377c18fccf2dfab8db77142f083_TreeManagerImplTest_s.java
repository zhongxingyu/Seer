 package org.devilry.core.session;
 
 import java.util.*;
 import javax.naming.NamingException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.devilry.core.entity.*;
 import org.devilry.core.session.*;
 import org.devilry.core.session.dao.*;
 
 public class TreeManagerImplTest extends AbstractSessionBeanTestHelper {
 	TreeManagerRemote tm;
 
 	@Before
 	public void setUp() throws NamingException {
 		setupEjbContainer();
 		tm = getRemoteBean(TreeManagerImpl.class);
 	}
 
 	@After
 	public void tearDown() {
 		destroyEjbContainer();
 	}
 
 	@Test
 	public void addNode() {
 		tm.addNode("uio", "Universitetet i Oslo");
		assertTrue(0 != tm.getNodeIdFromPath("uio"));
 
 	}
 
 	@Test
 	public void addCourseNode() {
 		tm.addNode("uio", "Universitetet i Oslo");
 		tm.addCourseNode("inf1000", "INF1000", 
 				"Grunnkurs i objektorientert programmering", 
 				tm.getNodeIdFromPath("uio"));
 
		assertTrue(0 != tm.getNodeIdFromPath("uio.inf1000"));
 	}
 
 	@Test
 	public void addPeriodNode() {
 		tm.addNode("uio", "Universitetet i Oslo");
 		tm.addCourseNode("inf1000", "INF1000", "Grunnkurs i objektorientert programmering",
 				tm.getNodeIdFromPath("uio"));
 
 		Calendar start = new GregorianCalendar(2009, 00, 01);
 		Calendar end = new GregorianCalendar(2009, 05, 15);
 
 		tm.addPeriodNode("fall09", "Fall 2009", start.getTime(), end.getTime(),
 				tm.getNodeIdFromPath("uio.inf1000"));
 
		assertTrue(0 != tm.getNodeIdFromPath("uio.inf1000.fall09"));
 	}
 
 	@Test
 	public void getNodeIdFromPath() {
 		tm.addNode("uio", "Universitetet i Oslo");
		assertTrue(0 != tm.getNodeIdFromPath("uio"));
 	}
 }
 
