 package no.vindsiden.parser.impl;
 
 import java.io.IOException;
 import java.math.BigDecimal;
import java.net.URLEncoder;
 import java.util.TimeZone;
 
 import no.vindsiden.parser.WeatherDataParser;
 import no.vindsiden.parser.impl.support.DavisDescription;
 import no.vindsiden.parser.impl.support.WeatherDataUnavailableException;
 import no.vindsiden.parser.impl.support.WindUnitType;
 import no.vindsiden.vindsiden.Measurement;
 import no.vindsiden.weatherstation.WeatherStation;
 import no.vindsiden.weatherstation.impl.DavisWeatherStation;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.lang.math.NumberUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 public class DavisDataParser extends WeatherDataParser {
 
 	private String[] data;
 	private Measurement measurement;
 
 	public DavisDataParser(WeatherStation<DavisDataParser> weatherStation) {
 		setWeatherStation(weatherStation);
 	}
 
 	@Override
 	public Measurement fetchMeasurement() throws IOException {
 		fetchDataFromWeatherStation();
 
 		parseWeatherData();
 		
 		return measurement;
 	}
 
 	private void parseWeatherData()  {		
 		if (data.length < DavisDescription.AVG_WIND_SPEED_10_MINUTES.getIndex()) {
 			throw new WeatherDataUnavailableException("Davis WeatherData not available");
 		}
 		
 		Measurement m = new Measurement();
 		m.setStationID(getWeatherStation().getWeatherStationId());	
 		m.setTime(new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Oslo"))));
 
 		Double avgWindSpeed = getWindSpeed(Double.parseDouble(data[DavisDescription.AVG_WIND_SPEED_10_MINUTES.getIndex()])); 
 		Double windSpeed = getWindSpeed(Double.parseDouble(data[DavisDescription.AVG_WIND_SPEED.getIndex()]));
 		Double maxWindSpeed = getWindSpeed(Double.parseDouble(data[DavisDescription.GUSTS.getIndex()]));
 			
 		m.setWindAvg(avgWindSpeed);
 		m.setWindMin(NumberUtils.min(avgWindSpeed, maxWindSpeed, windSpeed));
 		m.setWindMax(NumberUtils.max(avgWindSpeed, maxWindSpeed, windSpeed));
 		
 		m.setDirectionAvg(Integer.parseInt(data[DavisDescription.WIND_DIRECTION.getIndex()]));
 		m.setTemperature1(Double.parseDouble(data[DavisDescription.TEMP.getIndex()]));
 
 		m.setWindVectorAvg(-999.0);
 		m.setWindStDev(0.5);
 		m.setDirectionStDev(0.0);
 		m.setTemperature2(-999.0);
 		m.setLight(-999);
 		m.setBattery(-999.0);
 		
 		measurement = m;
 	}
 
 	private Double getWindSpeed(double windSpeed) {
 		double ws = windSpeed;
 		if (WindUnitType.KNOTS.equals(((DavisWeatherStation)getWeatherStation()).getWindUnitType())) {
 			ws = windSpeed * 0.5144;
 		}
 		BigDecimal wsBigDecimal = new BigDecimal(ws).setScale(1, BigDecimal.ROUND_UP);
 		return wsBigDecimal.doubleValue();
 	}
 
 	private void fetchDataFromWeatherStation() throws IOException, HttpException {
 		String url = ((DavisWeatherStation) getWeatherStation()).getUrl();
 		HttpClient httpClient = new HttpClient();
 		GetMethod method = new GetMethod(url + "/clientraw.txt");
 
 		httpClient.executeMethod(method);
 		byte[] result = method.getResponseBody();
 		data = new String(result).split(" ");
 		method.releaseConnection();
 	}
 
 }
