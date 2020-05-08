 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  *  Copyright (C) 2001 Myles Chippendale
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Myles Chippendale <mylesc@paneris.org>
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense. 
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.poem.csv;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.Writer;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.NoSuchElementException;
 import java.util.Vector;
 import java.util.Hashtable;
 import org.melati.poem.Table;
 import org.melati.poem.Persistent;
 import org.melati.util.CSVFileParser;
 
 /**
  * A representation of a CSV file as a POEM Table.
  */
 public class CSVTable {
 
   protected Table table = null;
   protected File data = null;
   protected Hashtable columns = new Hashtable();
   protected Vector columnsInUploadOrder = new Vector();
   protected CSVColumn primaryKey = null;
   protected Vector records = new Vector();
   /** The line number of the CSV file */
   private int lineNo;
   
   /** The record number of the CSV file */
   private int recordNo;
 
 
  /**
   * Constructor.
   * 
   * @param table POEM table to load data into
   * @param data CSV file to read from
   */
   public CSVTable(Table table, File data) {
     this.table = table;
     this.data = data;
   }
 
  /**
   * Add column definitions to this table
   */
   public void addColumn(String csvName, String poemName) {
     columns.put(csvName, new CSVColumn(poemName));
   }
 
  /**
   * Add column definitions, perhaps Primary Keys, to this table
   */
   public void addColumn(String csvName, String poemName, boolean isPrimaryKey)
       throws CSVPrimaryKeyColumnAlreadySetException {
     if (isPrimaryKey && primaryKey != null)
       throw new CSVPrimaryKeyColumnAlreadySetException(table.getName());
     CSVColumn col = new CSVColumn(poemName, isPrimaryKey);
     columns.put(csvName, col);
     if (isPrimaryKey)
       primaryKey = col;
   }
 
  /**
   * Add column definitions for foreign keys to this table
   */
   public void addColumn(String csvName, String poemName, 
                         CSVTable foreignTable) {
     columns.put(csvName, new CSVColumn(poemName, foreignTable));
   }
 
 
  /**
   * Parse the CSV data file and store the data for saving later
   */
   public void load() throws IOException, CSVParseException {
 
     BufferedReader reader = new BufferedReader(new FileReader(data));
     CSVFileParser parser = new CSVFileParser(reader);
 
     // 1st line contains the field names - this needs to be
     // validated againsed expected values, and the order of
     // the fields established
     parser.nextRecord();
     lineNo = 1;
     recordNo = 0;
 
     try {
       while (parser.recordHasMoreFields()) {
 
         String key = parser.nextField();
         Object col = columns.get(key);
         if (col == null)
           throw new CSVParseException(
             "I don't know what to do with the column in " + data.getPath() +
             " called " + key); 
         columnsInUploadOrder.addElement(col);
       }
 
       // Suck in all the data
       CSVRecord record;
       while(null != (record = parseRecord(parser, data.getPath()))) {
         record.lineNo = lineNo++;
         record.recordNo = recordNo++;
         records.addElement(record);
       }
 
     }
     catch (IllegalArgumentException e) {
       throw new CSVParseException("Failed to read column header in " +
                                     data.getPath() + 
                                     " line " + lineNo +
                                     ": " + e.toString());
     }
     catch (NoSuchElementException f) {
        throw new CSVParseException("Failed to read column header in " +
                                     data.getPath() + 
                                     " line " + lineNo +
                                     ": " + f.toString());
     }
     finally {
       reader.close();
     }
   }
 
 
  /**
   * Reads the file until is has seen an object's-worth of
   * field values (ie until it sees an EOF or a line starting
   * with '$') which it returns in a hashtable (null if there are
   * no field values).
   */
   private CSVRecord parseRecord(CSVFileParser parser, String fileName)
                                     throws IOException, CSVParseException {
     if (!parser.nextRecord())
       return null;
 
     int i = 0;
     String value = null;
     try {
       CSVRecord record = new CSVRecord(table);
       for (; i < columnsInUploadOrder.size(); i++) {
         value = parser.nextField();
         CSVColumn col = (CSVColumn)columnsInUploadOrder.elementAt(i);
         record.addField(new CSVField(col, value));
       }
 
       return record;
     }
     catch (IllegalArgumentException e) {
        throw new CSVParseException("Failed to read data field no. " + 
                                     (i+1) + 
                                     " in " +
                                     fileName + 
                                     " line " + lineNo +
                                     ": " + e.toString());
     }
     catch (NoSuchElementException f) {
       String message = "Problem with data field no. " + (i+1) + 
       " in " + fileName +
       " line " + lineNo;
       
       if (value == null) {
         message += " (Check last line of file) : " + 
                     f.toString();
       } else {
         message += ", Value:" + value + ": " + f.toString();
       }
       throw new CSVParseException(message);
     }
   }
 
 
  /**
   * Delete all Persistents from the Poem table
   */
   public void emptyTable() {
     Enumeration rows = table.selection();
     while(rows.hasMoreElements()) {
       Persistent p = (Persistent)rows.nextElement();
       p.delete();
     }
   }
 
  /**
   * Write the records to the database.
   */
   public void writeRecords() 
       throws NoPrimaryKeyInCSVTableException,  CSVWriteDownException {
     for (int i = 0; i < records.size(); i++) {
       try {
       ((CSVRecord)records.elementAt(i)).makePersistent();
       } catch (NoPrimaryKeyInCSVTableException e1) {
         throw e1;
       } catch (Exception e) {
         e.printStackTrace(System.err);
         throw new CSVWriteDownException (table.getName(),i+2,e);
       }
     }
   }
 
   /**
    * Lookup the Persistent corresponding to the CSV record
    * with the given value for the CSV table's primary key.
    */
   protected Persistent getRecordWithID(String csvValue)
                                      throws NoPrimaryKeyInCSVTableException {
     if (primaryKey == null)
       throw new NoPrimaryKeyInCSVTableException(table.getName(), csvValue);
 
     for (int i = 0; i < records.size(); i++) {
       CSVRecord record = (CSVRecord)records.elementAt(i);
       if (record.primaryKeyValue != null &&
           record.primaryKeyValue.equals(csvValue))
         return record.getPersistent();
     }
     return null;
   }
 
  /**
   * Return a string reporting on the data added to this table.
   */
   public void report(boolean recordDetails, boolean fieldDetails,
                        Writer output) throws IOException {
 
     output.write("*** TABLE: " + table.getName().toUpperCase() + " **\n\n");
     output.write("** I have read " + records.size() + " records of " +
                    columnsInUploadOrder.size() + " fields\n");
 
     if (recordDetails) {
       for (int i = 0; i < records.size(); i++) {
         CSVRecord record = (CSVRecord)records.elementAt(i);
         output.write("   Record: CSV primary key = " +
                         record.primaryKeyValue);
 
         if (record.poemRecord == null)
           output.write(", No Poem Persistent written\n");
         else
           output.write(", Poem Troid = " + record.poemRecord.troid() + "\n");
 
         if (fieldDetails) {
           for (int j = 0; j < record.size(); j++) {
             CSVField field = (CSVField)record.elementAt(j);
             output.write(field.column + "=\"" + field.value);
             if (j < record.size()-1)
               output.write("\",");
             else
               output.write("\"\n");
           }
         }
       }
     }
     output.write("** Currently " + table.count(null) +
                   " Persistents in this table\n\n");
   }
 
 }
 
