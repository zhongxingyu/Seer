 //
 //  TreeTent.java
 //  LEGUP
 //
 //  Created by Drew Housten on Wed Feb 16 2005.
 //  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
 //
 
 package edu.rpi.phil.legup.puzzles.treetent;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Stroke;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import edu.rpi.phil.legup.BoardImage;
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.CaseRule;
 import edu.rpi.phil.legup.Contradiction;
 import edu.rpi.phil.legup.PuzzleModule;
 import edu.rpi.phil.legup.PuzzleRule;
 import edu.rpi.phil.legup.Legup;
 import edu.rpi.phil.legup.Selection;
 
 /**
  * @TODO add link rule from the tree's perspective:
  * 	 grass / tree / linked tent
  */
 public class TreeTent extends PuzzleModule
 {
 	public static int CELL_TREE = 1;
 	public static int CELL_TENT = 2;
 	public static int CELL_GRASS = 3;
 	public static int CELL_UNKNOWN = 0;
 	public int numAcceptableStates(){return 3;}
 	public boolean hasLabels(){return true;}
 	private static Stroke med = new BasicStroke(2);
 
 	public TreeTent(){
 	}
 
 	/**
 	 * Take an action when the left mouse button is pressed
 	 * @param state the current board state
 	 * @param x the x position where the pressed event occured
 	 * @param y the y position where the pressed event occured
 	 */
 	public void mousePressedEvent(BoardState state, Point where)
 	{
 		super.mousePressedEvent(state,where);
 	}
 	public void mouseDraggedEvent(BoardState state, Point where)
 	{
         state.setCellContents(where.x,where.y,CELL_GRASS);
 	}
 	public void labelPressedEvent(BoardState state, int index, int side)
 	{
 		//System.out.println(index);
 		//System.out.println(side);
 		if(side == 0 || side == 1)
 		{
 			for(int i = 0; i < state.getHeight(); i++)
 			{
 				if (state.isModifiableCell(index,i))
 				{
 					if (!state.isModifiable()) {
 						BoardState next = state.addTransitionFrom();
 						state = next;
 						state.setCellContents(index,i,CELL_GRASS);
 						Legup.getInstance().getSelections().setSelection(new Selection(next, false));	
 					} else {
 						state.setCellContents(index,i,CELL_GRASS);
 
 					}
 				}
 			}
 		}
 		if(side == 2 || side == 3)
 		{
 			for(int i = 0; i < state.getWidth(); i++)
 			{
 				if (state.isModifiableCell(i,index))
 				{
 					if (!state.isModifiable()) {
 						BoardState next = state.addTransitionFrom();
 						state = next;
 						state.setCellContents(i,index,CELL_GRASS);
 						Legup.getInstance().getSelections().setSelection(new Selection(next, false));	
 					} else {
 						state.setCellContents(i,index,CELL_GRASS);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Take an action when a left mouse drag (or click) event occurs
 	 * @param state
 	 * @param from
 	 * @param to
 	 */
 	public void mouseDraggedEvent(BoardState state, Point from, Point to)
 	{
 		if (from.equals(to))
 		{ // click
 			//Warning: Legup doesn't check whether or not a cell can be modified when a dragged event occurs
 			//Already handled by PuzzleModule.mousePressedEvent()
 			/*if(state.isModifiableCell(to.x, to.y))
 			{
 				int next = getNextCellValue(from.x,from.y,state);
 				state.setCellContents(from.x,from.y,next);
 			}*/
 		}
 		else
 		{ // drag, create link, or remove it
 			ExtraTreeTentLink e = new ExtraTreeTentLink(from,to);
 			boolean removed = false;
 			
 			if(((to.x-from.x)^2 + (to.y-from.y)^2) != 1 && ((from.x-to.x)^2 + (from.y-to.y)^2) != 1)
 				return;
 			
 			//check if exactly one point is a tree
 			if (!((state.getCellContents(from.x, from.y) == CELL_TREE && state.getCellContents(to.x, to.y) != CELL_TREE) ||
 			(state.getCellContents(from.x, from.y) != CELL_TREE && state.getCellContents(to.x, to.y) == CELL_TREE)))
 				return;
 			BoardState next = ((state.isModifiable())? state : BoardState.addTransition());
 			if(next == null)return;
 			Legup.getInstance().getSelections().setSelection(new Selection(next,false));
 			ArrayList<Object> extra = next.getExtraData();
 			for (int x = 0; x < extra.size(); ++x)
 			{
 				if (extra.get(x).equals(e))
 				{
 					extra.remove(x);
 					removed = true;
 					next.boardDataChanged();
 					break;
 				}
 			}
 			
 			if (removed == false) // if we aren't removing, we're inserting
 				next.addExtraData(e);
 		}
 	}
 
 	/**
 	 * Draw any extra data for the board
 	 * @param g the Graphics to draw with
 	 * @param extraData the extra data of the current board state we're drawing
 	 * @param bounds the bounds of the grid
 	 * @param w the width (in boxes) of the puzzle
 	 * @param h the height (in boxes) of the puzzle
 	 */
 	public void drawExtraData(Graphics gr, ArrayList<Object> extraData, Rectangle bounds, int w, int h)
 	{
 		Graphics2D g = (Graphics2D)gr;
 		Stroke preStroke = g.getStroke();
 		Color preColor = g.getColor();
 		g.setColor(Color.red);
 		g.setStroke(med);
 
 		double dx = bounds.width / (double)w;
 		double dy = bounds.height / (double)h;
 		double halfX = dx/2;
 		double halfY = dy/2;
 
 		for (int x = 0; x < extraData.size(); ++x)
 		{
 			ExtraTreeTentLink e = (ExtraTreeTentLink)extraData.get(x);
 
 			double x1 = bounds.x + e.pos1.x * dx + halfX;
 			double y1 = bounds.y + e.pos1.y * dx + halfY;
 
 			double x2 = bounds.x + e.pos2.x * dx + halfX;
 			double y2 = bounds.y + e.pos2.y * dx + halfY;
 
 			g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
 		}
 
 		g.setColor(preColor);
 		g.setStroke(preStroke);
 	}
 
 	public String getImageLocation(int cellValue){
 		if (cellValue == 0){
 			return "images/treetent/unknown.gif";
 		} else if (cellValue == 3){
 			return "images/treetent/grass.png";
 		} else if (cellValue == 1){
 			return "images/treetent/tree.png";
 		} else if (cellValue == 2){
 			return "images/treetent/tent.png";
 		} else if (cellValue >= 10 && cellValue < 30){
 			return "images/treetent/" + (cellValue-10)+".gif"; }
 		  else if (cellValue >= 30 && cellValue <= 39){
 				return "images/treetent/" + (char)('a' + (cellValue - 30)) + ".gif";
 		}
 		else{
 			return "images/treetent/unknown.gif";
 		}
 	}
 
 	public void initBoard(BoardState state)
 	{
 		int[] dir =
 		{
 			BoardState.LABEL_LEFT,
 			BoardState.LABEL_RIGHT,
 			BoardState.LABEL_TOP,
 			BoardState.LABEL_BOTTOM
 		};
 
 		int[] sizes =
 		{
 			state.getHeight(),
 			state.getHeight(),
 			state.getWidth(),
 			state.getWidth(),
 		};
 
 		int[] type =
 		{
 			1,
 			0,
 			2,
 			0
 		};
 
 		final int TYPE_ZERO = 0;
 		final int TYPE_LETTER = 1;
 		final int TYPE_NUMBER = 2;
 		final int numberOffset = 10;
 		final int letterOffset = 30;
 
 		for (int x = 0; x < dir.length; ++x)
 		{
 			for (int c = 0; c < sizes[x]; ++c)
 			{
 				if (type[x] == TYPE_ZERO)
 					state.setLabel(dir[x],c, numberOffset);
 				else if (type[x] == TYPE_LETTER)
 					state.setLabel(dir[x],c, letterOffset + c);
 				else if (type[x] == TYPE_NUMBER)
 					state.setLabel(dir[x],c, numberOffset + c + 1);
 			}
 		}
 	}
 
 	/**
 	 * Get all the images (as strings to the image path) used by this puzzle in the center part
 	 * @return an array of strings to image paths
 	 */
 	public BoardImage[] getAllCenterImages()
 	{
 		BoardImage[] s =
 		{
 				new BoardImage("images/treetent/unknown.gif",0),
 				new BoardImage("images/treetent/tree.gif",1),
 				new BoardImage("images/treetent/tent.gif",2),
 				new BoardImage("images/treetent/grass.gif",3)
 
 		};
 
 		return s;
 	}
 
 	/**
 	 * Get all the images (as strings to the image path) used by this puzzle in the border part
 	 * @return an array of strings to image paths
 	 */
 	public BoardImage[] getAllBorderImages()
 	{
 		BoardImage[] s = new BoardImage[30];
 		int count = 0;
 
 		for (int x = 0; x < 20; ++x)
 		{
 			s[count++] = new BoardImage("images/treetent/" + (x)+ ".gif",10 + x);
 		}
 
 		for (int x = 0; x < 10; ++x)
 		{
 			s[count++] = new BoardImage("images/treetent/" + (char)('a' + (x)) + ".gif",30 + x);
 		}
 
 		return s;
 	}
 
 	/**
 	 * Get the next label value if we're at this one (like the numbers around the border)
 	 * This is used when we're creating puzzles
 	 *
 	 * @param curValue the current value of the label
 	 * @return the next value of the label
 	 */
 	public int getNextLabelValue(int curValue)
 	{
 		if (curValue < 10)
 			curValue = 9; // will get incremented
 
 		return (curValue + 1 <= 39 ? curValue + 1 : 10);
 	}
 
 	public int getAbsoluteNextCellValue(int x, int y, BoardState boardState)
 	{
 		int contents = boardState.getCellContents(x,y);
 		int rv = CELL_UNKNOWN;
 
 		if (contents == CELL_UNKNOWN)
 		{
 			rv = CELL_TREE;
 		}
 		else if (contents == CELL_TREE)
 		{
 			rv = CELL_GRASS;
 		}
 		else if (contents == CELL_GRASS)
 		{
 			rv = CELL_TENT;
 		}
 
 		return rv;
 	}
 
 	public int getNextCellValue(int x, int y, BoardState boardState)
 	{
 		int contents = boardState.getCellContents(x,y);
 
 		if (contents == CELL_UNKNOWN)
 		{
 			return CELL_TENT;
 		}
 		else if (contents == CELL_TENT)
 		{
 			return CELL_GRASS;
 		}
 		else if (contents == CELL_GRASS)
 		{
 			return CELL_UNKNOWN;
 		}
 		else
 		{
 			return contents;
 		}
 	}
 	public String getStateName(int state)
 	{
 		if(state == 0)return "blank";
 		//commented out to skip over & return null, since the user shouldn't be able to make trees
 		//included for readability/potential future extension
 		//else if(state == CELL_TREE)return "tree";
 		else if(state == 1)return "tent";
 		else if(state == 2)return "grass";
 		else return null;
 	}
 	public int getStateNumber(String state)
 	{
 		if(state == "blank")return CELL_UNKNOWN;
 		else if(state == "tent")return CELL_TENT;
 		else if(state == "grass")return CELL_GRASS;
 		else return CELL_UNKNOWN;
 	}
 	public boolean checkGoal(BoardState currentBoard, BoardState goalBoard){
 		return currentBoard.compareBoard(goalBoard);
 	}
 
 	public Vector <PuzzleRule> getRules(){
 		Vector <PuzzleRule>ruleList = new Vector <PuzzleRule>();
 		//ruleList.add(new PuzzleRule());
 		ruleList.add(new RuleNewTentLink());
 		ruleList.add(new RuleAllGrass());
 		ruleList.add(new RuleNoTreesAround());
 		ruleList.add(new RuleGrassNextToTent());
 		
 		ruleList.add(new RuleOneUnknownNearTree());
 		ruleList.add(new RuleAllTents());
 		
 		//ruleList.add(new RuleNewLink());
 		//ruleList.add(new RuleNewLink());
 		return ruleList;
 	}
 
 	 /**
 	 * Gets a list of Contradictions associated with this puzzle
 	 *
 	 * @return A Vector of Contradictions
 	 */
 	public Vector <Contradiction> getContradictions()
 	{
 		Vector <Contradiction>contradictionList = new Vector <Contradiction>();
 
 		contradictionList.add(new ContradictionAdjacentTents());
 		contradictionList.add(new ContradictionMiscount());
 		contradictionList.add(new ContradictionNoTents());
 		contradictionList.add( new ContradictionTentNotNearTree() );
 
 		return contradictionList;
 	}
 
 	public Vector <CaseRule> getCaseRules()
 	{
 		Vector <CaseRule> caseRules = new Vector <CaseRule>();
 
 		caseRules.add(new CaseTentOrGrass());
 		caseRules.add( new CaseTentsInRow() );
 
 		return caseRules;
 	}
 
 	public static int translateNumTents(int cellValue){
 	return (cellValue - 10);
 	}
 
 	public boolean checkValidBoardState(BoardState boardState){
 	int height = boardState.getHeight();
 	int width = boardState.getWidth();
 
 
 	// Check all tents to see if they are adjacent to a tree
 	for (int i=0;i<height;i++){
 		for (int j=0;j<width;j++){
 		try{
 			if (boardState.getCellContents(i,j) == 2){
 
 			// Check if it is adjacent to a tree
 			if (!checkAdjacentTree(boardState, i, j)){
 				System.out.println("A tent is not adjacent to a tree");
 				return false;
 			}
 
 			// Check if it is adjacent to another tent
 			if (checkAdjacentTent(boardState, i, j)){
 				System.out.println("A tent is adjacent to another tent");
 				return false;
 			}
 			}
 		} catch (Exception e){
 		}
 		}
 	}
 
 	// Check that the number of tents in a row or column do not exceed the
 	// number allowed
 	for (int i =0;i<height;i++){
 		if (!checkRow(boardState, i)){
 		return false;
 		}
 	}
 	for (int i =0;i<width;i++){
 		if (!checkCol(boardState, i)){
 		return false;
 		}
 	}
 
 
 	return true;
 
 	}
 
 
 	private boolean checkRow(BoardState boardState, int rowNum){
 	int width = boardState.getWidth();
 	int numTents = 0;
 	try{
 		numTents = TreeTent.translateNumTents(boardState.getLabel(BoardState.LABEL_RIGHT, rowNum));
 	} catch (Exception e){
 	}
 
 	for (int i=0;i<width;i++){
 		try{
 		if (boardState.getCellContents(rowNum,i) == 2){
 			numTents--;
 		}
 		} catch (Exception e){
 		}
 	}
 
 	if (numTents < 0){
 		return false;
 	}
 	else{
 		return true;
 	}
 	}
 
 	private boolean checkCol(BoardState boardState, int colNum){
 	int height = boardState.getHeight();
 	int numTents = 0;
 	try{
 		numTents = TreeTent.translateNumTents(boardState.getLabel(BoardState.LABEL_BOTTOM, colNum));
 	} catch (Exception e){
 	}
 
 	for (int i=0;i<height;i++){
 		try{
 		if (boardState.getCellContents(i,colNum) == 2){
 			numTents--;
 		}
 		} catch (Exception e){
 		}
 	}
 
 	if (numTents < 0){
 		return false;
 	}
 	else{
 		return true;
 	}
 
 	}
 
 
 
 	private boolean checkAdjacentTent(BoardState boardState, int row, int col){
 	// Check Up
 	try{
 		if (boardState.getCellContents(row-1, col) == 2){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 	// Check Left
 	try{
 		if (boardState.getCellContents(row, col-1) == 2){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 
 	// Check Right
 	try{
 		if (boardState.getCellContents(row, col+1) == 2){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 	// Check Down
 	try{
 		if (boardState.getCellContents(row+1, col) == 2){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 	return false;
 
 	}
 
 	private boolean checkAdjacentTree(BoardState boardState, int row, int col){
 	// Check Up
 	try{
 		if (boardState.getCellContents(row-1, col) == 1){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 	// Check Left
 	try{
 		if (boardState.getCellContents(row, col-1) == 1){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 
 	// Check Right
 	try{
 		if (boardState.getCellContents(row, col+1) == 1){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 	// Check Down
 	try{
 		if (boardState.getCellContents(row+1, col) == 1){
 		return true;
 		}
 	} catch (Exception e){
 	}
 
 	return false;
 	}
 
 	public static boolean isLinked(ArrayList<Object> links, Point cell)
 	{
 		for (int a = 0; a < links.size(); ++a)
 		{
 			ExtraTreeTentLink e = (ExtraTreeTentLink)links.get(a);
 			if (e.pos1.equals(cell) || e.pos2.equals(cell))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	/* AI stuff */
 	public BoardState guess(BoardState Board) {
 		// out of forced moves, need to guess
 		Point guess = GenerateBestGuess(Board);
 		// guess, if we found one
 		if (guess.x != -1 && guess.y != -1) {
 			BoardState Parent = Board.getSingleParentState();
 			BoardState CaseTent = Board;
 			BoardState CaseGrass = Parent.addTransitionFrom();
 			CaseTent.setCellContents(guess.x, guess.y, CELL_TENT);
 			CaseGrass.setCellContents(guess.x, guess.y, CELL_GRASS);
 			Parent.setCaseSplitJustification(new CaseTentOrGrass());
 			System.out.println("Guessed at "+guess.x+","+guess.y);
 			//Legup.setSelection(CaseTent,false);
 			return CaseTent;
 		}
 		// if we didn't then the board is full, and we are finished (thus, the returned board will be the same as the one we were given
 		System.out.println("Statement: Your puzzle has been solved already. Why do you persist?");
 		return Board;
 	}
    
 
 
 
 	private Point GenerateBestGuess(BoardState Board) {
 		// this should more properly be some kind of ranking system whereby different
 		// conditions scored points and the highest scoring square was chosen.
 		// until there is more time to actually watch the AI, it scores based on closeness
 		// to a probability. In the future, it might include points for having only one extra
 		// free space or something like that.
 		int currentX=-1;
 		int currentY=-1;
 		int height = Board.getHeight();
 		int width = Board.getWidth();
 		double currentOff = Double.POSITIVE_INFINITY;
 		double BESTPROB = .25;
 		for (int r = 0; r < width; r++ ) {
 			for (int c = 0; c < height; c++) {
 				if (Board.getCellContents(r,c) == 0) {
 					// compute probability of a hit
 					double myProb = HitProb(r,c,Board);
 					//System.out.println("Square "+r+","+c+" prob: "+myProb);
 					double myOff = Math.abs(BESTPROB-myProb);
 					if (myOff < currentOff) {
 						System.out.println("Got new guess square: "+r+","+c+", off ="+myOff);
 						currentX = r;
 						currentY = c;
 						currentOff = myOff;
 					}
 				}
 			}
 		}
 		return new Point(currentX,currentY);
 	}
 	private double HitProb (int row, int column, BoardState Board) {
 		double R;
 		double C;
 		int width = Board.getHeight();
 		int height = Board.getWidth();
 		// Row
 		double neededTents = TreeTent.translateNumTents(Board.getLabel(BoardState.LABEL_RIGHT, row));
 		double currentTents = 0;
 		double OpenSpace = 0;
 		for (int i = 0; i<width; i++) {
 			int cell = Board.getCellContents(row, i);
 			if (cell == CELL_TENT) {
 				currentTents++;
 			} else if (cell == 0) {
 				OpenSpace++;
 			}
 		}
 		//	   Tents to be placed		places to put them
 		//System.out.println("Row "+row+": "+neededTents+"-"+currentTents+"/"+OpenSpace);
 		R = (neededTents-currentTents)/OpenSpace;
 		//System.out.println(R);
 		neededTents = TreeTent.translateNumTents(Board.getLabel(BoardState.LABEL_BOTTOM, column));
 		currentTents = 0;
 		OpenSpace = 0;
 		for (int i = 0; i<height; i++) {
 			int cell = Board.getCellContents(i, column);
 			if (cell == CELL_TENT) {
 				currentTents++;
 			} else if (cell == 0) {
 				OpenSpace++;
 			}
 		}
 		//System.out.println("Column "+column+": "+neededTents+"-"+currentTents+"/"+OpenSpace);
 		//	 Tents to be placed		places to put them
 		C = (neededTents-currentTents)/OpenSpace;
 		//System.out.println(C);
 		return R*C;
 	}
 
 	public String numberToLetters(int number)
 	{
 		String s = "";
 		while(number != 0)
 		{
 			int rem = (number % 26) + 64;
 			number = number / 26;
 			char c = (char)rem;
 			s = c + s;
 		}
 		return s;
 	}
 
 	public void drawLeftLabel(Graphics2D g, int val, int x, int y){
 		drawText(g,x, y, numberToLetters(y + 1));
 	}
 
 	public void drawRightLabel(Graphics2D g, int val, int x, int y){
 		drawText(g,x, y, String.valueOf(val - 10));
 	}
 
 	public void drawTopLabel(Graphics2D g, int val, int x, int y){
 		drawText(g,x, y, String.valueOf(x + 1));
 	}
 
 	public void drawBottomLabel(Graphics2D g, int val, int x, int y){
 		drawText(g,x, y, String.valueOf(val - 10));
 	}
 
 }
