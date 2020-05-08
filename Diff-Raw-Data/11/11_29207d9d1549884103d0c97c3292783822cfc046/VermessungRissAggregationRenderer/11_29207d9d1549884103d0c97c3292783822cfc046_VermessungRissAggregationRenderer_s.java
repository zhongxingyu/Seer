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
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 import java.awt.EventQueue;
 import java.awt.geom.Rectangle2D;
 
 import java.net.URL;
 
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
 
 import javax.swing.DefaultComboBoxModel;
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
 
 import de.cismet.cids.custom.objecteditors.wunda_blau.VermessungRissEditor;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.PrintingWaitDialog;
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
 
 import de.cismet.tools.gui.MultiPagePictureReader;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.Download;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.HttpDownload;
 import de.cismet.tools.gui.downloadmanager.MultipleDownload;
 
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
     private static final String PARAMETER_TYPE = "TYPE";
     private static final String PARAMETER_STARTINGPAGES = "STARTINGPAGES";
     private static final String PARAMETER_IMAGEAVAILABLE = "IMAGEAVAILABLE";
     private static final String TYPE_VERMESSUNGSRISSE = "Vermessungsrisse";
     private static final String TYPE_COMPLEMENTARYDOCUMENTS = "Ergänzende Dokumente";
 
     // Spaltenueberschriften
     private static final String[] AGR_COMLUMN_NAMES = new String[] {
             "Auswahl",
             "Schlüssel",
             "Gemarkung",
             "Flur",
             "Blatt",
             "Jahr"
         };
     // Namen der Properties -> Spalten
     private static final String[] AGR_PROPERTY_NAMES = new String[] {
             "schluessel",
             "gemarkung.name",
             "flur",
             "blatt",
             "jahr"
         };
 
     private static final int[] AGR_COMLUMN_WIDTH = new int[] { 40, 85, 125, 85, 85, 60 };
 
     //~ Instance fields --------------------------------------------------------
 
     private List<CidsBean> cidsBeans;
     private String title = "";
     private PointTableModel tableModel;
     private Map<CidsBean, CidsFeature> features;
     private Comparator<Integer> tableComparator;
     private PrintingWaitDialog printingWaitDialog;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnGenerateReport;
     private javax.swing.JComboBox cmbType;
     private javax.swing.Box.Filler flrGap;
     private javax.swing.JLabel lblJobnumber;
     private javax.swing.JLabel lblProjectname;
     private javax.swing.JLabel lblType;
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
 
         printingWaitDialog = new PrintingWaitDialog(StaticSwingTools.getParentFrame(this), true);
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
         cmbType = new javax.swing.JComboBox();
         lblType = new javax.swing.JLabel();
 
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
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         pnlReport.add(btnGenerateReport, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
         pnlReport.add(cmbType, gridBagConstraints);
 
         lblType.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissAggregationRenderer.class,
                 "VermessungRissAggregationRenderer.lblType.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
         pnlReport.add(lblType, gridBagConstraints);
 
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
 
         final Object typeObj = cmbType.getSelectedItem();
         final String type;
         if (typeObj instanceof String) {
             type = (String)typeObj;
 
             if (type.equalsIgnoreCase(TYPE_VERMESSUNGSRISSE)) {
                 downloadProducts(selectedVermessungsrisse, type, AlkisConstants.COMMONS.VERMESSUNG_HOST_BILDER);
             } else if (type.equalsIgnoreCase(TYPE_COMPLEMENTARYDOCUMENTS)) {
                 downloadProducts(
                     selectedVermessungsrisse,
                     type,
                     AlkisConstants.COMMONS.VERMESSUNG_HOST_GRENZNIEDERSCHRIFTEN);
             }
         } else {
             // TODO: User feedback?!
             LOG.info("Unknown type '" + typeObj + "' encountered. Skipping report generation.");
             return;
         }
     } //GEN-LAST:event_btnGenerateReportActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  selectedVermessungsrisse  DOCUMENT ME!
      * @param  type                      DOCUMENT ME!
      * @param  host                      DOCUMENT ME!
      */
     private void downloadProducts(final Collection<CidsBean> selectedVermessungsrisse,
             final String type,
             final String host) {
         final Runnable runnable = new Runnable() {
 
                 @Override
                 public void run() {
                     EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 StaticSwingTools.showDialog(
                                     StaticSwingTools.getParentFrame(VermessungRissAggregationRenderer.this),
                                     printingWaitDialog,
                                     true);
                             }
                         });
 
                     final Collection<VermessungRissReportBean> reportBeans = new LinkedList<VermessungRissReportBean>();
                     final Collection<VermessungRissImageReportBean> imageBeans =
                         new LinkedList<VermessungRissImageReportBean>();
                     final Collection<URL> additionalFilesToDownload = new LinkedList<URL>();
 
                     // Not the most elegant way, but it works. We have to calculate on which page an image will appear.
                     // This can't be easily done with JasperReports. In order to let JasperReports calculate which page
                     // an image appears on, we have to know how many pages the overview will take. And that is not
                     // possible in JasperReports itself. Whether we evaluate the page calculation "Now" - which means at
                     // the time one row is written -: Then we only get the current page count, not the future page
                     // count. Or we evaluate the page calculation "Report", that means after the rest of the reportwas
                    // created: Then the page count has a fix value for every row. The first page can contain 26 rows,
                    // the following pages are able to hold 36 rows. The first image will appear on page 2 if there are
                    // less than 26 rows to write.
                     final Map startingPages = new HashMap();
                     int startingPage = 2;
                    if (selectedVermessungsrisse.size() > 26) {
                        startingPage += Math.ceil((selectedVermessungsrisse.size() - 26D) / 36D);
                     }
 
                     final Map imageAvailable = new HashMap();
 
                     for (final CidsBean vermessungsriss : selectedVermessungsrisse) {
                         final String schluessel;
                         final Integer gemarkung;
                         final String flur;
                         final String blatt;
 
                         try {
                             schluessel = vermessungsriss.getProperty("schluessel").toString();
                             gemarkung = (Integer)vermessungsriss.getProperty("gemarkung.id");
                             flur = vermessungsriss.getProperty("flur").toString();
                             blatt = vermessungsriss.getProperty("blatt").toString();
                         } catch (final Exception ex) {
                             // TODO: User feedback?
                             LOG.warn("Could not include raster document for vermessungsriss '"
                                         + vermessungsriss.toJSONString()
                                         + "'.",
                                 ex);
                             continue;
                         }
 
                         final StringBuilder description;
                         if (TYPE_VERMESSUNGSRISSE.equalsIgnoreCase(type)) {
                             description = new StringBuilder("Vermessungsriss ");
                         } else {
                             description = new StringBuilder("Ergänzende Dokumente zum Vermessungsriss ");
                         }
                         description.append(vermessungsriss.getProperty("schluessel"));
                         description.append(" - ");
                         description.append(vermessungsriss.getProperty("gemarkung.name"));
                         description.append(" - ");
                         description.append(vermessungsriss.getProperty("flur"));
                         description.append(" - ");
                         description.append(vermessungsriss.getProperty("blatt"));
                         description.append(" - Seite ");
 
                         final Map<URL, URL> validURLs = VermessungRissEditor.getCorrespondingURLs(
                                 host,
                                 gemarkung,
                                 flur,
                                 schluessel,
                                 blatt);
 
                         MultiPagePictureReader reader = null;
                         int pageCount = 0;
                         final StringBuilder fileReference = new StringBuilder();
                         for (final Map.Entry<URL, URL> urls : validURLs.entrySet()) {
                             try {
                                 reader = new MultiPagePictureReader(urls.getValue(), false, false);
                                 pageCount = reader.getNumberOfPages();
                                 additionalFilesToDownload.add(urls.getKey());
 
                                 String path = urls.getKey().getPath();
                                 path = path.substring(path.lastIndexOf('/') + 1);
                                 fileReference.append(" (");
                                 fileReference.append(path);
                                 fileReference.append(')');
                                 break;
                             } catch (final Exception ex) {
                                 LOG.warn("Could not read document from URL '" + urls.getValue().toExternalForm()
                                             + "'. Skipping this url.",
                                     ex);
                             }
                         }
 
                         boolean isOfReducedSize = true;
                         if (reader == null) {
                             // Didn't find an image of reduced size
                             for (final Map.Entry<URL, URL> urls : validURLs.entrySet()) {
                                 try {
                                     reader = new MultiPagePictureReader(urls.getKey(), false, false);
                                     pageCount = reader.getNumberOfPages();
                                     isOfReducedSize = false;
                                     break;
                                 } catch (final Exception ex) {
                                     LOG.warn("Could not read document from URL '" + urls.getValue().toExternalForm()
                                                 + "'. Skipping this url.",
                                         ex);
                                 }
                             }
                         }
 
                         imageAvailable.put(vermessungsriss.getProperty("id"), Boolean.valueOf(reader != null));
 
                         if (reader == null) {
                             // Couldn't open any image.
                             continue;
                         }
 
                         for (int i = 0; i < pageCount; i++) {
                             imageBeans.add(new VermessungRissImageReportBean(
                                     description.toString()
                                             + (i + 1)
                                             + fileReference.toString(),
                                     host,
                                     schluessel,
                                     gemarkung,
                                     flur,
                                     blatt,
                                     i,
                                     reader));
                         }
 
                         String startingPageString = Integer.toString(startingPage);
                         if (isOfReducedSize) {
                             startingPageString = startingPageString.concat("*");
                         }
 
                         startingPages.put(vermessungsriss.getProperty("id"), startingPageString);
                         startingPage += pageCount;
                     }
 
                     reportBeans.add(new VermessungRissReportBean(selectedVermessungsrisse, imageBeans));
                     final JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportBeans);
 
                     final HashMap parameters = new HashMap();
                     parameters.put(PARAMETER_JOBNUMBER, txtJobnumber.getText());
                     parameters.put(PARAMETER_PROJECTNAME, txtProjectname.getText());
                     parameters.put(PARAMETER_TYPE, type);
                     parameters.put(PARAMETER_STARTINGPAGES, startingPages);
                     parameters.put(PARAMETER_IMAGEAVAILABLE, imageAvailable);
 
                     final JasperReport jasperReport;
                     try {
                         jasperReport = (JasperReport)JRLoader.loadObject(getClass().getResourceAsStream(
                                     "/de/cismet/cids/custom/wunda_blau/res/vermessungsrisse.jasper"));
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
                     } finally {
                         EventQueue.invokeLater(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     printingWaitDialog.setVisible(false);
                                 }
                             });
                     }
 
                     if (DownloadManagerDialog.showAskingForUserTitle(VermessungRissAggregationRenderer.this)) {
                         String projectname = txtProjectname.getText();
                         if ((projectname == null) || (projectname.trim().length() == 0)) {
                             projectname = type;
                         }
                         final String jobname = DownloadManagerDialog.getJobname();
                         final JasperDownload jasperDownload = new JasperDownload(
                                 jasperReport,
                                 parameters,
                                 dataSource,
                                 jobname,
                                 projectname,
                                 "vermriss");
 
                         if (additionalFilesToDownload.isEmpty()) {
                             DownloadManager.instance().add(jasperDownload);
                         } else {
                             final Collection<Download> downloads = new LinkedList<Download>();
                             downloads.add(jasperDownload);
 
                             for (final URL additionalFileToDownload : additionalFilesToDownload) {
                                 final String file = additionalFileToDownload.getFile()
                                             .substring(additionalFileToDownload.getFile().lastIndexOf('/') + 1);
                                 final String filename = file.substring(0, file.lastIndexOf('.'));
                                 final String extension = file.substring(file.lastIndexOf('.'));
 
                                 downloads.add(new HttpDownload(
                                         additionalFileToDownload,
                                         null,
                                         jobname,
                                         file,
                                         filename,
                                         extension));
                             }
 
                             DownloadManager.instance().add(new MultipleDownload(downloads, projectname));
                         }
                     }
                 }
             };
 
         CismetThreadPool.execute(runnable);
     }
 
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
      * @param  beans  DOCUMENT ME!
      */
     @Override
     public void setCidsBeans(final Collection<CidsBean> beans) {
         if (beans instanceof List) {
             this.cidsBeans = (List<CidsBean>)beans;
             features = new HashMap<CidsBean, CidsFeature>(beans.size());
 
             initMap();
 
             boolean allowVermessungsrisseReport = false;
             boolean allowErgaenzendeDokumenteReport = false;
 
             final List<Object[]> tableData = new ArrayList<Object[]>();
             for (final CidsBean vermessungsrissBean : cidsBeans) {
                 tableData.add(cidsBean2Row(vermessungsrissBean));
 
                 if (!allowVermessungsrisseReport) {
                     allowVermessungsrisseReport = hasVermessungsriss(vermessungsrissBean);
                 }
                 if (!allowErgaenzendeDokumenteReport) {
                     allowErgaenzendeDokumenteReport = hasErgaenzendeDokumente(vermessungsrissBean);
                 }
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
 
             if (allowErgaenzendeDokumenteReport && allowVermessungsrisseReport) {
                 cmbType.setModel(new DefaultComboBoxModel(
                         new String[] { TYPE_VERMESSUNGSRISSE, TYPE_COMPLEMENTARYDOCUMENTS }));
             } else if (allowErgaenzendeDokumenteReport) {
                 cmbType.setModel(new DefaultComboBoxModel(new String[] { TYPE_COMPLEMENTARYDOCUMENTS }));
             } else if (allowVermessungsrisseReport) {
                 cmbType.setModel(new DefaultComboBoxModel(new String[] { TYPE_VERMESSUNGSRISSE }));
             } else {
                 cmbType.setEnabled(false);
                 btnGenerateReport.setEnabled(false);
                 txtJobnumber.setEnabled(false);
                 txtProjectname.setEnabled(false);
             }
         }
 
         setTitle(null);
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
      * @param   cidsBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static boolean hasVermessungsriss(final CidsBean cidsBean) {
         try {
             return VermessungRissReportScriptlet.isImageAvailable(
                     AlkisConstants.COMMONS.VERMESSUNG_HOST_BILDER,
                     (String)cidsBean.getProperty("schluessel"),
                     (Integer)cidsBean.getProperty("gemarkung.id"),
                     (String)cidsBean.getProperty("flur"),
                     (String)cidsBean.getProperty("blatt"));
         } catch (final Exception ex) {
             LOG.info("Could not determine if CidsBean has measurement sketches.", ex);
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   cidsBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static boolean hasErgaenzendeDokumente(final CidsBean cidsBean) {
         try {
             return VermessungRissReportScriptlet.isImageAvailable(
                     AlkisConstants.COMMONS.VERMESSUNG_HOST_GRENZNIEDERSCHRIFTEN,
                     (String)cidsBean.getProperty("schluessel"),
                     (Integer)cidsBean.getProperty("gemarkung.id"),
                     (String)cidsBean.getProperty("flur"),
                     (String)cidsBean.getProperty("blatt"));
         } catch (final Exception ex) {
             LOG.info("Could not determine if CidsBean has measurement sketches.", ex);
             return false;
         }
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
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
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
         private String host;
         private String schluessel;
         private Integer gemarkung;
         private String flur;
         private String blatt;
         private Integer page;
         private MultiPagePictureReader reader;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new VermessungRissImageReportBean object.
          *
          * @param  description  DOCUMENT ME!
          * @param  host         DOCUMENT ME!
          * @param  schluessel   DOCUMENT ME!
          * @param  gemarkung    DOCUMENT ME!
          * @param  flur         DOCUMENT ME!
          * @param  blatt        DOCUMENT ME!
          * @param  page         DOCUMENT ME!
          * @param  reader       DOCUMENT ME!
          */
         public VermessungRissImageReportBean(final String description,
                 final String host,
                 final String schluessel,
                 final Integer gemarkung,
                 final String flur,
                 final String blatt,
                 final Integer page,
                 final MultiPagePictureReader reader) {
             this.description = description;
             this.host = host;
             this.schluessel = schluessel;
             this.gemarkung = gemarkung;
             this.flur = flur;
             this.blatt = blatt;
             this.page = page;
             this.reader = reader;
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
         public String getBlatt() {
             return blatt;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getFlur() {
             return flur;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Integer getGemarkung() {
             return gemarkung;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getHost() {
             return host;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Integer getPage() {
             return page;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getSchluessel() {
             return schluessel;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public MultiPagePictureReader getReader() {
             return reader;
         }
     }
 }
