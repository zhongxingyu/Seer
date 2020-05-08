 /* 
  * Project       : Bachelor Thesis - Sudoku game implementation as portlet
  * Document      : GameRestFacade.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.sudoku.service;
 
 import java.net.URI;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import org.gatein.portal.examples.games.sudoku.controller.GamesController;
 import org.gatein.portal.examples.games.sudoku.entity.Game;
 import org.gatein.portal.examples.games.sudoku.entity.GameSolution;
 import org.gatein.portal.examples.games.sudoku.entity.datatype.GameType;
 import org.gatein.portal.examples.games.sudoku.provider.generator.Generator;
 
 /**
  * Game REST Facade Class
  *
  * @author Ondřej Fibich
  */
 @Path("game")
 public class GameRestFacade
 {
     /**
      * An instance of Games Controller
      */
     private GamesController gamesController;
 
     /**
      * A construct of the class - creates an instance of the Games Controller
      */
     public GameRestFacade()
     {
         gamesController = new GamesController();
     }
 
     @POST
     @Consumes({"application/xml", "application/json"})
     public Response create(Game game)
     {
         try
         {
             game.setInitValues(Generator.generate(game.getTypeDifficulty()));
             game.setInitDate(new Date());
             game.setType(GameType.GENERATED);
             game.setTypeServiceId(null);
             gamesController.create(game);
             return Response.created(URI.create(game.getId().toString())).build();
         }
         catch (Exception ex)
         {
             final String name = GameRestFacade.class.getName();
             Logger.getLogger(name).log(Level.WARNING, null, ex);
             return Response.notModified(ex.toString()).build();
         }
     }
 
     @GET
     @Path("{id}")
     @Produces({"application/xml", "application/json"})
     public Game find(@PathParam("id") Integer id)
     {
         return gamesController.findGame(id);
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     public List<Game> findAll()
     {
         return gamesController.findGameEntities();
     }
 
     @GET
     @Path("proposals/{uid}")
     @Produces({"application/xml", "application/json"})
     public List<Game> findAllProposals(@PathParam("uid") String id)
     {
         return gamesController.findGameProposalEntities(id);
     }
     
     @GET
     @Path("stats")
     @Produces({"application/json"})
     public String statsOfGames(@PathParam("id") Integer id)
     {
         StringBuilder b = new StringBuilder();
         Map<String, Object> totalStats;
         
         try
         {
             totalStats = gamesController.findStatistics();
             
             b.append("{\"state\":true, \"stats\": {");
             
             for (Iterator it = totalStats.keySet().iterator(); it.hasNext();)
             {
                 final String key = (String) it.next();
                 
                 b.append("\"");
                 b.append(key);
                 b.append("\":");
                 b.append(totalStats.get(key));
                 
                 if (it.hasNext())
                     b.append(",");
             }
             
             b.append("}}");
             
             return b.toString();
         }
         catch (Exception ex)
         {
             return "{\"state\":false,\"message\":\""
                  + ex.getMessage().replace('"', '\'')
                  + "\"}";
         }
     }
     
     @GET
     @Path("stats/{id}")
     @Produces({"application/json"})
     public String statsOfGame(@PathParam("id") Integer id)
     {
         StringBuilder b = new StringBuilder();
         Map<String, Object> totalStats;
         
         try
         {
             totalStats = gamesController.findStatisticsOf(id);
             
             b.append("{\"state\":true, \"stats\": {");
             
             for (Iterator it = totalStats.keySet().iterator(); it.hasNext();)
             {
                 final String key = (String) it.next();
                 
                 b.append("\"");
                 b.append(key);
                 b.append("\":");
                 b.append(totalStats.get(key));
                 
                 if (it.hasNext())
                     b.append(",");
             }
             
             b.append("}}");
             
             return b.toString();
         }
         catch (Exception ex)
         {
             return "{\"state\":false,\"message\":\""
                  + ex.getMessage().replace('"', '\'')
                  + "\"}";
         }
     }
     
     @GET
     @Path("stats/best_solvers/{id}")
     @Produces({"application/json"})
     public List<GameSolution> bestSolversOfGame(@PathParam("id") Integer id)
     {
         return gamesController.findBestSolvedGameSolutionEntities(id);
     }
     
     @GET
     @Path("stats/best_solvers")
     @Produces({"application/json"})
     public String bestSolvers()
     {
         StringBuilder b = new StringBuilder();
         List<Map<String, Object>> solvers;
         
         try
         {
             solvers = gamesController.findBestSolvers();
             
             b.append("{\"state\":true, \"solvers\": [");
             
             for (Iterator<Map<String, Object>> it = solvers.iterator(); it.hasNext();)
             {
                 final Map<String, Object> map = it.next();
                 
                 b.append("{");
                 
                 for (Iterator it2 = map.keySet().iterator(); it2.hasNext();)
                 {
                     final String key = String.valueOf(it2.next());
                     final Object value = map.get(key);
                 
                     b.append("\"");
                     b.append(key);
                     b.append("\":");
                     
                     if (value instanceof String && value != null)
                         b.append("\"").append(value).append("\"");
                     else
                         b.append(value);
 
                     if (it2.hasNext())
                         b.append(",");
                 }
                 
                 b.append("}");
                 
                 if (it.hasNext())
                     b.append(",");
             }
             
             b.append("]}");
             
             return b.toString();
         }
         catch (Exception ex)
         {
             return "{\"state\":false,\"message\":\""
                  + ex.getMessage().replace('"', '\'')
                  + "\"}";
         }
     }
     
 }
