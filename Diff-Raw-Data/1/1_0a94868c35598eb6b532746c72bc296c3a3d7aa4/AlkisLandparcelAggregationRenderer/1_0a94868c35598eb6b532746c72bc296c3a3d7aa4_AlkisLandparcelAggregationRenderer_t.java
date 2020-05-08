 /*
  * Copyright (C) 2011 cismet GmbH
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * AlkisLandparcelAggregationRenderer.java
  *
  * Created on 07.07.2011, 09:27:21
  */
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import com.vividsolutions.jts.geom.Geometry;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanAggregationRenderer;
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 import de.cismet.tools.gui.downloadmanager.MultipleDownload;
 import de.cismet.tools.gui.downloadmanager.SingleDownload;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import org.apache.log4j.Logger;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author jweintraut
  */
 public class AlkisLandparcelAggregationRenderer extends javax.swing.JPanel implements CidsBeanAggregationRenderer {
     private static final Logger LOG = Logger.getLogger(AlkisLandparcelAggregationRenderer.class);
     
     private static final String PRODUCT_ACTION_TAG_FLURSTUECKSNACHWEIS = "custom.alkis.product.flurstuecksnachweis";
     private static final String PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_NRW = "custom.alkis.product.flurstuecks_eigentumsnachweis_nrw";
     private static final String PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_KOM = "custom.alkis.product.flurstuecks_eigentumsnachweis_kom";
     private static final String PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_KOM_INTERN = "custom.alkis.product.flurstuecks_eigentumsnachweis_kom_intern";
     private static final String PRODUCT_ACTION_TAG_KARTE = "custom.alkis.product.karte";
     
     private static final Color[] COLORS = new Color[]{
         new Color(247, 150, 70),
         new Color(155, 187, 89),
         new Color(128, 100, 162),
         new Color(75, 172, 198),
         new Color(192, 80, 77)
     };
     
     private static volatile boolean initialisedMap = false;
     
     private List<CidsBeanWrapper> cidsBeanWrappers;
     private LandparcelTableModel tableModel;
     private MappingComponent map;
     private CidsBeanWrapper selectedCidsBeanWrapper;
     private Thread mapThread;
     
 
     /** Creates new form AlkisLandparcelAggregationRenderer */
     public AlkisLandparcelAggregationRenderer() {
         tableModel = new LandparcelTableModel();
         initComponents();
         
         map = new MappingComponent();
         pnlMap.add(map, BorderLayout.CENTER);
         tblLandparcels.setDefaultRenderer(Color.class, new ColorRenderer());
         tblLandparcels.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                 
                 if(lsm.isSelectionEmpty()) {
                     selectedCidsBeanWrapper = null;
                 } else {
                     selectedCidsBeanWrapper = tableModel.get(lsm.getLeadSelectionIndex());
                 }
                 changeMap();
             }
         });
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
 
         pnlButtons = new RoundedPanel();
         srpHeaderButtons = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderButtons = new javax.swing.JLabel();
         jxlFlurstuecksnachweis = new org.jdesktop.swingx.JXHyperlink();
         jxlNachweisNRW = new org.jdesktop.swingx.JXHyperlink();
         jxlNachweisKommunal = new org.jdesktop.swingx.JXHyperlink();
         jxlNachweisKommunalIntern = new org.jdesktop.swingx.JXHyperlink();
         jxlKarte = new org.jdesktop.swingx.JXHyperlink();
         pnlMap = new javax.swing.JPanel();
         pnlLandparcels = new RoundedPanel();
         srpHeaderLandparcels = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeaderLandparcels = new javax.swing.JLabel();
         scpLandparcels = new javax.swing.JScrollPane();
         tblLandparcels = new javax.swing.JTable();
 
         setLayout(new java.awt.GridBagLayout());
 
         pnlButtons.setOpaque(false);
         pnlButtons.setLayout(new java.awt.GridBagLayout());
 
         srpHeaderButtons.setBackground(java.awt.Color.darkGray);
         srpHeaderButtons.setLayout(new java.awt.GridBagLayout());
 
         lblHeaderButtons.setForeground(java.awt.Color.white);
         lblHeaderButtons.setText(org.openide.util.NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.lblHeaderButtons.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeaderButtons.add(lblHeaderButtons, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         pnlButtons.add(srpHeaderButtons, gridBagConstraints);
 
         jxlFlurstuecksnachweis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlFlurstuecksnachweis.setText(org.openide.util.NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.jxlFlurstuecksnachweis.text")); // NOI18N
         jxlFlurstuecksnachweis.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlFlurstuecksnachweisActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
         pnlButtons.add(jxlFlurstuecksnachweis, gridBagConstraints);
 
         jxlNachweisNRW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlNachweisNRW.setText(org.openide.util.NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.jxlNachweisNRW.text")); // NOI18N
         jxlNachweisNRW.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlNachweisNRWActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
         pnlButtons.add(jxlNachweisNRW, gridBagConstraints);
 
         jxlNachweisKommunal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlNachweisKommunal.setText(org.openide.util.NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.jxlNachweisKommunal.text")); // NOI18N
         jxlNachweisKommunal.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlNachweisKommunalActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
         pnlButtons.add(jxlNachweisKommunal, gridBagConstraints);
 
         jxlNachweisKommunalIntern.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlNachweisKommunalIntern.setText(org.openide.util.NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.jxlNachweisKommunalIntern.text")); // NOI18N
         jxlNachweisKommunalIntern.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlNachweisKommunalInternActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
         pnlButtons.add(jxlNachweisKommunalIntern, gridBagConstraints);
 
         jxlKarte.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/icons/pdf.png"))); // NOI18N
         jxlKarte.setText(org.openide.util.NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.jxlKarte.text")); // NOI18N
         jxlKarte.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxlKarteActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(7, 10, 10, 10);
         pnlButtons.add(jxlKarte, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
         add(pnlButtons, gridBagConstraints);
 
         pnlMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnlMap.setLayout(new java.awt.BorderLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         add(pnlMap, gridBagConstraints);
 
         pnlLandparcels.setOpaque(false);
        pnlLandparcels.setPreferredSize(new java.awt.Dimension(270, 376));
         pnlLandparcels.setLayout(new java.awt.GridBagLayout());
 
         srpHeaderLandparcels.setBackground(java.awt.Color.darkGray);
         srpHeaderLandparcels.setLayout(new java.awt.GridBagLayout());
 
         lblHeaderLandparcels.setForeground(java.awt.Color.white);
         lblHeaderLandparcels.setText(org.openide.util.NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.lblHeaderLandparcels.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         srpHeaderLandparcels.add(lblHeaderLandparcels, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         pnlLandparcels.add(srpHeaderLandparcels, gridBagConstraints);
 
         scpLandparcels.setPreferredSize(new java.awt.Dimension(250, 402));
 
         tblLandparcels.setModel(tableModel);
         tblLandparcels.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         tblLandparcels.setShowVerticalLines(false);
         tblLandparcels.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 tblLandparcelsFocusLost(evt);
             }
         });
         scpLandparcels.setViewportView(tblLandparcels);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.35;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlLandparcels.add(scpLandparcels, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
         add(pnlLandparcels, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     private void jxlFlurstuecksnachweisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlFlurstuecksnachweisActionPerformed
         downloadEinzelnachweisProduct(jxlFlurstuecksnachweis.getText(), AlkisUtils.PRODUCTS.FLURSTUECKSNACHWEIS_PDF, PRODUCT_ACTION_TAG_FLURSTUECKSNACHWEIS);
     }//GEN-LAST:event_jxlFlurstuecksnachweisActionPerformed
 
     private void jxlNachweisNRWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlNachweisNRWActionPerformed
         downloadEinzelnachweisProduct(jxlNachweisNRW.getText(), AlkisUtils.PRODUCTS.FLURSTUECKS_UND_EIGENTUMSNACHWEIS_NRW_PDF, PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_NRW);
     }//GEN-LAST:event_jxlNachweisNRWActionPerformed
 
     private void jxlNachweisKommunalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlNachweisKommunalActionPerformed
         downloadEinzelnachweisProduct(jxlNachweisKommunal.getText(), AlkisUtils.PRODUCTS.FLURSTUECKS_UND_EIGENTUMSNACHWEIS_KOMMUNAL_PDF, PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_KOM);
     }//GEN-LAST:event_jxlNachweisKommunalActionPerformed
 
     private void jxlNachweisKommunalInternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlNachweisKommunalInternActionPerformed
         downloadEinzelnachweisProduct(jxlNachweisKommunalIntern.getText(), AlkisUtils.PRODUCTS.FLURSTUECKS_UND_EIGENTUMSNACHWEIS_KOMMUNAL_INTERN_PDF, PRODUCT_ACTION_TAG_FLURSTUECKS_EIGENTUMSNACHWEIS_KOM_INTERN);
     }//GEN-LAST:event_jxlNachweisKommunalInternActionPerformed
 
     private void jxlKarteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxlKarteActionPerformed
         downloadKarteProduct(jxlKarte.getText());
     }//GEN-LAST:event_jxlKarteActionPerformed
 
     private void tblLandparcelsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblLandparcelsFocusLost
         map.gotoInitialBoundingBox();
         tblLandparcels.clearSelection();
     }//GEN-LAST:event_tblLandparcelsFocusLost
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXHyperlink jxlFlurstuecksnachweis;
     private org.jdesktop.swingx.JXHyperlink jxlKarte;
     private org.jdesktop.swingx.JXHyperlink jxlNachweisKommunal;
     private org.jdesktop.swingx.JXHyperlink jxlNachweisKommunalIntern;
     private org.jdesktop.swingx.JXHyperlink jxlNachweisNRW;
     private javax.swing.JLabel lblHeaderButtons;
     private javax.swing.JLabel lblHeaderLandparcels;
     private javax.swing.JPanel pnlButtons;
     private javax.swing.JPanel pnlLandparcels;
     private javax.swing.JPanel pnlMap;
     private javax.swing.JScrollPane scpLandparcels;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeaderButtons;
     private de.cismet.tools.gui.SemiRoundedPanel srpHeaderLandparcels;
     private javax.swing.JTable tblLandparcels;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public Collection<CidsBean> getCidsBeans() {
         final Collection<CidsBean> result = new LinkedList<CidsBean>();
         
         for(CidsBeanWrapper wrapper : cidsBeanWrappers) {
             result.add(wrapper.getCidsBean());
         }
         
         return result;
     }
 
     @Override
     public void setCidsBeans(Collection<CidsBean> cidsBeans) {
         if(cidsBeans != null) {
             int colorIndex = 0;
             cidsBeanWrappers = new LinkedList<CidsBeanWrapper>();
             
             for(CidsBean cidsBean : cidsBeans) {
                 cidsBeanWrappers.add(new CidsBeanWrapper(cidsBean, true));
             }
             Collections.sort(cidsBeanWrappers);
             for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
                 cidsBeanWrapper.setColor(COLORS[colorIndex]);
                 colorIndex = (colorIndex + 1) % COLORS.length;
             }
             
             tableModel.setCidsBeans(cidsBeanWrappers);
             
             initMap();
             
             if(tblLandparcels != null && tblLandparcels.getColumnModel() != null) {
                 TableColumn column = tblLandparcels.getColumnModel().getColumn(0);
                 if(column != null) {
                     column.setPreferredWidth(20);
                 }
                 column = tblLandparcels.getColumnModel().getColumn(3);
                 if(column != null) {
                     column.setPreferredWidth(15);
                 }
             }
             
             changeButtonAvailability(cidsBeanWrappers.size() > 0);
         }
     }
 
     @Override
     public void dispose() {
         map.dispose();
     }
 
     @Override
     public String getTitle() {
         return NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.title", (cidsBeanWrappers != null ? cidsBeanWrappers.size() : "0"));
     }
 
     @Override
     public void setTitle(String title) {
         //NOP
     }
     
     private void initMap() {
         mapThread = new Thread(new InitialiseMapRunnable());
         if (EventQueue.isDispatchThread()) {
             mapThread.start();
         } else {
             EventQueue.invokeLater(mapThread);
         }
         
         revalidate();
         repaint();
     }
     
     private void changeMap() {
         if(mapThread != null && mapThread.isAlive()) {
             if(initialisedMap) {
                 //Map is initialised. Can be changed.
                 mapThread.interrupt();
             } else {
                 //Initialising the map is still running. Don't change the map now.
                 return;
             }
         }
         
         mapThread = new Thread(new ChangeMapRunnable());
         if (EventQueue.isDispatchThread()) {
             mapThread.start();
         } else {
             EventQueue.invokeLater(mapThread);
         }
         
         revalidate();
         repaint();
     }
     
     private void changeButtonAvailability(final boolean enable) {
         jxlFlurstuecksnachweis.setEnabled(enable);
         jxlNachweisNRW.setEnabled(enable);
         jxlNachweisKommunal.setEnabled(enable);
         jxlNachweisKommunalIntern.setEnabled(enable);
         jxlKarte.setEnabled(enable);
     }
     
     private void downloadEinzelnachweisProduct(String downloadTitle, String product, String actionTag) {
         if(!ObjectRendererUtils.checkActionTag(actionTag)) {
             showNoProductPermissionWarning();
             return;
         }
         
         if(!DownloadManagerDialog.showAskingForUserTitle(StaticSwingTools.getParentFrame(this))) {
             return;
         }
         final String jobname = DownloadManagerDialog.getJobname();
         
         List<SingleDownload> downloads = new LinkedList<SingleDownload>();
         
         for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
             if(!cidsBeanWrapper.isSelected()) {
                 continue;
             }
             
             final String parcelCode = AlkisUtils.getLandparcelCodeFromParcelBeanObject(cidsBeanWrapper.getCidsBean());
             URL url = null;
             
             if (parcelCode != null && parcelCode.length() > 0) {
                 try {
                     url = AlkisUtils.PRODUCTS.productEinzelNachweisUrl(parcelCode, product);
                     
                     if(url != null) {
                         downloads.add(new SingleDownload(url, "", jobname, downloadTitle, product, ".pdf"));
                     }
                 } catch (Exception ex) {
                     ObjectRendererUtils.showExceptionWindowToUser(
                         "Fehler beim Aufruf des Produkts: " + product,
                         ex,
                         AlkisLandparcelAggregationRenderer.this);
                     LOG.error("The URL to download product '" + product + "' (actionTag: " + actionTag + ") could not be constructed.", ex);
                 }
             }
         }
         
         if(downloads.size() > 1) {
             DownloadManager.instance().add(new MultipleDownload(downloads, jobname));
         } else if(downloads.size() == 1) {
             DownloadManager.instance().add(downloads.get(0));
         }
     }
     
     private void downloadKarteProduct(final String downloadTitle) {
         if (!ObjectRendererUtils.checkActionTag(PRODUCT_ACTION_TAG_KARTE)) {
             showNoProductPermissionWarning();
             return;
         }
         
         if(!DownloadManagerDialog.showAskingForUserTitle(StaticSwingTools.getParentFrame(this))) {
             return;
         }
         final String jobname = DownloadManagerDialog.getJobname();
         
         List<SingleDownload> downloads = new LinkedList<SingleDownload>();
 
         for (CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
             if(!cidsBeanWrapper.isSelected()) {
                 continue;
             }
             
             final String parcelCode = AlkisUtils.getLandparcelCodeFromParcelBeanObject(cidsBeanWrapper.getCidsBean());
             URL url = null;
             
             if (parcelCode.length() > 0) {
                 try {
                     url = AlkisUtils.PRODUCTS.productKarteUrl(parcelCode);
                 } catch (MalformedURLException ex) {
                     ObjectRendererUtils.showExceptionWindowToUser(
                             "Fehler beim Aufruf des Produkts: Kartenprodukt",
                             ex,
                             AlkisLandparcelAggregationRenderer.this);
                     LOG.error(ex);
                 }
             }
 
             if (url != null) {
                 downloads.add(new SingleDownload(url, "", jobname, downloadTitle, parcelCode.replace('/', '_'), ".pdf"));
             }
         }
         
         if(downloads.size() > 1) {
             DownloadManager.instance().add(new MultipleDownload(downloads, jobname));
         } else if(downloads.size() == 1) {
             DownloadManager.instance().add(downloads.get(0));
         }
     }
     
     private void showNoProductPermissionWarning() {
         JOptionPane.showMessageDialog(this, "Sie besitzen keine Berechtigung zur Erzeugung dieses Produkts!");
     }
     
     private class LandparcelTableModel extends AbstractTableModel {
         private int selectedCidsBeans = 0;
         
         @Override
         public int getRowCount() {
             if(cidsBeanWrappers == null) {
                 return 0;
             }
             
             return cidsBeanWrappers.size();
         }
 
         @Override
         public int getColumnCount() {
             if(cidsBeanWrappers == null) {
                 return 0;
             }
             
             return 4;
         }
 
         @Override
         public Class<?> getColumnClass(int columnIndex) {
             if(columnIndex == 0) {
                 return Boolean.class;
             } else if(columnIndex == 3) {
                 return Color.class;
             } else {
                 return String.class;
             }
         }
 
         @Override
         public String getColumnName(int column) {
             return NbBundle.getMessage(AlkisLandparcelAggregationRenderer.class, "AlkisLandparcelAggregationRenderer.LandparcelTableModel.getColumnName(" + column + ")");
         }
         
         @Override
         public Object getValueAt(int rowIndex, int columnIndex) {
             if(cidsBeanWrappers == null) {
                 return null;
             }
             
             CidsBeanWrapper cidsBeanWrapper = cidsBeanWrappers.get(rowIndex);
             if(columnIndex == 0) {
                 return cidsBeanWrapper.isSelected();
             } else if(columnIndex == 1) {
                 return cidsBeanWrapper.getGemarkung();
             } else if (columnIndex == 2) {
                 return cidsBeanWrapper.getBezeichnung();
             } else {
                 return cidsBeanWrapper.getColor();
             }
         }
         
         @Override
         public boolean isCellEditable(int row, int column) {
             return column == 0;
         }
         
         public void setCidsBeans(Collection<CidsBeanWrapper> cidsBeans) {
             if(cidsBeans != null) {
                 selectedCidsBeans = cidsBeanWrappers.size();
                 fireTableStructureChanged();
             }
         }
 
         @Override
         public void setValueAt(Object value, int row, int column) {
             if(column != 0) {
                 return;
             }
             
             CidsBeanWrapper cidsBeanWrapper = cidsBeanWrappers.get(row);
             cidsBeanWrapper.setSelected(!cidsBeanWrapper.isSelected());
             if(cidsBeanWrapper.isSelected()) {
                 selectedCidsBeans++;
             } else {
                 selectedCidsBeans--;
             }
             
             fireTableRowsUpdated(row, row);
             changeMap();
             changeButtonAvailability(selectedCidsBeans > 0);
         }
         
         public CidsBeanWrapper get(int index) {
             return cidsBeanWrappers.get(index);
         }
     }
     
     private class InitialiseMapRunnable implements Runnable {
         @Override
         public void run() {
             initialisedMap = false;
             
             final ActiveLayerModel mappingModel = new ActiveLayerModel();
             mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
             mappingModel.addHome(getBoundingBox());
             
             final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisConstants.COMMONS.MAP_CALL_STRING));
             swms.setName("Flurstueck");
 
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
             
             for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
                 map.getFeatureCollection().addFeature(cidsBeanWrapper.getFeature());
             }
             
             map.setAnimationDuration(duration);
             
             initialisedMap = true;
         }
         
         private XBoundingBox getBoundingBox() {
             XBoundingBox result = null;
             for(CidsBeanWrapper cidsBeanWrapper : cidsBeanWrappers) {
                 Geometry geometry = cidsBeanWrapper.getGeometry();
                 
                 if(result == null) {
                     result = new XBoundingBox(geometry.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
                     result.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                     result.setMetric(true);
                 } else {
                     XBoundingBox temp = new XBoundingBox(geometry.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
                     temp.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                     temp.setMetric(true);
                     
                     if(temp.getX1() < result.getX1()) {
                         result.setX1(temp.getX1());
                     }
                     if(temp.getY1() < result.getY1()) {
                         result.setY1(temp.getY1());
                     }
                     if(temp.getX2() > result.getX2()) {
                         result.setX2(temp.getX2());
                     }
                     if(temp.getY2() > result.getY2()) {
                         result.setY2(temp.getY2());
                     }
                 }
             }
             
             return result;
         }
     }
     
     private class ChangeMapRunnable implements Runnable {
         @Override
         public void run() {
             final XBoundingBox boxToGoto = new XBoundingBox(selectedCidsBeanWrapper.getGeometry().getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
             boxToGoto.setX1(boxToGoto.getX1() - AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth());
             boxToGoto.setX2(boxToGoto.getX2() + AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getWidth());
             boxToGoto.setY1(boxToGoto.getY1() - AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight());
             boxToGoto.setY2(boxToGoto.getY2() + AlkisConstants.COMMONS.GEO_BUFFER_MULTIPLIER * boxToGoto.getHeight());
             map.gotoBoundingBox(boxToGoto, false, true, 500);
         }
     }
     
     private class ColorRenderer extends JLabel implements TableCellRenderer {
         public ColorRenderer() {
             setOpaque(true);
         }
 
         @Override
         public Component getTableCellRendererComponent(
                 JTable table, Object color,
                 boolean isSelected, boolean hasFocus,
                 int row, int column) {
             
             Color newColor = (Color) color;
             setBackground(newColor);
             
             return this;
         }
     }
     
     private class CidsBeanWrapper implements Comparable<CidsBeanWrapper> {
         private CidsBean cidsBean;
         private boolean selected;
         private Color color;
         private String gemarkung;
         private String bezeichnung;
         private Geometry geometry;
         private StyledFeature feature;
         
         public CidsBeanWrapper(final CidsBean cidsBean, final boolean selected) {
             this.cidsBean = cidsBean;
             this.selected = selected;
             this.gemarkung = cidsBean.getProperty("gemarkung").toString();
             this.bezeichnung = cidsBean.getProperty("bezeichnung").toString();
             if(cidsBean.getProperty("geometrie.geo_field") instanceof Geometry) {
                 this.geometry = CrsTransformer.transformToGivenCrs((Geometry) cidsBean.getProperty("geometrie.geo_field"), AlkisConstants.COMMONS.SRS_SERVICE);
             }
             
             final StyledFeature dsf = new DefaultStyledFeature();
             dsf.setGeometry(this.geometry);
             
             this.feature = dsf;
         }
         
         public CidsBean getCidsBean() {
             return cidsBean;
         }
 
         public Color getColor() {
             return color;
         }
 
         public void setColor(Color color) {
             this.color = color;
             feature.setFillingPaint(this.color);
         }
 
         public boolean isSelected() {
             return selected;
         }
 
         public void setSelected(boolean selected) {
             this.selected = selected;
         }
         
         public String getGemarkung() {
             return gemarkung;
         }
         
         public String getBezeichnung() {
             return bezeichnung;
         }
         
         public Geometry getGeometry() {
             return geometry;
         }
 
         public StyledFeature getFeature() {
             return feature;
         }
         
         @Override
         public int compareTo(CidsBeanWrapper o) {
             final CidsBean cidsBean1 = cidsBean;
             final CidsBean cidsBean2 = o.cidsBean;
             
             if(cidsBean1 == null && cidsBean2 == null) {
                 return 0;
             } else if(cidsBean1 == null) {
                 return -1;
             } else if(cidsBean2 == null) {
                 return 1;
             }
             
             int districtComparison = cidsBean1.getProperty("gemarkung").toString().compareTo(cidsBean2.getProperty("gemarkung").toString());
             
             if(districtComparison != 0) {
                 return districtComparison;
             } else {
                 return cidsBean1.getProperty("bezeichnung").toString().compareTo(cidsBean2.getProperty("bezeichnung").toString());
             }
         }
     }
 }
