 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.PrecisionModel;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.NbBundle;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.sql.Date;
 
 import java.text.MessageFormat;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultListModel;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JToggleButton;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListModel;
 import javax.swing.SwingWorker;
 import javax.swing.WindowConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DocumentFilter;
 
 import de.cismet.cids.custom.objectrenderer.utils.AlphanumComparator;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsVermessungRissArtSearchStatement;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsVermessungRissSearchStatement;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.DisposableCidsBeanStore;
 
 import de.cismet.cids.editors.DefaultBindableReferenceCombo;
 import de.cismet.cids.editors.DefaultCustomObjectEditor;
 import de.cismet.cids.editors.EditorClosedEvent;
 import de.cismet.cids.editors.EditorSaveListener;
 import de.cismet.cids.editors.converters.SqlDateToStringConverter;
 
 import de.cismet.cismap.cids.geometryeditor.DefaultCismapGeometryComboBoxEditor;
 
 import de.cismet.cismap.commons.Crs;
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.gui.measuring.MeasuringComponent;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.MultiPagePictureReader;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.TitleComponentProvider;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.HttpDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class VermessungRissEditor extends javax.swing.JPanel implements DisposableCidsBeanStore,
     TitleComponentProvider,
     FooterComponentProvider,
     BorderProvider,
     RequestsFullSizeComponent,
     EditorSaveListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(VermessungRissEditor.class);
 
     public static final String[] SUFFIXES = new String[] {
             "tif",
             "jpg",
             "jpe",
             "tiff",
             "jpeg",
             "TIF",
             "JPG",
             "JPE",
             "TIFF",
             "JPEG"
         };
     private static final String SUFFIX_REDUCED_SIZE = "_RS";
 
     protected static final int DOCUMENT_BILD = 0;
     protected static final int DOCUMENT_GRENZNIEDERSCHRIFT = 1;
     protected static final int NO_SELECTION = -1;
     protected static final ListModel MODEL_LOAD = new DefaultListModel() {
 
             {
                 add(0, "Wird geladen...");
             }
         };
 
     protected static XBoundingBox INITIAL_BOUNDINGBOX = new XBoundingBox(
             2583621.251964098d,
             5682507.032498134d,
             2584022.9413952776d,
             5682742.852810634d,
             AlkisConstants.COMMONS.SRS_SERVICE,
             true);
     protected static Crs CRS = new Crs(
             AlkisConstants.COMMONS.SRS_SERVICE,
             AlkisConstants.COMMONS.SRS_SERVICE,
             AlkisConstants.COMMONS.SRS_SERVICE,
             true,
             true);
     protected static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(
                 PrecisionModel.FLOATING),
             CrsTransformer.extractSridFromCrs(AlkisConstants.COMMONS.SRS_SERVICE));
 
     private static Collection<CidsBean> veraenderungsarts = new LinkedList<CidsBean>();
 
     protected static final Map<Integer, Color> COLORS_GEOMETRIE_STATUS = new HashMap<Integer, Color>();
 
     static {
         COLORS_GEOMETRIE_STATUS.put(new Integer(1), Color.green);
         COLORS_GEOMETRIE_STATUS.put(new Integer(2), Color.yellow);
         COLORS_GEOMETRIE_STATUS.put(new Integer(3), Color.yellow);
         COLORS_GEOMETRIE_STATUS.put(new Integer(4), Color.red);
         COLORS_GEOMETRIE_STATUS.put(new Integer(5), Color.red);
         COLORS_GEOMETRIE_STATUS.put(new Integer(6), Color.green);
     }
 
     //~ Instance fields --------------------------------------------------------
 
     protected CidsBean cidsBean;
     protected Object schluessel;
     protected Object gemarkung;
     protected Object flur;
     protected Object blatt;
     protected boolean readOnly;
     protected Map.Entry<URL, URL>[] documentURLs;
     protected JToggleButton[] documentButtons;
     protected JToggleButton currentSelectedButton;
     protected PictureSelectWorker currentPictureSelectWorker = null;
     protected MultiPagePictureReader pictureReader;
     protected VermessungFlurstueckSelectionDialog flurstueckDialog;
     protected volatile int currentDocument = NO_SELECTION;
     protected volatile int currentPage = NO_SELECTION;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup bgrControls;
     private javax.swing.ButtonGroup bgrDocument;
     private javax.swing.JButton btnAddLandparcel;
     private javax.swing.JButton btnCombineGeometries;
     private javax.swing.JButton btnHome;
     private javax.swing.JButton btnOpen;
     private javax.swing.JButton btnRemoveLandparcel;
     private javax.swing.JComboBox cmbFormat;
     private javax.swing.JComboBox cmbGemarkung;
     private javax.swing.JComboBox cmbGeometrie;
     private javax.swing.JComboBox cmbGeometrieStatus;
     private javax.swing.JComboBox cmbSchluessel;
     private javax.swing.Box.Filler gluGapControls;
     private javax.swing.Box.Filler gluGeneralInformationGap;
     private javax.swing.JLabel lblBlatt;
     private javax.swing.JLabel lblErrorWhileLoadingBild;
     private javax.swing.JLabel lblErrorWhileLoadingGrenzniederschrift;
     private javax.swing.JLabel lblFlur;
     private javax.swing.JLabel lblFormat;
     private javax.swing.JLabel lblGemarkung;
     private javax.swing.JLabel lblGeneralInformation;
     private javax.swing.JLabel lblGeometrie;
     private javax.swing.JLabel lblGeometrieStatus;
     private javax.swing.JLabel lblHeaderControls;
     private javax.swing.JLabel lblHeaderDocument;
     private javax.swing.JLabel lblHeaderDocuments;
     private javax.swing.JLabel lblHeaderLandparcels;
     private javax.swing.JLabel lblHeaderPages;
     private javax.swing.JLabel lblJahr;
     private javax.swing.JLabel lblLetzteAenderungDatum;
     private javax.swing.JLabel lblLetzteAenderungName;
     private javax.swing.JLabel lblMissingDocuments;
     private javax.swing.JLabel lblSchluessel;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JList lstLandparcels;
     private javax.swing.JList lstPages;
     private de.cismet.cismap.commons.gui.measuring.MeasuringComponent measuringComponent;
     private javax.swing.JPanel pnlContainer;
     private de.cismet.tools.gui.RoundedPanel pnlControls;
     private de.cismet.tools.gui.RoundedPanel pnlDocument;
     private de.cismet.tools.gui.RoundedPanel pnlDocuments;
     private de.cismet.tools.gui.RoundedPanel pnlGeneralInformation;
     private de.cismet.tools.gui.SemiRoundedPanel pnlHeaderControls;
     private de.cismet.tools.gui.SemiRoundedPanel pnlHeaderDocument;
     private de.cismet.tools.gui.SemiRoundedPanel pnlHeaderDocuments;
     private de.cismet.tools.gui.SemiRoundedPanel pnlHeaderGeneralInformation;
     private de.cismet.tools.gui.SemiRoundedPanel pnlHeaderLandparcels;
     private de.cismet.tools.gui.SemiRoundedPanel pnlHeaderPages;
     private de.cismet.tools.gui.RoundedPanel pnlLandparcels;
     private de.cismet.tools.gui.RoundedPanel pnlPages;
     private javax.swing.JPanel pnlTitle;
     private javax.swing.JPopupMenu popChangeVeraenderungsart;
     private javax.swing.JScrollPane scpLandparcels;
     private javax.swing.JScrollPane scpPages;
     private javax.swing.Box.Filler strFooter;
     private javax.swing.JToggleButton togBild;
     private javax.swing.JToggleButton togGrenzniederschrift;
     private javax.swing.JToggleButton togPan;
     private javax.swing.JToggleButton togZoom;
     private javax.swing.JTextField txtBlatt;
     private javax.swing.JTextField txtFlur;
     private javax.swing.JTextField txtJahr;
     private javax.swing.JTextField txtLetzteaenderungDatum;
     private javax.swing.JTextField txtLetzteaenderungName;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new VermessungRissEditor object.
      */
     public VermessungRissEditor() {
         this(false);
     }
 
     /**
      * Creates new form VermessungRissEditor.
      *
      * @param  readOnly  DOCUMENT ME!
      */
     public VermessungRissEditor(final boolean readOnly) {
         this.readOnly = readOnly;
 
         documentURLs = new Map.Entry[2];
         documentButtons = new JToggleButton[documentURLs.length];
         initComponents();
         documentButtons[DOCUMENT_BILD] = togBild;
         documentButtons[DOCUMENT_GRENZNIEDERSCHRIFT] = togGrenzniederschrift;
 
         if (readOnly) {
             lblSchluessel.setVisible(false);
             cmbSchluessel.setVisible(false);
             lblGemarkung.setVisible(false);
             cmbGemarkung.setVisible(false);
             lblFlur.setVisible(false);
             txtFlur.setVisible(false);
             lblBlatt.setVisible(false);
             txtBlatt.setVisible(false);
             txtJahr.setEditable(false);
             cmbFormat.setEditable(false);
             cmbFormat.setEnabled(false);
             cmbGeometrieStatus.setEditable(false);
             cmbGeometrieStatus.setEnabled(false);
             lblGeometrie.setVisible(false);
             btnAddLandparcel.setVisible(false);
             btnRemoveLandparcel.setVisible(false);
             btnCombineGeometries.setVisible(false);
         } else {
             flurstueckDialog = new VermessungFlurstueckSelectionDialog();
             flurstueckDialog.pack();
             flurstueckDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
             flurstueckDialog.addWindowListener(new EnableCombineGeometriesButton());
             if (txtBlatt.getDocument() instanceof AbstractDocument) {
                 ((AbstractDocument)txtBlatt.getDocument()).setDocumentFilter(new DocumentSizeFilter());
             }
             if (txtFlur.getDocument() instanceof AbstractDocument) {
                 ((AbstractDocument)txtFlur.getDocument()).setDocumentFilter(new DocumentSizeFilter());
             }
         }
 
         // Initialize the popup menu to change the veraenderungsart. Since the set of available veraenderungsart is very
         // unlikely to change, we once load it and save it in a static Collection.
         if ((veraenderungsarts == null) || veraenderungsarts.isEmpty()) {
             final Collection result;
             try {
                 result = SessionManager.getProxy()
                             .customServerSearch(SessionManager.getSession().getUser(),
                                     new CidsVermessungRissArtSearchStatement(SessionManager.getSession().getUser()));
             } catch (final ConnectionException ex) {
                 LOG.warn("Could not fetch veranederungsart entries. Editing flurstuecksvermessung will not work.", ex);
                 // TODO: USer feedback?
                 return;
             }
 
             for (final Object veraenderungsart : result) {
                 veraenderungsarts.add(((MetaObject)veraenderungsart).getBean());
             }
         }
 
         for (final CidsBean veraenderungsart : veraenderungsarts) {
             popChangeVeraenderungsart.add(new ChangeVeraenderungsartAction(veraenderungsart));
         }
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
 
         strFooter = new javax.swing.Box.Filler(new java.awt.Dimension(0, 22),
                 new java.awt.Dimension(0, 22),
                 new java.awt.Dimension(32767, 22));
         pnlTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         bgrControls = new javax.swing.ButtonGroup();
         bgrDocument = new javax.swing.ButtonGroup();
         popChangeVeraenderungsart = new javax.swing.JPopupMenu();
         pnlContainer = new javax.swing.JPanel();
         pnlGeneralInformation = new de.cismet.tools.gui.RoundedPanel();
         pnlHeaderGeneralInformation = new de.cismet.tools.gui.SemiRoundedPanel();
         lblGeneralInformation = new javax.swing.JLabel();
         lblJahr = new javax.swing.JLabel();
         txtJahr = new javax.swing.JTextField();
         lblFormat = new javax.swing.JLabel();
         cmbFormat = new DefaultBindableReferenceCombo();
         lblLetzteAenderungName = new javax.swing.JLabel();
         txtLetzteaenderungName = new javax.swing.JTextField();
         lblLetzteAenderungDatum = new javax.swing.JLabel();
         txtLetzteaenderungDatum = new javax.swing.JTextField();
         lblGeometrie = new javax.swing.JLabel();
         if (!readOnly) {
             cmbGeometrie = new DefaultCismapGeometryComboBoxEditor();
         }
         btnCombineGeometries = new javax.swing.JButton();
         gluGeneralInformationGap = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
         lblGeometrieStatus = new javax.swing.JLabel();
         cmbGeometrieStatus = new DefaultBindableReferenceCombo();
         lblSchluessel = new javax.swing.JLabel();
         cmbSchluessel = new javax.swing.JComboBox();
         lblGemarkung = new javax.swing.JLabel();
         cmbGemarkung = new DefaultBindableReferenceCombo();
         lblFlur = new javax.swing.JLabel();
         txtFlur = new javax.swing.JTextField();
         lblBlatt = new javax.swing.JLabel();
         txtBlatt = new javax.swing.JTextField();
         pnlLandparcels = new de.cismet.tools.gui.RoundedPanel();
         pnlHeaderLandparcels = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderLandparcels = new javax.swing.JLabel();
         scpLandparcels = new javax.swing.JScrollPane();
         lstLandparcels = new javax.swing.JList();
         btnAddLandparcel = new javax.swing.JButton();
         btnRemoveLandparcel = new javax.swing.JButton();
         pnlControls = new de.cismet.tools.gui.RoundedPanel();
         togPan = new javax.swing.JToggleButton();
         togZoom = new javax.swing.JToggleButton();
         btnHome = new javax.swing.JButton();
         pnlHeaderControls = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderControls = new javax.swing.JLabel();
         btnOpen = new javax.swing.JButton();
         pnlDocuments = new de.cismet.tools.gui.RoundedPanel();
         pnlHeaderDocuments = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderDocuments = new javax.swing.JLabel();
         togBild = new javax.swing.JToggleButton();
         togGrenzniederschrift = new javax.swing.JToggleButton();
         pnlPages = new de.cismet.tools.gui.RoundedPanel();
         pnlHeaderPages = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderPages = new javax.swing.JLabel();
         scpPages = new javax.swing.JScrollPane();
         lstPages = new javax.swing.JList();
         pnlDocument = new de.cismet.tools.gui.RoundedPanel();
         pnlHeaderDocument = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderDocument = new javax.swing.JLabel();
         measuringComponent = new MeasuringComponent(INITIAL_BOUNDINGBOX, CRS);
         lblErrorWhileLoadingBild = new javax.swing.JLabel();
         lblErrorWhileLoadingGrenzniederschrift = new javax.swing.JLabel();
         lblMissingDocuments = new javax.swing.JLabel();
         gluGapControls = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
 
         pnlTitle.setOpaque(false);
         pnlTitle.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         lblTitle.setForeground(java.awt.Color.white);
         lblTitle.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblTitle.text"));       // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlTitle.add(lblTitle, gridBagConstraints);
 
         setLayout(new java.awt.GridBagLayout());
 
         pnlContainer.setOpaque(false);
         pnlContainer.setLayout(new java.awt.GridBagLayout());
 
         pnlGeneralInformation.setLayout(new java.awt.GridBagLayout());
 
         pnlHeaderGeneralInformation.setBackground(new java.awt.Color(51, 51, 51));
         pnlHeaderGeneralInformation.setLayout(new java.awt.FlowLayout());
 
         lblGeneralInformation.setForeground(new java.awt.Color(255, 255, 255));
         lblGeneralInformation.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblGeneralInformation.text")); // NOI18N
         pnlHeaderGeneralInformation.add(lblGeneralInformation);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         pnlGeneralInformation.add(pnlHeaderGeneralInformation, gridBagConstraints);
 
         lblJahr.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblJahr.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         pnlGeneralInformation.add(lblJahr, gridBagConstraints);
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.jahr}"),
                 txtJahr,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 7);
         pnlGeneralInformation.add(txtJahr, gridBagConstraints);
 
         lblFormat.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblFormat.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         pnlGeneralInformation.add(lblFormat, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.format}"),
                 cmbFormat,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 7);
         pnlGeneralInformation.add(cmbFormat, gridBagConstraints);
 
         lblLetzteAenderungName.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblLetzteAenderungName.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 5);
         pnlGeneralInformation.add(lblLetzteAenderungName, gridBagConstraints);
 
         txtLetzteaenderungName.setEditable(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.letzteaenderung_name}"),
                 txtLetzteaenderungName,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
         pnlGeneralInformation.add(txtLetzteaenderungName, gridBagConstraints);
 
         lblLetzteAenderungDatum.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblLetzteAenderungDatum.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 5);
         pnlGeneralInformation.add(lblLetzteAenderungDatum, gridBagConstraints);
 
         txtLetzteaenderungDatum.setEditable(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.letzteaenderung_datum}"),
                 txtLetzteaenderungDatum,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setConverter(new SqlDateToStringConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
         pnlGeneralInformation.add(txtLetzteaenderungDatum, gridBagConstraints);
 
         lblGeometrie.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblGeometrie.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlGeneralInformation.add(lblGeometrie, gridBagConstraints);
 
         if (!readOnly) {
             binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                     org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                     this,
                     org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geometrie}"),
                     cmbGeometrie,
                     org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
             binding.setConverter(((DefaultCismapGeometryComboBoxEditor)cmbGeometrie).getConverter());
             bindingGroup.addBinding(binding);
         }
         if (!readOnly) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 3;
             gridBagConstraints.gridy = 5;
             gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
             gridBagConstraints.weightx = 0.5;
             gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
             pnlGeneralInformation.add(cmbGeometrie, gridBagConstraints);
         }
 
         btnCombineGeometries.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/wizard.png"))); // NOI18N
         btnCombineGeometries.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnCombineGeometries.text"));                                     // NOI18N
         btnCombineGeometries.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnCombineGeometries.toolTipText"));                              // NOI18N
         btnCombineGeometries.setEnabled(false);
         btnCombineGeometries.setFocusPainted(false);
         btnCombineGeometries.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCombineGeometriesActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 10);
         pnlGeneralInformation.add(btnCombineGeometries, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         pnlGeneralInformation.add(gluGeneralInformationGap, gridBagConstraints);
 
         lblGeometrieStatus.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblGeometrieStatus.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlGeneralInformation.add(lblGeometrieStatus, gridBagConstraints);
 
         cmbGeometrieStatus.setRenderer(new GeometrieStatusRenderer(cmbGeometrieStatus.getRenderer()));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geometrie_status}"),
                 cmbGeometrieStatus,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         cmbGeometrieStatus.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmbGeometrieStatusActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
         pnlGeneralInformation.add(cmbGeometrieStatus, gridBagConstraints);
 
         lblSchluessel.setLabelFor(cmbSchluessel);
         lblSchluessel.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblSchluessel.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         pnlGeneralInformation.add(lblSchluessel, gridBagConstraints);
 
         cmbSchluessel.setModel(new javax.swing.DefaultComboBoxModel(
                 new String[] { "501", "502", "503", "504", "505", "506", "507", "508", "600", "605" }));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.schluessel}"),
                 cmbSchluessel,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         pnlGeneralInformation.add(cmbSchluessel, gridBagConstraints);
 
         lblGemarkung.setLabelFor(cmbGemarkung);
         lblGemarkung.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblGemarkung.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         pnlGeneralInformation.add(lblGemarkung, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.gemarkung}"),
                 cmbGemarkung,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
         pnlGeneralInformation.add(cmbGemarkung, gridBagConstraints);
 
         lblFlur.setLabelFor(txtFlur);
         lblFlur.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblFlur.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         pnlGeneralInformation.add(lblFlur, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.flur}"),
                 txtFlur,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlGeneralInformation.add(txtFlur, gridBagConstraints);
 
         lblBlatt.setLabelFor(txtBlatt);
         lblBlatt.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblBlatt.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlGeneralInformation.add(lblBlatt, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.blatt}"),
                 txtBlatt,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
         pnlGeneralInformation.add(txtBlatt, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.75;
         gridBagConstraints.weighty = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
         pnlContainer.add(pnlGeneralInformation, gridBagConstraints);
 
         pnlLandparcels.setLayout(new java.awt.GridBagLayout());
 
         pnlHeaderLandparcels.setBackground(new java.awt.Color(51, 51, 51));
         pnlHeaderLandparcels.setLayout(new java.awt.FlowLayout());
 
         lblHeaderLandparcels.setForeground(new java.awt.Color(255, 255, 255));
         lblHeaderLandparcels.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblHeaderLandparcels.text")); // NOI18N
         pnlHeaderLandparcels.add(lblHeaderLandparcels);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         pnlLandparcels.add(pnlHeaderLandparcels, gridBagConstraints);
 
         scpLandparcels.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         scpLandparcels.setMinimumSize(new java.awt.Dimension(266, 138));
         scpLandparcels.setOpaque(false);
 
         lstLandparcels.setCellRenderer(new HighlightReferencingFlurstueckeCellRenderer());
 
         final org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create(
                 "${cidsBean.flurstuecksvermessung}");
         final org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJListBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         lstLandparcels);
         bindingGroup.addBinding(jListBinding);
 
         lstLandparcels.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lstLandparcelsMouseClicked(evt);
                 }
                 @Override
                 public void mousePressed(final java.awt.event.MouseEvent evt) {
                     lstLandparcelsMousePressed(evt);
                 }
                 @Override
                 public void mouseReleased(final java.awt.event.MouseEvent evt) {
                     lstLandparcelsMouseReleased(evt);
                 }
             });
         lstLandparcels.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                     lstLandparcelsValueChanged(evt);
                 }
             });
         scpLandparcels.setViewportView(lstLandparcels);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weighty = 0.1;
         pnlLandparcels.add(scpLandparcels, gridBagConstraints);
 
         btnAddLandparcel.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddLandparcel.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnAddLandparcel.text"));                                                // NOI18N
         btnAddLandparcel.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnAddLandparcel.toolTipText"));                                         // NOI18N
         btnAddLandparcel.setFocusPainted(false);
         btnAddLandparcel.setMaximumSize(new java.awt.Dimension(43, 25));
         btnAddLandparcel.setMinimumSize(new java.awt.Dimension(43, 25));
         btnAddLandparcel.setPreferredSize(new java.awt.Dimension(43, 25));
         btnAddLandparcel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddLandparcelActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 2);
         pnlLandparcels.add(btnAddLandparcel, gridBagConstraints);
 
         btnRemoveLandparcel.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveLandparcel.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnRemoveLandparcel.text"));                                                // NOI18N
         btnRemoveLandparcel.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnRemoveLandparcel.toolTipText"));                                         // NOI18N
         btnRemoveLandparcel.setEnabled(false);
         btnRemoveLandparcel.setFocusPainted(false);
         btnRemoveLandparcel.setMaximumSize(new java.awt.Dimension(43, 25));
         btnRemoveLandparcel.setMinimumSize(new java.awt.Dimension(43, 25));
         btnRemoveLandparcel.setPreferredSize(new java.awt.Dimension(43, 25));
         btnRemoveLandparcel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveLandparcelActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 10, 10);
         pnlLandparcels.add(btnRemoveLandparcel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.25;
         gridBagConstraints.weighty = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
         pnlContainer.add(pnlLandparcels, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weighty = 0.1;
         add(pnlContainer, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         bgrControls.add(togPan);
         togPan.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/pan.gif"))); // NOI18N
         togPan.setSelected(true);
         togPan.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.togPan.text"));                                      // NOI18N
         togPan.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.togPan.toolTipText"));                               // NOI18N
         togPan.setEnabled(false);
         togPan.setFocusPainted(false);
         togPan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togPan.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togPanActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 10, 3, 10);
         pnlControls.add(togPan, gridBagConstraints);
 
         bgrControls.add(togZoom);
         togZoom.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/zoom.gif"))); // NOI18N
         togZoom.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.togZoom.text"));                                      // NOI18N
         togZoom.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.togZoom.toolTipText"));                               // NOI18N
         togZoom.setEnabled(false);
         togZoom.setFocusPainted(false);
         togZoom.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         togZoom.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togZoomActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 10, 3, 10);
         pnlControls.add(togZoom, gridBagConstraints);
 
         btnHome.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/home.gif"))); // NOI18N
         btnHome.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnHome.text"));                                      // NOI18N
         btnHome.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnHome.toolTipText"));                               // NOI18N
         btnHome.setEnabled(false);
         btnHome.setFocusPainted(false);
         btnHome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnHome.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnHomeActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 3, 10);
         pnlControls.add(btnHome, gridBagConstraints);
 
         pnlHeaderControls.setBackground(new java.awt.Color(51, 51, 51));
         pnlHeaderControls.setLayout(new java.awt.FlowLayout());
 
         lblHeaderControls.setForeground(new java.awt.Color(255, 255, 255));
         lblHeaderControls.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblHeaderControls.text")); // NOI18N
         pnlHeaderControls.add(lblHeaderControls);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         pnlControls.add(pnlHeaderControls, gridBagConstraints);
 
         btnOpen.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/folder-image.png"))); // NOI18N
         btnOpen.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnOpen.text"));                                              // NOI18N
         btnOpen.setToolTipText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.btnOpen.toolTipText"));                                       // NOI18N
         btnOpen.setEnabled(false);
         btnOpen.setFocusPainted(false);
         btnOpen.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnOpen.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnOpenActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 10, 8, 10);
         pnlControls.add(btnOpen, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
         add(pnlControls, gridBagConstraints);
 
         pnlDocuments.setLayout(new java.awt.GridBagLayout());
 
         pnlHeaderDocuments.setBackground(new java.awt.Color(51, 51, 51));
         pnlHeaderDocuments.setLayout(new java.awt.FlowLayout());
 
         lblHeaderDocuments.setForeground(new java.awt.Color(255, 255, 255));
         lblHeaderDocuments.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblHeaderDocuments.text")); // NOI18N
         pnlHeaderDocuments.add(lblHeaderDocuments);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         pnlDocuments.add(pnlHeaderDocuments, gridBagConstraints);
 
         bgrDocument.add(togBild);
         togBild.setSelected(true);
         togBild.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.togBild.text")); // NOI18N
         togBild.setEnabled(false);
         togBild.setFocusPainted(false);
         togBild.setMaximumSize(new java.awt.Dimension(49, 32));
         togBild.setMinimumSize(new java.awt.Dimension(49, 32));
         togBild.setPreferredSize(new java.awt.Dimension(49, 32));
         togBild.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togBildActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 2, 10);
         pnlDocuments.add(togBild, gridBagConstraints);
 
         bgrDocument.add(togGrenzniederschrift);
         togGrenzniederschrift.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.togGrenzniederschrift.text")); // NOI18N
         togGrenzniederschrift.setEnabled(false);
         togGrenzniederschrift.setFocusPainted(false);
         togGrenzniederschrift.setMaximumSize(new java.awt.Dimension(150, 32));
         togGrenzniederschrift.setMinimumSize(new java.awt.Dimension(150, 32));
         togGrenzniederschrift.setPreferredSize(new java.awt.Dimension(150, 32));
         togGrenzniederschrift.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togGrenzniederschriftActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(2, 10, 10, 10);
         pnlDocuments.add(togGrenzniederschrift, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 5);
         add(pnlDocuments, gridBagConstraints);
 
         pnlHeaderPages.setBackground(new java.awt.Color(51, 51, 51));
         pnlHeaderPages.setLayout(new java.awt.FlowLayout());
 
         lblHeaderPages.setForeground(new java.awt.Color(255, 255, 255));
         lblHeaderPages.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblHeaderPages.text")); // NOI18N
         pnlHeaderPages.add(lblHeaderPages);
 
         pnlPages.add(pnlHeaderPages, java.awt.BorderLayout.PAGE_START);
 
         scpPages.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         scpPages.setMinimumSize(new java.awt.Dimension(31, 75));
         scpPages.setOpaque(false);
         scpPages.setPreferredSize(new java.awt.Dimension(85, 75));
 
         lstPages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         lstPages.setEnabled(false);
         lstPages.setFixedCellWidth(75);
         lstPages.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                     lstPagesValueChanged(evt);
                 }
             });
         scpPages.setViewportView(lstPages);
 
         pnlPages.add(scpPages, java.awt.BorderLayout.CENTER);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
         add(pnlPages, gridBagConstraints);
 
         pnlDocument.setLayout(new java.awt.GridBagLayout());
 
         pnlHeaderDocument.setBackground(java.awt.Color.darkGray);
         pnlHeaderDocument.setLayout(new java.awt.GridBagLayout());
 
         lblHeaderDocument.setForeground(java.awt.Color.white);
         lblHeaderDocument.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblHeaderDocument.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlHeaderDocument.add(lblHeaderDocument, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         pnlDocument.add(pnlHeaderDocument, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         pnlDocument.add(measuringComponent, gridBagConstraints);
 
         lblErrorWhileLoadingBild.setBackground(java.awt.Color.white);
         lblErrorWhileLoadingBild.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblErrorWhileLoadingBild.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/missingRasterdocument.png"))); // NOI18N
         lblErrorWhileLoadingBild.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblErrorWhileLoadingBild.text"));                                                // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         pnlDocument.add(lblErrorWhileLoadingBild, gridBagConstraints);
 
         lblErrorWhileLoadingGrenzniederschrift.setBackground(java.awt.Color.white);
         lblErrorWhileLoadingGrenzniederschrift.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblErrorWhileLoadingGrenzniederschrift.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/missingRasterdocument.png"))); // NOI18N
         lblErrorWhileLoadingGrenzniederschrift.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblErrorWhileLoadingGrenzniederschrift.text"));                                  // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         pnlDocument.add(lblErrorWhileLoadingGrenzniederschrift, gridBagConstraints);
 
         lblMissingDocuments.setBackground(java.awt.Color.white);
         lblMissingDocuments.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblMissingDocuments.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/missingRasterdocument.png"))); // NOI18N
         lblMissingDocuments.setText(org.openide.util.NbBundle.getMessage(
                 VermessungRissEditor.class,
                 "VermessungRissEditor.lblMissingDocuments.text"));                                                     // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         pnlDocument.add(lblMissingDocuments, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
         add(pnlDocument, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         add(gluGapControls, gridBagConstraints);
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togPanActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togPanActionPerformed
         measuringComponent.actionPan();
     }                                                                          //GEN-LAST:event_togPanActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togZoomActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togZoomActionPerformed
         measuringComponent.actionZoom();
     }                                                                           //GEN-LAST:event_togZoomActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnHomeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnHomeActionPerformed
         measuringComponent.actionOverview();
     }                                                                           //GEN-LAST:event_btnHomeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnOpenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOpenActionPerformed
         if (currentDocument != NO_SELECTION) {
             final String url = documentURLs[currentDocument].getKey().toExternalForm();
 
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         if (DownloadManagerDialog.showAskingForUserTitle(VermessungRissEditor.this)) {
                             final String filename = url.substring(url.lastIndexOf("/") + 1);
 
                             DownloadManager.instance()
                                     .add(
                                         new HttpDownload(
                                             documentURLs[currentDocument].getKey(),
                                             "",
                                             DownloadManagerDialog.getJobname(),
                                             (currentDocument == DOCUMENT_BILD) ? "Vermessungsriss"
                                                                                : "Ergänzende Dokumente",
                                             filename.substring(0, filename.lastIndexOf(".")),
                                             filename.substring(filename.lastIndexOf("."))));
                         }
                     }
                 });
         }
     } //GEN-LAST:event_btnOpenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstLandparcelsMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lstLandparcelsMouseClicked
         if (evt.getClickCount() <= 1) {
             return;
         }
 
         final Object selectedObj = lstLandparcels.getSelectedValue();
         if (selectedObj instanceof CidsBean) {
             final CidsBean selectedBean = (CidsBean)selectedObj;
 
             if ((selectedBean.getProperty("flurstueck") instanceof CidsBean)
                         && (selectedBean.getProperty("flurstueck.flurstueck") instanceof CidsBean)) {
                 final MetaObject selMO = ((CidsBean)selectedBean.getProperty("flurstueck.flurstueck")).getMetaObject();
                 ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(selMO, "");
             }
         }
     } //GEN-LAST:event_lstLandparcelsMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstPagesValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstPagesValueChanged
         if (!evt.getValueIsAdjusting()) {
             final Object page = lstPages.getSelectedValue();
 
             if (page instanceof Integer) {
                 loadPage(((Integer)page) - 1);
             }
         }
     } //GEN-LAST:event_lstPagesValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togBildActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togBildActionPerformed
         loadBild();
     }                                                                           //GEN-LAST:event_togBildActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togGrenzniederschriftActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togGrenzniederschriftActionPerformed
         loadGrenzniederschrift();
     }                                                                                         //GEN-LAST:event_togGrenzniederschriftActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddLandparcelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddLandparcelActionPerformed
         flurstueckDialog.setCurrentListToAdd(CidsBeanSupport.getBeanCollectionFromProperty(
                 cidsBean,
                 "flurstuecksvermessung"));
 
         StaticSwingTools.showDialog(StaticSwingTools.getParentFrame(this), flurstueckDialog, true);
     } //GEN-LAST:event_btnAddLandparcelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveLandparcelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRemoveLandparcelActionPerformed
         final Object[] selection = lstLandparcels.getSelectedValues();
 
         if ((selection != null) && (selection.length > 0)) {
             final int answer = JOptionPane.showConfirmDialog(
                     StaticSwingTools.getParentFrame(this),
                     "Soll das Flurstück wirklich gelöscht werden?",
                     "Flurstück entfernen",
                     JOptionPane.YES_NO_OPTION);
 
             if (answer == JOptionPane.YES_OPTION) {
                 final Collection flurstuecke = CidsBeanSupport.getBeanCollectionFromProperty(
                         cidsBean,
                         "flurstuecksvermessung");
 
                 if (flurstuecke != null) {
                     for (final Object flurstueckToRemove : selection) {
                         try {
                             flurstuecke.remove(flurstueckToRemove);
                         } catch (Exception e) {
                             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Löschen", e, this);
                         } finally {
                             btnCombineGeometries.setEnabled(lstLandparcels.getModel().getSize() > 0);
                         }
                     }
                 }
             }
         }
     } //GEN-LAST:event_btnRemoveLandparcelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstLandparcelsValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstLandparcelsValueChanged
         if (!evt.getValueIsAdjusting()) {
             btnRemoveLandparcel.setEnabled(!readOnly && (lstLandparcels.getSelectedIndex() > -1));
         }
     }                                                                                         //GEN-LAST:event_lstLandparcelsValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCombineGeometriesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCombineGeometriesActionPerformed
         if (cidsBean == null) {
             return;
         }
 
         Geometry union = null;
         final Collection<CidsBean> flurstuecksvermessungen = cidsBean.getBeanCollectionProperty(
                 "flurstuecksvermessung");
         for (final CidsBean flurstuecksvermessung : flurstuecksvermessungen) {
             if (flurstuecksvermessung.getProperty("flurstueck.flurstueck.umschreibendes_rechteck.geo_field")
                         instanceof Geometry) {
                 final Geometry geometry = (Geometry)flurstuecksvermessung.getProperty(
                         "flurstueck.flurstueck.umschreibendes_rechteck.geo_field");
                 final Geometry transformedGeometry = CrsTransformer.transformToGivenCrs(
                         geometry,
                         AlkisConstants.COMMONS.SRS_SERVICE);
 
                 if (union == null) {
                     union = transformedGeometry;
                 } else {
                     union = union.union(transformedGeometry);
                 }
             }
         }
 
         if (union == null) {
             LOG.warn("Could not find geometries on given landparcels. Did not attach a new geometry.");
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 "Keines der betroffenen Flurstücke weist eine Geometrie auf.",
                 "Keine Geometrie erstellt",
                 JOptionPane.WARNING_MESSAGE);
             return;
         }
 
         final Map<String, Object> properties = new HashMap<String, Object>();
         properties.put("geo_field", union);
 
         try {
             final CidsBean geomBean = CidsBeanSupport.createNewCidsBeanFromTableName("geom", properties);
             geomBean.persist();
             cidsBean.setProperty("geometrie", geomBean);
         } catch (Exception ex) {
             // TODO: Tell user about error.
             LOG.error("Could set new geometry: '" + union.toText() + "'.", ex);
         }
     } //GEN-LAST:event_btnCombineGeometriesActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmbGeometrieStatusActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmbGeometrieStatusActionPerformed
         if (cmbGeometrieStatus.getSelectedItem() instanceof CidsBean) {
             final CidsBean geometrieStatus = (CidsBean)cmbGeometrieStatus.getSelectedItem();
 
             if (geometrieStatus.getProperty("id") instanceof Integer) {
                 cmbGeometrieStatus.setBackground(COLORS_GEOMETRIE_STATUS.get(
                         (Integer)geometrieStatus.getProperty("id")));
             }
         }
     } //GEN-LAST:event_cmbGeometrieStatusActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstLandparcelsMousePressed(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lstLandparcelsMousePressed
         if (!readOnly && popChangeVeraenderungsart.isPopupTrigger(evt)) {
             final int indexUnderMouse = lstLandparcels.locationToIndex(evt.getPoint());
 
             int[] selection = lstLandparcels.getSelectedIndices();
 
             boolean selectValueUnderMouse = true;
             if ((selection != null) && (selection.length > 0)) {
                 for (final int index : selection) {
                     if (index == indexUnderMouse) {
                         selectValueUnderMouse = false;
                     }
                 }
             }
 
             if (selectValueUnderMouse) {
                 lstLandparcels.setSelectedIndex(lstLandparcels.locationToIndex(evt.getPoint()));
                 selection = lstLandparcels.getSelectedIndices();
             }
 
             if ((selection != null) && (selection.length > 0)) {
                 popChangeVeraenderungsart.show(evt.getComponent(), evt.getX(), evt.getY());
             }
         }
     } //GEN-LAST:event_lstLandparcelsMousePressed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstLandparcelsMouseReleased(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lstLandparcelsMouseReleased
         // Hock for popup menu. The return value of JPopupMenu.isPopupTrigger() depends on the OS.
         lstLandparcelsMousePressed(evt);
     } //GEN-LAST:event_lstLandparcelsMouseReleased
 
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
             this.cidsBean = cidsBean;
 
             final List<CidsBean> flurstuecksvermessung = cidsBean.getBeanCollectionProperty("flurstuecksvermessung");
             if ((flurstuecksvermessung != null) && !flurstuecksvermessung.isEmpty()) {
                 Collections.sort(flurstuecksvermessung, AlphanumComparator.getInstance());
                 try {
                     cidsBean.setProperty("flurstuecksvermessung", flurstuecksvermessung);
                 } catch (final Exception ex) {
                     LOG.info("Couldn't sort the linked landparcels. Plausibility check of landparcels will fail.", ex);
                     // TODO: User feedback?
                 }
             }
 
             DefaultCustomObjectEditor.setMetaClassInformationToMetaClassStoreComponentsInBindingGroup(
                 bindingGroup,
                 this.cidsBean);
             bindingGroup.bind();
 
             lblTitle.setText(generateTitle());
             btnCombineGeometries.setEnabled(lstLandparcels.getModel().getSize() > 0);
             if ((cidsBean.getProperty("geometrie_status") instanceof CidsBean)
                         && (cidsBean.getProperty("geometrie_status.id") instanceof Integer)) {
                 cmbGeometrieStatus.setBackground(COLORS_GEOMETRIE_STATUS.get(
                         (Integer)cidsBean.getProperty("geometrie_status.id")));
             }
 
             schluessel = cidsBean.getProperty("schluessel");
             gemarkung = (cidsBean.getProperty("gemarkung") != null) ? cidsBean.getProperty("gemarkung.id") : null;
             flur = cidsBean.getProperty("flur");
             blatt = cidsBean.getProperty("blatt");
         }
 
         setCurrentDocumentNull();
 
         EventQueue.invokeLater(new RefreshDocumentWorker());
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         bindingGroup.unbind();
         // dispose panels here if necessary
         measuringComponent.dispose();
         if (flurstueckDialog != null) {
             flurstueckDialog.dispose();
         }
         if (!readOnly) {
             ((DefaultCismapGeometryComboBoxEditor)cmbGeometrie).dispose();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getTitleComponent() {
         return pnlTitle;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getFooterComponent() {
         return strFooter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getTitleBorder() {
         return new EmptyBorder(10, 10, 10, 10);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getFooterBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getCenterrBorder() {
         return new EmptyBorder(0, 5, 0, 5);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  event  DOCUMENT ME!
      */
     @Override
     public void editorClosed(final EditorClosedEvent event) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public boolean prepareForSave() {
         boolean save = true;
         final StringBuilder errorMessage = new StringBuilder();
 
         if (cmbSchluessel.getSelectedItem() == null) {
             LOG.warn("No 'schluessel' specified. Skip persisting.");
             errorMessage.append(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().noSchluessel"));
         }
         if (cmbGemarkung.getSelectedItem() == null) {
             LOG.warn("No 'gemarkung' specified. Skip persisting.");
             errorMessage.append(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().noGemarkung"));
         }
         if ((txtFlur.getText() == null) || txtFlur.getText().trim().isEmpty()) {
             LOG.warn("No 'flur' specified. Skip persisting.");
             errorMessage.append(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().noFlur"));
         } else if (txtFlur.getText().length() > 31) {
             LOG.warn("Property 'flur' is too long. Skip persisting.");
             errorMessage.append(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().tooLongFlur"));
         }
         if ((txtBlatt.getText() == null) || txtBlatt.getText().trim().isEmpty()) {
             LOG.warn("No 'blatt' specified. Skip persisting.");
             errorMessage.append(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().noBlatt"));
         } else if (txtBlatt.getText().length() > 31) {
             LOG.warn("Property 'blatt' is too long. Skip persisting.");
             errorMessage.append(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().tooLongBlatt"));
         }
 
         if (errorMessage.length() > 0) {
             save = false;
 
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().JOptionPane.message.prefix")
                         + errorMessage.toString()
                         + NbBundle.getMessage(
                             VermessungRissEditor.class,
                             "VermessungRissEditor.prepareForSave().JOptionPane.message.suffix"),
                 NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().JOptionPane.title"),
                 JOptionPane.WARNING_MESSAGE);
         }
         final Object newSchluessel = cidsBean.getProperty("schluessel");
         final Object newGemarkung = cidsBean.getProperty("gemarkung.id");
         final Object newFlur = cidsBean.getProperty("flur");
         final Object newBlatt = cidsBean.getProperty("blatt");
 
         final CidsVermessungRissSearchStatement search = new CidsVermessungRissSearchStatement(
                 newSchluessel.toString(),
                 newGemarkung.toString(),
                 newFlur.toString(),
                 newBlatt.toString(),
                 null,
                 null,
                 null);
 
         final Collection result;
         try {
             result = SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), search);
         } catch (final ConnectionException ex) {
             LOG.error("Could not check if the natural key of this measurement sketch is valid.", ex);
             JOptionPane.showMessageDialog(
                 this,
                 NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().noConnection.message"),
                 NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().noConnection.title"),
                 JOptionPane.WARNING_MESSAGE);
             return false;
         }
 
         if ((result != null) && !result.isEmpty()
                     && !(newSchluessel.equals(schluessel) && newGemarkung.equals(gemarkung) && newFlur.equals(flur)
                         && newBlatt.equals(blatt))) {
             save = false;
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("The given natural key of the measurement sketch already exists. Skip saving.");
             }
 
             JOptionPane.showMessageDialog(
                 this,
                 NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().keyExists.message"),
                 NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.prepareForSave().keyExists.title"),
                 JOptionPane.WARNING_MESSAGE);
         } else {
             save = true;
 
             try {
                 cidsBean.setProperty("letzteaenderung_datum", new Date(System.currentTimeMillis()));
                 cidsBean.setProperty("letzteaenderung_name", SessionManager.getSession().getUser().getName());
             } catch (final Exception ex) {
                 LOG.warn("Could not save date and user of last change.", ex);
                 // TODO: User feedback?
             }
         }
 
         if (save) {
             final CidsBean geometrieStatus = (CidsBean)cidsBean.getProperty("geometrie_status");
 
            if (geometrieStatus.getProperty("id") instanceof Integer) {
                 final Integer geometrieStatusId = (Integer)geometrieStatus.getProperty("id");
 
                 if (geometrieStatusId.intValue() == 6) {
                     final Object nameObj = cidsBean.getProperty("optimiert_name");
                     final String name;
 
                     if (nameObj instanceof String) {
                         name = (String)nameObj;
                     } else {
                         name = "";
                     }
 
                     if (name.isEmpty()) {
                         try {
                             cidsBean.setProperty("optimiert_datum", new Date(System.currentTimeMillis()));
                             cidsBean.setProperty("optimiert_name", SessionManager.getSession().getUser().getName());
                         } catch (final Exception ex) {
                             LOG.info("Couldn't save who changed when the geometry's state to '"
                                         + geometrieStatus.getProperty("name") + "'.",
                                 ex);
                             // TODO: User feedback?
                         }
                     }
                 }
             }
         }
 
         return save;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   host        DOCUMENT ME!
      * @param   gemarkung   DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      * @param   schluessel  DOCUMENT ME!
      * @param   blatt       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Map<URL, URL> getCorrespondingURLs(final String host,
             final Integer gemarkung,
             final String flur,
             final String schluessel,
             final String blatt) {
         final Map<URL, URL> validURLs = new HashMap<URL, URL>();
 
         String urlString = null;
         try {
             urlString = MessageFormat.format(
                     host,
                     schluessel,
                     gemarkung,
                     flur,
                     new Integer(Integer.parseInt(blatt)));
         } catch (final Exception ex) {
             LOG.warn("Can't build a valid URL for current measurement sketch.", ex);
             return validURLs;
         }
 
         for (final String suffix : SUFFIXES) {
             URL urlToTry = null;
             URL urlToTryReducedSize = null;
             try {
                 urlToTry = new URL(urlString + '.' + suffix);
             } catch (final MalformedURLException ex) {
                 LOG.warn("The URL '" + urlString.toString() + '.' + suffix
                             + "' is malformed. Can't load the corresponding picture.",
                     ex);
                 continue;
             }
 
             try {
                 urlToTryReducedSize = new URL(urlString + SUFFIX_REDUCED_SIZE + '.' + suffix);
             } catch (final MalformedURLException ex) {
                 LOG.warn("The URL '" + urlString.toString() + SUFFIX_REDUCED_SIZE + '.' + suffix
                             + "' is malformed. Can't load the corresponding picture.",
                     ex);
             }
 
             if (urlToTry != null) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("URL '" + urlToTry.toExternalForm() + "' is valid.");
                 }
 
                 validURLs.put(urlToTry, null);
             }
 
             if (urlToTryReducedSize != null) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("URL '" + urlToTry.toExternalForm() + "' is valid.");
                 }
 
                 validURLs.put(urlToTry, urlToTryReducedSize);
             }
         }
 
         return validURLs;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   property  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected String getSimplePropertyOfCurrentCidsBean(final String property) {
         String result = "";
 
         if (cidsBean != null) {
             if (cidsBean.getProperty(property) != null) {
                 result = (String)cidsBean.getProperty(property).toString();
             }
         }
 
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected String generateTitle() {
         if (cidsBean == null) {
             return "Bitte wählen Sie einen Vermessungsriss.";
         }
 
         final StringBuilder result = new StringBuilder();
         final Object schluessel = cidsBean.getProperty("schluessel");
         final Object flur = cidsBean.getProperty("flur");
         final Object blatt = cidsBean.getProperty("blatt");
 
         result.append("Schlüssel ");
         if ((schluessel instanceof String) && (((String)schluessel).trim().length() > 0)) {
             result.append(schluessel);
         } else {
             result.append("unbekannt");
         }
         result.append(" - ");
 
         result.append("Gemarkung ");
         String gemarkung = "unbekannt";
         if ((cidsBean.getProperty("gemarkung") instanceof CidsBean)
                     && (cidsBean.getProperty("gemarkung.name") instanceof String)) {
             final String gemarkungFromBean = (String)cidsBean.getProperty("gemarkung.name");
 
             if (gemarkungFromBean.trim().length() > 0) {
                 gemarkung = gemarkungFromBean;
             }
         }
         result.append(gemarkung);
         result.append(" - ");
 
         result.append("Flur ");
         if ((flur instanceof String) && (((String)flur).trim().length() > 0)) {
             result.append(flur);
         } else {
             result.append("unbekannt");
         }
         result.append(" - ");
 
         result.append("Blatt ");
         if ((blatt instanceof String) && (((String)blatt).trim().length() > 0)) {
             result.append(blatt);
         } else {
             result.append("unbekannt");
         }
 
         return result.toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected Integer getGemarkungOfCurrentCidsBean() {
         Integer result = Integer.valueOf(-1);
 
         if (cidsBean != null) {
             if (cidsBean.getProperty("gemarkung") != null) {
                 final Object gemarkung = cidsBean.getProperty("gemarkung.id");
                 if (gemarkung instanceof Integer) {
                     result = (Integer)gemarkung;
                 }
             }
         }
 
         return result;
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void loadBild() {
         currentSelectedButton = togBild;
         currentDocument = DOCUMENT_BILD;
 
         final URL url;
         if (documentURLs[currentDocument].getValue() != null) {
             url = documentURLs[currentDocument].getValue();
             lblHeaderDocument.setText(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.lblHeaderDocument.text.vermessungsriss") + " "
                         + NbBundle.getMessage(
                             VermessungRissEditor.class,
                             "VermessungRissEditor.lblHeaderDocument.text.suffixWarningReducedSize"));
         } else {
             url = documentURLs[currentDocument].getKey();
             lblHeaderDocument.setText(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.lblHeaderDocument.text.vermessungsriss"));
         }
 
         EventQueue.invokeLater(new PictureReaderWorker(url));
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void loadGrenzniederschrift() {
         currentSelectedButton = togGrenzniederschrift;
         currentDocument = DOCUMENT_GRENZNIEDERSCHRIFT;
 
         final URL url;
         if (documentURLs[currentDocument].getValue() != null) {
             url = documentURLs[currentDocument].getValue();
             lblHeaderDocument.setText(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.lblHeaderDocument.text.ergaenzendeDokumente") + " "
                         + NbBundle.getMessage(
                             VermessungRissEditor.class,
                             "VermessungRissEditor.lblHeaderDocument.text.suffixWarningReducedSize"));
         } else {
             url = documentURLs[currentDocument].getKey();
             lblHeaderDocument.setText(NbBundle.getMessage(
                     VermessungRissEditor.class,
                     "VermessungRissEditor.lblHeaderDocument.text.ergaenzendeDokumente"));
         }
 
         EventQueue.invokeLater(new PictureReaderWorker(url));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  page  DOCUMENT ME!
      */
     protected void loadPage(final int page) {
         final PictureSelectWorker oldWorkerTest = currentPictureSelectWorker;
         if (oldWorkerTest != null) {
             oldWorkerTest.cancel(true);
         }
 
         currentPictureSelectWorker = new PictureSelectWorker(page);
         EventQueue.invokeLater(currentPictureSelectWorker);
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void setCurrentDocumentNull() {
         currentDocument = NO_SELECTION;
         setCurrentPageNull();
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void setCurrentPageNull() {
         currentPage = NO_SELECTION;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  enabled  DOCUMENT ME!
      */
     protected void setDocumentControlsEnabled(final boolean enabled) {
         for (int i = 0; i < documentURLs.length; i++) {
             final JToggleButton current = documentButtons[i];
             current.setEnabled((documentURLs[i] != null) && enabled);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  errorOccurred  DOCUMENT ME!
      */
     protected void displayErrorOrEnableControls(final boolean errorOccurred) {
         measuringComponent.setVisible(!errorOccurred);
         btnHome.setEnabled(!errorOccurred);
         btnOpen.setEnabled(!errorOccurred);
         togPan.setEnabled(!errorOccurred);
         togZoom.setEnabled(!errorOccurred);
         lstPages.setEnabled(!errorOccurred);
 
         lblMissingDocuments.setVisible(false);
         lblErrorWhileLoadingBild.setVisible(false);
         lblErrorWhileLoadingGrenzniederschrift.setVisible(false);
 
         if (errorOccurred) {
             lstPages.setModel(new DefaultListModel());
 
             if (currentDocument == DOCUMENT_BILD) {
                 lblErrorWhileLoadingBild.setVisible(true);
             } else if (currentDocument == DOCUMENT_GRENZNIEDERSCHRIFT) {
                 lblErrorWhileLoadingGrenzniederschrift.setVisible(true);
             } else {
                 lblMissingDocuments.setVisible(true);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void closeReader() {
         if (pictureReader != null) {
             pictureReader.close();
             pictureReader = null;
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     //J-
 
     //When Wupp decides to publish the correspoding files on a WebDAV server, use following three classes.
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected final class PictureReaderWorker extends SwingWorker<ListModel, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final URL url;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PictureReaderWorker object.
          *
          * @param  url  DOCUMENT ME!
          */
         public PictureReaderWorker(final URL url) {
             this.url = url;
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Preparing picture reader for file " + this.url.toExternalForm());
             }
 
             lstPages.setModel(MODEL_LOAD);
             measuringComponent.removeAllFeatures();
             setDocumentControlsEnabled(false);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected ListModel doInBackground() throws Exception {
             final DefaultListModel model = new DefaultListModel();
 
             closeReader();
 
             try {
                 pictureReader = new MultiPagePictureReader(url, false, true);
             } catch (final Exception e) {
                 LOG.error("Could not create a MultiPagePictureReader for URL '" + url.toExternalForm() + "'.", e);
                 return model;
             }
 
             final int numberOfPages = pictureReader.getNumberOfPages();
 
             for (int i = 0; i < numberOfPages; ++i) {
                 model.addElement(i + 1);
             }
 
             return model;
         }
 
         @Override
         protected void done() {
             boolean enableControls = true;
             try {
                 final ListModel model = get();
                 lstPages.setModel(model);
 
                 if (model.getSize() > 0) {
                     lstPages.setSelectedIndex(0);
                     enableControls = false;
                 } else {
                     lstPages.setModel(new DefaultListModel());
                 }
             } catch (InterruptedException ex) {
                 setCurrentDocumentNull();
                 displayErrorOrEnableControls(true);
                 closeReader();
                 LOG.warn("Reading found pictures was interrupted.", ex);
             } catch (ExecutionException ex) {
                 setCurrentDocumentNull();
                 displayErrorOrEnableControls(true);
                 closeReader();
                 LOG.error("Could not read found pictures.", ex);
             } finally {
                 // We don't want to enable the controls if we set the selected index in lstPages. Calling
                 // lstPages.setSelectedIndex(0)
                 // invokes a PictureSelectWorker and thus disables the controls.
                 if (enableControls) {
                     setDocumentControlsEnabled(true);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected final class PictureSelectWorker extends SwingWorker<BufferedImage, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final int pageNumber;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PictureSelectWorker object.
          *
          * @param  pageNumber  DOCUMENT ME!
          */
         public PictureSelectWorker(final int pageNumber) {
             this.pageNumber = pageNumber;
             setCurrentPageNull();
             setDocumentControlsEnabled(false);
             measuringComponent.reset();
             btnHome.setEnabled(false);
             btnOpen.setEnabled(false);
             togPan.setEnabled(false);
             togZoom.setEnabled(false);
             lstPages.setEnabled(false);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected BufferedImage doInBackground() throws Exception {
             if (pictureReader != null) {
                 return pictureReader.loadPage(pageNumber);
             }
 
             throw new IllegalStateException("PictureReader is null!");
         }
 
         @Override
         protected void done() {
             try {
                 if (!isCancelled()) {
                     currentPage = pageNumber;
                     measuringComponent.addImage(get());
                     togPan.setSelected(true);
                     measuringComponent.zoomToFeatureCollection();
                     displayErrorOrEnableControls(false);
                 }
             } catch (final InterruptedException ex) {
                 setCurrentPageNull();
                 displayErrorOrEnableControls(true);
                 LOG.warn("Was interrupted while setting new image.", ex);
             } catch (final Exception ex) {
                 setCurrentPageNull();
                 displayErrorOrEnableControls(true);
                 LOG.error("Could not set new image.", ex);
             } finally {
                 setDocumentControlsEnabled(true);
                 currentPictureSelectWorker = null;
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected final class RefreshDocumentWorker extends SwingWorker<Void, Object> {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new RefreshDocumentWorker object.
          */
         public RefreshDocumentWorker() {
             lblMissingDocuments.setVisible(false);
             lblErrorWhileLoadingBild.setVisible(false);
             lblErrorWhileLoadingGrenzniederschrift.setVisible(false);
             togBild.setEnabled(false);
             togGrenzniederschrift.setEnabled(false);
             lstPages.setModel(MODEL_LOAD);
             btnHome.setEnabled(false);
             btnOpen.setEnabled(false);
             togPan.setEnabled(false);
             togZoom.setEnabled(false);
             setCurrentDocumentNull();
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected Void doInBackground() throws Exception {
             documentURLs[DOCUMENT_BILD] = null;
             documentURLs[DOCUMENT_GRENZNIEDERSCHRIFT] = null;
 
             final CidsBean cidsBean = getCidsBean();
             if (cidsBean == null) {
                 return null;
             }
 
             final Map<URL, URL> validBildURLs = getCorrespondingURLs(
                     AlkisConstants.COMMONS.VERMESSUNG_HOST_BILDER,
                     getGemarkungOfCurrentCidsBean(),
                     getSimplePropertyOfCurrentCidsBean("flur"),
                     getSimplePropertyOfCurrentCidsBean("schluessel"),
                     getSimplePropertyOfCurrentCidsBean("blatt"));
             final Map<URL, URL> validGrenzniederschriftURLs = getCorrespondingURLs(
                     AlkisConstants.COMMONS.VERMESSUNG_HOST_GRENZNIEDERSCHRIFTEN,
                     getGemarkungOfCurrentCidsBean(),
                     getSimplePropertyOfCurrentCidsBean("flur"),
                     getSimplePropertyOfCurrentCidsBean("schluessel"),
                     getSimplePropertyOfCurrentCidsBean("blatt"));
 
             tryToLoadImageFromURLs(validBildURLs, DOCUMENT_BILD);
             tryToLoadImageFromURLs(validGrenzniederschriftURLs, DOCUMENT_GRENZNIEDERSCHRIFT);
 
             return null;
         }
 
         private void tryToLoadImageFromURLs(final Map<URL, URL> urls, final int type) {
             InputStream streamToReadFrom = null;
 
             //First we try to load an image of reduced size
             for (final Map.Entry<URL, URL> urlEntry : urls.entrySet()) {
                 if(urlEntry.getValue() == null) {
                     continue;
                 }
 
                 try {
                     streamToReadFrom = WebAccessManager.getInstance().doRequest(urlEntry.getValue());
                     documentURLs[type] = urlEntry;
                     break;
                 } catch (final Exception ex) {
                     LOG.debug("An exception occurred while opening URL '" + urlEntry.getValue().toExternalForm()
                                 + "'. Skipping this url.", ex);
                 } finally {
                     if (streamToReadFrom != null) {
                         try {
                             streamToReadFrom.close();
                         } catch (final IOException ex) {
                         }
                     }
                 }
             }
 
             if(documentURLs[type] != null) {
                 // Found an image.
                 return;
             }
 
             for (final Map.Entry<URL, URL> urlEntry : urls.entrySet()) {
                 try {
                     streamToReadFrom = WebAccessManager.getInstance().doRequest(urlEntry.getKey());
                     documentURLs[type] = urlEntry;
                     break;
                 } catch (final Exception ex) {
                     LOG.debug("An exception occurred while opening URL '" + urlEntry.getKey().toExternalForm()
                                 + "'. Skipping this url.", ex);
                 } finally {
                     if (streamToReadFrom != null) {
                         try {
                             streamToReadFrom.close();
                         } catch (final IOException ex) {
                         }
                     }
                 }
             }
 
             if(documentURLs[type] != null) {
                 documentURLs[type].setValue(null);
             }
         }
 
         @Override
         protected void done() {
             try {
                 if (!isCancelled()) {
                     get();
                 }
             } catch (InterruptedException ex) {
                 LOG.warn("Was interrupted while refreshing document.", ex);
             } catch (ExecutionException ex) {
                 LOG.warn("There was an exception while refreshing document.", ex);
             }
 
             if ((documentURLs[DOCUMENT_BILD] == null) && (documentURLs[DOCUMENT_GRENZNIEDERSCHRIFT] == null)) {
                 measuringComponent.setVisible(false);
                 lblMissingDocuments.setVisible(true);
                 lstPages.setModel(new DefaultListModel());
                 lstPages.setEnabled(false);
             } else {
                 if (documentURLs[DOCUMENT_BILD] != null) {
                     togBild.setEnabled(true);
                     togBild.setSelected(true);
                     currentSelectedButton = togBild;
                     currentDocument = DOCUMENT_BILD;
                 }
 
                 if (documentURLs[DOCUMENT_GRENZNIEDERSCHRIFT] != null) {
                     togGrenzniederschrift.setEnabled(true);
 
                     if (currentDocument == NO_SELECTION) {
                         togGrenzniederschrift.setSelected(true);
                         currentSelectedButton = togGrenzniederschrift;
                         currentDocument = DOCUMENT_GRENZNIEDERSCHRIFT;
                     }
                 }
 
                 if(currentDocument == DOCUMENT_BILD) {
                     loadBild();
                 } else if(currentDocument == DOCUMENT_GRENZNIEDERSCHRIFT) {
                     loadGrenzniederschrift();
                 }
             }
         }
     }
 
     //J+
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class EnableCombineGeometriesButton extends WindowAdapter {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void windowDeactivated(final WindowEvent e) {
             super.windowDeactivated(e);
 
             btnCombineGeometries.setEnabled(lstLandparcels.getModel().getSize() > 0);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void windowClosed(final WindowEvent e) {
             super.windowClosed(e);
 
             btnCombineGeometries.setEnabled(lstLandparcels.getModel().getSize() > 0);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class HighlightReferencingFlurstueckeCellRenderer extends JLabel implements ListCellRenderer {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   list          DOCUMENT ME!
          * @param   value         DOCUMENT ME!
          * @param   index         DOCUMENT ME!
          * @param   isSelected    DOCUMENT ME!
          * @param   cellHasFocus  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public Component getListCellRendererComponent(final JList list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             setOpaque(true);
 
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
 
             final String errorWhileLoading = "Fehler beim Laden des Flurstücks";
 
             final StringBuilder result = new StringBuilder();
             if (value instanceof CidsBean) {
                 final CidsBean vermessung = (CidsBean)value;
 
                 if (vermessung.getProperty("flurstueck") instanceof CidsBean) {
                     final CidsBean flurstueck = (CidsBean)vermessung.getProperty("flurstueck");
 
                     if (flurstueck.getProperty("gemarkung") != null) {
                         final Object gemarkung = flurstueck.getProperty("gemarkung.name");
 
                         if ((gemarkung instanceof String) && (((String)gemarkung).trim().length() > 0)) {
                             result.append(gemarkung);
                         } else {
                             result.append(flurstueck.getProperty("gemarkung.id"));
                         }
                     } else {
                         result.append("Unbekannte Gemarkung");
                     }
 
                     result.append("-");
                     result.append(flurstueck.getProperty("flur"));
                     result.append("-");
                     result.append(flurstueck.getProperty("zaehler"));
                     final Object nenner = flurstueck.getProperty("nenner");
                     result.append('/');
                     if (nenner != null) {
                         result.append(nenner);
                     } else {
                         result.append('0');
                     }
 
                     if (flurstueck.getProperty("flurstueck") instanceof CidsBean) {
                         if (isSelected) {
                             setBackground(list.getSelectionBackground());
                             setForeground(list.getSelectionForeground());
                         } else {
                             setBackground(list.getBackground());
                             setForeground(Color.blue);
                         }
                     }
                 } else {
                     result.append(errorWhileLoading);
                 }
 
                 if (vermessung.getProperty("veraenderungsart") != null) {
                     result.append(" (");
 
                     final Object vermessungsart = vermessung.getProperty("veraenderungsart.name");
                     if ((vermessungsart instanceof String) && (((String)vermessungsart).trim().length() > 0)) {
                         result.append(vermessungsart);
                     } else {
                         result.append(vermessung.getProperty("veraenderungsart.code"));
                     }
 
                     result.append(')');
                 }
             } else {
                 result.append(errorWhileLoading);
             }
 
             setText(result.toString());
 
             return this;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class GeometrieStatusRenderer implements ListCellRenderer {
 
         //~ Instance fields ----------------------------------------------------
 
         private ListCellRenderer originalRenderer;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new GeometrieStatusRenderer object.
          *
          * @param  originalRenderer  DOCUMENT ME!
          */
         public GeometrieStatusRenderer(final ListCellRenderer originalRenderer) {
             this.originalRenderer = originalRenderer;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   list          DOCUMENT ME!
          * @param   value         DOCUMENT ME!
          * @param   index         DOCUMENT ME!
          * @param   isSelected    DOCUMENT ME!
          * @param   cellHasFocus  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public Component getListCellRendererComponent(final JList list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             final Component result = originalRenderer.getListCellRendererComponent(
                     list,
                     value,
                     index,
                     isSelected,
                     cellHasFocus);
 
             if (isSelected) {
                 result.setBackground(list.getSelectionBackground());
                 result.setForeground(list.getSelectionForeground());
             } else {
                 result.setBackground(list.getBackground());
                 result.setForeground(list.getForeground());
 
                 if (value instanceof CidsBean) {
                     final CidsBean geometrieStatus = (CidsBean)value;
                     if (geometrieStatus.getProperty("id") instanceof Integer) {
                         result.setBackground(COLORS_GEOMETRIE_STATUS.get((Integer)geometrieStatus.getProperty("id")));
                     }
                 }
             }
 
             return result;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ChangeVeraenderungsartAction extends AbstractAction {
 
         //~ Instance fields ----------------------------------------------------
 
         private final CidsBean veraenderungsart;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ChangeVeraenderungsartAction object.
          *
          * @param  veraenderungsart  DOCUMENT ME!
          */
         public ChangeVeraenderungsartAction(final CidsBean veraenderungsart) {
             this.veraenderungsart = veraenderungsart;
 
             putValue(
                 NAME,
                 this.veraenderungsart.getProperty("code")
                         + " - "
                         + this.veraenderungsart.getProperty("name"));
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             for (final Object flurstuecksvermessung : lstLandparcels.getSelectedValues()) {
                 try {
                     ((CidsBean)flurstuecksvermessung).setProperty("veraenderungsart", veraenderungsart);
                     lstLandparcels.clearSelection();
                     lstLandparcels.revalidate();
                     lstLandparcels.repaint();
                 } catch (final Exception ex) {
                     LOG.info("Couldn't set veraenderungsart to '" + veraenderungsart + "' for flurstuecksvermessung '"
                                 + flurstuecksvermessung + "'.",
                         ex);
                     // TODO: User feedback?
                 }
             }
         }
     }
 
     //J-
     private final class DocumentSizeFilter extends DocumentFilter {
         @Override
         public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
             if ((fb.getDocument().getLength() + string.length()) <= 31) {
                 super.insertString(fb, offset, string, attr);
             }
         }
 
         @Override
         public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
             if ((fb.getDocument().getLength() + text.length() - length) <= 31) {
                 super.replace(fb, offset, length, text, attrs);
             }
         }
     }
     //J+
 }
