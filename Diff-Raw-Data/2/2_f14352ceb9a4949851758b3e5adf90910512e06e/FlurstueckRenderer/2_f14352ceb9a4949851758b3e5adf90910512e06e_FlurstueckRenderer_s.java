 /*
  *  Copyright (C) 2011 thorsten
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * FlurstueckRenderer.java
  *
  * Created on 11.08.2011, 14:48:28
  */
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.method.MethodManager;
 
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import com.vividsolutions.jts.geom.Geometry;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.wunda_blau.res.StaticProperties;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsAlkisSearchStatement;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
 import de.cismet.cismap.commons.features.StyledFeature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 import de.cismet.tools.BrowserLauncher;
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.TitleComponentProvider;
 import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
 import edu.umd.cs.piccolo.event.PInputEvent;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 /**
  *
  * @author thorsten
  */
 public class FlurstueckRenderer extends javax.swing.JPanel implements BorderProvider,
         CidsBeanRenderer,
         TitleComponentProvider, FooterComponentProvider {
 
     private CidsBean cidsBean;
     private String title;
     private final MappingComponent map;
     private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
 
     /** Creates new form FlurstueckRenderer */
     public FlurstueckRenderer() {
         initComponents();
         map = new MappingComponent();
         panFlurstueckMap.add(map, BorderLayout.CENTER);
         jXHyperlink1.setVisible(false);
     }
 
     @Override
     public void setCidsBean(final CidsBean cb) {
         bindingGroup.unbind();
         if (cb != null) {
             this.cidsBean = cb;
             initMap();
             bindingGroup.bind();
             String z = String.valueOf(cb.getProperty("fstnr_z"));
             String n = String.valueOf(cb.getProperty("fstnr_n"));
 
             String result = z;
             if (n != null && !n.trim().equals("0") && !(n.trim().length() == 0)) {
                 result += "/" + n;
             }
             lblFlurstueck.setText(result);
 
             String fnr = String.valueOf(cidsBean.getProperty("fortfuehrungsnummer"));
             if (cidsBean.getProperty("fortfuehrungsnummer") != null && fnr != null && fnr.trim().length() > 0) {
                 try {
                     String f = fnr.substring(0, 6) + "-" + fnr.substring(6, fnr.length());
                     jxhFortfuehrungsnummer.setText(f);
                 } catch (Exception e) {
                     log.warn("fnr problem: " + fnr, e);
                     jxhFortfuehrungsnummer.setText(fnr);
                 }
 
             }
             title = "Flurstück " + lblFlurstueck.getText();
             lblTitle.setText(this.title);
 
 
         }
 
         if (lblHist.getText().trim().equals("----")) {
             jXHyperlink1.setVisible(true);
         }
     }
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
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
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         panTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         panFooter = new javax.swing.JPanel();
         jXHyperlink1 = new org.jdesktop.swingx.JXHyperlink();
         panDescription = new javax.swing.JPanel();
         panMainInfo = new RoundedPanel();
         lblFlurstueck = new javax.swing.JLabel();
         lblDescFlurstueck = new javax.swing.JLabel();
         lblDescFlur = new javax.swing.JLabel();
         lblFlur = new javax.swing.JLabel();
         lblDescGemarkung = new javax.swing.JLabel();
         lblGemarkung = new javax.swing.JLabel();
         lblDescHist = new javax.swing.JLabel();
         lblDescFortfuehrungsnummer = new javax.swing.JLabel();
         semiRoundedPanel2 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel6 = new javax.swing.JLabel();
         lblHist = new javax.swing.JLabel();
         jxhFortfuehrungsnummer = new org.jdesktop.swingx.JXHyperlink();
         panFlurstueckMap = new javax.swing.JPanel();
 
         panTitle.setOpaque(false);
         panTitle.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.lblTitle.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panTitle.add(lblTitle, gridBagConstraints);
 
         panFooter.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
         panFooter.setOpaque(false);
         panFooter.setLayout(new java.awt.GridBagLayout());
 
         jXHyperlink1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
         jXHyperlink1.setForeground(new java.awt.Color(204, 204, 204));
         jXHyperlink1.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.jXHyperlink1.text")); // NOI18N
         jXHyperlink1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jXHyperlink1ActionPerformed(evt);
             }
         });
         panFooter.add(jXHyperlink1, new java.awt.GridBagConstraints());
 
         setLayout(new java.awt.BorderLayout());
 
         panDescription.setOpaque(false);
         panDescription.setLayout(new java.awt.GridBagLayout());
 
         panMainInfo.setLayout(new java.awt.GridBagLayout());
 
         lblFlurstueck.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.lblFlurstueck.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 10);
         panMainInfo.add(lblFlurstueck, gridBagConstraints);
 
         lblDescFlurstueck.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescFlurstueck.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.lblDescFlurstueck.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescFlurstueck, gridBagConstraints);
 
         lblDescFlur.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescFlur.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.lblDescFlur.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescFlur, gridBagConstraints);
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.flur}"), lblFlur, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("----");
         binding.setSourceUnreadableValue("----");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 10);
         panMainInfo.add(lblFlur, gridBagConstraints);
 
         lblDescGemarkung.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescGemarkung.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.lblDescGemarkung.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         panMainInfo.add(lblDescGemarkung, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.gemarkungs_nr.name}"), lblGemarkung, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("----");
         binding.setSourceUnreadableValue("----");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 6, 5, 10);
         panMainInfo.add(lblGemarkung, gridBagConstraints);
 
         lblDescHist.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescHist.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.lblDescHist.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescHist, gridBagConstraints);
 
         lblDescFortfuehrungsnummer.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblDescFortfuehrungsnummer.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.lblDescFortfuehrungsnummer.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         panMainInfo.add(lblDescFortfuehrungsnummer, gridBagConstraints);
 
         semiRoundedPanel2.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel2.setLayout(new java.awt.GridBagLayout());
 
         jLabel6.setText(org.openide.util.NbBundle.getMessage(FlurstueckRenderer.class, "FlurstueckRenderer.jLabel6.text")); // NOI18N
         jLabel6.setForeground(new java.awt.Color(255, 255, 255));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel2.add(jLabel6, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panMainInfo.add(semiRoundedPanel2, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.historisch}"), lblHist, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("----");
         binding.setSourceUnreadableValue("----");
         binding.setConverter(new de.cismet.cids.custom.objectrenderer.converter.SQLDateToStringConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 10);
         panMainInfo.add(lblHist, gridBagConstraints);
 
         jxhFortfuehrungsnummer.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jxhFortfuehrungsnummerActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 30);
         panMainInfo.add(jxhFortfuehrungsnummer, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weighty = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
         panDescription.add(panMainInfo, gridBagConstraints);
 
         panFlurstueckMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         panFlurstueckMap.setLayout(new java.awt.BorderLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
         panDescription.add(panFlurstueckMap, gridBagConstraints);
 
         add(panDescription, java.awt.BorderLayout.CENTER);
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jxhFortfuehrungsnummerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxhFortfuehrungsnummerActionPerformed
         String fnr = String.valueOf(cidsBean.getProperty("fortfuehrungsnummer"));
         if (cidsBean.getProperty("fortfuehrungsnummer") != null && fnr != null && fnr.trim().length() > 0) {
             try {
                 java.sql.Date d = (java.sql.Date) cidsBean.getProperty("historisch");
                 SimpleDateFormat formater = new SimpleDateFormat("yyyy");
                 String year = formater.format(d);
                 String laufendeNr = fnr.substring(6, fnr.length());
                 String documentName = "FN_" + year + "_" + cidsBean.getProperty("gemarkungs_nr.gemarkungsnummer") + "_" + laufendeNr;
                 String prefix = StaticProperties.FORTFUEHRUNGSNACHWEISE_URL_PREFIX;
                 if (prefix == null) {
                     prefix = "file://///S102gs/_102-alkis-dokumente/Echtfortführungen/Fortführungsnachweise/";
                 }
                 BrowserLauncher.openURLorFile(prefix + documentName + ".pdf");
 //                
 //                URL url = new URL( prefix + documentName+".pdf");
 //                final SingleDownload download = new SingleDownload(url, "", DownloadManagerDialog.getJobname(), "Fortfuehrungsnachweis: " + documentName, "fortfuehrungsnachweis_" + documentName, "pdf");
 //                DownloadManager.instance().add(download);
             } catch (Exception e) {
                 log.error("Hier muss noch ne Messagebox hin", e);
             }
         }
 
     }//GEN-LAST:event_jxhFortfuehrungsnummerActionPerformed
 
 private void jXHyperlink1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXHyperlink1ActionPerformed
     String z = String.valueOf(cidsBean.getProperty("fstnr_z"));
     String n = String.valueOf(cidsBean.getProperty("fstnr_n"));
     
     String zf=String.format("%05d", new Integer(z));
     String nf=String.format("%04d", new Integer(n));
     String f=zf;
     if (!nf.equals("0000")){
         f=f+"/"+nf;
     }
     
     final String fString = "05" + cidsBean.getProperty("gemarkungs_nr.gemarkungsnummer") + "-" + lblFlur.getText() + "-" + f;
     log.fatal(fString);
     final CidsAlkisSearchStatement stmnt = new CidsAlkisSearchStatement(CidsAlkisSearchStatement.Resulttyp.FLURSTUECK, CidsAlkisSearchStatement.SucheUeber.FLURSTUECKSNUMMER, fString, null);
 
 
     
 
     final SwingWorker<Collection<Node>, Void> searchWorker;
     searchWorker = new SwingWorker<Collection<Node>, Void>() {
 
         @Override
         protected Collection<Node> doInBackground() throws Exception {
             return SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), stmnt);
         }
 
         @Override
         protected void done() {
             try {
                 if (!isCancelled()) {
                     Collection<Node> nodes = get();
                     for (Node n : nodes) {
 
                         MetaObjectNode mon=(MetaObjectNode)n;
                         
                         final String tabname = "alkis_landparcel";
                         final MetaClass mc = ClassCacheMultiple.getMetaClass(mon.getDomain(), mon.getClassId());
                         if (mc != null) {
                             
                            ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(mc, mon.getObjectId(), "");
                         } else {
                             log.error("Could not find MetaClass for " + tabname);
                         }
                     }
 
                 }
             } catch (InterruptedException ex) {
                 log.warn(ex, ex);
             } catch (Exception ex) {
                 log.error(ex, ex);
             } finally {
 //                    lblBusy.setBusy(false);
 //                    btnAbort.setEnabled(false);
 //                    btnSearch.setEnabled(true);
             }
         }
     };
     CismetThreadPool.execute(searchWorker);
 
 
 
 }//GEN-LAST:event_jXHyperlink1ActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel jLabel6;
     private org.jdesktop.swingx.JXHyperlink jXHyperlink1;
     private org.jdesktop.swingx.JXHyperlink jxhFortfuehrungsnummer;
     private javax.swing.JLabel lblDescFlur;
     private javax.swing.JLabel lblDescFlurstueck;
     private javax.swing.JLabel lblDescFortfuehrungsnummer;
     private javax.swing.JLabel lblDescGemarkung;
     private javax.swing.JLabel lblDescHist;
     private javax.swing.JLabel lblFlur;
     private javax.swing.JLabel lblFlurstueck;
     private javax.swing.JLabel lblGemarkung;
     private javax.swing.JLabel lblHist;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JPanel panDescription;
     private javax.swing.JPanel panFlurstueckMap;
     private javax.swing.JPanel panFooter;
     private javax.swing.JPanel panMainInfo;
     private javax.swing.JPanel panTitle;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel2;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     /**
      * DOCUMENT ME!
      */
     private void initMap() {
         final Object geoObj = cidsBean.getProperty("umschreibendes_rechteck.geo_field");
         if (geoObj instanceof Geometry) {
             final Geometry pureGeom = CrsTransformer.transformToGivenCrs((Geometry) geoObj, AlkisConstants.COMMONS.SRS_SERVICE);
             final BoundingBox box = new BoundingBox(pureGeom.getEnvelope().buffer(AlkisConstants.COMMONS.GEO_BUFFER));
 
             final Runnable mapRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     final ActiveLayerModel mappingModel = new ActiveLayerModel();
                     mappingModel.setSrs(AlkisConstants.COMMONS.SRS_SERVICE);
                     mappingModel.addHome(new XBoundingBox(
                             box.getX1(),
                             box.getY1(),
                             box.getX2(),
                             box.getY2(),
                             AlkisConstants.COMMONS.SRS_SERVICE,
                             true));
                     final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(AlkisConstants.COMMONS.MAP_CALL_STRING));
                     swms.setName("Flurstueck");
                     final StyledFeature dsf = new DefaultStyledFeature();
                     dsf.setGeometry(pureGeom);
                     dsf.setFillingPaint(new Color(1, 0, 0, 0.5f));
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
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public void setTitle(String title) {
 //        if (title == null) {
 //            title = "<Error>";
 //        } else {
 //            this.title = title;
 //        }
 //        this.title = title;
 //        lblTitle.setText(this.title);
     }
 
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
 
     @Override
     public void dispose() {
         bindingGroup.unbind();
 //        if (!continueInBackground) {
 //            AlkisSOAPWorkerService.cancel(retrieveBuchungsblaetterWorker);
 //            setWaiting(false);
 //        }
         map.dispose();
     }
 
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
     }
 
     @Override
     public JComponent getFooterComponent() {
         return panFooter;
     }
 }
