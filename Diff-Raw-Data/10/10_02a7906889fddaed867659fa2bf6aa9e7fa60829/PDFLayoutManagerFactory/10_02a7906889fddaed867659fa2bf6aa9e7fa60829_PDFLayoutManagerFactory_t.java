 
 package org.eclipse.birt.report.engine.layout.pdf;
 
 /***********************************************************************
  * Copyright (c) 2004 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Actuate Corporation - initial API and implementation
  ***********************************************************************/
 
 import org.eclipse.birt.core.format.NumberFormatter;
 import org.eclipse.birt.report.engine.content.IAutoTextContent;
 import org.eclipse.birt.report.engine.content.ICellContent;
 import org.eclipse.birt.report.engine.content.IContainerContent;
 import org.eclipse.birt.report.engine.content.IContent;
 import org.eclipse.birt.report.engine.content.IContentVisitor;
 import org.eclipse.birt.report.engine.content.IDataContent;
 import org.eclipse.birt.report.engine.content.IForeignContent;
 import org.eclipse.birt.report.engine.content.IGroupContent;
 import org.eclipse.birt.report.engine.content.IImageContent;
 import org.eclipse.birt.report.engine.content.ILabelContent;
 import org.eclipse.birt.report.engine.content.IListBandContent;
 import org.eclipse.birt.report.engine.content.IListContent;
 import org.eclipse.birt.report.engine.content.IListGroupContent;
 import org.eclipse.birt.report.engine.content.IPageContent;
 import org.eclipse.birt.report.engine.content.IRowContent;
 import org.eclipse.birt.report.engine.content.ITableBandContent;
 import org.eclipse.birt.report.engine.content.ITableContent;
 import org.eclipse.birt.report.engine.content.ITableGroupContent;
 import org.eclipse.birt.report.engine.content.ITextContent;
 import org.eclipse.birt.report.engine.content.impl.LabelContent;
 import org.eclipse.birt.report.engine.executor.IReportItemExecutor;
 import org.eclipse.birt.report.engine.internal.executor.dom.DOMReportItemExecutor;
 import org.eclipse.birt.report.engine.ir.DimensionType;
 import org.eclipse.birt.report.engine.layout.content.LineStackingExecutor;
 import org.eclipse.birt.report.engine.layout.pdf.util.HTML2Content;
 import org.eclipse.birt.report.engine.layout.pdf.util.PropertyUtil;
 
 public class PDFLayoutManagerFactory
 {
 
 	private ContentVisitor visitor = new ContentVisitor( );
 
 	PDFAbstractLM layoutManager = null;
 
 	PDFStackingLM parent = null;
 
 	private PDFLayoutEngineContext context = null;
 
 	protected HTML2Content converter = null;
 
 	protected IReportItemExecutor executor;
 
 	public PDFLayoutManagerFactory( PDFLayoutEngineContext context )
 	{
 		this.context = context;
 	}
 
 	public PDFAbstractLM createLayoutManager( PDFStackingLM parent,
 			IContent content, IReportItemExecutor executor )
 	{
 		this.parent = parent;
 		this.executor = executor;
 		if ( executor instanceof LineStackingExecutor )
 		{
 			return new PDFLineAreaLM( context, parent,  executor );
 		}
 		if ( content != null )
 		{
 			return (PDFAbstractLM) content.accept( visitor, null );
 		}
 		assert ( false );
 		return null;
 	}
 
 	private class ContentVisitor implements IContentVisitor
 	{
 
 		public Object visit( IContent content, Object value )
 		{
 			return visitContent( content, value );
 		}
 
 		public Object visitContent( IContent content, Object value )
 		{
 			boolean isInline = PropertyUtil.isInlineElement( content );
 			if ( isInline )
 			{
 				return new PDFTextInlineBlockLM( context, parent, content,
 						executor );
 			}
 			else
 			{
 				return new PDFBlockContainerLM( context, parent, content,
 						executor );
 			}
 		}
 
 		public Object visitPage( IPageContent page, Object value )
 		{
 			assert ( false );
 			return null;
 		}
 
 		public Object visitContainer( IContainerContent container, Object value )
 		{
 			return visitContent( container, value );
 		}
 
 		public Object visitTable( ITableContent table, Object value )
 		{
 			return new PDFTableLM( context, parent, table, executor );
 		}
 
 		public Object visitTableBand( ITableBandContent tableBand, Object value )
 		{
 			return new PDFTableBandLM( context, parent, tableBand, 
 					executor );
 		}
 
 		public Object visitRow( IRowContent row, Object value )
 		{
 			return new PDFRowLM( context, parent, row, executor );
 		}
 
 		public Object visitCell( ICellContent cell, Object value )
 		{
 			return new PDFCellLM( context, parent, cell, executor );
 		}
 
 		public Object visitText( ITextContent text, Object value )
 		{
 			// FIXME
 			return handleText( text );
 		}
 
 		public Object visitLabel( ILabelContent label, Object value )
 		{
 			return handleText( label );
 		}
 
 		public Object visitData( IDataContent data, Object value )
 		{
 			return handleText( data );
 		}
 
 		public Object visitImage( IImageContent image, Object value )
 		{
 			boolean isInline = parent instanceof PDFLineAreaLM;
 			if ( isInline )
 			{
 				assert ( parent instanceof PDFLineAreaLM );
 				return new PDFImageLM( context, parent, image,
 						executor );
 			}
 			else
 			{
 				return new PDFImageBlockContainerLM( context, parent, image,
 						executor );
 			}
 		}
 
 		public Object visitForeign( IForeignContent foreign, Object value )
 		{
 			if ( IForeignContent.HTML_TYPE.equals( foreign.getRawType( ) ) )
 			{
 				if ( converter == null )
 				{
 					converter = new HTML2Content( foreign.getReportContent( )
 							.getDesign( ).getBasePath( ) );
 				}
 				// build content DOM tree for HTML text
 				converter.html2Content( foreign );
 				executor = new DOMReportItemExecutor( foreign );
 				executor.execute( );
 				return visitContent( foreign, value );
 			}
 			LabelContent label = new LabelContent( foreign );
 			return handleText( label );
 		}
 
 		public Object visitAutoText( IAutoTextContent autoText, Object value )
 		{
 			if ( IAutoTextContent.PAGE_NUMBER == autoText.getType( ) )
 			{
 				if ( parent instanceof PDFLineAreaLM )
 				{
 					String originalPageNumber = autoText.getText( );
 					NumberFormatter nf = new NumberFormatter( );
 					String patternStr = autoText.getComputedStyle( )
 							.getNumberFormat( );
 					nf.applyPattern( patternStr );
 					try
 					{
 						autoText.setText( nf.format( Integer
 								.parseInt( originalPageNumber ) ) );
 					}
 					catch(NumberFormatException nfe)
 					{
 						autoText.setText( originalPageNumber );
 					}
 				}
 				return handleText( autoText );
 			}
 			return new PDFTemplateLM( context, parent, autoText, 
 					executor );
 		}
 
 		private Object handleText( ITextContent content )
 		{
 			boolean isInline = parent instanceof PDFLineAreaLM;
 			if ( isInline )
 			{
				return new PDFTextLM( context, parent, content, 
						executor );
				/*assert ( parent instanceof PDFLineAreaLM );
 				DimensionType width = content.getWidth( );
 				// if text contains line break or width is specified, this text
 				// will be regard as a inline-block area
				if ( width != null  || text.indexOf( '\n' )>=0 )
 				{
 					return new PDFTextInlineBlockLM( context, parent, content,
 							executor );
 				}
 				else
 				{
 					return new PDFTextLM( context, parent, content, 
 							executor );
				}*/
 			}
 			else
 			{
 				String text = content.getText( );
 				if(text==null || "".equals( text )) //$NON-NLS-1$
 				{
 					content.setText( " " ); //$NON-NLS-1$
 				}
 				return new PDFTextBlockContainerLM( context, parent, content,
 						executor );
 			}
 		}
 
 		public Object visitList( IListContent list, Object value )
 		{
 			return new PDFListLM( context, parent, list, executor );
 
 		}
 
 		public Object visitListBand( IListBandContent listBand, Object value )
 		{
 			assert ( false );
 			return null;
 			// return new PDFListBandLM(context, parent, listBand, emitter,
 			// executor);
 
 		}
 
 		public Object visitListGroup( IListGroupContent group, Object value )
 		{
 			return new PDFListGroupLM( context, parent, group, 
 					executor );
 		}
 
 		public Object visitTableGroup( ITableGroupContent group, Object value )
 		{
 			return new PDFTableGroupLM( context, parent, group, 
 					executor );
 		}
 
 		public Object visitGroup( IGroupContent group, Object value )
 		{
 			return new PDFListGroupLM( context, parent, group, 
 					executor );
 		}
 
 	}
 
 }
