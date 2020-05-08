 /*
  * DatasetCreatorView.java
  */
 package com.voet.datasetcreator;
 
 import com.voet.datasetcreator.data.MetaDataAccessor;
 import com.voet.datasetcreator.data.entities.SchemaMapper;
 import com.voet.datasetcreator.data.entities.TableMapper;
 import com.voet.datasetcreator.io.DatasetWriter;
 import com.voet.datasetcreator.swing.MyCheckBoxRenderer;
 import com.voet.datasetcreator.swing.MyNumericInputVerifier;
 import com.voet.datasetcreator.swing.MyTableModel;
 import com.voet.datasetcreator.util.ConnectionStringUtil;
 import com.voet.datasetcreator.util.Tuple;
 import javax.swing.ButtonModel;
 import javax.swing.UIManager.LookAndFeelInfo;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.List;
 import javax.swing.DefaultCellEditor;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JToggleButton.ToggleButtonModel;
 import javax.swing.UIManager;
 import javax.swing.table.TableColumn;
 import net.infonode.gui.laf.InfoNodeLookAndFeel;
 import org.jvnet.substance.SubstanceLookAndFeel;
 
 /**
  * The application's main frame.
  */
 public class DatasetCreatorView extends FrameView {
 
     public DatasetCreatorView( SingleFrameApplication app ) {
         super( app );
 
         initComponents();
         // hide the panels upon initial load
         pnlConnInfo.setVisible( false );
         pnlTableNames.setVisible( false );
         pnlOptions.setVisible( false );
 
 
         // Build the list of known L&F dynamically
         String curLAF = UIManager.getLookAndFeel().getClass().getName();
         LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
         if ( installedLookAndFeels != null && installedLookAndFeels.length > 0 ){
             for ( LookAndFeelInfo info : installedLookAndFeels ){
                 boolean selected = ( info.getClassName().equals( curLAF ));
                 JRadioButtonMenuItem item = new JRadioButtonMenuItem( info.getName(), selected );
                 btnGrpLAF.add( item );
                 item.setActionCommand( info.getClassName() );
                 item.addActionListener( new ActionListener(){
 
                     @Override
                     public void actionPerformed( ActionEvent e ) {
                         DatasetCreatorApp.updateLAF( e.getActionCommand() );
                     }
                 });
                 mnuLAF.add( item );
             }
         }
 
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger( "StatusBar.messageTimeout" );
         messageTimer = new Timer( messageTimeout, new ActionListener() {
 
             public void actionPerformed( ActionEvent e ) {
                 statusMessageLabel.setText( "" );
             }
         } );
         messageTimer.setRepeats( false );
         int busyAnimationRate = resourceMap.getInteger(
                 "StatusBar.busyAnimationRate" );
         for ( int i = 0; i < busyIcons.length; i++ ) {
             busyIcons[i] = resourceMap.getIcon( "StatusBar.busyIcons[" + i + "]" );
         }
         busyIconTimer = new Timer( busyAnimationRate, new ActionListener() {
 
             public void actionPerformed( ActionEvent e ) {
                 busyIconIndex = ( busyIconIndex + 1 ) % busyIcons.length;
                 statusAnimationLabel.setIcon( busyIcons[busyIconIndex] );
             }
         } );
         idleIcon = resourceMap.getIcon( "StatusBar.idleIcon" );
         statusAnimationLabel.setIcon( idleIcon );
         progressBar.setVisible( false );
 
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor( getApplication().getContext() );
         taskMonitor.addPropertyChangeListener( new java.beans.PropertyChangeListener() {
 
             public void propertyChange( java.beans.PropertyChangeEvent evt ) {
                 String propertyName = evt.getPropertyName();
                 if ( "started".equals( propertyName ) ) {
                     if ( !busyIconTimer.isRunning() ) {
                         statusAnimationLabel.setIcon( busyIcons[0] );
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     progressBar.setVisible( true );
                     progressBar.setIndeterminate( true );
                 } else if ( "done".equals( propertyName ) ) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon( idleIcon );
                     progressBar.setVisible( false );
                     progressBar.setValue( 0 );
                 } else if ( "message".equals( propertyName ) ) {
                     String text = (String) ( evt.getNewValue() );
                     statusMessageLabel.setText( ( text == null ) ? "" : text );
                     messageTimer.restart();
                 } else if ( "progress".equals( propertyName ) ) {
                     int value = (Integer) ( evt.getNewValue() );
                     progressBar.setVisible( true );
                     progressBar.setIndeterminate( false );
                     progressBar.setValue( value );
                 }
             }
         } );
     }
 
     @Action
     public void showAboutBox() {
         if ( aboutBox == null ) {
             JFrame mainFrame = DatasetCreatorApp.getApplication().getMainFrame();
             aboutBox = new DatasetCreatorAboutBox( mainFrame );
             aboutBox.setLocationRelativeTo( mainFrame );
         }
         DatasetCreatorApp.getApplication().show( aboutBox );
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings( "unchecked" )
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         cboDrivers = new javax.swing.JComboBox();
         pnlConnInfo = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         txtHost = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         txtDbName = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         txtPort = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         txtUsername = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         txtPassword = new javax.swing.JTextField();
         btnGetTableList = new javax.swing.JButton();
         jLabel7 = new javax.swing.JLabel();
         txtConnString = new javax.swing.JTextField();
         btnConnString = new javax.swing.JButton();
         jLabel8 = new javax.swing.JLabel();
         txtSchemaName = new javax.swing.JTextField();
         pnlTableNames = new javax.swing.JPanel();
         scrlPnlTableNames = new javax.swing.JScrollPane();
         tblTableNames = new javax.swing.JTable();
         jPanel1 = new javax.swing.JPanel();
         pnlOptions = new javax.swing.JPanel();
         jLabel9 = new javax.swing.JLabel();
         chkGenDefaults = new javax.swing.JCheckBox();
         jLabel10 = new javax.swing.JLabel();
         txtNumRows = new javax.swing.JTextField();
         btnBuildDatasets = new javax.swing.JButton();
         rdoAll = new javax.swing.JRadioButton();
         rdoReq = new javax.swing.JRadioButton();
         rdoNone = new javax.swing.JRadioButton();
         txtFileLocation = new javax.swing.JTextField();
         jLabel11 = new javax.swing.JLabel();
         chkConstraints = new javax.swing.JCheckBox();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         mnuLAF = new javax.swing.JMenu();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
         btnGrpFieldOptions = new javax.swing.ButtonGroup();
         btnGrpLAF = new javax.swing.ButtonGroup();
 
         mainPanel.setName("mainPanel"); // NOI18N
         mainPanel.setPreferredSize(new java.awt.Dimension(570, 400));
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.voet.datasetcreator.DatasetCreatorApp.class).getContext().getResourceMap(DatasetCreatorView.class);
         jLabel1.setText(resourceMap.getString("lbl_drivers.text")); // NOI18N
         jLabel1.setName("lbl_drivers"); // NOI18N
 
         cboDrivers.setModel(DatasetCreatorApp.getDriverList());
         cboDrivers.setName("cboDrivers"); // NOI18N
         cboDrivers.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 driverSelectionChanged(evt);
             }
         });
 
         pnlConnInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnlConnInfo.setToolTipText(resourceMap.getString("pnlConnInfo.toolTipText")); // NOI18N
         pnlConnInfo.setName("pnlConnInfo"); // NOI18N
 
         jLabel2.setText(resourceMap.getString("lblHostName.text")); // NOI18N
         jLabel2.setName("lblHostName"); // NOI18N
 
         txtHost.setText(resourceMap.getString("txtHost.text")); // NOI18N
         txtHost.setName("txtHost"); // NOI18N
 
         jLabel3.setText(resourceMap.getString("lblDbName.text")); // NOI18N
         jLabel3.setName("lblDbName"); // NOI18N
 
         txtDbName.setText(resourceMap.getString("txtDbName.text")); // NOI18N
         txtDbName.setName("txtDbName"); // NOI18N
 
         jLabel4.setText(resourceMap.getString("lblPort.text")); // NOI18N
         jLabel4.setName("lblPort"); // NOI18N
 
         txtPort.setText(resourceMap.getString("txtPort.text")); // NOI18N
         txtPort.setName("txtPort"); // NOI18N
 
         jLabel5.setText(resourceMap.getString("lblUsername.text")); // NOI18N
         jLabel5.setName("lblUsername"); // NOI18N
 
         txtUsername.setText(resourceMap.getString("txtUsername.text")); // NOI18N
         txtUsername.setName("txtUsername"); // NOI18N
 
         jLabel6.setText(resourceMap.getString("lblUsername.text")); // NOI18N
         jLabel6.setName("lblUsername"); // NOI18N
 
         txtPassword.setText(resourceMap.getString("txtPassword.text")); // NOI18N
         txtPassword.setName("txtPassword"); // NOI18N
 
         btnGetTableList.setText(resourceMap.getString("btnListTableNames.text")); // NOI18N
         btnGetTableList.setName("btnListTableNames"); // NOI18N
         btnGetTableList.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnListTableNames(evt);
             }
         });
 
         jLabel7.setText(resourceMap.getString("lblConnString.text")); // NOI18N
         jLabel7.setName("lblConnString"); // NOI18N
 
         txtConnString.setText(resourceMap.getString("txtConnString.text")); // NOI18N
         txtConnString.setName("txtConnString"); // NOI18N
 
         btnConnString.setText(resourceMap.getString("btnConnString.text")); // NOI18N
         btnConnString.setName("btnConnString"); // NOI18N
         btnConnString.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 buildConnectionStringHandler(evt);
             }
         });
 
         jLabel8.setText(resourceMap.getString("lblSchemaName.text")); // NOI18N
         jLabel8.setName("lblSchemaName"); // NOI18N
 
         txtSchemaName.setText(resourceMap.getString("txtSchemaName.text")); // NOI18N
         txtSchemaName.setName("txtSchemaName"); // NOI18N
 
         javax.swing.GroupLayout pnlConnInfoLayout = new javax.swing.GroupLayout(pnlConnInfo);
         pnlConnInfo.setLayout(pnlConnInfoLayout);
         pnlConnInfoLayout.setHorizontalGroup(
             pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlConnInfoLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel6)
                     .addGroup(pnlConnInfoLayout.createSequentialGroup()
                         .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel3)
                             .addComponent(jLabel5)
                             .addComponent(jLabel7)
                             .addComponent(jLabel2))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(txtConnString, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                             .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                             .addComponent(txtDbName, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                             .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))))
                 .addGap(18, 18, 18)
                 .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel4)
                     .addComponent(jLabel8))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnGetTableList)
                     .addComponent(btnConnString)
                     .addComponent(txtSchemaName, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(51, 51, 51))
         );
         pnlConnInfoLayout.setVerticalGroup(
             pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlConnInfoLayout.createSequentialGroup()
                 .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel4))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(pnlConnInfoLayout.createSequentialGroup()
                         .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel3)
                             .addComponent(txtDbName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel8)
                             .addComponent(txtSchemaName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel5)
                             .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel6)
                             .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addComponent(btnConnString))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlConnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel7)
                     .addComponent(btnGetTableList)
                     .addComponent(txtConnString, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pnlTableNames.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnlTableNames.setName("pnlTableNames"); // NOI18N
         pnlTableNames.setPreferredSize(new java.awt.Dimension(315, 300));
 
         scrlPnlTableNames.setName("scrlPnlTableNames"); // NOI18N
 
         tblTableNames.setAutoCreateRowSorter(true);
         tblTableNames.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null},
                 {null, null},
                 {null, null},
                 {null, null}
             },
             new String [] {
                 "Title 1", "Title 2"
             }
         ));
         tblTableNames.setFillsViewportHeight(true);
         tblTableNames.setName("tblTableNames"); // NOI18N
         scrlPnlTableNames.setViewportView(tblTableNames);
 
         javax.swing.GroupLayout pnlTableNamesLayout = new javax.swing.GroupLayout(pnlTableNames);
         pnlTableNames.setLayout(pnlTableNamesLayout);
         pnlTableNamesLayout.setHorizontalGroup(
             pnlTableNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 254, Short.MAX_VALUE)
             .addGroup(pnlTableNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(pnlTableNamesLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(scrlPnlTableNames, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         pnlTableNamesLayout.setVerticalGroup(
             pnlTableNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 318, Short.MAX_VALUE)
             .addGroup(pnlTableNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(pnlTableNamesLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(scrlPnlTableNames, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                     .addContainerGap()))
         );
 
         jPanel1.setName("jPanel1"); // NOI18N
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 457, Short.MAX_VALUE)
         );
 
         pnlOptions.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnlOptions.setName("pnlOptions"); // NOI18N
 
         jLabel9.setText(resourceMap.getString("lblOptions.text")); // NOI18N
         jLabel9.setName("lblOptions"); // NOI18N
 
         chkGenDefaults.setText(resourceMap.getString("chkGenDefaults.text")); // NOI18N
         chkGenDefaults.setName("chkGenDefaults"); // NOI18N
         chkGenDefaults.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chkGenDefaultsHandler(evt);
             }
         });
 
         jLabel10.setText(resourceMap.getString("lblNumRows.text")); // NOI18N
         jLabel10.setName("lblNumRows"); // NOI18N
 
         txtNumRows.setText(resourceMap.getString("txtNumRows.text")); // NOI18N
         txtNumRows.setInputVerifier(new MyNumericInputVerifier());
         txtNumRows.setName("txtNumRows"); // NOI18N
 
         btnBuildDatasets.setText(resourceMap.getString("btnBuildDatasets.text")); // NOI18N
         btnBuildDatasets.setName("btnBuildDatasets"); // NOI18N
         btnBuildDatasets.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBuildDatasetsHandler(evt);
             }
         });
 
         btnGrpFieldOptions.add(rdoAll);
         rdoAll.setSelected(true);
         rdoAll.setText(resourceMap.getString("rdoAll.text")); // NOI18N
         rdoAll.setActionCommand(resourceMap.getString("rdoAll.actionCommand")); // NOI18N
         rdoAll.setName("rdoAll"); // NOI18N
 
         btnGrpFieldOptions.add(rdoReq);
         rdoReq.setText(resourceMap.getString("rdoReq.text")); // NOI18N
         rdoReq.setActionCommand(resourceMap.getString("rdoReq.actionCommand")); // NOI18N
         rdoReq.setName("rdoReq"); // NOI18N
 
         btnGrpFieldOptions.add(rdoNone);
         rdoNone.setText(resourceMap.getString("rdoNone.text")); // NOI18N
         rdoNone.setActionCommand(resourceMap.getString("rdoNone.actionCommand")); // NOI18N
         rdoNone.setName("rdoNone"); // NOI18N
 
         txtFileLocation.setText(resourceMap.getString("txtFileLocation.text")); // NOI18N
         txtFileLocation.setName("txtFileLocation"); // NOI18N
 
         jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
         jLabel11.setName("jLabel11"); // NOI18N
 
         chkConstraints.setText(resourceMap.getString("chkConstraints.text")); // NOI18N
         chkConstraints.setName("chkConstraints"); // NOI18N
 
         javax.swing.GroupLayout pnlOptionsLayout = new javax.swing.GroupLayout(pnlOptions);
         pnlOptions.setLayout(pnlOptionsLayout);
         pnlOptionsLayout.setHorizontalGroup(
             pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlOptionsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlOptionsLayout.createSequentialGroup()
                         .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel9)
                             .addComponent(chkGenDefaults))
                         .addContainerGap(299, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(pnlOptionsLayout.createSequentialGroup()
                         .addComponent(rdoAll)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(rdoReq)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(rdoNone)
                         .addContainerGap())
                     .addGroup(pnlOptionsLayout.createSequentialGroup()
                         .addComponent(btnBuildDatasets)
                         .addContainerGap(329, Short.MAX_VALUE))
                     .addGroup(pnlOptionsLayout.createSequentialGroup()
                         .addComponent(txtFileLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap(119, Short.MAX_VALUE))
                     .addGroup(pnlOptionsLayout.createSequentialGroup()
                         .addComponent(jLabel11)
                         .addContainerGap(369, Short.MAX_VALUE))
                     .addGroup(pnlOptionsLayout.createSequentialGroup()
                         .addComponent(jLabel10)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(txtNumRows, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                         .addGap(196, 196, 196))
                     .addGroup(pnlOptionsLayout.createSequentialGroup()
                         .addComponent(chkConstraints)
                         .addContainerGap(288, Short.MAX_VALUE))))
         );
         pnlOptionsLayout.setVerticalGroup(
             pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlOptionsLayout.createSequentialGroup()
                 .addGap(6, 6, 6)
                 .addComponent(jLabel9)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(rdoAll)
                     .addComponent(rdoNone)
                     .addComponent(rdoReq))
                 .addGap(18, 18, 18)
                 .addComponent(chkGenDefaults)
                 .addGap(6, 6, 6)
                 .addComponent(chkConstraints)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel10)
                     .addComponent(txtNumRows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(jLabel11)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(txtFileLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnBuildDatasets)
                 .addContainerGap(37, Short.MAX_VALUE))
         );
 
         rdoReq.getAccessibleContext().setAccessibleName(resourceMap.getString("jRadioButton2.AccessibleContext.accessibleName")); // NOI18N
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(mainPanelLayout.createSequentialGroup()
                         .addComponent(jLabel1)
                         .addGap(18, 18, 18)
                         .addComponent(cboDrivers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                         .addComponent(pnlTableNames, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pnlOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(mainPanelLayout.createSequentialGroup()
                         .addComponent(pnlConnInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(1, 1, 1)))
                 .addContainerGap())
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(cboDrivers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(1, 1, 1)
                 .addComponent(pnlConnInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(mainPanelLayout.createSequentialGroup()
                         .addComponent(pnlOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(135, 135, 135))
                     .addGroup(mainPanelLayout.createSequentialGroup()
                         .addComponent(pnlTableNames, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                         .addGap(135, 135, 135)))
                 .addGap(0, 0, 0))
         );
 
         menuBar.setName("menuBar"); // NOI18N
 
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.voet.datasetcreator.DatasetCreatorApp.class).getContext().getActionMap(DatasetCreatorView.class, this);
         exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
         exitMenuItem.setName("exitMenuItem"); // NOI18N
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
         helpMenu.setName("helpMenu"); // NOI18N
 
         aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
         aboutMenuItem.setName("aboutMenuItem"); // NOI18N
         helpMenu.add(aboutMenuItem);
 
         menuBar.add(helpMenu);
 
         mnuLAF.setText(resourceMap.getString("mnuSkin.text")); // NOI18N
         mnuLAF.setName("mnuSkin"); // NOI18N
         menuBar.add(mnuLAF);
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(statusMessageLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 564, Short.MAX_VALUE)
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(statusAnimationLabel)
                 .addContainerGap())
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(statusMessageLabel)
                     .addComponent(statusAnimationLabel)
                     .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(3, 3, 3))
         );
 
         setComponent(mainPanel);
         setMenuBar(menuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     private void driverSelectionChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_driverSelectionChanged
         Tuple<String, String> choice = (Tuple<String, String>) cboDrivers.getSelectedItem();
         if ( choice.getFirst() == null || choice.getFirst().trim().length() == 0 ) {
             pnlConnInfo.setVisible( false );
             pnlTableNames.setVisible( false );
         } else {
             pnlConnInfo.setVisible( true );
         }
 
     }//GEN-LAST:event_driverSelectionChanged
 
     private void btnListTableNames(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListTableNames
         Tuple<String, String> cboItem = (Tuple<String, String>) cboDrivers.getSelectedItem();
 
         String displayedConnString = txtConnString.getText();
         String generatedConnString = ConnectionStringUtil.getConnectionString( txtHost.getText(), txtPort.getText(), cboItem.getFirst(), txtDbName.getText(), txtSchemaName.getText(), txtUsername.getText(), txtPassword.getText() );
         String connectionString = null;
         if ( displayedConnString.trim().length() > 0 && !generatedConnString.equals( displayedConnString ) ) {
             connectionString = displayedConnString;
         } else {
             connectionString = generatedConnString;
         }
         SchemaMapper schema = MetaDataAccessor.getTableNames( cboItem.getFirst(), connectionString,
                 txtDbName.getText(), txtSchemaName.getText(), txtUsername.getText(), txtPassword.getText() );
 
         MyTableModel model = new MyTableModel();
         for ( TableMapper tbl : schema.getTables() ) {
             model.add( new Tuple( Boolean.FALSE, tbl.getName() ) );
 
         }
         tblTableNames.setModel( model );
 
         TableColumn selectedColumn = tblTableNames.getColumnModel().getColumn( 0 );
         JCheckBox chk = new JCheckBox();
         chk.setHorizontalAlignment( JLabel.CENTER );
         selectedColumn.setCellEditor( new DefaultCellEditor( chk ) );
         selectedColumn.setCellRenderer( new MyCheckBoxRenderer() );
         selectedColumn.setPreferredWidth( 15 );
         tblTableNames.doLayout();
         pnlTableNames.setVisible( true );
         pnlOptions.setVisible( true );
 
     }//GEN-LAST:event_btnListTableNames
 
     private void buildConnectionStringHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildConnectionStringHandler
         Tuple<String, String> cboItem = (Tuple<String, String>) cboDrivers.getSelectedItem();
         String connectionString = ConnectionStringUtil.getConnectionString( txtHost.getText(), txtPort.getText(), cboItem.getFirst(), txtDbName.getText(), txtSchemaName.getText(), txtUsername.getText(), txtPassword.getText() );
         txtConnString.setText( connectionString );
     }//GEN-LAST:event_buildConnectionStringHandler
 
     private void chkGenDefaultsHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGenDefaultsHandler
         if ( chkGenDefaults.isSelected() ) {
         } else {
         }
     }//GEN-LAST:event_chkGenDefaultsHandler
 
     private void btnBuildDatasetsHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuildDatasetsHandler
         SchemaMapper schema = new SchemaMapper( txtDbName.getText(), txtSchemaName.getText() );
         ButtonModel selection =  (ToggleButtonModel) btnGrpFieldOptions.getSelection();
 
 
         MyTableModel model = (MyTableModel) tblTableNames.getModel();
         List<Tuple<Boolean, String>> rows = model.getCheckedRows();
         for ( Tuple<Boolean, String> row : rows ) {
             schema.add( row.getSecond() );
         }
         Tuple<String, String> cboItem = (Tuple<String, String>) cboDrivers.getSelectedItem();
         String displayedConnString = txtConnString.getText();
         String generatedConnString = ConnectionStringUtil.getConnectionString( txtHost.getText(), txtPort.getText(), cboItem.getFirst(), txtDbName.getText(), txtSchemaName.getText(), txtUsername.getText(), txtPassword.getText() );
         String connectionString = null;
         if ( displayedConnString.trim().length() > 0 && !generatedConnString.equals( displayedConnString ) ) {
             connectionString = displayedConnString;
         } else {
             connectionString = generatedConnString;
         }
         schema = MetaDataAccessor.getColumnInfo( schema, cboItem.getFirst(), connectionString, schema.getDbName(), schema.getSchemaName(), txtUsername.getText(), txtPassword.getText() );
 
         DatasetWriter writer = new DatasetWriter( schema );
         String fileName = txtFileLocation.getText();
         File outFile = null;
        if ( fileName.indexOf( System.getProperty( "file.separator" ) ) > 0 ){
             outFile = new File( fileName );
         } else {
             outFile = new File( System.getProperty( "user.home" ), fileName );
         }
         boolean success = writer.writeDataset( outFile, selection.getActionCommand(), Integer.parseInt(txtNumRows.getText()), chkGenDefaults.isSelected(), chkConstraints.isSelected() );
         if ( success ) {
             JOptionPane.showMessageDialog( pnlConnInfo, "File written successfully", "Dataset Status", JOptionPane.INFORMATION_MESSAGE );
         } else {
             JOptionPane.showMessageDialog( pnlConnInfo, "Error writting file", "Dataset Status", JOptionPane.ERROR_MESSAGE );
         }
 
 
     }//GEN-LAST:event_btnBuildDatasetsHandler
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnBuildDatasets;
     private javax.swing.JButton btnConnString;
     private javax.swing.JButton btnGetTableList;
     private javax.swing.ButtonGroup btnGrpFieldOptions;
     private javax.swing.ButtonGroup btnGrpLAF;
     private javax.swing.JComboBox cboDrivers;
     private javax.swing.JCheckBox chkConstraints;
     private javax.swing.JCheckBox chkGenDefaults;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenu mnuLAF;
     private javax.swing.JPanel pnlConnInfo;
     private javax.swing.JPanel pnlOptions;
     private javax.swing.JPanel pnlTableNames;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JRadioButton rdoAll;
     private javax.swing.JRadioButton rdoNone;
     private javax.swing.JRadioButton rdoReq;
     private javax.swing.JScrollPane scrlPnlTableNames;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     private javax.swing.JTable tblTableNames;
     private javax.swing.JTextField txtConnString;
     private javax.swing.JTextField txtDbName;
     private javax.swing.JTextField txtFileLocation;
     private javax.swing.JTextField txtHost;
     private javax.swing.JTextField txtNumRows;
     private javax.swing.JTextField txtPassword;
     private javax.swing.JTextField txtPort;
     private javax.swing.JTextField txtSchemaName;
     private javax.swing.JTextField txtUsername;
     // End of variables declaration//GEN-END:variables
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
     private JDialog aboutBox;
 }
