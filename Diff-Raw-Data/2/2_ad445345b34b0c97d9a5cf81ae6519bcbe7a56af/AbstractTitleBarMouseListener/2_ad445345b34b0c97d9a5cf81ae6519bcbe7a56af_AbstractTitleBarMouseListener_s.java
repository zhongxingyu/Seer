 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.renderer.AbstractTitleBarRenderer;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * TODO After any mouse operation a method of this listener is called.
  */
 public abstract class AbstractTitleBarMouseListener implements MouseListener, MouseTrackListener, MouseMoveListener {
 
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
 	private BtnState[] btnStates = new BtnState[BTN_COUNT];
 	private boolean mouseDownOnButton;
 	private boolean moveInside;
 	private boolean move;
 	private Point moveStartPoint;
 
 	public AbstractTitleBarMouseListener() {
 		resetBtnStates();
 		mouseDownOnButton = false;
 		move = false;
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
 	 *            - state to set
 	 * @param btnIndex
 	 *            - button index
 	 */
 	private void changeBtnState(BtnState newState, int btnIndex) {
 		if (newState != BtnState.NONE) {
 			resetBtnStates();
 		}
 		btnStates[btnIndex] = newState;
 	}
 
 	/**
 	 * Updates the states of the buttons.
 	 * 
 	 * @param e
 	 *            - mouse event
 	 */
 	private void updateButtonStates(MouseEvent e) {
 
 		Point pointer = new Point(e.x, e.y);
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
 
 		boolean redraw = false;
 		for (int i = 0; i < btnStates.length; i++) {
 			boolean hover = btnStates[i] == BtnState.HOVER;
 			boolean pressed = btnStates[i] == BtnState.HOVER_SELECTED && mouseDownOnButton;
 			switch (i) {
 			case CLOSE_BTN_INDEX:
 				if (getTitleBarRenderer().isCloseButtonHover() != hover) {
 					getTitleBarRenderer().setCloseButtonHover(hover);
 					redraw = true;
 				}
 				if (getTitleBarRenderer().isCloseButtonPressed() != pressed) {
 					getTitleBarRenderer().setCloseButtonPressed(pressed);
 					redraw = true;
 				}
 				break;
 			case MAX_BTN_INDEX:
 				if (getTitleBarRenderer().isMaximizedButtonHover() != hover) {
 					getTitleBarRenderer().setMaximizedButtonHover(hover);
 					redraw = true;
 				}
 				if (getTitleBarRenderer().isMaximizedButtonPressed() != pressed) {
 					getTitleBarRenderer().setMaximizedButtonPressed(pressed);
 					redraw = true;
 				}
 				break;
 			case MIN_BTN_INDEX:
 				if (getTitleBarRenderer().isMinimizedButtonHover() != hover) {
 					getTitleBarRenderer().setMinimizedButtonHover(hover);
 					redraw = true;
 				}
 				if (getTitleBarRenderer().isMinimizedButtonPressed() != pressed) {
 					getTitleBarRenderer().setMinimizedButtonPressed(pressed);
 					redraw = true;
 				}
 				break;
 			}
 		}
 
 		if (redraw) {
 			Control control = (Control) e.getSource();
 			if (!control.isDisposed()) {
 				Rectangle buttonBounds = getTitleBarRenderer().getAllButtonsBounds();
 				control.redraw(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height, false);
 			}
 		}
 
 	}
 
 	private void updateCursor(MouseEvent e) {
 
 		Control control = (Control) e.getSource();
 		// avoids widget is disposed exception on close
 		if (!control.isDisposed()) {
 			Point pointer = new Point(e.x, e.y);
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
 
 	/**
 	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseDoubleClick(MouseEvent e) {
 	}
 
 	/**
 	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseDown(MouseEvent e) {
 		mouseDownOnButton = true;
 		updateButtonStates(e);
 		if (!mouseDownOnButton) {
 			Point pointer = new Point(e.x, e.y);
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
 	public void mouseUp(MouseEvent e) {
 
 		Point pointer = new Point(e.x, e.y);
 
 		if (mouseDownOnButton && (e.getSource() instanceof Control)) {
 			Control control = (Control) e.getSource();
 			Shell shell = getShell(control);
 			if (shell != null) {
 				if (getTitleBarRenderer().isInsideCloseButton(pointer)) {
 					if (btnStates[CLOSE_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 						shell.close();
 					}
 				} else if (getTitleBarRenderer().isInsideMaximizeButton(pointer)) {
 					if (btnStates[MAX_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 						boolean maximized = shell.getMaximized();
 						shell.setMaximized(!maximized);
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
 
 	/**
 	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseEnter(MouseEvent e) {
 		updateButtonStates(e);
 		moveInside = true;
 		move = false;
 		updateCursor(e);
 	}
 
 	/**
 	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseExit(MouseEvent e) {
 		updateButtonStates(e);
 		moveInside = false;
 		move = false;
 		updateCursor(e);
 	}
 
 	/**
 	 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseHover(MouseEvent e) {
 		// unused
 	}
 
 	/**
 	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseMove(MouseEvent e) {
 		updateButtonStates(e);
 		if (move) {
 			move(e);
 		}
 		updateCursor(e);
 	}
 
 	private void move(MouseEvent e) {
 		Point moveEndPoint = new Point(e.x, e.y);
 		Control control = (Control) e.getSource();
 		Shell shell = getShell(control);
 		int xMove = moveStartPoint.x - moveEndPoint.x;
 		int yMove = moveStartPoint.y - moveEndPoint.y;
 		int x = shell.getLocation().x - xMove;
 		int y = shell.getLocation().y - yMove;
 		shell.setLocation(x, y);
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
 	private void showHandCursor(Control control) {
 		if (handCursor == null) {
 			handCursor = createCursor(control, ILnfKeyConstants.TITLELESS_SHELL_HAND_IMAGE);
 		}
 		setCursor(control, handCursor);
 	}
 
 	/**
 	 * Sets the grab cursor for the given control.
 	 * 
 	 * @param control
 	 */
 	private void showGrabCursor(Control control) {
 		if (grabCursor == null) {
 			grabCursor = createCursor(control, ILnfKeyConstants.TITLELESS_SHELL_GRAB_IMAGE);
 		}
 		setCursor(control, grabCursor);
 
 	}
 
 	/**
 	 * Sets the default cursor for the given control.
 	 * 
 	 * @param shell
 	 */
 	private void showDefaultCursor(Control control) {
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
 	 *            - new cursor
 	 */
 	private void setCursor(Control control, Cursor cursor) {
 		if (!SwtUtilities.isDisposed(control)) {
 			if ((cursor != null) && (control.getCursor() != cursor)) {
 				control.setCursor(cursor);
 			}
 		}
 	}
 
 	/**
 	 * Creates a cursor there for the corresponding image of the look and feel
 	 * is used.
 	 * 
 	 * @param control
 	 * @param lnfKey
 	 *            - look and feel key of the cursor image
 	 * @return cursor
 	 */
 	private Cursor createCursor(Control control, String lnfKey) {
 
 		Cursor cursor = null;
 
 		Image cursorImage = LnfManager.getLnf().getImage(lnfKey);
 		if (cursorImage != null) {
 			ImageData imageData = cursorImage.getImageData();
 			int x = imageData.width / 2;
 			int y = imageData.height / 2;
 			cursor = new Cursor(control.getDisplay(), imageData, x, y);
 		}
 		return cursor;
 
 	}
 
 	public void dispose() {
 		SwtUtilities.disposeResource(handCursor);
 		SwtUtilities.disposeResource(grabCursor);
 		SwtUtilities.disposeResource(defaultCursor);
 	}
 
 }
