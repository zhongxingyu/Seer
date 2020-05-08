 /*
  * Andrew Darwin
  * www.adarwin.com
  * github.com/adarwin
  * SUNY Oswego
  */
 
 package com.adarwin.edrum;
 
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.Graphics;
 import javax.swing.JComponent;
 import java.awt.Point;
 import javax.swing.JSlider;
 import javax.swing.SwingConstants;
 import java.awt.Color;
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import java.awt.GridBagConstraints;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowEvent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import java.util.ArrayList;
 
 class GraphDialog extends JDialog {
     private static final long serialVersionUID = 1L;
     private GraphPanel graphPanel;
     private JPanel contentPane;
     private JPanel optionsPane;
     private JPanel eastPane, westPane;
     private JSlider thresholdSlider, sensitivitySlider, timeoutSlider;
     private JButton testButton, resetButton;
 
     private static int SENSITIVITY_MAX = 800;
     private static int SENSITIVITY_DEFAULT = 200;
     private static int SENSITIVITY_MIN = 100;
     private static int THRESHOLD_MAX = 100;
     private static int THRESHOLD_DEFAULT = 30;
     private static int THRESHOLD_MIN = 0;
     private static int TIMEOUT_MIN = 0;
     private static int TIMEOUT_DEFAULT = 1000;
     private static int TIMEOUT_MAX = 10000;
 
     private final int TOP_MARGIN = 30;
     private final int BOTTOM_MARGIN = 10;
     private final int SIDE_MARGIN = 5;
     private final double WEIGHTX_1 = 1.0;
     private final double WEIGHTX_0 = 0.0;
     private final double WEIGHTY_0 = 0.0;
     private final int WIDTH_1 = 1;
     private final int WIDTH_2 = 2;
     private final int HEIGHT_1 = 1;
     private final int ANCHOR_CENTER = GridBagConstraints.CENTER;
     private final int ANCHOR_EAST = GridBagConstraints.EAST;
     private final int ANCHOR_WEST = GridBagConstraints.WEST;
     private final int FILL_NONE = GridBagConstraints.NONE;
     private final int FILL_HORIZONTAL = GridBagConstraints.HORIZONTAL;
     private final int FILL_VERTICAL = GridBagConstraints.VERTICAL;
     private final int COL_0 = 0;
     private final int COL_1 = 1;
     private final int COL_2 = 2;
     private final int COL_3 = 3;
 
     public GraphDialog(JFrame frame) {
         super(frame, "Stroke Graph", false);
         setSize(new Dimension(800, 500));
         setLocationRelativeTo(frame);
         initializeVariables();
         buildContainers();
         configureLayouts();
         buildComponents();
         addComponentsToContainers();
         addWindowListeners();
         setVisible(true);
     }
 
     private void initializeVariables() {
     }
 
     private void buildContainers() {
         contentPane = new JPanel();
         graphPanel = new GraphPanel();
         optionsPane = new JPanel();
         eastPane = new JPanel();
         westPane = new JPanel();
     }
 
    protected void addStrokeDatum(int value) {
        graphPanel.addStrokeDatum(value);
    }

     private void configureLayouts() {
         contentPane.setLayout(new BorderLayout());
         optionsPane.setLayout(new GridBagLayout());
         eastPane.setLayout(new FlowLayout());
 
     }
 
     private void buildComponents() {
         testButton = new JButton("Test Button");
         resetButton = new JButton("Reset to Defaults");
         thresholdSlider = new JSlider(SwingConstants.HORIZONTAL,
                                       THRESHOLD_MIN, THRESHOLD_MAX,
                                       THRESHOLD_DEFAULT);
         thresholdSlider.setPaintTicks(true);
         thresholdSlider.setPaintLabels(true);
         thresholdSlider.setSnapToTicks(true);
         thresholdSlider.setMajorTickSpacing(10);
         thresholdSlider.setMinorTickSpacing(5);
 
         sensitivitySlider = new JSlider(SwingConstants.HORIZONTAL,
                                         SENSITIVITY_MIN, SENSITIVITY_MAX,
                                         SENSITIVITY_DEFAULT);
         sensitivitySlider.setPaintTicks(true);
         sensitivitySlider.setPaintLabels(true);
         sensitivitySlider.setSnapToTicks(true);
         sensitivitySlider.setMajorTickSpacing(100);
         sensitivitySlider.setMinorTickSpacing(25);
         timeoutSlider = new JSlider(SwingConstants.HORIZONTAL,
                                     TIMEOUT_MIN, TIMEOUT_MAX,
                                     TIMEOUT_DEFAULT);
         timeoutSlider.setPaintTicks(true);
         timeoutSlider.setPaintLabels(true);
         timeoutSlider.setSnapToTicks(true);
         timeoutSlider.setMajorTickSpacing(1000);
         timeoutSlider.setMinorTickSpacing(500);
     }
 
     private void addComponentsToContainers() {
         contentPane.add(graphPanel, BorderLayout.CENTER);
         contentPane.add(optionsPane, BorderLayout.SOUTH);
         contentPane.add(eastPane, BorderLayout.EAST);
         contentPane.add(westPane, BorderLayout.WEST);
 
         // Add row 1
         int row = addSliderToOptionsPane("Threshold (% of each " +
                                          "stroke velocity)", thresholdSlider,
                                          0, THRESHOLD_MIN + "%",
                                          THRESHOLD_MAX + "%");
 
         // Add row 2
         row = addSliderToOptionsPane("Sensitivity (peak input voltage value)", sensitivitySlider, ++row,
                                      Integer.toString(SENSITIVITY_MIN),
                                      Integer.toString(SENSITIVITY_MAX));
 
         // Add row 3
         row = addSliderToOptionsPane("Timeout (µs)", timeoutSlider, row,
                                      Integer.toString(TIMEOUT_MIN),
                                      Integer.toString(TIMEOUT_MAX));
 
         // Add row 4
         optionsPane.add(resetButton, new GridBagConstraints(COL_2, row++,
                                      WIDTH_2, HEIGHT_1,
                                      WEIGHTX_0, WEIGHTY_0,
                                      ANCHOR_EAST,
                                      FILL_NONE,
                                      new Insets(10, 0, 5, 5),
                                      0, 0));
         //optionsPane.add(resetButton, gbc);
 
         setContentPane(contentPane);
     }
 
     private int addSliderToOptionsPane(String name, JSlider slider, int row,
                                         String minValue, String maxValue) {
 
         GridBagConstraints gbc;
         gbc = new GridBagConstraints(COL_1, row++, WIDTH_2, HEIGHT_1,
                                      WEIGHTX_1, WEIGHTY_0,
                                      ANCHOR_CENTER,
                                      FILL_NONE,
                                      new Insets(TOP_MARGIN, 0, 0, 0),
                                      0, 0);
         optionsPane.add(new JLabel(name, SwingConstants.CENTER), gbc);
 
         gbc = new GridBagConstraints(COL_0, row, WIDTH_1, HEIGHT_1,
                                      WEIGHTX_0, WEIGHTY_0,
                                      ANCHOR_EAST,
                                      FILL_NONE,
                                      new Insets(0, 0, 0, SIDE_MARGIN),
                                      10, 0);
         optionsPane.add(new JLabel(minValue, SwingConstants.RIGHT),
                         gbc);
 
         gbc = new GridBagConstraints(COL_1, row, WIDTH_2, HEIGHT_1,
                                      WEIGHTX_1, WEIGHTY_0,
                                      ANCHOR_CENTER,
                                      FILL_HORIZONTAL,
                                      new Insets(0, 0, 0, 0),
                                      0, 0);
         optionsPane.add(slider, gbc);
 
         gbc = new GridBagConstraints(COL_3, row, WIDTH_1, HEIGHT_1,
                                      WEIGHTX_0, WEIGHTY_0,
                                      ANCHOR_WEST,
                                      FILL_NONE,
                                      new Insets(0, SIDE_MARGIN, 0, 0),
                                      10, 0);
         optionsPane.add(new JLabel(maxValue, SwingConstants.LEFT),
                         gbc);
         return ++row;
     }
 
 
     private void addToOptionsPane(JComponent component, int x, int y,
                                   int width, int height, double weightx,
                                   int anchor, int fill, int ipadx, int ipady,
                                   Insets insets) {
         double weighty = 0.0;
         GridBagConstraints gbc = new GridBagConstraints(x, y, width, height,
                                                         weightx, weighty,
                                                         anchor, fill, insets,
                                                         ipadx, ipady);
         optionsPane.add(component, gbc);
     }
 
 
     private void constructContentPane() {
     }
 
     private void addWindowListeners() {
         addWindowListener(new WindowListener() {
             public void windowActivated(WindowEvent e) {
                 AceDrums.requestGraphMode(true);
             }
             public void windowClosed(WindowEvent e) { }
             public void windowClosing(WindowEvent e) { }
             public void windowDeactivated(WindowEvent e) {
                 AceDrums.requestGraphMode(false);
             }
             public void windowDeiconified(WindowEvent e) { }
             public void windowIconified(WindowEvent e) { }
             public void windowOpened(WindowEvent e) { }
         });
     }
 }
