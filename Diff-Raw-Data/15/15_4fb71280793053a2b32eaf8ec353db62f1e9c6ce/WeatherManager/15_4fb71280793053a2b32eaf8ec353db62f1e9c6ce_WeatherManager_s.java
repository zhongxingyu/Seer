 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.weather;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.gots.garden.GardenInterface;
 import org.gots.garden.sql.GardenDBHelper;
 import org.gots.weather.exception.UnknownWeatherException;
 import org.gots.weather.sql.WeatherDBHelper;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class WeatherManager {
 
 	private WeatherSet ws;
 	private Integer temperatureLimitHot;
 	private Integer temperatureLimitCold;
 	private Integer runningLimit;
 	private Date today;
 	private Context mContext;
 	private static SharedPreferences preferences;
 	private Calendar weatherday;
 
 	public WeatherManager(Context context) {
 		this.mContext = context;
 		update(false);
 
 	}
 
 	public void update(boolean force) {
 		weatherday = new GregorianCalendar();
 
 		GardenDBHelper helper = new GardenDBHelper(mContext);
 		preferences = mContext.getSharedPreferences("org.gots.preference", 0);
 
 		GardenInterface garden = helper.getGarden(preferences.getInt("org.gots.preference.gardenid", 0));
 		getWeather(garden, force);
 
 	}
 
 	private void getWeather(GardenInterface garden, boolean force) {
 		WeatherDBHelper helper = new WeatherDBHelper(mContext);
 		WeatherConditionInterface wc;
 
 		weatherday.setTime(Calendar.getInstance().getTime());
 		wc = helper.getWeatherByDayofyear(weatherday.get(Calendar.DAY_OF_YEAR));
 
 		if (wc == null || force) {
 			today = Calendar.getInstance().getTime();
 
 			try {
 				WeatherTask wt = new WeatherTask(garden.getAddress());
 				ws = wt.execute().get();
 				if (ws == null)
 					return;
 
 				updateCondition(ws.getWeatherCurrentCondition(), 0);
 				updateCondition(ws.getWeatherForecastConditions().get(1), 1);
 				updateCondition(ws.getWeatherForecastConditions().get(2), 2);
 				updateCondition(ws.getWeatherForecastConditions().get(3), 3);
 
 				// weatherday.set(Calendar.DAY_OF_WEEK,
 				// weatherday.get(Calendar.DAY_OF_WEEK) + 1);
 				// ws.getWeatherForecastConditions().get(1).setDate(weatherday.getTime());
 				// ws.getWeatherForecastConditions().get(1).setDayofYear(weatherday.get(Calendar.DAY_OF_YEAR));
 				// helper.insertWeather(ws.getWeatherForecastConditions().get(1));
 				//
 				// weatherday.set(Calendar.DAY_OF_WEEK,
 				// weatherday.get(Calendar.DAY_OF_WEEK) + 1);
 				// ws.getWeatherForecastConditions().get(2).setDayofYear(weatherday.get(Calendar.DAY_OF_YEAR));
 				// ws.getWeatherForecastConditions().get(2).setDate(weatherday.getTime());
 				// helper.insertWeather(ws.getWeatherForecastConditions().get(2));
 				//
 				// weatherday.set(Calendar.DAY_OF_WEEK,
 				// weatherday.get(Calendar.DAY_OF_WEEK) + 1);
 				// ws.getWeatherForecastConditions().get(3).setDayofYear(weatherday.get(Calendar.DAY_OF_YEAR));
 				// ws.getWeatherForecastConditions().get(3).setDate(weatherday.getTime());
 				// helper.insertWeather(ws.getWeatherForecastConditions().get(3));
 			} catch (Exception e) {
 				if (e.getMessage() != null)
 					Log.e("WeatherManager", e.getMessage());
 			}
 		}
 	}
 
 	private void updateCondition(WeatherConditionInterface condition, int day) {
 		WeatherDBHelper helper = new WeatherDBHelper(mContext);
	
		weatherday.add(Calendar.DAY_OF_YEAR, day);
		condition.setDate(weatherday.getTime());
		condition.setDayofYear(weatherday.get(Calendar.DAY_OF_YEAR));
 
		WeatherConditionInterface wc = helper.getWeatherByDayofyear(weatherday.get(Calendar.DAY_OF_YEAR));
 
 		if (wc == null)
 			helper.insertWeather(condition);
 		else
 			helper.updateWeather(condition);
 		return;
 
 	}
 
 	public Integer getTemperatureLimitHot() {
 		return temperatureLimitHot;
 	}
 
 	public void setTemperatureLimitHot(Integer temperatureLimitHot) {
 		this.temperatureLimitHot = temperatureLimitHot;
 	}
 
 	public Integer getTemperatureLimitCold() {
 		return temperatureLimitCold;
 	}
 
 	public void setTemperatureLimitCold(Integer temperatureLimitCold) {
 		this.temperatureLimitCold = temperatureLimitCold;
 	}
 
 	public Integer getRunningLimit() {
 		return runningLimit;
 	}
 
 	public void setRunningLimit(Integer runningLimit) {
 		this.runningLimit = runningLimit;
 	}
 
 	public WeatherConditionInterface getCondition(int i) throws UnknownWeatherException {
 		Calendar weatherday = new GregorianCalendar();
 		WeatherConditionInterface wc;
 
 		weatherday.setTime(Calendar.getInstance().getTime());
 		weatherday.add(Calendar.DAY_OF_YEAR, i);
 
 		WeatherDBHelper helper = new WeatherDBHelper(mContext);
 		wc = helper.getWeatherByDayofyear(weatherday.get(Calendar.DAY_OF_YEAR));
 		if (wc == null)
 			throw new UnknownWeatherException();
 		return wc;
 	}
 
 	public List<WeatherConditionInterface> getConditionSet(int nbDays) {
 		List<WeatherConditionInterface> conditions = new ArrayList<WeatherConditionInterface>();
 		for (int i = -nbDays; i <= nbDays; i++) {
 			try {
 				conditions.add(getCondition(i));
 			} catch (UnknownWeatherException e) {
 				conditions.add(new WeatherCondition());
 				e.printStackTrace();
 			}
 		}
 		return conditions;
 	}
 
 }
