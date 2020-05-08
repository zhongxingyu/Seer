 package src.ui.creepside;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import src.FilePaths;
 import src.core.Creep;
 import src.core.IAlignment;
 import src.ui.ColorConstants;
 import src.ui.controller.GameController;
 import src.ui.side.TowerStatsPanel;
 
 public class CreepPurchasePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	private GameController gc;
 	
 	private CreepQueuePanel creepQueue;
 	private Creep creep;
 	private ImageIcon creepIcon;
 	private JLabel iconLabel;
 	
 	private JButton neutralButton;
 	private JButton redButton;
 	private JButton greenButton;
 	private JButton blueButton;
 	private JButton yellowButton;
 	private JButton buyButton;
 	private JButton cancelButton;
 	
 	private ImageIcon buyCreepIcon;
 	private ImageIcon buyCreepHoverIcon;
 	private ImageIcon buyCreepPressedIcon;
 
 	private int creepIndex;
 	
 	private Creep.Type[] creepTypes = {Creep.Type.GENERIC, Creep.Type.FLYING, Creep.Type.BIG_GUY, Creep.Type.ASSASSIN, Creep.Type.FAST};
 
 	public CreepPurchasePanel(CreepQueuePanel cq, GameController controller) {
 		super(new GridBagLayout());
 		gc = controller;
 		
 		buyCreepIcon = new ImageIcon(FilePaths.buttonPath + "BuyCreepButton.png");
 		buyCreepHoverIcon = new ImageIcon(FilePaths.buttonPath + "BuyCreepButtonHover.png");
 		buyCreepPressedIcon = new ImageIcon(FilePaths.buttonPath + "BuyCreepsButtonDown.png");
 		
 		creepQueue = cq;
 		creepIndex = -1;		
 		neutralButton = new JButton();
 		neutralButton.setBackground(ColorConstants.neutralColor);
 		neutralButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(creep != null) {
 					creep.setAlignment(IAlignment.Alignment.NEUTRAL);
 					setIconLabel(creep);
 				}
 			}
 		});
 		
 		redButton = new JButton();
 		redButton.setBackground(ColorConstants.redColor);
 		redButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(creep != null) {
 					creep.setAlignment(IAlignment.Alignment.RED);
 					setIconLabel(creep);
 				}
 			}
 		});		
 		greenButton = new JButton();
 		greenButton.setBackground(ColorConstants.greenColor);
 		greenButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(creep != null) {
 					creep.setAlignment(IAlignment.Alignment.GREEN);
 					setIconLabel(creep);
 				}
 			}
 		});			
 		blueButton = new JButton();
 		blueButton.setBackground(ColorConstants.blueColor);
 		blueButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(creep != null) {
 					creep.setAlignment(IAlignment.Alignment.BLUE);
 					setIconLabel(creep);
 				}
 			}
 		});			
 		yellowButton = new JButton();
 		yellowButton.setBackground(ColorConstants.yellowColor);
 		yellowButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(creep != null) {
 					creep.setAlignment(IAlignment.Alignment.YELLOW);
 					setIconLabel(creep);
 				}
 			}
 		});			
 		
 		buyButton = new JButton("Buy creep");
 		
 		buyButton.setBackground(Color.ORANGE);
 		buyButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Creep creepToAdd = Creep.copyCreep(creep);
 				gc.getGame().getPlayer().increaseIncomePerWave(creepToAdd.getAdditionalGoldPerWave());
 				gc.getGame().getPlayer().purchase(creepToAdd);
 				creepQueue.enqueue(creepToAdd, creepIndex);
 				gc.getSideBar().getPlayerStatsPanel().setGoldChange(" -" + creepToAdd.getPrice());
 			}
 		});
 		
 		buyButton.addMouseListener(new MouseAdapter() {
 				public void mouseEntered(MouseEvent e) {
 					if (creep != null) {
 						gc.getSideBar().getPlayerStatsPanel().setShowingGoldChange(true);
 						gc.getSideBar().getPlayerStatsPanel().setGoldChange(" -" + creep.getPrice());
 					}
 				}
 				
 				public void mouseExited(MouseEvent e) {
 					gc.getSideBar().getPlayerStatsPanel().setShowingGoldChange(false);
 					if (buyButton.isEnabled())
 						gc.getSideBar().getPlayerStatsPanel().setGoldChange(" ");
 				}
 			
 		});
 		
 		cancelButton = new JButton("Cancel");
 		cancelButton.setBackground(Color.WHITE);
 		cancelButton.setEnabled(false);
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setCreepByIndex(-1);
 			}
 		});	
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		String path = FilePaths.imgPath + "blank.png";
 		creepIcon = new ImageIcon(path);
 		
 		Image i = creepIcon.getImage();
 		i = i.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);  
 		creepIcon = new ImageIcon(i);  
 		iconLabel = new JLabel(creepIcon);
 		
 
 		c.insets = new Insets(0, 5, 0, 10);
 		
 		c.gridx = 0;
 		c.gridy = 1;
 
 		add(iconLabel, c);
 		c.gridwidth = 1;
 		c.gridheight = 3;
 		c.ipady = 0;
 		
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.ipady = 10;
		c.insets.set(20, 0, 5, 5);
 		c.gridx = 1;
 		c.gridy = 0;
 		add(neutralButton, c);
 		c.gridx = 2;
 		add(redButton, c);
 		c.gridx = 3;
 		add(greenButton, c);
 		c.gridx = 4;
 		add(blueButton, c);
 		c.gridx = 5;
		c.insets.set(20, 0, 5, 0);
 		add(yellowButton, c);
 		
 		updateAllowedButtons();
		
 		c.ipady = 0;
		c.insets.set(0, 0, 5, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 5;
 		add(buyButton, c);
		c.gridy = 2;
 		add(cancelButton, c);
 	}
 
 	private void updateAllowedButtons() {
 		if ( creep == null || (creep != null && !gc.playerCanAfford(creep)) || !gc.playerCanAfford(creep)) {
 			buyButton.setEnabled(false);
 		} 
 		else{
 			buyButton.setEnabled(true);
 		}
 		
 		if(creep != null){
 			neutralButton.setEnabled(true);
 			redButton.setEnabled(true);
 			greenButton.setEnabled(true);
 			blueButton.setEnabled(true);
 			yellowButton.setEnabled(true);	
 		}
 		else{
 			neutralButton.setEnabled(false);
 			redButton.setEnabled(false);
 			greenButton.setEnabled(false);
 			blueButton.setEnabled(false);
 			yellowButton.setEnabled(false);			
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		updateAllowedButtons();
 	}
 	
 	
 	public void setCreepByIndex(int index){
 		creepIndex = index;
 		if(index == -1){
 			this.creep = null;
 			String path = FilePaths.imgPath + "blank.png";
 			creepIcon = new ImageIcon(path);
 			
 			Image i = creepIcon.getImage();
 			i = i.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);  
 			creepIcon = new ImageIcon(i);  
 			iconLabel.setIcon(creepIcon);
 			buyButton.setEnabled(false);
 			cancelButton.setEnabled(false);	
 
 		}
 		else{
 			this.creep = Creep.createCreep(creepTypes[index], gc.getGame().getWavesSent());
 			setIconLabel(creep);
 			
 			buyButton.setEnabled(true);
 			cancelButton.setEnabled(true);
 		}
 	}
 	
 	//Sets the iconLabel to the appropriate creep based off of type and and alignment
 	public void setIconLabel(Creep c) {
 		String path = Creep.getcreepIconPath(c.getAlignment(), c.getType());
 		creepIcon = new ImageIcon(path);
 		
 		Image i = creepIcon.getImage();
 		i = i.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);  
 		creepIcon = new ImageIcon(i);  
 		iconLabel.setIcon(creepIcon);
 
 	}
 }
