 /**
  * Test updating organization-privileges.
  */
 package de.fiz.ddb.aas.test.organization;
 
 import static org.junit.Assert.assertTrue;
 
 import org.apache.http.entity.ContentType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import de.fiz.ddb.aas.test.AasTestBase;
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.exceptions.BadRequest;
 import de.fiz.ddb.aas.test.exceptions.NotFound;
 import de.fiz.ddb.aas.test.person.PersonUtility;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 import de.fiz.ddb.aas.test.util.privilege.Privilege;
 import de.fiz.ddb.aas.test.util.privilege.PrivilegeItem;
 import de.fiz.ddb.aas.test.util.privilege.PrivilegeSet;
 
 /**
  * Tests for organization-resource privileges methods
  * Run with xml-representations and json representations.
  * 
  * tests methods:
  * update privileges of an organization
  *
  * @author mih
  *
  */
 @RunWith(value = Parameterized.class)
 public class OrganizationPrivilegesUpdateIT extends AasTestBase {
 
     private OrganizationUtility organizationUtility;
 
     private PersonUtility personUtility;
 
     private ContentType contentType;
 
     /**
      * Constructor with contentType
      * 
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @throws Exception
      */
     public OrganizationPrivilegesUpdateIT(ContentType contentType) throws Exception {
         organizationUtility = new OrganizationUtility();
         personUtility = new PersonUtility();
         this.contentType = contentType;
     }
 
     /**
      * check for admin-user before tests run.
      * 
      * @throws Exception
      */
     @BeforeClass
     public static void init() throws Exception {
         initAdminUser();
     }
 
     /**
      * Set up test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Before
     public void initialize() throws Exception {
     }
 
     /**
      * Clean up after test.
      *
      * @throws Exception If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
     }
 
     /**
      * Test updating organization-privileges
      */
     @Test
     public void testUpdatePrivileges() throws Exception {
         //create Organization
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
 
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
 
         //create ADMIN_USER Privilege
         PrivilegeItem privilege = new PrivilegeItem();
         privilege.setPrivilege(Privilege.ADMIN_USER);
         organizationUtility.createPrivilege(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id, privilege,
             contentType, null);
 
         String user =
             personUtility.doTestCreatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, "person",
                 true, PersonUtility.contentType, null);
         String uid1 = Utility.extract(user, Constants.XPATH_PERSON_ID, PersonUtility.contentType);
         user =
             personUtility.doTestCreatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, "person",
                 true, PersonUtility.contentType, null);
         String uid2 = Utility.extract(user, Constants.XPATH_PERSON_ID, PersonUtility.contentType);
 
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.ADMIN_ORG);
         privilegeItem.getIds().add(uid1);
         privilegeItem.getIds().add("admin");
         PrivilegeItem privilegeItem1 = new PrivilegeItem();
         privilegeItem1.setPrivilege(Privilege.ADMIN_USER);
         privilegeItem1.getIds().add(uid2);
         PrivilegeSet privilegeSet = new PrivilegeSet();
         privilegeSet.getPrivilegeItems().add(privilegeItem);
         privilegeSet.getPrivilegeItems().add(privilegeItem1);
         resource =
             organizationUtility.updatePrivileges(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id,
                 privilegeSet, contentType, null);
 
         //check Privileges
         PrivilegeSet retrievedSet = new PrivilegeSet(resource, contentType);
         assertTrue("Privileges are not as expected", privilegeSet.equals(retrievedSet));
 
     }
 
     /**
      * Test updating organization-privileges with nonexisting user
      */
     @Test
     public void testUpdatePrivilegesNonexistingUser() throws Exception {
         //create Organization
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
 
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
 
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.ADMIN_ORG);
         privilegeItem.getIds().add("nonexisting");
         privilegeItem.getIds().add("admin");
         PrivilegeSet privilegeSet = new PrivilegeSet();
         privilegeSet.getPrivilegeItems().add(privilegeItem);
         resource =
             organizationUtility.updatePrivileges(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id,
                 privilegeSet, contentType, BadRequest.class);
     }
 
     /**
      * Test updating organization-privileges with nonexisting privilege
      */
     @Test
     public void testUpdatePrivilegesNonexistingPrivilege() throws Exception {
         //create Organization
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
 
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
 
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.NONEXISTING);
         privilegeItem.getIds().add("admin");
         PrivilegeSet privilegeSet = new PrivilegeSet();
         privilegeSet.getPrivilegeItems().add(privilegeItem);
        Class<?> expectedException = null;
         //application reacts differently for xml and json with enum-values!!
         if (contentType.equals(ContentType.APPLICATION_XML)) {
             expectedException = NotFound.class;
         }
         else {
             expectedException = BadRequest.class;
         }
         resource =
             organizationUtility.updatePrivileges(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id,
                 privilegeSet, contentType, expectedException);
     }
 
     /**
      * Test updating organization-privileges with privilege not created yet
      */
     @Test
     public void testUpdatePrivilegesNoncreatedPrivilege() throws Exception {
         //create Organization
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
 
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
 
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.ADMIN_USER);
         privilegeItem.getIds().add("admin");
         PrivilegeSet privilegeSet = new PrivilegeSet();
         privilegeSet.getPrivilegeItems().add(privilegeItem);
         resource =
             organizationUtility.updatePrivileges(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id,
                 privilegeSet, contentType, NotFound.class);
     }
 
 }
