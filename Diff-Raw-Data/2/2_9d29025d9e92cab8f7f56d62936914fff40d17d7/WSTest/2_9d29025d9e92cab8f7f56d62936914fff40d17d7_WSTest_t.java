 package com.orangeleap.tangerine.test.ws;
 
 import com.orangeleap.tangerine.service.exception.ConstituentValidationException;
 import com.orangeleap.tangerine.service.ws.OrangeLeapWSV2;
 import com.orangeleap.tangerine.service.ws.exception.InvalidRequestException;
 import com.orangeleap.tangerine.test.BaseTest;
 import com.orangeleap.tangerine.ws.schema.v2.AbstractCustomizableEntity.CustomFieldMap;
 import com.orangeleap.tangerine.ws.schema.v2.AddCommunicationHistoryRequest;
 import com.orangeleap.tangerine.ws.schema.v2.AddPickListItemByNameRequest;
 import com.orangeleap.tangerine.ws.schema.v2.AddPickListItemByNameResponse;
 import com.orangeleap.tangerine.ws.schema.v2.Address;
 import com.orangeleap.tangerine.ws.schema.v2.BulkAddCommunicationHistoryRequest;
 import com.orangeleap.tangerine.ws.schema.v2.CommunicationHistory;
 import com.orangeleap.tangerine.ws.schema.v2.Constituent;
 import com.orangeleap.tangerine.ws.schema.v2.Email;
 import com.orangeleap.tangerine.ws.schema.v2.FindConstituentsRequest;
 import com.orangeleap.tangerine.ws.schema.v2.GetConstituentGiftRequest;
 import com.orangeleap.tangerine.ws.schema.v2.GetConstituentGiftResponse;
 import com.orangeleap.tangerine.ws.schema.v2.GetConstituentRecurringGiftRequest;
 import com.orangeleap.tangerine.ws.schema.v2.GetConstituentRecurringGiftResponse;
 import com.orangeleap.tangerine.ws.schema.v2.GetPickListByNameRequest;
 import com.orangeleap.tangerine.ws.schema.v2.GetPickListByNameResponse;
 import com.orangeleap.tangerine.ws.schema.v2.Gift;
 import com.orangeleap.tangerine.ws.schema.v2.PaymentSource;
 import com.orangeleap.tangerine.ws.schema.v2.PaymentType;
 import com.orangeleap.tangerine.ws.schema.v2.PicklistItem;
 import com.orangeleap.tangerine.ws.schema.v2.RecurringGift;
 import com.orangeleap.tangerine.ws.schema.v2.SaveOrUpdateConstituentRequest;
 import com.orangeleap.tangerine.ws.schema.v2.SaveOrUpdateConstituentResponse;
 import com.orangeleap.tangerine.ws.schema.v2.SaveOrUpdateGiftRequest;
 import com.orangeleap.tangerine.ws.schema.v2.SaveOrUpdateGiftResponse;
 import com.orangeleap.tangerine.ws.schema.v2.SaveOrUpdateRecurringGiftRequest;
 import com.orangeleap.tangerine.ws.schema.v2.SaveOrUpdateRecurringGiftResponse;
 import java.math.BigDecimal;
 import org.junit.Assert;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.validation.BindException;
 import org.springframework.webflow.execution.RequestContext;
 import org.springframework.webflow.test.MockRequestContext;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 
 public class WSTest extends BaseTest {
 	private RequestContext mockRequestContext;
 
 	@Autowired
 	public OrangeLeapWSV2 constituentEndpointV2;
 
 	@BeforeMethod
 	public void setupMocks() {
 		mockRequestContext = new MockRequestContext();
 	}
 
 	@Test(groups = "soapAPITests")
 	void testAutowired() {
 		Assert.assertNotNull(constituentEndpointV2);
 	}
 
 	@Test(groups = "soapAPITests")
 	void testAddConstituent() {
 		try {
 			SaveOrUpdateConstituentRequest request = new SaveOrUpdateConstituentRequest();
 			SaveOrUpdateConstituentResponse response = null;
 
 			Constituent c = new Constituent();
 
 			//
 			// Test without constituent type
 			c.setFirstName("Test");
 			c.setLastName("User");
 
 			request.setConstituent(c);
 
 			try {
 				response = constituentEndpointV2.maintainConstituent(request);
 			} catch (ConstituentValidationException e) {
 				//
 				// We should not get here
 				Assert.assertTrue(false);
 			} catch (InvalidRequestException e) {
 				Assert.assertTrue(true);
 			}
 
 			//
 			// Test with a constituent type
 			c.setConstituentType("individual");
 			request.setConstituent(c);
 			try {
 				response = constituentEndpointV2.maintainConstituent(request);
 			} catch (ConstituentValidationException e) {
 				//
 				// Should not get here
 				Assert.assertTrue(false);
 			} catch (InvalidRequestException e) {
 				//
 				// Should not get here either
 				Assert.assertTrue(false);
 			}
 			
 			//
 			// Test to make sure I got back a constituent id
 			Assert.assertNotNull(response);
 			Constituent returned = response.getConstituent();
 			Assert.assertNotNull(returned);
 			Assert.assertTrue(returned.getId() != null && returned.getId() > 0);
 		} catch (BindException e) {
 			//
 			// We better not get here!
 			Assert.assertTrue(false);
 		}
 
 	}
 
 	@Test(groups = "soapAPITests", dependsOnGroups = { "testReadPaymentSource" })
 	void testAddGift() {
 		SaveOrUpdateGiftRequest request = new SaveOrUpdateGiftRequest();
 		SaveOrUpdateGiftResponse response = null;
 		com.orangeleap.tangerine.ws.schema.v2.Gift g = new com.orangeleap.tangerine.ws.schema.v2.Gift();
 		
 		g.setConstituentId(100L);
 		g.setAmount(new BigDecimal(100.00));
 		request.setGift(g);
 		
 		//
 		// test without a paymentsource
 		try {
 			response = constituentEndpointV2.maintainGift(request);
 		} catch (InvalidRequestException e) {
 			//
 			// This should fail because there is no payment source
 			logger.error(e.getMessage());
 			Assert.assertTrue(true);
 		}
 		
 		PaymentSource paySource = new PaymentSource();
 		paySource.setPaymentType(PaymentType.CREDIT_CARD);
 		paySource.setCreditCardHolderName("Leo DAngelo");
 		paySource.setCreditCardNumber("4111111111111111");
 		paySource.setCreditCardExpirationMonth(11);
 		paySource.setCreditCardExpirationYear(2011);
 		g.setPaymentSource(paySource);
 		request.setGift(g);
 		try {
 			response = constituentEndpointV2.maintainGift(request);
 		} catch (InvalidRequestException e) {
 			//
 			// This should fail because the constituent id is missing from the paymentsource
 			logger.error(e.getMessage());
 			Assert.assertTrue(true);
 		}		
 
 		paySource.setConstituentId(100L);
 		request.setGift(g);
 		try {
 			response = constituentEndpointV2.maintainGift(request);
 		} catch (InvalidRequestException e) {
 			//
 			// This should fail because the credit card type is missing from the paymentsource
 			logger.error(e.getMessage());
 			Assert.assertTrue(true);
 		}		
 		
 		paySource.setCreditCardType("bogus");
 		request.setGift(g);
 		try {
 			response = constituentEndpointV2.maintainGift(request);
 		} catch (InvalidRequestException e) {
 			//
 			// This should fail because the credit card type is bogus
 			logger.error(e.getMessage());
 			Assert.assertTrue(true);
 		}		
 		
 		paySource.setCreditCardType("Visa");
 		try {
 			response = constituentEndpointV2.maintainGift(request);
 			Assert.assertTrue(false);
 		} catch (InvalidRequestException e) {
 			//
 			//  This should fail because we don't have a constituent id on the request
 			logger.error(e.getMessage());
 			Assert.assertTrue(true);
 		}		
 		
 		g.setPaymentType(PaymentType.CREDIT_CARD);
 		request.setConstituentId(100L);
 		
 		try {
 			response = constituentEndpointV2.maintainGift(request);
 			Assert.assertTrue(true);
 		} catch (InvalidRequestException e) {
 			//
 			//  This should pass
 			logger.error(e.getMessage());
 			Assert.assertTrue(false);
 		}				
 		
 	}
 
 	@Test(groups = "soapAPITests")
 	void testAddCommunicationHistory() {
 		AddCommunicationHistoryRequest request = new AddCommunicationHistoryRequest();
 		CommunicationHistory ch = new CommunicationHistory();
 		
 		request.setConstituentId(100L);
 		ch.setConstituentId(100L);
 		ch.setComments("This is a test");
 		ch.setCommunicationHistoryType("");
 
 		try {
 			constituentEndpointV2.addCommunicationHistory(request);
 		} catch (InvalidRequestException e) {
 			Assert.assertTrue(false);			
 		}
 		Assert.assertTrue(true);
 	}
 
 	@Test(groups = "soapAPITests")
 	void testBulkAddCommunicationHistory() {
 		BulkAddCommunicationHistoryRequest request = new BulkAddCommunicationHistoryRequest();
 		CommunicationHistory ch = new CommunicationHistory();
 		
 		request.getConstituentId().add(100L);
 		request.getConstituentId().add(200L);
 		ch.setConstituentId(100L);
 		ch.setComments("This is a test");
 		ch.setCommunicationHistoryType("");
 		
 		try {
 			constituentEndpointV2.bulkAddCommunicationHistory(request);
 		} catch (InvalidRequestException e) {
 			Assert.assertTrue(false);			
 		}
 		Assert.assertTrue(true);		
 
 	}
 
 	@Test(groups = "soapAPITests")
 	void testFind() {
 		FindConstituentsRequest request = new FindConstituentsRequest();
 		com.orangeleap.tangerine.ws.schema.v2.FindConstituentsResponse response = null;
 		//
 		// test a bogus find request
 		try {
 			response = constituentEndpointV2.findConstituent(request);
 			Assert.assertTrue(false);
 		} catch (InvalidRequestException e1) {
 			Assert.assertTrue(true);
 		}
 
 		//
 		// Test with firstName LastName
 		request.setFirstName("Howdy");
 		request.setLastName("Doody");
 		try {
 			response = constituentEndpointV2.findConstituent(request);
 		} catch (InvalidRequestException e1) {
 			// TODO Auto-generated catch block
 			Assert.assertTrue(false);
 		}
 		Assert.assertNotNull(response);
 		Assert.assertNotNull(response.getConstituent());
 		Assert.assertTrue(response.getConstituent().size() > 0);
 
 		//
 		// Test with e-mail address
 		Email e = new Email();
 		e.setEmailAddress("");
 		request.setPrimaryEmail(e);
 		try {
 			response = constituentEndpointV2.findConstituent(request);
 		} catch (InvalidRequestException e1) {
 			Assert.assertTrue(false);
 		}
 		Assert.assertNotNull(response);
 		Assert.assertNotNull(response.getConstituent());
 		Assert.assertTrue(response.getConstituent().size() > 0);
 
 		//
 		// Make sure we don't find him with a different e-mail
 		e.setEmailAddress("dog@dog.com");
 		request.setPrimaryEmail(e);
 		try {
 			response = constituentEndpointV2.findConstituent(request);
 		} catch (InvalidRequestException e1) {
 			Assert.assertTrue(false);
 		}
 		Assert.assertNotNull(response);
 		Assert.assertNotNull(response.getConstituent());
 		Assert.assertTrue(response.getConstituent().size() == 0);
 
 		//
 		// See if we can find with an address
 		request.setFirstName("Pablo");
 		request.setLastName("");
 		e.setEmailAddress("");
 		request.setPrimaryEmail(e);
 
 		Address address = new Address();
 		address.setAddressLine1("8457 ACORN");
 		request.setPrimaryAddress(address);
 		try {
 			response = constituentEndpointV2.findConstituent(request);
 		} catch (InvalidRequestException e1) {
 			Assert.assertTrue(false);
 		}
 		Assert.assertNotNull(response);
 		Assert.assertNotNull(response.getConstituent());
 		Assert.assertTrue(response.getConstituent().size() > 0);
 
 	}
 	
 	@Test(groups = "soapAPITests")
 	void testAddPickListItem() {
 		String picklistname = "currencyCode";
 		int oldcount = 0;
 		
 		GetPickListByNameRequest getRequest = new GetPickListByNameRequest();
 		GetPickListByNameResponse getResponse = new GetPickListByNameResponse();
 		
 		getRequest.setName(picklistname);
 		try {
 			getResponse = constituentEndpointV2.getPickListByName(getRequest);
 		} catch (InvalidRequestException e1) {
 			Assert.assertTrue(false);
 		}
 	
 		oldcount = getResponse.getPicklist().getPicklistItems().size();
 		
 		AddPickListItemByNameRequest addRequest = new AddPickListItemByNameRequest();
 		AddPickListItemByNameResponse addResponse = new AddPickListItemByNameResponse();
 		PicklistItem item = new PicklistItem();
 		item.setItemName("junk");
 		item.setDefaultDisplayValue("Testing adding picklist Items");
 		addRequest.setPicklistitem(item);
 		addRequest.setPicklistname(picklistname);
 		
 		
 		try {
 			addResponse = constituentEndpointV2.addPickListItem(addRequest);
 		} catch (InvalidRequestException e) {
 			Assert.assertTrue(false);
 		}
 		Assert.assertTrue(addResponse.getPicklist().getPicklistItems().size() > oldcount);		
 		
 	}
 	
	@Test(groups = "soapAPITests", dependsOnGroups = { "testSearchReadRecurringGifts" })
 	void testRecurringGiftAdd() 
 	{
 		SaveOrUpdateRecurringGiftRequest request = new SaveOrUpdateRecurringGiftRequest();
 		SaveOrUpdateRecurringGiftResponse response = new SaveOrUpdateRecurringGiftResponse();
 		RecurringGift rg = new RecurringGift();
 		
 		PaymentSource paySource = new PaymentSource();
 		paySource.setPaymentType(PaymentType.CREDIT_CARD);
 		paySource.setCreditCardHolderName("Leo DAngelo");
 		paySource.setCreditCardNumber("4111111111111111");
 		paySource.setCreditCardExpirationMonth(11);
 		paySource.setCreditCardExpirationYear(2011);
 		rg.setAmountPerGift(new BigDecimal(10.00));
 		rg.setPaymentSource(paySource);
 		
 		request.setConstituentId(100L);
 		request.setRecurringgift(rg);
 		
 		try {
 		response = constituentEndpointV2.maintainRecurringGift(request);
 		} catch (InvalidRequestException e1) {
 			Assert.assertTrue(false);
 		}
 		
 		Assert.assertNotNull(response.getRecurringgift());
 		Assert.assertNotNull(response.getRecurringgift().getId());		
 		Assert.assertTrue(response.getRecurringgift().getId() > 0);
 	}
 	
 	@Test(groups = "soapAPITests",dependsOnMethods="testRecurringGiftAdd")
 	void testRecurringGiftGet() {
 		GetConstituentRecurringGiftRequest request = new GetConstituentRecurringGiftRequest();
 		GetConstituentRecurringGiftResponse response = null;
 		
 		request.setConstituentId(100L);
 		
 		try {
 			response = constituentEndpointV2.getConstituentsRecurringGifts(request);
 		} catch (InvalidRequestException e1) {
 			Assert.assertTrue(false);
 		}
 		
 		//
 		// there should be a recurring gift cause we just put one in...
 		Assert.assertTrue(response.getRecurringgift().size() > 0);
 	}
 	
 	@Test(groups = "soapAPITests",dependsOnMethods="testAddGift") 
 	void testGetGift()
 	{
 		GetConstituentGiftRequest request = new GetConstituentGiftRequest();
 		GetConstituentGiftResponse response = new GetConstituentGiftResponse();
 		
 		request.setConstituentId(200L);
 		
 		try {
 			response = constituentEndpointV2.getConstituentsGifts(request);
 		} catch (InvalidRequestException e) {
 			Assert.assertTrue(false);
 		}
 		
 		Assert.assertTrue(response.getGift().size() > 0);
 		Gift g = response.getGift().get(0);
 		Assert.assertTrue(g.getDistributionLines().size() > 0);
 		CustomFieldMap map = g.getDistributionLines().get(0).getCustomFieldMap();
 //		Assert.assertTrue(map.getEntry().size() > 0);
 	}
 }
 
