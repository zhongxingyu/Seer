 package org.esa.nest.gpf;
 
 import org.esa.beam.framework.dataop.resamp.ResamplingFactory;
 import org.esa.beam.framework.dataop.dem.ElevationModelRegistry;
 import org.esa.beam.framework.dataop.dem.ElevationModelDescriptor;
 import org.esa.beam.framework.gpf.ui.BaseOperatorUI;
 import org.esa.beam.framework.gpf.ui.UIValidation;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.ui.AppContext;
 import org.esa.beam.visat.VisatApp;
 import org.esa.nest.util.DialogUtils;
 import org.esa.nest.util.ResourceUtils;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Map;
 import java.io.File;
 
 /**
  * User interface for SARSimulationOp
  */
 public class SARSimulationOpUI extends BaseOperatorUI {
 
     private final JList bandList = new JList();
     private final JComboBox demName = new JComboBox();
     private static final String externalDEMStr = "External DEM";
 
     private final JComboBox demResamplingMethod = new JComboBox(new String[] {ResamplingFactory.NEAREST_NEIGHBOUR_NAME,
                                                                            ResamplingFactory.BILINEAR_INTERPOLATION_NAME,
                                                                            ResamplingFactory.CUBIC_CONVOLUTION_NAME});
 
     private final JTextField externalDEMFile = new JTextField("");
     private final JTextField externalDEMNoDataValue = new JTextField("");
     private final JButton externalDEMBrowseButton = new JButton("...");
     private final JLabel externalDEMFileLabel = new JLabel("External DEM:");
     private final JLabel externalDEMNoDataValueLabel = new JLabel("DEM No Data Value:");
 
     @Override
     public JComponent CreateOpTab(String operatorName, Map<String, Object> parameterMap, AppContext appContext) {
 
         final ElevationModelRegistry elevationModelRegistry = ElevationModelRegistry.getInstance();
 
         final ElevationModelDescriptor[] demDesciptors = elevationModelRegistry.getAllDescriptors();
         for(ElevationModelDescriptor dem : demDesciptors) {
             demName.addItem(dem.getName());
         }
         demName.addItem(externalDEMStr);
 
         initializeOperatorUI(operatorName, parameterMap);
 
         final JComponent panel = createPanel();
         initParameters();
 
         demName.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent event) {
                 final String item = (String)demName.getSelectedItem();
                 if(item.equals(externalDEMStr)) {
                     enableExternalDEM(true);
                 } else {
                     externalDEMFile.setText("");
                     enableExternalDEM(false);
                 }
             }
         });
         externalDEMFile.setColumns(30);
         demName.setSelectedItem(parameterMap.get("demName"));
         enableExternalDEM(false);
 
         externalDEMBrowseButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 final File file = VisatApp.getApp().showFileOpenDialog("External DEM File", false, null);
                 externalDEMFile.setText(file.getAbsolutePath());
             }
         });
 
         return new JScrollPane(panel);
     }
 
     @Override
     public void initParameters() {
 
         OperatorUIUtils.initBandList(bandList, getBandNames());
 
         demName.setSelectedItem(paramMap.get("demName"));
         demResamplingMethod.setSelectedItem(paramMap.get("demResamplingMethod"));
 
         final File extFile = (File)paramMap.get("externalDEMFile");
         if(extFile != null) {
             externalDEMFile.setText(extFile.getAbsolutePath());
             externalDEMNoDataValue.setText(String.valueOf(paramMap.get("externalDEMNoDataValue")));
         }
     }
 
     @Override
     public UIValidation validateParameters() {
 
         return new UIValidation(true, "");
     }
 
     @Override
     public void updateParameters() {
 
         OperatorUIUtils.updateBandList(bandList, paramMap);
 
         paramMap.put("demName", demName.getSelectedItem());
         paramMap.put("demResamplingMethod", demResamplingMethod.getSelectedItem());
 
         final String extFileStr = externalDEMFile.getText();
         if(!extFileStr.isEmpty()) {
             paramMap.put("externalDEMFile", new File(extFileStr));
             if (externalDEMNoDataValue.getText().isEmpty()) {
                 throw new OperatorException("Please enter DEM No Data Value");
             } else {
                 paramMap.put("externalDEMNoDataValue", Double.parseDouble(externalDEMNoDataValue.getText()));
             }
        } else {
            throw new OperatorException("Please enter External DEM");
         }
     }
 
     private JComponent createPanel() {
 
         final JPanel contentPane = new JPanel();
         contentPane.setLayout(new GridBagLayout());
         final GridBagConstraints gbc = DialogUtils.createGridBagConstraints();
 
         contentPane.add(new JLabel("Source Bands:"), gbc);
         gbc.fill = GridBagConstraints.BOTH;
         gbc.gridx = 1;
         contentPane.add(new JScrollPane(bandList), gbc);
 
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.gridx = 0;
         gbc.gridy++;
         DialogUtils.addComponent(contentPane, gbc, "Digital Elevation Model:", demName);
         gbc.gridy++;
         DialogUtils.addComponent(contentPane, gbc, externalDEMFileLabel, externalDEMFile);
         gbc.gridx = 2;
         contentPane.add(externalDEMBrowseButton, gbc);
         gbc.gridy++;
         DialogUtils.addComponent(contentPane, gbc, externalDEMNoDataValueLabel, externalDEMNoDataValue);
         gbc.gridy++;
         DialogUtils.addComponent(contentPane, gbc, "DEM Resampling Method:", demResamplingMethod);
 
         //DialogUtils.fillPanel(contentPane, gbc);
 
         return contentPane;
     }
 
     private void enableExternalDEM(boolean flag) {
         DialogUtils.enableComponents(externalDEMFileLabel, externalDEMFile, flag);
         DialogUtils.enableComponents(externalDEMNoDataValueLabel, externalDEMNoDataValue, flag);
         externalDEMBrowseButton.setVisible(flag);
     }
 }
