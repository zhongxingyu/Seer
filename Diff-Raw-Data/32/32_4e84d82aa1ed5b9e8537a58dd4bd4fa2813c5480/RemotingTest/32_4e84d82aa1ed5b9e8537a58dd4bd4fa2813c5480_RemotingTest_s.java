 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.mediation.MediationConfigurationWS;
 import com.sapienter.jbilling.server.mediation.MediationProcessWS;
 import com.sapienter.jbilling.server.mediation.MediationRecordWS;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.process.BillingProcessConfigurationWS;
 import com.sapienter.jbilling.server.process.BillingProcessDTOEx;
 import com.sapienter.jbilling.server.process.BillingProcessWS;
 import com.sapienter.jbilling.server.user.ContactWS;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.IWebServicesSessionBean;
 import com.sapienter.jbilling.server.util.RemoteContext;
 import junit.framework.TestCase;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * Smoke tests for the web services api. Ensures that all method calls complete successfully
  * and that the return value can be serialized using various web service API protocols.
  */
 public class RemotingTest extends TestCase {
 
     private final static Integer USER_ID = 2;
     private final static Integer AUTH_OK = 0;
 
     private IWebServicesSessionBean service = null;
 
     public void testHessian() {
 
         service = RemoteContext.getBean(RemoteContext.Name.API_CLIENT);
 
         // Hessian API Client for user 'admin' company 1
         assertEquals(1, service.getCallerId().intValue());
         assertEquals(1, service.getCallerCompanyId().intValue());
 
         System.out.println("Hessian tests");
         makeCalls();
         System.out.println("Hessian tests done");
     }
 
     public void testWebServices() {
 
         service = RemoteContext.getBean("apiClient2");
 
         // SOAP API for user 'admin' company 1
         assertEquals(1, service.getCallerId().intValue());
         assertEquals(1, service.getCallerCompanyId().intValue());
 
         System.out.println("Web Services tests");
         makeCalls();
         System.out.println("Web Services tests done");
     }
    
     public void testInvoker() {
         
         service = RemoteContext.getBean("apiClient3");
 
         // Spring HTTP Invoker API for user 'admin' company 1
         assertEquals(1, service.getCallerId().intValue());
         assertEquals(1, service.getCallerCompanyId().intValue());
 
         System.out.println("HTTP Invoker tests");
         makeCalls();
         System.out.println("HTTP Invoker tests done");
     }
 
     private void makeCalls() {
         try {
             // the goal is to test that the call can be done, not to test business logic
 
             // test InvoiceWS
             InvoiceWS invoice = service.getInvoiceWS(15);
             assertNotNull(invoice);
             assertEquals(15, invoice.getId().intValue());
 
             invoice = service.getLatestInvoice(USER_ID);
             assertNotNull(invoice);
             assertEquals(invoice.getUserId(), USER_ID);
 
             Integer[] invoicesIds = service.getLastInvoices(USER_ID, 5);
             assertNotNull(invoicesIds);
             assertFalse(invoicesIds.length == 0);
 
             invoicesIds = service.getInvoicesByDate("2006-01-01", "2007-01-01");
             assertNotNull(invoicesIds);
             assertFalse(invoicesIds.length == 0);
 
             invoicesIds = service.getUserInvoicesByDate(2, "2006-07-26", "2006-07-29");
             assertNotNull(invoicesIds);
             assertFalse(invoicesIds.length == 0);
 
             invoicesIds = service.createInvoice(USER_ID, false);
             assertTrue(invoicesIds.length > 0);
 
             service.createInvoice(USER_ID, false);
             try {
                 service.deleteInvoice(invoicesIds[0]);
                 invoice = service.getInvoiceWS(invoicesIds[0]);
                 fail("invoice should be deleted");
             } catch (Exception ex) {
                 //invoice not found
             }
 
             // orders WS
             OrderWS orderWS = createOrderWs();
 
             Integer[] orderIds = service.getOrderByPeriod(USER_ID, 1);
             assertNotNull(orderIds);
             assertTrue(orderIds.length > 0);
 
             orderWS = service.getLatestOrder(USER_ID);
             assertNotNull(orderWS);
 
             orderIds = service.getLastOrders(USER_ID, 1);
             assertNotNull(orderIds);
             assertTrue(orderIds.length > 0);
 
             // users WS
             UserWS userWS = createUserWS();
             Integer newUserId = service.createUser(userWS);
             assertNotNull(newUserId);
 
             UserWS newUser = service.getUserWS(newUserId);
             assertNotNull(newUser);
 
             ContactWS[] userContacts = service.getUserContactsWS(newUserId);
             assertNotNull(userContacts);
             assertTrue(userContacts.length > 0);
 
             ContactWS contactWS = newUser.getContact();
             contactWS.setCity("testCity");
             service.updateUserContact(newUserId, 2, contactWS);
             newUser = service.getUserWS(newUserId);
             assertNotNull(newUser);
             assertNotNull(newUser.getContact());
             assertEquals("testCity", newUser.getContact().getCity());
 
             Integer userId = service.getUserId(newUser.getUserName());
             assertEquals(newUserId, userId);
 
             Integer[] userIds = service.getUsersInStatus(1);
             assertNotNull(userIds);
             assertTrue(userIds.length > 0);
 
             userIds = service.getUsersByStatus(1, true);
             assertNotNull(userIds);
             assertTrue(userIds.length > 0);
 
             userIds = service.getUsersByCustomField(1, "serial-from-ws");
             assertNotNull(userIds);
             assertTrue(userIds.length > 0);
 
             Integer authResult = service.authenticate(newUser.getUserName(), "asdfasdf1");
             assertNotNull(authResult);
             assertEquals(AUTH_OK, authResult);
 
             invoice = service.getLatestInvoiceByItemType(USER_ID, 1);
             assertNotNull(invoice);
 
             
             // billing calls
             BillingProcessConfigurationWS billingConfig = service.getBillingProcessConfiguration();
             assertNotNull(billingConfig);
             assertEquals(1, billingConfig.getId());
 
             BillingProcessWS billingProcess = service.getBillingProcess(2);
             assertNotNull(billingProcess);
             assertEquals(2, billingProcess.getId().intValue());
 
             Integer lastBillingProccessID = service.getLastBillingProcess();
             assertNotNull(lastBillingProccessID);
             assertEquals(12, lastBillingProccessID.intValue());
 
             BillingProcessWS reviewProcess = service.getReviewBillingProcess();
             assertNull(reviewProcess); // no review process yet... i'm happy as long as the call was successful
 
             // todo: translate Collection to a typed list
             // todo: use InvoiceDTOEx WS representation
 //            @SuppressWarnings("unchecked")
 //            Collection<InvoiceDTO> invoices = service.getBillingProcessGeneratedInvoices(2);
 //            assertNotNull(invoices);
 //            assertFalse(invoices.isEmpty());
 

             // mediation calls
             List<MediationProcessWS> mediationProcesses = service.getAllMediationProcesses();
             assertNotNull(mediationProcesses);
             assertFalse(mediationProcesses.isEmpty());
 
             List<MediationRecordWS> mediationRecords = service.getMediationRecordsByMediationProcess(1);
             assertNotNull(mediationRecords);
             assertFalse(mediationRecords.isEmpty());
 
             Map<Integer, Long> recordCounts = service.getNumberOfMediationRecordsByStatuses();
             assertNotNull(recordCounts);
             assertFalse(recordCounts.isEmpty());
 
             List<MediationConfigurationWS> mediationConfigs = service.getAllMediationConfigurations();
             assertNotNull(mediationConfigs);
             assertFalse(mediationConfigs.isEmpty());
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception" + e.getMessage());
         }
     }
 
     private OrderWS createOrderWs() {
         OrderWS order = new OrderWS();
         order.setUserId(2);
         order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         order.setPeriod(1); // once
         order.setCurrencyId(1);
 
 
         order.setOrderLines(new OrderLineWS[]{createOrderLineWS()});
         return order;
     }
 
     private OrderLineWS createOrderLineWS() {
         OrderLineWS line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setDescription("Order line");
         line.setItemId(1);
         line.setQuantity(1);
         line.setPrice(new BigDecimal("10"));
         line.setAmount(new BigDecimal("10"));
         return line;
     }
 
     private UserWS createUserWS() {
         UserWS newUser = new UserWS();
         newUser.setUserName("testUserName-" + Calendar.getInstance().getTimeInMillis());
         newUser.setPassword("asdfasdf1");
         newUser.setLanguageId(1);
         newUser.setMainRoleId(5);
         newUser.setParentId(null);
         newUser.setStatusId(UserDTOEx.STATUS_ACTIVE);
         newUser.setCurrencyId(null);
         newUser.setBalanceType(Constants.BALANCE_NO_DYNAMIC);
 
         // add a contact
         newUser.setContact(createContactWS());
 
         // add a credit card
         CreditCardDTO cc = new CreditCardDTO();
         cc.setName("Frodo Baggins");
         cc.setNumber("4111111111111152");
 
         // valid credit card must have a future expiry date to be valid for payment processing
         Calendar expiry = Calendar.getInstance();
         expiry.set(Calendar.YEAR, expiry.get(Calendar.YEAR) + 1);
         cc.setExpiry(expiry.getTime());
 
         newUser.setCreditCard(cc);
 
         return newUser;
     }
 
     private ContactWS createContactWS() {
         ContactWS contact = new ContactWS();
         contact.setEmail("frodo@shire.com");
         contact.setFirstName("Frodo");
         contact.setLastName("Baggins");
         String fields[] = new String[2];
         fields[0] = "1";
         fields[1] = "2";
         String fieldValues[] = new String[2];
         fieldValues[0] = "serial-from-ws";
         fieldValues[1] = "FAKE_2";
         contact.setFieldNames(fields);
         contact.setFieldValues(fieldValues);
         contact.setType(2);
         return contact;
     }
 
 }
