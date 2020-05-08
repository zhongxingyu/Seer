 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import javax.swing.JPanel;
 
 public class VisPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener {
 	private float xValues[], yValues[];
 	private int xPoints[], yPoints[];
 	private float maxX, minX, maxY, minY;
 	// defines the region inside the plot axes
 	private Rectangle plotRectangle;
 	// defines a border around the plot
 	private int borderSize = 10; 
 	// shape of the data marks
 	private Shape dataShape = new Ellipse2D.Float(0.f, 0.f, 5.f, 5.f);
 	// color of the data marks
 	private Color dataColor = new Color(50, 50, 50, 150);
 	private boolean antialiasEnabled = true;
 	private Point mousePoint;
 	
 	// for drawing the plot offscreen for better efficiency
 	private BufferedImage offscreenImage;
 	
 	public VisPanel () {
 		addComponentListener(this);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	// sets the data values and forces the panel to layout the boundaries and
 	// compute data points
 	public void setData(float xValues[], float yValues[]) {
 		this.xValues = xValues;
 		this.yValues = yValues;
 		
 		maxX = Float.MIN_VALUE;
 		minX = Float.MAX_VALUE;
 		for (float value : xValues) {
 			if (value > maxX) {
 				maxX = value;
 			}
 			if (value < minX) {
 				minX = value;
 			}
 		}
 		
 		maxY = Float.MIN_VALUE;
 		minY = Float.MAX_VALUE;
 		for (float value : yValues) {
 			if (value > maxY) {
 				maxY = value;
 			}
 			if (value < minY) {
 				minY = value;
 			}
 		}
 		
		offscreenImage = null;
 		layoutPlot();
 		calculatePoints();
 		repaint();
 	}
 	
 	private void layoutPlot() {
 		if (xValues == null || yValues == null) {
 			return;
 		}
 		
 		// forces the scatterplot to be square using the smaller dimension (width or height)
 		int plotSize = getWidth();
 		if (getHeight() < getWidth()) {
 			plotSize = getHeight();
 		}
 		
 		// centers the scatterplot in the middle of the panel
 		int xOffset = (getWidth() - plotSize) / 2;
 		int yOffset = (getHeight() - plotSize) / 2;
 		
 		// get the dimensions of the plot region for later use
 		int left = borderSize + xOffset;
 		int right = xOffset + (plotSize - (borderSize*2));
 		int bottom = yOffset + (plotSize - (borderSize*2));
 		int top = borderSize + yOffset;
 		plotRectangle = new Rectangle(left, top, (right-left), (bottom-top));
 	}
 	
 	// converts x value to screen pixel location
 	private int toScreenX(float value, float minValue, float maxValue, int offset, int plotWidth) {
 		float norm = (value - minValue) / (maxValue - minValue);
 		int x = offset + (int)(Math.round(norm * plotWidth));
 		return x;
 	}
 	
 	// converts y value to screen pixel location
 	private int toScreenY(float value, float minValue, float maxValue, int offset, int plotHeight) {
 		float normVal = 1.f - ((value - minValue) / (maxValue - minValue));
 		int y = offset + (int)(Math.round(normVal * plotHeight));
 		return y;
 	}
 	
 	// computes the x and y pixel locations for scatterplot data
 	private void calculatePoints() {
 		// nothing to compute
 		if (xValues == null || yValues == null) {
 			return;
 		}
 		
 		xPoints = new int[xValues.length];
 		for (int i = 0; i < xValues.length; i++) {
 			xPoints[i] = toScreenX(xValues[i], minX, maxX, 0, plotRectangle.width);
 		}
 		
 		yPoints = new int[yValues.length];
 		for (int i = 0; i < yValues.length; i++) {
 			yPoints[i] = toScreenY(yValues[i], minY, maxY, 0, plotRectangle.height);
 		}
 	}
 	
 	private void render(Graphics2D g2) {
 		if (xPoints != null && yPoints != null) {
 			g2.setColor(dataColor);
 			g2.translate(plotRectangle.x, plotRectangle.y);
 			
 			for (int i = 0; i < xPoints.length; i++) {
 				int x = xPoints[i] - (int)(dataShape.getBounds2D().getWidth() / 2.);
 				int y = yPoints[i] - (int)(dataShape.getBounds2D().getHeight() / 2.);
 				
 				g2.translate(x, y);
 				g2.draw(dataShape);
 				g2.translate(-x, -y);
 			}
 			
 			g2.translate(-plotRectangle.x, -plotRectangle.y);
 			
 			g2.setStroke(new BasicStroke(2.f));
 			g2.setColor(Color.LIGHT_GRAY);
 			g2.draw(plotRectangle);
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setColor(getBackground());
 		g2.fillRect(0, 0, getWidth(), getHeight());
 		
 		if (antialiasEnabled) {
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);				
 			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		}
 		
 		if (xValues != null && yValues != null) {
 			render(g2);
 
 			// code below draws in offscreen image for buffering and greater efficiency
 //			if (offscreenImage == null) {
 //				offscreenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
 //				Graphics2D offscreenImageGraphics = (Graphics2D)offscreenImage.getGraphics();
 //				if (antialiasEnabled) {
 //					offscreenImageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);				
 //					offscreenImageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 //				}
 //				
 //				render(offscreenImageGraphics);
 //			}
 //		
 //			g2.drawImage(offscreenImage, 0, 0, this);
 		}
 	
 		// draw the mouse location
 		if (mousePoint != null && plotRectangle != null) {
 			g2.setColor(Color.LIGHT_GRAY);
 			g2.drawLine(mousePoint.x, plotRectangle.y, mousePoint.x, plotRectangle.y+plotRectangle.height);
 			g2.drawLine(plotRectangle.x, mousePoint.y, plotRectangle.x+plotRectangle.width, mousePoint.y);
 		}
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		if (plotRectangle != null) {
 			if (plotRectangle.contains(e.getPoint())) {
 				mousePoint = e.getPoint();
 				repaint();
 			} else {
 				mousePoint = null;
 				repaint();
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		setCursor(Cursor.getDefaultCursor());
 	}
 
 	@Override
 	public void componentResized(ComponentEvent e) {
		offscreenImage = null;
 		layoutPlot();
 		calculatePoints();
 		repaint();
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent e) {
 	}
 
 	@Override
 	public void componentShown(ComponentEvent e) {
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent e) {
 	}
 }
