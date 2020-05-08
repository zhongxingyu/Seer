 package by.framework.ecgview;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.Stroke;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.font.FontRenderContext;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class Ploter extends JPanel {
 
 	private class MouseHandler extends MouseAdapter {
 		@Override
 		public void mouseClicked(MouseEvent event) {
 			if(event.getButton() == 3) {
 				if(history.size() > 0) {
 					Border border = history.pop();
 					maxX = border.getMaxX();
 					minX = border.getMinX();
 					maxY = border.getMaxY();
 					minY = border.getMinY();
 					zoomToRegion(minX, maxY, maxX, minY);
 				} 
 				else
 					zoomToRegion(minX, maxY, maxX, minY);
 			}
 			repaint();
 		}
 
 		@Override
 		public void mousePressed(MouseEvent event) {
 			if(event.getButton() != 1)
 				return;
 			selectedMarker = findSelectedPoint(event.getX(), event.getY());
 			originalPoint = translatePointToXY(event.getX(), event.getY());
 			if(selectedMarker >= 0) {
 				changeMode = true;
 				setCursor(Cursor.getPredefinedCursor(8));
 			} 
 			else {
 				scaleMode = true;
 				setCursor(Cursor.getPredefinedCursor(5));
 				selectionRect.setFrame(event.getX(), event.getY(), 1.0D, 1.0D);
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent event) {
 			if(event.getButton() != 1)
 				return;
 			setCursor(Cursor.getPredefinedCursor(0));
 			if(changeMode) {
 				changeMode = false;
 			}
 			else {
 				scaleMode = false;
 				EcgPoint finalPoint = translatePointToXY(event.getX(), event.getY());
 				if(Math.abs(originalPoint.getX() - finalPoint.getX()) > 1E-4 
 						&& Math.abs(originalPoint.getY() - finalPoint.getY()) > 1E-4) {
 					history.add(new Border(maxX, minX, maxY, minY));
 					zoomToRegion(originalPoint.getX(), originalPoint.getY(), 
 							finalPoint.getX(), finalPoint.getY());
 				}
 				repaint();
 			}
 		}
 	}
 
 	private class MouseMotionHandler implements MouseMotionListener {
 
 		@Override
 		public void mouseMoved(MouseEvent event) {
 			selectedMarker = findSelectedPoint(event.getX(), event.getY());
 			if(selectedMarker >= 0)
 				setCursor(Cursor.getPredefinedCursor(8));
 			else
 				setCursor(Cursor.getPredefinedCursor(0));
 			repaint();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent event) {
 			if(changeMode) {
 				EcgPoint currentPoint = translatePointToXY(event.getX(), event.getY());
 				double newY = (points.get(selectedMarker).getY() + (currentPoint.getY() - points.get(selectedMarker).getY()));
 				if(newY > maxY)
 					newY = maxY;
 				if(newY < minY)
 					newY = minY;
 				points.get(selectedMarker).setY(newY);
 				repaint();
 			} else {
 				double width = (double)event.getX() - selectionRect.getX();
 				if(width < 50D)
 					width = 50D;
 				double height = (double)event.getY() - selectionRect.getY();
 				if(height < 50D)
 					height = 50D;
 				selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
 				repaint();
 			}
 		}
 
 	}
 
 	private ArrayList<EcgPoint> points = new ArrayList<EcgPoint>();
 	private ArrayList<EcgPoint> originalPoints;
 	private double scaleX;
 	private double scaleY;
 
 	private boolean showAxis = true;
 	private boolean showMarkers = true;
 	private boolean rotate = false;
 
 	private BasicStroke graphicsStroke;
 	private BasicStroke axisStroke;
 	private BasicStroke markerStroke;
 	private Font axisFont;
 	private Font labelsFont;
 	private BasicStroke selectionStroke;
 
 	private Stack<Border> history = new Stack<Border>();
 
 	private double minX, maxX, minY, maxY;
 	private double dx = 0, dy = 0;
 	private boolean scaleMode;
 	private boolean changeMode;
 	private Rectangle2D.Double selectionRect = new Rectangle2D.Double();;
 	public EcgPoint originalPoint;
 
 	private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();
 
 	private int selectedMarker = -1;
 
 	private int iterFirst;
 	private int iterLast;
 
 	public Ploter() {
 		setBackground(Color.WHITE);
 
 		float[] dash2 = null; //{20, 5, 5, 5, 5, 5, 10, 5, 10, 5};
 		graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, 
 				BasicStroke.JOIN_BEVEL, 10f, dash2, 0f);
 
 		axisStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
 				BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
 		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
 				BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
 
 		axisFont = new Font("Serif", Font.PLAIN, 12);
 
 		selectionStroke = new BasicStroke(1.0F, 0, 0, 10F, new float[] {10F, 10F}, 0.0F);
 
 		labelsFont = new Font("Serif", 0, 10);
 		formatter.setMaximumFractionDigits(3);
 
 		addMouseListener(new MouseHandler());
 		addMouseMotionListener(new MouseMotionHandler());
 	}
 
 	public void paintGraphic(ArrayList<EcgPoint> points) {
 		this.originalPoint = null;
 		this.originalPoints = points;
 		this.points.clear();
 		for(EcgPoint point : originalPoints) {
 			this.points.add(new EcgPoint(point.getX(), point.getY()));
 		}
 
 		maxX = points.get(points.size()-1).getX();
 		minX = points.get(0).getX();
 
 		maxY = points.get(0).getY();
 		minY = points.get(0).getY();
 
 		for(int i = 1; i < points.size(); i++) {
 			if(minY > points.get(i).getY())
 				minY = points.get(i).getY();
 			if(maxY < points.get(i).getY())
 				maxY = points.get(i).getY();
 		}
 
 		zoomToRegion(minX, maxY, maxX, minY);
 
 		repaint();
 	}
 
 	public void setShowAxis(boolean show) {
 		showAxis = show;
 		repaint();
 	}
 
 	public void setShowMarkers(boolean show) {
 		this.showMarkers = show;
 		repaint();
 	}
 
 	public void setRotate(boolean rotate) {
 		this.rotate = rotate;
 		repaint();
 	}
 
 	public void zoomToRegion(double minx, double maxy, double maxx, double miny) {
 		maxX = maxx;
 		minX = minx;
 		maxY = maxy;
 		minY = miny;
 
 		int iter = 0;
 		do {
 			++iter;
 		} while(points.get(iter).getX()<minX);
 		iterFirst = iter;
 
 		do {
 			++iter;
 		} while(points.get(iter).getX()<maxX);
 		iterLast = iter;
 		repaint();
 	}
 
 	public void reset() {
 		paintGraphic(originalPoints);
 	}
 
 	public ArrayList<EcgPoint> getPoints() {
 		if(originalPoints != null) {
 			return points;
 		} else return null;
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		if(points == null || points.isEmpty()) return;
 
 		Graphics2D canvas = (Graphics2D) g;
 		Stroke oldStroke = canvas.getStroke();
 		Color oldColor = canvas.getColor();
 		Paint oldPaint = canvas.getPaint();
 		Font oldFont = canvas.getFont();
 
 		scaleX = getSize().getWidth()/(maxX - minX);
 		scaleY = getSize().getHeight()/(maxY - minY);
 
 		dx = 0; dy = 0;
 		if(rotate) {
 
 			double X = getSize().getWidth()/2.0;
 			double Y = getSize().getHeight()/2.0;
 
 			scaleX = getSize().getHeight()/(maxX - minX);
 			scaleY = getSize().getWidth()/(maxY - minY);
 
 			dx = (Y - X) / scaleX;
 			dy = (X - Y) / scaleY;
 			canvas.rotate(-Math.PI/2., X, Y);
 		}
 
 		if(showAxis) paintAxis(canvas);
 		printGraphic(canvas);
 		if(showMarkers) paintMarkers(canvas);
 		paintSelection(canvas);
 
 		canvas.setFont(oldFont);
 		canvas.setPaint(oldPaint);
 		canvas.setColor(oldColor);
 		canvas.setStroke(oldStroke);
 	}
 
 	private void paintSelection(Graphics2D canvas) {
 		if(!scaleMode)
 			return;
 		else {
 			canvas.setStroke(selectionStroke);
 			canvas.setColor(Color.BLACK);
 			canvas.draw(selectionRect);
 			return;
 		}
 	}
 
 	private void printGraphic(Graphics2D canvas) {		
 		canvas.setStroke(graphicsStroke);
 		canvas.setColor(Color.RED);
 		GeneralPath graphics = new GeneralPath();
 		for(int i = iterFirst; i <= iterLast; i++) {
 			Point2D.Double pointStart = 
 					xyToPoint(points.get(i-1).getX(), points.get(i-1).getY());
 			Point2D.Double pointFinish = 
 					xyToPoint(points.get(i).getX(), points.get(i).getY());
 			graphics.append(new Line2D.Double(pointStart.getX(), 
 					pointStart.getY(), pointFinish.getX(), pointFinish.getY()), true);
 		}
 		canvas.draw(graphics);
 	}
 
 	private void paintMarkers(Graphics2D g2) {
 		g2.setStroke(markerStroke);
 		g2.setColor(Color.RED);
 		g2.setPaint(Color.RED);
 
 		for(int iter = iterFirst - 1; iter <= iterLast; iter++) {
 
 			EcgPoint p = points.get(iter);
 
 			double radius = 3;
 			if(iter == selectedMarker) {
 				radius+=2;
 				g2.setPaint(Color.GREEN);
 			} else {
 				g2.setPaint(Color.RED);
 			}
 
 			Point2D.Double point = xyToPoint(p.getX(), p.getY());
 			Point2D.Double corner = shiftPoint(point, radius, radius);
 
 			Ellipse2D.Double ellipse = new Ellipse2D.Double();
 			ellipse.setFrameFromCenter(point, corner);
 			g2.draw(ellipse);
 			g2.fill(ellipse);
 
 			if(selectedMarker >= 0) {
				/*
				 * TODO: Fix label outside panel
				 */
				FontRenderContext context = g2.getFontRenderContext();
 				Point2D.Double selectedPoint = translateXYtoPoint(points.get(selectedMarker).getX(),  points.get(selectedMarker).getY());
				String label = (new StringBuilder("X=")).append(formatter.format(points.get(selectedMarker).getX())).append(", Y=").append(
						formatter.format(points.get(selectedMarker).getY())).toString();
				Rectangle2D bounds = labelsFont.getStringBounds(label, context);
 				
 				g2.setColor(Color.BLUE);
				g2.drawString(label, (float)(selectedPoint.getX()), (float)(selectedPoint.getY() - bounds.getHeight()));
 			}
 		}
 	}
 
 	private void paintAxis(Graphics2D g2) {
 		g2.setStroke(axisStroke);
 		g2.setColor(Color.BLACK);
 		g2.setPaint(Color.BLACK);
 		g2.setFont(axisFont);
 		FontRenderContext context = g2.getFontRenderContext();
 
 		if(minX <= 0.0 && maxX >= 0.0) {
 			g2.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
 			GeneralPath arrow = new GeneralPath();
 			Point2D.Double lineEnd = xyToPoint(0, maxY);
 			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
 			arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);
 			arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());
 			arrow.closePath();
 			g2.draw(arrow);
 			g2.fill(arrow);
 			Rectangle2D bounds = axisFont.getStringBounds("y", context);
 			Point2D.Double labelPos = xyToPoint(0, maxY);
 			g2.drawString("y", 
 					(float)labelPos.getX() + 10, 
 					(float)(labelPos.getY() - bounds.getY()));
 		}
 
 		if(minY <= 0.0 && maxY >= 0.0) {
 			Line2D.Double lineX = new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0));
 			g2.draw(lineX);
 			GeneralPath arrow = new GeneralPath();
 			Point2D.Double lineEnd = xyToPoint(maxX, 0);
 			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
 			arrow.lineTo(arrow.getCurrentPoint().getX()-20, arrow.getCurrentPoint().getY()-5);
 			arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY()+10);
 			arrow.closePath();
 			g2.draw(arrow);
 			g2.fill(arrow);
 			Rectangle2D bounds = axisFont.getStringBounds("x", context);
 			Point2D.Double labelPos = xyToPoint(maxX, 0);
 			g2.drawString("x", 
 					(float)(labelPos.getX() - bounds.getWidth() - 10), 
 					(float)(labelPos.getY() + bounds.getY()));
 		}
 	}
 
 	protected Point2D.Double xyToPoint(double x, double y) {
 		double deltaX = x - minX;
 		double deltaY = maxY - y;
 		return new Point2D.Double((-dx + deltaX)*scaleX, (-dy + deltaY)*scaleY);
 	}
 
 	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
 		Point2D.Double dest = new Point2D.Double();
 		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
 		return dest;
 	}
 
 	protected Point2D.Double translateXYtoPoint(double x, double y) {
 		double deltaX = x - minX;
 		double deltaY = maxY - y;
 		return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
 	}
 
 	protected EcgPoint translatePointToXY(int x, int y) {
 		return (new EcgPoint(minX + (double)x / scaleX, maxY - (double)y / scaleY));
 	}
 
 	protected int findSelectedPoint(int x, int y) {
 		if(points == null)
 			return -1;
 		int pos = 0;
 		for(EcgPoint point : points) {
 			Point2D.Double screenPoint = translateXYtoPoint(point.getX(), point.getY());
 			double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + 
 					(screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
 			if(distance < 100D)
 				return pos;
 			pos++;
 		}
 		return -1;
 	}
 
 	private class Border {
 		private double maxX;
 		private double minX;
 		private double maxY;
 		private double minY;
 		public Border(double maxX, double minX, double maxY, double minY) {
 			this.maxX = maxX;
 			this.minX = minX;
 			this.maxY = maxY;
 			this.minY = minY;
 		}
 		public double getMaxX() {
 			return maxX;
 		}
 		public double getMinX() {
 			return minX;
 		}
 		public double getMaxY() {
 			return maxY;
 		}
 		public double getMinY() {
 			return minY;
 		}
 	}
 
 }
