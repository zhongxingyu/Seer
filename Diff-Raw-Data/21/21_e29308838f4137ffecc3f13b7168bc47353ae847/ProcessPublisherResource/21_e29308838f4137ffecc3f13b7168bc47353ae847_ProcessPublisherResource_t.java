 package local.rest.resources.proc;
 
 import local.rest.resources.*;
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.util.*;
 import local.rest.interfaces.*;
 import is4.*;
 
 import net.sf.json.*;
 
 import java.net.*;
 import java.util.UUID;
 import java.util.Hashtable;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import javax.naming.InvalidNameException;
 import java.io.*; 
 import com.sun.net.httpserver.*;
 
 public class ProcessPublisherResource extends GenericPublisherResource {
 	
 	protected static transient final Logger logger = Logger.getLogger(ProcessPublisherResource.class.getPackage().getName());
 
     protected boolean materialize = false;
     protected UUID associatedSubId = null;
 
     public ProcessPublisherResource (String path, UUID pubid, boolean mat) 
         throws Exception, InvalidNameException
     {
         super(path, pubid);
         materialize = mat;
         //set type to generic_publisher
         TYPE= ResourceUtils.PROCESS_PUBLISHER_RSRC;
 		database.setRRType(URI, ResourceUtils.translateType(TYPE).toLowerCase());
     }
 
     public void setAssociatedSubId(UUID subid) {
         associatedSubId = subid;
     }
 
     public synchronized void post(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
         boolean isQuery = (exchangeJSON.containsKey("query") && 
                             exchangeJSON.optString("query").equalsIgnoreCase("true"))?true:false;
         if(materialize || isQuery){
             super.post(exchange, data, internalCall, internalResp);
         } else {
            try {
                JSONObject dataObj = (JSONObject)JSONSerializer.toJSON(data);
                dataObj.put("PubId", publisherId.toString());
                SubMngr submngr  = SubMngr.getSubMngrInstance();
                submngr.dataReceived(dataObj);
                sendResponse(exchange, 200, null, internalCall, internalResp);
            } catch(Exception e){
                JSONObject resp = new JSONObject();
                if(e instanceof JSONException)
                    resp.put("error", "Invalid JSON");
                logger.log(Level.WARNING, "", e);
                sendResponse(exchange, 500, resp.toString(), 
                                    internalCall, internalResp);
            }
         }
     }
 
     public void delete(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
         //delete the subscription associated with this publisher
         //i.e the subscription where this publisher is the destination
         if(database.isSubscription(associatedSubId) != null){
             String subPath = database.getSubUriBySubId(associatedSubId);
             if(subPath !=null){
                 Resource r = RESTServer.getResource(subPath);
                 if(r !=null && r instanceof SubscriptionResource){
                     JSONObject inresp = new JSONObject();
                     SubscriptionResource sr  = (SubscriptionResource)r;
                     if(sr!=null)
                         sr.delete(exchange, true, inresp);
                 }
             }
         }
         //kill the process on the process server associated with this publisher
         if(associatedSubId!=null)
             ProcessManagerResource.killProcessing(associatedSubId.toString());
   
         //this not noly removes this resource, but also any subscriptions to this resource
         // (i.e. any subscriptions where this publisher is the source)
         super.delete(exchange, internalCall, internalResp);
     }
 } 
