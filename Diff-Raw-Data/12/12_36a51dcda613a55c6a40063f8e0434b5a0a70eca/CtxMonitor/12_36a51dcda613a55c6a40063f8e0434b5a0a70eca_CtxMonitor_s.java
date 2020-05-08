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
 package org.societies.enterprise.collabtools.runtime;
 
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.societies.enterprise.collabtools.acquisition.LongTermCtxTypes;
 import org.societies.enterprise.collabtools.acquisition.Person;
 import org.societies.enterprise.collabtools.acquisition.PersonRepository;
import org.societies.enterprise.collabtools.acquisition.ShortTermCtxTypes;
 import org.societies.enterprise.collabtools.interpretation.Rules;
 
 
 /**
  * Describe your class here...
  *
  * @author cviana
  *
  */
 public class CtxMonitor extends Thread{
 
 	private Rules conditions;
 	private SessionRepository sessionRepository;
 	private static final Logger logger  = LoggerFactory.getLogger(CtxMonitor.class);
 	private static final long SECONDS = 5 * 1000;
 
 	public CtxMonitor (PersonRepository personRepository, SessionRepository sessionRepository) {
 		conditions = new Rules(personRepository, sessionRepository);
 		this.sessionRepository = sessionRepository;
 	}
 
 	public synchronized void run(){
 		try {
 			while (true) {
 
 				logger.info("Checking if people context match");
 
 
 				//First rule: location
 				Hashtable<String, HashSet<Person>> personsSameLocation = conditions.getPersonsSameLocation();
 
 				if (!personsSameLocation.isEmpty()) {
 					Enumeration<String> iterator = personsSameLocation.keys();
 					//For each different location, apply the follow rules...
 					while(iterator.hasMoreElements()) {
 						//Session name = actual location
 						String sessionName = iterator.nextElement();
 						logger.info("First rule: Location "+personsSameLocation.toString());
 						
 //						//Second rule: Company
 //						//Check company
 //						Hashtable<String, HashSet<Person>> personsWithSameCompany = conditions.getPersonsWithMatchingLongTermCtx(LongTermCtxTypes.COMPANY, personsSameLocation.get(sessionName));
 //						logger.info("Second rule: Company "+personsWithSameCompany.toString());
 						
 //						//Third rule: Interest
 //						//Check similar interest
 						Hashtable<String, HashSet<Person>> personsWithSameInterests = conditions.getPersonsByWeight(sessionName, personsSameLocation.get(sessionName));
 						logger.info("Third rule: Interests "+personsWithSameInterests.toString());
 //						
 //						//Fourth rule: Status
 //						//Check status of the user e.g busy, on phone, driving...
 //						Hashtable<String, HashSet<Person>> personsWithSameStatus = conditions.getPersonsWithMatchingShortTermCtx(ShortTermCtxTypes.STATUS, personsWithSameInterests.get(sessionName));
 //						logger.info("Fourth rule: Status "+personsWithSameStatus.toString());
 						
 
 						if (!sessionRepository.containSession(sessionName) && !personsWithSameInterests.isEmpty()) {
 							logger.info("Starting a new session..");
 							logger.info("Inviting people..");
 							sessionRepository.inviteMembers(sessionName, personsWithSameInterests.get(sessionName));
 						} //Check conflict if actual users in the session
 						else if (sessionRepository.differenceBetweenSessionMembers(personsWithSameInterests.get(sessionName), sessionName)){
 							if (!personsWithSameInterests.get(sessionName).isEmpty()) {
 								//Compare persons in same context with members in this session
 								sessionRepository.inviteMembers(sessionName, personsWithSameInterests.get(sessionName));
 								logger.info("Checking if users are in a session..");
 							}
 
 						}
 					}
 				}
 				//Sleep in seconds
 				Thread.sleep(SECONDS);
 
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 }
