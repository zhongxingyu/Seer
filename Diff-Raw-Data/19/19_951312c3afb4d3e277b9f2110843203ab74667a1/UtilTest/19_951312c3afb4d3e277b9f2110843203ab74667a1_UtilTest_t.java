 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.main;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.activity.main.Util;
 
 import org.junit.Test;
 
 import android.net.UrlQuerySanitizer;
 import android.net.UrlQuerySanitizer.ValueSanitizer;
 
 import java.util.Locale;
 
 public class UtilTest {
     private void splitLatLonHelper(String coords, String lat, String lon) {
         CharSequence[] latLon = Util.splitLatLon(coords);
         assertEquals(lat, latLon[0]);
         assertEquals(lon, latLon[1]);
     }
 
     @Test
     public void testDistanceKm() {
         assertEquals(2759.2, GeoUtils.distanceKm(180, 45, 160, 30), 0.5);
     }
 
     @Test
     public void testCtorForStatics() {
         new GeoUtils();
         new Util();
     }
 
     @Test
     public void testBearing() {
         assertEquals(210, GeoUtils.bearing(90, 120, 75, 90), .1);
     }
 
     @Test
     public void testConvertDegreesToMinutes() {
         // Make sure formatting is US even if the phone is in a different
         // locale.
         // TODO: add locale-specific parsing/formatting.
         Locale.setDefault(Locale.GERMANY);
         assertEquals("-122 30.000", Util
                 .formatDegreesAsDecimalDegreesString(-122.5));
         assertEquals("-122 30.600", Util
                 .formatDegreesAsDecimalDegreesString(-122.51));
         assertEquals("-122 03.000", Util
                 .formatDegreesAsDecimalDegreesString(-122.05));
         assertEquals("-0 03.000", Util
                 .formatDegreesAsDecimalDegreesString(-0.05));
     }
 
     @Test
     public void testGetLatLonDescriptionFromQuery() {
         CharSequence[] coordsAndDescription = Util
                 .splitLatLonDescription("Wildwood Park, Saratoga, CA(The Nut Case #89882)@37.258356797547,-122.0354267005 ");
         assertEquals("37.258356797547", coordsAndDescription[0]);
         assertEquals("-122.0354267005", coordsAndDescription[1]);
         assertEquals("LB89882", coordsAndDescription[2]);
         assertEquals("The Nut Case", coordsAndDescription[3]);
     }
 
     @Test
     public void testMinutesToDegrees() {
         assertEquals(122.0, Util.parseCoordinate("122"), 0.0);
         assertEquals(122.5, Util.parseCoordinate("122 30"), 0.0);
         assertEquals(122.51, Util.parseCoordinate("122 30.600"), 0.0);
         assertEquals(-122.51, Util.parseCoordinate("-122 30.600"), 0.0);
         assertEquals(-0.0165, Util.parseCoordinate("W 000 00.990"), 0.0);
         assertEquals(-0.0165, Util.parseCoordinate("-000 00.990"), 0.0);
         assertEquals(-122.51, Util.parseCoordinate("-122 30.600"), 0.0);
         assertEquals(122.51, Util.parseCoordinate("E 122 30.600"), 0.0);
         assertEquals(-122.51, Util.parseCoordinate("W 122 30.600"), 0.0);
         assertEquals(-37.0, Util.parseCoordinate("S 37 0.000"), 0.0);
         assertEquals(-37.0, Util.parseCoordinate(" S 37 0.000"), 0.0);
     }
 
     @Test
     public void testParseCoordinate() {
         assertEquals(122.5, Util.parseCoordinate("122.5"), 0);
        assertEquals(122.5, Util.parseCoordinate("122,5"), 0);
         assertEquals(-122.5, Util.parseCoordinate("W122.5"), 0);
         assertEquals(122.51, Util.parseCoordinate("122 30.6"), 0);
        assertEquals(122.51, Util.parseCoordinate("122 30,6"), 0);
         assertEquals(122.51, Util.parseCoordinate("122 30 36"), 0);
 
         assertEquals(40.4425, Util.parseCoordinate("40:26:33"), 0.00001);
 
         assertEquals(-40.4425, Util.parseCoordinate("40:26:33S"), 0.00001);
         assertEquals(40.4425, Util.parseCoordinate("40:26:33N"), 0.00001);
         assertEquals(40.4425, Util.parseCoordinate("E40:26:33"), 0.00001);
         assertEquals(37.25275, Util.parseCoordinate("N+37+15.165 "), 0.00001);
 
        assertEquals(40.446195, Util.parseCoordinate("40 26,7717"), 0);
         assertEquals(40.446195, Util.parseCoordinate("40 26.7717"), 0);
         assertEquals(40.446195, Util.parseCoordinate("40:26:46.302N"),
                 0.0000001);
         assertEquals(40.4461, Util.parseCoordinate("4026'46\"N"), 0.0001);
         assertEquals(40.4461, Util.parseCoordinate("40d 26' 46\" N"), 0.0001);
         assertEquals(40.446195, Util.parseCoordinate("40.446195N"), 0.0000001);
         assertEquals(40.446195, Util.parseCoordinate("40.446195"), 0.0000001);
        assertEquals(40.446195, Util.parseCoordinate("40,446195"), 0.0000001);
         assertEquals(40.446195, Util.parseCoordinate("40 26.7717"), 0.0000001);
     }
 
     @Test
     public void testParseDescription() {
         CharSequence[] groundspeak = Util.parseDescription("GCTANE");
         assertEquals("GCTANE", groundspeak[0]);
         assertEquals("", groundspeak[1]);
 
         CharSequence[] atlasquest = Util
                 .parseDescription("Wildwood Park, Saratoga, CA(The Nut Case #89882)");
         assertEquals("LB89882", atlasquest[0]);
         assertEquals("The Nut Case", atlasquest[1]);
     }
 
     @Test
     public void testParseHttpUri() {
         UrlQuerySanitizer sanitizer = createMock(UrlQuerySanitizer.class);
         ValueSanitizer valueSanitizer = createMock(ValueSanitizer.class);
         sanitizer.registerParameters(Util.geocachingQueryParam, valueSanitizer);
         final String unsanitizedQuery = "#unsanitized_query";
         sanitizer.parseQuery(unsanitizedQuery);
         final String sanitizedQuery = "#sanitized_query";
         expect(sanitizer.getValue("q")).andReturn(sanitizedQuery);
 
         replay(sanitizer);
         assertEquals(sanitizedQuery, Util.parseHttpUri(unsanitizedQuery,
                 sanitizer, valueSanitizer));
         verify(sanitizer);
     }
 
     @Test
     public void testSplitCoordsAndDescriptions() {
         CharSequence[] coordsAndDescription = Util
                 .splitCoordsAndDescription("GC1ERCC@N+37+15.165+W+122+02.620+");
         assertEquals("N+37+15.165+W+122+02.620+", coordsAndDescription[0]);
         assertEquals("GC1ERCC", coordsAndDescription[1]);
 
         coordsAndDescription = Util
                 .splitCoordsAndDescription("N+47+40.464+W+122+20.119+(GCTANE)+");
         assertEquals("N+47+40.464+W+122+20.119+", coordsAndDescription[0]);
         assertEquals("GCTANE", coordsAndDescription[1]);
 
         coordsAndDescription = Util
                 .splitCoordsAndDescription("37.258356797547,-122.0354267005 (Wildwood Park, Saratoga, CA)");
         assertEquals("37.258356797547,-122.0354267005", coordsAndDescription[0]);
         assertEquals("Wildwood Park, Saratoga, CA", coordsAndDescription[1]);
 
         coordsAndDescription = Util
                 .splitCoordsAndDescription("Wildwood Park, Saratoga, CA(The Nut Case #89882)@37.258356797547,-122.0354267005 ");
         assertEquals("37.258356797547,-122.0354267005", coordsAndDescription[0]);
         assertEquals("Wildwood Park, Saratoga, CA(The Nut Case #89882)",
                 coordsAndDescription[1]);
 
         coordsAndDescription = Util
                 .splitCoordsAndDescription("37.258356797547,-122.0354267005");
         assertEquals("37.258356797547,-122.0354267005", coordsAndDescription[0]);
         assertEquals("", coordsAndDescription[1]);
     }
 
     @Test
     public void testSplitLatLon() {
         // http://en.wikipedia.org/wiki/Geographic_coordinate_conversion.
         splitLatLonHelper("40:26:46N,79:56:55W", "40:26:46N", "79:56:55W");
         splitLatLonHelper("40:26:46.302N 79:56:55.903W", "40:26:46.302N",
                 "79:56:55.903W");
         splitLatLonHelper("4026'21\"N 7958'36\"W", "4026'21\"N",
                 "7958'36\"W");
         splitLatLonHelper("4026'21\"S 7958'36\"E", "4026'21\"S",
                 "7958'36\"E");
         splitLatLonHelper("40d 26' 21\" N 79d 58' 36\" W", "40d 26' 21\" N",
                 "79d 58' 36\" W");
         splitLatLonHelper("40.446195N 79.948862W", "40.446195N", "79.948862W");
         splitLatLonHelper("40.446195, -79.948862", "40.446195", "-79.948862");
         splitLatLonHelper("40 26.7717, -79 56.93172", "40 26.7717",
                 "-79 56.93172");
 
         // Geocaching.com:
         splitLatLonHelper("N37 15.165 W 122 02.620", "N37 15.165",
                 "W 122 02.620");
         splitLatLonHelper("N+37+15.165+W+122+02.620+", "N+37+15.165+",
                 "W+122+02.620+");
 
         // atlasquest.com:
         splitLatLonHelper("37.258356797547,-122.0354267005", "37.258356797547",
                 "-122.0354267005");
 
         // opencaching.pl:
         splitLatLonHelper("52.029483333333 20.464366666667", "52.029483333333",
                 "20.464366666667");
 
     }
 
 }
