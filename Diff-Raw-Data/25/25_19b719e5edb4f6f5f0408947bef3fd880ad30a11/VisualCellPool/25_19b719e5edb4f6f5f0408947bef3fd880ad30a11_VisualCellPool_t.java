 package swarm.client.view.cell;
 
 import java.util.ArrayList;
 
 import swarm.shared.statemachine.*;
 
 import com.google.gwt.user.client.ui.Widget;
 
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.I_BufferCellListener;
 import swarm.client.managers.CameraManager;
 import swarm.client.states.camera.I_State_SnappingOrViewing;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.structs.I_CellPoolDelegate;
 import swarm.client.view.ViewContext;
 import swarm.client.view.sandbox.SandboxManager;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.E_CodeSafetyLevel;
 import swarm.shared.memory.ObjectPool;
 import swarm.shared.reflection.I_Class;
 
 public class VisualCellPool implements I_CellPoolDelegate
 {
 	private static final class CustomPool extends ObjectPool<VisualCell>
 	{
 		public CustomPool(I_Class<VisualCell> type)
 		{
 			super(type);
 		}
 		
 		void cleanCode()
 		{
 			for( int i = getAllocCount(); i < this.m_instances.size(); i++ )
 			{
 				VisualCell cell = this.m_instances.get(i);
 				
 				if( cell.getParent() != null )
 				{
 					cell.removeFromParent();
 				}
 				
 				cell.showEmptyContent();
 			}
 		}
 	}
 	
 	private final I_Class<VisualCell> m_visualCellClass;
 	
 	private final CustomPool m_pool;
 	
 	private final ArrayList<VisualCell> m_queuedRemovals = new ArrayList<VisualCell>();
 	
 	private final Widget m_cellContainer;
 	
 	private int m_queueIndex = 0;
 	
 	private int m_maxQueueSize = 40;
 	
 	private boolean m_poolNeedsCleaning = false;
 	
 	private final ViewContext m_viewContext;
 	
 	private final VisualCell.I_CodeListener m_codeListener = new VisualCell.I_CodeListener()
 	{
 		@Override
 		public void onCodeLoaded(VisualCell cell)
 		{
 			BufferCell bufferCell = cell.getBufferCell();
 			
 			if( bufferCell == null )  return;
 			if( bufferCell.getFocusedCellSize() == null )  return;
 			if( !bufferCell.getFocusedCellSize().hasNaturalDimension() )  return;
 			
 			StateMachine_Camera cameraMachine = m_viewContext.stateContext.get(StateMachine_Camera.class);
 			A_State state = cameraMachine.getCurrentState();
 			if( state instanceof I_State_SnappingOrViewing )
 			{
 				I_State_SnappingOrViewing snappingOrViewing = (I_State_SnappingOrViewing) state;
 				
 				if( snappingOrViewing.getTargetCoord().equals(bufferCell.getCoordinate()) )
 				{
 					//--- DRK > Hacky way of seeing if we're leaving the viewing state.
 					if( state instanceof State_ViewingCell && !bufferCell.isFocused() )
 					{
 						return;
 					}
 					
 					m_viewContext.scrollNavigator.onResize();
 				}
 			}
 		}
 	};
 	
 	VisualCellPool(final SandboxManager sandboxMngr, Widget cellContainer, final I_CellSpinnerFactory spinnerFactory, final ViewContext viewContext)
 	{
 		m_cellContainer = cellContainer;
 		m_viewContext = viewContext;
 		
 		m_visualCellClass = new I_Class<VisualCell>()
 		{
 			@Override
 			public VisualCell newInstance()
 			{
 				VisualCell cell = new VisualCell(spinnerFactory.newSpinner(), sandboxMngr, viewContext.appContext.cameraMngr, viewContext.config.cellRetractionEasing, viewContext.config.cellSizeChangeTime_seconds);
 				cell.setCodeListener(m_codeListener);
 				
 				return cell;
 			}
 		};
 		
 		m_pool = new CustomPool(m_visualCellClass);
 	}
 	
 	public I_BufferCellListener createVisualization(BufferCell bufferCell, int width, int height, int padding, int subCellDim)
 	{
 		VisualCell newVisualCell = m_pool.allocate();
 		newVisualCell.setVisible(true);
 		
 		//s_logger.severe("Creating: " + newVisualCell.getId() + " at " + smCellBufferManager.getInstance().getUpdateCount());
 		
 		if( newVisualCell.getParent() != null )
 		{
 			//--- DRK > Invalid assert...for now cells' visibility is just toggled.
 			//smU_Debug.ASSERT(false, "createVisualization1");
 		}
 		
 		newVisualCell.onCreate(bufferCell, width, height, padding, subCellDim);
 		
 		return newVisualCell;
 	}
 	
 	public void destroyVisualization(I_BufferCellListener visualization)
 	{
 		VisualCell visualCell = (VisualCell) visualization;
 		
 		//s_logger.severe("Destroying: " + visualCell.getId() + " at " + smCellBufferManager.getInstance().getUpdateCount());
 		
 		if( visualCell.getParent() != m_cellContainer )
 		{
 			//--- DRK > Fringe case can hit this assert, so it's removed...
 			//---		You flick the camera so it comes to rest right at the edge of a new cell, creating the visualization.
 			//---		Then inside the same update loop, you do a mouse press which, probably due to numerical error, 
 			//---		takes the new cell out of view. Currently, in the state machine implementation, that mouse press
 			//---		forces a refresh of the cell buffer in the same update loop, right after the refresh that caused the 
 			//---		visualization to get created. Because this manager lazily adds/removes cells, the cell didn't have a
 			//---		chance to get a parent.
 			//smU_Debug.ASSERT(false, "destroyVisualization1....bad parent: " + visualCell.getParent());
 		}
 		
 		visualCell.setVisible(false);
 		destroyVisualCell(visualCell);
 	}
 	
 	public void cleanPool()
 	{
 		if( !m_poolNeedsCleaning )  return;
 		
 		m_pool.cleanCode();
 		
 		m_poolNeedsCleaning = false;
 	}
 	
 	private void destroyVisualCell(VisualCell cell)
 	{
		if( cell.getCodeSafetyLevel() != null && cell.getCodeSafetyLevel().isStatic() )
 		{
 			m_poolNeedsCleaning = true;
 		}
 		
 		cell.onDestroy();
 		
 		if( cell.getParent() != null )
 		{
 			U_Debug.ASSERT(cell.getParent() == m_cellContainer, "processRemovals1");
 			
 			//--- DRK > No real need to remove from parent I think.
 			//---		It's just invisible, and will probably be added back eventually anyway.
 			//cell.removeFromParent();
 		}
 		
 		m_pool.deallocate(cell);
 	}
 	
 	private void flushDestroyQueue(int destroyLimit)
 	{
 		int i = 0;
 		
 		for( i = m_queueIndex; i < m_queuedRemovals.size() && i < m_queueIndex + destroyLimit; i++ )
 		{
 			VisualCell ithCell = m_queuedRemovals.get(i);
 			m_queuedRemovals.set(i, null);
 			
 			destroyVisualCell(ithCell);
 		}
 		
 		if( i == m_queuedRemovals.size() )
 		{
 			m_queueIndex = 0;
 			m_queuedRemovals.clear();
 		}
 		else
 		{
 			m_queueIndex = i;
 		}
 	}
 }
