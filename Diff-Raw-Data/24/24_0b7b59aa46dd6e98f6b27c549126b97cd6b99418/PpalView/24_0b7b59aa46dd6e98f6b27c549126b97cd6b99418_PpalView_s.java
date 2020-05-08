 /*
  * ConfPpalView.java
  *
  * Created on 01-ago-2011, 17:47:34
  */
 package com.isolux.view;
 
 import com.isolux.utils.ViewUtils;
 import com.isolux.bo.*;
 import com.isolux.control.BalastosConfiguracionControl;
 import com.isolux.control.BalastosControl;
 import com.isolux.control.EntradaControl;
 import com.isolux.control.EscenaControl;
 import com.isolux.control.EventControl;
 import com.isolux.control.GeneralControl;
 import com.isolux.control.GrupoControl;
 import com.isolux.control.RealTimeControl;
 import com.isolux.dao.jmodbus.ConfiguracionDAOJmodbus;
 import com.isolux.dao.jmodbus.OperacionesBalastoConfiguracionDaoJmodbus;
 import com.isolux.dao.modbus.DAOJmodbus;
 import com.isolux.dao.properties.PropHandler;
 import com.isolux.hilos.OperacionesDaoHilo;
 import com.isolux.properties.MapaDeMemoria;
 import com.isolux.utils.LimitadorDeCaracteresNum_InputVerifier;
 import com.isolux.utils.Validacion;
 import com.isolux.view.threads.CargaInicial;
 import com.isolux.view.threads.ThreadManager;
 import com.toedter.calendar.JDateChooser;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu.Separator;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.UIManager;
 import javax.swing.tree.TreePath;
 
 /**
  *
  * @author Juan Diego Toro Cano
  */
 public class PpalView extends javax.swing.JFrame {
 
     /**
      * variable booleana que indica si existe una operacion modbus en progreso.
      */
     private boolean operacionModbusEnProgreso = false;
     //GENERAL
     private static PpalView index;
     private Map<String, Character> menuParents;
     private String ip;
     private int port;
     //LOAD FLAGS
     private boolean balastsStauts = false;
     private boolean groupsStauts = false;
     private boolean sceneStauts = false;
     private boolean inStauts = false;
     private boolean eventStauts = false;
     private boolean showAreas = true;
     //LOADED ELEMENS
     private HashMap<String, Balasto> balasts;
     private HashMap<String, Grupo> groups;
     private HashMap<String, Escena> scenes;
     private HashMap<String, Entrada> ins;
     private HashMap<String, Evento> events;
     //SELECTIONS
     private String selectedBalastNumber;
     private String selectedGroupNumber;
     private String selectedSceneNumber;
     private String selectedInNumber;
     private String selectedEventNumber;
     //Added elements
     private HashMap<String, Balasto> groupBalasts;
     private HashMap<String, Balasto> sceneBalasts;
     private HashMap<String, Balasto> inBalasts;
     private HashMap<String, Grupo> inGroups;
     private HashMap<String, Escena> inScene;
     private HashMap<String, Balasto> eventBalasts;
     private HashMap<String, Grupo> eventGroups;
     private HashMap<String, Escena> eventScene;
     //Balasts values 
     private int[] sceneBalastsValues;
     private int[] eventBalastsValues;
     //in variables
     private int inType;
     private int inOutType;
     //Event variables
     private int eventOutType;
     //CONTROLLERS
     private BalastosControl balastoCtrl;
     private EventControl eventCtrl;
     private GeneralControl generalCtrl;
     private GrupoControl groupsCtrl;
     private EntradaControl insCtrl;
     private RealTimeControl realCtrl;
     private EscenaControl sceneCtrl;
     //MODBUS DAO
     private DAOJmodbus dao;
     //Checking threads
     private ThreadManager threadManager;
     CargaInicial cargaInicial;
     private BalastosConfiguracionControl balastoConfigCtrl;
 
     //Init Ctrls
     public void initControls() {
         this.balastoCtrl = new BalastosControl();
         this.balastoConfigCtrl = new BalastosConfiguracionControl();
         this.eventCtrl = new EventControl();
         this.generalCtrl = new GeneralControl();
         this.groupsCtrl = new GrupoControl();
         this.insCtrl = new EntradaControl();
         this.realCtrl = new RealTimeControl();
         this.sceneCtrl = new EscenaControl();
 
     }
 
     public void initAddedElements() {
         this.balasts = new HashMap<String, Balasto>();
         this.groups = new HashMap<String, Grupo>();
         this.scenes = new HashMap<String, Escena>();
         this.ins = new HashMap<String, Entrada>();
         this.events = new HashMap<String, Evento>();
     }
 
     //Init selections.
     public void initSelections() {
         selectedBalastNumber = "";
         selectedGroupNumber = "";
         selectedSceneNumber = "";
         selectedInNumber = "";
         selectedEventNumber = "";
     }
 
     public void initModbus() {
         this.dao = DAOJmodbus.getInstancia();
     }
 
     public void getConnectionProperties() {
         this.ip = PropHandler.getProperty("general.ip");
         this.port = Integer.parseInt(PropHandler.getProperty("general.port"));
     }
 
     public void initInOutTypes() {
         inType = 0;
         inOutType = 0;
         eventOutType = 0;
     }
 
     public void initThreads() {
         this.threadManager = new ThreadManager(this);
     }
 
     /**
      * Creates new form ConfPpalView
      */
     public PpalView() {
 
         applicationTheme();
         initControls();
         initModbus();
         initSelections();
         initAddedElements();
         new GeneralControl().menuParents(this);
         getConnectionProperties();
         initInOutTypes();
         initThreads();
 
         initComponents();
         //        generalCtrl.loadConfigurationViewData(this);
         showAreas = true;
         //        new RealTimeControl().refreshBalastsLevels(this);
 
         CargaInicial c = new CargaInicial(this);
         c.execute();
 
 
 
     }
 
     /**
      * GENERATED CODE
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jTextField2 = new javax.swing.JTextField();
         botoneraComportamiento = new javax.swing.ButtonGroup();
         botoneraTipoContacto = new javax.swing.ButtonGroup();
         confRol = new javax.swing.ButtonGroup();
         repetirCiclo_jCheckBox = new javax.swing.JCheckBox();
         dias_buttonGroup = new javax.swing.ButtonGroup();
         leerFlash_jMenuItem = new javax.swing.JMenuItem();
         nivel_jLabel = new javax.swing.JLabel();
         nivel_jTextField = new javax.swing.JTextField();
         labelDns = new javax.swing.JLabel();
         fieldPort = new javax.swing.JFormattedTextField();
         grabarIp_jButton = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         jButton3 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jScrollPane3 = new javax.swing.JScrollPane();
         jList2 = new javax.swing.JList();
         jScrollPane4 = new javax.swing.JScrollPane();
         jList3 = new javax.swing.JList();
         jLabel39 = new javax.swing.JLabel();
         jLabel57 = new javax.swing.JLabel();
         escenasBalastos_jPanel = new javax.swing.JPanel();
         agregarBalastoEscena_jButton = new javax.swing.JButton();
         removerBalastoEscena_jButton = new javax.swing.JButton();
         agregarTodosBalastosEscena_jButton = new javax.swing.JButton();
         removerTodosBalastosEscenas_jButton = new javax.swing.JButton();
         jScrollPane5 = new javax.swing.JScrollPane();
         balastrosDisponibles_jList = new javax.swing.JList();
         jScrollPane6 = new javax.swing.JScrollPane();
         balastosAfectados_jList = new javax.swing.JList();
         actualizarNivelEscena_jButton = new javax.swing.JButton();
         jLabel18 = new javax.swing.JLabel();
         jLabel19 = new javax.swing.JLabel();
         nivelEscena_jTextField = new javax.swing.JTextField();
         nivelEscena_jLabel = new javax.swing.JLabel();
         jPanel13 = new javax.swing.JPanel();
         rbIsMaster = new javax.swing.JRadioButton();
         rbIsSlave = new javax.swing.JRadioButton();
         principal_jScrollPane = new javax.swing.JScrollPane();
         panelPrincipal_jPanel = new javax.swing.JPanel();
         header = new javax.swing.JPanel();
         headerImage = new javax.swing.JLabel();
         tabbedPane = new javax.swing.JTabbedPane();
         configuracionSmaiee_jPanel = new javax.swing.JPanel();
         arbolJerarquia_jScrollPane = new javax.swing.JScrollPane();
         arbol_jTree = new javax.swing.JTree();
         panelPpal = new javax.swing.JPanel();
         panelBienvenida = new javax.swing.JPanel();
         panelBalastos = new javax.swing.JPanel();
         balastoEnviar_jButton = new javax.swing.JButton();
         balastoEliminar_jButton = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         balastoNombreSmaiee_jTextField = new javax.swing.JTextField();
         jLabel41 = new javax.swing.JLabel();
         balastoNum_jComboBox = new javax.swing.JComboBox();
         panelGrupos = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         gruposNombreSmaiee_jTextField = new javax.swing.JTextField();
         enviarGrupo_jButton = new javax.swing.JButton();
         eliminarGrupo_jButton = new javax.swing.JButton();
         jLabel58 = new javax.swing.JLabel();
         gruposNum_jComboBox = new javax.swing.JComboBox();
         panelEscenas = new javax.swing.JPanel();
         nombreEscena_jLabel = new javax.swing.JLabel();
         nombreEscena_jTextField = new javax.swing.JTextField();
         escenaNumero_jComboBox = new javax.swing.JComboBox();
         enviarEscena_jButton = new javax.swing.JButton();
         eliminarEscena_jButton = new javax.swing.JButton();
         jLabel61 = new javax.swing.JLabel();
         panelEntradas = new javax.swing.JPanel();
         jLabel62 = new javax.swing.JLabel();
         jLabel63 = new javax.swing.JLabel();
         entradaNumero_jComboBox = new javax.swing.JComboBox();
         panelConfEntradas = new javax.swing.JPanel();
         panelEntradaInit = new javax.swing.JPanel();
         panelBotonera = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         jRadioButton1 = new javax.swing.JRadioButton();
         jRadioButton2 = new javax.swing.JRadioButton();
         jLabel8 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jTextField5 = new javax.swing.JTextField();
         jTextField6 = new javax.swing.JTextField();
         jPanel10 = new javax.swing.JPanel();
         jButton30 = new javax.swing.JButton();
         jButton31 = new javax.swing.JButton();
         jButton32 = new javax.swing.JButton();
         jButton33 = new javax.swing.JButton();
         jScrollPane15 = new javax.swing.JScrollPane();
         jList14 = new javax.swing.JList();
         jScrollPane16 = new javax.swing.JScrollPane();
         jList15 = new javax.swing.JList();
         balastros_jCheckBox = new javax.swing.JCheckBox();
         grupos_jCheckBox = new javax.swing.JCheckBox();
         jCheckBox18 = new javax.swing.JCheckBox();
         jButton14 = new javax.swing.JButton();
         jLabel17 = new javax.swing.JLabel();
         jRadioButton3 = new javax.swing.JRadioButton();
         jRadioButton4 = new javax.swing.JRadioButton();
         jButton40 = new javax.swing.JButton();
         panelFotocelda = new javax.swing.JPanel();
         jLabel11 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jTextField7 = new javax.swing.JTextField();
         entradaFotoceldaNivelIlum_jTextField = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         entradaFotoceldaNivelDeseado_jTextField = new javax.swing.JTextField();
         jPanel7 = new javax.swing.JPanel();
         jButton18 = new javax.swing.JButton();
         jButton19 = new javax.swing.JButton();
         jButton20 = new javax.swing.JButton();
         jButton21 = new javax.swing.JButton();
         jScrollPane9 = new javax.swing.JScrollPane();
         jList8 = new javax.swing.JList();
         jScrollPane10 = new javax.swing.JScrollPane();
         jList9 = new javax.swing.JList();
         jCheckBox10 = new javax.swing.JCheckBox();
         jCheckBox11 = new javax.swing.JCheckBox();
         jLabel14 = new javax.swing.JLabel();
         jTextField10 = new javax.swing.JTextField();
         jButton15 = new javax.swing.JButton();
         jLabel37 = new javax.swing.JLabel();
         jButton41 = new javax.swing.JButton();
         panelSensor = new javax.swing.JPanel();
         jPanel11 = new javax.swing.JPanel();
         jButton34 = new javax.swing.JButton();
         jButton35 = new javax.swing.JButton();
         jButton36 = new javax.swing.JButton();
         jButton37 = new javax.swing.JButton();
         jScrollPane17 = new javax.swing.JScrollPane();
         jList16 = new javax.swing.JList();
         jScrollPane18 = new javax.swing.JScrollPane();
         jList17 = new javax.swing.JList();
         jCheckBox19 = new javax.swing.JCheckBox();
         jCheckBox20 = new javax.swing.JCheckBox();
         fotoceldas_entrada_escenas_jCheckBox = new javax.swing.JCheckBox();
         jButton16 = new javax.swing.JButton();
         jButton42 = new javax.swing.JButton();
         jLabel66 = new javax.swing.JLabel();
         jTextField34 = new javax.swing.JTextField();
         jLabel67 = new javax.swing.JLabel();
         jTextField35 = new javax.swing.JTextField();
         jLabel68 = new javax.swing.JLabel();
         jTextField36 = new javax.swing.JTextField();
         panelEventos = new javax.swing.JPanel();
         jLabel20 = new javax.swing.JLabel();
         nombreEvento_jTextField = new javax.swing.JTextField();
         jLabel64 = new javax.swing.JLabel();
         porDias_jCheckBox = new javax.swing.JCheckBox();
         jLabel36 = new javax.swing.JLabel();
         porFechaYHora_jPanel = new javax.swing.JPanel();
         jDateChooser2 = new com.toedter.calendar.JDateChooser();
         jLabel15 = new javax.swing.JLabel();
         jLabel24 = new javax.swing.JLabel();
         jFormattedTextField2 = new javax.swing.JFormattedTextField();
         porDiasEvento_jPanel = new javax.swing.JPanel();
         jLabel16 = new javax.swing.JLabel();
         lunes_jCheckBox = new javax.swing.JCheckBox();
         martes_jCheckBox = new javax.swing.JCheckBox();
         miercoles_jCheckBox = new javax.swing.JCheckBox();
         jueves_jCheckBox = new javax.swing.JCheckBox();
         viernes_jCheckBox = new javax.swing.JCheckBox();
         sabado_jCheckBox = new javax.swing.JCheckBox();
         domingo_jCheckBox = new javax.swing.JCheckBox();
         jLabel25 = new javax.swing.JLabel();
         horaDiasEvento_jFormattedTextField = new javax.swing.JFormattedTextField();
         jPanel9 = new javax.swing.JPanel();
         jButton26 = new javax.swing.JButton();
         jButton27 = new javax.swing.JButton();
         jButton28 = new javax.swing.JButton();
         jButton29 = new javax.swing.JButton();
         jScrollPane13 = new javax.swing.JScrollPane();
         jList12 = new javax.swing.JList();
         jScrollPane14 = new javax.swing.JScrollPane();
         jList13 = new javax.swing.JList();
         selBalastosEntradas_jCheckBox = new javax.swing.JCheckBox();
         selGruposEntradas_jCheckBox = new javax.swing.JCheckBox();
         selEscenaEntrada_jCheckBox = new javax.swing.JCheckBox();
         jLabel21 = new javax.swing.JLabel();
         jLabel22 = new javax.swing.JLabel();
         jLabel23 = new javax.swing.JLabel();
         jTextField25 = new javax.swing.JTextField();
         jButton48 = new javax.swing.JButton();
         enviarEventos_jButton = new javax.swing.JButton();
         eliminarEvento_jButton = new javax.swing.JButton();
         eventoNum_jComboBox = new javax.swing.JComboBox();
         panelConfiguracion = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         jDateChooser1 = new com.toedter.calendar.JDateChooser();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jFormattedTextField1 = new javax.swing.JFormattedTextField();
         jButton24 = new javax.swing.JButton();
         enviarConfiguracion_jButton2 = new javax.swing.JButton();
         configuracionRed_jPanel = new javax.swing.JPanel();
         cbIsStaticConfiguration = new javax.swing.JCheckBox();
         labelIp = new javax.swing.JLabel();
         ip_jTextField = new javax.swing.JTextField();
         mask_jTextField = new javax.swing.JFormattedTextField();
         gateway_jTextField = new javax.swing.JFormattedTextField();
         puerto_jTextField = new javax.swing.JTextField();
         labelMascara = new javax.swing.JLabel();
         labelGateway = new javax.swing.JLabel();
         labelPuerto = new javax.swing.JLabel();
         configuracionBalastos_jPanel = new javax.swing.JPanel();
         balastoDali_jPanel = new javax.swing.JPanel();
         jLabel27 = new javax.swing.JLabel();
         jLabel28 = new javax.swing.JLabel();
         jLabel29 = new javax.swing.JLabel();
         jLabel30 = new javax.swing.JLabel();
         jLabel31 = new javax.swing.JLabel();
         jLabel32 = new javax.swing.JLabel();
         pot_jLabel = new javax.swing.JLabel();
         balastoMin_jTextField = new javax.swing.JTextField();
         balastoMax_jTextField = new javax.swing.JTextField();
         balastoFT_jTextField = new javax.swing.JTextField();
         balastoFR_jTextField = new javax.swing.JTextField();
         balastoLF_jTextField = new javax.swing.JTextField();
         balastoLX_jTextField = new javax.swing.JTextField();
         balastoPot_jTextField = new javax.swing.JTextField();
         gruposDeBalasto_jPanel = new javax.swing.JPanel();
         grupo_jCheckBox1 = new javax.swing.JCheckBox();
         grupo_jCheckBox2 = new javax.swing.JCheckBox();
         grupo_jCheckBox3 = new javax.swing.JCheckBox();
         grupo_jCheckBox4 = new javax.swing.JCheckBox();
         grupo_jCheckBox5 = new javax.swing.JCheckBox();
         grupo_jCheckBox6 = new javax.swing.JCheckBox();
         grupo_jCheckBox7 = new javax.swing.JCheckBox();
         grupo_jCheckBox8 = new javax.swing.JCheckBox();
         grupo_jCheckBox9 = new javax.swing.JCheckBox();
         grupo_jCheckBox10 = new javax.swing.JCheckBox();
         grupo_jCheckBox11 = new javax.swing.JCheckBox();
         grupo_jCheckBox12 = new javax.swing.JCheckBox();
         grupo_jCheckBox13 = new javax.swing.JCheckBox();
         grupo_jCheckBox14 = new javax.swing.JCheckBox();
         grupo_jCheckBox15 = new javax.swing.JCheckBox();
         grupo_jCheckBox16 = new javax.swing.JCheckBox();
         escenasDeBalasto_jPanel = new javax.swing.JPanel();
         sliderConValor1 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor2 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor3 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor4 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor5 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor6 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor7 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor8 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor9 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor10 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor11 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor12 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor13 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor14 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor15 = new com.isolux.view.componentes.SliderConValor();
         sliderConValor16 = new com.isolux.view.componentes.SliderConValor();
         balastoConfigBasico_jPanel = new javax.swing.JPanel();
         jLabel35 = new javax.swing.JLabel();
         balastoDir_jTextField = new javax.swing.JTextField();
         balastoConfiguracion_jComboBox = new javax.swing.JComboBox();
         balastoEscribirConfig_jButton = new javax.swing.JButton();
         balastoResetConfig_jButton = new javax.swing.JButton();
         jLabel5 = new javax.swing.JLabel();
         cambiarDir_jButton = new javax.swing.JButton();
         monitoreoTiempoReal_jPanel = new javax.swing.JPanel();
         panelTiempoReal = new javax.swing.JPanel();
         jComboBox2 = new javax.swing.JComboBox();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jLabel44 = new javax.swing.JLabel();
         jButton38 = new javax.swing.JButton();
         jLabel53 = new javax.swing.JLabel();
         jLabel54 = new javax.swing.JLabel();
         jLabel55 = new javax.swing.JLabel();
         jSlider1 = new javax.swing.JSlider();
         jSpinner3 = new javax.swing.JSpinner();
         jLabel56 = new javax.swing.JLabel();
         jButton44 = new javax.swing.JButton();
         jLabel65 = new javax.swing.JLabel();
         jButton46 = new javax.swing.JButton();
         jButton47 = new javax.swing.JButton();
         statusBar = new javax.swing.JPanel();
         jLabel34 = new javax.swing.JLabel();
         jLabel45 = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jSeparator2 = new javax.swing.JSeparator();
         statusLabel = new javax.swing.JLabel();
         barraProgreso_jProgressBar = new javax.swing.JProgressBar();
         mainMenuBar = new javax.swing.JMenuBar();
         archivo_jMenu = new javax.swing.JMenu();
         guardarEnFlash_jMenuItem = new javax.swing.JMenuItem();
         jSeparator4 = new javax.swing.JPopupMenu.Separator();
         formatear_jMenuItem = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JPopupMenu.Separator();
         config_jMenuItem = new javax.swing.JMenuItem();
 
         jTextField2.setText("jTextField2");
 
         repetirCiclo_jCheckBox.setText("Repetir ciclo");
         repetirCiclo_jCheckBox.setEnabled(false);
 
         leerFlash_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
         leerFlash_jMenuItem.setText("Leer flash");
         leerFlash_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 readFromFlash(evt);
             }
         });
 
         nivel_jLabel.setText("Nivel");
 
         nivel_jTextField.setText("0");
 
         labelDns.setText("Puerto");
         labelDns.setEnabled(false);
 
         fieldPort.setToolTipText("Puerto por defecto: 502");
         fieldPort.setEnabled(false);
         fieldPort.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(65535));
 
         grabarIp_jButton.setText("Grabar Ip");
         grabarIp_jButton.setToolTipText("<html><p>Escribe la ip en la tarjeta.<br>\nLa nueva ip es la ip que se encuentre en la caja de texto correspondiente</p>\n\n</html>");
         grabarIp_jButton.setEnabled(false);
 
         jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Balastos"));
         jPanel3.setPreferredSize(new java.awt.Dimension(800, 442));
 
         jButton3.setText("<");
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         jButton2.setText(">");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jButton4.setText(">>");
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jButton5.setText("<<");
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
 
         jScrollPane3.setViewportView(jList2);
 
         jScrollPane4.setViewportView(jList3);
 
         jLabel39.setText("Disponibles");
 
         jLabel57.setText("Agregados");
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(jLabel39)
                         .addGap(225, 225, 225)))
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel57, javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel39)
                     .addComponent(jLabel57))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         escenasBalastos_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Balastos"));
         escenasBalastos_jPanel.setPreferredSize(new java.awt.Dimension(800, 413));
 
         agregarBalastoEscena_jButton.setText(">");
         agregarBalastoEscena_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 agregarBalastoEscena_jButtonActionPerformed(evt);
             }
         });
 
         removerBalastoEscena_jButton.setText("<");
         removerBalastoEscena_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removerBalastoEscena_jButtonActionPerformed(evt);
             }
         });
 
         agregarTodosBalastosEscena_jButton.setText(">>");
         agregarTodosBalastosEscena_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 agregarTodosBalastosEscena_jButtonActionPerformed(evt);
             }
         });
 
         removerTodosBalastosEscenas_jButton.setText("<<");
         removerTodosBalastosEscenas_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removerTodosBalastosEscenas_jButtonActionPerformed(evt);
             }
         });
 
         jScrollPane5.setViewportView(balastrosDisponibles_jList);
 
         balastosAfectados_jList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 selectBalast(evt);
             }
         });
         jScrollPane6.setViewportView(balastosAfectados_jList);
 
         javax.swing.GroupLayout escenasBalastos_jPanelLayout = new javax.swing.GroupLayout(escenasBalastos_jPanel);
         escenasBalastos_jPanel.setLayout(escenasBalastos_jPanelLayout);
         escenasBalastos_jPanelLayout.setHorizontalGroup(
             escenasBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(escenasBalastos_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(escenasBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER, false)
                     .addComponent(agregarBalastoEscena_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(removerBalastoEscena_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(agregarTodosBalastosEscena_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(removerTodosBalastosEscenas_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                 .addContainerGap())
         );
         escenasBalastos_jPanelLayout.setVerticalGroup(
             escenasBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, escenasBalastos_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(escenasBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                     .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE))
                 .addContainerGap())
             .addGroup(escenasBalastos_jPanelLayout.createSequentialGroup()
                 .addGap(11, 11, 11)
                 .addComponent(agregarBalastoEscena_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(removerBalastoEscena_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(agregarTodosBalastosEscena_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(removerTodosBalastosEscenas_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                 .addContainerGap(41, Short.MAX_VALUE))
         );
 
         actualizarNivelEscena_jButton.setText("Actualizar nivel");
         actualizarNivelEscena_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 actualizarNivelEscena_jButtonActionPerformed(evt);
             }
         });
 
         jLabel18.setText("Balasto:");
 
         jLabel19.setText("#");
 
         nivelEscena_jLabel.setText("Nivel");
 
         jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Rol"));
         jPanel13.setPreferredSize(new java.awt.Dimension(560, 60));
 
         confRol.add(rbIsMaster);
         rbIsMaster.setText("Maestro");
 
         confRol.add(rbIsSlave);
         rbIsSlave.setSelected(true);
         rbIsSlave.setText("Esclavo");
 
         javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(rbIsMaster)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(rbIsSlave)
                 .addContainerGap(482, Short.MAX_VALUE))
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(rbIsMaster)
                     .addComponent(rbIsSlave))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("SMAIEE - Configuración DALI");
         getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));
 
         principal_jScrollPane.setPreferredSize(new java.awt.Dimension(1067, 703));
 
         panelPrincipal_jPanel.setPreferredSize(new java.awt.Dimension(800, 701));
         panelPrincipal_jPanel.setLayout(new java.awt.BorderLayout());
 
         header.setBackground(new java.awt.Color(0, 101, 137));
 
         headerImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/title.png"))); // NOI18N
 
         javax.swing.GroupLayout headerLayout = new javax.swing.GroupLayout(header);
         header.setLayout(headerLayout);
         headerLayout.setHorizontalGroup(
             headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(headerImage, javax.swing.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE)
         );
         headerLayout.setVerticalGroup(
             headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(headerLayout.createSequentialGroup()
                 .addComponent(headerImage)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         panelPrincipal_jPanel.add(header, java.awt.BorderLayout.NORTH);
 
         tabbedPane.setName("tabbedPane"); // NOI18N
         tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 selectTab(evt);
             }
         });
 
         configuracionSmaiee_jPanel.setName("configuracionSmaie"); // NOI18N
         configuracionSmaiee_jPanel.setPreferredSize(new java.awt.Dimension(800, 574));
 
         javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("SMAIEE");
         javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Balastos");
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Grupos");
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Escenas");
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Entradas");
         javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Botoneras");
         treeNode2.add(treeNode3);
         treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Fotoceldas");
         treeNode2.add(treeNode3);
         treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Sensores");
         treeNode2.add(treeNode3);
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Eventos");
         treeNode1.add(treeNode2);
         arbol_jTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         arbol_jTree.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         arbol_jTree.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 arbol_jTreeMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 arbol_jTreeMousePressed(evt);
             }
         });
         arbol_jTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
             public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                 arbol_jTreeValueChanged(evt);
             }
         });
         arbolJerarquia_jScrollPane.setViewportView(arbol_jTree);
 
         panelPpal.setLayout(new java.awt.CardLayout());
 
         panelBienvenida.setPreferredSize(new java.awt.Dimension(800, 552));
 
         javax.swing.GroupLayout panelBienvenidaLayout = new javax.swing.GroupLayout(panelBienvenida);
         panelBienvenida.setLayout(panelBienvenidaLayout);
         panelBienvenidaLayout.setHorizontalGroup(
             panelBienvenidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 633, Short.MAX_VALUE)
         );
         panelBienvenidaLayout.setVerticalGroup(
             panelBienvenidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 553, Short.MAX_VALUE)
         );
 
         panelPpal.add(panelBienvenida, "card8");
 
         panelBalastos.setBorder(javax.swing.BorderFactory.createTitledBorder("Balastos"));
 
         balastoEnviar_jButton.setText("Enviar");
         balastoEnviar_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoEnviar_jButtonActionPerformed(evt);
             }
         });
 
         balastoEliminar_jButton.setText("Eliminar");
         balastoEliminar_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoEliminar_jButtonActionPerformed(evt);
             }
         });
 
         jLabel1.setText("Nombre");
 
         balastoNombreSmaiee_jTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoNombreSmaiee_jTextFieldActionPerformed(evt);
             }
         });
 
         jLabel41.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel41.setText("#");
 
         balastoNum_jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Seleccione un número", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63" }));
         balastoNum_jComboBox.setToolTipText("Dirección Dali");
         balastoNum_jComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoNum_jComboBoxActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelBalastosLayout = new javax.swing.GroupLayout(panelBalastos);
         panelBalastos.setLayout(panelBalastosLayout);
         panelBalastosLayout.setHorizontalGroup(
             panelBalastosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelBalastosLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(balastoNombreSmaiee_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jLabel41)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)
                 .addGroup(panelBalastosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(panelBalastosLayout.createSequentialGroup()
                         .addComponent(balastoEnviar_jButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(balastoEliminar_jButton))
                     .addGroup(panelBalastosLayout.createSequentialGroup()
                         .addGap(9, 9, 9)
                         .addComponent(balastoNum_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         panelBalastosLayout.setVerticalGroup(
             panelBalastosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelBalastosLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelBalastosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(jLabel1)
                     .addComponent(balastoNombreSmaiee_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel41)
                     .addComponent(balastoNum_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(150, 150, 150)
                 .addGroup(panelBalastosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(balastoEnviar_jButton)
                     .addComponent(balastoEliminar_jButton))
                 .addContainerGap(326, Short.MAX_VALUE))
         );
 
         panelPpal.add(panelBalastos, "card2");
 
         panelGrupos.setBorder(javax.swing.BorderFactory.createTitledBorder("Grupos"));
         panelGrupos.setPreferredSize(new java.awt.Dimension(800, 552));
 
         jLabel4.setText("Nombre");
 
         gruposNombreSmaiee_jTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 gruposNombreSmaiee_jTextFieldActionPerformed(evt);
             }
         });
 
         enviarGrupo_jButton.setText("Enviar");
         enviarGrupo_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 enviarGrupo_jButtonActionPerformed(evt);
             }
         });
 
         eliminarGrupo_jButton.setText("Eliminar");
         eliminarGrupo_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminarGrupo_jButtonActionPerformed(evt);
             }
         });
 
         jLabel58.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel58.setText("#");
 
         gruposNum_jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Seleccione uno", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
         gruposNum_jComboBox.setToolTipText("Dirección Dali");
 
         javax.swing.GroupLayout panelGruposLayout = new javax.swing.GroupLayout(panelGrupos);
         panelGrupos.setLayout(panelGruposLayout);
         panelGruposLayout.setHorizontalGroup(
             panelGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelGruposLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelGruposLayout.createSequentialGroup()
                         .addComponent(jLabel4)
                         .addGap(30, 30, 30)
                         .addComponent(gruposNombreSmaiee_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel58)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 259, Short.MAX_VALUE)
                         .addComponent(gruposNum_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelGruposLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(enviarGrupo_jButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(eliminarGrupo_jButton)))
                 .addContainerGap())
         );
         panelGruposLayout.setVerticalGroup(
             panelGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelGruposLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(gruposNombreSmaiee_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel58)
                     .addComponent(gruposNum_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(151, 151, 151)
                 .addGroup(panelGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(eliminarGrupo_jButton)
                     .addComponent(enviarGrupo_jButton))
                 .addContainerGap(325, Short.MAX_VALUE))
         );
 
         panelPpal.add(panelGrupos, "card4");
 
         panelEscenas.setBorder(javax.swing.BorderFactory.createTitledBorder("Escenas"));
         panelEscenas.setPreferredSize(new java.awt.Dimension(800, 552));
 
         nombreEscena_jLabel.setText("Escena");
 
         nombreEscena_jTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nombreEscena_jTextFieldActionPerformed(evt);
             }
         });
 
         escenaNumero_jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Seleccione una", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
         escenaNumero_jComboBox.setToolTipText("Dirección dalí.");
 
         enviarEscena_jButton.setText("Enviar");
         enviarEscena_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 enviarEscena_jButtonActionPerformed(evt);
             }
         });
 
         eliminarEscena_jButton.setText("Eliminar");
         eliminarEscena_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminarEscena_jButtonActionPerformed(evt);
             }
         });
 
         jLabel61.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel61.setText("#");
 
         javax.swing.GroupLayout panelEscenasLayout = new javax.swing.GroupLayout(panelEscenas);
         panelEscenas.setLayout(panelEscenasLayout);
         panelEscenasLayout.setHorizontalGroup(
             panelEscenasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelEscenasLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelEscenasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelEscenasLayout.createSequentialGroup()
                         .addComponent(nombreEscena_jLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(nombreEscena_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel61)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 297, Short.MAX_VALUE)
                         .addComponent(escenaNumero_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEscenasLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(enviarEscena_jButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(eliminarEscena_jButton)))
                 .addContainerGap())
         );
         panelEscenasLayout.setVerticalGroup(
             panelEscenasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelEscenasLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelEscenasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(nombreEscena_jLabel)
                     .addComponent(nombreEscena_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel61)
                     .addComponent(escenaNumero_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(150, 150, 150)
                 .addGroup(panelEscenasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(enviarEscena_jButton)
                     .addComponent(eliminarEscena_jButton))
                 .addContainerGap(326, Short.MAX_VALUE))
         );
 
         panelPpal.add(panelEscenas, "card5");
 
         panelEntradas.setBorder(javax.swing.BorderFactory.createTitledBorder("Entradas"));
         panelEntradas.setPreferredSize(new java.awt.Dimension(800, 552));
 
         jLabel62.setText("Entrada número:");
 
         jLabel63.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel63.setText("#");
 
         entradaNumero_jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Seleccione uno", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" }));
         entradaNumero_jComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 entradaNumero_jComboBoxActionPerformed(evt);
             }
         });
 
         panelConfEntradas.setPreferredSize(new java.awt.Dimension(800, 487));
         panelConfEntradas.setLayout(new java.awt.CardLayout());
 
         javax.swing.GroupLayout panelEntradaInitLayout = new javax.swing.GroupLayout(panelEntradaInit);
         panelEntradaInit.setLayout(panelEntradaInitLayout);
         panelEntradaInitLayout.setHorizontalGroup(
             panelEntradaInitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 621, Short.MAX_VALUE)
         );
         panelEntradaInitLayout.setVerticalGroup(
             panelEntradaInitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 488, Short.MAX_VALUE)
         );
 
         panelConfEntradas.add(panelEntradaInit, "card5");
 
         panelBotonera.setBorder(javax.swing.BorderFactory.createTitledBorder("Botonera"));
 
         jLabel7.setText("Comportamiento");
 
         botoneraComportamiento.add(jRadioButton1);
         jRadioButton1.setSelected(true);
         jRadioButton1.setText("Mantenido");
 
         botoneraComportamiento.add(jRadioButton2);
         jRadioButton2.setText("Momentaneo");
 
         jLabel8.setText("% ON");
 
         jLabel10.setText("% OFF");
 
         jTextField5.setText("100");
 
         jTextField6.setText("0");
 
         jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuración"));
 
         jButton30.setText("<");
         jButton30.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton30ActionPerformed(evt);
             }
         });
 
         jButton31.setText(">");
         jButton31.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton31ActionPerformed(evt);
             }
         });
 
         jButton32.setText(">>");
         jButton32.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton32.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton32.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton32.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton32ActionPerformed(evt);
             }
         });
 
         jButton33.setText("<<");
         jButton33.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton33.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton33.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton33.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton33ActionPerformed(evt);
             }
         });
 
         jScrollPane15.setViewportView(jList14);
 
         jScrollPane16.setViewportView(jList15);
 
         balastros_jCheckBox.setText("Balastos");
         balastros_jCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastros_jCheckBoxActionPerformed(evt);
             }
         });
 
         grupos_jCheckBox.setText("Grupos");
         grupos_jCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 grupos_jCheckBoxActionPerformed(evt);
             }
         });
 
         jCheckBox18.setText("Escena");
         jCheckBox18.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox18ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jButton32, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                             .addComponent(jButton31, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                             .addComponent(jButton30, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                             .addComponent(jButton33, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addComponent(balastros_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(grupos_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jCheckBox18)))
                 .addContainerGap())
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(balastros_jCheckBox)
                     .addComponent(grupos_jCheckBox)
                     .addComponent(jCheckBox18))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                         .addComponent(jButton31, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton30, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton32, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton33, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                     .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         jButton14.setText("Enviar");
         jButton14.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton14ActionPerformed(evt);
             }
         });
 
         jLabel17.setText("Tipo contacto");
 
         botoneraTipoContacto.add(jRadioButton3);
         jRadioButton3.setSelected(true);
         jRadioButton3.setText("NA");
 
         botoneraTipoContacto.add(jRadioButton4);
         jRadioButton4.setText("NC");
 
         jButton40.setText("Eliminar");
         jButton40.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton40ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelBotoneraLayout = new javax.swing.GroupLayout(panelBotonera);
         panelBotonera.setLayout(panelBotoneraLayout);
         panelBotoneraLayout.setHorizontalGroup(
             panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelBotoneraLayout.createSequentialGroup()
                 .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelBotoneraLayout.createSequentialGroup()
                         .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelBotoneraLayout.createSequentialGroup()
                                 .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(panelBotoneraLayout.createSequentialGroup()
                                         .addGap(10, 10, 10)
                                         .addComponent(jLabel7)
                                         .addGap(18, 18, 18)
                                         .addComponent(jRadioButton1))
                                     .addGroup(panelBotoneraLayout.createSequentialGroup()
                                         .addContainerGap()
                                         .addComponent(jLabel17)
                                         .addGap(32, 32, 32)
                                         .addComponent(jRadioButton3)))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jRadioButton4)
                                     .addComponent(jRadioButton2)))
                             .addGroup(panelBotoneraLayout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(jLabel8)
                                 .addGap(23, 23, 23)
                                 .addComponent(jTextField5)
                                 .addGap(77, 77, 77)
                                 .addComponent(jLabel10)))
                         .addGap(18, 18, 18)
                         .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE))
                     .addGroup(panelBotoneraLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBotoneraLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(jButton14)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jButton40))
         );
         panelBotoneraLayout.setVerticalGroup(
             panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelBotoneraLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel7)
                     .addComponent(jRadioButton1)
                     .addComponent(jRadioButton2))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel17)
                     .addComponent(jRadioButton3)
                     .addComponent(jRadioButton4))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel10)
                     .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelBotoneraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton14)
                     .addComponent(jButton40))
                 .addGap(5, 5, 5))
         );
 
         panelConfEntradas.add(panelBotonera, "card2");
 
         panelFotocelda.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Fotocelda")));
 
         jLabel11.setText("Tiempo de retardo");
 
         jLabel12.setText("Nivel ilum/voltio");
 
         jTextField7.setText("5");
 
         entradaFotoceldaNivelIlum_jTextField.setText("10");
         entradaFotoceldaNivelIlum_jTextField.setToolTipText("");
 
         jLabel13.setText("Nivel deseado");
 
         entradaFotoceldaNivelDeseado_jTextField.setText("50");
         entradaFotoceldaNivelDeseado_jTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 entradaFotoceldaNivelDeseado_jTextFieldActionPerformed(evt);
             }
         });
 
         jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuración"));
 
         jButton18.setText("<");
         jButton18.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton18ActionPerformed(evt);
             }
         });
 
         jButton19.setText(">");
         jButton19.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton19ActionPerformed(evt);
             }
         });
 
         jButton20.setText(">>");
         jButton20.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton20.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton20.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton20.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton20ActionPerformed(evt);
             }
         });
 
         jButton21.setText("<<");
         jButton21.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton21.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton21.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton21.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton21ActionPerformed(evt);
             }
         });
 
         jScrollPane9.setViewportView(jList8);
 
         jScrollPane10.setViewportView(jList9);
 
         jCheckBox10.setText("Balastos");
         jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox10ActionPerformed(evt);
             }
         });
 
         jCheckBox11.setText("Grupos");
         jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox11ActionPerformed(evt);
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
                         .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jButton20, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                             .addComponent(jButton19, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                             .addComponent(jButton18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                             .addComponent(jButton21, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
                     .addGroup(jPanel7Layout.createSequentialGroup()
                         .addComponent(jCheckBox10)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jCheckBox11)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jCheckBox10)
                     .addComponent(jCheckBox11))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                     .addComponent(jScrollPane10, 0, 0, Short.MAX_VALUE)
                     .addGroup(jPanel7Layout.createSequentialGroup()
                         .addComponent(jButton19, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton18, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                         .addGap(7, 7, 7)
                         .addComponent(jButton20, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton21, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         jLabel14.setText("Ganancia");
 
         jTextField10.setText("1");
 
         jButton15.setText("Enviar");
         jButton15.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton15ActionPerformed(evt);
             }
         });
 
         jLabel37.setText("seg");
 
         jButton41.setText("Eliminar");
         jButton41.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton41ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelFotoceldaLayout = new javax.swing.GroupLayout(panelFotocelda);
         panelFotocelda.setLayout(panelFotoceldaLayout);
         panelFotoceldaLayout.setHorizontalGroup(
             panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelFotoceldaLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(panelFotoceldaLayout.createSequentialGroup()
                         .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel11)
                             .addComponent(jLabel12))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jTextField7)
                             .addComponent(entradaFotoceldaNivelIlum_jTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel37)
                         .addGap(13, 13, 13)
                         .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel13)
                             .addComponent(jLabel14))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(entradaFotoceldaNivelDeseado_jTextField)
                             .addComponent(jTextField10, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFotoceldaLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(jButton15)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton41)))
                 .addContainerGap())
         );
         panelFotoceldaLayout.setVerticalGroup(
             panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelFotoceldaLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel11)
                     .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel14)
                     .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel37))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel12)
                     .addComponent(jLabel13)
                     .addComponent(entradaFotoceldaNivelDeseado_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(entradaFotoceldaNivelIlum_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelFotoceldaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton15)
                     .addComponent(jButton41))
                 .addGap(5, 5, 5))
         );
 
         panelConfEntradas.add(panelFotocelda, "card3");
 
         panelSensor.setBorder(javax.swing.BorderFactory.createTitledBorder("Sensor"));
 
         jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuración"));
 
         jButton34.setText("<");
         jButton34.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton34ActionPerformed(evt);
             }
         });
 
         jButton35.setText(">");
         jButton35.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton35ActionPerformed(evt);
             }
         });
 
         jButton36.setText(">>");
         jButton36.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton36.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton36.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton36.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton36ActionPerformed(evt);
             }
         });
 
         jButton37.setText("<<");
         jButton37.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton37.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton37.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton37.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton37ActionPerformed(evt);
             }
         });
 
         jScrollPane17.setViewportView(jList16);
 
         jScrollPane18.setViewportView(jList17);
 
         jCheckBox19.setText("Balastos");
         jCheckBox19.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox19ActionPerformed(evt);
             }
         });
 
         jCheckBox20.setText("Grupos");
         jCheckBox20.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox20ActionPerformed(evt);
             }
         });
 
         fotoceldas_entrada_escenas_jCheckBox.setText("Escena");
         fotoceldas_entrada_escenas_jCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fotoceldas_entrada_escenas_jCheckBoxActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel11Layout.createSequentialGroup()
                         .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jButton36, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                             .addComponent(jButton35, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                             .addComponent(jButton34, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                             .addComponent(jButton37, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                     .addGroup(jPanel11Layout.createSequentialGroup()
                         .addComponent(jCheckBox19)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jCheckBox20)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(fotoceldas_entrada_escenas_jCheckBox)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jCheckBox19)
                     .addComponent(jCheckBox20)
                     .addComponent(fotoceldas_entrada_escenas_jCheckBox))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                         .addComponent(jButton35, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton34, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                         .addGap(6, 6, 6)
                         .addComponent(jButton36, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton37, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                     .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         jButton16.setText("Enviar");
         jButton16.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton16ActionPerformed(evt);
             }
         });
 
         jButton42.setText("Eliminar");
         jButton42.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton42ActionPerformed(evt);
             }
         });
 
         jLabel66.setText("Nivel ON");
 
         jTextField34.setText("0");
 
         jLabel67.setText("Nivel OFF");
 
         jTextField35.setText("0");
 
         jLabel68.setText("Retardo");
 
         jTextField36.setText("0");
 
         javax.swing.GroupLayout panelSensorLayout = new javax.swing.GroupLayout(panelSensor);
         panelSensor.setLayout(panelSensorLayout);
         panelSensorLayout.setHorizontalGroup(
             panelSensorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelSensorLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelSensorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(panelSensorLayout.createSequentialGroup()
                         .addComponent(jLabel66)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel67)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel68)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSensorLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(jButton16)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton42)))
                 .addContainerGap())
         );
         panelSensorLayout.setVerticalGroup(
             panelSensorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSensorLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelSensorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel66)
                     .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel67)
                     .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel68)
                     .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelSensorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton16)
                     .addComponent(jButton42))
                 .addGap(5, 5, 5))
         );
 
         panelConfEntradas.add(panelSensor, "card4");
 
         javax.swing.GroupLayout panelEntradasLayout = new javax.swing.GroupLayout(panelEntradas);
         panelEntradas.setLayout(panelEntradasLayout);
         panelEntradasLayout.setHorizontalGroup(
             panelEntradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(panelConfEntradas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEntradasLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel62)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jLabel63)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(entradaNumero_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         panelEntradasLayout.setVerticalGroup(
             panelEntradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelEntradasLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelEntradasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(entradaNumero_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel62)
                     .addComponent(jLabel63))
                 .addGap(11, 11, 11)
                 .addComponent(panelConfEntradas, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE))
         );
 
         panelPpal.add(panelEntradas, "card6");
 
         panelEventos.setBorder(javax.swing.BorderFactory.createTitledBorder("Eventos"));
         panelEventos.setPreferredSize(new java.awt.Dimension(800, 552));
 
         jLabel20.setText("Nombre");
 
         jLabel64.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel64.setText("#");
 
         porDias_jCheckBox.setText("Por días");
         porDias_jCheckBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 porDias_jCheckBoxItemStateChanged(evt);
             }
         });
         porDias_jCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 porDias_jCheckBoxActionPerformed(evt);
             }
         });
 
         jLabel36.setText("Número:");
 
         porFechaYHora_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Fecha y hora"));
 
         jLabel15.setText("Fecha");
 
         jLabel24.setText("Hora");
 
         jFormattedTextField2.setText("00:00");
 
         javax.swing.GroupLayout porFechaYHora_jPanelLayout = new javax.swing.GroupLayout(porFechaYHora_jPanel);
         porFechaYHora_jPanel.setLayout(porFechaYHora_jPanelLayout);
         porFechaYHora_jPanelLayout.setHorizontalGroup(
             porFechaYHora_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(porFechaYHora_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(porFechaYHora_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel15)
                     .addComponent(jLabel24))
                 .addGap(18, 18, 18)
                 .addGroup(porFechaYHora_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jFormattedTextField2)
                     .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         porFechaYHora_jPanelLayout.setVerticalGroup(
             porFechaYHora_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(porFechaYHora_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(porFechaYHora_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel15)
                     .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(8, 8, 8)
                 .addGroup(porFechaYHora_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel24)
                     .addComponent(jFormattedTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         porDiasEvento_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Días"));
 
         jLabel16.setText("Días");
         jLabel16.setEnabled(false);
 
         lunes_jCheckBox.setText("L");
         lunes_jCheckBox.setEnabled(false);
 
         martes_jCheckBox.setText("M");
         martes_jCheckBox.setEnabled(false);
 
         miercoles_jCheckBox.setText("M");
         miercoles_jCheckBox.setEnabled(false);
 
         jueves_jCheckBox.setText("J");
         jueves_jCheckBox.setEnabled(false);
 
         viernes_jCheckBox.setText("V");
         viernes_jCheckBox.setEnabled(false);
 
         sabado_jCheckBox.setText("S");
         sabado_jCheckBox.setEnabled(false);
 
         domingo_jCheckBox.setText("D");
         domingo_jCheckBox.setEnabled(false);
 
         jLabel25.setText("Hora");
         jLabel25.setEnabled(false);
 
         horaDiasEvento_jFormattedTextField.setText("00:00");
         horaDiasEvento_jFormattedTextField.setEnabled(false);
 
         javax.swing.GroupLayout porDiasEvento_jPanelLayout = new javax.swing.GroupLayout(porDiasEvento_jPanel);
         porDiasEvento_jPanel.setLayout(porDiasEvento_jPanelLayout);
         porDiasEvento_jPanelLayout.setHorizontalGroup(
             porDiasEvento_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(porDiasEvento_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(porDiasEvento_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(porDiasEvento_jPanelLayout.createSequentialGroup()
                         .addComponent(jLabel25)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(horaDiasEvento_jFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(porDiasEvento_jPanelLayout.createSequentialGroup()
                         .addComponent(jLabel16)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(lunes_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(martes_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(miercoles_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jueves_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(viernes_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(sabado_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(domingo_jCheckBox)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         porDiasEvento_jPanelLayout.setVerticalGroup(
             porDiasEvento_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(porDiasEvento_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(porDiasEvento_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel16)
                     .addComponent(lunes_jCheckBox)
                     .addComponent(martes_jCheckBox)
                     .addComponent(miercoles_jCheckBox)
                     .addComponent(jueves_jCheckBox)
                     .addComponent(viernes_jCheckBox)
                     .addComponent(sabado_jCheckBox)
                     .addComponent(domingo_jCheckBox))
                 .addGap(9, 9, 9)
                 .addGroup(porDiasEvento_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel25)
                     .addComponent(horaDiasEvento_jFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuración"));
         jPanel9.setPreferredSize(new java.awt.Dimension(550, 338));
 
         jButton26.setText("<");
         jButton26.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton26ActionPerformed(evt);
             }
         });
 
         jButton27.setText(">");
         jButton27.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton27ActionPerformed(evt);
             }
         });
 
         jButton28.setText(">>");
         jButton28.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton28.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton28.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton28.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton28ActionPerformed(evt);
             }
         });
 
         jButton29.setText("<<");
         jButton29.setMaximumSize(new java.awt.Dimension(41, 23));
         jButton29.setMinimumSize(new java.awt.Dimension(41, 23));
         jButton29.setPreferredSize(new java.awt.Dimension(41, 23));
         jButton29.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton29ActionPerformed(evt);
             }
         });
 
         jScrollPane13.setViewportView(jList12);
 
         jList13.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 selectElements(evt);
             }
         });
         jScrollPane14.setViewportView(jList13);
 
         selBalastosEntradas_jCheckBox.setText("Balastos");
         selBalastosEntradas_jCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selBalastosEntradas_jCheckBoxActionPerformed(evt);
             }
         });
 
         selGruposEntradas_jCheckBox.setText("Grupos");
         selGruposEntradas_jCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selGruposEntradas_jCheckBoxActionPerformed(evt);
             }
         });
 
         selEscenaEntrada_jCheckBox.setText("Escena");
         selEscenaEntrada_jCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selEscenaEntrada_jCheckBoxActionPerformed(evt);
             }
         });
 
         jLabel21.setText("Nivel");
 
         jLabel22.setText("Balasto / Grupo:");
 
         jLabel23.setText("#");
 
         jTextField25.setText("0");
 
         jButton48.setText("Actualizar nivel");
         jButton48.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton48ActionPerformed(evt);
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
                         .addComponent(selBalastosEntradas_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(selGruposEntradas_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(selEscenaEntrada_jCheckBox))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                         .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addComponent(jButton28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addComponent(jButton27, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                                 .addComponent(jButton26, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(jLabel21)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel22)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel23)
                         .addGap(18, 18, 18)
                         .addComponent(jButton48)))
                 .addContainerGap())
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel21)
                     .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel22)
                     .addComponent(jLabel23)
                     .addComponent(jButton48))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(selBalastosEntradas_jCheckBox)
                     .addComponent(selGruposEntradas_jCheckBox)
                     .addComponent(selEscenaEntrada_jCheckBox))
                 .addGap(5, 5, 5)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(jButton27, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton26, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton28, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton29, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
                     .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                     .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         enviarEventos_jButton.setText("Enviar");
         enviarEventos_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 enviarEventos_jButtonActionPerformed(evt);
             }
         });
 
         eliminarEvento_jButton.setText("Eliminar");
         eliminarEvento_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminarEvento_jButtonActionPerformed(evt);
             }
         });
 
         eventoNum_jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
 
         javax.swing.GroupLayout panelEventosLayout = new javax.swing.GroupLayout(panelEventos);
         panelEventos.setLayout(panelEventosLayout);
         panelEventosLayout.setHorizontalGroup(
             panelEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelEventosLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE)
                     .addGroup(panelEventosLayout.createSequentialGroup()
                         .addComponent(jLabel20)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(nombreEvento_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel64)
                         .addGap(18, 18, 18)
                         .addComponent(porDias_jCheckBox)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jLabel36)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(eventoNum_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(panelEventosLayout.createSequentialGroup()
                         .addComponent(porFechaYHora_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(porDiasEvento_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEventosLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(enviarEventos_jButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(eliminarEvento_jButton)))
                 .addContainerGap())
         );
         panelEventosLayout.setVerticalGroup(
             panelEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelEventosLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel20)
                     .addComponent(nombreEvento_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel64)
                     .addComponent(porDias_jCheckBox)
                     .addComponent(jLabel36)
                     .addComponent(eventoNum_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(panelEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(porFechaYHora_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(porDiasEvento_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(panelEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(enviarEventos_jButton)
                     .addComponent(eliminarEvento_jButton))
                 .addContainerGap())
         );
 
         panelPpal.add(panelEventos, "card7");
 
         panelConfiguracion.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuración"));
         panelConfiguracion.setPreferredSize(new java.awt.Dimension(800, 551));
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Hora del sistema"));
         jPanel2.setPreferredSize(new java.awt.Dimension(470, 68));
 
         jDateChooser1.setEnabled(false);
 
         jLabel2.setText("Fecha");
 
         jLabel3.setText("Hora");
 
         jFormattedTextField1.setToolTipText("Formato hora: hh:mm");
         jFormattedTextField1.setEnabled(false);
 
         jButton24.setText("Configurar");
         jButton24.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton24ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton24)
                 .addContainerGap(150, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel3)
                         .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jButton24))
                     .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel2))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         enviarConfiguracion_jButton2.setText("Enviar");
         enviarConfiguracion_jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 enviarConfiguracion_jButton2ActionPerformed(evt);
             }
         });
 
         configuracionRed_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuración de red"));
         configuracionRed_jPanel.setPreferredSize(new java.awt.Dimension(560, 147));
 
         cbIsStaticConfiguration.setText("Configuración estática");
         cbIsStaticConfiguration.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbIsStaticConfigurationActionPerformed(evt);
             }
         });
 
         labelIp.setText("IP");
         labelIp.setEnabled(false);
 
         ip_jTextField.setEnabled(false);
 
         mask_jTextField.setEnabled(false);
 
         gateway_jTextField.setEnabled(false);
 
         puerto_jTextField.setEnabled(false);
         puerto_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(65535));
         puerto_jTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 puerto_jTextFieldActionPerformed(evt);
             }
         });
 
         labelMascara.setText("Máscara");
         labelMascara.setEnabled(false);
 
         labelGateway.setText("Gateway");
         labelGateway.setEnabled(false);
 
         labelPuerto.setText("Puerto");
         labelPuerto.setEnabled(false);
 
         javax.swing.GroupLayout configuracionRed_jPanelLayout = new javax.swing.GroupLayout(configuracionRed_jPanel);
         configuracionRed_jPanel.setLayout(configuracionRed_jPanelLayout);
         configuracionRed_jPanelLayout.setHorizontalGroup(
             configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(configuracionRed_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(labelIp)
                     .addComponent(labelMascara)
                     .addComponent(labelPuerto))
                 .addGap(13, 13, 13)
                 .addGroup(configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(puerto_jTextField)
                     .addComponent(mask_jTextField)
                     .addComponent(ip_jTextField))
                 .addGap(18, 18, 18)
                 .addComponent(labelGateway)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gateway_jTextField)
                 .addGap(171, 171, 171))
             .addGroup(configuracionRed_jPanelLayout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addComponent(cbIsStaticConfiguration)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         configuracionRed_jPanelLayout.setVerticalGroup(
             configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(configuracionRed_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(cbIsStaticConfiguration)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(labelGateway)
                         .addComponent(gateway_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(labelIp)
                         .addComponent(ip_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(mask_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labelMascara))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(configuracionRed_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(labelPuerto)
                     .addComponent(puerto_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(20, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout panelConfiguracionLayout = new javax.swing.GroupLayout(panelConfiguracion);
         panelConfiguracion.setLayout(panelConfiguracionLayout);
         panelConfiguracionLayout.setHorizontalGroup(
             panelConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelConfiguracionLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(configuracionRed_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
                     .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConfiguracionLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(enviarConfiguracion_jButton2)
                 .addContainerGap())
         );
         panelConfiguracionLayout.setVerticalGroup(
             panelConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelConfiguracionLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(configuracionRed_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(enviarConfiguracion_jButton2)
                 .addContainerGap(245, Short.MAX_VALUE))
         );
 
         panelPpal.add(panelConfiguracion, "card3");
 
         javax.swing.GroupLayout configuracionSmaiee_jPanelLayout = new javax.swing.GroupLayout(configuracionSmaiee_jPanel);
         configuracionSmaiee_jPanel.setLayout(configuracionSmaiee_jPanelLayout);
         configuracionSmaiee_jPanelLayout.setHorizontalGroup(
             configuracionSmaiee_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(configuracionSmaiee_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(arbolJerarquia_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(652, Short.MAX_VALUE))
             .addGroup(configuracionSmaiee_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configuracionSmaiee_jPanelLayout.createSequentialGroup()
                     .addGap(157, 157, 157)
                     .addComponent(panelPpal, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         configuracionSmaiee_jPanelLayout.setVerticalGroup(
             configuracionSmaiee_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(configuracionSmaiee_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(arbolJerarquia_jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(configuracionSmaiee_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(configuracionSmaiee_jPanelLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(panelPpal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
 
         tabbedPane.addTab("Configuración Smaiee", null, configuracionSmaiee_jPanel, "Configuracion del Smaiee");
 
         configuracionBalastos_jPanel.setName("configuracionDeBalastos"); // NOI18N
         configuracionBalastos_jPanel.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 configuracionBalastos_jPanelFocusGained(evt);
             }
         });
 
         balastoDali_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("DALI"));
         balastoDali_jPanel.setToolTipText("Configuración del balasto");
 
         jLabel27.setText("Min");
 
         jLabel28.setText("Max");
 
         jLabel29.setText("FT");
 
         jLabel30.setText("FR");
 
         jLabel31.setText("LF");
 
         jLabel32.setText("LX");
 
         pot_jLabel.setText("Pot");
 
         balastoMin_jTextField.setText("0");
         balastoMin_jTextField.setToolTipText("Minimo. Valor mínimo del balasto actual");
         balastoMin_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(100));
 
         balastoMax_jTextField.setText("0");
         balastoMax_jTextField.setToolTipText("Máximo. Valor máximo del balasto");
         balastoMax_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(100));
 
         balastoFT_jTextField.setText("0");
         balastoFT_jTextField.setToolTipText("Fade Time. Tiempo entre cambios para cambiar la luminosidad del balasto actual");
         balastoFT_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(15));
 
         balastoFR_jTextField.setText("0");
         balastoFR_jTextField.setToolTipText("Fade Rate: Numero de pasos por segundo para cambiar la luminosidad del balasto actual");
         balastoFR_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(15));
 
         balastoLF_jTextField.setText("0");
         balastoLF_jTextField.setToolTipText("Level Fail");
         balastoLF_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(100));
 
         balastoLX_jTextField.setText("0");
         balastoLX_jTextField.setToolTipText("");
         balastoLX_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(100));
 
         balastoPot_jTextField.setText("0");
         balastoPot_jTextField.setToolTipText("Potencia");
         balastoPot_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(100));
 
         javax.swing.GroupLayout balastoDali_jPanelLayout = new javax.swing.GroupLayout(balastoDali_jPanel);
         balastoDali_jPanel.setLayout(balastoDali_jPanelLayout);
         balastoDali_jPanelLayout.setHorizontalGroup(
             balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(balastoDali_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(balastoDali_jPanelLayout.createSequentialGroup()
                         .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                             .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel28)
                             .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(18, 18, 18)
                         .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(balastoMin_jTextField)
                             .addComponent(balastoMax_jTextField)
                             .addComponent(balastoFT_jTextField))
                         .addGap(73, 73, 73)
                         .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel31)
                             .addComponent(jLabel30)
                             .addComponent(jLabel32)))
                     .addComponent(pot_jLabel))
                 .addGap(18, 18, 18)
                 .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(balastoFR_jTextField)
                     .addComponent(balastoLF_jTextField)
                     .addComponent(balastoLX_jTextField)
                     .addComponent(balastoPot_jTextField))
                 .addContainerGap())
         );
         balastoDali_jPanelLayout.setVerticalGroup(
             balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(balastoDali_jPanelLayout.createSequentialGroup()
                 .addGap(27, 27, 27)
                 .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel27)
                     .addComponent(balastoMin_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel30)
                     .addComponent(balastoFR_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(23, 23, 23)
                 .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel28)
                     .addComponent(balastoMax_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel31)
                     .addComponent(balastoLF_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(26, 26, 26)
                 .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel29)
                     .addComponent(balastoFT_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel32)
                     .addComponent(balastoLX_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(balastoDali_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(pot_jLabel)
                     .addComponent(balastoPot_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(80, Short.MAX_VALUE))
         );
 
         gruposDeBalasto_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Grupos a los que pertenece"));
         gruposDeBalasto_jPanel.setToolTipText("Seleccione los grupos a los que pertenece el balasto actual");
         gruposDeBalasto_jPanel.setLayout(new java.awt.GridBagLayout());
 
         grupo_jCheckBox1.setText("1");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox1, gridBagConstraints);
 
         grupo_jCheckBox2.setText("2");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox2, gridBagConstraints);
 
         grupo_jCheckBox3.setText("3");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox3, gridBagConstraints);
 
         grupo_jCheckBox4.setText("4");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox4, gridBagConstraints);
 
         grupo_jCheckBox5.setText("5");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox5, gridBagConstraints);
 
         grupo_jCheckBox6.setText("6");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox6, gridBagConstraints);
 
         grupo_jCheckBox7.setText("7");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox7, gridBagConstraints);
 
         grupo_jCheckBox8.setText("8");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox8, gridBagConstraints);
 
         grupo_jCheckBox9.setText("9");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox9, gridBagConstraints);
 
         grupo_jCheckBox10.setText("10");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox10, gridBagConstraints);
 
         grupo_jCheckBox11.setText("11");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox11, gridBagConstraints);
 
         grupo_jCheckBox12.setText("12");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox12, gridBagConstraints);
 
         grupo_jCheckBox13.setText("13");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox13, gridBagConstraints);
 
         grupo_jCheckBox14.setText("14");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox14, gridBagConstraints);
 
         grupo_jCheckBox15.setText("15");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox15, gridBagConstraints);
 
         grupo_jCheckBox16.setText("16");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         gruposDeBalasto_jPanel.add(grupo_jCheckBox16, gridBagConstraints);
 
         escenasDeBalasto_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Escenas a las que pertenece"));
         escenasDeBalasto_jPanel.setToolTipText("Seleccione las escenas a las que pertenece el balasto actual");
         escenasDeBalasto_jPanel.setLayout(new java.awt.GridBagLayout());
 
         sliderConValor1.setLabel("1");
         escenasDeBalasto_jPanel.add(sliderConValor1, new java.awt.GridBagConstraints());
 
         sliderConValor2.setLabel("2");
         escenasDeBalasto_jPanel.add(sliderConValor2, new java.awt.GridBagConstraints());
 
         sliderConValor3.setLabel("3");
         escenasDeBalasto_jPanel.add(sliderConValor3, new java.awt.GridBagConstraints());
 
         sliderConValor4.setLabel("4");
         escenasDeBalasto_jPanel.add(sliderConValor4, new java.awt.GridBagConstraints());
 
         sliderConValor5.setLabel("5");
         escenasDeBalasto_jPanel.add(sliderConValor5, new java.awt.GridBagConstraints());
 
         sliderConValor6.setLabel("6");
         escenasDeBalasto_jPanel.add(sliderConValor6, new java.awt.GridBagConstraints());
 
         sliderConValor7.setLabel("7");
         escenasDeBalasto_jPanel.add(sliderConValor7, new java.awt.GridBagConstraints());
 
         sliderConValor8.setLabel("8");
         escenasDeBalasto_jPanel.add(sliderConValor8, new java.awt.GridBagConstraints());
 
         sliderConValor9.setLabel("9");
         escenasDeBalasto_jPanel.add(sliderConValor9, new java.awt.GridBagConstraints());
 
         sliderConValor10.setLabel("10");
         escenasDeBalasto_jPanel.add(sliderConValor10, new java.awt.GridBagConstraints());
 
         sliderConValor11.setLabel("11");
         escenasDeBalasto_jPanel.add(sliderConValor11, new java.awt.GridBagConstraints());
 
         sliderConValor12.setLabel("12");
         escenasDeBalasto_jPanel.add(sliderConValor12, new java.awt.GridBagConstraints());
 
         sliderConValor13.setLabel("13");
         escenasDeBalasto_jPanel.add(sliderConValor13, new java.awt.GridBagConstraints());
 
         sliderConValor14.setLabel("14");
         escenasDeBalasto_jPanel.add(sliderConValor14, new java.awt.GridBagConstraints());
 
         sliderConValor15.setLabel("15");
         escenasDeBalasto_jPanel.add(sliderConValor15, new java.awt.GridBagConstraints());
 
         sliderConValor16.setLabel("16");
         escenasDeBalasto_jPanel.add(sliderConValor16, new java.awt.GridBagConstraints());
 
         balastoConfigBasico_jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Balasto"));
 
         jLabel35.setText("Dir");
 
         balastoDir_jTextField.setText("0");
         balastoDir_jTextField.setToolTipText("Dirección del balasto actual.");
         balastoDir_jTextField.setInputVerifier(new LimitadorDeCaracteresNum_InputVerifier(100));
         balastoDir_jTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoDir_jTextFieldActionPerformed(evt);
             }
         });
         balastoDir_jTextField.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 balastoDir_jTextFieldFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 balastoDir_jTextFieldFocusLost(evt);
             }
         });
 
         balastoConfiguracion_jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         balastoConfiguracion_jComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoConfiguracion_jComboBoxActionPerformed(evt);
             }
         });
         balastoConfiguracion_jComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 balastoConfiguracion_jComboBoxPropertyChange(evt);
             }
         });
 
         balastoEscribirConfig_jButton.setText("Escribir");
         balastoEscribirConfig_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoEscribirConfig_jButtonActionPerformed(evt);
             }
         });
 
         balastoResetConfig_jButton.setText("Reset");
         balastoResetConfig_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 balastoResetConfig_jButtonActionPerformed(evt);
             }
         });
 
         jLabel5.setText("Balasto");
 
         cambiarDir_jButton.setText("Cambiar");
         cambiarDir_jButton.setToolTipText("Cambia la dirección actual del balasto por la nueva");
         cambiarDir_jButton.setEnabled(false);
         cambiarDir_jButton.setName("cambiarDir_jButton"); // NOI18N
         cambiarDir_jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cambiarDir_jButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout balastoConfigBasico_jPanelLayout = new javax.swing.GroupLayout(balastoConfigBasico_jPanel);
         balastoConfigBasico_jPanel.setLayout(balastoConfigBasico_jPanelLayout);
         balastoConfigBasico_jPanelLayout.setHorizontalGroup(
             balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(balastoConfigBasico_jPanelLayout.createSequentialGroup()
                 .addGroup(balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(balastoConfigBasico_jPanelLayout.createSequentialGroup()
                         .addGap(16, 16, 16)
                         .addGroup(balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, balastoConfigBasico_jPanelLayout.createSequentialGroup()
                                 .addGap(0, 0, Short.MAX_VALUE)
                                 .addComponent(cambiarDir_jButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(balastoConfigBasico_jPanelLayout.createSequentialGroup()
                                 .addGroup(balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel35, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGap(14, 14, 14)
                                 .addGroup(balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                     .addComponent(balastoDir_jTextField)
                                     .addComponent(balastoConfiguracion_jComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, balastoConfigBasico_jPanelLayout.createSequentialGroup()
                         .addContainerGap(109, Short.MAX_VALUE)
                         .addComponent(balastoResetConfig_jButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(balastoEscribirConfig_jButton, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         balastoConfigBasico_jPanelLayout.setVerticalGroup(
             balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(balastoConfigBasico_jPanelLayout.createSequentialGroup()
                 .addGap(32, 32, 32)
                 .addGroup(balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(balastoConfiguracion_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(balastoResetConfig_jButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(balastoEscribirConfig_jButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(22, 22, 22)
                 .addGroup(balastoConfigBasico_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(balastoDir_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(cambiarDir_jButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout configuracionBalastos_jPanelLayout = new javax.swing.GroupLayout(configuracionBalastos_jPanel);
         configuracionBalastos_jPanel.setLayout(configuracionBalastos_jPanelLayout);
         configuracionBalastos_jPanelLayout.setHorizontalGroup(
             configuracionBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(configuracionBalastos_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(configuracionBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(gruposDeBalasto_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                     .addComponent(escenasDeBalasto_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(configuracionBalastos_jPanelLayout.createSequentialGroup()
                         .addComponent(balastoConfigBasico_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(balastoDali_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         configuracionBalastos_jPanelLayout.setVerticalGroup(
             configuracionBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(configuracionBalastos_jPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(configuracionBalastos_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(balastoDali_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(balastoConfigBasico_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gruposDeBalasto_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(escenasDeBalasto_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         tabbedPane.addTab("Configuración de balastos", configuracionBalastos_jPanel);
 
         monitoreoTiempoReal_jPanel.setName("MonitoreoTiempoReal"); // NOI18N
         monitoreoTiempoReal_jPanel.setPreferredSize(new java.awt.Dimension(800, 574));
 
         jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Seleccione el área" }));
         jComboBox2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox2ActionPerformed(evt);
             }
         });
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "No", "Nombre", "Nivel"
             }
         ));
         jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 selectBalasto(evt);
             }
         });
         jScrollPane2.setViewportView(jTable1);
 
         jLabel44.setText("Balastos por área");
 
         jButton38.setText("Imagen área");
         jButton38.setEnabled(false);
         jButton38.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton38ActionPerformed(evt);
             }
         });
 
         jLabel53.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel53.setText("Balasto:");
 
         jLabel54.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel54.setText("#");
 
         jLabel55.setText("Nivel");
 
         jSlider1.setForeground(new java.awt.Color(0, 0, 0));
         jSlider1.setPaintLabels(true);
         jSlider1.setPaintTicks(true);
         jSlider1.setSnapToTicks(true);
         jSlider1.setToolTipText("Seleccione el nivel del balasto");
         jSlider1.setValue(0);
         jSlider1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 saveBalastState(evt);
             }
         });
         jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 selectSliderValue(evt);
             }
         });
 
         jLabel56.setBackground(new java.awt.Color(153, 153, 153));
         jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/photo_not_available.jpg"))); // NOI18N
 
         jButton44.setText("Editar");
         jButton44.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton44ActionPerformed(evt);
             }
         });
 
         jLabel65.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel65.setText("#");
 
         jButton46.setText("Eliminar área");
         jButton46.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton46ActionPerformed(evt);
             }
         });
 
         jButton47.setText("Nueva área");
         jButton47.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton47ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelTiempoRealLayout = new javax.swing.GroupLayout(panelTiempoReal);
         panelTiempoReal.setLayout(panelTiempoRealLayout);
         panelTiempoRealLayout.setHorizontalGroup(
             panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelTiempoRealLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTiempoRealLayout.createSequentialGroup()
                         .addComponent(jButton47)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton46))
                     .addGroup(panelTiempoRealLayout.createSequentialGroup()
                         .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(panelTiempoRealLayout.createSequentialGroup()
                                 .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jButton38))
                             .addGroup(panelTiempoRealLayout.createSequentialGroup()
                                 .addComponent(jLabel53)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jLabel54)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jLabel65))
                             .addGroup(panelTiempoRealLayout.createSequentialGroup()
                                 .addComponent(jLabel55)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelTiempoRealLayout.createSequentialGroup()
                                     .addComponent(jLabel44)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                     .addComponent(jButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(0, 0, 0)
                         .addComponent(jLabel56, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         panelTiempoRealLayout.setVerticalGroup(
             panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTiempoRealLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel56, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(panelTiempoRealLayout.createSequentialGroup()
                         .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton38))
                         .addGap(18, 18, 18)
                         .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel53)
                             .addComponent(jLabel54)
                             .addComponent(jLabel65))
                         .addGap(15, 15, 15)
                         .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel55)
                             .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 217, Short.MAX_VALUE)
                         .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel44)
                             .addComponent(jButton44))
                         .addGap(11, 11, 11)
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGap(18, 18, 18)
                 .addGroup(panelTiempoRealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton46)
                     .addComponent(jButton47))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout monitoreoTiempoReal_jPanelLayout = new javax.swing.GroupLayout(monitoreoTiempoReal_jPanel);
         monitoreoTiempoReal_jPanel.setLayout(monitoreoTiempoReal_jPanelLayout);
         monitoreoTiempoReal_jPanelLayout.setHorizontalGroup(
             monitoreoTiempoReal_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 800, Short.MAX_VALUE)
             .addGroup(monitoreoTiempoReal_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(monitoreoTiempoReal_jPanelLayout.createSequentialGroup()
                     .addComponent(panelTiempoReal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGap(0, 204, Short.MAX_VALUE)))
         );
         monitoreoTiempoReal_jPanelLayout.setVerticalGroup(
             monitoreoTiempoReal_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 575, Short.MAX_VALUE)
             .addGroup(monitoreoTiempoReal_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(panelTiempoReal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         tabbedPane.addTab("Monitoreo y control en tiempo real", monitoreoTiempoReal_jPanel);
 
         panelPrincipal_jPanel.add(tabbedPane, java.awt.BorderLayout.CENTER);
 
         statusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         statusBar.setPreferredSize(new java.awt.Dimension(800, 29));
 
         jLabel34.setText("Conexion");
 
         jLabel45.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/connection.jpg"))); // NOI18N
         jLabel45.setMaximumSize(new java.awt.Dimension(33, 9));
         jLabel45.setMinimumSize(new java.awt.Dimension(33, 9));
 
         jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
 
         jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
 
         statusLabel.setText("#");
 
         barraProgreso_jProgressBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 
         javax.swing.GroupLayout statusBarLayout = new javax.swing.GroupLayout(statusBar);
         statusBar.setLayout(statusBarLayout);
         statusBarLayout.setHorizontalGroup(
             statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusBarLayout.createSequentialGroup()
                 .addGap(40, 40, 40)
                 .addComponent(jLabel34)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel45, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                 .addGap(140, 140, 140)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(230, 230, 230)
                 .addComponent(statusLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(barraProgreso_jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         statusBarLayout.setVerticalGroup(
             statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusBarLayout.createSequentialGroup()
                 .addGroup(statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(barraProgreso_jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(statusLabel)
                     .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jLabel45, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusBarLayout.createSequentialGroup()
                 .addComponent(jSeparator2)
                 .addGap(11, 11, 11))
             .addGroup(statusBarLayout.createSequentialGroup()
                 .addComponent(jSeparator1)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         panelPrincipal_jPanel.add(statusBar, java.awt.BorderLayout.PAGE_END);
 
         principal_jScrollPane.setViewportView(panelPrincipal_jPanel);
 
         getContentPane().add(principal_jScrollPane);
 
         archivo_jMenu.setText("Archivo");
 
         guardarEnFlash_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
         guardarEnFlash_jMenuItem.setText("Guardar en flash");
         guardarEnFlash_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 guardarEnFlash(evt);
             }
         });
         archivo_jMenu.add(guardarEnFlash_jMenuItem);
         archivo_jMenu.add(jSeparator4);
 
         formatear_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
         formatear_jMenuItem.setText("Formatear memoria");
         formatear_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 formatearMemoria(evt);
             }
         });
         archivo_jMenu.add(formatear_jMenuItem);
         archivo_jMenu.add(jSeparator3);
 
         config_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         config_jMenuItem.setText("Configuración");
         config_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selectConfiguracion(evt);
             }
         });
         archivo_jMenu.add(config_jMenuItem);
 
         mainMenuBar.add(archivo_jMenu);
 
         setJMenuBar(mainMenuBar);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * ELEMENT EVETNS
      */
     // <editor-fold defaultstate="collapsed" desc="Element events">
     /**
      * Tree selection.
      *
      * @param evt
      */
     protected void arbol_jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_arbol_jTreeValueChanged
 //        this.generalCtrl.treeSelection(this, realCtrl);
     }//GEN-LAST:event_arbol_jTreeValueChanged
 
     /**
      * Load configuration view.
      *
      * @param evt
      */
     protected void selectConfiguracion(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectConfiguracion
         this.generalCtrl.initConfiguration(this);
     }//GEN-LAST:event_selectConfiguracion
 
     /**
      * Select the ins
      *
      * @param evt
      */
     /**
      * Sets the value of the slider in the spinner
      *
      * @param evt
      */
     protected void selectSliderValue(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectSliderValue
         this.realCtrl.setSpinnerValue(this);
     }//GEN-LAST:event_selectSliderValue
 
     /**
      * Send balast
      *
      * @param evt
      */
     protected void balastoEnviar_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoEnviar_jButtonActionPerformed
         try {
             enviarBalastoSmaiee();
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, "Error enviando los datos del balasto en el smaiee", ex);
         }
     }//GEN-LAST:event_balastoEnviar_jButtonActionPerformed
 
     public void enviarBalastoSmaiee() throws Exception{         
         this.balastoCtrl.saveElement(this);
        
     }
     
     /**
      * Show the selection file window. (Real time control).
      *
      * @param evt
      */
     protected void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
         this.realCtrl.uploadAreaPicture(this);
     }//GEN-LAST:event_jButton38ActionPerformed
 
     /**
      * Static ip configuration
      */
     protected void cbIsStaticConfigurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbIsStaticConfigurationActionPerformed
         this.generalCtrl.enableIpConfig(this);
         this.generalCtrl.cargarVarolesIpConfig(this);
     }//GEN-LAST:event_cbIsStaticConfigurationActionPerformed
 
     /**
      * Enviar: Configuracion general
      *
      * @param evt
      */
     protected void enviarConfiguracion_jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarConfiguracion_jButton2ActionPerformed
         jDateChooser1.setEnabled(false);
         jFormattedTextField1.setEnabled(false);
         this.generalCtrl.saveConfiguration(this);
         threadManager.startThreadIfTerminated(ThreadManager.RTC_REFRESHING);
     }//GEN-LAST:event_enviarConfiguracion_jButton2ActionPerformed
 
     /**
      * Delete balast.
      *
      * @param evt
      */
     protected void balastoEliminar_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoEliminar_jButtonActionPerformed
         this.balastoCtrl.deleteElement(this);
     }//GEN-LAST:event_balastoEliminar_jButtonActionPerformed
 
     /**
      * Add balasts to the group.
      *
      * @param evt
      */
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         ViewUtils.addSelected(jList2, jList3);
     }//GEN-LAST:event_jButton2ActionPerformed
 
     /**
      * Remove a balast from the group.
      *
      * @param evt
      */
     private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         ViewUtils.remSelected(jList2, jList3);
     }//GEN-LAST:event_jButton3ActionPerformed
 
     /**
      * Add all balasts to the group.
      *
      * @param evt
      */
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         ViewUtils.addAll(jList2, jList3);
     }//GEN-LAST:event_jButton4ActionPerformed
 
     /**
      * Remove all balasts from the group.
      *
      * @param evt
      */
     private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         ViewUtils.remAll(jList2, jList3);
     }//GEN-LAST:event_jButton5ActionPerformed
 
     /**
      * Send the group.
      *
      * @param evt
      */
     private void enviarGrupo_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarGrupo_jButtonActionPerformed
         try {
             enviarGrupoSmaiee();
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, "Error grabando el grupo", ex);
             JOptionPane.showMessageDialog(this, "No se pudo guardar el grupo","Error",JOptionPane.ERROR_MESSAGE);
         }
         
     }//GEN-LAST:event_enviarGrupo_jButtonActionPerformed
 
     public void enviarGrupoSmaiee()throws Exception{
          this.groupsCtrl.saveElement(this);
          
     }
     
     /**
      * Deletes a group
      *
      * @param evt
      */
     private void eliminarGrupo_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarGrupo_jButtonActionPerformed
         this.groupsCtrl.deleteElement(this);
     }//GEN-LAST:event_eliminarGrupo_jButtonActionPerformed
 
     /**
      * Add balasts to the scene.
      *
      * @param evt
      */
     private void agregarBalastoEscena_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agregarBalastoEscena_jButtonActionPerformed
         ViewUtils.addSelected(balastrosDisponibles_jList, balastosAfectados_jList);
     }//GEN-LAST:event_agregarBalastoEscena_jButtonActionPerformed
 
     /**
      * Remove selected balasts from the scene.
      *
      * @param evt
      */
     private void removerBalastoEscena_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removerBalastoEscena_jButtonActionPerformed
         ViewUtils.remSelected(balastrosDisponibles_jList, balastosAfectados_jList);
     }//GEN-LAST:event_removerBalastoEscena_jButtonActionPerformed
 
     /**
      * Add all available balasts to the scene.
      *
      * @param evt
      */
     private void agregarTodosBalastosEscena_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agregarTodosBalastosEscena_jButtonActionPerformed
         ViewUtils.addAll(balastrosDisponibles_jList, balastosAfectados_jList);
     }//GEN-LAST:event_agregarTodosBalastosEscena_jButtonActionPerformed
 
     /**
      * Remove all selected balasts from the scene.
      *
      * @param evt
      */
     private void removerTodosBalastosEscenas_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removerTodosBalastosEscenas_jButtonActionPerformed
         ViewUtils.remAll(balastrosDisponibles_jList, balastosAfectados_jList);
     }//GEN-LAST:event_removerTodosBalastosEscenas_jButtonActionPerformed
 
     /**
      * Save a scene
      *
      * @param evt
      */
     private void enviarEscena_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarEscena_jButtonActionPerformed
         enviarEscenaSmaiee();
     }//GEN-LAST:event_enviarEscena_jButtonActionPerformed
 
     /**
      * Deletes a scene.
      *
      * @param evt
      */
     private void eliminarEscena_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarEscena_jButtonActionPerformed
         this.sceneCtrl.deleteElement(this);
     }//GEN-LAST:event_eliminarEscena_jButtonActionPerformed
 
     /**
      * Select a scene balast
      *
      * @param evt
      */
     private void selectBalast(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectBalast
         this.sceneCtrl.selectEsceneBalast(this);
     }//GEN-LAST:event_selectBalast
 
     /**
      * Update scene balast level.
      *
      * @param evt
      */
     private void actualizarNivelEscena_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizarNivelEscena_jButtonActionPerformed
         this.sceneCtrl.updateEsceneBalastLevel(this);
     }//GEN-LAST:event_actualizarNivelEscena_jButtonActionPerformed
 
     /**
      * Select element Btns (ins)
      *
      * @param evt
      */
     private void jButton31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton31ActionPerformed
         ViewUtils.addSelected(jList14, jList15);
     }//GEN-LAST:event_jButton31ActionPerformed
 
     /**
      * Remove selected element Btns (ins)
      *
      * @param evt
      */
     private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
         ViewUtils.remSelected(jList14, jList15);
     }//GEN-LAST:event_jButton30ActionPerformed
 
     /**
      * Select all the elemens. Btns (ins)
      *
      * @param evt
      */
     private void jButton32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton32ActionPerformed
         ViewUtils.addAll(jList14, jList15);
     }//GEN-LAST:event_jButton32ActionPerformed
 
     /**
      * Remove all the elemens. Btns (ins)
      *
      * @param evt
      */
     private void jButton33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton33ActionPerformed
         ViewUtils.remAll(jList14, jList15);
     }//GEN-LAST:event_jButton33ActionPerformed
 
     /**
      * Save btns.
      *
      * @param evt
      */
     private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
         this.insCtrl.saveElement(this);
     }//GEN-LAST:event_jButton14ActionPerformed
 
     /**
      * Select an element in the ftcld view.
      *
      * @param evt
      */
     private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
         ViewUtils.addSelected(jList8, jList9);
     }//GEN-LAST:event_jButton19ActionPerformed
 
     /**
      * Remove selected ftcld.
      *
      * @param evt
      */
     private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
         ViewUtils.remSelected(jList8, jList9);
     }//GEN-LAST:event_jButton18ActionPerformed
 
     /**
      * Add all items ftcld.
      *
      * @param evt
      */
     private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
         ViewUtils.addAll(jList8, jList9);
     }//GEN-LAST:event_jButton20ActionPerformed
 
     /**
      * Remove all items.
      *
      * @param evt
      */
     private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
         ViewUtils.remAll(jList8, jList9);
     }//GEN-LAST:event_jButton21ActionPerformed
 
     /**
      * Select balasts items ftcld.
      *
      * @param evt
      */
     private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox10ActionPerformed
         inOutType = ViewUtils.selectCheks(jCheckBox10, jCheckBox11, fotoceldas_entrada_escenas_jCheckBox);
         this.generalCtrl.showAvailableBalasts(jList8, jList9, this);
     }//GEN-LAST:event_jCheckBox10ActionPerformed
 
     /**
      * Select group items ftcld.
      *
      * @param evt
      */
     private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox11ActionPerformed
         inOutType = ViewUtils.selectCheks(jCheckBox10, jCheckBox11, fotoceldas_entrada_escenas_jCheckBox);
         this.generalCtrl.showAvailableGroups(jList8, jList9, this);
     }//GEN-LAST:event_jCheckBox11ActionPerformed
 
     /**
      * Select balasts items Btns
      *
      * @param evt
      */
     private void balastros_jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastros_jCheckBoxActionPerformed
         inOutType = ViewUtils.selectCheks(balastros_jCheckBox, grupos_jCheckBox, jCheckBox18);
         this.generalCtrl.showAvailableBalasts(jList14, jList15, this);
     }//GEN-LAST:event_balastros_jCheckBoxActionPerformed
 
     /**
      * Select group items Btns.
      *
      * @param evt
      */
     private void grupos_jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grupos_jCheckBoxActionPerformed
         inOutType = ViewUtils.selectCheks(balastros_jCheckBox, grupos_jCheckBox, jCheckBox18);
         this.generalCtrl.showAvailableGroups(jList14, jList15, this);
     }//GEN-LAST:event_grupos_jCheckBoxActionPerformed
 
     /**
      * Select scene items Btns.
      *
      * @param evt
      */
     private void jCheckBox18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox18ActionPerformed
         inOutType = ViewUtils.selectCheks(balastros_jCheckBox, grupos_jCheckBox, jCheckBox18);
         this.generalCtrl.showAvailableScenes(jList14, jList15, this);
     }//GEN-LAST:event_jCheckBox18ActionPerformed
 
     /**
      * Select balasts items sensor
      *
      * @param evt
      */
     private void jCheckBox19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox19ActionPerformed
         inOutType = ViewUtils.selectCheksSensor(jCheckBox19, jCheckBox20);
         this.generalCtrl.showAvailableBalasts(jList16, jList17, this);
     }//GEN-LAST:event_jCheckBox19ActionPerformed
 
     /**
      * Select item sensors.
      *
      * @param evt
      */
     private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
         ViewUtils.addSelected(jList16, jList17);
     }//GEN-LAST:event_jButton35ActionPerformed
 
     /**
      * Remove item sensors
      */
     private void jButton34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton34ActionPerformed
         ViewUtils.remSelected(jList16, jList17);
     }//GEN-LAST:event_jButton34ActionPerformed
 
     /**
      * Add all sensors.
      *
      * @param evt
      */
     private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
         ViewUtils.addAll(jList16, jList17);
     }//GEN-LAST:event_jButton36ActionPerformed
 
     /**
      * Remove all sensors.
      *
      * @param evt
      */
     private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
         ViewUtils.remAll(jList16, jList17);
     }//GEN-LAST:event_jButton37ActionPerformed
 
     /**
      * Select group items sensor
      *
      * @param evt
      */
     private void jCheckBox20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox20ActionPerformed
         inOutType = ViewUtils.selectCheksSensor(jCheckBox19, jCheckBox20);
         this.generalCtrl.showAvailableGroups(jList16, jList17, this);
     }//GEN-LAST:event_jCheckBox20ActionPerformed
 
     /**
      * Save sensor.
      *
      * @param evt
      */
     private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
         this.insCtrl.saveElement(this);
     }//GEN-LAST:event_jButton16ActionPerformed
 
     /**
      * Save ftcld
      *
      * @param evt
      */
     private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
         this.insCtrl.saveElement(this);
     }//GEN-LAST:event_jButton15ActionPerformed
 
     /**
      * Delete in: btns
      *
      * @param evt
      */
     private void jButton40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton40ActionPerformed
         this.insCtrl.deleteElement(this);
     }//GEN-LAST:event_jButton40ActionPerformed
 
     /**
      * Delete in: ftcld
      *
      * @param evt
      */
     private void jButton41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton41ActionPerformed
         this.insCtrl.deleteElement(this);
     }//GEN-LAST:event_jButton41ActionPerformed
 
     /**
      * Delete in: sensor
      *
      * @param evt
      */
     private void jButton42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton42ActionPerformed
         this.insCtrl.deleteElement(this);
     }//GEN-LAST:event_jButton42ActionPerformed
 
     /**
      * Add select items. Events
      *
      * @param evt
      */
     private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
         ViewUtils.addSelected(jList12, jList13);
     }//GEN-LAST:event_jButton27ActionPerformed
 
     /**
      * Remove selected items. Events
      *
      * @param evt
      */
     private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
         ViewUtils.remSelected(jList12, jList13);
     }//GEN-LAST:event_jButton26ActionPerformed
 
     /**
      * Add all items. Events.
      *
      * @param evt
      */
     private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
         ViewUtils.addAll(jList12, jList13);
     }//GEN-LAST:event_jButton28ActionPerformed
 
     /**
      * Remove all items. Events.
      *
      * @param evt
      */
     private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
         ViewUtils.remAll(jList12, jList13);
     }//GEN-LAST:event_jButton29ActionPerformed
 
     /**
      * Show available balasts.
      *
      * @param evt
      */
     private void selBalastosEntradas_jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selBalastosEntradas_jCheckBoxActionPerformed
         eventOutType = ViewUtils.selectCheks(selBalastosEntradas_jCheckBox, selGruposEntradas_jCheckBox, selEscenaEntrada_jCheckBox);
         this.generalCtrl.showAvailableBalasts(jList12, jList13, this);
     }//GEN-LAST:event_selBalastosEntradas_jCheckBoxActionPerformed
 
     /**
      * Show available groups.
      *
      * @param evt
      */
     private void selGruposEntradas_jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selGruposEntradas_jCheckBoxActionPerformed
         eventOutType = ViewUtils.selectCheks(selBalastosEntradas_jCheckBox, selGruposEntradas_jCheckBox, selEscenaEntrada_jCheckBox);
         this.generalCtrl.showAvailableGroups(jList12, jList13, this);
     }//GEN-LAST:event_selGruposEntradas_jCheckBoxActionPerformed
 
     /**
      * Show available scenes.
      *
      * @param evt
      */
     private void selEscenaEntrada_jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selEscenaEntrada_jCheckBoxActionPerformed
         eventOutType = ViewUtils.selectCheks(selBalastosEntradas_jCheckBox, selGruposEntradas_jCheckBox, selEscenaEntrada_jCheckBox);
         this.generalCtrl.showAvailableScenes(jList12, jList13, this);
     }//GEN-LAST:event_selEscenaEntrada_jCheckBoxActionPerformed
 
     /**
      * Seleccionar por fecha o por dia.
      *
      * @param evt
      */
     private void porDias_jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_porDias_jCheckBoxActionPerformed
     }//GEN-LAST:event_porDias_jCheckBoxActionPerformed
 
     /**
      * Envio de evento.
      *
      * @param evt
      */
     private void enviarEventos_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarEventos_jButtonActionPerformed
         try {
             this.eventCtrl.saveElement(this);
         } catch (Exception ex) {
             System.out.println("Problema guardando el evento");
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, null, ex);
             System.out.println("Problema guardando el evento: " + ex.toString());
             JOptionPane.showMessageDialog(null, "Error guardando el evento. Revise los datos como la fecha e intente la acción nuevamente.", "Error guardando el evento", JOptionPane.ERROR_MESSAGE);
 
         }
     }//GEN-LAST:event_enviarEventos_jButtonActionPerformed
 
     /**
      * Charge the available balasts.
      *
      * @param evt
      */
     private void selectTab(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectTab
         try {
             //        generalCtrl.continueConfigurationViewData(this);
             this.realCtrl.showAreas(this);
             String panel = getTabbedPane().getSelectedComponent().getName();
             int numeroPanel = getTabbedPane().getSelectedIndex();
             System.out.println("Se cambio a el panel " + panel + " num " + numeroPanel);
 
 
             switch (numeroPanel) {
                case 0:
 
 
                     balastoCtrl.refrescarVista(this);
 
 
                     break;
                 case 1: //caso del panel de configuracion de balastos.
 
//                    OperacionesBalastoConfiguracionDaoJmodbus.setMode(OperacionesBalastoConfiguracionDaoJmodbus.MODE_CONFIG);
 
//                    ColaOperaciones cola = ColaOperaciones.getInstancia();
                     OperacionesDaoHilo hilo = new OperacionesDaoHilo(OperacionesBalastoConfiguracionDaoJmodbus.OPCODE_VERIFICA_RED);
                     hilo.setLabel(getStatusLabel());
                     hilo.getLabel().setText("Cargando elementos de la configuracion de balastos...");
                     hilo.setBar(getBarraProgreso_jProgressBar());
//                    cola.getCola().enqueue(hilo);
//                    cola.iniciarOperaciones();
                     hilo.execute();
                     hilo.get();
 
                     int intentosBalastosRed = 0;
 
                     while (intentosBalastosRed < 5) {
 
                         if (this.getBalastoConfiguracion_jComboBox().getItemAt(0) == null) {
                             OperacionesDaoHilo hilo1 = new OperacionesDaoHilo(OperacionesBalastoConfiguracionDaoJmodbus.OPCODE_VERIFICA_RED);
                             hilo1.setLabel(getStatusLabel());
                             hilo1.getLabel().setText("Cargando los balastos en red. Intento: " + (intentosBalastosRed + 1));
                             hilo1.setBar(getBarraProgreso_jProgressBar());
                             hilo.setDelay(MapaDeMemoria.DELAY_OPERACIONES_LARGO);
 
//                    cola.getCola().enqueue(hilo);
//                    cola.iniciarOperaciones();
                             hilo1.execute();
                             hilo1.get();
                             intentosBalastosRed++;
                             Thread.sleep(MapaDeMemoria.DELAY_OPERACIONES_CORTO);
 
                             if (intentosBalastosRed == MapaDeMemoria.REINTENTOS) {
                                 JOptionPane.showMessageDialog(null, "Al parecer no hay balastos en la red. Verifique que si se encuentran conectados.\nSi estan conectados intente reiniciar el programa.", "", inType);
                             }
 
                         } else {
                             intentosBalastosRed = 5;
                         }
                     }
 
 
                     break;
                case 2:



                     break;
 
             }
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, "Error en la seleccion de los tabbs", ex);
 //        } catch (ExecutionException ex) {
 //            Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
 
 
     }//GEN-LAST:event_selectTab
 
     /**
      * Seleccionar un abalasto.
      *
      * @param evt
      */
     private void selectBalasto(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectBalasto
         this.realCtrl.seleccionarBalasto(this);
     }//GEN-LAST:event_selectBalasto
 
     /**
      * Add a new area
      *
      * @param evt
      */
     private void jButton47ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton47ActionPerformed
         this.realCtrl.newArea(this);
     }//GEN-LAST:event_jButton47ActionPerformed
 
     /**
      * Select elements to set the values.
      *
      * @param evt
      */
     private void selectElements(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectElements
         this.eventCtrl.selectElements(this);
     }//GEN-LAST:event_selectElements
 
     /**
      * Actualizar nivel.
      *
      * @param evt
      */
     private void jButton48ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton48ActionPerformed
         this.eventCtrl.updateLevel(this);
     }//GEN-LAST:event_jButton48ActionPerformed
 
     /**
      * Selecting an area
      */
     private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
         this.realCtrl.selectArea(this);
     }//GEN-LAST:event_jComboBox2ActionPerformed
 
     /**
      * Saves program in flash memory.
      *
      * @param evt
      */
     private void guardarEnFlash(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarEnFlash
         try {
 
 
 
 //            getBarraProgreso_jProgressBar().setIndeterminate(true);
 //            getStatusLabel().setText("Guardando en flash...");
 //            
             OperacionesDaoHilo hilo = new OperacionesDaoHilo(OperacionesBalastoConfiguracionDaoJmodbus.OPCODE_GRABAR_EN_FLASH);
             hilo.setBar(barraProgreso_jProgressBar);
             hilo.setLabel(getStatusLabel());
             hilo.getBar().setIndeterminate(true);
             hilo.getLabel().setText("Guardando en flash...");
             hilo.setPpalView(this.getTabbedPane());
             
             
 //            ConfiguracionDAOJmodbus g = new ConfiguracionDAOJmodbus(dao);
 //            
 //            getBarraProgreso_jProgressBar().setIndeterminate(false);
 //            getStatusLabel().setText("");
 
             hilo.execute();
 
         } catch (Exception e) {
             getBarraProgreso_jProgressBar().setIndeterminate(false);
             getStatusLabel().setText("");
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, "Problemas guardando en flash", e);
             JOptionPane.showMessageDialog(this, "Hubo problemas guardando", "Error guardando en flash", JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_guardarEnFlash
 
     /**
      * Read from flash.
      *
      * @param evt
      */
     private void readFromFlash(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readFromFlash
         limpiarArbol(arbol_jTree);
 
         ConfiguracionDAOJmodbus conf = new ConfiguracionDAOJmodbus(dao);
         conf.readFromFlash();
         this.getGeneralCtrl().menuParents(this);
 
 
         CargaInicial c = new CargaInicial(this);
         c.execute();
 
         //Despertamos el hilo de carga y volvemos a cargar todo el arbol de jerarfquia.
 
     }//GEN-LAST:event_readFromFlash
 
     /**
      * Delete events.
      *
      * @param evt
      */
     private void eliminarEvento_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarEvento_jButtonActionPerformed
         this.eventCtrl.deleteElement(this);
     }//GEN-LAST:event_eliminarEvento_jButtonActionPerformed
 
     /**
      * Saves balast state.
      *
      * @param evt
      */
     private void saveBalastState(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBalastState
         this.realCtrl.monitoreoBalasto(this);
     }//GEN-LAST:event_saveBalastState
 
     /**
      * Deletes the selected area.
      *
      * @param evt
      */
     private void jButton46ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton46ActionPerformed
         this.realCtrl.deleteArea(this);
     }//GEN-LAST:event_jButton46ActionPerformed
 
     /**
      * Edit balasts per area.
      *
      * @param evt
      */
     private void jButton44ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton44ActionPerformed
         this.realCtrl.areaBalasts(this);
     }//GEN-LAST:event_jButton44ActionPerformed
 
     /**
      * Erase the memory
      */
     private void formatearMemoria(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatearMemoria
         int confirm = JOptionPane.showConfirmDialog(null, "Está apunto de borrar la memoria flash. Desea continuar?");
         if (confirm == JOptionPane.OK_OPTION) {
             try {
                 OperacionesDaoHilo hilo = new OperacionesDaoHilo(OperacionesBalastoConfiguracionDaoJmodbus.OPCODE_FORMATEAR_FLASH);
                 hilo.setBar(barraProgreso_jProgressBar);
                 hilo.setLabel(getStatusLabel());
                 hilo.getBar().setIndeterminate(true);
                 hilo.getLabel().setText("Formateando memoria...");
                 hilo.setPpalView(this.getTabbedPane());
                 hilo.execute();
 //                
 //                getBarraProgreso_jProgressBar().setIndeterminate(true);
 //                getStatusLabel().setText("Formateando memoria...");
 //                ConfiguracionDAOJmodbus c = new ConfiguracionDAOJmodbus(dao);
 //                c.eraseMemory();
                 limpiarArbol(getArbol_jTree());
                 establecerModeloArbolDefault(this.getArbol_jTree());
                 
 
 //                hilo.get();
 
 
 //                getStatusLabel().setText("");
 //                getBarraProgreso_jProgressBar().setIndeterminate(false);
 //                JOptionPane.showMessageDialog(null, "Memoria flash formateandose. Cuando el sistema termine se mostra");
                 CargaInicial ci=new CargaInicial(this);
                 ci.execute();
 //                this.getGeneralCtrl().habilitarTodo(this.getTabbedPane(), true);
                 
 //            } catch (InterruptedException ex) {
 //                Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, null, ex);
 //            } catch (ExecutionException ex) {
 //                Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, null, ex);
             } catch (HeadlessException ex) {
                 ex.printStackTrace();
                 getStatusLabel().setText("");
                 getBarraProgreso_jProgressBar().setIndeterminate(false);
                 JOptionPane.showMessageDialog(null, "Error borrando la memoria flash: " + ex.getMessage());
             }
         }
     }//GEN-LAST:event_formatearMemoria
 
     /**
      * Configure date and time.
      *
      * @param evt
      */
     private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
         if (!jDateChooser1.isEnabled() && !jFormattedTextField1.isEnabled()) {
             jDateChooser1.setEnabled(true);
             jFormattedTextField1.setEnabled(true);
             threadManager.stopAllCurrentThreads();
         } else {
             jDateChooser1.setEnabled(false);
             jFormattedTextField1.setEnabled(false);
             threadManager.startThreadIfTerminated(ThreadManager.RTC_REFRESHING);
         }
     }//GEN-LAST:event_jButton24ActionPerformed
 
     private void entradaNumero_jComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entradaNumero_jComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_entradaNumero_jComboBoxActionPerformed
 
     private void arbol_jTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_arbol_jTreeMouseClicked
     }//GEN-LAST:event_arbol_jTreeMouseClicked
 
     private void arbol_jTreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_arbol_jTreeMousePressed
         this.generalCtrl.treeSelection(this, realCtrl);
     }//GEN-LAST:event_arbol_jTreeMousePressed
 
     /**
      * Select scenes items ftcld.
      *
      * @param evt
      */
     private void fotoceldas_entrada_escenas_jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fotoceldas_entrada_escenas_jCheckBoxActionPerformed
         inOutType = ViewUtils.selectCheks(jCheckBox19, jCheckBox20, fotoceldas_entrada_escenas_jCheckBox);
         this.generalCtrl.showAvailableScenes(jList16, jList17, this);
     }//GEN-LAST:event_fotoceldas_entrada_escenas_jCheckBoxActionPerformed
 
     private void porDias_jCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_porDias_jCheckBoxItemStateChanged
         this.eventCtrl.selectByDays(this);
     }//GEN-LAST:event_porDias_jCheckBoxItemStateChanged
 
     private void balastoNum_jComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoNum_jComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_balastoNum_jComboBoxActionPerformed
 
     private void puerto_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_puerto_jTextFieldActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_puerto_jTextFieldActionPerformed
 
     private void configuracionBalastos_jPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_configuracionBalastos_jPanelFocusGained
         System.out.println("Se gano el foco");
     }//GEN-LAST:event_configuracionBalastos_jPanelFocusGained
 
     private void balastoEscribirConfig_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoEscribirConfig_jButtonActionPerformed
         balastoConfigCtrl.saveElement(this);
     }//GEN-LAST:event_balastoEscribirConfig_jButtonActionPerformed
 
     private void balastoResetConfig_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoResetConfig_jButtonActionPerformed
 
         balastoConfigCtrl.resetElement(this);
 
     }//GEN-LAST:event_balastoResetConfig_jButtonActionPerformed
 
     private void balastoConfiguracion_jComboBoxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_balastoConfiguracion_jComboBoxPropertyChange
     }//GEN-LAST:event_balastoConfiguracion_jComboBoxPropertyChange
 
     private void balastoConfiguracion_jComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoConfiguracion_jComboBoxActionPerformed
 
         balastoConfigCtrl.showSelectedElement(balastoConfiguracion_jComboBox.getSelectedItem().toString(), this);
 
     }//GEN-LAST:event_balastoConfiguracion_jComboBoxActionPerformed
 
     private void cambiarDir_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cambiarDir_jButtonActionPerformed
         try {
             // TODO add your handling code here:
             balastoConfigCtrl.cambiarDireccion(this);
 //            cambiarDir_jButton.setEnabled(false);
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, "Error cambiando la direccion del balasto", ex);
         }
     }//GEN-LAST:event_cambiarDir_jButtonActionPerformed
 
     private void balastoDir_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoDir_jTextFieldActionPerformed
         try {
             balastoConfigCtrl.cambiarDireccion(this);
             cambiarDir_jButton.setEnabled(false);
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, "Error cambiando la dirección del balasto", ex);
         }
     }//GEN-LAST:event_balastoDir_jTextFieldActionPerformed
 
     private void balastoDir_jTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_balastoDir_jTextFieldFocusGained
         cambiarDir_jButton.setEnabled(true);
     }//GEN-LAST:event_balastoDir_jTextFieldFocusGained
 
     private void balastoDir_jTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_balastoDir_jTextFieldFocusLost
 //        try {
 //            if (getFocusOwner().getName() != null) {
 //                if ("cambiarDir_jButton".equals(getFocusOwner().getName())) {
 //                    cambiarDir_jButton.setEnabled(true);
 //                } else {
 //                    cambiarDir_jButton.setEnabled(false);
 //                }
 //            }
 //        } catch (Exception e) {
 //            cambiarDir_jButton.setEnabled(false);
 //        }
     }//GEN-LAST:event_balastoDir_jTextFieldFocusLost
 
     private void entradaFotoceldaNivelDeseado_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entradaFotoceldaNivelDeseado_jTextFieldActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_entradaFotoceldaNivelDeseado_jTextFieldActionPerformed
 
     private void balastoNombreSmaiee_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_balastoNombreSmaiee_jTextFieldActionPerformed
         try {
             enviarBalastoSmaiee();
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_balastoNombreSmaiee_jTextFieldActionPerformed
 
     private void gruposNombreSmaiee_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gruposNombreSmaiee_jTextFieldActionPerformed
         try {
             enviarGrupoSmaiee();
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, "Error enviando el grupo.", ex);
         }
     }//GEN-LAST:event_gruposNombreSmaiee_jTextFieldActionPerformed
 
     private void nombreEscena_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nombreEscena_jTextFieldActionPerformed
         enviarEscenaSmaiee();
     }//GEN-LAST:event_nombreEscena_jTextFieldActionPerformed
 
     //</editor-fold>
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 index = new PpalView();
                 index.setLocationRelativeTo(null);
                 index.setVisible(true);
             }
         });
     }
     /*
      * Variable declarations
      */
     // <editor-fold defaultstate="collapsed" desc="Variable declarations">
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton actualizarNivelEscena_jButton;
     private javax.swing.JButton agregarBalastoEscena_jButton;
     private javax.swing.JButton agregarTodosBalastosEscena_jButton;
     private javax.swing.JScrollPane arbolJerarquia_jScrollPane;
     private javax.swing.JTree arbol_jTree;
     private javax.swing.JMenu archivo_jMenu;
     private javax.swing.JPanel balastoConfigBasico_jPanel;
     private javax.swing.JComboBox balastoConfiguracion_jComboBox;
     private javax.swing.JPanel balastoDali_jPanel;
     protected javax.swing.JTextField balastoDir_jTextField;
     private javax.swing.JButton balastoEliminar_jButton;
     private javax.swing.JButton balastoEnviar_jButton;
     private javax.swing.JButton balastoEscribirConfig_jButton;
     protected javax.swing.JTextField balastoFR_jTextField;
     protected javax.swing.JTextField balastoFT_jTextField;
     protected javax.swing.JTextField balastoLF_jTextField;
     protected javax.swing.JTextField balastoLX_jTextField;
     protected javax.swing.JTextField balastoMax_jTextField;
     protected javax.swing.JTextField balastoMin_jTextField;
     protected javax.swing.JTextField balastoNombreSmaiee_jTextField;
     protected javax.swing.JComboBox balastoNum_jComboBox;
     protected javax.swing.JTextField balastoPot_jTextField;
     private javax.swing.JButton balastoResetConfig_jButton;
     private javax.swing.JList balastosAfectados_jList;
     private javax.swing.JList balastrosDisponibles_jList;
     private javax.swing.JCheckBox balastros_jCheckBox;
     private javax.swing.JProgressBar barraProgreso_jProgressBar;
     private javax.swing.ButtonGroup botoneraComportamiento;
     private javax.swing.ButtonGroup botoneraTipoContacto;
     private javax.swing.JButton cambiarDir_jButton;
     private javax.swing.JCheckBox cbIsStaticConfiguration;
     private javax.swing.ButtonGroup confRol;
     private javax.swing.JMenuItem config_jMenuItem;
     private javax.swing.JPanel configuracionBalastos_jPanel;
     private javax.swing.JPanel configuracionRed_jPanel;
     private javax.swing.JPanel configuracionSmaiee_jPanel;
     private javax.swing.ButtonGroup dias_buttonGroup;
     private javax.swing.JCheckBox domingo_jCheckBox;
     private javax.swing.JButton eliminarEscena_jButton;
     private javax.swing.JButton eliminarEvento_jButton;
     private javax.swing.JButton eliminarGrupo_jButton;
     private javax.swing.JTextField entradaFotoceldaNivelDeseado_jTextField;
     private javax.swing.JTextField entradaFotoceldaNivelIlum_jTextField;
     private javax.swing.JComboBox entradaNumero_jComboBox;
     private javax.swing.JButton enviarConfiguracion_jButton2;
     private javax.swing.JButton enviarEscena_jButton;
     private javax.swing.JButton enviarEventos_jButton;
     private javax.swing.JButton enviarGrupo_jButton;
     private javax.swing.JComboBox escenaNumero_jComboBox;
     private javax.swing.JPanel escenasBalastos_jPanel;
     private javax.swing.JPanel escenasDeBalasto_jPanel;
     private javax.swing.JComboBox eventoNum_jComboBox;
     private javax.swing.JFormattedTextField fieldPort;
     private javax.swing.JMenuItem formatear_jMenuItem;
     private javax.swing.JCheckBox fotoceldas_entrada_escenas_jCheckBox;
     private javax.swing.JFormattedTextField gateway_jTextField;
     private javax.swing.JButton grabarIp_jButton;
     private javax.swing.JCheckBox grupo_jCheckBox1;
     private javax.swing.JCheckBox grupo_jCheckBox10;
     private javax.swing.JCheckBox grupo_jCheckBox11;
     private javax.swing.JCheckBox grupo_jCheckBox12;
     private javax.swing.JCheckBox grupo_jCheckBox13;
     private javax.swing.JCheckBox grupo_jCheckBox14;
     private javax.swing.JCheckBox grupo_jCheckBox15;
     private javax.swing.JCheckBox grupo_jCheckBox16;
     private javax.swing.JCheckBox grupo_jCheckBox2;
     private javax.swing.JCheckBox grupo_jCheckBox3;
     private javax.swing.JCheckBox grupo_jCheckBox4;
     private javax.swing.JCheckBox grupo_jCheckBox5;
     private javax.swing.JCheckBox grupo_jCheckBox6;
     private javax.swing.JCheckBox grupo_jCheckBox7;
     private javax.swing.JCheckBox grupo_jCheckBox8;
     private javax.swing.JCheckBox grupo_jCheckBox9;
     private javax.swing.JPanel gruposDeBalasto_jPanel;
     private javax.swing.JTextField gruposNombreSmaiee_jTextField;
     private javax.swing.JComboBox gruposNum_jComboBox;
     private javax.swing.JCheckBox grupos_jCheckBox;
     private javax.swing.JMenuItem guardarEnFlash_jMenuItem;
     private javax.swing.JPanel header;
     private javax.swing.JLabel headerImage;
     private javax.swing.JFormattedTextField horaDiasEvento_jFormattedTextField;
     private javax.swing.JTextField ip_jTextField;
     private javax.swing.JButton jButton14;
     private javax.swing.JButton jButton15;
     private javax.swing.JButton jButton16;
     private javax.swing.JButton jButton18;
     private javax.swing.JButton jButton19;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton20;
     private javax.swing.JButton jButton21;
     private javax.swing.JButton jButton24;
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
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton40;
     private javax.swing.JButton jButton41;
     private javax.swing.JButton jButton42;
     private javax.swing.JButton jButton44;
     private javax.swing.JButton jButton46;
     private javax.swing.JButton jButton47;
     private javax.swing.JButton jButton48;
     private javax.swing.JButton jButton5;
     private javax.swing.JCheckBox jCheckBox10;
     private javax.swing.JCheckBox jCheckBox11;
     private javax.swing.JCheckBox jCheckBox18;
     private javax.swing.JCheckBox jCheckBox19;
     private javax.swing.JCheckBox jCheckBox20;
     private javax.swing.JComboBox jComboBox2;
     protected com.toedter.calendar.JDateChooser jDateChooser1;
     private com.toedter.calendar.JDateChooser jDateChooser2;
     private javax.swing.JFormattedTextField jFormattedTextField1;
     private javax.swing.JFormattedTextField jFormattedTextField2;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
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
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel31;
     private javax.swing.JLabel jLabel32;
     private javax.swing.JLabel jLabel34;
     private javax.swing.JLabel jLabel35;
     private javax.swing.JLabel jLabel36;
     private javax.swing.JLabel jLabel37;
     private javax.swing.JLabel jLabel39;
     private javax.swing.JLabel jLabel4;
     protected javax.swing.JLabel jLabel41;
     private javax.swing.JLabel jLabel44;
     private javax.swing.JLabel jLabel45;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel53;
     private javax.swing.JLabel jLabel54;
     private javax.swing.JLabel jLabel55;
     private javax.swing.JLabel jLabel56;
     private javax.swing.JLabel jLabel57;
     private javax.swing.JLabel jLabel58;
     private javax.swing.JLabel jLabel61;
     private javax.swing.JLabel jLabel62;
     private javax.swing.JLabel jLabel63;
     private javax.swing.JLabel jLabel64;
     private javax.swing.JLabel jLabel65;
     private javax.swing.JLabel jLabel66;
     private javax.swing.JLabel jLabel67;
     private javax.swing.JLabel jLabel68;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JList jList12;
     private javax.swing.JList jList13;
     private javax.swing.JList jList14;
     private javax.swing.JList jList15;
     private javax.swing.JList jList16;
     private javax.swing.JList jList17;
     protected javax.swing.JList jList2;
     private javax.swing.JList jList3;
     private javax.swing.JList jList8;
     private javax.swing.JList jList9;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JRadioButton jRadioButton1;
     private javax.swing.JRadioButton jRadioButton2;
     private javax.swing.JRadioButton jRadioButton3;
     private javax.swing.JRadioButton jRadioButton4;
     private javax.swing.JScrollPane jScrollPane10;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane14;
     private javax.swing.JScrollPane jScrollPane15;
     private javax.swing.JScrollPane jScrollPane16;
     private javax.swing.JScrollPane jScrollPane17;
     private javax.swing.JScrollPane jScrollPane18;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JPopupMenu.Separator jSeparator3;
     private javax.swing.JPopupMenu.Separator jSeparator4;
     private javax.swing.JSlider jSlider1;
     private javax.swing.JSpinner jSpinner3;
     private javax.swing.JTable jTable1;
     private javax.swing.JTextField jTextField10;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField25;
     private javax.swing.JTextField jTextField34;
     private javax.swing.JTextField jTextField35;
     private javax.swing.JTextField jTextField36;
     private javax.swing.JTextField jTextField5;
     private javax.swing.JTextField jTextField6;
     private javax.swing.JTextField jTextField7;
     private javax.swing.JCheckBox jueves_jCheckBox;
     private javax.swing.JLabel labelDns;
     private javax.swing.JLabel labelGateway;
     private javax.swing.JLabel labelIp;
     private javax.swing.JLabel labelMascara;
     private javax.swing.JLabel labelPuerto;
     private javax.swing.JMenuItem leerFlash_jMenuItem;
     private javax.swing.JCheckBox lunes_jCheckBox;
     private javax.swing.JMenuBar mainMenuBar;
     private javax.swing.JCheckBox martes_jCheckBox;
     private javax.swing.JFormattedTextField mask_jTextField;
     private javax.swing.JCheckBox miercoles_jCheckBox;
     private javax.swing.JPanel monitoreoTiempoReal_jPanel;
     private javax.swing.JLabel nivelEscena_jLabel;
     private javax.swing.JTextField nivelEscena_jTextField;
     private javax.swing.JLabel nivel_jLabel;
     protected javax.swing.JTextField nivel_jTextField;
     private javax.swing.JLabel nombreEscena_jLabel;
     private javax.swing.JTextField nombreEscena_jTextField;
     private javax.swing.JTextField nombreEvento_jTextField;
     private javax.swing.JPanel panelBalastos;
     private javax.swing.JPanel panelBienvenida;
     private javax.swing.JPanel panelBotonera;
     private javax.swing.JPanel panelConfEntradas;
     private javax.swing.JPanel panelConfiguracion;
     private javax.swing.JPanel panelEntradaInit;
     private javax.swing.JPanel panelEntradas;
     private javax.swing.JPanel panelEscenas;
     private javax.swing.JPanel panelEventos;
     private javax.swing.JPanel panelFotocelda;
     private javax.swing.JPanel panelGrupos;
     private javax.swing.JPanel panelPpal;
     private javax.swing.JPanel panelPrincipal_jPanel;
     private javax.swing.JPanel panelSensor;
     private javax.swing.JPanel panelTiempoReal;
     private javax.swing.JPanel porDiasEvento_jPanel;
     private javax.swing.JCheckBox porDias_jCheckBox;
     private javax.swing.JPanel porFechaYHora_jPanel;
     private javax.swing.JLabel pot_jLabel;
     private javax.swing.JScrollPane principal_jScrollPane;
     private javax.swing.JTextField puerto_jTextField;
     private javax.swing.JRadioButton rbIsMaster;
     private javax.swing.JRadioButton rbIsSlave;
     private javax.swing.JButton removerBalastoEscena_jButton;
     private javax.swing.JButton removerTodosBalastosEscenas_jButton;
     private javax.swing.JCheckBox repetirCiclo_jCheckBox;
     private javax.swing.JCheckBox sabado_jCheckBox;
     private javax.swing.JCheckBox selBalastosEntradas_jCheckBox;
     private javax.swing.JCheckBox selEscenaEntrada_jCheckBox;
     private javax.swing.JCheckBox selGruposEntradas_jCheckBox;
     private com.isolux.view.componentes.SliderConValor sliderConValor1;
     private com.isolux.view.componentes.SliderConValor sliderConValor10;
     private com.isolux.view.componentes.SliderConValor sliderConValor11;
     private com.isolux.view.componentes.SliderConValor sliderConValor12;
     private com.isolux.view.componentes.SliderConValor sliderConValor13;
     private com.isolux.view.componentes.SliderConValor sliderConValor14;
     private com.isolux.view.componentes.SliderConValor sliderConValor15;
     private com.isolux.view.componentes.SliderConValor sliderConValor16;
     private com.isolux.view.componentes.SliderConValor sliderConValor2;
     private com.isolux.view.componentes.SliderConValor sliderConValor3;
     private com.isolux.view.componentes.SliderConValor sliderConValor4;
     private com.isolux.view.componentes.SliderConValor sliderConValor5;
     private com.isolux.view.componentes.SliderConValor sliderConValor6;
     private com.isolux.view.componentes.SliderConValor sliderConValor7;
     private com.isolux.view.componentes.SliderConValor sliderConValor8;
     private com.isolux.view.componentes.SliderConValor sliderConValor9;
     private javax.swing.JPanel statusBar;
     private javax.swing.JLabel statusLabel;
     private javax.swing.JTabbedPane tabbedPane;
     private javax.swing.JCheckBox viernes_jCheckBox;
     // End of variables declaration//GEN-END:variables
     //</editor-fold>
 
     //VIEW
     /**
      * Sets the application theme
      */
     public void applicationTheme() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception ex) {
             Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Encapsulates the select area event.
      *
      * @param evt
      */
     public void selectingAreaEncapsulating(java.awt.event.ActionEvent evt) {
         jComboBox2ActionPerformed(evt);
     }
 
     /**
      * Sets the icon image of the application.
      *
      * @return
      */
     @Override
     public Image getIconImage() {
         Image retValue = Toolkit.getDefaultToolkit().
                 getImage(ClassLoader.getSystemResource("images/icono.png"));
         return retValue;
     }
 
     /**
      * GET SETS
      */
     // <editor-fold defaultstate="collapsed" desc="GET / SETS">
     // Variables declaration - do not modify     
     public HashMap<String, Balasto> getBalasts() {
         return balasts;
     }
 
     public void setBalasts(HashMap<String, Balasto> balasts) {
         this.balasts = balasts;
     }
 
     public boolean getBalastsStauts() {
         return balastsStauts;
     }
 
     public void setBalastsStauts(boolean balastsStauts) {
         this.balastsStauts = balastsStauts;
     }
 
     public HashMap<String, Balasto> getEventBalasts() {
         return eventBalasts;
     }
 
     public void setEventBalasts(HashMap<String, Balasto> eventBalasts) {
         this.eventBalasts = eventBalasts;
     }
 
     public int[] getEventBalastsValues() {
         return eventBalastsValues;
     }
 
     public void setEventBalastsValues(int[] eventBalastsValues) {
         this.eventBalastsValues = eventBalastsValues;
     }
 
     public HashMap<String, Grupo> getEventGroups() {
         return eventGroups;
     }
 
     public void setEventGroups(HashMap<String, Grupo> eventGroups) {
         this.eventGroups = eventGroups;
     }
 
     public HashMap<String, Escena> getEventScene() {
         return eventScene;
     }
 
     public void setEventScene(HashMap<String, Escena> eventScene) {
         this.eventScene = eventScene;
     }
 
     public boolean getEventStauts() {
         return eventStauts;
     }
 
     public void setEventStauts(boolean eventStauts) {
         this.eventStauts = eventStauts;
     }
 
     public HashMap<String, Evento> getEvents() {
         return events;
     }
 
     public void setEvents(HashMap<String, Evento> events) {
         this.events = events;
     }
 
     public HashMap<String, Balasto> getGroupBalasts() {
         return groupBalasts;
     }
 
     public void setGroupBalasts(HashMap<String, Balasto> groupBalasts) {
         this.groupBalasts = groupBalasts;
     }
 
     public HashMap<String, Grupo> getGroups() {
         return groups;
     }
 
     public void setGroups(HashMap<String, Grupo> groups) {
         this.groups = groups;
     }
 
     public boolean getGroupsStauts() {
         return groupsStauts;
     }
 
     public void setGroupsStauts(boolean groupsStauts) {
         this.groupsStauts = groupsStauts;
     }
 
     public HashMap<String, Balasto> getInBalasts() {
         return inBalasts;
     }
 
     public void setInBalasts(HashMap<String, Balasto> inBalasts) {
         this.inBalasts = inBalasts;
     }
 
     public HashMap<String, Grupo> getInGroups() {
         return inGroups;
     }
 
     public void setInGroups(HashMap<String, Grupo> inGroups) {
         this.inGroups = inGroups;
     }
 
     public int getInOutType() {
         return inOutType;
     }
 
     public void setInOutType(int inOutType) {
         this.inOutType = inOutType;
     }
 
     public HashMap<String, Escena> getInScene() {
         return inScene;
     }
 
     public void setInScene(HashMap<String, Escena> inScene) {
         this.inScene = inScene;
     }
 
     public boolean getInStauts() {
         return inStauts;
     }
 
     public void setInStauts(boolean inStauts) {
         this.inStauts = inStauts;
     }
 
     public int getInType() {
         return inType;
     }
 
     public void setInType(int inType) {
         this.inType = inType;
     }
 
     public HashMap<String, Entrada> getIns() {
         return ins;
     }
 
     public void setIns(HashMap<String, Entrada> ins) {
         this.ins = ins;
     }
 
     public String getIp() {
         return ip;
     }
 
     public void setIp(String ip) {
         this.ip = ip;
     }
 
     public Map<String, Character> getMenuParents() {
         return menuParents;
     }
 
     public void setMenuParents(Map<String, Character> menuParents) {
         this.menuParents = menuParents;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public HashMap<String, Balasto> getSceneBalasts() {
         return sceneBalasts;
     }
 
     public void setSceneBalasts(HashMap<String, Balasto> sceneBalasts) {
         this.sceneBalasts = sceneBalasts;
     }
 
     public int[] getSceneBalastsValues() {
         return sceneBalastsValues;
     }
 
     public void setSceneBalastsValues(int[] sceneBalastsValues) {
         this.sceneBalastsValues = sceneBalastsValues;
     }
 
     public boolean getSceneStauts() {
         return sceneStauts;
     }
 
     public void setSceneStauts(boolean sceneStauts) {
         this.sceneStauts = sceneStauts;
     }
 
     public HashMap<String, Escena> getScenes() {
         return scenes;
     }
 
     public void setScenes(HashMap<String, Escena> scenes) {
         this.scenes = scenes;
     }
 
     public String getSelectedBalastNumber() {
         return selectedBalastNumber;
     }
 
     public void setSelectedBalastNumber(String selectedBalastNumber) {
         this.selectedBalastNumber = selectedBalastNumber;
     }
 
     public String getSelectedEventNumber() {
         return selectedEventNumber;
     }
 
     public void setSelectedEventNumber(String selectedEventNumber) {
         this.selectedEventNumber = selectedEventNumber;
     }
 
     public String getSelectedGroupNumber() {
         return selectedGroupNumber;
     }
 
     public void setSelectedGroupNumber(String selectedGroupNumber) {
         this.selectedGroupNumber = selectedGroupNumber;
     }
 
     public String getSelectedInNumber() {
         return selectedInNumber;
     }
 
     public void setSelectedInNumber(String selectedInNumber) {
         this.selectedInNumber = selectedInNumber;
     }
 
     public String getSelectedSceneNumber() {
         return selectedSceneNumber;
     }
 
     public void setSelectedSceneNumber(String selectedSceneNumber) {
         this.selectedSceneNumber = selectedSceneNumber;
     }
 
     public boolean getShowAreas() {
         return showAreas;
     }
 
     public void setShowAreas(boolean showAreas) {
         this.showAreas = showAreas;
     }
 
     public ButtonGroup getBotoneraComportamiento() {
         return botoneraComportamiento;
     }
 
     public void setBotoneraComportamiento(ButtonGroup botoneraComportamiento) {
         this.botoneraComportamiento = botoneraComportamiento;
     }
 
     public ButtonGroup getBotoneraTipoContacto() {
         return botoneraTipoContacto;
     }
 
     public void setBotoneraTipoContacto(ButtonGroup botoneraTipoContacto) {
         this.botoneraTipoContacto = botoneraTipoContacto;
     }
 
     public JCheckBox getCbIsStaticConfiguration() {
         return cbIsStaticConfiguration;
     }
 
     public void setCbIsStaticConfiguration(JCheckBox cbIsStaticConfiguration) {
         this.cbIsStaticConfiguration = cbIsStaticConfiguration;
     }
 
     public ButtonGroup getConfRol() {
         return confRol;
     }
 
     public void setConfRol(ButtonGroup confRol) {
         this.confRol = confRol;
     }
 
     public int getEventOutType() {
         return eventOutType;
     }
 
     public void setEventOutType(int eventOutType) {
         this.eventOutType = eventOutType;
     }
 
     public JFormattedTextField getFieldGateway() {
         return gateway_jTextField;
     }
 
     public void setFieldGateway(JFormattedTextField fieldGateway) {
         this.gateway_jTextField = fieldGateway;
     }
 
     public JTextField getFieldIp() {
         return ip_jTextField;
     }
 
     public void setFieldIp(JTextField fieldIp) {
         this.ip_jTextField = fieldIp;
     }
 
     public JFormattedTextField getFieldMask() {
         return mask_jTextField;
     }
 
     public void setFieldMask(JFormattedTextField fieldMask) {
         this.mask_jTextField = fieldMask;
     }
 
     public JFormattedTextField getFieldPort() {
         return fieldPort;
     }
 
     public void setFieldPort(JFormattedTextField fieldPort) {
         this.fieldPort = fieldPort;
     }
 
     public JTextField getFieldPuerto() {
         return puerto_jTextField;
     }
 
     public void setFieldPuerto(JTextField fieldPuerto) {
         this.puerto_jTextField = fieldPuerto;
     }
 
     public JPanel getHeader() {
         return header;
     }
 
     public void setHeader(JPanel header) {
         this.header = header;
     }
 
     public JLabel getHeaderImage() {
         return headerImage;
     }
 
     public void setHeaderImage(JLabel headerImage) {
         this.headerImage = headerImage;
     }
 
     public static PpalView getIndex() {
         return index;
     }
 
     public static void setIndex(PpalView index) {
         PpalView.index = index;
     }
 
     public JButton getjButton1() {
         return balastoEnviar_jButton;
     }
 
     public void setjButton1(JButton jButton1) {
         this.balastoEnviar_jButton = jButton1;
     }
 
     public JButton getjButton10() {
         return removerBalastoEscena_jButton;
     }
 
     public void setjButton10(JButton jButton10) {
         this.removerBalastoEscena_jButton = jButton10;
     }
 
     public JButton getjButton11() {
         return agregarBalastoEscena_jButton;
     }
 
     public void setjButton11(JButton jButton11) {
         this.agregarBalastoEscena_jButton = jButton11;
     }
 
     public JButton getjButton12() {
         return agregarTodosBalastosEscena_jButton;
     }
 
     public void setjButton12(JButton jButton12) {
         this.agregarTodosBalastosEscena_jButton = jButton12;
     }
 
     public JButton getjButton13() {
         return removerTodosBalastosEscenas_jButton;
     }
 
     public void setjButton13(JButton jButton13) {
         this.removerTodosBalastosEscenas_jButton = jButton13;
     }
 
     public JButton getjButton14() {
         return jButton14;
     }
 
     public void setjButton14(JButton jButton14) {
         this.jButton14 = jButton14;
     }
 
     public JButton getjButton15() {
         return jButton15;
     }
 
     public void setjButton15(JButton jButton15) {
         this.jButton15 = jButton15;
     }
 
     public JButton getjButton16() {
         return jButton16;
     }
 
     public void setjButton16(JButton jButton16) {
         this.jButton16 = jButton16;
     }
 
     public JButton getjButton17() {
         return enviarEventos_jButton;
     }
 
     public void setjButton17(JButton jButton17) {
         this.enviarEventos_jButton = jButton17;
     }
 
     public JButton getjButton18() {
         return jButton18;
     }
 
     public void setjButton18(JButton jButton18) {
         this.jButton18 = jButton18;
     }
 
     public JButton getjButton19() {
         return jButton19;
     }
 
     public void setjButton19(JButton jButton19) {
         this.jButton19 = jButton19;
     }
 
     public JButton getjButton2() {
         return jButton2;
     }
 
     public void setjButton2(JButton jButton2) {
         this.jButton2 = jButton2;
     }
 
     public JButton getjButton20() {
         return jButton20;
     }
 
     public void setjButton20(JButton jButton20) {
         this.jButton20 = jButton20;
     }
 
     public JButton getjButton21() {
         return jButton21;
     }
 
     public void setjButton21(JButton jButton21) {
         this.jButton21 = jButton21;
     }
 
     public JButton getjButton22() {
         return enviarConfiguracion_jButton2;
     }
 
     public void setjButton22(JButton jButton22) {
         this.enviarConfiguracion_jButton2 = jButton22;
     }
 
     public JButton getjButton23() {
         return balastoEliminar_jButton;
     }
 
     public void setjButton23(JButton jButton23) {
         this.balastoEliminar_jButton = jButton23;
     }
 
     public JButton getjButton26() {
         return jButton26;
     }
 
     public void setjButton26(JButton jButton26) {
         this.jButton26 = jButton26;
     }
 
     public JButton getjButton27() {
         return jButton27;
     }
 
     public void setjButton27(JButton jButton27) {
         this.jButton27 = jButton27;
     }
 
     public JButton getjButton28() {
         return jButton28;
     }
 
     public void setjButton28(JButton jButton28) {
         this.jButton28 = jButton28;
     }
 
     public JButton getjButton29() {
         return jButton29;
     }
 
     public void setjButton29(JButton jButton29) {
         this.jButton29 = jButton29;
     }
 
     public JButton getjButton3() {
         return jButton3;
     }
 
     public void setjButton3(JButton jButton3) {
         this.jButton3 = jButton3;
     }
 
     public JButton getjButton30() {
         return jButton30;
     }
 
     public void setjButton30(JButton jButton30) {
         this.jButton30 = jButton30;
     }
 
     public JButton getjButton31() {
         return jButton31;
     }
 
     public void setjButton31(JButton jButton31) {
         this.jButton31 = jButton31;
     }
 
     public JButton getjButton32() {
         return jButton32;
     }
 
     public void setjButton32(JButton jButton32) {
         this.jButton32 = jButton32;
     }
 
     public JButton getjButton33() {
         return jButton33;
     }
 
     public void setjButton33(JButton jButton33) {
         this.jButton33 = jButton33;
     }
 
     public JButton getjButton34() {
         return jButton34;
     }
 
     public void setjButton34(JButton jButton34) {
         this.jButton34 = jButton34;
     }
 
     public JButton getjButton35() {
         return jButton35;
     }
 
     public void setjButton35(JButton jButton35) {
         this.jButton35 = jButton35;
     }
 
     public JButton getjButton36() {
         return jButton36;
     }
 
     public void setjButton36(JButton jButton36) {
         this.jButton36 = jButton36;
     }
 
     public JButton getjButton37() {
         return jButton37;
     }
 
     public void setjButton37(JButton jButton37) {
         this.jButton37 = jButton37;
     }
 
     public JButton getjButton38() {
         return jButton38;
     }
 
     public void setjButton38(JButton jButton38) {
         this.jButton38 = jButton38;
     }
 
     public JButton getjButton39() {
         return actualizarNivelEscena_jButton;
     }
 
     public void setjButton39(JButton jButton39) {
         this.actualizarNivelEscena_jButton = jButton39;
     }
 
     public JButton getjButton4() {
         return jButton4;
     }
 
     public void setjButton4(JButton jButton4) {
         this.jButton4 = jButton4;
     }
 
     public JButton getjButton40() {
         return jButton40;
     }
 
     public void setjButton40(JButton jButton40) {
         this.jButton40 = jButton40;
     }
 
     public JButton getjButton41() {
         return jButton41;
     }
 
     public void setjButton41(JButton jButton41) {
         this.jButton41 = jButton41;
     }
 
     public JButton getjButton42() {
         return jButton42;
     }
 
     public void setjButton42(JButton jButton42) {
         this.jButton42 = jButton42;
     }
 
     public JButton getjButton43() {
         return eliminarEvento_jButton;
     }
 
     public void setjButton43(JButton jButton43) {
         this.eliminarEvento_jButton = jButton43;
     }
 
     public JButton getjButton44() {
         return jButton44;
     }
 
     public void setjButton44(JButton jButton44) {
         this.jButton44 = jButton44;
     }
 
     public JButton getjButton46() {
         return jButton46;
     }
 
     public void setjButton46(JButton jButton46) {
         this.jButton46 = jButton46;
     }
 
     public JButton getjButton47() {
         return jButton47;
     }
 
     public void setjButton47(JButton jButton47) {
         this.jButton47 = jButton47;
     }
 
     public JButton getjButton48() {
         return jButton48;
     }
 
     public void setjButton48(JButton jButton48) {
         this.jButton48 = jButton48;
     }
 
     public JButton getjButton5() {
         return jButton5;
     }
 
     public void setjButton5(JButton jButton5) {
         this.jButton5 = jButton5;
     }
 
     public JButton getjButton6() {
         return enviarGrupo_jButton;
     }
 
     public void setjButton6(JButton jButton6) {
         this.enviarGrupo_jButton = jButton6;
     }
 
     public JButton getjButton7() {
         return eliminarGrupo_jButton;
     }
 
     public void setjButton7(JButton jButton7) {
         this.eliminarGrupo_jButton = jButton7;
     }
 
     public JButton getjButton8() {
         return enviarEscena_jButton;
     }
 
     public void setjButton8(JButton jButton8) {
         this.enviarEscena_jButton = jButton8;
     }
 
     public JButton getjButton9() {
         return eliminarEscena_jButton;
     }
 
     public void setjButton9(JButton jButton9) {
         this.eliminarEscena_jButton = jButton9;
     }
 
     public JCheckBox getPorDiasEvento_jCheckBox() {
         return porDias_jCheckBox;
     }
 
     public void setPorDiasEvento_jCheckBox(JCheckBox jCheckBox1) {
         this.porDias_jCheckBox = jCheckBox1;
     }
 
     public JCheckBox getjCheckBox10() {
         return jCheckBox10;
     }
 
     public void setjCheckBox10(JCheckBox jCheckBox10) {
         this.jCheckBox10 = jCheckBox10;
     }
 
     public JCheckBox getjCheckBox11() {
         return jCheckBox11;
     }
 
     public void setjCheckBox11(JCheckBox jCheckBox11) {
         this.jCheckBox11 = jCheckBox11;
     }
 
     public JCheckBox getFotoceldasEntradas_jCheckBox() {
         return fotoceldas_entrada_escenas_jCheckBox;
     }
 
     public void setFotoceldasEntradas_jCheckBox(JCheckBox jCheckBox12) {
         this.fotoceldas_entrada_escenas_jCheckBox = jCheckBox12;
     }
 
     public JCheckBox getselBalastosEvento_jCheckBox() {
         return selBalastosEntradas_jCheckBox;
     }
 
     public void setBalastrosEntradas_jCheckBox(JCheckBox jCheckBox13) {
         this.selBalastosEntradas_jCheckBox = jCheckBox13;
     }
 
     public JCheckBox getSelGruposEntradas_jCheckBox() {
         return selGruposEntradas_jCheckBox;
     }
 
     public void setjCheckBox14(JCheckBox jCheckBox14) {
         this.selGruposEntradas_jCheckBox = jCheckBox14;
     }
 
     public JCheckBox getjCheckBox15() {
         return selEscenaEntrada_jCheckBox;
     }
 
     public void setjCheckBox15(JCheckBox jCheckBox15) {
         this.selEscenaEntrada_jCheckBox = jCheckBox15;
     }
 
     public JCheckBox getjCheckBox16() {
         return balastros_jCheckBox;
     }
 
     public void setjCheckBox16(JCheckBox jCheckBox16) {
         this.balastros_jCheckBox = jCheckBox16;
     }
 
     public JCheckBox getjCheckBox17() {
         return grupos_jCheckBox;
     }
 
     public void setjCheckBox17(JCheckBox jCheckBox17) {
         this.grupos_jCheckBox = jCheckBox17;
     }
 
     public JCheckBox getjCheckBox18() {
         return jCheckBox18;
     }
 
     public void setjCheckBox18(JCheckBox jCheckBox18) {
         this.jCheckBox18 = jCheckBox18;
     }
 
     public JCheckBox getjCheckBox19() {
         return jCheckBox19;
     }
 
     public void setjCheckBox19(JCheckBox jCheckBox19) {
         this.jCheckBox19 = jCheckBox19;
     }
 
     public JCheckBox getRepetirCicloEvento_jCheckBox() {
         return repetirCiclo_jCheckBox;
     }
 
     public void setRepetirCicloEvento_jCheckBox(JCheckBox jCheckBox2) {
         this.repetirCiclo_jCheckBox = jCheckBox2;
     }
 
     public JCheckBox getjCheckBox20() {
         return jCheckBox20;
     }
 
     public void setjCheckBox20(JCheckBox jCheckBox20) {
         this.jCheckBox20 = jCheckBox20;
     }
 
     public JCheckBox getLunes_jCheckBox() {
         return lunes_jCheckBox;
     }
 
     public void setLunes_jCheckBox(JCheckBox jCheckBox3) {
         this.lunes_jCheckBox = jCheckBox3;
     }
 
     public JCheckBox getMartes_jCheckBox() {
         return martes_jCheckBox;
     }
 
     public void setMartes_jCheckBox(JCheckBox jCheckBox4) {
         this.martes_jCheckBox = jCheckBox4;
     }
 
     public JCheckBox getMiercoles_jCheckBox() {
         return miercoles_jCheckBox;
     }
 
     public void setMiercoles_jCheckBox(JCheckBox jCheckBox5) {
         this.miercoles_jCheckBox = jCheckBox5;
     }
 
     public JCheckBox getJuevesjCheckBox() {
         return jueves_jCheckBox;
     }
 
     public void setJueves_jCheckBox(JCheckBox jCheckBox6) {
         this.jueves_jCheckBox = jCheckBox6;
     }
 
     public JCheckBox getViernes_jCheckBox() {
         return viernes_jCheckBox;
     }
 
     public void setViernes_jCheckBox(JCheckBox jCheckBox7) {
         this.viernes_jCheckBox = jCheckBox7;
     }
 
     public JComboBox getEventoNum_jComboBox() {
         return eventoNum_jComboBox;
     }
 
     public void setEventoNum_jComboBox(JComboBox eventoNum_jComboBox) {
         this.eventoNum_jComboBox = eventoNum_jComboBox;
     }
 
     public JCheckBox getSabado_jCheckBox() {
         return sabado_jCheckBox;
     }
 
     public void setSabado_jCheckBox(JCheckBox jCheckBox8) {
         this.sabado_jCheckBox = jCheckBox8;
     }
 
     public JCheckBox getDomingo_jCheckBox() {
         return domingo_jCheckBox;
     }
 
     public void setjCheckBox9(JCheckBox jCheckBox9) {
         this.domingo_jCheckBox = jCheckBox9;
     }
 
     public JComboBox getjComboBox2() {
         return jComboBox2;
     }
 
     public void setjComboBox2(JComboBox jComboBox2) {
         this.jComboBox2 = jComboBox2;
     }
 
     public JComboBox getBalastoNum_jComboBox() {
         return balastoNum_jComboBox;
     }
 
     public void setBalastoNum_jComboBox(JComboBox jComboBox3) {
         this.balastoNum_jComboBox = jComboBox3;
     }
 
     public JComboBox getEntradaNumero_jComboBox() {
         return entradaNumero_jComboBox;
     }
 
     public void setEntradaNumero_jComboBox(JComboBox jComboBox4) {
         this.entradaNumero_jComboBox = jComboBox4;
     }
 
     public JDateChooser getjDateChooser1() {
         return jDateChooser1;
     }
 
     public void setjDateChooser1(JDateChooser jDateChooser1) {
         this.jDateChooser1 = jDateChooser1;
     }
 
     public JDateChooser getjDateChooser2() {
         return jDateChooser2;
     }
 
     public void setjDateChooser2(JDateChooser jDateChooser2) {
         this.jDateChooser2 = jDateChooser2;
     }
 
     public JFormattedTextField getjFormattedTextField1() {
         return jFormattedTextField1;
     }
 
     public void setjFormattedTextField1(JFormattedTextField jFormattedTextField1) {
         this.jFormattedTextField1 = jFormattedTextField1;
     }
 
     public JFormattedTextField getjFormattedTextField2() {
         return jFormattedTextField2;
     }
 
     public void setjFormattedTextField2(JFormattedTextField jFormattedTextField2) {
         this.jFormattedTextField2 = jFormattedTextField2;
     }
 
     public JFormattedTextField getjFormattedTextField3() {
         return horaDiasEvento_jFormattedTextField;
     }
 
     public void setjFormattedTextField3(JFormattedTextField jFormattedTextField3) {
         this.horaDiasEvento_jFormattedTextField = jFormattedTextField3;
     }
 
     public JLabel getjLabel1() {
         return jLabel1;
     }
 
     public void setjLabel1(JLabel jLabel1) {
         this.jLabel1 = jLabel1;
     }
 
     public JLabel getjLabel10() {
         return jLabel10;
     }
 
     public void setjLabel10(JLabel jLabel10) {
         this.jLabel10 = jLabel10;
     }
 
     public JLabel getjLabel11() {
         return jLabel11;
     }
 
     public void setjLabel11(JLabel jLabel11) {
         this.jLabel11 = jLabel11;
     }
 
     public JLabel getjLabel12() {
         return jLabel12;
     }
 
     public void setjLabel12(JLabel jLabel12) {
         this.jLabel12 = jLabel12;
     }
 
     public JLabel getjLabel13() {
         return jLabel13;
     }
 
     public void setjLabel13(JLabel jLabel13) {
         this.jLabel13 = jLabel13;
     }
 
     public JLabel getjLabel14() {
         return jLabel14;
     }
 
     public void setjLabel14(JLabel jLabel14) {
         this.jLabel14 = jLabel14;
     }
 
     public JLabel getjLabel15() {
         return jLabel15;
     }
 
     public void setjLabel15(JLabel jLabel15) {
         this.jLabel15 = jLabel15;
     }
 
     public JLabel getjLabel16() {
         return jLabel16;
     }
 
     public void setjLabel16(JLabel jLabel16) {
         this.jLabel16 = jLabel16;
     }
 
     public JLabel getjLabel17() {
         return jLabel17;
     }
 
     public void setjLabel17(JLabel jLabel17) {
         this.jLabel17 = jLabel17;
     }
 
     public JLabel getjLabel18() {
         return jLabel18;
     }
 
     public void setjLabel18(JLabel jLabel18) {
         this.jLabel18 = jLabel18;
     }
 
     public JLabel getjLabel19() {
         return jLabel19;
     }
 
     public void setjLabel19(JLabel jLabel19) {
         this.jLabel19 = jLabel19;
     }
 
     public JLabel getjLabel2() {
         return jLabel2;
     }
 
     public void setjLabel2(JLabel jLabel2) {
         this.jLabel2 = jLabel2;
     }
 
     public JLabel getjLabel20() {
         return jLabel20;
     }
 
     public void setjLabel20(JLabel jLabel20) {
         this.jLabel20 = jLabel20;
     }
 
     public JLabel getjLabel21() {
         return jLabel21;
     }
 
     public void setjLabel21(JLabel jLabel21) {
         this.jLabel21 = jLabel21;
     }
 
     public JLabel getjLabel22() {
         return jLabel22;
     }
 
     public void setjLabel22(JLabel jLabel22) {
         this.jLabel22 = jLabel22;
     }
 
     public JLabel getjLabel23() {
         return jLabel23;
     }
 
     public void setjLabel23(JLabel jLabel23) {
         this.jLabel23 = jLabel23;
     }
 
     public JLabel getjLabel24() {
         return jLabel24;
     }
 
     public void setjLabel24(JLabel jLabel24) {
         this.jLabel24 = jLabel24;
     }
 
     public JLabel getjLabel25() {
         return jLabel25;
     }
 
     public void setjLabel25(JLabel jLabel25) {
         this.jLabel25 = jLabel25;
     }
 
     public JLabel getjLabel27() {
         return jLabel27;
     }
 
     public void setjLabel27(JLabel jLabel27) {
         this.jLabel27 = jLabel27;
     }
 
     public JLabel getjLabel28() {
         return jLabel28;
     }
 
     public void setjLabel28(JLabel jLabel28) {
         this.jLabel28 = jLabel28;
     }
 
     public JLabel getjLabel29() {
         return jLabel29;
     }
 
     public void setjLabel29(JLabel jLabel29) {
         this.jLabel29 = jLabel29;
     }
 
     public JLabel getjLabel3() {
         return jLabel3;
     }
 
     public void setjLabel3(JLabel jLabel3) {
         this.jLabel3 = jLabel3;
     }
 
     public JLabel getjLabel30() {
         return jLabel30;
     }
 
     public void setjLabel30(JLabel jLabel30) {
         this.jLabel30 = jLabel30;
     }
 
     public JLabel getjLabel31() {
         return jLabel31;
     }
 
     public void setjLabel31(JLabel jLabel31) {
         this.jLabel31 = jLabel31;
     }
 
     public JLabel getjLabel32() {
         return jLabel32;
     }
 
     public void setjLabel32(JLabel jLabel32) {
         this.jLabel32 = jLabel32;
     }
 
     public JLabel getjLabel33() {
         return pot_jLabel;
     }
 
     public void setjLabel33(JLabel jLabel33) {
         this.pot_jLabel = jLabel33;
     }
 
     public JLabel getjLabel34() {
         return jLabel34;
     }
 
     public void setjLabel34(JLabel jLabel34) {
         this.jLabel34 = jLabel34;
     }
 
     public JLabel getjLabel37() {
         return jLabel37;
     }
 
     public void setjLabel37(JLabel jLabel37) {
         this.jLabel37 = jLabel37;
     }
 
     public JLabel getjLabel39() {
         return jLabel39;
     }
 
     public void setjLabel39(JLabel jLabel39) {
         this.jLabel39 = jLabel39;
     }
 
     public JLabel getjLabel4() {
         return jLabel4;
     }
 
     public void setjLabel4(JLabel jLabel4) {
         this.jLabel4 = jLabel4;
     }
 
 
     public JLabel getjLabel41() {
         return jLabel41;
     }
 
     public void setjLabel41(JLabel jLabel41) {
         this.jLabel41 = jLabel41;
     }
 
     public JLabel getjLabel44() {
         return jLabel44;
     }
 
     public void setjLabel44(JLabel jLabel44) {
         this.jLabel44 = jLabel44;
     }
 
     public JLabel getjLabel45() {
         return jLabel45;
     }
 
     public void setjLabel45(JLabel jLabel45) {
         this.jLabel45 = jLabel45;
     }
 
     public JLabel getjLabel5() {
         return nivelEscena_jLabel;
     }
 
     public void setjLabel5(JLabel jLabel5) {
         this.nivelEscena_jLabel = jLabel5;
     }
 
     public JLabel getjLabel53() {
         return jLabel53;
     }
 
     public void setjLabel53(JLabel jLabel53) {
         this.jLabel53 = jLabel53;
     }
 
     public JLabel getjLabel54() {
         return jLabel54;
     }
 
     public void setjLabel54(JLabel jLabel54) {
         this.jLabel54 = jLabel54;
     }
 
     public JLabel getjLabel55() {
         return jLabel55;
     }
 
     public void setjLabel55(JLabel jLabel55) {
         this.jLabel55 = jLabel55;
     }
 
     public JLabel getjLabel56() {
         return jLabel56;
     }
 
     public void setjLabel56(JLabel jLabel56) {
         this.jLabel56 = jLabel56;
     }
 
     public JLabel getjLabel57() {
         return jLabel57;
     }
 
     public void setjLabel57(JLabel jLabel57) {
         this.jLabel57 = jLabel57;
     }
 
     public JLabel getjLabel58() {
         return jLabel58;
     }
 
     public void setjLabel58(JLabel jLabel58) {
         this.jLabel58 = jLabel58;
     }
 
     public JLabel getjLabel6() {
         return nombreEscena_jLabel;
     }
 
     public void setjLabel6(JLabel jLabel6) {
         this.nombreEscena_jLabel = jLabel6;
     }
 
     public JLabel getjLabel61() {
         return jLabel61;
     }
 
     public void setjLabel61(JLabel jLabel61) {
         this.jLabel61 = jLabel61;
     }
 
     public JLabel getjLabel62() {
         return jLabel62;
     }
 
     public void setjLabel62(JLabel jLabel62) {
         this.jLabel62 = jLabel62;
     }
 
     public JLabel getjLabel63() {
         return jLabel63;
     }
 
     public void setjLabel63(JLabel jLabel63) {
         this.jLabel63 = jLabel63;
     }
 
     public JLabel getjLabel64() {
         return jLabel64;
     }
 
     public void setjLabel64(JLabel jLabel64) {
         this.jLabel64 = jLabel64;
     }
 
     public JLabel getjLabel65() {
         return jLabel65;
     }
 
     public void setjLabel65(JLabel jLabel65) {
         this.jLabel65 = jLabel65;
     }
 
     public JLabel getjLabel66() {
         return jLabel66;
     }
 
     public void setjLabel66(JLabel jLabel66) {
         this.jLabel66 = jLabel66;
     }
 
     public JLabel getjLabel67() {
         return jLabel67;
     }
 
     public void setjLabel67(JLabel jLabel67) {
         this.jLabel67 = jLabel67;
     }
 
     public JLabel getjLabel68() {
         return jLabel68;
     }
 
     public void setjLabel68(JLabel jLabel68) {
         this.jLabel68 = jLabel68;
     }
 
     public JLabel getjLabel7() {
         return jLabel7;
     }
 
     public void setjLabel7(JLabel jLabel7) {
         this.jLabel7 = jLabel7;
     }
 
     public JLabel getjLabel8() {
         return jLabel8;
     }
 
     public void setjLabel8(JLabel jLabel8) {
         this.jLabel8 = jLabel8;
     }
 
     public JLabel getjLabel9() {
         return nivel_jLabel;
     }
 
     public void setjLabel9(JLabel jLabel9) {
         this.nivel_jLabel = jLabel9;
     }
 
     public JList getjList12() {
         return jList12;
     }
 
     public void setjList12(JList jList12) {
         this.jList12 = jList12;
     }
 
     public JList getjList13() {
         return jList13;
     }
 
     public void setjList13(JList jList13) {
         this.jList13 = jList13;
     }
 
     public JList getjList14() {
         return jList14;
     }
 
     public void setjList14(JList jList14) {
         this.jList14 = jList14;
     }
 
     public JList getjList15() {
         return jList15;
     }
 
     public void setjList15(JList jList15) {
         this.jList15 = jList15;
     }
 
     public JList getjList16() {
         return jList16;
     }
 
     public void setjList16(JList jList16) {
         this.jList16 = jList16;
     }
 
     public JList getjList17() {
         return jList17;
     }
 
     public void setjList17(JList jList17) {
         this.jList17 = jList17;
     }
 
     public JList getjList2() {
         return jList2;
     }
 
     public void setjList2(JList jList2) {
         this.jList2 = jList2;
     }
 
     public JList getjList3() {
         return jList3;
     }
 
     public void setjList3(JList jList3) {
         this.jList3 = jList3;
     }
 
     public JList getjList4() {
         return balastrosDisponibles_jList;
     }
 
     public void setjList4(JList jList4) {
         this.balastrosDisponibles_jList = jList4;
     }
 
     public JList getBalastosAfectados_jList() {
         return balastosAfectados_jList;
     }
 
     public void setjList5(JList jList5) {
         this.balastosAfectados_jList = jList5;
     }
 
     public JList getjList8() {
         return jList8;
     }
 
     public void setjList8(JList jList8) {
         this.jList8 = jList8;
     }
 
     public JList getjList9() {
         return jList9;
     }
 
     public void setjList9(JList jList9) {
         this.jList9 = jList9;
     }
 
     public JMenu getjMenu1() {
         return archivo_jMenu;
     }
 
     public void setjMenu1(JMenu jMenu1) {
         this.archivo_jMenu = jMenu1;
     }
 
     public JMenuItem getjMenuItem1() {
         return config_jMenuItem;
     }
 
     public void setjMenuItem1(JMenuItem jMenuItem1) {
         this.config_jMenuItem = jMenuItem1;
     }
 
     public JMenuItem getjMenuItem5() {
         return guardarEnFlash_jMenuItem;
     }
 
     public void setjMenuItem5(JMenuItem jMenuItem5) {
         this.guardarEnFlash_jMenuItem = jMenuItem5;
     }
 
     public JMenuItem getjMenuItem6() {
         return leerFlash_jMenuItem;
     }
 
     public void setjMenuItem6(JMenuItem jMenuItem6) {
         this.leerFlash_jMenuItem = jMenuItem6;
     }
 
     public JPanel getjPanel10() {
         return jPanel10;
     }
 
     public void setjPanel10(JPanel jPanel10) {
         this.jPanel10 = jPanel10;
     }
 
     public JPanel getjPanel11() {
         return jPanel11;
     }
 
     public void setjPanel11(JPanel jPanel11) {
         this.jPanel11 = jPanel11;
     }
 
     public JPanel getjPanel12() {
         return configuracionRed_jPanel;
     }
 
     public void setjPanel12(JPanel jPanel12) {
         this.configuracionRed_jPanel = jPanel12;
     }
 
     public JPanel getjPanel13() {
         return jPanel13;
     }
 
     public void setjPanel13(JPanel jPanel13) {
         this.jPanel13 = jPanel13;
     }
 
     public JPanel getjPanel2() {
         return jPanel2;
     }
 
     public void setjPanel2(JPanel jPanel2) {
         this.jPanel2 = jPanel2;
     }
 
     public JPanel getjPanel3() {
         return jPanel3;
     }
 
     public void setjPanel3(JPanel jPanel3) {
         this.jPanel3 = jPanel3;
     }
 
     public JPanel getjPanel4() {
         return escenasBalastos_jPanel;
     }
 
     public void setjPanel4(JPanel jPanel4) {
         this.escenasBalastos_jPanel = jPanel4;
     }
 
     public JPanel getjPanel5() {
         return porFechaYHora_jPanel;
     }
 
     public void setjPanel5(JPanel jPanel5) {
         this.porFechaYHora_jPanel = jPanel5;
     }
 
     public JPanel getjPanel6() {
         return balastoDali_jPanel;
     }
 
     public void setjPanel6(JPanel jPanel6) {
         this.balastoDali_jPanel = jPanel6;
     }
 
     public JPanel getjPanel7() {
         return jPanel7;
     }
 
     public void setjPanel7(JPanel jPanel7) {
         this.jPanel7 = jPanel7;
     }
 
     public JPanel getjPanel8() {
         return porDiasEvento_jPanel;
     }
 
     public void setjPanel8(JPanel jPanel8) {
         this.porDiasEvento_jPanel = jPanel8;
     }
 
     public JPanel getjPanel9() {
         return jPanel9;
     }
 
     public void setjPanel9(JPanel jPanel9) {
         this.jPanel9 = jPanel9;
     }
 
     public JRadioButton getjRadioButton1() {
         return jRadioButton1;
     }
 
     public void setjRadioButton1(JRadioButton jRadioButton1) {
         this.jRadioButton1 = jRadioButton1;
     }
 
     public JRadioButton getjRadioButton2() {
         return jRadioButton2;
     }
 
     public void setjRadioButton2(JRadioButton jRadioButton2) {
         this.jRadioButton2 = jRadioButton2;
     }
 
     public JRadioButton getjRadioButton3() {
         return jRadioButton3;
     }
 
     public void setjRadioButton3(JRadioButton jRadioButton3) {
         this.jRadioButton3 = jRadioButton3;
     }
 
     public JRadioButton getjRadioButton4() {
         return jRadioButton4;
     }
 
     public void setjRadioButton4(JRadioButton jRadioButton4) {
         this.jRadioButton4 = jRadioButton4;
     }
 
     public JScrollPane getjScrollPane1() {
         return arbolJerarquia_jScrollPane;
     }
 
     public void setjScrollPane1(JScrollPane jScrollPane1) {
         this.arbolJerarquia_jScrollPane = jScrollPane1;
     }
 
     public JScrollPane getjScrollPane10() {
         return jScrollPane10;
     }
 
     public void setjScrollPane10(JScrollPane jScrollPane10) {
         this.jScrollPane10 = jScrollPane10;
     }
 
     public JScrollPane getjScrollPane13() {
         return jScrollPane13;
     }
 
     public void setjScrollPane13(JScrollPane jScrollPane13) {
         this.jScrollPane13 = jScrollPane13;
     }
 
     public JScrollPane getjScrollPane14() {
         return jScrollPane14;
     }
 
     public void setjScrollPane14(JScrollPane jScrollPane14) {
         this.jScrollPane14 = jScrollPane14;
     }
 
     public JScrollPane getjScrollPane15() {
         return jScrollPane15;
     }
 
     public void setjScrollPane15(JScrollPane jScrollPane15) {
         this.jScrollPane15 = jScrollPane15;
     }
 
     public JScrollPane getjScrollPane16() {
         return jScrollPane16;
     }
 
     public void setjScrollPane16(JScrollPane jScrollPane16) {
         this.jScrollPane16 = jScrollPane16;
     }
 
     public JScrollPane getjScrollPane17() {
         return jScrollPane17;
     }
 
     public void setjScrollPane17(JScrollPane jScrollPane17) {
         this.jScrollPane17 = jScrollPane17;
     }
 
     public JScrollPane getjScrollPane18() {
         return jScrollPane18;
     }
 
     public void setjScrollPane18(JScrollPane jScrollPane18) {
         this.jScrollPane18 = jScrollPane18;
     }
 
     public JScrollPane getjScrollPane2() {
         return jScrollPane2;
     }
 
     public void setjScrollPane2(JScrollPane jScrollPane2) {
         this.jScrollPane2 = jScrollPane2;
     }
 
     public JScrollPane getjScrollPane3() {
         return jScrollPane3;
     }
 
     public void setjScrollPane3(JScrollPane jScrollPane3) {
         this.jScrollPane3 = jScrollPane3;
     }
 
     public JScrollPane getjScrollPane4() {
         return jScrollPane4;
     }
 
     public void setjScrollPane4(JScrollPane jScrollPane4) {
         this.jScrollPane4 = jScrollPane4;
     }
 
     public JScrollPane getjScrollPane5() {
         return jScrollPane5;
     }
 
     public void setjScrollPane5(JScrollPane jScrollPane5) {
         this.jScrollPane5 = jScrollPane5;
     }
 
     public JScrollPane getjScrollPane6() {
         return jScrollPane6;
     }
 
     public void setjScrollPane6(JScrollPane jScrollPane6) {
         this.jScrollPane6 = jScrollPane6;
     }
 
     public JScrollPane getjScrollPane9() {
         return jScrollPane9;
     }
 
     public void setjScrollPane9(JScrollPane jScrollPane9) {
         this.jScrollPane9 = jScrollPane9;
     }
 
     public JSeparator getjSeparator1() {
         return jSeparator1;
     }
 
     public void setjSeparator1(JSeparator jSeparator1) {
         this.jSeparator1 = jSeparator1;
     }
 
     public JSeparator getjSeparator2() {
         return jSeparator2;
     }
 
     public void setjSeparator2(JSeparator jSeparator2) {
         this.jSeparator2 = jSeparator2;
     }
 
     public Separator getjSeparator3() {
         return jSeparator3;
     }
 
     public void setjSeparator3(Separator jSeparator3) {
         this.jSeparator3 = jSeparator3;
     }
 
     public JSlider getjSlider1() {
         return jSlider1;
     }
 
     public void setjSlider1(JSlider jSlider1) {
         this.jSlider1 = jSlider1;
     }
 
     public JSpinner getjSpinner3() {
         return jSpinner3;
     }
 
     public void setjSpinner3(JSpinner jSpinner3) {
         this.jSpinner3 = jSpinner3;
     }
 
     public JTable getjTable1() {
         return jTable1;
     }
 
     public void setjTable1(JTable jTable1) {
         this.jTable1 = jTable1;
     }
 
     public JTextField getjTextField1() {
         return balastoNombreSmaiee_jTextField;
     }
 
     public JTextField getBalastoMax_jTextField() {
         return balastoMax_jTextField;
     }
 
     public void setBalastoMax_jTextField(JTextField balastoMax_jTextField) {
         this.balastoMax_jTextField = balastoMax_jTextField;
     }
 
     public JTextField getBalastoMin_jTextField() {
         return balastoMin_jTextField;
     }
 
     public void setBalastoMin_jTextField(JTextField balastoMin_jTextField) {
         this.balastoMin_jTextField = balastoMin_jTextField;
     }
 
     public void setjTextField1(JTextField jTextField1) {
         this.balastoNombreSmaiee_jTextField = jTextField1;
     }
 
     public JTextField getjTextField10() {
         return jTextField10;
     }
 
     public void setjTextField10(JTextField jTextField10) {
         this.jTextField10 = jTextField10;
     }
 
     public JTextField getNombreevento_jTextField() {
         return nombreEvento_jTextField;
     }
 
     public void setjTextField11(JTextField jTextField11) {
         this.nombreEvento_jTextField = jTextField11;
     }
 
     public JTextField getjTextField2() {
         return jTextField2;
     }
 
     public void setjTextField2(JTextField jTextField2) {
         this.jTextField2 = jTextField2;
     }
 
     public JTextField getjTextField20() {
         return nivel_jTextField;
     }
 
     public void setjTextField20(JTextField jTextField20) {
         this.nivel_jTextField = jTextField20;
     }
 
     public JTextField getjTextField23() {
         return nivelEscena_jTextField;
     }
 
     public void setjTextField23(JTextField jTextField23) {
         this.nivelEscena_jTextField = jTextField23;
     }
 
     public JTextField getjTextField25() {
         return jTextField25;
     }
 
     public void setjTextField25(JTextField jTextField25) {
         this.jTextField25 = jTextField25;
     }
 
     public JTextField getjTextField27() {
         return balastoMin_jTextField;
     }
 
     public void setjTextField27(JTextField jTextField27) {
         this.balastoMin_jTextField = jTextField27;
     }
 
     public JTextField getjTextField28() {
         return balastoMax_jTextField;
     }
 
     public void setjTextField28(JTextField jTextField28) {
         this.balastoMax_jTextField = jTextField28;
     }
 
     public JTextField getjTextField29() {
         return balastoFT_jTextField;
     }
 
     public void setjTextField29(JTextField jTextField29) {
         this.balastoFT_jTextField = jTextField29;
     }
 
     public JTextField getjTextField3() {
         return gruposNombreSmaiee_jTextField;
     }
 
     public void setjTextField3(JTextField jTextField3) {
         this.gruposNombreSmaiee_jTextField = jTextField3;
     }
 
     public JTextField getjTextField30() {
         return balastoFR_jTextField;
     }
 
     public void setjTextField30(JTextField jTextField30) {
         this.balastoFR_jTextField = jTextField30;
     }
 
     public JTextField getjTextField31() {
         return balastoLF_jTextField;
     }
 
     public void setjTextField31(JTextField jTextField31) {
         this.balastoLF_jTextField = jTextField31;
     }
 
     public JTextField getjTextField32() {
         return balastoLX_jTextField;
     }
 
     public void setjTextField32(JTextField jTextField32) {
         this.balastoLX_jTextField = jTextField32;
     }
 
     public JTextField getjTextField33() {
         return balastoPot_jTextField;
     }
 
     public void setjTextField33(JTextField jTextField33) {
         this.balastoPot_jTextField = jTextField33;
     }
 
     public JTextField getjTextField34() {
         return jTextField34;
     }
 
     public void setjTextField34(JTextField jTextField34) {
         this.jTextField34 = jTextField34;
     }
 
     public JTextField getjTextField35() {
         return jTextField35;
     }
 
     public void setjTextField35(JTextField jTextField35) {
         this.jTextField35 = jTextField35;
     }
 
     public JTextField getjTextField36() {
         return jTextField36;
     }
 
     public void setjTextField36(JTextField jTextField36) {
         this.jTextField36 = jTextField36;
     }
 
     public JTextField getNombreEscenaJTextField() {
         return nombreEscena_jTextField;
     }
 
     public void setNombreEscenaJTextField(JTextField jTextField4) {
         this.nombreEscena_jTextField = jTextField4;
     }
 
     public JTextField getjTextField5() {
         return jTextField5;
     }
 
     public void setjTextField5(JTextField jTextField5) {
         this.jTextField5 = jTextField5;
     }
 
     public JTextField getjTextField6() {
         return jTextField6;
     }
 
     public void setjTextField6(JTextField jTextField6) {
         this.jTextField6 = jTextField6;
     }
 
     public JTextField getjTextField7() {
         return jTextField7;
     }
 
     public void setjTextField7(JTextField jTextField7) {
         this.jTextField7 = jTextField7;
     }
 
     public JTextField getEntradaFotoceldaNivelIlum_jTextField() {
         return entradaFotoceldaNivelIlum_jTextField;
     }
 
     public void setEntradaFotoceldaNivelIlum_jTextField(JTextField jTextField8) {
         this.entradaFotoceldaNivelIlum_jTextField = jTextField8;
     }
 
     public JTextField getEntradaFotoceldaNivelDeseado_jTextField() {
         return entradaFotoceldaNivelDeseado_jTextField;
     }
 
     public void setjTextField9(JTextField jTextField9) {
         this.entradaFotoceldaNivelDeseado_jTextField = jTextField9;
     }
 
     public JTree getArbol_jTree() {
         return arbol_jTree;
     }
 
     public void setjTree1(JTree jTree1) {
         this.arbol_jTree = jTree1;
     }
 
     public JLabel getLabelDns() {
         return labelDns;
     }
 
     public void setLabelDns(JLabel labelDns) {
         this.labelDns = labelDns;
     }
 
     public JLabel getLabelGateway() {
         return labelGateway;
     }
 
     public void setLabelGateway(JLabel labelGateway) {
         this.labelGateway = labelGateway;
     }
 
     public JLabel getLabelIp() {
         return labelIp;
     }
 
     public void setLabelIp(JLabel labelIp) {
         this.labelIp = labelIp;
     }
 
     public JLabel getLabelMascara() {
         return labelMascara;
     }
 
     public void setLabelMascara(JLabel labelMascara) {
         this.labelMascara = labelMascara;
     }
 
     public JLabel getLabelPuerto() {
         return labelPuerto;
     }
 
     public void setLabelPuerto(JLabel labelPuerto) {
         this.labelPuerto = labelPuerto;
     }
 
     public JMenuBar getMainMenuBar() {
         return mainMenuBar;
     }
 
     public void setMainMenuBar(JMenuBar menuBar) {
         this.mainMenuBar = menuBar;
     }
 
     public JPanel getPanelBalastos() {
         return panelBalastos;
     }
 
     public void setPanelBalastos(JPanel panelBalastos) {
         this.panelBalastos = panelBalastos;
     }
 
     public JPanel getPanelBienvenida() {
         return panelBienvenida;
     }
 
     public void setPanelBienvenida(JPanel panelBienvenida) {
         this.panelBienvenida = panelBienvenida;
     }
 
     public JPanel getPanelBotonera() {
         return panelBotonera;
     }
 
     public void setPanelBotonera(JPanel panelBotonera) {
         this.panelBotonera = panelBotonera;
     }
 
     public JPanel getPanelConfEntradas() {
         return panelConfEntradas;
     }
 
     public void setPanelConfEntradas(JPanel panelConfEntradas) {
         this.panelConfEntradas = panelConfEntradas;
     }
 
     public JPanel getPanelConfiguracion() {
         return panelConfiguracion;
     }
 
     public void setPanelConfiguracion(JPanel panelConfiguracion) {
         this.panelConfiguracion = panelConfiguracion;
     }
 
     public JPanel getPanelEntradaInit() {
         return panelEntradaInit;
     }
 
     public void setPanelEntradaInit(JPanel panelEntradaInit) {
         this.panelEntradaInit = panelEntradaInit;
     }
 
     public JPanel getPanelEntradas() {
         return panelEntradas;
     }
 
     public void setPanelEntradas(JPanel panelEntradas) {
         this.panelEntradas = panelEntradas;
     }
 
     public JPanel getPanelEscenas() {
         return panelEscenas;
     }
 
     public void setPanelEscenas(JPanel panelEscenas) {
         this.panelEscenas = panelEscenas;
     }
 
     public JPanel getPanelEventos() {
         return panelEventos;
     }
 
     public void setPanelEventos(JPanel panelEventos) {
         this.panelEventos = panelEventos;
     }
 
     public JPanel getPanelFotocelda() {
         return panelFotocelda;
     }
 
     public void setPanelFotocelda(JPanel panelFotocelda) {
         this.panelFotocelda = panelFotocelda;
     }
 
     public JPanel getPanelGrupos() {
         return panelGrupos;
     }
 
     public void setPanelGrupos(JPanel panelGrupos) {
         this.panelGrupos = panelGrupos;
     }
 
     public JPanel getPanelPpal() {
         return panelPpal;
     }
 
     public void setPanelPpal(JPanel panelPpal) {
         this.panelPpal = panelPpal;
     }
 
     public JPanel getPanelSensor() {
         return panelSensor;
     }
 
     public void setPanelSensor(JPanel panelSensor) {
         this.panelSensor = panelSensor;
     }
 
     public JPanel getPanelTiempoReal() {
         return panelTiempoReal;
     }
 
     public void setPanelTiempoReal(JPanel panelTiempoReal) {
         this.panelTiempoReal = panelTiempoReal;
     }
 
     public JRadioButton getRbIsMaster() {
         return rbIsMaster;
     }
 
     public void setRbIsMaster(JRadioButton rbIsMaster) {
         this.rbIsMaster = rbIsMaster;
     }
 
     public JRadioButton getRbIsSlave() {
         return rbIsSlave;
     }
 
     public void setRbIsSlave(JRadioButton rbIsSlave) {
         this.rbIsSlave = rbIsSlave;
     }
 
     public JPanel getStatusBar() {
         return statusBar;
     }
 
     public void setStatusBar(JPanel statusBar) {
         this.statusBar = statusBar;
     }
 
     public JLabel getStatusLabel() {
         return statusLabel;
     }
 
     public void setStatusLabel(JLabel statusLabel) {
         this.statusLabel = statusLabel;
     }
 
     public JPanel getTabConfiguracion() {
         return configuracionSmaiee_jPanel;
     }
 
     public void setTabConfiguracion(JPanel tabConfiguracion) {
         this.configuracionSmaiee_jPanel = tabConfiguracion;
     }
 
     public JPanel getTabMonitoreo() {
         return monitoreoTiempoReal_jPanel;
     }
 
     public void setTabMonitoreo(JPanel tabMonitoreo) {
         this.monitoreoTiempoReal_jPanel = tabMonitoreo;
     }
 
     public JComboBox getGruposNum_jComboBox() {
         return gruposNum_jComboBox;
     }
 
     public void setGruposNum_jComboBox(JComboBox jComboBox5) {
         this.gruposNum_jComboBox = jComboBox5;
     }
 
     public JTabbedPane getTabbedPane() {
         return tabbedPane;
     }
 
     public void setTabbedPane(JTabbedPane tabbedPane) {
         this.tabbedPane = tabbedPane;
     }
 
     public JComboBox getEscenaNumero_jComboBox() {
         return escenaNumero_jComboBox;
     }
 
     public void setEscenaNumero_jComboBox(JComboBox jComboBox6) {
         this.escenaNumero_jComboBox = jComboBox6;
     }
 
 //    public JTextField getjTextField13() {
 //        return numEvento_jTextField;
 //    }
 //
 //    public void setjTextField13(JTextField jTextField13) {
 //        this.numEvento_jTextField = jTextField13;
 //    }
     public DAOJmodbus getDao() {
         return dao;
     }
 
     public void setDao(DAOJmodbus dao) {
         this.dao = dao;
     }
 
     public GeneralControl getGeneralCtrl() {
         return generalCtrl;
     }
 
     public void setGeneralCtrl(GeneralControl generalCtrl) {
         this.generalCtrl = generalCtrl;
     }
 
     public RealTimeControl getRealCtrl() {
         return realCtrl;
     }
 
     public void setRealCtrl(RealTimeControl realCtrl) {
         this.realCtrl = realCtrl;
     }
 
     public ThreadManager getThreadManager() {
         return threadManager;
     }
 
     public void setThreadManager(ThreadManager threadManager) {
         this.threadManager = threadManager;
     }
 
     /**
      * Método que expande o contrae el arbol de jerarquía
      *
      * @param tree Arbol a expandir o contraer
      * @param path
      * @param expanded
      */
     public void expandirArbol(JTree tree, TreePath path, boolean expanded) {
         Object lastNode = path.getLastPathComponent();
         for (int i = 0; i < tree.getModel().getChildCount(lastNode); i++) {
             Object child = tree.getModel().getChild(lastNode, i);
             TreePath pathToChild = path.pathByAddingChild(child);
             expandirArbol(tree, pathToChild, expanded);
         }
         if (expanded) {
             tree.expandPath(path);
         } else {
             tree.collapsePath(path);
         }
 
 
     }
 
     /**
      * Limpia el arbol de jerarquia.
      *
      * @param arbol
      */
     public void limpiarArbol(JTree arbol) {
         TreePath[] selectionPaths = arbol.getSelectionPaths();
         arbol.removeSelectionPaths(selectionPaths);
         establecerModeloArbolDefault(arbol);
     }
 
     /**
      * Método que establece un nuevo modelo vacio pero con la estructura al
      * arbol que se le pase por parámetros.
      *
      * @param arbol
      */
     public void establecerModeloArbolDefault(JTree arbol) {
         javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("SMAIEE");
         javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Balastos");
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Grupos");
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Escenas");
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Entradas");
         javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Botoneras");
         treeNode2.add(treeNode3);
         treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Fotoceldas");
         treeNode2.add(treeNode3);
         treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Sensores");
         treeNode2.add(treeNode3);
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Eventos");
         treeNode1.add(treeNode2);
         arbol.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         arbol.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         arbol.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         arbol.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 arbol_jTreeMouseClicked(evt);
             }
 
             @Override
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 arbol_jTreeMousePressed(evt);
             }
         });
         arbol.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
             @Override
             public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                 arbol_jTreeValueChanged(evt);
             }
         });
     }
 
     public javax.swing.JTextField getBalastoDir_jTextField() {
         return balastoDir_jTextField;
     }
 
     public void setBalastoDir_jTextField(javax.swing.JTextField balastoDir_jTextField) {
         this.balastoDir_jTextField = balastoDir_jTextField;
     }
 
     public javax.swing.JTextField getBalastoFR_jTextField() {
         return balastoFR_jTextField;
     }
 
     public void setBalastoFR_jTextField(javax.swing.JTextField balastoFR_jTextField) {
         this.balastoFR_jTextField = balastoFR_jTextField;
     }
 
     public javax.swing.JTextField getBalastoFT_jTextField() {
         return balastoFT_jTextField;
     }
 
     public void setBalastoFT_jTextField(javax.swing.JTextField balastoFT_jTextField) {
         this.balastoFT_jTextField = balastoFT_jTextField;
     }
 
     public javax.swing.JTextField getBalastoLF_jTextField() {
         return balastoLF_jTextField;
     }
 
     public void setBalastoLF_jTextField(javax.swing.JTextField balastoLF_jTextField) {
         this.balastoLF_jTextField = balastoLF_jTextField;
     }
 
     public javax.swing.JTextField getBalastoLX_jTextField() {
         return balastoLX_jTextField;
     }
 
     public void setBalastoLX_jTextField(javax.swing.JTextField balastoLX_jTextField) {
         this.balastoLX_jTextField = balastoLX_jTextField;
     }
 
     public javax.swing.JTextField getBalastoPot_jTextField() {
         return balastoPot_jTextField;
     }
 
     public void setBalastoPot_jTextField(javax.swing.JTextField balastoPot_jTextField) {
         this.balastoPot_jTextField = balastoPot_jTextField;
     }
 
     public javax.swing.JComboBox getBalastoConfiguracion_jComboBox() {
         return balastoConfiguracion_jComboBox;
     }
 
     public void setBalastoConfiguracion_jComboBox(javax.swing.JComboBox balastoConfiguracion_jComboBox) {
         this.balastoConfiguracion_jComboBox = balastoConfiguracion_jComboBox;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox1() {
         return grupo_jCheckBox1;
     }
 
     public void setGrupo_jCheckBox1(javax.swing.JCheckBox grupo_jCheckBox1) {
         this.grupo_jCheckBox1 = grupo_jCheckBox1;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox10() {
         return grupo_jCheckBox10;
     }
 
     public void setGrupo_jCheckBox10(javax.swing.JCheckBox grupo_jCheckBox10) {
         this.grupo_jCheckBox10 = grupo_jCheckBox10;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox11() {
         return grupo_jCheckBox11;
     }
 
     public void setGrupo_jCheckBox11(javax.swing.JCheckBox grupo_jCheckBox11) {
         this.grupo_jCheckBox11 = grupo_jCheckBox11;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox12() {
         return grupo_jCheckBox12;
     }
 
     public void setGrupo_jCheckBox12(javax.swing.JCheckBox grupo_jCheckBox12) {
         this.grupo_jCheckBox12 = grupo_jCheckBox12;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox13() {
         return grupo_jCheckBox13;
     }
 
     public void setGrupo_jCheckBox13(javax.swing.JCheckBox grupo_jCheckBox13) {
         this.grupo_jCheckBox13 = grupo_jCheckBox13;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox14() {
         return grupo_jCheckBox14;
     }
 
     public void setGrupo_jCheckBox14(javax.swing.JCheckBox grupo_jCheckBox14) {
         this.grupo_jCheckBox14 = grupo_jCheckBox14;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox15() {
         return grupo_jCheckBox15;
     }
 
     public void setGrupo_jCheckBox15(javax.swing.JCheckBox grupo_jCheckBox15) {
         this.grupo_jCheckBox15 = grupo_jCheckBox15;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox16() {
         return grupo_jCheckBox16;
     }
 
     public void setGrupo_jCheckBox16(javax.swing.JCheckBox grupo_jCheckBox16) {
         this.grupo_jCheckBox16 = grupo_jCheckBox16;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox2() {
         return grupo_jCheckBox2;
     }
 
     public void setGrupo_jCheckBox2(javax.swing.JCheckBox grupo_jCheckBox2) {
         this.grupo_jCheckBox2 = grupo_jCheckBox2;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox3() {
         return grupo_jCheckBox3;
     }
 
     public void setGrupo_jCheckBox3(javax.swing.JCheckBox grupo_jCheckBox3) {
         this.grupo_jCheckBox3 = grupo_jCheckBox3;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox4() {
         return grupo_jCheckBox4;
     }
 
     public void setGrupo_jCheckBox4(javax.swing.JCheckBox grupo_jCheckBox4) {
         this.grupo_jCheckBox4 = grupo_jCheckBox4;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox5() {
         return grupo_jCheckBox5;
     }
 
     public void setGrupo_jCheckBox5(javax.swing.JCheckBox grupo_jCheckBox5) {
         this.grupo_jCheckBox5 = grupo_jCheckBox5;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox6() {
         return grupo_jCheckBox6;
     }
 
     public void setGrupo_jCheckBox6(javax.swing.JCheckBox grupo_jCheckBox6) {
         this.grupo_jCheckBox6 = grupo_jCheckBox6;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox7() {
         return grupo_jCheckBox7;
     }
 
     public void setGrupo_jCheckBox7(javax.swing.JCheckBox grupo_jCheckBox7) {
         this.grupo_jCheckBox7 = grupo_jCheckBox7;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox8() {
         return grupo_jCheckBox8;
     }
 
     public void setGrupo_jCheckBox8(javax.swing.JCheckBox grupo_jCheckBox8) {
         this.grupo_jCheckBox8 = grupo_jCheckBox8;
     }
 
     public javax.swing.JCheckBox getGrupo_jCheckBox9() {
         return grupo_jCheckBox9;
     }
 
     public void setGrupo_jCheckBox9(javax.swing.JCheckBox grupo_jCheckBox9) {
         this.grupo_jCheckBox9 = grupo_jCheckBox9;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor1() {
         return sliderConValor1;
     }
 
     public void setSliderConValor1(com.isolux.view.componentes.SliderConValor sliderConValor1) {
         this.sliderConValor1 = sliderConValor1;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor10() {
         return sliderConValor10;
     }
 
     public void setSliderConValor10(com.isolux.view.componentes.SliderConValor sliderConValor10) {
         this.sliderConValor10 = sliderConValor10;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor11() {
         return sliderConValor11;
     }
 
     public void setSliderConValor11(com.isolux.view.componentes.SliderConValor sliderConValor11) {
         this.sliderConValor11 = sliderConValor11;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor12() {
         return sliderConValor12;
     }
 
     public void setSliderConValor12(com.isolux.view.componentes.SliderConValor sliderConValor12) {
         this.sliderConValor12 = sliderConValor12;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor13() {
         return sliderConValor13;
     }
 
     public void setSliderConValor13(com.isolux.view.componentes.SliderConValor sliderConValor13) {
         this.sliderConValor13 = sliderConValor13;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor14() {
         return sliderConValor14;
     }
 
     public void setSliderConValor14(com.isolux.view.componentes.SliderConValor sliderConValor14) {
         this.sliderConValor14 = sliderConValor14;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor15() {
         return sliderConValor15;
     }
 
     public void setSliderConValor15(com.isolux.view.componentes.SliderConValor sliderConValor15) {
         this.sliderConValor15 = sliderConValor15;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor16() {
         return sliderConValor16;
     }
 
     public void setSliderConValor16(com.isolux.view.componentes.SliderConValor sliderConValor16) {
         this.sliderConValor16 = sliderConValor16;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor2() {
         return sliderConValor2;
     }
 
     public void setSliderConValor2(com.isolux.view.componentes.SliderConValor sliderConValor2) {
         this.sliderConValor2 = sliderConValor2;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor3() {
         return sliderConValor3;
     }
 
     public void setSliderConValor3(com.isolux.view.componentes.SliderConValor sliderConValor3) {
         this.sliderConValor3 = sliderConValor3;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor4() {
         return sliderConValor4;
     }
 
     public void setSliderConValor4(com.isolux.view.componentes.SliderConValor sliderConValor4) {
         this.sliderConValor4 = sliderConValor4;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor5() {
         return sliderConValor5;
     }
 
     public void setSliderConValor5(com.isolux.view.componentes.SliderConValor sliderConValor5) {
         this.sliderConValor5 = sliderConValor5;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor6() {
         return sliderConValor6;
     }
 
     public void setSliderConValor6(com.isolux.view.componentes.SliderConValor sliderConValor6) {
         this.sliderConValor6 = sliderConValor6;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor7() {
         return sliderConValor7;
     }
 
     public void setSliderConValor7(com.isolux.view.componentes.SliderConValor sliderConValor7) {
         this.sliderConValor7 = sliderConValor7;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor8() {
         return sliderConValor8;
     }
 
     public void setSliderConValor8(com.isolux.view.componentes.SliderConValor sliderConValor8) {
         this.sliderConValor8 = sliderConValor8;
     }
 
     public com.isolux.view.componentes.SliderConValor getSliderConValor9() {
         return sliderConValor9;
     }
 
     public void setSliderConValor9(com.isolux.view.componentes.SliderConValor sliderConValor9) {
         this.sliderConValor9 = sliderConValor9;
     }
 
     public boolean isOperacionModbusEnProgreso() {
         return operacionModbusEnProgreso;
     }
 
     public void setOperacionModbusEnProgreso(boolean operacionModbusEnProgreso) {
         this.operacionModbusEnProgreso = operacionModbusEnProgreso;
     }
 
     /**
      * @return the barraProgreso_jProgressBar
      */
     public javax.swing.JProgressBar getBarraProgreso_jProgressBar() {
         return barraProgreso_jProgressBar;
     }
 
     /**
      * @param barraProgreso_jProgressBar the barraProgreso_jProgressBar to set
      */
     public void setBarraProgreso_jProgressBar(javax.swing.JProgressBar barraProgreso_jProgressBar) {
         this.barraProgreso_jProgressBar = barraProgreso_jProgressBar;
     }
 
     public javax.swing.JButton getGrabarIp_jButton() {
         return grabarIp_jButton;
     }
 
     public javax.swing.JTextField getIp_jTextField() {
         return ip_jTextField;
     }
 
     public void setIp_jTextField(javax.swing.JTextField ip_jTextField) {
         this.ip_jTextField = ip_jTextField;
     }
 
     public javax.swing.JFormattedTextField getGateway_jTextField() {
         return gateway_jTextField;
     }
 
     public void setGateway_jTextField(javax.swing.JFormattedTextField gateway_jTextField) {
         this.gateway_jTextField = gateway_jTextField;
     }
 
     public javax.swing.JFormattedTextField getMask_jTextField() {
         return mask_jTextField;
     }
 
     public void setMask_jTextField(javax.swing.JFormattedTextField mask_jTextField) {
         this.mask_jTextField = mask_jTextField;
     }
 
     public javax.swing.JTextField getPuerto_jTextField() {
         return puerto_jTextField;
     }
 
     public void setPuerto_jTextField(javax.swing.JTextField puerto_jTextField) {
         this.puerto_jTextField = puerto_jTextField;
     }
 
     public javax.swing.JFormattedTextField getHoraDiasEvento_jFormattedTextField() {
         return horaDiasEvento_jFormattedTextField;
     }
 
     public void setHoraDiasEvento_jFormattedTextField(javax.swing.JFormattedTextField horaDiasEvento_jFormattedTextField) {
         this.horaDiasEvento_jFormattedTextField = horaDiasEvento_jFormattedTextField;
     }
 
     // End of variables declaration                   
     //</editor-fold>
     public BalastosControl getBalastoCtrl() {
         return balastoCtrl;
     }
 
     public EventControl getEventCtrl() {
         return eventCtrl;
     }
 
     public GrupoControl getGroupsCtrl() {
         return groupsCtrl;
     }
 
     public EntradaControl getInsCtrl() {
         return insCtrl;
     }
 
     public EscenaControl getSceneCtrl() {
         return sceneCtrl;
     }
 
     public BalastosConfiguracionControl getBalastoConfigCtrl() {
         return balastoConfigCtrl;
     }
 
     public javax.swing.JButton getEnviarConfiguracion_jButton2() {
         return enviarConfiguracion_jButton2;
     }
 
     private void enviarEscenaSmaiee() {
       this.sceneCtrl.saveElement(this);
             
     }
 
     public javax.swing.JTextField getBalastoNombreSmaiee_jTextField() {
         return balastoNombreSmaiee_jTextField;
     }
 
     public void setBalastoNombreSmaiee_jTextField(javax.swing.JTextField balastoNombreSmaiee_jTextField) {
         this.balastoNombreSmaiee_jTextField = balastoNombreSmaiee_jTextField;
     }
 
     public javax.swing.JTextField getGruposNombreSmaiee_jTextField() {
         return gruposNombreSmaiee_jTextField;
     }
 
     public void setGruposNombreSmaiee_jTextField(javax.swing.JTextField gruposNombreSmaiee_jTextField) {
         this.gruposNombreSmaiee_jTextField = gruposNombreSmaiee_jTextField;
     }
 
     public javax.swing.JTextField getNombreEscena_jTextField() {
         return nombreEscena_jTextField;
     }
 
     public void setNombreEscena_jTextField(javax.swing.JTextField nombreEscena_jTextField) {
         this.nombreEscena_jTextField = nombreEscena_jTextField;
     }
 
     public javax.swing.JTextField getNombreEvento_jTextField() {
         return nombreEvento_jTextField;
     }
 
     public void setNombreEvento_jTextField(javax.swing.JTextField nombreEvento_jTextField) {
         this.nombreEvento_jTextField = nombreEvento_jTextField;
     }
 }
