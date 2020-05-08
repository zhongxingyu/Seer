 /**
  * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
  * If a copy of the MPL was not distributed with this file, You can obtain one at 
  * http://mozilla.org/MPL/2.0/.
  * 
  * This Source Code Form is also subject to the terms of the Health-Related Additional
  * Disclaimer of Warranty and Limitation of Liability available at
  * http://www.carewebframework.org/licensing/disclaimer.
  */
 package gov.ihs.cwf.mbroker;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import gov.ihs.cwf.mbroker.BrokerSession.IAsyncRPCEvent;
 import gov.ihs.cwf.mbroker.PollingThread.IHostEventHandler;
 
 import org.junit.Test;
 
 public class BrokerTest implements IHostEventHandler, IAsyncRPCEvent {
     
     private static final boolean debug = false;
     
     public static class TestBean implements Serializable {
         
         private static final long serialVersionUID = 1L;
         
         private int intValue;
         
         private String strValue;
         
         public TestBean() {
             
         }
         
         public TestBean(int intValue, String strValue) {
             this.intValue = intValue;
             this.strValue = strValue;
         }
         
         public int getIntValue() {
             return intValue;
         }
         
         public void setIntValue(int intValue) {
             this.intValue = intValue;
         }
         
         public String getStrValue() {
             return strValue;
         }
         
         public void setStrValue(String strValue) {
             this.strValue = strValue;
         }
     }
     
     private int asyncHandle;
     
     private boolean eventReceived;
     
     @Test
     public void testConnection() throws Exception {
         String server = System.getenv("cwf-test-server");
         assertTrue("Environment variable 'cwf-test-server' not set.", server != null);
         BrokerSession session = getConnection(server);
         session.connect();
         assertTrue(session.isConnected());
         assertTrue(session.isAuthenticated());
         print("Connected to " + session.getConnectionParams());
         session.addHostEventHandler(this);
         List<String> results = new ArrayList<String>();
         session.callRPCList("XWB ECHO LIST", results);
         assertList(results);
         session.callRPCList("XWB ECHO MEMO", results, results);
         assertEquals("DHCP RECEIVED:", results.get(0));
         results.remove(0);
         assertList(results);
         session.eventSubscribe("test", true);
        session.fireRemoteEvent("test", new TestBean(123, "test"), (String) null);
         asyncHandle = session.callRPCAsync("XWB ECHO LIST", this);
         print("Async RPC Handle: " + asyncHandle);
         int tries = 30;
         
         while (tries-- > 0 && (asyncHandle != 0 || !eventReceived)) {
             Thread.sleep(1000);
         }
         
         assertTrue("Async RPC failed - is TaskMan running?", asyncHandle == 0);
         assertTrue("Host event failed - is TaskMan running?", eventReceived);
         session.disconnect();
     }
     
     private void assertList(List<String> list) {
         assertTrue(list.size() == 28);
         
         for (int i = 0; i < 28; i++) {
             assertEquals("List Item #" + (i + 1), list.get(i));
         }
     }
     
     public BrokerSession getConnection(String params) throws Exception {
         ConnectionParams connectionParams = new ConnectionParams(params);
         connectionParams.setDebug(debug);
         System.out.println("Requesting connection from " + connectionParams);
         return new BrokerSession(connectionParams);
     }
     
     @Override
     public void onRPCComplete(int handle, String data) {
         assertTrue(handle == asyncHandle);
         asyncHandle = 0;
         print("Async Success: " + handle);
         print(data);
     }
     
     @Override
     public void onRPCError(int handle, int code, String text) {
         print("Async Error: " + code);
         print(text);
     }
     
     @Override
     public void onHostEvent(String name, Object data) {
         assertEquals("test", name);
         assertTrue(data instanceof TestBean);
         TestBean bean = (TestBean) data;
         assertEquals(123, bean.intValue);
         assertEquals("test", bean.strValue);
         print("Host Event Name: " + name);
         print("Host Event Data: " + data);
         eventReceived = true;
     }
     
     private void print(Object object) {
         System.out.println(object);
     }
     
 }
