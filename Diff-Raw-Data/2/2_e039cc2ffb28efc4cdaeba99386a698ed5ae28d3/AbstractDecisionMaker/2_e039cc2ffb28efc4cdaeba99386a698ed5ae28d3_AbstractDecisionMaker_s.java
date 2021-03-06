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
 
 package org.societies.api.internal.useragent.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.societies.api.internal.useragent.conflict.IConflictResolutionManager;
 import org.societies.api.internal.useragent.decisionmaking.IDecisionMaker;
 import org.societies.api.internal.useragent.feedback.IUserFeedback;
 import org.societies.api.personalisation.model.IAction;
 import org.societies.api.internal.personalisation.model.IOutcome;
 
 public abstract class AbstractDecisionMaker implements IDecisionMaker {
 	IConflictResolutionManager manager;
 	IUserFeedback feedbackHandler;
 	
 	public IConflictResolutionManager getManager() {
 		return manager;
 	}
 
 	public void setManager(IConflictResolutionManager manager) {
 		this.manager = manager;
 	}
 
 	public IUserFeedback getFeedbackHandler() {
 		return feedbackHandler;
 	}
 
 	public void setFeedbackHandler(IUserFeedback feedbackHandler) {
 		this.feedbackHandler = feedbackHandler;
 	}
 
 
 
 	@Override
 	public void makeDecision(List<IOutcome> intents, List<IOutcome> preferences) {
 		// TODO Auto-generated method stub
 		for (IOutcome intent : intents) {
 			IOutcome action=intent;
 			for (IOutcome preference : preferences) {
 				ConflictType conflict = detectConflict(intent, preference);
 				if (conflict == ConflictType.PREFERNCE_INTENT_NOT_MATCH) {
 					action = manager.resolveConflict(action,preference);
 					if(action ==null){
 						List<String> options=new ArrayList<String>();
						options.add(action.toString());
 						options.add(preference.toString());
 						ExpProposalContent epc=new ExpProposalContent("Conflict Detected!",
 								options);
 						if(feedbackHandler.getExplicitFB(
 								ExpProposalType.RADIOLIST,epc)){
 							action=intent;
 							/*return true for intent false for preference*/
 						}else{
 							action=preference;
 						}
 					}
 				}else if (conflict==ConflictType.UNKNOWN_CONFLICT){
 					/*handler the unknown work*/
 				}
 			}
 			this.implementIAction(action);
 		}
 	}
 
 	protected abstract ConflictType detectConflict(IOutcome intent,
 			IOutcome prefernce);
 
 	protected abstract void implementIAction(IAction action);
 
 }
