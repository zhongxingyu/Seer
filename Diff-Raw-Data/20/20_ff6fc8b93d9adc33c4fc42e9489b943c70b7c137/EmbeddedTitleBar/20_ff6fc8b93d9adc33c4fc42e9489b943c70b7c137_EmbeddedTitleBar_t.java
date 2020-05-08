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
 
 import java.util.Collection;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.marker.Markable;
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.renderer.EmbeddedTitlebarRenderer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * Title bar of an embedded window (e.g. view of the current sub-module).
  */
 public class EmbeddedTitleBar extends Canvas {
 
 	private boolean windowActive;
 	private boolean pressed;
 	private boolean hover;
 	private boolean closeButtonPressed;
 	private boolean closeButtonHover;
 	private boolean closeable;
 	private Image image;
 	private String title;
 	private Collection<? extends IMarker> markers;
 
 	protected ListenerList<IEmbeddedTitleBarListener> titleBarListeners;
 
 	/**
 	 * Constructs a new instance of {@code EmbeddedTitleBar} given its parent
 	 * and a style value describing its behavior and appearance.
 	 * 
 	 * @param parent
 	 *            - a composite control which will be the parent of the new
 	 *            instance (cannot be null)
 	 * @param style
 	 *            - the style of control to construct
 	 */
 	public EmbeddedTitleBar(Composite parent, int style) {
 		super(parent, style | SWT.DOUBLE_BUFFERED);
 		addListeners();
 		titleBarListeners = new ListenerList<IEmbeddedTitleBarListener>(IEmbeddedTitleBarListener.class);
 	}
 
 	/**
 	 * Adds a {@code PaintListener} to this {@code EmbeddedTitleBar}.
 	 */
 	protected void addListeners() {
 
 		addPaintListener(new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				onPaint(e);
 			}
 		});
 
 	}
 
 	/**
 	 * Paints the title bar.<br>
 	 * Configures and calls the renderer that paints the title bar.
 	 * 
 	 * @param e
 	 *            - an event containing information about the paint
 	 */
 	private void onPaint(PaintEvent e) {
 
 		GC gc = e.gc;
 
 		// title bar
 		getLnfTitlebarRenderer().setActive(isWindowActive());
 		getLnfTitlebarRenderer().setCloseable(isCloseable());
 		getLnfTitlebarRenderer().setPressed(isPressed());
 		getLnfTitlebarRenderer().setHover(isHover());
 		getLnfTitlebarRenderer().setCloseButtonPressed(isCloseButtonPressed());
 		getLnfTitlebarRenderer().setCloseButtonHover(isCloseButtonHover());
 		getLnfTitlebarRenderer().setImage(getImage());
 		getLnfTitlebarRenderer().setTitle(getTitle());
 		getLnfTitlebarRenderer().setMarkers(getMarkers());
 		Point titlebarSize = getLnfTitlebarRenderer().computeSize(gc, getBounds().width, 0);
 		Rectangle titlebarBounds = new Rectangle(getBounds().x, 0, titlebarSize.x, titlebarSize.y);
 		getLnfTitlebarRenderer().setBounds(titlebarBounds);
 		getLnfTitlebarRenderer().paint(gc, this);
 
 	}
 
 	/**
 	 * Returns the renderer of the title bar.
 	 * 
 	 * @return renderer
 	 */
 	protected EmbeddedTitlebarRenderer getLnfTitlebarRenderer() {
 
 		EmbeddedTitlebarRenderer renderer = (EmbeddedTitlebarRenderer) LnfManager.getLnf().getRenderer(
 				ILnfKeyConstants.SUB_MODULE_VIEW_TITLEBAR_RENDERER);
 		if (renderer == null) {
 			renderer = new EmbeddedTitlebarRenderer();
 		}
 		return renderer;
 
 	}
 
 	public void addEmbeddedTitleBarListener(IEmbeddedTitleBarListener listener) {
 		titleBarListeners.add(listener);
 	}
 
 	public void removeEmbeddedTitleBarListener(IEmbeddedTitleBarListener listener) {
 		titleBarListeners.remove(listener);
 	}
 
 	/**
 	 * @see org.eclipse.swt.widgets.Control#getSize()
 	 */
 	@Override
 	public Point getSize() {
 		Point size = super.getSize();
 		GC gc = new GC(this);
 		size = getLnfTitlebarRenderer().computeSize(gc, size.x, size.y);
 		gc.dispose();
 		return size;
 	}
 
 	/**
 	 * @see org.eclipse.swt.widgets.Control#isFocusControl()
 	 */
 	@Override
 	public boolean isFocusControl() {
 		return false;
 	}
 
 	/**
 	 * @return the windowActive
 	 */
 	public boolean isWindowActive() {
 		return windowActive;
 	}
 
 	/**
 	 * @param windowActive
 	 *            the windowActive to set
 	 */
 	public void setWindowActive(boolean active) {
 		if (hasChanged(this.windowActive, active)) {
 			this.windowActive = active;
 			redraw();
 		}
 	}
 
 	/**
 	 * @return the pressed
 	 */
 	public boolean isPressed() {
 		return pressed;
 	}
 
 	/**
 	 * @param pressed
 	 *            the pressed to set
 	 */
 	public void setPressed(boolean pressed) {
 		if (hasChanged(this.pressed, pressed)) {
 			this.pressed = pressed;
 			redraw();
 		}
 	}
 
 	/**
 	 * @return the hover
 	 */
 	public boolean isHover() {
 		return hover;
 	}
 
 	/**
 	 * @param hover
 	 *            the hover to set
 	 */
 	public void setHover(boolean hover) {
 		if (hasChanged(this.hover, hover)) {
 			this.hover = hover;
 			redraw();
 		}
 	}
 
 	/**
 	 * @return the closeable
 	 */
 	public boolean isCloseable() {
 		return closeable;
 	}
 
 	/**
 	 * @param closeable
 	 *            the closeable to set
 	 */
 	public void setCloseable(boolean closeable) {
 		if (hasChanged(this.closeable, closeable)) {
 			this.closeable = closeable;
 			redraw();
 		}
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title
 	 *            the title to set
 	 */
 	public void setTitle(String title) {
 		if (hasChanged(this.title, title)) {
 			this.title = title;
 			redraw();
 		}
 	}
 
 	/**
 	 * @param image
 	 *            the image to set
 	 */
 	public void setImage(Image image) {
 		if (hasChanged(this.image, image)) {
 			this.image = image;
 			redraw();
 		}
 	}
 
 	/**
 	 * @return the image
 	 */
 	public Image getImage() {
 		return image;
 	}
 
 	public void setMarkers(Collection<? extends IMarker> markers) {
 		this.markers = markers;
 	}
 
 	public Collection<? extends IMarker> getMarkers() {
 		return markers;
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.ILnfRenderer#getMarkersOfType(java.lang.Class)
 	 */
 	public <T extends IMarker> Collection<T> getMarkersOfType(Class<T> type) {
 		return Markable.getMarkersOfType(getMarkers(), type);
 	}
 
 	protected boolean isOverClose(Point point) {
 
 		if (!isCloseable()) {
 			return false;
 		}
 
 		boolean inside = getLnfTitlebarRenderer().isInsideCloseButton(point);
 		return inside;
 
 	}
 
 	public boolean isTextClipped() {
 
 		EmbeddedTitlebarRenderer titlebarRenderer = getLnfTitlebarRenderer();
 		titlebarRenderer.setBounds(getBounds());
 		GC gc = new GC(this);
 		String clippedText = titlebarRenderer.getClippedText(gc, title);
 		gc.dispose();
 
 		return !title.equals(clippedText);
 
 	}
 
 	/**
 	 * Compares the two given values.
 	 * 
 	 * @param oldValue
 	 *            - old value
 	 * @param newValue
 	 *            - new value
 	 * @return true, if value has changed; otherwise false
 	 */
 	private boolean hasChanged(Object oldValue, Object newValue) {
		return (oldValue == null && newValue != null) || (oldValue != null && !oldValue.equals(newValue));
 	}
 
 	public void setCloseButtonPressed(boolean closeButtonPressed) {
 		if (hasChanged(this.closeButtonPressed, closeButtonPressed)) {
 			this.closeButtonPressed = closeButtonPressed;
 			redraw();
 		}
 	}
 
 	public boolean isCloseButtonPressed() {
 		return closeButtonPressed;
 	}
 
 	public void setCloseButtonHover(boolean closeButtonHover) {
 		if (hasChanged(this.closeButtonHover, closeButtonHover)) {
 			this.closeButtonHover = closeButtonHover;
 			redraw();
 		}
 	}
 
 	public boolean isCloseButtonHover() {
 		return closeButtonHover;
 	}
 
 }
