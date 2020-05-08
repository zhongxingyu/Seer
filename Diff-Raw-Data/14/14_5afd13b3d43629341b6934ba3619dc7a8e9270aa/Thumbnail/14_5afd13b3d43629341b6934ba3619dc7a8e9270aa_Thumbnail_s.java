 /*
 
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 package app.gui.svgComponents.thumbnail;
 
 import app.gui.detailspanel.AlphaJPanel;
 import app.gui.svgComponents.Canvas;
 import app.gui.svgComponents.PaintingTransformIterface;
 import app.utils.GraphicsUtilities;
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Dimension2D;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.event.MouseInputAdapter;
 import org.apache.batik.bridge.ViewBox;
 import org.apache.batik.gvt.CanvasGraphicsNode;
 import org.apache.batik.gvt.CompositeGraphicsNode;
 import org.apache.batik.gvt.GraphicsNode;
 import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
 import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
 import org.apache.batik.swing.gvt.JGVTComponent;
 import org.apache.batik.swing.gvt.Overlay;
 import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
 import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
 import org.apache.batik.util.SVGConstants;
 import org.w3c.dom.svg.SVGDocument;
 import org.w3c.dom.svg.SVGSVGElement;
 
 /**
  * This class represents a alpha panel that displays a Thumbnail of the current SVG
  * document.
  *
  * @author <a href="mailto:tkormann@apache.org">Thierry Kormann</a>
  * @author <a href="mailto:war29@wp.pl">"modyfi" Grzegorz Warywoda 2009-02-15 13:27
  * @version $Id: Thumbnail.java 592619 2007-11-07 05:47:24Z cam $
  */
 public class Thumbnail extends AlphaJPanel {
   
     /**
      *
      */
     protected Canvas svgCanvas;
     /**
      *
      */
     protected JGVTComponent svgThumbnailCanvas;
     /**
      *
      */
     protected boolean documentChanged;
     /**
      *
      */
     protected AreaOfInterestOverlay overlay;
     /**
      *
      */
     protected AreaOfInterestListener aoiListener;
     /**
      *
      */
     protected boolean interactionEnabled = false;
 
     /**
      *
      * @param svgCanvas
      */
     public Thumbnail(Canvas svgCanvas) {
 
         super(new BorderLayout());
         setOpaque(false);
         this.svgCanvas = svgCanvas;
         svgCanvas.addGVTTreeRendererListener(new ThumbnailGVTListener());
         svgCanvas.addSVGDocumentLoaderListener(new ThumbnailDocumentListener());
         svgCanvas.addComponentListener(new ThumbnailCanvasComponentListener());
         
         //when svg document state is ALWAYS_DINAMIC then Thumbnail not perform
         //at pan iterator (;f does not work as I wanted)
         //svgCanvas.addPaintingTranformListener(new PaintingTransformListener());
 
         // create the thumbnail
         svgThumbnailCanvas = new SVGThumbnailCanvas();
 
         overlay = new AreaOfInterestOverlay();
         svgThumbnailCanvas.getOverlays().add(overlay);
         //svgThumbnailCanvas.addComponentListener(new ThumbnailComponentListener());
         //svgThumbnailCanvas.addGVTTreeRendererListener(new ThumbnailGVTListener());
         aoiListener = new AreaOfInterestListener();
 
         add(svgThumbnailCanvas, BorderLayout.CENTER);       
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
     }
 
     /**
      *
      * @param b
      */
     public void setInteractionEnabled(boolean b) {
 
         if (b == interactionEnabled) return;
         interactionEnabled = b;
         if (b) {
             svgThumbnailCanvas.addMouseListener      (aoiListener);
             svgThumbnailCanvas.addMouseMotionListener(aoiListener);
         } else {
             svgThumbnailCanvas.removeMouseListener      (aoiListener);
             svgThumbnailCanvas.removeMouseMotionListener(aoiListener);
         }
     }
 
     /**
      *
      * @return
      */
     public boolean getInteractionEnabled() {
         return interactionEnabled;
     }
 
     /**
      * Updates the thumbnail component.
      */
     protected void updateThumbnailGraphicsNode() {
         svgThumbnailCanvas.setGraphicsNode(svgCanvas.getGraphicsNode());
         updateThumbnailRenderingTransform();
     }
 
     /**
      *
      * @param gn
      * @return
      */
     protected CanvasGraphicsNode getCanvasGraphicsNode(GraphicsNode gn) {
         if (!(gn instanceof CompositeGraphicsNode))
             return null;
         CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
         List children = cgn.getChildren();
         if (children.size() == 0)
             return null;
         gn = (GraphicsNode)cgn.getChildren().get(0);
         if (!(gn instanceof CanvasGraphicsNode))
             return null;
         return (CanvasGraphicsNode)gn;
     }
 
     /**
      * Updates the thumbnail component rendering transform.
      */
     public void updateThumbnailRenderingTransform() {
         //System.out.println("****update ThumbnailRenderingTransform");
         SVGDocument svgDocument = svgCanvas.getSVGDocument();
         if (svgDocument != null) {
             SVGSVGElement elt = svgDocument.getRootElement();
             Dimension dim = svgThumbnailCanvas.getSize();
             //System.out.println("Dim size "+dim);
             // XXX Update this to use the animated values of 'viewBox'
             //     and 'preserveAspectRatio'.
             String viewBox = elt.getAttributeNS
                 (null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
             //System.out.println("ViewBox SVG_VIEW_BOX_ATTRIBUTE ->"+viewBox);
             AffineTransform Tx;
             if (viewBox.length() != 0) {
                 String aspectRatio = elt.getAttributeNS
                     (null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
                 //System.out.println("aspectRatio SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE ->"+aspectRatio);
                 Tx = ViewBox.getPreserveAspectRatioTransform
                     (elt, viewBox, aspectRatio, dim.width, dim.height, null);
                 //System.out.println("Tx ViewBox.getPreserveAspectRatioTransform -> "+Tx);
             } else {
                 // no viewBox has been specified, create a scale transform
                 //System.out.println("no viewBox has been specified, create a scale transform");
                 Dimension2D docSize = svgCanvas.getSVGDocumentSize();
                 if(docSize == null){
                     //not build yet
                     return;
                 }
                 double sx = dim.width / docSize.getWidth();
                 double sy = dim.height / docSize.getHeight();
                 double s = Math.min(sx, sy);
                 Tx = AffineTransform.getScaleInstance(s, s);
                  //System.out.println("Tx -> "+Tx);
             }
 
             GraphicsNode gn = svgCanvas.getGraphicsNode();
             CanvasGraphicsNode cgn = getCanvasGraphicsNode(gn);
             if (cgn != null) {
                 AffineTransform vTx = cgn.getViewingTransform();
                 //System.out.println("ViewingTransform vtx "+vTx);
                 if ((vTx != null) && !vTx.isIdentity()) {
                     try {
                         AffineTransform invVTx = vTx.createInverse();
                         Tx.concatenate(invVTx);
                         //System.out.println("Tx.concatenate(invVTx) "+Tx);
                     } catch (NoninvertibleTransformException nite) {
                         /* nothing */
                     }
                 }
             }
 
             svgThumbnailCanvas.setRenderingTransform(Tx);
             overlay.synchronizeAreaOfInterest();
         }
     }
 
     class SVGThumbnailCanvas extends JGVTComponent{
 
         private BufferedImage bi;
 
         @Override
         public void paintComponent(Graphics g) {
             Graphics2D g2d = (Graphics2D)g;
 
             if(getAlpha()!=0){
                 if (image != null ) {
                    if(bi == null){
                        System.err.println("GraphicsUtilities.toCompatibleImage");
                         bi = GraphicsUtilities.toCompatibleImage(image);
                     }
                     if (paintingTransform != null) {
                         g2d.transform(paintingTransform);
                     }
                     g2d.drawRenderedImage(image, null);
                     g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                          RenderingHints.VALUE_ANTIALIAS_OFF);
                     Iterator it = overlays.iterator();
                     while (it.hasNext()) {
                         ((Overlay)it.next()).paint(g2d);
                     }
                 }else{
                     if(bi != null){
                         g2d.drawRenderedImage(bi, null);
                     }
                 }
             }
         }
 
         @Override
         public void setGraphicsNode(GraphicsNode gn) {
             bi = null;
             image = null;
             super.setGraphicsNode(gn);
         }
 
         @Override
         protected boolean updateRenderingTransform() {
             return super.updateRenderingTransform();
         }
     }
 
     /**
      * Used to determine whether or not the GVT tree of the thumbnail has to be
      * updated.
      */
     protected class ThumbnailDocumentListener extends SVGDocumentLoaderAdapter {
 
         /**
          *
          * @param e
          */
         @Override
         public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
             documentChanged = true;
         }
     }
 
     /**
      * Used to perform a translation using the area of interest.
      */
     protected class AreaOfInterestListener extends MouseInputAdapter {
 
         /**
          *
          */
         /**
          *
          */
         protected int sx, sy;
         /**
          *
          */
         protected boolean in;
 
         @Override
         public void mousePressed(MouseEvent evt) {
             sx = evt.getX();
             sy = evt.getY();
             in = overlay.contains(sx, sy);
            System.out.println("in "+in+" sx "+sx+" sy "+sy);
             overlay.setPaintingTransform(new AffineTransform());
         }
 
         @Override
         public void mouseDragged(MouseEvent evt) {
             if (in) {
                 int dx = evt.getX() - sx;
                 int dy = evt.getY() - sy;
                 overlay.setPaintingTransform
                     (AffineTransform.getTranslateInstance(dx, dy));
                 svgThumbnailCanvas.repaint();
             }
         }
 
         @Override
         public void mouseReleased(MouseEvent evt) {
             if (in) {
                 int dx = evt.getX() - sx;
                 int dy = evt.getY() - sy;
                 AffineTransform at = overlay.getOverlayTransform();
                 Point2D pt0 = new Point2D.Float(0, 0);
                 Point2D pt = new Point2D.Float(dx, dy);
 
                 try {
                     at.inverseTransform(pt0, pt0);
                     at.inverseTransform(pt, pt);
                     double tx = pt0.getX() - pt.getX();
                     double ty = pt0.getY() - pt.getY();
                     at = svgCanvas.getRenderingTransform();
                     at.preConcatenate
                         (AffineTransform.getTranslateInstance(tx, ty));
                     svgCanvas.setRenderingTransform(at);
                     updateThumbnailRenderingTransform();
                 } catch (NoninvertibleTransformException ex) { }
             }
         }
     }
 
     /**
      * Used to update the overlay and/or the GVT tree of the thumbnail.
      */
     protected class ThumbnailGVTListener extends GVTTreeRendererAdapter {
 
         /**
          *
          * @param e
          */
         @Override
         public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
             if (documentChanged) {
                 updateThumbnailGraphicsNode();
                 documentChanged = false;
             } else {
                 overlay.synchronizeAreaOfInterest();
                 svgThumbnailCanvas.repaint();
             }
         }
 
         /**
          *
          * @param e
          */
         @Override
         public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
             if (documentChanged) {
                 svgThumbnailCanvas.setGraphicsNode(null);
                 svgThumbnailCanvas.setRenderingTransform(new AffineTransform());
             }
         }
 
         /**
          *
          * @param e
          */
         @Override
         public void gvtRenderingFailed(GVTTreeRendererEvent e) {
             if (documentChanged) {
                 svgThumbnailCanvas.setGraphicsNode(null);
                 svgThumbnailCanvas.setRenderingTransform(new AffineTransform());
             }
         }
     }
 
     /**
      * Used to allow the SVG document being displayed by the thumbnail to be
      * resized properly.
      */
     protected class ThumbnailComponentListener extends ComponentAdapter {
 
         @Override
         public void componentResized(ComponentEvent e) {
             updateThumbnailRenderingTransform();
         }
     }
 
     /**
      * Used to allow the SVG document being displayed by the thumbnail to be
      * resized properly when parent resizes.
      */
     protected class ThumbnailCanvasComponentListener extends ComponentAdapter {
 
         @Override
         public void componentResized(ComponentEvent e) {
             updateThumbnailRenderingTransform();
         }
     }
 
     /*when svg document state is ALWAYS_DINAMIC then Thumbnail not perform
      *at pan iterator
      */
 
     /**
      *
      */
     protected class PaintingTransformListener implements PaintingTransformIterface{
 
         /**
          *
          * @param pt
          */
         @Override
         public void setPaintingTransform(AffineTransform pt) {
             updateThumbnailRenderingTransform();
         }
     }
     /**
      * An overlay that represents the current area of interest.
      */
     protected class AreaOfInterestOverlay implements Overlay {
 
         /**
          *
          */
        protected Shape s;
         /**
          *
          */
         protected AffineTransform at;
         /**
          *
          */
         protected AffineTransform paintingTransform = new AffineTransform();
 
         /**
          *
          * @param x
          * @param y
          * @return
          */
         public boolean contains(int x, int y) {
            System.out.println("s bounds "+s.getBounds());
             return (s != null) ? s.contains(x, y) : false;
         }
 
         /**
          *
          * @return
          */
         public AffineTransform getOverlayTransform() {
             return at;
         }
 
         /**
          *
          * @param rt
          */
         public void setPaintingTransform(AffineTransform rt) {
             this.paintingTransform = rt;
         }
 
         /**
          *
          * @return
          */
         public AffineTransform getPaintingTransform() {
             return paintingTransform;
         }
 
         /**
          *
          */
         public void synchronizeAreaOfInterest() {
             paintingTransform = new AffineTransform();
             Dimension dim = svgCanvas.getSize();
             s = new Rectangle2D.Float(0, 0, dim.width, dim.height);
             try {
                 at = svgCanvas.getRenderingTransform().createInverse();
                 at.preConcatenate(svgThumbnailCanvas.getRenderingTransform());
                 s = at.createTransformedShape(s);
             } catch (NoninvertibleTransformException ex) {
                 dim = svgThumbnailCanvas.getSize();
                 s = new Rectangle2D.Float(0, 0, dim.width, dim.height);
             }
         }
 
         @Override
         public void paint(Graphics g) {
             if (s != null) {
                 Graphics2D g2d = (Graphics2D)g;
                 g2d.transform(paintingTransform);
                 g2d.setColor(new Color(255, 255, 255, 128));
                 g2d.fill(s);
                 g2d.setColor(Color.black);
                 g2d.setStroke(new BasicStroke());
                 g2d.draw(s);
             }
         }
     }
 
     /**
      *
      * @return
      */
     public boolean isDisplay(){
         return getAlpha()!=0.0f;
     }
 }
