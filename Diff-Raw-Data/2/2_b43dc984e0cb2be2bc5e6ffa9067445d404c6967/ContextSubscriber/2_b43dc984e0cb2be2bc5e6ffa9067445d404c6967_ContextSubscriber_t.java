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
 
 package org.societies.collabtools.acquisition;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.collabtools.Activator;
 import org.societies.collabtools.api.IContextSubscriber;
 import org.societies.collabtools.api.IIncrementCtx.EnrichmentTypes;
 import org.societies.collabtools.interpretation.ContextAnalyzer;
 import org.societies.collabtools.runtime.CtxMonitor;
 import org.societies.collabtools.runtime.Session;
 import org.societies.collabtools.runtime.SessionRepository;
 
 public class ContextSubscriber implements IContextSubscriber, Observer 
 {
 	private static final Logger logger = LoggerFactory.getLogger(ContextSubscriber.class);
 	private PersonRepository personRepository;
 	private SessionRepository sessionRepository;
 	InternalContextConnector ctxConnector;
 	private CtxMonitor monitor;
 	private Activator activator;
 
 
 	public ContextSubscriber(Activator activator, PersonRepository personRepository, SessionRepository sessionRepository)
 	{
 		this.activator = activator;
 		this.personRepository = personRepository;
 		this.sessionRepository = sessionRepository;
 
 		//Starting Context Monitor
 		logger.info("Starting Context Monitor..." );
 		monitor = new CtxMonitor(personRepository, sessionRepository);
 	}
 
 	public void initialCtx(Object cisID) {
 		logger.info("Getting initial context..." );
 		ctxConnector = new InternalContextConnector(this);
 		//Parameter in this case is the group ID/cisID
 		HashMap<String, HashMap<String, String[]>> persons = ctxConnector.getInitialContext(cisID);
 		Iterator<String> personIterator = persons.keySet().iterator();
 
 		while (personIterator.hasNext()) {  
 			String personKey = personIterator.next().toString();  
 			HashMap<String, String[]> ctxAttributes = persons.get(personKey);
 			Iterator<String> ctxIterator = ctxAttributes.keySet().iterator();
 			logger.info("Person with context attributes: "+ personKey + " " + ctxAttributes);
 			while (ctxIterator.hasNext()) {
 				String ctxKey = ctxIterator.next().toString(); 
 				String[] ctxArray = ctxAttributes.get(ctxKey);
 				try {
 					//Set ctx with type of context, array of strings and person name
 					this.setContext(ctxKey, ctxArray, personKey);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}  
 		}
 
 		//Enrichment of ctx
 		logger.info("Starting enrichment of context..." );
 		ContextAnalyzer ctxRsn = new ContextAnalyzer(this.personRepository);
 		ctxRsn.incrementCtx(LongTermCtxTypes.INTERESTS, EnrichmentTypes.CONCEPT, null);
 		ctxRsn.incrementCtx(LongTermCtxTypes.INTERESTS, EnrichmentTypes.CATEGORY, null);
 
 		//Applying weight between edges
 		logger.info("Setup weight among participants..." );
 		ctxRsn.setupWeightAmongPeople(LongTermCtxTypes.INTERESTS);
 
 
 		//Registering for ctx changes
 		ctxConnector.registerForshortTermCtxUpdates(cisID);
 		
 		//Setting language
 		HashMap<String, String> ctxAttributes = ctxConnector.getCommunityCtx(cisID);
 		if (ctxAttributes != null) {
 			System.out.println("sessionRepository.setLanguage: "+ctxAttributes.get("languages"));
 			sessionRepository.setLanguage(ctxAttributes.get("languages"));
 		}
 		else {
 			sessionRepository.setLanguage("English");
 		}
 		//Starting Context Monitor
 		//        logger.info("Starting Context Monitor..." );
 		//        monitor = new CtxMonitor(personRepository, sessionRepository);
 		//	    monitor.wait();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.societies.collabtools.api.IContextSubscriber#stopCtx(java.lang.Object)
 	 */
 	@Override
 	public void stopCtx(Object cisID) {
 		//Unregistering  ctx changes
 		ctxConnector.unregisterForshortTermCtxUpdates(cisID);
 		//Restarting graph database
 		this.activator.setup();
 	}
 
 	public void update(Observable o, Object arg)
 	{
 		//Arrays values: [0]model type, [1]string ctx value, [2]person
 		String[] msg = (String[])arg;
 		logger.info("\n");
 		logger.info("******************* update event  Ctx type*************** " + msg[0]);
 		logger.info("******************* update event  Ctx value************** " + msg[1]);
 		logger.info("******************* update event  Person***************** " + msg[2]);
 		Map<String, String> shortTermCtx = new HashMap<String, String>();
 		String type = msg[0];
 		String context = msg[1];
 		Person individual = this.personRepository.getPersonByName(msg[2]);
 
 		if (type.equals("locationSymbolic")) {
 			shortTermCtx.put(ShortTermCtxTypes.LOCATION, context);
 		}
 		else {
 			shortTermCtx.put(type, context);
 		}
 		individual.addContextStatus(shortTermCtx, this.sessionRepository);
 		monitor.run();
 	}
 
 	public Hashtable<String,List<String>> getSessions() {
 		//Format: session name, members
 		Hashtable<String,List<String>> sessionHashtable = new Hashtable<String,List<String>>();
 		for (Session session : sessionRepository.getAllSessions() ) {
 			List<String> membersList = new ArrayList<String>();
 			Iterator<Person> it = session.getMembers();
 			while (it.hasNext()) {
 				membersList.add(((Person)it.next()).getName());
 			}
 			sessionHashtable.put(session.getSessionName(), membersList);
 			logger.info("Session: "+session.getSessionName());
 			logger.info("Session members: "+membersList.toString());
 		}
 		return sessionHashtable;		
 	}
 	
 	public String getSessionLanguage(String sessionName) {
 		Session session =sessionRepository.getSessionByName(sessionName);
 			logger.info("Session language: "+session.getLanguage(null));
 		return session.getLanguage(Session.LANGUAGE);
 	}
 	
 	/**
 	 * @return the monitor
 	 */
 	public CtxMonitor getMonitor() {
 		return monitor;
 	}
 	
 
 	private void setContext(String type, String[] context, String person) throws Exception {
		logger.info("******************************* Adding context info for: " + person + ", " + type + ", " + context[0].toString());
 
 		//Otherwise will be the first element, name : if (type == "name")
 		if (!this.personRepository.hasPerson(person)) {
 			Person individual = this.personRepository.createPerson(person);
 			individual.setLongTermCtx(LongTermCtxTypes.NAME, person);
 		}
 
 		Person individual = this.personRepository.getPersonByName(person);
 		//shortTermCtx format: context type, ctx value
 		Map<String, String> shortTermCtx = new HashMap<String, String>();
 		if (type.equals(LongTermCtxTypes.INTERESTS)) {
 			individual.setLongTermCtx(LongTermCtxTypes.INTERESTS, context);
 		} else if (type.equals(LongTermCtxTypes.OCCUPATION)) {
 			individual.setLongTermCtx(LongTermCtxTypes.OCCUPATION, context[0]);
 		} else if (type.equals(LongTermCtxTypes.WORK_POSITION)) {
 			individual.setLongTermCtx(LongTermCtxTypes.WORK_POSITION, context[0]);
 		} else if (type.equals(ShortTermCtxTypes.LOCATION)) {
 			//			shortTermCtx.put(individual.getLastStatus() == null ? "" : individual.getLastStatus().getShortTermCtx(ShortTermCtxTypes.STATUS), context[0]);
 			shortTermCtx.put(ShortTermCtxTypes.LOCATION,context[0]);
 			individual.addContextStatus(shortTermCtx, this.sessionRepository);
 		}
 		else if (type.equals(ShortTermCtxTypes.STATUS)) {
 			//			shortTermCtx.put(context[0], individual.getLastStatus() == null ? "" : individual.getLastStatus().getShortTermCtx(ShortTermCtxTypes.LOCATION));
 			shortTermCtx.put(ShortTermCtxTypes.STATUS,context[0]);
 			individual.addContextStatus(shortTermCtx, this.sessionRepository);
 		}
 		//TODO: For now chat is default
 		individual.setLongTermCtx(LongTermCtxTypes.COLLAB_APPS, new String[] { "chat" });
 	}
 
 }
 
 
 
