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
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
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
 
 import org.apache.commons.lang.ArrayUtils;
 
 import org.jdesktop.swingbinding.JListBinding;
 import org.jdesktop.swingx.graphics.ReflectionRenderer;
 
 import org.openide.util.Exceptions;
 
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
 
 import java.net.URL;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
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
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtils;
 import de.cismet.cids.custom.objectrenderer.utils.billing.BillingPopup;
 import de.cismet.cids.custom.objectrenderer.utils.billing.ProductGroupAmount;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.utils.alkis.AlkisSOAPWorkerService;
 import de.cismet.cids.custom.utils.alkis.SOAPAccessProvider;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.tools.BrowserLauncher;
 import de.cismet.tools.StaticDebuggingTools;
 
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.TitleComponentProvider;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.HttpDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class AlkisBuchungsblattRenderer extends javax.swing.JPanel implements CidsBeanRenderer,
     BorderProvider,
     TitleComponentProvider,
     FooterComponentProvider,
     RequestsFullSizeComponent {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Color[] COLORS = new Color[] {
             new Color(41, 86, 178),
             new Color(101, 156, 239),
             new Color(125, 189, 0),
             new Color(220, 246, 0),
             new Color(255, 91, 0)
         };
     public static final List<Color> LANDPARCEL_COLORS = Collections.unmodifiableList(Arrays.asList(COLORS));
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
             AlkisBuchungsblattRenderer.class);
     private static final String ICON_RES_PACKAGE = "/de/cismet/cids/custom/wunda_blau/res/";
     private static final String ALKIS_RES_PACKAGE = ICON_RES_PACKAGE + "alkis/";
     private static final String CARD_1 = "CARD_1";
     private static final String CARD_2 = "CARD_2";
     //
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW =
         "custom.alkis.product.bestandsnachweis_nrw@WUNDA_BLAU";
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM =
         "custom.alkis.product.bestandsnachweis_kom@WUNDA_BLAU";
     private static final String PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN =
         "custom.alkis.product.bestandsnachweis_kom_intern@WUNDA_BLAU";
     private static final String PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW =
         "custom.alkis.product.grundstuecksnachweis_nrw@WUNDA_BLAU";
     private static int nextColor = 0;
 
     //~ Instance fields --------------------------------------------------------
 
     private ImageIcon BESTAND_NRW_PDF;
     private ImageIcon BESTAND_NRW_HTML;
     private ImageIcon BESTAND_KOM_PDF;
     private ImageIcon BESTAND_KOM_HTML;
     private ImageIcon GRUND_NRW_PDF;
     private ImageIcon GRUND_NRW_HTML;
     //
     private final List<LightweightLandParcel3A> landParcel3AList;
     private final MappingComponent map;
     //
     private RetrieveWorker retrieveWorker;
     private SOAPAccessProvider soapProvider;
     private ALKISInfoServices infoService;
     private Buchungsblatt buchungsblatt;
     private CidsBean cidsBean;
     private String title;
     private final JListBinding landparcel3AListBinding;
     private final CardLayout cardLayout;
     private final Map<Object, ImageIcon> productPreviewImages;
     private boolean continueInBackground = false;
     private final boolean demoMode = StaticDebuggingTools.checkHomeForFile("demoMode");
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXBusyLabel blWait;
     private org.jdesktop.swingx.JXBusyLabel blWaitingLandparcel;
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
     private javax.swing.JLabel jLabel1;
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
     private javax.swing.JList lstBuchungsstellen;
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
     private javax.swing.JPanel pnlBusy;
     private javax.swing.JPanel pnlError;
     private javax.swing.JPanel pnlLandparcels;
     private javax.swing.JScrollPane scpBuchungsstellen;
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
         map.setOpaque(false);
         landParcel3AList = new ArrayList<LightweightLandParcel3A>();
         productPreviewImages = new HashMap<Object, ImageIcon>();
 
         if (!AlkisUtils.validateUserShouldUseAlkisSOAPServerActions()) {
             try {
                 soapProvider = new SOAPAccessProvider();
                 infoService = soapProvider.getAlkisInfoService();
             } catch (Exception ex) {
                 LOG.fatal(ex, ex);
             }
         }
         initIcons();
         initComponents();
         initFooterElements();
         initProductPreview();
         final LayoutManager layoutManager = getLayout();
         if (layoutManager instanceof CardLayout) {
             cardLayout = (CardLayout)layoutManager;
             cardLayout.show(this, CARD_1);
         } else {
             cardLayout = new CardLayout();
             LOG.error("Alkis_buchungsblattRenderer exspects CardLayout as major layout manager, but has " + getLayout()
                         + "!");
         }
         scpOwner.getViewport().setOpaque(false);
         scpBuchungsstellen.getViewport().setOpaque(false);
         panKarte.add(map, BorderLayout.CENTER);
         initEditorPanes();
 
         lstBuchungsstellen.setCellRenderer(new FancyListCellRenderer());
         final org.jdesktop.beansbinding.ELProperty eLProperty3A = org.jdesktop.beansbinding.ELProperty.create(
                 "${landParcel3AList}");
         landparcel3AListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ,
                 this,
                 eLProperty3A,
                 lstBuchungsstellen);
         landparcel3AListBinding.setSourceNullValue(null);
         landparcel3AListBinding.setSourceUnreadableValue(null);
 
         if (!AlkisUtils.validateUserHasAlkisProductAccess()) {
             // disable Product page if user does not have the right to see it.
             btnForward.setEnabled(false);
             lblForw.setEnabled(false);
         }
 
         panProdukteHTML.setVisible(AlkisUtils.validateUserHasAlkisHTMLProductAccess());
 
         hlBestandsnachweisKomPdf.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM) && BillingPopup.isBillingAllowed());
         hlBestandsnachweisKomHtml.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM));
         hlBestandsnachweisNrwPdf.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW) && BillingPopup.isBillingAllowed());
         hlBestandsnachweisNrwHtml.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW));
         hlBestandsnachweisKomInternPdf.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN));
         hlBestandsnachweisKomInternHtml.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN));
         hlGrundstuecksnachweisNrwPdf.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW) && BillingPopup.isBillingAllowed());
         hlGrundstuecksnachweisNrwHtml.setEnabled(ObjectRendererUtils.checkActionTag(
                 PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void initIcons() {
         final ReflectionRenderer reflectionRenderer = new ReflectionRenderer(0.5f, 0.15f, false);
 
         BufferedImage i1 = null;
         BufferedImage i2 = null;
         BufferedImage i3 = null;
         BufferedImage i4 = null;
         BufferedImage i5 = null;
         BufferedImage i6 = null;
         try {
             // TODO: Richtige Screenshots machen und zuordnen!
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
             LOG.error(ex, ex);
         }
         BESTAND_NRW_PDF = new ImageIcon(i1);
         BESTAND_NRW_HTML = new ImageIcon(i2);
         BESTAND_KOM_PDF = new ImageIcon(i3);
         BESTAND_KOM_HTML = new ImageIcon(i4);
         GRUND_NRW_PDF = new ImageIcon(i5);
         GRUND_NRW_HTML = new ImageIcon(i6);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void showNoProductPermissionWarning() {
         JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
             "Sie besitzen keine Berechtigung zur Erzeugung dieses Produkts!");
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
         final StyleSheet css = ((HTMLEditorKit)epOwner.getEditorKit()).getStyleSheet();
 
         css.addRule(bodyRule);
         css.addRule(tableRule);
         css.addRule(tableHeadRule);
 
         // Change scroll behaviour: avoid autoscrolls on setText(...)
         final Caret caret = epOwner.getCaret();
         if (caret instanceof DefaultCaret) {
             final DefaultCaret dCaret = (DefaultCaret)caret;
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
         pnlLandparcels = new javax.swing.JPanel();
         scpBuchungsstellen = new javax.swing.JScrollPane();
         lstBuchungsstellen = new javax.swing.JList();
         pnlError = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         pnlBusy = new javax.swing.JPanel();
         blWaitingLandparcel = new org.jdesktop.swingx.JXBusyLabel();
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
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
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
 
         lblBack.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         lblBack.setForeground(new java.awt.Color(255, 255, 255));
         lblBack.setText("Info");
         lblBack.setEnabled(false);
         lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lblBackMouseClicked(evt);
                 }
             });
         panFooterLeft.add(lblBack);
 
         btnBack.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left.png")));         // NOI18N
         btnBack.setBorder(null);
         btnBack.setBorderPainted(false);
         btnBack.setContentAreaFilled(false);
         btnBack.setEnabled(false);
         btnBack.setFocusPainted(false);
         btnBack.setMaximumSize(new java.awt.Dimension(30, 30));
         btnBack.setMinimumSize(new java.awt.Dimension(30, 30));
         btnBack.setPreferredSize(new java.awt.Dimension(30, 30));
         btnBack.setPressedIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-pressed.png"))); // NOI18N
         btnBack.setRolloverIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left-sel.png")));     // NOI18N
         btnBack.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
 
         btnForward.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right.png"))); // NOI18N
         btnForward.setBorder(null);
         btnForward.setBorderPainted(false);
         btnForward.setContentAreaFilled(false);
         btnForward.setFocusPainted(false);
         btnForward.setMaximumSize(new java.awt.Dimension(30, 30));
         btnForward.setMinimumSize(new java.awt.Dimension(30, 30));
         btnForward.setPreferredSize(new java.awt.Dimension(30, 30));
         btnForward.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnForwardActionPerformed(evt);
                 }
             });
         panFooterRight.add(btnForward);
 
         lblForw.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         lblForw.setForeground(new java.awt.Color(255, 255, 255));
         lblForw.setText("Produkte");
         lblForw.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
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
 
         lblDescKatasteramt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblDescKatasteramt.setText("Katasteramt:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel1.add(lblDescKatasteramt, gridBagConstraints);
 
         lblDescAmtsgericht.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblDescAmtsgericht.setText("Amtsgericht:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         jPanel1.add(lblDescAmtsgericht, gridBagConstraints);
 
         lblDescGrundbuchbezirk.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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
 
         lblDescBlattart.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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
 
         lblDescBuchungsart.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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
 
         epOwner.setEditable(false);
         epOwner.setBorder(null);
         epOwner.setContentType("text/html"); // NOI18N
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
         jPanel4.setLayout(new java.awt.GridBagLayout());
 
         pnlLandparcels.setOpaque(false);
         pnlLandparcels.setLayout(new java.awt.CardLayout());
 
         scpBuchungsstellen.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         scpBuchungsstellen.setOpaque(false);
 
         lstBuchungsstellen.setOpaque(false);
         lstBuchungsstellen.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lstBuchungsstellenMouseClicked(evt);
                 }
             });
         lstBuchungsstellen.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                     lstBuchungsstellenValueChanged(evt);
                 }
             });
         lstBuchungsstellen.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     lstBuchungsstellenFocusLost(evt);
                 }
             });
         scpBuchungsstellen.setViewportView(lstBuchungsstellen);
 
         pnlLandparcels.add(scpBuchungsstellen, "landparcels");
 
         pnlError.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         pnlError.setOpaque(false);
         pnlError.setLayout(new java.awt.BorderLayout());
 
         jLabel1.setText("Fehler beim Laden!");
         pnlError.add(jLabel1, java.awt.BorderLayout.PAGE_START);
 
         pnlLandparcels.add(pnlError, "error");
 
         pnlBusy.setOpaque(false);
         pnlBusy.setLayout(new java.awt.GridBagLayout());
         pnlBusy.add(blWaitingLandparcel, new java.awt.GridBagConstraints());
 
         pnlLandparcels.add(pnlBusy, "busy");
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         jPanel4.add(pnlLandparcels, gridBagConstraints);
 
         panGrundstuecke.add(jPanel4, java.awt.BorderLayout.CENTER);
 
         srpHeadGrundstuecke.setBackground(Color.DARK_GRAY);
         srpHeadGrundstuecke.setBackground(java.awt.Color.darkGray);
         srpHeadGrundstuecke.setLayout(new java.awt.GridBagLayout());
 
         lblHeadFlurstuecke.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadFlurstuecke.setText("Buchungsstellen und Flurstücke");
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
 
         hlBestandsnachweisNrwPdf.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlBestandsnachweisNrwPdf.setText("Bestandsnachweis (NRW)");
         hlBestandsnachweisNrwPdf.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
 
         hlBestandsnachweisKomPdf.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlBestandsnachweisKomPdf.setText("Bestandsnachweis (kommunal)");
         hlBestandsnachweisKomPdf.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     hlBestandsnachweisKomPdfActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProduktePDF.add(hlBestandsnachweisKomPdf, gridBagConstraints);
 
         hlGrundstuecksnachweisNrwPdf.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlGrundstuecksnachweisNrwPdf.setText("Grundstücksnachweis (NRW)");
         hlGrundstuecksnachweisNrwPdf.setEnabled(false);
         hlGrundstuecksnachweisNrwPdf.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     hlGrundstuecksnachweisNrwPdfActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProduktePDF.add(hlGrundstuecksnachweisNrwPdf, gridBagConstraints);
 
         hlBestandsnachweisKomInternPdf.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlBestandsnachweisKomInternPdf.setText("Bestandsnachweis (kommunal, intern)");
         hlBestandsnachweisKomInternPdf.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
 
         hlBestandsnachweisNrwHtml.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlBestandsnachweisNrwHtml.setText("Bestandsnachweis (NRW)");
         hlBestandsnachweisNrwHtml.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
 
         hlBestandsnachweisKomHtml.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlBestandsnachweisKomHtml.setText("Bestandsnachweis (kommunal)");
         hlBestandsnachweisKomHtml.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     hlBestandsnachweisKomHtmlActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProdukteHTML.add(hlBestandsnachweisKomHtml, gridBagConstraints);
 
         hlGrundstuecksnachweisNrwHtml.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlGrundstuecksnachweisNrwHtml.setText("Grundstücksnachweis (NRW)");
         hlGrundstuecksnachweisNrwHtml.setEnabled(false);
         hlGrundstuecksnachweisNrwHtml.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     hlGrundstuecksnachweisNrwHtmlActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProdukteHTML.add(hlGrundstuecksnachweisNrwHtml, gridBagConstraints);
 
         hlBestandsnachweisKomInternHtml.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlBestandsnachweisKomInternHtml.setText("Bestandsnachweis (kommunal, intern)");
         hlBestandsnachweisKomInternHtml.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  downloadTitle  DOCUMENT ME!
      * @param  product        DOCUMENT ME!
      * @param  actionTag      DOCUMENT ME!
      */
     private void downloadProduct(final String downloadTitle, final String product, final String actionTag) {
         if (!ObjectRendererUtils.checkActionTag(actionTag)) {
             showNoProductPermissionWarning();
             return;
         }
 
         String extension = ".pdf";
         if (AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_HTML.equals(product)
                     || AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_HTML.equals(product)
                     || AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_INTERN_HTML.equals(product)
                     || AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_NRW_HTML.equals(product)) {
             extension = ".html";
         }
 
         try {
             String buchungsblattCode_Anhang = getCompleteBuchungsblattCode();
             if (buchungsblattCode_Anhang.length() > 0) {
                 if (AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_PDF.equals(product)
                             || AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_HTML.equals(product)) {
                     final String anhang = getCompleteLaufendeNrCode();
                     if (anhang != null) {
                         buchungsblattCode_Anhang += anhang;
                     } else {
                         return;
                     }
                 }
                 final String queryID = AlkisUtils.escapeHtmlSpaces(buchungsblattCode_Anhang);
                 final URL url = AlkisUtils.PRODUCTS.productEinzelNachweisUrl(queryID, product);
 
                 if (url != null) {
                     if (!DownloadManagerDialog.showAskingForUserTitle(this)) {
                         return;
                     }
 
                     String filename = product + "." + buchungsblattCode_Anhang.replace("/", "--").trim();
                     filename = filename.replaceAll(" +", "_"); // replace all whitespaces
                     final HttpDownload download = new HttpDownload(
                             url,
                             "",
                             DownloadManagerDialog.getJobname(),
                             downloadTitle,
                             filename,
                             extension);
                     DownloadManager.instance().add(download);
                 }
             }
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser(
                 "Fehler beim Aufruf des Produkts: "
                         + product,
                 ex,
                 AlkisBuchungsblattRenderer.this);
             LOG.error(ex);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisNrwHtmlActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlBestandsnachweisNrwHtmlActionPerformed
         if (!demoMode) {
             downloadProduct(hlBestandsnachweisNrwHtml.getText(),
                 AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_NRW_HTML,
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW);
         } else {
             BrowserLauncher.openURLorFile(AlkisConstants.COMMONS.DEMOSERVICEURL + "bestandsnachweis_nrw.html");
         }
     }                                                                                             //GEN-LAST:event_hlBestandsnachweisNrwHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisNrwPdfActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlBestandsnachweisNrwPdfActionPerformed
         if (!demoMode) {
             try {
                 if (BillingPopup.doBilling("benw", "no.yet", (Geometry)null, new ProductGroupAmount("ea", 1))) {
                     downloadProduct(hlBestandsnachweisNrwPdf.getText(),
                         AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_NRW_PDF,
                         PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_NRW);
                 }
             } catch (Exception e) {
                 LOG.error("Error when trying to produce a alkis product", e);
                 // Hier noch ein Fehlerdialog
             }
         } else {
             BrowserLauncher.openURLorFile(AlkisConstants.COMMONS.DEMOSERVICEURL + "bestandsnachweis_nrw.pdf");
         }
     }                                                                                            //GEN-LAST:event_hlBestandsnachweisNrwPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblBackMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
     }                                                                       //GEN-LAST:event_lblBackMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnBackActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, CARD_1);
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
     }                                                                           //GEN-LAST:event_btnBackActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnForwardActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, CARD_2);
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
     }                                                                              //GEN-LAST:event_btnForwardActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblForwMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
     }                                                                       //GEN-LAST:event_lblForwMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisKomPdfActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlBestandsnachweisKomPdfActionPerformed
         if (!demoMode) {
             try {
                 if (BillingPopup.doBilling("bekom", "no.yet", (Geometry)null, new ProductGroupAmount("ea", 1))) {
                     downloadProduct(hlBestandsnachweisKomPdf.getText(),
                         AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_PDF,
                         PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM);
                 }
             } catch (Exception e) {
                 LOG.error("Error when trying to produce a alkis product", e);
                 // Hier noch ein Fehlerdialog
             }
         } else {
             BrowserLauncher.openURLorFile(AlkisConstants.COMMONS.DEMOSERVICEURL + "bestandsnachweis_kommunal.pdf");
         }
     }                                                                                            //GEN-LAST:event_hlBestandsnachweisKomPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlGrundstuecksnachweisNrwPdfActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlGrundstuecksnachweisNrwPdfActionPerformed
         try {
             if (BillingPopup.doBilling("grnw", "no.yet", (Geometry)null, new ProductGroupAmount("ea", 1))) {
                 downloadProduct(hlGrundstuecksnachweisNrwPdf.getText(),
                     AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_PDF,
                     PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW);
             }
         } catch (Exception e) {
             LOG.error("Error when trying to produce a alkis product", e);
             // Hier noch ein Fehlerdialog
         }
     }                                                                                                //GEN-LAST:event_hlGrundstuecksnachweisNrwPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisKomHtmlActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlBestandsnachweisKomHtmlActionPerformed
         if (!demoMode) {
             downloadProduct(hlBestandsnachweisKomHtml.getText(),
                 AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_HTML,
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM);
         } else {
             BrowserLauncher.openURLorFile(AlkisConstants.COMMONS.DEMOSERVICEURL + "bestandsnachweis_kommunal.html");
         }
     }                                                                                             //GEN-LAST:event_hlBestandsnachweisKomHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlGrundstuecksnachweisNrwHtmlActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlGrundstuecksnachweisNrwHtmlActionPerformed
         downloadProduct(hlGrundstuecksnachweisNrwHtml.getText(),
             AlkisUtils.PRODUCTS.GRUNDSTUECKSNACHWEIS_NRW_HTML,
             PRODUCT_ACTION_TAG_GRUNDSTUECKSNACHWEIS_NRW);
     }                                                                                                 //GEN-LAST:event_hlGrundstuecksnachweisNrwHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisKomInternPdfActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlBestandsnachweisKomInternPdfActionPerformed
         if (!demoMode) {
             downloadProduct(hlBestandsnachweisKomInternPdf.getText(),
                 AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_INTERN_PDF,
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN);
         } else {
             BrowserLauncher.openURLorFile(AlkisConstants.COMMONS.DEMOSERVICEURL
                         + "bestandsnachweis_kommunal_intern.pdf");
         }
     }                                                                                                  //GEN-LAST:event_hlBestandsnachweisKomInternPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hlBestandsnachweisKomInternHtmlActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hlBestandsnachweisKomInternHtmlActionPerformed
         if (!demoMode) {
             downloadProduct(hlBestandsnachweisKomInternHtml.getText(),
                 AlkisUtils.PRODUCTS.BESTANDSNACHWEIS_KOMMUNAL_INTERN_HTML,
                 PRODUCT_ACTION_TAG_BESTANDSNACHWEIS_KOM_INTERN);
         } else {
             BrowserLauncher.openURLorFile(AlkisConstants.COMMONS.DEMOSERVICEURL
                         + "bestandsnachweis_kommunal_intern.html");
         }
     }                                                                                                   //GEN-LAST:event_hlBestandsnachweisKomInternHtmlActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstBuchungsstellenMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lstBuchungsstellenMouseClicked
         if (evt.getClickCount() > 1) {
             try {
                 final Object selection = lstBuchungsstellen.getSelectedValue();
                 if (selection instanceof LightweightLandParcel3A) {
                     final LightweightLandParcel3A lwParcel = (LightweightLandParcel3A)selection;
                     final MetaClass mc = ClassCacheMultiple.getMetaClass(
                             CidsBeanSupport.DOMAIN_NAME,
                             "ALKIS_LANDPARCEL");
                     continueInBackground = true;
                     ComponentRegistry.getRegistry()
                             .getDescriptionPane()
                             .gotoMetaObject(mc, lwParcel.getFullObjectID(), "");
                 }
             } catch (Exception ex) {
                 LOG.error(ex, ex);
             }
         }
     }                                                                                  //GEN-LAST:event_lstBuchungsstellenMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstBuchungsstellenValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstBuchungsstellenValueChanged
         if (!evt.getValueIsAdjusting()) {
             try {
                 final Object[] selObjs = lstBuchungsstellen.getSelectedValues();
                 final List<Geometry> allSelectedGeoms = new ArrayList<Geometry>();
                 for (final Object obj : selObjs) {
                     if (obj instanceof LightweightLandParcel3A) {
                         final LightweightLandParcel3A lwlp = (LightweightLandParcel3A)obj;
                         if (lwlp.getGeometry() != null) {
                             allSelectedGeoms.add(lwlp.getGeometry());
                         }
                     }
                 }
 
                 if (allSelectedGeoms.size() > 0) {
                     final GeometryCollection geoCollection = new GeometryCollection(allSelectedGeoms.toArray(
                                 new Geometry[allSelectedGeoms.size()]),
                             new GeometryFactory());
                     final Geometry extendGeom = geoCollection.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER);
 
                     extendGeom.setSRID(allSelectedGeoms.get(0).getSRID());
                     final Geometry transformedGeom = CrsTransformer.transformToDefaultCrs(extendGeom);
                     transformedGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
 
                     final XBoundingBox boxToGoto = new XBoundingBox(transformedGeom);
                     boxToGoto.setX1(boxToGoto.getX1()
                                 - (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth()));
                     boxToGoto.setX2(boxToGoto.getX2()
                                 + (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth()));
                     boxToGoto.setY1(boxToGoto.getY1()
                                 - (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight()));
                     boxToGoto.setY2(boxToGoto.getY2()
                                 + (AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight()));
                     map.gotoBoundingBox(boxToGoto, true, true, 500);
                 }
             } catch (Error t) {
                 LOG.fatal(t, t);
             }
         }
     } //GEN-LAST:event_lstBuchungsstellenValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstBuchungsstellenFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_lstBuchungsstellenFocusLost
         map.gotoInitialBoundingBox();
         lstBuchungsstellen.clearSelection();
     }                                                                               //GEN-LAST:event_lstBuchungsstellenFocusLost
 
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
 
     /**
      * DOCUMENT ME!
      *
      * @param   laufendeNrCode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
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
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String getCompleteLaufendeNrCode() {
         if (buchungsblatt != null) {
             final Buchungsstelle[] stellen = buchungsblatt.getBuchungsstellen();
             if ((stellen != null) && (stellen.length > 0)) {
                 if (stellen.length == 1) {
                     final Buchungsstelle first = stellen[0];
                     if (first != null) {
                         return fixLaufendeNrCode(first.getSequentialNumber());
                     }
                 } else {
                     // mehr als eins deswegen nachfragen
                     final HashMap<String, Buchungsstelle> stellenLookup = new HashMap<String, Buchungsstelle>(
                             stellen.length);
                     for (final Buchungsstelle b : stellen) {
                         // gehe davon aus dass hier immer nur ein flurstueck drin sein kann #bugrisk
                         if ((b.getLandParcel() != null) && (b.getLandParcel().length > 0)) {
                             for (final LandParcel lp : b.getLandParcel()) {
                                 final String code = lp.getLandParcelCode();
                                 stellenLookup.put(code, b);
                             }
                         }
                     }
                     final String[] flurstuecke = stellenLookup.keySet().toArray(new String[0]);
                     Arrays.sort(flurstuecke);
 
                     final String s = (String)JOptionPane.showInputDialog(
                             StaticSwingTools.getParentFrame(this),
                             "Auf welches Flurstück soll sich der Grundstücksnachweis beziehen?",
                             "Flurstückauswahl",
                             JOptionPane.PLAIN_MESSAGE,
                             null, // icon
                             flurstuecke,
                             null);
                     if (s != null) {
                         final Buchungsstelle b = stellenLookup.get(s);
                         return fixLaufendeNrCode(b.getSequentialNumber());
                     } else {
                         return null;
                     }
                 }
             }
         }
         return null;
     }
 
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
      * @return  DOCUMENT ME!
      */
     public Object getLandParcel3AList() {
         return landParcel3AList;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cb  DOCUMENT ME!
      */
     @Override
     public void setCidsBean(final CidsBean cb) {
         if (landparcel3AListBinding.isBound()) {
             landparcel3AListBinding.unbind();
         }
         if (cb != null) {
             cidsBean = cb;
             retrieveWorker = new RetrieveWorker(cidsBean);
             final Runnable edtRunner = new Runnable() {
 
                     @Override
                     public void run() {
                         if (AlkisUtils.validateUserHasAlkisBuchungsblattAccess() && !demoMode) {
                             AlkisSOAPWorkerService.execute(retrieveWorker);
                         } else {
                             panEigentuemer.setVisible(false);
                             setWaiting(false);
                         }
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
     private XBoundingBox boundingBoxFromLandparcelList(final List<LightweightLandParcel3A> lpList) {
         final List<Geometry> allGeomList = new ArrayList<Geometry>();
         for (final LightweightLandParcel3A parcel : lpList) {
             if (parcel.geometry != null) {
                 allGeomList.add(parcel.geometry);
             }
         }
         final GeometryCollection geoCollection = new GeometryCollection(allGeomList.toArray(
                     new Geometry[allGeomList.size()]),
                 new GeometryFactory());
         final Geometry extentGeom = geoCollection.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER);
 
         if (!allGeomList.isEmpty()) {
             geoCollection.setSRID(allGeomList.get(0).getSRID());
         }
         final Geometry transformedGeom = CrsTransformer.transformToDefaultCrs(extentGeom);
         transformedGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
 
         return new XBoundingBox(transformedGeom);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         if (landParcel3AList.size() > 0) {
             try {
                 final ActiveLayerModel mappingModel = new ActiveLayerModel();
                 mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                 // TODO: do we need an swsw for every class?
                 final XBoundingBox box = boundingBoxFromLandparcelList(landParcel3AList);
                 mappingModel.addHome(new XBoundingBox(
                         box.getX1(),
                         box.getY1(),
                         box.getX2(),
                         box.getY2(),
                         AlkisConstants.COMMONS.SRS_SERVICE,
                         true));
                 final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisConstants.COMMONS.MAP_CALL_STRING));
                 swms.setName("Buchungsblatt");
                 mappingModel.addLayer(swms);
                 map.setMappingModel(mappingModel);
                 for (final LightweightLandParcel3A lwLandparcel : landParcel3AList) {
                     final StyledFeature dsf = new DefaultStyledFeature();
                     dsf.setGeometry(lwLandparcel.getGeometry());
                     final Color lpColor = lwLandparcel.getColor();
                     final Color lpColorWithAlpha = new Color(lpColor.getRed(),
                             lpColor.getGreen(),
                             lpColor.getBlue(),
                             192);
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
                                     switchToMapAndShowGeometries();
                                 }
                             } catch (Exception ex) {
                                 LOG.error(ex, ex);
                             }
                         }
                     });
                 map.setInteractionMode("MUTE");
                 map.setAnimationDuration(duration);
             } catch (Throwable t) {
                 LOG.fatal(t, t);
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
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  buchungsblatt  DOCUMENT ME!
      */
     private void displayBuchungsblattInfos(final Buchungsblatt buchungsblatt) {
         if (buchungsblatt != null) {
             final Offices offices = buchungsblatt.getOffices();
             final String bezirk = AlkisUtils.getBuchungsblattbezirkFromBuchungsblattnummer(
                     buchungsblatt.getBuchungsblattCode());
             if (bezirk != null) {
                 lblGrundbuchbezirk.setText(bezirk);
             }
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
     public void setTitle(String title) {
         if (title == null) {
             title = "<Error>";
         } else {
             title = "Buchungsblatt " + title;
         }
         this.title = title;
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
      *
      * @return  DOCUMENT ME!
      */
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
         blWaitingLandparcel.setBusy(waiting);
         if (waiting) {
             ((CardLayout)pnlLandparcels.getLayout()).show(pnlLandparcels, "busy");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isWaiting() {
         return blWait.isBusy();
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         landparcel3AListBinding.unbind();
         if (!continueInBackground) {
             AlkisSOAPWorkerService.cancel(retrieveWorker);
             setWaiting(false);
         }
         map.dispose();
     }
 
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
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static int getNextColor() {
         return nextColor;
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
 
             if (SwingUtilities.isEventDispatchThread()) {
                 setWaiting(true);
                 epOwner.setText("Wird geladen...");
             } else {
                 SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             setWaiting(true);
                             epOwner.setText("Wird geladen...");
                         }
                     });
             }
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         @Override
         protected Buchungsblatt doInBackground() throws Exception {
             Buchungsblatt buchungsblatt;
             if (infoService != null) {
                 buchungsblatt = infoService.getBuchungsblatt(soapProvider.getIdentityCard(),
                         soapProvider.getService(),
                         fixBuchungslattCode(String.valueOf(bean.getProperty("buchungsblattcode"))));
             } else {
                 buchungsblatt = AlkisUtils.getBuchungsblattFromAlkisSOAPServerAction(AlkisBuchungsblattRenderer
                                 .fixBuchungslattCode(String.valueOf(bean.getProperty("buchungsblattcode"))));
             }
             if (buchungsblatt != null) {
                 generateLightweightLandParcel3A(buchungsblatt);
             }
             return buchungsblatt;
         }
 
         /**
          * DOCUMENT ME!
          */
         @Override
         protected void done() {
             try {
                 if (!isCancelled()) {
                     buchungsblatt = get();
                     if (buchungsblatt != null) {
                         displayBuchungsblattInfos(buchungsblatt);
                         final Owner[] owners = buchungsblatt.getOwners();
 
                         final StringBuilder ownerBuilder = new StringBuilder("<html><table>");
                         for (final Owner owner : owners) {
                             ownerBuilder.append(AlkisUtils.ownerToString(owner, ""));
                         }
                         ownerBuilder.append("</table></html>");
                         epOwner.setText(ownerBuilder.toString());
                         // enable products that depend on soap info
                         hlGrundstuecksnachweisNrwPdf.setEnabled(true);
                         hlGrundstuecksnachweisNrwHtml.setEnabled(true);
 
                         landparcel3AListBinding.bind();
                         ((CardLayout)pnlLandparcels.getLayout()).show(pnlLandparcels, "landparcels");
                         initMap();
                     }
                 }
             } catch (InterruptedException ex) {
                 LOG.warn(ex, ex);
             } catch (Exception ex) {
                 ObjectRendererUtils.showExceptionWindowToUser(
                     "Fehler beim Retrieve",
                     ex,
                     AlkisBuchungsblattRenderer.this);
                 epOwner.setText("Fehler beim Laden!");
                 ((CardLayout)pnlLandparcels.getLayout()).show(pnlLandparcels, "error");
                 LOG.error(ex, ex);
             } finally {
                 setWaiting(false);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   buchungsblatt  DOCUMENT ME!
          *
          * @throws  ConnectionException  DOCUMENT ME!
          */
         private void generateLightweightLandParcel3A(final Buchungsblatt buchungsblatt) throws ConnectionException {
             final Buchungsstelle[] buchungsstellen = buchungsblatt.getBuchungsstellen();
             for (final Buchungsstelle buchungsstelle : buchungsstellen) {
                 final String buchungsart = buchungsstelle.getBuchungsart();
                 final String lfn = buchungsstelle.getSequentialNumber();
                 final String fraction = buchungsstelle.getFraction();
                 final String aufteilungsnummer = buchungsstelle.getNumber();
 
                 for (final LandParcel landparcel : AlkisUtils.getLandparcelFromBuchungsstelle(buchungsstelle)) {
                     final String landparcelCode = landparcel.getLandParcelCode().trim();
                     final LightweightLandParcel3A landparcel3A = new LightweightLandParcel3A(
                             landparcelCode,
                             buchungsart,
                             lfn,
                             fraction,
                             aufteilungsnummer);
                     landParcel3AList.add(landparcel3A);
                 }
             }
             Collections.sort(landParcel3AList, new Comparator<LightweightLandParcel3A>() {
 
                     @Override
                     public int compare(final LightweightLandParcel3A t, final LightweightLandParcel3A t1) {
                         return t.toString().compareTo(t1.toString());
                     }
                 });
             retrieveAlkisLandparcelForLightweightLandparcels3A();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws  ConnectionException  DOCUMENT ME!
          */
         private void retrieveAlkisLandparcelForLightweightLandparcels3A() throws ConnectionException {
             if ((landParcel3AList != null) && (landParcel3AList.size() > 0)) {
                 final StringBuilder inBuilder = new StringBuilder("(");
                 for (final LightweightLandParcel3A landParcel3A : landParcel3AList) {
                     if (inBuilder.length() > 1) {
                         inBuilder.append(",");
                     }
                     inBuilder.append("'");
                     inBuilder.append(landParcel3A.getLandparcelCode());
                     inBuilder.append("'");
                 }
                 inBuilder.append(")");
                 final User user = SessionManager.getSession().getUser();
                 final MetaClass mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "alkis_landparcel");
                 final String query = "select " + mc.getID() + "," + mc.getPrimaryKey() + " from " + mc.getTableName()
                             + " where alkis_id in " + inBuilder.toString();
                final MetaObject[] moArr = SessionManager.getProxy()
                            .getMetaObjectByQuery(user, query, CidsBeanSupport.DOMAIN_NAME);
 
                 for (final LightweightLandParcel3A landParcel3A : landParcel3AList) {
                     for (final MetaObject mo : moArr) {
                         final CidsBean bean = mo.getBean();
                         if (landParcel3A.getLandparcelCode().equals(bean.getProperty("alkis_id"))) {
                             landParcel3A.setFullObjectID((Integer)bean.getProperty("id"));
                             landParcel3A.setGeometry((Geometry)bean.getProperty("geometrie.geo_field"));
                             break;
                         }
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
     public final class LightweightLandParcel3A {
 
         //~ Instance fields ----------------------------------------------------
 
         private final String landparcelCode;
         private final Color color;
         private final String buchungsart;
         private final String lfn;
         private final String fraction;
         private final String aufteilungsnummer;
         private Geometry geometry;
         private int fullObjectID;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new LightweightLandParcel3A object.
          *
          * @param  landparcelCode     DOCUMENT ME!
          * @param  buchungsart        DOCUMENT ME!
          * @param  lfn                DOCUMENT ME!
          * @param  fraction           DOCUMENT ME!
          * @param  aufteilungsnummer  DOCUMENT ME!
          */
         public LightweightLandParcel3A(final String landparcelCode,
                 final String buchungsart,
                 final String lfn,
                 final String fraction,
                 final String aufteilungsnummer) {
             this.landparcelCode = landparcelCode;
             this.buchungsart = buchungsart;
             this.lfn = lfn;
             this.fraction = fraction;
             this.aufteilungsnummer = aufteilungsnummer;
             nextColor = (nextColor + 1) % COLORS.length;
             this.color = COLORS[nextColor];
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String toString() {
             final String basicString = String.valueOf(lfn) + "  " + String.valueOf(landparcelCode);
             if ((buchungsart == null)
                         || (buchungsart.equals(lblBuchungsart.getText()) && (fraction == null)
                             && (aufteilungsnummer == null))) {
                 return basicString;
             }
 
             final StringBuilder sb = new StringBuilder();
             sb.append(basicString);
             sb.append(" (");
             if ((buchungsart != null) && !buchungsart.equals(lblBuchungsart.getText())) {
                 sb.append(buchungsart);
                 sb.append(", ");
             }
             if (fraction != null) {
                 sb.append(AlkisUtils.prettyPrintFraction(fraction));
                 sb.append(", ");
             }
             if (aufteilungsnummer != null) {
                 sb.append(AlkisUtils.prettyPrintAufteilungsnummer(aufteilungsnummer));
                 sb.append(", ");
             }
             sb.replace(sb.length() - 2, sb.length(), "");
             sb.append(")");
 
             return sb.toString();
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
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public int getFullObjectID() {
             return fullObjectID;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  fullObjectID  DOCUMENT ME!
          */
         public void setFullObjectID(final int fullObjectID) {
             this.fullObjectID = fullObjectID;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getLandparcelCode() {
             return landparcelCode;
         }
 
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
          * @return  DOCUMENT ME!
          */
         public String getBuchungsart() {
             return buchungsart;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getLfn() {
             return lfn;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getFraction() {
             return fraction;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getAufteilungsnummer() {
             return aufteilungsnummer;
         }
     }
 
     /**
      * <editor-fold defaultstate="collapsed" desc="Listeners">.
      *
      * @version  $Revision$, $Date$
      */
     class ProductLabelMouseAdaper extends MouseAdapter {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void mouseEntered(final MouseEvent e) {
             final Object srcObj = e.getSource();
             final ImageIcon imageIcon = productPreviewImages.get(srcObj);
             if (imageIcon != null) {
                 lblProductPreview.setIcon(imageIcon);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
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
             final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             selected = isSelected;
             if (value instanceof LightweightLandParcel3A) {
                 final LightweightLandParcel3A lwlp = (LightweightLandParcel3A)value;
                 setBackground(lwlp.getColor());
             }
 
             setBorder(BorderFactory.createEmptyBorder(1, (2 * SPACING) + MARKER_WIDTH, 1, 0));
             return comp;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  g  DOCUMENT ME!
          */
         @Override
         protected void paintComponent(final Graphics g) {
             final Graphics2D g2d = (Graphics2D)g;
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
