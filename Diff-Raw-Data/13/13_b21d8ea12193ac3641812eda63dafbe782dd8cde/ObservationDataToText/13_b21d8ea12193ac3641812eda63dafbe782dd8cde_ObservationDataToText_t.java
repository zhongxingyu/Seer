 package it.giacomos.android.osmer.widgets.map;
 
 import android.content.res.Resources;
 import it.giacomos.android.osmer.R;
 import it.giacomos.android.osmer.observations.MapMode;
 import it.giacomos.android.osmer.observations.ObservationData;
 import it.giacomos.android.osmer.observations.ObservationType;
 
 public class ObservationDataToText 
 {
 	private MapMode mMapMode;
 	private ObservationType mObservationType;
 	private Resources mResources;
 	
 	public ObservationDataToText(MapMode mapMode, 
 			ObservationType observationType, 
 			Resources resources)
 	{
 		mMapMode = mapMode;
 		mObservationType = observationType;
 		mResources = resources;
 	}
 	
 	public String toText(ObservationData od)
 	{
 		String txt;
 		txt = od.time + " - " + mResources.getString(R.string.sky) + ": " + od.sky + "\n";
 		
 		if(mMapMode == MapMode.DAILY_OBSERVATIONS)
 		{
 			if(mObservationType == ObservationType.SKY || 
 					mObservationType == ObservationType.MIN_TEMP ||  
 					mObservationType == ObservationType.MAX_TEMP ||  
 					mObservationType == ObservationType.AVERAGE_TEMP)
 			{
 				boolean hasTMin, hasTMax;
 				hasTMin = od.has(ObservationType.MIN_TEMP);
 				hasTMax = od.has(ObservationType.MAX_TEMP);
 				/* tmin and tmax abbreviated on the same line */
 				if(hasTMin)
 					txt += mResources.getString(R.string.min_temp_abbr) + ": " + od.tMin;
 				if(hasTMax)
 					txt += mResources.getString(R.string.max_temp_abbr) + ": " + od.tMax;
 				if(hasTMin || hasTMax) /* newline if necessary */
 					txt += "\n";
 				if(od.has(ObservationType.AVERAGE_TEMP))
 					txt += mResources.getString(R.string.mean_temp) + ": " + od.tMed + "\n";
 			}
 			else if(mObservationType == ObservationType.RAIN)
 			{
 				if(od.has(ObservationType.RAIN))
 					txt += mResources.getString(R.string.rain) + ": " + od.rain + "\n";	
 			}
 			else if(mObservationType == ObservationType.MAX_WIND || mObservationType == ObservationType.AVERAGE_WIND)
 			{
 				if(od.has(ObservationType.AVERAGE_WIND))
 					txt += mResources.getString(R.string.mean_wind) + ": " + od.vMed + "\n";
 				if(od.has(ObservationType.MAX_WIND))
 					txt += mResources.getString(R.string.max_wind) + ": " + od.vMax + "\n";
 			}
 			else if(mObservationType == ObservationType.AVERAGE_HUMIDITY)
 			{
 				if(od.has(ObservationType.AVERAGE_HUMIDITY))
 					txt += mResources.getString(R.string.mean_humidity) + ": " + od.uMed + "\n";
 			}
 		}
 		else 
 		{
 			if(mObservationType == ObservationType.SKY || 
 					mObservationType == ObservationType.TEMP ||  
 					mObservationType == ObservationType.SEA ||  
 					mObservationType == ObservationType.SNOW || 
 					mObservationType == ObservationType.RAIN)
 			{
 				if(od.has(ObservationType.TEMP))
 					txt += mResources.getString(R.string.temp) + ": " + od.temp + "\n";
 				if(od.has(ObservationType.SEA))
 					txt += mResources.getString(R.string.sea) + ": " + od.sea + "\n";
 				if(od.has(ObservationType.SNOW))
 					txt += mResources.getString(R.string.snow) + ": " + od.snow + "\n";
 				if(od.has(ObservationType.RAIN))
 				{
 					String rain = od.rain;
 					rain = rain.replaceAll("[^\\d+\\.)]", "");
					try
					{
						/* when observation type is rain, show rain even if rain is 0.0 */
						if(Float.parseFloat(rain) > 0.0f || mObservationType == ObservationType.RAIN)
							txt += mResources.getString(R.string.rain) + ": " + od.rain + "\n";
					}
					catch(NumberFormatException e)
					{
						
					}
 				}
 			}
 			else if(mObservationType == ObservationType.HUMIDITY && od.has(ObservationType.HUMIDITY))
 				txt += mResources.getString(R.string.humidity) + ": " + od.humidity + "\n";
 			else if(mObservationType == ObservationType.PRESSURE && od.has(ObservationType.PRESSURE))
 				txt += mResources.getString(R.string.pressure) + ": " + od.pressure + "\n";
 			
 			else if(mObservationType == ObservationType.WIND && od.has(ObservationType.WIND))
 				txt += mResources.getString(R.string.wind) + ": " + od.wind + "\n";
 		}
 
 		return txt;
 	}
 }
