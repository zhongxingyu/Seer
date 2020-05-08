 /***************************************************
  *
  * cismet GmbH, Saarbruecken, Germany
  *
  *              ... and it just works.
  *
  ****************************************************/
 /*
  * PrintingSettingsWidget.java
  *
  * Created on 10. Juli 2006, 14:06
  */
 package de.cismet.cids.custom.objectrenderer.utils.alkis.print;
 
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 
 import java.awt.Color;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.custom.objectrenderer.utils.AlphanumComparator;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisProductDescription;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.CidsBeanDropListener;
 import de.cismet.cids.navigator.utils.CidsBeanDropTarget;
 
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.util.FormatToRealWordCalculator;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.HttpDownload;
 import java.net.URL;
 
 /**
  * DOCUMENT ME!
  *
  * @author   stefan
  * @version  $Revision$, $Date$
  */
 public class AlkisPrintingSettingsWidget extends javax.swing.JDialog implements CidsBeanDropListener {
 
     //~ Static fields/initializers ---------------------------------------------
     private static final String ALKIS_LANDPARCEL_TABLE = "ALKIS_LANDPARCEL";
     //~ Instance fields --------------------------------------------------------
 // private static final ProductLayout[] LAYOUTS = ProductLayout.values();
 // private static final ProductTyp[] TYPES = ProductTyp.values();
 // private static final LiegenschaftskarteProduct[] PRODUCTS = LiegenschaftskarteProduct.values();
 // private static final Integer[] MASSSTAEBE = new Integer[]{500, 1000, 2000, 5000};
     //
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private final MappingComponent mappingComponent;
     private final DefaultListModel flurstueckListModel;
     private final AlkisPrintListener mapPrintListener;
     private Geometry allLandparcelGeometryUnion;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnRemove;
     private javax.swing.JComboBox cbClazz;
     private javax.swing.JComboBox cbFormat;
     private javax.swing.JComboBox cbProduct;
     private javax.swing.JComboBox cbScales;
     private javax.swing.JCheckBox chkRotation;
     private javax.swing.JButton cmdCancel;
     private javax.swing.JButton cmdOk;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JList lstFlurstuecke;
     private javax.swing.JPanel panDesc;
     private javax.swing.JPanel panSettings;
     private javax.swing.JScrollPane scpAdditionalText;
     private javax.swing.JScrollPane scpFlurstuecke;
     private javax.swing.JTextArea taAdditionalText;
     private javax.swing.JTextField txtAuftragsnummer;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
     /**
      * Creates new form PrintingSettingsWidget.
      *
      * @param  modal             DOCUMENT ME!
      * @param  mappingComponent  DOCUMENT ME!
      */
     public AlkisPrintingSettingsWidget(final boolean modal, final MappingComponent mappingComponent) {
         super(StaticSwingTools.getParentFrame(mappingComponent), modal);
         this.flurstueckListModel = new DefaultListModel();
         initComponents();
         getRootPane().setDefaultButton(cmdOk);
         cbClazz.setModel(getProductClassModel());
         cbClazz.setSelectedIndex(cbClazz.getModel().getSize() - 1);
         cbProduct.setSelectedIndex(cbProduct.getModel().getSize() - 1);
         this.panDesc.setBackground(new Color(216, 228, 248));
         this.mappingComponent = mappingComponent;
         // enable D&D
         new CidsBeanDropTarget(this);
         // init PrintListener
         this.mapPrintListener = new AlkisPrintListener(mappingComponent, this);
         this.mappingComponent.addInputListener(MappingComponent.ALKIS_PRINT, mapPrintListener);
 //        updateFormatProposal();
     }
 
     //~ Methods ----------------------------------------------------------------
     @Override
     public void setVisible(final boolean b) {
         flurstueckListModel.clear();
         Collection<CidsBean> beansToPrint = getAlkisFlurstueckBeansInMap();
         if (beansToPrint.isEmpty()) {
             beansToPrint = getAlkisFlurstueckBeansFromTreeSelection();
         } else if (beansToPrint.size() > 1) {
             final int dialogResult = JOptionPane.showConfirmDialog(
                     this,
                     "Sollen alle Flurstückeobjekte der Karte gedruckt werden?",
                     "Flurstücke in Druckauswahl übernehmen",
                     JOptionPane.YES_NO_OPTION);
             if (JOptionPane.NO_OPTION == dialogResult) {
                 beansToPrint.clear();
             }
         }
 
         for (final CidsBean currentBean : beansToPrint) {
             flurstueckListModel.addElement(currentBean);
         }
 
         updateFormatProposal();
         syncOkButtonWithListStatus();
         super.setVisible(b);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ComboBoxModel getProductClassModel() {
         final Set<String> classes = new HashSet<String>();
 //        log.fatal(AlkisCommons.Products.ALKIS_MAP_PRODUCTS);
 //        log.fatal(AlkisCommons.Products.ALKIS_FORMATS);
 //        log.fatal(AlkisCommons.Products.FLURSTUECKSNACHWEIS_PDF);
 //        log.fatal(AlkisCommons.USER);
         for (final AlkisProductDescription product : AlkisUtils.PRODUCTS.ALKIS_MAP_PRODUCTS) {
             classes.add(product.getClazz());
         }
         return new DefaultComboBoxModel(classes.toArray());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ComboBoxModel getProductTypeModel() {
         final String clazz = String.valueOf(cbClazz.getSelectedItem());
         final Set<String> prodSet = new HashSet<String>();
         final List<String> typesOrdered = new ArrayList<String>();
         for (final AlkisProductDescription product : AlkisUtils.PRODUCTS.ALKIS_MAP_PRODUCTS) {
             if (clazz.equals(product.getClazz())) {
                 if (prodSet.add(product.getType())) {
                     typesOrdered.add(product.getType());
                 }
             }
         }
         return new DefaultComboBoxModel(typesOrdered.toArray());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ComboBoxModel[] getProductDetailModels() {
         final String clazz = String.valueOf(cbClazz.getSelectedItem());
         final String type = String.valueOf(cbProduct.getSelectedItem());
         final ComboBoxModel[] result = new ComboBoxModel[2];
         final Set<String> prodScale = new TreeSet<String>(AlphanumComparator.getInstance());
         final Set<String> prodLayout = new HashSet<String>();
         final List<LayoutMetaInfo> prodLayoutOrdered = new ArrayList<LayoutMetaInfo>();
         for (final AlkisProductDescription product : AlkisUtils.PRODUCTS.ALKIS_MAP_PRODUCTS) {
             if (clazz.equals(product.getClazz()) && type.equals(product.getType())) {
                 prodScale.add(product.getMassstab());
                 if (prodLayout.add(product.getDinFormat())) {
                     prodLayoutOrdered.add(new LayoutMetaInfo(
                             product.getDinFormat(),
                             product.getWidth(),
                             product.getHeight()));
                 }
             }
         }
         result[0] = new DefaultComboBoxModel(prodLayoutOrdered.toArray());
         result[1] = new DefaultComboBoxModel(prodScale.toArray());
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private AlkisProductDescription getSelectedProduct() {
         final String clazz = String.valueOf(cbClazz.getSelectedItem());
         final String type = String.valueOf(cbProduct.getSelectedItem());
         final String scale = String.valueOf(cbScales.getSelectedItem());
         final String layout = String.valueOf(cbFormat.getSelectedItem());
         for (final AlkisProductDescription product : AlkisUtils.PRODUCTS.ALKIS_MAP_PRODUCTS) {
             if (clazz.equals(product.getClazz()) && type.equals(product.getType())
                     && scale.equals(product.getMassstab()) && layout.equals(product.getDinFormat())) {
                 return product;
             }
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Collection<CidsBean> getAlkisFlurstueckBeansInMap() {
         final Collection<CidsBean> result = TypeSafeCollections.newArrayList();
         for (final Feature feature : mappingComponent.getPFeatureHM().keySet()) {
             if (feature instanceof CidsFeature) {
                 final CidsFeature cidsFeature = (CidsFeature) feature;
                 final MetaObject metaObj = cidsFeature.getMetaObject();
                 final MetaClass mc = metaObj.getMetaClass();
                 if (ALKIS_LANDPARCEL_TABLE.equalsIgnoreCase(mc.getTableName())) {
                     result.add(metaObj.getBean());
                 }
             }
         }
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Collection<CidsBean> getAlkisFlurstueckBeansFromTreeSelection() {
         final Collection<CidsBean> result = TypeSafeCollections.newArrayList();
         final Collection<?> nodes = ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNodes();
         if (nodes != null) {
             for (final Object nodeObj : nodes) {
                 if (nodeObj instanceof ObjectTreeNode) {
                     try {
                         final ObjectTreeNode metaTreeNode = (ObjectTreeNode) nodeObj;
                         final MetaClass mc = metaTreeNode.getMetaClass();
                         if (ALKIS_LANDPARCEL_TABLE.equalsIgnoreCase(mc.getTableName())) {
                             result.add(metaTreeNode.getMetaObject().getBean());
                         }
                     } catch (Exception ex) {
                         log.error(ex, ex);
                     }
                 }
             }
         }
         return result;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         panDesc = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jSeparator2 = new javax.swing.JSeparator();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jSeparator3 = new javax.swing.JSeparator();
         panSettings = new javax.swing.JPanel();
         jLabel6 = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jSeparator4 = new javax.swing.JSeparator();
         btnRemove = new javax.swing.JButton();
         scpFlurstuecke = new javax.swing.JScrollPane();
         lstFlurstuecke = new javax.swing.JList();
         jLabel11 = new javax.swing.JLabel();
         scpAdditionalText = new javax.swing.JScrollPane();
         taAdditionalText = new javax.swing.JTextArea();
         jLabel10 = new javax.swing.JLabel();
         cbScales = new javax.swing.JComboBox();
         cbFormat = new javax.swing.JComboBox();
         jLabel8 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         chkRotation = new javax.swing.JCheckBox();
         cbProduct = new javax.swing.JComboBox();
         jLabel9 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         cbClazz = new javax.swing.JComboBox();
         jLabel4 = new javax.swing.JLabel();
         txtAuftragsnummer = new javax.swing.JTextField();
         jPanel1 = new javax.swing.JPanel();
         cmdCancel = new javax.swing.JButton();
         cmdOk = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setAlwaysOnTop(true);
         setMinimumSize(new java.awt.Dimension(750, 500));
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         panDesc.setBackground(java.awt.SystemColor.inactiveCaptionText);
         panDesc.setMaximumSize(new java.awt.Dimension(150, 150));
         panDesc.setMinimumSize(new java.awt.Dimension(150, 150));
         panDesc.setPreferredSize(new java.awt.Dimension(150, 150));
         panDesc.setLayout(new java.awt.GridBagLayout());
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel1.setText("Schritte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
         panDesc.add(jLabel1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 319;
         gridBagConstraints.ipady = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panDesc.add(jSeparator2, gridBagConstraints);
 
         jLabel2.setText("1. Einstellungen");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
         panDesc.add(jLabel2, gridBagConstraints);
 
         jLabel3.setText("2. Druckbereich");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
         panDesc.add(jLabel3, gridBagConstraints);
 
         jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/frameprint.png"))); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.ipadx = -8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 7);
         panDesc.add(jLabel5, gridBagConstraints);
 
         jSeparator3.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 339;
         gridBagConstraints.ipady = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panDesc.add(jSeparator3, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 127;
         gridBagConstraints.ipady = 200;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         getContentPane().add(panDesc, gridBagConstraints);
 
         panSettings.setMaximumSize(new java.awt.Dimension(425, 300));
         panSettings.setMinimumSize(new java.awt.Dimension(425, 300));
         panSettings.setPreferredSize(new java.awt.Dimension(425, 300));
         panSettings.setLayout(new java.awt.GridBagLayout());
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel6.setText("1. Einstellungen");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
         panSettings.add(jLabel6, gridBagConstraints);
 
         jSeparator1.setMaximumSize(new java.awt.Dimension(0, 0));
         jSeparator1.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 411;
         gridBagConstraints.ipady = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(6, 5, 0, 5);
         panSettings.add(jSeparator1, gridBagConstraints);
 
         jSeparator4.setMaximumSize(new java.awt.Dimension(0, 0));
         jSeparator4.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 421;
         gridBagConstraints.ipady = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         panSettings.add(jSeparator4, gridBagConstraints);
 
         btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/editors/edit_remove_mini.png"))); // NOI18N
         btnRemove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(btnRemove, gridBagConstraints);
 
         scpFlurstuecke.setMinimumSize(new java.awt.Dimension(250, 110));
         scpFlurstuecke.setPreferredSize(new java.awt.Dimension(250, 110));
 
         lstFlurstuecke.setModel(flurstueckListModel);
         scpFlurstuecke.setViewportView(lstFlurstuecke);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(scpFlurstuecke, gridBagConstraints);
 
         jLabel11.setText("Flurstücke:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(jLabel11, gridBagConstraints);
 
         scpAdditionalText.setMinimumSize(new java.awt.Dimension(250, 110));
         scpAdditionalText.setPreferredSize(new java.awt.Dimension(250, 110));
 
         taAdditionalText.setColumns(20);
         taAdditionalText.setRows(5);
         scpAdditionalText.setViewportView(taAdditionalText);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(scpAdditionalText, gridBagConstraints);
 
         jLabel10.setText("Zusätzlicher Text:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(jLabel10, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(cbScales, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(cbFormat, gridBagConstraints);
 
         jLabel8.setText("Maßstab:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(jLabel8, gridBagConstraints);
 
         jLabel7.setText("Format:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(jLabel7, gridBagConstraints);
 
         chkRotation.setText("Drehwinkel vorschlagen:");
         chkRotation.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(chkRotation, gridBagConstraints);
 
         cbProduct.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbProductActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(cbProduct, gridBagConstraints);
 
         jLabel9.setText("Produkt:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(jLabel9, gridBagConstraints);
 
         jLabel12.setText("Produktklasse:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(jLabel12, gridBagConstraints);
 
         cbClazz.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbClazzActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(cbClazz, gridBagConstraints);
 
         jLabel4.setText("Auftragsnummer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(jLabel4, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSettings.add(txtAuftragsnummer, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         getContentPane().add(panSettings, gridBagConstraints);
 
         cmdCancel.setMnemonic('A');
         cmdCancel.setText("Abbrechen");
         cmdCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdCancelActionPerformed(evt);
             }
         });
         jPanel1.add(cmdCancel);
 
         cmdOk.setMnemonic('O');
         cmdOk.setText("Ok");
         cmdOk.setEnabled(false);
         cmdOk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdOkActionPerformed(evt);
             }
         });
         jPanel1.add(cmdOk);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         getContentPane().add(jPanel1, gridBagConstraints);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdOkActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOkActionPerformed
         try {
             final AlkisProductDescription selectedProduct = getSelectedProduct();
             mapPrintListener.init(selectedProduct, allLandparcelGeometryUnion, chkRotation.isSelected());
             dispose();
         } catch (Exception e) {
             log.error("Fehler beim Verarbeiten der Druckeinstellungen", e);
         }
     }//GEN-LAST:event_cmdOkActionPerformed
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCancelActionPerformed
         dispose();
     }//GEN-LAST:event_cmdCancelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
         final int[] sel = lstFlurstuecke.getSelectedIndices();
         for (int i = sel.length; --i >= 0;) {
             flurstueckListModel.removeElementAt(sel[i]);
         }
         updateFormatProposal();
         syncOkButtonWithListStatus();
     }//GEN-LAST:event_btnRemoveActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbClazzActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbClazzActionPerformed
         cbProduct.setModel(getProductTypeModel());
         cbProductActionPerformed(null);
     }//GEN-LAST:event_cbClazzActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbProductActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProductActionPerformed
         final ComboBoxModel[] models = getProductDetailModels();
         cbFormat.setModel(models[0]);
         cbScales.setModel(models[1]);
     }//GEN-LAST:event_cbProductActionPerformed
 
     @Override
     public void beansDropped(final ArrayList<CidsBean> beans) {
         if (beans != null) {
             for (final CidsBean bean : beans) {
                 if (ALKIS_LANDPARCEL_TABLE.equals(bean.getMetaObject().getMetaClass().getTableName())) {
                     if (!flurstueckListModel.contains(bean)) {
                         flurstueckListModel.addElement(bean);
                     }
                 }
             }
             updateFormatProposal();
             syncOkButtonWithListStatus();
         }
     }
 
     /**
      * private boolean doCurrentLandparcelsFitNordedToSelectedFormatAndScale() { BoundingBox allGeomBB = new
      * BoundingBox(allLandparcelGeometryUnion); return doesBoundingBoxFitIntoLayout(allGeomBB, (ProduktLayout)
      * cbFormat.getSelectedItem(), (Integer) cbScales.getSelectedItem()); }.
      */
     private void updateFormatProposal() {
         this.allLandparcelGeometryUnion = unionAllLandparcelGeometries();
         if (allLandparcelGeometryUnion != null) {
             final BoundingBox allGeomBB = new BoundingBox(allLandparcelGeometryUnion);
             // current: erst auf passendes format durchtesten, dann massstaebe
 // String clazz = String.valueOf(cbClazz.getSelectedItem());
 // String type = String.valueOf(cbProduct.getSelectedItem());
             for (int j = 0; j < cbScales.getModel().getSize(); ++j) {
                 for (int i = 0; i < cbFormat.getModel().getSize(); ++i) {
                     final LayoutMetaInfo currentLayout = (LayoutMetaInfo) cbFormat.getItemAt(i);
                     final Integer currentMassstab = Integer.parseInt(String.valueOf(cbScales.getItemAt(j)));
                     if (doesBoundingBoxFitIntoLayout(
                             allGeomBB,
                             currentLayout.width,
                             currentLayout.heigth,
                             currentMassstab)) {
                         cbFormat.setSelectedIndex(i);
                         cbScales.setSelectedIndex(j);
                         chkRotation.setSelected(false);
                         return;
                     }
                 }
             }
             chkRotation.setSelected(true);
             String formatHint;
             if (allGeomBB.getWidth() >= allGeomBB.getHeight()) {
                 formatHint = "Hoch";
                 cbFormat.setSelectedIndex(cbFormat.getModel().getSize() - 1);
             } else {
                 cbFormat.setSelectedIndex(cbFormat.getModel().getSize() - 2);
                 formatHint = "Quer";
             }
 
             for (int i = cbFormat.getModel().getSize(); --i >= 0;) {
                 if (String.valueOf(cbFormat.getItemAt(i)).equals(formatHint)) {
                     cbFormat.setSelectedIndex(i);
                     break;
                 }
             }
 
             cbScales.setSelectedIndex(cbScales.getModel().getSize() - 1);
         } else {
             chkRotation.setSelected(false);
             cbFormat.setSelectedIndex(cbFormat.getModel().getSize() - 1);
             cbScales.setSelectedIndex(cbScales.getModel().getSize() - 1);
         }
     }
 
    /**
     * DOCUMENT ME!
     *
     * @param  center         DOCUMENT ME!
     * @param  rotationAngle  DOCUMENT ME!
     */
    public void createProduct(final Point center, final double rotationAngle) {
        if (flurstueckListModel.size() > 0) {
            final String landParcelCode = AlkisUtils.getLandparcelCodeFromParcelBeanObject(flurstueckListModel.get(
                    0));
            final AlkisProductDescription selectedProduct = getSelectedProduct();
            AlkisUtils.PRODUCTS.productKarte(
                    landParcelCode,
                    selectedProduct,
                    toInt(rotationAngle),
                    toInt(center.getX()),
                    toInt(center.getY()),
                    taAdditionalText.getText(),
                    txtAuftragsnummer.getText(),
                    false);
        }
    }
     
     /**
      * Adds the selected product to the DownloadManager.
      *
      * @param  center         DOCUMENT ME!
      * @param  rotationAngle  DOCUMENT ME!
      */
     public void downloadProduct(final Point center, final double rotationAngle) {
         if(flurstueckListModel.size() <= 0) {
             return;
         }
 
         final String landParcelCode = AlkisUtils.getLandparcelCodeFromParcelBeanObject(flurstueckListModel.get(
                 0));
         final AlkisProductDescription selectedProduct = getSelectedProduct();
         URL url = null;
         try {
             url = AlkisUtils.PRODUCTS.productKarteUrl(
                 landParcelCode,
                 selectedProduct,
                 toInt(rotationAngle),
                 toInt(center.getX()),
                 toInt(center.getY()),
                 taAdditionalText.getText(),
                txtAuftragsnummer.getText(),
                 false);
             
             if (url != null) {
                 if (!DownloadManagerDialog.showAskingForUserTitle(StaticSwingTools.getParentFrame(this))) {
                     return;
                 }
 
                 final HttpDownload download = new HttpDownload(url, "", DownloadManagerDialog.getJobname(), "ALKIS-Druck", landParcelCode.replaceAll("\\/", "-"), ".pdf");
                 DownloadManager.instance().add(download);
             }
         } catch(Exception e) {
             ObjectRendererUtils.showExceptionWindowToUser(
                     "Fehler beim Aufruf des Produkts: " + selectedProduct,
                     e,
                     AlkisPrintingSettingsWidget.this);
             log.error(e);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   input  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private int toInt(final double input) {
         return Double.valueOf(input).intValue();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Geometry unionAllLandparcelGeometries() {
         Geometry allGeomUnion = null;
         for (int i = flurstueckListModel.size(); --i >= 0;) {
             final Object currentLandparcelObj = flurstueckListModel.get(i);
             if (currentLandparcelObj instanceof CidsBean) {
                 final CidsBean currentLandparcelBean = (CidsBean) currentLandparcelObj;
                 final Object currentGeomObj = currentLandparcelBean.getProperty("geometrie.geo_field");
                 if (currentGeomObj instanceof Geometry) {
                     final Geometry currentGeom = (Geometry) currentGeomObj;
                     if (allGeomUnion == null) {
                         allGeomUnion = currentGeom;
                     } else {
                         allGeomUnion = currentGeom.union(allGeomUnion);
                     }
                 }
             }
         }
 //        StyledFeature debug = new DefaultStyledFeature();
 //        debug.setGeometry(allGeomUnion.getEnvelope());
 //        debug.setFillingPaint(Color.RED);
 //        mappingComponent.getFeatureCollection().addFeature(debug);
         return allGeomUnion;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   box     DOCUMENT ME!
      * @param   width   DOCUMENT ME!
      * @param   height  DOCUMENT ME!
      * @param   scale   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean doesBoundingBoxFitIntoLayout(final BoundingBox box,
             final int width,
             final int height,
             final double scale) {
         final double realWorldLayoutWidth = FormatToRealWordCalculator.toRealWorldValue(width, scale);
         final double realWorldLayoutHeigth = FormatToRealWordCalculator.toRealWorldValue(height, scale);
         return (realWorldLayoutWidth >= box.getWidth()) && (realWorldLayoutHeigth >= box.getHeight());
     }
 
     /**
      * DOCUMENT ME!
      */
     private void syncOkButtonWithListStatus() {
         cmdOk.setEnabled(flurstueckListModel.size() > 0);
     }
 
     //~ Inner Classes ----------------------------------------------------------
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     static final class LayoutMetaInfo {
 
         //~ Instance fields ----------------------------------------------------
         String layoutDesc;
         String layoutCode;
         int width;
         int heigth;
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new LayoutMetaInfo object.
          *
          * @param  layoutDesc  DOCUMENT ME!
          * @param  width       DOCUMENT ME!
          * @param  heigth      DOCUMENT ME!
          */
         public LayoutMetaInfo(final String layoutDesc, final int width, final int heigth) {
             this.layoutDesc = layoutDesc;
             this.width = width;
             this.heigth = heigth;
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         public String toString() {
             return layoutDesc;
         }
     }
 }
