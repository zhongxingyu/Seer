 /**
  * (c) FH Potsdam
  */
 package org.fritzing.fritzing.diagram.edit.parts;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gmf.runtime.diagram.ui.internal.editparts.GridLayerEx;
 import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 
 public class SketchGridLayer extends GridLayerEx {
 
 	public SketchGridLayer() {
 		super();
 	}
 
 	public SketchGridLayer(Color color, Point p) {
 		super(color, p);
 	}
 
 	public SketchGridLayer(Color color) {
 		super(color);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.draw2d.Figure#setForegroundColor(org.eclipse.swt.graphics.Color)
 	 */
 	@Override
 	public void setForegroundColor(Color fg) {
 		// TODO Auto-generated method stub
 		super.setForegroundColor(fg);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.GridLayer#paintGrid(org.eclipse.draw2d.Graphics)
 	 */
 	@Override
 	protected void paintGrid(Graphics g) {
 		super.paintGrid(g);
 //		Rectangle clip = g.getClip(Rectangle.SINGLETON);
 //		
 ////		int dashDist = MapModeUtil.getMapMode().LPtoDP(gridX);
 ////		System.out.println("gridX "+gridX+"    dash "+dashDist);
 ////		int[] dashes = new int[]{5, dashDist-5};
 //
 //		int origLineStyle = g.getLineStyle();	
 //		g.setLineWidth(2);
 //		g.setLineStyle(SWT.LINE_SOLID);
 //		g.setLineDash(null);
 //		
 //		int j = gridY;
 //		if (gridX > 0) {
 //			if (origin.x >= clip.x)
 //				while (origin.x - gridX >= clip.x)
 //					origin.x -= gridX;
 //			else
 //				while (origin.x < clip.x)
 //					origin.x += gridX;
 //			for (int i = origin.x; i < clip.x + clip.width; i += gridX) {
 //				g.drawLine(i-50, j, i+50, j);
 //				g.drawLine(i, j-50, i, j+50);
 //			}
 //		}
 //		
 //		g.setLineStyle(origLineStyle);
 
 //		if (gridY > 0) {
 //			if (origin.y >= clip.y)
 //				while (origin.y - gridY >= clip.y)
 //					origin.y -= gridY;
 //			else
 //				while (origin.y < clip.y)
 //					origin.y += gridY;
 //			for (int i = origin.y; i < clip.y + clip.height; i += gridY) {
 //				g.setLineStyle(lineStyle);
 //				g.drawLine(clip.x, i, clip.x + clip.width, i);
 //				g.setLineStyle(origLineStyle);
 //			}
 //		}
 
 //		this.setBackgroundColor(this.getForegroundColor());
 //		g.setLineWidth(5);
 //		Rectangle clip = g.getClip(Rectangle.SINGLETON);
 //		
 //		if (gridX > 0) {
 //			if (origin.x >= clip.x)
 //				while (origin.x - gridX >= clip.x)
 //					origin.x -= gridX;
 //			else
 //				while (origin.x < clip.x)
 //					origin.x += gridX;
 //
 //			if (gridX > 0) {
 //				if (origin.y >= clip.y)
 //					while (origin.y - gridX >= clip.y)
 //						origin.y -= gridX;
 //				else
 //					while (origin.y < clip.y)
 //						origin.y += gridX;
 //			}
 //
 //			for (int j = origin.y; j < clip.y + clip.height; j += gridX)
 //				for (int i = origin.x; i < clip.x + clip.width; i += gridX) {
 ////					g.drawPoint(i, j);
 ////					g.fillOval(i, j, 10, 10);
 //					g.drawLine(i-3, j, i+3, j);
 //					g.drawLine(i, j-3, i, j+3);
 //				}
 //		}
 	}
 
	/* XXX: this currently needs to be hard-coded in 
	 * FritzingDiagramEditor.configureGraphicalViewer() due to a bug on Mac
	 */
 	public static final Color THIS_FORE = new Color(null, 140, 140, 140);
 }
