 package ultraextreme.model.enemyspawning.wavelist;
 
 /**
  * This abstract WaveSpawningList implements some stuff that most lists will
  * need/use. It has the current wave counter and the number of maximum waves.
  * 
  * @author Daniel Jonsson
  * 
  */
 public abstract class AbstractWaveList implements WaveSpawningList {
 
 	/**
 	 * The number of waves that this wave list has spawned.
 	 */
 	private int currentWaveNumber;
 
 	/**
 	 * Total number of waves that this list contains.
 	 */
 	private final int numberOfWaves;
 
 	/**
 	 * @param numberOfWaves
 	 *            The number of available waves.
 	 */
 	public AbstractWaveList(final int numberOfWaves) {
 		this.numberOfWaves = numberOfWaves;
 		this.currentWaveNumber = 1;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void next() {
 		currentWaveNumber++;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final int getCurrentWaveNumber() {
 		return currentWaveNumber;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final int getNumberOfWaves() {
 		return numberOfWaves;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final boolean hasNext() {
		return currentWaveNumber < numberOfWaves;
 	}
 }
