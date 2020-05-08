 package controllers;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.core.NamedThreadFactory;
 import play.libs.Akka;
 import play.mvc.Controller;
 import play.mvc.Result;
 import scala.concurrent.ExecutionContext;
 import akka.dispatch.ExecutionContexts;
 import akka.dispatch.Futures;
 
 import com.google.common.base.Strings;
 
 public class Application extends Controller {
 
     public static final ObjectMapper MAPPER = new ObjectMapper();
 
 
     private static final ThreadPoolExecutor tpe = new ThreadPoolExecutor(300, 300, 0L,
             TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("dbEc"));
     private static final ExecutionContext dbEc = ExecutionContexts.fromExecutorService(tpe);
     
     public static Result getOrderInfo(final String name, final String company) {
         if (Strings.isNullOrEmpty(name)) {
             return badRequest("The name is mandatory but missing.");
         }
 
         return async(Akka.asPromise(Futures.future(orderInfoCallable(name, company), dbEc)));
         // return async(Akka.future(orderInfoCallable(name, company)));
     }
 
     private static Callable<Result> orderInfoCallable(final String name, final String company) {
         return new Callable<Result>() {
             @Override
             public Result call() throws Exception {
                 // Simulate some processing
                 Thread.sleep(50);
 
                 // Some arbitrary but stable values
                 final int price = Math.abs(name.hashCode());
                 final int availability = Math.abs((name + ":" + company).hashCode());
 
                 final ObjectNode result = MAPPER.createObjectNode();
                 result.put("price", price);
                 result.put("availability", availability);
 
                 return ok(result);
             }
         };
     }
   
 }
