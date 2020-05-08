 package org.bodytrack.client;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.DirectShapeRenderer;
 import gwt.g2d.client.graphics.Surface;
 import gwt.g2d.client.graphics.TextAlign;
 import gwt.g2d.client.graphics.TextBaseline;
 import gwt.g2d.client.graphics.canvas.Context;
 import gwt.g2d.client.math.Vector2;
 
 import org.bodytrack.client.InstanceController.InstanceProducer;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 
 /**
  * Wrapper for a G2D Surface/DirectShapeRenderer pair.
  *
  * <p>This is intended to collect many of the important methods of the
  * {@link gwt.g2d.client.graphics.Surface Surface} and its associated
  * {@link gwt.g2d.client.graphics.DirectShapeRenderer DirectShapeRenderer}
  * in one class.  Note that not all methods of the two classes are
  * represented here.  However, the important methods of the two classes are
  * wrapped here, and calls to {@link #getSurface()} and to
  * {@link #getRenderer()}, which simply return references to those two
  * objects, allow calls to the other methods.</p>
  *
  * <p>This class is instance-controlled for efficiency: under this
  * system, only one DirectShapeRenderer is created per Surface.</p>
  */
 public final class Canvas {
 
 	/**
 	 * The default color, which classes should set as the stroke color
 	 * if wishing to &quot;clean up after themselves&quot; when done
 	 * changing colors and drawing.
 	 */
 	public static final Color DEFAULT_COLOR = ColorUtils.BLACK;
 
 	/**
 	 * The default alpha value, which classes should <em>always</em>
 	 * set as the alpha after changing alpha on a Canvas.
 	 */
 	public static final double DEFAULT_ALPHA = 1.0;
 
 	private final Surface surface;
 	private final DirectShapeRenderer renderer;
 	private final Element nativeCanvasElement;
 
 	private static InstanceController<Surface, Canvas> instances;
 
 	static {
 		instances = new InstanceController<Surface, Canvas>(
 			new InstanceProducer<Surface, Canvas>() {
 				@Override
 				public Canvas newInstance(Surface param) {
 					// The constructor will throw the NullPointerException
 					// if param is null
 					return new Canvas(param);
 				}
 			});
 	}
 
 	/**
 	 * Creates a new <tt>Canvas</tt> that draws on the specified surface.
 	 *
 	 * @param s
 	 * 	The surface on which the new {@link Canvas} will draw
 	 * @throws NullPointerException
 	 * 	If s is <code>null</code>
 	 * @throws IllegalArgumentException
 	 * 	If s has no native HTML canvas element inside its DOM tree
 	 */
 	private Canvas(Surface s) {
 		if (s == null)
 			throw new NullPointerException("Can't draw on a null surface");
 
 		surface = s;
 		renderer = new DirectShapeRenderer(surface);
 		nativeCanvasElement = findCanvasElement(surface.getElement());
 		if (nativeCanvasElement == null)
 			throw new IllegalArgumentException(
 				"No native canvas element available");
 	}
 
 	/**
 	 * Finds the first canvas element in the DOM tree rooted at e.
 	 *
 	 * @param e
 	 * 	The root of the search tree
 	 * @return
 	 * 	The first canvas element found in the subtree rooted
 	 * 	at e, or <code>null</code> if there is no canvas element
 	 * 	in the subtree rooted at e
 	 */
 	private Element findCanvasElement(Element e) {
 		if (e == null)
 			return null;
 
 		// This is kind of a heuristic that allows us to ignore
 		// namespaces in the DOM
 		if (e.getTagName().toLowerCase().contains("canvas"))
 			return e;
 
 		int childCount = DOM.getChildCount(e);
 		for (int i = 0; i < childCount; i++) {
 			Element child = DOM.getChild(e, i);
 			if (child == null)
 				continue; // Should never happen
 			Element canvas = findCanvasElement(child);
 			if (canvas != null)
 				return canvas;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Factory method to create a new Canvas object.
 	 *
 	 * @param s
 	 * 	The {@link gwt.g2d.client.graphics.Surface Surface}
 	 * 	on which the new Canvas will draw
 	 * @return
 	 * 	A Canvas with a pointer to s and to an associated
 	 * 	{@link gwt.g2d.client.graphics.DirectShapeRenderer
 	 * 	DirectShapeRenderer}
 	 */
 	public static Canvas buildCanvas(Surface s) {
 		return instances.newInstance(s);
 	}
 
 	public Surface getSurface() {
 		return surface;
 	}
 
 	/**
 	 * Returns the DirectShapeRenderer derived from the Surface passed
 	 * in to this object's constructor.
 	 */
 	public DirectShapeRenderer getRenderer() {
 		return renderer;
 	}
 
 	/**
 	 * Returns the native HTML canvas element found in the DOM tree
 	 * rooted at the surface passed in to this object's constructor.
 	 *
 	 * @return
 	 * 	The native canvas element
 	 */
 	public Element getNativeCanvasElement() {
 		return nativeCanvasElement;
 	}
 
 	/**
 	 * Returns the Context returned by {@code getSurface().getContext()}
 	 *
 	 * @return
 	 * 	The context for this canvas
 	 */
 	public Context getContext() {
 		return surface.getContext();
 	}
 
 	// --------------------------------------------------------------
 	// Wrappers for Surface methods
 	// --------------------------------------------------------------
 
 	/**
 	 * Equivalent to <code>getSurface().getWidth()</code>
 	 *
 	 * @return
 	 * 	The width of this canvas
 	 */
 	public int getWidth() {
 		return surface.getWidth();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().getHeight()</code>
 	 *
 	 * @return
 	 * 	The height of this canvas
 	 */
 	public int getHeight() {
 		return surface.getHeight();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().setStrokeStyle(color)</code>
 	 *
 	 * @param color
 	 * 	The color that will be used to stroke future drawing on the surface
 	 * @return
 	 * 	The Surface used for the setStrokeStyle call
 	 */
 	public Surface setStrokeStyle(Color color) {
 		return surface.setStrokeStyle(color);
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().setFillStyle(color)</code>
 	 *
 	 * @param color
 	 * 	The color that will be used to fill future drawing on the surface
 	 * @return
 	 * 	The Surface used for the setFillStyle call
 	 */
 	public Surface setFillStyle(Color color) {
 		return surface.setFillStyle(color);
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().getLineWidth()</code>
 	 */
 	public double getLineWidth() {
 		return surface.getLineWidth();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().setLineWidth(width)</code>
 	 *
 	 * @param width
 	 * 	The new width for lines on the surface
 	 * @return
 	 * 	The Surface used for the setLineWidth call
 	 */
 	public Surface setLineWidth(double width) {
 		return surface.setLineWidth(width);
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().getGlobalAlpha()</code>
 	 */
 	public double getGlobalAlpha() {
 		return surface.getGlobalAlpha();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().setGlobalAlpha(alpha)</code>
 	 *
 	 * @param alpha
 	 * 	The new alpha for the canvas
 	 * @return
 	 * 	The Surface used for the setGlobalAlpha call
 	 */
 	public Surface setGlobalAlpha(double alpha) {
 		return surface.setGlobalAlpha(alpha);
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().getTextAlign()</code>
 	 *
 	 * @return
 	 * 	The text alignment for this canvas
 	 */
 	public TextAlign getTextAlign() {
 		return surface.getTextAlign();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().setTextAlign(textAlign)</code>
 	 *
 	 * @param textAlign
 	 * 	The new alignment to use for text on the canvas
 	 * @return
 	 * 	The Surface used for the setTextAlign call
 	 */
 	public Surface setTextAlign(TextAlign textAlign) {
 		return surface.setTextAlign(textAlign);
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().getTextBaseline()</code>
 	 */
 	public TextBaseline getTextBaseline() {
 		return surface.getTextBaseline();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().setTextBaseline(textBaseline)</code>
 	 *
 	 * @param textBaseline
 	 * 	The new baseline to use for text on the canvas
 	 * @return
 	 * 	The Surface used for the setTextBaseline call
 	 */
 	public Surface setTextBaseline(TextBaseline textBaseline) {
 		return surface.setTextBaseline(textBaseline);
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().clear()</code>
 	 */
 	public Surface clear() {
 		return surface.clear();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().save()</code>
 	 */
 	public Surface save() {
 		return surface.save();
 	}
 
 	/**
 	 * Equivalent to <code>getSurface().restore()</code>
 	 */
 	public Surface restore() {
 		return surface.restore();
 	}
 
 	/**
 	 * Equivalent to
 	 * <code>getSurface().strokeRectangle(x, y, width, height)</code>
 	 *
 	 * @param x
 	 * 	The X-coordinate of the top left of the rectangle
 	 * @param y
 	 * 	The Y-coordinate of the top left of the rectangle
 	 * @param width
 	 * 	The width of the rectangle
 	 * @param height
 	 * 	The height of the rectangle
 	 * @return
 	 * 	The Surface used for the strokeRectangle call
 	 */
 	public Surface strokeRectangle(double x,
 			double y, double width, double height) {
 		return surface.strokeRectangle(x, y, width, height);
 	}
 
 	/**
 	 * Is exactly equivalent to a call to
 	 * <code>getSurface().fillRectangle(x, y, width, height)</code>
 	 *
 	 * @param x
 	 * 	The X-coordinate of the top left of the rectangle
 	 * @param y
 	 * 	The Y-coordinate of the top left of the rectangle
 	 * @param width
 	 * 	The width of the rectangle
 	 * @param height
 	 * 	The height of the rectangle
 	 * @return
 	 * 	The Surface used for the fillRectangle call
 	 */
 	public Surface fillRectangle(double x,
 			double y, double width, double height) {
 		return surface.fillRectangle(x, y, width, height);
 	}
 
 	// --------------------------------------------------------------
 	// Wrappers for DirectShapeRenderer methods
 	// --------------------------------------------------------------
 
 	/**
 	 * Equivalent to <code>getRenderer().beginPath()</code>
 	 */
 	public DirectShapeRenderer beginPath() {
 		return renderer.beginPath();
 	}
 
 	/**
 	 * Equivalent to <code>getRenderer().stroke()</code>
 	 */
 	public DirectShapeRenderer stroke() {
 		return renderer.stroke();
 	}
 
 	/**
 	 * Equivalent to <code>getRenderer().fill()</code>
 	 */
 	public DirectShapeRenderer fill() {
 		return renderer.fill();
 	}
 
 	/**
 	 * Equivalent to <code>getRenderer().drawLineSegment(start, end)</code>
 	 */
 	public DirectShapeRenderer drawLineSegment(Vector2 start, Vector2 end) {
 		return renderer.drawLineSegment(start, end);
 	}
 }
