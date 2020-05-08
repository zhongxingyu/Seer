 /**
  * 
  */
 package gui;
 
 import gui.GuiElementField.eStates;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import sun.applet.Main;
 
 import logic.Board;
 import logic.CheckBoard;
 import logic.CheckEditorBoardDifficulty;
 import logic.Field;
 
 
 /**
  * 
  * @author Andreas
  * @version 0.1
  *
  */
 
 public abstract class GuiElementBoard extends JScrollPane
 {
 	private boolean hasChanged = false;
	
	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}
 	/**
 	 * 
 	 */
 	private int _cols = 0;
 	
 	/**
 	 * 
 	 */
 	private int _rows = 0;
 	
 	/**
 	 * 
 	 */
 	private int _elementWidth = 40;
 	
 	/**
 	 * 
 	 */
 	private int _elementHeight = 40;
 	
 	/**
 	 * 
 	 */
 	private GuiElementStarCounter[] _rowStarElements = null;
 	
 	/**
 	 * 
 	 */
 	private GuiElementStarCounter[] _columnStarElements = null;
 	
 	/**
 	 * 
 	 */
 	protected Board _oLogicBoard = null;
 	
 	/**
 	 * 
 	 */
 	protected JPanel _container = null;
 	
 	/**
 	 * 
 	 */
 	protected GuiElementField[][] _fields = new GuiElementField[0][0];
 	
 	
 	/**
 	 * 
 	 * @param int rows
 	 * @param int cols
 	 */
 	
 	public GuiElementBoard(int rows, int cols)
 	{
 		this._cols = cols;
 		this._rows = rows;
 		
 		_container = new JPanel();
 		_container.setLayout(null);
 		_container.setLocation(0, 0);
 		
 		Color oTransCol = new Color(0, 0, 0, 0);
 		_container.setBackground(oTransCol);
 		setBackground(oTransCol);
 		setBorder(null);
 		
 		_rowStarElements = new GuiElementStarCounter[_rows];
 	    _columnStarElements = new GuiElementStarCounter[_cols];
 	    
 	    for (int iY = 0; iY < _rows; iY++)
 		{
 	    	GuiElementStarCounter oGuiStarCounter = new GuiElementStarCounter(0);
 	    	_container.add(oGuiStarCounter);
 	    	_rowStarElements[iY] = oGuiStarCounter;
 		}
 	    
 	    for (int iX = 0; iX < _cols; iX++)
 		{
 	    	GuiElementStarCounter oGuiStarCounter = new GuiElementStarCounter(0);
 	    	_columnStarElements[iX] = oGuiStarCounter;
 	    	_container.add(oGuiStarCounter);
 		}
 	}
 	
 	@Override
 	public void setBounds(int x, int y, int width, int height) 
 	{
 		// TODO Auto-generated method stub
 		super.setBounds(x, y, width, height);
 		initControls();
 	}
 	
 	
 	/**
 	 * 
 	 * 
 	 */
 	
 	public void initControls()
 	{
 		_elementHeight = (getHeight()) / (_rows+1);
 		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
 		
 		_elementWidth = (getWidth()) / (_cols+1);
 		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	    
 	    _container.setSize(_elementWidth * (_cols+1), _elementHeight * (_rows+1));
 	    _container.setPreferredSize(_container.getSize());
 	    
 	    for (int iY = 0; iY < _rows; iY++)
 		{
 	    	GuiElementStarCounter oGuiStarCounter = _rowStarElements[iY];
 	    	oGuiStarCounter.setLocation(0, (iY+1) *_elementHeight);
 	    	oGuiStarCounter.setSize(_elementWidth, _elementHeight);
 		}
 	    
 	    for (int iX = 0; iX < _cols; iX++)
 		{
 	    	GuiElementStarCounter oGuiStarCounter = _columnStarElements[iX];
 	    	oGuiStarCounter.setLocation((iX+1) *_elementWidth, 0);
 	    	oGuiStarCounter.setSize(_elementWidth, _elementHeight);
 		}
 	    
 	    GuiElementField.clearImageCache();
 	    
 	    // draw all buttons 
 	    for (int iY = 0; iY < _rows; iY++)
 		{
 	    	for (int iX = 0; iX < _cols; iX++)
 			{
 	    		GuiElementField oGuiElementField = (GuiElementField)_fields[iY][iX];
 	    		oGuiElementField.setLocation((iX+1) *_elementWidth, (iY+1) *_elementHeight);
 	    		oGuiElementField.setSize(_elementWidth, _elementHeight);
 	    		oGuiElementField.setState(oGuiElementField.getState());
 	    		_container.add(oGuiElementField);
 			}
 		}
 	    
 		setViewportView(_container);
 	}
 	
 	/**
 	 * 
 	 * @author Andreas
 	 *
 	 */
 	protected class onClick implements MouseListener 
 	{
 		
 		public void mouseClicked(MouseEvent e) 
 		{
 			
 			if (e.getButton() == MouseEvent.BUTTON1)
 			{
 				hasChanged = true;
 				GuiElementField oGuiElementField = (GuiElementField)e.getSource();
 				if (PlayFrame._oPlayFrame._oBoard instanceof GuiElementGameBoard)
 				{
 					if (eStates.BLANK == oGuiElementField.getState() ||
 						eStates.CROSS == oGuiElementField.getState() ||
 						eStates.STAR == oGuiElementField.getState() ||
 						eStates.QUESTION == oGuiElementField.getState())
 					{
 						oGuiElementField.setNextLeftState();
 					}
 				}
 				else
 				{
 					oGuiElementField.setNextLeftState();
 				}
 				drawHelpingLines(oGuiElementField.getXPos(), oGuiElementField.getYPos());
 			}
 			else if (e.getButton() == MouseEvent.BUTTON3)
 			{
 				hasChanged = true;
 				GuiElementField oGuiElementField = (GuiElementField)e.getSource();
 				if (PlayFrame._oPlayFrame._oBoard instanceof GuiElementGameBoard)
 				{
 					if (eStates.BLANK == oGuiElementField.getState() ||
 						eStates.CROSS == oGuiElementField.getState() ||
 						eStates.STAR == oGuiElementField.getState() ||
 						eStates.QUESTION == oGuiElementField.getState())
 					{
 						oGuiElementField.setNextRightState();
 					}
 				}
 				else
 				{
 					oGuiElementField.setNextRightState();
 				}
 				drawHelpingLines(oGuiElementField.getXPos(), oGuiElementField.getYPos());
 			}
 			
 			PlayFrame._oPlayFrame.refreshWindow();
 		}
 
 		public void mouseEntered(MouseEvent e) 
 		{
 			// TODO Auto-generated method stub
 			GuiElementField oGuiElementField = (GuiElementField)e.getSource();
 //			oGuiElementField.requestFocusInWindow();
 			drawHelpingLines(oGuiElementField.getXPos(), oGuiElementField.getYPos());
 			PlayFrame._oPlayFrame.refreshWindow();
 		}
 
 		public void mouseExited(MouseEvent e) {
 			// TODO Auto-generated method stub
 			PlayFrame._oPlayFrame.refreshWindow();
 		}
 
 		public void mousePressed(MouseEvent e) {
 			// TODO Auto-generated method stub
 			PlayFrame._oPlayFrame.refreshWindow();
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			// TODO Auto-generated method stub
 			PlayFrame._oPlayFrame.refreshWindow();
 		}
 	 }
 	
 	/**
 	 * 
 	 * @param int posY
 	 * @param int posX
 	 * @return GuiElementField
 	 * 
 	 */
 	public GuiElementField getField(int posY, int posX)
 	{
 		return this._fields[posY][posX];
 	}
 	
 	public void save(String filename)
 	{
 		_oLogicBoard.save(filename);
 	}
 	
 	
 	
 	public void drawHelpingLines(int curXPos, int curYPos)
 	{
 		int adder = 1;
 		// first set all to normal fields
 		for (int iY = 0; iY < _rows; iY++)
 		{
 	    	for (int iX = 0; iX < _cols; iX++)
 			{
 	    		GuiElementField oGuiElementField = (GuiElementField)_fields[iY][iX];
 	    		oGuiElementField.setToNormalField();
 			}
 		}
 		
 		GuiElementField oGuiElementField = (GuiElementField)_fields[curYPos][curXPos];
 		oGuiElementField.setToHighlightField();
 		
 		switch (oGuiElementField.getState()) 
 		{
 			case ARROW_E:
 				for (int iX = curXPos; iX < _cols; iX++)
 				{
 		    		GuiElementField oTmpGuiElementField = (GuiElementField)_fields[curYPos][iX];
 		    		oTmpGuiElementField.setToHighlightField();
 				}
 				break;
 				
 			case ARROW_W:
 				for (int iX = 0; iX < curXPos; iX++)
 				{
 		    		GuiElementField oTmpGuiElementField = (GuiElementField)_fields[curYPos][iX];
 		    		oTmpGuiElementField.setToHighlightField();
 				}
 				break;
 				
 			case ARROW_S:
 				for (int iY = curYPos; iY < _rows; iY++)
 				{
 		    		GuiElementField oTmpGuiElementField = (GuiElementField)_fields[iY][curXPos];
 		    		oTmpGuiElementField.setToHighlightField();
 				}
 				break;
 				
 			case ARROW_N:
 				for (int iY = 0; iY < curYPos; iY++)
 				{
 		    		GuiElementField oTmpGuiElementField = (GuiElementField)_fields[iY][curXPos];
 		    		oTmpGuiElementField.setToHighlightField();
 				}
 				break;
 				
 			case ARROW_NE:
 				adder = 1;
 				while (((curYPos - adder) >= 0) && ((curXPos + adder) < _cols))
 				{
 					GuiElementField oTmpGuiElementField = (GuiElementField)_fields[(curYPos - adder)][(curXPos + adder)];
 		    		oTmpGuiElementField.setToHighlightField();
 		    		adder++;
 				}
 				break;
 				
 			case ARROW_SE:
 				adder = 1;
 				while (((curYPos + adder) < _rows) && ((curXPos + adder) < _cols))
 				{
 					GuiElementField oTmpGuiElementField = (GuiElementField)_fields[(curYPos + adder)][(curXPos + adder)];
 		    		oTmpGuiElementField.setToHighlightField();
 		    		adder++;
 				}
 				break;
 				
 			case ARROW_NW:
 				adder = 1;
 				while (((curYPos - adder) >= 0) && ((curXPos - adder) >= 0))
 				{
 					GuiElementField oTmpGuiElementField = (GuiElementField)_fields[(curYPos - adder)][(curXPos - adder)];
 		    		oTmpGuiElementField.setToHighlightField();
 		    		adder++;
 				}
 				break;
 				
 			case ARROW_SW:
 				adder = 1;
 				while (((curYPos + adder) < _rows) && ((curXPos - adder) >= 0))
 				{
 					GuiElementField oTmpGuiElementField = (GuiElementField)_fields[(curYPos + adder)][(curXPos - adder)];
 		    		oTmpGuiElementField.setToHighlightField();
 		    		adder++;
 				}
 				break;
 	
 			default:
 				break;
 		}
 	}
 	
 	
 	/**
 	 * 
 	 */
 	public void calculateStars()
 	{
 		int count = 0;
 		for (int iY = 0; iY < _rows; iY++)
 		{
 	    	count = _oLogicBoard.getCountStarsForRow(iY);
 	    	_rowStarElements[iY].setStars(count);
 	    	count = 0;
 		}
 		
 		for (int iX = 0; iX < _cols; iX++)
 		{
 			count = _oLogicBoard.getCountStarsForColumn(iX);
 			_columnStarElements[iX].setStars(count);
 	    	count = 0;
 		}
 		
 		PlayFrame.refreshWindow();
 	}
 	
 	public boolean check()
 	{
 		return CheckBoard.getInstance(_oLogicBoard).check();
 	}
 	
 	public String getDifficulty()
 	{
 		CheckEditorBoardDifficulty oDiff = new CheckEditorBoardDifficulty(_oLogicBoard);
 		String result = oDiff.checkDifficulty();
 		return result;
 	}
 	
 	public Map<Integer, HashMap<Integer, Integer>> getUnsolvableStars()
 	{
 		CheckEditorBoardDifficulty oDiff = new CheckEditorBoardDifficulty(_oLogicBoard);
 		oDiff.checkDifficulty();
 		return oDiff.getUnsolvableStars();
 	}
 
 	public Board getLogicBoard()
 	{
 		return _oLogicBoard;
 	}
 	
 	public int getCols()
 	{
 		return this._cols;
 	}
 	
 	public int getRows()
 	{
 		return this._rows;
 	}
 	public boolean isHasChanged() {
 		return hasChanged;
 	}
 }
