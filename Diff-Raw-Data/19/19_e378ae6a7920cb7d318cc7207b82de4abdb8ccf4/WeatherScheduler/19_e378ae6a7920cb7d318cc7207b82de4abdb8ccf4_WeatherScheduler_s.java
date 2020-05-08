 package com.tieto.weather.scheduler;
 
 import com.tieto.weather.error.ServerError;
 import com.tieto.weather.service.WeatherService;
 import com.tieto.weather.vo.CitiesVO;
 
 public class WeatherScheduler {
 	
	private WeatherService service;
 	private CitiesVO cities;
 	
	public WeatherScheduler() throws ServerError {
 		// to be able to load everything before context is initialized 
 		readWeathers();
 	}
 
 	public void readWeathers() throws ServerError {
 		for (String city : cities.getCities().keySet()) {
 			System.out.println("Scheduled." + city);
			service.updateWeatherData(city);
 		}
 	}

	public void setWeatherService(WeatherService service) {
		this.service = service;
	}
	
	public void setCities(CitiesVO cities) {
		this.cities = cities;
	}
 }
