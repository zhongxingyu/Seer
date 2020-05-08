 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.extensions.timeasy;
 
 import Sirius.navigator.connection.SessionManager;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.permission.Permission;
 import Sirius.server.newuser.permission.PermissionHolder;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 
 import edu.umd.cs.piccolo.event.PInputEvent;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.geom.Point2D;
 
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 
 import de.cismet.cismap.commons.WorldToScreenTransform;
 import de.cismet.cismap.commons.features.InputEventAwareFeature;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.features.XStyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.tools.PFeatureTools;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class TimEasyPureNewFeature extends PureNewFeature implements InputEventAwareFeature {
 
     //~ Instance fields --------------------------------------------------------
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private String classId = java.util.ResourceBundle.getBundle("de/cismet/extensions/timeasy/Bundle")
                 .getString("classID");
     private String domainserver = java.util.ResourceBundle.getBundle("de/cismet/extensions/timeasy/Bundle")
                 .getString("domainserver");
     private int parentNodeId = new Integer(java.util.ResourceBundle.getBundle("de/cismet/extensions/timeasy/Bundle")
                     .getString("parentNodeId")).intValue();
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new TimEasyPureNewFeature object.
      *
      * @param  g  DOCUMENT ME!
      */
     public TimEasyPureNewFeature(final Geometry g) {
         super(g);
     }
     /**
      * Creates a new TimEasyPureNewFeature object.
      *
      * @param  point  DOCUMENT ME!
      * @param  wtst   DOCUMENT ME!
      */
     public TimEasyPureNewFeature(final Point2D point, final WorldToScreenTransform wtst) {
         super(point, wtst);
     }
     /**
      * Creates a new TimEasyPureNewFeature object.
      *
      * @param  canvasPoints  DOCUMENT ME!
      * @param  wtst          DOCUMENT ME!
      */
     public TimEasyPureNewFeature(final Point2D[] canvasPoints, final WorldToScreenTransform wtst) {
         super(canvasPoints, wtst);
     }
     /**
      * Creates a new TimEasyPureNewFeature object.
      *
      * @param  coordArr  DOCUMENT ME!
      * @param  wtst      DOCUMENT ME!
      */
     public TimEasyPureNewFeature(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
         super(coordArr, wtst);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public boolean noFurtherEventProcessing(final PInputEvent event) {
         return true;
     }
 
     @Override
     public void mouseWheelRotated(final PInputEvent event) {
     }
 
     @Override
     public void mouseReleased(final PInputEvent event) {
     }
 
     @Override
     public void mousePressed(final PInputEvent event) {
     }
 
     @Override
     public void mouseMoved(final PInputEvent event) {
     }
 
     @Override
     public void mouseExited(final PInputEvent event) {
     }
 
     @Override
     public void mouseEntered(final PInputEvent event) {
     }
 
     @Override
     public void mouseDragged(final PInputEvent event) {
     }
 
     @Override
     public void mouseClicked(final PInputEvent event) {
         try {
             event.setHandled(noFurtherEventProcessing(event));
             final MetaClass metaClass = SessionManager.getProxy().getMetaClass(classId + "@" + domainserver);
             final String userkey = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
             final Permission permission = PermissionHolder.WRITEPERMISSION;
             final boolean writePermission = metaClass.getPermissions().hasPermission(userkey, permission);
             // Node parentNode=SessionManager.getProxy().getNode(parentNodeId,domainserver);
 
             final boolean timLiegPermission = metaClass.getPermissions()
                        .hasWritePermission(SessionManager.getSession().getUser());
             final Object o = PFeatureTools.getFirstValidObjectUnderPointer(event, new Class[] { PFeature.class });
             if (event.getComponent() instanceof MappingComponent) {
                 final MappingComponent mc = (MappingComponent)event.getComponent();
 
                 if (event.getModifiers() == InputEvent.BUTTON3_MASK) { // &&writePermission) {
                     if (log.isDebugEnabled()) {
                         log.debug("right mouseclick");
                     }
                     if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof XStyledFeature)) {
                         final XStyledFeature xf = (XStyledFeature)((PFeature)o).getFeature();
                         if (log.isDebugEnabled()) {
                             log.debug("valid object under pointer");
                         }
                         final JPopupMenu popup = new JPopupMenu("Test");
                         final JMenuItem m = new JMenuItem("TIM Merker anlegen");
                         m.setIcon(xf.getIconImage());
                         m.addActionListener(new ActionListener() {
 
                                 @Override
                                 public void actionPerformed(final ActionEvent e) {
                                     if (log.isDebugEnabled()) {
                                         log.debug("TIM Action performed");
                                     }
                                     log.info("permission an TimLieg:" + timLiegPermission);
                                     if (true || timLiegPermission) {
                                         final TimEasyDialog ted = new TimEasyDialog(
                                                 StaticSwingTools.getParentFrame(mc),
                                                 true,
                                                 TimEasyPureNewFeature.this,
                                                 mc);
                                         ted.pack();
                                         ted.setLocationRelativeTo(mc);
                                         ted.setVisible(true);
                                     } else {
                                         JOptionPane.showMessageDialog(
                                             StaticSwingTools.getParentFrame(mc),
                                             java.util.ResourceBundle.getBundle("de/cismet/extensions/timeasy/Bundle")
                                                         .getString("keine_rechte_am_parentnode"),
                                             "TimEasy Fehler",
                                             JOptionPane.ERROR_MESSAGE);
                                     }
                                 }
                             });
                         popup.add(m);
                         popup.show(mc, (int)event.getCanvasPosition().getX(), (int)event.getCanvasPosition().getY());
                     }
                 }
             }
         } catch (Throwable t) {
             log.error("Fehler beim Aufruf des TIM Easy Dialog.", t);
         }
     }
 }
