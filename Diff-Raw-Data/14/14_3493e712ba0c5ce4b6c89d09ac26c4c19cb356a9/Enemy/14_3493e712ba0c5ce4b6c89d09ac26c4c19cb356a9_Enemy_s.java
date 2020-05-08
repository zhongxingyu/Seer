 package com.picotech.lightrunnerlibgdx;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.MathUtils;
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

 	boolean isSlow = false;
 
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
 		float enemySpawning = (float) MathUtils.random(11 - level / 3, 30);
 		if (enemySpawning < 10 && enemySpawning > 3) {
 			type = Type.FAST;
 			maxHealth = 25;
 			velocity = new Vector2(-7.5f, MathUtils.random(-.1f, .1f));
 		} else if (enemySpawning <= 3) {
 			type = Type.RANDOM;
 			maxHealth = 10;
 			velocity = new Vector2(-5.0f, 0);
 		} else {
 			type = Type.NORMAL;
 			maxHealth = 50;
 			velocity = new Vector2(-1.0f, MathUtils.random(-.2f, .2f));
 		}
 
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
 			velocity.x += (float) Math.sin(position.x / 100);
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
		} else {
 			if (type == Type.NORMAL) {
				//velocity = new Vector2(-1.0f, MathUtils.random(-.2f, .2f));
 			} else if (type == Type.FAST) {
				//velocity = new Vector2(-7.5f, MathUtils.random(-.1f, .1f));
 			}
 		}
 
 		position.x += velocity.x;
 		position.y += velocity.y;
 		if (health <= 5 || position.x + bounds.width < 0
 				|| position.y + bounds.height < 0
 				|| position.y - bounds.height > 720) {
 			alive = false;
 			Assets.died.play();
 		}
 		updateVertices();
 	}
 
 	/**
 	 * Draws the Enemy as a filled rectangle. The color is a function of the
 	 * health.
 	 * 
 	 * @param sr
 	 *            the ShapeRenderer to use to draw the circle.
 	 */
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
 	}
 }
