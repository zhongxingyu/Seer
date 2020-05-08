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
 package org.openmrs.module.shr.contenthandler.api;
 
 import java.util.Date;
 import java.util.List;
 
 import org.openmrs.Encounter;
 import org.openmrs.EncounterRole;
 import org.openmrs.EncounterType;
 import org.openmrs.Patient;
 import org.openmrs.Provider;
 
 /**
  * A content handler provides an implementation for storing and retrieving patient encounter data in a particular format.
  * <p>
  * Content handlers are registered with the {@link ContentHandlerService} and should not be instantiated directly.
  * To find a particular content handler, use the {@link ContentHandlerService#getContentHandler(String)} method.
  * <p>
  * A content handler can register itself for a particular content type using
  * the {@link ContentHandlerService#registerContentHandler(String, ContentHandler)} method
  * and deregister using the {@link ContentHandlerService#deregisterContentHandler(String)} method.
  * 
  * @see ContentHandlerService
  */
 public interface ContentHandler {
 
 	/**
 	 * Parse and store clinical content for the specified patient. Binary data should be sent base64 encoded.
 	 * 
 	 * @param patient The patient associated with the content
 	 * @param content The raw payload
 	 * @return The created and saved encounter object
 	 */
 	Encounter saveContent(Patient patient, Provider provider, EncounterRole role, EncounterType encounterType, Content content);
 
 	/**
 	 * Retrieve the content associated with the specified encounter uuid.
 	 * 
 	 * @param encounterUuid The encounter identifier
 	 * @return The content in the content handler's format
 	 */
 	Content fetchDocument(String encounterUuid);
 
 	/**
 	 * Retrieve the content associated with the specified encounter uuid.
 	 * 
 	 * @param encounterUuid The encounter identifier
 	 * @return The content in the content handler's format
 	 */
 	Content fetchDocument(int encounterId);
 
 	/**
 	 * Retrieve a list of formatted encounters for a specified patient.
 	 * 
 	 * @param patient The patient associated with the content
 	 * @param from The earliest encounter time to search for (inclusive)
 	 * @param to The latest encounter time to search for (exclusive)
 	 * @return A list of encounters in the content handler's format
 	 */
 	List<Content> queryEncounters(Patient patient, Date from, Date to);
 
 	/**
 	 * Create a clone of this handler.
 	 * <p>
 	 * Note that this method allows a content handler to return itself as an instance (i.e. {@code return this;}).
 	 * This is likely to happen if the handler doesn't have any state.
 	 * 
 	 * @return A clone of this handler
 	 */
 	ContentHandler cloneHandler();
 }
