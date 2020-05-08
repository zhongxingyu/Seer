 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2010 Funambol, Inc.
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.syncml.spds;
 
 import java.util.Date;
 import java.util.Vector;
 import java.util.Hashtable;
 
 import com.funambol.storage.StringKeyValueMemoryStore;
 import com.funambol.storage.StringKeyValueStore;
 import com.funambol.storage.StringKeyValueStoreFactory;
 import com.funambol.syncml.protocol.SyncML;
 import com.funambol.syncml.protocol.Sync;
 import com.funambol.syncml.protocol.SyncBody;
 import com.funambol.syncml.protocol.Alert;
 import com.funambol.syncml.protocol.Map;
 import com.funambol.syncml.protocol.Cred;
 import com.funambol.syncml.protocol.Meta;
 import com.funambol.syncml.protocol.Target;
 import com.funambol.syncml.client.BaseSyncSource;
 import com.funambol.util.CodedException;
 import com.funambol.util.Log;
 import com.funambol.util.ConsoleAppender;
 import com.funambol.util.TransportAgent;
 
 import junit.framework.*;
 
 /**
  * Test the SyncManager suspend and resume functionality
  */
 public class SyncManagerSuspendResumeTest extends TestCase {
 
     private static final String TAG_LOG = "SyncManagerSuspendResumeTest";
 
     private final String TEST_SERVER_URL  = "http://test.server.url";
     private final String TEST_USERNAME    = "test";
     private final String TEST_PASSWORD    = "test";
 
     private SyncManager  sm = null;
     private SyncConfig   sc = null;
     private SourceConfig ssc = null;
     private SuspendResumeSource tss = null;
 
     private final String JSESSION_ID      = ";jsessionid=F2EA56F802D65950FAC3E37336BE1EEA.NODE01";
     private int sentMessagesCount = 0;
 
     private static final int NUM_ITERATIONS = 80;
 
     private final String TEST_END_MESSAGE = "Test Ended";
 
     private StringKeyValueStore store = null;
 
     private TestStringKeyValueStoreFactory storeFactory = new TestStringKeyValueStoreFactory();
 
     
     public SyncManagerSuspendResumeTest(String name) {
         super(name);
     }
 
     /**
      * Set up all of the tests
      */
     public void setUp() {
 
         sc = new SyncConfig();
         sc.syncUrl = TEST_SERVER_URL;
         sc.userName = TEST_USERNAME;
         sc.password = TEST_PASSWORD;
         sc.deviceConfig = new DeviceConfig();
 
         ssc = new SourceConfig();
         
         sm = new SyncManager(sc);
 
         tss = new SuspendResumeSource(ssc);
 
         sentMessagesCount = 0;
 
         SyncStatus.setStoreFactory(storeFactory);
 
         Log.initLog(new ConsoleAppender(), Log.TRACE);
     }
 
     /**
      * Tear down all of the tests
      */
     public void tearDown() {
 
     }
 
     //----------------------------------------------- Authentication phase tests
 
     public void testSuspendSlow1() throws Exception {
 
         sm.setTransportAgent(new TestTransportAgent(new TestMessageHandler() {
             
             public byte[] handleMessage(byte message[]) throws Exception {
 
                 byte response[] = null;
                 sentMessagesCount++;
 
                 if (sentMessagesCount == 1) {
                     // Handle first message (Force a slow sync)
                     response = getInitResponse(false, 201).getBytes("UTF-8");
                 } else if (sentMessagesCount == 2) {
                     // The client is sending its items
                     int firstLuid = tss.getFirstLuid();
                     int lastLuid  = tss.getLastLuid();
                     response = getRecevingResponse(firstLuid, lastLuid);
                     if (sentMessagesCount == NUM_ITERATIONS) {
                         // Block the source from providing items
                         tss.endSending();
                     }
                     sm.cancel();
                 } else if (sentMessagesCount == 3) {
                     // This is the resume request
                     SyncMLParser parser = new SyncMLParser(false);
                     SyncML syncML = parser.parse(message);
                     // We expect an alert with the resume request
                     SyncBody syncBody = syncML.getSyncBody();
                     Vector bodyCommands = syncBody.getCommands();
                     Alert alert = null; 
                     for(int i=0;i<bodyCommands.size();++i) {
                         if (bodyCommands.elementAt(i) instanceof Alert) {
                             alert = (Alert)bodyCommands.elementAt(i);
                             break;
                         }
                     }
                     assertTrue(alert != null);
                     assertTrue(alert.getData() == SyncML.ALERT_CODE_RESUME);
 
                     throw new SyncException(SyncException.CLIENT_ERROR, TEST_END_MESSAGE);
                 }
 
                 // Note that we don't send the status to the map command, but
                 // currenly the SyncManager does not really care ;)
 
                 tss.resetLuidCounters();
                 return response;
             }
 
             public String handleMessage(String message) throws Exception {
                 return null;
             }
         }));
         
         try {
             sm.sync(tss);
         } catch (SyncException se) {
             // The sync must have been cancelled
             assertTrue(se.getCode() == SyncException.CANCELLED);
         }
 
         try {
             // Now we fire the same sync again and we expect it to be resumed
             sm.sync(tss);
         } catch (SyncException se) {
             assertTrue(se.getCode() == SyncException.CLIENT_ERROR);
             assertTrue(se.getMessage().endsWith(TEST_END_MESSAGE));
         }
     }
 
     public void testSuspendFast1() throws Exception {
 
         sm.setTransportAgent(new TestTransportAgent(new TestMessageHandler() {
             
             public byte[] handleMessage(byte message[]) throws Exception {
 
                 byte response[] = null;
                 sentMessagesCount++;
 
                 if (sentMessagesCount == 1) {
                     response = getInitResponse(true, 200).getBytes("UTF-8");
                     tss.endSending();
                 } else if (sentMessagesCount == 2) {
                     // The client has no items to send
                     response = getSendingResponse(false);
                 } else if (sentMessagesCount == 3) {
                     // This message shall contain the client mappings, but the
                     // sync gets interrupted here
                     throw new SyncException(SyncException.CONN_NOT_FOUND, "IOError");
                 } else if (sentMessagesCount == 4) {
                     // This is the resume request
                     SyncMLParser parser = new SyncMLParser(false);
                     SyncML syncML = parser.parse(message);
                     // We expect an alert with the resume request
                     SyncBody syncBody = syncML.getSyncBody();
                     Vector bodyCommands = syncBody.getCommands();
                     Alert alert = null; 
                     for(int i=0;i<bodyCommands.size();++i) {
                         if (bodyCommands.elementAt(i) instanceof Alert) {
                             alert = (Alert)bodyCommands.elementAt(i);
                             break;
                         }
                     }
                     assertTrue(alert != null);
                     // We don't force a resume here because our server will
                     // revert to a slow sync instead
                     //assertTrue(alert.getData() == SyncML.ALERT_CODE_RESUME);
 
                     response = getInitResponse(true, 200).getBytes("UTF-8");
                     //throw new SyncException(SyncException.CLIENT_ERROR, TEST_END_MESSAGE);
                 } else if (sentMessagesCount == 5) {
                     // We expect a message with 20 map items
                     SyncMLParser parser = new SyncMLParser(false);
                     SyncML syncML = parser.parse(message);
                     // We expect a map command with 20 map items
                     SyncBody syncBody = syncML.getSyncBody();
                     Vector bodyCommands = syncBody.getCommands();
                     Map map = null; 
                     for(int i=0;i<bodyCommands.size();++i) {
                         if (bodyCommands.elementAt(i) instanceof Map) {
                             map = (Map)bodyCommands.elementAt(i);
                             break;
                         }
                     }
                     assertTrue(map != null);
                     Vector mapItems = map.getMapItems();
                     assertTrue(mapItems != null);
                     assertTrue(mapItems.size() == 20);
                     throw new SyncException(SyncException.CLIENT_ERROR, TEST_END_MESSAGE);
                 }
 
                 // Note that we don't send the status to the map command, but
                 // currenly the SyncManager does not really care ;)
 
                 tss.resetLuidCounters();
                 return response;
             }
 
             public String handleMessage(String message) throws Exception {
                 return null;
             }
         }));
         
         try {
             sm.sync(tss);
         } catch (SyncException se) {
             // The sync must have been cancelled
             assertTrue(se.getCode() == SyncException.CLIENT_ERROR);
             assertTrue(se.getMessage().endsWith("IOError"));
         }
 
         try {
             // Now we fire the same sync again and we expect it to be resumed
             sm.sync(tss);
         } catch (SyncException se) {
             assertTrue(se.getCode() == SyncException.CLIENT_ERROR);
             assertTrue(se.getMessage().endsWith(TEST_END_MESSAGE));
         }
     }
 
 
 
     private String getInitResponse(boolean acceptSyncMode, int forcedSyncMode) {
         return getServerResponseFromStatus(getStatus_AUTH_BASIC_OK(), !acceptSyncMode, forcedSyncMode);
     }
 
     private String getStatus_AUTH_BASIC_OK() {
         return "<Status>\n" +
                "<CmdID>1</CmdID>\n" +
                "<MsgRef>1</MsgRef>\n" +
                "<CmdRef>0</CmdRef>\n" +
                "<Cmd>SyncHdr</Cmd>\n" +
                "<TargetRef>" + sc.syncUrl + "</TargetRef>\n" +
                "<SourceRef>" + sc.deviceConfig.devID + "</SourceRef>\n" +
                "<Data>212</Data>\n" +
                "</Status>";
     }
 
     private String getServerResponseFromStatus(String status, boolean addAlertFromServer, int forcedSyncMode) {
         return "<SyncML>\n" +
                "<SyncHdr>\n" +
                "<VerDTD>1.2</VerDTD>\n" +
                "<VerProto>SyncML/1.2</VerProto>\n" +
                "<SessionID>1266917419910</SessionID>\n" +
                "<MsgID>1</MsgID>\n" +
                "<Target>\n" +
                "<LocURI>" + sc.deviceConfig.devID + "</LocURI>\n" +
                "</Target>\n" +
                "<Source>\n" +
                "<LocURI>" + sc.syncUrl + "</LocURI>\n" +
                "</Source>\n" +
                "<RespURI>" + sc.syncUrl + JSESSION_ID + "</RespURI>\n" +
                "</SyncHdr>\n" +
                "<SyncBody>\n" +
                status +
                "<Status>\n" +
                "<CmdID>2</CmdID>\n" +
                "<MsgRef>1</MsgRef>\n" +
                "<CmdRef>1</CmdRef>\n" +
                "<Cmd>Alert</Cmd>\n" +
                "<TargetRef>" + ssc.getName() + "</TargetRef>\n" +
                "<SourceRef>" + ssc.getRemoteUri() + "</SourceRef>\n" +
                "<Data>" + (addAlertFromServer ? 508 : 200) + "</Data>\n" +
                "</Status>\n" +
                "<Status>\n" +
                "<CmdID>3</CmdID>\n" +
                "<MsgRef>1</MsgRef>\n" +
                "<CmdRef>2</CmdRef>\n" +
                "<Cmd>Put</Cmd>\n" +
                "<SourceRef>./devinf12</SourceRef>\n" +
                "<Data>200</Data>\n" +
                "</Status>\n" +
                getAlertFromServer(forcedSyncMode) +
                "<Final/>\n" +
                "</SyncBody>\n" +
                "</SyncML>";
     }
 
     private String getAlertFromServer(int forcedSyncMode) {
         return "<Alert>\n" +
                "<CmdID>4</CmdID>\n" +
                "<Data>" + forcedSyncMode + "</Data>\n" +
                "<Item>\n" +
                "<Target>\n" +
                "<LocURI>" + ssc.getName() + "</LocURI>\n" +
                "</Target>\n" +
                "<Source>\n" +
                "<LocURI>" + ssc.getRemoteUri() + "</LocURI>\n" +
                "</Source>\n" +
                "<Meta>\n" +
                "<Anchor xmlns='syncml:metinf'>\n" +
                "<Last>0</Last>\n" +
                "<Next>0</Next>\n" +
                "</Anchor>\n" +
                "</Meta>\n" +
                "</Item>\n" + 
                "</Alert>\n";
     }
 
     private byte[] getRecevingResponse(int firstLuid, int lastLuid) {
 
         StringBuffer res = new StringBuffer();
 
         res.append("<SyncML>\n")
            .append("<SyncHdr>\n")
            .append("<VerDTD>1.2</VerDTD>\n")
            .append("<VerProto>SyncML/1.2</VerProto>\n")
            .append("<SessionID>1266917419910</SessionID>\n")
            .append("<MsgID>1</MsgID>\n")
            .append("<Target>\n")
            .append("<LocURI>").append(sc.deviceConfig.devID).append("</LocURI>\n")
            .append("</Target>\n")
            .append("<Source>\n")
            .append("<LocURI>" + sc.syncUrl + "</LocURI>\n")
            .append("</Source>\n")
            .append("<RespURI>").append(sc.syncUrl).append(JSESSION_ID).append("</RespURI>\n")
            .append("</SyncHdr>\n")
            .append("<SyncBody>\n");
 
         int cmdId = 1;
 
         res.append("<Status>\n")
            .append("<CmdID>").append(cmdId++).append("</CmdID>\n")
            .append("<MsgRef>1</MsgRef>\n")
            .append("<CmdRef>0</CmdRef>\n")
            .append("<Cmd>SyncHdr</Cmd>\n")
            .append("<TargetRef>").append(sc.syncUrl).append("</TargetRef>\n")
            .append("<SourceRef>").append(sc.deviceConfig.devID).append("</SourceRef>\n")
            .append("<Data>200</Data>\n")
            .append("</Status>");
 
 
         for(int i=firstLuid;i<lastLuid+1;++i) {
             res.append("<Status>\n")
                .append("<CmdID>").append(cmdId++).append("</CmdID>\n")
                .append("<MsgRef>1</MsgRef>\n")
                .append("<CmdRef>0</CmdRef>\n")
                .append("<Cmd>Replace</Cmd>\n")
                .append("<SourceRef>").append(i).append("</SourceRef>\n")
                .append("<Data>200</Data>\n")
                .append("</Status>");
         }
 
         res.append("</SyncBody>\n")
            .append("</SyncML>");
 
         return res.toString().getBytes();
     }
 
     private byte[] getSendingResponse(boolean last) {
 
         StringBuffer res = new StringBuffer();
 
         res.append("<SyncML>\n")
            .append("<SyncHdr>\n")
            .append("<VerDTD>1.2</VerDTD>\n")
            .append("<VerProto>SyncML/1.2</VerProto>\n")
            .append("<SessionID>1266917419910</SessionID>\n")
            .append("<MsgID>1</MsgID>\n")
            .append("<Target>\n")
            .append("<LocURI>").append(sc.deviceConfig.devID).append("</LocURI>\n")
            .append("</Target>\n")
            .append("<Source>\n")
            .append("<LocURI>" + sc.syncUrl + "</LocURI>\n")
            .append("</Source>\n")
            .append("<RespURI>").append(sc.syncUrl).append(JSESSION_ID).append("</RespURI>\n")
            .append("</SyncHdr>\n")
            .append("<SyncBody>\n");
 
         int cmdId = 1;
 
         res.append("<Status>\n")
            .append("<CmdID>").append(cmdId++).append("</CmdID>\n")
            .append("<MsgRef>1</MsgRef>\n")
            .append("<CmdRef>0</CmdRef>\n")
            .append("<Cmd>SyncHdr</Cmd>\n")
            .append("<TargetRef>").append(sc.syncUrl).append("</TargetRef>\n")
            .append("<SourceRef>").append(sc.deviceConfig.devID).append("</SourceRef>\n")
            .append("<Data>200</Data>\n")
            .append("</Status>");
 
         res.append("<Sync>\n")
            .append("<CmdID>").append(cmdId++).append("</CmdID>\n")
            .append("<Target>\n")
            .append("<LocURI>briefcase</LocURI>\n")
            .append("</Target>\n")
            .append("<Source>\n")
            .append("<LocURI>briefcase</LocURI>\n")
            .append("</Source>\n");
 
         for(int i=0;i<20;++i) {
            res.append("<Replace>\n")
                .append("<CmdID>").append(cmdId++).append("</CmdID>\n")
                .append("<Item>\n")
                .append("<Source>\n")
                .append("<LocURI>1205").append(i).append("</LocURI>\n")
                .append("</Source>\n")
                .append("<Meta>\n")
                .append("<Type xmlns=\"syncml:metinf\">application/*</Type>\n")
                .append("</Meta>\n")
                .append("<Data>This is just a test for performance evaluation</Data>\n")
                .append("</Item>\n")
               .append("</Replace>\n");
         }
  
         res.append("</Sync>\n");
 
         if (last) {
             res.append("<Final/>\n");
         }
 
         res.append("</SyncBody>\n")
            .append("</SyncML>");
 
         return res.toString().getBytes();
     }
 
     private class TestTransportAgent implements TransportAgent {
 
         private TestMessageHandler handler;
 
         public TestTransportAgent(TestMessageHandler h) {
             handler = h;
         }
 
         public String sendMessage(String request, String charset) throws CodedException {
             try {
                 return handler.handleMessage(request);
             } catch (Exception e) {
                 throw new CodedException(-1, e.toString());
             }
         }
 
         public String sendMessage(String request) throws CodedException {
             return sendMessage(request, null);
         }
 
         public byte[] sendMessage(byte[] request) throws CodedException {
             try {
                 return handler.handleMessage(request);
             } catch (Exception e) {
                 throw new CodedException(-1, e.toString());
             }
         }
 
         public void setRetryOnWrite(int retries) { }
         public void setRequestURL(String requestUrl) { }
         public String getResponseDate() { return new Date().toString(); }
 
         public void setRequestContentType(String contentType) {
         }
 
         public void setCustomHeaders(Hashtable headers) {
         }
     }
 
     private interface TestMessageHandler {
         public String handleMessage(String message) throws Exception;
         public byte[] handleMessage(byte[] message) throws Exception;
     }
 
     private class TestSyncSource extends BaseSyncSource {
 
         private boolean done = false;
         private int luid = 0;
         private int firstLuid = -1;
         private int lastLuid  = -1;
 
         public TestSyncSource(SourceConfig sc) {
             super(sc);
         }
 
         public int addItem(SyncItem item) throws SyncException {
             return 200;
         }
     
         public int updateItem(SyncItem item) throws SyncException {
             return 200;
         }
     
         public int deleteItem(String key) throws SyncException {
             return 200;
         }
 
         public SyncItem getNextItem() throws SyncException {
 
             if (firstLuid == -1) {
                 firstLuid = luid;
             }
             lastLuid = luid;
 
             if (!done) {
                 SyncItem res = new SyncItem("" + luid);
                 res.setContent("Test item, make is of a signficant length".getBytes());
                 luid++;
                 return res;
             } else {
                 return null;
             }
         }
 
         public void endSending() {
             done = true;
         }
 
         public void resetLuidCounters() {
             firstLuid = -1;
             lastLuid  = -1;
         }
 
         public int getFirstLuid() {
             return firstLuid;
         }
 
         public int getLastLuid() {
             return lastLuid;
         }
 
         protected void initAllItems() throws SyncException {
         }
 
         protected void initNewItems() throws SyncException {
         }
 
         protected void initUpdItems() throws SyncException {
         }
 
         protected void initDelItems() throws SyncException {
         }
 
         protected SyncItem getItemContent(final SyncItem item) throws SyncException {
             return null;
         }
     }
 
     private class TestStringKeyValueStoreFactory extends StringKeyValueStoreFactory {
 
         private StringKeyValueMemoryStore store = new StringKeyValueMemoryStore();
 
         public TestStringKeyValueStoreFactory() {
         }
 
         public StringKeyValueStore getStringKeyValueStore(String name) {
             return store;
         }
     }
 
     private class SuspendResumeSource extends TestSyncSource {
 
         public SuspendResumeSource(SourceConfig config) {
             super(config);
         }
 
         public boolean supportsResume() {
             return true;
         }
     }
 
 }
 
