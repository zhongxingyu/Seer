 package com.testgame.mechanics.unit;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Random;
 
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.modifier.IModifier;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.graphics.Point;
 import android.util.Log;
 
 import com.testgame.mechanics.map.GameMap;
 import com.testgame.player.APlayer;
 import com.testgame.player.ComputerPlayer;
 import com.testgame.resource.ResourcesManager;
 import com.testgame.scene.GameScene;
 import com.testgame.sprite.CharacterSprite;
 import com.testgame.sprite.ProgressBar;
 import com.testgame.sprite.WalkMoveModifier;
 import com.testgame.OnlineGame;
 
 /**
  * Class which represents an abstract unit.
  */
 public class AUnit extends CharacterSprite implements IUnit {
 	
 	// --------------------------------------
 	//     Constructors & Initialization
 	// --------------------------------------
 	
 	public AUnit(float pX, float pY, ITextureRegion pTextureRegion,
 			VertexBufferObjectManager pVertexBufferObjectManager) {
 		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
 	}
 	
 	public void init() {
 		this.setPosition(this.x*this.game.tileSize, this.y*this.game.tileSize);
 		//this.initializeText(this.energy, this.currentHealth);
 		this.setOffsetCenter(0, 0);
 		this.game.attachChild(this);
 		this.game.registerTouchArea(this);
 		
 		healthBar = new ProgressBar(this.game, this.x*this.game.tileSize, this.y*this.game.tileSize, this.maxHealth);
 		healthBar.setProgressColor(1, 0, 0, .5f);
 		healthBar.setProgress(this.energy);
 		healthBar.setVisible(false);
 		game.attachChild(healthBar);
 		healthBar.setZIndex(GameScene.SQUARE_Z);
 		
 		energyBar = new ProgressBar(this.game, this.x*this.game.tileSize, this.y*this.game.tileSize, 100);
 		energyBar.setProgressColor(0, 0, 1, .5f);
 		energyBar.setProgress(this.energy);
 		energyBar.setVisible(false);
 		game.attachChild(energyBar);
 		energyBar.setZIndex(GameScene.SQUARE_Z);
 		
 		this.setZIndex(GameScene.SPRITE_Z);
 		
 		game.sortChildren();
 		
 		// TODO: make tiles within sight range visible
 	}
 	
 	// --------------------------------------
 	//      Properties 
 	// --------------------------------------
 	
 	protected int sightRange = 7; // TODO: must be bigger ? than all movement ranges
 	
 	/**
 	 * String representing unit type, such as "Jock" or "Nerd".
 	 */
 	public String unitType;
 
 	/**
 	 * The player who owns this unit.
 	 */
 	protected APlayer owner;
 	
 	/**
 	 * Map that this unit is on and that game is occurring on.
 	 */
 	protected GameMap map;
 	
 	/**
 	 * Unit's x-coordinate on the GameMap.
 	 */
 	protected int x;
 	
 	/**
 	 * Unit's y-coordinate on the GameMap.
 	 */
 	protected int y;
 	
 	/**
 	 * The maximum amount of health this unit can have.
 	 */
 	protected int maxHealth;
 	
 	/**
 	 * Unit's current health.
 	 */
 	protected int currentHealth;
 	
 	/**
 	 * Unit's attack statistic.
 	 */
 	protected int attack;
 	protected int attackEnergy;
 	protected int attackRange; // straight up radius
 	
 	/**
 	 * Unit's range statistic.
 	 */
 	protected int movementRange; 
 	
 	/**
 	 * Unit's current energy.
 	 */
 	protected int energy;
 	
 	/**
 	 * Energy the unit expended last turn.
 	 */
 	protected int energyUsedLastTurn;
 	
 	/**
 	 * Whether the unit is currently defending.
 	 */
 	protected boolean isDefending;
 	
 	/**
 	 * Random number generator.
 	 */
 	protected Random rand;
 	
 	// -----------------------------------------
 	//     Getter & Setters
 	// -----------------------------------------
 	
 	@Override
 	public void setPlayer(APlayer player) {
 		owner = player;
 		this.player = player;
 	}
 	
 	public APlayer getPlayer() {
 		return this.player;
 	}
 	
 	@Override
 	public int getMapX() {
 		return x;
 	}
 	
 	public int getMapY() {
 		return y;
 	}
 	
 	public int getMaxHealth() {
 		return maxHealth;
 	}
 
 	@Override
 	public int getHealth() {
 		return currentHealth;
 	}
 
 	@Override
 	public int getAttack() {
 		return attack;
 	}
 	
 	public int getAttackRange() {
 		return attackRange;
 	}
 	
 	public int getAttackCost() {
 		return attackEnergy;
 	}
 
 	@Override
 	public int getRange() {
 		return movementRange;
 	}
 
 	@Override
 	public int getEnergy() {
 		return energy;
 	}
 	
 	@Override
 	public String toString() {
 		return this.owner.getName() +"'s "+this.unitType;
 	}
 	
 	public void setEnergy(int energy){
 		int diff = energy - this.energy;  // positive if regaining, negative if losing
 		this.energy = energy;
 		//this.setText(this.energy, this.currentHealth);
 		//animatePoints(diff, "blue"); // recharging energy;
 		//this.setAlpha(this.energy / 100 + .1f);
 		this.energyBar.setProgress(this.energy);
 	}
 	
 	public String getType(){
 		return this.unitType;
 	}
 
 	// --------------------------------------
 	//          Game Code
 	// --------------------------------------
 	
 	public void computerMove(int xNew, int yNew, final int energy, final ComputerPlayer player){
 		
 		Log.d("Moving", "In computer move method");
		//Log.d("xOld", x+"");
		//Log.d("yOld", y+"");
		//Log.d("xNew", xNew+"");
		//Log.d("yNew", yNew+"");
 		ArrayList<Point> path = map.computePath(new Point(x, y), new Point(xNew, yNew));
 		map.setUnoccupied(x, y);
 		this.x = xNew;
 		this.y = yNew;
 		map.setOccupied(x, y, this);
 		this.reduceEnergy(energy);
 
 		Log.d("Path", path.toString());
 
 		int destX = this.game.getTileSceneX(xNew, yNew);
 		int destY = this.game.getTileSceneY(xNew, yNew);
 		
 		energyBar.setPosition(destX, destY);
 		healthBar.setPosition(destX, destY);
 		
 		//ArrayList<Point> path = map.computePath(new Point(oldX, oldY), new Point(xNew, yNew));
 		
 		walkAnimateAlongPath(path, true, energy);
 
 
 		/*
 		int destX = this.game.getTileSceneX(xNew, yNew);
 		int destY = this.game.getTileSceneY(xNew, yNew);
 		
 		float timePerTile = .2f; 
 		float numTilesX = Math.abs(this.getX() - destX) / game.tileSize;
 		float numTilesY = Math.abs(this.getY() - destY) / game.tileSize;
 		
 		
 		WalkMoveModifier one = new WalkMoveModifier(timePerTile*numTilesX + .1f, this.getX(), this.getY(), destX, this.getY(), true);
 		WalkMoveModifier two = new WalkMoveModifier(timePerTile*numTilesY + .1f, destX, this.getY(), destX, destY, false);
 				
 		SequenceEntityModifier seq = new SequenceEntityModifier(new IEntityModifierListener() {
 			@Override
 			public void onModifierStarted(IModifier<IEntity> pModifier,
 					IEntity pItem) {
 				game.animating = true;
 				game.camera.setChaseEntity(pItem);
 				ResourcesManager.getInstance().walking_sound.play();
 				
 			}
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 				game.animating = false;
 				game.camera.setChaseEntity(null);
 				ResourcesManager.getInstance().walking_sound.pause();
 				((AUnit)pItem).setCurrentTileIndex(((AUnit)pItem).start_frame);
 				game.setEventText("Moved using "+energy+" energy.");
 				player.performNext(); // finished this action, call next
 			}
 		}, one, two);
 		
 		//this.clearEntityModifiers();
 		this.registerEntityModifier(seq);
 		
 		*/
 	}
 	
 	@Override
 	public void move(int xNew, int yNew, ArrayList<Point> path, int cost) {
 			cost = this.movementRange * cost;
 			map.setUnoccupied(this.x, this.y);
 			int origX = this.x;
 			int origY = this.y;
 			this.x = xNew;
 			this.y = yNew;
 			map.setOccupied(x, y, this);
 			this.reduceEnergy(cost);
 			//this.energyUsedLastTurn += eCost;
 			// TODO: code to actually move the sprite on the map
 			
 			
 			
 			int destX = this.game.getTileSceneX(xNew, yNew);
 			int destY = this.game.getTileSceneY(xNew, yNew);
 			
 			energyBar.setPosition(destX, destY);
 			healthBar.setPosition(destX, destY);
 			
 			JSONObject temp = new JSONObject();
 			
 			try {
 				temp.put("MoveType", "MOVE");
 				temp.put("DestX", xNew);
 				temp.put("DestY", yNew);
 				temp.put("UnitX", origX);
 				temp.put("UnitY", origY);
 				temp.put("Energy", cost);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			if(!this.game.resourcesManager.isLocal)
 				((OnlineGame)this.game.getGame()).addMove(temp);
 
 			
 			walkAnimateAlongPath(path, false, cost);
 			
 			/*
 			float timePerTile = .2f; 
 			float numTilesX = Math.abs(this.getX() - destX) / game.tileSize;
 			float numTilesY = Math.abs(this.getY() - destY) / game.tileSize;
 			
 			
 			
 			WalkMoveModifier one = new WalkMoveModifier(timePerTile*numTilesX + .1f, this.getX(), this.getY(), destX, this.getY(), true);
 			WalkMoveModifier two = new WalkMoveModifier(timePerTile*numTilesY + .1f, destX, this.getY(), destX, destY, false);
 			
 			SequenceEntityModifier seq = new SequenceEntityModifier(game.animationListener, one, two);
 			
 			this.clearEntityModifiers();
 			
 			this.registerEntityModifier(seq);
 			
 			this.game.setEventText("Moved using "+eCost+" energy.");*/
 
         	
 	}
 
 	public void computerAttack(AUnit unit, int attack, int energy, ComputerPlayer player){
 		this.reduceEnergy(energy);
 
 		unit.attackedAnimate(player, unit, attack);
 		
 		if (unit.getHealth() > 0) this.game.setEventText("Did "+this.attack+" damage!\n Unit health "+unit.getHealth()+"/"+unit.getMaxHealth());
 		
 	}
 	
 	@Override
 	public void attack(final AUnit unit) {
 		int dist = this.manhattanDistance(this.x, this.y, unit.getMapX(), unit.getMapY());
 		if(dist <= this.attackRange && this.attackEnergy <= this.energy){
 			rand = new Random(System.currentTimeMillis()); // new rng with random seed
 			int realAttack = this.attack + ((int) (0.15*this.attack*rand.nextGaussian())); // randomize attack
 			this.reduceEnergy(this.attackEnergy);
 			
 			JSONObject temp = new JSONObject();
 			try {
 				temp.put("MoveType", "ATTACK");
 				temp.put("UnitX", this.x);
 				temp.put("UnitY", this.y);
 				temp.put("Energy", this.attackEnergy);
 				temp.put("OppX", unit.x);
 				temp.put("OppY", unit.y);
 				temp.put("Attack", realAttack);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if(!this.game.resourcesManager.isLocal)
 				((OnlineGame)this.game.getGame()).addMove(temp);
 			
 			unit.attackedAnimate(null, unit, realAttack);
 			
 			
 			if (unit.getHealth() > 0) this.game.setEventText("Did "+realAttack+" damage!\n Enemy health "+unit.getHealth()+"/"+unit.getMaxHealth());
 		}
 		else {
 			this.game.setEventText(this.toString() + " cannot attack,\n not enough energy!");
 		}
 		
 		this.game.deselectCharacter(true);
 	}
 	
 	@Override
 	public void defend() {
 		this.isDefending = true;
 	}
 
 	@Override
 	public boolean isDefending() {
 		return isDefending;
 	}
 
 	@Override
 	public void reduceHealth(int health) {
 		int dec = health;
 		if (this.isDefending)
 			dec /= 2;
 		this.currentHealth -= dec; 
 		if(this.currentHealth <= 0){
 			owner.removeUnit(this);
 			map.setUnoccupied(this.x, this.y);
 			final AUnit u = this;
 			game.engine.runOnUpdateThread(new Runnable() {
 				@Override
 				public void run() {
 					game.detachChild(u);
 					game.unregisterTouchArea(u);
 				}
 			});
 
 			this.game.setEventText(this.toString() + " died!");
 		}
 		
 		animatePoints(-dec, "red");
 		//this.setText(this.energy, this.currentHealth);
 		this.healthBar.setProgress(this.currentHealth);
 	}
 	
 	@Override
 	public void restoreEnergy(int energy) {
 		this.energy += energy;
 		if (this.energy > 100) this.energy = 100;
 		//this.setText(this.energy, this.currentHealth);
 		animatePoints(energy, "blue"); 
 		//this.setAlpha(this.energy / 100 + .1f);
 		this.energyBar.setProgress(this.energy);
 	}
 
 	@Override
 	public void reduceEnergy(int energy) {
 		this.energy -= energy;
 		//this.setText(this.energy, this.currentHealth);
 		//animatePoints(-energy, "blue");
 		this.energyBar.setProgress(this.energy);
 	}
 	
 	@Override
 	public void turnInit() {
 		if(this.energy >= 50)
 			this.setEnergy(100);
 		else if(this.energy >= 25){
 			this.restoreEnergy(50);
 		}
 		else
 			this.restoreEnergy(+25);
 		this.isDefending = false;
 	}
 	
 	/**
 	 * Calculates all of the available moves for this unit.
 	 * @return list of Points on map that are possible moves
 	 */
 	public ArrayList<Point> availableMoves() {
 
 		if (unitType.equals("Base")) return new ArrayList<Point>();
 		
 		HashSet<Point> moves = map.bfs(new Point(x , y), energy / movementRange);
 		
 		ArrayList<Point> result = new ArrayList<Point>();
 		result.addAll(moves);
 		return result;
 
 	}
 	
 	/**
 	 * Calculates all possible targets for this unit.
 	 * @return list of units this unit can attack
 	 */
 	public ArrayList<AUnit> availableTargets() {
 		if (unitType.equals("Base")) return new ArrayList<AUnit>();
 		if(this.energy < this.attackEnergy) return new ArrayList<AUnit>();	
 		HashSet<AUnit> moves = map.bfsTarget(new Point(x , y), attackRange, player);
 		ArrayList<AUnit> result = new ArrayList<AUnit>();
 		result.addAll(moves);
 		return result;
 
 	}
 
 	/**
 	 * Utility method for calculating Manhattan distance.
 	 * @param x1 The x-coordinate of the first location.
 	 * @param y1 The y-coordinate of the first location.
 	 * @param x2 The x-coordinate of the second location.
 	 * @param y2 The y-coordinate of the second location.
 	 */
 	public int manhattanDistance(int x1, int y1, int x2, int y2) {
 		return Math.abs(x1-x2) + Math.abs(y1-y2);
 	}
 	
 	// ------------------------------------------
 	//      Energy & Health Modes Code
 	// ------------------------------------------
 	
 	public void switchMode(int newMode) {
 		switch(newMode) {
 			case (GameScene.SPRITE_MODE):
 				//this.setVisible(true);
 				healthBar.setVisible(false);
 				energyBar.setVisible(false);
 				break;
 			case (GameScene.HEALTH_MODE):
 				//this.setVisible(true);
 				healthBar.setVisible(true);
 				energyBar.setVisible(false);
 				break;
 			case (GameScene.ENERGY_MODE):
 				//this.setVisible(false);
 				healthBar.setVisible(false);
 				energyBar.setVisible(true);
 				break;
 			default:
 				break;
 			
 		}		
 	}
 	
 	// ------------------------------------------
 	//      Animation Code
 	// ------------------------------------------
 	
 	/**
 	 * Default texture frame for this unit.
 	 */
 	public int start_frame = 0;
 	
 	/** 
 	 * Start and end frames for idling.
 	 */
 	protected int IDLE_START_FRAME;
 	protected int IDLE_END_FRAME;
 	
 	/** 
 	 * Start and end frames for walking to the right.
 	 */
 	protected int WALK_RIGHT_START_FRAME;
 	protected int WALK_RIGHT_END_FRAME;
 	
 	/**
 	 * Start and end frames for walking to the left.
 	 */
 	protected int WALK_LEFT_START_FRAME;
 	protected int WALK_LEFT_END_FRAME;
 	
 	/**
 	 * Start and end frames for walking up.
 	 */
 	protected int WALK_UP_START_FRAME;
 	protected int WALK_UP_END_FRAME;
 	
 	/**
 	 * Start and end frames for walking down.
 	 */
 	protected int WALK_DOWN_START_FRAME;
 	protected int WALK_DOWN_END_FRAME;
 	
 	/**
 	 * Frame for guarding.
 	 */
 	protected int GUARD_FRAME;
 	
 	/**
 	 * Start and end frames for being attacked.
 	 */
 	protected int ATTACKED_START_FRAME;
 	protected int ATTACKED_END_FRAME;
 	
 	/**
 	 * Animates this unit being idle.
 	 */
 	public void idleAnimate() {
 		if(this.getType().equals("Base"))
 			return;
 		this.animate(new long[] { 100, 100 }, start_frame + IDLE_START_FRAME, start_frame + IDLE_END_FRAME, true);
 	}
 	
 	/**
 	 * Animates this unit walking
 	 * @param xDirection indicates left of right (1 or -1)
 	 * @param yDirection indicates up or down (1 or -1)
 	 */
 	public void walkAnimate(int xDirection, int yDirection) {
 		if(this.getType().equals("Base"))
 			return;
 
 		if (xDirection == 0) { // walking up or down
 			if (yDirection > 0) { // walking up
 				this.animate(new long[] { 100, 100, 100 }, start_frame + WALK_UP_START_FRAME, start_frame + WALK_UP_END_FRAME, true);
 			} else {
 				this.animate(new long[] { 100, 100, 100 }, start_frame + WALK_DOWN_START_FRAME, start_frame + WALK_DOWN_END_FRAME, true);
 			}
 		} else { // walking right or left
 			if (xDirection > 0) { // walking right
 				this.animate(new long[] { 100, 100, 100 }, start_frame + WALK_RIGHT_START_FRAME, start_frame + WALK_RIGHT_END_FRAME, true);
 			} else { // walking left
 				this.animate(new long[] { 100, 100, 100 }, start_frame + WALK_LEFT_START_FRAME, start_frame + WALK_LEFT_END_FRAME, true);
 			}
 		}
 	}
 	
 	/**
 	 * Animates this unit guarding.
 	 */
 	public void guardAnimate() {
 		if(this.getType().equals("Base"))
 			return;
 		this.setCurrentTileIndex(start_frame + GUARD_FRAME);
 	}
 	
 	/**
 	 * Animates this unit being attacked.
 	 * @param computerPlayer null if local, player if online
 	 * @param unit who attacked this unit
 	 * @param attack cost in energy
 	 */
 	public void attackedAnimate(final ComputerPlayer computerPlayer, final AUnit unit, final int attack) {
 		if(this.getType().equals("Base")){
 			unit.reduceHealth(attack);
 			if(game.resourcesManager.isLocal){
 				game.getGame().endGame();
 			}
 			game.working = false;
 			if(computerPlayer != null){
 				computerPlayer.performNext();
 			}
 			return;
 		}
 		ResourcesManager.getInstance().attack_sound.play();
 		this.animate(new long[] { 100, 100 }, start_frame + ATTACKED_START_FRAME, start_frame + ATTACKED_END_FRAME, true);
 		
 		final AUnit u = this;
 		if (computerPlayer != null) {
 			this.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() 
 	        {
 	            public void onTimePassed(final TimerHandler pTimerHandler) 
 	            {
 	                u.stopAnimation();
 	                u.setCurrentTileIndex(start_frame);
 	                unit.reduceHealth(attack);
 	                computerPlayer.performNext();
 	                game.working = false;
 	                
 	            }
 	        }));
 		} else {
 			this.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() 
 	        {
 	            public void onTimePassed(final TimerHandler pTimerHandler) 
 	            {
 	                u.stopAnimation();
 	                
 	                u.setCurrentTileIndex(start_frame);
 	                unit.reduceHealth(attack);
 	                game.working = false;
 	    			game.getGame().endGame();
 	            }
 	        }));
 		}
 	}
 	
 	/**
 	 * Animates this unit to walk along a given path.
 	 * @param path to walk along
 	 * @param computer boolean to indicate local versus computer player
 	 * @param cost energy used to walk on this path
 	 */
 	public void walkAnimateAlongPath(ArrayList<Point> path, boolean computer, final int cost) {
 		
 		IEntityModifierListener animationListener;
 		
		Log.d("AndEngine", "Creating walk animation...");
		
 		if (computer) {
 			animationListener = new IEntityModifierListener() {
 				@Override
 				public void onModifierStarted(IModifier<IEntity> pModifier,
 						IEntity pItem) {
 					game.animating = true;
 					game.camera.setChaseEntity(pItem);
 					ResourcesManager.getInstance().walking_sound.play();
					Log.d("AndEngine", "Started animating walk...");
 					
 				}
 				@Override
 				public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 					game.animating = false;
 					game.camera.setChaseEntity(null);
 					ResourcesManager.getInstance().walking_sound.pause();
 					((AUnit)pItem).setCurrentTileIndex(((AUnit)pItem).start_frame);
 					game.setEventText("Moved using "+cost+" energy.");
 					
 					((AUnit)pItem).animatePoints(-cost, "blue");
					Log.d("AndEngine", "Finished animating walk, calling perform next!");
 					((ComputerPlayer)player).performNext();
 				}
 			};
 		} else {
 			animationListener = new IEntityModifierListener() {
 				@Override
 				public void onModifierStarted(IModifier<IEntity> pModifier,
 						IEntity pItem) {
 					Log.d("AndEngine", "animation modifier started.");
 					game.animating = true;
 					game.camera.setChaseEntity(pItem);
 					Log.d("AndEngine", "position is now "+pItem.getX()+", "+pItem.getY());
 					game.resourcesManager.walking_sound.play();
 				}
 				@Override
 				public void onModifierFinished(IModifier<IEntity> pModifier,
 						IEntity pItem) {
 					Log.d("AndEngine", "animation modifier ended.");
 					game.animating = false;
 					game.camera.setChaseEntity(null);
 					((AUnit)pItem).setCurrentTileIndex(((AUnit)pItem).start_frame);
 					((AUnit)pItem).animatePoints(-cost, "blue");
 					Log.d("AndEngine", "position is now "+pItem.getX()+", "+pItem.getY());
 					game.resourcesManager.walking_sound.pause();
 				}
 			};
 		}
 		
 		WalkMoveModifier[] walks = new WalkMoveModifier[path.size() - 1];
 		
 		for (int i = 0; i < path.size() - 1; i++) {
 			
 			Point a = path.get(i);
 			Point b = path.get(i+1);
 			
 			int length = GameMap.manhattanDistance(a, b);
 			
 			boolean horiz = Math.abs(a.y - b.y) == 0 ? true : false;
 			
 			walks[i] = new WalkMoveModifier(length * .2f, a.x*game.tileSize, a.y*game.tileSize, b.x*game.tileSize, b.y*game.tileSize, horiz);
 			
 		}
 		
 		SequenceEntityModifier seq = new SequenceEntityModifier(animationListener, walks);
 		
		//clearEntityModifiers();
 		
 		registerEntityModifier(seq);
 		
 	}
 }
