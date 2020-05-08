 package swarm.client.managers;
 
 import java.util.ArrayList;
 
 import swarm.client.entities.BufferCell;
 import swarm.client.structs.BufferCellPool;
 
 class CellKillQueue extends A_BufferCellList
 {
	private static final double DEATH_COUNTDOWN = .65;
//	private static final double DEATH_COUNTDOWN = 5.0;
 	
 	CellKillQueue(BufferCellPool pool)
 	{
 		super(pool);
 	}
 	
 	void sentenceToDeath(BufferCell cell)
 	{
 		m_cellList.add(cell);
 		cell.sentenceToDeath(DEATH_COUNTDOWN);
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
 			else if( ithCell.killSlowly_isItDeadQuestionMark(timestep) )
 			{
 				m_cellPool.deallocCell(ithCell);
 				m_cellList.remove(i);
 			}
 		}
 	}
 }
