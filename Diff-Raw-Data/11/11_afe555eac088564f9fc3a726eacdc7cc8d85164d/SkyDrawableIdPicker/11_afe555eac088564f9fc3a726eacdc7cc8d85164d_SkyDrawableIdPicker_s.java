 /**
  * 
  */
 package it.giacomos.android.osmer.observations;
 
 import it.giacomos.android.osmer.R;
 
 import java.util.Calendar;
 import java.util.Date;
 import android.util.Log;
 
 
 /**
  * @author giacomo
  *
  */
 public class SkyDrawableIdPicker {
 
 	/**
 	 * 
 	 */
 	public int get(String sky) 
 	{
 		int id = -1;
 		/* what time is it? decide icons */
 		
 		boolean night = new SunsetCalculator().isDark();
 		
 		/* choose the right icon now! */
 		if(sky.contains("sereno"))
 		{
 			if(night)
 				id = (R.drawable.weather_sky_0_nite);
 			else
 				id = (R.drawable.weather_sky_0);
 		}
 		else if(sky.contains("poco") && sky.contains("nuv"))
 		{
 			if(night)
 				id = (R.drawable.weather_few_clouds_nite);
 			else
 				id = (R.drawable.weather_few_clouds);
 		}
 		else if(sky.contains("nuvoloso"))
 		{
 			if(night)
 				id = (R.drawable.weather_sky_3_nite);
 			else
 			{
 				id = (R.drawable.weather_sky_3);
 			}
 		}
 		else if(sky.contains("coperto"))
 		{
 			/* the same for day and night */
 			id = (R.drawable.weather_sky_4);
 		}
 		else if(sky.contains("variabil"))
 		{
 			if(night)
 				id = (R.drawable.weather_clouds_nite);
 			else
 				id = (R.drawable.weather_clouds);
 		}
 		else if(sky.contains("neve") && sky.contains("piogg")) /* pioggia e neve */
 		{
 				id = (R.drawable.snow_rain);
 		}	
 		else if(sky.contains("piogg"))
 		{
			id = (R.drawable.weather_showers);
 		}
 		else if(sky.contains("neve"))
 		{
 				id = (R.drawable.weather_snow);
 		}
 		else if(sky.contains("temporal"))
 		{
 			if(night)
 				id = (R.drawable.weather_storm_night);
 			else
 				id = (R.drawable.weather_storm_13);
 		}
 		else if(sky.contains("grandin"))
 		{
 			id = (R.drawable.hail);
 		}
 		else if(sky.contains("piogg") && sky.contains("ghiac"))
 		{
 			id = (R.drawable.freezing_rain);
 		}
 		else if(sky.contains("nebbia"))
 			id = (R.drawable.weather_mist);
 		
 		return id;
 	}
 }
