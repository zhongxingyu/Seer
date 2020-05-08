 /* 
  * Project       : Bachelor Thesis - SudokuPortlet game implementation as portlet
  * Document      : ServerSideApplication.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.sudoku;
 
 import java.util.HashSet;
 import java.util.Set;
 import javax.ws.rs.core.Application;
 import org.gatein.portal.examples.games.sudoku.service.*;
 
 /**
  * This class representes the Server side of the application.
  * It provides singletons of REST facade classes for RESTful application.
  *
  * @author Ondřej Fibich
  */
 public class ServerSideApplication extends Application
 {
     private Set<Object> singletons = new HashSet<Object>();

    public ServerSideApplication()
     {
        singletons.add(new GameRestFacade());
        singletons.add(new GameSolutionRestFacade());
         singletons.add(new SavedGameSolutionRestFacade());
         singletons.add(new LastPlayedGameSolutionRestFacade());
         singletons.add(new ServicesRestFacade());
    }

    @Override
    public Set<Object> getSingletons()
     {
        return singletons;
    }
 }
