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
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import src.FilePaths;
 import src.core.TargetingInfo;
 import src.core.Tower;
 import src.core.Upgrade;
 import src.ui.controller.GameController;
 
 /**
  * A display area which presents the selected tower's statistics and upgrade
  * options
  */
 public class TowerUpgradePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	private Color defaultButtonBackground; // we need to store this because we
 											// change the colors of some buttons
 
 	private GameController controller;
 	private TowerStatsPanel towerStats;
 	private ElementalUpgradePanel elementalUpgrade;
 
 	private boolean checkPaused;
 	
 	private JLabel levelOneLabel;
 	private JLabel levelTwoLabel;
 	private JLabel levelThreeLabel;
 
 	private JButton sellTowerButton;
 	private JButton cancelButton;
 	private JButton strongestButton, weakestButton, closestButton,
 			furthestButton;
 	private JButton[][] upgradeButtons;
 
 	private ImageIcon cancelUpgradeIcon;
 	private ImageIcon cancelUpgradePressedIcon;
 	private ImageIcon cancelUpgradeHoverIcon;
 	private ImageIcon cancelUpgradeDisabledIcon;
 
 	private ImageIcon sellIcon;
 	private ImageIcon sellPressedIcon;
 	private ImageIcon sellHoverIcon;
 	private ImageIcon sellDisabledIcon;
 
 	private ImageIcon farIcon;
 	private ImageIcon farPressedIcon;
 	private ImageIcon farHoverIcon;
 	private ImageIcon farDisabledIcon;
 	private ImageIcon closeIcon;
 	private ImageIcon closePressedIcon;
 	private ImageIcon closeHoverIcon;
 	private ImageIcon closeDisabledIcon;
 	private ImageIcon strongIcon;
 	private ImageIcon strongPressedIcon;
 	private ImageIcon strongHoverIcon;
 	private ImageIcon strongDisabledIcon;
 	private ImageIcon weakIcon;
 	private ImageIcon weakPressedIcon;
 	private ImageIcon weakHoverIcon;
 	private ImageIcon weakDisabledIcon;
 
 	private ImageIcon up1Icon;
 	private ImageIcon up1PressedIcon;
 	private ImageIcon up1HoverIcon;
 	private ImageIcon up1DisabledIcon;
 
 	private ImageIcon up2Icon;
 	private ImageIcon up2PressedIcon;
 	private ImageIcon up2HoverIcon;
 	private ImageIcon up2DisabledIcon;
 
 	private ImageIcon up3Icon;
 	private ImageIcon up3PressedIcon;
 	private ImageIcon up3HoverIcon;
 	private ImageIcon up3DisabledIcon;
 
 	public TowerUpgradePanel(GameController gc) {
 		super(new GridBagLayout());
 		controller = gc;
 		checkPaused = false;
 		
 		cancelUpgradeIcon = new ImageIcon(FilePaths.buttonPath + "CancelUpgradeButton.png");
 		cancelUpgradePressedIcon = new ImageIcon(FilePaths.buttonPath + "CancelUpgradeButtonDown.png");
 		cancelUpgradeHoverIcon = new ImageIcon(FilePaths.buttonPath + "CancelUpgradeButtonHover.png");
 		cancelUpgradeDisabledIcon = new ImageIcon(FilePaths.buttonPath + "CancelUpgradeButtonDisabled.png");
 
 		sellIcon = new ImageIcon(FilePaths.buttonPath + "BlankSellButton.png");
 		sellPressedIcon = new ImageIcon(FilePaths.buttonPath + "BlankSellButtonDown.png");
 		// sellHoverIcon = new ImageIcon(FilePaths.buttonPath +
 		// "BlankSellButtonHover.png");
 		 sellDisabledIcon = new ImageIcon(FilePaths.buttonPath +
 		 "BlankSellButtonDisabled.png");
 
 		farIcon = new ImageIcon(FilePaths.buttonPath + "FarButton.png");
 		farPressedIcon = new ImageIcon(FilePaths.buttonPath + "FarButtonDown.png");
 		farHoverIcon = new ImageIcon(FilePaths.buttonPath + "FarButtonHover.png");
 		farDisabledIcon = new ImageIcon(FilePaths.buttonPath + "FarButtonDisabled.png");
 
 		closeIcon = new ImageIcon(FilePaths.buttonPath + "CloseButton.png");
 		closePressedIcon = new ImageIcon(FilePaths.buttonPath + "CloseButtonDown.png");
 		closeHoverIcon = new ImageIcon(FilePaths.buttonPath	+ "CloseButtonHover.png");
 		closeDisabledIcon = new ImageIcon(FilePaths.buttonPath + "CloseButtonDisabled.png");
 
 		weakIcon = new ImageIcon(FilePaths.buttonPath + "WeakButton.png");
 		weakPressedIcon = new ImageIcon(FilePaths.buttonPath + "WeakButtonDown.png");
 		weakHoverIcon = new ImageIcon(FilePaths.buttonPath + "WeakButtonHover.png");
 		weakDisabledIcon = new ImageIcon(FilePaths.buttonPath + "WeakButtonDisabled.png");
 
 		strongIcon = new ImageIcon(FilePaths.buttonPath + "StrongButton.png");
 		strongPressedIcon = new ImageIcon(FilePaths.buttonPath + "StrongButtonDown.png");
 		strongHoverIcon = new ImageIcon(FilePaths.buttonPath + "StrongButtonHover.png");
 		strongDisabledIcon = new ImageIcon(FilePaths.buttonPath + "StrongButtonDisabled.png");
 
 		up1Icon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton1.png");
 		up1PressedIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton1Down.png");
 		up1HoverIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton1Hover.png");
 		up1DisabledIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton1Disabled.png");
 
 		up2Icon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton2.png");
 		up2PressedIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton2Down.png");
 		up2HoverIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton2Hover.png");
 		up2DisabledIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton2Disabled.png");
 
 		up3Icon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton3.png");
 		up3PressedIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton3Down.png");
 		up3HoverIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton3Hover.png");
 		up3DisabledIcon = new ImageIcon(FilePaths.buttonPath + "UpgradeButton3Disabled.png");
 
 		towerStats = new TowerStatsPanel(controller);
 		towerStats.setTower(controller.getSelectedTower());
 
 		// set up the buttons to handle changing targeting strategy
 		Insets targetingButtonInsets = new Insets(0, 5, 0, 5);
 
 		strongestButton = new JButton(strongIcon);
 		strongestButton.setBorder(BorderFactory.createEmptyBorder());
 		strongestButton.setContentAreaFilled(false);
 		strongestButton.setRolloverIcon(strongHoverIcon);
 		strongestButton.setDisabledIcon(strongDisabledIcon);
 		strongestButton.setPressedIcon(strongPressedIcon);
 
 		strongestButton.setMargin(targetingButtonInsets);
 		strongestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.STRONGEST);
 			}
 		});
 
 		weakestButton = new JButton(weakIcon);
 		weakestButton.setBorder(BorderFactory.createEmptyBorder());
 		weakestButton.setContentAreaFilled(false);
 		weakestButton.setRolloverIcon(weakHoverIcon);
 		weakestButton.setDisabledIcon(weakDisabledIcon);
 		weakestButton.setPressedIcon(weakPressedIcon);
 
 		weakestButton.setMargin(targetingButtonInsets);
 		weakestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.WEAKEST);
 			}
 		});
 
 		closestButton = new JButton(closeIcon);
 		closestButton.setBorder(BorderFactory.createEmptyBorder());
 		closestButton.setContentAreaFilled(false);
 		closestButton.setRolloverIcon(closeHoverIcon);
 		closestButton.setDisabledIcon(closeDisabledIcon);
 		closestButton.setPressedIcon(closePressedIcon);
 
 		closestButton.setMargin(targetingButtonInsets);
 		closestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.CLOSEST);
 			}
 		});
 
 		furthestButton = new JButton(farIcon);
 		furthestButton.setBorder(BorderFactory.createEmptyBorder());
 		furthestButton.setContentAreaFilled(false);
 		furthestButton.setRolloverIcon(farHoverIcon);
 		furthestButton.setDisabledIcon(farDisabledIcon);
 		furthestButton.setPressedIcon(farPressedIcon);
 
 		furthestButton.setMargin(targetingButtonInsets);
 		furthestButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.setTowerStrategy(TargetingInfo.Strategy.FURTHEST);
 			}
 		});
 
 		sellTowerButton = new JButton(sellIcon);
 		sellTowerButton.setHorizontalTextPosition(SwingConstants.CENTER);
 		sellTowerButton.setBorder(BorderFactory.createEmptyBorder());
 		sellTowerButton.setContentAreaFilled(false);
 		// sellTowerButton.setRolloverIcon(sellHoverIcon);
 		sellTowerButton.setDisabledIcon(sellDisabledIcon);
 
 		sellTowerButton.setForeground(Color.BLACK);
 		sellTowerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.sellTower();
 			}
 		});
 
 		sellTowerButton.addMouseListener(new MouseAdapter() {
 			public void mouseEntered(MouseEvent e) {
 				if (e.getComponent().isEnabled()) {
 					sellTowerButton.setForeground(Color.WHITE);
 				}
 
 			}
 
 			public void mouseExited(MouseEvent e) {
 				sellTowerButton.setForeground(Color.BLACK);
 			}
 
 		});
 
 		cancelButton = new JButton(cancelUpgradeIcon);
 		cancelButton.setBorder(BorderFactory.createEmptyBorder());
 		cancelButton.setContentAreaFilled(false);
 		cancelButton.setPressedIcon(cancelUpgradePressedIcon);
 		cancelButton.setRolloverIcon(cancelUpgradeHoverIcon);
 		cancelButton.setDisabledIcon(cancelUpgradeDisabledIcon);
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
 		holder.setOpaque(false);
 		
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
 				JButton upgradeButton = new JButton();
 				switch(l) {
 					case 0:
 						upgradeButton.setIcon(up1Icon);
 						upgradeButton.setBorder(BorderFactory.createEmptyBorder());
 						upgradeButton.setContentAreaFilled(false);
 						upgradeButton.setPressedIcon(up1PressedIcon);
 						upgradeButton.setRolloverIcon(up1HoverIcon);	
 						upgradeButton.setDisabledIcon(up1DisabledIcon);
 						break;
 					case 1:
 						upgradeButton = new JButton(up2Icon);
 						upgradeButton.setBorder(BorderFactory.createEmptyBorder());
 						upgradeButton.setContentAreaFilled(false);
 						upgradeButton.setPressedIcon(up2PressedIcon);
 						upgradeButton.setRolloverIcon(up2HoverIcon);	
 						upgradeButton.setDisabledIcon(up2DisabledIcon);
 						break;
 					case 2:
 						upgradeButton = new JButton(up3Icon);
 						upgradeButton.setBorder(BorderFactory.createEmptyBorder());
 						upgradeButton.setContentAreaFilled(false);
 						upgradeButton.setPressedIcon(up3PressedIcon);
 						upgradeButton.setRolloverIcon(up3HoverIcon);	
 						upgradeButton.setDisabledIcon(up3DisabledIcon);
 						break;
 				
 				}
 
 				upgradeButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						controller.applyTowerUpgrade(level, idx);
 					}
 				});
 
 				upgradeButton.addMouseListener(new MouseAdapter() {
 					public void mouseEntered(MouseEvent e) {
 						if (e.getComponent().isEnabled()
 								|| controller.getTowerUpgrade(level, idx)
 										.getLevel() > controller
 										.getSelectedTower().getUpgradeLevel())
 							towerStats.setUpgrade(controller.getTowerUpgrade(
 									level, idx));
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
 
 		c.insets = new Insets(15, 0, 0, 0);
 		c.gridx = 0;
 		c.gridy = 6;
 		c.gridwidth = 4;
 		c.fill = GridBagConstraints.NONE;
 		add(sellTowerButton, c);
 		c.insets = new Insets(5, 0, 0, 0);
 		c.gridy = 7;
 		add(cancelButton, c);
 	}
 
 	private void updateSellButton() {
 		sellTowerButton.setFont(new Font("sell Font", Font.BOLD, 13));
 		sellTowerButton.setText("Sell for "
 				+ controller.getSelectedTower().getInvestment()
 				* GameController.towerRefundPercentage);
 	}
 
 	private void updateClickableButtons() {
 		Tower tower = controller.getSelectedTower();
 
 		for (int l = 0; l < 3; l++) {
 			for (int n = 0; n < 3; n++) {
 				JButton upgradeButton = upgradeButtons[l][n];
 				Upgrade potentialUpgrade = controller.getTowerUpgrade(l + 1, n);
 
 				// only allow upgrades at the proper level and that the player
 				// can afford
 				if (l == tower.getUpgradeLevel()
 						&& controller.playerCanAfford(potentialUpgrade)
 						&& controller.getPaused() == false) {
 					upgradeButton.setEnabled(true);
 				} else {
 					upgradeButton.setEnabled(false);
 				}
 			}
 		}
 		// make sure we don't re-enable the current targeting strategy
 		if (!checkPaused && controller.getSelectedTower() != null) {
 			TargetingInfo.Strategy strat = controller.getSelectedTower()
 					.getTargeting().getStrategy();
 			switch (strat) {
 			case STRONGEST:
 				strongestButton.setEnabled(false);
 				weakestButton.setEnabled(true);
 				closestButton.setEnabled(true);
 				furthestButton.setEnabled(true);
 				break;
 			case WEAKEST:
 				strongestButton.setEnabled(true);
 				weakestButton.setEnabled(false);
 				closestButton.setEnabled(true);
 				furthestButton.setEnabled(true);
 				break;
 			case FURTHEST:
 				strongestButton.setEnabled(true);
 				weakestButton.setEnabled(true);
 				closestButton.setEnabled(true);
 				furthestButton.setEnabled(false);
 				break;
 			case CLOSEST:
 				strongestButton.setEnabled(true);
 				weakestButton.setEnabled(true);
 				closestButton.setEnabled(false);
 				furthestButton.setEnabled(true);
 				break;
 			}
 		}
 		
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		towerStats.setTower(controller.getSelectedTower());
 		updateSellButton();
 		updateClickableButtons();
 	}
 
 	public void disableTowerUpgrade() {
 		checkPaused = true;
 		for (int x = 0; x < upgradeButtons.length; x++) {
 			for (int y = 0; y < upgradeButtons[x].length; y++) {
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
 		checkPaused = false;
 		for (int x = 0; x < upgradeButtons.length; x++) {
 			for (int y = 0; y < upgradeButtons[x].length; y++) {
 				upgradeButtons[x][y].setEnabled(true);
 			}
 		}
 


 		sellTowerButton.setEnabled(true);
 		cancelButton.setEnabled(true);

 	}
 }
