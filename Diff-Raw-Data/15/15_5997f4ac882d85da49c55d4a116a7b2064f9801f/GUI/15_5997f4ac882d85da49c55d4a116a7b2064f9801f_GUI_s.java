 package clueGame;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 public class GUI extends JFrame {
 
 	public GUI() {
 
 		setSize(800, 800);
 		setTitle("Clue Game");
 		add(new Board(), BorderLayout.CENTER);
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(createFileMenu());
 		setJMenuBar(menuBar);
 		
 		
 
 	}
 
 	private JMenu createFileMenu() {
 		JMenu menu = new JMenu("Menu");
 		menu.add(createFileExitItem());
 		menu.add(createDetectiveNotesWindow());
 		return menu;
 	}
 
 	
 	
 	private JMenuItem createFileExitItem() {
 		JMenuItem exit = new JMenuItem("Exit");
 		class MenuItemListener implements ActionListener {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		}
 		exit.addActionListener(new MenuItemListener());
 		return exit;
 	}
 
	private DetectiveNotesDialog dialog;
 	
 	private JMenuItem createDetectiveNotesWindow() {
 		
 		JMenuItem detectiveNotes = new JMenuItem("Detective Notes");
 		class MenuItemListener implements ActionListener {
 			public void actionPerformed(ActionEvent e) {
				
				dialog = new DetectiveNotesDialog();
 				dialog.setVisible(true);
 
 			}
 			
 		}
 		detectiveNotes.addActionListener(new MenuItemListener());
 		return detectiveNotes;
 	}
 
 
 	private class DetectiveChecksPanel extends JPanel{
 
 		public DetectiveChecksPanel(){
 
 			add(new DetectivePeopleChecksPanel());
 			add(new DetectiveRoomChecksPanel());
 			add(new DetectiveWeaponChecksPanel());
 			setLayout(new GridLayout(0,1));
 			
 
 		}
 
 	}
 
 	private class DetectivePeopleChecksPanel extends JPanel{
 		private JCheckBox scarlett, mustard, peacack, white, green, plum;
 
 		DetectivePeopleChecksPanel(){
 			scarlett = new JCheckBox("Miss Scarlett");
 			mustard = new JCheckBox("Colonel Mustard");
 			peacack = new JCheckBox("Mrs. Peacock");
 			white = new JCheckBox("Mrs. White");
 			green = new JCheckBox("Reverend Green");
 			plum = new JCheckBox("Professor Plum");
 
 			add(scarlett);
 			add(mustard);
 			add(peacack);
 			add(white);
 			add(green);
 			add(plum);
 			setLayout(new GridLayout(3,2));
 			setBorder(new TitledBorder(new EtchedBorder(), "People"));
 
 
 		}
 	}
 
 	private class DetectiveRoomChecksPanel extends JPanel{
 
 
 		private JCheckBox kitchen, dining, lounge, ballroom, conservatory, hall, study, library, billiard;
 
 
 		public DetectiveRoomChecksPanel(){
 			kitchen = new JCheckBox("Kitchen");
 			dining = new JCheckBox("Dining Room");
 			lounge = new JCheckBox("Lounge");
 			ballroom = new JCheckBox("Ballroom");
 			conservatory = new JCheckBox("Conservatory");
 			hall = new JCheckBox("Hall");
 			study = new JCheckBox("Study");
 			library = new JCheckBox("Library");
 			billiard= new JCheckBox("Billiard Room");
 
 			add(kitchen);
 			add(dining);				
 			add(lounge);
 			add(ballroom);
 			add(conservatory);
 			add(hall);
 			add(study);				
 			add(library);
 			add(billiard);
 			setLayout(new GridLayout(0,2));
 			setBorder(new TitledBorder(new EtchedBorder(), "Rooms"));				
 
 		}
 
 
 	}
 
 
 
 
 
 
 private class DetectiveWeaponChecksPanel extends JPanel{
 	private JCheckBox candlestick, knife, leadpipe, revolver, rope, wrench;
 
 	DetectiveWeaponChecksPanel(){
 		candlestick = new JCheckBox("CandleStick");
 		knife = new JCheckBox("Knife");
 		leadpipe = new JCheckBox("Lead Pipe");
 		revolver = new JCheckBox("Revolver");
 		rope = new JCheckBox("Rope");
 		wrench = new JCheckBox("Wrench");
 
 		add(candlestick);
 		add(knife);
 		add(leadpipe);
 		add(revolver);
 		add(rope);
 		add(wrench);
 		setLayout(new GridLayout(3,2));
 		setBorder(new TitledBorder(new EtchedBorder(), "Weapons"));
 
 
 	}
 }
 	public class DetectiveNotesDialog extends JDialog{
 
 		public DetectiveNotesDialog(){
 			setTitle("Detective Notes");
 			setSize(500, 500);
 			add(new DetectiveChecksPanel());
 			add(new DetectiveGuessComboPanel());
 			setLayout(new GridLayout(1,2));
 		}
 
 	}
 
 	private class DetectiveGuessComboPanel extends JPanel{
 		
 		
 		public DetectiveGuessComboPanel(){
 			
 			JComboBox personCombo = new JComboBox();
 			JComboBox roomCombo = new JComboBox();
 			JComboBox weaponCombo = new JComboBox();
 			
 			personCombo.addItem("Miss scarlett");
 			personCombo.addItem("Colonel Mustard");
 			personCombo.addItem("Mrs. Peacack");
 			personCombo.addItem("Mrs. White");
 			personCombo.addItem("Reverend Green");
 			personCombo.addItem("Professor Plum");
 			
 			
 			roomCombo.addItem("Kitchen");
 			roomCombo.addItem("Dining Room");
 			roomCombo.addItem("Lounge");
 			roomCombo.addItem("Ballroom");
 			roomCombo.addItem("Conservatory");
 			roomCombo.addItem("Hall");
 			roomCombo.addItem("Study");
 			roomCombo.addItem("Library");
 			roomCombo.addItem("Billiard Room");
 			
 			
 			weaponCombo.addItem("Candlestick");
 			weaponCombo.addItem("Knife");
 			weaponCombo.addItem("Lead Pipe");
 			weaponCombo.addItem("Revolver");
 			weaponCombo.addItem("Rope");
 			weaponCombo.addItem("Wrench");
 			
 			add(personCombo);
 			add(roomCombo);
 			add(weaponCombo);
 			
 			personCombo.setBorder(new TitledBorder(new EtchedBorder(), "People Guess"));			
 			roomCombo.setBorder(new TitledBorder(new EtchedBorder(), "Room Guess"));
 			weaponCombo.setBorder(new TitledBorder(new EtchedBorder(), "Weapon Guess"));
 			
 			
 			setLayout( new GridLayout(3,0));
 			
 		}
 		
 	}
 	
 
 
 	public static void main(String args[]) {
 		GUI clueBoard = new GUI();
 		clueBoard.setVisible(true);
 		clueBoard.setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 
 }
