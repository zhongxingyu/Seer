 package de.fiz.ddb.aas.test.person;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.entity.ContentType;
 import org.junit.Test;
 
 import de.fiz.ddb.aas.test.AasTestBase;
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.organization.OrganizationUtility;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 import de.fiz.ddb.aas.test.util.privilege.Privilege;
 import de.fiz.ddb.aas.test.util.privilege.PrivilegeItem;
 
 /**
  * Tests for person-resource
  * Run with json representations.
  * 
  * @author mih
  *
  */
 public class PersonAllIT extends AasTestBase {
 
     private PersonUtility personUtility;
 
     private OrganizationUtility organizationUtility;
 
     private ContentType contentType;
 
     /**
      * Constructor with contentType
      * 
      * @throws Exception
      */
     public PersonAllIT() throws Exception {
         personUtility = new PersonUtility();
         organizationUtility = new OrganizationUtility();
         this.contentType = ContentType.APPLICATION_JSON;
     }
 
     /**
      * Test deleting a person.
      */
     @Test
     public void testDeletePerson() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         resource =
             personUtility.deletePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id, contentType,
                 null);
     }
 
     /**
      * Test retrieving a person.
      */
     @Test
     public void testRetrievePerson() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         resource =
             personUtility.retrievePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id, contentType,
                 null);
     }
 
     /**
      * Test retrieving a person by email.
      */
     @Test
     public void testRetrievePersonByEmail() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String eMail = Utility.extract(resource, Constants.XPATH_PERSON_EMAIL, contentType);
         resource =
             personUtility.retrievePersonByEmail(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, eMail,
                 contentType, null);
     }
 
     /**
      * Test retrieving a person self by email.
      */
     @Test
     public void testRetrievePersonSelfByEmail() throws Exception {
         String password = "myPassword";
         String person = personUtility.getTemplate(PersonUtility.getTemplatepath(), "person", contentType);
         person = Utility.replace(person, Constants.XPATH_PERSON_ID, "id" + System.currentTimeMillis(), contentType);
         person = Utility.replace(person, Constants.XPATH_PERSON_PASSWORD, password, contentType);
         String resource = personUtility.doTestCreatePersonWithObject(null, null, person, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         String eMail = Utility.extract(resource, Constants.XPATH_PERSON_EMAIL, contentType);
         resource = personUtility.retrievePersonByEmail(id, password, eMail, contentType, null);
     }
 
     /**
      * Test retrieving persons privileges.
      */
     @Test
     public void testRetrievePersonPrivileges() throws Exception {
         String user = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String organization =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_with_parent", true, contentType, null);
         String orgId = Utility.extract(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         String id = Utility.extract(user, Constants.XPATH_PERSON_ID, contentType);
         PrivilegeItem privilege = new PrivilegeItem();
         privilege.setPrivilege(Privilege.ADMIN_ORG);
         List<String> userIds = new ArrayList<String>();
         userIds.add(id);
         privilege.setUserIds(userIds);
         organizationUtility.updatePrivilege(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, orgId,
             Privilege.ADMIN_ORG.toString(), privilege, contentType, null);
         user =
             personUtility.retrievePrivileges(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id,
                 contentType, null);
     }
 
     /**
      * Test changing a persons password.
      */
     @Test
     public void testChangePassword() throws Exception {
         String resource = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         resource =
             personUtility.changePassword(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id,
                 "testPassword1", contentType, null);
     }
 
     /**
      * Test retrieving organizations a person belongs to.
      */
     @Test
     public void testRetrievePersonsOrganizations() throws Exception {
         List<String> orgIds = new ArrayList<String>();
         String user = personUtility.doTestCreatePerson(null, null, "person", true, contentType, null);
         String userId = Utility.extract(user, Constants.XPATH_PERSON_ID, contentType);
         for (int i = 0; i < 30; i++) {
             String organization =
                 organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID,
                     Authentication.ADMIN_PASSWORD, "organization_with_parent", true, contentType, null);
             String orgId = Utility.extract(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
             orgIds.add(orgId);
             PrivilegeItem privilege = new PrivilegeItem();
             privilege.setPrivilege(Privilege.FTP_UPLOAD);
             List<String> userIds = new ArrayList<String>();
             userIds.add(userId);
             privilege.setUserIds(userIds);
             organizationUtility.createPrivilege(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, orgId,
                 privilege, contentType, null);
         }
 
        Thread.sleep(10000);
         String searchResult =
             personUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, userId,
                 contentType, null);
         List<String> foundIds =
             Utility.extractMany(searchResult, Constants.XPATH_SEARCH_RESULT + Constants.XPATH_ORGANIZATION_ID,
                 contentType);
         assertEquals(orgIds.size(), foundIds.size());
         for (String orgId : orgIds) {
             if (!foundIds.contains(orgId)) {
                 fail("user " + userId + " doesnt belong to organization " + orgId);
             }
         }
     }
 
 }
