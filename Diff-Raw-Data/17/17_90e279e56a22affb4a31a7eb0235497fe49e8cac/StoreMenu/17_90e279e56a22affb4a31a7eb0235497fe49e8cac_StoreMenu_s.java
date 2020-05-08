 package edu.gatech.cs2340.ui;
 
 import java.awt.Font;
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.GroupLayout;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 
 import edu.gatech.cs2340.data.Player;
 import edu.gatech.cs2340.data.Store;
 import edu.gatech.cs2340.data.ResourceAmount.ResourceType;
 import edu.gatech.cs2340.io.StoreImageLoader;
 import edu.gatech.cs2340.sequencing.WaitedOn;
 
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 /**
  * @author Shreyyas Vanarase Function group: Model: UI Created for: M8 10/25/13
  *         Assigned to: Shreyyas Vanarase Modifications: M8 11/4/13 Shreyyas
  *         Vanarase Updated the menu for button disabling functionality and for
  *         proper mule selection.
  * 
  *         Purpose: Generates the store menu for the player to interact with the
  *         store.
  */
 public class StoreMenu extends JPanel implements WaitedOn {
 	private static final long serialVersionUID = 1L;
 	private int amount = 1;
 	private Player player;
 	private Store store;
 	
 	private StoreImageLoader imageLoader;
 	private JButton buyEnergy;
 	private JButton buySmithore;
 	private JButton buyFood;
 	private JButton buyCrystite;
 	private JButton sellEnergy;
 	private JButton sellSmithore;
 	private JButton sellFood;
 	private JButton sellCrystite;
 	private JButton buyEnergyMule;
 	private JButton buySmithoreMule;
 	private JButton buyFoodMule;
 	private JButton exitButton;
 	private boolean exitKilla;
 	private JLabel moneyLabel;
 	private JLabel playerFood;
 	private JLabel playerEnergy;
 	private JLabel playerSmithore;
 	private JLabel playerCrystite;
 	private JLabel playerMule;
 	private JLabel storeEnergy;
 	private JLabel storeFood;
 	private JLabel storeSmithore;
 	private JLabel storeCrystite;
 	private JLabel storeMule;
 
 	/**
 	 * #M8 Create the application.
 	 */
 	public StoreMenu(Player player, Store store) {
 		this.player = player;
 		this.store = store;
 		initialize();
 		refreshMenu();
 	}
 
 	/**
 	 * #M8 Initialize the contents of the panel and make the store menu. Allows
 	 * the store to buy/sell resources and Mules. Allows the player to choose
 	 * how much of each resource he/she wants and adds it to their resource
 	 * list.
 	 */
 
 	private void initialize() {
 		store.addPlayer(player);
 
 		imageLoader = new StoreImageLoader();
 
 		// Defines the panel and sets its bounds
 		setBackground(new Color(255, 255, 102));
 		setBounds(100, 100, 650, 500);
 
 		// Makes the Store Menu Label
 		JLabel title = new JLabel("Store Menu");
 		title.setHorizontalAlignment(SwingConstants.CENTER);
 		title.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 21));
 
 		// Button to sell energy to player (player is buying energy)
 		buyEnergy = new JButton("Energy");
 		buyEnergy.addActionListener(new BuyEnergyListener());
 		buyEnergy.setBackground(new Color(244, 50, 0));
 		buyEnergy.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 
 		// Button to sell smithore to player (player is buying smithore)
 		buySmithore = new JButton("Smithore");
 		buySmithore.addActionListener(new BuySmithoreListener());
 		buySmithore
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 		buySmithore.setBackground(new Color(244, 50, 0));
 
 		// Button to sell food to player (player is buying food)
 		buyFood = new JButton("Food");
 		buyFood.addActionListener(new BuyFoodListener());
 		buyFood.setBackground(new Color(244, 50, 0));
 		buyFood.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 
 		// Button to sell crystite to player (player is buying crystite)
 		buyCrystite = new JButton("Crystite");
 		buyCrystite.addActionListener(new BuyCrystiteListener());
 		buyCrystite
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 		buyCrystite.setBackground(new Color(244, 50, 0));
 
 		// Button to buy energy from player (player is selling energy)
 		sellEnergy = new JButton("Energy");
 		sellEnergy.addActionListener(new SellEnergyListener());
 		sellEnergy.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 		sellEnergy.setBackground(new Color(244, 50, 0));
 
 		// Button to buy smithore from player (player is selling smithore)
 		sellSmithore = new JButton("Smithore");
 		sellSmithore.addActionListener(new SellSmithoreListener());
 		sellSmithore
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 		sellSmithore.setBackground(new Color(244, 50, 0));
 
 		// Button to buy food from player (player is selling food)
 		sellFood = new JButton("Food");
 		sellFood.addActionListener(new SellFoodListener());
 		sellFood.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 		sellFood.setBackground(new Color(244, 50, 0));
 
 		// Button to buy crystite from player (player is selling crystite)
 		sellCrystite = new JButton("Crystite");
 		sellCrystite.addActionListener(new SellCrystiteListener());
 		sellCrystite
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 		sellCrystite.setBackground(new Color(244, 50, 0));
 
 		// Button to sell energy mule to player
 		buyEnergyMule = new JButton("Energy Mule");
 		buyEnergyMule.addActionListener(new BuyEnergyMuleListener());
 		buyEnergyMule.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				12));
 		buyEnergyMule.setBackground(new Color(244, 50, 0));
 
 		// Button to sell smithore mule to player
 		buySmithoreMule = new JButton("Ore Mule");
 		buySmithoreMule.addActionListener(new BuySmithoreMuleListener());
 		buySmithoreMule.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				12));
 		buySmithoreMule.setBackground(new Color(244, 50, 0));
 
 		// Button to sell food mule to player
 		buyFoodMule = new JButton("Food Mule");
 		buyFoodMule.addActionListener(new BuyFoodMuleListener());
 		buyFoodMule
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 		buyFoodMule.setBackground(new Color(244, 50, 0));
 
 		// Exits the store
 		exitButton = new JButton("Exit Store!");
 		exitButton.addActionListener(new ExitListener());
 		exitButton.setBackground(new Color(244, 50, 0));
 		exitButton.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 14));
 
 		/*
 		 * Makes labels for the resources and images
 		 */
 		JLabel foodIcon = new JLabel();
 		foodIcon.setIcon(imageLoader.getImage("food"));
 
 		JLabel energyIcon = new JLabel();
 		energyIcon.setIcon(imageLoader.getImage("energy"));
 
 		JLabel smithoreIcon = new JLabel();
 		smithoreIcon.setIcon(imageLoader.getImage("smithore"));
 
 		JLabel crystiteIcon = new JLabel();
 		crystiteIcon.setIcon(imageLoader.getImage("crystite"));
 
 		JLabel muleIcon = new JLabel();
 		muleIcon.setIcon(imageLoader.getImage("mule"));
 
 		// Makes a food label
 		JLabel foodLabel = new JLabel("Food");
 		foodLabel.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 
 		JLabel energyLabel = new JLabel("Energy");
 		energyLabel
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 
 		JLabel smithoreLabel = new JLabel("Smithore");
 		smithoreLabel.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				12));
 
 		JLabel crystiteLabel = new JLabel("Crystite");
 		crystiteLabel.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				12));
 
 		JLabel muleLabel = new JLabel("Mule");
 		muleLabel.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 
 		JLabel welcomeLabel = new JLabel(
 				"Welcome to Irata 1 Thrift Shop!! Best of the Resource Shops!");
 		welcomeLabel
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 14));
 
 		JSeparator buySellSeperator = new JSeparator();
 		buySellSeperator.setOrientation(SwingConstants.VERTICAL);
 		buySellSeperator.setForeground(new Color(204, 0, 0));
 		buySellSeperator.setBackground(new Color(204, 0, 0));
 
 		JSeparator sellStatsSeperator = new JSeparator();
 		sellStatsSeperator.setOrientation(SwingConstants.VERTICAL);
 		sellStatsSeperator.setForeground(new Color(204, 0, 0));
 		sellStatsSeperator.setBackground(new Color(204, 0, 0));
 
 		JLabel statisticsLabel = new JLabel("Statistics");
 		statisticsLabel.setForeground(new Color(0, 153, 51));
 		statisticsLabel.setFont(new Font("Copperplate Gothic Bold", Font.BOLD,
 				14));
 		statisticsLabel.setBackground(new Color(0, 153, 51));
 
 		JLabel playerMoney = new JLabel("Player Money:");
 		playerMoney
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
 
 		JLabel playerResources = new JLabel("Your Current Resources:");
 		playerResources.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				12));
 
 		JLabel storeResources = new JLabel("Store Resources: ");
 		storeResources.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				12));
 
 		JLabel energyPrice = new JLabel("$25");
 		energyPrice
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		JLabel smithorePrice = new JLabel("$50");
 		smithorePrice.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				11));
 
 		JLabel foodPrice = new JLabel("$30");
 		foodPrice.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		JLabel crystitePrice = new JLabel("$100");
 		crystitePrice.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				11));
 
 		JLabel mulePrice = new JLabel("$100");
 		mulePrice.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		// Makes a label for the Buying section
 		JLabel buyTitle = new JLabel("Buy from the Store!");
 		buyTitle.setForeground(new Color(0, 153, 51));
 		buyTitle.setFont(new Font("Copperplate Gothic Bold", Font.BOLD, 14));
 
 		// Makes a label for the Selling section
 		JLabel sellTitle = new JLabel("Sell to the Store!");
 		sellTitle.setForeground(new Color(0, 153, 51));
 		sellTitle.setBackground(new Color(0, 153, 51));
 		sellTitle.setFont(new Font("Copperplate Gothic Bold", Font.BOLD, 14));
 
 		// Separates the selling title and the selling section
 		JSeparator mainSeperator = new JSeparator();
 		mainSeperator.setBackground(new Color(204, 0, 0));
 		mainSeperator.setForeground(new Color(244, 50, 0));
 
 		moneyLabel = new JLabel(player.getMoney() + "");
 		moneyLabel.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		playerEnergy = new JLabel("Energy: "
 				+ player.getResourceAmount(ResourceType.ENERGY));
 		playerEnergy
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		playerFood = new JLabel("Food: "
 				+ player.getResourceAmount(ResourceType.FOOD));
 		playerFood.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		playerSmithore = new JLabel("Smithore: "
 				+ player.getResourceAmount(ResourceType.SMITHORE));
 		playerSmithore.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				11));
 
 		playerCrystite = new JLabel("Crystite: "
 				+ player.getResourceAmount(ResourceType.CRYSTITE));
 		playerCrystite.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				11));
 
 		playerMule = new JLabel("Mule: " + player.getMuleAmount());
 		playerMule.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		storeEnergy = new JLabel("Energy: "
 				+ store.getResourceAmount(ResourceType.ENERGY));
 		storeEnergy
 				.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		storeFood = new JLabel("Food: "
 				+ store.getResourceAmount(ResourceType.FOOD));
 		storeFood.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 
 		storeSmithore = new JLabel("Smithore: "
 				+ store.getResourceAmount(ResourceType.SMITHORE));
 		storeSmithore.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				11));
 
 		storeCrystite = new JLabel("Crystite: "
 				+ store.getResourceAmount(ResourceType.CRYSTITE));
 		storeCrystite.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN,
 				11));
 
 		storeMule = new JLabel("Mule:  " + store.getMuleAmount());
 		storeMule.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
 		GroupLayout groupLayout = new GroupLayout(this);
 		groupLayout
 				.setHorizontalGroup(groupLayout
 						.createParallelGroup(Alignment.LEADING)
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(227)
 										.addComponent(title,
 												GroupLayout.PREFERRED_SIZE,
 												189, GroupLayout.PREFERRED_SIZE))
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(74)
 										.addComponent(welcomeLabel,
 												GroupLayout.PREFERRED_SIZE,
 												509, GroupLayout.PREFERRED_SIZE))
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(43)
 										.addComponent(energyIcon,
 												GroupLayout.PREFERRED_SIZE, 39,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(63)
 										.addComponent(smithoreIcon,
 												GroupLayout.PREFERRED_SIZE, 72,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(62)
 										.addComponent(foodIcon,
 												GroupLayout.PREFERRED_SIZE, 80,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(48)
 										.addComponent(crystiteIcon,
 												GroupLayout.PREFERRED_SIZE, 72,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(54)
 										.addComponent(muleIcon,
 												GroupLayout.PREFERRED_SIZE, 75,
 												GroupLayout.PREFERRED_SIZE))
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(37)
 										.addComponent(energyLabel,
 												GroupLayout.PREFERRED_SIZE, 72,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(46)
 										.addComponent(smithoreLabel,
 												GroupLayout.PREFERRED_SIZE, 82,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(63)
 										.addComponent(foodLabel,
 												GroupLayout.PREFERRED_SIZE, 46,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(71)
 										.addComponent(crystiteLabel,
 												GroupLayout.PREFERRED_SIZE, 87,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(58)
 										.addComponent(muleLabel,
 												GroupLayout.PREFERRED_SIZE, 56,
 												GroupLayout.PREFERRED_SIZE))
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(51)
 										.addComponent(energyPrice,
 												GroupLayout.PREFERRED_SIZE, 35,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(89)
 										.addComponent(smithorePrice)
 										.addGap(106)
 										.addComponent(foodPrice,
 												GroupLayout.PREFERRED_SIZE, 35,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(85)
 										.addComponent(crystitePrice,
 												GroupLayout.PREFERRED_SIZE, 46,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(91)
 										.addComponent(mulePrice,
 												GroupLayout.PREFERRED_SIZE, 46,
 												GroupLayout.PREFERRED_SIZE))
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(37)
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.LEADING)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(14)
 																		.addGroup(
 																				groupLayout
 																						.createParallelGroup(
 																								Alignment.LEADING)
 																						.addComponent(
 																								buyEnergyMule,
 																								GroupLayout.PREFERRED_SIZE,
 																								135,
 																								GroupLayout.PREFERRED_SIZE)
 																						.addComponent(
 																								buyFoodMule,
 																								GroupLayout.PREFERRED_SIZE,
 																								135,
 																								GroupLayout.PREFERRED_SIZE)
 																						.addComponent(
 																								buySmithoreMule,
 																								GroupLayout.PREFERRED_SIZE,
 																								135,
 																								GroupLayout.PREFERRED_SIZE))
 																		.addGap(68)
 																		.addGroup(
 																				groupLayout
 																						.createParallelGroup(
 																								Alignment.LEADING)
 																						.addComponent(
 																								sellTitle)
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(23)
 																										.addComponent(
 																												sellSmithore,
 																												GroupLayout.PREFERRED_SIZE,
 																												111,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(23)
 																										.addComponent(
 																												sellFood,
 																												GroupLayout.PREFERRED_SIZE,
 																												111,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(23)
 																										.addComponent(
 																												sellCrystite,
 																												GroupLayout.PREFERRED_SIZE,
 																												111,
 																												GroupLayout.PREFERRED_SIZE)))
 																		.addGap(19)
 																		.addComponent(
 																				sellStatsSeperator,
 																				GroupLayout.PREFERRED_SIZE,
 																				GroupLayout.DEFAULT_SIZE,
 																				GroupLayout.PREFERRED_SIZE)
 																		.addGap(10)
 																		.addGroup(
 																				groupLayout
 																						.createParallelGroup(
 																								Alignment.LEADING)
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(35)
 																										.addComponent(
 																												statisticsLabel))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(1)
 																										.addComponent(
 																												playerMoney,
 																												GroupLayout.PREFERRED_SIZE,
 																												192,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addComponent(
 																								playerResources,
 																								GroupLayout.PREFERRED_SIZE,
 																								193,
 																								GroupLayout.PREFERRED_SIZE)
 																						.addComponent(
 																								playerEnergy,
 																								GroupLayout.PREFERRED_SIZE,
 																								95,
 																								GroupLayout.PREFERRED_SIZE)
 																						.addComponent(
 																								playerFood)
 																						.addComponent(
 																								storeSmithore,
 																								GroupLayout.PREFERRED_SIZE,
 																								136,
 																								GroupLayout.PREFERRED_SIZE)
 																						.addComponent(
 																								storeCrystite,
 																								GroupLayout.PREFERRED_SIZE,
 																								95,
 																								GroupLayout.PREFERRED_SIZE)))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(25)
 																		.addComponent(
 																				buyEnergy,
 																				GroupLayout.PREFERRED_SIZE,
 																				111,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(190)
 																		.addComponent(
 																				buySellSeperator,
 																				GroupLayout.PREFERRED_SIZE,
 																				GroupLayout.DEFAULT_SIZE,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(410)
 																		.addComponent(
 																				playerSmithore,
 																				GroupLayout.PREFERRED_SIZE,
 																				95,
 																				GroupLayout.PREFERRED_SIZE))
 														.addComponent(buyTitle)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(25)
 																		.addComponent(
 																				buyFood,
 																				GroupLayout.PREFERRED_SIZE,
 																				111,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(240)
 																		.addComponent(
 																				sellEnergy,
 																				GroupLayout.PREFERRED_SIZE,
 																				111,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(25)
 																		.addComponent(
 																				buySmithore,
 																				GroupLayout.PREFERRED_SIZE,
 																				111,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(410)
 																		.addComponent(
 																				storeFood,
 																				GroupLayout.PREFERRED_SIZE,
 																				95,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(25)
 																		.addComponent(
 																				buyCrystite,
 																				GroupLayout.PREFERRED_SIZE,
 																				111,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(410)
 																		.addComponent(
 																				moneyLabel,
 																				GroupLayout.PREFERRED_SIZE,
 																				46,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(410)
 																		.addComponent(
 																				playerCrystite,
 																				GroupLayout.PREFERRED_SIZE,
 																				95,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(410)
 																		.addComponent(
 																				storeResources,
 																				GroupLayout.PREFERRED_SIZE,
 																				171,
 																				GroupLayout.PREFERRED_SIZE))
 														.addComponent(
 																mainSeperator,
 																GroupLayout.PREFERRED_SIZE,
 																581,
 																GroupLayout.PREFERRED_SIZE)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(410)
 																		.addComponent(
 																				storeEnergy,
 																				GroupLayout.PREFERRED_SIZE,
 																				95,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(410)
 																		.addComponent(
 																				playerMule,
 																				GroupLayout.PREFERRED_SIZE,
 																				151,
 																				GroupLayout.PREFERRED_SIZE))))
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(277)
 										.addComponent(exitButton)
 										.addGap(49)
 										.addComponent(storeMule,
 												GroupLayout.PREFERRED_SIZE, 95,
 												GroupLayout.PREFERRED_SIZE)));
 		groupLayout
 				.setVerticalGroup(groupLayout
 						.createParallelGroup(Alignment.LEADING)
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addGap(12)
 										.addComponent(title,
 												GroupLayout.PREFERRED_SIZE, 31,
 												GroupLayout.PREFERRED_SIZE)
 										.addComponent(welcomeLabel,
 												GroupLayout.PREFERRED_SIZE, 14,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(11)
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.LEADING)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(4)
 																		.addComponent(
 																				energyIcon,
 																				GroupLayout.PREFERRED_SIZE,
 																				62,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(1)
 																		.addComponent(
 																				smithoreIcon,
 																				GroupLayout.PREFERRED_SIZE,
 																				71,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(5)
 																		.addComponent(
 																				foodIcon,
 																				GroupLayout.PREFERRED_SIZE,
 																				60,
 																				GroupLayout.PREFERRED_SIZE))
 														.addComponent(
 																crystiteIcon,
 																GroupLayout.PREFERRED_SIZE,
 																71,
 																GroupLayout.PREFERRED_SIZE)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(5)
 																		.addComponent(
 																				muleIcon,
 																				GroupLayout.PREFERRED_SIZE,
 																				60,
 																				GroupLayout.PREFERRED_SIZE)))
 										.addGap(4)
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.LEADING)
 														.addComponent(
 																energyLabel)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(1)
 																		.addComponent(
 																				smithoreLabel))
 														.addComponent(foodLabel)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(1)
 																		.addComponent(
 																				crystiteLabel))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(1)
 																		.addComponent(
 																				muleLabel)))
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.LEADING)
 														.addComponent(
 																energyPrice,
 																GroupLayout.PREFERRED_SIZE,
 																14,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																smithorePrice,
 																GroupLayout.PREFERRED_SIZE,
 																14,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																foodPrice,
 																GroupLayout.PREFERRED_SIZE,
 																14,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																crystitePrice,
 																GroupLayout.PREFERRED_SIZE,
 																14,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																mulePrice,
 																GroupLayout.PREFERRED_SIZE,
 																14,
 																GroupLayout.PREFERRED_SIZE))
 										.addGap(11)
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.LEADING)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(175)
 																		.addComponent(
 																				buyEnergyMule)
 																		.addGap(9)
 																		.addComponent(
 																				buyFoodMule)
 																		.addGap(11)
 																		.addComponent(
 																				buySmithoreMule))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(4)
 																		.addComponent(
 																				sellTitle,
 																				GroupLayout.PREFERRED_SIZE,
 																				14,
 																				GroupLayout.PREFERRED_SIZE)
 																		.addGap(55)
 																		.addComponent(
 																				sellSmithore)
 																		.addGap(11)
 																		.addComponent(
 																				sellFood)
 																		.addGap(11)
 																		.addComponent(
 																				sellCrystite))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(20)
 																		.addComponent(
 																				sellStatsSeperator,
 																				GroupLayout.PREFERRED_SIZE,
 																				249,
 																				GroupLayout.PREFERRED_SIZE))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(39)
 																		.addComponent(
 																				buyEnergy))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(20)
 																		.addComponent(
 																				buySellSeperator,
 																				GroupLayout.PREFERRED_SIZE,
 																				249,
 																				GroupLayout.PREFERRED_SIZE))
 														.addComponent(
 																buyTitle,
 																GroupLayout.PREFERRED_SIZE,
 																23,
 																GroupLayout.PREFERRED_SIZE)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(107)
 																		.addComponent(
 																				buyFood))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(39)
 																		.addComponent(
 																				sellEnergy))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(73)
 																		.addComponent(
 																				buySmithore))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(141)
 																		.addComponent(
 																				buyCrystite))
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGroup(
 																				groupLayout
 																						.createParallelGroup(
 																								Alignment.LEADING)
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(4)
 																										.addComponent(
 																												statisticsLabel,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE)
 																										.addGap(21)
 																										.addComponent(
 																												playerMoney)
 																										.addGap(23)
 																										.addComponent(
 																												playerResources)
 																										.addGap(11)
 																										.addComponent(
 																												playerEnergy,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE)
 																										.addGap(1)
 																										.addComponent(
 																												playerFood,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(134)
 																										.addComponent(
 																												playerSmithore,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(56)
 																										.addComponent(
 																												moneyLabel,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(151)
 																										.addComponent(
 																												playerCrystite,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(20)
 																										.addComponent(
 																												mainSeperator,
 																												GroupLayout.PREFERRED_SIZE,
 																												8,
 																												GroupLayout.PREFERRED_SIZE)))
 																		.addGap(3)
 																		.addComponent(
 																				playerMule,
 																				GroupLayout.PREFERRED_SIZE,
 																				14,
 																				GroupLayout.PREFERRED_SIZE)
 																		.addPreferredGap(
 																				ComponentPlacement.RELATED)
 																		.addGroup(
 																				groupLayout
 																						.createParallelGroup(
 																								Alignment.LEADING)
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(63)
 																										.addComponent(
 																												storeSmithore,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE)
 																										.addGap(3)
 																										.addComponent(
 																												storeCrystite,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(45)
 																										.addComponent(
 																												storeFood,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(11)
 																										.addComponent(
 																												storeResources))
 																						.addGroup(
 																								groupLayout
 																										.createSequentialGroup()
 																										.addGap(30)
 																										.addComponent(
 																												storeEnergy,
 																												GroupLayout.PREFERRED_SIZE,
 																												14,
 																												GroupLayout.PREFERRED_SIZE)))))
 										.addGap(3)
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.LEADING)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addGap(3)
 																		.addComponent(
 																				exitButton,
 																				GroupLayout.PREFERRED_SIZE,
 																				23,
 																				GroupLayout.PREFERRED_SIZE))
 														.addComponent(
 																storeMule,
 																GroupLayout.PREFERRED_SIZE,
 																14,
 																GroupLayout.PREFERRED_SIZE))));
 		setLayout(groupLayout);
 
 	}
 
 	private class BuyEnergyListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.sellResources(ResourceType.ENERGY, amount);
 			refreshMenu();
 		}
 	}
 
 	private class BuySmithoreListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.sellResources(ResourceType.SMITHORE, amount);
 			refreshMenu();
 		}
 	}
 
 	private class BuyCrystiteListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.sellResources(ResourceType.CRYSTITE, amount);
 			refreshMenu();
 		}
 	}
 
 	private class BuyFoodListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.sellResources(ResourceType.FOOD, amount);
 			buyFood.setEnabled(true);
 			refreshMenu();
 		}
 	}
 
 	private class SellEnergyListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.buyResources(ResourceType.ENERGY, amount);
 			refreshMenu();
 		}
 	}
 
 	private class SellSmithoreListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.buyResources(ResourceType.SMITHORE, amount);
 			refreshMenu();
 		}
 	}
 
 	private class SellFoodListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.buyResources(ResourceType.FOOD, amount);
 			refreshMenu();
 		}
 	}
 
 	private class SellCrystiteListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.buyResources(ResourceType.CRYSTITE, amount);
 			refreshMenu();
 		}
 	}
 
 	private class BuyEnergyMuleListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.sellMule(ResourceType.ENERGY);
 			refreshMenu();
 		}
 	}
 
 	private class BuySmithoreMuleListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.sellMule(ResourceType.SMITHORE);
 			refreshMenu();
 		}
 	}
 
 	private class BuyFoodMuleListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			store.sellMule(ResourceType.FOOD);
 			refreshMenu();
 		}
 	}
 
 	private class ExitListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			exitKilla = true;
 		}
 	}
 
 	/**
 	 * Allows you to exit the store when the Exit Store button is pressed.
 	 */
 	public boolean isFinished() {
 		return exitKilla;
 	}
 
 	/**
 	 * Resets the exitKilla to allow you to reenter the storemenu.
 	 * 
 	 * @return
 	 */
 	public boolean reset() {
 		return (exitKilla = false);
 	}
 
 	/**
 	 * Refreshes the store menu.
 	 */
 	public void refreshMenu() {
 		moneyLabel.setText(player.getMoney() + "");
 		playerEnergy.setText("Energy: "
 				+ player.getResourceAmount(ResourceType.ENERGY));
 		playerFood.setText("Food: "
 				+ player.getResourceAmount(ResourceType.FOOD));
 		playerSmithore.setText("Smithore: "
 				+ player.getResourceAmount(ResourceType.SMITHORE));
 		playerCrystite.setText("Crystite: "
 				+ player.getResourceAmount(ResourceType.CRYSTITE));
 		if (player.hazMule()) {
 			playerMule.setText("Mule: " + player.getMuleAmount() + " , "
 					+ player.getMule().toString());
 		}
 		storeEnergy.setText("Energy: "
 				+ store.getResourceAmount(ResourceType.ENERGY));
 		storeFood.setText("Food: "
 				+ store.getResourceAmount(ResourceType.FOOD));
 		storeSmithore.setText("Smithore: "
 				+ store.getResourceAmount(ResourceType.SMITHORE));
 		storeCrystite.setText("Crystite: "
 				+ store.getResourceAmount(ResourceType.CRYSTITE));
 		storeMule.setText("Mule:  " + store.getMuleAmount());
 
 		resourceButtonDisable(ResourceType.ENERGY, buyEnergy);
 		resourceButtonDisable(ResourceType.FOOD, buyFood);
 		resourceButtonDisable(ResourceType.SMITHORE, buySmithore);
 		resourceButtonDisable(ResourceType.CRYSTITE, buyCrystite);
 
 		muleButtonDisable(ResourceType.ENERGY, buyEnergyMule);
 		muleButtonDisable(ResourceType.FOOD, buyFoodMule);
 		muleButtonDisable(ResourceType.SMITHORE, buySmithoreMule);
 	}
 
 	/**
 	 * Disables button if player doesn't have enough money for the transaction.
 	 * 
 	 * @param type
 	 * @param button
 	 */
 	public void resourceButtonDisable(ResourceType type, JButton button) {
 		if (player.getMoney() < store.getResourcePrice(type)) {
 			button.setEnabled(false);
 		} else
 			button.setEnabled(true);
 	}
 
 	/**
 	 * Disables button if the player has a mule or if the player doesn't have
 	 * enough money for the transaction.
 	 * 
 	 * @param type
 	 * @param button
 	 */
 	public void muleButtonDisable(ResourceType type, JButton button) {
 		if (player.hazMule()
 				|| player.getMoney() < store.getResourcePrice(type)) {
 			button.setEnabled(false);
 		} else
 			button.setEnabled(true);
 	}
 }
