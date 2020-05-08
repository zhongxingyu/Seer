 package com.picotech.lightrunnerlibgdx;
 
 import java.util.Random;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 public class Enemy extends Sprite2 {
 	/**
 	 * Represents the different types of Enemies.
 	 */
 	static enum Type {
 		NORMAL, FAST, RANDOM
 	}
 
 	Type type = Type.NORMAL;
 	int health;
 	int maxHealth;
 	boolean alive;
 	boolean losingHealth;
 	boolean normalizedVelocity;
 	boolean isSlow = false;
 	Random r = new Random();
 
 	/**
 	 * Initializes a new enemy at a particular level.
 	 * 
 	 * @param Position
 	 *            the new position
 	 * @param newW
 	 *            the width of the enemy
 	 * @param newH
 	 *            the height of the enemy
 	 * @param level
 	 *            the level of the enemy
 	 */
 	public Enemy(Vector2 Position, int newW, int newH, int level) {
 		super(Position, newW, newH);
 		float enemySpawning = (float) (r.nextInt(30 - (11 - level / 3)) + (11 - level / 3)); //  MathUtils.random(11 - level / 3, 30);
 		if (enemySpawning < 10 && enemySpawning > 3) {
 			type = Type.FAST;
 			maxHealth = 25;
 			velocity = new Vector2(-7.5f, r.nextFloat()*2 - 1);// MathUtils.random(-.1f, .1f));
 		} else if (enemySpawning <= 3) {
 			type = Type.RANDOM;
 			maxHealth = 10;
 			velocity = new Vector2(-5.0f, 0);
 		} else {
 			type = Type.NORMAL;
 			maxHealth = 50;
 			velocity = new Vector2(-1.0f, r.nextFloat() - .5f); //MathUtils.random(-.2f, .2f));
 		}
 		asset="enemy.png";
 		alive = true;
 		health = maxHealth;
 	}
 
 	/**
 	 * Updates the enemy, moving it and checking if it is still alive.
 	 */
 	public void update() {
 		losingHealth = false;
 
 		// played around with some sinusoidal functions for the random blocks
 		if (type == Type.RANDOM) {
			velocity.x -= (float) Math.sin(position.x / 100);
 			velocity.y += (float) Math.sin(position.y / 25);
 		}
 
 		if (isSlow) {
 			if (type == Type.NORMAL) {
 				velocity.x = -0.2f;
 			} else if (type == Type.FAST) {
 				velocity.x = -1.5f;
 			} else if (type == Type.RANDOM) {
 				velocity.x /= 5f;
 			}
 			normalizedVelocity = false;
 		} else if (!normalizedVelocity){
 			if (type == Type.NORMAL) {
 				velocity = new Vector2(-1.0f, r.nextFloat() - .5f);//MathUtils.random(-.2f, .2f));
 			} else if (type == Type.FAST) {
 				velocity = new Vector2(-7.5f, r.nextFloat()*2 - 1f);//MathUtils.random(-.1f, .1f));
 			}
 			normalizedVelocity = true;
 		}
 
 		position.x += velocity.x;
 		position.y += velocity.y;
 		if (health <= 5 || position.x + bounds.width < 0
 				|| position.y + bounds.height < 0
 				|| position.y - bounds.height > GameScreen.height) {
 			alive = false;
 			Assets.playSound(Assets.died);
 		}
 		updateVertices();
 	}
 
 	
 	/* Draws the Enemy as a filled rectangle. The color is a function of the
 	 * health.
 	 * 
 	 * @param sr
 	 *            the ShapeRenderer to use to draw the rectangle.
 	
 	public void draw(ShapeRenderer sr) {
 		if (alive) {
 			sr.begin(ShapeType.FilledRectangle);
 			if (type == Type.NORMAL) {
 				sr.setColor((health - 5) / 5f, 0, 0, 1);
 				sr.filledRect(position.x, position.y, bounds.width,
 						bounds.height);
 			} else if (type == Type.FAST) {
 				sr.setColor(0, (health - 5) / 5f, 0, 1);
 				sr.filledRect(position.x, position.y, bounds.width,
 						bounds.height);
 			} else if (type == Type.RANDOM) {
 				sr.setColor(0, 0, (health - 5) / 5f, 1);
 				sr.filledRect(position.x, position.y, bounds.width,
 						bounds.height);
 			}
 			if (losingHealth || health < maxHealth) {
 				sr.setColor(Color.WHITE);
 				sr.filledRect(position.x, position.y + bounds.height + 2,
 						((health / (float) maxHealth) * bounds.width), 5);
 			}
 			sr.end();
 		}
 	}*/
 	
 	/**
 	 * Draws the Enemy based on the texture enemy.png.
 	 */
 	@Override
 	public void draw(SpriteBatch batch) {
 		if (alive) {
 			batch.begin();
 			if(type == Type.NORMAL) {
 				batch.setColor((health - 5) / 5f, 0, 0, 1);
 				batch.draw(texture, position.x, position.y);
 			} else if (type == Type.FAST) {
 				batch.setColor(0, (health - 5) / 5f, 0, 1);
 				batch.draw(texture, position.x, position.y);
 			} else if (type == Type.RANDOM) {
 				batch.setColor(0, 0, (health - 5) / 5f, 1);
 				batch.draw(texture, position.x, position.y);
 			}
 			if (losingHealth || health < maxHealth) {
 				batch.setColor(Color.WHITE);
 				batch.draw(new TextureRegion(Assets.pixel), position.x, position.y + bounds.height + 2, 0, 0,
 						1, 1, ((health / (float) maxHealth) * bounds.width), 5, 0f);
 			}
 			batch.setColor(Color.WHITE);
 			batch.end();
 		}
 	}
 	
 }
