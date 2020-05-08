 package pl.edu.agh.two.mud.client.ui;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 
 import net.miginfocom.swing.MigLayout;
 import pl.edu.agh.two.mud.common.*;
 
 public class PlayerPanel extends JPanel {
 
 	private static final String SLASH = "/";
 
 	private static final String PZ = "PZ";
 
 	private static final String AGILITY_LABEL_TEXT = "Zwinnosc";
 
 	private static final String EQUIPMENT_WEAPON_LABEL_TEXT = "Bron";
 
 	private static final String EQUIPMENT_FEET_LABEL_TEXT = "Ubranie - nogi";
 
 	private static final String EQUIPMENT_HEAD_LABEL_TEXT = "Ubranie - glowa";
 
 	private static final String EQUIPMENT_BODY_LABEL_TEXT = "Ubranie - tulow";
 
 	private static final String EXPERIENCE_LABEL_TEXT = "Doswiadczenie";
 
 	private static final String NONE = "brak";
 
 	private static final String POWER_LABEL_TEXT = "Moc";
 
 	private static final String STRENGTH_LABEL_TEXT = "Sila";
 
 	private static final String LEVEL_LABEL_TEXT = "Poziom";
 
 	private static final String NAME_LABEL_TEXT = "Imie postaci";
 
 	private static final String GOLD_LABEL_TEXT = "Zloto";
 
 	private static final long serialVersionUID = -8842308968094233877L;
 
 	private static final Color LABEL_TEXT_COLOR = Color.WHITE;
 
 	private static final Color VALUE_TEXT_COLOR = Color.YELLOW;
 
 	private static final IPlayer NULL_PLAYER = new Player();
 	{
 		NULL_PLAYER.setAgililty(0);
 		NULL_PLAYER.setPower(0);
 		NULL_PLAYER.setStrength(0);
 
 		NULL_PLAYER.setExperience(0);
 		NULL_PLAYER.setGold(0);
 		NULL_PLAYER.setLevel(0);
 
 		NULL_PLAYER.setHealthPoints(0);
 		NULL_PLAYER.setMaxHealthPoints(0);
 	}
 
 	private JLabel feet;
 
 	private JLabel body;
 
 	private JLabel head;
 
 	private JLabel weapon;
 
 	private JLabel agility;
 
 	private JLabel power;
 
 	private JLabel strength;
 
 	private JLabel experience;
 
 	private JLabel gold;
 
 	private JLabel name;
 
 	private JLabel level;
 
 	private JLabel health;
 
 	/**
 	 * Create the panel.
 	 */
 	public PlayerPanel() {
 		setBorder(new LineBorder(Color.WHITE));
 		setForeground(Color.WHITE);
 		setBackground(Color.BLACK);
 		setLayout(new MigLayout("", "[100px][100px][100px][100px]", "[][][]"));
 
 		createGeneralInfo();
 		createStatisticsPanel();
 		createEquipmentPanel();
 		createOthersPanel();
 
 		updateHero(null);
 
 	}
 
 	private void createGeneralInfo() {
 		JLabel playerNameLabel = new JLabel(NAME_LABEL_TEXT);
 		playerNameLabel.setForeground(LABEL_TEXT_COLOR);
 		add(playerNameLabel, "cell 0 0,alignx left,aligny center");
 
 		name = new JLabel("");
 		name.setForeground(VALUE_TEXT_COLOR);
 		add(name, "cell 1 0,alignx left,aligny center");
 
 		JLabel levelLabel = new JLabel(LEVEL_LABEL_TEXT);
 		levelLabel.setForeground(LABEL_TEXT_COLOR);
 		add(levelLabel, "cell 2 0");
 
 		level = new JLabel();
 		level.setForeground(VALUE_TEXT_COLOR);
 		add(level, "cell 3 0");
 	}
 
 	private void createStatisticsPanel() {
 		JPanel statisticsPanel = new JPanel();
 		statisticsPanel.setForeground(Color.WHITE);
 		statisticsPanel.setBackground(Color.BLACK);
 		statisticsPanel.setBorder(new LineBorder(new Color(255, 255, 255)));
 		add(statisticsPanel, "cell 0 1 2 1,grow");
 		statisticsPanel.setLayout(new GridLayout(0, 2, 4, 4));
 
 		JLabel strengthLabel = new JLabel(STRENGTH_LABEL_TEXT);
 		strengthLabel.setForeground(LABEL_TEXT_COLOR);
 		statisticsPanel.add(strengthLabel);
 
 		strength = new JLabel();
 		strength.setForeground(VALUE_TEXT_COLOR);
 		statisticsPanel.add(strength);
 
 		JLabel powerLabel = new JLabel(POWER_LABEL_TEXT);
 		powerLabel.setForeground(LABEL_TEXT_COLOR);
 		statisticsPanel.add(powerLabel);
 
 		power = new JLabel();
 		power.setForeground(VALUE_TEXT_COLOR);
 		statisticsPanel.add(power);
 
 		JLabel agilityLabel = new JLabel(AGILITY_LABEL_TEXT);
 		agilityLabel.setForeground(LABEL_TEXT_COLOR);
 		statisticsPanel.add(agilityLabel);
 
 		agility = new JLabel();
 		agility.setForeground(VALUE_TEXT_COLOR);
 		statisticsPanel.add(agility);
 	}
 
 	private void createEquipmentPanel() {
 		JPanel equipmentPanel = new JPanel();
 		equipmentPanel.setBorder(new LineBorder(Color.WHITE));
 		equipmentPanel.setBackground(Color.BLACK);
 		equipmentPanel.setForeground(Color.WHITE);
 		add(equipmentPanel, "cell 2 1 2 1,grow");
 		equipmentPanel.setLayout(new GridLayout(0, 2, 0, 0));
 
 		JLabel weaponLabel = new JLabel(EQUIPMENT_WEAPON_LABEL_TEXT);
 		weaponLabel.setForeground(LABEL_TEXT_COLOR);
 		equipmentPanel.add(weaponLabel);
 
 		weapon = new JLabel(NONE);
 		weapon.setForeground(VALUE_TEXT_COLOR);
 		equipmentPanel.add(weapon);
 
 		JLabel headLabel = new JLabel(EQUIPMENT_HEAD_LABEL_TEXT);
 		headLabel.setForeground(LABEL_TEXT_COLOR);
 		equipmentPanel.add(headLabel);
 
 		head = new JLabel(NONE);
 		head.setForeground(VALUE_TEXT_COLOR);
 		equipmentPanel.add(head);
 
 		JLabel bodyLabel = new JLabel(EQUIPMENT_BODY_LABEL_TEXT);
 		bodyLabel.setForeground(LABEL_TEXT_COLOR);
 		equipmentPanel.add(bodyLabel);
 
 		body = new JLabel(NONE);
 		body.setForeground(VALUE_TEXT_COLOR);
 		equipmentPanel.add(body);
 
 		JLabel feetLabel = new JLabel(EQUIPMENT_FEET_LABEL_TEXT);
 		feetLabel.setForeground(LABEL_TEXT_COLOR);
 		equipmentPanel.add(feetLabel);
 
 		feet = new JLabel(NONE);
 		feet.setForeground(VALUE_TEXT_COLOR);
 		equipmentPanel.add(feet);
 	}
 
 	private void createOthersPanel() {
 		JPanel othersPanel = new JPanel();
 		othersPanel.setBorder(new LineBorder(Color.WHITE));
 		othersPanel.setBackground(Color.BLACK);
 		othersPanel.setForeground(Color.WHITE);
 		add(othersPanel, "cell 0 2 4 1,grow");
 		othersPanel.setLayout(new GridLayout(0, 4, 0, 0));
 
 		JLabel expirienceLabel = new JLabel(EXPERIENCE_LABEL_TEXT);
 		othersPanel.add(expirienceLabel);
 		expirienceLabel.setForeground(LABEL_TEXT_COLOR);
 
 		experience = new JLabel();
 		othersPanel.add(experience);
 		experience.setForeground(VALUE_TEXT_COLOR);
 
 		JLabel goldLabel = new JLabel(GOLD_LABEL_TEXT);
 		othersPanel.add(goldLabel);
 		goldLabel.setForeground(LABEL_TEXT_COLOR);
 
 		gold = new JLabel();
 		othersPanel.add(gold);
 		gold.setForeground(VALUE_TEXT_COLOR);
 
 		JLabel healthLabel = new JLabel(PZ);
 		othersPanel.add(healthLabel);
 		healthLabel.setForeground(LABEL_TEXT_COLOR);
 
 		health = new JLabel();
 		othersPanel.add(health);
 		health.setForeground(VALUE_TEXT_COLOR);
 	}
 
 	public void updateHero(IPlayer p) {
 		IPlayer player = p;
 		if (p == null) {
 			player = NULL_PLAYER;
 		}
 		// updating name
 		name.setText(player.getName());
 
 		// updating level
 		level.setText(player.getLevel().toString());
 
 		// updating basic stats
 		strength.setText(player.getStrength().toString());
 		power.setText(player.getPower().toString());
 		agility.setText(player.getAgililty().toString());
 
 		// updating exp
 		experience.setText(player.getExperience().toString());
 
 		// updating gold
 		gold.setText(player.getGold().toString());
 
 		// updating health
 		health.setText(player.getHealthPoints() + SLASH
 				+ player.getMaxHealthPoints());
 		repaint();
 	}
 
 	public void updateHeroData(UpdateData updateData) {
 		if(updateData==null) {
 			return;
 		}
 		
		health.setText(updateData.getHealthPoints() + SLASH + updateData.getMaxHealthPoints());
 		repaint();
 	}
 
 }
