 package ultraextreme.model.enemyspawning;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import ultraextreme.model.enemy.IEnemy;
 import ultraextreme.model.enemyspawning.wave.Wave;
 import ultraextreme.model.enemyspawning.wave.WaveListener;
 import ultraextreme.model.enemyspawning.wavelist.WaveSpawningList;
 
 /**
  * This class is responsible for managing the waves of enemies. It takes one or
  * more WaveSpawningLists in its constructor, which this class then grabs new
  * waves from. References to the enemies are then sent to all of the
  * EnemySpawner's listeners.
  * 
  * @author Daniel Jonsson
  * 
  */
 public class EnemySpawner implements WaveListener {
 
 	public static final String NEW_ENEMY = "nE";
 
 	/**
 	 * A clock that keeps track of the time.
 	 */
 	private float timer;
 
 	/**
 	 * The number of the last started enemy wave.
 	 */
 	private int wave;
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	/**
 	 * The wave lists that the enemy spawner is working with and grabbing waves
 	 * from.
 	 */
	private List<WaveSpawningList> waveLists;
 
 	/**
 	 * The waves of enemies that are currently spawning enemies.
 	 */
	private List<Wave> activeWaves;
 
 	/**
 	 * Keeps track of which waves that have ended since the last update.
 	 */
	private List<Wave> finishedWaves;
 
 	/**
 	 * Create an enemy spawner.
 	 * 
 	 * @param waveLists
 	 *            Unspecified number of WaveSpawningLists. These will the
 	 *            EnemySpawner keep track of and spawn the enemies from their
 	 *            waves.
 	 */
	public EnemySpawner(WaveSpawningList... waveLists) {
 		this.waveLists = new ArrayList<WaveSpawningList>();
 		for (WaveSpawningList waveList : waveLists) {
 			if (waveList != null) {
 				this.waveLists.add(waveList);
 			}
 		}
 		if (this.waveLists.size() == 0) {
 			throw new IllegalArgumentException(
 					"You must specify one or more non-null waveLists");
 		}
 		activeWaves = new ArrayList<Wave>();
 		finishedWaves = new ArrayList<Wave>();
 		wave = 0;
 	}
 
 	/**
 	 * Run an update on the enemy spawner. The PropertyChangeListeners
 	 * registered to this class will get an enemy in an event if one is spawned.
 	 * 
 	 * @param timeElapsed
 	 *            Time since this method was last called.
 	 */
	public void update(float timeElapsed) {
 		timer += timeElapsed;
 		addNewWaves();
 		updateWaves(timeElapsed);
 	}
 
 	/**
 	 * Check the wave lists that are being monitored and grab new waves if any
 	 * should be spawned. Note that this method is recursive and that it calls
 	 * itself.
 	 */
 	private void addNewWaves() {
 		// TODO: This method could use some optimization.
 		for (int i = 0; i < waveLists.size(); i++) {
			WaveSpawningList waveList = waveLists.get(i);
 			if (waveList.hasNext()) {
 				if (waveList.getCurrentSpawningTime() <= timer) {
 					activeWaves.add(waveList.getCurrentWave());
 					waveList.getCurrentWave().addListener(this);
 					waveList.next();
 					wave++;
 					addNewWaves();
 				}
 			} else {
 				waveLists.remove(waveList);
 				i--;
 			}
 		}
 	}
 
 	/**
 	 * Run through the active waves and update them.
 	 * 
 	 * @param timeElapsed
 	 *            Time that has elapsed since the last update.
 	 */
	private void updateWaves(float timeElapsed) {
 		for (int i = 0; i < finishedWaves.size(); i++) {
 			activeWaves.remove(finishedWaves.get(i));
 			finishedWaves.remove(i);
 			i--;
 		}
		for (Iterator<Wave> i = activeWaves.iterator(); i.hasNext();) {
			Wave wave = i.next();
 			wave.update(timeElapsed);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
	public void waveEnded(Wave wave) {
 		// This method can't directly delete the wave from the activeWaves list
 		// because that will result in an ConcurrentModificationException, since
 		// higher up in the stack is this class' updateWaves method running.
 		finishedWaves.add(wave);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
	public void enemySpawned(IEnemy enemy) {
 		pcs.firePropertyChange(EnemySpawner.NEW_ENEMY, null, enemy);
 	}
 
 	/**
 	 * Add a listener that wants to get references to the spawned enemies.
 	 * 
 	 * @param listener
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	/**
 	 * Get the current wave number.
 	 * 
 	 * @return The number of waves that has started.
 	 */
 	public int getCurrentWave() {
 		return wave;
 	}
 }
