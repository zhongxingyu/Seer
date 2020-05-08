 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Edmonton Public Library
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301, USA.
  *
  */
 package mecard.customer;
 
 import java.util.Date;
 import java.util.HashMap;
 import mecard.config.BImportTableTypes;
 
 /**
  * Single line of either a header or data file.
  * @author andrew
  */
 public class BImportTable implements FormattedTable
 {
     private final String tableName;
     private final HashMap<String, String> columns;
     // set so that we can check if the user has output the header then added
     // another value then tries to output the data, bimport will break.
     private boolean isPairAccessed;
     
     public static BImportTable getInstanceOf(BImportTableTypes type, HashMap<String, String> dataFields)
     {
         return new BImportTable(type.toString(), dataFields);
     }
 
     private BImportTable(String dbTable, HashMap<String, String> headDataMap)
     {
         this.columns = new HashMap<>();
         for (String key: headDataMap.keySet())
         {
             this.columns.put(key, headDataMap.get(key));
         }
         this.tableName      = dbTable;
         this.isPairAccessed = true;
     }
     
     /**
      * Adds or changes an a key in the table entry. If the key exists then the 
      * value for that key is updated and the return result is true. If the key
      * didn't exist, the key value pair are added, but the return value is false.
      * The return value is false if either the key of value is null, and no
      * changes are made to the table.
      * @param key
      * @param value
      * @return true if a key was found and updated and false if the key or value
      * could not be added because they were null, or the key and value pair did
      * not exist when they were added.
      */
     @Override
     public boolean setValue(String key, String value)
     {
         if (! isPairAccessed)
         {
             System.out.println(new Date() + " WARNING: a process has modified "
                     + "the table, the data is out of sync.");
         }
         boolean response = false;
         if (key != null && value != null)
         {
             response = this.columns.containsKey(key);
             this.columns.put(key, value);
         }
         return response;
     }
     
     /**
      * Returns the value associated with this key.
      * @param key
      * @return the value associated with this key, or an empty string if the key is not present.
      */
     @Override
     public String getValue(String key)
     {
         if (this.columns.containsKey(key))
         {
             return this.columns.get(key);
         }
         return "";
     }
     
     /**
      * 
      * @return properly formatted bimport header string for the table this represents.
      */
     @Override
     public String getHeader()
     {
         // x- borrower: second_id; name; expiration_date; birth_date
         String head = this.tableName + ": ";
        for (String sKeys: this.columns.keySet())
         {
            head += sKeys + "; ";
         }
         isPairAccessed = !isPairAccessed; 
         return finalizeLine(head);
     }
     
     /**
      * Creates matching.
      * @return properly formatted bimport data string for the table this represents.
      */
     @Override
     public String getData()
     {
         // M- borrower: 21221012345677; Balzac, Billy; 04-15-2014; 01-31-1998
         String data = this.tableName + ": ";
        for (String sKeys: this.columns.keySet())
         {
            data += this.columns.get(sKeys) + "; ";
         }
         isPairAccessed = !isPairAccessed;
         return finalizeLine(data);
     }
     
     /**
      * Cleans the return strings.
      * @param line to clean
      * @return clean line with proper termination.
      */
     private String finalizeLine(String line)
     {
         int lastSemiColonPos = line.lastIndexOf("; ");
         // bStat doesn't have a ';' so don't fail.
         if (lastSemiColonPos < 0)
         {
             return line + "\r\n";
         }
         return line.substring(0, lastSemiColonPos) + "\r\n";
     }
     
     @Override
     public String getName()
     {
         return this.tableName;
     }
 }
