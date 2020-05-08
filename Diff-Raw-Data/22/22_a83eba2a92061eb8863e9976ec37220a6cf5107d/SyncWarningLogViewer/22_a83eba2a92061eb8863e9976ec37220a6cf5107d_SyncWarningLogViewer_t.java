 /**
  * 
  */
 package pt.ist.syncherWebRestserver.json;
 
 import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.core.rest.json.DomainObjectViewer;
 import pt.ist.bennu.json.JsonBuilder;
 import pt.ist.bennu.json.JsonViewer;
 import pt.ist.maidSyncher.domain.sync.logs.SyncWarningLog;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 /**
  * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 14 de Jun de 2013
  *
  * 
  */
 @DefaultJsonAdapter(SyncWarningLog.class)
 public class SyncWarningLogViewer implements JsonViewer<SyncWarningLog> {
 
     /* (non-Javadoc)
      * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
      */
     @Override
     public JsonElement view(SyncWarningLog obj, JsonBuilder ctx) {
         if (obj == null)
             return null;
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("id", obj.getExternalId());
        jsonObject.add("syncLog", ctx.view(obj.getSyncLog(), DomainObjectViewer.class));
         jsonObject.addProperty("description", obj.getDescription());
         return jsonObject;
     }
 
 }
