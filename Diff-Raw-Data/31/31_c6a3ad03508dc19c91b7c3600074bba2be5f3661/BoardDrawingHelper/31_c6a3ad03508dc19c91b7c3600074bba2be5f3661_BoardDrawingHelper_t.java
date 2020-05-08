 package edu.rpi.phil.legup;
 import edu.rpi.phil.legup.newgui.TreePanel;
 import edu.rpi.phil.legup.newgui.CaseRuleSelectionHelper;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Rectangle;
 import java.awt.geom.Rectangle2D;
 
 
 public abstract class BoardDrawingHelper
 {
 	private static int imageWidth;
 	private static int imageHeight;
 	private static int width;
 	private static int height;
 
 	/**
 	 *	- Daniel P - February 20, 2009 -
 	 *
 	 *	If this variable is true, then the feature that "animates" choices will be activated.
 	 *	To turn off this feature, set it to false
 	 */
 	public static final boolean ANIMATE_SPLIT_CASE = true;
 
 	public static void draw(Graphics2D g, Point crshPointSelected, int crshMode)
 	{
 		// initGraphics
 		PuzzleModule pm = Legup.getInstance().getPuzzleModule();
 		if(pm == null)
 			return;
 
 		Dimension d = pm.getImageSize();
 		imageWidth = d.width;
 		imageHeight = d.height;
 
 		// TODO new stuff --kueblc
 		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
 			RenderingHints.VALUE_ANTIALIAS_ON );
 		g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
 			RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
 
 		// /initGraphics
 
 		Selection selection = Legup.getInstance().getSelections().getFirstSelection();
 		BoardState state = selection.getState();
 
 		int width  = state.getWidth();
 		int height = state.getHeight();
 
 		BoardState origState = null, newState = state;
 		if (state.getTransitionsTo().size() == 1)
 			origState = state.getTransitionsTo().get(0);
 		boolean showOrange = false;
 
 		if( TreePanel.getMouseOver() != null )
 		{
 			selection = TreePanel.getMouseOver();
 			if(selection.isState())
 			{
 				newState = selection.getState();
 				origState = newState.getSingleParentState();
 			}
 			else newState = (origState = selection.getState()).getTransitionsFrom().get(0);
 			//showOrange = true;
 		}
 
 		if (ANIMATE_SPLIT_CASE && !selection.isState())
 			newState = origState.getTransitionsFrom().get((int)((System.currentTimeMillis()/1000)%origState.getTransitionsFrom().size()));
 
 		// Draw each cell
 		for( int y = 0; y < height; ++y )
 		{
 			for( int x = 0; x < width; ++x )
 			{
 				pm.drawCell(g, x, y, newState);
 				//Determine if we need to add a different color based on
 				//transitions and changes made
 				Color filterColor = determineTransitionColor(x, y, newState, origState, showOrange);
 				//Color it, if needed
 				g.setStroke( new BasicStroke(3f) );
 				if( filterColor != null )
 				{
 					g.setColor(filterColor);
 					g.drawRect(
 						(x+1) * imageWidth + 2,
 						(y+1) * imageHeight + 2,
 						imageWidth - 4,
 						imageHeight - 4 );
 				}
 			}
 		}
 		if((crshPointSelected != null))
 		{
 			for(int x = 0;x < width;++x)
 			{
 				for(int y = 0;y < height;++y)
 				{
 					if((crshMode == CaseRuleSelectionHelper.MODE_TILE)||(crshMode == CaseRuleSelectionHelper.MODE_TILETYPE))
 					{
 						if((crshPointSelected.x == x) && (crshPointSelected.y == y))
 						{
 							g.setStroke(new BasicStroke(3f));
 							g.setColor(cyanFilter);
 							g.drawRect(
 									(x+1) * imageWidth + 2,
 									(y+1) * imageHeight + 2,
 									imageWidth - 4,
 									imageHeight - 4 );
 						}
 					}
 					else if(crshMode == CaseRuleSelectionHelper.MODE_COL_ROW)
 					{
 						if(((crshPointSelected.x == -1)&&(crshPointSelected.y == y))||((crshPointSelected.x == x)&&(crshPointSelected.y == -1)))
 						{
 							g.setStroke(new BasicStroke(3f));
 							g.setColor(cyanFilter);
 							g.drawRect(
 									(x+1) * imageWidth + 2,
 									(y+1) * imageHeight + 2,
 									imageWidth - 4,
 									imageHeight - 4 );
 						}
 					}
 				}
 			}
 		}
 		// Color hint cells
 		if( newState.getHintCells() != null ){
 			g.setColor(purpleFilter);
 			for( Point p : state.getHintCells() ){
 				g.drawRect(
 					(p.x+1) * imageWidth + 2,
 					(p.y+1) * imageHeight + 2,
 					imageWidth - 4,
 					imageHeight - 4 );
 			}
 		}
 		g.setStroke( new BasicStroke(1f) );
 
 		// drawLabels
 		int h = height;
 		int w = width;
 
 		for( int x = 0; x < w; ++x ){
 			int val = state.getLabel(BoardState.LABEL_TOP,x);
 			pm.drawTopLabel(g, val, x, -1);
 
 			val = state.getLabel(BoardState.LABEL_BOTTOM,x);
 			pm.drawBottomLabel(g, val, x, h);
 		}
 
 		for( int y = 0; y < h; ++y ){
 			int val = state.getLabel(BoardState.LABEL_LEFT,y);
 			pm.drawLeftLabel(g, val, -1, y);
 
 			val = state.getLabel(BoardState.LABEL_RIGHT,y);
 			pm.drawRightLabel(g, val, w, y);
 		}
 		// /drawLabels
 
 
 		// Draw grid on top, handled by puzzle module
 		pm.drawGrid( g, new Rectangle(
 			imageWidth, imageHeight,
 			imageWidth * width, imageHeight * height ),
 			width, height );
 		//Let the puzzle module draw its special stuff
 		try {
 			pm.drawExtraData( g, newState.getExtraData(),newState.extraDataDelta,
 				new Rectangle(
 					imageWidth, imageHeight,
 					imageWidth * width, imageHeight * height ),
 				width, height );
 		} catch( Exception e ){
 			// There is a massive problem with Board Listeners. Simulated
 			// generation should not be drawn
 			System.out.println("Another one bites the dust: " + e);
 		}
 	}
 
 	public static Color greenFilter = new Color(0,255,0);//,128);
 	public static Color redFilter = new Color(255,0,0);//,128);
 	public static Color purpleFilter = new Color(240,0,240);//,128);
 	public static Color cyanFilter = new Color(0,255,255);
 	//public static Color blueFilter = new Color(000,255,255,64);
 	//public static Color orangeFilter = new Color(255,165,0,128);
	//private static Color orangeSquare = ((!ANIMATE_SPLIT_CASE) ? new Color(225,182,100,255) : new Color(255,182,100,128));
	private static Color orangeSquare = new Color(255,192,0,255);
 	public static Color determineTransitionColor(int x, int y, BoardState newState, BoardState oldState, boolean showOrange)
 	{
 		int curVal = newState.getCellContents(x, y);
 		int prevVal = ((oldState == null) ? curVal : oldState.getCellContents(x, y));
 
 		//Draw green if a cell has been changed from CELL_UNKNOWN to a value
 		//Draw red if a cell has been changed, and not from an unknown
 		//If it is a transition then draw all cells which don't match as orange
 		if( prevVal == PuzzleModule.CELL_UNKNOWN && curVal != prevVal )
 		{
 			if( showOrange ) return orangeSquare;
 			else return greenFilter;
 		}
 		else if( prevVal != PuzzleModule.CELL_UNKNOWN && prevVal != curVal )
 			return redFilter;
 		else if( oldState == null && newState.getTransitionsTo().size() > 1 ){
 			//Transition
 			// check if it's identical in all children if not draw orange
 			for( BoardState parent : newState.getTransitionsTo() ){
 				if( parent.getCellContents(x,y) != curVal )
 					return orangeSquare;
 			}
 		}
 		return null;
 	}
 
 }
