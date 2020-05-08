 /**
  * 
  */
 package logic;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 
 import gui.GuiElementField;
 import gui.GuiElementField.eStates;
 
 /**
  * Die CheckEditorBoardDifficulty Klasse dient dazu, um die Schwirigkeit des Boards auszugeben
  * Durch set und get Methoden knnen die Daten des Board gesetzt und ausgegeben werden.
  * 
  * @author Fabian, Mats, Eren, Daniel, Andreas, Anatoli
  * @version 0.1
  * 
  */
 public class CheckEditorBoardDifficulty 
 {
 	/**
 	 * Dekleration der Variabeln fr die Klasse
 	 */
 	final static public String BOARD_DIFFICULTY_EASY = "einfach";	
 	final static public String BOARD_DIFFICULTY_MEDIUM = "mittel";	
 	final static public String BOARD_DIFFICULTY_HARD = "schwer";	
 	final static public String BOARD_DIFFICULTY_NOT_SOLVABLE = "unlsbar";
 	Board _oBoard = null;
 	eStatesDiff[][] _tmpDiff = null;
 	protected Map<Integer, HashMap<Integer, Integer>> _unsolvableStars = null;
 	
 	
 	/**
 	 * Ein Enum wird hier deklariert
 	 */
 	public static enum eStatesDiff
 	{
 	    BLOCKED,
 	    ISSTAR,
 	}
 			
 
 	/**
 	 * Konstruktor der Klasse CheckEditorBoardDifficulty
 	 * Das Editor Board wird nach der Schwirigkeit geprft
 	 * @param Board - Hier wird das Editor Board angegeben
 	 */
 	public CheckEditorBoardDifficulty(Board oBoard)
 	{
 		_oBoard = oBoard;
 	}
 	
 	/**
 	 * Konstruktor der Klasse CheckEditorBoardDifficulty
 	 * Das Editor Board wird nach der Schwirigkeit geprft
 	 * @return BOARD_DIFFICULTY_EASY = "einfach" - Hier wird angegeben, dass das Board die Schwirigkeit leicht hat	
 	 * @return BOARD_DIFFICULTY_MEDIUM = Hier wird angegeben, dass das Board die Schwirigkeit mittel hat	
 	 * @return BOARD_DIFFICULTY_HARD = Hier wird angegeben, dass das Board die Schwirigkeit schwer hat	
 	 * @return BOARD_DIFFICULTY_NOT_SOLVABLE = Hier wird angegeben, dass das Board die Schwirigkeit unlsbar hat	
 	 */
 	public String checkDifficulty()
 	{
 		/**
 		 * Dekleration der Variabeln fr die Funktion checkDifficulty
 		 */
 		_tmpDiff = new eStatesDiff[_oBoard.getHeight()][_oBoard.getWidth()];
 		
 		boolean isSolvable = false;
 		boolean doMediumSearch = false;
 		boolean doHardSearch = false;
 		boolean isSolved = false;
 		boolean hasChanged = true;
 		int tmpStarCount = 0;
 		int tmpEmptyFields = 0;
 		int counter = 0;
 		boolean foundEmptySpot = false;
 		int foundSpotX = -1;
 		int foundSpotY = -1;
 		int adder = 0;
 		
 		// go through logicuntil game is solved or stop when no changes were made in last run
 		while (!isSolved &&
 				hasChanged)
 		{
 			hasChanged = false;
 			for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 			{
 				int rowStars = getCalculatedRowStars(iY, doMediumSearch);
 				
 				for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 				{
 					int columnStars = getCalculatedColumnStars(iX, doMediumSearch);
 					Field oField = _oBoard.getField(iY, iX);
 					
 					if (0 == columnStars ||
 						0 == rowStars ||
 						isArrow(oField.getState()))
 					{
 						
 						boolean tmpChanged = updateTmpFields(iY, iX, eStatesDiff.BLOCKED);
 						if (!hasChanged)
 						{
 							hasChanged = tmpChanged;
 						}
 					}
 					
 					if (!isArrowPointingAtField(iY, iX))
 					{
 						boolean tmpChanged = updateTmpFields(iY, iX, eStatesDiff.BLOCKED);
 						if (!hasChanged)
 						{
 							hasChanged = tmpChanged;
 						}
 					}
 					
 					
 				}
 				
 				tmpEmptyFields = 0;
 				for (int iX2 = 0; iX2 < _oBoard.getWidth(); iX2++) 
 				{
 					if (_tmpDiff[iY][iX2] != eStatesDiff.BLOCKED && _tmpDiff[iY][iX2] != eStatesDiff.ISSTAR)
 					{
 						tmpEmptyFields++;
 					}
 				}
 				
 				if (tmpEmptyFields == rowStars)
 				{
 					for (int iX2 = 0; iX2 < _oBoard.getWidth(); iX2++) 
 					{
 						if (_tmpDiff[iY][iX2] != eStatesDiff.BLOCKED)
 						{
 							boolean tmpChanged = updateTmpFields(iY, iX2, eStatesDiff.ISSTAR);
 							if (!hasChanged)
 							{
 								hasChanged = tmpChanged;
 							}
 						}
 					}
 				}
 			}
 			
 			tmpEmptyFields = 0;
 			for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 			{
 				tmpEmptyFields = 0;
 				
 				int columnStars = getCalculatedColumnStars(iX, doMediumSearch);
 				for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 				{
 					if (_tmpDiff[iY][iX] != eStatesDiff.BLOCKED && _tmpDiff[iY][iX] != eStatesDiff.ISSTAR)
 					{
 						tmpEmptyFields++;
 					}
 				}
 
 				if (tmpEmptyFields == columnStars)
 				{
 					for (int iY2 = 0; iY2 < _oBoard.getWidth(); iY2++) 
 					{
 						if (_tmpDiff[iY2][iX] != eStatesDiff.BLOCKED)
 						{
 							boolean tmpChanged = updateTmpFields(iY2, iX, eStatesDiff.ISSTAR);
 							if (!hasChanged)
 							{
 								hasChanged = tmpChanged;
 							}
 						}
 					}
 				}
 			}
 			
 			// cross calculation
 			for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 			{
 				tmpEmptyFields = 0;
 				
 				for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 				{
 					foundEmptySpot = false;
 					foundSpotX = -1;
 					foundSpotY = -1;
 					Field oField = _oBoard.getField(iY, iX);
 					
 					if (oField.getState() == eStates.ARROW_SW)
 					{
 						adder = 1;
 						while (((iY + adder) < _oBoard.getHeight()) && ((iX - adder) >= 0))
 						{
 							if (_tmpDiff[iY + adder][iX - adder] != eStatesDiff.BLOCKED && _tmpDiff[iY + adder][iX - adder] != eStatesDiff.ISSTAR)
 							{
 								if (!foundEmptySpot)
 								{
 									foundSpotX = iX - adder;
 									foundSpotY = iY + adder;
 									foundEmptySpot = true;
 								}
 								else
 								{
 									foundSpotX = -1;
 									foundSpotY = -1;
 									foundEmptySpot = false;
 									break;
 								}
 							}
 							adder++;
 						}
 					}
 					else if (oField.getState() == eStates.ARROW_SE)
 					{
 						adder = 1;
 						while (((iY + adder) < _oBoard.getHeight()) && ((iX + adder) < _oBoard.getWidth()))
 						{
 							if (_tmpDiff[iY + adder][iX + adder] != eStatesDiff.BLOCKED && _tmpDiff[iY + adder][iX + adder] != eStatesDiff.ISSTAR)
 							{
 								if (!foundEmptySpot)
 								{
 									foundSpotX = iX + adder;
 									foundSpotY = iY + adder;
 									foundEmptySpot = true;
 								}
 								else
 								{
 									foundSpotX = -1;
 									foundSpotY = -1;
 									foundEmptySpot = false;
 									break;
 								}
 							}
 							adder++;
 						}
 					}
 					else if (oField.getState() == eStates.ARROW_NW)
 					{
 						adder = 1;
 						while (((iY - adder) >= 0) && ((iX - adder) >= 0))
 						{
 							if (_tmpDiff[iY - adder][iX - adder] != eStatesDiff.BLOCKED && _tmpDiff[iY - adder][iX - adder] != eStatesDiff.ISSTAR)
 							{
 								if (!foundEmptySpot)
 								{
 									foundSpotX = iX - adder;
 									foundSpotY = iY - adder;
 									foundEmptySpot = true;
 								}
 								else
 								{
 									foundSpotX = -1;
 									foundSpotY = -1;
 									foundEmptySpot = false;
 									break;
 								}
 							}
 							adder++;
 						}
 					}
 					else if (oField.getState() == eStates.ARROW_NE)
 					{
 						adder = 1;
 						while (((iY - adder) >= 0) && ((iX + adder) < _oBoard.getWidth()))
 						{
 							if (_tmpDiff[iY - adder][iX + adder] != eStatesDiff.BLOCKED && _tmpDiff[iY - adder][iX + adder] != eStatesDiff.ISSTAR)
 							{
 								if (!foundEmptySpot)
 								{
 									foundSpotX = iX + adder;
 									foundSpotY = iY - adder;
 									foundEmptySpot = true;
 								}
 								else
 								{
 									foundSpotX = -1;
 									foundSpotY = -1;
 									foundEmptySpot = false;
 									break;
 								}
 							}
 							adder++;
 						}
 					}
 					
 					if (foundEmptySpot)
 					{
 						boolean tmpChanged = updateTmpFields(foundSpotY, foundSpotX, eStatesDiff.ISSTAR);
 						if (!hasChanged)
 						{
 							hasChanged = tmpChanged;
 						}
 						
 						foundSpotX = -1;
 						foundSpotY = -1;
 					}
 				}
 			}
 			
 			if (doHardSearch)
 			{
 				boolean upCheckDone = false;
 				boolean downCheckDone = false;
 				foundEmptySpot = false;
 				foundSpotX = -1;
 				foundSpotY = -1;
 				
 				for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 				{
 					upCheckDone = false;
 					downCheckDone = false;
 					int columnStars = getCalculatedColumnStars(iX, doHardSearch);
 					if (1 <= columnStars)
 					{
 						// up check
 						for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 						{
 							foundEmptySpot = false;
 							Field oField = _oBoard.getField(iY, iX);
 							
 							if (!upCheckDone && oField.getState() == eStates.ARROW_N)
 							{
 								upCheckDone = true;
 								for (int iTmpY = 0; iTmpY < iY; iTmpY++) 
 								{
 									if (_tmpDiff[iTmpY][iX] != eStatesDiff.BLOCKED && _tmpDiff[iTmpY][iX] != eStatesDiff.ISSTAR)
 									{
 										if (!foundEmptySpot)
 										{
 											foundSpotX = iX;
 											foundSpotY = iTmpY;
 											foundEmptySpot = true;
 										}
 										else
 										{
 											foundSpotX = -1;
 											foundSpotY = -1;
 											foundEmptySpot = false;
 											break;
 										}
 									}
 								}
 								
 								if (foundEmptySpot)
 								{
 									boolean tmpChanged = updateTmpFields(foundSpotY, foundSpotX, eStatesDiff.ISSTAR);
 									if (!hasChanged)
 									{
 										hasChanged = tmpChanged;
 									}
 									
 									foundSpotX = -1;
 									foundSpotY = -1;
 								}
 							}
 							
 						}
 						
 						// down check
 						for (int iY = _oBoard.getHeight()-1; iY >= 0; iY--) 
 						{
 							foundEmptySpot = false;
 							Field oField = _oBoard.getField(iY, iX);
 							
 							if (!downCheckDone && oField.getState() == eStates.ARROW_S)
 							{
 								downCheckDone = true;
 								for (int iTmpY = iY+1; iTmpY < _oBoard.getHeight(); iTmpY++) 
 								{
 									if (_tmpDiff[iTmpY][iX] != eStatesDiff.BLOCKED && _tmpDiff[iTmpY][iX] != eStatesDiff.ISSTAR)
 									{
 										if (!foundEmptySpot)
 										{
 											foundSpotX = iX;
 											foundSpotY = iTmpY;
 											foundEmptySpot = true;
 										}
 										else
 										{
 											foundSpotX = -1;
 											foundSpotY = -1;
 											foundEmptySpot = false;
 											break;
 										}
 									}
 								}
 								
 								if (foundEmptySpot)
 								{
 									boolean tmpChanged = updateTmpFields(foundSpotY, foundSpotX, eStatesDiff.ISSTAR);
 									if (!hasChanged)
 									{
 										hasChanged = tmpChanged;
 									}
 									
 									foundSpotX = -1;
 									foundSpotY = -1;
 								}
 							}
 							
 						}
 					}
 					
 					
 				}
 				
 				for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 				{
 					upCheckDone = false;
 					downCheckDone = false;
 					int rowStars = getCalculatedRowStars(iY, doHardSearch);
 					if (1 <= rowStars)
 					{
 						// up check
 						for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 						{
 							foundEmptySpot = false;
 							Field oField = _oBoard.getField(iY, iX);
 							
 							if (!upCheckDone && oField.getState() == eStates.ARROW_W)
 							{
 								upCheckDone = true;
 								for (int iTmpX = 0; iTmpX < iX; iTmpX++) 
 								{
 									if (_tmpDiff[iY][iTmpX] != eStatesDiff.BLOCKED && _tmpDiff[iY][iTmpX] != eStatesDiff.ISSTAR)
 									{
 										if (!foundEmptySpot)
 										{
 											foundSpotX = iTmpX;
 											foundSpotY = iY;
 											foundEmptySpot = true;
 										}
 										else
 										{
 											foundSpotX = -1;
 											foundSpotY = -1;
 											foundEmptySpot = false;
 											break;
 										}
 									}
 								}
 								
 								if (foundEmptySpot)
 								{
 									boolean tmpChanged = updateTmpFields(foundSpotY, foundSpotX, eStatesDiff.ISSTAR);
 									if (!hasChanged)
 									{
 										hasChanged = tmpChanged;
 									}
 									
 									foundSpotX = -1;
 									foundSpotY = -1;
 								}
 							}
 							
 						}
 						
 						// up check
 						for (int iX = _oBoard.getWidth()-1; iX >= 0; iX--) 
 						{
 							foundEmptySpot = false;
 							Field oField = _oBoard.getField(iY, iX);
 							
 							if (!downCheckDone && oField.getState() == eStates.ARROW_E)
 							{
 								downCheckDone = true;
 								for (int iTmpX = iX+1; iTmpX < _oBoard.getWidth(); iTmpX++) 
 								{
 									if (_tmpDiff[iY][iTmpX] != eStatesDiff.BLOCKED && _tmpDiff[iY][iTmpX] != eStatesDiff.ISSTAR)
 									{
 										if (!foundEmptySpot)
 										{
 											foundSpotX = iTmpX;
 											foundSpotY = iY;
 											foundEmptySpot = true;
 										}
 										else
 										{
 											foundSpotX = -1;
 											foundSpotY = -1;
 											foundEmptySpot = false;
 											break;
 										}
 									}
 								}
 								
 								if (foundEmptySpot)
 								{
 									boolean tmpChanged = updateTmpFields(foundSpotY, foundSpotX, eStatesDiff.ISSTAR);
 									if (!hasChanged)
 									{
 										hasChanged = tmpChanged;
 									}
 									
 									foundSpotX = -1;
 									foundSpotY = -1;
 								}
 							}
 							
 						}
 					}
 					
 					
 				}
 				
 				if (!hasChanged)
 				{
					boolean tmpChanged = blockSpecialAlgorigthm();
					if (!hasChanged)
					{
						hasChanged = tmpChanged;
					}
 				}
 			}
 
 			isSolved = isSolved();
 			counter++;
 			
 			if (!isSolved && !doMediumSearch &&
 				 (counter > 1 || !hasChanged))
 			{
 				// its medium
 				doMediumSearch = true;
 				hasChanged = true;
 			}
 			
 			if (!isSolved && doMediumSearch && !doHardSearch &&
				 (counter > 5 || !hasChanged))
 			{
 				// its medium
 				doHardSearch = true;
 				hasChanged = true;
 			}
 		}
 		
 		if (isSolved && !doMediumSearch && counter <2)
 		{
 			return BOARD_DIFFICULTY_EASY;
 		}
		else if(isSolved && counter < 6)
 		{
 			return BOARD_DIFFICULTY_MEDIUM;
 		}
 		else if(isSolved)
 		{
 			return BOARD_DIFFICULTY_HARD;
 		}
 		else
 		{
 			calculatedUnsolvableStars();
 			return BOARD_DIFFICULTY_NOT_SOLVABLE;
 		}
 	}
 	
 	
 	public boolean isArrow(eStates state)
 	{
 		if (state == eStates.ARROW_E ||
 			state == eStates.ARROW_N ||
 			state == eStates.ARROW_NE ||
 			state == eStates.ARROW_NW ||
 			state == eStates.ARROW_S ||
 			state == eStates.ARROW_SE ||
 			state == eStates.ARROW_SW ||
 			state == eStates.ARROW_W)
 		{
 			return true;
 		}
 		
 		
 		return false;
 	}
 	
 	public boolean isArrowPointingAtField(int yPos, int xPos)
 	{
 		int adder = 0;
 		// is there a north arrow pointing at me?
 		for (int iY = yPos+1; iY < _oBoard.getHeight(); iY++) 
 		{
 			if (_oBoard.getField(iY, xPos).getState() == eStates.ARROW_N)
 			{
 				return true;
 			}
 		}
 		
 		// is there a south arrow pointing at me?
 		for (int iY = 0; iY < yPos; iY++) 
 		{
 			if (_oBoard.getField(iY, xPos).getState() == eStates.ARROW_S)
 			{
 				return true;
 			}
 		}
 		
 		// is there a west arrow pointing at me?
 		for (int iX = xPos+1; iX < _oBoard.getWidth(); iX++) 
 		{
 			if (_oBoard.getField(yPos, iX).getState() == eStates.ARROW_W)
 			{
 				return true;
 			}
 		}
 		
 		// is there a east arrow pointing at me?
 		for (int iX = 0; iX < xPos; iX++) 
 		{
 			if (_oBoard.getField(yPos, iX).getState() == eStates.ARROW_E)
 			{
 				return true;
 			}
 		}
 		
 		// is there a northeast arrow pointing at me?
 		adder = 1;
 		while (((yPos - adder) >= 0) && ((xPos + adder) < _oBoard.getWidth()))
 		{
 			if (_oBoard.getField((yPos - adder), (xPos + adder)).getState() == eStates.ARROW_SW)
 			{
 				return true;
 			}
 			adder++;
 		}
 		
 		// is there a southeast arrow pointing at me?
 		adder = 1;
 		while (((yPos + adder) < _oBoard.getHeight()) && ((xPos + adder) < _oBoard.getWidth()))
 		{
 			if (_oBoard.getField((yPos + adder), (xPos + adder)).getState() == eStates.ARROW_NW)
 			{
 				return true;
 			}
 			adder++;
 		}
 		
 		// is there a northwest arrow pointing at me?
 		adder = 1;
 		while (((yPos - adder) >= 0) && ((xPos - adder) >= 0))
 		{
 			if (_oBoard.getField((yPos - adder), (xPos - adder)).getState() == eStates.ARROW_SE)
 			{
 				return true;
 			}
 			adder++;
 		}
 		
 		// is there a southwest arrow pointing at me?
 		adder = 1;
 		while (((yPos + adder) < _oBoard.getHeight()) && ((xPos - adder) >= 0))
 		{
 			if (_oBoard.getField((yPos + adder), (xPos - adder)).getState() == eStates.ARROW_NE)
 			{
 				return true;
 			}
 			adder++;
 		}
 		
 		return false;
 	}
 	
 	
 	private boolean blockSpecialAlgorigthm()
 	{
 		boolean hasChanged = false;
 		boolean containsUpwardArrow = false;
 		boolean containsDownwardArrow = false;
 		
 		for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 		{
 			System.out.println("1");
 			int columnStars = getCalculatedColumnStars(iX, true);
 			if (1 == columnStars)
 			{
 				containsUpwardArrow = false;
 				containsDownwardArrow = false;
 				for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 				{
 					System.out.println("2");
 					Field oField = _oBoard.getField(iY, iX);
 					if (oField.getState() == eStates.ARROW_N)
 					{
 						containsDownwardArrow = true;
 					}
 					
 					if (oField.getState() == eStates.ARROW_S)
 					{
 						containsUpwardArrow = true;
 					}
 				}
 				
 				if (containsUpwardArrow)
 				{
 					// collect Fields that are Pointed by Arrow
 					for (int iY = _oBoard.getHeight() -1; iY >= 0; iY--)
 					{
 						System.out.println("3");
 						Field oField = _oBoard.getField(iY, iX);
 						if (oField.getState() == eStates.ARROW_S)
 						{
 							for (int iY2 = iY; iY2 >= 0; iY2--) 
 							{
 								System.out.println("4");
 								boolean tmpChanged = updateTmpFields(iY2, iX, eStatesDiff.BLOCKED);
 								if (!hasChanged)
 								{
 									hasChanged = tmpChanged;
 								}
 								
 							}
 							break;
 						}
 					}
 				}
 				
 				if (containsDownwardArrow)
 				{
 					// collect Fields that are Pointed by Arrow
 					for (int iY = 0; iY < _oBoard.getHeight(); iY++)
 					{
 						System.out.println("5");
 						Field oField = _oBoard.getField(iY, iX);
 						if (oField.getState() == eStates.ARROW_N)
 						{
 							for (int iY2 = iY; iY2 < _oBoard.getHeight(); iY2++) 
 							{
 								System.out.println("6");
 								boolean tmpChanged = updateTmpFields(iY2, iX, eStatesDiff.BLOCKED);
 								if (!hasChanged)
 								{
 									hasChanged = tmpChanged;
 								}
 								
 							}
 							break;
 						}
 					}
 				}
 			}
 		}
 		
 		for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 		{
 			int rowStars = getCalculatedColumnStars(iY, true);
 			if (1 == rowStars)
 			{
 				containsUpwardArrow = false;
 				containsDownwardArrow = false;
 				for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 				{
 					Field oField = _oBoard.getField(iY, iX);
 					if (oField.getState() == eStates.ARROW_W)
 					{
 						containsDownwardArrow = true;
 					}
 					
 					if (oField.getState() == eStates.ARROW_E)
 					{
 						containsUpwardArrow = true;
 					}
 				}
 				
 				if (containsUpwardArrow)
 				{
 					// collect Fields that are Pointed by Arrow
 					for (int iX = _oBoard.getWidth() -1; iX >= 0; iX--)
 					{
 						Field oField = _oBoard.getField(iY, iX);
 						if (oField.getState() == eStates.ARROW_E)
 						{
 							for (int iX2 = iX; iX2 >= 0; iX2--) 
 							{
 								boolean tmpChanged = updateTmpFields(iY, iX2, eStatesDiff.BLOCKED);
 								if (!hasChanged)
 								{
 									hasChanged = tmpChanged;
 								}
 								
 							}
 							break;
 						}
 					}
 				}
 				
 				if (containsDownwardArrow)
 				{
 					// collect Fields that are Pointed by Arrow
 					for (int iX = 0; iX < _oBoard.getWidth(); iX++)
 					{
 						Field oField = _oBoard.getField(iY, iX);
 						if (oField.getState() == eStates.ARROW_W)
 						{
 							for (int iX2 = iX; iX2 < _oBoard.getHeight(); iX2++) 
 							{
 								boolean tmpChanged = updateTmpFields(iY, iX2, eStatesDiff.BLOCKED);
 								if (!hasChanged)
 								{
 									hasChanged = tmpChanged;
 								}
 								
 							}
 							break;
 						}
 					}
 				}
 			}
 		}
 		
 		return hasChanged;
 	}
 	
 	/**
 	 * Gibt an ob das Board gelst ist
 	 * @return solved - Enthlt den Status ob das Editor Board gelst ist
 	 */
 	public boolean isSolved()
 	{
 		boolean solved = true;
 		for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 		{
 			for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 			{
 				if (_tmpDiff[iY][iX] != eStatesDiff.BLOCKED &&
 					_tmpDiff[iY][iX] != eStatesDiff.ISSTAR)
 				{
 					return false;
 				}
 			}
 		}
 		
 		return solved;
 	}
 	
 	/**
 	 * Die temporren Felder werden hier geupdatet
 	 * @param iY - Hier wird die Positon des Feldes auf der Y Achse angegeben
 	 * @param iX - Hier wird die Positon des Feldes auf der X Achse angegeben
 	 * @param state  - Hier wird der Status des Feldes angegeben
 	 * @return boolean - Hier wird zurckgegeben ob die temporren Felder geupdatet wurden
 	 */
 	public boolean updateTmpFields(int iY, int iX, eStatesDiff state)
 	{
 		if (_tmpDiff[iY][iX] != eStatesDiff.BLOCKED &&
 			_tmpDiff[iY][iX] != eStatesDiff.ISSTAR)
 		{
 			_tmpDiff[iY][iX] = state;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Die Anzahl der Sterne fr die Zeile werden hier zurckgegeben
 	 * @param iY - Hier wird die Positon des Feldes auf der Zeile angegeben
 	 * @param isMedium - Hier wird angegeben ob die Schwirigkeit mittel ist
 	 * @return stars - Die anzahl der ausgerechneten Sterne fr die Zeile wird hier zurckgegeben
 	 */	
 	public int getCalculatedRowStars(int iY, boolean isMedium)
 	{
 		int stars = _oBoard.getCountStarsForRow(iY);
 		if (isMedium)
 		{
 			for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 			{
 				if (_tmpDiff[iY][iX] == eStatesDiff.ISSTAR)
 				{
 					stars--;
 				}
 			}
 		}
 		
 		return stars;
 	}
 	
 	/**
 	 * Die Anzahl der Sterne fr die Spalte werden hier zurckgegeben
 	 * @param iX - Hier wird die Positon des Feldes fr Die Spalte angegeben
 	 * @param isMedium - Hier wird angegeben ob die Schwirigkeit mittel ist
 	 * @return stars - Die anzahl der ausgerechneten Sterne fr die Spalte wird hier zurckgegeben
 	 */	
 	public int getCalculatedColumnStars(int iX, boolean isMedium)
 	{
 		int stars = _oBoard.getCountStarsForColumn(iX);
 		if (isMedium)
 		{
 			for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 			{
 				if (_tmpDiff[iY][iX] == eStatesDiff.ISSTAR)
 				{
 					stars--;
 				}
 			}
 		}
 		
 		return stars;
 	}
 	
 	/**
 	 * Die Sterne die unlsbar sind werden hier in eine HashMap eingetragen
 	 */	
 	public void calculatedUnsolvableStars()
 	{
 		HashMap<Integer, Integer> unsolvableStar = null;
 		_unsolvableStars = new HashMap<Integer, HashMap<Integer, Integer>>();
 		
 		for (int iY = 0; iY < _oBoard.getHeight(); iY++) 
 		{
 			for (int iX = 0; iX < _oBoard.getWidth(); iX++) 
 			{
 				unsolvableStar = new HashMap<Integer, Integer>();
 				Field oField = _oBoard.getField(iY, iX);
 				if (oField.getState() == eStates.STAR &&
 					_tmpDiff[iY][iX] != eStatesDiff.ISSTAR)
 				{
 					unsolvableStar.put(iY, iX);
 					_unsolvableStars.put(_unsolvableStars.size(), unsolvableStar);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Die Sterne die unlsbar sind werden hier in eine HashMap eingetragen
 	 * @param _unsolvableStars.size()- Hier wird die Anzahl der Sterne bergeben die unlsboar sind
 	 * @param unsolvableStar - Hier wird das Stern bergeben das unlsbar ist
 	 * @return _unsolvableStars - Hier werden die Sterne zrckgegben die unlsbar sind
 	 */	
 	public Map<Integer, HashMap<Integer, Integer>> getUnsolvableStars()
 	{
 		return _unsolvableStars;
 	}
 }
