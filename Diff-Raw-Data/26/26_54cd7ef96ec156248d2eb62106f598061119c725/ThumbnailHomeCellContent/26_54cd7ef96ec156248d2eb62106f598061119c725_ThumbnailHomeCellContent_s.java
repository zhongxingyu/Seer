 package com.dougkoellmer.server.homecells;
 
 import java.util.Iterator;
 
 import javax.servlet.ServletContext;
 
 import com.dougkoellmer.server.entities.ServerGrid;
 import com.dougkoellmer.shared.homecells.E_HomeCell;
 
 import swarm.server.thirdparty.servlet.U_Servlet;
 import swarm.shared.entities.E_CodeSafetyLevel;
 import swarm.shared.entities.E_CodeType;
 import swarm.shared.structs.CellSize;
 
 public class ThumbnailHomeCellContent implements I_HomeCellContent
 {
 	//private static final String;
 	
 	private String m_sourcCode;
 	private int m_height = 0;
 	
 	public ThumbnailHomeCellContent()
 	{
 	}
 	
 	public void init(ServletContext servletContext, E_HomeCell homeCell)
 	{
 		String trClass = "class='dk_thumb_row'";
 		StringBuilder builder = new StringBuilder();
 		builder.append("<div style='width:100%; height:100%; font-size:0px; overflow:hidden;'>");
 		builder.append("<table style='' class='dk_thumb_table'><tr "+trClass+">");
 		
 		int i = 0, minCount = 12;
 		double rowHeight = 512.0/6.0;
 		Iterator<E_HomeCell> children = homeCell.getChildren();
 		
 		while( i < minCount || (children.hasNext() || !children.hasNext() && i % 2 != 0) )
 		{
 			boolean isLeft = i%2==0;
 			String tdClass = "class='dk_thumb_cell "+(isLeft?"dk_thumb_cell_left":"")+"'";
 			
 			if( children.hasNext() )
 			{
 				E_HomeCell child = children.next();
 				String description = U_HomeCellMeta.getDescription(child);
 				String address = child.getPrimaryAddress();
 				
 				builder.append("<td "+tdClass+">");
 				builder.append("<a href='"+address+"' class='waypoint_cell_link'>");
 				builder.append("<table style='width:100%; height:100%;' class='waypoint_no_table_fluff'><tr><td style='vertical-align:middle;'><img class='dk_thumb_cell_img' src='/img/coming_soon.thumb.png/'></td><td style='text-align:right;'><div class='dk_thumb_desc'>");
 				builder.append(description);
 				builder.append("</div></td></tr></table>");
 				builder.append("</a>");
 				builder.append("</td>");
 			}
 			else
 			{
 				builder.append("<td "+tdClass+">");
 				builder.append("</td>");
 			}
 			
 			boolean lastCell = !children.hasNext() && i >= minCount-1;
 			
 			if( i % 2 != 0 && i != 0 && !lastCell)
 			{
 				builder.append("</tr><tr "+trClass+">");
 			}
 			
 			i++;
 		}
 		
 		int rowCount = i/2;
 		m_height = (int) Math.ceil(((double)rowCount)*rowHeight);
 		m_height = Math.max(m_height, ServerGrid.CELL_SIZE);
 		
 		builder.append("</tr></table></div>");
 		
 		m_sourcCode = builder.toString();
 	}
 	
 	public CellSize getFocusedCellSize()
 	{
 		CellSize cellSize = new CellSize(CellSize.DEFAULT_DIMENSION, m_height);
 		
 		return cellSize;
 	}
 	
 	public String getSourceCode(E_CodeType eCodeType)
 	{
 		if( eCodeType == E_CodeType.SOURCE )
 		{
 			return m_sourcCode;
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	@Override
 	public E_CodeSafetyLevel getSafetyLevel(E_CodeType eCodeType)
 	{
 		if( eCodeType == E_CodeType.SPLASH )
 		{
 			return E_CodeSafetyLevel.VIRTUAL_STATIC_SANDBOX;
 		}
 		else
 		{
 			return null;
 		}
 	}
 }
