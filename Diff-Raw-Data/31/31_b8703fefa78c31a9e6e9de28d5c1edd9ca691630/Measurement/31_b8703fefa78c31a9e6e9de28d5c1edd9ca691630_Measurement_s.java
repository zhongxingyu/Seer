 package nl.tudelft.otsim.Simulators;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 import javax.swing.event.EventListenerList;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.StandardChartTheme;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.DomainOrder;
 import org.jfree.data.general.DatasetChangeEvent;
 import org.jfree.data.general.DatasetChangeListener;
 import org.jfree.data.general.DatasetGroup;
 import org.jfree.data.xy.XYDataset;
 
 import nl.tudelft.otsim.Events.Scheduler;
 import nl.tudelft.otsim.Events.Step;
 import nl.tudelft.otsim.GUI.GraphicsPanel;
 import nl.tudelft.otsim.SpatialTools.Planar;
 
 /**
  * Runtime counterpart of MeasurementPlan.
  * 
  * @author Peter Knoppers
  */
 public class Measurement extends JFrame implements Step, SimulatedObject, XYDataset, ShutDownAble {
 	private static final long serialVersionUID = 1L;
 	private final Point2D.Double[] area;
 	private final Point2D.Double[] projectionPath;
 	private final Simulator simulator;
 	private final Scheduler scheduler;
 	private HashMap<SimulatedObject, Integer> indices = new HashMap<SimulatedObject, Integer>();
 	private ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();
 	private transient EventListenerList listenerList = new EventListenerList();
 	DatasetGroup datasetGroup = null;
 	
 	/**
 	 * Create a new Measurement.
 	 * @param name String; name of the new Measurement
 	 * @param area String; textual description of the outline of the measurement area
 	 * @param projectionPath String; textual description of the center of the measurement area
 	 * @param simulator {@link Simulator}; the Simulator that owns the moving {@link SimulatedObject SimulatedObjects}
 	 * @param scheduler {@link Scheduler}; the Scheduler of the simulation
 	 */
 	public Measurement(String name, String area, String projectionPath, Simulator simulator, Scheduler scheduler) {
 		this.area = Planar.coordinatesToPoints(area.replaceAll("[()m,]", "").split(" "));
 		this.projectionPath = Planar.coordinatesToPoints(projectionPath.replaceAll("[()m,]", "").split(" "));
 		this.simulator = simulator;
 		this.scheduler = scheduler;
 		scheduler.enqueueEvent(0, this);
 		setTitle(name);
 		setMinimumSize(new Dimension(1000, 800));
 		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
         getContentPane().setLayout(new java.awt.BorderLayout());
         ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
		JFreeChart chart = ChartFactory.createScatterPlot(name, "Time", "Distance", this, PlotOrientation.HORIZONTAL , false, false, false);
		ValueAxis x = chart.getXYPlot().getRangeAxis();
		x.setAutoRange(true);
		x.setLowerBound(0);
 		double length = 0;
 		Point2D.Double prevPoint = null;
 		for (Point2D.Double p : this.projectionPath) {
 			if (null != prevPoint)
 				length += prevPoint.distance(p);
 			prevPoint = p;
 		}	
		x.setUpperBound(length);
        ValueAxis y = chart.getXYPlot().getDomainAxis();
        y.setAutoRange(true);
        y.setLowerBound(0);
        y.setUpperBound(240);
         XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
         renderer.setBaseLinesVisible(true);
         renderer.setBaseShapesVisible(false);
 		ChartPanel cp = new ChartPanel(chart);
         cp.setFillZoomRectangle(true);
         cp.setMouseWheelEnabled(true);
         add(cp, java.awt.BorderLayout.CENTER);
 		setVisible(true);
 	}
 
 	@Override
 	public boolean step(double now) {
 		for (SimulatedObject vehicle : simulator.SampleMovables()) {
 			if (Planar.polygonContainsPoint(area, vehicle.center(now))) {
 				Integer index = indices.get(vehicle);
 				Trajectory trajectory;
 				if (null == index) {
 					trajectory = new Trajectory(now);
 					trajectories.add(trajectory);
 					indices.put(vehicle, trajectories.indexOf(trajectory));
 				} else
 					trajectory = trajectories.get(index);
 				double longitudinal = 0;
 				double bestLongitudinal = java.lang.Double.NaN;
 				double bestLateral = java.lang.Double.NaN;
 				Point2D.Double vehicleLocation = vehicle.center(now);
 				Point2D.Double prevPoint = null;
 				double bestDistance = java.lang.Double.MAX_VALUE;
 				for (Point2D.Double p : projectionPath) {
 					if (null != prevPoint) {
 						Line2D.Double lineSegment = new Line2D.Double(prevPoint, p);
 						double thisDistance = Planar.distanceLineSegmentToPoint(lineSegment, vehicleLocation);
 						if (thisDistance < bestDistance) {
 							Point2D.Double projection = Planar.nearestPointOnLine(lineSegment, vehicleLocation);
 							bestLongitudinal = longitudinal + prevPoint.distance(projection);
 							bestLateral = thisDistance;
 							// figure out if the vehicle is to the left or to the right of the projectionPath
 							double projectionPathDirection = Math.atan2(lineSegment.y2 - lineSegment.y1, lineSegment.x2 - lineSegment.x1);
 							double vehicleDirection = Math.atan2(vehicleLocation.y - prevPoint.y, vehicleLocation.x - prevPoint.x);
 							double difference = (vehicleDirection - projectionPathDirection + 2 * Math.PI) % (2 * Math.PI);
 							if (difference < Math.PI)
 								bestLateral = -bestLateral;
 							bestDistance = thisDistance;
 						}
 						longitudinal += prevPoint.distance(p);
 					}
 					prevPoint = p;
 				}
 				trajectory.extend(now, new Point2D.Double(bestLongitudinal, bestLateral));
 			}
 		}
 		notifyListeners(new DatasetChangeEvent(this, null));	// This guess work actually works!
 		scheduler.enqueueEvent(now + 1, this);
 		return true;
 	}
 
 	@Override
 	public void paint(double when, GraphicsPanel graphicsPanel) {
 		graphicsPanel.setColor(Color.BLUE);
 		graphicsPanel.setStroke(0);
 		graphicsPanel.drawPolyLine(area);
 	}
 
 	@Override
 	public Point2D.Double[] outline(double when) {
 		return area;
 	}
 
 	private class Trajectory {
 		final double startTime;
 		final double timeStep = 1.0;
 		private ArrayList<Point2D.Double> path = new ArrayList<Point2D.Double>();
 		
 		public Trajectory(double startTime) {
 			this.startTime = startTime;
 		}
 
 		public void extend(double when, Point2D.Double where) {
 			int index = (int) ((when - startTime) / timeStep);
 			if (index < path.size())
 				throw new Error("vehicle generated too many samples");
 			while (index > path.size())
 				path.add(null);
 			path.add(new Point2D.Double(where.x, where.y));
 		}
 
 		public int size() {
 			return path.size();
 		}
 
 		public Number getTime(int item) {
 			return startTime + item * timeStep;
 		}
 
 		public double getDistance(int item) {
 			Point2D.Double p = path.get(item);
 			if (null == p)
 				return Double.NaN;
 			return p.getX();
 		}
 	}
 
 	@Override
 	public Point2D.Double center(double when) {
 		return null;
 	}
 
 	@Override
 	public int getSeriesCount() {
 		return trajectories.size();
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Comparable getSeriesKey(int series) {
 		return series;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public int indexOf(Comparable seriesKey) {
 		if (seriesKey instanceof Integer)
 			return (Integer) seriesKey;
 		return -1;
 	}
 
 	@Override
 	public void addChangeListener(DatasetChangeListener listener) {
 		listenerList.add(DatasetChangeListener.class, listener);
 	}
 
 	@Override
 	public void removeChangeListener(DatasetChangeListener listener) {
 		listenerList.remove(DatasetChangeListener.class, listener);
 	}
 	
 	private void notifyListeners(DatasetChangeEvent event) {
 		for (DatasetChangeListener dcl : listenerList.getListeners(DatasetChangeListener.class))
 			dcl.datasetChanged(event);
 	}
 
 	@Override
 	public DatasetGroup getGroup() {
 		return datasetGroup;
 	}
 
 	@Override
 	public void setGroup(DatasetGroup group) {
 		datasetGroup = group;
 	}
 
 	@Override
 	public DomainOrder getDomainOrder() {
 		return DomainOrder.ASCENDING;
 	}
 
 	@Override
 	public int getItemCount(int series) {
 		if ((series < 0) || (series >= trajectories.size()))
 			return 0;
 		return trajectories.get(series).size();
 	}
 
 	@Override
 	public Number getX(int series, int item) {
 		if ((series < 0) || (series >= trajectories.size()))
 			return null;
		return trajectories.get(series).getTime(item);
 	}
 
 	@Override
 	public double getXValue(int series, int item) {
 		if ((series < 0) || (series >= trajectories.size()))
 			return Double.NaN;
		return (Double) trajectories.get(series).getTime(item);
 	}
 
 	@Override
 	public Number getY(int series, int item) {
 		if ((series < 0) || (series >= trajectories.size()))
 			return null;
		return trajectories.get(series).getDistance(item);
 	}
 
 	@Override
 	public double getYValue(int series, int item) {
 		if ((series < 0) || (series >= trajectories.size()))
 			return Double.NaN;
		return trajectories.get(series).getDistance(item);
 	}
 
 	@Override
 	public void ShutDown() {
 		dispose();
 	}
 }
 
