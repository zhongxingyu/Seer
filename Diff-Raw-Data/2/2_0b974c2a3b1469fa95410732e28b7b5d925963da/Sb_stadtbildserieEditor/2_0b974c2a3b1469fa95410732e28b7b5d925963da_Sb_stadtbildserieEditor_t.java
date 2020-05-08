 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
 import edu.umd.cs.piccolo.event.PInputEvent;
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.lang.ref.SoftReference;
 
 import java.net.URL;
 
 import java.sql.Timestamp;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 import javax.swing.Timer;
 
 import de.cismet.cids.client.tools.DevelopmentTools;
 
 import de.cismet.cids.custom.objectrenderer.utils.AbstractJasperReportPrint;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.wunda_blau.StadtbildJasperReportPrint;
 import de.cismet.cids.custom.utils.TifferDownload;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.DefaultBindableJCheckBox;
 import de.cismet.cids.editors.DefaultCustomObjectEditor;
 import de.cismet.cids.editors.FastBindableReferenceCombo;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.TitleComponentProvider;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 
 import static de.cismet.cids.custom.objecteditors.wunda_blau.MauerEditor.adjustScale;
 
 /**
  * DOCUMENT ME!
  *
  * @author   Gilles Baatz
  * @version  $Revision$, $Date$
  */
 public class Sb_stadtbildserieEditor extends JPanel implements CidsBeanRenderer, TitleComponentProvider {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String REPORT_FILE = "/de/cismet/cids/custom/wunda_blau/res/StadtbildA4H.jasper";
     private static final ImageIcon FOLDER_ICON = new ImageIcon(MauerEditor.class.getResource(
                 "/de/cismet/cids/custom/objecteditors/wunda_blau/inode-directory.png"));
 
     private static final ImageIcon ERROR_ICON = new ImageIcon(MauerEditor.class.getResource(
                 "/de/cismet/cids/custom/objecteditors/wunda_blau/file-broken.png"));
 
     private static final int CACHE_SIZE = 20;
 
     private static final Map<String, SoftReference<BufferedImage>> IMAGE_CACHE =
         new LinkedHashMap<String, SoftReference<BufferedImage>>(CACHE_SIZE) {
 
             @Override
             protected boolean removeEldestEntry(final Map.Entry<String, SoftReference<BufferedImage>> eldest) {
                 return size() >= CACHE_SIZE;
             }
         };
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Sb_stadtbildserieEditor.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private CidsBean cidsBean;
     private String title;
     private final Converter<Timestamp, Date> timeStampConverter = new Converter<Timestamp, Date>() {
 
             @Override
             public Date convertForward(final Timestamp value) {
                 try {
                     if (value != null) {
                         return new java.util.Date(value.getTime());
                     } else {
                         return null;
                     }
                 } catch (Exception ex) {
                     LOG.fatal(ex);
                     return new java.util.Date(System.currentTimeMillis());
                 }
             }
 
             @Override
             public Timestamp convertReverse(final Date value) {
                 try {
                     if (value != null) {
                         return new Timestamp(value.getTime());
                     } else {
                         return null;
                     }
                 } catch (Exception ex) {
                     LOG.fatal(ex);
                     return new Timestamp(System.currentTimeMillis());
                 }
             }
         };
 
     private final PropertyChangeListener listRepaintListener = new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 lstBildnummern.repaint();
             }
         };
 
     private final AbstractAttributeRepresentationFormater strasseFormater =
         new AbstractAttributeRepresentationFormater() {
 
             @Override
             public final String getRepresentation() {
                 return String.valueOf(getAttribute("name"));
             }
         };
 
     private CidsBean fotoCidsBean;
 
     private BufferedImage image;
     private boolean resizeListenerEnabled;
     private final Timer timer;
     private Sb_stadtbildserieEditor.ImageResizeWorker currentResizeWorker;
     private MappingComponent map;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private de.cismet.cids.editors.FastBindableReferenceCombo bcbStrasse;
     private javax.swing.JButton btnAddImageNumber;
     private javax.swing.JButton btnAddSuchwort;
     private javax.swing.JButton btnCombineGeometries;
     private javax.swing.JButton btnDownloadHighResImage;
     private javax.swing.JButton btnNextImg;
     private javax.swing.JButton btnPrevImg;
     private javax.swing.JButton btnRemoveImageNumber;
     private javax.swing.JButton btnRemoveSuchwort;
     private javax.swing.JCheckBox chbPruefen;
     private de.cismet.cids.editors.DefaultBindableReferenceCombo dbcAuftraggeber;
     private de.cismet.cids.editors.DefaultBindableReferenceCombo dbcFilmart;
     private de.cismet.cids.editors.DefaultBindableReferenceCombo dbcFotograf;
     private de.cismet.cids.editors.DefaultBindableReferenceCombo dbcOrt;
     private de.cismet.cids.editors.DefaultBindableJTextField defaultBindableJTextField1;
     private de.cismet.cids.editors.DefaultBindableReferenceCombo defaultBindableReferenceCombo2;
     private de.cismet.cids.editors.DefaultBindableReferenceCombo defaultBindableReferenceCombo3;
     private de.cismet.cismap.cids.geometryeditor.DefaultCismapGeometryComboBoxEditor
         defaultCismapGeometryComboBoxEditor1;
     private javax.swing.Box.Filler filler1;
     private javax.swing.Box.Filler filler2;
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextArea jTextArea2;
     private org.jdesktop.swingx.JXDatePicker jXDatePicker1;
     private org.jdesktop.swingx.JXBusyLabel lblBusy;
     private javax.swing.JLabel lblDescAufnahmedatum;
     private javax.swing.JLabel lblDescAuftraggeber;
     private javax.swing.JLabel lblDescBildnummer;
     private javax.swing.JLabel lblDescBildtyp;
     private javax.swing.JLabel lblDescFilmart;
     private javax.swing.JLabel lblDescFotograf;
     private javax.swing.JLabel lblDescGeometrie;
     private javax.swing.JLabel lblDescInfo;
     private javax.swing.JLabel lblDescLagerort;
     private javax.swing.JLabel lblDescOrt;
     private javax.swing.JLabel lblDescStrasse;
     private javax.swing.JLabel lblDescSuchworte;
     private javax.swing.JLabel lblGeomAus;
     private javax.swing.JLabel lblPicture;
     private javax.swing.JLabel lblPrint;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JLabel lblVorschau;
     private javax.swing.JList lstBildnummern;
     private javax.swing.JList lstSuchworte;
     private javax.swing.JPanel panContent;
     private javax.swing.JPanel panDetails;
     private javax.swing.JPanel panDetails1;
     private javax.swing.JPanel panDetails3;
     private javax.swing.JPanel panDetails4;
     private javax.swing.JPanel panPrintButton;
     private javax.swing.JPanel panTitle;
     private javax.swing.JPanel panTitleString;
     private javax.swing.JPanel pnlCtrlBtn;
     private javax.swing.JPanel pnlCtrlButtons;
     private javax.swing.JPanel pnlCtrlButtons1;
     private javax.swing.JPanel pnlFoto;
     private javax.swing.JPanel pnlMap;
     private de.cismet.tools.gui.RoundedPanel pnlVorschau;
     private de.cismet.tools.gui.RoundedPanel roundedPanel1;
     private de.cismet.tools.gui.RoundedPanel roundedPanel2;
     private de.cismet.tools.gui.RoundedPanel roundedPanel3;
     private de.cismet.tools.gui.RoundedPanel roundedPanel4;
     private de.cismet.tools.gui.RoundedPanel roundedPanel6;
     private de.cismet.tools.gui.RoundedPanel roundedPanel7;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel1;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel2;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel3;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel4;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel5;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel7;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel8;
     private de.cismet.cids.custom.objectrenderer.converter.SQLDateToStringConverter sqlDateToStringConverter;
     private de.cismet.cids.editors.converters.SqlDateToUtilDateConverter sqlDateToUtilDateConverter;
     private javax.swing.JToggleButton tbtnIsPreviewImage;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form Arc_stadtbildRenderer.
      */
     public Sb_stadtbildserieEditor() {
         initComponents();
         jScrollPane5.getViewport().setOpaque(false);
         title = "";
         ObjectRendererUtils.decorateComponentWithMouseOverCursorChange(
             lblPrint,
             Cursor.HAND_CURSOR,
             Cursor.DEFAULT_CURSOR);
         map = new MappingComponent();
         pnlMap.setLayout(new BorderLayout());
         pnlMap.add(map, BorderLayout.CENTER);
 
         timer = new Timer(300, new ActionListener() {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         if (resizeListenerEnabled) {
                             if (currentResizeWorker != null) {
                                 currentResizeWorker.cancel(true);
                             }
                             currentResizeWorker = new Sb_stadtbildserieEditor.ImageResizeWorker();
                             currentResizeWorker.execute();
                         }
                     }
                 });
         timer.setRepeats(false);
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
 
         sqlDateToUtilDateConverter = new de.cismet.cids.editors.converters.SqlDateToUtilDateConverter();
         sqlDateToStringConverter = new de.cismet.cids.custom.objectrenderer.converter.SQLDateToStringConverter();
         panTitle = new javax.swing.JPanel();
         panTitleString = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         panPrintButton = new javax.swing.JPanel();
         lblPrint = new javax.swing.JLabel();
         roundedPanel1 = new de.cismet.tools.gui.RoundedPanel();
         semiRoundedPanel1 = new de.cismet.tools.gui.SemiRoundedPanel();
         jScrollPane5 = new javax.swing.JScrollPane();
         jPanel3 = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         roundedPanel2 = new de.cismet.tools.gui.RoundedPanel();
         semiRoundedPanel3 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel1 = new javax.swing.JLabel();
         panContent = new RoundedPanel();
         pnlCtrlButtons1 = new javax.swing.JPanel();
         btnAddSuchwort = new javax.swing.JButton();
         btnRemoveSuchwort = new javax.swing.JButton();
         pnlCtrlButtons = new javax.swing.JPanel();
         btnAddImageNumber = new javax.swing.JButton();
         btnRemoveImageNumber = new javax.swing.JButton();
         lblDescBildnummer = new javax.swing.JLabel();
         lblDescLagerort = new javax.swing.JLabel();
         lblDescAufnahmedatum = new javax.swing.JLabel();
         lblDescInfo = new javax.swing.JLabel();
         lblDescBildtyp = new javax.swing.JLabel();
         lblDescSuchworte = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         lstBildnummern = new javax.swing.JList();
         jScrollPane2 = new javax.swing.JScrollPane();
         lstSuchworte = new javax.swing.JList();
         defaultBindableReferenceCombo2 = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
         defaultBindableReferenceCombo3 = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jXDatePicker1 = new org.jdesktop.swingx.JXDatePicker();
         roundedPanel3 = new de.cismet.tools.gui.RoundedPanel();
         semiRoundedPanel4 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel2 = new javax.swing.JLabel();
         panDetails = new RoundedPanel();
         lblDescFilmart = new javax.swing.JLabel();
         lblDescFotograf = new javax.swing.JLabel();
         lblDescAuftraggeber = new javax.swing.JLabel();
         dbcAuftraggeber = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
         dbcFotograf = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
         dbcFilmart = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
         roundedPanel4 = new de.cismet.tools.gui.RoundedPanel();
         semiRoundedPanel5 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel3 = new javax.swing.JLabel();
         panDetails1 = new RoundedPanel();
         lblDescGeometrie = new javax.swing.JLabel();
         lblDescOrt = new javax.swing.JLabel();
         lblDescStrasse = new javax.swing.JLabel();
         dbcOrt = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
         jLabel7 = new javax.swing.JLabel();
         defaultBindableJTextField1 = new de.cismet.cids.editors.DefaultBindableJTextField();
         lblGeomAus = new javax.swing.JLabel();
         btnCombineGeometries = new javax.swing.JButton();
         defaultCismapGeometryComboBoxEditor1 =
             new de.cismet.cismap.cids.geometryeditor.DefaultCismapGeometryComboBoxEditor();
         bcbStrasse = new FastBindableReferenceCombo(
                 "select s.strassenschluessel,s.name from strasse s",
                 strasseFormater,
                 new String[] { "NAME" });
         jPanel2 = new javax.swing.JPanel();
         pnlVorschau = new de.cismet.tools.gui.RoundedPanel();
         semiRoundedPanel2 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblVorschau = new javax.swing.JLabel();
         pnlFoto = new javax.swing.JPanel();
         lblBusy = new org.jdesktop.swingx.JXBusyLabel(new Dimension(75, 75));
         jPanel4 = new javax.swing.JPanel();
         lblPicture = new javax.swing.JLabel();
         pnlCtrlBtn = new javax.swing.JPanel();
         btnDownloadHighResImage = new javax.swing.JButton();
         btnPrevImg = new javax.swing.JButton();
         btnNextImg = new javax.swing.JButton();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 0));
         filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 0));
         tbtnIsPreviewImage = new javax.swing.JToggleButton();
         roundedPanel7 = new de.cismet.tools.gui.RoundedPanel();
         semiRoundedPanel8 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel6 = new javax.swing.JLabel();
         panDetails4 = new RoundedPanel();
         pnlMap = new javax.swing.JPanel();
         roundedPanel6 = new de.cismet.tools.gui.RoundedPanel();
         semiRoundedPanel7 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel5 = new javax.swing.JLabel();
         panDetails3 = new RoundedPanel();
         chbPruefen = new DefaultBindableJCheckBox();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTextArea2 = new javax.swing.JTextArea();
         jButton1 = new javax.swing.JButton();
 
         panTitle.setOpaque(false);
         panTitle.setLayout(new java.awt.BorderLayout());
 
         panTitleString.setOpaque(false);
         panTitleString.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText("TITLE");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panTitleString.add(lblTitle, gridBagConstraints);
 
         panTitle.add(panTitleString, java.awt.BorderLayout.CENTER);
 
         panPrintButton.setOpaque(false);
         panPrintButton.setLayout(new java.awt.GridBagLayout());
 
         lblPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/printer.png"))); // NOI18N
         lblPrint.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lblPrintMouseClicked(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panPrintButton.add(lblPrint, gridBagConstraints);
 
         panTitle.add(panPrintButton, java.awt.BorderLayout.EAST);
 
         roundedPanel1.add(semiRoundedPanel1, java.awt.BorderLayout.CENTER);
 
         setOpaque(false);
         setLayout(new java.awt.GridBagLayout());
 
         jScrollPane5.setBorder(null);
         jScrollPane5.setOpaque(false);
 
         jPanel3.setOpaque(false);
         jPanel3.setLayout(new java.awt.GridBagLayout());
 
         jPanel1.setOpaque(false);
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         roundedPanel2.setLayout(new java.awt.GridBagLayout());
 
         semiRoundedPanel3.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel3.setLayout(new java.awt.FlowLayout());
 
         jLabel1.setForeground(new java.awt.Color(255, 255, 255));
         jLabel1.setText("Allgemeine Informationen");
         semiRoundedPanel3.add(jLabel1);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 1;
         roundedPanel2.add(semiRoundedPanel3, gridBagConstraints);
 
         panContent.setOpaque(false);
         panContent.setLayout(new java.awt.GridBagLayout());
 
         pnlCtrlButtons1.setOpaque(false);
         pnlCtrlButtons1.setLayout(new java.awt.GridBagLayout());
 
         btnAddSuchwort.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddSuchwort.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.btnAddImg.text"));                                                                // NOI18N
         btnAddSuchwort.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddSuchwortActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlCtrlButtons1.add(btnAddSuchwort, gridBagConstraints);
 
         btnRemoveSuchwort.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveSuchwort.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.btnRemoveImg.text"));                                                                // NOI18N
         btnRemoveSuchwort.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveSuchwortActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 1;
         pnlCtrlButtons1.add(btnRemoveSuchwort, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         panContent.add(pnlCtrlButtons1, gridBagConstraints);
 
         pnlCtrlButtons.setOpaque(false);
         pnlCtrlButtons.setLayout(new java.awt.GridBagLayout());
 
         btnAddImageNumber.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddImageNumber.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.btnAddImg.text"));                                                                // NOI18N
         btnAddImageNumber.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddImageNumberActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlCtrlButtons.add(btnAddImageNumber, gridBagConstraints);
 
         btnRemoveImageNumber.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveImageNumber.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.btnRemoveImg.text"));                                                                // NOI18N
         btnRemoveImageNumber.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveImageNumberActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 1;
         pnlCtrlButtons.add(btnRemoveImageNumber, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         panContent.add(pnlCtrlButtons, gridBagConstraints);
 
         lblDescBildnummer.setText("Bildnummer");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(lblDescBildnummer, gridBagConstraints);
 
         lblDescLagerort.setText("Lagerort");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(lblDescLagerort, gridBagConstraints);
 
         lblDescAufnahmedatum.setText("Aufnahmedatum");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(lblDescAufnahmedatum, gridBagConstraints);
 
         lblDescInfo.setText("Kommentar");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(lblDescInfo, gridBagConstraints);
 
         lblDescBildtyp.setText("Bildtyp");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(lblDescBildtyp, gridBagConstraints);
 
         lblDescSuchworte.setText("Suchworte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(lblDescSuchworte, gridBagConstraints);
 
         lstBildnummern.setModel(new javax.swing.AbstractListModel() {
 
                 String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
 
                 @Override
                 public int getSize() {
                     return strings.length;
                 }
                 @Override
                 public Object getElementAt(final int i) {
                     return strings[i];
                 }
             });
         lstBildnummern.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create(
                 "${cidsBean.stadtbilder_arr}");
         org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJListBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         lstBildnummern);
         bindingGroup.addBinding(jListBinding);
 
         lstBildnummern.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                     lstBildnummernValueChanged(evt);
                 }
             });
         jScrollPane1.setViewportView(lstBildnummern);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(jScrollPane1, gridBagConstraints);
 
         lstSuchworte.setModel(new javax.swing.AbstractListModel() {
 
                 String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
 
                 @Override
                 public int getSize() {
                     return strings.length;
                 }
                 @Override
                 public Object getElementAt(final int i) {
                     return strings[i];
                 }
             });
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cidsBean.suchwort_arr}");
         jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 eLProperty,
                 lstSuchworte);
         bindingGroup.addBinding(jListBinding);
 
         jScrollPane2.setViewportView(lstSuchworte);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(jScrollPane2, gridBagConstraints);
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bildtyp}"),
                 defaultBindableReferenceCombo2,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(defaultBindableReferenceCombo2, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.lager}"),
                 defaultBindableReferenceCombo3,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(defaultBindableReferenceCombo3, gridBagConstraints);
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.kommentar}"),
                 jTextArea1,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jScrollPane3.setViewportView(jTextArea1);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(jScrollPane3, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.aufnahmedatum}"),
                 jXDatePicker1,
                 org.jdesktop.beansbinding.BeanProperty.create("date"));
         binding.setConverter(timeStampConverter);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panContent.add(jXDatePicker1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         roundedPanel2.add(panContent, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(roundedPanel2, gridBagConstraints);
 
         roundedPanel3.setLayout(new java.awt.GridBagLayout());
 
         semiRoundedPanel4.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel4.setLayout(new java.awt.FlowLayout());
 
         jLabel2.setForeground(new java.awt.Color(255, 255, 255));
         jLabel2.setText("Metainformationen");
         semiRoundedPanel4.add(jLabel2);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 1;
         roundedPanel3.add(semiRoundedPanel4, gridBagConstraints);
 
         panDetails.setOpaque(false);
         panDetails.setLayout(new java.awt.GridBagLayout());
 
         lblDescFilmart.setText("Filmart");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails.add(lblDescFilmart, gridBagConstraints);
 
         lblDescFotograf.setText("Fotograf");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails.add(lblDescFotograf, gridBagConstraints);
 
         lblDescAuftraggeber.setText("Auftraggeber");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails.add(lblDescAuftraggeber, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.auftraggeber}"),
                 dbcAuftraggeber,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails.add(dbcAuftraggeber, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.fotograf}"),
                 dbcFotograf,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails.add(dbcFotograf, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.filmart}"),
                 dbcFilmart,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails.add(dbcFilmart, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         roundedPanel3.add(panDetails, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(roundedPanel3, gridBagConstraints);
 
         roundedPanel4.setLayout(new java.awt.GridBagLayout());
 
         semiRoundedPanel5.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel5.setLayout(new java.awt.FlowLayout());
 
         jLabel3.setForeground(new java.awt.Color(255, 255, 255));
         jLabel3.setText("Ortbezogene Informationen");
         semiRoundedPanel5.add(jLabel3);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 1;
         roundedPanel4.add(semiRoundedPanel5, gridBagConstraints);
 
         panDetails1.setOpaque(false);
         panDetails1.setLayout(new java.awt.GridBagLayout());
 
         lblDescGeometrie.setText("Geometrie");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(lblDescGeometrie, gridBagConstraints);
 
         lblDescOrt.setText("Ort");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(lblDescOrt, gridBagConstraints);
 
         lblDescStrasse.setText("Straße");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(lblDescStrasse, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.ort}"),
                 dbcOrt,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(dbcOrt, gridBagConstraints);
 
         jLabel7.setText("Hs.-Nr.");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(jLabel7, gridBagConstraints);
 
         defaultBindableJTextField1.setPreferredSize(new java.awt.Dimension(50, 19));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.hausnummer}"),
                 defaultBindableJTextField1,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(defaultBindableJTextField1, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geom_aus}"),
                 lblGeomAus,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(lblGeomAus, gridBagConstraints);
 
         btnCombineGeometries.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/wizard.png"))); // NOI18N
         btnCombineGeometries.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "VermessungRissEditor.btnCombineGeometries.text"));                                     // NOI18N
         btnCombineGeometries.setToolTipText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "VermessungRissEditor.btnCombineGeometries.toolTipText"));                              // NOI18N
         btnCombineGeometries.setEnabled(false);
         btnCombineGeometries.setFocusPainted(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(btnCombineGeometries, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geom}"),
                 defaultCismapGeometryComboBoxEditor1,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(defaultCismapGeometryComboBoxEditor1, gridBagConstraints);
 
         ((FastBindableReferenceCombo)bcbStrasse).setSorted(true);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.strasse}"),
                 bcbStrasse,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails1.add(bcbStrasse, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         roundedPanel4.add(panDetails1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(roundedPanel4, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 3.0;
         jPanel3.add(jPanel1, gridBagConstraints);
 
         jPanel2.setOpaque(false);
         jPanel2.setLayout(new java.awt.GridBagLayout());
 
         pnlVorschau.setPreferredSize(new java.awt.Dimension(140, 300));
         pnlVorschau.setLayout(new java.awt.GridBagLayout());
 
         semiRoundedPanel2.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel2.setLayout(new java.awt.FlowLayout());
 
         lblVorschau.setForeground(new java.awt.Color(255, 255, 255));
         lblVorschau.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.lblVorschau.text")); // NOI18N
         semiRoundedPanel2.add(lblVorschau);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         pnlVorschau.add(semiRoundedPanel2, gridBagConstraints);
 
         pnlFoto.setOpaque(false);
         pnlFoto.setLayout(new java.awt.CardLayout());
 
         lblBusy.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblBusy.setMaximumSize(new java.awt.Dimension(140, 40));
         lblBusy.setMinimumSize(new java.awt.Dimension(140, 60));
         lblBusy.setPreferredSize(new java.awt.Dimension(140, 60));
         pnlFoto.add(lblBusy, "busy");
 
         jPanel4.setOpaque(false);
         jPanel4.setLayout(new java.awt.GridBagLayout());
 
         lblPicture.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.lblPicture.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         jPanel4.add(lblPicture, gridBagConstraints);
 
         pnlFoto.add(jPanel4, "image");
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         pnlVorschau.add(pnlFoto, gridBagConstraints);
 
         pnlCtrlBtn.setOpaque(false);
         pnlCtrlBtn.setPreferredSize(new java.awt.Dimension(100, 50));
         pnlCtrlBtn.setLayout(new java.awt.GridBagLayout());
 
         btnDownloadHighResImage.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/tools/gui/downloadmanager/res/download.png"))); // NOI18N
         btnDownloadHighResImage.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.btnPrevImg.text"));                                                   // NOI18N
         btnDownloadHighResImage.setBorder(null);
         btnDownloadHighResImage.setBorderPainted(false);
         btnDownloadHighResImage.setContentAreaFilled(false);
         btnDownloadHighResImage.setFocusPainted(false);
         btnDownloadHighResImage.setMaximumSize(new java.awt.Dimension(30, 30));
         btnDownloadHighResImage.setMinimumSize(new java.awt.Dimension(30, 30));
         btnDownloadHighResImage.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnDownloadHighResImageActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         pnlCtrlBtn.add(btnDownloadHighResImage, gridBagConstraints);
 
         btnPrevImg.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left.png")));          // NOI18N
         btnPrevImg.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.btnPrevImg.text"));                                                           // NOI18N
         btnPrevImg.setBorder(null);
         btnPrevImg.setBorderPainted(false);
         btnPrevImg.setContentAreaFilled(false);
         btnPrevImg.setFocusPainted(false);
         btnPrevImg.setMaximumSize(new java.awt.Dimension(30, 30));
         btnPrevImg.setMinimumSize(new java.awt.Dimension(30, 30));
         btnPrevImg.setPreferredSize(new java.awt.Dimension(30, 30));
         btnPrevImg.setPressedIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-pressed.png")));  // NOI18N
         btnPrevImg.setRolloverIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-selected.png"))); // NOI18N
         btnPrevImg.setSelectedIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-selected.png"))); // NOI18N
         btnPrevImg.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnPrevImgActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
         pnlCtrlBtn.add(btnPrevImg, gridBagConstraints);
 
         btnNextImg.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right.png")));          // NOI18N
         btnNextImg.setText(org.openide.util.NbBundle.getMessage(
                 Sb_stadtbildserieEditor.class,
                 "MauerEditor.btnNextImg.text"));                                                            // NOI18N
         btnNextImg.setBorder(null);
         btnNextImg.setBorderPainted(false);
         btnNextImg.setContentAreaFilled(false);
         btnNextImg.setFocusPainted(false);
         btnNextImg.setMaximumSize(new java.awt.Dimension(30, 30));
         btnNextImg.setMinimumSize(new java.awt.Dimension(30, 30));
         btnNextImg.setPreferredSize(new java.awt.Dimension(30, 30));
         btnNextImg.setPressedIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right-pressed.png")));  // NOI18N
         btnNextImg.setRolloverIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right-selected.png"))); // NOI18N
         btnNextImg.setSelectedIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right-selected.png"))); // NOI18N
         btnNextImg.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnNextImgActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         pnlCtrlBtn.add(btnNextImg, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlCtrlBtn.add(filler1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlCtrlBtn.add(filler2, gridBagConstraints);
 
         tbtnIsPreviewImage.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/tick_32.png"))); // NOI18N
         tbtnIsPreviewImage.setSelected(true);
         tbtnIsPreviewImage.setBorderPainted(false);
         tbtnIsPreviewImage.setMaximumSize(new java.awt.Dimension(30, 30));
         tbtnIsPreviewImage.setMinimumSize(new java.awt.Dimension(30, 30));
         tbtnIsPreviewImage.setPreferredSize(new java.awt.Dimension(32, 32));
         tbtnIsPreviewImage.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     tbtnIsPreviewImageActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         pnlCtrlBtn.add(tbtnIsPreviewImage, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlVorschau.add(pnlCtrlBtn, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel2.add(pnlVorschau, gridBagConstraints);
 
         roundedPanel7.setLayout(new java.awt.GridBagLayout());
 
         semiRoundedPanel8.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel8.setLayout(new java.awt.FlowLayout());
 
         jLabel6.setForeground(new java.awt.Color(255, 255, 255));
         jLabel6.setText("Karte");
         semiRoundedPanel8.add(jLabel6);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 1;
         roundedPanel7.add(semiRoundedPanel8, gridBagConstraints);
 
         panDetails4.setOpaque(false);
         panDetails4.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails4.add(pnlMap, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         roundedPanel7.add(panDetails4, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel2.add(roundedPanel7, gridBagConstraints);
 
         roundedPanel6.setLayout(new java.awt.GridBagLayout());
 
         semiRoundedPanel7.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel7.setLayout(new java.awt.FlowLayout());
 
         jLabel5.setForeground(new java.awt.Color(255, 255, 255));
         jLabel5.setText("Prüfhinweis");
         semiRoundedPanel7.add(jLabel5);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 1;
         roundedPanel6.add(semiRoundedPanel7, gridBagConstraints);
 
         panDetails3.setOpaque(false);
         panDetails3.setLayout(new java.awt.GridBagLayout());
 
         chbPruefen.setText("Prüfen");
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.pruefen}"),
                 chbPruefen,
                 org.jdesktop.beansbinding.BeanProperty.create("selected"));
         binding.setConverter(((DefaultBindableJCheckBox)chbPruefen).getConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails3.add(chbPruefen, gridBagConstraints);
 
         jTextArea2.setColumns(20);
         jTextArea2.setRows(5);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.pruefhinweis_von}"),
                 jTextArea2,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jScrollPane4.setViewportView(jTextArea2);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails3.add(jScrollPane4, gridBagConstraints);
 
         jButton1.setText("Prüfhinweis speichern");
         jButton1.setEnabled(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         panDetails3.add(jButton1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         roundedPanel6.add(panDetails3, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel2.add(roundedPanel6, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 3.0;
         jPanel3.add(jPanel2, gridBagConstraints);
 
         jScrollPane5.setViewportView(jPanel3);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(jScrollPane5, gridBagConstraints);
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblPrintMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblPrintMouseClicked
         if ((evt != null) && !evt.isPopupTrigger()) {
             final CidsBean bean = cidsBean;
             if (bean != null) {
                 final AbstractJasperReportPrint jp = new StadtbildJasperReportPrint(REPORT_FILE, bean);
                 jp.print();
             }
         }
     }                                                                        //GEN-LAST:event_lblPrintMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnPrevImgActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnPrevImgActionPerformed
         lstBildnummern.setSelectedIndex(lstBildnummern.getSelectedIndex() - 1);
     }                                                                              //GEN-LAST:event_btnPrevImgActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnNextImgActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnNextImgActionPerformed
         lstBildnummern.setSelectedIndex(lstBildnummern.getSelectedIndex() + 1);
     }                                                                              //GEN-LAST:event_btnNextImgActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnDownloadHighResImageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDownloadHighResImageActionPerformed
         if (DownloadManagerDialog.showAskingForUserTitle(
                         this)) {
             final String jobname = DownloadManagerDialog.getJobname();
             final String imageNumber = (String)((CidsBean)lstBildnummern.getSelectedValue()).getProperty("bildnummer");
             DownloadManager.instance()
                     .add(
                         new TifferDownload(
                             jobname,
                             "Stadtbild "
                             + imageNumber,
                             "stadtbild_"
                             + imageNumber,
                             lstBildnummern.getSelectedValue().toString(),
                             "1"));
         }
     }                                                                                           //GEN-LAST:event_btnDownloadHighResImageActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstBildnummernValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstBildnummernValueChanged
         if (!evt.getValueIsAdjusting()) {
             if (!lstBildnummern.isSelectionEmpty()) {
                 final String imageNumber = lstBildnummern.getSelectedValue().toString();
                 new CheckAccessibilityOfHighResImage(imageNumber).execute();
                 loadFoto();
                 final boolean isPreviewImage = cidsBean.getProperty("vorschaubild")
                             .equals(lstBildnummern.getSelectedValue());
                 tbtnIsPreviewImage.setSelected(isPreviewImage);
                 tbtnIsPreviewImage.setEnabled(!isPreviewImage);
                 lstBildnummern.ensureIndexIsVisible(lstBildnummern.getSelectedIndex());
             } else {
                 tbtnIsPreviewImage.setSelected(false);
                 tbtnIsPreviewImage.setEnabled(false);
             }
         }
     }                                                                                         //GEN-LAST:event_lstBildnummernValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void tbtnIsPreviewImageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_tbtnIsPreviewImageActionPerformed
         if (tbtnIsPreviewImage.isSelected()) {
             try {
                 cidsBean.setProperty("vorschaubild", lstBildnummern.getSelectedValue());
             } catch (Exception e) {
                 LOG.error("Error while setting the preview image of the CidsBean", e);
             }
         }
     }                                                                                      //GEN-LAST:event_tbtnIsPreviewImageActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddImageNumberActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddImageNumberActionPerformed
     }                                                                                     //GEN-LAST:event_btnAddImageNumberActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveImageNumberActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRemoveImageNumberActionPerformed
         final Object selection = lstBildnummern.getSelectedValue();
         if ((selection != null) && (selection instanceof CidsBean)) {
             final CidsBean cidesBeanToRemove = (CidsBean)selection;
             final int answer = JOptionPane.showConfirmDialog(
                     StaticSwingTools.getParentFrame(this),
                     "Soll die Bildnummer wirklich entfernt werden?",
                     "Bildernummern entfernen",
                     JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 final int modelSize = lstBildnummern.getModel().getSize();
                 if (modelSize >= 2) {
                     final int oldIndex = lstBildnummern.getSelectedIndex();
                     // select the second or second last element as new selected element
                     final int newIndex = (oldIndex == 0) ? 1 : (oldIndex - 1);
                     lstBildnummern.setSelectedIndex(newIndex);
                 } else {
                     image = null;
                     lblPicture.setIcon(FOLDER_ICON);
                 }
 
                 try {
                     final List<CidsBean> fotos = cidsBean.getBeanCollectionProperty("stadtbilder_arr");
                     if (fotos != null) {
                         fotos.remove(cidesBeanToRemove);
                     }
                     IMAGE_CACHE.remove(cidesBeanToRemove.toString());
                 } catch (Exception e) {
                     LOG.error(e, e);
                     final ErrorInfo ei = new ErrorInfo(
                             "Fehler",
                             "Beim Entfernen der Bildernummern ist ein Fehler aufgetreten",
                             null,
                             null,
                             e,
                             Level.SEVERE,
                             null);
                     JXErrorPane.showDialog(StaticSwingTools.getParentFrame(this), ei);
                 }
             }
         }
     } //GEN-LAST:event_btnRemoveImageNumberActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddSuchwortActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddSuchwortActionPerformed
         // TODO add your handling code here:
     } //GEN-LAST:event_btnAddSuchwortActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveSuchwortActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRemoveSuchwortActionPerformed
         final Object[] selection = lstSuchworte.getSelectedValues();
         if ((selection != null) && (selection.length > 0)) {
             final int answer = JOptionPane.showConfirmDialog(
                     StaticSwingTools.getParentFrame(this),
                     "Sollen die Suchwörter wirklich entfernt werden?",
                     "Suchwörter entfernen",
                     JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 try {
                     final List<Object> removeList = Arrays.asList(selection);
                     final List<CidsBean> suchwoerter = cidsBean.getBeanCollectionProperty("suchwort_arr");
                     if (suchwoerter != null) {
                         suchwoerter.removeAll(removeList);
                     }
                 } catch (Exception e) {
                     LOG.error(e, e);
                     final ErrorInfo ei = new ErrorInfo(
                             "Fehler",
                             "Beim Entfernen der Suchwörter ist ein Fehler aufgetreten",
                             null,
                             null,
                             e,
                             Level.SEVERE,
                             null);
                     JXErrorPane.showDialog(StaticSwingTools.getParentFrame(this), ei);
                 }
             }
         }
     }                                                                                     //GEN-LAST:event_btnRemoveSuchwortActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cidsBean  DOCUMENT ME!
      */
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         bindingGroup.unbind();
         if (cidsBean != null) {
             DefaultCustomObjectEditor.setMetaClassInformationToMetaClassStoreComponentsInBindingGroup(
                 bindingGroup,
                 cidsBean);
             this.cidsBean = cidsBean;
             bindingGroup.bind();
             lstBildnummern.setSelectedValue(cidsBean.getProperty("vorschaubild"), true);
             initMap();
 
             final String obj = String.valueOf(cidsBean.getProperty("bildnummer"));
 //            lblPicture.setPictureURL(StaticProperties.ARCHIVAR_URL_PREFIX + obj + StaticProperties.ARCHIVAR_URL_SUFFIX);
         }
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
         this.title = "Stadtbild " + title;
         lblTitle.setText(this.title);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         bindingGroup.unbind();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void loadFoto() {
         final Object stadtbild = lstBildnummern.getSelectedValue();
         if (fotoCidsBean != null) {
             fotoCidsBean.removePropertyChangeListener(listRepaintListener);
         }
         if (stadtbild instanceof CidsBean) {
             fotoCidsBean = (CidsBean)stadtbild;
             fotoCidsBean.addPropertyChangeListener(listRepaintListener);
             final String bildnummer = (String)fotoCidsBean.getProperty("bildnummer");
             boolean cacheHit = false;
             if (bildnummer != null) {
                 final SoftReference<BufferedImage> cachedImageRef = IMAGE_CACHE.get(bildnummer);
                 if (cachedImageRef != null) {
                     final BufferedImage cachedImage = cachedImageRef.get();
                     if (cachedImage != null) {
                         showWait(true);
                         cacheHit = true;
                         image = cachedImage;
                         resizeListenerEnabled = true;
                         timer.restart();
                     }
                 }
                 if (!cacheHit) {
                     new Sb_stadtbildserieEditor.LoadSelectedImageWorker(bildnummer).execute();
                 }
             }
         } else {
             image = null;
             lblPicture.setIcon(FOLDER_ICON);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void defineButtonStatus() {
         final int selectedIdx = lstBildnummern.getSelectedIndex();
         btnPrevImg.setEnabled(selectedIdx > 0);
         btnNextImg.setEnabled((selectedIdx < (lstBildnummern.getModel().getSize() - 1)) && (selectedIdx > -1));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  wait  DOCUMENT ME!
      */
     private void showWait(final boolean wait) {
         if (wait) {
             if (!lblBusy.isBusy()) {
                 ((CardLayout)pnlFoto.getLayout()).show(pnlFoto, "busy");
 //                lblPicture.setIcon(null);
                 lblBusy.setBusy(true);
                 btnPrevImg.setEnabled(false);
                 btnNextImg.setEnabled(false);
             }
         } else {
             ((CardLayout)pnlFoto.getLayout()).show(pnlFoto, "image");
             lblBusy.setBusy(false);
             defineButtonStatus();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  tooltip  DOCUMENT ME!
      */
     private void indicateError(final String tooltip) {
         lblPicture.setIcon(ERROR_ICON);
         lblPicture.setText("Fehler beim Übertragen des Bildes!");
         lblPicture.setToolTipText(tooltip);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
         DevelopmentTools.createEditorInFrameFromRMIConnectionOnLocalhost(
             "WUNDA_BLAU",
             "Administratoren",
             "admin",
             "kif",
             "sb_stadtbildserie",
             161078, // id 161078 high res, id 18 = interval
             1280,
             1024);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         if (cidsBean != null) {
             final Object geoObj = cidsBean.getProperty("geom");
             if (geoObj instanceof Geometry) {
                 final Geometry pureGeom = CrsTransformer.transformToGivenCrs((Geometry)geoObj,
                         AlkisConstants.COMMONS.SRS_SERVICE);
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("ALKISConstatns.Commons.GeoBUffer: " + AlkisConstants.COMMONS.GEO_BUFFER);
                 }
                 final XBoundingBox box = new XBoundingBox(pureGeom.getEnvelope().buffer(
                             AlkisConstants.COMMONS.GEO_BUFFER));
                 final double diagonalLength = Math.sqrt((box.getWidth() * box.getWidth())
                                 + (box.getHeight() * box.getHeight()));
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Buffer for map: " + diagonalLength);
                 }
                 final XBoundingBox bufferedBox = new XBoundingBox(box.getGeometry().buffer(diagonalLength));
                 final Runnable mapRunnable = new Runnable() {
 
                         @Override
                         public void run() {
                             final ActiveLayerModel mappingModel = new ActiveLayerModel();
                             mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                             mappingModel.addHome(new XBoundingBox(
                                     bufferedBox.getX1(),
                                     bufferedBox.getY1(),
                                     bufferedBox.getX2(),
                                     bufferedBox.getY2(),
                                     AlkisConstants.COMMONS.SRS_SERVICE,
                                     true));
                             final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(
                                         AlkisConstants.COMMONS.MAP_CALL_STRING));
                             swms.setName("Mauer");
                             final StyledFeature dsf = new DefaultStyledFeature();
                             dsf.setGeometry(pureGeom);
                             dsf.setFillingPaint(new Color(1, 0, 0, 0.5f));
                             dsf.setLineWidth(3);
                             dsf.setLinePaint(new Color(1, 0, 0, 1f));
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
                             map.addCustomInputListener("MUTE", new PBasicInputEventHandler() {
 
                                     @Override
                                     public void mouseClicked(final PInputEvent evt) {
                                         if (evt.getClickCount() > 1) {
                                             final CidsBean bean = cidsBean;
                                             ObjectRendererUtils.switchToCismapMap();
                                             ObjectRendererUtils.addBeanGeomAsFeatureToCismapMap(bean, false);
                                         }
                                     }
                                 });
                             map.setInteractionMode("MUTE");
                             map.getFeatureCollection().addFeature(dsf);
                             map.setAnimationDuration(duration);
                         }
                     };
                 if (EventQueue.isDispatchThread()) {
                     mapRunnable.run();
                 } else {
                     EventQueue.invokeLater(mapRunnable);
                 }
             }
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class ImageResizeWorker extends SwingWorker<ImageIcon, Void> {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ImageResizeWorker object.
          */
         public ImageResizeWorker() {
             if (image != null) {
                 lblPicture.setText("Wird neu skaliert...");
             }
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected ImageIcon doInBackground() throws Exception {
             if (image != null) {
                 final ImageIcon result = new ImageIcon(adjustScale(image, pnlFoto, 20, 20));
                 return result;
             } else {
                 return null;
             }
         }
 
         @Override
         protected void done() {
             if (!isCancelled()) {
                 try {
                     resizeListenerEnabled = false;
                     final ImageIcon result = get();
                     lblPicture.setIcon(result);
                     lblPicture.setText("");
                     lblPicture.setToolTipText(null);
                 } catch (InterruptedException ex) {
                     LOG.warn(ex, ex);
                 } catch (ExecutionException ex) {
                     LOG.error(ex, ex);
                     lblPicture.setText("Fehler beim Skalieren!");
                 } finally {
                     showWait(false);
                     if (currentResizeWorker == this) {
                         currentResizeWorker = null;
                     }
                     resizeListenerEnabled = true;
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class LoadSelectedImageWorker extends SwingWorker<BufferedImage, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final String bildnummer;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new LoadSelectedImageWorker object.
          *
          * @param  toLoad  DOCUMENT ME!
          */
         public LoadSelectedImageWorker(final String toLoad) {
             this.bildnummer = toLoad;
             lblPicture.setText("");
             lblPicture.setToolTipText(null);
             showWait(true);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected BufferedImage doInBackground() throws Exception {
             if ((bildnummer != null) && (bildnummer.length() > 0)) {
                 return downloadImageFromUrl(bildnummer);
             }
             return null;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   bildnummer  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private BufferedImage downloadImageFromUrl(final String bildnummer) {
             final URL urlLowResImage = TifferDownload.getURLOfLowResPicture(bildnummer);
             if (urlLowResImage != null) {
                 InputStream is = null;
                 try {
                     is = WebAccessManager.getInstance().doRequest(urlLowResImage);
                     final BufferedImage img = ImageIO.read(is);
                     return img;
                 } catch (Exception ex) {
                     LOG.warn("Image could not be loaded.", ex);
                 } finally {
                     if (is != null) {
                         try {
                             is.close();
                         } catch (IOException ex) {
                             LOG.warn("Error during closing InputStream.", ex);
                         }
                     }
                 }
             }
             return null;
         }
 
         @Override
         protected void done() {
             try {
                 image = get();
                 if (image != null) {
                     IMAGE_CACHE.put(bildnummer, new SoftReference<BufferedImage>(image));
                     resizeListenerEnabled = true;
                     timer.restart();
                 } else {
                     indicateError("Bild konnte nicht geladen werden.");
                 }
             } catch (InterruptedException ex) {
                 image = null;
                 LOG.warn(ex, ex);
             } catch (ExecutionException ex) {
                 image = null;
                 LOG.error(ex, ex);
                 indicateError(ex.getMessage());
             } finally {
                 if (image == null) {
                     showWait(false);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class CheckAccessibilityOfHighResImage extends SwingWorker<Boolean, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final String imageNumber;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new CheckAccessibilityOfHighResImage object.
          *
          * @param  imageNumber  DOCUMENT ME!
          */
         public CheckAccessibilityOfHighResImage(final String imageNumber) {
             this.imageNumber = imageNumber;
             btnDownloadHighResImage.setEnabled(false);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected Boolean doInBackground() throws Exception {
             return TifferDownload.getFormatOfHighResPicture(imageNumber) != null;
         }
 
         @Override
         protected void done() {
             try {
                 final boolean accessible = get();
                 btnDownloadHighResImage.setEnabled(accessible);
             } catch (InterruptedException ex) {
                 LOG.warn(ex, ex);
             } catch (ExecutionException ex) {
                 LOG.warn(ex, ex);
             }
         }
     }
 }
