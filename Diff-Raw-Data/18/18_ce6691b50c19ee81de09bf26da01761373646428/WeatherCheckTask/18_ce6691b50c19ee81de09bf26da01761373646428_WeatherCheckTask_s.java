 package wfm.task;
 
 
 import org.activiti.engine.delegate.DelegateExecution;
 import org.activiti.engine.delegate.JavaDelegate;
 import org.fedy2.weather.YahooWeatherService;
 import org.fedy2.weather.data.Channel;
 import org.fedy2.weather.data.Forecast;
 import org.fedy2.weather.data.unit.DegreeUnit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WeatherCheckTask implements JavaDelegate {
 
 	public static String woeidVienna = "12591694"; // woeid vieanna check: http://woeid.rosselliot.co.nz/
 	public static int limitTemperature = 15; // if high temperature is below this limit -> weather bad   STD.= 15
 	private static final Logger log = LoggerFactory.getLogger(WeatherCheckTask.class);
 
 	@Override
 	public void execute(DelegateExecution execution) throws Exception {
 
 		// http://developer.yahoo.com/weather/
 		// for more information on how the weather service works and the codes
		// need to be interpreted
		
 		Forecast forecast = null;
 		try {
 		YahooWeatherService service = new YahooWeatherService();
		Channel channel = service.getForecast(WeatherCheckTask.woeidVienna,
				DegreeUnit.CELSIUS);
 		// read tomorrows weather condition
 		forecast = channel.getItem().getForecasts().get(0);
		
 		if (forecast==null) {
 			throw new NullPointerException("Could not retreive Forecast!");
 		}
 		
 		}
 		catch (Exception ex) {
			log.error("Could not receive weather forecast for the next day - expecting the worst case " + ex.getMessage());
 			execution.setVariable("weatherOk", false);
 			return;
 		}
 		// needless assignments to point out the meaning of the forecast
 		int conditioncode = forecast.getCode(); // numeric code describing the weather see:  http://developer.yahoo.com/weather/#codes
 		String conditiontext = forecast.getText(); // the weather text
 		int highTemp = forecast.getHigh(); // highest temperature of the next day
 		
 		
 		//if weather code predicts snow, rain, heavy wind etc. or the temperature is below our defined limit for outdoor course - it evaluates to true
 		if ((conditioncode < 19 || conditioncode >= 37 || conditioncode == 35) || highTemp < limitTemperature) {
 			// cancel course
 			log.info("Weather is really bad. it is " + conditiontext + " let Trainer decide wthat to do next");
 			execution.setVariable("weatherOk", false);
 			execution.setVariable("badWeatherCondition", conditiontext);
 		} else {
 			log.info("Weather is " + conditiontext + " - Course is taking place as scheduled: ");
 			execution.setVariable("weatherOk", true);
 		}
 
 	}
 
 }
