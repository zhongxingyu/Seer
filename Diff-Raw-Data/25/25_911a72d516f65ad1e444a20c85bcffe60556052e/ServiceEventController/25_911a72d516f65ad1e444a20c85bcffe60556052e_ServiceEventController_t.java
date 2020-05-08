 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.controller;
 
 
 import it.sayservice.platform.client.DomainEngineClient;
 import it.sayservice.platform.client.DomainObject;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import eu.trentorise.smartcampus.dt.model.EventObject;
 import eu.trentorise.smartcampus.dt.model.ServiceEventObject;
 import eu.trentorise.smartcampus.presentation.common.util.Util;
 import eu.trentorise.smartcampus.processor.EventProcessorImpl;
 
 @Controller
 public class ServiceEventController extends AbstractObjectController {
 
 	@Autowired
 	private DomainEngineClient domainEngineClient; 
 	
 	@RequestMapping(method = RequestMethod.PUT, value="/eu.trentorise.smartcampus.dt.model.ServiceEventObject/{id}")
 	public ResponseEntity<ServiceEventObject> updateEvent(HttpServletRequest request, @RequestBody Map<String,Object> objMap, @PathVariable String id) {
 		ServiceEventObject obj = Util.convert(objMap, ServiceEventObject.class);
 		
 		Map<String,Object> parameters = new HashMap<String, Object>(1);
 
 		// TODO THE FOLLOWING REMOVED TO BLOCK ANY MODIFICATION OF THE SERVICE DATA
 		/*
		Map<String,Object> customData = new HashMap<String, Object>();
 		if (obj.getType() != null) {
 			customData.put("type", obj.getType());
 		}
 		if (obj.getFromTime() != null) {
 			customData.put("fromTime", obj.getFromTime());
 		}
 		if (obj.getTiming() != null) {
 			customData.put("timing", obj.getTiming());
 		}
 		if (obj.getToTime() != null) {
 			customData.put("toTime", obj.getToTime());
 		}
 		if (obj.getPoiId() != null) {
 			customData.put("poiId",obj.getPoiId());
 		}
 		*/
 		// TODO the notes cannot be assigned
 		if (obj.getCommunityData() != null) {
 			obj.getCommunityData().setNotes(null);
 			obj.getCommunityData().setRatings(null);
 		}
 		
//		parameters.put("newCustomData", customData);
 		parameters.put("newCommunityData",  Util.convert(obj.getCommunityData(), Map.class));
 		try {
 			domainEngineClient.invokeDomainOperation(
//					"updateCustomData", 
					"updateCommunityData", 
 					"eu.trentorise.smartcampus.domain.discovertrento.ServiceEventObject", 
 					obj.getDomainId(),
 					parameters, null, null); 
 
 			String oString = domainEngineClient.searchDomainObject(obj.getDomainType(), obj.getDomainId(), null);
 			DomainObject dObj = new DomainObject(oString);
 			EventObject uObj = EventProcessorImpl.convertEventObject(dObj, storage);
 			storage.storeObject(uObj);
 			
 		} catch (Exception e) {
 			logger.error("Failed to update ServiceEvent: "+e.getMessage());
 			e.printStackTrace();
 			return new ResponseEntity<ServiceEventObject>(HttpStatus.METHOD_FAILURE);
 		}
 		
 		return new ResponseEntity<ServiceEventObject>(obj,HttpStatus.OK);
 	}
 }
