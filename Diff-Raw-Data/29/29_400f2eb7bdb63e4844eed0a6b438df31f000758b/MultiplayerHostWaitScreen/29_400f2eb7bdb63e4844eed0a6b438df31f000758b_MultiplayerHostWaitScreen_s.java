 package src.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import src.FilePaths;
 import src.core.Map;
 import src.ui.controller.MultiplayerController;
 
 public class MultiplayerHostWaitScreen extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private JButton startGameButton;
 	private JButton cancelButton;
 	private JButton bootButton;
 	
 	private JPanel waitingPanel;
 	private JPanel gridPanel;
 	
 	private JLabel gameLabel;
 	private JLabel mapNameLabel;
 	private JLabel waitingLabel;
 	private JLabel opponentLabel;
 	private JLabel opponentNameLabel;
 	
 	private MapComponent mc;
 	
 	private MultiplayerController controller;
 	
 	private ImageIcon background;
 	private ImageIcon cancelIcon;
 	private ImageIcon cancelHoverIcon;
 	private ImageIcon cancelPressedIcon;
 	
 	private ImageIcon startIcon;
 	private ImageIcon startHoverIcon;
 	private ImageIcon startPressedIcon;
 	private ImageIcon startDisabledIcon;
 	
 	private ImageIcon bootIcon;
 	private ImageIcon bootHoverIcon;
 	private ImageIcon bootPressedIcon;
 	
 	private ImageIcon bgImage;
 	
 	public MultiplayerHostWaitScreen(String gameName, String mapName, MultiplayerController multiController) {
 		super (new GridBagLayout());
 		this.controller = multiController;
 		
 		background = new ImageIcon(FilePaths.bgPath + "GenericBGRDsmall.png");
 		
 		cancelIcon = new ImageIcon(FilePaths.buttonPath + "CancelBigButton.png");
 		cancelHoverIcon = new ImageIcon(FilePaths.buttonPath + "CancelBigButtonHover.png");
 		cancelPressedIcon = new ImageIcon(FilePaths.buttonPath + "CancelBigButtonDown.png");
 	
 		startIcon = new ImageIcon(FilePaths.buttonPath + "GameStartButton.png");
 		startHoverIcon = new ImageIcon(FilePaths.buttonPath + "GameStartButtonHover.png");
 		startPressedIcon = new ImageIcon(FilePaths.buttonPath + "GameStartButtonDown.png");
 		startDisabledIcon = new ImageIcon(FilePaths.buttonPath + "GameStartButtonDisabled.png");
 		
 		bootIcon = new ImageIcon(FilePaths.buttonPath + "BootButton.png");
 		bootHoverIcon = new ImageIcon(FilePaths.buttonPath + "BootButtonHover.png");
 		bootPressedIcon = new ImageIcon(FilePaths.buttonPath + "BootButtonDown.png");
 
 		bgImage = new ImageIcon(FilePaths.miscPath + "MPWaitImg.png");
 		JLabel imageLabel = new JLabel(bgImage);
 		
		
 		startGameButton = new JButton(startIcon);
 		startGameButton.setBorder(BorderFactory.createEmptyBorder());
 		startGameButton.setContentAreaFilled(false);
 		startGameButton.setFocusPainted(false);
 		startGameButton.setPressedIcon(startPressedIcon);
 		startGameButton.setRolloverIcon(startHoverIcon);
 		startGameButton.setDisabledIcon(startDisabledIcon);
 		startGameButton.setEnabled(false);
 		
 		bootButton = new JButton(bootIcon);
 		bootButton.setBorder(BorderFactory.createEmptyBorder());
 		bootButton.setContentAreaFilled(false);
 		bootButton.setFocusPainted(false);
 		bootButton.setPressedIcon(bootPressedIcon);
 		bootButton.setRolloverIcon(bootHoverIcon);		
 		bootButton.setEnabled(false);			
 		bootButton.setVisible(false);
 		
 		cancelButton = new JButton(cancelIcon);
 		cancelButton.setBorder(BorderFactory.createEmptyBorder());
 		cancelButton.setContentAreaFilled(false);
 		cancelButton.setFocusPainted(false);
 		cancelButton.setPressedIcon(cancelPressedIcon);
 		cancelButton.setRolloverIcon(cancelHoverIcon);	
 		
 		waitingLabel = new JLabel("Waiting for opponents...");
 		waitingLabel.setFont(new Font("waiting font", Font.BOLD, 16));
 		waitingLabel.setForeground(Color.WHITE);
 		
 		mc = new MapComponent(true);
 		mc.setGridOn(true);
 		mc.setSize(400, 400);
 		mc.setMap(Map.getMapByName(mapName));
 		
 		
 		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 0);
		c.ipadx = 40;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.WEST;
 		c.gridwidth = 2;
 		add(waitingLabel, c);
 		c.gridwidth = 1;
 		
		c.insets.set(0, 0, 0, 30);
 		c.gridx = 1;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.EAST;
 		add(bootButton, c);
 		
		c.insets.set(0,0,0,0);
 		c.gridx = 0;
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.CENTER;
 		add(imageLabel, c);
 		
 		c.insets.set(30, 100, 0, 30);
 		c.gridx = 1;
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.CENTER;
 		add(mc, c);
 		
		c.insets.set(20, 0, 0, 0);
 		c.gridx = 0;
 		c.gridy = 3;
 		c.anchor = GridBagConstraints.WEST;
 		add(cancelButton, c);
 
 		c.insets.set(20, 0, 0, 30);
 		c.gridx = 1;
 		c.gridy = 3;
 		c.anchor = GridBagConstraints.EAST;
 		add(startGameButton, c);
 		
 		setupButtonHandlers();
 	}
 	
 	public void setupButtonHandlers() {
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.stopHostingGame();
 			}
 		});
 		
 		startGameButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.startNetworkGame();
 			}
 		});
 		
 		bootButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.bootPotentialOpponent();
 			}
 		});
 	}
 
 	public void setPotentialOpponent(String name) {
 		if (name == null) {
 			waitingLabel.setText("Waiting for opponents...");
 			startGameButton.setEnabled(false);
 			bootButton.setEnabled(false);
 			bootButton.setVisible(false);
			
 		} else {
 			waitingLabel.setText(name + " wants to battle you!");
 			startGameButton.setEnabled(true);
 			bootButton.setEnabled(true);
 			bootButton.setVisible(true);
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		g.drawImage(background.getImage(), 0 ,0, null);
 	}
 }
