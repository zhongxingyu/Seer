 package visualize;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.RenderingHints;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 class Surface extends JPanel {
 
 	ArrayList<ArrayList<double[]>> points;
 	ArrayList<double []> debug;
 	int stroke, mult, radius;
 	boolean disp;
 
 	public Surface (ArrayList<ArrayList<double[]>> x, ArrayList<double[]> y, 
 			int sizeOfTheBrush, boolean displayCoordinates) {
 		points = x;
 		debug = y;
 		stroke = sizeOfTheBrush;
 		disp = displayCoordinates;
 		radius = stroke * 2;
 	}
 
 	private void doDrawing(Graphics g) {
 
 		int color, num;
 		double cordx, cordy, cordx2 = 0, cordy2 = 0;
 		
 		Graphics2D g2d = (Graphics2D) g;
 
 		Dimension size = getSize();
 		Insets insets = getInsets();
 
 		int w = size.width - insets.left - insets.right;
 		int h = size.height - insets.top - insets.bottom;
 		mult = Math.min(w,h);
 
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
 				RenderingHints.VALUE_RENDER_QUALITY);
 
 		BasicStroke bs = new BasicStroke(stroke, BasicStroke.CAP_ROUND,
 				BasicStroke.JOIN_ROUND);
 
 		g2d.setStroke(bs);
 		
 		//draw point that aren't in the hull
 		for (int k = 0; k < debug.size(); k++) {
 			cordx = getCoord(k,0,true);
 			cordy = getCoord(k,1,true);
 			g2d.setColor(new Color(0, 200, 0));
 			drawPoint(cordx, cordy, g2d);
 		}
 
 		for (int i = 0; i < points.size(); i++) {
 			num = points.get(i).size() - 1;
 			//draw first point
 			if (num >= 0) {
 				g2d.setColor(new Color(0, 0, 200));
 				cordx = getCoord(0,0,i);
 				cordy = getCoord(0,1,i);
 				drawPoint(cordx, cordy, g2d);
 			}
 	
 			for (int k = 0; k < num; k++) {
 				cordx = getCoord(k,0,i);
 				cordy = getCoord(k,1,i);
 				cordx2 = getCoord(k+1,0,i);
 				cordy2 = getCoord(k+1,1,i);
 				color = (int) Math.round(points.get(i).get(k+1)[2]);
 				System.out.print(points.get(i).get(k+1)[2] + "\n");
 				g2d.setColor(new Color(color, color, color));
 				drawLine(cordx, cordy, cordx2, cordy2, g2d);
 				if (k < 1) g2d.setColor(new Color(0, 0, 200));
 				else g2d.setColor(new Color(0, 0, 0));
 				drawPoint(cordx, cordy, g2d);          
 			}
 	
 			//color last point
 			if (num > 0) {
 				g2d.setColor(new Color(200, 0, 0));
 				drawPoint(cordx2, cordy2, g2d);
 			}	
 		}
 	}
 
 	private double getCoord(int index, int coord, int chain) {
 		if (coord == 0) return points.get(chain).get(index)[0];
 		else return (1 - points.get(chain).get(index)[1]);
 	}
 	
 	private double getCoord(int index, int coord, boolean ifDebug) {
 		if (coord == 0) return debug.get(index)[0];
 		else return (1 - debug.get(index)[1]);
 	}
 	
 	private void drawPoint(double x, double y, Graphics2D g2d) {
 		double cx = x * mult;
 		double cy = y * mult; 
		if (disp) {
 		g2d.drawString("[" + String.format("%1$,.2f", x) + "," + String.format("%1$,.2f", 1 - y) + "]",
 				Math.round(cx + radius), Math.round(cy));
		}
 		g2d.fill(new Ellipse2D.Double(cx - radius/2, cy - radius/2, (double) radius, (double) radius));
 	}
 	
 	private void drawLine(double x1, double y1, double x2, double y2, Graphics2D g2d) {
 		g2d.draw(new Line2D.Double(x1 * mult, y1 * mult, x2 * mult, y2 * mult));
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 
 		super.paintComponent(g);
 		doDrawing(g);
 	}
 }
