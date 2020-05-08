 //
 // UserInterfacePanel.java
 //
 
 /*
 SLIMPlugin for combined spectral-lifetime image analysis.
 
 Copyright (c) 2010, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.slim.ui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.CheckboxMenuItem;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Menu;
 import java.awt.MenuBar;
 import java.awt.MenuItem;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.Hashtable;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.ProgressMonitor;
 import javax.swing.SpringLayout;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.Document;
 
 import loci.slim.SLIMProcessor.FitAlgorithm;
 import loci.slim.SLIMProcessor.FitFunction;
 import loci.slim.SLIMProcessor.FitRegion;
 
 /**
  * TODO
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/UserInterfacePanel.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/UserInterfacePanel.java">SVN</a></dd></dl>
  *
  * @author Aivar Grislis grislis at wisc.edu
  */
 
 /*
 class edit_dialog extends javax.swing.JFrame{
     javax.swing.JTextField title = new javax.swing.JTextField();
     public edit_dialog(){
         setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
         setTitle("New entity");
         getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));
         add(title);
         pack();
         setVisible(true);
     }
 }
 Your problem is that you're creating a BoxLayout for a JFrame (this), but setting it as the layout for a JPanel (getContentPane()). Try:
 
 getContentPane().setLayout(
     new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS)
 );
 
 link
  */
public class UserInterfacePanel extends JFrame implements IUserInterfacePanel, ItemListener {
     // Unicode special characters
     private static final Character CHI    = '\u03c7';
     private static final Character SQUARE = '\u00b2';
     private static final Character TAU    = '\u03c4';
     private static final Character LAMBDA = '\u03bb';
     private static final Character SIGMA  = '\u03c3';
     private static final Character SUB_1  = '\u2081';
     private static final Character SUB_2  = '\u2082';
     private static final Character SUB_3  = '\u2083';
 
     // UI panel
     JPanel m_uiPanel;
 
     // parameter panel
     JPanel m_paramPanel;
     int m_paramPanelIndex;
 
     // single exponent fit
     JTextField m_a1Param1;
     JCheckBox m_a1Fix1;
     JTextField m_t1Param1;
     JCheckBox m_t1Fix1;
     JTextField m_cParam1;
     JCheckBox m_cFix1;
 
     // double exponent fit
     JTextField m_a1Param2;
     JCheckBox m_a1Fix2;
     JTextField m_a2Param2;
     JCheckBox m_a2Fix2;
     JTextField m_t1Param2;
     JCheckBox m_t1Fix2;
     JTextField m_t2Param2;
     JCheckBox m_t2Fix2;
     JTextField m_cParam2;
     JCheckBox m_cFix2;
 
     // triple exponent fit
     JTextField m_a1Param3;
     JCheckBox m_a1Fix3;
     JTextField m_a2Param3;
     JCheckBox m_a2Fix3;
     JTextField m_a3Param3;
     JCheckBox m_a3Fix3;
     JTextField m_t1Param3;
     JCheckBox m_t1Fix3;
     JTextField m_t2Param3;
     JCheckBox m_t2Fix3;
     JTextField m_t3Param3;
     JCheckBox m_t3Fix3;
     JTextField m_cParam3;
     JCheckBox m_cFix3;
 
     // fit settings
     JTextField m_xField;
     JTextField m_yField;
     JTextField m_startField;
     JTextField m_stopField;
     JTextField m_thresholdField;
 
     // data
     FitRegion m_region;
     FitAlgorithm m_algorithm;
     FitFunction m_function;
     int m_start;
     int m_stop;
     int m_threshold;
     int m_x;
     int m_y;
     int m_components;
     double m_params[];
     boolean m_free[];
 
