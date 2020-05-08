 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.appbase.client.swing;
 
 import com.jme.math.Vector3f;
 import java.awt.Component;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import javax.swing.JComponent;
 import javax.swing.Popup;
 import com.sun.embeddedswing.EmbeddedToolkit;
 import com.sun.embeddedswing.EmbeddedPeer;
 import java.awt.Canvas;
 import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Point;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.modules.appbase.client.DrawingSurfaceBufferedImage;
 
 /**
  * The main interface to Embedded Swing. This singleton provides access to the three basic capabilities
  * of Embedded Swing.
  * <br><br>
  * 1. Component embedding for the purpose of drawing.
  * <br><br>
  * 2. Mouse event handling.
  * <br><br>
  * 3. Popup window creation.
  */
 
 class WindowSwingEmbeddedToolkit 
     extends EmbeddedToolkit<WindowSwingEmbeddedToolkit.WindowSwingEmbeddedPeer> 
 {
     private static final Logger logger = Logger.getLogger(WindowSwingEmbeddedToolkit.class.getName());
 
     private static final WindowSwingEmbeddedToolkit embeddedToolkit = new WindowSwingEmbeddedToolkit();
 
     private Point lastPressPointScreen;
 
     public static WindowSwingEmbeddedToolkit getWindowSwingEmbeddedToolkit() {
         return embeddedToolkit;
     }
     
     @Override
     protected WindowSwingEmbeddedPeer createEmbeddedPeer(JComponent parent, Component embedded, Object... args) {
         return new WindowSwingEmbeddedPeer(parent, embedded, this);
     }
     
     @Override
     protected CoordinateHandler createCoordinateHandler(JComponent parent, Point2D point, MouseEvent e) {
 	logger.fine("Enter WSET.createCoordinateHandler, mouseEvent = " + e);
 	
 	// Convert event from frame coords into canvas coords
 	Canvas canvas = JmeClientMain.getFrame().getCanvas();
 	JFrame frame = (JFrame) e.getSource();
 	Point framePoint = e.getPoint();
 	Point canvasPoint = SwingUtilities.convertPoint(frame, framePoint, canvas);
 	e.translatePoint(canvasPoint.x - framePoint.x, canvasPoint.y - framePoint.y);
 
 	// TODO: someday: I don't think we need to do this anymore for drag events. But it doesn't hurt.
 	InputManager.PickEventReturn ret = InputManager.inputManager().pickMouseEventSwing(e);
 	if (ret == null || ret.entity == null || ret.destPickDetails == null) {
 	    logger.fine("WindowSwing miss");
 	    e.translatePoint(-(canvasPoint.x - framePoint.x), -(canvasPoint.y - framePoint.y));
 	    return null;
 	}
 	logger.fine("WindowSwing hit");
 	logger.fine("Pick hit entity = " + ret.entity);
 
 	EntityComponent comp = ret.entity.getComponent(WindowSwing.WindowSwingReference.class);
 	assert comp != null;
 	final WindowSwing windowSwing = ((WindowSwing.WindowSwingReference)comp).getWindowSwing();
 	assert windowSwing != null;
 
 	// TODO: someday: I don't think we need to set this anymore for drag events. But it doesn't hurt.
 	final Vector3f intersectionPointWorld = ret.destPickDetails.getPosition();
 	logger.fine("intersectionPointWorld = " + intersectionPointWorld);
 
 	if (e.getID() == MouseEvent.MOUSE_PRESSED) {
 	    lastPressPointScreen = new Point(e.getX(), e.getY());
 	}
 
         final EmbeddedPeer targetEmbeddedPeer = windowSwing.getEmbeddedPeer();
         CoordinateHandler coordinateHandler = new CoordinateHandler() {
             		
             public EmbeddedPeer getEmbeddedPeer() {
                 return targetEmbeddedPeer;
             }
 
 	    public Point2D transform(Point2D src, Point2D dst, MouseEvent event) {
 
 		logger.fine("src = " + src);
 		logger.fine("event = " + event);
 
 		Point pt = windowSwing.calcWorldPositionInPixelCoordinates(src, event, 
 			       intersectionPointWorld, lastPressPointScreen);
 
 		if (dst == null) {
 		    dst = new Point2D.Double();
 		}
 
 		// TODO: for now
 		dst.setLocation(new Point2D.Double((double)pt.x, (double)pt.y));
 		logger.fine("dst = " + dst);
 
 		return dst;
             }
         };
 	e.translatePoint(-(canvasPoint.x - framePoint.x), -(canvasPoint.y - framePoint.y));
         return coordinateHandler;
     }
 
     @Override
     // Note: peer should be the owning WindowSwing.embeddedPeer	
     public Popup getPopup(EmbeddedPeer peer, Component contents, int x, int y) {
 
 	int width = (int) contents.getPreferredSize().getWidth();
 	int height = (int) contents.getPreferredSize().getHeight();
 
 	if (!(peer instanceof WindowSwingEmbeddedPeer)) {
 	    throw new RuntimeException("Invalid embedded peer type");
 	}
 	WindowSwing winOwner = ((WindowSwingEmbeddedPeer)peer).getWindowSwing();
 	WindowSwing winPopup = null;
 	try {
 	    winPopup = new WindowSwing(winOwner.getApp(), width, height, false, winOwner.getPixelScale());
 	    winPopup.setComponent(contents);
 	} catch (InstantiationException ex) {
 	    logger.warning("Cannot create a WindowSwing popup");
 	    return null;
 	}
 	final WindowSwing popup = winPopup;
 
 	winPopup.positionRelativeTo(winOwner, x, y);
 	
         return new Popup() {
             @Override
             public void show() {
 		popup.setVisible(true);
             } 
             @Override
             public void hide() {
 		popup.setVisible(false);
             }
         };
 
 	// TODO: for now
 	//return null;
 
 	/* Old: sort of worked, but not very well
 	//final WindowSwingPopup wsp =  new WindowSwingPopup(peer, x, y, contents.getWidth(), contents.getHeight());
 	final WindowSwingPopup wsp =  new WindowSwingPopup(peer, x, y, 50, 50);
 	wsp.setComponent(contents);
 	*/
 
 	/* Old: Original Igor code
 
 This walks the up SG scene graph adding in the parent-relative offsets of each ancestor embedded peer
 and creates a transform group to position the popup at a (x,y) offset relative to the owning embedded peer
 Note that I don't support nested embedded components - an embedded swing component is always top-level.
 So I don't need to walk up any tree. I just need to calc the transform offset of the popup from the center
 of the cell.
 
         WindowSwingEmbeddedPeer embeddedPeer = (WindowSwingEmbeddedPeer) peer;
         SGGroup topSGGroup = null;
         AffineTransform accTransform = new AffineTransform();
         Point offset = new Point(x, y);
         //find topmost embeddedPeer and accumulated transform
         while (embeddedPeer != null) {
             accTransform.preConcatenate(AffineTransform.getTranslateInstance(
                     offset.getX(), offset.getY()));
             SGLeaf leaf = embeddedPeer.getSGComponent();
             accTransform.preConcatenate(
                     leaf.getCumulativeTransform());
             JSGPanel jsgPanel = leaf.getPanel();
             System.out.println(jsgPanel);
             topSGGroup = jsgPanel.getSceneGroup();
             //check if it is an embedded JSGPanel
             WindowSwingEmbeddedPeer parentPeer = WindowSwingEmbeddedToolkit.getWindowSwingEmbeddedToolkit().getEmbeddedPeer(jsgPanel);
             if (parentPeer != null) {
                 offset = SwingUtilities.convertPoint(
                         embeddedPeer.getEmbeddedComponent(),
                         0, 0,
                         parentPeer.getEmbeddedComponent());
             }
             embeddedPeer = parentPeer;
         }
         final SGComponent sgComponent = new SGComponent();
         sgComponent.setComponent(contents);
         final SGTransform.Affine sgTransform = 
             SGTransform.createAffine(accTransform, sgComponent);
         sgTransform.setVisible(false);
         topSGGroup.add(sgTransform);
 
         return new Popup() {
             @Override
             public void show() {
 		wsp.setShowing(true);
             } 
             @Override
             public void hide() {
 		wsp.setShowing(false);
             }
         };
 	*/
     }
     
     static class WindowSwingEmbeddedPeer extends EmbeddedPeer {
 
 	WindowSwingEmbeddedToolkit toolkit;
 
         private WindowSwing windowSwing = null;
 
         protected WindowSwingEmbeddedPeer(JComponent parent, Component embedded, WindowSwingEmbeddedToolkit toolkit) {
             super(parent, embedded);
 	    this.toolkit = toolkit;
         }
 
 	void repaint () {
             Component embedded = getEmbeddedComponent();
 	    repaint(embedded.getX(), embedded.getY(), embedded.getWidth(), embedded.getHeight());
 	}
 
         @Override
 	public void repaint(int x, int y, int width, int height) {
             if (windowSwing == null) {
                 return;
             }
 
 	    //System.err.println("repaint xywh = " + x + ", " + y + ", " + width + ", " + height);
 
 	    // Clip the dirty region to the component
             Component embedded = getEmbeddedComponent();
             int compX0 = embedded.getX();
             int compY0 = embedded.getY();
             int compX1 = compX0 + embedded.getWidth();
             int compY1 = compY0 + embedded.getHeight();
             int x0 = Math.max(x, compX0);
             int y0 = Math.max(y, compY0);
             int x1 = Math.min(x + width, compX1);
             int y1 = Math.min(y + height, compY1);
 	    x = x0;
 	    y = y0;
 	    width = x1 - x0;
 	    height = y1 - y0;
 
 	    paintOnWindow(windowSwing, x, y, width, height);
         }
 
         void setWindowSwing(WindowSwing windowSwing) {
             this.windowSwing = windowSwing;
             synchronized (this) {
                 notifyAll();
             }
         }
 
         WindowSwing getWindowSwing () {
             return windowSwing;
         }
 
 	protected EmbeddedToolkit<?> getEmbeddedToolkit () {
 	    return toolkit;
 	}
     
         @Override
         protected void sizeChanged(Dimension oldSize, Dimension newSize) {
 	    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> Size changed");
 	    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> oldSize = " + oldSize);
 	    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> newSize = " + newSize);
 
             synchronized (this) {
                 while (windowSwing == null) {
                     try { wait(); } catch (InterruptedException ex) {}
                 }
             }
 
            windowSwing.setSize(newSize.width, newSize.height);
         }
 
 	private void paintOnWindow (final WindowSwing window,
 				    final int x, final int y, final int width, final int height) {
 
 	    final EmbeddedPeer embeddedPeer = this;
 
 	    EventQueue.invokeLater(new Runnable () {
 		public void run () {
 		    DrawingSurface drawingSurface = window.getSurface();
 		    final DrawingSurfaceBufferedImage.DirtyTrackingGraphics gDst = 
 			(DrawingSurfaceBufferedImage.DirtyTrackingGraphics) drawingSurface.getGraphics();
 		    gDst.setClip(x, y, width, height);
 		    gDst.executeAtomic(new Runnable () {
 			public void run () {
 			    embeddedPeer.paint(gDst);
 			    gDst.addDirtyRectangle(x, y, width, height);
 			}
 		    });
 		}
 	    });
 	}
     }
 }
