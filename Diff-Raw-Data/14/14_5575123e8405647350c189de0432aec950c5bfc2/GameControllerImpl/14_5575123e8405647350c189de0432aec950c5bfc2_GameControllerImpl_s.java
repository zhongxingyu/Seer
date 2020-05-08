 /*
  * investovator, Stock Market Gaming framework
  * Copyright (C) 2013  investovator
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.investovator.controller;
 
 import org.investovator.ann.nngaming.util.GameTypes;
 import org.investovator.controller.utils.enums.GameModes;
 import org.investovator.controller.utils.enums.GameStates;
 import org.investovator.controller.utils.events.GameEventListener;
 import org.investovator.controller.utils.exceptions.GameCreationException;
 import org.investovator.controller.utils.exceptions.GameProgressingException;
 
 import java.util.*;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public class GameControllerImpl implements GameController {
 
     private HashMap<String, GameFacade> gameInstances;
     private HashMap<String, GameStates> gameStates;
     private static GameControllerImpl instance;
 
     private GameControllerImpl(){
         gameInstances = new HashMap<>();
         gameStates = new HashMap<>();
     }
 
     public static synchronized GameController getInstance() {
         if(instance==null) instance = new GameControllerImpl();
         return instance;
     }
 
     @Override
     public String createGameInstance(GameModes type) throws GameCreationException{
         Collection<GameFacade> facades = gameInstances.values();
 
         for (Iterator<GameFacade> facade = facades.iterator(); facade.hasNext(); ) {
             GameFacade next = facade.next();
             if(next.getGameMode() == type) throw new GameCreationException("Game Mode Already Exists");
         }
 
         String instanceId = type.toString();
 
         gameInstances.put(instanceId, GameFactory.getInstance().createGame(type));
         gameStates.put(instanceId, GameStates.NEW);
 
         return type.toString();
     }
 
     @Override
     public void removeGameInstance(String instance) {
 
        if(gameStates.get(instance)==GameStates.RUNNING){
            stopGame(instance);
        }
 
        gameInstances.remove(instance);
        gameStates.remove(instance);
     }
 
     @Override
     public List<String> getGameInstances() {
         List<String> instanceList =  new ArrayList<>();
         instanceList.addAll(gameInstances.keySet()) ;
         return instanceList;
     }
 
     @Override
     public void registerListener(String instance, GameEventListener listener) {
         gameInstances.get(instance).registerListener(listener);
     }
 
     @Override
     public void removeListener(String instance, GameEventListener listener) {
         gameInstances.get(instance).removeListener(listener);
     }
 
     @Override
     public boolean startGame(String instance) throws GameProgressingException {
         boolean status = gameInstances.get(instance).startGame();
         if(status){
             gameStates.put(instance, GameStates.RUNNING);
         }
         return status;
     }
 
     @Override
     public void stopGame(String instance) {
         gameInstances.get(instance).stopGame();
         gameStates.put(instance, GameStates.STOPPED);
     }
 
     @Override
     public void setupGame(String instance, Object[] configurations) {
         gameInstances.get(instance).setupGame(configurations);
     }
 
     @Override
     public GameStates getCurrentGameState(String instance) {
         return gameStates.get(instance);
     }
 }
