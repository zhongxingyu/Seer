 package diagram;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.Enumeration;
 
 /**
  * Title: Description: Copyright: Copyright (c) 2001 Company:
  * 
  * @author
  * @version 1.0
  */
 
 interface Renderer {
 
     abstract public void paint(DiagramElement d, Graphics g);
 
     abstract public boolean contains(DiagramElement d, int x, int y);
 }
 
 class DefaultRenderer implements Renderer {
 
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
         g.setColor(d.getForeground());
         g.drawRect(0, 0, w - 1, h - 1);
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         boolean result = d.contains2(x, y);
 
         return (result);
     }
 }
 
 class LineRenderer implements Renderer {
 
     public void paint(DiagramElement d, Graphics g) {
 
         DiagramElement source, target;
        Enumeration enumeration;
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
 
         DiagramConnector dc = (DiagramConnector) d;
 
         int fromX, fromY, toX, toY;
 
         for (int i = 1; i < dc._points.size(); i++) {
             fromX = (int) ((Point) dc._points.elementAt(i - 1)).getX() - dc.getX();
             fromY = (int) ((Point) dc._points.elementAt(i - 1)).getY() - dc.getY();
             toX = (int) ((Point) dc._points.elementAt(i)).getX() - dc.getX();
             toY = (int) ((Point) dc._points.elementAt(i)).getY() - dc.getY();
             g.drawLine(fromX, fromY, toX, toY);
         }
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         DiagramConnector dc = (DiagramConnector) d;
 
         if (dc.contain2(x, y) == -1) {
 //			dc.setReshapePointsVisible(false);
 
             return false;
         } else {
 //			System.err.println("In reneder");
 //			dc.setReshapePointsVisible(true);
             dc.requestFocus();
 
             return true;
         }
     }
 }
 
 class SolidArrowLineRenderer extends LineRenderer {
 
     public void paint(DiagramElement d, Graphics g) {
 
         super.paint(d, g);
 
         DiagramConnector dc = (DiagramConnector) d;
 
         int x1 = dc.getPoint(dc.getPoints().size() - 2).x;
         int y1 = dc.getPoint(dc.getPoints().size() - 2).y;
         int x2 = dc.getPoint(-1).x;
         int y2 = dc.getPoint(-1).y;
 
         int navX, navY;
 
         if (x1 < x2) {
             navX = 1;
         } else {
             navX = -1;
         }
 
         if (y1 < y2) {
             navY = 1;
         } else {
             navY = -1;
         }
 
         double a, b, c;
         double a2, b2;
         double lenX, lenY;
 
         int x3, y3;
 
         a = (x2 - x1) * navX;
         b = (y2 - y1) * navY;
 
         c = Point2D.distance(x1, y1, x2, y2);
 
         a2 = a / c * (c - 10);
         b2 = b / c * (c - 10);
 
         x3 = x1 + (int) (a2 * navX);
         y3 = y1 + (int) (b2 * navY);
 
         lenX = b / c * 5;
         lenY = a / c * 5;
 
         int[] pX = new int[3];
         int[] pY = new int[3];
 
         pX[0] = x2;
         pX[1] = (int) (x3 + lenX * navX);
         pX[2] = (int) (x3 - lenX * navX);
         pY[0] = y2;
         pY[1] = (int) (y3 - lenY * navY);
         pY[2] = (int) (y3 + lenY * navY);
 
         for (int i = 0; i < 3; i++) {
             pX[i] -= dc.getX();
             pY[i] -= dc.getY();
         }
 
         g.fillPolygon(pX, pY, 3);
     }
 }
 
 // modified by Kenny
 class DotArrowLineRenderer extends LineRenderer {
 
     public void paint(DiagramElement d, Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         g2.setStroke(new BasicStroke(1.5f));
 
         DiagramConnector dc = (DiagramConnector) d;
 
         int navX, navY;
         double a, b, c;
         int x1 = dc.getPoint(dc.getPoints().size() - 2).x;
         int y1 = dc.getPoint(dc.getPoints().size() - 2).y;
         int x2 = dc.getPoint(-1).x;
         int y2 = dc.getPoint(-1).y;
 
         if (x1 < x2) {
             navX = 1;
         } else {
             navX = -1;
         }
 
         if (y1 < y2) {
             navY = 1;
         } else {
             navY = -1;
         }
 
         double a2, b2;
         double lenX, lenY;
 
         int x3, y3;
 
         a = (x2 - x1) * navX;
         b = (y2 - y1) * navY;
 
         c = Point2D.distance(x1, y1, x2, y2);
 
         a2 = a / c * (c - 10);
         b2 = b / c * (c - 10);
 
         x3 = x1 + (int) (a2 * navX);
         y3 = y1 + (int) (b2 * navY);
 
         lenX = b / c * 5;
         lenY = a / c * 5;
 
         int[] pX = new int[3];
         int[] pY = new int[3];
 
         pX[0] = x2;
         pX[1] = (int) (x3 + lenX * navX);
         pX[2] = (int) (x3 - lenX * navX);
         pY[0] = y2;
         pY[1] = (int) (y3 - lenY * navY);
         pY[2] = (int) (y3 + lenY * navY);
 
         for (int i = 0; i < 3; i++) {
             pX[i] -= dc.getX();
             pY[i] -= dc.getY();
         }
 
         // paint the Arrow head
         g2.drawLine(pX[0], pY[0], pX[1], pY[1]);
         g2.drawLine(pX[0], pY[0], pX[2], pY[2]);
 
         // paint dotted line
         float dash[] = { 9.0f };
         g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
         paintLine(d, g2);
     }
 
     public void paintLine(DiagramElement d, Graphics2D g2) {
 
         DiagramElement source, target;
        Enumeration enumeration;
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g2.setColor(d.getBackground());
             g2.fillRect(0, 0, w, h);
         }
 
         g2.setColor(d.getForeground());
 
         DiagramConnector dc = (DiagramConnector) d;
 
         int fromX, fromY, toX, toY;
 
         for (int i = 1; i < dc._points.size(); i++) {
             fromX = (int) ((Point) dc._points.elementAt(i - 1)).getX() - dc.getX();
             fromY = (int) ((Point) dc._points.elementAt(i - 1)).getY() - dc.getY();
             toX = (int) ((Point) dc._points.elementAt(i)).getX() - dc.getX();
             toY = (int) ((Point) dc._points.elementAt(i)).getY() - dc.getY();
             g2.drawLine(fromX, fromY, toX, toY);
         }
     }
 }
 
 //original dotarrow line renderer, didn't not modified by kenny
 //class DotArrowLineRenderer extends LineRenderer {
 //	
 //	public void paint(DiagramElement d, Graphics g) {
 //		
 //		DiagramElement source, target;
 //		Enumeration enum;
 //		int w, h;
 //		
 //		w = d.getWidth();
 //		h = d.getHeight();
 //		if (d.isOpaque()) {
 //			g.setColor(d.getBackground());
 //			g.fillRect(0,0,w,h);
 //		}
 //		
 //		g.setColor(d.getForeground());
 //		
 //		DiagramConnector dc = (DiagramConnector) d;
 //		
 //		int navX, navY;
 //		
 //		int fromX, fromY, toX, toY;
 //		
 //		double a, b, c;
 //		
 //		for (int i = 1; i < dc._points.size(); i++)
 //		{
 //			fromX = (int) ((Point) dc._points.elementAt(i-1)).getX() - dc.getX();
 //			fromY = (int) ((Point) dc._points.elementAt(i-1)).getY() - dc.getY();
 //			toX = (int) ((Point) dc._points.elementAt(i)).getX() - dc.getX();
 //			toY = (int) ((Point) dc._points.elementAt(i)).getY() - dc.getY();
 //			
 //			if (fromX < toX)
 //				navX = 1;
 //			else
 //				navX = -1;
 //			
 //			if (fromY < toY)
 //				navY = 1;
 //			else
 //				navY = -1;
 //			
 //			a = (toX - fromX) * navX;
 //			b = (toY - fromY) * navY;
 //			
 //			c = Point.distance(fromX, fromY, toX, toY);
 //			
 //			double n = c/5;
 //			
 //			if (n == 0)
 //				n = 1;
 //			
 //			double tx, ty;
 //			tx = fromX;
 //			ty = fromY;
 //			for (int j = 0; j < n-1; j+=2) {
 //				g.drawLine((int) tx, (int) ty, (int) (tx + (a/n)*navX), (int) (ty + (b/n)*navY));
 //				tx += (a/n)*2*navX;
 //				ty += (b/n)*2*navY;
 //				
 ////				g.drawLine(fromX + ((int) (a/n)*navX)*j, fromY + ((int) (b/n)*navY)*j, fromX + ((int) (a/n)*navX)*(j+1), fromY + ((int) (b/n)*navY)*(j+1));
 //			}
 //		}
 //		
 //		int x1 = dc.getPoint(dc.getPoints().size()-2).x;
 //		int y1 = dc.getPoint(dc.getPoints().size()-2).y;
 //		int x2 = dc.getPoint(-1).x;
 //		int y2 = dc.getPoint(-1).y;
 //		
 //		if (x1 < x2)
 //			navX = 1;
 //		else
 //			navX = -1;
 //		
 //		if (y1 < y2)
 //			navY = 1;
 //		else
 //			navY = -1;
 //		
 //		double a2, b2;
 //		double lenX, lenY;
 //		
 //		int x3, y3;
 //		
 //		a = (x2 - x1) * navX;
 //		b = (y2 - y1) * navY;
 //		
 //		c = Point.distance(x1, y1, x2, y2);
 //		
 //		a2 = a/c*(c-10);
 //		b2 = b/c*(c-10);
 //		
 //		x3 = x1 + (int)(a2 * navX);
 //		y3 = y1 + (int)(b2 * navY);
 //		
 //		lenX = b/c*5;
 //		lenY = a/c*5;
 //		
 //		int[] pX = new int[3];
 //		int[] pY = new int[3];
 //		
 //		pX[0] = x2; pX[1] = (int)(x3+lenX*navX); pX[2] = (int)(x3-lenX*navX);
 //		pY[0] = y2; pY[1] = (int)(y3-lenY*navY); pY[2] = (int)(y3+lenY*navY);
 //		
 //		for (int i = 0; i < 3; i++) {
 //			pX[i] -= dc.getX();
 //			pY[i] -= dc.getY();
 //		}
 //		
 //		g.drawLine(pX[0], pY[0], pX[1], pY[1]);
 //		g.drawLine(pX[0], pY[0], pX[2], pY[2]);
 ////		g.fillPolygon(pX, pY, 3);
 //	}
 //}
 
 class InPortRenderer implements Renderer {
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
         g.fillOval(0, 0, w, h);
         g.setColor(d.getBackground());
         g.fillOval(1, 1, w - 2, h - 2);
         if (((DiagramPort) d).getPortIndex() != -1) {
             g.setColor(d.getBackground());
             g.setColor(Color.blue);
             g.drawString("" + ((DiagramPort) d).getPortIndex(), 4, 10);
         }
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         return (d.contains2(x, y));
     }
 }
 
 class OutPortRenderer implements Renderer {
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
         g.fillOval(0, 0, w, h);
         if (((DiagramPort) d).getPortIndex() != -1) {
             g.setColor(d.getBackground());
             g.setColor(new Color(255, 255, 255));
             g.drawString("" + ((DiagramPort) d).getPortIndex(), 4, 10);
         }
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         return (d.contains2(x, y));
     }
 }
 
 class ExitPortRenderer implements Renderer {
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
         g.fillOval(0, 0, w, h);
 
         g.setColor(d.getBackground());
         g.drawLine((int) (w * 0.25), (int) (h * 0.25), (int) (w * 0.75) - 1, (int) (h * 0.75) - 1);
         g.drawLine((int) (w * 0.25), (int) (h * 0.75) - 1, (int) (w * 0.75) - 1, (int) (h * 0.25));
 
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         return (d.contains2(x, y));
     }
 }
 
 class StartPortRenderer implements Renderer {
 
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
         g.fillOval(0, 0, w, h);
         g.setColor(d.getBackground());
         g.fillOval(1, 1, w - 2, h - 2);
         g.setColor(d.getForeground());
         g.fillOval(0, 0, (int) (w * 0.75), (int) (h * 0.75));
         g.setColor(d.getBackground());
         g.fillOval(1, 1, (int) (w * 0.6), (int) (h * 0.6));
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         return (d.contains2(x, y));
     }
 }
 
 class EndPortRenderer implements Renderer {
 
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
         g.fillOval(0, 0, w, h);
         g.setColor(d.getBackground());
         g.fillOval(1, 1, w - 2, h - 2);
 
         g.setColor(d.getForeground());
         g.fillOval(1, 1, (int) (w * 0.6), (int) (h * 0.6));
         g.setColor(d.getBackground());
         g.fillOval(1, 1, (int) (w * 0.4), (int) (h * 0.4));
 
         g.setColor(d.getForeground());
         g.fillOval((int) (w * 0.3) + 1, 1, (int) (w * 0.6), (int) (h * 0.6));
         g.setColor(d.getBackground());
         g.fillOval((int) (w * 0.5) + 1, 1, (int) (w * 0.4), (int) (h * 0.4));
 
         g.setColor(d.getForeground());
         g.fillOval(1, (int) (h * 0.3) + 1, (int) (w * 0.6), (int) (h * 0.6));
         g.setColor(d.getBackground());
         g.fillOval(1, (int) (h * 0.5) + 1, (int) (w * 0.4), (int) (h * 0.4));
 
         g.setColor(d.getForeground());
         g.fillOval((int) (w * 0.3) + 1, (int) (h * 0.3) + 1, (int) (w * 0.6), (int) (h * 0.6));
         g.setColor(d.getBackground());
         g.fillOval((int) (w * 0.5) + 1, (int) (h * 0.5) + 1, (int) (w * 0.4), (int) (h * 0.4));
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         return (d.contains2(x, y));
     }
 }
 
 // add by Kenny
 class DataSourcePortRenderer implements Renderer {
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
         g.fillOval(0, 0, w, h);
         if (((DiagramPort) d).getPortIndex() != -1) {
             g.setColor(d.getBackground());
             g.setColor(new Color(255, 255, 255));
             g.drawString("" + ((DiagramPort) d).getPortIndex(), 4, 10);
         }
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         return (d.contains2(x, y));
     }
 }
 
 // add by Kenny
 class ChartPortRenderer implements Renderer {
     public void paint(DiagramElement d, Graphics g) {
         int w, h;
 
         w = d.getWidth();
         h = d.getHeight();
         if (d.isOpaque()) {
             g.setColor(d.getBackground());
             g.fillRect(0, 0, w, h);
         }
 
         g.setColor(d.getForeground());
         g.fillOval(0, 0, w, h);
         if (((DiagramPort) d).getPortIndex() != -1) {
             g.setColor(d.getBackground());
             g.setColor(new Color(255, 255, 255));
             g.drawString("" + ((DiagramPort) d).getPortIndex(), 4, 10);
         }
     }
 
     public boolean contains(DiagramElement d, int x, int y) {
         return (d.contains2(x, y));
     }
 }
