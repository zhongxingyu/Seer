 /*******************************************************************************
  * Copyright (c) 2011 epyx SA.
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License along
  * with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package ch.windmobile.server.jdc2;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.MutableDateTime;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.windmobile.server.datasourcemodel.DataSourceException;
 import ch.windmobile.server.datasourcemodel.LinearRegression;
 import ch.windmobile.server.datasourcemodel.WindMobileDataSource;
 import ch.windmobile.server.datasourcemodel.xml.Chart;
 import ch.windmobile.server.datasourcemodel.xml.Point;
 import ch.windmobile.server.datasourcemodel.xml.Serie;
 import ch.windmobile.server.datasourcemodel.xml.StationData;
 import ch.windmobile.server.datasourcemodel.xml.StationInfo;
 import ch.windmobile.server.datasourcemodel.xml.StationLocationType;
 import ch.windmobile.server.datasourcemodel.xml.StationUpdateTime;
 import ch.windmobile.server.datasourcemodel.xml.Status;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 
 public class JdcDataSource implements WindMobileDataSource {
     protected final Logger log = LoggerFactory.getLogger(getClass());
 
    protected static final String OFFLINE = "offline";
     protected static final String MAINTENANCE = "maintenance";
     protected static final String TEST = "test";
    protected static final String ONLINE = "online";
 
     // 1 hour by default
     private int historicDuration = 60 * 60;
     private int windTrendScale = 500000;
 
     static enum DataTypeConstant {
         windDirection("wind-direction", "windDirection"), windAverage("wind-average", "windAverage"), windMax("wind-maximum", "windMax"), airTemperature(
             "temperature", "airTemperature"), airHumidity("humidity", "airHumidity");
 
         private final String jsonKey;
         private final String name;
 
         private DataTypeConstant(String jsonKey, String name) {
             this.jsonKey = jsonKey;
             this.name = name;
         }
 
         public String getJsonKey() {
             return jsonKey;
         }
 
         public String getName() {
             return name;
         }
     }
 
     private Mongo mongoService;
     private DB database;
 
     public JdcDataSource() {
         try {
             mongoService = new Mongo();
         } catch (UnknownHostException e) {
         }
         database = mongoService.getDB("windmobile");
     }
 
     private String getStationsCollectionName() {
         return "jdc_stations";
     }
 
     private String getDataCollectionName(String stationId) {
         return "jdc_values_" + stationId;
     }
 
     private DateTime getLastUpdateDateTime(String stationId) {
         DBCollection stations = database.getCollection(getStationsCollectionName());
         DBObject stationJson = stations.findOne(BasicDBObjectBuilder.start("_id", stationId).get());
         BasicDBObject lastDataJson = (BasicDBObject) ((BasicDBList) stationJson.get("last-measurements")).get(0);
 
         return ISODateTimeFormat.dateTimeNoMillis().parseDateTime(lastDataJson.getString("date"));
     }
 
     private List<DBObject> getHistoricData(DBCollection collection, DateTime lastUpdate, int duration) {
         long startTime = lastUpdate.getMillis() - duration * 1000;
         DBObject query = BasicDBObjectBuilder.start("_id", BasicDBObjectBuilder.start("$gte", startTime / 1000).get()).get();
 
         List<DBObject> datas = new ArrayList<DBObject>();
         DBCursor cursor = collection.find(query);
         while (cursor.hasNext()) {
             datas.add(cursor.next());
 
         }
         return datas;
     }
 
     @Override
     public StationUpdateTime getLastUpdate(String stationId) throws DataSourceException {
         try {
             StationUpdateTime returnObject = new StationUpdateTime();
             returnObject.setLastUpdate(getLastUpdateDateTime(stationId));
 
             return returnObject;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         }
     }
 
     // Replaced by getExpirationDate()
     @Deprecated
     private int getDataValidity(DateTime now) {
         if (isSummerFrequency(now)) {
             // Summer frequency
             return 20 * 60;
         } else {
             // Winter frequency
             return 60 * 60;
         }
     }
 
     static protected boolean isSummerFrequency(DateTime date) {
         return (date.getMonthOfYear() >= 4) && (date.getMonthOfYear() <= 9);
     }
 
     static protected DateTime getExpirationDate(DateTime now, DateTime lastUpdate) {
         MutableDateTime expirationDate = new MutableDateTime(lastUpdate.getZone());
 
         if (isSummerFrequency(now)) {
             expirationDate.setMillis(lastUpdate.getMillis() + 20 * 60 * 1000);
             if (expirationDate.getHourOfDay() >= 20) {
                 expirationDate.addDays(1);
                 expirationDate.setHourOfDay(8);
                 expirationDate.setMinuteOfHour(0);
                 expirationDate.setSecondOfMinute(0);
             }
         } else {
             expirationDate.setMillis(lastUpdate.getMillis() + 60 * 60 * 1000);
             if (expirationDate.getHourOfDay() >= 17) {
                 expirationDate.addDays(1);
                 expirationDate.setHourOfDay(9);
                 expirationDate.setMinuteOfHour(0);
                 expirationDate.setSecondOfMinute(0);
             }
         }
         return expirationDate.toDateTime();
     }
 
     static protected Status getStationStatus(String status) {
        if (OFFLINE.equalsIgnoreCase(status) || MAINTENANCE.equalsIgnoreCase(status)) {
             return Status.RED;
         } else if (TEST.equalsIgnoreCase(status)) {
             return Status.ORANGE;
         } else {
             return Status.GREEN;
         }
     }
 
     static protected Status getDataStatus(String status, DateTime now, DateTime expirationDate) {
         // Orange > 10 minutes late
         DateTime orangeStatusLimit = expirationDate.plus(10 * 60 * 1000);
         // Red > 2h10 late
         DateTime redStatusLimit = expirationDate.plus(2 * 3600 * 1000 + 10 * 60 * 1000);
 
        if ((OFFLINE.equalsIgnoreCase(status) || MAINTENANCE.equalsIgnoreCase(status)) || (now.isAfter(redStatusLimit))) {
             return Status.RED;
         } else if ((TEST.equalsIgnoreCase(status)) || (now.isAfter(orangeStatusLimit))) {
             return Status.ORANGE;
         } else {
             return Status.GREEN;
         }
     }
 
     private StationInfo createStationInfo(BasicDBObject stationJson) {
         StationInfo stationInfo = new StationInfo();
 
         stationInfo.setId(stationJson.getString("_id"));
         stationInfo.setShortName(stationJson.getString("shortname"));
         stationInfo.setName(stationJson.getString("name"));
         stationInfo.setDataValidity(getDataValidity(new DateTime()));
         stationInfo.setStationLocationType(StationLocationType.TAKEOFF);
         stationInfo.setWgs84Latitude(stationJson.getDouble("latitude"));
         stationInfo.setWgs84Longitude(stationJson.getDouble("longitude"));
         stationInfo.setAltitude(stationJson.getInt("altitude"));
         stationInfo.setMaintenanceStatus(getStationStatus(stationJson.getString("status")));
 
         return stationInfo;
     }
 
     @Override
     public List<StationInfo> getStationInfoList(boolean allStation) throws DataSourceException {
         try {
             DBCollection stations = database.getCollection(getStationsCollectionName());
 
             List<String> list = new ArrayList<String>();
             if (allStation == true) {
                list.add(MAINTENANCE);
                list.add(TEST);
                list.add(ONLINE);
             } else {
                list.add(ONLINE);
             }
             DBObject query = BasicDBObjectBuilder.start("status", new BasicDBObject("$in", list)).get();
             DBCursor cursor = stations.find(query);
 
             List<StationInfo> stationInfoList = new ArrayList<StationInfo>();
             while (cursor.hasNext()) {
                 try {
                     stationInfoList.add(createStationInfo((BasicDBObject) cursor.next()));
                 } catch (Exception e) {
                     log.warn("Station was ignored because:", e);
                 }
             }
 
             return stationInfoList;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         }
     }
 
     @Override
     public StationInfo getStationInfo(String stationId) throws DataSourceException {
         try {
             DBCollection stations = database.getCollection(getStationsCollectionName());
             BasicDBObject query = (BasicDBObject) BasicDBObjectBuilder.start("_id", stationId).get();
             BasicDBObject stationJson = (BasicDBObject) stations.findOne(query);
 
             if (stationJson != null) {
                 return createStationInfo(stationJson);
             } else {
                 throw new DataSourceException(DataSourceException.Error.INVALID_DATA, "Unable to find stationId '" + stationId + "'");
             }
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         }
     }
 
     private StationData createStationData(String stationId) {
         DBCollection stations = database.getCollection(getStationsCollectionName());
         BasicDBObject stationJson = (BasicDBObject) stations.findOne(BasicDBObjectBuilder.start("_id", stationId).get());
         BasicDBObject lastDataJson = (BasicDBObject) ((BasicDBList) stationJson.get("last-measurements")).get(0);
 
         StationData stationData = new StationData();
         stationData.setStationId(stationId);
 
         DateTime lastUpdate = getLastUpdateDateTime(stationId);
         stationData.setLastUpdate(lastUpdate);
         DateTime now = new DateTime();
         DateTime expirationDate = getExpirationDate(now, lastUpdate);
         stationData.setExpirationDate(expirationDate);
 
         // Status
         stationData.setStatus(getDataStatus(stationJson.getString("status"), now, expirationDate));
 
         // Wind average
         stationData.setWindAverage((float) lastDataJson.getDouble(DataTypeConstant.windAverage.getJsonKey()));
 
         // Wind max
         stationData.setWindMax((float) lastDataJson.getDouble(DataTypeConstant.windMax.getJsonKey()));
 
         // Wind direction chart
         List<DBObject> datas = getHistoricData(database.getCollection(getDataCollectionName(stationId)), lastUpdate, getHistoricDuration());
         Serie windDirectionSerie = createSerie(datas, DataTypeConstant.windDirection.getJsonKey());
         windDirectionSerie.setName(DataTypeConstant.windDirection.getName());
         Chart windDirectionChart = new Chart();
         windDirectionChart.setDuration(getHistoricDuration());
         windDirectionChart.getSeries().add(windDirectionSerie);
         stationData.setWindDirectionChart(windDirectionChart);
 
         // Wind history min/average
         double minValue = Double.MAX_VALUE;
         double maxValue = Double.MIN_VALUE;
         double sum = 0;
         double[][] windTrendMaxDatas = new double[datas.size()][2];
         // double[][] windTrendAverageDatas = new double[windAverageDatas.size()][2];
         for (int i = 0; i < datas.size(); i++) {
             DBObject data = datas.get(i);
             // JDC unix-time is in seconds, windmobile java-time in millis
             long millis = ((Number) data.get("_id")).longValue() * 1000;
             double windAverage = ((Number) data.get(DataTypeConstant.windAverage.getJsonKey())).doubleValue();
             double windMax = ((Number) data.get(DataTypeConstant.windMax.getJsonKey())).doubleValue();
             minValue = Math.min(minValue, windAverage);
             maxValue = Math.max(maxValue, windMax);
             sum += windAverage;
             windTrendMaxDatas[i][0] = millis;
             windTrendMaxDatas[i][1] = windMax;
         }
         stationData.setWindHistoryMin((float) minValue);
         stationData.setWindHistoryAverage((float) (sum / datas.size()));
         stationData.setWindHistoryMax((float) maxValue);
 
         // Wind trend
         LinearRegression linearRegression = new LinearRegression(windTrendMaxDatas);
         linearRegression.compute();
         double slope = linearRegression.getBeta1();
         double angle = Math.toDegrees(Math.atan(slope * getWindTrendScale()));
         stationData.setWindTrend((int) Math.round(angle));
 
         // Air temperature
         stationData.setAirTemperature((float) lastDataJson.getDouble(DataTypeConstant.airTemperature.getJsonKey()));
 
         // Air humidity
         stationData.setAirHumidity((float) lastDataJson.getDouble(DataTypeConstant.airHumidity.getJsonKey()));
 
         return stationData;
     }
 
     @Override
     public StationData getStationData(String stationId) throws DataSourceException {
         return createStationData(stationId);
     }
 
     private Serie createSerie(List<DBObject> datas, String key) {
         Serie serie = new Serie();
         for (DBObject data : datas) {
             Point newPoint = new Point();
             // JDC unix-time is in seconds, windmobile java-time in millis
             newPoint.setDate(((Number) data.get("_id")).longValue() * 1000);
             newPoint.setValue(((Number) data.get(key)).floatValue());
             serie.getPoints().add(newPoint);
         }
         return serie;
     }
 
     @Override
     public Chart getWindChart(String stationId, int duration) throws DataSourceException {
         try {
             Chart windChart = new Chart();
             windChart.setStationId(stationId);
 
             DateTime lastUpdate = getLastUpdateDateTime(stationId);
             windChart.setLastUpdate(lastUpdate);
             DateTime now = new DateTime();
             DateTime expirationDate = getExpirationDate(now, lastUpdate);
             windChart.setExpirationDate(expirationDate);
 
             List<DBObject> datas = getHistoricData(database.getCollection(getDataCollectionName(stationId)), lastUpdate, duration);
 
             // Wind historic chart
             Serie windAverageSerie = createSerie(datas, DataTypeConstant.windAverage.getJsonKey());
             windAverageSerie.setName(DataTypeConstant.windAverage.getName());
             Serie windMaxSerie = createSerie(datas, DataTypeConstant.windMax.getJsonKey());
             windMaxSerie.setName(DataTypeConstant.windMax.getName());
             Serie windDirectionSerie = createSerie(datas, DataTypeConstant.windDirection.getJsonKey());
             windDirectionSerie.setName(DataTypeConstant.windDirection.getName());
             windChart.setDuration(duration);
             windChart.getSeries().add(windAverageSerie);
             windChart.getSeries().add(windMaxSerie);
             windChart.getSeries().add(windDirectionSerie);
 
             return windChart;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         }
     }
 
     public int getHistoricDuration() {
         return historicDuration;
     }
 
     public void setHistoricDuration(int historicDuration) {
         this.historicDuration = historicDuration;
     }
 
     public int getWindTrendScale() {
         return windTrendScale;
     }
 
     public void setWindTrendScale(int windTrendScale) {
         this.windTrendScale = windTrendScale;
     }
 }
