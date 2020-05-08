 package com.avona.games.towerdefence.inputActors;
 
 import com.avona.games.towerdefence.Game;
 import com.avona.games.towerdefence.Mouse;
 import com.avona.games.towerdefence.V2;
 import com.avona.games.towerdefence.enemy.Enemy;
 import com.avona.games.towerdefence.tower.Tower;
 
 public class GameInputActor extends EmptyInputActor {
 	private Game game;
 	private Mouse mouse;
 
 	public GameInputActor(Game game, Mouse mouse) {
 		this.game = game;
 		this.mouse = mouse;
 	}
 
 	@Override
 	public void mouseBtn1DownAt(V2 location) {
 		if (game.selectedBuildTower == null)
 			return;
 		game.draggingTower = true;
 	}
 
 	@Override
 	public void mouseBtn1UpAt(V2 location) {
 		if (!game.draggingTower)
 			return;
 		game.draggingTower = false;
 
		Tower t = game.getTowerWithinRadius(location, mouse.radius);
		if (t == null) {
			if (game.canBuildTowerAt(location)) {
				game.addTowerAt(location);
			}
 		}
 		checkMouseOverTower(location);
 	}
 
 	@Override
 	public void mouseBtn2DownAt(V2 location) {
 		game.startWave();
 	}
 
 	@Override
 	public void mouseExited() {
 		game.draggingTower = false;
 	}
 
 	public void checkMouseOverTower(V2 location) {
 		final Tower t = game.getTowerWithinRadius(location, mouse.radius);
 		if (t != null) {
 			game.selectedObject = t;
 			return;
 		}
 		final Enemy e = game.getEnemyWithinRadius(location, mouse.radius);
 		game.selectedObject = e;
 	}
 
 	@Override
 	public void mouseMovedTo(V2 location) {
 		checkMouseOverTower(location);
 	}
 }
