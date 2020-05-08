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
 package org.openmrs.module.shr.contenthandler.api.impl;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.module.shr.contenthandler.UnstructuredDataHandler;
 import org.openmrs.module.shr.contenthandler.api.AlreadyRegisteredException;
 import org.openmrs.module.shr.contenthandler.api.ContentHandler;
 import org.openmrs.module.shr.contenthandler.api.ContentHandlerService;
 import org.openmrs.module.shr.contenthandler.api.InvalidContentTypeException;
 
 /**
  * It is a default implementation of {@link ContentHandlerService}.
  */
 public class ContentHandlerServiceImpl extends BaseOpenmrsService implements ContentHandlerService {
 	
 	protected final Log log = LogFactory.getLog(this.getClass());
 	
 	protected final Map<String, ContentHandler> handlers = new HashMap<String, ContentHandler>();
 
 	@Override
 	public ContentHandler getContentHandler(String contentType) {
 		if (contentType==null || contentType.isEmpty() || !handlers.containsKey(contentType)) {
 			return new UnstructuredDataHandler(contentType);
 		}
 		
 		return handlers.get(contentType).cloneHandler();
 	}
 
 	@Override
 	public void registerContentHandler(String contentType,
 			ContentHandler prototype) throws AlreadyRegisteredException, InvalidContentTypeException {
 		
		if (prototype==null) {
			throw new NullPointerException();
		}
		
 		if (!isValidContentType(contentType)) {
 			throw new InvalidContentTypeException();
 		}
 		
 		if (handlers.containsKey(contentType)) {
 			throw new AlreadyRegisteredException();
 		}
 		
 		handlers.put(contentType, prototype);
 	}
 	
 	private static boolean isValidContentType(String contentType) {
 		//Relaxed validation; just look for something/something
 		return contentType!=null && !contentType.isEmpty() &&
 			contentType.matches("[\\w\\+\\-\\.]+\\/[\\w\\+\\-\\.]+");
 	}
 
 	@Override
 	public void deregisterContentHandler(String contentType) {
 		handlers.remove(contentType);
 	}
 }
