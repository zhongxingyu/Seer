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
 import de.aedsicad.aaaweb.service.util.Offices;
 import de.aedsicad.aaaweb.service.util.Owner;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisCommons;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisSOAPWorkerService;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.SOAPAccessProvider;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.Crs;
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
 import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
 import edu.umd.cs.piccolo.event.PInputEvent;
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
 import org.jdesktop.swingbinding.JListBinding;
 import org.jdesktop.swingx.graphics.ReflectionRenderer;
 
 /**
  *
  * @author srichter
  */
 public class Alkis_buchungsblattRenderer extends javax.swing.JPanel implements CidsBeanRenderer, BorderProvider, TitleComponentProvider, FooterComponentProvider {
 
     private static final Color[] COLORS = new Color[]{
         new Color(41, 86, 178), new Color(101, 156, 239), new Color(125, 189, 0), new Color(220, 246, 0), new Color(255, 91, 0)
     };
     public static final List<Color> LANDPARCEL_COLORS = Collections.unmodifiableList(Arrays.asList(COLORS));
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alkis_buchungsblattRenderer.class);
     private static final String ICON_RES_PACKAGE = "/de/cismet/cids/custom/wunda_blau/res/";
     private static final String ALKIS_RES_PACKAGE = ICON_RES_PACKAGE + "alkis/";
     private static final String CARD_1 = "CARD_1";
     private static final String CARD_2 = "CARD_2";
 //    private ImageIcon FORWARD_PRESSED;
 //    private ImageIcon FORWARD_SELECTED;
 //    private ImageIcon BACKWARD_PRESSED;
 //    private ImageIcon BACKWARD_SELECTED;
     private ImageIcon BESTAND_PDF;
     private ImageIcon BESTAND_HTML;
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
 
     /** Creates new form Alkis_pointRenderer */
     public Alkis_buchungsblattRenderer() {
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
             log.error("Alkis_buchungsblattRenderer exspects CardLayout as major layout manager, but has " + getLayout() + "!");
         }
         scpOwner.getViewport().setOpaque(false);
         scpLandparcels.getViewport().setOpaque(false);
         panKarte.add(map, BorderLayout.CENTER);
         initEditorPanes();
         lstLandparcels.setCellRenderer(new FancyListCellRenderer());
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${landParcelList}");
         landparcelListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, eLProperty, lstLandparcels);
         landparcelListBinding.setSourceNullValue(null);
         landparcelListBinding.setSourceUnreadableValue(null);
         if (!AlkisCommons.validateUserHasAlkisProductAccess()) {
             //disable Product page if user does not have the right to see it.
             btnForward.setEnabled(false);
             lblForw.setEnabled(false);
         }
     }
 
     private void initIcons() {
         final ReflectionRenderer reflectionRenderer = new ReflectionRenderer(0.5f, 0.15f, false);
 //        BACKWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-sel.png"));
 //        BACKWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-left-pressed.png"));
 //
 //        FORWARD_SELECTED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-sel.png"));
 //        FORWARD_PRESSED = new ImageIcon(getClass().getResource(ICON_RES_PACKAGE + "arrow-right-pressed.png"));
         BufferedImage i1 = null, i2 = null;
         try {
             i1 = reflectionRenderer.appendReflection(ImageIO.read(getClass().getResource(ALKIS_RES_PACKAGE + "bestandsnachweispdf.png")));
             i2 = reflectionRenderer.appendReflection(ImageIO.read(getClass().getResource(ALKIS_RES_PACKAGE + "bestandsachweishtml.png")));
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         BESTAND_PDF = new ImageIcon(i1);
         BESTAND_HTML = new ImageIcon(i2);
     }
 
     private void initFooterElements() {
         ObjectRendererUtils.decorateJLabelAndButtonSynced(lblForw, btnForward, ObjectRendererUtils.FORWARD_SELECTED, ObjectRendererUtils.FORWARD_PRESSED);
         ObjectRendererUtils.decorateJLabelAndButtonSynced(lblBack, btnBack, ObjectRendererUtils.BACKWARD_SELECTED, ObjectRendererUtils.BACKWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblForw, btnForward, FORWARD_SELECTED, FORWARD_PRESSED);
 //        ObjectRendererUtils.decorateJLabelAndButtonSynced(lblBack, btnBack, BACKWARD_SELECTED, BACKWARD_PRESSED);
     }
 
     private void initProductPreview() {
         initProductPreviewImages();
         int maxX = 0, maxY = 0;
         for (ImageIcon ii : productPreviewImages.values()) {
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
 
     private void initProductPreviewImages() {
         productPreviewImages.put(hlBestandsnachweisPdf, BESTAND_PDF);
         productPreviewImages.put(hlBestandsnachweisHtml, BESTAND_HTML);
         final ProductLabelMouseAdaper productListener = new ProductLabelMouseAdaper();
         hlBestandsnachweisHtml.addMouseListener(productListener);
         hlBestandsnachweisPdf.addMouseListener(productListener);
     }
 
     private void initEditorPanes() {
         //Font and Layout
         final Font font = UIManager.getFont("Label.font");
         final String bodyRule = "body { font-family: " + font.getFamily() + "; "
                 + "font-size: " + font.getSize() + "pt; }";
         final String tableRule = "td { padding-right : 15px; }";
         final String tableHeadRule = "th { padding-right : 15px; }";
         final StyleSheet css = ((HTMLEditorKit) epOwner.getEditorKit()).getStyleSheet();
 
         css.addRule(bodyRule);
         css.addRule(tableRule);
         css.addRule(tableHeadRule);
 
         //Change scroll behaviour: avoid autoscrolls on setText(...)
         final Caret caret = epOwner.getCaret();
         if (caret instanceof DefaultCaret) {
             final DefaultCaret dCaret = (DefaultCaret) caret;
             dCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
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
         hlBestandsnachweisPdf = new org.jdesktop.swingx.JXHyperlink();
         jPanel7 = new javax.swing.JPanel();
         jPanel8 = new javax.swing.JPanel();
         srpHeadProdukte = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadProdukte = new javax.swing.JLabel();
         jPanel9 = new javax.swing.JPanel();
         panProductPreview = new RoundedPanel();
         lblProductPreview = new javax.swing.JLabel();
         semiRoundedPanel3 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblPreviewHead = new javax.swing.JLabel();
         panProdukteHTML = new RoundedPanel();
         srpHeadProdukte1 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadProdukte1 = new javax.swing.JLabel();
         hlBestandsnachweisHtml = new org.jdesktop.swingx.JXHyperlink();
         jPanel2 = new javax.swing.JPanel();
         jPanel10 = new javax.swing.JPanel();
 
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
         lblDescBuchungsart.setText("Buchungsart");
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
 
         hlBestandsnachweisPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         hlBestandsnachweisPdf.setText("Bestandsnachweis");
         hlBestandsnachweisPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisPdfActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProduktePDF.add(hlBestandsnachweisPdf, gridBagConstraints);
 
         jPanel7.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.weighty = 1.0;
         panProduktePDF.add(jPanel7, gridBagConstraints);
 
         jPanel8.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
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
 
         hlBestandsnachweisHtml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/text-html.png"))); // NOI18N
         hlBestandsnachweisHtml.setText("Bestandsnachweis");
         hlBestandsnachweisHtml.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hlBestandsnachweisHtmlActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panProdukteHTML.add(hlBestandsnachweisHtml, gridBagConstraints);
 
         jPanel2.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = 2;
         panProdukteHTML.add(jPanel2, gridBagConstraints);
 
         jPanel10.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         panProdukteHTML.add(jPanel10, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 5);
         panProducts.add(panProdukteHTML, gridBagConstraints);
 
         add(panProducts, "CARD_2");
     }// </editor-fold>//GEN-END:initComponents
 
     private void hlBestandsnachweisHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisHtmlActionPerformed
         try {
             String buchungsblattCode = getCompleteBuchungsblattCode();
             if (buchungsblattCode.length() > 0) {
                 buchungsblattCode = AlkisCommons.escapeHtmlSpaces(buchungsblattCode);
                 AlkisCommons.Products.productBestandsnachweisProduct(buchungsblattCode, AlkisCommons.ProductFormat.HTML);
             }
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Aufruf des Produkts", ex, Alkis_buchungsblattRenderer.this);
             log.error(ex);
         }
     }//GEN-LAST:event_hlBestandsnachweisHtmlActionPerformed
 
     private void hlBestandsnachweisPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hlBestandsnachweisPdfActionPerformed
         try {
             String buchungsblattCode = getCompleteBuchungsblattCode();
             if (buchungsblattCode.length() > 0) {
                 buchungsblattCode = AlkisCommons.escapeHtmlSpaces(buchungsblattCode);
                 AlkisCommons.Products.productBestandsnachweisProduct(buchungsblattCode, AlkisCommons.ProductFormat.PDF);
             }
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Aufruf des Produkts", ex, Alkis_buchungsblattRenderer.this);
             log.error(ex);
         }
     }//GEN-LAST:event_hlBestandsnachweisPdfActionPerformed
 
     private void lstLandparcelsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstLandparcelsMouseClicked
         if (evt.getClickCount() > 1) {
             try {
                 final Object selection = lstLandparcels.getSelectedValue();
                 if (selection instanceof LightweightLandParcel) {
                     final LightweightLandParcel lwParcel = (LightweightLandParcel) selection;
                     final MetaClass mc = ClassCacheMultiple.getMetaClass(SessionManager.getSession().getUser().getDomain(), "ALKIS_LANDPARCEL");
                     continueInBackground = true;
                     ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(mc, lwParcel.getFullObjectID(), "");
                 }
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
     }//GEN-LAST:event_lstLandparcelsMouseClicked
 
     private void lstLandparcelsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstLandparcelsValueChanged
         if (!evt.getValueIsAdjusting()) {
             try {
                 //release cache
 //                realLandParcelMetaObjectsCache = null;
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
                 final GeometryCollection geoCollection = new GeometryCollection(allSelectedGeoms.toArray(new Geometry[allSelectedGeoms.size()]), new GeometryFactory());
                 map.gotoBoundingBox(new BoundingBox(geoCollection), true, true, 500);
 //                map.gotoBoundingBoxWithoutHistory(new BoundingBox(geoCollection));
             } catch (Error t) {
                 log.fatal(t, t);
             }
         }
     }//GEN-LAST:event_lstLandparcelsValueChanged
 
     private void lblBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
 }//GEN-LAST:event_lblBackMouseClicked
 
     private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, CARD_1);
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
 }//GEN-LAST:event_btnBackActionPerformed
 
     private void btnForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, CARD_2);
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
 }//GEN-LAST:event_btnForwardActionPerformed
 
     private void lblForwMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
 }//GEN-LAST:event_lblForwMouseClicked
 
     public static String fixBuchungslattCode(String buchungsblattCode) {
         if (buchungsblattCode != null) {
             final StringBuffer buchungsblattCodeSB = new StringBuffer(buchungsblattCode);
             //Fix SICAD-API-strangeness...
             while (buchungsblattCodeSB.length() < 14) {
                 buchungsblattCodeSB.append(" ");
             }
             return buchungsblattCodeSB.toString();
         } else {
             return "";
         }
     }
 
     private String getCompleteBuchungsblattCode() {
         if (cidsBean != null) {
             final Object buchungsblattCodeObj = cidsBean.getProperty("buchungsblattcode");
             if (buchungsblattCodeObj != null) {
                 return fixBuchungslattCode(buchungsblattCodeObj.toString());
             }
         }
         return "";
     }
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     public final Object getLandParcelList() {
         return landParcelList;
     }
 
     @Override
     public void setCidsBean(CidsBean cb) {
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
 
     private BoundingBox boundingBoxFromLandparcelList(List<LightweightLandParcel> lpList) {
         final List<Geometry> allGeomList = TypeSafeCollections.newArrayList();
         for (final LightweightLandParcel parcel : lpList) {
             allGeomList.add(parcel.geometry);
         }
         final GeometryCollection geoCollection = new GeometryCollection(allGeomList.toArray(new Geometry[allGeomList.size()]), new GeometryFactory());
         return new BoundingBox(geoCollection);
     }
 
     private void initMap() {
         if (landParcelList.size() > 0) {
             try {
                 final ActiveLayerModel mappingModel = new ActiveLayerModel();
                 mappingModel.setSrs(AlkisCommons.SRS);
                 //TODO: do we need an swsw for every class?
                 final BoundingBox box = boundingBoxFromLandparcelList(landParcelList);
                 mappingModel.addHome(new XBoundingBox(box.getX1(), box.getY1(), box.getX2(), box.getY2(), AlkisCommons.SRS, true));
                 SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisCommons.MAP_CALL_STRING));
                 swms.setName("Buchungsblatt");
                 mappingModel.addLayer(swms);
                 map.setMappingModel(mappingModel);
                 for (final LightweightLandParcel lwLandparcel : landParcelList) {
                     final StyledFeature dsf = new DefaultStyledFeature();
                     dsf.setGeometry(lwLandparcel.getGeometry());
                     final Color lpColor = lwLandparcel.getColor();
                     final Color lpColorWithAlpha = new Color(lpColor.getRed(), lpColor.getGreen(), lpColor.getBlue(), 168);
                     dsf.setFillingPaint(lpColorWithAlpha);
                     map.getFeatureCollection().addFeature(dsf);
                 }
                 map.gotoInitialBoundingBox();
                 map.unlock();
                 final int duration = map.getAnimationDuration();
                 map.setAnimationDuration(0);
                 map.setInteractionMode(MappingComponent.ZOOM);
                 //finally when all configurations are done ...
                 map.addCustomInputListener("MUTE", new PBasicInputEventHandler() {
 
                     @Override
                     public void mouseClicked(PInputEvent evt) {
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
 
     private void switchToMapAndShowGeometries() {
         ObjectRendererUtils.switchToCismapMap();
         ObjectRendererUtils.addBeanGeomAsFeatureToCismapMap(cidsBean, false);
 //        ObjectRendererUtils.addBeanGeomsAsFeaturesToCismapMap(realLandParcelMetaObjectsCache, false);
     }
 
     private List<MetaObject> queryForRealLandParcels() throws ConnectionException {
         Object[] selectedParcels = lstLandparcels.getSelectedValues();
         if (selectedParcels == null || selectedParcels.length == 0) {
             selectedParcels = landParcelList.toArray(new Object[landParcelList.size()]);
         }
         if (selectedParcels != null && selectedParcels.length > 0) {
             StringBuilder inBuilder = new StringBuilder("(");
             for (Object cur : selectedParcels) {
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
             final String query = "select " + mc.getID() + "," + mc.getPrimaryKey() + " from " + mc.getTableName() + " where id in " + inBuilder.toString();
             final MetaObject[] moArr = SessionManager.getProxy().getMetaObjectByQuery(user, query);
             return Arrays.asList(moArr);
         }
         return Collections.EMPTY_LIST;
     }
 
     private void displayBuchungsblattInfos(Buchungsblatt buchungsblatt) {
         if (buchungsblatt != null) {
             final Offices offices = buchungsblatt.getOffices();
 //            buchungsblatt.getBuchungsstellen()[0].getLandParcel()[0].getAdministrativeDistricts().get;
             if (offices != null) {
                 lblAmtgericht.setText(surroundWithHTMLTags(AlkisCommons.arrayToSeparatedString(offices.getDistrictCourtName(), "<br>")));
                 lblKatasteramt.setText(surroundWithHTMLTags(AlkisCommons.arrayToSeparatedString(offices.getLandRegistryOfficeName(), "<br>")));
             }
             lblBlattart.setText(buchungsblatt.getBlattart());
             lblBuchungsart.setText(AlkisCommons.getBuchungsartFromBuchungsblatt(buchungsblatt));
         }
     }
 
     private String surroundWithHTMLTags(String in) {
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
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXBusyLabel blWait;
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnForward;
     private javax.swing.JEditorPane epOwner;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisHtml;
     private org.jdesktop.swingx.JXHyperlink hlBestandsnachweisPdf;
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
 
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
 
 
     }
 
     @Override
     public JComponent getFooterComponent() {
         return panFooter;
 
 
     }
 
     /**
      * @return the buchungsblatt
      */
     public Object getBuchungsblatt() {
         return buchungsblatt;
 
 
     }
 
     /**
      * @param buchungsblatt the buchungsblatt to set
      */
     public void setBuchungsblatt(Buchungsblatt buchungsblatt) {
         this.buchungsblatt = buchungsblatt;
 
 
     }
 
     private void setWaiting(boolean waiting) {
         blWait.setVisible(waiting);
         blWait.setBusy(waiting);
     }
 
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
 
     final class RetrieveWorker extends SwingWorker<Buchungsblatt, Void> {
 
         private final CidsBean bean;
 
         public RetrieveWorker(CidsBean bean) {
             this.bean = bean;
             setWaiting(true);
             epOwner.setText("Wird geladen...");
         }
 
         @Override
         protected Buchungsblatt doInBackground() throws Exception {
             return infoService.getBuchungsblatt(soapProvider.getIdentityCard(), soapProvider.getService(), fixBuchungslattCode(String.valueOf(bean.getProperty("buchungsblattcode"))));
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
                             ownerBuilder.append(AlkisCommons.ownerToString(owner, ""));
                         }
                         ownerBuilder.append("</html>");
                         epOwner.setText(ownerBuilder.toString());
                     }
                 }
             } catch (InterruptedException ex) {
                 log.warn(ex, ex);
             } catch (Exception ex) {
                 ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Retrieve", ex, Alkis_buchungsblattRenderer.this);
                 epOwner.setText("Fehler beim Laden!");
                 log.error(ex, ex);
             } finally {
                 setWaiting(false);
             }
         }
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
 
 //    /**
 //     * cancel worker if renderer is disposed.
 //     */
 //    @Override
 //    public void removeNotify() {
 //        super.removeNotify();
 //        if (!continueInBackground) {
 //            AlkisSOAPWorkerService.cancel(retrieveWorker);
 //            setWaiting(false);
 //        }
 //    }
     private static final class LightweightLandParcel {
 
         public LightweightLandParcel(CidsBean buchungsBlattLandparcelBean) {
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
         private final String landparcelCode;
         private final Color color;
         private final Geometry geometry;
         private final int fullObjectID;
         private static int nextColor = 0;
 
         /**
          * @return the landparcelCode
          */
         public String getLandparcelCode() {
             return landparcelCode;
         }
 
         /**
          * @return the geometry
          */
         public Geometry getGeometry() {
             return geometry;
         }
 
         /**
          * @return the fullObjectID
          */
         public int getFullObjectID() {
             return fullObjectID;
         }
 
         @Override
         public String toString() {
             return String.valueOf(landparcelCode);
         }
 
         /**
          * @return the color
          */
         public Color getColor() {
             return color;
         }
     }
 
 // <editor-fold defaultstate="collapsed" desc="Listeners">
     class ProductLabelMouseAdaper extends MouseAdapter {
 
         @Override
         public void mouseEntered(MouseEvent e) {
             final Object srcObj = e.getSource();
             final ImageIcon imageIcon = productPreviewImages.get(srcObj);
             if (imageIcon != null) {
                 lblProductPreview.setIcon(imageIcon);
             }
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
             lblProductPreview.setIcon(null);
         }
     }
 
 // </editor-fold>
     private static final class FancyListCellRenderer extends DefaultListCellRenderer {
 
         private static final int SPACING = 5;
         private static final int MARKER_WIDTH = 4;
         private boolean selected = false;
 
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             selected = isSelected;
             if (value instanceof LightweightLandParcel) {
                 final LightweightLandParcel lwlp = (LightweightLandParcel) value;
                 setBackground(lwlp.getColor());
             }
 
             setBorder(BorderFactory.createEmptyBorder(1, 2 * SPACING + MARKER_WIDTH, 1, 0));
             return comp;
         }
 
         public FancyListCellRenderer() {
             setOpaque(false);
         }
 
         @Override
         protected void paintComponent(Graphics g) {
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
