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
 
 import android.net.UrlQuerySanitizer;
 import android.net.UrlQuerySanitizer.ValueSanitizer;
 
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Util {
     public static final String[] geocachingQueryParam = new String[] {
         "q"
     };
 
     // #Wildwood Park, Saratoga, CA(The Nut Case #89882)
     private static final Pattern PAT_ATLASQUEST = Pattern.compile(".*\\((.*)#(.*)\\)");
     private static final Pattern PAT_ATSIGN_FORMAT = Pattern.compile("([^@]*)@(.*)");
 
     private static final Pattern PAT_COORD_COMPONENT = Pattern.compile("([\\d.]+)[^\\d]*");
     private static final Pattern PAT_LATLON = Pattern
             .compile("([NS]?[^NSEW,]*[NS]?),?\\s*([EW]?.*)");

     private static final Pattern PAT_NEGSIGN = Pattern.compile("[-WS]");
     private static final Pattern PAT_PAREN_FORMAT = Pattern.compile("([^(]*)\\(([^)]*).*");
     private static final Pattern PAT_SIGN = Pattern.compile("[-EWNS]");
 
     public static CharSequence formatDegreesAsDecimalDegreesString(double fDegrees) {
         final double fAbsDegrees = Math.abs(fDegrees);
         final int dAbsDegrees = (int)fAbsDegrees;
         return String.format(Locale.US, (fDegrees < 0 ? "-" : "") + "%1$d %2$06.3f", dAbsDegrees,
                 60.0 * (fAbsDegrees - dAbsDegrees));
     }
 
     public static double parseCoordinate(CharSequence string) {
         int sign = 1;
         final Matcher negsignMatcher = PAT_NEGSIGN.matcher(string);
         if (negsignMatcher.find()) {
             sign = -1;
         }
 
         final Matcher signMatcher = PAT_SIGN.matcher(string);
         string = signMatcher.replaceAll("");
 
         final Matcher dmsMatcher = PAT_COORD_COMPONENT.matcher(string);
         double degrees = 0.0;
         for (double scale = 1.0; scale <= 3600.0 && dmsMatcher.find(); scale *= 60.0) {
             degrees += Double.parseDouble(dmsMatcher.group(1)) / scale;
         }
         return sign * degrees;
     }
 
     public static CharSequence[] parseDescription(CharSequence string) {
         Matcher matcher = PAT_ATLASQUEST.matcher(string);
         if (matcher.matches())
             return new CharSequence[] {
                     "LB" + matcher.group(2).trim(), matcher.group(1).trim()
             };
         return new CharSequence[] {
                 string, ""
         };
     }
 
     public static CharSequence parseHttpUri(String query, UrlQuerySanitizer sanitizer,
             ValueSanitizer valueSanitizer) {
         sanitizer.registerParameters(geocachingQueryParam, valueSanitizer);
         sanitizer.parseQuery(query);
         return sanitizer.getValue("q");
     }
 
     public static CharSequence[] splitCoordsAndDescription(CharSequence location) {
         Matcher matcher = PAT_ATSIGN_FORMAT.matcher(location);
         if (matcher.matches()) {
             return new CharSequence[] {
                     matcher.group(2).trim(), matcher.group(1).trim()
             };
         }
         matcher = PAT_PAREN_FORMAT.matcher(location);
         if (matcher.matches()) {
             return new CharSequence[] {
                     matcher.group(1).trim(), matcher.group(2).trim()
             };
         }
         // No description.
         return new CharSequence[] {
                 location, ""
         };
     }
 
     public static CharSequence[] splitLatLon(CharSequence string) {
         Matcher matcher = PAT_LATLON.matcher(string);
         if (matcher.matches())
             return new String[] {
                     matcher.group(1).trim(), matcher.group(2).trim()
             };
         return null;
     }
 
     public static CharSequence[] splitLatLonDescription(CharSequence location) {
         CharSequence coordsAndDescription[] = splitCoordsAndDescription(location);
         CharSequence latLon[] = splitLatLon(coordsAndDescription[0]);
 
         CharSequence[] parseDescription = parseDescription(coordsAndDescription[1]);
         return new CharSequence[] {
                 latLon[0], latLon[1], parseDescription[0], parseDescription[1]
         };
     }
 
 }
