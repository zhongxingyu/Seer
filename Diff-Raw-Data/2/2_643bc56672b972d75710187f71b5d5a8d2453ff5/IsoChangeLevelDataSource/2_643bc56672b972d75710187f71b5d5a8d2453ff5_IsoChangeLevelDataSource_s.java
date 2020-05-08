 package ui.isometric.datasource;
 
 import game.GameWorld;
 import game.Level;
 
 /**
  * A data source which allows you to change the level it is drawing
  * 
  * @author melby
  *
  */
 public class IsoChangeLevelDataSource extends IsoGameModelDataSource {
 	private int level;
 	
 	/**
 	 * Create a IsoChangeLevelDataSource with the default level (0)
 	 * @param model
 	 */
 	public IsoChangeLevelDataSource(GameWorld model) {
 		this(model, 0);
 	}
 	
 	/**
 	 * Create a IsoChangeLevelDataSource with the given world and level
 	 * @param model
 	 * @param startLevel
 	 */
 	public IsoChangeLevelDataSource(GameWorld model, int startLevel) {
 		super(model);
 		level = startLevel;
 	}
 
 	@Override
 	public Level level() {
		return world().level(0);
 	}
 	
 	/**
 	 * Get the level number
 	 * @return
 	 */
 	public int levelNumber() {
 		return level;
 	}
 
 	/**
 	 * Set the level to display
 	 * @param l
 	 */
 	public void setLevel(Level l) {
 		level = l.z();
 	}
 	
 	/**
 	 * Set the level to display
 	 * @param l
 	 */
 	public void setLevel(int l) {
 		level = l;
 	}
 	
 	/**
 	 * Move the renderer up a level
 	 */
 	public void goUp() {
 		setLevel(levelNumber()+1);
 	}
 	
 	/**
 	 * Move the renderer down a level
 	 */
 	public void goDown() {
 		setLevel(levelNumber()-1);
 	}
 
 	@Override
 	public boolean levelIsDark() {
 		return false;
 	}
 }
