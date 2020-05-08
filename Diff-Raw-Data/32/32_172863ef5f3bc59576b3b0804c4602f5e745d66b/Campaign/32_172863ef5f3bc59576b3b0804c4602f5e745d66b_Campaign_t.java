 package singleplayer;
 
 import java.util.ArrayList;
 
 import map.Map;
 
 /**
  * Stores sequences of map names ( = level) and the corresponding world map.
  * Offers static method readCampaignFromFile to load previously stored campaign
  * objects from a text file.
  * 
  * @author tohei
  * @see singleplayer.WorldMap
  */
 public class Campaign {
 
 	/**
 	 * Constant needed for the parsing process.
 	 */
 	private static final String SEQUENCE_INDICATOR = "seq";
 	/**
 	 * Constant needed for the parsing process.
 	 */
 	private static final String WORLDMAP_INDICATOR = "wm";
 
 	/**
 	 * An ArrayList of levels; each level being another ArrayList of
 	 * StoryMapContainers
 	 */
 	private ArrayList<ArrayList<StoryMapContainer>> levels;
 
 	/**
 	 * Flag indicating the status of a particular campaign object. If every
 	 * level has been activated and completed successfully, this flag will be
 	 * set to true.
 	 */
 	private boolean campaignFinished;
 	/**
 	 * Keeps track of the maps in a MapSequence.
 	 */
 	private int mapCounter;
 	/**
 	 * Every campaign needs a WorldMap to visualize the player's progress.
 	 */
 	private WorldMap worldMap;
 
 	/**
 	 * Construct a new Campaign object from a series of MapSequences and a
 	 * WorldMap.
 	 * 
 	 * @param levels
 	 *            list of MapSequences (sequences of map names).
 	 * @param worldMap
 	 *            a WorldMap object.
 	 */
 	public Campaign(ArrayList<ArrayList<StoryMapContainer>> levels,
 			WorldMap worldMap) {
 		if (levels == null || worldMap == null) {
 			throw new IllegalArgumentException();
 		} else {
 			this.levels = levels;
 			this.worldMap = worldMap;
 			campaignFinished = false;
 			mapCounter = 0;
 		}
 	}
 
 	/**
 	 * Returns map object that is being referred to by the world map counters.
 	 * 
 	 * @return current map
 	 */
 	public Map getCurrentMap() {
 		return new Map(levels.get(worldMap.getSelectedLevel()).get(mapCounter)
 				.getMapName());
 	}
 
 	/**
 	 * Returns textual introduction to current map if there is one and if it has
 	 * not yet been displayed. Returns null otherwise.
 	 * 
 	 * @return textual introduction to current map
 	 */
 	public String[] getIntroToCurrentMap() {
 		StoryMapContainer currentMap = levels.get(worldMap.getSelectedLevel())
 				.get(mapCounter);
 
 		if (!currentMap.introWasShown()) {
 			currentMap.setIntroShown(true);
 			return currentMap.getIntroMessage();
 		}
 		return null;
 	}
 
 	public WorldMap getWorldMap() {
 		return worldMap;
 	}
 
 	/**
 	 * Called by the LevelManager. Returns boolean value: <br>
 	 * <ul>
 	 * <LI>true - map counter has been successfully incremented</LI>
 	 * <LI>false - map counter could not be incremented => there were no more
 	 * maps left in this particular level</LI>
 	 * </ul>
 	 * 
 	 * As long as there is a level remaining, update level progress, otherwise
 	 * turn campaignFinished to true.<br>
 	 * <br>
 	 * 
 	 * Providing these return values, the LevelManager is able to decide whether
 	 * to ask for the next map or to initiate the WorldMap.
 	 */
 	public boolean updateCounters() {
 		if (mapCounter < levels.get(worldMap.getSelectedLevel()).size() - 1) {
 			mapCounter++;
 			return true;
 		} else {
 			if (worldMap.getSelectedLevel() == worldMap.getMaxLevelAccessible()) {
 				if (worldMap.getMaxLevelAccessible() == levels.size() - 1) {
 					campaignFinished = true;
 				} else {
 					worldMap.setMaxLevelAccessible(worldMap
 							.getMaxLevelAccessible() + 1);
					mapCounter = 0;
 				}
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Check for campaign status.
 	 * 
 	 * @return campaignFinished flag
 	 */
 	public boolean isFinished() {
 		return campaignFinished;
 	}
 
 	/**
 	 * Simple Container class storing a map name and an introductory text
 	 * message. Does also contain a boolean variable that may be used as a flag
 	 * to determine if the intro has been displayed yet.
 	 * 
 	 * @author tohei
 	 * 
 	 */
 	public static class StoryMapContainer {
 		private String mapName;
 		private String[] introMessage;
 		private boolean introShown;
 
 		public StoryMapContainer(String mapName, String[] introMessage) {
 			this.mapName = mapName;
 			this.introMessage = introMessage;
 			introShown = false;
 		}
 
 		public String getMapName() {
 			return mapName;
 		}
 
 		public String[] getIntroMessage() {
 			return introMessage;
 		}
 
 		public void setIntroShown(boolean introShown) {
 			this.introShown = introShown;
 		}
 
 		public boolean introWasShown() {
 			return introShown;
 		}
 	}
 }
