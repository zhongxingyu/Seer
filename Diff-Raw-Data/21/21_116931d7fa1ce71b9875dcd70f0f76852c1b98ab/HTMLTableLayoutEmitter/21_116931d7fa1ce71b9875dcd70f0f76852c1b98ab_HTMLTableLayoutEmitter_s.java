 /***********************************************************************
  * Copyright (c) 2004,2007 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Actuate Corporation - initial API and implementation
  ***********************************************************************/
 
 package org.eclipse.birt.report.engine.layout.html;
 
 import java.util.Iterator;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.report.engine.content.ICellContent;
 import org.eclipse.birt.report.engine.content.IContent;
 import org.eclipse.birt.report.engine.content.IReportContent;
 import org.eclipse.birt.report.engine.content.IRowContent;
 import org.eclipse.birt.report.engine.content.ITableContent;
 import org.eclipse.birt.report.engine.emitter.BufferedReportEmitter;
 import org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter;
 import org.eclipse.birt.report.engine.emitter.ContentEmitterUtil;
 import org.eclipse.birt.report.engine.emitter.IContentEmitter;
 import org.eclipse.birt.report.engine.emitter.IEmitterServices;
 import org.eclipse.birt.report.engine.executor.buffermgr.Cell;
 import org.eclipse.birt.report.engine.executor.buffermgr.Row;
 import org.eclipse.birt.report.engine.executor.buffermgr.TableContentLayout;
 import org.eclipse.birt.report.engine.internal.content.wrap.CellContentWrapper;
 import org.eclipse.birt.report.engine.ir.EngineIRConstants;
 
 abstract public class HTMLTableLayoutEmitter extends ContentEmitterAdapter
 {
 
 	final static Logger logger = Logger.getLogger( HTMLTableLayoutEmitter.class
 			.getName( ) );
 
 	/**
 	 * the emitter used to output the table content
 	 */
 	protected IContentEmitter emitter;
 
 	/**
 	 * the current table layout
 	 */
 	protected TableContentLayout layout;
 
 	/**
 	 * the cached start/end content events
 	 */
 	protected Stack layoutEvents;
 
 	/**
 	 * emitter used to cache the content in current cell.
 	 */
 	protected IContentEmitter cellEmitter;
 	
 	protected HTMLLayoutContext context; 
 
 
 	/**
 	 * the group level information used to resovle the drop cells.
 	 */
 	protected Stack groupStack = new Stack( );
 
 	public HTMLTableLayoutEmitter( IContentEmitter emitter,HTMLLayoutContext context  )
 	{
 		this.emitter = emitter;
 		this.context = context;
 	}
 	
 	public void end( IReportContent report )
 	{
 		emitter.end( report );
 	}
 
 	public String getOutputFormat( )
 	{
 		return emitter.getOutputFormat( );
 	}
 
 	public void initialize( IEmitterServices service )
 	{
 		emitter.initialize( service );
 	}
 
 	public void start( IReportContent report )
 	{
 		emitter.start( report );
 	}
 
 	protected int getGroupLevel( )
 	{
 		if ( !groupStack.isEmpty( ) )
 		{
 			return ( (Integer) groupStack.peek( ) ).intValue( );
 		}
 		return -1;
 	}
 	
 	protected boolean isContentFinished(IContent content)
 	{
 		if(context!=null)
 		{
 				return context.getLayoutHint( content );
 		}
 		return true;
 	}
 	
 	protected boolean allowPageBreak()
 	{
 		if(context!=null)
 		{
 				return context.allowPageBreak( );
 		}
 		return false;
 	}
 
 	public void startContent( IContent content )
 	{
 		if ( cellEmitter != null )
 		{
 			ContentEmitterUtil.startContent( content, cellEmitter );
 		}
 		else
 		{
 			ContentEmitterUtil.startContent( content, emitter );
 		}
 	}
 
 	public void endContent( IContent content )
 	{
 		if ( cellEmitter != null )
 		{
 			ContentEmitterUtil.endContent( content, cellEmitter );
 		}
 		else
 		{
 			ContentEmitterUtil.endContent( content, emitter );
 		}
 	}
 
 	boolean hasDropCell = false;
 
 	public void resetLayout( )
 	{
 		layout.reset( );
 		layoutEvents.clear( );
 		hasDropCell = false;
 	}
 
 	public void initLayout( ITableContent table )
 	{
 		this.layout = new TableContentLayout( table,
				EngineIRConstants.FORMAT_TYPE_VIEWER, context );
 		this.layoutEvents = new Stack( );
 	}
 	
 	public boolean isLayoutStarted()
 	{
 		return layout!=null;
 	}
 
 	protected boolean hasDropCell( )
 	{
 		return hasDropCell;
 	}
 
 	public void createCell( int colId, int rowSpan, int colSpan,
 			Cell.Content cellContent )
 	{
 		layout.createCell( colId, rowSpan, colSpan, cellContent );
 		if ( rowSpan < 0 )
 		{
 			hasDropCell = true;
 		}
 	}
 
 	protected int createDropID( int groupIndex, String dropType )
 	{
 		int dropId = -10 * ( groupIndex + 1 );
 		if ( "all".equals( dropType ) ) //$NON-NLS-1$
 		{
 			dropId--;
 		}
 		return dropId;
 	}
 
 	public void resolveAll(boolean finished )
 	{
 		if ( hasDropCell )
 		{
 			layout.resolveDropCells( finished );
 			hasDropCell = layout.hasDropCell( );
 		}
 	}
 
 	public void resolveCellsOfDrop( int groupLevel, boolean dropAll, boolean finished )
 	{
 		if ( hasDropCell )
 		{
 			if ( dropAll )
 			{
 				layout.resolveDropCells( createDropID( groupLevel, "all" ), finished ); //$NON-NLS-1$
 			}
 			else
 			{
 				layout.resolveDropCells( createDropID( groupLevel, "detail" ), finished ); //$NON-NLS-1$
 			}
 			hasDropCell = layout.hasDropCell( );
 		}
 	}
 	
 	protected static class LayoutEvent
 	{
 		final static int START_GROUP = 0;
 		final static int START_BAND = 1;
 		final static int END_GROUP = 2;
 		final static int END_BAND = 3;
 		final static int ON_ROW = 4;
 		final static int ON_FIRST_DROP_CELL = 5;
 		
 		LayoutEvent(int type, Object value )
 		{
 			this.eventType= type;
 			this.value = value;
 		}
 		int eventType;
 		Object value;
 	}
 	
 	protected static class StartInfo
 	{
 		StartInfo(int rowId, int cellId)
 		{
 			this.rowId = rowId;
 			this.cellId = cellId;
 		}
 		int rowId;
 		int cellId;
 	}
 
 	public static class CellContent implements Cell.Content
 	{
 
 		protected ICellContent cell;
 
 		protected BufferedReportEmitter buffer;
 
 		public CellContent( ICellContent cell, BufferedReportEmitter buffer )
 		{
 			this.cell = cell;
 			this.buffer = buffer;
 
 		}
 
 		public ICellContent getContent()
 		{
 			return cell;
 		}
 		public boolean isEmpty( )
 		{
 			return buffer == null || buffer.isEmpty( );
 		}
 
 		public void reset( )
 		{
 			buffer = null;
 		}
 	}
 	
 	public void flush( )
 	{
 		if ( hasDropCell( ) )
 		{
 			return;
 		}
 		Iterator iter = layoutEvents.iterator( );
 		while ( iter.hasNext( ) )
 		{
 			LayoutEvent event = (LayoutEvent) iter.next( );
 			switch ( event.eventType )
 			{
 				case LayoutEvent.START_GROUP :
 				case LayoutEvent.START_BAND :
 					ContentEmitterUtil.startContent( (IContent) event.value,
 							emitter );
 					break;
 				case LayoutEvent.END_GROUP :
 				case LayoutEvent.END_BAND :
 					ContentEmitterUtil.endContent( (IContent) event.value,
 							emitter );
 					break;
 				case LayoutEvent.ON_ROW :
 					flushRow( ( (StartInfo) event.value ).rowId , 0, true );
 					break;
 				case LayoutEvent.ON_FIRST_DROP_CELL:
 					flushRow( ( (StartInfo) event.value ).rowId,
 							( (StartInfo) event.value ).cellId, false );
 					break;
 			}
 		}
 		resetLayout();
 	}
 	
 	protected void flushRow( int rowId, int colId, boolean withStart )
 	{
 		int colCount = layout.getColCount( );
 		Row row = layout.getRow( rowId );
 		IRowContent rowContent = (IRowContent) row.getContent( );
 		if ( withStart )
 		{
 			emitter.startRow( rowContent );
 		}
 		for ( int j = colId; j < colCount; j++ )
 		{
 			Cell cell = row.getCell( j );
 			if ( cell.getStatus( ) == Cell.CELL_USED )
 			{
 				CellContent content = (CellContent) cell.getContent( );
 				CellContentWrapper tempCell = new CellContentWrapper(
 						content.cell ); 
 				tempCell.setColumn( cell.getColId( ) );
 				tempCell.setRowSpan( cell.getRowSpan( ) );
 				tempCell.setColSpan( cell.getColSpan( ) );
 
 				emitter.startCell( tempCell );
 				if ( content.buffer != null )
 				{
 					content.buffer.flush( );
 				}
 				emitter.endCell( tempCell );
 			}
 			if ( cell.getStatus( ) == Cell.CELL_EMPTY )
 			{
 				IReportContent report = rowContent.getReportContent( );
 				ICellContent cellContent = report.createCellContent( );
 				cellContent.setParent( rowContent );
 				cellContent.setColumn( cell.getColId( ) + 1 );
 				cellContent.setRowSpan( cell.getRowSpan( ) );
 				cellContent.setColSpan( cell.getColSpan( ) );
 				emitter.startCell( cellContent );
 				emitter.endCell( cellContent );
 			}
 		}
 		emitter.endRow( rowContent );
 	}
 
 }
