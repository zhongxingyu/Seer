 package com.hexcore.cas.ui.toolkit;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2f;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.TriangleGrid;
 
 public class TriangleGridWidget extends Grid2DWidget
 {
 	public TriangleGridWidget(TriangleGrid grid, int cellSize)
 	{
 		this(new Vector2i(0, 0), grid, cellSize);
 	}
 
 	public TriangleGridWidget(Vector2i position, TriangleGrid grid, int cellSize)
 	{
 		super(position, new Vector2i((grid.getWidth() * cellSize) / 2 + cellSize / 2, 
 								      grid.getHeight() * (int)(cellSize * Math.sqrt(2) * 0.5)),
 								      grid, cellSize);
 	}
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		Vector2i pos = this.position.add(position);
 		Graphics.renderRectangle(gl, pos, size, backgroundColour);
 		
 		float s = cellSize * zoom;
 		float r = s / 2;
 		float h = (float)(s * Math.sqrt(2) * 0.5);
 		
 		pos.inc((int)(scroll.x * s), (int)(scroll.y * s));
 		
 		Vector2f[]	downTriangle = new Vector2f[3];
 		downTriangle[0] = new Vector2f(0.0f, 	0.0f);
 		downTriangle[1] = new Vector2f(r+r, 	0.0f);
 		downTriangle[2] = new Vector2f(r,		h);
 		
 		Vector2f[]	downTriangleBorder = new Vector2f[3];
 		downTriangleBorder[0] = new Vector2f(0.5f, 		0.5f);
 		downTriangleBorder[1] = new Vector2f(r+r+0.5f, 	0.5f);
 		downTriangleBorder[2] = new Vector2f(r+0.5f,	h+0.5f);		
 		
 		Vector2f[]	upTriangle = new Vector2f[3];
 		upTriangle[0] = new Vector2f(r, 	0.0f);
 		upTriangle[1] = new Vector2f(r+r, 	h);
 		upTriangle[2] = new Vector2f(0.0f,	h);
 		
 		Vector2f[]	upTriangleBorder = new Vector2f[3];
 		upTriangleBorder[0] = new Vector2f(r+0.5f, 		0.5f);
 		upTriangleBorder[1] = new Vector2f(r+r+0.5f, 	h+0.5f);
 		upTriangleBorder[2] = new Vector2f(0.5f,		h+0.5f);		
 				
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				Cell		cell = grid.getCell(x, y);
 				Colour		colour = Colour.DARK_GREY;
 				Vector2i 	p = pos.add((int)(x * r), (int)(y * h));
 				
 				if (colourRules != null)
 					colour = colourRules.getColour(cell, colourProperty);
 				else if (cell.getValue(colourProperty) > 0) 
 					colour = Colour.LIGHT_GREY;
 				
				if ((x & 1) == (y & 1)) // Checker-board pattern
 				{
 					Graphics.renderPolygon(gl, p, downTriangle, false, colour);
 					if (drawWireframe) Graphics.renderPolygon(gl, p, downTriangleBorder, true, cellBorderColour);
 				}
 				else
 				{
 					Graphics.renderPolygon(gl, p, upTriangle, false, colour);
 					if (drawWireframe) Graphics.renderPolygon(gl, p, upTriangleBorder, true, cellBorderColour);
 				}
 			}
 		
 		if (drawSelected)
 		{
 			Vector2i 	p = pos.add((int)(selectedCell.x * r), (int)(selectedCell.y * h));
 			
			if ((selectedCell.x & 1) == (selectedCell.y & 1)) // Checker-board pattern
 				Graphics.renderPolygon(gl, p, downTriangleBorder, true, cellSelectedBorderColour);
 			else
 				Graphics.renderPolygon(gl, p, upTriangleBorder, true, cellSelectedBorderColour);
 		}
 	}
 	
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		boolean handled = super.handleEvent(event, position);
 		
 		if ((event.type == Event.Type.MOUSE_CLICK) 
 				&& ((event.position.x < position.x) || (event.position.y < position.y) 
 				|| (event.position.x > position.x + size.x)
 				|| (event.position.y > position.y + size.y)))
 			return handled;
 		
 		if (event.type == Event.Type.MOUSE_CLICK)
 		{
 			Vector2i pos = event.position.subtract(position);
 			float s = cellSize * zoom;
 			float r = s / 2;
 			float h = (float)(s * Math.sqrt(2) * 0.5);
 			
 			pos.dec((int)(scroll.x * s), (int)(scroll.y * s));
 					
 			int y = (int)(pos.y / h);
 			float fy = pos.y / h - y;
 			int x = (int)(pos.x / r);
 			float fx = pos.x / r - x;
 			
 			System.out.println(fx + " " + fy);
 			
			if ((x & 1) == (y & 1))
 				x = x - (fy > fx ? 1 : 0);
 			else
 				x = x - ((1.0 - fy) > fx ? 1 : 0);
 									
 			if (x >= 0 && y >= 0 && x < grid.getWidth() && y < grid.getHeight())
 			{
 				Event changeEvent = new Event(Event.Type.CHANGE);
 				changeEvent.target = this;
 				window.sendWindowEvent(changeEvent);	
 				
 				selectedCell.set(x, y);
 				window.requestFocus(this);
 			}
 		}
 		
 		return handled;
 	}
 }
