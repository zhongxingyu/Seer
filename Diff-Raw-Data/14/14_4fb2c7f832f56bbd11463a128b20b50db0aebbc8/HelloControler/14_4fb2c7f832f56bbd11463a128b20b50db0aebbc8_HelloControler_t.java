 package ${package}.controllers;
 
import vinna.response.Response;
import vinna.response.StringResponse;
 
 public class HelloControler {
 
    public Response index() {
        return new StringResponse("Go to /hello/{your name} for a free hug !");
     }
 
    public Response sayHello(String name, String ohai) {
        return new StringResponse(String.format("%s %s !", (ohai == null ? "Ohai" : ohai), name));
     }
 }
