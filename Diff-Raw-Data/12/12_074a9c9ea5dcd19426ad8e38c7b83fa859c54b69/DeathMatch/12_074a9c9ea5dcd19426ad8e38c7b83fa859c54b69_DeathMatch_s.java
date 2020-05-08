 package match;
 
 import physics.CattoPhysicsEngine;
 import server.Server;
 import server.ServerGameState;
 import jig.engine.hli.ScrollingScreenGame;
 import jig.engine.util.Vector2D;
 import world.LevelMap;
 import world.LevelSet;
 import world.PlayerObject;
 
 //# of kills
 //# of deaths
 //a timer
 //where to spawn, etc.
 public class DeathMatch extends Match {
 	
 	public DeathMatch(LevelSet l) {
 		super(l);
 	}
 	
 	@Override
 	public void loadNextLevel() {
 		// clean out all the objects first
 		Server.getServer().clear();
 		
 		// get the next level number
 		curLevel++;
 		if (curLevel > levels.getNumLevels()-1) curLevel = 0;
 		
 		// load current level
 		LevelMap level = levels.getThisLevel(curLevel);
 		if (level == null) {
 			System.err.println("Error: Level wasn't correctly loaded.\n");
 			System.exit(1);
 		}
 		level.buildLevel();
 		
 		// re-add players
 		for (PlayerObject po : players) {
 			ServerGameState.getGameState().getLayer().add(po);
 		}
 	}
 	
	public void loadLevel(int levelNum) {
		// clean out all the objects first
		Server.getServer().clear();
		
 		// get the next level number
 		curLevel = levelNum;
 		
 		// load current level
 		LevelMap level = levels.getThisLevel(curLevel);
 		if (level == null) {
 			System.err.println("Error: Level wasn't correctly loaded.\n");
 			System.exit(1);
 		}
 		level.buildLevel();
 	}
 	
 	@Override
 	public void startMatch() {
 		// load the level
 		//loadNextLevel(); //TODO: fix if time
 		
 		// reset all scores
 		for(PlayerObject p: players) {
 			p.setActivation(false);
 			p.setHealth(0); // this will be zero until we spawn
 			p.clearKills();
 			p.clearDeaths();
 		}		
 		
 		// start timer
 		startTime = ServerGameState.getGameState().totalMs;
 		
 		// notify players to spawn
 		
 	}
 
 	@Override
 	public void endMatch() {
 		// disable input?
 		
 		// display scores
 		
 		// start timer
 		
 	}
 	
 	@Override
 	public void addPlayer(PlayerObject p) {
 		players.add(p);
 		//p.setActivation(false);
 		p.setHealth(0); // this will be zero until we spawn
 		p.clearKills();
 		p.clearDeaths();
 	}
 	
 	@Override
 	public void removePlayer(PlayerObject p) {
 		players.remove(p);
 	}
 	
 	@Override
 	public void spawnPlayer(PlayerObject p, Vector2D loc) {
 		p.setSpawn(0);
 		p.setActivation(true);
 		p.setCenterPosition(loc);
 		p.setHealth(PlayerObject.MAXHEALTH);
 	}
 }
