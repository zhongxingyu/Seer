 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.registrationcore.api;
 
 import java.util.List;
 
 import javax.jms.MapMessage;
 import javax.jms.Message;
 
 import junit.framework.Assert;
 
 import org.openmrs.event.MockEventListener;
 import org.openmrs.module.registrationcore.RegistrationCoreConstants;
 
 /**
  * Subclass of {@link MockEventListener} that extracts the patient registration pay load for testing
  * purposes
  */
 public class MockRegistrationEventListener extends MockEventListener {
 	
 	private volatile String patientUuid;
 	
 	private volatile List<String> relationshipUuids;
 	
 	private volatile String registererUuid;
 	
 	private volatile String dateRegistered;
 	
 	private volatile Boolean wasAPerson = Boolean.FALSE;
 	
 	MockRegistrationEventListener(int expectedEventsCount) {
 		super(expectedEventsCount);
 	}
 	
 	/**
 	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onMessage(Message message) {
		super.onMessage(message);
 		MapMessage mapMessage = (MapMessage) message;
 		try {
 			patientUuid = mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID);
			relationshipUuids = (List<String>) mapMessage.getObject(RegistrationCoreConstants.KEY_RELATIONSHIP_UUIDS);
			registererUuid = mapMessage.getString(RegistrationCoreConstants.KEY_REGISTERER_UUID);
			dateRegistered = mapMessage.getString(RegistrationCoreConstants.KEY_DATE_REGISTERED);
			wasAPerson = mapMessage.getBoolean(RegistrationCoreConstants.KEY_WAS_A_PERSON);
 		}
 		catch (Exception e) {
 			Assert.fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * @return the patientUuid
 	 */
 	public String getPatientUuid() {
 		return patientUuid;
 	}
 	
 	/**
 	 * @return the relationshipUuids
 	 */
 	public List<String> getRelationshipUuids() {
 		return relationshipUuids;
 	}
 	
 	/**
 	 * @return the registererUuid
 	 */
 	public String getRegistererUuid() {
 		return registererUuid;
 	}
 	
 	/**
 	 * @return the dateRegistered
 	 */
 	public String getDateRegistered() {
 		return dateRegistered;
 	}
 	
 	/**
 	 * @return the wasAPerson
 	 */
 	public Boolean getWasAPerson() {
 		return wasAPerson;
 	}
 	
 }
