 package com.nextgestion.bjlb.main;
 
 import com.basho.riak.client.IRiakClient;
 import com.basho.riak.client.RiakException;
 import com.basho.riak.client.RiakFactory;
 import com.nextgestion.bjlb.handler.RestHandler;
 import com.nextgestion.bjlb.handler.StaticWebHandler;
 import com.nextgestion.bjlb.handler.impl.AddingRestHandlerImpl;
 import com.nextgestion.bjlb.handler.impl.NavigationRestHandlerImpl;
 import com.nextgestion.bjlb.handler.impl.StaticWebHandlerImpl;
 import com.nextgestion.bjlb.repository.JokesRepository;
 import com.nextgestion.bjlb.service.PageContentService;
 import org.vertx.java.core.http.RouteMatcher;
 import org.vertx.java.platform.Verticle;
 
 import java.util.logging.Logger;
 
 public class VertxServer extends Verticle {
 	
     public static final String GLOBAL_LOGGER = "Global Logger";
 
 	private static final Integer PORT_NUMBER = 8182;
 
     private static final Logger logger = Logger.getLogger(GLOBAL_LOGGER);
 
     @Override
     public void start() {
 
         try {
             final IRiakClient riakClient = RiakFactory.pbcClient("localhost", 10017);
             final JokesRepository jokesRepository = new JokesRepository(riakClient);
             PageContentService pageContentService = new PageContentService(jokesRepository);
 
             StaticWebHandler staticWebHandler = new StaticWebHandlerImpl();
             RestHandler navigationRestHandler = new NavigationRestHandlerImpl(pageContentService);
             RestHandler addingRestHandler = new AddingRestHandlerImpl();
 
             final RouteMatcher routeMatcher = new RouteMatcher();
 
             routeMatcher.put("/jokeContent/addingJoke", addingRestHandler);
             routeMatcher.get("/jokeContent/:date", navigationRestHandler);
             routeMatcher.getWithRegEx(".*", staticWebHandler);
 
             vertx.createHttpServer().requestHandler(routeMatcher).listen(PORT_NUMBER);
 
         } catch (RiakException e) {
            logger.severe("Impossible de se connecter à la base ");
         }
 
     }
 }
