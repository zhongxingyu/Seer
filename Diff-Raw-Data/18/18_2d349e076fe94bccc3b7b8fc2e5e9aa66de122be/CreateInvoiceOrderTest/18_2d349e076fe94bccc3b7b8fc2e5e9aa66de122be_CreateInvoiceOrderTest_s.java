 package se.sveaekonomi.webpay.integration.webservice.payment;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.w3c.dom.NodeList;
 
 import se.sveaekonomi.webpay.integration.WebPay;
 import se.sveaekonomi.webpay.integration.config.ConfigurationProviderTestData;
 import se.sveaekonomi.webpay.integration.config.SveaConfig;
 import se.sveaekonomi.webpay.integration.order.row.Item;
 import se.sveaekonomi.webpay.integration.response.webservice.CreateOrderResponse;
 import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
 import se.sveaekonomi.webpay.integration.util.constant.CURRENCY;
 import se.sveaekonomi.webpay.integration.util.test.TestingTool;
 import se.sveaekonomi.webpay.integration.webservice.handleorder.CloseOrder;
 import se.sveaekonomi.webpay.integration.webservice.helper.WebServiceXmlBuilder;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaCreateOrder;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaRequest;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaSoapBuilder;
 
 public class CreateInvoiceOrderTest {
 
 	@Test
 	public void testInvoiceForIndividualFromSE() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig()).addOrderRow(TestingTool.createExVatBasedOrderRow("1"))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber(TestingTool.DefaultTestIndividualNationalIdNumber)).setCountryCode(TestingTool.DefaultTestCountryCode)
 				.setOrderDate(TestingTool.DefaultTestDate).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setCurrency(TestingTool.DefaultTestCurrency).useInvoicePayment().doRequest();
 
 		assertEquals("Invoice", response.orderType);
 		assertTrue(response.isOrderAccepted());
 		assertTrue(response.sveaWillBuyOrder);
 		assertEquals(250.00, response.amount, 0);
 
 		// CustomerIdentity
 		assertEquals("Individual", response.customerIdentity.getCustomerType());
 		assertEquals("194605092222", response.customerIdentity.getNationalIdNumber());
 		assertEquals("Persson, Tess T", response.customerIdentity.getFullName());
 		assertEquals("Testgatan 1", response.customerIdentity.getStreet());
 		assertEquals("c/o Eriksson, Erik", response.customerIdentity.getCoAddress());
 		assertEquals("99999", response.customerIdentity.getZipCode());
 		assertEquals("Stan", response.customerIdentity.getCity());
 		assertEquals("SE", response.customerIdentity.getCountryCode());
 	}
 
 	@Test
 	public void testInvoiceRequestFailing() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig()).addOrderRow(TestingTool.createExVatBasedOrderRow("1"))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber("")).setCountryCode(TestingTool.DefaultTestCountryCode).setOrderDate(TestingTool.DefaultTestDate)
 				.setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setCurrency(TestingTool.DefaultTestCurrency).useInvoicePayment().doRequest();
 
 		assertFalse(response.isOrderAccepted());
 	}
 
 	@Test
 	public void testFormationOfDecimalsInCalculation() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig())
 				.addOrderRow(Item.orderRow().setArticleNumber("1").setQuantity(2.0).setAmountExVat(22.68).setDescription("Specification").setName("Prod").setVatPercent(6.0).setDiscountPercent(0.0))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber(TestingTool.DefaultTestIndividualNationalIdNumber)).setCountryCode(TestingTool.DefaultTestCountryCode)
 				.setOrderDate(TestingTool.DefaultTestDate).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setCurrency(TestingTool.DefaultTestCurrency).useInvoicePayment().doRequest();
 
 		assertTrue(response.isOrderAccepted());
 		assertEquals(48.08, response.amount, 0);
 	}
 
 	@Test
 	public void testInvoiceCompanySe() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig()).setCountryCode(TestingTool.DefaultTestCountryCode).setOrderDate(TestingTool.DefaultTestDate)
 				.setCurrency(TestingTool.DefaultTestCurrency).addCustomerDetails(TestingTool.createMiniCompanyCustomer()).addOrderRow(TestingTool.createExVatBasedOrderRow("1")).useInvoicePayment()
 				.doRequest();
 
 		assertTrue(response.isOrderAccepted());
 		assertTrue(response.sveaWillBuyOrder);
 		assertEquals("SE", response.customerIdentity.getCountryCode());
 	}
 
 	@Test
 	public void testInvoiceForIndividualFromNl() {
 		CreateOrderResponse response = WebPay
 				.createOrder(SveaConfig.getDefaultConfig())
 				.addOrderRow(TestingTool.createOrderRowNl())
 				.addCustomerDetails(
 						Item.individualCustomer().setBirthDate(1955, 03, 07).setInitials("SB").setName("Sneider", "Boasman").setStreetAddress("Gate", "42").setLocality("BARENDRECHT")
 								.setZipCode("1102 HG").setCoAddress("138")).setCountryCode(COUNTRYCODE.NL).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
 				.setOrderDate(TestingTool.DefaultTestDate).setCurrency(CURRENCY.EUR).useInvoicePayment().doRequest();
 
 		assertTrue(response.isOrderAccepted());
 		assertTrue(response.sveaWillBuyOrder);
 		assertEquals(212.00, response.amount, 0);
 		assertEquals("0", response.getResultCode());
 		assertEquals("Invoice", response.orderType);
 
 		// CustomerIdentity
 		assertEquals("Individual", response.customerIdentity.getCustomerType());
 		assertEquals("Sneider Boasman", response.customerIdentity.getFullName());
 		assertNull(response.customerIdentity.getPhoneNumber());
 		assertNull(response.customerIdentity.getEmail());
 		assertNull(response.customerIdentity.getIpAddress());
 		assertEquals("Gate 42", response.customerIdentity.getStreet());
 		assertEquals("138", response.customerIdentity.getCoAddress());
 		assertEquals("23", response.customerIdentity.getHouseNumber());
 		assertEquals("1102 HG", response.customerIdentity.getZipCode());
 		assertEquals("BARENDRECHT", response.customerIdentity.getCity());
 		assertEquals("NL", response.customerIdentity.getCountryCode());
 	}
 
 	@Test
 	public void testInvoiceDoRequestWithIpAddressSetSE() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig()).addOrderRow(TestingTool.createExVatBasedOrderRow("1")).addOrderRow(TestingTool.createExVatBasedOrderRow("2"))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber(TestingTool.DefaultTestIndividualNationalIdNumber).setIpAddress("123.123.123"))
 				.setCountryCode(TestingTool.DefaultTestCountryCode).setOrderDate(TestingTool.DefaultTestDate).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
 				.setCurrency(TestingTool.DefaultTestCurrency).useInvoicePayment().doRequest();
 
 		assertTrue(response.isOrderAccepted());
 	}
 
 	@Test
 	public void testInvoiceRequestUsingAmountIncVatWithZeroVatPercent() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig()).addOrderRow(TestingTool.createExVatBasedOrderRow("1")).addOrderRow(TestingTool.createExVatBasedOrderRow("2"))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber(TestingTool.DefaultTestIndividualNationalIdNumber)).setCountryCode(TestingTool.DefaultTestCountryCode)
 				.setOrderDate(TestingTool.DefaultTestDate).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setCurrency(TestingTool.DefaultTestCurrency)
 				.setCustomerReference(TestingTool.DefaultTestCustomerReferenceNumber).useInvoicePayment().doRequest();
 
 		assertTrue(response.isOrderAccepted());
 	}
 
 	@Test
 	public void testFailOnMissingCountryCodeOfCloseOrder() {
 		Long orderId = 0L;
 		SveaSoapBuilder soapBuilder = new SveaSoapBuilder();
 
 		SveaRequest<SveaCreateOrder> request = WebPay.createOrder(SveaConfig.getDefaultConfig()).addOrderRow(TestingTool.createExVatBasedOrderRow("1"))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber(TestingTool.DefaultTestIndividualNationalIdNumber)).setCountryCode(TestingTool.DefaultTestCountryCode)
 				.setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setOrderDate(TestingTool.DefaultTestDate).setCurrency(TestingTool.DefaultTestCurrency).useInvoicePayment()
 				.prepareRequest();
 
 		WebServiceXmlBuilder xmlBuilder = new WebServiceXmlBuilder();
 
 		String xml = xmlBuilder.getCreateOrderEuXml(request.request);
 		String url = SveaConfig.getTestWebserviceUrl().toString();
 		String soapMessage = soapBuilder.makeSoapMessage("CreateOrderEu", xml);
 		NodeList soapResponse = soapBuilder.createOrderEuRequest(soapMessage, url);
 		CreateOrderResponse response = new CreateOrderResponse(soapResponse);
 		orderId = response.orderId;
 
 		assertTrue(response.isOrderAccepted());
 
		soapBuilder = new SveaSoapBuilder();

 		CloseOrder closeRequest = WebPay.closeOrder(SveaConfig.getDefaultConfig()).setOrderId(orderId).closeInvoiceOrder();
 
 		String expectedMsg = "MISSING VALUE - CountryCode is required, use setCountryCode(...).\n";
 
 		assertEquals(expectedMsg, closeRequest.validateRequest());
 	}
 
 	@Test
 	public void testConfiguration() {
 		ConfigurationProviderTestData conf = new ConfigurationProviderTestData();
 		CreateOrderResponse response = WebPay.createOrder(conf).addOrderRow(TestingTool.createExVatBasedOrderRow("1")).addOrderRow(TestingTool.createExVatBasedOrderRow("2"))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber(TestingTool.DefaultTestIndividualNationalIdNumber).setIpAddress("123.123.123"))
 				.setCountryCode(TestingTool.DefaultTestCountryCode).setOrderDate(TestingTool.DefaultTestDate).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
 				.setCurrency(TestingTool.DefaultTestCurrency).useInvoicePayment().doRequest();
 
 		assertTrue(response.isOrderAccepted());
 	}
 
 	@Test
 	public void testFormatShippingFeeRowsZero() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig()).addOrderRow(TestingTool.createExVatBasedOrderRow("1"))
 				.addFee(Item.shippingFee().setShippingId("0").setName("Tess").setDescription("Tester").setAmountExVat(0).setVatPercent(0).setUnit("st"))
 				.addCustomerDetails(Item.individualCustomer().setNationalIdNumber(TestingTool.DefaultTestIndividualNationalIdNumber)).setCountryCode(TestingTool.DefaultTestCountryCode)
 				.setOrderDate(TestingTool.DefaultTestDate).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setCurrency(TestingTool.DefaultTestCurrency)
 				.setCustomerReference(TestingTool.DefaultTestCustomerReferenceNumber).useInvoicePayment().doRequest();
 
 		assertTrue(response.isOrderAccepted());
 	}
 
 	@Test
 	public void testCompanyIdResponse() {
 		CreateOrderResponse response = WebPay.createOrder(SveaConfig.getDefaultConfig()).addOrderRow(TestingTool.createExVatBasedOrderRow("1"))
 				.addCustomerDetails(Item.companyCustomer().setNationalIdNumber(TestingTool.DefaultTestCompanyNationalIdNumber)).setCountryCode(TestingTool.DefaultTestCountryCode)
 				.setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setOrderDate(TestingTool.DefaultTestDate).setCurrency(TestingTool.DefaultTestCurrency).useInvoicePayment().doRequest();
 
 		assertFalse(response.isIndividualIdentity);
 		assertTrue(response.isOrderAccepted());
 	}
 
 	@Test
 	public void testInvoiceCompanyDe() {
 		CreateOrderResponse response = WebPay
 				.createOrder(SveaConfig.getDefaultConfig())
 				.addOrderRow(TestingTool.createOrderRowDe())
 				.addCustomerDetails(Item.companyCustomer().setNationalIdNumber("12345").setVatNumber("DE123456789").setStreetAddress("Adalbertsteinweg", "1").setZipCode("52070").setLocality("AACHEN"))
 				.setCountryCode(COUNTRYCODE.DE).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setOrderDate(TestingTool.DefaultTestDate).setCurrency(CURRENCY.EUR).useInvoicePayment()
 				.doRequest();
 
 		assertFalse(response.isIndividualIdentity);
 		assertTrue(response.isOrderAccepted());
 	}
 
 	@Test
 	public void testInvoiceCompanyNl() {
 		CreateOrderResponse response = WebPay
 				.createOrder(SveaConfig.getDefaultConfig())
 				.addOrderRow(TestingTool.createOrderRowNl())
 				.addCustomerDetails(
 						Item.companyCustomer().setCompanyName("Svea bakkerij 123").setVatNumber("NL123456789A12").setStreetAddress("broodstraat", "1").setZipCode("1111 CD").setLocality("BARENDRECHT"))
 				.setCountryCode(COUNTRYCODE.NL).setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber).setOrderDate(TestingTool.DefaultTestDate).setCurrency(CURRENCY.EUR).useInvoicePayment()
 				.doRequest();
 
 		assertFalse(response.isIndividualIdentity);
 		assertTrue(response.isOrderAccepted());
 	}
 }
