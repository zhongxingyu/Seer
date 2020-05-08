 /**
  * 
  */
 package org.weather.weatherman.content;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.json.simple.JSONValue;
 
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.util.Log;
 
 import com.baidu.mobstat.StatService;
 
 /**
  * 天气信息服务类
  * 
  * @author gengmaozhang01
  * @since 2014-1-3 下午7:05:57
  */
 public class WeatherSupport {
 
 	private static final String tag = WeatherSupport.class.getSimpleName();
 	private static final String api = "http://42.96.143.229:8387/openapi/api/weather";
 
 	private int connectTimeout = 30000;
 	private int readTimeout = 30000;
 	private int retry = 3;
 
 	private DatabaseSupport databaseSupport;
 
 	public WeatherSupport(DatabaseSupport databaseSupport) {
 		super();
 		this.databaseSupport = databaseSupport;
 	}
 
 	/**
 	 * 获取天气实况信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-1-3 下午7:28:04
 	 */
 	private Weather.RealtimeWeather getRealtimeWeather(String citycode) throws Exception {
 		if (citycode == null || citycode.length() == 0) {
 			throw new IllegalArgumentException("citycode is required");
 		}
 		String url = api + "/realtime/" + citycode;
 		for (int i = 0; i < retry; i++) {
 			try {
 				String json = this.request(url);
 				@SuppressWarnings("unchecked")
 				Map<String, Object> value = (Map<String, Object>) JSONValue.parse(json);
 				Object code = value.get("code");
 				if (code == null || !Number.class.isInstance(code) || ((Number) code).intValue() != 0) {
 					Object msg = value.get("message");
 					throw new Exception(msg != null ? msg.toString() : "get realtime weather failed");
 				}
 				StatService.onEvent(databaseSupport.getContext(), "api", "realtime-success", 1);
 				return new Weather.RealtimeWeather(value);
 			} catch (Exception ex) {
 				Log.e(tag, "get realtime weather failed", ex);
 				StatService.onEvent(databaseSupport.getContext(), "api", "realtime-failure", 1);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 获取天气预报信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-1-3 下午7:28:04
 	 */
 	private Weather.ForecastWeather getForecastWeather(String citycode) throws Exception {
 		if (citycode == null || citycode.length() == 0) {
 			throw new IllegalArgumentException("citycode is required");
 		}
 		String url = api + "/forecast/" + citycode;
 		for (int i = 0; i < retry; i++) {
 			try {
 				String json = this.request(url);
 				@SuppressWarnings("unchecked")
 				Map<String, Object> value = (Map<String, Object>) JSONValue.parse(json);
 				Object code = value.get("code");
 				if (code == null || !Number.class.isInstance(code) || ((Number) code).intValue() != 0) {
 					Object msg = value.get("message");
					throw new Exception(msg != null ? msg.toString() : "get realtime weather failed");
 				}
 				StatService.onEvent(databaseSupport.getContext(), "api", "forecast-success", 1);
 				return new Weather.ForecastWeather(value);
 			} catch (Exception ex) {
				Log.e(tag, "get realtime weather failed", ex);
 				StatService.onEvent(databaseSupport.getContext(), "api", "forecast-failure", 1);
 			}
 		}
 		return null;
 	}
 
 	private String request(String url) throws Exception {
 		String json = null;
 		HttpURLConnection conn = null;
 		InputStream ins = null;
 		try {
 			conn = (HttpURLConnection) new URL(url).openConnection();
 			conn.setRequestProperty("User-Agent", "WeatherMan/1.7 Android");
 			conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
 			if (connectTimeout > 0) {
 				conn.setConnectTimeout(connectTimeout);
 			}
 			if (readTimeout > 0) {
 				conn.setReadTimeout(readTimeout);
 			}
 			// read
 			conn.connect();
 			ins = conn.getInputStream();
 			ByteArrayOutputStream ous = new ByteArrayOutputStream();
 			byte[] b = new byte[1024];
 			int len = 0;
 			while ((len = ins.read(b)) > -1) {
 				ous.write(b, 0, len);
 			}
 			json = ous.toString();
 			ous.close();
 		} finally {
 			if (ins != null) {
 				ins.close();
 			}
 			if (conn != null) {
 				conn.disconnect();
 			}
 		}
 		return json;
 	}
 
 	/**
 	 * 获取天气实况信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-1-3 下午7:53:19
 	 */
 	public Cursor findRealtimeWeather(String citycode) {
 		Log.i(tag, "city is " + citycode);
 		MatrixCursor result = new MatrixCursor(new String[] { Weather.RealtimeWeather.ID, Weather.RealtimeWeather.NAME,
 				Weather.RealtimeWeather.TIME, Weather.RealtimeWeather.TEMPERATURE, Weather.RealtimeWeather.HUMIDITY,
 				Weather.RealtimeWeather.WINDDIRECTION, Weather.RealtimeWeather.WINDFORCE });
 		// database
 		String value = null;
 		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
 				new Object[] { Weather.RealtimeWeather.TYPE, citycode });
 		if (cursor.moveToFirst()) {
 			value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
 			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
 			if (this.isNotOvertime(uptime)) {
 				try {
 					result.addRow(value.split(";"));
 					Log.i(tag, "get realtime weather from database");
 					return result;
 				} catch (Exception ex) {
 					Log.e(tag, "parse realtime weather failed", ex);
 				} finally {
 					cursor.close();
 				}
 			}
 		}
 		cursor.close();
 		// web
 		Weather.RealtimeWeather realtime = null;
 		try {
 			realtime = this.getRealtimeWeather(citycode);
 		} catch (Exception e) {
 			Log.e(tag, "get realtime weather failed", e);
 		}
 		if (realtime != null) {
 			result.addRow(new Object[] { realtime.getCityId(), realtime.getCityName(), realtime.getTime(),
 					realtime.getTemperature(), realtime.getHumidity(), realtime.getWindDirection(),
 					realtime.getWindForce() });
 			databaseSupport.saveRealtimeWeather(realtime);
 			Log.i(tag, "refresh realtime weather by api");
 		} else if (value != null) { // 网络异常使用旧的实况信息
 			try {
 				result.addRow(value.split(";"));
 				Log.i(tag, "using old realtime weather");
 			} catch (Exception ex) {
 				Log.e(tag, "parse realtime weather failed", ex);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 获取天气预报信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-1-3 下午7:53:35
 	 */
 	public Cursor findForecastWeather(String citycode) {
 		Log.i(tag, "city is " + citycode);
 		MatrixCursor result = new MatrixCursor(new String[] { Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
 				Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER, Weather.ForecastWeather.TEMPERATURE,
 				Weather.ForecastWeather.IMAGE, Weather.ForecastWeather.WIND, Weather.ForecastWeather.WINDFORCE });
 		// query database
 		String value = null;
 		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
 				new Object[] { Weather.ForecastWeather.TYPE, citycode });
 		if (cursor.moveToFirst()) {
 			value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
 			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
 			if (this.isNotOvertime(uptime)) {
 				try {
 					String[] rows = value.split("#");
 					for (String row : rows) {
 						result.addRow(row.split(";"));
 					}
 					Log.i(tag, "get forecast weather from database");
 					return result;
 				} catch (Exception ex) {
 					result.close();
 					result = new MatrixCursor(new String[] { Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
 							Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER,
 							Weather.ForecastWeather.TEMPERATURE, Weather.ForecastWeather.IMAGE,
 							Weather.ForecastWeather.WIND, Weather.ForecastWeather.WINDFORCE });
 					Log.e(tag, "parse forcast weather failed", ex);
 				} finally {
 					cursor.close();
 				}
 			}
 		}
 		cursor.close();
 		// query web server
 		Weather.ForecastWeather forecast = null;
 		try {
 			forecast = this.getForecastWeather(citycode);
 		} catch (Exception e) {
 			Log.e(tag, "get forecast weather failed", e);
 		}
 		if (forecast != null) {
 			List<String> wl = forecast.getWeather(), tl = forecast.getTemperature(), il = forecast.getImage(), wdl = forecast
 					.getWind(), wfl = forecast.getWindForce();
 			int length = Math
 					.min(wl.size(), Math.min(tl.size(), Math.min(il.size(), Math.min(wdl.size(), wfl.size()))));
 			for (int i = 0; i < length; i++) {
 				result.addRow(new Object[] { forecast.getCityId(), forecast.getCityName(), forecast.getTime(),
 						wl.size() > i ? wl.get(i) : null, tl.size() > i ? tl.get(i) : null,
 						il.size() > i ? il.get(i) : null, wdl.size() > i ? wdl.get(i) : null,
 						wfl.size() > i ? wfl.get(i) : null });
 			}
 			databaseSupport.saveForecastAndIndexWeather(forecast);
 			Log.i(tag, "refresh forecast weather by api");
 		} else if (value != null && value.length() > 0) { // 网络异常使用旧的预报信息
 			try {
 				String[] rows = value.split("#");
 				for (String row : rows) {
 					result.addRow(row.split(";"));
 				}
 				Log.i(tag, "using old forecast weather");
 			} catch (Exception ex) {
 				Log.e(tag, "parse forcast weather failed", ex);
 				result.close();
 				result = new MatrixCursor(new String[] { Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
 						Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER,
 						Weather.ForecastWeather.TEMPERATURE, Weather.ForecastWeather.IMAGE,
 						Weather.ForecastWeather.WIND, Weather.ForecastWeather.WINDFORCE });
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 获取天气指数信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-1-3 下午7:57:48
 	 */
 	public Cursor findIndexWeather(String citycode) {
 		Log.i(tag, "city is " + citycode);
 		MatrixCursor result = new MatrixCursor(new String[] { Weather.LivingIndex.ID, Weather.LivingIndex.NAME,
 				Weather.LivingIndex.TIME, Weather.LivingIndex.DRESS, Weather.LivingIndex.ULTRAVIOLET,
 				Weather.LivingIndex.CLEANCAR, Weather.LivingIndex.TRAVEL, Weather.LivingIndex.COMFORT,
 				Weather.LivingIndex.MORNINGEXERCISE, Weather.LivingIndex.SUNDRY, Weather.LivingIndex.IRRITABILITY });
 		// database
 		String value = null;
 		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
 				new Object[] { Weather.LivingIndex.TYPE, citycode });
 		if (cursor.moveToFirst()) {
 			value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
 			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
 			if (this.isNotOvertime(uptime)) {
 				try {
 					result.addRow(value.split(";"));
 					Log.i(tag, "get living index from database");
 					return result;
 				} catch (Exception ex) {
 					Log.e(tag, "parse living index failed", ex);
 				} finally {
 					cursor.close();
 				}
 			}
 		}
 		cursor.close();
 		// web
 		Weather.ForecastWeather forecast = null;
 		try {
 			forecast = this.getForecastWeather(citycode);
 		} catch (Exception e) {
 			Log.e(tag, "get living index failed", e);
 		}
 		if (forecast != null) {
 			List<Object> row = new ArrayList<Object>();
 			Collections.addAll(row, forecast.getCityId(), forecast.getCityName(), forecast.getTime());
 			Weather.LivingIndex li = forecast.getDressIndex();
 			row.add(li != null ? li.getIndex() : null);
 			li = forecast.getUltravioletIndex();
 			row.add(li != null ? li.getIndex() : null);
 			li = forecast.getCleanCarIndex();
 			row.add(li != null ? li.getIndex() : null);
 			li = forecast.getTravelIndex();
 			row.add(li != null ? li.getIndex() : null);
 			li = forecast.getComfortIndex();
 			row.add(li != null ? li.getIndex() : null);
 			li = forecast.getMorningExerciseIndex();
 			row.add(li != null ? li.getIndex() : null);
 			li = forecast.getSunDryIndex();
 			row.add(li != null ? li.getIndex() : null);
 			li = forecast.getIrritabilityIndex();
 			row.add(li != null ? li.getIndex() : null);
 			result.addRow(row);
 			databaseSupport.saveForecastAndIndexWeather(forecast);
 			Log.i(tag, "refresh living index by api");
 		} else if (value != null && value.length() > 0) { // 网络异常使用旧的指数信息
 			try {
 				result.addRow(value.split(";"));
 				Log.i(tag, "using old living index");
 			} catch (Exception ex) {
 				Log.e(tag, "using old living index failed", ex);
 			}
 		}
 		return result;
 	}
 
 	private boolean isNotOvertime(String uptime) {
 		if (System.currentTimeMillis() - Long.parseLong(uptime) > 10 * 60 * 1000) {
 			return false;
 		}
 		return true;
 	}
 
 }
