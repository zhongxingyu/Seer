 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.sudplan.threeD;
 
 import com.dfki.av.sudplan.camera.AnimatedCamera;
 import com.dfki.av.sudplan.camera.BoundingBox;
 import com.dfki.av.sudplan.camera.BoundingVolume;
 import com.dfki.av.sudplan.camera.Camera;
 import com.dfki.av.sudplan.camera.CameraListener;
 import com.dfki.av.sudplan.camera.Vector3D;
import com.dfki.av.sudplan.vis.LayerInfo;
 import com.dfki.av.sudplan.vis.VisualizationPanel;
import com.dfki.av.sudplan.vis.WMSHeightUtils;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
 
 import gov.nasa.worldwind.View;
 import gov.nasa.worldwind.avlist.AVKey;
 import gov.nasa.worldwind.geom.Position;
 import gov.nasa.worldwind.geom.Vec4;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.WeakListeners;
 import org.openide.util.lookup.ServiceProvider;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 
 import java.beans.PropertyChangeEvent;
 
 import java.net.URI;
 
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.tree.TreePath;
 
 import javax.vecmath.Vector3d;
 
 import de.cismet.cids.custom.sudplan.SMSUtils;
 import de.cismet.cids.custom.sudplan.cismap3d.CameraChangedEvent;
 import de.cismet.cids.custom.sudplan.cismap3d.CameraChangedListener;
 import de.cismet.cids.custom.sudplan.cismap3d.CameraChangedSupport;
 import de.cismet.cids.custom.sudplan.cismap3d.Canvas3D;
 import de.cismet.cids.custom.sudplan.cismap3d.DropTarget3D;
 
 import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.wms.capabilities.Layer;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 @ServiceProvider(service = Canvas3D.class)
 public final class Canvas3DImpl implements Canvas3D, DropTarget3D {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(Canvas3DImpl.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient VisualizationPanel visPanel;
 
     private final transient CameraChangedSupport ccs;
     private final transient CameraListener camL;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new Canvas3DImpl object.
      */
     public Canvas3DImpl() {
         ccs = new CameraChangedSupport();
         camL = new CameraChangeL();
 
         visPanel = Registry3D.getInstance().getVisPanel();
         visPanel.addCameraListener(WeakListeners.create(CameraListener.class, camL, visPanel));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void home() {
         final Geometry homeBBoxGeom = CismapBroker.getInstance()
                     .getMappingComponent()
                     .getMappingModel()
                     .getInitialBoundingBox()
                     .getGeometry(4326);
         setCameraDirection(new Vector3d(0, 0, 1));
         setBoundingBox(homeBBoxGeom);
     }
 
     @Override
     public void setCameraPosition(final Geometry geom) {
         final Point centroid = geom.getCentroid();
         final Camera cam = new AnimatedCamera(centroid.getY(), centroid.getX(), 100);
         visPanel.setCamera(cam);
     }
 
     @Override
     public Geometry getCameraPosition() {
         try {
             final Camera cam = visPanel.getCamera();
             final GeometryFactory gf = new GeometryFactory();
 
             return gf.createPoint(new Coordinate(cam.getLongitude(), cam.getLatitude()));
         } catch (final Exception e) {
             LOG.error("cannot fetch camera position", e); // NOI18N
 
             return null;
         }
     }
 
     @Override
     public void setCameraDirection(final Vector3d direction) {
         final Vector3D v3d = new Vector3D(direction.x, direction.y, direction.z);
         final Camera cam = visPanel.getCamera();
         final AnimatedCamera ac = new AnimatedCamera(cam.getLatitude(), cam.getLongitude(), cam.getAltitude(), v3d);
 
         visPanel.setCamera(ac);
     }
 
     @Override
     public void resetCamera() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Vector3d getCameraDirection() {
         try {
             final Vector3D v3D = visPanel.getCamera().getViewingDirection();
             final Vector3d camDir = new Vector3d(v3D.getX(), v3D.getY(), v3D.getZ());
 
             return camDir;
         } catch (final Exception e) {
             LOG.error("cannot fetch camera direction", e); // NOI18N
 
             return null;
         }
     }
 
     @Override
     public void setBoundingBox(final Geometry geom) {
         final Coordinate[] coords = SMSUtils.getLlAndUr(geom);
         final BoundingVolume bv = new BoundingBox(coords[0].y, coords[1].y, coords[0].x, coords[1].x);
 
         visPanel.setBoundingVolume(bv);
     }
 
     @Override
     public Geometry getBoundingBox() {
         final BoundingVolume bv = visPanel.getBoundingVolume();
 
         final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
         final Coordinate[] bbox = new Coordinate[5];
         bbox[0] = new Coordinate(bv.getMinLongitude(), bv.getMinLatitude());
         bbox[1] = new Coordinate(bv.getMinLongitude(), bv.getMaxLatitude());
         bbox[2] = new Coordinate(bv.getMaxLongitude(), bv.getMaxLatitude());
         bbox[3] = new Coordinate(bv.getMaxLongitude(), bv.getMinLatitude());
         bbox[4] = new Coordinate(bv.getMinLongitude(), bv.getMinLatitude());
         final LinearRing ring = new LinearRing(new CoordinateArraySequence(bbox), factory);
         final Geometry geometry = factory.createPolygon(ring, new LinearRing[0]);
 
         return geometry;
     }
 
     @Override
     public void setInteractionMode(final InteractionMode mode) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public InteractionMode getInteractionMode() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addCameraChangedListener(final CameraChangedListener ccl) {
         ccs.addCameraChangedListener(ccl);
     }
 
     @Override
     public void removeCameraChangedListener(final CameraChangedListener ccl) {
         ccs.removeCameraChangedListener(ccl);
     }
 
     @Override
     public JComponent getUI() {
         return visPanel;
     }
 
     @Override
     public void dragEnter(final DropTargetDragEvent dtde) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("drag enter: " + dtde);
         }
     }
 
     @Override
     public void dragOver(final DropTargetDragEvent dtde) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("drag over: " + dtde);
         }
     }
 
     @Override
     public void dropActionChanged(final DropTargetDragEvent dtde) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("drag action changed: " + dtde);
         }
     }
 
     @Override
     public void dragExit(final DropTargetEvent dte) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("drag exit: " + dte);
         }
     }
 
     @Override
     public void drop(final DropTargetDropEvent dtde) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("drop: " + dtde);
         }
 
         final DataFlavor TREEPATH_FLAVOR = new DataFlavor(
                 DataFlavor.javaJVMLocalObjectMimeType,
                 "SelectionAndCapabilities"); // NOI18N
 
         if (dtde.isDataFlavorSupported(TREEPATH_FLAVOR)) {
             try {
                 final Object o = dtde.getTransferable().getTransferData(TREEPATH_FLAVOR);
                 dtde.dropComplete(true);
 
                 if (o instanceof SelectionAndCapabilities) {
                     final SelectionAndCapabilities sac = (SelectionAndCapabilities)o;
                     final TreePath[] selectionPath = sac.getSelection();
 
                     if (selectionPath.length != 1) {
                         throw new UnsupportedOperationException("only single layers are currently supported"); // NOI18N
                     }
 
                     final Object lastpathComponent = selectionPath[0].getLastPathComponent();
 
                     final String layername;
                     if (lastpathComponent instanceof Layer) {
                         final Layer layer = (Layer)lastpathComponent;
                         layername = layer.getName();
                     } else {
                         throw new UnsupportedOperationException("only wms layers are currently supported"); // NOI18N
                     }
 
                    final List<LayerInfo> infos = WMSHeightUtils.getLayerInfos(new URI(sac.getUrl()));
                     for (final LayerInfo info : infos) {
                        if (layername.equals(info.getParameter().getStringValue(AVKey.LAYER_NAMES))) {
                            visPanel.addWMSHeightLayer(info.getWMSCapabilities(),
                                info.getLayerCapabilities(),
                                info.getParameter(),
                                 0,
                                 1);
                         }
                     }
                 }
             } catch (final Exception ex) {
                 final String message = "cannot create 3D wms layer"; // NOI18N
                 LOG.error(message, ex);
             }
         } else {
             dtde.rejectDrop();
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class CameraChangeL implements CameraListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void propertyChange(final PropertyChangeEvent evt) {
             final View newView = (View)evt.getNewValue();
             final Vec4 newUpVec = newView.getUpVector();
             final Position newPos = newView.getCurrentEyePosition();
             final Coordinate newCoord = new Coordinate(
                     newPos.longitude.degrees,
                     newPos.latitude.degrees,
                     newPos.elevation);
 
             final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
             final Geometry newGeom = new Point(new CoordinateArraySequence(new Coordinate[] { newCoord }), factory);
 
             final Vec4 newForwardVec = newView.getForwardVector();
 
             final Vector3d newV3d = new Vector3d(newForwardVec.x, newForwardVec.y, newForwardVec.z);
 
             final CameraChangedEvent cce = new CameraChangedEvent(
                     Canvas3DImpl.this,
                     null,
                     newGeom,
                     null,
                     newV3d);
 
             ccs.fireCameraChanged(cce);
         }
     }
 }
