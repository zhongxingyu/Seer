 package com.marketo.mktows.test;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javassist.CannotCompileException;
 import javassist.CtClass;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import play.Logger;
 
 import com.marketo.mktows.client.MktServiceException;
 import com.marketo.mktows.client.MktowsClientException;
 import com.marketo.mktows.client.MktowsUtil;
 import com.marketo.mktows.wsdl.ActivityRecord;
 import com.marketo.mktows.wsdl.ActivityType;
 import com.marketo.mktows.wsdl.ArrayOfAttribute;
 import com.marketo.mktows.wsdl.Attrib;
 import com.marketo.mktows.wsdl.Attribute;
 import com.marketo.mktows.wsdl.CampaignRecord;
 import com.marketo.mktows.wsdl.CustomObj;
 import com.marketo.mktows.wsdl.ImportToListModeEnum;
 import com.marketo.mktows.wsdl.ImportToListStatusEnum;
 import com.marketo.mktows.wsdl.LeadChangeRecord;
 import com.marketo.mktows.wsdl.LeadKey;
 import com.marketo.mktows.wsdl.LeadKeyRef;
 import com.marketo.mktows.wsdl.LeadMergeStatusEnum;
 import com.marketo.mktows.wsdl.LeadRecord;
 import com.marketo.mktows.wsdl.LeadSyncStatus;
 import com.marketo.mktows.wsdl.MObjCriteria;
 import com.marketo.mktows.wsdl.MObject;
 import com.marketo.mktows.wsdl.MergeStatus;
 import com.marketo.mktows.wsdl.ParamsDescribeMObject;
 import com.marketo.mktows.wsdl.ResultSyncLead;
 import com.marketo.mktows.wsdl.SuccessDescribeMObject;
 import com.marketo.mktows.wsdl.SyncCustomObjStatus;
 import com.marketo.mktows.wsdl.SyncStatus;
 import common.CodeSandbox;
 import common.MktowsClient;
 import common.StreamPostionHolder;
 
 public class TestMktows {
 
 	protected MktowsClient client = null;
 
 	public TestMktows() {
 
 		client = new MktowsClient(TestMktows.ACCESS_KEY, TestMktows.SECRET_KEY,
 				TestMktows.HOST_NAME);
 	}
 
 	public static final String MUNCH_ACCT_ID = "100-AEK-913";
 	public static final String HOST_NAME = "100-AEK-913.mktoapi.com";
 	// public static final String HOST_NAME = "localhost";
 	public static final String ACCESS_KEY = "demo17_1_809934544BFABAE58E5D27";
 	public static final String SECRET_KEY = "27272727aa";
 	public static final String TEST_CODE_SANDBOX = "System.out.println(\"Lead id is : \" + leadRecord.getEmail());  return leadRecord; ";
 	public static final String TEST_CODE_SANDBOX2 = "Map attrMap = null;		ArrayOfAttribute aoAttribute = leadRecord.getLeadAttributeList();		if (aoAttribute != null) {			attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);			if (attrMap != null && !attrMap.isEmpty()) {				Set keySet = attrMap.keySet();String ds = attrMap.get(\"DemographicScore\").toString();String bs = attrMap.get(\"BehaviorScore\").toString();Integer dsi = Integer.valueOf(ds); Integer bsi = Integer.valueOf(bs); Integer nsi = dsi + bsi;/* String nScore = new String(String.valueOf(ns));*/System.out.println(nsi); }		}		return null;";
 	public static final String TEST_CODE_SANDBOX3 = "Integer dsi = new Integer(15); Integer bsi = new Integer(99); Integer nsi = dsi + bsi;System.out.println(nsi);	return null;";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		TestMktows tester = new TestMktows();
 		// tester.testGetMultipleLeadsStaticList();
		 tester.testGetLead();
 		// tester.testGetMultipleLeadsEmail();
 		// tester.testGetLeadActivity();
		// tester.testGetLeadChanges();
 		// tester.testGetMObjects();
 		// tester.testListMObjects();
 		// tester.testDescMObjects();
 		// tester.testRandomFunctionEval();
 		// tester.testCapitalizeName();
 		 //tester.testGetCustomObjects();
 		// tester.testSyncCustomObjects();
 		//tester.testMergeLeads();
 	}
 
 	private void testCapitalizeName() {
 		List<LeadRecord> leadRecords = null;
 		try {
 			leadRecords = this.client.getLead(LeadKeyRef.IDNUM, "1090177");
 			if (leadRecords.size() == 1) {
 				LeadRecord leadRecord = leadRecords.get(0);
 				CodeSandbox csb = new CodeSandbox(ACCESS_KEY, SECRET_KEY,
 						MUNCH_ACCT_ID, 12L);
 				leadRecord = csb.mktoProperCaseLeadFields(leadRecord,
 						new String[] { "FirstName" }, true);
 			}
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 	}
 
 	public void testRandomFunctionEval() {
 		List<LeadRecord> leadRecords = null;
 		try {
 			leadRecords = this.client.getLead(LeadKeyRef.EMAIL,
 					"rrajamani@marketo.com");
 			CodeSandbox csb = new CodeSandbox(ACCESS_KEY, SECRET_KEY,
 					MUNCH_ACCT_ID, 1L);
 
 			CtClass mktoClass = csb.createClass("Marketo");
 			String mName = csb.addMethod(mktoClass, TEST_CODE_SANDBOX3);
 			csb.executeMethod(mktoClass.toClass(), mName, leadRecords.get(0));
 
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		} catch (CannotCompileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public LeadRecord eval100_AEK_9133(LeadRecord leadRecord) {
 		Map<String, Object> attrMap = null;
 		ArrayOfAttribute aoAttribute = leadRecord.getLeadAttributeList();
 		if (aoAttribute != null) {
 			attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 			if (attrMap != null && !attrMap.isEmpty()) {
 				String ds = attrMap.get("DemographicScore").toString();
 				String bs = attrMap.get("BehaviorScore").toString();
 				int dsi = Integer.valueOf(ds);
 				String newScore = String.valueOf(ds + bs);
 				HashMap<String, String> newAttrs = new HashMap<String, String>();
 				newAttrs.put("MyNewScore", newScore);
 				LeadRecord newLeadRecord = MktowsUtil.newLeadRecord(
 						leadRecord.getId(), null, null, null, newAttrs);
 				try {
 					client.syncLead(newLeadRecord, null, false);
 				} catch (MktowsClientException e) {
 					Logger.error("Unable to sync lead with new score :%s",
 							e.getMessage());
 				} catch (MktServiceException e) {
 					Logger.error("Unable to sync lead with new score:%s",
 							e.getMessage());
 				}
 			}
 		}
 		return leadRecord;
 	}
 
 	public LeadRecord testAddLeadScores(LeadRecord leadRecord) {
 		Map attrMap = null;
 		ArrayOfAttribute aoAttribute = leadRecord.getLeadAttributeList();
 		if (aoAttribute != null) {
 			attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 			if (attrMap != null && !attrMap.isEmpty()) {
 				Set keySet = attrMap.keySet();
 				String ds = attrMap.get("DemographicStore").toString();
 				String bs = attrMap.get("BehaviorScore").toString();
 				Float dsF = 0F;
 				Float bsF = 0F;
 				if (ds != null) {
 					dsF = Float.valueOf(ds);
 				}
 				if (bs != null) {
 					bsF = Float.valueOf(bs);
 				}
 				String newScore = String.valueOf(dsF + bsF);
 				HashMap newAttrs = new HashMap();
 				newAttrs.put("MyNewScore", newScore);
 
 				LeadRecord newLeadRecord = MktowsUtil.newLeadRecord(
 						leadRecord.getId(), null, null, null, newAttrs);
 				return newLeadRecord;
 			}
 		}
 		return null;
 	}
 
 	public void testGetCampaignsForSource() {
 
 		List<CampaignRecord> campaignRecords = null;
 		try {
 			campaignRecords = this.client.getCampaignsForSource();
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (campaignRecords != null) {
 			for (CampaignRecord item : campaignRecords) {
 				System.out.println("Campaign name: " + item.getName()
 						+ ",  ID: " + item.getId());
 			}
 		}
 	}
 
 	public void testGetLead() {
 
 		List<LeadRecord> leadRecords = null;
 		try {
 			leadRecords = this.client.getLead(LeadKeyRef.EMAIL,
 					"rrajamani@marketo.com");
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (leadRecords != null) {
 			Map<String, Object> attrMap = null;
 			for (LeadRecord item : leadRecords) {
 				System.out.println("Lead Id: " + item.getId() + ",  Email: "
 						+ item.getEmail());
 				ArrayOfAttribute aoAttribute = item.getLeadAttributeList();
 				if (aoAttribute != null) {
 					attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 					if (attrMap != null && !attrMap.isEmpty()) {
 						Set<String> keySet = attrMap.keySet();
 						for (String key : keySet) {
 							System.out
 									.println("    Attribute name: " + key
 											+ ", value: "
 											+ attrMap.get(key).toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void testGetCustomObjects() {
 
 		List<CustomObj> listCustomObjects = null;
 		try {
 			listCustomObjects = this.client.getCustomObjects("Roadshow");
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		for (CustomObj obj : listCustomObjects) {
 			System.out.println("CustomObject " + obj.getClass());
 		}
 	}
 
 	public void testSyncCustomObjects() {
 
 		List<SyncCustomObjStatus> listCustomObjects = null;
 		try {
 			listCustomObjects = this.client.syncCustomObject("Roadshow");
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		for (SyncCustomObjStatus obj : listCustomObjects) {
 			System.out.println("CustomObject sync status - " + obj.getStatus()
 					+ obj.getError());
 		}
 	}
 
 	public void testGetMObjects() {
 
 		List<MObjCriteria> listMObjCriteria = new ArrayList<MObjCriteria>();
 		MObjCriteria c = MktowsUtil.objectFactory.createMObjCriteria();
 		// c.setAttrName("Role");
 		// c.setComparison(ComparisonEnum.EQ);
 		// c.setAttrValue("Admin");
 		// listMObjCriteria.add(c);
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		List<MObject> listMObjects = null;
 		try {
 			listMObjects = this.client.getMObjects("Opportunity", null, null,
 					null, null, posHolder);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		for (MObject mObj : listMObjects) {
 			System.out.println("MObject " + mObj.getType() + ", with ID "
 					+ mObj.getId());
 		}
 	}
 
 	public void testDescMObjects() {
 
 		List<MObjCriteria> listMObjCriteria = new ArrayList<MObjCriteria>();
 		ParamsDescribeMObject c = MktowsUtil.objectFactory
 				.createParamsDescribeMObject();
 		c.setObjectName("Opportunity");
 		// listMObjCriteria.add(c);
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		SuccessDescribeMObject listMObjects = null;
 		listMObjects = this.client.describeMObject(c);
 		System.out.println("Received DescMobj output");
 	}
 
 	public void testRequestCampaign() {
 
 		final String myCampName = "Product Seminar - Summer";
 		final String leadEmail = "scooby1@marketo.com";
 		// Find the available campaigns
 		List<CampaignRecord> campaignRecords = null;
 		try {
 			campaignRecords = this.client.getCampaignsForSource();
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		int myCampId = 0;
 		if (campaignRecords != null) {
 			// Find ID for campaign of interest
 			for (CampaignRecord item : campaignRecords) {
 				if (item.getName() == myCampName) {
 					myCampId = item.getId();
 					break;
 				}
 			}
 		}
 
 		if (myCampId == 0) {
 			System.out.println("Campaign not found: " + myCampName);
 			return;
 		}
 
 		// Find the lead ID for lead(s) that need to be added to the campaign
 		List<LeadRecord> leadRecords = null;
 		try {
 			leadRecords = this.client.getLead(LeadKeyRef.EMAIL, leadEmail);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		int leadId = 0;
 		if (leadRecords != null) {
 			for (LeadRecord item : leadRecords) {
 				leadId = item.getId();
 				break;
 			}
 		}
 
 		if (leadId == 0) {
 			System.out.println("Lead not found: " + leadEmail);
 			return;
 		}
 
 		// Request that lead(s) be added to the campaign
 		LeadKey leadKey = MktowsUtil.objectFactory.createLeadKey();
 		leadKey.setKeyType(LeadKeyRef.IDNUM);
 		leadKey.setKeyValue(new Integer(leadId).toString());
 		List<LeadKey> leadList = new ArrayList<LeadKey>();
 		leadList.add(leadKey);
 		boolean success = false;
 		try {
 			success = this.client.requestCampaign(myCampId, leadList);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (success) {
 			System.out.println("Lead " + leadId + " added to campaign "
 					+ myCampName);
 		} else {
 			System.out.println("Failed to add lead " + leadId + " to campaign "
 					+ myCampName);
 		}
 	}
 
 	public void testRequestCampaign2() {
 
 		final String myProgName = "Customer Query";
 		final String myCampName = "Send Crendentials";
 		final String leadEmail = "aahsan@mkto.com";
 		// Find the available campaigns
 
 		// Request that lead(s) be added to the campaign
 		List<Attrib> tokenList = new ArrayList<Attrib>();
 		Attrib token = null;
 		token = MktowsUtil.objectFactory.createAttrib();
 		token.setName("Username");
 		token.setValue("AAHSAN");
 		tokenList.add(token);
 
 		token = MktowsUtil.objectFactory.createAttrib();
 		token.setName("Password");
 		token.setValue("marketo12345");
 		tokenList.add(token);
 
 		LeadKey leadKey = MktowsUtil.objectFactory.createLeadKey();
 		leadKey.setKeyType(LeadKeyRef.EMAIL);
 		leadKey.setKeyValue(leadEmail);
 		List<LeadKey> leadList = new ArrayList<LeadKey>();
 		leadList.add(leadKey);
 
 		boolean success = false;
 		try {
 			success = this.client.requestCampaign(myProgName, myCampName,
 					leadList, tokenList);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (success) {
 			System.out.println("Lead " + leadEmail + " added to campaign "
 					+ myCampName);
 		} else {
 			System.out.println("Failed to add lead " + leadEmail
 					+ " to campaign " + myCampName);
 		}
 	}
 
 	public void testScheduleCampaign() {
 
 		final String programName = "Spring 2012";
 		final String campaignName = "Send Invite";
 		// Find the available campaigns
 
 		// Request that lead(s) be added to the campaign
 		List<Attrib> tokenList = new ArrayList<Attrib>();
 		Attrib token = null;
 		token = MktowsUtil.objectFactory.createAttrib();
 		token.setName("{{my.CustToken1}}");
 		token.setValue("Override value 3");
 		tokenList.add(token);
 
 		token = MktowsUtil.objectFactory.createAttrib();
 		token.setName("{{my.CustToken1}}");
 		token.setValue("Override value 4");
 		tokenList.add(token);
 
 		GregorianCalendar cal = new GregorianCalendar();
 		cal.setTime(new Date());
 
 		boolean success = false;
 		try {
 			XMLGregorianCalendar runAt = DatatypeFactory.newInstance()
 					.newXMLGregorianCalendar(cal);
 			success = this.client.scheduleCampaign(programName, campaignName,
 					runAt, tokenList);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		} catch (DatatypeConfigurationException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		}
 		if (success) {
 			System.out.println("Scheduled campaign " + programName + "."
 					+ campaignName);
 		} else {
 			System.out.println("Failed to scheduled campaign " + programName
 					+ "." + campaignName);
 		}
 	}
 
 	public void testGetLeadActivity() {
 
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		Date lastestCreatedAt = new Date();
 		List<ActivityType> filter = new ArrayList<ActivityType>();
 		filter.add(ActivityType.VISIT_WEBPAGE);
 		filter.add(ActivityType.FILL_OUT_FORM);
 		filter.add(ActivityType.OPEN_EMAIL);
 		filter.add(ActivityType.OPEN_SALES_EMAIL);
 		filter.add(ActivityType.CLICK_EMAIL);
 		filter.add(ActivityType.CLICK_SALES_EMAIL);
 		filter.add(ActivityType.NEW_LEAD);
 		List<ActivityRecord> activityRecords = null;
 		try {
 			activityRecords = this.client.getLeadActivity(LeadKeyRef.IDNUM,
 					"84618", 10, lastestCreatedAt, null, filter, posHolder);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (activityRecords != null) {
 			Map<String, Object> attrMap = null;
 			for (ActivityRecord item : activityRecords) {
 				String wsdlTS = item.getActivityDateTime().toString();
 				Date localDT = MktowsUtil.w3cDateToDateObject(item
 						.getActivityDateTime());
 				DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				String localTS = fmt.format(localDT);
 				System.out.println("Activity: " + item.getActivityType()
 						+ ",  WSDL Timestamp: " + wsdlTS
 						+ ",  LOCAL Timestamp: " + localTS);
 				ArrayOfAttribute aoAttribute = item.getActivityAttributes();
 				if (aoAttribute != null) {
 					attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 					if (attrMap != null && !attrMap.isEmpty()) {
 						Set<String> keySet = attrMap.keySet();
 						for (String key : keySet) {
 							System.out
 									.println("    Attribute name: " + key
 											+ ", value: "
 											+ attrMap.get(key).toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void testGetLeadChanges() {
 
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		Calendar cal = new GregorianCalendar(2010, Calendar.MAY, 1, 0, 0, 0);
 		Date oldestCreatedAt = cal.getTime();
 		List<ActivityType> filter = new ArrayList<ActivityType>();
		filter.add(ActivityType.VISIT_WEBPAGE);
 		filter.add(ActivityType.FILL_OUT_FORM);
		filter.add(ActivityType.OPEN_EMAIL);
		filter.add(ActivityType.OPEN_SALES_EMAIL);
		filter.add(ActivityType.CLICK_EMAIL);
		filter.add(ActivityType.CLICK_SALES_EMAIL);
		filter.add(ActivityType.NEW_LEAD);
 		List<LeadChangeRecord> leadChangeRecords = null;
 		try {
 			leadChangeRecords = this.client.getLeadChanges(100, null,
 					oldestCreatedAt, filter, posHolder);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (leadChangeRecords != null) {
 			Map<String, Object> attrMap = null;
 			for (LeadChangeRecord item : leadChangeRecords) {
 				String wsdlTS = item.getActivityDateTime().toString();
 				Date localDT = MktowsUtil.w3cDateToDateObject(item
 						.getActivityDateTime());
 				DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				String localTS = fmt.format(localDT);
 				System.out.println("Lead: " + item.getId() + ",  Activity: "
 						+ item.getActivityType() + ",  WSDL Timestamp: "
 						+ wsdlTS + ",  LOCAL Timestamp: " + localTS);
 				ArrayOfAttribute aoAttribute = item.getActivityAttributes();
 				if (aoAttribute != null) {
 					attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 					if (attrMap != null && !attrMap.isEmpty()) {
 						Set<String> keySet = attrMap.keySet();
 						for (String key : keySet) {
 							System.out
 									.println("    Attribute name: " + key
 											+ ", value: "
 											+ attrMap.get(key).toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void testGetMultipleLeadsEmail() {
 		List<LeadRecord> leadRecords = null;
 		try {
 			List<String> emails = new ArrayList<String>();
 			emails.add("rrajamani@marketo.com");
 			emails.add("lboyle@uog.com");
 			leadRecords = this.client.getMultipleLeads(emails);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (leadRecords != null) {
 			Map<String, Object> attrMap = null;
 			for (LeadRecord item : leadRecords) {
 				System.out.println("Lead Id: " + item.getId() + ",  Email: "
 						+ item.getEmail());
 				ArrayOfAttribute aoAttribute = item.getLeadAttributeList();
 				if (aoAttribute != null) {
 					attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 					if (attrMap != null && !attrMap.isEmpty()) {
 						Set<String> keySet = attrMap.keySet();
 						for (String key : keySet) {
 							System.out
 									.println("    Attribute name: " + key
 											+ ", value: "
 											+ attrMap.get(key).toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void testGetMultipleLeadsLastUpdatedAt() {
 
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		Calendar cal = new GregorianCalendar(2010, Calendar.MAY, 1, 0, 0, 0);
 		Date lastUpdatedAt = cal.getTime();
 		List<LeadRecord> leadRecords = null;
 		try {
 			leadRecords = this.client.getMultipleLeads(100, lastUpdatedAt,
 					posHolder);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (leadRecords != null) {
 			Map<String, Object> attrMap = null;
 			for (LeadRecord item : leadRecords) {
 				System.out.println("Lead Id: " + item.getId() + ",  Email: "
 						+ item.getEmail());
 				ArrayOfAttribute aoAttribute = item.getLeadAttributeList();
 				if (aoAttribute != null) {
 					attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 					if (attrMap != null && !attrMap.isEmpty()) {
 						Set<String> keySet = attrMap.keySet();
 						for (String key : keySet) {
 							System.out
 									.println("    Attribute name: " + key
 											+ ", value: "
 											+ attrMap.get(key).toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void testGetMultipleLeadsUnsubscribedFlag() {
 
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		Calendar cal = new GregorianCalendar(2010, Calendar.MAY, 1, 0, 0, 0);
 		Date lastUpdatedAt = cal.getTime();
 		List<String> leadAttrs = new ArrayList<String>();
 		leadAttrs.add("Unsubscribed");
 		leadAttrs.add("UnsubscribedReason");
 		List<LeadRecord> leadRecords = null;
 		try {
 			leadRecords = this.client.getMultipleLeads(100, lastUpdatedAt,
 					posHolder, leadAttrs);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (leadRecords != null) {
 			Map<String, Object> attrMap = null;
 			for (LeadRecord item : leadRecords) {
 				System.out.println("Lead Id: " + item.getId() + ",  Email: "
 						+ item.getEmail());
 				ArrayOfAttribute aoAttribute = item.getLeadAttributeList();
 				if (aoAttribute != null) {
 					attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 					if (attrMap != null && !attrMap.isEmpty()) {
 						Set<String> keySet = attrMap.keySet();
 						for (String key : keySet) {
 							System.out
 									.println("    Attribute name: " + key
 											+ ", value: "
 											+ attrMap.get(key).toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void testSyncLead() {
 
 		HashMap<String, String> attrs = new HashMap<String, String>();
 		attrs.put("FirstName", "Sam");
 		attrs.put("LastName", "Haggy");
 		LeadRecord leadRec = MktowsUtil.newLeadRecord(null,
 				"shaggy@marketo.com", null, null, attrs);
 		ResultSyncLead result = null;
 		try {
 			result = client.syncLead(leadRec, null, true);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		leadRec = result.getLeadRecord();
 		SyncStatus sts = result.getSyncStatus();
 		if (sts.getStatus() == LeadSyncStatus.CREATED) {
 			System.out.println("Lead CREATED with Id " + sts.getLeadId());
 		} else if (sts.getStatus() == LeadSyncStatus.UPDATED) {
 			System.out.println("Lead UPDATED with Id " + sts.getLeadId());
 		} else {
 			System.out.println("Unexpected lead sync status");
 		}
 	}
 
 	public void testSyncMultipleLeads() {
 
 		List<LeadRecord> leadRecList = new ArrayList<LeadRecord>();
 		Date dt = new Date();
 		for (int i = 1; i <= 3; ++i) {
 			HashMap<String, String> attrs = new HashMap<String, String>();
 			attrs.put("FirstName", "FName" + dt + i);
 			attrs.put("LastName", "LName" + dt + i);
 			LeadRecord leadRec = MktowsUtil.newLeadRecord(null, "testemail"
 					+ dt + i + "@marketo.com", null, null, attrs);
 			leadRecList.add(leadRec);
 		}
 		List<SyncStatus> syncStsList = null;
 		try {
 			syncStsList = client.syncMultipleLeads(leadRecList, true);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		for (SyncStatus sts : syncStsList) {
 			if (sts.getStatus() == LeadSyncStatus.CREATED) {
 				System.out.println("Lead CREATED with Id " + sts.getLeadId());
 			} else if (sts.getStatus() == LeadSyncStatus.UPDATED) {
 				System.out.println("Lead UPDATED with Id " + sts.getLeadId());
 			} else {
 				System.out.println("Unexpected lead sync status");
 			}
 		}
 	}
 
 	public void testListMObjects() {
 
 		List<String> objectNames = null;
 		try {
 			objectNames = this.client.listMObjects();
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		for (String name : objectNames) {
 			System.out.println("Object name: " + name);
 		}
 	}
 
 	public void testMergeLeads() {
 
 		String ID_ATTR = "IDNUM"; // Lookup attribute name
 		// The winning lead
 		List<Attribute> winningLead = new ArrayList<Attribute>();
 		winningLead.add(MktowsUtil.newAttribute(ID_ATTR, "2"));
 
 		// The losing leads
 		List<List<Attribute>> losingLeadList = new ArrayList<List<Attribute>>();
 		List<Attribute> loser = new ArrayList<Attribute>();
 		// Lead 1
 		loser = new ArrayList<Attribute>();
 		loser.add(MktowsUtil.newAttribute(ID_ATTR, "11"));
 		losingLeadList.add(loser);
 		// Lead 2
 		loser = new ArrayList<Attribute>();
 		loser.add(MktowsUtil.newAttribute(ID_ATTR, "12"));
 		losingLeadList.add(loser);
 
 		MergeStatus status = null;
 		try {
 			status = this.client.mergeLeads(winningLead, losingLeadList);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (status.getStatus() == LeadMergeStatusEnum.MERGED) {
 			System.out.println("Winning lead Id " + status.getWinningLeadId());
 			List<Integer> loserIds = status.getLosingLeadIdList()
 					.getIntegerItem();
 			for (Integer intItem : loserIds) {
 				System.out.println("Losing leads Id " + intItem);
 			}
 		} else {
 			System.out.println("Lead merge status is " + status.getStatus());
 			System.out.println("Error: " + status.getError());
 		}
 	}
 
 	public void testImportList() {
 		ImportToListStatusEnum status = null;
 
 		Date dt = new Date();
 		List<String> rows = new ArrayList<String>();
 		rows.add("em" + dt.getTime() + "_1@mkto.com, Bob_" + dt.getTime()
 				+ ",Dole_" + dt.getTime());
 		rows.add("em" + dt.getTime() + "_2@mkto.com, Bob_" + dt.getTime()
 				+ ",Dole_" + dt.getTime());
 		String header = "\"Email Address\",\"First Name\",\"Last Name\"";
 		try {
 			status = this.client.importToList(ImportToListModeEnum.UPSERTLEADS,
 					"Customer Query", "Some Leads", header, rows, null);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (status == ImportToListStatusEnum.PROCESSING) {
 			System.out.println("Import is processing");
 		} else {
 			System.out.println("Import failed");
 		}
 	}
 
 	public void testImportList2() {
 		ImportToListStatusEnum status = null;
 
 		List<String> rows = new ArrayList<String>();
 		rows.add("aahsan99@gmail.com,Override value 1-1,Override value 1-2");
 		rows.add("aahsan99@yahoo.com,Override value 2-1,Override value 2-2");
 		String header = "Email Address,my.CustToken1,my.CustToken2";
 		try {
 			status = this.client.importToList(ImportToListModeEnum.LISTONLY,
 					"Spring 2012", "Invitees", header, rows, "Send Invite");
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (status == ImportToListStatusEnum.PROCESSING) {
 			System.out.println("Import is processing");
 		} else {
 			System.out.println("Import failed");
 		}
 	}
 
 	public void testGetMultipleLeadsStaticList() {
 
 		StreamPostionHolder posHolder = new StreamPostionHolder();
 		List<LeadRecord> leadRecords = new ArrayList<LeadRecord>();
 		try {
 			do {
 				this.client.getMultipleLeads(2,
 						"RajExportableProgram.listForTesting", posHolder,
 						leadRecords, null);
 				System.out
 						.println("Retrieved " + leadRecords.size() + " leads");
 			} while (leadRecords.size() != 0);
 		} catch (MktowsClientException e) {
 			System.out.println("Exception occurred: " + e.getMessage());
 			return;
 		} catch (MktServiceException e) {
 			System.out.println("Exception occurred: " + e.getLongMessage());
 			return;
 		}
 		if (leadRecords != null) {
 			Map<String, Object> attrMap = null;
 			for (LeadRecord item : leadRecords) {
 				System.out.println("Lead Id: " + item.getId() + ",  Email: "
 						+ item.getEmail());
 				ArrayOfAttribute aoAttribute = item.getLeadAttributeList();
 				if (aoAttribute != null) {
 					attrMap = MktowsUtil.getLeadAttributeMap(aoAttribute);
 					if (attrMap != null && !attrMap.isEmpty()) {
 						Set<String> keySet = attrMap.keySet();
 						for (String key : keySet) {
 							System.out
 									.println("    Attribute name: " + key
 											+ ", value: "
 											+ attrMap.get(key).toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
