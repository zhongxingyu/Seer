 /*******************************************************************************
  * Copyright (c) 2004 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.report.engine.executor;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.report.engine.content.IPageContent;
 import org.eclipse.birt.report.engine.content.IReportContent;
 import org.eclipse.birt.report.engine.content.impl.ReportContent;
 import org.eclipse.birt.report.engine.emitter.DOMBuilderEmitter;
 import org.eclipse.birt.report.engine.emitter.IContentEmitter;
 import org.eclipse.birt.report.engine.extension.IReportItemExecutor;
 import org.eclipse.birt.report.engine.internal.executor.dom.DOMReportItemExecutor;
 import org.eclipse.birt.report.engine.ir.MasterPageDesign;
 import org.eclipse.birt.report.engine.ir.Report;
 import org.eclipse.birt.report.engine.ir.ReportItemDesign;
 import org.eclipse.birt.report.engine.toc.TOCBuilder;
 import org.eclipse.birt.report.engine.toc.TOCTree;
 
 /**
  * Captures the (report design to) report instance creation logic, by combining
  * the report design structure and the data. It acts as an entry point to allow
  * actual data to drive the contents that appears in a report, i.e., header
  * frames use the first data row, detail rows are repeated for each data row,
  * etc. The output of the executor, for now, is a specific output format, i.e.,
  * HTML, FO or PDF, with the help of the emitter extensions.
  * <p>
  * 
  * The report instance creation logic is subject to further abstraction, because
  * it is needed in both report generation and report presentation. This is
  * because report document (not supported for now) does not store each report
  * item instance. As a result, the report item instances need to be created at
  * presentation time too. For now, report generation and presentation are merged
  * as we do not generate report documents. The report instance creation logic is
  * therefore run only once. When the generation and presentation phases are
  * separated, the output of an executor could not only be a specific report
  * output format, but also be a report document. Data would then come from
  * database in factory engine, and from report document in the presentation
  * engine.
  * 
  */
 public class ReportExecutor implements IReportExecutor
 {
 
 	protected static Logger logger = Logger.getLogger( ReportExecutor.class
 			.getName( ) );
 
 	// the report execution context
 	private ExecutionContext context;
 
 	// the manager used to manage the executors.
 	private ExecutorManager manager;
 
 	private Report report;
 
 	private ReportContent reportContent;
 
 	private long uniqueId;
 	
 	private HashMap pages = new HashMap();
 
 	/**
 	 * constructor
 	 * 
 	 * @param context
 	 *            the executor context
 	 * @param emitter
 	 *            the report emitter
 	 * 
 	 */
 	public ReportExecutor( ExecutionContext context )
 	{
 		this.context = context;
 		this.report = context.getReport( );
 		this.manager = new ExecutorManager( this );
 		this.uniqueId = 0;
 	}
 
 	public IReportContent execute( )
 	{
 		reportContent = new ReportContent( report );
 		context.setReportContent( reportContent );
 		TOCTree tocTree = new TOCTree( );
 		reportContent.setTOCTree( tocTree );
 		TOCBuilder tocBuilder = new TOCBuilder( tocTree );
 		context.setTOCBuilder( tocBuilder );
 
 		// Prepare necessary data for this report
 		Map appContext = context.getAppContext( );
 		context.getDataEngine( ).prepare( report, appContext );
 		
 		
 		//create execution optimize policy
 		context.optimizeExecution( );
 
 		// prepare to execute the child
 		currentItem = 0;
 
 		return reportContent;
 	}
 
 	public void close( )
 	{
 		uniqueId = 0;
 	}
 
 	int currentItem;
 
 	public IReportItemExecutor getNextChild( )
 	{
 		if ( hasNextChild( ) )
 		{
 			ReportItemDesign design = report.getContent( currentItem++ );
 			ReportItemExecutor executor = manager.createExecutor( null, design );
 			return executor;
 		}
 		return null;
 	}
 
 	public boolean hasNextChild( )
 	{
 		if ( currentItem < report.getContentCount( ) )
 		{
 			return true;
 		}
 		return false;
 	}
 
 	public ExecutionContext getContext( )
 	{
 		return this.context;
 	}
 
 	public ExecutorManager getManager( )
 	{
 		return this.manager;
 	}
 
 	long generateUniqueID( )
 	{
 		return uniqueId++;
 	}
 
 	public IReportItemExecutor createPageExecutor( long pageNumber,
 			MasterPageDesign pageDesign )
 	{
 		//only execute once for the same page design
 		IPageContent pageContent = (IPageContent)pages.get( pageDesign );
 		if(pageContent == null )
 		{
 			
 			IReportItemExecutor pageExecutor = new MasterPageExecutor( manager, pageNumber, pageDesign );
 			pageContent = (IPageContent) pageExecutor.execute( );
 			IContentEmitter domEmitter = new DOMBuilderEmitter( pageContent );
 			ReportExecutorUtil.executeAll( pageExecutor, domEmitter );
 			pageExecutor.close( );
 			pages.put( pageDesign, pageContent );
 		}
 		else
 		{
 			pageContent.setPageNumber( pageNumber );
			context.setPageNumber( pageNumber );
 		}
 		return new DOMReportItemExecutor( pageContent, true );
 	}
 }
