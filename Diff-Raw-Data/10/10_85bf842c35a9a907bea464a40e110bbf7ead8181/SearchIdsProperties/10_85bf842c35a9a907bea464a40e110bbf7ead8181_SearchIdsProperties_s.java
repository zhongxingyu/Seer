 /*
  * This file is part of SearchIds.
  *
  * Copyright © 2012-2013 Visual Illusions Entertainment
  *
  * SearchIds is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * SearchIds is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with SearchIds.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.searchids;
 
 import net.visualillusionsent.utils.PropertiesFile;
 
 public enum SearchIdsProperties {
     $;
 
     private PropertiesFile props;
     private boolean init;
 
     public static String searchType = "all";
     public static String dataXml = "config/SearchIds/search-ids-data.xml";
     public static String updateSource = "http://www.visualillusionsent.net/SearchIds/search-ids-data.xml";
    public static String updateSourceALT = "http://dl.dropbox.com/u/25586491/CanaryPlugins/SearchIds/search-ids-data.xml";
     public static boolean autoUpdate = true;
    public static String searchCommand = "search";
    public static String consoleCommand = "search";
     public static String base = "decimal";
     public static String baseId = "decimal";
     public static int nameWidth = 25;
     public static int numWidth = 4;
     public static String delimiter = "-";
     public static int autoUpdateInterval = 600000;
 
     private SearchIdsProperties() {}
 
     public static boolean initProps() {
         if ($.init) {
             return false;
         }
 
         $.props = new PropertiesFile("config/SearchIds/SearchIds.cfg");
         searchType = $.props.getString("search-type", searchType);
         base = $.props.getString("base", base);
        searchCommand = $.props.getString("command", searchCommand);
        consoleCommand = $.props.getString("console-command", consoleCommand);
         dataXml = $.props.getString("data-xml", dataXml);
         updateSource = $.props.getString("update-source", updateSource);
         updateSourceALT = $.props.getString("update-source-Alternate", updateSourceALT);
         autoUpdate = $.props.getBoolean("auto-update-data", autoUpdate);
         autoUpdateInterval = $.props.getInt("auto-update-interval", autoUpdateInterval);
         nameWidth = $.props.getInt("width-blockname", nameWidth);
         numWidth = $.props.getInt("width-number", numWidth);
         delimiter = $.props.getString("delimiter", delimiter);
         if (autoUpdateInterval < 60000) {
             autoUpdateInterval = 60000;
             //log.warning("[SearchIds] auto-update-interval cannot be less than 60000! auto-update-interval set to 60000");
         }
         return $.props != null;
     }
 
     public static String leftPad(String s, int width) {
         return String.format("%" + width + "s", s);
     }
 
     public static String rightPad(String s, int width) {
         return String.format("%-" + width + "s", s);
     }
 }
