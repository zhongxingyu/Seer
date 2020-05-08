 package ch.opentrainingcenter.model.training.internal;
 
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.opentrainingcenter.core.data.Pair;
 import ch.opentrainingcenter.model.training.Intervall;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 @SuppressWarnings("nls")
 public class GoldMedalModelTest {
 
     GoldMedalModel model;
 
     @Before
     public void before() {
         Locale.setDefault(Locale.GERMAN);
         TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
 
         final DateTimeZone zoneUTC = DateTimeZone.forID("Europe/Berlin");
         DateTimeZone.setDefault(zoneUTC);
 
         model = new GoldMedalModel();
     }
 
     @After
     public void after() {
         model = null;
     }
 
     @Test
     public void testSchnellstePaceNullKey() {
         final Pair<Long, String> pace = new Pair<Long, String>(null, null);
         model.setSchnellstePace(pace);
         final Pair<Long, String> result = model.getSchnellstePace();
         assertEmptyPair(result);
     }
 
     @Test
     public void testSchnellstePaceNullValue() {
         final Pair<Long, String> pace = new Pair<Long, String>(1L, null);
         model.setSchnellstePace(pace);
         final Pair<Long, String> result = model.getSchnellstePace();
         assertEmptyPair(result);
     }
 
     @Test
     public void testSchnellstePaceValue() {
         final String value = "123";
         final Pair<Long, String> pace = new Pair<Long, String>(1L, value);
         model.setSchnellstePace(pace);
         final String result = model.getSchnellstePace().getSecond();
         assertEquals("Wert (second)", value, result);
     }
 
     @Test
     public void testGetSchnellstePaceIntervallValueNotFound() {
         for (final Intervall intervall : Intervall.values()) {
            final Pair<Long, String> result = model.getSchnellstePace(intervall);
             assertEmptyPair(result);
         }
     }
 
     @Test
     public void testGetSchnellstePaceIntervallValueFound() {
         final String value = "123";
         final Pair<Long, String> schnellstePace = new Pair<>(1L, value);
         model.setSchnellstePace(Intervall.KLEINER_10, schnellstePace);
         final Pair<Long, String> result = model.getSchnellstePace(Intervall.KLEINER_10);
         assertEquals("Key vorhanden", 1L, result.getFirst(), 0.00001);
         assertEquals("Wert vorhanden", "123", result.getSecond());
     }
 
     @Test
     public void testLongestDistanceNullKey() {
         model.setLongestDistance(new Pair<Long, String>(null, null));
         assertEmptyPair(model.getLongestDistance());
     }
 
     @Test
     public void testLongestDistanceNullValue() {
         model.setLongestDistance(new Pair<Long, String>(1L, null));
         assertEmptyPair(model.getLongestDistance());
     }
 
     @Test
     public void testLongestDistanceValue() {
         final String value = "123";
         model.setLongestDistance(new Pair<Long, String>(1L, value));
         final String result = model.getLongestDistance().getSecond();
         assertEquals("Wert vorhanden", value, result);
     }
 
     @Test
     public void testLongestRunNullKey() {
         model.setLongestRun(new Pair<Long, String>(null, null));
         assertEmptyPair(model.getLongestRun());
     }
 
     @Test
     public void testLongestRunNullValue() {
         model.setLongestRun(new Pair<Long, String>(1L, null));
         assertEmptyPair(model.getLongestRun());
     }
 
     @Test
     public void testLongestRunValue() {
         final String value = "123";
         model.setLongestRun(new Pair<Long, String>(1L, value));
         final String result = model.getLongestRun().getSecond();
         assertEquals("Wert vorhanden", value, result);
     }
 
     // Pulse
     @Test
     public void testHighestPulsKeyNull() {
         model.setHighestPulse(new Pair<Long, String>(null, null));
         assertEmptyPair(model.getHighestPulse());
     }
 
     @Test
     public void testHighestPulsNullValue() {
         model.setHighestPulse(new Pair<Long, String>(1L, null));
         assertEmptyPair(model.getHighestPulse());
     }
 
     @Test
     public void testHighestPulsValue() {
         final String value = "123";
         model.setHighestPulse(new Pair<Long, String>(1L, value));
         final String result = model.getHighestPulse().getSecond();
         assertEquals("Wert vorhanden", value, result);
     }
 
     // average pulse
     @Test
     public void testHighestAveragePulsKeyNull() {
         model.setHighestAveragePulse(new Pair<Long, String>(null, null));
         assertEmptyPair(model.getHighestAveragePulse());
     }
 
     @Test
     public void testHighestAveragePulsNullValue() {
         model.setHighestAveragePulse(new Pair<Long, String>(1L, null));
         assertEmptyPair(model.getHighestAveragePulse());
     }
 
     @Test
     public void testHighestAveragePulsValue() {
         final String value = "123";
         model.setHighestAveragePulse(new Pair<Long, String>(1L, value));
         final String result = model.getHighestAveragePulse().getSecond();
         assertEquals("Wert vorhanden", value, result);
     }
 
     // lowest puls
     @Test
     public void testLowestAveragePulsKeyNull() {
         model.setLowestAveragePulse(new Pair<Long, String>(null, null));
         assertEmptyPair(model.getLowestAveragePulse());
     }
 
     @Test
     public void testLowestAveragePulsNullValue() {
         model.setLowestAveragePulse(new Pair<Long, String>(1L, null));
         assertEmptyPair(model.getLowestAveragePulse());
     }
 
     @Test
     public void testLowestAveragePulsValue() {
         final String value = "123";
         model.setLowestAveragePulse(new Pair<Long, String>(1L, value));
         final String result = model.getLowestAveragePulse().getSecond();
         assertEquals("Wert vorhanden", value, result);
     }
 
     private void assertEmptyPair(final Pair<Long, String> result) {
         assertNull("Key vom EmptyPair", result.getFirst());
         assertEquals("Default value", "-", result.getSecond());
     }
 }
