 package nl.tue.fingerpaint.client.model;
 
 import java.util.ArrayList;
 
 import nl.tue.fingerpaint.client.model.drawingtool.DrawingTool;
 import nl.tue.fingerpaint.client.model.drawingtool.SquareDrawingTool;
 import nl.tue.fingerpaint.shared.model.MixingStep;
 import nl.tue.fingerpaint.shared.utils.Colour;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.ImageData;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Abstract class representing a geometry
  * 
  * @author Project Fingerpaint
  */
 public abstract class Geometry {
 
 	/** Internal representation of the geometry */
 	protected int[] distribution;
 
 	/** The canvas to draw on */
 	protected Canvas canvas;
 	/** The context belonging to the canvas */
 	protected Context2d context;
 	/** The factor by which the base dimension of the geometry are multiplied */
 	protected double factor;
 
 	/**
 	 * The height of the wall in the same distance unit as the
 	 * {@code rectangleHeight}.
 	 */
 	protected final static int HEIGHT_OF_WALL = 25;
 	/**
 	 * Distance between left border of the canvas and the left border of the
 	 * drawing area.
 	 */
 	protected static final int X_OFFSET = 0;
 	/**
 	 * Distance between the top border of the canvas and the top border of the
 	 * drawing area.
 	 */
 	protected static final int TOP_OFFSET = HEIGHT_OF_WALL;
 	/**
 	 * Minimum distance between the bottom border of the canvas and the bottom
 	 * border of the drawing area.
 	 */
 	protected static final int BOTTOM_OFFSET = HEIGHT_OF_WALL;
 	/**
 	 * Threshold in pixels to decide when a large enough swipe has been carried
 	 * out to define a protocol step.
 	 */
 	protected final static int SWIPE_THRESHOLD = 20;
 
 	/** The current drawing tool */
 	protected DrawingTool tool;
 	/** The image to draw with */
 	protected ImageElement toolImage;
 	/** The extra width of the drawing tool. Equal to factor * tool.radius */
 	protected int displacement;
 
 	/** X-position of the previous MouseMoveEvent, relative to the canvas */
 	private int previousX;
 	/** Y-position of the previous MouseMoveEvent, relative to the canvas */
 	private int previousY;
 	/** {@code true} if the user is currently drawing, {@code false} otherwise */
 	private boolean drawing;
 	/** The current colour to draw with */
 	protected Colour currentColor;
 
 	/**
 	 * {@code true} if the user is currently defining a step, {@code false}
 	 * otherwise
 	 */
 	private boolean definingStep;
 	
 	/**
 	 * Stores the x-coordinate of the mouse event that initiates a wall
 	 * movement.
 	 */
 	protected int swipeStartX;
 	/**
 	 * Stores the y-coordinate of the mouse event that initiates a wall
 	 * movement.
 	 */
 	protected int swipeStartY;
 
 	/** The background colour of the walls */
 	protected static Colour wallColor = Colour.LIGHT_GRAY;
 	/** The colour of the arrows on the walls */
 	protected static Colour wallStripeColor = Colour.BLACK;
 	/** The width of the arrows on the walls */
 	protected static int STRIPE_WIDTH = 5;
 	/** The distance between two arrows */
 	protected static int STRIPE_INTERVAL = 50;
 	/** The total width of a single arrow */
 	protected static int STRIPE_SLOPE = 5;
 
 	/** The timer used for animating wall movement */
 	private Timer animationTimer;
 	/** The time between two 'frames' of the animation, in milliseconds */
 	private static int REFRESH_INTERVAL = 20;
 	/** The distance to move a wall in a single animation 'frame' */
 	private static int ANIMATION_DISTANCE = 3;
 	/** The amount of pixels after which the speed of the movement is increased */
 	private static int ACCELERATION_DISTANCE = 50;
 
 	// ----Constructor-----------------------------------------------
 	/**
 	 * Creates a new Geometry, initial colour is black.
 	 * 
 	 * @param clientHeight
 	 *            The height of the window in which the application is displayed
 	 * 
 	 * @param clientWidth
 	 *            The width of the window in which the application is displayed
 	 */
 	protected Geometry(int clientHeight, int clientWidth) {
 		initialiseDistribution();
 
 		drawing = false;
 		definingStep = false;
 
 		// Actually create the canvas element
 		createCanvas(clientHeight, clientWidth);
 		updateSize(clientWidth, clientHeight);
 		
 		// Initialise drawing colour to black
 		setColor(Colour.BLACK);
 		// Initialise drawing tool to a square with radius 3
 		setDrawingTool(new SquareDrawingTool(3));
 
 		// Draw the outline of the walls and the drawing canvas. Then clip the
 		// drawing area.
 		drawWalls();
 		drawGeometryOutline();
 		clipGeometryOutline();
 		
 		// Initialise mouse handlers
 		initialiseNativeHandlers(canvas.getElement());
 	}
 
 	/**
 	 * Initialise the mouse/touch handlers on the canvas, using native code.
 	 * 
 	 * @param canvas
 	 *            The element to initialise the handlers on.
 	 */
 	private native void initialiseNativeHandlers(Element canvas) /*-{
 		var hammer = $wnd.Hammer(canvas, {
 			drag_min_distance : 2,
 			drag_horizontal : true,
 			drag_vertical : true,
 			transform : true,
 			scale_treshold : 0.1,
 			hold : true,
 			hold_timeout : 400,
 			swipe : true,
 			swipe_time : 200, // ms
 			swipe_min_distance : 20, // pixels
 			prevent_default : true
 		}), that = this;
 
 		// Drag handlers
 		hammer.on('touch', function(e) {
 			var left = ($wnd.document.documentElement && $wnd.document.documentElement.scrollLeft) || 
               $wnd.document.body.scrollLeft,
               top = ($wnd.document.documentElement && $wnd.document.documentElement.scrollTop) || 
               $wnd.document.body.scrollTop;
 			that.@nl.tue.fingerpaint.client.model.Geometry::onDragStart(II)(
 					e.gesture.touches[0].pageX - e.target.offsetLeft - left,
 					e.gesture.touches[0].pageY - e.target.offsetTop - top);
 		});
 
 		hammer.on('drag', function(e) {
 			var left = ($wnd.document.documentElement && $wnd.document.documentElement.scrollLeft) || 
               $wnd.document.body.scrollLeft,
               top = ($wnd.document.documentElement && $wnd.document.documentElement.scrollTop) || 
               $wnd.document.body.scrollTop;
 			that.@nl.tue.fingerpaint.client.model.Geometry::onDragMove(II)(
 					e.gesture.touches[0].pageX - e.target.offsetLeft - left,
 					e.gesture.touches[0].pageY - e.target.offsetTop - top);
 		});
 
 		hammer.on('release', function(e) {
 			var left = ($wnd.document.documentElement && $wnd.document.documentElement.scrollLeft) || 
               $wnd.document.body.scrollLeft,
               top = ($wnd.document.documentElement && $wnd.document.documentElement.scrollTop) || 
               $wnd.document.body.scrollTop;
 			that.@nl.tue.fingerpaint.client.model.Geometry::onDragEnd(II)(
 					e.gesture.touches[0].pageX - e.target.offsetLeft - left,
 					e.gesture.touches[0].pageY - e.target.offsetTop - top);
 		});
 	}-*/;
 
 	/**
 	 * Updates the internal size and factor.
 	 * 
 	 * @param clientWidth The width of the canvas element.
 	 * @param clientHeight The height of the canvas element.
 	 */
 	protected abstract void updateSize(int clientWidth, int clientHeight);
 	
 	/**
 	 * Resizes the canvas to adjust to mobile-device orientation changes.
 	 * 
 	 * @param clientWidth The width of the canvas element.
 	 * @param clientHeight The height of the canvas element.
 	 * 
 	 */
 	public void resize(int clientWidth, int clientHeight){
 		int[] dist = getDistribution();		
 
 		drawing = false;
 		definingStep = false;
 		
 		canvas.setWidth(clientWidth + "px");
 		canvas.setCoordinateSpaceWidth(clientWidth);
 		canvas.setHeight(clientHeight + "px");
 		canvas.setCoordinateSpaceHeight(clientHeight);
 		// Get context of canvas, to use for painting, and save it in its
 		// original state
 		context = canvas.getContext2d();
 		context.save();
 		
 		updateSize(clientWidth, clientHeight);
 
 		// Draw the outline of the walls and the drawing canvas. Then clip the
 		// drawing area.
 		drawWalls();
 		drawGeometryOutline();
 		clipGeometryOutline();
 
 		drawDistribution(dist);		
 	}
 	
 	// ----Getters and Setters---------------------------------------
 	/**
 	 * Returns the canvas belonging to this Geometry
 	 * 
 	 * @return The canvas belonging to this Geometry
 	 */
 	public Canvas getCanvas() {
 		return this.canvas;
 	}
 
 	/**
 	 * Sets the size of the drawing tool
 	 * 
 	 * @param size
 	 *            The size to set
 	 */
 	public void setDrawingToolSize(int size) {
 		tool.setRadius(size);
 		setDrawingTool(tool);
 	}
 
 	/**
 	 * Returns the current drawing colour
 	 * 
 	 * @return The current colour which is used for drawing
 	 */
 	public Colour getColor() {
 		return this.currentColor;
 	}
 
 	/**
 	 * Sets the current drawing colour
 	 * 
 	 * @param colour
 	 *            The colour which becomes the current drawing colour
 	 * 
 	 * @post The current drawing colour is set to @param{color}
 	 */
 	public void setColor(Colour colour) {
 		this.currentColor = colour;
 		if (this.tool != null) {
 			setDrawingTool(this.tool);
 		}
 	}
 
 	/**
 	 * Returns the distribution
 	 * 
 	 * @return The distribution of this geometry
 	 */
 	abstract public int[] getDistribution();
 
 	/**
 	 * Sets the distribution to {@code dist}
 	 * 
 	 * @param dist
 	 *            The distribution to set
 	 */
 	public void setDistribution(int[] dist) {
 		this.distribution = dist;
 	}
 
 	/**
 	 * Returns the base height of the geometry
 	 * 
 	 * @return The minimum height of this geometry
 	 */
 	abstract public int getBaseHeight();
 
 	/**
 	 * Returns the base width of the geometry
 	 * 
 	 * @return The minimum width of this geometry
 	 */
 	abstract public int getBaseWidth();
 
 	/**
 	 * Returns the total height of the drawing area
 	 * 
 	 * @return total height of the drawing area
 	 */
 	public int getHeight() {
 		return (int) Math.ceil(factor * getBaseHeight());
 	}
 
 	/**
 	 * Returns the total width of the drawing area
 	 * 
 	 * @return total width of the drawing area
 	 */
 	public int getWidth() {
 		return (int) Math.ceil(factor * getBaseWidth());
 	}
 
 	/**
 	 * Sets the factor to multiply the outline of the geometry with.
 	 * 
 	 * @param factor
 	 *            The factor to set
 	 * 
 	 * @post The multiply factor has been set to @param{factor}
 	 */
 	public void setFactor(int factor) {
 		this.factor = factor;
 	}
 
 	/**
 	 * Sets the drawing tool
 	 * 
 	 * @param tool
 	 *            The tool to set as the drawing tool
 	 * 
 	 * @post {@code this.tool} has been set to {@code tool}
 	 */
 	public void setDrawingTool(DrawingTool tool) {
 		this.tool = tool;
 
 		int rad = tool.getRadius();
 		int size = (int) Math.floor((rad * 2 + 1) * factor);
 		this.displacement = (int) Math.floor(rad * factor);
 
 		ImageData data = this.tool.getTool(context.createImageData(size, size),
 				currentColor);
 
 		this.toolImage = getToolImage(size, data);
 	}
 
 	// ----Protected and private methods for initialisation and
 	// drawing------------
 
 	/**
 	 * Initialisation method which creates the canvas. If the browser doesn't
 	 * support HTML5 canvas, adds a message stating this to the RootPanel and
 	 * returns.
 	 * 
 	 * @param height
 	 *            The height of the canvas to be created
 	 * @param width
 	 *            The width of the canvas to be created
 	 */
 	protected void createCanvas(int height, int width) {
 		// Create the canvas
 		canvas = Canvas.createIfSupported();
 
 		// If canvas wan't created, add a label to the root panel stating this
 		// and return.
 		if (canvas == null) {
 			RootPanel
 					.get()
 					.add(new Label(
 							"Sorry, your browser doesn't support the HTML5 Canvas element"));
 			return;
 		}
 
 		// Initialise canvas
 		canvas.setStyleName("paintCanvas");
 		canvas.getElement().setId("fingerpaintCanvas");
 		canvas.setWidth(width + "px");
 		canvas.setCoordinateSpaceWidth(width);
 		canvas.setHeight(height + "px");
 		canvas.setCoordinateSpaceHeight(height);
 
 		// Get context of canvas, to use for painting, and save it in its
 		// original state
 		context = canvas.getContext2d();
 		context.save();
 	}
 
 	/**
 	 * Code to execute on a MouseDown event.
 	 * 
 	 * @param x
 	 *            The x-coordinate of the event, relative to the canvas
 	 * @param y
 	 *            The y-coordinate of the event, relative to the canvas
 	 */
 	public void onDragStart(int x, int y) {
 		x -= X_OFFSET;
 		y -= TOP_OFFSET;
 
 		if (isInsideDrawingArea(x, y)) {
 			// User started drawing the concentration distribution
 			drawing = true;
 
 			// Store starting coordinates
 			previousX = x;
 			previousY = y;
 
 			x = getValidCoord(x);
 			y = getValidCoord(y);
 
 			// Draw the first image of the drawing tool
			context.drawImage(toolImage, 0, 0,
					toolImage.getWidth(), toolImage.getHeight(),
					x - displacement + X_OFFSET, y - displacement + TOP_OFFSET,
					toolImage.getWidth(), toolImage.getHeight()
				);
 
 		} else if (isInsideWall(x, y)) {
 			// cancel current animation if there is one
 			if (animationTimer != null) {
 				animationTimer.cancel();
 				removeClippingArea();
 				fillWall(0);
 				clipGeometryOutline();
 			}
 
 			// User started defining a step of the protocol
 			definingStep = true;
 
 			startDefineMixingStep(x, y);
 		}
 	}
 
 	/**
 	 * Returns whether the point {@code (x, y)} is inside one of the walls.
 	 * 
 	 * @param x
 	 *            The x-coordinate of the event, relative to the canvas.
 	 * @param y
 	 *            The y-coordinate of the event, relative to the canvas.
 	 * @return whether the point {@code (x, y)} is inside one of the walls.
 	 */
 	public abstract boolean isInsideWall(int x, int y);
 
 	/**
 	 * Code to execute on a MouseUp event.
 	 * 
 	 * @param x
 	 *            The x-coordinate of the event, relative to the canvas
 	 * @param y
 	 *            The y-coordinate of the event, relative to the canvas
 	 */
 	public void onDragEnd(int x, int y) {
 		x -= X_OFFSET;
 		y -= TOP_OFFSET;
 
 		if (definingStep) {
 			stopDefineMixingStep(x, y);
 		}
 		removeMouseMoveHandler(x - swipeStartX);
 	}
 
 	/**
 	 * Code to execute on a MouseMove event.
 	 * 
 	 * @param x
 	 *            The x-coordinate of the event, relative to the canvas
 	 * @param y
 	 *            The y-coordinate of the event, relative to the canvas
 	 */
 	public void onDragMove(int x, int y) {
 		x -= X_OFFSET;
 		y -= TOP_OFFSET;
 
 		if (drawing && isInsideDrawingArea(x, y)) {
 			drawLine(previousX, previousY, x, y);
 			previousX = x;
 			previousY = y;
 		} else if (definingStep) {
 			removeClippingArea();
 			fillWall(x - swipeStartX);
 			clipGeometryOutline();
 		}
 	}
 
 	/**
 	 * Code to execute on a MouseOut event.
 	 */
 	public void onDragOut() {
 		removeClippingArea();
 		fillWall(0);
 		clipGeometryOutline();
 
 		removeMouseMoveHandler(0);
 	}
 
 	/**
 	 * Removes the mouseMoveHandler from the canvas, if it is present. If
 	 * defining a step has been ended, an animation is started to move the walls
 	 * back to their initial position.
 	 * 
 	 * @param xStop
 	 *            The x-coordinate, relative to the start coordinates of the
 	 *            mouse movement, at which the mouse movement was ended.
 	 */
 	private void removeMouseMoveHandler(final int xStop) {
 		if (drawing) {
 			drawing = false;
 		}
 		if (definingStep) {
 			definingStep = false;
 
 			animationTimer = new Timer() {
 				private int x = xStop;
 
 				@Override
 				public void run() {
 					int speed = Math.abs(x) / ACCELERATION_DISTANCE + 1;
 					if (x == 0) {
 						animationTimer.cancel();
 						return;
 					} else if (x < 0) {
 						x = Math.min(x + speed * ANIMATION_DISTANCE, 0);
 					} else if (x > 0) {
 						x = Math.max(x - speed * ANIMATION_DISTANCE, 0);
 					}
 					removeClippingArea();
 
 					fillWall(x);
 					clipGeometryOutline();
 				}
 			};
 			animationTimer.scheduleRepeating(REFRESH_INTERVAL);
 		}
 	}
 
 	/**
 	 * Colours the inside of one of the walls of the geometry.
 	 * 
 	 * @param x
 	 *            The x-distance to the initial position
 	 */
 	protected abstract void fillWall(int x);
 
 	/**
 	 * Initialises the distribution of the drawing area
 	 * 
 	 * @post {@code distribution} is initialised
 	 */
 	abstract protected void initialiseDistribution();
 
 	/**
 	 * Draws the outline around the walls
 	 * 
 	 * @post The outline of the walls has been drawn on the {@code canvas}
 	 */
 	abstract protected void drawWalls();
 
 	/**
 	 * Draws the border around the drawing area
 	 * 
 	 * @post The outline of the geometry has been drawn on the {@code canvas}
 	 */
 	abstract protected void drawGeometryOutline();
 
 	/**
 	 * Clips the drawing area, so that drawing outside of the drawing area is
 	 * not possible
 	 * 
 	 * @post The outline of the geometry has been clipped
 	 */
 	abstract protected void clipGeometryOutline();
 
 	/**
 	 * Clips the entire canvas
 	 * 
 	 * @post The entire canvas has been clipped
 	 */
 	protected void removeClippingArea() {
 		context.restore();
 		context.save();
 	}
 
 	/**
 	 * Returns whether the position ({@code x}, {@code y}) is inside the drawing
 	 * area on the {@code canvas}.
 	 * 
 	 * @param x
 	 *            The x-coordinate relative to the top-left corner of the
 	 *            {@code canvas}
 	 * @param y
 	 *            The y-coordinate relative to the top-left corner of the
 	 *            {@code canvas}
 	 * 
 	 * @return {@code true} if coordinates (x, y) are inside the drawing area.
 	 *         {@code false} otherwise.
 	 */
 	abstract protected boolean isInsideDrawingArea(int x, int y);
 
 	/**
 	 * Draws a line manually, filling a single pixel at a time. Can't use the
 	 * standard 'drawLine()' method for this purpose, since this makes it
 	 * impossible to update internal representation.
 	 * 
 	 * Algorithm obtained from the Internet. Source:
 	 * http://tech-algorithm.com/articles
 	 * /drawing-line-using-bresenham-algorithm/
 	 * 
 	 * @param x1
 	 *            x-coordinate, relative to the canvas element, of the start
 	 *            point of the line
 	 * @param y1
 	 *            y-coordinate, relative to the canvas element, of the start
 	 *            point of the line
 	 * @param x2
 	 *            x-coordinate, relative to the canvas element, of the end point
 	 *            of the line
 	 * @param y2
 	 *            y-coordinate, relative to the canvas element, of the end point
 	 *            of the line
 	 */
 	private void drawLine(int x1, int y1, int x2, int y2) {
 		int w = x2 - x1;
 		int h = y2 - y1;
 		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
 		if (w < 0)
 			dx1 = -1;
 		else if (w > 0)
 			dx1 = 1;
 		if (h < 0)
 			dy1 = -1;
 		else if (h > 0)
 			dy1 = 1;
 		if (w < 0)
 			dx2 = -1;
 		else if (w > 0)
 			dx2 = 1;
 		int longest = Math.abs(w);
 		int shortest = Math.abs(h);
 		if (!(longest > shortest)) {
 			longest = Math.abs(h);
 			shortest = Math.abs(w);
 			if (h < 0)
 				dy2 = -1;
 			else if (h > 0)
 				dy2 = 1;
 			dx2 = 0;
 		}
 		int numerator = longest >> 1;
 		for (int i = 0; i <= longest; i++) {
 			int x3 = getValidCoord(x1);
 			int y3 = getValidCoord(y1);
			context.drawImage(toolImage, 0, 0,
					toolImage.getWidth(), toolImage.getHeight(),
					x3 - displacement + X_OFFSET, y3 - displacement + TOP_OFFSET,
					toolImage.getWidth(), toolImage.getHeight()
				);
 			numerator += shortest;
 			if (!(numerator < longest)) {
 				numerator -= longest;
 				x1 += dx1;
 				y1 += dy1;
 			} else {
 				x1 += dx2;
 				y1 += dy2;
 			}
 		}
 	}
 
 	/**
 	 * Together with {@code stopDefineMixingStep()}, this function checks
 	 * whether the protocol should be updated by adding a new {@code Step}. This
 	 * particular function only stores the coordinates of the press event, to
 	 * calculate the distance travelled when the mouse or the user's finger is
 	 * lifted from the canvas.
 	 * 
 	 * @param mouseX
 	 *            x-coordinate of the mouseEvent.
 	 * @param mouseY
 	 *            y-coordinate of the mouseEvent.
 	 */
 	protected void startDefineMixingStep(int mouseX, int mouseY) {
 		swipeStartX = mouseX;
 		swipeStartY = mouseY;
 	}
 
 	/**
 	 * Together with {@code startDefineMixingStep()}, this function checks
 	 * whether the protocol should be updated by adding a new {@code Step}. This
 	 * particular function should be implemented by geometries to detail when
 	 * and how a new {@code Step} should be defined.
 	 * 
 	 * @param mouseX
 	 *            x-coordinate of the mouseEvent.
 	 * @param mouseY
 	 *            y-coordinate of the mouseEvent.
 	 */
 	protected abstract void stopDefineMixingStep(int mouseX, int mouseY);
 
 	/**
 	 * Should return the direction and wall of the current swiping movement.
 	 * should return null if the swipe is not a valid swipe.
 	 * 
 	 * This method should additionally draw a graphic to display that a swiping
 	 * motion is in progress
 	 * 
 	 * @param mouseX
 	 *            The x-coordinate of the current mouse position.
 	 * @param mouseY
 	 *            The y-coordinate of the current mouse position.
 	 * @return The direction and wall of the current swiping movement, or
 	 *         {@code null} if the swipe is not valid.
 	 */
 	protected abstract MixingStep determineSwipe(int mouseX, int mouseY);
 
 	/**
 	 * Creates and returns an image element with the image data belonging to the
 	 * drawing tool.
 	 * 
 	 * @param size
 	 *            The width and height of the dummy canvas, equal to the width
 	 *            (and height) of the drawing tool
 	 * @param img
 	 *            The ImageData belonging to the drawing tool
 	 * 
 	 * @return The created image element
 	 */
 	private ImageElement getToolImage(int size, ImageData img) {
 		Canvas dummycanvas = Canvas.createIfSupported();
 
 		// Initialise canvas
 		if (dummycanvas != null) {
 			dummycanvas.setWidth(size + "px");
 			dummycanvas.setCoordinateSpaceWidth(size);
 			dummycanvas.setHeight(size + "px");
 			dummycanvas.setCoordinateSpaceHeight(size);
 		}
 
 		Context2d dummyContext = dummycanvas.getContext2d();
 		dummyContext.putImageData(img, 0, 0);
 
 		return ImageElement.as(new Image(dummycanvas.toDataUrl()).getElement());
 	}
 
 	/**
 	 * Converts a value to a valid coordinate, meaning the a value representing
 	 * the top left pixel of a 'grid-pixel'
 	 * 
 	 * @param c
 	 *            The coordinate to convert
 	 * 
 	 * @return The valid coordinate belonging to input c
 	 */
 	protected int getValidCoord(int c) {
 		return (int) Math.floor(((c - 1) / factor) * factor + 1);
 	}
 
 	// --Public methods for general use---------------------------------
 	/**
 	 * Sets the given distribution as the current distribution, and draws it on
 	 * the canvas
 	 * 
 	 * @param dist
 	 *            The distribution to be set and drawn
 	 */
 
 	abstract public void drawDistribution(int[] dist);
 
 	/**
 	 * Returns the string representation of the .svg image of the canvas
 	 * 
 	 * @return The string representation of the .svg image of the canvas
 	 */
 	abstract public String getCanvasImage();
 
 	/**
 	 * Resets the current distribution to all white
 	 */
 	abstract public void resetDistribution();
 
 	/** ArrayList of stepAddedListeners connected to this geometry */
 	protected ArrayList<StepAddedListener> stepAddedListeners = new ArrayList<StepAddedListener>();
 
 	/** Interface for a listener for protocol steps */
 	public interface StepAddedListener {
 		/**
 		 * Code to execute when a mixing step is being added to the protocol
 		 * 
 		 * @param step
 		 *            The mixing step that is being added.
 		 */
 		public void onStepAdded(MixingStep step);
 	}
 
 	/**
 	 * Adds a stepAddedListener to the list of listeners.
 	 * 
 	 * @param l
 	 *            The listener to be added
 	 */
 	public void addStepAddedListener(StepAddedListener l) {
 		stepAddedListeners.add(l);
 	}
 
 	/**
 	 * Removes a stepAddedListner from the list of listeners.
 	 * 
 	 * @param l
 	 *            The listener to be removed
 	 */
 	public void removeStepAddedListener(StepAddedListener l) {
 		stepAddedListeners.remove(l);
 	}
 }
