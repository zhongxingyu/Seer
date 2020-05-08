 
 import com.sun.rowset.WebRowSetImpl;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import java.sql.SQLException;
 import javax.sql.rowset.WebRowSet;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import commons.Service;
 import data.DataStructBuilder;
 import gui.AresTableModel;
 import gui.LoginForm;
 import comunication.ServerServicesInterface;
 import comunication.RMIComms;
 import java.io.StringReader;
 import java.io.StringWriter;
 import javax.sql.rowset.spi.SyncProviderException;
 import javax.swing.JFrame;
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * testeGui.java
  *
  * Created on 06/06/2011, 16:12:27
  */
 /**
  *
  * @author Hugo
  */
 public class GUIStart extends javax.swing.JFrame {
 
     private ServerServicesInterface aresServices;
     private RMIComms comms;
     private final String serverIP = "127.0.0.1";
     //private final String serverIP = "192.168.25.2";
     private final String serverRemoteObjectName = "AresRemoteAPI";
     private final int serverPort = 8001;
     private int userID;
 
     public GUIStart() throws RemoteException {
 
         setupRMIComms();
 
         LoginForm loginForm;
         int mark = 0;
         do {
             loginForm = new LoginForm(this);
             loginForm.setVisible(true);
             if (loginForm.isCanceled()) {
                 System.out.println("Programa finalizado com sucesso! (by Hugo)\n");
                 System.exit(0);
             }
             userID = aresServices.login(loginForm.getUser(), loginForm.getPassword());
 
         } while (!aresServices.isConnected());
 
         DataStructBuilder structBuilder;
         try {
             structBuilder = new DataStructBuilder();
             this.treeModel = structBuilder.getObra(); // Faz referência ao objeto de modelagem da árvore de serviços.
             currentService = (Service) treeModel.getRoot();
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
             System.exit(1);
         } catch (SQLException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
             System.exit(1);
         } catch (IOException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
             System.exit(1);
         }
 
         nameMap = new HashMap<String, String>();
         nameMap.put("definido", "defined");
         nameMap.put("aprovado", "approved");
         nameMap.put("solicitado", "requested");
         nameMap.put("in loco", "inloco");
         nameMap.put("disponível", "available");
         nameMap.put("contratdo", "engaged");
 
         looks = javax.swing.UIManager.getInstalledLookAndFeels();
         lookNames = new String[looks.length];
         for (int i = 0; i < looks.length; i++) {
             lookNames[i] = looks[i].getName();
             if (lookNames[i].equalsIgnoreCase("nimbus")) {
                 mark = i;
             }
         }
 
         handler = new ItemHandler(); // Handler da apar?ncia e do comportamento
         initComponents();
         lookSubMenus[mark].setSelected(true);
         this.addWindowListener(exitListener);
         setComponentsEnabled(servicePanel, false);
         this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
     }
 
     private void setupRMIComms() {
         comms = new RMIComms();
 
         comms.setUpClient(serverIP, serverPort);
 
         try {
             aresServices = (ServerServicesInterface) comms.lookup(serverRemoteObjectName);
         } catch (RemoteException ex) {
 
             System.out.println("Erro ao obter objeto remoto.\n");
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
 
 
         }
     }
 
     private void changeTheLookAndFeel(int value)
             throws ClassNotFoundException, InstantiationException,
             IllegalAccessException, UnsupportedLookAndFeelException {
         changeTheLookAndFeel(looks[value].getClassName());
     }
 
     private void changeTheLookAndFeel(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
         javax.swing.UIManager.setLookAndFeel(className);
         System.out.println(className);
         javax.swing.SwingUtilities.updateComponentTreeUI(this);
     }
 
     private class ItemHandler implements ItemListener {
 
         @Override
         public void itemStateChanged(ItemEvent event) {
             System.out.println("Mudou o estado...");
             for (int count = 0; count < lookSubMenus.length; count++) {
                 if (lookSubMenus[count].isSelected()) {
                     try {
                         changeTheLookAndFeel(count); // muda apar?ncia e comportamento
                     } catch (ClassNotFoundException ex) {
                         Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (InstantiationException ex) {
                         Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (IllegalAccessException ex) {
                         Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (UnsupportedLookAndFeelException ex) {
                         Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } // fim do if
             } // fim do for
         } // fim do m?todo itemStateChanged
     } // fim da classe interna privada ItemHandler
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         jSplitPane1 = new javax.swing.JSplitPane();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         servicePanel = new javax.swing.JPanel();
         jPanel3 = new javax.swing.JPanel();
         subPanelProject = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         projectJTable = new javax.swing.JTable();
         jButton_addProject = new javax.swing.JButton();
         jButton_removeProject = new javax.swing.JButton();
         subPanelLogistics = new javax.swing.JPanel();
         jButton_addLogistic = new javax.swing.JButton();
         jScrollPane4 = new javax.swing.JScrollPane();
         logisticJTable = new javax.swing.JTable();
         jButton_removeLogistic = new javax.swing.JButton();
         subPanelMaterial = new javax.swing.JPanel();
         jButton_addMaterial = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         materialJTable = new javax.swing.JTable();
         jButton_removeMaterial = new javax.swing.JButton();
         subPanelLabour = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         workmanJTable = new javax.swing.JTable();
         jButton_addWorkman = new javax.swing.JButton();
         jButton_removeWorkman = new javax.swing.JButton();
         serviceDescriptionLabel = new javax.swing.JLabel();
         jPanel5 = new javax.swing.JPanel();
         jCheckBox1 = new javax.swing.JCheckBox();
         jLabel7 = new javax.swing.JLabel();
         jScrollPane5 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jPanel4 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jScrollPane6 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         startDateLabel = new javax.swing.JLabel();
         endDateLabel = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jCheckBox2 = new javax.swing.JCheckBox();
         jCheckBox3 = new javax.swing.JCheckBox();
         budgetLabel = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         reportPanel = new javax.swing.JPanel();
         tableComboBox = new javax.swing.JComboBox();
         statusComboBox = new javax.swing.JComboBox();
         jTextField2 = new javax.swing.JTextField();
         jLabel8 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jTextField3 = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         startDateField = new javax.swing.JFormattedTextField();
         jLabel5 = new javax.swing.JLabel();
         endDateField = new javax.swing.JFormattedTextField();
         generateReportButton = new javax.swing.JButton();
         clearSearchButton = new javax.swing.JButton();
         jScrollPane7 = new javax.swing.JScrollPane();
         reportTable = new javax.swing.JTable();
         saveReportButton = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         jPanel8 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         jRadioButton1 = new javax.swing.JRadioButton();
         jRadioButton2 = new javax.swing.JRadioButton();
         jRadioButton3 = new javax.swing.JRadioButton();
         jRadioButton4 = new javax.swing.JRadioButton();
         jPanel6 = new javax.swing.JPanel();
         reportDefined = new javax.swing.JCheckBox();
         reportApproved = new javax.swing.JCheckBox();
         reportRequested = new javax.swing.JCheckBox();
         reportInLoco = new javax.swing.JCheckBox();
         reportAvailable = new javax.swing.JCheckBox();
         reportEngaged = new javax.swing.JCheckBox();
         reportNotStarted = new javax.swing.JCheckBox();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         jPanel7 = new javax.swing.JPanel();
         jLabel12 = new javax.swing.JLabel();
         jTextField4 = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jTextField5 = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         jTextField6 = new javax.swing.JTextField();
         jLabel15 = new javax.swing.JLabel();
         jTextField7 = new javax.swing.JTextField();
         jScrollPane8 = new javax.swing.JScrollPane();
         jTable2 = new javax.swing.JTable();
         scrlTree = new javax.swing.JScrollPane();
         treeServicos = new javax.swing.JTree(treeModel);
         jMenuBar1 = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         jMenuItem2 = new javax.swing.JMenuItem();
         editMenu = new javax.swing.JMenu();
         lookMenu = new javax.swing.JMenu();
         helpMenu = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Ares");
 
         jTabbedPane1.setMaximumSize(new java.awt.Dimension(1024, 640));
         jTabbedPane1.setMinimumSize(new java.awt.Dimension(710, 555));
         jTabbedPane1.setPreferredSize(new java.awt.Dimension(710, 555));
 
         jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         jPanel3.setLayout(new java.awt.GridLayout(2, 2, 1, 1));
 
         subPanelProject.setBorder(javax.swing.BorderFactory.createTitledBorder("Projeto"));
         subPanelProject.setMaximumSize(new java.awt.Dimension(342, 32767));
         subPanelProject.setPreferredSize(new java.awt.Dimension(342, 85));
 
         projectJTable.getSelectionModel().addListSelectionListener(new RowListener());
         projectJTable.getColumnModel().getSelectionModel().addListSelectionListener(new ColumnListener());
         projectJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         //AresTableModel(String[] columnNames, Class[] types, DataBaseManager dbManager, int tableID)
         projectJTable.setModel(new AresTableModel(
             new String [] {
                 "Descrição", "Responsável", "Definido", "Aprovado"
             },
             new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class
             },
             aresServices, PROJECT, userID)
     );
     projectJTable.setCellSelectionEnabled(true);
     projectJTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
     projectJTable.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
             projectJTableMouseClicked(evt);
         }
     });
     jScrollPane1.setViewportView(projectJTable);
 
     jButton_addProject.setText("Adicionar Projeto");
     jButton_addProject.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_addProjectActionPerformed(evt);
         }
     });
 
     jButton_removeProject.setText("Remover Projeto(s)");
     jButton_removeProject.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_removeProjectActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout subPanelProjectLayout = new javax.swing.GroupLayout(subPanelProject);
     subPanelProject.setLayout(subPanelProjectLayout);
     subPanelProjectLayout.setHorizontalGroup(
         subPanelProjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(subPanelProjectLayout.createSequentialGroup()
             .addComponent(jButton_addProject)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jButton_removeProject)
             .addContainerGap())
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
     );
     subPanelProjectLayout.setVerticalGroup(
         subPanelProjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, subPanelProjectLayout.createSequentialGroup()
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(subPanelProjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jButton_addProject)
                 .addComponent(jButton_removeProject)))
     );
 
     jPanel3.add(subPanelProject);
 
     subPanelLogistics.setBorder(javax.swing.BorderFactory.createTitledBorder("Logística"));
 
     jButton_addLogistic.setText("Adicionar Logistica");
     jButton_addLogistic.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_addLogisticActionPerformed(evt);
         }
     });
 
     logisticJTable.setModel(new AresTableModel(
         new String [] {
             "Descrição", "Responsável", "Definido", "Aprovado"
         },
         new Class [] {
             java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class
         },
         aresServices, LOGISTIC, userID)
     );
     logisticJTable.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
             logisticJTableMouseClicked(evt);
         }
     });
     jScrollPane4.setViewportView(logisticJTable);
 
     jButton_removeLogistic.setText("Remover Logística(s)");
     jButton_removeLogistic.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_removeLogisticActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout subPanelLogisticsLayout = new javax.swing.GroupLayout(subPanelLogistics);
     subPanelLogistics.setLayout(subPanelLogisticsLayout);
     subPanelLogisticsLayout.setHorizontalGroup(
         subPanelLogisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(subPanelLogisticsLayout.createSequentialGroup()
             .addComponent(jButton_addLogistic)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jButton_removeLogistic)
             .addContainerGap())
         .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
     );
     subPanelLogisticsLayout.setVerticalGroup(
         subPanelLogisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, subPanelLogisticsLayout.createSequentialGroup()
             .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(subPanelLogisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jButton_addLogistic)
                 .addComponent(jButton_removeLogistic)))
     );
 
     jPanel3.add(subPanelLogistics);
 
     subPanelMaterial.setBorder(javax.swing.BorderFactory.createTitledBorder("Material"));
     subPanelMaterial.setMaximumSize(new java.awt.Dimension(366, 32767));
 
     jButton_addMaterial.setText("Adicionar Material");
     jButton_addMaterial.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_addMaterialActionPerformed(evt);
         }
     });
 
     materialJTable.getSelectionModel().addListSelectionListener(new RowListener());
     materialJTable.getColumnModel().getSelectionModel().addListSelectionListener(new ColumnListener());
     materialJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
     materialJTable.setModel(new AresTableModel(
         new String [] {
             "Descrição", "Responsável", "Solicitado", "in Loco", "Disponível"
         },
         new Class [] {
             java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
         },
         aresServices, MATERIAL, userID)
     );
     materialJTable.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
             materialJTableMouseClicked(evt);
         }
     });
     jScrollPane2.setViewportView(materialJTable);
 
     jButton_removeMaterial.setText("Remover Material");
     jButton_removeMaterial.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_removeMaterialActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout subPanelMaterialLayout = new javax.swing.GroupLayout(subPanelMaterial);
     subPanelMaterial.setLayout(subPanelMaterialLayout);
     subPanelMaterialLayout.setHorizontalGroup(
         subPanelMaterialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(subPanelMaterialLayout.createSequentialGroup()
             .addComponent(jButton_addMaterial)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jButton_removeMaterial)
             .addContainerGap())
         .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
     );
     subPanelMaterialLayout.setVerticalGroup(
         subPanelMaterialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, subPanelMaterialLayout.createSequentialGroup()
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(subPanelMaterialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jButton_removeMaterial)
                 .addComponent(jButton_addMaterial)))
     );
 
     jPanel3.add(subPanelMaterial);
 
     subPanelLabour.setBorder(javax.swing.BorderFactory.createTitledBorder("Mão de Obra"));
     subPanelLabour.setMaximumSize(new java.awt.Dimension(342, 32767));
 
     workmanJTable.setModel(new AresTableModel(
         new String [] {
             "Descrição", "Responsável", "Disponível", "Contratada"
         },
         new Class [] {
             java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class
         },
         aresServices, WORKMAN, userID)
     );
     workmanJTable.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
             workmanJTableMouseClicked(evt);
         }
     });
     jScrollPane3.setViewportView(workmanJTable);
 
     jButton_addWorkman.setText("Adicionar Equipe");
     jButton_addWorkman.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_addWorkmanActionPerformed(evt);
         }
     });
 
     jButton_removeWorkman.setText("Remover Equipe(s)");
     jButton_removeWorkman.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton_removeWorkmanActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout subPanelLabourLayout = new javax.swing.GroupLayout(subPanelLabour);
     subPanelLabour.setLayout(subPanelLabourLayout);
     subPanelLabourLayout.setHorizontalGroup(
         subPanelLabourLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(subPanelLabourLayout.createSequentialGroup()
             .addComponent(jButton_addWorkman)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jButton_removeWorkman)
             .addContainerGap())
         .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
     );
     subPanelLabourLayout.setVerticalGroup(
         subPanelLabourLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, subPanelLabourLayout.createSequentialGroup()
             .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(subPanelLabourLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jButton_addWorkman)
                 .addComponent(jButton_removeWorkman)))
     );
 
     jPanel3.add(subPanelLabour);
 
     serviceDescriptionLabel.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
     serviceDescriptionLabel.setText("OBRA");
 
     jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
     jCheckBox1.setText("Iniciado");
     jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jCheckBox1ActionPerformed(evt);
         }
     });
 
     jLabel7.setText("Observações");
 
     jTextArea1.setColumns(20);
     jTextArea1.setLineWrap(true);
     jTextArea1.setRows(5);
     jTextArea1.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
             jTextArea1FocusGained(evt);
         }
     });
     jScrollPane5.setViewportView(jTextArea1);
 
     jPanel4.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createEtchedBorder()));
     jPanel4.setMaximumSize(new java.awt.Dimension(361, 126));
     jPanel4.setMinimumSize(new java.awt.Dimension(361, 126));
 
     jLabel4.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
     jLabel4.setText("Previsão de Inicio:");
 
     jLabel6.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
     jLabel6.setText("Previsão de Término:");
 
     jTable1.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
             {null},
             {null},
             {null}
         },
         new String [] {
             "Predecessores"
         }
     ) {
         Class[] types = new Class [] {
             java.lang.String.class
         };
 
         public Class getColumnClass(int columnIndex) {
             return types [columnIndex];
         }
     });
     jScrollPane6.setViewportView(jTable1);
 
     jLabel1.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
     jLabel1.setText("Iniado em ");
 
     jLabel2.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
     jLabel2.setText("% do Serviço Concluído");
 
     startDateLabel.setText("jLabel3");
 
     endDateLabel.setText("jLabel5");
 
     jLabel9.setText("jLabel9");
 
     jLabel11.setText("jLabel11");
 
     javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
     jPanel4.setLayout(jPanel4Layout);
     jPanel4Layout.setHorizontalGroup(
         jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel4Layout.createSequentialGroup()
             .addContainerGap()
             .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel4Layout.createSequentialGroup()
                     .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                         .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(endDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(startDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap())
     );
     jPanel4Layout.setVerticalGroup(
         jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel4Layout.createSequentialGroup()
             .addContainerGap()
             .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                 .addGroup(jPanel4Layout.createSequentialGroup()
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel4)
                         .addComponent(startDateLabel))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel6)
                         .addComponent(endDateLabel))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel1)
                         .addComponent(jLabel9))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel2)
                         .addComponent(jLabel11))))
             .addContainerGap())
     );
 
     jCheckBox2.setText("Paralisado");
 
     jCheckBox3.setText("Concluído");
 
     budgetLabel.setText("Orçamento:");
 
     jTextField1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jTextField1ActionPerformed(evt);
         }
     });
     jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
             jTextField1FocusGained(evt);
         }
     });
 
     javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
     jPanel5.setLayout(jPanel5Layout);
     jPanel5Layout.setHorizontalGroup(
         jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel5Layout.createSequentialGroup()
             .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                 .addGroup(jPanel5Layout.createSequentialGroup()
                     .addComponent(jCheckBox1)
                     .addGap(18, 18, 18)
                     .addComponent(jCheckBox2)
                     .addGap(18, 18, 18)
                     .addComponent(jCheckBox3)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(budgetLabel))
                 .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                 .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addContainerGap())
     );
     jPanel5Layout.setVerticalGroup(
         jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
             .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addGroup(jPanel5Layout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jLabel7)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jPanel5Layout.createSequentialGroup()
                     .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jCheckBox1)
                         .addComponent(jCheckBox2)
                         .addComponent(jCheckBox3)
                         .addComponent(budgetLabel))))
             .addContainerGap())
     );
 
     javax.swing.GroupLayout servicePanelLayout = new javax.swing.GroupLayout(servicePanel);
     servicePanel.setLayout(servicePanelLayout);
     servicePanelLayout.setHorizontalGroup(
         servicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, servicePanelLayout.createSequentialGroup()
             .addContainerGap()
             .addGroup(servicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 762, Short.MAX_VALUE)
                 .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(serviceDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE))
             .addContainerGap())
     );
     servicePanelLayout.setVerticalGroup(
         servicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(servicePanelLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(serviceDescriptionLabel)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
             .addContainerGap())
     );
 
     jTabbedPane1.addTab("Serviço", servicePanel);
 
     reportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pendências"));
 
     tableComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tabela", "Logística", "Material", "Mão de Obra", "Projetos", "Serviços" }));
     tableComboBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             tableComboBoxActionPerformed(evt);
         }
     });
 
     statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Status", "Não Solicitado", "Solicitado", "Não definido", "Definido", "Aprovado", "In Loco", "Não Disponível", "Disponível", "Contratado" }));
     statusComboBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             statusComboBoxActionPerformed(evt);
         }
     });
 
     jLabel8.setText("Nome:");
 
     jLabel10.setText("Responsável:");
 
     jLabel3.setText("Com início em:");
 
     startDateField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT))));
 
     jLabel5.setText("Com término em:");
 
     endDateField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT))));
 
     generateReportButton.setText("Gerar Relatório");
     generateReportButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             generateReportButtonActionPerformed(evt);
         }
     });
 
     clearSearchButton.setText("Limpar Busca");
     clearSearchButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             clearSearchButtonActionPerformed(evt);
         }
     });
 
     reportTable.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
 
         },
         new String [] {
             "Title 1", "Title 2", "Title 3", "Title 4"
         }
     ));
     jScrollPane7.setViewportView(reportTable);
 
     saveReportButton.setText("Salvar Relatório");
 
     javax.swing.GroupLayout reportPanelLayout = new javax.swing.GroupLayout(reportPanel);
     reportPanel.setLayout(reportPanelLayout);
     reportPanelLayout.setHorizontalGroup(
         reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reportPanelLayout.createSequentialGroup()
             .addContainerGap()
             .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
                 .addGroup(reportPanelLayout.createSequentialGroup()
                     .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(statusComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(tableComboBox, 0, 114, Short.MAX_VALUE))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(reportPanelLayout.createSequentialGroup()
                             .addComponent(jLabel8)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                         .addGroup(reportPanelLayout.createSequentialGroup()
                             .addComponent(jLabel10)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                         .addGroup(reportPanelLayout.createSequentialGroup()
                             .addComponent(jLabel3)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(startDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(jLabel5)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(endDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGroup(reportPanelLayout.createSequentialGroup()
                             .addComponent(clearSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(generateReportButton)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(saveReportButton)))))
             .addContainerGap())
     );
     reportPanelLayout.setVerticalGroup(
         reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(reportPanelLayout.createSequentialGroup()
             .addContainerGap()
             .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(reportPanelLayout.createSequentialGroup()
                     .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel8))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel10)))
                 .addGroup(reportPanelLayout.createSequentialGroup()
                     .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(tableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(endDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel5)
                         .addComponent(startDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel3))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(clearSearchButton)
                         .addComponent(saveReportButton)
                         .addComponent(generateReportButton))))
             .addGap(18, 18, 18)
             .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
             .addContainerGap())
     );
 
     jTabbedPane1.addTab("Relatórios", reportPanel);
 
     jPanel1.setMinimumSize(new java.awt.Dimension(780, 500));
 
     jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
     jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Tabela"));
 
     buttonGroup1.add(jRadioButton1);
     jRadioButton1.setText("Logística");
     jRadioButton1.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
             jRadioButton1StateChanged(evt);
         }
     });
     jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jRadioButton1ActionPerformed(evt);
         }
     });
 
     buttonGroup1.add(jRadioButton2);
     jRadioButton2.setText("Mão-de-obra");
     jRadioButton2.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
             jRadioButton2StateChanged(evt);
         }
     });
 
     buttonGroup1.add(jRadioButton3);
     jRadioButton3.setText("Material");
     jRadioButton3.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
             jRadioButton3StateChanged(evt);
         }
     });
 
     buttonGroup1.add(jRadioButton4);
     jRadioButton4.setText("Projeto");
     jRadioButton4.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
             jRadioButton4StateChanged(evt);
         }
     });
 
     javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
     jPanel2.setLayout(jPanel2Layout);
     jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jRadioButton1)
                 .addComponent(jRadioButton2)
                 .addComponent(jRadioButton3)
                 .addComponent(jRadioButton4))
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
     jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
             .addComponent(jRadioButton1)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jRadioButton2)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jRadioButton3)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jRadioButton4)
             .addContainerGap(12, Short.MAX_VALUE))
     );
 
     jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Status"));
 
     reportDefined.setText("Definido");
 
     reportApproved.setText("Aprovado");
 
     reportRequested.setText("Solicitado");
 
     reportInLoco.setText("in Loco");
 
     reportAvailable.setText("Disponível");
 
     reportEngaged.setText("Contratado");
 
     reportNotStarted.setText("Não iniciado");
 
     javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
     jPanel6.setLayout(jPanel6Layout);
     jPanel6Layout.setHorizontalGroup(
         jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel6Layout.createSequentialGroup()
             .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(reportNotStarted)
                 .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                     .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(reportAvailable)
                         .addComponent(reportInLoco)
                         .addComponent(reportRequested))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                     .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(reportDefined)
                         .addComponent(reportApproved)
                         .addComponent(reportEngaged))))
             .addContainerGap())
     );
     jPanel6Layout.setVerticalGroup(
         jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
             .addComponent(reportNotStarted)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
             .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(reportRequested)
                 .addComponent(reportDefined))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(reportInLoco)
                 .addComponent(reportApproved))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(reportAvailable)
                 .addComponent(reportEngaged)))
     );
 
     jButton1.setText("Salvar");
 
     jButton2.setText("Gerar");
 
     jButton3.setText("Limpar");
 
     jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtros"));
 
     jLabel12.setText("Nome:");
 
     jLabel13.setText("Responsável:");
 
     jLabel14.setText("Início:");
 
     jLabel15.setText("Término:");
 
     javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
     jPanel7.setLayout(jPanel7Layout);
     jPanel7Layout.setHorizontalGroup(
         jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel7Layout.createSequentialGroup()
             .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel7Layout.createSequentialGroup()
                     .addComponent(jLabel13)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jTextField5))
                 .addGroup(jPanel7Layout.createSequentialGroup()
                     .addComponent(jLabel12)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jTextField4))
                 .addGroup(jPanel7Layout.createSequentialGroup()
                     .addComponent(jLabel14)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGap(18, 18, 18)
                     .addComponent(jLabel15)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
             .addContainerGap())
     );
     jPanel7Layout.setVerticalGroup(
         jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel7Layout.createSequentialGroup()
             .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel12)
                 .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel13)
                 .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel14)
                 .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel15)
                 .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
     jPanel8.setLayout(jPanel8Layout);
     jPanel8Layout.setHorizontalGroup(
         jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel8Layout.createSequentialGroup()
             .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                 .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING))
             .addContainerGap())
     );
     jPanel8Layout.setVerticalGroup(
         jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
             .addGap(26, 26, 26)
             .addComponent(jButton2)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jButton1)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
             .addComponent(jButton3)
             .addGap(20, 20, 20))
         .addComponent(jPanel7, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
     );
 
     jTable2.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
 
         },
         new String [] {
             "Title 1", "Title 2", "Title 3", "Title 4"
         }
     ));
     jScrollPane8.setViewportView(jTable2);
 
     javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
     jPanel1.setLayout(jPanel1Layout);
     jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
             .addContainerGap()
             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addContainerGap())
     );
     jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap(34, Short.MAX_VALUE))
     );
 
     jTabbedPane1.addTab("Relatórios(2)", jPanel1);
 
     jSplitPane1.setRightComponent(jTabbedPane1);
 
     scrlTree.setAutoscrolls(true);
     scrlTree.setMaximumSize(new java.awt.Dimension(0, 0));
     scrlTree.setMinimumSize(new java.awt.Dimension(120, 200));
 
     treeServicos.setShowsRootHandles(true);
     treeServicos.setFocusCycleRoot(true);
     treeServicos.setMaximumSize(new java.awt.Dimension(700, 800));
     treeServicos.setMinimumSize(new java.awt.Dimension(200, 200));
     treeServicos.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
         public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
             treeServicosValueChanged(evt);
         }
     });
     scrlTree.setViewportView(treeServicos);
 
     jSplitPane1.setLeftComponent(scrlTree);
 
     fileMenu.setText("Arquivo");
 
     jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
     jMenuItem2.setText("Salvar");
     fileMenu.add(jMenuItem2);
 
     jMenuBar1.add(fileMenu);
 
     editMenu.setText("Editar");
 
     lookMenu.setText("Aparencia");
     group = new javax.swing.ButtonGroup(); // grupo de botões para aparência e comportamento
 
     lookSubMenus = new javax.swing.JRadioButtonMenuItem[lookNames.length];
     for(int count=0; count < lookSubMenus.length; count++)
     {
         lookSubMenus[count] = new javax.swing.JRadioButtonMenuItem(lookNames[count]);
         lookSubMenus[count].addItemListener(handler); // adiciona handler
         group.add(lookSubMenus[count]); // adiciona botões de opão ao grupo
         lookMenu.add(lookSubMenus[count]);
     }
     lookSubMenus[0].setSelected(true);
     editMenu.add(lookMenu);
 
     jMenuBar1.add(editMenu);
 
     helpMenu.setText("Ajuda");
 
     jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
     jMenuItem1.setText("Sobre");
     helpMenu.add(jMenuItem1);
 
     jMenuBar1.add(helpMenu);
 
     setJMenuBar(jMenuBar1);
 
     javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
     getContentPane().setLayout(layout);
     layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 923, Short.MAX_VALUE)
     );
     layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE)
     );
 
     pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void treeServicosValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeServicosValueChanged
         if (currentService != null && currentService.isLeaf()) {
             try {
                 aresServices.acceptChanges(userID);
                 aresServices.updateService(userID, jTextArea1.getText(), jTextField1.getText());
                 aresServices.acceptChanges(userID);
             } catch (SyncProviderException ex) {
                 if(ex.getMessage().contains("SPE:")){
                     javax.swing.JOptionPane.showMessageDialog(null, "Erro ao salvar as informações,"
                             + " alguém as modificou antes de você.", "Erro!", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
             } catch (RemoteException ex) {
                 Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         currentService = (Service) treeServicos.getLastSelectedPathComponent();
         System.out.printf("Noh selecionado: %s\n", currentService);
 
         serviceDescriptionLabel.setText(currentService.getDescricao());
         startDateLabel.setText(currentService.dataInicio.toString());
         endDateLabel.setText(currentService.dataTermino.toString());
 
         if (currentService == null) {
             return;
         } else {
             serviceDescriptionLabel.setText(currentService.getDescricao());
             if (currentService.isLeaf()) {
 
                 setComponentsEnabled(servicePanel, true);
                 try {
                     loadServiceData(currentService);
 
 
                     AresTableModel model;
                     model = (AresTableModel) projectJTable.getModel();
                     model.executeQuery(userID, currentService.ID);
                     model = (AresTableModel) materialJTable.getModel();
                     model.executeQuery(userID, currentService.ID);
                     model = (AresTableModel) logisticJTable.getModel();
                     model.executeQuery(userID, currentService.ID);
                     model = (AresTableModel) workmanJTable.getModel();
                     model.executeQuery(userID, currentService.ID);
 
                     jTextArea1.setText(currentService.comments);
                     jTextField1.setText(currentService.budget);
                 } catch (SQLException ex) {
                     Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (RemoteException ex) {
                     Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                 }
             } else { // é apenas um serviço.
                 jTextArea1.setText("");
                 jTextField1.setText("");
                 clearTables();
                 setComponentsEnabled(servicePanel, false);
             }
         }
         System.out.println("-----------------------------");
 
     }//GEN-LAST:event_treeServicosValueChanged
 
     private void jButton_addWorkmanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addWorkmanActionPerformed
         clearSelection(WORKMAN);
         AresTableModel model = (AresTableModel) workmanJTable.getModel();
         model.addRow(userID, currentService.ID);
     }//GEN-LAST:event_jButton_addWorkmanActionPerformed
 
     private void jButton_addProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addProjectActionPerformed
         clearSelection(PROJECT);
         AresTableModel model = (AresTableModel) projectJTable.getModel();
         model.addRow(userID, currentService.ID);
     }//GEN-LAST:event_jButton_addProjectActionPerformed
 
     private void workmanJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workmanJTableMouseClicked
         jButton_removeWorkman.setEnabled(true);
         clearSelection(WORKMAN);
     }//GEN-LAST:event_workmanJTableMouseClicked
 
     private void materialJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_materialJTableMouseClicked
         jButton_removeMaterial.setEnabled(true);
         clearSelection(MATERIAL);
     }//GEN-LAST:event_materialJTableMouseClicked
 
     private void logisticJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logisticJTableMouseClicked
         jButton_removeLogistic.setEnabled(true);
         clearSelection(LOGISTIC);
     }//GEN-LAST:event_logisticJTableMouseClicked
 
     private void projectJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_projectJTableMouseClicked
         jButton_removeProject.setEnabled(true);
         clearSelection(PROJECT);
     }//GEN-LAST:event_projectJTableMouseClicked
 
     private void jButton_addMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addMaterialActionPerformed
         clearSelection(MATERIAL);
         AresTableModel model = (AresTableModel) materialJTable.getModel();
         model.addRow(userID, currentService.ID);
     }//GEN-LAST:event_jButton_addMaterialActionPerformed
 
     private void jButton_addLogisticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addLogisticActionPerformed
         clearSelection(LOGISTIC);
         AresTableModel model = (AresTableModel) logisticJTable.getModel();
         model.addRow(userID, currentService.ID);
     }//GEN-LAST:event_jButton_addLogisticActionPerformed
 
     private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
     }//GEN-LAST:event_jTextField1ActionPerformed
 
     private void jButton_removeProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_removeProjectActionPerformed
         int row = projectJTable.getSelectedRow();
         AresTableModel model = (AresTableModel) projectJTable.getModel();
         try {
             model.removeRow(userID, row);
         } catch (SQLException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RemoteException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_jButton_removeProjectActionPerformed
 
     private void jButton_removeLogisticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_removeLogisticActionPerformed
         int row = logisticJTable.getSelectedRow();
         AresTableModel model = (AresTableModel) logisticJTable.getModel();
         try {
             model.removeRow(userID, row);
         } catch (SQLException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RemoteException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_jButton_removeLogisticActionPerformed
 
     private void jButton_removeMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_removeMaterialActionPerformed
         int row = materialJTable.getSelectedRow();
         AresTableModel model = (AresTableModel) materialJTable.getModel();
         try {
             model.removeRow(userID, row);
         } catch (SQLException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RemoteException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_jButton_removeMaterialActionPerformed
 
     private void jButton_removeWorkmanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_removeWorkmanActionPerformed
         int row = workmanJTable.getSelectedRow();
         AresTableModel model = (AresTableModel) workmanJTable.getModel();
         try {
             model.removeRow(userID, row);
         } catch (SQLException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RemoteException ex) {
             Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_jButton_removeWorkmanActionPerformed
 
     private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained
         clearSelection(ALL);
     }//GEN-LAST:event_jTextField1FocusGained
 
     private void jTextArea1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea1FocusGained
         clearSelection(ALL);
     }//GEN-LAST:event_jTextArea1FocusGained
 
     private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jCheckBox1ActionPerformed
 
     private void tableComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tableComboBoxActionPerformed
 
     private void clearSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSearchButtonActionPerformed
         endDateField.setText("--/--/--");
         startDateField.setText("--/--/--");
         jTextField2.setText(null);
         jTextField3.setText(null);
         tableComboBox.setSelectedIndex(0);
         statusComboBox.setSelectedIndex(0);
         //statusComboBox.setEditable(false);
     }//GEN-LAST:event_clearSearchButtonActionPerformed
 
     private void statusComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_statusComboBoxActionPerformed
 
     private void generateReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReportButtonActionPerformed
     }//GEN-LAST:event_generateReportButtonActionPerformed
 
     private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jRadioButton1ActionPerformed
 
     private void jRadioButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton1StateChanged
         if (jRadioButton1.isSelected()) {
             reportApproved.setEnabled(true);
             reportAvailable.setEnabled(false);
             reportDefined.setEnabled(true);
             reportEngaged.setEnabled(false);
             reportInLoco.setEnabled(false);
             reportNotStarted.setEnabled(true);
             reportRequested.setEnabled(false);
         }
     }//GEN-LAST:event_jRadioButton1StateChanged
 
     private void jRadioButton2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton2StateChanged
         if (jRadioButton2.isSelected()) {
             reportApproved.setEnabled(false);
             reportAvailable.setEnabled(true);
             reportDefined.setEnabled(false);
             reportEngaged.setEnabled(true);
             reportInLoco.setEnabled(false);
             reportNotStarted.setEnabled(true);
             reportRequested.setEnabled(false);
         }
     }//GEN-LAST:event_jRadioButton2StateChanged
 
     private void jRadioButton3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton3StateChanged
         if (jRadioButton3.isSelected()) {
             reportApproved.setEnabled(false);
             reportAvailable.setEnabled(true);
             reportDefined.setEnabled(false);
             reportEngaged.setEnabled(false);
             reportInLoco.setEnabled(true);
             reportNotStarted.setEnabled(true);
             reportRequested.setEnabled(true);
         }
     }//GEN-LAST:event_jRadioButton3StateChanged
 
     private void jRadioButton4StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton4StateChanged
         if (jRadioButton4.isSelected()) {
             reportApproved.setEnabled(true);
             reportAvailable.setEnabled(false);
             reportDefined.setEnabled(true);
             reportEngaged.setEnabled(false);
             reportInLoco.setEnabled(false);
             reportNotStarted.setEnabled(true);
             reportRequested.setEnabled(false);
         }
     }//GEN-LAST:event_jRadioButton4StateChanged
 
     private void clearSelection(int tableId) {
         if (tableId != PROJECT) {
             projectJTable.clearSelection();
             jButton_removeProject.setEnabled(false);
         }
         if (tableId != MATERIAL) {
             materialJTable.clearSelection();
             jButton_removeMaterial.setEnabled(false);
         }
         if (tableId != WORKMAN) {
             workmanJTable.clearSelection();
             jButton_removeWorkman.setEnabled(false);
         }
         if (tableId != LOGISTIC) {
             logisticJTable.clearSelection();
             jButton_removeLogistic.setEnabled(false);
         }
     }
 
     private void setComponentsEnabled(Container component, boolean enabled) {
         Component[] com = component.getComponents();
         for (int a = 0; a < com.length; a++) {
             com[a].setEnabled(enabled);
             if (com[a] instanceof Container) {
                 setComponentsEnabled((Container) com[a], enabled);
             }
         }
     }
 
     private void loadServiceData(Service service) throws SQLException, RemoteException {
         String result = aresServices.executeQuery(userID, service.ID, SERVICE);
 
         StringReader sr = new StringReader(result);
 
         WebRowSet rowSet = new WebRowSetImpl();
         rowSet.readXml(sr);
 
         if (rowSet.next()) {
             currentService.budget = rowSet.getString("budget");
             currentService.comments = rowSet.getString("comments");
         }
     }
 
     private void clearTables() {
         AresTableModel amodel;
 
         amodel = (AresTableModel) projectJTable.getModel();
         amodel.setRowCount(0);
         amodel = (AresTableModel) logisticJTable.getModel();
         amodel.setRowCount(0);
         amodel = (AresTableModel) materialJTable.getModel();
         amodel.setRowCount(0);
         amodel = (AresTableModel) workmanJTable.getModel();
         amodel.setRowCount(0);
     }
 
     private class RowListener implements ListSelectionListener {
 
         @Override
         public void valueChanged(ListSelectionEvent lse) {
             if (lse.getValueIsAdjusting()) {
                 return;
             }
             //tableRow = projectJTable.getSelectionModel().getLeadSelectionIndex();
         }
     }
 
     private class ColumnListener implements ListSelectionListener {
 
         @Override
         public void valueChanged(ListSelectionEvent event) {
             if (event.getValueIsAdjusting()) {
                 return;
             }
             //tableCol = projectJTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
         }
     }
 
     /**
      * @param args the command line arguments
      * @throws SQLException
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public static void main(String args[]) throws SQLException, ClassNotFoundException, IOException {
         GUIStart tela = new GUIStart();
         tela.setVisible(true);
     }
     private final int MATERIAL = 0;
     private final int LOGISTIC = 1;
     private final int PROJECT = 2;
     private final int WORKMAN = 3;
     private final int SERVICE = 4;
     private final int ALL = 100;
     private String lookNames[];
     private Map< String, String> nameMap;
     private ItemHandler handler;
     private Service currentService;
     private javax.swing.tree.TreeModel treeModel;
     private javax.swing.ButtonGroup group; // grupo para botões de opção
     private javax.swing.JRadioButtonMenuItem lookSubMenus[]; // submenus para selecionar apar?ncias
     private javax.swing.UIManager.LookAndFeelInfo looks[]; // aparências e comportamentos
     private WindowListener exitListener = new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
             int confirm = javax.swing.JOptionPane.showOptionDialog(null,
                     "Você tem certeza que deseja fechar o Ares?", "Tá cedo, sem pressa!!!",
                     javax.swing.JOptionPane.YES_NO_OPTION,
                     javax.swing.JOptionPane.QUESTION_MESSAGE,
                     null, null, null);
             if (confirm == 0) {
                 if (currentService.isLeaf()) {
                     try {
                         aresServices.updateService(userID, jTextArea1.getText(), jTextField1.getText());
                         aresServices.acceptChanges(userID);
                         aresServices.logout(userID);
                     }catch (SQLException ex) {
                         Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (RemoteException ex) {
                         Logger.getLogger(GUIStart.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                 }
                 System.out.println("Programa finalizado com sucesso! (by Hugo)\n");
                 System.exit(0);
             }
         }
     };
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel budgetLabel;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JButton clearSearchButton;
     private javax.swing.JMenu editMenu;
     private javax.swing.JFormattedTextField endDateField;
     private javax.swing.JLabel endDateLabel;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JButton generateReportButton;
     private javax.swing.JMenu helpMenu;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton_addLogistic;
     private javax.swing.JButton jButton_addMaterial;
     private javax.swing.JButton jButton_addProject;
     private javax.swing.JButton jButton_addWorkman;
     private javax.swing.JButton jButton_removeLogistic;
     private javax.swing.JButton jButton_removeMaterial;
     private javax.swing.JButton jButton_removeProject;
     private javax.swing.JButton jButton_removeWorkman;
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JCheckBox jCheckBox2;
     private javax.swing.JCheckBox jCheckBox3;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JRadioButton jRadioButton1;
     private javax.swing.JRadioButton jRadioButton2;
     private javax.swing.JRadioButton jRadioButton3;
     private javax.swing.JRadioButton jRadioButton4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTable jTable1;
     private javax.swing.JTable jTable2;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JTextField jTextField4;
     private javax.swing.JTextField jTextField5;
     private javax.swing.JTextField jTextField6;
     private javax.swing.JTextField jTextField7;
     private javax.swing.JTable logisticJTable;
     private javax.swing.JMenu lookMenu;
     private javax.swing.JTable materialJTable;
     private javax.swing.JTable projectJTable;
     private javax.swing.JCheckBox reportApproved;
     private javax.swing.JCheckBox reportAvailable;
     private javax.swing.JCheckBox reportDefined;
     private javax.swing.JCheckBox reportEngaged;
     private javax.swing.JCheckBox reportInLoco;
     private javax.swing.JCheckBox reportNotStarted;
     private javax.swing.JPanel reportPanel;
     private javax.swing.JCheckBox reportRequested;
     private javax.swing.JTable reportTable;
     private javax.swing.JButton saveReportButton;
     private javax.swing.JScrollPane scrlTree;
     private javax.swing.JLabel serviceDescriptionLabel;
     private javax.swing.JPanel servicePanel;
     private javax.swing.JFormattedTextField startDateField;
     private javax.swing.JLabel startDateLabel;
     private javax.swing.JComboBox statusComboBox;
     private javax.swing.JPanel subPanelLabour;
     private javax.swing.JPanel subPanelLogistics;
     private javax.swing.JPanel subPanelMaterial;
     private javax.swing.JPanel subPanelProject;
     private javax.swing.JComboBox tableComboBox;
     private javax.swing.JTree treeServicos;
     private javax.swing.JTable workmanJTable;
     // End of variables declaration//GEN-END:variables
 }
