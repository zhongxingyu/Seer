 package de.tu_darmstadt.gdi1.samegame.model;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import java.util.Date;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Vector;
 
 import java.util.regex.Pattern;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 import javax.swing.undo.UndoManager;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 
 import javax.xml.transform.dom.DOMSource;
 
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.xml.sax.SAXException;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.LevelNotLoadedFromFileException;
 import de.tu_darmstadt.gdi1.samegame.exceptions.ParameterOutOfRangeException;
 import de.tu_darmstadt.gdi1.samegame.exceptions.WrongLevelFormatException;
 
 import de.tu_darmstadt.gdi1.samegame.model.highscore.Highscore;
 import static de.tu_darmstadt.gdi1.samegame.model.highscore.Highscore.HIGHSCORE_ENTRY;
 
 /**
  * The Model of the MVC Design pattern. <br><br>
  *
  * It contains the level data and implements it's buisiness logic, which 
  * means that it nows how to change its state. If this state is 
  * changed, the Model invokes a ChangeEvent, which is listened by the 
  * View class, so that the View can update it's content 
  * ({@link #updateState(Byte[][], int) updateState(...)}). <br><br>
  *
  * It is also an {@link javax.swing.undo.UndoManager UndoManager} for 
  * the game state, which means it can {@link #undo() undo} and 
  * {@link #redo() redo} the field and point state of a game. <br><br>
  *
  * A level can be parsed from a valid {@link #loadLevelFromString(String)} 
  * string or from a file {@link #restoreLevel(File)}. <br><br>
  *
  * Also the current level state could be 
  * {@link #storeLevelState(File, boolean) saved} and 
  * {@link #restoreLevelState(File) loaded} from files.
  */
 @SuppressWarnings("serial")
 public class Level extends UndoManager{
 
 	////////////////////////Class/Attributes//////////////////////////
 	/**
 	 * the time to finish the level
 	 */
 	private int targetTime;
 
 
 	/**
 	 * minimal number of Stones which can be deleted
 	 */
 	private int minStones;
 
 
 	/**
 	 * the original game state, in order to reset the current state 
 	 * to it.
 	 */
 	private GameState ORIGINAL_LEVEL_STATE;
 
 
 	/**
 	 * the current game state. It contains the field state and the
 	 * number of points the player reached. Is kept under Undo/Redo
 	 * history by the CompoundEdit-Manager
 	 */
 	private GameState currentGameState;
 
 
 	/**
 	 * the listener from the view, which is used to tell the view to
 	 * update.
 	 */
 	private ChangeListener changeListener;
 
 
 	/**
 	 * The Highscore of the Level. It is read from the level file,
 	 * during runtime stored in this attribute and can been rewritten 
 	 * to the level file after it is updated
 	 */
 	private Highscore highscore;
 
 
 	/**
 	 * Pattern for additional level information.
 	 */
 	private static final String ADDITIONAL_LEVEL_INF = 
 		"###(target_time:\\d*|min_stones:\\d*)"
 	   +"(\\|(target_time:\\d*|min_stones:\\d*))?";
 
 	
 	/**
 	 * Stop watch from apache common to get the time a user needed to
 	 * finish a level. It's also used to pause if the user want's to
 	 * break
 	 */
 	private MyStopWatch watch;
 
 
 	/**
 	 * if the first click was performed. True if this is the first
 	 * click. False if this is a later click
 	 */
 	private boolean firstClick;
 
 
 	/**
 	 * Stores the file from which the level information was loaded.
 	 * Is initialized in the function {@link #restoreLevel(File)}.
 	 */
 	private File loadedLevel;
 
 
 	////////////////////////Class/Constructors////////////////////////
 	/**
 	 * Class constructor to instantiate a level without a field state
 	 *
 	 * @param changeListener the listener from the view
 	 */
 	public Level(final ChangeListener changeListener){
 		currentGameState = new GameState(null, 0);
 
 		init(changeListener);
 	}
 
 
 	/**
 	 * Class constructor to instantiate a level and read the level date
 	 * from a file on the disc
 	 *
 	 * @param f the file which contains the level date
 	 * @param changeListener the listener from the view
 	 *
 	 * @throws FileNotFoundException if the file could not been found
 	 * @throws WrongLevelFormatException if the file contains a invalid
 	 * level
 	 * @throws IOException if a IO-exception occurs during the reading
 	 * of the file
 	 */
 	public Level(final File f, final ChangeListener changeListener)
 		throws FileNotFoundException, WrongLevelFormatException, 
 			   IOException{
 
 		restoreLevel(f);
 
 		init(changeListener);
 	}
 
 
 	/**
 	 * Class constructor to instantiate a level and set specific
 	 * attributes given as params
 	 *
 	 * @param targetTime the time in which the player should finish the
 	 * level
 	 * @param minStones the number of stones needed to apply an delete
 	 * operation
 	 * @param field the field which is assigned to the level state
 	 * @param changeListener the listener from the view
 	 */
 	public Level(final int targetTime, 
 				 final int minStones,
 				 final Byte[][] field, 
 				 final ChangeListener changeListener){
 
 		this.targetTime = targetTime;
 		this.minStones = minStones;
 
 		currentGameState = new GameState(field, 0);
 
 		init(changeListener);
 	}
 
 
 	/**
 	 * Construct a level from a String. The function parses the string.
 	 * if the level isn't valid, it throws a WrongLevelFormatException
 	 *
 	 * @param levelstring the string to be load
 	 *
 	 * @throws WrongLevelFormatException
 	 */
 	public Level(final String levelstring, 
 				 final ChangeListener changeListener) 
 		throws WrongLevelFormatException{
 
 		loadLevelFromString(levelstring);
 		
 		init(changeListener);
 	}
 
 
 	/**
 	 * helping function to initialize a few class attributes
 	 *
 	 * @param changeListener the listener from the viewer class
 	 */
 	private void init(final ChangeListener changeListener){
 		
 		watch = new MyStopWatch();
 
 		firstClick = true;
 
 		ORIGINAL_LEVEL_STATE = (GameState) currentGameState.clone();
 
 		this.changeListener = changeListener;
 		this.changeListener.stateChanged(new ChangeEvent(this));
 	
 		if(this.currentGameState.getFieldState() != null)
 			this.addEdit(currentGameState);
 
 	}
 
 
 
 	////////////////////////Getters//&//Setters///////////////////////
 	/**
 	 * get the points the player reached at the moment
 	 *
 	 * @return the points the player reached at the moment
 	 */
 	public double getPoints(){
 		return currentGameState.getPoints();
 	}
 
 
 	/**
 	 * gets a copy of the current field state
 	 *
 	 * @return a copy of the current field state
 	 */
 	public Byte[][] getFieldState(){
 		return currentGameState.getFieldState();
 	}
 
 
 	/**
 	 * get the height of the level field
 	 *
 	 * @return the field height
 	 *
 	 * @throws NullPointerException if no level is loaded
 	 */
 	public int getFieldHeight(){
 		return ORIGINAL_LEVEL_STATE.getFieldState().length;
 	}
 
 
 	/**
 	 * get the width of the level field
 	 *
 	 * @return the field width
 	 *
 	 * @throws NullPointerException if no level is loaded
 	 */
 	public int getFieldWidth(){
 		return ORIGINAL_LEVEL_STATE.getFieldState()[0].length;
 	}
 	
 
 
 	/**
 	 * Get a highscore list in form of a two dimensional String array.
 	 * <ol>
 	 * 	<li>Column: points</li>
 	 * 	<li>Column: remaining time</li>
 	 * 	<li>Column: date</li>
 	 * 	<li>Column: player's name</li>
 	 * </ol>
 	 *
 	 * @return a String representation in the above described form
 	 */
 	public String[][] getHighscore(){
 		if(highscore != null)
 			return highscore.getHighscoreEntrys();
 		else
 			return null;
 	}
 
 
 	/**
 	 * insert a new highscore into the highscore-list of the level
 	 *
 	 * @param playername the name which shall appear in the highscore
 	 * @param remTime remaining time in seconds until target time 
 	 * is reached (which can be negative)
 	 * @param creationDate timestamp of when the game was finished
 	 * @param points the score which was achieved
 	 */
 	public void insertHighscore(final String playername, 
 								final double remTime, 
 								final Date creationDate, 
 								final double points){
 			if(highscore == null)
 				highscore = new Highscore();
 
 			highscore.insertHighscore(playername,
 									  remTime,
 									  creationDate,
 									  points);
 	}
 
 	/**
 	 * insert a new highscore into the highscore-list of the level
 	 *
 	 * @param playername the name which shall appear in the highscore
 	 */
 	public void insertHighscore(final String playername){
 			if(highscore == null)
 				highscore = new Highscore();
 
 			highscore.insertHighscore(playername,
 									  watch.getTime() - targetTime,
 									  new Date(),
 									  currentGameState.getPoints());
 			System.out.println(highscore.toString());
 	}
 
 
 
 	/**
 	  * resets the highscore
 	  */
 	public void resetHighscore(){
 		highscore.resetHighscore();
 	}
 
 	/**
 	 * sets the target time in which the player has to finish the level
 	 *
 	 * @param targetTime the time the player has to finish the level
 	 */
 	void setTargetTime(final int targetTime){
 		this.targetTime = targetTime;
 	}
 
 
 	/**
 	 * gets the target time in which the player has to finish the level
 	 *
 	 * @return the time the player has to finish the level
 	 */
 	public int getTargetTime(){
 		return this.targetTime;
 	}
 
 
 	/**
 	 * gets the elapsed time for the current game
 	 *
 	 * @return the elapsed time for the current game
 	 */
 	public long getElapsedTime(){
 		return watch.getTime();
 	}
 
 
 	/**
 	 * sets the minimal number of Stones needed to cause remove 
 	 * operation
 	 *
 	 * @param minStones the minimal number of Stones for a Remove
 	 */
 	void setMinStones(final int minStones){
 		this.minStones = minStones;
 	}
 
 
 	/**
 	 * gets the minimal number of Stones needed to cause remove 
 	 * operation
 	 *
 	 * @return the minimal number of Stones for a Remove
 	 */
 	public int getMinStones(){
 		return this.minStones;
 	}
 
 
 	/**
 	 * return a string representation of the extra level informations.
 	 * It's in the form:<br>
 	 *
 	 * <code>###target_time:[number]|min_stones:[number]</code>
 	 * 
 	 * @return the string with the additional level informations
 	 */
 	public String getAdditionalLevelInf(){
 		StringBuffer out = new StringBuffer();
 		out.append("###target_time:").append(targetTime);
 		out.append("|min_stones:").append(minStones);
 		return out.toString();
 	}
 
 
 	/**
 	 * returns a string containing the level state information.
 	 * the level must been loaded from a file, else an exception
 	 * is thrown.
 	 * It's in the form: <br>
 	 *
 	 * <code>
 	 * [current level field state]<br>
 	 * ###loaded_level:[absolute path to file]|reached_points:[number]
 	 * |elapsed_time:[number]
 	 * </code> 
 	 *
 	 * @return the string which contains current level state information
 	 *
 	 * @throws LevelNotLoadedFromFileException if the level wasn't loaded
 	 * from a file
 	 */
 	public String getLevelStateInf() 
 		throws LevelNotLoadedFromFileException{
 		if(loadedLevel == null)
 			throw new LevelNotLoadedFromFileException(
 					"so the level state information can't been print");
 
 		String stateInf;
 
 		stateInf = ORIGINAL_LEVEL_STATE.toString() + "\n"
 			+ "###loaded_level:" + this.loadedLevel.getAbsolutePath()
 			+   "|reached_points:" + (long) currentGameState.getPoints()
 			+   "|elapsed_time:"+ (long) watch.getTime();
 		
 		return stateInf;
 	}
 
 
 	/**
 	 * returns a string representation of the original level fieldstate
 	 * without extra level informations.
 	 *
 	 * @return the string which contains a representation of the level
 	 * fieldstate
 	 */
 	public String getOrigLevelState(){
 		return ORIGINAL_LEVEL_STATE.toString();
 	}
 
 
 	/**
 	 * returns a string representation of the current level fieldstate
 	 * without extra level informations.
 	 *
 	 * @return the string which contains a representation of the level
 	 * fieldstate
 	 */
 	public String getCurrentLevelState(){
 		return currentGameState.toString();
 	}
 
 	/**
 	 * returns the highscore-list of the level
 	 * 
 	 * @return the highscore-list of the level
 	 */
 	public String getHighscorelist(){
 		if(highscore != null)
 			return highscore.toString();
 		else
 			return null;
 	}
 	
 	
 	
 	////////////////////////Class/Operations//////////////////////////
 	/**
 	 * Resets the current level state and delete the undo/redo history
 	 */
 	public void restartLevel(){
 
 		currentGameState = (GameState) ORIGINAL_LEVEL_STATE.clone();
 
 		this.discardAllEdits();
 		this.addEdit(currentGameState);
 
 		this.changeListener.stateChanged(new ChangeEvent(this));
 	}
 
 
 	/**
 	 * undo to the last game state
 	 *
 	 * @throws CannotUndoException if undo is impossible
 	 */
 	@Override
 	public void undo() throws CannotUndoException{
 		super.undo();
 		
 		if(this.editToBeUndone() instanceof GameState){
 			this.currentGameState = 
 			(GameState) this.editToBeUndone();
 			changeListener.stateChanged(new ChangeEvent(this));
 		} else
 			throw new CannotUndoException();
 		
 	}
 
 
 	/**
 	 * redo to the previous undone action
 	 *
 	 * @throws CannotredoException if redo is impossible
 	 */
 	@Override
 	public void redo() throws CannotRedoException{
 		super.redo();
 			
 		if(this.editToBeUndone() instanceof GameState){
 			this.currentGameState = 
 			(GameState) this.editToBeUndone();
 			changeListener.stateChanged(new ChangeEvent(this));
 		} else
 			throw new CannotRedoException();
 		
 	}
 
 	/**
 	 * generates a new level according to the given parameters
 	 * @param width the number of columns
 	 * @param height the number of rows
 	 * @param numberOfColors the number of different stone-colors 
 	 * @param minStones the minimal number of same-colored stones, which can be removed
 	 * @throws IllegalArgumentException if one of the parameters is out of range
 	 */
 	public void generateLevel(final int width, 
 							  final int height, 
 							  final int numberOfColors, 
 							  final int minStones) 
 		throws IllegalArgumentException{
 
 		if(		width < 6 		  || width > 30 
 			|| height < 5		  || height > 20
 			|| numberOfColors < 2 || numberOfColors > 5
 			|| minStones < 2	  || minStones > 5)
 
 			throw new IllegalArgumentException("One of the given parameter is out of range");
 
 		else{
 
 			Random r = new Random();
 			Byte[][] level = new Byte[height][width];
 
 			do{
 				for(int i = 0; i<height; i++)
 					for(int j=0; j<width; j++)
 						level[i][j] = 
 							new Byte((byte) (1 + r.nextInt(numberOfColors)));
 			}while(!validateSemantical(level, numberOfColors, minStones));
 
 			this.targetTime = width * height;
 			this.minStones = minStones;
 
 			currentGameState = new GameState(level, 0);
 			ORIGINAL_LEVEL_STATE = (GameState) currentGameState.clone();
 			this.addEdit(currentGameState);
 		}
 	}
 
 	/**
 	 * generates a random level.
 	 *
 	 * The random values are:
 	 * <ul>
 	 * 	<li>level-width [6, 30]</li>
 	 * 	<li>level-height [5, 20]</li>
 	 * 	<li>number of Colors [2, 5]</li>
 	 * 	<li>minimum number of Stones to remove [2, 5]</li>
 	 * </ul>
 	 */
 	public void generateLevel(){
 		Random r = new Random();
 		int height = 5 + r.nextInt(16);
 		int width = 6 + r.nextInt(25);
 
 		int numOfColors = 2 + r.nextInt(4);
 		this.minStones = 2 + r.nextInt(4);
 
 		this.targetTime = height * width;
 		
 		generateLevel(width, height, numOfColors, minStones);
 	}
 	
 
 	/**
 	 * check a level for semantical correctness, that means that it
 	 * returns false if there are not enough stones of each color or 
 	 * none removable stonegroup.
 	 *
 	 * @return true if it is a semantical correct level
 	 */
 	private static boolean validateSemantical(final Byte[][] level, 
 											  final int numOfCol, 
 											  final int minStones){
 
 		int rows = level.length;
 		int cols = level[0].length;
 
 		int[] colorSpread = new int[numOfCol];
 		// check if the level just contains values in range [1, 5]
 		for(int i = 0; i<rows; i++)
 			for(int j=0; j<cols; j++)
 				colorSpread[level[i][j]-1]++;
 		
 		for(int i = 0; i<colorSpread.length; i++)
 			if(colorSpread[i] < minStones)
 				return false;
 
 		// check if the level contains minimum one removable element
 		if(isFinished(level, minStones))
 			return false;
 
 		return true;
 	}
 
 
 	/**
 	 * validates if a level string is syntactical correct.
 	 *
 	 * @see #loadLevelFromString(String) for the explenation of the 
 	 * level format
 	 *
 	 * @param levelString
 	 *
 	 * @throws WrongLevelFormatException if the level is not in the 
 	 * described format
 	 */
 	public static void validateSyntactical(final String levelString)
 			throws WrongLevelFormatException{
 		Scanner s;
 		LineNumberReader line = 
 			new LineNumberReader(
 					new StringReader(levelString));
 
 		s = new Scanner(line);
 
 		// get the length of the first line
 		if(!s.hasNextLine())			
 			throw new WrongLevelFormatException(
 				"Wrong level format while parsing level from "
 				+"string: no level informations available");
 			
 			
 		String parsedLine = s.nextLine();
 		int cols = parsedLine.length();
 		
 		if(cols == 0)			
 			throw new WrongLevelFormatException(
 				"Wrong level format while parsing level from "
 				+"string: no level informations available");
 
 		// the following level line must have the length of the 
 		// first line
 		final String LEVEL_LINE = "^[0-5]{"+cols+"}";
 
 		// check level lines
 		while(true){
 			
 			if(Pattern.matches("^###.*", parsedLine))
 				break;
 			
 			if(!Pattern.matches(LEVEL_LINE, parsedLine))
 				throw new WrongLevelFormatException(
 						"Wrong level format while parsing level from "
 						+"string in line "+line.getLineNumber()+": "
 						+parsedLine);
 			
 			if(s.hasNextLine())
 				parsedLine = s.nextLine();
 			else break;
 		}
 
 		if(Pattern.matches(ADDITIONAL_LEVEL_INF, parsedLine)){
 			validateAdditionalLevelInf(parsedLine);
 			
 			if(s.hasNextLine()){
 				String rest = s.nextLine();
 				if(!rest.equals("null")){
 					if(Pattern.matches(HIGHSCORE_ENTRY, rest)){
 						while(s.hasNextLine())
							rest += s.nextLine() + "\n";
 						Highscore.validate(rest);
 					}else
 						throw new WrongLevelFormatException(
 								"Wrong level format while parsing level from "
 								+"string: unexcepted characters in line "
 								+line.getLineNumber());
 				}
 			}
 			
 		}else if(Pattern.matches(HIGHSCORE_ENTRY, parsedLine)){
 			String rest = parsedLine + "\n";
 			while(s.hasNextLine())
 				rest += s.nextLine() + "\n";
 			Highscore.validate(rest);
 			
 		}else if(!Pattern.matches(LEVEL_LINE, parsedLine)){
 			throw new WrongLevelFormatException(
 					"Wrong level format while parsing level from "
 					+"string: unexcepted characters in line "
 					+line.getLineNumber());
 		}
 
 		s.close();
 	}
 
 
 	/**
 	 * check if the additional level information have the right format
 	 * and if there are no duplicates.
 	 *
 	 * @param addLevelInf the line with the additional level 
 	 * informations
 	 *
 	 * @throws WrongLevelFormatException if the additional level 
 	 * informations are not in the excepted format
 	 */
 	public static void validateAdditionalLevelInf(
 			final String addLevelInf)
 		throws WrongLevelFormatException{
 
 		if(!Pattern.matches(ADDITIONAL_LEVEL_INF, addLevelInf))
 			throw new WrongLevelFormatException(
 					"Wrong level format, the given additional level "
 					+ "informations are not valide");
 
 		Scanner s = new Scanner(addLevelInf);
 
 		s.skip("###");
 
 		String[] infos = new String[2];
 		int[] values = new int[2];
 		
 		// scanning informations
 		for(int i=0; i<2; i++){
 			try{
 				infos[i] = s.findInLine("(\\w|_)*");
 				s.skip(":");
 				values[i] = Integer.parseInt(s.findInLine("\\d*"));
 			}catch(NoSuchElementException e){
 				throw new WrongLevelFormatException(
 						"wrong level format while parsing additional "
 						+"Level informations from string :"
 						+ e.getMessage());
 			}catch(NumberFormatException e){
 				throw new WrongLevelFormatException(
 						"wrong level format while parsing additional "
 						+"Level informations from string :"
 						+ e.getMessage());
 			}
 			if(s.hasNext("\\|.*"))
 				s.skip("\\|");
 			else
 				break;
 		}
 
 		// test if min_stone have a value between[2,2^32]
 		if(infos[0].equals("min_stones") && values[0] < 2)
 			throw new WrongLevelFormatException(
 					"wrong level format while parsing additional Level"
 					+" informations from string: min_stone must be have"
 					+" a value of [2,2^32]:"+ values[0]);
 		
 		if(infos[1] != null && infos[1].equals("min_stones") && values[1] < 2)
 			throw new WrongLevelFormatException(
 					"wrong level format while parsing additional Level"
 					+" informations from string: min_stone must be have"
 					+" a value of [2,2^32]:"+ values[1]);
 
 		// if there are duplicated informations
 		if(infos[1] != null && infos[0].equals(infos[1]))
 			throw new WrongLevelFormatException(
 					"wrong level format while parsing additional Level"
 					+" informations from string: dublicated level "
 					+"informations");
 	}
 
 
 	/**
 	 * parses a level from a String and stores the informations in the 
 	 * object.
 	 *
 	 * the string !!!must!!! have the following format:<br>
 	 * <ol>
 	 * 	<li>
 	 * 		the level field must be a square and must consist of values 
 	 * 		between 1 and 5.<br>
 	 * 		Example: <br>
 	 * 		<code>
 	 * 			423234232212<br>
 	 * 			543345553234<br>
 	 * 			345342125352<br>
 	 * 			142341253414<br>
 	 * 			152354243123
 	 * 		</code>
 	 * 	</li>
 	 *
 	 * 	<li>
 	 * 		The additional level information must have the following format:<br>
 	 *
 	 * 		a "###" at the beginning of the line, then one or two of the 
 	 * 		following informations: target_time:[number] AND/OR 
 	 * 		min_stones:[number], separated with a "|".<br>
 	 *
 	 * 		Example: <code>###target_time:888|min_stones:4</code>
 	 * 	</li>
 	 *
 	 * 	<li>
 	 *		The highscore entries must have the following format:<br>
 	 *		a "###" at the beginning of the line, then each of the following
 	 *		informations:<br>
 	 *		name:[string] AND points:[number] AND date:dd.mm.yy HH;MM;SS AND 
 	 *		rem_time:[number]<br>
 	 *		separated with a "|".<br>
 	 *		Example: 
 	 *		<code>###name:xyz|points:888|date:23.05.2010 23;59;12|rem_time:123</code>
 	 *	</li>	
 	 * </ol>
 	 *
 	 * @param levelString the string which contains the whole level
 	 *
 	 * @throws WrongLevelFormatException if the level is not in the 
 	 * described format
 	 */
 	public void loadLevelFromString(final String levelString) 
 		throws WrongLevelFormatException{
 
 		// return with an exception if it is an syntactical incorrect
 		// Level	
 		validateSyntactical(levelString);
 
 		Scanner s;
 		LineNumberReader line = 
 			new LineNumberReader(
 					new StringReader(levelString));
 
 		s = new Scanner(line);
 		
 		Vector<Byte[]> parsedLevel = new Vector<Byte[]>();
 
 		// get the length of the first line
 		String parsedLine = s.nextLine();
 		int cols = parsedLine.length();
 		parsedLevel.add(parseByteDigits(parsedLine));
 
 		// get all other level lines
 		while(s.hasNextLine() && s.hasNext("\\d*")){
 			parsedLine = s.nextLine();
 			parsedLevel.add(parseByteDigits(parsedLine));
 		}
 
 		// sets the additional level information to default values 
 		// which are overwritten if there were scanned others
 		this.targetTime = parsedLevel.size() *cols;
 		this.minStones = 2;
 
 		// if there are additional level information or highscore entrys
 		if(s.hasNextLine()){
 
 			// if there are additional level information
 			if(s.hasNext(ADDITIONAL_LEVEL_INF))
 				parseAdditionalLevelInf(s.nextLine());
 
 			if(s.hasNextLine() && 
 					Pattern.matches(HIGHSCORE_ENTRY, parsedLine=s.nextLine())){
 				
 				String rest = parsedLine + "\n";
 				
 				while(s.hasNext())
 					rest += s.nextLine() + "\n";
 						
 				this.highscore = new Highscore(rest);
 				
 			}
 			 
 		}
 
 		// write the parsed vector to the class attribute
 		Byte[][] field = new Byte[parsedLevel.size()][cols];
 		parsedLevel.toArray(field);
 		
 		boolean emptyLevel = true;
 		for(int i=0; i<field.length; i++)
 			for(int j=0; j<field[i].length; j++)
 				if(field[i][j] != 0){
 					emptyLevel = false;
 					break;
 				}
 		
 		if(emptyLevel)
 			throw new WrongLevelFormatException(
 					"Wrong level format while parsing level from "
 					+"string: empty level");
 			
 		// let the field collaps
 		moveUp(field);
 		
 		this.currentGameState = new GameState(field, 0);
 		this.ORIGINAL_LEVEL_STATE = (GameState) currentGameState.clone();
 
 		s.close();
 	}
 
 
 	/**
 	 * parses a Level line.
 	 *
 	 * @param s a line in the level field
 	 *
 	 * @throws NumberFormatException if a char isn't parseable
 	 *
 	 * @return an byte array with each char of the string at a position
 	 * in the array
 	 */
 	private Byte[] parseByteDigits(final String s) 
 		throws NumberFormatException{
 
 		Byte[] parsedArray = new Byte[s.length()];
 		for(int i=0; i<s.length(); i++)
 			parsedArray[i] = Byte.parseByte(""+s.charAt(i));
 		return parsedArray;
 	}
 
 
 	/**
 	 * parses additional level information like the target time or the
 	 * minimal number of stones which can be removed.
 	 *
 	 * @param additionalLevelInf the string which contains the extra 
 	 * level informations
 	 *
 	 * @throws WrongLevelFormatException if the level is not in the
 	 * right format
 	 */
 	private void parseAdditionalLevelInf(final String additionalLevelInf) 
 		throws WrongLevelFormatException{
 
 		validateAdditionalLevelInf(additionalLevelInf);
 
 		String[] infos = new String[2];
 		int[] values = new int[2];
 		
 		Scanner s = new Scanner(additionalLevelInf);
 		
 		s.skip("###");
 		
 		// scanning informations
 		for(int i=0; i<2; i++){
 			infos[i] = s.findInLine("(\\w|_)*");
 			s.skip(":");
 			values[i] = Integer.parseInt(s.findInLine("\\d*"));
 			if(s.hasNext("\\|.*"))
 				s.skip("\\|");
 			else
 				break;
 		}
 		
 		for(int i=0; i<2; i++){
 			if(infos[i] != null){
 				if(infos[i].equals("target_time"))
 					this.targetTime = values[i];
 				else if(infos[i].equals("min_stones"))
 					this.minStones = values[i];
 			}
 		}
 	}
 
 
 	/**
 	 * check if the stone group at the position is bigger equal the
 	 * the minimal number of stones to remove.
 	 * Static version of the function.
 	 *
 	 * @param state the field which contains the level informations
 	 * @param minStones the minimal number of stones which can be removed
 	 * @param row the row in which a member of the stone group is located
 	 * @param col the col in which a member of the stone group is located
 	 *
 	 * @return true if the stone group is removeable
 	 */
 	public static boolean removeable(final Byte[][] state, 
 									 final int minStones, 
 									 final int row, 
 									 final int col) {
 
 		// make a copy of the field to don't change the current field state
 		Byte[][] stateCopy = new Byte[state.length][state[0].length];
 		for (int i = 0; i < state.length; i++)
 			System.arraycopy(state[i], 0, stateCopy[i], 0, state[i].length);
 
 		int rows = state.length;
 		int cols = state[0].length;
 		byte color = state[row][col];
 
 		// check if the position is in the field and not allready removed
 		if (row >= 0 && col >= 0 && row < rows && col < cols 
 			&& color != 0) {
 
 			stateCopy[row][col] = 100;
 			countableFloodFill(stateCopy, row - 1, col, color, (byte) 100);
 			countableFloodFill(stateCopy, row + 1, col, color, (byte) 100);
 			countableFloodFill(stateCopy, row, col - 1, color, (byte) 100);
 			countableFloodFill(stateCopy, row, col + 1, color, (byte) 100);
 		}
 		else
 			return false;
 
 		// count the number of stones which may be removed
 		int stonesToRemove = 0;
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				if (stateCopy[i][j] == 100)
 					stonesToRemove++;
 		return stonesToRemove >= minStones;
 
 	}
 
 
 	/**
 	 * check if the stone group at the position is bigger equal the
 	 * the minimal number of stones to remove.
 	 * Non-Static version of the function with uses object attributes to
 	 * calculate
 	 *
 	 * @param row the row in which a member of the stone group is located
 	 * @param col the col in which a member of the stone group is located
 	 *
 	 * @return true if the stone group is removeable
 	 */
 	public boolean removeable(final int row, final int col) {
 
 		Byte[][] field = currentGameState.getFieldState();
 		return removeable(field, minStones, row, col);
 	}
 
 	
 	/**
 	 * a floodfill algorithm which fills a group of stones in the field.
 	 * It changes the color of the selected stone group to the new 
 	 * color, so the new color can be count etc.
 	 *
 	 * @param state the field to operate on, <b>changes during operation!!!</b>
 	 * @param row the row in which a member of the stone group is located
 	 * @param col the col in which a member of the stone group is located
 	 * @param color the color of the stonegroup
 	 * @param newCol the new color to color the stone group with
 	 */
 	private static void countableFloodFill(Byte[][] state, 
 										   final int row, 
 										   final int col, 
 										   final byte color,
 										   final byte newCol){
 
 		int rows = state.length;
 		int cols = state[0].length;
 		if (row >= 0 && col >= 0 && row < rows && col < cols
 			&& state[row][col] == color && state[row][col] != 0) {
 
 			state[row][col] = newCol;
 			countableFloodFill(state, row - 1, col, color, newCol);
 			countableFloodFill(state, row + 1, col, color, newCol);
 			countableFloodFill(state, row, col - 1, color, newCol);
 			countableFloodFill(state, row, col + 1, color, newCol);
 		}
 	}
 
 
 	/**
 	 * a floodfill algorithm which fills a group of stones in the field.
 	 * It fills the selected stone group with 0's and count the stones
 	 * which are removed
 	 *
 	 * @param state the field to operate on, <b>changes during operation!!!</b>
 	 * @param row the row in which a member of the stone group is located
 	 * @param col the col in which a member of the stone group is located
 	 * @param color the color of the stonegroup
 	 * @param stonesRemoved the number of stones which are removed.
 	 * The object contains the number after the operation
 	 */
 	public static int removeFloodFill(Byte[][] state, 
 									   final int row, 
 									   final int col, 
 									   final byte color){
 
 		int rows = state.length;
 		int cols = state[0].length;
 		if (row >= 0 && col >= 0 && row < rows && col < cols
 			&& state[row][col] == color && state[row][col] != 0) {
 
 			state[row][col] = 0;
 			int stonesRemoved = 1;
 
 			stonesRemoved += removeFloodFill(state, row - 1, col, color);
 			stonesRemoved += removeFloodFill(state, row + 1, col, color);
 			stonesRemoved += removeFloodFill(state, row, col - 1, color);
 			stonesRemoved += removeFloodFill(state, row, col + 1, color);
 			return stonesRemoved;
 		}
 		else return 0;
 	}
 
 
 	/**
 	 * function to remove stones in the level field. The function also
 	 * performs a move up of the stones that means that the stones 
 	 * first fall down and than empty columns are deleted. At the end
 	 * of a successful remove the current level state is updated 
 	 * ({@link #updateState(Byte[][], int) updateState(...)})
 	 * and points for the operation are calculated
 	 *
 	 * <br>
 	 * Example remove operation:<br>
 	 * <code>
 	 * &ensp;v 	<br>
 	 *      121		<br>
 	 *      222&lt;	<br>
 	 *      121  	<br>
 	 *      <br>
 	 *      000		<br>
 	 *      110      <br>
 	 *      110      <br>
 	 * </code>
 	 *
 	 * @param row the row in which a member of the stone group is 
 	 * located, which is be removed.
 	 * @param col the col in which a member of the stone group is
 	 * located, which is be removed.
 	 *
 	 * @throws ParameterOutOfRangeException if the row and the col
 	 * parameter are not inside the field
 	 *
 	 * @return true if the remove operation could be performed
 	 */
 	public boolean removeStone(final int row, final int col) 
 		throws ParameterOutOfRangeException{
 
 		Byte[][] state = getFieldState();
 		int rows = state.length;
 		int cols = state[0].length;
 
 		// check if the position is inside the field
 		if (row < 0 || row >= rows)
 			throw new ParameterOutOfRangeException(
 					"the \"row\" parameter is out of possible range"
 					+"[0,"+rows+"]: "+row);
 
 		// check if the position is inside the field
 		if (col < 0 || col >= cols)
 			throw new ParameterOutOfRangeException(
 					"the \"col\" parameter is out of possible range"
 					+"[0,"+cols+"]: "+col);
 
 		// if the position is already removed
 		if (state[row][col] == 0)
 			return false;
 
 		byte color = state[row][col];
 
 		if (removeable(state, minStones, row, col)) {
 			
 			int elementsRemoved = removeFloodFill(state, row, col, color);
 			
 			// perform the fall down and empty column deletion
 			moveUp(state);
 
 			updateState(state, elementsRemoved);
 
 			if(firstClick){
 				watch.start();
 				firstClick = false;
 			}else
 				watch.resume();
 
 			return true;
 		}
 		return false;
 	}
 
 
 	/**
 	 * function to move up a field in which recently a remove operation
 	 * has happened.<br>
 	 *
 	 * Example move up operation:<br>
 	 *
 	 * <code>
 	 * &ensp;v 	<br>
 	 *      121		<br>
 	 *      222&lt;	<br>
 	 *      121  	<br>
 	 *      <br>
 	 *      000		<br>
 	 *      110      <br>
 	 *      110      <br>
 	 * </code>
 	 *
 	 * @param state the field which have to be updated
 	 */
 	public static void moveUp(Byte[][] state) {
 
 		int rows = state.length;
 		int cols = state[0].length;
 		int walkTroughRows = rows;
 		int walkTroughCols = cols;
 
 		// let the stones fall down
 		while (walkTroughRows > 0) {
 			for (int i = rows - 2; i >= 0; i--) {
 				for (int j = cols - 1; j >= 0; j--) {
 					if (state[i + 1][j] == 0 && state[i][j] != 0) {
 						state[i + 1][j] = state[i][j];
 						state[i][j] = 0;
 					}
 				}
 			}
 			walkTroughRows--;
 		}
 
 		// move non-empty columns up to delete empty columns
 		while (walkTroughCols > 0) {
 			for (int i = 0; i <= cols - 2; i++) {
 				if (state[rows - 1][i] == 0) {
 					for (int j = 0; j < rows; j++) {
 						state[j][i] = state[j][i + 1];
 						state[j][i + 1] = 0;
 					}
 				}
 			}
 			walkTroughCols--;
 		}
 	}
 
 
 	public static boolean fallDownOneField(Byte[][] state){
 
 		boolean fieldstateHasChanged = false;
 
 		int rows = state.length;
 		int cols = state[0].length;
 		// let the stones fall down
 
 		for (int i = rows - 2; i >= 0; i--) {
 			for (int j = cols - 1; j >= 0; j--) {
 				if (state[i + 1][j] == 0 && state[i][j] != 0) {
 					state[i + 1][j] = state[i][j];
 					state[i][j] = 0;
 					fieldstateHasChanged = true;
 				}
 			}
 		}
 
 		return fieldstateHasChanged;
 	}
 
 
 	/**
 	 * function to calculate if another remove operation is possible.
 	 * Non-Static version of the function works with the current level
 	 * state
 	 *
 	 * @return return true if no remove operation is possible anymore
 	 */
 	public boolean isFinished() {
 		if(currentGameState.getFieldState() == null)
 			return true;
 		else{
 			Byte[][] state = currentGameState.getFieldState();
 			boolean finished = isFinished(state, minStones);
 			if(finished)
 				watch.suspend();
 			return finished;
 		}
 	}
 
 
 	/**
 	 * function to calculate if another remove operation is possible.
 	 * Static version of the function.
 	 *
 	 * @param state the field to check
 	 * @param minStones the minimal number of stones which can be removed
 	 *
 	 * @return return true if no remove operation is possible anymore
 	 */
 	public static boolean isFinished(Byte[][] state, int minStones) {
 		int rows = state.length;
 		int cols = state[0].length;
 
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				if (state[i][j] != 0) {
 					if (removeable(state, minStones, i, j))
 						return false;
 				}
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * updates the current level state and notifies the viewer to update.
 	 *
 	 * @param state the new field state
 	 * @param removedElements the number of elements wich was removed
 	 */
 	void updateState(Byte[][] state, int removedElements){
 
 		double newPoints = calculatePoints(state, removedElements);
 
 		currentGameState = 
 			new GameState(state, newPoints);
 
 		this.addEdit(currentGameState);
 
 		// notify the viewer that the level state has changed
 		changeListener.stateChanged(new ChangeEvent(this));
 	}
 
 
 	/**
 	 * calculates the points after a remove operation.
 	 * 
 	 * @param state the new field in which the points are counted
 	 * @param removedElements the number of elements which were removed
 	 *
 	 * @return the new calculated Points
 	 */
 	public double calculatePoints(Byte[][] state, int removedElements){
 
 		double newPoints = currentGameState.getPoints();
 
 		double time =  (double)watch.getTime() /1000;
 		boolean overTime = (double)targetTime - time < 0;
 		newPoints += Level.calculatePoints(removedElements, overTime);
 
 		if(isFinished(state, minStones))
 			newPoints += calculatePointsFinished(state);
 
 		return newPoints;
 	}
 	
 
 	/**
 	 * calculates the points for a remove operation.
 	 * If the operation was performed just in time, the new points
 	 * are (removedElements)&sup2; else (removedElements/2)&sup2;<br>
 	 *
 	 * @param removedElements the number of elements which were removed
 	 * @param afterTargetTime if the remove operation was performed 
 	 * after ore before the target time
 	 *
 	 * @return the new calculated Points
 	 */
 	public static double calculatePoints(int removedElements,
 									  boolean afterTargetTime){
 
 		if(!afterTargetTime)
 			return Math.pow(removedElements, 2);
 		else
 			return Math.pow(removedElements, 2) / 2.0;
 	}
 
 	/**
 	 * calculates pointbonus or malus if the level was finished.
 	 *
 	 * the bonus is calculated as follows:<br>
 	 *
 	 * <ul>
 	 * 	<li> if the level was finished just in time, the remaining time
 	 * 		 is added to, else removed from the points </li>
 	 * 	<li> the remaining stones are counted as malus</li>
 	 * 	<li> if the stones could all be removed, an extra
 	 * 		 bonus is added: the number of stones which were in the original 
 	 * 		 level</li>
 	 * </ul>
 	 *
 	 * @param state the new state count the points in
 	 *
 	 * @return just the point bonus or malus
 	 */
 	public double calculatePointsFinished(final Byte[][] state){
 		
 		int elementsLeft = 0;
 		for(int i=0; i<state.length; i++)
 			for(int j=0; j<state[i].length; j++)
 				if(state[i][j] != 0)
 					elementsLeft++;
 
 		double timeLeft =  targetTime -  (double) watch.getTime()/1000;
 
 		int initialElements = 0;
 		Byte[][] origState = ORIGINAL_LEVEL_STATE.getFieldState();
 		int rows = origState.length;
 		int cols = origState[0].length;
 		initialElements = rows * cols;
 		
 		return calculatePointsFinished(elementsLeft,
 									   timeLeft,
 									   initialElements);
 	}
 
 	/**
 	 * calculate pointbonus or malus if the level was finished.
 	 *
 	 * the bonus is calculated the following:<br>
 	 *
 	 * <ul>
 	 * 	<li> if the level was finished just in time, the remaining time
 	 * 		 is added to the points, else removed </li>
 	 * 	<li> the remaining stones are counted as malus</li>
 	 * 	<li> if the stones could all be removed, it's added an extra
 	 * 		 bonus: the number of stones which was in the original 
 	 * 		 level</li>
 	 * </ul>
 	 *
 	 * @param elementsLeft the elements left in field
 	 * @param timeLeft the remaining time until the target time is over
 	 * @param initialElements the number of elements which was in the
 	 * original field
 	 *
 	 * @return just the point bonus or malus
 	 */
 	public static double calculatePointsFinished(int elementsLeft,
 											  double timeLeft,
 											  int initialElements){
 		
 		double additionalPoints = 0;
 		additionalPoints += timeLeft;
 		
 		
 		if(elementsLeft > 0)
 			additionalPoints -= elementsLeft;
 		else
 			additionalPoints += initialElements;
 
 		return additionalPoints;
 	}
 
 
 	/**
 	 * writes the given information into a file. Throws an exception if
 	 * the given file already exists and the force option is false.
 	 *
 	 * @param inf the information which should be write in the file
 	 * @param f the file to write in
 	 * @param force if true: overwrite existing file
 	 *
 	 * @throws IllegalArgumentException if the given file already 
 	 * exists and the force option is false
 	 * @throws IOException if a IO-exception occurs during the reading
 	 * the file
 	 * @throws SecurityException if the user hasn't write permissions
 	 * the given path
 	 */
 	private void store(final String inf, final File f, final boolean force)
 		throws IllegalArgumentException, 
 			   IOException,
 			   SecurityException{
 		// test if the given path is a file
 		if(f.isFile() || !f.exists()){
 
 			// if the file already exists overwrite it if the force
 			// option is on, else throw a exception
 			if(force && f.exists())
 				f.delete();
 			else if(!force && f.exists())
 				throw new IllegalArgumentException("Error while storing to *.lvl file: "
 						+"the given file allready exists: "+f.getName());
 
 			f.createNewFile();
 			BufferedWriter b = new BufferedWriter(new FileWriter(f));
 			b.write(inf, 0, inf.length());
 
 			this.loadedLevel = f;
 
 			b.close();
 
 		}else{
 			throw new IllegalArgumentException("Error while storing to *.lvl file: "
 					+"the given path is a directory: "+f.getPath());
 		}
 	}
 
 
 	/**
 	 * save level state information to a *.sve file. Don't proceed if the file
 	 * already exists, except the force boolean is true.
 	 *
 	 * @param f the file in which the level should be stored
 	 * @param force override an existing file
 	 *
 	 * @throws LevelNotLoadedFromFileException if the level wasn't loaded
 	 * from a file
 	 * @throws IllegalArgumentException if the given file already 
 	 * exists and the force option is false
 	 * @throws IOException if a IO-exception occurs during the reading
 	 * the file
 	 * @throws SecurityException if the user hasn't write permissions
 	 * the given path
 	 */
 	public void storeLevelState(final File f, boolean force) 
 		throws IllegalArgumentException, 
 			   IOException,
 			   SecurityException{
 
 		try{
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
 			DocumentBuilder parser = factory.newDocumentBuilder();
 
 			Document doc = parser.newDocument();
 			
 			Element levelState = doc.createElement("levelState");
 
 			if(loadedLevel != null)
 				levelState.setAttribute("name", loadedLevel.getAbsolutePath());
 			else
 				levelState.setAttribute("name", "null");
 			levelState.setAttribute("pointsReached", ""+(long)this.currentGameState.getPoints());
 			levelState.setAttribute("elapsedTime", ""+watch.getTime());
 			
 			Element fieldLine;
 			Byte[][] field = this.ORIGINAL_LEVEL_STATE.getFieldState();
 			StringBuilder lineString;
 
 			for(int i=0; i<field.length; i++){
 				fieldLine = doc.createElement("line");
 				lineString = new StringBuilder();
 
 				for(int j=0; j<field[0].length; j++)
 					lineString.append(field[i][j]);
 				
 				fieldLine.setAttribute("value", lineString.toString());
 				levelState.appendChild(fieldLine);
 			}
 
 			doc.appendChild(levelState);
 
 			TransformerFactory tranFactory = TransformerFactory.newInstance(); 
 			Transformer aTransformer = tranFactory.newTransformer(); 
 			aTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
             aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
             //create string from xml tree
             StringWriter sw = new StringWriter();
             StreamResult result = new StreamResult(sw);
             DOMSource source = new DOMSource(doc);
             aTransformer.transform(source, result);
             String xmlString = sw.toString();
 
 			store(xmlString, f, force);
 
 		}catch(ParserConfigurationException e){
 			e.printStackTrace();
 		}catch(TransformerConfigurationException e){
 			e.printStackTrace();
 		}catch(TransformerException e){
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * save level information to a *.lvl file. Don't proceed if the file
 	 * already exists, except the force boolean is true.
 	 *
 	 * @param f the file in which the level should be stored
 	 * @param force override an existing file
 	 *
 	 * @throws IllegalArgumentException if the given file already 
 	 * exists and the force option is false
 	 * @throws FileNotFoundException if the file could not been found
 	 * @throws IOException if a IO-exception occurs during the reading
 	 * the file
 	 * @throws SecurityException if the user hasn't write permissions
 	 * the given path
 	 */
 	public void storeLevel(final File f, boolean force)
 		throws IllegalArgumentException, 
 			   IOException,
 			   SecurityException,
 			   LevelNotLoadedFromFileException{
 
 		String levelInf = this.getOrigLevelState() + "\n" +
 			this.getAdditionalLevelInf();
 		if(this.getHighscore() != null);
 			levelInf += "\n" + this.getHighscorelist();
 
 		store(levelInf, f, force);
 	}
 
 
 	/**
 	 * load a level state from a *.sve file.
 	 *
 	 * @param f the file to load from
 	 *
 	 * @throws FileNotFoundException if the file could not been found
 	 * @throws SAXException if the file is not in valid xml format
 	 * @throws IOException if a IO-exception occurs during the reading
 	 * the file
 	 */
 	public void restoreLevelState(final File f) 
 		throws FileNotFoundException, 
 			   WrongLevelFormatException, 
 			   IOException{
 
 		try{
 			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = fact.newDocumentBuilder();
 			Document doc = builder.parse(f);
 			
 			doc.normalizeDocument();
 			
 			Node levelState = doc.getDocumentElement();
 			NamedNodeMap attr = levelState.getAttributes();
 
 			String loadedLevel = attr.getNamedItem("name").getNodeValue();
 			if(loadedLevel.equals("null")){
 				this.loadedLevel = null;
 				this.currentGameState = new GameState(null, 0);
 				init(changeListener);
 			}
 			else{
 				this.loadedLevel = new File(loadedLevel);
 				restoreLevel(this.loadedLevel);
 			}
 
 			double points = Double.parseDouble(attr.getNamedItem("pointsReached").getNodeValue());
 			long elapsedTime = Long.parseLong(attr.getNamedItem("elapsedTime").getNodeValue());
 			this.watch = new MyStopWatch(elapsedTime);
 			this.firstClick = true;
 
 			NodeList lines = doc.getElementsByTagName("line");
 
 			int height = lines.getLength();
 			int width = lines.item(0).getAttributes().getNamedItem("value").getNodeValue().length();
 			Byte[][] field = new Byte[height][width];
 
 			for(int i=0; i<height; i++)
 				field[i] = 
 					parseByteDigits(
 							lines.item(i).getAttributes().getNamedItem("value").getNodeValue());
 
 			this.currentGameState = new GameState(field, points);
 			this.addEdit(currentGameState);
 
 		}catch(ParserConfigurationException e){
 			e.printStackTrace();
 		}catch(SAXException e){
 			throw new WrongLevelFormatException(
 					"The parsed level state xml file is not well-formed");
 		}catch(NullPointerException e){
 			throw new WrongLevelFormatException(
 					"While parsing level state xml file: some attribute wasn't found");
 		}catch(NumberFormatException e){
 			throw new WrongLevelFormatException(
 					"While parsing level state xml file: a number wasn't in the right format");
 		}
 	}
 
 	
 	/**
 	 * load a level information from a *.lvl file.
 	 * See {@link #loadLevelFromString(String)} for levelformat
 	 *
 	 * @param f the file to load from
 	 *
 	 * @throws FileNotFoundException if the file could not been found
 	 * @throws WrongLevelFormatException if the file contains a invalid
 	 * level
 	 * @throws IOException if a IO-exception occurs during the reading
 	 * the file
 	 */
 	public void restoreLevel(final File f)
 		throws FileNotFoundException, 
 			   WrongLevelFormatException, 
 			   IOException{
 
 		BufferedReader r = new BufferedReader(new FileReader(f));
 
 		char[] levelString = new char[(int)f.length()];
 		r.read(levelString, 0, (int)f.length());
 		loadLevelFromString(new String(levelString));
 		
 		this.loadedLevel = f;
 
 		r.close();
 	}
 
 
 	/**
 	 * returns a string representation of the current level fieldstate
 	 * without extra level informations.
 	 *
 	 * @return the string which contains a representation of the level
 	 * fieldstate
 	 */
 	@Override
 	public String toString(){
 		return this.getCurrentLevelState();
 	}
 }
