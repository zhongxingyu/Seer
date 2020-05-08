 /*  Filter.java - A class that filters incoming messages
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.gjt.fredde.yamm.mail;
 
 import java.io.*;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 /**
  * A class that filters incoming messages
  */
 public class Filter {
 
   String confHome = System.getProperty("user.home") + "/.yamm/";
   String temp, temp2;
   String con1, con2, con3, exec;
   StringTokenizer tok;
   BufferedReader in;
   int type;
   Vector list = new Vector();
 
   /**
    * Starts the filtering
    */
   public Filter() throws IOException {
 
     Mailbox.createFilterList(list);
 
     try{
       in = new BufferedReader(new InputStreamReader(new FileInputStream(confHome + ".filters")));
 
       for(;;) {
         temp = in.readLine();
 
         if(temp == null) break;
 
         tok = new StringTokenizer(temp, ";");
         con1 = tok.nextToken();
         con2 = tok.nextToken();
         con3 = tok.nextToken();
         exec = tok.nextToken();
 
        System.out.println("if " + con1 + " " + con2 +  " " + con3 + " then move to " + exec);

         if(con1.equals("from")) type = 0;
         else if(con1.equals("to")) type = 1;
         else if(con1.equals("subject")) type = 2;
         else if(con1.equals("reply")) type = 3;
 
         if(con2.equals("is")) {
           for(int i=0; i < list.size();i++) {
             temp2 = ((Vector)list.elementAt(i)).elementAt(type).toString();
            System.out.println("temp2: " + temp2);
             if(temp2.equalsIgnoreCase(con3)) {
              System.out.println("mail " + i + "'s " + con1 + " equals " + con3 + ". Will move to " + exec);
               list.remove(i);
               Mailbox.moveMail(confHome + "boxes/.filter", confHome + "boxes/" + exec, i);
               i--;
             }
           }
         }
         else if(con2.equals("contains")) {
           for(int i=0; i < list.size();i++) {
             temp2 = ((Vector)list.elementAt(i)).elementAt(type).toString();
            System.out.println("temp2: " + temp2);
             if(temp2.toLowerCase().indexOf(con3.toLowerCase()) != -1) {
              System.out.println("mail " + i + "'s " + con1 + " contains " + con3 + ". Will move to " + exec);
               list.remove(i);
              Mailbox.moveMail(confHome + "boxes/.filter", confHome + "boxes/" + exec, i - 1);
               i--;
             }
           }
         }
       }
     } finally { in.close(); }
     while(Mailbox.hasMail(confHome + "boxes/.filter")) {
       Mailbox.moveMail(confHome + "boxes/.filter", confHome + "boxes/inbox", 0);
     }
   }
 }
 
