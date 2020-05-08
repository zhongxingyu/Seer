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
 import de.fiz.ddb.aas.test.exceptions.Conflict;
 import de.fiz.ddb.aas.test.util.Utility;
 import de.fiz.ddb.aas.test.util.http.Authentication;
 
 /**
  * Create Tests for organization-resource Run with xml-representations and json
  * representations.
  * 
  * tests method: create organization
  * 
  * NOTE: Creating an Organization with empty sector, status, lat or lon is not
  * possible! As in XML Enumerations or Doubles may not be empty.
  * 
  * @author mih
  * 
  */
 @RunWith(value = Parameterized.class)
 public class OrganizationCreateIT extends AasTestBase {
 
     private OrganizationUtility organizationUtility;
 
     private ContentType contentType;
 
     /**
      * Constructor with contentType
      * 
      * @param contentType
      *            contentType (APPLICATION_XML or APPLICATION_JSON)
      * @throws Exception
      */
     public OrganizationCreateIT(ContentType contentType) throws Exception {
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
      * Test creating an organization with unknown element
      */
     @Test
     public void testCreateOrganizationUnknownElement() throws Exception {
         organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             "organization_unknown_element", true, contentType, BadRequest.class);
     }
 
     /**
      * Test creating an organization with empty parent element and orgId
      */
     @Test
     public void testCreateOrganizationEmptyParentId() throws Exception {
         organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             "organization_with_parent", true, contentType, null);
     }
 
     /**
      * Test creating an organization with empty parent element and empty orgId
      */
     @Test
     public void testCreateOrganizationEmptyParentEmptyId() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_ID, contentType, null);
     }
 
     /**
      * Test creating an organization with empty parent element and no orgId
      */
     @Test
     public void testCreateOrganizationEmptyParentNoId() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_ID, contentType, null);
     }
 
     /**
      * Test creating an organization with no parent element and no orgId
      */
     @Test
     public void testCreateOrganizationNoParentNoId() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_ID, contentType, null);
     }
 
     /**
      * Test creating an organization with parent element and orgId
      */
     @Test
     public void testCreateOrganizationParentId() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.doTestCreateOrganizationWithParent(Authentication.ADMIN_USER_ID,
             Authentication.ADMIN_PASSWORD, "organization_with_parent", parentId, true, contentType, null);
     }
 
     /**
      * Test creating an organization with parent element and empty orgId
      */
     @Test
     public void testCreateOrganizationParentEmptyId() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_with_parent",
                 contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_PARENT, parentId, contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.doTestCreateOrganizationWithObject(Authentication.ADMIN_USER_ID,
             Authentication.ADMIN_PASSWORD, organization, contentType, null);
     }
 
     /**
      * Test creating an organization with parent element and no orgId
      */
     @Test
     public void testCreateOrganizationParentNoId() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_with_parent",
                 contentType);
         organization = Utility.replace(organization, Constants.XPATH_ORGANIZATION_PARENT, parentId, contentType);
         organization = Utility.remove(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.doTestCreateOrganizationWithObject(Authentication.ADMIN_USER_ID,
             Authentication.ADMIN_PASSWORD, organization, contentType, null);
     }
 
     /**
      * Test creating an organization with elements in wrong order XML-Input:
      * HttpClientException JSON-Input: No Exception
      */
     @Test
     public void testCreateOrganizationWithWrongElementOrder() throws Exception {
         if (contentType == ContentType.APPLICATION_XML) {
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_wrong_element_order", true, contentType, BadRequest.class);
         }
         else if (contentType == ContentType.APPLICATION_JSON) {
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization_wrong_element_order", true, contentType, null);
         }
     }
 
     /**
      * Test creating an organization containing special characters
      */
     @Test
     public void testCreateOrganizationWithSpecialCharacters() throws Exception {
         organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             "organization_special_characters", true, contentType, null);
     }
 
     /**
      * Test creating an organization with empty pid-element
      */
     @Test
     public void testCreateOrganizationEmptyPid() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_PID, contentType, null);
     }
 
     /**
      * Test creating an organization with empty displayName-element
      * 
      * @expect HttpClientException
      */
     @Test
     public void testCreateOrganizationEmptyDisplayName() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_DISPLAY_NAME, contentType,
             BadRequest.class);
     }
 
     /**
      * Test creating an organization with empty abbreviation-element
      */
     @Test
     public void testCreateOrganizationEmptyAbbreviation() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_ABBREVIATION, contentType, null);
     }
 
     /**
      * Test creating an organization with empty description-element
      */
     @Test
     public void testCreateOrganizationEmptyDescription() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_DESCRIPTION, contentType, null);
     }
 
     /**
      * Test creating an organization with empty legalStatus-element
      */
     @Test
     public void testCreateOrganizationEmptyLegalStatus() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_LEGAL_STATUS, contentType, null);
     }
 
     /**
      * Test creating an organization with empty sector-element
      */
     @Test
     public void testCreateOrganizationEmptySector() throws Exception {
         // XML schema doesn't allow an empty string as enumeration value
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_SECTOR, contentType,
             BadRequest.class);
     }
 
     /**
      * Test creating an organization with empty fundingAgency-element
      */
     @Test
     public void testCreateOrganizationEmptyFundingAgency() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_FUNDING_AGENCY, contentType, null);
     }
 
     /**
      * Test creating an organization with empty street-element
      */
     @Test
     public void testCreateOrganizationEmptyStreet() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_STREET, contentType, null);
     }
 
     /**
      * Test creating an organization with empty houseIdentifier-element
      */
     @Test
     public void testCreateOrganizationEmptyHouseIdentifier() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_HOUSE_IDENTIFIER, contentType, null);
     }
 
     /**
      * Test creating an organization with empty addressSupplement-element
      */
     @Test
     public void testCreateOrganizationEmptyAddressSuplement() throws Exception {
         organizationUtility
             .testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_ADDRESS_SUPLEMENT, contentType, null);
     }
 
     /**
      * Test creating an organization with empty postalCode-element
      */
     @Test
     public void testCreateOrganizationEmptyPostalCode() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_POSTAL_CODE, contentType, null);
     }
 
     /**
      * Test creating an organization with empty city-element
      */
     @Test
     public void testCreateOrganizationEmptyCity() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_CITY, contentType, null);
     }
 
     /**
      * Test creating an organization with empty state-element
      */
     @Test
     public void testCreateOrganizationEmptyState() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_STATE, contentType, null);
     }
 
     /**
      * Test creating an organization with empty country-element
      */
     @Test
     public void testCreateOrganizationEmptyCountry() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_COUNTRY, contentType, null);
     }
 
     /**
      * Test creating an organization with empty latitude-element
      */
     @Test
     public void testCreateOrganizationEmptyLatitude() throws Exception {
         // XML schema doesn't accept an empty string as decimal
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_LATITUDE, contentType,
             BadRequest.class);
     }
 
     /**
      * Test creating an organization with empty longitude-element
      */
     @Test
     public void testCreateOrganizationEmptyLongitude() throws Exception {
         // XML schema doesn't accept an empty string as decimal
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_LONGITUDE, contentType,
             BadRequest.class);
     }
 
     /**
      * Test creating an organization with empty telephone-element
      */
     @Test
     public void testCreateOrganizationEmptyTelephone() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_PHONE, contentType, null);
     }
 
     /**
      * Test creating an organization with empty fax-element
      */
     @Test
     public void testCreateOrganizationEmptyFax() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_FAX, contentType, null);
     }
 
     /**
      * Test creating an organization with empty email-element
      */
     @Test
     public void testCreateOrganizationEmptyEmail() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_EMAIL, contentType, null);
     }
 
     /**
      * Test creating an organization with empty url-element
      */
     @Test
     public void testCreateOrganizationEmptyUrl() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_URL, contentType, null);
     }
 
     /**
      * Test creating an organization with empty logo-element
      */
     @Test
     public void testCreateOrganizationEmptyLogo() throws Exception {
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_LOGO, contentType, null);
     }
 
     /**
      * Test creating an organization with empty status-element
      */
     @Test
     public void testCreateOrganizationEmptyStatus() throws Exception {
         // XML schema doesn't allow an empty string as enumeration value
         organizationUtility.testEmptyAttribute(false, Constants.XPATH_ORGANIZATION_STATUS, contentType,
             BadRequest.class);
     }
 
     /**
      * Test creating an organization with no pid-element
      */
     @Test
     public void testCreateOrganizationMissingPid() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_PID, contentType, null);
     }
 
     /**
      * Test creating an organization with no displayName-element
      * 
      * @expect HttpClientException
      */
     @Test
     public void testCreateOrganizationMissingDisplayName() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_DISPLAY_NAME, contentType,
             BadRequest.class);
     }
 
     /**
      * Test creating an organization with no abbreviation-element
      */
     @Test
     public void testCreateOrganizationMissingAbbreviation() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_ABBREVIATION, contentType, null);
     }
 
     /**
      * Test creating an organization with no description-element
      */
     @Test
     public void testCreateOrganizationMissingDescription() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_DESCRIPTION, contentType, null);
     }
 
     /**
      * Test creating an organization with no legalStatus-element
      */
     @Test
     public void testCreateOrganizationMissingLegalStatus() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_LEGAL_STATUS, contentType, null);
     }
 
     /**
      * Test creating an organization with no sector-element
      */
     @Test
     public void testCreateOrganizationMissingSector() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_SECTOR, contentType, null);
     }
 
     /**
      * Test creating an organization with no fundingAgency-element
      */
     @Test
     public void testCreateOrganizationMissingFundingAgency() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_FUNDING_AGENCY, contentType, null);
     }
 
     /**
      * Test creating an organization with no street-element
      */
     @Test
     public void testCreateOrganizationMissingStreet() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_STREET, contentType, null);
     }
 
     /**
      * Test creating an organization with no houseIdentifier-element
      */
     @Test
     public void testCreateOrganizationMissingHouseIdentifier() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_HOUSE_IDENTIFIER, contentType,
             null);
     }
 
     /**
      * Test creating an organization with no addressSupplement-element
      */
     @Test
     public void testCreateOrganizationMissingAddressSuplement() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_ADDRESS_SUPLEMENT, contentType,
             null);
     }
 
     /**
      * Test creating an organization with no postalCode-element
      */
     @Test
     public void testCreateOrganizationMissingPostalCode() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_POSTAL_CODE, contentType, null);
     }
 
     /**
      * Test creating an organization with no city-element
      */
     @Test
     public void testCreateOrganizationMissingCity() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_CITY, contentType, null);
     }
 
     /**
      * Test creating an organization with no state-element
      */
     @Test
     public void testCreateOrganizationMissingState() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_STATE, contentType, null);
     }
 
     /**
      * Test creating an organization with no country-element
      */
     @Test
     public void testCreateOrganizationMissingCountry() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_COUNTRY, contentType, null);
     }
 
     /**
      * Test creating an organization with no telephone-element
      */
     @Test
     public void testCreateOrganizationMissingTelephone() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_PHONE, contentType, null);
     }
 
     /**
      * Test creating an organization with no fax-element
      */
     @Test
     public void testCreateOrganizationMissingFax() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_FAX, contentType, null);
     }
 
     /**
      * Test creating an organization with no email-element
      */
     @Test
     public void testCreateOrganizationMissingEmail() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_EMAIL, contentType, null);
     }
 
     /**
      * Test creating an organization with no url-element
      */
     @Test
     public void testCreateOrganizationMissingUrl() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_URL, contentType, null);
     }
 
     /**
      * Test creating an organization with no logo-element
      */
     @Test
     public void testCreateOrganizationMissingLogo() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_LOGO, contentType, null);
     }
 
     /**
      * Test creating an organization with no status-element
      */
     @Test
     public void testCreateOrganizationMissingStatus() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_STATUS, contentType, null);
     }
 
     /**
      * Test creating an organization without id
      */
     @Test
     public void testCreateOrganizationMissingId() throws Exception {
         organizationUtility.testMissingAttribute(false, Constants.XPATH_ORGANIZATION_ID, contentType, null);
     }
 
     /**
      * Test creating an organization with already existing id
      */
     @Test
     public void testCreateOrganizationExistingId() throws Exception {
         String organization =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             organization, contentType, Conflict.class);
     }
 
     /**
      * Test creating an organization with malformed email
      */
     @Test
     public void testCreateOrganizationMalformedEmail() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization", contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_EMAIL, Constants.REPLACEMENT_MALFORMED,
                 contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             organization, contentType, BadRequest.class);
     }
 
     /**
      * Test creating an organization with malformed country
      */
     @Test
     public void testCreateOrganizationMalformedCountry() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization", contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_COUNTRY, Constants.REPLACEMENT_MALFORMED,
                 contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
            organization, contentType, null);
     }
 
     /**
      * Test creating an organization with malformed state
      */
     @Test
     public void testCreateOrganizationMalformedState() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization", contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_STATE, Constants.REPLACEMENT_MALFORMED,
                 contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             organization, contentType, null);
     }
 
     /**
      * Test creating an organization with sector=Archiv
      */
     @Test
     public void testCreateOrganizationArchivSector() throws Exception {
         organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             "organization_sub_sector", true, contentType, null);
     }
 
     /**
      * Test creating an organization with sector=Archiv
      */
     @Test
     public void testCreateOrganizationArchivSectors() throws Exception {
         organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             "organization_sub_sectors", true, contentType, null);
     }
 
     /**
      * Test creating an organization with sector=Archiv and missing subSector
      */
     @Test
     public void testCreateOrganizationArchivSectorMissingSubSector() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector",
                 contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization = Utility.remove(organization, Constants.XPATH_ORGANIZATION_SUB_SECTOR, contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             organization, contentType, null);
     }
 
     /**
      * Test creating an organization with sector=Archiv and wrong subSector
      */
     @Test
     public void testCreateOrganizationArchivSectorWrongSubSector() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector",
                 contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_SUB_SECTOR, Constants.REPLACEMENT_MALFORMED,
                 contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             organization, contentType, BadRequest.class);
     }
 
     /**
      * Test creating an organization with sector=Archiv and empty subSector
      */
     @Test
     public void testCreateOrganizationArchivSectorEmptySubSector() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector",
                 contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_SUB_SECTOR, contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
            organization, contentType, null);
     }
 
     /**
      * Test creating an organization with sector=Archiv and wrong subSector
      */
     @Test
     public void testCreateOrganizationArchivSectorsWrongSubSector() throws Exception {
         organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             "organization_sub_sectors_wrong", true, contentType, BadRequest.class);
     }
 
     /**
      * Test creating an organization with sector!=Archiv and subSector
      */
     @Test
     public void testCreateOrganizationNoArchivSector() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_sub_sector",
                 contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization =
             Utility.replace(organization, Constants.XPATH_ORGANIZATION_SECTOR, Constants.REPLACEMENT_SECTOR,
                 contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             organization, contentType, null);
     }
 
     /**
      * Test creating an organization with funding Agency
      */
     @Test
     public void testCreateOrganizationFundingAgency() throws Exception {
         organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             "organization_funding_agency", true, contentType, null);
     }
 
     /**
      * Test creating an organization without funding Agency
      */
     @Test
     public void testCreateOrganizationWithoutFundingAgency() throws Exception {
         String organization =
             organizationUtility.getTemplate(OrganizationUtility.getTemplatepath(), "organization_funding_agency",
                 contentType);
         organization = Utility.removeValue(organization, Constants.XPATH_ORGANIZATION_ID, contentType);
         organization = Utility.remove(organization, Constants.XPATH_ORGANIZATION_FUNDING_AGENCY, contentType);
         organizationUtility.createOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
             organization, contentType, null);
     }
 
     /**
      * Test creating an child-organization with funding Agency element
      */
     @Test
     public void testCreateOrganizationFundingAgencyChild() throws Exception {
         String parent =
             organizationUtility.doTestCreateOrganization(Authentication.ADMIN_USER_ID, Authentication.ADMIN_PASSWORD,
                 "organization", true, contentType, null);
         String parentId = Utility.extract(parent, Constants.XPATH_ORGANIZATION_ID, contentType);
         organizationUtility.doTestCreateOrganizationWithParent(Authentication.ADMIN_USER_ID,
             Authentication.ADMIN_PASSWORD, "organization_funding_agency_with_parent", parentId, true, contentType,
             BadRequest.class);
     }
 }
