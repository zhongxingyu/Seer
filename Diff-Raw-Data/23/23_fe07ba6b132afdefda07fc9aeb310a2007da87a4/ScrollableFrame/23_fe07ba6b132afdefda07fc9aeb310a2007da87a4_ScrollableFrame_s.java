 //A scrollable frame containing UIElements. They can be placed in any arrangement.
 //If there is enough height to display all, no scroll bar is dislayed.
 
 package org.haferlib.slick.gui;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Color;
 
 import java.util.ArrayList;
 
 public class ScrollableFrame extends GUISubcontext {
 
 	// Non-input fields.
 	protected int x2, y2;
 	protected int width, height;
 	protected int depth;
 	protected boolean hasScrollBar;
 	protected int scrollStepSize;
 	protected int scrollBarX, scrollBarY;
 	protected int scrollBarWidth, scrollBarHeight;
 	protected int scrollBarCornerRadius;
 	protected Color scrollBarColor;
 
 	// Input fields.
 	private boolean mouseDragging;
 	private int mouseDragDistFromScrollY;
 
 	// Constructors.
 	public ScrollableFrame(int x, int y, int width, int height, int depth, int scrollBarWidth, Color scrollBarColor) {
 		super(x, y);
 		setWidth(width);
 		setHeight(height);
 		setDepth(depth);
 		this.scrollBarWidth = scrollBarWidth;
 		scrollBarCornerRadius = scrollBarWidth / 2;
 		this.scrollBarColor = scrollBarColor;
 		scrollBarX = x2 - scrollBarWidth;
 		scrollBarY = y1;
 		scrollStepSize = 0;
 		recalculateScrollingFields();
 	}
 
 	public ScrollableFrame(GUIElement[] elements, int x, int y, int width, int height, int depth, int scrollBarWidth, Color scrollBarColor) {
 		this(x, y, width, height, depth, scrollBarWidth, scrollBarColor);
 		addElements(elements);
 	}
 
 	
 	@Override
 	public void update(int delta) {
 		// Scroll when dragging.
 		super.update(delta);
 		if (mouseDragging) {
 			int lastScrollBarY = scrollBarY;
 			scrollBarY = mouseY + mouseDragDistFromScrollY;
 			if (scrollBarY < y1)
 				scrollBarY = y1;
 			else if (scrollBarY + scrollBarHeight > y2)
 				scrollBarY = y2 - scrollBarHeight;
 			int dScroll = lastScrollBarY - scrollBarY;
 			if (dScroll != 0)
 				scroll(dScroll);
 		}
 	}
 
 	@Override
 	public void render(Graphics g) {
 		// Draw the subcontext.
 		renderSubcontext(g, x1, y1, x2, y2);
 
 		// Draw the scroll bar if there is one.
 		if (hasScrollBar) {
 			g.setColor(scrollBarColor);
 			g.fillRoundRect(scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight, scrollBarCornerRadius);
 		}
 	}
 
 	// Recalculate the scrolling fields.
 	protected void recalculateScrollingFields() {
 		// First, determine the smallest y and the largest y of the elements.
 		ArrayList<GUIElement> elements = subcontext.getElements();
 		int topY = Integer.MAX_VALUE;
 		int bottomY = Integer.MIN_VALUE;
 		for (GUIElement e : elements) {
 			if (e.getY() < topY)
 				topY = e.getY();
 			if (e.getY() + e.getHeight() > bottomY)
 				bottomY = e.getY() + e.getHeight();
 		}
 
 		// Translate the elements so the top one has y = y1.
 		subcontext.translateY(y1 - topY);
 
 		// Calculate the height of the scrolling area. If it's smaller than the height, just move everything up to the top of the frame and don't show the scroll bar.
 		int scrollAreaHeight = bottomY - topY;
 		if (scrollAreaHeight <= height) {
 			hasScrollBar = false;
 			return;
 		}
 
 		// Get the amount we have currently scrolled.
 		int amountScrolled = (scrollBarY - y1) * scrollStepSize;
 
 		// If we will have a scroll bar, adjust the scroll area and scroll bar.
 		hasScrollBar = true;
 		scrollBarY = y1;
 		scrollBarHeight =  height * height / scrollAreaHeight; //Set the scroll bar height to the (percentage of the scroll area that is shown by the frame) * frame height
 		scrollStepSize = (int)Math.ceil((float)(scrollAreaHeight - height) / (height - scrollBarHeight)); //The amount of translation to do for every pixel the scroll bar moves.
 		mouseDragging = false;
 
 		// Scroll back to where we were now that everything is all taken care of.
 		if (amountScrolled != 0) {
 			amountScrolled /= scrollStepSize;
 			scrollBarY += amountScrolled;
 			scroll(-amountScrolled);
 		}
 	}
 
 	// Scroll some amount of pixels on the scroll bar.
 	public void scroll(int amount) {
 		subcontext.translateY(amount * scrollStepSize);
 	}
 
 	// Get the width of the scroll bar.
 	public int getScrollBarWidth() {
 		return scrollBarWidth;
 	}
 	
 	// Add and remove elements.
 	public void addElement(GUIElement e) {
 		subcontext.addElement(e);
 		subcontext.addAndRemoveElements();
 		recalculateScrollingFields();
 	}
 
 	public void addElements(GUIElement[] es) {
 		for (int i = 0; i < es.length; i++)
 			subcontext.addElement(es[i]);
 		subcontext.addAndRemoveElements();
 		recalculateScrollingFields();
 	}
 
 	public void removeElement(GUIElement e) {
 		if (subcontext.contains(e)) {
 			subcontext.removeElement(e);
 			subcontext.addAndRemoveElements();
 			recalculateScrollingFields();
 		}
 	}
 	
 	public void clearElements() {
 		subcontext.clear();
 	}
 	
 	public void reinitSubcontext() {
 		subcontext.destroy();
 		subcontext = new GUIContext();
 	}
 	
 	@Override
 	public void setX(int x) {
 		super.setX(x);
 		x2 = x1 + width;
 		scrollBarX = x2 - scrollBarWidth;
 	}
 
 	@Override
 	public void setY(int y) {
 		scrollBarY = (scrollBarY - y1) + y;
 		super.setY(y);
 		y2 = y1 + height;
 	}
 
 	@Override
 	public void setWidth(int w) {
 		width = w;
 		x2 = x1 + width;
 	}
 
 	@Override
 	public int getWidth() {
 		return width;
 	}
 
 	@Override
 	public void setHeight(int h) {
 		height = h;
 		y2 = y1 + height;
 	}
 
 	@Override
 	public int getHeight() {
 		return height;
 	}
 
 	@Override
 	public boolean pointIsWithin(int x, int y) {
 		return (x >= x1 && x <= x2 && y >= y1 && y <= y2);
 	}
 
 	@Override
 	public void mouseDown(int x, int y, int button) {
 		super.mouseDown(x, y, button);
 		if (!mouseDragging && x > scrollBarX && y > scrollBarY && y < scrollBarY + scrollBarHeight) {
 			mouseDragging = true;
 			mouseDragDistFromScrollY = scrollBarY - y;
 		}
 	}
 
 	@Override
 	public void hover(int x, int y) {
 		super.hover(x, y);
 		mouseDragging = false;
 	}
 
 	@Override
 	public void clickedElsewhere(GUIElement target, int button) {
 		super.clickedElsewhere(target, button);
 		mouseDragging = false;
 	}
 
 	@Override
 	public void mouseDownElsewhere(GUIElement target, int button) {
 		super.mouseDownElsewhere(target, button);
 		mouseDragging = false;
 	}
 
 	@Override
 	public void hoveredElsewhere(GUIElement target) {
 		super.hoveredElsewhere(target);
 		mouseDragging = false;
 	}
 	
 	@Override
 	public void setDepth(int d) {
 		depth = d;
 	}
 
 	@Override
 	public int getDepth() {
 		return depth;
 	}
 
 	@Override
 	public boolean dead() {
 		return false;
 	}
 
 }
