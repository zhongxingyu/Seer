 package org.apache.pivot.wtk;
 
 import java.awt.AWTEvent;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.AWTEventListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowStateListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JWindow;
 import javax.swing.SwingUtilities;
 
 import org.apache.pivot.wtk.DesktopFrame.DragListener;
 import org.apache.pivot.wtk.Window.WindowPopupListener;
 import org.apache.pivot.wtk.skin.PanelSkin;
 
 /**
  * <pre>
  * 
  * 
  *     -------------------------- o
  *     |  o                      |  o  	   <---- Apache Pivot Component residing in the Pivot host component.
  *     |       o                 |     o
  *     |            o  -------------------
  *     |               |                 | 
  *     |               |                 |
  *     |               |                 | <---- Awt Frame delegate which receives size information
  *     |               |                 |		 and calculates location information translated to screen
  *     |               |                 |		 coordinates from messages delegated from the Apache Pivot
  *     |               |                 |       component.
  *     |               |                 |
  *     |               |                 |
  *     |               |                 |         
  *     ----------------|                 |
  *       o             |                 |
  *             o       |                 |
  *                   o -------------------
  * 
  * 
  * 
  * Apache Pivot container which resides within the layout of the parent host.
  * This container is never seen due to the delegate Swing JWindow which covers
  * it, maintaining size and position information delegated to it from the 
  * underlying Pivot container. 
  * 
  * Because the underlying ui upon receiving focus will come to the front
  * of the delegate window, a low-level mouse listener is added so that the
  * delegate window will always remain in front therefore making it appear
  * as if the Swing component is actually inside the Apache container.
  * 
  * When the window containing the parent ui is moved, there will be a delay
  * within which the delegate window will not move. Once the parent window has
  * stopped moving briefly, the delegate window will update its position (or size
  * in the case of resizing) to the "delegator's" position (or size).
  * 
  * One approach to dealing with this drawback is to fork the code providing
  * an "undecorated frame" which subclasses {@link org.apache.pivot.wtk.DesktopApplicationContext.HostFrame}.
  * The next step is to paint your own titlebar and add {@link MouseListener} to it 
  * which can receive drag events and update the position of the frame (also causing
  * the delegate window to update ongoingly and thus keeping the ui positioning 
  * in sync). see {@link org.apache.pivot.wtk.DesktopFrame} for details
  * 
  * 
  * @author David Ray
  * @see org.apache.pivot.wtk.DesktopFrame
  */
 public class SwingContainer extends Container implements WindowPopupListener {
 	private Skin skin;
 	private Delegate delegate;
 	Display display;
 	private java.awt.Window topLevelWindow;
 	private java.awt.Insets insets;
 	private javax.swing.Timer visibilityTimer;
 
 	private DragListener dragListener;
 	private WindowStateListener windowStateListener;
 	private TitleBar.IconizeListener iconizeListener;
 	private ComponentAdapter componentAdapter;
 	private AWTEventListener awtListener;
 
 	private java.awt.Component occupant;
 	
 	private BufferedImage background;
 	private boolean paintingBackground;
 	private boolean clientSetVisible;
 	private boolean isPopupTransition;
 	private boolean isDragging;
 	private boolean isDialogTransition;
 	
 	private int windowsOpened = 0;
 
 	private List<WindowDisposeListener> windowDisposeListeners;
 
 	/**
 	 * Creates a new {@code SwingContainer}
 	 * 
 	 * @param parent  the immediate parent container
 	 */
 	public SwingContainer() {
 		installSkin(ImageView.class);
 		
 		// Keeps the delegate window in front.
 		Toolkit.getDefaultToolkit().addAWTEventListener(awtListener = new AWTEventListener() {
 			@Override
 			public void eventDispatched(AWTEvent event) {
 				 if(event.getID() == MouseEvent.MOUSE_MOVED) {
 					if(!isPopupTransition && !isDialogTransition && occupant != null && occupant.getBounds().width != 0) {
						Point containerLoc = SwingContainer.this.getLocationOnScreen();
						Point p = ((MouseEvent)event).getLocationOnScreen();
						p.x -= (containerLoc.x + 5);
						p.y -= (containerLoc.y + 5);
						Rectangle r = occupant.getBounds();
						if(paintingBackground && r.contains(p)) {
							paintingBackground = false;
							delegate.setVisible(true);
 						}
 					}
 				}else if(event.getID() == WindowEvent.WINDOW_CLOSING) {
 					if(((WindowEvent)event).getWindow() == topLevelWindow) {
 						isPopupTransition = true;
 						if(clientSetVisible) {
 							setContentAsBackground();
 						}
 					}
 				}
 			}
 		}, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
 
 		// Delegate(java.awt.Window) must be instantiated with the top level window
 		// owner in order to receive focus. Therefore, we must wait until the top
 		// level window becomes available
 		(new Thread() {
 			public void run() {
 				while (getParent() == null
 						|| (display = getParent().getDisplay()) == null) {
 					try {
 						Thread.sleep(2000);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 
 				topLevelWindow = display.getHostWindow();
 
 				// Delegate(java.awt.Window) must be instantiated with the top level window
 				// owner in order to receive focus. Therefore, we must wait until the top
 				// level window becomes available before instantiating the delegate(window).
 				// In addition all calls to mutate the delegate before it is instantiated -
 				// must also be delayed.
 				delegate = new Delegate(topLevelWindow);
 				
 				if(!SwingContainer.this.clientSetVisible) {
 					SwingContainer.this.clientSetVisible(false);
 				}
 				
 				//Notification of tooltip/menu to dismiss SwingContainer so
                 //popup windows aren't covered by the SwingContainer Delegate
 				Window.getWindowPopupListeners().add(SwingContainer.this);
 				
 				if (occupant != null) {
 					processComponentAdd(occupant);
 				}
 
 				getParent().getContainerListeners().add(createContainerListener());
 
 				if (topLevelWindow instanceof DesktopFrame) {
 					((DesktopFrame) topLevelWindow).addDragListener(dragListener = new DragListener() {
 						public void windowDragged(Point oldLoc, Point newLoc) {
 							delegate.setVisible(false);
 							isDragging = true;
 						}
 
 						public void draggingStopped() {
 							isDragging = false;
 							Point p = getLocationOnScreen();
 							moveWindow(p.x, p.y);
 							if(clientSetVisible) {
 								delegate.setVisible(true);
 							}
 						}
 					});
 					//Sets the Delegate visibility for iconify/deiconify events.
 					((DesktopFrame) topLevelWindow).addWindowStateListener(windowStateListener = new WindowStateListener() {
 						@Override
 						public void windowStateChanged(WindowEvent e) {
 							if ((e.getNewState() & JFrame.ICONIFIED) == JFrame.ICONIFIED) {
 								delegate.setVisible(false);
 							} else {
 								delegate.setVisible(false);
 								(new Thread() {
 									public void run() {
 										try {
 											Thread.sleep(10);
 										} catch (Exception e) {
 											e.printStackTrace();
 										}
 										EventQueue.invokeLater(new Runnable() {
 											public void run() {
 												if(SwingContainer.this.getParent() != null) {
 													SwingContainer.this.getParent().invalidate();
 													SwingContainer.this.getParent().layout();
 													if(clientSetVisible) {
 														delegate.setVisible(true);
 														delegate.toFront();
 													}
 												}
 											}
 										});
 									}
 								}).start();
 							}
 						}
 					});
 					
 					TitleBar tb = ((DesktopFrame) topLevelWindow).getTitleBar();
 					if (tb != null) {
 						tb.addIconizeListener(iconizeListener = new TitleBar.IconizeListener() {
 							@Override
 							public void frameWillIconize() {
 								delegate.setVisible(false);
 							}
 						});
 					}
 				}
 				topLevelWindow.addComponentListener(componentAdapter = new ComponentAdapter() {
 					@Override
 					public void componentMoved(ComponentEvent e) {
 						Point p = getLocationOnScreen();
 						moveWindow(p.x, p.y);
 					}
 				});
 			}
 		}).start();
 
 		// Timer which is restarted to avoid setting the delegate visible until
 		// after any
 		// relocation or resizing is done.
 		visibilityTimer = new javax.swing.Timer(250,
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					delegate.setVisible(true);
 					delegate.requestFocus();
 				}
 			});
 
 		visibilityTimer.setRepeats(false);
 	}
 	
 	/**
 	 * Called from {@link Dummy} (in the case of Platform) /
 	 * Externally to resume state when application closing
 	 * is cancelled.
 	 */
 	public void applicationCloseCancelled() {
 		isPopupTransition = false;
 		isDialogTransition = false;
 		paintingBackground = false;
 		repaint();
 		if(clientSetVisible) {
 			delegate.setVisible(true);
 		}
 	}
 	
 	/** Implemetation of Window.WindowPopupListener */
 	@Override
 	public void popupOpened(Window popup) {
 		isPopupTransition = true;
 		if(clientSetVisible) {
 			setContentAsBackground();
 		}
 	}
 	
 	/** Implemetation of Window.WindowPopupListener */
 	@Override
 	public void popupClosed(Window popup) {}
 	
 	/** Implemetation of Window.WindowPopupListener */
 	@Override 
 	public void popupEnded(Window popup) {
 		isPopupTransition = false;
 	}
 	
 	public void dialogOpened() {
 		++windowsOpened;
 				
 		if(!isDialogTransition) {
 			if(clientSetVisible) {
 				setContentAsBackground();
 			}
 		}
 		isDialogTransition = true;
 	}
 	
 	public void dialogClosed() {
 		windowsOpened = Math.max(0, --windowsOpened);
 		if(windowsOpened == 0) {
 			isDialogTransition = false;
 			applicationCloseCancelled();
 		}
 	}
 	
 	public void onAddModeChanged(boolean isAddMode)  {
 		if(isAddMode) {
 			Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener);
 			setContentAsBackground();
 		}else{
 			Toolkit.getDefaultToolkit().addAWTEventListener(awtListener,
 				AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
 			paintingBackground = false;
 			setVisible(true);
 		}
 	}
 	
 	/**
 	 * Sets this container visible and delegates to
 	 * the {@link Delegate} to set it visible as
 	 * well.
 	 */
 	@Override
 	public void setVisible(boolean b) {
 		if(delegate == null) {
 			queueSetVisible(b);
 		}else{
 			delegate.setVisible(b);
 		}
 	}
 	
 	/**
 	 * Returns a {@link BufferedImage} of the current
 	 * rendered contents.
 	 * 
 	 * @return	a {@link BufferedImage} of the current
 	 * rendered contents.
 	 */
 	public BufferedImage getSnapshot() {
 		if(delegate == null) return null;
 		return delegate.getSnapshot();
 	}
 	
 	/**x
 	 * Triggers the painting of the {@link Delegate}'s 
 	 * contents directly in this {@code SwingContainer} 
 	 * as an image. This is a workaround for allowing 
 	 * popups/menuss in the main window to appear to be
 	 * above the contents of the Delegate. 
 	 */
 	public void setContentAsBackground() {
 		if(!paintingBackground && getSize().width > 0 && getSize().height > 0) {
 			background = getSnapshot();
 			if(background == null) return; 
 			paintingBackground = true;
 			(new Thread() {
 				public void run() {
 					try{Thread.sleep(250);}catch(Exception e) {}
 					delegate.setVisible(false);
 				}
 			}).start();
 			repaint();
 		}
 	}
 	
 	/**
 	 * Only called by client code. Used to distinguish between
 	 * internal visibility requests and client visibility requests.
 	 * @param b
 	 */
 	public void clientSetVisible(boolean b) {
 		this.clientSetVisible = b;
 		this.setVisible(b);
 		if(delegate != null) {
 			if(b) {
 				delegate.toFront();
 			}
 		}
 	}
 	
 	/**
 	 * Called externally to retrieve the {@link #clientSetVisible}
 	 * flag indicating that the client is to remain visible/invisible
 	 * despite internal calls to {@link #setVisible(boolean)}
 	 * @return	true if so, false if not
 	 */
 	public boolean isClientSetVisible() {
 		return this.clientSetVisible;
 	}
 	
 	/**
 	 * Returns the overlying delegate window
 	 * @return	the overlying delegate window
 	 */
 	public java.awt.Window getHostWindow() {
 		return delegate;
 	}
 
 	/**
 	 * Must be called by users to get rid of {@link Window} resource.
 	 */
 	public void dispose() {
 		delegate.setVisible(false);
 		delegate.dispose();
 		removeListeners();
 		fireWindowDisposeEvent();
 	}
 
 	/**
 	 * Custom event to notify interested listeners of recent dispose call on
 	 * {@link SwingContainer}
 	 */
 	public class WindowDisposeEvent {
 		SwingContainer container;
 		int eventType;
 
 		public WindowDisposeEvent(SwingContainer c, int windowEventType) {
 			this.container = c;
 			this.eventType = windowEventType;
 		}
 
 		public SwingContainer getComponent() {
 			return container;
 		}
 
 		public int getEventType() {
 			return eventType;
 		}
 	}
 
 	/**
 	 * Listeners of this type receive notifications when this
 	 * {@link SwingContainer} has dispose called on it.
 	 */
 	public interface WindowDisposeListener {
 		public void windowDisposed(WindowDisposeEvent d);
 	}
 
 	/**
 	 * Adds the specified {@link WindowStateListener} to this
 	 * {@code SwingContainer}
 	 * 
 	 * @param wse
 	 */
 	public void addWindowDisposeListener(WindowDisposeListener wdl) {
 		if (windowDisposeListeners == null) {
 			windowDisposeListeners = new ArrayList<WindowDisposeListener>();
 		}
 		windowDisposeListeners.add(wdl);
 	}
 
 	/**
 	 * Removes the specified {@link WindowStateListener} from registration.
 	 * 
 	 * @param wse
 	 */
 	public void removeWindowStateListener(WindowStateListener wse) {
 		if (windowDisposeListeners == null)
 			return;
 		windowDisposeListeners.remove(wse);
 	}
 
 	/**
 	 * Notifies all registered {@link WindowStateEvent} listeners that this
 	 * container will dismiss it's delegate.
 	 */
 	private void fireWindowDisposeEvent() {
 		if (windowDisposeListeners != null) {
 			WindowDisposeEvent we = new WindowDisposeEvent(this, WindowEvent.WINDOW_CLOSING);
 			for (WindowDisposeListener wse : windowDisposeListeners) {
 				wse.windowDisposed(we);
 			}
 		}
 	}
 
 	/**
 	 * Removes all the listeners added to control and distribute behavioral
 	 * state.
 	 */
 	public void removeListeners() {
 		if (topLevelWindow != null) {
 			if(topLevelWindow instanceof DesktopFrame){
 				((DesktopFrame) topLevelWindow).removeDragListener(dragListener);
 				((DesktopFrame) topLevelWindow).getTitleBar().removeIconizeListener(iconizeListener);
 			}
 			
 			topLevelWindow.removeWindowStateListener(windowStateListener);
 			topLevelWindow.removeComponentListener(componentAdapter);
 			
 			Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener);
 		}
 	}
 
 	/**
 	 * Listener which ensures that this "layered" container is visible.
 	 * 
 	 * @return ContainerListener
 	 */
 	private ContainerListener createContainerListener() {
 		return new ContainerListener.Adapter() {
 			@Override
 			public void componentInserted(Container container, int index) {
 				delegate.setVisible(true);
 			}
 		};
 	}
 
 	/**
 	 * Routes size information to the delegate
 	 * 
 	 * @param w
 	 *            width
 	 * @param h
 	 *            height
 	 */
 	public void setSize(int w, int h) {
 		if( w <= 1 || h <= 1) {
 			return;
 		}
 		
 		if (isDragging && delegate != null) {
 			if(clientSetVisible) {
 				visibilityTimer.restart();
 				delegate.setVisible(false);
 			}
 		}
 
 		super.setSize(w, h);
 		resizeWindow(w, h);
 		invalidate();
 		repaint();
 	}
 
 	/**
 	 * Routes location information to the delegate.
 	 * 
 	 * @param x
 	 *            coordinate
 	 * @param y
 	 *            coordinate
 	 */
 	public void setLocation(int x, int y) {
 		super.setLocation(x, y);
 		java.awt.Point convertedPoint = getLocationOnScreen();
 		moveWindow(convertedPoint.x, convertedPoint.y);
 	}
 
 	/**
 	 * Method used to add the Swing component to be hosted by this container.
 	 * 
 	 * @param c
 	 *            a java.awt.Component
 	 */
 	public void addSwingComponent(java.awt.Component c) {
 		if (c == null) {
 			throw new IllegalArgumentException("Component \"c\" cannot be null");
 		}
 
 		if (delegate == null) {
 			occupant = c;
 		} else {
 			processComponentAdd(c);
 		}
 
 	}
 
 	/**
 	 * Called directly if the "delegate" is instantiated, or by the delayed
 	 * initialization thread which waits for the top level window to be
 	 * available before instantiating the "delegate" so that the parent window
 	 * can be assigned at construction time.
 	 * 
 	 * @param c
 	 */
 	private void processComponentAdd(java.awt.Component c) {
 		java.awt.Component child = delegate.getChild();
 		if (child != null) {
 			delegate.remove(child);
 		}
 
 		delegate.add(c);
 		delegate.setVisible(true);
 		c.setFocusable(true);
 		occupant = c;
 	}
 
 	/**
 	 * Calculates the screen coordinates of this container which are used to
 	 * relocate the delegate window.
 	 * 
 	 * @return java.awt.Point the new screen coordinates of the delegate
 	 */
 	public java.awt.Point getLocationOnScreen() {
 		int xLocal = 0;
 		int yLocal = 0;
 
 		Component component = this;
 		while (component != null && component.isVisible()) {
 			if (component instanceof Display) {
 				java.awt.Window window = ((Display) component).getHostWindow();
 				xLocal += window.getX();
 				yLocal += window.getY();
 				if (window instanceof DesktopFrame) {
 					insets = ((DesktopFrame) window).getTitlebarInsets();
 				} else {
 					insets = window.getInsets();
 				}
 				xLocal += insets.left;
 				yLocal += insets.top;
 				break;
 			} else {
 				xLocal += component.getX();
 				yLocal += component.getY();
 			}
 			component = component.getParent();
 		}
 
 		return new java.awt.Point(xLocal, yLocal);
 	}
 
 	/**
 	 * Moves the delegate window to the specified coordinates.
 	 * 
 	 * @param x
 	 *            the horizontal coordinate
 	 * @param y
 	 *            the vertical coordinate
 	 */
 	boolean init = true;
 	public void moveWindow(int x, int y) {
 		if(init && (x <= 1 && y <= 1)) return;
 		init = false;
 		
 		if (delegate == null) {
 			queueMoveWindow(x, y);
 		} else {
 			delegate.setLocation(x, y);
 			delegate.toFront();
 		}
 	}
 
 	/**
 	 * Resizes the delegate window to the specified size.
 	 * 
 	 * @param w
 	 *            the new delegate width
 	 * @param h
 	 *            the new delegate height
 	 */
 	public void resizeWindow(int w, int h) {
 		if (delegate == null) {
 			queueResizeWindow(w, h);
 		} else {
 			delegate.setSize(w, h);
 			delegate.toFront();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	private void queueSetVisible(final boolean b) {
 		(new Thread() {
 			public void run() {
 				while (delegate == null) {
 					try {
 						Thread.sleep(100);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						SwingContainer.this.setVisible(b);
 					}
 				});
 			}
 		}).start();
 	}
 
 	/**
 	 * Waits until the delegate is available before issuing move calls.
 	 * 
 	 * @param x
 	 *            the new x location
 	 * @param y
 	 *            the new y location
 	 */
 	private void queueMoveWindow(final int x, final int y) {
 		(new Thread() {
 			public void run() {
 				while (delegate == null) {
 					try {
 						Thread.sleep(100);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						SwingContainer.this.moveWindow(x, y);
 						SwingContainer.this.invalidate();
 					}
 				});
 			}
 		}).start();
 	}
 
 	/**
 	 * Waits until delegate is available before issuing resize calls.
 	 * 
 	 * @param w
 	 *            the new width
 	 * @param h
 	 *            the new height
 	 */
 	private void queueResizeWindow(final int w, final int h) {
 		(new Thread() {
 			public void run() {
 				while (delegate == null) {
 					try {
 						Thread.sleep(100);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						SwingContainer.this.resizeWindow(w, h);
 					}
 				});
 			}
 		}).start();
 	}
 	
 	public void paint(Graphics2D g) {
 		if(paintingBackground) {
 			g.drawImage(background, 0, 0, null);
 		}else{
 			super.paint(g);
 			
 			g.setColor(Color.BLACK);
 			Rectangle r = g.getClipBounds();
 			g.fillRect(r.x, r.y, r.width, r.height);
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g.setFont(g.getFont().deriveFont(30f).deriveFont(java.awt.Font.BOLD));
 			int x = SwingUtilities.computeStringWidth(g.getFontMetrics(), "Loading...");
 			int y = g.getFontMetrics().getMaxAscent();
 			
 			x = (r.width - x) / 2;
 			y = (r.height - y) / 2;
 			g.setColor(Color.WHITE);
 			g.drawString("Loading...", x, y);
 		}
 		
 	}
 
 	/**
 	 * Overridden to install a NOOP skin basically
 	 * 
 	 * @param componentClass
 	 *            ignored
 	 */
 	@Override
 	protected void installSkin(Class<? extends org.apache.pivot.wtk.Component> componentClass) {
 		skin = new PanelSkin();
 		setSkin(skin);
 	}
 
 	/**
 	 * Delegate {@link JWindow} which contains the Swing component which is kept
 	 * in sync with the Apache Pivot component {@link SwingContainer}
 	 * 
 	 * @author David Ray
 	 */
 	@SuppressWarnings("serial")
 	private class Delegate extends java.awt.Window {
 		private java.awt.Component child;
 
 		public Delegate(java.awt.Window parent) {
 			super(parent);
 		}
 
 		@Override
 		public void update(Graphics g) {
 			paint(g);
 		}
 
 		@Override
 		public java.awt.Component add(java.awt.Component c) {
 			this.child = c;
 			return super.add(c);
 		}
 
 		@Override
 		public void setSize(int w, int h) {
 			super.setSize(w, h);
 			if(child == null) return;
 			child.setSize(w, h);
 		}
 
 		private java.awt.Component getChild() {
 			return child;
 		}
 
 		@Override
 		public void remove(java.awt.Component c) {
 			super.remove(c);
 			if (c == child) {
 				child = null;
 			}
 		}
 		
 		public void setVisible(boolean b) {
 			super.setVisible(b);
 			if(b) {
 				setSize(SwingContainer.this.getSize().width, SwingContainer.this.getSize().height);
 				setLocation(SwingContainer.this.getLocationOnScreen());
 			}else{
 				toBack();
 				if(topLevelWindow != null)  {
 					topLevelWindow.requestFocus();
 				}
 					
 			}
 		}
 		
 		public BufferedImage getSnapshot() {
 			BufferedImage img = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_ARGB);
 			Graphics g = img.getGraphics();
 			paint(g);
 			return img;
 		}
 	}
 
 }
