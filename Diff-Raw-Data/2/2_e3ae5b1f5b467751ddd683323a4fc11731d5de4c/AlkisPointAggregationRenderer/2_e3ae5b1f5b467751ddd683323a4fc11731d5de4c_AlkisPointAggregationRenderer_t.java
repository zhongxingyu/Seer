 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 srichter
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * Alkis_pointAggregationRenderer.java
  *
  * Created on 03.03.2010, 09:45:18
  */
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import org.openide.util.NbBundle;
 
 import java.awt.EventQueue;
 import java.awt.geom.Rectangle2D;
 
 import java.net.URL;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JOptionPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 import de.cismet.cids.client.tools.DevelopmentTools;
 
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtils;
 import de.cismet.cids.custom.objectrenderer.utils.billing.BillingPopup;
 import de.cismet.cids.custom.objectrenderer.utils.billing.ProductGroupAmount;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanAggregationRenderer;
 
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.printing.JasperDownload;
 import de.cismet.cismap.commons.gui.printing.PrintingWidget;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.HttpDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public final class AlkisPointAggregationRenderer extends javax.swing.JPanel implements CidsBeanAggregationRenderer,
     RequestsFullSizeComponent {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
             AlkisPointAggregationRenderer.class);
 
     private static final double BUFFER = 0.005;
 
     public static final HashMap<String, String> POST_HEADER = new HashMap<String, String>();
 
     static {
         POST_HEADER.put(WebAccessManager.HEADER_CONTENTTYPE_KEY, WebAccessManager.HEADER_CONTENTTYPE_VALUE_POST);
     }
 
     // Spaltenueberschriften
     private static final String[] AGR_COMLUMN_NAMES = new String[] {
             "Auswahl",
             "Punktkennung",
             "Punktart",
             "Punktort"
         };
     // Spaltenbreiten
     private static final int[] AGR_COMLUMN_WIDTH = new int[] { 40, 80, 200, 200 };
     // Namen der Properties -> Spalten
     private static final String[] AGR_PROPERTY_NAMES = new String[] { "pointcode", "pointtype", "geom.geo_field" };
     // Formater fuer Hochwert/Rechtswert
     private static final NumberFormat HW_RW_NUMBER_FORMAT = new DecimalFormat("##########.###");
     // Modell fuer die Auswahlbox des produktformats
     private static final String PDF = "Punktliste (PDF)";
     private static final String HTML = "Punktliste (HTML)";
     private static final String TEXT = "Punktliste (TEXT)";
     private static final String APMAP = "AP-Karten (PDF)";
     // Speichert Punkte ueber die Lebzeit eines Renderers hinaus
     private static final Set<CidsBean> gehaltenePunkte = TypeSafeCollections.newLinkedHashSet();
 
     //~ Instance fields --------------------------------------------------------
 
     private List<CidsBean> cidsBeans = null;
     private Collection<CidsBean> pureSelectionCidsBeans = null;
     private String title = "";
     private PointTableModel tableModel;
     private Map<CidsBean, CidsFeature> features;
     private Comparator<Integer> tableComparator;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCreate;
     private javax.swing.JButton btnRelease;
     private javax.swing.JButton btnRemember;
     private javax.swing.JComboBox cbProducts;
     private javax.swing.JLabel lblProductDescr;
     private de.cismet.cismap.commons.gui.MappingComponent mappingComponent;
     private javax.swing.JPanel panMap;
     private javax.swing.JPanel panProdukte;
     private javax.swing.JScrollPane scpAggregationTable;
     private javax.swing.JTable tblAggregation;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form Alkis_pointAggregationRenderer.
      */
     public AlkisPointAggregationRenderer() {
         initComponents();
         scpAggregationTable.getViewport().setOpaque(false);
         tblAggregation.getSelectionModel().addListSelectionListener(new TableSelectionListener());
         tableComparator = new TableModelIndexConvertingToViewIndexComparator((tblAggregation));
         btnRelease.setEnabled(gehaltenePunkte.size() > 0);
         btnRemember.setVisible(false);
         btnRelease.setVisible(false);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         scpAggregationTable = new javax.swing.JScrollPane();
         tblAggregation = new javax.swing.JTable();
         panProdukte = new javax.swing.JPanel();
         cbProducts = new javax.swing.JComboBox();
         btnCreate = new javax.swing.JButton();
         lblProductDescr = new javax.swing.JLabel();
         btnRemember = new javax.swing.JButton();
         btnRelease = new javax.swing.JButton();
         panMap = new javax.swing.JPanel();
         mappingComponent = new de.cismet.cismap.commons.gui.MappingComponent();
 
         addAncestorListener(new javax.swing.event.AncestorListener() {
 
                 @Override
                 public void ancestorMoved(final javax.swing.event.AncestorEvent evt) {
                 }
                 @Override
                 public void ancestorAdded(final javax.swing.event.AncestorEvent evt) {
                     formAncestorAdded(evt);
                 }
                 @Override
                 public void ancestorRemoved(final javax.swing.event.AncestorEvent evt) {
                 }
             });
         setLayout(new java.awt.BorderLayout());
 
         tblAggregation.setOpaque(false);
         tblAggregation.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     tblAggregationFocusLost(evt);
                 }
             });
         scpAggregationTable.setViewportView(tblAggregation);
 
         add(scpAggregationTable, java.awt.BorderLayout.CENTER);
 
         panProdukte.setOpaque(false);
         panProdukte.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 5;
         gridBagConstraints.ipady = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
         panProdukte.add(cbProducts, gridBagConstraints);
 
         btnCreate.setText("Erzeugen");
         btnCreate.setFocusPainted(false);
         btnCreate.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCreateActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 5);
         panProdukte.add(btnCreate, gridBagConstraints);
 
         lblProductDescr.setText("Verfügbare Berichte:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
         panProdukte.add(lblProductDescr, gridBagConstraints);
 
         btnRemember.setText("Merken");
         btnRemember.setFocusPainted(false);
         btnRemember.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRememberActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
         panProdukte.add(btnRemember, gridBagConstraints);
 
         btnRelease.setText("Vergessen");
         btnRelease.setFocusPainted(false);
         btnRelease.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnReleaseActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 10);
         panProdukte.add(btnRelease, gridBagConstraints);
 
         add(panProdukte, java.awt.BorderLayout.SOUTH);
 
         panMap.setMaximumSize(new java.awt.Dimension(300, 450));
         panMap.setMinimumSize(new java.awt.Dimension(300, 450));
         panMap.setOpaque(false);
         panMap.setPreferredSize(new java.awt.Dimension(300, 450));
         panMap.setLayout(new java.awt.GridBagLayout());
 
         mappingComponent.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         mappingComponent.setMaximumSize(new java.awt.Dimension(100, 100));
         mappingComponent.setMinimumSize(new java.awt.Dimension(100, 100));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         panMap.add(mappingComponent, gridBagConstraints);
 
         add(panMap, java.awt.BorderLayout.EAST);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCreateActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
         if (!ObjectRendererUtils.checkActionTag(AlkisPointRenderer.PRODUCT_ACTION_TAG_PUNKTLISTE)) {
             JOptionPane.showMessageDialog(this, "Sie besitzen keine Berechtigung zur Erzeugung dieses Produkts!");
             return;
         }
 
         final String format = cbProducts.getSelectedItem().toString();
 
         if (APMAP.equalsIgnoreCase(format)) {
             final Collection<CidsBean> selectedAlkisPoints = getSelectedAlkisPointsContainingAPMap();
 
             if (selectedAlkisPoints.isEmpty()) {
                 JOptionPane.showMessageDialog(
                     StaticSwingTools.getParentFrame(this),
                     NbBundle.getMessage(
                         AlkisPointAggregationRenderer.class,
                         "AlkisPointAggregationRenderer.btnCreateActionPerformed(ActionEvent).emptySelection_apmaps.message"),
                     NbBundle.getMessage(
                         AlkisPointAggregationRenderer.class,
                         "AlkisPointAggregationRenderer.btnCreateActionPerformed(ActionEvent).emptySelection_apmaps.title"),
                     JOptionPane.INFORMATION_MESSAGE);
                 return;
             }
 
             CismetThreadPool.execute(new GenerateAPMapReport(selectedAlkisPoints));
         } else {
             final Collection<CidsBean> selectedAlkisPoints = getSelectedAlkisPoints();
 
             if (selectedAlkisPoints.isEmpty()) {
                 JOptionPane.showMessageDialog(
                     StaticSwingTools.getParentFrame(this),
                     NbBundle.getMessage(
                         AlkisPointAggregationRenderer.class,
                         "AlkisPointAggregationRenderer.btnCreateActionPerformed(ActionEvent).emptySelection.message"),
                     NbBundle.getMessage(
                         AlkisPointAggregationRenderer.class,
                         "AlkisPointAggregationRenderer.btnCreateActionPerformed(ActionEvent).emptySelection.title"),
                     JOptionPane.INFORMATION_MESSAGE);
                 return;
             }
 
             final int numOfPoints = selectedAlkisPoints.size();
 
             if (format.equalsIgnoreCase(PDF)) {
                 try {
                     if (BillingPopup.doBilling(
                                     "pktlstpdf",
                                     "no.yet",
                                     (Geometry)null,
                                    new ProductGroupAmount("eafifty", 1 + (int)Math.floor(numOfPoints / 50f)))) {
                         CismetThreadPool.execute(new GenerateProduct(format, selectedAlkisPoints));
                     }
                 } catch (Exception e) {
                     log.error("Error when trying to produce a alkis product", e);
                     // Hier noch ein Fehlerdialog
                 }
             } else if (format.equalsIgnoreCase(TEXT)) {
                 try {
                     final String eapkt;
                     if (numOfPoints <= 1000) {
                         eapkt = "eapkt_1000";
                     } else if (numOfPoints <= 10000) {
                         eapkt = "eapkt_1001-10000";
                     } else if (numOfPoints <= 100000) {
                         eapkt = "eapkt_10001-100000";
                     } else if (numOfPoints <= 1000000) {
                         eapkt = "eapkt_100001-1000000";
                     } else {
                         eapkt = "eapkt_1000001";
                     }
 
                     if (BillingPopup.doBilling(
                                     "pktlsttxt",
                                     "no.yet",
                                     (Geometry)null,
                                     new ProductGroupAmount(eapkt, numOfPoints))) {
                         CismetThreadPool.execute(new GenerateProduct(format, selectedAlkisPoints));
                     }
                 } catch (Exception e) {
                     log.error("Error when trying to produce a alkis product", e);
                     // Hier noch ein Fehlerdialog
                 }
             } else if (format.equalsIgnoreCase(APMAP)) {
                 try {
                     if (BillingPopup.doBilling(
                                     "appdf",
                                     "no.yet",
                                     (Geometry)null,
                                     new ProductGroupAmount("ea", numOfPoints))) {
                         CismetThreadPool.execute(new GenerateProduct(format, selectedAlkisPoints));
                     }
                 } catch (Exception e) {
                     log.error("Error when trying to produce a alkis product", e);
                     // Hier noch ein Fehlerdialog
                 }
             } else {
                 CismetThreadPool.execute(new GenerateProduct(format, selectedAlkisPoints));
             }
         }
     }//GEN-LAST:event_btnCreateActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnReleaseActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReleaseActionPerformed
         gehaltenePunkte.clear();
         setCidsBeans(pureSelectionCidsBeans);
         btnRelease.setEnabled(false);
     }//GEN-LAST:event_btnReleaseActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRememberActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRememberActionPerformed
         gehaltenePunkte.addAll(cidsBeans);
         btnRelease.setEnabled(true);
     }//GEN-LAST:event_btnRememberActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void tblAggregationFocusLost(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblAggregationFocusLost
         animateToOverview();
         tblAggregation.clearSelection();
     }//GEN-LAST:event_tblAggregationFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formAncestorAdded(final javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
         CismetThreadPool.execute(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException ex) {
                         log.warn("Sleeping to wait for zooming to added features was interrupted.", ex);
                     }
                     EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 animateToOverview();
                             }
                         });
                 }
             });
     }//GEN-LAST:event_formAncestorAdded
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Collection<CidsBean> getSelectedAlkisPoints() {
         final Collection<CidsBean> result = new LinkedList<CidsBean>();
 
         final TableModel tableModel = tblAggregation.getModel();
         for (int i = 0; i < tableModel.getRowCount(); ++i) {
             final Object includedObj = tableModel.getValueAt(i, 0);
             if ((includedObj instanceof Boolean) && (Boolean)includedObj) {
                 result.add(cidsBeans.get(i));
             }
         }
 
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Collection<CidsBean> getSelectedAlkisPointsContainingAPMap() {
         final Collection<CidsBean> result = new LinkedList<CidsBean>();
         final List<Integer> selectedIndexes = new ArrayList<Integer>();
 
         final TableModel tableModel = tblAggregation.getModel();
         for (int i = 0; i < tableModel.getRowCount(); ++i) {
             final Object includedObj = tableModel.getValueAt(i, 0);
             if ((includedObj instanceof Boolean) && (Boolean)includedObj) {
                 if (AlkisPointRenderer.hasAPMap(cidsBeans.get(i))) {
                     selectedIndexes.add(Integer.valueOf(i));
                 }
             }
         }
 
         Collections.sort(selectedIndexes, tableComparator);
 
         for (final Integer selectedIndex : selectedIndexes) {
             result.add(cidsBeans.get(selectedIndex));
         }
 
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   alkisPoints  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String getPunktlistenStringForChosenPoints(final Collection<CidsBean> alkisPoints) {
         final StringBuffer punktListeString = new StringBuffer();
 
         for (final CidsBean alkisPoint : alkisPoints) {
             if (punktListeString.length() > 0) {
                 punktListeString.append(",");
             }
             punktListeString.append(AlkisUtils.PRODUCTS.getPointDataForProduct(alkisPoint));
         }
 
         return punktListeString.toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Collection<CidsBean> getCidsBeans() {
         return cidsBeans;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public String getTitle() {
         return title;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  title  DOCUMENT ME!
      */
     @Override
     public void setTitle(final String title) {
         String desc = "Punktliste";
         final Collection<CidsBean> beans = cidsBeans;
         if ((beans != null) && (beans.size() > 0)) {
             desc += " - " + beans.size() + " Punkte ausgewählt";
         }
         this.title = desc;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  beans  DOCUMENT ME!
      */
     @Override
     public void setCidsBeans(final Collection<CidsBean> beans) {
         if (beans instanceof List) {
             pureSelectionCidsBeans = beans;
 
             if (gehaltenePunkte.size() > 0) {
                 gehaltenePunkte.addAll(beans);
                 this.cidsBeans = Arrays.asList(gehaltenePunkte.toArray(new CidsBean[gehaltenePunkte.size()]));
             } else {
                 this.cidsBeans = (List<CidsBean>)beans;
             }
 
             features = new HashMap<CidsBean, CidsFeature>(beans.size());
             initMap();
 
             boolean allowAPMapReport = false;
             final List<Object[]> tableData = TypeSafeCollections.newArrayList();
             for (final CidsBean punktBean : cidsBeans) {
                 tableData.add(cidsBean2Row(punktBean));
 
                 // We only want to find the first alkis point which may have an AP map.
                 if (!allowAPMapReport) {
                     allowAPMapReport = AlkisPointRenderer.hasAPMap(punktBean);
                 }
             }
             tableModel = new PointTableModel(tableData.toArray(new Object[tableData.size()][]), AGR_COMLUMN_NAMES);
             tblAggregation.setModel(tableModel);
             final TableColumnModel cModel = tblAggregation.getColumnModel();
             for (int i = 0; i < cModel.getColumnCount(); ++i) {
                 cModel.getColumn(i).setPreferredWidth(AGR_COMLUMN_WIDTH[i]);
             }
             ObjectRendererUtils.decorateTableWithSorter(tblAggregation);
 
             if (allowAPMapReport) {
                 cbProducts.setModel(new DefaultComboBoxModel(new String[] { PDF, HTML, TEXT, APMAP }));
             } else {
                 cbProducts.setModel(new DefaultComboBoxModel(new String[] { PDF, HTML, TEXT }));
             }
         }
         setTitle(null);
     }
 
     /**
      * Extracts the date from a CidsBean into an Object[] -> table row. (Collection attributes are flatened to
      * comaseparated lists)
      *
      * @param   baulastBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Object[] cidsBean2Row(final CidsBean baulastBean) {
         if (baulastBean != null) {
             final Object[] result = new Object[AGR_COMLUMN_NAMES.length];
             result[0] = Boolean.TRUE;
             for (int i = 0; i < AGR_PROPERTY_NAMES.length; ++i) {
                 final Object property = baulastBean.getProperty(AGR_PROPERTY_NAMES[i]);
                 String propertyString;
                 if (property instanceof Point) {
                     final Point point = (Point)property;
                     propertyString = "RW: " + HW_RW_NUMBER_FORMAT.format(point.getX()) + "; HW: "
                                 + HW_RW_NUMBER_FORMAT.format(point.getY());
                 } else {
                     propertyString = ObjectRendererUtils.propertyPrettyPrint(property);
                 }
                 result[i + 1] = propertyString;
             }
             return result;
         }
         return new Object[0];
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         try {
             final ActiveLayerModel mappingModel = new ActiveLayerModel();
             mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
             final XBoundingBox box = boundingBoxFromPointList(cidsBeans);
             mappingModel.addHome(new XBoundingBox(
                     box.getX1(),
                     box.getY1(),
                     box.getX2(),
                     box.getY2(),
                     AlkisConstants.COMMONS.SRS_SERVICE,
                     true));
             final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisConstants.COMMONS.MAP_CALL_STRING));
             swms.setName("Alkis_Points");
             mappingModel.addLayer(swms);
             mappingComponent.setMappingModel(mappingModel);
             mappingComponent.setAnimationDuration(0);
             mappingComponent.gotoInitialBoundingBox();
             mappingComponent.setInteractionMode(MappingComponent.ZOOM);
             mappingComponent.unlock();
             // finally when all configurations are done ...
             mappingComponent.setInteractionMode("MUTE");
 //            mappingComponent.addCustomInputListener("MUTE", new PBasicInputEventHandler() {
 //
 //                @Override
 //                public void mouseClicked(PInputEvent evt) {
 //                    try {
 //                        if (evt.getClickCount() > 1) {
 ////                            if (realLandParcelMetaObjectsCache == null) {
 ////                                CismetThreadPool.execute(new GeomQueryWorker());
 ////                            } else {
 ////                                switchToMapAndShowGeometries();
 ////                            }
 //                        }
 //                    } catch (Exception ex) {
 //                        log.error(ex, ex);
 //                    }
 //                }
 //            });
 //            mappingComponent.setInteractionMode("MUTE");
             for (final CidsBean cidsBean : cidsBeans) {
                 final CidsFeature feature = new CidsFeature(cidsBean.getMetaObject());
                 features.put(cidsBean, feature);
             }
             mappingComponent.getFeatureCollection().addFeatures(features.values());
         } catch (Throwable t) {
             log.fatal(t, t);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void animateToOverview() {
         mappingComponent.gotoInitialBoundingBox();
         final Rectangle2D viewBounds = mappingComponent.getCamera().getViewBounds().getBounds2D();
         final double scale = mappingComponent.getScaleDenominator();
         final double newX = ((viewBounds.getX() / scale) - BUFFER) * scale;
         final double newY = ((viewBounds.getY() / scale) - BUFFER) * scale;
         final double newWidth = ((viewBounds.getWidth() / scale) + (BUFFER * 2)) * scale;
         final double newHeight = ((viewBounds.getHeight() / scale) + (BUFFER * 2)) * scale;
         viewBounds.setRect(newX, newY, newWidth, newHeight);
         mappingComponent.getCamera()
                 .animateViewToCenterBounds(viewBounds, true, mappingComponent.getAnimationDuration());
     }
     /**
      * DOCUMENT ME!
      *
      * @param   lpList  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private XBoundingBox boundingBoxFromPointList(final Collection<CidsBean> lpList) {
         final List<Geometry> allGeomList = TypeSafeCollections.newArrayList();
 
         for (final CidsBean parcel : lpList) {
             try {
                 allGeomList.add((Geometry)parcel.getProperty("geom.geo_field"));
             } catch (Exception ex) {
                 log.warn(ex, ex);
             }
         }
         final GeometryCollection geoCollection = new GeometryCollection(allGeomList.toArray(
                     new Geometry[allGeomList.size()]),
                 new GeometryFactory());
 
         return new XBoundingBox(geoCollection.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         mappingComponent.dispose();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
 //        final CidsBean[] cidsBeans = DevelopmentTools.createCidsBeansFromRMIConnectionOnLocalhost("WUNDA_BLAU", "Administratoren", "admin", "sb", "alkis_point", "pointtype=1", 10);
         final CidsBean[] cidsBeans = DevelopmentTools.createCidsBeansFromRMIConnectionOnLocalhost(
                 "WUNDA_BLAU",
                 "Administratoren",
                 "admin",
                 "sb",
                 "alkis_point",
                 "pointtype=4",
                 10);
         DevelopmentTools.createAggregationRendererInFrameFromRMIConnectionOnLocalhost(Arrays.asList(cidsBeans),
             "ALKIS-Punkte Aggregationsrenderer",
             1024,
             768);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class TableSelectionListener implements ListSelectionListener {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void valueChanged(final ListSelectionEvent e) {
             if (!e.getValueIsAdjusting() && (cidsBeans != null)) {
                 final int[] indexes = tblAggregation.getSelectedRows();
 
                 if ((indexes != null) && (indexes.length > 0)) {
                     for (final int viewIdx : indexes) {
                         final int modelIdx = tblAggregation.getRowSorter().convertRowIndexToModel(viewIdx);
                         if ((modelIdx > -1) && (modelIdx < cidsBeans.size())) {
                             final CidsBean selectedBean = cidsBeans.get(modelIdx);
                             final XBoundingBox boxToGoto = new XBoundingBox(features.get(selectedBean).getGeometry()
                                             .getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
                             boxToGoto.setX1(boxToGoto.getX1()
                                         - (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth()));
                             boxToGoto.setX2(boxToGoto.getX2()
                                         + (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth()));
                             boxToGoto.setY1(boxToGoto.getY1()
                                         - (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight()));
                             boxToGoto.setY2(boxToGoto.getY2()
                                         + (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight()));
                             mappingComponent.gotoBoundingBox(boxToGoto, false, true, 500);
                             break;
                         }
                     }
                     // mappingComponent.zoomToFeatureCollection();
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     static final class PointTableModel extends DefaultTableModel {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PointTableModel object.
          *
          * @param  data    DOCUMENT ME!
          * @param  labels  DOCUMENT ME!
          */
         public PointTableModel(final Object[][] data, final String[] labels) {
             super(data, labels);
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   row     DOCUMENT ME!
          * @param   column  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public boolean isCellEditable(final int row, final int column) {
             return column == 0;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   columnIndex  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public Class<?> getColumnClass(final int columnIndex) {
             if (columnIndex == 0) {
                 return Boolean.class;
             } else {
                 return super.getColumnClass(columnIndex);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public static class AlkisPointReportBean {
 
         //~ Instance fields ----------------------------------------------------
 
         private Collection<CidsBean> alkisPunkte;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new AlkisPointReportBean object.
          *
          * @param  alkisPunkte  DOCUMENT ME!
          */
         public AlkisPointReportBean(final Collection<CidsBean> alkisPunkte) {
             this.alkisPunkte = alkisPunkte;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Collection<CidsBean> getAlkisPunkte() {
             return alkisPunkte;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class GenerateProduct implements Runnable {
 
         //~ Instance fields ----------------------------------------------------
 
         private String format;
         private Collection<CidsBean> alkisPoints;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new GenerateProduct object.
          *
          * @param  format       DOCUMENT ME!
          * @param  alkisPoints  DOCUMENT ME!
          */
         public GenerateProduct(final String format, final Collection<CidsBean> alkisPoints) {
             this.format = format;
             this.alkisPoints = alkisPoints;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          */
         @Override
         public void run() {
             final String punktListenString = getPunktlistenStringForChosenPoints(alkisPoints);
             final String code;
             final String extension;
 
             if (PDF.equals(format)) {
                 code = AlkisUtils.PRODUCTS.PUNKTLISTE_PDF;
                 extension = ".pdf";
             } else if (HTML.equals(format)) {
                 code = AlkisUtils.PRODUCTS.PUNKTLISTE_HTML;
                 extension = ".html";
             } else {
                 code = AlkisUtils.PRODUCTS.PUNKTLISTE_TXT;
                 extension = ".plst";
             }
 
             if (punktListenString.length() > 3) {
                 if ((code != null) && (code.length() > 0)) {
                     try {
                         final String url = AlkisUtils.PRODUCTS.productListenNachweisUrl(punktListenString, code);
                         if ((url != null) && (url.trim().length() > 0)) {
                             if (!DownloadManagerDialog.showAskingForUserTitle(AlkisPointAggregationRenderer.this)) {
                                 return;
                             }
 
                             HttpDownload download = null;
                             final int parameterPosition = url.indexOf('?');
 
                             if (parameterPosition < 0) {
                                 download = new HttpDownload(
                                         new URL(url),
                                         "",
                                         DownloadManagerDialog.getJobname(),
                                         "Punktnachweis",
                                         code,
                                         extension);
                             } else {
                                 final String parameters = url.substring(parameterPosition + 1);
                                 download = new HttpDownload(
                                         new URL(url.substring(0, parameterPosition)),
                                         parameters,
                                         POST_HEADER,
                                         DownloadManagerDialog.getJobname(),
                                         "Punktnachweis",
                                         code,
                                         extension);
                             }
 
                             DownloadManager.instance().add(download);
                         }
                     } catch (Exception ex) {
                         ObjectRendererUtils.showExceptionWindowToUser(
                             "Fehler beim Aufruf des Produkts: "
                                     + code,
                             ex,
                             AlkisPointAggregationRenderer.this);
                         log.error("The URL to download product '" + code + "' (actionTag: "
                                     + AlkisPointRenderer.PRODUCT_ACTION_TAG_PUNKTLISTE + ") could not be constructed.",
                             ex);
                     }
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class GenerateAPMapReport implements Runnable {
 
         //~ Instance fields ----------------------------------------------------
 
         private Collection<CidsBean> alkisPoints;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new GenerateAPMapReport object.
          *
          * @param  alkisPoints  DOCUMENT ME!
          */
         public GenerateAPMapReport(final Collection<CidsBean> alkisPoints) {
             this.alkisPoints = alkisPoints;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          */
         @Override
         public void run() {
             final Collection<AlkisPointReportBean> reportBeans = new LinkedList<AlkisPointReportBean>();
             reportBeans.add(new AlkisPointReportBean(alkisPoints));
             final JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportBeans);
 
             final JasperReport jasperReport;
             final JasperPrint jasperPrint;
             try {
                 jasperReport = (JasperReport)JRLoader.loadObject(AlkisPointAggregationRenderer.class
                                 .getResourceAsStream(
                                     "/de/cismet/cids/custom/wunda_blau/res/apmaps.jasper"));
                 jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), dataSource);
             } catch (JRException ex) {
                 log.error("Could not generate report for ap maps.", ex);
 
                 final ErrorInfo ei = new ErrorInfo(NbBundle.getMessage(
                             AlkisPointAggregationRenderer.class,
                             "AlkisPointAggregationRenderer.GenerateAPMapReport.run().ErrorInfo.title"),   // NOI18N
                         NbBundle.getMessage(
                             PrintingWidget.class,
                             "AlkisPointAggregationRenderer.GenerateAPMapReport.run().ErrorInfo.message"), // NOI18N
                         null,
                         null,
                         ex,
                         Level.ALL,
                         null);
                 JXErrorPane.showDialog(AlkisPointAggregationRenderer.this, ei);
 
                 return;
             }
 
             if (DownloadManagerDialog.showAskingForUserTitle(AlkisPointAggregationRenderer.this)) {
                 final String jobname = DownloadManagerDialog.getJobname();
 
                 DownloadManager.instance().add(new JasperDownload(jasperPrint, jobname, "AP-Karten", "apkarten"));
             }
         }
     }
 }
