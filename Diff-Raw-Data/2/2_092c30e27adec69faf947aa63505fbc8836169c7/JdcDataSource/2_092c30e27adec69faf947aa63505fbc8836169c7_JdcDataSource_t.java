 package ch.windmobile.server.jdc;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.Vector;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.windmobile.server.jdc.dataobject.Data;
 import ch.windmobile.server.jdc.dataobject.Sensor;
 import ch.windmobile.server.jdc.dataobject.Station;
 import ch.windmobile.server.model.DataSourceException;
 import ch.windmobile.server.model.DataSourceException.Error;
 import ch.windmobile.server.model.LinearRegression;
 import ch.windmobile.server.model.WindMobileDataSource;
 import ch.windmobile.server.model.xml.Chart;
 import ch.windmobile.server.model.xml.Point;
 import ch.windmobile.server.model.xml.Serie;
 import ch.windmobile.server.model.xml.StationData;
 import ch.windmobile.server.model.xml.StationInfo;
 import ch.windmobile.server.model.xml.StationLocationType;
 import ch.windmobile.server.model.xml.Status;
 
 public class JdcDataSource implements WindMobileDataSource {
     protected final Logger log = LoggerFactory.getLogger(getClass());
 
     // 1 hour by default
     private int historicDuration = 60 * 60;
     private int windTrendScale = 500000;
 
     static enum Channel {
         windDirection(1, "windDirection"), windAverage(2, "windAverage"), windMax(3, "windMax"), airTemperature(4, "airTemperature"), airHumidity(5,
             "airHumidity");
 
         private final int id;
         private final String name;
 
         private Channel(int id, String name) {
             this.id = id;
             this.name = name;
         }
 
         public int getId() {
             return id;
         }
 
         public String getName() {
             return name;
         }
     }
 
     static enum JdcStatus {
         inactive(0), active(1), maintenance(2), test(3);
 
         private final int value;
 
         private JdcStatus(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
     }
 
     private final SessionFactory sessionFactory;
 
     public JdcDataSource(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     private Session getSession() {
         return sessionFactory.openSession();
     }
 
     private int getJdcStationId(String stationId) {
         return Integer.parseInt(stationId);
     }
 
     private String getServerStationId(int stationId) {
         return Integer.toString(stationId);
     }
 
     @SuppressWarnings("unchecked")
     private List<Station> getStations(Session session, boolean allStation) {
         Criteria criteria = session.createCriteria(Station.class);
         if (allStation) {
             criteria.add(Restrictions.ge("status", 1));
         } else {
             criteria.add(Restrictions.eq("status", 1));
         }
         criteria.setCacheable(true);
         criteria.setCacheRegion("stationInfos");
         return criteria.list();
     }
 
     private Station getStation(Session session, String stationId) {
         Criteria criteria = session.createCriteria(Station.class);
         criteria.add(Restrictions.eq("id", getJdcStationId(stationId)));
         criteria.setCacheable(true);
         criteria.setCacheRegion("stationInfos");
         return (Station) criteria.uniqueResult();
     }
 
     private Calendar getLastUpdate(Session session, Sensor sensor) throws DataSourceException {
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("sensor", sensor));
         criteria.addOrder(Order.desc("time"));
         criteria.setMaxResults(1);
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
 
         Data lastData = (Data) criteria.uniqueResult();
         if ((lastData == null) || (lastData.getTime() == null) || (lastData.getTime().getTime() == 0)) {
             throw new DataSourceException(Error.INVALID_DATA, "No data found for sensor '" + sensor.getId() + "'");
         }
         Calendar lastUpdate = new GregorianCalendar();
         lastUpdate.setTime(lastData.getTime());
         return lastUpdate;
     }
 
     private Sensor getSensorForChannel(Station station, Channel channel) throws DataSourceException {
         Set<Sensor> sensors = station.getSensors();
         for (Sensor sensor : sensors) {
             if (sensor.getChannel() == channel.getId()) {
                 return sensor;
             }
         }
         throw new DataSourceException(Error.INVALID_DATA, "No sensor found for channel '" + channel.getId() + "'");
     }
 
     /**
      * Return the last value of a sensorId
      * 
      * @param session
      * @param sensor
      * @param date
      * @return
      * @throws DataSourceException
      */
     @SuppressWarnings("unchecked")
     private double getData(Session session, Sensor sensor, Calendar date) throws DataSourceException {
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("sensor", sensor));
         criteria.add(Restrictions.eq("time", date.getTime()));
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
 
         List<Data> datas = criteria.list();
         if (datas.size() != 1) {
             log.warn("There are multiple values for sensor '" + sensor.getId() + "' at '" + date.getTime() + "'");
             // Try to return the 1st non null value
             for (Data data : datas) {
                 if (data.getValue() != 0) {
                     return data.getValue();
                 }
             }
         }
         return datas.get(0).getValue();
     }
 
     /**
      * Return a list of Data from (lastUpdate - duration) to lastUpdate
      * 
      * @param session
      * @param sensorId
      * @param duration
      *            The duration in seconds
      * @return
      * @throws DataSourceException
      */
     @SuppressWarnings("unchecked")
     private List<Data> getHistoricData(Session session, Sensor sensor, int duration) throws DataSourceException {
         Calendar lastUpdate = getLastUpdate(session, sensor);
         long startTime = lastUpdate.getTimeInMillis() - duration * 1000;
 
         Criteria criteria = session.createCriteria(Data.class);
         criteria.add(Restrictions.eq("sensor", sensor));
         criteria.add(Restrictions.ge("time", new Date(startTime)));
         criteria.setCacheable(true);
         criteria.setCacheRegion("dataQueries");
         return criteria.list();
     }
 
     @Override
     public Calendar getLastUpdate(String stationId) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
 
             Sensor sensor = getSensorForChannel(station, Channel.windAverage);
             return getLastUpdate(session, sensor);
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     // Replaced by getExpirationDate()
     @Deprecated
     private int getDataValidity() {
         Calendar now = Calendar.getInstance();
         if (isSummerFrequency(now)) {
             // Summer frequency
             return 20 * 60;
         } else {
             // Winter frequency
             return 60 * 60;
         }
     }
 
     static protected boolean isSummerFrequency(Calendar date) {
         return (date.get(Calendar.MONTH) >= Calendar.APRIL) && (date.get(Calendar.MONTH) <= Calendar.SEPTEMBER);
     }
 
     static protected Calendar getExpirationDate(Calendar now, Calendar lastUpdate) {
         TimeZone stationLocalTimeZone = lastUpdate.getTimeZone();
         Calendar expirationDate = Calendar.getInstance(stationLocalTimeZone);
 
         now.setTimeZone(stationLocalTimeZone);
         if (isSummerFrequency(now)) {
             expirationDate.setTimeInMillis(lastUpdate.getTimeInMillis() + 20 * 60 * 1000);
            if (expirationDate.get(Calendar.HOUR_OF_DAY) >= 20) {
                 expirationDate.add(Calendar.DAY_OF_MONTH, 1);
                 expirationDate.set(Calendar.HOUR_OF_DAY, 8);
                 expirationDate.set(Calendar.MINUTE, 0);
                 expirationDate.set(Calendar.SECOND, 0);
             }
         } else {
             expirationDate.setTimeInMillis(lastUpdate.getTimeInMillis() + 60 * 60 * 1000);
             if (expirationDate.get(Calendar.HOUR_OF_DAY) >= 17) {
                 expirationDate.add(Calendar.DAY_OF_MONTH, 1);
                 expirationDate.set(Calendar.HOUR_OF_DAY, 9);
                 expirationDate.set(Calendar.MINUTE, 0);
                 expirationDate.set(Calendar.SECOND, 0);
             }
         }
         return expirationDate;
     }
 
     static protected Status getMaintenanceStatus(Station station) {
         if (station.getStatus() == JdcStatus.maintenance.getValue()) {
             return Status.RED;
         } else if (station.getStatus() == JdcStatus.test.getValue()) {
             return Status.ORANGE;
         } else {
             return Status.GREEN;
         }
     }
 
     static protected Status getStatus(Station station, Calendar now, Calendar expirationDate) {
         // Orange > 10 minutes late
         Date orangeStatusLimit = new Date(expirationDate.getTimeInMillis() + 10 * 60 * 1000);
         // Red > 2h10 late
         Date redStatusLimit = new Date(expirationDate.getTimeInMillis() + 2 * 3600 * 1000 + 10 * 60 * 1000);
 
         if ((station.getStatus() == JdcStatus.maintenance.getValue()) || (now.getTime().after(redStatusLimit))) {
             return Status.RED;
         } else if ((station.getStatus() == JdcStatus.test.getValue()) || (now.getTime().after(orangeStatusLimit))) {
             return Status.ORANGE;
         } else {
             return Status.GREEN;
         }
     }
 
     private StationInfo createStationInfo(Session session, Station station) throws NumberFormatException {
         StationInfo stationInfo = new StationInfo();
 
         stationInfo.setId(getServerStationId(station.getId()));
         stationInfo.setShortName(station.getShortName());
         stationInfo.setName(station.getName());
         stationInfo.setDataValidity(getDataValidity());
         stationInfo.setStationLocationType(StationLocationType.TAKEOFF);
         stationInfo.setWgs84Latitude(Double.parseDouble(station.getWgs84Latitude()));
         stationInfo.setWgs84Longitude(Double.parseDouble(station.getWgs84Longitude()));
         stationInfo.setAltitude(station.getAltitude());
         stationInfo.setMaintenanceStatus(getMaintenanceStatus(station));
 
         return stationInfo;
     }
 
     @Override
     public StationInfo getStationInfo(String stationId) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
 
             return createStationInfo(session, station);
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     @Override
     public List<StationInfo> getStationInfoList(boolean allStation) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             List<Station> stations = getStations(session, allStation);
 
             List<StationInfo> stationInfoList = new Vector<StationInfo>();
             for (Station station : stations) {
                 try {
                     stationInfoList.add(createStationInfo(session, station));
                 } catch (NumberFormatException e) {
                     log.warn("This station '" + station.getId() + "' was ignored because:", e);
                 }
             }
 
             return stationInfoList;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     private StationData createStationData(Session session, Station station) throws DataSourceException {
         StationData stationData = new StationData();
 
         stationData.setStationId(getServerStationId(station.getId()));
 
         // Last update, based on wind average
         Sensor sensor = getSensorForChannel(station, Channel.windAverage);
         Calendar lastUpdate = getLastUpdate(session, sensor);
         stationData.setLastUpdate(lastUpdate);
 
         // Expiration date
         Calendar now = Calendar.getInstance();
         Calendar expirationDate = getExpirationDate(now, lastUpdate);
         stationData.setExpirationDate(expirationDate);
 
         // Status
         stationData.setStatus(getStatus(station, now, expirationDate));
 
         // Wind average
         sensor = getSensorForChannel(station, Channel.windAverage);
         double windAverage = getData(session, sensor, lastUpdate);
         stationData.setWindAverage((float) windAverage);
 
         // Wind max
         sensor = getSensorForChannel(station, Channel.windMax);
         double windMax = getData(session, sensor, lastUpdate);
         stationData.setWindMax((float) windMax);
 
         // Wind direction chart
         sensor = getSensorForChannel(station, Channel.windDirection);
         List<Data> windDirectionDatas = getHistoricData(session, sensor, getHistoricDuration());
         Serie windDirectionSerie = createSerie(windDirectionDatas);
         windDirectionSerie.setName(Channel.windDirection.getName());
         Chart windDirectionChart = new Chart();
         windDirectionChart.setDuration(getHistoricDuration());
         windDirectionChart.getSeries().add(windDirectionSerie);
         stationData.setWindDirectionChart(windDirectionChart);
 
         // Wind history min/average
         sensor = getSensorForChannel(station, Channel.windAverage);
         List<Data> windAverageDatas = getHistoricData(session, sensor, getHistoricDuration());
         double minValue = Double.MAX_VALUE;
         double sum = 0;
         // double[][] windTrendAverageDatas = new double[windAverageDatas.size()][2];
         for (int i = 0; i < windAverageDatas.size(); i++) {
             Data data = windAverageDatas.get(i);
             minValue = Math.min(minValue, data.getValue());
             sum += data.getValue();
             // windTrendAverageDatas[i][0] = data.getTime().getTime();
             // windTrendAverageDatas[i][1] = data.getValue();
         }
         stationData.setWindHistoryMin((float) minValue);
         stationData.setWindHistoryAverage((float) (sum / windAverageDatas.size()));
 
         // Wind history max
         sensor = getSensorForChannel(station, Channel.windMax);
         List<Data> windMaxDatas = getHistoricData(session, sensor, getHistoricDuration());
         double maxValue = Double.MIN_VALUE;
         double[][] windTrendMaxDatas = new double[windMaxDatas.size()][2];
         for (int i = 0; i < windMaxDatas.size(); i++) {
             Data data = windMaxDatas.get(i);
             maxValue = Math.max(maxValue, data.getValue());
             windTrendMaxDatas[i][0] = data.getTime().getTime();
             windTrendMaxDatas[i][1] = data.getValue();
         }
         stationData.setWindHistoryMax((float) maxValue);
 
         // Wind trend
         LinearRegression linearRegression = new LinearRegression(windTrendMaxDatas);
         linearRegression.compute();
         double slope = linearRegression.getBeta1();
         double angle = Math.toDegrees(Math.atan(slope * getWindTrendScale()));
         stationData.setWindTrend((int) angle);
 
         // Air temperature
         sensor = getSensorForChannel(station, Channel.airTemperature);
         double airTemperature = getData(session, sensor, lastUpdate);
         stationData.setAirTemperature((float) airTemperature);
 
         // Air humidity
         sensor = getSensorForChannel(station, Channel.airHumidity);
         double airHumidity = getData(session, sensor, lastUpdate);
         stationData.setAirHumidity((float) airHumidity);
 
         return stationData;
     }
 
     @Override
     public StationData getStationData(String stationId) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
 
             return createStationData(session, station);
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
         }
     }
 
     private Serie createSerie(List<Data> datas) {
         Serie serie = new Serie();
         for (Data data : datas) {
             Point newPoint = new Point();
             newPoint.setDate(data.getTime().getTime());
             newPoint.setValue((float) data.getValue());
             serie.getPoints().add(newPoint);
         }
         return serie;
     }
 
     @Override
     public Chart getWindChart(String stationId, int duration) throws DataSourceException {
         Session session = null;
         try {
             session = getSession();
             Station station = getStation(session, stationId);
 
             Chart windChart = new Chart();
 
             // Last update
             Sensor sensor = getSensorForChannel(station, Channel.windAverage);
             Calendar lastUpdate = getLastUpdate(session, sensor);
             windChart.setLastUpdate(lastUpdate);
 
             // Wind historic chart
             sensor = getSensorForChannel(station, Channel.windAverage);
             List<Data> windAverageDatas = getHistoricData(session, sensor, duration);
             Serie windAverageSerie = createSerie(windAverageDatas);
             windAverageSerie.setName(Channel.windAverage.getName());
             sensor = getSensorForChannel(station, Channel.windMax);
             List<Data> windMaxDatas = getHistoricData(session, sensor, duration);
             Serie windMaxSerie = createSerie(windMaxDatas);
             windMaxSerie.setName(Channel.windMax.getName());
             sensor = getSensorForChannel(station, Channel.windDirection);
             List<Data> windDirectionDatas = getHistoricData(session, sensor, duration);
             Serie windDirectionSerie = createSerie(windDirectionDatas);
             windDirectionSerie.setName(Channel.windDirection.getName());
             windChart.setDuration(duration);
             windChart.getSeries().add(windAverageSerie);
             windChart.getSeries().add(windMaxSerie);
             windChart.getSeries().add(windDirectionSerie);
 
             return windChart;
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         } finally {
             try {
                 session.close();
             } catch (Exception e) {
             }
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
