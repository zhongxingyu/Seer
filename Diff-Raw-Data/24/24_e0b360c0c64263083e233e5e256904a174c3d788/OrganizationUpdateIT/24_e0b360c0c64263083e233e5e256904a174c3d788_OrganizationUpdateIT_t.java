 package de.fiz.ddb.aas.test.organization;
 
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
import de.fiz.ddb.aas.test.exceptions.HttpServerException;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 
 /**
  * Update Tests for organization-resource
  * Run with xml-representations and json representations.
  * 
  * tests method update organization
  * 
  * NOTE: Creating an Organization with empty sector, status, lat or lon is not possible!
  * As in XML Enumerations or Doubles may not be empty.
  * 
  * @author mih
  *
  */
 @RunWith(value = Parameterized.class)
 public class OrganizationUpdateIT extends AasTestBase {
 
     private OrganizationUtility organizationUtility;
 
     private ContentType contentType;
 
     private static String uid = Authentication.ADMIN_USER_ID;
 
     private static String password = Authentication.ADMIN_PASSWORD;
 
     /**
      * Constructor with contentType
      * 
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @throws Exception
      */
     public OrganizationUpdateIT(ContentType contentType) throws Exception {
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
     }
 
     /**
      * Clean up after test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
     }
 
     /**
      * Test updating an organization
      */
     @Test
     public void testUpdateOrganization() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource =
             Utility.replace(resource, Constants.XPATH_ORGANIZATION_EMAIL, System.currentTimeMillis()
                 + "@fiz-karlsruhe.de", contentType);
         resource = organizationUtility.doTestUpdateOrganization(uid, password, resource, id, contentType, null);
     }
 
     /**
      * Test updating an organization with new attribute
      */
     @Test
     public void testUpdateOrganizationWithNewAttribute() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_with_parent", true,
                 contentType);
         String organizationWithRemovedAtt =
             Utility.remove(organization, Constants.XPATH_ORGANIZATION_STREET, contentType);
         String resource =
             organizationUtility.doTestCreateOrganizationWithObject(uid, password, organizationWithRemovedAtt,
                 contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_STREET, Constants.REPLACEMENT_NEW_VALUE,
                 contentType);
         resource = organizationUtility.doTestUpdateOrganization(uid, password, resource, id, contentType, null);
     }
 
     /**
      * Test updating an organization with special Characters
      */
     @Test
     public void testUpdateOrganizationWithSpecialCharacters() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(uid, password, "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         resource =
             Utility.replace(resource, Constants.XPATH_ORGANIZATION_DISPLAY_NAME, Constants.REPLACEMENT_CHINESE,
                 contentType);
         resource =
             Utility.replace(resource, Constants.XPATH_ORGANIZATION_ABBREVIATION, Constants.REPLACEMENT_GERMAN,
                 contentType);
         resource =
             Utility.replace(resource, Constants.XPATH_ORGANIZATION_DESCRIPTION, Constants.REPLACEMENT_RUSSIAN,
                 contentType);
         resource =
             Utility.replace(resource, Constants.XPATH_ORGANIZATION_STREET, Constants.REPLACEMENT_ARABIC, contentType);
         resource = organizationUtility.doTestUpdateOrganization(uid, password, resource, id, contentType, null);
     }
 
     /**
      * Test updating an organization with empty pid-element
      */
     @Test
     public void testUpdateOrganizationEmptyPid() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_PID, contentType, null);
     }
 
     /**
      * Test updating an organization with empty displayName-element
      * 
      * @expect HttpClientException
      */
     @Test
     public void testUpdateOrganizationEmptyDisplayName() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_DISPLAY_NAME, contentType,
             BadRequest.class);
     }
 
     /**
      * Test updating an organization with empty abbreviation-element
      */
     @Test
     public void testUpdateOrganizationEmptyAbbreviation() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_ABBREVIATION, contentType, null);
     }
 
     /**
      * Test updating an organization with empty description-element
      */
     @Test
     public void testUpdateOrganizationEmptyDescription() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_DESCRIPTION, contentType, null);
     }
 
     /**
      * Test updating an organization with empty legalStatus-element
      */
     @Test
     public void testUpdateOrganizationEmptyLegalStatus() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_LEGAL_STATUS, contentType, null);
     }
 
     /**
      * Test updating an organization with empty sector-element
      */
     @Test
     public void testUpdateOrganizationEmptySector() throws Exception {
         // XML schema doesn't allow an empty string as enumeration value
         organizationUtility
             .testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_SECTOR, contentType, BadRequest.class);
     }
 
     /**
      * Test updating an organization with empty street-element
      */
     @Test
     public void testUpdateOrganizationEmptyStreet() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_STREET, contentType, null);
     }
 
     /**
      * Test updating an organization with empty houseIdentifier-element
      */
     @Test
     public void testUpdateOrganizationEmptyHouseIdentifier() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_HOUSE_IDENTIFIER, contentType, null);
     }
 
     /**
      * Test updating an organization with empty addressSupplement-element
      */
     @Test
     public void testUpdateOrganizationEmptyAddressSuplement() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_ADDRESS_SUPLEMENT, contentType, null);
     }
 
     /**
      * Test updating an organization with empty postalCode-element
      */
     @Test
     public void testUpdateOrganizationEmptyPostalCode() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_POSTAL_CODE, contentType, null);
     }
 
     /**
      * Test updating an organization with empty city-element
      */
     @Test
     public void testUpdateOrganizationEmptyCity() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_CITY, contentType, null);
     }
 
     /**
      * Test updating an organization with empty state-element
      */
     @Test
     public void testUpdateOrganizationEmptyState() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_STATE, contentType, null);
     }
 
     /**
      * Test updating an organization with empty country-element
      */
     @Test
     public void testUpdateOrganizationEmptyCountry() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_COUNTRY, contentType, null);
     }
 
     /**
      * Test updating an organization with empty latitude-element
      */
     @Test
     public void testUpdateOrganizationEmptyLatitude() throws Exception {
         // XML schema doesn't accept an empty string as decimal
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_LATITUDE, contentType,
             BadRequest.class);
     }
 
     /**
      * Test updating an organization with empty longitude-element
      */
     @Test
     public void testUpdateOrganizationEmptyLongitude() throws Exception {
         // XML schema doesn't accept an empty string as decimal
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_LONGITUDE, contentType,
             BadRequest.class);
     }
 
     /**
      * Test updating an organization with empty telephone-element
      */
     @Test
     public void testUpdateOrganizationEmptyTelephone() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_PHONE, contentType, null);
     }
 
     /**
      * Test updating an organization with empty fax-element
      */
     @Test
     public void testUpdateOrganizationEmptyFax() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_FAX, contentType, null);
     }
 
     /**
      * Test updating an organization with empty email-element
      */
     @Test
     public void testUpdateOrganizationEmptyEmail() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_EMAIL, contentType, null);
     }
 
     /**
      * Test updating an organization with empty url-element
      */
     @Test
     public void testUpdateOrganizationEmptyUrl() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_URL, contentType, null);
     }
 
     /**
      * Test updating an organization with empty logo-element
      */
     @Test
     public void testUpdateOrganizationEmptyLogo() throws Exception {
         organizationUtility.testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_LOGO, contentType, null);
     }
 
     /**
      * Test updating an organization with empty status-element
      */
     @Test
     public void testUpdateOrganizationEmptyStatus() throws Exception {
         // XML schema doesn't allow an empty string as enumeration value
         organizationUtility
             .testEmptyAttribute(true, Constants.XPATH_ORGANIZATION_STATUS, contentType, BadRequest.class);
     }
 
     /**
      * Test updating an organization with no pid-element
      */
     @Test
     public void testUpdateOrganizationMissingPid() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_PID, contentType, null);
     }
 
     /**
      * Test updating an organization with no displayName-element
      * 
      * @expect HttpClientException
      */
     @Test
     public void testUpdateOrganizationMissingDisplayName() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_DISPLAY_NAME, contentType,
             BadRequest.class);
     }
 
     /**
      * Test updating an organization with no abbreviation-element
      */
     @Test
     public void testUpdateOrganizationMissingAbbreviation() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_ABBREVIATION, contentType, null);
     }
 
     /**
      * Test updating an organization with no description-element
      */
     @Test
     public void testUpdateOrganizationMissingDescription() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_DESCRIPTION, contentType, null);
     }
 
     /**
      * Test updating an organization with no legalStatus-element
      */
     @Test
     public void testUpdateOrganizationMissingLegalStatus() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_LEGAL_STATUS, contentType, null);
     }
 
     /**
      * Test updating an organization with no sector-element
      */
     @Test
     public void testUpdateOrganizationMissingSector() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_SECTOR, contentType, null);
     }
 
     /**
      * Test updating an organization with no street-element
      */
     @Test
     public void testUpdateOrganizationMissingStreet() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_STREET, contentType, null);
     }
 
     /**
      * Test updating an organization with no houseIdentifier-element
      */
     @Test
     public void testUpdateOrganizationMissingHouseIdentifier() throws Exception {
         organizationUtility
             .testMissingAttribute(true, Constants.XPATH_ORGANIZATION_HOUSE_IDENTIFIER, contentType, null);
     }
 
     /**
      * Test updating an organization with no addressSupplement-element
      */
     @Test
     public void testUpdateOrganizationMissingAddressSuplement() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_ADDRESS_SUPLEMENT, contentType,
             null);
     }
 
     /**
      * Test updating an organization with no postalCode-element
      */
     @Test
     public void testUpdateOrganizationMissingPostalCode() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_POSTAL_CODE, contentType, null);
     }
 
     /**
      * Test updating an organization with no city-element
      */
     @Test
     public void testUpdateOrganizationMissingCity() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_CITY, contentType, null);
     }
 
     /**
      * Test updating an organization with no state-element
      */
     @Test
     public void testUpdateOrganizationMissingState() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_STATE, contentType, null);
     }
 
     /**
      * Test updating an organization with no country-element
      */
     @Test
     public void testUpdateOrganizationMissingCountry() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_COUNTRY, contentType, null);
     }
 
     /**
      * Test updating an organization with no telephone-element
      */
     @Test
     public void testUpdateOrganizationMissingTelephone() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_PHONE, contentType, null);
     }
 
     /**
      * Test updating an organization with no fax-element
      */
     @Test
     public void testUpdateOrganizationMissingFax() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_FAX, contentType, null);
     }
 
     /**
      * Test updating an organization with no email-element
      */
     @Test
     public void testUpdateOrganizationMissingEmail() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_EMAIL, contentType, null);
     }
 
     /**
      * Test updating an organization with no url-element
      */
     @Test
     public void testUpdateOrganizationMissingUrl() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_URL, contentType, null);
     }
 
     /**
      * Test updating an organization with no logo-element
      */
     @Test
     public void testUpdateOrganizationMissingLogo() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_LOGO, contentType, null);
     }
 
     /**
      * Test updating an organization with New abbreviation-element
      */
     @Test
     public void testUpdateOrganizationNewAbbreviation() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_ABBREVIATION, "new value",
             contentType, null);
     }
 
     /**
      * Test updating an organization with New description-element
      */
     @Test
     public void testUpdateOrganizationNewDescription() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_DESCRIPTION, "new value", contentType,
             null);
     }
 
     /**
      * Test updating an organization with New legalStatus-element
      */
     @Test
     public void testUpdateOrganizationNewLegalStatus() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_LEGAL_STATUS, "new value",
             contentType, null);
     }
 
     /**
      * Test updating an organization with New sector-element
      */
     @Test
     public void testUpdateOrganizationNewSector() throws Exception {
         // XML schema doesn't allow an empty string as enumeration value
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_SECTOR, "new value", contentType,
             BadRequest.class);
     }
 
     /**
      * Test updating an organization with New street-element
      */
     @Test
     public void testUpdateOrganizationNewStreet() throws Exception {
         organizationUtility
             .testAdditionalAttribute(Constants.XPATH_ORGANIZATION_STREET, "new value", contentType, null);
     }
 
     /**
      * Test updating an organization with New houseIdentifier-element
      */
     @Test
     public void testUpdateOrganizationNewHouseIdentifier() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_HOUSE_IDENTIFIER, "new value",
             contentType, null);
     }
 
     /**
      * Test updating an organization with New addressSupplement-element
      */
     @Test
     public void testUpdateOrganizationNewAddressSuplement() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_ADDRESS_SUPLEMENT, "new value",
             contentType, null);
     }
 
     /**
      * Test updating an organization with New postalCode-element
      */
     @Test
     public void testUpdateOrganizationMissingNewCode() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_POSTAL_CODE, "new value", contentType,
             null);
     }
 
     /**
      * Test updating an organization with New city-element
      */
     @Test
     public void testUpdateOrganizationNewCity() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_CITY, "new value", contentType, null);
     }
 
     /**
      * Test updating an organization with New state-element
      */
     @Test
     public void testUpdateOrganizationNewState() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_STATE, "new value", contentType, null);
     }
 
     /**
      * Test updating an organization with New country-element
      */
     @Test
     public void testUpdateOrganizationNewCountry() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_COUNTRY, "fr", contentType, null);
     }
 
     /**
      * Test updating an organization with New latitude-element
      */
     @Test
     public void testUpdateOrganizationNewLatitude() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_LATITUDE, "56.2", contentType, null);
     }
 
     /**
      * Test updating an organization with New longitude-element
      */
     @Test
     public void testUpdateOrganizationNewLongitude() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_LONGITUDE, "56.3", contentType, null);
     }
 
     /**
      * Test updating an organization with New telephone-element
      */
     @Test
     public void testUpdateOrganizationNewTelephone() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_PHONE, "new value", contentType, null);
     }
 
     /**
      * Test updating an organization with New fax-element
      */
     @Test
     public void testUpdateOrganizationNewFax() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_FAX, "new value", contentType, null);
     }
 
     /**
      * Test updating an organization with New email-element
      */
     @Test
     public void testUpdateOrganizationNewEmail() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_EMAIL, "newvalue@val.de", contentType,
             null);
     }
 
     /**
      * Test updating an organization with New url-element
      */
     @Test
     public void testUpdateOrganizationNewUrl() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_URL, "new value", contentType, null);
     }
 
     /**
      * Test updating an organization with New logo-element
      */
     @Test
     public void testUpdateOrganizationNewLogo() throws Exception {
         organizationUtility.testAdditionalAttribute(Constants.XPATH_ORGANIZATION_LOGO, "new value", contentType, null);
     }
 
     /**
      * Test updating an organization with lat/lon
      * 
      * https://jira.deutsche-digitale-bibliothek.de/browse/DDBAAS-56
      */
     @Test
     public void testUpdateOrganizationLatLon() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_with_parent", true,
                 contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_LATITUDE, Constants.REPLACEMENT_DOUBLE_SMALL,
                 contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_LONGITUDE, Constants.REPLACEMENT_DOUBLE_LARGE,
                 contentType);
         String resource =
             organizationUtility.doTestCreateOrganizationWithObject(Authentication.ADMIN_USER_ID,
                 Authentication.ADMIN_PASSWORD, organization, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_with_parent", false,
                 contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_LATITUDE, Constants.REPLACEMENT_DOUBLE_LARGE,
                 contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_LONGITUDE, Constants.REPLACEMENT_DOUBLE_SMALL,
                 contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, null);
     }
 
     /**
      * Test updating an organization with no status-element
      */
     @Test
     public void testUpdateOrganizationMissingStatus() throws Exception {
         organizationUtility.testMissingAttribute(true, Constants.XPATH_ORGANIZATION_STATUS, contentType, null);
     }
 
     /**
      * Test updating an organization with malformed email
      */
     @Test
     public void testUpdateOrganizationMalformedEmail() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization", false, contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_EMAIL, Constants.REPLACEMENT_MALFORMED,
                 contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, BadRequest.class);
     }
 
     /**
      * Test updating an organization with malformed country
      */
     @Test
     public void testUpdateOrganizationMalformedCountry() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization", false, contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_COUNTRY, Constants.REPLACEMENT_MALFORMED,
                 contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                organization, id, contentType, HttpServerException.class);
     }
 
     /**
      * Test updating an organization with malformed state
      */
     @Test
     public void testUpdateOrganizationMalformedState() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization", false, contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_STATE, Constants.REPLACEMENT_MALFORMED,
                 contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, null);
     }
 
     /**
      * Test updating an organization with changed subsector
      */
     @Test
     public void testUpdateOrganizationSubsector() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_sub_sector", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector", false,
                 contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_SUB_SECTOR, Constants.REPLACEMENT_SUB_SECTOR,
                 contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, null);
     }
 
     /**
      * Test updating an organization with empty subsector
      */
     @Test
     public void testUpdateOrganizationEmptySubsector() throws Exception {
        // XML schema doesn't allow an empty string as enumeration value
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_sub_sector", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector", false,
                 contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_SUB_SECTOR, contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                organization, id, contentType, BadRequest.class);
     }
 
     /**
      * Test updating an organization with missing subsector
      */
     @Test
     public void testUpdateOrganizationMissingSubsector() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_sub_sector", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector", false,
                 contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization = Utility.remove(organization, Constants.XPATH_ORGANIZATION_SUB_SECTOR, contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, null);
     }
 
     /**
      * Test updating an organization with changed sector
      */
     @Test
     public void testUpdateOrganizationChangedSector() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_sub_sector", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector", false,
                 contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_SECTOR, Constants.REPLACEMENT_SECTOR,
                 contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                organization, id, contentType, null);
     }
 
     /**
      * Test updating an organization with changed fundingAgency
      */
     @Test
     public void testUpdateOrganizationChangedFundingAgency() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_funding_agency", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_funding_agency",
                 false, contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_FUNDING_AGENCY,
                 Constants.REPLACEMENT_FUNDING_AGENCY, contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, null);
     }
 
     /**
      * Test updating an organization with empty fundingAgency
      */
     @Test
     public void testUpdateOrganizationEmptyFundingAgency() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_funding_agency", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_funding_agency",
                 false, contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_FUNDING_AGENCY, contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, null);
     }
 
     /**
      * Test updating an organization with missing fundingAgency
      */
     @Test
     public void testUpdateOrganizationMissingFundingAgency() throws Exception {
         String resource =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_funding_agency", true, contentType, null);
         String id = Utility.extract(resource, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_funding_agency",
                 false, contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_ID, id, contentType);
         organization = Utility.remove(organization, Constants.XPATH_ORGANIZATION_FUNDING_AGENCY, contentType);
         resource =
             organizationUtility.doTestUpdateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 organization, id, contentType, null);
     }
 
 }
