 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.wunda_blau.search;
 
 import Sirius.navigator.actiontag.ActionTagProtected;
 import Sirius.navigator.search.CidsSearchExecutor;
 import Sirius.navigator.search.dynamic.SearchControlListener;
 import Sirius.navigator.search.dynamic.SearchControlPanel;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.search.CidsServerSearch;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.NbBundle;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.net.URL;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsMeasurementPointSearchStatement;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsMeasurementPointSearchStatement.GST;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsMeasurementPointSearchStatement.Pointtype;
 
 import de.cismet.cids.navigator.utils.CidsBeanDropTarget;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.tools.search.clientstuff.CidsWindowSearch;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultFeatureCollection;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.features.SearchFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchCreateSearchGeometryListener;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 import de.cismet.cismap.tools.gui.CidsBeanDropJPopupMenuButton;
 
 import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
 import de.cismet.tools.gui.JPopupMenuButton;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsWindowSearch.class)
 public class MeasurementPointWindowSearch extends javax.swing.JPanel implements CidsWindowSearch,
     ActionTagProtected,
     SearchControlListener,
     PropertyChangeListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(MeasurementPointWindowSearch.class);
    private static final String ACTION_TAG = "custom.alkis.windowsearch";
     private static final String ACTION_POINTTYPE_ALLE = "cmdAllePunkte";
     private static final String ACTION_POINTTYPE_ANSCHLUSS = "cmdAnschlussPunkte";
     private static final String ACTION_POINTTYPE_GRENZUNDGEBAEUDE = "cmdGrenzUndGebaeudePunkte";
     private static final String ACTION_POINTTYPE_GEBAEUDEUNDBAUWERK = "cmdGebaeudeUndBauwerksPunkte";
     private static final String ACTION_POINTTYPE_HOEHENFEST = "cmdHoehenfestPunkte";
 
     //~ Instance fields --------------------------------------------------------
 
     private final MetaClass metaClass;
     private final ImageIcon icon;
     private final MappingComponent mappingComponent;
     private SearchControlPanel pnlSearchCancel;
     private CidsBeanDropJPopupMenuButton btnGeoSearch;
     private ImageIcon icoPluginRectangle;
     private ImageIcon icoPluginPolygon;
     private ImageIcon icoPluginEllipse;
     private ImageIcon icoPluginPolyline;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup bgrFilterGST;
     private javax.swing.ButtonGroup bgrSearch;
     private javax.swing.JButton btnAllePunkte;
     private javax.swing.JButton btnAnschlusspunkte;
     private javax.swing.JButton btnGebaeudeUndBauwerkspunkte;
     private javax.swing.JButton btnGrenzUndGebaudepunkte;
     private javax.swing.JButton btnHoehenfestpunkte;
     private javax.swing.JCheckBox chkAufnahmepunkte;
     private javax.swing.JCheckBox chkBesondereBauwerkspunkte;
     private javax.swing.JCheckBox chkBesondereGebaeudepunkte;
     private javax.swing.JCheckBox chkBesondereTopographischePunkte;
     private javax.swing.JCheckBox chkGrenzpunkte;
     private javax.swing.JCheckBox chkNivellementPunkte;
     private javax.swing.JCheckBox chkSearchInCismap;
     private javax.swing.JCheckBox chkSonstigeVermessungspunkte;
     private javax.swing.Box.Filler gluFiller;
     private javax.swing.JLabel lblPointcode;
     private javax.swing.JLabel lblPointcodeWildcardPercent;
     private javax.swing.JLabel lblPointcodeWildcardUnderline;
     private javax.swing.JLabel lblPointcodeWildcards;
     private javax.swing.JMenuItem mniSearchBuffer;
     private javax.swing.JRadioButtonMenuItem mniSearchCidsFeature;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle;
     private javax.swing.JMenuItem mniSearchRedo;
     private javax.swing.JMenuItem mniSearchShowLastFeature;
     private javax.swing.JPanel pnlButtons;
     private javax.swing.JPanel pnlFilterGST;
     private javax.swing.JPanel pnlFilterPointcode;
     private javax.swing.JPanel pnlFilterPointtype;
     private javax.swing.JPanel pnlPointcodeWildcards;
     private javax.swing.JPanel pnlPointtypeButtons;
     private javax.swing.JPanel pnlPointtypeCheckboxes;
     private javax.swing.JPopupMenu popMenSearch;
     private javax.swing.JRadioButton rdoFilterGSTAll;
     private javax.swing.JRadioButton rdoFilterGSTLE10;
     private javax.swing.JRadioButton rdoFilterGSTLE2;
     private javax.swing.JRadioButton rdoFilterGSTLE3;
     private javax.swing.JRadioButton rdoFilterGSTLE6;
     private javax.swing.JSeparator sepSearchGeometries;
     private javax.swing.JTextField txtPointcode;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form AlkisPointWindowSearch.
      */
     public MeasurementPointWindowSearch() {
         mappingComponent = CismapBroker.getInstance().getMappingComponent();
         metaClass = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "ALKIS_POINT");
         final byte[] iconDataFromMetaclass = metaClass.getIconData();
 
         if (iconDataFromMetaclass.length > 0) {
             LOG.info("Using icon from metaclass.");
             icon = new ImageIcon(metaClass.getIconData());
         } else {
             LOG.warn("Metaclass icon is not set. Trying to load default icon.");
             final URL urlToIcon = getClass().getResource("/de/cismet/cids/custom/wunda_blau/search/search.png");
 
             if (urlToIcon != null) {
                 icon = new ImageIcon(urlToIcon);
             } else {
                 icon = new ImageIcon(new byte[] {});
             }
         }
 
         icoPluginRectangle = new ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png"));
         icoPluginPolygon = new ImageIcon(getClass().getResource("/images/pluginSearchPolygon.png"));
         icoPluginEllipse = new ImageIcon(getClass().getResource("/images/pluginSearchEllipse.png"));
         icoPluginPolyline = new ImageIcon(getClass().getResource("/images/pluginSearchPolyline.png"));
 
         initComponents();
 
         final MeasurementPointCreateSearchGeometryListener measurementPointCreateSearchGeometryListener =
             new MeasurementPointCreateSearchGeometryListener(mappingComponent, new MeasurementPointSearchTooltip(icon));
         measurementPointCreateSearchGeometryListener.addPropertyChangeListener(this);
 
         pnlSearchCancel = new SearchControlPanel(this);
         final Dimension max = pnlSearchCancel.getMaximumSize();
         final Dimension min = pnlSearchCancel.getMinimumSize();
         final Dimension pre = pnlSearchCancel.getPreferredSize();
         pnlSearchCancel.setMaximumSize(new java.awt.Dimension(
                 new Double(max.getWidth()).intValue(),
                 new Double(max.getHeight() + 6).intValue()));
         pnlSearchCancel.setMinimumSize(new java.awt.Dimension(
                 new Double(min.getWidth()).intValue(),
                 new Double(min.getHeight() + 6).intValue()));
         pnlSearchCancel.setPreferredSize(new java.awt.Dimension(
                 new Double(pre.getWidth() + 6).intValue(),
                 new Double(pre.getHeight() + 6).intValue()));
         pnlButtons.add(pnlSearchCancel);
 
         pnlButtons.add(Box.createHorizontalStrut(5));
 
         btnGeoSearch = new CidsBeanDropJPopupMenuButton(
                 MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY,
                 mappingComponent,
                 null);
         btnGeoSearch.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnGeoSearchActionPerformed(evt);
                 }
             });
         btnGeoSearch.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.btnGeoSearch.toolTipText"));
         ((JPopupMenuButton)btnGeoSearch).setPopupMenu(popMenSearch);
         btnGeoSearch.setFocusPainted(false);
         pnlButtons.add(btnGeoSearch);
 
         visualizeSearchMode((MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                 MappingComponent.CREATE_SEARCH_POLYGON));
         mappingComponent.getInteractionButtonGroup().add(btnGeoSearch);
         new CidsBeanDropTarget(btnGeoSearch);
 
         ((CidsBeanDropJPopupMenuButton)btnGeoSearch).setTargetIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/images/pluginSearchTarget.png")));
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
 
         bgrFilterGST = new javax.swing.ButtonGroup();
         popMenSearch = new javax.swing.JPopupMenu();
         mniSearchRectangle = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolygon = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchEllipse = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolyline = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         sepSearchGeometries = new javax.swing.JSeparator();
         mniSearchCidsFeature = new javax.swing.JRadioButtonMenuItem();
         mniSearchShowLastFeature = new javax.swing.JMenuItem();
         mniSearchRedo = new javax.swing.JMenuItem();
         mniSearchBuffer = new javax.swing.JMenuItem();
         bgrSearch = new javax.swing.ButtonGroup();
         pnlFilterPointcode = new javax.swing.JPanel();
         lblPointcode = new javax.swing.JLabel();
         txtPointcode = new javax.swing.JTextField();
         pnlPointcodeWildcards = new javax.swing.JPanel();
         lblPointcodeWildcards = new javax.swing.JLabel();
         lblPointcodeWildcardPercent = new javax.swing.JLabel();
         lblPointcodeWildcardUnderline = new javax.swing.JLabel();
         pnlFilterPointtype = new javax.swing.JPanel();
         pnlPointtypeButtons = new javax.swing.JPanel();
         btnAllePunkte = new javax.swing.JButton();
         btnAnschlusspunkte = new javax.swing.JButton();
         btnGrenzUndGebaudepunkte = new javax.swing.JButton();
         btnGebaeudeUndBauwerkspunkte = new javax.swing.JButton();
         btnHoehenfestpunkte = new javax.swing.JButton();
         pnlPointtypeCheckboxes = new javax.swing.JPanel();
         chkAufnahmepunkte = new javax.swing.JCheckBox();
         chkSonstigeVermessungspunkte = new javax.swing.JCheckBox();
         chkGrenzpunkte = new javax.swing.JCheckBox();
         chkBesondereGebaeudepunkte = new javax.swing.JCheckBox();
         chkBesondereBauwerkspunkte = new javax.swing.JCheckBox();
         chkBesondereTopographischePunkte = new javax.swing.JCheckBox();
         chkNivellementPunkte = new javax.swing.JCheckBox();
         pnlFilterGST = new javax.swing.JPanel();
         rdoFilterGSTLE2 = new javax.swing.JRadioButton();
         rdoFilterGSTLE3 = new javax.swing.JRadioButton();
         rdoFilterGSTLE6 = new javax.swing.JRadioButton();
         rdoFilterGSTLE10 = new javax.swing.JRadioButton();
         rdoFilterGSTAll = new javax.swing.JRadioButton();
         chkSearchInCismap = new javax.swing.JCheckBox();
         pnlButtons = new javax.swing.JPanel();
         gluFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
 
         bgrSearch.add(mniSearchRectangle);
         mniSearchRectangle.setSelected(true);
         mniSearchRectangle.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchRectangle.text"));                                       // NOI18N
         mniSearchRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
         mniSearchRectangle.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchRectangleActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchRectangle);
 
         bgrSearch.add(mniSearchPolygon);
         mniSearchPolygon.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchPolygon.text"));                                     // NOI18N
         mniSearchPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         mniSearchPolygon.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchPolygonActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchPolygon);
 
         bgrSearch.add(mniSearchEllipse);
         mniSearchEllipse.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchEllipse.text"));                                     // NOI18N
         mniSearchEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
         mniSearchEllipse.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchEllipseActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchEllipse);
 
         bgrSearch.add(mniSearchPolyline);
         mniSearchPolyline.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchPolyline.text"));                                      // NOI18N
         mniSearchPolyline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
         mniSearchPolyline.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchPolylineActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchPolyline);
         popMenSearch.add(sepSearchGeometries);
 
         mniSearchCidsFeature.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchCidsFeature.text"));                                     // NOI18N
         mniSearchCidsFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         mniSearchCidsFeature.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchCidsFeatureActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchCidsFeature);
 
         mniSearchShowLastFeature.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchShowLastFeature.text"));        // NOI18N
         mniSearchShowLastFeature.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchShowLastFeature.toolTipText")); // NOI18N
         mniSearchShowLastFeature.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchShowLastFeatureActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchShowLastFeature);
 
         mniSearchRedo.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchRedo.text"));        // NOI18N
         mniSearchRedo.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchRedo.toolTipText")); // NOI18N
         mniSearchRedo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchRedoActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchRedo);
 
         mniSearchBuffer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
         mniSearchBuffer.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchBuffer.text"));                                    // NOI18N
         mniSearchBuffer.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.mniSearchBuffer.toolTipText"));                             // NOI18N
         mniSearchBuffer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSearchBufferActionPerformed(evt);
                 }
             });
         popMenSearch.add(mniSearchBuffer);
 
         setLayout(new java.awt.GridBagLayout());
 
         pnlFilterPointcode.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     MeasurementPointWindowSearch.class,
                     "MeasurementPointWindowSearch.pnlFilterPointcode.border.title"))); // NOI18N
         pnlFilterPointcode.setLayout(new java.awt.GridBagLayout());
 
         lblPointcode.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.lblPointcode.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterPointcode.add(lblPointcode, gridBagConstraints);
 
         txtPointcode.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.txtPointcode.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlFilterPointcode.add(txtPointcode, gridBagConstraints);
 
         pnlPointcodeWildcards.setLayout(new java.awt.GridBagLayout());
 
         lblPointcodeWildcards.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.lblPointcodeWildcards.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlPointcodeWildcards.add(lblPointcodeWildcards, gridBagConstraints);
 
         lblPointcodeWildcardPercent.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.lblPointcodeWildcardPercent.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         pnlPointcodeWildcards.add(lblPointcodeWildcardPercent, gridBagConstraints);
 
         lblPointcodeWildcardUnderline.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.lblPointcodeWildcardUnderline.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(3, 5, 5, 5);
         pnlPointcodeWildcards.add(lblPointcodeWildcardUnderline, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         pnlFilterPointcode.add(pnlPointcodeWildcards, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlFilterPointcode, gridBagConstraints);
 
         pnlFilterPointtype.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     MeasurementPointWindowSearch.class,
                     "MeasurementPointWindowSearch.pnlFilterPointtype.border.title"))); // NOI18N
         pnlFilterPointtype.setLayout(new java.awt.GridBagLayout());
 
         pnlPointtypeButtons.setLayout(new java.awt.GridBagLayout());
 
         btnAllePunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.btnAllePunkte.text")); // NOI18N
         btnAllePunkte.setActionCommand(ACTION_POINTTYPE_ALLE);
         btnAllePunkte.setFocusPainted(false);
         btnAllePunkte.setMaximumSize(new java.awt.Dimension(85, 25));
         btnAllePunkte.setMinimumSize(new java.awt.Dimension(85, 25));
         btnAllePunkte.setPreferredSize(new java.awt.Dimension(85, 25));
         btnAllePunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAllePunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 4, 5);
         pnlPointtypeButtons.add(btnAllePunkte, gridBagConstraints);
 
         btnAnschlusspunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.btnAnschlusspunkte.text")); // NOI18N
         btnAnschlusspunkte.setActionCommand(ACTION_POINTTYPE_ANSCHLUSS);
         btnAnschlusspunkte.setFocusPainted(false);
         btnAnschlusspunkte.setMaximumSize(new java.awt.Dimension(112, 25));
         btnAnschlusspunkte.setMinimumSize(new java.awt.Dimension(112, 25));
         btnAnschlusspunkte.setPreferredSize(new java.awt.Dimension(112, 25));
         btnAnschlusspunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAnschlusspunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(4, 5, 4, 5);
         pnlPointtypeButtons.add(btnAnschlusspunkte, gridBagConstraints);
 
         btnGrenzUndGebaudepunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.btnGrenzUndGebaudepunkte.text")); // NOI18N
         btnGrenzUndGebaudepunkte.setActionCommand(ACTION_POINTTYPE_GRENZUNDGEBAEUDE);
         btnGrenzUndGebaudepunkte.setFocusPainted(false);
         btnGrenzUndGebaudepunkte.setMaximumSize(new java.awt.Dimension(150, 25));
         btnGrenzUndGebaudepunkte.setMinimumSize(new java.awt.Dimension(150, 25));
         btnGrenzUndGebaudepunkte.setPreferredSize(new java.awt.Dimension(150, 25));
         btnGrenzUndGebaudepunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnGrenzUndGebaudepunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(4, 5, 4, 5);
         pnlPointtypeButtons.add(btnGrenzUndGebaudepunkte, gridBagConstraints);
 
         btnGebaeudeUndBauwerkspunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.btnGebaeudeUndBauwerkspunkte.text")); // NOI18N
         btnGebaeudeUndBauwerkspunkte.setActionCommand(ACTION_POINTTYPE_GEBAEUDEUNDBAUWERK);
         btnGebaeudeUndBauwerkspunkte.setFocusPainted(false);
         btnGebaeudeUndBauwerkspunkte.setMaximumSize(new java.awt.Dimension(168, 25));
         btnGebaeudeUndBauwerkspunkte.setMinimumSize(new java.awt.Dimension(168, 25));
         btnGebaeudeUndBauwerkspunkte.setPreferredSize(new java.awt.Dimension(168, 25));
         btnGebaeudeUndBauwerkspunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnGebaeudeUndBauwerkspunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(4, 5, 4, 5);
         pnlPointtypeButtons.add(btnGebaeudeUndBauwerkspunkte, gridBagConstraints);
 
         btnHoehenfestpunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.btnHoehenfestpunkte.text")); // NOI18N
         btnHoehenfestpunkte.setActionCommand(ACTION_POINTTYPE_HOEHENFEST);
         btnHoehenfestpunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnHoehenfestpunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(4, 5, 5, 5);
         pnlPointtypeButtons.add(btnHoehenfestpunkte, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         pnlFilterPointtype.add(pnlPointtypeButtons, gridBagConstraints);
 
         pnlPointtypeCheckboxes.setLayout(new java.awt.GridBagLayout());
 
         chkAufnahmepunkte.setSelected(true);
         chkAufnahmepunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkAufnahmepunkte.text")); // NOI18N
         chkAufnahmepunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkAufnahmepunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(1, 5, 2, 5);
         pnlPointtypeCheckboxes.add(chkAufnahmepunkte, gridBagConstraints);
 
         chkSonstigeVermessungspunkte.setSelected(true);
         chkSonstigeVermessungspunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkSonstigeVermessungspunkte.text")); // NOI18N
         chkSonstigeVermessungspunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkSonstigeVermessungspunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
         pnlPointtypeCheckboxes.add(chkSonstigeVermessungspunkte, gridBagConstraints);
 
         chkGrenzpunkte.setSelected(true);
         chkGrenzpunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkGrenzpunkte.text")); // NOI18N
         chkGrenzpunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkGrenzpunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
         pnlPointtypeCheckboxes.add(chkGrenzpunkte, gridBagConstraints);
 
         chkBesondereGebaeudepunkte.setSelected(true);
         chkBesondereGebaeudepunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkBesondereGebaeudepunkte.text")); // NOI18N
         chkBesondereGebaeudepunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkBesondereGebaeudepunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
         pnlPointtypeCheckboxes.add(chkBesondereGebaeudepunkte, gridBagConstraints);
 
         chkBesondereBauwerkspunkte.setSelected(true);
         chkBesondereBauwerkspunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkBesondereBauwerkspunkte.text")); // NOI18N
         chkBesondereBauwerkspunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkBesondereBauwerkspunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
         pnlPointtypeCheckboxes.add(chkBesondereBauwerkspunkte, gridBagConstraints);
 
         chkBesondereTopographischePunkte.setSelected(true);
         chkBesondereTopographischePunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkBesondereTopographischePunkte.text")); // NOI18N
         chkBesondereTopographischePunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkBesondereTopographischePunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
         pnlPointtypeCheckboxes.add(chkBesondereTopographischePunkte, gridBagConstraints);
 
         chkNivellementPunkte.setSelected(true);
         chkNivellementPunkte.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkNivellementPunkte.text")); // NOI18N
         chkNivellementPunkte.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkNivellementPunkteActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
         pnlPointtypeCheckboxes.add(chkNivellementPunkte, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         pnlFilterPointtype.add(pnlPointtypeCheckboxes, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlFilterPointtype, gridBagConstraints);
 
         pnlFilterGST.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     MeasurementPointWindowSearch.class,
                     "MeasurementPointWindowSearch.pnlFilterGST.border.title"))); // NOI18N
         pnlFilterGST.setLayout(new java.awt.GridBagLayout());
 
         bgrFilterGST.add(rdoFilterGSTLE2);
         rdoFilterGSTLE2.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.rdoFilterGSTLE2.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 2);
         pnlFilterGST.add(rdoFilterGSTLE2, gridBagConstraints);
 
         bgrFilterGST.add(rdoFilterGSTLE3);
         rdoFilterGSTLE3.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.rdoFilterGSTLE3.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 2);
         pnlFilterGST.add(rdoFilterGSTLE3, gridBagConstraints);
 
         bgrFilterGST.add(rdoFilterGSTLE6);
         rdoFilterGSTLE6.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.rdoFilterGSTLE6.text")); // NOI18N
         rdoFilterGSTLE6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 2);
         pnlFilterGST.add(rdoFilterGSTLE6, gridBagConstraints);
 
         bgrFilterGST.add(rdoFilterGSTLE10);
         rdoFilterGSTLE10.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.rdoFilterGSTLE10.text")); // NOI18N
         rdoFilterGSTLE10.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 2);
         pnlFilterGST.add(rdoFilterGSTLE10, gridBagConstraints);
 
         bgrFilterGST.add(rdoFilterGSTAll);
         rdoFilterGSTAll.setSelected(true);
         rdoFilterGSTAll.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.rdoFilterGSTAll.text")); // NOI18N
         rdoFilterGSTAll.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 5);
         pnlFilterGST.add(rdoFilterGSTAll, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlFilterGST, gridBagConstraints);
 
         chkSearchInCismap.setText(org.openide.util.NbBundle.getMessage(
                 MeasurementPointWindowSearch.class,
                 "MeasurementPointWindowSearch.chkSearchInCismap.text")); // NOI18N
         chkSearchInCismap.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(chkSearchInCismap, gridBagConstraints);
 
         pnlButtons.setLayout(new javax.swing.BoxLayout(pnlButtons, javax.swing.BoxLayout.LINE_AXIS));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(pnlButtons, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 0.1;
         add(gluFiller, gridBagConstraints);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAllePunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAllePunkteActionPerformed
         changeFilterPointtype(evt.getActionCommand());
     }                                                                                 //GEN-LAST:event_btnAllePunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAnschlusspunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAnschlusspunkteActionPerformed
         changeFilterPointtype(evt.getActionCommand());
     }                                                                                      //GEN-LAST:event_btnAnschlusspunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnGrenzUndGebaudepunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnGrenzUndGebaudepunkteActionPerformed
         changeFilterPointtype(evt.getActionCommand());
     }                                                                                            //GEN-LAST:event_btnGrenzUndGebaudepunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnGebaeudeUndBauwerkspunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnGebaeudeUndBauwerkspunkteActionPerformed
         changeFilterPointtype(evt.getActionCommand());
     }                                                                                                //GEN-LAST:event_btnGebaeudeUndBauwerkspunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkAufnahmepunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkAufnahmepunkteActionPerformed
         changeEnabledStateOfSearchButtons();
     }                                                                                     //GEN-LAST:event_chkAufnahmepunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkSonstigeVermessungspunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkSonstigeVermessungspunkteActionPerformed
         changeEnabledStateOfSearchButtons();
     }                                                                                                //GEN-LAST:event_chkSonstigeVermessungspunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkGrenzpunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkGrenzpunkteActionPerformed
         changeEnabledStateOfSearchButtons();
     }                                                                                  //GEN-LAST:event_chkGrenzpunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkBesondereGebaeudepunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkBesondereGebaeudepunkteActionPerformed
         changeEnabledStateOfSearchButtons();
     }                                                                                              //GEN-LAST:event_chkBesondereGebaeudepunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkBesondereBauwerkspunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkBesondereBauwerkspunkteActionPerformed
         changeEnabledStateOfSearchButtons();
     }                                                                                              //GEN-LAST:event_chkBesondereBauwerkspunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkBesondereTopographischePunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkBesondereTopographischePunkteActionPerformed
         changeEnabledStateOfSearchButtons();
     }                                                                                                    //GEN-LAST:event_chkBesondereTopographischePunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnGeoSearchActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchRectangleActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchRectangleActionPerformed
         btnGeoSearch.setIcon(icoPluginRectangle);
         btnGeoSearch.setSelectedIcon(icoPluginRectangle);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                             MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY))
                             .setMode(
                                 CreateGeometryListenerInterface.RECTANGLE);
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchRectangleActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchPolygonActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchPolygonActionPerformed
         btnGeoSearch.setIcon(icoPluginPolygon);
         btnGeoSearch.setSelectedIcon(icoPluginPolygon);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                             MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY))
                             .setMode(
                                 CreateGeometryListenerInterface.POLYGON);
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchPolygonActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchEllipseActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchEllipseActionPerformed
         btnGeoSearch.setIcon(icoPluginEllipse);
         btnGeoSearch.setSelectedIcon(icoPluginEllipse);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                             MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY))
                             .setMode(
                                 CreateGeometryListenerInterface.ELLIPSE);
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchEllipseActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchPolylineActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchPolylineActionPerformed
         btnGeoSearch.setIcon(icoPluginPolyline);
         btnGeoSearch.setSelectedIcon(icoPluginPolyline);
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                             MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY))
                             .setMode(
                                 CreateGeometryListenerInterface.LINESTRING);
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchPolylineActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchCidsFeatureActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchCidsFeatureActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                     final MeasurementPointCreateSearchGeometryListener searchListener =
                         ((MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                                 MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY));
 
                     de.cismet.tools.CismetThreadPool.execute(
                         new javax.swing.SwingWorker<SearchFeature, Void>() {
 
                             @Override
                             protected SearchFeature doInBackground() throws Exception {
                                 final DefaultMetaTreeNode[] nodes = ComponentRegistry.getRegistry()
                                                 .getActiveCatalogue()
                                                 .getSelectedNodesArray();
                                 final Collection<Geometry> searchGeoms = new ArrayList<Geometry>();
 
                                 for (final DefaultMetaTreeNode dmtn : nodes) {
                                     if (dmtn instanceof ObjectTreeNode) {
                                         final MetaObject mo = ((ObjectTreeNode)dmtn).getMetaObject();
                                         final CidsFeature cf = new CidsFeature(mo);
                                         searchGeoms.add(cf.getGeometry());
                                     }
                                 }
 
                                 final Geometry[] searchGeomsArr = searchGeoms.toArray(new Geometry[0]);
                                 final GeometryCollection coll =
                                     new GeometryFactory().createGeometryCollection(searchGeomsArr);
 
                                 final Geometry newG = coll.buffer(0.1d);
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("SearchGeom " + newG.toText());
                                 }
 
                                 final SearchFeature sf = new SearchFeature(newG);
                                 sf.setGeometryType(PureNewFeature.geomTypes.MULTIPOLYGON);
                                 return sf;
                             }
 
                             @Override
                             protected void done() {
                                 try {
                                     final SearchFeature search = get();
                                     if (search != null) {
                                         searchListener.search(search);
                                     }
                                 } catch (Exception e) {
                                     LOG.error("Exception in Background Thread", e);
                                 }
                             }
                         });
                 }
             });
     } //GEN-LAST:event_mniSearchCidsFeatureActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchShowLastFeatureActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchShowLastFeatureActionPerformed
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final MeasurementPointCreateSearchGeometryListener searchListener =
                         (MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                             MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                     searchListener.showLastFeature();
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchShowLastFeatureActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchRedoActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchRedoActionPerformed
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final MeasurementPointCreateSearchGeometryListener searchListener =
                         (MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                             MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                     searchListener.redoLastSearch();
                     mappingComponent.setInteractionMode(
                         MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                 }
             });
     } //GEN-LAST:event_mniSearchRedoActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSearchBufferActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniSearchBufferActionPerformed
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final String s = (String)JOptionPane.showInputDialog(
                             null,
                             "Geben Sie den Abstand des zu erzeugenden\n"       // NOI18N
                                     + "Puffers der letzten Suchgeometrie an.", // NOI18N
                             "Puffer",                                          // NOI18N
                             JOptionPane.PLAIN_MESSAGE,
                             null,
                             null,
                             "");                                               // NOI18N
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(s);
                     }
 
                     // , statt . ebenfalls erlauben
                     if (s.matches("\\d*,\\d*")) { // NOI18N
                         s.replace(",", ".");      // NOI18N
                     }
 
                     try {
                         final float buffer = Float.valueOf(s);
 
                         final MeasurementPointCreateSearchGeometryListener searchListener =
                             (MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                                 MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                         final PureNewFeature lastFeature = searchListener.getLastSearchFeature();
 
                         if (lastFeature != null) {
                             // Geometrie-Daten holen
                             final Geometry geom = lastFeature.getGeometry();
 
                             // Puffer-Geometrie holen
                             final Geometry bufferGeom = geom.buffer(buffer);
 
                             // und setzen
                             lastFeature.setGeometry(bufferGeom);
 
                             // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder
                             // �hnliches mehr)
                             lastFeature.setGeometryType(PureNewFeature.geomTypes.POLYGON);
 
                             for (final Object feature : mappingComponent.getFeatureCollection().getAllFeatures()) {
                                 final PFeature sel = (PFeature)mappingComponent.getPFeatureHM().get(feature);
 
                                 if (sel.getFeature().equals(lastFeature)) {
                                     // Koordinaten der Puffer-Geometrie als Feature-Koordinaten
                                     // setzen
                                     sel.setCoordArr(bufferGeom.getCoordinates());
 
                                     // refresh
                                     sel.syncGeometry();
 
                                     final Vector v = new Vector();
                                     v.add(sel.getFeature());
                                     ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                             .fireFeaturesChanged(v);
                                 }
                             }
 
                             searchListener.search(lastFeature);
                             mappingComponent.setInteractionMode(
                                 MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY);
                         }
                     } catch (NumberFormatException ex) {
                         JOptionPane.showMessageDialog(
                             null,
                             "The given value was not a floating point value.!",
                             "Error",
                             JOptionPane.ERROR_MESSAGE); // NOI18N
                     } catch (Exception ex) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("", ex);          // NOI18N
                         }
                     }
                 }
             });
     }                                                   //GEN-LAST:event_mniSearchBufferActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnHoehenfestpunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnHoehenfestpunkteActionPerformed
         changeFilterPointtype(evt.getActionCommand());
     }                                                                                       //GEN-LAST:event_btnHoehenfestpunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkNivellementPunkteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkNivellementPunkteActionPerformed
         changeEnabledStateOfSearchButtons();
     }                                                                                        //GEN-LAST:event_chkNivellementPunkteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  actionCommand  DOCUMENT ME!
      */
     protected void changeFilterPointtype(final String actionCommand) {
         if (ACTION_POINTTYPE_ALLE.equals(actionCommand)) {
             chkAufnahmepunkte.setSelected(true);
             chkSonstigeVermessungspunkte.setSelected(true);
             chkGrenzpunkte.setSelected(true);
             chkBesondereGebaeudepunkte.setSelected(true);
             chkBesondereBauwerkspunkte.setSelected(true);
             chkBesondereTopographischePunkte.setSelected(true);
             chkNivellementPunkte.setSelected(false);
         } else if (ACTION_POINTTYPE_ANSCHLUSS.equals(actionCommand)) {
             chkAufnahmepunkte.setSelected(true);
             chkSonstigeVermessungspunkte.setSelected(true);
             chkGrenzpunkte.setSelected(false);
             chkBesondereGebaeudepunkte.setSelected(false);
             chkBesondereBauwerkspunkte.setSelected(false);
             chkBesondereTopographischePunkte.setSelected(false);
             chkNivellementPunkte.setSelected(false);
         } else if (ACTION_POINTTYPE_GRENZUNDGEBAEUDE.equals(actionCommand)) {
             chkAufnahmepunkte.setSelected(false);
             chkSonstigeVermessungspunkte.setSelected(false);
             chkGrenzpunkte.setSelected(true);
             chkBesondereGebaeudepunkte.setSelected(true);
             chkBesondereBauwerkspunkte.setSelected(true);
             chkBesondereTopographischePunkte.setSelected(false);
             chkNivellementPunkte.setSelected(false);
         } else if (ACTION_POINTTYPE_GEBAEUDEUNDBAUWERK.equals(actionCommand)) {
             chkAufnahmepunkte.setSelected(false);
             chkSonstigeVermessungspunkte.setSelected(false);
             chkGrenzpunkte.setSelected(false);
             chkBesondereGebaeudepunkte.setSelected(true);
             chkBesondereBauwerkspunkte.setSelected(true);
             chkBesondereTopographischePunkte.setSelected(false);
             chkNivellementPunkte.setSelected(false);
         } else if (ACTION_POINTTYPE_HOEHENFEST.equals(actionCommand)) {
             chkAufnahmepunkte.setSelected(false);
             chkSonstigeVermessungspunkte.setSelected(false);
             chkGrenzpunkte.setSelected(false);
             chkBesondereGebaeudepunkte.setSelected(false);
             chkBesondereBauwerkspunkte.setSelected(false);
             chkBesondereTopographischePunkte.setSelected(false);
             chkNivellementPunkte.setSelected(true);
         }
 
         changeEnabledStateOfSearchButtons();
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void changeEnabledStateOfSearchButtons() {
         boolean enableSearchButtons = false;
 
         enableSearchButtons |= chkAufnahmepunkte.isSelected();
         enableSearchButtons |= chkSonstigeVermessungspunkte.isSelected();
         enableSearchButtons |= chkGrenzpunkte.isSelected();
         enableSearchButtons |= chkBesondereGebaeudepunkte.isSelected();
         enableSearchButtons |= chkBesondereBauwerkspunkte.isSelected();
         enableSearchButtons |= chkBesondereTopographischePunkte.isSelected();
         enableSearchButtons |= chkNivellementPunkte.isSelected();
 
         pnlSearchCancel.setEnabled(enableSearchButtons);
         btnGeoSearch.setEnabled(enableSearchButtons);
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void visualizeSearchMode() {
         visualizeSearchMode((MeasurementPointCreateSearchGeometryListener)mappingComponent.getInputListener(
                 MeasurementPointCreateSearchGeometryListener.MEASUREMENTPOINT_CREATE_SEARCH_GEOMETRY));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  searchListener  DOCUMENT ME!
      */
     protected void visualizeSearchMode(final CreateSearchGeometryListener searchListener) {
         final String searchMode = searchListener.getMode();
         final PureNewFeature lastGeometry = searchListener.getLastSearchFeature();
 
         if (CreateGeometryListenerInterface.RECTANGLE.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginRectangle);
             btnGeoSearch.setSelectedIcon(icoPluginRectangle);
         } else if (CreateGeometryListenerInterface.POLYGON.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginPolygon);
             btnGeoSearch.setSelectedIcon(icoPluginPolygon);
         } else if (CreateGeometryListenerInterface.ELLIPSE.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginEllipse);
             btnGeoSearch.setSelectedIcon(icoPluginEllipse);
         } else if (CreateGeometryListenerInterface.LINESTRING.equals(searchMode)) {
             btnGeoSearch.setIcon(icoPluginPolyline);
             btnGeoSearch.setSelectedIcon(icoPluginPolyline);
         }
 
         mniSearchRectangle.setSelected(CreateGeometryListenerInterface.RECTANGLE.equals(searchMode));
         mniSearchPolygon.setSelected(CreateGeometryListenerInterface.POLYGON.equals(searchMode));
         mniSearchEllipse.setSelected(CreateGeometryListenerInterface.ELLIPSE.equals(searchMode));
         mniSearchPolyline.setSelected(CreateGeometryListenerInterface.LINESTRING.equals(searchMode));
 
         if (lastGeometry == null) {
             mniSearchShowLastFeature.setIcon(null);
             mniSearchShowLastFeature.setEnabled(false);
             mniSearchRedo.setIcon(null);
             mniSearchRedo.setEnabled(false);
             mniSearchBuffer.setEnabled(false);
         } else {
             switch (lastGeometry.getGeometryType()) {
                 case ELLIPSE: {
                     mniSearchRedo.setIcon(mniSearchEllipse.getIcon());
                     break;
                 }
 
                 case LINESTRING: {
                     mniSearchRedo.setIcon(mniSearchPolyline.getIcon());
                     break;
                 }
 
                 case POLYGON: {
                     mniSearchRedo.setIcon(mniSearchPolygon.getIcon());
                     break;
                 }
 
                 case RECTANGLE: {
                     mniSearchRedo.setIcon(mniSearchRectangle.getIcon());
                     break;
                 }
             }
 
             mniSearchRedo.setEnabled(true);
             mniSearchShowLastFeature.setIcon(mniSearchRedo.getIcon());
             mniSearchShowLastFeature.setEnabled(true);
             mniSearchBuffer.setEnabled(true);
         }
     }
 
     @Override
     public void propertyChange(final PropertyChangeEvent evt) {
         if (AbstractCreateSearchGeometryListener.PROPERTY_FORGUI_LAST_FEATURE.equals(evt.getPropertyName())
                     || AbstractCreateSearchGeometryListener.PROPERTY_FORGUI_MODE.equals(evt.getPropertyName())) {
             visualizeSearchMode();
         }
 
         if (MeasurementPointCreateSearchGeometryListener.ACTION_SEARCH_STARTED.equals(evt.getPropertyName())) {
             if ((evt.getNewValue() != null) && (evt.getNewValue() instanceof Geometry)) {
                 final CidsServerSearch cidsServerSearch = getServerSearch((Geometry)evt.getNewValue());
                 CidsSearchExecutor.searchAndDisplayResultsWithDialog(cidsServerSearch);
             }
         }
     }
 
     @Override
     public JComponent getSearchWindowComponent() {
         return this;
     }
 
     @Override
     public CidsServerSearch getServerSearch() {
         return getServerSearch(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   geometry  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public CidsServerSearch getServerSearch(final Geometry geometry) {
         final Collection<Pointtype> pointtypes = new LinkedList<Pointtype>();
 
         if (chkAufnahmepunkte.isSelected()) {
             pointtypes.add(Pointtype.AUFNAHMEPUNKTE);
         }
         if (chkSonstigeVermessungspunkte.isSelected()) {
             pointtypes.add(Pointtype.SONSTIGE_VERMESSUNGSPUNKTE);
         }
         if (chkGrenzpunkte.isSelected()) {
             pointtypes.add(Pointtype.GRENZPUNKTE);
         }
         if (chkBesondereGebaeudepunkte.isSelected()) {
             pointtypes.add(Pointtype.BESONDERE_GEBAEUDEPUNKTE);
         }
         if (chkBesondereBauwerkspunkte.isSelected()) {
             pointtypes.add(Pointtype.BESONDERE_BAUWERKSPUNKTE);
         }
         if (chkBesondereTopographischePunkte.isSelected()) {
             pointtypes.add(Pointtype.BESONDERE_TOPOGRAPHISCHE_PUNKTE);
         }
         if (chkNivellementPunkte.isSelected()) {
             pointtypes.add(Pointtype.NIVELLEMENT_PUNKTE);
         }
 
         GST gst = null;
         if (rdoFilterGSTLE2.isSelected()) {
             gst = GST.LE2;
         } else if (rdoFilterGSTLE3.isSelected()) {
             gst = GST.LE3;
         } else if (rdoFilterGSTLE6.isSelected()) {
             gst = GST.LE6;
         } else if (rdoFilterGSTLE10.isSelected()) {
             gst = GST.LE10;
         }
 
         Geometry geometryToSearchFor = null;
         if (geometry != null) {
             geometryToSearchFor = geometry;
         } else {
             if (chkSearchInCismap.isSelected()) {
                 geometryToSearchFor =
                     ((XBoundingBox)CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBox())
                             .getGeometry();
             }
         }
 
         String geometryString = null;
         if (geometryToSearchFor != null) {
             final Geometry transformedBoundingBox = CrsTransformer.transformToDefaultCrs(geometryToSearchFor);
             transformedBoundingBox.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
             geometryString = PostGisGeometryFactory.getPostGisCompliantDbString(transformedBoundingBox);
         }
 
         return new CidsMeasurementPointSearchStatement(txtPointcode.getText(), pointtypes, gst, geometryString);
     }
 
     @Override
     public ImageIcon getIcon() {
         return icon;
     }
 
     @Override
     public String getName() {
         return NbBundle.getMessage(MeasurementPointWindowSearch.class, "MeasurementPointWindowSearch.name");
     }
 
     @Override
     public boolean checkActionTag() {
         return ObjectRendererUtils.checkActionTag(ACTION_TAG);
     }
 
     @Override
     public CidsServerSearch assembleSearch() {
         return getServerSearch();
     }
 
     @Override
     public void searchStarted() {
     }
 
     @Override
     public void searchDone(final int result) {
     }
 
     @Override
     public void searchCanceled() {
     }
 
     @Override
     public boolean suppressEmptyResultMessage() {
         return false;
     }
 }
