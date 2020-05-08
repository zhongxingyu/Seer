 package ua.pp.fland.labs.identif.lab6.gui;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ua.pp.fland.labs.identif.lab6.gui.tools.BoxLayoutUtils;
 import ua.pp.fland.labs.identif.lab6.gui.tools.ComponentUtils;
 import ua.pp.fland.labs.identif.lab6.gui.tools.GUITools;
 import ua.pp.fland.labs.identif.lab6.gui.tools.StandardBordersSizes;
 import ua.pp.fland.labs.identif.lab6.model.ImplicitFiniteDifferenceMethod;
 import ua.pp.fland.labs.identif.lab6.model.storage.CsvTimeTemperatureStorer;
 import ua.pp.fland.labs.identif.lab6.model.storage.TimeTemperatureStorer;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 /**
  * @author Maxim Bondarenko
  * @version 1.0 11/7/11
  */
 
 public class MainWindow {
     private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
 
     private final static ResourceBundle bundle = ResourceBundle.getBundle("lab6");
 
     private final static Dimension MAIN_FRAME_SIZE = new Dimension(Integer.parseInt(bundle.getString("window.width")),
             Integer.parseInt(bundle.getString("window.height")));
 
     private final static String PROCESS_BTN_TEXT = "Process";
     private final static String EXIT_BTN_TEXT = "Exit";
 
     private final static String X_START_VALUE_LABEL_TEXT = "x start value: ";
     private final static String X_END_VALUE_LABEL_TEXT = "x end value: ";
     private final static String X_COEFF_LABEL_TEXT = "x coeff: ";
     private final static String FREE_TERM_LABEL_TEXT = "Free term: ";
     private final static String FIRST_EQUATION_LABEL_TEXT = "First equation: ";
     private final static String SECOND_EQUATION_LABEL_TEXT = "Second equation: ";
     private final static String X_0_VALUE_LABEL_TEXT = "x0: ";
     private final static String X_I_VALUE_LABEL_TEXT = "xi: ";
     private final static String TIME_STEP_LABEL_TEXT = "Time step: ";
     private final static String END_TIME_LABEL_TEXT = "End time: ";
 
     private final JFrame mainFrame;
 
     private final JTextField firstEqXStartValueInput;
     private final JTextField firstEqXEndValueInput;
     private final JTextField firstEqXCoeffInput;
     private final JTextField firstEqFreeTermInput;
     private final JTextField secondEqXStartValueInput;
     private final JTextField secondEqXEndValueInput;
     private final JTextField secondEqXCoeffInput;
     private final JTextField secondEqFreeTermInput;
     private final JTextField x0ValueInput;
     private final JTextField xiValueInput;
     private final JTextField timeStepInput;
     private final JTextField endTimeInput;
 
     public MainWindow() {
         mainFrame = new JFrame("Lab 6");
         mainFrame.setSize(MAIN_FRAME_SIZE);
         mainFrame.setResizable(false);
         mainFrame.setLocationRelativeTo(null);
         mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         firstEqXEndValueInput = new JTextField("0.15");
         GUITools.fixTextFieldSize(firstEqXEndValueInput);
         firstEqXEndValueInput.setCaretPosition(0);
 
         firstEqXCoeffInput = new JTextField("0.066666667");
         GUITools.fixTextFieldSize(firstEqXCoeffInput);
         firstEqXCoeffInput.setCaretPosition(0);
 
         firstEqFreeTermInput = new JTextField("1.2");
         GUITools.fixTextFieldSize(firstEqFreeTermInput);
         firstEqFreeTermInput.setCaretPosition(0);
 
         firstEqXStartValueInput = new JTextField("0.0");
         GUITools.fixTextFieldSize(firstEqXStartValueInput);
         firstEqXStartValueInput.setCaretPosition(0);
 
         secondEqXEndValueInput = new JTextField("1.0");
         GUITools.fixTextFieldSize(secondEqXEndValueInput);
         secondEqXEndValueInput.setCaretPosition(0);
 
         secondEqXCoeffInput = new JTextField("-2.705882353");
         GUITools.fixTextFieldSize(secondEqXCoeffInput);
         secondEqXCoeffInput.setCaretPosition(0);
 
         secondEqFreeTermInput = new JTextField("3.905882353");
         GUITools.fixTextFieldSize(secondEqFreeTermInput);
         secondEqFreeTermInput.setCaretPosition(0);
 
         secondEqXStartValueInput = new JTextField("0.16");
         GUITools.fixTextFieldSize(secondEqXStartValueInput);
         secondEqXStartValueInput.setCaretPosition(0);
 
         x0ValueInput = new JTextField("1.2");
         GUITools.fixTextFieldSize(x0ValueInput);
         x0ValueInput.setCaretPosition(0);
 
         xiValueInput = new JTextField("3.5");
         GUITools.fixTextFieldSize(xiValueInput);
         xiValueInput.setCaretPosition(0);
 
        timeStepInput = new JTextField("0.001");
         GUITools.fixTextFieldSize(timeStepInput);
         timeStepInput.setCaretPosition(0);
 
        endTimeInput = new JTextField("0.1");
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
                 Map<BigDecimal, Double> xStartTemp = new HashMap<BigDecimal, Double>();
                 final int xValuesScale = 2;
 
                 /*double xCoeff = -2.705882353d;
                 double freeTerm = 3.905882353d;
                 double startX = 0.16d;
                 double endX = 1d;*/
                 double xCoeff = Double.parseDouble(firstEqXCoeffInput.getText());
                 double freeTerm = Double.parseDouble(firstEqFreeTermInput.getText());
                 double startX = Double.parseDouble(firstEqXStartValueInput.getText());
                 double endX = Double.parseDouble(firstEqXEndValueInput.getText());
                 final double xStep = 0.001d;
                 for (double currX = startX; currX <= endX; currX = currX + xStep) {
                     BigDecimal temp = new BigDecimal(currX);
                     xStartTemp.put(temp.setScale(xValuesScale, RoundingMode.HALF_UP), (xCoeff * currX) + freeTerm);
                 }
 
                 /*xCoeff = 0.066666667d;
                 freeTerm = 1.2d;
                 startX = 0d;
                 endX = 0.15d;*/
                 xCoeff = Double.parseDouble(secondEqXCoeffInput.getText());
                 freeTerm = Double.parseDouble(secondEqFreeTermInput.getText());
                 startX = Double.parseDouble(secondEqXStartValueInput.getText());
                 endX = Double.parseDouble(secondEqXEndValueInput.getText());
                 for (double currX = startX; currX <= endX; currX = currX + xStep) {
                     BigDecimal temp = new BigDecimal(currX);
                     xStartTemp.put(temp.setScale(xValuesScale, RoundingMode.HALF_UP), (xCoeff * currX) + freeTerm);
                 }
 
                 double x0 = Double.parseDouble(x0ValueInput.getText());
                 double xi = Double.parseDouble(xiValueInput.getText());
 
                 log.debug("Start data forming finished");
                 final double timeStep = Double.parseDouble(timeStepInput.getText());
                 final double endTime = Double.parseDouble(endTimeInput.getText());
                 ImplicitFiniteDifferenceMethod implicitFiniteDifferenceMethod = new
                         ImplicitFiniteDifferenceMethod(xStartTemp, xStep, timeStep, endTime, x0, xi, xValuesScale);
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
         JLabel firstEquationLabel = new JLabel(FIRST_EQUATION_LABEL_TEXT);
         tempHorPanel.add(firstEquationLabel);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_HALF_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel firstEqXStartValueLabel = new JLabel(X_START_VALUE_LABEL_TEXT);
         tempHorPanel.add(firstEqXStartValueLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(firstEqXStartValueInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel firstEqXEndValueLabel = new JLabel(X_END_VALUE_LABEL_TEXT);
         tempHorPanel.add(firstEqXEndValueLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(firstEqXEndValueInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel firstEqXCoeffLabel = new JLabel(X_COEFF_LABEL_TEXT);
         tempHorPanel.add(firstEqXCoeffLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(firstEqXCoeffInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_HALF_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel firstEqFreeTermLabel = new JLabel(FREE_TERM_LABEL_TEXT);
         tempHorPanel.add(firstEqFreeTermLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(firstEqFreeTermInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel secondEquationLabel = new JLabel(SECOND_EQUATION_LABEL_TEXT);
         tempHorPanel.add(secondEquationLabel);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_HALF_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel secondEqXStartValueLabel = new JLabel(X_START_VALUE_LABEL_TEXT);
         tempHorPanel.add(secondEqXStartValueLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(secondEqXStartValueInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel secondEqXEndValueLabel = new JLabel(X_END_VALUE_LABEL_TEXT);
         tempHorPanel.add(secondEqXEndValueLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(secondEqXEndValueInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel secondEqXCoeffLabel = new JLabel(X_COEFF_LABEL_TEXT);
         tempHorPanel.add(secondEqXCoeffLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(secondEqXCoeffInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_HALF_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel secondEqFreeTermLabel = new JLabel(FREE_TERM_LABEL_TEXT);
         tempHorPanel.add(secondEqFreeTermLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(secondEqFreeTermInput);
         inputsPanel.add(tempHorPanel);
 
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel x0ValueLabel = new JLabel(X_0_VALUE_LABEL_TEXT);
         tempHorPanel.add(x0ValueLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(x0ValueInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_HALF_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel xiValueLabel = new JLabel(X_I_VALUE_LABEL_TEXT);
         tempHorPanel.add(xiValueLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(xiValueInput);
         inputsPanel.add(tempHorPanel);
 
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel timeStepLabel = new JLabel(TIME_STEP_LABEL_TEXT);
         tempHorPanel.add(timeStepLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(timeStepInput);
         inputsPanel.add(tempHorPanel);
         inputsPanel.add(Box.createRigidArea(StandardDimension.VER_HALF_RIGID_AREA.getValue()));
 
         tempHorPanel = BoxLayoutUtils.createHorizontalPanel();
         JLabel endTimeLabel = new JLabel(END_TIME_LABEL_TEXT);
         tempHorPanel.add(endTimeLabel);
         tempHorPanel.add(Box.createRigidArea(StandardDimension.HOR_HALF_RIGID_AREA.getValue()));
         tempHorPanel.add(endTimeInput);
         inputsPanel.add(tempHorPanel);
 
         GUITools.makeSameSize(firstEqXEndValueLabel, firstEqXCoeffLabel, firstEqFreeTermLabel, firstEqXStartValueLabel,
                 secondEqFreeTermLabel, secondEqXCoeffLabel, secondEqXEndValueLabel, secondEqXStartValueLabel,
                 x0ValueLabel, xiValueLabel, timeStepLabel, endTimeLabel);
 
         return inputsPanel;
     }
 
     private void shutdown() {
         mainFrame.setVisible(false);
         mainFrame.dispose();
     }
 }
