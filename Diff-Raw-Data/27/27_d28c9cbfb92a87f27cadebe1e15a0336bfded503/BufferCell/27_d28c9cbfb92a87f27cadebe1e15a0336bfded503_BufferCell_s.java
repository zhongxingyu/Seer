 package swarm.client.entities;
 
 import java.util.logging.Logger;
 
 import swarm.client.managers.CellCodeManager;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.utils.U_Math;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Cell;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.entities.E_CodeSafetyLevel;
 import swarm.shared.entities.E_CodeType;
 import swarm.shared.structs.CellAddress;
 import swarm.shared.structs.Code;
 import swarm.shared.structs.Point;
 
 
 /**
  * ...
  * @author 
  */
 public class BufferCell extends A_Cell
 {
 	private static final Logger s_logger = Logger.getLogger(BufferCell.class.getName());
 	
 	private static final Point s_utilPoint = new Point();
 	
 	private I_CellVisualization m_visualization = null;
 	
 	private final E_CodeStatus m_codeStatus[] = new E_CodeStatus[E_CodeType.values().length];
 	
 	private boolean m_isFocused = false;
 	private boolean m_hasBeenPreviewed = false;
 	
 	private CellAddress m_address = null;
 	private A_Grid m_grid = null;
 	
 	private double m_deathCountdown = -1;
 	private double m_totalDeathTime = -1;
 	
 	private boolean m_hasEverSetCodeOnVisualization = false;
 	
 	public BufferCell() 
 	{
 		this.setStatusAll(E_CodeStatus.NEEDS_CODE);
 	}
 	
 	public void init(A_Grid grid)
 	{
 		m_grid = grid;
 	}
 	
 	public A_Grid getGrid()
 	{
 		return m_grid;
 	}
 	
 	public CellAddress getAddress()
 	{
 		return m_address;
 	}
 	
 	public boolean isFocused()
 	{
 		return m_isFocused;
 	}
 	
 	public boolean hasBeenPreviewed()
 	{
 		return m_hasBeenPreviewed;
 	}
 	
 	public void onPreviewSuccess(Code compiled)
 	{
 		if( m_isFocused )
 		{
 			this.setCode(E_CodeType.COMPILED, compiled);
 			
 			m_hasBeenPreviewed = true;
 		}
 		else
 		{
 			U_Debug.ASSERT(false, "smBufferCell::Can't preview cell if it isn't focused.");
 		}
 	}
 	
 	public void onSyncStart(Code source, Code compiled)
 	{
 		this.setCode(E_CodeType.SOURCE, source);
 		this.setCode(E_CodeType.SPLASH, null);
 		this.setCode(E_CodeType.COMPILED, compiled);
 			
 		m_hasBeenPreviewed = false;
 	}
 	
 	public void onSyncResponseSuccess(Code splash, Code compiled_nullable)
 	{
 		this.setCode(E_CodeType.SPLASH, splash);
 		
 		this.setCode_private(E_CodeType.COMPILED, compiled_nullable, false);
 	}
 	
 	public void onSyncResponseError()
 	{
 		m_codeStatus[E_CodeType.COMPILED.ordinal()] = E_CodeStatus.HAS_CODE;
 		m_codeStatus[E_CodeType.SPLASH.ordinal()] = E_CodeStatus.HAS_CODE;
 		
 		if( m_visualization != null )
 		{
 			m_visualization.clearLoading();
 		}
 	}
 	
 	public void onAddressFound(CellAddress address)
 	{
 		m_address = address;
 	}
 	
 	public void onServerRequest(E_CodeType eType)
 	{
 		m_codeStatus[eType.ordinal()] = E_CodeStatus.WAITING_ON_CODE;
 		
 		showLoading_private(eType);
 	}
 	
 	private void showLoading_private(E_CodeType eType)
 	{
 		if( m_visualization == null )  return;
 		
 		if( m_isFocused && eType == E_CodeType.COMPILED )
 		{
 			m_visualization.showLoading();
 		}
 		else if( !m_isFocused && eType == E_CodeType.SPLASH )
 		{
 			m_visualization.showLoading();
 		}
 	}
 	
 	/**
 	 * Cell is nuked by cell populator either when snapping to a cell, or refreshing cell.
 	 * There could be more cases in the future, but the basic idea is to force a refresh of a cell's contents.
 	 */
 	public void nuke(E_CellNuke nukeType)
 	{
 		switch( nukeType )
 		{
 			case EVERYTHING:
 			{
 				for( int i = 0 ; i < m_codeStatus.length; i++ )
 				{
 					this.setCode(E_CodeType.values()[i], null);
 					m_codeStatus[i] = E_CodeStatus.NEEDS_CODE;
 				}
 				
 				break;
 			}
 			
 			case ERRORS_ONLY:
 			{
 				for( int i = 0 ; i < m_codeStatus.length; i++ )
 				{
 					if( m_codeStatus[i] == E_CodeStatus.GET_ERROR )
 					{
 						this.setCode(E_CodeType.values()[i], null);
 						m_codeStatus[i] = E_CodeStatus.NEEDS_CODE;
 					}
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	public void onFocusGained()
 	{
 		m_hasBeenPreviewed = false;
 		m_isFocused = true;
 		
 		if( m_visualization != null )
 		{
 			m_visualization.onFocusGained();
 			
 			E_CodeType type = E_CodeType.COMPILED;
 			E_CodeStatus compiledStatus = getStatus(type);
 			
 			if( compiledStatus == E_CodeStatus.HAS_CODE )
 			{
 				Code code = this.getCode(type);
 				
 				if( code.isStandInFor(E_CodeType.SPLASH) )
 				{
 					//--- DRK > NOTE: In this case, we're assuming that the splash code was already set on the visualization,
 					//---				and that the splash code is identical to the compiled code, so we don't do anything
 					//---				for reasons of optimization.  This may be a bad assumption however...time will tell.
 					//---		UPDATE: Was getting NPE here, fixed upstream issue, but just commenting out for now...too much shit hitting the fan.
 //					U_Debug.ASSERT(this.getCode(E_CodeType.SPLASH).isStandInFor(E_CodeType.COMPILED), "onFocusGained1");
 				}
 				else
 				{
 					setCodeOnVis(code, this.getCellNamespace());
 				}
 			}
 			else if( compiledStatus == E_CodeStatus.GET_ERROR )
 			{
 				m_visualization.onError(type);
 			}
 			else if( compiledStatus == E_CodeStatus.WAITING_ON_CODE )
 			{
 				m_visualization.showLoading();
 			}
 			else
 			{
 				//--- DRK > This block was hit once when looking at the b33hive "terms" cell from afar, signing in, then focusing the cell.
 				//---		It was in local release mode (pretty sure about release), and probably after local cache expired.
 				//---		At the time, a "showLoading" call was made to the visualization instead of an error, and basically prevented
 				//---		the cell from being updated again...I didn't try panning away from the cell and coming back though.
 				//---
 				//---		Anyway, after that, I changed the below to first check if we have splash code that can stand in for compile.
 				//---		At this point, something's still very wrong, because we should also have had compiled code that can stand in for splash.
 				//---		But, at least we try to salvage the UX as much as possible.
 				//---
 				//---	NOTE: This above problem might be because I was an idiot and didn't have a WAITING_ON_CODE case defined above...now there is one.
 				
 				E_CodeStatus splashStatus = this.getStatus(E_CodeType.SPLASH);
 				Code code = this.getCode(E_CodeType.SPLASH);
 				if( splashStatus == E_CodeStatus.HAS_CODE && code != null && code.isStandInFor(E_CodeType.COMPILED))
 				{
 					setCodeOnVis(code, this.getCellNamespace());
 					
 					//--- DRK > This ASSERT and similar one below don't seem to make sense cause the case would have
 					//---		been caught upstream in the if/else.
 //					U_Debug.ASSERT(compiledStatus == E_CodeStatus.WAITING_ON_CODE, "onFocusGained2 " + compiledStatus);
 				}
 				else
 				{
 					m_visualization.onError(type);
 
 //					U_Debug.ASSERT(compiledStatus == E_CodeStatus.WAITING_ON_CODE, "onFocusGained3 " + compiledStatus);
 				}
 			}
 		}
 	}
 	
 	public void onFocusLost()
 	{
 		m_isFocused = false;
 		
 		if( m_visualization != null )
 		{
 			m_visualization.onFocusLost();
 			
 			E_CodeStatus status = getStatus(E_CodeType.SPLASH);
 			if( status == E_CodeStatus.HAS_CODE )
 			{
 				m_visualization.clearLoading(); // in case we're loading compiled code.
 				
 				boolean setCodeVisualization = true;
 				
 				//--- DRK > The following if block is an optimization that causes static-only code to not 
 				//---		get reset on unfocus. Commenting it out for now to err on the side of always resetting a user's
 				//---		cell to the original state if you navigate away from it.  This ensures that scroll
 				//---		bars, interactive elements, tab order, etc. are all put back in their original place.
 				//---		At some point in the future it might be decided that letting cells keep their state
 				//---		as much as possible is a good thing sometimes, but for now, this code will be left commented out.
 				/*if( !m_hasBeenPreviewed )
 				{
 					if( getStatus(smE_CodeType.COMPILED) == smE_CodeStatus.HAS_CODE )
 					{
 						if( getCode(smE_CodeType.COMPILED).isStandInFor(smE_CodeType.SPLASH) )
 						{
 							//--- DRK > NOTE: This is the corollary to the note in onFocusGained.
 							//---				It's an optimization, but could be a bad one.
 							setCodeVisualization = false;
 						}
 					}
 				}*/
 				
 				if( setCodeVisualization )
 				{
 					m_visualization.setCodeAfterFocusLost(this.getCode(E_CodeType.SPLASH), this.getCellNamespace());
 				}
 			}
 			else if( status == E_CodeStatus.WAITING_ON_CODE )
 			{
 				//--- DRK > May already be showing loading...visualization has to account for this
 				//---		if showing loading twice in a row resets an animation or something.
 				m_visualization.showLoading();
 				m_visualization.showEmptyContent();
 			}
 			else
 			{
 				m_visualization.clearLoading(); // in case we're loading compiled code.
 				m_visualization.showEmptyContent();
 			}
 		}
 		
 		m_hasBeenPreviewed = false;
 	}
 	
 	public void onGetResponseError(E_CodeType eCodeType)
 	{
 		this.setCode(eCodeType, null);
 
 		m_codeStatus[eCodeType.ordinal()] = E_CodeStatus.GET_ERROR;
 		
 		if( m_visualization != null )
 		{
 			if( m_isFocused )
 			{
 				if( eCodeType == E_CodeType.COMPILED )
 				{
 					m_visualization.onError(eCodeType);
 				}
 			}
 			else
 			{
 				if( eCodeType == E_CodeType.SPLASH )
 				{
 					m_visualization.onError(eCodeType);
 				}
 			}
 		}
 	}
 	
 	private void setStatusAll(E_CodeStatus eStatus)
 	{
 		for( int i = 0; i < m_codeStatus.length; i++ )
 		{
 			m_codeStatus[i] = eStatus;
 		}
 	}
 	
 	public E_CodeStatus getStatus(E_CodeType eType)
 	{
 		return m_codeStatus[eType.ordinal()];
 	}
 	
 	private String getCellNamespace()
 	{
 		return this.getCoordinate().writeString();
 	}
 	
 	@Override
 	public void setCode(E_CodeType eType, Code code)
 	{
 		this.setCode_private(eType, code, true);
 	}
 	
 	private void setCode_private(E_CodeType eType, Code code, boolean updateVisualization)
 	{
		if( getCoordinate().isEqualTo(16, 14) )
		{
			boolean onlineBuffer = getVisualization() != null;
			String onlineBufferString = onlineBuffer ? "ONLINE" : "OFFLINE";
			
			s_logger.severe("setCode for " + onlineBufferString + " " + eType + " with updateVis=" + updateVisualization + " and isFocused=" + m_isFocused);
		}
 		
 		super.setCode(eType, code);
 		
 		if( code == null )  return;
 		
 		this.m_codeStatus[eType.ordinal()] = E_CodeStatus.HAS_CODE;
 		
 		if( m_visualization != null )
 		{
 			if( m_isFocused )
 			{
 				//--- DRK > If focused, only COMPILED can be set on the visualization.
 				if( eType == E_CodeType.COMPILED )
 				{
 					if( updateVisualization )
 					{
 						setCodeOnVis(code, this.getCellNamespace());
 					}
 				}
 			}
 			else
 			{
 				//--- DRK > If not focused, only SPLASH_SCREEN can be set on the visualization.
 				if( eType == E_CodeType.SPLASH )
 				{
 					if( updateVisualization )
 					{
 						setCodeOnVis(code, this.getCellNamespace());
 					}
 				}
 			}
 		}
 	}
 	
 	private void setCodeOnVis(Code code, String namespace)
 	{
 		m_visualization.setCode(code, namespace);
 		
 		m_hasEverSetCodeOnVisualization = true;
 	}
 	
 	public void setVisualization(I_CellVisualization visualization)
 	{
 		m_visualization = visualization;
 	}
 	
 	public I_CellVisualization getVisualization()
 	{
 		return m_visualization;
 	}
 	
 	public void onServerRequestCancelled(E_CodeType eType)
 	{
 		if( eType == E_CodeType.COMPILED )
 		{
 			m_visualization.clearLoading();
 		}
 		
 		if( this.getCode(eType) != null )
 		{
 			this.m_codeStatus[eType.ordinal()] = E_CodeStatus.HAS_CODE;
 		}
 		else
 		{
 			this.m_codeStatus[eType.ordinal()] = E_CodeStatus.NEEDS_CODE;
 		}
 	}
 	
 	private void clear_private()
 	{
 		super.clearCode();
 		
 		m_hasEverSetCodeOnVisualization = false;
 		
 		this.setStatusAll(E_CodeStatus.NEEDS_CODE);
 		this.m_focusedCellSize.setToDefaults();
 		
 		U_Debug.ASSERT(!m_isFocused, "Cell should not be GCed when focused.");
 		
 		m_isFocused = false;
 		m_address = null;
 		m_hasBeenPreviewed = false;
 
 		clearDeathTimer();
 	}
 	
 	public void onCellDestroyed()
 	{
 		this.clear_private();
 		
 		this.m_visualization = null;
 		this.m_grid = null;
 	}
 	
 	public double getTotalDeathTime()
 	{
 		return m_totalDeathTime;
 	}
 	
 	public double getTimeToDeath()
 	{
 		return m_deathCountdown;
 	}
 	
 	public void sentenceToDeath(double countdown)
 	{
 		m_deathCountdown = countdown;
 		m_totalDeathTime = countdown;
 	}
 	
 	public void saveFromDeathSentence()
 	{
 		clearDeathTimer();
 		
 		getVisualization().onSavedFromDeathSentence();
 	}
 	
 	private void clearDeathTimer()
 	{
 		m_deathCountdown = -1;
 		m_totalDeathTime = -1;
 	}
 	
 	public boolean isOnDeathRow()
 	{
 		return m_deathCountdown >= 0.0;
 	}
 	
 	public void updateDeathRowTimer(double timestep)
 	{
 		m_deathCountdown -= timestep;
 	}
 	
 	public boolean kill()
 	{
 		if( m_deathCountdown <= 0 )
 		{
 			clearDeathTimer();
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 //	public void onCellRecycled(int cellSize)
 //	{
 //		this.clear_private();
 //		
 //		if( this.m_visualization != null )
 //		{
 //			m_visualization.onCellRecycled(m_grid.getCellWidth(), m_grid.getCellHeight(), m_grid.getCellPadding(), cellSize);
 //		}
 //	}
 }
