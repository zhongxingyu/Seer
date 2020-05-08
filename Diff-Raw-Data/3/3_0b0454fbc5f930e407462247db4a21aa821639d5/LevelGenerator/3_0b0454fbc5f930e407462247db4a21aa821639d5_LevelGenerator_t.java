 package de.hotware.blockbreaker.model.generator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import de.hotware.blockbreaker.model.Block;
 import de.hotware.blockbreaker.model.Level;
 import de.hotware.blockbreaker.model.WinCondition;
 import de.hotware.blockbreaker.model.Block.BlockColor;
 import de.hotware.blockbreaker.model.Level.Gravity;
 import de.hotware.blockbreaker.util.misc.Randomizer;
 
 /**
  * I am pretty proud of this little baby. Took me a while figuring out the
  * algorithm, but behold: The almighty LevelGenerator that renders the need
  * for creating a LevelEditor to a not that big concern anymore.
  * @author Martin Braun
  * @since Jan 2012
  */
 public class LevelGenerator {
 	
 	private static final int LEVEL_WIDTH = 6;
 	private static final int LEVEL_HEIGHT = 6;
 	
 	/**
 	 * @return randomly created Level
 	 */
 	public static Level randomUncheckedLevel() {
 		ArrayList<Block> list = new ArrayList<Block>();
 		Block[][] matrix = new Block[LEVEL_WIDTH][LEVEL_HEIGHT];
 		fillRestOfMatrixWithRandomBlocks(matrix);
         for(int i = 0; i < LEVEL_WIDTH; ++i) {
         	for(int j = 0; j < LEVEL_HEIGHT; ++j) {
         		list.add(new Block(BlockColor.random()));        		
         	}
         }
         WinCondition win = new WinCondition(Randomizer.nextInt(7),
         		Randomizer.nextInt(7),
         		Randomizer.nextInt(7),
         		Randomizer.nextInt(7),
         		Randomizer.nextInt(7));
 		Level level = new Level(matrix, Gravity.NORTH, list, win);
 		return level;
 	}
 	
 	public static Level createRandomLevelFromSeed(long pSeed, int pNumberOfMoves, int pWinCount) {
 		Randomizer.setSeed(pSeed);
 		Level level = createRandomSolvedLevel(pWinCount);
 		level.getReplacementList().ensureCapacity(pNumberOfMoves);
 		rearrangeLevel(level, pNumberOfMoves);
 		Randomizer.newRandomObject();
 		return level;
 	}
 	
 	public static Level createRandomSolvedLevel(int pWinCount) {
 		Block[][] matrix = new Block[LEVEL_WIDTH][LEVEL_HEIGHT];
 		
 		WinCondition win = null;
 		
 		while(win == null || win.getTotalWinCount() < pWinCount) {
 			win = new WinCondition(Randomizer.nextInt(7),
 					Randomizer.nextInt(7),
 	        		Randomizer.nextInt(7),
 	        		Randomizer.nextInt(7),
 	        		Randomizer.nextInt(7));
 		}
 	        		
 		WinValuePair[] sorting = new WinValuePair[5];
 		for(int i = 0; i < sorting.length; ++i) {
 			sorting[i] = new WinValuePair(BlockColor.numberToColor(i+1), win.getWinCount(i+1));
 		}
 		
 		Arrays.sort(sorting);
 		
 		fillMatrixWithConditionalBlocks(matrix, sorting);
 		
 		fillRestOfMatrixWithRandomBlocks(matrix);
 		
 		ArrayList<Block> repl = new ArrayList<Block>();
 		Level level = new Level(matrix, Gravity.NORTH, repl, win);
 		
 		if(!level.checkWin()) {
 			return createRandomSolvedLevel(pWinCount);
 		}
 		
 		return level;
 	}
 	
 	public static void rearrangeLevel(Level pLevel, int pNumberOfMoves) {		
 		while(pLevel.checkWin()) {
 			createReplacementList(pLevel, pNumberOfMoves);
 		}
 	}
 	
 	////////////////////////////////////////////////////////////////////
 	////					Private Methods							////
 	////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * Moves the blocks in the Array around and creates a ReplacementList
 	 * @return the Replacementlist for the given matrix
 	 */
 	private static void createReplacementList(Level pLevel, 
 			int pNumberOfMoves) {
 		ArrayList<Block> replacementList = pLevel.getReplacementList();
 		Gravity oldGrav = pLevel.getGravity();
 		int x;
 		int y;
 		for(int j = 0; j < pNumberOfMoves; ++j) {
 			Gravity grav = Gravity.random();
 			if(grav == Gravity.SOUTH || grav == Gravity.NORTH) {
 				x = Randomizer.nextInt(LEVEL_WIDTH);
 				y = (LEVEL_HEIGHT - 1) * Randomizer.nextInt(2);
 			} else {
 				x = (LEVEL_WIDTH - 1) * Randomizer.nextInt(2);
 				y = Randomizer.nextInt(LEVEL_HEIGHT);
 			}
 			pLevel.setGravity(grav);
 			Block removed = pLevel.removeBlock(x, y, new Block(BlockColor.random()));
 			if(!pLevel.checkWin()) {
 				replacementList.add(removed);
 			} else {
				//repeat the whole loop from the beginning
				j = -1;
 				replacementList.clear();
 			}
 			
 		}
 		pLevel.setGravity(oldGrav);
 	}
 	
 	private static void fillRestOfMatrixWithRandomBlocks(Block[][] pMatrix) {
 		for(int i = 0; i < LEVEL_WIDTH; ++i) {
         	for(int j = 0; j < LEVEL_HEIGHT; ++j) {
         		if(pMatrix[i][j] == null) {
         			pMatrix[i][j] = new Block(BlockColor.random(), i, j);        
         		}
         	}
         }
 	}
 	
 	private static void fillMatrixWithConditionalBlocks(Block[][] pMatrix, WinValuePair[] pWinValuePairs) {
 		BlockColor blockColor;
 		int winCount;
 		for(int i = 0; i < pWinValuePairs.length; ++i) {
 			winCount = pWinValuePairs[i].getWinCount();
 			if(winCount == 0) {
 				break;
 			}
 			blockColor = pWinValuePairs[i].getBlockColor();
 			if(Randomizer.nextInt(2) == 1) {
 				if(!setToRow(pMatrix, blockColor, winCount)) {
 					setToColumn(pMatrix, blockColor, winCount);
 				}
 			} else {
 				if(!setToColumn(pMatrix, blockColor, winCount)) {
 					setToRow(pMatrix, blockColor, winCount);
 				}
 			}
 		}
 	}
 	
 	private static boolean setToRow(Block[][] pMatrix, BlockColor pColor, int pSize) {
 		//compute the fitting rows
 		ArrayList<Integer> help  = new ArrayList<Integer>();
 		for(int i = 0; i < LEVEL_WIDTH; ++i) {
 			if(fitsInRow(pMatrix, i, pSize)) {
 				help.add(i);
 			}	
 		}
 		int count = help.size();
 		
 		//compute the fitting positions in the chosen row and set it to a random possible position
 		if(count > 0) {			
 			int randomRowNumber = help.get(Randomizer.nextInt(count));
 			help.clear();
 			count = 0;
 			for(int i = 0; i < LEVEL_HEIGHT; ++i) {
 				if(fitsInRowPosition(pMatrix, randomRowNumber, i, pSize)) {
 					help.add(i);
 				}
 			}
 			count = help.size();
 			if(count > 0) {
 				int randomPosNumber = help.get(Randomizer.nextInt(count));
 				setToPositionInRow(pMatrix, randomRowNumber, randomPosNumber, pColor, pSize);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private static boolean fitsInRow(Block[][] pMatrix, int pRow, int pSize) {
 		int space = 0;
 		for(int i = 0; i < LEVEL_WIDTH && space < pSize; ++i) {
 			if(pMatrix[pRow][i] == null) {
 				++space;
 			} else {
 				space = 0;
 			}
 		}
 		return space == pSize;
 	}
 	
 	private static boolean fitsInRowPosition(Block[][] pMatrix, int pRow, int pPosition, int pSize) {
 		int space = 0;
 		for(int i = pPosition; i < LEVEL_WIDTH && space < pSize; ++i) {
 			if(pMatrix[pRow][i] == null) {
 				++space;
 			} else {
 				break;
 			}
 		}
 		return space == pSize;
 	}
 	
 	private static void setToPositionInRow(Block[][] pMatrix, int pRow, int pPosition, BlockColor pColor, int pSize) {
 		pSize += pPosition;
 		for(int i = pPosition; i < pSize; ++i) {
 			pMatrix[pRow][i] = new Block(pColor, pRow, i);
 		}
 	}
 	
 	private static boolean setToColumn(Block[][] pMatrix, BlockColor pColor, int pSize) {
 		//compute the fitting columns
 		ArrayList<Integer> help = new ArrayList<Integer>();
 		for(int i = 0; i < LEVEL_WIDTH; ++i) {
 			if(fitsInColumn(pMatrix, i, pSize)) {
 				help.add(i);
 			}
 		}
 		int count = help.size();
 		
 		//compute the fitting positions in the chosen column and set it to a random possible position
 		if(count > 0) {			
 			int randomColumnNumber = help.get(Randomizer.nextInt(count));
 			help.clear();
 			for(int i = 0; i < LEVEL_WIDTH; ++i) {
 				if(fitsInColumnPosition(pMatrix, randomColumnNumber, i, pSize)) {
 					help.add(i);
 				}
 			}
 			count = help.size();
 			if(count > 0) {
 				int randomPosNumber = help.get(Randomizer.nextInt(count));
 				setToPositionInColumn(pMatrix, randomColumnNumber, randomPosNumber, pColor, pSize);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private static boolean fitsInColumn(Block[][] pMatrix, int pColumn, int pSize) {
 		int space = 0;
 		for(int i = 0; i < LEVEL_WIDTH && space < pSize; ++i) {
 			if(pMatrix[i][pColumn] == null) {
 				++space;
 			} else {
 				space = 0;
 			}
 		}
 		return space == pSize;
 	}
 	
 	private static boolean fitsInColumnPosition(Block[][] pMatrix, int pColumn, int pPosition, int pSize) {
 		int space = 0;
 		for(int i = pPosition; i < LEVEL_WIDTH && space < pSize; ++i) {
 			if(pMatrix[i][pColumn] == null) {
 				++space;
 			} else {
 				break;
 			}
 		}
 		return space == pSize;
 	}
 	
 	private static void setToPositionInColumn(Block[][] pMatrix, int pColumn, int pPosition, BlockColor pColor, int pSize) {
 		pSize += pPosition;
 		for(int i = pPosition; i < pSize; ++i) {
 			pMatrix[i][pColumn] = new Block(pColor, i, pColumn);
 		}
 	}
 	
 	////////////////////////////////////////////////////////////////////
 	////					    Inner Classes						////
 	////////////////////////////////////////////////////////////////////
 	
 	private static class WinValuePair implements Comparable<WinValuePair> {
 		
 		private final BlockColor mBlockColor;
 		private final int mWinCount;
 		
 		public WinValuePair(BlockColor pBlockColor, int pWinCount) {
 			this.mBlockColor = pBlockColor;
 			this.mWinCount = pWinCount;
 		}
 		
 		public BlockColor getBlockColor() {
 			return this.mBlockColor;
 		}
 		
 		public int getWinCount() {
 			return this.mWinCount;
 		}
 
 		@Override
 		public int compareTo(WinValuePair pOther) {
 			return pOther.getWinCount() - this.mWinCount;
 		}
 		
 	}
 	
 }
