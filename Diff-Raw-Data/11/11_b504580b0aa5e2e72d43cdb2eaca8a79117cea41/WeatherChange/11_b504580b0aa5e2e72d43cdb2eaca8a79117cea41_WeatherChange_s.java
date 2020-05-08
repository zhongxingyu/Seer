 package tk.nekotech.war.events;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.weather.WeatherChangeEvent;
 
 public class WeatherChange implements Listener {
 	
 	@EventHandler
 	public void onWeatherChange(WeatherChangeEvent event) {
		event.getWorld().setStorm(false);
 	}
 
 }
