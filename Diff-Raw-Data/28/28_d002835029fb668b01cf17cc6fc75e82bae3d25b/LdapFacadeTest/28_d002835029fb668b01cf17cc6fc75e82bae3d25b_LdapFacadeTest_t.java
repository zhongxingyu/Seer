 package jamm.ldap;
 
 import jamm.LdapConstants;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.naming.NamingException;
 import javax.naming.AuthenticationException;
 import javax.naming.InvalidNameException;
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.BasicAttributes;
 
 import junit.framework.TestCase;
 
 public class LdapFacadeTest extends TestCase
 {
     public LdapFacadeTest(String name)
     {
         super(name);
     }
 
     protected void setUp()
     {
         mLdap = null;
     }
 
     protected void tearDown()
     {
         if (mLdap != null)
         {
             mLdap.close();
             mLdap = null;
         }
     }
 
     public void testAnonymousBind()
         throws NamingException
     {
         mLdap = new LdapFacade("localhost");
         mLdap.anonymousBind();
         assertNull("Checking name is null", mLdap.getName());
         mLdap.close();
     }
 
     public void testSimpleBind()
         throws NamingException
     {
         mLdap = new LdapFacade("localhost");
 
         mLdap.simpleBind(LdapConstants.MGR_DN, LdapConstants.MGR_PW);
         assertEquals("Checking manager name", LdapConstants.MGR_DN,
                      mLdap.getName());
         mLdap.close();
 
         mLdap.simpleBind(ACCT1_DN, ACCT1_PW);
         assertEquals("Checking account 1 name", ACCT1_DN, mLdap.getName());
         mLdap.close();
 
         mLdap.simpleBind(ACCT2_DN, ACCT2_PW);
         assertEquals("Checking account 2 name", ACCT2_DN, mLdap.getName());
         mLdap.close();
 
         try
         {
             // Try with invalid password
             mLdap.simpleBind(MGR_DN, "badpw");
             fail("Should have thrown AuthenticationException");
         }
         catch (AuthenticationException e)
         {
             // Expected
         }
 
         try
         {
             // Try with invalid DN
             mLdap.simpleBind("baddn", MGR_PW);
             fail("Should have thrown InvalidNameException");
         }
         catch (InvalidNameException e)
         {
             // Expected
         }
     }
 
     public void testGetElementAttributes()
         throws NamingException
     {
         Set expectedObjectClass;
         Set objectClass;
         
         mLdap = new LdapFacade("localhost");
 
         mLdap.simpleBind(ACCT1_DN, ACCT1_PW);
         assertEquals("Checking account1 DN", ACCT1_DN, mLdap.getName());
         assertEquals("Checking mail", "acct1@domain1.test",
                      mLdap.getAttribute("mail"));
         assertEquals("Checking homeDirectory", "/home/vmail/domains",
                      mLdap.getAttribute("homeDirectory"));
         assertEquals("Checking mailbox", "domain1.test/acct1",
                      mLdap.getAttribute("mailbox"));
 
         expectedObjectClass = new HashSet();
         expectedObjectClass.add("top");
         expectedObjectClass.add("JammMailAccount");
         objectClass = mLdap.getAllAttributeValues("objectClass");
         assertEquals("Checking multi-value objectClass", expectedObjectClass,
                      objectClass);
     }
 
     public void testSearchOneLevel()
         throws NamingException
     {
         Set expectedResults;
         Set results;
         
         mLdap = new LdapFacade("localhost");
 
         // There should only be these domains at this level
         expectedResults = new HashSet();
         expectedResults.add(DOMAIN1_DN);
 
         results = new HashSet();
 
         mLdap.anonymousBind();
         mLdap.searchOneLevel("o=hosting,dc=jamm,dc=test",
                              "objectclass=top");
 
         while (mLdap.nextResult())
         {
             results.add(mLdap.getResultName());
         }
 
         assertEquals("Checking results of one level search, " +
                      "(objectclass=top) ",
                      expectedResults, results);
     }
 
     public void testSearchSubtree()
         throws NamingException
     {
         Set expectedResults;
         Set results;
         
         mLdap = new LdapFacade("localhost");
 
         expectedResults = new HashSet();
         expectedResults.add(DOMAIN1_DN);
         expectedResults.add(ACCT1_DN);
         expectedResults.add(ACCT2_DN);
 
         results = new HashSet();
 
         mLdap.anonymousBind();
         mLdap.searchSubtree(DOMAIN1_DN, "objectClass=*");
 
         while (mLdap.nextResult())
         {
             results.add(mLdap.getResultName());
         }
 
         assertEquals("Checking results of subtree search, " +
                      "(objectClass=jammMailAccount)",
                      expectedResults, results);
     }
 
     public void testGetResultAttributes()
         throws NamingException
     {
         Set expectedObjectClass;
         Set objectClass;
         
         mLdap = new LdapFacade("localhost");
         mLdap.anonymousBind();
         mLdap.searchOneLevel("o=hosting,dc=jamm,dc=test",
                              "jvd=domain1.test");
         assertTrue("Checking for results", mLdap.nextResult());
         assertEquals("Checking result dn", DOMAIN1_DN,
                      mLdap.getResultName());
         assertEquals("Checking jvd", "domain1.test",
                      mLdap.getResultAttribute("jvd"));
         expectedObjectClass = new HashSet();
         expectedObjectClass.add("top");
         expectedObjectClass.add("JammVirtualDomain");
         objectClass = mLdap.getAllResultAttributeValues("objectClass");
         assertEquals("Checking objectClass", expectedObjectClass, objectClass);
 
         assertTrue("Checking for no more results", !mLdap.nextResult());
     }
 
     public void testAddModifyDeleteElement()
         throws NamingException
     {
         BasicAttribute  objectClass;
         BasicAttributes attributes;
         String  ouName;
         String  parent;
         String  dn;
 
         ouName = "my_ou";
         parent = "dc=jamm,dc=test";
         dn = "ou=" + ouName + ",dc=jamm,dc=test";
         mLdap = new LdapFacade("localhost");
         mLdap.simpleBind(MGR_DN, MGR_PW);
         
         // This element should not exist
         mLdap.searchOneLevel("dc=jamm,dc=test", "ou=" + ouName);
         assertTrue("ou=" + ouName + " should not exist",
                    !mLdap.nextResult());
 
         // Create a new element
         attributes = new BasicAttributes();
         objectClass = new BasicAttribute("objectClass");
         objectClass.add("top");
         objectClass.add("organizationalUnit");
         attributes.put(objectClass);
         attributes.put("ou", ouName);
         attributes.put("description", "my description");
         mLdap.addElement(dn, attributes);
 
         // See if the element exists and check the values
         mLdap.resetSearch();
         mLdap.searchOneLevel("dc=jamm,dc=test", "ou=" + ouName);
         assertTrue("ou=" + ouName + " should exist",
                    mLdap.nextResult());
 
         Set expectedObjectClass = new HashSet();
         expectedObjectClass.add("top");
         expectedObjectClass.add("organizationalUnit");
         assertEquals("Checking objectClass", expectedObjectClass,
                      mLdap.getAllResultAttributeValues("objectClass"));
         assertEquals("Checking ou", ouName, mLdap.getResultAttribute("ou"));
         assertEquals("Checking description", "my description",
                      mLdap.getResultAttribute("description"));
 
         // Modify the description
         mLdap.modifyElementAttribute(dn, "description", "new description");
         mLdap.resetSearch();
         mLdap.searchOneLevel("dc=jamm,dc=test", "ou=" + ouName);
         assertTrue("Checking for results", mLdap.nextResult());
         assertEquals("Checking description", "new description",
                      mLdap.getResultAttribute("description"));
 
         // Delete element
         mLdap.deleteElement(dn);
 
         // This element should not exist
         mLdap.resetSearch();
         mLdap.searchOneLevel("dc=jamm,dc=test", "ou=" + ouName);
         assertTrue("ou=" + ouName + " should not exist",
                    !mLdap.nextResult());
     }
 
     public void testAddUsingMap()
         throws NamingException
     {
         String ouName = "ou_map";
         String parent = "dc=jamm,dc=test";
         String dn = "ou=" + ouName + ",dc=jamm,dc=test";
         mLdap = new LdapFacade("localhost");
         mLdap.simpleBind(MGR_DN, MGR_PW);
         
         // This element should not exist
         mLdap.searchOneLevel("dc=jamm,dc=test", "ou=" + ouName);
         assertTrue("ou=" + ouName + " should not exist",
                    !mLdap.nextResult());
 
         // Create a new element
         Map attributes = new HashMap();
         attributes.put("objectClass", new String[] { "top",
                                                      "organizationalUnit"});
         Set telephoneNumbers = new HashSet();
         telephoneNumbers.add("555-1234");
         telephoneNumbers.add("555-6789");
         attributes.put("telephoneNumber", telephoneNumbers);
         attributes.put("ou", ouName);
        attributes.put("description", "my description");
         mLdap.addElement(dn, attributes);
 
         // See if the element exists and check the values
         mLdap.resetSearch();
         mLdap.searchOneLevel("dc=jamm,dc=test", "ou=" + ouName);
         assertTrue("ou=" + ouName + " should exist",
                    mLdap.nextResult());
 
         Set expectedObjectClass = new HashSet();
         expectedObjectClass.add("top");
         expectedObjectClass.add("organizationalUnit");
         assertEquals("Checking objectClass", expectedObjectClass,
                      mLdap.getAllResultAttributeValues("objectClass"));
         assertEquals("Checking ou", ouName, mLdap.getResultAttribute("ou"));
         assertEquals("Checking description", "my description",
                      mLdap.getResultAttribute("description"));
 
         Set expectedTelephoneNumbers = new HashSet();
         expectedTelephoneNumbers.add("555-1234");
         expectedTelephoneNumbers.add("555-6789");
         assertEquals("Checking telephoneNumber", expectedTelephoneNumbers,
                      mLdap.getAllResultAttributeValues("telephoneNumber"));
     }
 
     private LdapFacade mLdap;
 
     private static final String MGR_DN = "cn=Manager,dc=jamm,dc=test";
     private static final String MGR_PW = "jammtest";
 
     private static final String DOMAIN1_DN =
         "jvd=domain1.test,o=hosting,dc=jamm,dc=test";
 
     private static final String PM1_DN =
         "cn=postmaster,jvd=domain1.test,o=hosting,dc=jamm,dc=test";
     private static final String PM1_PW = "postmaster1pw";
 
     private static final String ABUSE1_DN =
         "mail=abuse@domain1.test,jvd=domain2.test,o=hosting,dc=jamm,dc=test";
 
     private static final String ACCT1_DN =
         "mail=acct1@domain1.test,jvd=domain1.test,o=hosting,dc=jamm,dc=test";
     private static final String ACCT1_PW = "acct1pw";
 
     private static final String ACCT2_DN =
         "mail=acct2@domain1.test,jvd=domain1.test,o=hosting,dc=jamm,dc=test";
     private static final String ACCT2_PW = "acct2pw";
 
     private static final String DOMAIN2_DN =
         "jvd=domain2.test,o=hosting,dc=jamm,dc=test";
 
     private static final String PM2_DN =
         "cn=postmaster,jvd=domain2.test,o=hosting,dc=jamm,dc=test";
     private static final String PM2_PW = "postmaster2pw";
     
     private static final String ABUSE2_DN =
         "mail=abuse@domain2.test,jvd=domain2.test,o=hosting,dc=jamm,dc=test";
 
     private static final String ACCT3_DN =
         "mail=acct3@domain2.test,jvd=domain2.test,o=hosting,dc=jamm,dc=test";
     private static final String ACCT3_PW = "acct3pw";
 
     private static final String ACCT4_DN =
         "mail=acct4@domain2.test,jvd=domain2.test,o=hosting,dc=jamm,dc=test";
     private static final String ACCT4_PW = "acct4pw";
 }
