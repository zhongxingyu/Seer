 package de.fiz.ddb.aas.test.organization.aa;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.entity.ContentType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.fiz.ddb.aas.test.AasTestBase;
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.exceptions.HttpClientException;
 import de.fiz.ddb.aas.test.organization.OrganizationUtility;
 import de.fiz.ddb.aas.test.person.PersonUtility;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.privilege.Privilege;
 import de.fiz.ddb.aas.test.util.privilege.PrivilegeItem;
 
 /**
  * Tests for person authorizations 
  * Run with xml-representations and json representations.
  * 
  * @author ttr
  *
  */
 public class PersonAaIT extends AasTestBase {
 
     private OrganizationUtility organizationUtility;
 
     private PersonUtility personUtility;
 
     private ContentType contentType;
 
     private static String uid = null;
 
     private static String password = null;
 
     private static int methodCounter = 0;
 
     public PersonAaIT() throws Exception {
         organizationUtility = new OrganizationUtility();
         personUtility = new PersonUtility();
         this.contentType = ContentType.APPLICATION_JSON;
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
      * Test creating a person as another user.
      */
     @Test
     public void testCreatePersonId() throws Exception {
         personUtility.doTestCreatePerson(uid, password, "person", true, contentType, null);
     }
 
     /**
      * Test updating a person as another user.
      */
     @Test
     public void testUpdatePerson() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         resource =
             Utility.replace(resource, Constants.XPATH_PERSON_EMAIL, System.currentTimeMillis() + "@fiz-karlsruhe.de",
                 contentType);
         resource =
             personUtility.doTestUpdatePerson(uid, password, resource, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test deleting a person as another user.
      */
     @Test
     public void testDeletePerson() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         resource = personUtility.deletePerson(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test retrieving a person as another user.
      */
     @Test
     public void testRetrievePerson() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         resource = personUtility.retrievePerson(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test retrieving persons privileges.
      */
     @Test
     public void testRetrievePersonPrivileges() throws Exception {
        String user = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String organization =
             organizationUtility.doTestCreateOrganization(uid, password, "organization_with_parent", true, contentType,
                 null);
         String orgId = Utility.extract(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         String id = Utility.extract(user, Constants.XPATH_PERSON_ID, contentType);
         PrivilegeItem privilege = new PrivilegeItem();
         privilege.setPrivilege(Privilege.ADMIN_ORG);
         List<String> userIds = new ArrayList<String>();
         userIds.add(id);
         privilege.setUserIds(userIds);
        organizationUtility.updatePrivilege(uid, password, orgId, Privilege.ADMIN_ORG.toString(), privilege,
             contentType, HttpClientException.class);
        user = personUtility.retrievePrivileges(uid, password, id, contentType, HttpClientException.class);
     }
 
     /**
      * Test changing a persons password as another user.
      */
     @Test
     public void testChangePassword() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         resource =
             personUtility.changePassword(uid, password, id, "testPassword2", contentType, HttpClientException.class);
     }
 
     /**
      * Test retrieving persons as another user.
      */
     @Test
     public void testRetrievePersons() throws Exception {
         personUtility.retrievePersons(uid, password, contentType, null, HttpClientException.class);
     }
 }
