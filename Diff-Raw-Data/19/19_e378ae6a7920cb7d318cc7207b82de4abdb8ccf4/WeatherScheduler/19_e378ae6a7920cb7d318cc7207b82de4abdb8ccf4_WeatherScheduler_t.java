 package com.tieto.weather.scheduler;
 
 import com.tieto.weather.error.ServerError;
 import com.tieto.weather.service.WeatherService;
 import com.tieto.weather.vo.CitiesVO;
 
 public class WeatherScheduler {
 	
	private WeatherService weatherService;
 	private CitiesVO cities;
 	
	public WeatherScheduler(WeatherService weatherService, CitiesVO cities) throws ServerError {
		this.weatherService = weatherService;
		this.cities = cities;
 		// to be able to load everything before context is initialized 
 		readWeathers();
 	}
 
 	public void readWeathers() throws ServerError {
 		for (String city : cities.getCities().keySet()) {
 			System.out.println("Scheduled." + city);
			weatherService.updateWeatherData(city);
 		}
 	}
 }
