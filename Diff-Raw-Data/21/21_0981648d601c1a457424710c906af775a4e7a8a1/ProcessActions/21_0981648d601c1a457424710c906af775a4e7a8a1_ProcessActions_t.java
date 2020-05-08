 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either Test 2 of the License, or
  * (at your option) any later Test.
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
 
 package org.umit.icm.mobile.process;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.umit.icm.mobile.aggregator.AggregatorRetrieve;
 import org.umit.icm.mobile.connectivity.Service;
 import org.umit.icm.mobile.connectivity.Website;
 import org.umit.icm.mobile.p2p.MessageSender;
 import org.umit.icm.mobile.proto.MessageProtos.AgentData;
 import org.umit.icm.mobile.proto.MessageProtos.AuthenticatePeer;
 import org.umit.icm.mobile.proto.MessageProtos.Event;
 
 import org.umit.icm.mobile.proto.MessageProtos.NewTests;
 import org.umit.icm.mobile.proto.MessageProtos.NewVersion;
 import org.umit.icm.mobile.proto.MessageProtos.NewVersionResponse;
 import org.umit.icm.mobile.proto.MessageProtos.RSAKey;
 import org.umit.icm.mobile.proto.MessageProtos.RegisterAgentResponse;
 import org.umit.icm.mobile.proto.MessageProtos.ResponseHeader;
 import org.umit.icm.mobile.proto.MessageProtos.Test;
 import org.umit.icm.mobile.utils.CryptoKeyReader;
 import org.umit.icm.mobile.utils.RSACrypto;
 
 /**
  * Performs actions based on aggregator and P2P communication responses.
  */
 
 public class ProcessActions {	
 	/**
 	 * Checks if the response agent version is higher than the current
 	 * agent number. If yes, updates the agent version and makes a webservice
 	 * call to the aggregator to be the latest version.
 	 * 
 	 *
 	 
 	 @param header Response header of type {@link ResponseHeader}
 	 * @throws Exception 
 	 *
 	 
 	 @see VersionManager
 	 */
 	public static void updateAgentVersion(ResponseHeader header) 
 		throws Exception {
 		
 		if (header.getCurrentVersionNo() 
 				> Globals.versionManager.getAgentVersion()) {
 			Globals.versionManager.setAgentVersion(header.getCurrentVersionNo());
 			
 			NewVersion newVersion = NewVersion.newBuilder()
 			.setAgentVersionNo(Globals.versionManager.getAgentVersion())
 			.setAgentType(Constants.AGENT_TYPE)
 			.build();
 			AggregatorRetrieve.checkVersion(newVersion);
 		}
 	}
 	
 	/**
 	 * Checks if the response tests version is higher than the current
 	 * tests number. If yes, updates the tests version and makes a webservice
 	 * call to the aggregator to get the latest tests version.
 	 * 
 	 *
 	 
 	 @param header Response header of type {@link ResponseHeader}
 	 * @throws Exception 
 	 *
 	 
 	 @see VersionManager
 	 */
 	public static void updateTestsVersion(ResponseHeader header)
 		throws Exception {
 		
 		if (header.getCurrentTestVersionNo() 
 				> Globals.versionManager.getTestsVersion()) {
 			Globals.versionManager.setTestsVersion(header.getCurrentTestVersionNo());
 			
 			NewTests newTests = NewTests.newBuilder()
 			.setCurrentTestVersionNo(Globals.versionManager.getTestsVersion())
 			.build();
 			AggregatorRetrieve.checkTests(newTests);
 		}
 	}
 			
 	/**
 	 * Patches the current agent
 	 * 
 	 *
 	 
 	 @param newVersionResponse Response header of type {@link NewVersionResponse}
 	 */
 	public static boolean updateAgent(NewVersionResponse newVersionResponse) {
 		// TODO patch current binary
 		return true;
 	}
 	
 	/**
 	 * Adds the new tests to {@link Globals#testsList}.
 	 * 
 	 *
 	 
 	 @param tests	{@link List} of {@link Test}
 	 *
 	 
 	 @return boolean
 	 *
 	 
 	 @see Test
 	 */
 	public synchronized static boolean updateTests(List<Test> tests) {
 		for(int i = 0 ; i < tests.size(); i++) {
			if(tests.get(i).getTestType() == 1) { // website
 				Globals.websitesList.add(
 						new Website(tests.get(i).getWebsite().getUrl(), 
 								"false", 
 								"true", 
 								tests.get(i).getTestID(), 
 								tests.get(i).getExecuteAtTimeUTC()));
 				
			} else if(tests.get(i).getTestType() == 2) { //service
 				Globals.servicesList.add(
 						new Service(tests.get(i).getService().getName(), 
								tests.get(i).getService().getPort(),
 								tests.get(i).getService().getIp(), 
 								"open", 
 								"true", 
 								tests.get(i).getTestID(), 
 								tests.get(i).getExecuteAtTimeUTC()));
 			}
 			
 		}			
 		return true;
 	}
 	
 	/**
 	 * Adds new events to {@link Globals#eventsList}.
 	 * 
 	 *
 	 
 	 @param events	{@link List} of {@link Event}
 	 *
 	 
 	 @return boolean
 	 *
 	 
 	 @see Event
 	 */
 	public synchronized static boolean updateEventsList(List<Event> events) {
 		Globals.runtimesList.setEventsList(events);
 		return true;
 	}
 	
 	/**
 	 * Adds new peers to {@link Globals#peersList}.
 	 * 
 	 *
 	 
 	 @param peers	{@link List} of {@link AgentData}
 	 *
 	 
 	 @return boolean
 	 *
 	 
 	 @see AgentData
 	 */
 	public synchronized static boolean updatePeersList(List<AgentData> peers) {
 		Iterator<AgentData> iterator = peers.iterator();	
 		RSAKey cipheredKey = RSAKey.newBuilder()
 		.setExp(Globals.keyManager.getMyCipheredKeyExp())
 		.setMod(Globals.keyManager.getMyCipheredKeyMod())
 		.build();
 		
 		AuthenticatePeer authenticatePeer = AuthenticatePeer.newBuilder()
 		.setAgentType(Constants.AGENT_TYPE_NUMBER)
 		.setAgentPort(Constants.MY_TCP_PORT)
 		.setAgentID(Globals.runtimeParameters.getAgentID())
 		.setCipheredPublicKey(cipheredKey)
 		.build();
 		
 		RSAKey rsaKey;
 		try {
 			rsaKey = RSACrypto.getPublicKeyIntegers(CryptoKeyReader.getMyDHPublicKey());
 			
 		} catch (NoSuchAlgorithmException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InvalidKeySpecException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		AgentData peer = null;
 		while(iterator.hasNext()) {
 			peer = iterator.next();
 			if(Globals.authenticatedPeers.checkPeer(peer) == false) {
 				try {
 					MessageSender.authenticatePeer(peer, authenticatePeer);
 
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		
 			}
 			
 		}
 		Globals.runtimesList.setPeersList(peers);
 		return true;
 	}
 	
 	/**
 	 * Adds new super peers to {@link Globals#superPeersList}.
 	 * 
 	 *
 	 
 	 @param superPeers	{@link List} of {@link AgentData}
 	 *
 	 
 	 @return boolean
 	 *
 	 
 	 @see AgentData
 	 */
 	public synchronized static boolean updateSuperPeersList(List<AgentData> superPeers) {
 		Iterator<AgentData> iterator = superPeers.iterator();	
 		RSAKey cipheredKey = RSAKey.newBuilder()
 		.setExp(Globals.keyManager.getMyCipheredKeyExp())
 		.setMod(Globals.keyManager.getMyCipheredKeyMod())
 		.build();
 		
 		AuthenticatePeer authenticatePeer = AuthenticatePeer.newBuilder()
 		.setAgentType(Constants.AGENT_TYPE_NUMBER)
 		.setAgentPort(Constants.MY_TCP_PORT)
 		.setAgentID(Globals.runtimeParameters.getAgentID())
 		.setCipheredPublicKey(cipheredKey)
 		.build();
 		
 		RSAKey rsaKey;
 		try {
 			rsaKey = RSACrypto.getPublicKeyIntegers(CryptoKeyReader.getMyDHPublicKey());
 
 		} catch (NoSuchAlgorithmException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InvalidKeySpecException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		AgentData peer = null;
 		while(iterator.hasNext()) {
 			peer = iterator.next();
 			if(Globals.authenticatedPeers.checkPeer(peer) == false) {
 				try {
 					MessageSender.authenticatePeer(peer, authenticatePeer);
 
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		
 			}
 		
 		}
 		Globals.runtimesList.setSuperPeersList(superPeers);
 		return true;
 	}
 	
 	/**
 	 * Adds the agentID received from the aggregator after registration.
 	 * 
 	 *
 	 
 	 @param registerAgentResponse	Response message of {@link RegisterAgentResponse}
 	 *
 	 
 	 @return boolean
 	 *	 
 	 
 	 @see RuntimeParameters
 	 */
 	public static boolean registerAgent(RegisterAgentResponse registerAgentResponse) {
 		System.out.println("Inside ProcessActions@registerAgent");
 		try {
 			Globals.runtimeParameters.setAgentID(registerAgentResponse.getAgentID());
 			return true;
 		} catch (RuntimeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 
 }
