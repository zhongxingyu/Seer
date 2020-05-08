 package org.bodytrack.client;
 
 import gwt.g2d.client.math.Vector2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a single set of data, along with its associated axes.
  * 
  * Has the ability to draw itself and its axes on a
  * {@link org.bodytrack.client.Canvas Canvas} object, and to update
  * the positions of its dots based on the zoom level.  Also, if the
  * zoom level changes enough, the {@link #zoom(double, double) zoom()}
  * method will automatically fetch the data from the server via Ajax
  * and redraw the data whenever the data comes in from the server.
  */
 public class DataPlot {
 	private GraphWidget container;
 	private GraphAxis xAxis;
 	private GraphAxis yAxis;
 	private Canvas canvas;
 	
 	// Values related to getting new values from the server
 	private String baseUrl;
 	private GrapherTile currentData;
 	private List<GrapherTile> pendingData;
 	private boolean waitingOnTile;
 
 	// Determining whether or not we should retrieve more data from
 	// the server
 	private int currentLevel;
 	private int currentOffset;
 	
 	/**
 	 * Main constructor for the DataPlot object.
 	 * 
 	 * The parameter url is the trickiest to get right.  This parameter
 	 * should be the <strong>beginning</strong> (the text up to, but
 	 * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
 	 * URL to fetch) of the URL which will be used to get more data.
 	 * Note that this <strong>must</strong> be a trusted BodyTrack
 	 * URL.  As described in the documentation for
 	 * {@link org.bodytrack.client.GrapherTile#retrieveTile(String, List)
 	 *  GrapherTile.retriveTile()}, an untrusted connection could allow
 	 * unauthorized access to all of a user's data.
 	 * 
 	 * @param container
 	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
 	 * 		which this DataPlot will draw itself and its axes
 	 * @param xAxis
 	 * 		the X-axis along which this data set will be aligned when
 	 * 		drawn.  Usually this is a
 	 * 		{@link org.bodytrack.client.TimeGraphAxis TimeGraphAxis}
 	 * @param yAxis
 	 * 		the Y-axis along which this data set will be aligned when
 	 * 		drawn
 	 * @param url
 	 * 		the beginning of the URL for fetching this data with Ajax
 	 * 		calls
 	 * @throws NullPointerException
 	 * 		if container, xAxis, yAxis, or url is <tt>null</tt>
 	 */
 	public DataPlot(GraphWidget container, GraphAxis xAxis, GraphAxis yAxis,
 			String url) {
 		if (container == null || xAxis == null
 				|| yAxis == null || url == null)
 			throw new NullPointerException(
 				"Cannot have a null container, axis, or url");
 		
 		this.container = container;
 		this.xAxis = xAxis;
 		this.yAxis = yAxis;
 		
 		baseUrl = url;
 
 		canvas = Canvas.buildCanvas(this.container);
 		pendingData = new ArrayList<GrapherTile>();
 
 		// The data will be pulled in with the checkForFetch call
 		currentData = null;
 		waitingOnTile = false;
 
 		currentLevel = Integer.MIN_VALUE;
 		currentOffset = Integer.MIN_VALUE;
 
 		checkForFetch();
 	}
 
 	public void zoom(double factor, double about) {
 		xAxis.zoom(factor, about);
 		yAxis.zoom(factor, about);
 
 		checkForFetch();
 	}
 
 	public void drag(Vector2 from, Vector2 to) {
 		xAxis.drag(from, to);
 		yAxis.drag(from, to);
 		
 		checkForFetch();
 	}
 	
 	/**
 	 * Checks for and performs a fetch for data from the server if
 	 * necessary.
 	 */
 	private void checkForFetch() {
 		int correctLevel = computeCurrentLevel();
 		int correctOffset = computeOffset(correctLevel);
 
 		if (correctLevel != currentLevel
 				|| correctOffset != currentOffset)
 			fetchFromServer(correctLevel, correctOffset);
 
 		// This way we don't fetch the same data multiple times
 		currentLevel = correctLevel;
 		currentOffset = correctOffset;
 	}
 
 	private void fetchFromServer(int level, int offset) {
 		String url = baseUrl + level + "." + offset + ".json";
 
 		GrapherTile.retrieveTile(url, pendingData);
 
 		waitingOnTile = true;
 	}
 
 	/**
 	 * Paints this DataPlot on the stored GraphWidget.
 	 */
 	public void paint() {
 		// Draw the axes in all cases
 		// TODO: Possibly (for performance reasons) make sure that
 		// the same axes never get painted multiple times (one option
 		// is to paint axes only in GraphWidget, and not here)
 		xAxis.paint(canvas.getSurface());
 		yAxis.paint(canvas.getSurface());
 
 		// If we have received data from the server
 		if (pendingData.size() > 0) {
 			// Take most recently retrieved tile, and get rid of
 			// all extra data
 			currentData = pendingData.get(pendingData.size() - 1);
 			pendingData.clear();
 
 			currentLevel = currentData.getLevel();
 			currentOffset = currentData.getOffset();
 
 			waitingOnTile = false;
 		}
 
 		// If we still have nothing to draw
 		if (currentData == null) {
 			if (! waitingOnTile)
 				checkForFetch();
 
 			return;
 		}
 
 		paintAllDataPoints();
 
 		checkForFetch();
 	}
 
 	/**
 	 * Renders all the data points in currentData.
 	 *
 	 * @throws NullPointerException
 	 * 		if currentData is <tt>null</tt>.  Since this is a private
 	 * 		method, we should see no problems with this
 	 */
 	private void paintAllDataPoints() {
 		if (currentData == null)
 			throw new NullPointerException(
 				"currentData cannot be null");
 
 		double prevX = Double.MIN_VALUE;
 		double prevY = Double.MIN_VALUE;
 
 		List<PlottablePoint> dataPoints = currentData.getDataPoints();
 
 		if (dataPoints == null)
 			return;
 
 		canvas.getRenderer().beginPath();
 
 		for (PlottablePoint point: dataPoints) {
 			// Don't draw points too far to the left or right
 			if (point.getDate() < xAxis.getMin()
 					|| point.getDate() > xAxis.getMax()) {
 				// Make sure we don't draw lines between points
 				// that aren't adjacent
 				prevX = prevY = Double.MIN_VALUE;
 				continue;
 			}
 
 			// Don't draw points too high or low to be rendered
 			if (point.getValue() < yAxis.getMin()
 					|| point.getValue() > yAxis.getMax()) {
 				// Make sure we don't draw lines between points
 				// that aren't adjacent
 				prevX = prevY = Double.MIN_VALUE;
 				continue;
 			}
 
 			double x = xAxis.project2D(point.getDate()).getX();
 			double y = yAxis.project2D(point.getValue()).getY();
 
 			// Draw this part of the line, reaching to all points
 			// except the first (the first point has a line coming from
 			// it, but not to it)
 			if (prevX != Double.MIN_VALUE && prevY != Double.MIN_VALUE)
 				canvas.getRenderer().drawLineSegment(prevX, prevY, x, y);
 
 			prevX = x;
 			prevY = y;
 		}
 
 		canvas.getRenderer().stroke();
 	}
 
 	/**
 	 * Returns the X-Axis for this DataPlot.
 	 *
 	 * @return
 	 * 		the X-axis for this DataPlot
 	 */
 	public GraphAxis getXAxis() {
 		return xAxis;
 	}
 
 	/**
 	 * Returns the Y-Axis for this DataPlot.
 	 *
 	 * @return
 	 * 		the Y-axis for this DataPlot
 	 */
 	public GraphAxis getYAxis() {
 		return yAxis;
 	}
 
 	/**
 	 * Computes the value for currentLevel based on xAxis.
 	 *
 	 * @return
 	 * 		the level at which xAxis is operating
 	 */
 	private int computeCurrentLevel() {
 		double xAxisWidth = xAxis.getMax() - xAxis.getMin();
		int dataPointWidth = (int) (xAxisWidth / GrapherTile.TILE_WIDTH);
 
 		// Add 1 so that the whole area will be covered
 		return log2(dataPointWidth) + 1;
 	}
 
 	/**
 	 * Computes the floor of the log (base 2) of n.
 	 *
 	 * @param n
 	 * 		the value for which we want to take the log
 	 * @return
 	 * 		the floor of the log (base 2) of n
 	 */
	private int log2(int n) {
		return (int) (Math.log(n) / Math.log(2));
 	}
 
 	/**
 	 * Returns the offset at which this X-axis is operating.
 	 *
 	 * Returns the offset of the tile in which the middle value
 	 * of the X-axis is found.
 	 *
 	 * @param level
 	 * 		the level at which we assume we are operating when calculating
 	 * 		offsets
 	 * @return
 	 * 		the current offset of the X-axis, based on level
 	 * 		and the private variable xAxis
 	 */
 	private int computeOffset(int level) {
 		double middle = 0.5 * (xAxis.getMin() + xAxis.getMax());
 
 		int tileWidth = getTileWidth(level);
 
 		// Tile number computation
 		return (int) (middle / tileWidth);
 	}
 
 	/**
 	 * Returns the width of a single tile.
 	 *
 	 * @param level
 	 * 		the level of the tile for which we will find the width
 	 * @return
 	 * 		the width of a tile at the given level
 	 */
 	private int getTileWidth(int level) {
 		// A tile is (1 << currentLevel) seconds per data point,
 		// times up to GrapherTile.TILE_WIDTH data points
 		return (1 << level) * GrapherTile.TILE_WIDTH;
 	}
 }
