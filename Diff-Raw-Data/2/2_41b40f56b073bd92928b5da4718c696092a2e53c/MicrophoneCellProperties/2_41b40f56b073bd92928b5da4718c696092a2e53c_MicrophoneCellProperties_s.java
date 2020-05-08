 /*
  * MicrophoneCellProperties.java
  *
  * Created on May 29, 2009, 1:04 PM
  */
 
 package org.jdesktop.wonderland.modules.microphone.client.cell;
 
 import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState;
 import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.FullVolumeArea;
 import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.ActiveArea;
 
 import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
 
 import org.jdesktop.wonderland.client.cell.properties.annotation.CellProperties;
 
 import org.jdesktop.wonderland.client.cell.properties.spi.CellPropertiesSPI;
 
 import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
 
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 
 import javax.swing.JPanel;
 import javax.swing.SpinnerNumberModel;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 //import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 
 /**
  *
  * @author  jp
  */
 @CellProperties
 public class MicrophoneCellProperties extends JPanel implements CellPropertiesSPI {
 
     private CellPropertiesEditor editor;
 
     private String originalName;
 
     private int originalVolume;
 
     private double originalFullVolumeX;
     private double originalFullVolumeY;
     private double originalFullVolumeZ;
 
     private double originalActiveOriginX;
     private double originalActiveOriginY;
     private double originalActiveOriginZ;
 
     private double originalActiveExtentX;
     private double originalActiveExtentY;
     private double originalActiveExtentZ;
 
     private SpinnerNumberModel volumeModel;
 
     private SpinnerNumberModel fullVolumeXModel;
     private SpinnerNumberModel fullVolumeYModel;
     private SpinnerNumberModel fullVolumeZModel;
 
     private SpinnerNumberModel activeOriginXModel;
     private SpinnerNumberModel activeOriginYModel;
     private SpinnerNumberModel activeOriginZModel;
 
     private SpinnerNumberModel activeExtentXModel;
     private SpinnerNumberModel activeExtentYModel;
     private SpinnerNumberModel activeExtentZModel;
 
     /** Creates new form MicrophoneCellProperties */
     public MicrophoneCellProperties() {
         initComponents();
 
 	nameTextField.getDocument().addDocumentListener(new NameTextFieldListener());
 
         Double value = new Double(1);
         Double min = new Double(0);
         Double max = new Double(20);
         Double step = new Double(1);
         volumeModel = new SpinnerNumberModel(value, min, max, step);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         fullVolumeXModel = new SpinnerNumberModel(value, min, max, step);
         fullVolumeXSpinner.setModel(fullVolumeXModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         fullVolumeYModel = new SpinnerNumberModel(value, min, max, step);
         fullVolumeYSpinner.setModel(fullVolumeYModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         fullVolumeZModel = new SpinnerNumberModel(value, min, max, step);
         fullVolumeZSpinner.setModel(fullVolumeZModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         activeOriginXModel = new SpinnerNumberModel(value, min, max, step);
         activeOriginXSpinner.setModel(activeOriginXModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         activeOriginYModel = new SpinnerNumberModel(value, min, max, step);
         activeOriginYSpinner.setModel(activeOriginYModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         activeOriginZModel = new SpinnerNumberModel(value, min, max, step);
         activeOriginZSpinner.setModel(activeOriginZModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         activeExtentXModel = new SpinnerNumberModel(value, min, max, step);
         activeExtentXSpinner.setModel(activeExtentXModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         activeExtentYModel = new SpinnerNumberModel(value, min, max, step);
         activeExtentYSpinner.setModel(activeExtentYModel);
 
         value = new Double(1);
         min = new Double(0);
         max = new Double(100);
         step = new Double(1);
         activeExtentZModel = new SpinnerNumberModel(value, min, max, step);
         activeExtentZSpinner.setModel(activeExtentZModel);
 
     }
 
     public Class getServerCellStateClass() {
        return MicrophoneCellServerState.class;
     }
 
     public String getDisplayName() {
         return "Microphone Cell";
     }
 
     public JPanel getPropertiesJPanel(CellPropertiesEditor editor) {
         this.editor = editor;
         return this;
     }
 
     public <T extends CellServerState> void updateGUI(T cellServerState) {
         MicrophoneCellServerState state = (MicrophoneCellServerState)cellServerState;
 
 	if (state == null) {
 	    return;
 	}
 
 	originalName = state.getName();
 
 	nameTextField.setText(originalName);
 
 	//originalVolume = VolumeUtil.getClientVolume(state.getVolume());
 	originalVolume = getClientVolume(state.getVolume());
 
 	volumeSlider.setValue(originalVolume);
 
 	FullVolumeArea fullVolumeArea = state.getFullVolumeArea();
 	ActiveArea activeArea = state.getActiveArea();
 
 	originalFullVolumeX = fullVolumeArea.xExtent;
 	originalFullVolumeY = fullVolumeArea.yExtent;
 	originalFullVolumeZ = fullVolumeArea.zExtent;
 
 	fullVolumeXModel.setValue((Double) originalFullVolumeX);
 	fullVolumeYModel.setValue((Double) originalFullVolumeY);
 	fullVolumeZModel.setValue((Double) originalFullVolumeZ);
 
 	Origin activeOrigin = activeArea.origin;
 
 	originalActiveOriginX = activeOrigin.x;
 	originalActiveOriginY = activeOrigin.y;
 	originalActiveOriginZ = activeOrigin.z;
 
 	activeOriginXModel.setValue((Double) originalActiveOriginX);
 	activeOriginYModel.setValue((Double) originalActiveOriginY);
 	activeOriginZModel.setValue((Double) originalActiveOriginZ);
 
 	originalActiveExtentX = activeArea.xExtent;
 	originalActiveExtentY = activeArea.yExtent;
 	originalActiveExtentZ = activeArea.zExtent;
 
 	activeExtentXModel.setValue((Double) originalActiveExtentX);
 	activeExtentYModel.setValue((Double) originalActiveExtentY);
 	activeExtentZModel.setValue((Double) originalActiveExtentZ);
     }
 
     public <T extends CellServerState> void getCellServerState(T cellServerState) {
 	MicrophoneCellServerState state = (MicrophoneCellServerState) cellServerState;
 
 	if (state == null) {
 	    return;
 	}
 
 	state.setName(nameTextField.getText());
 
 	//state.setVolume(VolumeUtil.getServerVolume(volumeSlider.getValue()));
 	state.setVolume(getServerVolume(volumeSlider.getValue()));
 
 	FullVolumeArea fullVolumeArea = new FullVolumeArea("BOX", 
 	    (Double) fullVolumeXModel.getValue(),
 	    (Double) fullVolumeYModel.getValue(),
 	    (Double) fullVolumeZModel.getValue());
 
 	state.setFullVolumeArea(fullVolumeArea);
 
 	Origin activeOrigin = new Origin();
 	activeOrigin.x = (Double) activeOriginXModel.getValue();
 	activeOrigin.y = (Double) activeOriginYModel.getValue();
 	activeOrigin.z = (Double) activeOriginZModel.getValue();
 	    
 	ActiveArea activeArea = new ActiveArea(activeOrigin, "BOX", (Double) activeExtentXModel.getValue(),
 	    (Double) activeExtentYModel.getValue(), (Double) activeExtentZModel.getValue());
 
 	state.setActiveArea(activeArea);
     }
 
     /**
      * Inner class to listen for changes to the text field and fire off dirty
      * or clean indications to the cell properties editor.
      */
     class NameTextFieldListener implements DocumentListener {
         public void insertUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void removeUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void changedUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         private void checkDirty() {
             if (editor == null) {
 		return;
 	    }
 
             String name = nameTextField.getText();
 
 	    if (name.length() > 0 && name.equals(originalName) == false) {
                 editor.setPanelDirty(MicrophoneCellProperties.class, true);
             } else {
                 editor.setPanelDirty(MicrophoneCellProperties.class, false);
             }
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
 
         jLabel1 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         fullVolumeXSpinner = new javax.swing.JSpinner();
         fullVolumeYSpinner = new javax.swing.JSpinner();
         fullVolumeZSpinner = new javax.swing.JSpinner();
         activeOriginXSpinner = new javax.swing.JSpinner();
         activeOriginYSpinner = new javax.swing.JSpinner();
         activeOriginZSpinner = new javax.swing.JSpinner();
         activeExtentXSpinner = new javax.swing.JSpinner();
         activeExtentYSpinner = new javax.swing.JSpinner();
         activeExtentZSpinner = new javax.swing.JSpinner();
         jLabel13 = new javax.swing.JLabel();
         nameTextField = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         volumeSlider = new javax.swing.JSlider();
 
         jLabel1.setText("x:");
 
         jLabel4.setText("Full Volume Extent");
 
         jLabel2.setText("y:");
 
         jLabel3.setText("z:");
 
         jLabel5.setText("Active Area Origin");
 
         jLabel6.setText("x:");
 
         jLabel7.setText("y:");
 
         jLabel8.setText("z:");
 
         jLabel9.setText("Active Area Extent");
 
         jLabel10.setText("x:");
 
         jLabel11.setText("y:");
 
         jLabel12.setText("z:");
 
         fullVolumeXSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 fullVolumeXSpinnerStateChanged(evt);
             }
         });
 
         fullVolumeYSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 fullVolumeYSpinnerStateChanged(evt);
             }
         });
 
         fullVolumeZSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 fullVolumeZSpinnerStateChanged(evt);
             }
         });
 
         activeOriginXSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 activeOriginXSpinnerStateChanged(evt);
             }
         });
 
         activeOriginYSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 activeOriginYSpinnerStateChanged(evt);
             }
         });
 
         activeOriginZSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 activeOriginZSpinnerStateChanged(evt);
             }
         });
 
         activeExtentXSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 activeExtentXSpinnerStateChanged(evt);
             }
         });
 
         activeExtentYSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 activeExtentYSpinnerStateChanged(evt);
             }
         });
 
         activeExtentZSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 activeExtentZSpinnerStateChanged(evt);
             }
         });
 
         jLabel13.setText("Name:");
 
         jLabel14.setText("Volume:");
 
         volumeSlider.setMajorTickSpacing(1);
         volumeSlider.setMaximum(10);
         volumeSlider.setPaintLabels(true);
         volumeSlider.setPaintTicks(true);
         volumeSlider.setSnapToTicks(true);
         volumeSlider.setValue(5);
         volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 volumeSliderStateChanged(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE)
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                             .add(layout.createSequentialGroup()
                                                 .add(jLabel13)
                                                 .add(22, 22, 22))
                                             .add(layout.createSequentialGroup()
                                                 .add(jLabel14)
                                                 .add(18, 18, 18)))
                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                             .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                             .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                         .add(32, 32, 32)
                                         .add(jLabel1)
                                         .add(18, 18, 18)
                                         .add(fullVolumeXSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                         .add(34, 34, 34)
                                         .add(jLabel2)
                                         .add(18, 18, 18)
                                         .add(fullVolumeYSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                         .add(29, 29, 29)
                                         .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                         .add(4, 4, 4)
                                         .add(fullVolumeZSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                 .add(479, 479, 479))
                             .add(layout.createSequentialGroup()
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(layout.createSequentialGroup()
                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                             .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                             .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                 .add(30, 30, 30)
                                                 .add(jLabel6)
                                                 .add(18, 18, 18)
                                                 .add(activeOriginXSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                             .add(activeExtentXSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                         .add(31, 31, 31)
                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                             .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)
                                             .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11))
                                         .add(18, 18, 18)
                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                             .add(activeExtentYSpinner)
                                             .add(activeOriginYSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                                         .add(26, 26, 26)
                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                             .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                             .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)))
                                     .add(layout.createSequentialGroup()
                                         .add(42, 42, 42)
                                         .add(jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                     .add(activeOriginZSpinner)
                                     .add(activeExtentZSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))
                                 .add(3, 3, 3)))
                         .addContainerGap())
                     .add(layout.createSequentialGroup()
                         .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                         .add(566, 566, 566))))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel13)
                     .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(18, 18, 18)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(fullVolumeXSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(fullVolumeYSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(fullVolumeZSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel3)
                     .add(jLabel2)
                     .add(jLabel1))
                 .add(34, 34, 34)
                 .add(jLabel5)
                 .add(18, 18, 18)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel6)
                     .add(jLabel7)
                     .add(jLabel8)
                     .add(activeOriginYSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(activeOriginXSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(activeOriginZSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(50, 50, 50)
                 .add(jLabel9)
                 .add(28, 28, 28)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel10)
                     .add(jLabel11)
                     .add(activeExtentYSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(activeExtentXSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel12)
                     .add(activeExtentZSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(232, 232, 232))
         );
     }// </editor-fold>//GEN-END:initComponents
 
 private void fullVolumeXSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fullVolumeXSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double fullVolumeX = (Double) fullVolumeXModel.getValue();
 
     if (fullVolumeX != originalFullVolumeX) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_fullVolumeXSpinnerStateChanged
 
 private void fullVolumeYSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fullVolumeYSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double fullVolumeY = (Double) fullVolumeYModel.getValue();
 
     if (fullVolumeY != originalFullVolumeY) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_fullVolumeYSpinnerStateChanged
 
 private void fullVolumeZSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fullVolumeZSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double fullVolumeZ = (Double) fullVolumeZModel.getValue();
 
     if (fullVolumeZ != originalFullVolumeZ) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_fullVolumeZSpinnerStateChanged
 
 private void activeOriginXSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_activeOriginXSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double activeOriginX = (Double) activeOriginXModel.getValue();
 
     if (activeOriginX != originalActiveOriginX) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_activeOriginXSpinnerStateChanged
 
 private void activeOriginYSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_activeOriginYSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double activeOriginY = (Double) activeOriginYModel.getValue();
 
     if (activeOriginY != originalActiveOriginY) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_activeOriginYSpinnerStateChanged
 
 private void activeExtentXSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_activeExtentXSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double activeOriginZ = (Double) activeOriginZModel.getValue();
 
     if (activeOriginZ != originalActiveOriginZ) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_activeExtentXSpinnerStateChanged
 
 private void activeExtentYSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_activeExtentYSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double activeExtentX = (Double) activeExtentXModel.getValue();
 
     if (activeExtentX != originalActiveExtentX) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_activeExtentYSpinnerStateChanged
 
 private void activeExtentZSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_activeExtentZSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double activeExtentY = (Double) activeExtentYModel.getValue();
 
     if (activeExtentY != originalActiveExtentY) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_activeExtentZSpinnerStateChanged
 
 private void activeOriginZSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_activeOriginZSpinnerStateChanged
     if (editor == null) {
         return;
     }
 
     Double activeExtentZ = (Double) activeExtentZModel.getValue();
 
     if (activeExtentZ != originalActiveExtentZ) {
         editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
 }//GEN-LAST:event_activeOriginZSpinnerStateChanged
 
 private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
     if (editor == null) { 
 	return;
     }
 
     int volume = volumeSlider.getValue();
 
     if (volume != originalVolume) {
 	editor.setPanelDirty(MicrophoneCellProperties.class, true);
     } else {
         editor.setPanelDirty(MicrophoneCellProperties.class, false);
     }
     // TODO add your handling code here:
 }//GEN-LAST:event_volumeSliderStateChanged
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JSpinner activeExtentXSpinner;
     private javax.swing.JSpinner activeExtentYSpinner;
     private javax.swing.JSpinner activeExtentZSpinner;
     private javax.swing.JSpinner activeOriginXSpinner;
     private javax.swing.JSpinner activeOriginYSpinner;
     private javax.swing.JSpinner activeOriginZSpinner;
     private javax.swing.JSpinner fullVolumeXSpinner;
     private javax.swing.JSpinner fullVolumeYSpinner;
     private javax.swing.JSpinner fullVolumeZSpinner;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JTextField nameTextField;
     private javax.swing.JSlider volumeSlider;
     // End of variables declaration//GEN-END:variables
 
     private int getClientVolume(double serverVolume) {
         int clientVolume;
 
         if (serverVolume <= 1) {
             clientVolume = (int) (Math.round(serverVolume * 5 * 10) / 10.);
         } else {
             clientVolume = (int) (Math.round((((serverVolume - 1) / .6) + 5) * 10) / 10.);
         }
 
         //System.out.println(" Server Volume " + serverVolume + " Client Volume " + clientVolume);
         return clientVolume;
     }
 
    private int double getServerVolume(double clientVolume) {
         double serverVolume;
 
         if (clientVolume > 5) {
             serverVolume = (double) (1 + ((clientVolume - 5) * .6));
         } else {
             serverVolume = (double) (clientVolume / 5.);
         }
 
         //System.out.println("Client Volume " + clientVolume + " Server Volume " + serverVolume);
         return serverVolume;
     }
 
 }
