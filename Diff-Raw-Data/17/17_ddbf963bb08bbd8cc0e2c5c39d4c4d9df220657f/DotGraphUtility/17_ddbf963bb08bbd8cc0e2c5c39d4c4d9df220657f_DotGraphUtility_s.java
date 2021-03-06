 /* Soot - a J*va Optimization Framework
  * Copyright (C) 2002 Sable Research Group
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 /*
  * Modified by the Sable Research Group and others 1997-1999.  
  * See the 'credits' file distributed with Soot for the complete list of
  * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
  */
 
 
 /* @author Feng Qian */
 
 package soot.util;
 
 import java.util.*;
 import java.io.*;
 
 public class DotGraphUtility {
 
   /* repalce any " to \" in the string */
   public static String replaceQuotes(String original){
     byte[] ord = original.getBytes();
     int quotes = 0;
     for (int i=0, n=ord.length; i<n; i++) {
       if (ord[i] == '\"') quotes++;
     }
 
     if (quotes == 0) return original;
 
     byte[] newsrc = new byte[ord.length+quotes];
     for (int i=0, j=0, n=ord.length; i<n; i++, j++){
       if (ord[i] == '\"') {
	newsrc[j++] = '\\';
       }
       newsrc[j] = ord[i];
     }
 
     /*
     System.out.println("before "+original);
     System.out.println("after  "+(new String(newsrc)));
     */
 
     return new String(newsrc);
   }
 
   /* replace any return by to "\n" */
   public static String replaceReturns(String original){
     byte[] ord = original.getBytes();
     int quotes = 0;
     for (int i=0, n=ord.length; i<n; i++) {
       if (ord[i] == '\n') quotes++;
     }
 
     if (quotes == 0) return original;
 
     byte[] newsrc = new byte[ord.length+quotes];
     for (int i=0, j=0, n=ord.length; i<n; i++, j++){
       if (ord[i] == '\n') {
	newsrc[j++] = '\\';
	newsrc[j] = 'n';
       } else {
 	newsrc[j] = ord[i];
       }
     }
 
     /*
     System.out.println("before "+original);
     System.out.println("after  "+(new String(newsrc)));
     */
 
     return new String(newsrc);    
   }
 
   public static void renderLine(OutputStream out,
 		      String content, 
 		      int indent) throws IOException {
     for (int i=0; i<indent; i++) {
       out.write(' ');
     }
     
     content = content + "\n";
 
     out.write(content.getBytes());
   }
 }
