 package cz.hrajlarp.controller;
 
 import cz.hrajlarp.model.Rights;
 import cz.hrajlarp.model.dao.GameDAO;
 import cz.hrajlarp.model.dao.UserAttendedGameDAO;
 import cz.hrajlarp.model.dao.UserDAO;
 import cz.hrajlarp.model.entity.GameEntity;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Matheo
  * Date: 14.3.13
  * Time: 22:09
  */
 @Controller
 public class CalendarController {
 
     @Autowired
     private GameDAO gameDAO;
 
     @Autowired
     private UserDAO userDAO;
 
     @Autowired
     private UserAttendedGameDAO userAttendedGameDAO;
 
     @Autowired
     private Rights rights;
 
     /**
      * Controller for view of calendar
      * @param model model
      * @return String of .JSP file name mapped for view of calendar
      */
     @RequestMapping(value = "/kalendar", method= RequestMethod.GET)
     public String calendar(Model model) {
         List <GameEntity> futureGames = gameDAO.getFutureGames();
         List <GameEntity> formerGames = gameDAO.getFormerGames();
 
         List<GameEntity> futureGameResult = new ArrayList<GameEntity>();
         List<GameEntity> formerGamesResult = new ArrayList<GameEntity>();
         for(GameEntity game: futureGames) {
             if(game.getConfirmed()) {
                 futureGameResult.add(game);
             }
         }
         for(GameEntity game: formerGames) {
             if(game.getConfirmed()) {
                 formerGamesResult.add(game);
             }
         }
 
         model.addAttribute("futureGames", futureGameResult);
         model.addAttribute("formerGames", formerGamesResult);
 
         if (rights.isLogged()){
             List<GameEntity> availableGames = userAttendedGameDAO.
                     filterAvailableGames(futureGames, rights.getLoggedUser());
 
             model.addAttribute("availableGames", availableGames);
             model.addAttribute("isLogged", true);
         }
         return "calendar";
     }
 }
