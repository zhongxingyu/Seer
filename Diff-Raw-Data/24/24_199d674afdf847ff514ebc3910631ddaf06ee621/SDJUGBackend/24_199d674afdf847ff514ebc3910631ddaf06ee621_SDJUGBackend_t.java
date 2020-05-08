 package sdjug;
 
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.deploy.Verticle;
 
 public class SDJUGBackend extends Verticle {
     @Override
     public void start() throws Exception {
         deployMongoModule();
         registerHandler();
     }
 
     private void registerHandler() {
         Handler<Message<JsonObject>> loadHandler = new Handler<Message<JsonObject>>() {
             public void handle(final Message<JsonObject> message) {
                 System.out.println("I received a load message!");
                 processLoad(message);
             }
         };
         vertx.eventBus().registerHandler("sdjug.eventbus.message.load", loadHandler);
 
         Handler<Message> saveHandler = new Handler<Message>() {
             public void handle(Message message) {
                 System.out.println("I received a save message!");
                 processSave(message);
             }
         };
         vertx.eventBus().registerHandler("sdjug.eventbus.message.save", saveHandler);
     }
 
     private void deployMongoModule() {
         JsonObject config = new JsonObject();
         config.putString("address", "sdjug.eventbus.mongo");
         config.putString("host", "localhost");
         config.putNumber("port", 27017);
         config.putString("db_name", "arconsisdb");
 
         container.deployModule("vertx.mongo-persistor-v1.2.1", config);
     }
 
 
     private void processLoad(final Message<JsonObject> message) {
         System.out.println(message.body);
 
         final JsonObject mongoJson = new JsonObject();
         mongoJson.putString("action", "find");
         mongoJson.putString("collection", message.body.getString("collection"));
         mongoJson.putObject("sort", new JsonObject().putNumber("created",-1));
         mongoJson.putObject("matcher", new JsonObject());
 
         vertx.eventBus().send("sdjug.eventbus.mongo", mongoJson, createLoadReplyHandler(message));
     }
 
     private void processSave(final Message<JsonObject> message) {
         System.out.println(message.body);
 
         final JsonObject mongoJson = new JsonObject();
         mongoJson.putString("action", "save");
         mongoJson.putString("collection", message.body.getString("collection"));
         mongoJson.putObject("document", message.body);
 
         vertx.eventBus().send("sdjug.eventbus.mongo", mongoJson, createSaveReplyHandler(message));
     }
 
     private Handler<Message<JsonObject>> createSaveReplyHandler(final Message<JsonObject> message) {
         return new Handler<Message<JsonObject>>() {
             @Override
             public void handle(final Message<JsonObject> mongoMessage) {
                 System.out.println(mongoMessage.body);
 
                 final JsonObject replyJson  = new JsonObject();
                 replyJson.putString("status", "ok");
                 replyJson.putString("id", mongoMessage.body.getString("_id"));
                 message.reply(replyJson);
             }
         };
     }
 
     private Handler<Message<JsonObject>> createLoadReplyHandler(final Message<JsonObject> message) {
         return new Handler<Message<JsonObject>>() {
             @Override
             public void handle(final Message<JsonObject> mongoMessage) {
                //System.out.println(mongoMessage.body);
                 message.reply(mongoMessage.body);
             }
         };
     }
 
 }
 
 
