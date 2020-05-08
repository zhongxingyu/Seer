 package tiles.propagation;
 
 import game.Ray;
 import gui.ImageUtils;
 
 import java.awt.Color;
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import misc.Direction;
 import tiles.DrawableLayer;
 import tiles.Tile;
 
 /**
  * The base propagation component that defines public methods and encapsulates
  * access to tile state in subclasses.
  * 
  * @see Ray
  */
public abstract class PropagationComponent implements Serializable{
 
 	private Tile tile;
 	private Ray[] rays = new Ray[4];
 
 	protected PropagationComponent(Tile tile) {
 		this.tile = tile;
 	}
 
 	/**
 	 * Processes the incoming ray.
 	 * 
 	 * @param ray
 	 */
 	public void process(Ray ray) {
 	}
 
 	/**
 	 * Returns the tile direction.
 	 */
 	protected final Direction getDirection() {
 		return tile.getDirection();
 	}
 
 	/**
 	 * Returns the tile color.
 	 * 
 	 * @return Color
 	 */
 	protected final Color getColor() {
 		return tile.getColor();
 	}
 
 	/**
 	 * Sets the ray origin.
 	 * 
 	 * @param ray
 	 */
 	protected void setOrigin(Ray ray) {
 		Direction origin = ray.getDirection().getOpposite();
 		setRay(origin, ray);
 	}
 
 	/**
 	 * Clears all rays from the tile.
 	 */
 	public void clear() {
 		for (int i = 0; i < 4; i++) {
 			rays[i] = null;
 		}
 	}
 
 	/**
 	 * Sets a ray in the tile, if a ray has been already set in that direction,
 	 * performs color mix and updates the incoming ray.
 	 * 
 	 * @param direction
 	 * @param ray
 	 */
 	protected final void setRay(Direction direction, Ray ray) {
 		Ray clone = ray.clone();
 		Ray existing = getRay(direction);
 		if (existing != null) {
 			clone.setColor(ImageUtils.mix(ray.getColor(), existing.getColor()));
 		}
 		clone.setDirection(direction);
 		rays[direction.ordinal()] = clone;
 	}
 
 	/**
 	 * Returns the ray at the specified direction
 	 * 
 	 * @param direction
 	 * @return Ray
 	 */
 	protected final Ray getRay(Direction direction) {
 		return rays[direction.ordinal()];
 	}
 
 	/**
 	 * Returns whether the tile has rays
 	 * 
 	 * @return boolean
 	 */
 	public boolean hasRays() {
 		for (Ray ray : rays) {
 			if (ray != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns if the tile has a ray of the color specified
 	 * 
 	 * @param color
 	 * @return boolean
 	 */
 	public boolean hasRay(Color color) {
 		for (Ray ray : rays) {
 			if (ray != null && ray.getColor().equals(color)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the array of ray layers
 	 */
 	public Iterable<DrawableLayer> getRays() {
 		// TODO: Fix use of generics
 		List<DrawableLayer> rays = new ArrayList<DrawableLayer>();
 
 		for (Ray ray : this.rays) {
 			rays.add(ray);
 		}
 
 		return rays;
 	}
 
 }
