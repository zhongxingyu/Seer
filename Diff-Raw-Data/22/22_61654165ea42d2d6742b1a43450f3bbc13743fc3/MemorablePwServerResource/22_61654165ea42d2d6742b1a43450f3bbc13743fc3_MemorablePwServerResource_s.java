 package com.pangratz.memorablepw.rest;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.ServerResource;
 
 import com.pangratz.memorablepw.model.ModelUtils;
 
 public abstract class MemorablePwServerResource extends ServerResource {
 
 	protected ModelUtils mModelUtils;
 
 	protected Representation createErrorRepresentation(String string) {
 		Map<Object, Object> data = new HashMap<Object, Object>();
 		data.put("error", true);
 		data.put("errorMsg", string);
 		return new JsonRepresentation(data);
 	}
 
 	protected Representation createSuccessRepresentation(String string) {
 		Map<Object, Object> data = new HashMap<Object, Object>();
 		data.put("msg", string);
		return new JsonRepresentation(data);
 	}
 
 	@Override
 	protected void doInit() throws ResourceException {
 		super.doInit();
 
 		this.mModelUtils = ModelUtils.getInstance();
 	}
 
 }
