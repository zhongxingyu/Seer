 package com.orangeleap.tangerine.test.dao.ibatis;
 
 import com.orangeleap.tangerine.dao.ConstituentDao;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import org.apache.commons.logging.Log;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 public class IBatisConstituentDaoTest extends AbstractIBatisTest {
     
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
     
     private ConstituentDao constituentDao;
 
     @BeforeMethod
     public void setup() {
         constituentDao = (ConstituentDao)super.applicationContext.getBean("constituentDAO");
     }
     
     public static void testConstituentId100(Constituent constituent) {
         assert constituent.getId() == 100L;
         assert "Billy Graham Ministries".equals(constituent.getOrganizationName());
         assert "Graham".equals(constituent.getLastName());
         assert "Billy".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert constituent.getSuffix() == null;
         assert "company1".equals(constituent.getSite().getName());    
         assert 1000000L == constituent.getAccountNumber();
     }
     
     public static void testConstituentId200(Constituent constituent) {
         assert constituent.getId() == 200L;
         assert "Painters, Inc.".equals(constituent.getOrganizationName());
         assert "Picasso".equals(constituent.getLastName());
         assert "Pablo".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert "Sr".equals(constituent.getSuffix());
         assert "company1".equals(constituent.getSite().getName());
         assert 2000000L == constituent.getAccountNumber();
     }
 
     public static void testConstituentId300(Constituent constituent) {
         assert constituent.getId() == 300L;
         assert "Howdy Doody Inc".equals(constituent.getOrganizationName());
         assert "Doody".equals(constituent.getLastName());
         assert "Howdy".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert constituent.getSuffix() == null;
         assert "company1".equals(constituent.getSite().getName());
         assert 3000000L == constituent.getAccountNumber();
     }
 
     @Test(groups = { "testMaintainConstituent" }, dependsOnGroups = { "testReadConstituent" })
     public void testMaintainConstituent() throws Exception {
         // Insert
         Constituent constituent = new Constituent();
         constituent.setFirstName("Joe");
         constituent.setLastName("Bob");
         constituent.setSite(new Site("company1"));
         constituent.setConstituentType(Constituent.INDIVIDUAL);
         constituent.setTitle("Sir");
         constituent.setAccountNumber(4000000L);
         
         constituent = constituentDao.maintainConstituent(constituent);
         assert constituent.getId() > 0;
         
         Constituent readConstituent = constituentDao.readConstituentById(constituent.getId());
         assert readConstituent != null;
         assert constituent.getFirstName().equals(readConstituent.getFirstName());
         assert constituent.getLastName().equals(readConstituent.getLastName());
         assert constituent.getSite().getName().equals(readConstituent.getSite().getName());
         assert constituent.getConstituentType().equals(readConstituent.getConstituentType());
         assert constituent.getTitle().equals(readConstituent.getTitle());
         assert constituent.getAccountNumber().equals(readConstituent.getAccountNumber());
         
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentIndividualRoles());
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentOrganizationRoles());
         assert readConstituent.getLegalName() == null;
         assert readConstituent.getLoginId() == null;
         assert "Unknown".equals(readConstituent.getMaritalStatus());
         assert readConstituent.getMiddleName() == null;
         assert readConstituent.getNaicsCode() == null;
         assert readConstituent.getOrganizationName() == null;
         assert readConstituent.getPreferredPhoneType() == null;
         assert readConstituent.getRecognitionName() != null;
         assert readConstituent.getSuffix() == null;
         assert readConstituent.getCreateDate() != null;
         assert readConstituent.getUpdateDate() != null;
         
         // Update
         constituent.setTitle("Lady");
         constituent.setLoginId("joe@bob.com");
         constituent.setNaicsCode("huh");
         constituent = constituentDao.maintainConstituent(constituent);
         readConstituent = constituentDao.readConstituentById(constituent.getId());
         assert readConstituent != null;
         assert "Lady".equals(readConstituent.getTitle());
         assert "joe@bob.com".equals(readConstituent.getLoginId());
         assert 4000000L == readConstituent.getAccountNumber();
         assert readConstituent.getNaicsCode() == null;
         
         assert constituent.getFirstName().equals(readConstituent.getFirstName());
         assert constituent.getLastName().equals(readConstituent.getLastName());
         assert constituent.getSite().getName().equals(readConstituent.getSite().getName());
         assert constituent.getConstituentType().equals(readConstituent.getConstituentType());
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentIndividualRoles());
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentOrganizationRoles());
         assert readConstituent.getLegalName() == null;
         assert "Unknown".equals(readConstituent.getMaritalStatus());
         assert readConstituent.getMiddleName() == null;
         assert readConstituent.getOrganizationName() == null;
         assert readConstituent.getPreferredPhoneType() == null;
         assert readConstituent.getRecognitionName() != null;
         assert readConstituent.getSuffix() == null;
         assert readConstituent.getCreateDate() != null;
         assert readConstituent.getUpdateDate() != null;      
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentByIdInvalid() throws Exception {
         Constituent constituent = constituentDao.readConstituentById(0L);
         assert constituent == null;
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentById() throws Exception {
         Constituent constituent = constituentDao.readConstituentById(100L);
         assert constituent != null;
         assert constituent.getId() == 100;
         assert "Billy Graham Ministries".equals(constituent.getOrganizationName());
         assert "Graham".equals(constituent.getLastName());
         assert "Billy".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert constituent.getSuffix() == null;
         assert "company1".equals(constituent.getSite().getName());        
     } 
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentByLoginIdInvalid() throws Exception {
         Constituent constituent = constituentDao.readConstituentByLoginId("pablo@companyDoesNotExist.com");
         assert constituent == null;
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentByLoginId() throws Exception {
         Constituent constituent = constituentDao.readConstituentByLoginId("pablo@company1.com");
         assert constituent != null;
         assert constituent.getId() == 200;
         testConstituentId200(constituent);
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadAllConstituentsBySiteName() throws Exception {
         List<Constituent> constituents = constituentDao.readAllConstituentsBySite();
         assert constituents != null && constituents.size() == 3;
         for (Constituent constituent : constituents) {
             assert "company1".equals(constituent.getSite().getName());
             assert constituent.getId() == 200 || constituent.getId() == 300 || constituent.getId() == 100;
             assert "Painters, Inc.".equals(constituent.getOrganizationName()) || "Howdy Doody Inc".equals(constituent.getOrganizationName()) || "Billy Graham Ministries".equals(constituent.getOrganizationName());
             assert "Picasso".equals(constituent.getLastName()) || "Doody".equals(constituent.getLastName()) || "Graham".equals(constituent.getLastName());
             assert "Pablo".equals(constituent.getFirstName()) || "Howdy".equals(constituent.getFirstName()) || "Billy".equals(constituent.getFirstName());
             assert constituent.getMiddleName() == null;
             assert "Sr".equals(constituent.getSuffix()) || constituent.getSuffix() == null;
         }
     }
     
     @Test(groups = { "testSearchConstituents" })
     public void testSearchConstituents() throws Exception {
     	Map<String, Object> params = new HashMap<String, Object>();
         params.put("firstName", "Pablo");
        params.put("primaryAddress.addressLine1", "ACORN");
         params.put("primaryEmail.emailAddress", "");
    	
         List<Constituent> constituents = constituentDao.searchConstituents(params, "firstName", "ASC", 0, 100, Locale.US);
         assert constituents != null && ! constituents.isEmpty();
         for (Constituent constituent : constituents) {
             assert constituent.getFirstName().equals("Pablo");
         }
 
         params = new HashMap<String, Object>();
         params.put("firstName", "Pablo");
        params.put("primaryAddress.addressLine1", "ACORN");
         params.put("primaryEmail.emailAddress", "dog@dog.com");
 
         constituents = constituentDao.searchConstituents(params, "firstName", "ASC", 0, 100, Locale.US);
         assert constituents != null && constituents.isEmpty();
 
         params = new HashMap<String, Object>();
         constituents = constituentDao.searchConstituents(params, "firstName", "ASC", 0, 100, Locale.US);
        assert constituents != null && ! constituents.isEmpty();        
 
         params = new HashMap<String, Object>();
         constituents = constituentDao.searchConstituents(params);
         assert constituents != null && ! constituents.isEmpty();
 
         params = new HashMap<String, Object>();
         params.put("firstName", "Pablo");
        params.put("primaryAddress.addressLine1", "ACORN");
         params.put("primaryEmail.emailAddress", "");
 
         constituents = constituentDao.searchConstituents(params);
         assert constituents != null && ! constituents.isEmpty();
         for (Constituent constituent : constituents) {
             assert constituent.getFirstName().equals("Pablo");
         }
     }
     
     @Test(groups = {"testFindConstituents"})
     public void testFindConstituents() throws Exception {
     	Map<String,Object> params = new HashMap<String, Object>();
     	List<Long> ignoreIds = new ArrayList<Long>();
     	long ignoreId = 1001;
     	ignoreIds.add(ignoreId);
     	
     	params.put("firstName", "Pablo");
 //    	params.put("phoneMap[home].number", "214-113-2542");
 //    	params.put("addressMap[home].addressLine1", "ACORN");
 //    	params.put("emailMap[home].email","pablo@company1.com");
     	
     	List<Constituent> constituents = constituentDao.findConstituents(params,ignoreIds);
     	assert constituents != null && constituents.size() >0;
     	for (Constituent constituent: constituents) {
     			System.out.println(constituents);
     			assert constituent.getFirstName().equals("Pablo");
     			assert constituent.getId().longValue() != ignoreId;
     	}
     }
     
     @Test(groups = { "testSearchConstituents" })
     public void testReadAllConstituentsByIdRange() throws Exception {
         List<Constituent> constituents = constituentDao.readAllConstituentsByAccountRange(1000000L, 2000000L);
         assert constituents.size() == 2;
         for (Constituent constituent : constituents) {
             assert constituent.getId() == 100L || constituent.getId() == 200L;
         }
 
         constituents = constituentDao.readAllConstituentsByAccountRange(1000001L, 1000001L);
         assert constituents.isEmpty();
 
         constituents = constituentDao.readAllConstituentsByAccountRange(1000000L, 1000000L);
         assert constituents.size() == 1;
         assert constituents.get(0).getId() == 100L;
 
         constituents = constituentDao.readAllConstituentsByAccountRange(1000000L, 3000000L);
         assert constituents.size() == 3;
         for (Constituent constituent : constituents) {
             assert constituent.getId() == 100L || constituent.getId() == 200L || constituent.getId() == 300L;
         }
     }
 }
