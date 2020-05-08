 package de.uni_leipzig.simba.limeswebservice.server;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import de.konrad.commons.sparql.SPARQLHelper;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.limeswebservice.service.LimesService;
 
 public class LimesServiceImpl implements LimesService{
 
 	@Override
 	public void getMapping(HashMap<String, Object> source,
 			HashMap<String, Object> target, HashMap<String, Object> metric) {
 			KBInfo sourceInfo = createKBInfo(source);
 			KBInfo targetInfo = createKBInfo(target);
			
 		
 	}
 	
 	/**Creates KBInfo @TODO create test **/
 	private KBInfo createKBInfo(HashMap<String, Object> param) {
 		KBInfo info = new KBInfo();
 		info.endpoint = (String) param.get("endpoint");
 		info.graph = (String) param.get("graph");
 		info.var = (String) param.get("var");
 		info.restrictions = new ArrayList<String>();
 		if(param.containsKey("class")) {
 			String classRestrString = info.var+" rdf:type "+SPARQLHelper.wrapIfNecessary((String)param.get("class"));
 		}
		info.prefixes = (HashMap<String, String>) param.get("properties");
		for(String prop : info.prefixes.keySet()) {
 			info.properties.add(prop);
 		}
 		info.type = "SPARQL";
 		info.id = (String) param.get("id");
 		return info;
 	}
 
 }
