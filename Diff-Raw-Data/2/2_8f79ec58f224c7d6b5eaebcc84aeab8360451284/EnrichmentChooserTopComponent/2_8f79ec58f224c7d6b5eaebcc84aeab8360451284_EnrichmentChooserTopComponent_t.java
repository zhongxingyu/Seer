 /*
  * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
  * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
  * 
  * MONGKIE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MONGKIE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mongkie.ui.enrichment;
 
 import java.awt.BorderLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.SwingUtilities;
 import static kobic.prefuse.Constants.NODES;
 import kobic.prefuse.display.DisplayListener;
 import org.mongkie.enrichment.EnrichmentController;
 import org.mongkie.enrichment.EnrichmentModel;
 import org.mongkie.enrichment.EnrichmentModelListener;
 import org.mongkie.enrichment.spi.Enrichment;
 import org.mongkie.enrichment.spi.EnrichmentBuilder;
 import static org.mongkie.visualization.Config.MODE_ACTION;
 import static org.mongkie.visualization.Config.ROLE_NETWORK;
 import org.mongkie.visualization.MongkieDisplay;
 import org.mongkie.visualization.workspace.ModelChangeListener;
 import org.netbeans.api.settings.ConvertAsProperties;
 import org.openide.DialogDisplayer;
 import org.openide.NotifyDescriptor;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.util.ImageUtilities;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.windows.TopComponent;
 import prefuse.Visualization;
 import prefuse.data.Graph;
 import prefuse.data.Table;
 import prefuse.data.Tuple;
 import static prefuse.data.event.EventConstants.*;
 import prefuse.data.event.TableListener;
 
 /**
  *
  * @author Yeongjun Jang <yjjang@kribb.re.kr>
  */
 @ConvertAsProperties(dtd = "-//org.mongkie.ui.enrichment//EnrichmentChooser//EN",
 autostore = false)
 @TopComponent.Description(preferredID = "EnrichmentChooserTopComponent",
 iconBase = "org/mongkie/ui/enrichment/resources/enrichment.png",
 persistenceType = TopComponent.PERSISTENCE_ALWAYS)
 @TopComponent.Registration(mode = MODE_ACTION, openAtStartup = false, roles = ROLE_NETWORK, position = 400)
 @ActionID(category = "Window", id = "org.mongkie.ui.enrichment.EnrichmentChooserTopComponent")
 @ActionReference(path = "Menu/Window", position = 70)
 @TopComponent.OpenActionRegistration(displayName = "#CTL_EnrichmentChooserAction",
 preferredID = "EnrichmentChooserTopComponent")
 public final class EnrichmentChooserTopComponent extends TopComponent
         implements EnrichmentModelListener, DisplayListener<MongkieDisplay>, TableListener {
 
     private static final String NO_SELECTION =
             NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.choose.displayText");
     private EnrichmentModel model;
 
     public EnrichmentChooserTopComponent() {
         initComponents();
         settingsSeparator.setVisible(false);
 
         setName(NbBundle.getMessage(EnrichmentChooserTopComponent.class, "CTL_EnrichmentChooserTopComponent"));
         setToolTipText(NbBundle.getMessage(EnrichmentChooserTopComponent.class, "HINT_EnrichmentChooserTopComponent"));
         putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
 
         initializeChooser();
         addEventListeners();
 
         Lookup.getDefault().lookup(EnrichmentController.class).fireModelChangeEvent();
     }
 
     private void initializeChooser() {
         DefaultComboBoxModel enrichmentComboBoxModel = new DefaultComboBoxModel();
         enrichmentComboBoxModel.addElement(NO_SELECTION);
         enrichmentComboBoxModel.setSelectedItem(NO_SELECTION);
         for (EnrichmentBuilder builder : Lookup.getDefault().lookupAll(EnrichmentBuilder.class)) {
             enrichmentComboBoxModel.addElement(builder);
         }
         enrichmentComboBox.setModel(enrichmentComboBoxModel);
     }
 
     private void addEventListeners() {
         Lookup.getDefault().lookup(EnrichmentController.class).addModelChangeListener(new ModelChangeListener<EnrichmentModel>() {
             @Override
             public void modelChanged(EnrichmentModel o, EnrichmentModel n) {
                 if (o != null) {
                     o.removeModelListener(EnrichmentChooserTopComponent.this);
                     o.getDisplay().removeDisplayListener(EnrichmentChooserTopComponent.this);
                 }
                 model = n;
                 if (model != null) {
                     model.addModelListener(EnrichmentChooserTopComponent.this);
                     model.getDisplay().addDisplayListener(EnrichmentChooserTopComponent.this);
                 }
                 refreshModel();
             }
         });
         enrichmentComboBox.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (enrichmentComboBox.getSelectedItem().equals(NO_SELECTION)) {
                     if (model != null) {
                         setSelectedEnrichment(null);
                     }
                     settingsSeparator.setVisible(false);
                     settingsPanel.removeAll();
                     settingsPanel.revalidate();
                     settingsPanel.repaint();
                 } else if (enrichmentComboBox.getSelectedItem() instanceof EnrichmentBuilder) {
                     EnrichmentBuilder builder = (EnrichmentBuilder) enrichmentComboBox.getSelectedItem();
                     setSelectedEnrichment(builder);
                     settingsPanel.removeAll();
                     EnrichmentBuilder.SettingUI settings = builder.getSettingUI();
                     if (settings != null) {
                         settingsSeparator.setVisible(true);
                         settings.load(builder.getEnrichment());
                         settingsPanel.add(settings.getPanel(), BorderLayout.CENTER);
                     } else {
                         settingsSeparator.setVisible(false);
                     }
                     settingsPanel.revalidate();
                     settingsPanel.repaint();
                 }
             }
         });
         geneIdColumnComboBox.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (model != null) {
                     Lookup.getDefault().lookup(EnrichmentController.class).setGeneIDColumn((String) e.getItem());
                 }
             }
         });
     }
 
     private void setSelectedEnrichment(EnrichmentBuilder builder) {
         if (model.get() != null && model.get().getBuilder() == builder) {
             return;
         }
         Lookup.getDefault().lookup(EnrichmentController.class).setEnrichment(builder != null ? builder.getEnrichment() : null);
     }
 
     private void refreshModel() {
         refreshChooser();
         refreshGeneIdColumnComboBox();
         refreshEnabled();
         refreshResult();
     }
 
     @Override
     public void graphDisposing(MongkieDisplay d, Graph g) {
         g.getNodeTable().removeTableListener(this);
     }
 
     @Override
     public void graphChanged(MongkieDisplay d, Graph g) {
         if (g != null) {
             g.getNodeTable().addTableListener(this);
         }
         refreshGeneIdColumnComboBox();
     }
 
     @Override
     public void tableChanged(Table t, int start, int end, int col, int type) {
         if (col != ALL_COLUMNS && (type == INSERT || type == DELETE)) {
             refreshGeneIdColumnComboBox();
         }
     }
 
     private void refreshGeneIdColumnComboBox() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 geneIdColumnComboBox.removeAllItems();
                 if (model != null) {
                     Table nodeTable = model.getDisplay().getGraph().getNodeTable();
                     String geneIdCol = model.getGeneIDColumn();
                     for (int i = 0; i < nodeTable.getColumnCount(); i++) {
                         if (nodeTable.getColumn(i).canGetString()) {
                             geneIdColumnComboBox.addItem(nodeTable.getColumnName(i));
                         }
                     }
                     geneIdColumnComboBox.setSelectedItem(
                             model.get() == null || nodeTable.getColumnNumber(geneIdCol) < 0 ? null : geneIdCol);
                 }
             }
         });
     }
 
     private void refreshChooser() {
         Enrichment en = model != null ? model.get() : null;
         enrichmentComboBox.getModel().setSelectedItem(en != null ? en.getBuilder() : NO_SELECTION);
     }
 
     private void refreshEnabled() {
         if (model == null || !model.isRunning()) {
             runButton.setText(NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.runButton.text"));
             runButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/enrichment/resources/run.gif", false));
             runButton.setToolTipText(NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.runButton.toolTipText"));
         } else if (model.isRunning()) {
             runButton.setText(NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.cancelButton.text"));
             runButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/enrichment/resources/stop.png", false));
             runButton.setToolTipText(NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.cancelButton.toolTipText"));
         }
 
         boolean enabled = model != null && model.get() != null && model.getDisplay().isFired();
         runButton.setEnabled(enabled);
         infoLabel.setEnabled(enabled);
         wholeNetworkButton.setEnabled(enabled && !model.isRunning());
         fromSelectionButton.setEnabled(enabled && !model.isRunning());
         geneIdColumnComboBox.setEnabled(enabled && !model.isRunning());
         EnrichmentBuilder.SettingUI settings = enabled ? model.get().getBuilder().getSettingUI() : null;
         if (settings != null) {
             settings.setEnabled(!model.isRunning());
         }
 
         enrichmentComboBox.setEnabled(model != null && !model.isRunning() && model.getDisplay().isFired());
     }
 
     private void refreshResult() {
         EnrichmentResultTopComponent resultDisplayer = EnrichmentResultTopComponent.getInstance();
         if (model == null || model.getDisplay().getGraph().getNodeCount() < 1) {
             resultDisplayer.setResult(null);
         } else if (model.isRunning()) {
             resultDisplayer.setBusy(true);
         } else {
             resultDisplayer.setResult(model.getResult());
         }
     }
 
     private void run() {
         Enrichment en = model.get();
         EnrichmentBuilder.SettingUI settings = en.getBuilder().getSettingUI();
         if (settings != null) {
             settings.apply(en);
         }
 
         Object geneIdCol = model.getGeneIDColumn();
         if (geneIdCol == null) {
             DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Select a ID column of query genes.", NotifyDescriptor.ERROR_MESSAGE));
             return;
         }
 
         Lookup.getDefault().lookup(EnrichmentController.class).analyze(getGeneIdsFromSelectedColumn());
     }
 
     private String[] getGeneIdsFromSelectedColumn() {
         Set<String> genes = new HashSet<String>();
 //        for (Iterator<Tuple> nodeIter = model.getDisplay().getGraph().getNodeTable().tuples(); nodeIter.hasNext();) {
         for (Iterator<Tuple> nodeIter = fromSelectionButton.isSelected()
                 ? model.getDisplay().getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).tuples()
                 : model.getDisplay().getVisualization().items(NODES);
                 nodeIter.hasNext();) {
             String gene = nodeIter.next().getString(model.getGeneIDColumn());
             if (gene == null || (gene = gene.trim()).isEmpty()) {
                 continue;
             }
             genes.add(gene);
         }
         return genes.toArray(new String[genes.size()]);
     }
 
     private void cancel() {
         Lookup.getDefault().lookup(EnrichmentController.class).cancelAnalyzing();
     }
 
     /**
      * This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         selectionButtonGroup = new javax.swing.ButtonGroup();
         enrichmentComboBox = new javax.swing.JComboBox();
         infoLabel = new javax.swing.JLabel();
         selectionSeparator = new org.jdesktop.swingx.JXTitledSeparator();
         wholeNetworkButton = new javax.swing.JRadioButton();
         fromSelectionButton = new javax.swing.JRadioButton();
         settingsSeparator = new org.jdesktop.swingx.JXTitledSeparator();
         runButton = new javax.swing.JButton();
         settingsPanel = new javax.swing.JPanel();
         geneIdColumnLabel = new javax.swing.JLabel();
         geneIdColumnComboBox = new javax.swing.JComboBox();
         selectionButtonGroup.add(wholeNetworkButton);
         selectionButtonGroup.add(fromSelectionButton);
 
         setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 3, 2, 4));
 
         enrichmentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Gene Ontology", "Pathway (KoPath)" }));
 
         infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/enrichment/resources/information.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.infoLabel.text")); // NOI18N
 
         selectionSeparator.setEnabled(false);
         selectionSeparator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/enrichment/resources/light-bulb.png"))); // NOI18N
         selectionSeparator.setTitle(org.openide.util.NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.selectionSeparator.title")); // NOI18N
 
         wholeNetworkButton.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(wholeNetworkButton, org.openide.util.NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.wholeNetworkButton.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(fromSelectionButton, org.openide.util.NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.fromSelectionButton.text")); // NOI18N
 
         settingsSeparator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/enrichment/resources/settings.png"))); // NOI18N
         settingsSeparator.setTitle(org.openide.util.NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.settingsSeparator.title")); // NOI18N
 
         runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/enrichment/resources/run.gif"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(runButton, org.openide.util.NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.runButton.text")); // NOI18N
         runButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 runButtonActionPerformed(evt);
             }
         });
 
         settingsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 2, 4));
         settingsPanel.setLayout(new java.awt.BorderLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(geneIdColumnLabel, org.openide.util.NbBundle.getMessage(EnrichmentChooserTopComponent.class, "EnrichmentChooserTopComponent.geneIdColumnLabel.text")); // NOI18N
 
         geneIdColumnComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Gene ID" }));
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(settingsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(settingsSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(selectionSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(enrichmentComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(8, 8, 8)
                 .addComponent(infoLabel)
                .addGap(2, 2, 2))
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(runButton, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                 .addComponent(geneIdColumnLabel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(geneIdColumnComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                 .addComponent(wholeNetworkButton)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(fromSelectionButton)))
                         .addContainerGap(20, Short.MAX_VALUE))))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(infoLabel)
                     .addComponent(enrichmentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(selectionSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(wholeNetworkButton)
                     .addComponent(fromSelectionButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(geneIdColumnLabel)
                     .addComponent(geneIdColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(settingsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(runButton))
         );
 
         layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {selectionSeparator, settingsSeparator});
 
     }// </editor-fold>//GEN-END:initComponents
 
     private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
         if (model.isRunning()) {
             cancel();
         } else {
             run();
         }
     }//GEN-LAST:event_runButtonActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox enrichmentComboBox;
     private javax.swing.JRadioButton fromSelectionButton;
     private javax.swing.JComboBox geneIdColumnComboBox;
     private javax.swing.JLabel geneIdColumnLabel;
     private javax.swing.JLabel infoLabel;
     private javax.swing.JButton runButton;
     private javax.swing.ButtonGroup selectionButtonGroup;
     private org.jdesktop.swingx.JXTitledSeparator selectionSeparator;
     private javax.swing.JPanel settingsPanel;
     private org.jdesktop.swingx.JXTitledSeparator settingsSeparator;
     private javax.swing.JRadioButton wholeNetworkButton;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void componentOpened() {
         // TODO add custom code on component opening
     }
 
     @Override
     public void componentClosed() {
         // TODO add custom code on component closing
     }
 
     void writeProperties(java.util.Properties p) {
         // better to version settings since initial version as advocated at
         // http://wiki.apidesign.org/wiki/PropertyFiles
         p.setProperty("version", "1.0");
         // TODO store your settings
     }
 
     void readProperties(java.util.Properties p) {
         String version = p.getProperty("version");
         // TODO read your settings according to their version
     }
 
     @Override
     public void analyzingStarted(Enrichment en) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 EnrichmentResultTopComponent resultDisplayer = EnrichmentResultTopComponent.getInstance();
 
                 Lookup.getDefault().lookup(EnrichmentController.class).clearResult();
                 resultDisplayer.setLookupContents();
 
                 resultDisplayer.setBusy(true);
                 resultDisplayer.open();
                 resultDisplayer.requestActive();
 
                 refreshEnabled();
             }
         });
     }
 
     @Override
     public void analyzingFinished(final Enrichment en) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 EnrichmentResultTopComponent resultDisplayer = EnrichmentResultTopComponent.getInstance();
                 resultDisplayer.setResult(model.getResult(en));
                 resultDisplayer.open();
                 resultDisplayer.requestActive();
 
                 refreshEnabled();
             }
         });
     }
 
     @Override
     public void enrichmentChanged(Enrichment oe, Enrichment ne) {
         refreshModel();
     }
 }
