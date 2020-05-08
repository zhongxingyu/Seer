 /*
  * ------------------------------------------------------------------------------
  * Hermes FTP Server
  * Copyright (c) 2005-2007 Lars Behnke
  * ------------------------------------------------------------------------------
  * 
  * This file is part of Hermes FTP Server.
  * 
  * Hermes FTP Server is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * Hermes FTP Server is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Hermes FTP Server; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  * ------------------------------------------------------------------------------
  */
 
 package net.sf.hermesftp.utils;
 
 import java.net.Inet4Address;
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Utility methods that facilitate networking.
  * 
  * @author Lars Behnke
  */
 public final class NetUtils {
 
     private static final String REGEX_POINT   = "\\.";
     private static final String REGEX_COLON   = ":";
 
     private static final int    QUADRUPLE_LEN = 4;
     private static final int    OCTUPLE_LEN = 8;
 
     private static Log          log           = LogFactory
                                                       .getLog(NetUtils.class);
 
     /**
      * Hidden constructor.
      */
     private NetUtils() {
         super();
     }
 
     /**
      * Returns the machine's network address.
      * 
      * @param fallBackToLocalhost True if loopback address should be used if
      *            there is no net.
      * @return The ip address.
      */
     public static InetAddress getMachineAddress(boolean fallBackToLocalhost) {
         InetAddress result = null;
         try {
             Enumeration<NetworkInterface> nis = NetworkInterface
                     .getNetworkInterfaces();
             while (nis.hasMoreElements()) {
                 NetworkInterface ni = nis.nextElement();
                 InetAddress ia = getMachineAddress(ni);
                 if (ia != null) {
                     result = ia;
                 }
             }
             if (result == null) {
                 result = InetAddress.getLocalHost();
             }
 
         } catch (SocketException e) {
             log.error(e);
         } catch (UnknownHostException e) {
             log.error(e);
         }
         return result;
     }
 
     /**
      * Checks the IP protocol version (IPv4 or IPv6)
      * 
      * @param addr The address to check.
      * @return True, if IPv6.
      */
     public static boolean isIPv6(InetAddress addr) {
         return addr instanceof Inet6Address;
     }
 
     /**
      * Returns the network address of a particular network interface.
      * 
      * @param ni The network interface.
      * @return The machine address of a particular network interface.
      */
     public static InetAddress getMachineAddress(NetworkInterface ni) {
         Enumeration<InetAddress> addrs = ni.getInetAddresses();
         InetAddress mAddr = null;
         while (addrs.hasMoreElements()) {
             InetAddress addr = addrs.nextElement();
             if (addr.isSiteLocalAddress()) {
                 mAddr = addr;
             }
         }
         return mAddr;
     }
 
     /**
      * Checks if SSL is available.
      * 
      * @return True, if SSL is available.
      */
     public static boolean isSSLAvailable() {
         try {
             Class.forName("com.sun.net.ssl.internal.ssl.Provider");
             log.info("JSSE installed.");
             return true;
         } catch (ClassNotFoundException e) {
             log.error("JSSE is NOT installed correctly!");
             return false;
         }
 
     }
 
     /**
      * Checks if the passed IP address complies to a given pattern.
      * 
      * @param ipTemplateList String list of patterns. Wild cards are allowed: 192.168.*.*, 127.0.0.1, !85.0.0.0
      * @param ip The IP address to check.
      * @return True, if the passed IP address matches at least one of the
      *         patterns.
      */
     public static boolean checkIPMatch(String ipTemplateList, InetAddress addr) {
         if (isIPv6(addr)) {
            return checkIPv6Match(ipTemplateList, (Inet6Address)addr);
         } else {
            return checkIPv4Match(ipTemplateList, (Inet4Address)addr);
         }
     }
     
     /**
      * Checks if the passed IPv4 complies to a given pattern.
      * 
      * @param ipTemplateList String list of patterns. Wild cards are allowed: 192.168.*.*, 127.0.0.1, !85.0.0.0
     * @param ip The IP address to check.
      * @return True, if the passed IP address matches at least one of the
      *         patterns.
      */
    public static boolean checkIPv4Match(String ipTemplateList, Inet4Address addr) {
        String ipStr = addr.getHostAddress();
         String[] chk = ipStr.split(REGEX_POINT);
         if (chk.length != QUADRUPLE_LEN) {
             throw new IllegalArgumentException("Illegal IPv4 address: " + ipStr);
         }
 
         boolean inverse = false;
         String[] ipTemplateArr = ipTemplateList.split(",");
         for (int i = 0; i < ipTemplateArr.length; i++) {
             String t;
             if (ipTemplateArr[i].trim().startsWith("!")) {
                 t = ipTemplateArr[i].substring(1).trim();
                 inverse = true;
             } else {
                 t = ipTemplateArr[i].trim();
             }
             String[] tmpl = t.split(REGEX_POINT);
             boolean match = true;
             for (int j = 0; j < tmpl.length; j++) {
                 match &= ("*".equals(tmpl[j].trim()))
                         || (tmpl[j].trim().equals(chk[j].trim())) ^ inverse;
             }
             if (match) {
                 return true;
             }
         }
 
         return false;
 
     }
 
     /**
      * Checks if the passed IPv6 complies to a given pattern.
      * 
      * @param ipTemplateList String list of patterns. Wild cards are allowed.
     * @param ip The IP address to check.
      * @return True, if the passed IP address matches at least one of the
      *         patterns.
      */
    public static boolean checkIPv6Match(String ipTemplateList, Inet6Address addr) {
        String ipStr = addr.getHostAddress();
         String[] chk = ipStr.split(REGEX_COLON);
         if (chk.length != OCTUPLE_LEN) {
             throw new IllegalArgumentException("Illegal IPv6 address: " + ipStr);
         }
 
         boolean inverse = false;
         String[] ipTemplateArr = ipTemplateList.split(",");
         for (int i = 0; i < ipTemplateArr.length; i++) {
             String t;
             if (ipTemplateArr[i].trim().startsWith("!")) {
                 t = ipTemplateArr[i].substring(1).trim();
                 inverse = true;
             } else {
                 t = ipTemplateArr[i].trim();
             }
             String[] tmpl = t.split(REGEX_COLON);
             boolean match = true;
             for (int j = 0; j < tmpl.length; j++) {
                 match &= ("*".equals(tmpl[j].trim()))
                         || (tmpl[j].trim().equals(chk[j].trim())) ^ inverse;
             }
             if (match) {
                 return true;
             }
         }
 
         return false;
 
     }
 }
