 package gui;
 
 import java.awt.BorderLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.JSlider;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JSplitPane;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class Options extends JFrame implements ActionListener {
     private static final long serialVersionUID = 2948479686052444601L;
 
     private Parameters params;
     
     private CalculatorWindow parent;
 
     private JPanel contentPane;
     private JTextField xMinBox;
     private JTextField xMaxBox;
     private JTextField yMinBox;
     private JTextField yMaxBox;
     private JTextField iterationsBox;
     private JTextField secantStepBox;
     private JTextField esBox;
     private JTextField derivStepBox;
     private JTextField divisionsBox;
     private JTextField rombergBox;
     private JSlider resolutionSlider;
     private JButton btnDefaults;
     private JButton btnSave;
 
     /**
      * Launch the application.
      */
     /*
      * public static void main(String[] args) { EventQueue.invokeLater(new
      * Runnable() { public void run() { try { Options frame = new Options();
      * frame.setVisible(true); } catch (Exception e) { e.printStackTrace(); } }
      * }); }
      */
 
     /**
      * Create the frame.
      */
     public Options(CalculatorWindow parent) {
         setResizable(false);
         setTitle("Options");
         setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 277, 424);
         contentPane = new JPanel();
         contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
         setContentPane(contentPane);
         contentPane.setLayout(new BorderLayout(0, 0));
 
         JPanel panel = new JPanel();
         contentPane.add(panel);
         GridBagLayout gbl_panel = new GridBagLayout();
         gbl_panel.columnWidths = new int[] { 0, 257, 0 };
         gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0,
                 0, 0, 0, 0, 0, 0, 0 };
         gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
         gbl_panel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                 Double.MIN_VALUE };
         panel.setLayout(gbl_panel);
 
         JLabel lblGraphing = new JLabel("Graphing:");
         GridBagConstraints gbc_lblGraphing = new GridBagConstraints();
         gbc_lblGraphing.anchor = GridBagConstraints.WEST;
         gbc_lblGraphing.insets = new Insets(0, 0, 5, 5);
         gbc_lblGraphing.gridx = 0;
         gbc_lblGraphing.gridy = 0;
         panel.add(lblGraphing, gbc_lblGraphing);
 
         JSplitPane splitPane = new JSplitPane();
         GridBagConstraints gbc_splitPane = new GridBagConstraints();
         gbc_splitPane.insets = new Insets(0, 0, 5, 0);
         gbc_splitPane.fill = GridBagConstraints.BOTH;
         gbc_splitPane.gridx = 1;
         gbc_splitPane.gridy = 0;
         panel.add(splitPane, gbc_splitPane);
 
         btnDefaults = new JButton("Defaults");
         splitPane.setLeftComponent(btnDefaults);
 
         btnSave = new JButton("Save");
         splitPane.setRightComponent(btnSave);
 
         JLabel lblPlotResolution = new JLabel("Plot Resolution");
         GridBagConstraints gbc_lblPlotResolution = new GridBagConstraints();
         gbc_lblPlotResolution.anchor = GridBagConstraints.EAST;
         gbc_lblPlotResolution.insets = new Insets(0, 0, 5, 5);
         gbc_lblPlotResolution.gridx = 0;
         gbc_lblPlotResolution.gridy = 1;
         panel.add(lblPlotResolution, gbc_lblPlotResolution);
 
         resolutionSlider = new JSlider();
         resolutionSlider
                 .setToolTipText("Pixel distance between function calls for plots. Smaller looks better, but is slower.");
         resolutionSlider.setValue(8);
         resolutionSlider.setPaintLabels(true);
         resolutionSlider.setMaximum(64);
         resolutionSlider.setMinimum(1);
         GridBagConstraints gbc_resolutionSlider = new GridBagConstraints();
         gbc_resolutionSlider.fill = GridBagConstraints.BOTH;
         gbc_resolutionSlider.insets = new Insets(0, 0, 5, 0);
         gbc_resolutionSlider.gridx = 1;
         gbc_resolutionSlider.gridy = 1;
         panel.add(resolutionSlider, gbc_resolutionSlider);
 
         JLabel lblDefaultXMin = new JLabel("Default X min");
         GridBagConstraints gbc_lblDefaultXMin = new GridBagConstraints();
         gbc_lblDefaultXMin.anchor = GridBagConstraints.EAST;
         gbc_lblDefaultXMin.insets = new Insets(0, 0, 5, 5);
         gbc_lblDefaultXMin.gridx = 0;
         gbc_lblDefaultXMin.gridy = 2;
         panel.add(lblDefaultXMin, gbc_lblDefaultXMin);
 
         xMinBox = new JTextField();
         GridBagConstraints gbc_xMinBox = new GridBagConstraints();
         gbc_xMinBox.insets = new Insets(0, 0, 5, 0);
         gbc_xMinBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_xMinBox.gridx = 1;
         gbc_xMinBox.gridy = 2;
         panel.add(xMinBox, gbc_xMinBox);
         xMinBox.setColumns(10);
 
         JLabel lblDefaultXMax = new JLabel("Default X max");
         GridBagConstraints gbc_lblDefaultXMax = new GridBagConstraints();
         gbc_lblDefaultXMax.anchor = GridBagConstraints.EAST;
         gbc_lblDefaultXMax.insets = new Insets(0, 0, 5, 5);
         gbc_lblDefaultXMax.gridx = 0;
         gbc_lblDefaultXMax.gridy = 3;
         panel.add(lblDefaultXMax, gbc_lblDefaultXMax);
 
         xMaxBox = new JTextField();
         GridBagConstraints gbc_xMaxBox = new GridBagConstraints();
         gbc_xMaxBox.insets = new Insets(0, 0, 5, 0);
         gbc_xMaxBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_xMaxBox.gridx = 1;
         gbc_xMaxBox.gridy = 3;
         panel.add(xMaxBox, gbc_xMaxBox);
         xMaxBox.setColumns(10);
 
         JLabel lblDefaultYMin = new JLabel("Default Y min");
         GridBagConstraints gbc_lblDefaultYMin = new GridBagConstraints();
         gbc_lblDefaultYMin.anchor = GridBagConstraints.EAST;
         gbc_lblDefaultYMin.insets = new Insets(0, 0, 5, 5);
         gbc_lblDefaultYMin.gridx = 0;
         gbc_lblDefaultYMin.gridy = 4;
         panel.add(lblDefaultYMin, gbc_lblDefaultYMin);
 
         yMinBox = new JTextField();
         GridBagConstraints gbc_yMinBox = new GridBagConstraints();
         gbc_yMinBox.insets = new Insets(0, 0, 5, 0);
         gbc_yMinBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_yMinBox.gridx = 1;
         gbc_yMinBox.gridy = 4;
         panel.add(yMinBox, gbc_yMinBox);
         yMinBox.setColumns(10);
 
         JLabel lblDefaultYMax = new JLabel("Default Y max");
         GridBagConstraints gbc_lblDefaultYMax = new GridBagConstraints();
         gbc_lblDefaultYMax.anchor = GridBagConstraints.EAST;
         gbc_lblDefaultYMax.insets = new Insets(0, 0, 5, 5);
         gbc_lblDefaultYMax.gridx = 0;
         gbc_lblDefaultYMax.gridy = 5;
         panel.add(lblDefaultYMax, gbc_lblDefaultYMax);
 
         yMaxBox = new JTextField();
         GridBagConstraints gbc_yMaxBox = new GridBagConstraints();
         gbc_yMaxBox.insets = new Insets(0, 0, 5, 0);
         gbc_yMaxBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_yMaxBox.gridx = 1;
         gbc_yMaxBox.gridy = 5;
         panel.add(yMaxBox, gbc_yMaxBox);
         yMaxBox.setColumns(10);
 
         JSeparator separator = new JSeparator();
         GridBagConstraints gbc_separator = new GridBagConstraints();
         gbc_separator.insets = new Insets(0, 0, 5, 5);
         gbc_separator.gridx = 0;
         gbc_separator.gridy = 6;
         panel.add(separator, gbc_separator);
 
         JLabel lblZeros = new JLabel("Zeros:");
         GridBagConstraints gbc_lblZeros = new GridBagConstraints();
         gbc_lblZeros.anchor = GridBagConstraints.WEST;
         gbc_lblZeros.insets = new Insets(0, 0, 5, 5);
         gbc_lblZeros.gridx = 0;
         gbc_lblZeros.gridy = 7;
         panel.add(lblZeros, gbc_lblZeros);
 
         JLabel lblMaxIterations = new JLabel("Max Iterations");
         GridBagConstraints gbc_lblMaxIterations = new GridBagConstraints();
         gbc_lblMaxIterations.anchor = GridBagConstraints.EAST;
         gbc_lblMaxIterations.insets = new Insets(0, 0, 5, 5);
         gbc_lblMaxIterations.gridx = 0;
         gbc_lblMaxIterations.gridy = 8;
         panel.add(lblMaxIterations, gbc_lblMaxIterations);
 
         iterationsBox = new JTextField();
         GridBagConstraints gbc_iterationsBox = new GridBagConstraints();
         gbc_iterationsBox.insets = new Insets(0, 0, 5, 0);
         gbc_iterationsBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_iterationsBox.gridx = 1;
         gbc_iterationsBox.gridy = 8;
         panel.add(iterationsBox, gbc_iterationsBox);
         iterationsBox.setColumns(10);
 
         JLabel lblModSecantStep = new JLabel("Mod. Secant Step");
         GridBagConstraints gbc_lblModSecantStep = new GridBagConstraints();
         gbc_lblModSecantStep.anchor = GridBagConstraints.EAST;
         gbc_lblModSecantStep.insets = new Insets(0, 0, 5, 5);
         gbc_lblModSecantStep.gridx = 0;
         gbc_lblModSecantStep.gridy = 9;
         panel.add(lblModSecantStep, gbc_lblModSecantStep);
 
         secantStepBox = new JTextField();
         GridBagConstraints gbc_secantStepBox = new GridBagConstraints();
         gbc_secantStepBox.insets = new Insets(0, 0, 5, 0);
         gbc_secantStepBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_secantStepBox.gridx = 1;
         gbc_secantStepBox.gridy = 9;
         panel.add(secantStepBox, gbc_secantStepBox);
         secantStepBox.setColumns(10);
 
         JLabel lblStoppingThresholdes = new JLabel("Es");
         GridBagConstraints gbc_lblStoppingThresholdes = new GridBagConstraints();
         gbc_lblStoppingThresholdes.anchor = GridBagConstraints.EAST;
         gbc_lblStoppingThresholdes.insets = new Insets(0, 0, 5, 5);
         gbc_lblStoppingThresholdes.gridx = 0;
         gbc_lblStoppingThresholdes.gridy = 10;
         panel.add(lblStoppingThresholdes, gbc_lblStoppingThresholdes);
 
         esBox = new JTextField();
         GridBagConstraints gbc_esBox = new GridBagConstraints();
         gbc_esBox.insets = new Insets(0, 0, 5, 0);
         gbc_esBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_esBox.gridx = 1;
         gbc_esBox.gridy = 10;
         panel.add(esBox, gbc_esBox);
         esBox.setColumns(10);
 
         JSeparator separator_1 = new JSeparator();
         GridBagConstraints gbc_separator_1 = new GridBagConstraints();
         gbc_separator_1.insets = new Insets(0, 0, 5, 5);
         gbc_separator_1.gridx = 0;
         gbc_separator_1.gridy = 11;
         panel.add(separator_1, gbc_separator_1);
 
         JLabel lblDerivatives = new JLabel("Derivatives:");
         GridBagConstraints gbc_lblDerivatives = new GridBagConstraints();
         gbc_lblDerivatives.anchor = GridBagConstraints.WEST;
         gbc_lblDerivatives.insets = new Insets(0, 0, 5, 5);
         gbc_lblDerivatives.gridx = 0;
         gbc_lblDerivatives.gridy = 12;
         panel.add(lblDerivatives, gbc_lblDerivatives);
 
         JLabel lblStep = new JLabel("Step");
         GridBagConstraints gbc_lblStep = new GridBagConstraints();
         gbc_lblStep.anchor = GridBagConstraints.EAST;
         gbc_lblStep.insets = new Insets(0, 0, 5, 5);
         gbc_lblStep.gridx = 0;
         gbc_lblStep.gridy = 13;
         panel.add(lblStep, gbc_lblStep);
 
         derivStepBox = new JTextField();
         GridBagConstraints gbc_derivStepBox = new GridBagConstraints();
         gbc_derivStepBox.insets = new Insets(0, 0, 5, 0);
         gbc_derivStepBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_derivStepBox.gridx = 1;
         gbc_derivStepBox.gridy = 13;
         panel.add(derivStepBox, gbc_derivStepBox);
         derivStepBox.setColumns(10);
 
         JSeparator separator_2 = new JSeparator();
         GridBagConstraints gbc_separator_2 = new GridBagConstraints();
         gbc_separator_2.insets = new Insets(0, 0, 5, 5);
         gbc_separator_2.gridx = 0;
         gbc_separator_2.gridy = 14;
         panel.add(separator_2, gbc_separator_2);
 
         JLabel lblIntegrals = new JLabel("Integrals:");
         GridBagConstraints gbc_lblIntegrals = new GridBagConstraints();
         gbc_lblIntegrals.anchor = GridBagConstraints.WEST;
         gbc_lblIntegrals.insets = new Insets(0, 0, 5, 5);
         gbc_lblIntegrals.gridx = 0;
         gbc_lblIntegrals.gridy = 15;
         panel.add(lblIntegrals, gbc_lblIntegrals);
 
         JLabel lblDivisions = new JLabel("Divisions");
         GridBagConstraints gbc_lblDivisions = new GridBagConstraints();
         gbc_lblDivisions.anchor = GridBagConstraints.EAST;
         gbc_lblDivisions.insets = new Insets(0, 0, 5, 5);
         gbc_lblDivisions.gridx = 0;
         gbc_lblDivisions.gridy = 16;
         panel.add(lblDivisions, gbc_lblDivisions);
 
         divisionsBox = new JTextField();
         GridBagConstraints gbc_divisionsBox = new GridBagConstraints();
         gbc_divisionsBox.insets = new Insets(0, 0, 5, 0);
         gbc_divisionsBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_divisionsBox.gridx = 1;
         gbc_divisionsBox.gridy = 16;
         panel.add(divisionsBox, gbc_divisionsBox);
         divisionsBox.setColumns(10);
 
         JLabel lblRombergLevel = new JLabel("Romberg Level");
         GridBagConstraints gbc_lblRombergLevel = new GridBagConstraints();
         gbc_lblRombergLevel.insets = new Insets(0, 0, 0, 5);
         gbc_lblRombergLevel.anchor = GridBagConstraints.EAST;
         gbc_lblRombergLevel.gridx = 0;
         gbc_lblRombergLevel.gridy = 17;
         panel.add(lblRombergLevel, gbc_lblRombergLevel);
 
         rombergBox = new JTextField();
         GridBagConstraints gbc_rombergBox = new GridBagConstraints();
         gbc_rombergBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_rombergBox.gridx = 1;
         gbc_rombergBox.gridy = 17;
         panel.add(rombergBox, gbc_rombergBox);
         rombergBox.setColumns(10);
 
         this.parent = parent;
         myInitialize();
     }
 
     private void myInitialize() {
         params = new Parameters();
 
         btnSave.setActionCommand("SAVE");
         btnSave.addActionListener(this);
 
         btnDefaults.setActionCommand("DEFAULTS");
         btnDefaults.addActionListener(this);
     }
 
     public void setVisible(boolean val) {
         super.setVisible(val);
         if (val) {
             populateControls();
         } else {
             saveControls();
         }
     }
 
     public double getParam(String param) {
         return params.getParam(param);
     }
 
     private void populateControls() {
         resolutionSlider.setValue((int) params.getParam("PlotResolution"));
         xMinBox.setText(Double.toString(params.getParam("Xmin")));
         xMaxBox.setText(Double.toString(params.getParam("Xmax")));
         yMinBox.setText(Double.toString(params.getParam("Ymin")));
         yMaxBox.setText(Double.toString(params.getParam("Ymax")));
         iterationsBox.setText(Integer.toString((int) params
                 .getParam("MaxIterations")));
         secantStepBox.setText(Double.toString(params
                 .getParam("ModifiedSecantStep")));
         esBox.setText(Double.toString(params.getParam("StoppingThreshold")));
         derivStepBox.setText(Double.toString(params.getParam("DerivStep")));
         divisionsBox
                 .setText(Integer.toString((int) params.getParam("Division")));
         rombergBox.setText(Integer.toString((int) params.getParam("Romberg")));
     }
 
     private void saveControls() {
         params.setParam("PlotResolution", resolutionSlider.getValue());
         trySet("Xmin", xMinBox.getText());
         trySet("Xmax", xMaxBox.getText());
         trySet("Ymin", yMinBox.getText());
         trySet("Ymax", yMaxBox.getText());
         trySet("MaxIterations", iterationsBox.getText());
         trySet("ModifiedSecantStep", secantStepBox.getText());
         trySet("StoppingThreshold", esBox.getText());
         trySet("DerivStep", derivStepBox.getText());
         trySet("Division", divisionsBox.getText());
         trySet("Romberg", rombergBox.getText());
         parent.updateOptions();
     }
 
     private void trySet(String param, String value) {
         try {
             Double val = Double.parseDouble(value);
             params.setParam(param, val);
         } catch (NumberFormatException e) {
         }
     }
 
     public void actionPerformed(ActionEvent arg0) {
         String name = arg0.getActionCommand();
 
         if (name.equals("DEFAULTS")) {
             params.defaults();
             parent.updateOptions();
             populateControls();
         } else if (name.equals("SAVE")) {
             saveControls();
             populateControls();
         }
     }
 }
