 /*******************************************************************************
  * Copyright (c) 2004, 2008 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.report.engine.presentation;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.core.archive.IDocArchiveWriter;
 import org.eclipse.birt.core.archive.RAOutputStream;
 import org.eclipse.birt.core.util.IOUtil;
 import org.eclipse.birt.report.engine.api.EngineException;
 import org.eclipse.birt.report.engine.api.IEngineTask;
 import org.eclipse.birt.report.engine.api.IPageHandler;
 import org.eclipse.birt.report.engine.api.IReportDocumentInfo;
 import org.eclipse.birt.report.engine.api.InstanceID;
 import org.eclipse.birt.report.engine.api.impl.EngineTask;
 import org.eclipse.birt.report.engine.api.impl.ReportDocumentConstants;
 import org.eclipse.birt.report.engine.api.impl.ReportDocumentWriter;
 import org.eclipse.birt.report.engine.content.IContent;
 import org.eclipse.birt.report.engine.content.IPageContent;
 import org.eclipse.birt.report.engine.content.IReportContent;
 import org.eclipse.birt.report.engine.content.impl.AbstractContent;
 import org.eclipse.birt.report.engine.emitter.CompositeContentEmitter;
 import org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter;
 import org.eclipse.birt.report.engine.emitter.IContentEmitter;
 import org.eclipse.birt.report.engine.executor.ContextPageBreakHandler;
 import org.eclipse.birt.report.engine.executor.EngineExtensionManager;
 import org.eclipse.birt.report.engine.executor.ExecutionContext;
 import org.eclipse.birt.report.engine.executor.IReportExecutor;
 import org.eclipse.birt.report.engine.executor.OnPageBreakLayoutPageHandle;
 import org.eclipse.birt.report.engine.extension.engine.IContentProcessor;
 import org.eclipse.birt.report.engine.extension.engine.IDocumentExtension;
 import org.eclipse.birt.report.engine.extension.internal.ExtensionManager;
 import org.eclipse.birt.report.engine.internal.document.DocumentExtension;
 import org.eclipse.birt.report.engine.internal.document.IPageHintWriter;
 import org.eclipse.birt.report.engine.internal.document.IReportContentWriter;
 import org.eclipse.birt.report.engine.internal.document.v3.ReportContentWriterV3;
 import org.eclipse.birt.report.engine.internal.document.v4.PageHintWriterV4;
 import org.eclipse.birt.report.engine.internal.presentation.ReportDocumentInfo;
 import org.eclipse.birt.report.engine.ir.ExtendedItemDesign;
 import org.eclipse.birt.report.engine.ir.ListItemDesign;
 import org.eclipse.birt.report.engine.ir.SimpleMasterPageDesign;
 import org.eclipse.birt.report.engine.ir.TableItemDesign;
 import org.eclipse.birt.report.engine.layout.CompositeLayoutPageHandler;
 import org.eclipse.birt.report.engine.layout.ILayoutPageHandler;
 import org.eclipse.birt.report.engine.layout.IReportLayoutEngine;
 import org.eclipse.birt.report.engine.layout.LayoutEngineFactory;
 import org.eclipse.birt.report.engine.layout.html.HTMLLayoutContext;
 
 /**
  * Used in run task. To builder the report document.
  * 
  * In builder, will call IReportLayoutEngine to lay out the report and create
  * the document. This means write page hint, page contents and all body contents
  * to the document. And in each page closing, we will call page handler's
  * onPage() to do some sepcial process, like write the current page's page hint,
  * evaluate the OnpageBreak script, reset the page row count to be 0 in layout
  * engine.
  * 
  * Here are several main fields used in this class: CompositeContentEmitter,
  * composite two emitters: PageEmitter: used to write the the master page
  * content OnPageBreakHandler.PageContentEmitter: used to collect the page mode
  * which is used to call onPageBreak. CompositeLayoutPageHandler, composite
  * three page handler: LayoutPageHandler: used to write the page hint, created
  * by the handler. OnPageBreakLayoutPageHandle: used to call the onPageBreak
  * script, collected by its PageContentEmitter. ContextPageBreakHandler: used to
  * call the onPageBreak of IPageBreakListener to reset the page row count.
  * IContentEmitter: ContentEmitter: used to write the content stream.
  * IPageHandler: used to recevie the document page events, mostly implemented by
  * user.
  * 
  */
 public class ReportDocumentBuilder
 {
 
 	protected static Logger logger = Logger
 			.getLogger( ReportDocumentBuilder.class.getName( ) );
 
 	/**
 	 * execution context used to execute the report
 	 */
 	protected ExecutionContext executionContext;
 	/**
 	 * current page number
 	 */
 	protected long pageNumber;
 	/**
 	 * the offset of the page content
 	 */
 	protected long pageOffset;
 
 	/**
 	 * report document used to save the informations.
 	 */
 	protected ReportDocumentWriter document;
 	/**
 	 * used to write the content stream
 	 */
 	protected CompositeContentEmitter contentEmitter;
 	/**
 	 * used to write the page content stream.
 	 */
 	protected CompositeContentEmitter outputEmitters;
 	/**
 	 * use the write the page hint stream.
 	 */
 	protected IPageHintWriter pageHintWriter;
 
 	/**
 	 * page handler used to recevie the document page events.
 	 */
 	protected IPageHandler pageHandler;
 
 	protected IReportLayoutEngine engine;
 
 	/**
 	 * handle used to recive the layout page events
 	 */
 	protected CompositeLayoutPageHandler layoutPageHandler;
 
 	public ReportDocumentBuilder( ExecutionContext context,
 			ReportDocumentWriter document ) throws EngineException
 	{
 		this.executionContext = context;
 		this.document = document;
 		OnPageBreakLayoutPageHandle onPageBreakHandler = new OnPageBreakLayoutPageHandle(
 				context );
 		// output emitter is used to receive the layout content.
 		outputEmitters = new CompositeContentEmitter( );
 		// pageEmitter is used to write the the master page content
 		outputEmitters.addEmitter( new PageEmitter( ) );
 		// onPageBreakHandler's emitter is used to collect the page mode which
 		// is used to call onPageBreak.
 		outputEmitters.addEmitter( onPageBreakHandler.getEmitter( ) );
 
 		// page handler is used to receive the layout engine's page event.
 		layoutPageHandler = new CompositeLayoutPageHandler( );
 		// used to write the page hint, created by itself.
 		layoutPageHandler.addPageHandler( new LayoutPageHandler( ) );
 		// used to call the onPageBreak script, collected by its
 		// PageContentEmitter.
 		layoutPageHandler.addPageHandler( onPageBreakHandler );
 		// used to call the onPageBreak of IPageBreakListener to reset the page
 		// row count.
 		layoutPageHandler.addPageHandler( new ContextPageBreakHandler(
 				executionContext ) );
 		// used to write the content stream.
 		contentEmitter = new CompositeContentEmitter( );
 		contentEmitter.addEmitter( new ContentEmitter( ) );
 
 		// prepare the document extension
 		String[] exts = context.getEngineExtensions( );
 		if ( exts != null )
 		{
 			for ( String extName : exts )
 			{
 				EngineExtensionManager extManager = context
 						.getEngineExtensionManager( );
 				IDocumentExtension docExt = extManager
 						.getDocumentExtension( extName );
 				if ( docExt != null )
 				{
 					IContentProcessor contProc = docExt.getContentProcessor( );
 					if ( contProc != null )
 					{
 						contentEmitter.addEmitter( new ProcessorEmitter(
 								contProc ) );
 					}
 					IContentProcessor pageProc = docExt.getPageProcessor( );
 					if ( pageProc != null )
 					{
 						outputEmitters.addEmitter( new ProcessorEmitter(
 								pageProc ) );
 					}
 				}
 			}
 		}
 	}
 
 	public IContentEmitter getContentEmitter( )
 	{
 		return contentEmitter;
 	}
 
 	public void build( )
 	{
 		IReportExecutor executor = executionContext.getExecutor( );
 		engine = LayoutEngineFactory
 				.createLayoutEngine( ExtensionManager.PAGE_BREAK_PAGINATION );
 		engine.setOption( EngineTask.TASK_TYPE, new Integer(
 				IEngineTask.TASK_RUN ) );
 		engine.setPageHandler( layoutPageHandler );
 		IReportContent report = executor.execute( );
 		outputEmitters.start( report );
 		engine.layout( executor, report, outputEmitters, true );
 		engine.close( );
 		outputEmitters.end( report );
 		engine = null;
 	}
 
 	public void cancel( )
 	{
 		if ( engine != null )
 		{
 			engine.cancel( );
 		}
 	}
 
 	public void setPageHandler( IPageHandler handler )
 	{
 		pageHandler = handler;
 	}
 
 	protected void ensureContentSaved( IReportContentWriter writer,
 			IContent content ) throws IOException
 	{
 		DocumentExtension docExt = (DocumentExtension) content
 				.getExtension( IContent.DOCUMENT_EXTENSION );
 		if ( docExt == null )
 		{
 			IContent parent = (IContent) content.getParent( );
 
 			if ( parent != null
 					&& parent != parent.getReportContent( ).getRoot( ) )
 			{
 				ensureContentSaved( writer, parent );
 			}
 			writer.writeContent( content );
 		}
 	}
 
 	long writeContent( IReportContentWriter writer, IContent content )
 			throws IOException
 	{
 		IContent parent = (IContent) content.getParent( );
 		if ( parent != null && parent != parent.getReportContent( ).getRoot( ) )
 		{
 			ensureContentSaved( writer, parent );
 		}
 		return writer.writeContent( content );
 	}
 
 	long writeFullContent( IReportContentWriter writer, IContent content )
 			throws IOException
 	{
 		long offset = writeContent( writer, content );
 		Iterator iter = content.getChildren( ).iterator( );
 		while ( iter.hasNext( ) )
 		{
 			IContent child = (IContent) iter.next( );
 			writeFullContent( writer, child );
 		}
 		return offset;
 	}
 
 	private boolean needSave( IContent content )
 	{
 		InstanceID id = content.getInstanceID( );
 		if ( id == null || id.getComponentID( ) == -1 )
 		{
 			return true;
 		}
 		IContent parent = (IContent) content.getParent( );
 		if ( parent != null )
 		{
 			InstanceID pid = parent.getInstanceID( );
 			if ( pid == null || pid.getComponentID( ) == -1 )
 			{
 				return true;
 			}
 			if ( parent.getGenerateBy( ) instanceof ExtendedItemDesign )
 			{
 				return true;
 			}
 		}
 		if ( content instanceof AbstractContent )
 		{
 			return ( (AbstractContent) content ).needSave( );
 		}
 		return true;
 	}
 
 	/**
 	 * emitter used to save the report content into the content stream
 	 * 
 	 */
 	class ContentEmitter extends ContentEmitterAdapter
 	{
 
 		IReportContentWriter writer;
 
 		protected void open( )
 		{
 			try
 			{
 				writer = new ReportContentWriterV3( document );
 				writer.open( ReportDocumentConstants.CONTENT_STREAM );
 			}
 			catch ( IOException ex )
 			{
 				logger.log( Level.SEVERE, "failed to open the content writers",
 						ex );
 				close( );
 			}
 		}
 
 		protected void close( )
 		{
 			if ( writer != null )
 			{
 				writer.close( );
 			}
 			writer = null;
 		}
 
 		public void start( IReportContent report )
 		{
 			open( );
 		}
 
 		public void end( IReportContent report )
 		{
 			close( );
 		}
 
 		public void startContent( IContent content )
 		{
 			if ( writer != null )
 			{
 				if ( !needSave( content ) )
 				{
 					return;
 				}
 				try
 				{
 					long offset = writeContent( writer, content );
 					// save the reportlet index
 					Object generateBy = content.getGenerateBy( );
 					if ( generateBy instanceof TableItemDesign
 							|| generateBy instanceof ListItemDesign
 							|| generateBy instanceof ExtendedItemDesign )
 					{
 						InstanceID iid = content.getInstanceID( );
 						if ( iid != null )
 						{
 							String strIID = iid.toUniqueString( );
 							document.setOffsetOfInstance( strIID, offset );
 						}
 					}
 
 					String bookmark = content.getBookmark( );
 					if ( bookmark != null )
 					{
 						document.setOffsetOfBookmark( bookmark, offset );
 					}
 				}
 				catch ( IOException ex )
 				{
 					logger.log( Level.SEVERE, "Write content error", ex );
 					close( );
 				}
 			}
 		}
 	}
 
 	/**
 	 * emitter used to save the master page.
 	 * 
 	 */
 	class PageEmitter extends ContentEmitterAdapter
 	{
 
 		IReportContentWriter pageWriter;
 		RAOutputStream indexStream;
 		HashSet masterPages = new HashSet( );
 
 		protected void open( )
 		{
 			try
 			{
 				pageWriter = new ReportContentWriterV3( document );
 				pageWriter.open( ReportDocumentConstants.PAGE_STREAM );
 				indexStream = document.getArchive( ).createRandomAccessStream(
 						ReportDocumentConstants.PAGE_INDEX_STREAM );
 			}
 			catch ( IOException ex )
 			{
 				logger.log( Level.SEVERE, "failed to open the content writers",
 						ex );
 				close( );
 			}
 		}
 
 		protected void close( )
 		{
 			if ( pageWriter != null )
 			{
 				pageWriter.close( );
 			}
 			pageWriter = null;
 			if ( indexStream != null )
 			{
 				try
 				{
 					indexStream.close( );
 				}
 				catch ( IOException e )
 				{
 
 				}
 			}
 			indexStream = null;
 		}
 
 		public void start( IReportContent report )
 		{
 			open( );
 		}
 
 		public void end( IReportContent report )
 		{
 			close( );
 		}
 
 		private ByteArrayOutputStream writeBuffer = new ByteArrayOutputStream( );
 
 		public void startPage( IPageContent page )
 		{
 			// write the page content into the disk
 			pageNumber = page.getPageNumber( );
 
 			// write the page contents
 			try
 			{
 				pageOffset = writeFullContent( pageWriter, page );
 				String masterPage = null;
 				Object generateBy = page.getGenerateBy( );
 				if ( generateBy != null
 						&& generateBy instanceof SimpleMasterPageDesign )
 				{
 					masterPage = ( (SimpleMasterPageDesign) generateBy )
 							.getName( );
 				}
 
 				if ( masterPage != null && !masterPages.contains( masterPage )
 						&& pageOffset >= 0 )
 				{
 					writeBuffer.reset( );
 					DataOutputStream indexBuffer = new DataOutputStream(
 							writeBuffer );
 					IOUtil.writeString( indexBuffer, masterPage );
 					IOUtil.writeLong( indexBuffer, pageOffset );
 					indexStream.write( writeBuffer.toByteArray( ) );
 					masterPages.add( masterPage );
 				}
 			}
 			catch ( IOException ex )
 			{
 				logger.log( Level.SEVERE, "write page content failed", ex );
 				pageOffset = -1;
 				close( );
 			}
 		}
 
 		public void startContent( IContent content )
 		{
 			// save the bookmark index
 			String bookmark = content.getBookmark( );
 			if ( bookmark != null )
 			{
 				document.setPageNumberOfBookmark( bookmark, pageNumber );
 			}
 		}
 	}
 
 	class LayoutPageHandler implements ILayoutPageHandler
 	{
 
 		IPageHintWriter hintWriter;
 
 		LayoutPageHandler( )
 		{
 		}
 
 		boolean ensureOpen( )
 		{
 			if ( hintWriter != null )
 			{
 				return true;
 			}
 			try
 			{
 				hintWriter = new PageHintWriterV4( document.getArchive( ) );
 			}
 			catch ( IOException ex )
 			{
 				logger.log( Level.SEVERE, "Can't open the hint stream", ex );
 				return false;
 			}
 			return true;
 		}
 
 		protected void close( )
 		{
 			if ( hintWriter != null )
 			{
 				hintWriter.close( );
 			}
 			hintWriter = null;
 		}
 
 		void writeTotalPage( long pageNumber )
 		{
 			if ( ensureOpen( ) )
 			{
 				try
 				{
 					hintWriter.writeTotalPage( pageNumber );
 				}
 				catch ( IOException ex )
 				{
 					logger.log( Level.SEVERE, "Failed to save the page number",
 							ex );
 					close( );
 				}
 			}
 		}
 
 		void writePageHint( PageHint pageHint )
 		{
 			if ( ensureOpen( ) )
 			{
 				try
 				{
 					hintWriter.writePageHint( pageHint );
 				}
 				catch ( IOException ex )
 				{
 					logger.log( Level.SEVERE, "Failed to save the page hint",
 							ex );
 					close( );
 				}
 			}
 		}
 
 		private long getContentIndex( IContent content )
 		{
 			DocumentExtension docExt = (DocumentExtension) content
 					.getExtension( IContent.DOCUMENT_EXTENSION );
 			if ( docExt != null )
 			{
 				long offset = docExt.getIndex( );
 				if ( offset != -1 )
 				{
 					return offset;
 				}
 				return docExt.getPrevious( );
 			}
 			return -1;
 
 		}
 
 		private InstanceIndex[] createInstanceIndexes( IContent content )
 		{
 			LinkedList indexes = new LinkedList( );
 
 			while ( content != null
 					&& content != content.getReportContent( ).getRoot( ) )
 			{
 				InstanceID id = content.getInstanceID( );
 				long offset = getContentIndex( content );
 				indexes.addFirst( new InstanceIndex( id, offset ) );
 				content = (IContent) content.getParent( );
 			}
 
 			return (InstanceIndex[]) indexes.toArray( new InstanceIndex[]{} );
 		}
 
 		private PageSection createPageSection( IContent start, IContent end )
 		{
 			PageSection section = new PageSection( );
 			section.starts = createInstanceIndexes( start );
 			section.ends = createInstanceIndexes( end );
 			return section;
 		}
 
 		public void onPage( long pageNumber, Object context )
 		{
 			if ( context instanceof HTMLLayoutContext )
 			{
 				HTMLLayoutContext htmlContext = (HTMLLayoutContext) context;
 				document.setPageCount( pageNumber );
 
 				boolean reportFinished = htmlContext.isFinished( );
 				if ( reportFinished )
 				{
 					writeTotalPage( pageNumber );
 					close( );
 					return;
 				}
 
 				boolean checkpoint = false;
 				if ( executionContext.isProgressiveViewingEnable( ) )
 				{
 					// check points for page 1, 10, 50, 100, 200 ...
 					// the end of report should also be check point.
 					if ( pageNumber == 1 || pageNumber == 10
 							|| pageNumber == 50 || pageNumber % 100 == 0 )
 					{
 						checkpoint = true;
 					}
 				}
 
 				ArrayList pageHint = htmlContext.getPageHint( );
 				PageHint hint = new PageHint( pageNumber, htmlContext
 						.getMasterPage( ) );
 				for ( int i = 0; i < pageHint.size( ); i++ )
 				{
 					IContent[] range = (IContent[]) pageHint.get( i );
 					PageSection section = createPageSection( range[0], range[1] );
 					hint.addSection( section );
 				}
 				hint
 						.addUnresolvedRowHints( htmlContext
 								.getUnresolvedRowHints( ) );
 				hint.addTableColumnHints( htmlContext.getTableColumnHints( ) );
 				writePageHint( hint );
 
 				if ( checkpoint )
 				{
 					try
 					{
 						IDocArchiveWriter archive = document.getArchive( );
 						writeTotalPage( pageNumber );
						document
								.savePersistentObjects( ReportDocumentBuilder.this.executionContext
										.getGlobalBeans( ) );
 						document.saveCoreStreams( );
 						archive.flush( );
 					}
 					catch ( Exception e )
 					{
 						logger.log( Level.WARNING, " check point failed ", e );
 					}
 				}
 				// notify the page handler
 				if ( pageHandler != null )
 				{
 					// if user has canceled the task, we should not do onPage.
 					if ( !htmlContext.getCancelFlag( ) )
 					{
 						IReportDocumentInfo docInfo = new ReportDocumentInfo(
 								executionContext, pageNumber, false );
 						pageHandler.onPage( (int) pageNumber, checkpoint,
 								docInfo );
 					}
 				}
 			}
 		}
 	}
 
 	class ProcessorEmitter extends ContentEmitterAdapter
 	{
 
 		IContentProcessor processor;
 
 		ProcessorEmitter( IContentProcessor processor )
 		{
 			this.processor = processor;
 		}
 
 		public void end( IReportContent report )
 		{
 			try
 			{
 				processor.end( report );
 			}
 			catch ( EngineException ex )
 			{
 				executionContext.addException( ex );
 			}
 		}
 
 		public void startContent( IContent content )
 		{
 			try
 			{
 				processor.startContent( content );
 			}
 			catch ( EngineException ex )
 			{
 				executionContext.addException( ex );
 			}
 		}
 
 		public void endContent( IContent content )
 		{
 			try
 			{
 				processor.endContent( content );
 			}
 			catch ( EngineException ex )
 			{
 				executionContext.addException( ex );
 			}
 		}
 
 		public void start( IReportContent report )
 		{
 			try
 			{
 				processor.start( report );
 			}
 			catch ( EngineException ex )
 			{
 				executionContext.addException( ex );
 			}
 		}
 	}
 }
