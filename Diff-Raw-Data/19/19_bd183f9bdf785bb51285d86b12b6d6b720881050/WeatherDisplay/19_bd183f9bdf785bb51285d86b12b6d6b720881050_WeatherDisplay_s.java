 package edu.mines.acmX.exhibit.modules.home_screen.view.weather;
 
 import java.util.List;
 
 import processing.core.PApplet;
 import processing.core.PImage;
 import edu.mines.acmX.exhibit.modules.home_screen.view.DisplayElement;
 
 /**
  * This is the main class for the Weather display at the bottom of the home
  * screen. It uses the WeatherLoader to pull data from an online XML and parses
  * accordingly. On the main screen, it's tiled with the time display. Polls
  * for new weather information every 10 minutes.
  * 
  * TODO use forecasting data in the expanded weather dialog
  * 
  * @author Ryan Stauffer
  * @author Matthew Stech
  * 
  * @see {@link WeatherLoader} {@link WeatherCurrentInfo} {@link WeatherForecastDayInfo}
  */
 public class WeatherDisplay extends DisplayElement {
 
 	//time (in minutes) to reload weather information
 	public static final int TIME_TO_REFRESH = 10;
 	//stores current weather information (main display)
 	private WeatherCurrentInfo currentInfo;
 	//stores forecasted data (currently unused)
 	private List<WeatherForecastDayInfo> forecastInfo;
 	private PImage img;
 	//stores millis, to check when to refresh the weather data
 	private int lastUpdate;
 	
 	public WeatherDisplay(PApplet parent, double weight) {
 		super(parent, weight);
 		lastUpdate = parent.millis();
 		WeatherLoader.loadWeatherInfo();
 		currentInfo = WeatherLoader.getCurrentInfo();
 		forecastInfo = WeatherLoader.getForecastInfo();
 		img = parent.loadImage(currentInfo.getPicture());
 	}
 
 	@Override
 	public void update(int x, int y) {
 		originX = x;
 		originY = y;
 		//updates weather info after a set number of minutes (TIME_TO_REFRESH)
 		if (parent.millis() - lastUpdate > TIME_TO_REFRESH * 60000) {
 			lastUpdate = parent.millis();
 			WeatherLoader.loadWeatherInfo();
 			currentInfo = WeatherLoader.getCurrentInfo();
 			forecastInfo = WeatherLoader.getForecastInfo();
 			img = parent.loadImage(currentInfo.getPicture());
 		}
 	}
 
 	@Override
 	public void draw() {
 		//grey background rectangle
 		parent.fill(84, 84, 84);
 		parent.rect(originX, originY, width, height);
 		char deg = "\u00b0".toCharArray()[0]; //degree symbol in Unicode
		String temps = currentInfo.getTempF() + deg + "F (" + currentInfo.getTempC() + deg + "C)";
 		String description = currentInfo.getDescription();
 		String windString = "Wind Speed: " + currentInfo.getWindspeed() + " mph   Humidity: " + currentInfo.getHumidity() + "%";
 		parent.textAlign(PApplet.LEFT, PApplet.CENTER);
 		//off-white text
 		parent.fill(200, 200, 200);
 		parent.textSize(32);
 		//number of pixels to move away from edge
 		int initialOffset = 10;
 		//number of pixels between elements
 		int padding = 20;
 		parent.text(temps, originX + initialOffset, originY + height/2);
 		parent.text(description, originX + parent.textWidth(temps) + initialOffset + padding, originY + height/2);
 		parent.imageMode(PApplet.CORNERS);
 		int imgOffsetX = (int) (parent.textWidth(temps) + parent.textWidth(description) + initialOffset + 2 * padding - 10);
 		parent.image(img, originX + imgOffsetX,	originY, originX + imgOffsetX + height, originY + height); //make the image square
 		parent.imageMode(PApplet.CORNER);
 		parent.text(windString, originX + imgOffsetX + height + padding, originY + height/2);
 	}
 
 	
 }
