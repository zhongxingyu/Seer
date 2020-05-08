 package de.fiz.ddb.aas.test.organization.aa;
 
 import org.apache.http.entity.ContentType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import de.fiz.ddb.aas.test.AasTestBase;
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
                 personUtility.doTestCreatePersonRetreavingUidPswd(null, null, "person", true, contentType, null);
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
         organizationUtility.doTestCreateOrganization(uid, password, "organization_empty_parent_id", true, contentType,
             null);
     }
 
     /**
      * Test creating an organization with parent element and orgId
      */
     @Test
     public void testCreateOrganizationParentId() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(uid, password, "organization_no_parent_id", true, contentType,
                 null);
         String parentId = Utility.extract(parent, "/organization/id", contentType);
         organizationUtility.doTestCreateOrganizationWithParent(uid, password, "organization_empty_parent_id", parentId,
             true, contentType, null);
     }
 
     /**
      * Test deleting an organization 
      */
     @Test
     public void testDeleteOrganization() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(uid, password, "organization_no_parent_id", true, contentType,
                 null);
         String id = Utility.extract(resource, "/organization/id", contentType);
         resource = organizationUtility.doTestDeleteOrganization(uid, password, id, contentType, null);
     }
 
     /**
      * Test deleting an organization created with admin privileges
      */
     @Test
    public void testDeleteOrganizationCreatedByAdmin() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_no_parent_id", true, contentType, null);
         String id = Utility.extract(resource, "/organization/id", contentType);
         resource =
             organizationUtility.doTestDeleteOrganization(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test retrieving an organization created with admin privileges
      */
     @Test
     public void testRetrieveOrganization() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_no_parent_id", true, contentType, null);
         String id = Utility.extract(resource, "/organization/id", contentType);
         resource =
             organizationUtility.doTestRetrieveOrganization(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test retrieving an organization created with simple user privileges.
      */
     @Test
     public void testRetrievePersonsOrganizations() throws Exception {
         String organization =
             organizationUtility.doTestCreateOrganization(uid, password, "organization_empty_parent_id", true,
                 contentType, null);
         String orgId = Utility.extract(organization, "/organization/id", contentType);
         organizationUtility.doTestCreateUserPrivilege(uid, password, orgId, Privilege.ADMIN_USER.toString(), uid,
             contentType, null);
         personUtility.doTestRetrieveOrganizations(uid, password, uid, contentType, null);
     }
 
     /**
      * Test retrieving organization as creator
      */
     @Test
     public void testRetrieveOrganizationAsCreator() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(uid, password, "organization_no_parent_id", true, contentType,
                 null);
         String parentId = Utility.extract(parent, "/organization/id", contentType);
         organizationUtility.doTestRetrieveOrganization(uid, password, parentId, contentType, null);
     }
 
     /**
      * Test retrieving sub-organizations created with simple user privileges.
      */
     @Test
     public void testRetrieveSubOrganizations() throws Exception {
         OrganizationUtility.MyHashMap resources =
             organizationUtility.doTestCreateOrganizationTree(uid, password, contentType, null);
         for (String orgId : resources.keySet()) {
             organizationUtility.doTestRetrieveSubOrganizations(uid, password, orgId, contentType, null);
         }
     }
 
     /**
      * Test retrieving organization-privileges
      */
     @Test
     public void testRetrievePrivileges() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_no_parent_id", true, contentType, null);
         String id = Utility.extract(resource, "/organization/id", contentType);
         resource =
             organizationUtility.doTestRetrievePrivileges(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test updating organization-privileges
      */
     @Test
     public void testUpdatePrivileges() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_no_parent_id", true, contentType, null);
         String id = Utility.extract(resource, "/organization/id", contentType);
 
         String user =
             personUtility.doTestCreatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, "person",
                 true, contentType, null);
         String uid1 = Utility.extract(user, "/user/id", contentType);
         user =
             personUtility.doTestCreatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, "person",
                 true, contentType, null);
         String uid2 = Utility.extract(user, "/user/id", contentType);
 
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
         organizationUtility.doTestUpdatePrivileges(uid, password, id, privilegeSet, contentType,
             HttpClientException.class);
     }
 
     /**
      * Test creating privilege of an admin organization
      */
     @Test
     public void testCreatePrivilege() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_no_parent_id", true, contentType, null);
         String id = Utility.extract(resource, "/organization/id", contentType);
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.FTP_UPLOAD);
         resource =
             organizationUtility.doTestCreatePrivilege(uid, password, id, privilegeItem, contentType,
                 HttpClientException.class);
 
     }
 
     /**
      * Test creating privilege of an user organization
      */
     @Test
     public void testCreateUserPrivilege() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(uid, password, "organization_no_parent_id", true, contentType,
                 null);
         String id = Utility.extract(resource, "/organization/id", contentType);
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.FTP);
         resource =
             organizationUtility.doTestCreatePrivilege(uid, password, id, privilegeItem, contentType,
                 HttpClientException.class);
 
     }
 }
