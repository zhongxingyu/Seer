 package com.openwager.rtp.test.integration.java;
 
 import org.junit.Test;
 import org.vertx.java.core.AsyncResult;
 import org.vertx.java.core.AsyncResultHandler;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.buffer.Buffer;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.http.HttpClient;
 import org.vertx.java.core.http.WebSocket;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.testtools.TestVerticle;
 
 import static org.vertx.testtools.VertxAssert.*;
 
 /**
  * Example Java integration test that deploys the module that this project builds.
  *
  * Quite often in integration tests you want to deploy the same module for all tests and you don't want tests
  * to start before the module has been deployed.
  *
  * This test demonstrates how to do that.
  */
 public class ModuleIntegrationTest extends TestVerticle {
 
     private static final int CONNS = 1;
     int connectCount = 0;
 
   @Test
   public void testSomethingElse() {
     // Whatever
     testComplete();
   }
 
     @Test
     public void testWebSocketServer() {
 
         long startTime;
         long endTime;
 
         startTime = System.currentTimeMillis();
         container.logger().info("Starting web socket test");
         HttpClient client = vertx.createHttpClient().setPort(8080).setHost("localhost").setMaxPoolSize(CONNS);
         for (int i = 0; i < CONNS; i++) {
             container.logger().info("Connecting ws: " + (i+1));
             client.connectWebsocket("/rtp", new Handler<WebSocket>() {
                 public void handle(WebSocket ws) {
 
                     ws.write(new Buffer(createEventMessage().toString()));
                     ws.dataHandler(new Handler<Buffer>() {
                         @Override
                         public void handle(Buffer buff) {
                             assertEquals(buff.toString(), createEventMessage().toString());
                             container.logger().info("Response:  " + buff.toString());
 
                             testComplete();
                         }
                     });
                 }
             });
         }
         endTime = System.currentTimeMillis();
         container.logger().info("Ending web socket test (asynchronous call) client in: " + (endTime - startTime) + " milliseconds");
     }
 
     // Here is a surprise why the service bus event published by WebSocket verticle is not catch by local handler
     // Maybe the event buss is not clustered
     @Test
     public void testWebSocketServerAndEventBus() {
         container.logger().info("Starting web socket and service bus test");
 
         vertx.eventBus().registerHandler("default.address", new Handler<Message>() {
             @Override
             public void handle(Message reply) {
                 assertEquals(createEventMessage().toString(), reply.body().toString());
                 container.logger().info("Response on Event Bus:  " + reply.body().toString());
 //                testComplete();
             }
         });
 
//        vertx.eventBus().publish("default.address", createEventMessage());
 
         HttpClient client = vertx.createHttpClient().setPort(8080).setHost("localhost").setMaxPoolSize(CONNS);
         container.logger().info("Connecting ws ");
 
         client.connectWebsocket("/rtp", new Handler<WebSocket>() {
             public void handle(WebSocket ws) {
 
                 ws.write(new Buffer(createEventMessage().toString()));
 
                 ws.dataHandler(new Handler<Buffer>() {
                     @Override
                     public void handle(Buffer buff) {
                         assertEquals(buff.toString(), createEventMessage().toString());
                         container.logger().info("Response on Web Socket:  " + buff.toString());
 
                         testComplete();
                     }
                 });
             }
         });
     }
 
     private JsonObject createEventMessage() {
         JsonObject message = new JsonObject().putString("_t", "msg");
         JsonObject header = new JsonObject().putString("_t", "hdr").putString("cid", "m4");
         JsonArray numbers = new JsonArray().addNumber(1).addNumber(2).addNumber(12312).addNumber(12);
         JsonObject body = new JsonObject().putString("_t", "sum").putArray("numbers", numbers);
         message.putObject("header", header);
         message.putObject("body", body);
         return message;
     }
 
     @Override
   public void start() {
     // Make sure we call initialize() - this sets up the assert stuff so assert functionality works correctly
     initialize();
     // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
     // don't have to hardecode it in your tests
     container.deployModule(System.getProperty("vertx.modulename"), new AsyncResultHandler<String>() {
       @Override
       public void handle(AsyncResult<String> asyncResult) {
       // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
       if (asyncResult.failed()) {
         container.logger().error(asyncResult.cause());
       }
       assertTrue(asyncResult.succeeded());
       assertNotNull("deploymentID should not be null", asyncResult.result());
       // If deployed correctly then start the tests!
       startTests();
       }
     });
   }
 
 }
