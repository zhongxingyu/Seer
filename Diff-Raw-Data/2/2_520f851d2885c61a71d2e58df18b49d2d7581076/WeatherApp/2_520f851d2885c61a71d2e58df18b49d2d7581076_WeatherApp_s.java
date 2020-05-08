 package de.luho.weather;
 
 import java.util.Date;
 
 import de.luho.weather.db.WeatherDatabase;
 import de.luho.weather.google.GoogleWeatherClient;
 import de.luho.weather.google.GoogleWeatherParser;
 import de.luho.weather.google.GoogleWeatherService;
 
 public class WeatherApp {
 
 	private WeatherService service;
 	private WeatherFormatter formatter;
 	private WeatherStore store;
 	
 	public String getAndStoreForecastForCity(String city) {
 		Forecast forecast = service.getForecastForCity(city);
 		String formatted = formatter.format(forecast);
		//store.save(forecast);
 		return formatted;
 	}
 
 	public String getPastForecast(String city, Date date) {
 		if (new Date().before(date)) {
 			throw new IllegalArgumentException("Date should be in the past.");
 		}
 		Forecast past = store.find(city, date);
 		String formatted = formatter.format(past);
 		return formatted;
 	}
 	
 	public void setWeatherService(WeatherService service) {
 		this.service = service;
 	}
 
 	public void setWeatherFormatter(WeatherFormatter formatter) {
 		this.formatter = formatter;
 	}
 	
 	public void setWeatherStore(WeatherStore store) {
 		this.store = store;
 	}
 
 	public static void main(String[] args) {
 		GoogleWeatherService service = new GoogleWeatherService();
 		service.setGoogleWeatherClient(new GoogleWeatherClient());
 		service.setGoogleWeatherParser(new GoogleWeatherParser());
 		
 		WeatherApp wapp = new WeatherApp();
 		wapp.setWeatherService(service);
 		wapp.setWeatherFormatter(new ConsoleWeatherFormatter());
 		wapp.setWeatherStore(new WeatherDatabase());
 		
 		String forecast = wapp.getAndStoreForecastForCity("München");
 		System.out.println(forecast);
 	}
 }
