 package com.secondhand.model.entity;
 
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.SoundType;
 
 public class Player extends BlackHole {
 
 	// starting lives for a new player.
 	public static final int STARTING_LIVES = 3;
 
 	private int lives;
 
 	private boolean isMirroredMovement;
 
 	private final int maxSize;
 
 	private float scoreMultiplier;
 
 	private float speedMultiplier;
 
 	private Vector2 needsToMovePosition;
 
 	//For playerView to get the color of player
 	// The color will change!
 	private float[] rgb = new float[3];
 
 	// ============== EVENT-CONSTANTS ==============
 	public final static String INCREASE_SCORE = "IncreaseScore";
 	public final static String INCREASE_LIFE = "IncreaseLife";
 	public final static String SOUND = "Sound";
 	public final static String COLOR = "color";
 	public final static float DEFAULT_COLOR_VALUE = 1f;
 	public final static String RANDOMLY_REPOSITION_PLAYER = "RandomlyRepositionPlayer";
 	public final static String MOVE = "Move";
 	// =============================================
 	public Player(final Vector2 position, final float radius, final int startingLives, final int score,
 			final int maxSize) {
 		super(position, radius, score);
 
 		this.maxSize = maxSize;
 		this.setSpeedMultiplier(1);
 		this.setScoreMultiplier(1);
 		this.lives = startingLives;
 		this.setMirroredMovement(false);
 		rgb[0] = DEFAULT_COLOR_VALUE;
 		rgb[1] = DEFAULT_COLOR_VALUE;
 		rgb[2] = DEFAULT_COLOR_VALUE;
 	}
 
 	public int getLives() {
 		return this.lives;
 	}
 
 	public int getMaxSize() {
 		return maxSize;
 	}
 
 	@Override
 	public void increaseScore(final int increase) {
 		final int oldScore = this.getScore();
 		super.increaseScore((int) this.getScoreMultiplier() * increase);
 		pcs.firePropertyChange(INCREASE_SCORE, oldScore, (int)getScore());
 	}
 
 	public void setScoreMultiplier(final float scoreMultiplier) {
 		this.scoreMultiplier = scoreMultiplier;
 	}
 
 	public float getScoreMultiplier() {
 		return this.scoreMultiplier;
 	}
 
 	public void setSpeedMultiplier(final float speedMultiplier) {
 		this.speedMultiplier = speedMultiplier;
 	}
 
 	public float getSpeedMultiplier() {
 		return this.speedMultiplier;
 	}
 
 
 	private void changeLives(final int change) {
 		lives += change;
 
 		pcs.firePropertyChange(INCREASE_LIFE, (int)0, (int)lives);
 	}
 
 	// the player loses a life
 	private void loseLife() {
 		changeLives(-1);
 	}
 
 	
 	
 	public void moveToRandomUnoccupiedArea() {
 	
 		pcs.firePropertyChange(Player.RANDOMLY_REPOSITION_PLAYER, null, null);
 	}
 
 	public boolean lostAllLives() {
 		return this.lives == 0;
 	}
 
 	public void gainLife() {
 		changeLives(+1);
 	}
 
 	// used to implement teleport, because you can't change the position inside
 	// a contact
 	// listener for some retarded reason(that would be too simple, now wouldn't
 	// it!?)
 	// So fuck you Erin Catto.
 	// - Sincerely, Eric
 	public void setNeedsToMovePosition(final Vector2 position) {
 		needsToMovePosition = new Vector2(position.x, position.y);
 	}
 
 	public void moveToNeededPositionIfNecessary() {
 		if (needsToMovePosition != null) {	
 			this.physics.setTransform(needsToMovePosition);
 			needsToMovePosition = null;
 		}
 	}
 
 	public float[] getRGB(){
 		return rgb;
 	}
 
 	public void setRGB(final float[] RGB){
 		this.rgb = RGB;
 		pcs.firePropertyChange(COLOR, null, RGB);
 	}
 
 	@Override
 	protected void onGrow() {
 		super.onGrow();
 		playSound(SoundType.GROW_SOUND);
 	}
 
 	@Override
 	protected void entityWasTooBigToEat(final Entity entity) {
 		super.entityWasTooBigToEat(entity);
 		playSound(SoundType.OBSTACLE_COLLISION_SOUND);
 	}
 
 	@Override
 	protected void wasEaten() {
 		playSound(SoundType.PLAYER_KILLED_SOUND);
 
 		// We override the default behaviour for wasEaten. We don't want the
 		// player to
 		// be entirely removed from the world when eaten, we only want to
 		// reposition the player and lose a life.
 
 		
 		// the player loses a life and is repositioned.
 		this.loseLife();
 			
 	
 		this.moveToRandomUnoccupiedArea();
 	}
 
 	public void playSound(final SoundType sound){
 		pcs.firePropertyChange(SOUND, null, sound);
 	}
 
 
 	public boolean isMirroredMovement() {
		return this.isMirroredMovement;
 	}
 
 	public void setMirroredMovement(final boolean mirrored) {
 		this.isMirroredMovement = mirrored;
 	}
 
 	public void reachToTouch(final Vector2 touch) {
 		
 		pcs.firePropertyChange(MOVE, null, touch);
 		
 		Vector2 forcePosition;
 
		if (this.isMirroredMovement()) {
 			final Vector2 v1 = new Vector2(touch.x - this.getCenterX(),
 					touch.y - this.getCenterY());
 			v1.mul(-1);
 			final Vector2 v2 = new Vector2(this.getCenterX(),
 					this.getCenterY());
 			v2.add(v1);
 			forcePosition = new Vector2(v2.x, v2.y);
 		} else {
 
 			forcePosition = new Vector2(touch.x, touch.y);
 		}
 
 		final Vector2 force = new Vector2((this.getCenterX() - touch.x),
 				this.getCenterY() - touch.y);
 
		if (this.isMirroredMovement) {
 			force.mul(-1);
 		}
 
 		force.x = force.x / force.len();
 		force.y = force.y / force.len();		
 
 		force.mul(this.getSpeedMultiplier() * 60);
 
 		this.physics.applyImpulse(force, forcePosition);
 	}
 	
 }
