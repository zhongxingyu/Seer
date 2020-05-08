 package edu.chl.codenameg.model;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 
 public class World {
 
 	private boolean gameOver;
 	private boolean hasWonGame;
 
 	private List<Entity> entities;
 	private int amountOfPlayers;
 
 	public World() {
 		this.gameOver = false;
 		this.hasWonGame = false;
 		this.entities = new ArrayList<Entity>();
 	}
 
 	public World(World copy) {
 		this();
 		this.entities = copy.getEntities();
 	}
 
 	public void add(Entity e) {
 		if (e instanceof PlayerCharacter) {
 			entities.add(e);
 			this.amountOfPlayers++;
 		} else {
 			entities.add(e);
 		}
 	}
 	public void remove(Entity e) {
 		if (e instanceof PlayerCharacter) {
 			entities.remove(e);
 			this.amountOfPlayers--;
 		} else {
 			entities.remove(e);
 		}
 	}
 
 	public List<Entity> getEntities() {
 		return new ArrayList<Entity>(this.entities);
 	}
 
 	public Camera getCamera() {
 		return new Camera();
 	}
 
 	public void update(int elapsedTime) {
 		// System.out.println("World update()!!!!!!!!");
 		for (Entity e : this.getEntities()) {
 			if (e instanceof PlayerCharacter) {
 				PlayerCharacter pc = (PlayerCharacter) e;
 				if (pc.hasWonGame() && this.getAmountOfPlayers() < 2) {
 					this.hasWonGame = true;
 					this.gameOver();
 				} else if (!pc.isAlive() && this.getAmountOfPlayers() < 2) {
 					this.gameOver();
 				}
 				if(!pc.isAlive()) {
 					this.remove(pc);
 				}
 				/*
 				 * else if(pc.hasWonGame() && this.getAmountOfPlayers() > 2){
 				 * entities.remove(pc); this.amountOfPlayers--; } else
 				 * if(!pc.isAlive() && this.getAmountOfPlayers() > 2){
 				 * entities.remove(pc); entities.add(new
 				 * PlayerCharacter(pc.getStartPosition())); }
 				 */
 			}
 			move(e);
 			e.update(elapsedTime);
 		}
 	}
 
 	private void move(Entity e) {
 		Vector2D vector = e.getVector2D();
 		motionx(e, vector.getX());
 		motiony(e, vector.getY());
 	}
 
 	public void gameOver() {
 		this.gameOver = true;
 	}
 
 	public boolean isGameOver() {
 		return this.gameOver;
 	}
 
 	public boolean isGameWon() {
 		return this.hasWonGame;
 	}
 
 	public int getAmountOfPlayers() {
 		return this.amountOfPlayers;
 	}
 
 	private boolean motionx(Entity e, float movex) {
 		// TODO float->int conversion!!!
 		int preferredx = Math.round(movex);
 		List<Entity> collided = new ArrayList<Entity>();
 		// System.out.println("NEW MOTIONX");
 		int addx = Math.signum(preferredx) > 0 ? 1 : -1;
 		for (int x = 0; x < Math.abs(preferredx); x++) {
 			
 			// Send friction to objects on top
 			for (Entity colliding : this.getEntitiesAt(new Rectangle(Math
 					.round(e.getPosition().getX()), Math.round(e
 					.getPosition().getY())-1, e.getHitbox().getWidth(), 1))) {
 				if (colliding != e) {
 					if (colliding.getCollideTypes().contains(e.getType())) {
 						this.motionx(colliding, Math.signum(preferredx));
 					}
 				}
 			}
 			
 			for (Entity colliding : this.getEntitiesAt(new Rectangle(Math
 					.round(e.getPosition().getX()
 							+ (x * Math.signum(preferredx)))
 					+ addx, Math.round(e.getPosition().getY()), e.getHitbox()
 					.getWidth(), e.getHitbox().getHeight()))) {
 				if (colliding != e) {
 
 					Direction d1 = Math.signum(preferredx) > 0 ? Direction.LEFT
 							: Direction.RIGHT;
 					Direction d2 = Math.signum(preferredx) > 0 ? Direction.RIGHT
 							: Direction.LEFT;
 
 					if (!collided.contains(colliding)) {
 						colliding.collide(new CollisionEvent(e, d1));
 						e.collide(new CollisionEvent(colliding, d2));
 						collided.add(colliding);
 					}
 
 					// If the entity is set to collide with this entity, do not
 					// allow it to move
 					if (e.getCollideTypes().contains(colliding.getType())) {
						if(colliding.getCollideTypes().contains(e.getType())) {
 							if(!motionx(colliding, Math.signum(preferredx))) {
 								return false;
 							}
 						} else {
 							return false;
 						}
 					}
 
 					// Else if the collided entity has this entity in his list,
 					// move it out of the way
					else if (colliding.getCollideTypes().contains(e.getType())) {
 						motionx(colliding, Math.signum(preferredx));
 					}
 
 					// Otherwise just pass through the entity
 				}
 			}
 			// System.out.println(Math.signum(preferredx));
 			e.setPosition(new Position(e.getPosition().getX()
 					+ Math.signum(preferredx), e.getPosition().getY()));
 		}
 		return true;
 	}
 
 	private boolean motiony(Entity e, float movey) {
 		// TODO float->int conversion!!!
 		int preferredy = Math.round(movey);
 		List<Entity> collided = new ArrayList<Entity>();// TODO
 
 		int addy = Math.signum(preferredy) > 0 ? 1 : -1;
 		for (int y = 0; y < Math.abs(preferredy); y++) {
 			for (Entity colliding : this
 					.getEntitiesAt(new Rectangle(Math.round(e.getPosition()
 							.getX()), Math.round(e.getPosition().getY()
 							+ (y * Math.signum(preferredy)))
 							+ addy, e.getHitbox().getWidth(), e.getHitbox()
 							.getHeight()))) {
 				if (colliding != e) {
 
 					Direction d1 = Math.signum(preferredy) > 0 ? Direction.TOP
 							: Direction.BOTTOM;
 					Direction d2 = Math.signum(preferredy) > 0 ? Direction.BOTTOM
 							: Direction.TOP;
 
 					if (!collided.contains(colliding)) {
 						colliding.collide(new CollisionEvent(e, d1));
 						e.collide(new CollisionEvent(colliding, d2));
 						collided.add(colliding);
 					}
 
 					if (e.getCollideTypes().contains(colliding.getType())) {
						if(colliding.getCollideTypes().contains(e.getType())) {
							if(!motiony(colliding, Math.signum(preferredy))) {
								return false;
							}
						} else {
							return false;
						}
 					} else if (colliding.getCollideTypes()
 							.contains(e.getType())) {
 						motiony(colliding, Math.signum(preferredy));
 					}
 				}
 			}
 			e.setPosition(new Position(e.getPosition().getX(), e.getPosition()
 					.getY() + Math.signum(preferredy)));
 		}
 		return true;
 	}
 
 	private List<Entity> getEntitiesAt(Position p) {
 		List<Entity> list = new ArrayList<Entity>();
 		for (Entity e : this.getEntities()) {
 			// TODO float->int :(((
 			Rectangle rect = new Rectangle(Math.round(e.getPosition().getX()),
 					Math.round(e.getPosition().getY()), e.getHitbox()
 							.getWidth(), e.getHitbox().getHeight());
 			if (rect.contains(new Point(Math.round(p.getX()), Math.round(p
 					.getY())))) {
 				list.add(e);
 			}
 		}
 		return list;
 	}
 
 	public List<Entity> getEntitiesAt(Rectangle target) {
 		List<Entity> list = new ArrayList<Entity>();
 		for (Entity e : this.getEntities()) {
 			// TODO float->int :(((
 			Rectangle rect = new Rectangle(Math.round(e.getPosition().getX()),
 					Math.round(e.getPosition().getY()), e.getHitbox()
 							.getWidth(), e.getHitbox().getHeight());
 			if (target.intersects(rect)) {
 				list.add(e);
 			}
 		}
 		return list;
 	}
 
 	@Deprecated
 	private void checkCollision(Entity e) {
 		for (Entity target : this.getEntities()) {
 			if (target != e) {
 				boolean collision = false;
 
 				// TODO Collision detection without using rounded doubles!!
 
 				Rectangle thisRectangle = new Rectangle(new Point(Math.round(e
 						.getPosition().getX()), Math.round(e.getPosition()
 						.getY())), new Dimension(e.getHitbox().getWidth(), e
 						.getHitbox().getHeight()));
 
 				Rectangle targetRectangle = new Rectangle(new Point(
 						Math.round(target.getPosition().getX()),
 						Math.round(target.getPosition().getY())),
 						new Dimension(target.getHitbox().getWidth(), target
 								.getHitbox().getHeight()));
 
 				if (thisRectangle.intersects(targetRectangle)) {
 					collision = true;
 
 					if (e.getCollideTypes().contains(target.getType())) {
 						Rectangle intersection = thisRectangle
 								.intersection(targetRectangle);
 
 						// Collision on bottom
 						if (intersection.y == targetRectangle.y) {
 							e.setPosition(new Position(e.getPosition().getX(),
 									e.getPosition().getY()
 											- intersection.height));
 						}
 						// Collision on top
 						if (intersection.y + intersection.height == targetRectangle.y
 								+ targetRectangle.height) {
 							System.out
 									.println(e.getType() + " collided on top");
 							e.setPosition(new Position(e.getPosition().getX(),
 									e.getPosition().getY()
 											+ intersection.height));
 						}
 						// Collision on right
 						if (intersection.x == targetRectangle.x) {
 							e.setPosition(new Position(e.getPosition().getX()
 									- intersection.width, e.getPosition()
 									.getY()));
 						}
 						// Collision on left
 						if (intersection.x + intersection.width == targetRectangle.x
 								+ targetRectangle.width) {
 							System.out.println(e.getType()
 									+ " collided on left");
 							e.setPosition(new Position(e.getPosition().getX()
 									+ intersection.width, e.getPosition()
 									.getY()));
 						}
 					}
 				}
 				if (collision) {
 					/*
 					 * e.collide(target); target.collide(e);
 					 */
 				}
 			}
 
 		}
 	}
 
 }
