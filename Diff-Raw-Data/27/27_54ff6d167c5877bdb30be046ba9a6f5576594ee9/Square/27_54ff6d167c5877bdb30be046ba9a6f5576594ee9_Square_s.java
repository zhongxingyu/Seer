 import be.kuleuven.cs.som.annotate.*;
 
 /**
  * 
  * Class to represent the smallest element of a location
  * 
 * @author benlefevere, Karel Domin
  * @version 1.0
  * 
  * @invar 	getTemperatureInCelcius() must be a valid temperature
  * 			| isValidTemperature(getTemperatureInCelcius())
  * 
  * @invar 	getHumidity() must be a valid humidity
  * 			| isValidHumidity(getHumidity())
  *
  */
 public class Square {
 	
 	
 	/**
 	 * this constructor will construct a new Square object with the default values assigned to the 
 	 * attributes. 
 	 */
	@Raw
 	public Square(){
 		;
 	}
 	
 	/**
 	 * this constructor will construct a new Square object with values assigned to temperature
 	 * and humidity.
 	 * 
 	 * @param 	temperature
 	 * 			the temperature of this new Square in degrees Celcius
 	 * @param 	humidity
 	 * 			the humidity of this new Square 
 	 * @Post	the temperature of this new Square is equal to temperature
 	 * 			| new.getTemperatureInCelcius() == temperature
 	 * @Post	the humidity of this new Square is equal to humidity
 	 * 			| new.getHumidity() == humidity
 	 */
 	@Raw
	public Square(double temperature, double humidity){
 		this.temperature = temperature;
 		this.humidity = humidity;
 	}
 	
 	/**
 	 * gives the temperature of the Square in degrees Celcius
 	 *
 	 * @return	The temperature of the square in degrees Celcius
 	 * 			| result == temperature
 	 */
 	@Basic
 	public double getTemperatureInCelcius(){
 		return temperature;
 	}
 	
 	/**
 	 * gives the temperature of the Square in Kelvin
 	 *
 	 * @return	The temperature of the square in Kelvin
 	 * 			| result == celciusToKelvin(temperature)
 	 */
 	@Basic
 	public double getTemperatureInKelvin(){
 		return celciusToKelvin(temperature);
 	}
 	
 	/**
 	 * gives the temperature of the Square in degrees Farenheit
 	 *
 	 * @return	The temperature of the square in degrees Farenheit
 	 * 			| result == celciusToFarenheit(temperature)
 	 */
 	@Basic
 	public double getTemperatureInFarenheit(){
 		return celciusToFarenheit(temperature);
 	}
 	
 	/**
 	 * converts the temperature from degrees Celcius to Farenheit
 	 * 
 	 * @param 	degreesCelcius
 	 * 			the temperature to be converted in degrees Celcius
 	 * @return	returns the temperature converted to degrees Farenheit
 	 * 			| result == ((degreesCelcius*9)/5) + 32
 	 */
 	public double celciusToFarenheit (double degreesCelcius){
 		return ((degreesCelcius*9)/5) + 32;
 	}
 	
 	/**
 	 * converts the temperature from degrees Celcius to Kelvin
 	 * 
 	 * @param	degreesCelcius
 	 * 			the temperature to be converted in degrees Celcius
 	 * @return 	returns the temperature converted to Kelvin
 	 * 			| result == degreesCelcius + 273
 	 */
 	public double celciusToKelvin (double degreesCelcius){
 		return degreesCelcius + 273;
 	}
 	
 	/**
 	 * sets the temperature of this Square to the given temperature
 	 * 
 	 * @param 	degreesCelcius	
 	 * 			the temperature in degrees Celcius
 	 * @throws IllegalArgumentException
 	 * 			throws an IllegalArgumentException if degreesCelcius is not valid
 	 * 			|if (! isValidTemperature(degreesCelcius)) 
 	 * 			|	then throw IllegalArgumentException
 	 * 
 	 * @post	The temperature of the square is equal to degreesCelcius
 	 * 			| new.getTemperatureInCelcius()== degreesCelcius
 	 */
 	public void setTemperature(double degreesCelcius) throws IllegalArgumentException{
 		if (isValidTemperature(degreesCelcius)){
 			temperature = degreesCelcius;
 		}
 		else
 			throw new IllegalArgumentException();
 	}
 	
 	/**
 	 * check whether the given temperature is valid
 	 * 
 	 * @param 	temperature
 	 * 			the temperature in degrees Celcius
 	 * @return	temperature must lie between MIN_TEMP and MAX_TEMP or be equal to MIN_TEMP or MAX_TEMP
 	 * 			| result == (temperature <= MAX_TEMP && temperature >= MIN_TEMP) 
 	 */
 	public static boolean isValidTemperature(double temperature){
 		return (MIN_TEMP <= temperature && temperature <= MAX_TEMP);
 	}
 	
 	private double temperature;
 	
 	public final static double MIN_TEMP = -200.00;
 	public final static double MAX_TEMP = 5000.00;
 	
 	
 	/**
 	 * returns the cold damage for this Square
 	 * 
 	 * @return	If the temperature is higher than -15 degrees Celcius, return 0
 	 * 			| if (getTemperatureInCelcius() > -15)
 	 * 			|	result == 0
 	 * @return	If temperature is equal or below -15 degrees Celcius, return the number of points lost due to cold damage
 	 * 			| If (getTemperatureInCelcius() <= -15)
 	 * 			|	then result == (int) Math.abs((5.0 + getTemperatureInCelcius())/10) 
 	 */
 	public int getColdDamage(){
 		if (getTemperatureInCelcius() > -15)
 			return 0;
 		
 		return (int) Math.abs((5.0 + getTemperatureInCelcius())/10) ;
 	}
 	
 	/**
 	 * returns the heat damage for this Square
 	 * 
 	 * @return	If the temperature is lower than 50 degrees Celcius, return 0
 	 * 			| if (getTemperatureInCelcius() < 50)
 	 * 			|	result == 0
 	 * @return	If temperature is equal or higher than 50 degrees Celcius, return the number of points lost due to heat damage
 	 * 			| If (getTemperatureInCelcius() >= 50)
 	 * 			|	then result == (int) ((getTemperatureInCelcius() - 35)/15)
 	 */
 	public int getHeatDamage(){
 		if (getTemperatureInCelcius() < 50)
 			return 0;
 		return (int) ((getTemperatureInCelcius() - 35)/15);
 	}
 	
 	/**
 	 * returns the rust damage for this Square
 	 * 
 	 * @return	If getHumidity() is higher than or equal to 37 % , return the number of points lost due to rust damage
 	 * 			| if ( getHumidity() >= 37.00)
 	 * 			|	then result == (int) ((getHumidity() - 30)/7)
 	 * 
 	 */
 	public int getRustDamage(){
 		if ( getHumidity() >= 37.00)
 			 return (int) ((getHumidity() - 30)/7);
 		return 0;
 	}
 	
 	/**
 	 * returns the humidity of this Square
 	 * 
 	 * @return	the humidity percentage of the square
 	 * 			| result == humidity
 	 * 
 	 */
 	@Basic
 	public double getHumidity(){
 		return humidity;
 	}
 	
 	/**
 	 * sets the humidity of this Square to a given humidity 
 	 * 
 	 * @Pre		percent must be a valid humidity
 	 * 			| isValidHumidity(percent)
 	 * @param 	humidity
 	 * 			the new humidity for this Square
 	 * @post	percent is the new humidity
 	 * 			|	new.getHumidity() == percent
 	 */
 	public void setHumidity(double humidity){
 		assert isValidHumidity(humidity);	
 		this.humidity = humidity;
 	}
 	
 	/**
 	 * check whether the given humidity is valid
 	 * 
 	 * @param 	humidity
 	 * 
 	 * @pre		the humidity can't have more than two decimal places
 	 * 			| (humidity*100)%1 == 0
 	 * 			the given humidity for this Square
 	 * @return	humidity must lie between 0 and 100 or be equal to 0 or 100
 	 * 			| result == (getHumidity()<= 100 && getHumidity() >= 0) 
 	 */
 	public static boolean isValidHumidity(double humidity){
 		return (humidity<= 100 && humidity >= 0);
 	}
 	
 	private double humidity;
 	
 	/**
 	 * return whether this Square is slippery or not
 	 * 
 	 * @return The square is slippery if it has a slippery surface OR 
 	 * 				the humidity is 100 and the temperature is higher than zero OR
 	 * 				the humidity is higher than 10 and the temperature is zero or below
 	 * 			| return == (isSlipperySurface || 
 	 * 							(getHumidity() == 100 && getTemperatureInCelcius() > 0) ||
 	 * 							(getHumidity() > 10 && getTemperatureInCelcius() <= 0)) 
 	 */
 	public boolean isSlippery(){
 		return (isSlipperySurface || 
 				(getHumidity() == 100 && getTemperatureInCelcius() > 0) ||
 				 (getHumidity() > 10 && getTemperatureInCelcius() <= 0));
 	}
 	
 	public boolean isSlipperySurface;
 	
 	/**
 	 * returns the inhabitability of this Square
 	 * 
 	 * @return		returns the Inhabitability from a square
 	 * 				| result == (-1)* (Math.sqrt(Math.pow(getHeatDamage(),3)) / Math.sqrt(101-getHumidity()))
 	 * 				|			- Math.sqrt(getColdDamage()) 
 	 */
 	public double getInhabitability(){
 		double tempInhabilityHeat = getHeatDamage();
 		double tempInhabilityCold = getColdDamage();
 		double tempInhabilityHumidity = getHumidity();
 		
 		tempInhabilityHeat = Math.pow(tempInhabilityHeat, 3);
 		tempInhabilityHeat= Math.sqrt(tempInhabilityHeat);
 		
 		tempInhabilityCold = Math.sqrt(tempInhabilityCold);
 		
 		tempInhabilityHumidity = 101 - tempInhabilityHumidity;
 		tempInhabilityHumidity = Math.sqrt(tempInhabilityHumidity);
 		
 		return (-1)*(tempInhabilityHeat/tempInhabilityHumidity) - tempInhabilityCold;
 		
 	}
 	
 	private final boolean[] borders = new boolean[6];
 	
 	/**
 	 * sets a new border in a given direction of this Square
 	 * 
 	 * @param 	direction
 	 * 			the direction to set the border of this Square to
 	 * @param 	newBorder
 	 * 			the new state of the border
 	 * @post	if the direction is a valid direction then the border in direction 'direction' is set to newBorder
 	 * 			|if(isValidDirection(direction)) 
 	 * 			|	then	new.borders[direction - 1] = newBorder
 	 * @post	if the direction is not a valid direction then the border in direction '1' is set to newBorder
 	 * 			|if(! isValidDirection(direction)) 
 	 * 			|	then	new.borders[0] = newBorder
 	 */
 	public void setBorderIndirectionTo(int direction, boolean newBorder){
 		if (isValidDirection(direction)) 
 			borders[direction - 1] = newBorder;
 		else if (! isValidDirection(direction)) 
 			borders[0] = newBorder;
 	}
 	
 	/**
 	 * returns the state of the border in direction 'direction'
 	 * 
 	 * @param 	direction
 	 * 			the direction 
 	 * @return 	if the direction is a valid direction then return the state of the border in direction 'direction'
 	 * 			| if (isValidDirection(direction)) then
 	 * 			|	return == borders[direction - 1]
 	 * @return 	if the direction is not a valid direction then return the state of direction '1'
 	 * 			| if (! isValidDirection(direction)) then
 	 * 			|	return == borders[0]
 	 * 				
 	 */
 	//TOTAAL
 	@Basic
 	public boolean getBorderInDirection(int direction){
 		if (isValidDirection(direction))
 			return  borders[direction - 1];
 		else 
 			return  borders[0] ;
 	}
 	
 	/**
 	 * check whether a given direction is valid
 	 * 
 	 * @param	direction
 	 * 			the given direction
 	 * @return	if the direction lies between 7 and 0 then return true
 	 * 			|if(direction < 7 && direction > 0) then
 	 * 			|	return == true
 	 * @return	if the direction is larger then 7 return false
 	 * 			|if(direction > 7) then
 	 * 			|	return == false
 	 * @return	if the direction equals 0 then return false
 	 * 			|if(direction <=0) then
 	 * 			|	return == false
 	 */
 	public static boolean isValidDirection(int direction){
 		if(direction < 7 && direction > 0)
 			return true;
 		else  
 			return false;
 	}
 	
 	
 	
 
 	/**
 	 * merges this Square with another Square in a given direction 
 	 * 
 	 * @param otherSquare
 	 * 			the other Square to merge with this one
 	 * @param direction
 	 * 			the direction the two squares are merged
 	 * 
	 *  @throws	throws an IllegalArgumentException if otherSquare's reference is null or the other Square equal to this Square 
 	 *  		or the direction is not valid
 	 *  		| if(otherSquare == null || otherSquare == this || (! isValidDirection(direction))
 	 *  		|	then throws IllegalArgumentException
 	 *  
	 *  @post	sets the direction in which the squares are merged to false
 	 *  		|	then new.getBorderInDirection(direction) == false &&
 	 *  		|		(new otherSquare).getBorderInDirection(direction) == false
	 *  @post	sets the humidity of both squares to the average humidity of both squares
 	 *  		|		new.getHumidity() = getAverageHumidity(this.getHumidity(),otherSquare.getHumidity()) &&
 	 *  		|		(new otherSquare).getHumidity() = getAverageHumidity(this.getHumidity(),otherSquare.getHumidity())
	 *  @post	sets the temperature of both squares to the new temperature
 	 *  		|		then new.getTemperatureInCelcius =  this.getTemperatureInCelcius() * (1 - mergeConstant) * 
 	 *  		|						(this.getHumidity/(getAverageHumidity(this.getHumidity(), otherSquare.getHumidity()))) + mergeConstant; 
 	 *  		|		(new otherSquare).getTemperatureInCelcius = otherSquare.getTemperatureInCelcius() * (1 - mergeConstant) * 
 	 *  		|						(otherSquare.getHumidity/(getAverageHumidity(this.getHumidity(), otherSquare.getHumidity()))) + mergeConstant; 
 	 */
 	public void mergeWithSquareInDirection(Square otherSquare, int direction) throws IllegalArgumentException{
 		
 		if (otherSquare == null || otherSquare == this || (! isValidDirection(direction)))
 				throw new IllegalArgumentException();
 		
 		this.setBorderIndirectionTo(direction, false);
 		otherSquare.setBorderIndirectionTo(direction, false);
 		
 		double averageHumidity = getAverageHumidity(this.getHumidity(), otherSquare.getHumidity());	
 		
 		double a = 1 - mergeConstant; 	
 		
 		double weightTemperatureOne;
 		double weightTemperatureTwo;
 		if(averageHumidity == 0){
 			weightTemperatureOne = 1;
 			weightTemperatureTwo = 1;	
 		}
 		else {
 		weightTemperatureOne = (a*(this.getHumidity()/averageHumidity)) + mergeConstant;
 		weightTemperatureTwo = (a*(otherSquare.getHumidity()/averageHumidity)) + mergeConstant;	
 		}
 		
 		double newTemperature = (this.getTemperatureInCelcius()*weightTemperatureOne+otherSquare.getTemperatureInCelcius()*weightTemperatureTwo)/2;
 		this.setTemperature(newTemperature);
 		otherSquare.setTemperature(newTemperature);
 		
 		//Makes sure that the new humidity only has two decimal places
 		double roundedAverageHumidity = ((int) (averageHumidity*100))/100;
 		this.setHumidity(roundedAverageHumidity);
 		otherSquare.setHumidity(averageHumidity);
 	}
 	
 	public static final double mergeConstant = 0.2;	
 	
 	/**
 	 * returns the average humidity of two given humidities 
 	 * 
 	 * @param 	humidity1
 	 * 			the first given humidity
 	 * @param	humidty2
 	 * 			the second given humidity
 	 * @return	the result equals the average of the two humidities
 	 * 			|	result == (humidity1+humidty2)/2
 	 * 			
 	 */
 	public double getAverageHumidity(double humidity1,double humidty2){
 		return (humidity1+humidty2)/2;
 	}
 	
 	
 }
