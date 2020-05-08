 package controllers;
 
 import models.PlayBasedServerConfiguration;
 import models.WallboardData;
 import models.WallboardException;
 import models.bamboo.*;
 import models.jira.*;
 import play.cache.Cache;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import java.util.List;
 
 /**
  * Wall board main controller
  */
 public class Wallboard extends Controller {
 
 
     private static String[] scrumBoardIds = {"6", "5", "7", "8", "17", "2", "9", "10"};
 
     private static String[] bambooPlanKeys = {"EFRAME-DEF", "EFRAME-SONAR"};
 
     static {
     }
 
 
     /**
      * Renders the anglar application
      *
      * @return
      */
     public static Result htmlWallboard() {
         return ok(views.html.wallboardAngular.render("title"));
 
     }
 
     /**
      * Return a JSON representation of a wall board
      *
      * @return
      */
     public static Result wallboard() {
 
         WallboardData wallBoardData;
 
         try {
             wallBoardData = getOrBuildWallboard();
         } catch (Exception e) {
            return internalServerError(e.getMessage());
         }
 
         return ok(Json.toJson(wallBoardData));
     }
 
     private static WallboardData getOrBuildWallboard() throws Exception {
 
         //Try cache
         if (Cache.get("wallboard") != null) {
             Object cachedObject = Cache.get("wallboard");
             if (cachedObject instanceof WallboardData) {
                 play.Logger.debug("Returning cached instance of Wallboard Data");
                 return (WallboardData) cachedObject;
             }
         }
 
 
         //ask further to construct a representation of a WallBoard
         WallboardData wallboardData = buildWallboardData();
         //Save in cache for 10 minutes
         Cache.set("wallboard", wallboardData, 60 * 10);
         return wallboardData;
 
     }
 
     /**
      * Buil the wall board
      *
      * @return
      * @throws Exception
      */
     private static WallboardData buildWallboardData() throws Exception {
 
 
         //Create a JiraServer
         JiraServerConfiguration jiraServerConfiguration = PlayBasedServerConfiguration.newJiraServerConfiguration();
         JiraServer jiraServer = new JiraServerFactory().create(jiraServerConfiguration);
 
         //Create a BambooServer Server
         BambooServerConfiguration bambooServerConfiguration = PlayBasedServerConfiguration.newBambooServerConfiguration();
         BambooServer bambooServer = BambooServerFactory.create(bambooServerConfiguration);
 
         List<ScrumBoard> scrumBoards;
 
         try {
             scrumBoards = new ScrumBoardBuilder().withIds(scrumBoardIds).with(jiraServer).build();
         } catch (WallboardException e) {
             play.Logger.error("Error", e);
             throw new Exception(e);
         }
 
 
         List<BambooBuild> bambooBuilds = new BambooBuildBuilder().withPlanKey(bambooPlanKeys).with(bambooServer).build();
 
 
         return new WallboardData(scrumBoards, bambooBuilds);
     }
 
 }
 
