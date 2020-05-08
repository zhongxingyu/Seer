 package com.chaman.svc;
 
 import java.util.ArrayList;
 import org.restlet.resource.Get;
 import org.restlet.resource.ServerResource;
 import com.chaman.model.Attending;
 import com.chaman.model.Model;
 
 public class EventStats extends ServerResource {
 
 	@Get("json")
 	public Response Read() {
 				
 		Response result = new Response();
 		
 		try {
 			
 			String accessToken		= getQuery().getValues("access_token");
 			String eid 				= getQuery().getValues("eid");
 			
 			ArrayList<Model> events = new ArrayList<Model>();
 			
 			events.add(Attending.GetNb_attending_and_gender_ratio(accessToken, eid));
 			
 			result.setSuccess(true);
			result.setRecords(events);
 			
 		} catch (Exception ex) {
 			
 			result.setSuccess(false);
 			result.setError(ex.toString());
 		}
 		
 		return result;
 	}
 
 }
