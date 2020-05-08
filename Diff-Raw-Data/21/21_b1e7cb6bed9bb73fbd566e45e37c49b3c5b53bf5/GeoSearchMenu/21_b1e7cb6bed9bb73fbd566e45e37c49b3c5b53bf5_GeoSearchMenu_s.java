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
 package de.cismet.cismap.navigatorplugin;
 
 import java.awt.Color;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.Action;
 import javax.swing.JMenu;
 import javax.swing.JSeparator;
 
 import de.cismet.cismap.commons.features.AbstractNewFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
 
 import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
 import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;
 
 import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
 import de.cismet.tools.gui.StayOpenCheckBoxMenuItem;
 
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.ELLIPSE;
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.LINESTRING;
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.POLYGON;
 import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.RECTANGLE;
 
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchBufferAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchCidsFeatureAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchEllipseAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchPolygonAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchPolylineAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchRectangleAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchRedoAction;
 import static de.cismet.cismap.navigatorplugin.GeoSearchButton.createSearchShowLastFeatureAction;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jruiz
  * @version  $Revision$, $Date$
  */
 public class GeoSearchMenu extends JMenu implements PropertyChangeListener {
 
     //~ Instance fields --------------------------------------------------------
 
     private final String interactionMode;
     private final MappingComponent mappingComponent;
     private final AbstractCreateSearchGeometryListener searchListener;
     private Action searchRectangleAction;
     private Action searchPolygonAction;
     private Action searchCidsFeatureAction;
     private Action searchEllipseAction;
     private Action searchPolylineAction;
     private Action searchRedoAction;
     private Action searchShowLastFeatureAction;
     private Action searchBufferAction;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPopupMenu.Separator jSeparator12;
     private javax.swing.JMenuItem mniSearchBuffer1;
     private javax.swing.JRadioButtonMenuItem mniSearchCidsFeature1;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline1;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle1;
     private javax.swing.JMenuItem mniSearchRedo1;
     private javax.swing.JMenuItem mniSearchShowLastFeature1;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form Test.
      *
      * @param  interactionMode   DOCUMENT ME!
      * @param  mappingComponent  DOCUMENT ME!
      */
     public GeoSearchMenu(final String interactionMode,
             final MappingComponent mappingComponent) {
         this.interactionMode = interactionMode;
         this.mappingComponent = mappingComponent;
         this.searchListener = (AbstractCreateSearchGeometryListener)mappingComponent.getInputListener(interactionMode);
         searchListener.addPropertyChangeListener(this);
 
         searchRectangleAction = createSearchRectangleAction(interactionMode, mappingComponent);
         searchPolygonAction = createSearchPolygonAction(interactionMode, mappingComponent);
         searchCidsFeatureAction = createSearchCidsFeatureAction(interactionMode, mappingComponent);
         searchEllipseAction = createSearchEllipseAction(interactionMode, mappingComponent);
         searchPolylineAction = createSearchPolylineAction(interactionMode, mappingComponent);
         searchRedoAction = createSearchRedoAction(interactionMode, mappingComponent);
         searchShowLastFeatureAction = createSearchShowLastFeatureAction(interactionMode, mappingComponent);
         searchBufferAction = createSearchBufferAction(interactionMode, mappingComponent);
 
         initComponents();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         mniSearchRectangle1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolygon1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchEllipse1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         mniSearchPolyline1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                     "ProgressBar.foreground"),
                 Color.WHITE);
         jSeparator12 = new javax.swing.JPopupMenu.Separator();
         mniSearchCidsFeature1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchShowLastFeature1 = new javax.swing.JMenuItem();
         mniSearchRedo1 = new javax.swing.JMenuItem();
         mniSearchBuffer1 = new javax.swing.JMenuItem();
 
         org.openide.awt.Mnemonics.setLocalizedText(
             this,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.text")); // NOI18N
 
         mniSearchRectangle1.setAction(searchRectangleAction);
         mniSearchRectangle1.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchRectangle1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchRectangle1.text")); // NOI18N
         mniSearchRectangle1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png")));  // NOI18N
         add(mniSearchRectangle1);
 
         mniSearchPolygon1.setAction(searchPolygonAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchPolygon1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchPolygon1.text")); // NOI18N
         mniSearchPolygon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png")));    // NOI18N
         add(mniSearchPolygon1);
 
         mniSearchEllipse1.setAction(searchEllipseAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchEllipse1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchEllipse1.text")); // NOI18N
         mniSearchEllipse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png")));    // NOI18N
         add(mniSearchEllipse1);
 
         mniSearchPolyline1.setAction(searchPolylineAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchPolyline1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchPolyline1.text")); // NOI18N
         mniSearchPolyline1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png")));   // NOI18N
         add(mniSearchPolyline1);
         add(jSeparator12);
 
         mniSearchCidsFeature1.setAction(searchCidsFeatureAction);
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchCidsFeature1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchCidsFeature1.text")); // NOI18N
         mniSearchCidsFeature1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png")));    // NOI18N
         add(mniSearchCidsFeature1);
 
         mniSearchShowLastFeature1.setAction(searchShowLastFeatureAction);
         mniSearchShowLastFeature1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.CTRL_MASK));
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchShowLastFeature1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchShowLastFeature1.text")); // NOI18N
         mniSearchShowLastFeature1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 GeoSearchMenu.class,
                 "GeoSearchMenu.mniSearchShowLastFeature1.toolTipText"));                                                // NOI18N
         add(mniSearchShowLastFeature1);
 
         mniSearchRedo1.setAction(searchRedoAction);
         mniSearchRedo1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.ALT_MASK
                         | java.awt.event.InputEvent.CTRL_MASK));
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchRedo1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchRedo1.text")); // NOI18N
         mniSearchRedo1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 GeoSearchMenu.class,
                 "GeoSearchMenu.mniSearchRedo1.toolTipText"));                                                // NOI18N
         add(mniSearchRedo1);
 
         mniSearchBuffer1.setAction(searchBufferAction);
         mniSearchBuffer1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png")));     // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             mniSearchBuffer1,
             org.openide.util.NbBundle.getMessage(GeoSearchMenu.class, "GeoSearchMenu.mniSearchBuffer1.text")); // NOI18N
         mniSearchBuffer1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 GeoSearchMenu.class,
                 "GeoSearchMenu.mniSearchBuffer1.toolTipText"));                                                // NOI18N
         add(mniSearchBuffer1);
     }                                                                                                          // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  metaSearch  DOCUMENT ME!
      */
     public void initSearchTopicMenues(final MetaSearch metaSearch) {
         if ((metaSearch.getSearchTopics() != null) && !metaSearch.getSearchTopics().isEmpty()) {
             add(new JSeparator());
             for (final SearchTopic searchTopic : metaSearch.getSearchTopics()) {
                 add(new StayOpenCheckBoxMenuItem(
                         (Action)searchTopic,
                         javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                         Color.WHITE));
 
                 searchTopic.addPropertyChangeListener((PropertyChangeListener)mappingComponent.getInputListener(
                         interactionMode));
             }
         }
     }
 
     @Override
     public void propertyChange(final PropertyChangeEvent evt) {
         if (evt.getSource().equals(searchListener)) {
             if (AbstractCreateSearchGeometryListener.PROPERTY_LAST_FEATURE.equals(evt.getPropertyName())) {
                 setLastFeature(searchListener.getLastSearchFeature());
             } else if (AbstractCreateSearchGeometryListener.PROPERTY_MODE.equals(evt.getPropertyName())) {
                 setModeSelection(searchListener.getMode());
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  mode  DOCUMENT ME!
      */
     private void setModeSelection(final String mode) {
         mniSearchRectangle1.setSelected(CreateSearchGeometryListener.RECTANGLE.equals(mode));
         mniSearchPolygon1.setSelected(CreateSearchGeometryListener.POLYGON.equals(mode));
         mniSearchEllipse1.setSelected(CreateSearchGeometryListener.ELLIPSE.equals(mode));
         mniSearchPolyline1.setSelected(CreateSearchGeometryListener.LINESTRING.equals(mode));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  lastFeature  DOCUMENT ME!
      */
     private void setLastFeature(final AbstractNewFeature lastFeature) {
         if (lastFeature == null) {
             mniSearchShowLastFeature1.setIcon(null);
             mniSearchShowLastFeature1.setEnabled(false);
             mniSearchRedo1.setIcon(null);
             mniSearchRedo1.setEnabled(false);
             mniSearchBuffer1.setEnabled(false);
         } else {
             switch (lastFeature.getGeometryType()) {
                 case ELLIPSE: {
                     mniSearchRedo1.setIcon(mniSearchEllipse1.getIcon());
                     break;
                 }
 
                 case LINESTRING: {
                     mniSearchRedo1.setIcon(mniSearchPolyline1.getIcon());
                     break;
                 }
 
                 case POLYGON: {
                     mniSearchRedo1.setIcon(mniSearchPolygon1.getIcon());
                     break;
                 }
 
                 case RECTANGLE: {
                     mniSearchRedo1.setIcon(mniSearchRectangle1.getIcon());
                     break;
                 }
             }
 
             mniSearchRedo1.setEnabled(true);
             mniSearchBuffer1.setEnabled(true);
             mniSearchShowLastFeature1.setIcon(mniSearchRedo1.getIcon());
             mniSearchShowLastFeature1.setEnabled(true);
         }
     }
 }
