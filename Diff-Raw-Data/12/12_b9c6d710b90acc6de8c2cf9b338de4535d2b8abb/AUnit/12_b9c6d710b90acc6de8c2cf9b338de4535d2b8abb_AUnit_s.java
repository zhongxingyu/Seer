 package com.testgame.mechanics.unit;
 
 import java.util.ArrayList;
import java.util.HashMap;

 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
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
 import com.testgame.sprite.WalkMoveModifier;
 
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
 	
 	protected static int IDLE_START_FRAME = 0;
 	protected static int IDLE_END_FRAME = 1;
 	
 	protected static int WALK_RIGHT_START_FRAME = 13;
 	protected static int WALK_RIGHT_END_FRAME = 15;
 	
 	protected static int WALK_LEFT_START_FRAME = 10;
 	protected static int WALK_LEFT_END_FRAME = 12;
 	
 	protected static int WALK_UP_START_FRAME = 5;
 	protected static int WALK_UP_END_FRAME = 7;
 	
 	protected static int WALK_DOWN_START_FRAME = 2;
 	protected static int WALK_DOWN_END_FRAME = 4;
 	
 	protected static int ATTACK_START_FRAME;
 	protected static int ATTACK_END_FRAME;
 	
 	protected static int GUARD_FRAME = 19;
 	
 	protected static int ATTACKED_START_FRAME = 20;
 	protected static int ATTACKED_END_FRAME = 21;
 	
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
 		
 		Log.d("AndEngine", "numTilesX = " + numTilesX + ", numTilesY = " +numTilesY);
 		
 		WalkMoveModifier one = new WalkMoveModifier(timePerTile*numTilesX + .1f, this.getX(), this.getY(), destX, this.getY(), true);
 		WalkMoveModifier two = new WalkMoveModifier(timePerTile*numTilesY + .1f, destX, this.getY(), destX, destY, false);
 				
 		SequenceEntityModifier seq = new SequenceEntityModifier(new IEntityModifierListener() {
 			@Override
 			public void onModifierStarted(IModifier<IEntity> pModifier,
 					IEntity pItem) {
 				game.animating = true;
 				game.camera.setChaseEntity(pItem);
 				
 			}
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 				game.animating = false;
 				game.camera.setChaseEntity(null);
 				((AUnit)pItem).setCurrentTileIndex(((AUnit)pItem).start_frame);
 				game.setEventText("Moved using "+energy+" energy.");
 				Log.d("AndEngine", "[ComputerMove] calling perform next on player.");
 				player.performNext(); // finished this action, call next
 			}
 		}, one, two);
 		
 		//this.clearEntityModifiers();
 		this.registerEntityModifier(seq);
 	}
 	
 	@Override
 	public void move(int xNew, int yNew) {
 		
 		Log.d("AndEngine", this.toString() + " moving to " +xNew+", "+yNew);
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
 			
 			this.game.addMove(temp);
 
 			Log.d("AndEngine", "moving to " + destX + ", "+destY);
 			
 			float timePerTile = .2f; 
 			float numTilesX = Math.abs(this.getX() - destX) / game.tileSize;
 			float numTilesY = Math.abs(this.getY() - destY) / game.tileSize;
 			
 			Log.d("AndEngine", "numTilesX = " + numTilesX + ", numTilesY = " +numTilesY);
 			
 			WalkMoveModifier one = new WalkMoveModifier(timePerTile*numTilesX + .1f, this.getX(), this.getY(), destX, this.getY(), true);
 			WalkMoveModifier two = new WalkMoveModifier(timePerTile*numTilesY + .1f, destX, this.getY(), destX, destY, false);
 			
 			SequenceEntityModifier seq = new SequenceEntityModifier(game.animationListener, one, two);
 			
 			this.clearEntityModifiers();
 			
 			this.registerEntityModifier(seq);
 			
 			this.game.setEventText("Moved using "+eCost+" energy.");
 
         	
         	Log.d("AndEgine", "modifier finished.");
         	
         	// TODO: Add message to user about successful move
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return this.owner.getName() +"'s "+this.unitType;
 	}
 
 	
 	public void ComputerAttack(AUnit unit, int attack, int energy, ComputerPlayer player){
 		this.reduceEnergy(energy);
 		unit.reduceHealth(this.attack);
 		
 		unit.attackedAnimate(player);
 		
 		if (unit.getHealth() > 0) this.game.setEventText("Did "+this.attack+" damage!\n Unit health "+unit.getHealth()+"/"+unit.getMaxHealth());
 		
 	}
 	
 	// Which order does this go? THIS is being attacked or unit is being attacked ?? 
 	@Override
 	public void attack(final AUnit unit) {
 		int dist = manhattanDistance(this.x, this.y, unit.getMapX(), unit.getMapY());
 		if(dist <= this.attackrange && this.attackenergy <= this.energy){
 			this.reduceEnergy(this.attackenergy);
 			unit.reduceHealth(this.attack); // unit being attacked
 			JSONObject temp = new JSONObject();
 			try {
 				temp.put("MoveType", "ATTACK");
 				temp.put("UnitX", this.x);
 				temp.put("UnitY", this.y);
 				temp.put("Energy", this.attackenergy);
 				temp.put("OppX", unit.x);
 				temp.put("OppY", unit.y);
 				temp.put("Attack",this.attack);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			final AUnit temp2 = this;
 			this.game.addMove(temp);
 			unit.attackedAnimate(null);
 			/* 
 			game.activity.runOnUiThread(new Runnable() {
         	    @Override
         	    public void run() {
         	    	game.textMenu(temp2.toString() +" attacked " + unit.toString() + " for " + attack + " damage.");
           			 
         	    }
         	});
         	*/ // TODO: Add message in an entity here.
 			
 			if (unit.getHealth() > 0) this.game.setEventText("Did "+this.attack+" damage!\n Enemy health "+unit.getHealth()+"/"+unit.getMaxHealth());
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
 		//this.setText(this.energy, this.currentHealth);
 	}
 
 	public void setEnergy(int energy){
 		this.energy = energy;
 		//this.setText(this.energy, this.currentHealth);
 	}
 	
 	@Override
 	public void restoreEnergy(int energy) {
 		this.energy += energy;
 		//this.setText(this.energy, this.currentHealth);
 	}
 
 	@Override
 	public void reduceEnergy(int energy) {
 		this.energy -= energy;
 		//this.setText(this.energy, this.currentHealth);
 	}
 	
 	@Override
 	public void turnInit() {
 		if(this.energy >= 50)
 			this.setEnergy(100);
 		else if(this.energy >= 25){
 			this.setEnergy(50);
 		}
 		else
 			this.setEnergy(25);
 		this.isDefending = false;
 	}
 	
 	/*
 	 * checks a point x,y
 	 */
 	public void checkPointForMove(int offsetX, int offsetY, ArrayList<Point> moves) {
 
 		if (this.x + offsetX < 0 || this.y + offsetY < 0) return; // off of map, not occupied
 		if (this.x + offsetX >= map.xDim || this.y + offsetY >= map.yDim) return; // ditto
 		
 		AUnit occupyingUnit = map.getOccupyingUnit(this.x + offsetX, this.y + offsetY);
 		
 		Point p = new Point(offsetX, offsetY);
 		
 		if (occupyingUnit == null) { // not occupied
 			if (moves.contains(p)) return;
 			else moves.add(p);
 		}
 	}
 	
 	// all the squares you can move to 
 	public ArrayList<Point> availableMoves() {
 		ArrayList<Point> moves = new ArrayList<Point>();
 				
 		int movementRange = this.energy/this.range;
 		
 		for (int i = 0; i <= movementRange; i++) {
 			for (int j = 0; j <= movementRange - i; j++){
 				
 				if (i == 0 && j == 0) continue;
 				
 				checkPointForMove(i, j, moves);
 				checkPointForMove(i, -j, moves);
 				checkPointForMove(-i, j, moves);
 				checkPointForMove(-i, -j, moves);
 			}
 		}
 		return moves;
 	}
 	
 	// all the squares of enemies you can attack
 	public ArrayList<AUnit> availableTargets() {
 		ArrayList<AUnit> targets = new ArrayList<AUnit>();
 		
 		Log.d("AndEngine", "[AvailableTargets] Attack Ranage: " + attackrange);
 		int attackRange = this.attackrange;
 		if (this.attackenergy > this.energy) attackRange = 0; // no available moves b/c no energy
 		
 		for (int i = 0; i <= attackRange; i++) {
 			for (int j = 0; j <= attackRange - i; j++){ // check all the points at most attackrange distance away
 				
 				if (i == 0 && j == 0) continue; // no movement, not a move
 				
 				checkPointForTargets(i, j, targets);
 				checkPointForTargets(i, -j, targets);
 				checkPointForTargets(-i, j, targets);
 				checkPointForTargets(-i, -j, targets);
 			}
 		}
 		return targets;
 	}
 	
 	// checks to see if a square offsetX, offsetY distance from this unit has an ENEMY unit, if so adds it to the array
 	private void checkPointForTargets(int offsetX, int offsetY, ArrayList<AUnit> targets) {
 		
 		if (this.x + offsetX < 0 || this.y + offsetY < 0) return; // off of map, not occupied
 		if (this.x + offsetX >= map.xDim || this.y + offsetY >= map.yDim) return; // ditto
 		
 		AUnit occupyingUnit = map.getOccupyingUnit(this.x + offsetX, this.y + offsetY);
 		
 		if (occupyingUnit != null) { // occupied
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
 	public static int manhattanDistance(int x1, int y1, int x2, int y2) {
 		return Math.abs(x1-x2) + Math.abs(y1-y2);
 	}
 	
 	public void init() {
 		this.setPosition(this.x*this.game.tileSize, this.y*this.game.tileSize);
 		//this.initializeText(this.energy, this.currentHealth);
 		this.setOffsetCenter(0, 0);
 		this.game.attachChild(this);
 		this.game.registerTouchArea(this);
 	}
 	
 	public void idleAnimate() {
 		this.animate(new long[] { 100, 100 }, start_frame + IDLE_START_FRAME, start_frame + IDLE_END_FRAME, true);
 	}
 	
 	public void walkAnimate(int xDirection, int yDirection) {
 		// TODO: Should work, but we don't have the correct graphics yet to do this.
 		
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
 		this.setCurrentTileIndex(start_frame + GUARD_FRAME);
 	}
 	
 	public void attackedAnimate(final ComputerPlayer computerPlayer) {
 		this.animate(new long[] { 100, 100 }, start_frame + ATTACKED_START_FRAME, start_frame + ATTACKED_END_FRAME, true);
 		
 		final AUnit u = this;
 		if (computerPlayer != null) {
 			this.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() 
 	        {
 	            public void onTimePassed(final TimerHandler pTimerHandler) 
 	            {
 	                u.stopAnimation();
 	                u.setCurrentTileIndex(start_frame);
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
 	            }
 	        }));
 		}
 	}
 }
