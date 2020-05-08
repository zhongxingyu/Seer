 package at.ac.dbisinformatik.snowprofile.web;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.Put;
 import org.restlet.resource.ServerResource;
 
 import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
 
 public class ListSnowProfileResource extends ServerResource {
 
 	public ListSnowProfileResource() {
 		setNegotiated(true);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Get()
 	public String getJson() throws JSONException, IOException {
 		JSONArray returnList = new JSONArray();
 		ODatabaseDocumentTx db = new ODatabaseDocumentTx("local:C:/Users/Administrator/Uni/Bachelor/OrientDB/orientdb110/databases/test/snowprofile").open("admin", "admin");
 		
 		List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>("select * from SnowProfile"));
 		
 		for (ODocument oDocument : result) {
 //			Map<?,?> sub = oDocument.field("SnowProfile");
 			JSONObject temp = new JSONObject(oDocument.toJSON());
 			
 			Map<String, Object> idMap = (Map<String, Object>) JSONHelpers.jsonToMap("SnowProfile", temp).get("SnowProfile");
 			idMap.put("rid", (String) temp.get("@rid").toString().substring(1));
 
 			returnList.put(idMap);
 		}
//		db.close();
 		return "{SnowprofileList: "+returnList.toString()+"}";
 	}
 
 	@Put
 	public String updateJson(String value) {
 		System.out.println("put json");
 		return value;
 	}
 	
 	@Post
	public String storeJson(Representation value) throws IOException, JSONException {
		JSONObject newSnowprofile = new JSONObject(value.getText());
		ODatabaseDocumentTx db = new ODatabaseDocumentTx("local:C:/Users/Administrator/Uni/Bachelor/OrientDB/orientdb110/databases/test/snowprofile").open("admin", "admin");
		ODocument doc = new ODocument("SnowProfile");
		doc.fromJSON(newSnowprofile.toString());
		doc.save();
		ORID rid = doc.getIdentity();
		db.close();
		return rid.toString().substring(1);
 	}
 }
