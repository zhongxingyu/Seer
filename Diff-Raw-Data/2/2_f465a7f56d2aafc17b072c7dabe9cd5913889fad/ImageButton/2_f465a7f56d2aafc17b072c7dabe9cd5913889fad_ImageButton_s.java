 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import java.util.Arrays;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.TypedListener;
 
 /**
  * A button with only an image. (No (button) border, no text). If the button has
  * the style {@code SWT.HOT}, the button has a border and a background like
  * other SWT buttons if the mouse pointer is over the button (hot/hover).
  * <p>
  * The button can have different image for different button states (e.g. pressed
  * or disabled).
  * 
  * @since 2.0
  * 
  */
 public class ImageButton extends Composite {
 
 	private static final Point DEF_HORIZONTAL_MARGIN = new Point(0, 0);
 	private static final Point DEF_HOVER_BUTTON_HORIZONTAL_MARGIN = new Point(12, 12);
 	private static final int IMAGE_INDEX = 0;
 	private static final int PRESSED_IMAGE_INDEX = 1; // p
 	private static final int FOCUSED_IMAGE_INDEX = 2; // f
 	private static final int DISABLED_IMAGE_INDEX = 3; // d
 	private static final int HOVER_IMAGE_INDEX = 4; // h
 	private static final int HOVER_FOCUSED_IMAGE_INDEX = 5; // hp
 	private Image[] images = { null, null, null, null, null, null };
 
 	private static int idealHeight = -1;
 
 	private boolean useIdealHeight;
 	private boolean pressed;
 	private boolean hover;
 	private boolean focused;
 	private DisposeListener disposeListener;
 	private PaintListener paintListener;
 	private ButtonMouseListener mouseListener;
 	private FocusListener focusListener;
 	private TraverseListener traverseListener;
 	private ButtonKeyListener keyListener;
 	private Button hoverButton;
 	private Point horizontalMargin = DEF_HORIZONTAL_MARGIN;
 
 	/**
 	 * Creates a new instance of {@code ImageButton}, initializes the button
 	 * states and adds listeners.
 	 * 
 	 * @param parent
 	 *            a widget which will be the parent of the new
 	 *            {@code ImageButton} (cannot be null)
 	 * @param style
 	 *            the style of widget to construct; SWT.HOT adds a button border
 	 *            and buttons background that is only visible if the mouse
 	 *            pointer is over the {@code ImageButton}.
 	 */
 	public ImageButton(Composite parent, int style) {
 
 		super(parent, style | SWT.DOUBLE_BUFFERED);
 
 		useIdealHeight = false;
 		pressed = false;
 		hover = false;
 		focused = false;
 
 		if (hasHotStyle()) {
 			setHorizontalMargin(DEF_HOVER_BUTTON_HORIZONTAL_MARGIN);
 			setLayout(new FormLayout());
 			addHoverButton();
 		}
 
 		addListeners();
 
 	}
 
 	/**
 	 * Returns whether the style of the button has {@code SWT.HOT}.
 	 * 
 	 * @return {@code true} if style has {@code SWT.HOT}; otherwise
 	 *         {@code false}
 	 */
 	private boolean hasHotStyle() {
 
 		int style = getStyle();
 		return (style & SWT.HOT) == SWT.HOT;
 
 	}
 
 	/**
 	 * Adds the "hover" button. The hover button is only visible if the mouse
 	 * pointer is over this UI control.
 	 */
 	private void addHoverButton() {
 		hoverButton = new Button(this, SWT.PUSH);
 		FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0);
 		data.top = new FormAttachment(0, 0);
 		data.bottom = new FormAttachment(100, 0);
 		hoverButton.setLayoutData(data);
 		hoverButton.setVisible(false);
 	}
 
 	/**
 	 * Adds listeners to this {@code ImageButton} and to the "hover" button (if
 	 * exists).
 	 */
 	private void addListeners() {
 
 		paintListener = new PaintDelegation();
 		addPaintListener(paintListener);
 
 		mouseListener = new ButtonMouseListener();
 		addMouseListener(mouseListener);
 		addMouseTrackListener(mouseListener);
 		addMouseMoveListener(mouseListener);
 		if (hoverButton != null) {
 			hoverButton.addMouseListener(mouseListener);
 			hoverButton.addMouseTrackListener(mouseListener);
 			hoverButton.addMouseMoveListener(mouseListener);
 		}
 
 		focusListener = new ButtonFocusListener();
 		addFocusListener(focusListener);
 
 		keyListener = new ButtonKeyListener();
 		addKeyListener(keyListener);
 
 		traverseListener = new TraverseListener() {
 			public void keyTraversed(TraverseEvent e) {
 				e.doit = true;
 			}
 		};
 		addTraverseListener(traverseListener);
 
 		disposeListener = new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				onDispose(e);
 			}
 		};
 		addDisposeListener(disposeListener);
 
 	}
 
 	/**
 	 * Removes all listeners form this {@code ImageButton} and from the "hover"
 	 * button (if exists).
 	 */
 	private void removeListeners() {
 
 		if (disposeListener != null) {
 			removeDisposeListener(disposeListener);
 			disposeListener = null;
 		}
 
 		if (traverseListener != null) {
 			removeTraverseListener(traverseListener);
 			traverseListener = null;
 		}
 
 		if (paintListener != null) {
 			removePaintListener(paintListener);
 			paintListener = null;
 		}
 
 		if (focusListener != null) {
 			removeFocusListener(focusListener);
 			focusListener = null;
 		}
 
 		if (mouseListener != null) {
 			if (hoverButton != null) {
 				hoverButton.removeMouseListener(mouseListener);
 				hoverButton.removeMouseTrackListener(mouseListener);
 				hoverButton.removeMouseMoveListener(mouseListener);
 			}
 			removeMouseListener(mouseListener);
 			removeMouseTrackListener(mouseListener);
 			removeMouseMoveListener(mouseListener);
 			mouseListener = null;
 		}
 
 		if (keyListener != null) {
 			removeKeyListener(keyListener);
 			keyListener = null;
 		}
 
 	}
 
 	/**
 	 * After the widget was disposed all listeners will be removed and the array
 	 * with the images will be cleared.
 	 * 
 	 * @param event
 	 *            an event containing information about the dispose
 	 */
 	private void onDispose(DisposeEvent event) {
 
 		if (event.widget != this) {
 			return;
 		}
 		removeListeners();
 		Arrays.fill(images, null);
 
 	}
 
 	/**
 	 * Paints the image of this {@code ImageButton}.
 	 * 
 	 * @param event
 	 *            e an event containing information about the paint
 	 */
 	private void onPaint(PaintEvent event) {
 
 		if (hoverButton != null && hoverButton.isVisible()) {
 			return;
 		}
 
 		Image image = getImageToDraw();
 		if (image != null) {
 			Point pos = computeImagePos(event, image);
 			GC gc = event.gc;
 			gc.drawImage(image, pos.x, pos.y);
 		}
 
 	}
 
 	/**
 	 * Computes the position of the image.
 	 * 
 	 * @param event
 	 *            e an event containing information about the paint
 	 * @param image
 	 *            the image to draw
 	 * @return position of image
 	 */
 	private Point computeImagePos(PaintEvent event, Image image) {
 
 		int x = 0;
 		int y = 0;
 
 		if ((image != null) && (event != null)) {
 			Rectangle imgBounds = image.getBounds();
 			x = (event.width - imgBounds.width) / 2;
 			if (x < 0) {
 				x = 0;
 			}
 			y = (event.height - imgBounds.height) / 2;
 			if (y < 0) {
 				y = 0;
 			}
 			if (hasHotStyle() && ((event.height % 2) != 0)) {
 				y++;
 			}
 		}
 
 		return new Point(x, y);
 
 	}
 
 	/**
 	 * Returns the image that will be draw according to the current state of the
 	 * button.
 	 * 
 	 * @return image to draw
 	 */
 	private Image getImageToDraw() {
 
 		Image imageToDraw = null;
 
 		if (!isEnabled()) {
 			imageToDraw = getDisabledImage();
 			if (imageToDraw == null) {
 				imageToDraw = getImage();
 			}
 			return imageToDraw;
 		}
 
 		if (isPressed()) {
 			imageToDraw = getPressedImage();
 			if (imageToDraw == null) {
 				imageToDraw = getImage();
 			}
 			return imageToDraw;
 		}
 
 		if (isHover()) {
 			if (isFocused()) {
 				imageToDraw = getHoverFocusedImage();
 			}
 			if (imageToDraw == null) {
 				imageToDraw = getHoverImage();
 			}
 			if (imageToDraw == null) {
 				imageToDraw = getImage();
 			}
 
 			return imageToDraw;
 		}
 
 		if (isFocused()) {
 			imageToDraw = getFocusedImage();
 		}
 		if (imageToDraw == null) {
 			imageToDraw = getImage();
 		}
 
 		return imageToDraw;
 
 	}
 
 	/**
 	 * Computes the size of this {@code ImageButton} according the size of the
 	 * image (the maximal widths and height of the images).
 	 * 
 	 * @param wHint
 	 *            hint for width
 	 * @param hHint
 	 *            hint for height
 	 * @param changed
 	 *            <i><i/>
 	 * 
 	 * @return button size
 	 */
 	@Override
 	public Point computeSize(int wHint, int hHint, boolean changed) {
 
 		checkWidget();
 
 		Point size = new Point(0, 0);
 		if (isUseIdealHeight()) {
 			size.y = getIdealHeight();
 		}
 		for (Image oneImage : images) {
 			if ((oneImage != null) && (!oneImage.isDisposed())) {
 				Rectangle bounds = oneImage.getBounds();
 				size.x = Math.max(size.x, bounds.width);
 				size.y = Math.max(size.y, bounds.height);
 			}
 		}
 
 		if (hoverButton != null) {
 			Point btnSize = hoverButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 			if (size.x < btnSize.x) {
 				size.x = btnSize.x;
 			}
 			if (size.y < btnSize.y) {
 				size.y = btnSize.y;
 			}
 		}
 
 		size.x += getHorizontalMargin().x;
 		size.x += getHorizontalMargin().y;
 
 		if (wHint != SWT.DEFAULT) {
 			size.x = wHint;
 		}
 		if (hHint != SWT.DEFAULT) {
 			size.y = hHint;
 		}
 
 		return size;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setBackground(Color color) {
 		super.setBackground(color);
 		if (hoverButton != null) {
 			hoverButton.setBackground(color);
 		}
 	}
 
 	/**
 	 * Shows or hides the "hover" button depending in the hover state.
 	 */
 	private void updateHoverButton() {
 		if (hoverButton != null) {
 			boolean visible = isHover() || isPressed();
 			if (visible != hoverButton.isVisible()) {
 				hoverButton.setVisible(visible);
 			}
 			if (hoverButton.isVisible()) {
 				hoverButton.setImage(getImageToDraw());
 			}
 		}
 	}
 
 	/**
 	 * Adds the given to the collection of listeners who will be notified when
 	 * this {@code ImageButton} was selected.
 	 * 
 	 * @param listener
 	 *            listener to add
 	 */
 	public void addSelectionListener(SelectionListener listener) {
 		checkWidget();
 		if (listener == null) {
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		}
 		TypedListener typedListener = new TypedListener(listener);
 		addListener(SWT.Selection, typedListener);
 		addListener(SWT.DefaultSelection, typedListener);
 	}
 
 	/**
 	 * Removes the given from the collection of listeners who will be notified
 	 * when this {@code ImageButton} was selected.
 	 * 
 	 * @param listener
 	 *            listener to remove
 	 */
 	public void removeSelectionListener(SelectionListener listener) {
 		checkWidget();
 		if (listener == null) {
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		}
 		removeListener(SWT.Selection, listener);
 		removeListener(SWT.DefaultSelection, listener);
 	}
 
 	/**
 	 * Returns the ideal height of an image button according to the height of a
 	 * push button.
 	 * 
 	 * @return ideal height
 	 */
 	private int getIdealHeight() {
 		if (idealHeight < 0) {
 			Button button = new Button(this, SWT.PUSH);
 			idealHeight = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
 			button.dispose();
 		}
 		return idealHeight;
 	}
 
 	/**
 	 * Sets whether the ideal height should or shouldn't be used for this
 	 * {@code ImageButton}. The {@code ImageButton} will have the same height as
 	 * other push buttons.<br>
 	 * 
 	 * @param useIdealHeight
 	 *            {@code true} use ideal height; otherwise {@code false}
 	 */
 	public void setUseIdealHight(boolean useIdealHeight) {
 		this.useIdealHeight = useIdealHeight;
 	}
 
 	/**
 	 * Returns whether the ideal height should or shouldn't be used for this
 	 * {@code ImageButton}. The {@code ImageButton} will have the same height as
 	 * other push buttons.
 	 * 
 	 * @return useIdealHight {@code true} use ideal height; otherwise
 	 *         {@code false}
 	 */
 	public boolean isUseIdealHeight() {
 		return useIdealHeight;
 	}
 
 	/**
 	 * Returns the standard image of the button.
 	 * 
 	 * @return standard image
 	 */
 	public Image getImage() {
 		return images[IMAGE_INDEX];
 	}
 
 	/**
 	 * Sets the standard image of the button
 	 * 
 	 * @param image
 	 *            the image to set
 	 */
 	public void setImage(Image image) {
 		if (image != this.images[IMAGE_INDEX]) {
 			images[IMAGE_INDEX] = image;
 			redraw();
 		}
 	}
 
 	/**
 	 * Returns the image of the button, if it is pressed.
 	 * 
 	 * @return pressed image
 	 */
 	public Image getPressedImage() {
 		return images[PRESSED_IMAGE_INDEX];
 	}
 
 	/**
 	 * Sets the image of the button, if it is pressed.
 	 * 
 	 * @param image
 	 *            the image to set
 	 */
 	public void setPressedImage(Image image) {
 		images[PRESSED_IMAGE_INDEX] = image;
 	}
 
 	/**
 	 * Returns the image of the button, if it has the focus.
 	 * 
 	 * @return focused image
 	 */
 	public Image getFocusedImage() {
 		return images[FOCUSED_IMAGE_INDEX];
 	}
 
 	/**
 	 * Sets the image of the button, if it has the focus.
 	 * 
 	 * @param image
 	 *            the image to set
 	 */
 	public void setFocusedImage(Image image) {
 		images[FOCUSED_IMAGE_INDEX] = image;
 	}
 
 	/**
 	 * Returns the image of the button, if it is disabled.
 	 * 
 	 * @return disabled image
 	 */
 	public Image getDisabledImage() {
 		return images[DISABLED_IMAGE_INDEX];
 	}
 
 	/**
 	 * Sets the image of the button, if it is disabled.
 	 * 
 	 * @param image
 	 *            the image to set
 	 */
 	public void setDisabledImage(Image image) {
 		images[DISABLED_IMAGE_INDEX] = image;
 	}
 
 	/**
 	 * Returns the image of the button, if the mouse pointer is over it.
 	 * 
 	 * @return hover image
 	 */
 	public Image getHoverImage() {
 		return images[HOVER_IMAGE_INDEX];
 	}
 
 	/**
 	 * Sets the image of the button, if the mouse pointer is over it.
 	 * 
 	 * @param image
 	 *            the image to set
 	 */
 	public void setHoverImage(Image image) {
 		images[HOVER_IMAGE_INDEX] = image;
 	}
 
 	/**
 	 * Returns the image of the button, if the mouse pointer is over it and the
 	 * it has the focus.
 	 * 
 	 * @return hover and focused image
 	 */
 	public Image getHoverFocusedImage() {
 		return images[HOVER_FOCUSED_IMAGE_INDEX];
 	}
 
 	/**
 	 * Sets the image of the button, if the mouse pointer is over it and the it
 	 * has the focus.
 	 * 
 	 * @param image
 	 *            the image to set
 	 */
 	public void setHoverFocusedImage(Image image) {
 		images[HOVER_FOCUSED_IMAGE_INDEX] = image;
 	}
 
 	/**
 	 * Sets whether the button is pressed.
 	 * 
 	 * @param pressed
 	 *            {@code true} if button is pressed; otherwise {@code false}
 	 */
 	private void setPressed(boolean pressed) {
 		if (this.pressed != pressed) {
 			this.pressed = pressed;
 			redraw();
 		}
 	}
 
 	/**
 	 * Returns whether the button is pressed.
 	 * 
 	 * @return {@code true} if button is pressed; otherwise {@code false}
 	 */
 	private boolean isPressed() {
 		return pressed;
 	}
 
 	/**
 	 * Sets whether the mouse pointer is or isn't over the button.
 	 * 
 	 * @param hover
 	 *            {@code true} if the mouse point is over the button; otherwise
 	 *            {@code false}
 	 * 
 	 */
 	private void setHover(boolean hover) {
 		if (this.hover != hover) {
 			this.hover = hover;
 			redraw();
 		}
 	}
 
 	/**
 	 * Returns whether the mouse pointer is or isn't over the button.
 	 * 
 	 * @return {@code true} if the mouse point is over the button; otherwise
 	 *         {@code false}
 	 */
 	private boolean isHover() {
 		return hover;
 	}
 
 	/**
 	 * Sets whether the button has the focus or hasn't the focus.
 	 * 
 	 * @param focused
 	 *            {@code true} if the button has the focus; otherwise
 	 *            {@code false}
 	 */
 	private void setFocused(boolean focused) {
 		if (isFocused() != focused) {
 			this.focused = focused;
 			redraw();
 		}
 	}
 
 	/**
 	 * Returns whether the button has the focus or hasn't the focus.
 	 * 
 	 * @return {@code true} if the button has the focus; otherwise {@code false}
 	 */
 	private boolean isFocused() {
 		return focused;
 	}
 
 	/**
 	 * Sets the left and right margin between button border and image.
 	 * 
 	 * @param horizontalMargin
 	 *            left and right margin
 	 * 
 	 */
 	public void setHorizontalMargin(Point horizontalMargin) {
 		this.horizontalMargin = horizontalMargin;
 	}
 
 	/**
 	 * Returns the left and right margin between button border and image.
 	 * 
 	 * @return left and right margin
 	 */
 	public Point getHorizontalMargin() {
 		return horizontalMargin;
 	}
 
 	/**
 	 * Presses the button after the space key was pressed and fires a selection
 	 * event after the space key was released.
 	 */
 	private final class ButtonKeyListener implements KeyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void keyPressed(KeyEvent e) {
 			if (!isEnabled()) {
 				return;
 			}
 			if (!ignore(e)) {
 				setPressed(true);
 				updateHoverButton();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void keyReleased(KeyEvent e) {
 			if (!isEnabled()) {
 				return;
 			}
 			if (!ignore(e)) {
 				if (isPressed()) {
 					Event event = new Event();
 					notifyListeners(SWT.Selection, event);
 				}
 				setPressed(false);
 				updateHoverButton();
 			}
 		}
 
 		/**
 		 * Ignores mouse events if the component is null, not enabled, or the
 		 * event is not associated with the left mouse button.
 		 */
 		private boolean ignore(KeyEvent e) {
 			return e.character != ' ';
 		}
 
 	}
 
 	/**
 	 * This listener paints the {@code ImageButton} after a pint event was
 	 * fired.
 	 */
 	private class PaintDelegation implements PaintListener {
 
 		/**
 		 * {@inheritDoc}
 		 * <p>
 		 * Paints the {@code ImageButton}.
 		 */
 		public void paintControl(PaintEvent e) {
 			onPaint(e);
 		}
 
 	}
 
 	/**
 	 * Listener of all mouse events.
 	 */
 	private class ButtonMouseListener implements MouseListener, MouseTrackListener, MouseMoveListener {
 
 		/**
 		 * {@inheritDoc}
 		 * <p>
 		 * Fires a selection event if the button is pressed and the mouse
 		 * pointer is over the button.<br>
 		 * Removes the pressed state of the button.
 		 */
 		public void mouseUp(MouseEvent e) {
 			if (!isEnabled()) {
 				return;
 			}
 			if (!ignoreMouseButton(e) && !ignoreWidget(e)) {
 				if (isPressed() && isHover() && isOverButton(new Point(e.x, e.y))) {
 					Event event = new Event();
 					notifyListeners(SWT.Selection, event);
 				}
 				setPressed(false);
 				updateHoverButton();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * <p>
 		 * Sets the pressed state of the button.
 		 */
 		public void mouseDown(MouseEvent e) {
 			if (!isEnabled()) {
 				return;
 			}
 			if (!ignoreMouseButton(e) && !ignoreWidget(e)) {
 				setPressed(true);
 				updateHoverButton();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * <p>
 		 * Sets the hover state of the button.
 		 */
 		public void mouseEnter(MouseEvent e) {
 			if (!isEnabled()) {
 				return;
 			}
 			if (!ignoreWidget(e)) {
 				boolean oldHover = isHover();
 				setHover(true);
 				if (oldHover != isHover()) {
 					updateHoverButton();
 				}
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * <p>
 		 * Removes the hover state of the button.
 		 */
 		public void mouseExit(MouseEvent e) {
 			if (!isEnabled()) {
 				return;
 			}
 			if (!ignoreWidget(e)) {
 				boolean oldHover = isHover();
 				setHover(false);
 				if (oldHover != isHover()) {
 					updateHoverButton();
 				}
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseHover(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * <p>
 		 * Sets or removes the hover state of the button according the mouse
 		 * pointer is over the button.
 		 */
 		public void mouseMove(MouseEvent e) {
 			if (!isEnabled()) {
 				return;
 			}
 			if (!ignoreWidget(e)) {
 				if ((e.stateMask & SWT.BUTTON_MASK) != 0) {
 					boolean oldHover = isHover();
 					Point point = new Point(e.x, e.y);
 					if (isOverButton(point)) {
 						setPressed(true);
 					} else {
 						setPressed(false);
 					}
 					if (oldHover != isHover()) {
 						updateHoverButton();
 					}
 				}
 			}
 		}
 
 		/**
 		 * Ignores mouse events if the event is not associated with the left
 		 * mouse button.
 		 * 
 		 * @param e
 		 *            mouse event
 		 * @return {@code true} ignore event; otherwise {@code false}
 		 */
 		private boolean ignoreMouseButton(MouseEvent e) {
 			return e.button != 1;
 		}
 
 		/**
 		 * Ignores mouse events if the source widget is "invisible"
 		 * 
 		 * @param e
 		 *            mouse event
 		 * @return {@code true} ignore event; otherwise {@code false}
 		 */
 		private boolean ignoreWidget(MouseEvent e) {
 			if (hoverButton != null) {
 				if (hoverButton.isVisible()) {
 					return e.widget != hoverButton;
 				} else {
 					return e.widget == hoverButton;
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * Returns whether the given point is inside or outside the bounds of
 		 * the button.
 		 * 
 		 * @param point
 		 *            position of the mouse pointer
 		 * @return {@code true} if point is inside the button; otherwise
 		 *         {@code false}
 		 */
 		private boolean isOverButton(Point point) {
			return (point.x <= getBounds().width) && (point.y <= getBounds().height);
 		}
 
 	}
 
 	/**
 	 * Registers whether the {@code ImageButton} has or hasn't the focus.
 	 */
 	private class ButtonFocusListener implements FocusListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void focusGained(FocusEvent e) {
 			setFocused(true);
 			updateHoverButton();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void focusLost(FocusEvent e) {
 			setFocused(false);
 			updateHoverButton();
 		}
 
 	}
 
 }
