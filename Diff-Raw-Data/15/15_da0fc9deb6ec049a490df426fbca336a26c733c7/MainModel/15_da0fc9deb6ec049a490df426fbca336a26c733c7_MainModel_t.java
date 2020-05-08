 package model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import model.save.ScoreModel;
 
 
 /**
  * The main model class which holds all the sub model systems.
  * 
  * @author 
  *
  */
 public class MainModel {
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	private ScoreModel scoreModel;
 	private GameModel gameModel;
 	
 	/**
 	 * Specifies that a new game model has been set.
 	 */
 	public static final String EVENT_NEW_GAME_MODEL = "newGameModel";
 	/**
 	 * Specifies that a new settings model has been set.
 	 */
 	public static final String EVENT_NEW_SETTINGS_MODEL = "newSettingsModel";
 	/**
 	 * Specified that a new high score model has been set.
 	 */
 	public static final String EVENT_NEW_HIGH_SCORE_MODEL = "newHighScoreModel";
 
 	/**
 	 * Creates a new default model.
 	 */
 	public MainModel() {
 		this.scoreModel = new ScoreModel();
 		this.gameModel = null;		
 	}
 	
 	/**
 	 * Gives the model of the game.
 	 * @return the model of the game.
 	 */
 	public GameModel getGameModel() {
 		return gameModel;
 	}
 
 	/**
 	 * Gives the high score model.
 	 * @return the high score model.
 	 */
 	public ScoreModel getScoreModel() {
 		return scoreModel;
 	}
 
 	/**
 	 * Sets the game model.
 	 * @param gameModel the new game model.
 	 */
 	public void setGameModel(GameModel gameModel) {
 		this.gameModel = gameModel;
 		this.pcs.firePropertyChange(EVENT_NEW_GAME_MODEL, null, gameModel);
 	}
 
 	/**
 	 * Sets the high score model.
 	 * @param scoreModel the new high score model.
 	 */
 	public void setScoreModel(ScoreModel scoreModel) {
 		this.scoreModel = scoreModel;
 		this.pcs.firePropertyChange(EVENT_NEW_HIGH_SCORE_MODEL, null, scoreModel);
 	}
 
 	/**
 	 * Adds a listener to this model. 
 	 * The listener will be notified when any of the models this main model holds
 	 * are set to new ones.
 	 * @param pcl the listener to add.
 	 */
 	public void addListener(PropertyChangeListener pcl) {
 		this.pcs.addPropertyChangeListener(pcl);
 	}
 	
 	/**
 	 * Removes the specified listener.
 	 * @param pcl the listener to remove.
 	 */
 	public void removeListener(PropertyChangeListener pcl) {
 		this.pcs.removePropertyChangeListener(pcl);
 	}
 }
