 /*
 	ChibiPaint
     Copyright (c) 2006-2008 Marc Schefer
 
     This file is part of ChibiPaint.
 
     ChibiPaint is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ChibiPaint is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ChibiPaint. If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package chibipaint.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.lang.reflect.Method;
 import java.util.prefs.Preferences;
 
 import javax.swing.*;
 
 import chibipaint.*;
 import chibipaint.engine.*;
 import chibipaint.util.*;
 
 public class CPCanvas extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener,
 ComponentListener, KeyListener, CPController.ICPToolListener, CPController.ICPModeListener,
 CPArtwork.ICPArtworkListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	CPController controller;
 
 	// FIXME: this should not be public
 	public Image img;
 
 	BufferedImage checkerboardPattern;
 	MemoryImageSource imgSource;
 	CPRect updateRegion = new CPRect();
 
 	int[] buffer;
 	CPArtwork artwork;
 
 	boolean spacePressed = false;
 
 	// canvas transformations
 	float zoom = 1, minZoom = .05f, maxZoom = 16.f;
 	int offsetX, offsetY;
 	float canvasRotation = 0.f;
 	AffineTransform transform = new AffineTransform();
 	boolean applyToAllLayers = false;
 	boolean interpolation = false;
 
 	private int cursorX;
 
 	private int cursorY;
 	private int modifiers;
 	private int button;
 
 	boolean brushPreview = false;
 	Rectangle oldPreviewRect;
 
 	Cursor defaultCursor, hideCursor, moveCursor, crossCursor;
 	boolean cursorIn;
 
 	private boolean dontStealFocus = false;
 
 	// Grid options
 
 	// FIXME: these shouldn't be public
 	public int gridSize = 32;
 	public boolean showGrid = false;
 
 	//
 	// Modes system: modes control the way the GUI is reacting to the user input
 	// All the tools are implemented through modes
 	//
 
 	CPMode defaultMode = new CPDefaultMode();
 	CPMode colorPickerMode = new CPColorPickerMode();
 	CPMode moveCanvasMode = new CPMoveCanvasMode();
 	CPMode rotateCanvasMode = new CPRotateCanvasMode();
 	CPMode floodFillMode = new CPFloodFillMode();
 	CPMode rectSelectionMode = new CPRectSelectionMode();
 	CPMode moveToolMode = new CPMoveToolMode();
 
 	// this must correspond to the stroke modes defined in CPToolInfo
 	CPMode drawingModes[] = { new CPFreehandMode(), new CPLineMode(), new CPBezierMode(), };
 
 	CPMode curDrawMode = drawingModes[CPBrushInfo.SM_FREEHAND];
 	CPMode curSelectedMode = curDrawMode;
 	private CPMode activeMode = defaultMode;
 
 	CPSelection curSelection;
 
 	// Container with scrollbars
 	JPanel container;
 	JScrollBar horizScroll, vertScroll;
 
 	// Are we using old JTable or 1.2
 	boolean oldJTabletUsed;
 
 	private float lastPressure;
 
 	public void setArtwork(CPArtwork artwork) {
 		this.artwork = artwork;
 	}
 
 	public void initCanvas (CPController ctrl)
 	{
 		this.controller = ctrl;
 		artwork = ctrl.getArtwork();
 
 		buffer = artwork.getDisplayBM().getData();
 
 		int w = artwork.width;
 		int h = artwork.height;
 
 		imgSource = new MemoryImageSource(w, h, buffer, 0, w);
 		imgSource.setAnimated(true);
 		// imgSource.setFullBufferUpdates(false);
 		img = createImage(imgSource);
 		curSelection = new CPSelection (w, h);
 
 		ctrl.setCanvas(this);
 
 		int[] pixels = new int[16 * 16];
 		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
 		hideCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisiblecursor");
 		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
 		moveCursor = new Cursor(Cursor.MOVE_CURSOR);
 		crossCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
 
 		// Creates the checkboard pattern seen in transparent areas
 		checkerboardPattern = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
 		pixels = new int[64 * 64];
 		for (int j = 0; j < 64; j++) {
 			for (int i = 0; i < 64; i++) {
 				if ((i & 0x8) != 0 ^ (j & 0x8) != 0) {
 					pixels[i + j * 64] = 0xffffffff;
 				} else {
 					pixels[i + j * 64] = 0xffcccccc;
 				}
 			}
 		}
 		checkerboardPattern.setRGB(0, 0, 64, 64, pixels, 0, 64);
 
 		/*
 		 * KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false); Action escapeAction = new
 		 * AbstractAction() { public void actionPerformed(ActionEvent e) { controller.setAlpha((int)(.5f*255)); } };
 		 *
 		 * getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW). put(escapeKeyStroke, "SPACE"); getActionMap().put("SPACE",
 		 * escapeAction);
 		 */
 
 		Class<?> JTabletExtensionClass;
 		try {
 			JTabletExtensionClass = Class.forName ("cello.jtablet.installer.JTabletExtension");
 			try {
 				Class<?>[] params = new Class[2];
 				params[0] = Component.class;
 				params[1] = String.class;
 				Method checkCompatibility = JTabletExtensionClass.getMethod("checkCompatibility", params);
 				oldJTabletUsed = ! (((Boolean) checkCompatibility.invoke(JTabletExtensionClass, this, "1.2.0")).booleanValue ());
 			}
 			catch (Exception e)
 			{
 				System.out.format("Error happened during checking compatility JTablet 1.2");
 				System.exit (1);
 			}
 		} catch (ClassNotFoundException e) {
 			oldJTabletUsed = true;
 		}
 
 		// register as a listener for Mouse & MouseMotion events
 		if (oldJTabletUsed) // Otherwise listening of these events would be done by JTablet itself
 		{
 			addMouseListener(this);
 			addMouseMotionListener(this);
 		}
 		addMouseWheelListener(this);
 		addComponentListener(this);
 		addKeyListener(this);
 
 		controller.addToolListener(this);
 		controller.addModeListener(this);
 
 		artwork.addListener(this);
 
 		// We'll need to refresh the whole thing at least once
 		updateRegion = new CPRect(w, h);
 
 		// So that the tab key will work
 		setFocusTraversalKeysEnabled(false);
 
 		if (!oldJTabletUsed)
 		{
 			CPTablet2.connectToCanvas(this);
 
 			// This stuff is to fix bug with not disappearing brush preview while moving cursor on widgets while
 			// using tablet
 			// It's bug of nature unknown to me, that's why I fixed it in a little confusing kind of way.
 			// TODO: Maybe fix it a better way.
 			addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseExited(MouseEvent me) {
 					cursorIn = false;
 					brushPreview = false;
 					repaint ();
 				}
 				@Override
 				public void mouseEntered(MouseEvent me) {
 					brushPreview = true;
 					cursorIn = true;
 				}
 			});
 		}
 
 
		Timer timer = new Timer (50, new ActionListener () {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				curSelection.RaiseInitialDash ();
 				repaint (curSelection.GetBoundingBox (getThis ()));
 			}});
		timer.start();
 	}
 
 	public CPCanvas getThis ()
 	{
 		return this;
 	}
 
 	public void KillCanvas()
 	{
 		removeMouseListener(this);
 		removeMouseMotionListener(this);
 		removeMouseWheelListener(this);
 		removeComponentListener(this);
 		removeKeyListener(this);
 
 		controller.removeToolListener(this);
 		controller.removeModeListener(this);
 
 		artwork.removeListener(this);
 	}
 
 	public CPCanvas(CPController ctrl) {
 		initCanvas (ctrl);
 	}
 
 	@Override
 	public boolean isOpaque() {
 		return true;
 	}
 
 	// //////////////////////////////////////////////////////////////////////////////////////
 	// Container and ScrollBars
 	// //////////////////////////////////////////////////////////////////////////////////////
 
 	public JPanel getCanvasContainer() {
 		if (container != null) {
 			return container;
 		}
 
 		container = new JPanel();
 		container.setLayout(new GridBagLayout());
 
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.weightx = 1.;
 		gbc.weighty = 1.;
 		container.add(this, gbc);
 
 		vertScroll = new JScrollBar(Adjustable.VERTICAL);
 		gbc = new GridBagConstraints();
 		gbc.fill = GridBagConstraints.VERTICAL;
 		container.add(vertScroll, gbc);
 
 		horizScroll = new JScrollBar(Adjustable.HORIZONTAL);
 		gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		container.add(horizScroll, gbc);
 
 		updateScrollBars();
 
 		horizScroll.addAdjustmentListener(new AdjustmentListener() {
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 				Point p = getOffset();
 				p.x = - e.getValue();
 				setOffset(p);
 			}
 		});
 
 		vertScroll.addAdjustmentListener(new AdjustmentListener() {
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 				Point p = getOffset();
 				p.y = - e.getValue();
 				setOffset(p);
 			}
 		});
 
 		return container;
 	}
 
 	void updateScrollBars() {
 		if (horizScroll == null || vertScroll == null
 				|| horizScroll.getValueIsAdjusting() || vertScroll.getValueIsAdjusting() ) {
 			return;
 		}
 
 		if (img == null) {
 			horizScroll.setEnabled(false);
 			vertScroll.setEnabled(false);
 		}
 
 		Rectangle visibleRect = getRefreshArea(new CPRect(img.getWidth(null), img.getHeight(null)));
 		updateScrollBar(horizScroll, visibleRect.x, visibleRect.width, getWidth(), -getOffset().x);
 		updateScrollBar(vertScroll, visibleRect.y, visibleRect.height, getHeight(), -getOffset().y);
 	}
 
 
 	static void updateScrollBar(JScrollBar scroll, int visMin, int visWidth, int viewSize, int offset) {
 		if (visMin >= 0 && visMin + visWidth < viewSize) {
 			scroll.setEnabled(false);
 		} else {
 			scroll.setEnabled(true);
 
 			int xMin = Math.min(0, (visMin - viewSize / 4));
 			int xMax = Math.max(viewSize, visMin + visWidth + viewSize / 4);
 
 			scroll.setValues(offset, viewSize, xMin + offset, xMax + offset);
 			scroll.setBlockIncrement(Math.max(1, (int) (viewSize * .66)));
 			scroll.setUnitIncrement(Math.max(1, (int) (viewSize * .05)));
 		}
 	}
 
 	// //////////////////////////////////////////////////////////////////////////////////////
 	// painting methods
 	// //////////////////////////////////////////////////////////////////////////////////////
 
 	@Override
 	public void update(Graphics g) {
 		paint(g);
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		Graphics2D g2d = (Graphics2D) g;
 
 		g2d.setColor(new Color(0x606060));
 		g2d.fillRect(0, 0, getWidth(), getHeight());
 
 		if (!updateRegion.isEmpty()) {
 			artwork.fusionLayers();
 			imgSource.newPixels(updateRegion.left, updateRegion.top, updateRegion.getWidth(), updateRegion.getHeight());
 			updateRegion.makeEmpty();
 		}
 
 		int w = img.getWidth(null);
 		int h = img.getHeight(null);
 
 		Graphics2D g2doc = (Graphics2D) g2d.create();
 		g2doc.transform(transform);
 
 		// Draw the checkerboard pattern
 		// we'll draw the pattern over an area larger than the image
 		// and then remove the extra to avoid display problems
 		// when the displayed bitmap doesn't match exactly with the checkerboard area
 
 		GeneralPath path = getCheckerboardBackgroundPath(new Rectangle2D.Float(0, 0, w, h));
 
 		// get the bounding rect and make it a bit larger to be sure to include everything
 		Rectangle pathRect = path.getBounds();
 		pathRect.x -= 2;
 		pathRect.y -= 2;
 
 		g2d.setPaint(new TexturePaint(checkerboardPattern, new Rectangle(0, 0, 64, 64)));
 		g2d.fill(pathRect);
 
 		// Draw the image on the canvas
 
 		if (interpolation) {
 			RenderingHints hints = g2doc.getRenderingHints();
 			hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 			g2doc.addRenderingHints(hints);
 		}
 
 		g2doc.drawImage(img, 0, 0, w, h, 0, 0, img.getWidth(null), img.getHeight(null), null);
 
 		// Redraw over the checkerboard border, removing a just a little bit of the image to avoid display problems
 		path.append(pathRect, false);
 		path.setWindingRule(Path2D.WIND_EVEN_ODD);
 		g2d.setColor(new Color(0x606060));
 		g2d.fill(path);
 		/*
 
 		// This XOR mode guarantees contrast over all colors
 		g2d.setColor(Color.black);
 		g2d.setXORMode(new Color(0x808080));
 
 		// Draw selection
 		if (!artwork.getSelection().isEmpty()) {
 			Stroke stroke = g2d.getStroke();
 			g2d
 			.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
 					new float[] { 2f }, 0f));
 			g2d.draw(coordToDisplay(artwork.getSelection()));
 			g2d.setStroke(stroke);
 		}
 		 */
 
 		curSelection.drawItself (g2d, this);
 
 		// Draw grid
 		if (showGrid) {
 			Stroke stroke = g2d.getStroke();
 			g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, null, 0f));
 
 			CPRect size = artwork.getSize();
 			for (int i = gridSize - 1; i < size.getWidth(); i += gridSize) {
 				Point2D.Float p1 = coordToDisplay(new Point2D.Float(i, 0));
 				Point2D.Float p2 = coordToDisplay(new Point2D.Float(i, size.getHeight() - 1));
 				g2d.draw(new Line2D.Float(p1, p2));
 			}
 
 			for (int i = gridSize - 1; i < size.getHeight(); i += gridSize) {
 				Point2D.Float p1 = coordToDisplay(new Point2D.Float(0, i));
 				Point2D.Float p2 = coordToDisplay(new Point2D.Float(size.getWidth() - 1, i));
 				g2d.draw(new Line2D.Float(p1, p2));
 			}
 
 			g2d.setStroke(stroke);
 		}
 
 		// Additional drawing by the current mode
 		getActiveMode().paint(g2d);
 
 		// This bit of code is used to test repaint areas
 		/*
 		 * if((test++ & 16) == 0) { g.setColor(Color.magenta); Dimension dd = getSize();
 		 * g.fillRect(0,0,dd.width,dd.height); g.setColor(Color.black); }
 		 */
 
 		// Adding * for unsaved changes
 		if (controller.isRunningAsApplication ())
 			((CPControllerApplication) controller).updateChanges (artwork.getUndoList().size () > 0 ? artwork.getUndoList().getFirst() : null,
 					artwork.getRedoList().size () > 0 ? artwork.getRedoList().getFirst() : null);
 	}
 
 	private GeneralPath getCheckerboardBackgroundPath(Rectangle2D r) {
 		GeneralPath path = new GeneralPath();
 		float delta = (canvasRotation == 0f ? 0f : 1f / zoom);
 
 		Point2D.Float p = coordToDisplay(new Point2D.Float((float) r.getX() + delta, (float) r.getY() + delta));
 		path.moveTo(p.x, p.y);
 
 		p = coordToDisplay(new Point2D.Float((float) r.getMaxX() - delta, (float) r.getY() + delta));
 		path.lineTo(p.x, p.y);
 
 		p = coordToDisplay(new Point2D.Float((float) r.getMaxX() - delta, (float) r.getMaxY() - delta));
 		path.lineTo(p.x, p.y);
 
 		p = coordToDisplay(new Point2D.Float((float) r.getX() + delta, (float) r.getMaxY() - delta));
 		path.lineTo(p.x, p.y);
 		path.closePath();
 
 		return path;
 	}
 
 	//
 	// Mouse input methods
 	//
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		cursorIn = true;
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		cursorIn = false;
 		repaint();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		setModifiers(e.getModifiersEx());
 		setCursorX(e.getX ());
 		setCursorY(e.getY ());
 		setButton(e.getButton ());
 		requestFocusInWindow();
 		getActiveMode().cursorPressAction ();
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		setCursorX(e.getX());
 		setCursorY(e.getY());
 		setLastPressure(CPTablet.getRef ().getPressure ());
 		getActiveMode().cursorDragAction ();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		setModifiers(e.getModifiersEx());
 		setButton(e.getButton ());
 		getActiveMode().cursorReleaseAction ();
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		setCursorX(e.getX());
 		setCursorY(e.getY());
 		setLastPressure(CPTablet.getRef ().getPressure ());
 
 		if (!isDontStealFocus()) {
 			requestFocusInWindow();
 		}
 		getActiveMode().cursorMoveAction();
 		CPTablet.getRef().mouseDetect();
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		float factor = 1;
 		if (e.getWheelRotation() > 0) {
 			factor = 1 / 1.15f;
 		}
 		if (e.getWheelRotation() < 0) {
 			factor = 1.15f;
 		}
 
 		Point2D.Float pf = coordToDocument(new Point2D.Float(getCursorX(), getCursorY()));
 		if (artwork.isPointWithin(pf.x, pf.y)) {
 			zoomOnPoint(getZoom() * factor, getCursorX(), getCursorY());
 		} else {
 			zoomOnPoint(getZoom() * factor, offsetX + (int) (artwork.width * zoom / 2), offsetY
 					+ (int) (artwork.height * zoom / 2));
 		}
 		// FIXME: clean the above code, some coordinates get transformed multiple times for nothing
 	}
 
 	// //////////////////////////////////////////////////////////////////////////////////////
 	// Transformation methods
 	// //////////////////////////////////////////////////////////////////////////////////////
 
 	// Zoom
 
 	public void setZoom(float zoom) {
 		this.zoom = zoom;
 		updateTransform();
 	}
 
 	public float getZoom() {
 		return zoom;
 	}
 
 	// Offset
 
 	public void setOffset(Point p) {
 		setOffset(p.x, p.y);
 	}
 
 	public void setOffset(int x, int y) {
 		offsetX = x;
 		offsetY = y;
 		updateTransform();
 	}
 
 	public Point getOffset() {
 		return new Point(offsetX, offsetY);
 	}
 
 	// Rotation
 
 	public void setRotation(float angle) {
 		canvasRotation = (float) (angle % (2 * Math.PI));
 		updateTransform();
 	}
 
 	public float getRotation() {
 		return canvasRotation;
 	}
 
 	public void setApplyToAllLayers (boolean enabled) {
 		applyToAllLayers = enabled;
 	}
 
 	public boolean getApplyToAllLayers() {
 		return applyToAllLayers;
 	}
 
 	public void setInterpolation(boolean enabled) {
 		interpolation = enabled;
 		repaint();
 	}
 
 	public boolean getInterpolation() {
 		return interpolation;
 	}
 
 	// Update the affine transformation
 
 	void updateTransform() {
 		transform.setToIdentity();
 		transform.translate(offsetX, offsetY);
 		transform.scale(zoom, zoom);
 		transform.rotate(canvasRotation);
 
 		updateScrollBars();
 		repaint();
 	}
 
 	// More advanced zoom methods
 
 	public void zoomOnCenter(float zoomArg) {
 		Dimension d = getSize();
 		zoomOnPoint(zoomArg, d.width / 2, d.height / 2);
 	}
 
 	public void zoomOnPoint(float zoomArg, int centerX, int centerY) {
 		float zoom_ = zoomArg;
 		zoom_ = Math.max(minZoom, Math.min(maxZoom, zoom_));
 		if (getZoom() != zoom_) {
 			Point offset = getOffset();
 			setOffset(offset.x + (int) ((centerX - offset.x) * (1 - zoom_ / getZoom())), offset.y
 					+ (int) ((centerY - offset.y) * (1 - zoom_ / getZoom())));
 			setZoom(zoom_);
 
 			CPController.CPViewInfo viewInfo = new CPController.CPViewInfo();
 			viewInfo.zoom = zoom_;
 			viewInfo.offsetX = offsetX;
 			viewInfo.offsetY = offsetY;
 			controller.callViewListeners(viewInfo);
 
 			repaint();
 		}
 	}
 
 	public void zoomIn() {
 		zoomOnCenter(getZoom() * 2);
 	}
 
 	public void zoomOut() {
 		zoomOnCenter(getZoom() * .5f);
 	}
 
 	public void zoom100() {
 		resetRotation();
 		zoomOnCenter(1);
 		centerCanvas();
 	}
 
 	public void centerCanvas() {
 		Dimension d = getSize();
 		setOffset((d.width - (int) (artwork.width * getZoom())) / 2,
 				(d.height - (int) (artwork.height * getZoom())) / 2);
 		repaint();
 	}
 
 	public void resetRotation() {
 		Dimension d = getSize();
 		Point2D.Float center = new Point2D.Float(d.width / 2.f, d.height / 2.f);
 
 		AffineTransform rotTrans = new AffineTransform();
 		rotTrans.rotate(-getRotation(), center.x, center.y);
 		rotTrans.concatenate(transform);
 
 		setOffset((int) rotTrans.getTranslateX(), (int) rotTrans.getTranslateY());
 		setRotation(0);
 	}
 
 	//
 	// Coordinates and refresh areas methods
 	//
 
 	public Point2D.Float coordToDocument(Point2D p) {
 		Point2D.Float result = new Point2D.Float();
 
 		try {
 			transform.inverseTransform(p, result);
 		} catch (NoninvertibleTransformException ex) {
 			return result;
 		}
 
 		return result;
 	}
 
 	public Point coordToDocumentInt(Point2D p) {
 		Point2D.Float result = new Point2D.Float();
 
 		try {
 			transform.inverseTransform(p, result);
 		} catch (NoninvertibleTransformException ex) {
 			return new Point (0, 0);
 		}
 
 		return new Point((int) result.x, (int) result.y);
 	}
 
 	public Point2D.Float coordToDisplay(Point2D p) {
 		Point2D.Float result = new Point2D.Float();
 
 		transform.transform(p, result);
 
 		return result;
 	}
 
 	public Point coordToDisplayInt(Point2D p) {
 		Point2D.Float result = new Point2D.Float();
 
 		transform.transform(p, result);
 
 		return new Point((int) result.x, (int) result.y);
 	}
 
 	public Polygon coordToDisplay(CPRect r) {
 		Polygon poly = new Polygon();
 
 		Point p = coordToDisplayInt(new Point(r.left, r.top));
 		poly.addPoint(p.x, p.y);
 
 		p = coordToDisplayInt(new Point(r.right - 1, r.top));
 		poly.addPoint(p.x, p.y);
 
 		p = coordToDisplayInt(new Point(r.right - 1, r.bottom - 1));
 		poly.addPoint(p.x, p.y);
 
 		p = coordToDisplayInt(new Point(r.left, r.bottom - 1));
 		poly.addPoint(p.x, p.y);
 
 		return poly;
 	}
 
 	public Rectangle getRefreshArea(CPRect r) {
 		Point p1 = coordToDisplayInt(new Point(r.left - 1, r.top - 1));
 		Point p2 = coordToDisplayInt(new Point(r.left - 1, r.bottom));
 		Point p3 = coordToDisplayInt(new Point(r.right, r.top - 1));
 		Point p4 = coordToDisplayInt(new Point(r.right, r.bottom));
 
 		Rectangle r2 = new Rectangle();
 
 		r2.x = Math.min(Math.min(p1.x, p2.x), Math.min(p3.x, p4.x));
 		r2.y = Math.min(Math.min(p1.y, p2.y), Math.min(p3.y, p4.y));
 		r2.width = Math.max(Math.max(p1.x, p2.x), Math.max(p3.x, p4.x)) - r2.x + 1;
 		r2.height = Math.max(Math.max(p1.y, p2.y), Math.max(p3.y, p4.y)) - r2.y + 1;
 
 		r2.grow(2, 2); // to be sure to include everything
 
 		return r2;
 	}
 
 	void repaintBrushPreview() {
 		if (oldPreviewRect != null) {
 			Rectangle r = oldPreviewRect;
 			oldPreviewRect = null;
 			repaint(r.x, r.y, r.width, r.height);
 		}
 	}
 
 	Rectangle getBrushPreviewOval(boolean calcPressureArg) {
 		// TODO: Disable ability to turn on PressureSize for M_SMUDGE and M_OIL
 		boolean calcPressure = calcPressureArg;
 		if  (controller.getBrushInfo().paintMode == CPBrushInfo.M_SMUDGE || controller.getBrushInfo().paintMode == CPBrushInfo.M_OIL)
 			calcPressure = false;
 		int bSize = (int) (controller.getBrushSize() * zoom * (calcPressure && controller.getBrushInfo().pressureSize ? getLastPressure() : 1.0));
 		return new Rectangle(getCursorX() - bSize / 2, getCursorY() - bSize / 2, bSize, bSize);
 	}
 
 	//
 	// ChibiPaint interfaces methods
 	//
 
 	@Override
 	public void newTool(int tool, CPBrushInfo toolInfo) {
 		if (curSelectedMode == curDrawMode) {
 			curSelectedMode = drawingModes[toolInfo.strokeMode];
 		}
 		curDrawMode = drawingModes[toolInfo.strokeMode];
 
 		if (!spacePressed && cursorIn) {
 			brushPreview = true;
 
 			Rectangle r = getBrushPreviewOval(false);
 			r.grow(2, 2);
 			if (oldPreviewRect != null) {
 				r = r.union(oldPreviewRect);
 				oldPreviewRect = null;
 			}
 
 			repaint(r.x, r.y, r.width, r.height);
 		}
 	}
 
 	@Override
 	public void modeChange(int mode) {
 		switch (mode) {
 		case CPController.M_DRAW:
 			curSelectedMode = curDrawMode;
 			break;
 
 		case CPController.M_FLOODFILL:
 			curSelectedMode = floodFillMode;
 			break;
 
 		case CPController.M_RECT_SELECTION:
 			curSelectedMode = rectSelectionMode;
 			break;
 
 		case CPController.M_MOVE_TOOL:
 			curSelectedMode = moveToolMode;
 			break;
 
 		case CPController.M_ROTATE_CANVAS:
 			curSelectedMode = rotateCanvasMode;
 			break;
 		}
 	}
 
 	//
 	// misc overloaded and interface standard methods
 	//
 
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension((int) (artwork.width * zoom), (int) (artwork.height * zoom));
 	}
 
 	@Override
 	public void componentResized(ComponentEvent e) {
 		centerCanvas();
 		repaint();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
 			spacePressed = true;
 			repaintBrushPreview();
 			if (getActiveMode() == defaultMode) {
 				setCursor(moveCursor);
 			}
 		}
 
 		// FIXME: these should probably go through the controller to be dispatched
 		if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
 			switch (e.getKeyCode()) {
 			// case KeyEvent.VK_ADD:
 			case KeyEvent.VK_EQUALS:
 				zoomIn();
 				break;
 				// case KeyEvent.VK_SUBTRACT:
 			case KeyEvent.VK_MINUS:
 				zoomOut();
 				break;
 			case KeyEvent.VK_0:
 				// case KeyEvent.VK_NUMPAD0:
 				zoom100();
 				break;
 			}
 		} else {
 			switch (e.getKeyCode()) {
 			case KeyEvent.VK_ADD:
 			case KeyEvent.VK_EQUALS:
 				zoomIn();
 				break;
 			case KeyEvent.VK_SUBTRACT:
 			case KeyEvent.VK_MINUS:
 				zoomOut();
 				break;
 			case KeyEvent.VK_1:
 				controller.setAlpha((int) (.1f * 255));
 				break;
 			case KeyEvent.VK_2:
 				controller.setAlpha((int) (.2f * 255));
 				break;
 			case KeyEvent.VK_3:
 				controller.setAlpha((int) (.3f * 255));
 				break;
 			case KeyEvent.VK_4:
 				controller.setAlpha((int) (.4f * 255));
 				break;
 			case KeyEvent.VK_5:
 				controller.setAlpha((int) (.5f * 255));
 				break;
 			case KeyEvent.VK_6:
 				controller.setAlpha((int) (.6f * 255));
 				break;
 			case KeyEvent.VK_7:
 				controller.setAlpha((int) (.7f * 255));
 				break;
 			case KeyEvent.VK_8:
 				controller.setAlpha((int) (.8f * 255));
 				break;
 			case KeyEvent.VK_9:
 				controller.setAlpha((int) (.9f * 255));
 				break;
 			case KeyEvent.VK_0:
 				controller.setAlpha(255);
 				break;
 			case KeyEvent.VK_OPEN_BRACKET:
 				if (controller.getBrushInfo().curSize < 200.0f)
 					controller.getBrushInfo().curSize += 1.0f;
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
 			spacePressed = false;
 			if (getActiveMode() == defaultMode) {
 				setCursor(defaultCursor);
 			}
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case '[':
 			controller.setBrushSize(controller.getBrushSize() - 1);
 			break;
 		case ']':
 			controller.setBrushSize(controller.getBrushSize() + 1);
 			break;
 		}
 	}
 
 	@Override
 	public boolean isFocusable() {
 		return true;
 	}
 
 	// Unused interface methods
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// To implement interface
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent e) {
 		// To implement interface
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent e) {
 		// To implement interface
 	}
 
 	@Override
 	public void componentShown(ComponentEvent e) {
 		// To implement interface
 	}
 
 	@Override
 	public void updateRegion(CPArtwork artworkArg, CPRect region) {
 		updateRegion.union(region);
 
 		Rectangle r = getRefreshArea(region);
 		repaint(r.x, r.y, r.width, r.height);
 	}
 
 	@Override
 	public void layerChange(CPArtwork artworkArg) {
 		// unused method
 	}
 
 	public void showGrid(boolean show) {
 		showGrid = show;
 		repaint();
 	}
 
 	public void saveCanvasSettings ()
 	{
 		Preferences userRoot = Preferences.userRoot();
 		Preferences preferences = userRoot.node( "chibipaintmod" );
 		preferences.putBoolean ("Lock Alpha", artwork.isLockAlpha());
 		preferences.putBoolean ("Interpolation", interpolation);
 		preferences.putBoolean ("Show Grid", showGrid);
 		preferences.putInt ("Grid size", gridSize);
 	}
 
 	public void loadCanvasSettings ()
 	{
 		Preferences userRoot = Preferences.userRoot();
 		Preferences preferences = userRoot.node( "chibipaintmod" );
 		setInterpolation (preferences.getBoolean ("Interpolation", interpolation));
 		setApplyToAllLayers(preferences.getBoolean ("Apply to All Layers", applyToAllLayers));
 		showGrid (preferences.getBoolean ("Show Grid", showGrid));
 		artwork.setLockAlpha (preferences.getBoolean ("Lock Alpha", artwork.isLockAlpha ()));
 		controller.getMainGUI().setPaletteMenuItem("Use Linear Interpolation", interpolation);
 		controller.getMainGUI().setPaletteMenuItem("Apply to All Layers", applyToAllLayers);
 		controller.getMainGUI().setPaletteMenuItem("Show Grid", showGrid);
 		gridSize = preferences.getInt ("Grid size", gridSize);
 	}
 
 	//
 	// base class for the different modes
 	//
 
 	public int getCursorX() {
 		return cursorX;
 	}
 
 	public void setCursorX(int cursorX) {
 		this.cursorX = cursorX;
 	}
 
 	public int getCursorY() {
 		return cursorY;
 	}
 
 	public void setCursorY(int cursorY) {
 		this.cursorY = cursorY;
 	}
 
 	public float getLastPressure() {
 		return lastPressure;
 	}
 
 	public void setLastPressure(float lastPressure) {
 		this.lastPressure = lastPressure;
 	}
 
 	public boolean isDontStealFocus() {
 		return dontStealFocus;
 	}
 
 	public void setDontStealFocus(boolean dontStealFocus) {
 		this.dontStealFocus = dontStealFocus;
 	}
 
 	public CPMode getActiveMode() {
 		return activeMode;
 	}
 
 	public void setActiveMode(CPMode activeMode) {
 		this.activeMode = activeMode;
 	}
 
 	public int getModifiers() {
 		return modifiers;
 	}
 
 	public void setModifiers(int modifiers) {
 		this.modifiers = modifiers;
 	}
 
 	public int getButton() {
 		return button;
 	}
 
 	public void setButton(int button) {
 		this.button = button;
 	}
 
 	public abstract class CPMode {
 
 		// Mouse Input
 
 		public void cursorPressAction () {
 			// To not define actions for some functions in every case
 		}
 
 		public void cursorReleaseAction () {
 			// To not define actions for some functions in every case
 		}
 
 
 		public void cursorClickAction () {
 			// To not define actions for some functions in every case
 		}
 
 		public void cursorMoveAction ()
 		{
 			// To not define actions for some functions in every case
 		}
 
 		public void cursorDragAction () {
 			// To not define actions for some functions in every case
 		}
 
 		// GUI drawing
 		@SuppressWarnings("unused")
 		public void paint(Graphics2D g2d) {
 			// To not define actions for some functions in every case
 		}
 
 	}
 
 	//
 	// Default UI Mode when not doing anything: used to start the other modes
 	//
 
 	//
 	// /!\ WARNING: always use getModifiersEx() to test for modifier
 	// keys as methods like isAltDown() have unexpected results
 	//
 
 	class CPDefaultMode extends CPMode {
 
 		@Override
 		public void cursorPressAction () {
 			// FIXME: replace the moveToolMode hack by a new and improved system
 			if (!spacePressed && getButton() == MouseEvent.BUTTON1
 					&& (((getModifiers() & InputEvent.ALT_DOWN_MASK) == 0) || curSelectedMode == moveToolMode)) {
 
 				if (!artwork.getActiveLayer().isVisible() && curSelectedMode != rotateCanvasMode
 						&& curSelectedMode != rectSelectionMode) {
 					brushPreview = false;
 					repaintBrushPreview ();
 					return; // don't draw on a hidden layer
 				}
 				repaintBrushPreview();
 
 				setActiveMode(curSelectedMode);
 				getActiveMode().cursorPressAction ();
 			} else if (!spacePressed
 					&& (getButton() == MouseEvent.BUTTON3 || (getButton() == MouseEvent.BUTTON1 && ((
 							getModifiers() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)))) {
 				repaintBrushPreview();
 
 				setActiveMode(colorPickerMode);
 				getActiveMode().cursorPressAction ();
 			} else if ((getButton() == MouseEvent.BUTTON2 || spacePressed)
 					&& ((getModifiers() & InputEvent.ALT_DOWN_MASK) == 0)) {
 				repaintBrushPreview();
 
 				setActiveMode(moveCanvasMode);
 				getActiveMode().cursorPressAction ();
 			} else if ((getButton() == MouseEvent.BUTTON2 || spacePressed)
 					&& ((getModifiers() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)) {
 				repaintBrushPreview();
 
 				setActiveMode(rotateCanvasMode);
 				getActiveMode().cursorPressAction ();
 			}
 
 		}
 
 		@Override
 		public void paint(Graphics2D g2d) {
 			if (brushPreview && curSelectedMode == curDrawMode) {
 				brushPreview = false;
 
 				Rectangle r;
 				r = getBrushPreviewOval(false);
 				g2d.drawOval(r.x, r.y, r.width, r.height);
 				r.grow(2, 2);
 				oldPreviewRect = oldPreviewRect != null ? r.union(oldPreviewRect) : r;
 			}
 		}
 
 		@Override
 		public void cursorMoveAction () {
 			Point p = new Point (getCursorX(), getCursorY());
 			if (!spacePressed && cursorIn) {
 				brushPreview = true;
 
 				Rectangle r = getBrushPreviewOval(false);
 				r.grow(2, 2);
 				if (oldPreviewRect != null) {
 					r = r.union(oldPreviewRect);
 					oldPreviewRect = null;
 				}
 
 				Point2D.Float pf = coordToDocument(p);
 				if (artwork.isPointWithin(pf.x, pf.y)) {
 					setCursor(defaultCursor); // FIXME find a cursor that everyone likes
 				} else {
 					setCursor(defaultCursor);
 				}
 
 				repaint(r.x, r.y, r.width, r.height);
 			}
 		}
 	}
 
 	//
 	// Freehand mode
 	//
 
 	// FIXME: dragLeft no longer necessary, should not specify the drag button
 
 	class CPFreehandMode extends CPMode {
 
 		boolean dragLeft = false;
 		Point2D.Float smoothMouse = new Point2D.Float(0, 0);
 
 		@Override
 		public void cursorPressAction () {
 			if (!dragLeft && getButton() == MouseEvent.BUTTON1) {
 				Point p = new Point (getCursorX(), getCursorY());
 				Point2D.Float pf = coordToDocument(p);
 
 				dragLeft = true;
 				artwork.beginStroke(pf.x, pf.y, getLastPressure());
 
 				smoothMouse = (Point2D.Float) pf.clone();
 			}
 		}
 
 		@Override
 		public void cursorDragAction () {
 			Point p = new Point (getCursorX(), getCursorY());
 			Point2D.Float pf = coordToDocument(p);
 
 			float smoothing = Math.min(.999f, (float) Math.pow(controller.getBrushInfo().smoothing, .3));
 
 			smoothMouse.x = (1f - smoothing) * pf.x + smoothing * smoothMouse.x;
 			smoothMouse.y = (1f - smoothing) * pf.y + smoothing * smoothMouse.y;
 
 			if (dragLeft) {
 				artwork.continueStroke(smoothMouse.x, smoothMouse.y, getLastPressure());
 			}
 
 			brushPreview = true;
 
 			Rectangle r = getBrushPreviewOval(true);
 			r.grow(2, 2);
 			if (oldPreviewRect != null) {
 				r = r.union(oldPreviewRect);
 				oldPreviewRect = null;
 			}
 
 			if (artwork.isPointWithin(pf.x, pf.y)) {
 				setCursor(defaultCursor); // FIXME find a cursor that everyone likes
 			} else {
 				setCursor(defaultCursor);
 			}
 
 			repaint(r.x, r.y, r.width, r.height);
 		}
 
 		@Override
 		public void cursorReleaseAction() {
 			if (dragLeft && getButton() == MouseEvent.BUTTON1) {
 				dragLeft = false;
 				artwork.endStroke();
 
 				setActiveMode(defaultMode); // yield control to the default mode
 			}
 		}
 
 		@Override
 		public void paint(Graphics2D g2d) {
 			if (brushPreview && curSelectedMode == curDrawMode) {
 				brushPreview = false;
 
 				Rectangle r = getBrushPreviewOval(true);
 				g2d.drawOval(r.x, r.y, r.width, r.height);
 
 				r.grow(2, 2);
 				oldPreviewRect = oldPreviewRect != null ? r.union(oldPreviewRect) : r;
 			}
 		}
 	}
 
 	//
 	// Line drawing mode
 	//
 
 	class CPLineMode extends CPMode {
 
 		boolean dragLine = false;
 		Point dragLineFrom, dragLineTo;
 
 		@Override
 		public void cursorPressAction () {
 			if (!dragLine && getButton() == MouseEvent.BUTTON1) {
 				Point p = new Point (getCursorX(), getCursorY());
 
 				dragLine = true;
 				dragLineFrom = dragLineTo = (Point) p.clone();
 			}
 		}
 
 		@Override
 		public void cursorDragAction() {
 			Point p = new Point (getCursorX(), getCursorY());
 
 			Rectangle r = new Rectangle(Math.min(dragLineFrom.x, dragLineTo.x), Math.min(dragLineFrom.y, dragLineTo.y),
 					Math.abs(dragLineFrom.x - dragLineTo.x) + 1, Math.abs(dragLineFrom.y - dragLineTo.y) + 1);
 			r = r.union(new Rectangle(Math.min(dragLineFrom.x, p.x), Math.min(dragLineFrom.y, p.y), Math
 					.abs(dragLineFrom.x - p.x) + 1, Math.abs(dragLineFrom.y - p.y) + 1));
 			dragLineTo = (Point) p.clone();
 			repaint(r.x, r.y, r.width, r.height);
 		}
 
 		@Override
 		public void cursorReleaseAction () {
 			if (dragLine && getButton() == MouseEvent.BUTTON1) {
 				Point p = new Point (getCursorX(), getCursorY());
 				Point2D.Float pf = coordToDocument(p);
 
 				dragLine = false;
 
 				Point2D.Float from = coordToDocument(dragLineFrom);
 				artwork.beginStroke(from.x, from.y, 1);
 				artwork.continueStroke(pf.x, pf.y, 1);
 				artwork.endStroke();
 
 				Rectangle r = new Rectangle(Math.min(dragLineFrom.x, dragLineTo.x), Math.min(dragLineFrom.y,
 						dragLineTo.y), Math.abs(dragLineFrom.x - dragLineTo.x) + 1, Math.abs(dragLineFrom.y
 								- dragLineTo.y) + 1);
 				repaint(r.x, r.y, r.width, r.height);
 
 				setActiveMode(defaultMode); // yield control to the default mode
 			}
 		}
 
 		@Override
 		public void paint(Graphics2D g2d) {
 			if (dragLine) {
 				g2d.drawLine(dragLineFrom.x, dragLineFrom.y, dragLineTo.x, dragLineTo.y);
 			}
 		}
 
 
 	}
 
 	//
 	// Bezier drawing mode
 	//
 
 	class CPBezierMode extends CPMode {
 
 		// bezier drawing
 		static final int BEZIER_POINTS = 500;
 		static final int BEZIER_POINTS_PREVIEW = 100;
 
 		boolean dragBezier = false;
 		int dragBezierMode; // 0 Initial drag, 1 first control point, 2 second point
 		Point2D.Float dragBezierP0, dragBezierP1, dragBezierP2, dragBezierP3;
 
 		@Override
 		public void cursorPressAction () {
 			Point2D.Float p = coordToDocument(new Point (getCursorX(), getCursorY()));
 
 			if (!dragBezier && !spacePressed && getButton() == MouseEvent.BUTTON1) {
 				dragBezier = true;
 				dragBezierMode = 0;
 				dragBezierP0 = dragBezierP1 = dragBezierP2 = dragBezierP3 = (Point2D.Float) p.clone();
 			}
 		}
 
 		@Override
 		public void cursorDragAction() {
 			Point2D.Float p = coordToDocument(new Point (getCursorX(), getCursorY()));
 
 			if (dragBezier && dragBezierMode == 0) {
 				dragBezierP2 = dragBezierP3 = (Point2D.Float) p.clone();
 				repaint();
 			}
 		}
 
 		@Override
 		public void cursorReleaseAction () {
 			if (dragBezier && getButton() == MouseEvent.BUTTON1) {
 				if (dragBezierMode == 0) {
 					dragBezierMode = 1;
 				} else if (dragBezierMode == 1) {
 					dragBezierMode = 2;
 				} else if (dragBezierMode == 2) {
 					dragBezier = false;
 
 					Point2D.Float p0 = dragBezierP0;
 					Point2D.Float p1 = dragBezierP1;
 					Point2D.Float p2 = dragBezierP2;
 					Point2D.Float p3 = dragBezierP3;
 
 					CPBezier bezier = new CPBezier();
 					bezier.x0 = p0.x;
 					bezier.y0 = p0.y;
 					bezier.x1 = p1.x;
 					bezier.y1 = p1.y;
 					bezier.x2 = p2.x;
 					bezier.y2 = p2.y;
 					bezier.x3 = p3.x;
 					bezier.y3 = p3.y;
 
 					float x[] = new float[BEZIER_POINTS];
 					float y[] = new float[BEZIER_POINTS];
 
 					bezier.compute(x, y, BEZIER_POINTS);
 
 					artwork.beginStroke(x[0], y[0], 1);
 					for (int i = 1; i < BEZIER_POINTS; i++) {
 						artwork.continueStroke(x[i], y[i], 1);
 					}
 					artwork.endStroke();
 					repaint();
 
 					setActiveMode(defaultMode); // yield control to the default mode
 				}
 			}
 		}
 
 		@Override
 		public void cursorMoveAction () {
 			Point2D.Float p_document = coordToDocument(new Point (getCursorX(), getCursorY()));
 
 			if (dragBezier && dragBezierMode == 1) {
 				dragBezierP1 = (Point2D.Float) p_document.clone();
 				repaint(); // FIXME: repaint only the bezier region
 			}
 
 			if (dragBezier && dragBezierMode == 2) {
 				dragBezierP2 = (Point2D.Float) p_document.clone();
 				repaint(); // FIXME: repaint only the bezier region
 			}
 		}
 
 		@Override
 		public void paint(Graphics2D g2d) {
 			if (dragBezier) {
 				CPBezier bezier = new CPBezier();
 
 				Point2D.Float p0 = coordToDisplay(dragBezierP0);
 				Point2D.Float p1 = coordToDisplay(dragBezierP1);
 				Point2D.Float p2 = coordToDisplay(dragBezierP2);
 				Point2D.Float p3 = coordToDisplay(dragBezierP3);
 
 				bezier.x0 = p0.x;
 				bezier.y0 = p0.y;
 				bezier.x1 = p1.x;
 				bezier.y1 = p1.y;
 				bezier.x2 = p2.x;
 				bezier.y2 = p2.y;
 				bezier.x3 = p3.x;
 				bezier.y3 = p3.y;
 
 				int x[] = new int[BEZIER_POINTS_PREVIEW];
 				int y[] = new int[BEZIER_POINTS_PREVIEW];
 				bezier.compute(x, y, BEZIER_POINTS_PREVIEW);
 
 				g2d.drawPolyline(x, y, BEZIER_POINTS_PREVIEW);
 				g2d.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
 				g2d.drawLine((int) p2.x, (int) p2.y, (int) p3.x, (int) p3.y);
 			}
 		}
 
 
 	}
 
 	//
 	// Color picker mode
 	//
 
 	class CPColorPickerMode extends CPMode {
 
 		int mouseButton;
 
 		@Override
 		public void cursorPressAction () {
 			Point p = new Point (getCursorX(), getCursorY());
 			Point2D.Float pf = coordToDocument(p);
 
 			mouseButton = getButton();
 
 			if (artwork.isPointWithin(pf.x, pf.y)) {
 				controller.setCurColorRgb(artwork.colorPicker(pf.x, pf.y));
 			}
 
 			setCursor(crossCursor);
 		}
 
 		@Override
 		public void cursorDragAction() {
 			Point p = new Point (getCursorX(), getCursorY());
 			Point2D.Float pf = coordToDocument(p);
 
 			if (artwork.isPointWithin(pf.x, pf.y)) {
 				controller.setCurColorRgb(artwork.colorPicker(pf.x, pf.y));
 			}
 		}
 
 		@Override
 		public void cursorReleaseAction () {
 			if (getButton() == mouseButton) {
 				setCursor(defaultCursor);
 				setActiveMode(defaultMode); // yield control to the default mode
 			}
 		}
 	}
 
 	//
 	// Canvas move mode
 	//
 
 	class CPMoveCanvasMode extends CPMode {
 
 		boolean dragMiddle = false;
 		int dragMoveX, dragMoveY;
 		Point dragMoveOffset;
 		int dragMoveButton;
 
 		@Override
 		public void cursorPressAction () {
 			Point p = new Point (getCursorX(), getCursorY());
 
 			if (!dragMiddle && (getButton() == MouseEvent.BUTTON2 || spacePressed)) {
 				repaintBrushPreview();
 
 				dragMiddle = true;
 				dragMoveButton = getButton();
 				dragMoveX = p.x;
 				dragMoveY = p.y;
 				dragMoveOffset = getOffset();
 				setCursor(moveCursor);
 			}
 		}
 
 		@Override
 		public void cursorDragAction() {
 			if (dragMiddle) {
 				Point p = new Point (getCursorX(), getCursorY());
 
 				setOffset(dragMoveOffset.x + p.x - dragMoveX, offsetY = dragMoveOffset.y + p.y - dragMoveY);
 				repaint();
 			}
 		}
 
 		@Override
 		public void cursorReleaseAction () {
 			if (dragMiddle && getButton() == dragMoveButton) {
 				dragMiddle = false;
 				setCursor(defaultCursor);
 
 				setActiveMode(defaultMode); // yield control to the default mode
 			}
 		}
 	}
 
 	//
 	// Flood fill mode
 	//
 
 	class CPFloodFillMode extends CPMode {
 
 		@Override
 		public void cursorPressAction () {
 			Point p = new Point (getCursorX(), getCursorY());
 			Point2D.Float pf = coordToDocument(p);
 
 			if (artwork.isPointWithin(pf.x, pf.y)) {
 				artwork.floodFill(pf.x, pf.y, controller.getColorDistance());
 				repaint();
 			}
 
 			setActiveMode(defaultMode); // yield control to the default mode
 		}
 
 
 	}
 
 	//
 	// CPRectSelection mode
 	//
 
 	class CPRectSelectionMode extends CPMode {
 
 		Point firstClick;
 		CPRect curRect = new CPRect();
 
 		@Override
 		public void cursorPressAction () {
 			Point p = coordToDocumentInt(new Point (getCursorX(), getCursorY()));
 
 			curRect.makeEmpty();
 			firstClick = p;
 
 			repaint();
 		}
 
 		@Override
 		public void cursorDragAction() {
 			Point p = coordToDocumentInt(new Point (getCursorX(), getCursorY()));
 			boolean square = (getModifiers() & InputEvent.SHIFT_MASK) != 0;
 			int squareDist = Math.max(Math.abs(p.x - firstClick.x), Math.abs(p.y - firstClick.y));
 
 			if (p.x >= firstClick.x) {
 				curRect.left = firstClick.x;
 				curRect.right = square ? firstClick.x + squareDist : p.x;
 			} else {
 				curRect.left = square ? firstClick.x - squareDist : p.x;
 				curRect.right = firstClick.x;
 			}
 
 			if (p.y >= firstClick.y) {
 				curRect.top = firstClick.y;
 				curRect.bottom = square ? firstClick.y + squareDist : p.y;
 			} else {
 				curRect.top = square ? firstClick.y - squareDist : p.y;
 				curRect.bottom = firstClick.y;
 			}
 
 			repaint ();
 		}
 
 		@Override
 		public void cursorReleaseAction () {
 
 			boolean ShiftPressed = (getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0;
 			boolean ControlPressed = (getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0;
 			if (ShiftPressed || ControlPressed)
 			{
 				CPSelection Rect = new CPSelection (artwork.width, artwork.height);
 				Rect.makeRectangularSelection(curRect);
 				if (ShiftPressed)
 				{
 					if (!ControlPressed)
 						curSelection.AddToSelection (Rect);
 					else
 						curSelection.IntersectWithSelection (Rect);
 				}
 				else
 					curSelection.SubtractFromSelection (Rect);
 			}
 			else
 				curSelection.makeRectangularSelection (curRect);
 
 			artwork.rectangleSelection(curRect);
 
 			setActiveMode(defaultMode); // yield control to the default mode
 			repaint();
 		}
 
 		@Override
 		public void paint(Graphics2D g2d) {
 			if (!curRect.isEmpty()) {
 				g2d.draw(coordToDisplay(curRect));
 			}
 		}
 
 	}
 
 	//
 	// CPMoveTool mode
 	//
 
 	class CPMoveToolMode extends CPMode {
 
 		Point firstClick;
 
 		@Override
 		public void cursorPressAction () {
 			Point p = coordToDocumentInt(new Point (getCursorX(), getCursorY()));
 			firstClick = p;
 
 			artwork.beginPreviewMode((getModifiers() & InputEvent.ALT_MASK) != 0);
 
 			// FIXME: The following hack avoids a slight display glitch
 			// if the whole move tool mess is fixed it probably won't be necessary anymore
 			artwork.move(0, 0);
 		}
 
 		@Override
 		public void cursorDragAction() {
 			Point p = coordToDocumentInt(new Point (getCursorX(), getCursorY()));
 			artwork.move(p.x - firstClick.x, p.y - firstClick.y);
 			repaint();
 		}
 
 		@Override
 		public void cursorReleaseAction () {
 			artwork.endPreviewMode();
 			setActiveMode(defaultMode); // yield control to the default mode
 			repaint();
 		}
 
 
 	}
 
 	//
 	// Canvas rotate mode
 	//
 
 	class CPRotateCanvasMode extends CPMode {
 
 		Point firstClick;
 		float initAngle;
 		AffineTransform initTransform;
 		boolean dragged;
 
 		@Override
 		public void cursorPressAction () {
 			Point p = new Point (getCursorX(), getCursorY());
 			firstClick = (Point) p.clone();
 
 			initAngle = getRotation();
 			initTransform = new AffineTransform(transform);
 
 			dragged = false;
 
 			repaintBrushPreview();
 		}
 
 		@Override
 		public void cursorDragAction() {
 			dragged = true;
 
 			Point p = new Point (getCursorX(), getCursorY());
 			Dimension d = getSize();
 			Point2D.Float center = new Point2D.Float(d.width / 2.f, d.height / 2.f);
 
 			float deltaAngle = (float) Math.atan2(p.y - center.y, p.x - center.x)
 					- (float) Math.atan2(firstClick.y - center.y, firstClick.x - center.x);
 
 			AffineTransform rotTrans = new AffineTransform();
 			rotTrans.rotate(deltaAngle, center.x, center.y);
 
 			rotTrans.concatenate(initTransform);
 
 			setRotation(initAngle + deltaAngle);
 			setOffset((int) rotTrans.getTranslateX(), (int) rotTrans.getTranslateY());
 			repaint();
 		}
 
 		@Override
 		public void cursorReleaseAction () {
 			if (!dragged) {
 				resetRotation();
 			}
 
 			setActiveMode(defaultMode); // yield control to the default mode
 		}
 	}
 
 	/*
 	 * // // mode //
 	 *
 	 * class CPMode extends CPMode { public void mousePressed(MouseEvent e) {} public void mouseDragged(MouseEvent e) {}
 	 * public void mouseReleased(MouseEvent e) {} }
 	 */
 }
 
