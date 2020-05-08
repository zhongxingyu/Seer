 package edu.gatech.cs2340.ui;
 
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import edu.gatech.cs2340.data.Player;
 import edu.gatech.cs2340.sequencing.WaitedOn;
 
 
 /**
  * 
  * @author Madeleine North
  * 
  *         Created for: M5 9/30/13 Modifications: M5 10/6/13 Shreyyas Vanarase
  *         Updating panel viewability and features M5 10/6/13 Thomas Mark Fixed
  *         parameter input
  * 
  * 
  *         Purpose: Second panel of game configuration menu. Allows user to
  *         select game player names, player races, and player colors. This
  *         information is passed back to Driver to start a new Game.
  */
 
 public class PlayerConfigMenu extends JPanel implements WaitedOn {
 	private static final long serialVersionUID = 1L;
 
 	public static String[] races = { "Bonzoid", "Buzzite", "Flapper ", "Ugaite" };
 
	public static String[] colorStrings = {"Blue","Gold","Green","Red"};
 	public static Color[] colors = {new Color(0,0,255), new Color(0,255,255), new Color(0,255,0), new Color(255,0,0)};
 
 	private boolean finished;
 	private Player[] players;
 	
 	private JTextField[] nameFields;
 	private JComboBox<String>[] raceLists;
	private JComboBox<String>[] colorsLists;
 
 	/**
 	 * Main constructor
 	 * 
 	 * @param manager
 	 *            GUIManager to handle callback from "Next" button
 	 */
 	public PlayerConfigMenu(int numPairs) {
 		finished = false;
 
 		// Create and populate the panel
 		SpringLayout layout = new SpringLayout();
 
 		this.setLayout(layout);
 		
 		nameFields = new JTextField[numPairs];
 		raceLists = new JComboBox[numPairs];
 		colorsLists = new JComboBox[numPairs];
 
 		// make a label and text field for each player so that they can input
 		// their names
 		for (int i = 0; i < numPairs; i++) {
 			JLabel nameLabel = new JLabel("Player "+ i + " Name: ", JLabel.TRAILING);
 			this.add(nameLabel);
 			JTextField nameField = new JTextField(10);
 			nameLabel.setLabelFor(nameField);
 			this.add(nameField);
 			nameFields[i] = nameField;
 
 			JLabel raceLabel = new JLabel(" Race: ", JLabel.TRAILING);
 			this.add(raceLabel);
 			JComboBox<String> raceList = new JComboBox<String>(races);
 			raceLabel.setLabelFor(raceList);
 			this.add(raceList);
 			raceLists[i] = raceList;
 
 			JLabel colorLabel = new JLabel(" Color: ", JLabel.TRAILING);
 			this.add(colorLabel);
 			
			JComboBox<String> colorsList = new JComboBox<String>(colorStrings);
 			colorLabel.setLabelFor(colorsList);
 			this.add(colorsList);
 			colorsLists[i] = colorsList;
 		}
 
 		this.add(new JPanel());
 		this.add(new JPanel());
 		JButton startButton = new JButton("Start");
 		startButton.addActionListener(new startListener());
 		this.add(startButton);
 		this.add(new JPanel());
 		this.add(new JPanel());
 		this.add(new JPanel());
 
 		// Lay out the panel.
 		SpringUtilities.makeCompactGrid(this,
                 numPairs +1 , 6, //rows, cols
                 5, 5, //initialX, initialY
                 5, 5);//xPad, yPad
 	}
 	
 	public Player[] getPlayers() {
 		return players;
 	}
 
 	/**
 	 * Listener class for "Start" button. Passes message to GUIManager.
 	 * 
 	 */
 	private class startListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int numPairs = nameFields.length;
 			players = new Player[numPairs];
 			for (int i=0; i<numPairs; i++) {
 				String name = nameFields[i].getText();
 				String race = raceLists[i].getSelectedItem().toString();
				Color color = colors[colorsLists[i].getSelectedIndex()];
 				players[i] = new Player(name, race, color);
 			}
 			finished = true;
 		}
 	}
 
 	@Override
 	public void run() {
 	}
 
 	@Override
 	public boolean isFinished() {
 		return finished;
 	}
 }
