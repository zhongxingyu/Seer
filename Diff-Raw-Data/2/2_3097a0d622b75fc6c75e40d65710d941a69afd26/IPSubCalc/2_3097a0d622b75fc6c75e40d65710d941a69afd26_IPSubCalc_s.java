 package ipsubcalc;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * IP Subnet Calculating Algorithms
  * @author G89390
  */
 public class IPSubCalc {
 
     public static byte[] bitwiseAnd(byte[] one, byte[] two) {
         if(one.length == two.length) {
             for(int i=0; i<one.length; i++) {
                 one[i] &= two[i];
             }
         }
         return one;
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
     public static byte[] writeByNum(int a) {
         int retLen = (int) Math.ceil((double)a/(double)8);
         System.out.println(retLen);
         byte[] ret = new byte[retLen+1];
         for(int i = 0; i <= retLen; i++){
             if(a >= 8) {
                 ret[i] = writeByByte(8);
                 a -=8;
             } else ret[i] = writeByByte(a%8);
         }
         return ret;
     }
     public static int countByBytes(byte[] arr) {
         int ret = 0;
         for(int i=0; i < arr.length; i++){
            ret += writeByByte()
         }
         return ret;
     }
     public static void printBytes(byte[] a) {
         for(int i = 0; i < a.length; i++)
             System.out.println(a[i]&0xFF);
     }
     public static InetAddress getNetworkAddressByteWise(byte[] addr, byte[] netmask) {
         try {
             printBytes(bitwiseAnd(addr, netmask));
             return InetAddress.getByAddress(bitwiseAnd(addr, netmask));
         } catch (UnknownHostException ex) {
             Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
         }
         return InetAddress.getLoopbackAddress();
     }
     public static InetAddress getNetworkAddress(InetAddress addr, byte[] netmask) {
         return getNetworkAddressByteWise(addr.getAddress(), netmask);        
     }
     public static InetAddress getNetworkAddress(InetAddress addr, int netmask) {        
         return getNetworkAddressByteWise(addr.getAddress(), writeByNum(netmask));        
     }
     public static void main(String[] args) {
         try {
             System.out.println(getNetworkAddress(InetAddress.getByName("192.168.33.1"), 22));
         } catch (UnknownHostException ex) {
             Logger.getLogger(IPSubCalc.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
