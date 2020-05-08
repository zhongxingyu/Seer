 package edu.brandeis.tjhickey.fireflies;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.graphics.PointF;
 
 /**
  * This is a simple model for a game with objects that move around the screen
  * and the user needs to catch them by clicking on them. This class represents
  * the state of the game
  * 
  * @author tim
  * 
  */
 public class GameModel {
 
 	/**
 	 * the game takes place on a square space with safety regions on the side
 	 */
 	public final float size;
 
 	List<GameActor> actors;
 	GameActor avatar;
 	GameActor hole;
 	
 	public boolean gameOver = false;
 	private boolean paused = true;
 	public int numActors;
 	public int numActive;
 	public int score;
 
 	private Random rand = new Random();
 
 	private PointF avatarMovement = new PointF(0f, 0f);
 
 	public GameModel(float size, int numActors) {
 
 		this.size = size;
 
 		this.numActors = numActors;
 		initActors();
 
 		this.avatar = new GameActor(size / 2, size / 2);
 		avatar.species = Species.avatar;
 		this.avatar.radius = 6;
 		
 		this.hole = new GameActor(size/2,size/2);
 		hole.species = Species.hole;
 		this.hole.radius = 6;
 
 		this.gameOver = false;
 	}
 
 	/**
 	 * initActors creates a new ArrayList of actors which consists of 90%
 	 * fireflies and 10% wasps REFACTOR!
 	 */
 	private void initActors() {
 		numActive = 0;
 		this.actors = new ArrayList<GameActor>();
 		for (int i = 0; i < numActors; i++) {
 			float x = rand.nextFloat() * size;
 			float y = rand.nextFloat() * size;
 			GameActor a = new GameActor(x, y);
 			this.actors.add(a);
 			a.speed = 1;
 			a.radius = 1;
 			if (numActive> numActors-5){
 				a.species = Species.wasp;
 			}
 			else {
 				a.species = Species.firefly;
 			}
 			numActive++;
 			}
 		}
 	
 
 	public void start() {
 		paused = false;
 		gameOver = false;
 	}
 
 	public void stop() {
 		gameOver = true;
 	}
 
 	public boolean isStopped() {
 		return gameOver;
 	}
 
 	/**
 	 * moveAvatar(dp) is called by the GameView when the user has
 	 * moved the avater by the model vector dp. The call is from
 	 * the UI thread so it could potentially have a conflict with
 	 * the GameLoop thread and hence we synchronize it
 	 * @param dp
 	 */
 	public void moveAvatar(PointF dp) {
 		synchronized (avatarMovement) {
 			this.avatarMovement.x = dp.x;
 			this.avatarMovement.y = dp.y;
 		}
 	}
 	
 	/**
 	 * getAvatarMovement(dp) stores the accumulated avatarMovement
 	 * in the parameter dp, since we don't want the other thread to be
 	 * modifying avatarMovement while we're working with it, we add
 	 * synchronized as we copy over the values and reset them to zero.
 	 * @param dp
 	 */
 	private void getAvatarMovement(PointF dp) {
 		synchronized (avatarMovement) {
 			dp.x = avatarMovement.x;
 			dp.y = avatarMovement.y;
 			// reset the avatarMovement
 		}
 	}
 
 	/**
 	 * Update moves all actors one step. Any fireflies that intersect with the
 	 * avatar become caught in the net if a wasp intersects the avatar,the game
 	 * ends
 	 * 
 	 */
 	public void update() {
 		if (paused || gameOver)
 			return;
 
 		// move the avatar to follow the mouse
 		PointF dp = new PointF(0f, 0f);
 		getAvatarMovement(dp);
 		avatar.x += dp.x;
 		avatar.y += dp.y;
 
 		// move all the other actors
 		for (GameActor a : this.actors) {
 			if (a.active) {
 				updateFreeActor(a);
 			} else {
 				updateCaughtActor(dp, a);
 			}
 		}
 		
 		
 
 		// check for gameOver
 		if (score == numActors)
 			gameOver = true;
 
 	}
 
 
 
 	private void updateCaughtActor(PointF dp, GameActor a) {
 		// move the caught fireflies along with the net
 		a.x += dp.x;
 		a.y += dp.y;
 		if (intersects(hole,avatar)){
 	    	
 	    	if(!a.active && a.species == Species.firefly && !a.scored){
 	    		a.scored = true;
 	    		score ++;
 	    	}
 	    }
 	}
 
 	private void updateFreeActor(GameActor a) {
 		// move the free fireflies and the wasps
 		a.update();
 		keepOnBoard(a);
 		if (intersects(a, avatar)) {
 			// check for actors in the net
 			if (a.species == Species.firefly) {
 				a.active = false;
 				numActive--;
 			}
 			
 			if(a.species == Species.wasp){
 				explode();
 			}
 		}
 		if(intersects(a,hole)) {
 			a.vx = (a.x-avatar.x);
 			a.vy = (a.y-avatar.y);
 		}
 	}
 	    
 	
 		
 	public void explode() {
 		for (GameActor a : actors) {
 			if(!a.scored && !a.active && a.species == Species.firefly) {
 			a.active = true;
 			numActive++;
 			float dx=a.x-avatar.x;
 			float dy=a.y-avatar.y;
 			a.vx=dx*3;
 			a.vy=dy*3;
 			}
 		}			
 	}
 
 	/*
 	 * if an actor moves off the board, in the x (or y) direction, it is bounced
 	 * back into the board and its velocity in the offending direction is
 	 * reversed
 	 * 
 	 * @param a
 	 */
 	private void keepOnBoard(GameActor a) {
 		if (a.x < 0) {
 			a.x = -a.x;
 			a.vx = -a.vx;
 		} else if (a.x > size) {
 			a.x = size - (a.x - size);
 			a.vx = -a.vx;
 		}
 		if (a.y < 0) {
 			a.y = -a.y;
 			a.vy = -a.vy;
 		} else if (a.y > size) {
 			a.y = size - (a.y - size);
 			a.vy = -a.vy;
 		}
 	}
 
 	/*
 	 * this returns true if the two actors intersect
 	 * 
 	 * @param a
 	 * 
 	 * @param b
 	 * 
 	 * @return
 	 */
 	private boolean intersects(GameActor a, GameActor b) {
 		float dx = a.x - b.x;
 		float dy = a.y - b.y;
 		float d = (float) Math.sqrt(dx * dx + dy * dy);
 		return (d < a.radius + b.radius);
 	}
 
 }