     public UserInterfacePanel(boolean showTau) {
         String lifetimeLabel = "" + (showTau ? TAU : LAMBDA);
 
         // create outer panel
         m_uiPanel = new JPanel(); //new BoxLayout(m_uiPanel, BoxLayout.Y_AXIS));
 
         // create inner panel
         JPanel innerPanel = null;
         innerPanel = new JPanel(); //new BoxLayout(innerPanel, BoxLayout.X_AXIS));
 
         // create parameter panel
         m_paramPanel = new JPanel(new CardLayout());
 
         // create & add single exponential panel
         m_paramPanel.add(createSingleExponentialPanel(lifetimeLabel), "PANEL 1");
 
         // create & add double exponential panel
         m_paramPanel.add(createDoubleExponentialPanel(lifetimeLabel), "PANEL 2");
 
         // create & add triple exponential panel
         m_paramPanel.add(createTripleExponentialPanel(lifetimeLabel), "PANEL 3");
 
         // create settings panel
         JPanel settingsPanel = new JPanel();
         settingsPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
         settingsPanel.setLayout(new SpringLayout());
 
         JLabel xLabel = new JLabel("x");
         xLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         settingsPanel.add(xLabel);
         m_xField = new JTextField(9);
         m_xField.setEditable(false);
         settingsPanel.add(m_xField);
 
         JLabel yLabel = new JLabel("y");
         yLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         settingsPanel.add(yLabel);
         m_yField = new JTextField(9);
         m_yField.setEditable(false);
         settingsPanel.add(m_yField);
 
         JLabel startLabel = new JLabel("start");
         startLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         settingsPanel.add(startLabel);
         m_startField = new JTextField(9);
         m_startField.setEditable(false);
         settingsPanel.add(m_startField);
 
         JLabel stopLabel = new JLabel("stop");
         stopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         settingsPanel.add(stopLabel);
         m_stopField = new JTextField(9);
         m_stopField.setEditable(false);
         settingsPanel.add(m_stopField);
 
         JLabel thresholdLabel = new JLabel("threshold");
         thresholdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         settingsPanel.add(thresholdLabel);
         m_thresholdField = new JTextField(9);
         m_thresholdField.setEditable(false);
         settingsPanel.add(m_thresholdField);
 
         // create control panel
         JPanel controlPanel = new JPanel();
 
         innerPanel.add(controlPanel);
         innerPanel.add(settingsPanel);
         innerPanel.add(m_paramPanel);
 
         JButton fitButton = new JButton("Fit");
 
         m_uiPanel.add(innerPanel);
         m_uiPanel.add(fitButton);
     }
 
     public JPanel getPanel() {
         return m_uiPanel;
     }
 
     private JPanel createSingleExponentialPanel(String lifetimeLabel) {
         JPanel expPanel1 = new JPanel();
         expPanel1.setBorder(new EmptyBorder(0, 0, 8, 8));
         expPanel1.setLayout(new SpringLayout());
 
         JLabel a1Label1 = new JLabel("a");
         a1Label1.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel1.add(a1Label1);
         m_a1Param1 = new JTextField(9);
         m_a1Param1.setEditable(false);
         expPanel1.add(m_a1Param1);
         m_a1Fix1 = new JCheckBox("Fix");
         m_a1Fix1.addItemListener(this);
         expPanel1.add(m_a1Fix1);
 
         JLabel t1Label1 = new JLabel(lifetimeLabel);
         t1Label1.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel1.add(t1Label1);
         m_t1Param1 = new JTextField(9);
         m_t1Param1.setEditable(false);
         expPanel1.add(m_t1Param1);
         m_t1Fix1 = new JCheckBox("Fix");
         m_t1Fix1.addItemListener(this);
         expPanel1.add(m_t1Fix1);
 
         JLabel cLabel1 = new JLabel("c");
         cLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel1.add(cLabel1);
         m_cParam1 = new JTextField(9);
         m_cParam1.setEditable(false);
         expPanel1.add(m_cParam1);
         m_cFix1 = new JCheckBox("Fix");
         m_cFix1.addItemListener(this);
         expPanel1.add(m_cFix1);
 
         return expPanel1;
     }
 
     private JPanel createDoubleExponentialPanel(String lifetimeLabel) {
         JPanel expPanel2 = new JPanel();
         expPanel2.setBorder(new EmptyBorder(0, 0, 8, 8));
         expPanel2.setLayout(new SpringLayout());
 
         JLabel a1Label2 = new JLabel("a" + SUB_1);
         a1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel2.add(a1Label2);
         m_a1Param2 = new JTextField(9);
         m_a1Param2.setEditable(false);
         expPanel2.add(m_a1Param2);
         m_a1Fix2 = new JCheckBox("Fix");
         m_a1Fix2.addItemListener(this);
         expPanel2.add(m_a1Fix2);
 
         JLabel t1Label2 = new JLabel(lifetimeLabel + SUB_1);
         t1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel2.add(t1Label2);
         m_t1Param2 = new JTextField(9);
         m_t1Param2.setEditable(false);
         expPanel2.add(m_t1Param2);
         m_t1Fix2 = new JCheckBox("Fix");
         m_t1Fix2.addItemListener(this);
         expPanel2.add(m_t1Fix2);
 
         JLabel a2Label2 = new JLabel("a" + SUB_2);
         a2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel2.add(a2Label2);
         m_a2Param2 = new JTextField(9);
         m_a2Param2.setEditable(false);
         expPanel2.add(m_a2Param2);
         m_a2Fix2 = new JCheckBox("Fix");
         m_a2Fix2.addItemListener(this);
         expPanel2.add(m_a2Fix2);
 
         JLabel t2Label2 = new JLabel(lifetimeLabel + SUB_2);
         t2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel2.add(t2Label2);
         m_t2Param2 = new JTextField(9);
         m_t2Param2.setEditable(false);
         expPanel2.add(m_t2Param2);
         m_t2Fix2 = new JCheckBox("Fix");
         m_t2Fix2.addItemListener(this);
         expPanel2.add(m_t2Fix2);
 
         JLabel cLabel2 = new JLabel("c");
         cLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel2.add(cLabel2);
         m_cParam2 = new JTextField(9);
         m_cParam2.setEditable(false);
         expPanel2.add(m_cParam2);
         m_cFix2 = new JCheckBox("Fix");
         m_cFix2.addItemListener(this);
         expPanel2.add(m_cFix2);
 
         return expPanel2;
     }
 
