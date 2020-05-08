 package org.eclipse.riena.ui.swt;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Scrollable;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.internal.ui.swt.Activator;
 import org.eclipse.riena.ui.swt.facades.SWTFacade;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * The border drawer needs to be registered like follows:
  * <ul>
  * <li>as {@link PaintListener} to the control for which the border will be drawn</li>
  * <li>as {@link ControlListener} to the entire parents hierarchy of the control (including the {@link Shell})</li>
  * </ul>
  * The registration/unregistration is implemented in the <tt>register()</tt> and <tt>unregister()</tt> methods, which must be called by clients.
  * 
 * @since 4.0
  */
 public class BorderDrawer implements Listener {
 
 	public static final int DEFAULT_BORDER_WIDTH = 1;
 	private static final String GC_KEY = BorderDrawer.class.getName() + ".GC"; //$NON-NLS-1$
 
 	private final IDecorationActivationStrategy activationStrategy;
 	private final Control control;
 	private Color borderColor;
 	private int borderWidth;
 
 	/**
 	 * collect all listener registrations so they can be unregistered when unregister() is called
 	 */
 	private final List<Runnable> toUnregister = new ArrayList<Runnable>();
 
 	/**
 	 * the currently visible control area in display-relative coordinates
 	 */
 	private Rectangle visibleControlAreaOnDisplay = new Rectangle(0, 0, 0, 0);
 
 	/**
 	 * The area that needs updating before drawing the decoration somewhere else.
 	 */
 	private Rectangle updateArea;
 
 	private int specialWidgetWidthAdjustment;
 	private boolean needRedraw = true;
 	private Event lastMoveEvent;
 
 	private final Listener updateListener = new Listener() {
 		public void handleEvent(final Event event) {
 			update(false);
 		}
 	};
 
 	/**
 	 * @param control
 	 *            the UI element for which the border will be drawn, not <code>null</code>
 	 */
 	public BorderDrawer(final Control control) {
 		this(control, DEFAULT_BORDER_WIDTH, null, null);
 	}
 
 	/**
 	 * @param control
 	 *            the UI element for which the border will be drawn, not <code>null</code>
 	 * @param borderWidth
 	 *            the desired width of the border that will be drawn
 	 * @param borderColor
 	 *            the desired color of the border that will be drawn
 	 * @param activationStrategy
 	 *            the strategy that determines when the border should be drawn or <code>null</code> if the border should be always shown
 	 */
 	public BorderDrawer(final Control control, final int borderWidth, final Color borderColor, final IDecorationActivationStrategy activationStrategy) {
 		Assert.isNotNull(control);
 		this.control = control;
 		this.borderWidth = borderWidth;
 		this.borderColor = borderColor;
 		this.activationStrategy = activationStrategy;
 	}
 
 	/**
 	 * Registers the {@link PaintListener} to the control and {@link ControlListener} to all parents from the hierarchy (including the {@link Shell}).
 	 */
 	public void register() {
 		if (control instanceof DatePickerComposite || control instanceof CompletionCombo) {
 			specialWidgetWidthAdjustment = 16;
 			final Control[] children = ((Composite) control).getChildren();
 			for (final Control child : children) {
 				if (child instanceof Text) {
 					registerToControl(child, SWTFacade.Paint);
 				}
 			}
 		} else if (control instanceof CCombo) {
 			registerToControl(control.getParent(), SWTFacade.Paint);
 		} else {
 			registerToControl(control, SWTFacade.Paint);
 		}
 
 		Composite parent = control.getParent();
 		do {
 			registerToControl(parent, SWT.Resize, SWT.Move);
 			registerToControl(parent, updateListener, SWTFacade.Paint);
 		} while ((parent = parent.getParent()) != null);
 
 		registerMnemonicsListener();
 	}
 
 	/**
 	 * Needed for the mnemonics handling in windows:
 	 * <p>
 	 * Pressing the ALT key causes the mnemonics to appear -> all mnemonic-able controls will be redrawn. This redraw causes the borders on non-mnemonic-able
 	 * widgets to disappear. This happens only the first time the mnemonics are triggered (the first time ALT is pressed).
 	 */
 	private void registerMnemonicsListener() {
 		final Listener altListener = new Listener() {
 			public void handleEvent(final Event event) {
 				if (event.keyCode == SWT.ALT) {
 					update(true);
 					control.getDisplay().removeFilter(SWT.KeyDown, this);
 				}
 			}
 		};
 		control.getDisplay().addFilter(SWT.KeyDown, altListener);
 		toUnregister.add(new Runnable() {
 			public void run() {
 				control.getDisplay().removeFilter(SWT.KeyDown, altListener);
 			}
 		});
 	}
 
 	/**
 	 * Register as listener for the given event types on the given control. This method will automatically add an unregister runnable. There is no need to
 	 * manually unregister this registration.
 	 * 
 	 * @param control
 	 *            the control to register the listener
 	 * @param eventTypes
 	 *            the event types to listen for
 	 */
 	private void registerToControl(final Control control, final int... eventTypes) {
 		registerToControl(control, this, eventTypes);
 	}
 
 	private void registerToControl(final Control control, final Listener listener, final int... eventTypes) {
 		for (final int eventType : eventTypes) {
 			control.addListener(eventType, listener);
 		}
 		toUnregister.add(new Runnable() {
 			public void run() {
 				for (final int eventType : eventTypes) {
 					control.removeListener(eventType, listener);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Unregisters the {@link PaintListener} and {@link ControlListener} from the control and all parents.
 	 */
 	public void unregister() {
 		for (final Runnable r : toUnregister) {
 			r.run();
 		}
 		toUnregister.clear();
 	}
 
 	/**
 	 * @param borderColor
 	 *            the borderColor to set
 	 */
 	public void setBorderColor(final Color borderColor) {
 		this.borderColor = borderColor;
 	}
 
 	/**
 	 * @return the borderColor
 	 */
 	public Color getBorderColor() {
 		return borderColor;
 	}
 
 	/**
 	 * @param borderWidth
 	 *            the borderWidth to set
 	 */
 	public void setBorderWidth(final int borderWidth) {
 		this.borderWidth = borderWidth;
 	}
 
 	/**
 	 * @return the borderWidth
 	 */
 	public int getBorderWidth() {
 		return borderWidth;
 	}
 
 	public void paintControl(final Event event) {
 		if (needRedraw) {
 			Rectangle visibleControlArea;
 			if (control instanceof CCombo) {
 				visibleControlArea = ((Composite) control).getClientArea();
 			} else {
 				visibleControlArea = getVisibleControlArea(event);
 			}
 			visibleControlAreaOnDisplay = includeBorder(toVisibleControlAreaOnDisplay(visibleControlArea));
 			needRedraw = false;
 		}
 		if (shouldShowDecoration()) {
 			Composite someParent = control instanceof Composite ? (Composite) control : control.getParent();
 			boolean fullyPainted = false;
 			while (someParent != null && !fullyPainted) {
 				fullyPainted = includeAndDrawBorder(someParent);
 				someParent = someParent.getParent();
 			}
 		}
 	}
 
 	/**
 	 * Updates the area where the border is normally drawn
 	 */
 	public void update(final boolean redraw) {
 		final Shell shell = control.getShell();
 		if (updateArea != null && redraw) {
 			shell.redraw(updateArea.x, updateArea.y, updateArea.width, updateArea.height, true);
 		}
 		Rectangle bounds = control.getBounds();
 		bounds.x = bounds.y = 0;
 		bounds = getVisibleControlAreaStartingWith(bounds, null);
 		final Rectangle onDisplay = toVisibleControlAreaOnDisplay(bounds);
 		updateArea = includeBorder(toAreaOnControl(onDisplay, shell));
 		updateArea.x = updateArea.x - 1;
 		updateArea.y = updateArea.y - 1;
 		updateArea.width = updateArea.width + 2;
 		updateArea.height = updateArea.height + 2;
 	}
 
 	// helping methods
 	//////////////////
 
 	private static void logWarning(final String message) {
 		final Logger logger = Log4r.getLogger(Activator.getDefault(), BorderDrawer.class);
 		logger.log(LogService.LOG_WARNING, message);
 	}
 
 	/**
 	 * Returns whether the decoration should be shown or it should not.
 	 * 
 	 * @return {@code true} if the decoration should be shown, {@code false} if it should not.
 	 */
 	private boolean shouldShowDecoration() {
 		if (SwtUtilities.isDisposed(control)) {
 			return false;
 		}
 		if (!control.isVisible()) {
 			return false;
 		}
 		if (borderWidth <= 0) {
 			return false;
 		}
 		if (activationStrategy != null && !activationStrategy.isActive()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @return true if the border could be completely painted on the available client area
 	 */
 	private boolean includeAndDrawBorder(final Composite someParent) {
 		final Rectangle areaOnControl = toAreaOnControl(visibleControlAreaOnDisplay, someParent);
 		final Rectangle clientArea = someParent.getClientArea();
 		drawBorder(areaOnControl, getGC(someParent));
 		return areaOnControl.x + areaOnControl.width < clientArea.width && areaOnControl.y + areaOnControl.width < clientArea.height;
 	}
 
 	/**
 	 * @param someParent
 	 * @return
 	 */
 	private GC getGC(final Composite someParent) {
 		if (someParent.getData(GC_KEY) == null) {
 			final GC gc = new GC(someParent);
 			someParent.setData(GC_KEY, gc);
 			someParent.addDisposeListener(new DisposeListener() {
 				public void widgetDisposed(final DisposeEvent e) {
 					gc.dispose();
 				}
 			});
 		}
 		return (GC) someParent.getData(GC_KEY);
 	}
 
 	/**
 	 * draws the border <i>inside</i> the given area
 	 */
 	private void drawBorder(final Rectangle rect, final GC gc) {
 		if ((rect.width == 0) && (rect.height == 0)) {
 			return;
 		}
 		final Color previousForeground = gc.getForeground();
 		if (borderColor != null) {
 			gc.setForeground(borderColor);
 		} else {
 			logWarning("BorderColor is null!"); //$NON-NLS-1$
 		}
 		for (int i = 0; i < borderWidth; i++) {
 			gc.drawRectangle(rect.x + i, rect.y + i, rect.width - 2 * i, rect.height - 2 * i);
 		}
 		gc.setForeground(previousForeground);
 	}
 
 	/**
 	 * retrieve the visible area of the given control (including scroll bars)
 	 */
 	private Rectangle getVisibleControlArea(final Event event) {
 		final Rectangle visibleControlArea = new Rectangle(0, 0, 0, 0);
 		visibleControlArea.width = event.x > 0 ? event.x + event.width : event.width;
 		visibleControlArea.height = event.y > 0 ? event.y + event.height : event.height;
 
 		return getVisibleControlAreaStartingWith(visibleControlArea, event);
 	}
 
 	/**
 	 * @param visibleControlArea
 	 * @param event
 	 * @return
 	 */
 	private Rectangle getVisibleControlAreaStartingWith(final Rectangle visibleControlArea, final Event event) {
 		// if some scroll bars are visible, their size must be also included
 		if (control instanceof Scrollable) {
 			final ScrollBar horizontalBar = ((Scrollable) control).getHorizontalBar();
 			if (horizontalBar != null && horizontalBar.isVisible()) {
 				visibleControlArea.height += horizontalBar.getSize().y;
 			}
 			final ScrollBar verticalBar = ((Scrollable) control).getVerticalBar();
 			if (verticalBar != null && verticalBar.isVisible()) {
 				visibleControlArea.width += verticalBar.getSize().x;
 			}
 		}
 
 		// some special handling for the tree widget
 		if (control instanceof Tree) {
 			final Tree t = (Tree) control;
 			if (t.getColumnCount() > 0 && event != null) {
 				visibleControlArea.x = event.x;
 				visibleControlArea.width -= event.x;
 			}
 			if (t.getHeaderVisible()) {
 				visibleControlArea.y -= t.getHeaderHeight();
 				visibleControlArea.height += t.getHeaderHeight();
 			}
 			//			System.out.println(visibleControlArea);
 		} else if (control instanceof DatePickerComposite
 				&& visibleControlArea.width + specialWidgetWidthAdjustment + 2 * control.getBorderWidth() == control.getBounds().width) {
 			visibleControlArea.width += specialWidgetWidthAdjustment;
 		} else if (control instanceof CompletionCombo) {
 			if (((Composite) control).getChildren().length == 3) {
 				// we also have a label
 				visibleControlArea.x -= 16;
 				visibleControlArea.width += 16;
 			} else if (visibleControlArea.width + specialWidgetWidthAdjustment + 2 * control.getBorderWidth() + 1 == control.getBounds().width) {
 				visibleControlArea.width += specialWidgetWidthAdjustment + 1;
 			}
 		}
 		return visibleControlArea;
 	}
 
 	/**
 	 * transform the given area from display-relative to target control relative coordinates
 	 */
 	private Rectangle toAreaOnControl(final Rectangle onDisplay, final Control target) {
 		final Point onControl = target.toControl(onDisplay.x, onDisplay.y);
 		return new Rectangle(onControl.x, onControl.y, onDisplay.width, onDisplay.height);
 	}
 
 	/**
 	 * @return include the border size on each side of the given rectangle
 	 */
 	private Rectangle includeBorder(final Rectangle visibleControlArea) {
 		final int controlBorder = control.getBorderWidth();
 		final int border = controlBorder + borderWidth;
 		final int x = visibleControlArea.x - border;
 		final int y = visibleControlArea.y - border;
 		final int width = visibleControlArea.width + 2 * border - 1;
 		final int height = visibleControlArea.height + 2 * border - 1;
 		return new Rectangle(x, y, width, height);
 	}
 
 	/**
 	 * @return the visible control area relative to its display
 	 */
 	private Rectangle toVisibleControlAreaOnDisplay(final Rectangle visibleControlArea) {
 		final Point onDisplay = control.toDisplay(visibleControlArea.x, visibleControlArea.y);
 		return new Rectangle(onDisplay.x, onDisplay.y, visibleControlArea.width, visibleControlArea.height);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
 	 */
 	public void handleEvent(final Event event) {
 		switch (event.type) {
 		case SWT.Move:
 			needRedraw = true;
 			lastMoveEvent = event;
 			update(false);
 			control.getDisplay().timerExec(200, new Runnable() {
 				public void run() {
 					if (event == lastMoveEvent) {
 						control.redraw();
 					}
 				}
 			});
 			break;
 		case SWT.Resize:
 			needRedraw = true;
 			update(true);
 			break;
 
 		case SWTFacade.Paint:
 			paintControl(event);
 			break;
 
 		default:
 			break;
 		}
 	}
 }
