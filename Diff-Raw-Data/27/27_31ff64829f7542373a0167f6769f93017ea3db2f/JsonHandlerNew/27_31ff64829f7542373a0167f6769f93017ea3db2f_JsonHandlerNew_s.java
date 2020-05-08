 package il.technion.ewolf.server;
 
 import il.technion.ewolf.server.handlers.JsonDataHandler;
 
 import java.io.IOException;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
import java.util.Set;
 
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
        Set<Entry<String, JsonElement>> jsonReqAsSet = jsonReq.entrySet();
        Iterator<Entry<String, JsonElement>> it = jsonReqAsSet.iterator();

         JsonObject jsonRes = new JsonObject();
        while (it.hasNext()) {
        	Entry<String, JsonElement> jsonObject = it.next();
        	String key = jsonObject.getKey();
         	JsonDataHandler handler = handlers.get(key);
        	if (handler == null) {
         		System.out.println("No handlers are registered to handle request with keyword " + key);
         		//TODO send response with Bad Request status
        	}
        	JsonElement jsonElem = handler.handleData(jsonObject.getValue());
        	jsonRes.add(key, jsonElem);
        }
         
         Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
         String json = gson.toJson(jsonRes);
         res.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
 	}
 
 }
