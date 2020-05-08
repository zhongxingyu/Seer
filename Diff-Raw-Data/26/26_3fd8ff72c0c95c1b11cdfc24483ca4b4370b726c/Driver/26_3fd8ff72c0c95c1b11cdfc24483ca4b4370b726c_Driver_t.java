 package edu.gatech.statusquo.spacetrader.driver;
 
 import edu.gatech.statusquo.spacetrader.model.*;
 import edu.gatech.statusquo.spacetrader.model.Good.GoodType;
 import edu.gatech.statusquo.spacetrader.presenter.*;
 import edu.gatech.statusquo.spacetrader.view.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 public class Driver {
 	Player player;
 	public Display display;
 	public Shell shell;
 	public static int currency = 1000;
 	static ArrayList<Integer> totalSkills;
 	ArrayList<Integer> partySkills;
 	private static ArrayList<Integer> X; // Contains randomly generated X
 	private static ArrayList<Integer> Y; // Contains randomly generated Y
 	public static ArrayList<String> listOfNames; // Contains randomly picked names
 	public SolarSystem solarSystem;	 // initial Solar system to give trade good presenter
 	
 	public ShipStatisticsView shipStatisticsView;
 	public TeamStatisticsView teamStatisticsView;
 	public SolarSystemStatsView solarSystemStatsView;
 	public TradeGoodsView tradeGoodsView;
 	public NotificationsView notificationsView;
 	public VitalsView vitalsView;
 	public LocalPlanetView localPlanetView;
 	
 	private static HashMap<GoodType, Integer> basePrice;   //map contaning all base prices
 	private static HashMap<GoodType, Integer> baseQty;     //map containing all base quantities
 	private final static double  PRICEPERCENT = .25;       //value used to increase or decrease prices
 	private final static double  QTYPERCENT = .50;         //value used to increase or decrease quantity
 	private static ArrayList<SolarSystem> listOfSystems;   //contains list of all SolarSystems
 
 	/*
 	 * Driver's constructor
 	 */
 	public Driver() throws IOException {
 		player = new Player();
 		
 		//fill in base prices
 		basePrice = new HashMap<GoodType, Integer>();
 		basePrice.put(GoodType.WATER, 30);
 		basePrice.put(GoodType.FUR, 250);
 		basePrice.put(GoodType.FOOD, 100);
 		basePrice.put(GoodType.ORE, 350);
 		basePrice.put(GoodType.FIREARM, 1200);
 		basePrice.put(GoodType.MEDICINE, 650);
 		basePrice.put(GoodType.MACHINE, 900);
 		basePrice.put(GoodType.NARCOTIC, 3500);
 		basePrice.put(GoodType.ROBOT, 5000);
 		
 		//fill in base quantity
 		baseQty = new HashMap<GoodType, Integer>();
 		baseQty.put(GoodType.WATER, 25);
 		baseQty.put(GoodType.FUR, 10);
 		baseQty.put(GoodType.FOOD, 20);
 		baseQty.put(GoodType.ORE, 20);
 		baseQty.put(GoodType.FIREARM, 5);
 		baseQty.put(GoodType.MEDICINE, 15);
 		baseQty.put(GoodType.MACHINE, 10);
 		baseQty.put(GoodType.NARCOTIC, 5);
 		baseQty.put(GoodType.ROBOT, 5);
 		
 		//represents a list of solar systems
 		listOfSystems = new ArrayList<SolarSystem>();
 		generateUniverse();
 		WelcomeView welcomeView = new WelcomeView();
 		new WelcomePresenter(this, welcomeView);
 	}
 	
 	/*
 	 * Main method of SpaceTrader game and creates driver object
 	 */
 	public static void main(String[] args) {
 		try {
 			new Driver();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void generateCreatePlayer() {
 		CreatePlayerView createPlayerView = new CreatePlayerView();
 		new CreatePlayerPresenter(this, createPlayerView, player);
 	}
 	
 	public void generateMainGame() {
 		display = Display.getDefault();
 		shell = new Shell(display, SWT.TITLE | SWT.CLOSE);
 		
 		shell.open();
 		shell.layout();
 		
 		shell.setSize(1024, 768);
 		shell.setText("Space Trader");
 		shell.setLayout(null);
 		
 		shipStatisticsView = new ShipStatisticsView(shell);
 		teamStatisticsView = new TeamStatisticsView(shell, player);
 		solarSystemStatsView = new SolarSystemStatsView(shell);
 		tradeGoodsView = new TradeGoodsView(shell);
 		notificationsView = new NotificationsView(shell, player);
 		vitalsView = new VitalsView(shell, player);
 		localPlanetView = new LocalPlanetView(shell);
 		
 		new ShipStatisticsPresenter(shell, this, shipStatisticsView);
 		new TeamStatisticsPresenter(shell, this, teamStatisticsView);
 		new SolarSystemStatsPresenter(shell, this, solarSystemStatsView);
 		new TradeGoodsPresenter(shell, this, tradeGoodsView, player, listOfSystems.get(0),vitalsView);
 		new NotificationsPresenter(shell, this, notificationsView);
 		new VitalsPresenter(shell, this, vitalsView, player, tradeGoodsView);
 		new LocalPlanetPresenter(shell, this, localPlanetView);
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Creates the universe that the game will be played in.
 	 * 
 	 * @throws IOException
 	 */
 	public static void generateUniverse() throws IOException {
 
 		generateCoordinates();
 		generateNames();
 
 		// copies global variables into local
 		ArrayList<Integer> xList = X;
 		ArrayList<Integer> yList = Y;
 		ArrayList<String> namesList = listOfNames;
 /*		ArrayList<SolarSystem> systemsList = new ArrayList<SolarSystem>();
 */
 		// Generates a SolarSystem object using the generated
 		// coordinates and names.
 		for (int i = 0; i < namesList.size(); i++) {
			int techLevel = (int) (Math.random() * 7);
 			int resourceLevel = (int) (Math.random() * 11);
 			String holdName = namesList.get(i);
 			int holdX = xList.get(i);
 			int holdY = yList.get(i);
 			Planet planetName = new Planet(holdName);
 			SolarSystem system = new SolarSystem(holdName, planetName,
 					techLevel, resourceLevel, holdX, holdY);
 			generateMarket(system);
 			listOfSystems.add(system);
 			// **TEMP PRINTS TO CONSOLE**
 			//SolarSystem printThis = listOfSystems.get(i);
 			//System.out.println(printThis.toString() + "\n");
 		}
 	}
 	
 	/**
 	 * Generates a market stated by the game rules
 	 * An empty market is manipulated to match the SolarSystems technology
 	 * and resource levels.
 	 * @param s
 	 */
 	
 	public static void generateMarket(SolarSystem s) {
 		int techLvl = s.getTechLevel();
 		int resLvl = s.getResourceLevel();
 		HashMap<GoodType, Integer> mPrices = s.getMarketPrice();
 		HashMap<GoodType, Integer> mQtys = s.getMarketQuantity();
 
 	    //Setting market quantity
         //**Needs to be proof read to see if variables and quantities are matching**
 		switch(techLvl){
 			case 0:
 				//if tech level is 0, only resources avilable are water and fur
 				//baseQtys set in constructor
 				mQtys.put(GoodType.WATER, baseQty.get(GoodType.WATER));
 				//new quantity is a 50% increase
 				int newFurQty = (int) ((baseQty.get(GoodType.FUR)) + ((baseQty.get(GoodType.FUR))*QTYPERCENT));
 				mQtys.put(GoodType.FUR, newFurQty);
 				//Rest are initialized to be 0, so no other changes.
 				break;
 			case 1:
 				mQtys.put(GoodType.WATER, baseQty.get(GoodType.WATER));
 				mQtys.put(GoodType.FUR, baseQty.get(GoodType.FUR));
 				int newFoodQty = (int) ((baseQty.get(GoodType.FOOD)) + ((baseQty.get(GoodType.FOOD))*QTYPERCENT));
 				mQtys.put(GoodType.FOOD, newFoodQty);
 				break;
 			case 2:
 			    int newWaterQty = (int) ((baseQty.get(GoodType.WATER)) + ((baseQty.get(GoodType.WATER))*QTYPERCENT));
 			    mQtys.put(GoodType.WATER, newWaterQty);
 			    mQtys.put(GoodType.FUR, baseQty.get(GoodType.FUR));
 			    mQtys.put(GoodType.FOOD, baseQty.get(GoodType.FOOD));
 			    mQtys.put(GoodType.ORE, baseQty.get(GoodType.ORE));
 			    break;
 			case 3:
 			    mQtys.put(GoodType.WATER, baseQty.get(GoodType.WATER));
 	            mQtys.put(GoodType.FUR, baseQty.get(GoodType.FUR));
 	            mQtys.put(GoodType.FOOD, baseQty.get(GoodType.FOOD));
 	            int newOreQty = (int) ((baseQty.get(GoodType.ORE)) + ((baseQty.get(GoodType.ORE))*QTYPERCENT));
 	            mQtys.put(GoodType.ORE, newOreQty);
 	            mQtys.put(GoodType.FIREARM, baseQty.get(GoodType.FIREARM));
 	            break;
 			case 4: 
 	             mQtys.put(GoodType.WATER, baseQty.get(GoodType.WATER));
 	             mQtys.put(GoodType.FUR, baseQty.get(GoodType.FUR));
 	             mQtys.put(GoodType.FOOD, baseQty.get(GoodType.FOOD));
 	             mQtys.put(GoodType.ORE, baseQty.get(GoodType.ORE));
 	             mQtys.put(GoodType.FIREARM, baseQty.get(GoodType.FIREARM));
 	             mQtys.put(GoodType.MEDICINE, baseQty.get(GoodType.MEDICINE));
 	             mQtys.put(GoodType.MACHINE, baseQty.get(GoodType.MACHINE));
 	             break;
 			case 5:
 	             mQtys.put(GoodType.WATER, baseQty.get(GoodType.WATER));
 	             mQtys.put(GoodType.FUR, baseQty.get(GoodType.FUR));
 	             mQtys.put(GoodType.FOOD, baseQty.get(GoodType.FOOD));
 	             mQtys.put(GoodType.ORE, baseQty.get(GoodType.ORE));
 	             int newFireQty = (int) ((baseQty.get(GoodType.FIREARM)) + ((baseQty.get(GoodType.FIREARM))*QTYPERCENT));
 	             mQtys.put(GoodType.FIREARM, newFireQty);
 	             mQtys.put(GoodType.MEDICINE, baseQty.get(GoodType.MEDICINE));
 	             int newMachQty = (int) ((baseQty.get(GoodType.MACHINE)) + ((baseQty.get(GoodType.MACHINE))*QTYPERCENT));
 	             mQtys.put(GoodType.MACHINE, newMachQty);
 	             int newNarcQty = (int) ((baseQty.get(GoodType.NARCOTIC)) + ((baseQty.get(GoodType.NARCOTIC))*QTYPERCENT));
 	             mQtys.put(GoodType.NARCOTIC, newNarcQty);
 	             break;
 			case 6: 
 	             mQtys.put(GoodType.WATER, baseQty.get(GoodType.WATER));
                  mQtys.put(GoodType.FUR, baseQty.get(GoodType.FUR));
                  mQtys.put(GoodType.FOOD, baseQty.get(GoodType.FOOD));
                  mQtys.put(GoodType.ORE, baseQty.get(GoodType.ORE));
                  mQtys.put(GoodType.FIREARM, baseQty.get(GoodType.FIREARM));
                  int newMedQty = (int) ((baseQty.get(GoodType.MEDICINE)) + ((baseQty.get(GoodType.MEDICINE))*QTYPERCENT));
                  mQtys.put(GoodType.MEDICINE, newMedQty);
                  mQtys.put(GoodType.MACHINE, baseQty.get(GoodType.MACHINE));
                  mQtys.put(GoodType.NARCOTIC, baseQty.get(GoodType.NARCOTIC));
                  mQtys.put(GoodType.ROBOT, baseQty.get(GoodType.ROBOT));
                  break;
 		}
 
 		//Sets prices of all goods to its basePrice
 		mPrices.put(GoodType.WATER, basePrice.get(GoodType.WATER));
 	    mPrices.put(GoodType.FUR, basePrice.get(GoodType.FUR));
 	    mPrices.put(GoodType.FOOD, basePrice.get(GoodType.FOOD));
 	    mPrices.put(GoodType.ORE, basePrice.get(GoodType.ORE));
 	    mPrices.put(GoodType.FIREARM, basePrice.get(GoodType.FIREARM));
 	    mPrices.put(GoodType.MEDICINE, basePrice.get(GoodType.MEDICINE));
 	    mPrices.put(GoodType.MACHINE, basePrice.get(GoodType.MACHINE));
 	    mPrices.put(GoodType.NARCOTIC, basePrice.get(GoodType.NARCOTIC));
 	    mPrices.put(GoodType.ROBOT, basePrice.get(GoodType.ROBOT));
 	    
 	    // Respectively increases/decreases depending on generation rules
 	    // Generation rules are described in the "SpaceTrader: Game Information" 
 		switch(resLvl){
     		case 0:
    		    int lotsOfWater = (int) ((basePrice.get(GoodType.WATER)) - ((basePrice.get(GoodType.WATER))*PRICEPERCENT));
     		    mPrices.put(GoodType.WATER, lotsOfWater);
     		    break;
     		case 1:
                int drought = (int) ((basePrice.get(GoodType.WATER)) + ((basePrice.get(GoodType.WATER))*PRICEPERCENT));
                 mPrices.put(GoodType.WATER, drought);
                 break;
             case 2:
                int richFauna = (int) ((basePrice.get(GoodType.FUR)) - ((basePrice.get(GoodType.FUR))*PRICEPERCENT));
                 mPrices.put(GoodType.FUR, richFauna);
                 break;
             case 3:
                int lifeless = (int) ((basePrice.get(GoodType.FUR)) + ((basePrice.get(GoodType.FUR))*PRICEPERCENT));
                 mPrices.put(GoodType.FUR, lifeless);
                 break;
             case 4:
                int richSoil = (int) ((basePrice.get(GoodType.FOOD)) - ((basePrice.get(GoodType.FOOD))*PRICEPERCENT));
                 mPrices.put(GoodType.FOOD, richSoil);
                 break;
             case 5: 
                int poorSoil = (int) ((basePrice.get(GoodType.FOOD)) + ((basePrice.get(GoodType.FOOD))*PRICEPERCENT));
                 mPrices.put(GoodType.FOOD, poorSoil);
                 break;
             case 6:
                int mineralRich = (int) ((basePrice.get(GoodType.ORE)) - ((basePrice.get(GoodType.ORE))*PRICEPERCENT));
                 mPrices.put(GoodType.ORE, mineralRich);
             case 7:
                int mineralPoor = (int) ((basePrice.get(GoodType.ORE)) + ((basePrice.get(GoodType.ORE))*PRICEPERCENT));
                 mPrices.put(GoodType.ORE, mineralPoor);
                 break;
             case 8:
                 int warLike = (int) ((basePrice.get(GoodType.FIREARM)) - ((basePrice.get(GoodType.FIREARM))*PRICEPERCENT));
                 mPrices.put(GoodType.FIREARM, warLike);
                 break;
             case 9:
                 int lotsOfHerbs = (int) ((basePrice.get(GoodType.MEDICINE)) - ((basePrice.get(GoodType.MEDICINE))*PRICEPERCENT));
                 mPrices.put(GoodType.MEDICINE, lotsOfHerbs);
                 break;
             case 10:
                 int weirdMushrooms = (int) ((basePrice.get(GoodType.NARCOTIC)) - ((basePrice.get(GoodType.NARCOTIC))*PRICEPERCENT));
                 mPrices.put(GoodType.NARCOTIC, weirdMushrooms);
                 break;
 		}
 		
 		s.setMarketPrice(mPrices);
 		s.setMarketQuantity(mQtys);
 		
 		
 	}
 
 	/**
 	 * Creates list of usable X and Y coordinates for solar systems. That are at
 	 * least 10 units away from any other coordinate.
 	 */
 	public static void generateCoordinates() {
 
 		int amtOfCoordinates = 150; // Amount of coordinates to generate
 		ArrayList<Integer> holdX = new ArrayList<Integer>(); // List to hold X coordinates
 		ArrayList<Integer> holdY = new ArrayList<Integer>(); // Lost to hold Y coordinates
 		
 		//will loop until proper amt of coordinates have been generated
 		while (amtOfCoordinates > 0) { 
 			boolean goodCoords = false;
 			//updates length of arraylist
 			int arrayLength = holdX.size();
 
 			//will loop until good coordinates have been found
 			while (goodCoords != true) {
 				int trueCounter = 0;
 				//randomly picks two integers for X and Y.
 				int testX = (int) (Math.random() * 201);
 				int testY = (int) (Math.random() * 201);
 				/* Goes through current array to make sure randomly picked X and
 				Y are at a distance of 10 or more then all other coordinates. */
 				for (int i = 0; i < arrayLength; i++) {
 					int tempX = holdX.get(i);
 					int tempY = holdY.get(i);
 
 					int distance = (int) ((Math.pow(tempX - testX, 2)) + (Math
 							.pow(tempY - testY, 2)));
 					distance = (int) (Math.sqrt(distance));
 					/* The randomly picked numbers are >= to coordinates at index i then 
 					add to the trueCounter. Else break and generate new coordinates. */
 					if (distance >= 10) {
 						trueCounter++;
 					} else {
 						break;
 					}
 				}
 				if (trueCounter == arrayLength) {
 					holdX.add(testX);
 					holdY.add(testY);
 					goodCoords = true;
 				}
 			}
 			amtOfCoordinates--;
 		}
 		X = holdX;
 		Y = holdY;
 	}
 
 	/**
 	 * Creates the player
 	 * 
 	 * @param name the name of the player
 	 * @param traderUsed points assigned for trader
 	 * @param engineerUsed points assigned for engineer
 	 * @param pilotUsed points assigned for pilot
 	 * @param fighterUsed points assigned for fighter
 	 */
 //	public void createPlayer(String name, int traderUsed, int engineerUsed,
 //			int pilotUsed, int fighterUsed) {
 //		playerOne = new Player(currCurrency, Ship.ShipType.GNAT, name,
 //				pilotUsed, fighterUsed, traderUsed, engineerUsed);
 //		totalSkills.add(traderUsed);
 //		totalSkills.add(engineerUsed);
 //		totalSkills.add(pilotUsed);
 //		totalSkills.add(fighterUsed);
 //	}
 
 	/**
 	 * Gets the entire party's skills.
 	 * 
 	 * @return an ArrayList<Integer> that contains all the skills.
 	 */
 	public static ArrayList<Integer> getSkills() {
 		return totalSkills;
 	}
 	
 	/**
 	 * This method is to boost the player's statistics in case he/she purchases
 	 * a mercenary.
 	 * 
 	 * @param tr number of trader points.
 	 * @param en number of engineer points.
 	 * @param pi number of pilot points.
 	 * @param fi number of fighter points.
 	 */
 	// this needs to reference player correctly
 	public void boostSkillPoints(int tr, int en, int pi, int fi) {
 		int[] boostPoints = { tr, en, pi, fi };
 		ArrayList<Integer> newTotalSkills = new ArrayList<Integer>();
 		for (int i = 0; i < boostPoints.length; i++) {
 			newTotalSkills.add(totalSkills.get(i) + boostPoints[i]);
 		}
 		totalSkills = newTotalSkills;
 	}
 
 	/**
 	 * Generates a list of 150 planet names from an existing text file.
 	 * 
 	 */
 
 	public static void generateNames() throws IOException {
 		Scanner s = new Scanner(new File("PlanetNames.txt"));
 		ArrayList<String> planetNames = new ArrayList<String>();
 		//pulls all possible system names from file and puts them into ArrayList
 		while (s.hasNextLine()) {
 			String temp = s.nextLine();
 			planetNames.add(temp);
 		}
 		ArrayList<String> chosenNames = new ArrayList<String>();
 		int amtOfStrings = 150;
 		//randomly selects 150 system names
 		for (int i = 0; i < amtOfStrings; i++) {
 			boolean check = true;
 			while (check == true) {
 				int random = (int) (Math.random() * planetNames.size());
 				String hold = planetNames.get(random);
 				if (chosenNames.contains(hold) == false) {
 					chosenNames.add(hold);
 					check = false;
 				}
 			} //end of while-loop
 		} //end of for-loop
 		listOfNames = chosenNames;
 		s.close();
 	}
 	
 //	/**
 //	 * Creates static list of all goods
 //	 */
 //	public static void generateGoods(){
 //		ArrayList<Good> goodsList = new ArrayList<Good>();
 //		
 //		Good water = new Good(0, 0, 2, 30, 25, true);
 //		Good fur = new Good(0, 0, 0, 250, 10, true);
 //		Good food = new Good(1, 0, 1, 100, 20, true);
 //		Good ore = new Good(2, 2, 3, 350, 20, true);
 //		Good firearms = new Good(3, 1, 5, 1200, 5, true);
 //		Good medicine = new Good(4, 1, 6, 650, 15, true);
 //		Good machines = new Good(4, 3, 5, 900, 10, true);
 //		Good narcotics = new Good(5, 0, 5, 3500, 5, false);
 //		Good robots = new Good(6, 4, 6, 5000, 5, true);
 //		
 //		goodsList.add(water);
 //		goodsList.add(fur);
 //		goodsList.add(food);
 //		goodsList.add(ore);
 //		goodsList.add(firearms);
 //		goodsList.add(medicine);
 //		goodsList.add(machines);
 //		goodsList.add(narcotics);
 //		goodsList.add(robots);
 //		
 //		listOfGoods = goodsList;
 //			
 //	}
 }
