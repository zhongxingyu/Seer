 /**
  * 
  */
 package de.fiz.ddb.aas.test.person;
 
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.apache.http.entity.ContentType;
 
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.client.PersonClient;
 import de.fiz.ddb.aas.test.util.ObjectType;
 import de.fiz.ddb.aas.test.util.ResourceUtility;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 
 /**
  * Utility class for person-resource.
  * 
  * @author mih
  * 
  */
 public class PersonUtility extends ResourceUtility {
 
     private PersonClient client;
 
     public static final String templatePath = "persons";
 
     public static final ContentType contentType = ContentType.APPLICATION_JSON;
 
     public PersonUtility() {
         client = new PersonClient();
     }
 
     /**
      * try creating a person.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param person
      *            person as json
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String createPerson(
        final String uid, final String password, final String person, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.create(person, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try updating a person.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param person
      *            person as String (json)
      * @param personId
      *            id of the person
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String updatePerson(
         final String uid, final String password, final String person, final String personId, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.update(personId, person, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try deleting a person.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param userId
      *            userId
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String deletePerson(
         final String uid, final String password, final String userId, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.delete(userId, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try retrieving a person.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param userId
      *            userId
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String retrievePerson(
         final String uid, final String password, final String userId, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.retrieve(userId, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try retrieving a person by eMail.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param email
      *            eMail
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String retrievePersonByEmail(
         final String uid, final String password, final String email, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.retrieveByEmail(email, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try retrieving privileges of a person.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param userId
      *            userId
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String privileges json
      * @throws Exception
      *             e
      * 
      */
     public String retrievePrivileges(
         final String uid, final String password, final String userId, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.retrievePrivileges(userId, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try changing a persons password.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param userId
      *            userId
      * @param newPassword
      *            newPassword
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String changePassword(
         final String uid, final String password, final String userId, final String newPassword,
         ContentType contentType, Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String passwordFile = getTemplate(templatePath, "password", contentType);
             passwordFile = Utility.replace(passwordFile, Constants.XPATH_PASSWORD_PSWD, newPassword, contentType);
             String response = client.changePassword(userId, passwordFile, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try retrieving a persons organizations.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param userId
      *            userId
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String org search-result json
      * @throws Exception
      *             e
      * 
      */
     public String retrieveOrganizations(
         final String uid, final String password, final String userId, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.retrieveOrganizations(userId, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try retrieving persons.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param parameters
      *            search-parameters
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person search-result json
      * @throws Exception
      *             e
      * 
      */
     public String retrievePersons(
         final String uid, final String password, ContentType contentType, final Map<String, String[]> parameters,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.retrieveUsers(contentType, parameters);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try creating a person.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param personTemplate
      *            name of template without extension
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String doTestCreatePerson(
         final String uid, final String password, final String personTemplate, boolean createId,
         ContentType contentType, Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String user = getTemplate(templatePath, personTemplate, createId, contentType);
             String response = client.create(user, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             String id = Utility.extract(response, Constants.XPATH_PERSON_ID, contentType);
             response =
                 retrievePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id, contentType,
                     expectedExceptionClass);
             compareValues(user, response, contentType, ObjectType.PERSON);
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try creating a person.
      * givejson string as parameter
      * After create, retrieve resource and compare with given person.
      * If expected to return exception, check thrown exception against expected exception.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param person
      *            person as json
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String doTestCreatePersonWithObject(
         final String uid, final String password, final String person, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.create(person, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             String id = Utility.extract(response, Constants.XPATH_PERSON_ID, contentType);
             response =
                 retrievePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id, contentType,
                     expectedExceptionClass);
             compareValues(person, response, contentType, ObjectType.PERSON);
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try creating a person retrieving uid and pswd
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param personTemplate
      *            name of template without extension
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String[] {id, pswd} 
      * @throws Exception
      *             e
      * 
      */
     public String[] doTestCreatePersonRetreavingUidPswd(
         final String uid, final String pswd, final String personTemplate, boolean createId, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, pswd);
             String user = getTemplate(templatePath, personTemplate, createId, contentType);
             String resource = client.create(user, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
             String password = Utility.extract(resource, Constants.XPATH_PERSON_PASSWORD, contentType);
             String[] response = new String[2];
             response[0] = id;
             response[1] = password;
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * try updating a person.
      * 
      * @param uid
      *            userid
      * @param password
      *            password
      * @param person
      *            person as String (json)
      * @param personId
      *            id of the person
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON
      * @param expectedExceptionClass
      *            expectedException
      * 
      * @return String person json
      * @throws Exception
      *             e
      * 
      */
     public String doTestUpdatePerson(
         final String uid, final String password, final String person, final String personId, ContentType contentType,
         Class<?> expectedExceptionClass) throws Exception {
         try {
             Authentication.setCredentials(uid, password);
             String response = client.update(personId, person, contentType);
             if (expectedExceptionClass != null) {
                 fail("No Exception was thrown. expected: " + expectedExceptionClass.getName());
             }
             String id = Utility.extract(response, Constants.XPATH_PERSON_ID, contentType);
             response =
                 retrievePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, id, contentType,
                     expectedExceptionClass);
             compareValues(person, response, contentType, ObjectType.PERSON);
             return response;
         }
         catch (Exception e) {
             assertExceptionType(expectedExceptionClass, e);
         }
         finally {
             Authentication.setEmpty();
         }
         return null;
     }
 
     /**
      * Test updating a person with xml as this person
      */
     public void testEmptyAttribute(
         final boolean doUpdate, final String attributeXpath, final ContentType contentType,
         final Class<?> expectedExceptionClass) throws Exception {
         String person = getTemplate(getTemplatepath(), "person", true, contentType);
         String resource;
         if (!doUpdate) {
             person = Utility.removeValue(person, attributeXpath, contentType);
             resource = doTestCreatePersonWithObject(null, null, person, contentType, expectedExceptionClass);
         }
         else {
             resource = doTestCreatePersonWithObject(null, null, person, contentType, null);
             String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
             resource = Utility.removeValue(resource, attributeXpath, contentType);
             doTestUpdatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, resource, id, contentType,
                 expectedExceptionClass);
         }
     }
 
     /**
      * Test updating a person with xml as this person
      */
     public void testMissingAttribute(
         final boolean doUpdate, final String attributeXpath, final ContentType contentType,
         final Class<?> expectedExceptionClass) throws Exception {
         String person = getTemplate(getTemplatepath(), "person", true, contentType);
         String resource;
         if (!doUpdate) {
             person = Utility.remove(person, attributeXpath, contentType);
             resource = doTestCreatePersonWithObject(null, null, person, contentType, expectedExceptionClass);
         }
         else {
             resource = doTestCreatePersonWithObject(null, null, person, contentType, null);
             String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
             resource = Utility.remove(resource, attributeXpath, contentType);
             doTestUpdatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, resource, id, contentType,
                 expectedExceptionClass);
         }
     }
 
     /**
      * Test creating a person with an empty particular attribute.
      * Then update the person with the attribute provided.
      * If expected to return exception, check thrown exception against expected exception.
      * 
      * @param attributeXpath xPath to attribute to clear
      * @param attributeValue value of attribute for update
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @param expectedExceptionClass expected Exception
      * @throws Exception
      */
     public void testAdditionalAttribute(
         final String attributeXpath, final String attributeValue, final ContentType contentType,
         final Class<?> expectedExceptionClass) throws Exception {
         String person = getTemplate(getTemplatepath(), "person", true, contentType);
         String resource;
         person = Utility.removeValue(person, attributeXpath, contentType);
 
         //create with removed value
         resource =
             doTestCreatePersonWithObject(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, person,
                 contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_PERSON_ID, contentType);
         person = getTemplate(getTemplatepath(), "person", false, contentType);
         person = Utility.replace(person, Constants.XPATH_PERSON_ID, id, contentType);
         person = Utility.replace(person, attributeXpath, attributeValue, contentType);
 
         //Update with value
         doTestUpdatePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, person, id, contentType,
             expectedExceptionClass);
     }
 
     /**
      * @return the templatepath
      */
     public static String getTemplatepath() {
         return templatePath;
     }
 
 }
