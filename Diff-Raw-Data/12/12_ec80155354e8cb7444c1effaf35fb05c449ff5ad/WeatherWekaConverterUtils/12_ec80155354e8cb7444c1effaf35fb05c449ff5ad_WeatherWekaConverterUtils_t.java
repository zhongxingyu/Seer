 package com.weathermining.weka;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.converters.ConverterUtils;
 
 import com.weathermining.model.Weather;
 
 public class WeatherWekaConverterUtils {
 	
 	private static int NUM_ATTRIBUTES = 19;
 	private static Attribute dateTime = new Attribute("dateTime");
 	private static Attribute temperatureCelcius = new Attribute("temperatureCelcius");
 	private static Attribute humidityPercentage = new Attribute("humidityPercentage");
 	private static Attribute dewpointCelcius = new Attribute("dewpointCelcius");
 	private static Attribute windSpeedMetersPerSecond = new Attribute("windSpeedMetersPerSecond");
 	private static Attribute windDirection;
 	private static Attribute barometerPressurePascal = new Attribute("barometerPressurePascal");
 	private static Attribute barometerTendency;
 	private static Attribute totalRainTodayMillimeters = new Attribute("totalRainTodayMillimeters");
 	private static Attribute rainRateMillimetersPerHour = new Attribute("rainRateMillimetersPerHour");
 	private static Attribute totalStormMillimeters = new Attribute("totalStormMillimeters");
 	private static Attribute totalMonthlyRainMillimeters = new Attribute("totalMonthlyRainMillimeters");
 	private static Attribute totalYearlyRainMillimeters = new Attribute("totalYearlyRainMillimeters");
 	private static Attribute thwIndexCelcius = new Attribute("thwIndexCelcius");
 	private static Attribute heatIndexCelcius = new Attribute("heatIndexCelcius");
 	private static Attribute windChillCelcius = new Attribute("windChillCelcius");
 	private static Attribute uvIndex = new Attribute("uvIndex");
 	private static Attribute solarRadiationWattsPerSquareMeter = new Attribute("solarRadiationWattsPerSquareMeter");
 	private static Attribute rainIntensity;
 		
 	
 	
 	static {
 				
 	}
 
 	public static String convertWeatherDataToArff(Collection<Weather> weatherData) throws Exception {
 		FastVector attributes = createAttributes(weatherData);
 		Instances instances = new Instances("weather", attributes, weatherData.size());
 		for(Weather weather:weatherData) {
 			Instance instance = new Instance(NUM_ATTRIBUTES);
 			instance.setValue(dateTime, weather.getDateTime());
 			instance.setValue(temperatureCelcius, weather.getTemperatureCelcius());
 			instance.setValue(humidityPercentage, weather.getHumidityPercentage());
 			instance.setValue(dewpointCelcius, weather.getDewpointCelcius());
 			instance.setValue(windSpeedMetersPerSecond, weather.getWindSpeedMetersPerSecond());
 			instance.setValue(windDirection, weather.getWindDirection().toString());
 			instance.setValue(barometerPressurePascal, weather.getBarometerPressurePascal());
 			instance.setValue(barometerTendency, weather.getBarometerTendency());
 			instance.setValue(totalRainTodayMillimeters, weather.getTotalRainTodayMillimeters());
 			instance.setValue(rainRateMillimetersPerHour, weather.getRainRateMillimetersPerHour());
 			instance.setValue(totalStormMillimeters, weather.getTotalStormMillimeters());
 			instance.setValue(totalMonthlyRainMillimeters, weather.getTotalMonthlyRainMillimeters());
 			instance.setValue(totalYearlyRainMillimeters, weather.getTotalYearlyRainMillimeters());
 			instance.setValue(thwIndexCelcius, weather.getThwIndexCelcius());
 			instance.setValue(heatIndexCelcius, weather.getHeatIndexCelcius());
 			instance.setValue(windChillCelcius, weather.getWindChillCelcius());
 			instance.setValue(uvIndex, weather.getUvIndex());
 			instance.setValue(solarRadiationWattsPerSquareMeter, weather.getSolarRadiationWattsPerSquareMeter());
 			instance.setValue(rainIntensity, classifyRainIntensity(weather.getRainRateMillimetersPerHour()).toString());
 			instances.add(instance);
 		}
 		
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ConverterUtils.DataSink dataSink = new ConverterUtils.DataSink(baos);
 		dataSink.write(instances);
 		
 		return baos.toString();
 	}
 	
 	private static FastVector createAttributes(Collection<Weather> weatherData) {
 		FastVector attributes = new FastVector(NUM_ATTRIBUTES);
 		FastVector directions = new FastVector(19);
 		directions.addElement("N");
 		directions.addElement("NNE");
 		directions.addElement("ENE");
 		directions.addElement("NE");
 		directions.addElement("E");
 		directions.addElement("ESE");
 		directions.addElement("SE");
 		directions.addElement("SSE");
 		directions.addElement("S");
 		directions.addElement("SSW");
 		directions.addElement("SW");
 		directions.addElement("WSW");
 		directions.addElement("W");
 		directions.addElement("WNW");
 		directions.addElement("NW");
 		directions.addElement("NNW");
 		windDirection = new Attribute("windDirection", directions);
 		barometerTendency = new Attribute("barometerTendency", createBarometerTendencyValues(weatherData));
 		FastVector intensities = new FastVector(4);
 		intensities.addElement("None");
 		intensities.addElement("Light");
 		intensities.addElement("Moderate");
 		intensities.addElement("Heavy");
 		rainIntensity = new Attribute("rainIntensity", intensities);
 		attributes.addElement(dateTime);
 		attributes.addElement(temperatureCelcius);
 		attributes.addElement(humidityPercentage);
 		attributes.addElement(dewpointCelcius);
 		attributes.addElement(windSpeedMetersPerSecond);
 		attributes.addElement(windDirection);
 		attributes.addElement(barometerPressurePascal);
 		attributes.addElement(barometerTendency);
 		attributes.addElement(totalRainTodayMillimeters);
 		attributes.addElement(rainRateMillimetersPerHour);
 		attributes.addElement(totalStormMillimeters);
 		attributes.addElement(totalMonthlyRainMillimeters);
 		attributes.addElement(totalYearlyRainMillimeters);
 		attributes.addElement(thwIndexCelcius);
 		attributes.addElement(heatIndexCelcius);
 		attributes.addElement(windChillCelcius);
 		attributes.addElement(uvIndex);
 		attributes.addElement(solarRadiationWattsPerSquareMeter);
 		attributes.addElement(rainIntensity);
 		return attributes;
 	}
 	
 	private static FastVector createBarometerTendencyValues(Collection<Weather> weatherData) {		
 		FastVector values = new FastVector();
 		HashSet<String> set = new HashSet<String>();
 		for(Weather weather:weatherData) {
 			set.add(weather.getBarometerTendency());
 		}
 		
 		Iterator<String> it = set.iterator();
 		while(it.hasNext())
 			values.addElement(it.next());
 		
 		return values;
 	}
 
	private static RainIntensity classifyRainIntensity(double rainRateMillimetersPerHour) {
		if(rainRateMillimetersPerHour == 0)
 			return RainIntensity.None;
		else if(rainRateMillimetersPerHour <= 2.5)
 			return RainIntensity.Light;
		else if(rainRateMillimetersPerHour <= 7.6)
 			return RainIntensity.Moderate;
 		else 
 			return RainIntensity.Heavy;
 	}
 	
 	private static enum RainIntensity {
 		None, Light, Moderate, Heavy
 	}
 }
