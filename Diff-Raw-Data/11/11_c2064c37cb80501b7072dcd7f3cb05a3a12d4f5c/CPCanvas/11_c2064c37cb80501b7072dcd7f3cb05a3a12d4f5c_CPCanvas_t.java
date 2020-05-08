 /*
  * ChibiPaintMod
  *     Copyright (c) 2012-2014 Sergey Semushin
  *     Copyright (c) 2006-2008 Marc Schefer
  *
  *     This file is part of ChibiPaintMod (previously ChibiPaint).
  *
  *     ChibiPaintMod is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     ChibiPaintMod is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ChibiPaintMod. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package chibipaint.gui;
 
 import chibipaint.controller.CPCommandId;
 import chibipaint.controller.CPCommandSettings;
 import chibipaint.controller.CPCommonController;
 import chibipaint.controller.CPControllerApplication;
 import chibipaint.effects.CPTransparentFillEffect;
 import chibipaint.engine.CPArtwork;
 import chibipaint.engine.CPBrushInfo;
 import chibipaint.engine.CPSelection;
 import chibipaint.engine.CPTransformHandler;
 import chibipaint.util.CPBezier;
 import chibipaint.util.CPEnums;
 import chibipaint.util.CPRect;
 import chibipaint.util.CPTablet;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.MemoryImageSource;
 import java.util.Arrays;
 import java.util.prefs.Preferences;
 
 import static chibipaint.engine.CPArtwork.SelectionTypeOfAppliance;
 
 public class CPCanvas extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener,
                                                     ComponentListener, KeyListener, CPCommonController.ICPToolListener, CPCommonController.ICPModeListener,
                                                     CPArtwork.ICPArtworkListener
 {
 
 /**
  *
  */
 private static final long serialVersionUID = 1L;
 
 private CPCommonController controller;
 
 // FIXME: this should not be public
 public Image img;
 public Image overlayImg;
 
 private BufferedImage checkerboardPattern;
 private MemoryImageSource imgSource;
 private CPRect updateRegion = new CPRect ();
 private MemoryImageSource overlayImgSource;
 
 private int[] buffer;
 private int[] overlayBuffer;
 private CPArtwork artwork;
 
 private boolean spacePressed = false;
 
 // canvas transformations
 private float zoom = 1;
 private final float minZoom = .05f;
 private final float maxZoom = 16.f;
 private int offsetX;
 private int offsetY;
 private float canvasRotation = 0.f;
 private final AffineTransform transform = new AffineTransform ();
 private boolean applyToAllLayers = false;
 private boolean interpolation = true;
 
 private int cursorX;
 
 private int cursorY;
 private int modifiers;
 private int button;
 private Timer selectionUpdateTimer;
 
 private Rectangle oldPreviewRect;
 private JFrame waitingFrame;
 
 private Cursor defaultCursor;
 private Cursor moveCursor;
 private Cursor crossCursor;
 private CPMode prevMode = null;
 private boolean drawPrevMode = false;
 private boolean showSelection = true;
 private boolean palettesShown = true;
 
 public boolean isCursorIn ()
 {
   return cursorIn;
 }
 
 private boolean cursorIn;
 
 private boolean dontStealFocus = false;
 
 // Grid options
 
 // FIXME: these shouldn't be public
 public int gridSize = 32;
 private boolean showGrid = false;
 
 //
 // Modes system: modes control the way the GUI is reacting to the user input
 // All the tools are implemented through modes
 //
 
 private final CPMode defaultMode = new CPDefaultMode ();
 private final CPMode colorPickerMode = new CPColorPickerMode ();
 private final CPMode moveCanvasMode = new CPMoveCanvasMode ();
 private final CPMode rotateCanvasMode = new CPRotateCanvasMode ();
 private final CPMode floodFillMode = new CPFloodFillMode ();
 private final CPMode magicWandMode = new CPMagicWandMode ();
 private final CPMode rectMode = new CPRectSelectionMode ();
 private final CPMode freeSelectionMode = new CPFreeSelectionMode ();
 private final CPFreeTransformMode freeTransformMode = new CPFreeTransformMode ();
 
 // this must correspond to the stroke modes defined in CPToolInfo
 private final CPMode[] drawingModes = {new CPFreehandMode (), new CPLineMode (), new CPBezierMode (),};
 
 private CPMode curDrawMode = drawingModes[CPBrushInfo.SM_FREEHAND];
 private CPMode curSelectedMode = curDrawMode;
 private CPMode activeMode = defaultMode;
 
 // Container with scrollbars
 private JPanel container;
 private JScrollBar horizontalScroll;
 private JScrollBar verticalScroll;
 
 private float lastPressure;
 
 void setArtwork (CPArtwork artwork)
 {
   this.artwork = artwork;
   if (artwork != null)
     freeTransformMode.setTransformHandler (artwork.getTransformHandler ());
 }
 
 private boolean isRunningAsApplication ()
 {
   return controller.isRunningAsApplication ();
 }
 
 private static SelectionTypeOfAppliance modifiersToSelectionApplianceType (int modifiers)
 {
   boolean ShiftPressed = (modifiers & InputEvent.SHIFT_DOWN_MASK) != 0;
   boolean ControlPressed = (modifiers & InputEvent.CTRL_DOWN_MASK) != 0;
   if (!ShiftPressed && !ControlPressed)
     return SelectionTypeOfAppliance.CREATE;
   else if (ShiftPressed)
     {
       if (ControlPressed)
         return SelectionTypeOfAppliance.INTERSECT;
       else
         return SelectionTypeOfAppliance.ADD;
     }
   else
     return SelectionTypeOfAppliance.SUBTRACT;
 }
 
 CPCanvas getCanvas ()
 {
   return this;
 }
 
 public void setNiceInitialPosition ()
 {
   Rectangle bounds = this.getBounds ();
   setZoom (Math.min (Math.min ((float) bounds.getHeight () / artwork.getHeight (), (float) bounds.getWidth () / artwork.getWidth ()), 1.0f));
   centerCanvas ();
 }
 
 public void reinitCanvas ()
 {
   setArtwork (controller.getArtwork ());
 
 
   artwork.addListener (this);
 
   prepareImages ();
   if (Arrays.asList (drawingModes).contains (curSelectedMode))
     {
       controller.setTool (controller.getCurBrush ());
     }
   repaint ();
   controller.canvas.setNiceInitialPosition ();
   initialUpdatesAfterCanvasCreation ();
 }
 
 void prepareImages ()
 {
   buffer = artwork.getDisplayBM ().getData ();
   int w = artwork.getWidth ();
   int h = artwork.getHeight ();
 
   overlayBuffer = artwork.getOverlayBM ().getData ();
 
   imgSource = new MemoryImageSource (w, h, buffer, 0, w);
   imgSource.setAnimated (true);
   img = createImage (imgSource);
 
   overlayImgSource = new MemoryImageSource (w, h, overlayBuffer, 0, w);
   overlayImgSource.setAnimated (true);
   overlayImg = createImage (overlayImgSource);
   updateRegion = new CPRect (w, h);
 }
 
 public void initialUpdatesAfterCanvasCreation ()
 {
   updateViewInfo ();
 }
 
 void initCanvas (CPCommonController ctrl)
 {
   this.controller = ctrl;
 
   setArtwork (ctrl.getArtwork ());
 
   prepareImages ();
 
   ctrl.setCanvas (this);
 
   int[] pixels = new int[16 * 16];
   defaultCursor = new Cursor (Cursor.DEFAULT_CURSOR);
   moveCursor = new Cursor (Cursor.MOVE_CURSOR);
   crossCursor = new Cursor (Cursor.CROSSHAIR_CURSOR);
 
   // Creates the checkboard pattern seen in transparent areas
   checkerboardPattern = new BufferedImage (64, 64, BufferedImage.TYPE_INT_RGB);
   pixels = new int[64 * 64];
   for (int j = 0; j < 64; j++)
     {
       for (int i = 0; i < 64; i++)
         {
           if ((i & 0x8) != 0 ^ (j & 0x8) != 0)
             {
               pixels[i + j * 64] = 0xffffffff;
             }
           else
             {
               pixels[i + j * 64] = 0xffcccccc;
             }
         }
     }
   checkerboardPattern.setRGB (0, 0, 64, 64, pixels, 0, 64);
 
 		/*
          * KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false); Action escapeAction = new
 		 * AbstractAction() { public void actionPerformed(ActionEvent e) { controller.setAlpha((int)(.5f*255)); } };
 		 *
 		 * getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW). put(escapeKeyStroke, "SPACE"); getActionMap().put("SPACE",
 		 * escapeAction);
 		 */
 
   // register as a listener for Mouse & MouseMotion events
   addMouseWheelListener (this);
   addComponentListener (this);
   addKeyListener (this);
 
   controller.addToolListener (this);
   controller.addModeListener (this);
 
   artwork.addListener (this);
 
   // So that the tab key will work
   setFocusTraversalKeysEnabled (false);
 
   killTimers ();
   selectionUpdateTimer = new Timer (50, new ActionListener ()
   {
 
     @Override
     public void actionPerformed (ActionEvent arg0)
     {
       artwork.getCurSelection ().RaiseInitialDash ();
       repaint (artwork.getCurSelection ().getBoundingBox (getThis ()));
     }
   });
   selectionUpdateTimer.start ();
 }
 
 public void initMouseListeners ()
 {
   addMouseListener (this);
   addMouseMotionListener (this);
 }
 
 CPCanvas getThis ()
 {
   return this;
 }
 
 public CPCanvas (CPCommonController ctrl)
 {
   initCanvas (ctrl);
 }
 
 public void ShowLoadingTabletListenerMessage ()
 {
   waitingFrame = new JFrame ("Loading...");
   waitingFrame.setUndecorated (true);
   waitingFrame.setSize (200, 50);
   waitingFrame.setLocationRelativeTo (null);
   waitingFrame.setVisible (true);
   JPanel panel = new JPanel ();
   panel.setLayout (new GridLayout ());
   JLabel label = new JLabel ("<html><center>Loading Tablet Listener...<br>Please Wait...</center></html>");
   panel.add (label);
   waitingFrame.setContentPane (panel);
   waitingFrame.pack ();
 }
 
 public void HideLoadingTabletListenerMessage ()
 {
   waitingFrame.setVisible (true);
   waitingFrame.dispose ();
 }
 
 
 @Override
 public boolean isOpaque ()
 {
   return true;
 }
 
 // //////////////////////////////////////////////////////////////////////////////////////
 // Container and ScrollBars
 // //////////////////////////////////////////////////////////////////////////////////////
 
 public JPanel getCanvasContainer ()
 {
   if (container != null)
     {
       return container;
     }
 
   container = new JPanel ();
   container.setLayout (new GridBagLayout ());
 
   GridBagConstraints gbc = new GridBagConstraints ();
   gbc.fill = GridBagConstraints.BOTH;
   gbc.weightx = 1.;
   gbc.weighty = 1.;
   container.add (this, gbc);
 
   verticalScroll = new JScrollBar (Adjustable.VERTICAL);
   gbc = new GridBagConstraints ();
   gbc.fill = GridBagConstraints.VERTICAL;
   container.add (verticalScroll, gbc);
 
   horizontalScroll = new JScrollBar (Adjustable.HORIZONTAL);
   gbc = new GridBagConstraints ();
   gbc.gridx = 0;
   gbc.fill = GridBagConstraints.HORIZONTAL;
   container.add (horizontalScroll, gbc);
 
   updateScrollBars ();
 
   horizontalScroll.addAdjustmentListener (new AdjustmentListener ()
   {
     @Override
     public void adjustmentValueChanged (AdjustmentEvent e)
     {
       Point p = getOffset ();
       p.x = -e.getValue ();
       setOffset (p);
     }
   });
 
   verticalScroll.addAdjustmentListener (new AdjustmentListener ()
   {
     @Override
     public void adjustmentValueChanged (AdjustmentEvent e)
     {
       Point p = getOffset ();
       p.y = -e.getValue ();
       setOffset (p);
     }
   });
 
   return container;
 }
 
 void updateScrollBars ()
 {
   if (horizontalScroll == null || verticalScroll == null
           || horizontalScroll.getValueIsAdjusting () || verticalScroll.getValueIsAdjusting ())
     {
       return;
     }
 
   if (img == null)
     {
       horizontalScroll.setEnabled (false);
       verticalScroll.setEnabled (false);
     }
 
   Rectangle visibleRect = getRefreshArea (new CPRect (img.getWidth (null), img.getHeight (null)));
   updateScrollBar (horizontalScroll, visibleRect.x, visibleRect.width, getWidth (), -getOffset ().x);
   updateScrollBar (verticalScroll, visibleRect.y, visibleRect.height, getHeight (), -getOffset ().y);
 }
 
 
 private static void updateScrollBar (JScrollBar scroll, int visMin, int visWidth, int viewSize, int offset)
 {
   if (visMin >= 0 && visMin + visWidth < viewSize)
     {
       scroll.setEnabled (false);
     }
   else
     {
       scroll.setEnabled (true);
 
       int xMin = Math.min (0, (visMin - viewSize / 4));
       int xMax = Math.max (viewSize, visMin + visWidth + viewSize / 4);
 
       scroll.setValues (offset, viewSize, xMin + offset, xMax + offset);
       scroll.setBlockIncrement (Math.max (1, (int) (viewSize * .66)));
       scroll.setUnitIncrement (Math.max (1, (int) (viewSize * .05)));
     }
 }
 
 // //////////////////////////////////////////////////////////////////////////////////////
 // painting methods
 // //////////////////////////////////////////////////////////////////////////////////////
 
 @Override
 public void update (Graphics g)
 {
   paint (g);
 }
 
 @Override
 public void paint (Graphics g)
 {
   Graphics2D g2d = (Graphics2D) g;
 
   g2d.setColor (new Color (0x606060));
   g2d.fillRect (0, 0, getWidth (), getHeight ());
 
   if (!updateRegion.isEmpty ())
     {
       artwork.fusionLayers ();
       imgSource.newPixels (updateRegion.left, updateRegion.top, updateRegion.getWidth (), updateRegion.getHeight ());
       overlayImgSource.newPixels (updateRegion.left, updateRegion.top, updateRegion.getWidth (), updateRegion.getHeight ());
       updateRegion.makeEmpty ();
     }
 
   int w = img.getWidth (null);
   int h = img.getHeight (null);
 
   Graphics2D g2doc = (Graphics2D) g2d.create ();
   g2doc.transform (transform);
 
   // Draw the checkerboard pattern
   // we'll draw the pattern over an area larger than the image
   // and then remove the extra to avoid display problems
   // when the displayed bitmap doesn't match exactly with the checkerboard area
 
   GeneralPath path = getCheckerboardBackgroundPath (new Rectangle2D.Float (0, 0, w, h));
 
   // get the bounding rect and make it a bit larger to be sure to include everything
   Rectangle pathRect = path.getBounds ();
   pathRect.x -= 2;
   pathRect.y -= 2;
 
   g2d.setPaint (new TexturePaint (checkerboardPattern, new Rectangle (0, 0, 64, 64)));
   g2d.fill (pathRect);
 
   // Draw the image on the canvas
 
   if (interpolation)
     {
       RenderingHints hints = g2doc.getRenderingHints ();
       hints.put (RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
       g2doc.addRenderingHints (hints);
     }
 
   g2doc.drawImage (img, 0, 0, w, h, 0, 0, img.getWidth (null), img.getHeight (null), null);
 
   g2doc.setColor (Color.magenta);
 
   if (artwork.getShowOverlay ())
     {
       g2doc.drawImage (overlayImg, 0, 0, w, h, 0, 0, overlayImg.getWidth (null), overlayImg.getHeight (null), null);
     }
 
   // Redraw over the checkerboard border, removing a just a little bit of the image to avoid display problems
   path.append (pathRect, false);
   path.setWindingRule (Path2D.WIND_EVEN_ODD);
   g2d.setColor (new Color (0x606060));
   g2d.fill (path);
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
 
   // Drawing Selection
   if (palettesShown && showSelection)
     artwork.getCurSelection ().drawItself (g2d, this);
 
   // Draw grid
   if (showGrid)
     {
       Stroke stroke = g2d.getStroke ();
       g2d.setStroke (new BasicStroke (1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, null, 0f));
 
       CPRect size = artwork.getSize ();
       for (int i = gridSize - 1; i < size.getWidth (); i += gridSize)
         {
           Point2D.Float p1 = coordToDisplay (new Point2D.Float (i, 0));
           Point2D.Float p2 = coordToDisplay (new Point2D.Float (i, size.getHeight () - 1));
           g2d.draw (new Line2D.Float (p1, p2));
         }
 
       for (int i = gridSize - 1; i < size.getHeight (); i += gridSize)
         {
           Point2D.Float p1 = coordToDisplay (new Point2D.Float (0, i));
           Point2D.Float p2 = coordToDisplay (new Point2D.Float (size.getWidth () - 1, i));
           g2d.draw (new Line2D.Float (p1, p2));
         }
 
       g2d.setStroke (stroke);
     }
 
   // Additional drawing by the current mode
   getActiveMode ().paint (g2d);
   if (prevMode != null && drawPrevMode)
     prevMode.paint (g2d);
 
   // This bit of code is used to test repaint areas
     /*
      * if((test++ & 16) == 0) { g.setColor(Color.magenta); Dimension dd = getSize();
 		 * g.fillRect(0,0,dd.width,dd.height); g.setColor(Color.black); }
 		 */
 
   // Adding * for unsaved changes, TODO: Move it to UndoManager finalizer
   if (controller.isRunningAsApplication ())
     ((CPControllerApplication) controller).updateChanges (artwork.getUndoManager ().getUndoList ().size () > 0 ? artwork.getUndoManager ().getUndoList ().getFirst () : null,
                                                           artwork.getUndoManager ().getRedoList ().size () > 0 ? artwork.getUndoManager ().getRedoList ().getFirst () : null);
 }
 
 private GeneralPath getCheckerboardBackgroundPath (Rectangle2D r)
 {
   GeneralPath path = new GeneralPath ();
   float delta = (canvasRotation == 0f ? 0f : 1f / zoom);
 
   Point2D.Float p = coordToDisplay (new Point2D.Float ((float) r.getX () + delta, (float) r.getY () + delta));
   path.moveTo (p.x, p.y);
 
   p = coordToDisplay (new Point2D.Float ((float) r.getMaxX () - delta, (float) r.getY () + delta));
   path.lineTo (p.x, p.y);
 
   p = coordToDisplay (new Point2D.Float ((float) r.getMaxX () - delta, (float) r.getMaxY () - delta));
   path.lineTo (p.x, p.y);
 
   p = coordToDisplay (new Point2D.Float ((float) r.getX () + delta, (float) r.getMaxY () - delta));
   path.lineTo (p.x, p.y);
   path.closePath ();
 
   return path;
 }
 
 //
 // Mouse input methods
 //
 
 @Override
 public void mouseEntered (MouseEvent e)
 {
   cursorIn = true;
 }
 
 @Override
 public void mouseExited (MouseEvent e)
 {
   cursorIn = false;
   repaint ();
 }
 
 @Override
 public void mousePressed (MouseEvent e)
 {
   setModifiers (e.getModifiersEx ());
   setCursorX (e.getX ());
   setCursorY (e.getY ());
   setButton (e.getButton ());
   requestFocusInWindow ();
   getActiveMode ().cursorPressAction ();
 }
 
 @Override
 public void mouseDragged (MouseEvent e)
 {
   setCursorX (e.getX ());
   setCursorY (e.getY ());
   setLastPressure (CPTablet.getRef ().getPressure ());
   getActiveMode ().cursorDragAction ();
 }
 
 @Override
 public void mouseReleased (MouseEvent e)
 {
   setModifiers (e.getModifiersEx ());
   setButton (e.getButton ());
   getActiveMode ().cursorReleaseAction ();
 }
 
 @Override
 public void mouseMoved (MouseEvent e)
 {
   setCursorX (e.getX ());
   setCursorY (e.getY ());
   setLastPressure (CPTablet.getRef ().getPressure ());
 
   if (!isDontStealFocus ())
     {
       requestFocusInWindow ();
     }
   getActiveMode ().cursorMoveAction ();
   CPTablet.getRef ().mouseDetect ();
 }
 
 @Override
 public void mouseWheelMoved (MouseWheelEvent e)
 {
   float factor = 1;
   if (e.getWheelRotation () > 0)
     {
       factor = 1 / 1.15f;
     }
   if (e.getWheelRotation () < 0)
     {
       factor = 1.15f;
     }
 
   Point2D.Float pf = coordToDocument (new Point2D.Float (getCursorX (), getCursorY ()));
  zoomOnPoint (getZoom () * factor, getCursorX (), getCursorY ());
 }
 
 // //////////////////////////////////////////////////////////////////////////////////////
 // Transformation methods
 // //////////////////////////////////////////////////////////////////////////////////////
 
 // Zoom
 
 public void setZoom (float zoom)
 {
   this.zoom = zoom;
   updateTransform ();
 }
 
 public float getZoom ()
 {
   return zoom;
 }
 
 // Offset
 
 void setOffset (Point p)
 {
   setOffset (p.x, p.y);
 }
 
 void setOffset (int x, int y)
 {
   offsetX = x;
   offsetY = y;
   updateTransform ();
 }
 
 Point getOffset ()
 {
   return new Point (offsetX, offsetY);
 }
 
 // Rotation
 
 void setRotation (float angle)
 {
   canvasRotation = (float) (angle % (2 * Math.PI));
   updateTransform ();
 }
 
 float getRotation ()
 {
   return canvasRotation;
 }
 
 public void setApplyToAllLayers (boolean enabled)
 {
   applyToAllLayers = enabled;
 }
 
 public boolean getApplyToAllLayers ()
 {
   return applyToAllLayers;
 }
 
 public void setInterpolation (boolean enabled)
 {
   interpolation = enabled;
   repaint ();
 }
 
 // Update the affine transformation
 
 void updateTransform ()
 {
   transform.setToIdentity ();
   transform.translate (offsetX, offsetY);
   transform.scale (zoom, zoom);
   transform.rotate (canvasRotation);
 
   updateScrollBars ();
   repaint ();
 }
 
 // More advanced zoom methods
 
 public void zoomOnCenter (float zoomArg)
 {
   Dimension d = getSize ();
   zoomOnPoint (zoomArg, d.width / 2, d.height / 2);
 }
 
 void zoomOnPoint (float zoomArg, int centerX, int centerY)
 {
   float zoom_ = zoomArg;
   zoom_ = Math.max (minZoom, Math.min (maxZoom, zoom_));
   if (getZoom () != zoom_)
     {
       Point offset = getOffset ();
       setOffset (offset.x + (int) ((centerX - offset.x) * (1 - zoom_ / getZoom ())), offset.y
               + (int) ((centerY - offset.y) * (1 - zoom_ / getZoom ())));
       setZoom (zoom_);
 
       updateViewInfo ();
     }
 }
 
 private void updateViewInfo ()
 {
   CPCommonController.CPViewInfo viewInfo = new CPCommonController.CPViewInfo ();
   viewInfo.zoom = zoom;
   viewInfo.offsetX = offsetX;
   viewInfo.offsetY = offsetY;
   viewInfo.height = artwork.getHeight ();
   viewInfo.width = artwork.getWidth ();
   controller.callViewListeners (viewInfo);
 
   repaint ();
 }
 
 public void zoomIn ()
 {
   zoomOnCenter (getZoom () * 2);
 }
 
 public void zoomOut ()
 {
   zoomOnCenter (getZoom () * .5f);
 }
 
 public void zoom100 ()
 {
   resetRotation ();
   zoomOnCenter (1);
   centerCanvas ();
 }
 
 void centerCanvas ()
 {
   Dimension d = getSize ();
   setOffset ((d.width - (int) (artwork.getWidth () * getZoom ())) / 2,
              (d.height - (int) (artwork.getHeight () * getZoom ())) / 2);
   repaint ();
 }
 
 public void resetRotation ()
 {
   Dimension d = getSize ();
   Point2D.Float center = new Point2D.Float (d.width / 2.f, d.height / 2.f);
 
   AffineTransform rotTrans = new AffineTransform ();
   rotTrans.rotate (-getRotation (), center.x, center.y);
   rotTrans.concatenate (transform);
 
   setOffset ((int) rotTrans.getTranslateX (), (int) rotTrans.getTranslateY ());
   setRotation (0);
 }
 
 //
 // Coordinates and refresh areas methods
 //
 
 Point2D.Float coordToDocument (Point2D p)
 {
   Point2D.Float result = new Point2D.Float ();
 
   try
     {
       transform.inverseTransform (p, result);
     }
   catch (NoninvertibleTransformException ex)
     {
       return result;
     }
 
   return result;
 }
 
 Point coordToDocumentInt (Point2D p)
 {
   Point2D.Float result = new Point2D.Float ();
 
   try
     {
       transform.inverseTransform (p, result);
     }
   catch (NoninvertibleTransformException ex)
     {
       return new Point (0, 0);
     }
 
   return new Point ((int) Math.round (result.x), (int) Math.round (result.y));
 }
 
 public Point2D.Float coordToDisplay (Point2D p)
 {
   Point2D.Float result = new Point2D.Float ();
 
   transform.transform (p, result);
 
   return result;
 }
 
 Point coordToDisplayInt (Point2D p)
 {
   Point2D.Float result = new Point2D.Float ();
 
   transform.transform (p, result);
 
   return new Point ((int) Math.round (result.x), (int) Math.round (result.y));
 }
 
 Polygon coordToDisplay (CPRect r)
 {
   Polygon poly = new Polygon ();
 
   Point p = coordToDisplayInt (new Point (r.left, r.top));
   poly.addPoint (p.x, p.y);
 
   p = coordToDisplayInt (new Point (r.right, r.top));
   poly.addPoint (p.x, p.y);
 
   p = coordToDisplayInt (new Point (r.right, r.bottom));
   poly.addPoint (p.x, p.y);
 
   p = coordToDisplayInt (new Point (r.left, r.bottom));
   poly.addPoint (p.x, p.y);
 
   return poly;
 }
 
 Rectangle getRefreshArea (CPRect r)
 {
   Point p1 = coordToDisplayInt (new Point (r.left - 1, r.top - 1));
   Point p2 = coordToDisplayInt (new Point (r.left - 1, r.bottom));
   Point p3 = coordToDisplayInt (new Point (r.right, r.top - 1));
   Point p4 = coordToDisplayInt (new Point (r.right, r.bottom));
 
   Rectangle r2 = new Rectangle ();
 
   r2.x = Math.min (Math.min (p1.x, p2.x), Math.min (p3.x, p4.x));
   r2.y = Math.min (Math.min (p1.y, p2.y), Math.min (p3.y, p4.y));
   r2.width = Math.max (Math.max (p1.x, p2.x), Math.max (p3.x, p4.x)) - r2.x + 1;
   r2.height = Math.max (Math.max (p1.y, p2.y), Math.max (p3.y, p4.y)) - r2.y + 1;
 
   r2.grow (2, 2); // to be sure to include everything
 
   return r2;
 }
 
 void repaintBrushPreview ()
 {
   if (oldPreviewRect != null)
     {
       Rectangle r = oldPreviewRect;
       oldPreviewRect = null;
       repaint (r.x, r.y, r.width, r.height);
     }
 }
 
 Rectangle getBrushPreviewOval (boolean calcPressureArg)
 {
   // TODO: Disable ability to turn on PressureSize for M_SMUDGE and M_OIL
   boolean calcPressure = calcPressureArg;
   if (controller.getBrushInfo ().paintMode == CPBrushInfo.M_SMUDGE || controller.getBrushInfo ().paintMode == CPBrushInfo.M_OIL)
     calcPressure = false;
   int bSize = (int) (controller.getBrushSize () * zoom * (calcPressure && controller.getBrushInfo ().pressureSize ? getLastPressure () : 1.0));
   return new Rectangle (getCursorX () - bSize / 2, getCursorY () - bSize / 2, bSize, bSize);
 }
 
 //
 // ChibiPaintMod interfaces methods
 //
 
 @Override
 public void newTool (int tool, CPBrushInfo toolInfo)
 {
   if (curSelectedMode == curDrawMode)
     {
       curSelectedMode = drawingModes[toolInfo.strokeMode];
     }
   curDrawMode = drawingModes[toolInfo.strokeMode];
 
   if (!spacePressed && cursorIn)
     {
       Rectangle r = getBrushPreviewOval (false);
       r.grow (2, 2);
       if (oldPreviewRect != null)
         {
           r = r.union (oldPreviewRect);
           oldPreviewRect = null;
         }
 
       repaint (r.x, r.y, r.width, r.height);
     }
 }
 
 public void initTransform ()
 {
 // Here is the special case we should remove selected part of active layer, cutSelected off inactive parts of selection
 // Then show the controls for doing transformation and operate the exact pixels which were removed.
   if (!artwork.initializeTransform (controller))
     return;
 
   setActiveMode (freeTransformMode);
   artwork.invalidateFusion ();
   updateTransformCursor ();
   setEnabledForTransform (false);
   if (isRunningAsApplication ())
     {
       ((CPControllerApplication) controller).setTransformState (true);
     }
 }
 
 @Override
 public void modeChange (int mode)
 {
   switch (mode)
     {
     case CPCommonController.M_DRAW:
       curSelectedMode = curDrawMode;
       break;
 
     case CPCommonController.M_FLOODFILL:
       curSelectedMode = floodFillMode;
       break;
 
     case CPCommonController.M_MAGIC_WAND:
       curSelectedMode = magicWandMode;
       break;
 
     case CPCommonController.M_FREE_SELECTION:
       curSelectedMode = freeSelectionMode;
       break;
 
     case CPCommonController.M_RECT_SELECTION:
       curSelectedMode = rectMode;
       break;
 
     case CPCommonController.M_ROTATE_CANVAS:
       curSelectedMode = rotateCanvasMode;
       break;
     }
 }
 
 //
 // misc overloaded and interface standard methods
 //
 
 @Override
 public Dimension getPreferredSize ()
 {
   return new Dimension ((int) (artwork.getWidth () * zoom), (int) (artwork.getHeight () * zoom));
 }
 
 @Override
 public void componentResized (ComponentEvent e)
 {
   centerCanvas ();
   repaint ();
 }
 
 public void copy ()
 {
   artwork.copySelected (!isRunningAsApplication ());
 }
 
 public void copyMerged ()
 {
   artwork.copySelectedMerged (!isRunningAsApplication ());
 }
 
 public void cut ()
 {
   artwork.cutSelected (!isRunningAsApplication ());
 }
 
 public void paste ()
 {
   artwork.pasteFromClipboard (!isRunningAsApplication ());
 }
 
 @Override
 public void keyPressed (KeyEvent e)
 {
   if (e.getKeyCode () == KeyEvent.VK_SPACE)
     {
       spacePressed = true;
       repaintBrushPreview ();
       if (getActiveMode () == defaultMode)
         {
           setCursor (moveCursor);
         }
     }
 
   // FIXME: these should probably go through the controller to be dispatched
   if ((e.getModifiers () & InputEvent.CTRL_MASK) != 0)
     {
       switch (e.getKeyCode ())
         {
         // case KeyEvent.VK_ADD:
         case KeyEvent.VK_EQUALS:
           zoomIn ();
           return;
         // case KeyEvent.VK_SUBTRACT:
         case KeyEvent.VK_MINUS:
           zoomOut ();
           return;
         case KeyEvent.VK_0:
           // case KeyEvent.VK_NUMPAD0:
           zoom100 ();
           return;
         }
     }
   else
     {
       switch (e.getKeyCode ())
         {
         case KeyEvent.VK_ADD:
         case KeyEvent.VK_EQUALS:
           zoomIn ();
           return;
         case KeyEvent.VK_SUBTRACT:
         case KeyEvent.VK_MINUS:
           zoomOut ();
           return;
         case KeyEvent.VK_1:
           controller.setAlpha ((int) (.1f * 255));
           return;
         case KeyEvent.VK_2:
           controller.setAlpha ((int) (.2f * 255));
           return;
         case KeyEvent.VK_3:
           controller.setAlpha ((int) (.3f * 255));
           return;
         case KeyEvent.VK_4:
           controller.setAlpha ((int) (.4f * 255));
           return;
         case KeyEvent.VK_5:
           controller.setAlpha ((int) (.5f * 255));
           return;
         case KeyEvent.VK_6:
           controller.setAlpha ((int) (.6f * 255));
           return;
         case KeyEvent.VK_7:
           controller.setAlpha ((int) (.7f * 255));
           return;
         case KeyEvent.VK_8:
           controller.setAlpha ((int) (.8f * 255));
           return;
         case KeyEvent.VK_9:
           controller.setAlpha ((int) (.9f * 255));
           return;
         case KeyEvent.VK_0:
           controller.setAlpha (255);
           return;
         case KeyEvent.VK_OPEN_BRACKET:
           if (controller.getBrushInfo ().curSize < 200.0f)
             controller.getBrushInfo ().curSize += 1.0f;
           return;
         }
     }
   activeMode.keyPressed (e);
 }
 
 @Override
 public void keyReleased (KeyEvent e)
 {
   if (e.getKeyCode () == KeyEvent.VK_SPACE)
     {
       spacePressed = false;
       if (getActiveMode () == defaultMode)
         {
           setCursor (defaultCursor);
         }
     }
 }
 
 @Override
 public void keyTyped (KeyEvent e)
 {
   switch (e.getKeyChar ())
     {
     case '[':
       controller.setBrushSize (controller.getBrushSize () - 1);
       break;
     case ']':
       controller.setBrushSize (controller.getBrushSize () + 1);
       break;
     }
 }
 
 @Override
 public boolean isFocusable ()
 {
   return true;
 }
 
 // Unused interface methods
 @Override
 public void mouseClicked (MouseEvent e)
 {
   // To implement interface
 }
 
 @Override
 public void componentHidden (ComponentEvent e)
 {
   // To implement interface
 }
 
 @Override
 public void componentMoved (ComponentEvent e)
 {
   // To implement interface
 }
 
 @Override
 public void componentShown (ComponentEvent e)
 {
   // To implement interface
 }
 
 @Override
 public void updateRegion (CPArtwork artworkArg, CPRect region)
 {
   updateRegion.union (region);
 
   Rectangle r = getRefreshArea (region);
   repaint (r.x, r.y, r.width, r.height);
 }
 
 @Override
 public void layerChange (CPArtwork artworkArg)
 {
   // unused method
 }
 
 public void showGrid (boolean show)
 {
   showGrid = show;
   repaint ();
 }
 
 public void saveCanvasSettings ()
 {
   Preferences userRoot = Preferences.userRoot ();
   Preferences preferences = userRoot.node ("chibipaintmod");
   preferences.putBoolean ("Lock Alpha", artwork.isLockAlpha ());
   preferences.putBoolean ("Interpolation", interpolation);
   preferences.putBoolean ("Show Grid", showGrid);
   preferences.putBoolean ("Show Selection", showSelection);
   preferences.putInt ("Grid size", gridSize);
 }
 
 public void killTimers ()
 {
   if (selectionUpdateTimer != null)
     {
       selectionUpdateTimer.stop ();
       selectionUpdateTimer = null;
     }
 }
 
 public void loadCanvasSettings ()
 {
   Preferences userRoot = Preferences.userRoot ();
   Preferences preferences = userRoot.node ("chibipaintmod");
   setInterpolation (preferences.getBoolean ("Interpolation", interpolation));
   setApplyToAllLayers (preferences.getBoolean ("Apply to All Layers", applyToAllLayers));
   setShowSelection (preferences.getBoolean ("Show Selection", showSelection));
   showGrid (preferences.getBoolean ("Show Grid", showGrid));
   artwork.setLockAlpha (preferences.getBoolean ("Lock Alpha", artwork.isLockAlpha ()));
   controller.getMainGUI ().setSelectedByCmdId (CPCommandId.LinearInterpolation, interpolation);
   controller.getMainGUI ().setSelectedByCmdId (CPCommandId.ApplyToAllLayers, applyToAllLayers);
   controller.getMainGUI ().setSelectedByCmdId (CPCommandId.ShowGrid, showGrid);
   controller.getMainGUI ().setSelectedByCmdId (CPCommandId.ShowSelection, showSelection);
   gridSize = preferences.getInt ("Grid size", gridSize);
 }
 
 //
 // base class for the different modes
 //
 
 int getCursorX ()
 {
   return cursorX;
 }
 
 public void setCursorX (int cursorX)
 {
   this.cursorX = cursorX;
 }
 
 int getCursorY ()
 {
   return cursorY;
 }
 
 public void setCursorY (int cursorY)
 {
   this.cursorY = cursorY;
 }
 
 float getLastPressure ()
 {
   return lastPressure;
 }
 
 public void setLastPressure (float lastPressure)
 {
   this.lastPressure = lastPressure;
 }
 
 public boolean isDontStealFocus ()
 {
   return dontStealFocus;
 }
 
 public void setDontStealFocus (boolean dontStealFocus)
 {
   this.dontStealFocus = dontStealFocus;
 }
 
 public CPMode getActiveMode ()
 {
   return activeMode;
 }
 
 void setActiveMode (CPMode activeMode)
 {
   prevMode = this.activeMode;
   this.activeMode = activeMode;
 }
 
 int getModifiers ()
 {
   return modifiers;
 }
 
 public void setModifiers (int modifiers)
 {
   this.modifiers = modifiers;
 }
 
 int getButton ()
 {
   return button;
 }
 
 public void setButton (int button)
 {
   this.button = button;
 }
 
 public void setCursorIn (boolean cursorIn)
 {
   this.cursorIn = cursorIn;
 }
 
 public void setShowSelection (boolean showSelection)
 {
   this.showSelection = showSelection;
 }
 
 public void setPalettesShown (boolean palettesShown)
 {
   this.palettesShown = palettesShown;
 }
 
 
 public abstract class CPMode
 {
 
 // Mouse Input
 
   public void cursorPressAction ()
   {
     // To not define actions for some functions in every case
   }
 
   public void cursorReleaseAction ()
   {
     // To not define actions for some functions in every case
   }
 
 
   public void cursorClickAction ()
   {
 
   }
 
   public void cursorMoveAction ()
   {
     // To not define actions for some functions in every case
   }
 
   public void cursorDragAction ()
   {
     // To not define actions for some functions in every case
   }
 
   // GUI drawing
   public void paint (Graphics2D g2d)
   {
     // To not define actions for some functions in every case
   }
 
   public void keyPressed (KeyEvent e)
   {
 
   }
 }
 
 //
 // Default UI Mode when not doing anything: used to start the other modes
 //
 
 //
 // /!\ WARNING: always use getModifiersEx() to test for modifier
 // keys as methods like isAltDown() have unexpected results
 //
 
 class CPDefaultMode extends CPMode
 {
 
   @Override
   public void cursorPressAction ()
   {
     if (!spacePressed && getButton () == MouseEvent.BUTTON1
             && (((getModifiers () & InputEvent.ALT_DOWN_MASK) == 0)))
       {
 
         if (!artwork.getActiveLayer ().isVisible () && curSelectedMode != rotateCanvasMode)
           {
             repaintBrushPreview ();
             return; // don't draw on a hidden layer
           }
         repaintBrushPreview ();
         setActiveMode (curSelectedMode);
         getActiveMode ().cursorPressAction ();
       }
     else if (!spacePressed
             && (getButton () == MouseEvent.BUTTON3 || (getButton () == MouseEvent.BUTTON1 && ((
             getModifiers () & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK))))
       {
         repaintBrushPreview ();
 
         setActiveMode (colorPickerMode);
         getActiveMode ().cursorPressAction ();
       }
     else if ((getButton () == MouseEvent.BUTTON2 || spacePressed)
             && ((getModifiers () & InputEvent.ALT_DOWN_MASK) == 0))
       {
         repaintBrushPreview ();
         drawPrevMode = false;
         setActiveMode (moveCanvasMode);
         getActiveMode ().cursorPressAction ();
       }
     else if ((getButton () == MouseEvent.BUTTON2 || spacePressed)
             && ((getModifiers () & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK))
       {
         repaintBrushPreview ();
         drawPrevMode = false;
         setActiveMode (rotateCanvasMode);
         getActiveMode ().cursorPressAction ();
       }
 
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     if (curSelectedMode == curDrawMode && cursorIn)
       {
         Rectangle r;
         r = getBrushPreviewOval (false);
         g2d.setXORMode (Color.WHITE);
         g2d.drawOval (r.x, r.y, r.width, r.height);
         r.grow (2, 2);
         oldPreviewRect = oldPreviewRect != null ? r.union (oldPreviewRect) : r;
       }
   }
 
   @Override
   public void cursorMoveAction ()
   {
     Point p = new Point (getCursorX (), getCursorY ());
     if (!spacePressed && cursorIn)
       {
         Rectangle r = getBrushPreviewOval (false);
         r.grow (2, 2);
         if (oldPreviewRect != null)
           {
             r = r.union (oldPreviewRect);
             oldPreviewRect = null;
           }
 
         Point2D.Float pf = coordToDocument (p);
         if (artwork.isPointWithin (pf.x, pf.y))
           {
             setCursor (defaultCursor); // FIXME find a cursor that everyone likes
           }
         else
           {
             setCursor (defaultCursor);
           }
 
         repaint (r.x, r.y, r.width, r.height);
       }
   }
 }
 
 //
 // Freehand mode
 //
 
 // FIXME: dragLeft no longer necessary, should not specify the drag button
 
 void setUndoRedoEnabled (boolean isEnabled)
 {
   controller.getMainGUI ().setEnabledByCmdId (CPCommandId.Undo, isEnabled);
   controller.getMainGUI ().setEnabledByCmdId (CPCommandId.Redo, isEnabled);
 }
 
 class CPFreehandMode extends CPMode
 {
 
   boolean dragLeft = false;
   Point2D.Float smoothMouse = new Point2D.Float (0, 0);
 
   @Override
   public void cursorPressAction ()
   {
     if (!dragLeft && getButton () == MouseEvent.BUTTON1)
       {
         Point p = new Point (getCursorX (), getCursorY ());
         Point2D.Float pf = coordToDocument (p);
 
         dragLeft = true;
         artwork.beginStroke (pf.x, pf.y, getLastPressure ());
 
         smoothMouse = (Point2D.Float) pf.clone ();
         setUndoRedoEnabled (false);
       }
   }
 
   @Override
   public void cursorDragAction ()
   {
     Point p = new Point (getCursorX (), getCursorY ());
     Point2D.Float pf = coordToDocument (p);
 
     float smoothing = Math.min (.999f, (float) Math.pow (controller.getBrushInfo ().smoothing, .3));
 
     smoothMouse.x = (1f - smoothing) * pf.x + smoothing * smoothMouse.x;
     smoothMouse.y = (1f - smoothing) * pf.y + smoothing * smoothMouse.y;
 
     if (dragLeft)
       {
         artwork.continueStroke (smoothMouse.x, smoothMouse.y, getLastPressure ());
       }
 
     Rectangle r = getBrushPreviewOval (true);
     r.grow (2, 2);
     if (oldPreviewRect != null)
       {
         r = r.union (oldPreviewRect);
         oldPreviewRect = null;
       }
 
     if (artwork.isPointWithin (pf.x, pf.y))
       {
         setCursor (defaultCursor); // FIXME find a cursor that everyone likes
       }
     else
       {
         setCursor (defaultCursor);
       }
 
     repaint (r.x, r.y, r.width, r.height);
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     if (dragLeft && getButton () == MouseEvent.BUTTON1)
       {
         dragLeft = false;
         artwork.endStroke ();
         setUndoRedoEnabled (true);
         setActiveMode (defaultMode); // yield control to the default mode
       }
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     if (curSelectedMode == curDrawMode)
       {
         Rectangle r = getBrushPreviewOval (true);
         g2d.drawOval (r.x, r.y, r.width, r.height);
 
         r.grow (2, 2);
         oldPreviewRect = oldPreviewRect != null ? r.union (oldPreviewRect) : r;
       }
   }
 }
 
 //
 // Line drawing mode
 //
 
 class CPLineMode extends CPMode
 {
 
   boolean dragLine = false;
   Point dragLineFrom, dragLineTo;
 
   @Override
   public void cursorPressAction ()
   {
     if (!dragLine && getButton () == MouseEvent.BUTTON1)
       {
         Point p = new Point (getCursorX (), getCursorY ());
 
         dragLine = true;
         dragLineFrom = dragLineTo = (Point) p.clone ();
       }
   }
 
   @Override
   public void cursorDragAction ()
   {
     Point p = new Point (getCursorX (), getCursorY ());
 
     Rectangle r = new Rectangle (Math.min (dragLineFrom.x, dragLineTo.x), Math.min (dragLineFrom.y, dragLineTo.y),
                                  Math.abs (dragLineFrom.x - dragLineTo.x) + 1, Math.abs (dragLineFrom.y - dragLineTo.y) + 1);
     r = r.union (new Rectangle (Math.min (dragLineFrom.x, p.x), Math.min (dragLineFrom.y, p.y), Math
             .abs (dragLineFrom.x - p.x) + 1, Math.abs (dragLineFrom.y - p.y) + 1));
     dragLineTo = (Point) p.clone ();
     repaint (r.x, r.y, r.width, r.height);
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     if (dragLine && getButton () == MouseEvent.BUTTON1)
       {
         Point p = new Point (getCursorX (), getCursorY ());
         Point2D.Float pf = coordToDocument (p);
 
         dragLine = false;
 
         Point2D.Float from = coordToDocument (dragLineFrom);
         artwork.beginStroke (from.x, from.y, 1);
         artwork.continueStroke (pf.x, pf.y, 1);
         artwork.endStroke ();
 
         Rectangle r = new Rectangle (Math.min (dragLineFrom.x, dragLineTo.x), Math.min (dragLineFrom.y,
                                                                                         dragLineTo.y), Math.abs (dragLineFrom.x - dragLineTo.x) + 1, Math.abs (dragLineFrom.y
                                                                                                                                                                        - dragLineTo.y) + 1);
         repaint (r.x, r.y, r.width, r.height);
 
         setActiveMode (defaultMode); // yield control to the default mode
       }
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     if (dragLine)
       {
         g2d.drawLine (dragLineFrom.x, dragLineFrom.y, dragLineTo.x, dragLineTo.y);
       }
   }
 
 
 }
 
 //
 // Bezier drawing mode
 //
 
 class CPBezierMode extends CPMode
 {
 
   // bezier drawing
   static final int BEZIER_POINTS = 500;
   static final int BEZIER_POINTS_PREVIEW = 100;
 
   boolean dragBezier = false;
   int dragBezierMode; // 0 Initial drag, 1 first control point, 2 second point
   Point2D.Float dragBezierP0, dragBezierP1, dragBezierP2, dragBezierP3;
 
   @Override
   public void cursorPressAction ()
   {
     Point2D.Float p = coordToDocument (new Point (getCursorX (), getCursorY ()));
 
     if (!dragBezier && !spacePressed && getButton () == MouseEvent.BUTTON1)
       {
         dragBezier = true;
         dragBezierMode = 0;
         dragBezierP0 = dragBezierP1 = dragBezierP2 = dragBezierP3 = (Point2D.Float) p.clone ();
       }
   }
 
   @Override
   public void cursorDragAction ()
   {
     Point2D.Float p = coordToDocument (new Point (getCursorX (), getCursorY ()));
 
     if (dragBezier && dragBezierMode == 0)
       {
         dragBezierP2 = dragBezierP3 = (Point2D.Float) p.clone ();
         repaint ();
       }
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     if (dragBezier && getButton () == MouseEvent.BUTTON1)
       {
         if (dragBezierMode == 0)
           {
             dragBezierMode = 1;
           }
         else if (dragBezierMode == 1)
           {
             dragBezierMode = 2;
           }
         else if (dragBezierMode == 2)
           {
             dragBezier = false;
 
             Point2D.Float p0 = dragBezierP0;
             Point2D.Float p1 = dragBezierP1;
             Point2D.Float p2 = dragBezierP2;
             Point2D.Float p3 = dragBezierP3;
 
             CPBezier bezier = new CPBezier ();
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
 
             bezier.compute (x, y, BEZIER_POINTS);
 
             artwork.beginStroke (x[0], y[0], 1);
             for (int i = 1; i < BEZIER_POINTS; i++)
               {
                 artwork.continueStroke (x[i], y[i], 1);
               }
             artwork.endStroke ();
             repaint ();
 
             setActiveMode (defaultMode); // yield control to the default mode
           }
       }
   }
 
   @Override
   public void cursorMoveAction ()
   {
     Point2D.Float p_document = coordToDocument (new Point (getCursorX (), getCursorY ()));
 
     if (dragBezier && dragBezierMode == 1)
       {
         dragBezierP1 = (Point2D.Float) p_document.clone ();
         repaint (); // FIXME: repaint only the bezier region
       }
 
     if (dragBezier && dragBezierMode == 2)
       {
         dragBezierP2 = (Point2D.Float) p_document.clone ();
         repaint (); // FIXME: repaint only the bezier region
       }
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     if (dragBezier)
       {
         CPBezier bezier = new CPBezier ();
 
         Point2D.Float p0 = coordToDisplay (dragBezierP0);
         Point2D.Float p1 = coordToDisplay (dragBezierP1);
         Point2D.Float p2 = coordToDisplay (dragBezierP2);
         Point2D.Float p3 = coordToDisplay (dragBezierP3);
 
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
         bezier.compute (x, y, BEZIER_POINTS_PREVIEW);
 
         g2d.drawPolyline (x, y, BEZIER_POINTS_PREVIEW);
         g2d.drawLine ((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
         g2d.drawLine ((int) p2.x, (int) p2.y, (int) p3.x, (int) p3.y);
       }
   }
 
 
 }
 
 //
 // Color picker mode
 //
 
 class CPColorPickerMode extends CPMode
 {
 
   int mouseButton;
 
   @Override
   public void cursorPressAction ()
   {
     Point p = new Point (getCursorX (), getCursorY ());
     Point2D.Float pf = coordToDocument (p);
 
     mouseButton = getButton ();
 
     if (artwork.isPointWithin (pf.x, pf.y))
       {
         controller.setCurColorRgb (artwork.colorPicker (pf.x, pf.y));
       }
 
     setCursor (crossCursor);
   }
 
   @Override
   public void cursorDragAction ()
   {
     Point p = new Point (getCursorX (), getCursorY ());
     Point2D.Float pf = coordToDocument (p);
 
     if (artwork.isPointWithin (pf.x, pf.y))
       {
         controller.setCurColorRgb (artwork.colorPicker (pf.x, pf.y));
       }
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     if (getButton () == mouseButton)
       {
         setCursor (defaultCursor);
         setActiveMode (defaultMode); // yield control to the default mode
       }
   }
 }
 
 //
 // Canvas move mode
 //
 
 class CPMoveCanvasMode extends CPMode
 {
 
   boolean dragMiddle = false;
   int dragMoveX, dragMoveY;
   Point dragMoveOffset;
   int dragMoveButton;
 
   @Override
   public void cursorPressAction ()
   {
     Point p = new Point (getCursorX (), getCursorY ());
 
     if (!dragMiddle && (getButton () == MouseEvent.BUTTON2 || spacePressed))
       {
         repaintBrushPreview ();
 
         dragMiddle = true;
         dragMoveButton = getButton ();
         dragMoveX = p.x;
         dragMoveY = p.y;
         dragMoveOffset = getOffset ();
         setCursor (moveCursor);
       }
   }
 
   @Override
   public void cursorDragAction ()
   {
     if (dragMiddle)
       {
         Point p = new Point (getCursorX (), getCursorY ());
 
         setOffset (dragMoveOffset.x + p.x - dragMoveX, offsetY = dragMoveOffset.y + p.y - dragMoveY);
         repaint ();
       }
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     if (dragMiddle && getButton () == dragMoveButton)
       {
         dragMiddle = false;
         setCursor (defaultCursor);
 
         setActiveMode (prevMode); // yield control to the previous mode
         prevMode = null;
       }
   }
 }
 
 //
 // Flood fill mode
 //
 
 abstract class CPGeneralFillMode extends CPMode
 {
   Point2D.Float cursorAnchorPos;
   int initialColorDistance;
   int floodFillActualColorDistance;
   Point2D.Float prevDocPoint;
   Timer updateTimer;
   protected boolean mindSelection;
 
   void updatePreview ()
   {
     calcColorDistance ();
     artwork.updateOverlayWithFloodfillPreview (cursorAnchorPos, floodFillActualColorDistance, cursorAnchorPos, mindSelection);
     updateRegion (artwork, artwork.getSize ());
   }
 
   CPGeneralFillMode ()
   {
     updateTimer = new Timer (50, new ActionListener ()
     {
       @Override
       public void actionPerformed (ActionEvent arg0)
       {
         updatePreview ();
       }
     });
     updateTimer.setRepeats (false);
   }
 
   @Override
   public void cursorPressAction ()
   {
     cursorAnchorPos = coordToDocument (new Point2D.Float (getCursorX (), getCursorY ()));
     initialColorDistance = controller.getColorDistance ();
   }
 
   private void calcColorDistance ()
   {
     floodFillActualColorDistance = initialColorDistance + ((int) cursorAnchorPos.getY () - (int) coordToDocument (new Point2D.Float (getCursorX (), getCursorY ())).getY ());
     if (floodFillActualColorDistance < 0)
       floodFillActualColorDistance = 0;
     if (floodFillActualColorDistance > 255)
       floodFillActualColorDistance = 255;
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     g2d.setColor (Color.WHITE);
     int x = getCursorX ();
     int y = getCursorY ();
     String str = "Threshold: " + floodFillActualColorDistance;
     FontMetrics fm = g2d.getFontMetrics ();
     Rectangle2D rect = fm.getStringBounds (str, g2d);
 
     g2d.fillRect (x,
                   y - fm.getAscent (),
                   (int) rect.getWidth (),
                   (int) rect.getHeight ());
     g2d.setColor (Color.BLACK);
     g2d.drawString (str, x, y);
   }
 
   @Override
   public void cursorDragAction ()
   {
     updatePreview ();
     Point p = new Point (getCursorX (), getCursorY ());
     Point2D.Float pf = coordToDocument (p);
     prevDocPoint = pf;
   }
 
   abstract void performFloodFill ();
 
   @Override
   public void cursorReleaseAction ()
   {
     artwork.cancelOverlayDrawing ();
     calcColorDistance ();
 
     if (artwork.isPointWithin (cursorAnchorPos.x, cursorAnchorPos.y))
       {
         performFloodFill ();
         artwork.finalizeUndo ();
         updateRegion (artwork, artwork.getSize ());
       }
 
     setActiveMode (defaultMode); // yield control to the default mode
   }
 
 
 }
 
 class CPFloodFillMode extends CPGeneralFillMode
 {
   CPFloodFillMode ()
   {
     mindSelection = true;
   }
 
   @Override
   void performFloodFill ()
   {
     artwork.performFloodFill (cursorAnchorPos.x, cursorAnchorPos.y, floodFillActualColorDistance);
   }
 }
 
 class CPMagicWandMode extends CPGeneralFillMode
 {
   CPMagicWandMode ()
   {
     mindSelection = false;
   }
 
   @Override
   void performFloodFill ()
   {
     artwork.performMagicWand (cursorAnchorPos.x, cursorAnchorPos.y, floodFillActualColorDistance, modifiersToSelectionApplianceType (getModifiers ()));
   }
 }
 
 //
 // CPRectSelection mode
 //
 
 class CPRectSelectionMode extends CPMode
 {
 
   Point firstClick;
   final CPRect curRect = new CPRect ();
 
   @Override
   public void cursorPressAction ()
   {
     Point p = coordToDocumentInt (new Point (getCursorX (), getCursorY ()));
 
     curRect.makeEmpty ();
     firstClick = p;
 
     repaint ();
   }
 
   @Override
   public void cursorDragAction ()
   {
     Point p = coordToDocumentInt (new Point (getCursorX (), getCursorY ()));
 
     int squareDist = Math.max (Math.abs (p.x - firstClick.x), Math.abs (p.y - firstClick.y));
     boolean square = (getModifiers () & InputEvent.ALT_MASK) != 0;
     if (p.x >= firstClick.x)
       {
         curRect.left = firstClick.x;
         curRect.right = square ? firstClick.x + squareDist : p.x;
       }
     else
       {
         curRect.left = square ? firstClick.x - squareDist : p.x;
         curRect.right = firstClick.x;
       }
 
     if (p.y >= firstClick.y)
       {
         curRect.top = firstClick.y;
         curRect.bottom = square ? firstClick.y + squareDist : p.y;
       }
     else
       {
         curRect.top = square ? firstClick.y - squareDist : p.y;
         curRect.bottom = firstClick.y;
       }
     repaint ();
   }
 
   @Override
   public void cursorReleaseAction ()
   {
 
     CPSelection Rect = new CPSelection (artwork.getWidth (), artwork.getHeight ());
     Rect.makeRectangularSelection (curRect);
     artwork.DoSelection (modifiersToSelectionApplianceType (getModifiers ()), Rect);
     artwork.finalizeUndo ();
 
     setActiveMode (defaultMode); // yield control to the default mode
     repaint ();
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     if (!curRect.isEmpty ())
       {
         g2d.setXORMode (Color.GRAY);
         g2d.draw (coordToDisplay (curRect));
       }
   }
 
 }
 
 void setEnabledForTransform (boolean enabled)
 {
   controller.getMainGUI ().setEnabledForTransform (enabled);
   CPCommandId viewItems[] = {CPCommandId.ZoomIn, CPCommandId.ZoomOut, CPCommandId.Zoom100, CPCommandId.About, CPCommandId.LinearInterpolation, CPCommandId.ShowGrid,
           CPCommandId.GridOptions, CPCommandId.TogglePalettes, CPCommandId.PalBrush, CPCommandId.PalColor, CPCommandId.PalLayers, CPCommandId.PalMisc, CPCommandId.PalStroke,
           CPCommandId.PalSwatches, CPCommandId.PalTextures, CPCommandId.PalTool, CPCommandId.LayerToggleAll};
   for (CPCommandId item : viewItems)
     controller.getMainGUI ().setEnabledByCmdId (item, true);
 }
 
 public void applyTransform ()
 {
   if (activeMode == freeTransformMode)
     {
       freeTransformMode.applyTransform ();
       setActiveMode (defaultMode);
       controller.callToolListeners ();
     }
 }
 
 public void cancelTransform ()
 {
   if (activeMode == freeTransformMode)
     {
       freeTransformMode.cancelTransform ();
       setActiveMode (defaultMode);
       controller.callToolListeners ();
     }
 }
 
 public void updateTransformCursor ()
 {
   Point2D.Float p = coordToDocument (new Point (getCursorX (), getCursorY ()));
   artwork.getTransformHandler ().updateCursor (p, this);
 }
 
 class CPFreeTransformMode extends CPMode
 {
   private CPTransformHandler transformHandler;
 
   public CPFreeTransformMode ()
   {
     super ();
   }
 
   @Override
   public void cursorMoveAction ()
   {
     updateTransformCursor ();
   }
 
   @Override
   public void cursorPressAction ()
   {
     if ((getButton () == MouseEvent.BUTTON2 || spacePressed)
             && ((getModifiers () & InputEvent.ALT_DOWN_MASK) == 0))
       {
         repaintBrushPreview ();
 
         drawPrevMode = true;
         setActiveMode (moveCanvasMode);
         getActiveMode ().cursorPressAction ();
         return;
       }
     else if ((getButton () == MouseEvent.BUTTON2 || spacePressed)
             && ((getModifiers () & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK))
       {
         repaintBrushPreview ();
 
         drawPrevMode = true;
         setActiveMode (rotateCanvasMode);
         getActiveMode ().cursorPressAction ();
         return;
       }
 
     Point2D.Float p = coordToDocument (new Point (getCursorX (), getCursorY ()));
     transformHandler.cursorPressed (p);
   }
 
   @Override
   public void cursorDragAction ()
   {
     Point2D.Float p = coordToDocument (new Point (getCursorX (), getCursorY ()));
     CPRect updatingRect = new CPRect (transformHandler.getRectNeededForUpdating ());
     transformHandler.cursorDragged (p);
     CPRect rectAfter = new CPRect (transformHandler.getRectNeededForUpdating ());
     updatingRect.union (rectAfter);
     artwork.invalidateFusion (updatingRect);
     repaint ();
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     transformHandler.cursorReleased ();
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     if (palettesShown)
       transformHandler.drawTransformHandles (g2d, transform);
   }
 
   public void setTransformHandler (CPTransformHandler transformHandlerArg)
   {
     transformHandler = transformHandlerArg;
   }
 
   public void keyPressed (KeyEvent e)
   {
     switch (e.getKeyCode ())
       {
       case KeyEvent.VK_ENTER:
         controller.performCommand (CPCommandId.ApplyTransform, null);
         break;
       case KeyEvent.VK_ESCAPE:
         controller.performCommand (CPCommandId.CancelTransform, null);
         break;
       case KeyEvent.VK_RIGHT:
       case KeyEvent.VK_LEFT:
       case KeyEvent.VK_UP:
       case KeyEvent.VK_DOWN:
         controller.performCommand (CPCommandId.MoveTransform, new CPCommandSettings.DirectionSettings (CPEnums.Direction.fromKeyEvent (e)));
       }
   }
 
   public void cancelTransform ()
   {
     transformHandler.stopTransform ();
     transformHandler.clearTransforms ();
     artwork.RestoreActiveLayerAndSelection ();
     prevMode = null;
     artwork.invalidateFusion ();
     setEnabledForTransform (true);
     if (isRunningAsApplication ())
       {
         ((CPControllerApplication) controller).setTransformState (false);
       }
     repaint ();
   }
 
   public void applyTransform ()
   {
     transformHandler.finalizeTransform ();
     transformHandler.clearTransforms ();
     artwork.FinishTransformUndo ();
     repaint ();
     setEnabledForTransform (true);
     if (isRunningAsApplication ())
       {
         ((CPControllerApplication) controller).setTransformState (false);
       }
   }
 
 }
 
 
 class CPFreeSelectionMode extends CPMode
 {
 
   Path2D polygon;
 
   @Override
   public void cursorPressAction ()
   {
     polygon = new Path2D.Float ();
     polygon.moveTo (getCursorX (), getCursorY ());
     repaint ();
   }
 
   @Override
   public void cursorDragAction ()
   {
     polygon.lineTo (getCursorX (), getCursorY ());
 
     // TODO: add shift and control modifiers for snapped by angle lines
     repaint ();
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     CPSelection polygonSelection = new CPSelection (artwork.getWidth (), artwork.getHeight ());
     polygonSelection.makeSelectionFromPolygon (polygon, transform);
     if (controller.getCurSelectionAction () == CPCommonController.selectionAction.SELECT)
       {
         artwork.DoSelection (modifiersToSelectionApplianceType (getModifiers ()), polygonSelection);
       }
     else
       {
         artwork.DoSelection (SelectionTypeOfAppliance.CREATE, polygonSelection);
         if (!artwork.getCurSelection ().isEmpty ())
           artwork.doEffectAction (false, new CPTransparentFillEffect (controller.getCurColorRgb () | controller.getSelectionFillAlpha () << 24));
         artwork.deselectAll ();
       }
     artwork.finalizeUndo ();
     setActiveMode (defaultMode); // yield control to the default mode
     repaint ();
   }
 
   @Override
   public void paint (Graphics2D g2d)
   {
     g2d.setXORMode (Color.WHITE);
     g2d.draw (polygon);
   }
 
 }
 
 
 //
 // CPMoveTool mode
 //
 
 //
 // Canvas rotate mode
 //
 
 class CPRotateCanvasMode extends CPMode
 {
 
   Point firstClick;
   float initAngle;
   AffineTransform initTransform;
   boolean dragged;
 
   @Override
   public void cursorPressAction ()
   {
     Point p = new Point (getCursorX (), getCursorY ());
     firstClick = (Point) p.clone ();
 
     initAngle = getRotation ();
     initTransform = new AffineTransform (transform);
 
     dragged = false;
 
     repaintBrushPreview ();
   }
 
   @Override
   public void cursorDragAction ()
   {
     dragged = true;
 
     Point p = new Point (getCursorX (), getCursorY ());
     Dimension d = getSize ();
     Point2D.Float center = new Point2D.Float (d.width / 2.f, d.height / 2.f);
 
     float deltaAngle = (float) Math.atan2 (p.y - center.y, p.x - center.x)
             - (float) Math.atan2 (firstClick.y - center.y, firstClick.x - center.x);
 
     AffineTransform rotTrans = new AffineTransform ();
     rotTrans.rotate (deltaAngle, center.x, center.y);
 
     rotTrans.concatenate (initTransform);
 
     setRotation (initAngle + deltaAngle);
     setOffset ((int) rotTrans.getTranslateX (), (int) rotTrans.getTranslateY ());
   }
 
   @Override
   public void cursorReleaseAction ()
   {
     if (!dragged)
       {
         resetRotation ();
       }
 
     setActiveMode (prevMode); // yield control to the previous mode
     prevMode = null;
   }
 }
 
 	/*
    * // // mode //
 	 *
 	 * class CPMode extends CPMode { public void cursorPressed(MouseEvent e) {} public void mouseDragged(MouseEvent e) {}
 	 * public void cursorReleased(MouseEvent e) {} }
 	 */
 }
 
