 /*
  *  This file is part of CandideRL.
  *
  *  CandideRL is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  CandideRL is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with CandideRL.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package game;
 import game.ai.ArtificialIntelligence;
 import game.characters.*;
 import game.map.Map;
 import game.map.MapFactory;
 import game.ui.GameUI;
 import game.utility.*;
 
 import java.util.ArrayList;
 
 public final class GameEngine {
 	
 	public static void main(String args[]) {
 		play();
 	}
 	
 	private static Player player = null;
 	private static ArrayList<NPC> npcs = null;
 	private static MessageLog messageLog = null;
 	private static int currentTurn;
     private static Map currentMap = null;
 	
 	private GameEngine() { }
 	
 	private static void init() {
 		GameUI.init();
         MapFactory.getInstance().setScreenSize(GameUI.getScreenWidth(), GameUI.getScreenHeight());
         currentMap = MapFactory.getInstance().getMap();
 		npcs = new ArrayList<NPC>();
 		messageLog = new MessageLog(500);
 		player = Player.getInstance();
 		ArtificialIntelligence.init(player, 
 				(GameUI.getMapWidth() + GameUI.getMapHeight()) / 2 + 100);
 		
 		npcs.add(new NPC("troll", "A furious beast with sharp claws.",
 				new ColoredChar('t', ColoredChar.RED)));
 //		npcs.add(new NPC("goblin", "A regular goblin.",
 //				new ColoredChar('g', ColoredChar.GREEN)));
 		
 		currentMap.putGameCharacter(player, Position.getPosition(43, 1));
 		for ( NPC mob : npcs ) 
 			currentMap.putGameCharacter(mob, Position.getPosition(1, 1));
 		currentTurn = 0;
 	}
 	
 	public static void showMessage(String msg) {
 		messageLog.add(msg);
 		GameUI.showMessage(msg);
 	}
 	
 	private static void processActions() {
 		player.performAction();
 		for (NPC npc : npcs.toArray(new NPC[npcs.size()])) {
 			if (npc.isDead()) {
 				npcs.remove(npc);
 				continue;
 			}
 			else ArtificialIntelligence.chooseAction(npc);
 			if ( npc.canPerformAction() )
 				npc.performAction();
 			else npc.removeCurrentAction();
 		}
 	}
 	
 	private static void advanceTime() {
 		currentTurn++;
 		processActions();
 		drawMap();
 		showStats();
 	}
 	
 	private static void handleInput() {
 		char input = GameUI.getInputChar();
 		if (KeyDefinitions.isExitChar(input))
 			exit();
 		if (KeyDefinitions.isDirectionKey(input))
 			ArtificialIntelligence
 			.chooseActionInDirection(player, Direction.getDirection(input));
 		if (input == 's')
 			advanceTime();
 	}
 	
 	private static void exit() {
 		GameUI.showAnnouncement("You're leaving the game.");
 		GameUI.exit();
 		System.exit(0);
 	}
 	
 	private static void drawMap() {
 		if (!player.isDead()) {
 			GameUI.drawMap(player.getVisibleMap());
 			return;
 		}
 	}
 	
 	private static void showStats() {
 		GameUI.showStats(player.getStats() + "Current turn: " 
 					+ currentTurn + "\n");
 	}
 
 	public static void play() {
         try {
             init();
             GameUI.showMessage("Prepare to play!");
             drawMap();
             showStats();
             while ( !player.isDead() ) {
                 handleInput();
                 if (player.hasAction())
                     advanceTime();
             }
             if (npcs.isEmpty()) GameUI.showAnnouncement("All mobs are dead!");
             if (player.isDead()) GameUI.showAnnouncement("You're dead!");
             exit();
        } catch(Exception e) {
             GameUI.exit();
            e.printStackTrace();
         }
 	}
 
 }
