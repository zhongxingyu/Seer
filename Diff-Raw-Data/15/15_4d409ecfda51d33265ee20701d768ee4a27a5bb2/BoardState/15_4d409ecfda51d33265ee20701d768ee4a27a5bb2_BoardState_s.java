 /**
  *  BoardState.java
  **/
 
 package edu.rpi.phil.legup;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Vector;
 import java.lang.Math;
 
 import edu.rpi.phil.legup.editor.SaveableBoardState;
 import edu.rpi.phil.legup.newgui.BoardDataChangeListener;
 import edu.rpi.phil.legup.newgui.TransitionChangeListener;
 import edu.rpi.phil.legup.newgui.TreePanel;
 import edu.rpi.phil.legup.saveable.SaveableProofState;
 import edu.rpi.phil.legup.saveable.SaveableProofTransition;
 
 
 /**
  * Stores all the information related to a board state.  Contains the cell
  * values, label values, transitions to and from the state, and any applied rules
  * associated with this state.
  *
  * @author Drew Housten & Stan Bak
  */
 public class BoardState
 {
 	private static ArrayList<BoardDataChangeListener> boardDataChangeListeners = new ArrayList<BoardDataChangeListener>();
 	private static ArrayList<TransitionChangeListener> transitionChangeListeners = new ArrayList<TransitionChangeListener>();
 
 	static final public int LABEL_TOP = 0;
 	static final public int LABEL_BOTTOM = 1;
 	static final public int LABEL_LEFT = 2;
 	static final public int LABEL_RIGHT = 3;
 
 	static final public int STATUS_UNJUSTIFIED = 0;
 	static final public int STATUS_RULE_CORRECT = 1;
 	static final public int STATUS_RULE_INCORRECT = 2;
 	static final public int STATUS_CONTRADICTION_CORRECT = 3;
 	static final public int STATUS_CONTRADICTION_INCORRECT = 4;
 
 	private int height;
 	private int width;
 	private int[][] boardCells;
 	private boolean[][] modifiableCells;
 	private boolean[][] editedCells;
 	private int[] topLabels;
 	private int[] bottomLabels;
 	private int[] leftLabels;
 	private int[] rightLabels;
 
 	private int hintsGiven = 0;
 
 	private String puzzleName = null;
 
 	// the location of this node within the proof
 	private Point offset = new Point(0,0);
 	private Point location = new Point(0,0);
 
 	// parents
 	private Vector<BoardState> transitionsTo = new Vector<BoardState>();
 
 	// children
 	private Vector<BoardState> transitionsFrom = new Vector<BoardState>();
 
 	// a PuzzleRule or Contradiction
 	private Object justification = null;
 
 	// the justification case rule for this parent state
 	private CaseRule caseRuleJustification = null;
 
 	// puzzle specific extra data
 	// for example, in tree tent, the tree which each tent belongs to
 	protected ArrayList <Object> extraData = new ArrayList <Object>();
 
 	private boolean collapsed = false;
 
 	private ArrayList<Point> hintCells = new ArrayList<Point>();
 
 	private boolean isSolution = false;
 
 	private ArrayList<BoardState> mergeChildren = new ArrayList<BoardState>();
 	private BoardState mergeOverlord;
 
 	private boolean virtualBoard = false;
 	
 	private boolean modifiableState = false;
 
 	/**
 	 * Constructor
 	 *
 	 * @param height Height of the board state
 	 * @param width Width of the board state
 	 * @throws Exception if the extents are invalid
 	 */
 	public BoardState(int height, int width)
 	{
 		// Set the height and width
 		this.height = height;
 		this.width = width;
 
 		// Allocate the arrays
 		boardCells = new int[height][width];
 		topLabels = new int[width];
 		bottomLabels = new int[width];
 		leftLabels = new int[height];
 		rightLabels = new int[height];
 		modifiableCells = new boolean[height][width];
 		editedCells = new boolean[height][width];
 
 		// Initialize the arrays
 		for (int i=0;i<height;i++)
 		{
 			leftLabels[i]=0;
 			rightLabels[i]=0;
 
 			for (int j=0;j<width;j++)
 			{
 				modifiableCells[i][j] = true;
 				boardCells[i][j] = 0;
 				editedCells[i][j] = false;
 
 				if (i==0)
 				{
 					topLabels[j] = 0;
 					bottomLabels[j] = 0;
 				}
 			}
 		}
 
 		if(Legup.getInstance().getPuzzleModule() != null)
 			this.puzzleName = Legup.getInstance().getPuzzleModule().name;
 	}
 
 	/**
 	 *	Constructor
 	 *
 	 *	@param state The BoardState to be replicated.  Connections will not be copied.
 	 */
 	public BoardState(BoardState copy)
 	{
 		this(copy.width, copy.height);
 		virtualBoard = copy.virtualBoard;
 
 		// Allocate the arrays
 		for (int x = 0; x < width; x++) for (int y = 0; y < height; y++)
 		{
 			boardCells[y][x] = copy.boardCells[y][x];
 			modifiableCells[y][x] = copy.modifiableCells[y][x];
 			editedCells[y][x] = copy.editedCells[y][x];
 		}
 		for (int x = 0; x < width; x++) { topLabels[x] = copy.topLabels[x]; bottomLabels[x] = copy.bottomLabels[x]; }
 		for (int y = 0; y < height; y++) { leftLabels[y] = copy.leftLabels[y]; rightLabels[y] = copy.rightLabels[y]; }
 		extraData = new ArrayList<Object>(copy.extraData);
 	}
 
 	public void setVirtual(boolean virtual)
 	{
 		virtualBoard = virtual;
 	}
 	
 	public boolean isModifiable() {
 		return modifiableState;
 	}
 	
 	public void setModifiableState(boolean mod) {
 		modifiableState = mod;
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
 	 * @param x the x coordinate of the collapsed but selectable (grand)parent
 	 * @param y the y coordinate of the collapsed but selectable (grand)parent
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
 		String rv = "No rule selected!";
 
 		if (caseRuleJustification != null)
 		{
 			rv = caseRuleJustification.checkCaseRuleRaw(this);
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Is the user allowed to modify this cell? This is used to prevent the user from
 	 * modifying initial data for sudoku, for example
 	 * @param x the x cell 0 = leftmost
 	 * @param y the y cell 0 = topmost
 	 * @return true iff the user is allowed to modify the cell during the proof
 	 */
 	public boolean isModifiableCell(int x, int y)
 	{
 		return modifiableCells[y][x];
 		//return Math.abs(boardCells[y][x]) == boardCells[y][x];
 	}
 
 	/**
 	 * Get the case split justification at this cell (null if not defined)
 	 * @return the CaseRule used to justify this state's children
 	 */
 	public CaseRule getCaseSplitJustification()
 	{
 		return caseRuleJustification;
 	}
 
 	/**
 	 * Set the justification in the parent state of the split
 	 * @param jusification
 	 */
 	public void setCaseSplitJustification(CaseRule jusification)
 	{
 		delayStatus = STATUS_UNJUSTIFIED;
 		caseRuleJustification = jusification;
 		modifyStatus();
 	}
 
 	/**
 	 * Get an array of all the defined extra data for this puzzle
 	 * @return
 	 */
 	public ArrayList<Object> getExtraData()
 	{
 		return extraData;
 	}
 
 	/**
 	 * Add an object to this state's extra data
 	 * @param o the extra data we're adding
 	 */
 	public void addExtraData(Object o)
 	{
 		extraData.add(o);
 
 		boardDataChanged();
 	}
 
 	/**
 	 * the board data was changed, tell the listeners
 	 */
 	public void boardDataChanged()
 	{
 		delayStatus = STATUS_UNJUSTIFIED;
 		modifyStatus();
 		for (BoardState B : transitionsTo) B.modifyStatus();
 		for (BoardState B : transitionsFrom) B.modifyStatus();
 		for (int a = 0; a < boardDataChangeListeners.size(); ++a)
 		{
 			BoardDataChangeListener c = boardDataChangeListeners.get(a);
 
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
 		return boardCells[y][x];
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
 
 		if (value == boardCells[y][x] || justification instanceof Contradiction)
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
 		if (parent != null) {
 			if (boardCells[y][x] != parent.getCellContents(x, y)) {
 				editedCells[y][x] = true;
 			} else {
 				editedCells[y][x] = false;
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
 	public void propagateChange(int x, int y, int value) {
 		boardCells[y][x] = value;
 		setModifiableCell(x, y, value == PuzzleModule.CELL_UNKNOWN);
 		editedCells[y][x] = false;
 		
 		Legup.getInstance().getPuzzleModule().updateState(this);
 		
 		for (BoardState child : transitionsFrom) {
 			child.propagateChange(x, y, value);
 		}
 		
 		if (!virtualBoard) boardDataChanged();
 	}
 
 	 /**
 	  * Used for puzzle generation.
 	  */
 	 public void setModifiableCell(int x, int y, boolean value)
 	 {
 		modifiableCells[y][x] = value;
 	 }
 	 
 	/**
 	 * Moves editedCells into modifiableCells. Called during endTranstion().
 	 */
 	public void editedToModifiable() {
 		for (int i = 0; i < height; i++) {
 			for (int j = 0; j < width; j++) {
 				if (editedCells[i][j]) {
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
 		boardDataChangeListeners.add(listener);
 	}
 
 	/**
 	 * Add a transition change listener to listen to any transition changes that occur
 	 * @param l the listener we're adding
 	 */
 	public static void addTransitionChangeListener(TransitionChangeListener l)
 	{
 		transitionChangeListeners.add(l);
 	}
 
 	/**
 	 * Gets the height of the Board
 	 *
 	 * @return The height of the board
 	 */
 	public int getHeight(){
 		return height;
 	}
 
 	/**
 	 * Gets the width of the Board
 	 *
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
 		if (pos<0 || pos >= width){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return topLabels[pos];
 	case 1:
 		if (pos<0 || pos >= width){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return bottomLabels[pos];
 	case 2:
 		if (pos<0 || pos >= height){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return leftLabels[pos];
 	case 3:
 		if (pos<0 || pos >= height){
 		throw new IndexOutOfBoundsException("Invalid index");
 		}
 		return rightLabels[pos];
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
 	public void setLabel(int labelLocation, int pos, int value) {
 	switch(labelLocation){
 	case 0:
 
 		topLabels[pos] = value;
 		return;
 	case 1:
 
 		bottomLabels[pos] = value;
 		return;
 	case 2:
 
 		leftLabels[pos] = value;
 		return;
 	case 3:
 
 		rightLabels[pos] = value;
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
 
 	public void evalDelayStatus()
 	{
 		delayStatus = getStatus();
 		for (BoardState B : transitionsFrom) B.evalDelayStatus();
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
 	 *
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
 		for (int x = 0; x < transitionChangeListeners.size(); ++x) transitionChangeListeners.get(x).transitionChanged();
 	}
 
 	/**
 	 * Adds a transition from this board state.
 	 *
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
 	public static void merge(ArrayList <BoardState> states)
 	{
 		BoardState child = states.get(0).copy();
 
 		for (int c = 1; c < states.size(); ++c)
 		{
 			BoardState parent = states.get(c);
 
 			for (int y = 0; y < child.height; ++y)
 			{
 				for (int x = 0; x < child.width; ++x)
 				{
 					int childCell = child.getCellContents(x,y);
 					int parentCell = parent.getCellContents(x,y);
 
 					// clear all differences
 					if (childCell != PuzzleModule.CELL_UNKNOWN && childCell != parentCell)
 					{
 						child.setCellContents(x, y, PuzzleModule.CELL_UNKNOWN);
 					}
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
 
 		Legup.getInstance().getSelections().setSelection(new Selection(child, false));
 
 		_transitionsChanged();
 	}
 
 	// The methods contained from this comment....
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
 
 	private void evalMerge(int change)
 	{
 		int mergeTot = 0, directTot = numDirectBranches(); // GAH!?!
 		for (BoardState B : mergeChildren) mergeTot += B.numBranches();
 
 		if (change == 1 && mergeTot == directTot+1)
 			expandXSpace(null);
 		else if (change == -1 && mergeTot == directTot)
 			contractXSpace(null);
 		else
 			recalculateLocation();
 	}
 
 	private int numDirectBranches()
 	{
 		ArrayList<BoardState> valid = new ArrayList<BoardState>();
 		for (BoardState B : transitionsFrom) if (B.transitionsTo.size() == 1) valid.add(B);
 
 		if (valid.size() == 0) return 1;
 		int tot = 0; for (BoardState B : valid) tot += B.numDirectBranches(); return tot;
 	}
 
 	private void evalMergeY()
 	{
 		if (mergeChildren.size() > 0)
 		{
 			int depth = getDirectDepth();
 			int mergeTot = 0; for (BoardState B : mergeChildren) mergeTot += B.numBranches();
 
 			int place = -(mergeTot-1)*(int)(1.5*TreePanel.NODE_RADIUS);
 			for (BoardState B : mergeChildren)
 			{
 				B.offset.y = (1+depth)*3*TreePanel.NODE_RADIUS;
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
 
 	private int getDepth()
 	{
 		return getDirectDepth()+getMergeDepth();
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
 
 	/**
 	 * Adds a transition from this board state.
 	 * @param child the new child state
 	 * @param rule the rule we applied
 	 */
 	public void addTransitionFrom(BoardState b, PuzzleRule rule)
 	{
 		transitionsFrom.add(b);
 		 b.transitionsTo.add(this);
 		 b.justification = rule;
 
 		b.offset.x = 0;
 		b.offset.y = TreePanel.NODE_RADIUS * 4;
 
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
 			this.caseRuleJustification = Legup.getInstance().getPuzzleModule().getCaseRuleByName(justification);
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
 
 
 	/**
 	 * Makes a copy of the current board state.
 	 *
 	 * @return New BoardState that is a copy of this board state
 	 */
 	public BoardState copy(){
 	BoardState newBoardState = null;
 	try{
 		newBoardState = new BoardState(this.height,this.width);
 		newBoardState.virtualBoard = virtualBoard;
 		newBoardState.modifiableState = modifiableState;
 	} catch (Exception e){
 		return newBoardState;
 	}
 
 	// Initialize the arrays
 	for (int i=0;i<height;i++)
 	{
 		newBoardState.leftLabels[i]=leftLabels[i];
 		newBoardState.rightLabels[i]=rightLabels[i];
 
 		for (int j=0;j<width;j++)
 		{
 			newBoardState.boardCells[i][j] = boardCells[i][j];
 			newBoardState.modifiableCells[i][j] = modifiableCells[i][j];
 			newBoardState.editedCells[i][j] = editedCells[i][j];
 
 			if (i==0)
 			{
 				newBoardState.topLabels[j] = topLabels[j];
 				newBoardState.bottomLabels[j] = bottomLabels[j];
 			}
 		}
 	}
 
 	// copy the extra data
 	newBoardState.setExtraData(copyExtraData());
 
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
 	if (this.height != compareBoard.height ||
 		this.width != compareBoard.width){
 		return false;
 	}
 
 	for (int i=0;i<height;i++){
 		for (int j=0;j<width;j++){
 		if (this.boardCells[i][j] != compareBoard.boardCells[i][j]){
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
 	public Object getJustification()
 	{
 		return justification;
 	}
 
 	/**
 	 * Sets the Justification for this board state
 	 */
 	public void setJustification(Object j)
 	{
 		// don't change justification to a contradiction if there are changes
 		if (j instanceof Contradiction) {
 			for (int y = 0; y < height; y++) {
 				for (int x = 0; x < width; x++) {
 					if (editedCells[y][x])
 						return;
 				}
 			}
 		}
 			
 		justification = j;
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
 		if (status == -1)
 		{
 			leadsToContradiction();
 			leadsToSolution();
 
 			status = STATUS_UNJUSTIFIED;
 
 			Object o = getJustification();
 
 			if (o != null)
 			{
 				if (o instanceof Contradiction)
 				{
 					Contradiction c = (Contradiction)o;
 
 					if (c.checkContradiction(this) == null)
 						status = STATUS_CONTRADICTION_CORRECT;
 					else
 						status = STATUS_CONTRADICTION_INCORRECT;
 				}
 				else if (o instanceof PuzzleRule)
 				{
 					PuzzleRule pz = (PuzzleRule)o;
 
 					if (pz.checkRuleRaw(this) == null)
 						status = STATUS_RULE_CORRECT;
 					else
 						status = STATUS_RULE_INCORRECT;
 				}
 			}
 			else
 			{
 				BoardState parent = getSingleParentState();
 
 				if (parent != null)
 				{
 					if (parent.getTransitionsFrom().size() > 1 &&
 							parent.getCaseSplitJustification() != null)
 					{
 						if (parent.isJustifiedCaseSplit() == null)
 							status = STATUS_RULE_CORRECT;
 						else
 							status = STATUS_RULE_INCORRECT;
 					}
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
 
 		s.height = height;
 		s.width = width;
 		s.boardCells = boardCells;
 		s.bottomLabels = bottomLabels;
 		s.leftLabels = leftLabels;
 		s.rightLabels = rightLabels;
 		s.topLabels = topLabels;
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
 
 			rv.puzzleName = s.puzzleMod;
 
 			rv.boardCells = s.boardCells;
 			rv.bottomLabels = s.bottomLabels;
 			rv.leftLabels = s.leftLabels;
 			rv.rightLabels = s.rightLabels;
 			rv.topLabels = s.topLabels;
 			rv.extraData = s.extraData;
 			rv.location = s.location;
 
 			// set modification properties such that any initial data is unmodifiable
 			for (int y = 0; y < rv.height; ++y) for (int x = 0; x < rv.width; ++x)
 			{
 				if (rv.boardCells[y][x] != PuzzleModule.CELL_UNKNOWN)
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
 		if (status != -1)
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
 
 		return (leadContradiction = rv);
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
 		for(BoardState child : oldParent.getTransitionsFrom())
 		{
 			child.transitionsTo.clear();
 			newParent.transitionsFrom.add(child);
 			child.transitionsTo.add(newParent);
			newParent.expandXSpace(child);
 		}
 		
 		while (oldParent.getTransitionsFrom().size() > 0)
			oldParent.removeLeaf(oldParent.getTransitionsFrom().get(0));
 		
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
 
 	public Point getOffset()
 	{
 		return offset;
 	}
 
 	public void setOffset(Point offset)
 	{
 		this.offset = offset;
 		this.recalculateLocation();
 	}
 
 	public void recalculateLocation()
 	{
 		if (this.getTransitionsTo().size() == 1)
 		{
 			BoardState parentState = this.getSingleParentState();
 			Point p = new Point(parentState.getLocation().y, parentState.getLocation().x);
 			this.location.x = p.x + offset.x;
 
 			//If this and its parent are collapsed, their locations are ontop of each other
 			//Places this over where the previous actual state is if it functions as a transition (isModifiable)
 			if ((this.isCollapsed() && this.getSingleParentState().isCollapsed()) || !this.isModifiable())
 				this.location.y = p.y;
 			else
 				this.location.y = p.y + offset.y;
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
 
 	public BoardState getFinalState()
 	{
 		BoardState ret = null;
 		BoardState test;
 		if(this.leadsToContradiction())
 			return null;
 		if(this.transitionsFrom.size() == 0)
 			return this;
 		else
 		{
 			for(BoardState s : this.transitionsFrom)
 			{
 				test = s.getFinalState();
 				if(test != null && ret != null)
 				{
 					//Multiple not closed
 					ret = null;
 					break;
 				}
 				else
 				{
 					ret = test;
 				}
 			}
 		}
 		return ret;
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
 		bs.boardCells = ps.boardCells;
 		//bs.modifiableCells = ps.modifiableCells;
 		bs.topLabels = ps.topLabels;
 		bs.bottomLabels = ps.bottomLabels;
 		bs.leftLabels = ps.leftLabels;
 		bs.rightLabels = ps.rightLabels;
 		bs.puzzleName = ps.puzzleName;
 		//bs.collapsed = ps.collapsed;
 		bs.collapsed = false;
 		//bs.hintCells = ps.hintCells;
 		//bs.offset = ps.offset;
 		bs.offset = new Point(0,0);
 		//bs.setExtraData(ps.extraData);
 
 		Legup.getInstance().loadPuzzleModule(bs.puzzleName);
 		//Legup.getInstance().loadPuzzleModule("Battleships");
 
 		return bs;
 	}
 
 	private SaveableProofState toSaveableProofState()
 	{
 		SaveableProofState s = new SaveableProofState();
 
 		s.height = this.height;
 		s.width = this.width;
 		s.boardCells = this.boardCells;
 		s.modifiableCells = this.modifiableCells;
 		s.topLabels = this.topLabels;
 		s.bottomLabels = this.bottomLabels;
 		s.leftLabels = this.leftLabels;
 		s.rightLabels = this.rightLabels;
 		s.puzzleName = this.puzzleName;
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
 				transitions.add(new SaveableProofTransition(this.id, b.id, this.caseRuleJustification.getName(), true));
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
 }
