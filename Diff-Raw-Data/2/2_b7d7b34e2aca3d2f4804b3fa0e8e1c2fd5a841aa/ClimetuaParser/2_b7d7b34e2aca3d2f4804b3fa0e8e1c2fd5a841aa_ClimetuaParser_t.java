 package com.weathermining.climetua;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.weathermining.model.Direction;
 import com.weathermining.model.Weather;
 
 class ClimetuaParser {
 	
 	private static final Logger LOG = LoggerFactory.getLogger(ClimetuaParser.class);
 	
 	private static final String DOUBLE_REGEX = "[0-9]+(\\.[0-9]+)?";
 	private static final String CELCIUS_UNIT = "�C";
 	
 	private Pattern temperaturePattern;
 	private Pattern percentagePattern;
 	private Pattern windPattern;
 	private Pattern barometerPattern;
 	private Pattern measurePattern;
 	
 	public ClimetuaParser() {
 		temperaturePattern = Pattern.compile("("+DOUBLE_REGEX+")"+CELCIUS_UNIT);
 		percentagePattern = Pattern.compile("([0-9]+)%");
 		windPattern = Pattern.compile("(" + Direction.generateRegularExpression() + ") at ("+DOUBLE_REGEX+") m/s");
 		barometerPattern = Pattern.compile("("+DOUBLE_REGEX+") hPa & (.*)");
		measurePattern = Pattern.compile("("+DOUBLE_REGEX+")[ ]*([^ ]*)");
 	}
 	
 	public Weather parse(Document doc) {		
 		Elements elementsFSSF = doc.select("html body table tbody tr td font strong small font");
 		Elements elementsFSFS = doc.select("html body table tbody tr td font strong font small");
 		
 		Weather weather = new Weather();
 	
 		weather.setTemperatureCelcius(parseDouble(elementsFSSF.get(0).text(), temperaturePattern, 1));		
 		weather.setHumidityPercentage(parseDouble(elementsFSSF.get(1).text(), percentagePattern, 1));
 		weather.setDewpointCelcius(parseDouble(elementsFSSF.get(2).text(), temperaturePattern, 1));
 		parseWind(elementsFSFS.get(1).text(), weather);
 		parseBarometer(elementsFSSF.get(3).text(), weather);
 		weather.setTotalRainTodayMillimeters(Double.parseDouble(matchMeasure(elementsFSSF.get(4).text(), "mm")));
 		weather.setRainRateMillimetersPerHour(Double.parseDouble(matchMeasure(elementsFSSF.get(5).text(), "mm/hr")));
 		weather.setTotalStormMillimeters(Double.parseDouble(matchMeasure(elementsFSSF.get(6).text(), "mm")));
 		weather.setTotalMonthlyRainMillimeters(Double.parseDouble(matchMeasure(elementsFSSF.get(7).text(), "mm")));
 		weather.setTotalYearlyRainMillimeters(Double.parseDouble(matchMeasure(elementsFSSF.get(8).text(), "mm")));
 		weather.setWindChillCelcius(Double.parseDouble(matchMeasure(elementsFSFS.get(3).text(), CELCIUS_UNIT)));
 		weather.setThwIndexCelcius(Double.parseDouble(matchMeasure(elementsFSFS.get(5).text(), CELCIUS_UNIT)));
 		weather.setHeatIndexCelcius(Double.parseDouble(matchMeasure(elementsFSFS.get(7).text(), CELCIUS_UNIT)));
 		weather.setUvIndex(Double.parseDouble(matchMeasure(elementsFSFS.get(9).text(), "index")));
 		weather.setSolarRadiationWattsPerSquareMeter(Integer.parseInt(matchMeasure(elementsFSFS.get(11).text(), "W/m�")));
 		
 		return weather;
 	}
 	
 	private double parseDouble(String text, Pattern pattern, int group) {
 		Matcher matcher = pattern.matcher(text);
 		if(matcher.matches())
 			return Double.parseDouble(matcher.group(group));
 		else
 			LOG.warn("No match for pattern '"+pattern+"' in '"+text+"'");
 		return 0;
 	}
 	
 	private void parseWind(String text, Weather weather) {
 		Matcher matcher = windPattern.matcher(text);
 		if(matcher.matches()) {
 			weather.setWindDirection(Direction.valueOf(matcher.group(1)));
 			weather.setWindSpeedMetersPerSecond(Double.parseDouble(matcher.group(2)));
 		}	
 		else
 			LOG.warn("No match for pattern '"+windPattern.pattern()+"' in '"+text+"'");
 	}
 	
 	private void parseBarometer(String text, Weather weather) {
 		Matcher matcher = barometerPattern.matcher(text);
 		if(matcher.matches()) {
 			weather.setBarometerPressurePascal(Double.parseDouble(matcher.group(1))*100);
 			weather.setBarometerTendency(matcher.group(3));
 		}
 		else
 			LOG.warn("No match for pattern '"+barometerPattern.pattern()+"' in '"+text+"'");
 	}
 	
 	private String matchMeasure(String text, String unit) {
 		Matcher matcher = measurePattern.matcher(text);	
 		if(matcher.matches() && matcher.group(3).equals(unit))
 			return matcher.group(1);
 		else
 			LOG.warn("No match for pattern '"+measurePattern.pattern()+"' in '"+text+"'");
 		return "0";
 	}
 }
 
