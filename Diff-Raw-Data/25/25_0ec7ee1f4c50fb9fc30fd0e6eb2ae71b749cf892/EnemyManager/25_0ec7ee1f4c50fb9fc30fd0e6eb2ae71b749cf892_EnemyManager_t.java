 package ultraextreme.model.enemy;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
import android.util.Log;

 import ultraextreme.model.enemyspawning.EnemySpawner;
 import ultraextreme.model.util.Constants;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * @author Daniel Jonsson
  * @author Johan Gronvall
  * 
  */
 public class EnemyManager implements PropertyChangeListener {
 
 	private final List<AbstractEnemy> enemies;
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	public EnemyManager() {
 		enemies = new ArrayList<AbstractEnemy>();
 	}
 
 	public List<AbstractEnemy> getEnemies() {
 		return enemies;
 	}
 
 	public void addEnemy(final AbstractEnemy enemy) {
 		enemies.add(enemy);
 		pcs.firePropertyChange(Constants.EVENT_NEW_ENTITY, null,
 				enemy.getShip());
 
 	}
 
 	public void clearDeadEnemies() {
 		for (int i = 0; i < enemies.size(); i++) {
 			boolean remove = false;
 			final AbstractEnemy e = enemies.get(i);
 			if (e.isDead()) {
				Log.d("DEBUG", "isDead() : enemies.size()=" + enemies.size() + ", i=" + i);
 				pcs.firePropertyChange(Constants.EVENT_ENEMY_KILLED, null, e);
 				remove = true;
 			} else if (e.getShip().isOutOfScreen(150)) {
				Log.d("DEBUG", "isOutOfScreen() : enemies.size()=" + enemies.size() + ", i=" + i);
 				remove = true;
 			}
 			if (remove) {
				Log.d("DEBUG", "enemies.size()=" + enemies.size() + ", i=" + i);
 				removeEnemy(i);
 				i--;
 			}
 		}
 	}
 
 	private void removeEnemy(int index) {
 		pcs.firePropertyChange(Constants.EVENT_REMOVED_ENTITY, null,
 				enemies.get(index));
 		enemies.remove(index);
 	}
 
 	public void addPropertyChangeListener(final PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(
 			final PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		// This is executed when an enemy spawner wants to add a new enemy.
 		if (event.getPropertyName().equals(EnemySpawner.NEW_ENEMY)) {
 			addEnemy((AbstractEnemy) event.getNewValue());
 		}
 	}
 
 	public void clearAllEnemies() {
 		for (int i=0; i<enemies.size(); i++)
 		{
 			removeEnemy(i);
 			i--;
 		}
 		enemies.clear();
 	}
 }
