 package src.ui.creepside;
 
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import src.FilePaths;
 import src.core.Creep;
 import src.core.Upgrade;
 import src.ui.controller.GameController;
 import src.ui.creepside.CreepStatsPanel;
 import src.core.IAlignment;
 
 
 public class CreepSelectionPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	private GameController controller;
 	
 	private static final String purchaseCreepsText = "Purchase Creeps:";
 	private JLabel purchaseCreepsLabel;
 	private CreepStatsPanel creepStats;
 	private CreepPurchasePanel creepInfoPurchase;
 	
 	private JButton[] creepButtons;	
 	private Action[] creepButtonActions;
 	private Creep.Type[] buttonTypes = {Creep.Type.GENERIC, Creep.Type.FLYING, Creep.Type.BIG_GUY, Creep.Type.ASSASSIN, Creep.Type.FAST}; 
 	
 	
 	public CreepSelectionPanel(CreepPurchasePanel cip, GameController gc){
 
 		super(new GridBagLayout());
 		controller = gc;
 		this.creepInfoPurchase = cip;
 		purchaseCreepsLabel = new JLabel(purchaseCreepsText);
 		creepButtons = new JButton[8];
 		creepButtonActions = new Action[8];
 		
 		creepStats = new CreepStatsPanel(controller);
 		
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.anchor = GridBagConstraints.LINE_START;
		//c.insets = new Insets(0,20,0,-20);
		c.insets.set(10, 10, 0, 0);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 5;
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		add(purchaseCreepsLabel, c);
 		
 		c.insets = new Insets(-20,20,0,-15);
 		c.gridwidth = 1;
 		
 		// initialize a purchase button for each of the creeps
 		for (int index = 0; index < 5; index++) {
 			String path = Creep.getcreepIconPath(IAlignment.Alignment.NEUTRAL, buttonTypes[index]);
 			ImageIcon creepIcon = new ImageIcon(path);
 			Image i = creepIcon.getImage();
 			i = i.getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);  
 			creepIcon = new ImageIcon(i);
 			
 			JButton creepButton = new JButton(creepIcon);
 			creepButton.setBorder(BorderFactory.createEmptyBorder());
 			creepButton.setFocusPainted(false);
 			creepButton.setContentAreaFilled(false);
 			
 			creepButtons[index] = creepButton;
 			
 			final Creep.Type type = buttonTypes[index];
 			final int setIndex = index;
 			creepButtonActions[index] = new AbstractAction() {
 				public void actionPerformed(ActionEvent e) {
 					//controller.beginPurchasingTower(Tower.createTower(type));
 					creepInfoPurchase.setCreepByIndex(setIndex);
 				}
 			};
 			
 			// set up buttons
 			creepButton.addActionListener(creepButtonActions[index]);	
 			
 			// set up mouse hover on buttons
 			creepButton.addMouseListener(new MouseAdapter() {
 				public void mouseEntered(MouseEvent e) {
 					creepStats.setCreep(Creep.createCreep(type, controller.getGame().getWavesSent()));
 				}
 
 				public void mouseExited(MouseEvent e) {
 					creepStats.setCreep(null);
 				}
 			});
 
 			c.gridx = index;
 			c.gridy = 1;
 			c.fill = GridBagConstraints.NONE;
 			c.anchor = GridBagConstraints.LINE_START;
 			c.insets.set(0, 10, 5, 0);
 			add(creepButton, c);
 		}
 		
 		updateAllowedButtons();
 
 		c.anchor = GridBagConstraints.LINE_START;
 		c.insets = new Insets(20, 20, 0, 0);
 		c.gridx = 6;
 		c.gridy = 0;
 		c.gridheight = 2;
 		c.ipadx = 100;
 		c.anchor = GridBagConstraints.LINE_END;
 		add(creepStats, c);
 	}
 	
 	private void updateAllowedButtons() {
 		for (int i = 0; i < 5; i++) {
 			JButton b = creepButtons[i];
 			Action a = creepButtonActions[i];
 			Creep.Type type = buttonTypes[i];
 			
 			Creep c = Creep.createCreep(type, controller.getGame().getWavesSent());
 			if (!controller.playerCanAfford(c)) {
 				b.setEnabled(false);
 				a.setEnabled(false);
 			} 
 			else{
 				b.setEnabled(true);
 				a.setEnabled(true);
 			}
 			
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		updateAllowedButtons();
 	}
 	
 	public void disableCreepPurchase(){
 		for (int x = 0; x< creepButtons.length; x++){
 			creepButtons[x].setEnabled(false);
 			creepButtonActions[x].setEnabled(false);
 		}
 	}
 	
 	public void enableCreepPurchase(){
 		for (int x = 0; x< creepButtons.length; x++){
 			creepButtons[x].setEnabled(true);
 			creepButtonActions[x].setEnabled(true);
 		}
 	}
 }
