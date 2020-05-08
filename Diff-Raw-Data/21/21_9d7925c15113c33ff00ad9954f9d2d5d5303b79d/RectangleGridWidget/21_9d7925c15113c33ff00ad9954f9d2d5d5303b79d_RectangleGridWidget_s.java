 package com.hexcore.cas.ui;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.RectangleGrid;
 
 public class RectangleGridWidget extends GridWidget<RectangleGrid>
 {
 	public RectangleGridWidget(RectangleGrid grid, int tileSize)
 	{
 		this(new Vector2i(0, 0), grid, tileSize);
 	}
 
 	public RectangleGridWidget(Vector2i position, RectangleGrid grid, int tileSize)
 	{
 		super(new Vector2i(grid.getWidth() * tileSize, grid.getHeight() * tileSize), grid, tileSize);
 	}
 
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		Vector2i pos = this.position.add(position);
 		
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				Cell 		cell = grid.getCell(x, y);
 				Colour		colour = Colour.DARK_GREY;
 				
 				if (colourRules != null)
 					colour = colourRules.getColour(cell, colourProperty);
 				else if (cell.getValue(colourProperty) > 0) 
 					colour = Colour.LIGHT_GREY;
 					
 				Graphics.renderRectangle(gl, pos.add(x * tileSize, y * tileSize), new Vector2i(tileSize, tileSize), colour);
 				Graphics.renderBorder(gl, pos.add(x * tileSize, y * tileSize), new Vector2i(tileSize, tileSize), Colour.WHITE);
 			}
 	}	
 }
