 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.geom.Rectangle2D;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import javax.swing.JOptionPane;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import de.cismet.cids.client.tools.DevelopmentTools;
 
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanAggregationRenderer;
 
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.printing.JasperDownload;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class VermessungRissAggregationRenderer extends javax.swing.JPanel implements CidsBeanAggregationRenderer,
     RequestsFullSizeComponent {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(VermessungRissAggregationRenderer.class);
 
     private static final double BUFFER = 0.005;
 
     private static final String PARAMETER_JOBNUMBER = "JOBNUMBER";
     private static final String PARAMETER_PROJECTNAME = "PROJECTNAME";
 
     // Spaltenueberschriften
     private static final String[] AGR_COMLUMN_NAMES = new String[] {
             "Auswahl",
             "Schlüssel",
             "Gemarkung",
             "Flur",
            "Blatt"
         };
     // Namen der Properties -> Spalten
     private static final String[] AGR_PROPERTY_NAMES = new String[] {
             "schluessel",
             "gemarkung.name",
             "flur",
            "blatt"
         };
 
    private static final int[] AGR_COMLUMN_WIDTH = new int[] { 40, 100, 140, 100, 100 };
 
     //~ Instance fields --------------------------------------------------------
 
     private List<CidsBean> cidsBeans;
     private String title = "";
     private PointTableModel tableModel;
     private Map<CidsBean, CidsFeature> features;
     private Comparator<Integer> tableComparator;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnGenerateReport;
     private javax.swing.Box.Filler flrGap;
     private javax.swing.JLabel lblJobnumber;
     private javax.swing.JLabel lblProjectname;
     private de.cismet.cismap.commons.gui.MappingComponent mappingComponent;
     private javax.swing.JPanel panMap;
     private javax.swing.JPanel pnlReport;
     private javax.swing.JScrollPane scpRisse;
     private javax.swing.JTable tblRisse;
     private javax.swing.JTextField txtJobnumber;
     private javax.swing.JTextField txtProjectname;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form NivellementPunktAggregationRenderer.
      */
     public VermessungRissAggregationRenderer() {
         initComponents();
 
         scpRisse.getViewport().setOpaque(false);
         tblRisse.getSelectionModel().addListSelectionListener(new TableSelectionListener());
 
         tableComparator = new TableModelIndexConvertingToViewIndexComparator(tblRisse);
     }
 
     //~ Methods ----------------------------------------------------------------
 
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
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         scpRisse = new javax.swing.JScrollPane();
         tblRisse = new javax.swing.JTable();
         panMap = new javax.swing.JPanel();
         mappingComponent = new de.cismet.cismap.commons.gui.MappingComponent();
         pnlReport = new javax.swing.JPanel();
         flrGap = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 0));
         lblProjectname = new javax.swing.JLabel();
         txtProjectname = new javax.swing.JTextField();
         lblJobnumber = new javax.swing.JLabel();
         txtJobnumber = new javax.swing.JTextField();
         btnGenerateReport = new javax.swing.JButton();
 
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
         setLayout(new java.awt.GridBagLayout());
 
         tblRisse.setModel(new javax.swing.table.DefaultTableModel(
                 new Object[][] {
                     {},
                     {},
                     {},
                     {}
                 },
                 new String[] {}));
         tblRisse.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     tblRisseFocusLost(evt);
                 }
             });
         scpRisse.setViewportView(tblRisse);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
         add(scpRisse, gridBagConstraints);
 
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
         panMap.add(mappingComponent, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
         add(panMap, gridBagConstraints);
 
         pnlReport.setOpaque(false);
         pnlReport.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         pnlReport.add(flrGap, gridBagConstraints);
 
         lblProjectname.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissAggregationRenderer.class,
                 "VermessungRissAggregationRenderer.lblProjectname.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlReport.add(lblProjectname, gridBagConstraints);
 
         txtProjectname.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissAggregationRenderer.class,
                 "VermessungRissAggregationRenderer.txtProjectname.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlReport.add(txtProjectname, gridBagConstraints);
 
         lblJobnumber.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissAggregationRenderer.class,
                 "VermessungRissAggregationRenderer.lblJobnumber.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         pnlReport.add(lblJobnumber, gridBagConstraints);
 
         txtJobnumber.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissAggregationRenderer.class,
                 "VermessungRissAggregationRenderer.txtJobnumber.text")); // NOI18N
         txtJobnumber.setMaximumSize(new java.awt.Dimension(250, 20));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         pnlReport.add(txtJobnumber, gridBagConstraints);
 
         btnGenerateReport.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissAggregationRenderer.class,
                 "VermessungRissAggregationRenderer.btnGenerateReport.text"));        // NOI18N
         btnGenerateReport.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissAggregationRenderer.class,
                 "VermessungRissAggregationRenderer.btnGenerateReport.toolTipText")); // NOI18N
         btnGenerateReport.setFocusPainted(false);
         btnGenerateReport.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnGenerateReportActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         pnlReport.add(btnGenerateReport, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         add(pnlReport, gridBagConstraints);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void tblRisseFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_tblRisseFocusLost
         tblRisse.clearSelection();
         animateToOverview();
     }                                                                     //GEN-LAST:event_tblRisseFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnGenerateReportActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnGenerateReportActionPerformed
         final Collection<CidsBean> selectedVermessungsrisse = getSelectedVermessungsrisse();
 
         if (selectedVermessungsrisse.isEmpty()) {
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 NbBundle.getMessage(
                     VermessungRissAggregationRenderer.class,
                     "VermessungRissAggregationRenderer.btnGenerateReportActionPerformed(ActionEvent).emptySelection.message"),
                 NbBundle.getMessage(
                     VermessungRissAggregationRenderer.class,
                     "VermessungRissAggregationRenderer.btnGenerateReportActionPerformed(ActionEvent).emptySelection.title"),
                 JOptionPane.INFORMATION_MESSAGE);
             return;
         }
 
         final Runnable runnable = new Runnable() {
 
                 @Override
                 public void run() {
                     final Collection<VermessungRissReportBean> reportBeans = new LinkedList<VermessungRissReportBean>();
                     final Collection<VermessungRissImageReportBean> imageBeans =
                         new LinkedList<VermessungRissImageReportBean>();
 
                     Image[] bilder = null;
                     Image[] grenzniederschriften = null;
                     for (final CidsBean vermessungsriss : selectedVermessungsrisse) {
                         try {
                             if (vermessungsriss.getProperty("bild") instanceof String) {
                                 bilder = VermessungRissReportScriptlet.loadImages(
                                         AlkisConstants.COMMONS.VERMESSUNG_HOST_BILDER,
                                         vermessungsriss.getProperty("bild").toString());
                             }
                         } catch (final Exception ex) {
                             // TODO: User feedback?
                             LOG.warn("Could not include 'bild' for vermessungsriss '" + vermessungsriss.toJSONString()
                                         + "'.",
                                 ex);
                         }
 
                         try {
                             if (vermessungsriss.getProperty("grenzniederschrift") instanceof String) {
                                 grenzniederschriften = VermessungRissReportScriptlet.loadImages(
                                         AlkisConstants.COMMONS.VERMESSUNG_HOST_GRENZNIEDERSCHRIFTEN,
                                         vermessungsriss.getProperty("grenzniederschriften").toString());
                             }
                         } catch (final Exception ex) {
                             LOG.warn("Could not include 'grenzniederschrift' for vermessungsriss '"
                                         + vermessungsriss.toJSONString() + "'.",
                                 ex);
                         }
 
                         final StringBuilder description = new StringBuilder("des Vermessungsrisses ");
                         description.append(vermessungsriss.getProperty("schluessel"));
                         description.append(" - ");
                         description.append(vermessungsriss.getProperty("gemarkung.name"));
                         description.append(" - ");
                         description.append(vermessungsriss.getProperty("flur"));
                         description.append(" - ");
                         description.append(vermessungsriss.getProperty("blatt"));
                         description.append(" - Seite ");
 
                         if (bilder != null) {
                             for (int i = 0; i < bilder.length; i++) {
                                 imageBeans.add(new VermessungRissImageReportBean(
                                         "Bild "
                                                 + description.toString()
                                                 + (i + 1),
                                         bilder[i]));
                             }
                         }
                         if (grenzniederschriften != null) {
                             for (int i = 0; i < grenzniederschriften.length; i++) {
                                 imageBeans.add(new VermessungRissImageReportBean(
                                         "Grenzniederschrift "
                                                 + description.toString()
                                                 + (i + 1),
                                         grenzniederschriften[i]));
                             }
                         }
 
                         bilder = null;
                         grenzniederschriften = null;
                     }
 
                     reportBeans.add(new VermessungRissReportBean(selectedVermessungsrisse, imageBeans));
                     final JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportBeans);
 
                     final HashMap parameters = new HashMap();
                     parameters.put(PARAMETER_JOBNUMBER, txtJobnumber.getText());
                     parameters.put(PARAMETER_PROJECTNAME, txtProjectname.getText());
 
                     final JasperReport jasperReport;
                     final JasperPrint jasperPrint;
                     try {
                         jasperReport = (JasperReport)JRLoader.loadObject(getClass().getResourceAsStream(
                                     "/de/cismet/cids/custom/wunda_blau/res/vermessungsrisse.jasper"));
                         jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                     } catch (JRException ex) {
                         LOG.error("Could not generate report for measurement sketches.", ex);
 
                         final ErrorInfo ei = new ErrorInfo(NbBundle.getMessage(
                                     VermessungRissAggregationRenderer.class,
                                     "VermessungRissAggregationRenderer.btnGenerateReportActionPerformed(ActionEvent).ErrorInfo.title"),   // NOI18N
                                 NbBundle.getMessage(
                                     VermessungRissAggregationRenderer.class,
                                     "VermessungRissAggregationRenderer.btnGenerateReportActionPerformed(ActionEvent).ErrorInfo.message"), // NOI18N
                                 null,
                                 null,
                                 ex,
                                 Level.ALL,
                                 null);
                         JXErrorPane.showDialog(VermessungRissAggregationRenderer.this, ei);
 
                         return;
                     }
 
                     if (DownloadManagerDialog.showAskingForUserTitle(
                                     StaticSwingTools.getParentFrame(VermessungRissAggregationRenderer.this))) {
                         String projectname = txtProjectname.getText();
                         if ((projectname == null) || (projectname.trim().length() == 0)) {
                             projectname = "Vermessungsrisse";
                         }
                         final String jobname = DownloadManagerDialog.getJobname();
 
                         DownloadManager.instance()
                                 .add(new JasperDownload(jasperPrint, jobname, projectname, "vermriss"));
                     }
                 }
             };
 
         CismetThreadPool.execute(runnable);
     } //GEN-LAST:event_btnGenerateReportActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formAncestorAdded(final javax.swing.event.AncestorEvent evt) { //GEN-FIRST:event_formAncestorAdded
         CismetThreadPool.execute(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException ex) {
                         LOG.warn("Sleeping to wait for zooming to added features was interrupted.", ex);
                     }
                     EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 animateToOverview();
                             }
                         });
                 }
             });
     } //GEN-LAST:event_formAncestorAdded
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected Collection<CidsBean> getSelectedVermessungsrisse() {
         final Collection<CidsBean> result = new LinkedList<CidsBean>();
         final List<Integer> selectedIndexes = new ArrayList<Integer>();
 
         final TableModel tableModel = tblRisse.getModel();
         for (int i = 0; i < tableModel.getRowCount(); ++i) {
             final Object includedObj = tableModel.getValueAt(i, 0);
             if ((includedObj instanceof Boolean) && (Boolean)includedObj) {
                 selectedIndexes.add(Integer.valueOf(i));
             }
         }
 
         Collections.sort(selectedIndexes, tableComparator);
 
         for (final Integer selectedIndex : selectedIndexes) {
             result.add(cidsBeans.get(selectedIndex));
         }
 
         return result;
     }
 
     @Override
     public Collection<CidsBean> getCidsBeans() {
         return cidsBeans;
     }
 
     @Override
     public void setCidsBeans(final Collection<CidsBean> beans) {
         if (beans instanceof List) {
             this.cidsBeans = (List<CidsBean>)beans;
             features = new HashMap<CidsBean, CidsFeature>(beans.size());
 
             initMap();
 
             final List<Object[]> tableData = new ArrayList<Object[]>();
             for (final CidsBean punktBean : cidsBeans) {
                 tableData.add(cidsBean2Row(punktBean));
             }
 
             tableModel = new PointTableModel(tableData.toArray(new Object[tableData.size()][]), AGR_COMLUMN_NAMES);
             tblRisse.setModel(tableModel);
 
             final TableColumnModel cModel = tblRisse.getColumnModel();
             for (int i = 0; i < cModel.getColumnCount(); ++i) {
                 cModel.getColumn(i).setPreferredWidth(AGR_COMLUMN_WIDTH[i]);
             }
 
             final TableRowSorter tableSorter = ObjectRendererUtils.decorateTableWithSorter(tblRisse);
             final List<RowSorter.SortKey> sortKeys = new LinkedList<RowSorter.SortKey>();
             sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
             sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
             sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
             sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
             tableSorter.setSortKeys(sortKeys);
         }
         setTitle(null);
     }
 
     @Override
     public void dispose() {
         mappingComponent.dispose();
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public void setTitle(final String title) {
         String desc = "Vermessungsrisse";
         final Collection<CidsBean> beans = cidsBeans;
         if ((beans != null) && (beans.size() > 0)) {
             desc += " - " + beans.size() + " Vermessungsrisse ausgewählt";
         }
         this.title = desc;
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void initMap() {
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
             swms.setName("Vermessung_Riss");
             mappingModel.addLayer(swms);
             mappingComponent.setMappingModel(mappingModel);
             mappingComponent.setAnimationDuration(0);
             mappingComponent.gotoInitialBoundingBox();
             mappingComponent.setInteractionMode(MappingComponent.ZOOM);
             mappingComponent.unlock();
 
             for (final CidsBean cidsBean : cidsBeans) {
                 final CidsFeature feature = new CidsFeature(cidsBean.getMetaObject());
                 features.put(cidsBean, feature);
             }
             mappingComponent.getFeatureCollection().addFeatures(features.values());
             mappingComponent.setAnimationDuration(500);
         } catch (Exception e) {
             LOG.fatal(e, e);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   vermessungsrisse  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected XBoundingBox boundingBoxFromPointList(final Collection<CidsBean> vermessungsrisse) {
         final List<Geometry> geometries = new LinkedList<Geometry>();
 
         for (final CidsBean vermessungsriss : vermessungsrisse) {
             try {
                 if (vermessungsriss.getProperty("geometrie.geo_field") instanceof Geometry) {
                     geometries.add((Geometry)vermessungsriss.getProperty("geometrie.geo_field"));
                 }
             } catch (Exception ex) {
                 LOG.warn("Could not add geometry to create a bounding box.", ex);
             }
         }
 
         final GeometryCollection geoCollection = new GeometryCollection(geometries.toArray(
                     new Geometry[geometries.size()]),
                 new GeometryFactory());
 
         return new XBoundingBox(geoCollection.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
     }
 
     /**
      * Extracts the date from a CidsBean into an Object[] -> table row. (Collection attributes are flatened to
      * comaseparated lists)
      *
      * @param   vermessungsriss  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected Object[] cidsBean2Row(final CidsBean vermessungsriss) {
         if (vermessungsriss != null) {
             final Object[] result = new Object[AGR_COMLUMN_NAMES.length];
             result[0] = Boolean.TRUE;
 
             for (int i = 0; i < AGR_PROPERTY_NAMES.length; ++i) {
                 final Object property = vermessungsriss.getProperty(AGR_PROPERTY_NAMES[i]);
                 final String propertyString;
                 propertyString = ObjectRendererUtils.propertyPrettyPrint(property);
                 result[i + 1] = propertyString;
             }
             return result;
         }
 
         return new Object[0];
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         try {
             final CidsBean[] vermessungsrisse = DevelopmentTools.createCidsBeansFromRMIConnectionOnLocalhost(
                     "WUNDA_BLAU",
                     "Administratoren",
                     "admin",
                     "sb",
                     "vermessung_riss",
                     5);
 //            final CidsBean[] vermessungsrisse = new CidsBean[] {
 //                    DevelopmentTools.createCidsBeanFromRMIConnectionOnLocalhost(
 //                        "WUNDA_BLAU",
 //                        "Administratoren",
 //                        "admin",
 //                        "sb",
 //                        "vermessung_riss",
 //                        // 6818)
 //                        4),
 //                };
 
             DevelopmentTools.createAggregationRendererInFrameFromRMIConnectionOnLocalhost(
                 Arrays.asList(vermessungsrisse),
                 "Aggregationsrenderer",
                 1024,
                 768);
 
 //            final Collection<VermessungRissReportBean> reportBeans = new LinkedList<VermessungRissReportBean>();
 //            reportBeans.add(new VermessungRissReportBean((Arrays.asList(vermessungsrisse))));
 //            DevelopmentTools.showReportForBeans(
 //                "/de/cismet/cids/custom/wunda_blau/res/vermessungsrisse.jasper",
 //                reportBeans);
 //            DevelopmentTools.showReportForCidsBeans(
 //                "/de/cismet/cids/custom/wunda_blau/res/vermessungsriss.jasper",
 //                vermessungsrisse);
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class TableSelectionListener implements ListSelectionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void valueChanged(final ListSelectionEvent e) {
             if (e.getValueIsAdjusting() || (cidsBeans == null)) {
                 return;
             }
 
             CidsFeature featureToSelect = null;
 
             final int[] indexes = tblRisse.getSelectedRows();
             if ((indexes != null) && (indexes.length > 0)) {
                 for (final int viewIdx : indexes) {
                     final int modelIdx = tblRisse.getRowSorter().convertRowIndexToModel(viewIdx);
                     if ((modelIdx > -1) && (modelIdx < cidsBeans.size())) {
                         final CidsBean selectedBean = cidsBeans.get(modelIdx);
                         featureToSelect = features.get(selectedBean);
                         break;
                     }
                 }
             }
 
             if ((featureToSelect != null) && (featureToSelect.getGeometry() != null)) {
                 final XBoundingBox boxToGoto = new XBoundingBox(featureToSelect.getGeometry().getEnvelope().buffer(
                             AlkisConstants.COMMONS.GEO_BUFFER));
                 boxToGoto.setX1(boxToGoto.getX1()
                             - (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth()));
                 boxToGoto.setX2(boxToGoto.getX2()
                             + (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth()));
                 boxToGoto.setY1(boxToGoto.getY1()
                             - (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight()));
                 boxToGoto.setY2(boxToGoto.getY2()
                             + (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight()));
                 mappingComponent.getFeatureCollection().unselectAll();
                 mappingComponent.getFeatureCollection().select(featureToSelect);
                 mappingComponent.gotoBoundingBox(boxToGoto, false, true, 500);
             } else {
                 mappingComponent.getFeatureCollection().unselectAll();
                 mappingComponent.gotoInitialBoundingBox();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class PointTableModel extends DefaultTableModel {
 
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
 
         @Override
         public boolean isCellEditable(final int row, final int column) {
             return column == 0;
         }
 
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
     public static class VermessungRissReportBean {
 
         //~ Instance fields ----------------------------------------------------
 
         private Collection<CidsBean> vermessungsrisse;
         private Collection<VermessungRissImageReportBean> images;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new VermessungRissReportBean object.
          *
          * @param  vermessungsrisse  DOCUMENT ME!
          * @param  images            DOCUMENT ME!
          */
         public VermessungRissReportBean(final Collection<CidsBean> vermessungsrisse,
                 final Collection<VermessungRissImageReportBean> images) {
             this.vermessungsrisse = vermessungsrisse;
             this.images = images;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Collection<CidsBean> getVermessungsrisse() {
             return vermessungsrisse;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Collection<VermessungRissImageReportBean> getImages() {
             return images;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public static class VermessungRissImageReportBean {
 
         //~ Instance fields ----------------------------------------------------
 
         private String description;
         private Image image;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new VermessungRissImageReportBean object.
          *
          * @param  description  DOCUMENT ME!
          * @param  image        DOCUMENT ME!
          */
         public VermessungRissImageReportBean(final String description, final Image image) {
             this.description = description;
             this.image = image;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getDescription() {
             return description;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Image getImage() {
             return image;
         }
     }
 }
