 /**
  *  BoardState.java
  **/
 
 package edu.rpi.phil.legup;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Vector;
 import java.lang.Math;
 import java.awt.Color;
 import edu.rpi.phil.legup.editor.SaveableBoardState;
 import edu.rpi.phil.legup.newgui.BoardDataChangeListener;
 import edu.rpi.phil.legup.newgui.TransitionChangeListener;
 import edu.rpi.phil.legup.newgui.TreePanel;
 import edu.rpi.phil.legup.saveable.SaveableProofState;
 import edu.rpi.phil.legup.saveable.SaveableProofTransition;
 import edu.rpi.phil.legup.newgui.JustificationFrame;
 
 
 /**
  * Stores all the information related to a board state.  Contains the cell
  * values, label values, transitions to and from the state, and any applied rules
  * associated with this state.
  *
  * @author Drew Housten & Stan Bak
  */
 public class BoardState implements java.io.Serializable
 {
 	static final long serialVersionUID = 9001L;
 
 	static final public int LABEL_TOP = 0;
 	static final public int LABEL_BOTTOM = 1;
 	static final public int LABEL_LEFT = 2;
 	static final public int LABEL_RIGHT = 3;
 
 	static final public int STATUS_UNJUSTIFIED = 0;
 	static final public int STATUS_RULE_CORRECT = 1;
 	static final public int STATUS_RULE_INCORRECT = 2;
 	static final public int STATUS_CONTRADICTION_CORRECT = 3;
 	static final public int STATUS_CONTRADICTION_INCORRECT = 4;
 	static final public int STATUS_CASE_SETUP = 5;
 	private Color current_color = TreePanel.nodeColor;
 	public Color getColor() { return current_color; }
 	public void setColor(Color c) { current_color = c; }
 	public String justificationText = null; //strings returned by justification-checks, to be displayed on mouseover
 	// BoardState when initially created (set in constructor)
 	private BoardState originalState = null;
 	private int height;
 	private int width;
 	private int[][] boardCells;
 	
 	//to keep track of what cells have changed
 	private Vector <Point> changedCells;
 	private boolean[][] modifiableCells;
 	private boolean[][] editedCells;
 	
 	//labels surrounding the grid? [hints, coordinates, etc.]
 	private int[] topLabels;
 	private int[] bottomLabels;
 	private int[] leftLabels;
 	private int[] rightLabels;
 
 	private int hintsGiven = 0;
 
 	private String puzzleName = null;
 	
 	// Duplicate Legup.user so that it can be saved in the file
 	private String user = null;
 
 	// the location of this node within the proof
 	private Point offset = new Point(0,0);
 	private Point location = new Point(0,0);
 
 	// parents
 	private Vector<BoardState> transitionsTo = new Vector<BoardState>();
 
 	// children
 	private Vector<BoardState> transitionsFrom = new Vector<BoardState>();
 	
 	// a PuzzleRule or Contradiction
 	private Justification justification = null;
 
 	// the justification case rule for this parent state
 	private CaseRule caseRuleJustification = null;
 
 	// puzzle specific extra data
 	// for example, in tree tent, the tree which each tent belongs to
 	protected ArrayList <Object> extraData = new ArrayList <Object>();
 	public ArrayList <Object> extraDataDelta = new ArrayList <Object>();
 	
 	private boolean collapsed = false;
 
 	private ArrayList<Point> hintCells = new ArrayList<Point>();
 
 	private boolean isSolution = false;
 
 	private ArrayList<BoardState> mergeChildren = new ArrayList<BoardState>();
 	private BoardState mergeOverlord;
 
 	private boolean virtualBoard = false;
 	
 	private boolean modifiableState = false;
 
 
 	//@throws Exception if the extents are invalid
 	public BoardState(int height, int width)
 	{
 		this(height,width,true);		
 	}
 	
 	private BoardState(int height, int width, boolean makeOriginalState)
 	{
 		this.user = Legup.getInstance().getUser();
 		// Set the height and width
 		this.setHeight(height);
 		this.setWidth(width);
 		
 		// Allocate arrays
 		setBoardCells(new int[height][width]);
 		setTopLabels(new int[width]);
 		setBottomLabels(new int[width]);
 		setLeftLabels(new int[height]);
 		setRightLabels(new int[height]);
 		modifiableCells = new boolean[height][width];
 		editedCells = new boolean[height][width];
 		changedCells = new Vector<Point>();
 		extraDataDelta = new ArrayList<Object>();
 		justificationText = null;
 		
 		// Initialize arrays
 		for (int i=0;i<height;i++)
 		{
 			getLeftLabels()[i]=0;
 			getRightLabels()[i]=0;
 			for (int j=0;j<width;j++)
 			{
 				modifiableCells[i][j] = true;
 				getBoardCells()[i][j] = 0;
 				editedCells[i][j] = false;
 				if (i==0)
 				{
 					getTopLabels()[j] = 0;
 					getBottomLabels()[j] = 0;
 				}
 			}
 		}
 		
 		//assign name of board
 		if(Legup.getInstance().getPuzzleModule() != null)
 		{
 			this.setPuzzleName(Legup.getInstance().getPuzzleModule().name);
 		}
 		
 		if (makeOriginalState)
 		{
 			originalState = new BoardState(this,false);
 		}
 	}
 
 	//Copy constructor. Connections will not be copied.
 	public BoardState(BoardState copy)
 	{
 		this(copy,true);
 	}
 	
 	//used for copy constructor
 	private BoardState(BoardState copy, boolean makeOriginalState)
 	{
 		this(copy.getWidth(), copy.getHeight(),false);
 		setFromBoardState(copy,makeOriginalState);
 	}
 	
 	//used for copy constructors
 	private void setFromBoardState(BoardState copy, boolean makeOriginalState)
 	{
 		virtualBoard = copy.virtualBoard;
 
 		// Allocate the arrays
 		for (int x = 0; x < getWidth(); x++) for (int y = 0; y < getHeight(); y++)
 		{
 			boardCells[y][x] = copy.getBoardCells()[y][x];
 			modifiableCells[y][x] = copy.modifiableCells[y][x];
 			editedCells[y][x] = copy.editedCells[y][x];
 		}
 		for (int x = 0; x < getWidth(); x++)
 		{
 			getTopLabels()[x] = copy.getTopLabels()[x];
 			getBottomLabels()[x] = copy.getBottomLabels()[x];
 		}
 		for (int y = 0; y < getHeight(); y++)
 		{
 			getLeftLabels()[y] = copy.getLeftLabels()[y];
 			getRightLabels()[y] = copy.getRightLabels()[y];
 		}
 		extraData = new ArrayList<Object>(copy.extraData);
 		justificationText = null;
 		changedCells = copy.getChangedCells();
 		
 		transitionsTo.clear();
 		for (int i=0;i<copy.transitionsTo.size();i++)
 		{
 			transitionsTo.add(copy.transitionsTo.get(i));
 		}
 		transitionsFrom.clear();
 		for (int i=0;i<copy.transitionsFrom.size();i++)
 		{
 			transitionsFrom.add(copy.transitionsFrom.get(i));
 		}
 		
 		if (makeOriginalState)
 		{
 			originalState = new BoardState(this,false);
 		}
 		else
 		{
 			originalState = null;
 		}
 	}
 	
 	public BoardState getOriginalState()
 	{
 		return originalState;
 	}
 	
 	public void revertToOriginalState()
 	{
 		if (originalState == null)
 			return;
 		setFromBoardState(originalState,true);
 	}
 	
 	public void setVirtual(boolean virtual)
 	{
 		virtualBoard = virtual;
 	}
 	
 	public boolean isModifiable()
 	{
 		return modifiableState;
 	}
 	
 	public void setModifiableState(boolean mod)
 	{
 		modifiableState = mod;
 		/*if(mod)this.setOffset(new Point(0, (int)(4.5*TreePanel.NODE_RADIUS)));
 		else this.setOffset(new Point(0, 0));*/
 	}
 
 	public String getUser()
 	{
 		return this.user;
 	}
 	
 	/**
 	 * Toggle whether this state and all its (single) children are collapsed
 	 * <not> called recursively to do the work
 	 * @see toggleCollapseRecursive
 	 */
 	public void toggleCollapse()
 	{
 		// if we can collapse it legally
 		if (transitionsFrom.size() == 1 && transitionsTo.size() < 2)
 		{
 			if (!collapsed)
 				location.y += TreePanel.NODE_RADIUS;
 			else
 				location.y -= TreePanel.NODE_RADIUS;
 
 			toggleCollapseRecursive(location.x,location.y);
 
 			transitionsChanged();
 		}
 		else
 		{
 			// TODO: elegant error handling, add error label to treeframe
 		}
 	}
 
 	/**
 	 * Recursively toggle the collapse value of this state and all it's children
 	 * @param x: the x coordinate of the collapsed but selectable (grand)parent
 	 * @param y: the y coordinate of the collapsed but selectable (grand)parent
 	 */
 	public void toggleCollapseRecursive(int x, int y)
 	{
 		if (transitionsFrom.size() == 1 && transitionsTo.size() < 2)
 		{
 			BoardState child = transitionsFrom.get(0);
 			collapsed = !collapsed;
 
 			// collapse the child too
 			if (collapsed != child.collapsed)
 				child.toggleCollapseRecursive(x,y);
 		}
 		else // TODO: fix positioning of children
 		{
 
 		}
 	}
 
 	/**
 	 * Is the case rule applied at this parent state valid ?!?
 	 * @return null iff it is validly applied, the error string otherwise
 	 */
 	public String isJustifiedCaseSplit()
 	{
 		String rv = null;//"No rule selected!";
 
 		if (getCaseRuleJustification() != null)
 		{
 			rv = getCaseRuleJustification().checkCaseRule(this);
 		}
 
 		return rv;
 	}
 
 	/**
 	 * input x y coordinates, where (0, 0) is the top left corner
 	 * @return true iff the user is allowed to modify the cell during the proof
 	 */
 	public boolean isModifiableCell(int x, int y)
 	{
 		//return modifiableCells[y][x];
 		return Math.abs(boardCells[y][x]) == boardCells[y][x];
 	}
 
 	/**
 	 * Get the case split justification at this cell (null if not defined)
 	 * @return the CaseRule used to justify this state's children
 	 */
 	public CaseRule getCaseSplitJustification()
 	{
 		return getCaseRuleJustification();
 	}
 
 	//Set the justification in the parent state of the split
 	public void setCaseSplitJustification(CaseRule jusification)
 	{
 		delayStatus = STATUS_UNJUSTIFIED;
 		setCaseRuleJustification(jusification);
 		modifyStatus();
 	}
 
 	//Get an array of all the defined extra data for this puzzle
 	public ArrayList<Object> getExtraData()
 	{
 		return extraData;
 	}
 
 	//Add object o to this state's extra data
 	public void addExtraData(Object o)
 	{
 		extraData.add(o);
 
 		boardDataChanged();
 	}
 
 	//tell listeners when board data changed
 	public void boardDataChanged()
 	{
 		delayStatus = STATUS_UNJUSTIFIED;
 		modifyStatus();
 		evalDelayStatus();
 		for (BoardState B : transitionsTo) B.modifyStatus();
 		for (BoardState B : transitionsFrom) B.modifyStatus();
 		for (int a = 0; a < Legup.boardDataChangeListeners.size(); ++a)
 		{
 			BoardDataChangeListener c = Legup.boardDataChangeListeners.get(a);
 
 			c.boardDataChanged(this);
 		}
 	}
 
 
 	/**
 	 * Gets the cell contents at a particular row and column
 	 *
 	 * @param x Column of the cell
 	 * @param y Row of the cell
 	 * @return Cell Value
 	 * @throws IndexOutOfBoundsException if the row or column is invalid
 	 */
 	public int getCellContents(int x, int y)
 	{
 		return Math.abs(getBoardCells()[y][x]);
 	}
 
 
 	/**
 	 * Sets the cell contents at a particular row and column
 	 *
 	 * @param x Column of the cell
 	 * @param y Row of the cell
 	 * @param value Value to set
 	 * @throws IndexOutOfBoundsException if the row or column is invalid
 	 */
 	public void setCellContents(int x, int y,int value)
 	{
 		//TODO: Settings
 		boolean playmode = false;
 		//System.out.println("DEBUG: setCellContents("+x+","+y+","+value+")");
 		if (value == boardCells[y][x] || justification instanceof Contradiction || isMergeTransition())
 			return;
 
 		// Obsolete with new proof mode system
 		/*if(playmode)
 		{
 			if(value == PuzzleModule.CELL_UNKNOWN)
 			{
 				BoardState parent = this.getSingleParentState();
 				Legup.getInstance().getSelections().setSelection(new Selection(parent, false));
 				deleteState(this);
 			}
 			else if(boardCells[y][x] == PuzzleModule.CELL_UNKNOWN)
 			{
 				BoardState child = this.addTransitionFrom();
 				Legup.getInstance().getSelections().setSelection(new Selection(child, false));
 				child.boardCells[y][x] = value;
 				Legup.getInstance().getPuzzleModule().updateState(child);
 				child.boardDataChanged();
 			}
 			else
 			{
 				boardCells[y][x] = value;
 				boardDataChanged();
 			}
 		}*/
 		
 		
   		boardCells[y][x] = value;
   		
   		//update editedCells if necessary
 		BoardState parent = getSingleParentState();
 		if (parent != null)
 		{
 			if (getBoardCells()[y][x] != parent.getCellContents(x, y))
 			{
 				editedCells[y][x] = true;
 				if(!changedCells.contains(new Point(x,y)))
 					changedCells.add(new Point(x,y));
 			}
 			else
 			{
 				editedCells[y][x] = false;
 				if(changedCells.contains(new Point(x,y)))
 					changedCells.removeElement(new Point(x,y));
 			}
 		}
 		
 		for (BoardState child : transitionsFrom) {
 			child.propagateChange(x, y, value);
 		}
 		
 		if (!virtualBoard) boardDataChanged();
 		
 	}
 	
 	/**
 	 * Propagates changes down the tree, overwriting boardCells, editedCells and
 	 * modifiableCells to match the condition. It then calls propagateChange() for
 	 * each of its children.
 	 * @param x Column of the cell
 	 * @param y Row of the cell
 	 * @param value Value to set
 	 */
 	public void propagateChange(int x, int y, int value)
 	{
 		getBoardCells()[y][x] = value;
 		setModifiableCell(x, y, value == PuzzleModule.CELL_UNKNOWN);
 		editedCells[y][x] = false;
 		
 		Legup.getInstance().getPuzzleModule().updateState(this);
 		
 		for (BoardState child : transitionsFrom)
 		{
 			child.propagateChange(x, y, value);
 		}
 		
 		if (!virtualBoard)
 			boardDataChanged();
 	}
 	
 	public void propagateExtraData(Object ex, boolean present)
 	{
 		if(ex == null)return;
 		if(!present)if(extraData.contains(ex))extraData.remove(ex);
 		if(present)if(!extraData.contains(ex))extraData.add(ex);
 		for(BoardState child : transitionsFrom)
 		{
 			if(!child.extraDataDelta.contains(ex))
 			{
 				child.propagateExtraData(ex,present);
 			}
 		}
 	}
 	
 	public void propagateContradiction(Contradiction j)
 	{
 		if(!(j instanceof Contradiction))return;
 		leadContradiction = true;
 		if(!isModifiable())current_color = Color.red; //red circles on the path to a contradiction
 		BoardState par = getSingleParentState();
 		if(par != null)
 		{
 			if(par.getCaseRuleJustification() != null)return;
 			if(!par.isModifiable())par.justification = j;
 			par.propagateContradiction(j);
 		}
 	}
 	public int numEmptySpaces(int where, boolean row)
 	{
 		if(where < 0)return -1;
 		int tmp = 0;
 		if(row)
 		{
 			if(where > height)return -1;
 			for(int c1=0;c1<width;c1++)
 			{
 				if(getCellContents(c1,where) == 0)tmp++;
 			}
 		}
 		else //column
 		{
 			if(where > width)return -1;
 			for(int c1=0;c1<height;c1++)
 			{
 				if(getCellContents(where,c1) == 0)tmp++;
 			}
 		}
 		return tmp;
 	}
 	public void fillBlanks(int where, boolean row, int[] filler)
 	{
 		PuzzleModule pm = Legup.getInstance().getPuzzleModule();
 		if(where < 0)return;
 		int tmp = 0;
 		if(row)
 		{
 			if(where > height)return;
 			for(int c1=0;c1<width;c1++)
 			{
 				if(getCellContents(c1,where) == 0)
 				{
 					setCellContents(c1,where,pm.getStateNumber(pm.getStateName(filler[tmp])));
 					tmp++;
 				}
 			}
 		}
 		else //column
 		{
 			if(where > width)return;
 			for(int c1=0;c1<height;c1++)
 			{
 				if(getCellContents(where,c1) == 0)
 				{
 					setCellContents(where,c1,pm.getStateNumber(pm.getStateName(filler[tmp])));
 					tmp++;
 				}
 			}
 		}
 	}
 	 //Used for puzzle generation.
 	 public void setModifiableCell(int x, int y, boolean value)
 	 {
 		//modifiableCells[y][x] = value;
 		 if (value)
 			 boardCells[y][x] = Math.abs(boardCells[y][x]);
 		 else
 			 boardCells[y][x] = Math.abs(boardCells[y][x])*-1;
 		 
 		 //if (boardCells[y][x] == 0)System.out.println("WARNING: tried to make 0 value negative");
 	 }
 	 
 	
 	//Moves editedCells into modifiableCells. Called during endTranstion().
 	public void editedToModifiable()
 	{
 		for (int i = 0; i < getHeight(); i++)
 		{
 			for (int j = 0; j < getWidth(); j++)
 			{
 				if (editedCells[i][j])
 				{
 					setModifiableCell(j, i, false);
 					editedCells[i][j] = false;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Add a cell change listener that will listen to any and all cell changes
 	 * @param listener the listener we're adding
 	 */
 	public static void addCellChangeListener(BoardDataChangeListener listener)
 	{
 		Legup.boardDataChangeListeners.add(listener);
 	}
 
 	/**
 	 * Add a transition change listener to listen to any transition changes that occur
 	 * @param l the listener we're adding
 	 */
 	public static void addTransitionChangeListener(TransitionChangeListener l)
 	{
 		Legup.transitionChangeListeners.add(l);
 	}
 
 	/**
 	 * @return The height of the board
 	 */
 	public int getHeight(){
 		return height;
 	}
 
 	/**
 	 * @return The width of the board
 	 */
 	public int getWidth(){
 		return width;
 	}
 
 
 	/**
 	 * Gets the value for one of the labels.
 	 * labelLocation should be either <code>LABEL_TOP</code>, <code>LABEL_BOTTOM</code>,<code>LABEL_LEFT</code>,
 	 * or <code>LABEL_RIGHT</code>
 	 *
 	 * @param labelLocation Location of the label in relation to the board
 	 * @param pos The position of the particular label value
 	 * @return The label value
 	 * @throws IndexOutOfBoundsException if the pos or labelLocation are invalid
 	 */
 	public int getLabel(int labelLocation, int pos) throws IndexOutOfBoundsException{
 	switch(labelLocation){
 	case 0:
 		if (pos<0 || pos >= getWidth()){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return getTopLabels()[pos];
 	case 1:
 		if (pos<0 || pos >= getWidth()){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return getBottomLabels()[pos];
 	case 2:
 		if (pos<0 || pos >= getHeight()){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return getLeftLabels()[pos];
 	case 3:
 		if (pos<0 || pos >= getHeight()){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return getRightLabels()[pos];
 	default:
 		throw new IndexOutOfBoundsException("Invalid label"); // CHANGE THIS!!!! not index
 	}
 	}
 
 	/**
 	 * Sets the value for one of the labels.
 	 * labelLocation should be either <code>LABEL_TOP</code>,
 	 * <code>LABEL_BOTTOM</code>,<code>LABEL_LEFT</code>,or <code>LABEL_RIGHT</code>
 	 *
 	 * @param labelLocation Location of the label in relation to the board
 	 * @param pos The position of the particular label value
 	 * @param value The new label value
 	 */
 	public void setLabel(int labelLocation, int pos, int value)
 	{
 		switch(labelLocation)
 		{
 		case 0:
 			getTopLabels()[pos] = value;
 			return;
 		case 1:
 			getBottomLabels()[pos] = value;
 			return;
 		case 2:
 			getLeftLabels()[pos] = value;
 			return;
 		case 3:
 			getRightLabels()[pos] = value;
 			return;
 		}
 		System.err.println("error: invalid label in BoardState::setLabel -> " + labelLocation);
 	}
 
 	/**
 	 * Gets the vector of all Transitions to this board state
 	 *
 	 * @return A Vector of the Transitions to this board state which are BoardStates
 	 */
 	public Vector<BoardState> getTransitionsTo()
 	{
 		return transitionsTo;
 	}
 
 	/**
 	 * Gets the vector of all Transitions from this board state
 	 *
 	 * @return A Vector of the Transitions from this board state which are BoardStates
 	 */
 	public Vector<BoardState> getTransitionsFrom(){
 		return transitionsFrom;
 	}
 	
 	/**
 	 * Sets the transitions to this BoardState
 	 * @param to Vector<BoardState> containing new transitionsTo
 	 */
 	public void setTransitionsTo(Vector<BoardState> to) {
 		transitionsTo = to;
 	}
 	
 	/**
 	 * Sets the transitions from this BoardState
 	 * @param from Vector<BoardState> containing new transitionsFrom
 	 */
 	public void setTransitionsFrom(Vector<BoardState> from) {
 		transitionsFrom = from;
 	}
 	
 	public ArrayList<Integer> getPathToNode()
 	{
 		ArrayList<Integer> retval = new ArrayList<Integer>();
 		BoardState back1 = (this.getTransitionsTo().size()>0)?this.getTransitionsTo().get(0):null;
 		if(back1 == null)return retval; //if there's no previous node, we're at the root, return an empty list (no steps to take to get to the root from the root)
 		retval.add(back1.getTransitionsFrom().indexOf(this));
 		retval.addAll(0,back1.getPathToNode()); //recursively prepend the rest of the path
 		return retval;
 	}
 	
 	public static BoardState evaluatePathToNode(ArrayList<Integer> path)
 	{
 		BoardState state = Legup.getInstance().getInitialBoardState();
 		for(int c1=0;c1<path.size();c1++)
 		{
 			state = state.getTransitionsFrom().get(path.get(c1));
 		}
 		return state;
 	}
 	
 	public boolean evalDelayStatus()
 	{
 		delayStatus = getStatus();
 		boolean rv = delayStatus == STATUS_UNJUSTIFIED || delayStatus == STATUS_RULE_CORRECT || delayStatus == STATUS_CONTRADICTION_CORRECT;
 		if(delayStatus == STATUS_RULE_CORRECT || delayStatus == STATUS_CONTRADICTION_CORRECT)
 		{
 			if(isModifiable())current_color = Color.green;
 		}
 		else if(delayStatus == STATUS_CASE_SETUP)
 		{
 			if(isModifiable())current_color = new Color(128,255,128);
 		}
 		else if(delayStatus != STATUS_UNJUSTIFIED || delayStatus == STATUS_RULE_INCORRECT || delayStatus == STATUS_RULE_INCORRECT)
 		{
 			if(isModifiable())current_color = Color.red;
 		}
 		else
 		{
 			current_color = TreePanel.nodeColor;
 		}
 		if(!isModifiable() && leadsToContradiction())
 		{
 			current_color = new Color(255,128,128);
 		}
 		for (BoardState B : transitionsFrom)
 		{
 			if(!B.evalDelayStatus())
 			{
 				rv = false;
 			}
 		}
 		return rv;
 	}
 
 	private void modifyStatus()
 	{
 		boolean prev1 = leadSolution, prev2 = leadContradiction;
 		int prevStat = status;
 		status = -1;
 		getStatus();
 
 		if ((leadSolution != prev1) || (leadContradiction != prev2))
 			for (BoardState B : transitionsTo) B.modifyStatus();
 		if (status != prevStat)
 			for (BoardState B : transitionsFrom) B.modifyStatus();
 	}
 
 	/**
 	 * Fire a transitions changed event
 	 */
 	private void transitionsChanged()
 	{
 		delayStatus = STATUS_UNJUSTIFIED;
 		modifyStatus();
 		for (BoardState B : transitionsTo) B.modifyStatus();
 		for (BoardState B : transitionsFrom) B.modifyStatus();
 		modifyStatus();
 	}
 
 	private static void _transitionsChanged()
 	{
 		//Legup.setCurrentState(Legup.getCurrentState()); //trigger a TreeSelectChanged also
 		for (int x = 0; x < Legup.transitionChangeListeners.size(); ++x) 
 		{
 			Legup.transitionChangeListeners.get(x).transitionChanged();
 		}
 	}
 	
 	//removes colors set by checkproof
 	public static void removeColorsFromTransitions()
 	{
 		removeColorsFromTransitions(Legup.getInstance().getInitialBoardState());
 		Legup.getInstance().getGui().getTree().treePanel.repaint();
 	}
 	private static void removeColorsFromTransitions(BoardState state)
 	{
 		/*if(state.isModifiable())*/state.setColor(TreePanel.nodeColor);
 		for(BoardState b : state.transitionsFrom)removeColorsFromTransitions(b);
 	}
 	
 	public static BoardState addTransition()
 	{
 		BoardState s = Legup.getCurrentState();
 		BoardState next = s;
 		if(s.getTransitionsFrom().size() == 0)
 		{
 			next = s.addTransitionFrom();
 		}
 		else if(s.getTransitionsFrom().size() >= 1)
 		{
 			next = s.getTransitionsFrom().firstElement();
 			if((next.getCaseRuleJustification() != null) && (!Legup.getInstance().getGui().autoGenCaseRules))
 			{
 				next = s.addTransitionFrom();
 			}
 			else
 			{
 				next = null;
 			}
 		}
 		return next;
 	}
 	
 	public BoardState conditionalAddTransition()
 	{
 		BoardState state = Legup.getCurrentState();
 		if (!state.isModifiable())
 		{
 			BoardState next = BoardState.addTransition();
 			if(next != null)
 			{
 				Legup.setCurrentState(next);
 				state = next;
 			}
 			else
 			{
 				state = null;
 			}
 		}
 		Legup.getInstance().getGui().getTree().colorTransitions();
 		return state;
 	}
 	
 	/**
 	 * Adds a transition from this board state.
 	 */
 	
 	public BoardState addTransitionFrom()
 	{
 		return addTransitionFrom(null);
 	}
 
 	public boolean parents(BoardState child)
 	{
 		for (BoardState B : transitionsFrom) if (B == child || B.parents(child)) return true;
 		return false;
 	}
 
 	/**
 	 *	Computes the Least Common Parent of the collection of BoardState
 	 *	LCP = The node of greatest depth that parents all nodes in collection
 	 */
 	public static BoardState lcp(Collection<? extends BoardState> col)
 	{
 		ArrayList<BoardState> states = new ArrayList<BoardState>(col);
 
 		while (states.size() > 1)
 		{
 			// Algorithm: Move all node references up to case splits
 			// Eliminate nodes that collide
 			// If more than one reference remains, check for parentage, and eliminate children
 			// If they still remain, move all nodes up one level, and repeat
 			// When merge node references goes up, send it automatically to the merge overlord
 			// Default: If the root node is reached, just return it
 			//		  No where else to go
 
 			// Step 1: move all nodes up
 			for (int i = 0; i < states.size(); i++)
 			{
 				boolean keepgoing = false;
 				do
 				{
 					keepgoing = false;
 					if (states.get(i).transitionsTo.size() == 0) return states.get(i);
 					if (states.get(i).transitionsFrom.size() < 2)
 					{
 						keepgoing = true;
 						if (states.get(i).transitionsTo.size() >= 2) states.set(i, states.get(i).mergeOverlord);
 						else states.set(i, states.get(i).transitionsTo.get(0));
 						if (states.get(i).transitionsTo.size() == 0) return states.get(i);
 					}
 				}
 				while (keepgoing);
 			}
 
 			// Step 2: eliminate coincidence
 			for (int i = 0; i < states.size()-1; i++) for (int j = i+1; j < states.size(); j++)
 				if (states.get(j) == states.get(i)) states.remove(j--);
 
 			if (states.size() == 1) return states.get(0);
 
 			// Step 3: eliminate parentage
 			for (int i = 0; i < states.size(); i++) for (int j = 0; j < states.size(); j++) if (j != i)
 				if (states.get(i).parents(states.get(j)))
 				{
 					if (j < i) i--;
 					states.remove(j--);
 				}
 
 			if (states.size() == 1) return states.get(0);
 
 			// Step 4: move each node up one level
 			for (int i = 0; i < states.size(); i++)
 				if (states.get(i).transitionsTo.size() == 1) states.set(i, states.get(i).transitionsTo.get(0));
 				else if (states.get(i).transitionsTo.size() >= 2) states.set(i, states.get(i).mergeOverlord);
 				else return states.get(i);
 		}
 
 		if (states.size() == 0)
 		{
 			System.out.println("ERRONEOUS INPUT!");
 			return null;
 		}
 		else
 			return states.get(0);
 	}
 
 	/**
 	 * Merge some board states
 	 * @param states the states to merge
 	 */
 	public static void merge(ArrayList <BoardState> states, boolean union)
 	{
 		/*/ make sure that no state is a direct ancestor of another state
 		for (BoardState s1 : states) {
 			for (BoardState s2 : states) {
 				if (s1 == s2)
 					continue;
 				if (s1.parents(s2))
 					return;
 			}
 		}*/
 		
 		// make sure that all states are leaves (very general and removes needing to check for ancestors)
 		for (BoardState s1 : states)
 		{
 			if (s1.getTransitionsFrom().size() > 0)
 				return;
 		}
 		
 		
 		BoardState child = states.get(0).copy();
 		
 		for (int c = 1; c < states.size(); ++c)
 		{
 			BoardState parent = states.get(c);
 
 			for (int y = 0; y < child.getHeight(); ++y)
 			{
 				for (int x = 0; x < child.getWidth(); ++x)
 				{
 					int childCell = child.getCellContents(x,y);
 					int parentCell = parent.getCellContents(x,y);
 
 					if (union)
 					{
 						// criteria for a union merge here
 						
 					}
 					else
 					{
 						// clear all differences
 						if (childCell != PuzzleModule.CELL_UNKNOWN && childCell != parentCell)
 						{
 							child.setCellContents(x, y, PuzzleModule.CELL_UNKNOWN);
 						}
 					}
 				}
 			}
 			for(int c1=0;c1<child.getExtraData().size();c1++)
 			{
 				Object obj = child.getExtraData().get(c1);
 				if(!parent.getExtraData().contains(obj))
 				{
 					child.getExtraData().remove(c1);
 					c1--;
 				}
 			}
 		}
 
 		// add transitions
 		for (int c = 0; c < states.size(); ++c)
 		{
 			BoardState parent = states.get(c);
 			parent.transitionsFrom.add(child);
 			child.transitionsTo.add(parent);
 		}
 
 		child.justification = RuleMerge.getInstance();
 		
 		child.mergeOverlord = lcp(states);
 		child.mergeOverlord.mergeChildren.add(child);
 		child.mergeOverlord.evalMergeY();
 		child.mergeOverlord.evalMerge(1);
 		
 		child.setModifiableState(true);
 		BoardState grandchild = child.addTransitionFrom(null);
 
 		Legup.setCurrentState(grandchild);
 
 		_transitionsChanged();
 	}
 	
 	/**
 	 * Identifies whether a state is the product of a merge. Used to make sure
 	 * the state cannot be edited.
 	 * @return the state has multiple parents
 	 */
 	public boolean isMergeTransition()
 	{
 		return transitionsTo.size() > 1;
 	}
 
 	//Expand collapsed nodes?
 	private void expandXSpace(BoardState child)
 	{
 		if (transitionsFrom.size() > 1 && child != null)
 		{
 			boolean foundChild = false;
 			for (BoardState B : transitionsFrom) if (B.transitionsTo.size() == 1)
 			{
 				if (B == child)
 					foundChild = true;
 				else if (!foundChild)
 					B.offset.x -= (int)(1.5 * TreePanel.NODE_RADIUS);
 				else
 					B.offset.x += (int)(1.5 * TreePanel.NODE_RADIUS);
 			}
 		}
 
 		if (transitionsTo.size() == 1)
 			transitionsTo.get(0).expandXSpace(this);
 		else if (transitionsTo.size() >= 2)
 			mergeOverlord.evalMerge(1);
 		else
 			recalculateLocation();
 	}
 
 	//collapse nodes
 	private void contractXSpace(BoardState child)
 	{
 		if (transitionsFrom.size() > 1 && child != null)
 		{
 			boolean foundChild = false;
 			for (BoardState B : transitionsFrom) if (B.transitionsTo.size() == 1)
 			{
 				if (B == child)
 					foundChild = true;
 				else if (!foundChild)
 					B.offset.x += (int)(1.5 * TreePanel.NODE_RADIUS);
 				else
 					B.offset.x -= (int)(1.5 * TreePanel.NODE_RADIUS);
 			}
 		}
 
 		if (transitionsTo.size() == 1)
 			transitionsTo.get(0).contractXSpace(this);
 		else if (transitionsTo.size() >= 2)
 			mergeOverlord.evalMerge(-1);
 		else
 			recalculateLocation();
 	}
 
 	//what does this do?
 	private void evalMerge(int change)
 	{
 		int mergeTot = 0;
 		int directTot = numDirectBranches();
 		
 		for (BoardState B : mergeChildren)
 		{
 			mergeTot += B.numBranches();
 		}
 
 		if (change == 1 && mergeTot == directTot+1)
 			expandXSpace(null);
 		else if (change == -1 && mergeTot == directTot)
 			contractXSpace(null);
 		else
 			recalculateLocation();
 	}
 
 	//returns total number of children
 	private int numDirectBranches()
 	{
 		ArrayList<BoardState> valid = new ArrayList<BoardState>();
 		for (BoardState B : transitionsFrom) if (B.transitionsTo.size() == 1) valid.add(B);
 
 		if (valid.size() == 0)
 			return 1;
 		int tot = 0;
 		for (BoardState B : valid) tot += B.numDirectBranches();
 		return tot;
 	}
 
 	private void evalMergeY()
 	{
 		if (mergeChildren.size() > 0)
 		{
 			int depth = getDepth();
 			int mergeTot = 0; for (BoardState B : mergeChildren) mergeTot += B.numBranches();
 
 			int place = -(mergeTot-1)*(int)(1.5*TreePanel.NODE_RADIUS);
 			for (BoardState B : mergeChildren)
 			{
 				B.offset.y = (1+depth)*4*TreePanel.NODE_RADIUS;
 				B.offset.x = place+(B.numBranches()-1)*((int)(1.5*TreePanel.NODE_RADIUS));
 				place += B.numBranches()*3*TreePanel.NODE_RADIUS;
 			}
 
 			recalculateLocation();
 		}
 
 		if (transitionsTo.size() == 1)
 			transitionsTo.get(0).evalMergeY();
 		else if (transitionsTo.size() == 0)
 			recalculateLocation();
 		else
 			mergeOverlord.evalMergeY();
 	}
 
 	private int getDirectDepth()
 	{
 		int maxDirect = 0; boolean isMax = false;
 		for (BoardState B : transitionsFrom) if (B.transitionsTo.size() == 1)
 		{
 			isMax = true;
 			int pot = B.getDepth();
 			if (pot > maxDirect) maxDirect = pot;
 		}
 		if (isMax) maxDirect++;
 		return maxDirect;
 	}
 
 	private int getMergeDepth()
 	{
 		int maxMerge = 0; boolean isMax = false;
 		for (BoardState B : mergeChildren)
 		{
 			isMax = true;
 			int pot = B.getDepth();
 			if (pot > maxMerge) maxMerge = pot;
 		}
 		if (isMax) maxMerge++;
 		return maxMerge;
 	}
 
 	public int getDepth()
 	{
 		//return getDirectDepth()+getMergeDepth();
 		int tmp_max = -1;
 		for(BoardState b : transitionsFrom)
 		{
 			if(b.getDepth() > tmp_max)tmp_max = b.getDepth();
 		}
 		return tmp_max+1;
 	}
 	// .... to this comment are all related to computation of position for
 	// regular nodes and Merge nodes
 	// Merge nodes are a lot more complicated :(
 
 	/**
 	 *	Returns the number of branches taken up by the BoardState
 	 */
 	private int numBranches()
 	{
 		if (transitionsFrom.size() == 0)
 			return 1;
 		else if (transitionsFrom.size() == 1)
 			return transitionsFrom.get(0).numBranches();
 		else
 		{
 			int mergeTot = 0, directTot = 0;
 			for (BoardState B : mergeChildren) mergeTot += B.numBranches();
 			for (BoardState B : transitionsFrom) if (B.transitionsTo.size() == 1) directTot += B.numBranches();
 			return Math.max(mergeTot, directTot);
 		}
 	}
 
 	/**
 	 * Adds a transition from this board state given a PuzzleRule
 	 * @param rule the rule to be applied to go from this state to the child state
 	 */
 	public BoardState addTransitionFrom(PuzzleRule rule)
 	{
 		BoardState b = copy();
 		b.setModifiableState(!modifiableState);
 
 		addTransitionFrom(b, rule);
 
 		return b;
 	}
 	/*public BoardState addTransitionFromWithNoMovement()
 	{
 		BoardState b = copy();
 		b.setModifiableState(!modifiableState);
 		transitionsFrom.add(b);
 		b.transitionsTo.add(this);
 		
 		return b;
 	}*/
 	/**
 	 * Adds a transition from this board state.
 	 * @param b: the new child state
 	 * @param rule: the rule we applied
 	 */
 	public void addTransitionFrom(BoardState b, PuzzleRule rule)
 	{
 		Legup.getInstance().getGui().getTree().pushUndo();
 		transitionsFrom.add(b);
 		 b.transitionsTo.add(this);
 		 b.justification = rule;
 
 		b.offset.x = 0;
 		b.offset.y = TreePanel.NODE_RADIUS * 5;
 
 		ArrayList<BoardState> valid = new ArrayList<BoardState>();
 		for (BoardState B : transitionsFrom) if (B.transitionsTo.size() == 1) valid.add(B);
 
 		if (valid.size() != 1) // if there are other children
 		{
 			// move all the children over by node radius, then add it
 			for (int x = 0; x < valid.size()-1; ++x)
 			{
 				BoardState child = valid.get(x);
 				child.offset.x -= (1.5 * TreePanel.NODE_RADIUS);
 			}
 
 			if (valid.size() >= 2)
 				b.offset.x = valid.get(valid.size()-2).offset.x+(valid.get(valid.size()-2).numBranches()+1)*(int)(1.5*TreePanel.NODE_RADIUS);
 
 			if (transitionsTo.size() >= 2)
 				mergeOverlord.evalMerge(1);
 			else if (transitionsTo.size() == 1)
 				transitionsTo.get(0).expandXSpace(this);
 		}
 
 		if (transitionsTo.size() == 0)
 			recalculateLocation();
 		else if (transitionsTo.size() >= 2)
 			mergeOverlord.evalMergeY();
 		else
 			transitionsTo.get(0).evalMergeY();
 		
 		if (!virtualBoard)
 		{
 			transitionsChanged();
 			_transitionsChanged();
 		}
 	}
 
 	/**
 	 * Adds a transition from this board state.
 	 * @param child the new child state
 	 */
 	public void addTransitionFrom(BoardState child, String justification, boolean isCase)
 	{
 		transitionsFrom.add(child);
 		child.transitionsTo.add(this);
 		if(isCase)
 		{
 			this.setCaseRuleJustification(Legup.getInstance().getPuzzleModule().getCaseRuleByName(justification));
 		}
 		else
 		{
 			child.justification = Legup.getInstance().getPuzzleModule().getRuleByName(justification);
 		}
 
 		transitionsChanged();
 		_transitionsChanged();
 	}
 	
 	/**
 	 * Finishes the changes made by the current transition state
 	 * @return child BoardState created by this operation or already existing child BoardState
 	 */ 
 	public BoardState endTransition() {
 		
 		//
 		if (!modifiableState)
 			return null;
 		else if (transitionsFrom.size() > 0)
 			return transitionsFrom.get(0);
 		
 		BoardState child = addTransitionFrom();
 		child.editedToModifiable();
 		
 		return child;
 	}
 
 	/**
 	 * Arranges children to be next to each other
 	 */
 	@Deprecated // As of 10-09-08
 	public void arrangeChildren()
 	{
 		int size = transitionsFrom.size();
 		if (size != 0)
 		{
 			for (int x = 0; x < size; ++x)
 			{
 				BoardState child = transitionsFrom.get(x);
 				child.offset.x = (int)(3 * TreePanel.NODE_RADIUS * (x-((size-1)/2.0)));
 				child.offset.y = TreePanel.NODE_RADIUS * 3;
 			}
 			recalculateLocation();
 		}
 	}
 
 	/**
 	 * Get the single parent state of this state, or null if there are multiple or no parents
 	 * @return the parent state, or null if there isn't a single parent
 	 */
 	public BoardState getSingleParentState()
 	{
 		BoardState rv = null;
 
 		Vector<BoardState> parents = getTransitionsTo();
 
 		if (parents.size() == 1)
 			rv = parents.get(0);
 
 		return rv;
 	}
 	
 	public BoardState getFirstChild()
 	{
 		return getTransitionsFrom().firstElement();
 	}
 
 	/**
 	 * Makes a copy of the current board state.
 	 *
 	 * @return New BoardState that is a copy of this board state
 	 */
 	public BoardState copy()
 	{
 		BoardState newBoardState = null;
 		try
 		{
 			newBoardState = new BoardState(this.getHeight(),this.getWidth());
 			newBoardState.virtualBoard = virtualBoard;
 			newBoardState.modifiableState = modifiableState;
 			if(modifiableState)newBoardState.setOffsetRaw(new Point(0, (int)(4.5*TreePanel.NODE_RADIUS)));
 			else newBoardState.setOffsetRaw(new Point(0, 0));
 		}
 		catch(Exception e)
 		{
 			return newBoardState;
 		}
 		
 		// Initialize the arrays
 		for(int i=0;i<getHeight();i++)
 		{
 			newBoardState.getLeftLabels()[i]=getLeftLabels()[i];
 			newBoardState.getRightLabels()[i]=getRightLabels()[i];
 			
 			for (int j=0;j<getWidth();j++)
 			{
 				newBoardState.getBoardCells()[i][j] = getBoardCells()[i][j];
 				newBoardState.modifiableCells[i][j] = modifiableCells[i][j];
 				newBoardState.editedCells[i][j] = editedCells[i][j];
 				if (i==0)
 				{
 					newBoardState.getTopLabels()[j] = getTopLabels()[j];
 					newBoardState.getBottomLabels()[j] = getBottomLabels()[j];
 				}
 			}
 		}
 		
 		// copy the extra data
 		newBoardState.setExtraData(copyExtraData());
		newBoardState.extraDataDelta = new ArrayList<Object>(extraDataDelta);
 		
 		// copy the location
 		newBoardState.location = new Point(location.x, location.y);
 		
 		return newBoardState;
 	}
 	
 	protected ArrayList<Object> copyExtraData()
 	{
 		return new ArrayList<Object>( extraData );
 	}
 
 	/**
 	 * Compares two boards.  If all the cell values match, it will return true.
 	 *
 	 * @param compareBoard BoardState to compare to
 	 * @return True if the board states match
 	 */
 	public boolean compareBoard(BoardState compareBoard){
 	if (this.getHeight() != compareBoard.getHeight() ||
 		this.getWidth() != compareBoard.getWidth()){
 		return false;
 	}
 
 	for (int i=0;i<getHeight();i++){
 		for (int j=0;j<getWidth();j++){
 		if (this.getBoardCells()[i][j] != compareBoard.getBoardCells()[i][j]){
 			return false;
 		}
 		}
 	}
 
 	return true;
 	}
 
 	/**
 	 * Gets the Justification for this board state
 	 *
 	 * @return a PuzzleRule, Contradiction, or null
 	 */
 	public Justification getJustification()
 	{
 		return justification;
 	}
 
 	/**
 	 * Sets the Justification for this board state
 	 */
 	public void setJustification(Justification j)
 	{
 		// don't change justification to a contradiction if there are changes
 		if (j instanceof Contradiction) {
 			for (int y = 0; y < getHeight(); y++) {
 				for (int x = 0; x < getWidth(); x++) {
 					if (editedCells[y][x])
 						return;
 				}
 			}
 		}
 			
 		//TODO this crap (Justification) justification.setName((string) j);
 		justification = j;
 		JustificationFrame.justificationApplied(this,j);
 		modifyStatus();
 		delayStatus = STATUS_UNJUSTIFIED;
 	}
 
 	 int delayStatus = -1;
 	 public int getDelayStatus()
 	 {
 		return delayStatus;
 	 }
 
 	 // Used to keep track of last verdict
 	 private int status = -1;
 	/**
 	 * Get the STATUS_ value of this state's justification
 	 * @return the status of the board state's justification
 	 */
 	public int getStatus()
 	{
 		//if (status == -1)
 		{
 			//leadsToContradiction();
 			//leadsToSolution();
 
 			status = STATUS_UNJUSTIFIED;
 
 			Object o = getJustification();
 
 			if (o != null)
 			{
 				if (o instanceof Contradiction)
 				{
 					Contradiction c = (Contradiction)o;
 					justificationText = c.checkContradiction(this);
 					if (justificationText == null)
 						status = STATUS_CONTRADICTION_CORRECT;
 					else
 						status = STATUS_CONTRADICTION_INCORRECT;
 				}
 				else if (o instanceof PuzzleRule)
 				{
 					PuzzleRule pz = (PuzzleRule)o;
 					justificationText = pz.checkRuleRaw(this);
 					if (justificationText == null)
 						status = STATUS_RULE_CORRECT;
 					else
 						status = STATUS_RULE_INCORRECT;
 				}
 			}
 			else
 			{
 				if(getCaseSplitJustification() != null)
 				{
 					justificationText = isJustifiedCaseSplit(); 
 					if(justificationText == null)status = STATUS_RULE_CORRECT;
 					else if(justificationText == CaseRule.caseSetupMessage())status = STATUS_CASE_SETUP;
 					else status = STATUS_RULE_INCORRECT;
 				}
 			}
 		}
 
 		return status;
 	}
 
 	/**
 	 * Does boardState s follow from the root state (with rules)
 	 * @param s the BoardState to check
 	 */
 	public static boolean followsFromRoot(BoardState s)
 	{
 		BoardState root = Legup.getInstance().getInitialBoardState();
 		boolean rv = false;
 
 		if (s == root)
 			rv = true;
 		else
 		{
 			if (s.getStatus() == STATUS_RULE_CORRECT)
 			{
 				for (int x = 0; x < s.transitionsTo.size(); ++x)
 				{
 					rv = followsFromRoot(s.transitionsTo.get(x));
 
 					if (rv)
 						break;
 				}
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Convert this boardstate to a SaveablBoardState (used to save puzzles)
 	 * @return the SaveableBoardState equivilent to this BoardState
 	 */
 	public SaveableBoardState getAsSaveableBoardState()
 	{
 		SaveableBoardState s = new SaveableBoardState();
 
 		s.height = getHeight();
 		s.width = getWidth();
 		s.boardCells = getBoardCells();
 		s.bottomLabels = getBottomLabels();
 		s.leftLabels = getLeftLabels();
 		s.rightLabels = getRightLabels();
 		s.topLabels = getTopLabels();
 		s.extraData = extraData;
 		s.location = location;
 
 		return s;
 	}
 
 	/**
 	 * Set this board state from a given SaveableBoardState (which we probably loaded from a file)
 	 * @param s the saveableboardstate we're loading from
 	 * @return the resultant BoardState
 	 */
 	public static BoardState loadFromSaveableBoardState(SaveableBoardState s)
 	{
 		BoardState rv = null;
 
 		if (s != null)
 		{
 			rv = new BoardState(s.height,s.width);
 
 			rv.setPuzzleName(s.puzzleMod);
 
 			rv.setBoardCells(s.boardCells);
 			rv.setBottomLabels(s.bottomLabels);
 			rv.setLeftLabels(s.leftLabels);
 			rv.setRightLabels(s.rightLabels);
 			rv.setTopLabels(s.topLabels);
 			rv.extraData = s.extraData;
 			rv.location = s.location;
 
 			// set modification properties such that any initial data is unmodifiable
 			for (int y = 0; y < rv.getHeight(); ++y) for (int x = 0; x < rv.getWidth(); ++x)
 			{
 				if (rv.getBoardCells()[y][x] != PuzzleModule.CELL_UNKNOWN)
 				{
 					rv.modifiableCells[y][x] = false;
 				}
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Get an ArrayList consisting of the Points in which these two board states differ
 	 * If they are not of the same size, null will be returned
 	 *
 	 * @param one a BoardState
 	 * @param two another BoardState
 	 * @return an ArrayList of the Points where these BoardStates differ
 	 */
 	public static ArrayList<Point> getDifferenceLocations(BoardState one, BoardState two)
 	{
 		ArrayList<Point> rv = new ArrayList<Point>();
 
 		if (one.getWidth() != two.getWidth() || one.getHeight() != two.getHeight())
 		{
 			rv = null;
 		}
 		else
 		{ // check each point
 			for (int y = 0; y < one.getHeight(); ++y)
 			{
 				for (int x = 0; x < one.getWidth(); ++x)
 				{
 					int val1 = one.getCellContents(x,y);
 					int val2 = two.getCellContents(x,y);
 
 					if (val1 != val2)
 					{
 						rv.add(new Point(x,y));
 					}
 				}
 			}
 		}
 
 		return rv;
 	}
 
 	private boolean leadContradiction = false;
 	/**
 	 * Does this board state lead to a contradiction?
 	 * @param state the state we're checking
 	 * @return true iff this board state (always) leads to a contradiction
 	 */
 	public boolean leadsToContradiction()
 	{
 		if(leadsToInvalidInference())return false; //since a contradiction obtained after an invalid inference doesn't reflect poorly on the states before
 		if(transitionsFrom.size() > 0)
 		{
 			BoardState next;
 			if(transitionsFrom.get(0).getCaseRuleJustification() != null)
 			{
 				if(numNonContradictoryChildren() == 0)return true;
 				next = doesNotLeadToContradiction();
 			}
 			else
 			{
 				next = transitionsFrom.get(0);
 			}
 			return (next != null) ? next.leadsToContradiction() : (getJustification() instanceof Contradiction);
 		}
 		else return (getJustification() instanceof Contradiction); //if there are no children
 		/*if (status != -1)
 			return leadContradiction;
 
 		if (justification instanceof Contradiction && ((Contradiction)justification).checkContradiction(this) == null)
 		{
 			status = STATUS_CONTRADICTION_CORRECT;
 			return (leadContradiction = true);
 		}
 
 		// does this boardstate lead to a contradiction?
 		Vector <BoardState> children = this.getTransitionsFrom();
 		boolean rv = false;
 
 		if (children.size() == 1)
 		{
 			BoardState child = children.get(0);
 
 			if (child.getStatus() == BoardState.STATUS_CONTRADICTION_CORRECT)
 				rv = true;
 			else if (child.getStatus() == BoardState.STATUS_RULE_CORRECT)
 				rv = child.leadsToContradiction();
 		}
 		else if (children.size() > 1)
 		{
 			if (this.isJustifiedCaseSplit() == null) // if it's valid
 			{
 				rv = true; // we're valid until we find a child that doesn't lead to a contradiction
 
 				for (int c = 0; c < children.size(); ++c)
 				{
 					BoardState child = children.get(c);
 
 					if (child.getStatus() == BoardState.STATUS_CONTRADICTION_CORRECT)
 						continue;
 					else if (!child.leadsToContradiction()) // we've found an invalid one
 					{
 						rv = false;
 						break;
 					}
 				}
 			}
 		}
 
 		return (leadContradiction = rv);*/
 	}
 	//checks all children, returns the BoardState that does not lead to a contradiction
 	//returns null if the number of children that do not lead to a contradiction is not exactly 1
 	public BoardState doesNotLeadToContradiction()
 	{
 		BoardState rv = null;
 		int numNonContradictions = 0;
 		for(BoardState child : transitionsFrom)
 		{
 			if(!child.leadsToContradiction())
 			{
 				numNonContradictions++;
 				rv = (numNonContradictions == 1) ? child : null;
 			}
 		}
 		return rv;
 	}
 	
 	public int numNonContradictoryChildren()
 	{
 		int numNonContradictions = 0;
 		for(BoardState child : transitionsFrom)
 		{
 			if(!child.leadsToContradiction())numNonContradictions++;
 		}
 		return numNonContradictions;
 	}
 	
 	public boolean leadsToInvalidInference()
 	{
 		if((status == STATUS_RULE_INCORRECT) || (status == STATUS_CONTRADICTION_INCORRECT))
 		{
 			return true;
 		}
 		for(BoardState b : transitionsFrom)
 		{
 			if(b.leadsToInvalidInference())return true;
 		}
 		return false;
 	}
 	
 	private boolean leadSolution = false;
 	/**
 	 * Does this board state lead to the solution?
 	 * @param state the state we're checking
 	 * @return true iff this board state (always) leads to a contradiction
 	 */
 	public boolean leadsToSolution()
 	{
 		if (status != -1)
 			return leadSolution;
 
 		if(this.isSolution)
 			return (leadSolution = true);
 
 		Vector <BoardState> children = this.getTransitionsFrom();
 
 		if (children.size() == 1)
 		{
 			BoardState child = children.get(0);
 
 			if (child.getStatus() == BoardState.STATUS_RULE_CORRECT)
 			{
 				return (leadSolution = child.leadsToSolution());
 			}
 		}
 		else if (children.size() > 1)
 		{
 			if (this.isJustifiedCaseSplit() == null) // if it's valid
 			{
 				for (int c = 0; c < children.size(); ++c)
 				{
 					BoardState child = children.get(c);
 
 					if(child.leadsToSolution())
 						return (leadSolution = true);
 				}
 			}
 		}
 
 		return (leadSolution = false);
 	}
 
 	private void removeLeaf(BoardState B)
 	{
 		boolean onlyRegChild = true;
 		for (BoardState BS : transitionsFrom) {
 			if (BS != B && BS.transitionsTo.size() == 1) {
 				onlyRegChild = false;
 				break;
 			}
 		}
 
 		if (!onlyRegChild) contractXSpace(B);
 		transitionsFrom.remove(B);
 
 		if (!virtualBoard) transitionsChanged();
 	}
 
 	private void removeUnderling(BoardState B)
 	{
 		mergeChildren.remove(B);
 		evalMerge(-1);
 		evalMergeY();
 	}
 
 	/**
 	 * Delete this state, and therefore all it's children too
 	 * @param s the state we're deleting
 	 * Modified 9/30/2008 to account for x-space methods
 	 */
 	public static void deleteState(BoardState s)
 	{
 		Legup.getInstance().getGui().getTree().pushUndo();
 		s.subDelete();
 
 		if (!s.virtualBoard) _transitionsChanged();
 	}
 
 	private void subDelete()
 	{
 		while (mergeChildren.size() > 0) mergeChildren.get(0).subDelete();
 		while (transitionsFrom.size() > 0) transitionsFrom.get(0).subDelete();
 
 		if (mergeOverlord != null) { mergeOverlord.removeUnderling(this); mergeOverlord = null; }
 		for (BoardState B : transitionsTo) B.removeLeaf(this);
 		transitionsTo.clear();
 	}
 
 	/**public static void deleteState(BoardState s, boolean saveChildren)
 	{
 		if(saveChildren)
 		{
 			for(BoardState parent : s.getTransitionsTo())
 			{
 				for(BoardState child : s.getTransitionsFrom())
 				{
 					parent.transitionsFrom.add(child);
 					child.transitionsTo.add(parent);
 				}
 			}
 		}
 		deleteState(s);
 	}*/
 
 	public static void reparentChildren(BoardState oldParent, BoardState newParent)
 	{
 		
 		if (oldParent.getCaseRuleJustification() != null) {
 			if (newParent.getCaseRuleJustification() != oldParent.getCaseRuleJustification() && newParent.getTransitionsFrom().size() == 0)
 				newParent.setCaseRuleJustification(oldParent.getCaseRuleJustification());
 			oldParent.setCaseRuleJustification((CaseRule)null);
 		}
 		
 		for(BoardState child : oldParent.getTransitionsFrom())
 		{
 			child.transitionsTo.clear();
 			newParent.transitionsFrom.add(child);
 			child.transitionsTo.add(newParent);
 			//newParent.expandXSpace(child);
 		}
 		
 		while (oldParent.getTransitionsFrom().size() > 0)
 			oldParent.getTransitionsFrom().remove(0);
 		
 		if (!oldParent.virtualBoard)
 			oldParent.transitionsChanged();
 		if (!newParent.virtualBoard)
 			newParent.transitionsChanged();
 	}
 
 	public Point getLocation()
 	{
 		return new Point(location.y, location.x);
 	}
 
 	public void setLocation(Point location)
 	{
 		this.location = location;
 	}
 	
 	public void resetLocation()
 	{
 		location = new Point(0,0);
 	}
 	
 	public Point getOffset()
 	{
 		return offset;
 	}
 
 	public void setOffset(Point offset)
 	{
 		this.offset = offset;
 		this.recalculateLocation();
 	}
 	
 	public void setOffsetRaw(Point offset)
 	{
 		this.offset = offset;
 		BoardState parent = (transitionsTo.size() > 0) ? transitionsTo.get(0) : null;
 		if(parent != null)parent.setOffsetRaw(parent.offset);
 		this.location = (parent != null) ? new Point(parent.location.x+offset.x,parent.location.y+offset.y) : offset;
 	}
 
 	public void recalculateLocation()
 	{
 		if (this.getTransitionsTo().size() == 1)
 		{
 			BoardState parentState = this.getSingleParentState();
 			Point p = new Point(parentState.getLocation().y, parentState.getLocation().x);
 			this.location.x = p.x + offset.x;
 			this.location.y = p.y + offset.y;
 			
 			//this.location.x = this.transitionsTo.lastElement().getLocation().x + this.offset.x;
 
 			//If this and its parent are collapsed, their locations are ontop of each other
 			//Places this over where the previous actual state is if it functions as a transition (isModifiable)
 			if ((this.isCollapsed() && this.getSingleParentState().isCollapsed()) || !this.isModifiable())
 				this.location.x = p.x;
 			else
 				this.location.x = p.x + offset.x;
 		}
 		else if(this.getTransitionsTo().size() == 0)
 		{
 			this.location.x = offset.x;
 			this.location.y = offset.y;
 		}
 		else // Merge Case - All calculations are performed when Tree is edited
 		{
 			if (mergeOverlord != null) // Safeguard for complex delete function
 			{
 				this.location.x = mergeOverlord.location.x + offset.x;
 				this.location.y = mergeOverlord.location.y + offset.y;
 			}
 		}
 
 		for (BoardState s : transitionsFrom) s.recalculateLocation();
 		for (BoardState s : mergeChildren) s.recalculateLocation();
 	}
 
 	public void setExtraData(ArrayList<Object> extraData)
 	{
 		this.extraData = extraData;
 	}
 
 
 	public String getPuzzleName()
 	{
 		return puzzleName;
 	}
 
 
 
 	public boolean isCollapsed()
 	{
 		return collapsed;
 	}
 
 
 	public int countStates()
 	{
 		int count = 1;
 		for(BoardState s : this.transitionsFrom)
 		{
 			count += s.countStates();
 		}
 		return count;
 	}
 
 	public int countLeaves()
 	{
 		int count = 0;
 		if(this.transitionsFrom.size() == 0)
 			return 1;
 		else
 		{
 			for(BoardState s : this.transitionsFrom)
 			{
 				count += s.countLeaves();
 			}
 		}
 		return count;
 	}
 
 	public int countDepth()
 	{
 		int count = 0;
 		for(BoardState s : this.transitionsFrom)
 		{
 			count = Math.max(s.countDepth(), count);
 		}
 		return count + 1;
 	}
 	
 	//return the unique leaf node that is not a contradiction, or null if none exists
 	public BoardState getFinalState()
 	{
 		if(this.leadsToContradiction())return null;
 		else if(this.transitionsFrom.size() == 0)return this; //leaf node
 		else if(this.transitionsFrom.size() == 1)return this.transitionsFrom.lastElement().getFinalState();
 		else return (doesNotLeadToContradiction() != null)?doesNotLeadToContradiction().getFinalState():null;
 	}
 
 	public void setAsSolution()
 	{
 		this.isSolution = true;
 	}
 
 
 	//*******************
 	//Hint cell methods
 	//*******************
 
 	/**
 	 * Adds a hint cell to the current collection
 	 */
 	public void addHintCell(Point cell)
 	{
 		if(!hintCells.contains(cell))
 			hintCells.add(cell);
 	}
 
 	/**
 	 * Adds a rectangular range of hint cells to the current collection
 	 * @param cell1 First corner cell
 	 * @param cell2 Second corner cell
 	 */
 	public void addHintCellRange(Point cell1, Point cell2)
 	{
 		int width = Math.abs(cell1.x - cell2.x);
 		int sx = Math.min(cell1.x, cell2.x);
 		int height = Math.abs(cell1.y - cell2.y);
 		int sy = Math.min(cell1.y, cell2.y);
 
 		for(int y = 0; y < height; ++y)
 		{
 			for(int x = 0; x < width; ++x)
 			{
 				addHintCell(new Point(sx + x, sy + y));
 			}
 		}
 	}
 
 	/**
 	 * Removes the hint cell from the current collection
 	 * @param cell The cell to remove
 	 */
 	public void removeHintCell(Point cell)
 	{
 		hintCells.remove(cell);
 	}
 
 	/**
 	 * Sets the hint cell as the only hint cell
 	 * @param cell The sell to set a the hint
 	 */
 	public void setHintCell(Point cell)
 	{
 		clearHintCells();
 		addHintCell(cell);
 	}
 
 	/**
 	 * Retrieves the list of hint cells
 	 * @return A list of current hint cells
 	 */
 	public ArrayList<Point> getHintCells()
 	{
 		return hintCells;
 	}
 
 	/**
 	 * Clears all hint cells
 	 */
 	public void clearHintCells()
 	{
 		hintCells.clear();
 	}
 
 
 	//***********************
 	//Proof Saving Procedures
 	//***********************
 
 	public static BoardState fromSaveableProofState(SaveableProofState ps)
 	{
 		if(ps == null)
 			return null;
 
 		BoardState bs = new BoardState(ps.height, ps.width);
 		bs.setBoardCells(ps.boardCells);
 		//bs.modifiableCells = ps.modifiableCells;
 		bs.setTopLabels(ps.topLabels);
 		bs.setBottomLabels(ps.bottomLabels);
 		bs.setLeftLabels(ps.leftLabels);
 		bs.setRightLabels(ps.rightLabels);
 		bs.setPuzzleName(ps.puzzleName);
 		//bs.collapsed = ps.collapsed;
 		bs.collapsed = false;
 		//bs.hintCells = ps.hintCells;
 		//bs.offset = ps.offset;
 		bs.offset = new Point(0,0);
 		//bs.setExtraData(ps.extraData);
 
 		Legup.getInstance().loadPuzzleModule(bs.getPuzzleName());
 		//Legup.getInstance().loadPuzzleModule("Battleships");
 
 		return bs;
 	}
 
 	private SaveableProofState toSaveableProofState()
 	{
 		SaveableProofState s = new SaveableProofState();
 
 		s.height = this.getHeight();
 		s.width = this.getWidth();
 		s.boardCells = this.getBoardCells();
 		s.boardCells = this.boardCells;
 		s.modifiableCells = this.modifiableCells;
 		s.topLabels = this.getTopLabels();
 		s.bottomLabels = this.getBottomLabels();
 		s.leftLabels = this.getLeftLabels();
 		s.rightLabels = this.getRightLabels();
 		s.puzzleName = this.getPuzzleName();
 		s.extraData = this.extraData;
 		s.collapsed = this.collapsed;
 		s.hintCells = this.hintCells;
 		s.offset = this.offset;
 
 		return s;
 	}
 
 	private int id = 0;
 	public int calcID()
 	{
 		resetID();
 		return calcID(0);
 	}
 
 	private void resetID()
 	{
 		this.id = 0;
 		for(BoardState b : transitionsFrom)
 		{
 			b.resetID();
 		}
 	}
 
 	private int calcID(int lastID)
 	{
 		if(this.id != 0)
 			return lastID;
 		this.id = lastID;
 		++lastID;
 		for(BoardState b : transitionsFrom)
 		{
 			lastID = b.calcID(lastID);
 		}
 		return lastID;
 	}
 
 	public void makeSaveableProof(SaveableProofState[] states, Vector<SaveableProofTransition> transitions)
 	{
 		states[this.id] = this.toSaveableProofState();
 		if(this.transitionsFrom.size() == 1)
 		{
 			BoardState child = this.transitionsFrom.get(0);
 			if( PuzzleRule.class.isInstance(child.justification))
 				transitions.add(new SaveableProofTransition(this.id, child.id, ((PuzzleRule)child.justification).getName(), false));
 			else if( Contradiction.class.isInstance(child.justification))
 				transitions.add(new SaveableProofTransition(this.id, child.id, ((Contradiction)child.justification).getName(), false));
 			child.makeSaveableProof(states, transitions);
 		}
 		else if(this.transitionsFrom.size() > 1)
 		{
 			for(BoardState b : this.transitionsFrom)
 			{
 				transitions.add(new SaveableProofTransition(this.id, b.id, this.getCaseRuleJustification().getName(), true));
 				b.makeSaveableProof(states, transitions);
 			}
 		}
 	}
 
 	public void addHint() {
 		hintsGiven += 1;
 	}
 	public int getHints() {
 		return hintsGiven;
 	}
 
 	public void setTopLabels(int[] topLabels) {
 		this.topLabels = topLabels;
 	}
 
 	public int[] getTopLabels() {
 		return topLabels;
 	}
 
 	public void setBottomLabels(int[] bottomLabels) {
 		this.bottomLabels = bottomLabels;
 	}
 
 	public int[] getBottomLabels() {
 		return bottomLabels;
 	}
 
 	public void setLeftLabels(int[] leftLabels) {
 		this.leftLabels = leftLabels;
 	}
 
 	public int[] getLeftLabels() {
 		return leftLabels;
 	}
 
 	public void setRightLabels(int[] rightLabels) {
 		this.rightLabels = rightLabels;
 	}
 
 	public int[] getRightLabels() {
 		return rightLabels;
 	}
 
 	public void setBoardCells(int[][] boardCells) {
 		this.boardCells = boardCells;
 	}
 
 	/**
 	 *  Returns the array of board cells
 	 * @param none
 	 * @return array of board cells
 	 */
 	public int[][] getBoardCells() {
 		return boardCells;
 	}
 
 	public void setChangedCells(Vector <Point> changedCells) {
 		this.changedCells = changedCells;
 	}
 
 	public Vector <Point> getChangedCells() {
 		return changedCells;
 	}
 	
 	public boolean extraDataChanged()
 	{
 		if(getSingleParentState() == null)return false;
 		ArrayList <Object> prevExtraData = getSingleParentState().getExtraData();
 		if(extraData.size() != prevExtraData.size())return true;
 		for(int c1=0;c1<extraData.size();c1++)
 		{
 			if(!(extraData.get(c1).equals(prevExtraData.get(c1))))return true;
 		}
 		return false;
 	}
 	
 	public void setPuzzleName(String puzzleName) {
 		this.puzzleName = puzzleName;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public void setCaseRuleJustification(CaseRule caseRuleJustification)
 	{
 		this.caseRuleJustification = caseRuleJustification;
 		JustificationFrame.justificationApplied(this,caseRuleJustification);
 	}
 
 	public CaseRule getCaseRuleJustification() {
 		return caseRuleJustification;
 	}
 
 	/*public void setCaseRuleJustification(String str) {
 		// TODO create a new instance of caseRuleJustification when null
 		if(this.caseRuleJustification != null)
 			this.caseRuleJustification.name = str;
 	}*/
 }
