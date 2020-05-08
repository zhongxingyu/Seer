 package com.hexcore.cas.ui;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2f;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.HexagonGrid;
 
 public class HexagonGridWidget extends GridWidget<HexagonGrid>
 {
 	public HexagonGridWidget(HexagonGrid grid, int tileSize)
 	{
 		this(new Vector2i(0, 0), grid, tileSize);
 	}
 
 	public HexagonGridWidget(Vector2i position, HexagonGrid grid, int tileSize)
 	{
 		super(new Vector2i(grid.getWidth() * 2 * (int)(tileSize * 0.866f) + (int)(tileSize * 0.866f), 
 						   grid.getHeight() * (tileSize + tileSize / 2) + tileSize / 2),
 						   grid, tileSize);
 	}
 
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		int s = tileSize;
 		int	h = tileSize / 2;
 		int r = (int)(tileSize * Math.cos(30.0 * Math.PI / 180.0));
 		
		Vector2i 	pos = this.position.add(position);
 		Vector2f[]	hexagon = new Vector2f[6];
 		hexagon[0] = new Vector2f(r, 	0);
 		hexagon[1] = new Vector2f(r+r, 	h);
 		hexagon[2] = new Vector2f(r+r,	h+s);
 		hexagon[3] = new Vector2f(r,	h+s+h);
 		hexagon[4] = new Vector2f(0, 	h+s);
 		hexagon[5] = new Vector2f(0, 	h);
 		
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				Cell 		cell = grid.getCell(x, y);
 				Colour		colour = Colour.DARK_GREY;
 				Vector2i	p = pos.add(x*r*2, y*(s+h));
 				if ((y & 1) == 1) p.inc(r, 0);
 				
 				if (colourRules != null)
 					colour = colourRules.getColour(cell, colourProperty);
 				else if (cell.getValue(colourProperty) > 0) 
 					colour = Colour.LIGHT_GREY;
 					
 				Graphics.renderPolygon(gl, p, hexagon, false, colour);
 				Graphics.renderPolygon(gl, p, hexagon, true, Colour.WHITE);
 			}
 	}	
 }
