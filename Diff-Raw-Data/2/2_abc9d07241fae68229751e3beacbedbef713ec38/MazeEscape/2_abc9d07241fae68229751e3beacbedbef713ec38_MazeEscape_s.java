 package mazeEscapeApp;
 
 import javax.swing.JMenuBar;
 import javax.swing.JToolBar;
 
 import org.jhotdraw.application.DrawApplication;
 
 /*
  * Class represents an instance of a mazeEscape game. No GUI involved here.
  */
 public class MazeEscape extends DrawApplication {
 	private static final long serialVersionUID = 5843503278232195529L;
 	// The row, column arrays storing the clickable areas and respective
 	// GridCell frames
 	private GCellArea[][] gCellClickableArea;
 	private GridCell[][] gridCells;
 
 	// For an n * n maze, length = width = n
 	private int lengthMaze;
 	private int gCellPixelLength = 35;
 	private String difficulty;
 
 	// Currently selected grid cell (used for game; user grid selection)
 	private GCellArea currentlySelected;
 	private GCellArea startCell, endCell;
 
 	// Used in handleClick. Checks to see if first selected cell
 	private boolean isFirstClick = true;
 	private boolean reachedEndCell = false;
 
 	// Used for calculation of score
 	private boolean on = true;
 	private int levelPoints = 0;
 	private int timePassed = 0;
 	private int timeScore = 0;
 	private int minSteps = 0;
 	private int stepsTaken = 0;
 	
 	private GUIDrawer guiDrawer;
 	private ForfeitButton fb;
 
 	public MazeEscape(String difficulty) {
 		super("MazeEscape");
 
 		setDifficultyMode(difficulty);
 
 		gridCells = new GridCell[lengthMaze][lengthMaze];
 		gCellClickableArea = new GCellArea[lengthMaze][lengthMaze];
 	}
 
 	/**
 	 * Calculates the score based on amount of steps taken combined with time
 	 * taken to reach the end
 	 */
 	@SuppressWarnings("static-access")
 	public void calculateScore() throws InterruptedException {
 		while (on == true) {
 			Thread thread = new Thread();
 			thread.start();
 			timePassed++;
 			// Checks if end of the maze has been reached
 			if (isReachedEndCell() == true) {
 				// Calculates maze points as well as completion accuracy
 				if (timePassed <= timeScore) {
 					double score = getStepsTaken();
 					int points = timeScore - timePassed + levelPoints;
 					double accuracy = minSteps / score * 100;
 					WinnerScreen win = new WinnerScreen(this);
 					win.writeOutput(accuracy, points);
 					// System.out.println("Your score is: " + points + "!");
 					// System.out.println("Maze completion accuracy is: " +
 					// accuracy +"%!");
 					on = false;
 				} else {
 					double score = getStepsTaken();
 					double accuracy = minSteps / score * 100;
 					WinnerScreen win = new WinnerScreen(this);
 					win.writeOutput(accuracy, levelPoints);
 					// System.out.println("Your score is: " + levelPoints +
 					// "!");
 					// System.out.println("Maze completion accuracy: " +
 					// accuracy +"%!");
 					on = false;
 				}
 			}
 			thread.sleep(1000);
 		}
 	}
 
 	public String getDifficulty() {
 		return difficulty;
 	}
 
 	public void setDifficulty(String d) {
 		difficulty = d;
 	}
 
 	public void setDifficultyMode(String difficulty) {
 		this.difficulty = difficulty;
 		if (difficulty.equals("Easy")) {
 			lengthMaze = 2;
 			timeScore = 30;
 			levelPoints = 50;
 		} else if (difficulty.equals("Medium")) {
 			lengthMaze = 15;
 			timeScore = 45;
 			levelPoints = 100;
 		} else if (difficulty.equals("Hard")) {
 			lengthMaze = 30;
 			timeScore = 90;
 			levelPoints = 200;
 		}
 	}
 	
 	protected void createTools(JToolBar tBar)
 	{
 
 		super.createTools(tBar);
 		fb = new ForfeitButton(this, guiDrawer);
		tBar.add(createToolButton(IMAGES+"OCONN1", "Forfeit See Solution", fb));
 	}
 
 	@Override
 	protected void createMenus(JMenuBar mb) {
 		addMenuIfPossible(mb, createFileMenu());
 		addMenuIfPossible(mb, createEditMenu());
 	}
 
 	/*
 	 * Getters and setters below here
 	 */
 
 	public void setGUIDrawer(GUIDrawer gd)
 	{
 		guiDrawer = gd;
 		fb.setGUIDrawer(guiDrawer);
 	}
 	
 	public GCellArea[][] getgCellClickableArea() {
 		return gCellClickableArea;
 	}
 
 	public void setgCellClickableArea(GCellArea[][] gCellClickableArea) {
 		this.gCellClickableArea = gCellClickableArea;
 	}
 
 	public GridCell[][] getGridCells() {
 		return gridCells;
 	}
 
 	public void setGridCells(GridCell[][] gridCells) {
 		this.gridCells = gridCells;
 	}
 
 	public int getLengthMaze() {
 		return lengthMaze;
 	}
 
 	public void setLengthMaze(int lengthMaze) {
 		this.lengthMaze = lengthMaze;
 	}
 
 	public int getgCellPixelLength() {
 		return gCellPixelLength;
 	}
 
 	public void setgCellPixelLength(int gCellPixelLength) {
 		this.gCellPixelLength = gCellPixelLength;
 	}
 
 	public GCellArea getCurrentlySelected() {
 		return currentlySelected;
 	}
 
 	public void setCurrentlySelected(GCellArea currentlySelected) {
 		this.currentlySelected = currentlySelected;
 	}
 
 	public GCellArea getStartCell() {
 		return startCell;
 	}
 
 	public void setStartCell(GCellArea startCell) {
 		this.startCell = startCell;
 	}
 
 	public GCellArea getEndCell() {
 		return endCell;
 	}
 
 	public void setEndCell(GCellArea endCell) {
 		this.endCell = endCell;
 	}
 
 	public boolean isFirstClick() {
 		return isFirstClick;
 	}
 
 	public void setFirstClick(boolean isFirstClick) {
 		this.isFirstClick = isFirstClick;
 	}
 
 	public boolean isReachedEndCell() {
 		return reachedEndCell;
 	}
 
 	public void setReachedEndCell(boolean reachedEndCell) {
 		this.reachedEndCell = reachedEndCell;
 	}
 
 	public void setSteps(int mSteps) {
 		this.minSteps = mSteps;
 	}
 
 	public boolean isOn() {
 		return on;
 	}
 
 	public void setOn(boolean on) {
 		this.on = on;
 	}
 
 	public int getStepsTaken() {
 		return stepsTaken;
 	}
 
 	public void setStepsTaken(int stepsTaken) {
 		this.stepsTaken = stepsTaken;
 	}
 
 }
