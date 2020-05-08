 package it.giacomos.android.osmer.pro.widgets.map.report;
 
 import it.giacomos.android.osmer.R;
 import android.content.Context;
 import android.content.res.Resources;
 import android.util.Log;
 
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class ReportData extends DataInterface
 {
	public String username, locality, comment, temperature, writable;
 	public int sky, wind;
 	
 	private Marker mMarker;
 	private MarkerOptions mMarkerOptions;
 	
 	public ReportData(String u, String d, String l, String c, 
 			String t, int s, int w, double lat, double longi, String writa)
 	{
 		super(lat, longi, d);
 		username = u;
 		locality = l;
 		comment = c;
 		temperature = t;
 		sky = s;
 		wind = w;
 		writable = writa;
 		mMarker = null;
 	}
 
 	public boolean isWritable()
 	{
 		return (writable.compareTo("w") == 0);
 	}
 
 	@Override
 	public int getType() 
 	{
 		return DataInterface.TYPE_REPORT;
 	}
 
 	@Override
 	public String getLocality() {
 		return locality;
 	}
 
 	@Override
 	public MarkerOptions buildMarkerOptions(Context ctx) 
 	{
 		String text = "";
 		String title = "";
 		String skystr = "";
 		int windIdx, iconId  = -1;
 		String windtxtItems[] = ctx.getResources().getStringArray(R.array.report_wind_textitems);
 		mMarkerOptions = null;
 		Resources res = ctx.getResources();
 
 		if(sky == 1)
 		{
 			iconId = R.drawable.weather_sky_0;
 			skystr += res.getString(R.string.sky0);
 		}
 		else if(sky == 2)
 		{
 			iconId = R.drawable.weather_few_clouds;
 			skystr += res.getString(R.string.sky1);
 		}
 		else if(sky == 3)
 		{
 			iconId = R.drawable.weather_clouds;
 			skystr += res.getString(R.string.sky2);
 		}
 		else if(sky == 4)
 		{
 			iconId = R.drawable.weather_sky_3;
 			skystr += res.getString(R.string.sky3);
 		}
 		else if(sky == 5)
 		{
 			iconId = R.drawable.weather_sky_4;
 			skystr += res.getString(R.string.sky4);
 		}
 		else if(sky == 6)
 		{
 			iconId = R.drawable.weather_rain_cloud_6;
 			skystr += res.getString(R.string.rain) + " " + res.getString(R.string.rain6);
 		}
 		else if(sky == 7)
 		{
 			iconId = R.drawable.weather_rain_cloud_7;
 			skystr += res.getString(R.string.rain) + " " + res.getString(R.string.rain7_abbrev);
 		}
 		else if(sky == 8)
 		{
 			iconId = R.drawable.weather_rain_cloud_8;
 			skystr += res.getString(R.string.rain) + " " + res.getString(R.string.rain8_abbrev);
 		}
 		else if(sky == 9)
 		{
 			iconId = R.drawable.weather_rain_cloud_9;
 			skystr += res.getString(R.string.rain) + " " + res.getString(R.string.rain9);
 		}
 		else if(sky == 10)
 		{
 			iconId = R.drawable.weather_rain_cloud_36;
 			skystr += res.getString(R.string.rain) + " " + res.getString(R.string.rain36);
 		}
 		else if(sky == 11)
 		{
 			iconId = R.drawable.weather_snow_10;
 			skystr += res.getString(R.string.snow) + " " + res.getString(R.string.snow10);
 		}
 		else if(sky == 12)
 		{
 			iconId = R.drawable.weather_snow_11;
 			skystr += res.getString(R.string.snow) + " " + res.getString(R.string.snow11);
 		}
 		else if(sky == 13)
 		{
 			iconId = R.drawable.weather_snow_12;
 			skystr += res.getString(R.string.snow) + " " + res.getString(R.string.snow12);
 		}
 		else if(sky == 14)
 		{
 			iconId = R.drawable.weather_storm_13;
 			skystr += res.getString(R.string.storm);
 		}
 		else if(sky == 15)
 		{
 			iconId = R.drawable.weather_mist_14;
 			skystr += res.getString(R.string.mist14);
 		}
 		else if(sky == 16)
 		{
 			iconId = R.drawable.weather_mist_15;
 			skystr += res.getString(R.string.mist15);
 		}
 		
 		title = username;
 		if(locality.length() > 1) /* different from "-" */
 			title += " - " + locality;
 		
		text = getDateTime() + "\n";
 		
 		if(skystr.length() > 0)
 			text += skystr + "\n";
 		
 		if(wind > 0)
 			text += res.getString(R.string.wind) + ": " + windtxtItems[wind] + "\n";
 		
 		try{
 			Float.parseFloat(temperature);
 			text += res.getString(R.string.temp)  + ": " + temperature + "C\n";
 			
 		}
 		catch(NumberFormatException e)
 		{
 			
 		}
 		if(comment.length() > 0)
 			text += res.getString(R.string.reportComment) + ":\n" + comment;
 		
 		if(isWritable())
 			text += "\n*" + res.getString(R.string.reportTouchBaloonToRemove);
 		
 		mMarkerOptions = new MarkerOptions();
 		mMarkerOptions.position(new LatLng(getLatitude(), getLongitude()));
 		mMarkerOptions.title(title);
 		mMarkerOptions.snippet(text);
 
 		if(iconId > -1)
 		{
 			/* for sky no label, so do not use obsBmpFactory */
 			BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(iconId);
 			if(bitmapDescriptor != null)
 			{
 				mMarkerOptions.icon(bitmapDescriptor);
 				Log.e("anchoring ", "anchoring");
 				mMarkerOptions.anchor(0.5f, 0.5f);
 			}
 		}
 		else
 		{
 			windIdx = wind;
 			if(windIdx == 1)
 				iconId = R.drawable.weather_wind_calm;
 			else if(windIdx == 2) /* breeze */
 				iconId = R.drawable.weather_wind_35;
 			else if(windIdx == 3)
 				iconId = R.drawable.weather_wind_17; /* moderato */
 			else if(windIdx == 4)
 				iconId = R.drawable.weather_wind_26; /* moderato */
 			else if(windIdx == 5)
 				iconId = R.drawable.weather_wind2_red_34; /* moderato */
 			
 			if(iconId > -1)
 			{
 				BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(iconId);
 				if(bitmapDescriptor != null)
 					mMarkerOptions.icon(bitmapDescriptor);
 			}
 			else
 				mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
 		}
 		
 		return mMarkerOptions;
 
 	}
 
 	public void setMarker(Marker m)
 	{
 		mMarker = m;
 	}
 	
 	public MarkerOptions getMarkerOptions()
 	{
 		return mMarkerOptions;
 	}
 	
 	@Override
 	public Marker getMarker() 
 	{
 		return mMarker;
 	}
 
 	/** @return true: always return true for reports.
 	 * 
 	 */
 	@Override
 	public boolean isPublished() {
 		return true;
 	}
 
 	
 	
 }
