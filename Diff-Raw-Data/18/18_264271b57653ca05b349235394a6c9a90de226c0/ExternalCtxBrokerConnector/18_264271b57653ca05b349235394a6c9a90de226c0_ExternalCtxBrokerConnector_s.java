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
 
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.cis.management.ICisManager;
 import org.societies.api.context.CtxException;
 import org.societies.api.context.broker.ICtxBroker;
 import org.societies.api.context.event.CtxChangeEvent;
 import org.societies.api.context.event.CtxChangeEventListener;
 import org.societies.api.context.model.CtxAttribute;
 import org.societies.api.context.model.CtxAttributeTypes;
 import org.societies.api.context.model.CtxEntity;
 import org.societies.api.identity.InvalidFormatException;
import org.societies.api.identity.RequestorService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * This class provides examples for using the internal Context Broker in OSGi. 
  */
 @Service
 public class ExternalCtxBrokerConnector extends Observable {
 
 	/** The logging facility. */
 	private static final Logger LOG = LoggerFactory.getLogger(ExternalCtxBrokerConnector.class);
 
 
 	/** The 3P Context Broker service reference. */
 	private ICtxBroker ctxBroker;
 	private IContextAware3pService ca3pService;
	private RequestorService requestorService;
 
 	@Autowired(required=true)
 	public ExternalCtxBrokerConnector(ICtxBroker externalCtxBroker, ICisManager cisManager, IContextAware3pService ca3pService) throws Exception {
 
 		LOG.info("*** ca3pService : " + ca3pService + " instantiated");
 
 		this.ctxBroker = externalCtxBroker;
 		this.ca3pService = ca3pService;
		this.requestorService = ca3pService.getRequestor();
 		this.ca3pService.setListener(new MyCtxChangeEventListener());
 
 	}
 
 	/**
 	 * @return retrieveLookupMembersCtxAttributes
 	 * @throws InvalidFormatException 
 	 */
 	public HashMap<String,HashMap<String,String[]>> retrieveLookupMembersCtxAttributes(Object cisID) {
 		try {
 			return this.ca3pService.retrieveLookupMembersCtxAttributes(cisID);
 		} catch (InvalidFormatException e) {
 			e.printStackTrace();
 		}
 		//TODO: FIX THIS!
 		return null;
 	}
 	
 	/**
 	 * @return retrieveLookupMembersCtxAttributes
 	 * @throws InvalidFormatException 
 	 */
 	public HashMap<String, String> retrieveCommunityCtxAttributes(Object cisID) {
 		try {
 			return this.ca3pService.retrieveCommunityCtxAttributes(cisID);
 		} catch (InvalidFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * @param String cisID
 	 */
 	public void registerForContextChanges(Object cisID) {
 		//registerForContextChanges()
 		try {
 			this.ca3pService.registerForContextChanges(cisID);
 		} catch (InvalidFormatException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * @param String cisID
 	 */
 	public void unregisterContextChanges(Object cisID) {
 		//registerForContextChanges()
 		try {
 			this.ca3pService.unregisterContextChanges(cisID);
 		} catch (InvalidFormatException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	private class MyCtxChangeEventListener implements CtxChangeEventListener {
 
 		/* (non-Javadoc)
 		 * @see org.societies.api.context.event.CtxChangeEventListener#onCreation(org.societies.api.context.event.CtxChangeEvent)
 		 */
 		@Override
 		public void onCreation(CtxChangeEvent event) {
 
 			LOG.info(event.getId() + ": *** CREATED event ***");
 		}
 
 		/* (non-Javadoc)
 		 * @see org.societies.api.context.event.CtxChangeEventListener#onModification(org.societies.api.context.event.CtxChangeEvent)
 		 */
 		@Override
 		public void onModification(CtxChangeEvent event) {
 
 			LOG.info(event.getId() + ": *** MODIFIED event ***");
 		}
 
 		/* (non-Javadoc)
 		 * @see org.societies.api.context.event.CtxChangeEventListener#onRemoval(org.societies.api.context.event.CtxChangeEvent)
 		 */
 		@Override
 		public void onRemoval(CtxChangeEvent event) {
 
 			LOG.info(event.getId() + ": *** REMOVED event ***");
 		}
 
 		/* (non-Javadoc)
 		 * @see org.societies.api.context.event.CtxChangeEventListener#onUpdate(org.societies.api.context.event.CtxChangeEvent)
 		 */
 		@Override
 		public void onUpdate(CtxChangeEvent event) {
 			LOG.info(event.getId() + ": *** UPDATED event ***");
 			CtxAttribute ctxAttr = null;
 			Set<CtxAttribute> ctxEntity = null;
 			String person = null;
 			try {
 
				ctxAttr = (CtxAttribute) ctxBroker.retrieve(requestorService, event.getId()).get();
 				LOG.info("ctxValue getScope: "+ctxAttr.getScope());
				CtxEntity retrievedCtxEntity = (CtxEntity) ctxBroker.retrieve(requestorService, ctxAttr.getScope()).get();
 				ctxEntity= retrievedCtxEntity.getAttributes(CtxAttributeTypes.NAME);
 
 				for(CtxAttribute name : ctxEntity) {
 					person = name.getStringValue();
 				}
 
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			} catch (CtxException e) {
 				e.printStackTrace();
 			}
 			String [] response = new String [] {ctxAttr.getType(), ctxAttr.getStringValue(), person};
 			LOG.info("Ctx value type: "+response[0]);
 			LOG.info("Ctx new value: "+response[1]);
 			LOG.info("Ctx person: "+response[2]);
 
 
 			// Notify observers of change
 			setChanged();
 			notifyObservers(response);
 		}
 	}
 
 }
