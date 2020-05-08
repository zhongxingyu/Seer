 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ThreadLocalRandom;
 
 import models.World;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.AsyncAppender;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.libs.F;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
    private static final int TEST_DATABASE_ROWS = 10000;
    //http://stackoverflow.com/questions/3907929/should-i-make-jacksons-objectmapper-as-static-final
    private static final ObjectMapper objectMapper = new ObjectMapper();
 
 
     public static void index() {
         render();
     }
 
     public static void json() {
    	// returns {"_children":{"message":{"_value":"Hello World!"}},"_nodeFactory":{}}
         final ObjectNode result = objectMapper.createObjectNode();
         result.put("message", "Hello World!");
         renderJSON(result);
     }
 
     public static void db() {
     	db_sync();
     }
     
     public static void db_sync() {
     	 Integer queries = 1 ;
     	 String queryCount = params.get("queries") ;
     	 if( StringUtils.isNotEmpty(queryCount)){
     		 queries = Integer.parseInt(queryCount) ;
     	 }
     	 final List<World> worlds = new ArrayList<World>();
          final Random random = ThreadLocalRandom.current();
          for (int i = 0; i < queries; ++i) {
              Long id = Long.valueOf(random.nextInt(TEST_DATABASE_ROWS) + 1);
        	 World result = World.findById(id);
         	 worlds.add(result);
          }
     	 renderJSON( worlds) ;
     }
     /*
 	public static Result db(final Integer queries) {
         return async(
                 future(new Callable<Result>() {
                     @Override
                     public Result call() {
                         final Random random = ThreadLocalRandom.current();
                         final List<F.Promise<? extends World>> promises = new ArrayList<F.Promise<? extends World>>(queries);
                         for (int i = 0; i < queries; ++i) {
                             promises.add(future(findWorld(Long.valueOf(random.nextInt(TEST_DATABASE_ROWS) + 1))));
                         }
                         final List<World> worlds = F.Promise.sequence(promises).get(5L * queries, TimeUnit.SECONDS);
                         return ok(Json.toJson(worlds));
                     }
 
                     private Callable<World> findWorld(final Long id) {
                         return new Callable<World>() {
                             @Override
                             public World call() {
                                 return World.find.byId(id);
                             }
                         };
                     }
                 })
         );
 */
 }
