 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.appbase.client.swing;
 
 import com.jme.math.Vector3f;
 import java.awt.Component;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import javax.swing.JComponent;
 import javax.swing.Popup;
 import com.sun.embeddedswing.EmbeddedToolkit;
 import com.sun.embeddedswing.EmbeddedPeer;
 import java.awt.Canvas;
 import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 
 /**
  * The main interface to Embedded Swing. This class provides access to the three basic capabilities
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
	MouseEvent newEvent = new MouseEvent(canvas, e.getID(), e.getWhen(), e.getModifiers(), 
					     e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), 
					     e.getButton());
	newEvent.translatePoint(canvasPoint.x - framePoint.x, canvasPoint.y - framePoint.y);
	logger.fine("pick event = " + newEvent);
 
	InputManager.PickEventReturn ret = InputManager.inputManager().pickMouseEventSwing(newEvent);
 	if (ret == null || ret.entity == null || ret.pickDetails == null) {
 	    logger.fine("WindowSwing miss");
 	    return null;
 	}
 	logger.fine("WindowSwing hit");
 	logger.fine("Pick hit entity = " + ret.entity);
 
 	EntityComponent comp = ret.entity.getComponent(WindowSwing.WindowSwingReference.class);
 	assert comp != null;
 	final WindowSwing windowSwing = ((WindowSwing.WindowSwingReference)comp).getWindowSwing();
 	assert windowSwing != null;
 
 	final Vector3f intersectionPointWorld = ret.pickDetails.getPosition();
 	logger.fine("intersectionPointWorld = " + intersectionPointWorld);
 
         final EmbeddedPeer targetEmbeddedPeer = windowSwing.getEmbeddedPeer();
         CoordinateHandler coordinateHandler = new CoordinateHandler() {
             public EmbeddedPeer getEmbeddedPeer() {
                 return targetEmbeddedPeer;
             }
 
             public Point2D transform(Point2D src, Point2D dst) {
 		Point pt = windowSwing.calcWorldPositionInPixelCoordinates(intersectionPointWorld);
 		logger.fine("pt = " + pt);
 
 		if (dst == null) {
 		    dst = new Point2D.Double();
 		}
 		
 		if (pt == null) {
 		    logger.warning("Event world point is outside window bounds");
 		    return dst;
 		}
 
 		// TODO: for now
 		dst.setLocation(new Point2D.Double((double)pt.x, (double)pt.y));
 		logger.fine("dst = " + dst);
 
 		return dst;
             }
         };
         return coordinateHandler;
     }
 
     @Override
     // Note: peer should be the owning WindowSwing.embeddedPeer	
     public Popup getPopup(EmbeddedPeer peer, Component contents, int x, int y) {
 	System.err.println("getPopup: xy = " + x + ", " + y);
 
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
 
 	private static PainterThread painterThread;
 
 	WindowSwingEmbeddedToolkit toolkit;
 
         private WindowSwing windowSwing = null;
 
         protected WindowSwingEmbeddedPeer(JComponent parent, Component embedded, WindowSwingEmbeddedToolkit toolkit) {
             super(parent, embedded);
 	    this.toolkit = toolkit;
 
 	    painterThread = new PainterThread();
 	    painterThread.start();
         }
 
 	void repaint () {
 	    // TODO: for now
 	    repaint(0, 0, 0, 0);
 	}
 
         @Override
 	public void repaint(int x, int y, int width, int height) {
 
             if (windowSwing == null) {
                 return;
             }
 
 	    paintOnWindow(windowSwing);
 
   	    /* TODO: if I do this it needs to be in the painter thread
             Component embedded = getEmbeddedComponent();
             int compX0 = embedded.getX();
             int compY0 = embedded.getY();
             int compX1 = compX0 + embedded.getWidth();
             int compY1 = compY0 + embedded.getHeight();
             int x0 = Math.max(x, compX0);
             int y0 = Math.max(y, compY0);
             int x1 = Math.min(x + width, compX1);
             int y1 = Math.min(y + height, compY1);
             
             if (x0 == compX0 && y0 == compY0
                     && x1 == compX1 && y1 == compY1) {
 		// TODO: windowSwing
                 sgComponent.repaint(false);
             } else if (x1 > x0 && y1 > y0){
 		// TODO: windowSwing
                 sgComponent.repaint(
                         new Rectangle2D.Float(x0, y0, x1 - x0, y1 - y0));
             }
 	    */
         }
 
         void setWindowSwing(WindowSwing windowSwing) {
             this.windowSwing = windowSwing;
         }
 
         WindowSwing getWindowSwing () {
             return windowSwing;
         }
 
 	protected EmbeddedToolkit<?> getEmbeddedToolkit () {
 	    return toolkit;
 	}
     
         @Override
         protected void sizeChanged(Dimension oldSize, Dimension newSize) {
 	    /* TODO
             if (getSGComponent() != null) {
                 getSGComponent().repaint(true);
             }
 	    */
         }
 
 	private void paintOnWindow (WindowSwing window) {
 	    painterThread.enqueuePaint(this, window);
 	}
 
 	private static class PainterThread extends Thread {
 
 	    private static final Logger logger = Logger.getLogger(PainterThread.class.getName());
 
 	    private static class PaintRequest {
 		private WindowSwingEmbeddedPeer embeddedPeer;
 		private WindowSwing window;
 		private PaintRequest (WindowSwingEmbeddedPeer embeddedPeer, WindowSwing window) {
 		    this.embeddedPeer = embeddedPeer;
 		    this.window = window;
 		}
 	    }
 
 	    private LinkedBlockingQueue<PaintRequest> queue = new LinkedBlockingQueue<PaintRequest>();
 
 	    private PainterThread () {
 		super("WindowSwingEmbeddedPeer-PainterThread");
 	    }
 
 	    private void enqueuePaint (WindowSwingEmbeddedPeer embeddedPeer, WindowSwing window) {
 		queue.add(new PaintRequest(embeddedPeer, window));
 	    }
 
 	    public void run () {
 		while (true) {
 		    try {
 			PaintRequest request = null;
 			request = queue.take();
 			DrawingSurface drawingSurface = request.window.getSurface();
 			Graphics2D gDst = drawingSurface.getGraphics();
 			request.embeddedPeer.paint(gDst);
 		    } catch (Exception ex) {
 			ex.printStackTrace();
 			logger.warning("Exception caught in WindowSwingEmbeddedToolkit Painter Thread.");
 		    }
 		}
 	    }
 	}
     }
 }
