 /**
  * 
  */
 package com.mcamier.apps.elevator;
 
 import static spark.Spark.get;
 import static spark.Spark.setPort;
 import spark.Request;
 import spark.Response;
 import spark.Route;
 
 import com.mcamier.apps.elevator.engine.IElevatorEngine;
 import com.mcamier.apps.elevator.engine.ScanElevatorEngine;
 import com.mcamier.apps.elevator.request.CallRequest;
 import com.mcamier.apps.elevator.request.DestinationRequest;
 import com.mcamier.apps.elevator.utils.Direction;
 
 /**
  * @author fufuuu
  *
  */
 public class Elevator {
 	
	private final static IElevatorEngine engine = new ScanElevatorEngine(6);
 
 	public static void main(String[] args) {
 		lauchElevatorServer(8000);
 	}
 	
 	public static void lauchElevatorServer(int port) {
 		setPort(port);
 		
 		get(new Route("/reset") {
 			@Override
 			public Object handle(Request request, Response response) {
 				engine.reset();
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/go") {
 			@Override
 			public Object handle(Request request, Response response) {
				engine.haveToGoTo(new DestinationRequest(Integer.parseInt(request.params("floorToGo"))));
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/call") {
 			@Override
 			public Object handle(Request request, Response response) {
				int atFloor = Integer.parseInt(request.params("atFloor"));
				Direction toGo = (request.params("to") == "UP") ? Direction.UP : Direction.DOWN; 
 				engine.call(new CallRequest(atFloor, toGo));
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/userHasEntered") {
 			@Override
 			public Object handle(Request request, Response response) {
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/userHasExited") {
 			@Override
 			public Object handle(Request request, Response response) {
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/nextCommand") {
 			@Override
 			public Object handle(Request request, Response response) {
 				response.status(200);
 				return engine.getNextCommand();
 			}
 		});
 	}
 }
