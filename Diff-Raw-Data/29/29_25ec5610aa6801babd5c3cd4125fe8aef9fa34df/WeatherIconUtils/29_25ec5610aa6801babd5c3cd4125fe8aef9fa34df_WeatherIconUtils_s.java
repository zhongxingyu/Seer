 package org.risney.adf.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class WeatherIconUtils {
     private static final Logger logger = Logger.getLogger(WeatherIconUtils.class.getName());
     private static final String CLOUDY_ICON_PREFIX = "cloudy";
     private static final String FOG_ICON_PREFIX = "fog";
     private static final String HAIL_ICON_PREFIX = "hail";
     private static final String LIGHTNING_ICON_PREFIX = "lightning";
     private static final String LIGHT_RAIN_ICON_PREFIX = "light-rain";
     private static final String LIGHT_SHOWERS_ICON_PREFIX = " light-showers";
     private static final String LIGHT_SNOW_ICON_PREFIX = "light-snow";
     private static final String OVERCAST_ICON_PREFIX = "overcast";
     private static final String RAIN_ICON_PREFIX = "rain";
     private static final String SHOWERS_ICON_PREFIX = "showers";
     private static final String SNOW_ICON_PREFIX = "snow";
     private static final String SUNNY_ICON_PREFIX = "sunny";
     private static final String SUNNY_INTERVAL_ICON_PREFIX = "sunny-interval";
     private static final String SUNNY_PERIOD_ICON_PREFIX = "sunny-period";
     private static final String THUNDER_ICON_PREFIX = "thunder";
     private static final String THUNDERSTORMS_ICON_PREFIX = "thunderstorms";
 
     private static final String CLOUD_NULL = "n/a";
     private static final String CLOUD_SKC = "clear sky";
     private static final String CLOUD_FEW = "few clouds";
     private static final String CLOUD_SCT = "scattered clouds";
     private static final String CLOUD_BKN = "broken clouds";
     private static final String CLOUD_OVC = "overcast";
     private static final String CLOUD_CAVOK = "clouds and visibility OK";
     private static final String CLOUD_NCD = "no clouds detected";
     private static final String CLOUD_NSC = "nil significant cloud";
     private static final String CLOUD_VV = "vertical visibility";
 
     private static final String CONDITION_NULL = "n/a";
     private static final String CONDITION_DZ = "drizzle";
     private static final String CONDITION_RA = "rain";
     private static final String CONDITION_SN = "snow";
     private static final String CONDITION_SG = "snow grains";
     private static final String CONDITION_IC = "ice crystals";
     private static final String CONDITION_PL = "ice pellets";
     private static final String CONDITION_GR = "hail";
 
     private static final String CONDITION_GS = "small hail/snow pellets";
     private static final String CONDITION_UP = "unknown precipitation";
     private static final String CONDITION_BR = "mist";
     private static final String CONDITION_FG = "fog";
     private static final String CONDITION_FU = "smoke";
     private static final String CONDITION_VA = "volcanic ash";
     private static final String CONDITION_SA = "sand";
     private static final String CONDITION_HZ = "haze";
     private static final String CONDITION_SPRAY = "spray";
     private static final String CONDITION_DU = "widespread dust";
     private static final String CONDITION_SQ = "squall";
     private static final String CONDITION_SS = "sandstorm";
     private static final String CONDITION_DS = "duststorm";
     private static final String CONDITION_PO = "well developed dust/sand whirls";
     private static final String CONDITION_FC = "funnel cloud";
     private static final String CONDITION_FC_PLUS = "tornado/waterspout";
 
     private Map<String, String> conditionIconMap;
 
     public WeatherIconUtils() {
         super();
         init();
         logger.setLevel(Level.ALL);
     }
 
     private void init() {
         conditionIconMap = new HashMap<String, String>();
         conditionIconMap.put(CLOUD_SKC, SUNNY_ICON_PREFIX);
         conditionIconMap.put(CLOUD_FEW, SUNNY_PERIOD_ICON_PREFIX);
         conditionIconMap.put(CLOUD_SCT, SUNNY_INTERVAL_ICON_PREFIX);
         conditionIconMap.put(CLOUD_BKN, SUNNY_INTERVAL_ICON_PREFIX);
         conditionIconMap.put(CLOUD_OVC, OVERCAST_ICON_PREFIX);
         conditionIconMap.put(CLOUD_CAVOK, SUNNY_ICON_PREFIX);
         conditionIconMap.put(CLOUD_NCD, SUNNY_ICON_PREFIX);
         conditionIconMap.put(CLOUD_NSC, CLOUDY_ICON_PREFIX);
         conditionIconMap.put(CLOUD_VV, CLOUDY_ICON_PREFIX);
 
         conditionIconMap.put(CONDITION_DZ, LIGHT_SHOWERS_ICON_PREFIX);
         conditionIconMap.put(CONDITION_RA, RAIN_ICON_PREFIX);
         conditionIconMap.put(CONDITION_SN, SNOW_ICON_PREFIX);
         conditionIconMap.put(CONDITION_SG, SNOW_ICON_PREFIX);
         conditionIconMap.put(CONDITION_IC, LIGHT_SNOW_ICON_PREFIX);
         conditionIconMap.put(CONDITION_PL, LIGHT_SNOW_ICON_PREFIX);
         conditionIconMap.put(CONDITION_GR, HAIL_ICON_PREFIX);
 
         conditionIconMap.put(CONDITION_GS, HAIL_ICON_PREFIX);
         conditionIconMap.put(CONDITION_UP, LIGHT_RAIN_ICON_PREFIX);
         conditionIconMap.put(CONDITION_BR, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_FG, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_FU, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_VA, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_SA, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_HZ, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_SPRAY, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_DU, FOG_ICON_PREFIX);
         conditionIconMap.put(CONDITION_SQ, THUNDERSTORMS_ICON_PREFIX);
         conditionIconMap.put(CONDITION_SS, THUNDERSTORMS_ICON_PREFIX);
         conditionIconMap.put(CONDITION_DS, THUNDERSTORMS_ICON_PREFIX);
         conditionIconMap.put(CONDITION_PO, THUNDERSTORMS_ICON_PREFIX);
         conditionIconMap.put(CONDITION_FC, THUNDERSTORMS_ICON_PREFIX);
         conditionIconMap.put(CONDITION_FC_PLUS, THUNDERSTORMS_ICON_PREFIX);
     }
 
     public String getImagePath(String cloud, String condition) {
     
        logger.info("clouds :" + cloud);
        logger.info("condition :" + condition);
 
         String imagePrefix = new String();
         if (null != cloud) {
             if (!cloud.equalsIgnoreCase("n/a")) {
                 if (null != conditionIconMap.get(cloud.trim())) {
                     imagePrefix = conditionIconMap.get(cloud.trim());
                 }
             }
         }
         if (null != condition) {
             if (!condition.equalsIgnoreCase("n/a")) {
                 if (null != conditionIconMap.get(condition.trim())) {
                     imagePrefix = conditionIconMap.get(condition.trim());
                 }
             }
         }
 
        logger.info("prefix = " + imagePrefix);
        return imagePrefix + ".png";
     }
 }
