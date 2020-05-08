 import driver.Mongo;
 import json.JsonOk;
 import model.Article;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.EventBus;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.deploy.Verticle;
 
 /**
  * @author victor benarbia
  */
 public class Dashboard extends Verticle
 {
    public static final String changes = "client.changes"; // Notify client something has changes
     public static final String refresh = "client.refresh"; // Refresh view
     public static String findAll = "server.findall";       // Find articles
     private final int limit = 9;
     private EventBus eb;
 
     @Override
     public void start() throws Exception
     {
         System.out.println("Start dashboard verticle");
 
         eb = vertx.eventBus();
         eb.registerHandler(findAll, findAll());
     }
 
     private Handler<? extends Message> findAll()
     {
         return new Handler<Message<JsonObject>>()
         {
             @Override
             public void handle(Message message)
             {
                 int page = ((JsonObject)message.body).getInteger("page");
                 System.out.println("Find all with page = " + page);
                 if(page == -1)
                 {
                     page = 0;
                 }
                 // Find all articles
                 eb.send(Mongo.address, Mongo.findAll(Article.name, page * limit, limit), new Handler<Message<JsonObject>>()
                 {
                     @Override
                     public void handle(Message<JsonObject> jsonObjectMessage)
                     {
                         System.out.println("refresh dashboard with " + jsonObjectMessage.body);
                         eb.send(Dashboard.refresh, jsonObjectMessage.body);
                     }
                 });
 
                 // Ack
                 message.reply(new JsonOk());
             }
         };
     }
 
 }
