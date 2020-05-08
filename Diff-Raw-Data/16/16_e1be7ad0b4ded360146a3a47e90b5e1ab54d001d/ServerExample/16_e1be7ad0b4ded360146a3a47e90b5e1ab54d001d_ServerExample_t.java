 package org.vertx.maven.plugin.test;
 
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.http.HttpServerRequest;
 import org.vertx.java.platform.Verticle;
 
 public class ServerExample extends Verticle {
 
 	@Override
 	public void start() {
 		vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
 			@Override
 			public void handle(final HttpServerRequest req) {
 				for (final String key : req.headers().keySet()) {
 					System.out.println(key + ":" + req.headers().get(key));
 				}
				req.response().headers().put("Content-Type", "text/html; charset=UTF-8");
				req.response().end("<html><body><h1>Hello from vert.x: Java</h1></body></html>");
 			}
 		}).listen(8080);
 	}
 }
