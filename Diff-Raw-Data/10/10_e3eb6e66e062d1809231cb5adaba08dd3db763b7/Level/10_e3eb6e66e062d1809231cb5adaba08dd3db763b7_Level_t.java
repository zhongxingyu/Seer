 package fr.umlv.escape.game;
 import java.util.ArrayList;
 import java.util.List;
 
import fr.umlv.escape.Objects;
import fr.umlv.escape.ship.Wave;
 
 /**
  * Class that represent a Level.
  */
 public class Level {
 	private final String name;
 	private final List<Wave> waveList;
 	private final List<Long> delayWaveList;
 	private int currentWave;
 	
 	/**
 	 * Constructor
 	 * @param levelName The name of the level
 	 */
 	public Level(String levelName){
 		Objects.requireNonNull(levelName);
 		
 		this.name=levelName;
		this.waveList=new ArrayList<Wave>();
		this.delayWaveList=new ArrayList<Long>();
 		this.currentWave=0;
 	}
 
 	/**
 	 * Launch the next {@link Wave} of the level.
 	 * @return true if {@link Wave} has been launched else false if there was no {@link Wave} to launch.
 	 */
 	public boolean launchNextWave(){
 		try{
 			this.waveList.get(currentWave).startWave();
 		}
 		catch(IndexOutOfBoundsException e){
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Get the name of the level.
 	 * @return The name of the level.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Add a {@link Wave} to the level.
 	 * @param wave The wave to add.
 	 * @return true if the {@link Wave} have been added else false;
 	 */
 	public boolean addWaveList(Wave wave) {
 		Objects.requireNonNull(wave);
 		
 		return this.waveList.add(wave);
 	}
 	
 	/**
 	 * Add a delay to wait for the next wave. Delay is used to synchronize the launching of the {@link Wave}s
 	 * @param delay The delay to add.
 	 * @return true if the delay have been added else false;
 	 */
 	public boolean addDelayList(long delay) {
 		if(delay<0){
 			throw new IllegalArgumentException("delay cannot be negative");
 		}
 		return this.delayWaveList.add(delay);
 	}
 	
 	/**
 	 * Get all the {@link Wave} of the level.
 	 * @return The list of {@link Wave} of the level.
 	 */
 	public List<Wave> getWaveList(){
 		return this.waveList;
 	}
 	
 	/**
 	 * Get the current {@link Wave} of the level.
 	 * @return The current {@link Wave} of the level.
 	 */
 	public int getCurrentWave(){
 		return this.currentWave;
 	}
 	
 	/**
 	 * Get the current delay to wait to launch the next {@link Wave}.
 	 * @return The current delay to wait to launch the next {@link Wave}.
 	 */
 	public long getCurrentDelay(){
 		return this.delayWaveList.get(this.currentWave);
 	}
 	
 	/**
 	 * Pass to the next wave of the level.
 	 */
 	public void changeWave(){
 		this.currentWave++;
 	}
 }
