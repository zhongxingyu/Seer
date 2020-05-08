 /* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
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
 
 package com.fluendo.jst;
 
 import java.util.*;
 
 public class Caps
 {
   protected String mime;
   protected Hashtable fields = new Hashtable();
 
   public synchronized String getMime () {
     return mime;
   }
   public synchronized void setMime (String newMime) {
     mime = newMime;
   }
 
   public Caps(String mime) {
     super();
     int sep1, sep2, sep3;
     int len;
     
     len = mime.length();
     sep1 = 0;
     sep2 = mime.indexOf (';');
     if (sep2 == -1)
       sep2 = len;
 
     this.mime = mime.substring(0, sep2);
     while (sep2 < len) {
       sep1 = sep2+1;
       sep2 = mime.indexOf ('=', sep1);
       sep3 = mime.indexOf (';', sep2);
       if (sep3 == -1)
         sep3 = len;
       setField (mime.substring(sep1, sep2), mime.substring(sep2+1, sep3));
      sep2 = sep3;
     }
   }
 
   public String toString () {
     StringBuffer buf = new StringBuffer();
 
     buf.append("Caps: ");
     buf.append(mime);
     buf.append("\n");
     for (Enumeration e = fields.keys(); e.hasMoreElements();) {
       String key = (String) e.nextElement();
       buf.append(" \"").append(key).append("\": \"").append(fields.get(key)).append("\"\n");
     }
     return buf.toString();
   }
 
   public void setField (String key, java.lang.Object value) {
     fields.put (key, value);
   }
   public void setFieldInt (String key, int value) {
     fields.put (key, new Integer (value));
   }
   public java.lang.Object getField (String key) {
     return fields.get (key);
   }
   public int getFieldInt (String key, int def) {
     Integer i;
     i = (Integer) fields.get(key);
     if (i == null)
       return def;
 
     return i.intValue();
   }
   public String getFieldString (String key, String def) {
     String s = (String) fields.get(key);
     if (s == null)
       return def;
 
     return s;
   }
 }
