 package eu.justas.jweather.ui;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 
 import org.primefaces.event.map.OverlaySelectEvent;
 import org.primefaces.model.map.DefaultMapModel;
 import org.primefaces.model.map.LatLng;
 import org.primefaces.model.map.MapModel;
 import org.primefaces.model.map.Marker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.justas.jweather.domain.Weather;
 import eu.justas.jweather.service.WeatherService;
 
 @ManagedBean(name = "mapBean")
 @SessionScoped
 public class MapBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger log = LoggerFactory.getLogger(MapBean.class);
 
	@ManagedProperty(value = "#{weatherService}")
 	private WeatherService weatherService;
 
 	private MapModel advancedModel;
 
 	private Marker marker;
 
 	@PostConstruct
 	public void init() {
 		advancedModel = new DefaultMapModel();
 
 		for (Weather weather : weatherService.returnWeather(null)) {
 
 			Double lat = Double.valueOf(weather.getWeatherObj().getCurrent_observation().getDisplay_location()
 					.getLatitude());
 			Double lng = Double.valueOf(weather.getWeatherObj().getCurrent_observation().getDisplay_location()
 					.getLongitude());
 			LatLng coord = new LatLng(lat, lng);
 
 			advancedModel.addOverlay(new Marker(coord, weather.getCity(), weather, weather.getIconUrl()));
 			log.debug("Marker added: " + weather.getCity());
 		}
 	}
 
 	public MapModel getAdvancedModel() {
 		return advancedModel;
 	}
 
 	public List<Weather> getWeather() {
 		return weatherService.returnWeather(null);
 	}
 
 	public void onMarkerSelect(OverlaySelectEvent event) {
 		marker = (Marker) event.getOverlay();
 	}
 
 	public Marker getMarker() {
 		return marker;
 	}
 
 	public void setWeatherService(WeatherService weatherService) {
 		this.weatherService = weatherService;
 	}
 }
