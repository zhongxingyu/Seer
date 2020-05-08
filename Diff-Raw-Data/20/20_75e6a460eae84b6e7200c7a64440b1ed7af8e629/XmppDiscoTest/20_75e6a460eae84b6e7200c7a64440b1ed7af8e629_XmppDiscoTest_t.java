 /*
  * Copyright (C) 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.waveprotocol.wave.examples.fedone.federation.xmpp;
 
 import com.google.protobuf.RpcCallback;
 
 import junit.framework.TestCase;
 
 import org.dom4j.Element;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.Packet;
 
 /**
  * Tests for the XMPPDisco class.
  *
  *
  */
 public class XmppDiscoTest extends TestCase {
 
   private XmppTestUtil.MockWaveXmppComponent mockComponent;
   private DiscoCallback discoCallback;
   private XmppDisco disco;
   private XmppFederationHost mockHost;
   private XmppFederationRemote mockRemote;
 
 
   private static final String EXPECTED_DISCO_ITEMS_GET =
       "\n<iq type=\"get\" id=\"1-1\" to=\"acmewave.com\" "
       + "from=\"wave.initech-corp.com\">\n"
       + "  <query xmlns=\"http://jabber.org/protocol/disco#items\"/>\n"
       + "</iq>";
 
   private static final String EXPECTED_DISCO_INFO_GET =
       "\n<iq type=\"get\" id=\"1-1\" to=\"wave.acmewave.com\" "
       + "from=\"wave.initech-corp.com\">\n"
       + "  <query xmlns=\"http://jabber.org/protocol/disco#info\"/>\n"
       + "</iq>";
 
   private static final String EXPECTED_DISCO_INFO_GET_PUBSUB =
       "\n<iq type=\"get\" id=\"1-1\" to=\"pubsub.acmewave.com\" "
       + "from=\"wave.initech-corp.com\">\n"
       + "  <query xmlns=\"http://jabber.org/protocol/disco#info\"/>\n"
       + "</iq>";
 
   private static final String EXPECTED_DISCO_ITEMS_RESULT =
       "\n<iq type=\"result\" id=\"1-1\" from=\"wave.initech-corp.com\" "
       + "to=\"wave.acmewave.com\">\n"
      + "  <query xmlns=\"http://jabber.org/protocol/disco#items\"/>\n"
       + "</iq>";
 
   private static final String EXPECTED_DISCO_INFO_RESULT =
       "\n<iq type=\"result\" id=\"1-1\" from=\"wave.initech-corp.com\" "
       + "to=\"wave.acmewave.com\">\n"
       + "  <query xmlns=\"http://jabber.org/protocol/disco#info\">\n"
       + "    <identity category=\"collaboration\" type=\"google-wave\" "
       + "name=\"Google Prototype Wave Server - FedOne\"/>\n"
       + "    <feature var=\"http://waveprotocol.org/protocol/0.2/waveserver\"/>\n"
       + "  </query>\n"
       + "</iq>";
 
 
   class DiscoCallback implements RpcCallback<String> {
 
     @SuppressWarnings("unused")
     private String resultJID = "not-run-yet";
 
     public void run(String parameter) {
       resultJID = parameter;
     }
 
     public String getResult() {
       return resultJID;
     }
   }
 
   @Override
   protected void setUp() throws Exception {
     super.setUp();
     discoCallback = new DiscoCallback();
     disco = new XmppDisco();
     mockRemote = createMock(XmppFederationRemote.class);
     mockHost = createMock(XmppFederationHost.class);
     mockComponent = new XmppTestUtil.MockWaveXmppComponent("", "", "", 0,
                                                            null, mockHost,
                                                            mockRemote,
                                                            disco);
     mockRemote.setComponent(mockComponent);
     mockHost.setComponent(mockComponent);
   }
 
   /**
    * Tests that starting disco sends a disco#items to the remote server.
    */
   public void testDiscoStart() {
     replayMocks();
     disco.discoverRemoteJid(XmppTestUtil.TEST_REMOTE_DOMAIN, discoCallback);
     verifyMocks();
 
     assertEquals(1, mockComponent.packetsSent);
     Packet packet = mockComponent.lastPacketSent;
     assertEquals(XmppTestUtil.TEST_REMOTE_DOMAIN, packet.getTo().toString());
     assertEquals(XmppTestUtil.TEST_LOCAL_JID, packet.getFrom().toString());
     assertEquals(EXPECTED_DISCO_ITEMS_GET, packet.toString());
   }
 
   /**
    * Tests stage 2 of disco. Inject a disco#items into the disco code, check it
    * calls disco#info on the JID.
    */
   public void testDiscoItemsResult() {
     disco.pendingDiscoMap.put(XmppTestUtil.TEST_IQ_ID, discoCallback);
     // create with wave, no pubsub
     IQ discoItemsResult = createDiscoItems(true, false);
 
     replayMocks();
     disco.processDiscoItemsResult(discoItemsResult);
     verifyMocks();
 
     assertEquals(1, mockComponent.packetsSent);
     Packet packet = mockComponent.lastPacketSent;
     assertEquals(XmppTestUtil.TEST_REMOTE_WAVE_JID, packet.getTo().toString());
     assertEquals(XmppTestUtil.TEST_LOCAL_JID, packet.getFrom().toString());
     assertEquals(EXPECTED_DISCO_INFO_GET, packet.toString());
   }
 
   /**
    * Tests stage 3 of disco. Inject a disco#info into the disco code (one that
    * matches wave) and check the callback gets run.
    */
   public void testDiscoInfoResultWave() {
     disco.pendingDiscoMap.put(XmppTestUtil.TEST_IQ_ID, discoCallback);
     // create with wave, no pubsub
     IQ discoItemsResult = createDiscoItems(true, false);
     // Start the process.
     disco.processDiscoItemsResult(discoItemsResult);
     assertEquals(1, mockComponent.packetsSent);
     Packet packet = mockComponent.lastPacketSent;
     assertEquals(EXPECTED_DISCO_INFO_GET, packet.toString());
     // create a wave disco result, inject into disco.
     disco.processDiscoInfoResult(createDiscoInfo(true /* wave */));
     assertEquals(1, mockComponent.packetsSent);
     assertEquals(XmppTestUtil.TEST_REMOTE_WAVE_JID, discoCallback.getResult());
   }
 
 
   /**
    * Tests stage 3 of disco. Inject a disco#info into the disco code (one that
    * doesn't match wave) and check callback gets run with null.
    */
   public void testDiscoInfoResultPubsub() {
     disco.pendingDiscoMap.put(XmppTestUtil.TEST_IQ_ID, discoCallback);
     // create with wave and pubsub
     IQ discoItemsResult = createDiscoItems(false, true);
     // Start the process.
     disco.processDiscoItemsResult(discoItemsResult);
     assertEquals(1, mockComponent.packetsSent);
     Packet packet = mockComponent.lastPacketSent;
     assertEquals(EXPECTED_DISCO_INFO_GET_PUBSUB, packet.toString());
     // create a wave disco result, inject into disco.
     disco.processDiscoInfoResult(createDiscoInfo(false /* not wave */));
     assertEquals(1, mockComponent.packetsSent);
     assertEquals(null, discoCallback.getResult());
   }
 
   /**
    * Tests stage 3 of disco. Inject a disco#items into the disco code with
    * pubsub, then wave. Then give it pubsub's disco#info, and check it then
    * sends a disco#info for wave.
    */
   public void testDiscoInfoResultPubsubAndWave() {
     disco.pendingDiscoMap.put(XmppTestUtil.TEST_IQ_ID, discoCallback);
     // create with no wave, pubsub
     IQ discoItemsResult = createDiscoItems(true, true);
     // Start the process.
     disco.processDiscoItemsResult(discoItemsResult);
     assertEquals(1, mockComponent.packetsSent);
     assertEquals(EXPECTED_DISCO_INFO_GET_PUBSUB,
                  mockComponent.lastPacketSent.toString());
 
     // create a non-wave disco result, inject into disco.
     disco.processDiscoInfoResult(createDiscoInfo(false /* not wave */));
     assertEquals(2, mockComponent.packetsSent);
     assertEquals(EXPECTED_DISCO_INFO_GET,
                  mockComponent.lastPacketSent.toString());
     assertEquals("not-run-yet", discoCallback.getResult());
 
     // create a non-wave disco result, inject into disco.
     disco.processDiscoInfoResult(createDiscoInfo(true /* wave */));
     assertEquals(2, mockComponent.packetsSent);
     assertEquals(XmppTestUtil.TEST_REMOTE_WAVE_JID, discoCallback.getResult());
   }
 
   /**
    * Tests that if disco is started for a remote server for which we already
    * have the result, the cached result is just passed to the callback.
    */
   public void testDiscoStartWithCachedResult() {
     disco.domainToJidMap.put(XmppTestUtil.TEST_REMOTE_DOMAIN,
                              XmppTestUtil.TEST_REMOTE_WAVE_JID);
     disco.discoverRemoteJid(XmppTestUtil.TEST_REMOTE_DOMAIN, discoCallback);
     assertEquals(0, mockComponent.packetsSent);
     assertEquals(XmppTestUtil.TEST_REMOTE_WAVE_JID, discoCallback.getResult());
   }
 
   /**
    * Tests that we return a (useless, empty) IQ for a disco#items.
    */
   public void testDiscoGetDiscoItems() {
     IQ request = createDiscoRequest(WaveXmppComponent.NAMESPACE_DISCO_ITEMS);
     replayMocks();
     disco.processDiscoItemsGet(request);
     verifyMocks();
     assertEquals(1, mockComponent.packetsSent);
     Packet packet = mockComponent.lastPacketSent;
     assertEquals(XmppTestUtil.TEST_REMOTE_WAVE_JID, packet.getTo().toString());
     assertEquals(XmppTestUtil.TEST_LOCAL_JID, packet.getFrom().toString());
     assertEquals(EXPECTED_DISCO_ITEMS_RESULT, packet.toString());
   }
 
   /**
    * Tests that we return the right wave-identifying IQ for a disco#info.
    */
   public void testDiscoGetDiscoInfo() {
     IQ request = createDiscoRequest(WaveXmppComponent.NAMESPACE_DISCO_INFO);
     replayMocks();
     disco.processDiscoInfoGet(request);
     verifyMocks();
     assertEquals(1, mockComponent.packetsSent);
     Packet packet = mockComponent.lastPacketSent;
     assertEquals(XmppTestUtil.TEST_REMOTE_WAVE_JID, packet.getTo().toString());
     assertEquals(XmppTestUtil.TEST_LOCAL_JID, packet.getFrom().toString());
     assertEquals(EXPECTED_DISCO_INFO_RESULT, packet.toString());
   }
 
   /**
    * Create a disco#info result from the remote server.
    *
    * @param forWaveJID if true, it's for the remote Wave JID, else it's the
    *                   remote pubsub JID.
    * @return the new IQ packet.
    */
   private IQ createDiscoInfo(boolean forWaveJID) {
     IQ response = new IQ(IQ.Type.result);
     response.setTo(XmppTestUtil.TEST_LOCAL_JID);
     response.setID(mockComponent.generateId());
     Element query = response
         .setChildElement("query", WaveXmppComponent.NAMESPACE_DISCO_INFO);
 
     if (forWaveJID) {
       response.setFrom(XmppTestUtil.TEST_REMOTE_WAVE_JID);
       query.addElement("identity")
           .addAttribute("category", XmppDisco.DISCO_INFO_CATEGORY)
           .addAttribute("type", XmppDisco.DISCO_INFO_TYPE)
           .addAttribute("name", mockComponent.getDescription());
       query.addElement("feature")
           .addAttribute("var", WaveXmppComponent.NAMESPACE_WAVE_SERVER);
     } else {
       response.setFrom(XmppTestUtil.TEST_REMOTE_PUBSUB_JID);
       query.addElement("identity")
           .addAttribute("category", "pubsub")
           .addAttribute("type", "whatever")
           .addAttribute("name", "not a wave server");
       query.addElement("feature")
           .addAttribute("var", WaveXmppComponent.NAMESPACE_PUBSUB);
     }
     return response;
   }
 
   /**
    * Create a disco#items result, with either or both of a pubsub and a wave
    * JID.
    *
    * @param wave   if true, create a wave JID item.
    * @param pubsub if true, create a pubsub JID item.
    * @return the new IQ packet.
    */
   private IQ createDiscoItems(boolean wave, boolean pubsub) {
     IQ discoItemsResult = new IQ(IQ.Type.result);
     discoItemsResult.setFrom(XmppTestUtil.TEST_REMOTE_DOMAIN);
     discoItemsResult.setTo(XmppTestUtil.TEST_LOCAL_JID);
     discoItemsResult.setID(mockComponent.generateId());
     Element discoBody = discoItemsResult.setChildElement("query",
                                                          WaveXmppComponent.NAMESPACE_DISCO_ITEMS);
     if (pubsub) {
       discoBody.addElement("item")
           .addAttribute("jid", XmppTestUtil.TEST_REMOTE_PUBSUB_JID);
     }
     if (wave) {
       discoBody.addElement("item")
           .addAttribute("jid", XmppTestUtil.TEST_REMOTE_WAVE_JID);
     }
     return discoItemsResult;
   }
 
   /**
    * Create a disco#info or disco#items query.
    *
    * @param namespace the namespace of the query - disco#info or disco#items
    * @return the new IQ packet
    */
   private IQ createDiscoRequest(String namespace) {
     IQ request = new IQ(IQ.Type.get);
     request.setID(mockComponent.generateId());
     request.setTo(XmppTestUtil.TEST_LOCAL_JID);
     request.setFrom(XmppTestUtil.TEST_REMOTE_WAVE_JID);
     request.setChildElement("query", namespace);
     return request;
   }
 
   private void replayMocks() {
     replay(mockHost);
     replay(mockRemote);
   }
 
   private void verifyMocks() {
     //    verify(mockHost);
     //    verify(mockRemote);
   }
 
 }
