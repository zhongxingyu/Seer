 package org.weather.weatherman.content;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.weather.weatherman.R;
 
 import android.provider.BaseColumns;
 
 public final class Weather {
 
 	private Weather() {
 	}
 
 	/**
 	 * 城市等设置信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-2-24 下午10:57:18
 	 */
 	public static final class Setting implements BaseColumns {
 
 		public static final String PATH_SETTING = "setting";
 		public static final int TYPE = 21;
 
 		private Map<String, String> value;
 
 		public Setting(Map<String, Object> value) {
 			this.value = new HashMap<String, String>();
 			if (value != null) {
 				for (String key : value.keySet()) {
 					this.value.put(key, String.valueOf(value.get(key)));
 				}
 			}
 		}
 
 		protected Map<String, String> getValue() {
 			return value;
 		}
 
 		/**
 		 * 获得省份
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午10:45:46
 		 */
 		public cn.seddat.weatherman.api.city.City getProvince() {
 			String id = value.get("c1c"), name = value.get("c1n");
 			return new cn.seddat.weatherman.api.city.City(id, name);
 		}
 
 		/**
 		 * 获得地市
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午10:46:01
 		 */
 		public cn.seddat.weatherman.api.city.City getCity() {
 			String id = value.get("c2c"), name = value.get("c2n");
 			return new cn.seddat.weatherman.api.city.City(id, name);
 		}
 
 		/**
 		 * 获得区县
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午10:46:14
 		 */
 		public cn.seddat.weatherman.api.city.City getDistrict() {
 			String id = value.get("c3c"), name = value.get("c3n");
 			return new cn.seddat.weatherman.api.city.City(id, name);
 		}
 
 	}
 
 	/**
 	 * 实时天气信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-1-3 下午6:55:14
 	 */
 	public static final class RealtimeWeather implements BaseColumns {
 
 		public static final String PATH_REALTIME = "realtime";
 		public static final int TYPE = 22;
 
 		private Map<String, Object> value;
 
 		@SuppressWarnings("unchecked")
 		public RealtimeWeather(Map<String, Object> value) {
 			this.value = new HashMap<String, Object>();
 			if (value != null) {
 				Object obj = value.get("weatherinfo");
 				if (obj != null && (obj instanceof Map)) {
 					value = (Map<String, Object>) obj;
 				}
 				this.value.putAll(value);
 			}
 		}
 
 		protected Map<String, Object> getValue() {
 			return value;
 		}
 
 		public String getTime() {
 			return (String) value.get("time");
 		}
 
 		public String getCityId() {
 			return (String) value.get("cityid");
 		}
 
 		public String getCityName() {
 			return (String) value.get("city");
 		}
 
 		public String getTemperature() {
 			String val = (String) value.get("temp");
 			return (val != null ? (val.endsWith("℃") ? val : val + "℃") : "");
 		}
 
 		public String getHumidity() {
 			return (String) value.get("SD");
 		}
 
 		public String getWindDirection() {
 			return (String) value.get("WD");
 		}
 
 		public String getWindForce() {
 			return (String) value.get("WS");
 		}
 
 		public int getWindForceValue() {
 			String wf = (String) value.get("WSE");
 			return wf != null && wf.length() > 0 ? Integer.valueOf(wf) : 0;
 		}
 
 		public boolean hasRadar() {
 			return "1".equals(value.get("isRadar"));
 		}
 
 		public String getRadar() {
 			return (String) value.get("Radar");
 		}
 
 		@SuppressWarnings("unchecked")
 		private List<Map<String, String>> getIndexes() {
 			return (List<Map<String, String>>) value.get("indexes");
 		}
 
 		/**
 		 * 获取生活指数个数
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午11:05:06
 		 */
 		public int getIndexSize() {
 			List<Map<String, String>> indexes = this.getIndexes();
 			return indexes != null ? indexes.size() : 0;
 		}
 
 		/**
 		 * 获取生活指数名称
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午11:16:03
 		 */
 		public String getIndexName(int i) {
 			String key = null;
 			List<Map<String, String>> indexes = this.getIndexes();
 			if (indexes != null && i < indexes.size()) {
 				Map<String, String> index = indexes.get(i);
 				key = index.get("name");
 			}
 			if (key == null || key.length() == 0) {
 				return null;
 			}
 			Map<String, String> names = new HashMap<String, String>();
 			names.put("ct", "穿衣指数");
 			names.put("tr", "旅游指数");
 			names.put("yd", "运动指数");
 			names.put("xc", "洗车指数");
 			names.put("pp", "化妆指数");
 			names.put("gm", "感冒指数");
			names.put("uv", "紫外线指数");
			names.put("co", "舒适度指数");
 			names.put("ag", "过敏指数");
 			names.put("gj", "逛街指数");
 			names.put("mf", "美发指数");
 			names.put("ys", "雨伞指数");
 			names.put("jt", "交通指数");
 			names.put("lk", "路况指数");
 			names.put("cl", "晨练指数");
 			names.put("dy", "钓鱼指数");
 			names.put("hc", "划船指数");
 			names.put("yh", "约会指数");
 			names.put("ls", "晾晒指数");
 			names.put("fs", "防晒指数");
 			return names.containsKey(key) ? names.get(key) : key;
 		}
 
 		/**
 		 * 获取生活指数等级值
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午11:17:12
 		 */
 		public String getIndexValue(int i) {
 			List<Map<String, String>> indexes = this.getIndexes();
 			if (indexes != null && i < indexes.size()) {
 				Map<String, String> index = indexes.get(i);
 				return index.get("value");
 			}
 			return null;
 		}
 
 		/**
 		 * 获取生活指数描述
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午11:17:42
 		 */
 		public String getIndexDesc(int i) {
 			List<Map<String, String>> indexes = this.getIndexes();
 			if (indexes != null && i < indexes.size()) {
 				Map<String, String> index = indexes.get(i);
 				return index.get("desc");
 			}
 			return null;
 		}
 
 		@Override
 		public String toString() {
 			return value.toString();
 		}
 
 	}
 
 	/**
 	 * 天气预报信息
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-2-24 下午11:18:16
 	 */
 	public static final class ForecastWeather implements BaseColumns {
 
 		public static final String PATH_FORECAST = "forecast";
 		public static final int TYPE = 23;
 
 		private Map<String, Object> value;
 
 		@SuppressWarnings("unchecked")
 		public ForecastWeather(Map<String, Object> value) {
 			this.value = new HashMap<String, Object>();
 			if (value != null) {
 				Object obj = value.get("weatherinfo");
 				if (obj != null && (obj instanceof Map)) {
 					value = (Map<String, Object>) obj;
 				}
 				this.value.putAll(value);
 			}
 		}
 
 		protected Map<String, Object> getValue() {
 			return value;
 		}
 
 		public String getTime() {
 			return (String) value.get("time");
 		}
 
 		public String getCityId() {
 			return (String) value.get("cityid");
 		}
 
 		@SuppressWarnings("unchecked")
 		private List<Map<String, String>> getForecast() {
 			return (List<Map<String, String>>) value.get("forecast");
 		}
 
 		/**
 		 * 获取预报信息的条数
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午11:24:43
 		 */
 		public int getForecastSize() {
 			List<Map<String, String>> forecast = this.getForecast();
 			return forecast != null ? forecast.size() : 0;
 		}
 
 		/**
 		 * 获取预报信息的时间，返回“2014.02.24 白天/夜间”格式
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-24 下午11:25:46
 		 */
 		public String getForecastTime(int index) {
 			List<Map<String, String>> forecast = this.getForecast();
 			if (forecast != null && index < forecast.size()) {
 				Map<String, String> weather = forecast.get(index);
 				return weather.get("time") + " " + weather.get("half");
 			}
 			return null;
 		}
 
 		public String getForecastImage(int index) {
 			List<Map<String, String>> forecast = this.getForecast();
 			if (forecast != null && index < forecast.size()) {
 				Map<String, String> weather = forecast.get(index);
 				return weather.get("image");
 			}
 			return null;
 		}
 
 		public String getForecastWeather(int index) {
 			List<Map<String, String>> forecast = this.getForecast();
 			if (forecast != null && index < forecast.size()) {
 				Map<String, String> weather = forecast.get(index);
 				return weather.get("weather");
 			}
 			return null;
 		}
 
 		public String getForecastTemperature(int index) {
 			List<Map<String, String>> forecast = this.getForecast();
 			if (forecast != null && index < forecast.size()) {
 				Map<String, String> weather = forecast.get(index);
 				return weather.get("temp");
 			}
 			return null;
 		}
 
 		public String getForecastWind(int index) {
 			List<Map<String, String>> forecast = this.getForecast();
 			if (forecast != null && index < forecast.size()) {
 				Map<String, String> weather = forecast.get(index);
 				return weather.get("wd");
 			}
 			return null;
 		}
 
 		public String getForecastWindForce(int index) {
 			List<Map<String, String>> forecast = this.getForecast();
 			if (forecast != null && index < forecast.size()) {
 				Map<String, String> weather = forecast.get(index);
 				return weather.get("ws");
 			}
 			return null;
 		}
 
 		@Override
 		public String toString() {
 			return value.toString();
 		}
 
 	}
 
 	/**
 	 * 空气质量指数
 	 * 
 	 * @author gengmaozhang01
 	 * @since 2014-2-24 下午11:31:17
 	 */
 	public static final class AirQualityIndex implements BaseColumns {
 
 		public static final String PATH_AQI = "aqi";
 		public static final int TYPE = 24;
 
 		/**
 		 * 获取AQI所属等级名称
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-10 下午10:46:01
 		 */
 		public static String getAQITitle(int value) {
 			String text = null;
 			if (value <= 50) {
 				text = "优";
 			} else if (value <= 100) {
 				text = "良";
 			} else if (value <= 150) {
 				text = "轻度污染";
 			} else if (value <= 200) {
 				text = "中度污染";
 			} else if (value <= 300) {
 				text = "重度污染";
 			} else {
 				text = "严重污染";
 			}
 			return text;
 		}
 
 		/**
 		 * 获取AQI所属等级的颜色ID
 		 * 
 		 * @author gengmaozhang01
 		 * @since 2014-2-10 下午10:46:35
 		 */
 		public static int getAQIColor(int value) {
 			int color = R.color.AQI_perfect;
 			if (value <= 50) {
 				color = R.color.AQI_perfect;
 			} else if (value <= 100) {
 				color = R.color.AQI_fine;
 			} else if (value <= 150) {
 				color = R.color.AQI_smell_little;
 			} else if (value <= 200) {
 				color = R.color.AQI_smell_middle;
 			} else if (value <= 300) {
 				color = R.color.AQI_smell_heavy;
 			} else {
 				color = R.color.AQI_smell_fatal;
 			}
 			return color;
 		}
 
 		private Map<String, Object> value;
 
 		@SuppressWarnings("unchecked")
 		public AirQualityIndex(Map<String, Object> value) {
 			this.value = new HashMap<String, Object>();
 			if (value != null) {
 				Object obj = value.get("weatherinfo");
 				if (obj != null && (obj instanceof Map)) {
 					value = (Map<String, Object>) obj;
 				}
 				for (String key : value.keySet()) {
 					this.value.put(key, value.get(key));
 				}
 			}
 		}
 
 		protected Map<String, Object> getValue() {
 			return value;
 		}
 
 		public String getTime() {
 			return (String) value.get("time");
 		}
 
 		public String getCityId() {
 			return (String) value.get("cityid");
 		}
 
 		public String getCityName() {
 			return (String) value.get("city");
 		}
 
 		public String getAQICity() {
 			return (String) value.get("AQI_city");
 		}
 
 		public int getCurrentAQI() {
 			Object obj = value.get("AQI");
 			return obj == null ? -1 : Integer.parseInt(obj.toString());
 		}
 
 		public int getHourlySize() {
 			List<Map<String, String>> hourly = getHourly();
 			return hourly != null ? hourly.size() : 0;
 		}
 
 		@SuppressWarnings("unchecked")
 		private List<Map<String, String>> getHourly() {
 			return (List<Map<String, String>>) value.get("hourly");
 		}
 
 		public String getHourlyTime(int index) {
 			List<Map<String, String>> hourly = this.getHourly();
 			if (index >= 0 && index < hourly.size()) {
 				return hourly.get(index).get("time");
 			}
 			return null;
 		}
 
 		public int getHourlyAQI(int index) {
 			List<Map<String, String>> hourly = this.getHourly();
 			if (index >= 0 && index < hourly.size()) {
 				String aqi = hourly.get(index).get("AQI");
 				return aqi != null ? Integer.parseInt(aqi) : -1;
 			}
 			return -1;
 		}
 
 		public int getDailySize() {
 			List<Map<String, String>> daily = getDaily();
 			return daily != null ? daily.size() : 0;
 		}
 
 		@SuppressWarnings("unchecked")
 		private List<Map<String, String>> getDaily() {
 			List<Map<String, String>> daily = (List<Map<String, String>>) value.get("daily");
 			return daily;
 		}
 
 		public String getDailyTime(int index) {
 			List<Map<String, String>> daily = getDaily();
 			if (index >= 0 && index < daily.size()) {
 				return daily.get(index).get("time");
 			}
 			return null;
 		}
 
 		public int getDailyAQI(int index) {
 			List<Map<String, String>> daily = getDaily();
 			if (index >= 0 && index < daily.size()) {
 				String aqi = daily.get(index).get("AQI");
 				return aqi != null ? Integer.parseInt(aqi) : -1;
 			}
 			return -1;
 		}
 
 		public long getTimestamp() {
 			Object obj = value.get("timestamp");
 			return obj == null ? 0 : Long.parseLong(obj.toString());
 		}
 
 		@Override
 		public String toString() {
 			return AirQualityIndex.class.getSimpleName() + "{value:" + value.toString() + "}";
 		}
 
 	}
 
 }
