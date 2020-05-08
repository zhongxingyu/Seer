 /*******************************************************************************
  * Copyright (c) Emil Crumhorn and others - Hexapixel.com - emil.crumhorn@gmail.com
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    emil.crumhorn@gmail.com  - initial API and implementation
  *    eclipse-dev@volanakis.de - fix: don't dispose shell when close is aborted,
  *                               fix: notify help button on click, fix: compute  
  *                               correct help icon position when other things  
 *                               are above folder, fix: stuck hover after closing 
 *                               big button menu
  *******************************************************************************/ 
 
 package com.hexapixel.widgets.ribbon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Layout;
 
 import com.hexapixel.widgets.generic.ColorCache;
 
 public class RibbonTabFolder extends Composite implements MouseListener, MouseMoveListener {
 
 	private List<RibbonTab> mTabs;
 	public static int FIRST_TAB_SPACING = 46;
 	public static int TAB_SPACING = 1;
 	public static int TEXT_SPACING_SIDE = 9;
 	public static int TEXT_SPACING_TOP = 6;
 
 	public static int TAB_HEIGHT = 23;
 	public static int TAB_AREA_HEIGHT = 25;
 	
 	public static int NON_MAXIMIZED_HEIGHT = 29;
 	public static int MAXIMIZED_HEIGHT = 25;
 	
 	private static int MAX_HEIGHT = 118;
 	
 	private Color mTopLine = ColorCache.getInstance().getColor(176, 207, 247);
 	private Color mTopLineInner = ColorCache.getInstance().getColor(219, 232, 249);	
 	private Color mFillTop = ColorCache.getInstance().getColor(191, 219, 255);
 	private Color mFillBottom = ColorCache.getInstance().getColor(165, 195, 239);
 	
 	private static AbstractTabPainter mTabPainter = new DefaultTabPainter();
 	private static AbstractShellPainter mShellPainter = new DefaultShellPainter();
 	private static AbstractButtonPaintManager mButtonPainter = new DefaultButtonPaintManager();
 	
 	private RibbonTab mHoverTab;
 	private RibbonTab mSelectedTab;
 	
 	private boolean mCollapsedTabFolder;
 	private boolean drawEmptyTabs = true;
 	
 	// when we act as shell we need loads of params
 	private boolean drawAsShell = false;
 	private RibbonShell mRibbonShell;
 	
 	public static final int STATE_NONE = 0;
 	public static final int STATE_HOVER = 1;
 	public static final int STATE_HOVER_SELECTED = 2;
 	public static final int STATE_INACTIVE = 3;
 	
 	private Rectangle minButtonRect;
 	private Rectangle maxButtonRect;
 	private Rectangle closeButtonRect;
 	private Rectangle allButtonsRect;
 	private Rectangle bigButtonRect;
 	
 	private Rectangle menuBarRect;
 	private Rectangle fullMenuBarRect;
 	
 	private int minButtonState;
 	private int maxButtonState;
 	private int closeButtonState;
 	private int bigButtonState;
 	
 	private boolean mMouseIsDown;
 	private boolean mMouseWasDownOnMenubar;
 	
 	private Image mHelpImage;
 	private RibbonButton mHelpButton;
 
 	private Rectangle mQuickAccessBounds;
 	// end shell params	
 	
 	public RibbonTabFolder(Composite parent, int style) {
 		this(parent, style, null);
 	}
 	
 	public RibbonTabFolder(Composite parent, int style, RibbonShell shell) {
 		super(parent, style | SWT.DOUBLE_BUFFERED);
 		drawAsShell = (shell != null);
 		mRibbonShell = shell;
 		mTabs = new ArrayList<RibbonTab>();
 		setLayout(new TabFolderLayout());
 		
 		if (isShell())
 			updateMenuBarButtonCalculations();
 		
		mRibbonShell.getBigButtonMenu().addMenuListener(new MenuAdapter() {
			public void menuHidden(MenuEvent e) {
				Point cursor = e.display.getCursorLocation();
				Point xy = e.display.map(null, RibbonTabFolder.this, cursor);
				updateButtonStates(xy.x, xy.y);
			}	
		});
		
 		addKeyListener(new KeyListener() {
 
 			public void keyPressed(KeyEvent e) {
 				RibbonTooltipDialog.kill();
 			}
 
 			public void keyReleased(KeyEvent e) {
 			}
 			
 		});
 		
 		addControlListener(new ControlListener() {
 
 			public void controlMoved(ControlEvent e) {
 				RibbonTooltipDialog.kill();
 			}
 
 			public void controlResized(ControlEvent e) {
 				RibbonTooltipDialog.kill();
 				if (isShell())
 					updateMenuBarButtonCalculations();
 			}
 			
 		});
 		
 		addPaintListener(new PaintListener() {
 
 			public void paintControl(PaintEvent event) {
 				GC gc = event.gc;
 				drawTabFolder(gc, 0);
 			}
 			
 		});
 		
 		addMouseTrackListener(new MouseTrackListener() {
 
 			public void mouseEnter(MouseEvent e) {
 			}
 
 			public void mouseExit(MouseEvent e) {
 				if (mHoverTab != null) {
 					Rectangle oldBounds = mHoverTab.getBounds();
 					redraw(oldBounds.x-10, 0, oldBounds.width+20, TAB_HEIGHT+4, false);
 					mHoverTab.setHover(false);
 				}
 				
 				if (drawAsShell) {
 					minButtonState = STATE_NONE;
 					maxButtonState = STATE_NONE;
 					closeButtonState = STATE_NONE;
 					redrawMenuBarButton();
 				}
 				
 				if (mHelpButton != null) {
 					if (mHelpButton.isHoverButton()) {
 						mHelpButton.setHoverButton(false);
 						redraw(mHelpButton.getBounds());
 					}
 				}
 			}
 
 			public void mouseHover(MouseEvent me) {
 				if (mHelpButton != null) {
 					if (isInside(me.x, me.y, mHelpButton.getBounds())) {
 						if (mHelpButton.getToolTip() != null) {
 							int y = mHelpButton.getBounds().y + mHelpButton.getBounds().height + 10;
 							RibbonTooltipDialog.makeDialog(mHelpButton.getToolTip(), toDisplay((new Point(mHelpButton.getBounds().x, y))));
 						}
 					}
 				}
 				
 				if (isShell()) {
 					if (isInside(me.x, me.y, bigButtonRect)) {
 						if (mRibbonShell.getBigButtonTooltip() != null) {							
 							RibbonTooltipDialog.makeDialog(mRibbonShell.getBigButtonTooltip(), toDisplay((new Point(bigButtonRect.x, bigButtonRect.y+bigButtonRect.height))));
 						}
 					}
 					
 					if (isInside(me.x, me.y, mRibbonShell.getArrowButtonBounds())) {
 						Rectangle arrBounds = mRibbonShell.getArrowButtonBounds();
 						if (mRibbonShell.getToolbar() != null && mRibbonShell.getToolbar().getArrowTooltip() != null) {
 							RibbonTooltipDialog.makeDialog(mRibbonShell.getToolbar().getArrowTooltip(), toDisplay((new Point(arrBounds.x, arrBounds.y+arrBounds.height))));
 						}
 					}
 				}
 			}
 			
 		});
 		
 		addMouseMoveListener(this);		
 		addMouseListener(this);
 		
 		addMouseWheelListener(new MouseWheelListener() {
 			public void mouseScrolled(MouseEvent me) {
 				RibbonTooltipDialog.kill();
 
 				// if we're not inside the bounds, ignore
 				if (getBounds() == null || !isInside(me.x, me.y, getBounds()))
 					return;
 								
                 if (me.count > 0) 
                 	selectPrevTab();
                 else 
                 	selectNextTab();					 
 			}			
 		});
 	}
 		
 	
 	public void mouseDoubleClick(MouseEvent me) {
 		for (int i = 0; i < mTabs.size(); i++) {
 			RibbonTab ft = mTabs.get(i);
 			if (ft.getBounds() == null)
 				continue;
 			
 			if (isInside(me.x, me.y, ft.getBounds())) {
 				if (mCollapsedTabFolder)
 					expandTabFolder();
 				else
 					collapseTabFolder();
 			}
 		}
 		
 		mMouseIsDown = false;
 		
 		// TODO: Windows only?
 		if (isShell() && me.button == 1) {
 			if (!isInside(me.x, me.y, bigButtonRect) && !isInside(me.x, me.y, mQuickAccessBounds) && isInside(me.x, me.y, menuBarRect))
 				mRibbonShell.setMaximized(!mRibbonShell.getMaximized());
 		}
 		
 		// check buttons in toolbar if any
 		if (drawAsShell) 
 			updateButtonStates(me.x, me.y);
 	}
 	
 	void setQuickAccessBounds(Rectangle bounds) {
 		mQuickAccessBounds = bounds;
 	}
 
 	public void mouseDown(MouseEvent me) {
 		RibbonTooltipDialog.kill();
 		mMouseIsDown = true;
 		
 		mMouseWasDownOnMenubar = false;		
 		
 		if (drawAsShell && isInside(me.x, me.y, fullMenuBarRect) && !isInside(me.x, me.y, allButtonsRect)) {
 			// if we allow this boolean to be true for the full area of the menu bar you could move the entire window even
 			// when a window-border resize cursor is showing, which would be plain weird
 			if (isInside(me.x, me.y, menuBarRect) && !isInside(me.x, me.y, bigButtonRect))
 				mMouseWasDownOnMenubar = true;		
 		}
 
 		// regardless, tell our top shell that the mouse was clicked or we can't resize the shell in areas that are occupied by us
 		if (drawAsShell)
 			mRibbonShell.mouseDown(me);
 		
 		if (me.button != 1)
 			return;
 		
 		for (int i = 0; i < mTabs.size(); i++) {
 			RibbonTab ft = mTabs.get(i);
 			if (ft.getBounds() == null)
 				continue;
 			
 			if (isInside(me.x, me.y, ft.getBounds())) {
 				// don't re-click already selected tab
 				if (mSelectedTab == ft)
 					continue;
 				
 				selectTab(ft);
 			}
 		}
 		
 		if (drawAsShell) {
 			if (isInside(me.x, me.y, allButtonsRect) || isInside(me.x, me.y, bigButtonRect)) 
 				updateButtonStates(me.x, me.y);				
 		}
 		
 		if (mHelpButton != null) {
 			if (isInside(me.x, me.y, mHelpButton.getBounds())) {
 				if (mHelpButton.isSelected()) {
 					mHelpButton.setSelected(false);
 					redraw(mHelpButton.getBounds());
 				}
 				else {
 					mHelpButton.setSelected(true);
 					redraw(mHelpButton.getBounds());
 				}
 			}
 		}
 		
 		if (drawAsShell) {
 			if (isInside(me.x, me.y, bigButtonRect)) {
 				bigButtonClicked(me);
 			}
 		}
 	}
 	
 	private void bigButtonClicked(MouseEvent me) {
 		mRibbonShell.bigButtonClicked(me);
 	}
 
 	public void mouseUp(MouseEvent me) {
 		RibbonTooltipDialog.kill();
 		mMouseIsDown = false;
 
 		if (drawAsShell)
 			mRibbonShell.mouseUp(me);
 
 		// do this regardless, mouse up could've been anywhere
 		updateButtonStates(me.x, me.y);
 		redrawMenuBarButton();				
 		
 		if (drawAsShell) {
 			if (me.button == 1) {
 				if (isInside(me.x, me.y, minButtonRect))
 					minButtonClicked();
 				else if (isInside(me.x, me.y, maxButtonRect))
 					maxButtonClicked();
 				else if (isInside(me.x, me.y, closeButtonRect))
 					closeButtonClicked();
 			}
 		}
 		
 		if (mHelpButton != null) {
 			if (mHelpButton.isSelected()) {
 				if (isInside(me.x, me.y, mHelpButton.getBounds())) {
 					helpButtonClicked(me);
 					mHelpButton.setSelected(false);
 					mHelpButton.setHoverButton(true);
 					redraw(mHelpButton.getBounds());
 				}
 				else {
 					// deselect, dehover, as user is elsewhere with the mouse
 					if (mHelpButton.isSelected() || mHelpButton.isHoverButton()) {
 						mHelpButton.setSelected(false);
 						mHelpButton.setHoverButton(false);
 						redraw(mHelpButton.getBounds());
 					}
 				}
 			}
 		}
 	}
 	
 	private void helpButtonClicked(MouseEvent me) {
 		mHelpButton.notifySelectionListeners(me);
 	}
 
 	public void mouseMove(MouseEvent me) {
 		RibbonTooltipDialog.kill();
 		
 		if (drawAsShell) {
 			if (mMouseIsDown && mMouseWasDownOnMenubar) {
 				//TODO: make this part of mMouseWasDownOnMenubar
 				//if (!isInside(me.x, me.y, bigButtonRect) && !isInside(me.x, me.y, mQuickAccessBounds) && isInside(me.x, me.y, menuBarRect))
 				mRibbonShell.moveShell(me);
 			}
 			
 			// for resize-shell events etc			
 			mRibbonShell.mouseMove(me);
 		}
 			
 		int y = TAB_HEIGHT + 4;
 		if (drawAsShell)
 			y += NON_MAXIMIZED_HEIGHT;
 		
 		for (int i = 0; i < mTabs.size(); i++) {
 			RibbonTab ft = mTabs.get(i);
 			if (ft.getBounds() == null)
 				continue;
 			
 			Rectangle tabBounds = ft.getBounds();
 								
 			if (isInside(me.x, me.y, ft.getBounds())) {
 				if (!ft.isHover()) {
 					if (mHoverTab != null) {
 						Rectangle oldBounds = mHoverTab.getBounds();
 						redraw(oldBounds.x-10, 0, oldBounds.width+20, y, false);
 						mHoverTab.setHover(false);
 					}
 					
 					ft.setHover(true);
 					redraw(tabBounds.x-10, 0, tabBounds.width+20, y, false);
 					mHoverTab = ft;
 				}
 			}					
 			else {
 				if (ft.isHover()) {
 					redraw(tabBounds.x-10, 0, tabBounds.width+20, y, false);
 					ft.setHover(false);
 				}
 			}
 		}
 		
 		// check buttons in toolbar if any
 		if (drawAsShell) 
 			updateButtonStates(me.x, me.y);
 		
 		if (mHelpButton != null) {
 			if (isInside(me.x, me.y, mHelpButton.getBounds())) {
 				if (!mHelpButton.isHoverButton()) {
 					mHelpButton.setHoverButton(true);
 					redraw(mHelpButton.getBounds());
 				}
 			}
 			else {
 				if (mHelpButton.isHoverButton()) {
 					mHelpButton.setHoverButton(false);
 					redraw(mHelpButton.getBounds());
 				}
 			}			
 		}
 	}
 	
 	// why doesn't swt let you pass in a rectangle to redraw??
 	private void redraw(Rectangle rect) {
 		redraw(rect.x, rect.y, rect.width, rect.height, false);
 	}
 
 	public boolean isShell() {
 		return (mRibbonShell != null && drawAsShell);
 	}
 	
 	public void minButtonClicked() {
 		if (!isShell())
 			return;
 
 		if (!mRibbonShell.getShell().getMinimized())
 			mRibbonShell.getShell().setMinimized(true);
 	}
 	
 	public void restoreButtonClicked() {
 		if (!isShell())
 			return;
 		
 		mRibbonShell.setMaximized(false);
 	}
 	
 	public void maxButtonClicked() {
 		if (!isShell())
 			return;
 		
 		if (!mRibbonShell.getMaximized()) 
 			mRibbonShell.setMaximized(true);
 		else
 			restoreButtonClicked();			
 	}
 	
 	public void closeButtonClicked() {
 		if (!isShell())
 			return;
 		
 		mRibbonShell.getShell().close();
 		// don't dispose, close may be cancelled and disposes already
 		// mRibbonShell.getShell().dispose();
 	}
 	
 	public List<RibbonTab> getTabs() {
 		return mTabs;
 	}
 	
 	public void disposeAllChildren() {
 		for (RibbonTab tab : mTabs) 
 			tab.dispose();		
 		
 		mHoverTab = null;
 		mSelectedTab = null;
 		
 		mTabs.clear();
 		redraw();
 	}
 	
 	public void redraw() {
 		if (isShell())
 			updateMenuBarButtonCalculations();
 		
 		super.redraw();
 	}
 	
 	public boolean isDrawEmptyTabs() {
 		return drawEmptyTabs;
 	}
 	
 	public void collapseTabFolder() {
 		//System.err.println("collapsing");
 		
 		mCollapsedTabFolder = true;
 		if (isShell()) {
 			mRibbonShell.getShell().layout(true, true);
 			mRibbonShell.redrawContents();
 			Rectangle bounds = getBounds();
 			redraw(bounds.x, bounds.y, bounds.width, TAB_HEIGHT+4, true);
 			getParent().layout(true, true);			
 		}
 		else {
 			redraw();
 			layout(true);
 			Rectangle bounds = getBounds();
 			redraw(bounds.x, bounds.y, bounds.width, TAB_HEIGHT+4, true);
 			getParent().layout(true, true);
 		}
 		
 	}
 	
 	public void expandTabFolder() {
 		mCollapsedTabFolder = false;
 		if (isShell()) {
 			mRibbonShell.getShell().layout(true);
 			mRibbonShell.redrawContents();
 		}
 		else {
 			layout(true);
 			redraw();
 		}
 	}
 	
 	public boolean isCollapsed() {
 		return mCollapsedTabFolder;
 	}
 	
 	public boolean isExpanded() {
 		return !mCollapsedTabFolder;
 	}
 	
 	private void deselectTab(RibbonTab ft) {
 		if (ft.isSelected()) {
 			Rectangle tabBounds = ft.getBounds();
 			redraw(tabBounds.x-10, 0, tabBounds.width+20, TAB_HEIGHT+4, false);
 			ft.setSelected(false);
 			if (ft.getFancyToolbar() != null)
 				ft.getFancyToolbar().setVisible(false);
 		}
 		else {
 			if (ft.getFancyToolbar() != null)
 				ft.getFancyToolbar().setVisible(false);			
 		}
 
 	}
 	
 	public void selectTab(RibbonTab ft) {
 		checkWidget();
 
 		if (mSelectedTab == ft)
 			return;
 		
 		if (mSelectedTab != null) {
 			deselectTab(mSelectedTab);
 		}
 		
 		Rectangle tabBounds = ft.getBounds();
 		if (tabBounds == null)
 			return;
 		
 		ft.setSelected(true);
 		// do a full redraw as the right edge (which moves when control resizes) might need a redraw depending on the 
 		// width of contents inside the tab
 		redraw();
 		mSelectedTab = ft;						
 		update();
 		layout(true);		
 		
 		//TODO: This isn't fully functional, old hovers can still remain, the rest seems to work, may be overkill
 		//mSelectedTab.scrollWheelUpdate();
 	}
 	
 	public void selectNextTab() {
 		checkWidget();
 		int cur = 0;
 		
 		List<RibbonTab> visibles = getVisibleTabs();
 		
 		if (mSelectedTab != null)
 			cur = visibles.indexOf(mSelectedTab);
 	
 		cur++;
 		if (cur > visibles.size()-1)
 			cur = visibles.size()-1;
 	
 		selectTab(visibles.get(cur));
 	}
 	
 	private List<RibbonTab> getVisibleTabs() {
 		if (drawEmptyTabs)
 			return mTabs;
 		
 		List<RibbonTab> visibles = new ArrayList<RibbonTab>();
 		for (int i = 0; i < mTabs.size(); i++) {
 			if (!mTabs.get(i).isEmpty())
 				visibles.add(mTabs.get(i));
 		}
 		
 		return visibles;	
 	}
 	
 	public void selectPrevTab() {			
 		checkWidget();
 		int cur = 0;
 		
 		List<RibbonTab> visibles = getVisibleTabs();
 		
 		if (mSelectedTab != null)
 			cur = visibles.indexOf(mSelectedTab);
 
 		cur--;
 		if (cur < 0)
 			cur = 0;
 
 		selectTab(visibles.get(cur));
 	}
 	
 	void redrawArea(Rectangle rect) {
 		redraw(rect.x, rect.y, rect.width, rect.height, false);
 	}
 	
 	void redrawMenuBarButton() {
 		if (!isShell())
 			return;
 		
 		redrawArea(allButtonsRect);
 	}
 	
 	void redrawBigButton() {
 		if (!isShell())
 			return;
 		
 		redrawArea(bigButtonRect);
 	}
 	
 	private boolean isInside(int x, int y, Rectangle rect) {
 		if (rect == null) {
 			return false;
 		}
 
 		return x >= rect.x && y >= rect.y && x <= (rect.x + rect.width - 1) && y <= (rect.y + rect.height - 1);
 	}
 
 	protected void tabControlSet(RibbonTab ft) {
 		if (ft.getFancyToolbar() == null)
 			return;
 		
 		if (ft == mSelectedTab) 
 			ft.getFancyToolbar().setVisible(true);
 		else
 			ft.getFancyToolbar().setVisible(false);
 		
 		layout(true);
 	}
 	
 	protected void tabAdded(RibbonTab ft) {
 		if (!mTabs.contains(ft)) {
 			// TODO: Make option, just selects the first tab automatically
 			if (mTabs.size() == 0) {
 				ft.setSelected(true);
 				mSelectedTab = ft;
 			}
 			
 			mTabs.add(ft);
 			recalculateTabBounds();
 			redraw();
 		}
 	}
 	
 	// sets tab bounds so that we can actually draw them
 	private void recalculateTabBounds() {
 		int x = FIRST_TAB_SPACING;
 		
 		GC temp = new GC(this);
 		
 		int y = drawAsShell ? NON_MAXIMIZED_HEIGHT : 0;
 		
 		for (int i = 0; i < mTabs.size(); i++) {
 			RibbonTab ft = (RibbonTab) mTabs.get(i);
 			ft.setIndex(i);
 			Point p = temp.stringExtent(ft.getName());
 			int width = p.x + (TEXT_SPACING_SIDE * 2) + (5 * 2);
 			Rectangle bounds = new Rectangle(x, y, width, TAB_HEIGHT);
 			ft.setBounds(bounds);
 			
 			x += TAB_SPACING + width;
 		}
 		
 		temp.dispose();
 	}
 			
 	private void drawTabFolder(GC gc, int yStart) {
 		Rectangle bounds = getBounds();
 		
 		int neg = 0;
 		int x = 0;
 		
 		if (drawAsShell) {
 			mShellPainter.paintShell(mRibbonShell, gc);
 			yStart += (mRibbonShell.getMaximized() ? MAXIMIZED_HEIGHT : NON_MAXIMIZED_HEIGHT);
 		}		
 		
 		gc.setForeground(mTopLine);
 		gc.drawLine(x, yStart+0, bounds.width-neg, yStart);
 		gc.setForeground(mTopLineInner);
 		gc.drawLine(x, yStart+1, bounds.width-neg, yStart+1);
 		gc.setForeground(mFillTop);
 		gc.setBackground(mFillBottom);
 		
 		int maxHeight = mCollapsedTabFolder ? TAB_HEIGHT : MAX_HEIGHT;
 		
 		gc.fillGradientRectangle(x, yStart+2, bounds.width-neg, maxHeight, true);
 		
 		if (drawAsShell) {
 			mShellPainter.drawBigButton(gc);
 			mShellPainter.drawMenubarToolbar(gc);
 		}
 		
 		// draw tabs
 		for (int i = 0; i < mTabs.size(); i++) {
 			RibbonTab ft = mTabs.get(i);
 			if (ft.isEmpty() && !drawEmptyTabs)
 				continue;
 			
 			mTabPainter.drawTab(gc, ft, yStart+3);
 		}
 		
 		updateBottomLine(gc, yStart);
 		
 		if (mHelpImage != null) {
 			//createHelpButton();
 			drawHelpArea(gc);
 		}
 		
 		// shell border, half of it, rest is drawn in the RibbonShell class
 		if (drawAsShell && !mRibbonShell.getMaximized()) {
 			Color borderColor = null;
 			if (Display.getDefault().getActiveShell() == mRibbonShell.getShell())
 				borderColor = AbstractShellPainter.outerBorderNonMaximized;
 			else
 				borderColor = AbstractShellPainter.outerBorderNonMaximized_Inactive;
 			
 			Color faded = GraphicsHelper.lighter(borderColor, 50);
 
 			gc.setForeground(borderColor);
 			// top left
 			gc.drawLine(1, 1, 2, 1);
 			gc.drawLine(0, 2, 1, 2);
 			gc.drawLine(0, 3, 0, 3);
 			
 			// top right
 			gc.drawLine(bounds.width-3, 1, bounds.width-2, 1);
 			gc.drawLine(bounds.width-2, 2, bounds.width-1, 2);
 			gc.drawLine(bounds.width-1, 3, bounds.width-1, 3);
 			// fade the last ones
 			gc.setForeground(faded);
 			gc.drawLine(0, 4, 0, 4);
 			
 			gc.drawLine(bounds.width-4, 1, bounds.width-4, 1);
 			gc.drawLine(bounds.width-1, 4, bounds.width-1, 4);
 			
 	
 		}
 	}
 	
 	private void drawHelpArea(GC gc) {
 		if (mHelpButton == null)
 			return;
 		
 		Rectangle bounds = getBounds();
 		int fromRight = 22;
 		// if the shell and the RibbonTabFolder are not at the same
 		// height (i.e. something is above the ribbon), we must adjust 
 		int fromTop = 31 + computeHeightDelta(getShell(), this);
 		
 		if (isShell()) {
 			if (mRibbonShell.getMaximized()) {
 				//fromRight += 0; // just a reminder
 				fromTop -= 4;
 			}
 			else
 				fromRight += 4;
 		}
 		else
 			fromRight += 4;
 		
 		// TODO: Replace 22's with globals
 		mHelpButton.setBounds(new Rectangle(bounds.x+bounds.width-fromRight, bounds.y+fromTop, 22, 22));				
 		
 		mButtonPainter.drawMenuToolbarButton(gc, mHelpButton);
 	}
 	
 	private int computeHeightDelta(Control topControl, Control btmControl) {
 		int topHeight = topControl.toDisplay(0, 0).y;
 		int btmHeight = btmControl.toDisplay(0, 0).y;
 		return topHeight - btmHeight;
 	}
 	
 	private void createHelpButton() {
 		if (mHelpButton == null)
 			mHelpButton = new RibbonButton(this, mHelpImage, null, RibbonButton.STYLE_NO_DEPRESS);
 		
 		if (mHelpImage == null && mHelpButton != null) 
 			mHelpButton = null;		
 	}
 	
 	private void updateMenuBarButtonCalculations() {		
 		boolean maximized = mRibbonShell.getMaximized();
 		
 		int fromRight = maximized ? -2 : 4;
 		int spacing = 0;
 		int xStart = fromRight + (25 * 3) + (spacing * 2);
 		xStart = getBounds().width - xStart;
 		int yStart = maximized ? 0 : 4;
 		
 		int bigSpacerXY = maximized ? 4 : 7;
 		
 		minButtonRect = new Rectangle(xStart, yStart, 25, 25);
 		maxButtonRect = new Rectangle(xStart+25, yStart, 25, 25);
 		closeButtonRect = new Rectangle(xStart+25+25, yStart, 25, 25);
 		allButtonsRect = new Rectangle(xStart, yStart, 75, 25);
 				
 		bigButtonRect = new Rectangle(bigSpacerXY, bigSpacerXY, 38, 38);
 		
 		menuBarRect = new Rectangle(RibbonShell.CORNER_FLEX, RibbonShell.SIDE_FLEX, getBounds().width - (RibbonShell.CORNER_FLEX * 2), (maximized ? MAXIMIZED_HEIGHT : NON_MAXIMIZED_HEIGHT) - RibbonShell.SIDE_FLEX);
 		fullMenuBarRect = new Rectangle(0, 0, getBounds().width, (maximized ? MAXIMIZED_HEIGHT : NON_MAXIMIZED_HEIGHT) - RibbonShell.SIDE_FLEX);
 	}	
 	
 	public Rectangle getMinButtonBounds() {
 		return minButtonRect;
 	}
 	
 	public Rectangle getMaxButtonBounds() {
 		return maxButtonRect;
 	}
 	
 	public Rectangle getCloseButtonBounds() {
 		return closeButtonRect;
 	}
 	
 	public Rectangle getBigButtonBounds() {
 		return bigButtonRect;
 	}
 	
 	public Point getMenubarToolbarLocation() {
 		return new Point(bigButtonRect.x+30, bigButtonRect.y-2);
 	}
 	
 	private boolean shellIsFocused() {
 		if (Display.getDefault().getFocusControl() == null)
 			return false;
 		
 		if (Display.getDefault().getFocusControl().getShell() == mRibbonShell.getShell())
 			return true;
 		
 		return false;
 	}
 
 	public int getMinButtonState() {
 		if (!shellIsFocused())
 			return STATE_INACTIVE;
 				
 		return minButtonState;
 	}
 
 	public int getMaxButtonState() {
 		if (!shellIsFocused())
 			return STATE_INACTIVE;
 				
 		return maxButtonState;
 	}
 
 	public int getCloseButtonState() {
 		if (!shellIsFocused())
 			return STATE_INACTIVE;
 				
 		return closeButtonState;
 	}
 	
 	public int getBigButtonState() {
 		if (!shellIsFocused())
 			return STATE_INACTIVE;
 		
 		return bigButtonState;
 	}
 	
 	private void updateButtonStates(int x, int y) {
 		Point onUs = new Point(x, y);
 		
 		if (isInside(onUs.x, onUs.y, allButtonsRect)) {
 			boolean minChanged = false;
 			boolean maxChanged = false;
 			boolean closeChanged = false;
 			
 			if (isInside(onUs.x, onUs.y, minButtonRect)) {
 				if (mMouseIsDown) {
 					if (minButtonState != STATE_HOVER_SELECTED)
 						minChanged = true;
 					
 					minButtonState = STATE_HOVER_SELECTED;
 				}
 				else {
 					if (minButtonState != STATE_HOVER)
 						minChanged = true;
 					
 					minButtonState = STATE_HOVER;
 				}
 			}
 			else {
 				if (minButtonState != STATE_NONE)
 					minChanged = true;
 
 				minButtonState = STATE_NONE;
 			}
 			
 			if (isInside(onUs.x, onUs.y, maxButtonRect)) {
 				if (mMouseIsDown) {
 					if (maxButtonState != STATE_HOVER_SELECTED)
 						maxChanged = true;
 					
 					maxButtonState = STATE_HOVER_SELECTED;
 				}
 				else {
 					if (maxButtonState != STATE_HOVER)
 						maxChanged = true;
 					
 					maxButtonState = STATE_HOVER;
 				}				
 			}
 			else {
 				if (maxButtonState != STATE_NONE)
 					maxChanged = true;
 				
 				maxButtonState = STATE_NONE;
 			}
 			
 			if (isInside(onUs.x, onUs.y, closeButtonRect)) {
 				if (mMouseIsDown) {
 					if (closeButtonState != STATE_HOVER_SELECTED)
 						closeChanged = true;
 					
 					closeButtonState = STATE_HOVER_SELECTED;
 				}
 				else {
 					if (closeButtonState != STATE_HOVER)
 						closeChanged = true;
 					
 					closeButtonState = STATE_HOVER;
 				}
 			}
 			else {
 				if (closeButtonState != STATE_NONE)
 					closeChanged = true;
 
 				closeButtonState = STATE_NONE;
 			}
 			
 			if (minChanged || maxChanged || closeChanged)
 				redrawMenuBarButton();
 		}
 		else {
 			// remove any effect if mouse isn't even here and some button is in a different state
 			if (minButtonState != STATE_NONE || maxButtonState != STATE_NONE || closeButtonState != STATE_NONE) {
 				minButtonState = STATE_NONE;
 				maxButtonState = STATE_NONE;
 				closeButtonState = STATE_NONE;
 				redrawMenuBarButton();
 			}
 		}
 		
 		if (isInside(onUs.x, onUs.y, bigButtonRect)) {
 			boolean bigChanged = false;
 			
 			if (mMouseIsDown) {
 				if (bigButtonState != STATE_HOVER_SELECTED)
 					bigChanged = true;
 				
 				bigButtonState = STATE_HOVER_SELECTED;
 			}
 			else {
 				if (bigButtonState != STATE_HOVER)
 					bigChanged = true;
 			
 				bigButtonState = STATE_HOVER;
 			}
 			
 			if (bigChanged)
 				redrawBigButton();
 		}
 		else {
 			bigButtonState = STATE_NONE;
 			redrawBigButton();
 		}
 	}
 		
 	private void updateBottomLine(GC gc, int yStart) {
 		// draw open tab, basically the 1px line below all tabs, draws even if mSelectedTab is null	
 		mTabPainter.drawTabBorder(gc, getBounds(), mSelectedTab, mCollapsedTabFolder, yStart);
 	}
 	
 	public RibbonTab getSelectedTab() {
 		return mSelectedTab;
 	}
 	
 	@Override
 	public Rectangle getBounds() {
 		checkWidget();
 		Rectangle bounds = super.getBounds();
 		Rectangle ret = null;
 		
 		int heightBonus = drawAsShell ? (mRibbonShell.getMaximized() ? MAXIMIZED_HEIGHT : NON_MAXIMIZED_HEIGHT) : 0;
 		
 		if (mCollapsedTabFolder)
 			ret = new Rectangle(bounds.x, bounds.y, bounds.width, TAB_HEIGHT+4+heightBonus);
 		else
 			ret = new Rectangle(bounds.x, bounds.y, bounds.width, MAX_HEIGHT+heightBonus);
 	
 		// compensate for shell
 		if (drawAsShell) 
 			ret.height -= 2;		
 			
 		return ret;
 	}
 	
 	public Point getSize() {
 		Rectangle bounds = getBounds();
 	
 		return new Point(bounds.width, bounds.height);
 	}
 	
 	public void setDrawEmptyTabs(boolean drawEmpty) {
 		drawEmptyTabs = drawEmpty;
 	}
 	
 	public void setHelpImage(Image helpImage) {
 		mHelpImage = helpImage;		
 		createHelpButton();
 		
 		if (helpImage != null && mHelpButton != null)
 			mHelpButton.setImage(helpImage);
 	}
 	
 	public Image getHelpImage() {
 		return mHelpImage;
 	}
 	
 	public RibbonButton getHelpButton() {
 		return mHelpButton;
 	}	
 	
 	class TabFolderLayout extends Layout {
 
 		@Override
 		protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
 			return getSize();
 		}
 
 		@Override
 		protected void layout(Composite composite, boolean flushCache) {
 			Control [] children = composite.getChildren();
 			
 			if (children == null)
 				return;
 			
 			int yBonus = drawAsShell ? (mRibbonShell.getMaximized() ? MAXIMIZED_HEIGHT : NON_MAXIMIZED_HEIGHT) : 0; 
 						
 			for (int i = 0; i < children.length; i++) {
 				Control child = children[i];
 				if (child instanceof RibbonTabComposite) {
 					RibbonTabComposite ft = (RibbonTabComposite) child;
 					
 					if (mCollapsedTabFolder) {
 						ft.setVisible(false);
 						continue;						
 					}					
 					
 					if (mSelectedTab != null) {
 						if (mSelectedTab.getFancyToolbar() == ft) {
 							ft.layout(true);
 							ft.setLocation(new Point(4, TAB_AREA_HEIGHT+2+yBonus));
 							// TODO: Why do I need to do this stupid call to make it work?
 							ft.setSize(ft.getSize());
 							ft.setVisible(true);
 						}
 					}
 				}
 			}
 	
 			
 		}
 		
 	}
 }
