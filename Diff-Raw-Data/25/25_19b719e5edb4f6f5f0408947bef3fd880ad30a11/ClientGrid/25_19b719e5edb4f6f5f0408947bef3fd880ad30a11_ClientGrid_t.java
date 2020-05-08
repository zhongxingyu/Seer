 package swarm.client.entities;
 
 import java.util.ArrayList;
 
 import swarm.shared.entities.A_Grid;
 import swarm.shared.json.A_JsonFactory;
 import swarm.shared.json.E_JsonKey;
 import swarm.shared.json.I_JsonObject;
 import swarm.shared.structs.BitArray;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.utils.U_Bits;
 
 public class ClientGrid extends A_Grid
 {
 	private BitArray[] m_metaOwnership = new BitArray[0];
 	
 	public BitArray getBaseOwnership()
 	{
 		return m_ownership;
 	}
 
 	private boolean isTaken(int bitIndex, int subCellCount)
 	{
 		if( subCellCount == 1 )
 		{
 			return isTaken(bitIndex);
 		}
 		
 		int ownershipIndex = U_Bits.calcBitPosition(subCellCount) - 1;
 		
 		if( ownershipIndex < 0 || ownershipIndex >= m_metaOwnership.length )
 		{
 			return false;
 		}
 		
 		BitArray ownership = m_metaOwnership[ownershipIndex];
 		
 		return ownership.isSet(bitIndex);
 	}
 	
 	public boolean isTaken(int m, int n, int subCellCount)
 	{
 		if( m_ownership == null )  return false;
 		
 		int gridSizeSub = getSubDimension(getWidth(), subCellCount);
 		
 		int bitIndex = calcBitIndex(n, m, gridSizeSub);
 		
 		return isTaken(bitIndex, subCellCount);
 	}
 	
 	private int getSubDimension(int dimension, int subCellCount)
 	{
 		//--- DRK > Not relying on mantissa-chopping round-down integer division
 		//---		cause of GWT/Javascript unknowns, but it would probably work.
 		return (dimension - (dimension % subCellCount)) / subCellCount;
 	}
 	
	public int getObscureOffset(int m, int n, int subCellCount, int potentiallyObscuringSubCellCount)
 	{
		if( potentiallyObscuringSubCellCount <= subCellCount )  return 0;
 		
 		for( int currentSubCellCount = subCellCount*2; currentSubCellCount <= potentiallyObscuringSubCellCount; currentSubCellCount *= 2)
 		{
 			m = getSubDimension(m, 2);
 			n = getSubDimension(n, 2);
 			
 			if( isTaken(m, n, currentSubCellCount) )
 			{
				return currentSubCellCount;
 			}
 		}
 		
		return 0;
 	}
 	
 	private int calcMetaBitIndexFromRawCoords(int rawRow, int rawCol, int subCellCount)
 	{
 		int gridSize = this.getWidth();
 		int gridSizeSub = getSubDimension(gridSize, subCellCount);
 //		int bitIndex = row * (gridSize/subCellCount) + col/subCellCount;
 		int rowSub = getSubDimension(rawRow, subCellCount);
 		int colSub = getSubDimension(rawCol, subCellCount);
 		
 		return calcBitIndex(rowSub, colSub, gridSizeSub);
 	}
 	
 	private int calcBitIndex(int row, int col, int gridSize)
 	{
 		return row * gridSize + col;
 	}//15, 13, 431
 	
 	public boolean isTaken(GridCoordinate coordinate, int subCellCount)
 	{
 		return isTaken(coordinate.getM(), coordinate.getN(), subCellCount);
 //		int m = getSubDimension(coordinate.getM(), subCellCount);
 //		int n = getSubDimension(coordinate.getN(), subCellCount);
 //		int bitIndex = calcBitIndex(n, m, getSubDimension(getWidth(), subCellCount));
 //		
 //		return isTaken(bitIndex, subCellCount);
 	}
 	
 	private boolean ownsMetaCell(int rawRow, int rawCol, int subCellCount)
 	{
 		int rawOwnershipCount = 0;
 		int blockSize = subCellCount/2;
 		int rawGridSize = getWidth();
 		
 		for( int row = rawRow; row < rawRow + subCellCount; row+=blockSize  )
 		{
 			for( int col = rawCol; col < rawCol + subCellCount; col+=blockSize )
 			{
 				for( int row_sub = row; row_sub < row+blockSize; row_sub++ )
 				{
 					boolean doBreak = false;
 					
 					for( int col_sub = col; col_sub < col+blockSize; col_sub++ )
 					{
 						int gridBitIndex = calcBitIndex(row_sub, col_sub, rawGridSize);
 						
 						if( m_ownership.isSet(gridBitIndex) )
 						{
 							rawOwnershipCount++;
 							
 							if( rawOwnershipCount == 2 )
 							{
 								return true;
 							}
 							else
 							{
 								doBreak = true;
 								break;
 							}
 						}
 					}
 					
 					if( doBreak )  break;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public int getMetaLevelCount()
 	{
 		return m_metaOwnership.length + 1;
 	}
 	
 	@Override public void readJson(I_JsonObject json, A_JsonFactory factory)
 	{
 		super.readJson(json, factory);
 		
 		int gridSize = this.getWidth();
 		
 		if( !U_Bits.isPowerOfTwo(gridSize) )  return;
 		
 		//TODO: maybe have optional early-out if for example the grid is 4x4 and the app just wants to always show raw cells
 		//		and doesn't care about meta cells.
 		
 		int arraySize = U_Bits.calcBitPosition(gridSize) - 1;
 		
 		if( arraySize == 0  )  return; // effectively early out for 2x2 case...no real need for meta cells.
 		
 		int currentMetaGridSize = gridSize / 2;
 
 		m_metaOwnership = new BitArray[arraySize];
 		int subCellCount = 2;
 		
 		for( int i = 0; i < arraySize; i++ )
 		{
 			m_metaOwnership[i] = new BitArray(currentMetaGridSize*currentMetaGridSize);
 			
 			for( int rawRow = 0; rawRow < gridSize; rawRow+=subCellCount )
 			{
 				for( int rawCol = 0; rawCol < gridSize; rawCol+=subCellCount )
 				{
 					if( ownsMetaCell(rawRow, rawCol, subCellCount) )
 					{
 						int metaBitIndex = calcMetaBitIndexFromRawCoords(rawRow, rawCol, subCellCount);
 
 						m_metaOwnership[i].set(metaBitIndex, true);
 					}
 				}
 			}
 			
 			subCellCount *= 2;
 			currentMetaGridSize /= 2;
 		}
 	}
 	
 	public String toString(int subCellN)
 	{
 		final GridCoordinate coord = new GridCoordinate();
 		String toReturn = "";
 		for( int row = 0; row < getHeight() / subCellN; row++ )
 		{
 			for( int col = 0; col < getWidth() / subCellN; col++ )
 			{
 				if( isTaken(col, row, subCellN) )
 				{
 					toReturn += "O";
 				}
 				else
 				{
 					toReturn += "X";
 				}
 			}
 			
 			toReturn += "\n";
 		}
 		
 		return toReturn;
 	}
 	
 	@Override public String toString()
 	{
 		return toString(2);
 	}
 }
