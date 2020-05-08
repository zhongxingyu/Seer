 package ui;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 
 import reader.JSPUtil;
 import shared.LoonylandConstant;
 import monster.MonsterDefinition;
 import monster.MonsterDefinitionList;
 
 /*TODO
  * Get default depending on byte in program
  * Make reset work properly 
  */
 @SuppressWarnings("serial")
 public class Profile extends JFrame implements ActionListener, FocusListener {
	JComboBox<String> JSPCombo;
 	MonsterDefinitionList monsterDefinitionList = new MonsterDefinitionList();
 	JButton titleScreen;
 	JButton exePreviewButton;
 	JButton jspResetButton;
 	JButton applyJspButton;
 	ImageIcon ii;
 	JLabel imageLabel = new JLabel();
 	static String[] GIFDIRECTORY = { LoonylandConstant.TOOLBOX_DIRECTORY + "/Gifs//Monster.gif", LoonylandConstant.TOOLBOX_DIRECTORY + "/Gifs//PreviousMonster.gif" };
 	static int FIELDHEIGHT = LoonylandConstant.FIELDHEIGHT;
 	int gifIndex = 0;
 	
 	public static void main(String[] args) {
 		(new File(LoonylandConstant.TOOLBOX_DIRECTORY+"/Gifs")).mkdir();
 		(new File(LoonylandConstant.TOOLBOX_DIRECTORY)).mkdir();
 		
 		Profile frame = new Profile(LoonylandConstant.PROFILENAME);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(950, 620);
 		frame.setResizable(false);
 		frame.setVisible(true);	
 		frame.setLocationRelativeTo(null);
 	}
 	
 	public Profile(String title) {
 		super(title);
 
 		if (!monsterDefinitionList.BuildMonsterDefinition()) {
 			JFrame frame = null;
 			JOptionPane.showMessageDialog(frame, "You must have Loonyland 2 Version 1.0M or 1.2 CE to run this program.", "Version error", JOptionPane.ERROR_MESSAGE);
 			System.exit(0);
 		}
 		ProfileEdit();
 	}
 	
 	public void ProfileEdit(){
 
 		// UI
 		getContentPane().setLayout(null);
 		
 		titleScreen = new JButton("Return to Title Screen"); //Button to go back to Title Screen
 		titleScreen.addActionListener(this);
 		titleScreen.setLocation(10, 570);
 		titleScreen.setSize(930, FIELDHEIGHT);
 
 		
 		JPanel JSPPanel = new JPanel(new GridLayout(3,2));
 		
 		//{"Arena Crystal", "Axe Trap", "Bokbok", "Bonehead", "Clockwork Robot", "Cryozoid", "Furnace", "Ghost", "Kinyova", "Loony", "Mimic", "Nuclear Bokbok", "Prototype AQ40", "Toasties"};
 		int[] validMonsterID = {62, 3, 57, 4, 43, 7, 52 ,35, 0, 70, 58, 64, 16, 2};
 		String[] validMonsterNames = new String[validMonsterID.length];
 		for(int i=0; i<validMonsterID.length; i++){
 			validMonsterNames[i] = monsterDefinitionList.getNameByIndex(validMonsterID[i], false);
 		}
		JSPCombo = new JComboBox<String>(validMonsterNames);
 		JSPCombo.setBackground(Color.white);
 		JSPCombo.setForeground(Color.black);
 		JSPCombo.setLocation(400, 40);
 		JSPCombo.setSize(150, FIELDHEIGHT);
 		JSPCombo.setSelectedIndex(8); //sets to Loony by default
 		JLabel monsterLabel = new JLabel("Monsters:");
 		monsterLabel.setLocation(320, 40);
 		monsterLabel.setSize(100, FIELDHEIGHT);
 
 		jspResetButton = new JButton("Reset");
 		jspResetButton.addActionListener(this);
 		jspResetButton.setLocation(320, 70);
 		jspResetButton.setSize(70, FIELDHEIGHT);
 
 		applyJspButton = new JButton("Apply Changes");
 		applyJspButton.addActionListener(this);
 		applyJspButton.setLocation(400, 70);
 		applyJspButton.setSize(150, FIELDHEIGHT);
 		
 		JLabel warningMessage = new JLabel("Warning: this WILL break some features!");
 		warningMessage.setForeground(Color.RED);
 		warningMessage.setLocation(320, 100);
 		warningMessage.setSize(300, FIELDHEIGHT);
 
 		JSPPanel.setLocation(270, 10);
 		JSPPanel.setSize(320, 120);
 		Border exeBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "JSP For Character");
 		JSPPanel.setBorder(exeBorder);
 
 		getContentPane().add(titleScreen);
 		getContentPane().add(monsterLabel);
 		getContentPane().add(JSPCombo);
 		getContentPane().add(jspResetButton);
 		getContentPane().add(applyJspButton);
 		getContentPane().add(warningMessage);
 		getContentPane().add(JSPPanel);
 
 		JSPCombo.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent ie) {
 				init(true);
 			}
 		});
 		
 		init(true);
 		imageLabel.setIcon(ii);
 		imageLabel.setLocation(10, 10);
 		imageLabel.setSize(250, 300);
 		Border border = BorderFactory.createLineBorder(Color.BLACK);
 		imageLabel.setBorder(border);
 		imageLabel.setOpaque(true);
 		imageLabel.setBackground(Color.WHITE);
 		imageLabel.setVerticalAlignment(JLabel.CENTER);
 		imageLabel.setHorizontalAlignment(JLabel.CENTER);
 		getContentPane().add(imageLabel);
 	}
 
 	public void focusGained(FocusEvent e) {
 	}
 
 	public void focusLost(FocusEvent e) {
 	}
 
 	public void actionPerformed(ActionEvent evt) {
 		//String index = ((String) JSPCombo.getSelectedItem());
 		
 		if (evt.getSource() == titleScreen){
 			Title frame = new Title(LoonylandConstant.TITLENAME);
 			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			frame.setSize(LoonylandConstant.WINDOWSIZE[0], LoonylandConstant.WINDOWSIZE[1]);
 			frame.setResizable(false);
 			frame.setVisible(true);	
 			dispose();
 		} else if (evt.getSource() == jspResetButton) {
 			// redraw
 			init(false);
 		} else if (evt.getSource() == applyJspButton) {
 			//FileUtile fileUtile = new FileUtile();
 			try {
 				//add writing to .exe
 			} catch (Exception e) {
 				e.printStackTrace();
 			}	
 		}
 	}
 
 	private void init(boolean changedMonster) {
 		int index = monsterDefinitionList.getIndexByName((String) JSPCombo.getSelectedItem(), false);
 		MonsterDefinition md = MonsterDefinitionList.allDefinitions[index];
 		
 		//write gif
		File f = new File(md.getJspName(true));
 		JSPUtil util = new JSPUtil(f, LoonylandConstant.TOOLDIR);
 		String gifName = GIFDIRECTORY[(gifIndex++) % 2]; //gets directory in which to print gif
		util.writeGif(gifName);
 		ii = new ImageIcon(gifName);
 		imageLabel.setIcon(ii);
 	}
 }
