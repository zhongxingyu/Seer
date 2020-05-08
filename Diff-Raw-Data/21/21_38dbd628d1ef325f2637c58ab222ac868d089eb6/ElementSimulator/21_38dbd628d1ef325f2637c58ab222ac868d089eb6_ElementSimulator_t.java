 package com.partyrock.element;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 
 import com.partyrock.settings.SectionSettings;
 
 /**
  * The base class for an element being simulated
  * @author Matthew
  * 
  */
 public abstract class ElementSimulator {
 
 	private ElementController controller;
 	private int x;
 	private int y;
 	private Point textSize;
 	private boolean collapsed = false;
 
 	public ElementSimulator(ElementController controller) {
 		this.controller = controller;
 
 		x = 25;
 		y = 25;
 	}
 
 	/**
 	 * What's actually called by the simulator (to handle things like closing)
 	 * @param gc GC to render with
 	 */
 	public void render(GC gc) {
 		renderName(gc);
 
 		// Render the element if it's not collapsed
 		if (!isCollapsed()) {
 			renderElement(gc);
 		}
 	}
 
 	/**
 	 * Render the element in the simulator
 	 * @param gc The GC to draw with
 	 */
 	public abstract void renderElement(GC gc);
 
 	/**
 	 * Renders the name of the element
 	 * @param gc
 	 */
 	public void renderName(GC gc) {
 		gc.setForeground(controller.getMaster().getWindowManager().getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		gc.drawText(getText(), x, y, true);
 
 		// We have to do this so we can calculate the width and height during a drag
 		getTextBounds(gc);
 	}
 
 	/**
 	 * Returns the size of the name, so the element can render its simulation under it
 	 * @param gc The GC to use
 	 * @return The Point with the bounds of the name
 	 */
 	public Point getTextBounds(GC gc) {
 		return (textSize = gc.textExtent(getText()));
 	}
 
 	/**
 	 * Returns the last cached textSize
 	 */
 	public Point getTextBounds() {
 		return textSize;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public void setPos(int x, int y) {
 		setX(x);
 		setY(y);
 	}
 
 	/**
 	 * Returns the X value the simulation is at
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Returns the Y value the simulation is at
 	 */
 	public int getY() {
 		return y;
 	}
 
 	public int getWidth() {
 		if (isCollapsed()) {
 			return getTextBounds().x;
 		}
 
 		return getNormalWidth();
 	}
 
 	public int getHeight() {
 		if (isCollapsed()) {
 			return getTextBounds().y;
 		}
 
 		return getNormalHeight();
 	}
 
 	/**
 	 * Returns the width when not compressed
 	 */
 	public abstract int getNormalWidth();
 
 	/**
 	 * Returns the height when not compressed
 	 * @return
 	 */
 	public abstract int getNormalHeight();
 
 	/**
 	 * Returns if the given mouseDown event occurs inside the element
 	 * @param e The MouseEvent to check
 	 * @return true if it's contained, false if not
 	 */
 	public boolean mouseDown(MouseEvent e) {
 		if (e.x >= getX() && e.y >= getY() && e.x <= getX() + getWidth() && e.y <= getY() + getHeight()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Returns the text that should be rendered
 	 */
 	public String getText() {
 		return (isCollapsed() ? "(+) " : "") + controller.getName();
 	}
 
 	public void setCollapsed(boolean c) {
 		collapsed = c;
 	}
 
 	public boolean isCollapsed() {
 		return collapsed;
 	}
 
 	/**
 	 * Saves data like x coord, y coord, etc.
 	 * @param settings
 	 */
 	public void saveData(SectionSettings settings) {
 		settings.put("simulator_x", getX());
 		settings.put("simulator_y", getY());
 		settings.put("simulator_collapsed", (isCollapsed() ? "true" : "false"));
 	}
 }
