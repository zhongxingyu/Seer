 /*
  * KartenPanel.java
  *
  * Created on 16. März 2007, 12:04
  */
 package de.cismet.commons.architecture.widget;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Polygon;
import de.cismet.cismap.commons.features.DefaultWFSFeature;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollectionEvent;
 import de.cismet.cismap.commons.features.FeatureCollectionListener;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.AttachFeatureListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.DeleteFeatureListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.FeatureMoveListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.JoinPolygonsListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.SplitPolygonListener;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.commons.architecture.broker.AdvancedPluginBroker;
 import de.cismet.commons.architecture.geometrySlot.GeometrySlotInformation;
 import de.cismet.commons.architecture.interfaces.NoPermissionsWidget;
 import de.cismet.tools.CurrentStackTrace;
 import de.cismet.tools.configuration.ConfigurationManager;
 import de.cismet.tools.configuration.NoWriteError;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.historybutton.JHistoryButton;
 import edu.umd.cs.piccolo.PNode;
 
 import edu.umd.cs.piccolox.event.PNotification;
 import edu.umd.cs.piccolox.event.PNotificationCenter;
 import edu.umd.cs.piccolox.event.PSelectionEventHandler;
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JToolBar;
 import org.jdom.Element;
 
 /**
  *
  * @author  Puhl
  */
 public class MapWidget extends DefaultWidget implements FeatureCollectionListener, NoPermissionsWidget {
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private MappingComponent mappingComponent;
     //private static final String WIDGET_NAME = "Karten Panel";
     private boolean isSnappingEnabled = false;
     private String gemarkungIdentifier = null;
     private String flurIdentifier = null;
     private String flurstueckZaehlerIdentifier = null;
     private String flurstueckNennerIdentifier = null;
     private ArrayList<JButton> customButtons = new ArrayList<JButton>();
 
     public void setToLastInteractionMode() {
         if (getLastMapMode() != null) {
             log.debug("Setting MapWidget to last interactionmode...");
             switch (getLastMapMode()) {
                 case SELECT:
                     log.debug("Select");
                     cmdSelectActionPerformed(null);
                     break;
                 case PAN:
                     log.debug("Pan");
                     cmdPanActionPerformed(null);
                     break;
                 case ZOOM:
                     log.debug("Zoom");
                     cmdZoomActionPerformed(null);
                     break;
                 case MOVE_POLYGON:
                     log.debug("Move polygon");
                     cmdMovePolygonActionPerformed(null);
                     break;
                 case REMOVE_POLYGON:
                     log.debug("Remove handle");
                     cmdRemovePolygonActionPerformed(null);
                     break;
                 case CUSTOM:
                     log.warn("Can't set to last interaction mode was custom");
                     break;
             }
         } else {
             log.debug("No last interaction mode. Setting mode to Select");
             cmdSelect.doClick();
         }
     }
 
     public enum MapMode {
 
         SELECT, PAN, ZOOM, MOVE_POLYGON, REMOVE_POLYGON, CUSTOM
     };
     private MapMode currentMapMode = null;
     private MapMode lastMapMode = null;
 
     public MapMode getCurrentMapMode() {
         return currentMapMode;
     }
 
     public void setCurrentMapMode(MapMode currentMapMode) {
         this.currentMapMode = currentMapMode;
     }
 
     public MapMode getLastMapMode() {
         return lastMapMode;
     }
 
     public void setLastMapMode(MapMode lastMapMode) {
         log.debug("setLastMapModeTo: " + lastMapMode + " was: " + getLastMapMode());
         this.lastMapMode = lastMapMode;
     }
 
     public JButton getCmdSelect() {
         return cmdSelect;
     }
     //private MappingComponent mapComponent;
     private ActiveLayerModel mappingModel = new ActiveLayerModel(); //{
 //
 //        @Override
 //        public void masterConfigure(Element e) {
 //            super.masterConfigure(e);
 //            super.configure(e);
 //        }
 //
 ////        @Override
 ////        public void configure(Element e) {
 ////        }
 //
 ////        @Override
 ////        public Element getConfiguration() throws NoWriteError {
 ////            Element conf = new Element("cismapActiveLayerConfiguration");
 ////            return conf;
 ////        }
 //    };
 
     public ActiveLayerModel getMappingModel() {
         return mappingModel;
     }
 
     /** Creates new form KartenPanel */
     public MapWidget(AdvancedPluginBroker broker) {
         super(broker);
         mappingComponent = new MappingComponent();
         log.fatal("Problem if lagis runs together with cismap or belis");
         CismapBroker.getInstance().setMappingComponent(mappingComponent);
         broker.setMappingComponent(mappingComponent);
     //ToDo set currentMapMode at startup
     //TODO make enumartion for InteractionModes      
     }
 
     public void addCustomButton(JButton customButton) {
         if (customButton != null) {
             customButton.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     removeMainGroupSelection();
                     setLastMapMode(getCurrentMapMode());
                     setCurrentMapMode(MapMode.CUSTOM);
                 }
             });
             customButtons.add(customButton);
             mapWidgetToolbar.add(customButton);
         }
     }
 
     public void setInteractionMode() {
         String currentInteractionMode = mappingComponent.getInteractionMode();
         log.debug("CurrentInteractionMode: " + currentInteractionMode, new CurrentStackTrace());
         if (currentInteractionMode != null) {
 
             if (currentInteractionMode.equals(MappingComponent.SELECT)) {
                 log.debug("InteractionMode set to SELCET");
                 cmdSelectActionPerformed(null);
 //            } else if (currentInteractionMode.equals(MappingComponent.CUSTOM_FEATUREINFO)) {
 //                log.debug("InteractionMode set to CUSTOM_FEATUREINFO");
 //                //cmdALB.setSelected(true);
 //                cmdALBActionPerformed(null);
             } else if (currentInteractionMode.equals(MappingComponent.PAN)) {
                 log.debug("InteractionMode set to PAN");
                 cmdPanActionPerformed(null);
             } else if (currentInteractionMode.equals(MappingComponent.NEW_POLYGON)) {
                 log.debug("InteractionMode set to NEW_POLYGON");
                 //Todo Not set beacuse of Belis
             //cmdNewPolygonActionPerformed(null);
             } else if (currentInteractionMode.equals(MappingComponent.ZOOM)) {
                 log.debug("InteractionMode set to ZOOM");
                 cmdZoomActionPerformed(null);
             } else {
                 log.debug("Unknown Interactionmode: " + currentInteractionMode);
             }
         } else {
             log.debug("InteractionMode == null");
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mapWidgetToolbar = new javax.swing.JToolBar();
         cmdFullPoly = new javax.swing.JButton();
         cmdFullPoly1 = new javax.swing.JButton();
         cmdBack = new JHistoryButton();
         cmdForward = new JHistoryButton();
         jSeparator4 = new javax.swing.JSeparator();
         cmdWmsBackground = new javax.swing.JButton();
         cmdForeground = new javax.swing.JButton();
         cmdSnap = new javax.swing.JButton();
         jSeparator5 = new javax.swing.JSeparator();
         cmdZoom = new javax.swing.JButton();
         cmdPan = new javax.swing.JButton();
         cmdSelect = new javax.swing.JButton();
         cmdMovePolygon = new javax.swing.JButton();
         cmdRemovePolygon = new javax.swing.JButton();
         jSeparator6 = new javax.swing.JSeparator();
         cmdMoveHandle = new javax.swing.JButton();
         cmdAddHandle = new javax.swing.JButton();
         cmdRemoveHandle = new javax.swing.JButton();
         jSeparator7 = new javax.swing.JSeparator();
         jPanel1 = new javax.swing.JPanel();
         cmdAdd = new javax.swing.JButton();
 
         setLayout(new java.awt.BorderLayout());
 
         mapWidgetToolbar.setMaximumSize(new java.awt.Dimension(32769, 32769));
         mapWidgetToolbar.setMinimumSize(new java.awt.Dimension(300, 25));
         mapWidgetToolbar.setPreferredSize(new java.awt.Dimension(300, 28));
 
         cmdFullPoly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/fullPoly.png"))); // NOI18N
         cmdFullPoly.setToolTipText("Zeige alle Flächen");
         cmdFullPoly.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdFullPoly.setIconTextGap(8);
         cmdFullPoly.setMargin(new java.awt.Insets(10, 14, 10, 14));
         cmdFullPoly.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdFullPolyActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdFullPoly);
 
         cmdFullPoly1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/fullSelPoly.png"))); // NOI18N
         cmdFullPoly1.setToolTipText("Zoom zur ausgewählten Fläche");
         cmdFullPoly1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdFullPoly1.setIconTextGap(8);
         cmdFullPoly1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdFullPoly1ActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdFullPoly1);
 
         cmdBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/back2.png"))); // NOI18N
         cmdBack.setToolTipText("Zurück");
         cmdBack.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdBackActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdBack);
 
         cmdForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/fwd.png"))); // NOI18N
         cmdForward.setToolTipText("Vor");
         cmdForward.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdForward.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdForwardActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdForward);
 
         jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator4.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator4.setMinimumSize(new java.awt.Dimension(2, 10));
         jSeparator4.setPreferredSize(new java.awt.Dimension(2, 10));
         mapWidgetToolbar.add(jSeparator4);
 
         cmdWmsBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/map.png"))); // NOI18N
         cmdWmsBackground.setToolTipText("Hintergrund an/aus");
         cmdWmsBackground.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdWmsBackground.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/map_on.png"))); // NOI18N
         cmdWmsBackground.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdWmsBackgroundActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdWmsBackground);
 
         cmdForeground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/foreground.png"))); // NOI18N
         cmdForeground.setToolTipText("Vordergrund an/aus");
         cmdForeground.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         cmdForeground.setFocusable(false);
         cmdForeground.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdForeground.setSelected(true);
         cmdForeground.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/foreground_on.png"))); // NOI18N
         cmdForeground.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdForeground.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdForegroundActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdForeground);
 
         cmdSnap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/snap.png"))); // NOI18N
         cmdSnap.setToolTipText("Snapping an/aus");
         cmdSnap.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdSnap.setSelected(true);
         cmdSnap.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/snap_selected.png"))); // NOI18N
         cmdSnap.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdSnapActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdSnap);
 
         jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator5.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator5.setMinimumSize(new java.awt.Dimension(2, 10));
         jSeparator5.setPreferredSize(new java.awt.Dimension(2, 10));
         mapWidgetToolbar.add(jSeparator5);
 
         cmdZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/zoom.png"))); // NOI18N
         cmdZoom.setToolTipText("Zoomen");
         cmdZoom.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdZoom.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/zoom_selected.png"))); // NOI18N
         cmdZoom.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdZoomActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdZoom);
 
         cmdPan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/move2.png"))); // NOI18N
         cmdPan.setToolTipText("Verschieben");
         cmdPan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdPan.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/move2_selected.png"))); // NOI18N
         cmdPan.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPanActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdPan);
 
         cmdSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/select.png"))); // NOI18N
         cmdSelect.setToolTipText("Auswählen");
         cmdSelect.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdSelect.setSelected(true);
         cmdSelect.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/select_selected.png"))); // NOI18N
         cmdSelect.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdSelectActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdSelect);
 
         cmdMovePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/movePoly.png"))); // NOI18N
         cmdMovePolygon.setToolTipText("Polygon verschieben");
         cmdMovePolygon.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdMovePolygon.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/movePoly_selected.png"))); // NOI18N
         cmdMovePolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdMovePolygonActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdMovePolygon);
 
         cmdRemovePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/removePoly.png"))); // NOI18N
         cmdRemovePolygon.setToolTipText("Polygon entfernen");
         cmdRemovePolygon.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdRemovePolygon.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/removePoly_selected.png"))); // NOI18N
         cmdRemovePolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRemovePolygonActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdRemovePolygon);
 
         jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator6.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator6.setMinimumSize(new java.awt.Dimension(2, 10));
         jSeparator6.setPreferredSize(new java.awt.Dimension(2, 10));
         mapWidgetToolbar.add(jSeparator6);
 
         cmdMoveHandle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/moveHandle.png"))); // NOI18N
         cmdMoveHandle.setToolTipText("Handle verschieben");
         cmdMoveHandle.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdMoveHandle.setSelected(true);
         cmdMoveHandle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/moveHandle_selected.png"))); // NOI18N
         cmdMoveHandle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdMoveHandleActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdMoveHandle);
 
         cmdAddHandle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/moveHandle.png"))); // NOI18N
         cmdAddHandle.setToolTipText("Handle hinzufügen");
         cmdAddHandle.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdAddHandle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/moveHandle_selected.png"))); // NOI18N
         cmdAddHandle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdAddHandleActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdAddHandle);
 
         cmdRemoveHandle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/removeHandle.png"))); // NOI18N
         cmdRemoveHandle.setToolTipText("Handle entfernen");
         cmdRemoveHandle.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
         cmdRemoveHandle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/removeHandle_selected.png"))); // NOI18N
         cmdRemoveHandle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRemoveHandleActionPerformed(evt);
             }
         });
         mapWidgetToolbar.add(cmdRemoveHandle);
 
         jSeparator7.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator7.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator7.setMinimumSize(new java.awt.Dimension(2, 10));
         jSeparator7.setPreferredSize(new java.awt.Dimension(2, 10));
         mapWidgetToolbar.add(jSeparator7);
 
         add(mapWidgetToolbar, java.awt.BorderLayout.NORTH);
 
         jPanel1.setMinimumSize(new java.awt.Dimension(50, 100));
         jPanel1.setPreferredSize(new java.awt.Dimension(50, 30));
 
         cmdAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/commons/architecture/resource/icon/toolbar/layersman.png"))); // NOI18N
         cmdAdd.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
         cmdAdd.setBorderPainted(false);
         cmdAdd.setFocusPainted(false);
         cmdAdd.setMinimumSize(new java.awt.Dimension(25, 25));
         cmdAdd.setPreferredSize(new java.awt.Dimension(25, 25));
         cmdAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdAddActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addContainerGap(497, Short.MAX_VALUE)
                 .add(cmdAdd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(cmdAdd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         add(jPanel1, java.awt.BorderLayout.SOUTH);
     }// </editor-fold>//GEN-END:initComponents
     private void cmdMovePolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMovePolygonActionPerformed
         removeMainGroupSelection();
         cmdMovePolygon.setSelected(true);
         mappingComponent.setInteractionMode(MappingComponent.MOVE_POLYGON);
         setLastMapMode(getCurrentMapMode());
         setCurrentMapMode(MapMode.MOVE_POLYGON);
     }//GEN-LAST:event_cmdMovePolygonActionPerformed
 
 private void cmdAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddActionPerformed
     mappingComponent.showInternalLayerWidget(!mappingComponent.isInternalLayerWidgetVisible(), 500);
 }//GEN-LAST:event_cmdAddActionPerformed
 
 private void cmdRemoveHandleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveHandleActionPerformed
     removeHandleGroupSelection();
     cmdRemoveHandle.setSelected(true);
     mappingComponent.setHandleInteractionMode(MappingComponent.REMOVE_HANDLE);
 }//GEN-LAST:event_cmdRemoveHandleActionPerformed
 
 private void cmdAddHandleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddHandleActionPerformed
     removeHandleGroupSelection();
     cmdAddHandle.setSelected(true);
     mappingComponent.setHandleInteractionMode(MappingComponent.ADD_HANDLE);
 }//GEN-LAST:event_cmdAddHandleActionPerformed
 
 private void cmdMoveHandleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMoveHandleActionPerformed
     removeHandleGroupSelection();
     cmdMoveHandle.setSelected(true);
     mappingComponent.setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
 }//GEN-LAST:event_cmdMoveHandleActionPerformed
 
 private void cmdRemovePolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemovePolygonActionPerformed
     removeMainGroupSelection();
     cmdRemovePolygon.setSelected(true);
     mappingComponent.setInteractionMode(MappingComponent.REMOVE_POLYGON);
     setLastMapMode(getCurrentMapMode());
     setCurrentMapMode(MapMode.REMOVE_POLYGON);
 }//GEN-LAST:event_cmdRemovePolygonActionPerformed
 
 private void cmdSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSelectActionPerformed
     removeMainGroupSelection();
     cmdSelect.setSelected(true);
     setLastMapMode(getCurrentMapMode());
     mappingComponent.setInteractionMode(MappingComponent.SELECT);
     cmdMoveHandleActionPerformed(null);
     setCurrentMapMode(MapMode.SELECT);
 }//GEN-LAST:event_cmdSelectActionPerformed
 
     private void removeHandleGroupSelection() {
         cmdRemoveHandle.setSelected(false);
         cmdAddHandle.setSelected(false);
         cmdMoveHandle.setSelected(false);
     }
 
 private void cmdPanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPanActionPerformed
     removeMainGroupSelection();
     cmdPan.setSelected(true);
     mappingComponent.setInteractionMode(MappingComponent.PAN);
     setLastMapMode(getCurrentMapMode());
     setCurrentMapMode(MapMode.PAN);
 }//GEN-LAST:event_cmdPanActionPerformed
 
     public void removeMainGroupSelection() {
         cmdSelect.setSelected(false);
         cmdPan.setSelected(false);
 //    cmdALB.setSelected(false);
         cmdZoom.setSelected(false);
         cmdMovePolygon.setSelected(false);
 //    cmdNewPolygon.setSelected(false);
         // cmdNewPoint.setSelected(false);
         cmdRemovePolygon.setSelected(false);
         // cmdAttachPolyToAlphadata.setSelected(false);
         // cmdSplitPoly.setSelected(false);
         // cmdJoinPoly.setSelected(false);
         // cmdRaisePolygon.setSelected(false);
 
 //        if (mappingComponent.isReadOnly()) {
 //            log.info("Ist Readonly snapping wird diseabled");
 //            mappingComponent.setSnappingEnabled(false);
 //            mappingComponent.setVisualizeSnappingEnabled(false);
 //        }
         for (JButton curButton : customButtons) {
             curButton.setSelected(false);
         }
     }
     private boolean isEditable = true;
 
     // should be static and in MappingComponent.
     public boolean assertMappingComponentMode(String modeToAssert) {
         final String currentInteractionMode = broker.getMappingComponent().getInteractionMode();
         if (currentInteractionMode != null) {
             if (currentInteractionMode.equals(modeToAssert)) {
                 return true;
             } else {
                 return false;
             }
         } else {
             if (modeToAssert == null) {
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     //ToDo here new input events like belis create mode should be checked
     public boolean assertMappingComponentIsInAnEditMode() {
         if (assertMappingComponentMode(MappingComponent.MOVE_POLYGON) ||
                 assertMappingComponentMode(MappingComponent.ADD_HANDLE) ||
                 assertMappingComponentMode(MappingComponent.ATTACH_POLYGON_TO_ALPHADATA) ||
                 assertMappingComponentMode(MappingComponent.JOIN_POLYGONS) ||
                 assertMappingComponentMode(MappingComponent.NEW_POLYGON) ||
                 assertMappingComponentMode(MappingComponent.RAISE_POLYGON) ||
                 assertMappingComponentMode(MappingComponent.REMOVE_POLYGON) ||
                 assertMappingComponentMode(MappingComponent.ROTATE_POLYGON) ||
                 assertMappingComponentMode(MappingComponent.SPLIT_POLYGON)) {
             return true;
         }
         return false;
     }
 
     public synchronized void setWidgetEditable(final boolean isEditable) {
         if (this.isEditable == isEditable) {
             return;
         }
         this.isEditable = isEditable;
         log.debug("MapPanel --> setComponentEditable");
         //Refactor code duplication
         if (EventQueue.isDispatchThread()) {
             mappingComponent.setReadOnly(!isEditable);
             //TODO only change if the actualMode is not allowed
             if (!isEditable) {
                 //mappingComponent.setInteractionMode(mappingComponent.PAN);
                 //TODO is it really the best default mode ?
                 //TODO look how to easily create events (or common)
                 //ToDo LagIS same problem
                 if (assertMappingComponentIsInAnEditMode()) {
                     log.warn("Application is not in edit mode, but mapping component is in an edit mode. Setting back to Select.");
                     removeMainGroupSelection();
                     cmdSelect.setSelected(true);
                     mappingComponent.setInteractionMode(MappingComponent.SELECT);
                     cmdMoveHandleActionPerformed(null);
                 }
             }
             log.debug("Anzahl Features in FeatureCollection:" + mappingComponent.getFeatureCollection().getFeatureCount());
             //((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).setAllFeaturesEditable(isEditable);
             //TODO TEST IT!!!!
             broker.getMappingComponent().setReadOnly(!isEditable);
             cmdMovePolygon.setVisible(isEditable);
             //this.cmdNewPolygon.setVisible(b);
             cmdRemovePolygon.setVisible(isEditable);
             //cmdAttachPolyToAlphadata.setVisible(isEditable);
             //cmdJoinPoly.setVisible(isEditable);
             jSeparator6.setVisible(isEditable);
             cmdMoveHandle.setVisible(isEditable);
             cmdAddHandle.setVisible(isEditable);
             cmdRemoveHandle.setVisible(isEditable);
             //cmdSplitPoly.setVisible(isEditable);
             //cmdRaisePolygon.setVisible(isEditable);
             jSeparator6.setVisible(isEditable);
         } else {
             EventQueue.invokeLater(new Runnable() {
 
                 public void run() {
                     mappingComponent.setReadOnly(!isEditable);
                     //TODO only change if the actualMode is not allowed
                     if (!isEditable) {
                         //mappingComponent.setInteractionMode(mappingComponent.PAN);
                         //TODO is it really the best default mode ?
                         //TODO look how to easily create events (or common)
                         if (assertMappingComponentIsInAnEditMode()) {
                             log.warn("Application is not in edit mode, but mapping component is in an edit mode. Setting back to Select.");
                             removeMainGroupSelection();
                             cmdSelect.setSelected(true);
                             mappingComponent.setInteractionMode(MappingComponent.SELECT);
                             cmdMoveHandleActionPerformed(null);
                         }
                     }
                     log.debug("Anzahl Features in FeatureCollection:" + mappingComponent.getFeatureCollection().getFeatureCount());
                     //((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).setAllFeaturesEditable(isEditable);
                     //TODO TEST IT!!!!
                     broker.getMappingComponent().setReadOnly(!isEditable);
                     cmdMovePolygon.setVisible(isEditable);
                     //this.cmdNewPolygon.setVisible(b);
                     cmdRemovePolygon.setVisible(isEditable);
                     //cmdAttachPolyToAlphadata.setVisible(isEditable);
                     //cmdJoinPoly.setVisible(isEditable);
                     jSeparator6.setVisible(isEditable);
                     cmdMoveHandle.setVisible(isEditable);
                     cmdAddHandle.setVisible(isEditable);
                     cmdRemoveHandle.setVisible(isEditable);
                     //cmdSplitPoly.setVisible(isEditable);
                     //cmdRaisePolygon.setVisible(isEditable);
                     jSeparator6.setVisible(isEditable);
                 }
             });
         }
         log.debug("MapPanel --> setComponentEditable finished");
     }
 
     public synchronized void clearComponent() {
     }
 
 private void cmdZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomActionPerformed
     removeMainGroupSelection();
     cmdZoom.setSelected(true);
     mappingComponent.setInteractionMode(MappingComponent.ZOOM);
     setLastMapMode(getCurrentMapMode());
     setCurrentMapMode(MapMode.ZOOM);
 }//GEN-LAST:event_cmdZoomActionPerformed
 
 private void cmdSnapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSnapActionPerformed
     log.debug("Set snapping Enabled: " + cmdSnap.isSelected());
     cmdSnap.setSelected(!cmdSnap.isSelected());
     //TODO CHANGE CONFIG FILE ACTION
     //cismapPrefs.getGlobalPrefs().setSnappingEnabled(cmdSnap.isSelected());
     //cismapPrefs.getGlobalPrefs().setSnappingPreviewEnabled(cmdSnap.isSelected());
     mappingComponent.setSnappingEnabled(!mappingComponent.isReadOnly() && cmdSnap.isSelected());
     mappingComponent.setVisualizeSnappingEnabled(!mappingComponent.isReadOnly() && cmdSnap.isSelected());
     mappingComponent.setInGlueIdenticalPointsMode(cmdSnap.isSelected());
 }//GEN-LAST:event_cmdSnapActionPerformed
 
     private void cmdWmsBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWmsBackgroundActionPerformed
         if (mappingComponent.isBackgroundEnabled()) {
             mappingComponent.setBackgroundEnabled(false);
             cmdWmsBackground.setSelected(false);
         } else {
             mappingComponent.setBackgroundEnabled(true);
             cmdWmsBackground.setSelected(true);
             mappingComponent.queryServices();
         }
 }//GEN-LAST:event_cmdWmsBackgroundActionPerformed
 
 private void cmdForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdForwardActionPerformed
     // TODO add your handling code here:
 }//GEN-LAST:event_cmdForwardActionPerformed
 
 private void cmdBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdBackActionPerformed
 }//GEN-LAST:event_cmdBackActionPerformed
 
 private void cmdFullPoly1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFullPoly1ActionPerformed
     mappingComponent.zoomToSelectedNode();
 }//GEN-LAST:event_cmdFullPoly1ActionPerformed
 
 private void cmdFullPolyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFullPolyActionPerformed
     mappingComponent.zoomToFullFeatureCollectionBounds();
 }//GEN-LAST:event_cmdFullPolyActionPerformed
 
 private void cmdForegroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdForegroundActionPerformed
     if (mappingComponent.isFeatureCollectionVisible()) {
         mappingComponent.setFeatureCollectionVisibility(false);
         cmdForeground.setSelected(false);
     } else {
         mappingComponent.setFeatureCollectionVisibility(true);
         cmdForeground.setSelected(true);
     }
 }//GEN-LAST:event_cmdForegroundActionPerformed
 
     public void featureDeleteRequested(PNotification notfication) {
         try {
             Object o = notfication.getObject();
             if (o instanceof DeleteFeatureListener) {
                 DeleteFeatureListener dfl = (DeleteFeatureListener) o;
                 PFeature pf = dfl.getFeatureRequestedForDeletion();
                 pf.getFeature().setGeometry(null);
 //            if(pf.getFeature() instanceof Verwaltungsbereich){
 //                log.debug("Verwaltungsbereichsgeometrie wurde gelöscht setze Flächee = 0");                
 //            }
             }
         } catch (Exception ex) {
             log.warn("Fehler beim featuredeleteRequest", ex);
         }
     }
 
     public void joinPolygons(PNotification notification) {
         PFeature one, two;
         one = mappingComponent.getSelectedNode();
         two = null;
         log.debug("");
         Object o = notification.getObject();
 
         if (o instanceof JoinPolygonsListener) {
             JoinPolygonsListener listener = ((JoinPolygonsListener) o);
             PFeature joinCandidate = listener.getFeatureRequestedForJoin();
             if (joinCandidate.getFeature() instanceof StyledFeature || joinCandidate.getFeature() instanceof PureNewFeature) {
                 int CTRL_MASK = 2; //TODO: HIer noch eine korrekte Konstante verwenden
                 if ((listener.getModifier() & CTRL_MASK) != 0) {
 
                     if (one != null && joinCandidate != one) {
                         if (one.getFeature() instanceof PureNewFeature && joinCandidate.getFeature() instanceof StyledFeature) {
                             two = one;
 
                             one = joinCandidate;
                             one.setSelected(true);
                             two.setSelected(false);
                             mappingComponent.getFeatureCollection().select(one.getFeature());
                         //tableModel.setSelectedFlaeche((Flaeche)one.getFeature());
                         //TODO implement or erase
                         //fireAuswahlChanged(one.getFeature());
                         } else {
                             two = joinCandidate;
                         }
                         try {
 
                             Geometry backup = one.getFeature().getGeometry();
                             Geometry newGeom = one.getFeature().getGeometry().union(two.getFeature().getGeometry());
                             if (newGeom.getGeometryType().equalsIgnoreCase("Multipolygon")) {
                                 JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this), "Es können nur Polygone zusammengefasst werden, die aneinander angrenzen oder sich überlappen.", "Zusammenfassung nicht möglich", JOptionPane.WARNING_MESSAGE, null);
                                 return;
                             }
                             if (newGeom.getGeometryType().equalsIgnoreCase("Polygon") && ((Polygon) newGeom).getNumInteriorRing() > 0) {
                                 JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this), "Polygone können nur dann zusammengefasst werden, wenn dadurch kein Loch entsteht.", "Zusammenfassung nicht möglich", JOptionPane.WARNING_MESSAGE, null);
                                 return;
                             }
                             if (one != null && two != null && one.getFeature() instanceof StyledFeature && two.getFeature() instanceof StyledFeature) {
                                 StyledFeature fOne = (StyledFeature) one.getFeature();
                                 StyledFeature fTwo = (StyledFeature) two.getFeature();
 //                            
 //                            if((fOne instanceof Verwaltungsbereich && !(fTwo instanceof Verwaltungsbereich)) || (fTwo instanceof Verwaltungsbereich && !(fOne instanceof Verwaltungsbereich))){
 //                                JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),"Flächeen können nur zusammengefasst werden, wenn die Flächeenart gleich ist.","Zusammenfassung nicht möglich",JOptionPane.WARNING_MESSAGE,null );
 //                                return;
 //                            }
 //                            
 //                            if((fOne instanceof ReBe && !(fTwo instanceof ReBe)) || (fTwo instanceof ReBe && !(fOne instanceof ReBe))){
 //                                JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),"Flächeen können nur zusammengefasst werden, wenn die Flächeenart gleich ist.","Zusammenfassung nicht möglich",JOptionPane.WARNING_MESSAGE,null );
 //                                return;
 //                            }
 
 //                            if (fOne.getArt()!=fTwo.getArt()||fOne.getGrad()!=fTwo.getGrad()) {
 //                                JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),"Flächeen können nur zusammengefasst werden, wenn Flächeenart und Anschlussgrad gleich sind.","Zusammenfassung nicht möglich",JOptionPane.WARNING_MESSAGE,null );
 //                                return;
 //                            }
 //                            //Check machen ob eine Flächee eine Teilflächee ist
 //                            if (fOne.getAnteil()!=null || fTwo.getAnteil()!=null) {
 //                                JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),"Flächeen die von Teileigentum betroffen sind können nicht zusammengefasst werden.","Zusammenfassung nicht möglich",JOptionPane.WARNING_MESSAGE,null );
 //                                return;
 //                            }
                                 two.getFeature().setGeometry(null);
                             //tableModel.removeFlaeche(fTwo);
                             //TODO größe updaten
 
                             //TODO make it right
                             //fOne.setGr_grafik(new Integer((int)(newGeom.getArea())));
 //                            if (fOne.getBemerkung()!=null && fOne.getBemerkung().trim().length()>0) {
 //                                fOne.setBemerkung(fOne.getBemerkung()+"\n");
 //                            }
 //                            fOne.setBemerkung(fTwo.getJoinBackupString());
 //                            if (!fOne.isSperre()&&fTwo.isSperre()) {
 //                                fOne.setSperre(true);
 //                                fOne.setBem_sperre("JOIN::"+fTwo.getBem_sperre());
 //                            }
 //                            fOne.sync();
 //                            //tableModel.fireSelectionChanged(); TODO
 //                            fireAuswahlChanged(fOne);
                             }
 //                        if (one.getFeature() instanceof Verwaltungsbereich) {
 //                            //Eine vorhandene Flächee und eine neuangelegt wurden gejoint                            
 //                            //((Flaeche)(one.getFeature())).sync();
 //                            //tableModel.fireSelectionChanged(); TODO
 //                            //fireAuswahlChanged((Flaeche)(one.getFeature()));
 //                        }
 
                             log.debug("newGeom ist vom Typ:" + newGeom.getGeometryType());
                             one.getFeature().setGeometry(newGeom);
                             if (!(one.getFeature().getGeometry().equals(backup))) {
                                 two.removeFromParent();
                                 two = null;
                             }
                             one.visualize();
 
                         } catch (Exception e) {
                             log.error("one: " + one + "\n two: " + two, e);
                         }
                         return;
                     }
                 } else {
                     PFeature pf = joinCandidate;
                     if (one != null) {
                         one.setSelected(false);
                     }
                     one = pf;
                     mappingComponent.selectPFeatureManually(one);
                     if (one.getFeature() instanceof StyledFeature) {
                         StyledFeature f = (StyledFeature) one.getFeature();
                         mappingComponent.getFeatureCollection().select(f);
                         //tableModel.setSelectedFlaeche(f);
                         //fireAuswahlChanged(f);
                         try {
                             //TODO
                             //makeRowVisible(this.jxtOverview,jxtOverview.getFilters().convertRowIndexToView(tableModel.getIndexOfFlaeche((Flaeche)f)));
                         } catch (Exception e) {
                             log.debug("Fehler beim Scrollen der Tabelle", e);
                         }
                     } else {
                         //tableModel.setSelectedFlaeche(null);
                         mappingComponent.getFeatureCollection().unselectAll();
                     //fireAuswahlChanged(null);
                     }
                 }
             }
         }
     }
 
 //TODO MEssage to the user if a area could not be attached for example wfs areas
     public void attachFeatureRequested(PNotification notification) {
         Object o = notification.getObject();
         log.info("Try to attach Geometry");
         AttachFeatureListener afl = (AttachFeatureListener) o;
         PFeature pf = afl.getFeatureToAttach();
         if (pf.getFeature() instanceof PureNewFeature) {
             Geometry g = pf.getFeature().getGeometry();
             GeometrySlotInformation slotInfo = broker.assignGeometry(g);
             if (slotInfo != null) {
                 slotInfo.getRefreshable().refresh(null);
                 mappingComponent.getFeatureCollection().removeFeature(pf.getFeature());
                 log.debug("Geometrie: " + slotInfo.getOpenSlot().getGeometry() + " wird hinzugefügt");
                 slotInfo.getOpenSlot().setEditable(true);
                 mappingComponent.getFeatureCollection().addFeature(slotInfo.getOpenSlot());
                 log.debug("Geometrie wurde an element: " + slotInfo.getSlotIdentifier() + " attached");
             } else {
                 log.debug("Geometrie wurde nicht attached");
             }
         }
 //    } else if(pf.getFeature() instanceof Geom){
 //        
 //    }
     }
 
     public void splitPolygon(PNotification notification) {
         Object o = notification.getObject();
         if (o instanceof SplitPolygonListener) {
             SplitPolygonListener l = (SplitPolygonListener) o;
             PFeature pf = l.getFeatureClickedOn();
             if (pf.isSplittable()) {
                 log.debug("Split");
                 ((StyledFeature) pf.getFeature()).setGeometry(null);
                 Feature[] f_arr = pf.split();
                 mappingComponent.getFeatureCollection().removeFeature(pf.getFeature());
                 f_arr[0].setEditable(true);
                 f_arr[1].setEditable(true);
                 mappingComponent.getFeatureCollection().addFeature(f_arr[0]);
                 mappingComponent.getFeatureCollection().addFeature(f_arr[1]);
             //cmdAttachPolyToAlphadataActionPerformed(null);
             }
         }
     }
 
 
 //ToDo implement
     public void coordinatesChanged(PNotification notification) {
 //    Object o=notification.getObject();
 //    if (o instanceof SimpleMoveListener) {
 //        double x=((SimpleMoveListener)o).getXCoord();
 //        double y=((SimpleMoveListener)o).getYCoord();
 //        double scale=((SimpleMoveListener)o).getCurrentOGCScale();
 //
 //        //double test= mappingComp.getWtst().getSourceX(36)-this.mappingComp.getWtst().getSourceX(0))/mappingComp.getCamera().getViewScale();
 //        //scale +" ... "+
 //        //setgetlblCoord.setText(MappingComponent.getCoordinateString(x,y));
 //
 //    }
 ////        PFeature pf=((SimpleMoveListener)o).getUnderlyingPFeature();
 ////
 ////        if (pf!=null&&pf.getFeature() instanceof DefaultFeatureServiceFeature &&pf.getVisible()==true&&pf.getParent()!=null&&pf.getParent().getVisible()==true) {
 ////            lblInfo.setText(((DefaultFeatureServiceFeature)pf.getFeature()).getObjectName());
 ////        } else if (pf!=null&&pf.getFeature() instanceof Flaeche) {
 ////            String name="Kassenzeichen: "+((Flaeche)pf.getFeature()).getKassenzeichen()+"::"+((Flaeche)pf.getFeature()).getBezeichnung();
 ////            lblInfo.setText(name);
 ////        } else {
 ////            lblInfo.setText("");
 //        }
         //}
     }
 
     public void selectionChanged(PNotification notfication) {
         Object o = notfication.getObject();
         if (o instanceof SelectionListener || o instanceof FeatureMoveListener || o instanceof SplitPolygonListener) {
             PNode p = null;
             PFeature pf = null;
             if (o instanceof SelectionListener) {
                 pf = ((SelectionListener) o).getSelectedPFeature();
                 //
                 //                if (pf!=null && pf.getFeature() instanceof Flaeche|| pf.getFeature() instanceof PureNewFeature) {
                 //                    if (((DefaultFeatureCollection)mappingComp.getFeatureCollection()).isSelected(pf.getFeature())) {
                 //                        if (((DefaultFeatureCollection)mappingComp.getFeatureCollection()).getSelectedFeatures().size()>1) {
                 //                            int index=sorter.getSortedPosition(getTableModel().getIndexOfFlaeche((Flaeche)pf.getFeature()));
                 //                            tblOverview.getSelectionModel().addSelectionInterval(index,index);
                 //                        } else {
                 //                            int index=sorter.getSortedPosition(getTableModel().getIndexOfFlaeche((Flaeche)pf.getFeature()));
                 //                            tblOverview.getSelectionModel().setSelectionInterval(index,index);
                 //                        }
                 //                    }
                 //                    else {
                 //                        int index=sorter.getSortedPosition(getTableModel().getIndexOfFlaeche((Flaeche)pf.getFeature()));
                 //                        tblOverview.getSelectionModel().removeSelectionInterval(index,index);
                 //                    }
                 //                } else
                if (cmdSelect.isSelected() && ((SelectionListener) o).getClickCount() > 1 && pf.getFeature() instanceof DefaultWFSFeature) {
                    log.debug("DefaultWFSFeature selected");
                     //log.debug("test"+((DefaultWFSFeature)pf.getFeature()).getProperties());
                    DefaultWFSFeature dwf = ((DefaultWFSFeature) pf.getFeature());
 //                    if(LagisBroker.getInstance().isInEditMode()){
 //                        log.debug("Flurstück kann nicht gewechselt werden --> Editmode");
 //                        JOptionPane.showMessageDialog(LagisBroker.getInstance().getParentComponent(),"Das Flurstück kann nur gewechselt werden, wenn alle Änderungen gespeichert oder verworfen worden sind.","Wechseln nicht möglich",JOptionPane.WARNING_MESSAGE);
 //                        return;
 //                    }
 //                    
                     HashMap props = dwf.getProperties();
                     log.debug("WFSFeature properties: " + props);
 //                    try{
 //                        if(props != null && checkIfIdentifiersAreSetProperly()){
 //                            String gem = (String)props.get(gemarkungIdentifier);
 //                            String flur =  (String)props.get(flurIdentifier);
 //                            String flurstz= (String)props.get(flurstueckZaehlerIdentifier);
 //                            String flurstn= (String)props.get(flurstueckNennerIdentifier);
 //                            if(gem != null && flur != null && flurstz != null){
 //                                Gemarkung resolvedGemarkung = LagisBroker.getInstance().getGemarkungForKey(Integer.parseInt(gem));
 //                                //TODO if this case happens it leads to bug XXX
 //                                if(resolvedGemarkung==null){
 //                                    log.debug("Gemarkung konnte nicht entschlüsselt werden");
 //                                    resolvedGemarkung = new Gemarkung();
 //                                    resolvedGemarkung.setSchluessel(Integer.parseInt(gem));
 //                                }else{
 //                                    log.debug("Gemarkung konnte entschlüsselt werden");
 //                                }
 //                                //Gemarkung cplGemarkung = EJBroker.getInstance().completeGemarkung(gemarkung);
 ////                        if (cplGemarkung != null){
 ////                            log.debug("gemarkung bekannt");
 ////                            gemarkung = cplGemarkung;
 ////                        }
 //                                FlurstueckSchluessel key = new FlurstueckSchluessel();
 //                                key.setGemarkung(resolvedGemarkung);
 //                                key.setFlur(Integer.parseInt(flur));
 //                                key.setFlurstueckZaehler(Integer.parseInt(flurstz));
 //                                if(flurstn != null){
 //                                    key.setFlurstueckNenner(Integer.parseInt(flurstn));
 //                                } else {
 //                                    key.setFlurstueckNenner(0);
 //                                }
 //                                log.debug("Schlüssel konnte konstruiert werden");
 //                                LagisBroker.getInstance().loadFlurstueck(key);
 //                            } else {
 //                                log.debug("Mindestens ein Property == null Flurstueck kann nicht ausgewählt werden");
 //                            }
 //                        } else {
 //                            log.error("Properties == null Flurstueck oder Identifier im Konfigfile nicht richtig gesetzt --> kann nicht ausgewählt werden");
 //                        }
 //                    }catch(final Exception ex){
 //                        log.error("Fehler beim laden des ausgewählten Flurstücks",ex);
 //                    }
                 }
             }
         }
     }
 
     public void refresh(Object refreshObject) {
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton cmdAdd;
     private javax.swing.JButton cmdAddHandle;
     private javax.swing.JButton cmdBack;
     private javax.swing.JButton cmdForeground;
     private javax.swing.JButton cmdForward;
     private javax.swing.JButton cmdFullPoly;
     private javax.swing.JButton cmdFullPoly1;
     private javax.swing.JButton cmdMoveHandle;
     private javax.swing.JButton cmdMovePolygon;
     private javax.swing.JButton cmdPan;
     private javax.swing.JButton cmdRemoveHandle;
     private javax.swing.JButton cmdRemovePolygon;
     private javax.swing.JButton cmdSelect;
     private javax.swing.JButton cmdSnap;
     private javax.swing.JButton cmdWmsBackground;
     private javax.swing.JButton cmdZoom;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JSeparator jSeparator5;
     private javax.swing.JSeparator jSeparator6;
     private javax.swing.JSeparator jSeparator7;
     private javax.swing.JToolBar mapWidgetToolbar;
     // End of variables declaration//GEN-END:variables
 //    @Override
 //    public String getWidgetName() {
 //        return WIDGET_NAME;
 //    }
 
     private boolean checkIfIdentifiersAreSetProperly() {
         return (gemarkungIdentifier != null && flurIdentifier != null && flurstueckZaehlerIdentifier != null && flurstueckNennerIdentifier != null);
     }
 
     public void masterConfigure(Element parent) {
         System.out.println("MapWidget masterConfigure");
         ConfigurationManager configManager = broker.getConfigManager();
         configManager.addConfigurable((ActiveLayerModel) mappingModel);
         configManager.configure(mappingModel);
         mappingComponent.preparationSetMappingModel(mappingModel);
         configManager.addConfigurable(mappingComponent);
         //ToDo should be no error (defaultValues) if there is no configuration :-)
         configManager.configure(mappingComponent);
         mappingComponent.setMappingModel(mappingModel);
         //setIsCoreWidget(true);        
 
         initComponents();
         setWidgetEditable(false);
         mappingComponent.getFeatureCollection().addFeatureCollectionListener(this);
         mappingComponent.setBackgroundEnabled(true);
         PNotificationCenter.defaultCenter().addListener(this,
                 "attachFeatureRequested",
                 AttachFeatureListener.ATTACH_FEATURE_NOTIFICATION,
                 mappingComponent.getInputListener(MappingComponent.ATTACH_POLYGON_TO_ALPHADATA));
         PNotificationCenter.defaultCenter().addListener(this,
                 "selectionChanged",
                 SplitPolygonListener.SELECTION_CHANGED,
                 mappingComponent.getInputListener(MappingComponent.SPLIT_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "splitPolygon",
                 SplitPolygonListener.SPLIT_FINISHED,
                 mappingComponent.getInputListener(MappingComponent.SPLIT_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "featureDeleteRequested",
                 DeleteFeatureListener.FEATURE_DELETE_REQUEST_NOTIFICATION,
                 mappingComponent.getInputListener(MappingComponent.REMOVE_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "joinPolygons",
                 JoinPolygonsListener.FEATURE_JOIN_REQUEST_NOTIFICATION,
                 mappingComponent.getInputListener(MappingComponent.JOIN_POLYGONS));
         PNotificationCenter.defaultCenter().addListener(this,
                 "selectionChanged",
                 PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                 mappingComponent.getInputListener(MappingComponent.SELECT));
         PNotificationCenter.defaultCenter().addListener(this,
                 "selectionChanged",
                 FeatureMoveListener.SELECTION_CHANGED_NOTIFICATION,
                 mappingComponent.getInputListener(MappingComponent.MOVE_POLYGON));
         PNotificationCenter.defaultCenter().addListener(this,
                 "coordinatesChanged",
                 SimpleMoveListener.COORDINATES_CHANGED,
                 mappingComponent.getInputListener(MappingComponent.MOTION));
         ((JHistoryButton) cmdForward).setDirection(JHistoryButton.DIRECTION_FORWARD);
         ((JHistoryButton) cmdBack).setDirection(JHistoryButton.DIRECTION_BACKWARD);
         ((JHistoryButton) cmdForward).setHistoryModel(mappingComponent);
         ((JHistoryButton) cmdBack).setHistoryModel(mappingComponent);
         mappingComponent.setBackground(getBackground());
         mappingComponent.setBackgroundEnabled(true);
         mappingComponent.setInternalLayerWidgetAvailable(true);
         this.add(BorderLayout.CENTER, mappingComponent);
 //        log.debug("MasterConfigure: "+this.getClass());
 //        try{
 //        Element identifier = parent.getChild("flurstueckXMLIdentifier");
 //        gemarkungIdentifier = identifier.getChildText("gemarkungIdentifier");
 //        log.debug("GemarkungsIdentifier: "+ gemarkungIdentifier);
 //        flurIdentifier = identifier.getChildText("flurIdentifier");
 //        log.debug("FlurIdentifier: "+ flurIdentifier);
 //        flurstueckZaehlerIdentifier = identifier.getChildText("flurstueckZaehlerIdentifier");
 //        log.debug("FlurstueckZaehlerIdentifier: "+ flurstueckZaehlerIdentifier);
 //        flurstueckNennerIdentifier = identifier.getChildText("flurstueckNennerIdentifier");
 //        log.debug("FlurstueckNennerIdentifier: "+ flurstueckNennerIdentifier);
 //        log.debug("MasterConfigure: "+this.getClass()+" erfolgreich");
 //        }catch(Exception ex){
 //            log.error("Fehler beim masterConfigure von: "+this.getClass(),ex);
 //        }
     }
     //ToDo getConfiguration ??    
 
     public void configure(Element parent) {
         log.debug("Configure: "+this.getClass());
              cmdSnap.setSelected(true);
 //                    //TODO CHANGE CONFIG FILE ACTION
 
                     mappingComponent.setSnappingEnabled(true);
 //                    mappingComponent.setVisualizeSnappingEnabled(!mappingComponent.isReadOnly()&&cmdSnap.isSelected());
 //                    mappingComponent.setInGlueIdenticalPointsMode(cmdSnap.isSelected());
 //        Element prefs=parent.getChild("cismapMappingPreferences");
 //        try{
 //            try {
 //                isSnappingEnabled=prefs.getAttribute("snapping").getBooleanValue();
 //                if(isSnappingEnabled){
 //                    cmdSnap.setSelected(isSnappingEnabled);
 //                    //TODO CHANGE CONFIG FILE ACTION
 //                    //cismapPrefs.getGlobalPrefs().setSnappingEnabled(cmdSnap.isSelected());
 //                    //cismapPrefs.getGlobalPrefs().setSnappingPreviewEnabled(cmdSnap.isSelected());
 //                    mappingComponent.setSnappingEnabled(!mappingComponent.isReadOnly()&&cmdSnap.isSelected());
 //                    mappingComponent.setVisualizeSnappingEnabled(!mappingComponent.isReadOnly()&&cmdSnap.isSelected());
 //                    mappingComponent.setInGlueIdenticalPointsMode(cmdSnap.isSelected());
 //                }
 //
 //            } catch (Exception ex) {
 //                log.warn("Fehler beim setzen des Snapping",ex);
 //            }
 //
 //        } catch(Exception ex){
 //            log.error("Fehler beim konfigurieren des Kartenpanels: ",ex);
 //        }
     }
 
     public void featuresRemoved(FeatureCollectionEvent fce) {
     }
 
     public void featuresChanged(FeatureCollectionEvent fce) {
         // try{
         log.debug("FeatureChanged");
 //        Collection<Feature> features =  fce.getEventFeatures();
 //        if(features != null){
 //            for(Feature currentFeature:features){
 //                if(currentFeature instanceof Verwaltungsbereich){
 //                    ((Verwaltungsbereich)currentFeature).setFlaeche((int)currentFeature.getGeometry().getArea());
 //                }
 //            }
 //        }
 //        }catch(Exception ex){
 //            log.warn("Fehler beim featureChanged");
 //        }
     }
 
     public void featuresAdded(FeatureCollectionEvent fce) {
     }
 
     public void featureSelectionChanged(FeatureCollectionEvent fce) {
         log.debug("FeatureSelection Changed");
         Collection<Feature> features = fce.getEventFeatures();
         broker.fireChangeEvent(features);
     }
 
     public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
     }
 
     public void allFeaturesRemoved(FeatureCollectionEvent fce) {
     }
 
     public void featureCollectionChanged() {
     }
 
     @Override
     public void guiObjectChanged(Object changedObject) {
     }
 
     public JToolBar getMapWidgetToolbar() {
         return mapWidgetToolbar;
     }
 
     public void setMapWidgetToolbar(JToolBar mapWidgetToollbar) {
         this.mapWidgetToolbar = mapWidgetToollbar;
     }
 }
