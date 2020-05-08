 /*
  * Copyright (C) 2011  Southern Storm Software, Pty Ltd.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.southernstorm.tvguide;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlSerializer;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.text.SpannableString;
 import android.text.format.DateUtils;
 
 /**
  * Bookmark object.
  */
 public class TvBookmark implements Comparable<TvBookmark> {
 
     /** Day of week mask value that matches any day of the week */
     public static final int ANY_DAY_MASK = 0xFE;
     
     /** Day of week mask value that matches any day between Monday and Friday */
     public static final int MON_TO_FRI_MASK = 0x3E;
     
     /** Day of week mask value that matches Saturday and Sunday */
     public static final int SAT_AND_SUN_MASK = 0xC0;
 
     private class Range {
         public static final int INFINITE = 0x7FFFFFFF;
         public int first;
         public int last;
         
         public Range(int first, int last) {
             this.first = first;
             this.last = last;
         }
     }
 
     private String title;
     private String channelId;
     private int dayOfWeekMask;
     private int startTime;
     private int stopTime;
     private boolean anyTime;
     private boolean onAir;
     private int color;
     private List<Range> seasons;
     private List<Range> years;
     private long internalId;
     private static long nextInternalId = 0;
 
     /**
      * Constructs a new bookmark with default parameters.
      */
     public TvBookmark() {
         title = null;
         channelId = null;
         dayOfWeekMask = ANY_DAY_MASK;
         startTime = 18 * 60 * 60;
         stopTime = 23 * 60 * 60;
         anyTime = false;
         onAir = true;
         color = 0xFFFF0000;
         internalId = nextInternalId++;
     }
 
     public long getInternalId() {
         return internalId;
     }
     
     /**
      * Gets the title of the programme to match with this bookmark.
      * 
      * @return the programme title
      */
     public String getTitle() {
         return title;
     }
     
     /**
      * Sets the title of the programme to match with this bookmark.
      * Case is ignored when matching against programmes.
      * 
      * @param title the programme title
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * Gets the identifier of the channel to match with this bookmark.
      * 
      * @return the channel identifier
      */
     public String getChannelId() {
         return channelId;
     }
     
     /**
      * Sets the identifier of the channel to match with this bookmark.
      * The value should be null to match against any channel.
      * 
      * @param channelId the channel identifier
      */
     public void setChannelId(String channelId) {
         this.channelId = channelId;
     }
 
     /**
      * Gets the channel object for this bookmark's channel id.
      * 
      * @return the channel object, or null if not found
      */
     public TvChannel getChannel() {
         if (channelId != null)
             return TvChannelCache.getInstance().getChannel(channelId);
         else
             return null;
     }
     
     /**
      * Gets the mask to use for testing if a day matches this bookmark.
      * 
      * @return the current day mask
      */
     public int getDayOfWeekMask() {
         return dayOfWeekMask;
     }
     
     /**
      * Sets the mask to use for testing if a day matches this bookmark.
      * 
      * @param mask the new day mask
      */
     public void setDayOfWeekMask(int mask) {
         dayOfWeekMask = mask;
     }
 
     /**
      * Gets the day mask value corresponding to a single day.
      * 
      * @param day the day, Calendar.SUNDAY, Calendar.MONDAY, ...
      * @return the mask value
      */
     public static int getMaskForDay(int day) {
         if (day == Calendar.SUNDAY)
             day = 7;
         else
             day = day - 1;
         return 1 << day;
     }
 
     /**
      * Sets the day mask to a value corresponding to a single day.
      * 
      * @param day the day, Calendar.SUNDAY, Calendar.MONDAY, ...
      */
     public void setDayOfWeek(int day) {
         dayOfWeekMask = getMaskForDay(day);
     }
 
     private static String longDayName(int day) {
         if (day == 7)
             day = Calendar.SUNDAY;
         else
             day = day + 1;
         return DateUtils.getDayOfWeekString(day, DateUtils.LENGTH_LONG);
     }
     
     private static String shortDayName(int day) {
         if (day == 7)
             day = Calendar.SUNDAY;
         else
             day = day + 1;
         return DateUtils.getDayOfWeekString(day, DateUtils.LENGTH_MEDIUM);
     }
 
     /**
      * Gets the human-readable name of a day of week mask.
      * 
      * @param mask the day of week mask
      * @param longForm true to use long-form day names (e.g. Monday), false for short-form (e.g. Mon)
      * @return the name
      */
     public static String getDayOfWeekMaskName(int mask, boolean longForm) {
         String name = "";
         int day = 1;
         int endDay;
         if (mask == ANY_DAY_MASK)
             return "Any day";
         while (day <= 7) {
             if (mask == (1 << day)) {
                 // Single day - use the long form name, even if short specified.
                 name += longDayName(day);
                 ++day;
             } else if ((mask & (1 << day)) != 0) {
                 if (name.length() > 0) {
                     if (longForm)
                         name += " and ";
                     else
                         name += ',';
                 }
                 if ((mask & (1 << (day + 1))) != 0 && (mask & (1 << (day + 2))) != 0) {
                     // At least three days in a row are combined into
                     // Day1-DayN, instead of Day1,Day2,...,DayN.
                     endDay = day + 2;
                     while ((mask & (1 << (endDay + 1))) != 0)
                         ++endDay;
                     if (longForm) {
                         name += longDayName(day);
                         name += " to ";
                         name += longDayName(endDay);
                     } else {
                         name += shortDayName(day);
                         name += '-';
                         name += shortDayName(endDay);
                     }
                     day = endDay + 1;
                 } else {
                     if (longForm)
                         name += longDayName(day);
                     else
                         name += shortDayName(day);
                     ++day;
                 }
             } else {
                 ++day;
             }
         }
         if (name.length() == 0)
             return "No day";
         else
             return name;
     }
     
     /**
      * Gets the human-readable name of the day of week mask.
      * 
      * @param longForm true to use long-form day names (e.g. Monday), false for short-form (e.g. Mon)
      * @return the name
      */
     public String getDayOfWeekMaskName(boolean longForm) {
         return getDayOfWeekMaskName(dayOfWeekMask, longForm);
     }
     
     /**
      * Gets the starting time for the bookmark.
      * 
      * @return the start time, as the number of seconds since midnight
      */
     public int getStartTime() {
         return startTime;
     }
     
     /**
      * Sets the starting time for the bookmark.
      * 
      * @param time the start time, as the number of seconds since midnight
      */
     public void setStartTime(int time) {
         startTime = time;
     }
     
     /**
      * Gets the stopping time for the bookmark.
      * 
      * @return the stop time, as the number of seconds since midnight
      */
     public int getStopTime() {
         return stopTime;
     }
     
     /**
      * Sets the stopping time for the bookmark.
      * 
      * @param time the stop time, as the number of seconds since midnight
      */
     public void setStopTime(int time) {
         stopTime = time;
     }
     
     /**
      * Gets the "any time" flag, which indicates if any time of day will match.
      * 
      * @return true if any time of day should match, false to use the start and stop times
      */
     public boolean getAnyTime() {
         return anyTime;
     }
     
     /**
      * Sets the "any time" flag, which indicates if any time of day will match.
      * 
      * @param anyTime true if any time of day should match, false to use the start and stop times
      */
     public void setAnyTime(boolean anyTime) {
         this.anyTime = anyTime;
     }
     
     /**
      * Determine if this show is expected to be on air at the moment or not.
      * 
      * @return true if the show is expected to be on air, false if not
      */
     public boolean isOnAir() {
         return onAir;
     }
     
     /**
      * Sets the "on air" flag for this bookmark.
      * 
      * @param onAir true if the show is expected to be on air, false if not
      */
     public void setOnAir(boolean onAir) {
         this.onAir = onAir;
     }
     
     /**
      * Gets the color associated with this bookmark.
      * 
      * @return the color
      */
     public int getColor() {
         return color;
     }
 
     /**
      * Sets the color associated with this bookmark.
      * 
      * @param color the color
      */
     public void setColor(int color) {
         this.color = color;
     }
 
     public static String rangeListToString(List<Range> list) {
         if (list == null || list.size() == 0)
             return null;
         String result = "";
         for (int index = 0; index < list.size(); ++index) {
             int first = list.get(index).first;
             int last = list.get(index).last;
             if (index != 0)
                 result += ',';
             if (first == last) {
                 result += Integer.toString(first);
             } else if (last == Range.INFINITE) {
                 result += Integer.toString(first);
                 result += '+';
             } else {
                 result += Integer.toString(first);
                 result += '-';
                 result += Integer.toString(last);
             }
         }
         return result;
     }
     
     private class RangeParser {
         private String str;
         private int index;
         
         private static final int ST_End     = -1;
         private static final int ST_Error   = -2;
         private static final int ST_Dash    = -3;
         private static final int ST_Plus    = -4;
         private static final int ST_Comma   = -5;
         
         public RangeParser(String str) {
             this.str = (str != null ? str : "");
             this.index = 0;
         }
         
         private int nextToken() {
             while (index < str.length()) {
                 char ch = str.charAt(index++);
                 if (ch >= '0' && ch <= '9') {
                     int number = ch - '0';
                     while (index < str.length()) {
                         ch = str.charAt(index);
                         if (ch >= '0' && ch <= '9') {
                             number = number * 10 + ch - '0';
                             ++index;
                         } else {
                             break;
                         }
                     }
                     return number;
                 } else if (ch == '-') {
                     return ST_Dash;
                 } else if (ch == '+') {
                     return ST_Plus;
                 } else if (ch == ',') {
                     return ST_Comma;
                 } else if (ch != ' ' && ch != '\t') {
                     return ST_Error;
                 }
             }
             return ST_End;
         }
 
         List<Range> parse() {
             List<Range> list = new ArrayList<Range>();
             int token = nextToken();
             while (token != ST_End) {
                 if (token > 0) {
                     int first = token;
                     token = nextToken();
                     if (token == ST_Dash) {
                         token = nextToken();
                         if (token >= 0 && token >= first) {
                             list.add(new Range(first, token));
                             token = nextToken();
                         } else {
                             token = ST_Error;
                             break;
                         }
                     } else if (token == ST_Plus) {
                         list.add(new Range(first, Range.INFINITE));
                         token = nextToken();
                     } else {
                         list.add(new Range(first, first));
                     }
                 } else if (token != ST_Comma) {
                     break;
                 } else {
                     token = nextToken();
                 }
             }
             if (token != ST_End)
                 return null;
             else
                 return list;
         }
     }
 
     /**
      * Gets the season numbers that should match this bookmark.
      * 
      * @return the seasons, or null if any season should match
      */
     public String getSeasons() {
         return rangeListToString(seasons);
     }
     
     /**
      * Sets the season numbers that should match this bookmark.
      * 
      * @param seasons the seasons, or null if any season should match
      */
     public void setSeasons(String seasons) {
         RangeParser parser = new RangeParser(seasons);
         List<Range> range = parser.parse();
         if (range == null || range.size() == 0)
             this.seasons = null;
         else
             this.seasons = range;
     }
     
     /**
      * Gets the year numbers that should match this bookmark.
      * 
      * @return the years, or null if any year should match
      */
     public String getYears() {
         return rangeListToString(years);
     }
     
     /**
      * Sets the year numbers that should match this bookmark.
      * 
      * @param years the years, or null if any year should match
      */
     public void setYears(String years) {
         RangeParser parser = new RangeParser(years);
         List<Range> range = parser.parse();
         if (range == null || range.size() == 0)
             this.years = null;
         else
             this.years = range;
     }
 
     /**
      * Determine if a string is a valid range string for use with setSeasons() or setYears().
      * 
      * @param str the string
      * @return true if the string is valid, false otherwise
      */
     public boolean isValidRangeString(String str) {
         RangeParser parser = new RangeParser(str);
         return parser.parse() != null;
     }
 
     /**
      * Gets the time of day of a date value in seconds since midnight.
      * 
      * @param date the date
      * @return seconds since midnight of the date
      */
     public static int getTimeOfDay(Calendar date) {
         int hour = date.get(Calendar.HOUR_OF_DAY);
         int minute = date.get(Calendar.MINUTE);
         int second = date.get(Calendar.SECOND);
         return hour * 60 * 60 + minute * 60 + second;
     }
 
     /**
      * Matches a programme against this bookmark.
      * 
      * @param programme the programme
      * @return the result of the matching process
      */
     public TvBookmarkMatch match(TvProgramme programme) {
         TvBookmarkMatch result = TvBookmarkMatch.FullMatch;
         boolean should = false;
     
         if (title == null)
             return TvBookmarkMatch.NoMatch;
         if (!title.equalsIgnoreCase(programme.getTitle())) {
             if (channelId != null && !programme.getChannel().isSameChannel(channelId))
                 return TvBookmarkMatch.NoMatch;
             should = true;
             result = TvBookmarkMatch.ShouldMatch;
         } else {
             if (channelId != null && !programme.getChannel().isSameChannel(channelId))
                return TvBookmarkMatch.TitleMatch;
         }
     
         // Check that start and stop times are within the expected range.
         int start = getTimeOfDay(programme.getStart());
         int stop = getTimeOfDay(programme.getStop());
         int dayOfWeekMask = this.dayOfWeekMask;
         if (anyTime) {
             // If we are matching at any time of day, then don't show
             // failed matches.  Otherwise everything will show as failed!
             if (result == TvBookmarkMatch.ShouldMatch)
                 return TvBookmarkMatch.NoMatch;
         } else if (startTime < stopTime) {
             if (start < startTime) {
                 if (stop > startTime)
                     result = TvBookmarkMatch.Underrun;
                 else
                     result = TvBookmarkMatch.TitleMatch;
             } else if (start >= stopTime) {
                 result = TvBookmarkMatch.TitleMatch;
             } else if (stop < startTime || stop > stopTime) {
                 result = TvBookmarkMatch.Overrun;
             }
         } else {
             if (start >= stopTime && start < startTime) {
                 if (stop > startTime || stop <= stopTime)
                     result = TvBookmarkMatch.Underrun;
                 else if (stop >= stopTime && stop < start)
                     result = TvBookmarkMatch.Underrun;
                 else
                     result = TvBookmarkMatch.TitleMatch;
             } else if (start < stopTime) {
                 // Adjust the expected weekday - start time is in tomorrow.
                 // We do this by rotating the day mask left by one position.
                 dayOfWeekMask = ((dayOfWeekMask << 1) |
                                  (dayOfWeekMask >> 6)) & 0xFE;
                 if (stop > stopTime)
                     result = TvBookmarkMatch.Overrun;
             } else {
                 if (stop > stopTime && stop < startTime)
                     result = TvBookmarkMatch.Overrun;
             }
         }
     
         // Validate the weekday.
         int weekday = programme.getStart().get(Calendar.DAY_OF_WEEK);
         if (weekday == Calendar.SUNDAY)
             weekday = 7;
         else
             weekday = weekday - 1;
         if ((dayOfWeekMask & (1 << weekday)) == 0)
             result = TvBookmarkMatch.TitleMatch;
     
         // Deal with non-matching bookmarks that cover the same timeslot.
         if (should && (result == TvBookmarkMatch.Underrun || result == TvBookmarkMatch.Overrun))
             result = TvBookmarkMatch.ShouldMatch;
         if (should && result != TvBookmarkMatch.ShouldMatch)
             result = TvBookmarkMatch.NoMatch;
     
         // Off-air bookmarks don't show failed matches.
         if (!onAir && result == TvBookmarkMatch.ShouldMatch)
             result = TvBookmarkMatch.NoMatch;
     
         // Match the season number.
         if (seasons != null && result != TvBookmarkMatch.ShouldMatch &&
                 result != TvBookmarkMatch.NoMatch) {
             int season = programme.getSeason();
             int index;
             if (season != 0) {
                 for (index = 0; index < seasons.size(); ++index) {
                     if (season >= seasons.get(index).first &&
                             season <= seasons.get(index).last)
                         break;
                 }
                 if (index >= seasons.size())
                     result = TvBookmarkMatch.NoMatch;
             } else {
                 // If the programme does not have a season, then match
                 // it against a bookmark with N+ as one of the ranges.
                 // Usually the programme does not have a season number
                 // because it is a new episode in the most recent season
                 // and the upstream XMLTV database doesn't have a season
                 // and episode number for it yet.
                 for (index = 0; index < seasons.size(); ++index) {
                     if (seasons.get(index).last == Range.INFINITE)
                         break;
                 }
                 if (index >= seasons.size())
                     result = TvBookmarkMatch.NoMatch;
             }
         }
     
         // Match the year number.
         if (years != null && result != TvBookmarkMatch.ShouldMatch &&
                 result != TvBookmarkMatch.NoMatch) {
             int year = programme.getYear();
             int index;
             if (year != 0) {
                 for (index = 0; index < years.size(); ++index) {
                     if (year >= years.get(index).first &&
                             year <= years.get(index).last)
                         break;
                 }
                 if (index >= years.size())
                     result = TvBookmarkMatch.NoMatch;
             } else {
                 // If the programme does not have a year, then match
                 // it against a bookmark with N+ as one of the ranges.
                 for (index = 0; index < years.size(); ++index) {
                     if (years.get(index).last == Range.INFINITE)
                         break;
                 }
                 if (index >= years.size())
                     result = TvBookmarkMatch.NoMatch;
             }
         }
     
         return result;
     }
 
     /**
      * Loads the bookmark details from an XML input stream.
      *
      * When this method exits, the parser will be positioned just after
      * the bookmark end element.
      *
      * @param parser Pull parser containing the input.  Must be positioned
      * on the bookmark element.
      * @throws XmlPullParserException error in xml data
      * @throws IOException error reading the xml data
      */
     public void loadFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
         title = null;
         channelId = null;
         dayOfWeekMask = ANY_DAY_MASK;
         startTime = 18 * 60 * 60;
         stopTime = 23 * 60 * 60;
         anyTime = false;
         onAir = true;
         color = 0xFFFF0000;
         seasons = null;
         years = null;
         int eventType = parser.next();
         while (eventType != XmlPullParser.END_DOCUMENT) {
             if (eventType == XmlPullParser.START_TAG) {
                 String name = parser.getName();
                 if (name.equals("title")) {
                     title = Utils.getContents(parser, name);
                 } else if (name.equals("channel-id")) {
                     channelId = Utils.getContents(parser, name);
                 } else if (name.equals("days")) {
                     String days = Utils.getContents(parser, name);
                     if (days != null)
                         dayOfWeekMask = Integer.valueOf(days);
                 } else if (name.equals("start-time")) {
                     startTime = parseTime(Utils.getContents(parser, name));
                 } else if (name.equals("stop-time")) {
                     stopTime = parseTime(Utils.getContents(parser, name));
                 } else if (name.equals("any-time")) {
                     anyTime = true;
                 } else if (name.equals("off-air")) {
                     onAir = false;
                 } else if (name.equals("color")) {
                     color = Color.parseColor(Utils.getContents(parser, name));
                 } else if (name.equals("seasons")) {
                     setSeasons(Utils.getContents(parser, name));
                 } else if (name.equals("years")) {
                     setYears(Utils.getContents(parser, name));
                 }
             } else if (eventType == XmlPullParser.END_TAG && parser.getName().equals("bookmark")) {
                 break;
             }
             eventType = parser.next();
         }
     }
 
     /**
      * Formats a color value as #RRGGBB.
      * 
      * @param color the color value
      * @return the formatted version of the color
      */
     private static String formatColor(int color) {
         String result = "#";
         int shift = 24;
         while (shift > 0) {
             shift -= 4;
             int value = (color >> shift) & 0x0F;
             if (value >= 10)
                 result += (char)('a' + value - 10);
             else
                 result += (char)('0' + value);
         }
         return result;
     }
 
     /**
      * Saves this bookmark to an XML stream.
      * 
      * @param serializer the serializer for the XML stream
      * @throws IOException failed to write to the XML stream
      */
     public void saveToXml(XmlSerializer serializer) throws IOException {
         serializer.startTag(null, "bookmark");
         Utils.writeContents(serializer, "title", title);
         Utils.writeContents(serializer, "channel-id", channelId);
         Utils.writeContents(serializer, "days", Integer.toString(dayOfWeekMask));
         Utils.writeContents(serializer, "start-time", formatTime(startTime));
         Utils.writeContents(serializer, "stop-time", formatTime(stopTime));
         if (anyTime)
             Utils.writeEmptyTag(serializer, "any-time");
         if (!onAir)
             Utils.writeEmptyTag(serializer, "off-air");
         Utils.writeContents(serializer, "color", formatColor(color));
         if (seasons != null)
             Utils.writeContents(serializer, "seasons", getSeasons());
         if (years != null)
             Utils.writeContents(serializer, "years", getYears());
         serializer.endTag(null, "bookmark");
     }
     
     private static int parseTime(String str) {
         if (str == null)
             return 0;
         int hour = 0;
         int minute = 0;
         int second = 0;
         int field = 0;
         int value = 0;
         for (int index = 0; index < str.length(); ++index) {
             char ch = str.charAt(index);
             if (ch >= '0' && ch <= '9') {
                 value = value * 10 + ch - '0';
             } else if (ch == ':') {
                 if (field == 0)
                     hour = value;
                 else if (field == 1)
                     minute = value;
                 else
                     second = value;
                 value = 0;
                 ++field;
             }
         }
         if (field == 0)
             hour = value;
         else if (field == 1)
             minute = value;
         else
             second = value;
         return hour * 60 * 60 + minute * 60 + second;
     }
     
     private static String formatTime(int time) {
         int hour = time / (60 * 60);
         int minute = (time / 60) % 60;
         int second = time % 60;
         String result = "";
         result += (char)('0' + (hour / 10));
         result += (char)('0' + (hour % 10));
         result += ':';
         result += (char)('0' + (minute / 10));
         result += (char)('0' + (minute % 10));
         if (second != 0) {
             result += ':';
             result += (char)('0' + (second / 10));
             result += (char)('0' + (second % 10));
         }
         return result;
     }
 
     private int compareTimes(int t1, int t2, TvBookmark other) {
         if (this.anyTime) {
             if (other.getAnyTime())
                 return 0;
             else
                 return -1;
         } else if (other.getAnyTime()) {
             return 1;
         } else if (t1 < t2) {
             return -1;
         } else if (t1 > t2) {
             return 1;
         } else {
             return 0;
         }
     }
 
     public int compareTo(TvBookmark other) {
         // Compare on title, then day, then time, then channel.
         int cmp = Utils.stringCompareIgnoreCase(this.title, other.getTitle());
         if (cmp != 0)
             return cmp;
         if (this.dayOfWeekMask < other.getDayOfWeekMask())
             return -1;
         else if (this.dayOfWeekMask > other.getDayOfWeekMask())
             return 1;
         cmp = compareTimes(this.startTime, other.getStartTime(), other);
         if (cmp != 0)
             return cmp;
         cmp = compareTimes(this.stopTime, other.getStopTime(), other);
         if (cmp != 0)
             return cmp;
         return Utils.stringCompareIgnoreCase(this.channelId, other.getChannelId());
     }
     /**
      * Returns the formatted description of the bookmark to display in list views.
      * 
      * @param context the application context for resolving images
      * @return the description as a formatted SpannableString
      */
     public SpannableString getFormattedDescription(Context context) {
         RichTextFormatter formatter = new RichTextFormatter(context);
         formatter.setColor(color);
         formatter.append(title);
         if (seasons != null) {
             formatter.append(", Season ");
             formatter.append(getSeasons());
         }
         if (years != null) {
             formatter.append(", ");
             formatter.append(getYears());
         }
         formatter.nl();
         formatter.setColor(0xFF000000);
         formatter.append(getDayOfWeekMaskName(false));
         formatter.append(", ");
         if (anyTime) {
             formatter.append("Any time");
         } else {
             formatter.append(Utils.formatTime(startTime));
             formatter.append(" to ");
             formatter.append(Utils.formatTime(stopTime));
         }
         if (channelId != null) {
             formatter.append(", ");
             TvChannel channel = getChannel();
             if (channel != null)
                 formatter.append(channel.getName());
             else
                 formatter.append(channelId);
         } else {
             formatter.append(", Any channel");
         }
         if (!onAir)
             formatter.append(", Off Air");
         return formatter.toSpannableString();
     }
 }
