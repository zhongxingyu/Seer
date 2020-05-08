  package org.haferlib.slick.gui;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 
 public class ScrollableListFrame extends ScrollableFrame implements GUIEventListener {
 	
 	private ListFrame listFrame; // The list frame that handles all the listing work, because why recode it?
 	
 	// Constructors.
 	public ScrollableListFrame(int x, int y, int width, int height, int depth, int scrollBarWidth, Color scrollBarColor, byte xAlign, int xAlignOffset, int ySpacing) {
 		super(x, y, width, height, depth, scrollBarWidth, scrollBarColor);
 		
 		// Make the list frame.
 		makeListFrame(xAlign, xAlignOffset, ySpacing);
 	}
 	
 	public ScrollableListFrame(int x, int y, int width, int height, int depth, int scrollBarWidth, Color scrollBarColor) {
 		this(x, y, width, height, depth, scrollBarWidth, scrollBarColor, ListFrame.XALIGN_LEFT, 0, 0);
 	}
 
 	public ScrollableListFrame(GUIElement[] elements, int x, int y, int width, int height, int depth, int scrollBarWidth, Color scrollBarColor, byte xAlign, int xAlignOffset, int ySpacing) {
 		this(x, y, width, height, depth, scrollBarWidth, scrollBarColor, xAlign, xAlignOffset, ySpacing);
 		addElements(elements);
 	}
 	
 	public ScrollableListFrame(GUIElement[] elements, int x, int y, int width, int height, int depth, int scrollBarWidth, Color scrollBarColor) {
 		this(elements, x, y, width, height, depth, scrollBarWidth, scrollBarColor, ListFrame.XALIGN_LEFT, 0, 0);
 	}
 	
 	// Make the list frame.
 	private void makeListFrame(byte xAlign, int xAlignOffset, int ySpacing) {
 		listFrame = new ListFrame(x1, y1, width - scrollBarWidth, 0, xAlign, xAlignOffset, ySpacing);
 		listFrame.addListener(this);
 		subcontext.addElement(listFrame);
 		subcontext.addAndRemoveElements();
 	}
 	
 	// Recalculate the list height.
 	public void recalculateListHeight() {
 		listFrame.recalculateHeight();
 	}
 	
 	// Add an element at a specific y, shifting elements down to make room.
 	public void addElement(GUIElement e, int y) {
 		listFrame.addElement(e, y);
 	}
 	
 	@Override
 	public void addElement(GUIElement e) {
 		listFrame.addElement(e);
 	}
 	
 	@Override
 	public void addElements(GUIElement[] es) {
 		listFrame.addElements(es);
 	}
 	
 	@Override
 	public void removeElement(GUIElement e) {
 		listFrame.removeElement(e);
 	}
 	
 	@Override
 	public void reinitSubcontext() {
 		byte xAlign = listFrame.getXAlign();
 		int xAlignOffset = listFrame.getXAlignOffset();
 		int ySpacing = listFrame.getYSpacing();
 		super.reinitSubcontext();
 		makeListFrame(xAlign, xAlignOffset, ySpacing);
 	}
 	
 	/**
 	 * GUISubcontext has some accessors that have to be overridden to look
 	 * at listFrame rather than subcontext.
 	 * These methods are between here...
 	 */
 	
 	@Override 
 	public GUIElement getElementAbove(int y) {
 		return listFrame.getElementAbove(y);
 	}
 	
 	@Override
 	public GUIElement getElementBelow(int y) {
 		return listFrame.getElementBelow(y);
 	}
 	
 	@Override
 	public GUIElement getElementAtPoint(int x, int y) {
 		return listFrame.getElementAtPoint(x, y);
 	}
 	
 	@Override
 	public ArrayList<GUIElement> getElements() {
 		if (listFrame != null)
 			return listFrame.getElements();
 		return null;
 	}
 	
 	/**
 	 * .. and here.
 	 */
 	
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
 				recalculateScrollingFields();
 		}
 	}
 }
