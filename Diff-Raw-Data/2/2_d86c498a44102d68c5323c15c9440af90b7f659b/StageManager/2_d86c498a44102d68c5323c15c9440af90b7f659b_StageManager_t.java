 package com.jpii.navalbattle.game;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.data.RoketGamerData;
 import com.jpii.navalbattle.game.entity.AircraftCarrier;
 import com.jpii.navalbattle.game.entity.BattleShip;
 import com.jpii.navalbattle.game.entity.PortEntity;
 import com.jpii.navalbattle.game.entity.Submarine;
 import com.jpii.navalbattle.game.entity.Whale;
 import com.jpii.navalbattle.game.turn.AI;
 import com.jpii.navalbattle.game.turn.Player;
 import com.jpii.navalbattle.game.turn.PlayerManager;
 import com.jpii.navalbattle.game.turn.TurnManager;
 import com.jpii.navalbattle.gui.MainMenuWindow;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.pavo.WorldSize;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.GridHelper;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.gui.NewWindowManager;
 import com.jpii.navalbattle.util.RoketUtils;
 import com.jpii.navalbattle.util.SoundUtils;
 
 public class StageManager {
 	
 	private GameComponent game;
 	private NavalManager nm;
 	TurnManager tm;
 	int stageNumber;
 	Player persists;
 	String playerName;
 	AI ai;
 	GridHelper gh;
 	
 	public StageManager(String pn){
 		stageNumber = 0;
 		playerName = pn;
 		if(playerName.equals(""))
 			playerName = "Player 1";
 		game = newGameComponent();
 	}
 	
 	public void checkForCompletion(boolean complete,int index){		
 		if(complete) {
 			if(index==0){
 				SoundUtils.playSound(RoketUtils.class.getResourceAsStream("/com/jpii/navalbattle/res/sfx/gameover.wav"));
 				MainMenuWindow.spg.nextWindow("GameOverWindow");
 				NavalBattle.getGameState().endStage();
 				RoketUtils.submitAchievement(RoketGamerData.ACHIEVEMENT_FALLING_FROM_TREES);
 			} else {
 				SoundUtils.playSound(RoketUtils.class.getResourceAsStream("/com/jpii/navalbattle/res/sfx/nextround.wav"));
 				MainMenuWindow.spg.setNewGame();
 				NavalBattle.getGameState().endStage();
 			}
 		}
 	}
 	
 	public int getStageNumber(){ 
 		return stageNumber;
 	}
 	
 	/**
 	 * @return the GameComponent
 	 */
 	public GameComponent getGameComponent(){
 		return game;
 	}
 	
 	public GameComponent newGameComponent(){
 		stageNumber++;
 		if(game!=null) {
 			game.dispose();
 		}
 		persists = new Player(playerName);
 		ai = new AI();
 		tm = (new TurnManager(new PlayerManager(persists,ai)));
 		switch(stageNumber){
 			case 1: Game.Settings.resetSeed(0); 
 					game=new GameComponent(new NavalGame(WorldSize.WORLD_TINY,tm));
 					break;
 			case 2: Game.Settings.resetSeed(10); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL,tm));  break;
 			case 3: Game.Settings.resetSeed(15); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL,tm));  break;
 			case 4: Game.Settings.resetSeed(20); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL,tm));  break;
 			case 5: Game.Settings.resetSeed(25); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL,tm));  break;
 			default: Game.Settings.resetSeed(1000); game=new GameComponent(new NavalGame(WorldSize.WORLD_MEDIUM,tm));  break;
 		}
 		nm = game.getGame().getManager();
 		setStage();
 		return game;
 	}
 	
 	private void setStage(){
		//waitForGenerator();
 		NewWindowManager wm = nm.getWorld().getGame().getWindows();
 		
 		if(stageNumber == 20)
 			RoketUtils.submitAchievement(RoketGamerData.ACHIEVEMENT_NAVAL_SUPERIORITY);
 		if(stageNumber == 30)
 			RoketUtils.submitAchievement(RoketGamerData.ACHIEVEMENT_UNSTOPPABLE);
 		
 		switch(stageNumber){
 			case 1: 
 				addEntities(persists, 1, 0, 0, 0);
 				addEntities(ai, 1, 0, 0, 0);
 				addWhales(5);
 				Location poll = gh.getClosestLocation(new Location(0,0), 0);
 				tm.addEntity(new PortEntity(nm,poll,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT),persists);
 				poll = gh.getClosestLocation(new Location(PavoHelper.getGameHeight(nm.getWorld().getWorldSize())*2-1,PavoHelper.getGameWidth(nm.getWorld().getWorldSize())*2-1), 0);
 				tm.addEntity(new PortEntity(nm,poll,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT),ai);
 				new TutorialWindow(wm,"Stage 1: Attacking", "To attack, select your ship and press the attack button.", "Attack the enemy ship.");
 				break;
 			case 2:
 				new TutorialWindow(wm,"Stage 2: Movement", "To move, select your ship and press the move button.", "Move to ttack the enemy ship.");
 				break;
 			case 3:
 				new TutorialWindow(wm,"Stage 3: Ports", "To win a stage, take all ports or sink all ships.", "Attack the enemy port.");
 				break;
 			case 4:
 				new TutorialWindow(wm,"Stage 4: AI", "AIs will attempt to defeat you.");
 				break;
 			case 5:
 				new TutorialWindow(wm,"Stage 5: Shops", "You can buy ship upgrades in the ship shop.");
 				break;
 			case 6:
 				new TutorialWindow(wm,"Stage 6: Submarines", "Submarines are fast, lightly armored, and can submerge to avoid attacks.");
 				break;
 			case 7:
 				new TutorialWindow(wm,"Stage 7: Aircraft Carriers", "Aircraft carriers are slow, heavily armored, and have powerful airstrikes.");
 				break;
 			case 8:
 				new TutorialWindow(wm,"Stage 8: Port Shops", "You can buy ships from ports.");
 				break;
 			case 9:
 				new TutorialWindow(wm,"Stage 9: Diplomacy", "Enact diplomacy with the enemy to save your ship.", "Move your other ship for backup.");
 				break;
 			case 10:
 				new TutorialWindow(wm,"Stage 10: Battle!", "Use what you have learned to win the battle.");
 				break;
 			case 11:
 				RoketUtils.submitAchievement(RoketGamerData.ACHIEVEMENT_PROMOTION);
 				new TutorialWindow(wm,"Congratulations!","You have passed basic training. Good luck!");
 				break;
 		    default:
 			   	addEntities(persists, 3, 3, 3, 2);
 				addEntities(ai, 3, 3, 3, 2);
 				addWhales(10);
 				new TutorialWindow(wm,"You have","created a","default stage");
 				break;
 		}
 	}
 	
 	private void waitForGenerator(){
 		while(game.getGame().getManager().isGenerating())
 			;
 	}
 	
 	private void addEntities(Player p, int bss, int subs, int acs,int ports){	
 		gh = new GridHelper(0,nm);
 		boolean placed = false;
 		Location poll;
 		
 		for(int index = 0; index<bss; index++){
 			placed = false;
 			while (!placed){
 				poll = gh.pollNextWaterTile();
 				placed = true;
 				if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, poll.getRow(), poll.getCol(), 4))
 					tm.addEntity(new BattleShip(nm, poll, GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT),p);
 				else if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, poll.getRow(), poll.getCol(), 4))
 					tm.addEntity(new BattleShip(nm, poll,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM),p);
 				else
 					placed = false;
 			}
 		}
 		
 		for(int index = 0; index<subs; index++){
 			placed = false;
 			while (!placed){
 				poll = gh.pollNextWaterTile(25);
 				placed = true;
 				if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, poll.getRow(), poll.getCol(), 2))
 					tm.addEntity(new Submarine(nm, poll,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT),p);
 				else if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, poll.getRow(), poll.getCol(), 2))
 					tm.addEntity(new Submarine(nm, poll,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM),p);
 				else
 					placed = false;
 			}
 		}
 		
 		for(int index = 0; index<acs; index++){
 			placed = false;
 			while (!placed){
 				poll = gh.pollNextWaterTile(25);
 				placed = true;
 				if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, poll.getRow(), poll.getCol(), 5))
 					tm.addEntity(new AircraftCarrier(nm, poll,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT),p);
 				else if(GridHelper.canPlaceInGrid(nm,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM, poll.getRow(), poll.getCol(), 5))
 					tm.addEntity(new AircraftCarrier(nm, poll,GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM),p);
 				else
 					placed = false;
 			}
 		}
 				
 		for(int index = 0; index<ports; index++){
 			placed = false;
 			while (!placed){
 				poll = gh.pollNextShoreTile();
 				placed = true;
 				tm.addEntity(new PortEntity(nm,poll,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT),p);
 				System.out.println("Port generated at " + poll);
 			}
 		}
 	}
 	
 	private void addWhales(int whales){
 		GridHelper gh = new GridHelper(0,nm);
 		boolean placed = false;
 		Location poll;			
 		for(int index = 0; index<whales; index++){
 			placed = false;
 			while (!placed){
 				poll = gh.pollNextWaterTile();
 				while(!GridHelper.canPlaceInGrid(nm, GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT, poll.getRow(), poll.getCol(), 1)){
 					poll = gh.pollNextWaterTile();
 				}
 				placed = true;
 				new Whale(nm,poll,GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT,NavalManager.w1,NavalManager.w2,NavalManager.w3);
 			}
 		}
 	}
 	
 	public void easterEgg16(){
 		RoketUtils.submitAchievement(RoketGamerData.ACHIEVEMENT_DAVE);
 		NewWindowManager wm = nm.getWorld().getGame().getWindows();
 		new TutorialWindow(wm,"You have","summoned the","fire nation","to do battle!","Prepare to die!");
 		
 		ai.reset();
 		
 		int startRow = PavoHelper.getGameHeight(nm.getWorld().getWorldSize())+1;
 		int startCol = PavoHelper.getGameWidth(nm.getWorld().getWorldSize()) * 2-1;
 		for(int row = 3; row>0; row--){
 			for(int col = 10; col>0; col--){
 				Entity e = nm.findEntity(startRow-row,startCol-col);
 				if(e!=null){
 					nm.getGame().getTurnManager().removeEntity(e);
 					e.dispose();
 				}
 				nm.setTileOverlay(startRow-row, startCol-col, (byte)124145);
 			}
 		}
 		startRow = (PavoHelper.getGameHeight(nm.getWorld().getWorldSize())+1)/2;
 		for(int row = 3; row>0; row--){
 			for(int col = 10; col>0; col--){
 				Entity e = nm.findEntity(startRow-row,startCol-col);
 				if(e!=null){
 					nm.getGame().getTurnManager().removeEntity(e);
 					e.dispose();
 				}
 				nm.setTileOverlay(startRow-row, startCol-col, (byte)124145);
 			}
 		}
 		startRow = (PavoHelper.getGameHeight(nm.getWorld().getWorldSize())+1)/2*3;
 		for(int row = 3; row>0; row--){
 			for(int col = 10; col>0; col--){
 				Entity e = nm.findEntity(startRow-row,startCol-col);
 				if(e!=null){
 					nm.getGame().getTurnManager().removeEntity(e);
 					e.dispose();
 				}
 				nm.setTileOverlay(startRow-row, startCol-col, (byte)124145);
 			}
 		}
 		
 	}
 }
