 package il.technion.ewolf.server;
 
 import il.technion.ewolf.server.handlers.JsonDataHandler;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 import org.apache.http.util.EntityUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class JsonHandlerNew implements HttpRequestHandler {
 	private Map<String,JsonDataHandler> handlers = new HashMap<String,JsonDataHandler>();
 
 	public JsonHandlerNew addHandler(String key, JsonDataHandler handler) {
 		handlers.put(key, handler);
 		return this;
 	}
 
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		//TODO move adding server header to response intercepter
 		res.addHeader(HTTP.SERVER_HEADER, "e-WolfNode");
 
 		String jsonReqAsString = EntityUtils.toString(((HttpEntityEnclosingRequest)req).getEntity());
 		JsonParser parser = new JsonParser();
         JsonObject jsonReq = parser.parse(jsonReqAsString).getAsJsonObject();
        
         JsonObject jsonRes = new JsonObject();
        for (Entry<String, JsonElement> obj : jsonReq.entrySet()) {
        	String key = obj.getKey();
         	JsonDataHandler handler = handlers.get(key);
        	if (handler != null) {
        		JsonElement jsonElem = handler.handleData(obj.getValue());
            	jsonRes.add(key, jsonElem);
        	} else {
         		System.out.println("No handlers are registered to handle request with keyword " + key);
         		//TODO send response with Bad Request status
        	}        	
		}
         
         Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
         String json = gson.toJson(jsonRes);
         res.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
 	}
 
 }
