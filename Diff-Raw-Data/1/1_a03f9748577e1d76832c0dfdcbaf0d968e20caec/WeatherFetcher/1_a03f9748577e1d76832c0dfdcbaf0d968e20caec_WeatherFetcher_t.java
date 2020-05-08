 package no.vindsiden.process;
 
 import java.io.IOException;
 
 import no.vindsiden.configuration.Configuration;
 import no.vindsiden.vindsiden.Measurement;
 import no.vindsiden.vindsiden.VindsidenHttpClient;
 import no.vindsiden.weatherstation.WeatherStation;
 
 import org.apache.commons.httpclient.HttpException;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.PeriodFormat;
 
 
 /**
  * @author Erik Mohn - mohn.erik@gmail.com
  */
 public class WeatherFetcher {
 
 	private VindsidenHttpClient httpClient;
 	private Configuration configuration;
 
 	public WeatherFetcher(Configuration config) {
 		this.httpClient = new VindsidenHttpClient();
 		this.configuration = config;
 	}
 
 	public void execute() {
 		for (WeatherStation<?> weatherStation : configuration.getWeatherStationList()) {
 			try {
 				processSingleWeatherStation(weatherStation);
 			} catch (Exception e) {
				log("Error occured while processing: " + weatherStation.getName());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void processSingleWeatherStation(WeatherStation<?> weatherStation) throws IOException, HttpException {
 		Measurement measurement = weatherStation.fetchMeasurement();
 		httpClient.sendHttpRequest(measurement);
 		
 		DateTime now = new DateTime();
 		DateTimeFormatter fmt = DateTimeFormat.mediumDateTime();
 		
 		log( fmt.print(now) + " Executed HTTP request, for " + weatherStation.getName() + " : " + measurement.toVindSidenUrl());
 	}
 
 	public void setHttpClient(VindsidenHttpClient httpClient) {
 		this.httpClient = httpClient;
 	}
 
 	protected void log(String log) {
 		System.out.println(log);
 	}
 
 }
