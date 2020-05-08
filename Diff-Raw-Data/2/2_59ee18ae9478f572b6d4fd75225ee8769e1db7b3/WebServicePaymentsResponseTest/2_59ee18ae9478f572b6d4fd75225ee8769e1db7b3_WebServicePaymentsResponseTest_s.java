 package se.sveaekonomi.webpay.integration.response;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import se.sveaekonomi.webpay.integration.WebPay;
 import se.sveaekonomi.webpay.integration.order.create.CreateOrderBuilder;
 import se.sveaekonomi.webpay.integration.order.handle.DeliverOrderBuilder;
 import se.sveaekonomi.webpay.integration.order.handle.DeliverOrderBuilder.DistributionType;
 import se.sveaekonomi.webpay.integration.order.row.Item;
 import se.sveaekonomi.webpay.integration.response.webservice.CreateOrderResponse;
 import se.sveaekonomi.webpay.integration.response.webservice.DeliverOrderResponse;
 import se.sveaekonomi.webpay.integration.response.webservice.GetAddressesResponse;
 import se.sveaekonomi.webpay.integration.response.webservice.PaymentPlanParamsResponse;
 import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
 import se.sveaekonomi.webpay.integration.webservice.getaddresses.GetAddresses;
 import se.sveaekonomi.webpay.integration.webservice.getpaymentplanparams.GetPaymentPlanParams;
 
 
 public class WebServicePaymentsResponseTest {
     
     @Test
     public void testDeliverInvoiceOrderResult() throws Exception {
         DeliverOrderBuilder orderBuilder = WebPay.deliverOrder();
         long orderId = createInvoiceAndReturnOrderId();
         orderBuilder.setTestmode();
             orderBuilder.addOrderRow(Item.orderRow()
             .setArticleNumber("1")
             .setQuantity(2)
             .setAmountExVat(100.00)
             .setDescription("Specification")
             .setName("Prod")
             .setUnit("st")
             .setVatPercent(25)
             .setDiscountPercent(0));
             
         
         DeliverOrderResponse response = orderBuilder.setOrderId(orderId)
             .setNumberOfCreditDays(1)
             .setInvoiceDistributionType(DistributionType.Post)
             .deliverInvoiceOrder()
             .doRequest();
         
         assertEquals(response.isOrderAccepted(), true);        
     }
         
     @Test
     public void testResultGetAddresses() throws Exception {
         GetAddresses addressRequest = WebPay.getAddresses(); 
         GetAddressesResponse request = addressRequest.setTestmode()
             .setCountryCode("SE")
             .setOrderTypeInvoice()
             .setIndividual("194605092222")
             .doRequest();
         
     }
     
     @Test
     public void testResultGetPaymentPlanParams() throws Exception {
         GetPaymentPlanParams addressRequest = WebPay.getPaymentPlanParams();
         PaymentPlanParamsResponse response = addressRequest.setTestmode()
             .doRequest();
         assertEquals(response.isOrderAccepted(), true);
         assertEquals(response.getResultCode(), 0);
         assertEquals(response.getCampaignCodes().get(0).getCampaignCode(), "213060");
        assertEquals(response.getCampaignCodes().get(0).getDescription(), "K�p nu betala om 3 m�nader (r�ntefritt)");
         assertEquals(response.getCampaignCodes().get(0).getPaymentPlanType(), "InterestAndAmortizationFree");
         assertEquals(response.getCampaignCodes().get(0).getContractLengthInMonths(), "3");
         assertEquals(response.getCampaignCodes().get(0).getInitialFee(), "100");
         assertEquals(response.getCampaignCodes().get(0).getNotificationFee(), "29");
         assertEquals(response.getCampaignCodes().get(0).getInterestRatePercent(), "0");
         assertEquals(response.getCampaignCodes().get(0).getNumberOfInterestFreeMonths(), "3");
         assertEquals(response.getCampaignCodes().get(0).getNumberOfPaymentFreeMonths(), "3");
         assertEquals(response.getCampaignCodes().get(0).getFromAmount(), "1000");
         assertEquals(response.getCampaignCodes().get(0).getToAmount(), "50000");
     }
     
     private long createInvoiceAndReturnOrderId() throws Exception {
         CreateOrderBuilder order = WebPay.createOrder()
                 .setTestmode();
         order.addOrderRow(Item.orderRow()
                 .setArticleNumber("1")
                 .setQuantity(2)
                 .setAmountExVat(100.00)
                 .setDescription("Specification")
                 .setName("Prod")
                 .setUnit("st")
                 .setVatPercent(25)
                 .setDiscountPercent(0));
                              
         order.addCustomerDetails(Item.individualCustomer().setSsn(194605092222L));
         CreateOrderResponse response = order.setCountryCode(COUNTRYCODE.SE)
                 .setClientOrderNumber("33")
                 .setOrderDate("2012-12-12")
                 .setCurrency("SEK")
                 .useInvoicePayment()// returnerar InvoiceOrder object
                 .doRequest();
       
         return response.orderId;
     }
 }
