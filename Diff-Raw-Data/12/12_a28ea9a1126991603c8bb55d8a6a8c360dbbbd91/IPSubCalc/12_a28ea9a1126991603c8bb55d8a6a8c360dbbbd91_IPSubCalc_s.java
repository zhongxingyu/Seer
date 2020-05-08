 package ipsubcalc;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * IP Subnet Calculating Algorithms
  * @author Nicholas Schmidt
  */
 public class IPSubCalc {
 
     public static byte[] bitwiseAnd(byte[] one, byte[] two) {
         if(one.length == two.length) {
             for(int i = 0; i < one.length; i++) {
                 one[i] &= two[i];
             }
         }
         return one;
     }
     public static byte[] bitwiseXOR(byte[] one, byte[] two) {
         if(one.length == two.length) {
             for(int i = 0; i < one.length; i++) {
                 one[i] ^= two[i];
             }
         }
         return one;
     }
     public static byte[] bitwiseOR(byte[] one, byte[] two) {
         if(one.length == two.length) {
             for(int i = 0; i < one.length; i++) {
                 one[i] |= two[i];
             }
         }
         return one;
     }
     public static byte[] bitwiseInvert(byte[] in) {
         for(int i = 0; i < in.length; i++) {
             in[i] ^= 0xFF;
         }
         return in;        
     }
     public static String join(String[] s, String delim) {
       if (s.length==0) return null;
       String out= s[0];
       for (int x=1;x<s.length;++x){
           out += delim;
           out +=s[x];
       }
       return out;
     }    
     public static byte writeByByte(int a) {
         switch(a){
             case 1:
                 return (byte) 128;
             case 2:
                 return (byte) 192;
             case 3:
                 return (byte) 224;
             case 4:
                 return (byte) 240;
             case 5:
                 return (byte) 248;
             case 6:
                 return (byte) 252;
             case 7:
                 return (byte) 254;
             case 8:
                 return (byte) 255;
             default: 
                 return (byte) 0;
         }
     }
     public static int writeFromByte(byte a) {
         switch((int)a&0xFF){
             case 128:
                 return 1;
             case 192:
                 return 2;
             case 224:
                 return 3;
             case 240:
                 return 4;
             case 248:
                 return 5;
             case 252:
                 return 6;
             case 254:
                 return 7;
             case 255:
                 return 8;
             default:
                 return 0;
         }
     }
     public static byte[] writeByBits(int a) {
         int retLen = (int) Math.ceil((double)a/(double)8);
         byte[] ret = new byte[retLen];
         for(int i = 0; i < retLen; i++){
             if(a >= 8) {
                 ret[i] = writeByByte(8);
                 a -=8;
             } else ret[i] = writeByByte(a%8);
         }
         return ret;
     }
     public static int countBits(byte[] arr) {
         int ret = 0;
         for(int i=0; i < arr.length; i++){
             ret += writeFromByte(arr[i]);
         }
         return ret;
     }
     public static void printBytes(byte[] a) {
         for(int i = 0; i < a.length; i++)
             System.out.println(a[i]&0xFF);
     }
     public static void printBytesHex(byte[] a) {
         for(int i = 0; i < a.length; i++)
             System.out.println(Integer.toHexString(a[i]&0xFF));        
     }
 
     public static byte[] getNetworkAddressBytes(byte[] addr, byte[] netmask) {
         return bitwiseAnd(addr, netmask);
     }
     public static InetAddress getNetworkAddress(byte[] addr, byte[] netmask) {
         try {
             return InetAddress.getByAddress(bitwiseAnd(addr, netmask));
         } catch (UnknownHostException ex) {
             Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
         }
         return InetAddress.getLoopbackAddress();
     }    
     public static InetAddress getNetworkAddress(InetAddress addr, byte[] netmask) {
         return getNetworkAddress(addr.getAddress(), netmask);        
     }
     public static InetAddress getNetworkAddress(InetAddress addr, int netmask) {        
         return getNetworkAddress(addr.getAddress(), writeByBits(netmask));        
     }
     public static int getNetMaskBits(String mask) {
         String[] arr = mask.split("\\.");
         int[] arrint = new int[arr.length];
         for(int i = 0; i < arr.length; i++) arrint[i] = Integer.parseInt(arr[i]);
         byte[] arrbyte = new byte[arr.length];
         for(int i = 0; i < arr.length; i++) arrbyte[i] = (byte) arrint[i];
         return countBits(arrbyte);
     }
     public static String genNetMask4(int bits){
         byte[] netmask = writeByBits(bits);
         System.out.println(netmask.length);
         int[] netmaskint = new int[netmask.length];
         for(int i = 0; i < netmask.length; i++) netmaskint[i] = (int)netmask[i]&0xFF;
         String[] netmaskstr = new String[netmask.length];
         for(int i = 0; i < netmask.length; i++) netmaskstr[i] = Integer.toString(netmaskint[i]);  
         return join(netmaskstr, ".");        
     }
     public static String genNetMaskHex(int bits){
         byte[] netmask = writeByBits(bits);
         int[] netmaskint = new int[netmask.length];
         for(int i = 0; i < netmask.length; i++) netmaskint[i] = (int)netmask[i]&0xFF;
         String[] netmaskstr = new String[netmask.length];
         for(int i = 0; i < netmask.length; i++) netmaskstr[i] = Integer.toHexString(netmaskint[i]);  
         return join(netmaskstr, ":");         
     }
     public static byte[] genNetMask(int bits) {
         return writeByBits(bits);      
     }
     public static byte[] getNetBroadcastByteWise(byte[] addr, byte[] mask) {
         byte[] ret = getNetworkAddressBytes(addr, mask);
        byte[] cmp = bitwiseInvert(mask);
         return bitwiseOR(ret, cmp);
     }
     public static InetAddress getNetBroadcast(byte[] addr, byte[] mask) {
         try {
             return InetAddress.getByAddress(getNetBroadcastByteWise(addr, mask));
         } catch (UnknownHostException ex) {
             Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
         }
         return InetAddress.getLoopbackAddress();
     }
     public static InetAddress getNetBroadcast(InetAddress addr, byte[] mask) {
         return getNetBroadcast(addr.getAddress(), mask);
     }
     public static InetAddress getNetBroadcast(InetAddress addr, int mask) {
         return getNetBroadcast(addr.getAddress(), writeByBits(mask));
     }
     public static void main(String[] args) {
         try {
            System.out.println(getNetBroadcast(InetAddress.getByName("192.168.0.1"), 26));
         } catch (UnknownHostException ex) {
             Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }
