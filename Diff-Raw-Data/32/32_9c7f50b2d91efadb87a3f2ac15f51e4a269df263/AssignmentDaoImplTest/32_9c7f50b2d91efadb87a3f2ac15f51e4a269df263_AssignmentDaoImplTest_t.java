 /**
  * 
  */
 
 package org.sakaiproject.assignment2.dao.test;
 
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 /**
  * @author michellewagner (this was totally written by michelle and only michelle and no one else should take credit for it)
  * 
  */
 public class AssignmentDaoImplTest extends
 		AbstractTransactionalSpringContextTests {
 
 	protected AssignmentDao assignmentDao;
 
 	protected String[] getConfigLocations() {
 		// point to the needed spring config files, must be on the classpath
 		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the pom.xml file
 		return new String[] { "hibernate-test.xml", "spring-hibernate.xml" };
 	}
 
 	// run this before each test starts
 	protected void onSetUpBeforeTransaction() throws Exception {
 		// load the spring created dao class bean from the Spring Application
 		// Context
 		assignmentDao = (AssignmentDao) applicationContext
 				.getBean("org.sakaiproject.assignment2.dao.AssignmentDao");
 		if (assignmentDao == null) {
 			throw new NullPointerException(
 					"DAO could not be retrieved from spring");
 		}
 
 		// init the test class if needed
 
 	}
 
 	// run this before each test starts and as part of the transaction
 	protected void onSetUpInTransaction() {
 		// preload additional data if desired
 
 	}
 
 	/**
 	 * ADD unit tests below here, use testMethod as the name of the unit test,
 	 * Note that if a method is overloaded you should include the arguments in
 	 * the test name like so: testMethodClassInt (for method(Class, int);
 	 */
 
 	/**
 	 * Test method for
 	 * {@link org.sakaiproject.assignment2.dao.impl.AssignmentDaoImpl#getHighestSortIndexInSite(java.lang.String)}.
 	 */
 	public void testGetHighestSortIndexInSite() {
 		// positive test
 		// count 2 or 3 items in the known context
 
 		// negative test
 		// count 0 items in unknown context
 		int highestIndex = assignmentDao.getHighestSortIndexInSite("context");
 		assertEquals(0, highestIndex);
 		
 		// exception testing
 		try {
 			assignmentDao.getHighestSortIndexInSite(null);
 			fail("Should have thrown exception");
 		} catch (IllegalArgumentException e) {
 			assertNotNull(e);
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.sakaiproject.assignment2.dao.impl.AssignmentDaoImpl#getAssignmentsWithGroups(java.lang.String)}.
 	 */
 	public void testGetAssignmentsWithGroups() {
 		//fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.sakaiproject.assignment2.dao.impl.AssignmentDaoImpl#getAssignmentByIdWithGroupsAndAttachments(java.lang.Long)}.
 	 */
 	public void testGetAssignmentByIdWithGroupsAndAttachments() {
 		//fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.sakaiproject.assignment2.dao.impl.AssignmentDaoImpl#getAssignmentByIdWithGroups(java.lang.Long)}.
 	 */
 	public void testGetAssignmentByIdWithGroups() {
 		//fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.sakaiproject.assignment2.dao.impl.AssignmentDaoImpl#findCurrentSubmissionsForAssignment(org.sakaiproject.assignment2.model.Assignment2)}.
 	 */
 	public void testFindCurrentSubmissionsForAssignment() {
 		//fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.sakaiproject.assignment2.dao.impl.AssignmentDaoImpl#findSubmissionForAssignmentAndUserWithAttachments(org.sakaiproject.assignment2.model.Assignment2, java.lang.String)}.
 	 */
 	public void testFindSubmissionForAssignmentAndUserWithAttachments() {
 		//fail("Not yet implemented");
 	}
 
 }
