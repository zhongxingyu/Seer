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
 package org.openmrs.module.shr.contenthandler.obs.handler;
 
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Obs;
 import org.openmrs.api.APIException;
 import org.openmrs.module.shr.contenthandler.api.Content;
 import org.openmrs.obs.ComplexData;
 import org.openmrs.obs.handler.TextHandler;
 
 import com.google.gson.Gson;
 
 /**
  * A basic complex obs handler for saving Content objects to the filesystem.
  * <p>
  * The purpose of this class is to provide a simple default obs handler for the Content Handler Module,
  * however the SHR Unstructured Data Handler Module is expected to be used instead.
  */
 public class ContentObsHandler extends TextHandler {
 	
 	Log log = LogFactory.getLog(this.getClass());
 	
 
 	@Override
 	public Obs getObs(Obs obs, String view) {
 		obs = super.getObs(obs, view);
 		
 		ComplexData data = obs.getComplexData();
 		if (data==null || !(data.getData() instanceof char[])) {
			throw new APIException("Unprocessable ComplexData found (obsId=" + obs.getObsId() + ")");
 		}
 		
 		String json = new String((char[])data.getData());
 		Content content = new Gson().fromJson(json, Content.class);
 		obs.setComplexData(new ComplexData(content.getContentType(), content));
 		
 		return obs;
 	}
 
 	@Override
 	public Obs saveObs(Obs obs) throws APIException {
 		ComplexData data = obs.getComplexData();
 		
 		if (data==null) {
			throw new APIException("ComplexData is null (obsId=" + obs.getObsId() + ")");
 		}
 		if (!(data.getData() instanceof Content)) {
			throw new APIException("ContentObsHandler can only be used with Content objects (obsId=" + obs.getObsId() + ")");
 		}
 		
 		Content content = (Content)data.getData();
 		String filename = UUID.randomUUID().toString() + ".json";
 		String json = new Gson().toJson(content, Content.class);
 		
 		obs.setComplexData(new ComplexData(filename, json.toCharArray()));
 		obs = super.saveObs(obs);
 		
 		return obs;
 	}
 }
