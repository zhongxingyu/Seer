 package game.util.UI;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.LinkedList;
 
 import game.util.IO.InputState;
 import game.util.IO.KeyState;
 import game.util.IO.MouseButton;
 import game.util.IO.Event.EventListner;
 import game.util.IO.Event.KeyEvent;
 import game.util.IO.Event.MouseButtonEvent;
 import game.util.IO.Event.MouseEvent;
 import game.util.IO.Event.NullEvent;
 
 import org.newdawn.slick.GameContainer;
 
 public abstract class BasicUIComponent implements UIComponent {
 	
 	private boolean ignoreFocus = false;
 	public boolean getIgnoreFocus() { return ignoreFocus; }
 	public void setIgnoreFocus(boolean ignoreFocus) { this.ignoreFocus = ignoreFocus; }
 	private boolean locksFocus = false;
 	public boolean getLocksFocus() { return locksFocus; }
 	public void setLocksFocus(boolean locksFocus) { this.locksFocus = locksFocus; }
 
 	private static BasicUIComponent lastFocus = null;
 	private static BasicUIComponent currentFocus = null;
 	private static boolean focusChanged = false;
 	private static boolean lockFocus = false;
 
 	private int layer;
 	public int getLayer() { return layer; }
 	public void setLayer(int layer) { this.layer = layer; }
 
 	private BasicUIComponent parent;
 	public BasicUIComponent getParent() { return parent; }
 	private void setParent(BasicUIComponent parent) { this.parent = parent; }
 	private LinkedList<BasicUIComponent> children;
 	
 	public void addChild(BasicUIComponent child) {
 		addChild(child,0);
 	}
 	public void addChild(BasicUIComponent child, int layer) {
 		if (children == null)
 			children = new LinkedList<>();
 		children.add(child);
 		child.setParent(this);
 		Collections.sort(children, new Comparator<BasicUIComponent>() {
 			@Override
 			public int compare(BasicUIComponent arg0, BasicUIComponent arg1) {
 				return Integer.compare(arg0.getLayer(), arg1.getLayer());
 			}
 		});
 	}
 	public void removeChild(BasicUIComponent child) {
 		children.remove(child);
 	}
 	
 	private Point location = new Point();
 	public Point getLocation() { return location; }
 	public void setLocation(Point location) { this.location = new Point(location); }
 	private Dimension dimension = new Dimension();
 	public Dimension getDimension() { return dimension; }
 	public void setDimension(Dimension dimension) { this.dimension = new Dimension(dimension); }
 	public Rectangle getRectangle() { return new Rectangle(location, dimension); }
 	
 	private boolean rightB, leftB, middleB;
 	
 	private boolean enabled = true;
 	public boolean isEnabled() { return enabled; }
 	public void Enable() { enabled = true; }
 	public void Disable() { enabled = false; }
 	
 	public void removeFocus() { 
 		if (lostFocus != null)
 			lostFocus.Invoke(this, new NullEvent());
 	}
 	
 	@Override
 	public void update(GameContainer container) {
 		if (!enabled)
 			return;
 		InputState last = null;
 		InputState current = null;
 		try {
 			last = InputState.GetLast();
 			current = InputState.Get();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		Rectangle r = this.getRectangle();
 		Point lp = last.MouseState.getPosition(),
 			  cp = current.MouseState.getPosition();
 		boolean lr = last.MouseState.isRightButtonDown(),
 				cr = current.MouseState.isRightButtonDown(),
 				ll = last.MouseState.isLeftButtonDown(),
 				cl = current.MouseState.isLeftButtonDown(),
 				lm = last.MouseState.isMiddleButtonDown(),
 				cm = current.MouseState.isMiddleButtonDown();
 		
 		if (mouseEnter != null) {
 			if (r.contains(cp) && !r.contains(lp))
 				mouseEnter.Invoke(this, new MouseEvent(cp));
 		}
 		if (mouseOver != null) {
 			if (r.contains(cp))
 				mouseOver.Invoke(this, new MouseEvent(cp));
 		}
 		if (mouseLeave != null) {
 			if (r.contains(lp) && !r.contains(cp))
				mouseEnter.Invoke(this, new MouseEvent(cp));
 		}
 		rightB &= cr;
 		leftB &= cl;
 		middleB &= cm;
 		boolean resetFocusChanged = focusChanged;
 		if (r.contains(cp)) {
 			if (mouseDown != null) {
 				if (!lr && cr) {
 					gainFocus();
 					mouseDown.Invoke(this, new MouseButtonEvent(MouseButton.RIGHT));
 					rightB = true;
 				}
 				if (!ll && cl) {
 					gainFocus();
 					mouseDown.Invoke(this, new MouseButtonEvent(MouseButton.LEFT));
 					leftB = true;
 				}
 				if (!lm && cm) {
 					gainFocus();
 					mouseDown.Invoke(this, new MouseButtonEvent(MouseButton.MIDDLE));
 					middleB = true;
 				}
 			}
 			if (mouseUp != null) {
 				if (!cr && lr) {
 					mouseUp.Invoke(this, new MouseButtonEvent(MouseButton.RIGHT));
 					if (mouseClick != null) {
 						if (rightB)
 							mouseClick.Invoke(this, new MouseButtonEvent(MouseButton.RIGHT));
 					}
 					rightB = false;
 				}
 				if (!cl && ll) {
 					mouseUp.Invoke(this, new MouseButtonEvent(MouseButton.LEFT));
 					if (mouseClick != null) {
 						if (leftB)
 							mouseClick.Invoke(this, new MouseButtonEvent(MouseButton.LEFT));
 					}
 					leftB = false;
 				}
 				if (!cm && lm) {
 					mouseUp.Invoke(this, new MouseButtonEvent(MouseButton.MIDDLE));
 					if (mouseClick != null) {
 						if (middleB)
 							mouseClick.Invoke(this, new MouseButtonEvent(MouseButton.MIDDLE));
 					}
 					middleB = false;
 				}
 				if (mouseMultiClick != null) {
 					try {
 						throw new Exception("Not yet implemented");
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		}
		for (BasicUIComponent child : children) {
			child.update(container);
		}
 		focusChanged = resetFocusChanged;
 		
 		boolean focused = (currentFocus == this);
 		if (gainedFocus != null) {
 			if (focused && (lastFocus != this)) {
 				gainedFocus.Invoke(this, new NullEvent());
 				lockFocus = locksFocus;
 			}
 		}
 		
 		if (focused || (ignoreFocus && !lockFocus)) {
 			Enumeration<KeyState> en = current.KeyboardState.GetKeyStates();
 			while (en.hasMoreElements()) {
 				KeyState ck = en.nextElement();
 				KeyState lk = last.KeyboardState.GetKeyState(ck.key);
 				if (keyDown != null) {
 					if (ck.Down() && !lk.Down()) {
 						keyDown.Invoke(this, new KeyEvent(ck.key));
 					}
 				}
 				if (keyPress != null) {
 					if (ck.Down()) {
 						keyPress.Invoke(this, new KeyEvent(ck.key));
 					}
 				}
 				if (keyUp != null) {
 					if (lk.Down() && !ck.Down()) {
 						keyUp.Invoke(this, new KeyEvent(ck.key));
 					}
 				}
 			}
 		}
 		if (updated != null)
 			updated.Invoke(this, new NullEvent());
 	}
 	
 	private void gainFocus() {
 		if (focusChanged) {
 			currentFocus = this;
 		} else {
 			lastFocus = currentFocus;
			lastFocus.removeFocus();
 			currentFocus = this;
 			focusChanged = true;
 		}
 		
 	}
 	private EventListner mouseEnter, mouseLeave, mouseOver, mouseClick, mouseMultiClick, mouseUp, mouseDown;
 	
 	public void addMouseEnterEventListner(EventListner delegate) {
 			if (mouseEnter == null)
 				mouseEnter = delegate;
 	}
 	public void addMouseLeaveEventListner(EventListner delegate) {
 		if (mouseLeave == null)
 			mouseLeave = delegate;
 	}
 	public void addMouseOverEventListner(EventListner delegate) {
 		if (mouseOver == null)
 			mouseOver = delegate;
 	}
 	public void addMouseMultiClickEventListner(EventListner delegate) {
 		if (mouseMultiClick == null)
 			mouseMultiClick = delegate;
 	}
 	public void addMouseClickEventListner(EventListner delegate) {
 		if (mouseClick == null)
 			mouseClick = delegate;
 	}
 	public void addMouseUpEventListner(EventListner delegate) {
 		if (mouseUp == null)
 			mouseUp = delegate;
 	}
 	public void addMouseDownEventListner(EventListner delegate) {
 		if (mouseDown == null)
 			mouseDown = delegate;
 	}
 
 	
 	private EventListner updated, rendered;
 	
 	public void addUpdateEventListner(EventListner delegate) {
 		if (updated == null)
 			updated = delegate;
 	}	
 	public void addRenderEventListner(EventListner delegate) {
 		if (rendered == null)
 			rendered = delegate;
 	}
 	
 	private EventListner lostFocus, gainedFocus;
 	
 	public void addLostFocusEventListner(EventListner delegate) {
 		if (lostFocus == null)
 			lostFocus = delegate;
 	}	
 	public void addGainedFocusEventListner(EventListner delegate) {
 		if (gainedFocus == null)
 			gainedFocus = delegate;
 	}
 	
 	
 	private EventListner keyDown, keyUp, keyPress;
 	
 	public void addKeyDownEventListner(EventListner delegate) {
 		if (keyDown == null)
 			keyDown = delegate;
 	}	
 	public void addKeyUpEventListner(EventListner delegate) {
 		if (keyUp == null)
 			keyUp = delegate;
 	}	
 	public void addKeyPressEventListner(EventListner delegate) {
 		if (keyPress == null)
 			keyPress = delegate;
 	}
 }
