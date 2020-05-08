 package nl.tue.fingerpaint.client.model;
 
 import nl.tue.fingerpaint.shared.model.MixingStep;
 import nl.tue.fingerpaint.shared.model.RectangleMixingStep;
 
 import com.google.gwt.canvas.dom.client.CanvasPixelArray;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.canvas.dom.client.ImageData;
 
 /**
  * Class that represents the rectangular geometry. Keeps the internal
  * representation in a vector. Also contains the canvas and methods to draw on
  * it.
  * 
  * @author Project Fingerpaint
  */
 public class RectangleGeometry extends Geometry {
 
 	/** Number of vertical cells. */
 	private int verticalCells = 240;
 	/** Number of horizontal cells. */
 	private int horizontalCells = 400;
 
 	/** {@code true} if the top wall is being moved, {@code false} otherwise */
 	protected boolean topWallStep;
 
 	// ----Constructors-----------------------------------------
 	/**
 	 * Uses constructor from super class (Geometry.java)
 	 * 
 	 * @param clientHeight
 	 *            The height of the window in which the application is displayed
 	 * @param clientWidth
 	 *            The width of the window in which the application is displayed
 	 * @param vertCells Number of vertical cells.
 	 * @param horCells Number of horizontal cells.
 	 */
 	public RectangleGeometry(int clientHeight, int clientWidth, int vertCells, int horCells) {
 		super(clientHeight, clientWidth);
 		
 		verticalCells = vertCells;
 		horizontalCells = horCells;
 		topWallStep = true;
 		
 		initialise(clientWidth, clientHeight);
 		initialiseDistribution();
 	}
 
 	// ----Getters and Setters-----------------------------------------
 	/**
 	 * Returns the base height of the geometry
 	 * 
 	 * @return The minimum height of this geometry
 	 */
 	@Override
 	public int getBaseHeight() {
 		return verticalCells;
 	}
 
 	/**
 	 * Returns the base width of the geometry
 	 * 
 	 * @return The minimum width of this geometry
 	 */
 	@Override
 	public int getBaseWidth() {
 		return horizontalCells;
 	}
 
 	@Override
 	public int getHeight() {
 		return canvasHeight;
 	}
 	
 	@Override
 	public int getWidth() {
 		return canvasWidth;
 	}
 	
 	/**
 	 * Get the current distribution from the canvas, and puts it in the
 	 * distribution.
 	 * 
 	 * @return The current concentration distribution
 	 * 
 	 */
 	@Override
 	public int[] getDistribution() {
 		int[] dist = new int[horizontalCells * verticalCells];
 		ImageData img = context.getImageData(X_OFFSET + 1, TOP_OFFSET + 1,
 				getWidth(), getHeight());
 		CanvasPixelArray data = img.getData();
 		int width = img.getWidth();
 		int height = img.getHeight();
 		int index;
 		for (int y = height; y >= 0; y -= (int) Math.floor(factor)) {
 			for (int x = 0; x < width; x += (int) Math.floor(factor)) {
 				// times 4 because we have R, G, B and alpha value per pixel
 				index = (y * width + x) * 4;
 				dist[(int) Math.floor(x / factor) + horizontalCells
 						* (verticalCells - 1 - (int) Math.floor(y / factor))] = data
 						.get(index);
 			}
 		}
 		return dist;
 	}
 
 	// ----Implemented abstract methods from superclass----------------
 	@Override
 	protected void updateSize(int clientWidth, int clientHeight) {
 		clientHeight -= TOP_OFFSET + BOTTOM_OFFSET;
 		clientWidth -= X_OFFSET * 2; // X_OFFSET is used on both sides
 		
 		if (clientHeight >= (clientWidth / (double) horizontalCells) * verticalCells) {
 			// width is limiting factor
 			canvasWidth = clientWidth - 2;
 			canvasHeight = (int) Math.floor((canvasWidth / (double) horizontalCells) * verticalCells);
 			factor = canvasWidth / (double) horizontalCells;
 		} else {
 			// height is limiting factor
 			canvasHeight = clientHeight - 2;
 			canvasWidth = (int) Math.floor((canvasHeight / (double) verticalCells) * horizontalCells);
 			factor = canvasHeight / (double) verticalCells;
 		}
 	}
 	
 	/**
 	 * Returns whether the position ({@code x}, {@code y}) is inside the drawing
 	 * area on the {@code canvas}. Note: The border around the drawing area
 	 * counts as 'outside'.
 	 * 
 	 * @return True if coordinates (x, y) are inside the rectangular shaped
 	 *         drawing area. False otherwise.
 	 */
 	@Override
 	public boolean isInsideDrawingArea(int x, int y) {
 		return x > 0 && y > 0 && x <= getWidth() && y <= getHeight();
 	}
 	
 	@Override
 	public boolean isInsideWall(int x, int y) {
 		return isInsideTopWall(x, y) || isInsideBottomWall(x, y);
 	}
 
 	/**
 	 * Returns whether the position ({@code x}, {@code y}) is inside the top
 	 * wall on the {@code canvas}.
 	 * 
 	 * @param x
 	 *            The x-coordinate relative to the top-left corner of the
 	 *            {@code canvas}
 	 * @param y
 	 *            The y-coordinate relative to the top-left corner of the
 	 *            {@code canvas}
 	 * 
 	 * @return {@code true} if coordinates (x, y) are inside the top wall.
 	 *         {@code false} otherwise.
 	 */
 	protected boolean isInsideTopWall(int x, int y) {
 		return (x > 0 && x <= getWidth() && y > -HEIGHT_OF_WALL && y <= 0);
 	}
 
 	/**
 	 * Returns whether the position ({@code x}, {@code y}) is inside the bottom
 	 * wall on the {@code canvas}.
 	 * 
 	 * @param x
 	 *            The x-coordinate relative to the top-left corner of the
 	 *            {@code canvas}
 	 * @param y
 	 *            The y-coordinate relative to the top-left corner of the
 	 *            {@code canvas}
 	 * 
 	 * @return {@code true} if coordinates (x, y) are inside the bottom wall.
 	 *         {@code false} otherwise.
 	 */
 	protected boolean isInsideBottomWall(int x, int y) {
 		return (x > 0 && x <= getWidth() && y > getHeight() && y <= getHeight()
 				+ HEIGHT_OF_WALL);
 	}
 
 	/**
 	 * Creates vector (array) of length 240 * 400. Initialises colour to all
 	 * white (1)
 	 * 
 	 * @post {@code representationVector} is initialised
 	 */
 	@Override
 	protected void initialiseDistribution() {
 		distribution = new int[horizontalCells * verticalCells];
 		for (int i = 0; i < distribution.length; i++) {
 			distribution[i] = 255;
 		}
 	}
 
 	/**
 	 * Draws the outline around the walls
 	 * 
 	 * @post The outline of the walls has been drawn on the {@code canvas}
 	 */
 	@Override
 	protected void drawWalls() {
 		// Set the lineWidth to 1 and colour to black
 		context.setLineWidth(1);
 		context.setStrokeStyle(CssColor.make("black"));
 
 		// Draw the outline of the top wall
 		context.beginPath();
 		context.moveTo(X_OFFSET + 0.5, TOP_OFFSET + 0.5 - HEIGHT_OF_WALL);
 		context.lineTo(X_OFFSET + getWidth() + 1.5, TOP_OFFSET + 0.5
 				- HEIGHT_OF_WALL);
 		context.lineTo(X_OFFSET + getWidth() + 1.5, TOP_OFFSET + 0.5);
 		context.lineTo(X_OFFSET + 0.5, TOP_OFFSET + 0.5);
 		context.closePath();
 		context.stroke();
 
 		// Draw the outline of the bottom wall
 		context.beginPath();
 		context.moveTo(X_OFFSET + 0.5, TOP_OFFSET + getHeight() + 1.5);
 		context.lineTo(X_OFFSET + getWidth() + 1.5, TOP_OFFSET + getHeight()
 				+ 1.5);
 		context.lineTo(X_OFFSET + getWidth() + 1.5, TOP_OFFSET + getHeight()
 				+ 1.5 + HEIGHT_OF_WALL);
 		context.lineTo(X_OFFSET + 0.5, TOP_OFFSET + getHeight() + 1.5
 				+ HEIGHT_OF_WALL);
 		context.closePath();
 		context.stroke();
 
 		// Fill the top and bottom walls
 		fillWall(0, true);
 		fillWall(0, false);
 	}
 	
 	@Override
 	protected void fillWall(int x) {
 		fillWall(x, topWallStep);
 	}
 
 	/**
 	 * Colours the inside of one of the walls of the geometry.
 	 * @param xOffset
 	 *            The x-distance to the initial position
 	 * @param topWall
 	 *            {@code true} if the top wall has to be filled, {@code false}
 	 *            otherwise
 	 */
 	protected void fillWall(int xOffset, boolean topWall) {
 		// Set the height of the upper border of the wall
 		double y = topWall ? TOP_OFFSET + 1 - HEIGHT_OF_WALL : TOP_OFFSET
 				+ getHeight() + 2;
 
 		// Clip the area inside the wall
 		context.beginPath();
 		context.moveTo(X_OFFSET + 1, y);
 		context.lineTo(X_OFFSET + getWidth() + 1, y);
 		context.lineTo(X_OFFSET + getWidth() + 1, y + HEIGHT_OF_WALL - 1);
 		context.lineTo(X_OFFSET + 1, y + HEIGHT_OF_WALL - 1);
 		context.closePath();
 		context.clip();
 
 		// Fill the area inside the wall
 		context.setFillStyle(wallColor.toHexString());
 		context.fillRect(X_OFFSET + 1, y, getWidth() + 1, HEIGHT_OF_WALL - 1);
 
 		// Set the stroke style for the arrows (stripes)
 		context.setStrokeStyle(wallStripeColor.toHexString());
 		context.setLineWidth(STRIPE_WIDTH);
 
 		// Set the initial x and y values for the arrows to the left
 		double x = X_OFFSET + 1 + getWidth() / 2.0 - STRIPE_INTERVAL / 2.0
 				+ xOffset;
 		y = topWall ? TOP_OFFSET + 0.5 - HEIGHT_OF_WALL / 2.0 : TOP_OFFSET
 				+ getHeight() + 1.5 + HEIGHT_OF_WALL / 2.0;
 
 		// Draw all the arrows to the left
 		while (x > X_OFFSET + 0.5) {
 			context.beginPath();
 			context.moveTo(x, y - HEIGHT_OF_WALL / 2.0);
 			context.lineTo(x - STRIPE_SLOPE, y);
 			context.lineTo(x, y + HEIGHT_OF_WALL / 2.0);
 			context.stroke();
 			x -= STRIPE_INTERVAL;
 		}
 
 		// Set the initial x value for the arrows to the right (y stays the
 		// same)
 		x = X_OFFSET + 1 + getWidth() / 2.0 + STRIPE_INTERVAL / 2.0 + xOffset;
 
 		// Draw all the arrows to the right
 		while (x < X_OFFSET + getWidth()) {
 			context.beginPath();
 			context.moveTo(x, y - HEIGHT_OF_WALL / 2.0);
 			context.lineTo(x + STRIPE_SLOPE, y);
 			context.lineTo(x, y + HEIGHT_OF_WALL / 2.0);
 			context.stroke();
 			x += STRIPE_INTERVAL;
 		}
 
 		// Restore clipping area to full canvas
 		removeClippingArea();
 	}
 
 	/**
 	 * Draws a rectangle on the canvas, starting at the upper left corner, with
 	 * the total height and width of the drawing area.
 	 * 
 	 * Note: The 0.5 and 1.5 coordinates are necessary to get solid black lines.
 	 * If you just use 0 and 1, the lines appear gray instead of black, and the
 	 * right and bottom lines are two pixels wide instead of 1.
 	 * 
 	 * @post The outline of the rectangle has been drawn on the {@code canvas}
 	 */
 	@Override
 	protected void drawGeometryOutline() {
 		context.setLineWidth(1);
 		context.setStrokeStyle(CssColor.make("black"));
 
 		context.beginPath();
 		context.moveTo(X_OFFSET + 0.5, TOP_OFFSET + 0.5);
 		context.lineTo(X_OFFSET + getWidth() + 1.5, TOP_OFFSET + 0.5);
 		context.lineTo(X_OFFSET + getWidth() + 1.5, TOP_OFFSET + getHeight()
 				+ 1.5);
 		context.lineTo(X_OFFSET + 0.5, TOP_OFFSET + getHeight() + 1.5);
 		context.closePath();
 		context.stroke();
 
 		context.setFillStyle(CssColor.make("white"));
 		context.fillRect(X_OFFSET + 1, TOP_OFFSET + 1, getWidth(), getHeight());
 	}
 
 	/**
 	 * Clips the drawing area, so that drawing outside of the drawing area is
 	 * not possible
 	 * 
 	 * @post The outline of the geometry has been clipped
 	 */
 	@Override
 	protected void clipGeometryOutline() {
 		context.beginPath();
 		context.moveTo(X_OFFSET + 1, TOP_OFFSET + 1);
 		context.lineTo(X_OFFSET + getWidth() + 1, TOP_OFFSET + 1);
 		context.lineTo(X_OFFSET + getWidth() + 1, TOP_OFFSET + getHeight() + 1);
 		context.lineTo(X_OFFSET + 1, TOP_OFFSET + getHeight() + 1);
 		context.closePath();
 		context.clip();
 	}
 	
 	@Override
 	protected void startDefineMixingStep(int mouseX, int mouseY) {
 		super.startDefineMixingStep(mouseX, mouseY);
 
 		topWallStep = isInsideTopWall(mouseX, mouseY);
 	}
 
 	/**
 	 * Checks whether a new {@code Step} should be added. If the MouseEvent's
 	 * coordinates are near the top of the rectangular geometry, a {@code TOP}
 	 * mixing step is generated; if it is near the bottom, a {@code BOTTOM} is
 	 * generated. The direction of movement decides whether the movement is to
 	 * the left or to the right.
 	 */
 	@Override
 	protected void stopDefineMixingStep(int mouseX, int mouseY) {
 		MixingStep movement = determineSwipe(mouseX, mouseY);
 
 		if (movement != null) {
 			for (StepAddedListener l : stepAddedListeners) {
 				l.onStepAdded(movement);
 			}
 		}
 	}
 
 	/**
 	 * Returns the direction and wall of the current swiping movement, returns
 	 * null if the swipe is not a valid swipe.
 	 * 
 	 * Additionally draws an arrow to indicate the direction of the current
 	 * swipe
 	 * 
 	 * @param mouseX
 	 *            The x-coordinate of the current mouse position.
 	 * @param mouseY
 	 *            The y-coordinate of the current mouse position.
 	 */
 	@Override
 	protected MixingStep determineSwipe(int mouseX, int mouseY) {
 		int diffX = mouseX - swipeStartX;
 		boolean topWall = topWallStep;
 		boolean toTheLeft = false;
 
 		if (diffX < -SWIPE_THRESHOLD) { // To the left
 			toTheLeft = true;
 		} else if (diffX > SWIPE_THRESHOLD) { // To the right
 			toTheLeft = false;
 		} else { // No movement of the geometry was specified
 			return null;
 		}
 
 		// converting the toTheLeft boolean to clockwise representation
 		boolean clockwise;
 		if (topWall) {
 			clockwise = !toTheLeft;
 		} else {
 			clockwise = toTheLeft;
 		}
 		RectangleMixingStep movement = new RectangleMixingStep(1, clockwise, topWall);
 		return movement;
 	}
 
 	/**
 	 * <p>Sets the given distribution as the current distribution, and draws it on
 	 * the canvas</p>
 	 * 
 	 * <p>Pre:
	 * {@code dist.length} == {@link #horizontalCells} * {@LINK #VERTICAL_CELLS}
 	 * </p>
 	 * 
 	 * @param dist
 	 * The distribution to be set and drawn
 	 */
 	@Override
 	public void drawDistribution(int[] dist) {
 		ImageData img = context.getImageData(X_OFFSET + 1, TOP_OFFSET + 1,
 				getWidth(), getHeight());
 		CanvasPixelArray data = img.getData();
 		int width = getWidth();
 		int l = dist.length;
 		int x, y, col, index, sw, sh, w2, h2;
 
 		for (int i = 0; i < l; i++) {
 			x = i % horizontalCells;
 			y = (verticalCells - 1) - i / horizontalCells;
 			col = dist[i];
 			sw = (int) Math.floor(x * factor);
 			sh = (int) Math.floor(y * factor);
 			w2 = (int) Math.floor((x + 1) * factor);
 			h2 = (int) Math.floor((y + 1) * factor);
 			for (int w = sw; w < w2; w++) {
 				for (int h = sh; h < h2; h++) {
 					index = (h * width + w) * 4;
 					data.set(index, col);
 					data.set(++index, col);
 					data.set(++index, col);
 				}
 			}
 		}
 		context.putImageData(img, X_OFFSET + 1, TOP_OFFSET + 1);
 	}
 
 	/**
 	 * Returns the string representation of the .svg image of the canvas
 	 * 
 	 * @return The string representation of the .svg image of the canvas
 	 */
 	@Override
 	public String getCanvasImage() {
 		int[] dist = getDistribution();
 		int width = getBaseWidth();
 		int height = getBaseHeight();
 		int d = (int) Math.floor(factor);
 		StringBuilder sb = new StringBuilder();
 		sb.append("<svg>");
 		
 		for (int i = 0; i < dist.length; i++) {
 			int x = i % width;
 			int y = height - i / width;
 			String col = intToHexString(dist[i]);		
 			sb.append("<rect fill=\""
 					+ col + "\" height=\""
 					+ d + "\" stroke=\"none\" width=\"" + d + "\" x=\""
 					+ (x * d) + "\" y=\"" + (y * d) + "\"/>");			
 		}
 		sb.append( "</svg>");
 		return sb.toString();
 	}
 
 	/**
 	 * Converts a 0-255 black and white value to a hexadecimal representation.
 	 * @param i Original colour.
 	 * @return Hexadecimal representation of {@code i}.
 	 */
 	private String intToHexString(int i) {
 		String hexValue = Integer.toHexString(i);
 	
 		hexValue = hexValue.length() == 1 ? "0"+hexValue : hexValue;
 		
 		return "#" + hexValue + hexValue + hexValue;
 	}
 
 	/**
 	 * Resets the current distribution to all white. Equivalent to calling
 	 * drawDistribution with a dist parameter containing '255' at all indices (but
 	 * faster)
 	 */
 	@Override
 	public void resetDistribution() {
 		context.setFillStyle(CssColor.make("white"));
 		context.fillRect(X_OFFSET + 1, TOP_OFFSET + 1, getWidth(), getHeight());
 	}
 }
