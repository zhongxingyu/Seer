 package ultraextreme.model.enemy;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import ultraextreme.model.entity.IBullet;
 
 public class EnemyManager implements PropertyChangeListener {
 
 	public static final String NEW_ENEMY = "add";
 
 	private List<IEnemy> enemies;
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	public EnemyManager() {
 		enemies = new ArrayList<IEnemy>();
 	}
 
 	public List<IEnemy> getEnemies() {
 		return enemies;
 	}
 
 	public void addEnemy(IEnemy enemy) {
 		enemies.add(enemy);
 		pcs.firePropertyChange(EnemyManager.NEW_ENEMY, null, enemy.getShip());
 	}
 	
	public void removeEnemy(int index) {
		pcs.firePropertyChange("remove", null, enemies.get(index));
		enemies.remove(index);
	}
	
 	public void clearDeadEnemies()
 	{
 		for (int i = 0; i < enemies.size(); i++) {
 			IEnemy e = enemies.get(i);
 			if (e.isDead()) {
 				// TODO Also check for when the enemy has left the screen and should be removed
 				// TODO Change to fit reversed Y axis. (do tests)
				removeEnemy(i);
 				i--;
 			}
 		}
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		// This is executed when an enemy spawner wants to add a new enemy.
 		if (event.getPropertyName().equals(EnemySpawner.NEW_ENEMY)) {
 			addEnemy((IEnemy) event.getNewValue());
 		}
 	}
 }
