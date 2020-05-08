 package com.testgame.mechanics.unit;
 
 import java.util.ArrayList;
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
  * @author Alen Lukic
  *
  */
 public class AUnit extends CharacterSprite implements IUnit {
 	
 	public AUnit(float pX, float pY, ITextureRegion pTextureRegion,
 			VertexBufferObjectManager pVertexBufferObjectManager) {
 		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
 	}
 	
 	public int start_frame = 0;
 	
 	protected int IDLE_START_FRAME;
 	protected int IDLE_END_FRAME;
 	
 	protected int WALK_RIGHT_START_FRAME;
 	protected int WALK_RIGHT_END_FRAME;
 	
 	protected int WALK_LEFT_START_FRAME;
 	protected int WALK_LEFT_END_FRAME;
 	
 	protected int WALK_UP_START_FRAME;
 	protected int WALK_UP_END_FRAME;
 	
 	protected int WALK_DOWN_START_FRAME;
 	protected int WALK_DOWN_END_FRAME;
 	
 	protected int GUARD_FRAME;
 	
 	protected int ATTACKED_START_FRAME;
 	protected int ATTACKED_END_FRAME;
 	
 	protected int sightRange = 7; // TODO: must be bigger ? than all movement ranges
 	
 	protected String unitType;
 
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
 	 * Unit's attack stat.
 	 */
 	protected int attack;
 	protected int attackenergy;
 	protected int attackrange; // straight up radius
 	
 	/**
 	 * Unit's range stat.
 	 */
 	protected int range; // ratio b/w energy
 	
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
 		return attackrange;
 	}
 	
 	public int getAttackCost() {
 		return attackenergy;
 	}
 
 	@Override
 	public int getRange() {
 		return range;
 	}
 
 	@Override
 	public int getEnergy() {
 		return energy;
 	}
 	
 	public void ComputerMove(int xNew, int yNew, final int energy, final ComputerPlayer player){
 		map.setUnoccupied(x, y);
 		this.x = xNew;
 		this.y = yNew;
 		map.setOccupied(x, y, this);
 		this.reduceEnergy(energy);
 		int destX = this.game.getTileSceneX(xNew, yNew);
 		int destY = this.game.getTileSceneY(xNew, yNew);
 		
 		float timePerTile = .2f; 
 		float numTilesX = Math.abs(this.getX() - destX) / game.tileSize;
 		float numTilesY = Math.abs(this.getY() - destY) / game.tileSize;
 		
 		
 		energyBar.setPosition(destX, destY);
 		healthBar.setPosition(destX, destY);
 		
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
 	}
 	
 	@Override
 	public void move(int xNew, int yNew) {
 		int dist = manhattanDistance(this.x, this.y, xNew, yNew);
 		final int eCost = dist*this.range; // Energy expense of this move 
 		if (eCost <= this.energy) {
 			map.setUnoccupied(this.x, this.y);
 			int origX = this.x;
 			int origY = this.y;
 			this.x = xNew;
 			this.y = yNew;
 			map.setOccupied(x, y, this);
 			this.reduceEnergy(eCost);
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
 				temp.put("Energy", eCost);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			if(!this.game.resourcesManager.isLocal)
 				((OnlineGame)this.game.getGame()).addMove(temp);
 
 			
 			
 			float timePerTile = .2f; 
 			float numTilesX = Math.abs(this.getX() - destX) / game.tileSize;
 			float numTilesY = Math.abs(this.getY() - destY) / game.tileSize;
 			
 			
 			
 			WalkMoveModifier one = new WalkMoveModifier(timePerTile*numTilesX + .1f, this.getX(), this.getY(), destX, this.getY(), true);
 			WalkMoveModifier two = new WalkMoveModifier(timePerTile*numTilesY + .1f, destX, this.getY(), destX, destY, false);
 			
 			SequenceEntityModifier seq = new SequenceEntityModifier(game.animationListener, one, two);
 			
 			this.clearEntityModifiers();
 			
 			this.registerEntityModifier(seq);
 			
 			this.game.setEventText("Moved using "+eCost+" energy.");
 
         	
         	
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return this.owner.getName() +"'s "+this.unitType;
 	}
 
 	
 	public void ComputerAttack(AUnit unit, int attack, int energy, ComputerPlayer player){
 		this.reduceEnergy(energy);
 		
 		
 		unit.attackedAnimate(player, unit, attack);
 		
 		if (unit.getHealth() > 0) this.game.setEventText("Did "+this.attack+" damage!\n Unit health "+unit.getHealth()+"/"+unit.getMaxHealth());
 		
 	}
 	
 	// Which order does this go? THIS is being attacked or unit is being attacked ?? 
 	@Override
 	public void attack(final AUnit unit) {
 		int dist = this.manhattanDistance(this.x, this.y, unit.getMapX(), unit.getMapY());
 		if(dist <= this.attackrange && this.attackenergy <= this.energy){
 			rand = new Random(System.currentTimeMillis()); // new rng with random seed
 			int realAttack = this.attack + ((int) (0.15*this.attack*rand.nextGaussian())); // randomize attack
 			this.reduceEnergy(this.attackenergy);
 			
 			JSONObject temp = new JSONObject();
 			try {
 				temp.put("MoveType", "ATTACK");
 				temp.put("UnitX", this.x);
 				temp.put("UnitY", this.y);
 				temp.put("Energy", this.attackenergy);
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
 
 	public void setEnergy(int energy){
 		int diff = energy - this.energy;  // positive if regaining, negative if losing
 		this.energy = energy;
 		//this.setText(this.energy, this.currentHealth);
 		animatePoints(diff, "blue"); // recharging energy;
 		//this.setAlpha(this.energy / 100 + .1f);
 		this.energyBar.setProgress(this.energy);
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
 		animatePoints(-energy, "blue");
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
 	
 	/*
 	 * checks a point x,y
 	 */
 	public void checkPointForMove(int offsetX, int offsetY, int range, ArrayList<Point> moves) {
 
 		if (this.x + offsetX < 0 || this.y + offsetY < 0) return; // off of map, not occupied
 		if (this.x + offsetX >= map.xDim || this.y + offsetY >= map.yDim) return; // ditto
 		
 		AUnit occupyingUnit = map.getOccupyingUnit(this.x + offsetX, this.y + offsetY);
 		
 		Point s = new Point(this.x, this.y);
 		Point d = new Point(this.x + offsetX, this.y + offsetY);
 		Point p = new Point(offsetX, offsetY);
 		
 		if (occupyingUnit == null) { // not occupied
 			if (moves.contains(p)) return;
 			int dist = map.manhattanDistance(s, d);
 			if (dist > range) return;
 			else moves.add(p);
 		} 
 	}
 	
 	// all the squares you can move to 
 	public ArrayList<Point> availableMoves() {
 		ArrayList<Point> moves = new ArrayList<Point>();
 		if(this.range == 0)
 			return moves;
 		int movementRange = this.energy/this.range;
 		
 		for (int i = 0; i <= movementRange; i++) {
 			for (int j = 0; j <= movementRange - i; j++){
 				
 				if (i == 0 && j == 0) continue;
 				
 				checkPointForMove(i, j, movementRange, moves);
 				checkPointForMove(i, -j, movementRange, moves);
 				checkPointForMove(-i, j, movementRange, moves);
 				checkPointForMove(-i, -j, movementRange, moves);
 			}
 		}
 
 		return moves;
 	}
 	
 	// all the squares of enemies you can attack
 	public ArrayList<AUnit> availableTargets() {
 		ArrayList<AUnit> targets = new ArrayList<AUnit>();
 		
 		int attackRange = this.attackrange;
 		if (this.attackenergy > this.energy) attackRange = 0; // no available moves b/c no energy
 		
 		for (int i = 0; i <= attackRange; i++) {
 			for (int j = 0; j <= attackRange - i; j++){ // check all the points at most attackrange distance away
 				
 				if (i == 0 && j == 0) continue; // no movement, not a move
 				
 				checkPointForTargets(i, j, attackRange, targets);
 				checkPointForTargets(i, -j, attackRange, targets);
 				checkPointForTargets(-i, j, attackRange, targets);
 				checkPointForTargets(-i, -j, attackRange, targets);
 			}
 		}
 		return targets;
 	}
 	
 	// checks to see if a square offsetX, offsetY distance from this unit has an ENEMY unit, if so adds it to the array
 	private void checkPointForTargets(int offsetX, int offsetY, int attackRange, ArrayList<AUnit> targets) {
 		
 		if (this.x + offsetX < 0 || this.y + offsetY < 0) return; // off of map, not occupied
 		if (this.x + offsetX >= map.xDim || this.y + offsetY >= map.yDim) return; // ditto
		if (map.manhattanDistance(new Point(this.x, this.y), new Point(this.x + offsetX, this.y + offsetY)) > attackRange)
 			return;
 		AUnit occupyingUnit = map.getOccupyingUnit(this.x + offsetX, this.y + offsetY);
 		
 		if (occupyingUnit != null) { // occupied
 			if (occupyingUnit.unitType.equals("Dummy")) return; // not truly a target, just a dummy obstacle
 			if (targets.contains(occupyingUnit)) return;
 			else {
 				if (occupyingUnit.getPlayer() == this.player) return; // same player, not an enemy
 				occupyingUnit.inSelectedCharactersAttackRange = true;
 				targets.add(occupyingUnit);
 			}
 		}
 	}
 
 	/**
 	 * Utility method for calculating Manhattan distance.
 	 * @param x1 The x-coordinate of the first location.
 	 * @param y1 The y-coordinate of the first location.
 	 * @param x2 The x-coordinate of the second location.
 	 * @param y2 The y-coordinate of the second location.
 	 */
 	public int manhattanDistance(int x1, int y1, int x2, int y2) {
		return map.manhattanDistance(new Point(x1, y1), new Point(x2, y2));
 	}
 	
 	public void init() {
 		this.setPosition(this.x*this.game.tileSize, this.y*this.game.tileSize);
 		//this.initializeText(this.energy, this.currentHealth);
 		this.setOffsetCenter(0, 0);
 		this.game.attachChild(this);
 		this.game.registerTouchArea(this);
 		
 		healthBar = new ProgressBar(this.game, this.x*this.game.tileSize, this.y*this.game.tileSize, this.maxHealth);
 		healthBar.setProgressColor(1, 0, 0, .7f);
 		healthBar.setProgress(this.energy);
 		healthBar.setVisible(false);
 		game.attachChild(healthBar);
 		
 		energyBar = new ProgressBar(this.game, this.x*this.game.tileSize, this.y*this.game.tileSize, 100);
 		energyBar.setProgressColor(0, 0, 1, .7f);
 		energyBar.setProgress(this.energy);
 		energyBar.setVisible(false);
 		game.attachChild(energyBar);
 		
 		// TODO: make tiles within sight range visible
 	}
 	
 	public void idleAnimate() {
 		if(this.getType().equals("Base"))
 			return;
 		this.animate(new long[] { 100, 100 }, start_frame + IDLE_START_FRAME, start_frame + IDLE_END_FRAME, true);
 	}
 	
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
 	
 	public void guardAnimate() {
 		if(this.getType().equals("Base"))
 			return;
 		this.setCurrentTileIndex(start_frame + GUARD_FRAME);
 	}
 	
 	public void attackedAnimate(final ComputerPlayer computerPlayer, final AUnit unit, final int attack) {
 		if(this.getType().equals("Base")){
 			unit.reduceHealth(attack);
 			game.getGame().endGame();
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
 
 	
 	public void switchMode(int newMode) {
 		switch(newMode) {
 			case (GameScene.SPRITE_MODE):
 				this.setVisible(true);
 				healthBar.setVisible(false);
 				energyBar.setVisible(false);
 				break;
 			case (GameScene.HEALTH_MODE):
 				this.setVisible(false);
 				healthBar.setVisible(true);
 				energyBar.setVisible(false);
 				break;
 			case (GameScene.ENERGY_MODE):
 				this.setVisible(false);
 				healthBar.setVisible(false);
 				energyBar.setVisible(true);
 				break;
 			default:
 				break;
 			
 		}		
 	}
 
 	
 	public String getType(){
 		return this.unitType;
 	}
 
 }
