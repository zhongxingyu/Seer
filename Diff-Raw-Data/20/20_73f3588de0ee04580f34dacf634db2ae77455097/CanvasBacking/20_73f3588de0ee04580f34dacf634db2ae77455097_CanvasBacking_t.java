 package swarm.client.view.cell;
 
 import swarm.shared.structs.BitArray;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.canvas.dom.client.FillStrokeStyle;
 
 public class CanvasBacking
 {
 	public static interface I_Skipper
 	{
 		int skip(int m, int n);
 	}
 	
 	private final Canvas m_canvas = Canvas.createIfSupported();
 	
 	private CssColor m_fillStyle;
 	private final CssColor m_gapStyle = CssColor.make("rgba(0,0,0,0)");
 	private final I_Skipper m_skipper;
 	
 	public CanvasBacking(I_Skipper skipper)
 	{
 		m_skipper = skipper;
 	}
 	
 	public void setColor(String color)
 	{
 		m_fillStyle = CssColor.make(color);
 	}
 	
 	public Canvas getCanvas()
 	{
 		return m_canvas;
 	}
 	
 	public void onResize(int width, int height)
 	{
 		m_canvas.setWidth(width + "px");
 		m_canvas.setHeight(height + "px");
 		m_canvas.setCoordinateSpaceWidth(width);
 		m_canvas.setCoordinateSpaceHeight(height);
 		
 //		m_canvas.getElement().getStyle().setZIndex(15);
 	}
 	
 	public void update(double startX_meta, double startY_meta, int startM, int startN, int across, int down, double cellSize, double cellSizePlusPadding, int totalGridSize, BitArray ownership, double metaCellSize, int metaSubCellCount, int startM_meta, int startN_meta)
 	{
  		clear();
 		
 		Context2d context = m_canvas.getContext2d();
 		context.setFillStyle(m_fillStyle);
		cellSize -= 1.0;
 		
 		int limit_n = startN + down;
 		int limit_m = startM + across;
 //		limit_n = limit_n < totalGridSize ? limit_n : totalGridSize-1;
 //		limit_m = limit_m < totalGridSize ? limit_m : totalGridSize-1;
 //		
 		for( int n = startN; n < limit_n; n++ )
 		{
 			for( int m = startM; m < limit_m; m++ )
 			{
 				int skip = m_skipper.skip(m, n);
 				
 				if( skip == 1 )
 				{
 					continue;
 				}
 				else if( skip > 1 )
 				{
 					m += skip;
 					m -= 1;
 					
 					continue;
 				}
 				
 				int index = n*totalGridSize + m;
 				
 				if( !ownership.isSet(index) )  continue;
 				
 				int offsetM = m - startM_meta;
 				int offsetN = n - startN_meta;
 				int offsetM_mod = offsetM % metaSubCellCount;
 				int offsetN_mod = offsetN % metaSubCellCount;
 				offsetM -= offsetM_mod;
 				offsetN -= offsetN_mod;
 				offsetM /= metaSubCellCount;
 				offsetN /= metaSubCellCount;
 				
 				double currX = startX_meta + offsetM * metaCellSize + offsetM_mod * cellSizePlusPadding;
 				double currY = startY_meta + offsetN * metaCellSize + offsetN_mod * cellSizePlusPadding;
 				
//				currX = Math.ceil(currX)+1.0;
//				currY = Math.ceil(currY)+1.0;
//				currX += 1.0;
//				currY += 1.0;
				currX += .5;
				currY += .5;
 		
 				context.fillRect(currX, currY, cellSize, cellSize);
 			}
 		}
 	}
 	
 	
 	private void clear()
 	{
 		int width = m_canvas.getCoordinateSpaceWidth();
 		int height = m_canvas.getCoordinateSpaceHeight();
 		Context2d context = m_canvas.getContext2d();
 		context.setFillStyle(m_gapStyle);
 		context.clearRect(0, 0, width, height);
 	}
 }
