 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 /**
  *
  */
 package org.Transformer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Hashtable;
 import java.util.Map;
 
/** Parses Config Files
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class ConfigParser
 {
     private Hashtable<String, Hashtable<String, String>> parsedData;
 
     /**
      *
      */
    public ConfigParser(Reader cfg)
     {
         String curSection = "[]";
         parsedData = new Hashtable<String, Hashtable<String, String>>();
         try
         {
            BufferedReader br = new BufferedReader(cfg);
             String line = br.readLine();
             Hashtable<String, String> curSettings = null;
             while(null != line)
             {
                 line = line.trim();
                 if((true == line.startsWith("[")) && (true == line.endsWith("]")))
                 {
                     // New Section Discovered
                     curSection = line;
                 }
                 else
                 {
                     if(true == line.contains("="))
                     {
                         // found a setting
                         String key = line.substring(0, line.indexOf('='));
                         key = key.trim();
                         String value = line.substring(line.indexOf('=') + 1);
                         value = value.trim();
                         curSettings = parsedData.get(curSection);
                         if(null == curSettings)
                         {
                             curSettings = new Hashtable<String, String>();
                             parsedData.put(curSection, curSettings);
                         }
                         curSettings.put(key, value);
                     }
                 }
                 line = br.readLine();
             }
             br.close();
         }
        catch(IOException e)
         {
             e.printStackTrace();
         }
     }
 
    public Map<String, String> getSettingsOfSection(String Section)
     {
         return parsedData.get("[" + Section + "]");
     }
 
 }
