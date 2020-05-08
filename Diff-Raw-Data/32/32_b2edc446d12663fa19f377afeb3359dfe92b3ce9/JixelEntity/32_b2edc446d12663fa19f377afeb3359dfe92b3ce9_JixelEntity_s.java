 package jixel.entity;
 
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import jixel.gui.JixelSprite;
 import jixel.stage.JixelGame;
 
 @SuppressWarnings("serial")
 public abstract class JixelEntity extends JixelSprite implements Comparable<JixelEntity> {
 
 	private String name, animPath;
 	private double speed;
 	public boolean wasUpdated = false;
 	private int originX, originY;
 
 	private Rectangle hitBox;
 	public boolean hitBoxLocked = false;
 	private List<JixelEntity> collisionList = Collections.synchronizedList(new ArrayList<JixelEntity>());
 
 	private String currentAnim = null;
 	private int animIndex, fps, frameCount;
 	private Map<String, List<Integer>> animMap = new HashMap<String, List<Integer>>();
 
 	public JixelEntity(String imgPath, String animPath, String name, int width, int height, int x, int y, double speed) {
 		super(imgPath, width, height, x, y);
 		this.animPath = animPath;
 		this.name = name;
 		hitBox = new Rectangle(x, y, width, height);
 		originX = width >> 1;
 		originY = height >> 1;
 		if (animPath != null) {
 			loadAnimFile(animPath);
 		}
 	}
 
 	public synchronized void changeSprite(JixelEntity e) {
 		if (e != null) {
 			changeSprite(e.getPath(), e.getAnimPath(), getWidth(), getHeight());
 		} else {
 			JixelGame.getConsole().printErr(new NullPointerException("Can not change sprite to null entity at " + name));
 		}
 	}
 
 	/**
 	 * Change the underlying sprite for the entity
 	 * 
 	 * @param imgPath - The new path for the underlying sprite sheet
 	 * @param animPath - The new path for the anim file
 	 * @param width - The new width of the entity
 	 * @param height - The new height of the entity
 	 */
 	public synchronized void changeSprite(String imgPath, String animPath, int width, int height) {
 		loadSheet(imgPath, width, height);
 		setTileID(0);
 		if (animPath != null) {
 			loadAnimFile(animPath);
 		} else {
 			animMap = null;
 			currentAnim = null;
 		}
 	}
 
 	/**
 	 * Reads the animation file for an entity
 	 * 
 	 * @param path - The path of the anim file
 	 */
 	public void loadAnimFile(String path) {
 		this.animPath = path;
 		animIndex = 1;
 		fps = 0;
 		frameCount = 0;
 		File f = new File(path);
 		try (Scanner scan = new Scanner(f)) {
 			fps = scan.nextInt();
 			scan.nextLine();
 			synchronized (this) {
 				while (scan.hasNextLine()) {
 					String name = scan.next();
 					List<Integer> tiles = new ArrayList<Integer>();
 					if (animMap.size() == 0) {
 						currentAnim = name;
 					}
 					while (scan.hasNextInt()) {
 						tiles.add(scan.nextInt());
 					}
 					animMap.put(name, tiles);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			JixelGame.getConsole().printErr("Could not find anim file at: " + f.getPath(), e);
 		} catch (Exception e) {
 			JixelGame.getConsole().printErr("Failed to read anim file at: " + f.getPath(), e);
 		}
 	}
 
 	/**
 	 * Updates the animation of the entity
 	 */
 	private void updateAnim() {
 		if (frameCount == fps) {
 			List<Integer> tiles = animMap.get(currentAnim);
 			frameCount = 0;
 			animIndex++;
 			if (animIndex == tiles.size()) {
 				if (tiles.get(0) == 0) {
 					animIndex--;
 				} else {
 					animIndex = 1;
 				}
 			}
 			setTileID(tiles.get(animIndex));
 		}
 	}
 
 	/**
 	 * Plays the animation with the given name for the entity
 	 * 
 	 * @param name - The name of the animation
 	 */
 	public synchronized void playAnim(String name) {
 		if (currentAnim == null) {
 			JixelGame.getConsole().printErr(new NullPointerException("Can not play animation if no anim file exists at " + this.name));
 			return;
 		}
 		if (animMap.containsKey(name)) {
 			if (!name.equals(currentAnim)) {
 				animIndex = 1;
 				currentAnim = name;
 			}
 		} else {
 			JixelGame.getConsole().printErr(new FileNotFoundException("No anim called " + name + " found in " + this.name));
 		}
 	}
 
 	/**
 	 * Compares if this entity's name is equal to a given String
 	 * 
 	 * @param name - The name to compare to
 	 * @return whether or not the string is equal to the name
 	 */
 	public boolean equalsByName(String name) {
 		return name.equals(this.name);
 	}
 
 	/**
 	 * Compares if two entities have the same name
 	 * 
 	 * @param e - Entity to compare
 	 * @return whether or not they have the same name
 	 */
 	public boolean equalsByName(JixelEntity entity) {
 		if (entity == null) {
 			return false;
 		}
 		return entity.getName().equals(this.name);
 	}
 
 	/**
 	 * Applies the update if game isn't paused
 	 */
 	public void applyActions() {
 		if (!JixelGame.getPaused() && !wasUpdated) {
 			synchronized (this) {
 				if (currentAnim != null && fps > 0) {
 					frameCount++;
 					updateAnim();
 				}
 			}
 			update();
 			wasUpdated = true;
 		}
 	}
 
 	/**
 	 * Update method to be run every frame
 	 */
 	public abstract void update();
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param the new name of the entity
 	 */
 	public void setName(String name) {
 		if (name != null) {
 			this.name = name;
 		} else {
 			JixelGame.getConsole().printErr(new NullPointerException("Can not set entity name to null"));
 		}
 	}
 
 	/**
 	 * @return the speed
 	 */
 	public double getSpeed() {
 		return speed;
 	}
 
 	/**
 	 * @param the new speed of the entity
 	 */
 	public void setSpeed(double speed) {
 		this.speed = speed;
 	}
 
 	/**
 	 * @return the underlying path for the entity's anim file
 	 */
 	public String getAnimPath() {
 		return animPath;
 	}
 
 	/**
 	 * Compares an entity by Y axis position
 	 * 
	 * @return -1 if entity is lower, sorts by x if equal, 1 if higher
 	 */
 	public int compareTo(JixelEntity e) {
 		if (e == null) {
 			return -1;
 		}
 		if (e.getY() + e.getOriginY() > getY() + getOriginY()) {
 			return -1;
 		} else if (e.getY() == getY()) {
 			if (e.getX() + e.getOriginX() > getX() + getOriginX()) {
 				return -1;
 			} else if (e.getX() == getX()) {
 				return 0;
 			}
 			return 1;
 		}
 		return 1;
 	}
 
 	/**
 	 * @return the origin for the x axis
 	 */
 	public int getOriginX() {
 		return originX;
 	}
 
 	/**
 	 * @param sets a new origin for the x axis
 	 */
 	public void setOriginX(int originX) {
 		this.originX = originX;
 	}
 
 	/**
 	 * @return the origin for the y axis
 	 */
 	public int getOriginY() {
 		return originY;
 	}
 
 	/**
 	 * @param sets a new origin for the y axis
 	 */
 	public void setOriginY(int originY) {
 		this.originY = originY;
 	}
 
 	/**
 	 * Sets a new origin for the entity
 	 * 
 	 * @param originX - The new origin for the x axis
 	 * @param originY - The new origin for the y axis
 	 */
 	public void setOrigin(int originX, int originY) {
 		this.originX = originX;
 		this.originY = originY;
 	}
 
 	/**
 	 * @return the entity's hit box
 	 */
 	public Rectangle getHitBox() {
 		return hitBox;
 	}
 
 	/**
 	 * Checks if the entity's underlying hit box intersects an entity
 	 * 
 	 * @param e - The entity to compare
 	 * @return whether or not they intersect
 	 */
 	public boolean intersects(JixelEntity e) {
 		if (e != null) {
 			return hitBox.intersects(e.getHitBox());
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Checks if the entity's underlying hit box intersects a shape
 	 * 
 	 * @param s - The shape to compare
 	 * @return whether or not they intersect
 	 */
 	public boolean intersects(Shape s) {
 		if (s != null) {
 			return s.intersects(hitBox);
 		}
 		return false;
 	}
 
 	@Override
 	public void setX(double x) {
 		super.setX(x);
 		if (!hitBoxLocked) {
 			hitBox.setLocation((int) x, (int) getY());
 		}
 	}
 
 	@Override
 	public void setY(double y) {
 		super.setY(y);
 		if (!hitBoxLocked) {
 			hitBox.setLocation((int) getX(), (int) y);
 		}
 	}
 
 	/**
 	 * Returns a list containing all the entities this entity is colliding with at this moment. Note: Use of this list must be synchronized with the list as a lock. Failure to do so will result in a
 	 * ConcurrentModificationException or undetermined behavior.
 	 * 
 	 * @return the underlying collision list
 	 */
 	public List<JixelEntity> getCollisionList() {
 		return collisionList;
 	}
 
 	@Override
 	public String toString() {
 		return "JixelEntity [name=" + name + ", x=" + getX() + ", y=" + getY() + ", tileID=" + getTileID() + "]";
 	}
 
 }
