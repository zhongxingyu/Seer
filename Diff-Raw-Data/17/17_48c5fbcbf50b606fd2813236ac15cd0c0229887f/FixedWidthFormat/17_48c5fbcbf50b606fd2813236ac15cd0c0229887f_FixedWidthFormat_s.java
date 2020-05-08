 /*
  * $Id: FixedWidthFormat.java,v 1.2 2005/12/19 12:31:29 oldman1004 Exp $
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
  * Data file format that uses fixed width to define each item
  * per row of data.
  *
  * @author <a href="mailto:oldman1004@gmail.com">Sam Kim</a>
  * @version $Revision: 1.2 $
  */
 public class FixedWidthFormat implements DataFormat {
 
     /**
      * Creates a new <code>FixedWidthFormat</code> instance.
      *
      * @param widths list of width of each item contained in
      *               data file in correct order.
      */
     public FixedWidthFormat(int[] widths) {
 
         beg = new int[widths.length];
         width = new int[widths.length];
 
         System.arraycopy(widths, 0, width, 0, widths.length);
         System.arraycopy(widths, 0, beg, 1, widths.length - 1);
 
         if (beg.length > 2) {
             for (int i = 2; i < beg.length; i++) {
                 beg[i] += beg[i - 1];
             }
         }
     }
 
     /**
      * Parses a line of text containing
      * data contained according to pre-defined character widths.
      *
      * @param line <code>String</code> object containing data.
      * @return <code>DataRow</code> object containing data parsed
      *         from <code>line</code>.
      */
     public DataRow parseLine(String line) {
 
         DataRow row = null;
 
         if (line != null) {
             row = new DataRow();
             for (int i = 0; i < width.length; i++) {
                 if (i == width.length - 1) {
                     row.add(line.substring(beg[i]).trim());
                 }
                else {
                    String val = line.substring(beg[i], beg[i + 1]);
                    row.add(val.trim());
                }
             }
         }
 
         return row;
     }
 
     /**
      * Formats the data contained in <code>DataRow</code> object
      * into a <code>String</code> according to pre-defined
      * widths.
      *
      * @param row <code>DataRow</code> containing data to be written
      *            to file.
      * @return line of text containing data in specified widths.
      */
     public String format(DataRow row) {
 
         StringBuffer o = new StringBuffer();
         Iterator i = row.iterator();
         int cnt = 0;
 
         while (i.hasNext() && cnt < beg.length) {
 
             String c = (String)i.next();
 
             int diff = width[cnt] - c.length();
 
             if (diff > 0) {
                 o.append(c);
                 for (int j = 0; j < diff; j++) {
                     o.append(SPACE);
                 }
             }
             else if (diff < 0) {
                 o.append(c.substring(0, width[cnt]));
             }
             else {
                 o.append(c);
             }
 
             cnt++;
 
         }
 
         return o.toString();
 
     }
 
     private int[] beg = null;
     private int[] width = null;
     private static final char SPACE = ' ';
 
 } // class FixedWidthFormat
