 package ch.windmobile.server.jdc;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.testng.Assert;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import ch.windmobile.server.jdc.JdcDataSource.JdcStatus;
 import ch.windmobile.server.jdc.dataobject.Station;
 import ch.windmobile.server.model.xml.Status;
 
 public class TestJdcDataSource {
     private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm Z");
 
     @DataProvider(name = "expirationDates")
     public Object[][] expirationDates() {
         return new Object[][] {
            { "21/03/2010 11:05 +0100", "21/03/2010 11:00 +0100", "21/03/2010 12:00 +0100", "GREEN" },
            { "21/03/2010 12:20 +0100", "21/03/2010 11:00 +0100", "21/03/2010 12:00 +0100", "ORANGE" },
             { "31/07/2010 11:05 +0200", "31/07/2010 11:00 +0200", "31/07/2010 11:20 +0200", "GREEN" },
            { "31/07/2010 22:45 +0200", "31/07/2010 19:45 +0200", "1/08/2010 08:00 +0200", "GREEN" },
            { "1/08/2010 08:09 +0200", "31/07/2010 19:45 +0200", "1/08/2010 08:00 +0200", "GREEN" },
            { "1/08/2010 09:00 +0200", "31/07/2010 19:45 +0200", "1/08/2010 08:00 +0200", "ORANGE" },
            { "1/08/2010 10:11 +0200", "31/07/2010 19:45 +0200", "1/08/2010 08:00 +0200", "RED" } };
     }
 
     @Test(dataProvider = "expirationDates")
     public void testGetExpirationDate(String sNow, String sLastUpdate, String sExpectedResult, String sExpectedStatus)
         throws ParseException {
         Calendar now = Calendar.getInstance();
         now.setTime(dateFormatter.parse(sNow));
 
         Calendar lastUpdate = Calendar.getInstance();
         lastUpdate.setTime(dateFormatter.parse(sLastUpdate));
 
         Calendar expectedResult = Calendar.getInstance();
         expectedResult.setTime(dateFormatter.parse(sExpectedResult));
 
         Calendar expirationDate = JdcDataSource.getExpirationDate(now, lastUpdate);
         Station station = new Station();
         station.setStatus(JdcStatus.active.getValue());
         Status status = JdcDataSource.getStatus(station, now, expirationDate);
 
         Assert.assertEquals(dateFormatter.format(expirationDate.getTime()),
             dateFormatter.format(expectedResult.getTime()));
         Assert.assertEquals(status.toString(), sExpectedStatus);
     }
 }
