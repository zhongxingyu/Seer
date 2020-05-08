 package no.vindsiden.process;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import no.vindsiden.configuration.Configuration;
 import no.vindsiden.process.support.WeatherStationComparator;
 import no.vindsiden.vindsiden.Measurement;
 import no.vindsiden.vindsiden.VindsidenHttpClient;
 import no.vindsiden.weatherstation.WeatherStation;
 
 import org.apache.commons.httpclient.HttpException;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 
 /**
  * @author Erik Mohn - mohn.erik@gmail.com
  */
 public class WeatherFetcher {
 
 	private Configuration configuration;
 	private VindsidenHttpClient httpClient;
 	private List<WeatherStation<?>> weatherStations;
 	private List<WeatherStation<?>> failedWeatherStations;
 	private boolean inErrorHandling = false; 
 	
 	public WeatherFetcher(Configuration config) {
 		this.configuration = config;
 		this.httpClient = new VindsidenHttpClient();
 		this.weatherStations = config.getWeatherStationList();
 		this.failedWeatherStations = new ArrayList<WeatherStation<?>>();
 	}
 
 	public void execute() {
 		processWeatherStations(weatherStations);
 		if (failedWeatherStationsExists()) {
 			log("Will do error handling of: " + failedWeatherStations);
 			executeErrorHandling();			
 		}	
 	}
 
 	private void processWeatherStations(List<WeatherStation<?>> weatherStations) {
 		Collections.sort(weatherStations, new WeatherStationComparator());
 		for (WeatherStation<?> weatherStation : weatherStations) {
 			try {
 				if(weatherStation.isEnabled()) {
 					processWeatherStation(weatherStation);					
 				}
 			} catch (Exception e) {
 				log("Error occured while processing: " + weatherStation);
 				if (!inErrorHandling) {
 					failedWeatherStations.add(weatherStation);					
 				}
				e.printStackTrace();
 			}
 		}
 	}
 
 	private void processWeatherStation(WeatherStation<?> weatherStation) throws IOException, HttpException {
 		Measurement measurement = weatherStation.fetchMeasurement();
 		httpClient.sendHttpRequest(measurement);
 		
 		DateTime now = new DateTime();
 		DateTimeFormatter fmt = DateTimeFormat.mediumDateTime();
 		log( fmt.print(now) + " Executed HTTP request, for " + weatherStation.getName() + " : " + measurement.toVindSidenUrl());
 	}
 
 	private void executeErrorHandling() {
 		inErrorHandling = true;
 		try {
 			Thread.sleep(configuration.getTimeToSleepBeforeErrorHandling());	
 			processWeatherStations(failedWeatherStations);
 		} catch (InterruptedException e) {
 			log("Thread.sleep Interrupted");
 			e.printStackTrace();
 		} finally {
 			clearFailedWeatherStations();
 		}
 	}
 	
 	private boolean failedWeatherStationsExists() {
 		return failedWeatherStations.size() != 0;
 	}
 	
 	private void clearFailedWeatherStations() {
 		failedWeatherStations.clear();
 	}
 
 	public void setHttpClient(VindsidenHttpClient httpClient) {
 		this.httpClient = httpClient;
 	}
 
 	protected void log(String log) {
 		System.out.println(log);
 	}
 	
 }
