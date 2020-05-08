 package de.fiz.ddb.aas.test.organization.aa;
 
 import org.apache.http.entity.ContentType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import de.fiz.ddb.aas.test.AasTestBase;
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.exceptions.HttpClientException;
 import de.fiz.ddb.aas.test.organization.OrganizationUtility;
 import de.fiz.ddb.aas.test.person.PersonUtility;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 import de.fiz.ddb.aas.test.util.privilege.Privilege;
 import de.fiz.ddb.aas.test.util.privilege.PrivilegeItem;
 import de.fiz.ddb.aas.test.util.privilege.PrivilegeSet;
 
 /**
  * Tests for organization authorizations 
  * Run with xml-representations and json representations.
  * 
  * @author ttr
  *
  */
 @RunWith(value = Parameterized.class)
 public class OrganizationAaIT extends AasTestBase {
 
     private OrganizationUtility organizationUtility;
 
     private PersonUtility personUtility;
 
     private ContentType contentType;
 
     private static String uid = null;
 
     private static String password = null;
 
     private static int methodCounter = 0;
 
     public OrganizationAaIT(ContentType contentType) throws Exception {
         organizationUtility = new OrganizationUtility();
         personUtility = new PersonUtility();
         this.contentType = contentType;
     }
 
     /**
      * Set up servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Before
     public void initialize() throws Exception {
         if (methodCounter == 0) {
             String[] response =
                 personUtility.doTestCreatePersonRetreavingUidPswd(null, null, "person", true,
                     PersonUtility.contentType, null);
             uid = response[0];
             password = response[1];
         }
     }
 
     /**
      * Clean up after servlet test.
      *
      * @throws Exception If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
         methodCounter++;
     }
 
     /**
      * Test creating an organization with empty parent element and orgId
      */
     @Test
     public void testCreateOrganizationEmptyParentId() throws Exception {
         organizationUtility
             .doTestCreateOrganization(uid, password, "organization_with_parent", true, contentType, null);
     }
 
     /**
      * Test creating an organization with parent element and orgId
      */
     @Test
     public void testCreateOrganizationParentId() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.doTestCreateOrganizationWithParent(uid, password, "organization_with_parent", parentId,
             true, contentType, null);
     }
 
     /**
      * Test deleting an organization 
      */
     @Test
     public void testDeleteOrganization() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource = organizationUtility.deleteOrganization(uid, password, id, contentType, null);
     }
 
     /**
      * Test deleting an organization created with admin privileges
      */
     @Test
     public void testDeleteOrganizationCreatedByAdmin() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource = organizationUtility.deleteOrganization(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test retrieving an organization created with admin privileges
      */
     @Test
     public void testRetrieveOrganization() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource = organizationUtility.retrieveOrganization(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test retrieving an organization created with simple user privileges.
      */
     @Test
     public void testRetrievePersonsOrganizations() throws Exception {
         String organization =
             organizationUtility.doTestCreateOrganization(uid, password, "organization_with_parent", true, contentType,
                 null);
         String orgId = Utility.extract(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.createUserPrivilege(uid, password, orgId, Privilege.ADMIN_USER.toString(), uid,
             contentType, null);
         personUtility.retrieveOrganizations(uid, password, uid, contentType, null);
     }
 
     /**
      * Test retrieving organization as creator
      */
     @Test
     public void testRetrieveOrganizationAsCreator() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.retrieveOrganization(uid, password, parentId, contentType, null);
     }
 
     /**
      * Test retrieving sub-organizations created with simple user privileges.
      */
     @Test
     public void testRetrieveSubOrganizations() throws Exception {
         OrganizationUtility.MyHashMap resources =
             organizationUtility.doTestCreateOrganizationTree(uid, password, contentType, null);
         for (String orgId : resources.keySet()) {
             organizationUtility.retrieveSubOrganizations(uid, password, orgId, contentType, null);
         }
     }
 
     /**
      * Test creating privilege of an admin organization
      */
     @Test
     public void testCreatePrivilege() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.FTP_UPLOAD);
         resource =
             organizationUtility.createPrivilege(uid, password, id, privilegeItem, contentType,
                 HttpClientException.class);
 
     }
 
     /**
      * Test creating privilege of an user organization
      */
     @Test
     public void testCreateUserPrivilege() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         PrivilegeItem privilegeItem = new PrivilegeItem();
        privilegeItem.setPrivilege(Privilege.FTP_UPLOAD);
         resource = organizationUtility.createPrivilege(uid, password, id, privilegeItem, contentType, null);
 
     }
 
     /**
      * Test retrieving organization-privileges
      */
     @Test
     public void testRetrievePrivileges() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource = organizationUtility.retrievePrivileges(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test updating an organization-privilege
      */
     @Test
     public void testUpdatePrivilege() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String user =
             personUtility.doTestCreatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, "person",
                 true, PersonUtility.contentType, null);
         String uid1 = Utility.extract(user, Constants.XPATH_PERSON_ID, PersonUtility.contentType);
 
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.ADMIN_ORG);
         privilegeItem.getUserIds().add(uid1);
         privilegeItem.getUserIds().add("admin");
         resource =
             organizationUtility.updatePrivilege(uid, password, id, Privilege.ADMIN_ORG.toString(), privilegeItem,
                 contentType, null);
     }
 
     /**
      * Test updating an organization-privilege created by admin 
      */
     @Test
     public void testUpdateAdminOrganizationPrivilege() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
 
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.ADMIN_ORG);
         privilegeItem.getUserIds().add(uid);
         privilegeItem.getUserIds().add("admin");
         resource =
             organizationUtility.updatePrivilege(uid, password, id, Privilege.ADMIN_ORG.toString(), privilegeItem,
                 contentType, HttpClientException.class);
     }
 
     /**
      * Test updating organization-privileges
      */
     @Test
     public void testUpdatePrivileges() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
 
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
         privilegeItem.getUserIds().add(uid1);
         privilegeItem.getUserIds().add("admin");
         PrivilegeItem privilegeItem1 = new PrivilegeItem();
         privilegeItem1.setPrivilege(Privilege.ADMIN_USER);
         privilegeItem1.getUserIds().add(uid2);
         PrivilegeSet privilegeSet = new PrivilegeSet();
         privilegeSet.getPrivilegeItems().add(privilegeItem);
         privilegeSet.getPrivilegeItems().add(privilegeItem1);
         organizationUtility.updatePrivileges(uid, password, id, privilegeSet, contentType, HttpClientException.class);
     }
 
     /**
      * Test deleting admin privilege 
      */
     @Test
     public void testDeleteAdminPrivilege() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource =
             organizationUtility.deletePrivilege(uid, password, id, Privilege.ADMIN_ORG.toString(), contentType,
                 HttpClientException.class);
     }
 
 }
