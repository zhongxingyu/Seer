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
 
 import junit.framework.Assert;
 import org.investovator.controller.utils.enums.GameModes;
import org.investovator.controller.utils.events.GameEvent;
import org.investovator.controller.utils.events.GameEventListener;
 import org.investovator.controller.utils.exceptions.GameCreationException;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public class GameControllerImplTest {
 
     GameController controller;
 
     @Before
     public void setUp() throws Exception {
         controller = GameControllerImpl.getInstance();
 
     }
 
     @Test
     public void testCreateAndRemoveGameInstance() throws Exception {
 
         ArrayList<String> gameInstances = new ArrayList<>();
         gameInstances.add(controller.createGameInstance(GameModes.AGENT_GAME));
         Assert.assertTrue(gameInstances.contains(GameModes.AGENT_GAME.toString()));
 
         for(String instance : gameInstances){
             controller.removeGameInstance(instance);
         }
 
         Assert.assertTrue(!controller.getGameInstances().contains(GameModes.AGENT_GAME.toString()));
     }
 
     @Test
     public void  testMultiAgentCreation(){
 
         String agentGame = null;
 
         try {
             agentGame = controller.createGameInstance(GameModes.AGENT_GAME);
         } catch (GameCreationException e) {
             Assert.assertTrue(false);
         }
         try {
             agentGame = controller.createGameInstance(GameModes.AGENT_GAME);
         } catch (GameCreationException e) {
             if(e.getMessage().equals("Game Mode Already Exists")) Assert.assertTrue(true);
         }
 
         controller.removeGameInstance(agentGame);
 
     }
 
 
 
 
 }
