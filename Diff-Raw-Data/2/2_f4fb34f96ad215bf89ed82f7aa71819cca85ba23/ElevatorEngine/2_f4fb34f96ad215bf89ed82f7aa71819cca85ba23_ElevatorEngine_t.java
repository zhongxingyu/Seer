 package com.bzn.codestory.elevator;
 
 import static spark.Spark.get;
 import static spark.Spark.setPort;
 
 import java.util.Arrays;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import spark.Request;
 import spark.Response;
 import spark.Route;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class ElevatorEngine {
 
 	private static Building building = null;
 
 	private static final Logger logger = LoggerFactory.getLogger(ElevatorEngine.class);
 
 	public static void main(String[] args) {
 		building = new Building();
 		ElevatorEngine.initServices(Integer.parseInt(System
 				.getProperty("app.port")));
 	}
 
 	public static void initServices(int port) {
 		setPort(port);
 		get(new Route("call") {
 			@Override
 			public Object handle(Request req, Response resp) {
 				int floor = Integer.valueOf(req.queryParams("atFloor"));
 				Direction direction = Direction.valueOf(req.queryParams("to"));
 				building.receiveCall(floor, direction);
 				resp.status(200);
 				return resp;
 			}
 		});
 		get(new Route("go") {
 			@Override
 			public Object handle(Request req, Response resp) {
 				int cabin = Integer.valueOf(req.queryParams("cabin"));
 				building.receiveGoTo(cabin, Integer.valueOf(req.queryParams("floorToGo")));
 				resp.status(200);
 				return resp;
 			}
 		});
 		get(new Route("userHasEntered") {
 			@Override
 			public Object handle(Request req, Response resp) {
 				int cabin = Integer.valueOf(req.queryParams("cabin"));
 				building.userEntered(cabin);
 				resp.status(200);
 				return resp;
 			}
 		});
 		get(new Route("userHasExited") {
 			@Override
 			public Object handle(Request req, Response resp) {
 				int cabin = Integer.valueOf(req.queryParams("cabin"));
 				building.userExited(cabin);
 				resp.status(200);
 				return resp;
 			}
 		});
 		get(new Route("reset") {
 			@Override
 			public Object handle(Request req, Response resp) {
 				int lower = Integer.valueOf(req.queryParams("lowerFloor"));
 				int higher = Integer.valueOf(req.queryParams("higherFloor"));
 				int cabinSize = Integer.valueOf(req.queryParams("cabinSize"));
 				int cabinCount = Integer.valueOf(req.queryParams("cabinCount"));
 
 				logger.info("RESET : cause : "
 						+ req.queryParams("cause"));
 
				building = new Building(lower, higher, cabinCount, cabinSize);
 				resp.status(200);
 				return resp;
 			}
 		});
 		get(new Route("nextCommands") {
 			@Override
 			public Object handle(Request req, Response resp) {
 				resp.status(200);
 				String[] commands = building.nextCommands();
 				logger.info("nextCommands : " + Arrays.toString(commands));
 				return StringUtils.join(commands, "\n");
 			}
 		});
 		get(new Route("status") {
 			@Override
 			public Object handle(Request req, Response resp) {
 				resp.type("application/json");
 				try {
 					ObjectMapper mapper = new ObjectMapper();
 					return mapper.writeValueAsString(building.elevators[0].getStatus());
 				} catch (JsonProcessingException e) {
 					logger.error("While processing status", e);
 					return "";
 				}
 			}
 		});
 	}
 
 }
