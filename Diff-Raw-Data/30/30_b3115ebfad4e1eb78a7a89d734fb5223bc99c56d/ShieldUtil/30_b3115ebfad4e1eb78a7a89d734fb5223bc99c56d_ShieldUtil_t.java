 /*
  * McZLauncher (ZeTRiX's Minecraft Launcher)
  * Copyright (C) 2012 Evgen Yanov <http://www.zetlog.ru>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program (In the "_License" folder). If not, see <http://www.gnu.org/licenses/>.
 */
 
 package ru.zetrix.settings;
 
 import java.io.BufferedInputStream;
import java.io.File;
 import java.io.FileInputStream;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Formatter;
 
 public class ShieldUtil {
     public static String ShaHash(String pass) {
         String sha1 = "";
         try {
             MessageDigest crypt = MessageDigest.getInstance("SHA-1");
             crypt.reset();
             crypt.update(pass.getBytes("UTF-8"));
             sha1 = byteToHex(crypt.digest());
         } catch(NoSuchAlgorithmException e) {
             e.printStackTrace();
         }  catch(UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         return sha1;
     }
     
     public static String GetMAC() {
     	InetAddress ip;
 	try {
 		ip = InetAddress.getLocalHost();
 		print("Current IP address: " + ip.getHostAddress());
  
 		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
 		byte[] mac = network.getHardwareAddress();
  
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < mac.length; i++) {
 			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                         if (sb.toString().equals("")) {
                             print("Current MAC address cannot be identified!");                            
                             return "Fail";
                         }
 		}
 		print("Current MAC address: " + sb.toString());
 		return sb.toString();
  
 	} catch (UnknownHostException e) {
 		e.printStackTrace();
 	} catch (SocketException e){
 		e.printStackTrace();
 	}
         return "Fail";
     }
     
     private static String byteToHex(final byte[] hash) {
         Formatter formatter = new Formatter();
         for (byte b : hash) {
             formatter.format("%02x", b);
         }
         return formatter.toString();
     }
     
     public static String FileHash(String FilePath) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("SHA1");
 
 			FileInputStream fis = new FileInputStream(FilePath);
 			DigestInputStream dis = new DigestInputStream(fis, md);
 			BufferedInputStream bis = new BufferedInputStream(dis);
 
 			while (true) {
 				int b = bis.read();
 				if (b == -1)
 					break;
 			}
  
 			BigInteger bi = new BigInteger(md.digest());
 			return(bi.toString(16));
  
 		} catch (Exception e) {
 			return(e.getMessage());
 		}
 	}
     
    public static long FileSize(String FilePath) {
        File chfile = new File(FilePath);
        if(chfile.exists()) {
            long chflenght = chfile.length();
            print("Размер " + FilePath + " вычислен: " + chflenght);
            return chflenght;
        }
        print("Размер " + FilePath + " вычислен не был.");
        return 0;
    }
    
     private static void print(String str) {
         Debug.Logger(str);
     }
 }
