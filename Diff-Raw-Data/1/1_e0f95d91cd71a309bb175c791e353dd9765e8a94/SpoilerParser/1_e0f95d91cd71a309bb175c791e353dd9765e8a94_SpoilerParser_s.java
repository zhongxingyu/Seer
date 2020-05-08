 /*
  * SpoilerParser.java
  * Copyright (C) 2002 Klaus Rennecke.
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
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package net.sourceforge.fraglets.mtgo.trader;
 
 import javax.swing.table.DefaultTableModel;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.BufferedReader;
 
 
 /**
  * Parser for the original spoiler lists.
  * @author  marion@users.sourceforge.net
  */
 public class SpoilerParser {
     public static final Double ZERO = new Double(0);
     
     /** Maximum line length. */
     public static final int MAX_LINE = 255;
     
     DefaultTableModel model;
     
     /** Creates a new instance of SpoilerParser */
     public SpoilerParser(DefaultTableModel model) {
         this.model = model;
        model.setColumnCount(2);
         model.setColumnIdentifiers(new Object[] { "Name", "Color", "Rarity", "Mana", "Type", "P/T" });
     }
     
     public void parse(Reader in) throws IOException {
         BufferedReader reader = new BufferedReader(in);
         
         String line[];
         Object[] row = null;
         while ((line = parseField(reader)) != null) {
             String key = line[0];
             if (key.equals("Card Title") || key.equals("Card Name")) {
                 if (row != null) {
                     model.addRow(row);
                 }
                 row = new Object[] { line[1], null, null, null, null, null };
             } else if (row == null) {
                 // ignore
             } else if (key.endsWith("Color")) {
                 row[1] = line[1];
             } else if (key.equals("Rarity")) {
                 row[2] = line[1];
             } else if (key.endsWith("Cost")) {
                 row[3] = line[1];
             } else if (key.endsWith("Type") || key.startsWith("Type")) {
                 row[4] = line[1];
             } else if (key.startsWith("Pow/T") || key.startsWith("Pwr/T")) {
                 row[5] = line[1];
             }
         }
         if (row != null) {
             model.addRow(row);
         }
     }
     
     protected String[] parseField(BufferedReader reader) throws IOException {
         String line;
         String name = null;
         StringBuffer value = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             int colon = line.indexOf(':');
             if (colon < 0)
                 continue;
             name = line.substring(0, colon).trim();
             value.append(line.substring(colon+1).trim());
             break;
         }
         if (name == null)
             return null;
         do {
             reader.mark(MAX_LINE);
             line = reader.readLine();
             if (line != null) {
                 if (line.length() > 0 && Character.isWhitespace(line.charAt(0))) {
                     value.append(' ').append(line.trim());
                 } else {
                     reader.reset();
                     break;
                 }
             }
         } while (line != null);
         
         return new String[] { name, value.toString() };
     }
 }
