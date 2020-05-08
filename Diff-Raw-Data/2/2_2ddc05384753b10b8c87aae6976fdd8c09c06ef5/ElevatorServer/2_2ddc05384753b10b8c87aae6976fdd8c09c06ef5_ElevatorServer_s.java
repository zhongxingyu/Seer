 package fr.binome.elevator.server;
 
 import fr.binome.elevator.model.CleverElevator;
 import fr.binome.elevator.model.Elevator;
 import fr.binome.elevator.model.ElevatorResponse;
 import spark.Request;
 import spark.Response;
 import spark.Route;
 
 import static spark.Spark.get;
 import static spark.Spark.setPort;
 
 public class ElevatorServer {
     private static final int PORT = new Integer(System.getenv("PORT"));
 
     public static void main(String[] args) {
         setPort(PORT);
 
 //        final Elevator elevator = new BasicElevator();
         final Elevator elevator = new CleverElevator();
 
         get(new Route("/hello") {
             @Override
             public Object handle(Request request, Response response) {
                 //Test
                 return "Hello world!";
             }
         });
 
         // /call?atFloor=[0-5]&to=[UP|DOWN]
         get(new Route("/call") {
             @Override
             public Object handle(Request request, Response response) {
                elevator.call(request.queryMap("atFloor").integerValue(), request.queryMap("to").toString());
 
                 return sendOkResponse(response);
             }
         });
 
         // /go?floorToGo=[0-5]
         get(new Route("/go") {
             @Override
             public Object handle(Request request, Response response) {
                 elevator.go(request.queryMap("floorToGo").integerValue());
 
                 return sendOkResponse(response);
             }
         });
 
         get(new Route("/userHasEntered") {
             @Override
             public Object handle(Request request, Response response) {
                 return sendOkResponse(response);
             }
         });
 
         get(new Route("/userHasExited") {
             @Override
             public Object handle(Request request, Response response) {
                 return sendOkResponse(response);
             }
         });
 
         // /reset?cause=information+message
         get(new Route("/reset") {
             @Override
             public Object handle(Request request, Response response) {
                 elevator.reset(request.queryMap("cause").toString());
 
                 return sendOkResponse(response);
             }
         });
 
         get(new Route("/nextCommand") {
             @Override
             public Object handle(Request request, Response response) {
                 ElevatorResponse elevatorResponse = elevator.nextCommand();
 
                 return elevatorResponse;
             }
         });
 
     }
 
     private static Object sendOkResponse(Response response) {
         response.status(200);
         response.body("");
 
         return response.raw();
     }
 
 }
