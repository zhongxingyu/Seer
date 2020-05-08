 package com.hexcore.cas.ui.toolkit;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2f;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.RectangleGrid;
 
 public class RectangleGrid3DWidget extends Grid3DWidget
 {
 	public RectangleGrid3DWidget(Vector2i size, RectangleGrid grid, int cellSize)
 	{
 		super(size, grid, cellSize);
 	}
 	
 	@Override
 	public void loadGeometry(GL gl)
 	{
 		float	s = cellSize;
 		
 		Vector2f[]	rect = new Vector2f[4];
 		rect[0] = new Vector2f(0.0f, 0.0f);
 		rect[1] = new Vector2f(s, 0.0f);
 		rect[2] = new Vector2f(s, s);
 		rect[3] = new Vector2f(0.0f, s);
 		
 		if (reallyDirty.getAndSet(false))
			setupVertexBuffer(gl, 4);
 		
 		resetVertexBuffer(gl, 4);
 
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				Cell 		cell = grid.getCell(x, y);
 				Vector2f	p = new Vector2f(x * cellSize, y * cellSize);
 				
 				addColumn(p, cell, rect);
 			}
 		
 		loadVertexBuffer(gl);	
 	}
 }
