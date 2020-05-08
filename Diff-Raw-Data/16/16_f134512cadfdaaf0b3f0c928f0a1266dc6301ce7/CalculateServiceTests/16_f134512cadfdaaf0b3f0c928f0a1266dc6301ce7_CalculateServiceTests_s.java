 package com.genability.test.client.api.service;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.genability.client.types.Account;
 import com.genability.client.types.CalculatedCostItem;
 import com.genability.client.types.Response;
 import com.genability.client.types.CalculatedCost;
 import com.genability.client.types.PropertyData;
 import com.genability.client.api.request.DeleteAccountRequest;
 import com.genability.client.api.request.GetCalculatedCostRequest;
 import com.genability.client.api.request.GetCalculationInputsRequest;
 import com.genability.client.api.service.AccountService;
 import com.genability.client.api.service.CalculateService;
 import com.genability.client.types.Tariff;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class CalculateServiceTests extends BaseServiceTests {
 
 	private static CalculateService calculateService;
 	private static AccountService accountService;
 
 	@BeforeClass
 	public static void runBeforeClass() {
 
 		calculateService = new CalculateService();
 		calculateService.setAppId(appId);
 		calculateService.setAppKey(appKey);
 		if(restApiServer != null) calculateService.setRestApiServer(restApiServer);
 		
 		accountService = new AccountService();
 		accountService.setAppId(appId);
 		accountService.setAppKey(appKey);
 		if(restApiServer != null) accountService.setRestApiServer(restApiServer);
 	}
 	
 	
 	@Test
 	public void testCalculateTariff512() {
 		
 		DateTime fromDateTime = new DateTime("2011-12-01T00:00:00.000-05:00");
 		DateTime toDateTime = new DateTime("2012-01-01T00:00:00.000-05:00");
 		
 		GetCalculatedCostRequest request = new GetCalculatedCostRequest();
 
 		request.setFromDateTime(fromDateTime);
 		request.setToDateTime(toDateTime);
 		request.setMasterTariffId(512l);
 
 		// Set the consumption property
 		PropertyData newProp3 = new PropertyData();
 		newProp3.setFromDateTime(fromDateTime);
 		newProp3.setToDateTime(toDateTime);
 		newProp3.setDataValue("220");
 		newProp3.setKeyName("consumption");
 		
 		// Set the cityLimits property
 		PropertyData newProp = new PropertyData();
 		newProp.setFromDateTime(fromDateTime);
 		newProp.setToDateTime(toDateTime);
 		newProp.setDataValue("Inside");
 		newProp.setKeyName("cityLimits");
 		
 		// Set the connectionType property
 		PropertyData newProp2 = new PropertyData();
 		newProp2.setFromDateTime(fromDateTime);
 		newProp2.setToDateTime(toDateTime);
 		newProp2.setDataValue("Primary");
 		newProp2.setKeyName("connectionType");
 		
 		request.addInput(newProp);
 		request.addInput(newProp2);
 		request.addInput(newProp3);
 		
 		callRunCalc("Test for master tariff 512",request);
 		
 	}
 	
 	
 	@Test
 	public void testCalculateTariff522() {
 		
 		// Where the tariff has a time zone (most do) you can use it to make sure your dates are the same
 		DateTime fromDateTime = new DateTime(2012, 1, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
 		DateTime toDateTime = new DateTime(2013, 1, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
 		
 		GetCalculatedCostRequest request = new GetCalculatedCostRequest();
 
 		request.setFromDateTime(fromDateTime);
 		request.setToDateTime(toDateTime);
 		request.setMasterTariffId(522l); // PGE E1 - residential tariff
 		request.setDetailLevel(GetCalculatedCostRequest.DETAIL_LEVEL_CHARGE_TYPE);
 		request.setGroupBy(GetCalculatedCostRequest.GROUP_BY_MONTH);
 
 		// Set the territoryId property
 		PropertyData newProp2 = new PropertyData();
 		newProp2.setFromDateTime(fromDateTime);
 		newProp2.setToDateTime(toDateTime);
 		newProp2.setDataValue("3534"); //Baseline Region P - 3534
 		newProp2.setKeyName("territoryId");
 		
 		request.addInput(newProp2);
 		
 		//
 		// Create consumption inputs for each hour of the day, first for 
 		// weekdays then for weekends.
 		//
 		DateTime propertyStartDateTime = new DateTime(fromDateTime);
 		while(propertyStartDateTime.isBefore(toDateTime)) {
 			
 			for(int hour = 0; hour < 24; hour++) {
 				
 				// Set the consumption property
 				PropertyData weekdayProp = new PropertyData();
 				weekdayProp.setFromDateTime(propertyStartDateTime);
 				weekdayProp.setToDateTime(propertyStartDateTime.plusMonths(1));
 				weekdayProp.setPeriod("1:5e " + hour + "H");
 				weekdayProp.setDataValue("0.5");
 				weekdayProp.setKeyName("consumption");
 				
 				request.addInput(weekdayProp);
 	
 				PropertyData weekendProp = new PropertyData();
 				weekendProp.setFromDateTime(fromDateTime);
 				weekendProp.setToDateTime(toDateTime);
 				weekendProp.setPeriod("6:7e " + hour + "H");
 				weekendProp.setDataValue("0.5");
 				weekendProp.setKeyName("consumption");
 				
 				request.addInput(weekendProp);
 				
 				propertyStartDateTime = propertyStartDateTime.plusMonths(1);
 				
 			}
 		}
 		
 		callRunCalc("Test for master tariff 522",request);
 		
 	}
 	
 	
 	@Test
 	public void testCalculateForAccount() {
 		
 		// We first create an account and specify a tariff as well as values
 		// for the tariff's properties.  We use masterTariffId 521 (PGE E-1) and
 		// the only required property is the territoryId.  
 		// We also set the zipCode as an additional example.
 		
 		Account addAccount = new Account();
 		addAccount.setAccountName("Java Client Lib Test Account - CAN DELETE");
 		
 		Map<String, PropertyData> properties = new HashMap<String, PropertyData>();
 		
 		PropertyData zipCodeData = new PropertyData();
 		zipCodeData.setKeyName("zipCode");
 		zipCodeData.setDataValue("94115");
 		properties.put(zipCodeData.getKeyName(), zipCodeData);
 
 		PropertyData territoryData = new PropertyData();
 		territoryData.setKeyName("territoryId");
 		territoryData.setDataValue("3538");
 		properties.put(territoryData.getKeyName(), territoryData);
 
 		addAccount.setProperties(properties);
 		
 		Tariff tariff = new Tariff();
 		tariff.setMasterTariffId(521l);
 		DateTime effDate = new DateTime(2012, 2, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
 		tariff.setEffectiveDate(effDate.toString());
 		List<Tariff> tariffs = new ArrayList<Tariff>();
 		tariffs.add(tariff);
 		addAccount.setTariffs(tariffs);
 							
 		Response<Account> restResponse = accountService.addAccount(addAccount);
 		
 		assertNotNull("restResponse null",restResponse);
 		assertEquals("bad status",restResponse.getStatus(),Response.STATUS_SUCCESS);
 		assertEquals("bad type",restResponse.getType(),Account.REST_TYPE);
 		assertTrue("bad count",restResponse.getCount() > 0);
 		
 		Account newAccount = null;
 		for(Account account : restResponse.getResults()) {
 			newAccount = account;
 			assertNotNull("accountId null",account.getAccountId());
 		}
 		
 		// Now we run the calculation for the new Account.  We set the date
 		// range for which to run the calc.
 		
 		// Where the tariff has a time zone (most do) you can use it to make sure your dates are the same
 		DateTime fromDateTime = new DateTime(2012, 1, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
 		DateTime toDateTime = new DateTime(2013, 1, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
 		
 		GetCalculatedCostRequest request = new GetCalculatedCostRequest();
 
 		request.setFromDateTime(fromDateTime);
 		request.setToDateTime(toDateTime);
 		
 		request.setAccountId(newAccount.getAccountId());
 				
 		PropertyData newProp2 = new PropertyData();
 		newProp2.setFromDateTime(fromDateTime);
 		newProp2.setToDateTime(toDateTime);
 		newProp2.setDataValue(newAccount.getAccountId());
 		newProp2.setKeyName("accountId");
		newProp2.setDataType("String");
 		
 		request.addInput(newProp2);
 		
 		callRunCalc("Test for calculateForAccount",request);
 		
 		// Delete the account we created to keep things clean
		DeleteAccountRequest deleteAccountrequest = new DeleteAccountRequest();
		request.setAccountId(newAccount.getAccountId());
		Response<Account> deleteResponse = accountService.deleteAccount(deleteAccountrequest);
 		assertEquals("bad status",deleteResponse.getStatus(),Response.STATUS_SUCCESS);
 		
 	}
 	
 	@Test
 	public void testGetCalculationInputs() {
 		
 		// Where the tariff has a time zone (most do) you can use it to make sure your dates are the same
 		DateTime fromDateTime = new DateTime(2012, 1, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
 		DateTime toDateTime = new DateTime(2013, 1, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
 		
 		GetCalculationInputsRequest request = new GetCalculationInputsRequest();
 
 		request.setFromDateTime(fromDateTime);
 		request.setToDateTime(toDateTime);
 		request.setMasterTariffId(522l);
 		request.setTerritoryId(3534l);
 		
 		Response<PropertyData> restResponse = calculateService.getCalculationInputs(request);
 
 		assertNotNull("restResponse null", restResponse);
 		assertEquals("bad status",restResponse.getStatus(),Response.STATUS_SUCCESS);
 		assertEquals("bad type",restResponse.getType(),PropertyData.REST_TYPE);
 		assertTrue("bad count",restResponse.getCount() > 0);
 		assertNotNull("results null",restResponse.getResults());
 		assertTrue("results were empty",restResponse.getResults().size() != 0);
 	}
 
 	
 	public void callRunCalc(String testCase, GetCalculatedCostRequest request) {
 		
 		Response<CalculatedCost> restResponse = calculateService.getCalculatedCost(request);
 		
 		assertNotNull("restResponse null",restResponse);
 		assertEquals("bad status",restResponse.getStatus(),Response.STATUS_SUCCESS);
 		assertEquals("bad type",restResponse.getType(),CalculatedCost.REST_TYPE);
 		assertTrue("bad count",restResponse.getCount() > 0);
 		assertNotNull("results null",restResponse.getResults());
 		assertEquals("results count",restResponse.getResults().size(),1);
 		
 		CalculatedCost calculatedCost = restResponse.getResults().get(0);
 		
 		assertNotNull("calculatedCost total",calculatedCost.getTotalCost());
 		log.debug("totalCost:" + calculatedCost.getTotalCost().toPlainString());
 		
 		for(CalculatedCostItem costItem : calculatedCost.getItems()) {
 			
 			assertNotNull("cost null",costItem.getCost());
 			
 		}
 		
 	}
 }
