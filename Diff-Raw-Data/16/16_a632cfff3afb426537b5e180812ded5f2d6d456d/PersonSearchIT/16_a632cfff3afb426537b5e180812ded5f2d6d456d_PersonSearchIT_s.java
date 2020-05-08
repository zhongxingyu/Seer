 /**
  * 
  */
 package de.fiz.ddb.aas.test.person;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.entity.ContentType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.fiz.ddb.aas.test.AasTestBase;
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.exceptions.HttpClientException;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 
 /**
  * Test search for Persons
  * 
  * tests method:
  * read persons (search)
  * 
  * @author mih
  *
  */
 public class PersonSearchIT extends AasTestBase {
 
     private PersonUtility personUtility;
 
     private ContentType contentType;
 
     private static int methodCounter = 0;
 
     private static List<String> searchIds = new ArrayList<String>();
 
     private static Map<String, HashMap<String, Object>> replacements;
 
     private static final String idXpath = Constants.XPATH_SEARCH_RESULT + Constants.XPATH_PERSON_ID;
 
     private static final String countXpath = Constants.XPATH_SEARCH_COUNT;
 
     private static final String totalHitsXpath = Constants.XPATH_SEARCH_TOTAL_HITS;
 
     private static final String searchTestField = Constants.XPATH_PERSON_SALUTATION.replaceFirst(".*/", "");
 
     private static final String searchTestFieldValue = "Search";
 
     /**
      * Constructor
      * 
      * @throws Exception
      */
     public PersonSearchIT() throws Exception {
         personUtility = new PersonUtility();
         this.contentType = ContentType.APPLICATION_JSON;
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
         if (methodCounter == 0) {
             initializeReplacements();
             initSearchPersons();
         }
     }
 
     /**
      * Clean up after test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
         methodCounter++;
         if (methodCounter == getTestAnnotationsCount()) {
             methodCounter = 0;
             deleteSearchPersons();
             searchIds = new ArrayList<String>();
             replacements = new LinkedHashMap<String, HashMap<String, Object>>();
         }
     }
 
     /**
      * Test retrieving all Persons
      */
     @Test
     public void testRetrieveAllPersons() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() + 10).toString() });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         assertEquals(Utility.extractMany(resource, idXpath, contentType).size(), Integer.parseInt(Utility.extract(
             resource, countXpath, contentType)));
         assertEquals("search hit count not as expected", replacements.size(), Utility.extractMany(resource, idXpath,
             contentType).size());
     }
 
     /**
      * Test retrieving Persons with field-search
      */
     @Test
     public void testRetrievePersonsWithReplacements() throws Exception {
         for (Entry<String, HashMap<String, Object>> replacementEntry : replacements.entrySet()) {
             Map<String, String[]> parameters = new HashMap<String, String[]>();
             parameters.put(searchTestField, new String[] { searchTestFieldValue });
             parameters.put("count", new String[] { new Integer(replacements.size() + 10).toString() });
             parameters.put(replacementEntry.getKey().replaceFirst(".*/", ""), new String[] { (String) replacementEntry
                 .getValue().get("value") });
             String resource =
                 personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                     parameters, null);
             List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
            List<String> expectedIds = (List<String>) replacementEntry.getValue().get("ids");
             assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
             assertEquals("search hit count not as expected for query "
                 + replacementEntry.getKey().replaceFirst(".*/", ""), expectedIds.size(), foundIds.size());
             assertEquals("lists are not equals", expectedIds, foundIds);
         }
     }
 
     /**
      * Test retrieving Persons with special characters title
      */
     @Test
     public void testRetrievePersonsWithSpecialCharactersTitle() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() + 10).toString() });
         parameters.put("title", new String[] { "مَفعولين" });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for special character query street ", new Integer(replacements
             .size() - 1).intValue(), foundIds.size());
 
     }
 
     /**
      * Test retrieving Persons with special characters surname
      */
     @Test
     public void testRetrievePersonsWithSpecialCharactersSurname() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("surName", new String[] { "汉语/漢語" });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for special character query street ", new Integer(replacements
             .size() - 1).intValue(), foundIds.size());
 
     }
 
     /**
      * Test retrieving Persons with special characters forename
      */
     @Test
     public void testRetrievePersonsWithSpecialCharactersForename() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("foreName", new String[] { "Вилка лежит на столе" });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for special character query street ", new Integer(replacements
             .size() - 1).intValue(), foundIds.size());
 
     }
 
     /**
      * Test retrieving Persons with paging
      */
     @Test
     public void testRetrievePersonsWithPaging() throws Exception {
         searchWithPaging(3, 4, null);
         searchWithPaging(1, 0, HttpClientException.class);
         searchWithPaging(12, 0, HttpClientException.class);
         searchWithPaging(300, 4, null);
         searchWithPaging(4, 400, null);
         searchWithPaging(0, 1, HttpClientException.class);
         searchWithPaging(-2, 4, HttpClientException.class);
         searchWithPaging(4, -2, HttpClientException.class);
         searchWithPaging(0, 0, HttpClientException.class);
         searchWithPaging(-1, -2, HttpClientException.class);
     }
 
     /**
      * Test retrieving Persons with several fields search
      */
     @Test
     public void testRetrievePersonsWith2Surnames() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("surName", new String[] { "汉语/漢語",
             (String) replacements.get(Constants.XPATH_PERSON_SURNAME).get("value") });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 surnames ", new Integer(replacements.size())
             .intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Persons with several fields search
      */
     @Test
     public void testRetrievePersonsWith2SurnamesAnd1Forename() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("surName", new String[] { "汉语/漢語",
             (String) replacements.get(Constants.XPATH_PERSON_SURNAME).get("value") });
         parameters.put("foreName", new String[] { (String) replacements.get(Constants.XPATH_PERSON_GIVENNAME).get(
             "value") });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
        List<String> expectedIds = (List<String>) replacements.get(Constants.XPATH_PERSON_GIVENNAME).get("ids");
         assertEquals("lists are not equals", expectedIds, foundIds);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 surnames and one forename", 1, foundIds.size());
     }
 
     /**
      * Test retrieving Persons with several fields search
      */
     @Test
     public void testRetrievePersonsWith2SurnamesAnd1Forename1() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("surName", new String[] { "汉语/漢語",
             (String) replacements.get(Constants.XPATH_PERSON_SURNAME).get("value") });
         parameters.put("foreName", new String[] { "Вилка лежит на столе" });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 surnames and one forename", new Integer(
             replacements.size() - 1).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Persons with several fields search
      */
     @Test
     public void testRetrievePersonsWith2SurnamesAnd2Forenames() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("surName", new String[] { "汉语/漢語",
             (String) replacements.get(Constants.XPATH_PERSON_SURNAME).get("value") });
         parameters.put("foreName", new String[] { "Вилка лежит на столе",
             (String) replacements.get(Constants.XPATH_PERSON_GIVENNAME).get("value") });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 surnames and one forename", new Integer(
             replacements.size()).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Persons with q
      */
     @Test
     public void testRetrievePersonsWithQ() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("q", new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for q=" + searchTestFieldValue, new Integer(
             replacements.size()).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Persons with q
      */
     @Test
     public void testRetrievePersonsWithQAndDefault() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("q", new String[] { searchTestFieldValue });
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for q=" + searchTestFieldValue, new Integer(
             replacements.size()).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Persons with q
      */
     @Test
     public void testRetrievePersonsWithQAndSurname() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("q", new String[] { searchTestFieldValue });
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("surName", new String[] { "汉语/漢語" });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for q=" + searchTestFieldValue + " and surname",
             new Integer(replacements.size() - 1).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Persons with q
      */
     @Test
     public void testRetrievePersonsWithQAndSurname1() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("q", new String[] { searchTestFieldValue });
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("surName", new String[] { "汉语/漢語",
             (String) replacements.get(Constants.XPATH_PERSON_SURNAME).get("value") });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for q=" + searchTestFieldValue + " and surname",
             new Integer(replacements.size()).intValue(), foundIds.size());
     }
 
     /**
      * Create Persons for Search
      */
     private void initSearchPersons() throws Exception {
         for (Entry<String, HashMap<String, Object>> replacementEntry : replacements.entrySet()) {
             String person =
                 personUtility.getTemplate(PersonUtility.getTemplatepath(), "search/person_search", true, contentType);
             person =
                 Utility.replace(person, replacementEntry.getKey(), (String) replacementEntry.getValue().get("value"),
                     contentType);
             String resource =
                 personUtility.doTestCreatePersonWithObject(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                     person, contentType, null);
 
             Map<String, String[]> parameters = new HashMap<String, String[]>();
             parameters.put(searchTestField, new String[] { searchTestFieldValue });
             parameters.put(replacementEntry.getKey().replaceFirst(".*/", ""), new String[] { (String) replacementEntry
                 .getValue().get("value") });
             resource =
                 personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                     parameters, null);
             replacements.get(replacementEntry.getKey()).put("ids", Utility.extractMany(resource, idXpath, contentType));
         }
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, null);
         searchIds = Utility.extractMany(resource, idXpath, contentType);
     }
 
     /**
      * Delete Persons after tests
      */
     private void deleteSearchPersons() throws Exception {
         for (String searchId : searchIds) {
             if (!searchId.equals(Authentication.ADMIN_USER_ID)) {
                 personUtility.deletePerson(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, searchId,
                     contentType, null);
             }
         }
     }
 
     /**
      * Search with paging
      */
     private void searchWithPaging(int start, int count, Class<?> expectedExceptionClass) throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(count).toString() });
         parameters.put("start", new String[] { new Integer(start).toString() });
         parameters.put("email", new String[] { "*" });
         String resource =
             personUtility.retrievePersons(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD, contentType,
                 parameters, expectedExceptionClass);
         if (expectedExceptionClass == null) {
             List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
             List<String> expectedIds = new ArrayList<String>();
             int i = 1;
             for (String id : searchIds) {
                 if (i >= start && i < count + start) {
                     expectedIds.add(id);
                 }
                 i++;
             }
             assertEquals("lists are not equals", expectedIds, foundIds);
             assertEquals("totalHits are not equals", new Integer(replacements.size()).toString(), Utility.extract(
                 resource, totalHitsXpath, contentType));
         }
     }
 
     /**
      *  initialize Replacements
      *  
      */
     private void initializeReplacements() throws Exception {
         replacements = new LinkedHashMap<String, HashMap<String, Object>>();
         replacements.put(Constants.XPATH_PERSON_ID, new HashMap<String, Object>() {
             private static final long serialVersionUID = -6562176075526784763L;
 
             {
                 put("value", "searchId");
             }
         });
         replacements.put(Constants.XPATH_PERSON_TITLE, new HashMap<String, Object>() {
             private static final long serialVersionUID = -7369076848460268396L;
 
             {
                 put("value", "searchTitle");
             }
         });
         replacements.put(Constants.XPATH_PERSON_SURNAME, new HashMap<String, Object>() {
             private static final long serialVersionUID = 7921148851312584175L;
 
             {
                 put("value", "searchSurName");
             }
         });
         replacements.put(Constants.XPATH_PERSON_GIVENNAME, new HashMap<String, Object>() {
             private static final long serialVersionUID = -8546401270589856009L;
 
             {
                 put("value", "searchId");
             }
         });
         replacements.put(Constants.XPATH_PERSON_PHONE, new HashMap<String, Object>() {
             private static final long serialVersionUID = 3920018305419376429L;
 
             {
                 put("value", "searchTelephoneNumber");
             }
         });
         replacements.put(Constants.XPATH_PERSON_EMAIL, new HashMap<String, Object>() {
             private static final long serialVersionUID = -7934441999657060705L;
 
             {
                 put("value", "searchEmail@address.de");
             }
         });
         replacements.put(Constants.XPATH_PERSON_PASSWORD, new HashMap<String, Object>() {
             private static final long serialVersionUID = -1201330993293801763L;
 
             {
                 put("value", "searchPswd");
             }
         });
     }
 }
