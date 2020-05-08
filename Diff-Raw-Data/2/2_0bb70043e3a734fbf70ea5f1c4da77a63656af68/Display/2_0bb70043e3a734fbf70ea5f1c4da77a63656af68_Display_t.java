 /*
  * Copyright (c) 2009, Miroslav Batchkarov, University of Sussex
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  *  * Neither the name of the University of Sussex nor the names of its
  *    contributors may be used to endorse or promote products  derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * Display.java
  *
  * Created on 15-Jul-2009, 14:27:03
  */
 package view;
 
 import controller.*;
 import edu.uci.ics.jung.algorithms.layout.*;
 import edu.uci.ics.jung.algorithms.layout.SpringLayout;
 import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
 import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
 import edu.uci.ics.jung.algorithms.util.IterativeContext;
 import edu.uci.ics.jung.graph.Forest;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.MyGraph;
 import edu.uci.ics.jung.graph.event.GraphEvent;
 import edu.uci.ics.jung.graph.event.GraphEventListener;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
 import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ScalingControl;
 import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
 import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
 import edu.uci.ics.jung.visualization.layout.LayoutTransition;
 import edu.uci.ics.jung.visualization.layout.PersistentLayoutImpl;
 import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
 import edu.uci.ics.jung.visualization.renderers.Renderer;
 import edu.uci.ics.jung.visualization.util.Animator;
 import model.MyEdge;
 import model.MyVertex;
 import model.Strings;
 import model.dynamics.SIDynamics;
 import model.dynamics.SIRDynamics;
 import model.dynamics.SISDynamics;
 import org.apache.commons.collections15.functors.ConstantTransformer;
 import view.CustomMouse.CustomGraphMouse;
 import view.CustomVisualization.CenterLabelPositioner;
 import view.CustomVisualization.CustomEdgeLabeller;
 import view.CustomVisualization.CustomVertexLabeler;
 import view.CustomVisualization.CustomVertexIconShapeTransformer;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import static view.Utils.round;
 
 /**
  * @author mb724
  */
 @SuppressWarnings({"MagicNumber", "AssignmentToStaticFieldFromInstanceMethod", "OverlyLongMethod", "MethodOnlyUsedFromInnerClass"})
 public class Display extends JFrame implements GraphEventListener<MyVertex, MyEdge>,
         ExtraGraphEventListener<MyVertex, MyEdge> {
 
     private static InfoGatherer gatherer;
     //declared as fields rather than as local variables so that their value can be altered by listeners
     public static VisualizationViewer vv;
     private static ScalingControl scaler;
     //    EditingModalGraphMouse<MyVertex, MyEdge> graphMouse;
     private static CustomGraphMouse graphMouse;
     static AnnotationControls<MyVertex, MyEdge> annotationControls;
     static JToolBar annotationControlsToolbar;
     private static PersistentLayoutImpl persistentLayout;
     public boolean handlingEvents;
     private Stats stats;
     private Controller controller;
     private MyGraph g;
     private IconsStore icons;
 
     /**
      * Creates new form Display
      */
     public Display(Stats stats, Controller cont, MyGraph g) {
         initComponents();
         pack();
         this.stats = stats;
         this.controller = cont;
         this.g = g;
         this.handlingEvents = false;
 
         gatherer = new InfoGatherer(controller, this);
 
         //set shortcuts for controlling the simulation speed
         InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
         ActionMap actionMap = getRootPane().getActionMap();
 
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "+Action");
         actionMap.put("+Action", incrementWaitTime);
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "-Action");
         actionMap.put("-Action", decrementWaitTime);
         scaler = new CrossoverScalingControl();
         redisplayCompletely();
 
         vNone.setSelected(true);
         parseSimulationParameters(null);//trigger parsing of default values for transmission params
     }
 
     private AbstractAction incrementWaitTime = new AbstractAction() {
 
         public void actionPerformed(ActionEvent e) {
             speedSlider.setValue(speedSlider.getValue() + 100);
         }
     };
     private AbstractAction decrementWaitTime = new AbstractAction() {
 
         public void actionPerformed(ActionEvent e) {
             speedSlider.setValue(speedSlider.getValue() - 100);
         }
     };
 
     /**
      * Recalculates and display the stats of the graph and of the vertex passed
      * in. If it is null, sets all local statistics to "N/A"
      */
     public void updateStatsDisplay() {
         // get selected vertex
         MyVertex selectedVertex = null;
         ArrayList<MyVertex> pickedVertices = new ArrayList(
                 this.getVV().getPickedVertexState().getPicked()
         );
         if (pickedVertices.size() == 1) {
             selectedVertex = pickedVertices.get(0);
         }
 
         //populate information labels across the screen
         globalCC.setText(round(stats.getCC()));
         globalAPL.setText(round(stats.getAPL()));
         globalAvgDegree.setText(round(stats.getAvgDegree()));
         globalDegreeCorrelation.setText(round(stats.getWeightedDegreeCorrelation()));
 
         globalMinDegree.setText(round(stats.getMinDegree()));
         globalMaxDegree.setText(round(stats.getMaxDegree()));
 
         globalVertexCount.setText(round(stats.getVertexCount()));
         globalEdgeCount.setText(round(stats.getEdgeCount()));
 
         updateDegreeDistributionChart();
 
         //information about a certain node
         if (selectedVertex != null) {
             localCC.setText(round(stats.getCC(selectedVertex)));
             localAPL.setText(round(stats.getAPL(selectedVertex)));
             localBC.setText(round(stats.getBetweennessCentrality(selectedVertex)));
             in.setText(round(g.inDegree(selectedVertex)));
             out.setText(round(g.outDegree(selectedVertex)));
 
         } else {
             localCC.setText("N/A");
             localAPL.setText("N/A");
             localBC.setText("N/A");
             in.setText("N/A");
             out.setText("N/A");
         }
     }
 
     private void initDegreeDistributionChart() {
         JPanel degreeChart = stats.buildDegreeDistributionChart(
                 degDistCumulative.isSelected(),
                 degDistLogScale.isSelected(),
                 degreeDistPanel.getSize());
 
         degreeDistPanel.setLayout(new FlowLayout());
         degreeDistPanel.removeAll();
         degreeDistPanel.add(degreeChart);
     }
 
     private void updateDegreeDistributionChart() {
         stats.updateDegreeDistributionChartData(degDistCumulative.isSelected(),
                 degDistLogScale.isSelected());
     }
 
     public VisualizationViewer getVV() {
         return vv;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         vertexLabel = new javax.swing.ButtonGroup();
         layouts = new javax.swing.ButtonGroup();
         edgeLabel = new javax.swing.ButtonGroup();
         mouseModeButtonGroup = new javax.swing.ButtonGroup();
         mouseModeToolbar = new javax.swing.JToolBar();
         select = new javax.swing.JToggleButton();
         edit = new javax.swing.JToggleButton();
         transform = new javax.swing.JToggleButton();
         annotate = new javax.swing.JToggleButton();
         pane = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         dynamics = new javax.swing.JComboBox();
         jLabel4 = new javax.swing.JLabel();
         tau = new javax.swing.JTextField();
         gamaLabel = new javax.swing.JLabel();
         gama = new javax.swing.JTextField();
         edgeBreakingLabel = new javax.swing.JLabel();
         breakingRate = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         deltaT = new javax.swing.JTextField();
         statsPanel = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         localCC = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         localAPL = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jLabel13 = new javax.swing.JLabel();
         localBC = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         in = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         out = new javax.swing.JLabel();
         graphStatsPanel = new javax.swing.JPanel();
         jLabel10 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jLabel16 = new javax.swing.JLabel();
         jLabel17 = new javax.swing.JLabel();
         globalCC = new javax.swing.JLabel();
         globalAPL = new javax.swing.JLabel();
         globalAvgDegree = new javax.swing.JLabel();
         globalDegreeCorrelation = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         globalVertexCount = new javax.swing.JLabel();
         globalEdgeCount = new javax.swing.JLabel();
         jLabel19 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         globalMinDegree = new javax.swing.JLabel();
         globalMaxDegree = new javax.swing.JLabel();
         jLabel21 = new javax.swing.JLabel();
         degreeDistPanel = new javax.swing.JPanel();
         degDistLogScale = new javax.swing.JCheckBox();
         degDistCumulative = new javax.swing.JCheckBox();
         simControlsPanel = new javax.swing.JPanel();
         pauseSimToolbarButton = new javax.swing.JButton();
         doStepToolbarButton = new javax.swing.JButton();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         speedSlider = new javax.swing.JSlider();
         jMenuBar1 = new javax.swing.JMenuBar();
         menuFile = new javax.swing.JMenu();
         newDoc = new javax.swing.JMenuItem();
         fileSave = new javax.swing.JMenuItem();
         fileLoad = new javax.swing.JMenuItem();
         fileGenerate1 = new javax.swing.JMenuItem();
         fileQuit1 = new javax.swing.JMenuItem();
         jMenu2 = new javax.swing.JMenu();
         dumpToJpg = new javax.swing.JMenuItem();
         label2 = new javax.swing.JMenu();
         vDEgree = new javax.swing.JRadioButtonMenuItem();
         vCC = new javax.swing.JRadioButtonMenuItem();
         vBC = new javax.swing.JRadioButtonMenuItem();
         vDist = new javax.swing.JRadioButtonMenuItem();
         vID = new javax.swing.JRadioButtonMenuItem();
         vNone = new javax.swing.JRadioButtonMenuItem();
         label3 = new javax.swing.JMenu();
         eWeight = new javax.swing.JRadioButtonMenuItem();
         eID = new javax.swing.JRadioButtonMenuItem();
         eBC = new javax.swing.JRadioButtonMenuItem();
         eNone = new javax.swing.JRadioButtonMenuItem();
         layoutMenu = new javax.swing.JMenu();
         kk = new javax.swing.JRadioButtonMenuItem();
         fr = new javax.swing.JRadioButtonMenuItem();
         isom = new javax.swing.JRadioButtonMenuItem();
         spring = new javax.swing.JRadioButtonMenuItem();
         circleL = new javax.swing.JRadioButtonMenuItem();
         menuSimulation = new javax.swing.JMenu();
         simPauseMenuItem = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
         jSeparator13 = new javax.swing.JSeparator();
         infectButton = new javax.swing.JMenuItem();
         healAllButton = new javax.swing.JMenuItem();
         menuHelp = new javax.swing.JMenu();
         helpHowTo = new javax.swing.JMenuItem();
         helpAbout = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Free Roam Mode");
         setMinimumSize(new java.awt.Dimension(1024, 768));
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
         addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 formKeyPressed(evt);
             }
         });
 
         mouseModeToolbar.setBorder(javax.swing.BorderFactory.createTitledBorder("Mouse mode"));
         mouseModeToolbar.setRollover(true);
         mouseModeToolbar.setEnabled(false);
 
         mouseModeButtonGroup.add(select);
         select.setSelected(true);
         select.setText("Select");
         select.setFocusable(false);
         select.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         select.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         select.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selectActionPerformed(evt);
             }
         });
         mouseModeToolbar.add(select);
 
         mouseModeButtonGroup.add(edit);
         edit.setText("Edit");
         edit.setFocusable(false);
         edit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         edit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         edit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editActionPerformed(evt);
             }
         });
         mouseModeToolbar.add(edit);
 
         mouseModeButtonGroup.add(transform);
         transform.setText("Transform");
         transform.setFocusable(false);
         transform.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         transform.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         transform.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 transformActionPerformed(evt);
             }
         });
         mouseModeToolbar.add(transform);
 
         mouseModeButtonGroup.add(annotate);
         annotate.setText("Annotate");
         annotate.setFocusable(false);
         annotate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         annotate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         annotate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 annotateActionPerformed(evt);
             }
         });
         mouseModeToolbar.add(annotate);
 
         pane.setBorder(javax.swing.BorderFactory.createTitledBorder("Graph"));
         pane.setMinimumSize(new java.awt.Dimension(100, 100));
         pane.setName("pane"); // NOI18N
 
         javax.swing.GroupLayout paneLayout = new javax.swing.GroupLayout(pane);
         pane.setLayout(paneLayout);
         paneLayout.setHorizontalGroup(
             paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         paneLayout.setVerticalGroup(
             paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Disease controls"));
 
         jLabel2.setText("Dynamics");
 
         dynamics.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SI", "SIS", "SIR" }));
         dynamics.setSelectedIndex(1);
         dynamics.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 dynamicsItemStateChanged(evt);
             }
         });
 
         jLabel4.setText("Transmission rate");
 
         tau.setText("2");
         tau.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 handleSimControlInput(evt);
             }
         });
 
         gamaLabel.setText("Recovery rate");
 
         gama.setText("1");
         gama.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 gamaKeyReleased(evt);
             }
         });
 
         edgeBreakingLabel.setText("Edge breaking rate");
 
         breakingRate.setText("0");
         breakingRate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 breakingRateActionPerformed(evt);
             }
         });
         breakingRate.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 breakingRateKeyReleased(evt);
             }
         });
 
         jLabel6.setText("Time step");
 
         deltaT.setText("0.1");
         deltaT.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 deltaTKeyReleased(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(gamaLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(edgeBreakingLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                             .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addGap(5, 5, 5)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(breakingRate, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                 .addComponent(dynamics, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(gama)
                                 .addComponent(tau, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(deltaT, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(14, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(dynamics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(tau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(gama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(gamaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(edgeBreakingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(breakingRate))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(deltaT))
                 .addGap(5, 5, 5))
         );
 
         statsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Disease statistics"));
 
         javax.swing.GroupLayout statsPanelLayout = new javax.swing.GroupLayout(statsPanel);
         statsPanel.setLayout(statsPanelLayout);
         statsPanelLayout.setHorizontalGroup(
             statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         statsPanelLayout.setVerticalGroup(
             statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 194, Short.MAX_VALUE)
         );
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected node statistics"));
 
         localCC.setText("0.0");
 
         jLabel9.setText("Clustering coef.");
 
         localAPL.setText("0.0");
 
         jLabel11.setText("APL");
 
         jLabel13.setText("Betweenness centrality");
 
         localBC.setText("0.0");
 
         jLabel14.setText("In degree");
 
         in.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         in.setText("0.0");
 
         jLabel15.setText("Out degree");
 
         out.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         out.setText("0.0");
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGap(5, 5, 5)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel11)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(localAPL))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel15)
                         .addGap(18, 18, 18)
                         .addComponent(out))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel14)
                         .addGap(29, 29, 29)
                         .addComponent(in)))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel9)
                         .addGap(18, 18, 18)
                         .addComponent(localCC))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel13)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(localBC)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel15)
                     .addComponent(jLabel13)
                     .addComponent(out)
                     .addComponent(localBC))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel14)
                     .addComponent(jLabel9)
                     .addComponent(in)
                     .addComponent(localCC))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel11)
                     .addComponent(localAPL)))
         );
 
         graphStatsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Graph statistics"));
         graphStatsPanel.setMaximumSize(new java.awt.Dimension(367, 269));
 
         jLabel10.setText("Avg degree = ");
 
         jLabel12.setText("APL = ");
 
         jLabel16.setText("Degree corr. = ");
 
         jLabel17.setText("Clustering coef. = ");
 
         globalCC.setText("0");
 
         globalAPL.setText("0");
 
         globalAvgDegree.setText("0");
 
         globalDegreeCorrelation.setText("0");
 
         jLabel18.setText("Vertex count = ");
 
         globalVertexCount.setText("0");
 
         globalEdgeCount.setText("0");
 
         jLabel19.setText("Edge count = ");
 
         jLabel20.setText("Min degree = ");
 
         globalMinDegree.setText("0");
 
         globalMaxDegree.setText("0");
 
         jLabel21.setText("Max degree = ");
 
         degreeDistPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Degree distribution"));
         degreeDistPanel.setMaximumSize(new java.awt.Dimension(349, 117));
         degreeDistPanel.setMinimumSize(new java.awt.Dimension(349, 117));
         degreeDistPanel.setSize(new java.awt.Dimension(349, 0));
 
         javax.swing.GroupLayout degreeDistPanelLayout = new javax.swing.GroupLayout(degreeDistPanel);
         degreeDistPanel.setLayout(degreeDistPanelLayout);
         degreeDistPanelLayout.setHorizontalGroup(
             degreeDistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 337, Short.MAX_VALUE)
         );
         degreeDistPanelLayout.setVerticalGroup(
             degreeDistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 93, Short.MAX_VALUE)
         );
 
         degDistLogScale.setText("Log scale");
         degDistLogScale.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 degDistLogScaleItemStateChanged(evt);
             }
         });
 
         degDistCumulative.setText("Cumulative");
         degDistCumulative.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 degDistCumulativeItemStateChanged(evt);
             }
         });
 
         javax.swing.GroupLayout graphStatsPanelLayout = new javax.swing.GroupLayout(graphStatsPanel);
         graphStatsPanel.setLayout(graphStatsPanelLayout);
         graphStatsPanelLayout.setHorizontalGroup(
             graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(graphStatsPanelLayout.createSequentialGroup()
                         .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                 .addComponent(jLabel17)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(globalCC, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                 .addComponent(jLabel10)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(globalAvgDegree))
                             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                 .addComponent(jLabel18)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(globalVertexCount))
                             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                 .addComponent(jLabel20)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(globalMinDegree)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                 .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel16)
                                     .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                         .addComponent(jLabel12)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(globalAPL)))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(globalDegreeCorrelation))
                             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                 .addComponent(jLabel19)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(globalEdgeCount))
                             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                                 .addComponent(jLabel21)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(globalMaxDegree)))
                         .addGap(5, 5, 5))
                     .addGroup(graphStatsPanelLayout.createSequentialGroup()
                         .addComponent(degDistLogScale)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(degDistCumulative, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .addGroup(graphStatsPanelLayout.createSequentialGroup()
                         .addComponent(degreeDistPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))))
         );
         graphStatsPanelLayout.setVerticalGroup(
             graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(graphStatsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel17)
                     .addComponent(jLabel12)
                     .addComponent(globalCC)
                     .addComponent(globalAPL))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel10)
                     .addComponent(jLabel16)
                     .addComponent(globalDegreeCorrelation)
                     .addComponent(globalAvgDegree))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel18)
                     .addComponent(globalVertexCount)
                     .addComponent(jLabel19)
                     .addComponent(globalEdgeCount))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel20)
                     .addComponent(globalMinDegree)
                     .addComponent(jLabel21)
                     .addComponent(globalMaxDegree))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(degreeDistPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(graphStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(degDistCumulative)
                     .addComponent(degDistLogScale))
                 .addGap(5, 5, 5))
         );
 
         simControlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Simulation"));
 
         pauseSimToolbarButton.setMnemonic('R');
         pauseSimToolbarButton.setText("Resume");
         pauseSimToolbarButton.setFocusable(false);
         pauseSimToolbarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         pauseSimToolbarButton.setSize(new java.awt.Dimension(93, 29));
         pauseSimToolbarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         pauseSimToolbarButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pauseSimToolbarButtonActionPerformed(evt);
             }
         });
 
         doStepToolbarButton.setMnemonic('S');
         doStepToolbarButton.setText("Do step");
         doStepToolbarButton.setFocusable(false);
         doStepToolbarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         doStepToolbarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         doStepToolbarButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 doStepToolbarButtonActionPerformed(evt);
             }
         });
 
         jButton1.setMnemonic('I');
         jButton1.setText("Infect...");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setMnemonic('H');
         jButton2.setText("Heal all");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout simControlsPanelLayout = new javax.swing.GroupLayout(simControlsPanel);
         simControlsPanel.setLayout(simControlsPanelLayout);
         simControlsPanelLayout.setHorizontalGroup(
             simControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(simControlsPanelLayout.createSequentialGroup()
                 .addGap(5, 5, 5)
                 .addGroup(simControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(doStepToolbarButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(pauseSimToolbarButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(5, 5, 5))
         );
         simControlsPanelLayout.setVerticalGroup(
             simControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(simControlsPanelLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(pauseSimToolbarButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(doStepToolbarButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jButton2))
         );
 
         jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Time between steps, ms"));
 
         speedSlider.setMajorTickSpacing(500);
         speedSlider.setMaximum(2000);
         speedSlider.setPaintLabels(true);
         speedSlider.setPaintTicks(true);
         speedSlider.setValue(500);
         speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 speedSliderStateChanged(evt);
             }
         });
         speedSlider.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 speedSliderKeyPressed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(speedSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(speedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(5, 5, 5))
         );
 
         menuFile.setMnemonic('F');
         menuFile.setText("File");
 
         newDoc.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
                                                                  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         newDoc.setMnemonic('N');
         newDoc.setText("New document");
         newDoc.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newDocActionPerformed(evt);
             }
         });
         menuFile.add(newDoc);
 
         fileSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                                                                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         fileSave.setMnemonic('S');
         fileSave.setText("Save...");
         fileSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fileSaveActionPerformed(evt);
             }
         });
         menuFile.add(fileSave);
 
         fileLoad.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
                                                                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         fileLoad.setMnemonic('L');
         fileLoad.setText("Load...");
         fileLoad.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fileLoadActionPerformed(evt);
             }
         });
         menuFile.add(fileLoad);
 
         fileGenerate1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G,
                                                                         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         fileGenerate1.setMnemonic('G');
         fileGenerate1.setText("Generate...");
         fileGenerate1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fileGenerate1ActionPerformed(evt);
             }
         });
         menuFile.add(fileGenerate1);
 
         fileQuit1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q,
                                                                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         fileQuit1.setMnemonic('Q');
         fileQuit1.setText("Quit");
         fileQuit1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fileQuit1ActionPerformed(evt);
             }
         });
         menuFile.add(fileQuit1);
 
         jMenuBar1.add(menuFile);
 
         jMenu2.setText("View");
 
         dumpToJpg.setText("Save to .jpg");
         dumpToJpg.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dumpToJpgActionPerformed(evt);
             }
         });
         jMenu2.add(dumpToJpg);
 
         jMenuBar1.add(jMenu2);
 
         label2.setText("Label vertices with...");
 
         vertexLabel.add(vDEgree);
         vDEgree.setMnemonic('A');
         vDEgree.setText("Degree");
         vDEgree.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vDEgreeActionPerformed(evt);
             }
         });
         label2.add(vDEgree);
 
         vertexLabel.add(vCC);
         vCC.setMnemonic('B');
         vCC.setText("Clustering coefficient");
         vCC.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vCCActionPerformed(evt);
             }
         });
         label2.add(vCC);
 
         vertexLabel.add(vBC);
         vBC.setMnemonic('C');
         vBC.setText("Betweennesss centrality");
         vBC.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vBCActionPerformed(evt);
             }
         });
         label2.add(vBC);
 
         vertexLabel.add(vDist);
         vDist.setMnemonic('D');
         vDist.setText("Distance from selected vertex");
         vDist.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vDistActionPerformed(evt);
             }
         });
         label2.add(vDist);
 
         vertexLabel.add(vID);
         vID.setMnemonic('E');
         vID.setSelected(true);
         vID.setText("ID");
         vID.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vIDActionPerformed(evt);
             }
         });
         label2.add(vID);
 
         vertexLabel.add(vNone);
         vNone.setMnemonic('F');
         vNone.setText("Nothing");
         vNone.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vNoneActionPerformed(evt);
             }
         });
         label2.add(vNone);
 
         jMenuBar1.add(label2);
 
         label3.setText("Label edges with...");
 
         edgeLabel.add(eWeight);
         eWeight.setMnemonic('H');
         eWeight.setText("Weight");
         eWeight.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eWeightActionPerformed(evt);
             }
         });
         label3.add(eWeight);
 
         edgeLabel.add(eID);
         eID.setMnemonic('I');
         eID.setText("ID");
         eID.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eIDActionPerformed(evt);
             }
         });
         label3.add(eID);
 
         edgeLabel.add(eBC);
         eBC.setMnemonic('J');
         eBC.setText("Centrality");
         eBC.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eBCActionPerformed(evt);
             }
         });
         label3.add(eBC);
 
         edgeLabel.add(eNone);
         eNone.setMnemonic('K');
         eNone.setSelected(true);
         eNone.setText("Nothing");
         eNone.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eNoneActionPerformed(evt);
             }
         });
         label3.add(eNone);
 
         jMenuBar1.add(label3);
 
         layoutMenu.setText("Change layout");
         layoutMenu.setToolTipText("Change the way vertices are positioned");
 
         layouts.add(kk);
         kk.setMnemonic('0');
         kk.setSelected(true);
         kk.setText("KKLayout");
         kk.setToolTipText("Kamada-Kawai algorithm");
         kk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 kkActionPerformed(evt);
             }
         });
         layoutMenu.add(kk);
 
         layouts.add(fr);
         fr.setMnemonic('1');
         fr.setText("FRLayout");
         fr.setToolTipText("Fruchterman- Reingold algorithm");
         fr.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 frActionPerformed(evt);
             }
         });
         layoutMenu.add(fr);
 
         layouts.add(isom);
         isom.setMnemonic('2');
         isom.setText("ISOMLayout");
         isom.setToolTipText("Self-organizing map algorithm");
         isom.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 isomActionPerformed(evt);
             }
         });
         layoutMenu.add(isom);
 
         layouts.add(spring);
         spring.setMnemonic('3');
         spring.setText("SpringLayout");
         spring.setToolTipText("A simple force-based algorithm");
         spring.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 springActionPerformed(evt);
             }
         });
         layoutMenu.add(spring);
 
         layouts.add(circleL);
         circleL.setMnemonic('4');
         circleL.setText("CircleLayout");
         circleL.setToolTipText("Arranges vertices in a circle");
         circleL.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 circleLActionPerformed(evt);
             }
         });
         layoutMenu.add(circleL);
 
         jMenuBar1.add(layoutMenu);
 
         menuSimulation.setText("Simulation");
 
         simPauseMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
         simPauseMenuItem.setText("Pause/ Resume");
         simPauseMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 simPauseMenuItemActionPerformed(evt);
             }
         });
         menuSimulation.add(simPauseMenuItem);
 
         jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
         jMenuItem1.setText("Do step");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         menuSimulation.add(jMenuItem1);
         menuSimulation.add(jSeparator13);
 
         infectButton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, 0));
         infectButton.setText("Infect nodes...");
         infectButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 infectButtonActionPerformed(evt);
             }
         });
         menuSimulation.add(infectButton);
 
         healAllButton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, 0));
         healAllButton.setText("Heal all nodes");
         healAllButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 healAllButtonActionPerformed(evt);
             }
         });
         menuSimulation.add(healAllButton);
 
         jMenuBar1.add(menuSimulation);
 
         menuHelp.setText("Help");
 
         helpHowTo.setText("How to");
         helpHowTo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 helpHowToActionPerformed(evt);
             }
         });
         menuHelp.add(helpHowTo);
 
         helpAbout.setText("About");
         helpAbout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 helpAboutActionPerformed(evt);
             }
         });
         menuHelp.add(helpAbout);
 
         jMenuBar1.add(menuHelp);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(mouseModeToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 138, Short.MAX_VALUE))
                     .addComponent(pane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(simControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(5, 5, 5)
                         .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(statsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(graphStatsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(5, 5, 5))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(mouseModeToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(simControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(graphStatsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(56, 56, 56)
                         .addComponent(pane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     public void loadLayout(String path) throws IOException, ClassNotFoundException {
         persistentLayout.restore(path);
     }
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
     }//GEN-LAST:event_formWindowClosing
 
     private void kkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kkActionPerformed
 //        redisplayCompletely();
         this.changeLayout();
     }//GEN-LAST:event_kkActionPerformed
 
     private void frActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frActionPerformed
 //        redisplayCompletely();
         this.changeLayout();
     }//GEN-LAST:event_frActionPerformed
 
     private void isomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isomActionPerformed
 //        redisplayCompletely();
         this.changeLayout();
     }//GEN-LAST:event_isomActionPerformed
 
     private void springActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_springActionPerformed
 //        redisplayCompletely();
         this.changeLayout();
     }//GEN-LAST:event_springActionPerformed
 
     private void circleLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_circleLActionPerformed
 //        redisplayCompletely();
         this.changeLayout();
     }//GEN-LAST:event_circleLActionPerformed
 
     private void dumpToJpgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dumpToJpgActionPerformed
         FileDialog window = new FileDialog(this, "Save", FileDialog.SAVE);
         window.setSize(500, 500);
         window.setVisible(true);
         String path = window.getDirectory() + window.getFile();
         if (!path.equals("nullnull")) {//if the user clicks CANCEL path will be set to "nullnull"
             if (!path.endsWith(".jpg")) {
                 path += ".jpg";
             }
             File f = new File(path);
             IOClass.writeJPEGImage(vv, f);
         }
 
     }//GEN-LAST:event_dumpToJpgActionPerformed
 
     private void infectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infectButtonActionPerformed
         InfectDisp d = new InfectDisp(g, controller);
     }//GEN-LAST:event_infectButtonActionPerformed
 
     private void healAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_healAllButtonActionPerformed
         if (g != null) {
             controller.setAllSusceptible();
             // fire appropriate event to trigger an update of the Infected graph
             g.fireExtraEvent(new ExtraGraphEvent(g, ExtraGraphEvent.SIM_STEP_COMPLETE));
             vv.repaint();
         } else {
             JOptionPane.showMessageDialog(this, "That makes no sense when the graph is empty!");
         }
     }//GEN-LAST:event_healAllButtonActionPerformed
 
     private void helpHowToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpHowToActionPerformed
         JOptionPane.showMessageDialog(this, Strings.help);
     }//GEN-LAST:event_helpHowToActionPerformed
 
     private void helpAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpAboutActionPerformed
         JOptionPane.showMessageDialog(this, Strings.about);
     }//GEN-LAST:event_helpAboutActionPerformed
 
     private void newDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDocActionPerformed
         controller.generateEmptyGraph();
     }//GEN-LAST:event_newDocActionPerformed
 
     private void fileSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileSaveActionPerformed
         gatherer.showSave(this, g, this.persistentLayout);
     }//GEN-LAST:event_fileSaveActionPerformed
 
     private void fileLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileLoadActionPerformed
         gatherer.showLoad(this);
     }//GEN-LAST:event_fileLoadActionPerformed
 
     private void fileGenerate1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileGenerate1ActionPerformed
         gatherer.showGenerate(this);
     }//GEN-LAST:event_fileGenerate1ActionPerformed
 
     private void fileQuit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileQuit1ActionPerformed
         // TODO a bit rude, bit OK as a temp solution
         System.exit(1);
     }//GEN-LAST:event_fileQuit1ActionPerformed
 
     private void vDEgreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vDEgreeActionPerformed
 //        vertexLabelIndex.put(g, vertexLabel.getSelection().getMnemonic() - 65);
         repaint();
     }//GEN-LAST:event_vDEgreeActionPerformed
 
     private void vCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vCCActionPerformed
 //        vertexLabelIndex.put(g, vertexLabel.getSelection().getMnemonic() - 65);
         repaint();
     }//GEN-LAST:event_vCCActionPerformed
 
     private void vBCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vBCActionPerformed
 //        vertexLabelIndex.put(g, vertexLabel.getSelection().getMnemonic() - 65);
         repaint();
     }//GEN-LAST:event_vBCActionPerformed
 
     private void vDistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vDistActionPerformed
 //        vertexLabelIndex.put(g, vertexLabel.getSelection().getMnemonic() - 65);
         repaint();
     }//GEN-LAST:event_vDistActionPerformed
 
     private void vIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vIDActionPerformed
         repaint();
     }//GEN-LAST:event_vIDActionPerformed
 
     private void vNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vNoneActionPerformed
 //        vertexLabelIndex.put(g, vertexLabel.getSelection().getMnemonic() - 65);
         repaint();
     }//GEN-LAST:event_vNoneActionPerformed
 
     private void eWeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eWeightActionPerformed
 //        edgeLabelIndex.put(g, edgeLabel.getSelection().getMnemonic() - 72);
         repaint();
     }//GEN-LAST:event_eWeightActionPerformed
 
     private void eIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eIDActionPerformed
 //        edgeLabelIndex.put(g, edgeLabel.getSelection().getMnemonic() - 72);
         repaint();
     }//GEN-LAST:event_eIDActionPerformed
 
     private void eBCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eBCActionPerformed
 //        edgeLabelIndex.put(g, edgeLabel.getSelection().getMnemonic() - 72);
         repaint();
     }//GEN-LAST:event_eBCActionPerformed
 
     private void eNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eNoneActionPerformed
 //        edgeLabelIndex.put(g, edgeLabel.getSelection().getMnemonic() - 72);
         repaint();
     }//GEN-LAST:event_eNoneActionPerformed
 
     private void pauseSimToolbarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseSimToolbarButtonActionPerformed
         if (checkEmptyPopupError()) {
             String currText = pauseSimToolbarButton.getText().toLowerCase();
             if (currText.trim().equals("pause")) {
                 pauseSimToolbarButton.setText("Resume");
                 controller.getSimulator().pauseSim();
             } else {
                 pauseSimToolbarButton.setText("Pause   ");
                 controller.getSimulator().resumeSim();
             }
         }
     }//GEN-LAST:event_pauseSimToolbarButtonActionPerformed
 
     public boolean checkEmptyPopupError() {
         if (g.getVertexCount() < 1) {
             JOptionPane.showMessageDialog(this, "That makes no sense when the graph is empty!");
             return false;
         } else {
             return true;
         }
     }
 
     private void doStepToolbarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doStepToolbarButtonActionPerformed
         if (checkEmptyPopupError()) {
             checkEmptyPopupError();
             controller.getSimulator().resumeSimForOneStep();
         }
     }//GEN-LAST:event_doStepToolbarButtonActionPerformed
 
     private void simPauseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simPauseMenuItemActionPerformed
         pauseSimToolbarButton.doClick();
     }//GEN-LAST:event_simPauseMenuItemActionPerformed
 
     private void speedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSliderStateChanged
         JSlider source = (JSlider) evt.getSource();
         if (!source.getValueIsAdjusting()) {
             parseSimulationParameters(null);
         }
     }//GEN-LAST:event_speedSliderStateChanged
 
     private void speedSliderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_speedSliderKeyPressed
         parseSimulationParameters(evt);
     }//GEN-LAST:event_speedSliderKeyPressed
 
     private void dynamicsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dynamicsItemStateChanged
         JComboBox source = (JComboBox) evt.getItemSelectable();
         if (evt.getStateChange() == ItemEvent.SELECTED) {
             if (source.getSelectedItem().toString().equals("SI")) {
                 gamaLabel.setVisible(false);
                 gama.setVisible(false);
                 breakingRate.setVisible(false);
                 edgeBreakingLabel.setVisible(false);
             }
             if (source.getSelectedItem().toString().equals("SIS")) {
                 gamaLabel.setVisible(true);
                 gama.setVisible(true);
                 breakingRate.setVisible(true);
                 edgeBreakingLabel.setVisible(true);
             }
             if (source.getSelectedItem().toString().equals("SIR")) {
                 gamaLabel.setVisible(true);
                 gama.setVisible(true);
 //                gama.setText("1");
                 breakingRate.setVisible(false);
                 edgeBreakingLabel.setVisible(false);
             }
             parseSimulationParameters(null);
             pack();
             validate();
             repaint();
         }
     }//GEN-LAST:event_dynamicsItemStateChanged
 
     private void breakingRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_breakingRateActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_breakingRateActionPerformed
 
     private void handleSimControlInput(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_handleSimControlInput
         parseSimulationParameters(evt);
     }//GEN-LAST:event_handleSimControlInput
 
     private void gamaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gamaKeyReleased
         parseSimulationParameters(evt);
     }//GEN-LAST:event_gamaKeyReleased
 
     private void breakingRateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_breakingRateKeyReleased
         parseSimulationParameters(evt);
     }//GEN-LAST:event_breakingRateKeyReleased
 
     private void deltaTKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deltaTKeyReleased
         parseSimulationParameters(evt);
     }//GEN-LAST:event_deltaTKeyReleased
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         if (checkEmptyPopupError()) {
             checkEmptyPopupError();
             doStepToolbarButton.doClick();
         }
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         if (checkEmptyPopupError()) {
             checkEmptyPopupError();
             infectButton.doClick();
         }
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         if (checkEmptyPopupError()) {
             checkEmptyPopupError();
             healAllButton.doClick();
         }
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
     }//GEN-LAST:event_formKeyPressed
 
     private void selectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectActionPerformed
         graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
         annotationControlsToolbar.setVisible(annotate.isSelected());
     }//GEN-LAST:event_selectActionPerformed
 
     private void editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editActionPerformed
         graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
         annotationControlsToolbar.setVisible(annotate.isSelected());
     }//GEN-LAST:event_editActionPerformed
 
     private void transformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transformActionPerformed
         graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
         annotationControlsToolbar.setVisible(annotate.isSelected());
     }//GEN-LAST:event_transformActionPerformed
 
     private void annotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotateActionPerformed
         graphMouse.setMode(ModalGraphMouse.Mode.ANNOTATING);
         annotationControlsToolbar.setVisible(annotate.isSelected());
     }//GEN-LAST:event_annotateActionPerformed
 
     private void degDistLogScaleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_degDistLogScaleItemStateChanged
         updateDegreeDistributionChart();
     }//GEN-LAST:event_degDistLogScaleItemStateChanged
 
     private void degDistCumulativeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_degDistCumulativeItemStateChanged
         updateDegreeDistributionChart();
     }//GEN-LAST:event_degDistCumulativeItemStateChanged
 
     public JPanel getStatsPanel() {
         return statsPanel;
     }
 
     /**
      * Parses the text of the provided text field. If that is not a valid
      * double, the text is highlighted to grab the user's attention
      *
      * @return
      */
     private double parseValueOrColourComponentOnError(JTextField textField) {
         double value = 0;
         try {
             value = Double.parseDouble(textField.getText());
             textField.setForeground(Color.black);
         } catch (NumberFormatException ex) {
             textField.setForeground(Color.red);
         }
         return value;//TODO must return default value for field
     }
 
     //TODO convert this from a keylistener to a keybinding, http://docs.oracle.com/javase/tutorial/uiswing/misc/keybinding.html
     private void parseSimulationParameters(KeyEvent evt) {
         //check the current state of the fields
         //parse the contents of the text field that should be active (based on the combos)
         //and attach them to the graph as a Dynamics object
         //attach the dynamics setting to the graph
         double tauValue = parseValueOrColourComponentOnError(tau);
         double gamaValue = parseValueOrColourComponentOnError(gama);
         double deltaTValue = parseValueOrColourComponentOnError(deltaT);
         double brakingRateValue = parseValueOrColourComponentOnError(breakingRate);
 
         if (dynamics.getSelectedItem().toString().equals("SIR")) {
             g.setDynamics(new SIRDynamics(tauValue, deltaTValue, gamaValue));
         } else if (dynamics.getSelectedItem().toString().equals("SIS")) {
             g.setDynamics(new SISDynamics(tauValue, deltaTValue, gamaValue, brakingRateValue));
         } else {
             g.setDynamics(new SIDynamics(tauValue, deltaTValue));
         }
        g.setSleepTimeBetweenSteps(speedSlider.getValue()+20);
         //make sure the graphs is in a proper state
         controller.validateNodeStates();
     }
 
     /**
      * Initialises the display
      */
     public void redisplayCompletely() {
         //clear all previous content
         pane.removeAll();
         pane.setLayout(new BorderLayout());
 
         persistentLayout = new PersistentLayoutImpl<MyVertex, MyEdge>(getSelectedGraphLayout(g));
 
         vv = new VisualizationViewer<MyVertex, MyEdge>(persistentLayout, pane.getSize());
         initDemoMap();
 
         this.icons = new IconsStore(vv.getPickedVertexState());
 //        vv.getRenderer().setVertexRenderer(new CustomVertexRenderer(vv.getPickedVertexState(), false));
         vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.black));
         vv.getRenderContext().setArrowDrawPaintTransformer(new ConstantTransformer(Color.black));
         vv.getRenderContext().setVertexLabelTransformer(new CustomVertexLabeler(this.stats));
         vv.getRenderContext().setEdgeLabelTransformer(new CustomEdgeLabeller(this.stats));
         vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.black));
         vv.getRenderer().getVertexLabelRenderer().setPositioner(new CenterLabelPositioner());
         vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
 
         //#########  MOUSE  PLUGINS  ###############
         graphMouse = new CustomGraphMouse(this, this.controller, this.stats, vv.getRenderContext(),
                 controller.getVertexFactory(), controller.getEdgeFactory());
         graphMouse.loadPlugins();
         vv.setGraphMouse(graphMouse);
 
         annotationControls = new AnnotationControls<MyVertex, MyEdge>(
                 graphMouse.getAnnotatingPlugin());
         annotationControlsToolbar = annotationControls.getAnnotationsToolBar();
         annotationControlsToolbar.setFocusable(false);
         annotationControlsToolbar.setFocusTraversalKeysEnabled(false);
         for (int i = 0; i < annotationControlsToolbar.getComponents().length; i++) {
             Component component = annotationControlsToolbar.getComponents()[i];
             component.setFocusTraversalKeysEnabled(false);
             component.setFocusable(false);
         }
 
         for (int i = 0; i < mouseModeToolbar.getComponents().length; i++) {
             Component comp = mouseModeToolbar.getComponents()[i];
             if (comp instanceof JToolBar) {
                 mouseModeToolbar.remove(comp);
             }
 
         }
 
         final VertexIconShapeTransformer<Number> vertexIconShapeTransformer
                 = new CustomVertexIconShapeTransformer(new EllipseVertexShapeTransformer(),
                         this.icons);
 
         vv.getRenderContext().setVertexShapeTransformer(vertexIconShapeTransformer);
         vv.getRenderContext().setVertexIconTransformer(this.icons);
 
         mouseModeToolbar.add(annotationControlsToolbar, BorderLayout.SOUTH);
         annotationControlsToolbar.setVisible(annotate.isSelected());
 
         pane.add(vv, BorderLayout.CENTER);
         pane.setVisible(true);
         redisplayPartially();
         parseSimulationParameters(null);
         //initially display nothing
         initDegreeDistributionChart();
         updateStatsDisplay();
 
         mouseModeButtonGroup.clearSelection();
         select.doClick();
 
         controller.getSimulator().createInfectedCountGraph(this.getStatsPanel());
         controller.getSimulator().resetSimulation();
     }
 
     private void initDemoMap() {
         BackgroundImageController.getInstance().setGraphBackgroundImage(vv, "maps/UK_Map.png",
                 1.75, 1.7, new Color(10, 20, 20));
         scaler.scale(vv, .5f, vv.getCenter());
         vv.getRenderContext().setEdgeDrawPaintTransformer(new ConstantTransformer(Color.white));
     }
 
     public static void redisplayPartially() {
         pane.validate();
         vv.repaint();
         pane.repaint();
     }
 
     /**
      * Returns an appropriate layout based on the state of the layout selection
      * buttons
      *
      * @return
      */
     private Layout<MyVertex, MyEdge> getSelectedGraphLayout(Graph g) {
         //ascii code of 0 is 48, 1 is 49, etc, and the menus have been assigned mnemonics from 0-5
         int type = layouts.getSelection().getMnemonic() - 48;
 //        System.out.println("layout is: " + type);
         Layout<MyVertex, MyEdge> l = null;
         switch (type) {
             case 0: {
                 l = getControlledKKLayout(g);
                 break;
             }
             case 1: {
                 l = new FRLayout<MyVertex, MyEdge>(g);
                 break;
             }
             case 2: {
                 l = new ISOMLayout<MyVertex, MyEdge>(g);
                 break;
             }
             case 3: {
                 l = new SpringLayout<MyVertex, MyEdge>(g);
 //                ((SpringLayout)layout).setForceMultiplier(10.0); //how close nodes are together
                 ((SpringLayout) l).setRepulsionRange(10000);
                 break;
             }
             case 4: {
                 l = new CircleLayout<MyVertex, MyEdge>(g);
                 break;
             }
             case 5: {
                 l = new BalloonLayout<MyVertex, MyEdge>((Forest<MyVertex, MyEdge>) g);
                 break;
             }
             default: {
                 l = getControlledKKLayout(g);
             }
         }
         return l;
     }
 
     // This method exists as a workaround for the 'unsettling graph' issue. It does two things:
     // 1. Limits the maximum number of iterations the Kamada-Kawai vertex positioning algorithm will make
     // before it settles down, based on the number of vertices in our generated graph.
     // By default, the maximum number of iterations done in the step() method is 2000. Even with smaller graphs,
     // sometimes the resulting distance between vertices does not satisfy the algorithm and it continues to displace 
     // them until the iteration limit is reached.
     // 2. Disables the local minimum escape technique used by the positioning algorhithm.
     // This technique promotes a more aggresive approach to the vertex displacement, which in larger graphs
     // could actually result in longer layout adjustment times.
     // 
     private Layout getControlledKKLayout(Graph g) {
         KKLayout kkLayout = new KKLayout<MyVertex, MyEdge>(g);
         kkLayout.setMaxIterations(g.getVertexCount());
         kkLayout.setExchangeVertices(false);
         return kkLayout;
     }
 
     /**
      * Based on AnimatingAddNodeDemo, should animate layout change
      */
     public void changeLayout() {
         try {
             Layout oldLayout = vv.getGraphLayout();
             PersistentLayoutImpl newLayout =  new PersistentLayoutImpl<MyVertex, MyEdge>(getSelectedGraphLayout(g));
             this.persistentLayout = newLayout;
             newLayout.setSize(oldLayout.getSize());
             oldLayout.initialize();
             Relaxer relaxer = new VisRunner((IterativeContext) oldLayout);
             relaxer.stop();
             relaxer.prerelax();
             LayoutTransition<MyVertex, MyEdge> lt = new LayoutTransition<MyVertex, MyEdge>(vv, oldLayout, newLayout);
             Animator animator = new Animator(lt);
             animator.start();
             vv.repaint();
         } catch (Exception ex) {
             System.out.println("Error while changing layout: " + ex.getMessage());
 
         }
 
     }
 
     /**
      * returns the index of the selectedvertex labeling option in the menu
      *
      * @return
      */
     public static int getSelectedVertexLabelingOption() {
         //the return type will begin with one of these options
         //Degree, Clustering, Centrality, Label
         //mnemonics here are set to A,B,C,D, so subtract 65 to get the selected index
         return vertexLabel.getSelection().getMnemonic() - 65;
     }
 
     public static int getSelectedEdgeLabelingOption() {
         //first one is H, so subtract 72
         return edgeLabel.getSelection().getMnemonic() - 72;
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     static javax.swing.JToggleButton annotate;
     private static javax.swing.JTextField breakingRate;
     private javax.swing.JRadioButtonMenuItem circleL;
     private static javax.swing.JCheckBox degDistCumulative;
     private static javax.swing.JCheckBox degDistLogScale;
     private static javax.swing.JPanel degreeDistPanel;
     private static javax.swing.JTextField deltaT;
     static javax.swing.JButton doStepToolbarButton;
     private javax.swing.JMenuItem dumpToJpg;
     private static javax.swing.JComboBox dynamics;
     private javax.swing.JRadioButtonMenuItem eBC;
     private javax.swing.JRadioButtonMenuItem eID;
     private javax.swing.JRadioButtonMenuItem eNone;
     private javax.swing.JRadioButtonMenuItem eWeight;
     private javax.swing.JLabel edgeBreakingLabel;
     private static javax.swing.ButtonGroup edgeLabel;
     static javax.swing.JToggleButton edit;
     private javax.swing.JMenuItem fileGenerate1;
     private javax.swing.JMenuItem fileLoad;
     private javax.swing.JMenuItem fileQuit1;
     private javax.swing.JMenuItem fileSave;
     private javax.swing.JRadioButtonMenuItem fr;
     private static javax.swing.JTextField gama;
     private javax.swing.JLabel gamaLabel;
     static javax.swing.JLabel globalAPL;
     static javax.swing.JLabel globalAvgDegree;
     static javax.swing.JLabel globalCC;
     static javax.swing.JLabel globalDegreeCorrelation;
     static javax.swing.JLabel globalEdgeCount;
     static javax.swing.JLabel globalMaxDegree;
     static javax.swing.JLabel globalMinDegree;
     static javax.swing.JLabel globalVertexCount;
     private javax.swing.JPanel graphStatsPanel;
     private javax.swing.JMenuItem healAllButton;
     private javax.swing.JMenuItem helpAbout;
     private javax.swing.JMenuItem helpHowTo;
     private static javax.swing.JLabel in;
     private javax.swing.JMenuItem infectButton;
     private javax.swing.JRadioButtonMenuItem isom;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     static javax.swing.JLabel jLabel10;
     private static javax.swing.JLabel jLabel11;
     static javax.swing.JLabel jLabel12;
     private static javax.swing.JLabel jLabel13;
     private static javax.swing.JLabel jLabel14;
     private static javax.swing.JLabel jLabel15;
     static javax.swing.JLabel jLabel16;
     static javax.swing.JLabel jLabel17;
     static javax.swing.JLabel jLabel18;
     static javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     static javax.swing.JLabel jLabel20;
     static javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel6;
     private static javax.swing.JLabel jLabel9;
     private static javax.swing.JMenu jMenu2;
     private static javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JSeparator jSeparator13;
     private javax.swing.JRadioButtonMenuItem kk;
     private static javax.swing.JMenu label2;
     private static javax.swing.JMenu label3;
     private static javax.swing.JMenu layoutMenu;
     private static javax.swing.ButtonGroup layouts;
     private static javax.swing.JLabel localAPL;
     private static javax.swing.JLabel localBC;
     private static javax.swing.JLabel localCC;
     private static javax.swing.JMenu menuFile;
     private static javax.swing.JMenu menuHelp;
     private static javax.swing.JMenu menuSimulation;
     private static javax.swing.ButtonGroup mouseModeButtonGroup;
     static javax.swing.JToolBar mouseModeToolbar;
     private javax.swing.JMenuItem newDoc;
     private static javax.swing.JLabel out;
     private static javax.swing.JPanel pane;
     static javax.swing.JButton pauseSimToolbarButton;
     static javax.swing.JToggleButton select;
     private javax.swing.JPanel simControlsPanel;
     private javax.swing.JMenuItem simPauseMenuItem;
     private static javax.swing.JSlider speedSlider;
     private javax.swing.JRadioButtonMenuItem spring;
     private static javax.swing.JPanel statsPanel;
     private static javax.swing.JTextField tau;
     static javax.swing.JToggleButton transform;
     private javax.swing.JRadioButtonMenuItem vBC;
     private javax.swing.JRadioButtonMenuItem vCC;
     private javax.swing.JRadioButtonMenuItem vDEgree;
     private javax.swing.JRadioButtonMenuItem vDist;
     private javax.swing.JRadioButtonMenuItem vID;
     private javax.swing.JRadioButtonMenuItem vNone;
     private static javax.swing.ButtonGroup vertexLabel;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void handleGraphEvent(GraphEvent<MyVertex, MyEdge> evt) {
         if (this.handlingEvents) {
             //invoked when a vertex/edge is added/deleted
             updateStatsDisplay();
         }
     }
 
     @Override
     public void handleExtraGraphEvent(ExtraGraphEvent<MyVertex, MyEdge> evt) {
         if (this.handlingEvents) {
             if (evt.type == ExtraGraphEvent.SIM_STEP_COMPLETE) {
                 controller.getSimulator().updateChartUnderlyingData();
                 controller.getSimulator().updateChartAxisParameters();
                 redisplayPartially();
             }
             if (evt.type == ExtraGraphEvent.STATS_CHANGED) {
                 updateStatsDisplay();
             }
             if (evt.type == ExtraGraphEvent.GRAPH_REPLACED) {
                 vv.getGraphLayout().setGraph(this.g);
                 changeLayout();
                 controller.getSimulator().resetSimulation();
             }
         }
     }
 
     public Controller getController() {
         return controller;
     }
 }
