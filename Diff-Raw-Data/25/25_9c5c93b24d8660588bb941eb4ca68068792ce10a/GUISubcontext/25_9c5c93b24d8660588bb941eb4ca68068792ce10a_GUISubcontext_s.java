 //An interface for anything that can be drawn as a UI element.
 //Coordinates for all parameters are always screen coordinates.
 
 package org.haferlib.slick.gui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 
 public abstract class GUISubcontext implements GUIElement {
 
 	protected int x1, y1;
 	protected GUIContext subcontext;
 	protected boolean lmbPressed, mmbPressed, rmbPressed;
 	protected boolean lmbDown, mmbDown, rmbDown;
 	protected int mouseX, mouseY;
 	private Rectangle gClip;
 
 	// Constructors.
 	protected GUISubcontext() {
 		this(0, 0);
 	}
 
 	protected GUISubcontext(int x, int y) {
 		subcontext = new GUIContext();
 		x1 = x;
 		y1 = y;
 		gClip = new Rectangle(0, 0, 0, 0);
 	}
 	
 	// EFFECTS:  Get the elements in the subcontext.
 	public ArrayList<GUIElement> getElements() {
 		return subcontext.getElements();
 	}
 	
 	// EFFECTS:  See if the subcontext contains an element.
 	public boolean contains(GUIElement e) {
 		return subcontext.contains(e);
 	}
 	
 	// EFFECTS:  Get the element immediately above the element given,
 	//			 or null if one could not be found.
 	public GUIElement getElementAbove(GUIElement e) {
 		return getElementAbove(e.getY());
 	}
 
 	// EFFECTS:  Get the element immediately above the given y coordinate,
 	//			 or null if one could not be found.
 	public GUIElement getElementAbove(int y) {
 		GUIElement out = null;
 		int dY = Integer.MAX_VALUE;
 
 		// Loop through the elements to find the one closest to the y.
 		for (GUIElement e : subcontext.getElements()) {
 			int newDY = y - e.getY();
 			if (newDY < dY && newDY > 0) {
 				dY = newDY;
 				out = e;
 			}
 		}
 
 		return out;
 	}
 
 	// EFFECTS:  Get the element immediately below the element given,
 	//			 or null if one could not be found.
 	public GUIElement getElementBelow(GUIElement e) {
 		return getElementBelow(e.getY());
 	}
 
 	// EFFECTS:  Get the element immediately below the given y coordinate,
 	//			 or null if one could not be found.
 	public GUIElement getElementBelow(int y) {
 		// Get a copy of the elements list sorted by Y.
 		ArrayList<GUIElement> elements = new ArrayList<GUIElement>(subcontext.getElements());
 		Collections.sort(elements, new ElementYComparator());
 
 		// Loop through the elements backwards until we find an element that is above the y.
 		for (int i = elements.size() - 2; i > -1; i--) {
 			if (elements.get(i).getY() < y)
 				return elements.get(i + 1);
 		}
 
 		// Return null if we can't find anything below the y.
 		return null;
 	}
 
 	// EFFECTS:  Get the element that contains a point,
 	//			 or null if one could not be found.
 	public GUIElement getElementAtPoint(int x, int y) {
 		return subcontext.getElementAtPoint(x, y);
 	}
 	
 	// REQUIRES: g != null
 	// EFFECTS:  Render the subcontext within the already defined clip area. Returns g.getClip()
 	//			 to its state when this method was called by the end so as not to interfere with
 	//			 other rendering.
 	protected void renderSubcontext(Graphics g, int leftX, int topY, int rightX, int bottomY) {
 		// Copy the old clipping.
 		gClip.setBounds(g.getClip());
 		
 		// Only make the clip area smaller. Don't increase its size.
		if (leftX < gClip.getX())
			leftX = (int)gClip.getX();
 		
		if (topY < gClip.getY())
			topY = (int)gClip.getY();
 		
		if (rightX > gClip.getX() + gClip.getWidth())
			rightX = (int)(gClip.getX() + gClip.getWidth());
		
		if (bottomY > gClip.getY() + gClip.getHeight());
			bottomY = (int)(gClip.getY() + gClip.getHeight());
 		
 		// Render the subcontext.
 		subcontext.render(g, leftX, topY, rightX, bottomY);
 		
 		// Set the clip to what it was.
 		g.setClip(gClip);
 	}
 
 	// Update the subcontext.
 	@Override
 	public void update(int delta) {
 		subcontext.update(mouseX, mouseY, lmbPressed, mmbPressed, rmbPressed, lmbDown, mmbDown, rmbDown, delta);
 		lmbPressed = false;
 		mmbPressed = false;
 		rmbPressed = false;
 		lmbDown = false;
 		mmbDown = false;
 		rmbDown = false;
 	}
 	
 	@Override
 	public void setX(int x) {
 		int dX = x - x1;
 		x1 = x;
 		subcontext.translateX(dX);
 	}
 
 	@Override
 	public int getX() {
 		return x1;
 	}
 
 	@Override
 	public void setY(int y) {
 		int dY = y - y1;
 		y1 = y;
 		subcontext.translateY(dY);
 	}
 
 	@Override
 	public int getY() {
 		return y1;
 	}
 
 	// Track mouse clicks.
 	@Override
 	public void click(int x, int y, int button) {
 		mouseX = x;
 		mouseY = y;
 		if (button == Input.MOUSE_LEFT_BUTTON)
 			lmbPressed = true;
 		else if (button == Input.MOUSE_MIDDLE_BUTTON)
 			mmbPressed = true;
 		else
 			rmbPressed = true;
 	}
 
 	// Track mouseDowns.
 	@Override
 	public void mouseDown(int x, int y, int button) {
 		mouseX = x;
 		mouseY = y;
 		if (button == Input.MOUSE_LEFT_BUTTON)
 			lmbDown = true;
 		else if (button == Input.MOUSE_MIDDLE_BUTTON)
 			mmbDown = true;
 		else
 			rmbDown = true;
 	}
 
 	// Track mouse position.
 	@Override
 	public void hover(int x, int y) {
 		mouseX = x;
 		mouseY = y;
 	}
 
 	// Track clicks and mouse position.
 	@Override
 	public void clickedElsewhere(GUIElement target, int button) {
 		mouseX = Integer.MIN_VALUE;
 		mouseY = Integer.MIN_VALUE;
 		if (button == Input.MOUSE_LEFT_BUTTON)
 			lmbPressed = true;
 		else if (button == Input.MOUSE_MIDDLE_BUTTON)
 			mmbPressed = true;
 		else
 			rmbPressed = true;
 	}
 
 	// Track mouseDowns and mouse position.
 	@Override
 	public void mouseDownElsewhere(GUIElement target, int button) {
 		mouseX = Integer.MIN_VALUE;
 		mouseY = Integer.MIN_VALUE;
 		if (button == Input.MOUSE_LEFT_BUTTON)
 			lmbDown = true;
 		else if (button == Input.MOUSE_MIDDLE_BUTTON)
 			mmbDown = true;
 		else
 			rmbDown = true;
 	}
 
 	// Track mouse position.
 	@Override
 	public void hoveredElsewhere(GUIElement target) {
 		mouseX = Integer.MIN_VALUE;
 		mouseY = Integer.MIN_VALUE;
 	}
 
 	// Tell the subcontext about key presses.
 	@Override
 	public void keyPressed(int key, char c) {
 		subcontext.keyPressed(key, c);
 	}
 
 	@Override
 	public void keyInputDone() {
 	}
 	
 	// Destroy the subcontext.
 	@Override
 	public void destroy() {
 		subcontext.destroy();
 	}
 
 }
