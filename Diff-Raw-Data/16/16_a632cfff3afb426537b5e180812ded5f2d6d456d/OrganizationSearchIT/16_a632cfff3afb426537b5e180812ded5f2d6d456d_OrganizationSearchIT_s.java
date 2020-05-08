 /**
  * 
  */
 package de.fiz.ddb.aas.test.organization;
 
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
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import de.fiz.ddb.aas.test.AasTestBase;
 import de.fiz.ddb.aas.test.Constants;
 import de.fiz.ddb.aas.test.exceptions.HttpClientException;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 
 /**
  * Test search for Organizations
  * 
  * method read organizations (search)
  * 
  * @author mih
  *
  */
 @RunWith(value = Parameterized.class)
 public class OrganizationSearchIT extends AasTestBase {
 
     private OrganizationUtility organizationUtility;
 
     private ContentType contentType;
 
     private static int methodCounter = 0;
 
     private static List<String> searchIds = new ArrayList<String>();
 
     private static List<String> parentIds = new ArrayList<String>();
 
     private static List<String> childIds = new ArrayList<String>();
 
     private static Map<String, HashMap<String, Object>> replacements;
 
     private static final String idXpath = Constants.XPATH_SEARCH_RESULT + Constants.XPATH_ORGANIZATION_ID;
 
     private static final String countXpath = Constants.XPATH_SEARCH_COUNT;
 
     private static final String totalHitsXpath = Constants.XPATH_SEARCH_TOTAL_HITS;
 
     private static final String searchTestField = Constants.XPATH_ORGANIZATION_SECTOR.replaceFirst(".*/", "");
 
     private static final String searchTestFieldValue = "Search";
 
     /**
      * Constructor with contentType
      * 
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @throws Exception
      */
     public OrganizationSearchIT(ContentType contentType) throws Exception {
         organizationUtility = new OrganizationUtility();
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
         if (methodCounter == 0) {
             initializeReplacements();
             initSearchOrganizations();
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
             deleteSearchOrganizations();
             searchIds = new ArrayList<String>();
             parentIds = new ArrayList<String>();
             childIds = new ArrayList<String>();
             replacements = new LinkedHashMap<String, HashMap<String, Object>>();
         }
     }
 
     /**
      * Test retrieving all Organizations
      */
     @Test
     public void testRetrieveAllOrganizations() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         assertEquals(Utility.extractMany(resource, idXpath, contentType).size(), Integer.parseInt(Utility.extract(
             resource, countXpath, contentType)));
         assertEquals("search hit count not as expected", replacements.size() * 2, Utility.extractMany(resource,
             idXpath, contentType).size());
     }
 
     /**
      * Test retrieving Organizations with field-search
      */
     @Test
     public void testRetrieveOrganizationsWithReplacements() throws Exception {
         for (Entry<String, HashMap<String, Object>> replacementEntry : replacements.entrySet()) {
             Map<String, String[]> parameters = new HashMap<String, String[]>();
             parameters.put(searchTestField, new String[] { searchTestFieldValue });
             parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
             parameters.put(replacementEntry.getKey().replaceFirst(".*/", ""), new String[] { (String) replacementEntry
                 .getValue().get("value") });
             String resource =
                 organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                     contentType, parameters, null);
             List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
            List<String> expectedIds = (List<String>) replacementEntry.getValue().get("ids");
             assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
             assertEquals("search hit count not as expected for query "
                 + replacementEntry.getKey().replaceFirst(".*/", ""), expectedIds.size(), foundIds.size());
             assertEquals("lists are not equals", expectedIds, foundIds);
         }
     }
 
     /**
      * Test retrieving Organizations with special characters displayName
      */
     @Test
     public void testRetrieveOrganizationsWithSpecialCharactersDisplayName() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("displayName", new String[] { "汉语/漢語" });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for special character query displayName ", new Integer(
             replacements.size() * 2 - 2).intValue(), foundIds.size());
 
     }
 
     /**
      * Test retrieving Organizations with special characters street
      */
     @Test
     public void testRetrieveOrganizationsWithSpecialCharactersStreet() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("street", new String[] { "مَفعولين" });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for special character query street ", new Integer(replacements
             .size() * 2 - 2).intValue(), foundIds.size());
 
     }
 
     /**
      * Test retrieving Organizations with special characters abbreviation
      */
     @Test
     public void testRetrieveOrganizationsWithSpecialCharactersAbbreviation() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("abbreviation", new String[] { "äöü" });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for special character query abbreviation ", new Integer(
             replacements.size() * 2 - 2).intValue(), foundIds.size());
 
     }
 
     /**
      * Test retrieving Organizations with special characters description
      */
     @Test
     public void testRetrieveOrganizationsWithSpecialCharactersDescription() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("description", new String[] { "Вилка лежит на столе" });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for special character query description ", new Integer(
             replacements.size() * 2 - 2).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Organizations with paging
      */
     @Test
     public void testRetrieveOrganizationsWithPaging() throws Exception {
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
      * Test retrieving Organizations with several fields search
      */
     @Test
     public void testRetrieveOrganizationsWith2Descriptions() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("description", new String[] { "Вилка лежит на столе",
             (String) replacements.get(Constants.XPATH_ORGANIZATION_DESCRIPTION).get("value") });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 descriptions ", new Integer(
             replacements.size() * 2).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Organizations with several fields search
      */
     @Test
     public void testRetrieveOrganizationsWith2DescriptionsAnd1DisplayName() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("description", new String[] { "Вилка лежит на столе",
             (String) replacements.get(Constants.XPATH_ORGANIZATION_DESCRIPTION).get("value") });
         parameters.put("displayName", new String[] { (String) replacements.get(
             Constants.XPATH_ORGANIZATION_DISPLAY_NAME).get("value") });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
        List<String> expectedIds =
            (List<String>) replacements.get(Constants.XPATH_ORGANIZATION_DISPLAY_NAME).get("ids");
         assertEquals("lists are not equals", expectedIds, foundIds);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 descriptions and one displayName", 2, foundIds
             .size());
     }
 
     /**
      * Test retrieving Organizations with several fields search
      */
     @Test
     public void testRetrieveOrganizationsWith2DescriptionsAnd1DisplayName1() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("description", new String[] { "Вилка лежит на столе",
             (String) replacements.get(Constants.XPATH_ORGANIZATION_DESCRIPTION).get("value") });
         parameters.put("displayName", new String[] { "汉语/漢語" });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 descriptions and one displayName", new Integer(
             replacements.size() * 2 - 2).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Organizations with several fields search
      */
     @Test
     public void testRetrieveOrganizationsWith2DescriptionsAnd2DisplayNames() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         parameters.put("description", new String[] { "Вилка лежит на столе",
             (String) replacements.get(Constants.XPATH_ORGANIZATION_DESCRIPTION).get("value") });
         parameters.put("displayName", new String[] { "汉语/漢語",
             (String) replacements.get(Constants.XPATH_ORGANIZATION_DISPLAY_NAME).get("value") });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for 2 descriptions and one displayName", new Integer(
             replacements.size() * 2).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Organizations with q
      */
     @Test
     public void testRetrieveOrganizationsWithQ() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("q", new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for q=" + searchTestFieldValue, new Integer(
             replacements.size() * 2).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Organizations with q
      */
     @Test
     public void testRetrieveOrganizationsWithQAndDefault() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("q", new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for q=" + searchTestFieldValue, new Integer(
             replacements.size() * 2).intValue(), foundIds.size());
     }
 
     /**
      * Test retrieving Organizations with q
      */
     @Test
     public void testRetrieveOrganizationsWithQAndDescription() throws Exception {
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("q", new String[] { searchTestFieldValue });
         parameters.put("description", new String[] { "Вилка лежит на столе" });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         List<String> foundIds = Utility.extractMany(resource, idXpath, contentType);
         assertEquals(foundIds.size(), Integer.parseInt(Utility.extract(resource, countXpath, contentType)));
         assertEquals("search hit count not as expected for search for q=" + searchTestFieldValue + " and Description",
             new Integer(replacements.size() * 2 - 2).intValue(), foundIds.size());
     }
 
     /**
      * Create Organizations for Search
      */
     private void initSearchOrganizations() throws Exception {
         for (Entry<String, HashMap<String, Object>> replacementEntry : replacements.entrySet()) {
             String organization =
                 organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "search/organization_search1",
                     true, contentType);
             organization =
                 Utility.replace(organization, replacementEntry.getKey(), (String) replacementEntry.getValue().get(
                     "value"), contentType);
             String resource =
                 organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                     organization, contentType, null);
             String parentId = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
             parentIds.add(parentId);
 
             organization =
                 organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "search/organization_search2",
                     true, contentType);
             organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_PARENT, parentId, contentType);
             organization =
                 Utility.replace(organization, replacementEntry.getKey(), (String) replacementEntry.getValue().get(
                     "value"), contentType);
             resource =
                 organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                     organization, contentType, null);
             String childId = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
             childIds.add(childId);
 
             Map<String, String[]> parameters = new HashMap<String, String[]>();
             parameters.put(searchTestField, new String[] { searchTestFieldValue });
             parameters.put(replacementEntry.getKey().replaceFirst(".*/", ""), new String[] { (String) replacementEntry
                 .getValue().get("value") });
             resource =
                 organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                     contentType, parameters, null);
             replacements.get(replacementEntry.getKey()).put("ids", Utility.extractMany(resource, idXpath, contentType));
         }
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(searchTestField, new String[] { searchTestFieldValue });
         parameters.put("count", new String[] { new Integer(replacements.size() * 2 + 10).toString() });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, null);
         searchIds = Utility.extractMany(resource, idXpath, contentType);
     }
 
     /**
      * Delete Organizations after tests
      */
     private void deleteSearchOrganizations() throws Exception {
         for (String childId : childIds) {
             organizationUtility.deleteOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 childId, contentType, null);
         }
         for (String parentId : parentIds) {
             organizationUtility.deleteOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 parentId, contentType, null);
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
         parameters.put("description", new String[] { "*" });
         String resource =
             organizationUtility.retrieveOrganizations(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 contentType, parameters, expectedExceptionClass);
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
             assertEquals("totalHits are not equals", new Integer(replacements.size() * 2).toString(), Utility.extract(
                 resource, totalHitsXpath, contentType));
         }
     }
 
     /**
      *  initialize Replacements
      *  
      *  TODO: Search for fax does not work. LDAP-schema definition doesnt allow search.
      */
     private void initializeReplacements() throws Exception {
         replacements = new LinkedHashMap<String, HashMap<String, Object>>();
         replacements.put(Constants.XPATH_ORGANIZATION_DISPLAY_NAME, new HashMap<String, Object>() {
             private static final long serialVersionUID = 5765348590529520504L;
 
             {
                 put("value", "searchDisplayName");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_ABBREVIATION, new HashMap<String, Object>() {
             private static final long serialVersionUID = 8869524730121322235L;
 
             {
                 put("value", "searchAbbreviation");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_DESCRIPTION, new HashMap<String, Object>() {
             private static final long serialVersionUID = 2635106100335936428L;
 
             {
                 put("value", "searchDescription");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_LEGAL_STATUS, new HashMap<String, Object>() {
             private static final long serialVersionUID = 744947597775032946L;
 
             {
                 put("value", "searchLegalStatus");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_STREET, new HashMap<String, Object>() {
             private static final long serialVersionUID = 4459421315417432000L;
 
             {
                 put("value", "searchStreet");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_HOUSE_IDENTIFIER, new HashMap<String, Object>() {
             private static final long serialVersionUID = -955612892328375444L;
 
             {
                 put("value", "searchHouseIdentifier");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_ADDRESS_SUPLEMENT, new HashMap<String, Object>() {
             private static final long serialVersionUID = -8656232926368077311L;
 
             {
                 put("value", "searchAddressSupplement");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_POSTAL_CODE, new HashMap<String, Object>() {
             private static final long serialVersionUID = -6548563784124726017L;
 
             {
                 put("value", "searchPostalCode");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_CITY, new HashMap<String, Object>() {
             private static final long serialVersionUID = -6979316576738938859L;
 
             {
                 put("value", "searchCity");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_COUNTRY, new HashMap<String, Object>() {
             private static final long serialVersionUID = -2791269762282384260L;
 
             {
                 put("value", "fr");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_LATITUDE, new HashMap<String, Object>() {
             private static final long serialVersionUID = -5017597331829567357L;
 
             {
                 put("value", "12.123");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_LONGITUDE, new HashMap<String, Object>() {
             private static final long serialVersionUID = 5963421460367927328L;
 
             {
                 put("value", "23.234");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_PHONE, new HashMap<String, Object>() {
             private static final long serialVersionUID = 898367891218381105L;
 
             {
                 put("value", "searchTelephone");
             }
         });
         //        replacements.put(Constants.XPATH_ORGANIZATION_FAX, new HashMap<String, Object>() {
         //            {
         //                put("value", "searchFax");
         //            }
         //        });
         replacements.put(Constants.XPATH_ORGANIZATION_EMAIL, new HashMap<String, Object>() {
             private static final long serialVersionUID = 3400039368200581946L;
 
             {
                 put("value", "searchEmail");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_URL, new HashMap<String, Object>() {
             private static final long serialVersionUID = -5085410745678882387L;
 
             {
                 put("value", "searchUrl");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_LOGO, new HashMap<String, Object>() {
             private static final long serialVersionUID = -6212155812086867240L;
 
             {
                 put("value", "searchLogo");
             }
         });
         replacements.put(Constants.XPATH_ORGANIZATION_STATUS, new HashMap<String, Object>() {
             private static final long serialVersionUID = -8185447817338675888L;
 
             {
                 put("value", "searchStatus");
             }
         });
     }
 }
