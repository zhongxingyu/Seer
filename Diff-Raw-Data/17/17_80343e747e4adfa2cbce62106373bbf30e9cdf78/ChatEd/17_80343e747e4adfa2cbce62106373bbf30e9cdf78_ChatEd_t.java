 /*
  * Copyright (c) 2012 Nuxeo SA (http://nuxeo.com/) and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Benoit Delbosc
  */
 
 package org.nuxeo.ecm.vertx.mod;
 
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.EventBus;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.http.HttpServer;
 import org.vertx.java.core.http.HttpServerRequest;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.core.logging.Logger;
 import org.vertx.java.core.sockjs.SockJSServer;
 import org.vertx.java.deploy.Verticle;
 
 /**
  * WebSocket server to handle browser communication.
  *
  */
 public class ChatEd extends Verticle {
 
     public static final String CHANNEL_NXIN = "chated.nxin";
 
     public static final String EVENT_BUS = "/chated";
 
     public void start() {
         final Logger logger = container.getLogger();
         JsonObject config = container.getConfig();
         logger.info("ChatEd config is " + config);
 
         HttpServer httpServer = vertx.createHttpServer();
 
         // Serve the static resources.
         httpServer.requestHandler(new Handler<HttpServerRequest>() {
             public void handle(HttpServerRequest req) {
                 if (req.path.equals("/test"))
                     req.response.sendFile("index.html");
                 if (req.path.endsWith("vertxbus.js"))
                     req.response.sendFile("vertxbus.js");
             }
         });
 
         // Subscribe to nuxeo message
         final EventBus eb = vertx.eventBus();
        Handler<Message<JsonObject>> myHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                logger.info("ChatEd Received a nuxeo message " + message.body.toString());
                logger.info("ChatEd retransmit to browsers " + message.body.toString());
                eb.publish(EVENT_BUS, message.body);
             }
         };
         eb.registerHandler(CHANNEL_NXIN, myHandler);
 
         // deploy NxIn server
         container.deployVerticle("org.nuxeo.ecm.vertx.mod.NxIn", config);
 
         JsonArray permitted = new JsonArray();
         permitted.add(new JsonObject()); // Let everything through
         SockJSServer sockJSServer = vertx.createSockJSServer(httpServer);
         sockJSServer.bridge(new JsonObject().putString("prefix", EVENT_BUS),
                 permitted, permitted);
 
         httpServer.listen((Integer) config.getNumber("chated_port"));
     }
 
 }