     private JPanel createTripleExponentialPanel(String lifetimeLabel) {
         JPanel expPanel3 = new JPanel();
         expPanel3.setBorder(new EmptyBorder(0, 0, 8, 8));
         expPanel3.setLayout(new SpringLayout());
 
         JLabel a1Label3 = new JLabel("a" + SUB_1);
         a1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel3.add(a1Label3);
         m_a1Param3 = new JTextField(9);
         m_a1Param3.setEditable(false);
         expPanel3.add(m_a1Param3);
         m_a1Fix3 = new JCheckBox("Fix");
         m_a1Fix3.addItemListener(this);
         expPanel3.add(m_a1Fix3);
 
         JLabel t1Label3 = new JLabel(lifetimeLabel + SUB_1);
         t1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel3.add(t1Label3);
         m_t1Param3 = new JTextField(9);
         m_t1Param3.setEditable(false);
         expPanel3.add(m_t1Param3);
         m_t1Fix3 = new JCheckBox("Fix");
         m_t1Fix3.addItemListener(this);
         expPanel3.add(m_t1Fix3);
 
         JLabel a2Label3 = new JLabel("a" + SUB_2);
         a2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel3.add(a2Label3);
         m_a2Param3 = new JTextField(9);
         m_a2Param3.setEditable(false);
         expPanel3.add(m_a2Param3);
         m_a2Fix3 = new JCheckBox("Fix");
         m_a2Fix3.addItemListener(this);
         expPanel3.add(m_a2Fix3);
 
         JLabel t2Label3 = new JLabel(lifetimeLabel + SUB_2);
         t2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel3.add(t2Label3);
         m_t2Param3 = new JTextField(9);
         m_t2Param3.setEditable(false);
         expPanel3.add(m_t2Param3);
         m_t2Fix3 = new JCheckBox("Fix");
         m_t2Fix3.addItemListener(this);
         expPanel3.add(m_t2Fix3);
 
         JLabel a3Label3 = new JLabel("a" + SUB_3);
         a3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel3.add(a3Label3);
         m_a3Param3 = new JTextField(9);
         m_a3Param3.setEditable(false);
         expPanel3.add(m_a3Param3);
         m_a3Fix3 = new JCheckBox("Fix");
         m_a3Fix3.addItemListener(this);
         expPanel3.add(m_a3Fix3);
 
         JLabel t3Label3 = new JLabel(lifetimeLabel + SUB_3);
         t3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel3.add(t3Label3);
         m_t3Param3 = new JTextField(9);
         m_t3Param3.setEditable(false);
         expPanel3.add(m_t3Param3);
         m_t3Fix3 = new JCheckBox("Fix");
         m_t3Fix3.addItemListener(this);
         expPanel3.add(m_t3Fix3);
 
         JLabel cLabel3 = new JLabel("c");
         cLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
         expPanel3.add(cLabel3);
         m_cParam3 = new JTextField(9);
         m_cParam3.setEditable(false);
         expPanel3.add(m_cParam3);
         m_cFix3 = new JCheckBox("Fix");
         m_cFix3.addItemListener(this);
         expPanel3.add(m_cFix3);
 
         return expPanel3;
     }
 
     public FitRegion getRegion() {
         return m_region;
     }
 
     public FitAlgorithm getAlgorithm() {
         return m_algorithm;
     }
 
     public FitFunction getFunction() {
         return m_function;
     }
 
     public int getStart() {
         return m_start;
     }
     public int getStop() {
         return m_stop;
     }
     public int getThreshold() {
         return m_threshold;
     }
 
     public int getX() {
         return m_x;
     }
 
     public void setX(int x) {
         m_x = x;
     }
 
     public int getY() {
         return m_y;
     }
 
     public void setY(int y) {
         m_y = y;
     }
 
     public int getComponents() {
         return m_components;
     }
     public double[] getParameters() {
         return m_params;
     }
 
     public void setParameters(double params[]) {
         m_params = params.clone();
     }
     public boolean[] getFree() {
         return m_free;
     }
 
     @Override
     public void itemStateChanged(ItemEvent e) {
         
     }
 }
