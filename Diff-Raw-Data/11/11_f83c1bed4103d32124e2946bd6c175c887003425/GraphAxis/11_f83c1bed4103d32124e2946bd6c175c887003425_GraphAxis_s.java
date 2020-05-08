 package org.bodytrack.client;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.DirectShapeRenderer;
 import gwt.g2d.client.graphics.KnownColor;
 import gwt.g2d.client.graphics.Surface;
 import gwt.g2d.client.graphics.TextAlign;
 import gwt.g2d.client.graphics.TextBaseline;
 import gwt.g2d.client.math.Vector2;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.dom.client.MouseWheelEvent;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class GraphAxis implements Resizable {
 
 	public interface EventListener {
 		void onAxisChange(final int eventId);
 	}
 
 	/**
 	 * A point that signals to the {@link #highlight(PlottablePoint)}
 	 * method that there should be no visible highlighted point, just
 	 * a darkening of the axis
 	 */
 	public static final PlottablePoint DARKEN_AXIS_ONLY =
 		new PlottablePoint(Long.MIN_VALUE, Long.MIN_VALUE);
 
 	protected static final Color NORMAL_COLOR = KnownColor.DARK_GRAY;
 	protected static final Color HIGHLIGHTED_COLOR = ColorUtils.BLACK;
 	protected static final Color HIGHLIGHTED_POINT_COLOR = ColorUtils.RED;
 	protected static final double HIGHLIGHTED_POINT_LINE_WIDTH = 3;
 	protected static final double HIGHLIGHTED_POINT_LINE_LENGTH = 15;
 
 	static final int JUSTIFY_MIN = 0;
 	static final int JUSTIFY_MED = 1;
 	static final int JUSTIFY_MAX = 2;
 
 	public double majorTickMinSpacingPixels = 30;
 	public double majorTickWidthPixels = 8;
 
 	public double minorTickMinSpacingPixels = 10;
 	public double minorTickWidthPixels = 3;
 
 	protected double min;
 	protected double max;
 
 	protected boolean hasMinRange = false;
 	protected double minRange = -1e+100;
 
 	protected boolean hasMaxRange = false;
 	protected double maxRange = 1e+100;
 
 	private final Canvas drawingCanvas;
 	protected Basis basis;
 	private Vector2 begin;
 	private double width;
 	protected double length;
 	private double scale;
 	private BBox bounds;
 
 	private final boolean isXAxis;
 
 	// For determining whether to highlight this GraphAxis
 	private PlottablePoint highlightedPoint; // null if this isn't highlighted
 
 	@SuppressWarnings("unused")
 	private final String placeholderElementId;
 
 	private final Set<EventListener> eventListeners = new HashSet<EventListener>();
 
 	private Vector2 mouseDragLastPos;
 
 	private int previousPaintEventId = 0;
 
 	public GraphAxis(final String placeholderElementId,
 			final double min,
 			final double max,
 			final Basis basis,
 			final double width,
 			final boolean isXAxis) {
 		this.placeholderElementId = placeholderElementId;
 		if (basis == null)
 			throw new NullPointerException("Null basis");
 		if (min >= max)
 			throw new IllegalArgumentException("Axis min must be less than max");
 
 		if (placeholderElementId != null) {
 			final RootPanel placeholderElement = RootPanel.get(placeholderElementId);
 			final int placeholderElementWidth =
 				placeholderElement.getElement().getClientWidth();
 			final int placeholderElementHeight =
 				placeholderElement.getElement().getClientHeight();
 			final Surface drawing = new Surface(placeholderElementWidth,
 					placeholderElementHeight);
 			placeholderElement.add(drawing);
 
 			drawing.addMouseWheelHandler(new BaseMouseWheelHandler() {
 				@Override
 				protected void handleMouseWheelEvent(final MouseWheelEvent event,
 						final double wheelDelta) {
 					final Vector2 pos = new Vector2(event.getX(), event.getY());
 					final double zoomFactor = Math.pow(MOUSE_WHEEL_ZOOM_RATE, wheelDelta);
 
 					zoom(zoomFactor, unproject(pos), SequenceNumber.getNextThrottled());
 				}
 			});
 
 			drawing.addMouseDownHandler(new MouseDownHandler() {
 				@Override
 				public void onMouseDown(final MouseDownEvent event) {
 					mouseDragLastPos = new Vector2(event.getX(), event.getY());
 				}
 			});
 
 			drawing.addMouseMoveHandler(new MouseMoveHandler() {
 				@Override
 				public void onMouseMove(final MouseMoveEvent event) {
 					// ignore mouse moves if the mouse button isn't being held down
 					if (mouseDragLastPos != null) {
 						final Vector2 pos = new Vector2(event.getX(), event.getY());
 						drag(mouseDragLastPos, pos, SequenceNumber.getNextThrottled());
 						mouseDragLastPos = pos;
 					}
 				}
 			});
 
 			drawing.addMouseUpHandler(new MouseUpHandler() {
 				@Override
 				public void onMouseUp(final MouseUpEvent event) {
 					mouseDragLastPos = null;
 
 					// Want a guaranteed update of the plots
 					paint(SequenceNumber.getNext());
 				}
 			});
 
 			drawing.addMouseOutHandler(new MouseOutHandler() {
 				@Override
 				public void onMouseOut(final MouseOutEvent event) {
 					mouseDragLastPos = null;
 
 					// Want a guaranteed update of the plots
 					paint(SequenceNumber.getNext());
 				}
 			});
 
 			drawingCanvas = Canvas.buildCanvas(drawing);
 		} else {
 			drawingCanvas = null;
 		}
 
 		this.min = min;
 		this.max = max;
 		this.basis = basis;
 		this.width = width;
 		this.isXAxis = isXAxis;
 
 		highlightedPoint = null;
 	}
 
 	/**
 	 * Returns the unwrapped GraphAxis from nativeAxis
 	 *
 	 * @param nativeAxis
 	 *  The native wrapped axis passed in from JavaScript code
 	 * @return
 	 *  The GraphAxis object wrapped by nativeAxis if possible, or
 	 *  <code>null</code> if not
 	 * @throws NullPointerException
 	 *  If nativeAxis is <code>null</code>
 	 */
 	public static GraphAxis getAxis(JavaScriptObject nativeAxis) {
 		if (nativeAxis == null) {
 			throw new NullPointerException("Cannot get axis from null");
 		}
 
 		Dynamic djso = nativeAxis.cast();
 		return djso.get("__backingAxis");
 	}
 
 	public final void addEventListener(final EventListener listener) {
 		if (listener != null) {
 			eventListeners.add(listener);
 		}
 	}
 
 	public final void removeEventListener(final EventListener listener) {
 		if (listener != null) {
 			eventListeners.remove(listener);
 		}
 	}
 
 	public final void addEventListener(final JavaScriptObject listener) {
 		if (listener != null) {
 			addEventListener(new JavaScriptAxisChangeListener(listener));
 		}
 	}
 
 	public final void removeEventListener(final JavaScriptObject listener) {
 		if (listener != null) {
 			removeEventListener(new JavaScriptAxisChangeListener(listener));
 		}
 	}
 
 	public void setSize(final int widthInPixels,
 			final int heightInPixels,
 			final int newPaintEventId) {
 		final Element nativeCanvasElement = drawingCanvas.getNativeCanvasElement();
 
 		if ((nativeCanvasElement.getClientWidth() != widthInPixels) ||
 				(nativeCanvasElement.getClientHeight() != heightInPixels)) {
 			final Surface surface = drawingCanvas.getSurface();
 			surface.setSize(widthInPixels, heightInPixels);
 			layout();
 			paint(newPaintEventId);
 			publishAxisChangeEvent(newPaintEventId);
 		}
 	}
 
 	public void layout() {
 		if (drawingCanvas == null) {
 			return;
 		}
 
 		final double axisLength;
 		final Vector2 beginVector;
 		if (isXAxis) {
 			axisLength = (double)drawingCanvas.getSurface().getWidth();
 			beginVector = Vector2.ZERO;
 		} else {
 			final double elementHeight = drawingCanvas.getSurface().getHeight();
 			axisLength = elementHeight;
 			beginVector = new Vector2(0, elementHeight);
 		}
 
 		layout(beginVector, axisLength);
 	}
 
 	private void layout(final Vector2 begin, final double length) {
 		this.begin = begin;
 		this.length = length;
 		rescale();
 	}
 
 	private void rescale() {
 		this.scale = length / (this.max - this.min);
 		final Vector2 vWidth = new Vector2(basis.x.scale(this.width));
 		final Vector2 vLength = new Vector2(basis.y.scale(this.length));
 		final Vector2 end = this.begin.add(vWidth).add(vLength);
 		final Vector2 boundsMin = new Vector2(Math.min(begin.getX(), end.getX()),
 				Math.min(begin.getY(), end.getY()));
 		final Vector2 boundsMax = new Vector2(Math.max(begin.getX(), end.getX()),
 				Math.max(begin.getY(), end.getY()));
 		bounds = new BBox(boundsMin, boundsMax);
 	}
 
 	private double project1D(final double value) {
 		return (value - this.min) * scale;
 	}
 
 	/**
 	 * Returns a Vector2 with the correct X- or Y-coordinate (X-coordinate
 	 * if this is an X-axis, Y-coordinate if this is a Y-axis) for
 	 * drawing a point.
 	 *
 	 * @param value
 	 * 	The value the user sees on the axis
 	 * @return
 	 * 	A {@link Vector2 Vector2} with one coordinate correct for drawing
 	 * 	the specified value relative to this axis
 	 */
 	public Vector2 project2D(double value) {
 		return begin.add(basis.y.scale(project1D(value)));
 	}
 
 	public double unproject(Vector2 point) {
 		return this.min + (point.subtract(begin).dot(basis.y) / scale);
 	}
 
 	/**
 	 * Marks this GraphAxis as highlighted.
 	 *
 	 * @param point
 	 * 	The point to highlight.  If this is <code>null</code>, the
 	 * 	result is the same as calling {@link #unhighlight()}.
 	 * 	Also, if the special value {@link #DARKEN_AXIS_ONLY} is passed in
 	 * 	as point, no line is drawn, but the axis is still darkened
 	 */
 	public void highlight(PlottablePoint point) {
 		highlightedPoint = point;
 	}
 
 	/**
 	 * Marks this GraphAxis as not highlighted.
 	 */
 	public void unhighlight() {
 		highlightedPoint = null;
 	}
 
 	/**
 	 * Returns <tt>true</tt> if and only if this GraphAxis is marked as
 	 * highlighted.
 	 *
 	 * @return
 	 * 	<code>true</code> if and only if {@link #highlight()} has been
 	 * 	called with a non-<code>null</code> parameter since this GraphAxis
 	 * 	was constructed, and {@link #unhighlight()} has not been called since
 	 * 	the last call to highlight
 	 */
 	public boolean isHighlighted() {
 		return highlightedPoint != null;
 	}
 
 	/**
 	 * Returns the most recently set highlighted point, as set in
 	 * the {@link #highlight(PlottablePoint) highlight} method.
 	 *
 	 * <p>This is intended for subclass use only.  However, if a
 	 * subclass overrides this, it should also override
 	 * {@link #highlight(PlottablePoint)} and {@link #isHighlighted()}.</p>
 	 *
 	 * @return
 	 * 	The highlighted point for this axis
 	 */
 	protected PlottablePoint getHighlightedPoint() {
 		return highlightedPoint;
 	}
 
 	public void paint(final int newPaintEventId) {
 		// guard against redundant paints
 		if (previousPaintEventId != newPaintEventId) {
 			previousPaintEventId = newPaintEventId;
 
 			if (drawingCanvas == null) {
 				return;
 			}
 
 			drawingCanvas.clear();
 
 			// Pick the color to use, based on highlighting status
 			if (isHighlighted()) {
 				drawingCanvas.setStrokeStyle(HIGHLIGHTED_COLOR);
 			} else {
 				drawingCanvas.setStrokeStyle(NORMAL_COLOR);
 			}
 
 			drawingCanvas.beginPath();
 			drawingCanvas.drawLineSegment(project2D(this.min), project2D(this.max));
 
 			final double majorTickSize = computeTickSize(majorTickMinSpacingPixels);
 			renderTicks(0, majorTickSize, null, drawingCanvas,
 					majorTickWidthPixels, new DefaultLabelFormatter());
 			//renderTickLabels(surface, majorTickSize, majorTickWidthPixels+3);
 
 			final double minorTickSize = computeTickSize(minorTickMinSpacingPixels);
 			renderTicks(0, minorTickSize, null, drawingCanvas,minorTickWidthPixels, null);
 
 			drawingCanvas.stroke();
 
 			renderHighlight(drawingCanvas, highlightedPoint);
 
 			// Clean up after ourselves
 			drawingCanvas.setStrokeStyle(Canvas.DEFAULT_COLOR);
 		}
 	}
 
 	/**
 	 * Draws a colored line for point, if that is on this axis.
 	 *
 	 * <p>Note that this does absolutely nothing unless this
 	 * axis is highlighted.</p>
 	 *
 	 * <p>This is designed to be overridden by subclasses.</p>
 	 *
 	 * @param canvas
 	 * 	The {@link org.bodytrack.client.Canvas Canvas} we can
 	 * 	use to perform all rendering operations
 	 * @param point
 	 * 	The point to render on the axis
 	 */
 	protected void renderHighlight(Canvas canvas,
 			PlottablePoint point) {
 		if (!isHighlighted()) {
 			return;
 		}
 
 		if (point.equals(DARKEN_AXIS_ONLY)) {
 			return;
 		}
 
 		DirectShapeRenderer renderer = canvas.getRenderer();
 
 		canvas.getSurface().save();
 		double oldLineWidth = canvas.getSurface().getLineWidth();
 		canvas.getSurface().setLineWidth(HIGHLIGHTED_POINT_LINE_WIDTH);
 		canvas.getSurface().setStrokeStyle(HIGHLIGHTED_POINT_COLOR);
 
 		renderer.beginPath();
 
 		if (isXAxis) {
 			double time = point.getDate();
 
 			if (time >= min && time <= max) {
 				// top of the line to draw
 				Vector2 top = project2D(time);
 				renderer.drawLineSegment(top,
 						new Vector2(top.getX(),
 								top.getY() + HIGHLIGHTED_POINT_LINE_LENGTH));
 			}
 		} else {
 			double value = point.getValue();
 			if (value >= min && value <= max) {
 				// left edge of the line to draw
 				Vector2 left = project2D(value);
 				double size = Math.min(HIGHLIGHTED_POINT_LINE_LENGTH,
 						getWidth());
 				renderer.drawLineSegment(left,
 						new Vector2(left.getX() + size, left.getY()));
 			}
 		}
 
 		renderer.stroke();
 
 		// Clean up after ourselves
 		canvas.getSurface().restore();
 		canvas.getSurface().setLineWidth(oldLineWidth);
 	}
 
 	public double getMin() {
 		return min;
 	}
 
 	public double getMax() {
 		return max;
 	}
 
 	public boolean isXAxis() {
 		return isXAxis;
 	}
 
 	static abstract class LabelFormatter {
 		abstract String format(double value);
 	}
 
 	static class DefaultLabelFormatter extends LabelFormatter {
 		String format(double value) {
 			String label = NumberFormat.getDecimalFormat().format(value);
 
 			if (label.equals("-0")) {
 				label = "0";
 			}
 
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
 
 			while (currentTick < min) {
 				advanceTick();
 			}
 
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
 		 * Builds a new IterableTickGenerator wrapping gen over (min, max)
 		 */
 		public IterableTickGenerator(TickGenerator gen, double min, double max) {
 			if (gen == null) {
 				throw new NullPointerException(
 						"TickGenerator cannot be null");
 			}
 
 			this.gen = gen;
 			this.minValue = min;
 			this.maxValue = max;
 		}
 
 		@Override
 		public Iterator<Double> iterator() {
 			return new TickGeneratorIterator();
 		}
 
 		private class TickGeneratorIterator implements Iterator<Double> {
 			private double currTick;
 			private double nextTick;
 
 			public TickGeneratorIterator() {
 				currTick = 0.0;
 				nextTick = gen.nextTick(minValue);
 			}
 
 			/**
 			 * Returns <tt>true</tt> iff the next tick returned from
 			 * {@link #next()} would be less than or equal to maxValue
 			 * (which was passed into the IterableTickGenerator constructor)
 			 */
 			@Override
 			public boolean hasNext() {
 				return nextTick <= maxValue;
 			}
 
 			/**
 			 * Returns the next tick
 			 */
 			@Override
 			public Double next() {
 				if (!hasNext()) {
 					throw new NoSuchElementException("No more ticks to show");
 				}
 
 				currTick = nextTick;
 				nextTick = gen.nextTick();
 				return currTick;
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
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
 
 	protected void renderTicks(double offsetPixels,
 			double tickSize,
 			TickGenerator tickGen,
 			Canvas canvas,
 			double tickWidthPixels,
 			LabelFormatter formatter) {
 		if (tickGen == null) {
 			tickGen = new TickGenerator(tickSize, 0);
 		}
 
 		double labelOffsetPixels = formatter == null ? 0
 				: setupText(canvas.getSurface(), JUSTIFY_MED)
 				+ offsetPixels + tickWidthPixels;
 
 		for (double tick : new IterableTickGenerator(tickGen, this.min, this.max)) {
 			renderTick(canvas.getRenderer(), tick,
 					offsetPixels + tickWidthPixels);
 
 			if (formatter != null) {
 				renderTickLabel(canvas.getSurface(), tick,
 						labelOffsetPixels, formatter);
 			}
 		}
 	}
 
 	protected void renderTicksRangeLabelInline(double offsetPixels,
 			double tickSize,
 			TickGenerator tickGen,
 			Canvas canvas,
 			double tickWidthPixels,
 			LabelFormatter formatter) {
 		Surface surface = canvas.getSurface();
 		DirectShapeRenderer renderer = canvas.getRenderer();
 
 		if (tickGen == null) {
 			tickGen = new TickGenerator(tickSize, 0);
 		}
 
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
 			renderTick(renderer, tick, tickWidthPixels + offsetPixels);
 			double nextTick = tickGen.nextTick();
 
 			if (nextTick > this.max) {
 				break;
 			}
 
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
 
 	protected void renderTicksRangeLabel(double offsetPixels,
 			double tickSize,
 			TickGenerator tickGen,
 			TickGenerator endOfRangeGenerator,
 			Canvas canvas,
 			double tickWidthPixels,
 			LabelFormatter formatter) {
 		Surface surface = canvas.getSurface();
 		DirectShapeRenderer renderer = canvas.getRenderer();
 
 		if (tickGen == null) {
 			tickGen = new TickGenerator(tickSize, 0);
 		}
 
 		double labelOffsetPixels = setupText(surface, JUSTIFY_MED)
 		+ offsetPixels + tickWidthPixels;
 
 		double minTick = tickGen.nextTick(this.min - tickSize * 1.5);
 
 		while (minTick <= this.max) {
 			if (this.min <= minTick) {
 				renderTick(renderer, minTick, tickWidthPixels + offsetPixels);
 			}
 
 			endOfRangeGenerator.nextTick(minTick);
 			double maxTick = endOfRangeGenerator.nextTick();
 
 			if (this.min <= maxTick && maxTick <= this.max) {
 				renderTick(renderer, maxTick, tickWidthPixels + offsetPixels);
 			}
 
 			if (this.min <= minTick && maxTick <= this.max) {
 				renderTickLabel(surface, (minTick + maxTick) / 2.0,
 						labelOffsetPixels, formatter);
 			}
 
 			minTick = tickGen.nextTick();
 		}
 	}
 
 	protected void renderTick(final DirectShapeRenderer renderer,
 			final double tick,
 			final double tickWidthPixels) {
 		final Vector2 fromPosition = project2D(tick);
 		final Vector2 toPosition = fromPosition.add(this.basis.x.scale(tickWidthPixels));
 		renderer.drawLineSegment(fromPosition, toPosition);
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
 
 	public Canvas getDrawingCanvas() {
 		return drawingCanvas;
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
 		if (hasMinRange) {
 			this.min = Math.max(this.min, minRange);
 		}
 		if (hasMaxRange) {
 			this.max = Math.min(this.max, maxRange);
 		}
 	}
 
 	public void zoom(final double factor, final double about, final int eventId) {
 		this.min = about + factor * (this.min - about);
 		this.max = about + factor * (this.max - about);
 		clampToRange();
 		rescale();
 
 		// notify event listeners that the axis has changed
 		publishAxisChangeEvent(eventId);
 	}
 
 	private void publishAxisChangeEvent(final int eventId) {
 		for (final EventListener listener : eventListeners) {
 			listener.onAxisChange(eventId);
 		}
 	}
 
 	public void drag(final Vector2 from, final Vector2 to, final int eventId) {
 		final double motion = unproject(from) - unproject(to);
 		uncheckedDrag(motion, eventId);
 	}
 
 	/**
 	 * Drags this axis by motion, which is expressed in terms of
 	 * the units for this axis.
 	 *
 	 * @param motion
 	 * 	The amount to move this axis in logical units (seconds
 	 * 	for an axis representing time, other values for another
 	 * 	axis), not in screen pixels
 	 */
 	private void uncheckedDrag(final double motion, final int eventId) {
 		uncheckedTranslate(motion);
 		clampToRange();
 		rescale();
 
 		// notify event listeners that the axis has changed
 		publishAxisChangeEvent(eventId);
 	}
 
 	/**
 	 * Replaces the bounds of this axis with the specified min and max.
 	 *
 	 * @param newMin
 	 * 	The new min value for this axis
 	 * @param newMax
 	 * 	The new max value for this axis
 	 * @throws IllegalArgumentException
 	 * 	If <code>newMax >= newMin</code>
 	 */
 	public void replaceBounds(final double newMin, final double newMax) {
 		if (newMin >= newMax)
 			throw new IllegalArgumentException("Must have min < max");
 
 		final double oldMin = getMin();
 		final double oldMax = getMax();
 
 		final int eventId = SequenceNumber.getNext();
 
 		// Zoom in place to the right factor
 		zoom((newMax - newMin) / (oldMax - oldMin), (oldMin + oldMax) / 2, eventId);
 
 		// Now translate
 		uncheckedDrag(newMin - getMin(), eventId);
 
 		paint(eventId);
 	}
 
 	// TODO: Removing an axis probably doesn't work
 	// TODO: Only fire events on user-initiated updates
 	public static class JavaScriptAxisChangeListener implements EventListener {
 		private final JavaScriptObject callback;
 		private int prevEventId;
 
 		public JavaScriptAxisChangeListener(JavaScriptObject callback) {
 			if (callback == null)
 				throw new NullPointerException("null callback is not allowed");
 
 			this.callback = callback;
 			prevEventId = -1;
 		}
 
 		@Override
		public native void onAxisChange(int eventId) /*-{
 			if (eventId != prevEventId) {
				callback(eventId);
 			}
 		}-*/;
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + callback.hashCode();
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (!(obj instanceof JavaScriptAxisChangeListener)) {
 				return false;
 			}
 			JavaScriptAxisChangeListener other = (JavaScriptAxisChangeListener) obj;
 			return callback.equals(other.callback);
 		}
 	}
 }
