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
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JOptionPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
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
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class NasDialog extends javax.swing.JDialog implements ChangeListener, DocumentListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger log = Logger.getLogger(NasDialog.class);
     private static double MAP_BUFFER = 50d;
 //    private static final Color FEATURE_COLOR_SELECTED = new Color(1f, 0f, 0f, 0.4f);
     private static final Color FEATURE_COLOR_SELECTED = new Color(1f, 0f, 0f, 0.7f);
     private static final Color FEATURE_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.1f);
 
     //~ Instance fields --------------------------------------------------------
 
     long lastDocEvent = 0;
     boolean ignoreNextDocEvents = false;
     Timer docTimer = new Timer();
     GeomWrapper totalMapWrapper;
     private MappingComponent map;
     private LinkedList<GeomWrapper> geomWrappers;
     private HashMap<GeomWrapper, Feature> bufferedFeatures = new HashMap<GeomWrapper, Feature>();
     private NasTableModel tableModel;
     private ArrayList<GeomWrapper> selectedGeomWrappers = new ArrayList<GeomWrapper>();
     private HashMap<GeomWrapper, Feature> bufferFeatureMap = new HashMap<GeomWrapper, Feature>();
     private NasFeePreviewPanel feePreview = new NasFeePreviewPanel();
     private ArrayList<NasProductTemplate> productTemplates = new ArrayList<NasProductTemplate>();
     private ArrayList<DefaultStyledFeature> pointFeatures = new ArrayList<DefaultStyledFeature>();
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
     private javax.swing.JLabel lblGeomBuffer;
     private javax.swing.JLabel lblType;
     private de.cismet.cids.custom.nas.NasFeePreviewPanel nasFeePreviewPanel1;
     private javax.swing.JPanel pnlControls;
     private javax.swing.JPanel pnlFee;
     private javax.swing.JPanel pnlMap;
     private javax.swing.JPanel pnlSettings;
     private javax.swing.JTable tblGeom;
     private javax.swing.JTextField tfAuftragsnummer;
     private javax.swing.JTextField tfGeomBuffer;
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
         this(parent, modal, null);
     }
 
     /**
      * Creates a new NasDialog object.
      *
      * @param  parent            DOCUMENT ME!
      * @param  modal             DOCUMENT ME!
      * @param  selectedFeatures  DOCUMENT ME!
      */
     public NasDialog(final java.awt.Frame parent, final boolean modal, final Collection<Feature> selectedFeatures) {
         super(parent, modal);
         MAP_BUFFER = Double.parseDouble(NbBundle.getMessage(NasDialog.class, "NasDialog.selectedGeomMapBuffer"));
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
             boolean selected = false;
             if (selectedFeatures == null) {
                 selected = true;
             } else {
                 for (final Feature featurToSelect : selectedFeatures) {
                     if (f.equals(featurToSelect)) {
                         selected = true;
                     }
                 }
             }
 //            if ((f.getGeometry() instanceof Polygon) || (f.getGeometry() instanceof MultiPolygon)) {
             double buffer = 0;
             if ((f.getGeometry() instanceof Point) || (f.getGeometry() instanceof LineString)) {
                 buffer = 0.001;
                 if (f.getGeometry() instanceof Point) {
                     final DefaultStyledFeature dsf = new DefaultStyledFeature();
                     dsf.setGeometry(f.getGeometry());
                     final BufferedImage bi = new BufferedImage(9, 9, BufferedImage.TYPE_4BYTE_ABGR);
                     final Graphics2D g = (Graphics2D)bi.getGraphics().create();
                     g.setStroke(new BasicStroke(1f));
                     g.setColor(Color.black);
                     g.drawOval(0, 0, 5, 5);
                     final FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(
                             new javax.swing.ImageIcon(
                                 getClass().getResource("/de/cismet/cids/custom/nas/icon-circlerecordempty.png"))
                                         .getImage());
                     fas.setSweetSpotX(0.5);
                     fas.setSweetSpotY(0.5);
                     dsf.setPointAnnotationSymbol(fas);
                     pointFeatures.add(dsf);
                 }
             }
             final PFeature pf = new PFeature(f, map);
             if (!pf.hasHole()) {
                 geomWrappers.add(new GeomWrapper(f.getGeometry().buffer(buffer), name, selected));
             }
 //            }
         }
         tableModel = new NasTableModel();
         initComponents();
         tblGeom.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final ListSelectionEvent e) {
                     final ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 
 //                    selectedGeomWrapper = tableModel.get(lsm.getLeadSelectionIndex());
                     selectedGeomWrappers.clear();
                     for (int i = lsm.getMinSelectionIndex(); i <= lsm.getMaxSelectionIndex(); i++) {
                         if (lsm.isSelectedIndex(i)) {
                             selectedGeomWrappers.add(tableModel.get(i));
                         }
                     }
 
                     changeMap();
                 }
             });
         tblGeom.getColumnModel().getColumn(0).setPreferredWidth(80);
         tblGeom.getColumnModel().getColumn(0).setMaxWidth(80);
         jsGeomBuffer.addChangeListener(this);
         tfGeomBuffer.getDocument().addDocumentListener(this);
         map = new MappingComponent();
         initMap();
         pnlMap.setLayout(new BorderLayout());
         pnlMap.add(map, BorderLayout.CENTER);
         cbType.setSelectedItem(NasProductTemplate.OHNE_EIGENTUEMER);
         calculateFee();
         isInitialized = true;
         map.addMouseListener(new MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final MouseEvent e) {
                     map.zoomToFeatureCollection();
                     tblGeom.clearSelection();
                 }
             });
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
         nasFeePreviewPanel1 = new de.cismet.cids.custom.nas.NasFeePreviewPanel();
         jPanel2 = new javax.swing.JPanel();
         lblGeomBuffer = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         tfGeomBuffer = new javax.swing.JTextField();
         pnlControls = new javax.swing.JPanel();
         btnOk = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.title")); // NOI18N
         setMinimumSize(new java.awt.Dimension(617, 180));
         setPreferredSize(new java.awt.Dimension(780, 540));
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         pnlMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         final javax.swing.GroupLayout pnlMapLayout = new javax.swing.GroupLayout(pnlMap);
         pnlMap.setLayout(pnlMapLayout);
         pnlMapLayout.setHorizontalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 345,
                 Short.MAX_VALUE));
         pnlMapLayout.setVerticalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 450,
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
         gridBagConstraints.gridy = 5;
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
         gridBagConstraints.gridy = 5;
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
         gridBagConstraints.gridy = 7;
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
         gridBagConstraints.gridy = 6;
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
         pnlFee.setMinimumSize(new java.awt.Dimension(144, 100));
         pnlFee.setPreferredSize(new java.awt.Dimension(144, 150));
         pnlFee.setLayout(new java.awt.BorderLayout());
         pnlFee.add(nasFeePreviewPanel1, java.awt.BorderLayout.CENTER);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
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
                 tfGeomBuffer,
                 org.jdesktop.beansbinding.ELProperty.create("${text}"),
                 jLabel1,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 "geomBufferBinding");
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
 
         tfGeomBuffer.setMinimumSize(new java.awt.Dimension(50, 27));
         tfGeomBuffer.setPreferredSize(new java.awt.Dimension(50, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
         pnlSettings.add(tfGeomBuffer, gridBagConstraints);
 
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
 //        map.gotoInitialBoundingBox();
 //        tblGeom.clearSelection();
     } //GEN-LAST:event_tblGeomFocusLost
 
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
                         if ((requestId != null)) {
                             boolean containsWrongChar = false;
                             String wrongChar = "";
                             if (requestId.contains("/")) {
                                 containsWrongChar = true;
                                 wrongChar += "/";
                             } else if (requestId.contains("\\")) {
                                 containsWrongChar = true;
                                 wrongChar += "\\";
                             }
 
                             if (containsWrongChar) {
                                 JOptionPane.showMessageDialog(
                                     StaticSwingTools.getParentFrame(NasDialog.this),
                                     org.openide.util.NbBundle.getMessage(
                                         NasDialog.class,
                                         "NasDialog.OrderIdCheck.JOptionPane.message")
                                             + " '"
                                             + wrongChar
                                             + "'",
                                     org.openide.util.NbBundle.getMessage(
                                         NasDialog.class,
                                         "NasDialog.OrderIdCheck.JOptionPane.title"),
                                     JOptionPane.ERROR_MESSAGE);
                                 tfAuftragsnummer.requestFocus();
                                 return;
                             }
                         }
                         final ArrayList<ProductGroupAmount> list = feePreview.getProductGroupAmounts();
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
                             "",
                             jobname,
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
                     swms.setTranslucency(0.4f);
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
                             geomWrappers.isEmpty() ? true : false);
 
                     geomWrappers.add(totalMapWrapper);
                     for (final GeomWrapper cidsBeanWrapper : geomWrappers) {
                         map.getFeatureCollection().addFeature(cidsBeanWrapper.getFeature());
                     }
                     map.getFeatureCollection().addFeatures(pointFeatures);
                 }
 
                 private XBoundingBox getBoundingBox() {
                     final XBoundingBox currBb = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                                 .getCurrentBoundingBox();
                     final Geometry transformedGeom = CrsTransformer.transformToGivenCrs(currBb.getGeometry(),
                             AlkisConstants.COMMONS.SRS_SERVICE);
                     XBoundingBox result = new XBoundingBox(transformedGeom.buffer(MAP_BUFFER));
 //                    final double diagonalLength = Math.sqrt((result.getWidth() * result.getWidth())
 //                                    + (result.getHeight() * result.getHeight()));
 //                    final XBoundingBox bufferedBox = new XBoundingBox(result.getGeometry().buffer(TOTAL_MAP_BUFFER));
                     for (final GeomWrapper gw : geomWrappers) {
                         final Geometry geometry = CrsTransformer.transformToGivenCrs(
                                 gw.getGeometry(),
                                 AlkisConstants.COMMONS.SRS_SERVICE);
 
                         if (result == null) {
                             result = new XBoundingBox(geometry.getEnvelope().buffer(
                                         MAP_BUFFER));
                             result.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                             result.setMetric(true);
                         } else {
                             final XBoundingBox temp = new XBoundingBox(geometry.getEnvelope().buffer(
                                         MAP_BUFFER));
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
                     Geometry g;
                     if (!selectedGeomWrappers.isEmpty()) {
                         g = createUnionGeom(selectedGeomWrappers, true);
                         // if the buffer geometries is empty use the original geoms as fallback
                         if (g.isEmpty()) {
                             g = createUnionGeom(selectedGeomWrappers, false);
                         }
                     } else {
                         g = totalMapWrapper.getGeometry();
                     }
                     final XBoundingBox boxToGoto = new XBoundingBox(g.getEnvelope(),
                             AlkisConstants.COMMONS.SRS_SERVICE,
                             true);
                     final XBoundingBox bufferedBox;
 //                            bufferedBox = new XBoundingBox(boxToGoto.getGeometry().buffer(diagonalLength / 2));
                     bufferedBox = new XBoundingBox(boxToGoto.getGeometry().buffer(MAP_BUFFER));
                     map.gotoBoundingBox(bufferedBox, false, true, 500);
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
             handleBufferChanged();
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void handleBufferChanged() {
         int buffer = 0;
         try {
             buffer = Integer.parseInt(tfGeomBuffer.getText());
         } catch (Exception e) {
         }
         tfGeomBuffer.getDocument().removeDocumentListener(this);
         tfGeomBuffer.setText("" + jsGeomBuffer.getValue());
         tfGeomBuffer.getDocument().addDocumentListener(this);
         if (!jsGeomBuffer.getValueIsAdjusting()) {
             // clear map visualisation and start the fee calculation
 
             clearMapVisualisation(buffer);
             changeMap();
             calculateFee();
         } else {
             // visualize the buffer
             visualizeBufferGeomsInMap(buffer);
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
                     bufferFeatureMap.clear();
 //                    bufferedFeatures.clear();
                     firstBufferCall = true;
                     map.getFeatureCollection().removeAllFeatures();
                     if ((jsGeomBuffer.getValue() == 0)) {
                         map.getFeatureCollection().addFeatures(pointFeatures);
                     }
 
                     for (final GeomWrapper geomWrapper : geomWrappers) {
                         final GeomWrapper bufferedGeomWrapper = new GeomWrapper(geomWrapper.getGeometry().buffer(
                                     buffer),
                                 null,
                                 geomWrapper.isSelected());
                         bufferedFeatures.put(geomWrapper, bufferedGeomWrapper.getFeature());
                         map.getFeatureCollection().addFeature(bufferedGeomWrapper.getFeature());
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
 //            if ((jsGeomBuffer.getValue() >= 0) && (jsGeomBuffer.getValue() < 5)) {
             map.getFeatureCollection().addFeatures(pointFeatures);
 //            }
 
             for (final GeomWrapper geomWrapper : geomWrappers) {
                 map.getFeatureCollection().addFeature(geomWrapper.getFeature());
             }
 //            visualizeBufferGeomsInMap(buffer);
         }
         if (bufferFeatureMap.isEmpty()) {
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
                 if (gw.isSelected()) {
                     dsf.setFillingPaint(FEATURE_COLOR_SELECTED);
                 } else {
                     dsf.setFillingPaint(FEATURE_COLOR);
                 }
                 dsf.setTransparency(0.5f);
                 bufferFeatureMap.put(gw, dsf);
             }
             map.getFeatureCollection().addFeatures(bufferFeatureMap.values());
         } else {
             for (final GeomWrapper gw : bufferFeatureMap.keySet()) {
                 Geometry g = gw.getGeometry();
                 g = g.buffer(buffer);
                 final Feature f = bufferFeatureMap.get(gw);
                 f.setGeometry(g);
                 map.getFeatureCollection().reconsiderFeature(f);
             }
         }
 //        changeMap();
 //        map.zoomToFeatureCollection();
     }
 
     /**
      * /** * DOCUMENT ME!*
      */
     private void calculateFee() {
         final NasProductTemplate selectedTemplate = (NasProductTemplate)cbType.getSelectedItem();
         final Geometry geom = generateSearchGeom();
         pnlFee.removeAll();
         feePreview = new NasFeePreviewPanel(selectedTemplate);
         feePreview.setGeom(geom);
         feePreview.refresh();
         pnlFee.add(feePreview);
         pnlFee.revalidate();
         pnlFee.repaint();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Geometry generateSearchGeom() {
         return createUnionGeom();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Geometry createUnionGeom() {
         final ArrayList<GeomWrapper> geoms = new ArrayList<GeomWrapper>();
         for (final GeomWrapper gw : geomWrappers) {
             if (gw.isSelected()) {
                 geoms.add(gw);
             }
         }
         return createUnionGeom(geoms, true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   gWrappers    DOCUMENT ME!
      * @param   bufferGeoms  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Geometry createUnionGeom(final Collection<GeomWrapper> gWrappers, final boolean bufferGeoms) {
         Geometry unionGeom = null;
         int buffer = 0;
         try {
             buffer = Integer.parseInt(tfGeomBuffer.getText());
         } catch (Exception e) {
         }
         for (final GeomWrapper gw : gWrappers) {
             Geometry g = gw.getGeometry();
             if (bufferGeoms && (buffer != 0)) {
                 g = g.buffer(buffer);
             }
             if (unionGeom == null) {
                 unionGeom = g;
             } else {
                 if (unionGeom instanceof MultiPolygon) {
                     unionGeom = unionGeom.union(g);
                     continue;
                 } else if (unionGeom instanceof GeometryCollection) {
                     final GeometryCollection gc = (GeometryCollection)unionGeom;
                     final Geometry[] geoms = new Geometry[unionGeom.getNumGeometries() + 1];
                     for (int i = 0; i < gc.getNumGeometries(); i++) {
                         geoms[i] = gc.getGeometryN(i);
                     }
                     geoms[geoms.length - 1] = g;
                     unionGeom = new GeometryCollection(geoms, gc.getFactory());
                 } else {
                     unionGeom = unionGeom.union(g);
                 }
             }
         }
         if (unionGeom != null) {
             final DefaultStyledFeature testDSF = new DefaultStyledFeature();
             testDSF.setGeometry(unionGeom);
             final PFeature pf = new PFeature(testDSF, map);
             pf.hasHole();
         }
 
         return unionGeom;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private GeometryCollection generateSearchGeomCollection() {
         final Geometry unionGeom = createUnionGeom();

        final GeometryFactory gf = new GeometryFactory(unionGeom.getPrecisionModel(), unionGeom.getSRID());
         Geometry[] geoms = null;
         if (unionGeom instanceof MultiPolygon) {
             final MultiPolygon mp = ((MultiPolygon)unionGeom);
             geoms = new Geometry[mp.getNumGeometries()];
             for (int i = 0; i < mp.getNumGeometries(); i++) {
                 final Geometry g = mp.getGeometryN(i);
                 geoms[i] = g;
             }
         } else if (unionGeom instanceof Polygon) {
             geoms = new Geometry[1];
             geoms[0] = unionGeom;
         }
 
         if (geoms == null) {
             return null;
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
 
     @Override
     public void insertUpdate(final DocumentEvent e) {
         final long currTime = System.currentTimeMillis();
         if ((currTime - lastDocEvent) < 800) {
             docTimer.cancel();
         }
         docTimer = new Timer();
         docTimer.schedule(new TimerTask() {
 
                 @Override
                 public void run() {
                     visualizeGeomBuffer();
                 }
             }, 1000);
         lastDocEvent = currTime;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void visualizeGeomBuffer() {
         ignoreNextDocEvents = true;
         final int buffer = Integer.parseInt(tfGeomBuffer.getText());
         if ((buffer >= -50) && (buffer <= 50)) {
             jsGeomBuffer.removeChangeListener(NasDialog.this);
             jsGeomBuffer.setValue(buffer);
             jsGeomBuffer.addChangeListener(NasDialog.this);
         }
         final Timer t = new Timer();
         t.schedule(new TimerTask() {
 
                 @Override
                 public void run() {
                     clearMapVisualisation(buffer);
                     calculateFee();
                     ignoreNextDocEvents = false;
                 }
             }, 2000);
         visualizeBufferGeomsInMap(buffer);
         changeMap();
     }
 
     @Override
     public void removeUpdate(final DocumentEvent e) {
         final long currTime = System.currentTimeMillis();
         if ((currTime - lastDocEvent) < 800) {
             docTimer.cancel();
         }
         docTimer = new Timer();
         docTimer.schedule(new TimerTask() {
 
                 @Override
                 public void run() {
                     visualizeGeomBuffer();
                 }
             }, 1000);
         lastDocEvent = currTime;
     }
 
     @Override
     public void changedUpdate(final DocumentEvent e) {
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
             final DefaultStyledFeature bufferedFeature = (DefaultStyledFeature)bufferedFeatures.get(this);
             if (selected) {
 //                dsf.setTransparency(0.8F);
                 feature.setFillingPaint(FEATURE_COLOR_SELECTED);
                 if (bufferedFeature != null) {
                     bufferedFeature.setFillingPaint(FEATURE_COLOR_SELECTED);
                 }
             } else {
 //                dsf.setTransparency(0.9F);
                 feature.setFillingPaint(FEATURE_COLOR);
                 if (bufferedFeature != null) {
                     bufferedFeature.setFillingPaint(FEATURE_COLOR);
                 }
             }
 //            map.getFeatureCollection().removeFeature(feature);
 //            map.getFeatureCollection().addFeature(feature);
             map.reconsiderFeature(feature);
             map.reconsiderFeature(bufferedFeature);
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
             if (selected) {
 //                dsf.setTransparency(0.8F);
                 dsf.setFillingPaint(FEATURE_COLOR_SELECTED);
             } else {
 //                dsf.setTransparency(0.9F);
                 dsf.setFillingPaint(FEATURE_COLOR);
             }
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
