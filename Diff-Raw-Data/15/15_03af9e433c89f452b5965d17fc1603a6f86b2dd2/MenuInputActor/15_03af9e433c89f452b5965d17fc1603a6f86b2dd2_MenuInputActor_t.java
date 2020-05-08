 package com.avona.games.towerdefence.inputActors;
 
 import com.avona.games.towerdefence.Game;
 import com.avona.games.towerdefence.V2;
 
 public class MenuInputActor extends EmptyInputActor {
 	private Game game;
 
 	public MenuInputActor(Game game) {
 		this.game = game;
 	}
 
 	@Override
 	public void mouseBtn1DownAt(V2 location) {
 		// Util.log("menu clicked at raw: " + location.toString());
 		final int height = 480; // FIXME get this from somewhere
 		final int buttonCount = 4;
 		location.y = height - location.y;
 		final int btn = (int) (Math.floor(buttonCount * location.y / height));
 		// Util.log("menu clicked at: " + location.toString() + " that is btn#"
 		// + btn);
 
 		switch (btn) {
 		case 0:
 		case 1:
 		case 2:
 			game.selectedBuildTower = game.level.buildableTowers[btn];
 			break;
		default: // FIXME code dupe to GameInputRightClick...
 			if (game.level.completed) {
 				game.loadNextLevel();
 			}
 			if (!game.level.completed) {
 				game.startWave();
 			}
 			break;
 		}
 	}
 
 }
