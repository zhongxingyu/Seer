 /*
  * @author     kebin, ucchy
  * @license    LGPLv3
  * @copyright  Copyright kebin, ucchy 2013
  */
 package com.github.ucchyocean.cw;
 
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 /**
  * Current Weather のコンフィグクラス
  * @author ucchy
  */
 public class CurrentWeatherConfig {
 
     /** 設定地域とMinecraft内の時刻を同期するかどうか。
      * 同期すると、Minecraft内の時間周期が1日24時間になります。 */
     private boolean syncTime;
     
     /** 天気同期や時刻同期を行わないワールド */
     private List<String> ignoreWorlds;
     
     /** 天気同期を実行するインターバル */
    private int intervalTicksSyncWeater;
     
     /** 時刻同期を実行するインターバル */
     private int intervalTicksSyncTime;
     
     /** 現在同期している都市のID */
     private int city;
     
     /**
      * config.ymlの読み出し処理。
      * @return 読み込んだ CurrentWeatherConfig オブジェクト
      */
     public static CurrentWeatherConfig loadConfig() {
         
         CurrentWeatherConfig config = new CurrentWeatherConfig();
 
         // config.yml が無い場合に、デフォルトを読み出し
         CurrentWeather.instance.saveDefaultConfig();
 
         // config取得
         CurrentWeather.instance.reloadConfig();
         FileConfiguration conf = CurrentWeather.instance.getConfig();
 
         config.syncTime = conf.getBoolean("syncTime", true);
         config.ignoreWorlds = conf.getStringList("ignoreWorlds");
        config.intervalTicksSyncWeater = conf.getInt("intervalTicksSyncWeater", 24000);
         config.intervalTicksSyncTime = conf.getInt("intervalTicksSyncTime", 60);
         config.city = conf.getInt("city", -1);
         
         return config;
     }
     
     /**
      * config.ymlへの書き出し処理。
      */
     public void saveConfig() {
         
         FileConfiguration conf = CurrentWeather.instance.getConfig();
         
         conf.set("syncTime", syncTime);
         conf.set("ignoreWorlds", ignoreWorlds);
        conf.set("intervalTicksSyncWeater", intervalTicksSyncWeater);
         conf.set("intervalTicksSyncTime", intervalTicksSyncTime);
         conf.set("city", city);
         
         CurrentWeather.instance.saveConfig();
     }
 
     /**
      * @return syncTime
      */
     public boolean isSyncTime() {
         return syncTime;
     }
 
     /**
      * @return ignoreWorlds
      */
     public List<String> getIgnoreWorlds() {
         return ignoreWorlds;
     }
 
     /**
     * @return intervalTicksSyncWeater
      */
    public int getIntervalTicksSyncWeater() {
        return intervalTicksSyncWeater;
     }
 
     /**
      * @return intervalTicksSyncTime
      */
     public int getIntervalTicksSyncTime() {
         return intervalTicksSyncTime;
     }
 
     /**
      * @return city
      */
     public int getCity() {
         return city;
     }
 
     /**
      * @param city 
      */
     public void setCity(int city) {
         this.city = city;
     }
 }
