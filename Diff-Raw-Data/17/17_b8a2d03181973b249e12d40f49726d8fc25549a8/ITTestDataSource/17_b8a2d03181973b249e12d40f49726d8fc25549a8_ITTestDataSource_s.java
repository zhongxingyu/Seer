 package ch.windmobile.server;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import ch.windmobile.Assert;
 import ch.windmobile.server.model.DataSourceException;
 import ch.windmobile.server.model.WindMobileDataSource;
 import ch.windmobile.server.model.xml.Chart;
 import ch.windmobile.server.model.xml.Serie;
 import ch.windmobile.server.model.xml.StationData;
 import ch.windmobile.server.model.xml.StationInfo;
 
 public abstract class ITTestDataSource extends AbstractTestNGSpringContextTests {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm Z");
 
     @Autowired
     private WindMobileDataSource dataSource;
 
     @DataProvider(name = "stationIds")
     public Object[][] stationIds() throws DataSourceException {
         List<StationInfo> stationInfos = dataSource.getStationInfoList(false);
         Object[][] results = new Object[stationInfos.size()][1];
 
         for (int i = 0; i < stationInfos.size(); i++) {
             results[i][0] = stationInfos.get(i).getId();
         }
         return results;
     }
 
    void testLastUpdate(Calendar lastUpdate) {
         Assert.assertNotNull(lastUpdate, "lastUpdate is null");
         Calendar now = new GregorianCalendar();
         Assert.assertFalse(lastUpdate.after(now), "lastUpdate is in the future");
        lastUpdate.add(Calendar.HOUR_OF_DAY, 18);
        Assert.assertTrue(lastUpdate.after(now), "lastUpdate is more than 18 hours old");
     }
 
     @Test(dataProvider = "stationIds")
     public void testGetLastUpdate(String stationId) throws DataSourceException {
         Calendar lastUpdate = dataSource.getLastUpdate(stationId);
         log.info("getLastUpdate returns " + dateFormatter.format(lastUpdate.getTime()));
 
        testLastUpdate(lastUpdate);
     }
 
     void testStationId(String stationId) {
         Assert.assertNotEmpty(stationId, "Not a valid id");
         Assert.assertTrue(stationId.contains(":") == false, "Not a valid id");
     }
 
     void testAltitude(int altitude) {
         if (altitude > 0 && altitude < 3500) {
             return;
         }
         Assert.fail("Not a valid altitude");
     }
 
     void testCoordinate(double coordinate) {
         Assert.assertTrue(coordinate != 0.0, "Not a valid coordinate");
     }
 
     void testStationInfoContent(StationInfo info) {
         log.info("StationInfo");
         log.info("    stationId --> " + info.getId());
         log.info("    shortName --> " + info.getShortName());
         log.info("    name --> " + info.getName());
 
         testStationId(info.getId());
         Assert.assertNotEmpty(info.getShortName(), "No short name");
         Assert.assertNotEmpty(info.getName(), "No name");
         Assert.assertTrue(info.getShortName().length() <= info.getName().length(), "Not a short name");
 
         testAltitude(info.getAltitude());
         testCoordinate(info.getWgs84Latitude());
         testCoordinate(info.getWgs84Longitude());
     }
 
     @Test(dataProvider = "stationIds")
     public void testGetStationInfos(String stationId) throws DataSourceException {
         testStationInfoContent(dataSource.getStationInfo(stationId));
     }
 
     float testWindValue(float value) {
         Assert.assertTrue(value >= 0.0 && value < 150.0, "Wind value out of range");
         return value;
     }
 
     void testStationDataContent(StationData data) {
         log.info("StationData");
         log.info("    stationId --> " + data.getStationId());
         log.info("    lastUpdate --> " + data.getLastUpdate());
         log.info("    status --> " + data.getStatus());
         log.info("    windAverage --> " + testWindValue(data.getWindAverage()));
         log.info("    windMax --> " + testWindValue(data.getWindMax()));
         log.info("    windHistoryMin --> " + testWindValue(data.getWindHistoryMin()));
         log.info("    windHistoryAverage --> " + testWindValue(data.getWindHistoryAverage()));
         log.info("    windHistoryMax --> " + testWindValue(data.getWindHistoryMax()));
 
         testStationId(data.getStationId());
        testLastUpdate(data.getLastUpdate());
 
         Assert.assertNotNull(data.getStatus().value());
 
         Assert.assertTrue(data.getWindAverage().floatValue() <= data.getWindMax().floatValue());
         Assert.assertTrue(data.getWindHistoryMin().floatValue() <= data.getWindHistoryAverage().floatValue());
         Assert.assertTrue(data.getWindHistoryAverage().floatValue() <= data.getWindHistoryMax().floatValue());
 
         Assert.assertTrue(data.getWindHistoryAverage() > 0, "Wind history is 0");
     }
 
     @Test(dataProvider = "stationIds")
     public void testGetStationDatas(String stationId) throws DataSourceException {
         testStationDataContent(dataSource.getStationData(stationId));
     }
 
     void testWindChart(String stationId, int chartDuration) throws DataSourceException {
         Chart windChart = dataSource.getWindChart(stationId, chartDuration);
         Assert.assertEquals((int) windChart.getDuration(), chartDuration);
 
         Serie windAverageSerie = windChart.getSeries().get(0);
         Assert.assertEquals(windAverageSerie.getName(), "windAverage");
         Assert.assertTrue(windAverageSerie.getPoints().size() > 0);
 
         Serie windMaxSerie = windChart.getSeries().get(1);
         Assert.assertEquals(windMaxSerie.getName(), "windMax");
         Assert.assertTrue(windMaxSerie.getPoints().size() > 0);
     }
 
     int[] windChartDuration() {
         return new int[] { 7200, 9600, 1200 };
     }
 
     @Test(dataProvider = "stationIds")
     public void testGetWindChart(String stationId) throws DataSourceException {
         for (int duration : windChartDuration()) {
             testWindChart(stationId, duration);
 
         }
     }
 }
