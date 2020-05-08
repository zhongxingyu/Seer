 /*-
  * jFUSE - FUSE bindings for Java
  * Copyright (C) 2008-2009  Erik Larsson <erik82@kth.se>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package org.catacombae.jfuse.types.system;
 
 import java.io.PrintStream;
 import java.util.Date;
 
 /**
  * A Java mapping of <code>struct timespec</code>.
  *
  * @author Erik Larsson
  */
 public class Timespec {
     /* TODO: 2038 problem. */
     /** Seconds. Darwin type: __darwin_time_t (4 bytes (32-bit platforms), 8 bytes (64-bit platforms)) */
     public int sec = 0;
     /** Nanoseconds. Darwin type: long (4 bytes (32-bit platforms), 8 bytes (64-bit platforms)) */
     public int nsec = 0;
 
     /**
      * Sets the fields of this Timespec object to the specified time value,
      * expressed in nanoseconds since January 1, 1970, 00:00:00 GMT.
      *
     * @param millis the new time value, in nanoseconds since January 1, 1970,
      * 00:00:00 GMT.
      */
     public void setToNanos(long nanos) {
         this.sec = (int)((nanos >> 32) & 0xFFFFFFFF);
         this.nsec = (int)(nanos & 0xFFFFFFFF);
     }
 
     /**
      * Sets the fields of this Timespec object to the specified time value,
      * expressed in milliseconds since January 1, 1970, 00:00:00 GMT.
      *
      * @param millis the new time value, in milliseconds since January 1, 1970,
      * 00:00:00 GMT.
      */
     public void setToMillis(long millis) {
         this.sec = (int)(millis / 1000);
         this.nsec = (int)(millis-this.sec*1000)*1000000;
     }
 
     /**
      * Sets the fields of this Timespec object to the specified time value,
      * expressed as a Java date.
      *
      * @param d the new time value.
      */
     public void setToDate(Date d) {
         setToMillis(d.getTime());
     }
 
     /**
      * Sets the fields of this Timespec object to the specified time value,
      * expressed as another Timespec object.
      *
      * @param tv the new time value.
      */
     public void setToTimespec(Timespec tv) {
         this.sec = tv.sec;
         this.nsec = tv.nsec;
     }
 
     /**
      * Zeroes all fields.
      */
     public void zero() {
         this.sec = 0;
         this.nsec = 0;
     }
 
     public long toMillis() {
         return this.sec*1000 + this.nsec/1000000;
     }
 
     public Date toDate() {
         return new Date(toMillis());
     }
 
     public void printFields(String prefix, PrintStream ps) {
         ps.println(prefix + "sec: " + sec);
         ps.println(prefix + "nsec: " + nsec);
     }
 
     public void print(String prefix, PrintStream ps) {
         ps.println(prefix + getClass().getSimpleName());
         printFields(prefix + " ", ps);
     }
 }
