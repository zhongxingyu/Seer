 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 public class LauncherView extends JPanel {
 	private FighterPool model;
 	private JPanel playerOnePanel, playerTwoPanel;
 	private JButton[] p1Character, p2Character;
 	private JButton startGame;
 	private JTextArea p1Desc, p2Desc;
 
 	public LauncherView(FighterPool m) {
 		model = m;
 		// general layout stuff
 		GridBagLayout generalLayout = new GridBagLayout();
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.insets = new Insets(5, 5, 5, 5); // default spacing
 		setLayout(generalLayout);
 
 		// Panels
 		// player one
 		playerOnePanel = new JPanel();
 		playerOnePanel
 				.setBorder(BorderFactory.createTitledBorder("Player One"));
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.weightx = 1;
 		constraints.weighty = 1;
 		generalLayout.setConstraints(playerOnePanel, constraints);
 		add(playerOnePanel);
 
 		// player two
 		playerTwoPanel = new JPanel();
 		playerTwoPanel
 				.setBorder(BorderFactory.createTitledBorder("Player Two"));
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.weightx = 1;
 		constraints.weighty = 1;
 		generalLayout.setConstraints(playerTwoPanel, constraints);
 		add(playerTwoPanel);
 
 		// buttons
 		startGame = new JButton("START!");
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 2;
 		constraints.gridheight = 1;
 		constraints.fill = GridBagConstraints.NONE;
 		constraints.weightx = 0;
 		constraints.weighty = 0;
 		generalLayout.setConstraints(startGame, constraints);
 		add(startGame);
 
 		// p1 panel
 		// p1 layout
 		GridBagLayout player1Layout = new GridBagLayout();
 		GridBagConstraints p1Const = new GridBagConstraints();
 		p1Const.insets = new Insets(5, 5, 5, 5); // default spacing
 		playerOnePanel.setLayout(player1Layout);
 
 		// buttons
		p1Character = new JButton[model.getFighters.length];
 		for (int i = 0; i < p1Character.length; i++) {
 			p1Character[i] = new JButton("Test" + i);
 			p1Const.gridx = i;
 			p1Const.gridy = 0;
 			p1Const.gridwidth = 1;
 			p1Const.gridheight = 1;
 			p1Const.fill = GridBagConstraints.NONE;
 			p1Const.weightx = 0;
 			p1Const.weighty = 0;
 			player1Layout.setConstraints(p1Character[i], p1Const);
 			playerOnePanel.add(p1Character[i]);
 		}
 
 		// p2 panel
 		// p2 layout
 		GridBagLayout player2Layout = new GridBagLayout();
 		GridBagConstraints p2Const = new GridBagConstraints();
 		p2Const.insets = new Insets(5, 5, 5, 5); // default spacing
 		playerTwoPanel.setLayout(player2Layout);
 
 		// buttons
		p2Character = new JButton[model.getFighters.length];
 		for (int i = 0; i < p2Character.length; i++) {
 			p2Character[i] = new JButton("Test" + i);
 			p2Const.gridx = i;
 			p2Const.gridy = 0;
 			p2Const.gridwidth = 1;
 			p2Const.gridheight = 1;
 			p2Const.fill = GridBagConstraints.NONE;
 			p2Const.weightx = 0;
 			p2Const.weighty = 0;
 			player2Layout.setConstraints(p2Character[i], p2Const);
 			playerTwoPanel.add(p2Character[i]);
 		}
 	}
 	
 	public JButton getStart() { return startGame; }
 	public JButton[] getP1Array() { return p1Character; }
 	public JButton[] getP2Array() { return p2Character; }
 }
