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
 package de.cismet.cids.custom.butler;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.PrecisionModel;
 
 import org.apache.log4j.Logger;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import java.text.DecimalFormatSymbols;
 
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.LineBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.plaf.TabbedPaneUI;
 import javax.swing.plaf.basic.BasicButtonUI;
 
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.utils.butler.ButlerProduct;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.MultipleDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class Butler1Dialog extends javax.swing.JDialog implements DocumentListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(Butler1Dialog.class);
     private static final double MIN_BUFFER = 100d;
 
     //~ Instance fields --------------------------------------------------------
 
     // End of variables declaration
     private ArrayList<PredefinedBoxes> boxes;
     private final DocumentListener upperTfListeners;
     private boolean firstUpperTFChange = false;
     private final java.text.DecimalFormat coordFormatter = new java.text.DecimalFormat("#.###");
     private MappingComponent map;
     private PredefinedBoxes noSelectionBox = null;
     private DefaultStyledFeature rectangleFeature;
     private DefaultStyledFeature pointFeature;
     private boolean isPointCentered = false;
     private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(),
             CrsTransformer.extractSridFromCrs(AlkisConstants.COMMONS.SRS_SERVICE));
     private boolean documentListenersRemoved = false;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnCreate;
     private de.cismet.cids.custom.butler.Butler1ProductPanel butler1ProductPanel1;
     private javax.swing.JComboBox cbGeoms;
     private javax.swing.JComboBox cbSize;
     private javax.swing.Box.Filler filler1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel lblLowerPosition;
     private javax.swing.JLabel lblRequestNumber;
    private javax.swing.JLabel lblSRSInfo;
     private javax.swing.JLabel lblSize;
     private javax.swing.JLabel lblUpperPosition;
     private javax.swing.JPanel pnlControls;
     private javax.swing.JPanel pnlMap;
     private javax.swing.JPanel pnlMapSettings;
     private javax.swing.JPanel pnlProductSettings;
     private javax.swing.JPanel pnlRequestNumber;
     private javax.swing.JPanel pnlUpperBound;
     private javax.swing.JTabbedPane tbpProducts;
     private javax.swing.JTextField tfLowerE;
     private javax.swing.JTextField tfLowerN;
     private javax.swing.JTextField tfOrderId;
     private javax.swing.JTextField tfUpperE;
     private javax.swing.JTextField tfUpperN;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form Butler1Dialog.
      *
      * @param  parent  DOCUMENT ME!
      * @param  modal   DOCUMENT ME!
      */
     public Butler1Dialog(final java.awt.Frame parent, final boolean modal) {
         super(parent, modal);
         pointFeature = createPointFeature();
         rectangleFeature = createRectangleFeature();
         final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
         formatSymbols.setDecimalSeparator('.');
         coordFormatter.setDecimalFormatSymbols(formatSymbols);
         boxes = PredefinedBoxes.butler1Boxes;
         initComponents();
         tfLowerE.getDocument().addDocumentListener(this);
         tfLowerN.getDocument().addDocumentListener(this);
         final FocusListener upperTfFocusListener = new FocusAdapter() {
 
                 @Override
                 public void focusGained(final FocusEvent e) {
                     firstUpperTFChange = false;
                 }
             };
         tfUpperE.addFocusListener(upperTfFocusListener);
         tfUpperN.addFocusListener(upperTfFocusListener);
         upperTfListeners = new DocumentListener() {
 
                 @Override
                 public void insertUpdate(final DocumentEvent de) {
                     handleUpperTFAction();
                 }
 
                 @Override
                 public void removeUpdate(final DocumentEvent de) {
                     handleUpperTFAction();
                 }
 
                 @Override
                 public void changedUpdate(final DocumentEvent de) {
                     handleUpperTFAction();
                 }
             };
 
         tfUpperE.getDocument().addDocumentListener(upperTfListeners);
         tfUpperN.getDocument().addDocumentListener(upperTfListeners);
         tbpProducts.addMouseListener(new MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final MouseEvent me) {
                     final int tabNr = ((TabbedPaneUI)tbpProducts.getUI()).tabForCoordinate(
                             tbpProducts,
                             me.getX(),
                             me.getY());
                     if (tabNr == (tbpProducts.getTabCount() - 1)) {
                         // if we have only 1 tab we need to enable the clear button on the first tab
                         if (tbpProducts.getTabCount() == 2) {
                             tbpProducts.setTabComponentAt(0, getTabComponent(true, 1));
                         }
                         addCloseableTab();
 
                         tbpProducts.setSelectedIndex(tbpProducts.getTabCount() - 2);
                     }
                 }
             });
         tbpProducts.setBorder(null);
         tbpProducts.setTabComponentAt(0, getTabComponent(false));
         tbpProducts.setToolTipTextAt(0, "Produkt 1");
         final JPanel addPan = new JPanel();
         addPan.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
         addPan.setOpaque(false);
         addPan.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
         final JLabel addLabel = new JLabel("+");
         addLabel.setOpaque(false);
         addLabel.setBorder(null);
         addLabel.setFocusable(false);
         addPan.add(addLabel);
         tbpProducts.addTab("+", null);
         tbpProducts.setTabComponentAt(tbpProducts.getTabCount() - 1, addPan);
 //        tbpProducts.setTabComponentAt(tbpProducts.getTabCount() - 1, addLabel);
         for (final PredefinedBoxes box : boxes) {
             if (box.getDisplayName().equals("keine Auswahl")) {
                 noSelectionBox = box;
                 cbSize.setSelectedItem(noSelectionBox);
                 break;
             }
         }
         map = new MappingComponent();
         initMap();
         pnlMap.setLayout(new BorderLayout());
         pnlMap.add(map, BorderLayout.CENTER);
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
 
         pnlProductSettings = new javax.swing.JPanel();
         tbpProducts = new javax.swing.JTabbedPane();
         butler1ProductPanel1 = new de.cismet.cids.custom.butler.Butler1ProductPanel();
         pnlMapSettings = new javax.swing.JPanel();
         lblLowerPosition = new javax.swing.JLabel();
         tfLowerE = new javax.swing.JTextField();
         tfLowerN = new javax.swing.JTextField();
         lblSize = new javax.swing.JLabel();
         cbSize = new javax.swing.JComboBox();
         pnlMap = new javax.swing.JPanel();
         pnlUpperBound = new javax.swing.JPanel();
         tfUpperE = new javax.swing.JTextField();
         tfUpperN = new javax.swing.JTextField();
         lblUpperPosition = new javax.swing.JLabel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 0));
         jLabel1 = new javax.swing.JLabel();
         cbGeoms = new ButlerGeometryComboBox(ButlerGeometryComboBox.GEOM_FILTER_TYPE.BOTH);
         jLabel2 = new javax.swing.JLabel();
        lblSRSInfo = new javax.swing.JLabel();
         pnlControls = new javax.swing.JPanel();
         btnCreate = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         pnlRequestNumber = new javax.swing.JPanel();
         lblRequestNumber = new javax.swing.JLabel();
         tfOrderId = new javax.swing.JTextField();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.title_1")); // NOI18N
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         pnlProductSettings.setMinimumSize(new java.awt.Dimension(450, 300));
         pnlProductSettings.setPreferredSize(new java.awt.Dimension(450, 300));
         pnlProductSettings.setLayout(new java.awt.GridBagLayout());
 
         tbpProducts.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         tbpProducts.setMinimumSize(new java.awt.Dimension(400, 400));
         tbpProducts.addTab(org.openide.util.NbBundle.getMessage(
                 Butler1Dialog.class,
                 "Butler1Dialog.butler1ProductPanel1.TabConstraints.tabTitle"),
             butler1ProductPanel1); // NOI18N
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlProductSettings.add(tbpProducts, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlProductSettings, gridBagConstraints);
 
         pnlMapSettings.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblLowerPosition,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.lblLowerPosition.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
         pnlMapSettings.add(lblLowerPosition, gridBagConstraints);
 
         tfLowerE.setText(org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.tfLowerE.text")); // NOI18N
         tfLowerE.setMinimumSize(new java.awt.Dimension(70, 27));
         tfLowerE.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
         pnlMapSettings.add(tfLowerE, gridBagConstraints);
 
         tfLowerN.setText(org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.tfLowerN.text")); // NOI18N
         tfLowerN.setMinimumSize(new java.awt.Dimension(70, 27));
         tfLowerN.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(tfLowerN, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblSize,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.lblSize.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(lblSize, gridBagConstraints);
 
         cbSize.setPreferredSize(new java.awt.Dimension(150, 27));
 
         final org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${boxes}");
         final org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJComboBoxBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         cbSize);
         bindingGroup.addBinding(jComboBoxBinding);
 
         cbSize.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbSizeActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(cbSize, gridBagConstraints);
 
         pnlMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         final javax.swing.GroupLayout pnlMapLayout = new javax.swing.GroupLayout(pnlMap);
         pnlMap.setLayout(pnlMapLayout);
         pnlMapLayout.setHorizontalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 479,
                 Short.MAX_VALUE));
         pnlMapLayout.setVerticalGroup(
             pnlMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 290,
                 Short.MAX_VALUE));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         pnlMapSettings.add(pnlMap, gridBagConstraints);
 
         pnlUpperBound.setLayout(new java.awt.GridBagLayout());
 
         tfUpperE.setText(org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.tfUpperE.text")); // NOI18N
         tfUpperE.setMinimumSize(new java.awt.Dimension(70, 27));
         tfUpperE.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         pnlUpperBound.add(tfUpperE, gridBagConstraints);
 
         tfUpperN.setText(org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.tfUpperN.text")); // NOI18N
         tfUpperN.setMinimumSize(new java.awt.Dimension(70, 27));
         tfUpperN.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         pnlUpperBound.add(tfUpperN, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblUpperPosition,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.lblUpperPosition.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         pnlUpperBound.add(lblUpperPosition, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlUpperBound.add(filler1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         pnlMapSettings.add(pnlUpperBound, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel1,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.jLabel1.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
         pnlMapSettings.add(jLabel1, gridBagConstraints);
         jLabel1.getAccessibleContext()
                 .setAccessibleName(org.openide.util.NbBundle.getMessage(
                         Butler1Dialog.class,
                         "Butler1Dialog.jLabel1.AccessibleContext.accessibleName"));                   // NOI18N
 
         cbGeoms.setPreferredSize(new java.awt.Dimension(150, 27));
         cbGeoms.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbGeomsActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
         pnlMapSettings.add(cbGeoms, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel2,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.jLabel2.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 15, 0);
         pnlMapSettings.add(jLabel2, gridBagConstraints);
 
        org.openide.awt.Mnemonics.setLocalizedText(
            lblSRSInfo,
            org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.lblSRSInfo.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        pnlMapSettings.add(lblSRSInfo, gridBagConstraints);

         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlMapSettings, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnCreate,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.btnCreate.text")); // NOI18N
         btnCreate.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCreateActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         pnlControls.add(btnCreate, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnCancel,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.btnCancel.text")); // NOI18N
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCancelActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         pnlControls.add(btnCancel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlControls, gridBagConstraints);
 
         pnlRequestNumber.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblRequestNumber,
             org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.lblRequestNumber.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 33);
         pnlRequestNumber.add(lblRequestNumber, gridBagConstraints);
 
         tfOrderId.setText(org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.tfOrderId.text")); // NOI18N
         tfOrderId.setMinimumSize(new java.awt.Dimension(70, 27));
         tfOrderId.setPreferredSize(new java.awt.Dimension(90, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
         pnlRequestNumber.add(tfOrderId, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
         getContentPane().add(pnlRequestNumber, gridBagConstraints);
 
         bindingGroup.bind();
 
         pack();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCreateActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCreateActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final StringBuilder missConfiguredProducts = new StringBuilder();
                     boolean showErrorPane = false;
                     int missConfigPrdCount = 0;
                     for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
                         final Butler1ProductPanel productPanel = (Butler1ProductPanel)tbpProducts.getComponentAt(i);
                         final ButlerProduct bp = productPanel.getSelectedProduct();
                         if (!isProductConfigurationValid(bp)) {
                             showErrorPane = true;
                             missConfigPrdCount++;
                             missConfiguredProducts.append(i + 1);
                             missConfiguredProducts.append(", ");
                         }
                     }
                     if (showErrorPane) {
                         final String productNumbers = missConfiguredProducts.toString()
                                     .substring(0, missConfiguredProducts.toString().length() - 2);
                         final String message = (missConfigPrdCount == 1)
                             ? org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.ProductConfigCheck.JOptionPane.singularMessage")
                             : org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.ProductConfigCheck.JOptionPane.multiMessage");
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(Butler1Dialog.this),
                             String.format(
                                 message,
                                 productNumbers),                                       // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 Butler1ProductPanel.class,
                                 "Butler1Dialog.ProductConfigCheck.JOptionPane.title"), // NOI18N
                             JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     // check the geom coordinates
                     if ((rectangleFeature == null) || (rectangleFeature.getGeometry() == null)
                                 || (rectangleFeature.getGeometry().getArea() == 0)) {
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(Butler1Dialog.this),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.GeomConfigCheck.JOptionPane.message"),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.GeomConfigCheck.JOptionPane.title"),
                             JOptionPane.ERROR_MESSAGE);
                         return;
                     }
 
                     final Geometry g = rectangleFeature.getGeometry();
                     final double minX = g.getEnvelopeInternal().getMinX();
                     final double minY = g.getEnvelopeInternal().getMinY();
                     final double maxX = g.getEnvelopeInternal().getMaxX();
                     final double maxY = g.getEnvelopeInternal().getMaxY();
 
                     if ((minX >= maxX) || (minY >= maxY)) {
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(Butler1Dialog.this),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.GeomConfigCheck.JOptionPane.message"),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.GeomConfigCheck.JOptionPane.title"),
                             JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     // check that the orderId only consits of [a-z], [A-Z],[0-9] and _
                     final String orderId = tfOrderId.getText();
                     if (!orderId.matches("[a-zA-Z0-9_]*")) {
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(Butler1Dialog.this),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.OrderIdCheck.JOptionPane.message"),
                             org.openide.util.NbBundle.getMessage(
                                 Butler1Dialog.class,
                                 "Butler1Dialog.OrderIdCheck.JOptionPane.title"),
                             JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     // for each product tab we have to create one download
                     final StringBuilder jobnameBuilder = new StringBuilder();
                     if (DownloadManagerDialog.showAskingForUserTitle(
                                     CismapBroker.getInstance().getMappingComponent())) {
                         final String jobname = DownloadManagerDialog.getJobname();
                         if (jobname == null) {
                             jobnameBuilder.append("");
                         } else {
                             jobnameBuilder.append(jobname);
                         }
                     }
 
                     final ArrayList<ButlerDownload> downloads = new ArrayList<ButlerDownload>();
                     for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
                         final Butler1ProductPanel productPanel = (Butler1ProductPanel)tbpProducts.getComponentAt(i);
                         final ButlerProduct bp = productPanel.getSelectedProduct();
 
                         LOG.info(
                             "Create the following Butler product:\n\t orderId: "
                                     + tfOrderId.getText()
                                     + "\n\t productId: "
                                     + bp.getKey()
                                     + "\n\t colorDepth: "
                                     + bp.getColorDepth()
                                     + "\n\t resolution: "
                                     + bp.getResolution()
                                     + "\n\t format: "
                                     + bp.getFormat());
                         final String title = tfOrderId.getText()
                                     + "#"
                                     + (i + 1);
                         final ButlerDownload download = new ButlerDownload(
                                 jobnameBuilder.toString(),
                                 tfOrderId.getText()
                                         + "_"
                                         + (i + 1),
                                 bp,
                                 minX,
                                 minY,
                                 maxX,
                                 maxY);
                         downloads.add(download);
                     }
                     DownloadManager.instance()
                             .add(new MultipleDownload(downloads, "Butler Downloads " + tfOrderId.getText()));
 
                     Butler1Dialog.this.dispose();
                 }
             });
     } //GEN-LAST:event_btnCreateActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbSizeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbSizeActionPerformed
         final PredefinedBoxes selectedBox = (PredefinedBoxes)cbSize.getSelectedItem();
         if ((selectedBox != null) && !selectedBox.getDisplayName().equals("keine Auswahl")) {
             removeDocumentListeners();
             if (cbGeoms.getSelectedItem() instanceof Geometry) {
                 final Geometry g = (Geometry)cbGeoms.getSelectedItem();
                 if (!(g instanceof Point)) {
                     cbGeoms.setSelectedIndex(0);
                 }
             }
             firstUpperTFChange = true;
 
             if (selectedBox.getDisplayName().startsWith("M")) {
                 isPointCentered = true;
                 final Point p;
                 if ((!(cbGeoms.getSelectedItem() instanceof Geometry) || !(cbGeoms.getSelectedItem() instanceof Point))
                             && (rectangleFeature != null)) {
                     p = rectangleFeature.getGeometry().getEnvelope().getCentroid();
                 } else {
                     p = (Point)pointFeature.getGeometry();
                 }
                 final double lowerE = p.getX() - (selectedBox.getEastSize() / 2d);
                 final double lowerN = p.getY() - (selectedBox.getNorthSize() / 2d);
                 final double upperE = p.getX() + (selectedBox.getEastSize() / 2d);
                 final double upperN = p.getY() + (selectedBox.getNorthSize() / 2d);
                 tfLowerE.setText(coordFormatter.format(lowerE));
                 tfLowerN.setText(coordFormatter.format(lowerN));
                 tfUpperE.setText(coordFormatter.format(upperE));
                 tfUpperN.setText(coordFormatter.format(upperN));
             } else {
                 if (isPointCentered) {
                     final double lowerE = ((Point)pointFeature.getGeometry()).getX();
                     final double lowerN = ((Point)pointFeature.getGeometry()).getY();
                     final double upperE = ((Point)pointFeature.getGeometry()).getX() + (selectedBox.getEastSize());
                     final double upperN = ((Point)pointFeature.getGeometry()).getY() + (selectedBox.getNorthSize());
                     tfLowerE.setText(coordFormatter.format(lowerE));
                     tfLowerN.setText(coordFormatter.format(lowerN));
                     tfUpperE.setText(coordFormatter.format(upperE));
                     tfUpperN.setText(coordFormatter.format(upperN));
                     isPointCentered = false;
                 } else {
                     updatePositionFields();
                 }
             }
 
             changeMap();
             addDocumentListeners();
         } else {
             firstUpperTFChange = false;
         }
     } //GEN-LAST:event_cbSizeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
         this.dispose();                                                           // TODO add your handling code here:
     }                                                                             //GEN-LAST:event_btnCancelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbGeomsActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbGeomsActionPerformed
         final Object obj = cbGeoms.getSelectedItem();
         if ((obj != null) && (obj instanceof Geometry)) {
             removeDocumentListeners();
 
             final Geometry g = ((Geometry)obj);
             if (g.isRectangle()) {
                 final Envelope envelope = g.getEnvelopeInternal();
                 final Point p = g.getFactory().createPoint(new Coordinate(envelope.getMinX(), envelope.getMinY()));
                 pointFeature.setGeometry(p);
                 tfLowerE.setText("" + coordFormatter.format(envelope.getMinX()));
                 tfLowerN.setText("" + coordFormatter.format(envelope.getMinY()));
                 tfUpperE.setText("" + coordFormatter.format(envelope.getMaxX()));
                 tfUpperN.setText("" + coordFormatter.format(envelope.getMaxY()));
             } else if (g instanceof Point) {
                 final Point p = ((Point)g);
                 pointFeature.setGeometry(g);
                 tfLowerE.setText("" + coordFormatter.format(p.getX()));
                 tfLowerN.setText("" + coordFormatter.format(p.getY()));
                 tfUpperE.setText("" + coordFormatter.format(p.getX()));
                 tfUpperN.setText("" + coordFormatter.format(p.getY()));
             }
             cbSize.setSelectedItem(noSelectionBox);
             changeMap();
             addDocumentListeners();
         }
     } //GEN-LAST:event_cbGeomsActionPerformed
 
     /**
      * DOCUMENT ME!
      */
     private void addDocumentListeners() {
         if (!documentListenersRemoved) {
             removeDocumentListeners();
         }
         documentListenersRemoved = false;
         tfLowerE.getDocument().addDocumentListener(this);
         tfLowerN.getDocument().addDocumentListener(this);
         tfUpperE.getDocument().addDocumentListener(upperTfListeners);
         tfUpperN.getDocument().addDocumentListener(upperTfListeners);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void removeDocumentListeners() {
         documentListenersRemoved = true;
         tfLowerE.getDocument().removeDocumentListener(this);
         tfLowerN.getDocument().removeDocumentListener(this);
         tfUpperE.getDocument().removeDocumentListener(upperTfListeners);
         tfUpperN.getDocument().removeDocumentListener(upperTfListeners);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updatePositionFields() {
         removeDocumentListeners();
         final PredefinedBoxes selectedBox = (PredefinedBoxes)cbSize.getSelectedItem();
         if ((selectedBox != null) && !selectedBox.getDisplayName().equals("keine Auswahl")) {
             final double eSize = selectedBox.getEastSize();
             final double nSize = selectedBox.getNorthSize();
             if ((tfLowerE.getText() != null) && !tfLowerE.getText().equals("")) {
                 final double lowerE = Double.parseDouble(tfLowerE.getText().replaceAll(",", "."));
                 tfUpperE.setText("" + (lowerE + eSize));
             }
             if ((tfLowerN.getText() != null) && !tfLowerN.getText().equals("")) {
                 final double lowerN = Double.parseDouble(tfLowerN.getText().replaceAll(",", "."));
                 tfUpperN.setText("" + (lowerN + nSize));
             }
         }
         addDocumentListeners();
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
                     swms.setName("butler-background");
 
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
                 }
 
                 private XBoundingBox getBoundingBox() {
                     final XBoundingBox currBb = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                                 .getCurrentBoundingBox();
                     final Geometry transformedGeom = CrsTransformer.transformToGivenCrs(currBb.getGeometry(),
                             AlkisConstants.COMMONS.SRS_SERVICE);
                     final XBoundingBox result = new XBoundingBox(transformedGeom.buffer(20));
 
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
                     // set the point feature
 
                     if (cbGeoms.getSelectedItem() instanceof Point) {
                         if (!map.getFeatureCollection().contains(pointFeature)) {
                             map.getFeatureCollection().addFeature(pointFeature);
                         }
                         map.reconsiderFeature(pointFeature);
                     } else {
                         map.getFeatureCollection().removeFeature(pointFeature);
                     }
 
                     final Geometry g = createGeometry();
                     if (g != null) {
                         updateGeomInAllProducts(g);
                         rectangleFeature.setGeometry(g);
                         if (!map.getFeatureCollection().contains(rectangleFeature)) {
                             map.getFeatureCollection().addFeature(rectangleFeature);
                         }
                         map.reconsiderFeature(rectangleFeature);
                     }
 
                     XBoundingBox bb = null;
                     if ((g == null) || (g.getArea() == 0)) {
                         if (pointFeature.getGeometry() != null) {
                             // zoom zo the point feature
                             final Envelope env = pointFeature.getGeometry()
                                         .buffer(MIN_BUFFER / 2)
                                         .getEnvelopeInternal();
                             bb = new XBoundingBox(env.getMinX(),
                                     env.getMinY(),
                                     env.getMaxX(),
                                     env.getMaxY(),
                                     CrsTransformer.createCrsFromSrid(pointFeature.getGeometry().getSRID()),
                                     true);
                         } else {
                             bb = (XBoundingBox)map.getInitialBoundingBox();
                         }
                     } else {
                         // zoom to the rectangle
                         final Envelope env = g.getEnvelopeInternal();
                         if ((env.getHeight() > 100) || (env.getWidth() > 100)) {
                             bb = new XBoundingBox(g);
                         } else {
                             final double buffer = (MIN_BUFFER - Math.max(env.getHeight(), env.getWidth())) / 2;
                             final Envelope e = g.buffer(buffer).getEnvelopeInternal();
                             bb = new XBoundingBox(e.getMinX(),
                                     e.getMinY(),
                                     e.getMaxX(),
                                     e.getMaxY(),
                                     CrsTransformer.createCrsFromSrid(g.getSRID()),
                                     true);
                         }
                     }
 //                    apply diagonal buffer
                     final double width = bb.getX2() - bb.getX1();
                     final double height = bb.getY2() - bb.getY1();
                     final double buffer = Math.sqrt((height * height)
                                     + (width * width)) * 0.1d;
                     bb = new XBoundingBox(bb.getGeometry().buffer(buffer).getEnvelope());
                     map.gotoBoundingBoxWithHistory(bb);
                 }
             };
         if (EventQueue.isDispatchThread()) {
             mapChangeRunnable.run();
         } else {
             EventQueue.invokeLater(mapChangeRunnable);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ArrayList<PredefinedBoxes> getBoxes() {
         return boxes;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  boxes  DOCUMENT ME!
      */
     public void setBoxes(final ArrayList<PredefinedBoxes> boxes) {
         this.boxes = boxes;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void addCloseableTab() {
         final Component tabComp = getTabComponent(true);
         final int number = tbpProducts.getTabCount();
         final String title = "Produkt " + number;
         final int tabPos = tbpProducts.getTabCount() - 1;
         final Butler1ProductPanel productPan = new Butler1ProductPanel();
         if ((rectangleFeature != null) && (rectangleFeature.getGeometry() != null)) {
             productPan.setGeometry(rectangleFeature.getGeometry());
         }
         tbpProducts.insertTab("", null, productPan, title, tabPos);
 //        tbpProducts.insertTab(null, null, comp, null, tabPos);
         final int tabPosNew = tbpProducts.indexOfComponent(productPan);
         tbpProducts.setTabComponentAt(tabPosNew, tabComp);
         tbpProducts.setBorder(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Geometry createGeometry() {
         double lowerE = 0;
         double lowerN = 0;
         double upperE = 0;
         double upperN = 0;
         try {
             lowerE = Double.parseDouble(tfLowerE.getText());
             lowerN = Double.parseDouble(tfLowerN.getText());
             upperE = Double.parseDouble(tfUpperE.getText());
             upperN = Double.parseDouble(tfUpperN.getText());
         } catch (Exception ex) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("error during change map - very likely a double parsing error", ex);
             }
             return null;
         }
         final Coordinate[] coords = new Coordinate[5];
         final Coordinate startCoord = new Coordinate(lowerE, lowerN);
         coords[0] = new Coordinate(lowerE, lowerN);
         final double c1 = lowerN + (Math.abs(upperN - lowerN));
         coords[1] = new Coordinate(lowerE, c1);
         final double c2 = lowerE + (Math.abs(lowerE - upperE));
         coords[2] = new Coordinate(c2, c1);
         coords[3] = new Coordinate(c2, lowerN);
         coords[4] = startCoord;
 
         final int currentSrid = CrsTransformer.extractSridFromCrs(map.getMappingModel().getSrs().getCode());
         final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(
                     PrecisionModel.FLOATING),
                 currentSrid);
 
         final Polygon p = new Polygon(geometryFactory.createLinearRing(coords), null, geometryFactory);
         return p;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private DefaultStyledFeature createRectangleFeature() {
         final DefaultStyledFeature dsf = new DefaultStyledFeature();
         final Geometry geom = createGeometry();
         dsf.setGeometry(geom);
         dsf.setTransparency(0.8F);
         dsf.setFillingPaint(new Color(192, 80, 77, 192));
         return dsf;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   isCloseableTab  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Component getTabComponent(final boolean isCloseableTab) {
         final int number = tbpProducts.getTabCount();
         return getTabComponent(isCloseableTab, number);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   isCloseableTab  DOCUMENT ME!
      * @param   tabNumber       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Component getTabComponent(final boolean isCloseableTab, final int tabNumber) {
         final JPanel tabComp = new JPanel();
         tabComp.setLayout(new java.awt.GridBagLayout());
         java.awt.GridBagConstraints gridBagConstraints;
         tabComp.setOpaque(false);
         // add more space to the top of the component
         tabComp.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
         tabComp.setPreferredSize(new Dimension(120, 22));
 
         final String title = "Produkt " + tabNumber;
         final JLabel label = new JLabel(title);
         label.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/butler/page_white.png")));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         tabComp.add(label, gridBagConstraints);
         // add more space between the label and the button
         label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
         if (isCloseableTab) {
             // tab button
             final JButton button = new JButton("x");
 //            button.setIcon();
             button.setPreferredSize(new Dimension(10, 19));
             button.setToolTipText("close this tab");
             // Make the button looks the same for all Laf's
             button.setUI(new BasicButtonUI());
             // Make it transparent
             button.setContentAreaFilled(false);
             // No need to be focusable
             button.setFocusable(false);
             button.setBorder(null);
             button.setBorderPainted(false);
             button.setRolloverEnabled(true);
             // Close the proper tab by clicking the button
             button.addActionListener(new ActionListener() {
 
                     @Override
                     public void actionPerformed(final ActionEvent ae) {
                         final int i = tbpProducts.indexOfTabComponent(tabComp);
                         if (i != -1) {
                             tbpProducts.remove(i);
                         }
                         if (i == tbpProducts.getSelectedIndex()) {
                             if (i == 0) {
                                 tbpProducts.setSelectedIndex(0);
                             } else {
                                 tbpProducts.setSelectedIndex(i - 1);
                             }
                         }
                         updateTabComponents();
                     }
                 });
             button.addMouseListener(new MouseAdapter() {
 
                     @Override
                     public void mouseEntered(final MouseEvent me) {
                         button.setBorder(new LineBorder(Color.GRAY));
                         button.setBorderPainted(true);
                     }
 
                     @Override
                     public void mouseExited(final MouseEvent me) {
                         button.setBorder(null);
                         button.setBorderPainted(false);
                     }
                 });
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 0;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
             tabComp.add(button, gridBagConstraints);
         }
         return tabComp;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updateTabComponents() {
         // check for the first button if we need to set the clear button
         if (tbpProducts.getTabCount() <= 2) {
             tbpProducts.setTabComponentAt(0, getTabComponent(false, 1));
         } else {
             for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
                 tbpProducts.setTabComponentAt(i, getTabComponent(true, i + 1));
                 tbpProducts.setToolTipTextAt(i, "Produkt " + (i + 1));
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  de  DOCUMENT ME!
      */
     @Override
     public void insertUpdate(final DocumentEvent de) {
         handleLowerTFAction();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  de  DOCUMENT ME!
      */
     @Override
     public void removeUpdate(final DocumentEvent de) {
         handleLowerTFAction();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  de  DOCUMENT ME!
      */
     @Override
     public void changedUpdate(final DocumentEvent de) {
         handleLowerTFAction();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   bp  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isProductConfigurationValid(final ButlerProduct bp) {
         if (bp == null) {
             return false;
         }
         if ((bp.getKey() == null) || bp.getKey().equals("")) {
             return false;
         }
         if ((bp.getFormat() == null) || (bp.getFormat().getKey() == null) || bp.getFormat().getKey().equals("")) {
             return false;
         }
         if ((bp.getResolution() == null) || (bp.getResolution().getKey() == null)
                     || bp.getResolution().getKey().equals("")) {
             return false;
         }
         return true;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void handleUpperTFAction() {
         // check if the geom checkbox need to be adopted:
         if (cbGeoms.getSelectedItem() instanceof Geometry) {
             final Geometry g = (Geometry)cbGeoms.getSelectedItem();
             if (g.isRectangle()) {
                 cbGeoms.setSelectedIndex(0);
             }
         }
 
         // check if the size checkbox needs to be adopted
         final PredefinedBoxes selectedBox = (PredefinedBoxes)cbSize.getSelectedItem();
         if ((selectedBox != null) && !selectedBox.getDisplayName().equals("keine Auswahl")) {
             if ((noSelectionBox != null) && !firstUpperTFChange) {
                 firstUpperTFChange = true;
                 cbSize.setSelectedItem(noSelectionBox);
             }
         }
         changeMap();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void handleLowerTFAction() {
         // check if the geom checkbox need to be adopted:
         cbGeoms.setSelectedIndex(0);
 
         // adjust the point feature to the right position
         final PredefinedBoxes selectedBox = (PredefinedBoxes)cbSize.getSelectedItem();
         double lowerE = 0;
         double lowerN = 0;
         try {
             lowerE = Double.parseDouble(tfLowerE.getText());
             lowerN = Double.parseDouble(tfLowerN.getText());
         } catch (Exception ex) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("error during change map - very likely a double parsing error", ex);
             }
             return;
         }
         final Point p;
         if (isPointCentered && (selectedBox != null) && !selectedBox.getDisplayName().equals("keine Auswahl")) {
             p = factory.createPoint(new Coordinate(
                         lowerE
                                 + (selectedBox.getEastSize() / 2d),
                         lowerN
                                 + (selectedBox.getNorthSize() / 2d)));
 //                tfLowerE.setText(coordFormatter.format(lowerE - (selectedBox.getEastSize() / 2d)));
 //                tfLowerN.setText(coordFormatter.format(lowerN - (selectedBox.getNorthSize() / 2d)));
         } else {
             p = factory.createPoint(new Coordinate(lowerE, lowerN));
         }
         pointFeature.setGeometry(p);
         updatePositionFields();
         changeMap();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  geom  DOCUMENT ME!
      */
     private void updateGeomInAllProducts(final Geometry geom) {
         for (int i = 0; i < (tbpProducts.getTabCount() - 1); i++) {
             final Butler1ProductPanel productPan = (Butler1ProductPanel)tbpProducts.getComponentAt(i);
             productPan.setGeometry(geom);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private DefaultStyledFeature createPointFeature() {
         final DefaultStyledFeature dsf = new DefaultStyledFeature();
         final BufferedImage bi = new BufferedImage(9, 9, BufferedImage.TYPE_4BYTE_ABGR);
         final Graphics2D g = (Graphics2D)bi.getGraphics().create();
         g.setStroke(new BasicStroke(1f));
         g.setColor(Color.black);
         g.drawOval(0, 0, 5, 5);
         final FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(new javax.swing.ImageIcon(
                     getClass().getResource("/de/cismet/cids/custom/nas/icon-circlerecordempty.png")).getImage());
         fas.setSweetSpotX(0.5);
         fas.setSweetSpotY(0.5);
         dsf.setPointAnnotationSymbol(fas);
         return dsf;
     }
 }
