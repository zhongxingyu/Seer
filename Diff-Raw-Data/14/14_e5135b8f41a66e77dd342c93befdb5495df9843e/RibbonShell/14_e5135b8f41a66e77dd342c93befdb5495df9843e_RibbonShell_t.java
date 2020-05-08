 /*******************************************************************************
 * Copyright (c) Emil Crumhorn and others - Hexapixel.com - emil.crumhorn@gmail.com
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    emil.crumhorn@gmail.com     - initial API and implementation
  *    eclipse-dev AT volanakis.de - refactored to avoid multiple calls to init,
  *       added constructor for pre-existing shell, made initial layout calc
  *       work for Shell.open(), push button support for QuickAccessToolbar,
 *       auto-redraw on enablement change, fix: maximize on two-screen setup
  *******************************************************************************/ 
 
 package com.hexapixel.widgets.ribbon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Region;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
 import org.eclipse.swt.widgets.Shell;
 
 import com.hexapixel.widgets.generic.ColorCache;
 import com.hexapixel.widgets.generic.CursorCache;
 
 public class RibbonShell implements MouseListener, MouseMoveListener, KeyListener, ShellListener, ControlListener {
 
 	private Shell mShell;
 	
 	private Point mMouseDownLoc;
 	
 	private RibbonTabFolder mTabFolder;
 	
 	private Color bgColor = ColorCache.getInstance().getColor(177, 200, 231);
 	
 	private static final int SIDE_NONE = 0;
 	private static final int SIDE_TOP_LEFT = 1;
 	private static final int SIDE_LEFT = 2;
 	private static final int SIDE_BOTTOM_LEFT = 3;
 	private static final int SIDE_BOTTOM = 4;
 	private static final int SIDE_BOTTOM_RIGHT = 5;
 	private static final int SIDE_RIGHT = 6;
 	private static final int SIDE_TOP_RIGHT = 7;
 	private static final int SIDE_TOP = 8;
 	
 	private Rectangle topLeftCorner;
 	private Rectangle leftSide;
 	private Rectangle bottomLeftCorner;
 	private Rectangle bottomSide;
 	private Rectangle bottomRightCorner;
 	private Rectangle rightSide;
 	private Rectangle topRightCorner;
 	private Rectangle topSide;
 	
 	private Rectangle mArrowButtonBounds;
 	private int mArrowButtonState = AbstractButtonPaintManager.STATE_NONE;
 	
 	public static final int CORNER_FLEX = 8;
 	public static final int SIDE_FLEX = 4;
 	
 	private int sideCurrentlyResizing = SIDE_NONE;
 	private Point mShellSizeBeforeResize;
 	private boolean mMouseIsDown;
 	private Image buttonImage;
 	private QuickAccessShellToolbar mToolbar;
 	
 	private Menu mBigButtonMenu;
 	private List<SelectionListener> mBigButtonListeners;
 	private RibbonTooltip mBigButtonTooltip;
 	
 	private boolean mShellMaximized;
 	private Point mNonMaximizedLocation;
 	private Point mNonMaximizedSize;
 		
 	public RibbonShell(Display parent) {
 		this(parent, null);	
 	}
 
 	public RibbonShell(Display parent, Image buttonImage) {
 		this(parent, SWT.NO_TRIM | SWT.DOUBLE_BUFFERED, buttonImage);
 	}
 
 	public RibbonShell(Display parent, int style) {
 		this(parent, style, null);
 	}
 
 	public RibbonShell(Display parent, int style, Image buttonImage) {
 		mShell = new Shell(parent, style);
 		this.buttonImage = buttonImage;
 		init();
 	}
 	
 	/**
 	 * Construct a RibbonShell for a pre-existing Shell
 	 * @param thisShell a Shell instance; never null; should have the SWT.NO_TRIM style
 	 */
 	public RibbonShell(Shell thisShell) {
 		mShell = thisShell;
 		init();
 	}
 	
 	public RibbonShell(Shell parent, int style) {
 		this(parent, style, null);
 	}
 	
 	public RibbonShell(Shell parent, int style, Image buttonImage) {
 		mShell = new Shell(parent, style);
 		this.buttonImage = buttonImage;
 		init();
 	}
 	
 	public QuickAccessShellToolbar getToolbar() {
 		return mToolbar;
 	}
 	
 	// internal
 	List<RibbonButton> getToolbarButtons() {
 		return mToolbar.getButtons();
 	}	
 	
 	public Image getButtonImage() {
 		return buttonImage;
 	}
 	
 	public void setButtonImage(Image buttonImage) {
 		this.buttonImage = buttonImage;
 	}
 	
 	public void setMaximized(boolean maximized) {
 		if (mShellMaximized != maximized) {
 			mShellMaximized = maximized;
 			if (mShellMaximized) 
 				maximize();
 			else
 				restore();
 		}
 	}
 	
 	private void maximize() {
 		mNonMaximizedLocation = mShell.getLocation();
 		mNonMaximizedSize = mShell.getSize();
 		
		Monitor monitor = mShell.getMonitor();
		mShell.setBounds(monitor.getBounds());
 	}
 	
 	private void restore() {
 		mShell.setSize(mNonMaximizedSize);
 		mShell.setLocation(mNonMaximizedLocation);
 	}
 	
 	public boolean getMaximized() {
 		return mShellMaximized;
 	}
 
 	private void init() {
 		mBigButtonListeners = new ArrayList<SelectionListener>();
 		mBigButtonMenu = new Menu(mShell);
 		
 		mToolbar = new QuickAccessShellToolbar(this);
 		updateCalculations();
 		mShell.setLayout(new RibbonShellLayout());
 		
 		mTabFolder = new RibbonTabFolder(mShell, SWT.NONE, this);
 		
 		mShell.addMouseListener(this);
 		mShell.addMouseMoveListener(this);
 		mShell.addShellListener(this);
 		mShell.addKeyListener(this);
 		mShell.addControlListener(this);	
 		
 		mShell.addPaintListener(new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				drawBackground(e.gc);
 			}
 		});
 		
 		mShell.addShellListener(new ShellAdapter() {
 			@Override
 			public void shellActivated(ShellEvent e) {
 				e.display.asyncExec(new Runnable() {
 					public void run() {
 						// re-layout post shell open or the tabfolder will look unpopulated 
 						// until some action happens
 						mTabFolder.layout(true, true);
 						mTabFolder.redraw();
 						mToolbar.updateBounds();
 					}
 				});
 				mShell.removeShellListener(this);
 			}
 		});
 	}	
 	
 	private void drawBackground(GC gc) {
 		Rectangle bounds = mShell.getBounds();
 		if (!getMaximized()) {
 			Color borderColor = null;
 			if (Display.getDefault().getActiveShell() == mShell)
 				borderColor = AbstractShellPainter.outerBorderNonMaximized;
 			else
 				borderColor = AbstractShellPainter.outerBorderNonMaximized_Inactive;
 			gc.setBackground(borderColor);
 			
 			gc.fillRectangle(0, 0, bounds.width, bounds.height);
 			
 			gc.setBackground(bgColor);
 			gc.fillRectangle(1, 1, bounds.width-2, bounds.height-2);
 			
 			// draw pretty borders, only bottoms, the top borders (as the menubar needs to connect for the big button to work)
 			// are drawn in a similar fashion in the RibbonTabFolder
 
 			gc.setForeground(borderColor);
 			// bottom left
 			gc.drawLine(1, bounds.height-4, 1, bounds.height-4);
 			gc.drawLine(1, bounds.height-3, 2, bounds.height-3);
 			gc.drawLine(2, bounds.height-2, 3, bounds.height-2);
 			
 			// bottom right
 			gc.drawLine(bounds.width-4, bounds.height-2, bounds.width-3, bounds.height-2);
 			gc.drawLine(bounds.width-3, bounds.height-3, bounds.width-2, bounds.height-3);
 			gc.drawLine(bounds.width-2, bounds.height-4, bounds.width-2, bounds.height-4);				
 		}
 		else {
 			gc.setBackground(bgColor);
 			gc.fillRectangle(bounds);
 		}
 	}
 	
 	public Shell getShell() {
 		return mShell;
 	}
 
 	public void setSize(int w, int h) {
 		mShell.setSize(w, h);
 	}
 	
 	public void setSize(Point size) {
 		mShell.setSize(size);
 	}
 		
 	public void setText(String text) {
 		// this is for the OS
 		mShell.setText(text);
 		redrawContents();
 	}
 	
 	public void redrawContents() {
 		mTabFolder.redraw();
 	}
 	
 	public boolean isDisposed() {
 		return mShell.isDisposed();
 	}
 	
 	void setArrowButtonBounds(Rectangle bounds) {
 		mArrowButtonBounds = bounds;
 	}
 	
 	int getArrowButtonState() {
 		return mArrowButtonState;
 	}
 	
 	Rectangle getArrowButtonBounds() {
 		return mArrowButtonBounds;
 	}
 	
 	public void setBigButtonTooltip(RibbonTooltip tooltip) {
 		mBigButtonTooltip = tooltip;
 	}
 	
 	public RibbonTooltip getBigButtonTooltip() {
 		return mBigButtonTooltip;
 	}
 		
 	public void addBigButtonListener(SelectionListener listener) {
 		if (!mBigButtonListeners.contains(listener))
 			mBigButtonListeners.add(listener);
 	}
 	
 	public void open() {
 		mShell.open();
 	}
 	
 	public void setVisible(boolean visible) {
 		mShell.setVisible(visible);
 	}
 	
 	public String getText() {
 		return mShell.getText();
 	}
 
 	public RibbonTabFolder getRibbonTabFolder() {
 		return mTabFolder;
 	}
 	
 	private void updateCalculations() {
 		Rectangle bounds = mShell.getBounds();
 		topLeftCorner = new Rectangle(0, 0, CORNER_FLEX, CORNER_FLEX);
 		leftSide = new Rectangle(0, CORNER_FLEX, SIDE_FLEX, bounds.height - (CORNER_FLEX * 2));
 		bottomLeftCorner = new Rectangle(0, bounds.height-CORNER_FLEX, CORNER_FLEX, CORNER_FLEX);
 		bottomSide = new Rectangle(CORNER_FLEX, bounds.height-SIDE_FLEX, bounds.width - (CORNER_FLEX * 2), SIDE_FLEX);
 		bottomRightCorner = new Rectangle(bounds.width-CORNER_FLEX, bounds.height-CORNER_FLEX, CORNER_FLEX, CORNER_FLEX);
 		rightSide = new Rectangle(bounds.width-SIDE_FLEX, CORNER_FLEX, SIDE_FLEX, bounds.height - (CORNER_FLEX * 2));
 		topRightCorner = new Rectangle(bounds.width-CORNER_FLEX, 0, CORNER_FLEX, CORNER_FLEX);
 		topSide = new Rectangle(CORNER_FLEX, 0, bounds.width-(CORNER_FLEX * 2), SIDE_FLEX);
 	}
 	
 	private boolean isInside(int x, int y, Rectangle rect) {
 		if (rect == null) {
 			return false;
 		}
 
 		return x >= rect.x && y >= rect.y && x <= (rect.x + rect.width - 1) && y <= (rect.y + rect.height - 1);
 	}
 	
 	private int getResizeSide(int x, int y) {
 		if (isInside(x, y, topLeftCorner))
 			return SIDE_TOP_LEFT;
 		else if (isInside(x, y, leftSide))
 			return SIDE_LEFT;
 		else if (isInside(x, y, bottomLeftCorner))
 			return SIDE_BOTTOM_LEFT;
 		else if (isInside(x, y, bottomSide))
 			return SIDE_BOTTOM;
 		else if (isInside(x, y, bottomRightCorner))
 			return SIDE_BOTTOM_RIGHT;
 		else if (isInside(x, y, rightSide))
 			return SIDE_RIGHT;
 		else if (isInside(x, y, topRightCorner))
 			return SIDE_TOP_RIGHT;
 		else if (isInside(x, y, topSide))
 			return SIDE_TOP;
 
 		return SIDE_NONE;
 	}
 	
 	// internal callback for when the big button is clicked
 	void bigButtonClicked(MouseEvent me) {
 		Event e = new Event();
 		e.button = me.button;
 		e.data = this;
 		e.display = me.display;
 		e.stateMask = me.stateMask;
 		e.widget = me.widget;
 		e.x = me.x;
 		e.y = me.y;
 		SelectionEvent se = new SelectionEvent(e);
 		
 		for (int i = 0; i < mBigButtonListeners.size(); i++) {
 			mBigButtonListeners.get(i).widgetSelected(se);
 		}
 	}
 		
 	public void showBigButtonMenu() {
 		Rectangle bBounds = mTabFolder.getBigButtonBounds(); 
 		mBigButtonMenu.setLocation(mShell.toDisplay(bBounds.x, bBounds.height+bBounds.y));
 		mBigButtonMenu.setVisible(true);		
 	}
 	
 	public Menu getBigButtonMenu() {
 		return mBigButtonMenu;
 	}
 	
 	public void mouseMove(MouseEvent e) {
 		Cursor cur = mShell.getCursor();
 
 		if (isInside(e.x, e.y, mToolbar.getBounds())) {
 			if (mToolbar.mouseMove(e))
 				redrawToolbar();
 		}
 		else {
 			if (mToolbar.dehover())
 				redrawToolbar();
 		}
 		
 		if (mArrowButtonBounds != null) {
 			if (mArrowButtonState != AbstractButtonPaintManager.STATE_SELECTED) {
 				if (isInside(e.x, e.y, mArrowButtonBounds)) {
 					if (mArrowButtonState == AbstractButtonPaintManager.STATE_NONE) {
 						mArrowButtonState = AbstractButtonPaintManager.STATE_HOVER;
 						redrawArrowButton();
 					}
 				}
 				else {
 					if (mArrowButtonState != AbstractButtonPaintManager.STATE_NONE) {
 						mArrowButtonState = AbstractButtonPaintManager.STATE_NONE;
 						redrawArrowButton();
 					}
 				}
 			}
 		}
 
 		if (getMaximized()) {
 			if (cur != null)
 				mShell.setCursor(null);
 			return;
 		}
 				
 		if (!mShellMaximized) {
 			int side = getResizeSide(e.x, e.y);
 			doResize(side, e);
 		}		
 	}
 	
 	private void redrawToolbar() {
 		mToolbar.redraw();
 	}
 	
 	private void redrawArrowButton() {
 		mTabFolder.redraw(mArrowButtonBounds.x, mArrowButtonBounds.y, mArrowButtonBounds.width, mArrowButtonBounds.height, false);
 	}
 	
 	private void doResize(int side, MouseEvent e) {
 		if (sideCurrentlyResizing == SIDE_NONE) {
 			Cursor cursor = null;
 			switch (side) {
 				case SIDE_TOP_LEFT:
 				case SIDE_BOTTOM_RIGHT:
 					cursor = CursorCache.getCursor(SWT.CURSOR_SIZENWSE);
 					break;
 				case SIDE_LEFT:
 				case SIDE_RIGHT:
 					cursor = CursorCache.getCursor(SWT.CURSOR_SIZEWE);
 					break;
 				case SIDE_BOTTOM_LEFT:
 				case SIDE_TOP_RIGHT:
 					cursor = CursorCache.getCursor(SWT.CURSOR_SIZENESW);
 					break;
 				case SIDE_TOP:
 				case SIDE_BOTTOM:
 					cursor = CursorCache.getCursor(SWT.CURSOR_SIZENS);
 					break;
 			}
 					
 			mShell.setCursor(cursor);
 		}
 		
 		// this clause deals with shell resizing as our shell has no borders but we still need to mimic it
 		// it's all just location and size pushing to simulate a mouse resize
 		if (sideCurrentlyResizing != SIDE_NONE) {
 			// do shell resize
 			Point point = Display.getDefault().map(mShell, null, e.x, e.y);
 			int deltaX = e.x - mMouseDownLoc.x;
 			int deltaY = e.y - mMouseDownLoc.y;
 			
 			switch (sideCurrentlyResizing) {
 				case SIDE_LEFT:
 					mShell.setLocation(point.x - mMouseDownLoc.x, mShell.getLocation().y);
 					mShell.setSize(mShell.getSize().x - deltaX, mShell.getSize().y);					
 					break;
 				case SIDE_BOTTOM:
 					mShell.setSize(mShell.getSize().x, mShellSizeBeforeResize.y + deltaY);
 					break;
 				case SIDE_RIGHT:
 					mShell.setSize(mShellSizeBeforeResize.x + deltaX, mShell.getSize().y);
 					break;
 				case SIDE_TOP:
 					mShell.setSize(mShellSizeBeforeResize.x, mShell.getSize().y - deltaY);
 					mShell.setLocation(mShell.getLocation().x, point.y - mMouseDownLoc.y);
 					break;
 				case SIDE_TOP_LEFT:
 					mShell.setLocation(point.x - mMouseDownLoc.x, point.y - mMouseDownLoc.y);
 					mShell.setSize(mShell.getSize().x - deltaX, mShell.getSize().y - deltaY);
 					break;
 				case SIDE_BOTTOM_LEFT:
 					mShell.setLocation(point.x - mMouseDownLoc.x, mShell.getLocation().y);
 					mShell.setSize(mShell.getSize().x - deltaX, mShellSizeBeforeResize.y + deltaY);
 					break;
 				case SIDE_BOTTOM_RIGHT:
 					mShell.setSize(mShellSizeBeforeResize.x + deltaX, mShellSizeBeforeResize.y + deltaY);
 					break;
 				case SIDE_TOP_RIGHT:
 					mShell.setLocation(mShell.getLocation().x, point.y - mMouseDownLoc.y);
 					mShell.setSize(mShellSizeBeforeResize.x + deltaX, mShell.getSize().y - deltaY);
 					break;
 			}
 			/*
 			// TODO: Make this pretty and flicker-less
 			if (mShell.getSize().x < 40)
 				mShell.setSize(40, mShell.getSize().y);
 			if (mShell.getSize().y < 40);
 				mShell.setSize(mShell.getSize().y, 40);*/
 		}
 	}
 	
 	public void moveShell(MouseEvent e) {
 		if (mMouseDownLoc == null || mShellMaximized)
 			return;
 		
 		Point point = Display.getDefault().map(mShell, null, e.x, e.y);		
 		mShell.setLocation(point.x - mMouseDownLoc.x, point.y - mMouseDownLoc.y);					
 	}
 		
 	public void mouseDoubleClick(MouseEvent e) {
 	}
 
 	public void mouseDown(MouseEvent e) {
 		if (e.button == 1) {
 			mMouseIsDown = true;
 			mMouseDownLoc = new Point(e.x, e.y);
 			
 			mShellSizeBeforeResize = mShell.getSize();
 			sideCurrentlyResizing = getResizeSide(e.x, e.y);
 		}			
 		
 		if (isInside(e.x, e.y, mToolbar.getBounds())) {			
 			if (mToolbar.mouseDown(e))
 				redrawToolbar();
 		}
 		
 		if (isInside(e.x, e.y, mArrowButtonBounds)) {
 			// if we click a selected button, deselect and switch to hover
 			if (mArrowButtonState == AbstractButtonPaintManager.STATE_SELECTED) {
 				mArrowButtonState = AbstractButtonPaintManager.STATE_HOVER;
 				redrawArrowButton();
 			}
 			else {
 				// click
 				mArrowButtonState = AbstractButtonPaintManager.STATE_SELECTED;
 				redrawArrowButton();
 				arrowButtonClicked();
 			}
 		}
 		else {
 			// reset state if clicked elsewhere
 			if (mArrowButtonState != AbstractButtonPaintManager.STATE_NONE) {
 				mArrowButtonState = AbstractButtonPaintManager.STATE_NONE;
 				redrawArrowButton();
 			}
 		}
 	}
 	
 	private void arrowButtonClicked() {
 		// TODO: something
 	}
 
 	public void mouseUp(MouseEvent e) {
 		mMouseIsDown = false;
 		mMouseDownLoc = null;
 		sideCurrentlyResizing = SIDE_NONE;
 		
 		if (isInside(e.x, e.y, mToolbar.getBounds())) {			
 			if (mToolbar.mouseUp(e))
 				redrawToolbar();
 		} else {
 			// update side resize cursor
 			doResize(getResizeSide(e.x, e.y), e);
 
 			// don't let the shell disappear due to extremely small size
 			Point size = mShell.getSize();
 			if (size.x < 40 || size.y < 40) {
 				if (size.x < 40)
 					size.x = 40;
 				if (size.y < 40)
 					size.y = 40;
 
 				mShell.setSize(size.x, size.y);
 			}
 		}
 	}
 
 	public void shellActivated(ShellEvent e) {
 		redrawContents();
 		mShell.redraw();
 	}
 
 	public void shellClosed(ShellEvent e) {
 	}
 
 	public void shellDeactivated(ShellEvent e) {
 		redrawContents();
 		mShell.redraw();
 	}
 
 	public void shellDeiconified(ShellEvent e) {
 		redrawContents();
 	}
 
 	public void shellIconified(ShellEvent e) {
 	}
 				
 	public void keyPressed(KeyEvent e) {
 		if (e.keyCode == SWT.F4 && e.stateMask == SWT.ALT)
 			mShell.dispose();
 	}
 
 	public void keyReleased(KeyEvent e) {
 	}
 	
 	public void controlMoved(ControlEvent e) {
 		
 	}
 	
 	public void controlResized(ControlEvent e) {
 		updateCalculations();
 		updateShellStructure();
 		
 		//not needed 
 		// redrawContents();		
 	}
 	
 	private void updateShellStructure() {
 		Region region = new Region(mShell.getDisplay());
 		int maxX = mShell.getBounds().width;
 		int maxY = mShell.getBounds().height;
 		
 		region.add(0, 0, maxX, maxY);
 		
 		if (!getMaximized()) {
 			// top left
 			region.subtract(0, 0, 4, 1);
 			region.subtract(0, 1, 2, 1);
 			region.subtract(0, 2, 1, 1);
 			region.subtract(0, 3, 1, 1);
 			
 			// bottom right
 			region.subtract(maxX-1, maxY-4, 1, 4);
 			region.subtract(maxX-2, maxY-2, 1, 2);
 			region.subtract(maxX-3, maxY-1, 1, 1);
 			region.subtract(maxX-4, maxY-1, 1, 1);
 			
 			// bottom left
 			region.subtract(0, maxY-4, 1, 4);
 			region.subtract(1, maxY-2, 1, 2);
 			region.subtract(2, maxY-1, 1, 1);
 			region.subtract(3, maxY-1, 1, 1);
 			
 			// top right
 			region.subtract(maxX-4, 0, 4, 1);
 			region.subtract(maxX-2, 1, 2, 1);
 			region.subtract(maxX-1, 2, 1, 1);
 			region.subtract(maxX-1, 3, 1, 1);
 		}
 		
 		mShell.setRegion(region);
 	}
 	
 	class RibbonShellLayout extends Layout {
 
 		private Point sizes [];
 
 		private void init(Composite composite) {
 			Control [] children = composite.getChildren();
 			sizes = new Point[children.length];
 			for (int i = 0; i < children.length; i++) {
 				sizes[i] = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
 			}
 		}
 		
 		@Override
 		protected Point computeSize(Composite composite, int hint, int hint2,
 				boolean flushCache) {
 			
 			if (flushCache) {
 				init(composite);
 			}
 			
 			return null;
 		}
 
 		@Override
 		protected void layout(Composite composite, boolean flushCache) {
 			Control [] children = composite.getChildren();
 			if (sizes == null || flushCache || sizes.length != children.length)
 				init(composite);
 			
 			int y = 0;
 			int x = getMaximized() ? 0 : 1; // border width
 			for (int i = 0; i < children.length; i++) {
 				Control child = children[i];
 				
 				if (child instanceof RibbonTabFolder)
 					child.setBounds(new Rectangle(x, y, mShell.getBounds().width-2, sizes[i].y));
 				else 
 					child.setBounds(new Rectangle(x, y, sizes[i].x-2, sizes[i].y));
 				
 				y += sizes[i].y;
 			}
 		}
 		
 		
 	}
 }
