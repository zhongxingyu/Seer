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
 
 package org.catacombae.jfuse.util;
 
 import java.io.UnsupportedEncodingException;
import org.catacombae.jfuse.types.system.Timespec;
 
 /**
  *
  * @author erik
  */
 public class FUSEUtil {
     /**
      * Convenience method for encoding a UTF-8 byte string from a Java
      * {@link String}.
      *
      * @param str the string to encode.
      * @return a UTF-8 encoded sequence containing the contents of 'str'.
      */
     public static byte[] encodeUTF8(String str) {
         try {
             // TODO: Encode properly and deal with errors.
             return str.getBytes("UTF-8");
         } catch(UnsupportedEncodingException e) {
             throw new RuntimeException("UTF-8 charset not found! This should not happen...", e);
         }
     }
 
     /**
      * Convenience method for decoding a UTF-8 byte sequence into a Java
      * {@link String}.
      *
      * @param utf8Data the string to decode.
      * @return a {@link String} containing the contents of 'utf8Data'.
      */
     public static String decodeUTF8(byte[] utf8Data) {
         try {
             // TODO: Decode properly and deal with errors.
             return new String(utf8Data, 0, utf8Data.length, "UTF-8");
         } catch(UnsupportedEncodingException e) {
             throw new RuntimeException("UTF-8 charset not found! This should not happen...", e);
         }
     }
 
     /**
      * Returns the directory component of a pathname, just like the unix utility
      * 'dirname'.
      *
      * @param path the pathname to process. For example "/a/b/c".
     * @return the path component. For example "/a/b".
      */
     public static String dirname(String path) {
        int lastSeparatorIndex = path.lastIndexOf('/');
        if(lastSeparatorIndex == path.indexOf('/'))
            return path.substring(0, lastSeparatorIndex+1);
        else
            return path.substring(0, lastSeparatorIndex);
     }
 
     /**
      * Returns the last component of a pathname, just like the unix utility
      * 'basename'.
      *
      * @param path the pathname to process. For example "/a/b/c".
      * @return the base component. For example "c".
      */
     public static String basename(String path) {
         return path.substring(path.lastIndexOf('/')+1);
     }
 
     /**
      * Returns the uid of the running Java process.
      *
      * @return the uid of the running Java process.
      */
     public static long getProcessUid() {
         return getProcessUidNative();
     }
 
     /**
      * Returns the gid of the running Java process.
      *
      * @return the gid of the running Java process.
      */
     public static long getProcessGid() {
         return getProcessGidNative();
     }
 
     /**
      * Returns the pid of the running Java process.
      *
      * @return the pid of the running Java process.
      */
     public static long getProcessPid() {
         return getProcessPidNative();
     }
 
     private static native long getProcessUidNative();
     private static native long getProcessGidNative();
     private static native long getProcessPidNative();

    public static void setTimespecToMillis(Timespec timespec, long timeMillis) {
        timespec.sec = (int)(timeMillis / 1000);
        timespec.nsec = (int)(timeMillis-timespec.sec*1000)*1000000;
    }
 }
