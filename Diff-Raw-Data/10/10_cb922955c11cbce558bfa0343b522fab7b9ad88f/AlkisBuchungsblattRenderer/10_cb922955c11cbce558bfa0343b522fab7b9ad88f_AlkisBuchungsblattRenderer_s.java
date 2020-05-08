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
 /*
  * Alkis_pointRenderer.java
  *
  * Created on 10.09.2009, 15:52:16
  */
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 import de.aedsicad.aaaweb.service.alkis.info.ALKISInfoServices;
 import de.aedsicad.aaaweb.service.util.Buchungsblatt;
 import de.aedsicad.aaaweb.service.util.Buchungsstelle;
 import de.aedsicad.aaaweb.service.util.LandParcel;
 import de.aedsicad.aaaweb.service.util.Offices;
 import de.aedsicad.aaaweb.service.util.Owner;
 
 import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
 import edu.umd.cs.piccolo.event.PInputEvent;
 
 import org.jdesktop.swingbinding.JListBinding;
 import org.jdesktop.swingx.graphics.ReflectionRenderer;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.LayoutManager;
 import java.awt.Paint;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.Caret;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.StyleSheet;
 
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisSOAPWorkerService;
 import de.cismet.cids.custom.utils.alkis.SOAPAccessProvider;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.TitleComponentProvider;
 import java.util.HashMap;
 import javax.swing.JOptionPane;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class AlkisBuchungsblattRenderer extends javax.swing.JPanel implements CidsBeanRenderer,
         BorderProvider,
         TitleComponentProvider,
         FooterComponentProvider {
 
     //~ Static fields/initializers ---------------------------------------------
     private static final Color[] COLORS = new Color[]{
         new Color(41, 86, 178),
         new Color(101, 156, 239),
         new Color(125, 189, 0),
         new Color(220, 246, 0),
         new Color(255, 91, 0)
     };
     public static final List<Color> LANDPARCEL_COLORS = Collections.unmodifiableList(Arrays.asList(COLORS));
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
             AlkisBuchungsblattRenderer.class);
     private static final String ICON_RES_PACKAGE = "/de/cismet/cids/custom/wunda_blau/res/";
     private static final String ALKIS_RES_PACKAGE = ICON_RES_PACKAGE + "alkis/";
     private static final String CARD_1 = "CARD_1";
     private static final String CARD_2 = "CARD_2";
     //
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW = "custom.alkis.product.bestandsnachweis_nrw";
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM = "custom.alkis.product.bestandsnachweis_kom";
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN = "custom.alkis.product.bestandsnachweis_kom_intern";
     private static final String PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW = "custom.alkis.product.grundstuecksnachweis_nrw";
     //~ Instance fields --------------------------------------------------------
 // private ImageIcon FORWARD_PRESSED;
 // private ImageIcon FORWARD_SELECTED;
 // private ImageIcon BACKWARD_PRESSED;
 // private ImageIcon BACKWARD_SELECTED;
     private ImageIcon BESTAND_NRW_PDF;
     private ImageIcon BESTAND_NRW_HTML;
     private ImageIcon BESTAND_KOM_PDF;
     private ImageIcon BESTAND_KOM_HTML;
     private ImageIcon GRUND_NRW_PDF;
     private ImageIcon GRUND_NRW_HTML;
     //
     private final List<LightweightLandParcel> landParcelList;
     private final MappingComponent map;
     //
     private RetrieveWorker retrieveWorker;
     private SOAPAccessProvider soapProvider;
     private ALKISInfoServices infoService;
     private Buchungsblatt buchungsblatt;
     private CidsBean cidsBean;
     private String title;
     private final JListBinding landparcelListBinding;
     private final CardLayout cardLayout;
     private final Map<Object, ImageIcon> productPreviewImages;
     private boolean continueInBackground = false;
 //    private List<MetaObject> realLandParcelMetaObjectsCache = null;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXBusyLabel blWait;
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnForward;
     private javax.swing.JEditorPane epOwner;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisKomHtml;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisKomInternHtml;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisKomInternPdf;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisKomPdf;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisNrwHtml;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisNrwPdf;
     private org.jdesktop.swingx.JXHyperlink hlGrundstuecksnachweisNrwHtml;
     private org.jdesktop.swingx.JXHyperlink hlGrundstuecksnachweisNrwPdf;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JLabel lblAmtgericht;
     private javax.swing.JLabel lblBack;
     private javax.swing.JLabel lblBlattart;
     private javax.swing.JLabel lblBuchungsart;
     private javax.swing.JLabel lblDescAmtsgericht;
     private javax.swing.JLabel lblDescBlattart;
     private javax.swing.JLabel lblDescBuchungsart;
     private javax.swing.JLabel lblDescGrundbuchbezirk;
     private javax.swing.JLabel lblDescKatasteramt;
     private javax.swing.JLabel lblForw;
     private javax.swing.JLabel lblGrundbuchbezirk;
     private javax.swing.JLabel lblHeadEigentuemer;
     private javax.swing.JLabel lblHeadFlurstuecke;
     private javax.swing.JLabel lblHeadMainInfo;
     private javax.swing.JLabel lblHeadProdukte;
     private javax.swing.JLabel lblHeadProdukte1;
     private javax.swing.JLabel lblKatasteramt;
     private javax.swing.JLabel lblPreviewHead;
     private javax.swing.JLabel lblProductPreview;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JList lstLandparcels;
     private javax.swing.JPanel panButtons;
     private javax.swing.JPanel panContent;
     private javax.swing.JPanel panEigentuemer;
     private javax.swing.JPanel panFooter;
     private javax.swing.JPanel panFooterLeft;
     private javax.swing.JPanel panFooterRight;
     private javax.swing.JPanel panGrundstuecke;
     private javax.swing.JPanel panInfo;
     private javax.swing.JPanel panKarte;
     private javax.swing.JPanel panProductPreview;
     private javax.swing.JPanel panProducts;
     private javax.swing.JPanel panProdukteHTML;
     private javax.swing.JPanel panProduktePDF;
     private javax.swing.JPanel panTitle;
     private javax.swing.JScrollPane scpLandparcels;
     private javax.swing.JScrollPane scpOwner;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel3;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeadContent;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeadEigentuemer;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeadGrundstuecke;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeadProdukte;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeadProdukte1;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
     /**
      * Creates new form Alkis_pointRenderer.
      */
     public AlkisBuchungsblattRenderer() {
         map = new MappingComponent();
 //        landParcelFeatureMap = TypeSafeCollections.newHashMap();
         map.setOpaque(false);
         landParcelList = TypeSafeCollections.newArrayList();
         productPreviewImages = TypeSafeCollections.newHashMap();
         try {
             soapProvider = new SOAPAccessProvider();
             infoService = soapProvider.getAlkisInfoService();
         } catch (Exception ex) {
             log.fatal(ex, ex);
         }
         initIcons();
         initComponents();
         initFooterElements();
         initProductPreview();
         final LayoutManager layoutManager = getLayout();
         if (layoutManager instanceof CardLayout) {
             cardLayout = (CardLayout) layoutManager;
             cardLayout.show(this, CARD_1);
         } else {
             cardLayout = new CardLayout();
             log.error("Alkis_buchungsblattRenderer exspects CardLayout as major layout manager, but has " + getLayout()
                     + "!");
         }
         scpOwner.getViewport().setOpaque(false);
         scpLandparcels.getViewport().setOpaque(false);
         panKarte.add(map, BorderLayout.CENTER);
         initEditorPanes();
         lstLandparcels.setCellRenderer(new FancyListCellRenderer());
         final org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create(
                 "${landParcelList}");
         landparcelListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ,
                 this,
                 eLProperty,
                 lstLandparcels);
         landparcelListBinding.setSourceNullValue(null);
         landparcelListBinding.setSourceUnreadableValue(null);
         if (!AlkisUtils.validateUserHasAlkisProductAccess()) {
             // disable Product page if user does not have the right to see it.
             btnForward.setEnabled(false);
             lblForw.setEnabled(false);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
     /**
      * DOCUMENT ME!
      */
     private void initIcons() {
         final ReflectionRenderer reflectionRenderer = new ReflectionRenderer(0.5f, 0.15f, false);
 //        BACKWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-sel.png"));
 //        BACKWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-pressed.png"));
 //
 //        FORWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-sel.png"));
 //        FORWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-pressed.png"));
         BufferedImage i1 = null;
         BufferedImage i2 = null;
         BufferedImage i3 = null;
         BufferedImage i4 = null;
         BufferedImage i5 = null;
         BufferedImage i6 = null;
         try {
             //TODO: Richtige Screenshots machen und zuordnen!
             i1 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bestandsnachweispdf.png")));
             i2 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bestandsachweishtml.png")));
             i3 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bestandsnachweispdf.png")));
             i4 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bestandsachweishtml.png")));
             i5 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bestandsnachweispdf.png")));
             i6 = reflectionRenderer.appendReflection(ImageIO.read(
                     getClass().getResource(ALKIS_RES_PACKAGE + "bestandsachweishtml.png")));
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         BESTAND_NRW_PDF = new ImageIcon(i1);
         BESTAND_NRW_HTML = new ImageIcon(i2);
         BESTAND_KOM_PDF = new ImageIcon(i3);
         BESTAND_KOM_HTML = new ImageIcon(i4);
         GRUND_NRW_PDF = new ImageIcon(i5);
         GRUND_NRW_HTML = new ImageIcon(i6);
     }
 
     private void showNoProductPermissionWarning() {
         JOptionPane.showMessageDialog(this, "Sie besitzen keine Berechtigung zur Erzeugung dieses Produkts!");
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initFooterElements() {
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
                 lblForw,
                 btnForward,
                 ObjectRendererUtils.FORWARD_SELECTED,
                 ObjectRendererUtils.FORWARD_PRESSED);
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
                 lblBack,
                 btnBack,
                 ObjectRendererUtils.BACKWARD_SELECTED,
                 ObjectRendererUtils.BACKWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblForw, btnForward, FORWARD_SELECTED, FORWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblBack, btnBack, BACKWARD_SELECTED, BACKWARD_PRESSED);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initProductPreview() {
         initProductPreviewImages();
         int maxX = 0;
         int maxY = 0;
         for (final ImageIcon ii : productPreviewImages.values()) {
             if (ii.getIconWidth() > maxX) {
                 maxX = ii.getIconWidth();
             }
             if (ii.getIconHeight() > maxY) {
                 maxY = ii.getIconHeight();
             }
         }
         final Dimension previewDim = new Dimension(maxX + 20, maxY + 40);
         ObjectRendererUtils.setAllDimensions(panProductPreview, previewDim);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initProductPreviewImages() {
         productPreviewImages.put(hlBestandsnachweisNrwPdf, BESTAND_NRW_PDF);
         productPreviewImages.put(hlBestandsnachweisNrwHtml, BESTAND_NRW_HTML);
         productPreviewImages.put(hlBestandsnachweisKomPdf, BESTAND_KOM_PDF);
         productPreviewImages.put(hlBestandsnachweisKomHtml, BESTAND_KOM_HTML);
         productPreviewImages.put(hlBestandsnachweisKomInternPdf, BESTAND_KOM_PDF);
         productPreviewImages.put(hlBestandsnachweisKomInternHtml, BESTAND_KOM_HTML);
         productPreviewImages.put(hlGrundstuecksnachweisNrwPdf, GRUND_NRW_PDF);
         productPreviewImages.put(hlGrundstuecksnachweisNrwHtml, GRUND_NRW_HTML);
         final ProductLabelMouseAdaper productListener = new ProductLabelMouseAdaper();
         hlBestandsnachweisNrwHtml.addMouseListener(productListener);
         hlBestandsnachweisNrwPdf.addMouseListener(productListener);
         hlBestandsnachweisKomHtml.addMouseListener(productListener);
         hlBestandsnachweisKomPdf.addMouseListener(productListener);
         hlBestandsnachweisKomInternHtml.addMouseListener(productListener);
         hlBestandsnachweisKomInternPdf.addMouseListener(productListener);
         hlGrundstuecksnachweisNrwHtml.addMouseListener(productListener);
         hlGrundstuecksnachweisNrwPdf.addMouseListener(productListener);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initEditorPanes() {
         // Font and Layout
         final Font font = UIManager.getFont("Label.font");
         final String bodyRule = "body { font-family: " + font.getFamily() + "; "
                 + "font-size: " + font.getSize() + "pt; }";
         final String tableRule = "td { padding-right : 15px; }";
         final String tableHeadRule = "th { padding-right : 15px; }";
         final StyleSheet css = ((HTMLEditorKit) epOwner.getEditorKit()).getStyleSheet();
 
         css.addRule(bodyRule);
         css.addRule(tableRule);
         css.addRule(tableHeadRule);
 
         // Change scroll behaviour: avoid autoscrolls on setText(...)
         final Caret caret = epOwner.getCaret();
         if (caret instanceof DefaultCaret) {
             final DefaultCaret dCaret = (DefaultCaret) caret;
             dCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         panTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         blWait = new org.jdesktop.swingx.JXBusyLabel();
         panFooter = new javax.swing.JPanel();
         panButtons = new javax.swing.JPanel();
         panFooterLeft = new javax.swing.JPanel();
         lblBack = new javax.swing.JLabel();
         btnBack = new javax.swing.JButton();
         panFooterRight = new javax.swing.JPanel();
         btnForward = new javax.swing.JButton();
         lblForw = new javax.swing.JLabel();
         panInfo = new javax.swing.JPanel();
         panContent = new RoundedPanel();
         jPanel1 = new javax.swing.JPanel();
         lblDescKatasteramt = new javax.swing.JLabel();
         lblDescAmtsgericht = new javax.swing.JLabel();
         lblDescGrundbuchbezirk = new javax.swing.JLabel();
         lblAmtgericht = new javax.swing.JLabel();
         lblGrundbuchbezirk = new javax.swing.JLabel();
         lblKatasteramt = new javax.swing.JLabel();
         jPanel5 = new javax.swing.JPanel();
         jPanel6 = new javax.swing.JPanel();
         lblDescBlattart = new javax.swing.JLabel();
         lblBlattart = new javax.swing.JLabel();
         lblDescBuchungsart = new javax.swing.JLabel();
         lblBuchungsart = new javax.swing.JLabel();
         srpHeadContent = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadMainInfo = new javax.swing.JLabel();
         panEigentuemer = new RoundedPanel();
         jPanel3 = new javax.swing.JPanel();
         scpOwner = new javax.swing.JScrollPane();
         epOwner = new javax.swing.JEditorPane();
         srpHeadEigentuemer = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadEigentuemer = new javax.swing.JLabel();
         panGrundstuecke = new RoundedPanel();
         jPanel4 = new javax.swing.JPanel();
         scpLandparcels = new javax.swing.JScrollPane();
         lstLandparcels = new javax.swing.JList();
         srpHeadGrundstuecke = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadFlurstuecke = new javax.swing.JLabel();
         panKarte = new javax.swing.JPanel();
         panProducts = new javax.swing.JPanel();
         panProduktePDF = new RoundedPanel();
         hlBestandsnachweisNrwPdf = new org.jdesktop.swingx.JXHyperlink();
         jPanel7 = new javax.swing.JPanel();
         jPanel8 = new javax.swing.JPanel();
         srpHeadProdukte = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadProdukte = new javax.swing.JLabel();
         hlBestandsnachweisKomPdf = new org.jdesktop.swingx.JXHyperlink();
         hlGrundstuecksnachweisNrwPdf = new org.jdesktop.swingx.JXHyperlink();
         hlBestandsnachweisKomInternPdf = new org.jdesktop.swingx.JXHyperlink();
         jPanel9 = new javax.swing.JPanel();
         panProductPreview = new RoundedPanel();
         lblProductPreview = new javax.swing.JLabel();
         semiRoundedPanel3 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblPreviewHead = new javax.swing.JLabel();
         panProdukteHTML = new RoundedPanel();
         srpHeadProdukte1 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadProdukte1 = new javax.swing.JLabel();
         hlBestandsnachweisNrwHtml = new org.jdesktop.swingx.JXHyperlink();
         jPanel2 = new javax.swing.JPanel();
         jPanel10 = new javax.swing.JPanel();
         hlBestandsnachweisKomHtml = new org.jdesktop.swingx.JXHyperlink();
         hlGrundstuecksnachweisNrwHtml = new org.jdesktop.swingx.JXHyperlink();
         hlBestandsnachweisKomInternHtml = new org.jdesktop.swingx.JXHyperlink();
 
         panTitle.setOpaque(false);
         panTitle.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18));
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText("TITLE");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panTitle.add(lblTitle, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 5);
         panTitle.add(blWait, gridBagConstraints);
 
         panFooter.setOpaque(false);
         panFooter.setLayout(new java.awt.BorderLayout());
 
         panButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
         panButtons.setOpaque(false);
         panButtons.setLayout(new java.awt.GridBagLayout());
 
         panFooterLeft.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setMinimumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setOpaque(false);
         panFooterLeft.setPreferredSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
 
         lblBack.setFont(new java.awt.Font("Tahoma", 1, 14));
         lblBack.setForeground(new java.awt.Color(255, 255, 255));
         lblBack.setText("Info");
         lblBack.setEnabled(false);
         lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblBackMouseClicked(evt);
             }
         });
         panFooterLeft.add(lblBack);
 
         btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left.png"))); // NOI18N
         btnBack.setBorder(null);
         btnBack.setBorderPainted(false);
         btnBack.setContentAreaFilled(false);
         btnBack.setEnabled(false);
         btnBack.setFocusPainted(false);
         btnBack.setMaximumSize(new java.awt.Dimension(30, 30));
         btnBack.setMinimumSize(new java.awt.Dimension(30, 30));
         btnBack.setPreferredSize(new java.awt.Dimension(30, 30));
         btnBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-pressed.png"))); // NOI18N
         btnBack.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-sel.png"))); // NOI18N
         btnBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBackActionPerformed(evt);
             }
         });
         panFooterLeft.add(btnBack);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panButtons.add(panFooterLeft, gridBagConstraints);
 
         panFooterRight.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterRight.setOpaque(false);
         panFooterRight.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         btnForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right.png"))); // NOI18N
         btnForward.setBorder(null);
         btnForward.setBorderPainted(false);
         btnForward.setContentAreaFilled(false);
         btnForward.setFocusPainted(false);
         btnForward.setMaximumSize(new java.awt.Dimension(30, 30));
         btnForward.setMinimumSize(new java.awt.Dimension(30, 30));
         btnForward.setPreferredSize(new java.awt.Dimension(30, 30));
         btnForward.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnForwardActionPerformed(evt);
             }
         });
         panFooterRight.add(btnForward);
 
         lblForw.setFont(new java.awt.Font("Tahoma", 1, 14));
         lblForw.setForeground(new java.awt.Color(255, 255, 255));
         lblForw.setText("Produkte");
         lblForw.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblForwMouseClicked(evt);
             }
         });
         panFooterRight.add(lblForw);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panButtons.add(panFooterRight, gridBagConstraints);
 
         panFooter.add(panButtons, java.awt.BorderLayout.CENTER);
 
         setLayout(new java.awt.CardLayout());
 
         panInfo.setOpaque(false);
         panInfo.setLayout(new java.awt.GridBagLayout());
 
         panContent.setLayout(new java.awt.BorderLayout());
 
         jPanel1.setOpaque(false);
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         lblDescKatasteramt.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescKatasteramt.setText("Katasteramt:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel1.add(lblDescKatasteramt, gridBagConstraints);
 
         lblDescAmtsgericht.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescAmtsgericht.setText("Amtsgericht:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         jPanel1.add(lblDescAmtsgericht, gridBagConstraints);
 
         lblDescGrundbuchbezirk.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescGrundbuchbezirk.setText("Grundbuchbezirk:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel1.add(lblDescGrundbuchbezirk, gridBagConstraints);
 
         lblAmtgericht.setText("keine Angabe");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         jPanel1.add(lblAmtgericht, gridBagConstraints);
 
         lblGrundbuchbezirk.setText("keine Angabe");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(lblGrundbuchbezirk, gridBagConstraints);
 
         lblKatasteramt.setText("keine Angabe");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(lblKatasteramt, gridBagConstraints);
 
         jPanel5.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridheight = 6;
         gridBagConstraints.weighty = 1.0;
         jPanel1.add(jPanel5, gridBagConstraints);
 
         jPanel6.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.weightx = 1.0;
         jPanel1.add(jPanel6, gridBagConstraints);
 
         lblDescBlattart.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescBlattart.setText("Blattart:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel1.add(lblDescBlattart, gridBagConstraints);
 
         lblBlattart.setText("keine Angabe");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(lblBlattart, gridBagConstraints);
 
         lblDescBuchungsart.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescBuchungsart.setText("Buchungsart:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel1.add(lblDescBuchungsart, gridBagConstraints);
 
         lblBuchungsart.setText("keine Angabe");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(lblBuchungsart, gridBagConstraints);
 
         panContent.add(jPanel1, java.awt.BorderLayout.CENTER);
 
         srpHeadContent.setBackground(Color.DARK_GRAY);
         srpHeadContent.setBackground(java.awt.Color.darkGray);
         srpHeadContent.setLayout(new java.awt.GridBagLayout());
 
         lblHeadMainInfo.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadMainInfo.setText("Buchungsblatt");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeadContent.add(lblHeadMainInfo, gridBagConstraints);
 
         panContent.add(srpHeadContent, java.awt.BorderLayout.NORTH);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panInfo.add(panContent, gridBagConstraints);
 
         panEigentuemer.setLayout(new java.awt.BorderLayout());
 
         jPanel3.setOpaque(false);
         jPanel3.setLayout(new java.awt.BorderLayout());
 
         scpOwner.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         scpOwner.setMaximumSize(new java.awt.Dimension(200, 135));
         scpOwner.setMinimumSize(new java.awt.Dimension(200, 135));
         scpOwner.setOpaque(false);
         scpOwner.setPreferredSize(new java.awt.Dimension(200, 135));
 
         epOwner.setBorder(null);
         epOwner.setContentType("text/html");
         epOwner.setEditable(false);
         epOwner.setOpaque(false);
         scpOwner.setViewportView(epOwner);
 
         jPanel3.add(scpOwner, java.awt.BorderLayout.CENTER);
 
         panEigentuemer.add(jPanel3, java.awt.BorderLayout.CENTER);
 
         srpHeadEigentuemer.setBackground(Color.DARK_GRAY);
         srpHeadEigentuemer.setBackground(java.awt.Color.darkGray);
         srpHeadEigentuemer.setLayout(new java.awt.GridBagLayout());
 
         lblHeadEigentuemer.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadEigentuemer.setText("Eigentümer");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeadEigentuemer.add(lblHeadEigentuemer, gridBagConstraints);
 
         panEigentuemer.add(srpHeadEigentuemer, java.awt.BorderLayout.NORTH);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panInfo.add(panEigentuemer, gridBagConstraints);
 
         panGrundstuecke.setLayout(new java.awt.BorderLayout());
 
         jPanel4.setOpaque(false);
         jPanel4.setLayout(new java.awt.BorderLayout());
 
         scpLandparcels.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         scpLandparcels.setOpaque(false);
 
         lstLandparcels.setOpaque(false);
         lstLandparcels.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lstLandparcelsMouseClicked(evt);
             }
         });
         lstLandparcels.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 lstLandparcelsValueChanged(evt);
             }
         });
         scpLandparcels.setViewportView(lstLandparcels);
 
         jPanel4.add(scpLandparcels, java.awt.BorderLayout.CENTER);
 
         panGrundstuecke.add(jPanel4, java.awt.BorderLayout.CENTER);
 
         srpHeadGrundstuecke.setBackground(Color.DARK_GRAY);
         srpHeadGrundstuecke.setBackground(java.awt.Color.darkGray);
         srpHeadGrundstuecke.setLayout(new java.awt.GridBagLayout());
 
         lblHeadFlurstuecke.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadFlurstuecke.setText("Flurstücke");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeadGrundstuecke.add(lblHeadFlurstuecke, gridBagConstraints);
 
         panGrundstuecke.add(srpHeadGrundstuecke, java.awt.BorderLayout.NORTH);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panInfo.add(panGrundstuecke, gridBagConstraints);
 
         panKarte.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         panKarte.setMaximumSize(new java.awt.Dimension(250, 450));
         panKarte.setMinimumSize(new java.awt.Dimension(250, 450));
         panKarte.setOpaque(false);
         panKarte.setPreferredSize(new java.awt.Dimension(250, 450));
         panKarte.setLayout(new java.awt.BorderLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridheight = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panInfo.add(panKarte, gridBagConstraints);
 
         add(panInfo, "CARD_1");
 
         panProducts.setOpaque(false);
         panProducts.setLayout(new java.awt.GridBagLayout());
 
         panProduktePDF.setOpaque(false);
         panProduktePDF.setLayout(new java.awt.GridBagLayout());
 
         hlBestandsnachweisNrwPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlBestandsnachweisNrwPdf.setText("Bestandsnachweis (NRW)");
         hlBestandsnachweisNrwPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisNrwPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProduktePDF.add(hlBestandsnachweisNrwPdf, gridBagConstraints);
 
         jPanel7.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = 4;
         gridBagConstraints.weighty = 1.0;
         panProduktePDF.add(jPanel7, gridBagConstraints);
 
         jPanel8.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.weightx = 1.0;
         panProduktePDF.add(jPanel8, gridBagConstraints);
 
         srpHeadProdukte.setBackground(Color.DARK_GRAY);
         srpHeadProdukte.setBackground(java.awt.Color.darkGray);
         srpHeadProdukte.setLayout(new java.awt.GridBagLayout());
 
         lblHeadProdukte.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadProdukte.setText("PDF-Produkte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeadProdukte.add(lblHeadProdukte, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panProduktePDF.add(srpHeadProdukte, gridBagConstraints);
 
         hlBestandsnachweisKomPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlBestandsnachweisKomPdf.setText("Bestandsnachweis (kommunal)");
         hlBestandsnachweisKomPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisKomPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProduktePDF.add(hlBestandsnachweisKomPdf, gridBagConstraints);
 
         hlGrundstuecksnachweisNrwPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlGrundstuecksnachweisNrwPdf.setText("Grundstücksnachweis (NRW)");
         hlGrundstuecksnachweisNrwPdf.setEnabled(false);
         hlGrundstuecksnachweisNrwPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlGrundstuecksnachweisNrwPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProduktePDF.add(hlGrundstuecksnachweisNrwPdf, gridBagConstraints);
 
         hlBestandsnachweisKomInternPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlBestandsnachweisKomInternPdf.setText("Bestandsnachweis (kommunal, intern)");
         hlBestandsnachweisKomInternPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisKomInternPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProduktePDF.add(hlBestandsnachweisKomInternPdf, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 143;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 5);
         panProducts.add(panProduktePDF, gridBagConstraints);
 
         jPanel9.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         panProducts.add(jPanel9, gridBagConstraints);
 
         panProductPreview.setOpaque(false);
         panProductPreview.setLayout(new java.awt.BorderLayout());
 
         lblProductPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblProductPreview.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
         panProductPreview.add(lblProductPreview, java.awt.BorderLayout.CENTER);
 
         semiRoundedPanel3.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel3.setLayout(new java.awt.GridBagLayout());
 
         lblPreviewHead.setText("Vorschau");
         lblPreviewHead.setForeground(new java.awt.Color(255, 255, 255));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel3.add(lblPreviewHead, gridBagConstraints);
 
         panProductPreview.add(semiRoundedPanel3, java.awt.BorderLayout.NORTH);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridheight = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
         panProducts.add(panProductPreview, gridBagConstraints);
 
         panProdukteHTML.setOpaque(false);
         panProdukteHTML.setLayout(new java.awt.GridBagLayout());
 
         srpHeadProdukte.setBackground(Color.DARK_GRAY);
         srpHeadProdukte1.setBackground(java.awt.Color.darkGray);
         srpHeadProdukte1.setLayout(new java.awt.GridBagLayout());
 
         lblHeadProdukte1.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadProdukte1.setText("HTML-Produkte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeadProdukte1.add(lblHeadProdukte1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         panProdukteHTML.add(srpHeadProdukte1, gridBagConstraints);
 
         hlBestandsnachweisNrwHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlBestandsnachweisNrwHtml.setText("Bestandsnachweis (NRW)");
         hlBestandsnachweisNrwHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisNrwHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProdukteHTML.add(hlBestandsnachweisNrwHtml, gridBagConstraints);
 
         jPanel2.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = 4;
         panProdukteHTML.add(jPanel2, gridBagConstraints);
 
         jPanel10.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         panProdukteHTML.add(jPanel10, gridBagConstraints);
 
         hlBestandsnachweisKomHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlBestandsnachweisKomHtml.setText("Bestandsnachweis (kommunal)");
         hlBestandsnachweisKomHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisKomHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProdukteHTML.add(hlBestandsnachweisKomHtml, gridBagConstraints);
 
         hlGrundstuecksnachweisNrwHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlGrundstuecksnachweisNrwHtml.setText("Grundstücksnachweis (NRW)");
         hlGrundstuecksnachweisNrwHtml.setEnabled(false);
         hlGrundstuecksnachweisNrwHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlGrundstuecksnachweisNrwHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProdukteHTML.add(hlGrundstuecksnachweisNrwHtml, gridBagConstraints);
 
         hlBestandsnachweisKomInternHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlBestandsnachweisKomInternHtml.setText("Bestandsnachweis (kommunal, intern)");
         hlBestandsnachweisKomInternHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisKomInternHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProdukteHTML.add(hlBestandsnachweisKomInternHtml, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 5);
         panProducts.add(panProdukteHTML, gridBagConstraints);
 
         add(panProducts, "CARD_2");
     }// </editor-fold>//GEN-END:initComponents
 
     private void openProduct(String product, String actionTag) {
         if (ObjectRendererUtils.checkActionTag(actionTag)) {
             try {
                 String queryID = getCompleteBuchungsblattCode();
                 if (queryID.length() > 0) {
                     if (AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_PDF.equals(product) || AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_HTML.equals(product)) {
                         String anhang = getCompleteLaufendeNrCode();
                         if (anhang != null) {
                             queryID += anhang;
 
                         } else {
                             return;
                         }
                     }
                     queryID = AlkisUtils.escapeHtmlSpaces(queryID);
                     AlkisUtils.PRODUCTS.productEinzelNachweis(queryID, product);
 
                 }
             } catch (Exception ex) {
                 ObjectRendererUtils.showExceptionWindowToUser(
                         "Fehler beim Aufruf des Produkts: " + product,
                         ex,
                         AlkisBuchungsblattRenderer.this);
                 log.error(ex);
             }
         } else {
             showNoProductPermissionWarning();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisNrwHtmlActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisNrwHtmlActionPerformed
         openProduct(AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_NRW_HTML, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW);
     }//GEN-LAST:event_hlBestandsnachweisNrwHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisNrwPdfActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisNrwPdfActionPerformed
         openProduct(AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_NRW_PDF, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW);
     }//GEN-LAST:event_hlBestandsnachweisNrwPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstLandparcelsMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstLandparcelsMouseClicked
         if (evt.getClickCount() > 1) {
             try {
                 final Object selection = lstLandparcels.getSelectedValue();
                 if (selection instanceof LightweightLandParcel) {
                     final LightweightLandParcel lwParcel = (LightweightLandParcel) selection;
                     final MetaClass mc = ClassCacheMultiple.getMetaClass(SessionManager.getSession().getUser().getDomain(),
                             "ALKIS_LANDPARCEL");
                     continueInBackground = true;
                     ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(mc, lwParcel.getFullObjectID(), "");
                 }
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
     }//GEN-LAST:event_lstLandparcelsMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstLandparcelsValueChanged(final javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstLandparcelsValueChanged
         if (!evt.getValueIsAdjusting()) {
             try {
                 // release cache
 // realLandParcelMetaObjectsCache = null;
                 final Object[] selObjs = lstLandparcels.getSelectedValues();
                 final List<Geometry> allSelectedGeoms = TypeSafeCollections.newArrayList();
                 for (final Object obj : selObjs) {
                     if (obj instanceof LightweightLandParcel) {
                         final LightweightLandParcel lwlp = (LightweightLandParcel) obj;
                         if (lwlp.getGeometry() != null) {
                             allSelectedGeoms.add(lwlp.getGeometry());
                         }
 //                    final DefaultStyledFeature dsFeature = landParcelFeatureMap.get(lwlp);
 //                    if (dsFeature != null) {
 //                        if (lwlp.getColor().equals(dsFeature.getFillingPaint())) {
 //
 //                        }
 //                    }
                     }
                 }
                 final GeometryCollection geoCollection = new GeometryCollection(allSelectedGeoms.toArray(
                         new Geometry[allSelectedGeoms.size()]),
                         new GeometryFactory());
                 map.gotoBoundingBox(new BoundingBox(geoCollection), true, true, 500);
 //                map.gotoBoundingBoxWithoutHistory(new BoundingBox(geoCollection));
             } catch (Error t) {
                 log.fatal(t, t);
             }
         }
     }//GEN-LAST:event_lstLandparcelsValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblBackMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
     }//GEN-LAST:event_lblBackMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnBackActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, CARD_1);
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
     }//GEN-LAST:event_btnBackActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnForwardActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, CARD_2);
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
     }//GEN-LAST:event_btnForwardActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblForwMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
     }//GEN-LAST:event_lblForwMouseClicked
 
     private void hlBestandsnachweisKomPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisKomPdfActionPerformed
         openProduct(AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_PDF, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM);
     }//GEN-LAST:event_hlBestandsnachweisKomPdfActionPerformed
 
     private void hlGrundstuecksnachweisNrwPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlGrundstuecksnachweisNrwPdfActionPerformed
         openProduct(AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_PDF, PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW);
     }//GEN-LAST:event_hlGrundstuecksnachweisNrwPdfActionPerformed
 
     private void hlBestandsnachweisKomHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisKomHtmlActionPerformed
         openProduct(AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_HTML, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM);
     }//GEN-LAST:event_hlBestandsnachweisKomHtmlActionPerformed
 
     private void hlGrundstuecksnachweisNrwHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlGrundstuecksnachweisNrwHtmlActionPerformed
         openProduct(AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_HTML, PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW);
     }//GEN-LAST:event_hlGrundstuecksnachweisNrwHtmlActionPerformed
 
     private void hlBestandsnachweisKomInternPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisKomInternPdfActionPerformed
         openProduct(AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_INTERN_PDF, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN);
         }//GEN-LAST:event_hlBestandsnachweisKomInternPdfActionPerformed
 
     private void hlBestandsnachweisKomInternHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisKomInternHtmlActionPerformed
         openProduct(AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_INTERN_HTML, PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN);
     }//GEN-LAST:event_hlBestandsnachweisKomInternHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsblattCode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String fixBuchungslattCode(final String buchungsblattCode) {
         if (buchungsblattCode != null) {
             final StringBuffer buchungsblattCodeSB = new StringBuffer(buchungsblattCode);
             // Fix SICAD-API-strangeness...
             while (buchungsblattCodeSB.length() < 14) {
                 buchungsblattCodeSB.append(" ");
             }
             return buchungsblattCodeSB.toString();
         } else {
             return "";
         }
     }
 
     public static String fixLaufendeNrCode(final String laufendeNrCode) {
         if (laufendeNrCode != null) {
             final StringBuffer laufendeNrCodeSB = new StringBuffer(laufendeNrCode);
             // Fix SICAD-API-strangeness...
             while (laufendeNrCodeSB.length() < 4) {
                 laufendeNrCodeSB.insert(0, '0');
             }
             laufendeNrCodeSB.insert(0, ' ');
             return laufendeNrCodeSB.toString();
         } else {
             return "";
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String getCompleteBuchungsblattCode() {
         if (cidsBean != null) {
             final Object buchungsblattCodeObj = cidsBean.getProperty("buchungsblattcode");
             if (buchungsblattCodeObj != null) {
                 return fixBuchungslattCode(buchungsblattCodeObj.toString());
             }
         }
         return "";
     }
 
     private String getCompleteLaufendeNrCode() {
         if (buchungsblatt != null) {
             Buchungsstelle[] stellen = buchungsblatt.getBuchungsstellen();
             if (stellen != null && stellen.length > 0) {
                 if (stellen.length == 1) {
                     Buchungsstelle first = stellen[0];
                     if (first != null) {
                         return fixLaufendeNrCode(first.getSequentialNumber());
                     }
                 } else {
                     //mehr als eins deswegen nachfragen
                     final HashMap<String, Buchungsstelle> stellenLookup = new HashMap<String, Buchungsstelle>(stellen.length);
                     for (Buchungsstelle b : stellen) {
                         //gehe davon aus dass hier immer nur ein flurstueck drin sein kann #bugrisk
                         if (b.getLandParcel() != null && b.getLandParcel().length > 0) {
                             for (LandParcel lp : b.getLandParcel()) {
                                 String code = lp.getLandParcelCode();
                                 stellenLookup.put(code, b);
                             }
                         }
                     }
                     String[] flurstuecke = stellenLookup.keySet().toArray(new String[0]);
                     Arrays.sort(flurstuecke);
 
                     String s = (String) JOptionPane.showInputDialog(
                             this,
                             "Auf welches Flurstück soll sich der Grundstücksnachweis beziehen?",
                             "Flurstückauswahl",
                             JOptionPane.PLAIN_MESSAGE,
                             null,//icon
                             flurstuecke,
                             null);
                     if (s != null) {
                         Buchungsstelle b = stellenLookup.get(s);
                         return fixLaufendeNrCode(b.getSequentialNumber());
                     } else {
                         return null;
                     }
                 }
 
             }
 
         }
         return null;
     }
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public final Object getLandParcelList() {
         return landParcelList;
     }
 
     @Override
     public void setCidsBean(final CidsBean cb) {
         if (landparcelListBinding.isBound()) {
             landparcelListBinding.unbind();
         }
         if (cb != null) {
             cidsBean = cb;
             retrieveWorker = new RetrieveWorker(cidsBean);
             final Object buchungsblattLandparcelListObj = cidsBean.getProperty("landparcels");
             if (buchungsblattLandparcelListObj instanceof List) {
                 final List<CidsBean> buchungsblattLandparcelList = (List<CidsBean>) buchungsblattLandparcelListObj;
                 for (final CidsBean buchungsblattLandparcelBean : buchungsblattLandparcelList) {
                     landParcelList.add(new LightweightLandParcel(buchungsblattLandparcelBean));
                 }
             }
             final Runnable edtRunner = new Runnable() {
 
                 @Override
                 public void run() {
                     AlkisSOAPWorkerService.execute(retrieveWorker);
 //                    CismetThreadPool.execute(retrieveWorker);
                     landparcelListBinding.bind();
                     initMap();
                 }
             };
             if (EventQueue.isDispatchThread()) {
                 edtRunner.run();
             } else {
                 EventQueue.invokeLater(edtRunner);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   lpList  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private BoundingBox boundingBoxFromLandparcelList(final List<LightweightLandParcel> lpList) {
         final List<Geometry> allGeomList = TypeSafeCollections.newArrayList();
         for (final LightweightLandParcel parcel : lpList) {
             allGeomList.add(parcel.geometry);
         }
         final GeometryCollection geoCollection = new GeometryCollection(allGeomList.toArray(
                 new Geometry[allGeomList.size()]),
                 new GeometryFactory());
         return new BoundingBox(geoCollection);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         if (landParcelList.size() > 0) {
             try {
                 final ActiveLayerModel mappingModel = new ActiveLayerModel();
                 mappingModel.setSrs(AlkisConstants.COMMONS.SRS_GEOM);
                 // TODO: do we need an swsw for every class?
                 final BoundingBox box = boundingBoxFromLandparcelList(landParcelList);
                 mappingModel.addHome(new XBoundingBox(
                         box.getX1(),
                         box.getY1(),
                         box.getX2(),
                         box.getY2(),
                         AlkisConstants.COMMONS.SRS_GEOM,
                         true));
                 final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisConstants.COMMONS.MAP_CALL_STRING));
                 swms.setName("Buchungsblatt");
                 mappingModel.addLayer(swms);
                 map.setMappingModel(mappingModel);
                 for (final LightweightLandParcel lwLandparcel : landParcelList) {
                     final StyledFeature dsf = new DefaultStyledFeature();
                     dsf.setGeometry(lwLandparcel.getGeometry());
                     final Color lpColor = lwLandparcel.getColor();
                     final Color lpColorWithAlpha = new Color(lpColor.getRed(),
                             lpColor.getGreen(),
                             lpColor.getBlue(),
                             168);
                     dsf.setFillingPaint(lpColorWithAlpha);
                     map.getFeatureCollection().addFeature(dsf);
                 }
                 map.gotoInitialBoundingBox();
                 map.unlock();
                 final int duration = map.getAnimationDuration();
                 map.setAnimationDuration(0);
                 map.setInteractionMode(MappingComponent.ZOOM);
                 // finally when all configurations are done ...
                 map.addCustomInputListener("MUTE", new PBasicInputEventHandler() {
 
                     @Override
                     public void mouseClicked(final PInputEvent evt) {
                         try {
                             if (evt.getClickCount() > 1) {
 //                                if (realLandParcelMetaObjectsCache == null) {
 //                                    CismetThreadPool.execute(new GeomQueryWorker());
 //                                } else {
                                 switchToMapAndShowGeometries();
 //                                }
                             }
                         } catch (Exception ex) {
                             log.error(ex, ex);
                         }
                     }
                 });
                 map.setInteractionMode("MUTE");
                 map.setAnimationDuration(duration);
             } catch (Throwable t) {
                 log.fatal(t, t);
             }
         } else {
             panKarte.setVisible(false);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void switchToMapAndShowGeometries() {
         ObjectRendererUtils.switchToCismapMap();
         ObjectRendererUtils.addBeanGeomAsFeatureToCismapMap(cidsBean, false);
 //        ObjectRendererUtils.addBeanGeomsAsFeaturesToCismapMap(realLandParcelMetaObjectsCache, false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     private List<MetaObject> queryForRealLandParcels() throws ConnectionException {
         Object[] selectedParcels = lstLandparcels.getSelectedValues();
         if ((selectedParcels == null) || (selectedParcels.length == 0)) {
             selectedParcels = landParcelList.toArray(new Object[landParcelList.size()]);
         }
         if ((selectedParcels != null) && (selectedParcels.length > 0)) {
             final StringBuilder inBuilder = new StringBuilder("(");
             for (final Object cur : selectedParcels) {
                 if (cur instanceof LightweightLandParcel) {
                     if (inBuilder.length() > 1) {
                         inBuilder.append(",");
                     }
                     inBuilder.append(((LightweightLandParcel) cur).fullObjectID);
                 }
             }
             inBuilder.append(")");
             final User user = SessionManager.getSession().getUser();
             final MetaClass mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "alkis_landparcel");
             final String query = "select " + mc.getID() + "," + mc.getPrimaryKey() + " from " + mc.getTableName()
                     + " where id in " + inBuilder.toString();
             final MetaObject[] moArr = SessionManager.getProxy().getMetaObjectByQuery(user, query);
             return Arrays.asList(moArr);
         }
         return Collections.EMPTY_LIST;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  buchungsblatt  DOCUMENT ME!
      */
     private void displayBuchungsblattInfos(final Buchungsblatt buchungsblatt) {
         if (buchungsblatt != null) {
             final Offices offices = buchungsblatt.getOffices();
 //            buchungsblatt.getBuchungsstellen()[0].getLandParcel()[0].getAdministrativeDistricts().get;
             if (offices != null) {
                 lblAmtgericht.setText(surroundWithHTMLTags(
                         AlkisUtils.arrayToSeparatedString(offices.getDistrictCourtName(), "<br>")));
                 lblKatasteramt.setText(surroundWithHTMLTags(
                         AlkisUtils.arrayToSeparatedString(offices.getLandRegistryOfficeName(), "<br>")));
             }
             lblBlattart.setText(buchungsblatt.getBlattart());
             lblBuchungsart.setText(AlkisUtils.getBuchungsartFromBuchungsblatt(buchungsblatt));
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   in  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String surroundWithHTMLTags(final String in) {
         final StringBuilder result = new StringBuilder("<html>");
         result.append(in);
         result.append("</html>");
         return result.toString();
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public void setTitle(String title) {
         if (title == null) {
             title = "<Error>";
         } else {
             title = "Buchungsblatt " + title;
         }
         this.title = title;
         lblTitle.setText(this.title);
     }
 
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
     }
 
     @Override
     public JComponent getFooterComponent() {
         return panFooter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the buchungsblatt
      */
     public Object getBuchungsblatt() {
         return buchungsblatt;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  buchungsblatt  the buchungsblatt to set
      */
     public void setBuchungsblatt(final Buchungsblatt buchungsblatt) {
         this.buchungsblatt = buchungsblatt;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  waiting  DOCUMENT ME!
      */
     private void setWaiting(final boolean waiting) {
         blWait.setVisible(waiting);
         blWait.setBusy(waiting);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isWaiting() {
         return blWait.isBusy();
     }
 
     @Override
     public void dispose() {
         landparcelListBinding.unbind();
         if (!continueInBackground) {
             AlkisSOAPWorkerService.cancel(retrieveWorker);
             setWaiting(false);
         }
         map.dispose();
     }
 
 //    final class GeomQueryWorker extends SwingWorker<List<MetaObject>, Void> {
 //
 //        @Override
 //        protected List<MetaObject> doInBackground() throws Exception {
 //            //set dummy to avoid multiple worker calls
 //            realLandParcelMetaObjectsCache = Collections.EMPTY_LIST;
 //            return queryForRealLandParcels();
 //        }
 //
 //        @Override
 //        protected void done() {
 //            try {
 //                if (!isCancelled()) {
 //                    realLandParcelMetaObjectsCache = get();
 //                    switchToMapAndShowGeometries();
 //                }
 //            } catch (InterruptedException ex) {
 //                log.warn(ex, ex);
 //                realLandParcelMetaObjectsCache = null;
 //            } catch (Exception ex) {
 //                ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Abrufen der Geometrien", ex, Alkis_buchungsblattRenderer.this);
 //                log.error(ex, ex);
 //                realLandParcelMetaObjectsCache = null;
 //            }
 //        }
 //    }
     @Override
     public Border getTitleBorder() {
         return new EmptyBorder(10, 10, 10, 10);
     }
 
     @Override
     public Border getFooterBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     @Override
     public Border getCenterrBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     //~ Inner Classes ----------------------------------------------------------
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class RetrieveWorker extends SwingWorker<Buchungsblatt, Void> {
 
         //~ Instance fields ----------------------------------------------------
         private final CidsBean bean;
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new RetrieveWorker object.
          *
          * @param  bean  DOCUMENT ME!
          */
         public RetrieveWorker(final CidsBean bean) {
             this.bean = bean;
             setWaiting(true);
             epOwner.setText("Wird geladen...");
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         protected Buchungsblatt doInBackground() throws Exception {
             return infoService.getBuchungsblatt(soapProvider.getIdentityCard(),
                     soapProvider.getService(),
                     fixBuchungslattCode(String.valueOf(bean.getProperty("buchungsblattcode"))));
         }
 
         @Override
         protected void done() {
             try {
                 if (!isCancelled()) {
                     buchungsblatt = get();
                     if (buchungsblatt != null) {
                         displayBuchungsblattInfos(buchungsblatt);
                         final Owner[] owners = buchungsblatt.getOwners();
 //                    final StringBuilder ownerBuilder = new StringBuilder("<html>");
                         final StringBuilder ownerBuilder = new StringBuilder("<html>");
                         for (final Owner owner : owners) {
                             ownerBuilder.append(AlkisUtils.ownerToString(owner, ""));
                         }
                         ownerBuilder.append("</html>");
                         epOwner.setText(ownerBuilder.toString());
                         //enable products that depend on soap info
                         hlGrundstuecksnachweisNrwPdf.setEnabled(true);
                         hlGrundstuecksnachweisNrwHtml.setEnabled(true);
                     }
                 }
             } catch (InterruptedException ex) {
                 log.warn(ex, ex);
             } catch (Exception ex) {
                 ObjectRendererUtils.showExceptionWindowToUser(
                         "Fehler beim Retrieve",
                         ex,
                         AlkisBuchungsblattRenderer.this);
                 epOwner.setText("Fehler beim Laden!");
                 log.error(ex, ex);
             } finally {
                 setWaiting(false);
             }
         }
     }
 
     /**
      * /** * cancel worker if renderer is disposed.
      *
      * @version  $Revision$, $Date$
      */
     private static final class LightweightLandParcel {
 
         //~ Static fields/initializers -----------------------------------------
         private static int nextColor = 0;
         //~ Instance fields ----------------------------------------------------
         private final String landparcelCode;
         private final Color color;
         private final Geometry geometry;
         private final int fullObjectID;
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new LightweightLandParcel object.
          *
          * @param  buchungsBlattLandparcelBean  DOCUMENT ME!
          */
         public LightweightLandParcel(final CidsBean buchungsBlattLandparcelBean) {
             this.landparcelCode = String.valueOf(buchungsBlattLandparcelBean.getProperty("landparcelcode"));
             final Object geoObj = buchungsBlattLandparcelBean.getProperty("geometrie.geo_field");
             if (geoObj instanceof Geometry) {
                 this.geometry = (Geometry) geoObj;
             } else {
                 this.geometry = null;
             }
             final Object fullObjIDObj = buchungsBlattLandparcelBean.getProperty("fullobjectid");
             int tmpFullObjID = -1;
             if (fullObjIDObj != null) {
                 try {
                     tmpFullObjID = Integer.parseInt(fullObjIDObj.toString());
                 } catch (Exception ex) {
                     log.error(ex, ex);
                 }
             }
             this.fullObjectID = tmpFullObjID;
             nextColor = (nextColor + 1) % COLORS.length;
             this.color = COLORS[nextColor];
         }
 
         //~ Methods ------------------------------------------------------------
         /**
          * DOCUMENT ME!
          *
          * @return  the landparcelCode
          */
         public String getLandparcelCode() {
             return landparcelCode;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  the geometry
          */
         public Geometry getGeometry() {
             return geometry;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  the fullObjectID
          */
         public int getFullObjectID() {
             return fullObjectID;
         }
 
         @Override
         public String toString() {
             return String.valueOf(landparcelCode);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  the color
          */
         public Color getColor() {
             return color;
         }
     }
 
     /**
      * <editor-fold defaultstate="collapsed" desc="Listeners">.
      *
      * @version  $Revision$, $Date$
      */
     class ProductLabelMouseAdaper extends MouseAdapter {
 
         //~ Methods ------------------------------------------------------------
         @Override
         public void mouseEntered(final MouseEvent e) {
             final Object srcObj = e.getSource();
             final ImageIcon imageIcon = productPreviewImages.get(srcObj);
             if (imageIcon != null) {
                 lblProductPreview.setIcon(imageIcon);
             }
         }
 
         @Override
         public void mouseExited(final MouseEvent e) {
             lblProductPreview.setIcon(null);
         }
     }
 
 // </editor-fold>
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class FancyListCellRenderer extends DefaultListCellRenderer {
 
         //~ Static fields/initializers -----------------------------------------
         private static final int SPACING = 5;
         private static final int MARKER_WIDTH = 4;
         //~ Instance fields ----------------------------------------------------
         private boolean selected = false;
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new FancyListCellRenderer object.
          */
         public FancyListCellRenderer() {
             setOpaque(false);
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         public Component getListCellRendererComponent(final JList list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             selected = isSelected;
             if (value instanceof LightweightLandParcel) {
                 final LightweightLandParcel lwlp = (LightweightLandParcel) value;
                 setBackground(lwlp.getColor());
             }
 
             setBorder(BorderFactory.createEmptyBorder(1, (2 * SPACING) + MARKER_WIDTH, 1, 0));
             return comp;
         }
 
         @Override
         protected void paintComponent(final Graphics g) {
             final Graphics2D g2d = (Graphics2D) g;
 //            final Color col = g2d.getColor();
             final Paint backup = g2d.getPaint();
             if (selected) {
                 g2d.setColor(javax.swing.UIManager.getDefaults().getColor("List.selectionBackground"));
                 g2d.fillRect(0, 0, getWidth(), getHeight());
             }
             g2d.setColor(getBackground());
             g2d.fillRect(SPACING, 0, MARKER_WIDTH, getHeight());
             g2d.setPaint(backup);
             super.paintComponent(g);
         }
     }
 }
