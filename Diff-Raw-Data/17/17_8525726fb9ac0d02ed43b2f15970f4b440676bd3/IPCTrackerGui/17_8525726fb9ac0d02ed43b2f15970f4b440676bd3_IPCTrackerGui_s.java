 package com.kronius.IPCTracker;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StringReader;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.miginfocom.swing.MigLayout;
 
 public class IPCTrackerGui implements ActionListener, KeyListener{
 	
 	private static String OS;
 	private static String CLASS_NAME;
 	
 	private JFrame f;
 	private static Image winsowsApplicationIcon;
 	
 	private JLabel lblSU;
 	private JLabel lblGer;
 	private JLabel lblUK;
 	private JLabel lblJap;
 	private JLabel lblUS;
 	
 	private JLabel lblTotalSU;
 	private JLabel lblTotalGer;
 	private JLabel lblTotalUK;
 	private JLabel lblTotalJap;
 	private JLabel lblTotalUS;
 
 	private JTextArea taSU;
 	private JTextArea taGer;
 	private JTextArea taUK;
 	private JTextArea taJap;
 	private JTextArea taUS;
 	
 	private JScrollPane sbrSU;
 	private JScrollPane sbrGer;
 	private JScrollPane sbrUK;
 	private JScrollPane sbrJap;
 	private JScrollPane sbrUS;
 	
 	private JTextField tfSU;
 	private JTextField tfGer;
 	private JTextField tfUK;
 	private JTextField tfJap;
 	private JTextField tfUS;
 	
     private JButton btnUpdate;
         
     private ButtonGroup btnGrpVictoryCondition;
     
     private JLabel lblVictoryCondition;
     
     private JLabel lblVictoryConditionCitiesToWin;
 	@SuppressWarnings("unused")
 	private ButtonGroup btnGrpVictoryCities;
     private JRadioButton rdioBtnVictoryConditionNine;
     private JRadioButton rdioBtnVictoryConditionTwelve;
     
     private JLabel lblVictoryCityWashington;
     
     private ButtonGroup btnGrpVictoryCityWashington;
     private JRadioButton rdioBtnVictoryCityWashingtonAxis;
     private JRadioButton rdioBtnVictoryCityWashingtonAllies;
     
     private JLabel lblVictoryCityLondon;
     
     private ButtonGroup btnGrpVictoryCityLondon;
     private JRadioButton rdioBtnVictoryCityLondonAxis;
     private JRadioButton rdioBtnVictoryCityLondonAllies;
     
     private JLabel lblVictoryCityLeningrad;
         
     private ButtonGroup btnGrpVictoryCityLeningrad;
     private JRadioButton rdioBtnVictoryCityLeningradAxis;
     private JRadioButton rdioBtnVictoryCityLeningradAllies;
     
     private JLabel lblVictoryCityMoscow;
     
     private ButtonGroup btnGrpVictoryCityMoscow;
     private JRadioButton rdioBtnVictoryCityMoscowAxis;
     private JRadioButton rdioBtnVictoryCityMoscowAllies;
     
     private JLabel lblVictoryCityCalcutta;
     
     private ButtonGroup btnGrpVictoryCityCalcutta;
     private JRadioButton rdioBtnVictoryCityCalcuttaAxis;
     private JRadioButton rdioBtnVictoryCityCalcuttaAllies;
     
     private JLabel lblVictoryCityLosAngeles;
     
     private ButtonGroup btnGrpVictoryCityLosAngeles;
     private JRadioButton rdioBtnVictoryCityLosAngelesAxis;
     private JRadioButton rdioBtnVictoryCityLosAngelesAllies;
     
     private JLabel lblVictoryCityBerlin;
     
     private ButtonGroup btnGrpVictoryCityBerlin;
     private JRadioButton rdioBtnVictoryCityBerlinAxis;
     private JRadioButton rdioBtnVictoryCityBerlinAllies;
     
     private JLabel lblVictoryCityParis;
     
     private ButtonGroup btnGrpVictoryCityParis;
     private JRadioButton rdioBtnVictoryCityParisAxis;
     private JRadioButton rdioBtnVictoryCityParisAllies;
     
     private JLabel lblVictoryCityRome;
     
     private ButtonGroup btnGrpVictoryCityRome;
     private JRadioButton rdioBtnVictoryCityRomeAxis;
     private JRadioButton rdioBtnVictoryCityRomeAllies;
     
     private JLabel lblVictoryCityShanghai;
     
     private ButtonGroup btnGrpVictoryCityShanghai;
     private JRadioButton rdioBtnVictoryCityShanghaiAxis;    
     private JRadioButton rdioBtnVictoryCityShanghaiAllies;
     
     private JLabel lblVictoryCityManila;
     
     private ButtonGroup btnGrpVictoryCityManila;
     private JRadioButton rdioBtnVictoryCityManilaAxis;
     private JRadioButton rdioBtnVictoryCityManilaAllies;
     
     private JLabel lblVictoryCityTokyo;
     
     private ButtonGroup btnGrpVictoryCityTokyo;
     private JRadioButton rdioBtnVictoryCityTokyoAxis;
     private JRadioButton rdioBtnVictoryCityTokyoAllies;
     
     private JButton btnVictoryCheck;
     
     private JMenuBar mb;
     private JMenu mnuFile;
     private JMenuItem mnuItemNew;
     private JMenuItem mnuItemOpen;
     private JMenuItem mnuItemSave;
     private JMenuItem mnuItemQuit;
     private JMenu mnuHelp;
     private JMenuItem mnuItemAbout;
     
     private JFileChooser fileChooser;
 	
 	private BasicLogger logger;
 	
 	private int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
 		
 	private IPCTrackerGui(){		
 		
 		f = new JFrame(IPCTrackerKeys.Strings.AppNameFull);
 		
 		if(OS.startsWith(IPCTrackerKeys.OS.Win)){
 			f.setIconImage(winsowsApplicationIcon);
 		}
 		
 		logger = BasicLogger.getInstance();
 		
 		lblSU = new JLabel(IPCTrackerKeys.Strings.SovietUnion);
 		lblGer = new JLabel(IPCTrackerKeys.Strings.Germany);
 		lblUK = new JLabel(IPCTrackerKeys.Strings.UnitedKingdom);
 		lblJap = new JLabel(IPCTrackerKeys.Strings.Japan);
 		lblUS = new JLabel(IPCTrackerKeys.Strings.UnitedStates);
 		
 		lblTotalSU = new JLabel(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueSU);
 		lblTotalGer = new JLabel(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueGer);
 		lblTotalUK = new JLabel(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueUK);
 		lblTotalJap = new JLabel(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueJap);
 		lblTotalUS = new JLabel(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueUS);
 		
 	    taSU = new JTextArea("+" + IPCTrackerKeys.Strings.StartingValueSU, 20, 5);
         taSU.setLineWrap(true);
         taSU.setEditable(false);
 	    taGer = new JTextArea("+" + IPCTrackerKeys.Strings.StartingValueGer, 20, 5);
 	    taGer.setLineWrap(true);
 	    taGer.setEditable(false);
 	    taUK = new JTextArea("+" + IPCTrackerKeys.Strings.StartingValueUK, 20, 5);
 	    taUK.setLineWrap(true);
 	    taUK.setEditable(false);
 	    taJap = new JTextArea("+" + IPCTrackerKeys.Strings.StartingValueJap, 20, 5);
 	    taJap.setLineWrap(true);
 	    taJap.setEditable(false);
 	    taUS = new JTextArea("+" + IPCTrackerKeys.Strings.StartingValueUS, 20, 0);
 	    taUS.setLineWrap(true);
 	    taUS.setEditable(false);
 	    
 	    sbrSU = new JScrollPane(taSU);
 	    sbrSU.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	    sbrGer = new JScrollPane(taGer);
 	    sbrGer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	    sbrUK = new JScrollPane(taUK);
 	    sbrUK.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	    sbrJap = new JScrollPane(taJap);
 	    sbrJap.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	    sbrUS = new JScrollPane(taUS);
 	    sbrUS.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	    
 	    tfSU = new JTextField(5);
 	    tfGer = new JTextField(5);
 	    tfUK = new JTextField(5);
 	    tfJap = new JTextField(5);
 	    tfUS = new JTextField(5);  
 
 	    btnUpdate = new JButton(IPCTrackerKeys.Strings.UpdateButtonText);
 	    
 	    lblVictoryCondition = new JLabel(IPCTrackerKeys.VictoryStrings.VictoryCondition);
 	    
 	    lblVictoryConditionCitiesToWin = new JLabel(IPCTrackerKeys.VictoryStrings.VictoryConditionCitiesToWin);
 	    
 	    btnGrpVictoryCondition = new ButtonGroup();
 	    
 	    rdioBtnVictoryConditionNine = new JRadioButton(IPCTrackerKeys.VictoryStrings.VictoryConditionNine);
 	    rdioBtnVictoryConditionNine.setSelected(true);
 	    rdioBtnVictoryConditionTwelve = new JRadioButton(IPCTrackerKeys.VictoryStrings.VictoryConditionTwelve);
 	    
 	    btnGrpVictoryCondition.add(rdioBtnVictoryConditionNine);
 	    btnGrpVictoryCondition.add(rdioBtnVictoryConditionTwelve);
 	    
 	    lblVictoryCityWashington = new JLabel(IPCTrackerKeys.VictoryStrings.Washington);
 	    
 	    btnGrpVictoryCityWashington = new ButtonGroup();
 	    rdioBtnVictoryCityWashingtonAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityWashingtonAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    rdioBtnVictoryCityWashingtonAllies.setSelected(true);
 	    
 	    btnGrpVictoryCityWashington.add(rdioBtnVictoryCityWashingtonAxis);
 	    btnGrpVictoryCityWashington.add(rdioBtnVictoryCityWashingtonAllies);
 	    
 	    lblVictoryCityLondon = new JLabel(IPCTrackerKeys.VictoryStrings.London);
 	    
 	    btnGrpVictoryCityLondon = new ButtonGroup();
 	    rdioBtnVictoryCityLondonAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityLondonAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    rdioBtnVictoryCityLondonAllies.setSelected(true);
 	    
 	    btnGrpVictoryCityLondon.add(rdioBtnVictoryCityLondonAxis);
 	    btnGrpVictoryCityLondon.add(rdioBtnVictoryCityLondonAllies);
 	    
 	    lblVictoryCityLeningrad = new JLabel(IPCTrackerKeys.VictoryStrings.Leningrad);
 	    
 	    btnGrpVictoryCityLeningrad = new ButtonGroup();
 	    rdioBtnVictoryCityLeningradAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityLeningradAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    rdioBtnVictoryCityLeningradAllies.setSelected(true);
 	    
 	    btnGrpVictoryCityLeningrad.add(rdioBtnVictoryCityLeningradAxis);
 	    btnGrpVictoryCityLeningrad.add(rdioBtnVictoryCityLeningradAllies);
 	    
 	    lblVictoryCityMoscow = new JLabel(IPCTrackerKeys.VictoryStrings.Moscow);
 	    
 	    btnGrpVictoryCityMoscow = new ButtonGroup();
 	    rdioBtnVictoryCityMoscowAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityMoscowAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    rdioBtnVictoryCityMoscowAllies.setSelected(true);
 	    
 	    btnGrpVictoryCityMoscow.add(rdioBtnVictoryCityMoscowAxis);
 	    btnGrpVictoryCityMoscow.add(rdioBtnVictoryCityMoscowAllies);
 	    
 	    lblVictoryCityCalcutta = new JLabel(IPCTrackerKeys.VictoryStrings.Calcutta);
 	    
 	    btnGrpVictoryCityCalcutta = new ButtonGroup();
 	    rdioBtnVictoryCityCalcuttaAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityCalcuttaAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    rdioBtnVictoryCityCalcuttaAllies.setSelected(true);
 	    
 	    btnGrpVictoryCityCalcutta.add(rdioBtnVictoryCityCalcuttaAxis);
 	    btnGrpVictoryCityCalcutta.add(rdioBtnVictoryCityCalcuttaAllies);
 
 	    lblVictoryCityLosAngeles = new JLabel(IPCTrackerKeys.VictoryStrings.LosAngeles);
 	    
 	    btnGrpVictoryCityLosAngeles = new ButtonGroup();
 	    rdioBtnVictoryCityLosAngelesAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityLosAngelesAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    rdioBtnVictoryCityLosAngelesAllies.setSelected(true);
 	    
 	    btnGrpVictoryCityLosAngeles.add(rdioBtnVictoryCityLosAngelesAxis);
 	    btnGrpVictoryCityLosAngeles.add(rdioBtnVictoryCityLosAngelesAllies);
 
 	    lblVictoryCityBerlin = new JLabel(IPCTrackerKeys.VictoryStrings.Berlin);
 	    
 	    btnGrpVictoryCityBerlin = new ButtonGroup();
 	    rdioBtnVictoryCityBerlinAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityBerlinAxis.setSelected(true);
 	    rdioBtnVictoryCityBerlinAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    
 	    btnGrpVictoryCityBerlin.add(rdioBtnVictoryCityBerlinAxis);
 	    btnGrpVictoryCityBerlin.add(rdioBtnVictoryCityBerlinAllies);
 
 	    lblVictoryCityParis = new JLabel(IPCTrackerKeys.VictoryStrings.Paris);
 	    
 	    btnGrpVictoryCityParis = new ButtonGroup();
 	    rdioBtnVictoryCityParisAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityParisAxis.setSelected(true);
 	    rdioBtnVictoryCityParisAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    
 	    btnGrpVictoryCityParis.add(rdioBtnVictoryCityParisAxis);
 	    btnGrpVictoryCityParis.add(rdioBtnVictoryCityParisAllies);
 
 	    lblVictoryCityRome = new JLabel(IPCTrackerKeys.VictoryStrings.Rome);
 	    
 	    btnGrpVictoryCityRome = new ButtonGroup();
 	    rdioBtnVictoryCityRomeAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityRomeAxis.setSelected(true);
 	    rdioBtnVictoryCityRomeAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    
 	    btnGrpVictoryCityRome.add(rdioBtnVictoryCityRomeAxis);
 	    btnGrpVictoryCityRome.add(rdioBtnVictoryCityRomeAllies);
 
 	    lblVictoryCityShanghai = new JLabel(IPCTrackerKeys.VictoryStrings.Shanghai);
 	    
 	    btnGrpVictoryCityShanghai = new ButtonGroup();
 	    rdioBtnVictoryCityShanghaiAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityShanghaiAxis.setSelected(true);
 	    rdioBtnVictoryCityShanghaiAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    
 	    btnGrpVictoryCityShanghai.add(rdioBtnVictoryCityShanghaiAxis);
 	    btnGrpVictoryCityShanghai.add(rdioBtnVictoryCityShanghaiAllies);
     
 	    lblVictoryCityManila = new JLabel(IPCTrackerKeys.VictoryStrings.Manila);
 	    
 	    btnGrpVictoryCityManila = new ButtonGroup();
 	    rdioBtnVictoryCityManilaAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityManilaAxis.setSelected(true);
 	    rdioBtnVictoryCityManilaAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    
 	    btnGrpVictoryCityManila.add(rdioBtnVictoryCityManilaAxis);
 	    btnGrpVictoryCityManila.add(rdioBtnVictoryCityManilaAllies);
 
 	    lblVictoryCityTokyo = new JLabel(IPCTrackerKeys.VictoryStrings.Tokyo);
 	    
 	    btnGrpVictoryCityTokyo = new ButtonGroup();
 	    rdioBtnVictoryCityTokyoAxis = new JRadioButton(IPCTrackerKeys.VictoryStrings.Axis);
 	    rdioBtnVictoryCityTokyoAxis.setSelected(true);
 	    rdioBtnVictoryCityTokyoAllies = new JRadioButton(IPCTrackerKeys.VictoryStrings.Allies);
 	    
 	    btnGrpVictoryCityTokyo.add(rdioBtnVictoryCityTokyoAxis);
 	    btnGrpVictoryCityTokyo.add(rdioBtnVictoryCityTokyoAllies);
 
 	    btnVictoryCheck = new JButton(IPCTrackerKeys.Strings.VictoryCheckButtonText);
 	    
 	    mb = new JMenuBar();
 	    mnuFile = new JMenu(IPCTrackerKeys.Strings.FileMenuText);
 	    mnuItemNew = new JMenuItem(IPCTrackerKeys.Strings.NewMenuItemText);
 	    mnuItemNew.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, MENU_MASK));
 	    mnuItemOpen = new JMenuItem(IPCTrackerKeys.Strings.OpenMenuItemText);
 	    mnuItemOpen.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, MENU_MASK));
 	    mnuItemSave = new JMenuItem(IPCTrackerKeys.Strings.SaveMenuItemText);
 	    mnuItemSave.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, MENU_MASK));
 	    mnuItemQuit = new JMenuItem(IPCTrackerKeys.Strings.QuitMenuitemText);
 	    mnuItemQuit.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, MENU_MASK));
 	    mnuHelp = new JMenu(IPCTrackerKeys.Strings.HelpMenuText);
 	    mnuItemAbout = new JMenuItem(IPCTrackerKeys.Strings.AboutMenuItemText);
 	    mnuItemAbout.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, MENU_MASK));
 	    
 	    fileChooser = new JFileChooser();
 	    
 	    //Add Menu listeners
 	    mnuItemNew.addActionListener(this);
 	    mnuItemOpen.addActionListener(this);
 	    mnuItemSave.addActionListener(this);
         mnuItemQuit.addActionListener(this);
         mnuItemAbout.addActionListener(this);
         
         //Add Button listener
         btnUpdate.addActionListener(this);
         btnVictoryCheck.addActionListener(this);
         
         //Add Key Listeners
         tfSU.addKeyListener(this);
         tfGer.addKeyListener(this);
         tfUK.addKeyListener(this);
         tfJap.addKeyListener(this);
         tfUS.addKeyListener(this);
 	}
 	
	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == btnUpdate){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Update Button");
 			}
 			updateTotals();
 		}else if(e.getSource() == btnVictoryCheck){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Victory Check Button");
 			}
 			victoryCheck();
 		}else if(e.getSource() == mnuItemNew){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: New Game Button");
 			}
 			createNewGame();
 		}else if(e.getSource() == mnuItemOpen){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Open Button");
 			}
 			loadState();
 		}else if(e.getSource() == mnuItemSave){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Save Button");
 			}
 			saveState();
 		}else if (e.getSource() == mnuItemQuit){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Quitting via menu Button");
 			}
 			System.exit(0);
 		}else if (e.getSource() == mnuItemAbout){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Showing About Message");
 			}
 			JOptionPane.showMessageDialog(f, IPCTrackerKeys.Strings.AboutMessage, IPCTrackerKeys.Strings.AboutMessageTitle, 
 					0, new ImageIcon("res" + File.separator + "Illuminati.png"));
 		}else{
 			logger.log(CLASS_NAME, "Showing Unknown event Message" + e.getSource().toString());
 			JOptionPane.showMessageDialog(f, "Unknown event: " + e.getSource().toString(), "Error", 0, new ImageIcon("res" + File.separator + "error.png"));
 		}
 	}
 	
	
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		// Ignore
 	}
 

 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// Ignore
 	}
 

 	@Override
 	public void keyTyped(KeyEvent key) {
 
 		if(key.getKeyChar() == '\n'){
 			String selectedTextAreaName = "";
 			
 			if(key.getSource() == tfSU){
 				selectedTextAreaName = "tfSU";
 			} else if(key.getSource() == tfGer){
 				selectedTextAreaName = "tfGer";
 			} else if(key.getSource() == tfUK){
 				selectedTextAreaName = "tfUK";
 			} else if(key.getSource() == tfJap){
 				selectedTextAreaName = "tfJap";
 			} else if(key.getSource() == tfUS){
 				selectedTextAreaName = "tfUS";
 			} else {
 				selectedTextAreaName = "Unknown Text Field";
 			}
 			
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Update via Enter / Return Key from Text Field: " + selectedTextAreaName);
 			}
 			
 			updateTotals();
 		}
 	}
 
	
 	private void createNewGame() {
 		
 		lblTotalSU.setText(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueSU);
 		lblTotalGer.setText(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueGer);
 		lblTotalUK.setText(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueUK);
 		lblTotalJap.setText(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueJap);
 		lblTotalUS.setText(IPCTrackerKeys.Strings.Total + IPCTrackerKeys.Strings.StartingValueUS);
 		
 	    taSU.setText("+" + IPCTrackerKeys.Strings.StartingValueSU);
 	    taGer.setText("+" + IPCTrackerKeys.Strings.StartingValueGer);
 	    taUK.setText("+" + IPCTrackerKeys.Strings.StartingValueUK);
 	    taJap.setText("+" + IPCTrackerKeys.Strings.StartingValueJap);
 	    taUS.setText("+" + IPCTrackerKeys.Strings.StartingValueUS);
 	    
 	    rdioBtnVictoryConditionNine.setSelected(true);
 	    
 	    rdioBtnVictoryCityWashingtonAllies.setSelected(true);
 	    rdioBtnVictoryCityLondonAllies.setSelected(true);
 	    rdioBtnVictoryCityLeningradAllies.setSelected(true);
 	    rdioBtnVictoryCityMoscowAllies.setSelected(true);
 	    rdioBtnVictoryCityCalcuttaAllies.setSelected(true);
 	    rdioBtnVictoryCityLosAngelesAllies.setSelected(true);
 	    rdioBtnVictoryCityBerlinAxis.setSelected(true);
 	    rdioBtnVictoryCityParisAxis.setSelected(true);
 	    rdioBtnVictoryCityRomeAxis.setSelected(true);
 	    rdioBtnVictoryCityShanghaiAxis.setSelected(true);
 	    rdioBtnVictoryCityManilaAxis.setSelected(true);
 	    rdioBtnVictoryCityTokyoAxis.setSelected(true);
 		
 	}
 
 	private void loadState() {
 		
 		String filePath = "Save1.ipctracker";
 		
 		int returnVal = fileChooser.showOpenDialog(mnuItemOpen);
 		 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fileChooser.getSelectedFile();
             filePath = file.getPath().trim();
             logger.log(CLASS_NAME, "Opening: " + filePath + ".");
         } else {
         	logger.log("Load command cancelled by user.");
             return;
         }
 		
 		IPCTrackerData loadedData = null;
 		FileInputStream fis = null;
 		ObjectInputStream in = null;
 	    
 		try
 	    {
 	    	fis = new FileInputStream(filePath);
 	    	in = new ObjectInputStream(fis);
 	    	loadedData = (IPCTrackerData)in.readObject(); 
 	    	in.close();
 	    }
 	    catch(IOException ex)
 	    {
 	    	ex.printStackTrace();
 	    }
 	    catch(ClassNotFoundException ex)
 	    {
 	    	ex.printStackTrace();
 	    }
 		
 		//Update fields and labels from loaded data
 		lblTotalSU.setText(IPCTrackerKeys.Strings.Total + loadedData.getSUTotal());
 		lblTotalGer.setText(IPCTrackerKeys.Strings.Total + loadedData.getGerTotal());
 		lblTotalUK.setText(IPCTrackerKeys.Strings.Total + loadedData.getUKTotal());
 		lblTotalJap.setText(IPCTrackerKeys.Strings.Total + loadedData.getJapTotal());
 		lblTotalUS.setText(IPCTrackerKeys.Strings.Total + loadedData.getUSTotal());
 		
 		List<String> TransactionHistorySU = loadedData.getTransactionHistorySU();
 		List<String> TransactionHistoryGer = loadedData.getTransactionHistoryGer();
 		List<String> TransactionHistoryUK = loadedData.getTransactionHistoryUK();
 		List<String> TransactionHistoryJap = loadedData.getTransactionHistoryJap();
 		List<String> TransactionHistoryUS = loadedData.getTransactionHistoryUS();
 		
 		String transHistSU = "";
 		String transHistGer = "";
 		String transHistUK = "";
 		String transHistJap = "";
 		String transHistUS = "";
 		
 		for(String s : TransactionHistorySU){
 			transHistSU = transHistSU + s + "\n";
 		}
 		
 		for(String s : TransactionHistoryGer){
 			transHistGer = transHistGer + s + "\n";
 		}
 		
 		for(String s : TransactionHistoryUK){
 			transHistUK = transHistUK + s + "\n";
 		}
 		
 		for(String s : TransactionHistoryJap){
 			transHistJap = transHistJap + s + "\n";
 		}
 		
 		for(String s : TransactionHistoryUS){
 			transHistUS = transHistUS + s + "\n";
 		}
 		
 		taSU.setText(transHistSU.trim());
 		taGer.setText(transHistGer.trim());
 		taUK.setText(transHistUK.trim());
 		taJap.setText(transHistJap.trim());
 		taUS.setText(transHistUS.trim());
 	}
 
 	private void saveState() {
 		
 		String filePath = "Save1.ipctracker";
 		
 		int returnVal = fileChooser.showSaveDialog(mnuItemSave);
 		
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fileChooser.getSelectedFile();
             
             filePath = file.getPath().trim();
             
             if(!filePath.endsWith(".ipctracker")){
     			filePath = filePath + ".ipctracker";
     		}
             
             logger.log(CLASS_NAME, "Saving: " + filePath + ".");
         } else {
             logger.log(CLASS_NAME, "Save command cancelled by user.");
             return;
         }
 		
 		IPCTrackerData dataToSave = new IPCTrackerData();
 		
 		dataToSave.setSUTotal(Integer.parseInt(lblTotalSU.getText().trim().substring(7, lblTotalSU.getText().trim().length())));
 		dataToSave.setGerTotal(Integer.parseInt(lblTotalGer.getText().trim().substring(7, lblTotalGer.getText().trim().length())));
 		dataToSave.setUKTotal(Integer.parseInt(lblTotalUK.getText().trim().substring(7, lblTotalUK.getText().trim().length())));
 		dataToSave.setJapTotal(Integer.parseInt(lblTotalJap.getText().trim().substring(7, lblTotalJap.getText().trim().length())));
 		dataToSave.setUSTotal(Integer.parseInt(lblTotalUS.getText().trim().substring(7, lblTotalUS.getText().trim().length())));
 		
 		List<String> TransactionHistorySU = new LinkedList<String>();
 		List<String> TransactionHistoryGer = new LinkedList<String>();
 		List<String> TransactionHistoryUK = new LinkedList<String>();
 		List<String> TransactionHistoryJap = new LinkedList<String>();
 		List<String> TransactionHistoryUS = new LinkedList<String>();
 		
 		String transHistSU = taSU.getText().trim();
 		String transHistGer = taGer.getText().trim();
 		String transHistUK = taUK.getText().trim();
 		String transHistJap = taJap.getText().trim();
 		String transHistUS = taUS.getText().trim();
 		
 		String reader = "";
 		BufferedReader br = null;
 		
 		try {
 			br = new BufferedReader(new StringReader(transHistSU));
 		    while ((reader = br.readLine()) != null) { // while loop begins here
 		    	TransactionHistorySU.add(reader.trim());
 		    } // end while 
 		} // end try
 		catch (IOException e) {
 			System.err.println("Error: " + e);
 		}
 		
 		reader = "";
 		br = null;
 		
 		try {
 			br = new BufferedReader(new StringReader(transHistGer));
 		    while ((reader = br.readLine()) != null) { // while loop begins here
 		    	TransactionHistoryGer.add(reader.trim());
 		    } // end while 
 		} // end try
 		catch (IOException e) {
 			System.err.println("Error: " + e);
 		}
 		
 		reader = "";
 		br = null;
 		
 		try {
 			br = new BufferedReader(new StringReader(transHistUK));
 		    while ((reader = br.readLine()) != null) { // while loop begins here
 		    	TransactionHistoryUK.add(reader.trim());
 		    } // end while 
 		} // end try
 		catch (IOException e) {
 			System.err.println("Error: " + e);
 		}
 		
 		reader = "";
 		br = null;
 		
 		try {
 			br = new BufferedReader(new StringReader(transHistJap));
 		    while ((reader = br.readLine()) != null) { // while loop begins here
 		    	TransactionHistoryJap.add(reader.trim());
 		    } // end while 
 		} // end try
 		catch (IOException e) {
 			System.err.println("Error: " + e);
 		}
 		
 		reader = "";
 		br = null;
 		
 		try {
 			br = new BufferedReader(new StringReader(transHistUS));
 		    while ((reader = br.readLine()) != null) { // while loop begins here
 		    	TransactionHistoryUS.add(reader.trim());
 		    } // end while 
 		} // end try
 		catch (IOException e) {
 			System.err.println("Error: " + e);
 		}
 		
 		dataToSave.setTransactionHistorySU(TransactionHistorySU);
 		dataToSave.setTransactionHistoryGer(TransactionHistoryGer);
 		dataToSave.setTransactionHistoryUK(TransactionHistoryUK);
 		dataToSave.setTransactionHistoryJap(TransactionHistoryJap);
 		dataToSave.setTransactionHistoryUS(TransactionHistoryUS);
 				
 		FileOutputStream fos = null;
 		ObjectOutputStream out = null;
 				
 		try
 		{
 			fos = new FileOutputStream(filePath);
 		    out = new ObjectOutputStream(fos);
 		    out.writeObject(dataToSave);
 		    out.close();
 		}
 		catch(IOException ex)
 		{
 			ex.printStackTrace();
 		}
 		
 	}
 	
 	private void victoryCheck() {
 		//TODO finish victory check
 		boolean isVictoryConditionNine = rdioBtnVictoryConditionNine.isSelected();
 		
 		int numberOfAxisControlledCities = 0;
 		
 		if(rdioBtnVictoryCityWashingtonAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityLondonAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityLeningradAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityMoscowAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityCalcuttaAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityLosAngelesAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityBerlinAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityParisAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityRomeAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityShanghaiAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityManilaAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		if(rdioBtnVictoryCityTokyoAxis.isSelected()){
 			numberOfAxisControlledCities++;
 		}
 		
 		String sideThatWon = "Axis";
 		int numberOfAlliesControlledCities = 12 - numberOfAxisControlledCities;
 		String icon = "avatar_gr_icon.gif";
 		
 		if(isVictoryConditionNine){
 			if(numberOfAxisControlledCities > 8){
 				sideThatWon = "Axis";
 				icon = "avatar_gr_icon.gif";
 			}else if(numberOfAxisControlledCities < 4){
 				sideThatWon = "Allies";
 				icon = "avatar_usa_icon.gif";
 			}
 			else{
 				return; //If there is no winner do nothing.
 			}
 		}else{
 			if(numberOfAxisControlledCities > 11){
 				sideThatWon = "Axis";
 				icon = "avatar_gr_icon.gif";
 			}else if (numberOfAxisControlledCities < 1){
 				sideThatWon = "Allies";
 				icon = "avatar_usa_icon.gif";
 			}
 			else{
 				return; //If there is no winner do nothing.
 			}
 		}
 		
 		JOptionPane.showMessageDialog(f, sideThatWon + " Wins!\nWith a score of:\n"
 				+ numberOfAxisControlledCities + " cities for Axis\nand:\n"
 				+ numberOfAlliesControlledCities + " cities for Allies" , sideThatWon
 				+ " Wins!", 0, new ImageIcon("res" + File.separator + icon));
 	}
 	
 	private void updateTotals() {
 		
 		String SUtext = tfSU.getText().trim(); 
 		String Gertext = tfGer.getText().trim(); 
 		String UKtext = tfUK.getText().trim(); 
 		String Japtext = tfJap.getText().trim(); 
 		String UStext = tfUS.getText().trim(); 
 		
 		if(!SUtext.isEmpty()){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Attempting to update SU with: \"" + SUtext + "\"");
 			}
 			updateTotalByCountry(IPCTrackerKeys.CountryCodes.SU, SUtext);
 		}
 		
 		if(!Gertext.isEmpty()){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Attempting to update Ger with: \"" + Gertext + "\"");
 			}
 			updateTotalByCountry(IPCTrackerKeys.CountryCodes.Ger, Gertext);
 		}
 		
 		if(!UKtext.isEmpty()){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Attempting to update UK with: \"" + UKtext + "\"");
 			}
 			updateTotalByCountry(IPCTrackerKeys.CountryCodes.UK, UKtext);
 		}
 		
 		if(!Japtext.isEmpty()){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Attempting to update Jap with: \"" + Japtext + "\"");
 			}
 			updateTotalByCountry(IPCTrackerKeys.CountryCodes.Jap, Japtext);
 		}
 		
 		if(!UStext.isEmpty()){
 			if(IPCTrackerKeys.DEBUG_STATUS){
 				logger.log(CLASS_NAME, "DEBUG: Attempting to update US with: \"" + UStext + "\"");
 			}
 			updateTotalByCountry(IPCTrackerKeys.CountryCodes.US, UStext);
 		}
 	}
 	
	
 	private void updateTotalByCountry(int countryCode, String updateText){
 		
 		char c = updateText.charAt(0);
 		
 		switch(c){
 			case '+': 
 				if(IPCTrackerKeys.DEBUG_STATUS){
 					logger.log(CLASS_NAME, "DEBUG: Adding");
 				}
 				addition(countryCode, Integer.parseInt(updateText.substring(1, updateText.length())));
 				break;
 			case '-': 
 				if(IPCTrackerKeys.DEBUG_STATUS){
 					logger.log(CLASS_NAME, "DEBUG: Subtracting");
 				}
 				subtraction(countryCode, Integer.parseInt(updateText.substring(1, updateText.length())));
 				break;
 			default :
 				
 				if(IPCTrackerKeys.DEBUG_STATUS){
 					logger.log(CLASS_NAME, "DEBUG: Adding (Without Operator)");
 				}
 				addition(countryCode, Integer.parseInt(updateText));
 				break;
 		}
 	}
 		
 	private void addition(int countryCode, int numberToAdd){
 		if(IPCTrackerKeys.DEBUG_STATUS){
 			logger.log(CLASS_NAME, "DEBUG: Add");
 		}
 		
 		switch(countryCode){
 			case IPCTrackerKeys.CountryCodes.SU:
 				logger.log(CLASS_NAME, "Adding: \"" + numberToAdd + "\" to SU");
 				int currentTotalSU = Integer.parseInt(lblTotalSU.getText().substring(7 , lblTotalSU.getText().length()));
 				int newTotalSU = currentTotalSU + numberToAdd;
 				tfSU.setText("");
 				taSU.append("\n+" + numberToAdd);
 				lblTotalSU.setText(IPCTrackerKeys.Strings.Total + newTotalSU);
 				break;
 			case IPCTrackerKeys.CountryCodes.Ger:
 				logger.log(CLASS_NAME, "Adding: \"" + numberToAdd + "\" to Ger");
 				int currentTotalGer = Integer.parseInt(lblTotalGer.getText().substring(7 , lblTotalGer.getText().length()));
 				int newTotalGer = currentTotalGer + numberToAdd;
 				tfGer.setText("");
 				taGer.append("\n+" + numberToAdd);
 				lblTotalGer.setText(IPCTrackerKeys.Strings.Total + newTotalGer);
 				break;
 			case IPCTrackerKeys.CountryCodes.UK:
 				logger.log(CLASS_NAME, "Adding: \"" + numberToAdd + "\" to UK");
 				int currentTotalUK = Integer.parseInt(lblTotalUK.getText().substring(7 , lblTotalUK.getText().length()));
 				int newTotalUK = currentTotalUK + numberToAdd;
 				tfUK.setText("");
 				taUK.append("\n+" + numberToAdd);
 				lblTotalUK.setText(IPCTrackerKeys.Strings.Total + newTotalUK);
 				break;
 			case IPCTrackerKeys.CountryCodes.Jap:
 				logger.log(CLASS_NAME, "Adding: \"" + numberToAdd + "\" to Jap");
 				int currentTotalJap = Integer.parseInt(lblTotalJap.getText().substring(7 , lblTotalJap.getText().length()));
 				int newTotalJap = currentTotalJap + numberToAdd;
 				tfJap.setText("");
 				taJap.append("\n+" + numberToAdd);
 				lblTotalJap.setText(IPCTrackerKeys.Strings.Total + newTotalJap);
 				break;
 			case IPCTrackerKeys.CountryCodes.US:
 				logger.log(CLASS_NAME, "Adding: \"" + numberToAdd + "\" to US");
 				int currentTotalUS = Integer.parseInt(lblTotalUS.getText().substring(7 , lblTotalUS.getText().length()));
 				int newTotalUS = currentTotalUS + numberToAdd;
 				tfUS.setText("");
 				taUS.append("\n+" + numberToAdd);
 				lblTotalUS.setText(IPCTrackerKeys.Strings.Total + newTotalUS);
 				break;
 			default:
 				logger.log(CLASS_NAME, "Country Code: \"" + countryCode + "\" does not exist.");
 				break;
 		}
 	}
 	
	
 	private void subtraction(int countryCode, int numberToMinus){
 		if(IPCTrackerKeys.DEBUG_STATUS){
 			logger.log(CLASS_NAME, "DEBUG: Minus");
 		}
 		
 		switch(countryCode){
 			case IPCTrackerKeys.CountryCodes.SU:
 				logger.log(CLASS_NAME, "Subtracting: \"" + numberToMinus + "\" from SU");
 				int currentTotalSU = Integer.parseInt(lblTotalSU.getText().substring(7 , lblTotalSU.getText().length()));
 				int newTotalSU = currentTotalSU - numberToMinus;
 				tfSU.setText("");
 				taSU.append("\n-" + numberToMinus);
 				lblTotalSU.setText(IPCTrackerKeys.Strings.Total + newTotalSU);
 				break;
 			case IPCTrackerKeys.CountryCodes.Ger:
 				logger.log(CLASS_NAME, "Subtracting: \"" + numberToMinus + "\" from Ger");
 				int currentTotalGer = Integer.parseInt(lblTotalGer.getText().substring(7 , lblTotalGer.getText().length()));
 				int newTotalGer = currentTotalGer - numberToMinus;
 				tfGer.setText("");
 				taGer.append("\n-" + numberToMinus);
 				lblTotalGer.setText(IPCTrackerKeys.Strings.Total + newTotalGer);
 				break;
 			case IPCTrackerKeys.CountryCodes.UK:
 				logger.log(CLASS_NAME, "Subtracting: \"" + numberToMinus + "\" from UK");
 				int currentTotalUK = Integer.parseInt(lblTotalUK.getText().substring(7 , lblTotalUK.getText().length()));
 				int newTotalUK = currentTotalUK - numberToMinus;
 				tfUK.setText("");
 				taUK.append("\n-" + numberToMinus);
 				lblTotalUK.setText(IPCTrackerKeys.Strings.Total + newTotalUK);
 				break;
 			case IPCTrackerKeys.CountryCodes.Jap:
 				logger.log(CLASS_NAME, "Subtracting: \"" + numberToMinus + "\" from Jap");
 				int currentTotalJap = Integer.parseInt(lblTotalJap.getText().substring(7 , lblTotalJap.getText().length()));
 				int newTotalJap = currentTotalJap - numberToMinus;
 				tfJap.setText("");
 				taJap.append("\n-" + numberToMinus);
 				lblTotalJap.setText(IPCTrackerKeys.Strings.Total + newTotalJap);
 				break;
 			case IPCTrackerKeys.CountryCodes.US:
 				logger.log(CLASS_NAME, "Subtracting: \"" + numberToMinus + "\" from US");
 				int currentTotalUS = Integer.parseInt(lblTotalUS.getText().substring(7 , lblTotalUS.getText().length()));
 				int newTotalUS = currentTotalUS - numberToMinus;
 				tfUS.setText("");
 				taUS.append("\n-" + numberToMinus);
 				lblTotalUS.setText(IPCTrackerKeys.Strings.Total + newTotalUS);
 				break;
 			default:
 				logger.log(CLASS_NAME, "Country Code: \"" + countryCode + "\" does not exist.");
 				break;
 		}
 	}
 	

 	private void launchFrame(){
 		
 		f.getContentPane().setLayout(new BorderLayout());
 		
 		// Set menubar
         f.setJMenuBar(mb);
                 
 		//Build Menus
         mnuFile.add(mnuItemNew);
         mnuFile.add(mnuItemOpen);
         mnuFile.add(mnuItemSave);
         
         if(!OS.equals(IPCTrackerKeys.OS.Mac)){
         	mnuFile.add(mnuItemQuit);
         }
         
         mnuHelp.add(mnuItemAbout);
         mb.add(mnuFile);
         mb.add(mnuHelp);
 		
 		f.getContentPane().add(buildMiGDashboard(), BorderLayout.CENTER);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.pack(); //Adjusts panel to components for display
 		f.setVisible(true);
 		f.setResizable (false);
         
 		logger.setLogName("IPCTracker.log");
         
         if(IPCTrackerKeys.DEBUG_STATUS){
         	logger.log(CLASS_NAME, "DEBUG: Frame Loaded");
         }
 	}
 	
	
 	private JPanel buildMiGDashboard() {
         JPanel panel = new JPanel();
         panel.setLayout(new MigLayout("wrap", "[fill]15[fill]15[fill]15[fill]15[fill]15[fill]", "rel[]rel"));
         buildMiGForm(panel);
         return panel;
     }
 	
 	private JPanel buildInnerMiGDashboard() {
         JPanel innerPanel = new JPanel();
         innerPanel.setLayout(new MigLayout("wrap 4", "", "1[]1"));
         buildInnerMiGForm(innerPanel);
         return innerPanel;
     }
 
     private void buildMiGForm(JPanel panel) {
         panel.add(lblSU, "");
         panel.add(lblGer, " ");
         panel.add(lblUK, "");
         panel.add(lblJap, "");
         panel.add(lblUS, "");
               
         panel.add(new JLabel(""), "");  
         
         panel.add(lblTotalSU, "");
         panel.add(lblTotalGer, "");
         panel.add(lblTotalUK, "");
         panel.add(lblTotalJap, "");
         panel.add(lblTotalUS, "");
         
         panel.add(new JLabel(""), "");
         
         panel.add(sbrSU, "");
         panel.add(sbrGer, "");
         panel.add(sbrUK, "");
         panel.add(sbrJap, "");
         panel.add(sbrUS, "");
          
         
         panel.add(buildInnerMiGDashboard());  
         
         panel.add(tfSU, "");
         panel.add(tfGer, "");
         panel.add(tfUK, "");
         panel.add(tfJap, "");
         panel.add(tfUS, "");
         
         panel.add(btnVictoryCheck, "");  
         
         panel.add(new JLabel(""));
         panel.add(new JLabel(""));
         panel.add(new JLabel(""));
         panel.add(new JLabel(""));
         panel.add(btnUpdate, "");
          
         panel.add(new JLabel(""));
     }
     
     private void buildInnerMiGForm(JPanel innerPanel) {
     	innerPanel.add(lblVictoryCondition, "span");
     	
     	innerPanel.add(lblVictoryConditionCitiesToWin, "span 2");
     	innerPanel.add(rdioBtnVictoryConditionNine, "");
     	innerPanel.add(rdioBtnVictoryConditionTwelve, "");
 
     	innerPanel.add(lblVictoryCityWashington, "span 2");
     	innerPanel.add(rdioBtnVictoryCityWashingtonAxis, "");
     	innerPanel.add(rdioBtnVictoryCityWashingtonAllies, "");
     	
     	innerPanel.add(lblVictoryCityLondon, "span 2");
     	innerPanel.add(rdioBtnVictoryCityLondonAxis, "");
     	innerPanel.add(rdioBtnVictoryCityLondonAllies, "");
 
     	innerPanel.add(lblVictoryCityLeningrad, "span 2");
     	innerPanel.add(rdioBtnVictoryCityLeningradAxis, "");
     	innerPanel.add(rdioBtnVictoryCityLeningradAllies, "");
 
     	innerPanel.add(lblVictoryCityMoscow, "span 2");
     	innerPanel.add(rdioBtnVictoryCityMoscowAxis, "");
     	innerPanel.add(rdioBtnVictoryCityMoscowAllies, "");
     	
     	innerPanel.add(lblVictoryCityCalcutta, "span 2");
     	innerPanel.add(rdioBtnVictoryCityCalcuttaAxis, "");
     	innerPanel.add(rdioBtnVictoryCityCalcuttaAllies, "");
     	
     	innerPanel.add(lblVictoryCityLosAngeles, "span 2");
     	innerPanel.add(rdioBtnVictoryCityLosAngelesAxis, "");
     	innerPanel.add(rdioBtnVictoryCityLosAngelesAllies, "");
     	
     	innerPanel.add(lblVictoryCityBerlin, "span 2");
     	innerPanel.add(rdioBtnVictoryCityBerlinAxis, "");
     	innerPanel.add(rdioBtnVictoryCityBerlinAllies, "");
     	
     	innerPanel.add(lblVictoryCityParis, "span 2");
     	innerPanel.add(rdioBtnVictoryCityParisAxis, "");
     	innerPanel.add(rdioBtnVictoryCityParisAllies, "");
 
     	innerPanel.add(lblVictoryCityRome, "span 2");
     	innerPanel.add(rdioBtnVictoryCityRomeAxis, "");
     	innerPanel.add(rdioBtnVictoryCityRomeAllies, "");
 
     	innerPanel.add(lblVictoryCityShanghai, "span 2");
     	innerPanel.add(rdioBtnVictoryCityShanghaiAxis, "");
     	innerPanel.add(rdioBtnVictoryCityShanghaiAllies, "");
 
     	innerPanel.add(lblVictoryCityManila, "span 2");
     	innerPanel.add(rdioBtnVictoryCityManilaAxis, "");
     	innerPanel.add(rdioBtnVictoryCityManilaAllies, "");
 
     	innerPanel.add(lblVictoryCityTokyo, "span 2");
     	innerPanel.add(rdioBtnVictoryCityTokyoAxis, "");
     	innerPanel.add(rdioBtnVictoryCityTokyoAllies, "");	
     }
 
 	public static void main(String args[]){
 		
 		BasicLogger osLogger = BasicLogger.getInstance();
 		osLogger.setLogName("IPCTracker.log");
 		
 		CLASS_NAME = IPCTrackerGui.class.getName();
 		
 		osLogger.log(CLASS_NAME, "Application Version: " + IPCTrackerKeys.Strings.ApplicationVersion);
 		
 		OS = System.getProperty("os.name");
 		
 		if(OS.equals(IPCTrackerKeys.OS.Mac)){
 			osLogger.log(CLASS_NAME, "Current OS is: " + IPCTrackerKeys.OS.Mac);
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 			System.setProperty("com.apple.mrj.application.apple.menu.about.name", IPCTrackerKeys.Strings.AppName);
 		}
 		else if(OS.startsWith(IPCTrackerKeys.OS.Win)){
 			osLogger.log(CLASS_NAME, "Current OS is: " + IPCTrackerKeys.OS.Win);
 			//Windows specific properties
 			winsowsApplicationIcon = Toolkit.getDefaultToolkit().getImage("res/AppIcon.png");
 		}else{
 			//Linux or unkown
 			osLogger.log(CLASS_NAME, "Current OS is: " + OS);
 			osLogger.log(CLASS_NAME, IPCTrackerKeys.OS.OSNotSupportedMessage);
 			JOptionPane.showMessageDialog(new JFrame(), IPCTrackerKeys.OS.OSNotSupportedMessage, IPCTrackerKeys.OS.OSNotSupportedMessageTitle, 
 					0, new ImageIcon("res" + File.separator + "error.png"));
 			
 			System.exit(0); 
 		}
 		
 		IPCTrackerGui gui = new IPCTrackerGui();
         gui.launchFrame();
 	}
 }
