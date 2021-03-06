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
 
 package org.eclipse.birt.report.taglib;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.JspWriter;
 
 import org.eclipse.birt.report.resource.BirtResources;
 import org.eclipse.birt.report.resource.ResourceConstants;
 import org.eclipse.birt.report.taglib.component.ParameterField;
 import org.eclipse.birt.report.taglib.util.BirtTagUtil;
 import org.eclipse.birt.report.utility.DataUtil;
 import org.eclipse.birt.report.utility.ParameterAccessor;
 
 /**
  * Abstract class for viewer tag. List base attributes for a viewer tag.
  * 
  */
 public abstract class AbstractViewerTag extends AbstractBaseTag
 {
 
 	/**
 	 * Locale information
 	 */
 	protected Locale locale;
 
 	/**
 	 * Report parameters
 	 */
 	protected Map parameters;
 
 	/**
 	 * Then entry to initialize tag
 	 * 
 	 * @throws Exception
 	 */
 	public void __init( )
 	{
 		super.__init( );
 		parameters = new HashMap( );
 	}
 
 	/**
 	 * validate the tag
 	 * 
 	 * @see org.eclipse.birt.report.taglib.AbstractBaseTag#__validate()
 	 */
 	public boolean __validate( ) throws Exception
 	{
 		String hasHostPage = (String) pageContext.getAttribute( ATTR_HOSTPAGE );
 		if ( hasHostPage != null && "true".equalsIgnoreCase( hasHostPage ) ) //$NON-NLS-1$
 		{
 			return false;
 		}		
 		
 		// get Locale
 		this.locale = BirtTagUtil.getLocale( (HttpServletRequest) pageContext
 				.getRequest( ), viewer.getLocale( ) );
 
 		// Set locale information
 		BirtResources.setLocale( this.locale );
 
 		// Validate viewer id
 		if ( viewer.getId( ) == null || viewer.getId( ).length( ) <= 0 )
 		{
 			throw new JspTagException( BirtResources
 					.getMessage( ResourceConstants.TAGLIB_NO_VIEWER_ID ) );
 		}
 
 		if ( !__validateViewerId( ) )
 		{
 			throw new JspTagException( BirtResources
 					.getMessage( ResourceConstants.TAGLIB_INVALID_VIEWER_ID ) );
 		}
 
 		// validate the viewer id if unique
 		if ( pageContext.findAttribute( viewer.getId( ) ) != null )
 		{
 			throw new JspTagException( BirtResources
 					.getMessage( ResourceConstants.TAGLIB_VIEWER_ID_DUPLICATE ) );
 		}
 
 		// Report design or document should be specified
 		if ( viewer.getReportDesign( ) == null
 				&& viewer.getReportDocument( ) == null )
 		{
 			throw new JspTagException( BirtResources
 					.getMessage( ResourceConstants.TAGLIB_NO_REPORT_SOURCE ) );
 		}
 
 		// If preview reportlet, report document file should be specified.
 		if ( viewer.getReportletId( ) != null
 				&& viewer.getReportDocument( ) == null )
 		{
 			throw new JspTagException( BirtResources
 					.getMessage( ResourceConstants.TAGLIB_NO_REPORT_DOCUMENT ) );
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Validate the viewer id. Viewer id only can include number, letter and
 	 * underline
 	 * 
 	 * @return
 	 */
 	protected boolean __validateViewerId( )
 	{
 		Pattern p = Pattern.compile( "^\\w+$" ); //$NON-NLS-1$
 		Matcher m = p.matcher( viewer.getId( ) );
 		return m.find( );
 	}
 
 	/**
 	 * Handle event before doEndTag
 	 */
 	protected void __beforeEndTag( )
 	{
 		super.__beforeEndTag( );
 		viewer.setParameters( parameters );
 
 		// Save viewer id
 		pageContext.setAttribute( viewer.getId( ), viewer.getId( ) );
 
 		// Save has HostPage
 		if ( viewer.isHostPage( ) )
 			pageContext.setAttribute( ATTR_HOSTPAGE, "true" ); //$NON-NLS-1$
 	}
 
 	/**
 	 * Handle use IFrame to preview report. Each IFrame should have an unique
 	 * id.
 	 * 
 	 * @param src
 	 * @param target
 	 * @throws Exception
 	 */
 	protected void __handleIFrame( String src, String target ) throws Exception
 	{
 		JspWriter writer = pageContext.getOut( );
 
 		writer.write( "<script type=\"text/javascript\">\n" ); //$NON-NLS-1$
 		writer.write( "function loadViewer" + viewer.getId( ) + "(){\n" ); //$NON-NLS-1$//$NON-NLS-2$
 		writer.write( "var divObj = document.createElement( \"DIV\" );\n" ); //$NON-NLS-1$
 		writer.write( "var bodyObj = document.body;\n" ); //$NON-NLS-1$
 		writer.write( "if( !bodyObj )\n" ); //$NON-NLS-1$
 		writer.write( "  bodyObj = document.createElement(\"BODY\");\n" ); //$NON-NLS-1$
 		writer.write( "bodyObj.appendChild( divObj );\n" ); //$NON-NLS-1$
 		writer.write( "divObj.style.display = \"none\";\n" ); //$NON-NLS-1$
 		writer.write( "var formObj = document.createElement( \"FORM\" );\n" ); //$NON-NLS-1$
 		writer.write( "divObj.appendChild( formObj );\n" ); //$NON-NLS-1$
 
 		Iterator it = viewer.getParameters( ).values( ).iterator( );
 		while ( it.hasNext( ) )
 		{
 			ParameterField param = (ParameterField) it.next( );
 			// set NULL parameter
 			if ( param.getValue( ) == null )
 			{
 				writer
 						.write( "var param = document.createElement( \"INPUT\" );\n" ); //$NON-NLS-1$
 				writer.write( "formObj.appendChild( param );\n" ); //$NON-NLS-1$
 				writer.write( "param.TYPE = \"HIDDEN\";\n" ); //$NON-NLS-1$
 				writer.write( "param.name='" + ParameterAccessor.PARAM_ISNULL //$NON-NLS-1$
 						+ "';\n" ); //$NON-NLS-1$
 				writer.write( "param.value='" + param.getName( ) + "';\n" ); //$NON-NLS-1$//$NON-NLS-2$
 				continue;
 			}
 
 			// parse parameter object as standard format
 			String paramValue = DataUtil.getDisplayValue( param.getValue( ) );
 
 			// set Parameter value
 			writer.write( "var param = document.createElement( \"INPUT\" );\n" ); //$NON-NLS-1$
 			writer.write( "formObj.appendChild( param );\n" ); //$NON-NLS-1$
 			writer.write( "param.TYPE = \"HIDDEN\";\n" ); //$NON-NLS-1$
 			writer.write( "param.name='" + param.getName( ) + "';\n" ); //$NON-NLS-1$ //$NON-NLS-2$
 			writer.write( "param.value='" + paramValue + "';\n" ); //$NON-NLS-1$//$NON-NLS-2$
 
 			// if value is string, check wheter set isLocale flag
 			if ( param.getValue( ) instanceof String && param.isLocale( ) )
 			{
 				writer
 						.write( "var param = document.createElement( \"INPUT\" );\n" ); //$NON-NLS-1$
 				writer.write( "formObj.appendChild( param );\n" ); //$NON-NLS-1$
 				writer.write( "param.TYPE = \"HIDDEN\";\n" ); //$NON-NLS-1$
 				writer
 						.write( "param.name='" + ParameterAccessor.PARAM_ISLOCALE + "';\n" ); //$NON-NLS-1$ //$NON-NLS-2$
 				writer.write( "param.value='" + param.getName( ) + "';\n" ); //$NON-NLS-1$//$NON-NLS-2$				
 			}
 
 			// set parameter pattern format
 			if ( param.getPattern( ) != null )
 			{
 				writer
 						.write( "var param = document.createElement( \"INPUT\" );\n" ); //$NON-NLS-1$
 				writer.write( "formObj.appendChild( param );\n" ); //$NON-NLS-1$
 				writer.write( "param.TYPE = \"HIDDEN\";\n" ); //$NON-NLS-1$
 				writer.write( "param.name='" + param.getName( ) //$NON-NLS-1$
 						+ "_format';\n" ); //$NON-NLS-1$
 				writer.write( "param.value='" + param.getPattern( ) + "';\n" ); //$NON-NLS-1$//$NON-NLS-2$
 			}
 
 			// set parameter display text
 			if ( param.getDisplayText( ) != null )
 			{
 				writer
 						.write( "var param = document.createElement( \"INPUT\" );\n" ); //$NON-NLS-1$
 				writer.write( "formObj.appendChild( param );\n" ); //$NON-NLS-1$
 				writer.write( "param.TYPE = \"HIDDEN\";\n" ); //$NON-NLS-1$
 				writer.write( "param.name='" //$NON-NLS-1$
 						+ ParameterAccessor.PREFIX_DISPLAY_TEXT
 						+ param.getName( ) + "';\n" ); //$NON-NLS-1$
 				writer
 						.write( "param.value='" + param.getDisplayText( ) + "';\n" ); //$NON-NLS-1$ //$NON-NLS-2$
 
 			}
 		}
 
 		writer.write( "formObj.action = \"" + src + "\";\n" ); //$NON-NLS-1$ //$NON-NLS-2$
 		writer.write( "formObj.method = \"post\";\n" ); //$NON-NLS-1$
 
 		if ( target != null )
 			writer.write( "formObj.target = \"" + target + "\";\n" ); //$NON-NLS-1$ //$NON-NLS-2$
 
 		writer.write( "formObj.submit( );\n" ); //$NON-NLS-1$
 		writer.write( "}\n" ); //$NON-NLS-1$
 		writer.write( "</script>\n" ); //$NON-NLS-1$
 
 		// write IFrame object
 		writer.write( __handleIFrameDefinition( ) );
 
 		writer.write( "<script type=\"text/javascript\">" ); //$NON-NLS-1$
 		writer.write( "loadViewer" + viewer.getId( ) + "();" ); //$NON-NLS-1$//$NON-NLS-2$
 		writer.write( "</script>\n" ); //$NON-NLS-1$
 	}
 
 	/**
 	 * Handle IFrame definition
 	 * 
 	 * @return
 	 */
 	protected String __handleIFrameDefinition( )
 	{
 		// create IFrame object
 		String iframe = "<iframe name=\"" + viewer.getId( ) //$NON-NLS-1$
 				+ "\" frameborder=\"" + viewer.getFrameborder( ) + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
 
 		if ( viewer.getScrolling( ) != null )
 			iframe += " scrolling = \"" + viewer.getScrolling( ) + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
 
 		iframe += __handleAppearance( ) + "></iframe>\r\n"; //$NON-NLS-1$
 
 		return iframe;
 	}
 
 	/**
 	 * IFrame Appearance style
 	 * 
 	 * @return
 	 */
 	protected String __handleAppearance( )
 	{
 		String style = " style='"; //$NON-NLS-1$
 
 		// position
 		if ( viewer.getPosition( ) != null )
 			style += "position:" + viewer.getPosition( ) + ";"; //$NON-NLS-1$//$NON-NLS-2$
 
 		// height
 		if ( viewer.getHeight( ) >= 0 )
 			style += "height:" + viewer.getHeight( ) + "px;"; //$NON-NLS-1$//$NON-NLS-2$
 
 		// width
 		if ( viewer.getWidth( ) >= 0 )
 			style += "width:" + viewer.getWidth( ) + "px;"; //$NON-NLS-1$//$NON-NLS-2$
 
 		// top
 		if ( viewer.getTop( ) >= 0 )
 			style += "top:" + viewer.getTop( ) + "px;"; //$NON-NLS-1$//$NON-NLS-2$
 
 		// left
 		if ( viewer.getLeft( ) >= 0 )
 			style = style + "left:" + viewer.getLeft( ) + "px;"; //$NON-NLS-1$//$NON-NLS-2$
 
 		// style
 		if ( viewer.getStyle( ) != null )
 			style += viewer.getStyle( ) + ";"; //$NON-NLS-1$
 
 		style += "' "; //$NON-NLS-1$	
 
 		return style;
 	}
 
 	/**
 	 * Add parameter into list
 	 * 
 	 * @param field
 	 */
 	public void addParameter( ParameterField field )
 	{
 		if ( field != null )
 			parameters.put( field.getName( ), field );
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId( String id )
 	{
 		viewer.setId( id );
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName( String name )
 	{
 		viewer.setName( name );
 	}
 
 	/**
 	 * @param contextRoot
 	 *            the contextRoot to set
 	 */
 	public void setContextRoot( String contextRoot )
 	{
 		viewer.setContextRoot( contextRoot );
 	}
 
 	/**
 	 * @param isHostPage
 	 *            the isHostPage to set
 	 */
 	public void setIsHostPage( String isHostPage )
 	{
 		viewer.setHostPage( Boolean.valueOf( isHostPage ).booleanValue( ) );
 	}
 
 	/**
 	 * @param scrolling
 	 *            the scrolling to set
 	 */
 	public void setScrolling( String scrolling )
 	{
 		viewer.setScrolling( scrolling );
 	}
 
 	/**
 	 * @param position
 	 *            the position to set
 	 */
 	public void setPosition( String position )
 	{
 		viewer.setPosition( position );
 	}
 
 	/**
 	 * @param style
 	 *            the style to set
 	 */
 	public void setStyle( String style )
 	{
 		viewer.setStyle( style );
 	}
 
 	/**
 	 * @param height
 	 *            the height to set
 	 */
 	public void setHeight( String height )
 	{
 		viewer.setHeight( Integer.parseInt( height ) );
 	}
 
 	/**
 	 * @param width
 	 *            the width to set
 	 */
 	public void setWidth( String width )
 	{
 		viewer.setWidth( Integer.parseInt( width ) );
 	}
 
 	/**
 	 * @param left
 	 *            the left to set
 	 */
 	public void setLeft( String left )
 	{
 		viewer.setLeft( Integer.parseInt( left ) );
 	}
 
 	/**
 	 * @param top
 	 *            the top to set
 	 */
 	public void setTop( String top )
 	{
 		viewer.setTop( Integer.parseInt( top ) );
 	}
 
 	/**
 	 * @param frameborder
 	 *            the frameborder to set
 	 */
 	public void setFrameborder( String frameborder )
 	{
 		viewer.setFrameborder( frameborder );
 	}
 
 	/**
 	 * @param reportDesign
 	 *            the reportDesign to set
 	 */
 	public void setReportDesign( String reportDesign )
 	{
 		viewer.setReportDesign( reportDesign );
 	}
 
 	/**
 	 * @param reportDocument
 	 *            the reportDocument to set
 	 */
 	public void setReportDocument( String reportDocument )
 	{
 		viewer.setReportDocument( reportDocument );
 	}
 
 	/**
 	 * @param bookmark
 	 *            the bookmark to set
 	 */
 	public void setBookmark( String bookmark )
 	{
 		viewer.setBookmark( bookmark );
 	}
 
 	/**
 	 * @param reportletId
 	 *            the reportletId to set
 	 */
 	public void setReportletId( String reportletId )
 	{
 		viewer.setReportletId( reportletId );
 	}
 
 	/**
 	 * @param locale
 	 *            the locale to set
 	 */
 	public void setLocale( String locale )
 	{
 		viewer.setLocale( locale );
 	}
 
 	/**
 	 * @param format
 	 *            the format to set
 	 */
 	public void setFormat( String format )
 	{
 		viewer.setFormat( format );
 	}
 
 	/**
 	 * @param svg
 	 *            the svg to set
 	 */
 	public void setSvg( String svg )
 	{
 		viewer.setSvg( BirtTagUtil.convertBooleanValue( svg ) );
 	}
 
 	/**
 	 * @param rtl
 	 *            the rtl to set
 	 */
 	public void setRtl( String rtl )
 	{
 		viewer.setRtl( BirtTagUtil.convertBooleanValue( rtl ) );
 	}
 
 	/**
 	 * @param allowMasterPage
 	 *            the allowMasterPage to set
 	 */
 	public void setAllowMasterPage( String allowMasterPage )
 	{
 		viewer.setAllowMasterPage( BirtTagUtil
 				.convertBooleanValue( allowMasterPage ) );
 	}
 
 	/**
 	 * @param forceParameterPrompting
 	 *            the forceParameterPrompting to set
 	 */
 	public void setForceParameterPrompting( String forceParameterPrompting )
 	{
 		viewer.setForceParameterPrompting( BirtTagUtil
 				.convertBooleanValue( forceParameterPrompting ) );
 	}
 
 	/**
 	 * @param resourceFolder
 	 *            the resourceFolder to set
 	 */
 	public void setResourceFolder( String resourceFolder )
 	{
 		viewer.setResourceFolder( resourceFolder );
 	}
 }
