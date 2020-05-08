 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.sudplan.threeD;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 
 import com.dfki.av.sudplan.camera.AnimatedCamera;
 import com.dfki.av.sudplan.camera.BoundingBox;
 import com.dfki.av.sudplan.camera.BoundingVolume;
 import com.dfki.av.sudplan.camera.Camera;
 import com.dfki.av.sudplan.camera.CameraListener;
 import com.dfki.av.sudplan.camera.Vector3D;
 import com.dfki.av.sudplan.vis.VisualizationPanel;
 import com.dfki.av.sudplan.vis.basic.VisBuildings;
 import com.dfki.av.sudplan.vis.core.ColorParameter;
 import com.dfki.av.sudplan.vis.core.ITransferFunction;
 import com.dfki.av.sudplan.vis.core.IVisAlgorithm;
 import com.dfki.av.sudplan.vis.core.IVisParameter;
 import com.dfki.av.sudplan.vis.core.NumberParameter;
 import com.dfki.av.sudplan.vis.core.VisConfiguration;
 import com.dfki.av.sudplan.vis.core.VisWorker;
 import com.dfki.av.sudplan.vis.functions.ConstantColor;
 import com.dfki.av.sudplan.vis.geocpm.VisGeoCPM;
 import com.dfki.av.sudplan.vis.spi.TransferFunctionFactory;
 import com.dfki.av.sudplan.vis.spi.VisAlgorithmFactory;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
 
 import gov.nasa.worldwind.View;
 import gov.nasa.worldwind.geom.Position;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.NbBundle;
 import org.openide.util.WeakListeners;
 import org.openide.util.lookup.ServiceProvider;
 
 import java.awt.Color;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 
 import java.beans.PropertyChangeEvent;
 
 import java.io.File;
 
 import java.net.URI;
 
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.tree.TreePath;
 
 import javax.vecmath.Vector3d;
 
 import de.cismet.cids.custom.sudplan.Manager;
 import de.cismet.cids.custom.sudplan.ManagerType;
 import de.cismet.cids.custom.sudplan.SMSUtils;
 import de.cismet.cids.custom.sudplan.cismap3d.CameraChangedEvent;
 import de.cismet.cids.custom.sudplan.cismap3d.CameraChangedListener;
 import de.cismet.cids.custom.sudplan.cismap3d.CameraChangedSupport;
 import de.cismet.cids.custom.sudplan.cismap3d.Canvas3D;
 import de.cismet.cids.custom.sudplan.cismap3d.DropTarget3D;
 import de.cismet.cids.custom.sudplan.geocpmrest.io.SimulationResult;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.util.DnDUtils;
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
         throw new UnsupportedOperationException("Not supported yet."); // NOI18N //NOI18N
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
         final Coordinate[] coords = SMSUtils.getLlAndUr(CrsTransformer.transformToGivenCrs(geom, "EPSG:4326")); // NOI18N
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
         throw new UnsupportedOperationException("Not supported yet."); // NOI18N
     }
 
     @Override
     public InteractionMode getInteractionMode() {
         throw new UnsupportedOperationException("Not supported yet."); // NOI18N
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
     }
 
     @Override
     public void dragOver(final DropTargetDragEvent dtde) {
     }
 
     @Override
     public void dropActionChanged(final DropTargetDragEvent dtde) {
     }
 
     @Override
     public void dragExit(final DropTargetEvent dte) {
     }
 
     @Override
     public void drop(final DropTargetDropEvent dtde) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("drop: " + dtde); // NOI18N
         }
 
         final DataFlavor treepathFlavor = new DataFlavor(
                 DataFlavor.javaJVMLocalObjectMimeType,
                 "SelectionAndCapabilities");                                                           // NOI18N
         final DataFlavor nodeFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" // NOI18N
                         + DefaultMetaTreeNode.class.getName(),
                 "a DefaultMetaTreeNode");                                                              // NOI18N
 
         try {
             if (dtde.isDataFlavorSupported(treepathFlavor)) {
                 dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                 final Object o = dtde.getTransferable().getTransferData(treepathFlavor);
 
                 if (o instanceof SelectionAndCapabilities) {
                     final SelectionAndCapabilities sac = (SelectionAndCapabilities)o;
                     final TreePath[] selectionPath = sac.getSelection();
 
                     if (selectionPath.length != 1) {
                         throw new UnsupportedOperationException("only single layers are currently supported"); // NOI18N
                     }
 
                     final Object lastpathComponent = selectionPath[0].getLastPathComponent();
 
                     final String layername;
                     final String layerTitle;
                     if (lastpathComponent instanceof Layer) {
                         final Layer layer = (Layer)lastpathComponent;
                         layername = layer.getName();
 
                         if (layer.getTitle() == null) {
                             layerTitle = layername;
                         } else {
                             layerTitle = layer.getTitle();
                         }
                     } else {
                         throw new UnsupportedOperationException("only wms layers are currently supported"); // NOI18N
                     }
 
                     final LayerAdder layerAdder = new LayerAdder(new URI(sac.getUrl()), layername, layerTitle);
                     Registry3D.getInstance().get3DExecutor().execute(layerAdder);
                     dtde.dropComplete(true);
                 } else {
                     dtde.dropComplete(false);
                 }
             } else if (dtde.isDataFlavorSupported(nodeFlavor)) {
                 dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                 final DefaultMetaTreeNode dmtn = (DefaultMetaTreeNode)dtde.getTransferable()
                             .getTransferData(nodeFlavor);
 
                 if (dmtn instanceof ObjectTreeNode) {
                     final ObjectTreeNode otn = (ObjectTreeNode)dmtn;
                     final MetaObject mo = otn.getMetaObject(true);
 
                     final IVisAlgorithm visAlgo;
                     final String[] dataAttributes;
                     final URI dataSource;
 
                     if ("MODELOUTPUT".equals(mo.getMetaClass().getTableName())) {     // NOI18N
                         final CidsBean moBean = mo.getBean();
                         final String name = (String)moBean.getProperty("model.name"); // NOI18N
                         if ((name != null) && name.startsWith("Wuppertal")) {         // NOI18N
                             final AddGeoCPMDialogPanel geocpmPanel = new AddGeoCPMDialogPanel();
                             final int answer = JOptionPane.showConfirmDialog(
                                     ComponentRegistry.getRegistry().getMainWindow(),
                                     geocpmPanel,
                                     NbBundle.getMessage(
                                         Canvas3DImpl.class,
                                         "Canvas3DImpl.drop(DropTargetDropEvent).addGeoCPMDialog.title"),
                                     JOptionPane.OK_CANCEL_OPTION,
                                     JOptionPane.QUESTION_MESSAGE);
 
                             if (JOptionPane.OK_OPTION == answer) {
                                 if (geocpmPanel.isStatic()) {
                                     final Manager manager = SMSUtils.loadManagerFromModel((CidsBean)moBean.getProperty(
                                                 "model"), // NOI18N
                                             ManagerType.OUTPUT);
                                     manager.setCidsBean(moBean);
                                     final Object ur = manager.getUR();
                                     if (ur instanceof SimulationResult) {
                                         final SimulationResult sr = (SimulationResult)ur;
                                         String capUri = sr.getWmsGetCapabilitiesRequest();
                                         if ((capUri != null) && capUri.contains("sudplanwp6.cismet.de")
                                                     && capUri.contains("GetCapabilities")) {
                                             capUri =
                                                 "http://localhost/~mscholl/sudplanDist/geocpm_caps_SERVICE=WMS.xml";
                                         }
                                         final String layerName = sr.getLayerName();
 
                                         final LayerAdder layerAdder = new LayerAdder(new URI(capUri),
                                                 layerName,
                                                 layerName);
                                         Registry3D.getInstance().get3DExecutor().execute(layerAdder);
                                     }
                                 }
 
                                 if (geocpmPanel.isDynamic()) {
                                     final MetaClass mc = ClassCacheMultiple.getMetaClass(
                                             SMSUtils.DOMAIN_SUDPLAN_WUPP,
                                             "CISMAP3DCONTENT"); // NOI18N
                                     if (mc == null) {
                                         dtde.dropComplete(false);
 
                                         return;
                                     } else {
                                         final String query = "SELECT " + mc.getID() + "," + mc.getPrimaryKey() // NOI18N
                                                     + " FROM "                                                 // NOI18N
                                                     + mc.getTableName()
                                                     + " WHERE name LIKE 'GeoCPM 3D result " + mo.getID()       // NOI18N
                                                     + "'";                                                     // NOI18N
                                         final MetaObject[] mos = SessionManager.getProxy()
                                                     .getMetaObjectByQuery(SessionManager.getSession().getUser(),
                                                         query,
                                                         SMSUtils.DOMAIN_SUDPLAN_WUPP);
                                         if ((mos == null) || (mos.length != 1)) {
                                             dtde.dropComplete(false);
 
                                             return;
                                         } else {
                                             dataSource = new URI((String)
                                                     mos[0].getAttributeByFieldName("uri")         // NOI18N
                                                     .getValue());
                                         }
                                     }
                                 } else {
                                     dtde.dropComplete(geocpmPanel.isStatic());
 
                                     return;
                                 }
                             } else {
                                 dtde.dropComplete(false);
 
                                 return;
                             }
                         } else {
                             dtde.dropComplete(false);
 
                             return;
                         }
 
                         dataAttributes = new String[] { "<<NO_ATTRIBUTE>>" }; // NOI18N
                         visAlgo = VisAlgorithmFactory.newInstance(VisGeoCPM.class.getName());
 
                         final ITransferFunction tf = TransferFunctionFactory.newInstance(
                                 "com.dfki.av.sudplan.vis.geocpm.functions.GeoCPMTrafficLights");               // NOI18N
                         visAlgo.getVisParameters().get(0).setTransferFunction(tf);
                     } else if ("CISMAP3DCONTENT".equals(mo.getMetaClass().getTableName())                      // NOI18N
                                 && "buildings".equals(mo.getAttributeByFieldName("layername").getValue())) {   // NOI18N
                         dataSource = new URI((String)mo.getAttributeByFieldName("uri").getValue());            // NOI18N
                         dataAttributes = new String[] { "GEB_HOEHE", "<<NO_ATTRIBUTE>>", "<<NO_ATTRIBUTE>>" }; // NOI18N
                         visAlgo = VisAlgorithmFactory.newInstance(VisBuildings.class.getName());
                         for (final IVisParameter visParam : visAlgo.getVisParameters()) {
                             final ITransferFunction tf;
                             if (visParam instanceof NumberParameter) {
                                 tf = TransferFunctionFactory.newInstance(
                                         "com.dfki.av.sudplan.vis.functions.IdentityFunction");                 // NOI18N
                             } else if (visParam instanceof ColorParameter) {
                                 final ConstantColor cc = (ConstantColor)TransferFunctionFactory.newInstance(
                                         "com.dfki.av.sudplan.vis.functions.ConstantColor");                    // NOI18N
 
                                 if (visParam.getName().equalsIgnoreCase("color of roof")) { // NOI18N
                                     cc.setColor(Color.DARK_GRAY);
                                 } else {
                                     cc.setColor(Color.GRAY);
                                 }
 
                                 tf = cc;
                             } else {
                                 dtde.dropComplete(false);
 
                                 return;
                             }
                             visParam.setTransferFunction(tf);
                         }
                     } else {
                         dtde.dropComplete(false);
 
                         return;
                     }
 
                     dtde.dropComplete(true);
 
                     visAlgo.addPropertyChangeListener(visPanel);
                     final VisConfiguration visConfig = new VisConfiguration(visAlgo, dataSource, dataAttributes);
                     final VisWorker producer = new VisWorker(visConfig, visPanel.getWwd());
                     Registry3D.getInstance().get3DExecutor().execute(producer);
                 } else {
                     dtde.dropComplete(false);
                 }
             } else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                         || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                 dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
 
                 final URI uri;
                 if (dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                     // unix drop
                     final String uriList = (String)dtde.getTransferable().getTransferData(DnDUtils.URI_LIST_FLAVOR);
                     final String[] uris = uriList.split(System.getProperty("line.separator")); // NOI18N
                     if (uris.length == 1) {
                         uri = new URI(uris[0]);
                         dtde.dropComplete(true);
                     } else {
                         uri = null;
                         dtde.dropComplete(false);
                     }
                 } else {
                     // win drop
                     final List<File> data = (List)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                     if (data.size() == 1) {
                         uri = data.get(0).toURI();
                         dtde.dropComplete(true);
                     } else {
                         uri = null;
                         dtde.dropComplete(false);
                     }
                 }
 
                 if (uri != null) {
                     visPanel.runVisWiz(uri);
                 }
             } else {
                 dtde.rejectDrop();
             }
         } catch (final Exception ex) {
             LOG.error("could not process drop event: " + dtde, ex); // NOI18N
             dtde.rejectDrop();
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class LayerAdder implements Runnable {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient URI capUri;
         private final transient String layerName;
         private final transient String layerTitle;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new LayerAdder object.
          *
          * @param  capUri      DOCUMENT ME!
          * @param  layerName   DOCUMENT ME!
          * @param  layerTitle  DOCUMENT ME!
          */
         private LayerAdder(final URI capUri, final String layerName, final String layerTitle) {
             this.capUri = capUri;
             this.layerName = layerName;
             this.layerTitle = layerTitle;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             try {
                 final AddLayerDialogPanel aldp = new AddLayerDialogPanel(layerTitle);
                 final int answer = JOptionPane.showConfirmDialog(ComponentRegistry.getRegistry().getMainWindow(),
                         aldp,
                         NbBundle.getMessage(Canvas3DImpl.class, "Canvas3DImpl.LayerAdder.run().addLayerDialog.title"),
                         JOptionPane.OK_CANCEL_OPTION,
                         JOptionPane.QUESTION_MESSAGE);
                 if (JOptionPane.OK_OPTION == answer) {
                     if (aldp.isTextureLayer()) {
                         LOG.fatal("[uri=" + capUri + "|layerName=" + layerName + "|opacity=" + aldp.getOpacity() + "]");
                         visPanel.addWMSLayer(capUri, layerName, aldp.getOpacity());
                     } else {
                         visPanel.addWMSHeightLayer(capUri,
                             layerName,
                             aldp.getLayerHeight(),
                             aldp.getOpacity());
                     }
                 }
             } catch (final Exception ex) {
                 final String message = "cannot create 3D wms layer"; // NOI18N
                 LOG.error(message, ex);
             }
         }
     }
 
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
             final Position newPos = newView.getCurrentEyePosition();
             final Coordinate newCoord = new Coordinate(
                     newPos.longitude.degrees,
                     newPos.latitude.degrees,
                     newPos.elevation);
 
             final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
             final Geometry newGeom = new Point(new CoordinateArraySequence(new Coordinate[] { newCoord }), factory);
 
            // 0 is north, 90 / PI/2 is west, 180 / PI is south, -90 / -PI/2 is east
            final double radians = newView.getHeading().getRadians();
 
            final Vector3d newV3d = new Vector3d(-Math.sin(radians), Math.cos(radians), 0);
 
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
