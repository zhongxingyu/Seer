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
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import javax.swing.DefaultCellEditor;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 /**
 *
 * @author Angelo
 */
 /**
 * The application's main frame.
 */
 public class ManagGuiView extends FrameView {
     
     private MonitorCen p = new MonitorCen(0, 0);
     private FinestraSwing DatiCliente;
     private FinestraSwing cercaCliente;
     private FinestraSwing schedaProdotto;
     private FinestraSwing DatiClienteView;
     private FinestraSwing schedaArticoloMagazzino;
     private FinestraSwing DatiClienteSimply;
     private FinestraSwing pezziUtilizzati;
     private FinestraSwing gestioneRientri;
     private Object[][] jtVal;
     private static final Object arr[] = {"Copia ricevuta", "Riepilogo riparazione", "Annulla stampa ed esci"};
    
     //ico
     private ImageIcon clienti = createImageIcon ("images/clienti2.png", "ico clienti scheda tab");
     private ImageIcon frontEnd = createImageIcon ("images/frontend2.png", "ico accettazione scheda tab");
     private ImageIcon warehouse = createImageIcon ("images/warehouse.png", "ico magazzino scheda tab");
     private ImageIcon billing = createImageIcon ("images/fatt.png", "ico fatturazione scheda tab");
     
     private Client.ServerInfo serverInfo;               // the open port of the server and its address (retrieved from file)
     private Client.WarehouseInfo warehouseInfo;         // warehouse treshold and iva percentage value
     
     private int bill = 0; /* legenda:
      *                       1 = nuova fattura
      *                       2 = nuova n.d.c.
      *                       3 = nuova r.d.a.
      */
     private boolean billingCustomerEditing = false;
     
     //class just for number :D
     private JustNumber justNumbers = new JustNumber();
     private Price price = new Price();
     private Price noIvaPrice = new Price();
     
     private SharedClasses.Customer c;
     private SharedClasses.Device d;
     private SharedClasses.Repair re;
     private SharedClasses.Warehouse sp;
     private SharedClasses.Details de;
     
     private SharedClasses.Customer billingCustomer;
     private SharedClasses.BillingCustomer billingCustomerInfo;
     private boolean newBillingInfo;
     
     private ArrayList<Object> repairRet;                // used by Repair search
     private ArrayList<Object> customerRet;
     private ArrayList<Object> billingCustomerRet;
     private ArrayList<Object> warehouseRet;
     private ArrayList<Object> usageRet;                 // used by Usage search
     private ArrayList<SharedClasses.UsageCache> cache;
     private ArrayList<SharedClasses.UsageCache> old;    // store the situation of usage before editing
     
     public ManagGuiView(SingleFrameApplication app) {
         super(app);
 
         initComponents();
                 
         //magazzino
         jPanel5.setVisible(false);//lista risultato tabella articoli magazzino
         jPanel6.setVisible(false);//aggiungi pezzo
         
         
         //front-end accettazione
         jPanel9.setVisible(false);
         jPanel10.setVisible(false);
         getConnection();
         getWarehouse();
        
         jButton45.setVisible(false);
         
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
         // initialize the dialog every time that is opened to get the info from the settings file
         JFrame mainFrame = ManagGuiApp.getApplication().getMainFrame();
         settingBox = new ManagGuiSettingBox(mainFrame);
         settingBox.setLocationRelativeTo(mainFrame);
         ManagGuiApp.getApplication().show(settingBox);
     }
     
     @Action
     public void showSetting2() {
         // initialize the dialog every time that is opened to get the info from the settings file
         JFrame mainFrame = ManagGuiApp.getApplication().getMainFrame();
         warehouseBox = new ManagGuiSetting2(mainFrame, this);
         warehouseBox.setLocationRelativeTo(mainFrame);
         ManagGuiApp.getApplication().show(warehouseBox);
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
         jLabel4 = new javax.swing.JLabel();
         jComboBox1 = new javax.swing.JComboBox();
         jCheckBox1 = new javax.swing.JCheckBox();
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
         mainCustomerSearch = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jTextField2 = new javax.swing.JTextField();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jScrollPane21 = new javax.swing.JScrollPane();
         jTable5 = new javax.swing.JTable();
         mainWarehouseEdit = new javax.swing.JPanel();
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
         jLabel3 = new javax.swing.JLabel();
         jTextField52 = new javax.swing.JTextField();
         jLabel19 = new javax.swing.JLabel();
         jTextField88 = new javax.swing.JTextField();
         jPanel5 = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         jTextField3 = new javax.swing.JTextField();
         jButton7 = new javax.swing.JButton();
         jButton8 = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jButton76 = new javax.swing.JButton();
         jLabel83 = new javax.swing.JLabel();
         jLabel124 = new javax.swing.JLabel();
         mainBillingEdit = new javax.swing.JPanel();
         jButton23 = new javax.swing.JButton();
         jButton24 = new javax.swing.JButton();
         billingCreation = new javax.swing.JPanel();
         jButton30 = new javax.swing.JButton();
         jButton31 = new javax.swing.JButton();
         jButton34 = new javax.swing.JButton();
         fatturaView = new javax.swing.JPanel();
         jTextField42 = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jScrollPane17 = new javax.swing.JScrollPane();
         jTable8 = new javax.swing.JTable();
         jButton50 = new javax.swing.JButton();
         jButton51 = new javax.swing.JButton();
         jButton52 = new javax.swing.JButton();
         jButton53 = new javax.swing.JButton();
         jLabel61 = new javax.swing.JLabel();
         jLabel62 = new javax.swing.JLabel();
         jLabel79 = new javax.swing.JLabel();
         jLabel80 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         jTextField53 = new javax.swing.JTextField();
         jTextField54 = new javax.swing.JTextField();
         jLabel21 = new javax.swing.JLabel();
         jTextField56 = new javax.swing.JTextField();
         jLabel22 = new javax.swing.JLabel();
         jLabel91 = new javax.swing.JLabel();
         jTextField63 = new javax.swing.JTextField();
         jLabel60 = new javax.swing.JLabel();
         jTextField57 = new javax.swing.JTextField();
         jTextField58 = new javax.swing.JTextField();
         jButton54 = new javax.swing.JButton();
         jButton55 = new javax.swing.JButton();
         ndcView = new javax.swing.JPanel();
         jTextField71 = new javax.swing.JTextField();
         jLabel101 = new javax.swing.JLabel();
         jLabel102 = new javax.swing.JLabel();
         jScrollPane24 = new javax.swing.JScrollPane();
         jTable11 = new javax.swing.JTable();
         jButton65 = new javax.swing.JButton();
         jButton66 = new javax.swing.JButton();
         jButton67 = new javax.swing.JButton();
         jButton68 = new javax.swing.JButton();
         jLabel104 = new javax.swing.JLabel();
         jLabel105 = new javax.swing.JLabel();
         jLabel106 = new javax.swing.JLabel();
         jLabel107 = new javax.swing.JLabel();
         jLabel108 = new javax.swing.JLabel();
         jTextField72 = new javax.swing.JTextField();
         jTextField73 = new javax.swing.JTextField();
         jLabel109 = new javax.swing.JLabel();
         jTextField74 = new javax.swing.JTextField();
         jLabel110 = new javax.swing.JLabel();
         rdaView = new javax.swing.JPanel();
         jTextField75 = new javax.swing.JTextField();
         jLabel111 = new javax.swing.JLabel();
         jLabel112 = new javax.swing.JLabel();
         jScrollPane25 = new javax.swing.JScrollPane();
         jTable12 = new javax.swing.JTable();
         jButton69 = new javax.swing.JButton();
         jButton70 = new javax.swing.JButton();
         jButton71 = new javax.swing.JButton();
         jButton72 = new javax.swing.JButton();
         jLabel114 = new javax.swing.JLabel();
         jLabel115 = new javax.swing.JLabel();
         jLabel116 = new javax.swing.JLabel();
         jLabel117 = new javax.swing.JLabel();
         jLabel118 = new javax.swing.JLabel();
         jTextField76 = new javax.swing.JTextField();
         jTextField77 = new javax.swing.JTextField();
         jLabel119 = new javax.swing.JLabel();
         jTextField78 = new javax.swing.JTextField();
         jLabel120 = new javax.swing.JLabel();
         jLabel125 = new javax.swing.JLabel();
         jTextField85 = new javax.swing.JTextField();
         jLabel126 = new javax.swing.JLabel();
         jTextField86 = new javax.swing.JTextField();
         jButton63 = new javax.swing.JButton();
         jButton64 = new javax.swing.JButton();
         billingSearch = new javax.swing.JPanel();
         jTextField79 = new javax.swing.JTextField();
         jLabel121 = new javax.swing.JLabel();
         jTextField80 = new javax.swing.JTextField();
         jButton73 = new javax.swing.JButton();
         jButton74 = new javax.swing.JButton();
         jScrollPane26 = new javax.swing.JScrollPane();
         jTable13 = new javax.swing.JTable();
         jLabel122 = new javax.swing.JLabel();
         jTextField81 = new javax.swing.JTextField();
         jLabel123 = new javax.swing.JLabel();
         jTextField82 = new javax.swing.JTextField();
         jTextField87 = new javax.swing.JTextField();
         jLabel98 = new javax.swing.JLabel();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenuItem2 = new javax.swing.JMenuItem();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
         customerEdit = new javax.swing.JPanel();
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
         customerSearch = new javax.swing.JPanel();
         jLabel17 = new javax.swing.JLabel();
         jTextField11 = new javax.swing.JTextField();
         jTextField12 = new javax.swing.JTextField();
         jButton20 = new javax.swing.JButton();
         jButton21 = new javax.swing.JButton();
         jButton22 = new javax.swing.JButton();
         jScrollPane20 = new javax.swing.JScrollPane();
         jTable2 = new javax.swing.JTable();
         repairDetail = new javax.swing.JPanel();
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
         jLabel5 = new javax.swing.JLabel();
         jComboBox2 = new javax.swing.JComboBox();
         jCheckBox4 = new javax.swing.JCheckBox();
         jButton77 = new javax.swing.JButton();
         jButton78 = new javax.swing.JButton();
         jButton79 = new javax.swing.JButton();
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
         warehouseEdit = new javax.swing.JPanel();
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
         jLabel2 = new javax.swing.JLabel();
         jTextField41 = new javax.swing.JTextField();
         jTextField89 = new javax.swing.JTextField();
         jLabel99 = new javax.swing.JLabel();
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
         customerInfoView = new javax.swing.JPanel();
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
         warehouseManagement = new javax.swing.JPanel();
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
         jTextField90 = new javax.swing.JTextField();
         jTextField91 = new javax.swing.JTextField();
         jLabel100 = new javax.swing.JLabel();
         jLabel103 = new javax.swing.JLabel();
         jPanel18 = new javax.swing.JPanel();
         jLabel81 = new javax.swing.JLabel();
         jTextField55 = new javax.swing.JTextField();
         jScrollPane19 = new javax.swing.JScrollPane();
         jTable4 = new javax.swing.JTable();
         jLabel78 = new javax.swing.JLabel();
         billingCustomerEdit = new javax.swing.JPanel();
         jScrollPane18 = new javax.swing.JScrollPane();
         jTextArea13 = new javax.swing.JTextArea();
         jLabel82 = new javax.swing.JLabel();
         jLabel84 = new javax.swing.JLabel();
         jTextField59 = new javax.swing.JTextField();
         jTextField60 = new javax.swing.JTextField();
         jLabel85 = new javax.swing.JLabel();
         jTextField61 = new javax.swing.JTextField();
         jTextField62 = new javax.swing.JTextField();
         jLabel86 = new javax.swing.JLabel();
         jLabel87 = new javax.swing.JLabel();
         jButton56 = new javax.swing.JButton();
         jButton57 = new javax.swing.JButton();
         jLabel88 = new javax.swing.JLabel();
         jTextFieldCF = new javax.swing.JTextField();
         jLabel89 = new javax.swing.JLabel();
         jTextFieldIVA = new javax.swing.JTextField();
         billingCustomerSearch = new javax.swing.JPanel();
         jLabel90 = new javax.swing.JLabel();
         jTextField65 = new javax.swing.JTextField();
         jTextField66 = new javax.swing.JTextField();
         jButton58 = new javax.swing.JButton();
         jButton59 = new javax.swing.JButton();
         jButton60 = new javax.swing.JButton();
         jScrollPane22 = new javax.swing.JScrollPane();
         jTable9 = new javax.swing.JTable();
 
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
         jTextArea5.setLineWrap(true);
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
 
         jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
         jLabel4.setName("jLabel4"); // NOI18N
 
         jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "---", "Ottimo", "Buono", "Discreto", "Rovinato", "Estr.nte rov.to" }));
         jComboBox1.setName("jComboBox1"); // NOI18N
 
         jCheckBox1.setBackground(resourceMap.getColor("jCheckBox1.background")); // NOI18N
         jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
         jCheckBox1.setName("jCheckBox1"); // NOI18N
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGap(12, 12, 12)
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
                                 .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(jPanel9Layout.createSequentialGroup()
                                 .addComponent(jLabel4)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(51, 51, 51)
                                 .addComponent(jCheckBox1)))
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
                             .addComponent(jTextField21, javax.swing.GroupLayout.Alignment.TRAILING)))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGap(6, 6, 6)
                         .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(jButton48)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton49))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(jLabel36)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(164, 164, 164)
                         .addComponent(jButton41)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton18)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton17))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGap(12, 12, 12)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                 .addComponent(jCheckBox2)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                             .addComponent(jButton45)))
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
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jCheckBox1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jCheckBox2)
                     .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel36)
                     .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton18)
                     .addComponent(jButton41)
                     .addComponent(jButton17))
                 .addContainerGap())
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
                     .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1008, Short.MAX_VALUE))
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
                 .addContainerGap(223, Short.MAX_VALUE))
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
                 .addContainerGap(223, Short.MAX_VALUE))
             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                     .addGap(50, 50, 50)
                     .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(256, Short.MAX_VALUE)))
         );
 
         jButton1.setVisible(false);
         jButton2.setVisible(false);
 
         jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), frontEnd, jPanel1); // NOI18N
 
         mainCustomerSearch.setBackground(resourceMap.getColor("mainCustomerSearch.background")); // NOI18N
         mainCustomerSearch.setName("mainCustomerSearch"); // NOI18N
 
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
 
         javax.swing.GroupLayout mainCustomerSearchLayout = new javax.swing.GroupLayout(mainCustomerSearch);
         mainCustomerSearch.setLayout(mainCustomerSearchLayout);
         mainCustomerSearchLayout.setHorizontalGroup(
             mainCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainCustomerSearchLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 1030, Short.MAX_VALUE)
                     .addGroup(mainCustomerSearchLayout.createSequentialGroup()
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
         mainCustomerSearchLayout.setVerticalGroup(
             mainCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainCustomerSearchLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton3)
                     .addComponent(jButton4))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab(resourceMap.getString("mainCustomerSearch.TabConstraints.tabTitle"), mainCustomerSearch); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("mainCustomerSearch.TabConstraints.tabTitle"), clienti, mainCustomerSearch); // NOI18N
 
         mainWarehouseEdit.setBackground(resourceMap.getColor("mainWarehouseEdit.background")); // NOI18N
         mainWarehouseEdit.setName("mainWarehouseEdit"); // NOI18N
 
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
         jTextArea1.setWrapStyleWord(true);
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
 
         jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
         jLabel3.setName("jLabel3"); // NOI18N
 
         jTextField52.setText(resourceMap.getString("jTextField52.text")); // NOI18N
         jTextField52.setName("jTextField52"); // NOI18N
         jTextField52.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField52KeyReleased(evt);
             }
         });
 
         jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
         jLabel19.setName("jLabel19"); // NOI18N
 
         jTextField88.setText(resourceMap.getString("jTextField88.text")); // NOI18N
         jTextField88.setName("jTextField88"); // NOI18N
         jTextField88.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField88KeyReleased(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                 .addGap(20, 20, 20)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel11)
                     .addComponent(jLabel10)
                     .addComponent(jLabel9)
                     .addComponent(jLabel3)
                     .addComponent(jLabel19)
                     .addComponent(jLabel8))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jTextField88)
                     .addComponent(jTextField4)
                     .addComponent(jTextField5)
                     .addComponent(jTextField52)
                     .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                     .addComponent(jScrollPane3))
                 .addGap(159, 159, 159))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                 .addContainerGap(424, Short.MAX_VALUE)
                 .addComponent(jButton10)
                 .addGap(20, 20, 20))
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton9)
                 .addContainerGap(394, Short.MAX_VALUE))
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel8))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel9)
                     .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel10))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(jTextField52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel19)
                     .addComponent(jTextField88, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jButton10)
                     .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLabel11)
                         .addGroup(jPanel6Layout.createSequentialGroup()
                             .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGap(16, 16, 16)
                             .addComponent(jButton9))))
                 .addContainerGap())
         );
 
         jTextField6.setDocument(justNumbers);
         jTextField52.setDocument(price);
         jTextField88.setDocument(noIvaPrice);
 
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
 
         jButton76.setIcon(resourceMap.getIcon("jButton76.icon")); // NOI18N
         jButton76.setText(resourceMap.getString("jButton76.text")); // NOI18N
         jButton76.setToolTipText(resourceMap.getString("jButton76.toolTipText")); // NOI18N
         jButton76.setName("jButton76"); // NOI18N
         jButton76.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton76MouseClicked(evt);
             }
         });
 
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
                         .addComponent(jButton8)
                         .addGap(69, 69, 69)
                         .addComponent(jButton76, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 706, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jButton7)
                             .addComponent(jButton8)
                             .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel7))
                         .addGap(18, 18, 18))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                         .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jButton76, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(2, 2, 2)))
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jLabel83.setIcon(resourceMap.getIcon("jLabel83.icon")); // NOI18N
         jLabel83.setName("jLabel83"); // NOI18N
 
         jLabel124.setIcon(resourceMap.getIcon("jLabel124.icon")); // NOI18N
         jLabel124.setText(resourceMap.getString("jLabel124.text")); // NOI18N
         jLabel124.setName("jLabel124"); // NOI18N
 
         javax.swing.GroupLayout mainWarehouseEditLayout = new javax.swing.GroupLayout(mainWarehouseEdit);
         mainWarehouseEdit.setLayout(mainWarehouseEditLayout);
         mainWarehouseEditLayout.setHorizontalGroup(
             mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainWarehouseEditLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(mainWarehouseEditLayout.createSequentialGroup()
                         .addComponent(jLabel83)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel124)
                         .addGap(3, 3, 3)
                         .addComponent(jButton5)
                         .addGap(49, 49, 49)
                         .addComponent(jButton6)))
                 .addContainerGap(312, Short.MAX_VALUE))
             .addGroup(mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(mainWarehouseEditLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(469, Short.MAX_VALUE)))
         );
         mainWarehouseEditLayout.setVerticalGroup(
             mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainWarehouseEditLayout.createSequentialGroup()
                 .addGap(12, 12, 12)
                 .addGroup(mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(mainWarehouseEditLayout.createSequentialGroup()
                         .addGroup(mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel83)
                             .addGroup(mainWarehouseEditLayout.createSequentialGroup()
                                 .addGap(44, 44, 44)
                                 .addGroup(mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                     .addComponent(jLabel124))
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(304, Short.MAX_VALUE))
             .addGroup(mainWarehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(mainWarehouseEditLayout.createSequentialGroup()
                     .addGap(135, 135, 135)
                     .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(308, Short.MAX_VALUE)))
         );
 
         //jLabel83.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         jTabbedPane1.addTab(resourceMap.getString("mainWarehouseEdit.TabConstraints.tabTitle"), mainWarehouseEdit); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("mainWarehouseEdit.TabConstraints.tabTitle"), warehouse, mainWarehouseEdit); // NOI18N
 
         mainBillingEdit.setName("mainBillingEdit"); // NOI18N
 
         jButton23.setText(resourceMap.getString("jButton23.text")); // NOI18N
         jButton23.setName("jButton23"); // NOI18N
         jButton23.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton23MouseClicked(evt);
             }
         });
 
         jButton24.setText(resourceMap.getString("jButton24.text")); // NOI18N
         jButton24.setName("jButton24"); // NOI18N
         jButton24.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton24MouseClicked(evt);
             }
         });
 
         billingCreation.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         billingCreation.setName("billingCreation"); // NOI18N
         billingCreation.setVisible(false);
 
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
 
         jButton34.setText(resourceMap.getString("jButton34.text")); // NOI18N
         jButton34.setName("jButton34"); // NOI18N
         jButton34.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton34MouseClicked(evt);
             }
         });
 
         fatturaView.setName("fatturaView"); // NOI18N
 
         jTextField42.setText(resourceMap.getString("jTextField42.text")); // NOI18N
         jTextField42.setName("jTextField42"); // NOI18N
 
         jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
         jLabel6.setName("jLabel6"); // NOI18N
 
         jLabel18.setFont(resourceMap.getFont("jLabel18.font")); // NOI18N
         jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
         jLabel18.setName("jLabel18"); // NOI18N
 
         jScrollPane17.setName("jScrollPane17"); // NOI18N
 
         jTable8.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jTable8.setName("jTable8"); // NOI18N
         jScrollPane17.setViewportView(jTable8);
         setJTableBilling(jTable8, 1);
 
         jButton50.setIcon(resourceMap.getIcon("jButton50.icon")); // NOI18N
         jButton50.setToolTipText(resourceMap.getString("jButton50.toolTipText")); // NOI18N
         jButton50.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         jButton50.setName("jButton50"); // NOI18N
         jButton50.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton50MouseClicked(evt);
             }
         });
 
         jButton51.setIcon(resourceMap.getIcon("jButton51.icon")); // NOI18N
         jButton51.setToolTipText(resourceMap.getString("jButton51.toolTipText")); // NOI18N
         jButton51.setAlignmentY(0.0F);
         jButton51.setName("jButton51"); // NOI18N
         jButton51.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton51MouseClicked(evt);
             }
         });
 
         jButton52.setIcon(resourceMap.getIcon("jButton52.icon")); // NOI18N
         jButton52.setToolTipText(resourceMap.getString("jButton52.toolTipText")); // NOI18N
         jButton52.setName("jButton52"); // NOI18N
         jButton52.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton52MouseClicked(evt);
             }
         });
 
         jButton53.setIcon(resourceMap.getIcon("jButton53.icon")); // NOI18N
         jButton53.setToolTipText(resourceMap.getString("jButton53.toolTipText")); // NOI18N
         jButton53.setName("jButton53"); // NOI18N
         jButton53.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton53MouseClicked(evt);
             }
         });
 
         jLabel61.setText(resourceMap.getString("jLabel61.text")); // NOI18N
         jLabel61.setName("jLabel61"); // NOI18N
 
         jLabel62.setText(resourceMap.getString("jLabel62.text")); // NOI18N
         jLabel62.setName("jLabel62"); // NOI18N
 
         jLabel79.setText(resourceMap.getString("jLabel79.text")); // NOI18N
         jLabel79.setName("jLabel79"); // NOI18N
 
         jLabel80.setText(resourceMap.getString("jLabel80.text")); // NOI18N
         jLabel80.setName("jLabel80"); // NOI18N
 
         jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
         jLabel20.setName("jLabel20"); // NOI18N
 
         jTextField53.setEditable(false);
         jTextField53.setText(resourceMap.getString("jTextField53.text")); // NOI18N
         jTextField53.setName("jTextField53"); // NOI18N
 
         jTextField54.setText(resourceMap.getString("jTextField54.text")); // NOI18N
         jTextField54.setName("jTextField54"); // NOI18N
 
         jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
         jLabel21.setName("jLabel21"); // NOI18N
 
         jTextField56.setName("jTextField56"); // NOI18N
 
         jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
         jLabel22.setName("jLabel22"); // NOI18N
 
         jLabel91.setText(resourceMap.getString("jLabel91.text")); // NOI18N
         jLabel91.setName("jLabel91"); // NOI18N
 
         jTextField63.setEditable(false);
         jTextField63.setName("jTextField63"); // NOI18N
 
         javax.swing.GroupLayout fatturaViewLayout = new javax.swing.GroupLayout(fatturaView);
         fatturaView.setLayout(fatturaViewLayout);
         fatturaViewLayout.setHorizontalGroup(
             fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(fatturaViewLayout.createSequentialGroup()
                 .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, fatturaViewLayout.createSequentialGroup()
                         .addComponent(jLabel20)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField53, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel21)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField54, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel91)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField63, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jLabel22)
                         .addGap(18, 18, 18)
                         .addComponent(jTextField56, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jScrollPane17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 907, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                 .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jButton53, 0, 0, Short.MAX_VALUE)
                     .addComponent(jButton52, 0, 0, Short.MAX_VALUE)
                     .addComponent(jButton51, 0, 0, Short.MAX_VALUE)
                     .addComponent(jButton50, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel80)
                     .addComponent(jLabel79)
                     .addComponent(jLabel62)
                     .addComponent(jLabel61))
                 .addGap(24, 24, 24))
             .addGroup(fatturaViewLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel6)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 590, Short.MAX_VALUE)
                 .addComponent(jLabel18)
                .addGap(260, 260, 260))
         );
         fatturaViewLayout.setVerticalGroup(
             fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(fatturaViewLayout.createSequentialGroup()
                 .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel6)
                     .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(fatturaViewLayout.createSequentialGroup()
                         .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton50)
                             .addComponent(jLabel61))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton51, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel62))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton52)
                             .addComponent(jLabel79))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton53)
                             .addComponent(jLabel80)))
                     .addGroup(fatturaViewLayout.createSequentialGroup()
                         .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(fatturaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel20)
                             .addComponent(jTextField53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel21)
                             .addComponent(jTextField54, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel91)
                             .addComponent(jTextField63, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField56, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel22)))))
         );
 
         jTextField42.setText(getDataOra());
         jTextField42.setEditable(false);
         jTextField53.setEditable(false);
         jTextField56.setEditable(false);
         jTextField56.setEditable(false);
 
         jLabel60.setText(resourceMap.getString("jLabel60.text")); // NOI18N
         jLabel60.setName("jLabel60"); // NOI18N
 
         jTextField57.setText(resourceMap.getString("jTextField57.text")); // NOI18N
         jTextField57.setName("jTextField57"); // NOI18N
 
         jTextField58.setText(resourceMap.getString("jTextField58.text")); // NOI18N
         jTextField58.setName("jTextField58"); // NOI18N
 
         jButton54.setText(resourceMap.getString("jButton54.text")); // NOI18N
         jButton54.setName("jButton54"); // NOI18N
         jButton54.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton54MouseClicked(evt);
             }
         });
 
         jButton55.setText(resourceMap.getString("jButton55.text")); // NOI18N
         jButton55.setName("jButton55"); // NOI18N
         jButton55.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton55MouseClicked(evt);
             }
         });
 
         ndcView.setName("ndcView"); // NOI18N
 
         jTextField71.setName("jTextField71"); // NOI18N
 
         jLabel101.setText(resourceMap.getString("jLabel101.text")); // NOI18N
         jLabel101.setName("jLabel101"); // NOI18N
 
         jLabel102.setText(resourceMap.getString("jLabel102.text")); // NOI18N
         jLabel102.setName("jLabel102"); // NOI18N
 
         jScrollPane24.setName("jScrollPane24"); // NOI18N
 
         jTable11.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jTable11.setName("jTable11"); // NOI18N
         jScrollPane24.setViewportView(jTable11);
         setJTableBilling(jTable11, 1);
 
         jButton65.setIcon(resourceMap.getIcon("jButton65.icon")); // NOI18N
         jButton65.setToolTipText(resourceMap.getString("jButton65.toolTipText")); // NOI18N
         jButton65.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         jButton65.setName("jButton65"); // NOI18N
         jButton65.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton65MouseClicked(evt);
             }
         });
 
         jButton66.setIcon(resourceMap.getIcon("jButton66.icon")); // NOI18N
         jButton66.setToolTipText(resourceMap.getString("jButton66.toolTipText")); // NOI18N
         jButton66.setAlignmentY(0.0F);
         jButton66.setName("jButton66"); // NOI18N
         jButton66.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton66MouseClicked(evt);
             }
         });
 
         jButton67.setIcon(resourceMap.getIcon("jButton67.icon")); // NOI18N
         jButton67.setToolTipText(resourceMap.getString("jButton67.toolTipText")); // NOI18N
         jButton67.setName("jButton67"); // NOI18N
         jButton67.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton67MouseClicked(evt);
             }
         });
 
         jButton68.setIcon(resourceMap.getIcon("jButton68.icon")); // NOI18N
         jButton68.setToolTipText(resourceMap.getString("jButton68.toolTipText")); // NOI18N
         jButton68.setName("jButton68"); // NOI18N
         jButton68.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton68MouseClicked(evt);
             }
         });
 
         jLabel104.setText(resourceMap.getString("jLabel104.text")); // NOI18N
         jLabel104.setName("jLabel104"); // NOI18N
 
         jLabel105.setText(resourceMap.getString("jLabel105.text")); // NOI18N
         jLabel105.setName("jLabel105"); // NOI18N
 
         jLabel106.setText(resourceMap.getString("jLabel106.text")); // NOI18N
         jLabel106.setName("jLabel106"); // NOI18N
 
         jLabel107.setText(resourceMap.getString("jLabel107.text")); // NOI18N
         jLabel107.setName("jLabel107"); // NOI18N
 
         jLabel108.setText(resourceMap.getString("jLabel108.text")); // NOI18N
         jLabel108.setName("jLabel108"); // NOI18N
 
         jTextField72.setName("jTextField72"); // NOI18N
 
         jTextField73.setText(resourceMap.getString("jTextField73.text")); // NOI18N
         jTextField73.setName("jTextField73"); // NOI18N
 
         jLabel109.setText(resourceMap.getString("jLabel109.text")); // NOI18N
         jLabel109.setName("jLabel109"); // NOI18N
 
         jTextField74.setName("jTextField74"); // NOI18N
 
         jLabel110.setText(resourceMap.getString("jLabel110.text")); // NOI18N
         jLabel110.setName("jLabel110"); // NOI18N
 
         javax.swing.GroupLayout ndcViewLayout = new javax.swing.GroupLayout(ndcView);
         ndcView.setLayout(ndcViewLayout);
         ndcViewLayout.setHorizontalGroup(
             ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ndcViewLayout.createSequentialGroup()
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ndcViewLayout.createSequentialGroup()
                         .addGap(35, 35, 35)
                         .addComponent(jLabel108)
                         .addGap(4, 4, 4)
                         .addComponent(jTextField72, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(32, 32, 32)
                         .addComponent(jLabel109)
                         .addGap(4, 4, 4)
                         .addComponent(jTextField73, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 297, Short.MAX_VALUE)
                         .addComponent(jLabel110)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField74, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(14, 14, 14))
                     .addComponent(jScrollPane24, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 897, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jButton68, 0, 0, Short.MAX_VALUE)
                     .addComponent(jButton67, 0, 0, Short.MAX_VALUE)
                     .addComponent(jButton66, 0, 0, Short.MAX_VALUE)
                     .addComponent(jButton65, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel107)
                     .addComponent(jLabel106)
                     .addComponent(jLabel105)
                     .addComponent(jLabel104))
                 .addContainerGap())
             .addGroup(ndcViewLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel101)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField71, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 545, Short.MAX_VALUE)
                 .addComponent(jLabel102)
                 .addGap(239, 239, 239))
         );
         ndcViewLayout.setVerticalGroup(
             ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ndcViewLayout.createSequentialGroup()
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField71, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel101)
                     .addComponent(jLabel102, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jTextField72, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel108))
                     .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jTextField73, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel109))
                     .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel110)
                         .addComponent(jTextField74, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
             .addGroup(ndcViewLayout.createSequentialGroup()
                 .addGap(27, 27, 27)
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jButton65)
                     .addComponent(jLabel104))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jButton66, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel105))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jButton67)
                     .addComponent(jLabel106))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(ndcViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jButton68)
                     .addComponent(jLabel107)))
         );
 
         jTextField71.setText(getDataOra());
         jTextField71.setEditable(false);
         jTextField72.setEditable(false);
         jTextField74.setEditable(false);
 
         rdaView.setName("rdaView"); // NOI18N
 
         jTextField75.setName("jTextField75"); // NOI18N
 
         jLabel111.setText(resourceMap.getString("jLabel111.text")); // NOI18N
         jLabel111.setName("jLabel111"); // NOI18N
 
         jLabel112.setText(resourceMap.getString("jLabel112.text")); // NOI18N
         jLabel112.setName("jLabel112"); // NOI18N
 
         jScrollPane25.setName("jScrollPane25"); // NOI18N
 
         jTable12.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jTable12.setName("jTable12"); // NOI18N
         jScrollPane25.setViewportView(jTable12);
         setJTableBilling(jTable12, 1);
 
         jButton69.setIcon(resourceMap.getIcon("jButton69.icon")); // NOI18N
         jButton69.setToolTipText(resourceMap.getString("jButton69.toolTipText")); // NOI18N
         jButton69.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         jButton69.setName("jButton69"); // NOI18N
         jButton69.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton69MouseClicked(evt);
             }
         });
 
         jButton70.setIcon(resourceMap.getIcon("jButton70.icon")); // NOI18N
         jButton70.setToolTipText(resourceMap.getString("jButton70.toolTipText")); // NOI18N
         jButton70.setAlignmentY(0.0F);
         jButton70.setName("jButton70"); // NOI18N
         jButton70.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton70MouseClicked(evt);
             }
         });
 
         jButton71.setIcon(resourceMap.getIcon("jButton71.icon")); // NOI18N
         jButton71.setToolTipText(resourceMap.getString("jButton71.toolTipText")); // NOI18N
         jButton71.setName("jButton71"); // NOI18N
         jButton71.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton71MouseClicked(evt);
             }
         });
 
         jButton72.setIcon(resourceMap.getIcon("jButton72.icon")); // NOI18N
         jButton72.setToolTipText(resourceMap.getString("jButton72.toolTipText")); // NOI18N
         jButton72.setName("jButton72"); // NOI18N
         jButton72.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton72MouseClicked(evt);
             }
         });
 
         jLabel114.setText(resourceMap.getString("jLabel114.text")); // NOI18N
         jLabel114.setName("jLabel114"); // NOI18N
 
         jLabel115.setText(resourceMap.getString("jLabel115.text")); // NOI18N
         jLabel115.setName("jLabel115"); // NOI18N
 
         jLabel116.setText(resourceMap.getString("jLabel116.text")); // NOI18N
         jLabel116.setName("jLabel116"); // NOI18N
 
         jLabel117.setText(resourceMap.getString("jLabel117.text")); // NOI18N
         jLabel117.setName("jLabel117"); // NOI18N
 
         jLabel118.setText(resourceMap.getString("jLabel118.text")); // NOI18N
         jLabel118.setName("jLabel118"); // NOI18N
 
         jTextField76.setName("jTextField76"); // NOI18N
 
         jTextField77.setText(resourceMap.getString("jTextField77.text")); // NOI18N
         jTextField77.setName("jTextField77"); // NOI18N
 
         jLabel119.setText(resourceMap.getString("jLabel119.text")); // NOI18N
         jLabel119.setName("jLabel119"); // NOI18N
 
         jTextField78.setName("jTextField78"); // NOI18N
 
         jLabel120.setText(resourceMap.getString("jLabel120.text")); // NOI18N
         jLabel120.setName("jLabel120"); // NOI18N
 
         jLabel125.setText(resourceMap.getString("jLabel125.text")); // NOI18N
         jLabel125.setName("jLabel125"); // NOI18N
 
         jTextField85.setText(resourceMap.getString("jTextField85.text")); // NOI18N
         jTextField85.setName("jTextField85"); // NOI18N
 
         jLabel126.setText(resourceMap.getString("jLabel126.text")); // NOI18N
         jLabel126.setName("jLabel126"); // NOI18N
 
         jTextField86.setText(resourceMap.getString("jTextField86.text")); // NOI18N
         jTextField86.setName("jTextField86"); // NOI18N
 
         javax.swing.GroupLayout rdaViewLayout = new javax.swing.GroupLayout(rdaView);
         rdaView.setLayout(rdaViewLayout);
         rdaViewLayout.setHorizontalGroup(
             rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rdaViewLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(rdaViewLayout.createSequentialGroup()
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addGroup(rdaViewLayout.createSequentialGroup()
                                 .addComponent(jLabel118)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField76, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(27, 27, 27)
                                 .addComponent(jLabel119)
                                 .addGap(2, 2, 2)
                                 .addComponent(jTextField77, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(61, 61, 61)
                                 .addComponent(jLabel125)
                                 .addGap(4, 4, 4)
                                 .addComponent(jTextField85, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(44, 44, 44)
                                 .addComponent(jLabel126)
                                 .addGap(4, 4, 4)
                                 .addComponent(jTextField86, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jLabel120)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jTextField78, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jScrollPane25, javax.swing.GroupLayout.PREFERRED_SIZE, 883, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                 .addComponent(jButton71, 0, 0, Short.MAX_VALUE)
                                 .addComponent(jButton70, javax.swing.GroupLayout.PREFERRED_SIZE, 26, Short.MAX_VALUE)
                                 .addComponent(jButton72, 0, 0, Short.MAX_VALUE))
                             .addComponent(jButton69, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel114)
                             .addComponent(jLabel115)
                             .addComponent(jLabel116)
                             .addComponent(jLabel117))
                         .addContainerGap())
                     .addGroup(rdaViewLayout.createSequentialGroup()
                         .addComponent(jLabel111)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField75, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 489, Short.MAX_VALUE)
                         .addComponent(jLabel112)
                         .addGap(354, 354, 354))))
         );
         rdaViewLayout.setVerticalGroup(
             rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rdaViewLayout.createSequentialGroup()
                 .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField75, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel111)
                     .addComponent(jLabel112, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane25, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(rdaViewLayout.createSequentialGroup()
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel114)
                             .addComponent(jButton69))
                         .addGap(9, 9, 9)
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton70, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel115))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton71)
                             .addComponent(jLabel116))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel117)
                             .addComponent(jButton72))
                         .addGap(208, 208, 208)
                         .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jLabel118)
                                 .addComponent(jTextField76, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(rdaViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jLabel119)
                                 .addComponent(jTextField77, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jTextField85, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel125)
                                 .addComponent(jTextField86, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel126)
                                 .addComponent(jTextField78, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel120))))))
         );
 
         jTextField75.setText(getDataOra());
         jTextField75.setEditable(false);
         jTextField76.setEditable(false);
         jTextField78.setEditable(false);
 
         jButton63.setIcon(resourceMap.getIcon("jButton63.icon")); // NOI18N
         jButton63.setText(resourceMap.getString("jButton63.text")); // NOI18N
         jButton63.setName("jButton63"); // NOI18N
         jButton63.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton63MouseClicked(evt);
             }
         });
 
         jButton64.setIcon(resourceMap.getIcon("jButton64.icon")); // NOI18N
         jButton64.setText(resourceMap.getString("jButton64.text")); // NOI18N
         jButton64.setName("jButton64"); // NOI18N
         jButton64.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton64MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout billingCreationLayout = new javax.swing.GroupLayout(billingCreation);
         billingCreation.setLayout(billingCreationLayout);
         billingCreationLayout.setHorizontalGroup(
             billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingCreationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(billingCreationLayout.createSequentialGroup()
                         .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(billingCreationLayout.createSequentialGroup()
                                 .addComponent(jButton30)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jButton31)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jButton34))
                             .addGroup(billingCreationLayout.createSequentialGroup()
                                 .addComponent(jLabel60)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField57, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jTextField58, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jButton54)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jButton55))
                             .addComponent(fatturaView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addContainerGap(77, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, billingCreationLayout.createSequentialGroup()
                         .addComponent(jButton63)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton64)
                         .addGap(375, 375, 375))))
             .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(billingCreationLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(ndcView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(111, Short.MAX_VALUE)))
             .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(billingCreationLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(rdaView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
         );
         billingCreationLayout.setVerticalGroup(
             billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingCreationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton30)
                     .addComponent(jButton31)
                     .addComponent(jButton34))
                 .addGap(18, 18, 18)
                 .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel60)
                     .addComponent(jTextField57, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField58, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton54)
                     .addComponent(jButton55))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(fatturaView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                 .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton63)
                     .addComponent(jButton64))
                 .addContainerGap())
             .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(billingCreationLayout.createSequentialGroup()
                     .addGap(98, 98, 98)
                     .addComponent(ndcView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(92, Short.MAX_VALUE)))
             .addGroup(billingCreationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(billingCreationLayout.createSequentialGroup()
                     .addGap(97, 97, 97)
                     .addComponent(rdaView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(102, Short.MAX_VALUE)))
         );
 
         fatturaView.setVisible(false);
         jTextField57.setEditable(false);
         jTextField58.setEditable(false);
         ndcView.setVisible(false);
         rdaView.setVisible(false);
 
         billingSearch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         billingSearch.setName("billingSearch"); // NOI18N
         billingSearch.setVisible(false);
 
         jTextField79.setText(resourceMap.getString("jTextField79.text")); // NOI18N
         jTextField79.setName("jTextField79"); // NOI18N
         jTextField79.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField79MouseClicked(evt);
             }
         });
 
         jLabel121.setText(resourceMap.getString("jLabel121.text")); // NOI18N
         jLabel121.setName("jLabel121"); // NOI18N
 
         jTextField80.setText(resourceMap.getString("jTextField80.text")); // NOI18N
         jTextField80.setName("jTextField80"); // NOI18N
         jTextField80.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField80MouseClicked(evt);
             }
         });
 
         jButton73.setText(resourceMap.getString("jButton73.text")); // NOI18N
         jButton73.setName("jButton73"); // NOI18N
         jButton73.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton73MouseClicked(evt);
             }
         });
 
         jButton74.setText(resourceMap.getString("jButton74.text")); // NOI18N
         jButton74.setName("jButton74"); // NOI18N
         jButton74.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton74MouseClicked(evt);
             }
         });
 
         jScrollPane26.setName("jScrollPane26"); // NOI18N
 
         jTable13.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         jTable13.setName("jTable13"); // NOI18N
         jTable13.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable13MouseClicked(evt);
             }
         });
         jScrollPane26.setViewportView(jTable13);
         setJTableSearchBill(jTable13, 0);
 
         jLabel122.setText(resourceMap.getString("jLabel122.text")); // NOI18N
         jLabel122.setName("jLabel122"); // NOI18N
 
         jTextField81.setText(resourceMap.getString("jTextField81.text")); // NOI18N
         jTextField81.setName("jTextField81"); // NOI18N
 
         jLabel123.setText(resourceMap.getString("jLabel123.text")); // NOI18N
         jLabel123.setName("jLabel123"); // NOI18N
 
         jTextField82.setText(resourceMap.getString("jTextField82.text")); // NOI18N
         jTextField82.setName("jTextField82"); // NOI18N
 
         jTextField87.setText(resourceMap.getString("jTextField87.text")); // NOI18N
         jTextField87.setName("jTextField87"); // NOI18N
 
         jLabel98.setText(resourceMap.getString("jLabel98.text")); // NOI18N
         jLabel98.setName("jLabel98"); // NOI18N
 
         javax.swing.GroupLayout billingSearchLayout = new javax.swing.GroupLayout(billingSearch);
         billingSearch.setLayout(billingSearchLayout);
         billingSearchLayout.setHorizontalGroup(
             billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingSearchLayout.createSequentialGroup()
                 .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, billingSearchLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jScrollPane26, javax.swing.GroupLayout.DEFAULT_SIZE, 1165, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, billingSearchLayout.createSequentialGroup()
                         .addGap(31, 31, 31)
                         .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel123)
                             .addComponent(jLabel121)
                             .addComponent(jLabel122))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jTextField82)
                             .addComponent(jTextField81, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                             .addComponent(jTextField87, javax.swing.GroupLayout.Alignment.TRAILING))
                         .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addGroup(billingSearchLayout.createSequentialGroup()
                                 .addGap(76, 76, 76)
                                 .addComponent(jLabel98)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(jTextField79, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                                     .addComponent(jTextField80, javax.swing.GroupLayout.Alignment.TRAILING))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, billingSearchLayout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jButton73)
                                 .addGap(26, 26, 26)))
                         .addComponent(jButton74)
                         .addGap(163, 163, 163)))
                 .addContainerGap())
         );
         billingSearchLayout.setVerticalGroup(
             billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingSearchLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(billingSearchLayout.createSequentialGroup()
                         .addComponent(jTextField80, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jButton74)
                             .addComponent(jButton73)))
                     .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField79, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel98))
                         .addGroup(billingSearchLayout.createSequentialGroup()
                             .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jLabel121)
                                 .addComponent(jTextField87, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGap(18, 18, 18)
                             .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jTextField81, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel122))
                             .addGap(18, 18, 18)
                             .addGroup(billingSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jLabel123)
                                 .addComponent(jTextField82, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane26, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(70, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout mainBillingEditLayout = new javax.swing.GroupLayout(mainBillingEdit);
         mainBillingEdit.setLayout(mainBillingEditLayout);
         mainBillingEditLayout.setHorizontalGroup(
             mainBillingEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainBillingEditLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainBillingEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(billingCreation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(mainBillingEditLayout.createSequentialGroup()
                         .addComponent(jButton23)
                         .addGap(18, 18, 18)
                         .addComponent(jButton24)))
                 .addContainerGap())
             .addGroup(mainBillingEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(mainBillingEditLayout.createSequentialGroup()
                     .addGap(12, 12, 12)
                     .addComponent(billingSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGap(12, 12, 12)))
         );
         mainBillingEditLayout.setVerticalGroup(
             mainBillingEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainBillingEditLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainBillingEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton23)
                     .addComponent(jButton24))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(billingCreation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(178, Short.MAX_VALUE))
             .addGroup(mainBillingEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(mainBillingEditLayout.createSequentialGroup()
                     .addGap(53, 53, 53)
                     .addComponent(billingSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(353, Short.MAX_VALUE)))
         );
 
         jTabbedPane1.addTab("Fatturazione", mainBillingEdit);
         jTabbedPane1.addTab(resourceMap.getString("mainBillingEdit.TabConstraints.tabTitle"), billing, mainBillingEdit); // NOI18N
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1059, Short.MAX_VALUE)
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 833, Short.MAX_VALUE)
         );
 
         jTabbedPane1.setTitleAt(3, "Fatturazione");
         //setUPBill();
 
         menuBar.setName("menuBar"); // NOI18N
 
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(managgui.ManagGuiApp.class).getContext().getActionMap(ManagGuiView.class, this);
         jMenuItem1.setAction(actionMap.get("showSettingBox")); // NOI18N
         jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
         jMenuItem1.setName("jMenuItem1"); // NOI18N
         fileMenu.add(jMenuItem1);
 
         jMenuItem2.setAction(actionMap.get("showSetting2")); // NOI18N
         jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
         jMenuItem2.setName("jMenuItem2"); // NOI18N
         fileMenu.add(jMenuItem2);
 
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
                         .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 875, Short.MAX_VALUE)))
                 .addGap(12, 12, 12)
                 .addComponent(statusAnimationLabel)
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
                 .addContainerGap(29, Short.MAX_VALUE))
         );
 
         statusPanel.setSize(1030, 20);
 
         customerEdit.setBackground(resourceMap.getColor("customerEdit.background")); // NOI18N
         customerEdit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
         customerEdit.setName("customerEdit"); // NOI18N
 
         jScrollPane4.setName("jScrollPane4"); // NOI18N
 
         jTextArea2.setColumns(20);
         jTextArea2.setLineWrap(true);
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
 
         jButton15.setIcon(resourceMap.getIcon("jButton15.icon")); // NOI18N
         jButton15.setText(resourceMap.getString("jButton15.text")); // NOI18N
         jButton15.setName("jButton15"); // NOI18N
         jButton15.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton15MouseClicked(evt);
             }
         });
 
         jButton16.setIcon(resourceMap.getIcon("jButton16.icon")); // NOI18N
         jButton16.setText(resourceMap.getString("jButton16.text")); // NOI18N
         jButton16.setName("jButton16"); // NOI18N
         jButton16.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton16MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout customerEditLayout = new javax.swing.GroupLayout(customerEdit);
         customerEdit.setLayout(customerEditLayout);
         customerEditLayout.setHorizontalGroup(
             customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerEditLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerEditLayout.createSequentialGroup()
                         .addComponent(jLabel12)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel13)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField8, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE))
                     .addGroup(customerEditLayout.createSequentialGroup()
                         .addComponent(jLabel16)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE))
                     .addGroup(customerEditLayout.createSequentialGroup()
                         .addComponent(jLabel15)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField10, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerEditLayout.createSequentialGroup()
                         .addComponent(jLabel14)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField9, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerEditLayout.createSequentialGroup()
                         .addComponent(jButton16)
                         .addGap(18, 18, 18)
                         .addComponent(jButton15)))
                 .addContainerGap())
         );
         customerEditLayout.setVerticalGroup(
             customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerEditLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel12)
                     .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel13)
                     .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel14)
                     .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel15)
                     .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel16)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton15)
                     .addComponent(jButton16))
                 .addContainerGap())
         );
 
         jTextField10.setDocument(justNumbers);
 
         jButton19.setText(resourceMap.getString("jButton19.text")); // NOI18N
         jButton19.setName("jButton19"); // NOI18N
 
         customerSearch.setBackground(resourceMap.getColor("customerSearch.background")); // NOI18N
         customerSearch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         customerSearch.setName("customerSearch"); // NOI18N
 
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
 
         javax.swing.GroupLayout customerSearchLayout = new javax.swing.GroupLayout(customerSearch);
         customerSearch.setLayout(customerSearchLayout);
         customerSearchLayout.setHorizontalGroup(
             customerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerSearchLayout.createSequentialGroup()
                 .addGroup(customerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, customerSearchLayout.createSequentialGroup()
                             .addContainerGap()
                             .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE))
                         .addComponent(jButton22))
                     .addGroup(customerSearchLayout.createSequentialGroup()
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
         customerSearchLayout.setVerticalGroup(
             customerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerSearchLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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
 
         repairDetail.setBackground(resourceMap.getColor("repairDetail.background")); // NOI18N
         repairDetail.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         repairDetail.setName("repairDetail"); // NOI18N
         repairDetail.setPreferredSize(new java.awt.Dimension(948, 575));
 
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
         jTextArea8.setLineWrap(true);
         jTextArea8.setRows(5);
         jTextArea8.setName("jTextArea8"); // NOI18N
         jScrollPane10.setViewportView(jTextArea8);
 
         jLabel54.setText(resourceMap.getString("jLabel54.text")); // NOI18N
         jLabel54.setName("jLabel54"); // NOI18N
 
         jScrollPane11.setName("jScrollPane11"); // NOI18N
 
         jTextArea9.setColumns(20);
         jTextArea9.setLineWrap(true);
         jTextArea9.setRows(5);
         jTextArea9.setName("jTextArea9"); // NOI18N
         jScrollPane11.setViewportView(jTextArea9);
 
         jLabel55.setText(resourceMap.getString("jLabel55.text")); // NOI18N
         jLabel55.setName("jLabel55"); // NOI18N
 
         jScrollPane12.setName("jScrollPane12"); // NOI18N
 
         jTextArea10.setColumns(20);
         jTextArea10.setLineWrap(true);
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
 
         jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
         jLabel5.setName("jLabel5"); // NOI18N
 
         jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "---", "Ottimo", "Buono", "Discreto", "Rovinato", "Estr.nte rov.to" }));
         jComboBox2.setName("jComboBox2"); // NOI18N
 
         jCheckBox4.setBackground(resourceMap.getColor("jCheckBox4.background")); // NOI18N
         jCheckBox4.setText(resourceMap.getString("jCheckBox4.text")); // NOI18N
         jCheckBox4.setName("jCheckBox4"); // NOI18N
 
         jButton77.setText(resourceMap.getString("jButton77.text")); // NOI18N
         jButton77.setName("jButton77"); // NOI18N
         jButton77.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton77MouseClicked(evt);
             }
         });
 
         jButton78.setText(resourceMap.getString("jButton78.text")); // NOI18N
         jButton78.setName("jButton78"); // NOI18N
         jButton78.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton78MouseClicked(evt);
             }
         });
 
         jButton79.setIcon(new javax.swing.ImageIcon(getClass().getResource("/managgui/images/printer.png"))); // NOI18N
         jButton79.setText(resourceMap.getString("jButton79.text")); // NOI18N
         jButton79.setName("jButton79"); // NOI18N
         jButton79.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton79MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout repairDetailLayout = new javax.swing.GroupLayout(repairDetail);
         repairDetail.setLayout(repairDetailLayout);
         repairDetailLayout.setHorizontalGroup(
             repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(repairDetailLayout.createSequentialGroup()
                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(repairDetailLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repairDetailLayout.createSequentialGroup()
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(jLabel49)
                                 .addGap(10, 10, 10)
                                 .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jLabel5)
                                 .addGap(10, 10, 10)
                                 .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jCheckBox4))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(12, 12, 12)
                                 .addComponent(jLabel50)
                                 .addGap(12, 12, 12)
                                 .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jLabel51)
                                 .addGap(12, 12, 12)
                                 .addComponent(jTextField37, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(12, 12, 12)
                                 .addComponent(jCheckBox3)
                                 .addGap(8, 8, 8)
                                 .addComponent(jTextField38, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(362, 362, 362)
                                 .addComponent(jButton27)))
                         .addGap(18, 18, 18)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addComponent(jLabel45)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jLabel44))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(jLabel47)
                                     .addComponent(jLabel48))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(jTextField34)
                                     .addComponent(jTextField35, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(18, 18, 18)
                                 .addComponent(jLabel46)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField33)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton77)
                             .addComponent(jButton78)))
                     .addGroup(repairDetailLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel53))
                         .addGap(75, 75, 75)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel54)
                             .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(77, 77, 77)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel55)
                             .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(10, 10, 10))
                     .addGroup(repairDetailLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton35))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repairDetailLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jLabel52)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 202, Short.MAX_VALUE)
                         .addComponent(jButton26)
                         .addGap(22, 22, 22)
                         .addComponent(jButton79)
                         .addGap(18, 18, 18)
                         .addComponent(jButton25)))
                 .addContainerGap())
         );
         repairDetailLayout.setVerticalGroup(
             repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(repairDetailLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(repairDetailLayout.createSequentialGroup()
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton27))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel49)
                             .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel5)
                             .addComponent(jCheckBox4))
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(13, 13, 13)
                                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(repairDetailLayout.createSequentialGroup()
                                         .addGap(7, 7, 7)
                                         .addComponent(jLabel50))
                                     .addGroup(repairDetailLayout.createSequentialGroup()
                                         .addGap(5, 5, 5)
                                         .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGroup(repairDetailLayout.createSequentialGroup()
                                         .addGap(7, 7, 7)
                                         .addComponent(jLabel51))))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(18, 18, 18)
                                 .addComponent(jTextField37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(13, 13, 13)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(5, 5, 5)
                                 .addComponent(jCheckBox3))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(7, 7, 7)
                                 .addComponent(jTextField38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                     .addGroup(repairDetailLayout.createSequentialGroup()
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel45)
                             .addComponent(jLabel44))
                         .addGap(14, 14, 14)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel46)
                             .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(15, 15, 15)
                                 .addComponent(jLabel48))
                             .addGroup(repairDetailLayout.createSequentialGroup()
                                 .addGap(13, 13, 13)
                                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jButton77))))
                         .addGap(13, 13, 13)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel47)
                             .addComponent(jButton78))))
                 .addGap(18, 18, 18)
                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel53)
                     .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel55)
                         .addComponent(jLabel54)))
                 .addGap(6, 6, 6)
                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(repairDetailLayout.createSequentialGroup()
                         .addComponent(jButton35)
                         .addGap(18, 89, Short.MAX_VALUE)
                         .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel52)
                             .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(repairDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jButton25)
                         .addComponent(jButton26)
                         .addComponent(jButton79)))
                 .addContainerGap())
         );
 
         jPanel13.setBackground(resourceMap.getColor("jPanel13.background")); // NOI18N
         jPanel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
         jPanel13.setName("jPanel13"); // NOI18N
 
         jScrollPane13.setName("jScrollPane13"); // NOI18N
 
         jTextArea3.setColumns(20);
         jTextArea3.setLineWrap(true);
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
 
         jButton13.setIcon(resourceMap.getIcon("jButton13.icon")); // NOI18N
         jButton13.setText(resourceMap.getString("jButton13.text")); // NOI18N
         jButton13.setName("jButton13"); // NOI18N
         jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton13MouseClicked(evt);
             }
         });
 
         jButton14.setIcon(resourceMap.getIcon("jButton14.icon")); // NOI18N
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
                         .addComponent(jTextField13, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE))
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
                             .addComponent(jTextField14, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                         .addComponent(jLabel23)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                                 .addComponent(jButton14)
                                 .addGap(18, 18, 18)
                                 .addComponent(jButton13))
                             .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE))))
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
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton13)
                     .addComponent(jButton14))
                 .addContainerGap())
         );
 
         jTextField13.setDocument(justNumbers);
 
         warehouseEdit.setBackground(resourceMap.getColor("warehouseEdit.background")); // NOI18N
         warehouseEdit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         warehouseEdit.setName("warehouseEdit"); // NOI18N
         warehouseEdit.setPreferredSize(new java.awt.Dimension(380, 420));
 
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
         jTextArea4.setLineWrap(true);
         jTextArea4.setRows(5);
         jTextArea4.setName("jTextArea4"); // NOI18N
         jScrollPane14.setViewportView(jTextArea4);
 
         jTextField18.setName("jTextField18"); // NOI18N
 
         jTextField39.setName("jTextField39"); // NOI18N
 
         jButton28.setIcon(resourceMap.getIcon("jButton28.icon")); // NOI18N
         jButton28.setText(resourceMap.getString("jButton28.text")); // NOI18N
         jButton28.setAlignmentY(0.0F);
         jButton28.setName("jButton28"); // NOI18N
         jButton28.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton28MouseClicked(evt);
             }
         });
 
         jButton29.setIcon(resourceMap.getIcon("jButton29.icon")); // NOI18N
         jButton29.setText(resourceMap.getString("jButton29.text")); // NOI18N
         jButton29.setAlignmentY(0.0F);
         jButton29.setName("jButton29"); // NOI18N
         jButton29.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton29MouseClicked(evt);
             }
         });
 
         jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
         jLabel2.setName("jLabel2"); // NOI18N
 
         jTextField41.setText(resourceMap.getString("jTextField41.text")); // NOI18N
         jTextField41.setName("jTextField41"); // NOI18N
         jTextField41.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField41KeyReleased(evt);
             }
         });
 
         jTextField89.setText(resourceMap.getString("jTextField89.text")); // NOI18N
         jTextField89.setName("jTextField89"); // NOI18N
         jTextField89.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField89KeyReleased(evt);
             }
         });
 
         jLabel99.setText(resourceMap.getString("jLabel99.text")); // NOI18N
         jLabel99.setName("jLabel99"); // NOI18N
 
         javax.swing.GroupLayout warehouseEditLayout = new javax.swing.GroupLayout(warehouseEdit);
         warehouseEdit.setLayout(warehouseEditLayout);
         warehouseEditLayout.setHorizontalGroup(
             warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(warehouseEditLayout.createSequentialGroup()
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(warehouseEditLayout.createSequentialGroup()
                         .addGap(20, 20, 20)
                         .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel99)
                             .addComponent(jLabel59)
                             .addComponent(jLabel58)
                             .addComponent(jLabel57)
                             .addComponent(jLabel2)
                             .addComponent(jLabel56))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jTextField17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                             .addComponent(jTextField89, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                             .addComponent(jTextField41, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                             .addComponent(jTextField18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                             .addComponent(jTextField39, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                             .addComponent(jScrollPane14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)))
                     .addGroup(warehouseEditLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton28)
                         .addGap(18, 18, 18)
                         .addComponent(jButton29)))
                 .addGap(30, 30, 30))
         );
         warehouseEditLayout.setVerticalGroup(
             warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(warehouseEditLayout.createSequentialGroup()
                 .addGap(23, 23, 23)
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel56))
                 .addGap(18, 18, 18)
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel57)
                     .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel58)
                     .addComponent(jTextField39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(jTextField41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel99)
                     .addComponent(jTextField89, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(22, 22, 22)
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel59)
                     .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                 .addGroup(warehouseEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton28)
                     .addComponent(jButton29))
                 .addContainerGap())
         );
 
         jTextField39.setDocument(justNumbers);
         jTextField41.setDocument(new managgui.Price());
 
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
                         .addComponent(jTextField44, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                     .addGroup(jDialog1Layout.createSequentialGroup()
                         .addComponent(jLabel63)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
                     .addGroup(jDialog1Layout.createSequentialGroup()
                         .addComponent(jLabel64)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField40, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                         .addComponent(jLabel65)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField43, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
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
 
         customerInfoView.setBackground(resourceMap.getColor("customerInfoView.background")); // NOI18N
         customerInfoView.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
         customerInfoView.setName("customerInfoView"); // NOI18N
 
         jScrollPane16.setName("jScrollPane16"); // NOI18N
 
         jTextArea12.setColumns(20);
         jTextArea12.setEditable(false);
         jTextArea12.setLineWrap(true);
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
 
         jToggleButton1.setIcon(resourceMap.getIcon("jToggleButton1.icon")); // NOI18N
         jToggleButton1.setText(resourceMap.getString("jToggleButton1.text")); // NOI18N
         jToggleButton1.setName("jToggleButton1"); // NOI18N
         jToggleButton1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jToggleButton1MouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout customerInfoViewLayout = new javax.swing.GroupLayout(customerInfoView);
         customerInfoView.setLayout(customerInfoViewLayout);
         customerInfoViewLayout.setHorizontalGroup(
             customerInfoViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerInfoViewLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerInfoViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerInfoViewLayout.createSequentialGroup()
                         .addComponent(jLabel72)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField49, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel71)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField48, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                     .addGroup(customerInfoViewLayout.createSequentialGroup()
                         .addComponent(jLabel68)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
                     .addGroup(customerInfoViewLayout.createSequentialGroup()
                         .addComponent(jLabel69)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField46, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerInfoViewLayout.createSequentialGroup()
                         .addComponent(jLabel70)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField47, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE))
                     .addComponent(jToggleButton1, javax.swing.GroupLayout.Alignment.TRAILING))
                 .addContainerGap())
         );
         customerInfoViewLayout.setVerticalGroup(
             customerInfoViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerInfoViewLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerInfoViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel72)
                     .addComponent(jTextField49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel71)
                     .addComponent(jTextField48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(customerInfoViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel70)
                     .addComponent(jTextField47, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(customerInfoViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel69)
                     .addComponent(jTextField46, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(customerInfoViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel68)
                     .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jToggleButton1)
                 .addGap(18, 18, 18))
         );
 
         warehouseManagement.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         warehouseManagement.setName("warehouseManagement"); // NOI18N
         warehouseManagement.setPreferredSize(new java.awt.Dimension(860, 429));
 
         jLabel73.setFont(resourceMap.getFont("jLabel73.font")); // NOI18N
         jLabel73.setText(resourceMap.getString("jLabel73.text")); // NOI18N
         jLabel73.setName("jLabel73"); // NOI18N
 
         jLabel74.setText(resourceMap.getString("jLabel74.text")); // NOI18N
         jLabel74.setName("jLabel74"); // NOI18N
 
         jLabel75.setFont(resourceMap.getFont("jLabel75.font")); // NOI18N
         jLabel75.setText(resourceMap.getString("jLabel75.text")); // NOI18N
         jLabel75.setName("jLabel75"); // NOI18N
 
         jButton37.setIcon(resourceMap.getIcon("jButton37.icon")); // NOI18N
         jButton37.setText(resourceMap.getString("jButton37.text")); // NOI18N
         jButton37.setName("jButton37"); // NOI18N
         jButton37.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton37MouseClicked(evt);
             }
         });
 
         jButton38.setIcon(resourceMap.getIcon("jButton38.icon")); // NOI18N
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
 
         jTable6.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Codice Articolo", "Nome Generico", "Quantità Disponibile", "Prezzo Un.", "Prezzo Un. Ivato"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTable6.setName("jTable6"); // NOI18N
         jScrollPane1.setViewportView(jTable6);
 
         jScrollPane6.setName("jScrollPane6"); // NOI18N
 
         jTable7.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Codice Articolo", "Nome Generico", "Quantità Utilizzata", "Prezzo Un.", "Prezzo Un. Ivato"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTable7.setName("jTable7"); // NOI18N
         jScrollPane6.setViewportView(jTable7);
 
         jTextField90.setEditable(false);
         jTextField90.setText(resourceMap.getString("jTextField90.text")); // NOI18N
         jTextField90.setName("jTextField90"); // NOI18N
 
         jTextField91.setEditable(false);
         jTextField91.setText(resourceMap.getString("jTextField91.text")); // NOI18N
         jTextField91.setName("jTextField91"); // NOI18N
 
         jLabel100.setText(resourceMap.getString("jLabel100.text")); // NOI18N
         jLabel100.setName("jLabel100"); // NOI18N
 
         jLabel103.setText(resourceMap.getString("jLabel103.text")); // NOI18N
         jLabel103.setName("jLabel103"); // NOI18N
 
         javax.swing.GroupLayout warehouseManagementLayout = new javax.swing.GroupLayout(warehouseManagement);
         warehouseManagement.setLayout(warehouseManagementLayout);
         warehouseManagementLayout.setHorizontalGroup(
             warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(warehouseManagementLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel73, javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, warehouseManagementLayout.createSequentialGroup()
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton38)
                             .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, warehouseManagementLayout.createSequentialGroup()
                                     .addComponent(jLabel74)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jTextField50, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, warehouseManagementLayout.createSequentialGroup()
                                     .addComponent(jLabel75)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jLabel76))))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, warehouseManagementLayout.createSequentialGroup()
                                 .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(jButton43, 0, 0, Short.MAX_VALUE)
                                     .addComponent(jButton42, 0, 0, Short.MAX_VALUE)
                                     .addComponent(jButton44, 0, 0, Short.MAX_VALUE)
                                     .addComponent(jButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel103)
                                     .addComponent(jLabel100, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                                     .addComponent(jTextField90, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                                     .addComponent(jTextField91, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)))
                             .addComponent(jButton37, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jButton39, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, warehouseManagementLayout.createSequentialGroup()
                                 .addComponent(jLabel77)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField51, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                 .addContainerGap())
         );
         warehouseManagementLayout.setVerticalGroup(
             warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(warehouseManagementLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(warehouseManagementLayout.createSequentialGroup()
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel74)
                             .addComponent(jTextField50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton36))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(warehouseManagementLayout.createSequentialGroup()
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel77)
                             .addComponent(jTextField51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel73)
                 .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(warehouseManagementLayout.createSequentialGroup()
                         .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel75)
                             .addComponent(jLabel76)))
                     .addGroup(warehouseManagementLayout.createSequentialGroup()
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel100)
                             .addComponent(jButton40))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextField90, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(warehouseManagementLayout.createSequentialGroup()
                                 .addComponent(jButton42)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jButton43))
                             .addGroup(warehouseManagementLayout.createSequentialGroup()
                                 .addComponent(jLabel103)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField91, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                 .addGap(18, 18, 18)
                 .addGroup(warehouseManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton37)
                     .addComponent(jButton38))
                 .addGap(49, 49, 49))
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
 
         billingCustomerEdit.setBackground(resourceMap.getColor("billingCustomerEdit.background")); // NOI18N
         billingCustomerEdit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
         billingCustomerEdit.setName("billingCustomerEdit"); // NOI18N
 
         jScrollPane18.setName("jScrollPane18"); // NOI18N
 
         jTextArea13.setColumns(20);
         jTextArea13.setLineWrap(true);
         jTextArea13.setRows(5);
         jTextArea13.setName("jTextArea13"); // NOI18N
         jScrollPane18.setViewportView(jTextArea13);
 
         jLabel82.setText(resourceMap.getString("jLabel82.text")); // NOI18N
         jLabel82.setName("jLabel82"); // NOI18N
 
         jLabel84.setText(resourceMap.getString("jLabel84.text")); // NOI18N
         jLabel84.setName("jLabel84"); // NOI18N
 
         jTextField59.setName("jTextField59"); // NOI18N
 
         jTextField60.setName("jTextField60"); // NOI18N
 
         jLabel85.setText(resourceMap.getString("jLabel85.text")); // NOI18N
         jLabel85.setName("jLabel85"); // NOI18N
 
         jTextField61.setName("jTextField61"); // NOI18N
 
         jTextField62.setName("jTextField62"); // NOI18N
 
         jLabel86.setText(resourceMap.getString("jLabel86.text")); // NOI18N
         jLabel86.setName("jLabel86"); // NOI18N
 
         jLabel87.setText(resourceMap.getString("jLabel87.text")); // NOI18N
         jLabel87.setName("jLabel87"); // NOI18N
 
         jButton56.setIcon(resourceMap.getIcon("jButton56.icon")); // NOI18N
         jButton56.setText(resourceMap.getString("jButton56.text")); // NOI18N
         jButton56.setName("jButton56"); // NOI18N
         jButton56.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton56MouseClicked(evt);
             }
         });
 
         jButton57.setIcon(resourceMap.getIcon("jButton57.icon")); // NOI18N
         jButton57.setText(resourceMap.getString("jButton57.text")); // NOI18N
         jButton57.setName("jButton57"); // NOI18N
         jButton57.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton57MouseClicked(evt);
             }
         });
 
         jLabel88.setText(resourceMap.getString("jLabel88.text")); // NOI18N
         jLabel88.setName("jLabel88"); // NOI18N
 
         jTextFieldCF.setText(resourceMap.getString("jTextFieldCF.text")); // NOI18N
         jTextFieldCF.setName("jTextFieldCF"); // NOI18N
 
         jLabel89.setText(resourceMap.getString("jLabel89.text")); // NOI18N
         jLabel89.setName("jLabel89"); // NOI18N
 
         jTextFieldIVA.setText(resourceMap.getString("jTextFieldIVA.text")); // NOI18N
         jTextFieldIVA.setName("jTextFieldIVA"); // NOI18N
 
         javax.swing.GroupLayout billingCustomerEditLayout = new javax.swing.GroupLayout(billingCustomerEdit);
         billingCustomerEdit.setLayout(billingCustomerEditLayout);
         billingCustomerEditLayout.setHorizontalGroup(
             billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingCustomerEditLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, billingCustomerEditLayout.createSequentialGroup()
                         .addComponent(jButton57)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton56))
                     .addGroup(billingCustomerEditLayout.createSequentialGroup()
                         .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel84)
                             .addComponent(jLabel82))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                             .addComponent(jTextField59, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)))
                     .addGroup(billingCustomerEditLayout.createSequentialGroup()
                         .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel85)
                             .addComponent(jLabel89)
                             .addComponent(jLabel88)
                             .addComponent(jLabel87))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jTextField60, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                             .addComponent(jTextFieldCF)
                             .addGroup(billingCustomerEditLayout.createSequentialGroup()
                                 .addComponent(jTextField62, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jLabel86)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextField61, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jTextFieldIVA))))
                 .addContainerGap())
         );
         billingCustomerEditLayout.setVerticalGroup(
             billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingCustomerEditLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel87)
                     .addComponent(jTextField62, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField61, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel86))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel88)
                     .addComponent(jTextFieldCF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel89)
                     .addComponent(jTextFieldIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel85)
                     .addComponent(jTextField60, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel84)
                     .addComponent(jTextField59, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel82)
                     .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                 .addGroup(billingCustomerEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton56)
                     .addComponent(jButton57))
                 .addContainerGap())
         );
 
         jTextField13.setDocument(justNumbers);
 
         billingCustomerEdit.getAccessibleContext().setAccessibleName(resourceMap.getString("billingCustomer.AccessibleContext.accessibleName")); // NOI18N
 
         billingCustomerSearch.setBackground(resourceMap.getColor("billingCustomerSearch.background")); // NOI18N
         billingCustomerSearch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         billingCustomerSearch.setName("billingCustomerSearch"); // NOI18N
 
         jLabel90.setText(resourceMap.getString("jLabel90.text")); // NOI18N
         jLabel90.setName("jLabel90"); // NOI18N
 
         jTextField65.setText(resourceMap.getString("jTextField65.text")); // NOI18N
         jTextField65.setName("jTextField65"); // NOI18N
         jTextField65.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField65MouseClicked(evt);
             }
         });
 
         jTextField66.setText(resourceMap.getString("jTextField66.text")); // NOI18N
         jTextField66.setName("jTextField66"); // NOI18N
         jTextField66.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTextField66MouseClicked(evt);
             }
         });
 
         jButton58.setText(resourceMap.getString("jButton58.text")); // NOI18N
         jButton58.setName("jButton58"); // NOI18N
         jButton58.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton58MouseClicked(evt);
             }
         });
 
         jButton59.setText(resourceMap.getString("jButton59.text")); // NOI18N
         jButton59.setName("jButton59"); // NOI18N
         jButton59.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton59MouseClicked(evt);
             }
         });
 
         jButton60.setIcon(resourceMap.getIcon("jButton60.icon")); // NOI18N
         jButton60.setText(resourceMap.getString("jButton60.text")); // NOI18N
         jButton60.setName("jButton60"); // NOI18N
         jButton60.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton60MouseClicked(evt);
             }
         });
 
         jScrollPane22.setName("jScrollPane22"); // NOI18N
 
         jTable9.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         jTable9.setName("jTable9"); // NOI18N
         jTable9.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable9MouseClicked(evt);
             }
         });
         jScrollPane22.setViewportView(jTable9);
         setJTableClient(jTable2, 0);
 
         javax.swing.GroupLayout billingCustomerSearchLayout = new javax.swing.GroupLayout(billingCustomerSearch);
         billingCustomerSearch.setLayout(billingCustomerSearchLayout);
         billingCustomerSearchLayout.setHorizontalGroup(
             billingCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingCustomerSearchLayout.createSequentialGroup()
                 .addGroup(billingCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, billingCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, billingCustomerSearchLayout.createSequentialGroup()
                             .addContainerGap()
                             .addComponent(jScrollPane22, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE))
                         .addComponent(jButton60))
                     .addGroup(billingCustomerSearchLayout.createSequentialGroup()
                         .addGap(31, 31, 31)
                         .addComponent(jLabel90)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField65, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jTextField66, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(39, 39, 39)
                         .addComponent(jButton58)
                         .addGap(18, 18, 18)
                         .addComponent(jButton59)))
                 .addContainerGap())
         );
         billingCustomerSearchLayout.setVerticalGroup(
             billingCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(billingCustomerSearchLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(billingCustomerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel90)
                     .addComponent(jTextField65, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField66, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton58)
                     .addComponent(jButton59))
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane22, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addComponent(jButton60)
                 .addContainerGap())
         );
 
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
         
     if(!checkCustomerSearch(surname, name)) { // info not inserted properly
         showWinAlert(customerSearch, "Valori errati, riprovare...", "Error", JOptionPane.ERROR_MESSAGE);
         jTable5.setVisible(false);
     } else { // search can be executed
         this.customerSearchResult(name, surname, false); // execute the operation and
             
         if(this.customerRet != null) {
             setTableCustomerData(jTable5, this.customerRet);
             jTable5.setVisible(true);
         } else {
             jTable5.setVisible(false);
         }
     }
 }//GEN-LAST:event_jButton3MouseClicked
 
     private void jTextField3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField3MouseClicked
         if(jTextField3.getText().equals("Codice o Nome Articolo"))
             jTextField3.setText(null);
     }//GEN-LAST:event_jTextField3MouseClicked
 
     // warehouse search reset
     private void jButton8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton8MouseClicked
         jTextField3.setText("Codice o Nome Articolo");
         jTable1.setVisible(false);
     }//GEN-LAST:event_jButton8MouseClicked
 
     private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
         jPanel5.setVisible(true);
         jPanel6.setVisible(false);
         jTextField3.setText("Codice o Nome Articolo");
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
                 jTable1.setVisible(true); // this was out of the if/else
             } else {
                 showWinAlert(customerSearch, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
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
         ComClasses.Request r = new ComClasses.Request(w, ComClasses.Constants.WARE, ComClasses.Constants.SELECT, SharedClasses.Warehouse.selectSerialName());
         
         try {
             this.warehouseRet = Utils.arrayOperation(r);
         } catch (Exception e) {
             showWinAlert(customerSearch, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // set warehouse data into jTable
     private static void setTableWarehouseData (JTable t, ArrayList<Object> a) {
         
         int n = a.size();
         SharedClasses.Warehouse w = null;
         setJTableWarehouse(t, n);
         
         for(int i = 0; i < n; i++) { // take the infos from every warehouse object
             w = (SharedClasses.Warehouse) a.get(i);
             t.setValueAt(w.getSerial(), i, 0);
             t.setValueAt(w.getName(), i, 1);
             t.setValueAt(w.getAvailability(), i, 2);
             t.setValueAt(w.getNoIvaPrice(), i, 3);
             t.setValueAt(w.getUnitPrice(), i, 4);
             t.setValueAt(w.getNote(), i, 5);
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
        
         resetValScheda();
         
         jPanel9.setVisible(false);
                
     }//GEN-LAST:event_jButton17MouseClicked
 
     // repair INSERT
     private void jButton18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton18MouseClicked
         
         int flagError = 0;
 
         if(jComboBox3.getSelectedIndex() == 0){
             showWinAlert(jPanel9, "Selezionare una tipologia.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jComboBox1.getSelectedIndex() == 0){
             showWinAlert(jPanel9, "Selezionare valore per estetica dispositivo.", "Warning", JOptionPane.WARNING_MESSAGE);
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
             
             this.handleDevice(); // do proper operations on device info
 
             SharedClasses.Repair rep = new SharedClasses.Repair(this.c, this.d, jTextField21.getText(), jTextField26.getText());
             ComClasses.Request req = new ComClasses.Request(rep, ComClasses.Constants.REPAIR, ComClasses.Constants.INSERT, rep.insert());
 
             try {
                 // TODO exception if the value from the server is an exception
                 int id = Utils.intOperation(req).intValue();
                 rep.setID(id);
 
                 SharedClasses.Details det = new SharedClasses.Details(rep, jComboBox1.getSelectedIndex(), jTextArea5.getText()); // create Details object
                 req = new ComClasses.Request(det, ComClasses.Constants.DETAILS, ComClasses.Constants.INSERT, det.insert());
                 // TODO manage return value
                 Utils.intOperation(req); // the INSERT for Repair and Details will become effective at the same time
             
                 int n = JOptionPane.showConfirmDialog(jPanel9, "Scheda prodotto n° " + id + "\nStampare la ricevuta?", "Nuova Scheda prodotto aperta", JOptionPane.YES_NO_OPTION);
             
                 if(n == JOptionPane.YES_OPTION) {
             
                     try {
                         Print.repairPrint(id, this.c, rep, this.d, det, true);
                     } catch (Exception e) {
                         showWinAlert(jPanel9, e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                     }
                 
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
         
             if(v == ComClasses.Constants.RET_EXI) { // the device already exists: it's necessary to get the id
                 r = new ComClasses.Request(this.d, ComClasses.Constants.DEVICE, ComClasses.Constants.FIELDSELECT, this.d.selectSerial());
                 v = Utils.intOperation(r).intValue();
 
                 if(v == ComClasses.Constants.RET_EXC) // exception occourred
                     showWinAlert(customerSearch, "Eccezione occorsa durante l'operazione: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 else
                     this.d.setID(v); // v now contains the existing id
 
             } else {
                 this.d.setID(v); // v already contains the generated id
             }
         } catch (Exception e) {
             showWinAlert(customerSearch, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
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
 
         //dato zozzo nn capisco da dove lo prende quindi lo forzo
         jLabel5.setText("Estetica dispositivo:");
         if(!checkInsertion(jTextField27, jTextField28, jTextField29, jTextField30)) {
             showWinAlert(jPanel10, "Inserire almeno un campo.", "Error", JOptionPane.ERROR_MESSAGE);
             jTable3.setVisible(false);
         } else {
             this.repairSearchResult();
             
             if(this.repairRet != null) {
                 setTableRepairData(jTable3, this.repairRet);
                 jTable3.setVisible(true);
             } else {
                 showWinAlert(customerSearch, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
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
             showWinAlert(customerSearch, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
     
     }
     
     // insert repair details into jtable
     private static void setTableRepairData(JTable t, ArrayList<Object> a) {
         
         int n = a.size();
         SharedClasses.RepairResponse r = null;
         setJTableRepair(t, n);
         
         for(int i = 0; i < n; i++) { // take the infos for every search result
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
             if(!f[i].getText().equals("")) // if one field is not empty, it's ok
                 return true;
         
         return false; // all fields are empty
         
     }
     
     // check if every field is not empty
     private static boolean neededInsertion (JTextField ... f) {
         
         for(int i = 0; i < f.length; i++)
             if(f[i].getText().equals(""))
                 return false;
         
         return true;
         
     }
 
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
         
         if(!checkCustomerSearch(surname, name)) { // info not inserted properly
             showWinAlert(customerSearch, "Valori errati, riprovare...", "Error", JOptionPane.ERROR_MESSAGE);
             jTable2.setVisible(false);
         } else { // search can be executed
             this.customerSearchResult(name, surname, false); // execute the operation and
             
             if(this.customerRet != null) {
                 setTableCustomerData(jTable2, this.customerRet);
                 jTable2.setVisible(true); // this was out of the if/else
             } else {
                 showWinAlert(customerSearch, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 jTable2.setVisible(false);
             }
             
         }
     }//GEN-LAST:event_jButton20MouseClicked
 
     // check if the fields for customer search have been properly modified
     private static boolean checkCustomerSearch (String a, String b) {
         
         if(!a.equals("Cognome")) { // if a is equals to "Cognome" no fields have been edited
             if(!a.equals("") || !b.equals("")) // if a or b are not equals to the empty string, it's ok
                 return true;
             
             return false; // a and b are equals to the empty string
         }
         return false; // fields have not been edited
     }
     
     // insert the customer data into jTable
     private static void setTableCustomerData (JTable t, ArrayList<Object> a) {
         
         int n = a.size();
         SharedClasses.Customer c = null;
         setJTableClient(t, n);
         
         for(int i = 0; i < n; i++) { // take infos from every customer object
             c = (SharedClasses.Customer) a.get(i);
             t.setValueAt(c.getSurname(), i, 0);
             t.setValueAt(c.getName(), i, 1);
             t.setValueAt(c.getAddress(), i, 2);
             t.setValueAt(c.getTelephone(), i, 3);
             t.setValueAt(c.getNote(), i, 4);
         }
         
     }
     
     // send the search request for Customer class and read the response
     private void customerSearchResult (String a, String b, boolean bill) {
         
         SharedClasses.Customer cus = new SharedClasses.Customer(a, b);
         ComClasses.Request r = new ComClasses.Request(cus, ComClasses.Constants.CUSTOMER, ComClasses.Constants.SELECT, cus.select());
         
         customerArrayFill(bill, r);
         
     }
     
     private void customerArrayFill (boolean bill, ComClasses.Request r) {
         
         try {
         
             if(bill)
                 this.billingCustomerRet = Utils.arrayOperation(r);
             else
                 this.customerRet = Utils.arrayOperation(r);
         
        } catch (SharedClasses.MyDBException e) {
            showWinAlert(customerSearch, "Nessun cliente soddisfa le condizioni di ricerca.", "Cliente inesistente", JOptionPane.WARNING_MESSAGE);    
         } catch (Exception e) {
             showWinAlert(customerSearch, Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     // send the search request for BillingCustomer class and read the response
     private void billingCustomerSearchResult (int id) {
         
         SharedClasses.BillingCustomer cus = new SharedClasses.BillingCustomer(id, null, null);
         ComClasses.Request r = new ComClasses.Request(cus, ComClasses.Constants.BILLCUS, ComClasses.Constants.SELECT, SharedClasses.BillingCustomer.select());
         
         try {
             this.billingCustomerInfo = (SharedClasses.BillingCustomer)Utils.arrayOperation(r).get(0);
         } catch (SharedClasses.MyDBException e) {
             if(e.getCode() != ComClasses.Constants.DBNULL) {
                 this.billingCustomerInfo = null;
                 showWinAlert(billingCustomerSearch, Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
             }
         } catch (Exception e) {
             this.billingCustomerInfo = null;
             showWinAlert(billingCustomerSearch, Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
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
 
         // UPDATE device of the selected repair
         if(!neededInsertion(jTextField36, jTextField37)) {
             // compulsory fields leaved empty
             showWinAlert(repairDetail, "Controllare modello ed imei", "Errore", JOptionPane.ERROR_MESSAGE);
         
         } else {
             // handle device update
             SharedClasses.Device md = new SharedClasses.Device(jTextField36.getText(), jComboBox5.getSelectedIndex(), jTextField37.getText());
             if(this.checkDeviceChange(md))
                 this.updateDevice(md);
             
             // UPDATE details of the selected repair
             SharedClasses.Details mdev = new SharedClasses.Details(this.re.getID(), jComboBox2.getSelectedIndex(), jTextField34.getText(), jTextArea8.getText(), jTextArea9.getText(), jTextArea10.getText());
             if(this.checkDetailsChange(mdev))
                 this.updateDetails(mdev);
 
             // UPDATE repair table of the database
             SharedClasses.Repair mrep = new SharedClasses.Repair(this.re.getID(), jTextField38.getText(), jTextField35.getText(), (Integer)jComboBox6.getSelectedIndex());
             if(this.checkRepairChange(mrep))
                 this.updateRepair(mrep);
 
             schedaProdotto.dispose();
             jTable3.setVisible(false);
             clearFields(jTextField27, jTextField28, jTextField29, jTextField30);
         }
         
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
     
     // check if an update is needed for the details of the selected repair
     private boolean checkDetailsChange (SharedClasses.Details det) {
         
         if(this.de.getCondition() != det.getCondition())
             return true;
         
         String start = this.de.getDateStart();
         if(start != null) {
             if(!start.equals(det.getDateStart()))
                 return true;
         } else {
             if(!det.getDateStart().equals(""))
                 return true;
         }
         
         String declared = this.de.getDeclared();
         String newDeclared = det.getDeclared();
         if(!newDeclared.equals("") && !newDeclared.equals(declared))
             return true;
         
         String found = this.de.getFound();
         if(found != null) {
             if(!found.equals(det.getFound()))
                 return true;
         } else {
             if(!det.getFound().equals(""))
                 return true;
         }
         
         String done = this.de.getDone();
         if(done != null) {
             if(!done.equals(det.getDone()))
                 return true;
         } else {
             if(!det.getDone().equals(""))
                 return true;
         }
         
         return false;
         
     }
     
     private boolean checkRepairChange (SharedClasses.Repair rep) {
         
         String optional = this.re.getOptional();
         if(optional != null) {
             if(!optional.equals(rep.getOptional()))
                 return true;
         } else {
             if(!rep.getOptional().equals(""))
                 return true;
         }
         
         String out = this.re.getDateOut();
         if(out != null) {
             if(!out.equals(rep.getDateOut()))
                 return true;
         } else {
             if(!rep.getDateOut().equals(""))
                 return true;
         }
         
         if(this.re.getStatus() != rep.getStatus())
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
             showWinAlert(repairDetail, Client.Utils.exceptionMessage(e), "Errore", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     private void updateDetails (SharedClasses.Details det) {
         
         ComClasses.Request req = new ComClasses.Request(det, ComClasses.Constants.DETAILS, ComClasses.Constants.UPDATE, this.de.update(det));
         
         try {
             Utils.intOperation(req);
         } catch (Exception e) {
             showWinAlert(repairDetail, Client.Utils.exceptionMessage(e), "Errore", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     private void updateRepair (SharedClasses.Repair rep) {
         
         ComClasses.Request req = new ComClasses.Request(rep, ComClasses.Constants.REPAIR, ComClasses.Constants.UPDATE, this.re.update(rep));
      
         try {
             Utils.intOperation(req);
         } catch (Exception e) {
             showWinAlert(repairDetail, Client.Utils.exceptionMessage(e), "Errore", JOptionPane.ERROR_MESSAGE);
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
             
             schedaProdotto = new FinestraSwing("Scheda riparazione n° " + this.re.getID(), p.getPX(), p.getPY(), repairDetail.getPreferredSize().width, repairDetail.getPreferredSize().height, repairDetail);
             jLabel44.setText(Integer.toString(this.re.getID()));
             
             getDataDbPracticeView();
             noEditablePracticeValue();
         }
     }//GEN-LAST:event_jTable3MouseClicked
 
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
         if(jTextField52.getText().equals("")){
             showWinAlert(jPanel6, "Inserire il prezzo unitario ivato.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField88.getText().equals("")){
             showWinAlert(jPanel6, "Inserire il prezzo unitario, iva esclusa.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){
             int ret = setArticleWarehouseDB(jTextField4, jTextField5, jTextField6, jTextField52, jTextArea1, jTextField88);
             // if the operation succeedes, hide the JPanel
             if(ret != ComClasses.Constants.RET_EXC && ret != ComClasses.Constants.RET_EXI)
                 jPanel6.setVisible(false);
         }
         
     }//GEN-LAST:event_jButton9MouseClicked
 
     // warehouse UPDATE
     private void jButton28MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton28MouseClicked
         
         int flagError = 0;
         
         if(jTextField17.getText().equals("")){
             showWinAlert(warehouseEdit, "Inserire il codice articolo.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField39.getText().equals("")){
             showWinAlert(warehouseEdit, "Inserire la quantita' dell'articolo.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField41.getText().equals("")){
             showWinAlert(warehouseEdit, "Inserire il prezzo unitario ivato.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         if(jTextField89.getText().equals("")){
             showWinAlert(warehouseEdit, "Inserire il prezzo unitario senza iva.", "Warning", JOptionPane.WARNING_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){
             this.updateSparePart();
             schedaArticoloMagazzino.dispose();
             jTextField3.setText("Codice o Nome Articolo");
             jTable1.setVisible(false);
         }
     }//GEN-LAST:event_jButton28MouseClicked
 
     private void updateSparePart () {
         
         SharedClasses.Warehouse w = new SharedClasses.Warehouse(this.sp.getSerial(), jTextField17.getText(), jTextField18.getText(), Integer.parseInt(jTextField39.getText()), jTextField41.getText(), jTextArea4.getText(), jTextField89.getText());
         ComClasses.Request req = new ComClasses.Request(w, ComClasses.Constants.WARE, ComClasses.Constants.UPDATE, this.sp.update(w));
    
         try {
             // TODO exception if the value from the server is an exception
             if(Utils.intOperation(req).intValue() != 1)
                 showWinAlert(warehouseEdit, "Modifica non riuscita. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
             
             this.sp = null;
         
         } catch (Exception e) {
             showWinAlert(warehouseEdit, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
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
             this.sp = (SharedClasses.Warehouse)this.warehouseRet.get(jTable1.getSelectedRow());
             schedaArticoloMagazzino = new FinestraSwing("Scheda Articolo Magazzino", p.getPX(), p.getPY(), warehouseEdit.getPreferredSize().width, warehouseEdit.getPreferredSize().height, warehouseEdit);
             getValArticleWarehouse(jTextField17, jTextField18, jTextField39, jTextField41, jTextArea4, jTextField89);
         }
     }//GEN-LAST:event_jTable1MouseClicked
 
     private void jButton27MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton27MouseClicked
         // creazione view dati cliente NON MODIFICABILI!
         setCenterMonitorDim(503, 300);
         DatiClienteSimply = new FinestraSwing("Dati Cliente", p.getPX(), p.getPY(), 503, 300, customerInfoView);
         getDatiClienteDb(jTextField48, jTextField49, jTextField47, jTextField46, jTextArea12, false);
     }//GEN-LAST:event_jButton27MouseClicked
 
     // open usage edit window
     private void jButton35MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton35MouseClicked
         int width17 = warehouseManagement.getPreferredSize().width;
         int height17 = warehouseManagement.getPreferredSize().height;
         setCenterMonitorDim(width17, height17);
         pezziUtilizzati = new FinestraSwing("Selezionare i pezzi utilizzati per la lavorazione!", p.getPX(), p.getPY(), width17, height17, warehouseManagement);
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
             BigDecimal tot = new BigDecimal(0);
 
             for(int i = 0; i < n; i += 2) {                                     // i + 1 contains the name of the spare part
                 u = (SharedClasses.Usage) this.usageRet.get(i);
                 this.old.add(new SharedClasses.UsageCache(u));
                 String aux = (String) this.usageRet.get(i+1);
                 String [] arrayString = aux.split("/");
                 BigDecimal a = new BigDecimal(u.getUsed());
                 BigDecimal b = new BigDecimal(arrayString[1]);
                 model.addRow(new Object[]{u.getSerial(), arrayString[0], u.getUsed(), b, arrayString[2]});
                 tot = tot.add(a.multiply(b));
             }
             
             jTextField90.setText(tot.toString());
             jTextField91.setText(handleIVA(jTextField90.getText(), this.warehouseInfo.getIVA(), true));
             
         } catch (Exception e) {
             showWinAlert(warehouseManagement, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
 
     }
     
     // delete rows from a table
     private static void tableRowDelete (DefaultTableModel model) {
         
         int o = model.getRowCount();
             
             for(int i = 0; i < o; i++)
                 model.removeRow(0); // delete the previous content
         
     }
     
     // warehouse SELECT into usage editing
     private void jButton36MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton36MouseClicked
         
         String ins = jTextField50.getText();
         
         if(ins.equals(""))
             showWinAlert(warehouseManagement, "Inserire un nome generico\noppure un codice articolo.", "Error Code", JOptionPane.ERROR_MESSAGE);
         else {
             SharedClasses.Warehouse aux = new SharedClasses.Warehouse(ins);
             ComClasses.Request wr = new ComClasses.Request(aux, ComClasses.Constants.WARE, ComClasses.Constants.SELECT, SharedClasses.Warehouse.selectSerialName());
             
             try {
                 this.warehouseRet = Utils.arrayOperation(wr);
                 setUsageWarehouseTable(jTable6, this.warehouseRet);
             } catch (Exception e) {
                 showWinAlert(warehouseManagement, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
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
         
         for(int i = 0; i < n; i++) { // take the infos from every warehouse object
             w = (SharedClasses.Warehouse) a.get(i);
             serial = w.getSerial();
             t.setValueAt(serial, i, 0);
             t.setValueAt(w.getName(), i, 1);
             
             x = this.cacheFindSparePart(serial);
             if(x != -1) // spare part usage modifed
                 delta = this.cache.get(x).getDelta();
             else        // spare part usage not modified
                 delta = 0;
             
             t.setValueAt(w.getAvailability() - delta, i, 2);
             t.setValueAt(w.getNoIvaPrice(), i, 3);
             t.setValueAt(w.getUnitPrice(), i, 4);
         }
         
     }
     
     private void jButton41MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton41MouseClicked
         // reset valori scheda accettazione
         resetValScheda();
     }//GEN-LAST:event_jButton41MouseClicked
 
     // add spare part to Usage
     private void jButton39MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton39MouseClicked
         // importa pezzo di ricambio su scheda
         int sel = jTable6.getSelectedRow();
         String req = jTextField51.getText();
         
         if(sel != -1){
             
             if(req.equals("")) // no quantity has been inserted
                 showWinAlert(warehouseManagement, "Inserire una quantità.", "Warning", JOptionPane.WARNING_MESSAGE);
             
             if(checkQuantity(req, jTable6, sel)) {                                  // check if the quantity inserted it's ok
                 importArticle(sel, req);                                            // do something to handle the situation
                 updateTotal(jTable7);
             } else {
                 showWinAlert(warehouseManagement, "Quantità pezzo disponibile insufficiente o quantità richiesta non corretta.", "Error", JOptionPane.ERROR_MESSAGE);
             }
             
         } else
             showWinAlert(warehouseManagement, "Selezionare un pezzo prima di importalo.", "Warning Selection", JOptionPane.WARNING_MESSAGE);
     }//GEN-LAST:event_jButton39MouseClicked
 
     // set the JTextFields for the total price of repair spare parts
     private void updateTotal (JTable t) {
         
         DefaultTableModel model = (DefaultTableModel)t.getModel();
         int n = model.getRowCount();
         BigDecimal tot = new BigDecimal(0);
         
         for(int i = 0; i < n; i++) {
             BigDecimal a = new BigDecimal((Integer)t.getValueAt(i, 2));
             tot = tot.add(a.multiply((BigDecimal)t.getValueAt(i, 3)));
         }
         
         jTextField90.setText(tot.setScale(2, RoundingMode.HALF_DOWN).toString());
         jTextField91.setText(handleIVA(jTextField90.getText(), this.warehouseInfo.getIVA(), true));
         
     }
     
     // check the requested quantity for a specific spare part
     private boolean checkQuantity (String s, JTable t, int r) {
         
         int req = Integer.parseInt(s);
         int ava = (Integer)t.getValueAt(r, 2);
         
         if(req <= 0) // requested quantity is negative or equal to 0
             return false;
         
         if(req > ava) // requested quantity is greater than availability
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
 
                     showWinAlert(warehouseManagement, "Impossibile aggiungere pezzo: quantità non sufficiente.", "Warning Selection", JOptionPane.WARNING_MESSAGE);
 
                 } else {
 
                     if(i != -1)
                         this.cache.get(i).increaseDelta();
                     else
                         this.cache.add(new SharedClasses.UsageCache(serial, 1));
 
                     int v = (Integer)jTable7.getValueAt(sel, 2);
 
                     jTable7.setValueAt(v + 1, sel, 2);
                     
                     int f = this.sparePartSearchResultFind(serial);
                     if(f != -1)
                         jTable6.setValueAt((Integer)jTable6.getValueAt(f, 2) - 1, f, 2);
                     
                     this.updateTotal(jTable7);
 
                 }
                 
             } catch (Exception e) {
                 showWinAlert(warehouseManagement, Client.Utils.exceptionMessage(e), "Warning Selection", JOptionPane.WARNING_MESSAGE);
             }
             
         } else
             showWinAlert(warehouseManagement, "Selezionare prima un pezzo\nper aumentarne la quantità!", "Warning Selection", JOptionPane.WARNING_MESSAGE);
         
         
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
                 
                 int f = this.sparePartSearchResultFind(serial);
                 if(f != -1)
                     jTable6.setValueAt((Integer)jTable6.getValueAt(f, 2) + 1, f, 2);
                 
                 this.updateTotal(jTable7);
 
             }
             
         } else
             showWinAlert(warehouseManagement, "Selezionare prima un pezzo\nper diminuirne la quantità!", "Warning Selection", JOptionPane.WARNING_MESSAGE);
         
     }//GEN-LAST:event_jButton44MouseClicked
     
     // delete a spare part from usage
     private void jButton42MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton42MouseClicked
 
         int sel = jTable7.getSelectedRow();
         
          if(sel != -1){
   
             int n = JOptionPane.showConfirmDialog(warehouseManagement, "Cancellare il pezzo selezionato?", "Cancellazione info", JOptionPane.YES_NO_OPTION);
             
             if(n == JOptionPane.YES_OPTION) {
                 
                 String serial = (String)jTable7.getValueAt(sel, 0);
                 int i = this.cacheFindSparePart(serial);
                 int v = (Integer)jTable7.getValueAt(sel, 2);
                 
                 if(i != -1)
                     this.cache.get(i).decreaseDelta(v);
                 else
                     this.cache.add(new SharedClasses.UsageCache(serial, -v));
                 
                 int f = this.sparePartSearchResultFind(serial);
                 if(f != -1)
                     jTable6.setValueAt((Integer)jTable6.getValueAt(f, 2) + (Integer)jTable7.getValueAt(sel, 2), f, 2);
                 
                 DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
                 model.removeRow(sel);
                 this.updateTotal(jTable7);
                 
             } else {
                 // do nothing
             }
             
         } else
             showWinAlert(warehouseManagement, "Selezionare prima un pezzo\nper cancellarlo dalla lista!", "Warning Selection", JOptionPane.WARNING_MESSAGE);
         
     }//GEN-LAST:event_jButton42MouseClicked
 
     // delete all spare part from usage
     private void jButton43MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton43MouseClicked
             int n = JOptionPane.showConfirmDialog(warehouseManagement, "Cancellare tutti i pezzi\nimpostati per questa lavorazione?", "Cancellazione info", JOptionPane.YES_NO_OPTION);
             
             if(n == JOptionPane.YES_OPTION){
                 int r = jTable7.getRowCount();
                 
                 if(r > 0) {
                     DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
                     
                     for(int i = 0; i < r; i++)
                         model.removeRow(0);
                     
                     clearFields(jTextField90, jTextField91);
                 }
                 
             } else {
                 // do nothing
             }
                 
     }//GEN-LAST:event_jButton43MouseClicked
 
     // if a there is a spare part in the search result table return index of the row
     private int sparePartSearchResultFind (String s) {
         
         DefaultTableModel model = (DefaultTableModel)jTable6.getModel();
         int n = model.getRowCount();
         
         for(int i = 0; i < n; i++)
             if(s.equals((String)(jTable6.getValueAt(i, 0))))
                 return i;
         
         return -1;
         
     }
     
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
             showWinAlert(warehouseManagement, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
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
         //toppy(gestioneRientri); //FIXME
 
         
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
         //warranty
         jCheckBox1.setSelected(false);
         //type
         jComboBox3.setSelectedIndex(0);
         //estetica
         jComboBox1.setSelectedIndex(0);
         //stato lav
         jComboBox4.setSelectedIndex(0);
         jComboBox4.setEnabled(false);
         //problematiche
         jTextArea6.setEditable(false);
         jTextArea7.setEditable(false);
         //orario
         
         jTextField21.setText(getDataOra());
         //fine setting//
         //set forzato dato che da valore sporco
         jLabel4.setText("Estetica dispositivo:");
         
     }//GEN-LAST:event_jButton46MouseClicked
 
     private void jButton47MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton47MouseClicked
         //preimpo cerca pratica
         jPanel9.setVisible(false);
         jPanel10.setVisible(true);
         //jScrollPane5.setVisible(false);
         jTable3.setVisible(false);
         clearFields(jTextField27, jTextField28, jTextField29, jTextField30);
     }//GEN-LAST:event_jButton47MouseClicked
 
     // select a customer from search results when creating a new repair
     private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
         
         if(evt.getClickCount() == 2){ //lol grosso trucco :D
             int sel = jTable2.getSelectedRow(); // take the index
             cercaCliente.dispose();
             this.c = (SharedClasses.Customer)this.customerRet.get(sel); // obtain the Customer class object
             this.customerRet = null; // reset the array with the search results
             
             jTextField19.setText(this.c.getSurname()); // take customer's surname
             jTextField20.setText(this.c.getName()); // take customer's name
         }
     }//GEN-LAST:event_jTable2MouseClicked
 
     // open the edit window for customer
     private void jTable5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable5MouseClicked
 
         if(evt.getClickCount() == 2){
             int sel = jTable5.getSelectedRow();
             this.c = (SharedClasses.Customer)this.customerRet.get(sel);
             
             setCenterMonitorDim(503, 300);
             DatiClienteView = new FinestraSwing("Scheda dati cliente", p.getPX(), p.getPY(), 503, 300, jPanel13);
             clearFields(jTextField15, jTextField16, jTextField14, jTextField13);
             jTextArea3.setText(null);
             getDatiClienteDb(jTextField15, jTextField16, jTextField14, jTextField13, jTextArea3, false);
         }
     }//GEN-LAST:event_jTable5MouseClicked
 
     private void jButton49MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton49MouseClicked
         // Cerca cliente per creazione nuova scheda
         setCenterMonitorDim(834, 460);
         cercaCliente = new FinestraSwing("Inserisci i dati per avviare la ricerca...", p.getPX(), p.getPY(), 834, 460, customerSearch);
         jTextField11.setText("Cognome");
         jTextField12.setText("Nome");
         jTable2.setVisible(false);
         //toppy(cercaCliente); //FIXME
     }//GEN-LAST:event_jButton49MouseClicked
 
     private void jButton48MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton48MouseClicked
         // aggiungi cliente
         setCenterMonitorDim(503, 300);
         DatiCliente = new FinestraSwing("Crea scheda dati cliente", p.getPX(), p.getPY(), 503, 300, customerEdit);
         clearFields(jTextField7, jTextField8, jTextField9, jTextField10);
         jTextArea2.setText(null);
     }//GEN-LAST:event_jButton48MouseClicked
     
     // customer INSERT
     private void jButton16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton16MouseClicked
       int flagError = 0;
         
         // check if all required fields are filled
         if(jTextField7.getText().equals("")){
             showWinAlert(customerEdit, "Manca il Cognome.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         if(jTextField8.getText().equals("")){
             showWinAlert(customerEdit, "Manca il Nome.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         if(jTextField10.getText().equals("")){
             showWinAlert(customerEdit, "Manca un recapito telefonico.", "Error", JOptionPane.ERROR_MESSAGE);
             flagError++;
         }
         
         if(flagError == 0){ // it's ok
             
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
                     showWinAlert(customerEdit, "Utente già esistente.", "Error", JOptionPane.ERROR_MESSAGE);
                 } else if(v == ComClasses.Constants.RET_EXC) {
                     showWinAlert(customerEdit, "Eccezione durante l'inserimento del cliente. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
                 }
                     
             } catch (Exception e) {
                 showWinAlert(customerEdit, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
     }//GEN-LAST:event_jButton16MouseClicked
    
     // customer edit window exit without changes (jPanel13)
     private void jButton15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton15MouseClicked
         DatiCliente.dispose();
     }//GEN-LAST:event_jButton15MouseClicked
         
     // customer edit window exit without changes (jPanel7)
     private void jButton13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton13MouseClicked
         DatiClienteView.dispose();
     }//GEN-LAST:event_jButton13MouseClicked
 
     // customer UPDATE
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
 
     //see section 3 type billing
     private void jButton23MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton23MouseClicked
         billingCreation.setVisible(true);
         billingSearch.setVisible(false);
         fatturaView.setVisible(false);
         ndcView.setVisible(false);
         rdaView.setVisible(false);
         deactivateComponent(jLabel60, jTextField57, jTextField58, jButton54, jButton55, jButton63, jButton64);
     }//GEN-LAST:event_jButton23MouseClicked
 
     
     // add a row in billing tables
     private static void addBillingRow(JTable table) {
         DefaultTableModel model = (DefaultTableModel) table.getModel();
         model.addRow(new Object[]{"", "", "", 1, "", false, ""});  
     }
     
     // FATTURA add row
     private void jButton50MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton50MouseClicked
         addBillingRow(jTable8);
     }//GEN-LAST:event_jButton50MouseClicked
 
     // FATTURA delete single row
     private void jButton51MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton51MouseClicked
         if(jTable8.getSelectedRow() != -1){
             DefaultTableModel model = (DefaultTableModel)jTable8.getModel();
             model.removeRow(jTable8.getSelectedRow());
         }
     }//GEN-LAST:event_jButton51MouseClicked
 
     // FATTURA sync
     private void jButton52MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton52MouseClicked
        this.billingSync(jTable8);
     }//GEN-LAST:event_jButton52MouseClicked
 
     // FATTURA delete all rows
     private void jButton53MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton53MouseClicked
         setJTableBilling(jTable8, 0);
         clearFields(jTextField53, jTextField56, jTextField63);
     }//GEN-LAST:event_jButton53MouseClicked
 
     // FATTURA new
     private void jButton30MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton30MouseClicked
         fatturaView.setVisible(true);
         setJTableBilling(jTable8, 1);
         ndcView.setVisible(false);
         rdaView.setVisible(false);
         this.bill = Client.Constants.FATTURA;
         activateComponent(jLabel60, jTextField57, jTextField58, jButton54, jButton55, jButton63, jButton64);  // make visible buttons, textfield and label
     }//GEN-LAST:event_jButton30MouseClicked
 
     private void jButton56MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton56MouseClicked
         DatiClienteView.dispose();
     }//GEN-LAST:event_jButton56MouseClicked
 
     // save billing customer - used when creating a new billing customer and when editing an existing one
     private void jButton57MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton57MouseClicked
         
         if(ManagGuiView.neededInsertion(jTextField61, jTextField62) && (ManagGuiView.neededInsertion(jTextFieldCF) || ManagGuiView.neededInsertion(jTextFieldIVA))) {
                             
             if(!checkTaxCode(jTextFieldCF.getText())) {
                 showWinAlert(billingCustomerEdit, "Struttura codice fiscale errata!", "Error", JOptionPane.ERROR_MESSAGE);
             } else {
                 
                 SharedClasses.Customer auxC = new SharedClasses.Customer(jTextField61.getText(), jTextField62.getText(), jTextField60.getText(), jTextField59.getText(), jTextArea13.getText());
                 ComClasses.Request r;
                 int v = 0;
                 
                 // manage customer info first
                 if(this.billingCustomerEditing) {
                     // an update is needed: the customer exists, so we have to take the id
                     auxC.setID(this.billingCustomer.getID());
                     r = new ComClasses.Request(auxC, ComClasses.Constants.CUSTOMER, ComClasses.Constants.UPDATE, this.billingCustomer.update(auxC));
                 } else {
                     // an insertion is needed
                     r = new ComClasses.Request(auxC, ComClasses.Constants.CUSTOMER, ComClasses.Constants.INSERT, auxC.insert());
                 }
 
                 try {
                     v = Utils.intOperation(r).intValue();
                 } catch (SharedClasses.MyDBException e) {
                     // if editing and using an existing record, ignore the exception
                     if(!(this.billingCustomerEditing && e.getCode() == ComClasses.Constants.NOTDONE)) {
                         showWinAlert(billingCustomerEdit, "Informazioni utente: ".concat(e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                 } catch (Exception e) {
                     // catch every exception that is not a custom database exception and don't coninue
                     showWinAlert(billingCustomerEdit, "Riprova! Eccezione non prevista durante inserimento info utente: ".concat(e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                     return;
                 }
        
                 this.billingCustomer = auxC;
                 SharedClasses.BillingCustomer auxB = new SharedClasses.BillingCustomer(jTextFieldIVA.getText(), jTextFieldCF.getText());
 
                 // manage billing info
                 if (this.billingCustomerEditing && !(this.newBillingInfo)) {
                     auxB.setID(this.billingCustomer.getID());
                     r = new ComClasses.Request(auxB, ComClasses.Constants.BILLCUS, ComClasses.Constants.UPDATE, this.billingCustomerInfo.update(auxB));
                 } else {
                     if(this.billingCustomer.getID() == 0)
                         this.billingCustomer.setID(v);
                     
                     auxB.setID(this.billingCustomer.getID());
                     r = new ComClasses.Request(auxB, ComClasses.Constants.BILLCUS, ComClasses.Constants.INSERT, SharedClasses.BillingCustomer.insert());
                 }
                 
                 try {
                     v = Utils.intOperation(r).intValue();
                 } catch (SharedClasses.MyDBException e) {
                     if(!(this.billingCustomerEditing && e.getCode() == ComClasses.Constants.NOTDONE)) {
                         showWinAlert(billingCustomerEdit, "Informazioni fatturazione: ".concat(e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                 } catch (Exception e) {
                     showWinAlert(billingCustomerEdit, "Riprova! Eccezione non prevista durante inserimento info fatturazione: ".concat(e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 
                 this.billingCustomerInfo = auxB;
                 
                 jTextField57.setText(this.billingCustomer.getSurname());
                 jTextField58.setText(this.billingCustomer.getName());
                  
                  if(DatiClienteView != null) {
                      DatiClienteView.dispose();
                      DatiClienteView = null;
                  }
                  if(cercaCliente != null) {
                      cercaCliente.dispose();
                      cercaCliente = null;
                  }
 
             }
             
         } else {
             // fill all text fields
             showWinAlert(billingCustomerEdit, "Inserire tutti i dati obbligatori (cognome, nome, codice fiscale)!", "Warning", JOptionPane.WARNING_MESSAGE);
         }
     }//GEN-LAST:event_jButton57MouseClicked
 
     // check if tax code format is correct
     private static boolean checkTaxCode (String c) {
         
         if(c.matches("[a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][a-zA-Z][0-9][0-9][abcdehlmprstABCDEHLMPRST][0-9][0-9][a-zA-Z][0-9][0-9][0-9][a-zA-Z]") || c.matches(""))
             return true;
         
         return false;
         
     }
     
     // create new customer for billing
     private void jButton54MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton54MouseClicked
         setCenterMonitorDim(520, 360);
         this.billingCustomerEditing = false;
         this.newBillingInfo = true;
         DatiClienteView = new FinestraSwing("Crea scheda dati cliente", p.getPX(), p.getPY(), 520, 360, billingCustomerEdit);
         clearFields(jTextField62, jTextField61, jTextFieldCF, jTextFieldIVA, jTextField60, jTextField59);
         jTextArea13.setText(null);
     }//GEN-LAST:event_jButton54MouseClicked
 
     private void jTextField65MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField65MouseClicked
         if(jTextField65.getText().equals("Cognome"))
             jTextField65.setText("");
         if(jTextField66.getText().equals("Nome"))
             jTextField66.setText("");
     }//GEN-LAST:event_jTextField65MouseClicked
 
     private void jTextField66MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField66MouseClicked
         if(jTextField65.getText().equals("Cognome"))
             jTextField65.setText("");
         if(jTextField66.getText().equals("Nome"))
             jTextField66.setText("");
     }//GEN-LAST:event_jTextField66MouseClicked
 
     // customer SELECT in billing view
     private void jButton58MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton58MouseClicked
          
         String surname = jTextField65.getText();
         String name = jTextField66.getText();
         
         if(!checkCustomerSearch(surname, name)) { // info not inserted properly
             showWinAlert(billingCustomerSearch, "Valori errati, riprovare...", "Error", JOptionPane.ERROR_MESSAGE);
             jTable9.setVisible(false);
         } else { // search can be executed
             this.customerSearchResult(name, surname, true); // execute the operation and
             
             if(this.billingCustomerRet != null) {
                 setTableCustomerData(jTable9, this.billingCustomerRet);
                 jTable9.setVisible(true); // this was out of the if/else
             } else {
                 jTable9.setVisible(false);
             }
             
         }
    
     }//GEN-LAST:event_jButton58MouseClicked
 
     // reset customer search fields in customer view
     private void jButton59MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton59MouseClicked
         jTextField65.setText("Cognome");
         jTextField66.setText("Nome");
         jTable9.setVisible(false);
     }//GEN-LAST:event_jButton59MouseClicked
 
     //exit without selecion - set client billing
     private void jButton60MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton60MouseClicked
         cercaCliente.dispose();
         
     }//GEN-LAST:event_jButton60MouseClicked
 
     // set billing customer
     private void jTable9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable9MouseClicked
 
         if(evt.getClickCount() == 2){
             int sel = jTable9.getSelectedRow();
             this.billingCustomer = (SharedClasses.Customer)this.billingCustomerRet.get(sel);
             this.billingCustomerSearchResult(this.billingCustomer.getID());
             this.billingCustomerEditing = true;
             
             setCenterMonitorDim(503, 300);
             DatiClienteView = new FinestraSwing("Scheda dati cliente", p.getPX(), p.getPY(), 520, 360, billingCustomerEdit);
             
             clearFields(jTextField59, jTextField60, jTextField61, jTextField62, jTextFieldCF);
             jTextArea13.setText(null);
             getDatiClienteDb(jTextField61, jTextField62, jTextField60, jTextField59, jTextArea13, true);
 
             if(this.billingCustomerInfo != null) {
                 jTextFieldCF.setText(this.billingCustomerInfo.getCF().toLowerCase());
                 jTextFieldIVA.setText(this.billingCustomerInfo.getIVA().toLowerCase());
                 this.newBillingInfo = false;
             } else {
                 this.newBillingInfo = true;
             }
         }
         
     }//GEN-LAST:event_jTable9MouseClicked
 
     // use existing customer for billing
     private void jButton55MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton55MouseClicked
         setCenterMonitorDim(834, 460);
         this.billingCustomerEditing = true;
         cercaCliente = new FinestraSwing("Inserisci i dati per avviare la ricerca...", p.getPX(), p.getPY(), 834, 460, billingCustomerSearch);
         jTextField65.setText("Cognome");
         jTextField66.setText("Nome");
         jTable9.setVisible(false);
     }//GEN-LAST:event_jButton55MouseClicked
 
     // NDC add row
     private void jButton65MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton65MouseClicked
         addBillingRow(jTable11);
     }//GEN-LAST:event_jButton65MouseClicked
 
     //delrow ndc
     private void jButton66MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton66MouseClicked
         if(jTable11.getSelectedRow() != -1){
             DefaultTableModel model = (DefaultTableModel)jTable11.getModel();
             model.removeRow(jTable11.getSelectedRow());
         }
     }//GEN-LAST:event_jButton66MouseClicked
 
     //sync win warehouse
     private void jButton67MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton67MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton67MouseClicked
 
     //del everything ndc
     private void jButton68MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton68MouseClicked
         setJTableBilling(jTable11, 0);
         clearFields(jTextField72, jTextField73, jTextField74);
     }//GEN-LAST:event_jButton68MouseClicked
 
     // create new noda di credito
     private void jButton31MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton31MouseClicked
         fatturaView.setVisible(false);
         ndcView.setVisible(true);
         setJTableBilling(jTable11, 0);
         rdaView.setVisible(false);
         this.bill = Client.Constants.NDC;
         activateComponent(jLabel60, jTextField57, jTextField58, jButton54, jButton55, jButton63, jButton64);  // make visible buttons, textfield and label
     }//GEN-LAST:event_jButton31MouseClicked
 
     // RDA add row
     private void jButton69MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton69MouseClicked
         addBillingRow(jTable12);
     }//GEN-LAST:event_jButton69MouseClicked
 
     // RDA delete single row
     private void jButton70MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton70MouseClicked
         if(jTable12.getSelectedRow() != -1){
             DefaultTableModel model = (DefaultTableModel)jTable12.getModel();
             model.removeRow(jTable12.getSelectedRow());
         }
     }//GEN-LAST:event_jButton70MouseClicked
 
     // RDA sync
     private void jButton71MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton71MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton71MouseClicked
 
     // RDA delete all row
     private void jButton72MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton72MouseClicked
         setJTableBilling(jTable12, 0);
         clearFields(jTextField76, jTextField78);
     }//GEN-LAST:event_jButton72MouseClicked
 
     // RDA new
     private void jButton34MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton34MouseClicked
         fatturaView.setVisible(false);
         setJTableBilling(jTable12, 0);
         ndcView.setVisible(false);
         rdaView.setVisible(true);
         this.bill = Client.Constants.RDA;
         activateComponent(jLabel60, jTextField57, jTextField58, jButton54, jButton55, jButton63, jButton64);  // make visible buttons, textfield and label
     }//GEN-LAST:event_jButton34MouseClicked
 
     private void jTextField79MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField79MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextField79MouseClicked
 
     private void jTextField80MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField80MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextField80MouseClicked
 
     private void jButton73MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton73MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton73MouseClicked
 
     private void jButton74MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton74MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton74MouseClicked
 
     //open search
     private void jTable13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable13MouseClicked
         // TODO add your handling code here:
     }//GEN-LAST:event_jTable13MouseClicked
 
     // active billing search
     private void jButton24MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton24MouseClicked
         billingCreation.setVisible(false);
         billingSearch.setVisible(true);
     }//GEN-LAST:event_jButton24MouseClicked
 
     // reset billing view and data
     private void jButton63MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton63MouseClicked
         this.billingCustomer = null;
         this.billingCustomerInfo = null;
         this.billingCustomerRet = null;
         this.resetBillingTables();
         jTextField57.setText("Cognome");
         jTextField58.setText("Nome");
         clearFields(jTextField53, jTextField56, jTextField63, jTextField72, jTextField73, jTextField74, jTextField76, jTextField78);
     }//GEN-LAST:event_jButton63MouseClicked
 
     // save and print billing
     private void jButton64MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton64MouseClicked
         
         try {
             // TODO select last billing id to increase it (consider billing year)
             this.checkBillingInfo();
            this.billingSync(jTable8);
             Print.repairPrint(0, this.billingCustomer, this.billingCustomerInfo, jTable8, jTextField53.getText(), jTextField54.getText(), jTextField56.getText());
             this.jButton63MouseClicked(evt);
         } catch (Exception e) {
             showWinAlert(billingCreation, e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
         }
         
         //new Billing(this.bill, getDataOra());
     }//GEN-LAST:event_jButton64MouseClicked
 
     private void checkBillingInfo () throws SharedClasses.InfoNeededException {
         
         if(this.billingCustomer == null)
             throw new SharedClasses.InfoNeededException(ComClasses.Constants.CUSTOMER);
         else if (this.billingCustomerInfo == null)
             throw new SharedClasses.InfoNeededException(ComClasses.Constants.BILLCUS);
 
     }
     
     
     // warehouse SELECT all by availability
     private void jButton76MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton76MouseClicked
         ComClasses.Request wr = new ComClasses.Request(Client.Utils.getWarehouse(), ComClasses.Constants.WARE, ComClasses.Constants.SELECT, SharedClasses.Warehouse.byAvailabilitySelect());
        
         this.warehouseRet = null;
         
         try {
             this.warehouseRet = Utils.arrayOperation(wr);
         } catch (Exception e) {
             showWinAlert(customerSearch, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
         if(this.warehouseRet != null) {
             setTableWarehouseData(jTable1, this.warehouseRet);
             jTable1.setVisible(true); // this was out of the if/else
         } else {
             showWinAlert(customerSearch, "Errore durante la ricerca: riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
             jTable1.setVisible(false);
         }
     }//GEN-LAST:event_jButton76MouseClicked
 
     // set start working date for a repair with current datetime
     private void jButton77MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton77MouseClicked
         jTextField34.setText(getDataOra());
         jComboBox6.setSelectedIndex(1);
     }//GEN-LAST:event_jButton77MouseClicked
 
     // set date out for a repair with current datetime
     private void jButton78MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton78MouseClicked
         jTextField35.setText(getDataOra());
         jComboBox6.setSelectedIndex(5);
     }//GEN-LAST:event_jButton78MouseClicked
 
     // number inserted in price with IVA JTextField (warehouse INSERT)
     private void jTextField52KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField52KeyReleased
         jTextField88.setText(handleIVA(jTextField52.getText(), this.warehouseInfo.getIVA(), false));
     }//GEN-LAST:event_jTextField52KeyReleased
 
     // number inserted in price without IVA JTextField (warehouse INSERT)
     private void jTextField88KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField88KeyReleased
         jTextField52.setText(handleIVA(jTextField88.getText(), this.warehouseInfo.getIVA(), true));
     }//GEN-LAST:event_jTextField88KeyReleased
 
     // number inserted in price with IVA JTextField (warehouse UPDATE)
     private void jTextField41KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField41KeyReleased
         jTextField89.setText(handleIVA(jTextField41.getText(), this.warehouseInfo.getIVA(), false));
     }//GEN-LAST:event_jTextField41KeyReleased
 
     // number inserted in price without IVA JTextField (warehouse UPDATE)
     private void jTextField89KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField89KeyReleased
         jTextField41.setText(handleIVA(jTextField89.getText(), this.warehouseInfo.getIVA(), true));
     }//GEN-LAST:event_jTextField89KeyReleased
 
     // print button in repair edit window
     private void jButton79MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton79MouseClicked
         // UPDATE the repair info first...
         if(!neededInsertion(jTextField36, jTextField37)) {
             // compulsory fields leaved empty
             showWinAlert(repairDetail, "Controllare modello ed imei", "Errore", JOptionPane.ERROR_MESSAGE);
         } else {
             // handle device update
             SharedClasses.Device md = new SharedClasses.Device(jTextField36.getText(), jComboBox5.getSelectedIndex(), jTextField37.getText());
             if(this.checkDeviceChange(md))
                 this.updateDevice(md);
             // UPDATE details of the selected repair
             SharedClasses.Details mdet = new SharedClasses.Details(this.re.getID(), jComboBox2.getSelectedIndex(), jTextField34.getText(), jTextArea8.getText(), jTextArea9.getText(), jTextArea10.getText());
             if(this.checkDetailsChange(mdet))
                 this.updateDetails(mdet);
             // UPDATE repair table of the database
             SharedClasses.Repair mrep = new SharedClasses.Repair(this.re.getID(), jTextField38.getText(), jTextField35.getText(), (Integer)jComboBox6.getSelectedIndex());
             if(this.checkRepairChange(mrep))
                 this.updateRepair(mrep);
             
             // ...then print. Show printing dialog
             int n = JOptionPane.showOptionDialog(repairDetail, "Vuoi stampare una copia della ricevuta o un riepilogo riparazione?", "Stampa", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, ManagGuiView.arr, ManagGuiView.arr[0]);
         
             try {
 
                 mrep.setDateIn(jTextField33.getText());
                 
                 if(n == JOptionPane.YES_OPTION)
                     Print.repairPrint(this.re.getID(), this.c, mrep, md, mdet, true);
                 else if(n == JOptionPane.NO_OPTION)
                     Print.repairPrint(this.re.getID(), this.c, mrep, md, mdet, false);
 
                 schedaProdotto.dispose();
                 jTable3.setVisible(false);
                 clearFields(jTextField27, jTextField28, jTextField29, jTextField30);
                 
             } catch (Exception e) {
                 // something wrong happened
                 showWinAlert(jPanel9, e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
             }
         }
         
     }//GEN-LAST:event_jButton79MouseClicked
 
     // obtain the price without iva
     private static String handleIVA (String s, Integer percentage, boolean iva) {
         
         if(!s.equals(""))
             // if the price has been inserted
             return handleIVA(new BigDecimal(s), percentage, iva).toString();
         else
             // if the price has not been inserted    
             return "0";
     }
     
     private static BigDecimal handleIVA (BigDecimal s, Integer percentage, boolean iva) {
         
         if(s != null) {
             
             BigDecimal tot = new BigDecimal(100 + percentage.intValue());
             
             if(iva)
                 return s.multiply(tot).divide(new BigDecimal(100), new MathContext(6, RoundingMode.HALF_DOWN)).setScale(2, RoundingMode.HALF_DOWN);
             else
                 return s.multiply(new BigDecimal(100)).divide(tot, new MathContext(6, RoundingMode.HALF_DOWN)).setScale(2, RoundingMode.HALF_DOWN);
             
         } else {
             return new BigDecimal(0);
         }
         
         
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel billingCreation;
     private javax.swing.JPanel billingCustomerEdit;
     private javax.swing.JPanel billingCustomerSearch;
     private javax.swing.JPanel billingSearch;
     private javax.swing.JPanel customerEdit;
     private javax.swing.JPanel customerInfoView;
     private javax.swing.JPanel customerSearch;
     private javax.swing.JPanel fatturaView;
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
     private javax.swing.JButton jButton23;
     private javax.swing.JButton jButton24;
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
     private javax.swing.JButton jButton34;
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
     private javax.swing.JButton jButton50;
     private javax.swing.JButton jButton51;
     private javax.swing.JButton jButton52;
     private javax.swing.JButton jButton53;
     private javax.swing.JButton jButton54;
     private javax.swing.JButton jButton55;
     private javax.swing.JButton jButton56;
     private javax.swing.JButton jButton57;
     private javax.swing.JButton jButton58;
     private javax.swing.JButton jButton59;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton60;
     private javax.swing.JButton jButton63;
     private javax.swing.JButton jButton64;
     private javax.swing.JButton jButton65;
     private javax.swing.JButton jButton66;
     private javax.swing.JButton jButton67;
     private javax.swing.JButton jButton68;
     private javax.swing.JButton jButton69;
     private javax.swing.JButton jButton7;
     private javax.swing.JButton jButton70;
     private javax.swing.JButton jButton71;
     private javax.swing.JButton jButton72;
     private javax.swing.JButton jButton73;
     private javax.swing.JButton jButton74;
     private javax.swing.JButton jButton76;
     private javax.swing.JButton jButton77;
     private javax.swing.JButton jButton78;
     private javax.swing.JButton jButton79;
     private javax.swing.JButton jButton8;
     private javax.swing.JButton jButton9;
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JCheckBox jCheckBox2;
     private javax.swing.JCheckBox jCheckBox3;
     private javax.swing.JCheckBox jCheckBox4;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JComboBox jComboBox2;
     private javax.swing.JComboBox jComboBox3;
     private javax.swing.JComboBox jComboBox4;
     private javax.swing.JComboBox jComboBox5;
     private javax.swing.JComboBox jComboBox6;
     private javax.swing.JDialog jDialog1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel100;
     private javax.swing.JLabel jLabel101;
     private javax.swing.JLabel jLabel102;
     private javax.swing.JLabel jLabel103;
     private javax.swing.JLabel jLabel104;
     private javax.swing.JLabel jLabel105;
     private javax.swing.JLabel jLabel106;
     private javax.swing.JLabel jLabel107;
     private javax.swing.JLabel jLabel108;
     private javax.swing.JLabel jLabel109;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel110;
     private javax.swing.JLabel jLabel111;
     private javax.swing.JLabel jLabel112;
     private javax.swing.JLabel jLabel114;
     private javax.swing.JLabel jLabel115;
     private javax.swing.JLabel jLabel116;
     private javax.swing.JLabel jLabel117;
     private javax.swing.JLabel jLabel118;
     private javax.swing.JLabel jLabel119;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel120;
     private javax.swing.JLabel jLabel121;
     private javax.swing.JLabel jLabel122;
     private javax.swing.JLabel jLabel123;
     private javax.swing.JLabel jLabel124;
     private javax.swing.JLabel jLabel125;
     private javax.swing.JLabel jLabel126;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel22;
     private javax.swing.JLabel jLabel23;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel3;
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
     private javax.swing.JLabel jLabel4;
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
     private javax.swing.JLabel jLabel5;
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
     private javax.swing.JLabel jLabel6;
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
     private javax.swing.JLabel jLabel79;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel80;
     private javax.swing.JLabel jLabel81;
     private javax.swing.JLabel jLabel82;
     private javax.swing.JLabel jLabel83;
     private javax.swing.JLabel jLabel84;
     private javax.swing.JLabel jLabel85;
     private javax.swing.JLabel jLabel86;
     private javax.swing.JLabel jLabel87;
     private javax.swing.JLabel jLabel88;
     private javax.swing.JLabel jLabel89;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JLabel jLabel90;
     private javax.swing.JLabel jLabel91;
     private javax.swing.JLabel jLabel98;
     private javax.swing.JLabel jLabel99;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel18;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane10;
     private javax.swing.JScrollPane jScrollPane11;
     private javax.swing.JScrollPane jScrollPane12;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane14;
     private javax.swing.JScrollPane jScrollPane15;
     private javax.swing.JScrollPane jScrollPane16;
     private javax.swing.JScrollPane jScrollPane17;
     private javax.swing.JScrollPane jScrollPane18;
     private javax.swing.JScrollPane jScrollPane19;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane20;
     private javax.swing.JScrollPane jScrollPane21;
     private javax.swing.JScrollPane jScrollPane22;
     private javax.swing.JScrollPane jScrollPane24;
     private javax.swing.JScrollPane jScrollPane25;
     private javax.swing.JScrollPane jScrollPane26;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTable jTable1;
     private javax.swing.JTable jTable11;
     private javax.swing.JTable jTable12;
     private javax.swing.JTable jTable13;
     private javax.swing.JTable jTable2;
     private javax.swing.JTable jTable3;
     private javax.swing.JTable jTable4;
     private javax.swing.JTable jTable5;
     private javax.swing.JTable jTable6;
     private javax.swing.JTable jTable7;
     private javax.swing.JTable jTable8;
     private javax.swing.JTable jTable9;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextArea jTextArea10;
     private javax.swing.JTextArea jTextArea11;
     private javax.swing.JTextArea jTextArea12;
     private javax.swing.JTextArea jTextArea13;
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
     private javax.swing.JTextField jTextField52;
     private javax.swing.JTextField jTextField53;
     private javax.swing.JTextField jTextField54;
     private javax.swing.JTextField jTextField55;
     private javax.swing.JTextField jTextField56;
     private javax.swing.JTextField jTextField57;
     private javax.swing.JTextField jTextField58;
     private javax.swing.JTextField jTextField59;
     private javax.swing.JTextField jTextField6;
     private javax.swing.JTextField jTextField60;
     private javax.swing.JTextField jTextField61;
     private javax.swing.JTextField jTextField62;
     private javax.swing.JTextField jTextField63;
     private javax.swing.JTextField jTextField65;
     private javax.swing.JTextField jTextField66;
     private javax.swing.JTextField jTextField7;
     private javax.swing.JTextField jTextField71;
     private javax.swing.JTextField jTextField72;
     private javax.swing.JTextField jTextField73;
     private javax.swing.JTextField jTextField74;
     private javax.swing.JTextField jTextField75;
     private javax.swing.JTextField jTextField76;
     private javax.swing.JTextField jTextField77;
     private javax.swing.JTextField jTextField78;
     private javax.swing.JTextField jTextField79;
     private javax.swing.JTextField jTextField8;
     private javax.swing.JTextField jTextField80;
     private javax.swing.JTextField jTextField81;
     private javax.swing.JTextField jTextField82;
     private javax.swing.JTextField jTextField85;
     private javax.swing.JTextField jTextField86;
     private javax.swing.JTextField jTextField87;
     private javax.swing.JTextField jTextField88;
     private javax.swing.JTextField jTextField89;
     private javax.swing.JTextField jTextField9;
     private javax.swing.JTextField jTextField90;
     private javax.swing.JTextField jTextField91;
     private javax.swing.JTextField jTextFieldCF;
     private javax.swing.JTextField jTextFieldIVA;
     private javax.swing.JToggleButton jToggleButton1;
     private javax.swing.JPanel mainBillingEdit;
     private javax.swing.JPanel mainCustomerSearch;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JPanel mainWarehouseEdit;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JPanel ndcView;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JPanel rdaView;
     private javax.swing.JPanel repairDetail;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     private javax.swing.JPanel warehouseEdit;
     private javax.swing.JPanel warehouseManagement;
     // End of variables declaration//GEN-END:variables
 
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
 
     private JDialog aboutBox;
     private JDialog settingBox;
     private JDialog warehouseBox;
     
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
         jCheckBox2.setSelected(false);
         jTextArea5.setText(null);
         clearFields(jTextField24, jTextField25, jTextField26);
     }
     
     private static String getDataOra(){
       
         Calendar calendar = new GregorianCalendar();
         
         int ore = calendar.get(Calendar.HOUR_OF_DAY);
         int minuti = calendar.get(Calendar.MINUTE);
         int giorno = calendar.get(Calendar.DAY_OF_MONTH);
         int mese = calendar.get(Calendar.MONTH);
         int anno = calendar.get(Calendar.YEAR);
 
         return lessThanTen(giorno) + "/" + lessThanTen(mese+1) +"/" + anno + " " + lessThanTen(ore) + ":" + lessThanTen(minuti);
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
         
         jTextField31.setText(this.c.getSurname().toLowerCase()); // customer' surname
         jTextField32.setText(this.c.getName().toLowerCase()); // customer's name
         
         jComboBox5.setSelectedIndex(this.d.getType()); // device type
         jTextField36.setText(this.d.getModel().toLowerCase()); // device model
         jTextField37.setText(this.d.getIdentification().toLowerCase()); // device serial number
         
         if(optional != null) { // optional
             jCheckBox3.setSelected(true);
             jTextField38.setText(optional.toLowerCase());
         }
         
         this.de = new SharedClasses.Details(this.re.getID());
         ComClasses.Request req = new ComClasses.Request(this.de, ComClasses.Constants.DETAILS, ComClasses.Constants.SELECT, this.de.select());
         
         try {
         
             ArrayList<Object> aux = Utils.arrayOperation(req);
 
             if(aux != null)
                 this.de = (SharedClasses.Details) aux.get(0);
 
             jTextArea8.setText(this.de.getDeclared().toLowerCase()); // defect declared
 
             
             String found = this.de.getFound();
             if(found != null)
                 jTextArea9.setText(found.toLowerCase()); // defect found
             else
                 jTextArea9.setText(null);
 
             String done = this.de.getDone();
             if(done != null)
                 jTextArea10.setText(done.toLowerCase()); // work done
             else
                 jTextArea10.setText(null);
             
             jTextField34.setText(this.de.getDateStart()); // work start date
             
             jComboBox2.setSelectedIndex(this.de.getCondition()); // device condition
             
         } catch (Exception e) {
             showWinAlert(jPanel10, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
             
         jComboBox6.setSelectedIndex(this.re.getStatus()); // repair status
         jTextField33.setText(this.re.getDateIn()); // date in
         jTextField35.setText(this.re.getDateOut()); // date out
     }
     
     private void noEditablePracticeValue(){
         jTextField31.setEditable(false);
         jTextField32.setEditable(false);
         jComboBox5.setEditable(false);
         jTextField33.setEditable(false);
     }
     
     // TODO change this method using Repair and Details classes - maybe this method in not useful
     private void setDataDbPracticeCreate(){
         jTextField19.getText(); //cognome
         jTextField20.getText(); //nome
         jComboBox3.getSelectedItem(); //tipologia prodotto
         jTextField24.getText(); //modello
         jTextField25.getText(); //imei
         if(jCheckBox2.isSelected())
             jTextField26.getText(); //accessori
         jTextArea5.getText(); //difetto dichiarato
         jComboBox4.getSelectedItem(); //a priori si sa lo stato di lavorazione dato è la creazione iniziale
         jTextField21.getText(); //data in
         // TODO after insert in into database, clear all objects used
     }
     
     private void resetDatiCliente(){
         //reset val cliente
         jTextArea2.setText(null);
         clearFields(jTextField7, jTextField8, jTextField9, jTextField10);
     }
     
     // insert customer's info into edit window
     private void getDatiClienteDb(JTextField nome, JTextField cognome, JTextField indirizzo, JTextField rec, JTextArea note, boolean bill){//FIXME
         
         SharedClasses.Customer aux;
         
         if(bill)
             aux = this.billingCustomer;
         else
             aux = this.c;
         
         nome.setText(aux.getName().toLowerCase());              // name
         cognome.setText(aux.getSurname().toLowerCase());        // surname
         indirizzo.setText(aux.getAddress().toLowerCase());      // address
         rec.setText(aux.getTelephone());                        // telephone number
         note.setText(aux.getNote().toLowerCase());              // note
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
                 showWinAlert(customerEdit, "Utente già esistente.", "Error", JOptionPane.ERROR_MESSAGE);
             else if(v == ComClasses.Constants.RET_EXC)
                 showWinAlert(customerEdit, "Eccezione durante l'inserimento del cliente. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
         
         } catch (Exception e) {
             showWinAlert(customerEdit, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     
     private int setArticleWarehouseDB(JTextField code, JTextField name, JTextField n, JTextField up, JTextArea note, JTextField nip){
         
         // create a new spare part
         SharedClasses.Warehouse w = new SharedClasses.Warehouse(code.getText(), name.getText(), Integer.parseInt(n.getText()), up.getText(), note.getText(), nip.getText());
         // create the request object
         ComClasses.Request r = new ComClasses.Request(w, ComClasses.Constants.WARE, ComClasses.Constants.INSERT, SharedClasses.Warehouse.insert());
         
         try {
 
             int v = Utils.intOperation(r).intValue();
 
             if(v == ComClasses.Constants.RET_EXI)
                 showWinAlert(jPanel6, "Pezzo di ricambio già esistente.", "Error", JOptionPane.ERROR_MESSAGE);
             else if(v == ComClasses.Constants.RET_EXC)
                 showWinAlert(jPanel6, "Eccezione durante l'inserimento del pezzo. Riprovare.", "Error", JOptionPane.ERROR_MESSAGE);
             
             return v;
             
         } catch (Exception e) {
             showWinAlert(jPanel6, Client.Utils.exceptionMessage(e), "Error", JOptionPane.ERROR_MESSAGE);
             return ComClasses.Constants.RET_EXC;
         }
         
     }
     
     // set properly the Warehouse object needed to edit a spare part
     private void getValArticleWarehouse(JTextField code, JTextField name, JTextField n, JTextField up, JTextArea note, JTextField nip){        
         code.setText(this.sp.getSerial().toLowerCase());
         name.setText(this.sp.getName().toLowerCase());
         n.setText(Integer.toString(this.sp.getAvailability()));
         up.setText(this.sp.getUnitPrice().toString());
         note.setText(this.sp.getNote().toLowerCase());
         nip.setText(this.sp.getNoIvaPrice().toString());
     }
     
     private void resetValArticleWarehouse(){
         jTextArea1.setText(null);
         clearFields(jTextField4, jTextField5, jTextField6, jTextField52, jTextField88);
     }
     
     // called in the usage edit window
     private void importArticle (int i, String q) {
         
         int av = (Integer)jTable6.getValueAt(i, 2);
         
         if (av > 0) {
         
             int qt = Integer.parseInt(q);
             int x, y;
             String serial = (String)jTable6.getValueAt(i, 0);
 
             y = this.tableFindSparePart(serial);
             
             if(y != -1) { // spare part already used in this repair
 
                 Integer aux = (Integer)jTable7.getValueAt(y, 2);
                 jTable7.setValueAt(aux.intValue() + qt, y, 2);
 
             } else {    // new spare part added
 
                 DefaultTableModel model = (DefaultTableModel)jTable7.getModel();
                 model.addRow(new Object[]{serial, jTable6.getValueAt(i, 1), qt, jTable6.getValueAt(i, 3), jTable6.getValueAt(i, 4)});
                 jTable6.setValueAt(av - qt, i, 2);
 
             }
 
             jTable6.setValueAt(av - qt, i, 2);
 
             x = this.cacheFindSparePart(serial);
             if(x != -1)
                 this.cache.get(x).increaseDelta(qt);
             else
                 this.cache.add(new SharedClasses.UsageCache(serial, qt));
             
         } else {
             showWinAlert(warehouseManagement, "Pezzo terminato", "Error", JOptionPane.ERROR_MESSAGE);
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
     
     private void getConnection () {
         
         try {
         
             this.serverInfo = Client.Utils.getSettings();
             
             try {
                 
                 Client.Utils.open(this.serverInfo.getAddress(), this.serverInfo.getPort()).close();
                 
             } catch (IOException e) {
               
                 showWinAlert(null, "Impossibile connettersi al server.\nVerificare che il server sia in esecuzione\ne controllare le impostazioni in File -> Setting IP.", "Warning", JOptionPane.WARNING_MESSAGE);
                 
             }
             
         } catch (FileNotFoundException e) {
             
             showWinAlert(null, "File impostazioni non trovato.\nPer utilizzare il programma inserire le info del server in\nFile -> Setting IP", "Warning", JOptionPane.WARNING_MESSAGE);
             
         } catch (IOException e) {
             
             showWinAlert(null, "Errore lettura dal file impostazioni.\nPer utilizzare il programma inserire nuovamente le info del server in\nFile -> Setting IP", "Warning", JOptionPane.WARNING_MESSAGE);
 
         }
         
     }
     
      private void getWarehouse () {
          this.warehouseInfo = Client.Utils.getWarehouseInfo(); 
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
         String[] columnNames = new String[]{"Codice Articolo", "Nome Generico", "Quantità", "Prezzo unitario", "Prezzo unitario ivato", "Note"};
         
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
         String[] columnNames = new String[]{"Codice Articolo", "Nome Generico", "Quantità Disponibile", "Prezzo Un.", "Prezzo Un. Ivato"};
         
         DefaultTableModel model = new DefaultTableModel(columnNames, n){
         
             private static final long serialVersionUID = 1L;
             @Override
             public boolean isCellEditable(int row, int column){
                 return false;
             }
         };
         
         jt.setModel(model);
     }
     
     
     private static void setJTableRetailer(JTable jt, int n){
         String[] columnNames = new String[]{"Cognome", "Nome", "Indirizzo", "P.Iva", "C.F."};
         
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
     
     private static void clearFields (JTextField ... f) {
         
         for(int i = 0; i < f.length; i++)
             f[i].setText(null);
         
     }
     
     private static void setJTableBilling(JTable jt, int n){
 
         String[] columnNames = new String[]{"Tipologia", "Codice", "Descrizione", "Quantità", "Prezzo unitario", "Esentasse", "Totale"};
         
 //        jt.setModel(new javax.swing.table.DefaultTableModel(
 //            new Object [][] {
 //                    {null, null, null, null, new Boolean(false), null},
 //            }, new String [] {
 //                 "Codice Articolo", "Descrizione", "Quantità", "Prezzo unitario", "Esentasse", "Totale"
 //            }));
         
         DefaultTableModel model = new DefaultTableModel(columnNames, 0){
         
 //            private static final long serialVersionUID =f 1L;
 //            @Override
 //            public boolean isCellEditable(int row, int column){
 //                return false;
 //            }
         };
         jt.setModel(model);
         
         TableColumn esentasse = jt.getColumnModel().getColumn(5); 
         
         JCheckBox checkbox = new JCheckBox();
         checkbox.setSelected(false);
         checkbox.setVisible(true);
         checkbox.setEnabled(true);
         
         esentasse.setCellEditor(new DefaultCellEditor(checkbox));
         
         jt.getDefaultRenderer(model.getColumnClass(n));
         
         MyTableCellRenderer renderer = new MyTableCellRenderer();
         jt.setDefaultRenderer(Object.class, renderer);
        
         TableColumn type = jt.getColumnModel().getColumn(0);
 
         JComboBox comboBox = new JComboBox();
         comboBox.addItem("---");
         comboBox.addItem("Numero scheda Ripar.");
         comboBox.addItem("Codice Articolo Mag.");
         comboBox.addItem("Altro");
         comboBox.setSelectedIndex(0);
         comboBox.setEnabled(true);
         comboBox.setVisible(true);
         type.setCellEditor(new DefaultCellEditor(comboBox));
         esentasse.setCellRenderer(new CheckBoxRenderer());
         for(int i = 0; i < n; i++)
             model.addRow(new Object[]{"", "", "", 1, "", false, ""});       
     }
     
     private void resetBillingTables () {
         setJTableBilling(jTable8, 1);
         setJTableBilling(jTable11, 1);
         setJTableBilling(jTable12, 1);
     }
     
     
     private void setUPBill(){
         jButton23.setText("Crea Scheda");
         jButton24.setText("Cerca Scheda");
         jButton30.setText("Nuova Fattura");
         jButton31.setText("Nuova N.d.C.");
         jButton34.setText("Nuova R.d.A.");
         jLabel6.setText("Data fatturazione:");
         jTextField42.setText(null);
         jLabel18.setText("Numero pratica fatturazione:");
         
     }
     
     private static void setJTableSearchBill(JTable jt, int n){
         String[] columnNames = new String[]{"Numero Pratica", "Cognome", "Nome","Indirizzo", "P.Iva", "C.F.", "Totale Finale"};
         
         DefaultTableModel model = new DefaultTableModel(columnNames, n){
         
             private static final long serialVersionUID = 1L;
             @Override
             public boolean isCellEditable(int row, int column){
                 return false;
             }
        };
         
         jt.setModel(model);
     }
     
     public Class getColumnClass(int c) {
             return getValueAt(0, c).getClass();
         }
     
     public Object getValueAt(int row, int col) {
         return jtVal[row][col];
     }
   
     private static void activateComponent (JComponent ... n) {
         for(JComponent f : n)
             f.setVisible(true);
     }
     
     private static void deactivateComponent (JComponent ... n ) {
         for(JComponent f : n)
             f.setVisible(false);
     }
 
     public void setWarehouseInfo (Client.WarehouseInfo wi) {
         this.warehouseInfo = wi;
     }
     
     private void billingSync (JTable JT) {
         
         int rc = JT.getRowCount();
         // check if the table is empty
         if(rc > 0) {
         
             int i = 0;
             BigDecimal tot = new BigDecimal(0);
             BigDecimal noTaxTot = new BigDecimal(0);
         
             while(i < rc) {
                 String type = (String)JT.getValueAt(i,0);
                 // check the kind of the element to add
                 if(type.contains("Codice")) {                       // add an article from warehouse
                     String serial = (String)JT.getValueAt(i, 1);    // get serial number
                     // retrieve article informations from database
                     SharedClasses.Warehouse w = new SharedClasses.Warehouse(serial);
                     ComClasses.Request r = new ComClasses.Request(w, ComClasses.Constants.WARE, ComClasses.Constants.SELECT, SharedClasses.Warehouse.select());    
                     // execute the query
                     try {
                          w = (SharedClasses.Warehouse)Utils.arrayOperation(r).get(0);
                     } catch (SharedClasses.MyDBException e) {
                         // first catch, custom exception
                         if(e.getCode() == ComClasses.Constants.DBNULL) {
                             showWinAlert(fatturaView, "Articolo ".concat(serial).concat(Client.Utils.exceptionMessage(e)).concat(", seriale inesistente."), "Error", JOptionPane.ERROR_MESSAGE);
                             i++;
                             continue;
                         }
                     } catch (Exception e) {
                         // second catch, generic exception
                         showWinAlert(fatturaView, "Errore imprevisto: ".concat(Client.Utils.exceptionMessage(e)), "Error", JOptionPane.ERROR_MESSAGE);
                         i++;
                         continue;
                     }
                     JT.setValueAt(w.getName(), i, 2);           // write article name in the table
                     JT.setValueAt(w.getNoIvaPrice(), i, 4);     // set price without iva
                 } 
     
                 if(JT.getValueAt(i, 3) == null)             // set quantity
                     JT.setValueAt(1, i, 3);
                                     
                 if((Boolean)JT.getValueAt(i, 5))        // no taxes
                     noTaxTot = noTaxTot.add(updateTotal(JT, i, JT.getValueAt(i, 4)));
                 else                                    // taxes
                     tot = tot.add(updateTotal(JT, i, JT.getValueAt(i, 4)));
                 
                 i++;
             }
             
             jTextField53.setText(tot.toString());
             jTextField63.setText(noTaxTot.toString());
             jTextField56.setText((handleIVA(tot, new Integer(jTextField54.getText()), true).add(noTaxTot)).toString()); 
         }
     }
     
     private static BigDecimal updateTotal (JTable JT, int row, Object price) {
         // returns item quantity * item price
         BigDecimal aux = null;
         if(price instanceof BigDecimal)
             aux = (BigDecimal)price;
         else if(price instanceof String)
             aux = new BigDecimal((String)price);
         
         if(aux != null) {
             try {
                 // when inserting a new element, quantity value is an Integer
                 Integer val = (Integer)JT.getValueAt(row, 3);
                 JT.setValueAt(aux.multiply(new BigDecimal(val.toString())), row, 6);
             } catch (java.lang.ClassCastException e) {
                 // when the element has been updated, quantity value is a String
                 JT.setValueAt(aux.multiply(new BigDecimal((String)JT.getValueAt(row, 3))), row, 6);
             }
             return (BigDecimal)JT.getValueAt(row, 6);
         } else {
             return new BigDecimal(0);
         }
     }
     
     private void printTextField (JTextField ... fs) {
         for(JTextField f : fs)
             System.out.println(f.getText());
         
     }
 
 }
