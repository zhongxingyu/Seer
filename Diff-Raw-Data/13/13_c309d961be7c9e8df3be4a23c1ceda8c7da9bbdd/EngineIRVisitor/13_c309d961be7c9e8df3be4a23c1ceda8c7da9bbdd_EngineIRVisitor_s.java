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
 
 package org.eclipse.birt.report.engine.parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.data.engine.api.IConditionalExpression;
 import org.eclipse.birt.data.engine.api.querydefn.ConditionalExpression;
 import org.eclipse.birt.report.engine.api.IParameterDefnBase;
 import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
 import org.eclipse.birt.report.engine.api.impl.ParameterGroupDefn;
 import org.eclipse.birt.report.engine.api.impl.ParameterSelectionChoice;
 import org.eclipse.birt.report.engine.api.impl.ScalarParameterDefn;
 import org.eclipse.birt.report.engine.ir.ActionDesign;
 import org.eclipse.birt.report.engine.ir.CellDesign;
 import org.eclipse.birt.report.engine.ir.ColumnDesign;
 import org.eclipse.birt.report.engine.ir.DataItemDesign;
 import org.eclipse.birt.report.engine.ir.DimensionType;
 import org.eclipse.birt.report.engine.ir.DrillThroughActionDesign;
 import org.eclipse.birt.report.engine.ir.EngineIRConstants;
 import org.eclipse.birt.report.engine.ir.Expression;
 import org.eclipse.birt.report.engine.ir.ExtendedItemDesign;
 import org.eclipse.birt.report.engine.ir.FreeFormItemDesign;
 import org.eclipse.birt.report.engine.ir.GraphicMasterPageDesign;
 import org.eclipse.birt.report.engine.ir.GridItemDesign;
 import org.eclipse.birt.report.engine.ir.GroupDesign;
 import org.eclipse.birt.report.engine.ir.HighlightDesign;
 import org.eclipse.birt.report.engine.ir.HighlightRuleDesign;
 import org.eclipse.birt.report.engine.ir.ImageItemDesign;
 import org.eclipse.birt.report.engine.ir.LabelItemDesign;
 import org.eclipse.birt.report.engine.ir.ListBandDesign;
 import org.eclipse.birt.report.engine.ir.ListGroupDesign;
 import org.eclipse.birt.report.engine.ir.ListItemDesign;
 import org.eclipse.birt.report.engine.ir.ListingDesign;
 import org.eclipse.birt.report.engine.ir.MapDesign;
 import org.eclipse.birt.report.engine.ir.MapRuleDesign;
 import org.eclipse.birt.report.engine.ir.MasterPageDesign;
 import org.eclipse.birt.report.engine.ir.MultiLineItemDesign;
 import org.eclipse.birt.report.engine.ir.PageSetupDesign;
 import org.eclipse.birt.report.engine.ir.Report;
 import org.eclipse.birt.report.engine.ir.ReportElementDesign;
 import org.eclipse.birt.report.engine.ir.ReportItemDesign;
 import org.eclipse.birt.report.engine.ir.RowDesign;
 import org.eclipse.birt.report.engine.ir.SimpleMasterPageDesign;
 import org.eclipse.birt.report.engine.ir.StyleDesign;
 import org.eclipse.birt.report.engine.ir.StyledElementDesign;
 import org.eclipse.birt.report.engine.ir.TableBandDesign;
 import org.eclipse.birt.report.engine.ir.TableGroupDesign;
 import org.eclipse.birt.report.engine.ir.TableItemDesign;
 import org.eclipse.birt.report.engine.ir.TextItemDesign;
 import org.eclipse.birt.report.engine.ir.VisibilityDesign;
 import org.eclipse.birt.report.engine.ir.VisibilityRuleDesign;
 import org.eclipse.birt.report.model.api.ActionHandle;
 import org.eclipse.birt.report.model.api.CellHandle;
 import org.eclipse.birt.report.model.api.ColumnHandle;
 import org.eclipse.birt.report.model.api.DataItemHandle;
 import org.eclipse.birt.report.model.api.DesignElementHandle;
 import org.eclipse.birt.report.model.api.DesignVisitor;
 import org.eclipse.birt.report.model.api.DimensionHandle;
 import org.eclipse.birt.report.model.api.ExtendedItemHandle;
 import org.eclipse.birt.report.model.api.FreeFormHandle;
 import org.eclipse.birt.report.model.api.GraphicMasterPageHandle;
 import org.eclipse.birt.report.model.api.GridHandle;
 import org.eclipse.birt.report.model.api.GroupHandle;
 import org.eclipse.birt.report.model.api.HideRuleHandle;
 import org.eclipse.birt.report.model.api.HighlightRuleHandle;
 import org.eclipse.birt.report.model.api.ImageHandle;
 import org.eclipse.birt.report.model.api.LabelHandle;
 import org.eclipse.birt.report.model.api.ListGroupHandle;
 import org.eclipse.birt.report.model.api.ListHandle;
 import org.eclipse.birt.report.model.api.ListingHandle;
 import org.eclipse.birt.report.model.api.MapRuleHandle;
 import org.eclipse.birt.report.model.api.MasterPageHandle;
 import org.eclipse.birt.report.model.api.MemberHandle;
 import org.eclipse.birt.report.model.api.ParamBindingHandle;
 import org.eclipse.birt.report.model.api.ParameterGroupHandle;
 import org.eclipse.birt.report.model.api.PropertyHandle;
 import org.eclipse.birt.report.model.api.ReportDesignHandle;
 import org.eclipse.birt.report.model.api.ReportElementHandle;
 import org.eclipse.birt.report.model.api.ReportItemHandle;
 import org.eclipse.birt.report.model.api.RowHandle;
 import org.eclipse.birt.report.model.api.ScalarParameterHandle;
 import org.eclipse.birt.report.model.api.SelectionChoiceHandle;
 import org.eclipse.birt.report.model.api.SimpleMasterPageHandle;
 import org.eclipse.birt.report.model.api.SlotHandle;
 import org.eclipse.birt.report.model.api.StructureHandle;
 import org.eclipse.birt.report.model.api.StyleHandle;
 import org.eclipse.birt.report.model.api.TableGroupHandle;
 import org.eclipse.birt.report.model.api.TableHandle;
 import org.eclipse.birt.report.model.api.TextDataHandle;
 import org.eclipse.birt.report.model.api.TextItemHandle;
 import org.eclipse.birt.report.model.api.core.UserPropertyDefn;
 import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
 import org.eclipse.birt.report.model.api.elements.structures.DateTimeFormatValue;
 import org.eclipse.birt.report.model.api.elements.structures.HighlightRule;
 import org.eclipse.birt.report.model.api.elements.structures.NumberFormatValue;
 import org.eclipse.birt.report.model.api.elements.structures.StringFormatValue;
 import org.eclipse.birt.report.model.api.metadata.DimensionValue;
 import org.eclipse.birt.report.model.api.util.ColorUtil;
 import org.eclipse.birt.report.model.api.util.StringUtil;
 import org.eclipse.birt.report.model.elements.Style;
 
 /**
  * Constructs an internal representation of the report design for report
  * geenration and presentation, based on the internal representation that design
  * engine creates. The DE IR services both the designer UI and factory, and has
  * certain features that are not quite suitable for FPE use. In particular, this
  * step of the reconstruction is needed for several reasons:
  * <p>
  * <li>Style handling: DE stores all styles in an unflatten version. Factory
  * needs to reference styles where the element hierarchy has been flattened.
  * <li>Faster lookup: DE stores various properties as property name/value
  * pairs. Factory IR might store them as structure. See
  * <code>createHighlightRule()</code> for an example.
  * <li>Merging properties: DE stores custom and default properties separately.
  * In FPE, they are merged.</li>
  * <p>
  * 
  * This class visits the Design Engine's IR to create a new IR for FPE. It is
  * usually used in the "Design Adaptation" phase of report generation, which is
  * also the first step in report generation after DE loads the report in.
  * 
 * @version $Revision: 1.47 $ $Date: 2005/07/11 05:55:42 $
  */
 class EngineIRVisitor extends DesignVisitor
 {
 
 	/**
 	 * logger used to log the error.
 	 */
 	protected static Logger logger = Logger.getLogger( EngineIRVisitor.class
 			.getName( ) );
 
 	/**
 	 * current report element created by visitor
 	 */
 	protected Object currentElement;
 
 	/**
 	 * default unit used by this report
 	 */
 	protected String defaultUnit;
 
 	/**
 	 * Factory IR created by this visitor
 	 */
 	protected Report report;
 
 	/**
 	 * report design handle
 	 */
 	protected ReportDesignHandle handle;
 	
 	/**
 	 * non-inheritable report style
 	 */
 	protected StyleDesign nonInheritableReportStyle;
 	
 	/**
 	 * inheritable report style
 	 */
 	protected StyleDesign inheritableReportStyle;
 
 	private static int STYLE_FLAG_NONE = 0;
 	private static int STYLE_FLAG_INHERITABLE = 1;
 	private static int STYLE_FLAG_PRIVATE = 2;
 	private static int STYLE_FLAG_ALL = 3;
 	/**
 	 * constructor
 	 * 
 	 * @param handle
 	 *            the entry point to the DE report design IR
 	 *  
 	 */
 	EngineIRVisitor( ReportDesignHandle handle )
 	{
 		super( );
 		this.handle = handle;
 	}
 
 	/**
 	 * translate the DE's IR to FPE's IR.
 	 * 
 	 * @return FPE's IR.
 	 */
 	public Report translate( )
 	{
 		report = new Report( );
 		report.setReportDesign( handle.getDesign( ) );
 		apply( handle );
 		return report;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.birt.report.model.api.DesignVisitor#visitReportDesign(org.eclipse.birt.report.model.api.ReportDesignHandle)
 	 */
 	public void visitReportDesign( ReportDesignHandle handle )
 	{
 		report.setUnit( handle.getDefaultUnits( ) );
 		if ( handle.getBase( ) != null && !handle.getBase( ).equals( "" ) ) //$NON-NLS-1$
 		{
 			report.setBasePath( handle.getBase( ) );
 		}
 		defaultUnit = report.getUnit( );
 
 		// INCLUDE LIBRARY
 		// INCLUDE SCRIPT
 		// CODE MODULES
 
 		// PARAMETERS: Handle parameter definitions
 		SlotHandle paramSlot = handle.getParameters( );
 		IParameterDefnBase param;
 		for ( int i = 0; i < paramSlot.getCount( ); i++ )
 		{
 			apply( paramSlot.get( i ) );
 			assert ( currentElement != null );
 			param = (IParameterDefnBase) currentElement;
 			report.addParameter( param );
 		}
 
 		// Sets the report default style
 		StyleHandle reportStyleHandle = handle.findStyle( "report" );//$NON-NLS-1$		
 		createReportStyles( reportStyleHandle );
 		report.setDefaultStyles( nonInheritableReportStyle, inheritableReportStyle );
 
 		//COLOR-PALETTE
 		//METHOD
 		//STYLES
 		// We needn't handle the style slot, it will be handled for each
 		// element.
 
 		// Handle Master Page
 		PageSetupDesign pageSetup = new PageSetupDesign( );
 		SlotHandle pageSlot = handle.getMasterPages( );
 		for ( int i = 0; i < pageSlot.getCount( ); i++ )
 		{
 			apply( pageSlot.get( i ) );
 			assert ( currentElement != null );
 			pageSetup.addMasterPage( (MasterPageDesign) currentElement );
 		}
 		//FIXME: add page sequence support
 		// Handle Page Sequence
 		//		SlotHandle seqSlot = handle.getPageSequences( );
 		//		for ( int i = 0; i < seqSlot.getCount( ); i++ )
 		//		{
 		//			apply( seqSlot.get( i ) );
 		//			assert ( currentElement != null );
 		//			pageSetup.addPageSequence( (PageSequenceDesign) currentElement );
 		//		}
 
 		report.setPageSetup( pageSetup );
 
 		//COMPONENTS
 
 		// Handle Report Body
 		SlotHandle bodySlot = handle.getBody( );
 		for ( int i = 0; i < bodySlot.getCount( ); i++ )
 		{
 			apply( bodySlot.get( i ) );
 			assert ( currentElement != null );
 			report.addContent( (ReportItemDesign) currentElement );
 		}
 
 		//SCRATCH-PAD
 		//CONFIG-VARS
 		//TRANSLATIONS
 		//IMAGES
 		//CUSTOM
 	}
 
 	/**
 	 * setup the master page object from the base master page handle.
 	 * 
 	 * @param page
 	 *            page object
 	 * @param handle
 	 *            page handle
 	 */
 	private void setupMasterPage( MasterPageDesign page, MasterPageHandle handle )
 	{
 		setupStyledElement( page, handle );
 		setupContentStyle( page );
 		
 		page.setPageType( handle.getPageType( ) );
 
 		// Master page width and height
 		DimensionValue effectWidth = handle.getEffectiveWidth( );
 		DimensionValue effectHeight = handle.getEffectiveHeight( );
 		DimensionType width = null;
 		DimensionType height = null;
 		if ( effectWidth != null )
 		{
 			width = new DimensionType( effectWidth.getMeasure( ), effectWidth
 					.getUnits( ) );
 		}
 		if ( effectHeight != null )
 		{
 			height = new DimensionType( effectHeight.getMeasure( ),
 					effectHeight.getUnits( ) );
 		}
 		page.setPageSize( width, height );
 		page.setOrientation( handle.getOrientation( ) );
 
 		// Master page margins
 		DimensionType top = createDimension( handle.getTopMargin( ) );
 		DimensionType left = createDimension( handle.getLeftMargin( ) );
 		DimensionType bottom = createDimension( handle.getBottomMargin( ) );
 		DimensionType right = createDimension( handle.getRightMargin( ) );
 		page.setMargin( top, left, bottom, right );
 	}
 
 	public void visitGraphicMasterPage( GraphicMasterPageHandle handle )
 	{
 		GraphicMasterPageDesign page = new GraphicMasterPageDesign( );
 
 		setupMasterPage( page, handle );
 
 		// Multi-column properties
 		page.setColumns( handle.getColumnCount( ) );
 		DimensionType spacing = createDimension( handle.getColumnSpacing( ) );
 		page.setColumnSpacing( spacing );
 
 		// Master page content
 		SlotHandle contentSlot = handle.getContent( );
 		for ( int i = 0; i < contentSlot.getCount( ); i++ )
 		{
 			apply( contentSlot.get( i ) );
 			assert ( currentElement != null );
 			page.addContent( (ReportItemDesign) currentElement );
 		}
 
 		currentElement = page;
 	}
 
 	public void visitSimpleMasterPage( SimpleMasterPageHandle handle )
 	{
 		SimpleMasterPageDesign page = new SimpleMasterPageDesign( );
 
 		//setup the base master page property.
 		setupMasterPage( page, handle );
 
 		page.setHeaderHeight( createDimension( handle.getHeaderHeight( ) ) );
 		page.setFooterHeight( createDimension( handle.getFooterHeight( ) ) );
 
 		SlotHandle headerSlot = handle.getPageHeader( );
 		for ( int i = 0; i < headerSlot.getCount( ); i++ )
 		{
 			apply( headerSlot.get( i ) );
 			assert ( currentElement != null );
 			page.addHeader( (ReportItemDesign) currentElement );
 		}
 
 		SlotHandle footerSlot = handle.getPageFooter( );
 		for ( int i = 0; i < footerSlot.getCount( ); i++ )
 		{
 			apply( footerSlot.get( i ) );
 			assert ( currentElement != null );
 			page.addFooter( (ReportItemDesign) currentElement );
 		}
 
 		currentElement = page;
 	}
 
 	public void visitList( ListHandle handle )
 	{
 		// Create ListItem
 		ListItemDesign listItem = new ListItemDesign( );
 		setupListingItem( listItem, handle );
 
 		// Header
 		SlotHandle headerSlot = handle.getHeader( );
 		listItem.setHeader( createListBand( headerSlot ) );
 
 		// Multiple groups
 		SlotHandle groupsSlot = handle.getGroups( );
 		for ( int i = 0; i < groupsSlot.getCount( ); i++ )
 		{
 			apply( groupsSlot.get( i ) );
 			listItem.addGroup( (ListGroupDesign) currentElement );
 		}
 
 		// List detail
 		SlotHandle detailSlot = handle.getDetail( );
 		listItem.setDetail( createListBand( detailSlot ) );
 
 		// List Footer
 		SlotHandle footerSlot = handle.getFooter( );
 		listItem.setFooter( createListBand( footerSlot ) );
 
 		currentElement = listItem;
 	}
 
 	public void visitFreeForm( FreeFormHandle handle )
 	{
 		// Create Free form element
 		FreeFormItemDesign container = new FreeFormItemDesign( );
 		setupReportItem( container, handle );
 
 		// Set up each individual item in a free form container
 		SlotHandle slot = handle.getReportItems( );
 		for ( int i = 0; i < slot.getCount( ); i++ )
 		{
 			apply( slot.get( i ) );
 			container.addItem( (ReportItemDesign) currentElement );
 		}
 
 		currentElement = container;
 	}
 
 	public void visitTextDataItem( TextDataHandle handle )
 	{
 		MultiLineItemDesign multiLineItem = new MultiLineItemDesign( );
 
 		setupReportItem( multiLineItem, handle );
 
 		String valueExpr = handle.getValueExpr( );
 		String typeExpr = handle.getContentTypeExpr( );
 		assert ( valueExpr != null );
 		multiLineItem.setContent( new Expression( valueExpr ) );
 		if ( typeExpr != null )
 		{
 			multiLineItem.setContentType( new Expression( typeExpr ) );
 		}
 		setHighlight( multiLineItem, valueExpr );
 		setMap( multiLineItem, valueExpr );
 
 		currentElement = multiLineItem;
 	}
 
 	public void visitParameterGroup( ParameterGroupHandle handle )
 	{
 		ParameterGroupDefn paramGroup = new ParameterGroupDefn( );
 		paramGroup.setHandle(handle);
 		paramGroup.setParameterType(IParameterDefnBase.PARAMETER_GROUP);
 		paramGroup.setName(handle.getName());
 		paramGroup.setDisplayName( handle.getDisplayName( ) );
 		paramGroup.setDisplayNameKey( handle.getDisplayNameKey( ) );
 		paramGroup.setHelpText( handle.getHelpText( ) );
 		paramGroup.setHelpTextKey( handle.getHelpTextKey( ) );		
 		SlotHandle parameters = handle.getParameters( );
 		
 		//set custom properties
 		List properties = handle.getUserProperties();
 		for(int i=0; i<properties.size(); i++)
 		{
 			UserPropertyDefn p = (UserPropertyDefn)properties.get(i);
 			paramGroup.addUserProperty(p.getName(),handle.getProperty(p.getName()) );
 		}
 		
 		int size = parameters.getCount( );
 		for ( int n = 0; n < size; n++ )
 		{
 			apply( parameters.get( n ) );
 			paramGroup.addParameter( (IParameterDefnBase) currentElement );
 		}
 
 		currentElement = paramGroup;
 	}
 
 	public void visitScalarParameter( ScalarParameterHandle handle )
 	{
 		assert ( handle.getName( ) != null );
 		// Create Parameter
 		ScalarParameterDefn scalarParameter = new ScalarParameterDefn( );
 		scalarParameter.setHandle(handle);
 		scalarParameter.setParameterType(IParameterDefnBase.SCALAR_PARAMETER);
 		scalarParameter.setName(handle.getName());
 		
 		//set custom properties
 		List properties = handle.getUserProperties();
 		for(int i=0; i<properties.size(); i++)
 		{
 			UserPropertyDefn p = (UserPropertyDefn)properties.get(i);
 			scalarParameter.addUserProperty(p.getName(),handle.getProperty(p.getName()) );
 		}
 		String align = handle.getAlignment();
 		if(DesignChoiceConstants.SCALAR_PARAM_ALIGN_CENTER.equals(align))
 			scalarParameter.setAlignment( IScalarParameterDefn.CENTER );
 		else if(DesignChoiceConstants.SCALAR_PARAM_ALIGN_LEFT.equals(align))
 			scalarParameter.setAlignment( IScalarParameterDefn.LEFT );
 		else if(DesignChoiceConstants.SCALAR_PARAM_ALIGN_RIGHT.equals(align))
 			scalarParameter.setAlignment( IScalarParameterDefn.RIGHT );
 		else 
 			scalarParameter.setAlignment( IScalarParameterDefn.AUTO );
 		
 		scalarParameter.setAllowBlank( handle.allowBlank( ));
 		scalarParameter.setAllowNull( handle.allowNull( ));
 		
 		String controlType = handle.getControlType( );
 		if(DesignChoiceConstants.PARAM_CONTROL_CHECK_BOX.equals(controlType))
 			scalarParameter.setControlType(IScalarParameterDefn.CHECK_BOX );
 		else if(DesignChoiceConstants.PARAM_CONTROL_LIST_BOX.equals(controlType))
 			scalarParameter.setControlType(IScalarParameterDefn.LIST_BOX);
 		else if(DesignChoiceConstants.PARAM_CONTROL_RADIO_BUTTON.equals(controlType))
 			scalarParameter.setControlType(IScalarParameterDefn.RADIO_BUTTON);
 		else 
 			scalarParameter.setControlType(IScalarParameterDefn.TEXT_BOX);
 		
 		scalarParameter.setDefaultValueExpr( handle.getDefaultValue( ) );
 		scalarParameter.setDisplayName( handle.getDisplayName( ) );
 		scalarParameter.setDisplayNameKey( handle.getDisplayNameKey( ) );
 		
 		scalarParameter.setFormat( handle.getFormat( ) );
 		scalarParameter.setHelpText( handle.getHelpText( ) );
 		scalarParameter.setHelpTextKey( handle.getHelpTextKey( ) );
 		scalarParameter.setIsHidden( handle.isHidden( ) );
 		scalarParameter.setName( handle.getName( ) );
 
 		String valueType = handle.getDataType( );
 		if ( DesignChoiceConstants.PARAM_TYPE_BOOLEAN.equals( valueType ) )
 			scalarParameter.setDataType( IScalarParameterDefn.TYPE_BOOLEAN );
 		else if ( DesignChoiceConstants.PARAM_TYPE_DATETIME.equals( valueType ) )
 			scalarParameter.setDataType( IScalarParameterDefn.TYPE_DATE_TIME );
 		else if ( DesignChoiceConstants.PARAM_TYPE_DECIMAL.equals( valueType ) )
 			scalarParameter.setDataType( IScalarParameterDefn.TYPE_DECIMAL );
 		else if ( DesignChoiceConstants.PARAM_TYPE_FLOAT.equals( valueType ) )
 			scalarParameter.setDataType( IScalarParameterDefn.TYPE_FLOAT );
 		else if ( DesignChoiceConstants.PARAM_TYPE_STRING.equals( valueType ) )
 			scalarParameter.setDataType( IScalarParameterDefn.TYPE_STRING );
 		else
 			scalarParameter.setDataType( IScalarParameterDefn.TYPE_ANY );
 		
 		ArrayList values = new ArrayList( );
 		Iterator selectionIter = handle.choiceIterator( );
 		while ( selectionIter.hasNext( ) )
 		{
 			SelectionChoiceHandle selection = (SelectionChoiceHandle) selectionIter.next( );
 			ParameterSelectionChoice selectionChoice = new ParameterSelectionChoice( handle.getDesign( ) );
 			selectionChoice.setLabel( selection.getLabelKey( ), selection.getLabel( ) );
 			selectionChoice.setValue( selection.getValue( ), scalarParameter.getDataType() );
 			values.add( selectionChoice );
 		}
 		scalarParameter.setSelectionList(values);
 		scalarParameter.setAllowNewValues(!handle.isMustMatch());
 		scalarParameter.setFixedOrder(handle.isFixedOrder());
 		
 		if(scalarParameter.getSelectionList() != null && scalarParameter.getSelectionList().size() > 0)
 			scalarParameter.setSelectionListType(IScalarParameterDefn.SELECTION_LIST_STATIC);
 		else
 			scalarParameter.setSelectionListType(IScalarParameterDefn.SELECTION_LIST_NONE);
 		
 		scalarParameter.setValueConcealed( handle.isConcealValue( ) );
 		currentElement = scalarParameter;
 	}
 
 	public void visitLabel( LabelHandle handle )
 	{
 		// Create Label Item
 		LabelItemDesign labelItem = new LabelItemDesign( );
 		setupReportItem( labelItem, handle );
 
 		// Text
 		String text = handle.getText( );
 		String textKey = handle.getTextKey( );
 
 		labelItem.setText( textKey, text );
 
 		// Handle Action
 		ActionHandle action = handle.getActionHandle( );
 		if ( action != null )
 		{
 			labelItem.setAction( createAction( action ) );
 		}
 		// Fill in help text
 		labelItem.setHelpText( handle.getHelpTextKey( ), handle.getHelpText( ) );
 
 		currentElement = labelItem;
 	}
 
 	public void visitDataItem( DataItemHandle handle )
 	{
 		// Create data item
 		DataItemDesign data = new DataItemDesign( );
 		setupReportItem( data, handle );
 		
 		// Fill in data expression
 		String expr = handle.getValueExpr( );
 		if ( expr != null )
 		{
 			data.setValue( new Expression( expr ) );
 		}
 		// Handle Action
 		ActionHandle action = handle.getActionHandle( );
 		if ( action != null )
 		{
 			data.setAction( createAction( action ) );
 		}
 
 		// Fill in help text
 		data.setHelpText( handle.getHelpTextKey( ), handle.getHelpText( ) );
 
 		setHighlight( data, expr );
 		setMap( data, expr );
 		currentElement = data;
 	}
 
 	public void visitGrid( GridHandle handle )
 	{
 		// Create Grid Item
 		GridItemDesign grid = new GridItemDesign( );
 		setupReportItem( grid, handle );
 
 		// Handle Columns
 		SlotHandle columnSlot = handle.getColumns();
 		for ( int i = 0; i < columnSlot.getCount( ); i++ )
 		{
 			apply( columnSlot.get( i ) );
 			assert currentElement != null;
 			grid.addColumn( (ColumnDesign) currentElement );
 		}
 		//column slot may be empty
 		if ( columnSlot.getCount( ) == 0 )
 		{
 			ColumnDesign column = new ColumnDesign( );
 			column.setRepeat( handle.getColumnCount( ) );
 			grid.addColumn( column );
 		}
 
 		// Handle Rows
 		SlotHandle rowSlot = handle.getRows();
 		for ( int i = 0; i < rowSlot.getCount( ); i++ )
 		{
 			apply( rowSlot.get( i ) );
 			grid.addRow( (RowDesign) currentElement );
 		}
 
 		currentElement = grid;
 	}
 
 	public void visitImage( ImageHandle handle )
 	{
 		// Create Image Item
 		ImageItemDesign image = new ImageItemDesign( );
 		setupReportItem( image, handle );
 		
 		// Handle Action
 		ActionHandle action = handle.getActionHandle( );
 		if ( action != null )
 		{
 			image.setAction( createAction( action ) );
 		}
 
 		// Alternative text for image
 		image.setAltText( handle.getAltTextKey( ), handle.getAltText( ) );
 
 		// Help text for image
 		image.setHelpText( handle.getHelpTextKey( ), handle.getHelpText( ) );
 
 		// Handle Image Source
 		String imageSrc = handle.getSource( );
 
 		if ( EngineIRConstants.IMAGE_REF_TYPE_URL.equals( imageSrc ) )
 		{
 			image.setImageUri( new Expression(handle.getURI( )) );
 		}
 		else if ( EngineIRConstants.IMAGE_REF_TYPE_EXPR.equals( imageSrc ) )
 		{
 			image.setImageExpression( new Expression( handle
					.getValueExpression( ) ), new Expression( handle
					.getTypeExpression( ) ) );
 		}
 		else if ( EngineIRConstants.IMAGE_REF_TYPE_EMBED.equals( imageSrc ) )
 		{
 			image.setImageName( handle.getImageName( ) );
 		}
 		else if ( EngineIRConstants.IMAGE_REF_TYPE_FILE.equals( imageSrc ) )
 		{
 			image.setImageFile(new Expression( handle.getURI( )) );
 		}
 		else
 		{
 			assert false;
 		}
 
 		currentElement = image;
 	}
 
 	public void visitTable( TableHandle handle )
 	{
 		// Create Table Item
 		TableItemDesign table = new TableItemDesign( );
 		table.setRepeatHeader( handle.repeatHeader( ) );
 
 		setupListingItem( table, handle );
 
 		// Handle table caption
 		String caption = handle.getCaption( );
 		String captionKey = handle.getCaptionKey( );
 		if ( caption != null || captionKey != null )
 		{
 			table.setCaption( captionKey, caption );
 		}
 
 		// Handle table Columns
 		SlotHandle columnSlot = handle.getColumns( );
 		for ( int i = 0; i < columnSlot.getCount( ); i++ )
 		{
 			apply( columnSlot.get( i ) );
 			assert currentElement != null;
 			table.addColumn( (ColumnDesign) currentElement );
 		}
 
 		//column slot may be empty
 		if ( columnSlot.getCount( ) == 0 && handle.getColumnCount( ) != 0 )
 		{
 			ColumnDesign column = new ColumnDesign( );
 			column.setRepeat( handle.getColumnCount( ) );
 			table.addColumn( column );
 		}
 
 		// Handle Table Header
 		SlotHandle headerSlot = handle.getHeader( );
 		TableBandDesign header = createTableBand( headerSlot );
 		table.setHeader( header );
 
 		// Handle grouping in table
 		SlotHandle groupSlot = handle.getGroups( );
 		for ( int i = 0; i < groupSlot.getCount( ); i++ )
 		{
 			apply( groupSlot.get( i ) );
 			table.addGroup( (TableGroupDesign) currentElement );
 		}
 
 		// Handle detail section
 		SlotHandle detailSlot = handle.getDetail( );
 		TableBandDesign detail = createTableBand( detailSlot );
 		table.setDetail( detail );
 
 		// Handle table footer
 		SlotHandle footerSlot = handle.getFooter( );
 		TableBandDesign footer = createTableBand( footerSlot );
 		table.setFooter( footer );
 		
 		new TableItemDesignLayout().layout(table);
 
 		currentElement = table;
 	}
 
 	public void visitColumn( ColumnHandle handle )
 	{
 		// Create a Column, mostly used in Table or Grid
 		ColumnDesign col = new ColumnDesign( );
 		setupStyledElement( col, handle, STYLE_FLAG_PRIVATE );
 
 		//TODO we need handle the column alignment
 
 		// Column Repetition
 		col.setRepeat( handle.getRepeatCount( ) );
 
 		// Column Width
 		DimensionType width = createDimension( handle.getWidth( ) );
 		col.setWidth( width );
 
 		currentElement = col;
 	}
 
 	public void visitRow( RowHandle handle )
 	{
 		// Create a Row, mostly used in Table and Grid Item
 		RowDesign row = new RowDesign( );
 		setupStyledElement( row, handle );
 
 		// Row Height
 		DimensionType height = createDimension( handle.getHeight( ) );
 		row.setHeight( height );
 
 		// Book mark
 		String bookmark = handle.getBookmark( );
 		if ( bookmark != null )
 		{
 			row.setBookmark( new Expression( bookmark ) );
 		}
 
 		// Visibility
 		VisibilityDesign visibility = createVisibility( handle
 				.visibilityRulesIterator( ) );
 		row.setVisibility( visibility );
 
 		// Cells in a row
 		SlotHandle cellSlot = handle.getCells();
 		for ( int i = 0; i < cellSlot.getCount( ); i++ )
 		{
 			apply( cellSlot.get( i ) );
 			row.addCell( (CellDesign) currentElement );
 		}
 
 		currentElement = row;
 	}
 
 	/**
 	 * Sets up cell element's style attribute.
 	 * 
 	 * @param cell
 	 *            engine's styled cell element.
 	 * @param handle
 	 *            DE's styled cell element.
 	 */
 	protected void setupStyledElement( StyledElementDesign design, ReportElementHandle handle )
 	{
 		setupStyledElement( design, handle, STYLE_FLAG_ALL );
 	}
 	
 	protected void setupStyledElement( StyledElementDesign design, ReportElementHandle handle, int flag )
 	{
 		DesignElementHandle parentHandle = handle;
 		do
 		{
 			parentHandle = parentHandle.getContainer( );
 		}
 		while( this.handle != parentHandle && !parentHandle.getDefn( ).hasStyle( ) );
 		
 		// Styled element is a report element
 		setupReportElement( design, handle );
 
 		StyleDesign style = createDistinctStyle( handle, parentHandle, flag );
 		if (style != null)
 		{
 			design.setStyle(style);
 		}
 	}
 	
 	public void visitCell( CellHandle handle )
 	{
 		// Create a Cell
 		CellDesign cell = new CellDesign( );
 		setupStyledElement( cell, handle );
 
 		// Cell contents
 		SlotHandle contentSlot = handle.getContent();
 		for ( int i = 0; i < contentSlot.getCount( ); i++ )
 		{
 			apply( contentSlot.get( i ) );
 			cell.addContent( (ReportItemDesign) currentElement );
 		}
 
 		// Span, Drop properties of a cell
 		cell.setColSpan( handle.getColumnSpan( ) );
 		cell.setColumn( handle.getColumn( ) );
 		cell.setRowSpan( handle.getRowSpan( ) );
 		cell.setDrop( handle.getDrop( ) );
 
 		currentElement = cell;
 	}
 
 	/**
 	 * create a list band using the items in slot.
 	 * 
 	 * @param elements
 	 *            items in DE's IR
 	 * @return ListBand.
 	 */
 	private ListBandDesign createListBand( SlotHandle elements )
 	{
 		ListBandDesign band = new ListBandDesign( );
 
 		for ( int i = 0; i < elements.getCount( ); i++ )
 		{
 			apply( elements.get( i ) );
 			assert ( currentElement != null );
 			band.addContent( (ReportItemDesign) currentElement );
 		}
 
 		return band;
 	}
 
 	/**
 	 * create a list group using the DE's ListGroup.
 	 * 
 	 * @param handle
 	 *            De's list group
 	 * @return engine's list group
 	 */
 	public void visitListGroup( ListGroupHandle handle )
 	{
 		ListGroupDesign listGroup = new ListGroupDesign( );
 
 		setupGroup( listGroup, handle );
 
 		ListBandDesign header = createListBand( handle.getHeader( ) );
 		listGroup.setHeader( header );
 
 		ListBandDesign footer = createListBand( handle.getFooter( ) );
 		listGroup.setFooter( footer );
 
 		currentElement = listGroup;
 	}
 
 	/**
 	 * create a table group using the DE's TableGroup.
 	 * 
 	 * @param handle
 	 *            De's table group
 	 * @return engine's table group
 	 */
 	public void visitTableGroup( TableGroupHandle handle )
 	{
 		TableGroupDesign tableGroup = new TableGroupDesign( );
 
 		setupGroup( tableGroup, handle );
 
 		TableBandDesign header = createTableBand( handle.getHeader( ) );
 		tableGroup.setHeader( header );
 
 		TableBandDesign footer = createTableBand( handle.getFooter( ) );
 		tableGroup.setFooter( footer );
 
 		currentElement = tableGroup;
 	}
 
 	public void visitTextItem( TextItemHandle handle )
 	{
 		// Create Text Item
 		TextItemDesign textItem = new TextItemDesign( );
 		setupReportItem( textItem, handle );
 
 		String contentType = handle.getContentType( );
 		if ( contentType != null )
 		{
 			textItem.setTextType( contentType );
 		}
 		textItem.setText( handle.getContentKey( ), handle.getContent( ) );
 
 		currentElement = textItem;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.birt.report.model.api.DesignVisitor#visitExtendedItem(org.eclipse.birt.report.model.api.ExtendedItemHandle)
 	 */
 	protected void visitExtendedItem( ExtendedItemHandle obj )
 	{
 		ExtendedItemDesign extendedItem = new ExtendedItemDesign( );
 		setupReportItem( extendedItem, obj );
 
 		currentElement = extendedItem;
 	}
 
 	protected void setupGroup( GroupDesign group, GroupHandle handle )
 	{
 
 		//name
 		group.setName( handle.getName( ) );
 		//on-start
 		group.setOnStart( handle.getOnFinish( ) );
 		//on-row
 		group.setOnRow( handle.getOnRow( ) );
 		//on-finish
 		group.setOnFinish( handle.getOnFinish( ) );
 	}
 
 	/**
 	 * create a table band using the items in slot.
 	 * 
 	 * @param elements
 	 *            items in DE's IR
 	 * @return TableBand.
 	 */
 	private TableBandDesign createTableBand( SlotHandle elements )
 	{
 		TableBandDesign band = new TableBandDesign( );
 
 		for ( int i = 0; i < elements.getCount( ); i++ )
 		{
 			apply( elements.get( i ) );
 			band.addRow( (RowDesign) currentElement );
 		}
 
 		return band;
 	}
 
 	/**
 	 * Creates the property visibility
 	 * 
 	 * @param visibilityRulesIterator
 	 *            the handle's rules iterator
 	 * @return null only if the iterator is null or it contains no rules,
 	 *         otherwise VisibilityDesign
 	 */
 	protected VisibilityDesign createVisibility(
 			Iterator visibilityRulesIterator )
 	{
 		if ( visibilityRulesIterator != null )
 		{
 			VisibilityDesign visibility = new VisibilityDesign( );
 			while ( visibilityRulesIterator.hasNext( ) )
 			{
 				VisibilityRuleDesign hide = createHide( (HideRuleHandle) visibilityRulesIterator
 						.next( ) );
 				visibility.addRule( hide );
 			}
 			if ( visibility.count( ) == 0 )
 			{
 				return null;
 			}
 			return visibility;
 		}
 		return null;
 	}
 
 	/**
 	 * Creates the visibility rule( i.e. the hide)
 	 * 
 	 * @param handle
 	 *            the DE's handle
 	 * @return the created visibility rule
 	 */
 	protected VisibilityRuleDesign createHide( HideRuleHandle handle )
 	{
 		VisibilityRuleDesign rule = new VisibilityRuleDesign( );
 		rule.setExpression( new Expression( handle.getExpression( ) ) );
 
 		String format = handle.getFormat( );
 		if ( EngineIRConstants.FORMAT_TYPE_ALL.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_ALL );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_EMAIL.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_EMAIL );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_EXCEL.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_EXCEL );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_PDF.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_PDF );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_POWERPOINT
 				.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_POWERPOINT );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_PRINT.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_PRINT );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_REPORTLET
 				.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_REPORTLET );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_RTF.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_RTF );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_VIEWER
 				.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_VIEWER );
 		}
 		else if ( EngineIRConstants.FORMAT_TYPE_WORD.equalsIgnoreCase( format ) )
 		{
 			rule.setFormat( VisibilityDesign.FORMAT_TYPE_WORD );
 		}
 		return rule;
 	}
 
 	/**
 	 * setup the attribute of report item
 	 * 
 	 * @param item
 	 *            Engine's Report Item
 	 * @param handle
 	 *            DE's report item.
 	 */
 	private void setupReportItem( ReportItemDesign item, ReportItemHandle handle )
 	{
 		setupStyledElement( item, handle );
 
 		// x, y, width & height
 		DimensionType height = createDimension( handle.getHeight( ) );
 		DimensionType width = createDimension( handle.getWidth( ) );
 		DimensionType x = createDimension( handle.getX( ) );
 		DimensionType y = createDimension( handle.getY( ) );
 		item.setHeight( height );
 		item.setWidth( width );
 		item.setX( x );
 		item.setY( y );
 
 		//setup book mark
 		String bookmark = handle.getBookmark( );
 		if ( bookmark != null )
 		{
 			item.setBookmark( new Expression( bookmark ) );
 		}
 		String onCreate = handle.getOnCreate( );
 		if (onCreate != null)
 		{
 			item.setOnCreate( new Expression(onCreate) );
 		}
 		item.setOnRender( handle.getOnRender( ) );
 
 		//Sets up the visibility
 		Iterator visibilityIter = handle.visibilityRulesIterator( );
 		VisibilityDesign visibility = createVisibility( visibilityIter );
 		item.setVisibility( visibility );
 	}
 
 	/**
 	 * Checks if a given style is in report's style list, if not,
 	 * assign a unique name to it and then add it to the style list.
 	 * 
 	 * @param style The <code>StyleDesign</code> object.
 	 */
 	private void assignStyleName( StyleDesign style )
 	{
 		// Check if the style is already in report's style list
 		for ( int i = 0; i < report.getStyleCount( ); i++ )
 		{
 			//Cast the type mandatorily
 			StyleDesign cachedStyle = (StyleDesign) report.getStyle( i );
 			if ( cachedStyle.equals( style ) )
 			{
 				//There exist a style which has same properties with this
 				// one,
 				style = cachedStyle;
 				break;
 			}
 		}
 
 		if ( style.getName( ) == null )
 		{
 			//the style is a new style, we need create a unique name for
 			// it, and
 			//add it into the report's style list.
 			style.setName( "style_" + report.getStyleCount( ) ); //$NON-NLS-1$
 			report.addStyle( style );
 		}
 	}
 
 	/**
 	 * setup report element attribute
 	 * 
 	 * @param elem
 	 *            engine's report element
 	 * @param handle
 	 *            DE's report element
 	 */
 	private void setupReportElement( ReportElementDesign element,
 			DesignElementHandle handle )
 	{
 		element.setHandle( handle );
 		element.setName( handle.getName( ) );
 		element.setID( handle.getID( ) );
 		DesignElementHandle extend = handle.getExtends( );
 		if ( extend != null )
 		{
 			element.setExtends( extend.getName( ) );
 		}
 		//handle the properties
 		Iterator iter = handle.getPropertyIterator( );
 		if ( iter != null )
 		{
 			PropertyHandle propHandle = (PropertyHandle) iter.next( );
 			if ( propHandle != null && propHandle.isSet( ) )
 			{
 				String name = propHandle.getDefn( ).getName( );
 				Object value = propHandle.getValue( );
 				assert name != null;
 				assert value != null;
 				Map properties = element.getCustomProperties( );
 				assert properties != null;
 				properties.put( name, value );
 			}
 		}
 	}
 
 	/**
 	 * create a Action.
 	 * 
 	 * @param handle
 	 *            action in DE
 	 * @return action in Engine.
 	 */
 	protected ActionDesign createAction( ActionHandle handle )
 	{
 		ActionDesign action = new ActionDesign( );
 		String linkType = handle.getLinkType( );
 		if ( EngineIRConstants.ACTION_LINK_TYPE_HYPERLINK.equals( linkType ) )
 		{
 			action.setHyperlink( new Expression( handle.getURI() ) );
 			action.setTargetWindow( handle.getTargetWindow( ) );
 		}
 		else if ( EngineIRConstants.ACTION_LINK_TYPE_BOOKMARK_LINK
 				.equals( linkType ) )
 		{
 			action.setBookmark( new Expression( handle.getTargetBookmark() ) );
 		}
 		else if ( EngineIRConstants.ACTION_LINK_TYPE_DRILL_THROUGH
 				.equals( linkType ) )
 		{
 			action.setTargetWindow( handle.getTargetWindow( ) );
 
 			DrillThroughActionDesign drillThrough = new DrillThroughActionDesign( );
 			action.setDrillThrough( drillThrough );
 
 			drillThrough.setReportName( handle.getReportName( ) );
 			drillThrough.setBookmark( new Expression( handle
 					.getTargetBookmark( ) ) );
 			Map params = new HashMap( );
 			Iterator paramIte = handle.paramBindingsIterator( );
 			while ( paramIte.hasNext( ) )
 			{
 				ParamBindingHandle member = (ParamBindingHandle) paramIte
 						.next( );
 				params.put( member.getParamName( ), new Expression( member
 						.getExpression( ) ) );
 			}
 			drillThrough.setParameters( params );
 			//XXX Search criteria is not supported yet.
 			//			Map search = new HashMap( );
 			//			Iterator searchIte = handle.searchIterator( );
 			//			while ( searchIte.hasNext( ) )
 			//			{
 			//				SearchKeyHandle member = (SearchKeyHandle) paramIte.next( );
 			//				params
 			//						.put( member., member
 			//								.getValue( ) );
 			//			}
 			//			drillThrough.setSearch( search );
 			
 		}
 		else
 		{
 			assert ( false );
 		}
 
 		return action;
 	}
 
 	/**
 	 * create a highlight rule from a structure handle.
 	 * 
 	 * @param ruleHandle
 	 *            rule in the MODEL.
 	 * @return rule design, null if exist any error.
 	 */
 	protected HighlightRuleDesign createHighlightRule(
 			StructureHandle ruleHandle, String defaultStr )
 	{
 		HighlightRuleDesign rule = new HighlightRuleDesign( );
 
 		//all other properties are style properties,
 		//copy those properties into a style design.
 		StyleDesign style = new StyleDesign( );
 		Iterator propIter = ruleHandle.iterator( );
 		//strange feature, we may get a null iterator
 		//perhaps there is no style data associated with this rule.
 		//so just return null.
 		if ( propIter == null )
 		{
 			return null;
 		}
 
 		String oper = null;
 		String value1 = null;
 		String value2 = null;
 		String testExpr = null;
 
 		while ( propIter.hasNext( ) )
 		{
 			MemberHandle member = (MemberHandle) propIter.next( );
 			assert member != null;
 			String propName = member.getDefn( ).getName( );
 			String propValue = member.getStringValue( );
 			if ( propValue != null )
 			{
 				//TODO: We need !isStyleProperty function from MODEL.
 				if ( HighlightRule.OPERATOR_MEMBER.equals( propName ) )
 				{
 					oper = propValue;
 				}
 				else if ( HighlightRule.VALUE1_MEMBER.equals( propName ) )
 				{
 					value1 = propValue;
 				}
 				else if ( HighlightRule.VALUE2_MEMBER.equals( propName ) )
 				{
 					value2 = propValue;
 				}
 				else if(HighlightRule.TEST_EXPR_MEMBER.equals(propName))
 				{
 					testExpr = propValue;
 				}
 				else
 				{
 					setStyleProperty( style, propName, propValue );
 				}
 			}
 		}
 		rule.setExpression( oper, value1, value2 );
 		if(testExpr!=null && testExpr.length()>0)
 		{
 			rule.setTestExpression(testExpr);
 		}
 		else if((defaultStr !=null)&& defaultStr.length()>0)
 		{
 			rule.setTestExpression(defaultStr);
 		}
 		else
 		{
 			//test expression is null
 			return null;
 		}
 
 		//this rule is empty, so we can drop it safely.
 		if ( style.entrySet( ).isEmpty( ) )
 		{
 			return null;
 		}
 		rule.setStyle( style );
 		ConditionalExpression condExpr = new ConditionalExpression(
 				rule.getTestExpression( ),
 				toDteFilterOperator( rule.getOperator( ) ), rule
 						.getValue1( ), rule.getValue2( ) );
 		rule.setConditionExpr( condExpr );
 		return rule;
 	}
 
 	/**
 	 * create highlight defined in the handle.
 	 * 
 	 * @param item
 	 *            styled item.
 	 */
 	protected void setHighlight( StyledElementDesign item, String defaultStr )
 	{
 		if ( item.getStyle( ) == null )
 		{
 			return;
 		}
 		StyleHandle handle = (StyleHandle) item.getStyle( ).getHandle( );
 		if ( handle == null )
 		{
 			return;
 		}
 		//		 hightlight Rules
 		Iterator iter = handle.highlightRulesIterator( );
 
 		if ( iter == null )
 		{
 			return;
 		}
 		HighlightDesign highlight = new HighlightDesign( );
 
 
 		while ( iter.hasNext( ) )
 		{
 			HighlightRuleHandle ruleHandle = (HighlightRuleHandle) iter.next( );
 			HighlightRuleDesign rule = createHighlightRule( ruleHandle, defaultStr );
 			if(rule!=null)
 			{
 				highlight.addRule( rule );
 			}
 		}
 		
 		if ( highlight.getRuleCount( ) > 0 )
 		{
 			item.setHighlight( highlight );
 		}
 	}
 
 	/**
 	 * setup a Map.
 	 * 
 	 * @param item
 	 *            styled item;
 	 */
 	protected void setMap( StyledElementDesign item, String defaultStr )
 	{
 		if ( item.getStyle( ) == null )
 		{
 			return;
 		}
 		StyleHandle handle = (StyleHandle) item.getStyle( ).getHandle( );
 		if ( handle == null )
 		{
 			return;
 		}
 		Iterator iter = handle.mapRulesIterator( );
 		if ( iter == null )
 		{
 			return;
 		}
 		MapDesign map = new MapDesign( );
 
 		while ( iter.hasNext( ) )
 		{
 			MapRuleHandle ruleHandle = (MapRuleHandle) iter.next( );
 			MapRuleDesign rule = createMapRule( ruleHandle, defaultStr );
 			if(rule!=null)
 			{
 				map.addRule( rule );
 			}
 		}
 
 		
 		if ( map.getRuleCount( ) > 0 )
 		{
 			item.setMap( map );
 		}
 
 	}
 
 	/**
 	 * create a map rule.
 	 * 
 	 * @param obj
 	 *            map rule in DE.
 	 * @return map rule in ENGINE.
 	 */
 	protected MapRuleDesign createMapRule( MapRuleHandle handle, String defaultStr )
 	{
 		MapRuleDesign rule = new MapRuleDesign( );
 		rule.setExpression( handle.getOperator( ), handle.getValue1( ), handle
 				.getValue2( ) );
 		rule.setDisplayText( handle.getDisplayKey( ), handle.getDisplay( ) );
 		
 		String testExpr = handle.getTestExpression();
 		if(testExpr!=null && testExpr.length()>0)
 		{
 			rule.setTestExpression(testExpr);
 		}
 		else if((defaultStr !=null)&& defaultStr.length()>0)
 		{
 			rule.setTestExpression(defaultStr);
 		}
 		else
 		{
 			//test expression is null
 			return null;
 		}
 		
 		ConditionalExpression condExpr = new ConditionalExpression( rule.getTestExpression(),
 				toDteFilterOperator( rule
 				.getOperator( ) ), rule.getValue1( ), rule.getValue2( ) );
 		rule.setConditionExpr( condExpr );
 		
 		return rule;
 	}
 
 	/**
 	 * Gets a property of style if it differs from that of parent element's style.
 	 * 
 	 * @param handle The handle of current report element.
 	 * @param parentHandle The handle of parent report element.
 	 * @param name The property name.
 	 * @return the property or null if it is not set.
 	 */
 	protected Object getDistinctProperty( DesignElementHandle handle, DesignElementHandle parentHandle, String name, int flag )
 	{
 		Object value = handle.getProperty( name ); 
 		
 		if( value == null )
 		{
 			return null;
 		}
 		
 		boolean canInherit = StyleDesign.canInherit( name );
 		if( canInherit && ( flag & STYLE_FLAG_INHERITABLE ) > 0 )
 		{
 			Object parentValue;
 			if( this.handle == parentHandle )
 			{
 				// if its parent is ReportDesignHandle
 				// get parentValue from reportDefaultStyle
 				parentValue = inheritableReportStyle.get( name );
 			}
 			else
 			{
 				parentValue = parentHandle.getProperty( name );
 			}
 			
 			if( !value.equals( parentValue ) )
 			{
 				return value;
 			}
 		}
 		else if( !canInherit && ( flag & STYLE_FLAG_PRIVATE ) > 0 )
 		{
 			Object defaultValue = nonInheritableReportStyle.get( name );
 			if( !value.equals( defaultValue ) )
 			{
 				return value;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets color property of style if it differs from that of parent element's style.
 	 * 
 	 * @param handle The handle of current report element.
 	 * @param parentHandle The handle of parent report element.
 	 * @param name The property name.
 	 * @return the property or null if it is not set.
 	 */
 	protected Object getDistinctColorProperty( DesignElementHandle handle, DesignElementHandle parentHandle, String name, int flag )
 	{
 		String strValue = getStrColorValue( handle.getIntProperty( name ) );
 		if( strValue == null )
 		{
 			return null;
 		}
 		
 		boolean canInherit = StyleDesign.canInherit( name );
 		if( canInherit && ( flag & STYLE_FLAG_INHERITABLE ) > 0 )
 		{
 			Object parentValue = null;
 			if( this.handle == parentHandle )
 			{
 				parentValue = inheritableReportStyle.get( name );
 			}
 			else
 			{
 				parentValue = getStrColorValue( parentHandle.getIntProperty( name ) );
 			}
 			
 			if( !strValue.equals( parentValue ) )
 			{
 				return strValue;
 			}
 		}
 		else if( !canInherit && ( flag & STYLE_FLAG_PRIVATE ) > 0 )
 		{
 			Object defaultValue = nonInheritableReportStyle.get( name );
 			if( !strValue.equals( defaultValue ) )
 			{
 				return strValue;
 			}
 		}
 		return null;
 	}
 	
 	protected StyleDesign createDistinctStyle( DesignElementHandle handle,
 			DesignElementHandle parentHandle, int flag )
 	{
 		StyleDesign style = new StyleDesign( );
 		style.setHandle( handle.getPrivateStyle( ) );
 		
 		setupDistinctStyleProperties( style, handle, parentHandle, flag );
 		
 		if ( !style.isEmpty( ) )
 		{
 			assignStyleName( style );
 			return style;
 		}
 		return null;
 	}
 	
 	/**
 	 * @param style
 	 * @param handle
 	 * @param parentHandle
 	 * @param flag
 	 */
 	protected void setupDistinctStyleProperties( StyleDesign style,
 			DesignElementHandle handle, DesignElementHandle parentHandle, int flag )
 	{
 		// Background
 		setStyleProperty( style, Style.BACKGROUND_COLOR_PROP,
 				getDistinctColorProperty( handle, parentHandle, Style.BACKGROUND_COLOR_PROP, flag ) );
 		setStyleProperty( style, Style.BACKGROUND_IMAGE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BACKGROUND_IMAGE_PROP, flag ) );
 		setStyleProperty( style, Style.BACKGROUND_POSITION_X_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BACKGROUND_POSITION_X_PROP, flag ) );
 		setStyleProperty( style, Style.BACKGROUND_POSITION_Y_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BACKGROUND_POSITION_Y_PROP, flag ) );
 		setStyleProperty( style, Style.BACKGROUND_REPEAT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BACKGROUND_REPEAT_PROP, flag ) );
 
 		// Text related
 		setStyleProperty( style, Style.TEXT_ALIGN_PROP,
 				getDistinctProperty( handle, parentHandle, Style.TEXT_ALIGN_PROP, flag ) );
 		setStyleProperty( style, Style.TEXT_INDENT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.TEXT_INDENT_PROP, flag ) );
 		setStyleProperty( style, Style.LETTER_SPACING_PROP, 
 				getDistinctProperty( handle, parentHandle, Style.LETTER_SPACING_PROP, flag ) );
 		setStyleProperty( style, Style.LINE_HEIGHT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.LINE_HEIGHT_PROP, flag ) );
 		setStyleProperty( style, Style.ORPHANS_PROP,
 				getDistinctProperty( handle, parentHandle, Style.ORPHANS_PROP, flag ) );
 		setStyleProperty( style, Style.TEXT_TRANSFORM_PROP,
 				getDistinctProperty( handle, parentHandle, Style.TEXT_TRANSFORM_PROP, flag ) );
 		setStyleProperty( style, Style.VERTICAL_ALIGN_PROP,
 				getDistinctProperty( handle, parentHandle, Style.VERTICAL_ALIGN_PROP, flag ) );
 		setStyleProperty( style, Style.WHITE_SPACE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.WHITE_SPACE_PROP, flag ) );
 		setStyleProperty( style, Style.WIDOWS_PROP,
 				getDistinctProperty( handle, parentHandle, Style.WIDOWS_PROP, flag ) );
 		setStyleProperty( style, Style.WORD_SPACING_PROP,
 				getDistinctProperty( handle, parentHandle, Style.WORD_SPACING_PROP, flag ) );
 
 		// Section properties
 		setStyleProperty( style, Style.DISPLAY_PROP,
 				getDistinctProperty( handle, parentHandle, Style.DISPLAY_PROP, flag ) );
 		setStyleProperty( style, Style.MASTER_PAGE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.MASTER_PAGE_PROP, flag ) );
 		setStyleProperty( style, Style.PAGE_BREAK_AFTER_PROP,
 				getDistinctProperty( handle, parentHandle, Style.PAGE_BREAK_AFTER_PROP, flag ) );
 		setStyleProperty( style, Style.PAGE_BREAK_BEFORE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.PAGE_BREAK_BEFORE_PROP, flag ) );
 		setStyleProperty( style, Style.PAGE_BREAK_INSIDE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.PAGE_BREAK_INSIDE_PROP, flag ) );
 		/*
 		 * setStyleProperty( style, Style.SHOW_IF_BLANK_PROP,
 		 * 			getDistinctProperty( handle, parentHandle, Style.SHOW_IF_BLANK_PROP ) );
 		 * setStyleProperty( style, Style.CAN_SHRINK_PROP,
 		 * 			getDistinctProperty( handle, parentHandle, Style.CAN_SHRINK_PROP ) );
 		 */
 		// Font related
 		setStyleProperty( style, Style.FONT_FAMILY_PROP,
 				getDistinctProperty( handle, parentHandle, Style.FONT_FAMILY_PROP, flag ) );
 		setStyleProperty( style, Style.COLOR_PROP,
 				getDistinctColorProperty( handle, parentHandle, Style.COLOR_PROP, flag ) );
 		setStyleProperty( style, Style.FONT_SIZE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.FONT_SIZE_PROP, flag ) );
 		setStyleProperty( style, Style.FONT_STYLE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.FONT_STYLE_PROP, flag ) );
 		setStyleProperty( style, Style.FONT_WEIGHT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.FONT_WEIGHT_PROP, flag ) );
 		setStyleProperty( style, Style.FONT_VARIANT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.FONT_VARIANT_PROP, flag ) );
 
 		// Text decoration
 		setStyleProperty( style, Style.TEXT_LINE_THROUGH_PROP,
 				getDistinctProperty( handle, parentHandle, Style.TEXT_LINE_THROUGH_PROP, flag ) );
 		setStyleProperty( style, Style.TEXT_OVERLINE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.TEXT_OVERLINE_PROP, flag ) );
 		setStyleProperty( style, Style.TEXT_UNDERLINE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.TEXT_UNDERLINE_PROP, flag ) );
 
 		// Border
 		setStyleProperty( style, Style.BORDER_BOTTOM_COLOR_PROP,
 				getDistinctColorProperty( handle, parentHandle, Style.BORDER_BOTTOM_COLOR_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_BOTTOM_STYLE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_BOTTOM_STYLE_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_BOTTOM_WIDTH_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_BOTTOM_WIDTH_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_LEFT_COLOR_PROP,
 				getDistinctColorProperty( handle, parentHandle, Style.BORDER_LEFT_COLOR_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_LEFT_STYLE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_LEFT_STYLE_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_LEFT_WIDTH_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_LEFT_WIDTH_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_RIGHT_COLOR_PROP,
 				getDistinctColorProperty( handle, parentHandle, Style.BORDER_RIGHT_COLOR_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_RIGHT_STYLE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_RIGHT_STYLE_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_RIGHT_WIDTH_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_RIGHT_WIDTH_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_TOP_COLOR_PROP,
 				getDistinctColorProperty( handle, parentHandle, Style.BORDER_TOP_COLOR_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_TOP_STYLE_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_TOP_STYLE_PROP, flag ) );
 		setStyleProperty( style, Style.BORDER_TOP_WIDTH_PROP,
 				getDistinctProperty( handle, parentHandle, Style.BORDER_TOP_WIDTH_PROP, flag ) );
 
 		// Margin
 		setStyleProperty( style, Style.MARGIN_TOP_PROP,
 				getDistinctProperty( handle, parentHandle, Style.MARGIN_TOP_PROP, flag ) );
 		setStyleProperty( style, Style.MARGIN_LEFT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.MARGIN_LEFT_PROP, flag ) );
 		setStyleProperty( style, Style.MARGIN_BOTTOM_PROP,
 				getDistinctProperty( handle, parentHandle, Style.MARGIN_BOTTOM_PROP, flag ) );
 		setStyleProperty( style, Style.MARGIN_RIGHT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.MARGIN_RIGHT_PROP, flag ) );
 
 		// Padding
 		setStyleProperty( style, Style.PADDING_TOP_PROP,
 				getDistinctProperty( handle, parentHandle, Style.PADDING_TOP_PROP, flag ) );
 		setStyleProperty( style, Style.PADDING_LEFT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.PADDING_LEFT_PROP, flag ) );
 		setStyleProperty( style, Style.PADDING_BOTTOM_PROP,
 				getDistinctProperty( handle, parentHandle, Style.PADDING_BOTTOM_PROP, flag ) );
 		setStyleProperty( style, Style.PADDING_RIGHT_PROP,
 				getDistinctProperty( handle, parentHandle, Style.PADDING_RIGHT_PROP, flag ) );
 
 		// Data Formatting
 		setStyleProperty( style, Style.NUMBER_ALIGN_PROP, handle.getProperty( Style.NUMBER_ALIGN_PROP ) );
 
 		Object value = handle.getProperty( Style.DATE_TIME_FORMAT_PROP );
 		if ( value != null && value instanceof DateTimeFormatValue )
 		{
 			setStyleProperty( style, Style.DATE_TIME_FORMAT_PROP, ( (DateTimeFormatValue) value ).getPattern( ) );
 		}
 		
 		value = handle.getProperty( Style.NUMBER_FORMAT_PROP );
 		if ( value != null && value instanceof NumberFormatValue )
 		{
 			setStyleProperty( style, Style.NUMBER_FORMAT_PROP, ( (NumberFormatValue) value ).getPattern( ) );
 		}
 		
 		value = handle.getProperty( Style.STRING_FORMAT_PROP );
 		if ( value != null && value instanceof StringFormatValue )
 		{
 			setStyleProperty( style, Style.STRING_FORMAT_PROP, ( (StringFormatValue) value ).getPattern( ) );
 		}
 
 		//Others
 		setStyleProperty( style, Style.CAN_SHRINK_PROP,	handle.getProperty( Style.CAN_SHRINK_PROP ) );
 		setStyleProperty( style, Style.SHOW_IF_BLANK_PROP, handle.getProperty( Style.SHOW_IF_BLANK_PROP ) );
 		setStyleProperty( style, Style.MASTER_PAGE_PROP, handle.getProperty( Style.MASTER_PAGE_PROP ) );
 	}
 	
 	/**
 	 * set style property.
 	 * 
 	 * property will be set only if the value is not null.
 	 * 
 	 * @param style
 	 *            style design
 	 * @param name
 	 *            property name
 	 * @param value
 	 *            property value.
 	 */
 	protected void setStyleProperty( StyleDesign style, String name,
 			Object value )
 	{
 		assert name != null;
 		if ( value != null )
 		{
 			style.put( name, value );
 		}
 	}
 
 	protected DimensionType createDimension( DimensionHandle handle )
 	{
 		if ( handle == null || !handle.isSet( ) )
 		{
 			return null;
 		}
 		//Extended Choice
 		if ( handle.isKeyword( ) )
 		{
 			return new DimensionType( handle.getStringValue( ) );
 		}
 		//set measure and unit
 		double measure = handle.getMeasure( );
 		String unit = handle.getUnits( );
 		if ( DimensionValue.DEFAULT_UNIT.equals( unit ) )
 		{
 			unit = defaultUnit;
 		}
 		return new DimensionType( measure, unit );
 	}
 
 	protected void setupListingItem( ListingDesign listing, ListingHandle handle )
 	{
 		//setup related scripts
 		setupReportItem( listing, handle );
 
 		//setup scripts
 		listing.setOnStart( handle.getOnStart( ) );
 		listing.setOnRow( handle.getOnRow( ) );
 		listing.setOnFinish( handle.getOnFinish( ) );
 
 	}
 
 	//	 Convert model operator value to DtE IColumnFilter enum value
 	protected int toDteFilterOperator( String modelOpr )
 	{
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_EQ ) )
 			return IConditionalExpression.OP_EQ;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NE ) )
 			return IConditionalExpression.OP_NE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_LT ) )
 			return IConditionalExpression.OP_LT;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_LE ) )
 			return IConditionalExpression.OP_LE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_GE ) )
 			return IConditionalExpression.OP_GE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_GT ) )
 			return IConditionalExpression.OP_GT;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_BETWEEN ) )
 			return IConditionalExpression.OP_BETWEEN;
 		if ( modelOpr
 				.equals( DesignChoiceConstants.FILTER_OPERATOR_NOT_BETWEEN ) )
 			return IConditionalExpression.OP_NOT_BETWEEN;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NULL ) )
 			return IConditionalExpression.OP_NULL;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_NOT_NULL ) )
 			return IConditionalExpression.OP_NOT_NULL;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_TRUE ) )
 			return IConditionalExpression.OP_TRUE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_FALSE ) )
 			return IConditionalExpression.OP_FALSE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_LIKE ) )
 			return IConditionalExpression.OP_LIKE;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_TOP_N ) )
 			return IConditionalExpression.OP_TOP_N;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_BOTTOM_N ) )
 			return IConditionalExpression.OP_BOTTOM_N;
 		if ( modelOpr
 				.equals( DesignChoiceConstants.FILTER_OPERATOR_TOP_PERCENT ) )
 			return IConditionalExpression.OP_TOP_PERCENT;
 		if ( modelOpr
 				.equals( DesignChoiceConstants.FILTER_OPERATOR_BOTTOM_PERCENT ) )
 			return IConditionalExpression.OP_BOTTOM_PERCENT;
 		if ( modelOpr.equals( DesignChoiceConstants.FILTER_OPERATOR_ANY ) )
 			return IConditionalExpression.OP_ANY;
 
 		return IConditionalExpression.OP_NONE;
 	}
 
 
 	protected void addReportStyleProperty( StyleHandle handle, String name )
 	{
 		Object value = null;
 		if( StyleDesign.canInherit( name ) )
 		{
 			if( handle != null )
 			{
 				value = handle.getProperty( name );
 			}
 			if( value == null )
 			{
 				value = StyleDesign.getDefaultValue( name );
 			}
 			setStyleProperty( inheritableReportStyle, name, value );
 		}
 		else
 		{
 			value = StyleDesign.getDefaultValue( name );
 			setStyleProperty( nonInheritableReportStyle, name, value );
 		}
 	}
 	
 	protected String getStrColorValue( int value )
 	{
 		if( value == -1 )
 		{
 			return null;
 		}
 		return StringUtil.toRgbText( value );
 	}
 
 	protected String getStrColorValue( Object value )
 	{
 		int intValue = -1;
 		if( value == null )
 		{
 			return null;
 		}
 		
 		if( value instanceof Integer )
 		{
 			intValue = ( ( Integer ) value ).intValue( );
 		}
 		else
 		{
 			intValue = ColorUtil.parseColor( value.toString( ) );
 		}
 
 		if( intValue == -1 )
 		{
 			return null;
 		}
 		
 		return StringUtil.toRgbText( intValue );
 	}
 	
 	protected void addReportColorStyleProperty( StyleHandle handle, String name )
 	{
 		String value = null;
 		
 		if( StyleDesign.canInherit( name ) )
 		{
 			if( handle != null )
 			{
 				value = getStrColorValue( handle.getIntProperty( name ) );
 			}
 			if( value == null )
 			{
 				value = getStrColorValue( StyleDesign.getDefaultValue( name ) );
 			}
 			setStyleProperty( inheritableReportStyle, name, value );
 		}
 		else
 		{
 			value = getStrColorValue( StyleDesign.getDefaultValue( name ) );
 			setStyleProperty( nonInheritableReportStyle, name, value );
 		}
 	}
 	
 	protected void createReportStyles( StyleHandle handle )
 	{
 		nonInheritableReportStyle = new StyleDesign( );
 		inheritableReportStyle = new StyleDesign( );
 		
 		// Background
 		addReportColorStyleProperty( handle, Style.BACKGROUND_COLOR_PROP );
 		addReportStyleProperty( handle, Style.BACKGROUND_IMAGE_PROP );
 		addReportStyleProperty( handle, Style.BACKGROUND_POSITION_X_PROP );
 		addReportStyleProperty( handle, Style.BACKGROUND_POSITION_Y_PROP );
 		addReportStyleProperty( handle, Style.BACKGROUND_REPEAT_PROP );
 
 		// Text related
 		addReportStyleProperty( handle, Style.TEXT_ALIGN_PROP );
 		addReportStyleProperty( handle, Style.TEXT_INDENT_PROP );
 		addReportStyleProperty( handle, Style.LETTER_SPACING_PROP );
 		addReportStyleProperty( handle, Style.LINE_HEIGHT_PROP );
 		addReportStyleProperty( handle, Style.ORPHANS_PROP );
 		addReportStyleProperty( handle, Style.TEXT_TRANSFORM_PROP );
 		addReportStyleProperty( handle, Style.VERTICAL_ALIGN_PROP );
 		addReportStyleProperty( handle, Style.WHITE_SPACE_PROP );
 		addReportStyleProperty( handle, Style.WIDOWS_PROP );
 		addReportStyleProperty( handle, Style.WORD_SPACING_PROP );
 
 		// Section properties
 		addReportStyleProperty( handle, Style.DISPLAY_PROP );
 		addReportStyleProperty( handle, Style.MASTER_PAGE_PROP );
 		addReportStyleProperty( handle, Style.PAGE_BREAK_AFTER_PROP );
 		addReportStyleProperty( handle, Style.PAGE_BREAK_BEFORE_PROP );
 		addReportStyleProperty( handle, Style.PAGE_BREAK_INSIDE_PROP );
 		// addStyleProperty( style, handle, Style.SHOW_IF_BLANK_PROP );
 		// addStyleProperty( style, handle, Style.CAN_SHRINK_PROP );
 
 		// Font related
 		addReportStyleProperty( handle, Style.FONT_FAMILY_PROP );
 		addReportColorStyleProperty( handle, Style.COLOR_PROP );
 		addReportStyleProperty( handle, Style.FONT_SIZE_PROP );
 		addReportStyleProperty( handle, Style.FONT_STYLE_PROP );
 		addReportStyleProperty( handle, Style.FONT_WEIGHT_PROP );
 		addReportStyleProperty( handle, Style.FONT_VARIANT_PROP );
 
 		// Text decoration
 		addReportStyleProperty( handle, Style.TEXT_LINE_THROUGH_PROP );
 		addReportStyleProperty( handle, Style.TEXT_OVERLINE_PROP );
 		addReportStyleProperty( handle, Style.TEXT_UNDERLINE_PROP );
 
 		// Border
 		addReportColorStyleProperty( handle, Style.BORDER_BOTTOM_COLOR_PROP );
 		addReportStyleProperty( handle, Style.BORDER_BOTTOM_STYLE_PROP );
 		addReportStyleProperty( handle, Style.BORDER_BOTTOM_WIDTH_PROP );
 		addReportColorStyleProperty( handle, Style.BORDER_LEFT_COLOR_PROP );
 		addReportStyleProperty( handle, Style.BORDER_LEFT_STYLE_PROP );
 		addReportStyleProperty( handle, Style.BORDER_LEFT_WIDTH_PROP );
 		addReportColorStyleProperty( handle, Style.BORDER_RIGHT_COLOR_PROP );
 		addReportStyleProperty( handle, Style.BORDER_RIGHT_STYLE_PROP );
 		addReportStyleProperty( handle, Style.BORDER_RIGHT_WIDTH_PROP );
 		addReportColorStyleProperty( handle, Style.BORDER_TOP_COLOR_PROP );
 		addReportStyleProperty( handle, Style.BORDER_TOP_STYLE_PROP );
 		addReportStyleProperty( handle, Style.BORDER_TOP_WIDTH_PROP );
 		
 		// Margin
 		addReportStyleProperty( handle, Style.MARGIN_TOP_PROP );
 		addReportStyleProperty( handle, Style.MARGIN_LEFT_PROP );
 		addReportStyleProperty( handle, Style.MARGIN_BOTTOM_PROP );
 		addReportStyleProperty( handle, Style.MARGIN_RIGHT_PROP );
 
 		// Padding
 		addReportStyleProperty( handle, Style.PADDING_TOP_PROP );
 		addReportStyleProperty( handle, Style.PADDING_LEFT_PROP );
 		addReportStyleProperty( handle, Style.PADDING_BOTTOM_PROP );
 		addReportStyleProperty( handle, Style.PADDING_RIGHT_PROP );
 		
 		if( !inheritableReportStyle.isEmpty( ) )
 		{
 			assignStyleName( inheritableReportStyle );
 		}
 	}
 	
 	protected void setupContentStyle( MasterPageDesign design )
 	{
 		StyleDesign style = design.getStyle( );
 		if( style == null || style.isEmpty( ) )
 		{
 			return;
 		}
 		
 		StyleDesign contentStyle = new StyleDesign( );
 		contentStyle.put( Style.BACKGROUND_COLOR_PROP, style.get( Style.BACKGROUND_COLOR_PROP ) );
 		contentStyle.put( Style.BACKGROUND_IMAGE_PROP, style.get( Style.BACKGROUND_IMAGE_PROP ) );
 		contentStyle.put( Style.BACKGROUND_POSITION_X_PROP, style.get( Style.BACKGROUND_POSITION_X_PROP ) );
 		contentStyle.put( Style.BACKGROUND_POSITION_Y_PROP, style.get( Style.BACKGROUND_POSITION_Y_PROP ) );
 		contentStyle.put( Style.BACKGROUND_REPEAT_PROP, style.get( Style.BACKGROUND_REPEAT_PROP ) );
 
 		if( !contentStyle.isEmpty( ) )
 		{
 			assignStyleName( contentStyle );
 			design.setContentStyle( contentStyle );
 		}
 	}
 }
