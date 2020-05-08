 /**
  * 
  */
 package de.fiz.ddb.aas.test.organization;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.entity.ContentType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import de.fiz.ddb.aas.test.AasTestBase;
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.person.PersonUtility;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 import de.fiz.ddb.aas.test.util.privilege.Privilege;
 import de.fiz.ddb.aas.test.util.privilege.PrivilegeItem;
 
 /**
  * Create Tests for organization retrieve
  * Run with xml-representations and json representations.
  * 
  * @author ttr
  *
  */
 @RunWith(value = Parameterized.class)
 public class OrganizationRetrieveIT extends AasTestBase {
 
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
     public OrganizationRetrieveIT(ContentType contentType) throws Exception {
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
      * Test creating an organization with parent element and orgId
      */
     @Test
     public void testRetrieveSubOrganization() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.doTestCreateOrganizationWithParent(Authentication.ADMIN_USER_ID,
             Authentication.ADMIN_PASSWORD, "organization_with_parent", parentId, true, contentType, null);
         organizationUtility.retrieveSubOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             parentId, contentType, null);
     }
 
     /**
      * Test creating an organization with parent element and orgId
      */
     @Test
     public void testRetrieveSubOrganizationAsUser() throws Exception {
 
         String[] response =
             personUtility.doTestCreatePersonRetreavingUidPswd(null, null, "person", true, PersonUtility.contentType,
                 null);
         String uid = response[0];
         String password = response[1];
         String parent =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.doTestCreateOrganizationWithParent(uid, password, "organization_with_parent", parentId,
             true, contentType, null);
         organizationUtility.retrieveSubOrganizations(uid, password, parentId, contentType, null);
     }
 
     /**
      * Test retrieving users belonging to an organization
      */
     @Test
     public void testRetrieveOrgPersons() throws Exception {
         List<String> uids = new ArrayList<String>();
 
         //create organization
         String organization =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String orgId = Utility.extract(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
 
         //create users and add to organization
         for (int i = 0; i < 30; i++) {
             String person =
                 personUtility.doTestCreatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, "person",
                     true, PersonUtility.contentType, null);
             String userId = Utility.extract(person, Constants.XPATH_PERSON_ID, PersonUtility.contentType);
             uids.add(userId);
         }
         PrivilegeItem privilegeItem = new PrivilegeItem();
         privilegeItem.setPrivilege(Privilege.ADMIN_USER);
         privilegeItem.setUserIds(uids);
         organizationUtility.createPrivilege(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, orgId,
             privilegeItem, contentType, null);
 
        Thread.sleep(3000);
         //check users of organization
         uids.add(Authentication.ADMIN_USER_ID);
         String searchResult =
             organizationUtility.retrieveUsers(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, orgId,
                 contentType, null);
         List<String> foundIds =
             Utility.extractMany(searchResult, Constants.XPATH_SEARCH_RESULT + Constants.XPATH_PERSON_ID, contentType);
         assertEquals(uids.size(), foundIds.size());
         for (String uid : uids) {
             if (!foundIds.contains(uid)) {
                 fail("user " + uid + " doesnt belong to organization " + orgId);
             }
         }
     }
 
 }
