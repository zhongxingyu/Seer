 package net.imagini.kafka;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import kafka.message.Message;
 import kafka.message.MessageAndMetadata;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import co.gridport.kafka.MirrorDestination;
 import co.gridport.kafka.MirrorResolver;
 
 
 /**
  * This class is responsible for sending the incoming
  * events into the right topic and with a partition key
  * appropriate for partitioning stratgy assigned to
  * the meaning of each topic.
  * 
  * It uses low-level jackson json streaming API to
  * extract the deciding attributes of each event as
  * fast as possible.
  * 
  * @author: michal.haris@visualdna.com
  */
 
 public class DestinationResolver  implements MirrorResolver
 {
 
     static private Logger log = LoggerFactory.getLogger(DestinationResolver.class);
     static private JsonFactory jsonFactory = new JsonFactory();
 
     /**
      * Entry point method which decides how to resolve the message depending
      * on the source topic.
      */
     public List<MirrorDestination> resolve(MessageAndMetadata<Message> metaMsg)
     {
         ArrayList<MirrorDestination> result = new ArrayList<MirrorDestination>();
         try {
             //prepare fields we're interetsed in for parititoning purpose
             Map<String,String> fields = new HashMap<String,String>();
             fields.put("timestamp", null);
             fields.put("date", null);
             fields.put("event_type", null);
             fields.put("userUid", null);
 
             fields.put("action", null);
             fields.put("objType", null);
             fields.put("objId", null);
             fields.put("vdna_widget_mc", null);
 
             try {
                parseMinimumJsonMessage(metaMsg.message(), fields);
             } catch (IOException e) {
                log.error("Couldn'r read JSON ", e);
                 return result;
             }
 
             //figure out User UUID and its hash for partitioning
             String uuid = fields.get("userUid");
             String widget_mc = fields.get("vdna_widget_mc");
             if (uuid == null || uuid.equals("null") || uuid.equals("OPT_OUT"))
             {
                 uuid = null;
                 //TODO JIRA/EDA-19 what is userUid=OPT_OUT
                 if (!widget_mc.equals("null") && !widget_mc.equals("OPT_OUT"))
                 {
                     //TODO JIRA/EDA-19 validate the if this comes from the deterministic generator or a true uuid
                     uuid = widget_mc;
                 }
             }
             Integer uuidHash = null; 
             if (uuid != null && !uuid.equals("null"))
             {
                 try {
                     uuidHash = (uuid == null ? null : Math.abs(UUID.fromString(uuid).hashCode()));
                 } catch (IllegalArgumentException invalidUuid)
                 {
                     uuidHash = null;
                 }
             }
 
             //now check event type and resolve accordingly
             String eventType = fields.get("event_type");
             if (eventType.equals("esVDNAAppUserActionEvent")) // event tracker message
             {
                 String action = fields.get("action");
                 if (action == null)
                 {
                    log.warn("Invalid esVDNAAppUserActionEvent with null action: " + metaMsg.message().payload().toString());
                     return result;
                 }
                 /*
                  * Publish into one of the topics for each event type - e.g. primary topic.
                  * Every event has to end up at least in one of the following.
                  */
                 if (action.startsWith("INSERTED_INTO_"))
                 {
                     result.add(new MirrorDestination(
                         "datasync", 
                         uuidHash
                     ));
                 }
                 else if (action.equals("CONVERSION") && fields.get("objId").equals("sync"))
                 {
                     result.add(new MirrorDestination(
                         "datasync", 
                         uuidHash
                     ));
                 }
                 else if (action.equals("MINTED_USER_KEY"))
                 {
                     result.add(new MirrorDestination(
                         "datasync", 
                         uuidHash
                     ));
                 }
                 else if (action.equals("CONVERSION") && fields.get("objType").equals("CONVERSION"))
                 {
                     String conversionId = fields.get("objId");
                     if (conversionId.equals("loaded_quiz")
                         || conversionId.equals("started_quiz")
                         || conversionId.equals("loaded_results")
                     ) {
                         result.add(new MirrorDestination(
                             "useractivity",
                             uuidHash
                         ));
                     } else {
                         result.add(new MirrorDestination(
                             "conversions", 
                             uuidHash
                         ));
                     }
                 }
                 else if (action.equals("IMPRESSION") && fields.get("objType").equals("AD"))
                 {
                     result.add(new MirrorDestination(
                         "adviews", 
                         uuidHash
                     ));
                 }
                 else if (action.equals("CLICK") && fields.get("objType").equals("AD"))
                 {
                     result.add(new MirrorDestination(
                         "adclicks", 
                         uuidHash
                     ));
                 }
                 else if (action.equals("PAGE_VIEW"))
                 {
                     result.add(new MirrorDestination(
                         "pageviews", 
                         uuidHash
                     ));
                 }
             } else if (eventType.equals("VDNAQuizUserAction")) { // quiz engine message
                 result.add(new MirrorDestination(
                     "useractivity", 
                     uuidHash
                 ));
             } else {
                 log.warn("Unknown event_type "  + eventType);
                 return result;
             }
 
             /*
              * If the event didn't end up in any primary topic, it's a problem.
              */
             if (result.size() == 0)
             {
                log.warn("No primary topic for event type `" + eventType + "`: " + metaMsg.message().payload().toString());
             }
 
             //TODO Collect metrics JIRA-35 Implement MBean
             /*
             try {
                 Double timestamp = Double.valueOf(fields.get("timestamp")) * 1000;
                 if (timestamp > CentralMirror.latestObservedTimestamp)
                 {
                     CentralMirror.latestObservedTimestamp = timestamp;
                 }
                 if (timestamp < CentralMirror.earliestObservedTimestamp)
                 {
                     CentralMirror.earliestObservedTimestamp = timestamp;
                 }
             } catch (Exception e)
             {
                 log.error("Could not extract timestamp from the json event", e);
             }
             */
         } catch (Exception e3) {
             log.error("DesinationResolver encountered serious error ", e3);
         }
 
         return result;
     }
 
    private void parseMinimumJsonMessage(Message message, Map<String,String> fields) throws IOException
     {
         ByteBuffer buffer = message.payload();
         byte [] bytes = new byte[buffer.remaining()];
         buffer.get(bytes);
        //read only the necessary fields (streaming jackson)
         JsonParser jp;
         try {
             jp = jsonFactory.createJsonParser(bytes);
         } catch (JsonParseException e) {
            log.warn("Message not in JSON format: " + new String(bytes)) ;
             throw new IOException("Invalid message json, see logs for more details", e);
         }
         try {
             int filled = 0;
             while(jp.nextToken() != null && filled < fields.size())
             {
                 if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                     String fieldName = jp.getCurrentName();
                     jp.nextToken();
                     String value = jp.getText();
                     if (fields.containsKey(fieldName))
                     {
                         value = value.equals("0") ? null : value;
                         fields.put(fieldName, value);
                         ++filled;
                     }
                 }
             }
         } finally {
             jp.close();
         }
 
     }
 }
