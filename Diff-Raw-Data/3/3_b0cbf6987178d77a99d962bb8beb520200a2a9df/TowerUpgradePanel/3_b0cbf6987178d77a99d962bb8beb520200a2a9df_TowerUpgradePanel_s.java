 package src.ui.side;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import src.core.TargetingInfo;
 import src.core.Tower;
 import src.core.Upgrade;
 import src.ui.controller.GameController;
 
 /**
  * A display area which presents the selected tower's statistics and upgrade options
  */
 public class TowerUpgradePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	private Color defaultButtonBackground; // we need to store this because we change the colors of some buttons
 	
 	private GameController controller;
 	private TowerStatsPanel towerStats;
 	private ElementalUpgradePanel elementalUpgrade;
 	
 	private JLabel levelOneLabel;
 	private JLabel levelTwoLabel;
 	private JLabel levelThreeLabel;
 	
 	private JButton sellTowerButton;
 	private JButton cancelButton;
 	private JButton strongestButton, weakestButton, closestButton, furthestButton;
 	private JButton[][] upgradeButtons;
 
 	public TowerUpgradePanel(GameController gc) {
 		super(new GridBagLayout());
 		controller = gc;
 		
 		towerStats = new TowerStatsPanel(controller);
 		towerStats.setTower(controller.getSelectedTower());
 		
 		// set up the buttons to handle changing targeting strategy
 		Font targetingButtonFont = new Font("Default", Font.PLAIN, 8);
 		Insets targetingButtonInsets = new Insets(0, 5, 0, 5);
 		
 		strongestButton = new JButton("STRONG");
 		strongestButton.setFont(targetingButtonFont);
 		strongestButton.setMargin(targetingButtonInsets);
 		strongestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.STRONGEST);
 			}
 		});
 		
 		weakestButton = new JButton("WEAK");
 		weakestButton.setFont(targetingButtonFont);
 		weakestButton.setMargin(targetingButtonInsets);
 		weakestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.WEAKEST);
 			}
 		});
 		
 		closestButton = new JButton("CLOSE");
 		closestButton.setFont(targetingButtonFont);
 		closestButton.setMargin(targetingButtonInsets);
 		closestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.CLOSEST);
 			}
 		});
 		
 		furthestButton = new JButton("FAR");
 		furthestButton.setFont(targetingButtonFont);
 		furthestButton.setMargin(targetingButtonInsets);
 		furthestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.FURTHEST);
 			}
 		});
 		
 		sellTowerButton = new JButton("Sell");
 		sellTowerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.sellTower();
 			}
 		});
 		
 		cancelButton = new JButton("Cancel Upgrading");
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.unselectTower();
 			}
 		});
 		
 		elementalUpgrade = new ElementalUpgradePanel(gc);
 		
 		levelOneLabel = new JLabel("1");
 		levelTwoLabel = new JLabel("2");
 		levelThreeLabel = new JLabel("3");
 		
 		upgradeButtons = new JButton[3][3];
 		
 		// lay out components
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.anchor = GridBagConstraints.CENTER;	
 		c.insets.set(10, 0, 0, 0);
 		c.gridx = 0;
 		c.gridwidth = 4;
 		c.gridy = 0;
 		add(towerStats, c);
 		c.gridwidth = 1;
 		
 		c.anchor = GridBagConstraints.CENTER;
 		c.insets = new Insets(10, 0, 10, 0);	
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 4;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		add(elementalUpgrade, c);
 		c.gridwidth = 1;
 		
 		// initialize a helper panel to hold the tower strategy buttons
 		// this makes laying them out nicely much easier
 		JPanel holder = new JPanel(new GridBagLayout());
 		c.gridx = 0;
 		c.insets.set(0, 5, 0, 5);
 		holder.add(strongestButton, c);
 		c.gridx = 1;
 		holder.add(weakestButton, c);
 		c.gridx = 2;
 		holder.add(closestButton, c);
 		c.gridx = 3;
 		c.insets.set(0, 5, 0, 0);
 		holder.add(furthestButton, c);
 		c.insets.set(0, 0, 0, 0);
 		
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 4;
 		c.insets.set(0, 0, 10, 0);
 		add(holder, c);
 		c.insets.set(0, 0, 0, 0);
 		c.gridwidth = 1;
 		
 		c.insets.set(0, 10, 0, 10);
 		c.gridx = 0;
 		c.gridy = 3;
 		add(levelThreeLabel, c);
 		
 		c.gridx = 0;
 		c.gridy = 4;
 		add(levelTwoLabel, c);
 		
 		c.gridx = 0;
 		c.gridy = 5;
 		add(levelOneLabel, c);
 		c.insets.set(0, 0, 0, 0);
 		
 		// initialize and lay out each of the upgrade buttons
 		for (int l = 0; l < 3; l++) { // there are 3 levels
 			for (int n = 0; n < 3; n++) { // and 3 upgrades in each level
 				final int level = l + 1;
 				final int idx = n;
 				
 				JButton upgradeButton = new JButton("up" + Integer.toString(level));
 				
 				upgradeButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						controller.applyTowerUpgrade(level, idx);
 					}
 				});
 				
 				upgradeButton.addMouseListener(new MouseAdapter() {
 					public void mouseEntered(MouseEvent e) {
						if (e.getComponent().isEnabled())
 							towerStats.setUpgrade(controller.getTowerUpgrade(level, idx));
 					}
 					
 					public void mouseExited(MouseEvent e) {
 							towerStats.setUpgrade(null);
 					}
 				});
 				
 				c.gridx = idx + 1;
 				c.gridy = 5 - level + 1;
 				add(upgradeButton, c);
 				
 				upgradeButtons[l][n] = upgradeButton;
 			}
 		}
 
 		c.insets = new Insets(10, 0, 0, 0);	
 		c.gridx = 0;
 		c.gridy = 6;
 		c.gridwidth = 4;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		add(sellTowerButton, c);
 		c.insets = new Insets(0, 0, 10, 0);
 		c.gridy = 7;
 		add(cancelButton, c);
 	}
 	
 	private void updateTargetingButtonHighlight() {
 		// clear all highlights
 		strongestButton.setBackground(defaultButtonBackground);
 		weakestButton.setBackground(defaultButtonBackground);
 		furthestButton.setBackground(defaultButtonBackground);
 		closestButton.setBackground(defaultButtonBackground);
 		
 		// highlight the appropriate targeting strategy button
 		switch (controller.getSelectedTower().getTargeting().getStrategy()) {
 			case STRONGEST:
 				strongestButton.setBackground(Color.ORANGE);
 				break;
 			case WEAKEST:
 				weakestButton.setBackground(Color.ORANGE);
 				break;
 			case FURTHEST:
 				furthestButton.setBackground(Color.ORANGE);
 				break;
 			case CLOSEST:
 				closestButton.setBackground(Color.ORANGE);
 				break;
 		}
 	}
 	
 	private void updateSellButton() {
 		sellTowerButton.setText("Sell for " + controller.getSelectedTower().getInvestment() * GameController.towerRefundPercentage);
 	}
 	
 	private void updateClickableButtons() {
 		Tower tower = controller.getSelectedTower();
 		
 		for (int l = 0; l < 3; l++) {
 			for (int n = 0; n < 3; n++) {
 				JButton upgradeButton = upgradeButtons[l][n];
 				Upgrade potentialUpgrade = controller.getTowerUpgrade(l + 1, n);
 				
 				// only allow upgrades at the proper level and that the player can afford
 				if (l == tower.getUpgradeLevel() && controller.playerCanAfford(potentialUpgrade) && controller.getPaused() == false) {
 					upgradeButton.setEnabled(true);
 				} else {
 					upgradeButton.setEnabled(false);
 				}
 			}
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		
 		towerStats.setTower(controller.getSelectedTower());
 		updateSellButton();
 		updateClickableButtons();
 		updateTargetingButtonHighlight();
 	}
 	
 	public void disableTowerUpgrade() {
 		for (int x = 0; x < upgradeButtons.length; x++){
 			for (int y = 0; y < upgradeButtons[x].length; y++){
 				upgradeButtons[x][y].setEnabled(false);
 			}
 		}
 		strongestButton.setEnabled(false);
 		weakestButton.setEnabled(false);
 		closestButton.setEnabled(false);
 		furthestButton.setEnabled(false);
 		sellTowerButton.setEnabled(false);
 		cancelButton.setEnabled(false);
 	}
 	
 	public void enableTowerUpgrade() {
 		for (int x = 0; x < upgradeButtons.length; x++){
 			for (int y = 0; y < upgradeButtons[x].length; y++){
 				upgradeButtons[x][y].setEnabled(true);
 			}
 		}
 		strongestButton.setEnabled(true);
 		weakestButton.setEnabled(true);
 		closestButton.setEnabled(true);
 		furthestButton.setEnabled(true);
 		sellTowerButton.setEnabled(true);
 		cancelButton.setEnabled(true);
 
 	}
 }
