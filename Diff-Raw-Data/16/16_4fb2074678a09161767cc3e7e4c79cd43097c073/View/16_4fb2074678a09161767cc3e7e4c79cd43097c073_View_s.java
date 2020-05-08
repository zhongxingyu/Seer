 /*
  * The MIT License
  *
  * Copyright 2013 Bernhard Sirlinger.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package mss;
 
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.nio.DoubleBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicReference;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import mss.integratoren.Integratoren;
 import mss.integratoren.Rechenmodul;
 import mss.util.DataFileSaver;
 import mss.util.Notifications;
 import mss.util.Planet;
 import mss.util.ProjectFileSaver;
 import mss.util.ScreenshotSaver;
 import mss.util.Util;
 import mss.util.Vektor2D;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.Color;
 
 /**
  *
  * @author Bernhard Sirlinger
  */
 public class View implements Observer, Runnable {
 
     private static boolean closeRequested = false;
     private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<>();
     private final Rechenmodul modul;
     private Thread rechenThread;
     private long speed = 100;
     private int currentIndex = 0;
     private double deltaT = 0.01;
 
     private enum ChangeType {
 
         INCREASE, DECREASE
     };
 
     private enum Directions {
 
         UP, DOWN, LEFT, RIGHT
     };
 
     private ArrayList<ArrayList<Planet>> results;
     private ArrayList<Planet> planets;
     private ArrayList<Planet> startPlanets;
 
     private long time;
     private String title;
     private final Canvas canvas = new Canvas();
     private final JFrame frame;
     private final JPanel panel = new JPanel();
     private final JSlider slider = new JSlider();
     private final JButton pauseButton;
     private final JButton playButton;
     private final JButton startCalculationButton;
     private final JButton resetButton;
     private final JButton takeScreenshotButton;
     private final JButton saveProjectButton;
     private final JButton saveDataButton;
     private final JButton zoomInButton;
     private final JButton zoomOutButton;
 
     private final JTabbedPane tabbedPane;
     private final JPanel planetsPanel = new JPanel();
     private final JPanel settingsPanel = new JPanel();
 
     private final JComboBox<String> planetsBox;
 
     private final JLabel errorLabel;
     private final JLabel vLabel;
     private final JLabel textLabel;
     private final JTextField labelField;
     private final JTextField vxField;
     private final JTextField vyField;
     private final JLabel coordsLabel;
     private final JTextField xField;
     private final JTextField yField;
     private final JLabel massLabel;
     private final JTextField massField;
     private final JLabel radixLabel;
     private final JTextField radixField;
     private final JLabel colorPreviewLabel;
     private final JColorChooser colorChooser;
     private final JPanel colorChooserPreview;
     private final JButton addPlanet;
     private final JButton removePlanet;
     private final JButton removeAllPlanets;
 
     private final JLabel integratorLabel;
     private final JComboBox<Integratoren> integratorBox;
     private final JLabel deltatLabel;
     private final JTextField deltatField;
 
     private boolean isPaused = true;
     private int zoomLevel = 1;
     private final DoubleBuffer buffer;
     private boolean wasInitialized = false;
     private boolean shouldReInit = false;
     private boolean shouldTakeScreenshot = false;
     private boolean isAddingNewPlanet = false;
 
     private String lastOpenedFilePath = "";
     private String lastSavedFilePath = "";
     private String lastSavedDataFilePath = "";
     private final String standardBoxEntry;
     private final String newPlanetBoxEntry;
     private final JMenuBar menuBar;
 
     /**
      *
      * @param title
      */
     public View(String title) {
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
         try {
             UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
         }
         System.out.println((-0.1 + 3.775294016443222E-11));
         this.planets = new ArrayList<>();
         //this.planets.add(new Planet("Sun", new Vektor2D(0, 3), 1e10, 1, new Vektor2D(0, 0), new org.lwjgl.util.Color(255, 255, 255)));
         //this.planets.add(new Planet("Planet", new Vektor2D(0, 0), 100, 0.5, new Vektor2D(-0.05, 0.05), new org.lwjgl.util.Color(244, 233, 10)));
         //this.planets.add(new Planet("Planet2", new Vektor2D(0, -3), 1e10, 1, new Vektor2D(), new org.lwjgl.util.Color(255, 255, 255)));
         this.startPlanets = (ArrayList<Planet>) this.planets.clone();
 
         this.modul = new Rechenmodul(Integratoren.RUNGE_KUTTA_KLASSISCH, 0.01);
 
         this.buffer = BufferUtils.createDoubleBuffer(16);
         this.initBuffer();
 
         this.title = title;
         this.frame = new JFrame(title);
 
         this.startCalculationButton = new JButton("Calculate");
         this.playButton = new JButton("Play");
         this.pauseButton = new JButton("Pause");
         this.resetButton = new JButton("Reset");
         this.takeScreenshotButton = new JButton("Take Screenshot");
         this.saveProjectButton = new JButton("Save Project");
         this.saveDataButton = new JButton("Save Computed Data");
         this.zoomInButton = new JButton("+");
         this.zoomOutButton = new JButton("-");
 
         /* Prevent Focus jumping */
         this.startCalculationButton.setFocusable(false);
         this.playButton.setFocusable(false);
         this.pauseButton.setFocusable(false);
         this.resetButton.setFocusable(false);
         this.takeScreenshotButton.setFocusable(false);
         this.saveProjectButton.setFocusable(false);
         this.saveDataButton.setFocusable(false);
         this.zoomInButton.setFocusable(false);
         this.zoomOutButton.setFocusable(false);
 
         this.slider.setEnabled(false);
         this.slider.setMinimumSize(new Dimension(800, this.slider.getHeight()));
 
         this.panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
         this.panel.setPreferredSize(new Dimension(800, 100));
         this.panel.setBackground(java.awt.Color.LIGHT_GRAY);
 
         this.panel.add(this.slider);
         this.panel.add(this.startCalculationButton);
         this.panel.add(this.playButton);
         this.panel.add(this.pauseButton);
         this.panel.add(this.resetButton);
         this.panel.add(this.takeScreenshotButton);
         this.panel.add(this.saveProjectButton);
         this.panel.add(this.saveDataButton);
         this.panel.add(this.zoomInButton);
         this.panel.add(this.zoomOutButton);
 
         this.tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
         this.tabbedPane.setPreferredSize(new Dimension(300, this.tabbedPane.getHeight()));
         this.tabbedPane.addTab("Start Values", this.planetsPanel);
         this.tabbedPane.addTab("Settings", this.settingsPanel);
 
         this.planetsPanel.setLayout(new GridBagLayout());
 
         this.standardBoxEntry = "Choose a Planet...";
         this.newPlanetBoxEntry = "Add New Planet...";
 
         this.planetsBox = new JComboBox<>();
         this.planetsBox.addItem(this.standardBoxEntry);
         this.planetsBox.addItem(this.newPlanetBoxEntry);
 
         this.errorLabel = new JLabel();
         this.textLabel = new JLabel("Label:");
         this.textLabel.setHorizontalAlignment(SwingConstants.CENTER);
         this.textLabel.setVerticalAlignment(SwingConstants.CENTER);
         this.labelField = new JTextField();
         this.vLabel = new JLabel("v in m/s :");
         this.vLabel.setHorizontalAlignment(SwingConstants.CENTER);
         this.vLabel.setVerticalAlignment(SwingConstants.CENTER);
         this.vxField = new JTextField();
         this.vyField = new JTextField();
         this.coordsLabel = new JLabel("Coordinates: ");
         this.coordsLabel.setHorizontalAlignment(SwingConstants.CENTER);
         this.coordsLabel.setVerticalAlignment(SwingConstants.CENTER);
         this.xField = new JTextField();
         this.yField = new JTextField();
         this.massLabel = new JLabel("Mass: ");
         this.massLabel.setHorizontalAlignment(SwingConstants.CENTER);
         this.massLabel.setVerticalAlignment(SwingConstants.CENTER);
         this.massField = new JTextField();
         this.radixLabel = new JLabel("Radix: ");
         this.radixLabel.setHorizontalAlignment(SwingConstants.CENTER);
         this.radixLabel.setVerticalAlignment(SwingConstants.CENTER);
         this.radixField = new JTextField();
         this.colorPreviewLabel = new JLabel("Color: ");
         this.colorChooserPreview = new JPanel();
         this.colorChooser = new JColorChooser();
 
         this.addPlanet = new JButton("Add Planet");
         this.addPlanet.setToolTipText("Add Planet");
         this.addPlanet.setFocusable(false);
         this.removePlanet = new JButton("Remove Planet");
         this.removePlanet.setToolTipText("Remove Planet");
         this.removePlanet.setFocusable(false);
         this.removeAllPlanets = new JButton("Remove all Planets");
         this.removeAllPlanets.setToolTipText("Remove all Planets");
         this.removeAllPlanets.setFocusable(false);
 
         /*Settings*/
         this.integratorLabel = new JLabel("Numerical Method:");
         this.integratorBox = new JComboBox<>();
         this.integratorBox.addItem(Integratoren.EULER);
         this.integratorBox.addItem(Integratoren.LEAPFROG);
         this.integratorBox.addItem(Integratoren.RUNGE_KUTTA_KLASSISCH);
         this.integratorBox.setSelectedIndex(2);
 
         this.deltatLabel = new JLabel("Delta t:");
         this.deltatField = new JTextField("" + this.deltaT);
 
         this.settingsPanel.add(this.integratorLabel);
         this.settingsPanel.add(this.integratorBox);
         this.settingsPanel.add(this.deltatLabel);
         this.settingsPanel.add(this.deltatField);
         /*Settings*/
 
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         c.gridwidth = 3;
         c.weightx = 1.0;
         c.weighty = 0.1;
         c.anchor = GridBagConstraints.FIRST_LINE_START;
         this.planetsPanel.add(this.planetsBox, c);
         c.gridy = 1;
         c.gridwidth = 1;
         c.weighty = 0.0;
         this.planetsPanel.add(this.textLabel, c);
         c.gridx = 1;
         c.gridwidth = 2;
         this.planetsPanel.add(this.labelField, c);
         c.gridy = 2;
         c.gridx = 0;
         c.gridwidth = 1;
         this.planetsPanel.add(this.vLabel, c);
         c.gridx = 1;
         this.planetsPanel.add(this.vxField, c);
         c.gridx = 2;
         this.planetsPanel.add(this.vyField, c);
         c.gridy = 3;
         c.gridx = 0;
         this.planetsPanel.add(this.coordsLabel, c);
         c.gridx = 1;
         this.planetsPanel.add(this.xField, c);
         c.gridx = 2;
         this.planetsPanel.add(this.yField, c);
         c.gridy = 4;
         c.gridx = 0;
         this.planetsPanel.add(this.massLabel, c);
         c.gridx = 1;
         this.planetsPanel.add(this.massField, c);
         c.gridy = 5;
         c.gridx = 0;
         this.planetsPanel.add(this.radixLabel, c);
         c.gridx = 1;
         this.planetsPanel.add(this.radixField, c);
         c.gridy = 6;
         c.gridx = 0;
         this.planetsPanel.add(this.addPlanet, c);
         c.gridx = 1;
         this.planetsPanel.add(this.removePlanet, c);
         c.gridx = 2;
         this.planetsPanel.add(this.removeAllPlanets, c);
 
         this.addListeners();
         this.addPlanetsUIListeners();
 
         this.menuBar = new JMenuBar();
         this.menuBar.addFocusListener(new FocusAdapter() {
             @Override
             public void focusGained(FocusEvent e) {
                 System.out.println("Gained");
                 super.focusGained(e);
             }
         });
         this.frame.setJMenuBar(this.menuBar);
 
         JMenu fileMenu = new JMenu("File");
         JMenu helpMenu = new JMenu("Help");
 
         JMenuItem openFile = new JMenuItem("Open File");
         openFile.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 isPaused = true;
                 openFile();
                 canvas.requestFocus();
             }
         });
         fileMenu.add(openFile);
 
         JMenuItem pause = new JMenuItem("Pause");
         pause.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (!isPaused) {
                     isPaused = true;
                     ((JMenuItem) e.getSource()).setText("Restart");
                 } else {
                     isPaused = false;
                     ((JMenuItem) e.getSource()).setText("Pause");
                 }
                 canvas.requestFocus();
             }
         });
         fileMenu.add(pause);
 
         JMenuItem saveDataToFile = new JMenuItem("Save Current Data to File");
         saveDataToFile.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 saveData();
                 canvas.requestFocus();
             }
         });
         fileMenu.add(saveDataToFile);
 
         JMenuItem about = new JMenuItem("About");
         about.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ((JMenuItem) e.getSource()).setName("");
                 if (!isPaused) {
                     isPaused = true;
                     ((JMenuItem) e.getSource()).setName("selfPaused");
                 }
                 showAboutDialog();
                 canvas.requestFocus();
             }
         });
         helpMenu.add(about);
 
         this.menuBar.add(fileMenu);
         this.menuBar.add(helpMenu);
     }
 
     private void initBuffer() {
         this.buffer.put(0, this.zoomLevel);
         this.buffer.put(1, 0);
         this.buffer.put(2, 0);
         this.buffer.put(3, 0);
         this.buffer.put(4, 0);
         this.buffer.put(5, this.zoomLevel);
         this.buffer.put(6, 0);
         this.buffer.put(7, 0);
         this.buffer.put(8, 0);
         this.buffer.put(9, 0);
         this.buffer.put(10, this.zoomLevel);
         this.buffer.put(11, 0);
         this.buffer.put(12, 0);
         this.buffer.put(13, 0);
         this.buffer.put(14, 0);
         this.buffer.put(15, this.zoomLevel);
     }
 
     private void addListeners() {
         this.slider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 JSlider slider = (JSlider) e.getSource();
                 currentIndex = slider.getValue();
                 planets = results.get(currentIndex);
                 if (slider.isFocusOwner()) {
                     isPaused = true;
                 }
             }
         });
 
         this.canvas.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 newCanvasSize.set(new Dimension(canvas.getSize().width, canvas.getSize().height));
             }
         });
 
         this.frame.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 layout(e.getComponent().getWidth(), e.getComponent().getHeight());
                 if (wasInitialized) {
                     shouldReInit = true;
                 }
             }
         });
 
         this.frame.addWindowListener(new WindowAdapter() {
             private boolean wasAlreadyPaused;
 
             @Override
             public void windowActivated(WindowEvent e) {
                 if (!this.wasAlreadyPaused) {
                     isPaused = false;
                 }
                 canvas.requestFocusInWindow();
             }
 
             @Override
             public void windowDeactivated(WindowEvent e) {
                 if (!isPaused) {
                     isPaused = true;
                     this.wasAlreadyPaused = false;
                 } else {
                     this.wasAlreadyPaused = true;
                 }
                 super.windowLostFocus(e);
             }
 
             @Override
             public void windowClosing(WindowEvent e) {
                 closeRequested = true;
             }
         });
 
         this.playButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.requestFocusInWindow();
                 isPaused = false;
             }
         });
 
         this.pauseButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 isPaused = true;
             }
         });
 
         this.startCalculationButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.requestFocus();
                 modul.setData(startPlanets);
                 rechenThread = new Thread(modul);
                 rechenThread.setDaemon(true);
                 rechenThread.start();
                 isPaused = true;
             }
         });
 
         this.resetButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (results != null) {
                     planets = (ArrayList<Planet>) startPlanets.clone();
                     isPaused = true;
                     currentIndex = 0;
                     slider.setValue(0);
                 }
             }
         });
 
         this.takeScreenshotButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 shouldTakeScreenshot = true;
             }
         });
 
         this.saveProjectButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 saveProject();
             }
         });
 
         this.saveDataButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 saveData();
             }
         });
 
         this.zoomInButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 changeZoomFactor(ChangeType.INCREASE, false);
                 shouldReInit = true;
             }
         });
 
         this.zoomOutButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 changeZoomFactor(ChangeType.DECREASE, false);
                 shouldReInit = true;
             }
         });
 
         this.planetsBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 int index = ((JComboBox) e.getSource()).getSelectedIndex();
                 try {
                     isAddingNewPlanet = false;
                     if(index > 1) {
                         Planet temp = startPlanets.get(index - 2);
                         labelField.setText(temp.getLabel());
                         labelField.setEnabled(true);
                         vxField.setText("" + temp.getV().getX());
                         vxField.setEnabled(true);
                         vyField.setText("" + temp.getV().getY());
                         vyField.setEnabled(true);
                         xField.setText("" + temp.getCoords().getX());
                         xField.setEnabled(true);
                         yField.setText("" + temp.getCoords().getY());
                         yField.setEnabled(true);
                         massField.setText("" + temp.getMass());
                         massField.setEnabled(true);
                         radixField.setText("" + temp.getRadix());
                         radixField.setEnabled(true);
                         addPlanet.setEnabled(false);
                         removePlanet.setEnabled(true);
                     } else if(index == 1) {
                         isAddingNewPlanet = true;
                         labelField.setEnabled(true);
                         labelField.setText("");
                         vxField.setEnabled(true);
                         vxField.setText("");
                         vyField.setEnabled(true);
                         vyField.setText("");
                         xField.setEnabled(true);
                         xField.setText("");
                         yField.setEnabled(true);
                         yField.setText("");
                         massField.setEnabled(true);
                         massField.setText("");
                         radixField.setEnabled(true);
                         radixField.setText("");
                         addPlanet.setEnabled(true);
                         removePlanet.setEnabled(false);
                     } else {
                         labelField.setText("");
                         labelField.setEnabled(false);
                         vxField.setText("");
                         vxField.setEnabled(false);
                         vyField.setText("");
                         vyField.setEnabled(false);
                         xField.setText("");
                         xField.setEnabled(false);
                         yField.setText("");
                         yField.setEnabled(false);
                         massField.setText("");
                         massField.setEnabled(false);
                         radixField.setText("");
                         radixField.setEnabled(false);
                         addPlanet.setEnabled(false);
                         removePlanet.setEnabled(false);
                     }
                 } catch(java.lang.IllegalStateException ex) {
                 }
             }
         });
 
         this.integratorBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 modul.setIntegrator((Integratoren) ((JComboBox) e.getSource()).getSelectedItem());
             }
         });
 
         this.deltatField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 String currentValue = ((JTextField) e.getSource()).getText();
                 if (currentValue.contains(",")) {
                     currentValue = currentValue.replace(',', '.');
                 }
 
                 try {
                     deltaT = Double.parseDouble(currentValue);
                     modul.setDeltaT(deltaT);
                 } catch (NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for deltaT is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     deltatField.setText("" + deltaT);
                 }
             }
         });
 
         this.addPlanet.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String error = "",
                        label,
                        temp;
 
                 Vektor2D coords = new Vektor2D(),
                          v = new Vektor2D();
                 double mass = 0,
                        radix = 0;
 
                 label = labelField.getText().trim();
 
                 temp = xField.getText().trim();
                 if(!temp.isEmpty()) {
                     try {
                         coords.setX(Double.parseDouble(temp));
                     } catch(NumberFormatException ex) {
                         error += "The value \"" + temp + " \" for the x coordinate is not valid.\n";
                     }
                 }
 
                 temp = yField.getText().trim();
                 if(!temp.isEmpty()) {
                     try {
                         coords.setY(Double.parseDouble(temp));
                     } catch(NumberFormatException ex) {
                         error += "The value \"" + temp + " \" for the y coordinate is not valid.\n";
                     }
                 }
 
                 temp = massField.getText().trim();
                 if(!temp.isEmpty()) {
                     try {
                         mass = Double.parseDouble(temp);
                     } catch(NumberFormatException ex) {
                         error += "The value \"" + temp + " \" for the mass is not valid.\n";
                     }
                 }
 
                 temp = radixField.getText().trim();
                 if(!temp.isEmpty()) {
                     try {
                         radix = Double.parseDouble(temp);
                     } catch(NumberFormatException ex) {
                         error += "The value \"" + temp + " \" for the radix is not valid.\n";
                     }
                 }
 
                 temp = vxField.getText().trim();
                 if(!temp.isEmpty()) {
                     try {
                         v.setX(Double.parseDouble(temp));
                     } catch(NumberFormatException ex) {
                         error += "The value \"" + temp + " \" for the x component of v is not valid.\n";
                     }
                 }
 
                 temp = vyField.getText().trim();
                 if(!temp.isEmpty()) {
                     try {
                         v.setY(Double.parseDouble(temp));
                     } catch(NumberFormatException ex) {
                         error += "The value \"" + temp + " \" for the y component of v is not valid.\n";
                     }
                 }
 
                 if(error.isEmpty()) {
                     Color c = new Color( (int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
                     startPlanets.add(new Planet(label, coords, mass, radix, v, c));
                     planets = (ArrayList<Planet>) startPlanets.clone();
                     planetsBox.addItem(label);
                     planetsBox.setSelectedIndex(planetsBox.getItemCount() - 1);
                 } else {
                     showInvalidFileDialog(error);//TODO: More general Error Dialog
                 }
             }
         });
 
         this.removePlanet.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 int index = planetsBox.getSelectedIndex() - 2;
 
                 planetsBox.setSelectedIndex(0);
 
                 startPlanets.remove(index);
                 planets = (ArrayList<Planet>) startPlanets.clone();
                 updateComboBoxes();
             }
         });
 
         this.removeAllPlanets.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 startPlanets = new ArrayList<>();
                 planets = (ArrayList<Planet>) startPlanets.clone();
 
                 planetsBox.removeAllItems();
                 planetsBox.addItem(standardBoxEntry);
                 planetsBox.addItem(newPlanetBoxEntry);
                 planetsBox.setSelectedIndex(0);
             }
         });
     }
 
     private void addPlanetsUIListeners() {
         this.labelField.addCaretListener(new CaretListener() {
             @Override
             public void caretUpdate(CaretEvent e) {
                 if(isAddingNewPlanet) {
                     return;
                 }
                 String currentValue = ((JTextField)(e.getSource())).getText();
                 int i = planetsBox.getSelectedIndex();
                 if(i < 2) {
                     return;
                 }
                 Planet temp = startPlanets.get(i - 2);
 
                 temp.setLabel(currentValue);
                 planetsBox.insertItemAt(currentValue, i);
                 planetsBox.removeItemAt(i + 1);
                 planetsBox.setSelectedIndex(i);
                 planetsBox.repaint();
                 startPlanets.set(i - 2, temp);
             }
         });
 
         this.vxField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 if(isAddingNewPlanet) {
                     return;
                 }
                 String currentValue = ((JTextField)(e.getSource())).getText();
                 int i = planetsBox.getSelectedIndex();
                 Planet temp = startPlanets.get(i - 2);
 
                 try {
                     double vx = Double.parseDouble(currentValue);
                     temp.setV(new Vektor2D(vx, temp.getV().getY()));
                     startPlanets.set(i - 2, temp);
                     ((JTextField)(e.getSource())).setText("" + vx);
                 } catch(NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for the x component of v is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     ((JTextField)(e.getSource())).setText("" + temp.getV().getX());
                 }
             }
         });
 
         this.vyField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 if(isAddingNewPlanet) {
                     return;
                 }
                 String currentValue = ((JTextField)(e.getSource())).getText();
                 int i = planetsBox.getSelectedIndex();
                 Planet temp = startPlanets.get(i - 2);
 
                 try {
                     double vy = Double.parseDouble(currentValue);
                     temp.setV(new Vektor2D(temp.getV().getX(), vy));
                     startPlanets.set(i - 2, temp);
                     ((JTextField)(e.getSource())).setText("" + vy);
                 } catch(NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for the y component of v is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     ((JTextField)(e.getSource())).setText("" + temp.getV().getY());
                 }
             }
         });
 
         this.xField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 if(isAddingNewPlanet) {
                     return;
                 }
                 String currentValue = ((JTextField)(e.getSource())).getText();
                 int i = planetsBox.getSelectedIndex();
                 Planet temp = startPlanets.get(i - 2);
 
                 try {
                     double x = Double.parseDouble(currentValue);
                     temp.setCoords(new Vektor2D(x, temp.getCoords().getY()));
                     startPlanets.set(i - 2, temp);
                     ((JTextField)(e.getSource())).setText("" + x);
                 } catch(NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for the x component of the coordinates is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     ((JTextField)(e.getSource())).setText("" + temp.getCoords().getX());
                 }
             }
         });
 
         this.yField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 if(isAddingNewPlanet) {
                     return;
                 }
                 String currentValue = ((JTextField)(e.getSource())).getText();
                 int i = planetsBox.getSelectedIndex();
                 Planet temp = startPlanets.get(i - 2);
 
                 try {
                     double y = Double.parseDouble(currentValue);
                     temp.setCoords(new Vektor2D(temp.getCoords().getX(), y));
                     startPlanets.set(i - 2, temp);
                     ((JTextField)(e.getSource())).setText("" + y);
                 } catch(NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for the y component of the coordinates is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     ((JTextField)(e.getSource())).setText("" + temp.getCoords().getY());
                 }
             }
         });
 
         this.massField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 if(isAddingNewPlanet) {
                     return;
                 }
                 String currentValue = ((JTextField)(e.getSource())).getText();
                 int i = planetsBox.getSelectedIndex();
                 Planet temp = startPlanets.get(i - 2);
 
                 try {
                     double mass = Double.parseDouble(currentValue);
                     temp.setMass(mass);
                     startPlanets.set(i - 2, temp);
                     ((JTextField)(e.getSource())).setText("" + mass);
                 } catch(NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for the mass is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     ((JTextField)(e.getSource())).setText("" + temp.getMass());
                 }
             }
         });
 
         this.radixField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 if(isAddingNewPlanet) {
                     return;
                 }
                 String currentValue = ((JTextField)(e.getSource())).getText();
                 int i = planetsBox.getSelectedIndex();
                 Planet temp = startPlanets.get(i - 2);
 
                 try {
                     double radix = Double.parseDouble(currentValue);
                     temp.setRadix(radix);
                     startPlanets.set(i - 2, temp);
                     ((JTextField)(e.getSource())).setText("" + radix);
                 } catch(NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for the radix is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     ((JTextField)(e.getSource())).setText("" + temp.getRadix());
                 }
             }
         });
     }
 
     private void layout(int width, int height) {
         this.slider.setSize(width - 14, this.slider.getHeight());
         this.slider.setPreferredSize(this.slider.getSize());
     }
 
     public void init() {
         this.modul.registerObserver("view", this);
         updateComboBoxes();
 
         try {
             Display.setParent(this.canvas);
             Keyboard.enableRepeatEvents(true);
             this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
             this.frame.setLayout(new BorderLayout());
             this.frame.add(this.tabbedPane, BorderLayout.LINE_START);
             this.frame.add(this.canvas, BorderLayout.CENTER);
             this.frame.add(this.panel, BorderLayout.PAGE_END);
             this.frame.setPreferredSize(new Dimension(1024, 786));
             this.frame.setMinimumSize(new Dimension(800, 600));
             this.frame.pack();
 
             this.frame.setVisible(true);
 
             Display.setTitle(this.title);
             Display.setResizable(true);
             Display.setFullscreen(false);
             Display.create();
             this.initOpenGL();
             this.wasInitialized = true;
             LWJGLRenderer renderer = new LWJGLRenderer();
 
             Dimension newDim;
 
             while (!Display.isCloseRequested() && !View.closeRequested) {
                 if (this.shouldReInit) {
                     this.initOpenGL();
                     this.shouldReInit = false;
                 }
 
                 if (this.shouldTakeScreenshot) {
                     this.saveScreenshot();
                     this.shouldTakeScreenshot = false;
                 }
 
                 GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                 newDim = newCanvasSize.getAndSet(null);
                 checkKeyInput();
                 if (newDim != null) {
                     GL11.glViewport(0, 0, newDim.width, newDim.height);
                     renderer.syncViewportSize();
                 }
 
                 for (int i = 0; i < this.planets.size(); i++) {
                     this.planets.get(i).draw2D(true);
                 }
 
                 if (!this.isPaused && this.results != null && this.currentIndex < this.results.size() - 1 && this.getDelta() / this.deltaT >= this.speed) {
                     this.planets = this.results.get(this.currentIndex);
                     int add = (int) (1 / deltaT);
                     if (add == 0) {
                         add = 1;
                     }
                     this.time = this.getTime();
                     this.currentIndex += add;
                     this.slider.setValue(this.slider.getValue() + add);
                 }
 
                 checkKeyInput();
                 checkMouseInput();
                 Display.update();
                 Display.sync(60);
             }
 
             Display.destroy();
             this.frame.dispose();
         } catch (LWJGLException e) {
             System.out.println(e.getMessage());
             System.exit(-1);
         }
     }
 
     private void checkKeyInput() {
         while (Keyboard.next()) {
             if (!Keyboard.getEventKeyState()) {
                 switch (Keyboard.getEventKey()) {
                     case Keyboard.KEY_F1:
                         this.saveScreenshot();
                         break;
                     case Keyboard.KEY_SPACE:
                         this.isPaused = !this.isPaused;
                         break;
                 }
             }
 
             switch (Keyboard.getEventKey()) {
                 case Keyboard.KEY_UP:
                     this.changeTranslationMatrix(Directions.UP);
                     break;
                 case Keyboard.KEY_DOWN:
                     this.changeTranslationMatrix(Directions.DOWN);
                     break;
                 case Keyboard.KEY_LEFT:
                     this.changeTranslationMatrix(Directions.LEFT);
                     break;
                 case Keyboard.KEY_RIGHT:
                     this.changeTranslationMatrix(Directions.RIGHT);
                     break;
                 case Keyboard.KEY_0:
                     Keyboard.poll();
                     if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                         this.resetScaleMatrix();
                     }
                     break;
                 case Keyboard.KEY_C:
                     this.resetTranslationMatrix();
                     break;
                 case Keyboard.KEY_ADD:
                     if (this.speed >= 5 / this.deltaT) {
                     this.speed -= 5 / this.deltaT;
                 }
                     break;
                 case Keyboard.KEY_SUBTRACT:
                     this.speed += 5 / this.deltaT;
                     break;
             }
         }
     }
 
     private void initOpenGL() {
         GL11.glMatrixMode(GL11.GL_PROJECTION);
 
         GL11.glLoadMatrix(this.buffer);
 
         GL11.glOrtho(-100, 100, -100, 100, 1, -1);
 
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
     }
 
     private void showAboutDialog() {
         JDialog dialog = new JDialog(this.frame, true);
         dialog.setSize(200, 200);
         dialog.setLocation(this.frame.getX() + this.frame.getWidth() / 2 - 100, this.frame.getY() + this.frame.getHeight() / 2 - 100);
         dialog.setEnabled(true);
         dialog.setVisible(true);
         dialog.addWindowListener(new WindowListener() {
             @Override
             public void windowOpened(WindowEvent e) {
             }
 
             @Override
             public void windowClosing(WindowEvent e) {
             }
 
             @Override
             public void windowClosed(WindowEvent e) {
             }
 
             @Override
             public void windowIconified(WindowEvent e) {
             }
 
             @Override
             public void windowDeiconified(WindowEvent e) {
             }
 
             @Override
             public void windowActivated(WindowEvent e) {
             }
 
             @Override
             public void windowDeactivated(WindowEvent e) {
                 isPaused = false;
                 e.getWindow().dispose();
             }
         });
     }
 
     private void saveScreenshot() {
         final File f = new File(View.class.getProtectionDomain().getCodeSource().getLocation().getPath());
         GL11.glReadBuffer(GL11.GL_FRONT);
         int width = Display.getWidth();
         int height = Display.getHeight();
         ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width * height * 4);
         GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
         ScreenshotSaver saver = new ScreenshotSaver(byteBuffer, f.getParent(), width, height);
         saver.start();
     }
 
     private void saveProject() {
         boolean wasSelfPaused = !this.isPaused;
 
         this.isPaused = true;
 
         JFileChooser fileChooser;
 
         if (this.lastOpenedFilePath.isEmpty()) {
             fileChooser = new JFileChooser();
         } else {
             fileChooser = new JFileChooser(this.lastOpenedFilePath);
         }
 
         int state = fileChooser.showSaveDialog(this.frame);
 
         if (state == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             this.lastSavedFilePath = selectedFile.getAbsolutePath();
 
             ProjectFileSaver saver = new ProjectFileSaver(this.lastSavedFilePath, this.startPlanets, this.modul.getIntegrator(), this.deltaT);
             saver.start();
         }
 
         if (wasSelfPaused) {
             this.isPaused = false;
         }
     }
 
     private void saveData() {
         if (this.results == null) {
             return;
         }
 
         boolean wasSelfPaused = !this.isPaused;
 
         this.isPaused = true;
 
         JFileChooser fileChooser;
 
         if (this.lastSavedDataFilePath.isEmpty()) {
             fileChooser = new JFileChooser();
         } else {
             fileChooser = new JFileChooser(this.lastSavedDataFilePath);
         }
 
         int state = fileChooser.showSaveDialog(this.frame);
 
         if (state == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             this.lastSavedDataFilePath = selectedFile.getAbsolutePath();
 
             DataFileSaver saver = new DataFileSaver(this.lastSavedDataFilePath, this.deltaT, this.results);
             saver.start();
         }
 
         if (wasSelfPaused) {
             this.isPaused = false;
         }
     }
 
     @SuppressWarnings("unchecked")
     private void openFile() {
         JFileChooser fileChooser;
 
         if (this.lastOpenedFilePath.isEmpty()) {
             final File f = new File(View.class.getProtectionDomain().getCodeSource().getLocation().getPath());
             fileChooser = new JFileChooser(f.getParentFile());
         } else {
             fileChooser = new JFileChooser(this.lastOpenedFilePath);
         }
         int state = fileChooser.showOpenDialog(this.frame);
 
         if (state == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             this.lastOpenedFilePath = selectedFile.getAbsolutePath();
             HashMap<String, Object> dataFromDataFile = Util.getDataFromDataFile(selectedFile);
             if ("".equals((String) dataFromDataFile.get("Error")) && !((ArrayList<Planet>) dataFromDataFile.get("Planets")).isEmpty()) {
                 this.planets = (ArrayList<Planet>) dataFromDataFile.get("Planets");
                 this.startPlanets = (ArrayList<Planet>) this.planets.clone();
                 this.deltaT = (double) dataFromDataFile.get("deltaT");
                 this.modul.setDeltaT(this.deltaT);
                 this.deltatField.setText("" + this.deltaT);
                 this.modul.setIntegrator((Integratoren) dataFromDataFile.get("Integrator"));
                 this.integratorBox.setSelectedItem(this.modul.getIntegrator());
                 this.speed = (long) (1 / this.deltaT);
                 updateComboBoxes();
             } else {
                 this.showInvalidFileDialog((String) dataFromDataFile.get("Error"));
             }
             this.isPaused = true;
         } else {
             this.isPaused = false;
         }
     }
 
     private void updateComboBoxes() {
         int size = this.startPlanets.size();
         String temp;
 
         this.planetsBox.removeAllItems();
         this.planetsBox.addItem(this.standardBoxEntry);
         this.planetsBox.addItem(this.newPlanetBoxEntry);
 
         for (int i = 0; i < size; i++) {
             temp = this.startPlanets.get(i).getLabel();
             this.planetsBox.addItem(temp);
         }
 
         this.planetsBox.setSelectedIndex(0);
     }
 
     private void showInvalidFileDialog(String errors) {
         System.out.println(errors);
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
         Display.setTitle(this.title);
     }
 
     public void setPlanets(ArrayList<Planet> planets) {
         this.planets = planets;
     }
 
     @Override
     public void run() {
         this.init();
     }
 
     @Override
     public void notify(Notifications type, String data) {
     }
 
     @Override
     public void sendPlanets(Notifications type, ArrayList<ArrayList<Planet>> planets) {
         this.results = planets;
         this.initSlider();
         this.isPaused = false;
     }
 
     private void initSlider() {
         this.slider.setMaximum(this.results.size() - 2);
         this.slider.setMinorTickSpacing(1);
         this.slider.setValue(0);
         this.slider.setEnabled(true);
     }
 
     private void checkMouseInput() {
         int delta = Mouse.getDWheel();
 
         if (delta > 0) {
             this.changeZoomFactor(ChangeType.INCREASE, true);
         } else if (delta < 0) {
             this.changeZoomFactor(ChangeType.DECREASE, true);
         }
     }
 
     private void resetScaleMatrix() {
         this.zoomLevel = 0;
         this.buffer.put(0, this.zoomLevel);
         this.buffer.put(5, this.zoomLevel);
         this.buffer.put(10, this.zoomLevel);
         this.initOpenGL();
     }
 
     private void resetTranslationMatrix() {
         this.buffer.put(12, 0);
         this.buffer.put(13, 0);
         this.initOpenGL();
     }
 
     private void changeTranslationMatrix(Directions direction) {
         switch (direction) {
             case UP:
                 this.buffer.put(13, this.buffer.get(13) + 0.01);
                 break;
             case DOWN:
                 this.buffer.put(13, this.buffer.get(13) - 0.01);
                 break;
             case LEFT:
                 this.buffer.put(12, this.buffer.get(12) - 0.01);
                 break;
             case RIGHT:
                 this.buffer.put(12, this.buffer.get(12) + 0.01);
                 break;
         }
 
         this.initOpenGL();
     }
 
     private void changeZoomFactor(ChangeType change, boolean shouldReInit) {
         switch (change) {
             case DECREASE:
                 if (this.zoomLevel != 1) {
                 this.zoomLevel -= 1;
             } else {
                 this.zoomLevel = -1;
             }
                 break;
             case INCREASE:
                 if (this.zoomLevel != -1) {
                 this.zoomLevel += 1;
             } else {
                 this.zoomLevel = 1;
             }
                 break;
         }
 
         if (this.zoomLevel > 0) {
             this.buffer.put(0, this.zoomLevel);
             this.buffer.put(5, this.zoomLevel);
             this.buffer.put(10, this.zoomLevel);
         } else if (this.zoomLevel < 0) {
             this.buffer.put(0, 1.0 / -this.zoomLevel);
             this.buffer.put(5, 1.0 / -this.zoomLevel);
             this.buffer.put(10, 1.0 / -this.zoomLevel);
         }
         if(shouldReInit) {
             this.initOpenGL();
         }
     }
 
     private long getTime() {
         return (Sys.getTime() * 1000 / Sys.getTimerResolution());
     }
 
     private long getDelta() {
         long current_time = this.getTime();
         long delta = current_time - this.time;
 
         return delta;
     }
 }
