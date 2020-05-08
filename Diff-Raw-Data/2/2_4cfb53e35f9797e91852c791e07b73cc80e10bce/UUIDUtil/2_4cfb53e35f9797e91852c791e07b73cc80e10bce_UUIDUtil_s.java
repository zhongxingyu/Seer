 package com.barchart.store.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicLong;
 
 public final class UUIDUtil {
 
 	// This comes from Hector's TimeUUIDUtils class:
 	// https://github.com/rantav/hector/blob/master/core/src/main/java/me/...
 	private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
 	
 	 /**
      * The cached MAC address.
      */
     private static String macAddress = null;
     
     /**
      * The current clock and node value.
      */
     private static long clockSeqAndNode = 0x8000000000000000L;
     
    private static AtomicLong sequence = new AtomicLong(0); 
     
     private UUIDUtil() {
     	
     }
 
     /**
      * Creates a new UUID using current system time and clock sequence node
      * @return
      */
 	public static final UUID timeUUID() {
 		return new UUID(newTime(), getClockSeqAndNode());
 	}
 
 	/**
      * Creates a new UUID using provided time and current clock sequence node
      * @return
      */
 	public static final UUID timeUUID(final long time) {
 		return new UUID(createTime(time), getClockSeqAndNode());
 	}
 	
 	/**
      * Creates a new UUID using provided time and lowest possible clock sequence node
      * @return
      */
 	public static final UUID timeUUIDMin(final long time) {
 		return new UUID(createTime(time), Long.MIN_VALUE);
 	}
 	
 	/**
      * Creates a new UUID using provided time and largest possible clock sequence node
      * @return
      */
 	public static final UUID timeUUIDMax(final long time) {
 		return new UUID(createTime(time), Long.MAX_VALUE);	
 	}
 
 	public static final long timestampFrom(final UUID uuid) {
 		return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
 	}
 
 	private static long newTime() {
 		return createTime(System.currentTimeMillis());
 	}
 	
 	/**
 	 * Creates a new time field from the given timestamp.
 	 * @param curTime the time stamp
 	 * @return
 	 */
 	static long createTime(final long curTime) {
 		
 		long timeMillis = (curTime * 10000) + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;
 		
 		 // time low
 		long time = timeMillis << 32;
 		
 		 // time mid
 		time |= (timeMillis & 0xFFFF00000000L) >> 16;
 		
 		// time hi and version
 		time |= 0x1000 | ((timeMillis >> 48) & 0x0FFF); // version 1
 		
 		return time;
 		
 	}
 	
     static {
 
         try {
             Class.forName("java.net.InterfaceAddress");
             macAddress = Class.forName(
                     "com.eaio.uuid.UUIDGen$HardwareAddressLookup").newInstance().toString();
         }
         catch (ExceptionInInitializerError err) {
             // Ignored.
         }
         catch (ClassNotFoundException ex) {
             // Ignored.
         }
         catch (LinkageError err) {
             // Ignored.
         }
         catch (IllegalAccessException ex) {
             // Ignored.
         }
         catch (InstantiationException ex) {
             // Ignored.
         }
         catch (SecurityException ex) {
             // Ignored.
         }
 
         if (macAddress == null) {
 
             Process p = null;
             BufferedReader in = null;
 
             try {
                 String osname = System.getProperty("os.name", "");
 
                 if (osname.startsWith("Windows")) {
                     p = Runtime.getRuntime().exec(new String[] { "ipconfig", "/all" }, null);
                 }
                 // Solaris code must appear before the generic code
                 else if (osname.startsWith("Solaris")
                         || osname.startsWith("SunOS")) {
                     String hostName = getFirstLineOfCommand("uname", "-n" );
                     if (hostName != null) {
                         p = Runtime.getRuntime().exec(
                                 new String[] { "/usr/sbin/arp", hostName },null);
                     }
                 } else if (new File("/usr/sbin/lanscan").exists()) {
                     p = Runtime.getRuntime().exec(new String[] { "/usr/sbin/lanscan" }, null);
                 } else if (new File("/sbin/ifconfig").exists()) {
                     p = Runtime.getRuntime().exec(new String[] { "/sbin/ifconfig", "-a" }, null);
                 }
 
                 if (p != null) {
                     in = new BufferedReader(new InputStreamReader(p.getInputStream()), 128);
                     String l = null;
                     while ((l = in.readLine()) != null) {
                         macAddress = MACAddressParser.parse(l);
                         if (macAddress != null && parseShort(macAddress) != 0xff) {
                             break;
                         }
                     }
                 }
 
             } catch (SecurityException ex) {
                 // Ignore it.
             } catch (IOException ex) {
                 // Ignore it.
             } finally {
                 if (p != null) {
                     if (in != null) {
                         try {
                             in.close();
                         } catch (IOException ex) {
                             // Ignore it.
                         }
                     }
                     
                     try {
                         p.getErrorStream().close();
                     } catch (IOException ex) {
                         // Ignore it.
                     } 
                     
                     try {
                         p.getOutputStream().close();
                     } catch (IOException ex) {
                         // Ignore it.
                     }
                     p.destroy();
                 }
             }
 
         }
 
         if (macAddress != null) {
             clockSeqAndNode |= parseLong(macAddress);
         } else {
             try {
                 byte[] local = InetAddress.getLocalHost().getAddress();
                 clockSeqAndNode |= (local[0] << 24) & 0xFF000000L;
                 clockSeqAndNode |= (local[1] << 16) & 0xFF0000;
                 clockSeqAndNode |= (local[2] << 8) & 0xFF00;
                 clockSeqAndNode |= local[3] & 0xFF;
             } catch (UnknownHostException ex) {
                 clockSeqAndNode |= (long) (Math.random() * 0x7FFFFFFF);
             }
         }
 
     }
     
     /**
      * Increments the sequence and returns the new clockSeqAndNode value.
      *
      * @return the clockSeqAndNode value
      * @see UUID#getClockSeqAndNode()
      */
     private static long getClockSeqAndNode() {
     	return clockSeqAndNode | ((sequence.getAndIncrement() & 0x3FFF) << 48);
     }
 	
 	 /**
      * Returns the first line of the shell command.
      *
      * @param commands the commands to run
      * @return the first line of the command
      * @throws IOException
      */
    private static String getFirstLineOfCommand(String... commands) throws IOException {
 
         Process p = null;
         BufferedReader reader = null;
 
         try {
             p = Runtime.getRuntime().exec(commands);
             reader = new BufferedReader(new InputStreamReader(p.getInputStream()), 128);
             return reader.readLine();
         } finally {
             if (p != null) {
                 if (reader != null) {
                     try {
                         reader.close();
                     } catch (IOException ex) {
                         // Ignore it.
                     }
                 } try {
                     p.getErrorStream().close();
                 } catch (IOException ex) {
                     // Ignore it.
                 }
                 
                 try {
                     p.getOutputStream().close();
                 } catch (IOException ex) {
                     // Ignore it.
                 }
                 p.destroy();
             }
         }
 
     }
     
 	/**
      * Scans MAC addresses for good ones.
      */
     static class HardwareAddressLookup {
 
         /**
          * @see java.lang.Object#toString()
          */
         @Override
         public String toString() {
             String out = null;
             try {
                 Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
                 if (ifs != null) {
                     while (ifs.hasMoreElements()) {
                         NetworkInterface iface = ifs.nextElement();
                         byte[] hardware = iface.getHardwareAddress();
                         if (hardware != null && hardware.length == 6
                                 && hardware[1] != (byte) 0xff) {
                             out = append(new StringBuilder(36), hardware).toString();
                             break;
                         }
                     }
                 }
             } catch (SocketException ex) {
                 // Ignore it.
             }
             return out;
         }
 
     }
 	
     private static class MACAddressParser {
     	
     	 /**
          * No instances needed.
          */
         private MACAddressParser() {
             super();
         }
 
         /**
          * Attempts to find a pattern in the given String.
          *
          * @param in the String, may not be <code>null</code>
          * @return the substring that matches this pattern or <code>null</code>
          */
         static String parse(String in) {
 
             String out = in;
 
             // lanscan
 
             int hexStart = out.indexOf("0x");
             if (hexStart != -1 && out.indexOf("ETHER") != -1) {
                 int hexEnd = out.indexOf(' ', hexStart);
                 if (hexEnd > hexStart + 2) {
                     out = out.substring(hexStart, hexEnd);
                 }
             }
 
             else {
 
                 int octets = 0;
                 int lastIndex, old, end;
 
                 if (out.indexOf('-') > -1) {
                     out = out.replace('-', ':');
                 }
 
                 lastIndex = out.lastIndexOf(':');
 
                 if (lastIndex > out.length() - 2) {
                     out = null;
                 } else {
 
                     end = Math.min(out.length(), lastIndex + 3);
                     ++octets;
                     old = lastIndex;
                     while (octets != 5 && lastIndex != -1 && lastIndex > 1) {
                         lastIndex = out.lastIndexOf(':', --lastIndex);
                         if (old - lastIndex == 3 || old - lastIndex == 2) {
                             ++octets;
                             old = lastIndex;
                         }
                     }
 
                     if (octets == 5 && lastIndex > 1) {
                         out = out.substring(lastIndex - 2, end).trim();
                     } else {
                         out = null;
                     }
 
                 }
 
             }
 
             if (out != null && out.startsWith("0x")) {
                 out = out.substring(2);
             }
 
             return out;
         }
     	
     }
     
     /* ***** *****  Number-to-hexadecimal and hexadecimal-to-number conversions ***** ***** */
     
     private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
     	'b', 'c', 'd', 'e', 'f' };
     
     /**
      * Turns a <code>byte</code> array into hex octets.
      *
      * @param a the {@link Appendable}, may not be <code>null</code>
      * @param bytes the <code>byte</code> array
      * @return {@link Appendable}
      */
     private static Appendable append(Appendable a, byte[] bytes) {
         try {
             for (byte b : bytes) {
                 a.append(DIGITS[(byte) ((b & 0xF0) >> 4)]);
                 a.append(DIGITS[(byte) (b & 0x0F)]);
             }
         }
         catch (IOException ex) {
             // Bla
         }
         return a;
     }
     
     /**
      * Parses a <code>long</code> from a hex encoded number. This method will skip all characters that are not 0-9,
      * A-F and a-f.
      * <p>
      * Returns 0 if the {@link CharSequence} does not contain any interesting characters.
      *
      * @param s the {@link CharSequence} to extract a <code>long</code> from, may not be <code>null</code>
      * @return a <code>long</code>
      * @throws NullPointerException if the {@link CharSequence} is <code>null</code>
      */
     private static long parseLong(CharSequence s) {
         long out = 0;
         byte shifts = 0;
         char c;
         for (int i = 0; i < s.length() && shifts < 16; i++) {
             c = s.charAt(i);
             if ((c > 47) && (c < 58)) {
                 ++shifts;
                 out <<= 4;
                 out |= c - 48;
             } else if ((c > 64) && (c < 71)) {
                 ++shifts;
                 out <<= 4;
                 out |= c - 55;
             } else if ((c > 96) && (c < 103)) {
                 ++shifts;
                 out <<= 4;
                 out |= c - 87;
             }
         }
         return out;
     }
     
     /**
      * Parses a <code>short</code> from a hex encoded number. This method will skip all characters that are not 0-9,
      * A-F and a-f.
      * <p>
      * Returns 0 if the {@link CharSequence} does not contain any interesting characters.
      *
      * @param s the {@link CharSequence} to extract a <code>short</code> from, may not be <code>null</code>
      * @return a <code>short</code>
      * @throws NullPointerException if the {@link CharSequence} is <code>null</code>
      */
      private static short parseShort(String s) {
         short out = 0;
         byte shifts = 0;
         char c;
         for (int i = 0; i < s.length() && shifts < 4; i++) {
             c = s.charAt(i);
             if ((c > 47) && (c < 58)) {
                 ++shifts;
                 out <<= 4;
                 out |= c - 48;
             } else if ((c > 64) && (c < 71)) {
                 ++shifts;
                 out <<= 4;
                 out |= c - 55;
             } else if ((c > 96) && (c < 103)) {
                 ++shifts;
                 out <<= 4;
                 out |= c - 87;
             }
         }
         return out;
     }
 
     
 }
