 package ${package}.controllers;
 
 import vinna.response.ResponseBuilder;
 import vinna.response.StringResponse;
 
 public class HelloControler {
 
   public ResponseBuilder sayHello(String name) {
     return new StringResponse(String.format("Hello %s !", name));
   }
}
