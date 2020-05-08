 package com.whiterabbit.bondi;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.platform.Verticle;
 
 import com.whiterabbit.bondi.domain.Position;
 
 public class DataVerticle extends Verticle {
 
 	private static final Logger log = LoggerFactory.getLogger(DataVerticle.class);
 
 	private Map<String, Map<String, Position>> data = new HashMap<>();
 
 	@Override
 	public void start() {
 		log.info("Starting DataVerticle");
 
 		vertx.eventBus().registerHandler(Messages.PUT_DATA,
 				new Handler<Message<JsonObject>>() {
 					@Override
 					public void handle(Message<JsonObject> message) {
 						JsonObject body = message.body();
 						String bus = body.getString("bus");
 						String clientId = body.getString("clientId");
 
 						if (!data.containsKey(bus)) {
 							data.put(bus, new HashMap<String, Position>());
 						}
 
 						Map<String, Position> positions = data.get(bus);
 
 						if (positions.containsKey(clientId)) {
 							updatePosition(positions.get(clientId), body);
 						} else {
 							positions.put(clientId, createPosition(body));
 						}
 						
 						body.removeField("clientId"); // Remove the clientId to avoid publishing it.
 						vertx.eventBus().publish("bondis.server.position.update", body);
 					}
 				});
 		
         vertx.eventBus().registerHandler("bondis.client.list",new Handler<Message<JsonObject>>() {
             @Override
             public void handle(Message<JsonObject> message) {
                final String bus = message.body().getString("busLine");
 
 				JsonArray result = new JsonArray();
 
 				if (data.containsKey(bus)) {
 					Map<String, Position> positions = data.get(bus);
 
 					log.debug(String.format(
 							"Found bus data, collecting %s positions",
 							positions.size()));
 
 					for (Position position : positions.values()) {
 						result.addObject(positionToJson(position));
 					}
 				}
 				message.reply(result);
             }
         });
 
 		log.debug("DataVerticle started");
 	}
 
 	protected Position createPosition(JsonObject body) {
 		Position position = new Position();
 		position.setLatitude(body.getNumber("latitude").doubleValue());
 		position.setLongitude(body.getNumber("longitude").doubleValue());
 		position.setTimestamp(now());
 		return position;
 	}
 	
 	protected void updatePosition(Position position, JsonObject body) {
 		position.setLatitude(body.getNumber("latitude").doubleValue());
 		position.setLongitude(body.getNumber("longitude").doubleValue());
 		position.setTimestamp(now());
 	}
 
 	protected JsonObject positionToJson(Position position) {
 		JsonObject jsonObject = new JsonObject();
 		jsonObject.putNumber("latitude", position.getLatitude());
 		jsonObject.putNumber("longitude", position.getLongitude());
 		return jsonObject;
 	}
 
 	protected Date now() {
 		return new Date();
 	}
 
 }
