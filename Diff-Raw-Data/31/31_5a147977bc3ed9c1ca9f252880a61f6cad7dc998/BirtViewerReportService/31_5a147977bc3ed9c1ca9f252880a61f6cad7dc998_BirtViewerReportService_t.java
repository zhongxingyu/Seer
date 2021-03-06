 /*************************************************************************************
  * Copyright (c) 2004 Actuate Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Actuate Corporation - Initial implementation.
  ************************************************************************************/
 
 package org.eclipse.birt.report.service;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.eclipse.birt.core.exception.BirtException;
 import org.eclipse.birt.report.context.ViewerAttributeBean;
 import org.eclipse.birt.report.engine.api.EngineException;
 import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
 import org.eclipse.birt.report.engine.api.IReportDocument;
 import org.eclipse.birt.report.engine.api.IReportRunnable;
 import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
 import org.eclipse.birt.report.engine.api.ReportParameterConverter;
 import org.eclipse.birt.report.engine.api.TOCNode;
 import org.eclipse.birt.report.model.api.ScalarParameterHandle;
 import org.eclipse.birt.report.service.api.ExportedColumn;
 import org.eclipse.birt.report.service.api.ExportedResultSet;
 import org.eclipse.birt.report.service.api.IViewerReportDesignHandle;
 import org.eclipse.birt.report.service.api.IViewerReportService;
 import org.eclipse.birt.report.service.api.InputOptions;
 import org.eclipse.birt.report.service.api.ReportServiceException;
 import org.eclipse.birt.report.service.api.ToC;
 import org.eclipse.birt.report.soapengine.api.Column;
 import org.eclipse.birt.report.soapengine.api.ResultSet;
 
 public class BirtViewerReportService implements IViewerReportService
 {
 
 	public BirtViewerReportService( ServletConfig conf )
 	{
 		try
 		{
 			ReportEngineService.initEngineInstance( conf );
 		}
 		catch ( BirtException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace( );
 		}
 	}
 
 	public String runReport( IViewerReportDesignHandle design,
 			String outputDocName, InputOptions runOptions, Map parameters )
 			throws ReportServiceException
 	{
 		IReportRunnable runnable;
 		String reportDesignName = design.getFileName( );
 		HttpServletRequest request = (HttpServletRequest) runOptions
 				.getOption( InputOptions.OPT_REQUEST );
 		Locale locale = (Locale) runOptions.getOption( InputOptions.OPT_LOCALE );
 		try
 		{
 			runnable = ReportEngineService.getInstance( ).openReportDesign(
 					reportDesignName );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 
 		Map parsedParams = getParsedParameters( design, runOptions, parameters );
 		try
 		{
 			ReportEngineService.getInstance( ).runReport( request, runnable,
 					outputDocName, locale, (HashMap) parsedParams );
 		}
 		catch ( RemoteException e )
 		{
 			e.printStackTrace( );
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 		return outputDocName;
 	}
 
 	public IViewerReportDesignHandle getReportDesignHandle( String docName,
 			InputOptions options )
 	{
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( options ), docName );
 		String fileName = doc.getReportRunnable( ).getReportName( );
 		doc.close( );
 		// TODO: What is content type?
 		IViewerReportDesignHandle design = new BirtViewerReportDesignHandle(
 				null, fileName );
 		return design;
 	}
 
 	// TODO: Maybe change pageID to long?
 	public ByteArrayOutputStream getPage( String docName, String pageID,
 			InputOptions renderOptions, List activeIds )
 			throws ReportServiceException
 	{
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( renderOptions ),
 						docName );
 		HttpServletRequest request = (HttpServletRequest) renderOptions
 				.getOption( InputOptions.OPT_REQUEST );
 		Locale locale = (Locale) renderOptions
 				.getOption( InputOptions.OPT_LOCALE );
 		Boolean isMasterPageContent = (Boolean) renderOptions
 				.getOption( InputOptions.OPT_IS_MASTER_PAGE_CONTENT );
 		Boolean svgFlag = (Boolean) renderOptions
 				.getOption( InputOptions.OPT_IS_MASTER_PAGE_CONTENT );
 		Long pageNum = Long.valueOf( pageID );
 		try
 		{
 			ByteArrayOutputStream os = ReportEngineService.getInstance( )
 					.renderReport( request, doc, pageNum.longValue( ),
 							isMasterPageContent.booleanValue( ),
 							svgFlag.booleanValue( ), activeIds, locale );
 			doc.close( );
 			return os;
 
 		}
 		catch ( RemoteException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	public ByteArrayOutputStream getPageByBookmark( String docName,
 			String bookmark, InputOptions renderOptions, List activeIds )
 			throws ReportServiceException
 	{
 		long pageNum = getPageNumberByBookmark( docName, bookmark,
 				renderOptions );
 		// TODO: Why String and not long in getPage?
 		return getPage( docName, pageNum + "", renderOptions, activeIds );
 	}
 
 	public ByteArrayOutputStream getPageByObjectId( String docName,
 			String objectId, InputOptions renderOptions, List activeIds )
 			throws ReportServiceException
 	{
 		// TODO: Implement
 		return null;
 	}
 
 	public ByteArrayOutputStream renderReportlet( String docName,
 			String objectId, InputOptions renderOptions, List activeIds )
 			throws ReportServiceException
 	{
 		// TODO: renderReportlet and getPageByObjectId should behave
 		// differently. renderReportlet should return all pages the object
 		// occurs in, while getPageByObjectId only returns the first
 		return getPageByObjectId( docName, objectId, renderOptions, activeIds );
 	}
 
 	public void renderReport( String docName, String pageRange,
 			InputOptions renderOptions, OutputStream out )
 			throws ReportServiceException
 	{
 		// TODO: Implement
 	}
 
 	public void extractResultSet( String docName, String resultSetId,
			Collection columns, Set filters, InputOptions options, OutputStream out )
 			throws ReportServiceException
 	{
 
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( options ), docName );
 		Locale locale = (Locale) options.getOption( InputOptions.OPT_LOCALE );
 		// TODO: Filters are not used...
 		try
 		{
 			ReportEngineService.getInstance( ).extractData( doc, resultSetId,
 					columns, locale, out );
 			doc.close( );
 		}
 		catch ( RemoteException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	public List getResultSetsMetadata( String docName, InputOptions options )
 			throws ReportServiceException
 	{
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( options ), docName );
 
 		ResultSet[] resultSetArray;
 		try
 		{
 			resultSetArray = ReportEngineService.getInstance( ).getResultSets(
 					doc );
 			doc.close( );
 		}
 		catch ( RemoteException e )
 		{
 			e.printStackTrace( );
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 		if ( resultSetArray == null || resultSetArray.length <= 0 )
 		{
 			throw new ReportServiceException(
 					"There is no result set available for extracting the data." ); //$NON-NLS-1$
 		}
 
 		return transformResultSetArray( resultSetArray );
 	}
 
 	public List getResultSetsMetadata( String docName, String instanceId,
 			InputOptions options ) throws ReportServiceException
 	{
 		// TODO: Implement
 		return null;
 	}
 
 	public void getImage( String docName, String imageId, OutputStream out,
 			InputOptions options ) throws ReportServiceException
 	{
 		try
 		{
 			// TODO: docName is not used. why?
 			ReportEngineService.getInstance( ).renderImage( imageId, out );
 		}
 		catch ( RemoteException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 
 	}
 
 	public ToC getTOC( String docName, String tocId, InputOptions options )
 			throws ReportServiceException
 	{
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( options ), docName );
 
 		TOCNode node = null;
 		if ( tocId != null )
 		{
 			node = doc.findTOC( tocId );
 
 		}
 		else
 		{
 			node = doc.findTOC( null );
 		}
 
 		if ( node == null )
 		{
 			throw new ReportServiceException( "Invalid TOC query." ); //$NON-NLS-1$
 		}
 
 		doc.close( );
 		return transformTOCNode( node );
 	}
 
 	public long getPageCount( String docName, InputOptions options )
 			throws ReportServiceException
 	{
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( options ), docName );
 		long count = doc.getPageCount( );
 		doc.close( );
 		return count;
 	}
 
 	public Collection getParameterDefinitions(
 			IViewerReportDesignHandle design, InputOptions runOptions )
 			throws ReportServiceException
 	{
 		String reportDesignName = design.getFileName( );
 		try
 		{
 			IGetParameterDefinitionTask task = getParameterDefinitionTask( reportDesignName );
 			return task.getParameterDefns( false );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	public Map getParameterValues( String docName, InputOptions options )
 	{
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( options ), docName );
 		Map paramValues = doc.getParameterValues( );
 		doc.close( );
 		return paramValues;
 	}
 
 	public Collection getSelectionListForCascadingGroup(
 			IViewerReportDesignHandle design, String groupName,
 			Object[] groupKeys ) throws ReportServiceException
 	{
 		String reportDesignName = design.getFileName( );
 
 		try
 		{
 			IGetParameterDefinitionTask task = getParameterDefinitionTask( reportDesignName );
 			task.evaluateQuery( groupName );
 			return task
 					.getSelectionListForCascadingGroup( groupName, groupKeys );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 
 	}
 
 	public Collection getParameterSelectionList(
 			IViewerReportDesignHandle design, InputOptions runOptions,
 			String paramName ) throws ReportServiceException
 	{
 		String reportDesignName = design.getFileName( );
 		try
 		{
 			IGetParameterDefinitionTask task = getParameterDefinitionTask( reportDesignName );
 			return task.getSelectionList( paramName );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	public long getPageNumberByBookmark( String docName, String bookmark,
 			InputOptions options ) throws ReportServiceException
 	{
 		IReportDocument doc = ReportEngineService.getInstance( )
 				.openReportDocument( getReportDesignName( options ), docName );
 		long pageNumber = doc.getPageNumber( bookmark );
 		doc.close( );
 		return pageNumber;
 	}
 
 	public long getPageNumberByObjectId( String docName, String objectId,
 			InputOptions options ) throws ReportServiceException
 	{
 		// TODO: Implement
 		return 0;
 	}
 
 	public void runAndRenderReport( IViewerReportDesignHandle design,
 			String outputDocName, InputOptions options, Map parameters,
 			OutputStream out, List activeIds ) throws ReportServiceException
 	{
 		// TODO: outputDocName is not used...
 		String reportDesignName = design.getFileName( );
 		HttpServletRequest request = (HttpServletRequest) options
 				.getOption( InputOptions.OPT_REQUEST );
 		Locale locale = (Locale) options.getOption( InputOptions.OPT_LOCALE );
 		Boolean isMasterPageContent = (Boolean) options
 				.getOption( InputOptions.OPT_IS_MASTER_PAGE_CONTENT );
 		Boolean svgFlag = (Boolean) options
 				.getOption( InputOptions.OPT_IS_MASTER_PAGE_CONTENT );
 		String format = (String) options
 				.getOption( InputOptions.OPT_RENDER_FORMAT );
 		try
 		{
 			IReportRunnable runnable = ReportEngineService.getInstance( )
 					.openReportDesign( reportDesignName );
 
 			Map parsedParams = getParsedParameters( design, options, parameters );
 
 			ReportEngineService.getInstance( )
 					.runAndRenderReport( request, runnable, out, format,
 							locale, parsedParams,
 							isMasterPageContent.booleanValue( ),
 							svgFlag.booleanValue( ) );
 		}
 		catch ( RemoteException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 
 	}
 
 	public Object getParameterDefaultValue( IViewerReportDesignHandle design,
 			String parameterName, InputOptions options )
 			throws ReportServiceException
 	{
 		try
 		{
 			IGetParameterDefinitionTask task = getParameterDefinitionTask( design
 					.getFileName( ) );
 			return task.getDefaultValue( parameterName );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	public Collection getParameterHandles( IViewerReportDesignHandle design,
 			InputOptions options ) throws ReportServiceException
 	{
 		try
 		{
 			IGetParameterDefinitionTask parameterTask = getParameterDefinitionTask( design
 					.getFileName( ) );
 			if ( parameterTask == null )
 				throw new ReportServiceException( "Can not get parameter task." ); //$NON-NLS-1$
 			return parameterTask.getParameters( ).getContents( );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 	}
 
 	public void setContext( Object context, InputOptions options )
 	{
 		HttpServletRequest request = (HttpServletRequest) options
 				.getOption( InputOptions.OPT_REQUEST );
 		ServletContext servletContext = (ServletContext) context;
 		ReportEngineService.getInstance( ).setEngineContext( servletContext,
 				request );
 	}
 
 	private Map getParsedParameters( IViewerReportDesignHandle design,
 			InputOptions options, Map parameters )
 			throws ReportServiceException
 	{
 		Locale locale = (Locale) options.getOption( InputOptions.OPT_LOCALE );
 		Collection parameterList = getParameterDefinitions( design, options );
 		Map paramMap = new HashMap( );
 		IGetParameterDefinitionTask task = null;
 		Map configMap = null;
 		Boolean isDesignerBoolean = (Boolean) options
 				.getOption( InputOptions.OPT_IS_DESIGNER );
 		boolean isDesigner = ( isDesignerBoolean != null ? isDesignerBoolean
 				.booleanValue( ) : false );
 		try
 		{
 			task = getParameterDefinitionTask( design.getFileName( ) );
 			IReportRunnable runnable = ReportEngineService.getInstance( )
 					.openReportDesign( design.getFileName( ) );
 			configMap = runnable.getTestConfig( );
 		}
 		catch ( EngineException e )
 		{
 			throw new ReportServiceException( e.getLocalizedMessage( ) );
 		}
 		for ( Iterator iter = parameterList.iterator( ); iter.hasNext( ); )
 		{
 			IScalarParameterDefn parameterObj = (IScalarParameterDefn) iter
 					.next( );
 
 			String paramValue = null;
 			Object paramValueObj = null;
 
 			ScalarParameterHandle paramHandle = (ScalarParameterHandle) parameterObj
 					.getHandle( );
 			String paramName = paramHandle.getName( );
 			String format = paramHandle.getPattern( );
 
 			ReportParameterConverter converter = new ReportParameterConverter(
 					format, locale );
 
 			Set paramNames = null;
 			if ( parameters != null )
 			{
 				paramNames = parameters.keySet( );
 			}
 
 			if ( parameters != null && paramName != null )
 			{
 				boolean found = false;
 				for ( Iterator it = paramNames.iterator( ); it.hasNext( ); )
 				{
 					String name = (String) it.next( );
 					if ( paramName.equals( name ) )
 					{
 						if ( parameters.get( name ) != null )
 							paramValue = parameters.get( name ).toString( );
 						paramValueObj = converter.parse( paramValue,
 								ParameterDataTypeConverter
 										.getEngineDataType( paramHandle
 												.getDataType( ) ) );
 						paramMap.put( paramName, paramValueObj );
 						found = true;
 						break;
 					}
 				}
 				if ( !found && configMap.containsKey( paramName ) && isDesigner )
 				{
 					// Get value from test config
 					String configValue = (String) configMap.get( paramName );
 					ReportParameterConverter cfgConverter = new ReportParameterConverter(
 							format, Locale.US );
 					paramValueObj = cfgConverter.parse( configValue,
 							parameterObj.getDataType( ) );
 				}
 				else if ( !found )
 				{
 					// Get default value from task
 					paramValueObj = task.getDefaultValue( parameterObj
 							.getName( ) );
 				}
 			}
 		}
 		return paramMap;
 
 	}
 
 	private static ToC transformTOCNode( TOCNode node )
 	{
 		ToC toc = new ToC( node.getNodeID( ), node.getDisplayString( ), node
 				.getBookmark( ) );
 		toc.setChildren( getToCChildren( node ) );
 		return toc;
 	}
 
 	private static List getToCChildren( TOCNode node )
 	{
 		if ( node.getChildren( ) == null )
 			return null;
 		List children = node.getChildren( );
 		List ret = new ArrayList( );
 		Iterator it = children.iterator( );
 		while ( it.hasNext( ) )
 		{
 			TOCNode childNode = (TOCNode) it.next( );
 			ToC child = new ToC( childNode.getNodeID( ), childNode
 					.getDisplayString( ), childNode.getBookmark( ) );
 			// Recursion to transform all children etc...
 			child.setChildren( getToCChildren( childNode ) );
 			ret.add( child );
 		}
 		return ret;
 	}
 
 	private IGetParameterDefinitionTask getParameterDefinitionTask(
 			String reportDesignName ) throws EngineException
 	{
 
 		IReportRunnable runnable = ReportEngineService.getInstance( )
 				.openReportDesign( reportDesignName );
 		IGetParameterDefinitionTask paramTask = ReportEngineService
 				.getInstance( ).createGetParameterDefinitionTask( runnable );
 		return paramTask;
 	}
 
 	private List transformResultSetArray( ResultSet[] resultSetArray )
 	{
 		List ret = new ArrayList( );
 		for ( int i = 0; i < resultSetArray.length; i++ )
 		{
 			ResultSet rs = resultSetArray[i];
 			String queryName = rs.getQueryName( );
 			Column[] columnArray = rs.getColumn( );
 			List columns = new ArrayList( );
 			for ( int j = 0; j < columnArray.length; j++ )
 			{
 				Column column = columnArray[j];
 				ExportedColumn exportedColumn = new ExportedColumn( column
 						.getName( ), column.getLabel( ), column.getVisibility( )
 						.booleanValue( ) );
 				columns.add( exportedColumn );
 			}
 			ExportedResultSet exportedResultSet = new ExportedResultSet(
 					queryName, columns );
 			ret.add( exportedResultSet );
 		}
 		return ret;
 	}
 
 	/**
 	 * Gets the report design name from the input options.
 	 * 
 	 * @param options
 	 *            the input options
 	 * @return the report design name if the request contains a valid name,
 	 *         otherwise null
 	 */
 
 	private String getReportDesignName( InputOptions options )
 	{
 		String reportDesignName = null;
 
 		if ( options != null )
 		{
 			HttpServletRequest request = (HttpServletRequest) options
 					.getOption( InputOptions.OPT_REQUEST );
 			if ( request != null )
 			{
 				ViewerAttributeBean attrBean = (ViewerAttributeBean) request
 						.getAttribute( "attributeBean" ); //$NON-NLS-1$
 				assert attrBean != null;
 
 				reportDesignName = attrBean.getReportDesignName( );
 				if ( reportDesignName != null )
 				{
 					// if the report design name is not a valid file, then set
 					// it to null
 
 					if ( reportDesignName.endsWith( "\\" ) || reportDesignName.endsWith( "/" ) )
 						reportDesignName = null;
 				}
 			}
 		}
 		return reportDesignName;
 	}
 
 }
