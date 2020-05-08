 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
import javax.swing.WindowConstants;
 
 /**
  * 
  * @author tugayac
  */
 public class ControlPanel extends JFrame {
 
 	private Player player;
 	private Map map;
 	private ArrayList<Tower> activeTowers;
 	private ArrayList<Enemy> activeEnemies;
 
 	public ControlPanel(Map map, Player player){
		this(player, map, map.getTowers(), map.getEnemies());
 	}
 	
 	/**
 	 * Creates new form ControlPanel
 	 */
 	public ControlPanel(Player player, Map map, ArrayList<Tower> activeTowers,
 			ArrayList<Enemy> activeEnemies) {
 		this.player = player;
 		this.map = map;
 		initComponents();
 
 
 		initLabelInfo();
 		
 		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 		this.setVisible(true);
 	}
 
 	private void initComponents() {
 
 		this.topLeftPanel = new JPanel();
 		this.playerInfoLabel = new JLabel();
 		this.playerScoreLabel = new JLabel();
 		this.playerManaLabel = new JLabel();
 		this.playerWaveLabel = new JLabel();
 		this.playerHealthLabel = new JLabel();
 		this.playerTimeLabel = new JLabel();
 		this.playerScoreValueLabel = new JLabel();
 		this.playerManaValueLabel = new JLabel();
 		this.playerWaveValueLabel = new JLabel();
 		this.playerHealthValueLabel = new JLabel();
 		this.playerTimeValueLabel = new JLabel();
 		this.topCenterPanel = new JPanel();
 		this.waveInfoLabel = new JLabel();
 		this.waveLevelLabel = new JLabel();
 		this.waveNameLabel = new JLabel();
 		this.waveHealthLabel = new JLabel();
 		this.waveArmorLabel = new JLabel();
 		this.waveSpeedLabel = new JLabel();
 		this.waveLevelValueLabel = new JLabel();
 		this.waveNameValueLabel = new JLabel();
 		this.waveHealthValueLabel = new JLabel();
 		this.waveArmorValueLabel = new JLabel();
 		this.waveSpeedValueLabel = new JLabel();
 		this.topRightPanel = new JPanel();
 		this.selectedEnemyLabel = new JLabel();
 		this.selectedHealthLabel = new JLabel();
 		this.selectedArmorLabel = new JLabel();
 		this.selectedSpeedLabel = new JLabel();
 		this.selectedHealthValueLabel = new JLabel();
 		this.selectedArmorValueLabel = new JLabel();
 		this.selectedSpeedValueLabel = new JLabel();
 		this.bottomLeftPanel = new JPanel();
 		this.availableTowersLabel = new JLabel();
 		this.fireCostLabel = new JLabel();
 		this.fireCostValueLabel = new JLabel();
 		this.waterCostLabel = new JLabel();
 		this.waterCostValueLabel = new JLabel();
 		this.airCostLabel = new JLabel();
 		this.airCostValueLabel = new JLabel();
 		this.lightCostLabel = new JLabel();
 		this.lightCostValueLabel = new JLabel();
 		this.earthCostLabel = new JLabel();
 		this.earthCostValueLabel = new JLabel();
 		this.fireButtonLabel = new JButton();
 		this.waterButtonLabel = new JButton();
 		this.airButtonLabel = new JButton();
 		this.lightButtonLabel = new JButton();
 		this.earthButtonLabel = new JButton();
 		this.bottomRightPanel = new JPanel();
 		this.towerUpgradeButton = new JButton();
 		this.towerSellButton = new JButton();
 		this.towerInfoLabel = new JLabel();
 		this.towerLevelLabel = new JLabel();
 		this.towerDamageLabel = new JLabel();
 		this.towerSpeedLabel = new JLabel();
 		this.towerLevelValueLabel = new JLabel();
 		this.towerDamageValueLabel = new JLabel();
 		this.towerSpeedValueLabel = new JLabel();
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
 		initTopLeftPanel();
 		topLeftPanelLayout();
 
 		initTopCenterPanel();
 		topCenterPanelLayout();
 
 		initTopRightPanel();
 		topRightPanelLayout();
 
 		initBottomLeftPanel();
 		bottomLeftPanelLayout();
 
 		initBottomRightPanel();
 		bottomRightPanelLayout();
 
 		pack();
 	}
 
 	private void initLabelInfo() {
 		/*
 		 * Player Information
 		 */
 		this.playerScoreValueLabel.setText(Integer.toString(this.player
 				.getScore()));
 		this.playerManaValueLabel.setText(Integer.toString(this.player
 				.getMana()));
 		this.playerWaveValueLabel.setText(Integer.toString(this.map
 				.getWaveNumber()));
 		this.playerHealthValueLabel.setText(Integer.toString(this.player
 				.getHealth()));
 
 		/*
 		 * Wave Information
 		 */
 
 		/*
 		 * Selected Enemy Information
 		 */
 
 	}
 
 	private void initImageIcons() {
 		/*
 		 * Fire button image initialization
 		 */
 		ImageIcon tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/fire_clicked.png"));
 		Image tempImage = tempIcon.getImage();
 		this.fireClicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/fire_unclicked.png"));
 		tempImage = tempIcon.getImage();
 		this.fireUnclicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/fire_grey1.png"));
 		tempImage = tempIcon.getImage();
 		this.fireInsuffMana = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/fire_grey.png"));
 		tempImage = tempIcon.getImage();
 		this.fireNotAvailable = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		/*
 		 * Water button image initialization
 		 */
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/water_clicked.png"));
 		tempImage = tempIcon.getImage();
 		this.waterClicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/water_unclicked.png"));
 		tempImage = tempIcon.getImage();
 		this.waterUnclicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/water_grey1.png"));
 		tempImage = tempIcon.getImage();
 		this.waterInsuffMana = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/water_grey.png"));
 		tempImage = tempIcon.getImage();
 		this.waterNotAvailable = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		/*
 		 * Air button image initialization
 		 */
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/air_clicked.png"));
 		tempImage = tempIcon.getImage();
 		this.airClicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/air_unclicked.png"));
 		tempImage = tempIcon.getImage();
 		this.airUnclicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/air_grey1.png"));
 		tempImage = tempIcon.getImage();
 		this.airInsuffMana = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/air_grey.png"));
 		tempImage = tempIcon.getImage();
 		this.airNotAvailable = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		/*
 		 * Light button image initialization
 		 */
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/light_clicked.png"));
 		tempImage = tempIcon.getImage();
 		this.lightClicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/light_unclicked.png"));
 		tempImage = tempIcon.getImage();
 		this.lightUnclicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/light_grey1.png"));
 		tempImage = tempIcon.getImage();
 		this.lightInsuffMana = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/light_grey.png"));
 		tempImage = tempIcon.getImage();
 		this.lightNotAvailable = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		/*
 		 * Earth button image initialization
 		 */
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/earth_clicked.png"));
 		tempImage = tempIcon.getImage();
 		this.earthClicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/earth_unclicked.png"));
 		tempImage = tempIcon.getImage();
 		this.earthUnclicked = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/earth_grey1.png"));
 		tempImage = tempIcon.getImage();
 		this.earthInsuffMana = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 
 		tempIcon = new ImageIcon(getClass().getResource(
 				"/resources/images/towers/earth_grey.png"));
 		tempImage = tempIcon.getImage();
 		this.earthNotAvailable = tempImage.getScaledInstance(100, 100,
 				Image.SCALE_SMOOTH);
 	}
 
 	private void initTopLeftPanel() {
 		this.topLeftPanel.setBorder(BorderFactory.createLineBorder(new Color(0,
 				0, 0)));
 		this.topLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 
 		this.playerInfoLabel.setFont(new Font("Tahoma", 0, 18)); // NOI18N
 		this.playerInfoLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		this.playerInfoLabel.setText("Player Information");
 
 		this.playerScoreLabel.setText("Score:");
 
 		this.playerManaLabel.setText("Mana:");
 
 		this.playerWaveLabel.setText("Wave:");
 
 		this.playerHealthLabel.setText("Health:");
 
 		this.playerTimeLabel.setText("Time:");
 	}
 
 	private void topLeftPanelLayout() {
 		GroupLayout jPanel1Layout = new GroupLayout(this.topLeftPanel);
 		this.topLeftPanel.setLayout(jPanel1Layout);
 		jPanel1Layout
 				.setHorizontalGroup(jPanel1Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								jPanel1Layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(
 												jPanel1Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addGroup(
 																jPanel1Layout
 																		.createSequentialGroup()
 																		.addComponent(
 																				this.playerInfoLabel,
 																				GroupLayout.DEFAULT_SIZE,
 																				GroupLayout.DEFAULT_SIZE,
 																				Short.MAX_VALUE)
 																		.addContainerGap())
 														.addGroup(
 																jPanel1Layout
 																		.createSequentialGroup()
 																		.addGap(10,
 																				10,
 																				10)
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.TRAILING)
 																						.addComponent(
 																								this.playerWaveLabel)
 																						.addComponent(
 																								this.playerScoreLabel)
 																						.addComponent(
 																								this.playerTimeLabel))
 																		.addPreferredGap(
 																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addComponent(
 																								this.playerTimeValueLabel)
 																						.addComponent(
 																								this.playerScoreValueLabel)
 																						.addComponent(
 																								this.playerWaveValueLabel))
 																		.addPreferredGap(
 																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
 																				39,
 																				Short.MAX_VALUE)
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addGroup(
 																								jPanel1Layout
 																										.createSequentialGroup()
 																										.addGap(5,
 																												5,
 																												5)
 																										.addComponent(
 																												this.playerManaLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.playerManaValueLabel))
 																						.addGroup(
 																								jPanel1Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.playerHealthLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.playerHealthValueLabel)))
 																		.addContainerGap(
 																				30,
 																				GroupLayout.PREFERRED_SIZE)))));
 
 		jPanel1Layout
 				.setVerticalGroup(jPanel1Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								jPanel1Layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addComponent(this.playerInfoLabel)
 										.addGap(18, 18, 18)
 										.addGroup(
 												jPanel1Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addGroup(
 																jPanel1Layout
 																		.createSequentialGroup()
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.BASELINE)
 																						.addComponent(
 																								this.playerManaLabel)
 																						.addComponent(
 																								this.playerManaValueLabel))
 																		.addGap(18,
 																				18,
 																				18)
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.BASELINE)
 																						.addComponent(
 																								this.playerHealthLabel)
 																						.addComponent(
 																								this.playerHealthValueLabel)))
 														.addGroup(
 																jPanel1Layout
 																		.createSequentialGroup()
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.BASELINE)
 																						.addComponent(
 																								this.playerScoreValueLabel)
 																						.addComponent(
 																								this.playerScoreLabel))
 																		.addGap(18,
 																				18,
 																				18)
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.BASELINE)
 																						.addComponent(
 																								this.playerWaveValueLabel)
 																						.addComponent(
 																								this.playerWaveLabel))
 																		.addGap(18,
 																				18,
 																				18)
 																		.addGroup(
 																				jPanel1Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.BASELINE)
 																						.addComponent(
 																								this.playerTimeValueLabel)
 																						.addComponent(
 																								this.playerTimeLabel))))
 										.addContainerGap(26, Short.MAX_VALUE)));
 	}
 
 	private void initTopCenterPanel() {
 		this.topCenterPanel.setBorder(BorderFactory.createLineBorder(new Color(
 				0, 0, 0)));
 
 		this.waveInfoLabel.setFont(new Font("Tahoma", 0, 18)); // NOI18N
 		this.waveInfoLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		this.waveInfoLabel.setText("Wave Information");
 
 		this.waveLevelLabel.setText("Level:");
 
 		this.waveNameLabel.setText("Name:");
 
 		this.waveHealthLabel.setText("Health:");
 
 		this.waveArmorLabel.setText("Armor:");
 
 		this.waveSpeedLabel.setText("Speed:");
 	}
 
 	private void topCenterPanelLayout() {
 
 		GroupLayout jPanel2Layout = new GroupLayout(this.topCenterPanel);
 		this.topCenterPanel.setLayout(jPanel2Layout);
 		jPanel2Layout
 				.setHorizontalGroup(jPanel2Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								jPanel2Layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(
 												jPanel2Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addGroup(
 																jPanel2Layout
 																		.createSequentialGroup()
 																		.addGap(8,
 																				8,
 																				8)
 																		.addGroup(
 																				jPanel2Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addGroup(
 																								jPanel2Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.waveSpeedLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.waveSpeedValueLabel)
 																										.addContainerGap())
 																						.addGroup(
 																								jPanel2Layout
 																										.createSequentialGroup()
 																										.addGap(4,
 																												4,
 																												4)
 																										.addGroup(
 																												jPanel2Layout
 																														.createParallelGroup(
 																																GroupLayout.Alignment.TRAILING)
 																														.addComponent(
 																																this.waveNameLabel)
 																														.addComponent(
 																																this.waveLevelLabel))
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addGroup(
 																												jPanel2Layout
 																														.createParallelGroup(
 																																GroupLayout.Alignment.LEADING)
 																														.addComponent(
 																																this.waveLevelValueLabel)
 																														.addComponent(
 																																this.waveNameValueLabel))
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
 																												GroupLayout.DEFAULT_SIZE,
 																												Short.MAX_VALUE)
 																										.addGroup(
 																												jPanel2Layout
 																														.createParallelGroup(
 																																GroupLayout.Alignment.LEADING,
 																																false)
 																														.addComponent(
 																																this.waveHealthLabel)
 																														.addComponent(
 																																this.waveArmorLabel))
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addGroup(
 																												jPanel2Layout
 																														.createParallelGroup(
 																																GroupLayout.Alignment.LEADING)
 																														.addComponent(
 																																this.waveHealthValueLabel)
 																														.addComponent(
 																																this.waveArmorValueLabel))
 																										.addGap(22,
 																												22,
 																												22))))
 														.addGroup(
 																jPanel2Layout
 																		.createSequentialGroup()
 																		.addComponent(
 																				this.waveInfoLabel,
 																				GroupLayout.DEFAULT_SIZE,
 																				GroupLayout.DEFAULT_SIZE,
 																				Short.MAX_VALUE)
 																		.addContainerGap()))));
 		jPanel2Layout
 				.setVerticalGroup(jPanel2Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								jPanel2Layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addComponent(this.waveInfoLabel)
 										.addGap(18, 18, 18)
 										.addGroup(
 												jPanel2Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.BASELINE)
 														.addComponent(
 																this.waveLevelLabel)
 														.addComponent(
 																this.waveLevelValueLabel)
 														.addComponent(
 																this.waveHealthLabel)
 														.addComponent(
 																this.waveHealthValueLabel))
 										.addGap(18, 18, 18)
 										.addGroup(
 												jPanel2Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.BASELINE)
 														.addComponent(
 																this.waveNameLabel)
 														.addComponent(
 																this.waveNameValueLabel)
 														.addComponent(
 																this.waveArmorLabel)
 														.addComponent(
 																this.waveArmorValueLabel))
 										.addGap(18, 18, 18)
 										.addGroup(
 												jPanel2Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.BASELINE)
 														.addComponent(
 																this.waveSpeedLabel)
 														.addComponent(
 																this.waveSpeedValueLabel))
 										.addContainerGap(
 												GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)));
 	}
 
 	private void initTopRightPanel() {
 		this.topRightPanel.setBorder(BorderFactory.createLineBorder(new Color(
 				0, 0, 0)));
 
 		this.selectedEnemyLabel.setFont(new Font("Tahoma", 0, 18)); // NOI18N
 		this.selectedEnemyLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		this.selectedEnemyLabel.setText("Selected Enemy");
 
 		this.selectedHealthLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 		this.selectedHealthLabel.setText("Health:");
 
 		this.selectedArmorLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 		this.selectedArmorLabel.setText("Armor:");
 
 		this.selectedSpeedLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 		this.selectedSpeedLabel.setText("Speed:");
 
 		this.selectedHealthValueLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 
 		this.selectedArmorValueLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 
 		this.selectedSpeedValueLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
 	}
 
 	private void topRightPanelLayout() {
 		GroupLayout jPanel3Layout = new GroupLayout(this.topRightPanel);
 		this.topRightPanel.setLayout(jPanel3Layout);
 		jPanel3Layout
 				.setHorizontalGroup(jPanel3Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								jPanel3Layout
 										.createSequentialGroup()
 										.addGroup(
 												jPanel3Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addGroup(
 																jPanel3Layout
 																		.createSequentialGroup()
 																		.addContainerGap()
 																		.addComponent(
 																				this.selectedEnemyLabel,
 																				GroupLayout.DEFAULT_SIZE,
 																				GroupLayout.DEFAULT_SIZE,
 																				Short.MAX_VALUE))
 														.addGroup(
 																jPanel3Layout
 																		.createSequentialGroup()
 																		.addGap(38,
 																				38,
 																				38)
 																		.addGroup(
 																				jPanel3Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.TRAILING,
 																								false)
 																						.addComponent(
 																								this.selectedSpeedLabel)
 																						.addGroup(
 																								GroupLayout.Alignment.LEADING,
 																								jPanel3Layout
 																										.createParallelGroup(
 																												GroupLayout.Alignment.TRAILING)
 																										.addComponent(
 																												this.selectedArmorLabel)
 																										.addComponent(
 																												this.selectedHealthLabel)))
 																		.addGap(18,
 																				18,
 																				18)
 																		.addGroup(
 																				jPanel3Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addComponent(
 																								this.selectedHealthValueLabel)
 																						.addComponent(
 																								this.selectedArmorValueLabel)
 																						.addComponent(
 																								this.selectedSpeedValueLabel))
 																		.addGap(0,
 																				0,
 																				Short.MAX_VALUE)))
 										.addContainerGap()));
 		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(
 				GroupLayout.Alignment.LEADING).addGroup(
 				jPanel3Layout
 						.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(this.selectedEnemyLabel)
 						.addGap(18, 18, 18)
 						.addGroup(
 								jPanel3Layout
 										.createParallelGroup(
 												GroupLayout.Alignment.BASELINE)
 										.addComponent(this.selectedHealthLabel)
 										.addComponent(
 												this.selectedHealthValueLabel))
 						.addGap(18, 18, 18)
 						.addGroup(
 								jPanel3Layout
 										.createParallelGroup(
 												GroupLayout.Alignment.BASELINE)
 										.addComponent(this.selectedArmorLabel)
 										.addComponent(
 												this.selectedArmorValueLabel))
 						.addGap(18, 18, 18)
 						.addGroup(
 								jPanel3Layout
 										.createParallelGroup(
 												GroupLayout.Alignment.BASELINE)
 										.addComponent(this.selectedSpeedLabel)
 										.addComponent(
 												this.selectedSpeedValueLabel))
 						.addContainerGap(GroupLayout.DEFAULT_SIZE,
 								Short.MAX_VALUE)));
 	}
 
 	private void initBottomLeftPanel() {
 		this.bottomLeftPanel.setBorder(BorderFactory
 				.createLineBorder(new Color(0, 0, 0)));
 
 		this.availableTowersLabel.setFont(new Font("Tahoma", 0, 18)); // NOI18N
 		this.availableTowersLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		this.availableTowersLabel.setText("Available Towers");
 
 		this.fireCostLabel.setText("Cost:");
 
 		this.waterCostLabel.setText("Cost:");
 
 		this.airCostLabel.setText("Cost:");
 
 		this.lightCostLabel.setText("Cost:");
 
 		this.earthCostLabel.setText("Cost:");
 
 		initImageIcons();
 
 		this.fireButtonLabel.setBorderPainted(false);
 		this.fireButtonLabel.setContentAreaFilled(false);
 		this.fireButtonLabel.setIcon(new ImageIcon(this.fireUnclicked));
 		this.fireButtonLabel.addMouseListener(new MouseListener() {
 
 			public void mouseReleased(MouseEvent e) {
 				ControlPanel.this.fireButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.fireUnclicked));
 			}
 
 			public void mousePressed(MouseEvent e) {
 				ControlPanel.this.fireButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.fireClicked));
 			}
 
 			public void mouseExited(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseEntered(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseClicked(MouseEvent e) {
 				// Nothing to do
 			}
 		});
 
 		this.waterButtonLabel.setBorderPainted(false);
 		this.waterButtonLabel.setContentAreaFilled(false);
 		this.waterButtonLabel.setIcon(new ImageIcon(this.waterUnclicked));
 		this.waterButtonLabel.addMouseListener(new MouseListener() {
 
 			public void mouseReleased(MouseEvent e) {
 				ControlPanel.this.waterButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.waterUnclicked));
 			}
 
 			public void mousePressed(MouseEvent e) {
 				ControlPanel.this.waterButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.waterClicked));
 			}
 
 			public void mouseExited(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseEntered(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseClicked(MouseEvent e) {
 				// Nothing to do
 			}
 		});
 
 		this.airButtonLabel.setBorderPainted(false);
 		this.airButtonLabel.setContentAreaFilled(false);
 		this.airButtonLabel.setIcon(new ImageIcon(this.airUnclicked));
 		this.airButtonLabel.addMouseListener(new MouseListener() {
 
 			public void mouseReleased(MouseEvent e) {
 				ControlPanel.this.airButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.airUnclicked));
 			}
 
 			public void mousePressed(MouseEvent e) {
 				ControlPanel.this.airButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.airClicked));
 			}
 
 			public void mouseExited(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseEntered(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseClicked(MouseEvent e) {
 				// Nothing to do
 			}
 		});
 
 		this.lightButtonLabel.setBorderPainted(false);
 		this.lightButtonLabel.setContentAreaFilled(false);
 		this.lightButtonLabel.setIcon(new ImageIcon(this.lightUnclicked));
 		this.lightButtonLabel.addMouseListener(new MouseListener() {
 
 			public void mouseReleased(MouseEvent e) {
 				ControlPanel.this.lightButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.lightUnclicked));
 			}
 
 			public void mousePressed(MouseEvent e) {
 				ControlPanel.this.lightButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.lightClicked));
 			}
 
 			public void mouseExited(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseEntered(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseClicked(MouseEvent e) {
 				// Nothing to do
 			}
 		});
 
 		this.earthButtonLabel.setBorderPainted(false);
 		this.earthButtonLabel.setContentAreaFilled(false);
 		this.earthButtonLabel.setIcon(new ImageIcon(this.earthUnclicked));
 		this.earthButtonLabel.addMouseListener(new MouseListener() {
 
 			public void mouseReleased(MouseEvent e) {
 				ControlPanel.this.earthButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.earthUnclicked));
 			}
 
 			public void mousePressed(MouseEvent e) {
 				ControlPanel.this.earthButtonLabel.setIcon(new ImageIcon(
 						ControlPanel.this.earthClicked));
 			}
 
 			public void mouseExited(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseEntered(MouseEvent e) {
 				// Nothing to do
 			}
 
 			public void mouseClicked(MouseEvent e) {
 				// Nothing to do
 			}
 		});
 	}
 
 	private void bottomLeftPanelLayout() {
 		GroupLayout jPanel4Layout = new GroupLayout(this.bottomLeftPanel);
 		this.bottomLeftPanel.setLayout(jPanel4Layout);
 		jPanel4Layout
 				.setHorizontalGroup(jPanel4Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								jPanel4Layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(
 												jPanel4Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING,
 																false)
 														.addGroup(
 																jPanel4Layout
 																		.createSequentialGroup()
 																		.addGroup(
 																				jPanel4Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addGroup(
 																								GroupLayout.Alignment.TRAILING,
 																								jPanel4Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.fireCostLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.fireCostValueLabel)
 																										.addGap(23,
 																												23,
 																												23))
 																						.addGroup(
 																								jPanel4Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.fireButtonLabel,
 																												GroupLayout.PREFERRED_SIZE,
 																												100,
 																												GroupLayout.PREFERRED_SIZE)
 																										.addGap(18,
 																												18,
 																												18)))
 																		.addGroup(
 																				jPanel4Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addGroup(
 																								GroupLayout.Alignment.TRAILING,
 																								jPanel4Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.waterCostLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.waterCostValueLabel)
 																										.addGap(23,
 																												23,
 																												23))
 																						.addGroup(
 																								jPanel4Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.waterButtonLabel,
 																												GroupLayout.PREFERRED_SIZE,
 																												100,
 																												GroupLayout.PREFERRED_SIZE)
 																										.addGap(18,
 																												18,
 																												18)))
 																		.addGroup(
 																				jPanel4Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addGroup(
 																								jPanel4Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.airCostLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.airCostValueLabel)
 																										.addGap(33,
 																												33,
 																												33)
 																										.addComponent(
 																												this.lightCostLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.lightCostValueLabel))
 																						.addGroup(
 																								jPanel4Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.airButtonLabel,
 																												GroupLayout.PREFERRED_SIZE,
 																												100,
 																												GroupLayout.PREFERRED_SIZE)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																										.addComponent(
 																												this.lightButtonLabel,
 																												GroupLayout.PREFERRED_SIZE,
 																												100,
 																												GroupLayout.PREFERRED_SIZE)))
 																		.addGap(23,
 																				23,
 																				23)
 																		.addGroup(
 																				jPanel4Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addGroup(
 																								GroupLayout.Alignment.TRAILING,
 																								jPanel4Layout
 																										.createSequentialGroup()
 																										.addComponent(
 																												this.earthCostLabel)
 																										.addPreferredGap(
 																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(
 																												this.earthCostValueLabel)
 																										.addGap(5,
 																												5,
 																												5))
 																						.addComponent(
 																								this.earthButtonLabel,
 																								GroupLayout.PREFERRED_SIZE,
 																								100,
 																								GroupLayout.PREFERRED_SIZE)))
 														.addComponent(
 																this.availableTowersLabel,
 																GroupLayout.DEFAULT_SIZE,
 																GroupLayout.DEFAULT_SIZE,
 																Short.MAX_VALUE))
 										.addContainerGap(
 												GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)));
 		jPanel4Layout
 				.setVerticalGroup(jPanel4Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								GroupLayout.Alignment.TRAILING,
 								jPanel4Layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addComponent(this.availableTowersLabel)
 										.addGap(18, 18, 18)
 										.addGroup(
 												jPanel4Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addComponent(
 																this.fireButtonLabel,
 																GroupLayout.PREFERRED_SIZE,
 																100,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																this.waterButtonLabel,
 																GroupLayout.PREFERRED_SIZE,
 																100,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																this.airButtonLabel,
 																GroupLayout.PREFERRED_SIZE,
 																100,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																this.lightButtonLabel,
 																GroupLayout.PREFERRED_SIZE,
 																100,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																this.earthButtonLabel,
 																GroupLayout.PREFERRED_SIZE,
 																100,
 																GroupLayout.PREFERRED_SIZE))
 										.addGap(18, 18, 18)
 										.addGroup(
 												jPanel4Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.BASELINE)
 														.addComponent(
 																this.waterCostLabel)
 														.addComponent(
 																this.waterCostValueLabel)
 														.addComponent(
 																this.fireCostValueLabel)
 														.addComponent(
 																this.airCostLabel)
 														.addComponent(
 																this.airCostValueLabel)
 														.addComponent(
 																this.lightCostLabel)
 														.addComponent(
 																this.lightCostValueLabel)
 														.addComponent(
 																this.earthCostLabel)
 														.addComponent(
 																this.earthCostValueLabel)
 														.addComponent(
 																this.fireCostLabel))
 										.addContainerGap(
 												GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)));
 	}
 
 	private void initBottomRightPanel() {
 		this.bottomRightPanel.setBorder(BorderFactory
 				.createLineBorder(new Color(0, 0, 0)));
 
 		this.towerUpgradeButton.setText("Upgrade");
 		this.towerUpgradeButton
 				.setToolTipText("Upgrade the selected tower to the next level.");
 
 		this.towerSellButton.setText("Sell");
 		this.towerSellButton
 				.setToolTipText("Sell this tower for 100% of its value.");
 
 		this.towerInfoLabel.setFont(new Font("Tahoma", 0, 18)); // NOI18N
 		this.towerInfoLabel
 				.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		this.towerInfoLabel.setText("Tower Information");
 
 		this.towerLevelLabel.setText("Level:");
 
 		this.towerDamageLabel.setText("Damage:");
 
 		this.towerSpeedLabel.setText("Speed:");
 	}
 
 	private void bottomRightPanelLayout() {
 		GroupLayout jPanel5Layout = new GroupLayout(this.bottomRightPanel);
 		this.bottomRightPanel.setLayout(jPanel5Layout);
 		jPanel5Layout
 				.setHorizontalGroup(jPanel5Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								jPanel5Layout
 										.createSequentialGroup()
 										.addGroup(
 												jPanel5Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addGroup(
 																jPanel5Layout
 																		.createSequentialGroup()
 																		.addContainerGap()
 																		.addGroup(
 																				jPanel5Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.LEADING)
 																						.addComponent(
 																								this.towerInfoLabel,
 																								GroupLayout.DEFAULT_SIZE,
 																								160,
 																								Short.MAX_VALUE)
 																						.addGroup(
 																								jPanel5Layout
 																										.createSequentialGroup()
 																										.addGap(10,
 																												10,
 																												10)
 																										.addGroup(
 																												jPanel5Layout
 																														.createParallelGroup(
 																																GroupLayout.Alignment.LEADING)
 																														.addComponent(
 																																this.towerDamageLabel)
 																														.addComponent(
 																																this.towerSpeedLabel,
 																																GroupLayout.Alignment.TRAILING)
 																														.addComponent(
 																																this.towerLevelLabel,
 																																GroupLayout.Alignment.TRAILING))
 																										.addGap(18,
 																												18,
 																												18)
 																										.addGroup(
 																												jPanel5Layout
 																														.createParallelGroup(
 																																GroupLayout.Alignment.LEADING)
 																														.addComponent(
 																																this.towerLevelValueLabel)
 																														.addComponent(
 																																this.towerDamageValueLabel)
 																														.addComponent(
 																																this.towerSpeedValueLabel))
 																										.addGap(0,
 																												0,
 																												Short.MAX_VALUE))))
 														.addGroup(
 																jPanel5Layout
 																		.createSequentialGroup()
 																		.addGap(51,
 																				51,
 																				51)
 																		.addGroup(
 																				jPanel5Layout
 																						.createParallelGroup(
 																								GroupLayout.Alignment.TRAILING)
 																						.addComponent(
 																								this.towerSellButton,
 																								GroupLayout.PREFERRED_SIZE,
 																								81,
 																								GroupLayout.PREFERRED_SIZE)
 																						.addComponent(
 																								this.towerUpgradeButton))
 																		.addGap(0,
 																				0,
 																				Short.MAX_VALUE)))
 										.addContainerGap()));
 
 		jPanel5Layout
 				.setVerticalGroup(jPanel5Layout
 						.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(
 								GroupLayout.Alignment.TRAILING,
 								jPanel5Layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addComponent(this.towerInfoLabel)
 										.addPreferredGap(
 												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 										.addGroup(
 												jPanel5Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addComponent(
 																this.towerLevelValueLabel)
 														.addComponent(
 																this.towerLevelLabel))
 										.addPreferredGap(
 												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 										.addGroup(
 												jPanel5Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addComponent(
 																this.towerDamageValueLabel)
 														.addComponent(
 																this.towerDamageLabel))
 										.addPreferredGap(
 												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 										.addGroup(
 												jPanel5Layout
 														.createParallelGroup(
 																GroupLayout.Alignment.LEADING)
 														.addComponent(
 																this.towerSpeedValueLabel)
 														.addComponent(
 																this.towerSpeedLabel))
 										.addGap(18, 18, 18)
 										.addComponent(this.towerUpgradeButton)
 										.addPreferredGap(
 												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
 												GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)
 										.addComponent(this.towerSellButton)
 										.addContainerGap()));
 
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(layout
 				.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(
 						layout.createSequentialGroup()
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.TRAILING,
 												false)
 												.addGroup(
 														GroupLayout.Alignment.LEADING,
 														layout.createSequentialGroup()
 																.addComponent(
 																		this.topLeftPanel,
 																		GroupLayout.PREFERRED_SIZE,
 																		GroupLayout.DEFAULT_SIZE,
 																		GroupLayout.PREFERRED_SIZE)
 																.addPreferredGap(
 																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																.addComponent(
 																		this.topCenterPanel,
 																		GroupLayout.DEFAULT_SIZE,
 																		GroupLayout.DEFAULT_SIZE,
 																		Short.MAX_VALUE))
 												.addComponent(
 														this.bottomLeftPanel,
 														GroupLayout.PREFERRED_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.PREFERRED_SIZE))
 								.addPreferredGap(
 										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING)
 												.addComponent(
 														this.bottomRightPanel,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														Short.MAX_VALUE)
 												.addComponent(
 														this.topRightPanel,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														Short.MAX_VALUE))));
 		layout.setVerticalGroup(layout
 				.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(
 						layout.createSequentialGroup()
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.TRAILING)
 												.addComponent(
 														this.topLeftPanel,
 														GroupLayout.Alignment.LEADING,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														Short.MAX_VALUE)
 												.addComponent(
 														this.topCenterPanel,
 														GroupLayout.Alignment.LEADING,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														Short.MAX_VALUE)
 												.addComponent(
 														this.topRightPanel,
 														GroupLayout.Alignment.LEADING,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														Short.MAX_VALUE))
 								.addPreferredGap(
 										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												false)
 												.addComponent(
 														this.bottomLeftPanel,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														Short.MAX_VALUE)
 												.addComponent(
 														this.bottomRightPanel,
 														GroupLayout.DEFAULT_SIZE,
 														GroupLayout.DEFAULT_SIZE,
 														Short.MAX_VALUE))));
 	}
 
 	// Called externally to update player information
 	public void updatePlayerInfo() {
 
 	}
 
 	// Called externally to update player information
 	public void updateWaveInfo() {
 
 	}
 
 	// Called externally to update player information
 	public void updateEnemyInfo() {
 
 	}
 
 	public static void main(String args[]) {
 		/*
 		 * Create and display the form
 		 */
 		java.awt.EventQueue.invokeLater(new Runnable() {
 
 			public void run() {
 				new ControlPanel(new Player(), new Map(),
 						new ArrayList<Tower>(), new ArrayList<Enemy>())
 						.setVisible(true);
 			}
 		});
 	}
 
 	// Button, Label, and Panel declerations
 	private JButton towerUpgradeButton;
 	private JButton towerSellButton;
 	private JLabel playerInfoLabel;
 	private JLabel playerHealthValueLabel;
 	private JLabel playerTimeValueLabel;
 	private JLabel selectedEnemyLabel;
 	private JLabel waveLevelLabel;
 	private JLabel waveNameLabel;
 	private JLabel waveHealthLabel;
 	private JLabel waveArmorLabel;
 	private JLabel waveSpeedLabel;
 	private JLabel waveLevelValueLabel;
 	private JLabel waveNameValueLabel;
 	private JLabel playerScoreLabel;
 	private JLabel waveHealthValueLabel;
 	private JLabel waveArmorValueLabel;
 	private JLabel waveSpeedValueLabel;
 	private JLabel selectedHealthLabel;
 	private JLabel selectedArmorLabel;
 	private JLabel selectedSpeedLabel;
 	private JLabel selectedHealthValueLabel;
 	private JLabel selectedArmorValueLabel;
 	private JLabel selectedSpeedValueLabel;
 	private JLabel availableTowersLabel;
 	private JLabel playerManaLabel;
 	private JLabel fireCostLabel;
 	private JLabel fireCostValueLabel;
 	private JLabel waterCostLabel;
 	private JLabel waterCostValueLabel;
 	private JLabel waveInfoLabel;
 	private JLabel airCostLabel;
 	private JLabel airCostValueLabel;
 	private JLabel lightCostLabel;
 	private JLabel lightCostValueLabel;
 	private JLabel earthCostLabel;
 	private JLabel playerWaveLabel;
 	private JLabel earthCostValueLabel;
 	private JLabel towerInfoLabel;
 	private JLabel towerLevelLabel;
 	private JLabel towerDamageLabel;
 	private JLabel towerSpeedLabel;
 	private JLabel towerLevelValueLabel;
 	private JLabel towerDamageValueLabel;
 	private JLabel towerSpeedValueLabel;
 	private JButton fireButtonLabel;
 	private JButton waterButtonLabel;
 	private JButton airButtonLabel;
 	private JButton lightButtonLabel;
 	private JButton earthButtonLabel;
 	private JLabel playerHealthLabel;
 	private JLabel playerTimeLabel;
 	private JLabel playerScoreValueLabel;
 	private JLabel playerManaValueLabel;
 	private JLabel playerWaveValueLabel;
 	private JPanel topLeftPanel;
 	private JPanel topCenterPanel;
 	private JPanel topRightPanel;
 	private JPanel bottomLeftPanel;
 	private JPanel bottomRightPanel;
 	// End of declaration
 
 	/*
 	 * ImageIcon declerations
 	 */
 	// Fire
 	private Image fireClicked;
 	private Image fireUnclicked;
 	private Image fireNotAvailable;
 	private Image fireInsuffMana;
 	// Water
 	private Image waterClicked;
 	private Image waterUnclicked;
 	private Image waterNotAvailable;
 	private Image waterInsuffMana;
 	// Air
 	private Image airClicked;
 	private Image airUnclicked;
 	private Image airNotAvailable;
 	private Image airInsuffMana;
 	// Light
 	private Image lightClicked;
 	private Image lightUnclicked;
 	private Image lightNotAvailable;
 	private Image lightInsuffMana;
 	// Earth
 	private Image earthClicked;
 	private Image earthUnclicked;
 	private Image earthNotAvailable;
 	private Image earthInsuffMana;
 	// End of declerations
 }
