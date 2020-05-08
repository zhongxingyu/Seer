 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.societies.context.externalBrokerConnector;
import java.net.URI;
import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.comm.xmpp.interfaces.ICommManager;
 import org.societies.api.context.CtxException;
 import org.societies.api.context.broker.ICtxBroker;
 import org.societies.api.context.event.CtxChangeEventListener;
 import org.societies.api.context.model.CommunityCtxEntity;
 import org.societies.api.context.model.CtxAttribute;
 import org.societies.api.context.model.CtxAttributeTypes;
 import org.societies.api.context.model.CtxEntity;
 import org.societies.api.context.model.CtxEntityIdentifier;
 import org.societies.api.identity.IIdentity;
 import org.societies.api.identity.IIdentityManager;
 import org.societies.api.identity.InvalidFormatException;
 import org.societies.api.identity.RequestorService;
 import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
 import org.societies.api.services.IServices;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * Connector for using Context Broker in CollabTools
  *
  * @author NikosK and Chris Lima
  *
  */
 @Service
 public class ContextAware3pService implements IContextAware3pService  {
 
 
 	private Logger LOG = LoggerFactory.getLogger(this.getClass());
 
 	//services
 	private ICtxBroker ctxBroker;
 	private ICommManager commsMgr;
 	private IIdentityManager idMgr;
 	private IServices serviceMgmt;
 
 	// identities
 	private RequestorService requestorService;
 	private IIdentity userIdentity;
 	private ServiceResourceIdentifier myServiceID;
 
 	private CtxChangeEventListener myCtxChangeEventListener;
 
 
 	@Autowired(required=true)
 	public ContextAware3pService(ICtxBroker ctxBroker, ICommManager commsMgr, IServices serviceMgmt){
 
 		LOG.info("*** ContextAware3pService started");
 
 		//services
 		this.ctxBroker = ctxBroker;
 		this.commsMgr = commsMgr;
 		this.idMgr = commsMgr.getIdManager();
 		this.serviceMgmt = serviceMgmt;
 
 		LOG.info("ctxBroker: "+this.ctxBroker);
 		LOG.info("commsMgr : "+this.commsMgr );
 		LOG.info("idMgr : "+this.idMgr );
 		LOG.info("serviceMgmt : "+this.serviceMgmt );
 
 
 		//		this.userIdentity = this.idMgr.getThisNetworkNode();
 		//		try {
 		//			this.serviceIdentity = this.idMgr.fromJid("cviana@societies.org");
 		//		} catch (InvalidFormatException e) {
 		//			e.printStackTrace();
 		//		}
 		//
 		//		myServiceID = new ServiceResourceIdentifier();
 		//		myServiceID.setServiceInstanceIdentifier("css://cviana@societies.org/Context3pServiceConnector");
 		//		try {
 		//			myServiceID.setIdentifier(new URI("css://cviana@societies.org/Context3pServiceConnector"));
 		//		} catch (URISyntaxException e) {
 		//			e.printStackTrace();
 		//		}
 		//		requestorService = new RequestorService(serviceIdentity, myServiceID);
 		//
 		//		LOG.info("userIdentity : "+ userIdentity.getBareJid());
 		//		LOG.info("requestor service : "+requestorService);
 	}
 
 
 	/**
 	 * This method register for context events in the context database
 	 * @throws InvalidFormatException 
 	 */
 	@Override
 	public void registerForContextChanges(Object communityId) throws InvalidFormatException {
 		//Cast IIdentity for the societies platform
 		IIdentity cisID = idMgr.fromJid(communityId.toString());
 		LOG.info("cisID retrieved: "+ cisID);
 
 		try {
 			CtxEntityIdentifier ctxCommunityEntityIdentifier = this.ctxBroker.retrieveCommunityEntityId(getRequestor(), cisID).get();
 			LOG.info("communityEntityIdentifier retrieved: " +ctxCommunityEntityIdentifier.toString()+ " based on cisID: "+ cisID);
 			CommunityCtxEntity communityEntity = (CommunityCtxEntity) this.ctxBroker.retrieve(getRequestor(), ctxCommunityEntityIdentifier).get();
 
 			Set<CtxEntityIdentifier> ctxMembersIDs = communityEntity.getMembers();
 			Iterator<CtxEntityIdentifier> members = ctxMembersIDs.iterator();
 
 			while(members.hasNext()){
 				CtxEntityIdentifier member = members.next();
 				LOG.info("*** Registering context changes for member: "+member.toString());
 
 				//TODO: Include here other ctx updates if necessary. For short term context
 				this.ctxBroker.registerForChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.LOCATION_SYMBOLIC);
 				this.ctxBroker.registerForChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.STATUS);
 				//				this.ctxBroker.registerForChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.OCCUPATION);
 				//				this.ctxBroker.registerForChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.ADDRESS_WORK_CITY);
 				//				this.ctxBroker.registerForChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.ADDRESS_WORK_COUNTRY);
 
 			}
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			e1.printStackTrace();
 		} catch (CtxException e1) {
 			e1.printStackTrace();
 		}
 
 		LOG.info("*** registerForContextChanges success");
 	}
 
 	/**
 	 * This method unregister context events in the context database
 	 * @throws InvalidFormatException 
 	 */
 	@Override
 	public void unregisterContextChanges(Object communityId) throws InvalidFormatException {
 		//Cast IIdentity for the societies platform
 		IIdentity cisID = idMgr.fromJid(communityId.toString());
 		LOG.info("cisID retrieved: "+ cisID);
 
 		try {
 			CtxEntityIdentifier ctxCommunityEntityIdentifier = this.ctxBroker.retrieveCommunityEntityId(getRequestor(), cisID).get();
 			LOG.info("communityEntityIdentifier retrieved: " +ctxCommunityEntityIdentifier.toString()+ " based on cisID: "+ cisID);
 			CommunityCtxEntity communityEntity = (CommunityCtxEntity) this.ctxBroker.retrieve(getRequestor(), ctxCommunityEntityIdentifier).get();
 
 			Set<CtxEntityIdentifier> ctxMembersIDs = communityEntity.getMembers();
 			Iterator<CtxEntityIdentifier> members = ctxMembersIDs.iterator();
 
 			while(members.hasNext()){
 				CtxEntityIdentifier member = members.next();
 				LOG.info("*** Unregistering  context changes for member: "+member.toString());
 
 				//TODO: Include here other ctx updates if necessary. For short term context
 				this.ctxBroker.unregisterFromChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.LOCATION_SYMBOLIC);
 				this.ctxBroker.unregisterFromChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.STATUS);
 				//				this.ctxBroker.unregisterFromChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.OCCUPATION);
 				//				this.ctxBroker.unregisterFromChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.ADDRESS_WORK_CITY);
 				//				this.ctxBroker.unregisterFromChanges(getRequestor(), this.myCtxChangeEventListener, member, CtxAttributeTypes.ADDRESS_WORK_COUNTRY);
 			}
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			e1.printStackTrace();
 		} catch (CtxException e1) {
 			e1.printStackTrace();
 		}
 
 		LOG.info("*** unregisterContextChanges success");
 	}
 
 	@Override
 	public HashMap<String, HashMap<String, String[]>> retrieveLookupMembersCtxAttributes(Object communityId) throws InvalidFormatException{
 
 		//Cast IIdentity for the societies platform
 		IIdentity cisID = idMgr.fromJid(communityId.toString());
 		LOG.info("cisID retrieved: "+ cisID);
 		CtxEntityIdentifier ctxCommunityEntityIdentifier;
 		//Hashmap with format: person, context attributes
 		HashMap<String, HashMap<String, String[]>> persons = new HashMap<String, HashMap<String, String[]>>();
 
 		try {
 			ctxCommunityEntityIdentifier = this.ctxBroker.retrieveCommunityEntityId(getRequestor(), cisID).get();
 			if (ctxCommunityEntityIdentifier == null)
 				throw new IllegalArgumentException("Community not created, ctxCommunityEntityIdentifier is null"); 
 			LOG.info("communityEntityIdentifier retrieved: " +ctxCommunityEntityIdentifier.toString());
 
 			CommunityCtxEntity communityEntity = (CommunityCtxEntity) this.ctxBroker.retrieve(getRequestor(), ctxCommunityEntityIdentifier).get();
 
 			Set<CtxEntityIdentifier> ctxMembersIDs = communityEntity.getMembers();
 			LOG.info("Community Members size: " + communityEntity.getMembers().size());
 
 			for(CtxEntityIdentifier member : ctxMembersIDs){
 				//Hashmap representing the context attributes
 				HashMap<String, String[]> othersCtx = new HashMap<String, String[]>();
 
 				CtxEntity retrievedCtxEntity = (CtxEntity) this.ctxBroker.retrieve(getRequestor(), member).get();
 				LOG.info("Retrieved member entity: "+retrievedCtxEntity.getId());
 
 				//Job Position
 				Set<CtxAttribute> attribute = retrievedCtxEntity.getAttributes(CtxAttributeTypes.OCCUPATION);
 				for(CtxAttribute occupation : attribute)
 					othersCtx.put("occupation", new String[]{occupation.getStringValue()});
 				//Company
 				attribute = retrievedCtxEntity.getAttributes(CtxAttributeTypes.ADDRESS_WORK_CITY);
 				for(CtxAttribute company : attribute)
 					othersCtx.put("company", new String[]{company.getStringValue()});
 				//Location
 				attribute = retrievedCtxEntity.getAttributes(CtxAttributeTypes.LOCATION_SYMBOLIC);
 				for(CtxAttribute location : attribute)
 					othersCtx.put("location", new String[]{location.getStringValue()});
 				//Status
 				attribute = retrievedCtxEntity.getAttributes(CtxAttributeTypes.STATUS);
 				for(CtxAttribute status : attribute)
 					othersCtx.put("status", new String[]{status.getStringValue()});
 
 				//Interests
 				attribute = retrievedCtxEntity.getAttributes(CtxAttributeTypes.INTERESTS);
 				List<String> list = new ArrayList<String>();
 				for(CtxAttribute interest : attribute) {
 					list.add(interest.getStringValue());
 				}
 				String[] strArray = new String[list.size()];
 				othersCtx.put("interests", list.toArray(strArray));
 
 				//Name or ID
 				//TODO: Name FIXED!!
 
 				//				attribute = retrievedCtxEntity.getAttributes(CtxAttributeTypes.NAME);
 				if (attribute.isEmpty()){
 					throw new NullPointerException("Name of the person cannot be null! ");
 				} 
 				else {
 					for(CtxAttribute name : attribute) {
 						//Associate the the name Jid for the ctx retrieved. Original format is joedoe.societies.local
 						String getOnlyNameSubstring[] = name.getStringValue().split("\\.");
 						System.out.println("CtxAttributeTypes.ID**********************  "+getOnlyNameSubstring[0]);
 						persons.put(getOnlyNameSubstring[0], othersCtx);
 					}
 				}
 
 			}
 
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		} catch (CtxException e) {
 			e.printStackTrace();
 		}
 		return persons;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.societies.context.externalBrokerConnector.IContextAware3pService#setListener(org.societies.api.context.event.CtxChangeEventListener)
 	 */
 	@Override
 	public void setListener(CtxChangeEventListener myCtxChangeEventListener) {
 		this.myCtxChangeEventListener = myCtxChangeEventListener;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.societies.context.externalBrokerConnector.IContextAware3pService#getRequestor()
 	 */
 	@Override
 	public RequestorService getRequestor() {
 		//identities
 		if (this.requestorService == null) {
 			this.userIdentity = this.idMgr.getThisNetworkNode();
 			this.myServiceID = this.serviceMgmt.getMyServiceId(getClass()); 
 			this.requestorService = new RequestorService(userIdentity, myServiceID);
 			LOG.info("requestor service : "+requestorService);
 		}
 		return this.requestorService;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.societies.context.externalBrokerConnector.IContextAware3pService#retrieveCommunityCtxAttributes(java.lang.Object)
 	 */
 	@Override
 	public HashMap<String, String> retrieveCommunityCtxAttributes(Object communityId) throws InvalidFormatException {
 		//Cast IIdentity for the societies platform
 		IIdentity cisID = idMgr.fromJid(communityId.toString());
 		LOG.info("cisID retrieved for ctx attributes: "+ cisID);
 		HashMap<String, String> results = new HashMap<String, String>();
 
 		try {
 			CtxEntityIdentifier ctxCommunityEntityIdentifier = this.ctxBroker.retrieveCommunityEntityId(getRequestor(), cisID).get();
 			LOG.info("communityEntityIdentifier retrieved: " +ctxCommunityEntityIdentifier.toString()+ " based on cisID: "+ cisID);
 			CommunityCtxEntity communityEntity = (CommunityCtxEntity) this.ctxBroker.retrieve(getRequestor(), ctxCommunityEntityIdentifier).get();
 
 			Set<CtxAttribute> communityLanguages = communityEntity.getAttributes(CtxAttributeTypes.LANGUAGES);
 			Iterator<CtxAttribute> languagesIterator = communityLanguages.iterator();
 			while(languagesIterator.hasNext()){
 				String ctx = languagesIterator.next().getStringValue();
 				LOG.info("*** Community Languages:"+ctx);
 				results.put(CtxAttributeTypes.LANGUAGES, ctx);
 			}
 
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			e1.printStackTrace();
 		} catch (CtxException e1) {
 			e1.printStackTrace();
 		}
 
 		return results;
 	}
 
 }
