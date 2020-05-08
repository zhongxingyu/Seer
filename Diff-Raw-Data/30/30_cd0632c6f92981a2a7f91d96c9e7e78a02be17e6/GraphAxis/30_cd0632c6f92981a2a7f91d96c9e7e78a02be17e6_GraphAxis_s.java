 package org.bodytrack.client;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.DirectShapeRenderer;
 import gwt.g2d.client.graphics.Surface;
 import gwt.g2d.client.graphics.TextAlign;
 import gwt.g2d.client.graphics.TextBaseline;
 import gwt.g2d.client.math.Vector2;
 
 import com.google.gwt.i18n.client.NumberFormat;
 
 public class GraphAxis {
 	public static final String NO_CHANNEL_NAME = "";
 
 	public double majorTickMinSpacingPixels = 50;
 	public double majorTickWidthPixels = 8;
 
 	public double minorTickMinSpacingPixels = 10;
 	public double minorTickWidthPixels = 3;
 
 	protected double min;
 	protected double max;
 
 	protected boolean hasMinRange = false;
 	protected double minRange = -1e+100;
 
 	protected boolean hasMaxRange = false;
 	protected double maxRange = 1e+100;
 
 	protected Basis basis;
 	private Vector2 begin;
 	private double width;
 	protected double length;
 	private double scale;
 	private BBox bounds;
 
 	private final boolean isXAxis;
 	private final String channelName;
 
 	// For determining whether to highlight this GraphAxis
 	private boolean highlighted;
 	protected static final Color NORMAL_COLOR = Canvas.DARK_GRAY;
 	protected static final Color HIGHLIGHTED_COLOR = Canvas.BLACK;
 
 	final static int JUSTIFY_MIN = 0;
 	final static int JUSTIFY_MED = 1;
 	final static int JUSTIFY_MAX = 2;
 
 	// Same as the other constructor, but attempts to guess whether this
 	// is an X-axis by examining basis.
 	public GraphAxis(String channelName, double min, double max,
 			Basis basis, double width) {
 		this(channelName, min, max, basis, width,
 			Basis.xDownYRight.equals(basis));
 	}
 
 	public GraphAxis(String channelName, double min, double max,
 			Basis basis, double width, boolean isXAxis) {
 		this.channelName = channelName;
 		this.min = min;
 		this.max = max;
 		this.basis = basis;
 		this.width = width;
 		this.isXAxis = isXAxis;
 
 		highlighted = false;
 	}
 
 	public void layout(Vector2 begin, double length) {
 		this.begin = begin;
 		this.length = length;
 		rescale();
 	}
 
 	private void rescale() {
 		this.scale = length / (this.max - this.min);
 		Vector2 vWidth = new Vector2(basis.x.scale(this.width));
 		Vector2 vLength = new Vector2(basis.y.scale(this.length));
 		Vector2 end = this.begin.add(vWidth).add(vLength);
 		bounds = new BBox(new Vector2(Math.min(begin.getX(), end.getX()),
 				Math.min(begin.getY(), end.getY())),
 				new Vector2(Math.max(begin.getX(), end.getX()),
 						Math.max(begin.getY(), end.getY())));
 	}
 
 	private double project1D(double value) {
 		return (value - this.min) * scale;
 	}
 
 	/**
 	 * Returns a Vector2 with the correct X- or Y-coordinate (X-coordinate
 	 * if this is an X-axis, Y-coordinate if this is a Y-axis) for
 	 * drawing a point.
 	 *
 	 * @param value
 	 * 		the value the user sees on the axis
 	 * @return
 	 * 		a {@link gwt.g2d.client.math.Vector2 Vector2} with one
 	 * 		coordinate correct for drawing the specified value relative
 	 * 		to this axis
 	 */
 	public Vector2 project2D(double value) {
 		return begin.add(basis.y.scale(project1D(value)));
 	}
 
 	public double unproject(Vector2 point) {
 		return this.min + (point.subtract(begin).dot(basis.y) / scale);
 	}
 
 	/**
 	 * Marks this GraphAxis as highlighted.
 	 */
 	public void highlight() {
 		highlighted = true;
 	}
 
 	/**
 	 * Marks this GraphAxis as not highlighted.
 	 */
 	public void unhighlight() {
 		highlighted = false;
 	}
 
 	/**
 	 * Returns <tt>true</tt> if and only if this GraphAxis is marked as
 	 * highlighted.
 	 *
 	 * @return
 	 * 		<tt>true</tt> if and only if {@link #highlight()} has been
 	 * 		called since this GraphAxis was constructed, and
 	 * 		{@link #unhighlight()} has not been called since the last
 	 * 		call to highlight
 	 */
 	public boolean isHighlighted() {
 		return highlighted;
 	}
 
 	public void paint(Surface surface) {
 		Canvas canvas = Canvas.buildCanvas(surface);
 
 		// Pick the color to use, based on highlighting status
 		if (isHighlighted())
 			canvas.getSurface().setStrokeStyle(HIGHLIGHTED_COLOR);
 		else
 			canvas.getSurface().setStrokeStyle(NORMAL_COLOR);
 
 		canvas.getRenderer().beginPath();
 		canvas.getRenderer().drawLineSegment(
 				project2D(this.min), project2D(this.max));
 
 		double majorTickSize = computeTickSize(majorTickMinSpacingPixels);
 		renderTicks(0, majorTickSize, null, canvas,
 				majorTickWidthPixels, new DefaultLabelFormatter());
 		//renderTickLabels(surface, majorTickSize, majorTickWidthPixels+3);
 
 		double minorTickSize = computeTickSize(minorTickMinSpacingPixels);
 		renderTicks(0, minorTickSize, null, canvas,
 				minorTickWidthPixels, null);
 
 		canvas.getRenderer().stroke();
 
 		// Clean up after ourselves
 		canvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);
 	}
 
 	public double getMin() {
 		return min;
 	}
 
 	public double getMax() {
 		return max;
 	}
 
 	static abstract class LabelFormatter {
 		abstract String format(double value);
 	}
 
 	static class DefaultLabelFormatter extends LabelFormatter {
 		String format(double value) {
 			String label = NumberFormat.getDecimalFormat().format(value);
 
 			if (label.equals("-0"))
 				label="0";
 
 			return label;
 		}
 	}
 
 	static class TickGenerator {
 		private double tickSize;
 		private double offset;
 		private double currentTick = 0.0;
 
 		TickGenerator(double tickSize, double offset) {
 			this.tickSize = tickSize;
 			this.offset = offset;
 		}
 
 		double nextTick(double min) {
 			currentTick = closestTick(min - tickSize);
 
 			while (currentTick < min)
 				advanceTick();
 
 			return currentTick;
 		}
 
 		double nextTick() {
 			advanceTick();
 			return currentTick;
 		}
 
 		void advanceTick() {
 			currentTick = closestTick(currentTick + tickSize);
 		}
 
 		double closestTick(double val) {
 			return Math.round((val - offset) / tickSize) * tickSize
                 + offset;
 		}
 	}
 
 	/**
 	 * Wrapper class for TickGenerator that allows iteration.
 	 */
 	static class IterableTickGenerator implements Iterable<Double> {
 		private TickGenerator gen;
 		private double maxValue;
 		private double minValue;
 
 		/**
 		 * Builds a new IterableTickGenerator wrapping the specified
 		 * TickGenerator.
 		 *
 		 * @param gen
 		 * 		the TickGenerator this IterableTickGenerator should wrap
 		 * @param minValue
 		 * 		the minimum (starting) value of a tick
 		 * @param maxValue
 		 * 		the maximum value of a tick this should return in
 		 * 		iteration
 		 * @throws NullPointerException
 		 * 		if gen is <tt>null</tt>
 		 */
 		public IterableTickGenerator(TickGenerator gen, double minValue,
 				double maxValue) {
 			if (gen == null)
 				throw new NullPointerException(
 					"TickGenerator cannot be null");
 
 			this.gen = gen;
 			this.minValue = minValue;
 			this.maxValue = maxValue;
 		}
 
 		@Override
 		public Iterator<Double> iterator() {
 			return new TickGeneratorIterator();
 		}
 
 		private class TickGeneratorIterator implements Iterator<Double> {
 			private double currTick;
 			private double nextTick;
 
 			/**
 			 * Primes the ticks to return.
 			 */
 			public TickGeneratorIterator() {
 				currTick = 0.0;
 				nextTick = gen.nextTick(minValue);
 			}
 
 			/**
 			 * Returns <tt>true</tt> iff the next tick returned from
 			 * {@link #next()} would be less than or equal to maxValue
 			 * (which was passed into the IterableTickGenerator constructor).
 			 *
 			 * @return
 			 * 		<tt>true</tt> iff the next tick would be
 			 * 		less than or equal to maxValue (which was passed
 			 * 		into the IterableTickGenerator constructor)
 			 */
 			@Override
 			public boolean hasNext() {
 				return nextTick <= maxValue;
 			}
 
 			/**
 			 * Returns the next tick.
 			 *
 			 * @throws NoSuchElementException
 			 * 		if there are no more ticks available (users of this class
 			 * 		can test for this by calling
 			 * 		{@link TickGeneratorIterator#hasNext() hasNext()}
 			 * @return
 			 * 		a Double obtained by calling nextTick() on the
 			 * 		TickGenerator specified in the constructor to this
 			 * 		IterableTickGenerator
 			 */
 			@Override
 			public Double next() {
 				if (! hasNext())
 					throw new NoSuchElementException("No more ticks to show");
 
 				currTick = nextTick;
 
 				nextTick = gen.nextTick();
 
 				return currTick;
 			}
 
 			/**
 			 * Always throws a
 			 * {@linkplain java.lang.UnsupportedOperationException}
 			 *
 			 * @throws UnsupportedOperationException
 			 * 		always
 			 */
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException(
 					"Cannot remove from this iterator");
 			}
 		}
 	}
 
 	protected double setupText(Surface surface, int justify) {
 		boolean textParallelToAxis =
 			(Math.abs(basis.x.getX()) < Math.abs(basis.x.getY()));
 		double labelOffsetPixels = 0;
 
 		if (textParallelToAxis) {
 			final TextAlign[] align =
 			{TextAlign.LEFT, TextAlign.CENTER, TextAlign.RIGHT};
 
 			surface.setTextAlign(align[justify]);
 			surface.setTextBaseline(TextBaseline.TOP);
 		} else {
 			surface.setTextAlign(TextAlign.LEFT);
 
 			final TextBaseline[] baseline =
 			{TextBaseline.BOTTOM, TextBaseline.MIDDLE, TextBaseline.TOP};
 
 			surface.setTextBaseline(baseline[justify]);
 			labelOffsetPixels = 3;
 		}
 
 		return labelOffsetPixels;
 	}
 
 	protected void renderTicks(double offsetPixels, double tickSize,
 			TickGenerator tickGen, Canvas canvas, double tickWidthPixels,
 			LabelFormatter formatter) {
 		if (tickGen == null) tickGen = new TickGenerator(tickSize, 0);
 
 		double labelOffsetPixels = formatter == null ? 0
 				: setupText(canvas.getSurface(), JUSTIFY_MED)
 				+ offsetPixels + tickWidthPixels;
 
 		IterableTickGenerator gen =
 			new IterableTickGenerator(tickGen, this.min, this.max);
 
 		for (double tick: gen) {
 			renderTick(canvas.getRenderer(), tick,
 					offsetPixels + tickWidthPixels);
 
 			if (formatter != null)
 				renderTickLabel(canvas.getSurface(), tick,
 						labelOffsetPixels, formatter);
 		}
 	}
 
 	protected void renderTicksRangeLabelInline(double offsetPixels,
 			double tickSize, TickGenerator tickGen, Canvas canvas,
 			double tickWidthPixels, LabelFormatter formatter) {
 		Surface surface = canvas.getSurface();
 		DirectShapeRenderer renderer = canvas.getRenderer();
 
 		if (tickGen == null)
 			tickGen = new TickGenerator(tickSize, 0);
 
 		double labelOffsetPixels = setupText(surface, JUSTIFY_MED)
 			+ offsetPixels;
 
 		double tick = tickGen.nextTick(this.min);
 
 		if (tick > this.max) {
 			// No ticks are visible
 			// Draw one inline label in the middle
 			renderTickLabel(surface, (this.min + this.max) / 2.0,
 					labelOffsetPixels, formatter);
 			return;
 		}
 
 		int pixelMargin = 100;  // TODO: fix this
 		if (project1D(tick) >= pixelMargin) {
 			// Draw label for before first tick, justified to the minimum
 			// of axis (left or bottom)
 			setupText(surface, JUSTIFY_MIN);
 			renderTickLabel(surface, this.min, labelOffsetPixels, formatter);
 			setupText(surface, JUSTIFY_MED);
 		}
 
 		while (true) {
 			renderTick(renderer, tick, tickWidthPixels+offsetPixels);
 			double nextTick = tickGen.nextTick();
 
 			if (nextTick > this.max) break;
 
 			renderTickLabel(surface, (tick + nextTick) / 2.0,
 					labelOffsetPixels, formatter);
 			tick = nextTick;
 		}
 
 		if (length - project1D(tick) >= pixelMargin) {
 			// Draw label for after last tick, justified to maximum of
 			// axis (right or top)
 
 			setupText(surface, JUSTIFY_MAX);
 			renderTickLabel(surface, this.max, labelOffsetPixels, formatter);
 		}
 	}
 
 	protected void renderTicksRangeLabel(double offsetPixels, double tickSize,
 			TickGenerator tickGen, TickGenerator endOfRangeGenerator,
 			Canvas canvas, double tickWidthPixels, LabelFormatter formatter) {
 		Surface surface = canvas.getSurface();
 		DirectShapeRenderer renderer = canvas.getRenderer();
 
 		if (tickGen == null)
 			tickGen = new TickGenerator(tickSize, 0);
 
 		double labelOffsetPixels = setupText(surface, JUSTIFY_MED)
 		+ offsetPixels + tickWidthPixels;
 
 		double minTick = tickGen.nextTick(this.min - tickSize * 1.5);
 
 		while (minTick <= this.max) {
 			if (this.min <= minTick)
 				renderTick(renderer, minTick, tickWidthPixels + offsetPixels);
 
 			endOfRangeGenerator.nextTick(minTick);
 			double maxTick = endOfRangeGenerator.nextTick();
 
 			if (this.min <= maxTick && maxTick <= this.max)
 				renderTick(renderer, maxTick, tickWidthPixels + offsetPixels);
 
 			if (this.min <= minTick && maxTick <= this.max)
 				renderTickLabel(surface, (minTick+maxTick)/2.0,
 						labelOffsetPixels, formatter);
 
 			minTick = tickGen.nextTick();
 		}
 	}
 
 	protected void renderTick(DirectShapeRenderer renderer, double tick,
 			double tickWidthPixels) {
 		renderer.drawLineSegment(project2D(tick),
 				project2D(tick).add(this.basis.x.scale(tickWidthPixels)));
 	}
 
 	protected void renderTickLabel(Surface surface, double tick,
 			double labelOffsetPixels, LabelFormatter formatter) {
 		renderTickLabel(surface, tick, labelOffsetPixels,
 				formatter.format(tick));
 	}
 
 	protected void renderTickLabel(Surface surface, double y,
 			double labelOffsetPixels, String label) {
 		// .setFont("italic 30px sans-serif")
 		surface.fillText(label,
 				project2D(y).add(this.basis.x.scale(labelOffsetPixels)));
 	}
 
 	public double computeTickSize(double minPixels) {
 		return computeTickSize(minPixels, 1.0);
 	}
 
 	public double computeTickSize(double minPixels, double unitSize) {
 		double minDelta = (this.max - this.min)
 		* (minPixels / this.length) / unitSize;
 		double minDeltaMantissa = minDelta
 		/ Math.pow(10, Math.floor(Math.log10(minDelta)));
 
 		// Round minDelta up to nearest (1,2,5)
 		double actualDeltaMantissa;
 		if (minDeltaMantissa > 5) {
 			actualDeltaMantissa = 10;
 		} else if (minDeltaMantissa > 2) {
 			actualDeltaMantissa = 5;
 		} else if (minDeltaMantissa > 1) {
 			actualDeltaMantissa = 2;
 		} else {
 			actualDeltaMantissa = 1;
 		}
 
 		return minDelta * (actualDeltaMantissa / minDeltaMantissa);
 	}
 
 	public double getWidth() {
 		return width;
 	}
 
 	public boolean contains(Vector2 pos) {
 		return bounds.contains(pos);
 	}
 
 	protected void uncheckedTranslate(double motion) {
 		this.min += motion;
 		this.max += motion;
 	}
 
 	protected void clampToRange() {
 		// First, try to translate to put in range
 		uncheckedTranslate(Math.max(0, minRange - this.min));
 		uncheckedTranslate(Math.min(0, maxRange - this.max));
 
 		// Second, truncate to range
 		if (hasMinRange) this.min = Math.max(this.min, minRange);
 		if (hasMaxRange) this.max = Math.min(this.max, maxRange);
 	}
 
 	public void zoom(double factor, double about) {
 		this.min = about + factor * (this.min - about);
 		this.max = about + factor * (this.max - about);
 		clampToRange();
 		rescale();
 		publishBounds();
 	}
 
 	public void drag(Vector2 from, Vector2 to) {
 		double motion = unproject(from) - unproject(to);
 		uncheckedTranslate(motion);
 		clampToRange();
 		rescale();
 		publishBounds();
 	}
 
 	/**
 	 * Uses an InfoPublisher to publish the min and max values to
 	 * the rest of the webpage.
 	 *
 	 * <p> Note that this method is intended to be used by
 	 * subclasses.</p>
 	 */
 	protected void publishBounds() {
 		InfoPublisher pub = InfoPublisher.getInstance();
 
 		if (isXAxis)
 			pub.publishXAxisBounds(min, max);
 		else
 			pub.publishYAxisBounds(channelName, min, max);
 	}
 }
