 /* 
  * Project       : Bachelor Thesis - Sudoku game implementation as portlet
  * Document      : GameSolutionRestFacade.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.sudoku.service;
 
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import org.gatein.portal.examples.games.sudoku.controller.GameSolutionsController;
 import org.gatein.portal.examples.games.sudoku.controller.GamesController;
 import org.gatein.portal.examples.games.sudoku.entity.GameSolution;
 import org.gatein.portal.examples.games.sudoku.util.GameUtil;
 
 /**
  * Game Solution REST Facade Class
  *
  * @author Ondřej Fibich
  */
 @Path("game_solution")
 public class GameSolutionRestFacade
 {
     /**
      * An instance of Game Solutions Controller
      */
     private GameSolutionsController gameSolutionsController;
 
     /**
      * A construct of the class - creates an instance of the Game Solutions Controller
      */
     public GameSolutionRestFacade()
     {
         gameSolutionsController = new GameSolutionsController();
     }
 
     @POST
     @Path("{gameId}")
     @Consumes({"application/xml", "application/json"})
     public Response create(GameSolution gameSolution,
                            @PathParam("gameId") Integer gameId)
     {
         GamesController gamesController;
 
         try
         {
             gamesController = new GamesController();
             gameSolution.setGameId(gamesController.findGame(gameId));
             gameSolution.setTimeStart(new Date());
             gameSolution.setFinished(false);
             gameSolution.setLasting(0);
             gameSolutionsController.create(gameSolution);
             return Response.created(URI.create(gameSolution.getId().toString())).build();
         }
         catch (Exception ex)
         {
             final String name = GameSolutionRestFacade.class.getName();
             Logger.getLogger(name).log(Level.WARNING, null, ex);
             return Response.notModified(ex.toString()).build();
         }
     }
     
     @PUT
     @Consumes({"application/xml", "application/json"})
     public Response edit(GameSolution gameSolution)
     {
         GameSolution merged;
         
         try
         {
             if (gameSolution.isFinished() &&
                 !GameUtil.check(gameSolution.getValues(), true).isEmpty())
             {
                 throw new IllegalArgumentException("The end of a game is incorrect");
             }
             
             merged = gameSolutionsController.findGameSolution(gameSolution.getId());
             
             if (merged.isFinished())
             {
                 merged.setRating(gameSolution.getRating());
             }
             else
             {
                 merged.setFinished(gameSolution.isFinished());
                 merged.setLasting(gameSolution.getLasting());
                 merged.setValues(gameSolution.getValues());
             }
             
             gameSolutionsController.edit(merged);
             return Response.ok().build();
         }
         catch (Exception ex)
         {
             final String name = GameSolutionRestFacade.class.getName();
             Logger.getLogger(name).log(Level.WARNING, null, ex);
             return Response.notModified(ex.getMessage()).build();
         }
     }
 
     @GET
     @Path("{id}")
     @Produces({"application/xml", "application/json"})
     public Response find(@PathParam("id") Integer id)
     {
         GameSolution gameSolution = gameSolutionsController.findGameSolution(id);
         
         if (gameSolution != null)
         {    
             return Response.ok(gameSolution).build(); 
         }
         else
         {
             return Response.status(Response.Status.NOT_FOUND).build(); 
         }
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     public List<GameSolution> findAll()
     {
         return gameSolutionsController.findGameSolutionEntities();
     }
 
     @GET
     @Path("unfinished/{uid}")
     @Produces({"application/xml", "application/json"})
     public List<GameSolution> findUnfinishedOfUser(@PathParam("uid") String uid)
     {
         return gameSolutionsController.findUnfinishedGameSolutionEntitiesOfUser(uid);
     }
     
     @POST
    @Path("check")
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    public String checkWithoutNote(String gameSolutionValues)
    {
        try
        {
            List<Integer> incorrectFields = GameUtil.check(gameSolutionValues);
            
            return "{\"state\":true, \"check\": {\"valid\":"
                    + incorrectFields.isEmpty() + ",\"fields\":["
                    + GameUtil.join(incorrectFields.toArray(), ",")
                    + "]}}";
        }
        catch (Exception ex)
        {
            return "{\"state\":false,\"message\":\""
                    + ex.getMessage().replace('"', '\'')
                    + "\"}";
        }
    }
    
    @POST
     @Path("check/{id}")
     @Consumes({"text/plain"})
     @Produces({"application/json"})
     public String check(String gameSolutionValues, @PathParam("id") Integer id)
     {
         GameSolution gameSolution = gameSolutionsController.findGameSolution(id);
         List<Integer> incorrectFields;
         
         try
         {
             if (gameSolution == null)
             {
                 throw new Exception("An incorrect identificator of the solution");
             }
             
             if (gameSolution.getCheckLeft() >= GameSolution.MAX_CHECK_COUNT)
             {
                 throw new Exception("You can check each game solution just 3 times");
             }
             else
             {
                 gameSolution.setCheckLeft(gameSolution.getCheckLeft() + 1);
                 gameSolutionsController.edit(gameSolution);
             }
             
             incorrectFields = GameUtil.check(gameSolutionValues);
             
             return "{\"state\":true, \"check\": {\"valid\":"
                     + incorrectFields.isEmpty() + ",\"fields\":["
                     + GameUtil.join(incorrectFields.toArray(), ",")
                     + "]}}";
         }
         catch (Exception ex)
         {
             return "{\"state\":false,\"message\":\""
                     + ex.getMessage().replace('"', '\'')
                     + "\"}";
         }
     }
     
 }
