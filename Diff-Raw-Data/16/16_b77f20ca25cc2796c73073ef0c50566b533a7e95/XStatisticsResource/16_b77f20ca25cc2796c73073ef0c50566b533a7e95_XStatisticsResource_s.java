 package savi.hcat.rest;
 
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.resource.Post;
 
 import savi.hcat.common.util.XConstants;
 import savi.hcat.rest.service.XStatApmtService;
 import savi.hcat.rest.service.XBaseStatService;
 import savi.hcat.rest.service.XStatRecordService;
 
 /**
  * This is to do the sum and average of all objects
  * @author dan
  *
  */
 public class XStatisticsResource extends XBaseResource{
 
 	private static Log LOG = LogFactory.getLog(XStatisticsResource.class);
 	
 	@Post	
 	public Representation statistics(Representation entity) throws IOException {
 		LOG.info("in statistics() ");						
 		
 		String jsonStr = null;
 		try {
 			jsonStr = entity.getText();
 		} catch (IOException e) {
 			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 			return new JsonRepresentation(getErrorMessage(e));			
 		}
 
 		try {
 			String kpi = (String)getRequestAttributes().get("kpi");			
 			
 			JSONObject payload = new JSONObject(jsonStr);
 			Object object = payload.get(XConstants.POST_KEY_OBJECT);			
 
 			LOG.info("the request: "+payload.toString());
 			// call service to get the data based on the parameter
 			JSONArray result = null;
 			if(object!= null){
 				if(object.equals(XConstants.POST_VALUE_APPOINTMENT)){ // appointment statistics
 					XStatApmtService stat_service = (XStatApmtService)XBaseStatService.getInstance(XConstants.POST_VALUE_APPOINTMENT);										
 					if(kpi.equals("sum")){
 						result = stat_service.getSummary(payload);	
 					}else if(kpi.equals("avg")){
 						result = stat_service.getAverage(payload);
 					}
 					LOG.info("after kpi "+kpi);
 				}else if(object.equals(XConstants.POST_VALUE_RECORD)){
 					XStatRecordService record_service = (XStatRecordService)XBaseStatService.getInstance(XConstants.POST_VALUE_RECORD);
 					if(kpi.equals("pct")){
 						result = record_service.getPercentage(payload);
 					}
				}											
 			}
 
 			getResponse().setStatus(Status.SUCCESS_OK);
 				
 			return new JsonRepresentation(result);
 
 		} catch (JSONException e) {
 			getResponse().setStatus(Status.SUCCESS_OK);
 			return new JsonRepresentation(getErrorMessage(e));
 		} /*catch (XStatisticsServiceException e) {
                  getResponse().setStatus(Status.SUCCESS_OK);
                  return new JsonRepresentation(getErrorMessage(e));
          } catch (ParseException e) {
                  getResponse().setStatus(Status.SUCCESS_OK);
                  return new JsonRepresentation(getErrorMessage(e));
          } */
 	}
 
 }
