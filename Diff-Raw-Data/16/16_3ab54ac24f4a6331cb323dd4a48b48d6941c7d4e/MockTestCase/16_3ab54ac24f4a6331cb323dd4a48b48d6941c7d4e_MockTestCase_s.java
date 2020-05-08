 package org.mule.transport.fix.examples.mock;
 
 import org.mule.api.MuleMessage;
 import org.mule.module.client.MuleClient;
 import org.mule.tck.FunctionalTestCase;
 
 import quickfix.field.ClOrdID;
 import quickfix.field.ExecTransType;
 import quickfix.field.ExecType;
 import quickfix.field.HandlInst;
 import quickfix.field.OrdStatus;
 import quickfix.field.OrdType;
 import quickfix.field.OrderQty;
 import quickfix.field.Side;
 import quickfix.field.Symbol;
 
 public class MockTestCase extends FunctionalTestCase {
 	private static int nextID = 1;
 	public void testExecutorConfig() throws Exception {
 		MuleClient client = new MuleClient();
 
 		quickfix.fix41.NewOrderSingle newOrderSingle = new quickfix.fix41.NewOrderSingle(
                 new ClOrdID(Long.valueOf(System.currentTimeMillis()+(nextID++)).toString()), new HandlInst('1'), new Symbol("LNX"),
                 new Side(Side.BUY), new OrdType(OrdType.MARKET));
         newOrderSingle.set(new OrderQty(1));
 
 
 		client
 				.dispatch(
 						"in",
 						newOrderSingle, null);
 		MuleMessage result;
 		quickfix.fix41.ExecutionReport report;
 		result=client.request("vm://out", 6000);
 		assertNotNull(result);
 		assertNotNull(result.getPayload());
 		assertTrue(result.getPayload() instanceof quickfix.fix41.ExecutionReport);
 		report=(quickfix.fix41.ExecutionReport)result.getPayload();
 		assertEquals(ExecTransType.NEW,report.getExecTransType().getValue());
 		assertEquals(ExecType.FILL,report.getExecType().getValue());
 		assertEquals(OrdStatus.NEW,report.getOrdStatus().getValue());
 		
 		result=client.request("vm://out", 5000);
 		assertNotNull(result);
 		assertNotNull(result.getPayload());
 		assertTrue(result.getPayload() instanceof quickfix.fix41.ExecutionReport);
 		report=(quickfix.fix41.ExecutionReport)result.getPayload();
 		assertEquals(ExecTransType.NEW,report.getExecTransType().getValue());
 		assertEquals(ExecType.FILL,report.getExecType().getValue());
 		assertEquals(OrdStatus.FILLED,report.getOrdStatus().getValue());
 		
 		result=client.request("vm://out", 1000);
 		assertNull(result);		
 	}
 
 	@Override
 	protected String getConfigResources() {
		return "mock/executor-mule-config.xml,mock/vm-as-banzai-mule-config.xml";
 	}
 
 }
