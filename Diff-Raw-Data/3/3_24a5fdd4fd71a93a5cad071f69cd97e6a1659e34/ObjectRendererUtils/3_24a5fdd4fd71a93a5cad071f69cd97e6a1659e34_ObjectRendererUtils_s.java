 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.objectrenderer.utils;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.plugin.PluginRegistry;
 
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 import Sirius.server.newuser.UserGroup;
 import Sirius.server.newuser.permission.PermissionHolder;
 
 import org.jdesktop.swingx.error.ErrorInfo;
 import org.jdesktop.swingx.graphics.ShadowRenderer;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.font.TextAttribute;
 import java.awt.image.BufferedImage;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureGroups;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.documents.DefaultDocument;
 
 /**
  * DOCUMENT ME!
  *
  * @author   stefan
  * @version  $Revision$, $Date$
  */
 public class ObjectRendererUtils {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String ICON_RES_PACKAGE = "/de/cismet/cids/custom/wunda_blau/res/";
     public static final ImageIcon FORWARD_PRESSED;
     public static final ImageIcon FORWARD_SELECTED;
     public static final ImageIcon BACKWARD_PRESSED;
     public static final ImageIcon BACKWARD_SELECTED;
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectRendererUtils.class);
     private static final String CISMAP_PLUGIN_NAME = "cismap";
 
     static {
         BACKWARD_SELECTED = new ImageIcon(ObjectRendererUtils.class.getResource(
                     ICON_RES_PACKAGE
                             + "arrow-left-sel.png"));
         BACKWARD_PRESSED = new ImageIcon(ObjectRendererUtils.class.getResource(
                     ICON_RES_PACKAGE
                             + "arrow-left-pressed.png"));
         FORWARD_SELECTED = new ImageIcon(ObjectRendererUtils.class.getResource(
                     ICON_RES_PACKAGE
                             + "arrow-right-sel.png"));
         FORWARD_PRESSED = new ImageIcon(ObjectRendererUtils.class.getResource(
                     ICON_RES_PACKAGE
                             + "arrow-right-pressed.png"));
     }
 
     //~ Enums ------------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public enum DateDiff {
 
         //~ Enum constants -----------------------------------------------------
 
         MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public enum PermissionType {
 
         //~ Enum constants -----------------------------------------------------
 
         READ, WRITE, READ_WRITE
     }
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ObjectRendererUtils object.
      *
      * @throws  AssertionError  DOCUMENT ME!
      */
     private ObjectRendererUtils() {
         throw new AssertionError("so gehts aber nicht ;-)");
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  metaObjectList  DOCUMENT ME!
      * @param  clear           DOCUMENT ME!
      */
     public static void addBeanGeomsAsFeaturesToCismapMap(final Collection<MetaObject> metaObjectList,
             final boolean clear) {
         if (metaObjectList != null) {
             final MappingComponent bigMap = CismapBroker.getInstance().getMappingComponent();
             if (clear) {
                 bigMap.getFeatureCollection().removeAllFeatures();
             }
             final List<Feature> addedFeatures = TypeSafeCollections.newArrayList(metaObjectList.size());
             for (final MetaObject mo : metaObjectList) {
                 final CidsFeature newGeomFeature = new CidsFeature(mo);
                 addedFeatures.addAll(FeatureGroups.expandAll(newGeomFeature));
             }
             bigMap.getFeatureCollection().addFeatures(addedFeatures);
             bigMap.zoomToFeatureCollection();
 //            bigMap.zoomToAFeatureCollection(addedFeatures, false, false);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  comp  DOCUMENT ME!
      * @param  dim   DOCUMENT ME!
      */
     public static void setAllDimensions(final JComponent comp, final Dimension dim) {
         comp.setMaximumSize(dim);
         comp.setMinimumSize(dim);
         comp.setPreferredSize(dim);
     }
 
     /**
      * DOCUMENT ME!
      */
     public static void switchToCismapMap() {
         PluginRegistry.getRegistry()
                 .getPluginDescriptor(CISMAP_PLUGIN_NAME)
                 .getUIDescriptor(CISMAP_PLUGIN_NAME)
                 .getView()
                 .makeVisible();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bean   DOCUMENT ME!
      * @param  clear  DOCUMENT ME!
      */
     public static void addBeanGeomAsFeatureToCismapMap(final CidsBean bean, final boolean clear) {
         if (bean != null) {
             final MetaObject mo = bean.getMetaObject();
             final List<MetaObject> mos = TypeSafeCollections.newArrayList(1);
             mos.add(mo);
             addBeanGeomsAsFeaturesToCismapMap(mos, clear);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  box  DOCUMENT ME!
      */
     public static void selectAllTextInEditableCombobox(final JComboBox box) {
         final Component editor = box.getEditor().getEditorComponent();
         if (editor instanceof JTextField) {
             final JTextField textEditor = (JTextField)editor;
             textEditor.selectAll();
         } else {
             log.warn("Editor of Combobox " + box + " is not instanceof JTextField - can not select the text : "
                         + editor);
         }
     }
 
     /**
      * shows an exception window to the user if the parent component is currently shown.
      *
      * @param  titleMessage  DOCUMENT ME!
      * @param  ex            DOCUMENT ME!
      * @param  parent        DOCUMENT ME!
      */
     public static void showExceptionWindowToUser(final String titleMessage,
             final Exception ex,
             final Component parent) {
         if ((ex != null) && (parent != null) && parent.isShowing()) {
             final org.jdesktop.swingx.error.ErrorInfo ei = new ErrorInfo(
                     titleMessage,
                     ex.getMessage(),
                     null,
                     null,
                     ex,
                     Level.ALL,
                     null);
             org.jdesktop.swingx.JXErrorPane.showDialog(StaticSwingTools.getParentFrame(parent), ei);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   tabName  DOCUMENT ME!
      * @param   fields   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MetaObject[] getLightweightMetaObjectsForTable(final String tabName, final String[] fields) {
         return getLightweightMetaObjectsForTable(tabName, fields, null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   tabName    DOCUMENT ME!
      * @param   fields     DOCUMENT ME!
      * @param   formatter  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MetaObject[] getLightweightMetaObjectsForTable(final String tabName,
             final String[] fields,
             AbstractAttributeRepresentationFormater formatter) {
         if (formatter == null) {
             formatter = new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         final StringBuffer sb = new StringBuffer();
                         for (final String attribute : fields) {
                             sb.append(getAttribute(attribute.toLowerCase())).append(" ");
                         }
                         return sb.toString().trim();
                     }
                 };
         }
         try {
             final User user = SessionManager.getSession().getUser();
             final MetaClass mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, tabName);
             return SessionManager.getProxy().getAllLightweightMetaObjectsForClass(mc.getID(), user, fields, formatter);
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         return new MetaObject[0];
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   tabName    DOCUMENT ME!
      * @param   query      DOCUMENT ME!
      * @param   fields     DOCUMENT ME!
      * @param   formatter  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MetaObject[] getLightweightMetaObjectsForQuery(final String tabName,
             final String query,
             final String[] fields,
             AbstractAttributeRepresentationFormater formatter) {
        if (log.isDebugEnabled()) {
            log.debug("getLightweightMetaObjectsForQuery: " + query);
        }
         if (formatter == null) {
             formatter = new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         final StringBuffer sb = new StringBuffer();
                         for (final String attribute : fields) {
                             sb.append(getAttribute(attribute.toLowerCase())).append(" ");
                         }
                         return sb.toString().trim();
                     }
                 };
         }
         try {
             final User user = SessionManager.getSession().getUser();
             final MetaClass mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, tabName);
             if (mc != null) {
                 return SessionManager.getProxy()
                             .getLightweightMetaObjectsByQuery(mc.getID(), user, query, fields, formatter);
             } else {
                 log.error("Can not find MetaClass for Tablename: " + tabName);
             }
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         return new MetaObject[0];
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   in           DOCUMENT ME!
      * @param   shadowPixel  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static BufferedImage generateShadow(final Image in, final int shadowPixel) {
         if (in == null) {
             return null;
         }
         final BufferedImage input;
         if (in instanceof BufferedImage) {
             input = (BufferedImage)in;
         } else {
             final BufferedImage temp = new BufferedImage(in.getWidth(null),
                     in.getHeight(null),
                     BufferedImage.TYPE_4BYTE_ABGR);
             final Graphics tg = temp.createGraphics();
             tg.drawImage(in, 0, 0, null);
             tg.dispose();
             input = temp;
         }
         if (shadowPixel < 1) {
             return input;
         }
         final ShadowRenderer renderer = new ShadowRenderer(shadowPixel, 0.5f, Color.BLACK);
         final BufferedImage shadow = renderer.createShadow(input);
         final BufferedImage result = new BufferedImage(input.getWidth() + (2 * shadowPixel),
                 input.getHeight()
                         + (2 * shadowPixel),
                 BufferedImage.TYPE_4BYTE_ABGR);
         final Graphics2D rg = result.createGraphics();
         rg.drawImage(shadow, 0, 0, null);
         rg.drawImage(input, 0, 0, null);
         rg.dispose();
         return result;
     }
 
     /**
      * Starts a background thread with loads the picture from the url, resizes it to the given maximums, adds a
      * dropshadow of the given length and then sets the whole picture on a given JLabel.
      *
      * <p>Can be called from ANY thread, no matter if EDT or not!</p>
      *
      * @param  bildURL     DOCUMENT ME!
      * @param  maxPixelX   DOCUMENT ME!
      * @param  maxPixelY   DOCUMENT ME!
      * @param  shadowSize  DOCUMENT ME!
      * @param  toSet       DOCUMENT ME!
      */
     public static void loadPictureAndSet(final String bildURL,
             final int maxPixelX,
             final int maxPixelY,
             final int shadowSize,
             final JLabel toSet) {
         if ((bildURL != null) && (toSet != null)) {
             final Runnable loader = new Runnable() {
 
                     @Override
                     public void run() {
                         try {
                             final ImageIcon finBild = loadPicture(bildURL, maxPixelX, maxPixelY, shadowSize);
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         if (finBild != null) {
                                             toSet.setIcon(finBild);
                                         } else {
                                             toSet.setIcon(null);
 //                                        toSet.setVisible(false);
                                         }
                                     }
                                 });
                         } catch (Exception e) {
                             log.error("Exeption when loading picture " + bildURL + " : " + e, e);
                             toSet.setIcon(null);
                         }
                     }
                 };
             CismetThreadPool.execute(loader);
         }
     }
 
     /**
      * Starts a background thread with loads the picture from the url, resizes it to the given maximums, adds a
      * dropshadow of the given length and then sets the whole picture on a given JButton.
      *
      * <p>Can be called from ANY thread, no matter if EDT or not!</p>
      *
      * @param  bildURL     DOCUMENT ME!
      * @param  maxPixelX   DOCUMENT ME!
      * @param  maxPixelY   DOCUMENT ME!
      * @param  shadowSize  DOCUMENT ME!
      * @param  toSet       DOCUMENT ME!
      */
     public static void loadPictureAndSet(final String bildURL,
             final int maxPixelX,
             final int maxPixelY,
             final int shadowSize,
             final JButton toSet) {
         if ((bildURL != null) && (toSet != null)) {
             final Runnable loader = new Runnable() {
 
                     @Override
                     public void run() {
                         final ImageIcon finBild = loadPicture(bildURL, maxPixelX, maxPixelY, shadowSize);
                         if (finBild != null) {
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         if (finBild != null) {
                                             toSet.setIcon(finBild);
                                         } else {
                                             toSet.setVisible(false);
                                         }
                                     }
                                 });
                         }
                     }
                 };
             CismetThreadPool.execute(loader);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   tagToCheck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean checkActionTag(final String tagToCheck) {
         boolean result;
         try {
             result = SessionManager.getConnection().getConfigAttr(SessionManager.getSession().getUser(), tagToCheck)
                         != null;
         } catch (ConnectionException ex) {
             log.error("Can not check ActionTag!", ex);
             result = false;
         }
         return result;
     }
 
     /**
      * Starts a background thread with loads the picture from the url, resizes it to the given maximums, adds a
      * dropshadow of the given length.
      *
      * @param   bildURL     DOCUMENT ME!
      * @param   maxPixelX   DOCUMENT ME!
      * @param   maxPixelY   DOCUMENT ME!
      * @param   shadowSize  DOCUMENT ME!
      *
      * @return  ImageIcon with the loaded picture
      */
     public static ImageIcon loadPicture(final String bildURL,
             final int maxPixelX,
             final int maxPixelY,
             final int shadowSize) {
         ImageIcon bild = null;
         if ((bildURL != null) && (bildURL.length() > 0)) {
             final String urlString = bildURL.trim();
 
             Image buffImage = new DefaultDocument(urlString, urlString).getPreview(maxPixelX, maxPixelY);
             if (buffImage != null) {
                 // Static2DTools.getFasterScaledInstance(buffImage, width, height,
                 // RenderingHints.VALUE_INTERPOLATION_BICUBIC, true)
                 if (shadowSize > 0) {
                     buffImage = generateShadow(buffImage, shadowSize);
                 }
                 bild = new ImageIcon(buffImage);
                 return bild;
             }
         }
         return null;
     }
 
     /**
      * Adds a mouse listener to the given component, so that the cursor will change on mouse entered/exited.
      *
      * <p>Hint: Uses the awt.Cursor.XXX constants!</p>
      *
      * @param   toDecorate    DOCUMENT ME!
      * @param   mouseEntered  DOCUMENT ME!
      * @param   mouseExited   DOCUMENT ME!
      *
      * @return  the listener that was added
      */
     public static MouseListener decorateComponentWithMouseOverCursorChange(final JComponent toDecorate,
             final int mouseEntered,
             final int mouseExited) {
         final MouseListener toAdd = new MouseAdapter() {
 
                 private final Cursor entered = new Cursor(mouseEntered);
                 private final Cursor exited = new Cursor(mouseExited);
 
                 @Override
                 public void mouseEntered(final MouseEvent e) {
                     if (toDecorate.isEnabled()) {
                         toDecorate.setCursor(entered);
                     }
                 }
 
                 @Override
                 public void mouseExited(final MouseEvent e) {
                     if (toDecorate.isEnabled()) {
                         toDecorate.setCursor(exited);
                     }
                 }
             };
         toDecorate.addMouseListener(toAdd);
         return toAdd;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  url  DOCUMENT ME!
      */
     public static void openURL(final String url) {
         if (url == null) {
             return;
         }
         String gotoUrl = url;
         try {
             de.cismet.tools.BrowserLauncher.openURL(gotoUrl);
         } catch (Exception e2) {
             log.warn("das 1te Mal ging schief.Fehler beim Oeffnen von:" + gotoUrl + "\nLetzter Versuch", e2);
             try {
                 gotoUrl = gotoUrl.replaceAll("\\\\", "/");
                 gotoUrl = gotoUrl.replaceAll(" ", "%20");
                 de.cismet.tools.BrowserLauncher.openURL("file:///" + gotoUrl);
             } catch (Exception e3) {
                 log.error("Auch das 2te Mal ging schief.Fehler beim Oeffnen von:file://" + gotoUrl, e3);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mcTableName        DOCUMENT ME!
      * @param   domain             DOCUMENT ME!
      * @param   permissionToCheck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean hasCurrentUserPermissionOnMetaClass(final String mcTableName,
             final String domain,
             final PermissionType permissionToCheck) {
         final MetaClass mc = ClassCacheMultiple.getMetaClass(domain, mcTableName);
         return hasCurrentUserPermissionOnMetaClass(mc, permissionToCheck);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc                 DOCUMENT ME!
      * @param   permissionToCheck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean hasCurrentUserPermissionOnMetaClass(final MetaClass mc,
             final PermissionType permissionToCheck) {
         return hasUserPermissionOnMetaClass(mc, SessionManager.getSession().getUser(), permissionToCheck);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc                 DOCUMENT ME!
      * @param   user               DOCUMENT ME!
      * @param   permissionToCheck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean hasUserPermissionOnMetaClass(final MetaClass mc,
             final User user,
             final PermissionType permissionToCheck) {
         if ((mc != null) && (user != null) && (permissionToCheck != null)) {
             final PermissionHolder mcPermissions = mc.getPermissions();
             final UserGroup group = user.getUserGroup();
             switch (permissionToCheck) {
                 case READ: {
                     return mcPermissions.hasReadPermission(group);
                 }
                 case WRITE: {
                     return mcPermissions.hasWritePermission(group);
                 }
                 case READ_WRITE: {
                     return mcPermissions.hasWritePermission(group) && mcPermissions.hasReadPermission(group);
                 }
             }
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   propertyValue  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String propertyPrettyPrint(final Object propertyValue) {
         if (propertyValue instanceof Collection) {
             final Collection beanCollection = (Collection)propertyValue;
             final StringBuilder resultSB = new StringBuilder();
             for (final Object bean : beanCollection) {
                 if (resultSB.length() != 0) {
                     resultSB.append(", ");
                 }
                 resultSB.append(String.valueOf(bean));
             }
             return resultSB.toString();
         } else if (propertyValue != null) {
             return propertyValue.toString();
         } else {
             return "-";
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   box           DOCUMENT ME!
      * @param   searchString  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static int findComboBoxItemForString(final JComboBox box, final String searchString) {
         if ((box != null) && (searchString != null)) {
             final ComboBoxModel model = box.getModel();
             if (model != null) {
                 for (int i = model.getSize(); --i >= 0;) {
                     if (searchString.equals(String.valueOf(model.getElementAt(i)))) {
                         return i;
                     }
                 }
             }
         }
         return -1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   bean    DOCUMENT ME!
      * @param   suffix  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String getUrlFromBean(final CidsBean bean, final String suffix) {
         final Object obj = bean.getProperty("url_base_id");
         if (obj instanceof CidsBean) {
             final CidsBean urlBase = (CidsBean)obj;
             final StringBuffer bildURL = new StringBuffer(urlBase.getProperty("prot_prefix").toString());
             bildURL.append(urlBase.getProperty("server").toString());
             bildURL.append(urlBase.getProperty("path").toString());
             bildURL.append(bean.getProperty("object_name").toString());
             if (suffix != null) {
                 bildURL.append(suffix);
             }
             return bildURL.toString();
         }
         return null;
     }
 
     /**
      * Makes the parameter table alphanumerically sortable.
      *
      * @param   tbl  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static TableRowSorter<TableModel> decorateTableWithSorter(final JTable tbl) {
         final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tbl.getModel());
 //        sorter.setSortsOnUpdates(true);
         for (int i = 0; i < tbl.getColumnCount(); ++i) {
             sorter.setComparator(i, AlphanumComparator.getInstance());
         }
         tbl.setRowSorter(sorter);
         tbl.getTableHeader().addMouseListener(new TableHeaderUnsortMouseAdapter(tbl));
         return sorter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   label  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MouseAdapter decorateJLabelWithLinkBehaviour(final JLabel label) {
         final LabelLinkBehaviourMouseAdapter llbma = new LabelLinkBehaviourMouseAdapter(label);
         label.addMouseListener(llbma);
         return llbma;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   label      DOCUMENT ME!
      * @param   button     DOCUMENT ME!
      * @param   highlight  DOCUMENT ME!
      * @param   pressed    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MouseAdapter decorateJLabelAndButtonSynced(final JLabel label,
             final JButton button,
             final Icon highlight,
             final Icon pressed) {
         final MouseAdapter syncedAdapter = new SyncLabelButtonMouseAdapter(label, button, highlight, pressed);
         label.addMouseListener(syncedAdapter);
         button.addMouseListener(syncedAdapter);
         return syncedAdapter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   button     DOCUMENT ME!
      * @param   plain      DOCUMENT ME!
      * @param   highlight  DOCUMENT ME!
      * @param   pressed    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MouseAdapter decorateButtonWithStatusImages(final JButton button,
             final Icon plain,
             final Icon highlight,
             final Icon pressed) {
         final ImagedButtonMouseAdapter ibma = new ImagedButtonMouseAdapter(button, plain, highlight, pressed);
         button.addMouseListener(ibma);
         return ibma;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   button     DOCUMENT ME!
      * @param   highlight  DOCUMENT ME!
      * @param   pressed    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MouseAdapter decorateButtonWithStatusImages(final JButton button,
             final Icon highlight,
             final Icon pressed) {
         final ImagedButtonMouseAdapter ibma = new ImagedButtonMouseAdapter(button, highlight, pressed);
         button.addMouseListener(ibma);
         return ibma;
     }
 }
 
 /**
  * MouseAdapter for remove sorting from the table when perfoming a right-click on the header.
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 final class TableHeaderUnsortMouseAdapter extends MouseAdapter {
 
     //~ Instance fields --------------------------------------------------------
 
     private JTable tbl;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new TableHeaderUnsortMouseAdapter object.
      *
      * @param  tbl  DOCUMENT ME!
      */
     public TableHeaderUnsortMouseAdapter(final JTable tbl) {
         this.tbl = tbl;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void mousePressed(final MouseEvent e) {
         if (e.isPopupTrigger()) {
             tbl.getRowSorter().setSortKeys(null);
         }
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         if (e.isPopupTrigger()) {
             tbl.getRowSorter().setSortKeys(null);
         }
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 final class LabelLinkBehaviourMouseAdapter extends MouseAdapter {
 
     //~ Instance fields --------------------------------------------------------
 
     protected final JLabel label;
     private final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
     private final Font underlined;
     private final Font plain;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new LabelLinkBehaviourMouseAdapter object.
      *
      * @param  label  DOCUMENT ME!
      */
     public LabelLinkBehaviourMouseAdapter(final JLabel label) {
         this.label = label;
         plain = label.getFont();
         final Map<TextAttribute, Object> attributesMap = (Map<TextAttribute, Object>)plain.getAttributes();
         attributesMap.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
         underlined = plain.deriveFont(attributesMap);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         label.setCursor(handCursor);
         if (label.isEnabled() && (label.getFont() != underlined)) {
             label.setFont(underlined);
         }
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         label.setCursor(Cursor.getDefaultCursor());
         if (label.getFont() != plain) {
             label.setFont(plain);
         }
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 final class ImagedButtonMouseAdapter extends MouseAdapter {
 
     //~ Instance fields --------------------------------------------------------
 
     protected final JButton button;
     protected boolean over = false;
     protected boolean pressed = false;
     private final Icon plainIcon;
     private final Icon highlightIcon;
     private final Icon pressedIcon;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ImagedButtonMouseAdapter object.
      *
      * @param  button     DOCUMENT ME!
      * @param  highlight  DOCUMENT ME!
      * @param  pressed    DOCUMENT ME!
      */
     public ImagedButtonMouseAdapter(final JButton button, final Icon highlight, final Icon pressed) {
         this(button, button.getIcon(), highlight, pressed);
     }
 
     /**
      * Creates a new ImagedButtonMouseAdapter object.
      *
      * @param  button     DOCUMENT ME!
      * @param  plain      DOCUMENT ME!
      * @param  highlight  DOCUMENT ME!
      * @param  pressed    DOCUMENT ME!
      */
     public ImagedButtonMouseAdapter(final JButton button, final Icon plain, final Icon highlight, final Icon pressed) {
         this.button = button;
         ObjectRendererUtils.decorateComponentWithMouseOverCursorChange(
             button,
             Cursor.HAND_CURSOR,
             Cursor.DEFAULT_CURSOR);
         this.plainIcon = plain;
         this.highlightIcon = highlight;
         this.pressedIcon = pressed;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         over = true;
         handleEvent(e);
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         over = false;
         handleEvent(e);
     }
 
     @Override
     public void mousePressed(final MouseEvent e) {
         pressed = true;
         handleEvent(e);
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         pressed = false;
         handleEvent(e);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  icon  DOCUMENT ME!
      */
     private void testAndSet(final Icon icon) {
         if (button.getIcon() != icon) {
             button.setIcon(icon);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  e  DOCUMENT ME!
      */
     protected void handleEvent(final MouseEvent e) {
         if (button.isEnabled()) {
             if (pressed && over) {
                 testAndSet(pressedIcon);
             } else if (over) {
                 testAndSet(highlightIcon);
             } else {
                 testAndSet(plainIcon);
             }
         } else {
             testAndSet(plainIcon);
         }
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 final class SyncLabelButtonMouseAdapter extends MouseAdapter {
 
     //~ Instance fields --------------------------------------------------------
 
     private final MouseAdapter delegateButton;
     private final MouseAdapter delegateLabel;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SyncLabelButtonMouseAdapter object.
      *
      * @param  label      DOCUMENT ME!
      * @param  button     DOCUMENT ME!
      * @param  highlight  DOCUMENT ME!
      * @param  pressed    DOCUMENT ME!
      */
     public SyncLabelButtonMouseAdapter(final JLabel label,
             final JButton button,
             final Icon highlight,
             final Icon pressed) {
         delegateButton = new ImagedButtonMouseAdapter(button, highlight, pressed);
         delegateLabel = new LabelLinkBehaviourMouseAdapter(label);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         delegateButton.mouseEntered(e);
         delegateLabel.mouseEntered(e);
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         delegateButton.mouseExited(e);
         delegateLabel.mouseExited(e);
     }
 
     @Override
     public void mousePressed(final MouseEvent e) {
         delegateButton.mousePressed(e);
         delegateLabel.mousePressed(e);
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         delegateButton.mouseReleased(e);
         delegateLabel.mouseReleased(e);
     }
 }
