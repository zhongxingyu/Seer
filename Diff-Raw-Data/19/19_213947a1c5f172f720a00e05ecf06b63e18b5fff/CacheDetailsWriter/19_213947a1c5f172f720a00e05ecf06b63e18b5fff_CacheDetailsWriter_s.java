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
 
 package com.google.code.geobeagle.cachedetails;
 
 import com.google.code.geobeagle.activity.main.Util;
 import com.google.inject.Inject;
 
 import android.content.Context;
 import android.util.Log;
 
 import java.io.IOException;
 import java.text.ParseException;
 
 public class CacheDetailsWriter {
     public static String replaceIllegalFileChars(String wpt) {
         return wpt.replaceAll("[<\\\\/:\\*\\?\">| \\t]", "_");
     }
 
     private final Context context;
     private final Emotifier emotifier;
     private final HtmlWriter htmlWriter;
     private String latitude;
     private int logNumber;
     private String longitude;
     private String time;
     private String finder;
     private String logType;
     private String relativeTime;
     private final RelativeDateFormatter relativeDateFormatter;
 
     @Inject
     public CacheDetailsWriter(HtmlWriter htmlWriter,
             Emotifier emotifier,
             Context context,
             RelativeDateFormatter relativeDateFormatter) {
         this.htmlWriter = htmlWriter;
         this.emotifier = emotifier;
         this.context = context;
         this.relativeDateFormatter = relativeDateFormatter;
     }
 
     public void close() throws IOException {
         htmlWriter.writeFooter();
         htmlWriter.close();
         latitude = longitude = time = finder = logType = relativeTime = "";
         logNumber = 0;
     }
 
     public void latitudeLongitude(String latitude, String longitude) {
         this.latitude = (String)Util.formatDegreesAsDecimalDegreesString(Double.valueOf(latitude));
         this.longitude = (String)Util
                 .formatDegreesAsDecimalDegreesString(Double.valueOf(longitude));
     }
 
     public void logType(String trimmedText) {
         logType = Emotifier.ICON_PREFIX + "log_" + trimmedText.replace(' ', '_').replace('\'', '_')
                 + Emotifier.ICON_SUFFIX;
     }
 
     public void placedBy(String text) throws IOException {
         Log.d("GeoBeagle", "PLACED BY: " + time);
         String on = "";
         try {
             on = relativeDateFormatter.getRelativeTime(context, time);
         } catch (ParseException e) {
             on = "PARSE ERROR";
         }
         writeField("Placed by", text);
         writeField("Placed on", on);
     }
 
     public void wptTime(String time) {
         this.time = time;
     }
 
     public void writeField(String fieldName, String field) throws IOException {
         htmlWriter.writeln("<font color=grey>" + fieldName + ":</font> " + field);
     }
 
     public void writeHint(String text) throws IOException {
         htmlWriter
                .write("<a class=hint id=hint_link onclick=\"dht('hint_link');return false;\" href=#>"
                         + "Encrypt</a>");
        htmlWriter.write("<div id=hint_link_text>" + text + "</div>");
     }
 
     public void writeLine(String text) throws IOException {
         htmlWriter.writeln(text);
     }
 
     public void writeLogDate(String text) throws IOException {
         htmlWriter.writeSeparator();
         try {
             relativeTime = relativeDateFormatter.getRelativeTime(context, text);
         } catch (ParseException e) {
             htmlWriter.writeln("error parsing date: " + e.getLocalizedMessage());
         }
     }
 
     public void writeLogText(String text, boolean encoded) throws IOException {
         String f;
         htmlWriter.writeln("<b>" + logType + " " + relativeTime + " by " + finder + "</b>");
         if (encoded)
             f = "<a class=hint id=log_%1$s onclick=\"dht('log_%1$s');return false;\" "
                    + "href=#>Encrypt</a><div id=log_%1$s_text>%2$s</div>";
         else
             f = "%2$s";
 
         htmlWriter.writeln(String.format(f, logNumber++, emotifier.emotify(text)));
     }
 
     public void writeLongDescription(String trimmedText) throws IOException {
         htmlWriter.write(trimmedText);
         htmlWriter.writeSeparator();
     }
 
     public void writeName(String name) throws IOException {
         htmlWriter.write("<center><h3>" + name + "</h3></center>\n");
     }
 
     public void writeShortDescription(String trimmedText) throws IOException {
         htmlWriter.writeSeparator();
         htmlWriter.writeln(trimmedText);
         htmlWriter.writeln("");
     }
 
     public void writeWptName() throws IOException {
         htmlWriter.open(null);
         htmlWriter.writeHeader();
         writeField("Location", latitude + ", " + longitude);
         latitude = longitude = null;
     }
 
     public void writeLogFinder(String finder) {
         this.finder = finder;
     }
 
 }
