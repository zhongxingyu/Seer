 package edu.brown.cs32.MFTG.monopoly;
 
 import java.util.HashMap;
 import java.util.Objects;
 
 import com.fasterxml.jackson.annotation.*;
 
 /**
  * This class models a player
  * @author jschvime
  *
  */
 public class Player {
 	public enum Expense {
 		CHEAP, EXPENSIVE
 	}
 	public enum Amount {
 		FEWER, MORE
 	}
 	public enum Balance {
 		EVEN, UNEVEN
 	}
 	public enum Aggression {
 		PASSIVE, AGGRESSIVE
 	}
 	public final int ID;
 	private HashMap<String, Integer> _propertyValues;
 	private HashMap<String, Double[]> _colorValues;
 	private double _liquidity, _timeChange, _tradingFear;
 	private int _minBuyCash, _minBuildCash, _minUnmortgageCash;
 	private int _jailWait;
 	private Expense _mortgageChoice;
 	private Amount _houseSelling;
 	private Expense _sellingChoice;
 	private Balance _buildingEvenness;
 	private Expense _buildingChoice;
 	private Aggression _buildAggression;
 	private int _jailPoor, _jailRich;
 	
 	@JsonCreator
 	public Player(@JsonProperty("id") int id){
 		ID=id;
 		_propertyValues= new HashMap<>();
 		initializePropertyValues();
 		
 		//a hashmap of values for each color. for each color there is an array such that;
 		//T[0]=value of monopoly
 		//T[1]=value of houses for the color
 		//T[2]=value of breaking an opponent's monopoly
 		//T[3]=how much value is if you have 1 other property of the same color, but this would not give you a monopoly
 		_colorValues = new HashMap<>();
 		initializeColorValues();
 		
 		//a number from 1-10 specifying how much they're willing to spend as they have more money
 		//1 means you don't care if you have more money, 10 means you're much more likely to buy with more money
 		_liquidity=1;
 		
 		//a number from 1-10 specifying how much more/less they value a property as time goes on
 		//1 means you prefer buying earlier 10 means you prefer buying at the end, 5 means it doesn't matter
 		_timeChange=5;
 		
 		_minBuyCash=0;
 		_minUnmortgageCash=0;
 		_jailWait=3;
 		
 		_mortgageChoice=Expense.EXPENSIVE;
 		
 		_buildingEvenness=Balance.UNEVEN;
 		_buildingChoice=Expense.CHEAP;
 		_houseSelling=Amount.FEWER;
 		
 		//a number from 1-3 specifying the number of turns they'd like to stay in if they're doing well/poorly
 		_jailPoor=3;
 		_jailRich=3;
 		
 		_minBuildCash=0;
 		
 		_buildAggression=Aggression.AGGRESSIVE;
 		
 		//this is a number that multiplies your valuation of a property you own if your opponent wants to trade for it (since likely
 		//it will help them a lot if they want it)
 		_tradingFear=1.25;
 	}
 	
 	/**
 	 * does nothing for human player, called however everytime to see if it wants to set its own heuristics
 	 */
 	public void setHeuristics() {
 		
 	}
 	
 	/**
 	 * @return _propertyValues
 	 */
 	public HashMap<String, Integer> getPropertyValues(){
 		return _propertyValues;
 	}
 	
 	/**
 	 * sets the _propertyValues attribute
 	 * @param propertyValues
 	 */
 	public void setPropertyValues(HashMap<String, Integer> propertyValues){
 		_propertyValues = propertyValues;
 	}
 	
 	/**
 	 * sets property value
 	 * @param property
 	 * @param value
 	 */
 	@JsonIgnore
 	public void setPropertyValue(String property, int value){
 		_propertyValues.put(property, value);
 	}
 	
 	/**
 	 * 
 	 * @param property
 	 * @return property value
 	 */
 	@JsonIgnore
 	public int getPropertyValue(String property){
 		return _propertyValues.get(property);
 	}
 	
 	/**
 	 * sets color value
 	 * @param color
 	 * @param value
 	 */
 	@JsonIgnore
 	public void setColorValue(String color, double monopolyValue, double housesValue, double breakingOpponentValue, double sameColorEffect ){
 		Double[] monopoly = new Double[4];
 		monopoly[0]=monopolyValue;
 		monopoly[1]=housesValue;
 		monopoly[2]=breakingOpponentValue;
 		monopoly[3]=sameColorEffect;
 		_colorValues.put(color, monopoly);
 	}
 	
 	/**
 	 * 
 	 * @return _colorValues
 	 */
 	public HashMap<String, Double[]> getColorValues(){
 		return _colorValues;
 	}
 	
 	/**
 	 * Sets _colorValues
 	 * @param colorValues
 	 */
 	public void setColorValues(HashMap<String, Double[]> colorValues){
 		_colorValues = colorValues;
 	}
 	
 	/**
 	 * 
 	 * @param color
 	 * @return value of monopoly
 	 */
 	@JsonIgnore
 	public double getMonopolyValue(String color){
 		return _colorValues.get(color)[0];
 	}
 	
 	/**
 	 * 
 	 * @param color
 	 * @return value of houses on color
 	 */
 	@JsonIgnore
 	public double getHouseValueOfColor(String color){
 		return _colorValues.get(color)[1];
 	}
 	
 	/**
 	 * 
 	 * @param color
 	 * @return value of breaking opponents value
 	 */
 	@JsonIgnore
 	public double getBreakingOpponentMonopolyValue(String color){
 		return _colorValues.get(color)[2];
 	}
 	
 	/**
 	 * 
 	 * @param color
 	 * @return how much value is affected by how many other properties of the same color you have
 	 */
 	@JsonIgnore
 	public double getSameColorEffect(String color){
 		return _colorValues.get(color)[3];
 	}
 	
 	/**
 	 * @return the _liquidity
 	 */
 	public double getLiquidity() {
 		return _liquidity;
 	}
 
 	/**
 	 * @param _liquidity the _liquidity to set
 	 */
 	public void setLiquidity(double liquidity) {
 		_liquidity = liquidity;
 	}
 	
 	/**
 	 * @return the _tradingFear
 	 */
 	public double getTradingFear() {
 		return _tradingFear;
 	}
 
 	/**
 	 * @param _tradingFear the _tradingFear to set
 	 */
 	public void setTradingFear(double tradingFear) {
 		_tradingFear = tradingFear;
 	}
 
 	/**
 	 * @return the _timeChange
 	 */
 	public double getTimeChange() {
 		return _timeChange;
 	}
 
 	/**
 	 * @param _timeChange the _timeChange to set
 	 */
 	public void setTimeChange(double timeChange) {
 		_timeChange = timeChange;
 	}
 
 	/**
 	 * @return the _minBuyCash
 	 */
 	public int getMinBuyCash() {
 		return _minBuyCash;
 	}
 
 	/**
 	 * @param _minBuyCash the _minBuyCash to set
 	 */
 	public void setMinBuyCash(int minBuyCash) {
 		_minBuyCash = minBuyCash;
 	}
 
 	/**
 	 * @return the _jailWait
 	 */
 	public int getJailWait() {
 		return _jailWait;
 	}
 
 	/**
 	 * @param _jailWait the _jailWait to set
 	 */
 	public void setJailWait(int jailWait) {
 		_jailWait = jailWait;
 	}
 
 	/**
 	 * @return the _mortgageChoice
 	 */
 	public Expense getMortgageChoice() {
 		return _mortgageChoice;
 	}
 
 	/**
 	 * @param _mortgageChoice the _mortgageChoice to set
 	 */
 	public void setMortgageChoice(Expense mortgageChoice) {
 		_mortgageChoice = mortgageChoice;
 	}
 
 	/**
 	 * @return the _houseSelling
 	 */
 	public Amount getHouseSelling() {
 		return _houseSelling;
 	}
 
 	/**
 	 * @param _houseSelling the _houseSelling to set
 	 */
 	public void setHouseSelling(Amount houseSelling) {
 		_houseSelling = houseSelling;
 	}
 
 	/**
 	 * @return the _buildingEvenness
 	 */
 	public Balance getBuildingEvenness() {
 		return _buildingEvenness;
 	}
 
 	/**
 	 * @param _buildingEvenness the _buildingEvenness to set
 	 */
 	public void setBuildingEvenness(Balance buildingEvenness) {
 		_buildingEvenness = buildingEvenness;
 	}
 
 	/**
 	 * @return the _buildingChoice
 	 */
 	public Expense getBuildingChoice() {
 		return _buildingChoice;
 	}
 
 	/**
 	 * @param _buildingChoice the _buildingChoice to set
 	 */
 	public void setBuildingChoice(Expense buildingChoice) {
 		_buildingChoice = buildingChoice;
 	}
 	
 	/**
 	 * @return the buy aggression
 	 */
 	public Aggression getBuildAggression() {
 		return _buildAggression;
 	}
 
 	/**
 	 * @param sets the buy aggression
 	 */
 	public void setBuildAggression(Aggression buildAggression) {
 		_buildAggression=buildAggression;
 	}
 	
 	/**
 	 * @return the sellingChoice
 	 */
 	public Expense getSellingChoice() {
 		return _sellingChoice;
 	}
 
 	/**
 	 * @param _sellingChoice the _sellingChoice to set
 	 */
 	public void setSellingChoice(Expense sellingChoice) {
 		_sellingChoice = sellingChoice;
 	}
 
 	/**
 	 * @return the _minBuildCash
 	 */
 	public int getMinBuildCash() {
 		return _minBuildCash;
 	}
 
 	/**
 	 * @param _minBuildCash the _minBuildCash to set
 	 */
 	public void setMinBuildCash(int minBuildCash) {
 		_minBuildCash = minBuildCash;
 	}
 	
 	/**
 	 * @return the _MinUnMortgageCash
 	 */
 	public int getMinUnmortgageCash() {
 		return _minUnmortgageCash;
 	}
 
 	/**
 	 * @param _MinUnMortgageCash the _MinUnMortgageCash to set
 	 */
 	public void setMinUnmortgageCash(int minUnmortgageCash) {
 		_minUnmortgageCash = minUnmortgageCash;
 	}
 
 	/**
 	 * @return the _jailPoor
 	 */
 	public int getJailPoor() {
 		return _jailPoor;
 	}
 
 	/**
 	 * @param _jailPoor the _jailPoor to set
 	 */
 	public void setJailPoor(int jailPoor) {
 		_jailPoor = jailPoor;
 	}
 
 	/**
 	 * @return the _jailRich
 	 */
 	public int getJailRich() {
 		return _jailRich;
 	}
 
 	/**
 	 * @param _jailRich the _jailRich to set
 	 */
 	public void setJailRich(int jailRich) {
 		_jailRich = jailRich;
 	}
 	
 	//T[0]=value of monopoly
 	//T[1]=value of houses for the color
 	//T[2]=value of breaking an opponent's monopoly
 	//T[3]=how much value is affected by how many other properties of the same color you have
 	/**
 	 * Sets color value defaults
 	 */
 	private void initializeColorValues() {
 		Double[] purple={200.0,50.0,150.0,100.0};
 		_colorValues.put("purple", purple);
 		
 		Double[] lightblue={250.0,50.0,200.0,200.0};
 		_colorValues.put("light blue", lightblue);
 		
 		Double[] pink={275.0,100.0,225.0,250.0};
 		_colorValues.put("pink", pink);
 		
 		Double[] orange={300.0,100.0,250.0,275.0};
 		_colorValues.put("orange", orange);
 		
 		Double[] red={325.0,150.0,275.0,300.0};
 		_colorValues.put("red", red);
 		
 		Double[] yellow={350.0,150.0,300.0,325.0};
 		_colorValues.put("yellow", yellow);
 		
 		Double[] green={375.0,200.0,325.0,350.0};
 		_colorValues.put("green", green);
 		
 		Double[] darkblue={500.0,200.0,400.0,450.0};
 		_colorValues.put("dark blue", darkblue);
 		
 	}
 	
 	/**
 	 * Sets property value defaults
 	 */
 	private void initializePropertyValues() {
 		_propertyValues.put("mediterranean avenue", 60);
 		_propertyValues.put("baltic avenue", 60);
 		_propertyValues.put("reading railroad", 200);
 		_propertyValues.put("oriental avenue", 100);
 		_propertyValues.put("vermont avenue", 100);
 		_propertyValues.put("connecticut avenue", 120);
 		_propertyValues.put("st. charles place", 140);
 		_propertyValues.put("electric company", 150);
 		_propertyValues.put("states avenue", 140);
 		_propertyValues.put("virginia avenue", 160);
 		_propertyValues.put("pennsylvania railroad", 200);
 		_propertyValues.put("st. james place", 180);
 		_propertyValues.put("tennessee avenue", 180);	
 		_propertyValues.put("new york avenue", 200);	
 		_propertyValues.put("kentucky avenue", 220);	
 		_propertyValues.put("indiana avenue", 220);	
 		_propertyValues.put("illinois avenue", 240);		
 		_propertyValues.put("b and o railroad",200);		
 		_propertyValues.put("atlantic avenue", 260);		
 		_propertyValues.put("ventnor avenue", 260);		
 		_propertyValues.put("water works",150);		
 		_propertyValues.put("marvin gardens", 280);	
 		_propertyValues.put("pacific avenue", 300);
 		_propertyValues.put("north carolina avenue", 300);
 		_propertyValues.put("pennsylvania avenue", 320);
 		_propertyValues.put("short line", 200);
 		_propertyValues.put("park place", 350);
 		_propertyValues.put("boardwalk", 400);
 		
 	}
 	
 	@Override
 	public String toString(){
 		return String.format("%d-Player", ID);
 	}
 	
 	private boolean colorValuesEqual(Player that){
 		HashMap<String, Double[]> thatMap = that.getColorValues();
 		
 		for (String s : _colorValues.keySet()){
 			if (!thatMap.containsKey(s))
 				return false;
 			
 			Double[] thisArray = _colorValues.get(s);
 			Double[] thatArray = thatMap.get(s);
 			
 			if(thisArray.length != thatArray.length)
 				return false;
 			
 			for (int i = 0; i < thisArray.length; i++){
 				if(!thisArray[i].equals(thatArray[i]))
 					return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean equals(Object o){
 		if (o == null || !(o instanceof Player))
 			return false;
 		
 		Player that = (Player) o;
 		
 		return Objects.equals(_propertyValues, that.getPropertyValues())
 			&& colorValuesEqual(that)
 			&& _liquidity == that.getLiquidity()
 			&& _timeChange == that.getTimeChange()
 			&& _minBuyCash == that.getMinBuyCash()
 			&& _minBuildCash == that.getMinBuildCash()
 			&& _minUnmortgageCash == that.getMinUnmortgageCash()
 			&& _jailWait == that.getJailWait()
 			&& _mortgageChoice == that.getMortgageChoice()
 			&& _houseSelling == that.getHouseSelling()
 			&& _sellingChoice == that.getSellingChoice()
 			&& _buildingEvenness == that.getBuildingEvenness()
 			&& _buildingChoice == that.getBuildingChoice()
			&& _buildAggression == that.getBuyAggression()
 			&& _jailPoor == that.getJailPoor()
 			&& _jailRich == that.getJailRich();
 	}
 }
