 package com.example.skidrow;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Game implements Serializable{
 	
 	/**
      * This version number helps make sure everything is sync'd if
      * we start changing object content
      */
     private static final long serialVersionUID = 0L;
     
 	
 	private Player player;
 	private City currentCity;
 	private Market currentMarket;
 	private City[] citiesList;
 	private int step;
 	private Event currEvent;
 	
 
 	//Tag for logcat
     protected static final String TAG = "Game";
     //True if we want to debug false otherwise
     private boolean Debug=true;
 	
 
 	
 	//List of top 50 Most Dangerous cities in the country (USA)
 	private final String[] cityNames = {"Poughkeepsie", "New Bedford","Fall River","Cincinnati","Miami","Alexandria",
 			"Bridgeton","Springfield","Lake Worth","Hartford","Washington","Daytona Beach","Springfield","Battle Creek",
 			"Compton","Buffalo","Cleveland","Atlanta","Pine Bluff","Trenton","Stockton","Homestead","Desert Hot Springs",
 			"Texarkana","Rockford","Baltimore","Little Rock","New Haven","Salisbury","Harvey","Harrisburg","Memphis",
 			"Oakland","Spartanburg","Inkster","Myrtle Beach","Chelsea","Riviera Beach","Newburgh","Pontiac","St. Louis",
 			"Atlantic City","Wilmington","Flint","Detroit","Camden","Saginaw","West Memphis","Chester","East St. Louis",
 	};
 	
 	/**
 	 * Constructor for the game class
 	 * 
 	 */
 	
 	public Game(){
 		step=0;
 	}
 	
 	
 	/**
 	 * Method used to create a new instance of player in the game.
 	 * 
 	 * @param name Name of the Player
 	 * @param communicationSkills Communications skill points
 	 * @param fighterSkills Fighter skill points
 	 * @param driverSkills Driver skill points
 	 * @param dealerSkills Dealer skill Points
 	 * @param difficultyLevel the level of difficulty
 	 */
 	public void createGame(String name, int communicationSkills, int fighterSkills, int driverSkills, int dealerSkills, String difficultyLevel){
 		player = new Player(name, communicationSkills, fighterSkills, driverSkills, dealerSkills, difficultyLevel);
 		generateCountry();
 		currentCity=citiesList[17];//Atlanta is set as the default starting location
 		generateMarket();
 	}
 	/**
 	 * Method used to generate all cities in the country. 
 	 */
 	private void generateCountry(){
 		//Checks the total number of city NAMES
 		int numCities = cityNames.length;
 		//initialize the total NUMBER of countries that will be in the game
 		citiesList = new City[numCities];
 		int checkNumCities = 0;
 		//Instantiate the Cities in the country
 		for (int i = 0; i < numCities; i++){
 			citiesList[i] = new City(cityNames[i]);
 			//maintains track of how many cities are actually created
 			checkNumCities++;
 		}
 		//Prints confirmation of how many cities are created
 		if (checkNumCities == numCities){
 			System.out.println("Country with " + checkNumCities + " cities was created");
 		} else{
 			System.out.println("Not all cities were instanciated");
 		}
 	}
 	
 	/**
 	 * Method to get the list of all the cities
 	 * 
 	 * @return the list of cities
 	 */
 	public String[] getCityList(){
 		return cityNames;
 	}
 	
 	/**
 	 * Returns the city where the player is currently located
 	 * @return City where the player is currently at
 	 */
 	public City getCurrentCity(){
 		return currentCity;
 	}
 	/**
 	 * Sets the current city
 	 * @param newCity new current city
 	 */
 	public void setCurrentCity(City currentCity){
 		this.currentCity=currentCity;
 	}
 	
 	/**
 	 * Method to get a city based on an index of CityList
 	 * @param the index of the city list
 	 * @return the city that is stored on the city list
 	 */
 	public City getCity(int index){
 		return citiesList[index];
 	}
 	
 	/**
 	 * Gets the information about a given city. The city is determined by its index in the citiesList.
 	 * It returns all the information needed by the UI in a String array
 	 * 
 	 * @param index The index of the city
 	 * @return String of information needed by the UI with the following format: [0]=name, [1]=techLevel, [2]=politicalSystem, [3]=distance
 	 */
 	public String[] getCityInfo(int index){
 		String[] info = new String[4];
 		City city = citiesList[index];
 		
 		info[0]=city.getName();
 		info[1]=city.getTechLevel();
 		info[2]=city.getResources();
 		
 		return info;
 	}
 	
 	/**
 	 * Returns the information about a city which is passed in as a parameter.
 	 * 
 	 * @param city City who's information is being returned
 	 * @return String of information needed by the UI with the following format: [0]=name, [1]=techLevel, [2]=politicalSystem, [3]=distance
 	 */
 	public String[] getCityInfo(City city){
 		String[] info = new String[4];
 		
 		info[0]=city.getName();
 		info[1]=city.getTechLevel();
 		info[2]=city.getResources();
 		
 		return info;
 	}
 	
 	/**
 	 * Grabs the necessary information for the UI from the player and packages it in a String array.
 	 * The information is as follows(from index 0 to 7):communicationSkills, fighterSkills, driverSkills, dealerSkills, currentCity, playerHealth, playerName, playerMoney
 	 * 
 	 * @return Returns an array of player information for the UI
 	 */
 	public String[] getPlayerStatInfo(){
 		String[] info = new String[9];
 		int[] playerLevels = player.getSkillsArray();
 		
 		for(int x=0; x<4; x+=1){
 			info[x] = Integer.toString(playerLevels[x]);
 		}
 		info[4] = currentCity.getName();
 		//info[5] = health
 		info[6] = player.getName();
 		info[7] = "$"+Integer.toString(player.getMoney());
 		info[8] = Double.toString(getGas());
 		
 		
 		return info;
 	}
 	
 	/**
 	 * Return the amount of money the market currrently has
 	 * @return int value of market's money
 	 */
 	public int getMarketMoney(){
 		return currentMarket.getMoney();
 	}
 	
 	/**
 	 * Method that is called every time the user makes a move;
 	 * As of now, the only functionality of this method is to increment
 	 * the step counter. 
 	 */
 	public void makeMove(City newCity, Event e){
 		if(Debug) Log.i(TAG, "Player make a move. Current step: " + getStep());
 		player.setGas(player.getGas()-getGasExpenditure(newCity));
 		if(Debug) Log.i(TAG, "Gas expenditure: "+getGasExpenditure(newCity));
 		setCurrentCity(newCity);
 		currEvent = e;
 		increaseStep();
 		
 	}
 	
 	/**
 	 * Returns the current step number
 	 * @return step number
 	 */
 	public int getStep(){
 		return step;
 	}
 	
 	/**
 	 * 
 	 */
 	public void increaseStep(){
 		step++;
 	}
 
 	/**
 	 * This method creates an instance of market with the given tech level of current city
 	 */
 	public void generateMarket(){
 		currentMarket = new Market(currentCity.getTechLevelInt());
 	}
 
 	/**
 	 * Gets all of the goods in the current city's market
 	 * 
 	 * @return List of all goods and their quantity for the current city
 	 */
 	public String[] getMarketGoods(){
 		if(currentMarket!=null){
 			Map<Good, Integer> goods = currentMarket.getGoods();
 			List<String> goodsList = new ArrayList<String>();
 			Iterator iterator = goods.entrySet().iterator();
 			
 			for(int x=0; x<goods.size(); x+=1){
 				Map.Entry<Good, Integer> entry = (Entry<Good, Integer>) iterator.next();
 				if(entry.getValue() > 0){ //if(entry.getValue()> 0)
					goodsList.add(entry.getKey().toString()+"("+Integer.toString(entry.getValue())+")"+"\n     $"+(currentMarket.getPrice(entry.getKey())+currEvent.getPriceEffect()));
 				}
 			}
 			
 			return goodsList.toArray(new String[goodsList.size()]);
 		}
 		return new String[]{""};
 	}
 	
 	/**
 	 * Gets all of the goods for the player
 	 * 
 	 * @return List of all goods and their quantity for the player
 	 */
 	public String[] getPlayerGoods(){
 		if(player!=null){
 			Map<String, Integer> goods = player.getGoods();
 			List<String> goodsList = new ArrayList<String>();
 			Iterator iterator = goods.entrySet().iterator();
 			for(int x=0; x<goods.size(); x+=1){
 				Map.Entry<String, Integer> entry = (Entry<String, Integer>) iterator.next();
 				if(entry.getValue() > 0 && currentMarket.getGoodFromString(entry.getKey()).minSellTechLevel <= currentCity.getTechLevelInt()){
					goodsList.add(entry.getKey()+"("+Integer.toString(entry.getValue())+")"+"\n     $"+(currentMarket.getPrice(currentMarket.getGoodFromString(entry.getKey()))+currEvent.getPriceEffect()));
 				}
 			}
 			
 			return goodsList.toArray(new String[goodsList.size()]);
 		}
 		return new String[]{""};
 	}
 
 	/**
 	 * This method transfers goods from the market to the player. This means the player gains goods and loses money and the Market loses goods and gains money.
 	 * If the player attempts to buy more goods than are available or exceeds his/her money then a string of the error is returned. If the transaction is valid then
 	 * null is returned and all appropriate information is updated
 	 * 
 	 * @param drug String of the drug to be transfered
 	 * @param quantity Quantity of the drug to be transfered
 	 * @return
 	 */
 	public String marketToPlayer(String drug, int quantity){
		int transPrice = (currentMarket.getPrice(currentMarket.getGoodFromString(drug))+currEvent.getPriceEffect())*quantity;
 		if(player.getMoney()<transPrice){
 			return "You are $" + (transPrice-player.getMoney()) + " short of buying " + quantity + " " + drug;
 		}
 		
 		if (quantity > player.getCargoSpace()){
     		System.out.println(" Integer.parseInt(quantity) "+ quantity+ " AppUtil.game.getCargoSpaceFromGame()  " + player.getCargoSpace());
     		return "You do not have enough cargo space!!";
     	}
 		
 		player.buyGood(drug, quantity, transPrice);
 		currentMarket.sellGood(currentMarket.getGoodFromString(drug), quantity, transPrice);
 		
 		return null;
 	}
 	
 	/**
 	 * This method transfers goods from the player to the market. This means the market gains goods and loses money and the player loses goods and gains money.
 	 * If the player attempts to sell more goods than are available or exceeds the markets money then a string of the error is returned. If the transaction is valid then
 	 * null is returned and all appropriate information is updated
 	 * 
 	 * @param drug String of the drug to be transfered
 	 * @param quantity Quantity of the drug to be transfered
 	 * @return
 	 */
 	public String playerToMarket(String drug, int quantity){
		int transPrice = (currentMarket.getPrice(currentMarket.getGoodFromString(drug))+currEvent.getPriceEffect())*quantity;
 		if(currentMarket.getMoney()<transPrice){
 			return "The market does not have enough money to buy your goods";
 		}
 		
 		currentMarket.buyGood(currentMarket.getGoodFromString(drug), quantity, transPrice);
 		player.sellMoney(drug, quantity, transPrice);
 		return null;
 	}
 	
 	/**
 	 * This method extracts cargo space availability from player class
 	 * @return space cargo availability
 	 */
 	public int getCargoSpaceFromGame(){
 		int cargoSpace = player.getCargoSpace();
 		return cargoSpace;
 	}
 	
 	
 	/**
      * Gets the distance between the current city and the city being investigated
      * @return String containing the distance between the current city and the selected city
      * @author apavia3
      */
     public String getDistance(City city){
     	int[] displayedCityLocation=city.getLocation();
 		int[] currentCityLocation=getCurrentCity().getLocation();
 		double hypotenuse= Math.sqrt(Math.pow(displayedCityLocation[0]-currentCityLocation[0], 2)+Math.pow(displayedCityLocation[1]-currentCityLocation[1], 2));
     	return String.format("%.3f", hypotenuse);
     }
 
 	/**
 	 * Checks if the the spaceship has enough fuel to go to the next city
 	 * @param nextCity
 	 * @return
 	 */
 	public boolean checkGas(City nextCity){
 	    if(getGasExpenditure(nextCity)<player.getGas()){
 	    	return true;
 	    }
 		return false;
 	}
 	/**
 	 * Returns the amount of gas needed to travel to the next city 
 	 * @param nextCity City that we are trying to go
 	 * @return the amount of gas needed to travel to the next city 
 	 */
 	public double getGasExpenditure(City nextCity){
 		return player.getFuelEfficiency()* Double.parseDouble(getDistance(nextCity));
 	}
 	/**
 	 * Returns the amount of gas left
 	 * @return a double representing the amount of gas left
 	 */
 	public double getGas(){
 		return player.getGas();
 	}
 	
 }
