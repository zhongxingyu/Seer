 // PrivilegeTest.java
 
 package gov.nih.nci.security.authorization.domainobjects;
 
 import gov.nih.nci.security.dao.hibernate.HibernateSessionFactory;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import net.sf.hibernate.Session;
 import net.sf.hibernate.Transaction;
 
 /**
  * PrivilegeTest (Copyright 2001 Your Company)
  * 
  * <p>
  * This class performs unit tests on
  * gov.nih.nci.security.authorization.domainobjects.Privilege
  * </p>
  * 
  * <p>
  * Explanation about the tested class and its responsibilities
  * </p>
  * 
  * <p>
  * Relations: Privilege extends java.lang.Object <br>
  * 
  * @author Your Name Your email - Your Company
 * @date $Date: 2004-12-06 22:15:16 $
 * @version $Revision: 1.2 $
  * 
  * @see gov.nih.nci.security.authorization.domainobjects.Privilege
  * @see some.other.package
  */
 
 public class PrivilegeTest extends TestCase {
 
 	/**
 	 * Constructor (needed for JTest)
 	 * 
 	 * @param name
 	 *            Name of Object
 	 */
 	public PrivilegeTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * Used by JUnit (called before each test method)
 	 */
 	protected void setUp() {
 		//privilege = new Privilege();
 	}
 
 	/**
 	 * Used by JUnit (called after each test method)
 	 */
 	protected void tearDown() {
 		privilege = null;
 	}
 
 	/**
 	 * Test the constructor: Privilege()
 	 */
 	public void testPrivilege() {
 
 	}
 
 	/**
 	 * Test method: void finalize() finalize throws java.lang.Throwable
 	 */
 	public void testFinalize() {
 
 	}
 
 	/**
 	 * Test method: String getName()
 	 */
 	public void testGetName() {
 
 	}
 
 	/**
 	 * Test method: void setName(String)
 	 */
 	public void testSetName() {
 		//Must test for the following parameters!
 		String str[] = { null, "\u0000", " " };
 
 	}
 
 	/**
 	 * Test method: String getDesc()
 	 */
 	public void testGetDesc() {
 
 	}
 
 	/**
 	 * Test method: java.lang.Long getId()
 	 */
 	public void testGetId() {
 
 	}
 
 	/**
 	 * Test method: void setDesc(String)
 	 */
 	public void testSetDesc() {
 		//Must test for the following parameters!
 		String str[] = { null, "\u0000", " " };
 
 	}
 
 	/**
 	 * Test method: void setId(Long)
 	 */
 	public void testSetId() {
 		//Must test for the following parameters!
 		//Long;
 
 	}
 
 	public void testCreate() throws Exception {
 		Session s = null;
 		Transaction t = null;
 		System.out.println("Running create test...");
 		try {
 			s = HibernateSessionFactory.currentSession();
 
 			t = s.beginTransaction();
 			Privilege p = new Privilege();
 			
 			p.setDesc("TestDesc");
 			p.setName("TestName");
 			s.save(p);
 			t.commit();
 		} catch (Exception ex) {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw ex;
 		}
 
 	}
 
 	/**
 	 * Main method needed to make a self runnable class
 	 * 
 	 * @param args
 	 *            This is required for main method
 	 */
 	public static void main(String[] args) {
 		junit.textui.TestRunner.run(new TestSuite(PrivilegeTest.class));
 	}
 
 	private Privilege privilege;
 }
