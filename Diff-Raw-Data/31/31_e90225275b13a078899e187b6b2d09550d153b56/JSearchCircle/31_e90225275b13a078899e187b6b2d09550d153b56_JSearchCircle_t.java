 package net.mrx13415.searchcircle.swing;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 
 import net.mrx13415.searchcircle.event.SearchCircleChangeEvent;
 import net.mrx13415.searchcircle.event.SearchCircleKeyEvent;
 import net.mrx13415.searchcircle.event.SearchCircleListener;
 import net.mrx13415.searchcircle.event.SearchCircleMouseEvent;
 import net.mrx13415.searchcircle.imageutil.ImageModifier;
 import net.mrx13415.searchcircle.imageutil.color.HSB;
 import net.mrx13415.searchcircle.math.Vector2d;
 
 
 /**
  * SearchCircle Daus/Bellmann (c) 2013
  * 
  * @author Oliver Daus / Jens Bellmann
 * @version 1.10.1
  * 
  *          Description: A round search and progress bar
  * 
  *          License:
  * 
  *             CreativeCommons Attribution-NonCommercial-ShareAlike 3.0 Unported
  *             (CC BY-NC-SA 3.0)
  * 
  *             For more Informations:
  *             http://creativecommons.org/licenses/by-nc-sa/3.0/
  *
 *			Version: 1.10.1
 * 			 - FIX: Added components are wrongly drawn
 * 
  * 			Version: 1.10.0
  * 			 - FIX: Changed internal package structure
  * 			 - FIX: performance improved 
  * 
  * 			Version: 1.9.9
  * 			 - FIX: ImageModifier works as intended
  * 
  * 			Version: 1.9.8
  *  		 - FIX: Child components are drawn correctly again
  *  
  * 			Version: 1.9.7
  * 			 - FIX: More performance improvements
  *  
  * 			Version: 1.9.6
  * 			 - FIX: About 50% less CPU usage
  *  
  *          Version: 1.9.5
  *           - FIX: Button is not fully drag able at the bar ends
  * 
  *          Version: 1.9.4
  *           - FIX: Button dragging sometimes drags other
  *                  SearchCircles too (only added components)
  *           - ADD: </code>considerInsetsOnMouseDispatch<code> Method 
  *          
  *          Version: 1.9.3
  *           - ADD: Better mouse interacting
  *           - ADD: Components within the SearchCircle (SearchCircel in SearchCircle)
  *           - FIX: Improved Listener
  *           - ADD: Key function
  * 
  *          Version: 1.9.2
  *           - ADD: Listener
  * 
  **/
 public class JSearchCircle extends JButton implements MouseListener,
 		MouseMotionListener, KeyListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	// Enums
 	public static final int BAR_DIRECTION_LEFT = -1;
 	public static final int BAR_DIRECTION_RIGHT = 1;
 
 	private static final double buttonPosAngleCorrectionTolerance = 0.1;
 	private static final double barPartSpaceAngleFixVaule = 200;
 
 	public boolean debug = false;
 
 	public static enum Anchor {
 		CENTER, LEFT, RIGHT;
 	}
 
 	// style
 	public static enum STYLE {
 		SMOOTH, PARTS;
 	}
 
 	// Mouse
 	private ArrayList<Component> passMouse = new ArrayList<Component>();
 	private boolean considerInsets = false;
 
 	// listener
 	private ArrayList<SearchCircleListener> SearchCircleListener = new ArrayList<SearchCircleListener>();
 
 	// parts
 	public double STYLE_PARTS_barPartSpaceAngleStyleValue = 3.5;
 	public int STYLE_PARTS_barPartWidth = 5;
 
 	// smooth
 	public double STYLE_SMOOTH_barPartSpaceAngleStyleValue = 1;
 	public int STYLE_SMOOTH_barPartWidth = 3;
 
 	// values
 	private double maximum = 100; // in %
 	private double minimum = 0; // in %
 	private double barValue = 0; // in %
 	private double buttonValue = 0;
 	private boolean mouseEvent = false; // true if a mouse button is pressed
 	private boolean keyEvent = false; // true if a key is pressed
 	private double keyScrollamount = -1; // -1 == auto
 	// pivot
 	private int searchCirclePivotX;
 	private int searchCirclePivotY;
 
 	// images
 	private Image imgBar;
 	private Image imgBarRotated; 
 	
 	private Image imgButton;
 	private Image imgButtonSelected;
 	private Image imgBarBackground;
 
 	// images Backups
 	private Image imgOrgBar;
 	private Image imgOrgButton;
 	private Image imgOrgBarBackground;
 
 	// desing
 	private STYLE style = STYLE.SMOOTH;
 	private boolean showButton = true;
 	private boolean rotateButton = true;
 	private double viewAngle = 360;
 	private boolean barRotated180 = false;
 
 	private int barDirection = BAR_DIRECTION_RIGHT;
 	private double startAngle = 0;
 	private double barPartSpaceAngle = 1; // @see recalcBarPartSpaceAngle();
 	private int barBoundsX = 10;
 	private int barBoundsY = 10;
 	private int barPartWidth = 3;
 	private int barPartHeight = 0;
 	private int buttonHeight = 18;
 	private int buttonWidth = 18;
 	private int buttonJutOut = 5;
 	private Anchor anchor = Anchor.CENTER;
 	private Alignment alignment = new Alignment(0, 0);
 
 	// vectors
 	private Vector2d povit = new Vector2d();
 	private Vector2d startPos = new Vector2d();
 	private Vector2d mousePos = new Vector2d();
 	private Vector2d povitStartPos = new Vector2d(-povit.getX()
 			+ startPos.getX(), -povit.getY() + startPos.getY()); // Vector from
 																	// Povit to
 																	// startpos
 	private Vector2d povitMousePos = new Vector2d(-povit.getX()
 			+ mousePos.getX(), -povit.getY() + mousePos.getY()); // Vector from
 																	// povit to
 																	// mousepos
 	private Vector2d buttonPosAtAngle = povit.add(povitStartPos).rotate(
 			Math.toRadians(0 * barDirection), povit);
 
 	public JSearchCircle() {
 		// we don't want to see anything from the Button ;)
 		// this.setBorder(BorderFactory.createEmptyBorder());
 		this.setContentAreaFilled(false);
 		this.setBorderPainted(false);
 
 		// init ...
 		this.setSize(new Dimension(300, 300));
 		this.setPreferredSize(new Dimension(300, 300));
 
 		this.addKeyListener(this);
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		
 		// init
 		reloadDefaultImages();
 
 		setButtonValue(buttonValue);
 		setBarValue(barValue);
 	}
 
 	public void reloadDefaultImages() {
 		
 		String resPackage = "design";
 		
 		// load Images
 		imgBar = new ImageIcon(getClass().getResource(resPackage + "/bar.png")).getImage();
 		
 		imgBarRotated = rotateImage180(imgBar);
 		
 		imgBarBackground = new ImageIcon(getClass().getResource(resPackage + "/barbackground.png")).getImage();
 		imgButton = new ImageIcon(getClass().getResource(resPackage + "/button.png")).getImage();
 		imgButtonSelected = new ImageIcon(getClass().getResource(resPackage + "/buttonSelected.png")).getImage();
 
 		// create Backups
 		imgOrgBar = imgBar;
 		imgOrgBarBackground = imgBarBackground;
 		imgOrgButton = imgButton;
 
 		recalcThickness();
 		recalcImageDesing();
 		recalcBarPartSpaceAngle();
 	}
 
 	public void addSearchCircleListener(SearchCircleListener scl) {
 		SearchCircleListener.add(scl);
 	}
 
 	public void removeSearchCircleListener(SearchCircleListener scl) {
 		SearchCircleListener.remove(scl);
 	}
 
 	public double getValue(double angle) {
 
 		double max = (double) maximum;
 		double min = (double) minimum;
 
 		double value = ((max - min) / viewAngle * (angle - startAngle)) + min;
 
 		double gap = ((max - min) / viewAngle * (360 - 300)) + min;
 
 		if (value > maximum + gap / 2) {
 			value = minimum;
 		} else if (value > maximum) {
 			value = maximum;
 		}
 
 		return value;
 	}
 
 	public double getAngle(double value) {
 
 		double max = (double) maximum;
 		double min = (double) minimum;
 
 		double angle = ((viewAngle / (max - min)) * value)
 				- ((viewAngle / (max - min)) * minimum);
 		angle += (startAngle * barDirection);
 
 		return angle;
 	}
 
 	public double recalcMouseAngle() {
 		double angle = povitMousePos.angle(povitStartPos); // calculate angle
 															// ...
 
 		buttonPosAtAngle = povit.add(povitStartPos).rotate(
 				Math.toRadians(angle * barDirection), povit);
 		double correctionAngle = povitMousePos.angle(buttonPosAtAngle
 				.sub(povit)); // calculate angle ...
 
 		// correct NaN error
 		if (Double.doubleToLongBits(correctionAngle) == Double
 				.doubleToLongBits(Double.NaN))
 			correctionAngle = 0.0;
 
 		if (correctionAngle >= buttonPosAngleCorrectionTolerance) {
 			angle = 180 + (180 - angle);
 		}
 
 		return angle;
 	}
 
 	/**
 	 * returns the Value of the Mouse on the circle (minimum - maximum)
 	 * 
 	 * @param event
 	 */
 	public double getMouseClickValue(double posX, double posY) {
 		// povit = new Vector2d(searchCirclePivotX, searchCirclePivotY); //povit
 		// of SearchCircle
 		// mousePos = new Vector2d(posX, posY); //mouse click pos from (0,0)
 		// startPos = new Vector2d(searchCirclePivotX, barBoundsY); //bar start
 		// point from (0,0)
 		//
 		// startPos = startPos.rotate(Math.toRadians(startAngle), povit);
 
 		recalcVectors(posX, posY);
 
 		// Vector2d povitStartPos = new Vector2d( -povit.getX() +
 		// startPos.getX(), -povit.getY() + startPos.getY() ); //Vector from
 		// Povit to startpos
 		// Vector2d povitMousePos = new Vector2d( -povit.getX() +
 		// mousePos.getX(), -povit.getY() + mousePos.getY() ); //Vector from
 		// povit to mousepos
 
 		double angle = recalcMouseAngle();
 
 		// povitMousePos.angle(povitStartPos); //calculate angle ...
 		//
 		// Vector2d buttonPosAtAngle =
 		// povit.add(povitStartPos).rotate(Math.toRadians(angle * barDirection),
 		// povit);
 		// double corresPackage + ".rectionAngle =
 		// povitMousePos.angle(buttonPosAtAngle.sub(povit)); //calculate angle
 		// ...
 		//
 		// //correct NaN error
 		// if (Double.doubleToLongBits(correctionAngle) ==
 		// Double.doubleToLongBits(Double.NaN)) correctionAngle = 0.0;
 		//
 		// if (correctionAngle >= buttonPosAngleCorrectionTolerance) {
 		// angle = 180 + (180 - angle) ;
 		// }
 
 		angle += startAngle; // startpos ...
 
 		return getValue(angle);
 	}
 
 	// public void setSize(Dimension size) {
 	// Dimension d = size;
 	//
 	// super.setSize(d);
 	// }
 	//
 	// public Dimension getSize() {
 	// super.setSize(700, 700);
 	// return new Dimension(300, 300);
 	// }
 
 	private void recalcPovit(Dimension size) {
 		if (anchor == Anchor.CENTER) {
 			searchCirclePivotX = this.getSize().width / 2 + alignment.getX();
 			searchCirclePivotY = this.getSize().height / 2 + alignment.getY();
 
 		} else if (anchor == Anchor.LEFT) {
 			searchCirclePivotX = this.getSize().height / 2 + alignment.getX();
 			searchCirclePivotY = this.getSize().height / 2 + alignment.getY();
 
 		} else if (anchor == Anchor.RIGHT) {
 			searchCirclePivotX = this.getSize().width
 					- (this.getSize().height / 2) + alignment.getX();
 			searchCirclePivotY = this.getSize().height / 2 + alignment.getY();
 
 		}
 	}
 
 	public void recalcPovit() {
 		recalcPovit(this.getSize());
 	}
 
 	/**
 	 * sets the Bar to the specified value
 	 * 
 	 * value must be in range of minimum and maximum
 	 * 
 	 * @param value
 	 */
 	public void setBarValue(double value) {
 		double oldValue = barValue;
 
 		// value is not in rage...
 		if (value > maximum) {
 			value = maximum;
 		} else if (value < minimum) {
 			value = minimum;
 		}
 
 		barValue = value;
 
 		for (SearchCircleListener scl : SearchCircleListener) {
 			scl.onBarChange(new SearchCircleChangeEvent(this, oldValue,
 					barValue));
 		}
 
 		repaint();
 	}
 
 	private Image rotateImage180(Image image) {
 		if (barRotated180) {
 			BufferedImage img = ImageModifier.createNewCompatibleBufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
 			Graphics2D g = img.createGraphics();
 			g.setRenderingHints(getRenderingHints());
 
 			int ix = img.getWidth() / 2;
 			int iy = img.getHeight() / 2;
 			g.rotate(Math.toRadians(180), ix, iy);
 			g.drawImage(image, 0, 0, null);
 
 			g.dispose();
 			return img;
 		} else {
 			return image;
 		}
 	}
 
 	public void setBarRotated180(boolean rotated) {
 		barRotated180 = rotated;
 		if (barRotated180) {
 			rotateImage180(imgBar);
 		}
 	}
 
 	public boolean getBarRotated180() {
 		return barRotated180;
 	}
 
 	/**
 	 * sets the Button to the specified value
 	 * 
 	 * value must be in range of minimum and maximum
 	 * 
 	 * @param value
 	 */
 	public synchronized void setButtonValueButEvent(double value) {
 		if (!mouseEvent && !keyEvent)
 			setButtonValue(value);
 	}
 
 	/**
 	 * sets the Button to the specified value
 	 * 
 	 * value must be in range of minimum and maximum
 	 * 
 	 * @param value
 	 */
 	public synchronized void setButtonValue(double value) {
 		double oldValue = buttonValue;
 
 		// value is not in rage...
 		if (value > maximum) {
 			value = maximum;
 		} else if (value < minimum) {
 			value = minimum;
 		}
 
 		buttonValue = value;
 
 		for (SearchCircleListener scl : SearchCircleListener) {
 			scl.onButtonChange(new SearchCircleChangeEvent(this, oldValue,
 					barValue));
 		}
 
 		repaint();
 	}
 
 	public boolean isMouseChangingButtonValue() {
 		return mouseEvent;
 	}
 
 	private void drawBackground(Graphics2D g) {
 		int lowValue = this.getSize().height;
 		if (this.getSize().width < this.getSize().height) {
 			lowValue = this.getSize().width;
 		}
 		
 		double backgroundPartSpaceAngle = barPartSpaceAngleFixVaule / lowValue * STYLE_SMOOTH_barPartSpaceAngleStyleValue;
 
 		// set start angle
 		g.rotate(Math.toRadians(startAngle), searchCirclePivotX, searchCirclePivotY); // rotate
 
 		// draw the bar to the specified value:
 		double curA = 0;
 		double minA = getAngle(minimum) - (startAngle * barDirection);
 		double maxA = (getAngle(maximum) - (startAngle * barDirection)) / backgroundPartSpaceAngle;
 
 		for (double index = minA; index <= maxA; index++) {
 
 			double a = backgroundPartSpaceAngle * barDirection;
 			a = (curA + a) * barDirection > viewAngle ? (viewAngle * barDirection) - curA : a;
 			curA += a;
 
 			g.rotate(Math.toRadians(a), searchCirclePivotX, searchCirclePivotY); // rotate
 			g.drawImage(imgBarBackground, searchCirclePivotX, barBoundsX,
 					STYLE_SMOOTH_barPartWidth, barPartHeight, this); // draw
 		}
 		
 		//reset rotation ...
 		g.rotate(Math.toRadians((startAngle + curA) * -1), searchCirclePivotX, searchCirclePivotY); // rotate
 	}
 	
 	private void drawBar(Graphics2D g){
 
 		// set start angle
 		g.rotate(Math.toRadians(startAngle), searchCirclePivotX, searchCirclePivotY); // rotate
 
 		// draw the bar to the specified value:
 		double curA = 0;
 		double minA = getAngle(minimum) - (startAngle * barDirection);
 		double maxA = (getAngle(barValue) - (startAngle * barDirection)) / barPartSpaceAngle;
 
 		for (double index = minA; index <= maxA; index++) {
 
 			double a = barPartSpaceAngle * barDirection;
 			a = (curA + a) * barDirection > viewAngle ? (viewAngle * barDirection) - curA : a;
 			curA += a;
 
 			g.rotate(Math.toRadians(a), searchCirclePivotX, searchCirclePivotY); // rotate
 			
 			if (imgBarRotated != null) {
 				g.drawImage(imgBarRotated, searchCirclePivotX, barBoundsY,
 						barPartWidth, barPartHeight, this); // draw
 			} else {
 				g.drawImage(imgBar, searchCirclePivotX, barBoundsY,
 						barPartWidth, barPartHeight, this); // draw
 			}
 		}
 		
 		//reset rotation ...
 		g.rotate(Math.toRadians((startAngle + curA) * -1), searchCirclePivotX, searchCirclePivotY); // rotate
 	}
 	
 	private void drawButton(Graphics2D g){
 
		double curA = getAngle(buttonValue) * barDirection;
 
		int ix = (searchCirclePivotX - buttonWidth / 2) + buttonWidth / 2;
		int iy = (barBoundsY - buttonJutOut) + buttonHeight / 2;
		
		// draws the Button on the specified value
		g.rotate(Math.toRadians(curA), searchCirclePivotX, searchCirclePivotY); // rotate

		if (rotateButton)
			g.rotate(Math.toRadians(curA) * -1, ix, iy);
 
 		if (this.hasFocus()){
 			g.drawImage(imgButtonSelected, searchCirclePivotX - buttonWidth / 2, barBoundsY
 					- buttonJutOut, buttonWidth, buttonHeight, this); // draw	
 		}else{
 			g.drawImage(imgButton, searchCirclePivotX - buttonWidth / 2, barBoundsY
 					- buttonJutOut, buttonWidth, buttonHeight, this); // draw
 		}

		//reset rotation ...
		g.rotate(Math.toRadians(curA),  ix, iy); // rotate
		g.rotate(Math.toRadians(curA * -1), searchCirclePivotX, searchCirclePivotY); // rotate
 	}
 
 	/**
 	 * repaints the search circle image
 	 * 
 	 * @param image
 	 */
 	public void repaintImages(Graphics2D g) {
 		
 		recalcPovit();
 		recalcBarPartSpaceAngle();
 		
 		//set fency graphics ...
 		g.setRenderingHints(getRenderingHints());
 
 		drawBackground(g);
 		drawBar(g);
 		drawButton(g);
 		
 		if (debug) {
 			// set start angle
 			g.setColor(Color.ORANGE);
 			g.drawLine(0, 0, (int) povit.getX(), (int) povit.getY());
 			g.setColor(Color.GREEN);
 			g.drawLine(0, 0, (int) mousePos.getX(), (int) mousePos.getY());
 			g.setColor(Color.BLUE);
 			g.drawLine(0, 0, (int) startPos.getX(), (int) startPos.getY());
 			g.setColor(Color.RED);
 			g.drawLine((int) povit.getX(), (int) povit.getY(),
 					(int) povit.add(povitStartPos).getX(),
 					(int) povit.add(povitStartPos).getY());
 			g.setColor(Color.YELLOW);
 			g.drawLine((int) povit.getX(), (int) povit.getY(),
 					(int) povit.add(povitMousePos).getX(),
 					(int) povit.add(povitMousePos).getY());
 			g.setColor(Color.DARK_GRAY);
 			g.drawLine((int) povit.getX(), (int) povit.getY(),
 					(int) buttonPosAtAngle.getX(),
 					(int) buttonPosAtAngle.getY());
 
 			double a = (getAngle(getValue(recalcMouseAngle())) - (startAngle * barDirection)) * barDirection;
 			Vector2d d = startPos.rotate(Math.toRadians(a), povit);
 			// System.out.println(d.getX() + " | " + d.getY() + " | " + a);
 			g.setColor(Color.PINK);
 			g.drawLine((int) povit.getX(), (int) povit.getY(), (int) d.getX(),
 					(int) d.getY());
 		}
 	}
 
 	public RenderingHints getRenderingHints() {
 		RenderingHints hints = new RenderingHints(
 				RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON); // enable Antialiasing ...
 		
 		hints.put(RenderingHints.KEY_RENDERING,
 				RenderingHints.VALUE_RENDER_QUALITY); // set render to HQ ...
 
 		return hints;
 	}
 
 	public void recalcVectors(double mousePosX, double mousePosY) {
 		povit = new Vector2d(searchCirclePivotX, searchCirclePivotY); // povit
 																		// of
 																		// SearchCircle
 		mousePos = new Vector2d(mousePosX, mousePosY); // mouse click pos from
 														// (0,0)
 		startPos = new Vector2d(searchCirclePivotX, barBoundsY); // bar start
 																	// point
 																	// from
 																	// (0,0)
 
 		startPos = startPos.rotate(Math.toRadians(startAngle), povit);
 
 		povitStartPos = new Vector2d(-povit.getX() + startPos.getX(),
 				-povit.getY() + startPos.getY()); // Vector from Povit to
 													// startpos
 		povitMousePos = new Vector2d(-povit.getX() + mousePos.getX(),
 				-povit.getY() + mousePos.getY()); // Vector from povit to
 													// mousepos
 	}
 
 	public boolean isMouseOverComponent(int x, int y) {
 
 		recalcVectors(x, y);
                 
 		double mRad = povitMousePos.length();
 		double maxRad = povitStartPos.length() + buttonJutOut;
 		double minRad = povitStartPos.length() - barPartHeight - buttonJutOut;
 
 		double angle = recalcMouseAngle();
  
 		if ((angle <= (viewAngle + buttonJutOut) ||  //<buttonJutOut>px over <viewangle>
                     angle >= (360 - buttonJutOut)) &&        //<buttonJutOut>px before startangle
                     mRad < maxRad && mRad > minRad) {       //vectorelength +-<buttonJutOut>px
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * This considers the componets insets on a mouse event dispatch<br>
 	 * <br>
 	 * Enable this if the dispatched mouse event provides wrong mouse position
 	 * values
 	 * 
 	 * @param arg0
 	 *            consider insets
 	 */
 	public void setConsiderInsetsOnMouseDispatch(boolean arg0) {
 		considerInsets = arg0;
 	}
 
 	public boolean isConsiderInsetsOnMouseDispatch() {
 		return considerInsets;
 	}
 
 	private void dispatchMouseEvent(MouseEvent event) {
 		for (Component component : passMouse) {
 			int offsetX = this.getX();
 			int offsetY = this.getY();
 
 			if (considerInsets) {
 				offsetX += this.getInsets().left;
 				offsetY += this.getInsets().top;
 			}
 
 			component.dispatchEvent(setOffset(event, offsetX, offsetY));
 		}
 	}
 
 	public void addParentMouseListener(Component component) {
 		passMouse.add(component);
 	}
 
 	public void removeParentMouseListener(Component component) {
 		passMouse.remove(component);
 	}
 
 	private MouseEvent setOffset(MouseEvent event, int xOffset, int yOffset) {
 		return new MouseEvent(this, event.getID(), event.getWhen(),
 				event.getModifiers(), event.getX() + xOffset, event.getY()
 						+ yOffset, event.getClickCount(),
 				event.isPopupTrigger());
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 		if (isMouseOverComponent(event.getX(), event.getY())) {
 			if (this.isEnabled()) {
 				mouseEvent = true;
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onMouseClicked(new SearchCircleMouseEvent(this, event));
 				}
 				mouseEvent = false;
 			}
 		} else
 			dispatchMouseEvent(event);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent event) {
 		if (isMouseOverComponent(event.getX(), event.getY())) {
 			if (this.isEnabled()) {
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onMouseEntered(new SearchCircleMouseEvent(this, event));
 				}
 			}
 		} else
 			dispatchMouseEvent(event);
 	}
 
 	@Override
 	public void mouseExited(MouseEvent event) {
 		if (isMouseOverComponent(event.getX(), event.getY())) {
 			if (this.isEnabled()) {
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onMouseExited(new SearchCircleMouseEvent(this, event));
 				}
 			}
 		} else
 			dispatchMouseEvent(event);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent event) {
 		if (isMouseOverComponent(event.getX(), event.getY())) {
 			if (this.isEnabled()) {
 				this.requestFocus();
 
 				mouseEvent = true;
 				setButtonValue(getMouseClickValue(event.getX(), event.getY()));
 
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onMousePressed(new SearchCircleMouseEvent(this, event));
 				}
 			}
 		} else
 			dispatchMouseEvent(event);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent event) {
 		if (isMouseOverComponent(event.getX(), event.getY()) || mouseEvent) {
 			if (this.isEnabled()) {
 
 				mouseEvent = false;
 
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onMouseReleased(new SearchCircleMouseEvent(this, event));
 				}
 			}
 		} else
 			dispatchMouseEvent(event);
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent event) {
 		if (mouseEvent) {
 			if (this.isEnabled()) {
 				setButtonValue(getMouseClickValue(event.getX(), event.getY()));
 				
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onMouseDragged(new SearchCircleMouseEvent(this, event));
 				}
 			}
 		} else
 			dispatchMouseEvent(event);
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent event) {
 		if (isMouseOverComponent(event.getX(), event.getY())) {
 			if (this.isEnabled()) {
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onMouseMoved(new SearchCircleMouseEvent(this, event));
 				}
 			}
 		} else
 			dispatchMouseEvent(event);
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 
 		// make sure that the circle is always in the JLabel
 		if (getSize().height > getSize().width) {
 			setSize(new Dimension(getSize().width, getSize().width));
 		}
 
 		super.paintComponent(g);
 		repaintImages((Graphics2D) g);
 	}
 
 	private void processSCKeyEvent(final KeyEvent e) {
 
 		keyEvent = true;
 
 		Thread ke = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while (keyEvent) {
 
 					double amount = ((maximum - minimum) / viewAngle) / 3;
 					if (amount <= 0)
 						amount = 1;
 
 					if (keyScrollamount >= 0)
 						amount = keyScrollamount;
 
 					if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 						setButtonValue(getButtonValue() + amount);
 
 					} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 						setButtonValue(getButtonValue() - amount);
 					}
 
 					for (int i = 0; i < 250; i++) {
 						if (!keyEvent)
 							break;
 						
 //						try {
 //							Thread.sleep(5);
 //						} catch (InterruptedException e) {
 //						}
 					}
 				}
 			}
 		});
 		ke.setName("SearchCircle(" + this.getName() + ")@" + this.hashCode()
 				+ "#KeyEventThread");
 		ke.start();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent event) {
 		if (this.isEnabled()) {
 
 			if (!keyEvent) {
 				for (SearchCircleListener scl : SearchCircleListener) {
 					scl.onKeyPressed(new SearchCircleKeyEvent(this, event));
 				}
 			}
 
 			processSCKeyEvent(event);
 
 			for (SearchCircleListener scl : SearchCircleListener) {
 				scl.onKeyHold(new SearchCircleKeyEvent(this, event));
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent event) {
 		if (this.isEnabled() || keyEvent) {
 
 			keyEvent = false;
 
 			for (SearchCircleListener scl : SearchCircleListener) {
 				scl.onKeyReleased(new SearchCircleKeyEvent(this, event));
 			}
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent event) {
 		if (this.isEnabled()) {
 			for (SearchCircleListener scl : SearchCircleListener) {
 				scl.onKeyTyped(new SearchCircleKeyEvent(this, event));
 			}
 		}
 	}
 
 	public double getBarValue() {
 		return barValue;
 	}
 
 	public double getButtonValue() {
 		return buttonValue;
 	}
 
 	public double getMaximum() {
 		return maximum;
 	}
 
 	public double getMinimum() {
 		return minimum;
 	}
 
 	public void setMaximum(double maximum) {
 		this.maximum = maximum;
 	}
 
 	public void setMinimum(double minimum) {
 		this.minimum = minimum;
 	}
 
 	public boolean isKeyEvent() {
 		return keyEvent;
 	}
 
 	public boolean isMousePressed() {
 		return mouseEvent;
 	}
 
 	public boolean isDebug() {
 		return debug;
 	}
 
 	public void setDebug(boolean debug) {
 		this.debug = debug;
 	}
 
 	/**
 	 * default image size: 9 x 20
 	 * 
 	 * @param img
 	 */
 	public void setBarImage(Image img) {
 		imgBar = img;
 		imgOrgBar = imgBar;
 		imgBarRotated = rotateImage180(img);
 		recalcThickness();
 		recalcImageDesing();
 	}
 
 	/**
 	 * default image size: 50 x 50
 	 * 
 	 * @param img
 	 */
 	public void setButtondImage(Image img) {
 		imgButton = img;
 		imgOrgButton = imgButton;
 		recalcThickness();
 		recalcImageDesing();
 	}
 
 	/**
 	 * default image size: 9 x 20
 	 * 
 	 * @param img
 	 */
 	public void setBarBackgroundImage(Image img) {
 		imgBarBackground = img;
 		imgOrgBarBackground = imgBarBackground;
 		recalcThickness();
 		recalcImageDesing();
 	}
 
 	public Image getBarImage() {
 		return imgBar;
 	}
 
 	public Image getButtonImage() {
 		return imgButton;
 	}
 
 	public Image getBarBackgroundImage() {
 		return imgBarBackground;
 	}
 
 	public Image getOriginalBarImage() {
 		return imgOrgBar;
 	}
 
 	public Image getOriginalButtonImage() {
 		return imgOrgButton;
 	}
 
 	public Image getOriginalBarBackgroundImage() {
 		return imgOrgBarBackground;
 	}
 
 	public void setButtonVisible(boolean showButton) {
 		this.showButton = showButton;
 	}
 
 	public boolean getButtonVisible() {
 		return showButton;
 	}
 
 	public void setButtonJutOut(int amount) {
 		buttonJutOut = amount;
 	}
 
 	public int getButtonJutOut() {
 		return buttonJutOut;
 	}
 
 	public void setButtonSize(Dimension size) {
 		buttonWidth = size.width;
 		buttonHeight = size.height;
 	}
 
 	public Dimension getButtonSize() {
 		return new Dimension(buttonWidth, buttonHeight);
 	}
 
 	public void setKeyScrollamount(double keyScrollamount) {
 		this.keyScrollamount = keyScrollamount;
 	}
 
 	public double getKeyScrollamount() {
 		return keyScrollamount;
 	}
 
 	public void setRotateButton(boolean rotate) {
 		this.rotateButton = rotate;
 	}
 
 	public boolean getRotateButton() {
 		return rotateButton;
 	}
 
 	public void setBarThickness(int thickness) {
 		barPartHeight = thickness;
 		recalcButtonSize();
 	}
 
 	public int getBarThickness() {
 		return barPartHeight;
 	}
 
 	public void recalcThickness() {
 		barPartHeight = imgBar.getHeight(null) / 2; // calculate default
 													// thickness from the image
 													// height
 		recalcButtonSize();
 	}
 
 	public void recalcButtonSize() {
 		int oldbuttonHeight = buttonHeight;
 		buttonHeight = barPartHeight + (2 * buttonJutOut); // calculate default
 															// button size ...
 
 		int diff = Math.abs(buttonHeight - oldbuttonHeight);
 		if (oldbuttonHeight > buttonHeight)
 			diff *= -1;
 
 		buttonWidth += diff;
 	}
 
 	public void recalcImageDesing() {
 		// set image size ...
 		buttonHeight = 10 + barPartHeight;
 		buttonWidth = 10 + barPartHeight;
 		recalcThickness();
 	}
 
 	/**
 	 * @see recalcBarPartSpaceAngle(double barPartSpaceAngleStyleValue, int
 	 *      barPartWidth)
 	 */
 	public void recalcBarPartSpaceAngle() {
 		double barPartSpaceAngleStyleValue = 1;
 		if (style == STYLE.PARTS) {
 			barPartSpaceAngleStyleValue = STYLE_PARTS_barPartSpaceAngleStyleValue;
 			barPartWidth = STYLE_PARTS_barPartWidth;
 		} else if (style == STYLE.SMOOTH) {
 			barPartSpaceAngleStyleValue = STYLE_SMOOTH_barPartSpaceAngleStyleValue;
 			barPartWidth = STYLE_SMOOTH_barPartWidth;
 		}
 
 		recalcBarPartSpaceAngle(barPartSpaceAngleStyleValue);
 	}
 
 	/**
 	 * At the Size of 300 x 300 (@see barPartSpaceAngleFixVaule) and a
 	 * barPartWidth of 3 the barPartSpaceAngle must be equals 1 to make sure it
 	 * looks smooth.
 	 * 
 	 * @param barPartSpaceAngleStyleValue
 	 * @param barPartWidth
 	 */
 	public void recalcBarPartSpaceAngle(double barPartSpaceAngleStyleValue) {
 		int lowValue = this.getSize().height;
 		if (this.getSize().width < this.getSize().height) {
 			lowValue = this.getSize().width;
 		}
 		barPartSpaceAngle = (barPartSpaceAngleFixVaule) / lowValue
 				* barPartSpaceAngleStyleValue;
 	}
 
 	public void setStyle(STYLE style) {
 		this.style = style;
 	}
 
 	public STYLE getStyle() {
 		return style;
 	}
 
 	public void setStartAngle(double angle) {
 		this.startAngle = angle;
 	}
 
 	public double getStartAngle() {
 		return startAngle;
 	}
 
 	public void setViewAngle(double angle) {
 		this.viewAngle = angle;
 	}
 
 	public double getViewAngle() {
 		return viewAngle;
 	}
 
 	public void setAnchor(Anchor position) {
 		anchor = position;
 	}
 
 	public Anchor getAnchor() {
 		return anchor;
 	}
 
 	public void setAlignment(int alignmentX, int alignmentY) {
 		alignment = new Alignment(alignmentX, alignmentY);
 	}
 
 	public Alignment getAlignment() {
 		return alignment;
 	}
 
 	public void setDirection(int direction) {
 		barDirection = direction;
 	}
 
 	public int getDirection() {
 		return barDirection;
 	}
 
 	private static class Alignment {
 		private int alignmentX = 0;
 		private int alignmentY = 0;
 
 		public Alignment(int alignmentX, int alignmentY) {
 			this.alignmentX = alignmentX;
 			this.alignmentY = alignmentY;
 		}
 
 		public int getX() {
 			return alignmentX;
 		}
 
 		public int getY() {
 			return alignmentY;
 		}
 	}
 
 	public void setButtonHSB(HSB hsb) {
 		int thickness = getBarThickness();
 
 		ImageModifier im = new ImageModifier(this.getOriginalButtonImage());
 		
 		im.setHue(hsb.getHue());
 		im.setSaturation(hsb.getSaturation());
 		im.setBrightness(hsb.getBrightness());
 
 		imgButton = im.modify();
 		
 		setBarThickness(thickness);
 		
 		repaint();
 	}
 
 	public void setBarHSB(HSB hsb) {
 		int thickness = getBarThickness();
 
 		ImageModifier im = new ImageModifier(this.getOriginalBarImage());
 		
 		im.setHue(hsb.getHue());
 		im.setSaturation(hsb.getSaturation());
 		im.setBrightness(hsb.getBrightness());
 
 		imgBar = im.modify();
 		imgBar = rotateImage180(imgBar);
 		
 		setBarThickness(thickness);
 		
 		repaint();
 	}
 
 	public void setBackgroundHSB(HSB hsb) {
 		int thickness = getBarThickness();
 
 		ImageModifier im = new ImageModifier(this.getOriginalBarBackgroundImage());
 		
 		im.setHue(hsb.getHue());
 		im.setSaturation(hsb.getSaturation());
 		im.setBrightness(hsb.getBrightness());
 		
 		imgBarBackground = im.modify();
 
 		setBarThickness(thickness);
 		
 		repaint();
 	}
 
 }
