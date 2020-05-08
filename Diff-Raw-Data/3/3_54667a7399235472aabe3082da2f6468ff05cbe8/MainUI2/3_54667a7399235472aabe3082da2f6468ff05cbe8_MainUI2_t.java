 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * MainUI2.java
  *
  * Created on 07-Aug-2009, 13:18:40
  */
 package heartsim.gui;
 
 import heartsim.DataLoader;
 import heartsim.ca.CAModel;
 import heartsim.ca.Nishiyama;
 import heartsim.ca.Tyson;
 import heartsim.ca.parameter.CAModelParameter;
 import heartsim.gui.layout.SpringUtilities;
 import heartsim.gui.util.FileChooserFilter;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 
 /**
  *
  * @author Lee Boynton
  */
 public class MainUI2 extends javax.swing.JFrame
 {
     private CAModel CAModel; // CA model being used in the simulation
     private final boolean DEBUG = true; // if debug is true then print output messages
     private File svgFile; // svg containing heart geometry
     private double cellSize = 1;
     private int time; // time to run simulation
     private int currentTime = 0; // current time in simulation
     private int stimX = 459; // X-axis location cell which should be stimulated
     private int stimY = 297; // Y-axis location cell which should be stimulated
     private SwingWorker<Object, Void> visualisationSwingWorker;
     private boolean simulationRunning = false;
     private DataLoader loader = new DataLoader(new String[]
             {
                 "ventricles"
             });
 
     /** Creates new form MainUI2 */
     public MainUI2()
     {
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception ex)
         {
             System.err.println("Unable to use system look and feel");
         }
 
         initComponents();
 
         // center frame on window
         this.setLocationRelativeTo(null);
 
         // load initially selected CA model
         cboBoxModel.setSelectedIndex(0);
 
         // initially load an SVG file
         setSvgFile(new File("geometry_data/heart.svg"));
     }
 
     /**
      * Loads the available CA models into the combo box in the GUI. New CA models
      * should be added in here
      * @return Combo box model containing all the CA models
      */
     private ComboBoxModel loadCAModels()
     {
         DefaultComboBoxModel CAModels = new DefaultComboBoxModel();
 
         // add CA models to combo box model
         CAModels.addElement(new Nishiyama());
         CAModels.addElement(new Tyson());
 
         return CAModels;
     }
 
     private void loadTissueCheckboxes()
     {
         String[] tissues = loader.getPathsInFile();
 
         ((GridLayout) pnlTissue.getLayout()).setRows(tissues.length);
 
         for (String tissue : tissues)
         {
             JCheckBox chkBox = new JCheckBox(tissue);
             pnlTissue.add(chkBox);
         }
     }
 
     /**
      * Called when the CA model changes. Loads the CA model parameters into the
      * GUI.
      */
     private void loadModelParameters()
     {
         output("Loading model parameters");
 
         // skip loading parameters if model hasn't changed
         if (CAModel != null && CAModel.equals(cboBoxModel.getSelectedItem()))
         {
             output("Same model selected, skipping.");
             return;
         }
 
         // get the CA model that was selected
         CAModel = (CAModel) cboBoxModel.getSelectedItem();
 
         output("Model selected is " + CAModel.getName());
 
         // clear any existing parameters in the GUI
         pnlParameters.removeAll();
 
         // use spring layout for this panel
         pnlParameters.setLayout(new SpringLayout());
 
         // add the tissue combo box selector first
         pnlParameters.add(lblTissue);
         pnlParameters.add(cboBoxTissue);
 
         // add the CA model combo box selector
         pnlParameters.add(lblModel);
         pnlParameters.add(cboBoxModel);
 
         // loop through the parameters in the CA model and put them on the GUI
         for (final CAModelParameter p : CAModel.getParameters().values())
         {
             output("Found parameter: " + p.getName());
 
             JLabel lbl = new JLabel(p.getName());
             final JTextField txt = new JTextField();
             txt.setText(p.getValue().toString());
             txt.setToolTipText(p.getDescription());
             txt.addKeyListener(new KeyAdapter()
             {
                 @Override
                 public void keyReleased(KeyEvent evt)
                 {
                     if (!p.setValue(txt.getText()))
                     {
                         output("Invalid parameter: " + txt.getText());
                         txt.setForeground(Color.red);
                     }
                     else
                     {
                         txt.setForeground(null);
                     }
                     CAModel.setParameter(p.getName(), p);
                 }
             });
 
             pnlParameters.add(lbl);
             pnlParameters.add(txt);
         }
 
         // place components in grid
         SpringUtilities.makeCompactGrid(pnlParameters,
                 CAModel.getParameters().size() + 2, 2, //rows, cols
                 10, 12, //initX, initY
                 12, 6);       //xPad, yPad
 
         pnlParameters.revalidate();
         pack();
     }
 
     private void output(String output)
     {
         if (DEBUG)
         {
             System.out.println(output);
         }
     }
 
     private void setStatusBar(String text)
     {
         lblStatus.setText(text);
     }
 
     private void setSvgFile(File svgFile)
     {
         if (!svgFile.exists())
         {
             return;
         }
 
         this.svgFile = svgFile;
         setStatusBar("Loaded file: " + svgFile.getName());
         loadHeart();
         loadTissueCheckboxes();
     }
 
     private void loadHeart()
     {
         loader.setFile(svgFile.getPath());
         loader.setSize(cellSize);
         pnlDisplay.setPreferredSize(new Dimension(loader.getGrid()[0].length, loader.getGrid().length));
         pnlDisplay.setSize(new Dimension(loader.getGrid()[0].length, loader.getGrid().length));
         positionHeartInCentre();
         CAModel.setCells(loader.getGrid());
         CAModel.setSize(pnlDisplay.getSize());
         pnlDisplay.reset();
         pnlDisplay.setPaths(loader.getPathShapes());
         pnlDisplay.repaint();
     }
 
     private void positionHeartInCentre()
     {
         int x = (scrollPaneDisplay.getWidth() / 2) - (pnlDisplay.getPreferredSize().width / 2);
         int y = (scrollPaneDisplay.getHeight() / 2) - (pnlDisplay.getPreferredSize().height / 2);
         pnlDisplayContainer.add(pnlDisplay, new org.netbeans.lib.awtextra.AbsoluteConstraints(x, y, -1, -1));
     }
 
     private void resetSimulation()
     {
        time = 0;
         currentTime = 0;
         CAModel.initCells();
         CAModel.stimulate(stimX, stimY);
         btnStart.setEnabled(true);
         btnStepForward.setEnabled(true);
         btnStop.setEnabled(false);
         visualisationSwingWorker = new VisualisationSwingWorker();
     }
 
     private void runSimulation()
     {
         btnStart.setEnabled(false);
         btnStepForward.setEnabled(false);
         btnStop.setEnabled(true);
 
         if (!CAModel.isCell(stimX, stimY))
         {
             output("No cell at X: " + stimX + " Y: " + stimY);
             return;
         }
 
         output("Started simulation at X: " + stimX + " Y: " + stimY);
 
         byte[] data = pnlDisplay.getBuffer();
 
         int[][] u = CAModel.getU();
 
         for (int t = 0; t < this.time; t++)
         {
             int k = 0;
 
             for (int i = 0; i < u.length; i++)
             {
                 for (int j = 0; j < u[0].length; j++)
                 {
                     byte val = (byte) u[i][j];
                     if (val == 0)
                     {
                         data[k] = -10;
                     }
                     else
                     {
                         data[k] = (byte) u[i][j];
                     }
                     k++;
                 }
             }
 
             /*if (chartData.getColumnCount() > 6)
             {
             chartData.removeColumn(0);
             }*/
 
             currentTime++;
             //chartData.addValue(u[stimX][stimY], "Voltage", String.valueOf(currentTime));
             //chartData.addValue(CAModel.getV(stimX, stimY), "Recovery", String.valueOf(currentTime));
             CAModel.step();
             pnlDisplay.repaint();
 
         }
 
         btnStart.setEnabled(true);
         btnStepForward.setEnabled(true);
         btnStop.setEnabled(false);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         lblStatus = new javax.swing.JLabel();
         toolBar = new javax.swing.JToolBar();
         btnOpen = new javax.swing.JButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         btnStart = new javax.swing.JButton();
         btnStop = new javax.swing.JButton();
         btnStepForward = new javax.swing.JButton();
         jSeparator2 = new javax.swing.JToolBar.Separator();
         btnZoomIn = new javax.swing.JButton();
         btnZoomOut = new javax.swing.JButton();
         jSeparator3 = new javax.swing.JToolBar.Separator();
         btnAbout = new javax.swing.JButton();
         scrollPaneDisplay = new javax.swing.JScrollPane();
         pnlDisplayContainer = new javax.swing.JPanel();
         pnlDisplay = new heartsim.gui.BinaryPlotPanel();
         tabbedPane = new javax.swing.JTabbedPane();
         pnlCA = new javax.swing.JPanel();
         pnlParameters = new javax.swing.JPanel();
         lblTissue = new javax.swing.JLabel();
         lblModel = new javax.swing.JLabel();
         cboBoxTissue = new javax.swing.JComboBox();
         cboBoxModel = new javax.swing.JComboBox();
         jPanel2 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         jTextField2 = new javax.swing.JTextField();
         jPanel3 = new javax.swing.JPanel();
         pnlTissue = new javax.swing.JPanel();
         lblTime = new javax.swing.JLabel();
         txtTime = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jLabel2 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         lblStatus.setText("Status");
 
         toolBar.setFloatable(false);
         toolBar.setRollover(true);
 
         btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/document-open.png"))); // NOI18N
         btnOpen.setToolTipText("Open SVG file containing heart geometry");
         btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnOpen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnOpenActionPerformed(evt);
             }
         });
         toolBar.add(btnOpen);
         toolBar.add(jSeparator1);
 
         btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/media-playback-start.png"))); // NOI18N
         btnStart.setToolTipText("Run the simulation with the specified parameters");
         btnStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnStart.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStartActionPerformed(evt);
             }
         });
         toolBar.add(btnStart);
 
         btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/media-playback-stop.png"))); // NOI18N
         btnStop.setToolTipText("Stop simulation");
         btnStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnStop.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStopActionPerformed(evt);
             }
         });
         toolBar.add(btnStop);
 
         btnStepForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/media-seek-forward.png"))); // NOI18N
         btnStepForward.setToolTipText("Step simulation forward");
         btnStepForward.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnStepForward.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnStepForward.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStepForwardActionPerformed(evt);
             }
         });
         toolBar.add(btnStepForward);
         toolBar.add(jSeparator2);
 
         btnZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/zoom-in.png"))); // NOI18N
         btnZoomIn.setToolTipText("Zoom in");
         btnZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnZoomIn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnZoomInActionPerformed(evt);
             }
         });
         toolBar.add(btnZoomIn);
 
         btnZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/zoom-out.png"))); // NOI18N
         btnZoomOut.setToolTipText("Zoom out");
         btnZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnZoomOut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnZoomOutActionPerformed(evt);
             }
         });
         toolBar.add(btnZoomOut);
         toolBar.add(jSeparator3);
 
         btnAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/help-browser.png"))); // NOI18N
         btnAbout.setToolTipText("About");
         btnAbout.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnAbout);
 
         pnlDisplayContainer.setBackground(java.awt.Color.white);
         pnlDisplayContainer.addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 pnlDisplayContainerComponentResized(evt);
             }
         });
         pnlDisplayContainer.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         pnlDisplay.setBackground(java.awt.Color.white);
         pnlDisplay.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 pnlDisplayMousePressed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlDisplayLayout = new javax.swing.GroupLayout(pnlDisplay);
         pnlDisplay.setLayout(pnlDisplayLayout);
         pnlDisplayLayout.setHorizontalGroup(
             pnlDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 157, Short.MAX_VALUE)
         );
         pnlDisplayLayout.setVerticalGroup(
             pnlDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 157, Short.MAX_VALUE)
         );
 
         pnlDisplayContainer.add(pnlDisplay, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, -1, -1));
 
         scrollPaneDisplay.setViewportView(pnlDisplayContainer);
 
         lblTissue.setText("Tissue");
 
         lblModel.setText("Model");
 
         cboBoxTissue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ventricles" }));
 
         cboBoxModel.setModel(loadCAModels());
         cboBoxModel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cboBoxModelActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlParametersLayout = new javax.swing.GroupLayout(pnlParameters);
         pnlParameters.setLayout(pnlParametersLayout);
         pnlParametersLayout.setHorizontalGroup(
             pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlParametersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblTissue)
                     .addComponent(lblModel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(cboBoxTissue, 0, 136, Short.MAX_VALUE)
                     .addComponent(cboBoxModel, 0, 136, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlParametersLayout.setVerticalGroup(
             pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlParametersLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblTissue)
                     .addComponent(cboBoxTissue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblModel)
                     .addComponent(cboBoxModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         javax.swing.GroupLayout pnlCALayout = new javax.swing.GroupLayout(pnlCA);
         pnlCA.setLayout(pnlCALayout);
         pnlCALayout.setHorizontalGroup(
             pnlCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pnlParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         pnlCALayout.setVerticalGroup(
             pnlCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlCALayout.createSequentialGroup()
                 .addComponent(pnlParameters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(395, Short.MAX_VALUE))
         );
 
         tabbedPane.addTab("Model", pnlCA);
 
         jLabel4.setText("Heart rate");
 
         jTextField2.setText("70");
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel4)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(428, Short.MAX_VALUE))
         );
 
         tabbedPane.addTab("Pacemaker", jPanel2);
 
         pnlTissue.setLayout(new java.awt.GridLayout(1, 1));
 
         lblTime.setText("Time");
 
         txtTime.setText("500");
 
         jLabel1.setText("Speed");
 
         jLabel2.setText("Paths:");
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(pnlTissue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblTime)
                             .addComponent(jLabel1))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                             .addComponent(txtTime, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)))
                     .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblTime)
                     .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnlTissue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(366, Short.MAX_VALUE))
         );
 
         tabbedPane.addTab("Simulation", jPanel3);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(scrollPaneDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(scrollPaneDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                     .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lblStatus)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void cboBoxModelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cboBoxModelActionPerformed
     {//GEN-HEADEREND:event_cboBoxModelActionPerformed
         loadModelParameters();
     }//GEN-LAST:event_cboBoxModelActionPerformed
 
     private void btnOpenActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnOpenActionPerformed
     {//GEN-HEADEREND:event_btnOpenActionPerformed
         JFileChooser chooser = new JFileChooser(".");
         chooser.setFileFilter(new FileChooserFilter("svg", "SVG Files"));
         int result = chooser.showOpenDialog(this);
 
         if (result == JFileChooser.APPROVE_OPTION)
         {
             setSvgFile(chooser.getSelectedFile());
         }
     }//GEN-LAST:event_btnOpenActionPerformed
 
     private void btnZoomInActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnZoomInActionPerformed
     {//GEN-HEADEREND:event_btnZoomInActionPerformed
         cellSize = cellSize - .5;
         loadHeart();
     }//GEN-LAST:event_btnZoomInActionPerformed
 
     private void btnZoomOutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnZoomOutActionPerformed
     {//GEN-HEADEREND:event_btnZoomOutActionPerformed
         cellSize = cellSize + .5;
         loadHeart();
     }//GEN-LAST:event_btnZoomOutActionPerformed
 
     private void btnStartActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStartActionPerformed
     {//GEN-HEADEREND:event_btnStartActionPerformed
         resetSimulation();
         lblStatus.setText("Started simulation at X: " + stimX + " Y: " + stimY);
         visualisationSwingWorker.execute();
     }//GEN-LAST:event_btnStartActionPerformed
 
     private void btnStopActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStopActionPerformed
     {//GEN-HEADEREND:event_btnStopActionPerformed
         resetSimulation();
     }//GEN-LAST:event_btnStopActionPerformed
 
     private void pnlDisplayMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_pnlDisplayMousePressed
     {//GEN-HEADEREND:event_pnlDisplayMousePressed
         stimX = evt.getY();
         stimY = evt.getX();
 
         if (!simulationRunning)
         {
             this.btnStartActionPerformed(null);
         }
 
         CAModel.stimulate(stimX, stimY);
     }//GEN-LAST:event_pnlDisplayMousePressed
 
     private void btnStepForwardActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStepForwardActionPerformed
     {//GEN-HEADEREND:event_btnStepForwardActionPerformed
         if (currentTime == 0)
         {
             resetSimulation();
         }
         lblStatus.setText("Stepped simulation at X: " + stimX + " Y: " + stimY);
         time = 1;
         runSimulation();
         btnStepForward.requestFocus();
     }//GEN-LAST:event_btnStepForwardActionPerformed
 
     private void pnlDisplayContainerComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_pnlDisplayContainerComponentResized
     {//GEN-HEADEREND:event_pnlDisplayContainerComponentResized
         positionHeartInCentre();
     }//GEN-LAST:event_pnlDisplayContainerComponentResized
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[])
     {
         java.awt.EventQueue.invokeLater(new Runnable()
         {
             public void run()
             {
                 new MainUI2().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAbout;
     private javax.swing.JButton btnOpen;
     private javax.swing.JButton btnStart;
     private javax.swing.JButton btnStepForward;
     private javax.swing.JButton btnStop;
     private javax.swing.JButton btnZoomIn;
     private javax.swing.JButton btnZoomOut;
     private javax.swing.JComboBox cboBoxModel;
     private javax.swing.JComboBox cboBoxTissue;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar.Separator jSeparator2;
     private javax.swing.JToolBar.Separator jSeparator3;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JLabel lblModel;
     private javax.swing.JLabel lblStatus;
     private javax.swing.JLabel lblTime;
     private javax.swing.JLabel lblTissue;
     private javax.swing.JPanel pnlCA;
     private heartsim.gui.BinaryPlotPanel pnlDisplay;
     private javax.swing.JPanel pnlDisplayContainer;
     private javax.swing.JPanel pnlParameters;
     private javax.swing.JPanel pnlTissue;
     private javax.swing.JScrollPane scrollPaneDisplay;
     private javax.swing.JTabbedPane tabbedPane;
     private javax.swing.JToolBar toolBar;
     private javax.swing.JTextField txtTime;
     // End of variables declaration//GEN-END:variables
 
     public class VisualisationSwingWorker extends SwingWorker<Object, Void>
     {
         @Override
         public Object doInBackground() throws Exception
         {
             simulationRunning = true;
             time = Integer.parseInt(txtTime.getText());
             runSimulation();
 
             return null;
         }
 
         @Override
         protected void done()
         {
             super.done();
             btnStepForward.setEnabled(true);
             btnStart.setEnabled(true);
             btnStop.setEnabled(false);
             lblStatus.setText("Simulation finished");
             simulationRunning = false;
         }
     }
 }
