 package ch.bfh.ti.projekt1.sokoban.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import ch.bfh.ti.projekt1.sokoban.controller.BoardController;
 import ch.bfh.ti.projekt1.sokoban.model.Board;
 import ch.bfh.ti.projekt1.sokoban.model.Field;
 import ch.bfh.ti.projekt1.sokoban.model.FieldState;
 import ch.bfh.ti.projekt1.sokoban.model.Position;
 import ch.bfh.ti.projekt1.sokoban.view.BoardView;
 import ch.bfh.ti.projekt1.sokoban.view.BoardView.Mode;
 import ch.bfh.ti.projekt1.sokoban.xml.Column;
 import ch.bfh.ti.projekt1.sokoban.xml.Level;
 import ch.bfh.ti.projekt1.sokoban.xml.Row;
 
 /**
  * Provides functions for loading and saving levels, collecting information
  * about existing levels and user progress
  * 
  * @author svennyffenegger
  * @since 04/11/13 21:16
  */
 public class LevelService {
 	private static final Logger LOG = Logger.getLogger(LevelService.class);
 
 	private static LevelService instance;
 
	private char DEL = File.separatorChar;
 
 	private LevelService() {
 	}
 
 	/**
 	 * singleton getter
 	 * 
 	 * @return instance
 	 */
 	public static synchronized LevelService getInstance() {
 		if (instance == null) {
 			instance = new LevelService();
 		}
 		return instance;
 	}
 
 	/**
 	 * Loads the level definition from the file
 	 * 
 	 * @param file
 	 *            to load definition from
 	 * @return the controller of the game with model and views
 	 * @throws LevelMisconfigurationException
 	 */
 	public BoardController getLevel(File file)
 			throws LevelMisconfigurationException {
 		// load level definition itself from the xml file
 		Level level = XmlService.getInstance().getLevelFromFile(file);
 
 		Position startPos = new Position(level.getStartPosition().getColumn(),
 				level.getStartPosition().getRow());
 
 		/**
 		 * create model
 		 */
 		Board board = new Board(XmlService.getInstance().getMaxColumnCount(
 				level.getRow()), level.getRow().size(), startPos);
 		// for each field set the type
 		for (Row rowType : level.getRow()) {
 			for (Column columnType : rowType.getColumn()) {
 				// convert xml type to enum
 				Field field = new Field(FieldState.parseXmlFieldType(columnType
 						.getType()));
 				board.setField(columnType.getId(), rowType.getId(), field);
 			}
 		}
 		board.setLevelName(level.getName());
 		board.setUuid(level.getUuid());
 
 		// if this level has been started before, then set the moves
 		if (level.getMoves() != null) {
 			board.setDiamondMoveCounter(level.getMoves());
 		}
 
 		/**
 		 * create controller and view
 		 */
 		BoardController boardController = new BoardController();
 		BoardView boardView = new BoardView(board, boardController,
 				board.getPosition(), level.getName(), Mode.WALKABLE);
 
 		boardController.setModel(board);
 		boardController.setView(boardView);
 
 		return boardController;
 	}
 
 	/**
 	 * Reads all the UUID from the levels
 	 * 
 	 * @return Map with Key(UUID) and Value(Level name)
 	 */
 	public Map<String, String> getLevelNameUUIDMap() {
 		Map<String, String> uuidNameMap = new HashMap<>();
 		List<String> filesNames = fileList(CoreConstants
 				.getProperty("game.levelspath"));
 		for (String file : filesNames) {
 			try {
 				Level level = XmlService.getInstance().getLevelFromFile(
 						new File(file));
 				uuidNameMap.put(level.getUuid(), level.getName());
 			} catch (LevelMisconfigurationException e) {
 				// there is something wrong with this level, but we dont want to
 				// abort this method
 				LOG.error(e);
 			}
 		}
 		return uuidNameMap;
 	}
 
 	/**
 	 * Reads the contents of the directory
 	 * 
 	 * @param directory
 	 * @return List with all xml files inside the directory
 	 */
 	private List<String> fileList(String directory) {
 		List<String> fileNames = new ArrayList<>();
 		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
 				Paths.get(directory), "*.xml")) {
 			for (Path path : directoryStream) {
 				fileNames.add(path.toString());
 			}
 		} catch (IOException ex) {
 		}
 		return fileNames;
 	}
 
 	/**
 	 * Saves the level progress for a player
 	 * 
 	 * @param board
 	 * @param player
 	 * @throws LevelMisconfigurationException
 	 */
 	public void saveLevelProgress(Board board, String player)
 			throws LevelMisconfigurationException {
 		File parentFolder = new File(CoreConstants.getProperty("game.basepath")
 				+ player + DEL
 				+ CoreConstants.getProperty("game.folder.progress"));
 		XmlService.getInstance().saveLevel(board, parentFolder);
 	}
 
 	/**
 	 * Search for all profiles inside the root folder
 	 * 
 	 * @return list with all profile names
 	 */
 	public List<String> getProfiles() {
 		List<String> dirList = new ArrayList<>();
 		try (DirectoryStream<Path> stream = Files.newDirectoryStream(
 				Paths.get(CoreConstants.getProperty("game.basepath")),
 				new DirectoryStream.Filter<Path>() {
 
 					@Override
 					public boolean accept(Path entry) throws IOException {
 						return Files.isDirectory(entry);
 					}
 				})) {
 			for (Path entry : stream) {
 				dirList.add(entry.getFileName().toString());
 			}
 		} catch (IOException e) {
 			LOG.error(e);
 		}
 
 		return dirList;
 	}
 
 	/**
 	 * Loads a progress for the user
 	 * 
 	 * @param user
 	 * @param levelName
 	 * @return the controller for the loaded level
 	 * @throws LevelMisconfigurationException
 	 */
 	public BoardController getLevelProgressForUser(String user, String levelName)
 			throws LevelMisconfigurationException {
 		Level level = XmlService.getInstance().getLevelFromFile(
 				new File(levelName));
 		String path = CoreConstants.getProperty("game.basepath") + user + DEL
 				+ CoreConstants.getProperty("game.folder.progress");
 
 		for (String fileStr : fileList(path)) {
 			try {
 				Level progress = XmlService.getInstance().getLevelFromFile(
 						new File(fileStr));
 				if (progress.getUuid().equals(level.getUuid())) {
 					return getLevel(new File(fileStr));
 				}
 			} catch (LevelMisconfigurationException e) {
 				LOG.error(e);
 
 			}
 		}
 
 		// if we get here, the level could not be loaded
 		throw new LevelMisconfigurationException(
 				"Fehler: Der angeforderte Fortschritt konnte nicht geladen werden! Prüfen Sie die Konfiguration.");
 	}
 
 }
