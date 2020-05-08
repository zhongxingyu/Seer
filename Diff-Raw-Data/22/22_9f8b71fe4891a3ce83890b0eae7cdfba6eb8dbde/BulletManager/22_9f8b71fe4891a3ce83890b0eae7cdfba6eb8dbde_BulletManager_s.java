 package ultraextreme.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import ultraextreme.model.entity.AbstractBullet;
 
 /**
  * Contains a queue of all bullets that are to be added to the game.
  * 
  * @author Bjorn Persson Mattsson
  * 
  */
 public class BulletManager {
 
 	private boolean isBombDropped = false;
 
 	private List<AbstractBullet> bullets;
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	public BulletManager() {
 		bullets = new ArrayList<AbstractBullet>();
 	}
 
 	/**
 	 * Adds a bullet to the creation queue.
 	 * 
 	 * @param b
 	 *            Bullet to be added.
 	 */
 	public void addBullet(AbstractBullet b) {
 		bullets.add(b);
 		pcs.firePropertyChange("newBullet", null, b);
 	}
 
 	/**
 	 * @return A list of all bullets that have been added since last call to
 	 *         this method.
 	 */
 	public List<AbstractBullet> getNewBullets() {
 		return bullets;
 	}
 
 	/**
 	 * Clear the list of bullets.
 	 */
 	public void clear() {
		// Maybe change this class to a BulletManager and let it handle all the
		// bullets
 		bullets.clear();
 	}
 
 	/**
 	 * Informs the class that a bomb has been dropped.
 	 */
 	public void dropBomb() {
 		this.isBombDropped = true;
 	}
 
 	/**
 	 * @return true if and only if a bomb has been dropped since last call to
 	 *         this method.
 	 */
 	public boolean isBombDropped() {
 		boolean output = this.isBombDropped;
 
 		if (output) {
 			this.isBombDropped = false;
 		}
 		return output;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 }
