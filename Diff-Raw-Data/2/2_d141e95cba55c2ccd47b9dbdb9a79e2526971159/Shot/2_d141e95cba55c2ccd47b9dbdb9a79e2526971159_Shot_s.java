 package ch.zhaw.mcag.model;
 
 import java.awt.Image;
 
 import ch.zhaw.mcag.*;
 
 /**
  * Shot item
  */
 public class Shot extends Item implements Movable, Destroyable {
 
 	private int direction = 1;
 
 	/**
 	 * Create a new shot
 	 *
 	 * @param position
 	 * @param dimension
 	 * @param image
 	 * @param good
 	 */
 	public Shot(Position position, Dimension dimension, Image image, boolean good) {
 		super(position, dimension, image);
 		if (!good) {
 			this.direction = -1;
 			this.good = good;
 		}
 	}
 
 	@Override
 	public void move() {
 		this.getPosition().setX(this.getPosition().getX() + Config.getMovePixels() * this.direction * 2);
 	}
 
 	@Override
 	public void destroy() {
 		this.setDisposed(true);
 	}
 
 	@Override
 	public boolean isGood() {
 		return good;
 	}
 }
