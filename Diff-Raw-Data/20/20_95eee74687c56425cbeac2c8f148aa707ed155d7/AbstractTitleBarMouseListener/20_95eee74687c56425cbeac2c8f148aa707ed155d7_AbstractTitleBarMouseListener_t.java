 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.internal.ui.swt.utils.ShellHelper;
 import org.eclipse.riena.ui.swt.facades.SWTFacade;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.renderer.AbstractTitleBarRenderer;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * TODO After any mouse operation a method of this listener is called.
  */
 public abstract class AbstractTitleBarMouseListener implements MouseListener, MouseTrackListener, MouseMoveListener {
 
 	private final static ShellHelper SHELL_HELPER = new ShellHelper();
 
 	enum BtnState {
 		NONE, HOVER, HOVER_SELECTED;
 	}
 
 	private Cursor handCursor;
 	private Cursor grabCursor;
 	private Cursor defaultCursor;
 
 	private final static int BTN_COUNT = 3;
 	private final static int CLOSE_BTN_INDEX = 0;
 	private final static int MAX_BTN_INDEX = 1;
 	private final static int MIN_BTN_INDEX = 2;
 	private final BtnState[] btnStates = new BtnState[BTN_COUNT];
 	private boolean mouseDownOnButton;
 	private boolean moveInside;
 	private boolean move;
 	private Point moveStartPoint;
 
 	public AbstractTitleBarMouseListener() {
 		resetBtnStates();
 		mouseDownOnButton = false;
 		move = false;
 		updateRenderer();
 	}
 
 	/**
 	 * Returns the renderer of the title bar.
 	 * 
 	 * @return renderer
 	 */
 	protected abstract AbstractTitleBarRenderer getTitleBarRenderer();
 
 	/**
 	 * Resets the states of the buttons.
 	 */
 	private void resetBtnStates() {
 		for (int i = 0; i < btnStates.length; i++) {
 			changeBtnState(BtnState.NONE, i);
 		}
 	}
 
 	/**
 	 * Sets the state of a button (and resets the others).
 	 * 
 	 * @param newState
 	 *            state to set
 	 * @param btnIndex
 	 *            button index
 	 */
 	private void changeBtnState(final BtnState newState, final int btnIndex) {
 		if (newState != BtnState.NONE) {
 			resetBtnStates();
 		}
 		btnStates[btnIndex] = newState;
 	}
 
 	/**
 	 * Updates the states of the buttons.
 	 * 
 	 * @param e
 	 *            mouse event
 	 */
 	private void updateButtonStates(final MouseEvent e) {
 
 		final Point pointer = new Point(e.x, e.y);
 		boolean insideAButton = false;
 
 		resetBtnStates();
 		if (getTitleBarRenderer().isInsideCloseButton(pointer)) {
 			if (mouseDownOnButton) {
 				changeBtnState(BtnState.HOVER_SELECTED, CLOSE_BTN_INDEX);
 			} else {
 				changeBtnState(BtnState.HOVER, CLOSE_BTN_INDEX);
 			}
 			insideAButton = true;
 		} else if (getTitleBarRenderer().isInsideMaximizeButton(pointer)) {
 			if (mouseDownOnButton) {
 				changeBtnState(BtnState.HOVER_SELECTED, MAX_BTN_INDEX);
 			} else {
 				changeBtnState(BtnState.HOVER, MAX_BTN_INDEX);
 			}
 			insideAButton = true;
 		} else if (getTitleBarRenderer().isInsideMinimizeButton(pointer)) {
 			if (mouseDownOnButton) {
 				changeBtnState(BtnState.HOVER_SELECTED, MIN_BTN_INDEX);
 			} else {
 				changeBtnState(BtnState.HOVER, MIN_BTN_INDEX);
 			}
 			insideAButton = true;
 		}
 		if (!insideAButton) {
 			mouseDownOnButton = false;
 		}
 
 		final boolean redraw = updateRenderer();
 		if (redraw) {
 			redrawButtons(e);
 		}
 	}
 
 	private void redrawButtons(final MouseEvent e) {
 		final Control control = (Control) e.getSource();
 		if (!control.isDisposed()) {
 			final Rectangle buttonBounds = getTitleBarRenderer().getAllButtonsBounds();
 			control.redraw(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height, false);
 		}
 	}
 
 	private boolean updateRenderer() {
 
 		boolean changed = false;
 
 		for (int i = 0; i < btnStates.length; i++) {
 			final boolean hover = btnStates[i] == BtnState.HOVER;
 			final boolean pressed = btnStates[i] == BtnState.HOVER_SELECTED && mouseDownOnButton;
 			switch (i) {
 			case CLOSE_BTN_INDEX:
 				if (getTitleBarRenderer().isCloseButtonHover() != hover) {
 					getTitleBarRenderer().setCloseButtonHover(hover);
 					changed = true;
 				}
 				if (getTitleBarRenderer().isCloseButtonPressed() != pressed) {
 					getTitleBarRenderer().setCloseButtonPressed(pressed);
 					changed = true;
 				}
 				break;
 			case MAX_BTN_INDEX:
 				if (getTitleBarRenderer().isMaximizedButtonHover() != hover) {
 					getTitleBarRenderer().setMaximizedButtonHover(hover);
 					changed = true;
 				}
 				if (getTitleBarRenderer().isMaximizedButtonPressed() != pressed) {
 					getTitleBarRenderer().setMaximizedButtonPressed(pressed);
 					changed = true;
 				}
 				break;
 			case MIN_BTN_INDEX:
 				if (getTitleBarRenderer().isMinimizedButtonHover() != hover) {
 					getTitleBarRenderer().setMinimizedButtonHover(hover);
 					changed = true;
 				}
 				if (getTitleBarRenderer().isMinimizedButtonPressed() != pressed) {
 					getTitleBarRenderer().setMinimizedButtonPressed(pressed);
 					changed = true;
 				}
 				break;
 			default:
 				throw new AssertionError("Illegal button index: " + i); //$NON-NLS-1$
 			}
 		}
 		return changed;
 	}
 
 	private void updateCursor(final MouseEvent e) {
 
 		final Control control = (Control) e.getSource();
 		// avoids widget is disposed exception on close
 		if (!control.isDisposed()) {
 			final Point pointer = new Point(e.x, e.y);
 			if (moveInside && getTitleBarRenderer().isInsideMoveArea(pointer)) {
 				if (move) {
 					showGrabCursor(control);
 				} else {
 					showHandCursor(control);
 				}
 			} else {
 				if (!move) {
 					showDefaultCursor(control);
 				}
 			}
 		}
 	}
 
 	public void mouseDoubleClick(final MouseEvent e) {
 		// unused
 	}
 
 	public void mouseDown(final MouseEvent e) {
 		mouseDownOnButton = true;
 		updateButtonStates(e);
 		if (!mouseDownOnButton) {
 			final Point pointer = new Point(e.x, e.y);
 			if (getTitleBarRenderer().isInsideMoveArea(pointer)) {
 				move = true;
 				moveStartPoint = pointer;
 			} else {
 				move = false;
 			}
 		}
 		updateCursor(e);
 	}
 
 	/**
 	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseUp(final MouseEvent e) {
 		final Point pointer = new Point(e.x, e.y);
 
 		if (mouseDownOnButton && (e.getSource() instanceof Control)) {
 			final Control control = (Control) e.getSource();
 			final Shell shell = getShell(control);
 			if (shell != null) {
 				if (getTitleBarRenderer().isInsideCloseButton(pointer)) {
 					if (btnStates[CLOSE_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 						shell.close();
 					}
 				} else if (getTitleBarRenderer().isInsideMaximizeButton(pointer)) {
 					if (btnStates[MAX_BTN_INDEX] == BtnState.HOVER_SELECTED) {
						SHELL_HELPER.maximizeRestore(shell);
 					}
 				} else if (getTitleBarRenderer().isInsideMinimizeButton(pointer)) {
 					if (btnStates[MIN_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 						shell.setMinimized(true);
 					}
 				}
 			}
 		}
 
 		mouseDownOnButton = false;
 		updateButtonStates(e);
 		move = false;
 		updateCursor(e);
 	}
 
 	public void mouseEnter(final MouseEvent e) {
 		updateButtonStates(e);
 		moveInside = true;
 		move = false;
 		updateCursor(e);
 	}
 
 	public void mouseExit(final MouseEvent e) {
 		updateButtonStates(e);
 		moveInside = false;
 		move = false;
 		updateCursor(e);
 	}
 
 	public void mouseHover(final MouseEvent e) {
 		// unused
 	}
 
 	public void mouseMove(final MouseEvent e) {
 		updateButtonStates(e);
 		if (move) {
 			move(e);
 		}
 		updateCursor(e);
 	}
 
 	private void move(final MouseEvent e) {
 		final boolean wasMaximized = ShellHelper.isShellMaximzed();
 		final Point moveEndPoint = new Point(e.x, e.y);
 		final Control control = (Control) e.getSource();
 		final Shell shell = getShell(control);
 		final int xMove = moveStartPoint.x - moveEndPoint.x;
 		final int yMove = moveStartPoint.y - moveEndPoint.y;
 		final int x = shell.getLocation().x - xMove;
 		final int y = shell.getLocation().y - yMove;
 		shell.setLocation(x, y);
 		if (wasMaximized != ShellHelper.isShellMaximzed()) {
 			redrawButtons(e);
 		}
 	}
 
 	private Shell getShell(Control control) {
 		Shell result = null;
 		while (control != null && result == null) {
 			if (control instanceof Shell) {
 				result = (Shell) control;
 			} else {
 				control = control.getParent();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Sets the hand cursor for the given control.
 	 * 
 	 * @param control
 	 */
 	private void showHandCursor(final Control control) {
 		if (handCursor == null) {
 			handCursor = createHandCursor(control.getDisplay());
 		}
 		setCursor(control, handCursor);
 	}
 
 	/**
 	 * Sets the grab cursor for the given control.
 	 * 
 	 * @param control
 	 */
 	private void showGrabCursor(final Control control) {
 		if (grabCursor == null) {
 			grabCursor = createGrabCursor(control.getDisplay());
 		}
 		setCursor(control, grabCursor);
 	}
 
 	/**
 	 * Sets the default cursor for the given control.
 	 * 
 	 * @param shell
 	 */
 	private void showDefaultCursor(final Control control) {
 		if (defaultCursor == null) {
 			defaultCursor = new Cursor(control.getDisplay(), SWT.CURSOR_ARROW);
 		}
 		setCursor(control, defaultCursor);
 	}
 
 	/**
 	 * Sets the given cursor for the control
 	 * 
 	 * @param control
 	 * @param cursor
 	 *            new cursor
 	 */
 	private void setCursor(final Control control, final Cursor cursor) {
 		if (!SwtUtilities.isDisposed(control)) {
 			if ((cursor != null) && (control.getCursor() != cursor)) {
 				control.setCursor(cursor);
 			}
 		}
 	}
 
 	private Cursor createHandCursor(final Display display) {
 		final String key = LnfKeyConstants.TITLELESS_SHELL_HAND_IMAGE;
 		final Image image = LnfManager.getLnf().getImage(key);
 		return SWTFacade.getDefault().createCursor(display, image, SWT.CURSOR_HAND);
 	}
 
 	private Cursor createGrabCursor(final Display display) {
 		final String key = LnfKeyConstants.TITLELESS_SHELL_GRAB_IMAGE;
 		final Image image = LnfManager.getLnf().getImage(key);
 		return SWTFacade.getDefault().createCursor(display, image, SWT.CURSOR_HAND);
 	}
 
 	public void dispose() {
 		SwtUtilities.dispose(handCursor);
 		SwtUtilities.dispose(grabCursor);
 		SwtUtilities.dispose(defaultCursor);
 	}
 
 }
