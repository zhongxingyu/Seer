 package graphpanel;
 
 import java.awt.Color;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import datatypes.Dimension;
 import datatypes.Point;
 import datatypes.TimeSerie;
 import datatypes.TimeSerieRange;
 
 public class GraphPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener{
 
 	private static final long serialVersionUID = 1L;
 	
 	private List<TimeSerieConfig> timeSeriesConfigs;
 	private PointConverter converter;
 	private java.awt.Dimension preferredSize = new java.awt.Dimension(0, 0);
 	
 	public int px = 0;
 	private int py = 0;
 	
 	
 	public GraphPanel() {
 		this.timeSeriesConfigs = new ArrayList<TimeSerieConfig>();
 		this.converter = new PointConverter();
 		this.addMouseWheelListener(this);
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		if (this.preferredSize.getWidth() == 0 && this.preferredSize.getHeight() == 0) {
 			Rectangle r = SwingUtilities.calculateInnerArea(this, new Rectangle());
 			this.preferredSize = new java.awt.Dimension((int)r.getWidth(), (int)r.getHeight());
 		}
 		long startTime = Calendar.getInstance().getTimeInMillis();
 		super.paintComponent(g);
 		
 		this.resetConverter();
 
 		Graphics2D g2d = (Graphics2D) g;
 		this.initGraphPanel(g2d);
 		this.drawAxes(g2d);
 		this.drawTimeSeries(g2d);
 		this.drawLabels(g2d);
 		long endTime = Calendar.getInstance().getTimeInMillis();
 		System.out.println("Casove Rady vykreslene za " + (endTime - startTime)
 				+ "ms.");
 	}
 
 	private void initGraphPanel(Graphics2D g2d) {
 		int width = this.getWidth();
 		int height = this.getHeight();
 
 		Color color1 = Color.WHITE;
 		Color color2 = new Color(135, 206, 250);
 		GradientPaint gp = new GradientPaint(0, 0, color1, width, height, color2);
 		g2d.setPaint(gp);
 		g2d.fillRect(0, 0, width, height);
 	}
 	
 	private void resetConverter() {
 		if (this.timeSeriesConfigs != null && !this.timeSeriesConfigs.isEmpty()) {
 			this.converter.setPanelSize(new Dimension(this.getWidth(), this.getHeight()));
 			this.converter.setTimeSerieSize(this.findBiggestTimeSerieAreaSize());
 			this.converter.setTimeSerieRange(this.findBiggestTimeSerieRange());
 			this.converter.recalculateProportions();
 		}
 	}
 	
 	private Dimension findBiggestTimeSerieAreaSize() {
 		if (this.timeSeriesConfigs != null) {
 			Dimension result = new Dimension(0, 0);
 			for (TimeSerieConfig tsc : this.timeSeriesConfigs) {
 				Dimension tscAreaSize = tsc.getTimeSerie().getAreaSize();
 				if (tscAreaSize.getWidth() > result.getWidth()) {
 					result.setWidth(tscAreaSize.getWidth());
 				}
 				if (tscAreaSize.getHeight() > result.getHeight()) {
 					result.setHeight(tscAreaSize.getHeight());
 				}
 				
 			}
 			return result;
 		}
 		return null;
 	}
 	
 	private TimeSerieRange findBiggestTimeSerieRange() {
 		if (this.timeSeriesConfigs != null) {
 			TimeSerieRange result = new TimeSerieRange();
 			for (TimeSerieConfig tsc : this.timeSeriesConfigs) {
 				TimeSerieRange tscRange = tsc.getTimeSerie().getRange();
 				if (tscRange.getMinX() < result.getMinX()) {
 					result.setMinX(tscRange.getMinX());
 				}
 				if (tscRange.getMinY() < result.getMinY()) {
 					result.setMinY(tscRange.getMinY());
 				}
 				if (tscRange.getMaxX() > result.getMaxX()) {
 					result.setMaxX(tscRange.getMaxX());
 				}
 				if (tscRange.getMaxY() > result.getMaxY()) {
 					result.setMaxY(tscRange.getMaxY());
 				}
 			}
 			return result;
 		}
 		return null;
 	}
 	
 	private void drawAxes(Graphics2D g2d) {
 		if (this.timeSeriesConfigs != null && !this.timeSeriesConfigs.isEmpty()) {
 			TimeSerieRange range = this.findBiggestTimeSerieRange();
 			if (range != null) {
 				Point axisYStart = this.converter.convertFromTimeSeriePoint(new Point(0.0, range.getMinY() - 1));
 				Point axisYEnd = this.converter.convertFromTimeSeriePoint(new Point(0, range.getMaxY() + 1));
 				Point axisXStart = this.converter.convertFromTimeSeriePoint(new Point(range.getMinX() - 1, 0));
 				Point axisXEnd = this.converter.convertFromTimeSeriePoint(new Point(range.getMaxX() + 1, 0));
 				
 				g2d.setColor(GraphPanelConfig.COLOR_AXIS);
 				g2d.drawLine((int) axisYStart.getX(), (int) axisYStart.getY(),
 						(int) axisYEnd.getX(), (int) axisYEnd.getY());
 				g2d.drawLine((int) axisXStart.getX(), (int) axisXStart.getY(),
 						(int) axisXEnd.getX(), (int) axisXEnd.getY());
 			}
 		}
 	}
 	
 	private void drawTimeSeries(Graphics2D g2d) {
 		if (this.timeSeriesConfigs != null) {
 			for (TimeSerieConfig tsc : this.timeSeriesConfigs) {
 				this.drawTimeSerie(g2d, tsc.getTimeSerie(), tsc.getColor());
 			}
 		}
 	}
 
 	private void drawTimeSerie(Graphics2D g2d, TimeSerie timeSerie, Color color) {
 		if (timeSerie != null) {
 			g2d.setColor(color);
 			Point previous = null;
 			for (int i = 0; i < timeSerie.size(); i++) {
 				if (i > 0) {
 					Point actual = this.converter.convertFromTimeSeriePoint(timeSerie.getPoint(i));
 					g2d.drawLine((int) previous.getX(), (int) previous.getY(),
 							(int) actual.getX(), (int) actual.getY());
 				}
 				previous = this.converter.convertFromTimeSeriePoint(timeSerie.getPoint(i));
 			}
 			
 			g2d.setColor(color);
 			for (int i = 0; i < timeSerie.size(); i++) {
 				Point p = timeSerie.getPoint(i);
 				Point convertedP = this.converter.convertFromTimeSeriePoint(p);
 				g2d.drawRect((int) (convertedP.getX() - 1), (int) (convertedP.getY() - 1), 2, 2);
 			}
 		}
 	}
 	
 	private void drawLabels(Graphics2D g2d) {
 		final int COUNT_OF_LABELS = 10;
 		if (this.timeSeriesConfigs != null && !this.timeSeriesConfigs.isEmpty()) {
 			g2d.setColor(GraphPanelConfig.COLOR_LABELS);
 			DecimalFormat df = new DecimalFormat("#.00");
 			
 			java.awt.Point startPoint = this.getLocation();
 			startPoint = new java.awt.Point(0-startPoint.x, 0-startPoint.y);
 			java.awt.Dimension viewSize = this.getParent().getSize();
 			
 			int startI = (int)startPoint.getX();
 			int endI = (int)(startPoint.getX() + viewSize.getWidth());
 			int iter = (int)viewSize.getWidth() / COUNT_OF_LABELS;
 
 			for (int i = startI; i < endI; i += iter) {
 				Point p = new Point(i, startPoint.getY());
 				p = this.converter.convertToTimeSeriePoint(p);
 				g2d.drawString(String.valueOf(df.format(p.getX())), i, (int)(startPoint.getY()+viewSize.getHeight())-10);
 			}
 			
 			startI = (int)startPoint.getY();
 			endI = (int)(startPoint.getY() + viewSize.getHeight());
 			iter = (int)viewSize.getHeight() / COUNT_OF_LABELS;
 			for (int i = startI; i < endI; i += iter) {
 				Point p = new Point(startPoint.getX(), i);
 				p = this.converter.convertToTimeSeriePoint(p);
 				g2d.drawString(String.valueOf(df.format(p.getY())), (int)startPoint.getX() + 10, i);
 			}
 		}
 	}
 
 	public List<TimeSerie> getTimeSeries() {
 		List<TimeSerie> result = new ArrayList<TimeSerie>();
 		for (TimeSerieConfig tsc : this.timeSeriesConfigs) {
 			result.add(tsc.getTimeSerie());
 		}
 		return result;
 	}
 	
 	public Map<String, TimeSerie> getTimeSeriesWithNames() {
 		Map<String, TimeSerie> result = new HashMap<String, TimeSerie>();
 		for (TimeSerieConfig tsc : this.timeSeriesConfigs) {
 			result.put(tsc.getName(), tsc.getTimeSerie());
 		}
 		return result;
 	}
 	
 	public void addTimeSerie(String name, TimeSerie timeSerie, Color color) {
 		this.timeSeriesConfigs.add(new TimeSerieConfig(name, timeSerie, color));
 		this.repaint();
 	}
 
 	public java.awt.Dimension getPreferredSize() {
 		return this.preferredSize;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		System.out.println(px + ", " + py);
 		java.awt.Point point = e.getPoint();
 		int diffX = point.x - this.px;
 		int diffY = point.y - this.py;
 		System.out.println("diff: " + diffX + ", " + diffY);
 		java.awt.Point oldLocation = getLocation();
 		java.awt.Point newLocation = new java.awt.Point(oldLocation.x+diffX, oldLocation.y+diffY);
 		if (this.canMoveGraph(newLocation)) {
 			setLocation(newLocation);
 			this.px = point.x;
 			this.py = point.y;
 		}
 	}
 	
 	private boolean canMoveGraph(java.awt.Point newLocation) {
 		Rectangle graphPosition = SwingUtilities.calculateInnerArea(this, new Rectangle());
 		java.awt.Point rightDownPoint = new java.awt.Point(graphPosition.x+graphPosition.width, graphPosition.y+graphPosition.height);
 		java.awt.Dimension viewScreen = this.getParent().getSize();
 		java.awt.Point lastAvailablePoint = new java.awt.Point((int)viewScreen.getWidth(), (int)viewScreen.getHeight());
 		
 		return (newLocation.x <= 0 && newLocation.y <= 0) && (lastAvailablePoint.x < rightDownPoint.x && lastAvailablePoint.y < rightDownPoint.y);
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		this.px = e.getPoint().x;
 		this.py = e.getPoint().y;
 		System.out.println(px + ", " + py);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		int mouseRotation = e.getWheelRotation();
		java.awt.Point point = e.getPoint();
		double d = (double) mouseRotation * 1.2;
		d = (mouseRotation > 0) ? 1 / d : -d;

		int w = (int) (getWidth() * d);
		int h = (int) (getHeight() * d);
		this.preferredSize.setSize(w, h);

		int offX = (int) (point.x * d) - point.x;
		int offY = (int) (point.y * d) - point.y;
		setLocation(getLocation().x - offX, getLocation().y - offY);

		getParent().doLayout();
 	}
 }
