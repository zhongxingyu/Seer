 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * FresnelPortalJPanel.java
  *
  * Created on May 9, 2012, 1:55:01 PM
  */
 package cz.muni.fi.fresneleditor.gui.mod.portal;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFWriter;
 import cz.muni.fi.fresneleditor.common.BrowserUtils;
 import cz.muni.fi.fresneleditor.common.ContextHolder;
 import cz.muni.fi.fresneleditor.common.DatasetInfo;
 import cz.muni.fi.fresneleditor.common.DatasetUtils;
 import cz.muni.fi.fresneleditor.common.FresnelApplication;
 import cz.muni.fi.fresneleditor.common.ITabComponent;
 import cz.muni.fi.fresneleditor.common.config.ProjectConfiguration;
 import cz.muni.fi.fresneleditor.common.guisupport.ExtendedDefaultLM;
 import cz.muni.fi.fresneleditor.common.utils.GuiUtils;
 import cz.muni.fi.fresneleditor.gui.mod.portal.model.Service;
 import cz.muni.fi.fresneleditor.gui.mod.portal.model.Transformation;
 import cz.muni.fi.fresneleditor.gui.mod.portal.services.PortalService;
 import cz.muni.fi.fresneleditor.gui.mod.portal.services.PortalServiceImpl;
 import fr.inria.jfresnel.Group;
 import fr.inria.jfresnel.jena.FresnelJenaWriter;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Level;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JFileChooser;
 import javax.swing.JScrollPane;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.URIImpl;
 import org.openrdf.rio.RDFHandler;
 import org.openrdf.rio.n3.N3Writer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author nodrock
  */
 public class FresnelPortalJPanel extends javax.swing.JPanel implements ITabComponent<ProjectConfiguration> {
     private static final Logger LOG = LoggerFactory
 			.getLogger(FresnelPortalJPanel.class);
     
     /** Creates new form FresnelPortalJPanel */
     public FresnelPortalJPanel() {
         initComponents();
         serverTextField.setText("http://localhost:8080/fresnelportal");
         customInitComponents();
     }
     
     private void customInitComponents() {
         List<URI> availGroups = new ArrayList<URI>();
     
         List<Group> groups = ContextHolder.getInstance().getFresnelDocumentDao().getGroups();
         for(Group g : groups){  
             availGroups.add(new URIImpl(g.getURI()));
         }
         
         groupSelectionList.setModel(new ExtendedDefaultLM(availGroups));
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         fresnelPortalSelectionJPanel = new javax.swing.JPanel();
         selectPortalBtn = new javax.swing.JButton();
         serverSelectionLabel = new javax.swing.JLabel();
         serverTextField = new javax.swing.JTextField();
         fresnelPortalVisualizationJPanel = new javax.swing.JPanel();
         groupSelectionLabel = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         groupSelectionList = new javax.swing.JList();
         selectServiceLabel = new javax.swing.JLabel();
         selectServiceComboBox = new javax.swing.JComboBox();
         selectTransformationLabel = new javax.swing.JLabel();
         selectTransformationComboBox = new javax.swing.JComboBox();
         selectOutputFileLabel = new javax.swing.JLabel();
         outputFileTextField = new javax.swing.JTextField();
         outputFileBtn = new javax.swing.JButton();
         visualizeBtn = new javax.swing.JButton();
         closeBtn = new javax.swing.JButton();
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("cz/muni/fi/fresneleditor/gui/mod/portal/FresnelPortalBundle"); // NOI18N
         fresnelPortalSelectionJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("fresnelPortalSelectionJPanelTitle"))); // NOI18N
         fresnelPortalSelectionJPanel.setName("fresnelPortalSelectionJPanel"); // NOI18N
 
         selectPortalBtn.setText(bundle.getString("load")); // NOI18N
         selectPortalBtn.setName("selectPortalBtn"); // NOI18N
         selectPortalBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selectPortalBtnActionPerformed(evt);
             }
         });
 
         serverSelectionLabel.setText(bundle.getString("serverSelectionLabel")); // NOI18N
         serverSelectionLabel.setName("serverSelectionLabel"); // NOI18N
 
         serverTextField.setName("serverTextField"); // NOI18N
 
         org.jdesktop.layout.GroupLayout fresnelPortalSelectionJPanelLayout = new org.jdesktop.layout.GroupLayout(fresnelPortalSelectionJPanel);
         fresnelPortalSelectionJPanel.setLayout(fresnelPortalSelectionJPanelLayout);
         fresnelPortalSelectionJPanelLayout.setHorizontalGroup(
             fresnelPortalSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, fresnelPortalSelectionJPanelLayout.createSequentialGroup()
                 .add(29, 29, 29)
                 .add(serverSelectionLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(serverTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(selectPortalBtn)
                 .addContainerGap())
         );
         fresnelPortalSelectionJPanelLayout.setVerticalGroup(
             fresnelPortalSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(fresnelPortalSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                 .add(serverSelectionLabel)
                 .add(selectPortalBtn)
                 .add(serverTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         fresnelPortalVisualizationJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("fresnelPortalVisualizationJPanelTitle"))); // NOI18N
         fresnelPortalVisualizationJPanel.setName("fresnelPortalVisualizationJPanel"); // NOI18N
 
         groupSelectionLabel.setText(bundle.getString("groupSelectionLabel")); // NOI18N
         groupSelectionLabel.setName("groupSelectionLabel"); // NOI18N
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         groupSelectionList.setName("groupSelectionList"); // NOI18N
         jScrollPane1.setViewportView(groupSelectionList);
 
         selectServiceLabel.setText(bundle.getString("selectServiceLabel")); // NOI18N
         selectServiceLabel.setName("selectServiceLabel"); // NOI18N
 
         selectServiceComboBox.setName("selectServiceComboBox"); // NOI18N
 
         selectTransformationLabel.setText(bundle.getString("selectTransformationLabel")); // NOI18N
         selectTransformationLabel.setName("selectTransformationLabel"); // NOI18N
 
         selectTransformationComboBox.setName("selectTransformationComboBox"); // NOI18N
 
         selectOutputFileLabel.setText(bundle.getString("selectOutputFileLabel")); // NOI18N
         selectOutputFileLabel.setName("selectOutputFileLabel"); // NOI18N
 
         outputFileTextField.setName("outputFileTextField"); // NOI18N
 
         outputFileBtn.setText(bundle.getString("browse")); // NOI18N
         outputFileBtn.setName("outputFileBtn"); // NOI18N
         outputFileBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 outputFileBtnActionPerformed(evt);
             }
         });
 
         visualizeBtn.setText(bundle.getString("visualize")); // NOI18N
         visualizeBtn.setName("visualizeBtn"); // NOI18N
         visualizeBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 visualizeBtnActionPerformed(evt);
             }
         });
 
         closeBtn.setText(bundle.getString("close")); // NOI18N
         closeBtn.setName("closeBtn"); // NOI18N
         closeBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 closeBtnActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout fresnelPortalVisualizationJPanelLayout = new org.jdesktop.layout.GroupLayout(fresnelPortalVisualizationJPanel);
         fresnelPortalVisualizationJPanel.setLayout(fresnelPortalVisualizationJPanelLayout);
         fresnelPortalVisualizationJPanelLayout.setHorizontalGroup(
             fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(fresnelPortalVisualizationJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                     .add(groupSelectionLabel)
                     .add(fresnelPortalVisualizationJPanelLayout.createSequentialGroup()
                         .add(fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                             .add(selectTransformationLabel)
                             .add(selectServiceLabel)
                             .add(selectOutputFileLabel)
                             .add(closeBtn))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(selectTransformationComboBox, 0, 515, Short.MAX_VALUE)
                            .add(selectServiceComboBox, 0, 515, Short.MAX_VALUE)
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, fresnelPortalVisualizationJPanelLayout.createSequentialGroup()
                                 .add(4, 4, 4)
                                .add(outputFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(outputFileBtn))
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, fresnelPortalVisualizationJPanelLayout.createSequentialGroup()
                                 .add(3, 3, 3)
                                .add(visualizeBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                                 .add(1, 1, 1)))))
                 .addContainerGap())
         );
         fresnelPortalVisualizationJPanelLayout.setVerticalGroup(
             fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(fresnelPortalVisualizationJPanelLayout.createSequentialGroup()
                 .add(groupSelectionLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(selectServiceLabel)
                     .add(selectServiceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(selectTransformationLabel)
                     .add(selectTransformationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(selectOutputFileLabel)
                     .add(outputFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(outputFileBtn))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fresnelPortalVisualizationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(visualizeBtn)
                     .add(closeBtn)))
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(fresnelPortalSelectionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(fresnelPortalVisualizationJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(fresnelPortalSelectionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(fresnelPortalVisualizationJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void selectPortalBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPortalBtnActionPerformed
         checkServerAndLoadData();
     }//GEN-LAST:event_selectPortalBtnActionPerformed
 
     private void outputFileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFileBtnActionPerformed
         openChooseOutputFileDialog();
     }//GEN-LAST:event_outputFileBtnActionPerformed
 
     private void visualizeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visualizeBtnActionPerformed
         visualizeDocument();
     }//GEN-LAST:event_visualizeBtnActionPerformed
 
     private void closeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtnActionPerformed
         FresnelApplication.getApp().getBaseFrame().closeTabByComponent(this);
     }//GEN-LAST:event_closeBtnActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton closeBtn;
     private javax.swing.JPanel fresnelPortalSelectionJPanel;
     private javax.swing.JPanel fresnelPortalVisualizationJPanel;
     private javax.swing.JLabel groupSelectionLabel;
     private javax.swing.JList groupSelectionList;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JButton outputFileBtn;
     private javax.swing.JTextField outputFileTextField;
     private javax.swing.JLabel selectOutputFileLabel;
     private javax.swing.JButton selectPortalBtn;
     private javax.swing.JComboBox selectServiceComboBox;
     private javax.swing.JLabel selectServiceLabel;
     private javax.swing.JComboBox selectTransformationComboBox;
     private javax.swing.JLabel selectTransformationLabel;
     private javax.swing.JLabel serverSelectionLabel;
     private javax.swing.JTextField serverTextField;
     private javax.swing.JButton visualizeBtn;
     // End of variables declaration//GEN-END:variables
 
     private void checkServerAndLoadData() {
         if(serverTextField.getText() != null && !serverTextField.getText().isEmpty()){
             PortalService portalService = new PortalServiceImpl(serverTextField.getText());
             List<Service> services = portalService.getServices();
             selectServiceComboBox.setModel(new DefaultComboBoxModel(new Vector<Service>(services)));
             
             List<Transformation> transformations = portalService.getTransformations();
             selectTransformationComboBox.setModel(new DefaultComboBoxModel(new Vector<Transformation>(transformations)));
         }
     }
 
     private void openChooseOutputFileDialog() {
         JFileChooser chooser = new JFileChooser();
 
 //        FileFilter filter = new FileNameExtensionFilter(
 //                "Fresnel Editor projects (*.n3)", "n3");
 //        chooser.setFileFilter(filter);
 
         int returnVal = chooser.showSaveDialog(GuiUtils.getTopComponent());
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             try {
                 outputFileTextField.setText(chooser.getSelectedFile().getCanonicalPath());        
             } catch (Exception ex) {
                 // FIXME message dialog
                 LOG.error("Wrong filename.", ex);
             }
         }
     }
 
     private void visualizeDocument() {
         if(groupSelectionList.getSelectedValue() != null && 
                 selectServiceComboBox.getSelectedItem() != null && 
                 selectTransformationComboBox.getSelectedItem() != null &&
                 serverTextField.getText() != null && !serverTextField.getText().isEmpty()){
             PortalService portalService = new PortalServiceImpl(serverTextField.getText());
             
             // check output file
             String output = outputFileTextField.getText();
             if(output == null){
                 output = "test.xml";
             }
             
             // do Fresnel project export
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             
             Model model = ModelFactory.createDefaultModel();
             RDFWriter writer = model.getWriter("N3");
 
             FresnelJenaWriter fjw = new FresnelJenaWriter();             
             fjw.write(ContextHolder.getInstance().getFresnelDocumentDao().getFresnelDocument(), model);
 
             ProjectConfiguration projectConfiguration = ContextHolder.getInstance().getProjectConfiguration();
             DatasetUtils.writeDatasetInfo(model, new DatasetInfo(DatasetUtils.getLastPart(projectConfiguration.getUri()), 
                     projectConfiguration.getName(),
                     projectConfiguration.getDescription()), "http://localproject/");
             
             Map<String, String> prefixes = ContextHolder.getInstance().getFresnelDocumentDao().getFresnelDocument().getPrefixes();
             model.setNsPrefixes(prefixes);
             
             writer.write(model, baos, "N3");
             
             
             // upload project
             InputStream is = new ByteArrayInputStream(baos.toByteArray());        
             Integer projectId = portalService.uploadProject(is);
             if(projectId != null){
                 // try to visualize
                 try {
                     portalService.visualizeProject(projectId, groupSelectionList.getSelectedValue().toString(), 
                             ((Service)selectServiceComboBox.getSelectedItem()).getId(),
                             ((Transformation)selectTransformationComboBox.getSelectedItem()).getId(), new FileOutputStream(output));
                 } catch (FileNotFoundException ex) {
                     java.util.logging.Logger.getLogger(FresnelPortalJPanel.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             
             BrowserUtils.navigate("file:///" + output);
         }
     }
 
     @Override
     public String getLabel() {
         return "Visualization portal";
     }
 
     private JScrollPane scrollPane;
     
     @Override
     public JScrollPane getScrollPane() {
         if (scrollPane == null) {
                 scrollPane = new JScrollPane(this);
         }
         return scrollPane;
     }
 
     @Override
     public ProjectConfiguration getItem() {
         return new ProjectConfiguration();
     }
 }
