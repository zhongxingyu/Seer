 /*
  * Copyright (C) 2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.utils;
 
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * IP address utility methods.
  */
 public final class IpUtils {
     private IpUtils() {
         // Utility class - don't instantiate
     }
     
     private static final Pattern IPV4_PATTERN = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
 
     /**
      * Parses the string into an InetAddress ensuring that no DNS lookup is performed
      * on the address (it must be specified as a literal IPv4 or IPv6 address).
      * 
      * @param value String IP address.
      * @return InetAddress for the specified address.
      * @throws IllegalArgumentException If the address is invalid.
      */
     public static InetAddress parseAddress(String value) throws IllegalArgumentException {
         // Looks for an IPv6 or IPv4 address. Discards anything that looks like a hostname
         // to prevent long running hostname lookups.
         final InetAddress addr;
         if (value.indexOf(':') != -1) {
             if (!value.startsWith("[")) {
                 // We expect an IPv6 address
                 value = "[" + value + "]";
             }
             try {
                 addr = InetAddress.getByName(value);
             } catch (UnknownHostException e) {
                 throw new IllegalArgumentException(e.getLocalizedMessage(), e);
             }
         }
         else {
             Matcher matcher = IPV4_PATTERN.matcher(value);
             if (matcher.matches()) {
                 final byte[] bytes = new byte[4];
                 for (int i = 1; i <= matcher.groupCount(); i++) {
                     int octet = Integer.parseInt(matcher.group(i));
                     if (octet > 255) {
                         throw new IllegalArgumentException("Invalid IP address: " + value);
                     }
                     bytes[i-1] = (byte)octet;
                 }
                 try {
                     addr = InetAddress.getByAddress(bytes);
                 } catch (UnknownHostException e) {
                     throw new IllegalArgumentException(e.getLocalizedMessage(), e);
                 }
             }
             else {
                 throw new IllegalArgumentException("Invalid IP address: " + value); 
             }
         }
         return addr;
     }
     
     /**
      * Converts the IP address to a canonical string which allows correct comparison
      * behavior with IP address (used for sorting and range queries). IPv4 and IPv6 
      * addresses are converted to have leading zeros in addresses (i.e. 192.168.1.2 becomes
      * 192.168.001.002 and ::1 becomes 0000:0000:0000:0000:0000:0000:0000:0001).
      * 
      * @param addr IP address.
      * @return The canonical format used for sorting and range queries.
      */
     public static String canonicalIpAddress(InetAddress addr) {
         StringBuilder sb = new StringBuilder(36);
         byte[] addrbytes = addr.getAddress();
         if (addrbytes.length == 4) {
             for (byte b : addrbytes) {
                 int i = (b & 0xff);
                 if (sb.length() > 0) {
                     sb.append('.');
                 }
                 sb.append(String.format("%03d", i));
             }
         }
         else if (addrbytes.length == 16) {
             for (int i = 0; i < addrbytes.length; i++) {
                 int octet = (addrbytes[i] & 0xff);
                 if (sb.length() > 0 && (i % 2) == 0) {
                     sb.append(':');
                 }
                 sb.append(String.format("%02x", octet));
             }
         }
         else {
             throw new IllegalStateException("Unexpected InetAddress storage");
         }
         return sb.toString();
     }
 
     /**
      * Converts a netmask to the number of prefix bits.
      * 
      * @param netmask The netmask.
      * @return The number of prefix bits.
      * @throws IllegalArgumentException If the netmask is invalid.
      */
     public static int netmaskToPrefixBits(InetAddress netmask) throws IllegalArgumentException {
         int prefixBits = 0;
         boolean foundZero = false;
         byte[] netmaskBytes = netmask.getAddress();
         for (byte b : netmaskBytes) {
             for (int i = 7; i >= 0; i--) {
                 if ((b & (1<<i)) != 0) {
                     if (foundZero) {
                         throw new IllegalArgumentException("Invalid netmask");
                     }
                     ++prefixBits;
                 }
                 else {
                     foundZero = true;
                 }
             }
         }
         return prefixBits;
     }
 
     /**
      * Converts the specified number of prefix bits to a InetAddress representing
      * the netmask.
      * 
      * @param prefixBits The number of prefix bits in the netmask.
      * @param isIPv6 True if the returned netmask should be IPv6, otherwise uses
      *               IPv4.
      * @return An InetAddress representing the netmask.
      * @throws IllegalArgumentException If the prefix bits are out of range.
      */
     public static InetAddress prefixBitsToNetmask(int prefixBits, boolean isIPv6) throws IllegalArgumentException {
         if (prefixBits < 0) {
             throw new IllegalArgumentException("Invalid prefix bits");
         }
         final byte[] bytes;
         if (isIPv6) {
             if (prefixBits > 128) {
                 throw new IllegalArgumentException("Invalid prefix bits");
             }
             bytes = new byte[16];
         }
         else {
             if (prefixBits > 32) {
                 throw new IllegalArgumentException("Invalid prefix bits");
             }
             bytes = new byte[4];
         }
         int byteOffset = 0;
         int bitOffset = 7;
         for (int i = 0; i < prefixBits; i++) {
             bytes[byteOffset] |= (1<<bitOffset);
             --bitOffset;
             if (bitOffset < 0) {
                 ++byteOffset;
                 bitOffset = 7;
             }
         }
         try {
             return InetAddress.getByAddress(bytes);
         } catch (UnknownHostException e) {
             throw new IllegalArgumentException(e.getLocalizedMessage(), e);
         }
     }
     
     /**
      * Given an IP address and a netmask, returns the first address in the range
      * specified by the netmask (e.g. 192.168.1.2, 255.255.255.0 -> 192.168.1.0).
      * 
      * @param addr IP address.
      * @param mask Netmask.
      * @return The last address in the range specified by the address/netmask.
      */
     private static InetAddress firstAddress(InetAddress addr, InetAddress mask) {
         final byte[] addrBytes = addr.getAddress();
         final byte[] maskBytes = mask.getAddress();
         for (int i = 0; i < addrBytes.length; i++) {
             addrBytes[i] &= maskBytes[i];
         }
         try {
             return InetAddress.getByAddress(addrBytes);
         } catch (UnknownHostException e) {
             throw new IllegalArgumentException(e.getLocalizedMessage(), e);
         }
     }
 
     /**
      * Given an IP address and a netmask, returns the last address in the range
      * specified by the netmask (e.g. 192.168.1.2, 255.255.255.0 -> 192.168.1.255).
      * 
      * @param addr IP address.
      * @param mask Netmask.
      * @return The last address in the range specified by the address/netmask.
      */
     private static InetAddress lastAddress(InetAddress addr, InetAddress mask) {
         final byte[] addrBytes = addr.getAddress();
         final byte[] maskBytes = mask.getAddress();
         for (int i = 0; i < addrBytes.length; i++) {
             addrBytes[i] |= (~maskBytes[i]);
         }
         try {
             return InetAddress.getByAddress(addrBytes);
         } catch (UnknownHostException e) {
             throw new IllegalArgumentException(e.getLocalizedMessage(), e);
         }
     }
 
     /**
      * <p>Parses an IP range in the format to an IP range.</p>
      * <ul>
      *     <li>ADDR/MASK</li>
      *     <li>ADDR/PREFIXBITS</li>
      *     <li>STARTADDR-ENDADDR</li>
      *     <li>ADDR (Converts to a range that includes just the address).</li>
      * </ul>
      * 
      * @param ipRange IP range string.
      * @return An IP range representing the start and end of the IP range.
      * @throws IllegalArgumentException If the specified IP range is invalid.
      */
     public static IpRange parseRange(String ipRange) throws IllegalArgumentException {
         final int slashIndex, dashIndex;
         final IpRange range;
         if ((slashIndex = ipRange.indexOf('/')) > 0) {
             final InetAddress from = parseAddress(ipRange.substring(0, slashIndex));
             InetAddress mask;
             try {
                 mask = parseAddress(ipRange.substring(slashIndex+1));
             } catch (IllegalArgumentException e) {
                 try {
                     int prefixBits = Integer.parseInt(ipRange.substring(slashIndex+1));
                     mask = prefixBitsToNetmask(prefixBits, from instanceof Inet6Address);
                 } catch (NumberFormatException nfe) {
                     throw new IllegalArgumentException(nfe.getLocalizedMessage(), nfe);
                 }
             }
             range = new IpRange(firstAddress(from, mask), lastAddress(from, mask));
         }
         else if ((dashIndex = ipRange.indexOf('-')) > 0) {
             final InetAddress from = parseAddress(ipRange.substring(0, dashIndex));
             InetAddress to = null;
             try {
                 to = parseAddress(ipRange.substring(dashIndex+1));
             } catch (IllegalArgumentException e) {
                 final byte[] fromAddr = from.getAddress();
                 if (fromAddr.length == 4) {
                     try {
                         final int lastByte = Integer.parseInt(ipRange.substring(dashIndex+1));
                         if (lastByte < 0 || lastByte > 255) {
                             throw new IllegalArgumentException("Invalid IP range: " + ipRange);
                         }
                         fromAddr[3] = (byte) lastByte;
                         to = InetAddress.getByAddress(fromAddr);
                    } catch (Exception ex) {
                         throw new IllegalArgumentException(e.getLocalizedMessage(), e);
                     }
                 }
                 else if (fromAddr.length == 16) {
                     try {
                         final int lastTwoBytes = Integer.parseInt(ipRange.substring(dashIndex+1), 16);
                         if (lastTwoBytes < 0 || lastTwoBytes > 0xffff) {
                             throw new IllegalArgumentException("Invalid IP range: " + ipRange);
                         }
                         ByteBuffer.wrap(fromAddr).putShort(14, (short) lastTwoBytes);
                         to = InetAddress.getByAddress(fromAddr);
                    } catch (Exception ex) {
                         throw new IllegalArgumentException(e.getLocalizedMessage(), e);
                     }
                 }
             }
             range = new IpRange(from, to);
         }
         else {
             final InetAddress addr = parseAddress(ipRange);
             range = new IpRange(addr, addr);
         }
         return range;
     }
 }
