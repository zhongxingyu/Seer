 /**
  * ********
  * Copyright © 2010-2012 Olanto Foundation Geneva
  *
  * This file is part of myCAT.
  *
  * myCAT is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * myCAT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with myCAT. If not, see <http://www.gnu.org/licenses/>.
  *
  *********
  */
 package org.olanto.mapman.server;
 
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.io.StringReader;
 import static org.olanto.idxvli.util.BytesAndFiles.*;
 import static org.olanto.util.Messages.*;
 
 /**
  * Une classe pour stocker un document sous forme de phrase.
  *
  *
  */
 public class SegDoc {
 
     public String[] lines;
     public int[][] positions;
     public int nblines;
     public String content;
     public String uri;
     public String lang;
     public String txt_encoding = "UTF-8";
 
     public void dump(String s) {
         System.out.println("--------------------------------------------");
         System.out.println(s);
         System.out.println("nblines:" + nblines);
         System.out.println("lines.length:" + lines.length);
         System.out.println("positions.lenght:" + positions.length);
         for (int i = 0; i < lines.length; i++) {
             System.out.println("line " + i + ":" + lines[i]);
         }
         for (int i = 0; i < positions.length; i++) {
             System.out.print("positions " + i + ":");
             for (int j = 0; j < positions[i].length; j++) {
                 System.out.print(positions[i][j] + ", ");
             }
             System.out.println();
         }
     }
 
     public SegDoc(String fname, String lang) {
         uri = fname;
         this.lang = lang;
         content = file2String(fname, txt_encoding);
        if (AlignBiText.skipLine) {
             //msg("skipline");
             content = content.replace("\n", "\n\n");
         }
         if (content == null) {
             content = "file source:" + fname + " language:" + lang + "\n*** ERROR :no file\n";
         }
         if (content.length() == 0) {
             content = "file source:" + fname + " language:" + lang + "\n*** ERROR :file is empty\n";
         }
         init(content);
     }
 
    /** used to initialise a error msg*/
     public SegDoc(String fname, String fromstring, String lang) {
         uri = fname;
         this.lang = lang;
         content = fromstring;
         init(content);
     }
 
     public void init(String document) {
         nblines = countLines(document);
         //("nblines:"+nblines);
         lines = getLines(document, nblines);
 
     }
 
     public static int countLines(String s) {
         int count = 0;
         try {
             Reader r = new StringReader(s);
             BufferedReader in = new BufferedReader(r);
             String w = in.readLine();
 
             while (w != null) {
                 count++;
                 w = in.readLine();
             }
             in.close();
             r.close();
 
         } catch (Exception e) {
             error("countLines", e);
         }
         return count;
     }
 
     public static String[] getLines(String s, int nblines) {
         String[] l = new String[nblines];
         int count = 0;
         try {
             Reader r = new StringReader(s);
             BufferedReader in = new BufferedReader(r);
             String w = in.readLine();
 
             while (w != null) {
                 l[count] = w;
                 count++;
                 w = in.readLine();
             }
             in.close();
             r.close();
 
         } catch (Exception e) {
             error("getLines", e);
         }
         return l;
     }
 }
