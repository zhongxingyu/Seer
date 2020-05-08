 /*
  * ManagGuiView.java
  */
 
 package managgui;
 
 import Client.Utils;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 /**
  *
  * @author Angelo
  */
 /**
  * The application's main frame.
  */
 public class ManagGuiView extends FrameView {
     
     private boolean flagCliente = false;
     private MonitorCen p = new MonitorCen(0, 0);
     private FinestraSwing DatiCliente;
     private FinestraSwing cercaCliente;
     private FinestraSwing schedaProdotto;
     private FinestraSwing DatiClienteView;
     private FinestraSwing schedaArticoloMagazzino;
     private FinestraSwing DatiClienteSimply;
     private FinestraSwing pezziUtilizzati;
     private FinestraSwing gestioneRientri;
     private ImageIcon clienti = createImageIcon ("images/clienti2.png", "ico clienti scheda tab");
     private ImageIcon frontEnd = createImageIcon ("images/frontend2.png", "ico accettazione scheda tab");
     private ImageIcon warehouse = createImageIcon ("images/warehouse.png", "ico magazzino scheda tab");
     private ImageIcon billing = createImageIcon ("images/fatt.png", "ico fatturazione scheda tab");
     
     //class just for number :D
     private JustNumber justNumbers = new JustNumber();
     
     private SharedClasses.Customer c;
     private SharedClasses.Device d;
     private SharedClasses.Repair re;
     private SharedClasses.Warehouse sp;
     private SharedClasses.Details de;
     
     private ArrayList<Object> repairRet;                        // used by Repair search
     private ArrayList<Object> customerRet;
     private ArrayList<Object> warehouseRet;
     private ArrayList<Object> usageRet;                         // used by Usage search
     private ArrayList<SharedClasses.UsageCache> cache;
     private ArrayList<SharedClasses.UsageCache> old;                   // store the situation of usage before editing
     
     public ManagGuiView(SingleFrameApplication app) {
         super(app);
 
         initComponents();
                 
         //magazzino
         jPanel5.setVisible(false);//lista risultato tabella articoli magazzino
         jPanel6.setVisible(false);//aggiungi pezzo
         
         
         //front-end accettazione
        jPanel9.setVisible(false);
        jPanel10.setVisible(false);
        ifConnected();
        
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
         messageTimer = new Timer(messageTimeout, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 statusMessageLabel.setText("");
             }
         });
         messageTimer.setRepeats(false);
         int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
         for (int i = 0; i < busyIcons.length; i++) {
             busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
         }
         busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                 statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
             }
         });
         idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
 
         
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
         taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 String propertyName = evt.getPropertyName();
                 if ("started".equals(propertyName)) {
                     if (!busyIconTimer.isRunning()) {
                         statusAnimationLabel.setIcon(busyIcons[0]);
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(true);
                 } else if ("done".equals(propertyName)) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon(idleIcon);
                     progressBar.setVisible(false);
                     progressBar.setValue(0);
                 } else if ("message".equals(propertyName)) {
                     String text = (String)(evt.getNewValue());
                     statusMessageLabel.setText((text == null) ? "" : text);
                     messageTimer.restart();
                 } else if ("progress".equals(propertyName)) {
                     int value = (Integer)(evt.getNewValue());
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(value);
                 }
             }
         });
     }
     
 
     @Action
     public void showAboutBox() {
         if (aboutBox == null) {
             JFrame mainFrame = ManagGuiApp.getApplication().getMainFrame();
             aboutBox = new ManagGuiAboutBox(mainFrame);
             aboutBox.setLocationRelativeTo(mainFrame);
         }
         ManagGuiApp.getApplication().show(aboutBox);
     }
     
     @Action
     public void showSettingBox() {
         if (settingBox == null) {
             JFrame mainFrame = ManagGuiApp.getApplication().getMainFrame();
             settingBox = new ManagGuiSettingBox(mainFrame);
             settingBox.setLocationRelativeTo(mainFrame);
         }
         ManagGuiApp.getApplication().show(settingBox);
     }
     
     
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel1 = new javax.swing.JPanel();
         jPanel9 = new javax.swing.JPanel();
         jLabel28 = new javax.swing.JLabel();
         jLabel29 = new javax.swing.JLabel();
         jTextField19 = new javax.swing.JTextField();
         jTextField20 = new javax.swing.JTextField();
         jLabel30 = new javax.swing.JLabel();
         jLabel31 = new javax.swing.JLabel();
         jLabel32 = new javax.swing.JLabel();
         jTextField21 = new javax.swing.JTextField();
         jTextField22 = new javax.swing.JTextField();
         jTextField23 = new javax.swing.JTextField();
         jComboBox3 = new javax.swing.JComboBox();
         jLabel33 = new javax.swing.JLabel();
         jLabel34 = new javax.swing.JLabel();
         jTextField24 = new javax.swing.JTextField();
         jLabel35 = new javax.swing.JLabel();
         jTextField25 = new javax.swing.JTextField();
         jCheckBox2 = new javax.swing.JCheckBox();
         jTextField26 = new javax.swing.JTextField();
         jLabel36 = new javax.swing.JLabel();
         jComboBox4 = new javax.swing.JComboBox();
         jLabel37 = new javax.swing.JLabel();
         jScrollPane7 = new javax.swing.JScrollPane();
         jTextArea5 = new javax.swing.JTextArea();
         jLabel38 = new javax.swing.JLabel();
         jScrollPane8 = new javax.swing.JScrollPane();
         jTextArea6 = new javax.swing.JTextArea();
         jLabel39 = new javax.swing.JLabel();
         jScrollPane9 = new javax.swing.JScrollPane();
         jTextArea7 = new javax.swing.JTextArea();
         jButton17 = new javax.swing.JButton();
         jButton18 = new javax.swing.JButton();
         jButton41 = new javax.swing.JButton();
         jButton45 = new javax.swing.JButton();
         jButton48 = new javax.swing.JButton();
         jButton49 = new javax.swing.JButton();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jPanel10 = new javax.swing.JPanel();
         jLabel40 = new javax.swing.JLabel();
         jLabel41 = new javax.swing.JLabel();
         jTextField27 = new javax.swing.JTextField();
         jTextField28 = new javax.swing.JTextField();
         jLabel42 = new javax.swing.JLabel();
         jTextField29 = new javax.swing.JTextField();
         jLabel43 = new javax.swing.JLabel();
         jTextField30 = new javax.swing.JTextField();
         jButton11 = new javax.swing.JButton();
         jButton12 = new javax.swing.JButton();
         jScrollPane5 = new javax.swing.JScrollPane();
         jTable3 = new javax.swing.JTable();
         jButton46 = new javax.swing.JButton();
         jButton47 = new javax.swing.JButton();
         jPanel2 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jTextField2 = new javax.swing.JTextField();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jScrollPane21 = new javax.swing.JScrollPane();
         jTable5 = new javax.swing.JTable();
         jPanel3 = new javax.swing.JPanel();
         jButton5 = new javax.swing.JButton();
         jButton6 = new javax.swing.JButton();
         jPanel6 = new javax.swing.JPanel();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jTextField4 = new javax.swing.JTextField();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jTextField5 = new javax.swing.JTextField();
         jTextField6 = new javax.swing.JTextField();
         jButton9 = new javax.swing.JButton();
         jButton10 = new javax.swing.JButton();
         jPanel5 = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         jTextField3 = new javax.swing.JTextField();
         jButton7 = new javax.swing.JButton();
         jButton8 = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jLabel83 = new javax.swing.JLabel();
         jPanel11 = new javax.swing.JPanel();
         jPanel19 = new javax.swing.JPanel();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
         jLabel80 = new javax.swing.JLabel();
         jLabel82 = new javax.swing.JLabel();
         jPanel7 = new javax.swing.JPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTextArea2 = new javax.swing.JTextArea();
         jLabel16 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         jTextField10 = new javax.swing.JTextField();
         jTextField9 = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         jTextField8 = new javax.swing.JTextField();
         jTextField7 = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jButton15 = new javax.swing.JButton();
         jButton16 = new javax.swing.JButton();
         jButton19 = new javax.swing.JButton();
         jPanel8 = new javax.swing.JPanel();
         jLabel17 = new javax.swing.JLabel();
         jTextField11 = new javax.swing.JTextField();
         jTextField12 = new javax.swing.JTextField();
         jButton20 = new javax.swing.JButton();
         jButton21 = new javax.swing.JButton();
         jButton22 = new javax.swing.JButton();
         jScrollPane20 = new javax.swing.JScrollPane();
         jTable2 = new javax.swing.JTable();
         jPanel12 = new javax.swing.JPanel();
         jLabel44 = new javax.swing.JLabel();
         jLabel45 = new javax.swing.JLabel();
         jTextField31 = new javax.swing.JTextField();
         jTextField32 = new javax.swing.JTextField();
         jLabel46 = new javax.swing.JLabel();
         jLabel47 = new javax.swing.JLabel();
         jLabel48 = new javax.swing.JLabel();
         jTextField33 = new javax.swing.JTextField();
         jTextField34 = new javax.swing.JTextField();
         jTextField35 = new javax.swing.JTextField();
         jComboBox5 = new javax.swing.JComboBox();
         jLabel49 = new javax.swing.JLabel();
         jLabel50 = new javax.swing.JLabel();
         jTextField36 = new javax.swing.JTextField();
         jLabel51 = new javax.swing.JLabel();
         jTextField37 = new javax.swing.JTextField();
         jCheckBox3 = new javax.swing.JCheckBox();
         jTextField38 = new javax.swing.JTextField();
         jLabel52 = new javax.swing.JLabel();
         jComboBox6 = new javax.swing.JComboBox();
         jLabel53 = new javax.swing.JLabel();
         jScrollPane10 = new javax.swing.JScrollPane();
         jTextArea8 = new javax.swing.JTextArea();
         jLabel54 = new javax.swing.JLabel();
         jScrollPane11 = new javax.swing.JScrollPane();
         jTextArea9 = new javax.swing.JTextArea();
         jLabel55 = new javax.swing.JLabel();
         jScrollPane12 = new javax.swing.JScrollPane();
         jTextArea10 = new javax.swing.JTextArea();
         jButton25 = new javax.swing.JButton();
         jButton26 = new javax.swing.JButton();
         jButton27 = new javax.swing.JButton();
         jButton35 = new javax.swing.JButton();
         jPanel13 = new javax.swing.JPanel();
         jScrollPane13 = new javax.swing.JScrollPane();
         jTextArea3 = new javax.swing.JTextArea();
         jLabel23 = new javax.swing.JLabel();
         jLabel24 = new javax.swing.JLabel();
         jTextField13 = new javax.swing.JTextField();
         jTextField14 = new javax.swing.JTextField();
         jLabel25 = new javax.swing.JLabel();
         jTextField15 = new javax.swing.JTextField();
         jTextField16 = new javax.swing.JTextField();
         jLabel26 = new javax.swing.JLabel();
         jLabel27 = new javax.swing.JLabel();
         jButton13 = new javax.swing.JButton();
         jButton14 = new javax.swing.JButton();
         jPanel14 = new javax.swing.JPanel();
         jLabel56 = new javax.swing.JLabel();
         jLabel57 = new javax.swing.JLabel();
         jLabel58 = new javax.swing.JLabel();
         jLabel59 = new javax.swing.JLabel();
         jTextField17 = new javax.swing.JTextField();
         jScrollPane14 = new javax.swing.JScrollPane();
         jTextArea4 = new javax.swing.JTextArea();
         jTextField18 = new javax.swing.JTextField();
         jTextField39 = new javax.swing.JTextField();
         jButton28 = new javax.swing.JButton();
         jButton29 = new javax.swing.JButton();
         jPanel15 = new javax.swing.JPanel();
         jLabel60 = new javax.swing.JLabel();
         jLabel61 = new javax.swing.JLabel();
         jLabel62 = new javax.swing.JLabel();
         jTextField41 = new javax.swing.JTextField();
         jTextField42 = new javax.swing.JTextField();
         jButton30 = new javax.swing.JButton();
         jButton31 = new javax.swing.JButton();
         jDialog1 = new javax.swing.JDialog();
         jButton32 = new javax.swing.JButton();
         jButton33 = new javax.swing.JButton();
         jScrollPane15 = new javax.swing.JScrollPane();
         jTextArea11 = new javax.swing.JTextArea();
         jLabel63 = new javax.swing.JLabel();
         jLabel64 = new javax.swing.JLabel();
         jTextField40 = new javax.swing.JTextField();
         jTextField43 = new javax.swing.JTextField();
         jLabel65 = new javax.swing.JLabel();
         jTextField44 = new javax.swing.JTextField();
         jTextField45 = new javax.swing.JTextField();
         jLabel66 = new javax.swing.JLabel();
         jLabel67 = new javax.swing.JLabel();
         jPanel16 = new javax.swing.JPanel();
         jScrollPane16 = new javax.swing.JScrollPane();
         jTextArea12 = new javax.swing.JTextArea();
         jLabel68 = new javax.swing.JLabel();
         jLabel69 = new javax.swing.JLabel();
         jTextField46 = new javax.swing.JTextField();
         jTextField47 = new javax.swing.JTextField();
         jLabel70 = new javax.swing.JLabel();
         jTextField48 = new javax.swing.JTextField();
         jTextField49 = new javax.swing.JTextField();
         jLabel71 = new javax.swing.JLabel();
         jLabel72 = new javax.swing.JLabel();
         jToggleButton1 = new javax.swing.JToggleButton();
         jPanel17 = new javax.swing.JPanel();
         jLabel73 = new javax.swing.JLabel();
         jLabel74 = new javax.swing.JLabel();
         jLabel75 = new javax.swing.JLabel();
         jButton37 = new javax.swing.JButton();
         jButton38 = new javax.swing.JButton();
         jLabel76 = new javax.swing.JLabel();
         jTextField50 = new javax.swing.JTextField();
         jButton36 = new javax.swing.JButton();
         jTextField51 = new javax.swing.JTextField();
         jLabel77 = new javax.swing.JLabel();
         jButton39 = new javax.swing.JButton();
         jButton40 = new javax.swing.JButton();
         jButton42 = new javax.swing.JButton();
         jButton43 = new javax.swing.JButton();
         jButton44 = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable6 = new javax.swing.JTable();
         jScrollPane6 = new javax.swing.JScrollPane();
         jTable7 = new javax.swing.JTable();
         jPanel18 = new javax.swing.JPanel();
         jLabel81 = new javax.swing.JLabel();
         jTextField55 = new javax.swing.JTextField();
         jScrollPane19 = new javax.swing.JScrollPane();
         jTable4 = new javax.swing.JTable();
         jLabel78 = new javax.swing.JLabel();
         jPanel4 = new javax.swing.JPanel();
         jFormattedTextField1 = new javax.swing.JFormattedTextField();
 
         mainPanel.setName("mainPanel"); // NOI18N
 
         jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         jTabbedPane1.setName("jTabbedPane1"); // NOI18N
         jTabbedPane1.setPreferredSize(new java.awt.Dimension(1024, 786));
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(managgui.ManagGuiApp.class).getContext().getResourceMap(ManagGuiView.class);
         jPanel1.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
         jPanel1.setName("jPanel1"); // NOI18N
 
         jPanel9.setBackground(resourceMap.getColor("jPanel9.background")); // NOI18N
         jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel9.setName("jPanel9"); // NOI18N
 
         jLabel28.setText(resourceMap.getString("jLabel28.text")); // NOI18N
         jLabel28.setName("jLabel28"); // NOI18N
 
         jLabel29.setText(resourceMap.getString("jLabel29.text")); // NOI18N
         jLabel29.setName("jLabel29"); // NOI18N
 
         jTextField19.setText(resourceMap.getString("jTextField19.text")); // NOI18N
         jTextField19.setName("jTextField19"); // NOI18N
 
         jTextField20.setText(resourceMap.getString("jTextField20.text")); // NOI18N
         jTextField20.setMaximumSize(new java.awt.Dimension(1024, 786));
         jTextField20.setName("jTextField20"); // NOI18N
 
         jLabel30.setText(resourceMap.getString("jLabel30.text")); // NOI18N
         jLabel30.setName("jLabel30"); // NOI18N
 
         jLabel31.setText(resourceMap.getString("jLabel31.text")); // NOI18N
         jLabel31.setName("jLabel31"); // NOI18N
 
         jLabel32.setText(resourceMap.getString("jLabel32.text")); // NOI18N
         jLabel32.setName("jLabel32"); // NOI18N
 
         jTextField21.setName("jTextField21"); // NOI18N
 
         jTextField22.setName("jTextField22"); // NOI18N
 
         jTextField23.setName("jTextField23"); // NOI18N
 
         jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "---", "Telefonia", "Informatica", "Altro" }));
         jComboBox3.setName("jComboBox3"); // NOI18N
 
         jLabel33.setText(resourceMap.getString("jLabel33.text")); // NOI18N
         jLabel33.setName("jLabel33"); // NOI18N
 
         jLabel34.setText(resourceMap.getString("jLabel34.text")); // NOI18N
         jLabel34.setName("jLabel34"); // NOI18N
 
         jTextField24.setName("jTextField24"); // NOI18N
 
         jLabel35.setText(resourceMap.getString("jLabel35.text")); // NOI18N
         jLabel35.setName("jLabel35"); // NOI18N
 
         jTextField25.setName("jTextField25"); // NOI18N
 
         jCheckBox2.setBackground(resourceMap.getColor("jCheckBox2.background")); // NOI18N
         jCheckBox2.setText(resourceMap.getString("jCheckBox2.text")); // NOI18N
         jCheckBox2.setName("jCheckBox2"); // NOI18N
         jCheckBox2.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jCheckBox2StateChanged(evt);
             }
         });
 
         jTextField26.setName("jTextField26"); // NOI18N
 
         jLabel36.setText(resourceMap.getString("jLabel36.text")); // NOI18N
         jLabel36.setName("jLabel36"); // NOI18N
 
         jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "in Accettazione", "in Lavorazione", "attesa pezzi", "LV Sup", "Lav terminata", "Restituito", "Prev non Accettato", "Rip non conveniente" }));
         jComboBox4.setName("jComboBox4"); // NOI18N
 
         jLabel37.setText(resourceMap.getString("jLabel37.text")); // NOI18N
         jLabel37.setName("jLabel37"); // NOI18N
 
         jScrollPane7.setName("jScrollPane7"); // NOI18N
 
         jTextArea5.setColumns(20);
         jTextArea5.setRows(5);
         jTextArea5.setName("jTextArea5"); // NOI18N
         jScrollPane7.setViewportView(jTextArea5);
 
         jLabel38.setText(resourceMap.getString("jLabel38.text")); // NOI18N
         jLabel38.setName("jLabel38"); // NOI18N
 
         jScrollPane8.setName("jScrollPane8"); // NOI18N
 
         jTextArea6.setColumns(20);
         jTextArea6.setRows(5);
         jTextArea6.setName("jTextArea6"); // NOI18N
         jScrollPane8.setViewportView(jTextArea6);
 
         jLabel39.setText(resourceMap.getString("jLabel39.text")); // NOI18N
         jLabel39.setName("jLabel39"); // NOI18N
 
         jScrollPane9.setName("jScrollPane9"); // NOI18N
 
         jTextArea7.setColumns(20);
         jTextArea7.setRows(5);
         jTextArea7.setName("jTextArea7"); // NOI18N
         jScrollPane9.setViewportView(jTextArea7);
 
         jButton17.setIcon(resourceMap.getIcon("jButton17.icon")); // NOI18N
         jButton17.setText(resourceMap.getString("jButton17.text")); // NOI18N
         jButton17.setName("jButton17"); // NOI18N
         jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton17MouseClicked(evt);
             }
         });
 
         jButton18.setIcon(resourceMap.getIcon("jButton18.icon")); // NOI18N
         jButton18.setText(resourceMap.getString("jButton18.text")); // NOI18N
         jButton18.setName("jButton18"); // NOI18N
         jButton18.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton18MouseClicked(evt);
             }
         });
 
         jButton41.setIcon(resourceMap.getIcon("jButton41.icon")); // NOI18N
         jButton41.setText(resourceMap.getString("jButton41.text")); // NOI18N
         jButton41.setName("jButton41"); // NOI18N
         jButton41.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton41MouseClicked(evt);
             }
         });
 
         jButton45.setIcon(resourceMap.getIcon("jButton45.icon")); // NOI18N
         jButton45.setText(resourceMap.getString("jButton45.text")); // NOI18N
         jButton45.setName("jButton45"); // NOI18N
         jButton45.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton45MouseClicked(evt);
             }
         });
 
         jButton48.setIcon(resourceMap.getIcon("jButton48.icon")); // NOI18N
         jButton48.setText(resourceMap.getString("jButton48.text")); // NOI18N
         jButton48.setName("jButton48"); // NOI18N
         jButton48.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton48MouseClicked(evt);
             }
         });
 
         jButton49.setIcon(resourceMap.getIcon("jButton49.icon")); // NOI18N
         jButton49.setText(resourceMap.getString("jButton49.text")); // NOI18N
         jButton49.setName("jButton49"); // NOI18N
         jButton49.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton49MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(jLabel36)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 217, Short.MAX_VALUE)
                         .addComponent(jButton41)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton18)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton17))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGap(12, 12, 12)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jCheckBox2)
                             .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(jPanel9Layout.createSequentialGroup()
                                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel37)
                                     .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGap(55, 55, 55)
                                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel38))
                                 .addGap(56, 56, 56)
                                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel39)
                                     .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addGroup(jPanel9Layout.createSequentialGroup()
                                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(jPanel9Layout.createSequentialGroup()
                                         .addComponent(jLabel35)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jButton45))
                                     .addGroup(jPanel9Layout.createSequentialGroup()
                                         .addComponent(jLabel33)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addGap(33, 33, 33)
                                         .addComponent(jLabel34)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(jLabel31)
                                     .addComponent(jLabel32)
                                     .addComponent(jLabel30))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(jTextField23, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                                         .addComponent(jLabel29)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addComponent(jLabel28)
                                         .addGap(6, 6, 6))
                                     .addComponent(jTextField22)
                                     .addComponent(jTextField21, javax.swing.GroupLayout.Alignment.TRAILING)))))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGap(6, 6, 6)
                         .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(jButton48)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton49)))
                 .addContainerGap())
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jButton49)
                             .addComponent(jButton48))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(18, 18, 18)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel33)
                             .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel34)
                             .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel35)
                             .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton45))
                         .addGap(8, 8, 8)
                         .addComponent(jCheckBox2)
                         .addGap(8, 8, 8)
                         .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel29)
                             .addComponent(jLabel28))
                         .addGap(18, 18, 18)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel30))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel32)
                             .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel31))))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(jPanel9Layout.createSequentialGroup()
                                 .addComponent(jLabel37)
                                 .addGap(6, 6, 6))
                             .addGroup(jPanel9Layout.createSequentialGroup()
                                 .addComponent(jLabel39)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(jLabel38)
                         .addGap(6, 6, 6)
                         .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel36)
                         .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jButton18)
                         .addComponent(jButton41)
                         .addComponent(jButton17)))
                 .addGap(12, 12, 12))
         );
 
         jButton1.setBackground(resourceMap.getColor("jButton1.background")); // NOI18N
         jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
         jButton1.setAutoscrolls(true);
         jButton1.setName("jButton1"); // NOI18N
         jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton1MouseClicked(evt);
             }
         });
 
         jButton2.setBackground(resourceMap.getColor("jButton2.background")); // NOI18N
         jButton2.setForeground(resourceMap.getColor("jButton2.foreground")); // NOI18N
         jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
         jButton2.setName("jButton2"); // NOI18N
         jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton2MouseClicked(evt);
             }
         });
 
         jPanel10.setBackground(resourceMap.getColor("jPanel10.background")); // NOI18N
         jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel10.setName("jPanel10"); // NOI18N
 
         jLabel40.setText(resourceMap.getString("jLabel40.text")); // NOI18N
         jLabel40.setName("jLabel40"); // NOI18N
 
         jLabel41.setText(resourceMap.getString("jLabel41.text")); // NOI18N
         jLabel41.setName("jLabel41"); // NOI18N
 
         jTextField27.setName("jTextField27"); // NOI18N
 
         jTextField28.setName("jTextField28"); // NOI18N
 
         jLabel42.setText(resourceMap.getString("jLabel42.text")); // NOI18N
         jLabel42.setName("jLabel42"); // NOI18N
 
         jTextField29.setName("jTextField29"); // NOI18N
 
         jLabel43.setText(resourceMap.getString("jLabel43.text")); // NOI18N
         jLabel43.setName("jLabel43"); // NOI18N
 
         jTextField30.setName("jTextField30"); // NOI18N
 
         jButton11.setIcon(resourceMap.getIcon("jButton11.icon")); // NOI18N
         jButton11.setText(resourceMap.getString("jButton11.text")); // NOI18N
         jButton11.setName("jButton11"); // NOI18N
         jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton11MouseClicked(evt);
             }
         });
 
         jButton12.setIcon(resourceMap.getIcon("jButton12.icon")); // NOI18N
         jButton12.setText(resourceMap.getString("jButton12.text")); // NOI18N
         jButton12.setName("jButton12"); // NOI18N
         jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton12MouseClicked(evt);
             }
         });
 
         jScrollPane5.setName("jScrollPane5"); // NOI18N
 
         jTable3.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Cognome", "Nome", "Imei-S/N", "stato lavorazione"
             }
         ));
         jTable3.setName("jTable3"); // NOI18N
         jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable3MouseClicked(evt);
             }
         });
         jScrollPane5.setViewportView(jTable3);
         setJTableRepair(jTable3, 0);
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel43)
                             .addComponent(jLabel40))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField27, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(36, 36, 36)
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel41)
                             .addComponent(jLabel42))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(24, 24, 24)
                         .addComponent(jButton12)
                         .addGap(18, 18, 18)
                         .addComponent(jButton11))
                     .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel41)
                             .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel42)))
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel40)
                             .addComponent(jTextField27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel43)
                             .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jButton12)
                         .addComponent(jButton11)))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jButton46.setIcon(resourceMap.getIcon("jButton46.icon")); // NOI18N
         jButton46.setText(resourceMap.getString("jButton46.text")); // NOI18N
         jButton46.setName("jButton46"); // NOI18N
         jButton46.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton46MouseClicked(evt);
             }
         });
 
         jButton47.setIcon(resourceMap.getIcon("jButton47.icon")); // NOI18N
         jButton47.setText(resourceMap.getString("jButton47.text")); // NOI18N
         jButton47.setName("jButton47"); // NOI18N
         jButton47.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton47MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(15, 15, 15)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jButton46)
                         .addGap(18, 18, 18)
                         .addComponent(jButton47)
                         .addGap(261, 261, 261)
                         .addComponent(jButton1)
                         .addGap(18, 18, 18)
                         .addComponent(jButton2)))
                 .addContainerGap(78, Short.MAX_VALUE))
             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton1)
                     .addComponent(jButton2)
                     .addComponent(jButton47)
                     .addComponent(jButton46))
                 .addGap(18, 18, 18)
                 .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(207, Short.MAX_VALUE))
             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                     .addGap(50, 50, 50)
                     .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(258, Short.MAX_VALUE)))
         );
 
         jButton1.setVisible(false);
         jButton2.setVisible(false);
 
         jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), frontEnd, jPanel1); // NOI18N
 
         jPanel2.setBackground(resourceMap.getColor("jPanel2.background")); // NOI18N
         jPanel2.setName("jPanel2"); // NOI18N
 
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
 
         jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
         jTextField1.setName("jTextField1"); // NOI18N
         jTextField1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField1MouseClicked(evt);
             }
         });
 
         jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
         jTextField2.setName("jTextField2"); // NOI18N
         jTextField2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField2MouseClicked(evt);
             }
         });
 
         jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
         jButton3.setName("jButton3"); // NOI18N
         jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton3MouseClicked(evt);
             }
         });
 
         jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
         jButton4.setName("jButton4"); // NOI18N
         jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton4MouseClicked(evt);
             }
         });
 
         jScrollPane21.setName("jScrollPane21"); // NOI18N
 
         jTable5.setAutoCreateRowSorter(true);
         jTable5.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         jTable5.setName("jTable5"); // NOI18N
         jTable5.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable5MouseClicked(evt);
             }
         });
         jScrollPane21.setViewportView(jTable5);
         setJTableClient(jTable5, 0);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 1026, Short.MAX_VALUE)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(39, 39, 39)
                         .addComponent(jButton3)
                         .addGap(18, 18, 18)
                         .addComponent(jButton4)))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton3)
                     .addComponent(jButton4))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), clienti, jPanel2); // NOI18N
 
         jPanel3.setBackground(resourceMap.getColor("jPanel3.background")); // NOI18N
         jPanel3.setName("jPanel3"); // NOI18N
 
         jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
         jButton5.setName("jButton5"); // NOI18N
         jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton5MouseClicked(evt);
             }
         });
 
         jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
         jButton6.setName("jButton6"); // NOI18N
         jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton6MouseClicked(evt);
             }
         });
 
         jPanel6.setBackground(resourceMap.getColor("jPanel6.background")); // NOI18N
         jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel6.setName("jPanel6"); // NOI18N
 
         jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
         jLabel8.setName("jLabel8"); // NOI18N
 
         jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
         jLabel9.setName("jLabel9"); // NOI18N
 
         jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
         jLabel10.setName("jLabel10"); // NOI18N
 
         jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
         jLabel11.setName("jLabel11"); // NOI18N
 
         jTextField4.setText(resourceMap.getString("jTextField4.text")); // NOI18N
         jTextField4.setName("jTextField4"); // NOI18N
 
         jScrollPane3.setName("jScrollPane3"); // NOI18N
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jTextArea1.setName("jTextArea1"); // NOI18N
         jScrollPane3.setViewportView(jTextArea1);
 
         jTextField5.setName("jTextField5"); // NOI18N
 
         jTextField6.setName("jTextField6"); // NOI18N
 
         jButton9.setIcon(resourceMap.getIcon("jButton9.icon")); // NOI18N
         jButton9.setText(resourceMap.getString("jButton9.text")); // NOI18N
         jButton9.setName("jButton9"); // NOI18N
         jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton9MouseClicked(evt);
             }
         });
 
         jButton10.setIcon(resourceMap.getIcon("jButton10.icon")); // NOI18N
         jButton10.setText(resourceMap.getString("jButton10.text")); // NOI18N
         jButton10.setName("jButton10"); // NOI18N
         jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton10MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                 .addGap(20, 20, 20)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                             .addComponent(jLabel8)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(jTextField4))
                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                             .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addComponent(jLabel10)
                                 .addComponent(jLabel9))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                     .addGroup(jPanel6Layout.createSequentialGroup()
                         .addGap(65, 65, 65)
                         .addComponent(jLabel11)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGap(102, 102, 102))
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton9)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                 .addComponent(jButton10)
                 .addContainerGap())
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel9)
                     .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel10))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel11))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton10)
                     .addComponent(jButton9))
                 .addContainerGap())
         );
 
         jTextField6.setDocument(justNumbers);
 
         jPanel5.setBackground(resourceMap.getColor("jPanel5.background")); // NOI18N
         jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel5.setName("jPanel5"); // NOI18N
 
         jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
         jLabel7.setName("jLabel7"); // NOI18N
 
         jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
         jTextField3.setName("jTextField3"); // NOI18N
         jTextField3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField3MouseClicked(evt);
             }
         });
 
         jButton7.setIcon(resourceMap.getIcon("jButton7.icon")); // NOI18N
         jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
         jButton7.setName("jButton7"); // NOI18N
         jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton7MouseClicked(evt);
             }
         });
 
         jButton8.setIcon(resourceMap.getIcon("jButton8.icon")); // NOI18N
         jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
         jButton8.setName("jButton8"); // NOI18N
         jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton8MouseClicked(evt);
             }
         });
 
         jScrollPane2.setName("jScrollPane2"); // NOI18N
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
             },
             new String [] {
             }
         ));
         jTable1.setName("jTable1"); // NOI18N
         jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable1MouseClicked(evt);
             }
         });
         jScrollPane2.setViewportView(jTable1);
         setJTableWarehouse(jTable1, 0);
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addGap(32, 32, 32)
                         .addComponent(jLabel7)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(41, 41, 41)
                         .addComponent(jButton7)
                         .addGap(35, 35, 35)
                         .addComponent(jButton8))
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 706, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton7)
                     .addComponent(jButton8)
                     .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel7))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jLabel83.setIcon(resourceMap.getIcon("jLabel83.icon")); // NOI18N
         jLabel83.setName("jLabel83"); // NOI18N
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(jLabel83)
                         .addGap(44, 44, 44)
                         .addComponent(jButton5)
                         .addGap(49, 49, 49)
                         .addComponent(jButton6))
                     .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(306, Short.MAX_VALUE))
             .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel3Layout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(575, Short.MAX_VALUE)))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jLabel83))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addGap(44, 44, 44)
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addGap(18, 18, 18)
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(293, Short.MAX_VALUE))
             .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel3Layout.createSequentialGroup()
                     .addGap(140, 140, 140)
                     .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(350, Short.MAX_VALUE)))
         );
 
         //jLabel83.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), warehouse, jPanel3); // NOI18N
 
         jPanel11.setName("jPanel11"); // NOI18N
 
         jPanel19.setName("jPanel19"); // NOI18N
 
         javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
         jPanel19.setLayout(jPanel19Layout);
         jPanel19Layout.setHorizontalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 501, Short.MAX_VALUE)
         );
         jPanel19Layout.setVerticalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 336, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(537, Short.MAX_VALUE))
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(457, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Fatturazione", jPanel11);
         jTabbedPane1.addTab(resourceMap.getString("jPanel11.TabConstraints.tabTitle"), billing, jPanel11); // NOI18N
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1059, Short.MAX_VALUE)
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 836, Short.MAX_VALUE)
         );
 
         menuBar.setName("menuBar"); // NOI18N
 
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(managgui.ManagGuiApp.class).getContext().getActionMap(ManagGuiView.class, this);
         jMenuItem1.setAction(actionMap.get("showSettingBox")); // NOI18N
         jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
         jMenuItem1.setName("jMenuItem1"); // NOI18N
         fileMenu.add(jMenuItem1);
 
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
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         jLabel80.setIcon(resourceMap.getIcon("jLabel80.icon")); // NOI18N
         jLabel80.setName("jLabel80"); // NOI18N
 
         jLabel82.setIcon(resourceMap.getIcon("jLabel82.icon")); // NOI18N
         jLabel82.setName("jLabel82"); // NOI18N
 
         javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(statusMessageLabel)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                         .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 781, Short.MAX_VALUE)))
                 .addGap(12, 12, 12)
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(statusAnimationLabel)
                     .addGroup(statusPanelLayout.createSequentialGroup()
                         .addGap(31, 31, 31)
                         .addComponent(jLabel80, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel82)))
                 .addContainerGap())
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(statusMessageLabel)
                         .addComponent(statusAnimationLabel))
                     .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(26, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                 .addContainerGap(20, Short.MAX_VALUE)
                 .addComponent(jLabel82))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel80, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
         );
 
         statusPanel.setSize(1030, 20);
 
         jPanel7.setBackground(resourceMap.getColor("jPanel7.background")); // NOI18N
         jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
         jPanel7.setName("jPanel7"); // NOI18N
 
         jScrollPane4.setName("jScrollPane4"); // NOI18N
 
         jTextArea2.setColumns(20);
         jTextArea2.setRows(5);
         jTextArea2.setName("jTextArea2"); // NOI18N
         jScrollPane4.setViewportView(jTextArea2);
 
         jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
         jLabel16.setName("jLabel16"); // NOI18N
 
         jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
         jLabel15.setName("jLabel15"); // NOI18N
 
         jTextField10.setName("jTextField10"); // NOI18N
 
         jTextField9.setName("jTextField9"); // NOI18N
 
         jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
         jLabel14.setName("jLabel14"); // NOI18N
 
         jTextField8.setText(resourceMap.getString("jTextField8.text")); // NOI18N
         jTextField8.setName("jTextField8"); // NOI18N
 
         jTextField7.setText(resourceMap.getString("jTextField7.text")); // NOI18N
         jTextField7.setName("jTextField7"); // NOI18N
 
         jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
         jLabel13.setName("jLabel13"); // NOI18N
 
         jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
         jLabel12.setName("jLabel12"); // NOI18N
 
         jButton15.setText(resourceMap.getString("jButton15.text")); // NOI18N
         jButton15.setName("jButton15"); // NOI18N
         jButton15.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton15MouseClicked(evt);
             }
         });
 
         jButton16.setName("jButton16"); // NOI18N
         jButton16.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton16MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel7Layout.createSequentialGroup()
                         .addComponent(jLabel12)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel13)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField8, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
                     .addGroup(jPanel7Layout.createSequentialGroup()
                         .addComponent(jLabel16)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE))
                     .addGroup(jPanel7Layout.createSequentialGroup()
                         .addComponent(jLabel15)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField10, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                         .addComponent(jLabel14)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField9, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                         .addComponent(jButton16)
                         .addGap(18, 18, 18)
                         .addComponent(jButton15)))
                 .addContainerGap())
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel12)
                     .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel13)
                     .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel14)
                     .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel15)
                     .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel16)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton15)
                     .addComponent(jButton16))
                 .addContainerGap())
         );
 
         jTextField10.setDocument(justNumbers);
 
         jButton19.setText(resourceMap.getString("jButton19.text")); // NOI18N
         jButton19.setName("jButton19"); // NOI18N
 
         jPanel8.setBackground(resourceMap.getColor("jPanel8.background")); // NOI18N
         jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         jPanel8.setName("jPanel8"); // NOI18N
 
         jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
         jLabel17.setName("jLabel17"); // NOI18N
 
         jTextField11.setText(resourceMap.getString("jTextField11.text")); // NOI18N
         jTextField11.setName("jTextField11"); // NOI18N
         jTextField11.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField11MouseClicked(evt);
             }
         });
 
         jTextField12.setText(resourceMap.getString("jTextField12.text")); // NOI18N
         jTextField12.setName("jTextField12"); // NOI18N
         jTextField12.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField12MouseClicked(evt);
             }
         });
 
         jButton20.setText(resourceMap.getString("jButton20.text")); // NOI18N
         jButton20.setName("jButton20"); // NOI18N
         jButton20.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton20MouseClicked(evt);
             }
         });
 
         jButton21.setText(resourceMap.getString("jButton21.text")); // NOI18N
         jButton21.setName("jButton21"); // NOI18N
         jButton21.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton21MouseClicked(evt);
             }
         });
 
         jButton22.setIcon(resourceMap.getIcon("jButton22.icon")); // NOI18N
         jButton22.setText(resourceMap.getString("jButton22.text")); // NOI18N
         jButton22.setName("jButton22"); // NOI18N
         jButton22.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton22MouseClicked(evt);
             }
         });
 
         jScrollPane20.setName("jScrollPane20"); // NOI18N
 
         jTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         jTable2.setName("jTable2"); // NOI18N
         jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable2MouseClicked(evt);
             }
         });
         jScrollPane20.setViewportView(jTable2);
         setJTableClient(jTable2, 0);
 
         javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel8Layout.createSequentialGroup()
                 .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                             .addContainerGap()
                             .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE))
                         .addComponent(jButton22))
                     .addGroup(jPanel8Layout.createSequentialGroup()
                         .addGap(31, 31, 31)
                         .addComponent(jLabel17)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(39, 39, 39)
                         .addComponent(jButton20)
                         .addGap(18, 18, 18)
                         .addComponent(jButton21)))
                 .addContainerGap())
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel8Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel17)
                     .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton20)
                     .addComponent(jButton21))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addComponent(jButton22)
                 .addContainerGap())
         );
 
         jPanel12.setBackground(resourceMap.getColor("jPanel12.background")); // NOI18N
         jPanel12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         jPanel12.setName("jPanel12"); // NOI18N
 
         jLabel44.setText(resourceMap.getString("jLabel44.text")); // NOI18N
         jLabel44.setName("jLabel44"); // NOI18N
 
         jLabel45.setText(resourceMap.getString("jLabel45.text")); // NOI18N
         jLabel45.setName("jLabel45"); // NOI18N
 
         jTextField31.setText(resourceMap.getString("jTextField31.text")); // NOI18N
         jTextField31.setName("jTextField31"); // NOI18N
 
         jTextField32.setText(resourceMap.getString("jTextField32.text")); // NOI18N
         jTextField32.setMaximumSize(new java.awt.Dimension(1024, 786));
         jTextField32.setName("jTextField32"); // NOI18N
 
         jLabel46.setText(resourceMap.getString("jLabel46.text")); // NOI18N
         jLabel46.setName("jLabel46"); // NOI18N
 
         jLabel47.setText(resourceMap.getString("jLabel47.text")); // NOI18N
         jLabel47.setName("jLabel47"); // NOI18N
 
         jLabel48.setText(resourceMap.getString("jLabel48.text")); // NOI18N
         jLabel48.setName("jLabel48"); // NOI18N
 
         jTextField33.setName("jTextField33"); // NOI18N
 
         jTextField34.setName("jTextField34"); // NOI18N
 
         jTextField35.setName("jTextField35"); // NOI18N
 
         jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "---", "Telefonia", "Informatica", "Altro" }));
         jComboBox5.setName("jComboBox5"); // NOI18N
 
         jLabel49.setText(resourceMap.getString("jLabel49.text")); // NOI18N
         jLabel49.setName("jLabel49"); // NOI18N
 
         jLabel50.setText(resourceMap.getString("jLabel50.text")); // NOI18N
         jLabel50.setName("jLabel50"); // NOI18N
 
         jTextField36.setName("jTextField36"); // NOI18N
 
         jLabel51.setText(resourceMap.getString("jLabel51.text")); // NOI18N
         jLabel51.setName("jLabel51"); // NOI18N
 
         jTextField37.setName("jTextField37"); // NOI18N
 
         jCheckBox3.setBackground(resourceMap.getColor("jCheckBox3.background")); // NOI18N
         jCheckBox3.setText(resourceMap.getString("jCheckBox3.text")); // NOI18N
         jCheckBox3.setName("jCheckBox3"); // NOI18N
         jCheckBox3.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jCheckBox3StateChanged(evt);
             }
         });
 
         jTextField38.setEditable(false);
         jTextField38.setName("jTextField38"); // NOI18N
 
         jLabel52.setText(resourceMap.getString("jLabel52.text")); // NOI18N
         jLabel52.setName("jLabel52"); // NOI18N
 
         jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "IN ACCETTAZIONE", "IN LAVORAZIONE", "ATTESA PEZZI", "LV SUP", "LAV TERMINATA", "RESTITUITO", "PREV NON ACCETTATO", "RIP NON CONVENIENTE" }));
         jComboBox6.setName("jComboBox6"); // NOI18N
 
         jLabel53.setText(resourceMap.getString("jLabel53.text")); // NOI18N
         jLabel53.setName("jLabel53"); // NOI18N
 
         jScrollPane10.setName("jScrollPane10"); // NOI18N
 
         jTextArea8.setColumns(20);
         jTextArea8.setRows(5);
         jTextArea8.setName("jTextArea8"); // NOI18N
         jScrollPane10.setViewportView(jTextArea8);
 
         jLabel54.setText(resourceMap.getString("jLabel54.text")); // NOI18N
         jLabel54.setName("jLabel54"); // NOI18N
 
         jScrollPane11.setName("jScrollPane11"); // NOI18N
 
         jTextArea9.setColumns(20);
         jTextArea9.setRows(5);
         jTextArea9.setName("jTextArea9"); // NOI18N
         jScrollPane11.setViewportView(jTextArea9);
 
         jLabel55.setText(resourceMap.getString("jLabel55.text")); // NOI18N
         jLabel55.setName("jLabel55"); // NOI18N
 
         jScrollPane12.setName("jScrollPane12"); // NOI18N
 
         jTextArea10.setColumns(20);
         jTextArea10.setRows(5);
         jTextArea10.setName("jTextArea10"); // NOI18N
         jScrollPane12.setViewportView(jTextArea10);
 
         jButton25.setIcon(resourceMap.getIcon("jButton25.icon")); // NOI18N
         jButton25.setText(resourceMap.getString("jButton25.text")); // NOI18N
         jButton25.setName("jButton25"); // NOI18N
         jButton25.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton25MouseClicked(evt);
             }
         });
 
         jButton26.setIcon(resourceMap.getIcon("jButton26.icon")); // NOI18N
         jButton26.setText(resourceMap.getString("jButton26.text")); // NOI18N
         jButton26.setName("jButton26"); // NOI18N
         jButton26.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton26MouseClicked(evt);
             }
         });
 
         jButton27.setText(resourceMap.getString("jButton27.text")); // NOI18N
         jButton27.setName("jButton27"); // NOI18N
         jButton27.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton27MouseClicked(evt);
             }
         });
 
         jButton35.setIcon(resourceMap.getIcon("jButton35.icon")); // NOI18N
         jButton35.setText(resourceMap.getString("jButton35.text")); // NOI18N
         jButton35.setName("jButton35"); // NOI18N
         jButton35.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton35MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(1042, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGap(77, 77, 77)
                         .addComponent(jLabel49)
                         .addGap(18, 18, 18)
                         .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGap(12, 12, 12)
                         .addComponent(jLabel50)
                         .addGap(12, 12, 12)
                         .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel51)
                         .addGap(12, 12, 12)
                         .addComponent(jTextField37, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGap(12, 12, 12)
                         .addComponent(jCheckBox3)
                         .addGap(8, 8, 8)
                         .addComponent(jTextField38, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGap(362, 362, 362)
                         .addComponent(jButton27)))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addComponent(jLabel45)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel44))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGap(18, 18, 18)
                         .addComponent(jLabel46)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 334, Short.MAX_VALUE)
                         .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel47)
                             .addComponent(jLabel48))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jTextField35, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
                             .addComponent(jTextField34, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE))))
                 .addGap(141, 141, 141))
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addComponent(jLabel53)
                         .addGap(185, 185, 185)
                         .addComponent(jLabel54)
                         .addGap(181, 181, 181)
                         .addComponent(jLabel55))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(75, 75, 75)
                         .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(77, 77, 77)
                         .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(553, Short.MAX_VALUE))
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton35)
                 .addContainerGap(1139, Short.MAX_VALUE))
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel52)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 629, Short.MAX_VALUE)
                 .addComponent(jButton26)
                 .addGap(18, 18, 18)
                 .addComponent(jButton25)
                 .addGap(141, 141, 141))
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton27))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(5, 5, 5)
                                 .addComponent(jLabel49))
                             .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(13, 13, 13)
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(7, 7, 7)
                                 .addComponent(jLabel50))
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(5, 5, 5)
                                 .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(7, 7, 7)
                                 .addComponent(jLabel51))
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(5, 5, 5)
                                 .addComponent(jTextField37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(13, 13, 13)
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(5, 5, 5)
                                 .addComponent(jCheckBox3))
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(7, 7, 7)
                                 .addComponent(jTextField38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel45)
                             .addComponent(jLabel44))
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(14, 14, 14)
                                 .addComponent(jLabel46))
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(12, 12, 12)
                                 .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(15, 15, 15)
                                 .addComponent(jLabel48))
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGap(13, 13, 13)
                                 .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(13, 13, 13)
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel47))))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel53)
                     .addComponent(jLabel54)
                     .addComponent(jLabel55))
                 .addGap(6, 6, 6)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jButton35)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel52)
                             .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jButton25)
                             .addComponent(jButton26))
                         .addGap(2, 2, 2)))
                 .addContainerGap())
         );
 
         jPanel13.setBackground(resourceMap.getColor("jPanel13.background")); // NOI18N
         jPanel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
         jPanel13.setName("jPanel13"); // NOI18N
 
         jScrollPane13.setName("jScrollPane13"); // NOI18N
 
         jTextArea3.setColumns(20);
         jTextArea3.setRows(5);
         jTextArea3.setName("jTextArea3"); // NOI18N
         jScrollPane13.setViewportView(jTextArea3);
 
         jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
         jLabel23.setName("jLabel23"); // NOI18N
 
         jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
         jLabel24.setName("jLabel24"); // NOI18N
 
         jTextField13.setName("jTextField13"); // NOI18N
 
         jTextField14.setName("jTextField14"); // NOI18N
 
         jLabel25.setText(resourceMap.getString("jLabel25.text")); // NOI18N
         jLabel25.setName("jLabel25"); // NOI18N
 
         jTextField15.setName("jTextField15"); // NOI18N
 
         jTextField16.setName("jTextField16"); // NOI18N
 
         jLabel26.setText(resourceMap.getString("jLabel26.text")); // NOI18N
         jLabel26.setName("jLabel26"); // NOI18N
 
         jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
         jLabel27.setName("jLabel27"); // NOI18N
 
         jButton13.setText(resourceMap.getString("jButton13.text")); // NOI18N
         jButton13.setName("jButton13"); // NOI18N
         jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton13MouseClicked(evt);
             }
         });
 
         jButton14.setText(resourceMap.getString("jButton14.text")); // NOI18N
         jButton14.setName("jButton14"); // NOI18N
         jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton14MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                         .addComponent(jLabel24)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField13, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                         .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel25)
                             .addComponent(jLabel27))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel13Layout.createSequentialGroup()
                                 .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jLabel26)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jTextField14, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                         .addComponent(jLabel23)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                                 .addComponent(jButton14)
                                 .addGap(18, 18, 18)
                                 .addComponent(jButton13))
                             .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))))
                 .addContainerGap())
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel27)
                     .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel26))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel25))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel24)
                     .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel23)
                     .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton14)
                     .addComponent(jButton13))
                 .addContainerGap())
         );
 
         jTextField13.setDocument(justNumbers);
 
         jPanel14.setBackground(resourceMap.getColor("jPanel14.background")); // NOI18N
         jPanel14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         jPanel14.setName("jPanel14"); // NOI18N
 
         jLabel56.setText(resourceMap.getString("jLabel56.text")); // NOI18N
         jLabel56.setName("jLabel56"); // NOI18N
 
         jLabel57.setText(resourceMap.getString("jLabel57.text")); // NOI18N
         jLabel57.setName("jLabel57"); // NOI18N
 
         jLabel58.setText(resourceMap.getString("jLabel58.text")); // NOI18N
         jLabel58.setName("jLabel58"); // NOI18N
 
         jLabel59.setText(resourceMap.getString("jLabel59.text")); // NOI18N
         jLabel59.setName("jLabel59"); // NOI18N
 
         jTextField17.setName("jTextField17"); // NOI18N
 
         jScrollPane14.setName("jScrollPane14"); // NOI18N
 
         jTextArea4.setColumns(20);
         jTextArea4.setRows(5);
         jTextArea4.setName("jTextArea4"); // NOI18N
         jScrollPane14.setViewportView(jTextArea4);
 
         jTextField18.setName("jTextField18"); // NOI18N
 
         jTextField39.setName("jTextField39"); // NOI18N
 
         jButton28.setIcon(resourceMap.getIcon("jButton28.icon")); // NOI18N
         jButton28.setText(resourceMap.getString("jButton28.text")); // NOI18N
         jButton28.setName("jButton28"); // NOI18N
         jButton28.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton28MouseClicked(evt);
             }
         });
 
         jButton29.setIcon(resourceMap.getIcon("jButton29.icon")); // NOI18N
         jButton29.setText(resourceMap.getString("jButton29.text")); // NOI18N
         jButton29.setName("jButton29"); // NOI18N
         jButton29.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton29MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
         jPanel14.setLayout(jPanel14Layout);
         jPanel14Layout.setHorizontalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel14Layout.createSequentialGroup()
                         .addGap(20, 20, 20)
                         .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel14Layout.createSequentialGroup()
                                     .addComponent(jLabel56)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jTextField17))
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel14Layout.createSequentialGroup()
                                     .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                         .addComponent(jLabel58)
                                         .addComponent(jLabel57))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                     .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(jTextField39, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                             .addGroup(jPanel14Layout.createSequentialGroup()
                                 .addGap(65, 65, 65)
                                 .addComponent(jLabel59)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(90, 90, 90))
                     .addGroup(jPanel14Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton28)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                         .addComponent(jButton29)))
                 .addContainerGap())
         );
         jPanel14Layout.setVerticalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addGap(23, 23, 23)
                 .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel56)
                     .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel57)
                     .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel58))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel59))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton28)
                     .addComponent(jButton29))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTextField39.setDocument(justNumbers);
 
         jPanel15.setBackground(resourceMap.getColor("jPanel15.background")); // NOI18N
         jPanel15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         jPanel15.setName("jPanel15"); // NOI18N
 
         jLabel60.setText(resourceMap.getString("jLabel60.text")); // NOI18N
         jLabel60.setName("jLabel60"); // NOI18N
 
         jLabel61.setText(resourceMap.getString("jLabel61.text")); // NOI18N
         jLabel61.setName("jLabel61"); // NOI18N
 
         jLabel62.setText(resourceMap.getString("jLabel62.text")); // NOI18N
         jLabel62.setName("jLabel62"); // NOI18N
 
         jTextField41.setText(resourceMap.getString("jTextField41.text")); // NOI18N
         jTextField41.setName("jTextField41"); // NOI18N
         jTextField41.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField41MouseClicked(evt);
             }
         });
 
         jTextField42.setName("jTextField42"); // NOI18N
 
         jButton30.setText(resourceMap.getString("jButton30.text")); // NOI18N
         jButton30.setName("jButton30"); // NOI18N
         jButton30.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton30MouseClicked(evt);
             }
         });
 
         jButton31.setText(resourceMap.getString("jButton31.text")); // NOI18N
         jButton31.setName("jButton31"); // NOI18N
         jButton31.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton31MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
         jPanel15.setLayout(jPanel15Layout);
         jPanel15Layout.setHorizontalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel15Layout.createSequentialGroup()
                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel15Layout.createSequentialGroup()
                         .addGap(20, 20, 20)
                         .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel60)
                             .addGroup(jPanel15Layout.createSequentialGroup()
                                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(jLabel62)
                                     .addComponent(jLabel61))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextField41, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                     .addGroup(jPanel15Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton30)
                         .addGap(18, 18, 18)
                         .addComponent(jButton31)))
                 .addContainerGap())
         );
         jPanel15Layout.setVerticalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel15Layout.createSequentialGroup()
                 .addGap(23, 23, 23)
                 .addComponent(jLabel60)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel61)
                     .addComponent(jTextField41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel62))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton30)
                     .addComponent(jButton31))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTextField42.setDocument(justNumbers);
 
         jDialog1.setName("jDialog1"); // NOI18N
 
         jButton32.setBackground(resourceMap.getColor("jButton32.background")); // NOI18N
         jButton32.setText(resourceMap.getString("jButton32.text")); // NOI18N
         jButton32.setName("jButton32"); // NOI18N
 
         jButton33.setBackground(resourceMap.getColor("jButton33.background")); // NOI18N
         jButton33.setText(resourceMap.getString("jButton33.text")); // NOI18N
         jButton33.setName("jButton33"); // NOI18N
 
         jScrollPane15.setName("jScrollPane15"); // NOI18N
 
         jTextArea11.setColumns(20);
         jTextArea11.setRows(5);
         jTextArea11.setName("jTextArea11"); // NOI18N
         jScrollPane15.setViewportView(jTextArea11);
 
         jLabel63.setText(resourceMap.getString("jLabel63.text")); // NOI18N
         jLabel63.setName("jLabel63"); // NOI18N
 
         jLabel64.setText(resourceMap.getString("jLabel64.text")); // NOI18N
         jLabel64.setName("jLabel64"); // NOI18N
 
         jTextField40.setName("jTextField40"); // NOI18N
 
         jTextField43.setName("jTextField43"); // NOI18N
 
         jLabel65.setText(resourceMap.getString("jLabel65.text")); // NOI18N
         jLabel65.setName("jLabel65"); // NOI18N
 
         jTextField44.setName("jTextField44"); // NOI18N
 
         jTextField45.setName("jTextField45"); // NOI18N
 
         jLabel66.setText(resourceMap.getString("jLabel66.text")); // NOI18N
         jLabel66.setName("jLabel66"); // NOI18N
 
         jLabel67.setText(resourceMap.getString("jLabel67.text")); // NOI18N
         jLabel67.setName("jLabel67"); // NOI18N
 
         javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
         jDialog1.getContentPane().setLayout(jDialog1Layout);
         jDialog1Layout.setHorizontalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 481, Short.MAX_VALUE)
             .addGroup(jDialog1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jDialog1Layout.createSequentialGroup()
                         .addComponent(jLabel67)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField45, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel66)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField44, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                     .addGroup(jDialog1Layout.createSequentialGroup()
                         .addComponent(jLabel63)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
                     .addGroup(jDialog1Layout.createSequentialGroup()
                         .addComponent(jLabel64)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField40, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                         .addComponent(jLabel65)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField43, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                         .addComponent(jButton33)
                         .addGap(18, 18, 18)
                         .addComponent(jButton32)))
                 .addContainerGap())
         );
         jDialog1Layout.setVerticalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 276, Short.MAX_VALUE)
             .addGroup(jDialog1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel67)
                     .addComponent(jTextField45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel66)
                     .addComponent(jTextField44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel65)
                     .addComponent(jTextField43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel64)
                     .addComponent(jTextField40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel63)
                     .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton33)
                     .addComponent(jButton32))
                 .addContainerGap())
         );
 
         jPanel16.setBackground(resourceMap.getColor("jPanel16.background")); // NOI18N
         jPanel16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
         jPanel16.setName("jPanel16"); // NOI18N
 
         jScrollPane16.setName("jScrollPane16"); // NOI18N
 
         jTextArea12.setColumns(20);
         jTextArea12.setEditable(false);
         jTextArea12.setRows(5);
         jTextArea12.setName("jTextArea12"); // NOI18N
         jScrollPane16.setViewportView(jTextArea12);
 
         jLabel68.setText(resourceMap.getString("jLabel68.text")); // NOI18N
         jLabel68.setName("jLabel68"); // NOI18N
 
         jLabel69.setText(resourceMap.getString("jLabel69.text")); // NOI18N
         jLabel69.setName("jLabel69"); // NOI18N
 
         jTextField46.setEditable(false);
         jTextField46.setName("jTextField46"); // NOI18N
 
         jTextField47.setEditable(false);
         jTextField47.setName("jTextField47"); // NOI18N
 
         jLabel70.setText(resourceMap.getString("jLabel70.text")); // NOI18N
         jLabel70.setName("jLabel70"); // NOI18N
 
         jTextField48.setEditable(false);
         jTextField48.setName("jTextField48"); // NOI18N
 
         jTextField49.setEditable(false);
         jTextField49.setName("jTextField49"); // NOI18N
 
         jLabel71.setText(resourceMap.getString("jLabel71.text")); // NOI18N
         jLabel71.setName("jLabel71"); // NOI18N
 
         jLabel72.setText(resourceMap.getString("jLabel72.text")); // NOI18N
         jLabel72.setName("jLabel72"); // NOI18N
 
         jToggleButton1.setName("jToggleButton1"); // NOI18N
         jToggleButton1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jToggleButton1MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
         jPanel16.setLayout(jPanel16Layout);
         jPanel16Layout.setHorizontalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel16Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel16Layout.createSequentialGroup()
                         .addComponent(jLabel72)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField49, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel71)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField48, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                     .addGroup(jPanel16Layout.createSequentialGroup()
                         .addComponent(jLabel68)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                     .addGroup(jPanel16Layout.createSequentialGroup()
                         .addComponent(jLabel69)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField46, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                         .addComponent(jLabel70)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField47, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
                     .addComponent(jToggleButton1, javax.swing.GroupLayout.Alignment.TRAILING))
                 .addContainerGap())
         );
         jPanel16Layout.setVerticalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel16Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel72)
                     .addComponent(jTextField49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel71)
                     .addComponent(jTextField48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel70)
                     .addComponent(jTextField47, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel69)
                     .addComponent(jTextField46, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel68)
                     .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jToggleButton1)
                 .addGap(18, 18, 18))
         );
 
         jPanel17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         jPanel17.setName("jPanel17"); // NOI18N
 
         jLabel73.setFont(resourceMap.getFont("jLabel73.font")); // NOI18N
         jLabel73.setText(resourceMap.getString("jLabel73.text")); // NOI18N
         jLabel73.setName("jLabel73"); // NOI18N
 
         jLabel74.setText(resourceMap.getString("jLabel74.text")); // NOI18N
         jLabel74.setName("jLabel74"); // NOI18N
 
         jLabel75.setFont(resourceMap.getFont("jLabel75.font")); // NOI18N
         jLabel75.setText(resourceMap.getString("jLabel75.text")); // NOI18N
         jLabel75.setName("jLabel75"); // NOI18N
 
         jButton37.setText(resourceMap.getString("jButton37.text")); // NOI18N
         jButton37.setName("jButton37"); // NOI18N
         jButton37.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton37MouseClicked(evt);
             }
         });
 
         jButton38.setText(resourceMap.getString("jButton38.text")); // NOI18N
         jButton38.setName("jButton38"); // NOI18N
         jButton38.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton38MouseClicked(evt);
             }
         });
 
         jLabel76.setFont(resourceMap.getFont("jLabel76.font")); // NOI18N
         jLabel76.setText(resourceMap.getString("jLabel76.text")); // NOI18N
         jLabel76.setName("jLabel76"); // NOI18N
 
         jTextField50.setText(resourceMap.getString("jTextField50.text")); // NOI18N
         jTextField50.setName("jTextField50"); // NOI18N
 
         jButton36.setText(resourceMap.getString("jButton36.text")); // NOI18N
         jButton36.setName("jButton36"); // NOI18N
         jButton36.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton36MouseClicked(evt);
             }
         });
 
         jTextField51.setText(resourceMap.getString("jTextField51.text")); // NOI18N
         jTextField51.setName("jTextField51"); // NOI18N
 
         jLabel77.setText(resourceMap.getString("jLabel77.text")); // NOI18N
         jLabel77.setName("jLabel77"); // NOI18N
 
         jButton39.setText(resourceMap.getString("jButton39.text")); // NOI18N
         jButton39.setName("jButton39"); // NOI18N
         jButton39.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton39MouseClicked(evt);
             }
         });
 
         jButton40.setIcon(resourceMap.getIcon("jButton40.icon")); // NOI18N
         jButton40.setText(resourceMap.getString("jButton40.text")); // NOI18N
         jButton40.setToolTipText(resourceMap.getString("jButton40.toolTipText")); // NOI18N
         jButton40.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         jButton40.setName("jButton40"); // NOI18N
         jButton40.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton40MouseClicked(evt);
             }
         });
 
         jButton42.setIcon(resourceMap.getIcon("jButton42.icon")); // NOI18N
         jButton42.setText(resourceMap.getString("jButton42.text")); // NOI18N
         jButton42.setToolTipText(resourceMap.getString("jButton42.toolTipText")); // NOI18N
         jButton42.setName("jButton42"); // NOI18N
         jButton42.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton42MouseClicked(evt);
             }
         });
 
         jButton43.setIcon(resourceMap.getIcon("jButton43.icon")); // NOI18N
         jButton43.setText(resourceMap.getString("jButton43.text")); // NOI18N
         jButton43.setToolTipText(resourceMap.getString("jButton43.toolTipText")); // NOI18N
         jButton43.setName("jButton43"); // NOI18N
         jButton43.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton43MouseClicked(evt);
             }
         });
 
         jButton44.setIcon(resourceMap.getIcon("jButton44.icon")); // NOI18N
         jButton44.setToolTipText(resourceMap.getString("jButton44.toolTipText")); // NOI18N
         jButton44.setAlignmentY(0.0F);
         jButton44.setName("jButton44"); // NOI18N
         jButton44.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton44MouseClicked(evt);
             }
         });
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         jTable6.setAutoCreateRowSorter(true);
         jTable6.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Codice Articolo", "Nome Generico", "Quantità Disponibile"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTable6.setName("jTable6"); // NOI18N
         jScrollPane1.setViewportView(jTable6);
 
         jScrollPane6.setName("jScrollPane6"); // NOI18N
 
         jTable7.setAutoCreateRowSorter(true);
         jTable7.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Codice Articolo", "Nome Generico", "Quantità Utilizzata"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTable7.setName("jTable7"); // NOI18N
         jScrollPane6.setViewportView(jTable7);
 
         javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
         jPanel17.setLayout(jPanel17Layout);
         jPanel17Layout.setHorizontalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel17Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel17Layout.createSequentialGroup()
                                 .addComponent(jLabel74)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField50, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jButton36, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                             .addGroup(jPanel17Layout.createSequentialGroup()
                                 .addComponent(jButton38)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jButton37))
                             .addGroup(jPanel17Layout.createSequentialGroup()
                                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
                                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(jPanel17Layout.createSequentialGroup()
                                         .addGap(10, 10, 10)
                                         .addComponent(jLabel77)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addComponent(jTextField51, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGroup(jPanel17Layout.createSequentialGroup()
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                         .addGap(17, 17, 17))
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel73)
                             .addGroup(jPanel17Layout.createSequentialGroup()
                                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(jPanel17Layout.createSequentialGroup()
                                         .addComponent(jLabel75)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jLabel76))
                                     .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(jButton43, 0, 0, Short.MAX_VALUE)
                                     .addComponent(jButton42, 0, 0, Short.MAX_VALUE)
                                     .addComponent(jButton44, 0, 0, Short.MAX_VALUE)
                                     .addComponent(jButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))
                         .addContainerGap(176, Short.MAX_VALUE))))
         );
         jPanel17Layout.setVerticalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel17Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel74)
                     .addComponent(jTextField50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton36))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addGap(6, 6, 6)
                         .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel77)
                             .addComponent(jTextField51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                 .addComponent(jLabel73)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addComponent(jButton40)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton42)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton43))
                     .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addGap(32, 32, 32)
                         .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jButton38)
                             .addComponent(jButton37)))
                     .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel75)
                         .addComponent(jLabel76)))
                 .addContainerGap())
         );
 
         jTextField51.setDocument(justNumbers);
 
         jPanel18.setBackground(resourceMap.getColor("jPanel18.background")); // NOI18N
         jPanel18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         jPanel18.setName("jPanel18"); // NOI18N
 
         jLabel81.setText(resourceMap.getString("jLabel81.text")); // NOI18N
         jLabel81.setName("jLabel81"); // NOI18N
 
         jTextField55.setEditable(false);
         jTextField55.setName("jTextField55"); // NOI18N
 
         jScrollPane19.setName("jScrollPane19"); // NOI18N
 
         jTable4.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Cognome", "Nome", "Imei-S/N", "stato lavorazione"
             }
         ));
         jTable4.setName("jTable4"); // NOI18N
         jTable4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable4MouseClicked(evt);
             }
         });
         jScrollPane19.setViewportView(jTable4);
         DefaultTableModel model4 = new DefaultTableModel(){
             private static final long serialVersionUID = 1L;
             public boolean isCellEditable(int row, int column){
                 return false;
             }
 
         };
         String[][] data4 = new String[][]{
             {null, null, null, null},
             {null, null, null, null},
             {null, null, null, null},
             {null, null, null, null}
         };
         String[] columnNames4 = new String[]{"Cognome", "Nome", "Imei-S/N", "stato lavorazione"};
         model4.setDataVector(data4, columnNames4);
         jTable4.setModel(model4);
 
         jLabel78.setFont(resourceMap.getFont("jLabel78.font")); // NOI18N
         jLabel78.setForeground(resourceMap.getColor("jLabel78.foreground")); // NOI18N
         jLabel78.setText(resourceMap.getString("jLabel78.text")); // NOI18N
         jLabel78.setName("jLabel78"); // NOI18N
 
         javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
         jPanel18.setLayout(jPanel18Layout);
         jPanel18Layout.setHorizontalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel18Layout.createSequentialGroup()
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 641, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addGap(80, 80, 80)
                         .addComponent(jLabel81)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField55, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel78)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel18Layout.setVerticalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel18Layout.createSequentialGroup()
                 .addGap(18, 18, 18)
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel81)
                     .addComponent(jTextField55, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel78))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel4.setName("jPanel4"); // NOI18N
 
         jFormattedTextField1.setText(resourceMap.getString("jFormattedTextField1.text")); // NOI18N
         jFormattedTextField1.setName("jFormattedTextField1"); // NOI18N
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(224, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(210, Short.MAX_VALUE))
         );
 
         jFormattedTextField1.setDocument(justNumbers);
 
         setComponent(mainPanel);
         setMenuBar(menuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     // surname search field into second panel
 private void jTextField1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField1MouseClicked
     if(jTextField1.getText().equals("Cognome"))
             jTextField1.setText("");
     if(jTextField2.getText().equals("Nome"))
             jTextField2.setText("");
     
 }//GEN-LAST:event_jTextField1MouseClicked
 
 // name search field into second panel
 private void jTextField2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField2MouseClicked
     if(jTextField2.getText().equals("Nome"))
         jTextField2.setText("");
     if(jTextField1.getText().equals("Cognome"))
             jTextField1.setText("");
 }//GEN-LAST:event_jTextField2MouseClicked
 
 // reset button into second panel search
 private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
     jTextField1.setText("Cognome");
     jTextField2.setText("Nome");
     jTable5.setVisible(false);
 }//GEN-LAST:event_jButton4MouseClicked
 
 // customer SELECT (second panel)
 private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
     
     String surname = jTextField1.getText();
     String name = jTextField2.getText();
         
     if(!checkCustomerSearch(surname, name)) {                               // info not inserted properly
         showWinAlert(jPanel8, "Valori errati, riprovare...", "Error", JOptionPane.ERROR_MESSAGE);
         jTable5.setVisible(false);
     } else {                                                                // search can be executed
         this.customerSearchResult(name, surname);                           // execute the operation and 
             
         if(this.customerRet != null) {
             setTableCustomerData(jTable5, this.customerRet);
             jTable5.setVisible(true);
         } else {
             showWinAlert(jPanel8, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
             jTable5.setVisible(false);
         }
         
     }
 }//GEN-LAST:event_jButton3MouseClicked
 
     private void jTextField3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField3MouseClicked
         jTextField3.setText("");
     }//GEN-LAST:event_jTextField3MouseClicked
 
     // warehouse search reset
     private void jButton8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton8MouseClicked
         jTextField3.setText("Codice Articolo");
         jTable1.setVisible(false);
     }//GEN-LAST:event_jButton8MouseClicked
 
     private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
         jPanel5.setVisible(true);
         jPanel6.setVisible(false);
         jTextField3.setText("Codice Articolo");
         jTable1.setVisible(false);
     }//GEN-LAST:event_jButton6MouseClicked
 
     // warehouse SELECT
     private void jButton7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton7MouseClicked
         
         String serial = jTextField3.getText();
         
         if(!checkSparePartSerial(serial))
             showWinAlert(jPanel5, "Inserire un codice Articolo valido.", "Error", JOptionPane.ERROR_MESSAGE);
         else {
             this.warehouseSearchResult(serial);
             
             if(this.warehouseRet != null) {
                 setTableWarehouseData(jTable1, this.warehouseRet);
                 jTable1.setVisible(true);                                       // this was out of the if/else
             } else {
                 showWinAlert(jPanel8, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 jTable1.setVisible(false);
             }
         }
         
     }//GEN-LAST:event_jButton7MouseClicked
 
     private boolean checkSparePartSerial (String s) {
         
         if(s.equals("") || s.equals("Codice Articolo"))
             return false;
         
         return true;
         
     }
     
     private void warehouseSearchResult (String a) {
         
         SharedClasses.Warehouse w = new SharedClasses.Warehouse(a);
         ComClasses.Request r = new ComClasses.Request(w, ComClasses.Constants.WARE, ComClasses.Constants.SELECT, SharedClasses.Warehouse.select());
         
         try {
             this.warehouseRet = Utils.arrayOperation(r);
         } catch (Exception e) {
             showWinAlert(jPanel8, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // set warehouse data into jTable
     public void setTableWarehouseData (JTable t, ArrayList<Object> a) {
         
         int n = a.size();
         SharedClasses.Warehouse w = null;
         setJTableWarehouse(t, n);
         
         for(int i = 0; i < n; i++) {                    // take the infos from every warehouse object
             w = (SharedClasses.Warehouse) a.get(i);
             t.setValueAt(w.getSerial(), i, 0);
             t.setValueAt(w.getName(), i, 1);
             t.setValueAt(w.getAvailability(), i, 2);
             t.setValueAt(w.getNote(), i, 3);
         }
         
     }
     
     //aggiungi nuovo articolo magazzino
     private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
         jPanel5.setVisible(false);
         jPanel6.setVisible(true);
         resetValArticleWarehouse();
         
     }//GEN-LAST:event_jButton5MouseClicked
 
     //tasto reset valori articolo magazzino
     private void jButton10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton10MouseClicked
         resetValArticleWarehouse();
         
     }//GEN-LAST:event_jButton10MouseClicked
 
     // cambia stato di accessorio
     private void jCheckBox2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox2StateChanged
         if(jCheckBox2.isSelected())
             jTextField26.setEditable(true);
         else{
             jTextField26.setEditable(false);
             jTextField26.setText(null);
         }
     }//GEN-LAST:event_jCheckBox2StateChanged
 
      // esci e resetta valori 
     private void jButton17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton17MouseClicked
        
         flagCliente=false;
         resetValScheda();
         
         jPanel9.setVisible(false);
                
     }//GEN-LAST:event_jButton17MouseClicked
 
     // repair INSERT
     private void jButton18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton18MouseClicked
         
         int flagError = 0;
         /*
         if(flagCliente == false){
             showWinAlert(jPanel9, "Dati cliente errati.", "Errore dati incompleti", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }*/
         if(jComboBox3.getSelectedIndex() == 0){
             showWinAlert(jPanel9, "Selezionare una tipologia.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField24.getText().equals("")){
             showWinAlert(jPanel9, "Inserire un modello del prodotto.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField25.getText().equals("")){
             showWinAlert(jPanel9, "Manca Imei/Serial Number.", "Errore", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         if(jCheckBox2.isSelected())
             if(jTextField26.getText().equals("")){
                 showWinAlert(jPanel9, "Controllare casella accessori.", "Warning", JOptionPane.WARNING_MESSAGE);
                 flagError++;
             }
         if(jTextArea5.getText().equals("")){
             showWinAlert(jPanel9, "Inserire una descrizione del difetto.", "Errore", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){
             
             this.handleDevice();                                // do proper operations on device info
 
             SharedClasses.Repair rep = new SharedClasses.Repair(this.c, this.d, jTextField33.getText(), jTextField26.getText());
             ComClasses.Request req = new ComClasses.Request(rep, ComClasses.Constants.REPAIR, ComClasses.Constants.INSERT, rep.insert());
 
             try {
                 // TODO exception if the value from the server is an exception
                 int id = Utils.intOperation(req).intValue();
                 rep.setID(id);
 
                 SharedClasses.Details det = new SharedClasses.Details(rep, jTextArea5.getText());        // create Details object
                 req = new ComClasses.Request(det, ComClasses.Constants.DETAILS, ComClasses.Constants.INSERT, det.insert());
                 // TODO manage return value    
                 Utils.intOperation(req);                    // the INSERT for Repair and Details will become effective at the same time
             
                 flagCliente = false;
             
                 int n = JOptionPane.showConfirmDialog(jPanel9, "Scheda prodotto n° " + id + "\nStampare la ricevuta?", "Nuova Scheda prodotto aperta", JOptionPane.YES_NO_OPTION);
             
                 if(n == JOptionPane.YES_OPTION) {
                     // TODO print on paper
                     Print.repairPrint(id, this.c, rep, this.d, det);
                 } else {
                     // do nothing
                 }
                 
             } catch (Exception e) {
                 showWinAlert(jPanel9, Client.Utils.exceptionMessage(e), "Errore", JOptionPane.ERROR_MESSAGE);
             }
             
             this.resetObjects();
             resetValScheda();
             jPanel9.setVisible(false);
         }
         
     }//GEN-LAST:event_jButton18MouseClicked
 
     // do proper device operations when inserting a new repair
     private void handleDevice () {
         
         int type = jComboBox3.getSelectedIndex();
         this.d = new SharedClasses.Device(jTextField24.getText(), type, jTextField25.getText());
         ComClasses.Request r = new ComClasses.Request(this.d, ComClasses.Constants.DEVICE, ComClasses.Constants.INSERT, this.d.insert());
         
         try {
             // TODO exception if the value from the server is an exception
             int v = Utils.intOperation(r).intValue();
         
             if(v == ComClasses.Constants.RET_EXI) {             // the device already exists: it's necessary to get the id
                 r = new ComClasses.Request(this.d, ComClasses.Constants.DEVICE, ComClasses.Constants.FIELDSELECT, this.d.selectSerial());
                 v = Utils.intOperation(r).intValue();
 
                 if(v == ComClasses.Constants.RET_EXC)           // exception occourred
                     showWinAlert(jPanel8, "Eccezione occorsa durante l'operazione: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 else
                     this.d.setID(v);                            // v now contains the existing id
 
             } else {
                 this.d.setID(v);                                // v already contains the generated id
             }
         } catch (Exception e) {
             showWinAlert(jPanel8, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // set to null the objects used for the operations
     private void resetObjects () {
         this.c = null;
         this.d = null;
     }
     
      // preimpostazioni crea pratica //
     private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        
         jPanel9.setVisible(true);
         jPanel10.setVisible(false);
         //dati cliente
         jTextField19.setEditable(false);
         jTextField20.setEditable(false);
         //date
         jTextField21.setEnabled(false);
         jTextField22.setEditable(false);
         jTextField23.setEditable(false);
         //accessori
         jCheckBox2.setSelected(false);
         jTextField26.setEditable(false);
         //type
         jComboBox3.setSelectedIndex(0);
         //stato lav
         jComboBox4.setSelectedIndex(0);
         jComboBox4.setEnabled(false);
         //problematiche
         jTextArea6.setEditable(false);
         jTextArea7.setEditable(false);
         //orario
         
         jTextField21.setText(getDataOra());
         //fine setting//
         
         
     }//GEN-LAST:event_jButton1MouseClicked
 
     //preimpo cerca pratica
     private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
         
         jPanel9.setVisible(false);
         jPanel10.setVisible(true);
         //jScrollPane5.setVisible(false);
         jTable3.setVisible(false);
         jTextField27.setText(null);
         jTextField28.setText(null);
         jTextField29.setText(null);
         jTextField30.setText(null);
         
     }//GEN-LAST:event_jButton2MouseClicked
 
     // repair SELECT window reset
     private void jButton11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton11MouseClicked
 
         jTable3.setVisible(false);
         jTextField27.setText("");
         jTextField28.setText("");
         jTextField29.setText("");
         jTextField30.setText("");
         this.repairRet = null;
     }//GEN-LAST:event_jButton11MouseClicked
 
     // repair SELECT
     private void jButton12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton12MouseClicked
 
         if(!checkInsertion(jTextField27, jTextField28, jTextField29, jTextField30)) {
             showWinAlert(jPanel10, "Inserire almeno un campo.", "Error", JOptionPane.ERROR_MESSAGE);
             jTable3.setVisible(false);
         } else {
             this.repairSearchResult();
             
             if(this.repairRet != null) {
                 setTableRepairData(jTable3, this.repairRet);
                 jTable3.setVisible(true);                                       
             } else {
                 showWinAlert(jPanel8, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 jTable3.setVisible(false);
             }
              
         }
        
     }//GEN-LAST:event_jButton12MouseClicked
    
     private void repairSearchResult () {
         // parameters are: name, surname, repair id, imei (serial number)
         SharedClasses.RepairRequest rs = new SharedClasses.RepairRequest(jTextField29.getText(), jTextField28.getText(), jTextField27.getText(), jTextField30.getText());
         ComClasses.Request r = new ComClasses.Request(rs, ComClasses.Constants.REPSEL, ComClasses.Constants.SELECT, rs.select());
         
         try {
             this.repairRet = Utils.arrayOperation(r);
         } catch (Exception e) {
             showWinAlert(jPanel8, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
     
     }
     
     // insert repair details into jtable
     private static void setTableRepairData(JTable t, ArrayList<Object> a) {
         
         int n = a.size();
         SharedClasses.RepairResponse r = null;
         setJTableRepair(t, n);
         
         for(int i = 0; i < n; i++) {                        // take the infos for every search result
             r = (SharedClasses.RepairResponse) a.get(i);
             t.setValueAt(r.getRepair().getID(), i, 0);
             t.setValueAt(r.getOwner().getSurname(), i, 1);
             t.setValueAt(r.getOwner().getName(), i, 2);
             t.setValueAt(r.getDevice().getIdentification(), i, 3);
             t.setValueAt(repairStatus(r.getRepair().getStatus()), i, 4);
         }
         
     }
     
     // return the string corresponding to repair status
     private static String repairStatus (int i) {
         
         switch(i) {
             
             case 0:
                 return "IN ACCETTAZIONE";
                 
             case 1:
                 return "IN LAVORAZIONE";
                 
             case 2:
                 return "ATTESA PEZZI";
                 
             case 3:
                 return "LV SUP";
                 
             case 4:
                 return "LAV TERMINATA";
                 
             case 5:
                 return "RESTITUITO";
                 
             case 6:
                 return "PREV NON ACCETTATO";
                 
             case 7:
                 return "RIP NON CONVENIENTE";
             
             default:
                 return "NON RICONOSCIUTO";
                 
         }
         
     }
     
     
     // check if any of the fields is not empty (useful for search)
     private static boolean checkInsertion (JTextField ... f) {
         
         for(int i = 0; i < f.length; i++)
             if(!f[i].getText().equals(""))      // if one field is not empty, it's ok
                 return true;
         
         return false;                           // all fields are empty
         
     }
     
     // check if every field is not empty
     private static boolean neededInsertion (JTextField ... f) {
         
         for(int i = 0; i < f.length; i++)
             if(f[i].getText().equals(""))
                 return false;
         
         return true;
         
     }
     
        //close the window e reset val
     // customer INSERT
     // customer SELECT surname field
     private void jTextField11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField11MouseClicked
         if(jTextField11.getText().equals("Cognome"))
             jTextField11.setText("");
         if(jTextField12.getText().equals("Nome"))
             jTextField12.setText("");
     }//GEN-LAST:event_jTextField11MouseClicked
 
     // customer SELECT name field
     private void jTextField12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField12MouseClicked
         if(jTextField12.getText().equals("Nome"))
             jTextField12.setText("");
         if(jTextField11.getText().equals("Cognome"))
             jTextField11.setText("");
     }//GEN-LAST:event_jTextField12MouseClicked
 
     // customer SELECT
     private void jButton20MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton20MouseClicked
         
         String surname = jTextField11.getText();
         String name = jTextField12.getText();
         
         if(!checkCustomerSearch(surname, name)) {                               // info not inserted properly
             showWinAlert(jPanel8, "Valori errati, riprovare...", "Error", JOptionPane.ERROR_MESSAGE);
             jTable2.setVisible(false);
         } else {                                                                // search can be executed
             this.customerSearchResult(name, surname);                           // execute the operation and 
             
             if(this.customerRet != null) {
                 setTableCustomerData(jTable2, this.customerRet);
                 jTable2.setVisible(true);                                       // this was out of the if/else
             } else {
                 showWinAlert(jPanel8, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 jTable2.setVisible(false);
             }
             
         }
     }//GEN-LAST:event_jButton20MouseClicked
 
     // check if the fields for customer search have been properly modified
     private static boolean checkCustomerSearch (String a, String b) {
         
         if(!a.equals("Cognome")) {                  // if a is equals to "Cognome" no fields have been edited
             if(!a.equals("") || !b.equals(""))      // if a or b are not equals to the empty string, it's ok
                 return true;
             
             return false;                           // a and b are equals to the empty string
         }
         return false;                               // fields have not been edited
     }
     
     // insert the customer data into jTable
     private static void setTableCustomerData (JTable t, ArrayList<Object> a) {
         
         int n = a.size();
         SharedClasses.Customer c = null;
         setJTableClient(t, n);
         
         for(int i = 0; i < n; i++) {                    // take infos from every customer object
             c = (SharedClasses.Customer) a.get(i);
             t.setValueAt(c.getSurname(), i, 0);
             t.setValueAt(c.getName(), i, 1);
             t.setValueAt(c.getAddress(), i, 2);
             t.setValueAt(c.getTelephone(), i, 3);
             t.setValueAt(c.getNote(), i, 4);
         }
         
     }
     
     // send the search request for Customer class and read the response
     private void customerSearchResult (String a, String b) {
         
         SharedClasses.Customer cus = new SharedClasses.Customer(a, b);
         ComClasses.Request r = new ComClasses.Request(cus, ComClasses.Constants.CUSTOMER, ComClasses.Constants.SELECT, cus.select());
         
         try {
             this.customerRet = Utils.arrayOperation(r);
         } catch (Exception e) {
             showWinAlert(jPanel8, Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // reset customer search field
     private void jButton21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton21MouseClicked
         jTextField11.setText("Cognome");
         jTextField12.setText("Nome");
         jTable2.setVisible(false);
         this.customerRet = null;
         this.c = null;
     }//GEN-LAST:event_jButton21MouseClicked
 
     private void jButton22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton22MouseClicked
         // annulla ed esci
         jTextField11.setText("Cognome");
         jTextField12.setText("Nome");
         
         flagCliente=false;
         cercaCliente.dispose();
         
     }//GEN-LAST:event_jButton22MouseClicked
 
      // cambia stato di accessorio
     private void jCheckBox3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox3StateChanged
        
         if(jCheckBox3.isSelected())
             jTextField38.setEditable(true);
         else{
             jTextField38.setEditable(false);
             jTextField38.setText(null);
         }
     }//GEN-LAST:event_jCheckBox3StateChanged
 
     // exit from Repair edit mode without make changes
     private void jButton25MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton25MouseClicked
 
         schedaProdotto.dispose();
         
     }//GEN-LAST:event_jButton25MouseClicked
 
     // repair UPDATE
     // TODO execute updates one by one and show alert if one doesn't succeed
     private void jButton26MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton26MouseClicked
 
         if(!neededInsertion(jTextField36, jTextField37)) {              // handle device operations
             showWinAlert(jPanel12, "Controllare modello ed imei", "Errore", JOptionPane.ERROR_MESSAGE);
         } else {
             SharedClasses.Device md = new SharedClasses.Device(jTextField36.getText(), jComboBox5.getSelectedIndex(), jTextField37.getText());
             if(this.checkDeviceChange(md)) {
                 this.updateDevice(md);
             }
         }
         /*
         SharedClasses.Details dd = this.de;
         dd.setDeclared(jTextArea8.getText());
         dd.setFound(jTextArea9.getText());
         dd.setDone(jTextArea10.getText());
         */
         /*
         jTextField38.getText();//accessori
         jTextArea8.getText();//dif dich
         jTextArea9.getText();//dif riscon
         jTextArea10.getText();//lav eff
         jComboBox6.getSelectedItem();//stato lavorazione
         jTextField34.getText();//data lav
         jTextField35.getText();//data out
         */
         schedaProdotto.dispose();
         jTable3.setVisible(false);
         jTextField27.setText("");
         jTextField28.setText("");
         jTextField29.setText("");
         jTextField30.setText("");
         
     }//GEN-LAST:event_jButton26MouseClicked
 
     // check if an update is needed for the device of the selected repair
     private boolean checkDeviceChange (SharedClasses.Device dev) {
         
         if(!this.d.getModel().equals(dev.getModel()))
             return true;
         
         if(!this.d.getIdentification().equals(dev.getIdentification()))
             return true;
         
         if(this.d.getType() != dev.getType())
             return true;
         
         return false;
         
     }
     
     private void updateDevice (SharedClasses.Device dev) {
        
         dev.setID(this.d.getID());
         ComClasses.Request req = new ComClasses.Request(dev, ComClasses.Constants.DEVICE, ComClasses.Constants.UPDATE, this.d.update(dev));
         
         try {
             // TODO exception if the value from the server is an exception
             Utils.intOperation(req);
         } catch (Exception e) {
             showWinAlert(jPanel12, Client.Utils.exceptionMessage(e), "Errore", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // open repair with details to edit
     private void jTable3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked
         
         if(evt.getClickCount() == 2){
             setCenterMonitorDim(935, 555);
             int i = jTable3.getSelectedRow();
             SharedClasses.RepairResponse rr = (SharedClasses.RepairResponse)repairRet.get(i);
             
             this.c = rr.getOwner();
             this.d = rr.getDevice();
             this.re = rr.getRepair();
             
             schedaProdotto = new FinestraSwing("Scheda riparazione n° " + this.re.getID(), p.getPX(), p.getPY(), 935, 555, jPanel12);
            jLabel44.setText(Integer.toString(this.re.getID()));
             
             getDataDbPracticeView();
             noEditablePracticeValue();
         }
     }//GEN-LAST:event_jTable3MouseClicked
 
     // customer UPDATE
     // warehouse INSERT
     private void jButton9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton9MouseClicked
       
         int flagError = 0;
         
         if(jTextField4.getText().equals("")){
             showWinAlert(jPanel6, "Inserire il codice articolo.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField6.getText().equals("")){
             showWinAlert(jPanel6, "Inserire la quantita' dell'articolo.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){
             setArticleWarehouseDB(jTextField4, jTextField5, jTextField6, jTextArea1);
             jPanel6.setVisible(false);
         }
         
     }//GEN-LAST:event_jButton9MouseClicked
 
     // warehouse UPDATE
     private void jButton28MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton28MouseClicked
         
         int flagError = 0;
         
         if(jTextField17.getText().equals("")){
             showWinAlert(jPanel14, "Inserire il codice articolo.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField39.getText().equals("")){
             showWinAlert(jPanel14, "Inserire la quantita' dell'articolo.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){
             this.updateSparePart();
             schedaArticoloMagazzino.dispose();
             jTextField3.setText("Codice Articolo");
             jTable1.setVisible(false);
         }
     }//GEN-LAST:event_jButton28MouseClicked
 
     private void updateSparePart () {
         
         SharedClasses.Warehouse w = new SharedClasses.Warehouse(this.sp.getSerial(), jTextField17.getText(), jTextField18.getText(), Integer.parseInt(jTextField39.getText()), jTextArea4.getText());
         ComClasses.Request req = new ComClasses.Request(w, ComClasses.Constants.WARE, ComClasses.Constants.UPDATE, this.sp.update(w));
    
         try {
             // TODO exception if the value from the server is an exception
             if(Utils.intOperation(req).intValue() != 1)
                 showWinAlert(jPanel7, "Modifica non riuscita. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
             this.sp = null;
         
         } catch (Exception e) {
             showWinAlert(jPanel7, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     private void jButton29MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton29MouseClicked
         // annulla ed esci senza salvare e/o effettuare modifiche all'articolo aperto
         schedaArticoloMagazzino.dispose();
     }//GEN-LAST:event_jButton29MouseClicked
 
     // open the spare part details to edit it
     private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
         // apertura articolo cercato
         if(evt.getClickCount() == 2){
             setCenterMonitorDim(465, 340);
             schedaArticoloMagazzino = new FinestraSwing("Scheda Articolo Magazzino", p.getPX(), p.getPY(), 465, 340, jPanel14);
             getValArticleWarehouse(jTextField17, jTextField18, jTextField39, jTextArea4);
         }
     }//GEN-LAST:event_jTable1MouseClicked
 
     private void jButton30MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton30MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton30MouseClicked
 
     private void jButton31MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton31MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton31MouseClicked
 
     private void jTextField41MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField41MouseClicked
         // cancellazione testo ip
         jTextField41.setText(null);
     }//GEN-LAST:event_jTextField41MouseClicked
 
     private void jButton27MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton27MouseClicked
         // creazione view dati cliente NON MODIFICABILI!
         setCenterMonitorDim(503, 300);
         DatiClienteSimply = new FinestraSwing("Dati Cliente", p.getPX(), p.getPY(), 503, 300, jPanel16);
         getDatiClienteDb(jTextField48, jTextField49, jTextField47, jTextField46, jTextArea12);
     }//GEN-LAST:event_jButton27MouseClicked
 
     // open usage edit window
     private void jButton35MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton35MouseClicked
         setCenterMonitorDim(720, 444);
         pezziUtilizzati = new FinestraSwing("Selezionare i pezzi utilizzati per la lavorazione!", p.getPX(), p.getPY(), 720, 444, jPanel17);
         this.cache = new ArrayList<SharedClasses.UsageCache>();
         this.old = new ArrayList<SharedClasses.UsageCache>();
         jTextField50.setText(null);
         jTextField51.setText(null);
         tableRowDelete((DefaultTableModel)jTable6.getModel());
         this.setUsageTable();
     }//GEN-LAST:event_jButton35MouseClicked
 
     private void setUsageTable () {
         
         SharedClasses.Usage u = new SharedClasses.Usage(this.re);
         DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
         
         ComClasses.Request r = new ComClasses.Request(u, ComClasses.Constants.USAGE, ComClasses.Constants.SELECT, SharedClasses.Usage.select());
         
         try {
 
             tableRowDelete(model);
             
             this.usageRet = Utils.arrayOperation(r);
             int n = this.usageRet.size();
 
             for(int i = 0; i < n; i += 2) {                         // i + 1  contains the name of the spare part
 
                 u = (SharedClasses.Usage) this.usageRet.get(i);
                 this.old.add(new SharedClasses.UsageCache(u));
                 model.addRow(new Object[]{u.getSerial(), (String)this.usageRet.get(i+1), u.getUsed()});
 
             }
             
         } catch (Exception e) {
             showWinAlert(jPanel17, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
 
     }
     
     // delete rows from a table
     private static void tableRowDelete (DefaultTableModel model) {
         
         int o = model.getRowCount();
             
             for(int i = 0; i < o; i++)
                 model.removeRow(0);                                 // delete the previous content
         
     }
     
     // warehouse SELECT into usage editing
     private void jButton36MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton36MouseClicked
         
         String ins = jTextField50.getText();
         
         if(ins.equals(""))
             showWinAlert(jPanel17, "Inserire un nome generico\noppure un codice articolo.", "Error Code", JOptionPane.ERROR_MESSAGE);
         else {
             SharedClasses.Warehouse aux = new SharedClasses.Warehouse(ins);
             ComClasses.Request wr = new ComClasses.Request(aux, ComClasses.Constants.WARE, ComClasses.Constants.SELECT, aux.selectSerialName());
             
             try {
                 this.warehouseRet = Utils.arrayOperation(wr);
                 setUsageWarehouseTable(jTable6, this.warehouseRet);
             } catch (Exception e) {
                 showWinAlert(jPanel17, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
         
     }//GEN-LAST:event_jButton36MouseClicked
 
     private void setUsageWarehouseTable (JTable t, ArrayList<Object> a) {
         
         int n = a.size();
         int delta = 0;
         int x;
         String serial;
         SharedClasses.Warehouse w = null;
         setJTableUsageWarehouse(t, n);
         
         for(int i = 0; i < n; i++) {                    // take the infos from every warehouse object
             w = (SharedClasses.Warehouse) a.get(i);
             serial = w.getSerial();
             t.setValueAt(serial, i, 0);
             t.setValueAt(w.getName(), i, 1);
             
             x = this.cacheFindSparePart(serial);
             if(x != -1)                                 // spare part usage modifed
                 delta = this.cache.get(x).getDelta();
             else                                        // spare part usage not modified
                 delta = 0;
             
             t.setValueAt(w.getAvailability() - delta, i, 2);
         }
         
     }
     
     private void jButton41MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton41MouseClicked
         // reset valori scheda accettazione
         flagCliente=false;
         resetValScheda();
     }//GEN-LAST:event_jButton41MouseClicked
 
     // add spare part to Usage
     private void jButton39MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton39MouseClicked
         // importa pezzo di ricambio su scheda
         int sel = jTable6.getSelectedRow();
         String req = jTextField51.getText();
         
         if(sel != -1){
             
             if(req.equals(""))                          // no quantity has been inserted
                 showWinAlert(jPanel17, "Inserire una quantità.", "Warning", JOptionPane.WARNING_MESSAGE);
             
             if(checkQuantity(req, jTable6, sel))                    // check if the quantity inserted it's ok
                 importArticle(sel, req);                            // do something to handle the situation
             else
                 showWinAlert(jPanel17, "Quantità pezzo disponibile insufficiente o quantità richiesta non corretta.", "Error", JOptionPane.ERROR_MESSAGE);
                    
         } else
             showWinAlert(jPanel17, "Selezionare un pezzo prima di importalo.", "Warning Selection", JOptionPane.WARNING_MESSAGE);
     }//GEN-LAST:event_jButton39MouseClicked
 
     // check the requested quantity for a specific spare part
     private boolean checkQuantity (String s, JTable t, int r) {
         
         int req = Integer.parseInt(s);
         int ava = (Integer)t.getValueAt(r, 2);
         
         if(req <= 0)             // requested quantity is negative or equal to 0
             return false;
         
         if(req > ava)           // requested quantity is greater than availability
             return false;
         
         return true;
         
     }
     
     // increase usage of a specific spare part
     private void jButton40MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton40MouseClicked
         
         int sel = jTable7.getSelectedRow();
         
         if(sel != -1){
             
             String serial = (String)jTable7.getValueAt(sel, 0);
             int i = this.cacheFindSparePart(serial);
             
             SharedClasses.Warehouse aux = new SharedClasses.Warehouse(serial);
             ComClasses.Request r = new ComClasses.Request(aux, ComClasses.Constants.WARE, ComClasses.Constants.FIELDSELECT, SharedClasses.Warehouse.availabilitySelect());
             
             try {
                 // TODO exception if the value from the server is an exception
                 int w = Utils.intOperation(r);
 
                 if(i != -1)
                     w -= this.cache.get(i).getDelta();
 
                 if(w < 1) {
 
                     showWinAlert(jPanel17, "Impossibile aggiungere pezzo: quantità non sufficiente.", "Warning Selection", JOptionPane.WARNING_MESSAGE);
 
                 } else {
 
                     if(i != -1)
                         this.cache.get(i).increaseDelta();
                     else
                         this.cache.add(new SharedClasses.UsageCache(serial, 1));
 
                     int v = (Integer)jTable7.getValueAt(sel, 2);
 
                     jTable7.setValueAt(v + 1, sel, 2);
 
                 }
                 
             } catch (Exception e) {
                 showWinAlert(jPanel17, Client.Utils.exceptionMessage(e), "Warning Selection", JOptionPane.WARNING_MESSAGE);
             }
             
         } else 
             showWinAlert(jPanel17, "Selezionare prima un pezzo\nper aumentarne la quantità!", "Warning Selection", JOptionPane.WARNING_MESSAGE);
         
         
     }//GEN-LAST:event_jButton40MouseClicked
 
     // decrease usage of a specific spare part
     private void jButton44MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton44MouseClicked
        
         int sel = jTable7.getSelectedRow();
         
         if(sel != -1){
             
             int v = (Integer)jTable7.getValueAt(sel, 2);
             
             if(v - 1 > 0) {
             
                 String serial = (String)jTable7.getValueAt(sel, 0);
                 int i = this.cacheFindSparePart(serial);
 
                 if(i != -1)
                     this.cache.get(i).decreaseDelta();
                 else
                     this.cache.add(new SharedClasses.UsageCache(serial, -1));
 
                 jTable7.setValueAt(v - 1, sel, 2);
 
             }
             
         } else
             showWinAlert(jPanel17, "Selezionare prima un pezzo\nper diminuirne la quantità!", "Warning Selection", JOptionPane.WARNING_MESSAGE);
         
     }//GEN-LAST:event_jButton44MouseClicked
 
     // delete a spare part from usage
     private void jButton42MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton42MouseClicked
 
         int sel = jTable7.getSelectedRow();
         
          if(sel != -1){
   
             int n = JOptionPane.showConfirmDialog(jPanel17, "Cancellare il pezzo selezionato?", "Cancellazione info", JOptionPane.YES_NO_OPTION);
             
             if(n == JOptionPane.YES_OPTION) {
                 
                 String serial = (String)jTable7.getValueAt(sel, 0);
                 int i = this.cacheFindSparePart(serial);
                 int v = (Integer)jTable7.getValueAt(sel, 2);
                 
                 if(i != -1)
                     this.cache.get(i).decreaseDelta(v);
                 else
                     this.cache.add(new SharedClasses.UsageCache(serial, -v));
                 
                 DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
                 model.removeRow(sel);
             
             } else {
                 // do nothing
             }
             
         } else
             showWinAlert(jPanel17, "Selezionare prima un pezzo\nper cancellarlo dalla lista!", "Warning Selection", JOptionPane.WARNING_MESSAGE);
         
     }//GEN-LAST:event_jButton42MouseClicked
 
     // delete all spare part from usage
     private void jButton43MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton43MouseClicked
 
             int n = JOptionPane.showConfirmDialog(jPanel17, "Cancellare tutti i pezzi\nimpostati per questa lavorazione?", "Cancellazione info", JOptionPane.YES_NO_OPTION);
             
             if(n == JOptionPane.YES_OPTION){
             
                 int r = jTable7.getRowCount();
                 
                 if(r > 0) {
                 
                     DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
                     
                     for(int i = 0; i < r; i++)
                         model.removeRow(0);
                     
                 }
                 
             } else {
                 // do nothing
             }
                 
   
     }//GEN-LAST:event_jButton43MouseClicked
 
     // check if the usage of a spare part has been already modified
     private int cacheFindSparePart (String serial) {
             
         int n = this.cache.size();
 
         if(n > 0) {
 
             for(int i = 0; i < n; i++)
                 if(this.cache.get(i).getSerial().equals(serial))
                     return i;
 
         }
         
         return -1;
         
     }
     
     private void jButton38MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton38MouseClicked
         // salva i pezzi importati nella lista dei pezzi utilizzati durante la lavorazione
         // TODO delete old usage
         try {
             usageDelete();
             warehouseAdd();
             newUsage();
             warehouseUpdate();
             pezziUtilizzati.dispose();
         } catch (Exception e) {
             showWinAlert(jPanel17, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }//GEN-LAST:event_jButton38MouseClicked
 
     private void jButton37MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton37MouseClicked
         // esci dalla view imposta pezzi di ricambio usati senza salvare le modifiche apportate
         pezziUtilizzati.dispose();
     }//GEN-LAST:event_jButton37MouseClicked
 
     private void jButton45MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton45MouseClicked
         // controllo rientri imei
         setCenterMonitorDim(667, 355);
         gestioneRientri = new FinestraSwing("Lavorazioni già effettuate", p.getPX(), p.getPY(), 667, 355, jPanel18);
         if(controlloRietri()){
             jTable4.setVisible(true);
             jLabel78.setVisible(false);
         }else{
             jTable4.setVisible(false);
             jLabel78.setVisible(true);
         }
         toppy(gestioneRientri);
 
         
     }//GEN-LAST:event_jButton45MouseClicked
 
     private void jTable4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable4MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jTable4MouseClicked
 
     private void jButton46MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton46MouseClicked
         // preimpostazioni crea pratica //
         jPanel9.setVisible(true);
         jPanel10.setVisible(false);
         //dati cliente
         jTextField19.setEditable(false);
         jTextField20.setEditable(false);
         //date
         jTextField21.setEnabled(false);
         jTextField22.setEditable(false);
         jTextField23.setEditable(false);
         //accessori
         jCheckBox2.setSelected(false);
         jTextField26.setEditable(false);
         //type
         jComboBox3.setSelectedIndex(0);
         //stato lav
         jComboBox4.setSelectedIndex(0);
         jComboBox4.setEnabled(false);
         //problematiche
         jTextArea6.setEditable(false);
         jTextArea7.setEditable(false);
         //orario
         
         jTextField21.setText(getDataOra());
         //fine setting//
         
     }//GEN-LAST:event_jButton46MouseClicked
 
     private void jButton47MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton47MouseClicked
         //preimpo cerca pratica
         jPanel9.setVisible(false);
         jPanel10.setVisible(true);
         //jScrollPane5.setVisible(false);
         jTable3.setVisible(false);
         jTextField27.setText(null);
         jTextField28.setText(null);
         jTextField29.setText(null);
         jTextField30.setText(null);
     }//GEN-LAST:event_jButton47MouseClicked
 
     // select a customer from search results when creating a new repair
     private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
         
         if(evt.getClickCount() == 2){                                       //lol grosso trucco :D
             int sel = jTable2.getSelectedRow();                                     // take the index
             cercaCliente.dispose();
             this.c = (SharedClasses.Customer)this.customerRet.get(sel);             // obtain the Customer class object
             this.customerRet = null;                                                // reset the array with the search results
             
             jTextField19.setText(this.c.getSurname());                              // take customer's surname
             jTextField20.setText(this.c.getName());                                 // take customer's name
         }
     }//GEN-LAST:event_jTable2MouseClicked
 
     // open the edit window for customer
     private void jTable5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable5MouseClicked
 
         if(evt.getClickCount() == 2){
             int sel = jTable5.getSelectedRow();
             this.c = (SharedClasses.Customer)this.customerRet.get(sel);
             this.customerRet = null;
             
             setCenterMonitorDim(503, 300);
             DatiClienteView = new FinestraSwing("Scheda dati cliente", p.getPX(), p.getPY(), 503, 300, jPanel13);
             clearField(jTextField15, jTextField16, jTextField14, jTextField13);
             jTextArea3.setText(null);
             getDatiClienteDb(jTextField15, jTextField16, jTextField14, jTextField13, jTextArea3);
         }
     }//GEN-LAST:event_jTable5MouseClicked
 
     private void jButton49MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton49MouseClicked
         // Cerca cliente per creazione nuova scheda
         setCenterMonitorDim(834, 460);
         cercaCliente = new FinestraSwing("Inserisci i dati per avviare la ricerca...", p.getPX(), p.getPY(), 834, 460, jPanel8);
         jTextField11.setText("Cognome");
         jTextField12.setText("Nome");
         jTable2.setVisible(false);     
         toppy(cercaCliente);
     }//GEN-LAST:event_jButton49MouseClicked
 
     private void jButton48MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton48MouseClicked
         // aggiungi cliente
         setCenterMonitorDim(503, 300);
         DatiCliente = new FinestraSwing("Crea scheda dati cliente", p.getPX(), p.getPY(), 503, 300, jPanel7);
         toppy(DatiCliente);
         //mainPanel.enableInputMethods(false);
         //jButton15.setEnabled(false);
        // jButton16.setEnabled(false);
     }//GEN-LAST:event_jButton48MouseClicked
     
     //Costumer insert
     private void jButton16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton16MouseClicked
       int flagError = 0;
         
         // check if all required fields are filled
         if(jTextField7.getText().equals("")){
             showWinAlert(jPanel7, "Manca il Cognome.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         if(jTextField8.getText().equals("")){
             showWinAlert(jPanel7, "Manca il Nome.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         if(jTextField10.getText().equals("")){
             showWinAlert(jPanel7, "Manca un recapito telefonico.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){          // it's ok
             
             this.c = new SharedClasses.Customer(jTextField8.getText(), jTextField7.getText(), jTextField9.getText(), jTextField10.getText(), jTextArea2.getText());
             ComClasses.Request r = new ComClasses.Request(this.c, ComClasses.Constants.CUSTOMER, ComClasses.Constants.INSERT, this.c.insert());
             
             try {
                 // TODO exception if the value from the server is an exception
                 int v = Client.Utils.intOperation(r).intValue();
             
                 if(v > 0) {
                     this.c.setID(v);
                     
                     DatiCliente.dispose();
                     jTextField19.setText(this.c.getSurname());
                     jTextField20.setText(this.c.getName());
                     
                 } else if(v == ComClasses.Constants.RET_EXI) {
                     showWinAlert(jPanel7, "Utente già esistente.", "Error", JOptionPane.ERROR_MESSAGE);
                 } else if(v == ComClasses.Constants.RET_EXC) {
                     showWinAlert(jPanel7, "Eccezione durante l'inserimento del cliente. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 }
                     
             } catch (Exception e) {
                 showWinAlert(jPanel7, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
     }//GEN-LAST:event_jButton16MouseClicked
    
     //reset Costumer insert
     private void jButton15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton15MouseClicked
        resetDatiCliente();
         DatiCliente.dispose();
         //jButton15.setEnabled(false);
        // jButton16.setEnabled(false);
     }//GEN-LAST:event_jButton15MouseClicked
         
         
     // esci dal cliente senza salvare nulla
     private void jButton13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton13MouseClicked
         DatiClienteView.dispose();
     }//GEN-LAST:event_jButton13MouseClicked
 
     //costumer UPDATE
     private void jButton14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton14MouseClicked
        
         int flagError = 0;
         
         if(jTextField16.getText().equals("")){
             showWinAlert(jPanel13, "Manca il Cognome.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         if(jTextField15.getText().equals("")){
             showWinAlert(jPanel13, "Manca il Nome.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         if(jTextField13.getText().equals("")){
             showWinAlert(jPanel13, "Manca un recapito telefonico.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){
             setDatiClienteDb(jTextField15, jTextField16, jTextField14, jTextField13, jTextArea3);
             DatiClienteView.dispose();
         }
               
         jTextField1.setText("Cognome");
         jTextField2.setText("Nome");
         jTable5.setVisible(false);
     }//GEN-LAST:event_jButton14MouseClicked
 
     // chiudi finestra visione semplice dati cliente
     private void jToggleButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton1MouseClicked
         DatiClienteSimply.dispose();
     }//GEN-LAST:event_jToggleButton1MouseClicked
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton10;
     private javax.swing.JButton jButton11;
     private javax.swing.JButton jButton12;
     private javax.swing.JButton jButton13;
     private javax.swing.JButton jButton14;
     private javax.swing.JButton jButton15;
     private javax.swing.JButton jButton16;
     private javax.swing.JButton jButton17;
     private javax.swing.JButton jButton18;
     private javax.swing.JButton jButton19;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton20;
     private javax.swing.JButton jButton21;
     private javax.swing.JButton jButton22;
     private javax.swing.JButton jButton25;
     private javax.swing.JButton jButton26;
     private javax.swing.JButton jButton27;
     private javax.swing.JButton jButton28;
     private javax.swing.JButton jButton29;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton30;
     private javax.swing.JButton jButton31;
     private javax.swing.JButton jButton32;
     private javax.swing.JButton jButton33;
     private javax.swing.JButton jButton35;
     private javax.swing.JButton jButton36;
     private javax.swing.JButton jButton37;
     private javax.swing.JButton jButton38;
     private javax.swing.JButton jButton39;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton40;
     private javax.swing.JButton jButton41;
     private javax.swing.JButton jButton42;
     private javax.swing.JButton jButton43;
     private javax.swing.JButton jButton44;
     private javax.swing.JButton jButton45;
     private javax.swing.JButton jButton46;
     private javax.swing.JButton jButton47;
     private javax.swing.JButton jButton48;
     private javax.swing.JButton jButton49;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton7;
     private javax.swing.JButton jButton8;
     private javax.swing.JButton jButton9;
     private javax.swing.JCheckBox jCheckBox2;
     private javax.swing.JCheckBox jCheckBox3;
     private javax.swing.JComboBox jComboBox3;
     private javax.swing.JComboBox jComboBox4;
     private javax.swing.JComboBox jComboBox5;
     private javax.swing.JComboBox jComboBox6;
     private javax.swing.JDialog jDialog1;
     private javax.swing.JFormattedTextField jFormattedTextField1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel23;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel31;
     private javax.swing.JLabel jLabel32;
     private javax.swing.JLabel jLabel33;
     private javax.swing.JLabel jLabel34;
     private javax.swing.JLabel jLabel35;
     private javax.swing.JLabel jLabel36;
     private javax.swing.JLabel jLabel37;
     private javax.swing.JLabel jLabel38;
     private javax.swing.JLabel jLabel39;
     private javax.swing.JLabel jLabel40;
     private javax.swing.JLabel jLabel41;
     private javax.swing.JLabel jLabel42;
     private javax.swing.JLabel jLabel43;
     private javax.swing.JLabel jLabel44;
     private javax.swing.JLabel jLabel45;
     private javax.swing.JLabel jLabel46;
     private javax.swing.JLabel jLabel47;
     private javax.swing.JLabel jLabel48;
     private javax.swing.JLabel jLabel49;
     private javax.swing.JLabel jLabel50;
     private javax.swing.JLabel jLabel51;
     private javax.swing.JLabel jLabel52;
     private javax.swing.JLabel jLabel53;
     private javax.swing.JLabel jLabel54;
     private javax.swing.JLabel jLabel55;
     private javax.swing.JLabel jLabel56;
     private javax.swing.JLabel jLabel57;
     private javax.swing.JLabel jLabel58;
     private javax.swing.JLabel jLabel59;
     private javax.swing.JLabel jLabel60;
     private javax.swing.JLabel jLabel61;
     private javax.swing.JLabel jLabel62;
     private javax.swing.JLabel jLabel63;
     private javax.swing.JLabel jLabel64;
     private javax.swing.JLabel jLabel65;
     private javax.swing.JLabel jLabel66;
     private javax.swing.JLabel jLabel67;
     private javax.swing.JLabel jLabel68;
     private javax.swing.JLabel jLabel69;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel70;
     private javax.swing.JLabel jLabel71;
     private javax.swing.JLabel jLabel72;
     private javax.swing.JLabel jLabel73;
     private javax.swing.JLabel jLabel74;
     private javax.swing.JLabel jLabel75;
     private javax.swing.JLabel jLabel76;
     private javax.swing.JLabel jLabel77;
     private javax.swing.JLabel jLabel78;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel80;
     private javax.swing.JLabel jLabel81;
     private javax.swing.JLabel jLabel82;
     private javax.swing.JLabel jLabel83;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel15;
     private javax.swing.JPanel jPanel16;
     private javax.swing.JPanel jPanel17;
     private javax.swing.JPanel jPanel18;
     private javax.swing.JPanel jPanel19;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane10;
     private javax.swing.JScrollPane jScrollPane11;
     private javax.swing.JScrollPane jScrollPane12;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane14;
     private javax.swing.JScrollPane jScrollPane15;
     private javax.swing.JScrollPane jScrollPane16;
     private javax.swing.JScrollPane jScrollPane19;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane20;
     private javax.swing.JScrollPane jScrollPane21;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTable jTable1;
     private javax.swing.JTable jTable2;
     private javax.swing.JTable jTable3;
     private javax.swing.JTable jTable4;
     private javax.swing.JTable jTable5;
     private javax.swing.JTable jTable6;
     private javax.swing.JTable jTable7;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextArea jTextArea10;
     private javax.swing.JTextArea jTextArea11;
     private javax.swing.JTextArea jTextArea12;
     private javax.swing.JTextArea jTextArea2;
     private javax.swing.JTextArea jTextArea3;
     private javax.swing.JTextArea jTextArea4;
     private javax.swing.JTextArea jTextArea5;
     private javax.swing.JTextArea jTextArea6;
     private javax.swing.JTextArea jTextArea7;
     private javax.swing.JTextArea jTextArea8;
     private javax.swing.JTextArea jTextArea9;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField10;
     private javax.swing.JTextField jTextField11;
     private javax.swing.JTextField jTextField12;
     private javax.swing.JTextField jTextField13;
     private javax.swing.JTextField jTextField14;
     private javax.swing.JTextField jTextField15;
     private javax.swing.JTextField jTextField16;
     private javax.swing.JTextField jTextField17;
     private javax.swing.JTextField jTextField18;
     private javax.swing.JTextField jTextField19;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField20;
     private javax.swing.JTextField jTextField21;
     private javax.swing.JTextField jTextField22;
     private javax.swing.JTextField jTextField23;
     private javax.swing.JTextField jTextField24;
     private javax.swing.JTextField jTextField25;
     private javax.swing.JTextField jTextField26;
     private javax.swing.JTextField jTextField27;
     private javax.swing.JTextField jTextField28;
     private javax.swing.JTextField jTextField29;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JTextField jTextField30;
     private javax.swing.JTextField jTextField31;
     private javax.swing.JTextField jTextField32;
     private javax.swing.JTextField jTextField33;
     private javax.swing.JTextField jTextField34;
     private javax.swing.JTextField jTextField35;
     private javax.swing.JTextField jTextField36;
     private javax.swing.JTextField jTextField37;
     private javax.swing.JTextField jTextField38;
     private javax.swing.JTextField jTextField39;
     private javax.swing.JTextField jTextField4;
     private javax.swing.JTextField jTextField40;
     private javax.swing.JTextField jTextField41;
     private javax.swing.JTextField jTextField42;
     private javax.swing.JTextField jTextField43;
     private javax.swing.JTextField jTextField44;
     private javax.swing.JTextField jTextField45;
     private javax.swing.JTextField jTextField46;
     private javax.swing.JTextField jTextField47;
     private javax.swing.JTextField jTextField48;
     private javax.swing.JTextField jTextField49;
     private javax.swing.JTextField jTextField5;
     private javax.swing.JTextField jTextField50;
     private javax.swing.JTextField jTextField51;
     private javax.swing.JTextField jTextField55;
     private javax.swing.JTextField jTextField6;
     private javax.swing.JTextField jTextField7;
     private javax.swing.JTextField jTextField8;
     private javax.swing.JTextField jTextField9;
     private javax.swing.JToggleButton jToggleButton1;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     // End of variables declaration//GEN-END:variables
 
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
 
     private JDialog aboutBox;
     private JDialog settingBox;
     
     private void addArticoloMagazzino(){
         jTextField4.getText();
         jTextField5.getText();
         jTextField6.getText();
         jTextArea1.getText();
     
     }
     
     private void resetValScheda(){
         jComboBox3.setSelectedIndex(0);
         jTextField19.setText("Cognome");
         jTextField20.setText("Nome");
         jTextField24.setText(null);
         jTextField25.setText(null);
         jCheckBox2.setSelected(false);
         jTextField26.setText(null);
         jTextArea5.setText(null);
         
     }
     
     private String getDataOra(){
       
         Calendar calendar = new GregorianCalendar();
         int ore = calendar.get(Calendar.HOUR_OF_DAY);
         int minuti = calendar.get(Calendar.MINUTE);
         int giorno = calendar.get(Calendar.DAY_OF_MONTH);
         int mese = calendar.get(Calendar.MONTH);
         int anno = calendar.get(Calendar.YEAR);
 
         return lessThanTen(giorno) + "/" + lessThanTen(mese+1)  +"/" + anno + " " + lessThanTen(ore) + ":" + lessThanTen(minuti);
     }
     
     private static String lessThanTen (int i) {
         
         if(i < 10)
             return "0".concat(Integer.toString(i));
 
         return Integer.toString(i);
         
     }
     
     private void showWinAlert(Component cmp, Object o, String s, int i){
         JOptionPane.showMessageDialog(cmp, o, s, i);
     }
    
         
     private void setCenterMonitorDim (int w, int h){
         // Get the size of the screen
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         // Determine the new location of the window
         //int w = jP.getSize().width;
         //int h = jP.getSize().height;
         int x = (dim.width-w)/2;
         int y = (dim.height-h)/2;
         // set position for Moving window
         p.setPX(x);
         p.setPY(y);
     }
     
     // insert info into repair update window
     private void getDataDbPracticeView() {
         // TODO check some fields
         String optional = this.re.getOptional();
         
         jTextField31.setText(this.c.getSurname().toLowerCase());                                // customer' surname
         jTextField32.setText(this.c.getName().toLowerCase());                                   // customer's name
         
         jComboBox5.setSelectedIndex(this.d.getType());                                          // device type
         jTextField36.setText(this.d.getModel().toLowerCase());                                  // device model
         jTextField37.setText(this.d.getIdentification().toLowerCase());                         // device serial number
         
         if(optional != null) {                                                                  // optional
             jCheckBox3.setSelected(true);
             jTextField38.setText(optional.toLowerCase());
         }
         
         this.de = new SharedClasses.Details(this.re.getID());
         ComClasses.Request req = new ComClasses.Request(this.de, ComClasses.Constants.DETAILS, ComClasses.Constants.SELECT, this.de.select());
         
         try {
         
             ArrayList<Object> aux = Utils.arrayOperation(req);
 
             if(aux != null)
                 this.de = (SharedClasses.Details) aux.get(0);
 
             jTextArea8.setText(this.de.getDeclared().toLowerCase());                // defect declared
 
             String found = this.de.getFound();
             if(found != null)
                 jTextArea9.setText(found.toLowerCase());                            // defect found
 
             String done = this.de.getFound();
             if(done != null)
                 jTextArea10.setText(done.toLowerCase());                            // work done
             jTextField34.setText(this.de.getDateStart());                           // date start work
             
         } catch (Exception e) {
             showWinAlert(jPanel10, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
             
         jComboBox6.setSelectedIndex(this.re.getStatus());                       // repair status
         jTextField33.setText(this.re.getDateIn());                              // date in
         jTextField35.setText(this.re.getDateOut());                             // date out
     }
     
     private void noEditablePracticeValue(){
         jTextField31.setEditable(false);
         jTextField32.setEditable(false);
         jComboBox5.setEditable(false);
         jTextField33.setEditable(false);
     }
     
     // TODO change this method using Repair and Details classes - maybe this method in not useful
     private void setDataDbPracticeCreate(){
         jTextField19.getText();         //cognome
         jTextField20.getText();         //nome
         jComboBox3.getSelectedItem();   //tipologia prodotto
         jTextField24.getText();         //modello
         jTextField25.getText();         //imei
         if(jCheckBox2.isSelected())
             jTextField26.getText();     //accessori
         jTextArea5.getText();           //difetto dichiarato
         jComboBox4.getSelectedItem();   //a priori si sa lo stato di lavorazione dato è la creazione iniziale
         jTextField21.getText();         //data in
         //  TODO after insert in into database, clear all objects used
     }
     
     private void resetDatiCliente(){
         //reset val cliente
         jTextField7.setText(null);
         jTextField8.setText(null);
         jTextField9.setText(null);
         jTextField10.setText(null);
         jTextArea2.setText(null);
     }
     
     // insert customer's info into edit window
     private void getDatiClienteDb(JTextField nome, JTextField cognome, JTextField indirizzo, JTextField rec, JTextArea note){//FIXME
         
         nome.setText(this.c.getName());                 // name 
         cognome.setText(this.c.getSurname());           // surname
         indirizzo.setText(this.c.getAddress());         // address
         rec.setText(this.c.getTelephone());             // telephone number
         note.setText(this.c.getNote());                 // note
     }
     
     // customer UPDATE
     private void setDatiClienteDb(JTextField nome, JTextField cognome, JTextField indirizzo, JTextField rec, JTextArea note){
 
         // create the object with new data
         SharedClasses.Customer cus = new SharedClasses.Customer(this.c.getID(), nome.getText(), cognome.getText(), indirizzo.getText(), rec.getText(), note.getText());
         // create the request object
         ComClasses.Request r = new ComClasses.Request(cus, ComClasses.Constants.CUSTOMER, ComClasses.Constants.UPDATE, this.c.update(cus));
 
         try {
             // TODO exception if value from the server is an exception
             int v = Utils.intOperation(r).intValue();
 
             if(v == ComClasses.Constants.RET_EXI)
                 showWinAlert(jPanel7, "Utente già esistente.", "Error", JOptionPane.ERROR_MESSAGE);
             else if(v == ComClasses.Constants.RET_EXC)
                 showWinAlert(jPanel7, "Eccezione durante l'inserimento del cliente. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
         
         } catch (Exception e) {
             showWinAlert(jPanel7, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     private void setArticleWarehouseDB(JTextField code, JTextField name, JTextField n, JTextArea note){
         
         // create a new spare part
         SharedClasses.Warehouse w = new SharedClasses.Warehouse(code.getText(), name.getText(), Integer.parseInt(n.getText()), note.getText());
         // create the request object
         ComClasses.Request r = new ComClasses.Request(w, ComClasses.Constants.WARE, ComClasses.Constants.INSERT, w.insert());
         
         try {
             // TODO exception if the value from the server is an exception
             int v = Utils.intOperation(r).intValue();
 
             if(v == ComClasses.Constants.RET_EXI)
                 showWinAlert(jPanel7, "Pezzo di ricambio già esistente.", "Error", JOptionPane.ERROR_MESSAGE);
             else if(v == ComClasses.Constants.RET_EXC)
                 showWinAlert(jPanel7, "Eccezione durante l'inserimento del pezzo. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
             
         } catch (Exception e) {
             showWinAlert(jPanel7, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // set properly the Warehouse object needed to edit a spare part
     private void getValArticleWarehouse(JTextField code, JTextField name, JTextField n, JTextArea note){
         
         String se = (String)jTable1.getValueAt(0, 0);
         String na = (String)jTable1.getValueAt(0, 1);
         Integer av = (Integer)jTable1.getValueAt(0, 2);
         String no = (String)jTable1.getValueAt(0, 3);
         
         this.sp = new SharedClasses.Warehouse(se, na, av.intValue(), no);
         
         code.setText(se);
         name.setText(na);
         n.setText(av.toString());
         note.setText(no);
     }
     
     private void resetValArticleWarehouse(){//FIXME
         jTextField4.setText(null);
         jTextField5.setText(null);
         jTextField6.setText(null);
         jTextArea1.setText(null);
     }
     
     // called in the usage edit window
     private void importArticle (int i, String q) {
         
         int av = (Integer)jTable6.getValueAt(i, 2);
         
         if (av > 0) {
         
             int qt = Integer.parseInt(q);
             int x, y;
             String serial = (String)jTable6.getValueAt(i, 0);
 
             y = this.tableFindSparePart(serial);
 
             if(y != -1) {                                                           // spare part already used in this repair
 
                 Integer aux = (Integer)jTable7.getValueAt(y, 2);
                 jTable7.setValueAt(aux.intValue() + qt, y, 2);
 
             } else {                                                                // new spare part added
 
                 DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
                 model.addRow(new Object[]{serial, jTable6.getValueAt(i, 1), qt});
                 jTable6.setValueAt(av - qt, i, 2);
 
             }
 
             jTable6.setValueAt(av - qt, i, 2);
 
             x = this.cacheFindSparePart(serial);
             if(x != -1)
                 this.cache.get(x).increaseDelta(qt);
             else
                 this.cache.add(new SharedClasses.UsageCache(serial, qt));
             
         } else {
             showWinAlert(jPanel17, "Pezzo terminato", "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // search for a spare part into the usage table
     private int tableFindSparePart (String serial) {
         
         DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
         int n = model.getRowCount();
         
         for(int i = 0; i < n; i++) 
             if(serial.equals(jTable7.getValueAt(i, 0)))
                 return i;
         
         return -1;
         
     }
     
     // add new usage to the database
     private void newUsage () throws Exception {
         
         int n = jTable7.getModel().getRowCount();
         
         if(n > 0) {
             
             ArrayList<Object> o = new ArrayList<Object>();
             SharedClasses.Usage u = null;
 
             for(int i = 0; i < n; i++) {
                 u = new SharedClasses.Usage(this.re.getID(), (String)jTable7.getValueAt(i, 0), (Integer)jTable7.getValueAt(i, 2));
                 o.add(u);
             }
 
             ComClasses.MultiRequest mr = new ComClasses.MultiRequest(o, ComClasses.Constants.USAGE, ComClasses.Constants.MULTINSERT, SharedClasses.Usage.insert());
             // TODO exception if the value from the server is an exception
             Utils.intOperation(mr);
     
         }
         
     }
     
     // delete usage for a selected Repair there were into the database
     private void usageDelete () throws Exception {
         
         ComClasses.Request dr = new ComClasses.Request(null, ComClasses.Constants.USAGE, ComClasses.Constants.DELETE, SharedClasses.Usage.delete());
         // TODO exception if the value from the server is an exception
         Utils.intOperation(dr);
         
     }
     
     // add to the warehouse the spare parts of the old Usage objects
     private void warehouseAdd () throws Exception {
         
         ArrayList<Object> aux = new ArrayList<Object>(old);
         ComClasses.MultiRequest wr = new ComClasses.MultiRequest(aux, ComClasses.Constants.WARE, ComClasses.Constants.MULTIUPDATE, SharedClasses.Warehouse.availabilityUpdate());
         
         Utils.intOperation(wr);
         
     }
     
     // update the warehouse table after an usage edit
     private void warehouseUpdate () throws Exception {
         
         int n = jTable7.getModel().getRowCount();
         
         if(n > 0) {
             
             ArrayList<Object> o = new ArrayList<Object>();
             SharedClasses.UsageCache u = null;
             
             for(int i = 0; i < n; i++) {
                 u = new SharedClasses.UsageCache((String)jTable7.getValueAt(i, 0), -(Integer)jTable7.getValueAt(i, 2));
                 o.add(u);
             }
             
             ComClasses.MultiRequest wr = new ComClasses.MultiRequest(o, ComClasses.Constants.WARE, ComClasses.Constants.MULTIUPDATE, SharedClasses.Warehouse.availabilityUpdate());
             
             Utils.intOperation(wr);
             
         }
         
     }
     
     private boolean controlloRietri(){
         //controlla nel db se ci sono lavorazioni chiuse con questo imei
         
         return false;
     }
     
     protected static ImageIcon createImageIcon(String path, String description) {
         java.net.URL imgURL = ManagGuiView.class.getResource(path);
         if (imgURL != null) {
             return new ImageIcon(imgURL, description);
         } else {
             System.err.println("Couldn't find file: " + path);
             return null;
         }
     }
     
     private boolean ifConnected(){
         if(true){//FIXME prendere valore ip per la connessione dal db
             jLabel80.setVisible(false);
             jLabel82.setVisible(true);
             return true;
         }
             jLabel80.setVisible(true);
             jLabel82.setVisible(false);
             return false;
         
     }
     
     private static void setJTableClient(JTable jt, int n){
         String[] columnNames = new String[]{"Cognome", "Nome", "Indirizzo", "Recapito", "Note"};
         
         DefaultTableModel model = new DefaultTableModel(columnNames, n){
         
             private static final long serialVersionUID = 1L;
             @Override
             public boolean isCellEditable(int row, int column){
                 return false;
             }
         };
         
         jt.setModel(model);
     }
     
     private static void setJTableRepair(JTable jt, int n){
         String[] columnNames = new String[]{"Scheda", "Cognome", "Nome", "Imei-S/N", "Stato Lavorazione"};
         
          DefaultTableModel model = new DefaultTableModel(columnNames, n){
         
             private static final long serialVersionUID = 1L;
             @Override
             public boolean isCellEditable(int row, int column){
                 return false;
             }
         };
 
         jt.setModel(model);
     }
     
     private static void setJTableWarehouse(JTable jt, int n){
         String[] columnNames = new String[]{"Codice Articolo", "Nome Generico", "Quantità", "Note"};
         
         DefaultTableModel model = new DefaultTableModel(columnNames, n){
         
             private static final long serialVersionUID = 1L;
             @Override
             public boolean isCellEditable(int row, int column){
                 return false;
             }
         };
         
         jt.setModel(model);
     }
     
     private static void setJTableUsageWarehouse(JTable jt, int n) {
         String[] columnNames = new String[]{"Codice Articolo", "Nome Generico", "Quantità Disponibile"};
         
         DefaultTableModel model = new DefaultTableModel(columnNames, n){
         
             private static final long serialVersionUID = 1L;
             @Override
             public boolean isCellEditable(int row, int column){
                 return false;
             }
         };
         
         jt.setModel(model);
     }
    
     //set always the top
     private void toppy (FinestraSwing fn){
         if(fn.isAlwaysOnTopSupported())
             fn.setAlwaysOnTop(true);
     }
     
     private static void clearField (JTextField ... f) {
         
         for(int i = 0; i < f.length; i++)
             f[i].setText(null);
         
     }
     
 }
