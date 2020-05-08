 package ua.pp.fland.yearly.essay.identif.task3.gui;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ua.pp.fland.yearly.essay.identif.task3.gui.tools.BoxLayoutUtils;
 import ua.pp.fland.yearly.essay.identif.task3.gui.tools.ComponentUtils;
 import ua.pp.fland.yearly.essay.identif.task3.gui.tools.GUITools;
 import ua.pp.fland.yearly.essay.identif.task3.gui.tools.StandardBordersSizes;
 import ua.pp.fland.yearly.essay.identif.task3.model.ImplicitFiniteDifferenceMethod;
 import ua.pp.fland.yearly.essay.identif.task3.model.storage.CsvTimeTemperatureStorer;
 import ua.pp.fland.yearly.essay.identif.task3.model.storage.TimeTemperatureStorer;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 /**
  * @author Maxim Bondarenko
  * @version 1.0 11/7/11
  */
 
 public class MainWindow {
     private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
 
     private final static ResourceBundle bundle = ResourceBundle.getBundle("task3");
 
     private final static Dimension MAIN_FRAME_SIZE = new Dimension(Integer.parseInt(bundle.getString("window.width")),
             Integer.parseInt(bundle.getString("window.height")));
 
     private final static String PROCESS_BTN_TEXT = "Process";
     private final static String EXIT_BTN_TEXT = "Exit";
 
     private final static String START_TEMP_LABEL_TEXT = "Start temperature, C: ";
     private final static String THERMAL_CONDUCTIVITY_LABEL_TEXT = "Thermal conductivity: ";
     private final static String ENV_TEMP_LABEL_TEXT = "Environment temperature, C: ";
     private final static String WALL_THICKNESS_LABEL_TEXT = "Wall thickness, m: ";
     private final static String HEAT_IRRADIATION_COEFF_LABEL_TEXT = "Heat irradiation coeff: ";
     private final static String THERMAL_DIFFUSIVITY_LABEL_TEXT = "Thermal diffusivity: ";
     private final static String X_STEP_LABEL_TEXT = "X step, m: ";
     private final static String TIME_STEP_LABEL_TEXT = "Time step, sec: ";
     private final static String END_TIME_LABEL_TEXT = "End time, sec: ";
 
     private final JFrame mainFrame;
 
     private final JTextField startTempInput;
     private final JTextField envTempInput;
     private final JTextField wallThicknessInput;
     private final JTextField heatIrradiationCoeefInput;
     private final JTextField thermalConductivityInput;
     private final JTextField thermalDiffusivityInput;
     private final JTextField xStepInput;
     private final JTextField timeStepInput;
     private final JTextField endTimeInput;
 
     public MainWindow() {
         mainFrame = new JFrame("Identification. Yearly Essay. Task 3");
         mainFrame.setSize(MAIN_FRAME_SIZE);
         mainFrame.setResizable(false);
         mainFrame.setLocationRelativeTo(null);
         mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         startTempInput = new JTextField("1000");
         GUITools.fixTextFieldSize(startTempInput);
         startTempInput.setCaretPosition(0);
 
         envTempInput = new JTextField("0");
         GUITools.fixTextFieldSize(envTempInput);
         envTempInput.setCaretPosition(0);
 
         wallThicknessInput = new JTextField("0.3");
         GUITools.fixTextFieldSize(wallThicknessInput);
         wallThicknessInput.setCaretPosition(0);
 
         thermalConductivityInput = new JTextField("237");
         GUITools.fixTextFieldSize(thermalConductivityInput);
         thermalConductivityInput.setCaretPosition(0);
 
        thermalDiffusivityInput = new JTextField("0.00008418");
         GUITools.fixTextFieldSize(thermalDiffusivityInput);
         thermalDiffusivityInput.setCaretPosition(0);
 
         xStepInput = new JTextField("0.001");
         GUITools.fixTextFieldSize(xStepInput);
         xStepInput.setCaretPosition(0);
 
         heatIrradiationCoeefInput = new JTextField("10");
         GUITools.fixTextFieldSize(heatIrradiationCoeefInput);
         heatIrradiationCoeefInput.setCaretPosition(0);
 
        timeStepInput = new JTextField("7200");
         GUITools.fixTextFieldSize(timeStepInput);
         timeStepInput.setCaretPosition(0);
 
        endTimeInput = new JTextField("172800");
         GUITools.fixTextFieldSize(endTimeInput);
         endTimeInput.setCaretPosition(0);
 
         final JPanel mainPanel = BoxLayoutUtils.createVerticalPanel();
         mainPanel.setBorder(new EmptyBorder(StandardBordersSizes.MAIN_BORDER.getValue()));
         ComponentUtils.setSize(mainPanel, MAIN_FRAME_SIZE.width, MAIN_FRAME_SIZE.height);
 
         mainPanel.add(createInputPanels());
         mainPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
         mainPanel.add(createButtonsPanel(mainFrame));
 
         mainFrame.add(mainPanel);
         mainFrame.pack();
         mainFrame.setVisible(true);
     }
 
     private JPanel createButtonsPanel(final JFrame mainFrame) {
         JPanel buttonsPanel = BoxLayoutUtils.createHorizontalPanel();
 
         JButton processButton = new JButton(PROCESS_BTN_TEXT);
         processButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 log.debug("Process btn pressed");
                 double envTemp = Double.parseDouble(envTempInput.getText());
                 double wallThickness = Double.parseDouble(wallThicknessInput.getText());
                 double thermalConductivity = Double.parseDouble(thermalConductivityInput.getText());
                 double thermalDiffusivity = Double.parseDouble(thermalDiffusivityInput.getText());
                 double startTemp = Double.parseDouble(startTempInput.getText());
                 double xStep = Double.parseDouble(xStepInput.getText());
 
                 double heatIrradiationCoeff = Double.parseDouble(heatIrradiationCoeefInput.getText());
 
                 final double timeStep = Double.parseDouble(timeStepInput.getText());
                 final double endTime = Double.parseDouble(endTimeInput.getText());
 
                 ImplicitFiniteDifferenceMethod implicitFiniteDifferenceMethod =
                         new ImplicitFiniteDifferenceMethod(startTemp, envTemp, wallThickness, heatIrradiationCoeff,
                                 xStep, thermalDiffusivity, thermalConductivity, timeStep, endTime);
                 Map<Double, Map<BigDecimal, Double>> calculatedTemp = implicitFiniteDifferenceMethod.calculate();
 
                 JFileChooser fileChooser = new JFileChooser() {
                     @Override
                     public void approveSelection() {
                         File selectedFile = getSelectedFile();
                         if (selectedFile.exists() && getDialogType() == SAVE_DIALOG) {
                             int result = JOptionPane.showConfirmDialog(this, "File " + selectedFile.getName() +
                                     " exist. Overwrite it?", "Overwrite file dialog", JOptionPane.YES_NO_CANCEL_OPTION);
                             switch (result) {
                                 case JOptionPane.YES_OPTION:
                                     super.approveSelection();
                                     return;
                                 case JOptionPane.NO_OPTION:
                                     return;
                                 case JOptionPane.CANCEL_OPTION:
                                     super.cancelSelection();
                                     return;
                             }
                         }
                         super.approveSelection();
                     }
                 };
                 fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
                 int result = fileChooser.showSaveDialog(mainFrame);
                 if (result == JFileChooser.APPROVE_OPTION) {
                     String path = fileChooser.getSelectedFile().getAbsolutePath();
                     if (!path.toLowerCase().endsWith(".csv")) {
                         path = path + ".csv";
                     }
                     log.debug("Storing data to: " + path);
                     try {
                         TimeTemperatureStorer timeTemperatureStorer = new CsvTimeTemperatureStorer(path);
                         timeTemperatureStorer.store(calculatedTemp);
                     } catch (IOException e) {
                         log.error("Exception: " + e, e);
                         JOptionPane.showMessageDialog(mainFrame, "Error while storing " + e,
                                 "IO Error", JOptionPane.ERROR_MESSAGE);
                     }
                     log.debug("Data stored.");
                     JOptionPane.showMessageDialog(mainFrame, "Temperature calculated and stored to " + path, "Success",
                             JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         });
 
         JButton exitButton = new JButton(EXIT_BTN_TEXT);
         exitButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 log.debug("Exit btn pressed");
                 shutdown();
             }
         });
 
         GUITools.createRecommendedMargin(processButton, exitButton);
         GUITools.makeSameSize(processButton, exitButton);
 
         buttonsPanel.add(processButton);
         buttonsPanel.add(Box.createRigidArea(StandardDimension.HOR_RIGID_AREA.getValue()));
         buttonsPanel.add(Box.createRigidArea(StandardDimension.HOR_RIGID_AREA.getValue()));
         buttonsPanel.add(exitButton);
 
         return buttonsPanel;
     }
 
     private JPanel createInputPanels() {
         JPanel inputsPanel = BoxLayoutUtils.createVerticalPanel();
 
         JPanel tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel startTempLabel = new JLabel(START_TEMP_LABEL_TEXT);
         tempHorPanel.add(startTempLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(startTempInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel environmentTempLabel = new JLabel(ENV_TEMP_LABEL_TEXT);
         tempHorPanel.add(environmentTempLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(envTempInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel wallThicknessLabel = new JLabel(WALL_THICKNESS_LABEL_TEXT);
         tempHorPanel.add(wallThicknessLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(wallThicknessInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel heatIrradiationCoeffLabel = new JLabel(HEAT_IRRADIATION_COEFF_LABEL_TEXT);
         tempHorPanel.add(heatIrradiationCoeffLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(heatIrradiationCoeefInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel thermalConductivityLabel = new JLabel(THERMAL_CONDUCTIVITY_LABEL_TEXT);
         tempHorPanel.add(thermalConductivityLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(thermalConductivityInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel thermalDiffusivityLabel = new JLabel(THERMAL_DIFFUSIVITY_LABEL_TEXT);
         tempHorPanel.add(thermalDiffusivityLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(thermalDiffusivityInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel xStepLabel = new JLabel(X_STEP_LABEL_TEXT);
         tempHorPanel.add(xStepLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(xStepInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel timeStepLabel = new JLabel(TIME_STEP_LABEL_TEXT);
         tempHorPanel.add(timeStepLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(timeStepInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel endTimeLabel = new JLabel(END_TIME_LABEL_TEXT);
         tempHorPanel.add(endTimeLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(endTimeInput);
         inputsPanel.add(tempHorPanel);
 
         GUITools.makeSameSize(environmentTempLabel, wallThicknessLabel, startTempLabel, xStepLabel,
                 thermalDiffusivityLabel, thermalConductivityLabel, heatIrradiationCoeffLabel, timeStepLabel,
                 endTimeLabel);
 
         return inputsPanel;
     }
 
     private void shutdown() {
         mainFrame.setVisible(false);
         mainFrame.dispose();
     }
 }
