 package com.andreschnabel.deathjam;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Player {
 
 	private static final float MOV_SPEED = 2f;
 	private static final float MAX_MOV_SPEED = 20.0f;
 	private static final float INERTIA_DECAY = 0.9f;
	private static final long SHIELD_INCR_DELAY = 2000;
 	private static final long SHIELD_DECR_DELAY = 500;
 	private static final long MIN_HIT_DELAY = 1000;
 
 	public Vector2 inertia = Vector2.Zero.cpy();
 
 	private Sprite playerSpr;
 	private List<TextureRegion> playerRegions = new ArrayList<TextureRegion>();
 	private final World world;
 
 	public int hp = 100;
 	public int shield = 0;
 	public boolean alive = true;
 	public int score;
 	public boolean gameover;
 
 	private long lastShieldIncr = Utils.getTicks();
 	private long lastShieldDecr = Utils.getTicks();
 
 	private final Sound dwSound;
 	private final Sound hitSound;
 	private final Sound goSound;
 
 	private final static long SHIELD_REQUEST_TIMEOUT = 350;
 	private long lastShieldRequest = Utils.getTicks();
 	private final TextureRegion shieldRegion;
 	private long lastHit = Utils.getTicks();
 
 	public Player(World world) {
 		this.world = world;
 
 		for(TextureAtlas.AtlasRegion region : Globals.atlas.getRegions()) {
 			if(region.name.matches("player\\d+"))
 				playerRegions.add(region);
 		}
 		playerSpr = new Sprite(playerRegions.get(0));
 		playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
 
 		dwSound = Gdx.audio.newSound(Utils.assetHandle("deathworld.wav"));
 		hitSound = Gdx.audio.newSound(Utils.assetHandle("hit.wav"));
 		goSound = Gdx.audio.newSound(Utils.assetHandle("gameover.wav"));
 
 		shieldRegion = Globals.atlas.findRegion("shield");
 	}
 
 	public void dispose() {
 		goSound.dispose();
 		hitSound.dispose();
 		dwSound.dispose();
 	}
 
 	public void move(float dx, float dy) {
 		dx *= MOV_SPEED;
 		dy *= MOV_SPEED;
 
 		inertia.x += dx;
 		inertia.y += dy;
 
 		if(inertia.x >= MAX_MOV_SPEED) inertia.x = MAX_MOV_SPEED;
 		if(inertia.y >= MAX_MOV_SPEED) inertia.y = MAX_MOV_SPEED;
 	}
 
 	private final float RECT_MINIFICATION_FACTOR = 0.85f;
 
 	private void minificateRect(Rectangle rect) {
 		rect.x += (1 - RECT_MINIFICATION_FACTOR) * rect.width;
 		rect.y += (1 - RECT_MINIFICATION_FACTOR) * rect.height;
 		rect.width *= RECT_MINIFICATION_FACTOR;
 		rect.height *= RECT_MINIFICATION_FACTOR;
 	}
 
 	// Returns true iff. switched maps
 	public boolean updatePlayerPos() {
 		playerSpr.setX(playerSpr.getX()+inertia.x);
 		playerSpr.setY(playerSpr.getY()+inertia.y);
 
 		Rectangle playerRect = playerSpr.getBoundingRectangle();
 		minificateRect(playerRect);
 
 		if(world.inTile(playerRect)) {
 			playerSpr.setX(playerSpr.getX()-inertia.x);
 			playerSpr.setY(playerSpr.getY() - inertia.y);
 			inertia.x *= -0.7f;
 			inertia.y *= -0.7f;
 			Utils.playSound(hitSound);
 
 			if(world.inTileOfType(playerRect, 'X') && !isShieldActive()) {
 				return kill();
 			}
 			else if(world.inTileOfType(playerRect, 'Y')) {
 				revive();
 				return true;
 			}
 			else if(world.inTileOfType(playerRect, 'Z')) {
 				nextMap();
 				return true;
 			}
 		}
 
 		List<Enemy> enemies = world.getEnemies();
 		playerRect = playerSpr.getBoundingRectangle();
 		minificateRect(playerRect);
 
 		for(Enemy enemy : enemies) {
 			if(Intersector.overlapRectangles(playerRect, enemy.getRect())) {
 				if(!isShieldActive() && Utils.getTicks() - lastHit > MIN_HIT_DELAY) {
 					hp -= 35;
 					lastHit = Utils.getTicks();
 					if(hp < 0) {
 						hp = 0;
 						return kill();
 					}
 				}
 			}
 		}
 
 		if(isShieldActive()) {
 			if(Utils.getTicks() - lastShieldDecr > SHIELD_DECR_DELAY) {
 				if(shield > 0)
 					shield--;
 				lastShieldDecr = Utils.getTicks();
 			}
 		}
 		else {
 			if(Utils.getTicks() - lastShieldIncr > SHIELD_INCR_DELAY) {
 				if(shield < 100)
 					shield++;
 				lastShieldIncr = Utils.getTicks();
 			}
 		}
 
 		return false;
 	}
 
 	private void nextMap() {
 		world.loadNextMap();
 		reset();
 	}
 
 	private void reset() {
 		playerSpr.setPosition(world.playerStart.x, world.playerStart.y);
 		inertia.set(0.0f, 0.0f);
 		hp = 100;
 		shield = 0;
 	}
 
 	public boolean kill() {
 		if(alive) {
 			playerSpr.setColor(Color.RED);
 			alive = false;
 			world.loadCurDeathworld();
 			reset();
 			Utils.playSound(dwSound);
 			return true;
 		} else {
 			if(!gameover) Utils.playSound(goSound);
 			gameover = true;
 			return false;
 		}
 	}
 
 	public void revive() {
 		alive = true;
 		world.loadCurMap();
 		reset();
 		playerSpr.setColor(Color.WHITE);
 	}
 
 	public float getMaxInertia() {
 		return Math.max(Math.abs(inertia.x), Math.abs(inertia.y));
 	}
 
 	public void updateInertia() {
 		//float delta = Gdx.graphics.getDeltaTime();
 		inertia.x *= INERTIA_DECAY;
 		inertia.y *= INERTIA_DECAY;
 
 		playerSpr.setRotation((float) (MathUtils.radiansToDegrees * Math.atan2(inertia.y, inertia.x)));
 
 		float maxInertia = getMaxInertia();
 		int regionIndex;
 		if(maxInertia <= 0.01f) {
 			regionIndex = 0;
 		} else if(maxInertia <= 5.0f) {
 			regionIndex = 1;
 		} else if(maxInertia <= 15.0f) {
 			regionIndex = 2;
 		} else {
 			regionIndex = 3;
 		}
 		playerSpr.setRegion(playerRegions.get(regionIndex));
 
 		score += world.tryCollectCoin(playerSpr.getBoundingRectangle());
 	}
 
 	public Rectangle getRect() {
 		return playerSpr.getBoundingRectangle();
 	}
 
 	public void draw(SpriteBatch sb) {
 		playerSpr.draw(sb);
 
 		if(isShieldActive()) {
 			sb.draw(shieldRegion, playerSpr.getX(), playerSpr.getY(),
 					playerSpr.getOriginX(), playerSpr.getOriginY(),
 					playerSpr.getWidth(), playerSpr.getHeight(),
 					playerSpr.getScaleX(), playerSpr.getScaleY(),
 					playerSpr.getRotation());
 		}
 	}
 
 	private boolean isShieldActive() {
 		return Utils.getTicks() - lastShieldRequest < SHIELD_REQUEST_TIMEOUT && shield > 0;
 	}
 
 	public void useShield() {
 		lastShieldRequest = Utils.getTicks();
 	}
 }
