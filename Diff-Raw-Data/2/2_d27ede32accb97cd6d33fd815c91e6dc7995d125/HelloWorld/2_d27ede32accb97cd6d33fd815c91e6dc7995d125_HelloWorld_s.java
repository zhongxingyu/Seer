 import static spark.Spark.*;
 import spark.*;
 
 public class HelloWorld {
   public static void main(String[] args) {
    setPort(Integer.parseInt(System.getEnv("PORT")));
     get(new Route("/hello") {
       @Override
       public Object handle(Request request, Response response) {
         return "Hello World!";
       }
     });
   }
 }
