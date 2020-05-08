 package swarm.client.managers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 
 
 
 
 
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.ClientGrid;
 import swarm.client.entities.I_BufferCellListener;
 import swarm.client.structs.BufferCellPool;
 import swarm.client.structs.I_LocalCodeRepository;
 import swarm.shared.utils.U_Bits;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.entities.E_CodeType;
 import swarm.shared.structs.CellAddressMapping;
 import swarm.shared.structs.GridCoordinate;
 
 
 /**
  * ...
  * @author 
  */
 public class CellBuffer extends A_BufferCellList
 {
 	private static final Logger s_logger = Logger.getLogger(CellBuffer.class.getName());
 	
 	private static final CellAddressMapping s_utilMapping = new CellAddressMapping();
 	
 	private final GridCoordinate m_coord = new GridCoordinate();
 //	private final ArrayList<BufferCell> m_cells = new ArrayList<BufferCell>();
 	private int m_width = 0;
 	private int m_height = 0;
 	
 	private final int m_subCellCount;
 	
 	private final CellBufferManager m_parent;
 	private final CellCodeManager m_codeMngr;
 	private final CellSizeManager m_cellSizeMngr;
 	
 	private final ClientGrid.Obscured m_obscured = new ClientGrid.Obscured();
 	
 	private final CellKillQueue m_killQueue;
 	
 	CellBuffer(CellBufferManager parent, CellCodeManager codeMngr, BufferCellPool cellPool, CellSizeManager cellSizeMngr, int subCellCount, CellKillQueue killQueue)
 	{
 		super(cellPool);
 		
 		m_codeMngr = codeMngr;
 		m_cellSizeMngr = cellSizeMngr;
 		m_parent = parent;
 		m_subCellCount = subCellCount;
 		m_killQueue = killQueue;
 	}
 	
 	public GridCoordinate getCoordinate()
 	{
 		return m_coord;
 	}
 	
 	public int getSubCellCount()
 	{
 		return m_subCellCount;
 	}
 	
 	public int getWidth()
 	{
 		return m_width;
 	}
 	
 	public int getHeight()
 	{
 		return m_height;
 	}
 	
 	public int getCellCount()
 	{
 		return m_cellList.size() + m_killQueue.m_cellList.size();
 	}
 	
 	void setExtents(int m, int n, int width, int height)
 	{
 		m_coord.set(m, n);
 		
 		m_width = width;
 		m_height = height;
 	}
 	
 	public BufferCell getCellAtIndex(int index)
 	{
 		if( index < m_cellList.size() )
 		{
 			return m_cellList.get(index);
 		}
 		else
 		{
 			if( m_killQueue.m_cellList.size() > 0 )
 			{
 				return m_killQueue.m_cellList.get(index-m_cellList.size());
 			}
 		}
 		
 		return null;
 	}
 	
 	public boolean isInBoundsAbsolute(GridCoordinate absolute)
 	{
 		return isInBoundsAbsolute(absolute.getM(), absolute.getN());
 	}
 	
 	public boolean isInBoundsAbsolute(int m, int n)
 	{
 		return m >= m_coord.getM() && m < m_coord.getM()+m_width && n >= m_coord.getN() && n < m_coord.getN()+m_height;
 	}
 	
 	public BufferCell getCellAtAbsoluteCoord(GridCoordinate absoluteCoord)
 	{
 		return getCellAtAbsoluteCoord(absoluteCoord.getM(), absoluteCoord.getN());
 	}
 	
 	void imposeBuffer(ClientGrid grid, CellBuffer otherBuffer, I_LocalCodeRepository localCodeSource, int highestSubCellCount, int options__extends__smF_BufferUpdateOption)
 	{
 		if( m_subCellCount == 16 )
 		{
 //			s_logger.severe("");
 		}
 		boolean createVisualizations = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.CREATE_VISUALIZATIONS) != 0;
 		boolean communicateWithServer = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.COMMUNICATE_WITH_SERVER) != 0;
 		boolean flushPopulator = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.FLUSH_CELL_POPULATOR) != 0;
 
 		int i;
 		int m, n;
 		
 		int otherBufferCellCount = otherBuffer.m_cellList.size();
 
 		if( highestSubCellCount < m_subCellCount )
 		{
 			for( i = otherBufferCellCount-1; i >= 0 ; i-- )
 			{
 				BufferCell ithCellFromOtherBuffer = otherBuffer.m_cellList.get(i);
 				
 				if( !this.isInBoundsAbsolute(ithCellFromOtherBuffer.getCoordinate()) )
 				{
 					m_cellPool.deallocCell(ithCellFromOtherBuffer);
 				}
 				else
 				{
 					m_killQueue.sentenceToDeath(ithCellFromOtherBuffer);
 				}
 			}
 			
 			otherBuffer.m_cellList.clear();
 			
 			return;
 		}
 		else
 		{
 			for( i = 0; i < otherBuffer.m_cellList.size(); i++ )
 			{
 				BufferCell ithCellFromOtherBuffer = otherBuffer.m_cellList.get(i);
 				
 				if( !this.isInBoundsAbsolute(ithCellFromOtherBuffer.getCoordinate()) )
 				{
 					m_cellPool.deallocCell(ithCellFromOtherBuffer);
 					otherBuffer.m_cellList.set(i, null);
 				}
 			}
 		}
 
 		int limitN = m_coord.getN() + m_height;
 		int limitM = m_coord.getM() + m_width;
 		for ( n = m_coord.getN(); n < limitN; n++ )
 		{
 			for ( m = m_coord.getM(); m < limitM; m++ )
 			{
 				boolean obscured = false;
 				
 				if( highestSubCellCount > m_subCellCount )
 				{
 					if( grid.isObscured(m, n, m_subCellCount, highestSubCellCount, m_obscured) )
 					{
 						obscured = true;
 						CellBuffer higherBuffer = m_parent.getDisplayBuffer(U_Bits.calcBitPosition(m_obscured.subCellDimension));
 						BufferCell cell = higherBuffer.getCellAtAbsoluteCoord(m_obscured.m, m_obscured.n);
 						
 						if( cell != null )
 						{
 							I_BufferCellListener visualization = cell.getVisualization();
 							
							if( visualization.isLoaded() || otherBuffer.getCellAtAbsoluteCoord(m, n) == null )
 							{
 								if( m_obscured.offset == 1 )
 								{
 									continue;
 								}
 								else if( m_obscured.offset > 1 )
 								{
 									m += m_obscured.offset;
 									m -= 1; // cancel out next increment in for loop
 									
 									continue;
 								}
 							}
 						}
 					}
 				}
 				
 				if( !grid.isTaken(m, n, m_subCellCount) )  continue;
 				if( swap(m, n, otherBuffer, this) )  continue;
 				if( swap(m, n, m_killQueue, this) )  continue;
 				
 				//--- DRK > If we're obscured then we attempt to swap an existing cell
 				//---		from the other buffer or the kill queue, but we don't make new cells.
 				if( obscured )  continue;
 				
 				BufferCell newCell = m_cellPool.allocCell(grid, m_subCellCount, createVisualizations);
 				this.m_cellList.add(newCell);
 				
 //				if( m_subCellCount == 2 )
 //				{
 //					s_logger.severe("ERER");;
 //				}
 				
 				newCell.getCoordinate().set(m, n);
 				
 //				s_logger.severe(m_subCellCount+"");
 				m_codeMngr.populateCell(newCell, localCodeSource, m_subCellCount, communicateWithServer, E_CodeType.SPLASH);
 				
 				s_utilMapping.getCoordinate().copy(newCell.getCoordinate());
 				
 				if( m_subCellCount == 1 )
 				{
 					m_cellSizeMngr.populateCellSize(s_utilMapping, this.m_parent, newCell);
 				}
 			}
 		}
 		
 		if( flushPopulator )
 		{
 			m_codeMngr.flush();
 		}
 		
 		for( i = 0; i < otherBufferCellCount; i++ )
 		{
 			BufferCell ithCell = otherBuffer.m_cellList.get(i);
 
 			if( ithCell != null )
 			{
 				m_killQueue.sentenceToDeath(ithCell);
 			}
 		}
 
 		otherBuffer.m_cellList.clear();
 	}
 	
 	@Override public BufferCell getCellAtAbsoluteCoord(int m, int n)
 	{
 		BufferCell fromSuper = super.getCellAtAbsoluteCoord(m, n);
 		
 		if( fromSuper != null )  return fromSuper;
 		
 		return m_killQueue.getCellAtAbsoluteCoord(m, n);
 	}
 	
 	@Override void drain()
 	{
 		super.drain();
 		
 		this.setExtents(0, 0, 0, 0);
 	}
 	
 	@Override public String toString()
 	{
 		return m_coord.getM() + " " + m_coord.getN() + " " +  m_width + " " +  m_height;
 	}
 }
