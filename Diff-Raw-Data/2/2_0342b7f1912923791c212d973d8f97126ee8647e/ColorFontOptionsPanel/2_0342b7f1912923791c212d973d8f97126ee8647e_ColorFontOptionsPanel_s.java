 /*
  * Copyright (c) 2006-2007, AIOTrade Computing Co. and Contributors
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  *  o Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer. 
  *    
  *  o Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution. 
  *    
  *  o Neither the name of AIOTrade Computing Co. nor the names of 
  *    its contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  *    
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.aiotrade.platform.core.ui.netbeans.options.colors;
 
 import java.awt.BorderLayout;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Collections;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import org.aiotrade.lib.charting.chart.QuoteChart;
 import org.aiotrade.lib.charting.laf.CityLights;
 import org.aiotrade.lib.charting.laf.Gray;
 import org.aiotrade.lib.charting.laf.LookFeel;
 import org.aiotrade.lib.charting.laf.Modern;
 import org.aiotrade.lib.charting.laf.White;
 import org.aiotrade.lib.charting.view.ChartingController;
 import org.aiotrade.lib.charting.view.ChartingControllerFactory;
 import org.aiotrade.lib.charting.view.WithQuoteChart;
 import org.aiotrade.lib.math.PersistenceManager;
 import org.aiotrade.lib.math.timeseries.descriptor.AnalysisContents;
 import org.aiotrade.platform.core.analysis.chartview.AnalysisChartViewContainer;
 import org.aiotrade.platform.core.dataserver.QuoteContract;
 import org.aiotrade.platform.core.dataserver.QuoteServer;
 import org.aiotrade.platform.core.sec.Stock;
 import org.openide.ErrorManager;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 /**
  *
  * @author Caoyuan Deng
  */
 public class ColorFontOptionsPanel extends javax.swing.JPanel {
     
     /** Creates new Panel */
     public ColorFontOptionsPanel() {
         initComponents();
         
         ComboBoxModel lafModel = new DefaultComboBoxModel(new String[] {
             "City Lights",
             "Gray",
             "Modern",
             "White"
         });
         lafBox.setModel(lafModel);
         
         ComboBoxModel quoteChartTypeModel = new DefaultComboBoxModel(QuoteChart.Type.values());
         quoteChartTypeBox.setModel(quoteChartTypeModel);
     }
     
     
     private AnalysisChartViewContainer previewContainer;
     private boolean previewPanelInited = false;
     public void initPreviewPanel() {
         String symbol = "Preview";
         
         QuoteContract quoteContract = new QuoteContract();
         QuoteServer previewQuoteServer = null;
         Collection<QuoteServer> quoteServers = PersistenceManager.getDefault().lookupAllRegisteredServices(QuoteServer.class, QuoteContract.getFolderName());
         for (QuoteServer quoteServer : quoteServers) {
             if (quoteServer.getDisplayName().toUpperCase().contains("CSV ASCII FILE")) {
                 previewQuoteServer = quoteServer;
                 break;
             }
         }
         if (previewQuoteServer == null) {
             return;
         }
         quoteContract.setActive(true);
         quoteContract.setServiceClassName(previewQuoteServer.getClass().getName());
         quoteContract.setSymbol(symbol);
         quoteContract.setDateFormatString(previewQuoteServer.getDefaultDateFormatString());
         
         FileObject previewFile = FileUtil.getConfigFile("UserOptions/Template/preview.csv");
         if (previewFile != null) {
             try {
                 InputStream is = previewFile.getInputStream();
                 quoteContract.setInputStream(is);
             } catch (Exception ex) {
                 ErrorManager.getDefault().notify(ex);
             }
         }
         
         AnalysisContents contents = PersistenceManager.getDefault().getDefaultContents();
         contents.addDescriptor(quoteContract);
         
         Stock stock = new Stock(symbol, Collections.singleton(quoteContract));
        
         if (!stock.isSerLoaded(quoteContract.getFreq())) {
             stock.loadSer(quoteContract.getFreq());
         }
         
         ChartingController controller = ChartingControllerFactory.createInstance(
                 stock.getSer(quoteContract.getFreq()), contents);
         previewContainer = controller.createChartViewContainer(
                 AnalysisChartViewContainer.class, this);
         
         previewPanel.setLayout(new BorderLayout());
         previewPanel.add(previewContainer, BorderLayout.CENTER);
         
         previewPanelInited = true;
     }
     
     private void refreshPreviewPanel() {
         if (previewPanelInited) {
             String lafStr = (String)lafBox.getSelectedItem();
             LookFeel laf = null;
             if (lafStr.equalsIgnoreCase("White")) {
                 laf = new White();
             } else if (lafStr.equalsIgnoreCase("City Lights")) {
                 laf = new CityLights();
             } else if (lafStr.equalsIgnoreCase("Modern")) {
                 laf = new Modern();
             } else {
                 laf = new Gray();
             }
             LookFeel.setCurrent(laf);
             
             boolean reversedPositiveNegativeColor = reverseColorBox.isSelected();
             LookFeel.getCurrent().setPositiveNegativeColorReversed(reversedPositiveNegativeColor);
             
             boolean thinVolume = thinVolumeBox.isSelected();
             LookFeel.getCurrent().setThinVolumeBar(thinVolume);
             
             QuoteChart.Type style = (QuoteChart.Type)quoteChartTypeBox.getSelectedItem();
             LookFeel.getCurrent().setQuoteChartType(style);
             
             boolean antiAlias = antiAliasBox.isSelected();
             LookFeel.getCurrent().setAntiAlias(antiAlias);
             
             boolean autoHideScroll = autoHideScrollBox.isSelected();
             LookFeel.getCurrent().setAutoHideScroll(autoHideScroll);
             
             ((WithQuoteChart)previewContainer.getMasterView()).switchQuoteChartType(style);
             
             previewContainer.repaint();
         }
     }
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         reverseColorBox = new javax.swing.JCheckBox();
         lafBox = new javax.swing.JComboBox();
         jLabel2 = new javax.swing.JLabel();
         quoteChartTypeBox = new javax.swing.JComboBox();
         previewPanel = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         antiAliasBox = new javax.swing.JCheckBox();
         autoHideScrollBox = new javax.swing.JCheckBox();
         thinVolumeBox = new javax.swing.JCheckBox();
 
         jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
         jLabel1.setText("Look And Feel:");
 
         reverseColorBox.setFont(new java.awt.Font("Dialog", 0, 11));
         reverseColorBox.setText("Reverse Positive / Negative Color");
         reverseColorBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         reverseColorBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
         reverseColorBox.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 reverseColorBoxStateChanged(evt);
             }
         });
 
         lafBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 lafBoxItemStateChanged(evt);
             }
         });
 
         jLabel2.setFont(new java.awt.Font("Dialog", 0, 11));
         jLabel2.setText("Default Quote Chart Type:");
 
         quoteChartTypeBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 quoteChartTypeBoxItemStateChanged(evt);
             }
         });
 
         previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(" Preview "));
         previewPanel.setFont(new java.awt.Font("Dialog", 0, 11));
 
         org.jdesktop.layout.GroupLayout previewPanelLayout = new org.jdesktop.layout.GroupLayout(previewPanel);
         previewPanel.setLayout(previewPanelLayout);
         previewPanelLayout.setHorizontalGroup(
             previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 530, Short.MAX_VALUE)
         );
         previewPanelLayout.setVerticalGroup(
             previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 224, Short.MAX_VALUE)
         );
 
         jLabel3.setFont(new java.awt.Font("Dialog", 0, 11));
         jLabel3.setText("Notice: You may need to re-open the views to take the full effect of new theme.");
 
         antiAliasBox.setFont(new java.awt.Font("Dialog", 0, 11));
         antiAliasBox.setText("Anti Alias");
         antiAliasBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         antiAliasBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
         antiAliasBox.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 antiAliasBoxStateChanged(evt);
             }
         });
 
         autoHideScrollBox.setFont(new java.awt.Font("Dialog", 0, 11));
         autoHideScrollBox.setText("Auto hide scroll control");
         autoHideScrollBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         autoHideScrollBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
         autoHideScrollBox.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 autoHideScrollBoxStateChanged(evt);
             }
         });
 
         thinVolumeBox.setFont(new java.awt.Font("Dialog", 0, 11));
         thinVolumeBox.setText("Thin Valume Bar");
         thinVolumeBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         thinVolumeBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
         thinVolumeBox.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 thinVolumeBoxStateChanged(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                             .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                 .add(jLabel1)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 13, Short.MAX_VALUE)
                                 .add(lafBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                             .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(jLabel2)
                                     .add(antiAliasBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(quoteChartTypeBox, 0, 147, Short.MAX_VALUE)
                                     .add(autoHideScrollBox))))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(thinVolumeBox)
                             .add(reverseColorBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)))
                     .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(reverseColorBox)
                     .add(lafBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel2)
                     .add(thinVolumeBox)
                     .add(quoteChartTypeBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(antiAliasBox)
                     .add(autoHideScrollBox))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel3)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void autoHideScrollBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoHideScrollBoxStateChanged
         refreshPreviewPanel();
     }//GEN-LAST:event_autoHideScrollBoxStateChanged
 
     private void thinVolumeBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thinVolumeBoxStateChanged
         refreshPreviewPanel();
     }//GEN-LAST:event_thinVolumeBoxStateChanged
     
     private void antiAliasBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_antiAliasBoxStateChanged
         refreshPreviewPanel();
     }//GEN-LAST:event_antiAliasBoxStateChanged
     
     private void reverseColorBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_reverseColorBoxStateChanged
         refreshPreviewPanel();
     }//GEN-LAST:event_reverseColorBoxStateChanged
     
     private void quoteChartTypeBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_quoteChartTypeBoxItemStateChanged
         refreshPreviewPanel();
     }//GEN-LAST:event_quoteChartTypeBoxItemStateChanged
     
     private void lafBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lafBoxItemStateChanged
         refreshPreviewPanel();
     }//GEN-LAST:event_lafBoxItemStateChanged
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     protected javax.swing.JCheckBox antiAliasBox;
     public javax.swing.JCheckBox autoHideScrollBox;
     protected javax.swing.JLabel jLabel1;
     protected javax.swing.JLabel jLabel2;
     protected javax.swing.JLabel jLabel3;
     protected javax.swing.JComboBox lafBox;
     protected javax.swing.JPanel previewPanel;
     protected javax.swing.JComboBox quoteChartTypeBox;
     protected javax.swing.JCheckBox reverseColorBox;
     public javax.swing.JCheckBox thinVolumeBox;
     // End of variables declaration//GEN-END:variables
     
 }
