 /*
  * This file is part of the rvt_irclogs project, a Jahia module to display IRC logs
  *
  * Copyright (C) 2010 R. van Twisk (rvt@dds.nl)
  *
  * This file may be distributed and/or modified under the terms of the
  * GNU General Public License version 2 as published by the Free Software
  * Foundation and appearing in the file gpl-2.0.txt included in the
  * packaging of this file.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *
  * This copyright notice MUST APPEAR in all copies of the script!
  */
 
 package org.jahia.modules.irclogs.eggdrop;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jahia.modules.irclogs.IRClogLine;
 import org.jahia.modules.irclogs.interfaces.ChatlogChannel;
 import org.jahia.modules.irclogs.interfaces.FilenameDateParser;
 import org.springframework.beans.factory.InitializingBean;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rvt
  * Date: 11/16/12
  * Time: 1:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class EggChatlogChannel implements ChatlogChannel, InitializingBean {
     private static Logger logger = Logger.getLogger(ChatlogChannel.class);
     private String channel;
     private String directory;
     private FilenameDateParser dateParser=null;
     private static final Pattern linePattern = Pattern.compile("\\[(\\d\\d:\\d\\d)\\].<(.*?)>(.+)");
 
 
 
     private Map<Integer, Map<Integer, Map<Integer, File>>> mapOfAvailableDates;
 
     EggChatlogChannel() {
         this.dateParser = new EgglogDateDDMMMYYYYParser();
     }
 
     /**
      * Return a list of available years within this chatlog
      *
      * @return
      */
     public List<Integer> getYears() {
         List<Integer> years = new ArrayList<Integer>(mapOfAvailableDates.keySet());
         Collections.sort(years);
         return years;
     }
 
     /**
      * Return a list of available months within a year within this chatlog
      *
      * @param year
      * @return
      */
     public List<Integer> getMonth(Integer year) {
         if (mapOfAvailableDates.get(year)!=null) {
             List<Integer> months = new ArrayList<Integer>(mapOfAvailableDates.get(year).keySet());
             Collections.sort(months);
             return months;
 
         } else {
             return Collections.emptyList();
         }
     }
 
     /**
      * Returns a list of available days within months within years withn this chatlog
      *
      * @param year
      * @param month
      * @return
      */
     public List<Integer> getDays(Integer year, Integer month) {
         if (getMonth(year).size()>0 && mapOfAvailableDates.get(year).get(month)!=null) {
             List<Integer> days = new ArrayList<Integer>(mapOfAvailableDates.get(year).get(month).keySet());
             Collections.sort(days);
             return days;
         } else {
             return Collections.emptyList();
         }
     }
 
     public List<IRClogLine> getLines(Integer year, Integer month, Integer day) {
         String[] lines = getLogData(year, month, day).split("\n");
         List<IRClogLine> parsedLines = new ArrayList<IRClogLine>();
 
         for (int lNum=0; lNum<lines.length; lNum++) {
             Matcher matcher = linePattern.matcher(lines[lNum]);
 
             if (matcher.find()) {
 
                 // get a time object
                 String[] timeParsed = matcher.group(1).split(":");
                 Calendar timeOfLine = (Calendar) Calendar.getInstance();
                 timeOfLine.set(Calendar.YEAR, year);
                 timeOfLine.set(Calendar.MONTH, month);
                 timeOfLine.set(Calendar.DAY_OF_MONTH, day);
                timeOfLine.set(Calendar.HOUR, Integer.parseInt(timeParsed[0]));
                 timeOfLine.set(Calendar.MINUTE, Integer.parseInt(timeParsed[1]));
 
                 IRClogLine entry;
                 entry = new IRClogLine(
                         timeOfLine,
                         StringUtils.replaceEach(matcher.group(2), new String[]{"&", "\"", "<", ">", "#"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;", "&#35;"}),
                         defaultLineReplacements(matcher.group(3)),
                         false);
 
 
                 parsedLines.add(entry);
             }
         }
         return parsedLines;
     }
 
     /**
      * Return a day's worth of logging as a string
      *
      * @param year
      * @param month
      * @param day
      * @return
      */
     private String getLogData(Integer year, Integer month, Integer day) {
 
         File file = mapOfAvailableDates.get(year).get(month).get(day);
         try {
             return FileUtils.readFileToString(file, "ISO-8859-1");
         } catch (IOException e) {
             logger.error("File " + file.getName() + " couldn't get opened!");
         }
         return "";
     }
 
     public void runJob() {
         long startTime = System.currentTimeMillis();
         WildcardFileFilter fileFilter = new WildcardFileFilter(channel + ".log.*");
         File fDirectory = new File(directory);
         Collection<File> files = FileUtils.listFiles(fDirectory, fileFilter, TrueFileFilter.INSTANCE);
         buildAvailabilityMap(files);
         long endTime = System.currentTimeMillis();
         logger.info("Processed channel " + channel + " with " + files.size() + " files. Done in " + (endTime - startTime) + " ms.");
     }
 
     /**
      * Builds a year/month/day availability map for this channel
      */
     private void buildAvailabilityMap(Collection<File> files) {
         mapOfAvailableDates = new HashMap<Integer, Map<Integer, Map<Integer, File>>>();
         for (File file : files) {
             Calendar filesDates = dateParser.getDate(file);
 
             if (filesDates != null) {
                 // Fetches Year
                 Map<Integer, Map<Integer, File>> monthData = mapOfAvailableDates.get(filesDates.get(Calendar.YEAR));
                 if (monthData == null) {
                     monthData = new HashMap<Integer, Map<Integer, File>>();
                     mapOfAvailableDates.put(filesDates.get(Calendar.YEAR), monthData);
                 }
 
                 // Fetches months
                 Map<Integer, File> dayData = monthData.get(filesDates.get(Calendar.MONTH));
                 if (dayData == null) {
                     dayData = new HashMap<Integer, File>();
                     monthData.put(filesDates.get(Calendar.MONTH), dayData);
                 }
 
                 dayData.put(filesDates.get(Calendar.DAY_OF_MONTH), file);
             }
         }
     }
 
     /**
      * This function replaces links and make the output html 'save'
      *
      * TODO: Make this more universal, possibly in a top level class somehow, or as a taglib?
      *
      * @param line
      * @return
      */
     private String defaultLineReplacements(String line) {
         final Pattern userPattern = Pattern.compile("Users/\\S+/?\\S*", Pattern.CASE_INSENSITIVE);
         final Pattern homePattern = Pattern.compile("Home/\\S+/?\\S*", Pattern.CASE_INSENSITIVE);
         final Pattern emailPattern = Pattern.compile("([_A-Za-z0-9-]+)(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})", Pattern.CASE_INSENSITIVE);
         final Pattern urlPattern = Pattern.compile("(\\A|\\s)((http|https|ftp):\\S+)(\\s|\\z)", Pattern.CASE_INSENSITIVE);
 
         line = userPattern.matcher(line).replaceAll("Users/<removed>/...");
         line = homePattern.matcher(line).replaceAll("Home/<removed>/...");
         line = emailPattern.matcher(line).replaceAll("(obscured mail address)");
 
         line = StringUtils.replaceEach(line, new String[]{"&", "\"", "<", ">", "#"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;", "&#35;"});
 
         line = urlPattern.matcher(line).replaceAll("$1<a target=\"_blank\" href=\"$2\">$2</a>$4");
         return line;
     }
 
     public String getChannel() {
         return channel;
     }
 
     public void setChannel(String channel) {
         this.channel = channel;
     }
 
     public String getDirectory() {
         return directory;
     }
 
     public void setDirectory(String directory) {
         this.directory = directory;
     }
 
     public void afterPropertiesSet() throws Exception {
         runJob();
     }
 }
