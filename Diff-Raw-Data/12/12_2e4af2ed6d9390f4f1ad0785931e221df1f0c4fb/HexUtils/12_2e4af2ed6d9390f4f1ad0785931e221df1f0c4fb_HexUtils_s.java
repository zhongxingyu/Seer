 /*
  * #%L
  * Bitrepository Webclient
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.utils;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 public class HexUtils {
    private static final Logger log = LoggerFactory.getLogger(HexUtils.class);
    
     public static String byteArrayToString(byte[] data) {
         StringBuffer sb = new StringBuffer(data.length * 2);
         for (int i = 0; i < data.length; i++){
           int v = data[i] & 0xff;
           if (v < 16) {
             sb.append('0');
           }
           sb.append(Integer.toHexString(v));
         }
        log.info("Converted byte array to string, string: " + sb.toString());
         return sb.toString();    
     }
     
     public static byte[] stringToByteArray(String hexString) {
         int len = hexString.length();
        log.info("Trying to convert '" + hexString + "' to byte array");
         byte[] data = new byte[len / 2];
         for (int i = 0; i < len; i += 2) {
             data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                  + Character.digit(hexString.charAt(i+1), 16));
         }
        log.info("Converted string to byte array, string: " + hexString);
         return data;
     }
 }
