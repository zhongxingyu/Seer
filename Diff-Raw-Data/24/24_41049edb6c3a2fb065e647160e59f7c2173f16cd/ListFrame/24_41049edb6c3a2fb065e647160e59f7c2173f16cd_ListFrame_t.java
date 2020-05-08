  package org.haferlib.slick.gui;
 
 import org.newdawn.slick.Graphics;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 
 public class ListFrame extends GUISubcontext implements GUIEventGenerator, GUIEventListener {
 	
 	public static final byte XALIGN_LEFT = 0;
 	public static final byte XALIGN_CENTER = 1;
 	public static final byte XALIGN_RIGHT = 2;
 	
 	private int x2, y2;
 	private int width, height;
 	private int depth;
 	private byte xAlign;
 	private int xAlignOffset;
 	private int ySpacing;
 	private int lastSize; // Keeps track of the number of elements in this list so if any die the elements can be realigned.
 	private HashSet<GUIEventListener> listeners;
 	
 	// Constructors.
 	public ListFrame(int x, int y, int width, int depth, byte xAlign, int xAlignOffset, int ySpacing) {
 		super(x, y);
 		listeners = new HashSet<>();
 		setWidth(width);
 		recalculateHeight();
 		setDepth(depth);
 		if (xAlign == XALIGN_LEFT || xAlign == XALIGN_CENTER || xAlign == XALIGN_RIGHT)
 			this.xAlign = xAlign;
 		else
 			this.xAlign = XALIGN_LEFT;
 		this.xAlignOffset = xAlignOffset;
 		this.ySpacing = ySpacing;
 		lastSize = 0;
 	}
 	
 	public ListFrame(int x, int y, int width, int depth) {
 		this(x, y, width, depth, XALIGN_LEFT, 0, 0);
 	}
 
 	public ListFrame(GUIElement[] elements, int x, int y, int width, int depth, byte xAlign, int xAlignOffset, int ySpacing) {
 		this(x, y, width, depth, xAlign, xAlignOffset, ySpacing);
 		addElements(elements);
 	}
 	
 	public ListFrame(GUIElement[] elements, int x, int y, int width, int depth) {
 		this(elements, x, y, width, depth, XALIGN_LEFT, 0, 0);
 	}
 	
 	// EFFECTS:  Return xAlign.
 	public byte getXAlign() {
 		return xAlign;
 	}
 	
 	// EFFECTS:  Return xAlignOffset.
 	public int getXAlignOffset() {
 		return xAlignOffset;
 	}
 	
 	// EFFECTS:  Return ySpacing.
 	public int getYSpacing() {
 		return ySpacing;
 	}
 	
 	// EFFECTS:  Get the x coordinate that we are aligning to.
 	private int getAlignedXAnchor() {
 		switch (xAlign) {
 			case XALIGN_LEFT: return x1 + xAlignOffset;
 			case XALIGN_CENTER: return x1 + getWidth() / 2 + xAlignOffset;
 			default: return x1 + getWidth() + xAlignOffset;
 		}
 	}
 
 	// EFFECTS:  X align an element to an x anchor.
 	private void alignElementX(GUIElement e, int xAnchor) {
 		switch (xAlign) {
 			case XALIGN_LEFT: e.setX(xAnchor); break;
 			case XALIGN_CENTER: e.setX(xAnchor - e.getWidth() / 2); break;
 			default: e.setX(xAnchor - e.getWidth()); break;
 		}
 	}
 
 	// EFFECTS:  Find the bottom y of the bottom element in this frame.
 	private int getBottomY() {
 		// Get the elements and loop through them to find which is on bottom.
 		ArrayList<GUIElement> elements = subcontext.getElements();
 		int bottomY = y1;
 		for (GUIElement e : elements) {
 			int elementY2 = e.getY() + e.getHeight();
 			if (elementY2 > bottomY)
 				bottomY = elementY2;
 		}
 		return bottomY;
 	}
 	
 	// EFFECTS:  Recalculate the height.
 	public void recalculateHeight() {
 		int oldHeight = height;
 		y2 = getBottomY();
 		height = y2 - y1;
 		if (height != oldHeight) {
 			for (GUIEventListener l : listeners) {
 				l.guiEvent(new GUIEvent<Object>(this, GUIEvent.RESIZE_EVENT));
 			}
 		}
 	}
 	
 	// EFFECTS:  Realign all the elements.
 	private void realignAllElements() {
 		// Get a copy of the elements list sorted by Y.
 		ArrayList<GUIElement> elements = new ArrayList<GUIElement>(subcontext.getElements());
 		Collections.sort(elements, new ElementYComparator());
 
 		// Reposition the elements below the index.
 		int xAnchor = getAlignedXAnchor();
 		int yPos = y1;
 		for (int i = 0; i < elements.size(); i++) {
 			GUIElement e = elements.get(i);
 			alignElementX(e, xAnchor);
 			e.setY(yPos);
 			yPos += e.getHeight() + ySpacing;
 		}
 
 		// Recalculate the height.
 		recalculateHeight();
 	}
 
 	// EFFECTS:  Realign the elements below the element given.
 	//			 If the element given is not in this, realign all elements.
 	private void realignFromElement(GUIElement e) {
 		// Get a copy of the elements list sorted by Y.
 		ArrayList<GUIElement> elements = new ArrayList<GUIElement>(subcontext.getElements());
 		Collections.sort(elements, new ElementYComparator());
 
 		// Get the index of the element, returning if we can't find it.
 		int i = elements.indexOf(e);
 		if (i == -1) {
 			realignAllElements();
 			return;
 		}
 
 		// Reposition the elements below the index.
 		int xAnchor = getAlignedXAnchor();
 		int yPos = e.getY() + e.getHeight() + ySpacing;
 		for (i += 1; i < elements.size(); i++) {
 			e = elements.get(i);
 			alignElementX(e, xAnchor);
 			e.setY(yPos);
 			yPos += e.getHeight() + ySpacing;
 		}
 
 		// Recalculate the height.
 		recalculateHeight();
 	}
 	
 	// EFFECTS:  Add an element to the subcontext, setting its position appropriately.
 	public void addElement(GUIElement e) {
 		// Find the bottom y to align to. 
 		int yPos = getBottomY() + ySpacing;
 		
 		// Align the element.
 		alignElementX(e, getAlignedXAnchor());
 		e.setY(yPos);
 		
 		// Listen to it, if possible.
 		if (e instanceof GUIEventGenerator)
 			((GUIEventGenerator)e).addListener(this);
 		
 		// Add the element.
 		subcontext.addElement(e);
 		subcontext.addAndRemoveElements();
 		lastSize++;
 		recalculateHeight();
 	}
 	
 	// EFFECTS:  Add many elements to the subcontext, setting their positions appropriately.
 	public void addElements(GUIElement[] es) {
 		// Find the bottom y to align to. 
 		int yPos = getBottomY() + ySpacing;
 		
 		// Figure out the x position we're aligning to.
 		int xAnchor = getAlignedXAnchor();
 		
 		// Align the elements and listen to them.
 		for (int i = 0; i < es.length; i++) {
 			alignElementX(es[i], xAnchor);
 			es[i].setY(yPos);
 			yPos += es[i].getHeight() + ySpacing;
 			
 			if (es[i] instanceof GUIEventGenerator)
 				((GUIEventGenerator)es[i]).addListener(this);
 		}
 		
 		// Add the elements.
 		for (GUIElement e : es)
 			subcontext.addElement(e);
 		subcontext.addAndRemoveElements();
 		lastSize += es.length;
 		recalculateHeight();
 	}
 	
 	// EFFECTS:  Add an element at a specific y, shifting elements down to make room.
 	public void addElement(GUIElement e, int y) {
 		alignElementX(e, getAlignedXAnchor());
 		GUIElement above = getElementAbove(y);
		subcontext.addElement(e);
		subcontext.addAndRemoveElements();
		lastSize++;
 		if (above == null) {
 			e.setY(Integer.MIN_VALUE);
			realignAllElements();
 		}
 		else {
			if (above.getY() + above.getHeight() >= y) {
				e.setY(above.getY());
				above.setY(y);
			}
			else 
				e.setY(above.getY() + above.getHeight() + ySpacing);
			
			realignFromElement(e);
 		}
 	}
 	
 	// EFFECTS:  Remove an element from the subcontext,
 	//			 realigning the other elements appropriately.
 	public void removeElement(GUIElement e) {
 		if (subcontext.contains(e)) {
 			// Remove the element.
 			subcontext.removeElement(e);
 			subcontext.addAndRemoveElements();
 			lastSize--;
 			
 			// Realign the others.
 			GUIElement realignAnchor = getElementAbove(e);
 			if (realignAnchor == null)
 				realignAllElements();
 			else
 				realignFromElement(realignAnchor);
 		}
 	}
 	
 	// EFFECTS:  Remove several elements from the subcontext,
 	//			 realigning other elements appropriately.
 	public void removeElements(GUIElement[] es) {
 		// Remove the elements.
 		for (GUIElement e : es) {
 			subcontext.removeElement(e);
 			subcontext.addAndRemoveElements();
 			lastSize--;
 		}
 		
 		// Realign all the elements.
 		realignAllElements();
 	}
 	
 	// EFFECTS:  Clear all elements from this.
 	public void clearElements() {
 		subcontext.clear();
 		recalculateHeight();
 	}
 	
 	// EFFECTS:  Change an element's position in the list to the one at the
 	//			 given y coordinate.
 	public void moveElement(GUIElement e, int y) {
 		// If we don't contain e, return.
 		if (!contains(e))
 			return;
 		
 		// Look through the elements for the one at the given y.
 		ArrayList<GUIElement> sortedElements = new ArrayList<>(subcontext.getElements());
 		Collections.sort(sortedElements, new ElementYComparator());
 		
 		GUIElement other = null;
 		int otherIndex = -1;
 		for (int i = 0; i < sortedElements.size(); i++) {
 			GUIElement o = sortedElements.get(i);
 			if (o.getY() > y) {
 				other = o;
 				otherIndex = i;
 				break;
 			}
 		}
 		
 		// If no element was found, place the element below all the other elements.
 		if (other == null) {
 			// We know there is at least one element in the list so this is ok (because e is in the list).
 			other = sortedElements.get(sortedElements.size() - 1);
 			e.setY(other.getY() + other.getHeight() + ySpacing);
 		}
 		// If the other element was found, place e at its bottom and shift down the elements after it.
 		else {
 			e.setY(other.getY() + other.getHeight() + ySpacing);
 			int dY = e.getHeight();
 			for (int i = otherIndex + 1; i < sortedElements.size(); i++) {
 				GUIElement o = sortedElements.get(i);
 				o.setY(o.getY() + dY);
 			}
 		}
 		
 		// Realign everything.
 		realignAllElements();
 	}
 	
 	@Override
 	public void update(int delta) {
 		super.update(delta);
 		
 		// Check if any elements died.
 		if (lastSize != subcontext.getNumElements()) {
 			realignAllElements();
 			lastSize = subcontext.getNumElements();
 		}
 	}
 
 	@Override
 	public void render(Graphics g) {
 		renderSubcontext(g, x1, y1, x2, y2);
 	}
 
 	@Override
 	public void setX(int x) {
 		super.setX(x);
 		x2 = x1 + width;
 	}
 	
 	@Override
 	public void setY(int y) {
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
 		return (x >= x1 && y >= y1 && x <= x2 && y <= y2);
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
 	
 	@Override
 	public void guiEvent(GUIEvent<?> event) {
 		// Upon receiving a GUIEvent, see if its data indicates it resized.
 		Object eventData = event.getData();
 		if (eventData == null)
 			return;
 		if (eventData.equals(GUIEvent.RESIZE_EVENT)) {
 			// If it was a resize, realign from the generator.
 			GUIEventGenerator generator = event.getGenerator();
 			if (generator instanceof GUIElement)
 				realignFromElement((GUIElement)generator);
 		}
 	}
 
 	@Override
 	public void addListener(GUIEventListener l) {
 		listeners.add(l);
 	}
 
 	@Override
 	public void removeListener(GUIEventListener l) {
 		listeners.remove(l);
 	}
 }
