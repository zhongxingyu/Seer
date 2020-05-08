 package com.hexcore.cas.ui.toolkit;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2f;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.HexagonGrid;
 
 public class HexagonGridWidget extends Grid2DWidget
 {
 	public HexagonGridWidget(HexagonGrid grid, int cellSize)
 	{
 		this(new Vector2i(0, 0), grid, cellSize);
 	}
 
 	public HexagonGridWidget(Vector2i position, HexagonGrid grid, int cellSize)
 	{
 		super(new Vector2i(grid.getWidth() * (cellSize + cellSize / 2) + cellSize / 2, 
 						   grid.getHeight() * 2 * (int)(cellSize * 0.866f) + (int)(cellSize * 0.866f)),
 						   grid, cellSize);
 	}
 
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		Vector2i pos = this.position.add(position);
 		Graphics.renderRectangle(gl, pos, size, backgroundColour);
 		
 		float s = cellSize * zoom;
 		float h = s / 2;
 		float r = (float)(s * Math.cos(30.0 * Math.PI / 180.0));
 		
 		pos.inc((int)(scroll.x * s), (int)(scroll.y * s));
 		
 		Vector2f[]	hexagon = new Vector2f[6];
 		hexagon[0] = new Vector2f(0.0f,	r);
 		hexagon[1] = new Vector2f(h,	r+r);
 		hexagon[2] = new Vector2f(h+s,	r+r);
 		hexagon[3] = new Vector2f(h+s+h,r);
 		hexagon[4] = new Vector2f(h+s,	0.0f);
 		hexagon[5] = new Vector2f(h,	0.0f);
 		
 		Vector2f[]	hexagonBorder = new Vector2f[6];
 		hexagonBorder[0] = new Vector2f(0.5f,       r+0.5f);
 		hexagonBorder[1] = new Vector2f(h+0.5f,     r+r+0.5f);
 		hexagonBorder[2] = new Vector2f(h+s+0.5f,   r+r+0.5f);
 		hexagonBorder[3] = new Vector2f(h+s+h+0.5f, r+0.5f);
 		hexagonBorder[4] = new Vector2f(h+s+0.5f,   0.5f);
 		hexagonBorder[5] = new Vector2f(h+0.5f,     0.5f);		
 		
 		for (Vector2f v : hexagon) v.inc(0.5f, 0.5f);
 		
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				Cell 		cell = grid.getCell(x, y);
 				Colour		colour = Colour.DARK_GREY;
 				Vector2i	p = pos.add((int)(x*(s+h)), (int)(y*r*2));
 				
				if ((x & 1) == 1) p.inc(0, (int)r);
 				
 				if (colourRules != null)
 					colour = colourRules.getColour(cell, colourProperty);
 				else if (cell.getValue(colourProperty) > 0) 
 					colour = Colour.LIGHT_GREY;
 					
 				Graphics.renderPolygon(gl, p, hexagon, false, colour);
 				if (drawWireframe) Graphics.renderPolygon(gl, p, hexagonBorder, true, cellBorderColour);
 			}
 		
 		if (drawSelected)
 		{
 			Vector2i	p = pos.add((int)(selectedCell.x*(s+h)), (int)(selectedCell.y*r*2));
			if ((selectedCell.x & 1) == 1) p.inc(0, (int)r);	
 			
 			Graphics.renderPolygon(gl, p, hexagonBorder, true, cellSelectedBorderColour);
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
 			float h = s / 2;
 			float r = (float)(s * Math.cos(30.0 * Math.PI / 180.0));
 			
 			pos.dec((int)(scroll.x * s), (int)(scroll.y * s));
 						
 			int x = (int)((pos.x - h/2) / (s+h));
 			int y = (int)(pos.y / (r*2));
 			
 			Vector2i p = new Vector2i((int)(x*(s+h)), (int)(y*r*2));
			if ((x & 1) == 1) p.inc(0, (int)r);	
 			
 			p = pos.subtract(p);
 			
 			if (p.y < 0) y--;
 			
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
