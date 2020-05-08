 package au.edu.anu.portal.portlets.basiclti.adapters;
 
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * This adapter processes the endpoint_url to append the tool_id to the end.
  * It also does some stuff with the userID (to be documented)
  * 
  * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
  *
  */
 public class SakaiAdapter extends AbstractAdapter {
 
 	private final Log log = LogFactory.getLog(getClass().getName());
 	
 	/**
 	 * Added to the map only when context_id=~. This parameter contains the eid of the user.
 	 */
 	private final String EXT_SAKAI_PROVIDER_EID = "ext_sakai_provider_eid";
 	
 	
 	/**
 	 * Modifies the map of params to append the tool_id to the endpoint URL.
 	 * 
	 * Also adds an additional property to signal we are sending the eid instead of the uuid
 	 * 
 	 * @param params	map of launch data params
 	 * @return the map, modified
 	 */
 	@Override
 	public Map<String, String> processLaunchData(Map<String, String> params) {
 		
 		log.debug("SakaiAdapter.processLaunchData() called");
 		
 		//add tool_id to endpoint_url
 		String tool_id = params.get("tool_id");
 		String endpoint_url = params.get("endpoint_url");
 		
 		params.put("endpoint_url", endpoint_url + tool_id);
 		
		//add ext_sakai_provider_eid 
 		String user_id = params.get("user_id");
		params.put(EXT_SAKAI_PROVIDER_EID, user_id);
 		
 		//add defaults
 		params.putAll(super.getDefaultParameters());
 		
 		return params;
 		
 	}
 
 }
