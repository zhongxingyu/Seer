 package swarm.client.view.cell;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.S_Client;
 import swarm.client.entities.Camera;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.ClientGrid;
 import swarm.client.entities.ClientGrid.Obscured;
 import swarm.client.managers.CellBuffer;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.states.StateMachine_Base;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_ViewingCell_Refresh;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.I_UIElement;
 import swarm.client.view.U_Css;
 import swarm.client.view.ViewContext;
 import swarm.client.view.cell.VisualCell.E_MetaState;
 import swarm.client.view.dialog.Dialog;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.entities.U_Grid;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.BitArray;
 import swarm.shared.structs.CellAddress;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.utils.U_Bits;
 
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.client.ui.Panel;
 
 /**
  * ...
  * @author 
  */
 public class VisualCellManager implements I_UIElement
 {
 	private static final double NO_SCALING = .99999;
 	private static final Logger s_logger = Logger.getLogger(VisualCellManager.class.getName());
 	
 	private static final double FLUSH_CODE_RATE = .1;
 	
 	private Panel m_container = null;
 	
 	private final Point m_utilPoint1 = new Point();
 	private final Point m_utilPoint2 = new Point();
 	private final Point m_utilPoint3 = new Point();
 	
 	private final Point m_lastBasePoint = new Point();
 	private double m_lastScaling = 0;
 	private double m_lastMetaCellWidth = 0.0;
 	private double m_lastMetaCellHeight = 0.0;
 	
 	private final Dialog m_alertDialog;
 	
 	private final ViewContext m_viewContext;
 	
 	private final VisualCellPool m_cellPool;
 	
 	private final int m_cellDestroyLimitWhileMoving = 1;
 	
 	private CanvasBacking m_backing = null;
 	
 	private final ClientGrid.Obscured m_obscured = new ClientGrid.Obscured();
 	
 	private final CanvasBacking.UpdateConfig m_backingConfig = new CanvasBacking.UpdateConfig();
 	private boolean m_needToUpdateBacking = false;
 	
 	private double m_flushCodeTimer = 0.0;
 	
 	public VisualCellManager(ViewContext viewContext, Panel container) 
 	{
 		m_container = container;
 		m_viewContext = viewContext;
 		m_cellPool = new VisualCellPool(viewContext.appContext.cellSandbox, m_container, m_viewContext.spinnerFactory, viewContext);
 		
 		m_viewContext.appContext.cellBufferMngr.getCellPool().setDelegate(m_cellPool);
 
 		m_alertDialog = new Dialog(m_viewContext.clickMngr, 256, 164, new Dialog.I_Delegate()
 		{
 			@Override
 			public void onOkPressed()
 			{
 				BufferCell bufferCell = getCurrentBufferCell();
 				VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
 				visualCell.getBlocker().setContent(null);
 				
 				VisualCellManager.this.m_viewContext.alertMngr.onHandled();
 			}
 		});
 		
 		m_viewContext.alertMngr.setDelegate(new AlertManager.I_Delegate()
 		{
 			@Override
 			public void showAlert(String message)
 			{
 				BufferCell bufferCell = getCurrentBufferCell();
 				
 				if( bufferCell != null )
 				{
 					CellAddress address = bufferCell.getAddress();
 					String title = "Cell says...";
 					
 					if( message.length() > S_Client.MAX_ALERT_CHARACTERS )
 					{
 						message = message.substring(0, S_Client.MAX_ALERT_CHARACTERS);
 						message += "...";
 					}
 					
 					SafeHtml safeHtml = SafeHtmlUtils.fromString(message);
 					m_alertDialog.setTitle(title);
 					m_alertDialog.setBodySafeHtml(safeHtml);
 					VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
 					visualCell.getBlocker().setContent(m_alertDialog);
 				}
 				else
 				{
 					U_Debug.ASSERT(false, "Expected current cell to be set.");
 				}
 			}
 		});
 	}
 	
 	private void initBacking(final ClientGrid grid)
 	{
 		if( m_backing == null )
 		{
 			SpritePlateAnimation animation = new SpritePlateAnimation
 			(
 				m_viewContext.config.spinnerAnimation,
 				m_viewContext.config.spinnerAnimationFramerate,
 				m_viewContext.config.spinnerAnimationFrameCount,
 				m_viewContext.config.spinnerAnimationFramesAcross
 			);
 			
 //			String color = "rgb(255,0,0)";
 			String color = "rgb(255,255,255)";
 			
 			//--- DRK > Max dimensions could actually be a bit smaller cause of padding...eh.
 			int maxCellWidth = grid.getCellWidth()/2;
 			int maxCellHeight = grid.getCellHeight()/2;
 			
 			m_backing = new CanvasBacking(animation, color, maxCellWidth, maxCellHeight, new CanvasBacking.I_Skipper()
 			{
 				@Override public int skip(int m, int n)
 				{
 					CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 					if( grid.isObscured(m, n, 1, cellManager.getSubCellCount(), m_obscured) )
 					{
 						CellBuffer cellBuffer = cellManager.getDisplayBuffer(U_Bits.calcBitPosition(m_obscured.subCellDimension));
 						BufferCell cell = cellBuffer.getCellAtAbsoluteCoord(m_obscured.m, m_obscured.n);
 						VisualCell visualCell = (VisualCell) cell.getVisualization();
 						E_MetaState state = visualCell.getMetaState();
 						
 						//function nkb(a,b,c){var d,e,f,g,i;f=a.b.t.d.e;if(xc(a.c,b,c,1,f.e,a.b.p)){e=s0(f,bCb(a.b.p.e));d=g0(e,a.b.p.b,a.b.p.c);i=d.i;g=yhb(i);if(g==(dib(),$hb)){return a.b.p.d}}else{if(zc(a.c,b,c,1)){return 2}}return 0}
 						
 //						s_logger.severe(state+"");
 						
 						if( state == VisualCell.E_MetaState.DEFINITELY_SHOULD_BE_RENDERED_BY_NOW )
 						{
 							return m_obscured.offset;
 						}
 					}
 					else
 					{
 						if( grid.isTaken(m, n, 1) )
 						{
 							return 2;
 						}
 					}
 					
 					return 0;
 				}
 			});
 			
 			m_backing.getCanvas().getElement().getStyle().setPosition(Position.ABSOLUTE);
 			m_backing.getCanvas().getElement().getStyle().setLeft(0, Unit.PX);
 			m_backing.getCanvas().getElement().getStyle().setTop(0, Unit.PX);
 			m_backing.getCanvas().getElement().getStyle().setProperty("transformOrigin", "0px 0px 0px");
 			m_backing.getCanvas().addStyleName("sm_canvas_backing");
 			E_ZIndex.CELL_BACKING.assignTo(m_backing.getCanvas());
 			resizeBacking();
 			
 //			E_ZIndex.CELL_BACKING.assignTo(m_backing.getCanvas());
 		}
 	}
 	
 	private void resizeBacking()
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		m_backing.onResize((int)Math.ceil(camera.getViewWidth()), (int)Math.ceil(camera.getViewHeight()));
 	}
 	
 	private void updateCanvasBacking()
 	{
 		if( m_backing == null )  return;
 		
 		if( m_needToUpdateBacking )
 		{
 			m_backing.getCanvas().setVisible(true);
 			m_backing.update(m_backingConfig);
 		}
 		else
 		{
 			m_backing.getCanvas().setVisible(false);
 		}
 	}
 	
 	private void updateCellsWithNoTransforms(double timeStep)
 	{
 		m_needToUpdateBacking = false;
 		
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		
 		for( int i = 0; i < cellManager.getBufferCount(); i++ )
 		{
 			CellBuffer cellBuffer = cellManager.getDisplayBuffer(i);
 			
 			updateCellsWithNoTransforms(cellBuffer, timeStep);
 		}
 		
 		updateCanvasBacking();
 	}
 	
 	private void updateCellsWithNoTransforms(CellBuffer cellBuffer, double timeStep)
 	{
 		int bufferSize = cellBuffer.getCellCount();
 		
 		boolean keepTryingToFlush = initFlushingRoundAndKeepFlushing(cellBuffer.getSubCellCount());
 		
 		int i = bufferSize%2 == 0 ? bufferSize/2-1 : bufferSize/2;
 		
 		for ( int offset = 0; i >= 0 && i < bufferSize; offset=offset<=0?-offset+1:-offset-1, i+=offset )
 		{
 			BufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
 			
 			if( ithBufferCell == null ) continue;
 			
 			VisualCell ithVisualCell = (VisualCell) ithBufferCell.getVisualization();
 			
 			if( ithVisualCell == null )  continue;
 			
 			if( keepTryingToFlush && ithVisualCell.flushQueuedCode() )
 			{
 				keepTryingToFlush = false;
 				m_flushCodeTimer = 0.0;
 			}
 			
 			if( cellBuffer.getSubCellCount() > 1 && ithVisualCell.getMetaState() != VisualCell.E_MetaState.DEFINITELY_SHOULD_BE_RENDERED_BY_NOW )
 			{
 				m_needToUpdateBacking = true;
 			}
 			
 			ithVisualCell.update(timeStep);
 		}
 	}
 	
 	private void updateCellTransforms(double timeStep)
 	{
 		this.updateCellTransforms(timeStep, false);
 	}
 	
 	private void updateCellTransforms(double timeStep, boolean isViewStateTransition)
 	{
 		m_needToUpdateBacking = false;
 		
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Get grid from somewhere else.
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		CellBuffer cellBuffer_highest = cellManager.getHighestDisplayBuffer();
 		
 		int subCellCount_highest = cellManager.getSubCellCount();
 		double distanceRatio = camera.calcDistanceRatio();
 		
 		Point basePoint_highest = m_utilPoint1;
 		cellBuffer_highest.getCoordinate().calcPoint(basePoint_highest, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), subCellCount_highest);
 		camera.calcScreenPoint(basePoint_highest, basePoint_highest);
 		Point basePoint_highest_rounded = m_utilPoint3;
 		basePoint_highest_rounded.copy(basePoint_highest);
 		basePoint_highest_rounded.round();
 		m_lastBasePoint.copy(basePoint_highest_rounded);
 		
 		if( subCellCount_highest > 1 )
 		{
 			BitArray ownership = grid.getBaseOwnership();
 			
 			if( m_backing == null && ownership != null )
 			{
 				initBacking(grid);
 				
 				m_container.add(m_backing.getCanvas());
 			}
 			
 			if( m_backing != null )
 			{
 				m_backing.getCanvas().setVisible(true);
 			}
 		}
 		else
 		{
 			if( m_backing != null )
 			{
 				m_backing.getCanvas().setVisible(false);
 			}
 		}
 		
 		double positionScaling = U_Grid.calcCellScaling(distanceRatio, subCellCount_highest, grid.getCellPadding(), grid.getCellWidth());
 		double cellWidthPlusPadding = -1;
 		double cellHeightPlusPadding = -1;
 		
 		if( subCellCount_highest == 1 )
 		{
 			cellWidthPlusPadding = (grid.getCellWidth() + grid.getCellPadding()) * positionScaling;
 			cellHeightPlusPadding = (grid.getCellHeight() + grid.getCellPadding()) * positionScaling;
 		}
 		else
 		{
 			//--- DRK > We have to fudge the scaling here so that thick artifact lines don't appear between the meta-cell images at some zoom levels.
 			//---		Basically just rounding things to the nearest pixel so that the browser doesn't just decide on its own how it should look at sub-pixels.
 			cellWidthPlusPadding = (grid.getCellWidth()) * positionScaling;
 			cellWidthPlusPadding = Math.round(cellWidthPlusPadding);
 			
 			cellHeightPlusPadding = grid.getCellHeight() * positionScaling;
 			cellHeightPlusPadding = Math.round(cellHeightPlusPadding);
 			
 			positionScaling = cellWidthPlusPadding / grid.getCellWidth();
 		}
 		
 		int limit = cellManager.getBufferCount();
 		
 		for( int i = 0; i < limit; i++ )
 		{
 			CellBuffer cellBuffer = cellManager.getDisplayBuffer(i);
 			
 			if( cellBuffer.getSubCellCount() > 1 && cellBuffer.getCellCount() == 0 )  continue;
 			
 			updateCellTransforms
 			(
 				cellManager, cellBuffer, timeStep, isViewStateTransition,
 				basePoint_highest_rounded, basePoint_highest, cellWidthPlusPadding, cellHeightPlusPadding, positionScaling
 			);
 		}
 		
 		updateCanvasBacking();
 	}
 	
 	private void updateCellTransforms
 		(
 			CellBufferManager manager, CellBuffer cellBuffer_i, double timeStep, boolean isViewStateTransition,
 			Point basePoint_highest_rounded, Point basePoint_highest, double cellWidthPlusPadding, double cellHeightPlusPadding, double positionScaling
 		)
 	{
 		int subCellCount_i = cellBuffer_i.getSubCellCount();
 		int bufferSize = cellBuffer_i.getCellCount();
 		
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Get grid from somewhere else.
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		CellBuffer cellBuffer_highest = cellManager.getHighestDisplayBuffer();
 		
 		int subCellCount_highest = cellManager.getSubCellCount();
 		
 		double distanceRatio = camera.calcDistanceRatio();
 		double sizeScaling = U_Grid.calcCellScaling(distanceRatio, subCellCount_i, grid.getCellPadding(), grid.getCellWidth());
 		
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEntered(State_ViewingCell.class);
 		boolean isViewingCell = viewingState != null;
 		BufferCell viewedCell = viewingState != null ? viewingState.getCell() : null;
 		boolean use3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
 		int scrollX = m_viewContext.scrollNavigator.getScrollX();
 		int scrollY = m_viewContext.scrollNavigator.getScrollY();
 		int windowWidth = (int) m_viewContext.scrollNavigator.getWindowWidth();
 		int windowHeight = (int) m_viewContext.scrollNavigator.getWindowHeight();
 	
 		String scaleProperty = null;
 		double cellWidthForMeta = 0.0, cellHeightForMeta = 0.0;
 		if( subCellCount_i == 1 )
 		{
 			scaleProperty = sizeScaling < NO_SCALING || sizeScaling > 1-NO_SCALING ? U_Css.createScaleTransform(sizeScaling, use3dTransforms) : null;
 		}
 		else if( subCellCount_i > 1 )
 		{
 			cellWidthForMeta = (grid.getCellWidth())*sizeScaling;
 			cellHeightForMeta = (grid.getCellHeight())*sizeScaling;
 		}
 		
 		String transformProperty = m_viewContext.appContext.platformInfo.getTransformProperty();
 		
 		int subCellCount_highest_div = subCellCount_highest / subCellCount_i;
 		double cellWidth_div = cellWidthPlusPadding / ((double)subCellCount_highest_div);
 		double cellHeight_div = cellHeightPlusPadding / ((double)subCellCount_highest_div);
 		int coordMOfHighest = cellBuffer_highest.getCoordinate().getM();
 		int coordNOfHighest = cellBuffer_highest.getCoordinate().getN();
 		coordMOfHighest *= subCellCount_highest_div;
 		coordNOfHighest *= subCellCount_highest_div;
 		
 		if( subCellCount_i == 1 && m_backing != null && m_backing.getCanvas().isVisible() )
 		{
 			double startX_meta = basePoint_highest_rounded.getX();
 			double startY_meta = basePoint_highest_rounded.getY();
 			GridCoordinate coord = cellBuffer_i.getCoordinate();
 			double scaledCellWidth = cellWidth_div - grid.getCellPadding()*sizeScaling;
 			double scaledCellWidthPlusPadding = cellWidth_div;
 //			double scaledCellWidth = ((double)grid.getCellWidth())*sizeScaling;
 //			double scaledCellWidthPlusPadding = ((double)grid.getCellWidth()+grid.getCellPadding())*sizeScaling;
 			
 			m_backingConfig.set
 			(
 				startX_meta, startY_meta, coord.getM(), coord.getN(), cellBuffer_i.getWidth(), cellBuffer_i.getHeight(),
 				scaledCellWidth, scaledCellWidthPlusPadding, grid.getWidth(), grid.getBaseOwnership(),
 				cellWidthPlusPadding, subCellCount_highest, coordMOfHighest, coordNOfHighest, sizeScaling, timeStep
 			);
 		}
 		
 		Point basePoint = null;
 		if( subCellCount_i > subCellCount_highest )
 		{
 			basePoint = m_utilPoint2;
 			cellBuffer_i.getCoordinate().calcPoint(basePoint, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), subCellCount_i);
 			camera.calcScreenPoint(basePoint, basePoint);
 			basePoint.round();
 			int sizeMultiplier = subCellCount_i / subCellCount_highest;
 			
 			cellWidthPlusPadding *= sizeMultiplier;
 			cellHeightPlusPadding *= sizeMultiplier;
 		}
 		else
 		{
 			basePoint = basePoint_highest_rounded;
 		}
 
 		boolean keepTryingToFlush = initFlushingRoundAndKeepFlushing(subCellCount_i);
 		
 //		s_logger.severe(subCellCount_buffer + " " + subCellCount_highest + " " +cellWidthPlusPadding + " " + cellWidth_div);
 		
 		//--- DRK > NOTE: ALL DOM-manipulation related to cells should occur within this block.
 		//---		This might be an extraneous optimization in some browsers if they themselves have
 		//---		intelligent DOM-change batching, but we must assume that most browsers are retarded.
 		//--	NOTE: Well, not ALL manipulation is in here, there are a few odds and ends done outside this...but most should be here.
 		m_container.getElement().getStyle().setDisplay(Display.NONE);
 		{
 			int i = bufferSize%2 == 0 ? bufferSize/2-1 : bufferSize/2;
 			
 			for ( int offset = 0; i >= 0 && i < bufferSize; offset=offset<=0?-offset+1:-offset-1, i+=offset )
 			{
 //				s_logger.severe(i+" " + offset);
 				
 				BufferCell ithBufferCell = cellBuffer_i.getCellAtIndex(i);
 				
 				if( ithBufferCell == null )  continue;
 				
 				VisualCell ithVisualCell = (VisualCell) ithBufferCell.getVisualization();
 				
 				if( keepTryingToFlush && ithVisualCell.flushQueuedCode() )
 				{
 					keepTryingToFlush = false;
 					m_flushCodeTimer = 0.0;
 				}
 				
 				if( subCellCount_i > 1 && ithVisualCell.getMetaState() != VisualCell.E_MetaState.DEFINITELY_SHOULD_BE_RENDERED_BY_NOW )
 				{
 					m_needToUpdateBacking = true;
 				}
 				
 				double offsetX, offsetY;
 				
 				if( subCellCount_i <= subCellCount_highest )
 				{
 					int offset_m = ithBufferCell.getCoordinate().getM() - coordMOfHighest;
 					int offset_n = ithBufferCell.getCoordinate().getN() - coordNOfHighest;
 					int offset_m_mod = offset_m % subCellCount_highest_div;
 					int offset_n_mod = offset_n % subCellCount_highest_div;
 					
 					offsetX = (((offset_m-offset_m_mod)/subCellCount_highest_div) * (cellWidthPlusPadding));
 					offsetY = (((offset_n-offset_n_mod)/subCellCount_highest_div) * (cellHeightPlusPadding));
 					offsetX += offset_m_mod * cellWidth_div;
 					offsetY += offset_n_mod * cellHeight_div;
 					
 					if( subCellCount_i > 1 )
 					{
 						ithVisualCell.setDefaultZIndex();
 					}
 				}
 				else
 				{
 					int offset_m = ithBufferCell.getCoordinate().getM() - cellBuffer_i.getCoordinate().getM();
 					int offset_n = ithBufferCell.getCoordinate().getN() - cellBuffer_i.getCoordinate().getN();
 					
 					offsetX = cellWidthPlusPadding * offset_m;
 					offsetY = cellHeightPlusPadding * offset_n;
 					
 					//--- DRK > Should always be true.
 					if( subCellCount_i > 1 )
 					{
 						//TODO: Probably has to be different logic if we're snapping.
 						if( subCellCount_highest == 1 )
 						{
 							ithVisualCell.setZIndex(E_ZIndex.CELL_META_ON_DEATH_ROW_ABOVE_CELL_1.get());
 						}
 						else
 						{
 							ithVisualCell.setDefaultZIndex();
 						}
 					}
 				}
 				
 				ithVisualCell.update(timeStep);
 				ithVisualCell.validate();
 				
 				if( !isViewingCell || isViewingCell && ithBufferCell != viewedCell )
 				{
 					offsetX += ((double)ithVisualCell.getXOffset())*positionScaling;
 					offsetY += ((double)ithVisualCell.getYOffset())*positionScaling;
 				}
 				else
 				{
 					//--- DRK > At this point in swarm's life, we should only be hitting this block 
 					//---		on window resizes and only resizes. For some reason the scroll has to
 					//---		be included here...it's not apparent why, but resetting the translation
 					//---		removes the window's scroll. Happens in all browsers, so not a "bug",
 					//---		but requires this "workaround" kind of as if it were a bug.
 					offsetX += ((double)ithVisualCell.getStartingXOffset())*positionScaling;
 					offsetY += ((double)ithVisualCell.getStartingYOffset())*positionScaling;
 					
 					//--- DRK > Really not sure why we have to change translation to account for scroll on window resizes.
 					//---		Doesn't make sense, but every browser behaves the same.
 					offsetX += scrollX;
 					offsetY += scrollY;
 				}
 				
 				double translateX = basePoint.getX() + offsetX;
 				double translateY = basePoint.getY() + offsetY;
 				
 				if( isViewingCell )
 				{
 					if( ithBufferCell != viewedCell )
 					{
 						//--- DRK > Need to crop the cell because it has a fixed position and will
 						//---		appear under the scroll bar but still capture the mouse through
 						//---		the scrollbar...happens on all browsers, so I guess not a "bug" per se
 						//---		but still needs this sloppy workaround. IE will in fact show the cell
 						//---		over the scroll bar...so at least it's more honest than other browsers.
 						ithVisualCell.crop((int)translateX, (int)translateY, windowWidth, windowHeight);
 						
 						//--- DRK > In a way we only should need to do this once when target cell becomes focused,
 						//---		but we're doing it for all cells everytime because it's a lightweight operation
 						//---		and passive for if new cells are created on a window resize.
 						ithVisualCell.setScrollMode(E_ScrollMode.SCROLLING_NOT_FOCUSED);
 					}
 					else
 					{
 						ithVisualCell.removeCrop();
 						ithVisualCell.setScrollMode(E_ScrollMode.SCROLLING_FOCUSED);
 					}
 				}
 				else if( !isViewingCell && isViewStateTransition )
 				{
 					//--- DRK > Removing the crop when exiting the viewing cell state.
 					ithVisualCell.removeCrop();
 					ithVisualCell.setScrollMode(E_ScrollMode.NOT_SCROLLING);
 				}
 				
 				String translateProperty = U_Css.createTranslateTransform(translateX, translateY, use3dTransforms);
 				String transform = scaleProperty != null ? translateProperty + " " + scaleProperty : translateProperty;
 				ithVisualCell.getElement().getStyle().setProperty(transformProperty, transform);
 				
				//--- DRK > Not using css scaling here cause Chrome (maybe others) seems to cache the image rendered
				//---		assuming the non-scaled element size (using css width and height) and then stretch it up, instead
				//---		of rendering it properly taking into account scaling. *Eventually* Chrome catches up and rerenders
				//---		the image properly, but it takes a while.
 				if( subCellCount_i > 1 )
 				{
 					ithVisualCell.setSize(cellWidthForMeta+"px", cellHeightForMeta+"px");
 				}
 				
 //				s_logger.severe(transform+"");
 				
 				if( ithVisualCell.getParent() == null )
 				{
 					m_container.add(ithVisualCell);
 				}
 			}
 		}
 		m_container.getElement().getStyle().setDisplay(Display.BLOCK);
 		
 		if( subCellCount_i == 1 )
 		{
 			m_lastScaling = sizeScaling;
 			m_lastMetaCellWidth = cellWidthPlusPadding;
 			m_lastMetaCellHeight = cellHeightPlusPadding;
 		}
 	}
 	
 	private boolean initFlushingRoundAndKeepFlushing(int subCellCount)
 	{
 		if( subCellCount > 1 )  return true;
 		
 		BufferCell snappingOrFocusedCell = null;
 		
 		State_ViewingCell viewingState = m_viewContext.stateContext.get(State_ViewingCell.class);
 		
 		if( viewingState != null )
 		{
 			snappingOrFocusedCell = viewingState.getCell();
 		}
 		else
 		{
 			State_CameraSnapping snappingState = m_viewContext.stateContext.get(State_CameraSnapping.class);
 			
 			if( snappingState != null )
 			{
 				snappingOrFocusedCell = snappingState.getCell();
 			}
 		}
 		
 		if( snappingOrFocusedCell != null )
 		{
 			VisualCell visualCell = (VisualCell) snappingOrFocusedCell.getVisualization();
 			
 			if( visualCell != null )
 			{
 				if( visualCell.flushQueuedCode() )
 				{
 					m_flushCodeTimer = 0.0;
 					
 					return false;
 				}
 			}
 		}
 		
 		if( m_flushCodeTimer < FLUSH_CODE_RATE )
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public double getLastScaling()
 	{
 		return m_lastScaling;
 	}
 	
 	public double getLastMetaCellWidth()
 	{
 		return m_lastMetaCellWidth;
 	}
 	
 	public double getLastMetaCellHeight()
 	{
 		return m_lastMetaCellHeight;
 	}
 	
 	public Point getLastBasePoint()
 	{
 		return m_lastBasePoint;
 	}
 	
 	@Override public void onStateEvent(StateEvent event)
 	{
 		switch( event.getType() )
 		{
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					double timestep = event.getState().getLastTimeStep();
 					m_flushCodeTimer += timestep;
 					
 					//boolean flushDestroyQueueIfMoving = (event.getState().getUpdateCount() % 8) == 0;
 					
 //					s_logger.severe(""+m_viewContext.appContext.cameraMngr.getAtRestFrameCount());
 					
 					if( m_viewContext.appContext.cameraMngr.getAtRestFrameCount() > 2 )
 					{
 						m_cellPool.cleanPool();
 						
 						//--- DRK > This first condition ensures that we're still updating cell positions as they're potentially shrinking
 						//---		after exiting viewing or snapping state. There's probably a more efficient way to determine if they're actually shrinking.
 						//---		This is just a catch-all for now.
 						if( (event.getState().getPreviousState() == State_ViewingCell.class || event.getState().getPreviousState() == State_CameraSnapping.class) &&
 							event.getState().getTotalTimeInState() <= m_viewContext.config.cellSizeChangeTime_seconds )
 						{
 //							s_logger.severe("updateCellTransforms >=2");
 							
 							this.updateCellTransforms(timestep);
 						}
 						else
 						{
 //							s_logger.severe("updateCellsWithNoTransforms >=2");
 							
 							this.updateCellsWithNoTransforms(timestep);
 						}
 					}
 					else if( m_viewContext.appContext.cameraMngr.getAtRestFrameCount() < 2 )
 					{
 //						s_logger.severe("updateCellTransforms <2");
 						
 						this.updateCellTransforms(timestep);
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					this.updateCellTransforms(0.0, true);
 				}
 				
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					this.updateCellTransforms(0.0, true);
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Action_Camera_SetViewSize.class )
 				{
 					Action_Camera_SetViewSize.Args args = event.getActionArgs();
 					
 					if( args.updateBuffer() )
 					{
 //						s_logger.severe("onResize updateBuffer()==true");
 						
 						if( m_backing != null )
 						{
 							resizeBacking();
 						}
 						
 						this.updateCellTransforms(0.0);
 					}
 //					else
 //					{
 //						s_logger.severe("onResize updateBuffer()==false");
 //					}
 				}
 				else if( event.getAction() == StateMachine_Base.OnGridUpdate.class )
 				{					
 					//--- DRK > In some sense this is a waste of an update since in just a few milliseconds
 					//---		we'll be doing another one anyway. If we don't do it here though, there's some 
 					//---		lagging of the cell visualizations with very rapid resizes of the window.
 					//---		Also, if the camera is still and the itself gets grid updated, visual cells won't appear until
 					//---		we move the camera again.
 					ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid();
 					initBacking(grid);
 					this.updateCellTransforms(0.0);
 				}
 				else if( event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					Action_Camera_SnapToPoint.Args args = event.getActionArgs();
 					
 					//--- DRK(TODO): Don't really like this null check here...necessary because of legacy
 					//---			 code using null snap args to simply change to floating state.
 					if( args == null || args.isInstant() )
 					{
 //						s_logger.severe("SnapToPoint");;
 						
 						this.updateCellTransforms(0.0);
 					}
 				}
 				else if( event.getAction() == Action_ViewingCell_Refresh.class )
 				{
 					//--- DRK > Used to clear alerts here, but moved it to the actual refresh button handler.
 					//--- 		Probably safe here, but refresh might instantly update the cell's code, before
 					//---		we get a chance to remove the alerts...might be order-dependent strangeness there.
 					//clearAlerts();
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	public void onMetaCellLoaded()
 	{
 	}
 	
 	public void onMetaCellRendered()
 	{
 	}
 	
 	private BufferCell getCurrentBufferCell()
 	{
 		State_ViewingCell state = m_viewContext.stateContext.getEntered(State_ViewingCell.class);
 		if( state != null )
 		{
 			return state.getCell();
 		}
 		
 		return null;
 	}
 	
 	public void clearAlerts()
 	{
 		m_viewContext.alertMngr.clear();
 		
 		BufferCell bufferCell = getCurrentBufferCell();
 		
 		if( bufferCell != null )
 		{
 			VisualCell cell = (VisualCell) bufferCell.getVisualization();
 			cell.getBlocker().setContent(null);
 		}
 		else
 		{
 			//--- DRK > Below assert trips badly when exiting view state...state is already
 			//---		exited when we get to here.
 			//smU_Debug.ASSERT(false, "Expected current cell to be set.");
 		}
 	}
 }
