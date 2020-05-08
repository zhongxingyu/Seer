 /**
  * 
  */
 package pt.ist.syncherWebRestserver.json;
 
 import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.core.rest.json.DomainObjectViewer;
 import pt.ist.bennu.json.JsonBuilder;
 import pt.ist.bennu.json.JsonViewer;
 import pt.ist.maidSyncher.domain.sync.logs.SyncEventConflictLog;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 /**
  * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 14 de Jun de 2013
  *
  * 
  */
 @DefaultJsonAdapter(SyncEventConflictLog.class)
 public class SyncEventConflictLogViewer implements JsonViewer<SyncEventConflictLog> {
 
 
     /* (non-Javadoc)
      * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
      */
     @Override
     public JsonElement view(SyncEventConflictLog obj, JsonBuilder ctx) {
         if (obj == null)
             return null;
         JsonObject jsonObject = new  JsonObject();
         jsonObject.addProperty("id", obj.getExternalId());
        jsonObject.add("syncLog", ctx.view(obj.getSyncLog(), DomainObjectViewer.class));
         jsonObject.addProperty("eventOneTypeOfChangeEvent", obj.getEventOneTypeOfChangeEvent().toString());
         jsonObject.addProperty("eventTwoTypeOfChangeEvent", obj.getEventTwoTypeOfChangeEvent().toString());
 
         //let's take care of the event
         jsonObject.add("eventOneOriginator", ctx.view(obj.getEventOneOriginator()));
         jsonObject.add("eventTwoOriginator", ctx.view(obj.getEventTwoOriginator()));
 
         jsonObject.add("winnerObject", ctx.view(obj.getWinnerObject()));
 
         return jsonObject;
 
     }
 
 }
