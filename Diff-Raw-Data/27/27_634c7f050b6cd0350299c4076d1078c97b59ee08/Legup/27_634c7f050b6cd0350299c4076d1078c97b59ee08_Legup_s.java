 /**
  *  Legup.java
  **/
 
 package edu.rpi.phil.legup;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Vector;
 import javax.swing.JOptionPane;
 import edu.rpi.phil.legup.editor.SaveableBoardState;
 import edu.rpi.phil.legup.newgui.LEGUP_Gui;
 import edu.rpi.phil.legup.newgui.TreeSelectionListener;
 import edu.rpi.phil.legup.newgui.BoardDataChangeListener;
 import edu.rpi.phil.legup.newgui.TransitionChangeListener;
 import edu.rpi.phil.legup.saveable.SaveableProof;
 import java.awt.Image;
 
 //TODO system l&f
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.UIManager;
 
 /**
  * The <code>Legup</code> class is the main class for the Legup Application. It
  * provides access to all the major data structures and functionality necessary.
  *
  * @author Drew Housten & Stan Bak
  * @version 1.0
  */
 public class Legup
 {
 	private static Legup instance = null;
 	
 	public static ArrayList<BoardDataChangeListener> boardDataChangeListeners = new ArrayList<BoardDataChangeListener>();
 	public static ArrayList<TransitionChangeListener> transitionChangeListeners = new ArrayList<TransitionChangeListener>();
 	
 	public static void renewListeners()
 	{
 		Legup legup = getInstance();
 		boardDataChangeListeners.clear();
 		transitionChangeListeners.clear();
 		legup.getSelections().clearTreeSelectionListeners();
 		BoardState.addCellChangeListener(legup.getGui().getBoard());
 		BoardState.addCellChangeListener(legup.getGui().getJustificationFrame());
 		BoardState.addCellChangeListener(legup.getGui().getTree());
 		BoardState.addTransitionChangeListener(legup.getGui().getTree().treePanel);
 		legup.getSelections().addTreeSelectionListener(legup.getGui().getJustificationFrame());
 		legup.getSelections().addTreeSelectionListener(legup.getGui());
 		legup.getSelections().addTreeSelectionListener(legup.getGui().getTree());
 		legup.getSelections().addTreeSelectionListener(legup.getGui().getTree().treePanel);
 	}
 	/**
 	 * Legup is a singleton. Get the instance of legup
 	 *
 	 * @return the Legup instance, creating if necessary
 	 */
 	public static Legup getInstance()
 	{
 		if (instance == null)
 			instance = new Legup();
 
 		return instance;
 	}
 	public LEGUP_Gui getGui()
 	{
 		return this.gui;
 	}
 
 	public java.net.URL getResource(String file){
 		return getClass().getResource('/'+file);
 	}
 
 	private Config config = new Config("config.xml");
 
 	/**
 	 * Returns the <code>Config</code> object so that the calling code can have
 	 * access to any configuration information
 	 *
 	 * @return Configuration object
 	 */
 	public Config getConfig()
 	{
 		return config;
 	}
 
 /*
  * BoardState methods
  */
 
 	private BoardState initialBoardState = null;
 
 	/**
 	 * Gets the initial board state. This is the start step of the proof.
 	 *
 	 * @return Initial Board State
 	 */
 	public BoardState getInitialBoardState()
 	{
 		return this.initialBoardState;
 	}
 
 	public void loadRandomBoard(String puzzle)
 	{
 		Vector<String> allBoards = config.getBoardsForPuzzle(puzzle);
 		Random random = new Random();
 		String filename = allBoards.get(random.nextInt(allBoards.size()));
 		System.out.println(filename + " selected");
 		loadBoardFile(filename);
 	}
 
 	public void loadBoardFile(String filename)
 	{
 		System.out.println("Loading board: " + filename);
 
 		//Try to load the file
 		try
 		{
 			initialBoardState = SaveableBoardState.loadState(filename);
 			selections.setSelection(new Selection(initialBoardState,false));
 		}
 		catch (Exception e)
 		{
 			initialBoardState = null;
 			errorMessage("Error loading board file:" + e.toString());
 			return;
 		}
 
 		String puzzle = initialBoardState.getPuzzleName();
 		System.out.println("Loading puzzle module: " + puzzle);
 		
 		if(loadPuzzleModule(puzzle))
 		{
 			errorMessage("Error encountered loading PuzzleModule.");
 		}
 
 		gui.reloadGui();
 	}
 
 	public void loadAssignmentFile(String filename)
 	{
 		//TODO: Assignment File loading
 	}
 
 	public void loadProofFile(String filename)
 	{
 		try
 		{
 			initialBoardState = SaveableProof.loadProof(filename);
 			selections = new Selections();
 			BoardState b = initialBoardState;
 			if(b!=null)while(b.getTransitionsFrom().size() > 0)b = b.getTransitionsFrom().lastElement();
 			selections.setSelection(new Selection(b,b.isModifiable()));
 		}
 		//catch (NullPointerException e1) {}
 		catch (Exception e)
 		{
 			initialBoardState = null;
 			errorMessage("Error loading proof: " + e.toString());
 			return;
 		}
 
 		String puzzle = initialBoardState.getPuzzleName();
 		if(puzzle == null)return;
 		System.out.println("Loading puzzle module: " + puzzle);
 		if(loadPuzzleModule(puzzle))
 		{
 			errorMessage("Error encountered loading PuzzleModule.");
 		}
 		gui.reloadGui();
 	}
 
 	/*
 	 * PuzzleModule methods
 	 */
 
 	private PuzzleModule puzzleModule = null;
 
 	/**
 	 * Gets the active Puzzle Module. Each type of puzzle will have its own
 	 * Puzzle Module which will provide support for puzzle-specific
 	 * functionality
 	 *
 	 * @return The active Puzzle Module
 	 */
 	public PuzzleModule getPuzzleModule()
 	{
 		//if(puzzleModule == null){System.out.println("bad");}
 		return this.puzzleModule;
 	}
 
 	/**
 	 * @param moduleNameName
 	 * @param error
 	 * @return
 	 */
 	public boolean loadPuzzleModule(String moduleName)
 	{
 		renewListeners();
 		// Load the puzzle
 		try
 		{
 			puzzleModule = (PuzzleModule) (Class.forName(config
 					.getPuzzleClassForName(moduleName)).newInstance());
 			puzzleModule.name = moduleName;
 		}
 		catch(Exception e)
 		{
 			errorMessage("PuzzleModule load error: " + e.toString());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 *	Method used for Puzzle Generators - no I/O interaction necessary
 	 */
 	public void initializeGeneratedPuzzle(PuzzleModule module, BoardState init)
 	{
 		puzzleModule = module;
 		initialBoardState = init;
 		selections.setSelection(new Selection(initialBoardState, false));
 		gui.reloadGui();
 	}
 
 	private Selections selections = new Selections();
 
 	public Selections getSelections()
 	{
 		return selections;
 	}
 
 	/**
 	 * Prints an error message to a log, console, and GUI
 	 * @param message The error to display
 	 */
 	public void errorMessage(String message)
 	{
 		if (gui != null)
 		{
 			gui.errorEncountered(message);
 		}
 		System.out.println(message);
 	}
 
 	// The GUI used - modified by Daniel for explicitness
 	private LEGUP_Gui gui = null;
 	private Login login = null;
 	private String user = null;
 	private String[] admins = {"heuveb"};
 	
 	public String getUser()
 	{
 		return user;
 	}
 	
 	public void setUser(String user)
 	{
 		this.user = user;  
 	}
 	
 	public String[] getAdmins()
 	{
 		return this.admins;
 	}
 
 	public void refresh()
 	{
 		gui.repaintBoard();
 	}
 	
 	/**
 	 * Starts up the program and the GUI
 	 */
 	public static void main(String[] args)
 	{
 		Legup legup = Legup.getInstance();
 		
 		// TODO system look & feel
 		// Set System L&F
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch( Exception e ){}
 
 		legup.login = new Login(legup);
 		//legup.login.promptLogin();
 		legup.gui = new LEGUP_Gui(legup);
 		
 		legup.gui.promptPuzzle();
 
 		// This is for the animation - Daniel P
 		if (BoardDrawingHelper.ANIMATE_SPLIT_CASE) {
 			legup.selections.addTreeSelectionListener(new TreeSelectionListener() {
 				private Thread myThread = null;
 				public void treeSelectionChanged(ArrayList<Selection> newSelections) {
 					/*if (myThread != null)
 					{
 						myThread.stop();
 						myThread = null;
 					}*/
 					if (newSelections.size() >= 1 && !newSelections.get(0).isState())
 					{
 						myThread = new Thread()
 						{
 							public void run()
 							{
 								while (true)
 								{
 									try { Thread.sleep(150); } catch (InterruptedException ie) { }
 									Legup.getInstance().gui.repaintBoard();
 								}
 							}
 						};
 						myThread.start();
 					}
 				}
 			});
 		}
 	}
 }
