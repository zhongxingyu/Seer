 package com.celements.payment;
 
 import static org.easymock.EasyMock.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 
 public class PaymentServiceTest extends AbstractBridgedComponentTestCase {
 
   private XWikiContext context;
   private XWiki xwiki;
   private PaymentService paymentService;
 
   @Before
   public void setUp_PaymentServiceTest() throws Exception {
     context = getContext();
     xwiki = createMock(XWiki.class);
     context.setWiki(xwiki);
     paymentService = (PaymentService) getComponentManager().lookup(IPaymentService.class);
   }
 
   @Test
   public void testExecutePaymentAction_noCallbackAction() {
     Map<String, String[]> data = new HashMap<String, String[]>();
     DocumentReference callbackActionDocRef = new DocumentReference(context.getDatabase(),
        "Payment", "CallbackAction");
     expect(xwiki.exists(eq(callbackActionDocRef), same(context))).andReturn(false
         ).anyTimes();
     replayAll();
     paymentService.executePaymentAction(data);
     verifyAll();
   }
 
   private void replayAll(Object ... mocks) {
     replay(xwiki);
     replay(mocks);
   }
 
   private void verifyAll(Object ... mocks) {
     verify(xwiki);
     verify(mocks);
   }
 
 }
