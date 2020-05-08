 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile.test.p2p;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.umit.icm.mobile.p2p.P2PActions;
 import org.umit.icm.mobile.process.Globals;
 import org.umit.icm.mobile.proto.MessageProtos.*;
 
 import android.test.AndroidTestCase;
 
 
 public class P2PActionsTests extends AndroidTestCase {
 
     
     public void testGetPeerList() throws Throwable {
     	RSAKey rsaKey1 = RSAKey.newBuilder()
 		.setExp("exp1")
 		.setMod("mod1")
 		.build();
     	AgentData agent1 = AgentData.newBuilder()
     	.setAgentIP("IP1")
     	.setAgentID("1")
     	.setAgentPort(11)
     	.setPeerStatus("On")
     	.setPublicKey(rsaKey1)
     	.setToken("token1")
     	.build();
     	
     	RSAKey rsaKey2 = RSAKey.newBuilder()
 		.setExp("exp2")
 		.setMod("mod2")
 		.build();
     	AgentData agent2 = AgentData.newBuilder()
     	.setAgentIP("IP2")
     	.setAgentID("2")
     	.setAgentPort(12)
     	.setPeerStatus("On")
     	.setPublicKey(rsaKey2)
     	.setToken("token2")
     	.build();
     	
     	P2PGetPeerListResponse getPeerListResponse 
     	= P2PGetPeerListResponse.newBuilder()    	
     	.addPeers(agent1)
     	.addPeers(agent2)
     	.build();
     	
     	P2PActions.getPeerListAction(getPeerListResponse);
     	    	
     	Assert.assertTrue(compareAgentData(Globals.runtimesList.getPeersList().get(0), agent1));
     	Assert.assertTrue(compareAgentData(Globals.runtimesList.getPeersList().get(1), agent2));
                         
     }        
     
     private boolean compareAgentData(AgentData agent1, AgentData agent2) {
     	if(agent1.getAgentIP().equals(agent2.getAgentIP())
     			&& agent1.getAgentPort() == agent2.getAgentPort()
     			&& agent1.getPeerStatus().equals(agent2.getPeerStatus())
     			&& agent1.getPublicKey().getExp().equals(agent2.getPublicKey().getExp())
     			&& agent1.getPublicKey().getMod().equals(agent2.getPublicKey().getMod())
     			&& agent1.getAgentID() == agent2.getAgentID()
     			&& agent1.getToken().equals(agent2.getToken()))
     		return true;
     	return false;
     }
     
     public void testGetSuperPeerList() throws Throwable {
     	RSAKey rsaKey3 = RSAKey.newBuilder()
 		.setExp("exp3")
 		.setMod("mod3")
 		.build();
     	AgentData agent1 = AgentData.newBuilder()
     	.setAgentIP("IP3")
     	.setAgentID("3")
     	.setAgentPort(13)
     	.setPeerStatus("On")
     	.setPublicKey(rsaKey3)
     	.setToken("token3")
     	.build();
     	
     	RSAKey rsaKey4 = RSAKey.newBuilder()
 		.setExp("exp4")
 		.setMod("mod4")
 		.build();
     	AgentData agent2 = AgentData.newBuilder()
     	.setAgentIP("IP4")
     	.setAgentID("4")
     	.setAgentPort(14)
     	.setPeerStatus("On")
     	.setPublicKey(rsaKey4)
     	.setToken("token4")
     	.build();
     	
     	P2PGetSuperPeerListResponse getSuperPeerListResponse 
     	= P2PGetSuperPeerListResponse.newBuilder()    	
     	.addPeers(agent1)
     	.addPeers(agent2)
     	.build();
     	
     	P2PActions.getSuperPeerListAction(getSuperPeerListResponse);
         
     	Assert.assertTrue(compareAgentData(Globals.runtimesList.getSuperPeersList().get(0), agent1));
     	Assert.assertTrue(compareAgentData(Globals.runtimesList.getSuperPeersList().get(1), agent2));
                         
     } 
     
     public void testCheckNewTests() throws Throwable {
     	ResponseHeader responseHeader = ResponseHeader.newBuilder()
     	.setCurrentTestVersionNo(26)
     	.setCurrentVersionNo(26)
     	.build();
 
     	Website website = Website.newBuilder()
     	.setUrl("url1")
     	.build();
     	
     	Test test1 = Test.newBuilder()
     	.setExecuteAtTimeUTC(11)
     	.setWebsite(website)
     	.setTestID("31")    	
     	.setTestType(1)
     	.build();
     	
     	Service service = Service.newBuilder()
     	.setIp("ip")
     	.setName("name")
     	.setPort(1000)
     	.build();
     	
     	Test test2 = Test.newBuilder()
     	.setExecuteAtTimeUTC(12)    	
     	.setTestID("32")
     	.setService(service)
     	.setTestType(2)
     	.build();
     	
     	NewTestsResponse newTestsResponse = NewTestsResponse.newBuilder()
     	.setHeader(responseHeader)
     	.setTestVersionNo(23)
     	.addTests(test1)
     	.addTests(test2)
     	.build();
     	
     	P2PActions.receiveTaskListAction(newTestsResponse);
     		    	
     	 Assert.assertTrue(Globals.websitesList.get(Globals.websitesList.size()-1)
          		.equals(        		
          		new org.umit.icm.mobile.connectivity.Website(test1.getWebsite().getUrl(), 
  						"false", 
  						"true", 
  						test1.getTestID(), 
  						test1.getExecuteAtTimeUTC())));
         List<Integer> ports = new ArrayList<Integer>();
         ports.clear();
 		ports.add(test2.getService().getPort());
          Assert.assertTrue(Globals.servicesList.get(Globals.servicesList.size()-1)
          		.equals(        		
          		new org.umit.icm.mobile.connectivity.Service(test2.getService().getName(), 
  						ports,
  						test2.getService().getIp(), 
  						"open", 
  						"true", 
  						test2.getTestID(), 
  						test2.getExecuteAtTimeUTC())));                        
     } 
  
     public void testGetEvents() throws Throwable {
     	ResponseHeader responseHeader = ResponseHeader.newBuilder()
     	.setCurrentTestVersionNo(27)
     	.setCurrentVersionNo(27)
     	.build();
     	
     	Location location = Location.newBuilder()
     	.setLatitude(10.1)
     	.setLongitude(10.1)
     	.build();
     	
     	Event event1 = Event.newBuilder()
     	.setEventType("CENSOR")
     	.addLocations(location)
     	.setSinceTimeUTC(100)
     	.setTimeUTC(1000)
     	.setTestType("SERVICE")
     	.build();
     	
     	Event event2 = Event.newBuilder()
     	.setEventType("OFF_LINE")
     	.addLocations(location)
     	.setSinceTimeUTC(101)
     	.setTimeUTC(1001)
     	.setTestType("WEB")
     	.build();
     	
     	GetEventsResponse getEventsResponse 
     	= GetEventsResponse.newBuilder()
     	.setHeader(responseHeader)
     	.addEvents(event1)
     	.addEvents(event2)
     	.build();
     	
     	P2PActions.receiveEventsAction(getEventsResponse);
     	
         Assert.assertEquals(Globals.versionManager.getAgentVersion(), 27);
         Assert.assertEquals(Globals.versionManager.getTestsVersion(), 27);
         Assert.assertTrue(compareEvent(Globals.runtimesList.getEventsList().get(0), event1));
         Assert.assertTrue(compareEvent(Globals.runtimesList.getEventsList().get(1), event2));
                         
     } 
     
     private boolean compareEvent(Event event1, Event event2) {
     	if(event1.getEventType().equals(event2.getEventType())
     			&& event1.getLocationsList().equals(event2.getLocationsList())
     			&& event1.getSinceTimeUTC() == event2.getSinceTimeUTC()
     			&& event1.getTimeUTC() == event2.getTimeUTC()
     			&& event1.getTestType().equals(event2.getTestType()))
     		return true;
     	return false;
     }       
 
 }
