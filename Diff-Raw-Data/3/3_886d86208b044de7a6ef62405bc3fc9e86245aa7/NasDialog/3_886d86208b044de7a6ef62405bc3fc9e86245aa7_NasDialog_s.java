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
 package de.cismet.cids.custom.nas;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Polygon;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 
 import java.text.DecimalFormat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JFrame;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 import de.cismet.cids.custom.objectrenderer.utils.billing.BillingPopup;
 import de.cismet.cids.custom.objectrenderer.utils.billing.ProductGroupAmount;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.utils.nas.NasProductTemplate;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollection;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.features.XStyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class NasDialog extends javax.swing.JDialog implements ChangeListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger log = Logger.getLogger(NasDialog.class);
     private static final double TOTAL_MAP_BUFFER = 50d;
 
     //~ Instance fields --------------------------------------------------------
 
     GeomWrapper totalMapWrapper;
     private MappingComponent map;
     private LinkedList<GeomWrapper> geomWrappers;
     private NasTableModel tableModel;
     private GeomWrapper selectedGeomWrapper;
     private HashMap<GeomWrapper, Feature> bufferFeatures = new HashMap<GeomWrapper, Feature>();
     private NasFeePreviewPanel feePreview = new NasFeePreviewPanel();
     private ArrayList<NasProductTemplate> productTemplates = new ArrayList<NasProductTemplate>();
     private DecimalFormat formatter = new DecimalFormat("#,###,##0.00 \u00A4\u00A4");
     private boolean firstBufferCall = true;
     private boolean isInitialized = false;
     private int pointAmount = 0;
     private int gebaeudeAmount = 0;
     private int flurstueckAmount = 0;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnOk;
     private javax.swing.JComboBox cbType;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSlider jsGeomBuffer;
     private javax.swing.JLabel lblAuftragsnummer;
     private org.jdesktop.swingx.JXBusyLabel lblBusy;
     private javax.swing.JLabel lblError;
     private javax.swing.JLabel lblGeomBuffer;
     private javax.swing.JLabel lblType;
     private javax.swing.JPanel pnlControls;
     private javax.swing.JPanel pnlFee;
     private javax.swing.JPanel pnlMap;
     private javax.swing.JPanel pnlSettings;
     private javax.swing.JTable tblGeom;
     private javax.swing.JTextField tfAuftragsnummer;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form NasDialog.
      *
      * @param  parent  DOCUMENT ME!
      * @param  modal   DOCUMENT ME!
      */
     public NasDialog(final java.awt.Frame parent, final boolean modal) {
         super(parent, modal);
         productTemplates.add(NasProductTemplate.POINTS);
         productTemplates.add(NasProductTemplate.OHNE_EIGENTUEMER);
         productTemplates.add(NasProductTemplate.KOMPLETT);
         geomWrappers = new LinkedList<GeomWrapper>();
         map = CismapBroker.getInstance().getMappingComponent();
         final FeatureCollection fc = map.getFeatureCollection();
         for (final Feature f : fc.getAllFeatures()) {
             String name = "";
             if (f instanceof XStyledFeature) {
                 name += ((XStyledFeature)f).getType() + " " + ((XStyledFeature)f).getName();
             } else {
                 name += f;
             }
             if ((f.getGeometry() instanceof Polygon) || (f.getGeometry() instanceof MultiPolygon)) {
                 final PFeature pf = new PFeature(f, map);
                 if (!pf.hasHole()) {
                     geomWrappers.add(new GeomWrapper(f.getGeometry(), name, true));
                 }
             }
         }
         tableModel = new NasTableModel();
         initComponents();
         tblGeom.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final ListSelectionEvent e) {
                     final ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 
                     if (lsm.isSelectionEmpty()) {
                         selectedGeomWrapper = null;
                     } else {
                         selectedGeomWrapper = tableModel.get(lsm.getLeadSelectionIndex());
                     }
 
                     changeMap();
                 }
             });
         tblGeom.getColumnModel().getColumn(0).setPreferredWidth(80);
         tblGeom.getColumnModel().getColumn(0).setMaxWidth(80);
         jsGeomBuffer.addChangeListener(this);
         map = new MappingComponent();
         initMap();
         pnlMap.setLayout(new BorderLayout());
         pnlMap.add(map, BorderLayout.CENTER);
         cbType.setSelectedItem(NasProductTemplate.OHNE_EIGENTUEMER);
         calculateFee();
         isInitialized = true;
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
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         lblError = new javax.swing.JLabel();
         pnlMap = new javax.swing.JPanel();
         pnlSettings = new javax.swing.JPanel();
         jsGeomBuffer = new javax.swing.JSlider();
         lblType = new javax.swing.JLabel();
         cbType = new javax.swing.JComboBox();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblGeom = new javax.swing.JTable();
         jSeparator1 = new javax.swing.JSeparator();
         lblAuftragsnummer = new javax.swing.JLabel();
         tfAuftragsnummer = new javax.swing.JTextField();
         pnlFee = new javax.swing.JPanel();
         lblBusy = new org.jdesktop.swingx.JXBusyLabel(new Dimension(75, 75));
         jPanel2 = new javax.swing.JPanel();
         lblGeomBuffer = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         pnlControls = new javax.swing.JPanel();
         btnOk = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblError,
             org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.lblError.text")); // NOI18N
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.title")); // NOI18N
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         pnlMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         final javax.swing.GroupLayout pnlMapLayout = new javax.swing.GroupLayout(pnlMap);
         pnlMap.setLayout(pnlMapLayout);
         pnlMapLayout.setHorizontalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 336,
                 Short.MAX_VALUE));
         pnlMapLayout.setVerticalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 413,
                 Short.MAX_VALUE));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlMap, gridBagConstraints);
 
         pnlSettings.setMinimumSize(new java.awt.Dimension(400, 100));
         pnlSettings.setPreferredSize(new java.awt.Dimension(400, 300));
         pnlSettings.setLayout(new java.awt.GridBagLayout());
 
         jsGeomBuffer.setMajorTickSpacing(50);
         jsGeomBuffer.setMaximum(50);
         jsGeomBuffer.setMinimum(-50);
         jsGeomBuffer.setMinorTickSpacing(1);
         jsGeomBuffer.setPaintLabels(true);
         jsGeomBuffer.setSnapToTicks(true);
         jsGeomBuffer.setValue(0);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
         pnlSettings.add(jsGeomBuffer, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblType,
             org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.lblType.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
         pnlSettings.add(lblType, gridBagConstraints);
 
         final org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create(
                 "${productTemplates}");
         final org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJComboBoxBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         cbType);
         bindingGroup.addBinding(jComboBoxBinding);
 
         cbType.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbTypeActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
         pnlSettings.add(cbType, gridBagConstraints);
 
         final javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 2;
         pnlSettings.add(jPanel1, gridBagConstraints);
 
         jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 100));
         jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));
 
         tblGeom.setModel(tableModel);
         tblGeom.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     tblGeomFocusLost(evt);
                 }
             });
         jScrollPane1.setViewportView(tblGeom);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
         pnlSettings.add(jScrollPane1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
         pnlSettings.add(jSeparator1, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblAuftragsnummer,
             org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.lblAuftragsnummer.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
         pnlSettings.add(lblAuftragsnummer, gridBagConstraints);
 
         tfAuftragsnummer.setText(org.openide.util.NbBundle.getMessage(
                 NasDialog.class,
                 "NasDialog.tfAuftragsnummer.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
         pnlSettings.add(tfAuftragsnummer, gridBagConstraints);
 
         pnlFee.setBackground(new java.awt.Color(254, 254, 254));
         pnlFee.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnlFee.setLayout(new java.awt.BorderLayout());
 
         lblBusy.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblBusy.setMaximumSize(new java.awt.Dimension(140, 40));
         lblBusy.setMinimumSize(new java.awt.Dimension(140, 60));
         lblBusy.setPreferredSize(new java.awt.Dimension(140, 60));
         pnlFee.add(lblBusy, java.awt.BorderLayout.CENTER);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipady = 1;
         gridBagConstraints.weighty = 1.0;
         pnlSettings.add(pnlFee, gridBagConstraints);
 
         jPanel2.setMinimumSize(new java.awt.Dimension(160, 27));
         jPanel2.setPreferredSize(new java.awt.Dimension(160, 27));
         jPanel2.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblGeomBuffer,
             org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.lblGeomBuffer.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
         jPanel2.add(lblGeomBuffer, gridBagConstraints);
 
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel1.setMinimumSize(new java.awt.Dimension(25, 17));
         jLabel1.setPreferredSize(new java.awt.Dimension(25, 17));
 
         final org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 jsGeomBuffer,
                 org.jdesktop.beansbinding.ELProperty.create("${value}"),
                 jLabel1,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
         jPanel2.add(jLabel1, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel2,
             org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.jLabel2.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
         jPanel2.add(jLabel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         pnlSettings.add(jPanel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlSettings, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnOk,
             org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.btnOk.text")); // NOI18N
         btnOk.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnOkActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         pnlControls.add(btnOk, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnCancel,
             org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.btnCancel.text")); // NOI18N
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCancelActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         pnlControls.add(btnCancel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
         getContentPane().add(pnlControls, gridBagConstraints);
 
         bindingGroup.bind();
 
         pack();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
         dispose();
     }                                                                             //GEN-LAST:event_btnCancelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void tblGeomFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_tblGeomFocusLost
         map.gotoInitialBoundingBox();
         tblGeom.clearSelection();
     }                                                                    //GEN-LAST:event_tblGeomFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbTypeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbTypeActionPerformed
         if (isInitialized) {
             calculateFee();
         }
     }                                                                          //GEN-LAST:event_cbTypeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOkActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         final NasProductTemplate template = (NasProductTemplate)cbType.getSelectedItem();
                         final String requestId = tfAuftragsnummer.getText().trim();
                         final ArrayList<ProductGroupAmount> list = getProductGroupAmounts(template);
                         final ProductGroupAmount[] goupAmounts = list.toArray(new ProductGroupAmount[list.size()]);
                         if (BillingPopup.doBilling(
                                         template.getBillingKey(),
                                         "request",
                                         requestId,
                                         null,
                                         goupAmounts)) {
                             doDownload(requestId, template);
                             dispose();
                         }
                     } catch (Exception ex) {
                         Exceptions.printStackTrace(ex);
                     }
                 }
             });
     } //GEN-LAST:event_btnOkActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param   template  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ArrayList<ProductGroupAmount> getProductGroupAmounts(final NasProductTemplate template) {
         final ArrayList<ProductGroupAmount> result = new ArrayList<ProductGroupAmount>();
         if (template == NasProductTemplate.POINTS) {
             result.addAll(getProductGroupAmountForObject("eapkt", pointAmount));
         } else if (template == NasProductTemplate.OHNE_EIGENTUEMER) {
             result.addAll(getProductGroupAmountForObject("eageb", gebaeudeAmount));
             result.addAll(getProductGroupAmountForObject("eaflst", flurstueckAmount));
         } else if (template == NasProductTemplate.KOMPLETT) {
             result.addAll(getProductGroupAmountForObject("eageb", gebaeudeAmount));
             result.addAll(getProductGroupAmountForObject("eaflst", flurstueckAmount));
             result.addAll(getProductGroupAmountForObject("eaeig", flurstueckAmount));
         }
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   objectBaseKey  DOCUMENT ME!
      * @param   amount         DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ArrayList<ProductGroupAmount> getProductGroupAmountForObject(final String objectBaseKey, int amount) {
         final ArrayList<ProductGroupAmount> result = new ArrayList<ProductGroupAmount>();
         if (amount > 1000000) {
             final int tmpPoints = amount - 1000000;
             result.add(new ProductGroupAmount(objectBaseKey + "_1000001", tmpPoints));
             amount = 1000000;
         }
         if (amount > 100000) {
             final int tmpPoints = amount - 100000;
             result.add(new ProductGroupAmount(objectBaseKey + "_100001-1000000", tmpPoints));
             amount = 100000;
         }
         if (amount > 10000) {
             final int tmpPoints = amount - 10000;
             result.add(new ProductGroupAmount(objectBaseKey + "_10001-100000", tmpPoints));
             amount = 10000;
         }
         if (amount > 1000) {
             final int tmpPoints = amount - 1000;
             result.add(new ProductGroupAmount(objectBaseKey + "_1001-10000", tmpPoints));
             amount = 1000;
         }
         result.add(new ProductGroupAmount(objectBaseKey + "_1000", amount));
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  requestId  DOCUMENT ME!
      * @param  template   DOCUMENT ME!
      */
     private void doDownload(final String requestId, final NasProductTemplate template) {
         if (DownloadManagerDialog.showAskingForUserTitle(
                         CismapBroker.getInstance().getMappingComponent())) {
             final String jobname = (!DownloadManagerDialog.getJobname().equals("")) ? DownloadManagerDialog
                             .getJobname() : null;
             DownloadManager.instance()
                     .add(
                         new NASDownload(
                             "NAS-Download",
                            jobname,
                             "",
                             requestId,
                             template,
                             generateSearchGeomCollection()));
         } else {
             DownloadManager.instance()
                     .add(
                         new NASDownload(
                             "NAS-Download",
                             "",
                             "",
                             requestId,
                             template,
                             generateSearchGeomCollection()));
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         final Runnable mapRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     final ActiveLayerModel mappingModel = new ActiveLayerModel();
                     mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                     mappingModel.addHome(getBoundingBox());
 
                     final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(
                                 AlkisConstants.COMMONS.MAP_CALL_STRING));
                     swms.setName("NAS-Dialog");
 
                     // add the raster layer to the model
                     mappingModel.addLayer(swms);
                     // set the model
                     map.setMappingModel(mappingModel);
                     // initial positioning of the map
                     final int duration = map.getAnimationDuration();
                     map.setAnimationDuration(0);
                     map.gotoInitialBoundingBox();
                     // interaction mode
                     map.setInteractionMode(MappingComponent.ZOOM);
                     // finally when all configurations are done ...
                     map.unlock();
                     map.setInteractionMode("MUTE");
                     map.setAnimationDuration(duration);
                     totalMapWrapper = new GeomWrapper(
                             ((XBoundingBox)CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBox())
                                         .getGeometry(),
                             "kompletter Kartenausschnitt",
                             false);
 
                     geomWrappers.add(totalMapWrapper);
                     for (final GeomWrapper cidsBeanWrapper : geomWrappers) {
                         map.getFeatureCollection().addFeature(cidsBeanWrapper.getFeature());
                     }
                 }
 
                 private XBoundingBox getBoundingBox() {
                     final XBoundingBox currBb = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                                 .getCurrentBoundingBox();
                     XBoundingBox result = new XBoundingBox(currBb.getGeometry().buffer(TOTAL_MAP_BUFFER));
 //                    final double diagonalLength = Math.sqrt((result.getWidth() * result.getWidth())
 //                                    + (result.getHeight() * result.getHeight()));
 //                    final XBoundingBox bufferedBox = new XBoundingBox(result.getGeometry().buffer(TOTAL_MAP_BUFFER));
                     for (final GeomWrapper gw : geomWrappers) {
                         final Geometry geometry = CrsTransformer.transformToGivenCrs(
                                 gw.getGeometry(),
                                 AlkisConstants.COMMONS.SRS_SERVICE);
 
                         if (result == null) {
                             result = new XBoundingBox(geometry.getEnvelope().buffer(
                                         TOTAL_MAP_BUFFER));
                             result.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                             result.setMetric(true);
                         } else {
                             final XBoundingBox temp = new XBoundingBox(geometry.getEnvelope().buffer(
                                         TOTAL_MAP_BUFFER));
                             temp.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                             temp.setMetric(true);
 
                             if (temp.getX1() < result.getX1()) {
                                 result.setX1(temp.getX1());
                             }
                             if (temp.getY1() < result.getY1()) {
                                 result.setY1(temp.getY1());
                             }
                             if (temp.getX2() > result.getX2()) {
                                 result.setX2(temp.getX2());
                             }
                             if (temp.getY2() > result.getY2()) {
                                 result.setY2(temp.getY2());
                             }
                         }
                     }
 
                     return result;
                 }
             };
 
         if (EventQueue.isDispatchThread()) {
             mapRunnable.run();
         } else {
             EventQueue.invokeLater(mapRunnable);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void changeMap() {
         final Runnable mapChangeRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     if (selectedGeomWrapper != null) {
                         final XBoundingBox boxToGoto = new XBoundingBox(selectedGeomWrapper.getGeometry().getEnvelope(),
                                 AlkisConstants.COMMONS.SRS_SERVICE,
                                 true);
                         final XBoundingBox bufferedBox;
                         if (selectedGeomWrapper != totalMapWrapper) {
                             final double diagonalLength = Math.sqrt((boxToGoto.getWidth() * boxToGoto.getWidth())
                                             + (boxToGoto.getHeight() * boxToGoto.getHeight()));
                             bufferedBox = new XBoundingBox(boxToGoto.getGeometry().buffer(diagonalLength / 2));
                         } else {
                             bufferedBox = new XBoundingBox(boxToGoto.getGeometry().buffer(TOTAL_MAP_BUFFER));
                         }
                         map.gotoBoundingBox(bufferedBox, false, true, 500);
                     }
                 }
             };
 
         if (EventQueue.isDispatchThread()) {
             mapChangeRunnable.run();
         } else {
             EventQueue.invokeLater(mapChangeRunnable);
         }
     }
 
     @Override
     public void stateChanged(final ChangeEvent ce) {
         if ((ce.getSource() == jsGeomBuffer)) {
             final int buffer = jsGeomBuffer.getValue();
             if (!jsGeomBuffer.getValueIsAdjusting()) {
                 // clear map visualisation and start the fee calculation
                 clearMapVisualisation(buffer);
                 calculateFee();
             } else {
                 // visualize the buffer
                 visualizeBufferGeomsInMap(buffer);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  buffer  DOCUMENT ME!
      */
     private void clearMapVisualisation(final int buffer) {
         final Runnable mapBufferRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     // create a new Feature for the finally buffered Geometry and add it to the map
                     bufferFeatures.clear();
                     firstBufferCall = true;
                     map.getFeatureCollection().removeAllFeatures();
 
                     for (final GeomWrapper geomWrapper : geomWrappers) {
                         final GeomWrapper gw = new GeomWrapper(geomWrapper.getGeometry().buffer(buffer),
                                 null,
                                 false);
                         map.getFeatureCollection().addFeature(gw.getFeature());
                     }
                 }
             };
 
         final Timer t = new Timer();
         t.schedule(new TimerTask() {
 
                 @Override
                 public void run() {
                     if (EventQueue.isDispatchThread()) {
                         mapBufferRunnable.run();
                     } else {
                         EventQueue.invokeLater(mapBufferRunnable);
                     }
                 }
             }, 10);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  buffer  DOCUMENT ME!
      */
     private void visualizeBufferGeomsInMap(final int buffer) {
         if (firstBufferCall) {
             firstBufferCall = false;
             // visualize the original geometries in map
             map.getFeatureCollection().removeAllFeatures();
             for (final GeomWrapper geomWrapper : geomWrappers) {
                 map.getFeatureCollection().addFeature(geomWrapper.getFeature());
             }
 //            visualizeBufferGeomsInMap(buffer);
         }
         if (bufferFeatures.isEmpty()) {
             for (final GeomWrapper gw : geomWrappers) {
                 final DefaultStyledFeature dsf = new DefaultStyledFeature();
                 final Geometry bufferGeom = gw.getGeometry().buffer(buffer);
                 final Geometry intersectGeom;
                 if (buffer > 0) {
                     intersectGeom = bufferGeom.difference(gw.getGeometry());
                 } else {
                     intersectGeom = bufferGeom.intersection(gw.getGeometry());
                 }
                 dsf.setGeometry(intersectGeom);
                 dsf.setFillingPaint(new Color(212, 100, 97, 212));
                 dsf.setTransparency(0.5f);
                 bufferFeatures.put(gw, dsf);
             }
             map.getFeatureCollection().addFeatures(bufferFeatures.values());
         } else {
             for (final GeomWrapper gw : bufferFeatures.keySet()) {
                 Geometry g = gw.getGeometry();
                 g = g.buffer(buffer);
                 final Feature f = bufferFeatures.get(gw);
                 f.setGeometry(g);
                 map.getFeatureCollection().reconsiderFeature(f);
             }
         }
         map.zoomToFeatureCollection();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  wait  DOCUMENT ME!
      */
     private void showWait(final boolean wait) {
         if (wait) {
             if (!lblBusy.isBusy()) {
                 pnlFee.removeAll();
                 pnlFee.add(lblBusy);
                 lblBusy.setBusy(true);
                 lblBusy.setVisible(true);
             }
         } else {
             lblBusy.setBusy(false);
             lblBusy.setVisible(wait);
             pnlFee.removeAll();
             pnlFee.add(feePreview);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void showError() {
 //        showWait(false);
 //        SwingUtilities.invokeLater(new Runnable() {
 //
 //                @Override
 //                public void run() {
         pnlFee.remove(lblBusy);
         pnlFee.removeAll();
         pnlFee.add(lblError, BorderLayout.CENTER);
         lblError.setVisible(true);
         pnlFee.invalidate();
         pnlFee.revalidate();
         repaint();
 //                }
 //            });
 //        repaint();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void calculateFee() {
         final SwingWorker<HashMap<String, ArrayList<String>>, Void> feeCalculator =
             new SwingWorker<HashMap<String, ArrayList<String>>, Void>() {
 
                 @Override
                 protected HashMap<String, ArrayList<String>> doInBackground() throws Exception {
                     SwingUtilities.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 showWait(true);
                             }
                         });
                     // clear the old amount fields
                     pointAmount = 0;
                     flurstueckAmount = 0;
                     gebaeudeAmount = 0;
                     final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
                     final Geometry searchGeom = generateSearchGeom();
                     if (searchGeom == null) {
                         return null;
                     }
                     // do the search
                     final NasProductTemplate type = (NasProductTemplate)cbType.getSelectedItem();
                     double totalFee = 0;
                     if (type == NasProductTemplate.POINTS) {
                         final ArrayList<String> values = new ArrayList<String>();
                         pointAmount = NasFeeCalculator.getPointAmount(searchGeom);
                         values.add("" + pointAmount);
                         final double pointFee = NasFeeCalculator.getFeeForPoints(pointAmount);
                         totalFee += pointFee;
                         values.add(formatter.format(pointFee));
                         result.put("points", values);
                     } else {
                         final ArrayList<String> flurstueckValues = new ArrayList<String>();
                         final ArrayList<String> gebaeudeValues = new ArrayList<String>();
                         flurstueckAmount = NasFeeCalculator.getFlurstueckAmount(searchGeom);
                         flurstueckValues.add("" + flurstueckAmount);
                         final double flurstueckFee = NasFeeCalculator.getFeeForFlurstuecke(flurstueckAmount);
                         totalFee += flurstueckFee;
                         // ToDo this is a quick and dirty way to calculate the fee for type KOMPLETT
                         if (type == NasProductTemplate.KOMPLETT) {
                             final double eigentuemerFee = NasFeeCalculator.getFeeForEigentuemer(flurstueckAmount);
                             totalFee += eigentuemerFee;
                             final ArrayList<String> eigentuemerValues = new ArrayList<String>();
                             eigentuemerValues.add("" + flurstueckAmount);
                             eigentuemerValues.add(formatter.format(eigentuemerFee));
                             result.put("eigentuemer", eigentuemerValues);
                         }
                         flurstueckValues.add(formatter.format(flurstueckFee));
                         result.put("flurstuecke", flurstueckValues);
                         gebaeudeAmount = NasFeeCalculator.getGebaeudeAmount(searchGeom);
                         gebaeudeValues.add("" + gebaeudeAmount);
                         final double gebaeudeFee = NasFeeCalculator.getFeeForGebaeude(gebaeudeAmount);
                         totalFee += gebaeudeFee;
                         gebaeudeValues.add(formatter.format(gebaeudeFee));
                         result.put("gebaeude", gebaeudeValues);
                     }
                     final ArrayList<String> totalList = new ArrayList<String>();
                     totalList.add(formatter.format(totalFee));
                     result.put("total", totalList);
                     return result;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         final HashMap<String, ArrayList<String>> result = get();
                         final NasProductTemplate selectedTemplate = (NasProductTemplate)cbType.getSelectedItem();
                         feePreview = new NasFeePreviewPanel(selectedTemplate);
                         if (result == null) {
 //                            showWait(false);
                             showError();
                             return;
                         }
                         for (final String key : result.keySet()) {
                             final ArrayList<String> values = result.get(key);
                             if (key.equals("total")) {
                                 feePreview.setTotalLabel(values.get(0));
                             }
                             if (selectedTemplate == NasProductTemplate.POINTS) {
                                 if (key.equals("points")) {
                                     feePreview.setPointLabels(values.get(0), values.get(1));
                                     break;
                                 }
                             } else {
                                 if (selectedTemplate == NasProductTemplate.KOMPLETT) {
                                     if (key.equals("eigentuemer")) {
                                         feePreview.setEigentuemerLabels(values.get(0), values.get(1));
                                     }
                                 }
                                 if (key.equals("gebaeude")) {
                                     feePreview.setGebaeudeLabels(values.get(0), values.get(1));
                                 } else if (key.equals("flurstuecke")) {
                                     feePreview.setFlurstueckLabels(values.get(0), values.get(1));
                                 }
                             }
                         }
                     } catch (InterruptedException ex) {
                         showError();
                         log.error("nas fee calculation was interrupted. showing error state", ex);
                         return;
                     } catch (ExecutionException ex) {
                         showError();
                         log.error("an error occured during nas fee calculation. showing error state", ex);
                         return;
                     }
 
                     showWait(false);
                 }
             };
 
         feeCalculator.execute();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Geometry generateSearchGeom() {
         Geometry unionGeom = null;
         final int buffer = jsGeomBuffer.getValue();
         for (final GeomWrapper gw : geomWrappers) {
             if (gw.isSelected()) {
                 Geometry g = gw.getGeometry();
                 if (buffer != 0) {
                     g = g.buffer(buffer);
                 }
                 if (unionGeom == null) {
                     unionGeom = g;
                 } else {
                     unionGeom = unionGeom.union(g);
                 }
             }
         }
         return unionGeom;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private GeometryCollection generateSearchGeomCollection() {
         int collectionSize = 0;
         for (final GeomWrapper gw : geomWrappers) {
             if (gw.isSelected()) {
                 collectionSize++;
             }
         }
         final Geometry[] geoms = new Geometry[collectionSize];
         final int buffer = jsGeomBuffer.getValue();
         int i = 0;
         GeometryFactory gf = null;
         for (final GeomWrapper gw : geomWrappers) {
             if (gw.isSelected()) {
                 Geometry g = gw.getGeometry();
                 if (buffer != 0) {
                     g = g.buffer(buffer);
                 }
                 if (gf == null) {
                     gf = g.getFactory();
                 }
                 geoms[i] = g;
                 i++;
             }
         }
         return new GeometryCollection(geoms, gf);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ArrayList<NasProductTemplate> getProductTemplates() {
         return productTemplates;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  productTemplates  DOCUMENT ME!
      */
     public void setProductTemplates(final ArrayList<NasProductTemplate> productTemplates) {
         this.productTemplates = productTemplates;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class GeomWrapper {
 
         //~ Instance fields ----------------------------------------------------
 
         private boolean selected;
         private Color color;
         private Geometry geometry;
         private StyledFeature feature;
         private String name;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new GeomWrapper object.
          *
          * @param  g         DOCUMENT ME!
          * @param  name      DOCUMENT ME!
          * @param  selected  DOCUMENT ME!
          */
         public GeomWrapper(final Geometry g, final String name, final boolean selected) {
             this.selected = selected;
             this.name = name;
             this.geometry = CrsTransformer.transformToGivenCrs(g,
                     AlkisConstants.COMMONS.SRS_SERVICE);
 
             this.feature = generateFeature();
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Color getColor() {
             return color;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  color  DOCUMENT ME!
          */
         public void setColor(final Color color) {
             this.color = color;
             feature.setFillingPaint(this.color);
             feature.setLinePaint(this.color);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public boolean isSelected() {
             return selected;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  selected  DOCUMENT ME!
          */
         public void setSelected(final boolean selected) {
             this.selected = selected;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public Geometry getGeometry() {
             return geometry;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  geometry  DOCUMENT ME!
          */
         public void setGeometry(final Geometry geometry) {
             this.geometry = geometry;
             this.feature = generateFeature();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public StyledFeature getFeature() {
             return feature;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getName() {
             return name;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private StyledFeature generateFeature() {
             final StyledFeature dsf = new DefaultStyledFeature();
             dsf.setGeometry(this.geometry);
             dsf.setTransparency(0.8F);
             dsf.setFillingPaint(new Color(192, 80, 77, 192));
             return dsf;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class NasTableModel extends AbstractTableModel {
 
         //~ Instance fields ----------------------------------------------------
 
         private int selectedGeoms = 0;
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public int getRowCount() {
             return geomWrappers.size();
         }
 
         @Override
         public int getColumnCount() {
             return 2;
         }
 
         @Override
         public Object getValueAt(final int rowIndex, final int columnIndex) {
             if (geomWrappers == null) {
                 return null;
             }
             final GeomWrapper gw = geomWrappers.get(rowIndex);
             if (columnIndex == 0) {
                 return gw.isSelected();
             } else {
                 return gw.getName();
             }
         }
 
         @Override
         public void setValueAt(final Object value, final int row, final int column) {
             if (column != 0) {
                 return;
             }
 
             final GeomWrapper geomWrapper = geomWrappers.get(row);
             geomWrapper.setSelected(!geomWrapper.isSelected());
             if (geomWrapper.isSelected()) {
                 selectedGeoms++;
             } else {
                 selectedGeoms--;
             }
             fireTableRowsUpdated(row, row);
 
             if (row == (geomWrappers.size() - 1)) {
                 totalMapWrapper.setSelected(geomWrapper.isSelected());
                 for (final GeomWrapper gw : geomWrappers) {
                     if ((gw != geomWrapper) && gw.isSelected()) {
                         gw.setSelected(false);
                     }
                 }
                 fireTableRowsUpdated(0, geomWrappers.size() - 2);
             } else {
                 final GeomWrapper lastEntry = geomWrappers.get(geomWrappers.size() - 1);
                 boolean otherGeomWrapperSelected = false;
 
                 for (final GeomWrapper gw : geomWrappers) {
                     if (gw != lastEntry) {
                         if (gw.isSelected()) {
                             otherGeomWrapperSelected = true;
                             break;
                         }
                     }
                 }
                 if (otherGeomWrapperSelected) {
                     lastEntry.setSelected(false);
                     totalMapWrapper.setSelected(false);
                     fireTableRowsUpdated(geomWrappers.size() - 1, geomWrappers.size() - 1);
                 }
             }
 
             changeMap();
             calculateFee();
         }
 
         @Override
         public Class<?> getColumnClass(final int columnIndex) {
             if (columnIndex == 0) {
                 return Boolean.class;
             } else if (columnIndex == 3) {
                 return Color.class;
             } else {
                 return String.class;
             }
         }
 
         @Override
         public String getColumnName(final int column) {
             return NbBundle.getMessage(
                     NasTableModel.class,
                     "NasDialog.NasTableModel.getColumnName("
                             + column
                             + ")");
         }
 
         @Override
         public boolean isCellEditable(final int row, final int column) {
             return column == 0;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   index  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public GeomWrapper get(final int index) {
             return geomWrappers.get(index);
         }
     }
 }
