 package swarm.client.managers;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.ClientGrid;
 import swarm.client.entities.ClientGrid.Obscured;
 import swarm.client.structs.BufferCellPool;
 import swarm.shared.utils.U_Bits;
 
 class CellKillQueue extends A_BufferCellList
 {
 	private static final Logger s_logger = Logger.getLogger(CellKillQueue.class.getName());
 	
 	private final class CustomObscured extends ClientGrid.Obscured
 	{		
 		@Override public boolean stopOnCurrentObscuringCell()
 		{
 			int index = U_Bits.calcBitPosition(this.subCellCount);
 			CellBuffer buffer = m_parent.getDisplayBuffer(index);
 			BufferCell cell = buffer.getCellAtAbsoluteCoord(this.m, this.n);
 			
 			if( cell == null )  return false;
 			if( cell.getVisualization() == null )  return false;
 			
 			if( !cell.getVisualization().isLoaded() )
 			{
 				return true;
 			}
 			
 			if( !cell.getVisualization().isFullyDisplayed() )
 			{
 				return true;
 			}
 			
 			return false;
 		}
 	};
 	
	private final CustomObscured m_obscured_everythingAbove = new CustomObscured();
 	
	private final ClientGrid.Obscured m_obscured_below = new ClientGrid.Obscured();
 	private final double m_deathCountdown;
 	
 	CellKillQueue(CellBufferManager parent, int subCellCount, BufferCellPool pool, double deathCountdown)
 	{
 		super(parent, subCellCount, pool);
 		
 		m_deathCountdown = deathCountdown;
 	}
 	
 	void sentenceToDeath(BufferCell cell)
 	{
 		m_cellList.add(cell);
 		cell.sentenceToDeath(m_deathCountdown);
 	}
 	
 	void update(double timestep)
 	{
 		for( int i = m_cellList.size()-1; i >= 0; i--)
 		{
 			BufferCell ithCell = m_cellList.get(i);
 			
 			if( ithCell == null )
 			{
 				m_cellList.remove(i);
 			}
 			else
 			{
 				ithCell.updateDeathRowTimer(timestep);
 			}
 		}
 	}
 	
	// LOGIC OK
 	private boolean isEverythingLoadedAbove(ClientGrid grid, BufferCell cell)
 	{
 		int maxSubCellCount = m_parent.getHighestDisplayBuffer().getSubCellCount();
 		
		if( grid.isObscured(cell.getCoordinate().getM(), cell.getCoordinate().getN(), m_subCellCount, maxSubCellCount, m_obscured_everythingAbove) )
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
	private boolean isEverythingLoadedUnderneath(BufferCell cell, ClientGrid grid)
 	{
 		if( m_subCellCount <= 1 )  return true;
 		
 		for( int ithSubCellCount = m_subCellCount >>> 1; ithSubCellCount > 0; ithSubCellCount >>>= 1 )
 		{
 			int index = U_Bits.calcBitPosition(ithSubCellCount);
 			CellBuffer buffer = m_parent.getDisplayBuffer(index);
 			
 			for( int j = 0; j < buffer.m_cellList.size(); j++ )
 			{
 				BufferCell jthCell = buffer.m_cellList.get(j);
 				
 				if( jthCell == null )  continue;
 				
 				if( isObscuring(grid, cell, jthCell, ithSubCellCount))
 				{
					if( !jthCell.getVisualization().isFullyDisplayed() )
 					{
 						return false;
 					}
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isObscuring(ClientGrid grid, BufferCell highCell, BufferCell lowCell, int subCellCount_low)
 	{
 		if( lowCell == null )  return false;
 		
		//--- DRK > Using max depth of 1 here cause we don't care about checking past highCell's subCellCount.
		if( grid.isObscured(lowCell.getCoordinate().getM(), lowCell.getCoordinate().getN(), subCellCount_low, m_subCellCount, /*maxDepth=*/1, m_obscured_below) )
 		{
			if( m_subCellCount == m_obscured_below.subCellCount && highCell.getCoordinate().isEqualTo(m_obscured_below.m, m_obscured_below.n) )
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	void clearTheDead(ClientGrid grid)
 	{
 		for( int i = m_cellList.size()-1; i >= 0; i--)
 		{
 			BufferCell ithCell = m_cellList.get(i);
 			
 			if( ithCell == null )
 			{
 				m_cellList.remove(i);
 			}
 			else if
 			(
 				!ithCell.getVisualization().isLoaded() ||//&& m_subCellCount > 1	||
 				ithCell.kill()													||
				(isEverythingLoadedAbove(grid, ithCell) && isEverythingLoadedUnderneath(ithCell, grid))
 			)
 			{
 				for( int ithSubCellCount = m_subCellCount >>> 1; ithSubCellCount > 0; ithSubCellCount >>>= 1 )
 				{
 					int index = U_Bits.calcBitPosition(ithSubCellCount);
 					CellBuffer buffer = m_parent.getDisplayBuffer(index);
 					
 					for( int j = 0; j < buffer.m_cellList.size(); j++ )
 					{
 						BufferCell jthCell = buffer.m_cellList.get(j);
 						
 						if( isObscuring(grid, ithCell, jthCell, ithSubCellCount) )
 						{
 							jthCell.getVisualization().onRevealed();
 						}
 					}
 				}
 				
 //				int m = ithCell.getCoordinate().getM();
 //				int n = ithCell.getCoordinate().getN();
 //				int subCellCount = m_subCellCount;
 //				s_logger.severe("killing " + subCellCount + " " + m + " " + n);
 				
 				m_cellPool.deallocCell(ithCell);
 				m_cellList.remove(i);
 			}
 		}
 	}
 }
