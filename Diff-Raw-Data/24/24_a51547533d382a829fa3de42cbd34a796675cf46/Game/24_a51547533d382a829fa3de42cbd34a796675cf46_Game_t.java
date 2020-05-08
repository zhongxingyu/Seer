 package com.github.propra13.gruppe43;
 
 public class Game {
 	//Anzahl der Levels, Levels des Spiels, und der Index des momentan aktiven Levels
 	int LEVEL_COUNT = 0;
 	int currentLevelId = 0;
 	Level[] levels = new Level[10];
 	//Figur des Spielers
 	Player player;
 	
 	
 	
 	public Game(String[] lvlpath) {
 		LEVEL_COUNT = lvlpath.length;
 		player = new Player();
 		//Levels laden
 		for (int i = 0; i<LEVEL_COUNT;i++) {
 		levels[i] = new Level();
 		levels[i].init(lvlpath[i]);
 		levels[i].game = this;
 		}
 		//Eingang des ersten und Ausgang des letzten Levels entfernen
 		levels[0].entrance.type = Field.FLOOR;
 		levels[LEVEL_COUNT-1].exit.type = Field.FLOOR;
 	
		player.move(levels[0].entrance);
 		
 		
 	}
 	
 	
 	//fhrt den nchsten Schritt des Spiels aus;
 	public void gameUpdate() {
 		player.act();
 		
 	}
 	//wechselt zum Level mit der angegebenen ID
 	//target gibt an zu welchem Feld sich der Spieler bewegt: 0 = Eingang, 1 = Ausgang
 	public void changeLevel(int id, int target) {
 		currentLevelId = id;
 		if (target==0) player.move(levels[currentLevelId].entrance, false);
 		if (target==1) player.move(levels[currentLevelId].exit, false);
 	}
 
 }
