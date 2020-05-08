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
 import org.vertx.java.core.buffer.Buffer;
 import org.vertx.java.core.eventbus.EventBus;
 import org.vertx.java.core.http.HttpServer;
 import org.vertx.java.core.http.HttpServerRequest;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.core.logging.Logger;
 import org.vertx.java.deploy.Verticle;
 
 public class NxIn extends Verticle {
 
     public void start() {
         final Logger logger = container.getLogger();
         JsonObject config = container.getConfig();
         HttpServer server = vertx.createHttpServer();
         final EventBus eb = vertx.eventBus();
 
         logger.info("NxIn config is " + config);
         server.requestHandler(new Handler<HttpServerRequest>() {
             public void handle(final HttpServerRequest request) {
                 request.bodyHandler(new Handler<Buffer>() {
                     public void handle(Buffer body) {
                         // The entire body has now been received
                         logger.info("The total body received was "
                                 + body.length() + " bytes");
                         request.response.statusCode = 200;
                         request.response.statusMessage = "Cool thanks";
                         request.response.end();
                        eb.publish(ChatEd.CHANNEL_NXIN, body);
                     }
                 });
 
             }
         });
         server.listen((Integer) config.getNumber("nxin_port"));
     }
 }
