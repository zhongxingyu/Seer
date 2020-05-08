 /*
  * This is the main form of the SIMS game. Each form element is created here.
  * From here different functions are called.
  */
 package test;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 /**
  *
  * @author Jannik, Yulia, Tobias, Nadir, Jörg, Dawid
  */
 public class Sims_1 extends javax.swing.JFrame {
 
     //**************************************************************  //by Dawid
     //*******************Section of constants: start****************
     //**************************************************************
     public static final String _dataFolderName = "data";
     public static final String _profileFileName = "profile.txt";
     public static final String _gameFileName = "game.txt";
     public static final String _inventoryFileName = "inventory.txt";
     public static final String _usersFileName = "users.csv";
     public static final String _UCoinsName = "UCoins";
     public static final String _creditsName = "credits";
     public static final String _loginFileName = "login.txt";
     public static final String _adminName = "administr4t0r";
     public static final String _adminPass = "p4ssw0rd";
     //***************************************************************
     //***************Section of constants: end***********************
     //***************************************************************
     //###############################################################
     //***************************************************************  //by Dawid
     //**************Section of global vars: start********************
     //***************************************************************
     public static User _mainuser;
     public static Game1 _maingame;
     public static Admin _mainadmin;
     public ActivityPhase activityPhase;
     public static PlanningPhase planningPhase;
     private javax.swing.JButton[] activityPhaseButtons;
     //***************************************************************
     //***************Section of global vars: end*********************
     //***************************************************************
     public CardLayout cl;
     Item item = new Item();
     CoinExchange exchange = new CoinExchange();
     private JPanel panel_mazeMinigame = new JPanel();
     private JPanel panel_numberMinigame = new JPanel();
     private JPanel panel_snakeMinigame = new JPanel();
     public JDialog miniGameFinished = new JDialog();
 
     /**
      * Sets up and initializes each component and some additional settings. The
      * auto login function is called from here as well.
      */
     public Sims_1() {
         panel_mazeMinigame = new MinigameMazegame();
         panel_numberMinigame = new MinigameNumbergame();
         button_afterGame = new JButton("Game");
         button_afterGame.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 goToNextPage();
             }
         });
         miniGameFinished.add(button_afterGame);
         initComponents();
         jPanel2.add(panel_numberMinigame, "card6");
         jPanel2.add(panel_mazeMinigame,"card7");
         setSize(1000, 700);
         buyCoins.setSize(400, 320);
         buyCoins.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         dialog_error.setSize(400, 320);
         dialog_error.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         warning.setSize(400, 320); //added  by Julia
         warning.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         lectorChangedDialog.setSize(400, 320); //added by Julia
         lectorChangedDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         lectorNotChangedDialog.setSize(400, 320);
         lectorNotChangedDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         cheatUsedDialog.setSize(400, 320); //added by Julia
         cheatUsedDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         jPanel2.setVisible(false);                  //changes by Dawid
         startPlanningGame.setVisible(false);
         panel_Register.setVisible(false);
         panel_Login.setVisible(true);
         panel_adminUser.setVisible(false);
         panel_Profile.setVisible(false);
         label_lErr.setText("");
         panel_adminUser.setVisible(false);
         cl = (CardLayout) (jPanel2.getLayout());
         setIconImage(new ImageIcon(getClass().getResource("/pictures/icon_test.png")).getImage()); // Icon added by Nadir
         setLocationRelativeTo(null); // Fenster zentrieren by Nadir
         autoLogin();
         panel_Admin.setVisible(false); // change by Nadir
         this.activityPhaseButtons=constructActivityPhaseButtons(); //added by Jörg
         noSave_overlay.setVisible(false); //by Nadir , deactivates the overlay for development phase, remove later
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buyCoins = new javax.swing.JDialog();
         label_swapper = new javax.swing.JLabel();
         label_swapperUcoins = new javax.swing.JLabel();
         label_swapperUcoinsAmount = new javax.swing.JLabel();
         label_swapperCredits = new javax.swing.JLabel();
         label_swapperCreditsAmount = new javax.swing.JLabel();
         label_currentExchange = new javax.swing.JLabel();
         textfield_swapperUcoins = new javax.swing.JTextField();
         button_swapperExchange = new javax.swing.JButton();
         button_swapperAbord = new javax.swing.JButton();
         textfield_swapperCredits = new javax.swing.JTextField();
         label_swapperArrow1 = new javax.swing.JLabel();
         label_swapperArrow3 = new javax.swing.JLabel();
         dialog_error = new javax.swing.JDialog();
         label_shopMessage = new javax.swing.JLabel();
         label_shopMessage1 = new javax.swing.JLabel();
         label_shopMessage2 = new javax.swing.JLabel();
         button_shopMessageOk = new javax.swing.JButton();
         warning = new javax.swing.JDialog();
         jBut_JA = new javax.swing.JButton();
         jBut_NEIN = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         lectorChangedDialog = new javax.swing.JDialog();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextArea2 = new javax.swing.JTextArea();
         jBut_OKlector = new javax.swing.JButton();
         cheatUsedDialog = new javax.swing.JDialog();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTextArea3 = new javax.swing.JTextArea();
         jBut_OKcheat = new javax.swing.JButton();
         lectorNotChangedDialog = new javax.swing.JDialog();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTextArea4 = new javax.swing.JTextArea();
         jBut_OKnotchanged = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         startPlanningGame = new javax.swing.JPanel();
         panel_Profile = new javax.swing.JPanel();
         textfield_pKontoname = new javax.swing.JTextField();
         textfield_pEmail1 = new javax.swing.JTextField();
         textfield_pEmail2 = new javax.swing.JTextField();
         textfield_pName = new javax.swing.JTextField();
         textfield_pSurename = new javax.swing.JTextField();
         password_pPass1 = new javax.swing.JPasswordField();
         password_pPass2 = new javax.swing.JPasswordField();
         button_pSave = new javax.swing.JButton();
         label_pKontoname = new javax.swing.JLabel();
         label_pPass1 = new javax.swing.JLabel();
         label_pPass2 = new javax.swing.JLabel();
         label_pEmail1 = new javax.swing.JLabel();
         label_pEmail2 = new javax.swing.JLabel();
         label_pFirstName = new javax.swing.JLabel();
         label_pSecondName = new javax.swing.JLabel();
         button_pCancel = new javax.swing.JButton();
         label_pErrAccname = new javax.swing.JLabel();
         label_pErrPass1 = new javax.swing.JLabel();
         label_pErrPass2 = new javax.swing.JLabel();
         label_pErrEmail1 = new javax.swing.JLabel();
         label_pErrEmail2 = new javax.swing.JLabel();
         label_pErrFirstName = new javax.swing.JLabel();
         label_pErrLastName = new javax.swing.JLabel();
         Menu_overlay1 = new javax.swing.JLabel();
         noSave_overlay = new javax.swing.JLabel();
         loggedinas = new javax.swing.JLabel();
         Menu_overlay = new javax.swing.JLabel();
         button_menuStartNewGame = new javax.swing.JButton();
         button_menuExit = new javax.swing.JButton();
         button_menuLoadGame = new javax.swing.JButton();
         button_menuCredits = new javax.swing.JButton();
         button_menuProfile = new javax.swing.JButton();
         button_menuLogOut = new javax.swing.JButton();
         button_menuStatistic = new javax.swing.JButton();
         jPanel2 = new javax.swing.JPanel();
         gamePlanning = new javax.swing.JPanel();
         Logo = new javax.swing.JPanel();
         jLab_Logo = new javax.swing.JLabel();
         Header = new javax.swing.JPanel();
         jLab_Planning_unused01 = new javax.swing.JLabel();
         jLab_Planning_unused02 = new javax.swing.JLabel();
         jLab_Planning_unused03 = new javax.swing.JLabel();
         jProgB_Müdigkeit = new javax.swing.JProgressBar();
         jProgB_Motivation = new javax.swing.JProgressBar();
         jProgB_Wissen = new javax.swing.JProgressBar();
         jLab_Planning_unused04 = new javax.swing.JLabel();
         Shop = new javax.swing.JPanel();
         jBut_startShop = new javax.swing.JButton();
         Navi = new javax.swing.JPanel();
         jPan_StudSwitch = new javax.swing.JPanel();
         jLab_StudSwitch = new javax.swing.JLabel();
         jLab_Planning_unused05 = new javax.swing.JLabel();
         jLab_Planning_unused06 = new javax.swing.JLabel();
         jLab_StudCounter = new javax.swing.JLabel();
         jToggleBut_SwitchStud = new javax.swing.JToggleButton();
         jPan_DozSwitch = new javax.swing.JPanel();
         jLab_DozSwitch = new javax.swing.JLabel();
         jLab_Planning_unused07 = new javax.swing.JLabel();
         jLab_Planning_unused08 = new javax.swing.JLabel();
         jLab_DozCounter = new javax.swing.JLabel();
         jBut_ChangeLector = new javax.swing.JButton();
         jPan_ItemSelect = new javax.swing.JPanel();
         jComboB_Items = new javax.swing.JComboBox();
         jBut_ComboB_useItem = new javax.swing.JButton();
         jPan_ItemStorage = new javax.swing.JPanel();
         jLab_Planning_unused09 = new javax.swing.JLabel();
         jLab_Redbull = new javax.swing.JLabel();
         jLab_Duplo = new javax.swing.JLabel();
         jLab_OMNI = new javax.swing.JLabel();
         jPan_Planning_Play = new javax.swing.JPanel();
         jBut_Play = new javax.swing.JButton();
         StudField = new javax.swing.JPanel();
         jBut_Dozent = new javax.swing.JButton();
         jBut_1 = new javax.swing.JButton();
         jBut_2 = new javax.swing.JButton();
         jBut_3 = new javax.swing.JButton();
         jBut_4 = new javax.swing.JButton();
         jBut_5 = new javax.swing.JButton();
         jBut_6 = new javax.swing.JButton();
         jBut_7 = new javax.swing.JButton();
         jBut_8 = new javax.swing.JButton();
         jBut_9 = new javax.swing.JButton();
         jBut_10 = new javax.swing.JButton();
         jBut_11 = new javax.swing.JButton();
         jBut_12 = new javax.swing.JButton();
         jBut_13 = new javax.swing.JButton();
         jBut_14 = new javax.swing.JButton();
         jBut_15 = new javax.swing.JButton();
         jBut_16 = new javax.swing.JButton();
         jBut_17 = new javax.swing.JButton();
         jBut_18 = new javax.swing.JButton();
         jBut_19 = new javax.swing.JButton();
         jBut_20 = new javax.swing.JButton();
         jBut_21 = new javax.swing.JButton();
         jBut_22 = new javax.swing.JButton();
         jBut_23 = new javax.swing.JButton();
         jBut_24 = new javax.swing.JButton();
         jBut_25 = new javax.swing.JButton();
         jBut_26 = new javax.swing.JButton();
         jBut_27 = new javax.swing.JButton();
         jBut_28 = new javax.swing.JButton();
         jBut_29 = new javax.swing.JButton();
         jBut_30 = new javax.swing.JButton();
         jLab_DozSwitch1 = new javax.swing.JLabel();
         shop = new javax.swing.JPanel();
         jPanel18 = new javax.swing.JPanel();
         jLabel55 = new javax.swing.JLabel();
         jPanel19 = new javax.swing.JPanel();
         label_inventar = new javax.swing.JLabel();
         jLabel58 = new javax.swing.JLabel();
         jLabel59 = new javax.swing.JLabel();
         jLabel60 = new javax.swing.JLabel();
         jLabel61 = new javax.swing.JLabel();
         label_item4 = new javax.swing.JLabel();
         label_item1 = new javax.swing.JLabel();
         label_item2 = new javax.swing.JLabel();
         label_item1Name = new javax.swing.JLabel();
         label_item1Amount = new javax.swing.JLabel();
         label_item2Name = new javax.swing.JLabel();
         label_item2Amount = new javax.swing.JLabel();
         label_item3Name = new javax.swing.JLabel();
         label_item3Amount = new javax.swing.JLabel();
         label_item3 = new javax.swing.JLabel();
         label_item4Name = new javax.swing.JLabel();
         label_item4Amount = new javax.swing.JLabel();
         jPanel20 = new javax.swing.JPanel();
         ucoinsShop = new javax.swing.JLabel();
         creditsShop = new javax.swing.JLabel();
         punkteShop = new javax.swing.JLabel();
         button_startExchange = new javax.swing.JButton();
         jLabel70 = new javax.swing.JLabel();
         jLabel71 = new javax.swing.JLabel();
         jLabel72 = new javax.swing.JLabel();
         jLabel85 = new javax.swing.JLabel();
         jPanel21 = new javax.swing.JPanel();
         label_returnToPlanningPhase = new javax.swing.JLabel();
         jPanel22 = new javax.swing.JPanel();
         label_cheatSheetOverlay = new javax.swing.JLabel();
         label_omniOverlay = new javax.swing.JLabel();
         label_duploOverlay = new javax.swing.JLabel();
         label_redBullOverlay = new javax.swing.JLabel();
         panel_redBull = new javax.swing.JPanel();
         label_redBullName = new javax.swing.JLabel();
         label_redBullAmount = new javax.swing.JLabel();
         label_redBullLocked = new javax.swing.JLabel();
         label_redBullImage = new javax.swing.JLabel();
         panel_omniSense = new javax.swing.JPanel();
         label_omniName = new javax.swing.JLabel();
         label_omniLocked = new javax.swing.JLabel();
         label_omniAmount = new javax.swing.JLabel();
         label_omniAudioImage = new javax.swing.JLabel();
         panel_duplo = new javax.swing.JPanel();
         label_duploName = new javax.swing.JLabel();
         label_duploAmount = new javax.swing.JLabel();
         label_duploLocked = new javax.swing.JLabel();
         label_duploImage = new javax.swing.JLabel();
         panel_cheatSheet = new javax.swing.JPanel();
         label_cheatSheetName = new javax.swing.JLabel();
         label_cheatSheetLocked = new javax.swing.JLabel();
         label_cheatSheetAmount = new javax.swing.JLabel();
         label_cheatSheetImage = new javax.swing.JLabel();
         gamePlaying = new javax.swing.JPanel();
         jPanel3 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         jPanel4 = new javax.swing.JPanel();
         jLabel11 = new javax.swing.JLabel();
         label_item3Inv = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jLabel19 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         jLabel21 = new javax.swing.JLabel();
         label_ucoinsInv = new javax.swing.JLabel();
         label_item2Inv = new javax.swing.JLabel();
         label_creditsInv = new javax.swing.JLabel();
         label_item3InvName = new javax.swing.JLabel();
         label_item3InvAmount = new javax.swing.JLabel();
         label_item1Inv = new javax.swing.JLabel();
         jLabel13 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         label_item1InvName = new javax.swing.JLabel();
         label_item2InvName = new javax.swing.JLabel();
         label_item2InvAmount = new javax.swing.JLabel();
         label_item1InvAmount = new javax.swing.JLabel();
         jPanel5 = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         KnowledgeBar = new javax.swing.JProgressBar();
         MotivationBar = new javax.swing.JProgressBar();
         TirednessBar = new javax.swing.JProgressBar();
         jButton1 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jLabel9 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jProgressBar5 = new javax.swing.JProgressBar();
         jProgressBar6 = new javax.swing.JProgressBar();
         jPanel6 = new javax.swing.JPanel();
         label_timer = new javax.swing.JLabel();
         panel_activityPhaseStudField = new javax.swing.JPanel();
         jBut_Dozent1 = new javax.swing.JButton();
         button_stud1 = new javax.swing.JButton();
         button_stud2 = new javax.swing.JButton();
         button_stud3 = new javax.swing.JButton();
         button_stud4 = new javax.swing.JButton();
         button_stud5 = new javax.swing.JButton();
         button_stud6 = new javax.swing.JButton();
         button_stud7 = new javax.swing.JButton();
         button_stud8 = new javax.swing.JButton();
         button_stud9 = new javax.swing.JButton();
         button_stud10 = new javax.swing.JButton();
         button_stud11 = new javax.swing.JButton();
         button_stud12 = new javax.swing.JButton();
         button_stud13 = new javax.swing.JButton();
         button_stud14 = new javax.swing.JButton();
         button_stud15 = new javax.swing.JButton();
         button_stud16 = new javax.swing.JButton();
         button_stud17 = new javax.swing.JButton();
         button_stud18 = new javax.swing.JButton();
         button_stud19 = new javax.swing.JButton();
         button_stud20 = new javax.swing.JButton();
         button_stud21 = new javax.swing.JButton();
         button_stud22 = new javax.swing.JButton();
         button_stud23 = new javax.swing.JButton();
         button_stud24 = new javax.swing.JButton();
         button_stud25 = new javax.swing.JButton();
         button_stud26 = new javax.swing.JButton();
         button_stud27 = new javax.swing.JButton();
         button_stud28 = new javax.swing.JButton();
         button_stud29 = new javax.swing.JButton();
         button_stud30 = new javax.swing.JButton();
         jLab_DozSwitch2 = new javax.swing.JLabel();
         panel_Login = new javax.swing.JPanel();
         panel_Register = new javax.swing.JPanel();
         textfield_rKontoname = new javax.swing.JTextField();
         textfield_rEmail1 = new javax.swing.JTextField();
         textfield_rEmail2 = new javax.swing.JTextField();
         textfield_rName = new javax.swing.JTextField();
         textfield_rSurename = new javax.swing.JTextField();
         password_rPass1 = new javax.swing.JPasswordField();
         password_rPass2 = new javax.swing.JPasswordField();
         button_rRegister = new javax.swing.JButton();
         label_rKontoname = new javax.swing.JLabel();
         label_rPass1 = new javax.swing.JLabel();
         label_rPass2 = new javax.swing.JLabel();
         label_rEmail1 = new javax.swing.JLabel();
         label_rEmail2 = new javax.swing.JLabel();
         label_rFirstName = new javax.swing.JLabel();
         label_rSecondName = new javax.swing.JLabel();
         button_cancel = new javax.swing.JButton();
         label_rErrAccname = new javax.swing.JLabel();
         label_rErrPass1 = new javax.swing.JLabel();
         label_rErrPass2 = new javax.swing.JLabel();
         label_rErrEmail1 = new javax.swing.JLabel();
         label_rErrEmail2 = new javax.swing.JLabel();
         label_rErrFirstName = new javax.swing.JLabel();
         label_rErrLastName = new javax.swing.JLabel();
         background_register = new javax.swing.JLabel();
         textfield_Kontoname = new javax.swing.JTextField();
         button_Login = new javax.swing.JButton();
         button_Register = new javax.swing.JToggleButton();
         password_Pass = new javax.swing.JPasswordField();
         jLabel12 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         check_saveUser = new javax.swing.JCheckBox();
         check_autoLogin = new javax.swing.JCheckBox();
         label_lErr = new javax.swing.JLabel();
         buton_enterAdmin = new javax.swing.JButton();
         login_background = new javax.swing.JLabel();
         panel_adminUser = new javax.swing.JPanel();
         button_aSave = new javax.swing.JButton();
         label_aAccname = new javax.swing.JLabel();
         label_aPass = new javax.swing.JLabel();
         textfield_aKontoname = new javax.swing.JTextField();
         textfield_aEmail = new javax.swing.JTextField();
         label_aEmail = new javax.swing.JLabel();
         button_auCancel1 = new javax.swing.JButton();
         label_aFirstName = new javax.swing.JLabel();
         textfield_aSurename = new javax.swing.JTextField();
         label_aLastName = new javax.swing.JLabel();
         textfield_aName = new javax.swing.JTextField();
         lable_aProfil = new javax.swing.JLabel();
         textfield_aUCoins = new javax.swing.JTextField();
         label_aUCions = new javax.swing.JLabel();
         textfield_aCredits = new javax.swing.JTextField();
         label_aCredits = new javax.swing.JLabel();
         textfield_aDuplo = new javax.swing.JTextField();
         label_aDuplo = new javax.swing.JLabel();
         textfield_aRedBull = new javax.swing.JTextField();
         textfield_aOMNI = new javax.swing.JTextField();
         label_aRedBull = new javax.swing.JLabel();
         label_aOMNI = new javax.swing.JLabel();
         check_aSpicker = new javax.swing.JCheckBox();
         label_aSpicker = new javax.swing.JLabel();
         slider_aMonth = new javax.swing.JSlider();
         label_aMonth = new javax.swing.JLabel();
         label_aGame = new javax.swing.JLabel();
         textfield_aPass = new javax.swing.JTextField();
         label_aMonthVal = new javax.swing.JLabel();
         button_aDelete = new javax.swing.JButton();
         label_aErrAccName = new javax.swing.JLabel();
         label_aErrPass = new javax.swing.JLabel();
         label_aErrEmail = new javax.swing.JLabel();
         label_aErrFirstName = new javax.swing.JLabel();
         label_aErrLastName = new javax.swing.JLabel();
         label_aErrUCoins = new javax.swing.JLabel();
         background_adminUser = new javax.swing.JLabel();
         panel_Admin = new javax.swing.JPanel();
         background_admin = new javax.swing.JLabel();
         button_aCancel = new javax.swing.JButton();
 
         buyCoins.setLocationByPlatform(true);
         buyCoins.setModal(true);
         buyCoins.setName("dialog_buyCoins"); // NOI18N
         buyCoins.getContentPane().setLayout(null);
 
         label_swapper.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
         label_swapper.setText("UCoins => Credits Tausch");
         buyCoins.getContentPane().add(label_swapper);
         label_swapper.setBounds(30, 20, 370, 60);
 
         label_swapperUcoins.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_swapperUcoins.setText("UCoins:");
         buyCoins.getContentPane().add(label_swapperUcoins);
         label_swapperUcoins.setBounds(50, 90, 80, 30);
 
         label_swapperUcoinsAmount.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_swapperUcoinsAmount.setText("5");
         buyCoins.getContentPane().add(label_swapperUcoinsAmount);
         label_swapperUcoinsAmount.setBounds(120, 80, 80, 50);
 
         label_swapperCredits.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_swapperCredits.setText("Credits:");
         buyCoins.getContentPane().add(label_swapperCredits);
         label_swapperCredits.setBounds(50, 160, 70, 30);
 
         label_swapperCreditsAmount.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_swapperCreditsAmount.setText("600");
         buyCoins.getContentPane().add(label_swapperCreditsAmount);
         label_swapperCreditsAmount.setBounds(120, 150, 80, 50);
 
         label_currentExchange.setText("1 UCoin = 100 Credits");
         buyCoins.getContentPane().add(label_currentExchange);
         label_currentExchange.setBounds(230, 120, 140, 40);
 
         textfield_swapperUcoins.setMinimumSize(new java.awt.Dimension(30, 20));
         textfield_swapperUcoins.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 textfield_swapperUcoinsKeyReleased(evt);
             }
         });
         buyCoins.getContentPane().add(textfield_swapperUcoins);
         textfield_swapperUcoins.setBounds(170, 90, 50, 30);
 
         button_swapperExchange.setText("Tauschen");
         button_swapperExchange.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_swapperExchangeActionPerformed(evt);
             }
         });
         buyCoins.getContentPane().add(button_swapperExchange);
         button_swapperExchange.setBounds(50, 220, 130, 23);
 
         button_swapperAbord.setText("Abbrechen");
         button_swapperAbord.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_swapperAbordActionPerformed(evt);
             }
         });
         buyCoins.getContentPane().add(button_swapperAbord);
         button_swapperAbord.setBounds(210, 220, 130, 23);
 
         textfield_swapperCredits.setEditable(false);
         textfield_swapperCredits.setMinimumSize(new java.awt.Dimension(30, 20));
         buyCoins.getContentPane().add(textfield_swapperCredits);
         textfield_swapperCredits.setBounds(170, 160, 50, 30);
 
         label_swapperArrow1.setText("||");
         buyCoins.getContentPane().add(label_swapperArrow1);
         label_swapperArrow1.setBounds(190, 130, 8, 14);
 
         label_swapperArrow3.setText("\\/");
         buyCoins.getContentPane().add(label_swapperArrow3);
         label_swapperArrow3.setBounds(190, 140, 40, 14);
 
         dialog_error.setModal(true);
         dialog_error.getContentPane().setLayout(null);
 
         label_shopMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_shopMessage.setText("Shop -  Mitteilung");
         dialog_error.getContentPane().add(label_shopMessage);
         label_shopMessage.setBounds(50, 10, 290, 70);
 
         label_shopMessage1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_shopMessage1.setText("Erwerbe neue Credits im Spiel oder tausche UCoins!");
         dialog_error.getContentPane().add(label_shopMessage1);
         label_shopMessage1.setBounds(10, 110, 380, 70);
 
         label_shopMessage2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_shopMessage2.setText("Du hast leider nicht mehr genug Credits/UCoins!");
         dialog_error.getContentPane().add(label_shopMessage2);
         label_shopMessage2.setBounds(10, 80, 380, 70);
 
         button_shopMessageOk.setText("OK");
         button_shopMessageOk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_shopMessageOkActionPerformed(evt);
             }
         });
         dialog_error.getContentPane().add(button_shopMessageOk);
         button_shopMessageOk.setBounds(170, 210, 47, 23);
 
         warning.setModal(true);
         warning.addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosed(java.awt.event.WindowEvent evt) {
                 warningWindowClosed(evt);
             }
         });
 
         jBut_JA.setText("ja");
         jBut_JA.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_JAMouseClicked(evt);
             }
         });
         jBut_JA.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jBut_JAActionPerformed(evt);
             }
         });
 
         jBut_NEIN.setText("nein");
         jBut_NEIN.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_NEINMouseClicked(evt);
             }
         });
         jBut_NEIN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jBut_NEINActionPerformed(evt);
             }
         });
 
         jTextArea1.setColumns(20);
         jTextArea1.setEditable(false);
         jTextArea1.setRows(5);
         jTextArea1.setText("\t\tAchtung!  \nSpicker ist ein mal im Semester einstzbarer\nJocker. Du kannst den nur EIN MAL pro Semester\neinem beliebigen Studenten schenken.\nDann wird er die Klausuren auf jeden Fall\nbestehen. \n\nWillst du wirklich den Spicker einsetzen?\nWenn ja, klicke auf \"JA\" Button und wähle\ndanach deinen Lieblingsstudenten.");
         jScrollPane1.setViewportView(jTextArea1);
 
         javax.swing.GroupLayout warningLayout = new javax.swing.GroupLayout(warning.getContentPane());
         warning.getContentPane().setLayout(warningLayout);
         warningLayout.setHorizontalGroup(
             warningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(warningLayout.createSequentialGroup()
                 .addGap(26, 26, 26)
                 .addComponent(jBut_JA, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                 .addComponent(jBut_NEIN, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(28, 28, 28))
             .addComponent(jScrollPane1)
         );
         warningLayout.setVerticalGroup(
             warningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(warningLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(warningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jBut_JA, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                     .addComponent(jBut_NEIN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(0, 31, Short.MAX_VALUE))
         );
 
         lectorChangedDialog.setModal(true);
 
         jTextArea2.setColumns(20);
         jTextArea2.setEditable(false);
         jTextArea2.setRows(5);
         jTextArea2.setText("\t\n\n\tDu hast jetzt deinen \n\tDozenten getauscht!");
         jScrollPane2.setViewportView(jTextArea2);
 
         jBut_OKlector.setText("OK");
         jBut_OKlector.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_OKlectorMouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout lectorChangedDialogLayout = new javax.swing.GroupLayout(lectorChangedDialog.getContentPane());
         lectorChangedDialog.getContentPane().setLayout(lectorChangedDialogLayout);
         lectorChangedDialogLayout.setHorizontalGroup(
             lectorChangedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(lectorChangedDialogLayout.createSequentialGroup()
                 .addGroup(lectorChangedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(lectorChangedDialogLayout.createSequentialGroup()
                         .addGap(46, 46, 46)
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(lectorChangedDialogLayout.createSequentialGroup()
                         .addGap(134, 134, 134)
                         .addComponent(jBut_OKlector, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(56, Short.MAX_VALUE))
         );
         lectorChangedDialogLayout.setVerticalGroup(
             lectorChangedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(lectorChangedDialogLayout.createSequentialGroup()
                 .addGap(87, 87, 87)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                 .addComponent(jBut_OKlector, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(27, 27, 27))
         );
 
         cheatUsedDialog.setModal(true);
 
         jTextArea3.setColumns(20);
         jTextArea3.setEditable(false);
         jTextArea3.setRows(5);
         jTextArea3.setText("\t\n\tDu hast den Spicker\n\teingesetzt!");
         jScrollPane3.setViewportView(jTextArea3);
 
         jBut_OKcheat.setText("OK");
         jBut_OKcheat.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_OKcheatMouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout cheatUsedDialogLayout = new javax.swing.GroupLayout(cheatUsedDialog.getContentPane());
         cheatUsedDialog.getContentPane().setLayout(cheatUsedDialogLayout);
         cheatUsedDialogLayout.setHorizontalGroup(
             cheatUsedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(cheatUsedDialogLayout.createSequentialGroup()
                 .addGroup(cheatUsedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(cheatUsedDialogLayout.createSequentialGroup()
                         .addGap(56, 56, 56)
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(cheatUsedDialogLayout.createSequentialGroup()
                         .addGap(112, 112, 112)
                         .addComponent(jBut_OKcheat, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(78, Short.MAX_VALUE))
         );
         cheatUsedDialogLayout.setVerticalGroup(
             cheatUsedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(cheatUsedDialogLayout.createSequentialGroup()
                 .addGap(26, 26, 26)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(28, 28, 28)
                 .addComponent(jBut_OKcheat, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(68, Short.MAX_VALUE))
         );
 
         lectorNotChangedDialog.setModal(true);
 
         jTextArea4.setColumns(20);
         jTextArea4.setRows(5);
         jTextArea4.setText("Du darfst den Dozenten \nmomentan nicht wechseln!");
         jScrollPane4.setViewportView(jTextArea4);
 
         jBut_OKnotchanged.setText("OK");
         jBut_OKnotchanged.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_OKnotchangedMouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout lectorNotChangedDialogLayout = new javax.swing.GroupLayout(lectorNotChangedDialog.getContentPane());
         lectorNotChangedDialog.getContentPane().setLayout(lectorNotChangedDialogLayout);
         lectorNotChangedDialogLayout.setHorizontalGroup(
             lectorNotChangedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(lectorNotChangedDialogLayout.createSequentialGroup()
                 .addGroup(lectorNotChangedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(lectorNotChangedDialogLayout.createSequentialGroup()
                         .addGap(80, 80, 80)
                         .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(lectorNotChangedDialogLayout.createSequentialGroup()
                         .addGap(120, 120, 120)
                         .addComponent(jBut_OKnotchanged, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(57, Short.MAX_VALUE))
         );
         lectorNotChangedDialogLayout.setVerticalGroup(
             lectorNotChangedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(lectorNotChangedDialogLayout.createSequentialGroup()
                 .addGap(24, 24, 24)
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(57, 57, 57)
                 .addComponent(jBut_OKnotchanged, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(77, Short.MAX_VALUE))
         );
 
         jLabel1.setText("jLabel1");
 
         jLabel3.setText("jLabel3");
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("S.I.M.S.");
         setMinimumSize(new java.awt.Dimension(1000, 700));
         setResizable(false);
         getContentPane().setLayout(new java.awt.GridLayout(1, 0));
 
         jPanel1.setMaximumSize(new java.awt.Dimension(1000, 700));
         jPanel1.setMinimumSize(new java.awt.Dimension(1000, 700));
         jPanel1.setPreferredSize(new java.awt.Dimension(1000, 700));
         jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         startPlanningGame.setMaximumSize(new java.awt.Dimension(1000, 700));
         startPlanningGame.setMinimumSize(new java.awt.Dimension(1000, 700));
         startPlanningGame.setName(""); // NOI18N
         startPlanningGame.setPreferredSize(new java.awt.Dimension(1000, 700));
         startPlanningGame.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         panel_Profile.setMaximumSize(new java.awt.Dimension(500, 700));
         panel_Profile.setMinimumSize(new java.awt.Dimension(500, 700));
         panel_Profile.setPreferredSize(new java.awt.Dimension(500, 700));
         panel_Profile.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         textfield_pKontoname.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_pKontonameActionPerformed(evt);
             }
         });
         textfield_pKontoname.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_pKontonameFocusGained(evt);
             }
         });
         panel_Profile.add(textfield_pKontoname, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 230, 149, -1));
 
         textfield_pEmail1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_pEmail1ActionPerformed(evt);
             }
         });
         textfield_pEmail1.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_pEmail1FocusGained(evt);
             }
         });
         panel_Profile.add(textfield_pEmail1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 320, 149, -1));
 
         textfield_pEmail2.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_pEmail2FocusGained(evt);
             }
         });
         panel_Profile.add(textfield_pEmail2, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 350, 149, -1));
 
         textfield_pName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_pNameActionPerformed(evt);
             }
         });
         textfield_pName.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_pNameFocusGained(evt);
             }
         });
         panel_Profile.add(textfield_pName, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 380, 149, -1));
 
         textfield_pSurename.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_pSurenameFocusGained(evt);
             }
         });
         panel_Profile.add(textfield_pSurename, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 410, 149, -1));
 
         password_pPass1.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 password_pPass1FocusGained(evt);
             }
         });
         panel_Profile.add(password_pPass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 260, 149, -1));
 
         password_pPass2.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 password_pPass2FocusGained(evt);
             }
         });
         panel_Profile.add(password_pPass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 290, 149, -1));
 
         button_pSave.setText("Übernehmen");
         button_pSave.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_pSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_pSaveActionPerformed(evt);
             }
         });
         panel_Profile.add(button_pSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 490, -1, -1));
 
         label_pKontoname.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_pKontoname.setText("Kontoname:");
         panel_Profile.add(label_pKontoname, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 230, -1, 20));
 
         label_pPass1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_pPass1.setText("Passwort:");
         panel_Profile.add(label_pPass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 260, -1, -1));
 
         label_pPass2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_pPass2.setText("Passwort wiederholen:");
         panel_Profile.add(label_pPass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 290, -1, -1));
 
         label_pEmail1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_pEmail1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         label_pEmail1.setText("Email:");
         panel_Profile.add(label_pEmail1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 320, -1, -1));
 
         label_pEmail2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_pEmail2.setText("Email wiederholen:");
         panel_Profile.add(label_pEmail2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 350, -1, -1));
 
         label_pFirstName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_pFirstName.setText("Vorname:");
         panel_Profile.add(label_pFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 380, -1, -1));
 
         label_pSecondName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_pSecondName.setText("Nachname:");
         panel_Profile.add(label_pSecondName, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 410, -1, -1));
 
         button_pCancel.setText("Abbrechen");
         button_pCancel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_pCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_pCancelActionPerformed(evt);
             }
         });
         panel_Profile.add(button_pCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 490, -1, -1));
 
         label_pErrAccname.setForeground(new java.awt.Color(204, 0, 0));
         label_pErrAccname.setText("Fehler?");
         label_pErrAccname.setName("label_rErrAccname"); // NOI18N
         panel_Profile.add(label_pErrAccname, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 230, -1, -1));
 
         label_pErrPass1.setForeground(new java.awt.Color(204, 0, 0));
         label_pErrPass1.setText("Fehler?");
         label_pErrPass1.setName("label_rErrPass1"); // NOI18N
         panel_Profile.add(label_pErrPass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 260, -1, -1));
 
         label_pErrPass2.setForeground(new java.awt.Color(204, 0, 0));
         label_pErrPass2.setText("Fehler?");
         panel_Profile.add(label_pErrPass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 290, -1, -1));
 
         label_pErrEmail1.setForeground(new java.awt.Color(204, 0, 0));
         label_pErrEmail1.setText("Fehler?");
         panel_Profile.add(label_pErrEmail1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 320, -1, -1));
 
         label_pErrEmail2.setForeground(new java.awt.Color(204, 0, 0));
         label_pErrEmail2.setText("Fehler?");
         panel_Profile.add(label_pErrEmail2, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 350, -1, -1));
 
         label_pErrFirstName.setForeground(new java.awt.Color(204, 0, 0));
         label_pErrFirstName.setText("Fehler?");
         panel_Profile.add(label_pErrFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 380, -1, -1));
 
         label_pErrLastName.setForeground(new java.awt.Color(204, 0, 0));
         label_pErrLastName.setText("Fehler?");
         panel_Profile.add(label_pErrLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 410, -1, -1));
 
         Menu_overlay1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pictures/mainmenue_new.png"))); // NOI18N
         Menu_overlay1.setText("Overlay_hauptmenü");
         Menu_overlay1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         panel_Profile.add(Menu_overlay1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
 
         startPlanningGame.add(panel_Profile, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 590, 870));
 
         noSave_overlay.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         noSave_overlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pictures/keinspielstand_00000.png"))); // NOI18N
         startPlanningGame.add(noSave_overlay, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 0, -1, 400));
 
         loggedinas.setFont(new java.awt.Font("Modern No. 20", 1, 24)); // NOI18N
         loggedinas.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         startPlanningGame.add(loggedinas, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 540, 420, 30));
 
         Menu_overlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pictures/mainmenue_new.png"))); // NOI18N
         Menu_overlay.setText("Overlay_hauptmenü");
         Menu_overlay.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         startPlanningGame.add(Menu_overlay, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
 
         button_menuStartNewGame.setText("Neues Spiel");
         button_menuStartNewGame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_menuStartNewGame.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_menuStartNewGameActionPerformed(evt);
             }
         });
         startPlanningGame.add(button_menuStartNewGame, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 200, 160, 40));
 
         button_menuExit.setText("Beenden");
         button_menuExit.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_menuExit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_menuExitActionPerformed(evt);
             }
         });
         startPlanningGame.add(button_menuExit, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 440, 160, 40));
 
         button_menuLoadGame.setText("Spiel Laden");
         button_menuLoadGame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_menuLoadGame.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_menuLoadGameActionPerformed(evt);
             }
         });
         startPlanningGame.add(button_menuLoadGame, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 240, 210, 40));
 
         button_menuCredits.setText("Credits");
         button_menuCredits.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_menuCredits.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_menuCreditsActionPerformed(evt);
             }
         });
         startPlanningGame.add(button_menuCredits, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 360, 160, 40));
 
         button_menuProfile.setText("Profil");
         button_menuProfile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_menuProfile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_menuProfileActionPerformed(evt);
             }
         });
         startPlanningGame.add(button_menuProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 280, 160, 40));
 
         button_menuLogOut.setText("Abmelden");
         button_menuLogOut.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_menuLogOut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_menuLogOutActionPerformed(evt);
             }
         });
         startPlanningGame.add(button_menuLogOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 400, 160, 40));
 
         button_menuStatistic.setText("Statistik");
         button_menuStatistic.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_menuStatistic.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_menuStatisticActionPerformed(evt);
             }
         });
         startPlanningGame.add(button_menuStatistic, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 320, 160, 40));
 
         jPanel1.add(startPlanningGame, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1000, 700));
 
         jPanel2.setLayout(new java.awt.CardLayout());
 
         gamePlanning.setName("card3"); // NOI18N
         gamePlanning.setLayout(null);
 
         Logo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         Logo.setLayout(null);
 
         jLab_Logo.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
         jLab_Logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLab_Logo.setText("SIMS Logo");
         jLab_Logo.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLab_LogoMouseClicked(evt);
             }
         });
         Logo.add(jLab_Logo);
         jLab_Logo.setBounds(0, 0, 150, 110);
 
         gamePlanning.add(Logo);
         Logo.setBounds(0, 0, 150, 110);
 
         Header.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         Header.setLayout(null);
 
         jLab_Planning_unused01.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLab_Planning_unused01.setText("Wissen:");
         Header.add(jLab_Planning_unused01);
         jLab_Planning_unused01.setBounds(10, 20, 70, 20);
 
         jLab_Planning_unused02.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLab_Planning_unused02.setText("Motivation:");
         Header.add(jLab_Planning_unused02);
         jLab_Planning_unused02.setBounds(10, 40, 80, 30);
 
         jLab_Planning_unused03.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLab_Planning_unused03.setText("Müdigkeit:");
         Header.add(jLab_Planning_unused03);
         jLab_Planning_unused03.setBounds(10, 80, 90, 20);
         Header.add(jProgB_Müdigkeit);
         jProgB_Müdigkeit.setBounds(90, 80, 260, 20);
         Header.add(jProgB_Motivation);
         jProgB_Motivation.setBounds(90, 50, 260, 20);
         Header.add(jProgB_Wissen);
         jProgB_Wissen.setBounds(90, 20, 260, 20);
 
         jLab_Planning_unused04.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
         jLab_Planning_unused04.setText("PLANUNGSPHASE");
         Header.add(jLab_Planning_unused04);
         jLab_Planning_unused04.setBounds(360, 10, 340, 90);
 
         gamePlanning.add(Header);
         Header.setBounds(150, 0, 700, 110);
 
         Shop.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         Shop.setLayout(null);
 
         jBut_startShop.setFont(new java.awt.Font("Lucida Grande", 1, 24)); // NOI18N
         jBut_startShop.setText("SHOP");
         jBut_startShop.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_startShopMouseClicked(evt);
             }
         });
         jBut_startShop.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jBut_startShopActionPerformed(evt);
             }
         });
         Shop.add(jBut_startShop);
         jBut_startShop.setBounds(5, 5, 120, 100);
 
         gamePlanning.add(Shop);
         Shop.setBounds(850, 0, 130, 110);
 
         Navi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         Navi.setLayout(null);
 
         jPan_StudSwitch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPan_StudSwitch.setLayout(null);
         jPan_StudSwitch.add(jLab_StudSwitch);
         jLab_StudSwitch.setBounds(0, 0, 150, 110);
 
         jLab_Planning_unused05.setText("umsetzen");
         jPan_StudSwitch.add(jLab_Planning_unused05);
         jLab_Planning_unused05.setBounds(40, 40, 70, 14);
 
         jLab_Planning_unused06.setText("Studenten");
         jPan_StudSwitch.add(jLab_Planning_unused06);
         jLab_Planning_unused06.setBounds(40, 20, 70, 20);
 
         jLab_StudCounter.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
         jLab_StudCounter.setText("5x");
         jPan_StudSwitch.add(jLab_StudCounter);
         jLab_StudCounter.setBounds(60, 60, 30, 24);
 
         jToggleBut_SwitchStud.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jToggleBut_SwitchStudMouseClicked(evt);
             }
         });
         jPan_StudSwitch.add(jToggleBut_SwitchStud);
         jToggleBut_SwitchStud.setBounds(10, 10, 130, 90);
 
         Navi.add(jPan_StudSwitch);
         jPan_StudSwitch.setBounds(0, 0, 150, 110);
         jPan_StudSwitch.getAccessibleContext().setAccessibleDescription("");
 
         jPan_DozSwitch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPan_DozSwitch.setLayout(null);
         jPan_DozSwitch.add(jLab_DozSwitch);
         jLab_DozSwitch.setBounds(0, 0, 150, 110);
 
         jLab_Planning_unused07.setText("Dozenten");
         jPan_DozSwitch.add(jLab_Planning_unused07);
         jLab_Planning_unused07.setBounds(40, 20, 70, 20);
 
         jLab_Planning_unused08.setText("tauschen");
         jPan_DozSwitch.add(jLab_Planning_unused08);
         jLab_Planning_unused08.setBounds(40, 40, 60, 20);
 
         jLab_DozCounter.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
         jLab_DozCounter.setText("1x");
         jPan_DozSwitch.add(jLab_DozCounter);
         jLab_DozCounter.setBounds(60, 60, 30, 30);
 
         jBut_ChangeLector.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
         jBut_ChangeLector.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_ChangeLectorMouseClicked(evt);
             }
         });
         jBut_ChangeLector.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jBut_ChangeLectorActionPerformed(evt);
             }
         });
         jPan_DozSwitch.add(jBut_ChangeLector);
         jBut_ChangeLector.setBounds(10, 10, 130, 90);
 
         Navi.add(jPan_DozSwitch);
         jPan_DozSwitch.setBounds(0, 110, 150, 110);
 
         jPan_ItemSelect.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPan_ItemSelect.setLayout(null);
 
         jComboB_Items.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "wähle Item", "Spicker" }));
         jComboB_Items.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jComboB_ItemsMouseClicked(evt);
             }
         });
         jComboB_Items.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboB_ItemsActionPerformed(evt);
             }
         });
         jPan_ItemSelect.add(jComboB_Items);
         jComboB_Items.setBounds(10, 20, 130, 30);
 
         jBut_ComboB_useItem.setText("Benutzen");
         jPan_ItemSelect.add(jBut_ComboB_useItem);
         jBut_ComboB_useItem.setBounds(10, 60, 120, 23);
 
         Navi.add(jPan_ItemSelect);
         jPan_ItemSelect.setBounds(0, 220, 150, 110);
 
         jPan_ItemStorage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPan_ItemStorage.setLayout(null);
 
         jLab_Planning_unused09.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
         jLab_Planning_unused09.setText("Item-Vorräte");
         jPan_ItemStorage.add(jLab_Planning_unused09);
         jLab_Planning_unused09.setBounds(10, 10, 120, 20);
 
         jLab_Redbull.setText("");
         jPan_ItemStorage.add(jLab_Redbull);
         jLab_Redbull.setBounds(10, 40, 90, 14);
 
         jLab_Duplo.setText("mehr text");
         jPan_ItemStorage.add(jLab_Duplo);
         jLab_Duplo.setBounds(10, 60, 90, 14);
 
         jLab_OMNI.setText("noch mehr text" );
         jPan_ItemStorage.add(jLab_OMNI);
         jLab_OMNI.setBounds(10, 80, 140, 14);
 
         Navi.add(jPan_ItemStorage);
         jPan_ItemStorage.setBounds(0, 330, 150, 110);
 
         jPan_Planning_Play.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPan_Planning_Play.setLayout(null);
 
         jBut_Play.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
         jBut_Play.setText("SPIELEN");
         jBut_Play.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_PlayMouseClicked(evt);
             }
         });
         jBut_Play.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jBut_PlayActionPerformed(evt);
             }
         });
         jPan_Planning_Play.add(jBut_Play);
         jBut_Play.setBounds(10, 10, 130, 90);
 
         Navi.add(jPan_Planning_Play);
         jPan_Planning_Play.setBounds(0, 440, 150, 110);
 
         gamePlanning.add(Navi);
         Navi.setBounds(0, 110, 150, 550);
 
         StudField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         StudField.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 StudFieldMouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 StudFieldMouseEntered(evt);
             }
         });
         StudField.setLayout(null);
 
         jBut_Dozent.setText("Dozent");
         jBut_Dozent.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_DozentMouseClicked(evt);
             }
         });
         StudField.add(jBut_Dozent);
         jBut_Dozent.setBounds(340, 470, 110, 50);
 
         jBut_1.setText("Platz_1");
         jBut_1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_1MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jBut_1MouseExited(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 jBut_1MouseEntered(evt);
             }
         });
         jBut_1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jBut_1ActionPerformed(evt);
             }
         });
         StudField.add(jBut_1);
         jBut_1.setBounds(60, 10, 110, 50);
 
         jBut_2.setText("Platz_2");
         jBut_2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_2MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 jBut_2MouseEntered(evt);
             }
         });
         StudField.add(jBut_2);
         jBut_2.setBounds(210, 10, 110, 50);
 
         jBut_3.setText("Platz_3");
         jBut_3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_3MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 jBut_3MouseEntered(evt);
             }
         });
         StudField.add(jBut_3);
         jBut_3.setBounds(360, 10, 110, 50);
 
         jBut_4.setText("Platz_4");
         jBut_4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_4MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 jBut_4MouseEntered(evt);
             }
         });
         StudField.add(jBut_4);
         jBut_4.setBounds(510, 10, 110, 50);
 
         jBut_5.setText("Platz_5");
         jBut_5.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_5MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 jBut_5MouseEntered(evt);
             }
         });
         StudField.add(jBut_5);
         jBut_5.setBounds(660, 10, 110, 50);
 
         jBut_6.setText("Platz_6");
         jBut_6.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_6MouseClicked(evt);
             }
         });
         StudField.add(jBut_6);
         jBut_6.setBounds(60, 80, 110, 50);
 
         jBut_7.setText("Platz_7");
         jBut_7.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_7MouseClicked(evt);
             }
         });
         StudField.add(jBut_7);
         jBut_7.setBounds(210, 80, 110, 50);
 
         jBut_8.setText("Platz_8");
         jBut_8.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_8MouseClicked(evt);
             }
         });
         StudField.add(jBut_8);
         jBut_8.setBounds(360, 80, 110, 50);
 
         jBut_9.setText("Platz_9");
         jBut_9.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_9MouseClicked(evt);
             }
         });
         StudField.add(jBut_9);
         jBut_9.setBounds(510, 80, 110, 50);
 
         jBut_10.setText("Platz_10");
         jBut_10.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_10MouseClicked(evt);
             }
         });
         StudField.add(jBut_10);
         jBut_10.setBounds(660, 80, 110, 50);
 
         jBut_11.setText("Platz_11");
         jBut_11.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_11MouseClicked(evt);
             }
         });
         StudField.add(jBut_11);
         jBut_11.setBounds(60, 150, 110, 50);
 
         jBut_12.setText("Platz_12");
         jBut_12.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_12MouseClicked(evt);
             }
         });
         StudField.add(jBut_12);
         jBut_12.setBounds(210, 150, 110, 50);
 
         jBut_13.setText("Platz_13");
         jBut_13.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_13MouseClicked(evt);
             }
         });
         StudField.add(jBut_13);
         jBut_13.setBounds(360, 150, 110, 50);
 
         jBut_14.setText("Platz_14");
         jBut_14.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_14MouseClicked(evt);
             }
         });
         StudField.add(jBut_14);
         jBut_14.setBounds(510, 150, 110, 50);
 
         jBut_15.setText("Platz_15");
         jBut_15.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_15MouseClicked(evt);
             }
         });
         StudField.add(jBut_15);
         jBut_15.setBounds(660, 150, 110, 50);
 
         jBut_16.setText("Platz_16");
         jBut_16.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_16MouseClicked(evt);
             }
         });
         StudField.add(jBut_16);
         jBut_16.setBounds(60, 220, 110, 50);
 
         jBut_17.setText("Platz_17");
         jBut_17.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_17MouseClicked(evt);
             }
         });
         jBut_17.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jBut_17ActionPerformed(evt);
             }
         });
         StudField.add(jBut_17);
         jBut_17.setBounds(210, 220, 110, 50);
 
         jBut_18.setText("Platz_18");
         jBut_18.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_18MouseClicked(evt);
             }
         });
         StudField.add(jBut_18);
         jBut_18.setBounds(360, 220, 110, 50);
 
         jBut_19.setText("Platz_19");
         jBut_19.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_19MouseClicked(evt);
             }
         });
         StudField.add(jBut_19);
         jBut_19.setBounds(510, 220, 110, 50);
 
         jBut_20.setText("Platz_20");
         jBut_20.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_20MouseClicked(evt);
             }
         });
         StudField.add(jBut_20);
         jBut_20.setBounds(660, 220, 110, 50);
 
         jBut_21.setText("Platz_21");
         jBut_21.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_21MouseClicked(evt);
             }
         });
         StudField.add(jBut_21);
         jBut_21.setBounds(60, 290, 110, 50);
 
         jBut_22.setText("Platz_22");
         jBut_22.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_22MouseClicked(evt);
             }
         });
         StudField.add(jBut_22);
         jBut_22.setBounds(210, 290, 110, 50);
 
         jBut_23.setText("Platz_23");
         jBut_23.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_23MouseClicked(evt);
             }
         });
         StudField.add(jBut_23);
         jBut_23.setBounds(360, 290, 110, 50);
 
         jBut_24.setText("Platz_24");
         jBut_24.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_24MouseClicked(evt);
             }
         });
         StudField.add(jBut_24);
         jBut_24.setBounds(510, 290, 110, 50);
 
         jBut_25.setText("Platz_25");
         jBut_25.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_25MouseClicked(evt);
             }
         });
         StudField.add(jBut_25);
         jBut_25.setBounds(660, 290, 110, 50);
 
         jBut_26.setText("Platz_26");
         jBut_26.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_26MouseClicked(evt);
             }
         });
         StudField.add(jBut_26);
         jBut_26.setBounds(60, 360, 110, 50);
 
         jBut_27.setText("Platz_27");
         jBut_27.setToolTipText("");
         jBut_27.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_27MouseClicked(evt);
             }
         });
         StudField.add(jBut_27);
         jBut_27.setBounds(210, 360, 110, 50);
 
         jBut_28.setText("Platz_28");
         jBut_28.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_28MouseClicked(evt);
             }
         });
         StudField.add(jBut_28);
         jBut_28.setBounds(360, 360, 110, 50);
 
         jBut_29.setText("Platz_29");
         jBut_29.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_29MouseClicked(evt);
             }
         });
         StudField.add(jBut_29);
         jBut_29.setBounds(510, 360, 110, 50);
 
         jBut_30.setText("Platz_30");
         jBut_30.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jBut_30MouseClicked(evt);
             }
         });
         StudField.add(jBut_30);
         jBut_30.setBounds(660, 360, 110, 50);
         StudField.add(jLab_DozSwitch1);
         jLab_DozSwitch1.setBounds(0, 0, 150, 110);
 
         gamePlanning.add(StudField);
         StudField.setBounds(150, 110, 830, 550);
 
         jPanel2.add(gamePlanning, "card3");
 
         shop.setLayout(null);
 
         jPanel18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel18.setLayout(null);
 
         jLabel55.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
         jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel55.setText("SIMS Logo");
         jLabel55.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel55MouseClicked(evt);
             }
         });
         jPanel18.add(jLabel55);
         jLabel55.setBounds(0, 0, 150, 110);
 
         shop.add(jPanel18);
         jPanel18.setBounds(0, 0, 150, 110);
 
         jPanel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel19.setLayout(null);
 
         label_inventar.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
         label_inventar.setText("Inventar");
         jPanel19.add(label_inventar);
         label_inventar.setBounds(30, 0, 120, 40);
 
         jLabel58.setText("Studenten:");
         jPanel19.add(jLabel58);
         jLabel58.setBounds(10, 450, 80, 14);
 
         jLabel59.setText("4 / 40");
         jPanel19.add(jLabel59);
         jLabel59.setBounds(80, 450, 50, 14);
 
         jLabel60.setText("Semester: ");
         jPanel19.add(jLabel60);
         jLabel60.setBounds(10, 470, 80, 14);
 
         jLabel61.setText("3 / 6");
         jPanel19.add(jLabel61);
         jLabel61.setBounds(80, 470, 60, 14);
 
         label_item4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel19.add(label_item4);
         label_item4.setBounds(30, 350, 90, 80);
 
         label_item1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel19.add(label_item1);
         label_item1.setBounds(30, 50, 90, 80);
 
         label_item2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_item2.setPreferredSize(new java.awt.Dimension(20, 20));
         jPanel19.add(label_item2);
         label_item2.setBounds(30, 150, 90, 80);
 
         label_item1Name.setText("jLabel4");
         jPanel19.add(label_item1Name);
         label_item1Name.setBounds(60, 80, 34, 14);
 
         label_item1Amount.setText("jLabel4");
         jPanel19.add(label_item1Amount);
         label_item1Amount.setBounds(60, 100, 34, 14);
 
         label_item2Name.setText("jLabel4");
         jPanel19.add(label_item2Name);
         label_item2Name.setBounds(60, 190, 34, 14);
 
         label_item2Amount.setText("jLabel4");
         jPanel19.add(label_item2Amount);
         label_item2Amount.setBounds(60, 210, 34, 14);
 
         label_item3Name.setText("jLabel4");
         jPanel19.add(label_item3Name);
         label_item3Name.setBounds(60, 380, 34, 14);
 
         label_item3Amount.setText("jLabel26");
         jPanel19.add(label_item3Amount);
         label_item3Amount.setBounds(60, 400, 40, 14);
 
         label_item3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel19.add(label_item3);
         label_item3.setBounds(30, 250, 90, 80);
 
         label_item4Name.setText("jLabel4");
         jPanel19.add(label_item4Name);
         label_item4Name.setBounds(60, 280, 34, 14);
 
         label_item4Amount.setText("jLabel26");
         jPanel19.add(label_item4Amount);
         label_item4Amount.setBounds(60, 300, 40, 14);
 
         shop.add(jPanel19);
         jPanel19.setBounds(0, 110, 150, 550);
 
         jPanel20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel20.setLayout(null);
 
         ucoinsShop.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         ucoinsShop.setText("0");
         jPanel20.add(ucoinsShop);
         ucoinsShop.setBounds(80, 10, 80, 20);
 
         creditsShop.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         creditsShop.setText("0");
         jPanel20.add(creditsShop);
         creditsShop.setBounds(80, 40, 70, 17);
 
         punkteShop.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         punkteShop.setText("0");
         jPanel20.add(punkteShop);
         punkteShop.setBounds(80, 70, 70, 17);
 
         button_startExchange.setText("UCoins Tauschen");
         button_startExchange.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_startExchangeActionPerformed(evt);
             }
         });
         jPanel20.add(button_startExchange);
         button_startExchange.setBounds(230, 10, 160, 90);
 
         jLabel70.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel70.setText("UCoins:");
         jPanel20.add(jLabel70);
         jLabel70.setBounds(10, 10, 80, 20);
 
         jLabel71.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel71.setText("Credits:");
         jPanel20.add(jLabel71);
         jLabel71.setBounds(10, 40, 70, 17);
 
         jLabel72.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel72.setText("Punkte:");
         jPanel20.add(jLabel72);
         jLabel72.setBounds(10, 70, 70, 17);
 
         jLabel85.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         jLabel85.setText("SHOP");
         jPanel20.add(jLabel85);
         jLabel85.setBounds(470, 0, 210, 110);
 
         shop.add(jPanel20);
         jPanel20.setBounds(150, 0, 700, 110);
 
         jPanel21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel21.setLayout(null);
 
         label_returnToPlanningPhase.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
         label_returnToPlanningPhase.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_returnToPlanningPhase.setText("SPIEL");
         label_returnToPlanningPhase.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_returnToPlanningPhaseMouseClicked(evt);
             }
         });
         jPanel21.add(label_returnToPlanningPhase);
         label_returnToPlanningPhase.setBounds(0, 0, 130, 110);
 
         shop.add(jPanel21);
         jPanel21.setBounds(850, 0, 130, 110);
 
         jPanel22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel22.setLayout(null);
 
         label_cheatSheetOverlay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_cheatSheetOverlay.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_cheatSheetOverlayMouseClicked(evt);
             }
         });
         jPanel22.add(label_cheatSheetOverlay);
         label_cheatSheetOverlay.setBounds(100, 300, 230, 220);
 
         label_omniOverlay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_omniOverlay.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_omniOverlayMouseClicked(evt);
             }
         });
         jPanel22.add(label_omniOverlay);
         label_omniOverlay.setBounds(510, 300, 230, 220);
 
         label_duploOverlay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_duploOverlay.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_duploOverlayMouseClicked(evt);
             }
         });
         jPanel22.add(label_duploOverlay);
         label_duploOverlay.setBounds(510, 40, 230, 220);
 
         label_redBullOverlay.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_redBullOverlay.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_redBullOverlayMouseClicked(evt);
             }
         });
         jPanel22.add(label_redBullOverlay);
         label_redBullOverlay.setBounds(100, 40, 230, 220);
 
         panel_redBull.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         panel_redBull.setName("panel_redBull"); // NOI18N
         panel_redBull.setLayout(null);
 
         label_redBullName.setBackground(new java.awt.Color(0, 0, 0));
         label_redBullName.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
         label_redBullName.setForeground(new java.awt.Color(255, 255, 255));
         label_redBullName.setText("Red Bull");
         label_redBullName.setOpaque(true);
         panel_redBull.add(label_redBullName);
         label_redBullName.setBounds(0, 130, 230, 30);
 
         label_redBullAmount.setBackground(new java.awt.Color(0, 0, 0));
         label_redBullAmount.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
         label_redBullAmount.setForeground(new java.awt.Color(255, 255, 255));
         label_redBullAmount.setText("100 Credits");
         label_redBullAmount.setOpaque(true);
         panel_redBull.add(label_redBullAmount);
         label_redBullAmount.setBounds(0, 160, 230, 30);
 
         label_redBullLocked.setBackground(new java.awt.Color(0, 0, 0));
         label_redBullLocked.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
         label_redBullLocked.setForeground(new java.awt.Color(204, 51, 0));
         label_redBullLocked.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_redBullLocked.setText("GESPERRT");
         label_redBullLocked.setOpaque(true);
         panel_redBull.add(label_redBullLocked);
         label_redBullLocked.setBounds(0, 50, 230, 50);
 
         label_redBullImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/items/RedBullShop230x220.png"))); // NOI18N
         label_redBullImage.setText("jLabel12");
         panel_redBull.add(label_redBullImage);
         label_redBullImage.setBounds(0, 0, 230, 220);
 
         jPanel22.add(panel_redBull);
         panel_redBull.setBounds(100, 40, 230, 220);
 
         panel_omniSense.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         panel_omniSense.setLayout(null);
 
         label_omniName.setBackground(new java.awt.Color(0, 0, 0));
         label_omniName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_omniName.setForeground(new java.awt.Color(255, 255, 255));
         label_omniName.setText("Verfügbar ab Semester 4");
         label_omniName.setOpaque(true);
         panel_omniSense.add(label_omniName);
         label_omniName.setBounds(0, 130, 230, 30);
 
         label_omniLocked.setBackground(new java.awt.Color(0, 0, 0));
         label_omniLocked.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
         label_omniLocked.setForeground(new java.awt.Color(204, 51, 0));
         label_omniLocked.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_omniLocked.setText("GESPERRT");
         label_omniLocked.setOpaque(true);
         panel_omniSense.add(label_omniLocked);
         label_omniLocked.setBounds(0, 50, 230, 50);
 
         label_omniAmount.setBackground(new java.awt.Color(0, 0, 0));
         label_omniAmount.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_omniAmount.setForeground(new java.awt.Color(255, 255, 255));
         label_omniAmount.setText("5 UCoins");
         label_omniAmount.setOpaque(true);
         panel_omniSense.add(label_omniAmount);
         label_omniAmount.setBounds(0, 160, 230, 30);
 
         label_omniAudioImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/items/OMNISenseAudioShop230x220.png"))); // NOI18N
         panel_omniSense.add(label_omniAudioImage);
         label_omniAudioImage.setBounds(0, 0, 230, 220);
 
         jPanel22.add(panel_omniSense);
         panel_omniSense.setBounds(510, 300, 230, 220);
 
         panel_duplo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         panel_duplo.setLayout(null);
 
         label_duploName.setBackground(new java.awt.Color(0, 0, 0));
         label_duploName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_duploName.setForeground(new java.awt.Color(255, 255, 255));
         label_duploName.setText("Duplo");
         label_duploName.setOpaque(true);
         panel_duplo.add(label_duploName);
         label_duploName.setBounds(0, 130, 230, 30);
 
         label_duploAmount.setBackground(new java.awt.Color(0, 0, 0));
         label_duploAmount.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_duploAmount.setForeground(new java.awt.Color(255, 255, 255));
         label_duploAmount.setText("100 Credits");
         label_duploAmount.setOpaque(true);
         panel_duplo.add(label_duploAmount);
         label_duploAmount.setBounds(0, 160, 230, 30);
 
         label_duploLocked.setBackground(new java.awt.Color(0, 0, 0));
         label_duploLocked.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
         label_duploLocked.setForeground(new java.awt.Color(204, 51, 0));
         label_duploLocked.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_duploLocked.setText("GESPERRT");
         label_duploLocked.setOpaque(true);
         panel_duplo.add(label_duploLocked);
         label_duploLocked.setBounds(0, 50, 230, 50);
 
         label_duploImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/items/FerreroDuploShop230x220.png"))); // NOI18N
         panel_duplo.add(label_duploImage);
         label_duploImage.setBounds(0, 0, 230, 220);
 
         jPanel22.add(panel_duplo);
         panel_duplo.setBounds(510, 40, 230, 220);
 
         panel_cheatSheet.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         panel_cheatSheet.setLayout(null);
 
         label_cheatSheetName.setBackground(new java.awt.Color(0, 0, 0));
         label_cheatSheetName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_cheatSheetName.setForeground(new java.awt.Color(255, 255, 255));
         label_cheatSheetName.setText("Spicker");
         label_cheatSheetName.setOpaque(true);
         panel_cheatSheet.add(label_cheatSheetName);
         label_cheatSheetName.setBounds(0, 130, 230, 30);
 
         label_cheatSheetLocked.setBackground(new java.awt.Color(0, 0, 0));
         label_cheatSheetLocked.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
         label_cheatSheetLocked.setForeground(new java.awt.Color(204, 51, 0));
         label_cheatSheetLocked.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         label_cheatSheetLocked.setText("GESPERRT");
         label_cheatSheetLocked.setOpaque(true);
         panel_cheatSheet.add(label_cheatSheetLocked);
         label_cheatSheetLocked.setBounds(0, 50, 230, 50);
 
         label_cheatSheetAmount.setBackground(new java.awt.Color(0, 0, 0));
         label_cheatSheetAmount.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         label_cheatSheetAmount.setForeground(new java.awt.Color(255, 255, 255));
         label_cheatSheetAmount.setText("5 UCoins");
         label_cheatSheetAmount.setOpaque(true);
         panel_cheatSheet.add(label_cheatSheetAmount);
         label_cheatSheetAmount.setBounds(0, 160, 230, 30);
 
         label_cheatSheetImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/items/SpickzettelShop230x220.png"))); // NOI18N
         panel_cheatSheet.add(label_cheatSheetImage);
         label_cheatSheetImage.setBounds(0, 0, 230, 220);
 
         jPanel22.add(panel_cheatSheet);
         panel_cheatSheet.setBounds(100, 300, 230, 220);
 
         shop.add(jPanel22);
         jPanel22.setBounds(150, 110, 830, 550);
 
         jPanel2.add(shop, "card4");
 
         gamePlaying.setMinimumSize(new java.awt.Dimension(1024, 768));
         gamePlaying.setPreferredSize(new java.awt.Dimension(1024, 768));
         gamePlaying.setLayout(null);
 
         jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel3.setLayout(null);
 
         jLabel2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
         jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel2.setText("SIMS Logo");
         jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel2MouseClicked(evt);
             }
         });
         jPanel3.add(jLabel2);
         jLabel2.setBounds(0, 0, 150, 110);
 
         gamePlaying.add(jPanel3);
         jPanel3.setBounds(0, 0, 150, 110);
 
         jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel4.setLayout(null);
 
         jLabel11.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
         jLabel11.setText("Inventar");
         jPanel4.add(jLabel11);
         jLabel11.setBounds(30, 0, 120, 40);
 
         label_item3Inv.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_item3Inv.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_item3InvMouseClicked(evt);
             }
         });
         jPanel4.add(label_item3Inv);
         label_item3Inv.setBounds(30, 250, 80, 80);
 
         jLabel18.setText("Studenten:");
         jPanel4.add(jLabel18);
         jLabel18.setBounds(10, 450, 80, 14);
 
         jLabel19.setText("4 / 40");
         jPanel4.add(jLabel19);
         jLabel19.setBounds(80, 450, 50, 14);
 
         jLabel20.setText("Semester: ");
         jPanel4.add(jLabel20);
         jLabel20.setBounds(10, 470, 80, 14);
 
         jLabel21.setText("3 / 6");
         jPanel4.add(jLabel21);
         jLabel21.setBounds(80, 470, 60, 14);
 
         label_ucoinsInv.setText("UCoins: 300");
         jPanel4.add(label_ucoinsInv);
         label_ucoinsInv.setBounds(10, 380, 130, 14);
 
         label_item2Inv.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_item2Inv.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_item2InvMouseClicked(evt);
             }
         });
         jPanel4.add(label_item2Inv);
         label_item2Inv.setBounds(30, 150, 80, 80);
 
         label_creditsInv.setText("Credits: 700");
         jPanel4.add(label_creditsInv);
         label_creditsInv.setBounds(10, 360, 140, 14);
 
         label_item3InvName.setText("Red Bull");
         jPanel4.add(label_item3InvName);
         label_item3InvName.setBounds(30, 260, 80, 30);
 
         label_item3InvAmount.setText("3 x");
         jPanel4.add(label_item3InvAmount);
         label_item3InvAmount.setBounds(30, 300, 80, 14);
 
         label_item1Inv.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         label_item1Inv.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 label_item1InvMouseClicked(evt);
             }
         });
         jPanel4.add(label_item1Inv);
         label_item1Inv.setBounds(30, 50, 80, 80);
 
         jLabel13.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         jLabel13.setText("Punkte:");
         jPanel4.add(jLabel13);
         jLabel13.setBounds(10, 510, 70, 20);
 
         jLabel14.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
         jLabel14.setText("700");
         jPanel4.add(jLabel14);
         jLabel14.setBounds(90, 500, 40, 40);
 
         label_item1InvName.setText("Red Bull");
         jPanel4.add(label_item1InvName);
         label_item1InvName.setBounds(30, 80, 80, 30);
 
         label_item2InvName.setText("Red Bull");
         jPanel4.add(label_item2InvName);
         label_item2InvName.setBounds(30, 160, 80, 30);
 
         label_item2InvAmount.setText("3 x");
         jPanel4.add(label_item2InvAmount);
         label_item2InvAmount.setBounds(30, 190, 80, 14);
 
         label_item1InvAmount.setText("3 x");
         jPanel4.add(label_item1InvAmount);
         label_item1InvAmount.setBounds(30, 110, 80, 14);
 
         gamePlaying.add(jPanel4);
         jPanel4.setBounds(0, 110, 150, 550);
 
         jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel5.setLayout(null);
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLabel5.setText("Wissen:");
         jPanel5.add(jLabel5);
         jLabel5.setBounds(10, 10, 70, 20);
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLabel6.setText("Motivation:");
         jPanel5.add(jLabel6);
         jLabel6.setBounds(10, 30, 80, 40);
 
         jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLabel7.setText("Müdigkeit:");
         jPanel5.add(jLabel7);
         jLabel7.setBounds(10, 80, 80, 20);
 
         KnowledgeBar.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 KnowledgeBarMouseClicked(evt);
             }
         });
         jPanel5.add(KnowledgeBar);
         KnowledgeBar.setBounds(80, 10, 260, 20);
         KnowledgeBar.getAccessibleContext().setAccessibleDescription("");
 
         MotivationBar.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 MotivationBarMouseClicked(evt);
             }
         });
         jPanel5.add(MotivationBar);
         MotivationBar.setBounds(80, 40, 260, 20);
 
         TirednessBar.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 TirednessBarMouseClicked(evt);
             }
         });
         jPanel5.add(TirednessBar);
         TirednessBar.setBounds(80, 80, 260, 20);
 
         jButton1.setText("Fenster : AUF");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
         jPanel5.add(jButton1);
         jButton1.setBounds(350, 10, 120, 23);
 
         jButton4.setText("Gruppenarbeit");
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
         jPanel5.add(jButton4);
         jButton4.setBounds(350, 40, 120, 23);
 
         jButton5.setText("Pause");
         jPanel5.add(jButton5);
         jButton5.setBounds(350, 80, 120, 23);
 
         jLabel9.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLabel9.setText("Lärmpegel:");
         jPanel5.add(jLabel9);
         jLabel9.setBounds(480, 10, 80, 20);
 
         jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLabel10.setText("Luftqualität:");
         jPanel5.add(jLabel10);
         jLabel10.setBounds(480, 34, 80, 30);
         jPanel5.add(jProgressBar5);
         jProgressBar5.setBounds(560, 10, 130, 20);
         jPanel5.add(jProgressBar6);
         jProgressBar6.setBounds(560, 40, 130, 20);
 
         gamePlaying.add(jPanel5);
         jPanel5.setBounds(150, 0, 700, 110);
 
         jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         jPanel6.setLayout(null);
 
         label_timer.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         label_timer.setText("--:--");
         jPanel6.add(label_timer);
         label_timer.setBounds(10, 10, 140, 90);
 
         gamePlaying.add(jPanel6);
         jPanel6.setBounds(850, 0, 130, 110);
 
         panel_activityPhaseStudField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         panel_activityPhaseStudField.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 panel_activityPhaseStudFieldMouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 panel_activityPhaseStudFieldMouseEntered(evt);
             }
         });
         panel_activityPhaseStudField.setLayout(null);
 
         jBut_Dozent1.setText("Dozent");
         panel_activityPhaseStudField.add(jBut_Dozent1);
         jBut_Dozent1.setBounds(340, 470, 110, 50);
 
         button_stud1.setText("Platz_1");
         button_stud1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud1MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 button_stud1MouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 button_stud1MouseExited(evt);
             }
         });
         button_stud1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_stud1ActionPerformed(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud1);
         button_stud1.setBounds(60, 10, 110, 50);
 
         button_stud2.setText("Platz_2");
         button_stud2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud2MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 button_stud2MouseEntered(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud2);
         button_stud2.setBounds(210, 10, 110, 50);
 
         button_stud3.setText("Platz_3");
         button_stud3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud3MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 button_stud3MouseEntered(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud3);
         button_stud3.setBounds(360, 10, 110, 50);
 
         button_stud4.setText("Platz_4");
         button_stud4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud4MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 button_stud4MouseEntered(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud4);
         button_stud4.setBounds(510, 10, 110, 50);
 
         button_stud5.setText("Platz_5");
         button_stud5.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud5MouseClicked(evt);
             }
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 button_stud5MouseEntered(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud5);
         button_stud5.setBounds(660, 10, 110, 50);
 
         button_stud6.setText("Platz_6");
         button_stud6.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud6MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud6);
         button_stud6.setBounds(60, 80, 110, 50);
 
         button_stud7.setText("Platz_7");
         button_stud7.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud7MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud7);
         button_stud7.setBounds(210, 80, 110, 50);
 
         button_stud8.setText("Platz_8");
         button_stud8.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud8MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud8);
         button_stud8.setBounds(360, 80, 110, 50);
 
         button_stud9.setText("Platz_9");
         button_stud9.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud9MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud9);
         button_stud9.setBounds(510, 80, 110, 50);
 
         button_stud10.setText("Platz_10");
         button_stud10.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud10MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud10);
         button_stud10.setBounds(660, 80, 110, 50);
 
         button_stud11.setText("Platz_11");
         button_stud11.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud11MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud11);
         button_stud11.setBounds(60, 150, 110, 50);
 
         button_stud12.setText("Platz_12");
         button_stud12.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud12MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud12);
         button_stud12.setBounds(210, 150, 110, 50);
 
         button_stud13.setText("Platz_13");
         button_stud13.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud13MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud13);
         button_stud13.setBounds(360, 150, 110, 50);
 
         button_stud14.setText("Platz_14");
         button_stud14.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud14MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud14);
         button_stud14.setBounds(510, 150, 110, 50);
 
         button_stud15.setText("Platz_15");
         button_stud15.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud15MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud15);
         button_stud15.setBounds(660, 150, 110, 50);
 
         button_stud16.setText("Platz_16");
         button_stud16.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud16MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud16);
         button_stud16.setBounds(60, 220, 110, 50);
 
         button_stud17.setText("Platz_17");
         button_stud17.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud17MouseClicked(evt);
             }
         });
         button_stud17.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_stud17ActionPerformed(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud17);
         button_stud17.setBounds(210, 220, 110, 50);
 
         button_stud18.setText("Platz_18");
         button_stud18.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud18MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud18);
         button_stud18.setBounds(360, 220, 110, 50);
 
         button_stud19.setText("Platz_19");
         button_stud19.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud19MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud19);
         button_stud19.setBounds(510, 220, 110, 50);
 
         button_stud20.setText("Platz_20");
         button_stud20.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud20MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud20);
         button_stud20.setBounds(660, 220, 110, 50);
 
         button_stud21.setText("Platz_21");
         button_stud21.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud21MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud21);
         button_stud21.setBounds(60, 290, 110, 50);
 
         button_stud22.setText("Platz_22");
         button_stud22.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud22MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud22);
         button_stud22.setBounds(210, 290, 110, 50);
 
         button_stud23.setText("Platz_23");
         button_stud23.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud23MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud23);
         button_stud23.setBounds(360, 290, 110, 50);
 
         button_stud24.setText("Platz_24");
         button_stud24.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud24MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud24);
         button_stud24.setBounds(510, 290, 110, 50);
 
         button_stud25.setText("Platz_25");
         button_stud25.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud25MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud25);
         button_stud25.setBounds(660, 290, 110, 50);
 
         button_stud26.setText("Platz_26");
         button_stud26.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud26MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud26);
         button_stud26.setBounds(60, 360, 110, 50);
 
         button_stud27.setText("Platz_27");
         button_stud27.setToolTipText("");
         button_stud27.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud27MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud27);
         button_stud27.setBounds(210, 360, 110, 50);
 
         button_stud28.setText("Platz_28");
         button_stud28.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud28MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud28);
         button_stud28.setBounds(360, 360, 110, 50);
 
         button_stud29.setText("Platz_29");
         button_stud29.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud29MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud29);
         button_stud29.setBounds(510, 360, 110, 50);
 
         button_stud30.setText("Platz_30");
         button_stud30.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 button_stud30MouseClicked(evt);
             }
         });
         panel_activityPhaseStudField.add(button_stud30);
         button_stud30.setBounds(660, 360, 110, 50);
         panel_activityPhaseStudField.add(jLab_DozSwitch2);
         jLab_DozSwitch2.setBounds(0, 0, 150, 110);
 
         gamePlaying.add(panel_activityPhaseStudField);
         panel_activityPhaseStudField.setBounds(150, 110, 830, 550);
 
         jPanel2.add(gamePlaying, "card2");
 
         jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1024, 768));
 
         panel_Login.setMaximumSize(new java.awt.Dimension(1000, 700));
         panel_Login.setMinimumSize(new java.awt.Dimension(1000, 700));
         panel_Login.setPreferredSize(new java.awt.Dimension(1000, 700));
         panel_Login.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         panel_Register.setMaximumSize(new java.awt.Dimension(500, 700));
         panel_Register.setMinimumSize(new java.awt.Dimension(500, 700));
         panel_Register.setPreferredSize(new java.awt.Dimension(500, 700));
         panel_Register.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         textfield_rKontoname.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_rKontonameActionPerformed(evt);
             }
         });
         textfield_rKontoname.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_rKontonameFocusGained(evt);
             }
         });
         panel_Register.add(textfield_rKontoname, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 140, 149, -1));
 
         textfield_rEmail1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_rEmail1ActionPerformed(evt);
             }
         });
         textfield_rEmail1.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_rEmail1FocusGained(evt);
             }
         });
         panel_Register.add(textfield_rEmail1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 230, 149, -1));
 
         textfield_rEmail2.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_rEmail2FocusGained(evt);
             }
         });
         panel_Register.add(textfield_rEmail2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 260, 149, -1));
 
         textfield_rName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_rNameActionPerformed(evt);
             }
         });
         textfield_rName.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_rNameFocusGained(evt);
             }
         });
         panel_Register.add(textfield_rName, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 290, 149, -1));
 
         textfield_rSurename.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_rSurenameFocusGained(evt);
             }
         });
         panel_Register.add(textfield_rSurename, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 320, 149, -1));
 
         password_rPass1.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 password_rPass1FocusGained(evt);
             }
         });
         panel_Register.add(password_rPass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 170, 149, -1));
 
         password_rPass2.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 password_rPass2FocusGained(evt);
             }
         });
         panel_Register.add(password_rPass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 200, 149, -1));
 
         button_rRegister.setText("Registrieren");
         button_rRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_rRegister.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_rRegisterActionPerformed(evt);
             }
         });
         panel_Register.add(button_rRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 400, -1, -1));
 
         label_rKontoname.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_rKontoname.setText("Kontoname:");
         panel_Register.add(label_rKontoname, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, -1, 20));
         label_rKontoname.getAccessibleContext().setAccessibleName("label_rKontoname");
 
         label_rPass1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_rPass1.setText("Passwort:");
         panel_Register.add(label_rPass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, -1, -1));
         label_rPass1.getAccessibleContext().setAccessibleName("label_rPass1");
 
         label_rPass2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_rPass2.setText("Passwort wiederholen:");
         panel_Register.add(label_rPass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, -1, -1));
         label_rPass2.getAccessibleContext().setAccessibleName("label_rPass2");
 
         label_rEmail1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_rEmail1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         label_rEmail1.setText("Email:");
         panel_Register.add(label_rEmail1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, -1, -1));
         label_rEmail1.getAccessibleContext().setAccessibleName("label_rEmail1");
 
         label_rEmail2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_rEmail2.setText("Email wiederholen:");
         panel_Register.add(label_rEmail2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 260, -1, -1));
         label_rEmail2.getAccessibleContext().setAccessibleName("label_rEmail2");
 
         label_rFirstName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_rFirstName.setText("Vorname:");
         panel_Register.add(label_rFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, -1, -1));
         label_rFirstName.getAccessibleContext().setAccessibleName("label_rFirstName");
 
         label_rSecondName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         label_rSecondName.setText("Nachname:");
         panel_Register.add(label_rSecondName, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 320, -1, -1));
         label_rSecondName.getAccessibleContext().setAccessibleName("label_rSecondName");
 
         button_cancel.setText("Abbrechen");
         button_cancel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_cancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_cancelActionPerformed(evt);
             }
         });
         panel_Register.add(button_cancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 400, -1, -1));
         button_cancel.getAccessibleContext().setAccessibleName("button_rCancel");
 
         label_rErrAccname.setForeground(new java.awt.Color(204, 0, 0));
         label_rErrAccname.setText("Fehler?");
         label_rErrAccname.setName("label_rErrAccname"); // NOI18N
         panel_Register.add(label_rErrAccname, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 140, -1, -1));
         label_rErrAccname.getAccessibleContext().setAccessibleName("label_rErrAccname");
 
         label_rErrPass1.setForeground(new java.awt.Color(204, 0, 0));
         label_rErrPass1.setText("Fehler?");
         label_rErrPass1.setName("label_rErrPass1"); // NOI18N
         panel_Register.add(label_rErrPass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 170, -1, -1));
         label_rErrPass1.getAccessibleContext().setAccessibleName("label_rErrPass1");
 
         label_rErrPass2.setForeground(new java.awt.Color(204, 0, 0));
         label_rErrPass2.setText("Fehler?");
         panel_Register.add(label_rErrPass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 200, -1, -1));
         label_rErrPass2.getAccessibleContext().setAccessibleName("label_rErrPass2");
 
         label_rErrEmail1.setForeground(new java.awt.Color(204, 0, 0));
         label_rErrEmail1.setText("Fehler?");
         panel_Register.add(label_rErrEmail1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 230, -1, -1));
         label_rErrEmail1.getAccessibleContext().setAccessibleName("label_rErrEmail1");
 
         label_rErrEmail2.setForeground(new java.awt.Color(204, 0, 0));
         label_rErrEmail2.setText("Fehler?");
         panel_Register.add(label_rErrEmail2, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 260, -1, -1));
         label_rErrEmail2.getAccessibleContext().setAccessibleName("label_rErrEmail2");
 
         label_rErrFirstName.setForeground(new java.awt.Color(204, 0, 0));
         label_rErrFirstName.setText("Fehler?");
         panel_Register.add(label_rErrFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 290, -1, -1));
         label_rErrFirstName.getAccessibleContext().setAccessibleName("label_rErrFirstName");
 
         label_rErrLastName.setForeground(new java.awt.Color(204, 0, 0));
         label_rErrLastName.setText("Fehler?");
         panel_Register.add(label_rErrLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 320, -1, -1));
         label_rErrLastName.getAccessibleContext().setAccessibleName("label_rErrSecondName");
         label_rErrLastName.getAccessibleContext().setAccessibleDescription("");
 
         background_register.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pictures/loginscreen2_00000.png"))); // NOI18N
         background_register.setText("jLabel17");
         panel_Register.add(background_register, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
 
         panel_Login.add(panel_Register, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 550, 870));
 
         textfield_Kontoname.setHorizontalAlignment(javax.swing.JTextField.LEFT);
         textfield_Kontoname.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_KontonameActionPerformed(evt);
             }
         });
         textfield_Kontoname.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_KontonameFocusGained(evt);
             }
         });
         panel_Login.add(textfield_Kontoname, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 160, 145, 30));
 
         button_Login.setText("Login");
         button_Login.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_Login.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_LoginActionPerformed(evt);
             }
         });
         panel_Login.add(button_Login, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 250, 80, 46));
 
         button_Register.setText("Registrieren");
         button_Register.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_Register.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_RegisterActionPerformed(evt);
             }
         });
         panel_Login.add(button_Register, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 320, 110, 30));
 
         password_Pass.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 password_PassFocusGained(evt);
             }
         });
         panel_Login.add(password_Pass, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 200, 145, 30));
 
         jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         jLabel12.setText("Kontoname:");
         panel_Login.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 170, -1, -1));
         jLabel12.getAccessibleContext().setAccessibleName("label_lUsername");
 
         jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         jLabel15.setText("Passwort:");
         panel_Login.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 200, -1, 20));
         jLabel15.getAccessibleContext().setAccessibleName("label_lPassword");
 
         check_saveUser.setText("Kontoname und Passwort speichern");
         check_saveUser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 check_saveUserActionPerformed(evt);
             }
         });
         panel_Login.add(check_saveUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 250, -1, -1));
         check_saveUser.getAccessibleContext().setAccessibleName("checkBox_lSaveData");
 
         check_autoLogin.setText("Automatisch einloggen");
         panel_Login.add(check_autoLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 270, -1, -1));
         check_autoLogin.getAccessibleContext().setAccessibleName("checkBox_lAutoLogin");
 
         label_lErr.setForeground(new java.awt.Color(204, 0, 0));
         label_lErr.setText("Fehler?");
         panel_Login.add(label_lErr, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 230, -1, -1));
 
         buton_enterAdmin.setText("enter Admin-Data");
         buton_enterAdmin.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 buton_enterAdminActionPerformed(evt);
             }
         });
         panel_Login.add(buton_enterAdmin, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 310, 120, 40));
 
         login_background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pictures/loginscreen2_00000.png"))); // NOI18N
         panel_Login.add(login_background, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
 
         jPanel1.add(panel_Login, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1000, 821));
 
         panel_adminUser.setMaximumSize(new java.awt.Dimension(1000, 700));
         panel_adminUser.setMinimumSize(new java.awt.Dimension(1000, 700));
         panel_adminUser.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         button_aSave.setText("Übernehmen");
         button_aSave.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_aSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_aSaveActionPerformed(evt);
             }
         });
         panel_adminUser.add(button_aSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 420, 149, 53));
 
         label_aAccname.setText("Kontoname:");
         panel_adminUser.add(label_aAccname, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));
 
         label_aPass.setText("Passwort:");
         panel_adminUser.add(label_aPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));
 
         textfield_aKontoname.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_aKontonameActionPerformed(evt);
             }
         });
         textfield_aKontoname.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_aKontonameFocusGained(evt);
             }
         });
         panel_adminUser.add(textfield_aKontoname, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 120, 149, -1));
 
         textfield_aEmail.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textfield_aEmailActionPerformed(evt);
             }
         });
         textfield_aEmail.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_aEmailFocusGained(evt);
             }
         });
         panel_adminUser.add(textfield_aEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 180, 149, -1));
 
         label_aEmail.setText("Email:");
         panel_adminUser.add(label_aEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 180, -1, -1));
 
         button_auCancel1.setText("Abbrechen");
         button_auCancel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_auCancel1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_auCancel1ActionPerformed(evt);
             }
         });
         panel_adminUser.add(button_auCancel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 420, 144, 53));
 
         label_aFirstName.setText("Vorname:");
         panel_adminUser.add(label_aFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));
 
         textfield_aSurename.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_aSurenameFocusGained(evt);
             }
         });
         panel_adminUser.add(textfield_aSurename, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 240, 149, -1));
 
         label_aLastName.setText("Nachname:");
         panel_adminUser.add(label_aLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, -1, -1));
 
         textfield_aName.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 textfield_aNameFocusGained(evt);
             }
         });
         panel_adminUser.add(textfield_aName, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 210, 149, -1));
 
         lable_aProfil.setFont(new java.awt.Font("Tahoma", 1, 15)); // NOI18N
         lable_aProfil.setText("Profil:");
         panel_adminUser.add(lable_aProfil, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 90, -1, -1));
         panel_adminUser.add(textfield_aUCoins, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 270, 149, -1));
 
         label_aUCions.setText("UCoins:");
         panel_adminUser.add(label_aUCions, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, -1, -1));
         panel_adminUser.add(textfield_aCredits, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 120, 149, -1));
 
         label_aCredits.setText("Credits:");
         panel_adminUser.add(label_aCredits, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 120, -1, -1));
         panel_adminUser.add(textfield_aDuplo, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 150, 149, -1));
 
         label_aDuplo.setText("Duplo:");
         panel_adminUser.add(label_aDuplo, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 150, -1, -1));
         panel_adminUser.add(textfield_aRedBull, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 180, 149, -1));
         panel_adminUser.add(textfield_aOMNI, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 210, 149, -1));
 
         label_aRedBull.setText("RedBull:");
         panel_adminUser.add(label_aRedBull, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 180, -1, -1));
 
         label_aOMNI.setText("OMNI-Sense:");
         panel_adminUser.add(label_aOMNI, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 210, -1, -1));
         panel_adminUser.add(check_aSpicker, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 250, -1, -1));
 
         label_aSpicker.setText("Spicker:");
         panel_adminUser.add(label_aSpicker, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 250, -1, -1));
 
         slider_aMonth.setMaximum(18);
         slider_aMonth.setMinimum(1);
         slider_aMonth.setSnapToTicks(true);
         slider_aMonth.setValue(9);
         slider_aMonth.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 slider_aMonthStateChanged(evt);
             }
         });
         panel_adminUser.add(slider_aMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 280, 150, -1));
 
         label_aMonth.setText("Monat: ");
         panel_adminUser.add(label_aMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 280, -1, -1));
 
         label_aGame.setFont(new java.awt.Font("Tahoma", 1, 15)); // NOI18N
         label_aGame.setText("Spielstand:");
         panel_adminUser.add(label_aGame, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 90, -1, -1));
         panel_adminUser.add(textfield_aPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 150, 149, -1));
 
         label_aMonthVal.setText("2");
         panel_adminUser.add(label_aMonthVal, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 280, 20, -1));
 
         button_aDelete.setText("Profil löschen");
         button_aDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         button_aDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_aDeleteActionPerformed(evt);
             }
         });
         panel_adminUser.add(button_aDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 320, 149, 34));
 
         label_aErrAccName.setForeground(new java.awt.Color(255, 0, 0));
         label_aErrAccName.setText("Fehler?");
         panel_adminUser.add(label_aErrAccName, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 120, -1, -1));
 
         label_aErrPass.setForeground(new java.awt.Color(255, 0, 0));
         label_aErrPass.setText("Fehler?");
         panel_adminUser.add(label_aErrPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 150, -1, -1));
 
         label_aErrEmail.setForeground(new java.awt.Color(255, 0, 0));
         label_aErrEmail.setText("Fehler?");
         panel_adminUser.add(label_aErrEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 180, -1, -1));
 
         label_aErrFirstName.setForeground(new java.awt.Color(255, 0, 0));
         label_aErrFirstName.setText("Fehler?");
         panel_adminUser.add(label_aErrFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 210, -1, -1));
 
         label_aErrLastName.setForeground(new java.awt.Color(255, 0, 0));
         label_aErrLastName.setText("Fehler?");
         panel_adminUser.add(label_aErrLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 240, -1, -1));
 
         label_aErrUCoins.setForeground(new java.awt.Color(255, 0, 0));
         label_aErrUCoins.setText("Fehler?");
         panel_adminUser.add(label_aErrUCoins, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 270, -1, -1));
 
         background_adminUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pictures/adminpanel_00000.png"))); // NOI18N
         panel_adminUser.add(background_adminUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
 
         jPanel1.add(panel_adminUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
 
         panel_Admin.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         panel_Admin.setMaximumSize(new java.awt.Dimension(1000, 700));
         panel_Admin.setMinimumSize(new java.awt.Dimension(1000, 700));
         panel_Admin.setPreferredSize(new java.awt.Dimension(1000, 700));
 
         background_admin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pictures/adminpanel_00000.png"))); // NOI18N
         background_admin.setText("jLabel4");
 
         button_aCancel.setText("Abbrechen");
         button_aCancel.setMaximumSize(new java.awt.Dimension(300, 50));
         button_aCancel.setMinimumSize(new java.awt.Dimension(300, 50));
         button_aCancel.setPreferredSize(new java.awt.Dimension(300, 50));
         button_aCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 button_aCancelActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panel_AdminLayout = new javax.swing.GroupLayout(panel_Admin);
         panel_Admin.setLayout(panel_AdminLayout);
         panel_AdminLayout.setHorizontalGroup(
             panel_AdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(background_admin, javax.swing.GroupLayout.PREFERRED_SIZE, 1000, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGroup(panel_AdminLayout.createSequentialGroup()
                 .addGap(359, 359, 359)
                 .addComponent(button_aCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         panel_AdminLayout.setVerticalGroup(
             panel_AdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(background_admin)
             .addGroup(panel_AdminLayout.createSequentialGroup()
                 .addGap(619, 619, 619)
                 .addComponent(button_aCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel1.add(panel_Admin, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
 
         getContentPane().add(jPanel1);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void goToNextPage() {
         // Flipes to the planning phase after a minigame.
         cl.show(jPanel2, "card3");
     }
 
     private void jToggleBut_SwitchStudMouseClicked(java.awt.event.MouseEvent evt) {
         planningPhase.startStudSwitch(jLab_StudCounter);
 
     }
 
     private void buton_enterAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buton_enterAdminActionPerformed
         textfield_Kontoname.setText(_adminName);
         password_Pass.setText(_adminPass);
     }//GEN-LAST:event_buton_enterAdminActionPerformed
 
     private void button_menuStartNewGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_menuStartNewGameActionPerformed
         // Starts a new game with initial values from the game_1.java. 
         if (jPanel2.isVisible() == false) {
             jPanel2.setVisible(true);
         }
         cl.show(jPanel2, "card2");
         startPlanningGame.setVisible(false);
         item.createItemInventory(label_item1Inv, label_item1InvName, label_item1InvAmount, _maingame.redBull);
         item.createItemInventory(label_item2Inv, label_item2InvName, label_item2InvAmount, _maingame.duplo);
         item.createItemInventory(label_item3Inv, label_item3InvName, label_item3InvAmount, _maingame.omniSenseAudio);
         label_ucoinsInv.setText("UCoins:  " + _mainuser.getUcoins());
         label_creditsInv.setText("Credits:   " + _maingame.credits);
         activityPhase = new ActivityPhase(label_timer, KnowledgeBar, MotivationBar, TirednessBar, label_item1InvAmount, label_item2InvAmount, label_item3InvAmount, activityPhaseButtons); // added by Jörg, Nadir
     }//GEN-LAST:event_button_menuStartNewGameActionPerformed
 
     private void button_menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_menuExitActionPerformed
         // Closes the entire game.
         System.exit(0);
     }//GEN-LAST:event_button_menuExitActionPerformed
 
     private void button_menuLoadGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_menuLoadGameActionPerformed
         // Loads and existing game which always starts with a planning phase.
         if (jPanel2.isVisible() == false) {
             jPanel2.setVisible(true);
         }
         cl.show(jPanel2, "card3");
         startPlanningGame.setVisible(false);
         planningPhase = new PlanningPhase(jProgB_Wissen, jProgB_Motivation, jProgB_Müdigkeit, jLab_DozCounter, jToggleBut_SwitchStud);  // added by Tobias, Yulyia
         jLab_Duplo.setText("Duplo: " + _maingame.duplo.amount + "x");
         jLab_Redbull.setText("Red Bull: " + _maingame.redBull.amount + "x");
         jLab_OMNI.setText("OMNISense Audio: " + _maingame.omniSenseAudio.amount + "x");
     }//GEN-LAST:event_button_menuLoadGameActionPerformed
 
     private void button_menuCreditsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_menuCreditsActionPerformed
         if (jPanel2.isVisible() == false) {
             jPanel2.setVisible(true);
         }
         panel_snakeMinigame = new Board();
         jPanel2.add(panel_snakeMinigame, "card5");
         cl.show(jPanel2, "card5");
         startPlanningGame.setVisible(false);
     }//GEN-LAST:event_button_menuCreditsActionPerformed
 
     private void button_menuProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_menuProfileActionPerformed
         initProfile();
         panel_Profile.setVisible(true);
     }//GEN-LAST:event_button_menuProfileActionPerformed
 
     private void button_menuLogOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_menuLogOutActionPerformed
         logOut();
     }//GEN-LAST:event_button_menuLogOutActionPerformed
 
     private void button_swapperExchangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_swapperExchangeActionPerformed
         // This function is called from the shop, after the coin exchanger was opened and
         // the amount that shall be swapped was entered.
         // To finish the exchange the exchange button has to be pressed. After that the ucoins are reduced and
         // the credits increased.
         int parseResult = 0;
         int result = 1;
         try {
             parseResult = Integer.parseInt(textfield_swapperUcoins.getText());
         } catch (Exception e) {
             textfield_swapperUcoins.setText("");
         }
         if (parseResult >= 0) {
             result = exchange.ucoinsToCredits(parseResult, 1, textfield_swapperCredits);
         }
         if (result == 0) {
             label_swapperCreditsAmount.setText("" + _maingame.credits);
             label_swapperUcoinsAmount.setText("" + _mainuser.getUcoins());
             OpenShop labels = new OpenShop();
             labels.changeLabels(creditsShop, punkteShop, ucoinsShop);
             textfield_swapperUcoins.setText("");
             textfield_swapperCredits.setText("");
         }
     }//GEN-LAST:event_button_swapperExchangeActionPerformed
 
     private void button_swapperAbordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_swapperAbordActionPerformed
         // This function closes the exchange window if the user does not want to exchange more.
         buyCoins.setVisible(false);
     }//GEN-LAST:event_button_swapperAbordActionPerformed
 
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton4ActionPerformed
 
     private void jLab_LogoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLab_LogoMouseClicked
         // if the toggleButton is clicked for switching student and not a student the button will be deselected
         jToggleBut_SwitchStud.setSelected(false);
         
         jPanel2.setVisible(false);
         startPlanningGame.setVisible(true);
     }//GEN-LAST:event_jLab_LogoMouseClicked
 
     private void jLabel55MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel55MouseClicked
         // TODO add your handling code here:
         jPanel2.setVisible(false);
         startPlanningGame.setVisible(true);
     }//GEN-LAST:event_jLabel55MouseClicked
 
     private void label_returnToPlanningPhaseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_returnToPlanningPhaseMouseClicked
         // Returns from the shop back to the planning phase
         cl.show(jPanel2, "card3");
         startPlanningGame.setVisible(false);
     }//GEN-LAST:event_label_returnToPlanningPhaseMouseClicked
 
     private void button_startExchangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_startExchangeActionPerformed
         // Starts the exchange dialog in the shop
         label_swapperCreditsAmount.setText("" + _maingame.credits);
         label_swapperUcoinsAmount.setText("" + _mainuser.getUcoins());
         buyCoins.setVisible(true);
     }//GEN-LAST:event_button_startExchangeActionPerformed
 
     private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
         // TODO add your handling code here:
         jPanel2.setVisible(false);
         startPlanningGame.setVisible(true);
     }//GEN-LAST:event_jLabel2MouseClicked
 
     private void button_menuStatisticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_menuStatisticActionPerformed
         if (jPanel2.isVisible() == false) {
             jPanel2.setVisible(true);
         }
         cl.show(jPanel2, "card6");
         startPlanningGame.setVisible(false);
     }//GEN-LAST:event_button_menuStatisticActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         String test = jButton1.getText();
        if (test.equals("Fenster : AUF")) {
             _maingame.windowClosed=true;
            jButton1.setText("Fenster : ZU");
         }
        if (test.equals("Fenster : ZU")) {
             _maingame.windowClosed=false;
            jButton1.setText("Fenster : AUF");
         }
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jBut_ChangeLectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBut_ChangeLectorActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_ChangeLectorActionPerformed
 
     private void jBut_PlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBut_PlayActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_PlayActionPerformed
 
     private void jBut_startShopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBut_startShopActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_startShopActionPerformed
 
     private void jBut_17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBut_17ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_17ActionPerformed
 
     private void jBut_startShopMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_startShopMouseClicked
         // if the toggleButton is clicked for switching student and not a student the button will be deselected
         jToggleBut_SwitchStud.setSelected(false);
         
         // This function is called after the shop button from the planning phase was pressed.
         // It opens the shop, creates the inventory and the objects that can be bought.
         cl.show(jPanel2, "card4");
         startPlanningGame.setVisible(false);
         OpenShop labels = new OpenShop();
         labels.changeLabels(creditsShop, punkteShop, ucoinsShop);
         item.createItemInventory(label_item1, label_item1Name, label_item1Amount, _maingame.redBull);
         item.createItemInventory(label_item2, label_item2Name, label_item2Amount, _maingame.duplo);
         item.createItemInventory(label_item3, label_item3Name, label_item3Amount, _maingame.omniSenseAudio);
         item.createItemInventory(label_item4, label_item4Name, label_item4Amount, _maingame.cheatSheet);
         item.createItemShop(label_redBullName, label_redBullAmount, label_redBullLocked, _maingame.redBull);
         item.createItemShop(label_duploName, label_duploAmount, label_duploLocked, _maingame.duplo);
         item.createItemShop(label_cheatSheetName, label_cheatSheetAmount, label_cheatSheetLocked, _maingame.cheatSheet);
         item.createItemShop(label_omniName, label_omniAmount, label_omniLocked, _maingame.omniSenseAudio);
     }//GEN-LAST:event_jBut_startShopMouseClicked
 
     private void label_redBullOverlayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_redBullOverlayMouseClicked
         // On click Red Bull can be bought if the user has enough credits 
         int result = item.managePurchase(_maingame.redBull, label_redBullLocked);
         if (result != 0) {
             dialog_error.setVisible(true);
             dialog_error.setBounds(300, 200, 400, 320);
         } else {
             item.updateInventroy(label_item1Amount, _maingame.redBull, creditsShop, _maingame.credits, label_item1);
             item.updateInventoryPlanningPhase(jLab_Redbull, _maingame.redBull);
         }
     }//GEN-LAST:event_label_redBullOverlayMouseClicked
 
     private void label_duploOverlayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_duploOverlayMouseClicked
         // On click Duplo can be bought if the user has enough credits 
         int result = item.managePurchase(_maingame.duplo, label_duploLocked);
         if (result != 0) {
             dialog_error.setVisible(true);
             dialog_error.setBounds(300, 200, 400, 320);
         } else {
             item.updateInventroy(label_item2Amount, _maingame.duplo, creditsShop, _maingame.credits, label_item2);
             item.updateInventoryPlanningPhase(jLab_Duplo, _maingame.duplo);
         }
     }//GEN-LAST:event_label_duploOverlayMouseClicked
 
     private void label_cheatSheetOverlayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_cheatSheetOverlayMouseClicked
         // On click the cheat sheet can be bought if the user has enough ucoins 
         int result = item.managePurchase(_maingame.cheatSheet, label_cheatSheetLocked);
         if (result != 0) {
             dialog_error.setVisible(true);
             dialog_error.setBounds(300, 200, 400, 320);
         } else {
             item.updateInventroy(label_item4Amount, _maingame.cheatSheet, ucoinsShop, _mainuser.getUcoins(), label_item4);
         }
     }//GEN-LAST:event_label_cheatSheetOverlayMouseClicked
 
     private void label_omniOverlayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_omniOverlayMouseClicked
         // On click the OMNI Sense audiobook can be bought if the user has enough ucoins 
         int result = item.managePurchase(_maingame.omniSenseAudio, label_omniLocked);
         if (result != 0) {
             dialog_error.setVisible(true);
             dialog_error.setBounds(300, 200, 400, 320);
         } else {
             item.updateInventroy(label_item3Amount, _maingame.omniSenseAudio, ucoinsShop, _mainuser.getUcoins(), label_item3);
             item.updateInventoryPlanningPhase(jLab_OMNI, _maingame.omniSenseAudio);
         }
     }//GEN-LAST:event_label_omniOverlayMouseClicked
 
     private void button_shopMessageOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_shopMessageOkActionPerformed
         dialog_error.setVisible(false);
     }//GEN-LAST:event_button_shopMessageOkActionPerformed
 
     private void jBut_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBut_1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_1ActionPerformed
 
     private void textfield_swapperUcoinsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textfield_swapperUcoinsKeyReleased
         // If the user types something in the exchange dialog the changed
         // amount is displayed directly.
         int parseResult = 0;
         try {
             parseResult = Integer.parseInt(textfield_swapperUcoins.getText());
         } catch (Exception e) {
             textfield_swapperUcoins.setText("");
         }
         if (parseResult >= 0) {
             exchange.ucoinsToCredits(parseResult, 0, textfield_swapperCredits);
         }
     }//GEN-LAST:event_textfield_swapperUcoinsKeyReleased
 
     private void jBut_1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_1MouseClicked
         // TODO add your handling code here:
 
         /**
          * @author Tobias Mauritz
          */
         planningPhase.StudButtonFunctions(0);
         // !!! kann nicht funzen!! wir brauchen akutelles Objekt von PLanningPhase, hier ist nur das generelle!
         // --> alles auslagern in Planungsphasen-Mehtode
 //        if (PlanningPhase.getSwitchFlag() == 0){
 //            
 //            // start method StudInfo() which shows knowledge, motivation and tiredness 
 //            // of the student which was clicked
 //        PlanningPhase.studInfo.StudInfoAttr(0);
 //            //StudInfo curStud = new StudInfo(0,jProgB_Wissen,jProgB_Motivation, jProgB_Müdigkeit);
 //        //System.out.println("Student 1 = "+PlanningPhase.StudArr[0].getId());
 //        
 //        } else {
 //            System.out.println("TauschFlag = 1");
 //            PlanningPhase.storeStud(1);
 //        }
     }//GEN-LAST:event_jBut_1MouseClicked
 
     private void jBut_2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_2MouseClicked
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.StudButtonFunctions(1);
 
 //        if (PlanningPhase.getSwitchFlag() == 0){
 //            
 //            PlanningPhase.studInfo.StudInfoAttr(1);
 //            //System.out.println("Student 2 = "+PlanningPhase.StudArr[1].getId());
 //      
 //        } else {
 //            System.out.println("TauschFlag = 1");
 //            PlanningPhase.storeStud(2);
 //            
 //        }
     }//GEN-LAST:event_jBut_2MouseClicked
 
     private void jBut_3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_3MouseClicked
 
         planningPhase.StudButtonFunctions(2);
 
     }//GEN-LAST:event_jBut_3MouseClicked
 
     private void jBut_4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_4MouseClicked
         planningPhase.StudButtonFunctions(3);
     }//GEN-LAST:event_jBut_4MouseClicked
 
     private void jBut_5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_5MouseClicked
         planningPhase.StudButtonFunctions(4);
     }//GEN-LAST:event_jBut_5MouseClicked
 
     private void jBut_1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_1MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_1MouseEntered
 
     private void jBut_1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_1MouseExited
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_1MouseExited
 
     private void StudFieldMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_StudFieldMouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_StudFieldMouseEntered
 
     private void StudFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_StudFieldMouseClicked
         // TODO add your handling code here:
 
         System.out.println("StudField");
         // 
         planningPhase.studInfo.StudInfoAverage();
         //StudInfo z = new StudInfo(jProgB_Wissen,jProgB_Motivation, jProgB_Müdigkeit);
 
     }//GEN-LAST:event_StudFieldMouseClicked
 
     private void jBut_2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_2MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_2MouseEntered
 
     private void jBut_3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_3MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_3MouseEntered
 
     private void jBut_4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_4MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_4MouseEntered
 
     private void jBut_5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_5MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_5MouseEntered
 
     private void jBut_6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_6MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(5);
     }//GEN-LAST:event_jBut_6MouseClicked
 
     private void jBut_7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_7MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(6);
     }//GEN-LAST:event_jBut_7MouseClicked
 
     private void jBut_8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_8MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(7);
     }//GEN-LAST:event_jBut_8MouseClicked
 
     private void jBut_9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_9MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(8);
     }//GEN-LAST:event_jBut_9MouseClicked
 
     private void jBut_10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_10MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(9);
     }//GEN-LAST:event_jBut_10MouseClicked
 
     private void jBut_11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_11MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(10);
     }//GEN-LAST:event_jBut_11MouseClicked
 
     private void jBut_12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_12MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(11);
     }//GEN-LAST:event_jBut_12MouseClicked
 
     private void jBut_13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_13MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(12);
     }//GEN-LAST:event_jBut_13MouseClicked
 
     private void jBut_14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_14MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(13);
     }//GEN-LAST:event_jBut_14MouseClicked
 
     private void jBut_15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_15MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(14);
     }//GEN-LAST:event_jBut_15MouseClicked
 
     private void jBut_16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_16MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(15);
     }//GEN-LAST:event_jBut_16MouseClicked
 
     private void jBut_17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_17MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(16);
     }//GEN-LAST:event_jBut_17MouseClicked
 
     private void jBut_18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_18MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(17);
     }//GEN-LAST:event_jBut_18MouseClicked
 
     private void jBut_19MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_19MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(18);
     }//GEN-LAST:event_jBut_19MouseClicked
 
     private void jBut_20MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_20MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(19);
     }//GEN-LAST:event_jBut_20MouseClicked
 
     private void jBut_21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_21MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(20);
     }//GEN-LAST:event_jBut_21MouseClicked
 
     private void jBut_22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_22MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(21);
     }//GEN-LAST:event_jBut_22MouseClicked
 
     private void jBut_23MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_23MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(22);
     }//GEN-LAST:event_jBut_23MouseClicked
 
     private void jBut_24MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_24MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(23);
     }//GEN-LAST:event_jBut_24MouseClicked
 
     private void jBut_25MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_25MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(24);
     }//GEN-LAST:event_jBut_25MouseClicked
 
     private void jBut_26MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_26MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(25);
     }//GEN-LAST:event_jBut_26MouseClicked
 
     private void jBut_27MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_27MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(26);
     }//GEN-LAST:event_jBut_27MouseClicked
 
     private void jBut_28MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_28MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(27);
     }//GEN-LAST:event_jBut_28MouseClicked
 
     private void jBut_29MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_29MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(28);
     }//GEN-LAST:event_jBut_29MouseClicked
 
     private void jBut_30MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_30MouseClicked
         // TODO add your handling code here:
         // functionality: see comment at jBut_1MouseClicked
         planningPhase.studInfo.StudInfoAttr(29);
     }//GEN-LAST:event_jBut_30MouseClicked
 
     private void jBut_ChangeLectorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_ChangeLectorMouseClicked
         // TODO add your handling code here:
         
         // if the toggleButton is clicked for switching student and not a student the button will be deselected
         jToggleBut_SwitchStud.setSelected(false);
         
         //check if can change lector. If so - do this, else - error 
         boolean allowed = planningPhase.validateLector(jLab_DozCounter);
         if (allowed) {
             planningPhase.changeLector(jLab_DozCounter);
             lectorChangedDialog.setVisible(true);
         } else {
             lectorNotChangedDialog.setVisible(true);
         }
     }//GEN-LAST:event_jBut_ChangeLectorMouseClicked
 
     private void jBut_PlayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_PlayMouseClicked
         // if the toggleButton is clicked for switching student and not a student the button will be deselected
         jToggleBut_SwitchStud.setSelected(false);
         
         // Starts the action phase and the inventory. 
         if (jPanel2.isVisible() == false) {
             jPanel2.setVisible(true);
         }
         cl.show(jPanel2, "card2");
         startPlanningGame.setVisible(false);
         item.createItemInventory(label_item1Inv, label_item1InvName, label_item1InvAmount, _maingame.redBull);
         item.createItemInventory(label_item2Inv, label_item2InvName, label_item2InvAmount, _maingame.duplo);
         item.createItemInventory(label_item3Inv, label_item3InvName, label_item3InvAmount, _maingame.omniSenseAudio);
         label_ucoinsInv.setText("UCoins:  " + _mainuser.getUcoins());
         label_creditsInv.setText("Credits:   " + _maingame.credits);
         activityPhase = new ActivityPhase(label_timer, KnowledgeBar, MotivationBar, TirednessBar, label_item1InvAmount, label_item2InvAmount, label_item3InvAmount, activityPhaseButtons); // added by Jörg, Nadir
     }//GEN-LAST:event_jBut_PlayMouseClicked
 
     private void textfield_rKontonameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_rKontonameFocusGained
         textfield_rKontoname.requestFocus();  //by Dawid
         textfield_rKontoname.selectAll();
     }//GEN-LAST:event_textfield_rKontonameFocusGained
 
     private void textfield_rEmail1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_rEmail1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_rEmail1ActionPerformed
 
     private void textfield_rEmail1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_rEmail1FocusGained
         textfield_rEmail1.requestFocus();  //by Dawid
         textfield_rEmail1.selectAll();
     }//GEN-LAST:event_textfield_rEmail1FocusGained
 
     private void textfield_rEmail2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_rEmail2FocusGained
         textfield_rEmail2.requestFocus();  //by Dawid
         textfield_rEmail2.selectAll();
     }//GEN-LAST:event_textfield_rEmail2FocusGained
 
     private void textfield_rNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_rNameFocusGained
         textfield_rName.requestFocus();  //by Dawid
         textfield_rName.selectAll();
     }//GEN-LAST:event_textfield_rNameFocusGained
 
     private void textfield_rSurenameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_rSurenameFocusGained
         textfield_rSurename.requestFocus();  //by Dawid
         textfield_rSurename.selectAll();
     }//GEN-LAST:event_textfield_rSurenameFocusGained
 
     private void password_rPass1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_password_rPass1FocusGained
         password_rPass1.requestFocus();  //by Dawid
         password_rPass1.selectAll();
     }//GEN-LAST:event_password_rPass1FocusGained
 
     private void password_rPass2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_password_rPass2FocusGained
         password_rPass2.requestFocus();  //by Dawid
         password_rPass2.selectAll();
     }//GEN-LAST:event_password_rPass2FocusGained
 
     private void button_rRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_rRegisterActionPerformed
         JComponent[] toCheck = {textfield_rKontoname, password_rPass1, password_rPass2, textfield_rEmail1, textfield_rEmail2, textfield_rName, textfield_rSurename};
         JLabel[] errMess = {label_rErrAccname, label_rErrPass1, label_rErrPass2, label_rErrEmail1, label_rErrEmail2, label_rErrFirstName, label_rErrLastName};
         JTextField[] accName_eMail = {textfield_rKontoname, textfield_rEmail1};
         JLabel[] errMess2 = {label_rErrAccname, label_rErrEmail1};
         if (checkRegister_(toCheck, errMess) & !checkGlobUser(accName_eMail, errMess2)) {  //by Dawid
             User.createUser(textfield_rKontoname.getText(), password_rPass1.getPassword(), textfield_rEmail1.getText(), textfield_rName.getText(), textfield_rSurename.getText());
             panel_Login.setVisible(true);
             panel_Register.setVisible(false);
             button_Register.setSelected(false);
         }
     }//GEN-LAST:event_button_rRegisterActionPerformed
 
     private void button_LoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_LoginActionPerformed
         if (checkLogIn(textfield_Kontoname.getText(), password_Pass.getPassword())) {  //by Dawid
             logIn(textfield_Kontoname.getText());
         }
     }//GEN-LAST:event_button_LoginActionPerformed
 
     private void button_RegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_RegisterActionPerformed
         initRegister();  //by Dawid
         button_Register.setSelected(true);
         panel_Register.setVisible(true);
     }//GEN-LAST:event_button_RegisterActionPerformed
 
     private void textfield_KontonameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_KontonameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_KontonameActionPerformed
 
     private void check_saveUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_saveUserActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_check_saveUserActionPerformed
 
     private void textfield_rKontonameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_rKontonameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_rKontonameActionPerformed
 
     private void button_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_cancelActionPerformed
         panel_Register.setVisible(false);  //by Dawid
         button_Register.setSelected(false);
     }//GEN-LAST:event_button_cancelActionPerformed
 
     private void jComboB_ItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboB_ItemsActionPerformed
         /*
          * lokale Variablen für diese Methode, momentan zu Testzwecken
          */
         int currTerm = planningPhase.getTerm(); // aktuelles Semester
         int eingesetzt2 = 0; //werte kommen aus game.txt, aus "spicker pro semester" zeilen
         int eingesetzt3 = 0;
         int eingesetzt4 = 0;
         int eingesetzt5 = 0;
         int eingesetzt6 = 0;
         int amount = 1; //ANZAHL DER SPICKER, MUSS ERSETZT WERDEN
 
         javax.swing.JComboBox box = (javax.swing.JComboBox) evt.getSource();
         String selected = (String) box.getSelectedItem();
         System.out.println("Right now selected " + selected);
         if (selected.equals("Spicker")) {
             if (Sims_1._maingame.cheatSheet.amount == 0) {
                 System.out.println("Kein Spicker verfügbar");
             } else if (((currTerm == 2) && (eingesetzt2 == 0)) || //---> Abfrage, ob im jeweiligen Semester der Spicker bereits benutzt wurde.
                     ((currTerm == 3) && (eingesetzt3 == 0)) || //Wenn nicht, dann wird der Cheat Flag gesetzt
                     ((currTerm == 4) && (eingesetzt4 == 0)) || // 
                     ((currTerm == 5) && (eingesetzt5 == 0))
                     || ((currTerm == 6) && (eingesetzt6 == 0))) {
                 warning.setVisible(true);
                 System.out.println("show warning");
             } else {
                 System.out.println("Du kannst in dem " + currTerm + " Semester den Spicker nicht mehr einsetzen");
             }
         }
     }//GEN-LAST:event_jComboB_ItemsActionPerformed
 
     private void jBut_JAMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_JAMouseClicked
         // TODO add your handling code here:
         planningPhase.setCheatFlag(1);
         warning.setVisible(false);
 
     }//GEN-LAST:event_jBut_JAMouseClicked
 
     private void jBut_NEINMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_NEINMouseClicked
         // TODO add your handling code here:
         planningPhase.setCheatFlag(0);
         warning.setVisible(false);
     }//GEN-LAST:event_jBut_NEINMouseClicked
 
     private void warningWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_warningWindowClosed
 //        // TODO add your handling code here:
 //        planningPhase.setCheatFlag(0);
 //        warning.setVisible(false);
     }//GEN-LAST:event_warningWindowClosed
 
     private void jBut_NEINActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBut_NEINActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_NEINActionPerformed
 
     private void jBut_JAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBut_JAActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jBut_JAActionPerformed
 
     private void jBut_OKlectorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_OKlectorMouseClicked
         // TODO add your handling code here:
         lectorChangedDialog.setVisible(false);
     }//GEN-LAST:event_jBut_OKlectorMouseClicked
 
     private void jBut_OKcheatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_OKcheatMouseClicked
         // TODO add your handling code here:
         cheatUsedDialog.setVisible(false);
     }//GEN-LAST:event_jBut_OKcheatMouseClicked
 
     private void jBut_OKnotchangedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_OKnotchangedMouseClicked
         // TODO add your handling code here:
 
         lectorNotChangedDialog.setVisible(false);
     }//GEN-LAST:event_jBut_OKnotchangedMouseClicked
 
     private void textfield_KontonameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_KontonameFocusGained
         textfield_Kontoname.requestFocus();
         textfield_Kontoname.selectAll();
     }//GEN-LAST:event_textfield_KontonameFocusGained
 
     private void password_PassFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_password_PassFocusGained
         password_Pass.requestFocus();
         password_Pass.selectAll();
     }//GEN-LAST:event_password_PassFocusGained
 
     private void button_stud1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud1MouseClicked
         activityPhase.StudentClicked(0);
     }//GEN-LAST:event_button_stud1MouseClicked
 
     private void button_stud1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud1MouseExited
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud1MouseExited
 
     private void button_stud1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud1MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud1MouseEntered
 
     private void button_stud1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_stud1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud1ActionPerformed
 
     private void button_stud2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud2MouseClicked
         activityPhase.StudentClicked(1);
     }//GEN-LAST:event_button_stud2MouseClicked
 
     private void button_stud2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud2MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud2MouseEntered
 
     private void button_stud3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud3MouseClicked
         activityPhase.StudentClicked(2);
     }//GEN-LAST:event_button_stud3MouseClicked
 
     private void button_stud3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud3MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud3MouseEntered
 
     private void button_stud4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud4MouseClicked
         activityPhase.StudentClicked(3);
     }//GEN-LAST:event_button_stud4MouseClicked
 
     private void button_stud4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud4MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud4MouseEntered
 
     private void button_stud5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud5MouseClicked
         activityPhase.StudentClicked(4);
     }//GEN-LAST:event_button_stud5MouseClicked
 
     private void button_stud5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud5MouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud5MouseEntered
 
     private void button_stud6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud6MouseClicked
         activityPhase.StudentClicked(5);
     }//GEN-LAST:event_button_stud6MouseClicked
 
     private void button_stud7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud7MouseClicked
         activityPhase.StudentClicked(6);
     }//GEN-LAST:event_button_stud7MouseClicked
 
     private void button_stud8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud8MouseClicked
         activityPhase.StudentClicked(7);
     }//GEN-LAST:event_button_stud8MouseClicked
 
     private void button_stud9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud9MouseClicked
         activityPhase.StudentClicked(8);
     }//GEN-LAST:event_button_stud9MouseClicked
 
     private void button_stud10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud10MouseClicked
         activityPhase.StudentClicked(9);
     }//GEN-LAST:event_button_stud10MouseClicked
 
     private void button_stud11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud11MouseClicked
         activityPhase.StudentClicked(10);
     }//GEN-LAST:event_button_stud11MouseClicked
 
     private void button_stud12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud12MouseClicked
         activityPhase.StudentClicked(11);
     }//GEN-LAST:event_button_stud12MouseClicked
 
     private void button_stud13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud13MouseClicked
         activityPhase.StudentClicked(12);
     }//GEN-LAST:event_button_stud13MouseClicked
 
     private void button_stud14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud14MouseClicked
         activityPhase.StudentClicked(13);
     }//GEN-LAST:event_button_stud14MouseClicked
 
     private void button_stud15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud15MouseClicked
         activityPhase.StudentClicked(14);
     }//GEN-LAST:event_button_stud15MouseClicked
 
     private void button_stud16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud16MouseClicked
         activityPhase.StudentClicked(15);
     }//GEN-LAST:event_button_stud16MouseClicked
 
     private void button_stud17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud17MouseClicked
         activityPhase.StudentClicked(16);
     }//GEN-LAST:event_button_stud17MouseClicked
 
     private void button_stud17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_stud17ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_button_stud17ActionPerformed
 
     private void button_stud18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud18MouseClicked
         activityPhase.StudentClicked(17);
     }//GEN-LAST:event_button_stud18MouseClicked
 
     private void button_stud19MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud19MouseClicked
         activityPhase.StudentClicked(18);
     }//GEN-LAST:event_button_stud19MouseClicked
 
     private void button_stud20MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud20MouseClicked
         activityPhase.StudentClicked(19);
     }//GEN-LAST:event_button_stud20MouseClicked
 
     private void button_stud21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud21MouseClicked
         activityPhase.StudentClicked(20);
     }//GEN-LAST:event_button_stud21MouseClicked
 
     private void button_stud22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud22MouseClicked
         activityPhase.StudentClicked(21);
     }//GEN-LAST:event_button_stud22MouseClicked
 
     private void button_stud23MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud23MouseClicked
         activityPhase.StudentClicked(22);
     }//GEN-LAST:event_button_stud23MouseClicked
 
     private void button_stud24MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud24MouseClicked
         activityPhase.StudentClicked(23);
     }//GEN-LAST:event_button_stud24MouseClicked
 
     private void button_stud25MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud25MouseClicked
         activityPhase.StudentClicked(24);
     }//GEN-LAST:event_button_stud25MouseClicked
 
     private void button_stud26MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud26MouseClicked
         activityPhase.StudentClicked(25);
     }//GEN-LAST:event_button_stud26MouseClicked
 
     private void button_stud27MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud27MouseClicked
         activityPhase.StudentClicked(26);
     }//GEN-LAST:event_button_stud27MouseClicked
 
     private void button_stud28MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud28MouseClicked
         activityPhase.StudentClicked(27);
     }//GEN-LAST:event_button_stud28MouseClicked
 
     private void button_stud29MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud29MouseClicked
         activityPhase.StudentClicked(28);
     }//GEN-LAST:event_button_stud29MouseClicked
 
     private void button_stud30MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_stud30MouseClicked
         activityPhase.StudentClicked(29);
     }//GEN-LAST:event_button_stud30MouseClicked
 
     private void panel_activityPhaseStudFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel_activityPhaseStudFieldMouseClicked
     activityPhase.doNotPaintFlag= false;
     activityPhase.studentDisplayed = -1;
     }//GEN-LAST:event_panel_activityPhaseStudFieldMouseClicked
 
     private void panel_activityPhaseStudFieldMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel_activityPhaseStudFieldMouseEntered
         // TODO add your handling code here:
     }//GEN-LAST:event_panel_activityPhaseStudFieldMouseEntered
 
     private void label_item1InvMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_item1InvMouseClicked
         if (activityPhase.redBullPressed == false) {
             activityPhase.redBullPressed = true;
             activityPhase.duploPressed = false;
             activityPhase.OmniSensePressed = false;
             System.out.println("redBull on!");
         } else {
             activityPhase.redBullPressed = false;
             System.out.println("redBull off!");
         }
     }//GEN-LAST:event_label_item1InvMouseClicked
 
     private void label_item2InvMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_item2InvMouseClicked
         if (activityPhase.duploPressed == false) {
             activityPhase.redBullPressed = false;
             activityPhase.duploPressed = true;
             activityPhase.OmniSensePressed = false;
             System.out.println("duplo on!");
         } else {
             activityPhase.duploPressed = false;
             System.out.println("duplo off!");
         }
     }//GEN-LAST:event_label_item2InvMouseClicked
 
     private void label_item3InvMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_item3InvMouseClicked
         if (activityPhase.redBullPressed == false) {
             activityPhase.redBullPressed = false;
             activityPhase.duploPressed = false;
             activityPhase.OmniSensePressed = true;
             System.out.println("OmniSense on!");
         } else {
             activityPhase.redBullPressed = false;
             System.out.println("OmniSense off!");
         }
     }//GEN-LAST:event_label_item3InvMouseClicked
 
     private void button_aSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_aSaveActionPerformed
         if (_mainadmin.checkChanges()) {
             _mainadmin.saveChages();
             adminLogin();
         }
     }//GEN-LAST:event_button_aSaveActionPerformed
 
     private void textfield_aKontonameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_aKontonameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_aKontonameActionPerformed
 
     private void textfield_aKontonameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_aKontonameFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_aKontonameFocusGained
 
     private void textfield_aEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_aEmailActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_aEmailActionPerformed
 
     private void textfield_aEmailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_aEmailFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_aEmailFocusGained
 
     private void button_auCancel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_auCancel1ActionPerformed
         panel_Admin.setVisible(true);
         panel_adminUser.setVisible(false);
     }//GEN-LAST:event_button_auCancel1ActionPerformed
 
     private void textfield_aSurenameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_aSurenameFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_aSurenameFocusGained
 
     private void textfield_aNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_aNameFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_aNameFocusGained
 
     private void slider_aMonthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider_aMonthStateChanged
         label_aMonthVal.setText(Integer.toString(slider_aMonth.getValue()));
     }//GEN-LAST:event_slider_aMonthStateChanged
 
     private void textfield_rNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_rNameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_rNameActionPerformed
 
     private void button_aDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_aDeleteActionPerformed
         if (JOptionPane.showConfirmDialog(null, "Profil wirklich löschen? Achtung: kann nciht rückgängig gemacht werden") == JOptionPane.OK_OPTION) {
             _mainadmin.delSpecUser();
             adminLogin();
         }
     }//GEN-LAST:event_button_aDeleteActionPerformed
 
     private void button_aCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_aCancelActionPerformed
         panel_Admin.setVisible(false);
         panel_Login.setVisible(true);
     }//GEN-LAST:event_button_aCancelActionPerformed
 
     private void textfield_pKontonameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_pKontonameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pKontonameActionPerformed
 
     private void textfield_pKontonameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_pKontonameFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pKontonameFocusGained
 
     private void textfield_pEmail1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_pEmail1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pEmail1ActionPerformed
 
     private void textfield_pEmail1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_pEmail1FocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pEmail1FocusGained
 
     private void textfield_pEmail2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_pEmail2FocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pEmail2FocusGained
 
     private void textfield_pNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfield_pNameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pNameActionPerformed
 
     private void textfield_pNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_pNameFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pNameFocusGained
 
     private void textfield_pSurenameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textfield_pSurenameFocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_textfield_pSurenameFocusGained
 
     private void password_pPass1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_password_pPass1FocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_password_pPass1FocusGained
 
     private void password_pPass2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_password_pPass2FocusGained
         // TODO add your handling code here:
     }//GEN-LAST:event_password_pPass2FocusGained
 
     private void button_pSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_pSaveActionPerformed
 
         JComponent[] toCheck = {textfield_pKontoname, password_pPass1, password_pPass2, textfield_pEmail1, textfield_pEmail2, textfield_pName, textfield_pSurename};
         JLabel[] errMess = {label_pErrAccname, label_pErrPass1, label_pErrPass2, label_pErrEmail1, label_pErrEmail2, label_pErrFirstName, label_pErrLastName};
         JTextField[] toCheck2 = {textfield_pKontoname, textfield_pEmail1};
         JLabel[] errMess2 = {label_pErrAccname, label_pErrEmail1};
         JPasswordField inputPw = new JPasswordField();
         Object[] ob = {inputPw};
 
         if (((_mainuser.getAccountname().equals(textfield_pKontoname.getText()) & _mainuser.getEmail().equals(textfield_pEmail1.getText())) | !checkGlobUserVar(toCheck2, errMess2)) & checkRegister_(toCheck, errMess)) {
             if (JOptionPane.showConfirmDialog(null, ob, "Bitte aktuelles Passwort eingeben!", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION & inputPw.getPassword().equals(_mainuser.getPassword().toCharArray())) {
                 if (!_mainuser.getAccountname().equals(textfield_pKontoname.getText()) | !_mainuser.getEmail().equals(textfield_pEmail1.getText())) {
                     User.deleteUser(_mainuser);
                 }
                 User.createUser(textfield_pKontoname.getText(), password_pPass1.getPassword(), textfield_pEmail1.getText(), textfield_pName.getText(), textfield_pSurename.getText(), _mainuser.getReg_date(), _mainuser.getLast_login(), _mainuser.getTime_played(), _mainuser.getUcoins());
                 _mainuser = new User(textfield_pKontoname.getText());
             }
             panel_Profile.setVisible(false);
         }
     }//GEN-LAST:event_button_pSaveActionPerformed
 
     private void button_pCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_pCancelActionPerformed
         panel_Profile.setVisible(false);
     }//GEN-LAST:event_button_pCancelActionPerformed
 
     private void KnowledgeBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_KnowledgeBarMouseClicked
         if(activityPhase.barNum==1){
             activityPhase.barNum=0;
         }else{
             activityPhase.barNum=1;
         }
         activityPhase.barClicked();
     }//GEN-LAST:event_KnowledgeBarMouseClicked
 
     private void MotivationBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MotivationBarMouseClicked
         if(activityPhase.barNum==2){
             activityPhase.barNum=0;
         }else{
             activityPhase.barNum=2;
         }
         activityPhase.barClicked();
     }//GEN-LAST:event_MotivationBarMouseClicked
 
     private void TirednessBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TirednessBarMouseClicked
         if(activityPhase.barNum==3){
             activityPhase.barNum=0;
         }else{
             activityPhase.barNum=3;
         }
         activityPhase.barClicked();
     }//GEN-LAST:event_TirednessBarMouseClicked
 
     private void jBut_DozentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBut_DozentMouseClicked
         // if the toggleButton is clicked for switching student and not a student the button will be deselected
         jToggleBut_SwitchStud.setSelected(false);
     }//GEN-LAST:event_jBut_DozentMouseClicked
 
     private void jComboB_ItemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboB_ItemsMouseClicked
         // if the toggleButton is clicked for switching student and not a student the button will be deselected
         jToggleBut_SwitchStud.setSelected(false);
     }//GEN-LAST:event_jComboB_ItemsMouseClicked
     
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(Sims_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(Sims_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(Sims_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(Sims_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new Sims_1().setVisible(true);
             }
         });
     }
     public static JButton button_afterGame; // Button used after a minigame added by Jannik
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel Header;
     private javax.swing.JProgressBar KnowledgeBar;
     private javax.swing.JPanel Logo;
     private javax.swing.JLabel Menu_overlay;
     private javax.swing.JLabel Menu_overlay1;
     private javax.swing.JProgressBar MotivationBar;
     private javax.swing.JPanel Navi;
     private javax.swing.JPanel Shop;
     private javax.swing.JPanel StudField;
     private javax.swing.JProgressBar TirednessBar;
     private javax.swing.JLabel background_admin;
     private javax.swing.JLabel background_adminUser;
     private javax.swing.JLabel background_register;
     private javax.swing.JButton buton_enterAdmin;
     private javax.swing.JButton button_Login;
     private javax.swing.JToggleButton button_Register;
     private javax.swing.JButton button_aCancel;
     private javax.swing.JButton button_aDelete;
     private javax.swing.JButton button_aSave;
     private javax.swing.JButton button_auCancel1;
     private javax.swing.JButton button_cancel;
     private javax.swing.JButton button_menuCredits;
     private javax.swing.JButton button_menuExit;
     private javax.swing.JButton button_menuLoadGame;
     private javax.swing.JButton button_menuLogOut;
     private javax.swing.JButton button_menuProfile;
     private javax.swing.JButton button_menuStartNewGame;
     private javax.swing.JButton button_menuStatistic;
     private javax.swing.JButton button_pCancel;
     private javax.swing.JButton button_pSave;
     private javax.swing.JButton button_rRegister;
     private javax.swing.JButton button_shopMessageOk;
     private javax.swing.JButton button_startExchange;
     private javax.swing.JButton button_stud1;
     private javax.swing.JButton button_stud10;
     private javax.swing.JButton button_stud11;
     private javax.swing.JButton button_stud12;
     private javax.swing.JButton button_stud13;
     private javax.swing.JButton button_stud14;
     private javax.swing.JButton button_stud15;
     private javax.swing.JButton button_stud16;
     private javax.swing.JButton button_stud17;
     private javax.swing.JButton button_stud18;
     private javax.swing.JButton button_stud19;
     private javax.swing.JButton button_stud2;
     private javax.swing.JButton button_stud20;
     private javax.swing.JButton button_stud21;
     private javax.swing.JButton button_stud22;
     private javax.swing.JButton button_stud23;
     private javax.swing.JButton button_stud24;
     private javax.swing.JButton button_stud25;
     private javax.swing.JButton button_stud26;
     private javax.swing.JButton button_stud27;
     private javax.swing.JButton button_stud28;
     private javax.swing.JButton button_stud29;
     private javax.swing.JButton button_stud3;
     private javax.swing.JButton button_stud30;
     private javax.swing.JButton button_stud4;
     private javax.swing.JButton button_stud5;
     private javax.swing.JButton button_stud6;
     private javax.swing.JButton button_stud7;
     private javax.swing.JButton button_stud8;
     private javax.swing.JButton button_stud9;
     private javax.swing.JButton button_swapperAbord;
     private javax.swing.JButton button_swapperExchange;
     private javax.swing.JDialog buyCoins;
     private javax.swing.JDialog cheatUsedDialog;
     private javax.swing.JCheckBox check_aSpicker;
     private javax.swing.JCheckBox check_autoLogin;
     private javax.swing.JCheckBox check_saveUser;
     private javax.swing.JLabel creditsShop;
     private javax.swing.JDialog dialog_error;
     private javax.swing.JPanel gamePlanning;
     private javax.swing.JPanel gamePlaying;
     private javax.swing.JButton jBut_1;
     private javax.swing.JButton jBut_10;
     private javax.swing.JButton jBut_11;
     private javax.swing.JButton jBut_12;
     private javax.swing.JButton jBut_13;
     private javax.swing.JButton jBut_14;
     private javax.swing.JButton jBut_15;
     private javax.swing.JButton jBut_16;
     private javax.swing.JButton jBut_17;
     private javax.swing.JButton jBut_18;
     private javax.swing.JButton jBut_19;
     private javax.swing.JButton jBut_2;
     private javax.swing.JButton jBut_20;
     private javax.swing.JButton jBut_21;
     private javax.swing.JButton jBut_22;
     private javax.swing.JButton jBut_23;
     private javax.swing.JButton jBut_24;
     private javax.swing.JButton jBut_25;
     private javax.swing.JButton jBut_26;
     private javax.swing.JButton jBut_27;
     private javax.swing.JButton jBut_28;
     private javax.swing.JButton jBut_29;
     private javax.swing.JButton jBut_3;
     private javax.swing.JButton jBut_30;
     private javax.swing.JButton jBut_4;
     private javax.swing.JButton jBut_5;
     private javax.swing.JButton jBut_6;
     private javax.swing.JButton jBut_7;
     private javax.swing.JButton jBut_8;
     private javax.swing.JButton jBut_9;
     private javax.swing.JButton jBut_ChangeLector;
     private javax.swing.JButton jBut_ComboB_useItem;
     private javax.swing.JButton jBut_Dozent;
     private javax.swing.JButton jBut_Dozent1;
     private javax.swing.JButton jBut_JA;
     private javax.swing.JButton jBut_NEIN;
     private javax.swing.JButton jBut_OKcheat;
     private javax.swing.JButton jBut_OKlector;
     private javax.swing.JButton jBut_OKnotchanged;
     private javax.swing.JButton jBut_Play;
     private javax.swing.JButton jBut_startShop;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JComboBox jComboB_Items;
     private javax.swing.JLabel jLab_DozCounter;
     private javax.swing.JLabel jLab_DozSwitch;
     private javax.swing.JLabel jLab_DozSwitch1;
     private javax.swing.JLabel jLab_DozSwitch2;
     private javax.swing.JLabel jLab_Duplo;
     private javax.swing.JLabel jLab_Logo;
     private javax.swing.JLabel jLab_OMNI;
     private javax.swing.JLabel jLab_Planning_unused01;
     private javax.swing.JLabel jLab_Planning_unused02;
     private javax.swing.JLabel jLab_Planning_unused03;
     private javax.swing.JLabel jLab_Planning_unused04;
     private javax.swing.JLabel jLab_Planning_unused05;
     private javax.swing.JLabel jLab_Planning_unused06;
     private javax.swing.JLabel jLab_Planning_unused07;
     private javax.swing.JLabel jLab_Planning_unused08;
     private javax.swing.JLabel jLab_Planning_unused09;
     private static javax.swing.JLabel jLab_Redbull;
     private javax.swing.JLabel jLab_StudCounter;
     private javax.swing.JLabel jLab_StudSwitch;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel55;
     private javax.swing.JLabel jLabel58;
     private javax.swing.JLabel jLabel59;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel60;
     private javax.swing.JLabel jLabel61;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel70;
     private javax.swing.JLabel jLabel71;
     private javax.swing.JLabel jLabel72;
     private javax.swing.JLabel jLabel85;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPan_DozSwitch;
     private javax.swing.JPanel jPan_ItemSelect;
     private javax.swing.JPanel jPan_ItemStorage;
     private javax.swing.JPanel jPan_Planning_Play;
     private javax.swing.JPanel jPan_StudSwitch;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel18;
     private javax.swing.JPanel jPanel19;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel20;
     private javax.swing.JPanel jPanel21;
     private javax.swing.JPanel jPanel22;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JProgressBar jProgB_Motivation;
     private javax.swing.JProgressBar jProgB_Müdigkeit;
     private javax.swing.JProgressBar jProgB_Wissen;
     private javax.swing.JProgressBar jProgressBar5;
     private javax.swing.JProgressBar jProgressBar6;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextArea jTextArea2;
     private javax.swing.JTextArea jTextArea3;
     private javax.swing.JTextArea jTextArea4;
     private javax.swing.JToggleButton jToggleBut_SwitchStud;
     private javax.swing.JLabel label_aAccname;
     private javax.swing.JLabel label_aCredits;
     private javax.swing.JLabel label_aDuplo;
     private javax.swing.JLabel label_aEmail;
     private javax.swing.JLabel label_aErrAccName;
     private javax.swing.JLabel label_aErrEmail;
     private javax.swing.JLabel label_aErrFirstName;
     private javax.swing.JLabel label_aErrLastName;
     private javax.swing.JLabel label_aErrPass;
     private javax.swing.JLabel label_aErrUCoins;
     private javax.swing.JLabel label_aFirstName;
     private javax.swing.JLabel label_aGame;
     private javax.swing.JLabel label_aLastName;
     private javax.swing.JLabel label_aMonth;
     private javax.swing.JLabel label_aMonthVal;
     private javax.swing.JLabel label_aOMNI;
     private javax.swing.JLabel label_aPass;
     private javax.swing.JLabel label_aRedBull;
     private javax.swing.JLabel label_aSpicker;
     private javax.swing.JLabel label_aUCions;
     private javax.swing.JLabel label_cheatSheetAmount;
     private javax.swing.JLabel label_cheatSheetImage;
     private javax.swing.JLabel label_cheatSheetLocked;
     private javax.swing.JLabel label_cheatSheetName;
     private javax.swing.JLabel label_cheatSheetOverlay;
     private javax.swing.JLabel label_creditsInv;
     private javax.swing.JLabel label_currentExchange;
     private javax.swing.JLabel label_duploAmount;
     private javax.swing.JLabel label_duploImage;
     private javax.swing.JLabel label_duploLocked;
     private javax.swing.JLabel label_duploName;
     private javax.swing.JLabel label_duploOverlay;
     private javax.swing.JLabel label_inventar;
     private javax.swing.JLabel label_item1;
     private javax.swing.JLabel label_item1Amount;
     private javax.swing.JLabel label_item1Inv;
     private javax.swing.JLabel label_item1InvAmount;
     private javax.swing.JLabel label_item1InvName;
     private javax.swing.JLabel label_item1Name;
     private javax.swing.JLabel label_item2;
     private javax.swing.JLabel label_item2Amount;
     private javax.swing.JLabel label_item2Inv;
     private javax.swing.JLabel label_item2InvAmount;
     private javax.swing.JLabel label_item2InvName;
     private javax.swing.JLabel label_item2Name;
     private javax.swing.JLabel label_item3;
     private javax.swing.JLabel label_item3Amount;
     private javax.swing.JLabel label_item3Inv;
     private javax.swing.JLabel label_item3InvAmount;
     private javax.swing.JLabel label_item3InvName;
     private javax.swing.JLabel label_item3Name;
     private javax.swing.JLabel label_item4;
     private javax.swing.JLabel label_item4Amount;
     private javax.swing.JLabel label_item4Name;
     private javax.swing.JLabel label_lErr;
     private javax.swing.JLabel label_omniAmount;
     private javax.swing.JLabel label_omniAudioImage;
     private javax.swing.JLabel label_omniLocked;
     private javax.swing.JLabel label_omniName;
     private javax.swing.JLabel label_omniOverlay;
     private javax.swing.JLabel label_pEmail1;
     private javax.swing.JLabel label_pEmail2;
     private javax.swing.JLabel label_pErrAccname;
     private javax.swing.JLabel label_pErrEmail1;
     private javax.swing.JLabel label_pErrEmail2;
     private javax.swing.JLabel label_pErrFirstName;
     private javax.swing.JLabel label_pErrLastName;
     private javax.swing.JLabel label_pErrPass1;
     private javax.swing.JLabel label_pErrPass2;
     private javax.swing.JLabel label_pFirstName;
     private javax.swing.JLabel label_pKontoname;
     private javax.swing.JLabel label_pPass1;
     private javax.swing.JLabel label_pPass2;
     private javax.swing.JLabel label_pSecondName;
     private javax.swing.JLabel label_rEmail1;
     private javax.swing.JLabel label_rEmail2;
     private javax.swing.JLabel label_rErrAccname;
     private javax.swing.JLabel label_rErrEmail1;
     private javax.swing.JLabel label_rErrEmail2;
     private javax.swing.JLabel label_rErrFirstName;
     private javax.swing.JLabel label_rErrLastName;
     private javax.swing.JLabel label_rErrPass1;
     private javax.swing.JLabel label_rErrPass2;
     private javax.swing.JLabel label_rFirstName;
     private javax.swing.JLabel label_rKontoname;
     private javax.swing.JLabel label_rPass1;
     private javax.swing.JLabel label_rPass2;
     private javax.swing.JLabel label_rSecondName;
     private javax.swing.JLabel label_redBullAmount;
     private javax.swing.JLabel label_redBullImage;
     private javax.swing.JLabel label_redBullLocked;
     private javax.swing.JLabel label_redBullName;
     private javax.swing.JLabel label_redBullOverlay;
     private javax.swing.JLabel label_returnToPlanningPhase;
     private javax.swing.JLabel label_shopMessage;
     private javax.swing.JLabel label_shopMessage1;
     private javax.swing.JLabel label_shopMessage2;
     private javax.swing.JLabel label_swapper;
     private javax.swing.JLabel label_swapperArrow1;
     private javax.swing.JLabel label_swapperArrow3;
     private javax.swing.JLabel label_swapperCredits;
     private javax.swing.JLabel label_swapperCreditsAmount;
     private javax.swing.JLabel label_swapperUcoins;
     private javax.swing.JLabel label_swapperUcoinsAmount;
     private javax.swing.JLabel label_timer;
     private javax.swing.JLabel label_ucoinsInv;
     private javax.swing.JLabel lable_aProfil;
     private javax.swing.JDialog lectorChangedDialog;
     private javax.swing.JDialog lectorNotChangedDialog;
     private javax.swing.JLabel loggedinas;
     private javax.swing.JLabel login_background;
     private javax.swing.JLabel noSave_overlay;
     private javax.swing.JPanel panel_Admin;
     private javax.swing.JPanel panel_Login;
     private javax.swing.JPanel panel_Profile;
     private javax.swing.JPanel panel_Register;
     private javax.swing.JPanel panel_activityPhaseStudField;
     private javax.swing.JPanel panel_adminUser;
     private javax.swing.JPanel panel_cheatSheet;
     private javax.swing.JPanel panel_duplo;
     private javax.swing.JPanel panel_omniSense;
     private javax.swing.JPanel panel_redBull;
     private javax.swing.JPasswordField password_Pass;
     private javax.swing.JPasswordField password_pPass1;
     private javax.swing.JPasswordField password_pPass2;
     private javax.swing.JPasswordField password_rPass1;
     private javax.swing.JPasswordField password_rPass2;
     private javax.swing.JLabel punkteShop;
     private javax.swing.JPanel shop;
     private javax.swing.JSlider slider_aMonth;
     private javax.swing.JPanel startPlanningGame;
     private javax.swing.JTextField textfield_Kontoname;
     private javax.swing.JTextField textfield_aCredits;
     private javax.swing.JTextField textfield_aDuplo;
     private javax.swing.JTextField textfield_aEmail;
     private javax.swing.JTextField textfield_aKontoname;
     private javax.swing.JTextField textfield_aName;
     private javax.swing.JTextField textfield_aOMNI;
     private javax.swing.JTextField textfield_aPass;
     private javax.swing.JTextField textfield_aRedBull;
     private javax.swing.JTextField textfield_aSurename;
     private javax.swing.JTextField textfield_aUCoins;
     private javax.swing.JTextField textfield_pEmail1;
     private javax.swing.JTextField textfield_pEmail2;
     private javax.swing.JTextField textfield_pKontoname;
     private javax.swing.JTextField textfield_pName;
     private javax.swing.JTextField textfield_pSurename;
     private javax.swing.JTextField textfield_rEmail1;
     private javax.swing.JTextField textfield_rEmail2;
     private javax.swing.JTextField textfield_rKontoname;
     private javax.swing.JTextField textfield_rName;
     private javax.swing.JTextField textfield_rSurename;
     private javax.swing.JTextField textfield_swapperCredits;
     private javax.swing.JTextField textfield_swapperUcoins;
     private javax.swing.JLabel ucoinsShop;
     private javax.swing.JDialog warning;
     // End of variables declaration//GEN-END:variables
 
     //***********************************************************************  /by Dawid
     //*****************LogIn/Register: start*********************************
     //***********************************************************************
     /**
      * Initializes the fields and labels for the register-form
      */
     private void initRegister() {
         label_rErrAccname.setText("");
         label_rErrEmail1.setText("");
         label_rErrEmail2.setText("");
         label_rErrFirstName.setText("");
         label_rErrPass1.setText("");
         label_rErrPass2.setText("");
         label_rErrLastName.setText("");
 
         textfield_rEmail1.setText("");
         textfield_rEmail2.setText("");
         textfield_rKontoname.setText("");
         textfield_rName.setText("");
         textfield_rSurename.setText("");
         password_rPass1.setText("");
         password_rPass2.setText("");
 
         textfield_rEmail1.setBackground(Color.white);
         textfield_rEmail2.setBackground(Color.white);
         textfield_rKontoname.setBackground(Color.white);
         textfield_rName.setBackground(Color.white);
         textfield_rSurename.setBackground(Color.white);
         password_rPass1.setBackground(Color.white);
         password_rPass2.setBackground(Color.white);
     }
 
     /**
      * Checks if the value consists of letters and/or numbers
      *
      * @param str character-array to get checked
      * @return true if there are only letters and/or numbers, false otherwise
      */
     public static boolean checkAlphaNumm(char[] str) {     //checks if a string contains only of numbers and letters
         int i;
         for (i = 0; i < str.length; i++) {
             if (!(Character.isDigit(str[i]) | Character.isLetter(str[i]))) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks if the passed value is a valid email-address (syntax-wise)
      *
      * @param str String with the email-address to check
      * @return true if the email-address is correct, false otherwise
      */
     public static boolean checkEmail(String str) {
         int i;
         boolean atChar = false;
         if (!str.contains(".")) {
             return false;
         }
         for (i = 0; i < str.length(); i++) {
             if (str.charAt(i) == '@') {
                 if (atChar) {
                     return false;
                 } else {
                     atChar = true;
                 }
             }
         }
         if (atChar) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Checks if the passed value is a valid name: -not empty -only letters and
      * "-" allowed
      *
      * @param str String containing the name to check
      * @return true if it is a valid name, false otherwise
      */
     public static boolean checkName(String str) {
         if (str.equals("")) {
             return false;
         }
         int i;
         for (i = 0; i < str.length(); i++) {
             if (!(Character.isLetter(str.charAt(i)) | str.charAt(i) == '-')) {
                 return false;
             }
         }
         return true;
     }
 
     private static boolean checkGlobUserVar(JTextField[] input, JLabel[] errMess) {
         JTextField[] test1 = {new JTextField("t"), input[1]};
         JTextField[] test2 = {input[0], new JTextField("t")};
         return checkGlobUser(test1, errMess) & checkGlobUser(test2, errMess);
     }
 
     /**
      * Checks in the global user-file if the user already exists
      *
      * @param str String with the name of the user to check
      * @return true if user already exists, false otherwise
      */
     public static boolean checkGlobUser(JTextField[] input, JLabel[] errMess) {
 
         try {
             java.util.LinkedList<java.util.LinkedList> list = CSVHandling.readCSV(Sims_1._usersFileName);
             for (java.util.LinkedList sublist : list) {
                 if (sublist.contains(input[0].getText())) {
                     errMess[0].setText("Kontoname schon vergeben!");
                     input[0].setBackground(Color.red);
                     return true;
                 } else {
                     errMess[0].setText("");
                     input[0].setBackground(Color.white);
                 }
                 if (sublist.contains(input[1].getText())) {
                     errMess[1].setText("Email schon in Benutzung!");
                     input[1].setBackground(Color.red);
                     return true;
                 } else {
                     errMess[1].setText("");
                     input[1].setBackground(Color.white);
                 }
             }
         } catch (Exception e) {
             System.err.println(e.getMessage());
         }
         return false;
     }
 
     public static boolean checkRegister_(JComponent[] toCheck, JLabel[] errMess) {
         JTextField account = (JTextField) toCheck[0];
         JPasswordField pass1 = (JPasswordField) toCheck[1];
         JPasswordField pass2 = (JPasswordField) toCheck[2];
         JTextField email1 = (JTextField) toCheck[3];
         JTextField email2 = (JTextField) toCheck[4];
         JTextField name = (JTextField) toCheck[5];
         JTextField lastName = (JTextField) toCheck[6];
 
         boolean check = true;
         if (account.getText().length() < 5) {
             account.setBackground(Color.red);
             errMess[0].setText("Kontoname zu kurz! (5-15 Zeichen)");
             check = false;
         } else if (account.getText().length() > 15) {
             account.setBackground(Color.red);
             errMess[0].setText("Kontoname zu lang! (5-15 Zeichen)");
             check = false;
         } else if (!checkAlphaNumm(account.getText().toCharArray())) {
             account.setBackground(Color.red);
             errMess[0].setText("Kontoname verwendet Sonderzeichen!");
         } else {
             account.setBackground(Color.white);
             errMess[0].setText("");
         }
 
         if (pass1.getPassword().length < 5) {
             pass1.setBackground(Color.red);
             pass2.setText("");
             errMess[1].setText("Passwort zu kurz! (5-15 Zeichen)");
             check = false;
         } else if (pass1.getPassword().length > 15) {
             pass1.setBackground(Color.red);
             pass2.setText("");
             errMess[1].setText("Passwort zu lang! (5-15 Zeichen)");
             check = false;
         } else if (!checkAlphaNumm(pass1.getPassword())) {
             pass1.setBackground(Color.red);
             pass2.setText("");
             errMess[1].setText("Passwort verwendet Sonderzeichen!");
             check = false;
         } else {
             pass1.setBackground(Color.white);
             errMess[1].setText("");
 
 
             if (!new String(pass1.getPassword()).equals(new String(pass2.getPassword()))) {
                 pass1.setBackground(Color.red);
                 pass2.setBackground(Color.red);
                 errMess[2].setText("Passwörter stimmen nicht überein!");
                 check = false;
             } else {
                 pass1.setBackground(Color.white);
                 pass2.setBackground(Color.white);
                 errMess[2].setText("");
             }
         }
 
         if (email1.getText().length() < 5 | !checkEmail(email1.getText())) {
             email1.setBackground(Color.red);
             check = false;
             errMess[3].setText("Ungültige Adresse!");
             if (!email1.equals(email2)) {
                 email2.setText("");
             };
         } else {
             email1.setBackground(Color.white);
             errMess[3].setText("");
 
 
             if (!email1.getText().equals(email2.getText())) {
                 email1.setBackground(Color.red);
                 email2.setBackground(Color.red);
                 errMess[4].setText("Emails stimmen nicht überein!");
                 check = false;
             } else {
                 email1.setBackground(Color.white);
                 email2.setBackground(Color.white);
                 errMess[4].setText("");
             }
         }
 
         if (!checkName(name.getText())) {
             name.setBackground(Color.red);
             errMess[5].setText("Kein gültiger Name!");
             check = false;
         } else {
             name.setBackground(Color.white);
             errMess[5].setText("");
         }
 
         if (!checkName(lastName.getText())) {
             lastName.setBackground(Color.red);
             errMess[6].setText("Kein gültiger Nachname!");
             check = false;
         } else {
             errMess[6].setText("");
             lastName.setBackground(Color.white);
         }
 
         if (check) {
             return true;
         }
         return false;
     }
 
     /**
      * Checks if the entered login-data is correct; user gets feedback to the
      * invalid input after this function is called
      *
      * @param accountname String containing the username
      * @param password Character-array containing the password
      * @return true if the login-data is correct, false otherwise
      */
     private boolean checkLogIn(String accountname, char[] password) {
 
         if (accountname.equals(_adminName) & new String(password).equals(_adminPass)) {
             adminLogin();
             return false;
         }
 
         if (accountname.equals("")) {
             label_lErr.setText("Falscher Benutzername oder falsches Passwort!");
             return false;
         }
         String userlist = "";
         try {
             userlist = CSVHandling.readCSVString(Sims_1._usersFileName);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         if (userlist.contains(accountname)) {
             String userPw = "";
             try {
                 userPw = CSVHandling.readCSVStringArr(Sims_1._dataFolderName + "/" + accountname + "/" + Sims_1._profileFileName)[1];
                 loggedinas.setText("Eingeloggt als: " + accountname); // by nadir , currentuser
             } catch (Exception e) {
                 e.printStackTrace();
             }
             if (userPw.contentEquals(new String(password))) {
                 return true;
             }
         }
         label_lErr.setText("Falscher Benutzername oder falsches Passwort!");
         return false;
     }
 
     /**
      * Logs the user into the program; writes the login-file used to save the
      * user or enable auto-login
      *
      * @param accountname String with the username
      */
     private void logIn(String accountname) {
         String[] strArr = new String[3];
         if (check_saveUser.isSelected()) {
             strArr[0] = textfield_Kontoname.getText();
             strArr[1] = new String(password_Pass.getPassword());
             if (check_autoLogin.isSelected()) {
                 strArr[2] = "1";
             } else {
                 strArr[2] = "0";
             }
         } else {
             strArr[0] = "";
             strArr[1] = "";
             strArr[2] = "0";
         }
         try {
             CSVHandling.writeCSV(strArr, _loginFileName);
         } catch (Exception e) {
             e.printStackTrace();
         }
         _mainuser = new User(accountname);
         _maingame = new Game1();
 //        CSVRead read = new CSVRead();
 //        try {
 //            read.readCSV();
 //        } catch (Exception ex) {
 //            Logger.getLogger(Sims_1.class.getName()).log(Level.SEVERE, null, ex);
 //        }
         panel_Login.setVisible(false);
         startPlanningGame.setVisible(true);
         System.out.println(_mainuser);
     }
 
     /**
      * Standardized login with a fictional user, just for dev-purposes
      */
     private void logIn() {
         _mainuser = new User();  //by Dawid
         _maingame = new Game1();
         CSVRead read = new CSVRead();
         try {
             read.readCSV();
         } catch (Exception ex) {
             Logger.getLogger(Sims_1.class.getName()).log(Level.SEVERE, null, ex);
         }
         panel_Login.setVisible(false);
         panel_Admin.setVisible(false);
         panel_adminUser.setVisible(false);
         startPlanningGame.setVisible(true);
         System.out.println(_mainuser);
     }
 
     private void logOut() {
         _mainuser = null;
         _maingame = null;
         panel_Profile.setVisible(false);
         check_autoLogin.setSelected(false);
         startPlanningGame.setVisible(false);
         panel_Login.setVisible(true);
 
     }
 
     /**
      * Reads the login file and writes the saved values into the login-fields;
      * if the auto-login-flag is set logIn(username) is called
      */
     private void autoLogin() {
         try {
             String[] loginArr = CSVHandling.readCSVStringArr(_loginFileName);
             if (!loginArr[0].equals("")) {
                 check_saveUser.setSelected(true);
             }
             textfield_Kontoname.setText(loginArr[0]);
             password_Pass.setText(loginArr[1]);
             if (loginArr[2].matches("1")) {
                 check_autoLogin.setSelected(true);
                 if (checkLogIn(loginArr[0], loginArr[1].toCharArray())) {
                     logIn(loginArr[0]);
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void adminLogin() {
         panel_Login.setVisible(false);
         panel_Admin.setVisible(true);
         panel_adminUser.setVisible(false);
         JTextField[] textFieldHelp = {textfield_aKontoname, textfield_aPass, textfield_aEmail, textfield_aName, textfield_aSurename, textfield_aUCoins, textfield_aCredits, textfield_aDuplo, textfield_aRedBull, textfield_aOMNI};
         JLabel[] errMess = {label_aErrAccName, label_aErrPass, label_aErrEmail, label_aErrFirstName, label_aErrLastName, label_aErrUCoins};
         _mainadmin = new Admin(panel_Admin, panel_adminUser, textFieldHelp, check_aSpicker, slider_aMonth, errMess); 
         panel_Admin.add(button_aCancel);
         panel_Admin.add(background_admin);
 
     }
 
     private void initProfile() {
         label_pErrEmail1.setText("");
         label_pErrAccname.setText("");
         label_pErrEmail2.setText("");
         label_pErrFirstName.setText("");
         label_pErrLastName.setText("");
         label_pErrPass1.setText("");
         label_pErrPass2.setText("");
 
         textfield_pKontoname.setBackground(Color.white);
         password_pPass1.setBackground(Color.white);
         password_pPass1.setBackground(Color.white);
         textfield_pEmail1.setBackground(Color.white);
         textfield_pEmail2.setBackground(Color.white);
         textfield_pName.setBackground(Color.white);
         textfield_pSurename.setBackground(Color.white);
 
         textfield_pKontoname.setText(_mainuser.getAccountname());
         password_pPass1.setText(_mainuser.getPassword());
         password_pPass2.setText(_mainuser.getPassword());
         textfield_pEmail1.setText(_mainuser.getEmail());
         textfield_pEmail2.setText(_mainuser.getEmail());
         textfield_pName.setText(_mainuser.getFirst_name());
         textfield_pSurename.setText(_mainuser.getLast_name());
     }
     
     private javax.swing.JButton[] constructActivityPhaseButtons(){
         javax.swing.JButton[] result = new javax.swing.JButton[30];
                 result[0]=button_stud1;
                 result[1]=button_stud2;
                 result[2]=button_stud3;
                 result[3]=button_stud4;
                 result[4]=button_stud5;
                 result[5]=button_stud6;
                 result[6]=button_stud7;
                 result[7]=button_stud8;
                 result[8]=button_stud9;
                 result[9]=button_stud10;
                 result[10]=button_stud11;
                 result[11]=button_stud12;
                 result[12]=button_stud13;
                 result[13]=button_stud14;
                 result[14]=button_stud15;
                 result[15]=button_stud16;
                 result[16]=button_stud17;
                 result[17]=button_stud18;
                 result[18]=button_stud19;
                 result[19]=button_stud20;
                 result[20]=button_stud21;
                 result[21]=button_stud22;
                 result[22]=button_stud23;
                 result[23]=button_stud24;
                 result[24]=button_stud25;
                 result[25]=button_stud26;
                 result[26]=button_stud27;
                 result[27]=button_stud28;
                 result[28]=button_stud29;
                 result[29]=button_stud30;              
         return result;
     }
     //***********************************************************************  /by Dawid
     //*****************LogIn/Register: end***********************************
     //***********************************************************************
 }
