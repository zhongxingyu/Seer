 /*
 * $Id: CSVFormat.java,v 1.4 2005/12/16 11:31:30 oldman1004 Exp $
  *
  * Copyright(c) 2002 Infomata
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package com.infomata.data;
 
 import java.util.Iterator;
 
 /**
  * Implementation DataFormat interface for CSV formated files.
  *
  * @author <a href="mailto:skim@infomata.com">skim</a>
 * @version $Revision: 1.4 $
  */
 public class CSVFormat implements DataFormat {
 
      /** 
       * Converts a set of data into a line in data file.
       * @see com.infomata.data.DataFormat#format(DataRow)
       * @param row row of data to be converted in to String
       * @return properly CSV formatted line of data.
       */
     public String format(DataRow row) {
         
         StringBuffer o = new StringBuffer();
        boolean first = true;
         Iterator items = row.iterator();
         while (items.hasNext()) {
            if (first) {
                first = false;
            }
            else
            {
                 o.append(",");
             }
             String cell = (String)items.next();
             o.append(encode(cell));
         }
 
         return o.toString();
 
     } // format(DataRow)
 
   
     /** 
      * Parses a line of data properly formatted in CSV format
      * into set of data.
      * @see com.infomata.data.DataFormat#parseLine(String)
      * @param line CSV formated line of data
      * @return row of data
      */
     public DataRow parseLine(String line) {
 
         DataRow res = null;
 
         if (row == null) {
             row = new DataRow();
         }
         else {
             line = remainder + NEW_LINE + line;
         }
 
         int offset = 0;
         boolean paired = true;
 
         if (line != null) {
             char[] cs = line.toCharArray();
             for (int i = 0; i < cs.length; i++) {
                 if (cs[i] == QUOTE) {
                     paired = !paired;
                 }
                 else if (cs[i] == COMMA && paired) {
                     row.add(decode(cs, offset, i - offset));
                     offset = i + 1;
                 }
             }
             if (paired) {
                 if (offset < cs.length) {
                     row.add(decode(cs, offset, cs.length - offset));
                 }
                else if (cs[offset - 1] == COMMA)
                {
                    row.addEmpty();
                }
                 res = row;
                 row = null;
             }
             else {
                 remainder = new String(cs, offset, cs.length - offset);
             }
         }
 
         return res;
 
     } // parseLine(String)
 
 
     /**
      * Helper method to convert one data item into
      * CSV compliant format.
      *
      * @param datum data item
      * @return CSV encoded string
      */
     private String encode(String datum) {
 
         String res = null;
         if (datum == null) {
             res = "null";
         }
         else if (datum.length() == 0) {
             res = "";
         }
         else if (datum.indexOf(QUOTE) >= 0 
                  || datum.indexOf(COMMA) >= 0
                  || datum.indexOf(NEW_LINE) >= 0
                  || Character.isWhitespace(datum.charAt(0))) {
             
             StringBuffer o = new StringBuffer();
             o.append(QUOTE);
             
             char[] cs = datum.toCharArray();
             for (int i = 0; i < cs.length; i++) {
                 if (cs[i] == QUOTE) {
                     o.append(QUOTE); // add 1 more.
                 }
                 o.append(cs[i]);
             }
             
             o.append(QUOTE);
             res = o.toString();
             
         }
         else {
             res = datum;
         }
 
         return res;
 
     } // encode(String)
 
 
     /**
      * Helper method to remove CSV encoding from CSV formated
      * data cell.
      *
      * @param cs a <code>char[]</code> value
      * @param offset an <code>int</code> value
      * @param len an <code>int</code> value
      * @return a <code>String</code> value
      */
     private static String decode(char[] cs, int offset, int len) {
         
         while (Character.isWhitespace(cs[offset]) && offset < cs.length) {
             offset += 1;
             len -= 1;
         }
 
         int end = offset + len - 1;
         
         StringBuffer o = new StringBuffer();
 
         // decode escaped content.
         if (cs[offset] == QUOTE && cs[end] == QUOTE) {
 
             for (int i = offset + 1; i < end; i++) {
                 // remove doubled '"'
                 if (cs[i] == QUOTE) {
                     i++;
                 }
                 if (i < end) {
                     o.append(cs[i]);
                 }
             }
 
         }
         else {
             o.append(cs, offset, len);
         }
 
         return o.toString();
 
     } // decode(char[], int, int)
 
     private DataRow row = null;
     private String remainder = null;
 
     /**
      * Double quote
      */
     private static final char QUOTE = '"';
 
     /**
      * new line character.
      */
     private static final char NEW_LINE = '\n';
 
     /**
      * Delimiter (comma)
      */
     private static final char COMMA = ',';
     
 }
