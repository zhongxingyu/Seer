 package net.rptools.maptool.model;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 
 public class SquareGrid extends Grid {
 
 	private static final GridCapabilities CAPABILITIES = new GridCapabilities() {
 		public boolean isPathingSupported() {return true;}
 		public boolean isSnapToGridSupported() {return true;}
 	};
 	
 	public SquareGrid(Zone zone) {
 		super(zone);
 	}
 	
 	@Override
 	public CellPoint convert(ZonePoint zp) {
     	
     	double calcX = (zp.x-getOffsetX()) / (float)getSize();
     	double calcY = (zp.y-getOffsetY()) / (float)getSize();
 
     	boolean exactCalcX = (zp.x-getOffsetX()) % getSize() == 0;
     	boolean exactCalcY = (zp.y-getOffsetY()) % getSize() == 0;
     	
     	int newX = (int)(zp.x < 0 && !exactCalcX ? calcX-1 : calcX);
     	int newY = (int)(zp.y < 0 && !exactCalcY ? calcY-1 : calcY);
     	
     	//System.out.format("%d / %d => %f, %f => %d, %d\n", zp.x, getSize(), calcX, calcY, newX, newY);
         return new CellPoint(newX, newY);
 	}
 
 	@Override
 	public int getTokenSpace() {
 		return getSize();
 	}
 	
 	@Override
 	public ZonePoint convert(CellPoint cp) {
 
         return new ZonePoint((int)(cp.x * getSize() + getOffsetX()), 
         		(int)(cp.y * getSize() + getOffsetY()));
 	}
 
 	@Override
 	public GridCapabilities getCapabilities() {
 		return CAPABILITIES;
 	}
 
 	@Override
 	public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
 
 		double scale = renderer.getScale();
         double gridSize = getSize() * scale;
 
         g.setColor(new Color(getZone().getGridColor()));
         
         int offX = (int)(renderer.getViewOffsetX() % gridSize + getOffsetX()*scale);
         int offY = (int)(renderer.getViewOffsetY() % gridSize + getOffsetY()*scale);
 
         int startCol = (int)((int)(bounds.x / gridSize) * gridSize);
         int startRow = (int)((int)(bounds.y / gridSize) * gridSize);
         
        for (int row = startRow; row < bounds.y + bounds.height + gridSize; row += gridSize) {
             
             if (AppState.getGridSize() == 1) {
                g.drawLine(bounds.x, row + offY, bounds.x+bounds.width, row + offY);
             } else {
            	g.fillRect(bounds.x, row + offY - (AppState.getGridSize()/2), bounds.width, AppState.getGridSize());
             }
         }
 
        for (int col = startCol; col < bounds.x + bounds.width + gridSize; col += gridSize) {
             
             if (AppState.getGridSize() == 1) {
                g.drawLine(col + offX, bounds.y, col + offX, bounds.y + bounds.height);
             } else {
            	g.fillRect(col + offX - (AppState.getGridSize()/2), bounds.y, AppState.getGridSize(), bounds.height);
             }
         }
 	}
 
 }
