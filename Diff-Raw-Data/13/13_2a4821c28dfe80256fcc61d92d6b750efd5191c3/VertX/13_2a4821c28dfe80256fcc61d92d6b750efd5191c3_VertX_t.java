 package org.micoli.phone.ccphone.remote;
 
 import java.io.File;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.Vertx;
 import org.vertx.java.core.buffer.Buffer;
 import org.vertx.java.core.eventbus.EventBus;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.http.HttpServer;
 import org.vertx.java.core.http.HttpServerRequest;
 import org.vertx.java.core.http.ServerWebSocket;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.core.sockjs.SockJSServer;
 
 public class VertX {
 	private static net.sourceforge.peers.Logger logger;
 	static public Vertx vertx;
 	static ObjectMapper mapper = new ObjectMapper();
 	static EventBus eb;
 	static String guiEventAddress = "calls";
 	static String actionAddress = "action";
 	private static HttpServer server;
 
 	public static void publishGui(JsonObject jsonObject){
 		eb.publish(guiEventAddress, jsonObject);
 	}
 
 	public static void init(net.sourceforge.peers.Logger logger2) {
 		logger = logger2;
 		vertx = Vertx.newVertx("localhost");
 		eb = vertx.eventBus();
 
 		Handler<Message<JsonObject>> myHandler = new Handler<Message<JsonObject>>() {
 			public void handle(Message<JsonObject> message) {
 				logger.info("VertX event due to registration : ["
 						+ message.body.getString("text") + "]\n"
 						+ message.body.toString());
 			}
 		};
 
 		eb.registerHandler(guiEventAddress, myHandler);
 
 		server = vertx.createHttpServer();
 
 		server.requestHandler(new Handler<HttpServerRequest>() {
 			public void handle(HttpServerRequest req) {
 				logger.debug("HttpServerRequest path " + req.path);
 				String internalPath = req.path;
 				String documentRoot = "src/main/resources/eventbus";
 
 				if (internalPath.equals("/pub")) {
 					eb.publish(guiEventAddress, "test message published");
 					req.response.sendFile("src/main/resources/eventbus/null.html");
 				}
 				if (internalPath.equals("/")){
 					internalPath = "/index.html";
 				}
 
 				File file = new File(documentRoot+internalPath);
 				if (!file.exists()){
 					internalPath = "/404.html";
 				}
 				req.response.sendFile(documentRoot+internalPath);
 			}
 		}).websocketHandler(new Handler<ServerWebSocket>() {
 			public void handle(final ServerWebSocket ws) {
 				if (ws.path.equals("/eventbus")) {
 					ws.closedHandler(new Handler<Void>() {
 						public void handle(Void event) {
 							// removeDispatch(ws.binaryHandlerID, ws);
 						}
 					});
 					ws.dataHandler(new Handler<Buffer>() {
 						public void handle(Buffer data) {
 							eb.publish(guiEventAddress, data);
 							// ws.writeTextFrame(data.toString());
 						}
 					});
 				} else {
 					ws.reject();
 				}
 			}
 		});
 
 		JsonArray permitted = new JsonArray();
 		permitted.add(new JsonObject());
 		SockJSServer sockJSServer = vertx.createSockJSServer(server);
 		sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"),permitted, permitted);
 	}
 
 	public static void run(){
 		logger.info("start");
 		server.listen(8080);
 	}
 }
