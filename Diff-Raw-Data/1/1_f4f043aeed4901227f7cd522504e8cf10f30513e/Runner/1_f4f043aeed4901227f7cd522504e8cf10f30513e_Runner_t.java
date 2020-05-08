 package com.me.mygdxgame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.utils.TimeUtils;
 
 public class Runner {
 	private static final float JUMP_HEIGHT = 5;
 	private Texture currentSprite;
 	private Texture runningSprite[];
 	private Texture jumpingSprite[];
 	private Texture duckingSprite[];
 	private int animationIndex;
 	private float lastFrameTime;
 	protected Rectangle hitbox;
 	private float RESW;
 	private float RESH;
 	final int SPRITE_WIDTH = 43;
 	final int SPRITE_HEIGHT = 60;
 	final int SCALE = 1; // Only affects hit box at the moment
 	final int WIDTH = SPRITE_WIDTH * SCALE;
 	final int HEIGHT = SPRITE_HEIGHT * SCALE;
 	private OrthographicCamera camera;
 	private float floorHeight;
 	private int posX = -300;
 	private float speedY;
 	private final float gravity = -1;
 	private float jumpSpeed;
 	protected boolean released;
 
 	enum State {
 		jumping, running, dead, ducking
 	};
 
 	State state;
 
 	// Debug
 
 	Runner(OrthographicCamera pcamera, float pheight) {
 		RESW = pcamera.viewportHeight;
 		RESH = pcamera.viewportHeight;
 		camera = pcamera;
 		floorHeight = pheight;
 		create();
 	}
 
 	public void create() {
 		speedY = 0;
 		Texture walkingInit[] = {
 				new Texture(Gdx.files.internal("data/bearsum/walk1.png")),
 				new Texture(Gdx.files.internal("data/bearsum/walk2.png")),
 				new Texture(Gdx.files.internal("data/bearsum/walk3.png")),
 				new Texture(Gdx.files.internal("data/bearsum/walk4.png")),
 				new Texture(Gdx.files.internal("data/bearsum/walk3.png")),
 				new Texture(Gdx.files.internal("data/bearsum/walk2.png"))};
 		runningSprite = walkingInit;
 		Texture jumpingInit[] = {
 				new Texture(Gdx.files.internal("data/bearsum/jump1.png")),
 				new Texture(Gdx.files.internal("data/bearsum/jump2.png")),
 				new Texture(Gdx.files.internal("data/bearsum/jump3.png")) };
 		jumpingSprite = jumpingInit;
 		Texture duckingInit[] = { 
 				new Texture(Gdx.files.internal("data/bearsum/roll1.png")),
 				new Texture(Gdx.files.internal("data/bearsum/roll2.png")),
 				new Texture(Gdx.files.internal("data/bearsum/roll3.png")),
 				new Texture(Gdx.files.internal("data/bearsum/roll4.png")),
 				new Texture(Gdx.files.internal("data/bearsum/roll5.png")) };
 		duckingSprite = duckingInit;
 		
 		hitbox = new Rectangle(camera.position.x + posX, floorHeight,
 				SPRITE_WIDTH, SPRITE_HEIGHT);
 		animationIndex = 0;
 		lastFrameTime = TimeUtils.nanoTime();
 		state = State.running;
 	}
 
 	public void draw(SpriteBatch batch) {
 		switch (state) {
 		case running:
 			if (animationIndex > runningSprite.length - 1) {
 				animationIndex = 0;
 			}
 			currentSprite = runningSprite[animationIndex];
 			break;
 
 		case jumping:
 			if (animationIndex > jumpingSprite.length - 1) {
 				animationIndex = 0;
 			}
 			currentSprite = jumpingSprite[animationIndex];
 			break;
 
 		case ducking:
 			if (animationIndex > duckingSprite.length - 1) {
 				animationIndex = 0;
 			}
			currentSprite = duckingSprite[animationIndex];
 		case dead:
 			break;
 		default:
 			break;
 		}
 
 		batch.draw(currentSprite, hitbox.x, hitbox.y);
 		if (TimeUtils.nanoTime() - lastFrameTime >= 100000000) {
 			if (state == State.jumping) {
 				if (animationIndex < jumpingSprite.length - 1) {
 					animationIndex++;
 				}
 			} else {
 				animationIndex++;
 			}
 
 			lastFrameTime = TimeUtils.nanoTime();
 		}
 
 	}
 
 	public void floorCheck() {
 		if (speedY > 0) {
 			state = state.jumping;
 			jumpSpeed *= 0.70;
 		} else {
 			if (hitbox.y <= floorHeight) {
 				if (state != State.ducking) {
 					state = state.running;
 				}
 				jumpSpeed = JUMP_HEIGHT;
 				hitbox.y = floorHeight;
 				speedY = 0;
 			
 			}
 		}
 
 	}
 
 	public void dispose() {
 		currentSprite.dispose();
 		for (int i = 0; i < runningSprite.length; i++) {
 			runningSprite[i].dispose();
 		}
 		for (int i = 0; i < jumpingSprite.length; i++) {
 			jumpingSprite[i].dispose();
 		}
 	}
 
 	public void update() {
 		speedY += gravity;
 		hitbox.y += speedY;
 		hitbox.x = camera.position.x + posX;
 		floorCheck();
 	}
 
 	public void jump() {
 		if (state == State.running && released == true) {
 			speedY += jumpSpeed;
 		}
 		if (state == State.jumping && released == false) {
 			speedY += jumpSpeed;
 		}
 		released = false;
 	}
 
 	public void release() {
 		if (state == State.running) {
 			released = true;
 		}
 		if (state == State.ducking) {
 			hitbox.setHeight(SPRITE_HEIGHT);
 			hitbox.setWidth(SPRITE_WIDTH);
 			state = State.running;
 		}
 
 	}
 
 	public void duck() {
 		hitbox.setHeight(SPRITE_WIDTH);
 		hitbox.setHeight(SPRITE_HEIGHT);
 		if (state == State.running) {
 			state = State.ducking;
 		}
 
 	}
 
 }
