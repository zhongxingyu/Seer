 package src.ui.side;
 
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import src.GameController;
 import src.core.Tower;
 import src.core.Upgrade;
 
 /**
  * A display area which presents the selected tower's statistics and upgrade options
  */
 public class TowerUpgradePanel extends JPanel {
 	private GameController controller;
 	
 	private TowerStatsPanel towerStats;
 	
 	private JLabel levelOneLabel;
 	private JLabel levelTwoLabel;
 	private JLabel levelThreeLabel;
 	
 	private JButton sellTowerButton;
 	private JButton cancelButton;
 	private JButton[] upgradeButtons;
 	
 	private JButton curButton;
 	
 	public TowerUpgradePanel(GameController gc) {
 		super(new GridBagLayout());
 		upgradeButtons = new JButton[9];
 		controller = gc;
 		
 		towerStats = new TowerStatsPanel();
 		towerStats.setTower(controller.getSelectedTower());
 		
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.unselectTower();
 			}
 		});
 		
 		sellTowerButton = new JButton("Sell");
 		sellTowerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.sellTower();
 			}
 		});
 		
 		levelOneLabel = new JLabel("1");
 		levelTwoLabel = new JLabel("2");
 		levelThreeLabel = new JLabel("3");
 		
 		upgradeButtons = new JButton[9];
 		
 		// lay out components
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 1;
 		c.gridwidth = 2;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.CENTER;
 		add(towerStats, c);
 		c.gridwidth = 1;
 		
 		c.gridx = 1;
 		c.gridwidth = 2;
 		c.gridy = 1;
 		add(cancelButton, c);
 		c.gridwidth = 1;
 		
 		c.gridx = 0;
 		c.gridy = 2;
 		add(levelThreeLabel, c);
 		
 		for (int n = 0; n < 3; n++) {
 			JButton upgradeButton = new JButton("up3");
 			final int i = n;
 			upgradeButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					controller.applyTowerUpgrade(3, i);
 				}
 			});
 			c.gridx = n + 1;
 			c.gridy = 2;
 			add(upgradeButton, c);
 			upgradeButtons[n] = upgradeButton;
 			setMouseListener(upgradeButton);
 		}
 		
 		c.gridx = 0;
 		c.gridy = 3;
 		add(levelTwoLabel, c);
 		
 		for (int n = 0; n < 3; n++) {
 			JButton upgradeButton = new JButton("up2");
 			final int i = n;
 			upgradeButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					controller.applyTowerUpgrade(2, i);
 				}
 			});
 			
 			c.gridx = n + 1;
 			c.gridy = 3;
 			add(upgradeButton, c);
 			upgradeButtons[n+3] = upgradeButton;
 			setMouseListener(upgradeButton);
 
 		}
 		
 		c.gridx = 0;
 		c.gridy = 4;
 		add(levelOneLabel, c);
 		
 		for (int n = 0; n < 3; n++) {
 			JButton upgradeButton = new JButton("up1");
 			final int i = n;
 			upgradeButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					controller.applyTowerUpgrade(1, i);
 				}
 			});
 			
 			c.gridx = n + 1;
 			c.gridy = 4;
 			add(upgradeButton, c);
 			upgradeButtons[n+6] = upgradeButton;
 			setMouseListener(upgradeButton);
 
 		}
 		
 		c.gridx = 0;
 		c.gridy = 5;
 		add(sellTowerButton, c);
 		
 	}
 	
 	//adds a MouseListener to the passed in button
 	private void setMouseListener(JButton button){
 		button.addMouseListener(new MouseAdapter(){
 			
 			//If an upgrade is hovered over, calls the setUpgrade method in
 			//the TowerStatsPanel with the appropriate upgrade
 			public void mouseEntered(MouseEvent e){
 				curButton = (JButton) e.getSource();
 				
 				Tower tower = controller.getSelectedTower();
 				Upgrade potentialUpgrade = null;
 				if (tower.getUpgradeLevel() == 0){
 					for (int y = 0; y<3;y++){
 						if (upgradeButtons[6+y] == curButton)
 							potentialUpgrade = controller.getTowerUpgrade(1,y);
 					}
 				} else if (tower.getUpgradeLevel() == 1) {
 					for (int y = 0; y<3;y++){
 						if (upgradeButtons[3+y] == curButton)
 							potentialUpgrade = controller.getTowerUpgrade(2,y);
 					}
 				} else if (tower.getUpgradeLevel() == 2) {
 					for (int y = 0; y<3;y++){
 						if (upgradeButtons[y] == curButton)
 							potentialUpgrade = controller.getTowerUpgrade(3,y);
 					}
 				}
 
 				towerStats.setUpgrade(potentialUpgrade);
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				towerStats.setUpgrade(null);
 			}
 			
 			});
 		}
 	
 
 	private void updateClickableButtons(){		
 		Tower tower = controller.getSelectedTower();
 
		ArrayList<Upgrade> upgrades = tower.getUpgrades();
 		if (tower != null){
 			
 			//First only allow upgrades for the proper level
 			for (int x= 0; x<upgradeButtons.length; x++) {
 				if (tower.getUpgradeLevel() == 3)
 					upgradeButtons[x].setEnabled(false);
 				if (tower.getUpgradeLevel() == 0) {
 					if(x>5)
 						upgradeButtons[x].setEnabled(true);
 					else upgradeButtons[x].setEnabled(false);
 				} else if (tower.getUpgradeLevel() == 1) {
 					if(x>2 && x<6)
 						upgradeButtons[x].setEnabled(true);
 					else upgradeButtons[x].setEnabled(false);
 				} else if (tower.getUpgradeLevel() == 2) {
 					if(x<3)
 						upgradeButtons[x].setEnabled(true);
 					else upgradeButtons[x].setEnabled(false);
 				}
 				
 			}
 			//Next check whether or not we can afford
 			for (int x= 0; x<upgradeButtons.length; x++) {
 				if (tower.getUpgradeLevel() == 0){
 					for (int y=0; y<3; y++){
 						if (controller.playerCanAfford(controller.getTowerUpgrade(1,y)))
 							upgradeButtons[6+y].setEnabled(true);
 						else upgradeButtons[6+y].setEnabled(false);
 					}
 				} else if (tower.getUpgradeLevel() == 1) {
 					for (int y=0; y<3; y++){
 						if (controller.playerCanAfford(controller.getTowerUpgrade(2,y)))
 							upgradeButtons[3+y].setEnabled(true);
 						else upgradeButtons[3+y].setEnabled(false);
 					}
 				} else if (tower.getUpgradeLevel() == 2) {
 					for (int y=0; y<3; y++){
 						if (controller.playerCanAfford(controller.getTowerUpgrade(3,y)))
 							upgradeButtons[y].setEnabled(true);
 						else upgradeButtons[y].setEnabled(false);
 					}
 				}
 			}
 		}
 	}
 	
 	
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		towerStats.setTower(controller.getSelectedTower());	
 		updateClickableButtons();
 	}
 }
