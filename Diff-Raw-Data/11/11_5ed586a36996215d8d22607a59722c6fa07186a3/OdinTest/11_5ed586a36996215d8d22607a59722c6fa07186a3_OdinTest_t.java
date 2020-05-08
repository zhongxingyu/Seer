 package net.floodlightcontroller.odin;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.floodlightcontroller.core.IFloodlightProviderService;
 import net.floodlightcontroller.core.IOFSwitch;
 import net.floodlightcontroller.core.module.FloodlightModuleContext;
 import net.floodlightcontroller.core.module.FloodlightModuleException;
 import net.floodlightcontroller.core.test.MockFloodlightProvider;
 import net.floodlightcontroller.odinmaster.NotificationCallback;
 import net.floodlightcontroller.odinmaster.NotificationCallbackContext;
 import net.floodlightcontroller.odinmaster.OdinApplication;
 import net.floodlightcontroller.odinmaster.OdinEventSubscription;
 import net.floodlightcontroller.odinmaster.OdinEventSubscription.Relation;
 import net.floodlightcontroller.odinmaster.AgentManager;
 import net.floodlightcontroller.odinmaster.ClientManager;
 import net.floodlightcontroller.odinmaster.ILvapManager;
 import net.floodlightcontroller.odinmaster.Lvap;
 import net.floodlightcontroller.odinmaster.LvapManagerImpl;
 import net.floodlightcontroller.odinmaster.OdinAgentFactory;
 import net.floodlightcontroller.odinmaster.OdinClient;
 import net.floodlightcontroller.odinmaster.OdinMaster;
 import net.floodlightcontroller.odinmaster.OdinMobilityManager;
 import net.floodlightcontroller.restserver.IRestApiService;
 import net.floodlightcontroller.restserver.RestApiServer;
 import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
 import net.floodlightcontroller.staticflowentry.StaticFlowEntryPusher;
 import net.floodlightcontroller.util.MACAddress;
 
 import org.easymock.EasyMock;
 import org.jboss.netty.channel.Channel;
 import org.junit.Before;
 import org.junit.Test;
 
 public class OdinTest {
 	protected MockFloodlightProvider mockFloodlightProvider;
     protected FloodlightModuleContext cntx;
     protected OdinMaster odinMaster;
     protected AgentManager agentManager;
     protected ClientManager clientManager;
     protected ILvapManager lvapManager;
     protected StaticFlowEntryPusher staticFlowEntryPusher;
     protected long switchId = 1L;
     
     /**
      * Use this to add a mock agent on IP:ipAddress and port:port.
      * 
      * @param ipAddress
      * @param port
      * @throws Exception
      */
     private void addAgentWithMockSwitch (String ipAddress, int port) throws Exception {
         int size = agentManager.getAgents().size();
     	
         agentManager.receivePing(InetAddress.getByName(ipAddress));
         
     	assertEquals(agentManager.getAgents().size(), size);
 
         long id = switchId++;
         // Now register a switch
     	IOFSwitch sw1;
     	sw1 = EasyMock.createNiceMock(IOFSwitch.class);
     	InetSocketAddress sa = new InetSocketAddress(ipAddress, port);
     	Channel ch = EasyMock.createMock(Channel.class);
         expect(sw1.getChannel()).andReturn(ch).anyTimes();
         expect(ch.getRemoteAddress()).andReturn((SocketAddress)sa).anyTimes();
         expect(sw1.getId()).andReturn(id).anyTimes();
 
         
         EasyMock.replay(sw1);
         EasyMock.replay(ch);
 
         // Update the switch map
         mockFloodlightProvider.getSwitches().put(id, sw1);
 
         // Let's try again
         agentManager.receivePing(InetAddress.getByName(ipAddress));
         
         assertEquals(agentManager.getAgents().size(),size + 1);
         
         assertEquals(agentManager.getAgents().get(InetAddress.getByName(ipAddress)).getSwitch(), sw1);
     }
     
     private void addClientToClientManagerSingleSsid (MACAddress sta, InetAddress inetAddr, MACAddress lvapBssid, String ssid) {
     	ArrayList<String> ssidList = new ArrayList<String> ();
     	ssidList.add(ssid);
     	Lvap lvap = new Lvap (lvapBssid, ssidList);
     	clientManager.addClient(sta, inetAddr, lvap);
     }
     
     @Before
     public void setup() throws FloodlightModuleException{
         // Mock context
         cntx = new FloodlightModuleContext();
         RestApiServer restApi = new RestApiServer();
         mockFloodlightProvider = new MockFloodlightProvider();
         
         OdinAgentFactory.setOdinAgentType("MockOdinAgent");
         
         clientManager = new ClientManager();
         agentManager = new AgentManager(clientManager);
         lvapManager = new LvapManagerImpl();
         lvapManager.addNetwork("odin");
         odinMaster = new OdinMaster(agentManager, clientManager, lvapManager);
         
         cntx.addService(IFloodlightProviderService.class, mockFloodlightProvider);
         cntx.addService(IRestApiService.class, restApi);
         
         try {
 			restApi.init(cntx);
 	        odinMaster.init(cntx);
 	        
 		} catch (FloodlightModuleException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         restApi.startUp(cntx);
         
         mockFloodlightProvider.addOFSwitchListener(odinMaster);
         agentManager.setFloodlightProvider(mockFloodlightProvider);
     }
     
     /************* Master Tests ******************/
 
     /**
      * Make sure the client tracker doesn't duplicate
      * client MAC addresses.
      * 
      * @throws Exception
      */
     @Test
     public void testClientTracker() throws Exception {
     	assertEquals(clientManager.getClients().size(),0);
     	addClientToClientManagerSingleSsid(MACAddress.valueOf("00:00:00:00:00:01"),
 								 InetAddress.getByName("172.17.1.1"),
 								 MACAddress.valueOf("00:00:00:00:00:01"),
 								 "odin");
 		
 		assertEquals(clientManager.getClients().size(),1);
 		addClientToClientManagerSingleSsid(MACAddress.valueOf("00:00:00:00:00:01"),
 								 InetAddress.getByName("172.17.1.2"),
 								 MACAddress.valueOf("00:00:00:00:00:02"),
 								 "odin");
 		assertEquals(clientManager.getClients().size(),1); // Same hw-addr cant exist twice
 
 		// TODO: None of the other parameters should repeat either!
 		clientManager.removeClient(MACAddress.valueOf("00:00:00:00:00:02"));
 		assertEquals(clientManager.getClients().size(),1);
 		
 		clientManager.removeClient(MACAddress.valueOf("00:00:00:00:00:01"));
 		assertEquals(clientManager.getClients().size(),0);
 		
 		clientManager.removeClient(MACAddress.valueOf("00:00:00:00:00:01"));
 		assertEquals(clientManager.getClients().size(),0);
     }
     
     /**
      *  Make sure that the agent tracker does not
      *  track an agent if there isn't a corresponding
      *  switch associated with it
      *  
      * @throws Exception
      */
     @Test
     public void testAgentTracker() throws Exception {
         
         // Send a ping to the OdinAgentTracker to have it
         // register the agent
     	agentManager.receivePing(InetAddress.getByName("127.0.0.1"));
         
         // We haven't registered a switch yet, so this should
         // still be zero
         assertEquals(agentManager.getAgents().size(),0);
         
         // Now register a switch
     	IOFSwitch sw1;
     	sw1 = EasyMock.createNiceMock(IOFSwitch.class);
     	InetSocketAddress sa= new InetSocketAddress("127.0.0.1", 12345);
     	Channel ch = EasyMock.createMock(Channel.class);
         expect(sw1.getChannel()).andReturn(ch).anyTimes();
         expect(ch.getRemoteAddress()).andReturn((SocketAddress)sa).anyTimes();
         
         EasyMock.replay(sw1);
         EasyMock.replay(ch);
         // Load the switch map
         Map<Long, IOFSwitch> switches = new HashMap<Long, IOFSwitch>();
         switches.put(1L, sw1);
         mockFloodlightProvider.setSwitches(switches);
 
         // Let's try again
         agentManager.receivePing(InetAddress.getByName("127.0.0.1"));
         
         assertEquals(agentManager.getAgents().size(),1);
     }
     
     
     /**
      * Test to see if OdinAgentTracker.receiveProbe()
      * works correctly
      * 
      * @throws Exception
      */
     @Test
     public void testReceiveProbe() throws Exception {
     	String ipAddress1 = "172.17.2.161";
     	String ipAddress2 = "172.17.2.162";
     	String ipAddress3 = "172.17.2.163";
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	MACAddress clientMacAddr2 = MACAddress.valueOf("00:00:00:00:00:02");
     	MACAddress clientMacAddr3 = MACAddress.valueOf("00:00:00:00:00:03");
   	
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	addAgentWithMockSwitch(ipAddress2, 12345);
     	
     	// 1. Things shouldn't explode when this is called
     	odinMaster.receiveProbe(null, null);
     	
     	assertEquals(agentManager.getAgents().size(), 2);
     	
     	System.err.println("DING DING DING " + lvapManager.getNumNetworks());
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	
     	// 2. Client should be added
     	assertEquals(clientManager.getClients().size(), 1);    	
     	addClientToClientManagerSingleSsid(clientMacAddr1, InetAddress.getByName("172.17.2.51"), MACAddress.valueOf("00:00:00:00:11:11"), "odin");
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent(), null);
     	
     	// 3, 4. now try again. Client should be assigned an AP
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	assertEquals(clientManager.getClients().size(), 1);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	// 5. another probe from the same AP/client should
     	// 	not be handed off, and should not feature
     	// 	in the client list a second time
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	assertEquals(clientManager.getClients().size(), 1);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	// 6. probe scan from new AP. client should not be
     	// handed off to the new one, nor should a new
     	// client be registered.
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress2), clientMacAddr1);
     	assertEquals(clientManager.getClients().size(), 1);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	// 7. New client performs a scan at AP1
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr2);
     	assertEquals(clientManager.getClients().size(), 2);
     	
     	// 8. Add client2
     	addClientToClientManagerSingleSsid(clientMacAddr2, InetAddress.getByName("172.17.2.52"), MACAddress.valueOf("00:00:00:00:11:12"), "odin");
     	assertEquals(clientManager.getClients().size(), 2);
     	assertEquals(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent(), null);
     	
     	// 9. Receive probe from both APs one after the other.
     	// Client should be assigned to the first AP
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr2);
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress2), clientMacAddr2);
     	assertEquals(clientManager.getClients().size(), 2);
     	assertEquals(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	
     	// 10. Receive probe from an AP which is yet to register,
     	// for an unauthorised client which is scanning for the first time.
     	// This can occur as a race condition.
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress3), clientMacAddr3);
     	assertNull(agentManager.getAgents().get(InetAddress.getByName(ipAddress3)));
     	assertEquals(clientManager.getClients().size(), 2);
     	
     	// 11. Add client3
     	addClientToClientManagerSingleSsid(clientMacAddr3, InetAddress.getByName("172.17.2.53"), MACAddress.valueOf("00:00:00:00:11:13"), "odin");
     	assertEquals(clientManager.getClients().size(), 3);
     	assertEquals(clientManager.getClients().get(clientMacAddr3).getLvap().getAgent(), null);
 
     	// 10. Receive probe from an AP which is yet to register,
     	// for an authorised client which is scanning for the first time.
     	// This can occur as a race condition.
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress3), clientMacAddr3);
     	assertNull(agentManager.getAgents().get(InetAddress.getByName(ipAddress3)));
     	assertEquals(clientManager.getClients().size(), 3);
     	
     	// 11. Now add agent3
     	addAgentWithMockSwitch(ipAddress3, 12345);
 
     	// 12. Receive probe from an AP which has registered,
     	// for an authorised client which is scanning for the first time.
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress3), clientMacAddr3);
     	assertEquals(clientManager.getClients().size(), 3);
     	assertEquals(clientManager.getClients().get(clientMacAddr3).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress3));
     	
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress3), null);
     }
     
 
     /**
      * Test to see if OdinMaster.handoff()
      * works correctly
      * 
      * @throws Exception
      */
     @Test
     public void testHandoff() throws Exception {
     	String ipAddress1 = "172.17.2.161";
     	String ipAddress2 = "172.17.2.162";
     	String ipAddress3 = "172.17.2.163";
     	String ipAddress4 = "172.17.2.164";
     	
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	MACAddress clientMacAddr2 = MACAddress.valueOf("00:00:00:00:00:02");
     	
     	// Things shouldn't explode if this is called
     	odinMaster.handoffClientToAp(null, null);
         
     	
     	addClientToClientManagerSingleSsid(clientMacAddr1, InetAddress.getByName("172.17.2.51"), MACAddress.valueOf("00:00:00:00:11:11"), "odin");
   	
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	addAgentWithMockSwitch(ipAddress2, 12345);
     	addAgentWithMockSwitch(ipAddress3, 12345);
     	
     	assertEquals(clientManager.getClients().size(), 1);
     	assertNull(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent());
     	assertEquals(agentManager.getAgents().size(), 3);
 
     	///// Sane cases /////
     	
     	// Handoff client for the first time to AP that exists
     	odinMaster.handoffClientToAp(clientMacAddr1, InetAddress.getByName(ipAddress1));
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	// Handoff client to the same AP, nothing should change
     	odinMaster.handoffClientToAp(clientMacAddr1, InetAddress.getByName(ipAddress1));
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	// Handoff client to AP2 that exists
     	odinMaster.handoffClientToAp(clientMacAddr1, InetAddress.getByName(ipAddress2));
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress2));
     	
     	// Handoff client to AP3 that exists
     	odinMaster.handoffClientToAp(clientMacAddr1, InetAddress.getByName(ipAddress3));
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress3));
     	
     	// And back to AP1
     	odinMaster.handoffClientToAp(clientMacAddr1, InetAddress.getByName(ipAddress1));
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	
     	///// Less sane cases /////
     	
     	// Handoff unauthorised client around, it should never
     	// be assigned an LVAP
     	odinMaster.handoffClientToAp(clientMacAddr2, InetAddress.getByName(ipAddress1));
     	assertNull(clientManager.getClients().get(clientMacAddr2)); // If this is null, it can never be assigned an LVAP
     	
     	odinMaster.handoffClientToAp(clientMacAddr2, InetAddress.getByName(ipAddress2));
     	assertNull(clientManager.getClients().get(clientMacAddr2));
     	
     	// Now try handing off to non-existent AP
     	odinMaster.handoffClientToAp(clientMacAddr2, InetAddress.getByName(ipAddress4));
     	assertNull(clientManager.getClients().get(clientMacAddr2));
     	
     	
     	// now authorise the client
     	addClientToClientManagerSingleSsid(clientMacAddr2, InetAddress.getByName("172.17.2.52"), MACAddress.valueOf("00:00:00:00:11:12"), "odin");
     	assertNotNull(clientManager.getClients().get(clientMacAddr2));
     	assertNull(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent());
     	
     	// Handoff authorised client to non-existent agent,
     	// it should still not have an LVAP
     	odinMaster.handoffClientToAp(clientMacAddr2, InetAddress.getByName(ipAddress4));
     	assertNotNull(clientManager.getClients().get(clientMacAddr2));
     	assertNull(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent());
     	
     	// Now handoff to an existing agent
     	odinMaster.handoffClientToAp(clientMacAddr2, InetAddress.getByName(ipAddress1));
     	assertNotNull(clientManager.getClients().get(clientMacAddr2));
     	assertEquals(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	// Now handoff to a non-existing agent, the client's
     	// LVAP should not have changed
     	odinMaster.handoffClientToAp(clientMacAddr2, InetAddress.getByName(ipAddress4));
     	assertNotNull(clientManager.getClients().get(clientMacAddr2));
     	assertEquals(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     	
     	// Now handoff to a null agent, the client's agent
     	// assignment shouldn't have changed
     	odinMaster.handoffClientToAp(clientMacAddr2, null);
     	assertNotNull(clientManager.getClients().get(clientMacAddr2));
     	assertEquals(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
     }
 
 
     /**
      * Test to see if we can handle agent
      * failures correctly
      * 
      * @throws Exception
      */
     @Test
     public void testAgentLeave() throws Exception {
     	String ipAddress1 = "172.17.2.161";
     	String ipAddress2 = "172.17.2.162";
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	MACAddress clientMacAddr2 = MACAddress.valueOf("00:00:00:00:00:02");
     	agentManager.setAgentTimeout(1000);
     	
     	// Add an agent and associate a client to it
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	addClientToClientManagerSingleSsid(clientMacAddr1, InetAddress.getByName("172.17.2.51"), MACAddress.valueOf("00:00:00:00:11:11"), "odin");
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	
     	// There should be an agent, and a client recorded at the master
     	assertEquals(agentManager.getAgents().size(), 1);
     	assertEquals(clientManager.getClients().size(), 1);
 
     	Thread.sleep(1500);
     	
     	// Agent should have been removed by now, and the associated
     	// client should have no agent assigned to it
     	assertEquals(agentManager.getAgents().size(), 0);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent(), null);
     	
     	// Now ping again to revive the agent
     	odinMaster.receivePing(InetAddress.getByName(ipAddress1));
     	odinMaster.receivePing(InetAddress.getByName(ipAddress1));
     	
        	// Agent should be setup again
     	assertEquals(agentManager.getAgents().size(), 1);
  
     	// Client should remain unassigned
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent(), null);
     	
     	// Time it out again
     	Thread.sleep(1500);
     	assertEquals(agentManager.getAgents().size(), 0);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent(), null);
     	
     	// There is no instance for the agent at the master, but we
     	// mock a client scan that is forwarded by the agent to the
     	// controller. The client shouldn't be assigned to the agent
     	// yet.
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent(), null);
 
     	// Now let the agent ping again. Master will track it, but client
     	// will still remain unassigned
     	odinMaster.receivePing(InetAddress.getByName(ipAddress1));
     	assertEquals(agentManager.getAgents().size(), 1);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent(), null);
     	
     	// Now mock a client probe. Client should be assigned now.
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
 
     	
     	// The following tests the LVAP syncing mechanism
     	
     	// Add another agent that already is hosting an LVAP. Make it join.
     	
     	List<OdinClient> lvapList = new ArrayList<OdinClient>();
     	ArrayList<String> ssidList = new ArrayList<String> ();
     	ssidList.add("odin");
    	ssidList.add("odin-1");
    	ssidList.add("odin-2");
     	Lvap lvap = new Lvap (MACAddress.valueOf("00:00:00:00:11:12"), ssidList);
     	OdinClient oc = new OdinClient(clientMacAddr2, InetAddress.getByName("172.17.1.52"), lvap);
     	lvapList.add(oc);
         OdinAgentFactory.setOdinAgentType("MockOdinAgent");
     	OdinAgentFactory.setMockOdinAgentLvapList(lvapList);
     	
     	addAgentWithMockSwitch(ipAddress2, 12345);
     	
     	assertEquals(clientManager.getClients().size(), 2);
     	assertEquals(agentManager.getAgents().size(), 2);
     	assertEquals(clientManager.getClients().get(clientMacAddr2).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress2));
    	assertEquals(clientManager.getClients().get(clientMacAddr2).getLvap().getSsids().size(), 3);
     	
     	OdinAgentFactory.setMockOdinAgentLvapList(new ArrayList<OdinClient>());
     }
     
     
     /**
      * Test to see if we can handle agent
      * failures correctly
      * 
      * @throws Exception
      */
     @Test
     public void testLvapSyncRaceCondition() throws Exception {
 
     	String ipAddress1 = "172.17.2.161";
     	String ipAddress2 = "172.17.2.162";
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	
     	
     	agentManager.setAgentTimeout(1000);
  
     	// Add an agent and associate a client to it
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	addClientToClientManagerSingleSsid(clientMacAddr1, InetAddress.getByName("172.17.2.51"), MACAddress.valueOf("00:00:00:00:11:11"), "odin");
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	
     	assertEquals(clientManager.getClients().size(), 1);
     	assertEquals(agentManager.getAgents().size(), 1);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), InetAddress.getByName(ipAddress1));
 
     	List<OdinClient> lvapList = new ArrayList<OdinClient>();
     	ArrayList<String> ssidList = new ArrayList<String> ();
     	ssidList.add("odin");
     	Lvap lvap = new Lvap (MACAddress.valueOf("00:00:00:00:11:11"), ssidList);
     	OdinClient oc = new OdinClient(clientMacAddr1, InetAddress.getByName("172.17.2.51"), lvap);
     	lvapList.add(oc);
         OdinAgentFactory.setOdinAgentType("MockOdinAgent");
     	OdinAgentFactory.setMockOdinAgentLvapList(lvapList);
     	
     	addAgentWithMockSwitch(ipAddress2, 12345);
     	odinMaster.receivePing(InetAddress.getByName(ipAddress2));
     	
     	assertEquals(agentManager.getAgents().get(InetAddress.getByName(ipAddress1)).getLvapsRemote().contains(oc), true);
     	assertEquals(agentManager.getAgents().get(InetAddress.getByName(ipAddress2)).getLvapsRemote().contains(oc), false);
     	
     	OdinAgentFactory.setMockOdinAgentLvapList(new ArrayList<OdinClient>());
     }
     
     /**
      * Test to see if we can handle agent
      * failures correctly
      * 
      * @throws Exception
      */
     @Test
     public void testPostFailureLvapSync() throws Exception {
 
     	String ipAddress1 = "172.17.2.161";
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	
     	
     	agentManager.setAgentTimeout(1000);
 
     	List<OdinClient> lvapList = new ArrayList<OdinClient>();
     	ArrayList<String> ssidList = new ArrayList<String> ();
     	ssidList.add("odin");
    	ssidList.add("odin-1");
    	ssidList.add("odin-2");
     	Lvap lvap = new Lvap (MACAddress.valueOf("00:00:00:00:11:11"), ssidList);
     	OdinClient oc = new OdinClient(clientMacAddr1, InetAddress.getByName("172.17.2.51"), lvap);
     	lvapList.add(oc);
         OdinAgentFactory.setOdinAgentType("MockOdinAgent");
     	OdinAgentFactory.setMockOdinAgentLvapList(lvapList);
     	
     	assertEquals(odinMaster.getClients().size(), 0);
     	assertEquals(agentManager.getAgents().size(), 0);
     	
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	odinMaster.receivePing(InetAddress.getByName(ipAddress1));
     	
     	assertEquals(agentManager.getAgents().size(), 1);
     	assertEquals(agentManager.getAgents().get(InetAddress.getByName(ipAddress1)).getLvapsRemote().contains(oc), true);
     	assertEquals(odinMaster.getClients().size(), 1);
    	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getSsids().size(), 3);
     	
     	// Time out the agent
     	Thread.sleep(2000);
     	
     	assertEquals(agentManager.getAgents().size(), 0);
     	assertEquals(odinMaster.getClients().size(), 1);
     	assertEquals(odinMaster.getClients().get(clientMacAddr1).getLvap().getAgent(), null);
    	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getSsids().size(), 3);
     	
     	OdinAgentFactory.setMockOdinAgentLvapList(new ArrayList<OdinClient>());
     }
     	
     /**
      * Test to see if the publish subscribe
      * interfaces work correctly when there
      * are multiple applications, each
      * pushing down a single subscription
      * with a single handler.
      * 
      * @throws Exception
      */
     @Test
     public void testSubscriptionsOneToOne() throws Exception {
     	DummyApplication1 app1 = new DummyApplication1();
     	DummyApplication1 app2 = new DummyApplication1();
     	app1.setOdinInterface(odinMaster);
     	app1.run(); // This isn't really a thread, but sets up callbacks
     	
     	String ipAddress1 = "172.17.2.161";
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	agentManager.setAgentTimeout(1000);
     	
     	// Add an agent with no clients associated
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	Map<Long, Long> subscriptionIds = new HashMap<Long, Long>();
     	long id1 = 1;
     	long id2 = 2;
     	long id3 = 10;
     	long id4 = 0;
     	
     	// not-so-sane cases //
     	subscriptionIds.put(id3, 10L);
 
     	/**
     	 * Shouldn't trigger anything
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
 
     	// not-so-sane cases //
     	subscriptionIds.put(id4, 10L);
 
     	/**
     	 * Still shouldn't trigger anything
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
 
     	/**
     	 * Still still shouldn't trigger anything
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
 
     	odinMaster.receivePublish(null, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
 
     	odinMaster.receivePublish(clientMacAddr1, null, subscriptionIds);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), null);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
     	
     	// Semi-sane cases //
     	subscriptionIds.put(id1, 10L);
     	
     	odinMaster.receivePublish(null, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
     	
     	odinMaster.receivePublish(clientMacAddr1, null, subscriptionIds);
     	assertEquals(app1.counter, 0);
     	assertEquals(app2.counter, 0);
     	
     	
     	// Sane cases //
     	
     	/**
     	 * The event registered should be subscription Id 1.
     	 * This test will break when we change subscriptions to
     	 * hash values.
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 1);
     	assertEquals(app2.counter, 0);
 
     	
     	/**
     	 * Now let app2 register its subscription
     	 */
     	app2.setOdinInterface(odinMaster);
     	app2.run();
     	
     	/**
     	 * Should not trigger app2's handler
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 2);
     	assertEquals(app2.counter, 0);
     	
     	subscriptionIds.clear();
     	subscriptionIds.put(id2, 10L);
 
     	/**
     	 * Should only trigger app2's handler
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 2);
     	assertEquals(app2.counter, 1);
     	
     	
     	odinMaster.unregisterSubscription(id1);
     	
     	/**
     	 * Should only trigger app2's handler
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 2);
     	assertEquals(app2.counter, 2);
     	
     	odinMaster.unregisterSubscription(id1);
     	
     	/**
     	 * Should only trigger app2's handler
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 2);
     	assertEquals(app2.counter, 3);
     	
     	odinMaster.unregisterSubscription(id2);
     	
     	/**
     	 * Should not trigger any handler
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 2);
     	assertEquals(app2.counter, 3);
     }
     
 
     /**
      * Test to see if the publish subscribe
      * interfaces work correctly when there
      * are multiple applications, each
      * pushing down multiple subscriptions
      * bound to a single handler.
      * 
      * @throws Exception
      */
     @Test
     public void testSubscriptionsOneToMany() throws Exception {
      	DummyApplication2 app1 = new DummyApplication2();
     	DummyApplication2 app2 = new DummyApplication2();
     	app1.setOdinInterface(odinMaster);
     	app1.run(); // This isn't really a thread, but sets up callbacks
     	
 
     	String ipAddress1 = "172.17.2.161";
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	agentManager.setAgentTimeout(1000);
     	
     	// Add an agent with no clients associated
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	Map<Long, Long> subscriptionIds = new HashMap<Long, Long>();
     	long id1 = 1;
     	long id2 = 2;
     	long id3 = 3;
     	long id4 = 4;
     	
     	
     	subscriptionIds.put(id1, 10L);
     	
     	/**
     	 * The event registered should be subscription Id 1.
     	 * This test will break when we change subscriptions to
     	 * hash values.
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 1);
     	assertEquals(app2.counter, 0);
     	
 
     	/**
     	 * add subscription2  as well. The handler should now be called twice for app1.
     	 */
     	subscriptionIds.put(id2, 10L);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 3);
     	assertEquals(app2.counter, 0);
     
     	
     	/**
     	 * Now let app1 register its subscription
     	 */
     	app2.setOdinInterface(odinMaster);
     	app2.run();
     	
     	/**
     	 * Invoke app1's subscriptions, should not invoke app2's handlers
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 5);
     	assertEquals(app2.counter, 0);
     	
     	/**
     	 * now invoke only app2's subscriptions
     	 */
     
     	subscriptionIds.clear();
     	subscriptionIds.put(id3, 10L);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 5);
     	assertEquals(app2.counter, 1);
     	
     	subscriptionIds.put(id4, 10L);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 5);
     	assertEquals(app2.counter, 3);
     	
     	/**
     	 * now invoke both app's subscriptions
     	 */
     
     	subscriptionIds.clear();
     	subscriptionIds.put(id1, 10L);
     	subscriptionIds.put(id3, 10L);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 6);
     	assertEquals(app2.counter, 4);
     	
     	subscriptionIds.put(id2, 10L);
     	subscriptionIds.put(id4, 10L);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter, 8);
     	assertEquals(app2.counter, 6);
     }
     
     /**
      * Test to see if the publish subscribe
      * interfaces work correctly when there
      * are multiple applications, each
      * pushing down multiple subscriptions
      * bound to a a handler each.
      * 
      * @throws Exception
      */
     @Test
     public void testSubscriptionsManyToMany() throws Exception {
     	DummyApplication3 app1 = new DummyApplication3();
     	DummyApplication3 app2 = new DummyApplication3();
     	app1.setOdinInterface(odinMaster);
     	app1.run(); // This isn't really a thread, but sets up callbacks
     	
     	String ipAddress1 = "172.17.2.161";
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	agentManager.setAgentTimeout(1000);
     	
     	// Add an agent with no clients associated
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	Map<Long, Long> subscriptionIds = new HashMap<Long, Long>();
     	long id1 = 1;
     	long id2 = 2;
     	
     	
     	subscriptionIds.put(id1, 10L);
     	
     	/**
     	 * The event registered should be subscription Id 1.
     	 * This test will break when we change subscriptions to
     	 * hash values.
     	 */
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter1, 1);
     	assertEquals(app1.counter2, 0);
     	
 
     	subscriptionIds.clear();
     	subscriptionIds.put(id2, 10L);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter1, 1);
     	assertEquals(app1.counter2, 1);
     	
     	subscriptionIds.put(id1, 10L);
     	
     	odinMaster.receivePublish(clientMacAddr1, InetAddress.getByName(ipAddress1), subscriptionIds);
     	assertEquals(app1.counter1, 2);
     	assertEquals(app1.counter2, 2);
     }
         
     /**
      * Test to see if the LVAP generation works correctly
      * 
      * @throws Exception
      */
     @Test
     public void testLvapGeneration() throws Exception {
     	LvapManagerImpl lvapManager = new LvapManagerImpl();
     	lvapManager.addNetwork("odin");
     	
     	Class target = Class.forName("net.floodlightcontroller.odinmaster.LvapManagerImpl");
     
     	Class[] paramTypes = {MACAddress.class};
     	Method method = target.getDeclaredMethod("assignLvapWithNullIp", paramTypes);
     	method.setAccessible(true);
  
     	Object[] parameters = { MACAddress.valueOf("00:00:00:00:00:01") };
     	
     	OdinClient oc1 = (OdinClient) method.invoke(lvapManager, parameters);
     	
     	parameters[0] = MACAddress.valueOf("00:00:00:00:00:02");
     	
     	OdinClient oc2 = (OdinClient) method.invoke(lvapManager, parameters);
     	
     	// Different clients, different details
     	assertTrue( !oc1.getLvap().getBssid().equals(oc2.getLvap().getBssid()) );
     	assertTrue( oc1.getIpAddress().getHostAddress().equals("0.0.0.0") );
     	assertTrue( oc2.getIpAddress().getHostAddress().equals("0.0.0.0") );
     	//assertTrue( oc1.getLvap().getSsids().get(0) == oc2.getLvap().getSsids().get(0) ); // FIXME: Assumes single SSID
     	assertTrue( oc1.getMacAddress() != oc2.getMacAddress() );
 
     	// Shouldn't be called, but still.
     	OdinClient oc3 = (OdinClient) method.invoke(lvapManager, parameters);
     	
     	assertTrue( oc3.getLvap().getBssid().equals(oc2.getLvap().getBssid()) );
     	assertTrue( oc3.getIpAddress().getHostAddress().equals("0.0.0.0") );
     	assertTrue( oc2.getIpAddress().getHostAddress().equals("0.0.0.0") );
     	//assertTrue( oc3.getLvap().getSsids().get(0) == oc2.getLvap().getSsids().get(0) ); // FIXME: Assumes single SSID
     	assertTrue( oc3.getMacAddress() == oc2.getMacAddress() );
     }
     
     // Application that registers 1 subscription -> 1 handler
     private class DummyApplication1 extends OdinApplication {
     	public int counter = 0;
     		
 		@Override
 		public void run() {
 			OdinEventSubscription oes = new OdinEventSubscription();
 			oes.setSubscription("*", "signal", Relation.GREATER_THAN, 180);
 			
 			NotificationCallback cb = new NotificationCallback() {
 				
 				@Override
 				public void exec(OdinEventSubscription oes, NotificationCallbackContext cntx) {
 					callback1(oes, cntx);
 				}
 			};
 			
 			odinMaster.registerSubscription(oes, cb);
 		}
 		
     	private void callback1(OdinEventSubscription oes, NotificationCallbackContext cntx){
     		counter += 1;
     	}
     }
     
     // Application that registers 2 subscription -> 1 handler    
     private class DummyApplication2 extends OdinApplication {
     	public int counter = 0;
     		
 		@Override
 		public void run() {
 			OdinEventSubscription oes1 = new OdinEventSubscription();
 			OdinEventSubscription oes2 = new OdinEventSubscription();
 			oes1.setSubscription("*", "signal", Relation.GREATER_THAN, 180);
 			oes2.setSubscription("00:00:00:00:00:03", "signal", Relation.LESSER_THAN, 150);
 			
 			NotificationCallback cb = new NotificationCallback() {
 				
 				@Override
 				public void exec(OdinEventSubscription oes, NotificationCallbackContext cntx) {
 					callback1(oes, cntx);
 				}
 			};
 			
 			odinMaster.registerSubscription(oes1, cb);
 			odinMaster.registerSubscription(oes2, cb);
 		}
 		
     	private void callback1(OdinEventSubscription oes, NotificationCallbackContext cntx){
     		counter += 1;
     	}
     }
 
     // Application that registers 2 subscriptions, and handler for each
     private class DummyApplication3 extends OdinApplication {
     	public int counter1 = 0;
     	public int counter2 = 0;
     		
 		@Override
 		public void run() {
 			OdinEventSubscription oes1 = new OdinEventSubscription();
 			OdinEventSubscription oes2 = new OdinEventSubscription();
 			oes1.setSubscription("*", "signal", Relation.GREATER_THAN, 180);
 			oes2.setSubscription("00:00:00:00:00:03", "signal", Relation.LESSER_THAN, 150);
 			
 			NotificationCallback cb1 = new NotificationCallback() {
 				
 				@Override
 				public void exec(OdinEventSubscription oes, NotificationCallbackContext cntx) {
 					callback1(oes, cntx);
 				}
 			};
 			
 			NotificationCallback cb2 = new NotificationCallback() {
 				
 				@Override
 				public void exec(OdinEventSubscription oes, NotificationCallbackContext cntx) {
 					callback2(oes, cntx);
 				}
 			};
 			
 			odinMaster.registerSubscription(oes1, cb1);
 			odinMaster.registerSubscription(oes2, cb2);
 		}
 		
     	private void callback1(OdinEventSubscription oes, NotificationCallbackContext cntx){
     		counter1 += 1;
     	}
     	
     	private void callback2(OdinEventSubscription oes, NotificationCallbackContext cntx){
     		counter2 += 1;
     	}
     }
     
     /************* Mobility Manager Tests ******************/
     
     private void triggerSingleEvent (MACAddress clientMacAddr, InetAddress agentAddr, long id, long value) {
     	// The app's subscription would be id 1
     	Map<Long, Long> subscriptionIds = new HashMap<Long, Long>();
     	subscriptionIds.put(id, value);
     	odinMaster.receivePublish(clientMacAddr, agentAddr, subscriptionIds);    	
     }
     
     /**
      * - The client should always be assigned to the agent where it has
      *   the highest receiver signal strength
      * - The client should never be re-assigned within the hysteresis
      *   interval
      * - A client that has left the range of the network and re-appears
      *   at another end must be immediately re-assigned wherever.
      */
     @Test   
     public void testMobilityManagerTwoAps() throws Exception {
     	OdinMobilityManager app = new OdinMobilityManager(1000, 2000, 10);
     	app.setOdinInterface(odinMaster);
     	app.run(); // This isn't really a thread, but sets up callbacks
 
     	String ipAddress1 = "172.17.2.161";
     	String ipAddress2 = "172.17.2.162";
     	InetAddress agentAddr1 = InetAddress.getByName(ipAddress1);
     	InetAddress agentAddr2 = InetAddress.getByName(ipAddress2);
     	MACAddress clientMacAddr1 = MACAddress.valueOf("00:00:00:00:00:01");
     	agentManager.setAgentTimeout(3000);
     	
     	// Add an agent with no clients associated
     	addAgentWithMockSwitch(ipAddress1, 12345);
     	addAgentWithMockSwitch(ipAddress2, 12345);
     	
     	// Add a client at agent1
     	odinMaster.receiveProbe(InetAddress.getByName(ipAddress1), clientMacAddr1);
     	triggerSingleEvent (clientMacAddr1, agentAddr1, 1, 200);
     	assertEquals(clientManager.getClients().size(), 1);
     	assertEquals(agentManager.getAgents().size(), 2);
     	assertNotNull(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent());
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr1);    	
 
     	// We should be within the hysteresis period now, so if we receive an event at
     	// agent 2, there shouldn't be a handoff
     	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 200);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr1);
     	assertEquals(agentManager.getAgents().size(), 2);
     	
     	// Sleep enough to get past the hysteresis period
     	Thread.sleep(1001);
     	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 200);
     	
     	// Shouldn't happen because the signal strength isn't higher than
     	// the current one
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr1);
     	assertEquals(agentManager.getAgents().size(), 2);
     	
     	// Should now switch
     	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 211);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr2);
     	assertEquals(agentManager.getAgents().size(), 2);
     	
     	// Now receive a trigger at the first AP, within current hysteresis cycle.
     	// There should be no handoff.
     	triggerSingleEvent (clientMacAddr1, agentAddr1, 1, 220);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr2);
     	assertEquals(agentManager.getAgents().size(), 2);
     	
     	// Now reduce the signal strengtha t AP1, and follow it
     	// with a trigger at AP2. Still no handoff due to hysteresis.
     	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 200);
     	triggerSingleEvent (clientMacAddr1, agentAddr1, 1, 211);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr2);
     	assertEquals(agentManager.getAgents().size(), 2);    	
     	
     	// Sleep enough to get past the hysteresis period
     	Thread.sleep(1001);
     	
     	// Now repeat the last sequence, client should be handed
     	// off back to AP1.
       	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 200);
     	triggerSingleEvent (clientMacAddr1, agentAddr1, 1, 211);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr1);
     	assertEquals(agentManager.getAgents().size(), 2);
     	
     	// Now let the client go out of range of the network (exceed idle threshold)
     	Thread.sleep(2001);
     	
     	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 190);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr2);
     	assertEquals(agentManager.getAgents().size(), 2);
     	odinMaster.receivePing(agentAddr1);
     	
     	// Now let the current agent die out, giving the client a null agent
     	Thread.sleep(2001);
     	odinMaster.receivePing(agentAddr1);
     	Thread.sleep(2001);
 
     	assertEquals(agentManager.getAgents().size(), 1);
     	
     	// Whoever hears the client should now be assigned
     	// the client.
     	triggerSingleEvent (clientMacAddr1, agentAddr1, 1, 160);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr1);
     	
     	// Let the other agent come back immediately, we need to wait
     	// at least for the hysteresis period for the client to get
     	// re-assigned. So it should be a no-op
     	odinMaster.receivePing(agentAddr2);
     	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 200);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr1);
     	
     	// Wait past hysteresis period, and now retry
     	Thread.sleep(1001);
     	odinMaster.receivePing(agentAddr2);
     	triggerSingleEvent (clientMacAddr1, agentAddr2, 1, 200);
     	assertEquals(clientManager.getClients().get(clientMacAddr1).getLvap().getAgent().getIpAddress(), agentAddr2);
     }
 }
