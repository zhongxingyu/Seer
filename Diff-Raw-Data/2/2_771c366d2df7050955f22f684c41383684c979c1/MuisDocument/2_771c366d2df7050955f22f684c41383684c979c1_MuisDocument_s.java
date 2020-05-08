 /*
  * Created Feb 23, 2009 by Andrew Butler
  */
 package org.muis.core;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 
 import org.muis.core.event.FocusEvent;
 import org.muis.core.event.KeyBoardEvent;
 import org.muis.core.event.MouseEvent;
 import org.muis.core.event.ScrollEvent;
 import org.muis.core.style.NamedStyleGroup;
 
 import prisms.util.ArrayUtils;
 
 /** Contains all data pertaining to a MUIS application */
 public class MuisDocument implements MuisMessage.MuisMessageCenter
 {
 	/** The different policies this document can take with regards to scrolling events */
 	public static enum ScrollPolicy
 	{
 		/**
 		 * With this policy, scroll events will fire as if the event came from the mouse pointer. The position fields will represent the
 		 * position of the pointer when the scroll event was fired.
 		 */
 		MOUSE,
 		/**
 		 * With this policy, scroll events will fire as if the event came from the upper-left corner of the currently focused widget. The
 		 * position fields will represent the (0, 0) position relative to that widget.
 		 */
 		FOCUS,
 		/**
 		 * With this policy, scroll events generated from a mouse wheel will fire the same as {@link #MOUSE}, while scroll events generated
 		 * from the keyboard will fire the same as {@link #FOCUS}.
 		 */
 		MIXED;
 	}
 
 	/** Allows a MUIS document to retrieve graphics to draw itself on demand */
 	public interface GraphicsGetter
 	{
 		/** @return The graphics object that this document should use at the moment */
 		java.awt.Graphics2D getGraphics();
 	}
 
 	private final java.net.URL theLocation;
 
 	private java.awt.Toolkit theAwtToolkit;
 
 	private org.muis.core.parser.MuisParser theParser;
 
 	private MuisToolkit theCoreToolkit;
 
 	private MuisClassView theClassView;
 
 	private MuisCache theCache;
 
 	private MuisHeadSection theHead;
 
 	private BodyElement theRoot;
 
 	private ArrayList<MuisMessage> theMessages;
 
 	private MuisMessage.Type theWorstMessageType;
 
 	private NamedStyleGroup [] theDocumentGroups;
 
 	private GraphicsGetter theGraphics;
 
 	private ScrollPolicy theScrollPolicy;
 
 	private MuisElement theFocus;
 
 	private boolean hasMouse;
 
 	private int theMouseX;
 
 	private int theMouseY;
 
 	private MouseEvent.ButtonType[] thePressedButtons;
 
 	private KeyBoardEvent.KeyCode[] thePressedKeys;
 
 	private final Object theButtonsLock;
 
 	private final Object theKeysLock;
 
 	private final MuisLock.Locker theLocker;
 
 	/**
 	 * Creates a document
 	 *
 	 * @param location The location of the file that this document was generated from
 	 * @param graphics The graphics getter that this document will use for retrieving the graphics object to draw itself on demand
 	 */
 	public MuisDocument(java.net.URL location, GraphicsGetter graphics)
 	{
 		theLocation = location;
 		theHead = new MuisHeadSection();
 		theAwtToolkit = java.awt.Toolkit.getDefaultToolkit();
 		theCache = new MuisCache();
 		theMessages = new ArrayList<MuisMessage>();
 		theDocumentGroups = new NamedStyleGroup[] {new NamedStyleGroup(this, "")};
 		theGraphics = graphics;
 		theScrollPolicy = ScrollPolicy.MOUSE;
 		thePressedButtons = new MouseEvent.ButtonType[0];
 		thePressedKeys = new KeyBoardEvent.KeyCode[0];
 		theButtonsLock = new Object();
 		theKeysLock = new Object();
 		theRoot = new BodyElement();
 		theLocker = new MuisLock.Locker();
 	}
 
 	/**
 	 * @param parser The parser that created this document
 	 * @param coreToolkit The toolkit to load core MUIS classes with
 	 */
 	public void initDocument(org.muis.core.parser.MuisParser parser, MuisToolkit coreToolkit)
 	{
 		if(theParser != null)
 			throw new IllegalArgumentException("Cannot initialize a document twice");
 		theParser = parser;
 		theClassView = new MuisClassView(this);
 		theHead = new MuisHeadSection();
 		theCoreToolkit = coreToolkit;
 	}
 
 	/** @return The location of the file that this document was generated from */
 	public java.net.URL getLocation()
 	{
 		return theLocation;
 	}
 
 	/** @return The parser that created this document */
 	public org.muis.core.parser.MuisParser getParser()
 	{
 		return theParser;
 	}
 
 	/** @return The toolkit to load core MUIS classes from */
 	public MuisToolkit getCoreToolkit()
 	{
 		return theCoreToolkit;
 	}
 
 	/** @return The class map that applies to the whole document */
 	public MuisClassView getClassView()
 	{
 		return theClassView;
 	}
 
 	/** @return The resource cache for this document */
 	public MuisCache getCache()
 	{
 		return theCache;
 	}
 
 	/** @return The head section of this document */
 	public MuisHeadSection getHead()
 	{
 		return theHead;
 	}
 
 	/** @return The locker to keep track of element locks */
 	public MuisLock.Locker getLocker()
 	{
 		return theLocker;
 	}
 
 	/** @return The root element of the document */
 	public BodyElement getRoot()
 	{
 		return theRoot;
 	}
 
 	/** @return The number of named groups that exist in this document */
 	public int getGroupCount()
 	{
 		return theDocumentGroups.length;
 	}
 
 	/** @return An Iterable to iterate through this document's groups */
 	public Iterable<NamedStyleGroup> groups()
 	{
 		return new Iterable<NamedStyleGroup>() {
 			@Override
 			public java.util.Iterator<NamedStyleGroup> iterator()
 			{
 				return new GroupIterator(theDocumentGroups);
 			}
 		};
 	}
 
 	/**
 	 * @param name The name of the group to determine existence of
 	 * @return Whether a group with the given name exists in this document
 	 */
 	public boolean hasGroup(String name)
 	{
 		for(NamedStyleGroup group : theDocumentGroups)
 			if(group.getName().equals(name))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Gets a group by name, or creates one if no such group exists
 	 *
 	 * @param name The name of the group to get or create
 	 * @return The group in this document with the given name. Will never be null.
 	 */
 	public NamedStyleGroup getGroup(String name)
 	{
 		for(NamedStyleGroup group : theDocumentGroups)
 			if(group.getName().equals(name))
 				return group;
 		NamedStyleGroup ret = new NamedStyleGroup(this, name);
 		theDocumentGroups = ArrayUtils.add(theDocumentGroups, ret);
 		java.util.Arrays.sort(theDocumentGroups, new java.util.Comparator<NamedStyleGroup>() {
 			@Override
 			public int compare(NamedStyleGroup g1, NamedStyleGroup g2)
 			{
 				return g1.getName().compareToIgnoreCase(g2.getName());
 			}
 		});
 		return ret;
 	}
 
 	/**
 	 * Removes a group from a document. This will remove the group from every element that is a member of the group as well.
 	 *
 	 * @param name The name of the group to remove from this document
 	 */
 	public void removeGroup(String name)
 	{
 		if("".equals(name))
 			throw new IllegalArgumentException("Cannot remove the unnamed group from the document");
 		for(NamedStyleGroup group : theDocumentGroups)
 			if(group.getName().equals(name))
 			{
 				for(MuisElement el : group.members())
 					el.getStyle().removeGroup(group);
 				theDocumentGroups = ArrayUtils.remove(theDocumentGroups, group);
 				break;
 			}
 	}
 
 	/** Called to initialize the document after all the parsing and linking has been performed */
 	public void postCreate()
 	{
 		theRoot.postCreate();
 	}
 
 	/**
 	 * Records a message in this document
 	 *
 	 * @param type The type of the message
 	 * @param text The text of the message
 	 * @param exception The exception which may have caused the message
 	 * @param params Any parameters relevant to the message
 	 */
 	@Override
 	public void message(MuisMessage.Type type, String text, Throwable exception, Object... params)
 	{
 		MuisMessage message = new MuisMessage(this, type, theRoot.life().getStage(), text, exception, params);
 		theMessages.add(message);
 		if(theWorstMessageType == null || type.compareTo(theWorstMessageType) > 0)
 			theWorstMessageType = type;
 	}
 
 	@Override
 	public final void fatal(String message, Throwable exception, Object... params)
 	{
 		message(MuisMessage.Type.FATAL, message, exception, params);
 	}
 
 	@Override
 	public final void error(String message, Throwable exception, Object... params)
 	{
 		message(MuisMessage.Type.ERROR, message, exception, params);
 	}
 
 	@Override
 	public final void warn(String message, Object... params)
 	{
 		message(MuisMessage.Type.WARNING, message, null, params);
 	}
 
 	@Override
 	public final void warn(String message, Throwable exception, Object... params)
 	{
 		message(MuisMessage.Type.WARNING, message, exception, params);
 	}
 
 	/** @param message The message to remove from this element */
 	public void removeMessage(MuisMessage message)
 	{
 		if(!theMessages.remove(message))
 			return;
 		reEvalWorstMessage();
 	}
 
 	private void reEvalWorstMessage()
 	{
 		MuisMessage.Type type = null;
 		for(MuisMessage message : theMessages)
 			if(type == null || message.type.compareTo(type) > 0)
 				type = message.type;
 		if(theWorstMessageType == null ? type != null : theWorstMessageType != type)
 			theWorstMessageType = type;
 	}
 
 	/** @return The worst type of message associated with the MUIS document */
 	@Override
 	public MuisMessage.Type getWorstMessageType()
 	{
 		if(theWorstMessageType == null)
 			return theRoot.getWorstMessageType();
 		if(theWorstMessageType.compareTo(theRoot.getWorstMessageType()) > 0)
 			return theWorstMessageType;
 		return theRoot.getWorstMessageType();
 	}
 
 	/** @return All messages attached to this element or its descendants */
 	@Override
 	public final MuisMessage [] getAllMessages()
 	{
 		ArrayList<MuisMessage> ret = new ArrayList<>();
 		ret.addAll(theMessages);
 		if(theRoot != null)
 			for(MuisMessage msg : theRoot.getAllMessages())
 				ret.add(msg);
 		return ret.toArray(new MuisMessage[ret.size()]);
 	}
 
 	/** @return The policy that this document uses to dispatch scroll events */
 	public ScrollPolicy getScrollPolicy()
 	{
 		return theScrollPolicy;
 	}
 
 	/** @param policy The policy that this document should use to dispatch scroll events */
 	public void setScrollPolicy(ScrollPolicy policy)
 	{
 		theScrollPolicy = policy;
 	}
 
 	/** @return The graphics that this document should use to render itself */
 	public java.awt.Graphics2D getGraphics()
 	{
 		return theGraphics.getGraphics();
 	}
 
 	/**
 	 * Sets the size that this document can render its content in
 	 *
 	 * @param width The width of the document size
 	 * @param height The height of the document size
 	 */
 	public void setSize(int width, int height)
 	{
		MuisEventQueue.get().scheduleEvent(new MuisEventQueue.ReboundEvent(theRoot, new java.awt.Rectangle(0, 0, width, height)), false);
 	}
 
 	/**
 	 * Renders this MUIS document in a graphics context
 	 *
 	 * @param graphics The graphics context to render in
 	 */
 	public void paint(java.awt.Graphics2D graphics)
 	{
 		theRoot.paint(graphics, null);
 	}
 
 	/** @return Whether the mouse is over this document */
 	public boolean hasMouse()
 	{
 		return hasMouse;
 	}
 
 	/**
 	 * @return The x-coordinate of either the mouse's current position relative to the document (if {@link #hasMouse()} is true) or the
 	 *         mouse's position where it exited the document (if {@link #hasMouse()} is false).
 	 */
 	public int getMouseX()
 	{
 		return theMouseX;
 	}
 
 	/**
 	 * @return The y-coordinate of either the mouse's current position relative to the document (if {@link #hasMouse()} is true) or the
 	 *         mouse's position where it exited the document (if {@link #hasMouse()} is false).
 	 */
 	public int getMouseY()
 	{
 		return getMouseY();
 	}
 
 	/**
 	 * @param button The mouse button to check
 	 * @return Whether the mouse button is currently pressed
 	 */
 	public boolean isButtonPressed(MouseEvent.ButtonType button)
 	{
 		return ArrayUtils.contains(thePressedButtons, button);
 	}
 
 	/** @return All mouse buttons that are currently pressed */
 	public MouseEvent.ButtonType[] getPressedButtons()
 	{
 		return thePressedButtons;
 	}
 
 	/**
 	 * @param code The key code to check
 	 * @return Whether the key with the given code is currently pressed
 	 */
 	public boolean isKeyPressed(KeyBoardEvent.KeyCode code)
 	{
 		return ArrayUtils.contains(thePressedKeys, code);
 	}
 
 	/** @return All key codes whose keys are currently pressed */
 	public KeyBoardEvent.KeyCode[] getPressedKeys()
 	{
 		return thePressedKeys;
 	}
 
 	/** @return Whether a shift button is currently pressed */
 	public boolean isShiftPressed()
 	{
 		return isKeyPressed(KeyBoardEvent.KeyCode.SHIFT_LEFT) || isKeyPressed(KeyBoardEvent.KeyCode.SHIFT_RIGHT);
 	}
 
 	/** @return Whether a control button is currently pressed */
 	public boolean isControlPressed()
 	{
 		return isKeyPressed(KeyBoardEvent.KeyCode.CTRL_LEFT) || isKeyPressed(KeyBoardEvent.KeyCode.CTRL_RIGHT);
 	}
 
 	/** @return Whether an alt button is currently pressed */
 	public boolean isAltPressed()
 	{
 		return isKeyPressed(KeyBoardEvent.KeyCode.ALT_LEFT) || isKeyPressed(KeyBoardEvent.KeyCode.ALT_RIGHT);
 	}
 
 	/** @return Whether caps lock is toggled on at the moment */
 	public boolean isCapsLocked()
 	{
 		return theAwtToolkit.getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
 	}
 
 	/** @return Whether num lock is toggled on at the moment */
 	public boolean isNumLocked()
 	{
 		return theAwtToolkit.getLockingKeyState(java.awt.event.KeyEvent.VK_NUM_LOCK);
 	}
 
 	/** @return Whether scroll lock is toggled on at the moment */
 	public boolean isScrollLocked()
 	{
 		return theAwtToolkit.getLockingKeyState(java.awt.event.KeyEvent.VK_SCROLL_LOCK);
 	}
 
 	/** @return Whether kana lock is toggled on at the moment (for Japanese keyboard layout) */
 	public boolean isKanaLocked()
 	{
 		return theAwtToolkit.getLockingKeyState(java.awt.event.KeyEvent.VK_KANA_LOCK);
 	}
 
 	/**
 	 * Emulates a mouse event on the document
 	 *
 	 * @param x The x-coordinate where the event occurred
 	 * @param y The y-coordinate where the event occurred
 	 * @param type The type of the event
 	 * @param buttonType The button that caused the event
 	 * @param clickCount The click count for the event
 	 */
 	public void mouse(int x, int y, MouseEvent.MouseEventType type, MouseEvent.ButtonType buttonType, int clickCount)
 	{
 		boolean oldHasMouse = hasMouse;
 		int oldX = theMouseX;
 		int oldY = theMouseY;
 		hasMouse = type != MouseEvent.MouseEventType.exited;
 		theMouseX = x;
 		theMouseY = y;
 		MuisElementCapture newCapture = MuisUtils.captureEventTargets(theRoot, x, y);
 		MuisElementCapture oldCapture = MuisUtils.captureEventTargets(theRoot, oldX, oldY);
 		MouseEvent evt = new MouseEvent(this, newCapture.getTarget().element, type, x, y, buttonType, clickCount, newCapture);
 
 		ArrayList<MuisEventQueue.Event> events = new ArrayList<>();
 
 		switch (type)
 		{
 		case moved:
 			// This means it moved within the document. We have to determine any elements that it might have exited or entered.
 			if(oldHasMouse)
 				mouseMove(oldCapture, newCapture, events);
 			else
 			{
 				evt = new MouseEvent(this, newCapture.getTarget().element, MouseEvent.MouseEventType.entered, x, y, buttonType, clickCount,
 					newCapture);
 				events.add(new MuisEventQueue.PositionQueueEvent(theRoot, evt, true));
 			}
 			break;
 		case pressed:
 			synchronized(theButtonsLock)
 			{
 				if(!ArrayUtils.contains(thePressedButtons, buttonType))
 					thePressedButtons = ArrayUtils.add(thePressedButtons, buttonType);
 			}
 			focusByMouse(newCapture, events);
 			events.add(new MuisEventQueue.PositionQueueEvent(theRoot, evt, false));
 			break;
 		case released:
 			synchronized(theButtonsLock)
 			{
 				if(ArrayUtils.contains(thePressedButtons, buttonType))
 					thePressedButtons = ArrayUtils.remove(thePressedButtons, buttonType);
 			}
 			events.add(new MuisEventQueue.PositionQueueEvent(theRoot, evt, false));
 			break;
 		case clicked:
 		case exited:
 			events.add(new MuisEventQueue.PositionQueueEvent(theRoot, evt, false));
 			break;
 		case entered:
 			events.add(new MuisEventQueue.PositionQueueEvent(theRoot, evt, true));
 			break;
 		}
 		for(MuisEventQueue.Event event : events)
 			MuisEventQueue.get().scheduleEvent(event, true);
 	}
 
 	/** Checks the mouse's current position, firing necessary mouse events if it has moved relative to any elements */
 	private void mouseMove(MuisElementCapture oldCapture, MuisElementCapture newCapture, java.util.List<MuisEventQueue.Event> events)
 	{
 		LinkedHashSet<MuisElementCapture> oldSet = new LinkedHashSet<>();
 		LinkedHashSet<MuisElementCapture> newSet = new LinkedHashSet<>();
 		LinkedHashSet<MuisElementCapture> common = new LinkedHashSet<>();
 		for(MuisElementCapture mec : oldCapture)
 			if(newSet.contains(mec))
 				common.add(mec);
 			else
 				oldSet.add(mec);
 		for(MuisElementCapture mec : newCapture)
 			if(!common.contains(mec))
 				newSet.add(mec);
 		// Remove child elements
 		java.util.Iterator<MuisElementCapture> iter = oldSet.iterator();
 		while(iter.hasNext())
 		{
 			MuisElementCapture mec = iter.next();
 			if(oldSet.contains(mec.parent))
 				iter.remove();
 		}
 		iter = newSet.iterator();
 		while(iter.hasNext())
 		{
 			MuisElementCapture mec = iter.next();
 			if(newSet.contains(mec.parent))
 				iter.remove();
 		}
 		// Fire exit events
 		for(MuisElementCapture mec : oldSet)
 		{
 			MouseEvent exit = new MouseEvent(this, mec.getTarget().element, MouseEvent.MouseEventType.exited, theMouseX, theMouseY, null,
 				0, mec);
 			events.add(new MuisEventQueue.PositionQueueEvent(exit.getElement(), exit, false));
 		}
 		for(MuisElementCapture mec : common)
 		{
 			MouseEvent move = new MouseEvent(this, mec.getTarget().element, MouseEvent.MouseEventType.moved, theMouseX, theMouseY, null, 0,
 				mec);
 			events.add(new MuisEventQueue.PositionQueueEvent(move.getElement(), move, false));
 		}
 		// Fire enter events
 		for(MuisElementCapture mec : newSet)
 		{
 			MouseEvent enter = new MouseEvent(this, mec.getTarget().element, MouseEvent.MouseEventType.entered, theMouseX, theMouseY, null,
 				0, mec);
 			MuisEventQueue.get().scheduleEvent(new MuisEventQueue.PositionQueueEvent(enter.getElement(), enter, false), true);
 		}
 	}
 
 	private void focusByMouse(MuisElementCapture capture, java.util.List<MuisEventQueue.Event> events)
 	{
 		for(MuisElementCapture mec : capture)
 			if(mec.element.isFocusable())
 			{
 				setFocus(mec.element);
 				return;
 			}
 	}
 
 	/**
 	 * Sets the document's focused element. This method does not invoke {@link MuisElement#isFocusable()}, so this will work on any element.
 	 *
 	 * @param toFocus The element to give the focus to
 	 */
 	public void setFocus(MuisElement toFocus)
 	{
 		ArrayList<MuisEventQueue.Event> events = new ArrayList<>();
 		setFocus(toFocus, events);
 		for(MuisEventQueue.Event event : events)
 			MuisEventQueue.get().scheduleEvent(event, true);
 	}
 
 	private void setFocus(MuisElement focus, java.util.List<MuisEventQueue.Event> events)
 	{
 		MuisElement oldFocus = theFocus;
 		theFocus = focus;
 		if(oldFocus != null)
 			events.add(new MuisEventQueue.UserQueueEvent(new FocusEvent(this, oldFocus, false), false));
 		if(theFocus != null)
 			events.add(new MuisEventQueue.UserQueueEvent(new FocusEvent(this, theFocus, true), false));
 	}
 
 	/** Moves this document's focus to the focusable widget previous to the currently focused widget */
 	public void backupFocus()
 	{
 		if(theFocus == null)
 			return;
 		if(searchFocus(theFocus, false))
 			return;
 		/* If we get here, then there was no previous focusable element. We must wrap around to the last focusable element. */
 		MuisElement deepest = getDeepestElement(theRoot, false);
 		searchFocus(deepest, false);
 	}
 
 	/** Moves this document's focus to the focusable widget after the currently focused widget */
 	public void advanceFocus()
 	{
 		if(theFocus == null)
 			return;
 		if(searchFocus(theFocus, true))
 			return;
 		/* If we get here, then there was no previous focusable element. We must wrap around to the last focusable element. */
 		MuisElement deepest = getDeepestElement(theRoot, true);
 		searchFocus(deepest, true);
 	}
 
 	boolean searchFocus(MuisElement el, boolean forward)
 	{
 		MuisElement lastChild = theFocus;
 		MuisElement parent = theFocus.getParent();
 		while(parent != null)
 		{
 			MuisElement [] children = parent.getChildren();
 			MuisElement [] forLoop = MuisElement.sortByZ(children);
 			if(!forward)
 			{
 				if(forLoop == children)
 					forLoop = forLoop.clone();
 				ArrayUtils.reverse(forLoop);
 			}
 			boolean foundLastChild = false;
 			for(int c = 0; c < children.length; c++)
 				if(foundLastChild)
 				{
 					MuisElement deepest = getDeepestElement(children[c], forward);
 					if(deepest != children[c])
 					{
 						// Do searchFocus from this deep element
 						parent = deepest;
 						break;
 					}
 					else if(children[c].isFocusable())
 					{
 						setFocus(children[c]);
 						return true;
 					}
 				}
 				else if(children[c] == lastChild)
 					foundLastChild = true;
 			if(parent.isFocusable())
 			{
 				setFocus(parent);
 				return true;
 			}
 			lastChild = parent;
 			parent = parent.getParent();
 		}
 		return false;
 	}
 
 	static MuisElement getDeepestElement(MuisElement root, boolean first)
 	{
 		while(root.getChildCount() > 0)
 			if(first)
 				root = root.getChild(0);
 			else
 				root = root.getChild(root.getChildCount() - 1);
 		return root;
 	}
 
 	/**
 	 * Emulates a scroll event on the document
 	 *
 	 * @param x The x-coordinate where the event occurred
 	 * @param y The y-coordinate where the event occurred
 	 * @param amount The amount that the mouse wheel was scrolled
 	 */
 	public void scroll(int x, int y, int amount)
 	{
 		ScrollEvent evt = null;
 		MuisElement element = null;
 		switch (theScrollPolicy)
 		{
 		case MOUSE:
 		case MIXED:
 			MuisElementCapture capture = MuisUtils.captureEventTargets(theRoot, x, y);
 			evt = new ScrollEvent(this, element, x, y, ScrollEvent.ScrollType.UNIT, true, amount, null, capture);
 			break;
 		case FOCUS:
 			element = theFocus;
 			if(element == null)
 				element = theRoot;
 			evt = new ScrollEvent(this, element, -1, -1, ScrollEvent.ScrollType.UNIT, true, amount, null, null);
 			break;
 		}
 		MuisEventQueue.get().scheduleEvent(new MuisEventQueue.PositionQueueEvent(theRoot, evt, false), true);
 	}
 
 	/**
 	 * Emulates a key event on this document
 	 *
 	 * @param code The key code of the event
 	 * @param pressed Whether the key was pressed or released
 	 */
 	public void keyed(KeyBoardEvent.KeyCode code, boolean pressed)
 	{
 		org.muis.core.event.KeyBoardEvent evt = null;
 		if(theFocus != null)
 			evt = new KeyBoardEvent(this, theFocus, code, pressed);
 		else
 			evt = new KeyBoardEvent(this, theRoot, code, pressed);
 
 		if(theFocus != null)
 			theFocus.fireUserEvent(evt);
 		synchronized(theKeysLock)
 		{
 			if(pressed)
 			{
 				if(!ArrayUtils.contains(thePressedKeys, code))
 					thePressedKeys = ArrayUtils.add(thePressedKeys, code);
 			}
 			else if(ArrayUtils.contains(thePressedKeys, code))
 				thePressedKeys = ArrayUtils.remove(thePressedKeys, code);
 		}
 		MuisElementCapture capture = null;
 		if(!evt.isCanceled())
 		{
 			MuisElement scrollElement = null;
 			int x = 0, y = 0;
 			switch (theScrollPolicy)
 			{
 			case MOUSE:
 				if(hasMouse)
 				{
 					capture = MuisUtils.captureEventTargets(theRoot, theMouseX, theMouseY);
 					scrollElement = capture.getTarget().element;
 					x = theMouseX;
 					y = theMouseY;
 				}
 				else
 					scrollElement = null;
 				break;
 			case FOCUS:
 			case MIXED:
 				if(theFocus != null)
 					scrollElement = theFocus;
 				else
 					scrollElement = theRoot;
 				x = -1;
 				y = -1;
 				break;
 			}
 			if(scrollElement != null)
 			{
 				ScrollEvent.ScrollType scrollType = null;
 				boolean vertical = true, downOrRight = true;
 				switch (code)
 				{
 				case LEFT_ARROW:
 					scrollType = ScrollEvent.ScrollType.UNIT;
 					vertical = false;
 					downOrRight = false;
 					break;
 				case RIGHT_ARROW:
 					scrollType = ScrollEvent.ScrollType.UNIT;
 					vertical = false;
 					downOrRight = true;
 					break;
 				case UP_ARROW:
 					scrollType = ScrollEvent.ScrollType.UNIT;
 					vertical = true;
 					downOrRight = false;
 					break;
 				case DOWN_ARROW:
 					scrollType = ScrollEvent.ScrollType.UNIT;
 					vertical = true;
 					downOrRight = true;
 					break;
 				case PAGE_UP:
 					scrollType = ScrollEvent.ScrollType.BLOCK;
 					vertical = true;
 					downOrRight = false;
 					break;
 				case PAGE_DOWN:
 					scrollType = ScrollEvent.ScrollType.BLOCK;
 					vertical = true;
 					downOrRight = true;
 					break;
 				default:
 					scrollType = null;
 				}
 				if(scrollType != null)
 				{
 					ScrollEvent scrollEvt = new ScrollEvent(this, scrollElement, x, y, scrollType, vertical, downOrRight ? 1 : -1, evt,
 						capture);
 					if(capture != null)
 						MuisEventQueue.get().scheduleEvent(new MuisEventQueue.PositionQueueEvent(scrollElement, scrollEvt, false), true);
 					else
 						MuisEventQueue.get().scheduleEvent(new MuisEventQueue.UserQueueEvent(scrollEvt, false), true);
 				}
 			}
 
 			if(code == KeyBoardEvent.KeyCode.TAB)
 				if(isShiftPressed())
 					backupFocus();
 				else
 					advanceFocus();
 		}
 	}
 
 	/**
 	 * Emulates textual input to the document
 	 *
 	 * @param c The character that was input
 	 */
 	public void character(char c)
 	{
 		org.muis.core.event.CharInputEvent evt = null;
 		if(theFocus != null)
 			evt = new org.muis.core.event.CharInputEvent(this, theFocus, c);
 		else
 			evt = new org.muis.core.event.CharInputEvent(this, theRoot, c);
 		MuisEventQueue.get().scheduleEvent(new MuisEventQueue.UserQueueEvent(evt, false), true);
 	}
 
 	private static class GroupIterator implements java.util.Iterator<NamedStyleGroup>
 	{
 		private final NamedStyleGroup [] theGroups;
 
 		private int theIndex;
 
 		GroupIterator(NamedStyleGroup [] groups)
 		{
 			theGroups = groups;
 		}
 
 		@Override
 		public boolean hasNext()
 		{
 			return theIndex < theGroups.length;
 		}
 
 		@Override
 		public NamedStyleGroup next()
 		{
 			NamedStyleGroup ret = theGroups[theIndex];
 			theIndex++;
 			return ret;
 		}
 
 		@Override
 		public void remove()
 		{
 			throw new UnsupportedOperationException("Document's group iterator does not support modification");
 		}
 	}
 }
