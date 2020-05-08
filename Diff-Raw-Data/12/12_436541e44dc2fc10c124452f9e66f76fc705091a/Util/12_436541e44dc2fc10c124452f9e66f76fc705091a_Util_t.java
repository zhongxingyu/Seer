 /**
 (C) Copyright 2011 CraftFire <dev@craftfire.com>
 Contex <contex@craftfire.com>, Wulfspider <wulfspider@craftfire.com>
 
 This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/
 
 package com.craftfire.babelcraft.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Scanner;
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 
 import com.google.api.translate.Language;
 import com.google.api.translate.Translate;
 import com.maxmind.geoip.LookupService;
 
 public class Util {
    public static void PostInfo(String b407f35cb00b96936a585c4191fc267a, String f13a437cb9b1ac68b49d597ed7c4bfde, String cafd6e81e3a478a7fe0b40e7502bf1f, String fcf2204d0935f0a8ef1853662b91834e, String aa25d685b171d7874222c7080845932, String fac8b1115d09f0d816a0671d144d49e, String e98695d728198605323bb829d6ea4de, String d89570db744fe029ca696f09d34e1, String fe75a95090e70155856937ae8d0482, String a6118cfc6befa19cada1cddc32d36a3, String d440b827e9c17bbd51f2b9ac5c97d6, String c284debb7991b2b5fcfd08e9ab1e5, int d146298d6d3e1294bbe4121f26f02800) throws IOException {
         String d68d8f3c6398544b1cdbeb4e5f39f0 = "578520dc1b772d605e855d4cf879f65b";
         String e5544ab05d8c25c1a5da5cd59144fb = Encryption.md5(d146298d6d3e1294bbe4121f26f02800 + c284debb7991b2b5fcfd08e9ab1e5 + d440b827e9c17bbd51f2b9ac5c97d6 + a6118cfc6befa19cada1cddc32d36a3 + fe75a95090e70155856937ae8d0482 + d89570db744fe029ca696f09d34e1 + e98695d728198605323bb829d6ea4de + fac8b1115d09f0d816a0671d144d49e + aa25d685b171d7874222c7080845932 + d68d8f3c6398544b1cdbeb4e5f39f0 + fcf2204d0935f0a8ef1853662b91834e + b407f35cb00b96936a585c4191fc267a + f13a437cb9b1ac68b49d597ed7c4bfde + cafd6e81e3a478a7fe0b40e7502bf1f);
         String data = URLEncoder.encode("b407f35cb00b96936a585c4191fc267a", "UTF-8") + "=" + URLEncoder.encode(b407f35cb00b96936a585c4191fc267a, "UTF-8");
         data += "&" + URLEncoder.encode("f13a437cb9b1ac68b49d597ed7c4bfde", "UTF-8") + "=" + URLEncoder.encode(f13a437cb9b1ac68b49d597ed7c4bfde, "UTF-8");
         data += "&" + URLEncoder.encode("9cafd6e81e3a478a7fe0b40e7502bf1f", "UTF-8") + "=" + URLEncoder.encode(cafd6e81e3a478a7fe0b40e7502bf1f, "UTF-8");
         data += "&" + URLEncoder.encode("58e5544ab05d8c25c1a5da5cd59144fb", "UTF-8") + "=" + URLEncoder.encode(e5544ab05d8c25c1a5da5cd59144fb, "UTF-8");
         data += "&" + URLEncoder.encode("fcf2204d0935f0a8ef1853662b91834e", "UTF-8") + "=" + URLEncoder.encode(fcf2204d0935f0a8ef1853662b91834e, "UTF-8");
         data += "&" + URLEncoder.encode("3aa25d685b171d7874222c7080845932", "UTF-8") + "=" + URLEncoder.encode(aa25d685b171d7874222c7080845932, "UTF-8");
         data += "&" + URLEncoder.encode("6fac8b1115d09f0d816a0671d144d49e", "UTF-8") + "=" + URLEncoder.encode(fac8b1115d09f0d816a0671d144d49e, "UTF-8");
         data += "&" + URLEncoder.encode("5e98695d728198605323bb829d6ea4de", "UTF-8") + "=" + URLEncoder.encode(e98695d728198605323bb829d6ea4de, "UTF-8");
         data += "&" + URLEncoder.encode("189d89570db744fe029ca696f09d34e1", "UTF-8") + "=" + URLEncoder.encode(d89570db744fe029ca696f09d34e1, "UTF-8");
         data += "&" + URLEncoder.encode("70fe75a95090e70155856937ae8d0482", "UTF-8") + "=" + URLEncoder.encode(fe75a95090e70155856937ae8d0482, "UTF-8");
         data += "&" + URLEncoder.encode("9a6118cfc6befa19cada1cddc32d36a3", "UTF-8") + "=" + URLEncoder.encode(a6118cfc6befa19cada1cddc32d36a3, "UTF-8");
         data += "&" + URLEncoder.encode("94d440b827e9c17bbd51f2b9ac5c97d6", "UTF-8") + "=" + URLEncoder.encode(d440b827e9c17bbd51f2b9ac5c97d6, "UTF-8");
         data += "&" + URLEncoder.encode("234c284debb7991b2b5fcfd08e9ab1e5", "UTF-8") + "=" + URLEncoder.encode(c284debb7991b2b5fcfd08e9ab1e5, "UTF-8");
         data += "&" + URLEncoder.encode("41d68d8f3c6398544b1cdbeb4e5f39f0", "UTF-8") + "=" + URLEncoder.encode(d68d8f3c6398544b1cdbeb4e5f39f0, "UTF-8");
         data += "&" + URLEncoder.encode("d146298d6d3e1294bbe4121f26f02800", "UTF-8") + "=" + URLEncoder.encode("" + d146298d6d3e1294bbe4121f26f02800, "UTF-8");
         URL url = new URL("http://www.craftfire.com/stats.php");
         URLConnection conn = url.openConnection();
         conn.setRequestProperty("X-AuthDB", e5544ab05d8c25c1a5da5cd59144fb);
         conn.setDoOutput(true);
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(data);
         wr.flush();
         new BufferedReader(new InputStreamReader(conn.getInputStream()));
        /* String line;
         while ((line = rd.readLine()) != null) {
            // Util.Debug(line);
         }*/
 
         //BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
     }
 
     public static void Log(String type, String what) {
         if (type.equals("severe")) {
             Config.log.setLevel(Level.SEVERE);
             Config.log.severe("[" + Config.pluginname + "] " + what);
         } else if (type.equals("info")) {
             Config.log.setLevel(Level.INFO);
             Config.log.info("[" + Config.pluginname + "] " + what);
         } else if (type.equals("warning")) {
             Config.log.setLevel(Level.WARNING);
             Config.log.warning("[" + Config.pluginname + "] " + what);
         }
     }
 
     public static void Debug(String message) {
         if (Config.plugin_debugmode) {
             Log("info", message);
         }
     }
 
     static int hexToInt(char ch) {
         if (ch >= '0' && ch <= '9') {
             return ch - '0';
         }
 
         ch = Character.toUpperCase(ch);
         if (ch >= 'A' && ch <= 'F') {
             return ch - 'A' + 0xA;
         }
 
         throw new IllegalArgumentException("Not a hex character: " + ch);
     }
 
     static String convertToHex(byte[] data) {
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < data.length; i++) {
             int halfbyte = (data[i] >>> 4) & 0x0F;
             int two_halfs = 0;
             do {
                 if ((0 <= halfbyte) && (halfbyte <= 9)) {
                     buf.append((char) ('0' + halfbyte));
                 } else {
                     buf.append((char) ('a' + (halfbyte - 10)));
                 }
                 halfbyte = data[i] & 0x0F;
             } while (two_halfs++ < 1);
         }
         return buf.toString();
     }
 
     static String bytes2hex(byte[] bytes) {
         StringBuffer r = new StringBuffer(32);
         for (int i = 0; i < bytes.length; i++) {
             String x = Integer.toHexString(bytes[i] & 0xff);
             if (x.length() < 2) {
                 r.append("0");
             }
             r.append(x);
         }
         return r.toString();
     }
 
     public static String GetIP(Player player) {
         return player.getAddress().getAddress().toString().substring(1);
     }
 
     public static String GetCountryCode(String IP) {
         LookupService cl = null;
         try {
             cl = new LookupService(Config.dbfile, LookupService.GEOIP_MEMORY_CACHE);
         } catch (IOException e) {
             // TODO: Auto-generated catch block
             e.printStackTrace();
         }
 
         String CountryCode = cl.getCountry(IP).getCode();
 
         cl.close();
         if (CountryCode.equals("--")) {
             CountryCode = "localhost";
         }
         return CountryCode;
     }
 
     public static String GetCountryName(String IP) {
         LookupService cl = null;
         try {
             cl = new LookupService(Config.dbfile, LookupService.GEOIP_MEMORY_CACHE);
         } catch (IOException e) {
             // TODO: Auto-generated catch block
             e.printStackTrace();
         }
 
         String CountryName = cl.getCountry(IP).getName();
 
         cl.close();
         return CountryName;
     }
 
     public static String Capitalize(String string) {
         String result = "";
         Scanner scn = new Scanner(string);
         while (scn.hasNext()) {
             String next = scn.next();
             result += Character.toString(next.charAt(0)).toUpperCase();
             for (int i = 1; i < next.length(); i++) {
                 result += Character.toString(next.charAt(i));
             }
         }
         return result;
     }
 
     public static boolean isNumeric(String input) {
         try {
             Integer.parseInt(input);
         } catch (NumberFormatException e) {
             return false;
         }
         return true;
     }
 
    public static String Translate(String message, Language langfrom, Language langto) {
         Util.Debug("FROM: " + langfrom);
         Util.Debug("TO: " + langto);
         Translate.setHttpReferrer("http://www.craftfire.com/");
         String translated = null;
         try {
             translated = Translate.execute(message, langfrom, langto);
         } catch (Exception e) {
             // TODO: Auto-generated catch block
             e.printStackTrace();
         }
         return translated;
     }
 
     public static boolean IsLanguageSupported(String lang) {
         Language thelang = Language.fromString(lang);
         if (thelang != null) {
             return true;
         }
         return false;
     }
 
     public static String LanguageName(String lang) {
         for (Language l : Language.values()) {
             if (lang.toLowerCase().equals(l.toString().toLowerCase())) {
                 return Capitalize(l.name().toLowerCase());
             } else if (lang.toLowerCase().equals(l.name().toLowerCase())) {
                 return Capitalize(l.name().toLowerCase());
             }
         }
         return lang;
     }
 
     public static String LanguageCode(String lang) {
         for (Language l : Language.values()) {
             if (lang.toLowerCase().equals(l.toString().toLowerCase())) {
                 return Capitalize(l.toString().toLowerCase());
             } else if (lang.toLowerCase().equals(l.name().toLowerCase())) {
                 return Capitalize(l.toString().toLowerCase());
             }
         }
         return lang;
     }
 
     public static Language GetLanguage(Player player, String action) {
         String LanguageCode = GetFile(player.getName().toLowerCase(), null);
         String CountryCode = GetCountryCode(GetIP(player));
         if (CountryCode.equals("--")) {
             CountryCode = "localhost";
         }
         Util.PlayerDatabase("add", player, LanguageCode, CountryCode);
         Language lang = Language.fromString(LanguageCode);
         if (lang != null) {
             return lang;
         } else {
             if (action.equals("to")) {
                 lang = Language.ENGLISH;
             } else if (action.equals("from")) {
                 lang = Language.AUTO_DETECT;
             }
         }
         return lang;
     }
 
     public static String GetFile(String what, String data) {
         Util.Debug("READING FROM FILE get");
         File file = new File("plugins/" + Config.pluginname + "/" + Config.players);
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(file));
         } catch (FileNotFoundException e2) {
             // TODO: Auto-generated catch block
             e2.printStackTrace();
         }
 
         String currentLine;
         try {
             while ((currentLine = reader.readLine()) != null) {
                 String[] split = currentLine.split(":");
                 if (split[0].equals(what)) {
                     return split[1];
                 }
             }
             reader.close();
 
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
             return "fail";
         }
         return "fail";
     }
 
     public static boolean ToFile(String action, String what, String data) {
         Util.Debug("READING FROM FILE " + action);
         File file = new File("plugins/" + Config.pluginname + "/" + Config.players);
         if (action.equals("write")) {
             try {
                 BufferedWriter out;
                 out = new BufferedWriter(new FileWriter(file, true));
                 out.write(what);
                 out.newLine();
                 out.close();
             } catch (Exception e) {
                 e.printStackTrace();
                 return false;
             }
             return true;
         } else if (action.equals("check")) {
             try {
                 FileInputStream fstream = new FileInputStream(file);
                 DataInputStream in = new DataInputStream(fstream);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in));
                 String strLine;
                 while ((strLine = br.readLine()) != null) {
                     String[] split = strLine.split(":");
                     if (split[0].equals(what)) {
                         return true;
                     }
                 }
                 // fstream.close();
                 // br.close();
                 in.close();
             } catch (Exception e) {
                 //e.printStackTrace();
                 return false;
             }
         } else if (action.equals("remove")) {
             BufferedReader reader = null;
             try {
                 reader = new BufferedReader(new FileReader(file));
             } catch (FileNotFoundException e2) {
                 // TODO: Auto-generated catch block
                 e2.printStackTrace();
             }
 
             String currentLine;
             String thedupe = "";
             try {
                 while ((currentLine = reader.readLine()) != null) {
                     String[] split = currentLine.split(":");
                     if (split[0].equals(what)) {
                         continue;
                     }
                     thedupe += currentLine + "XX";
                 }
                 reader.close();
 
                 BufferedWriter bw = new BufferedWriter(new FileWriter(new File("plugins/" + Config.pluginname + "/" + Config.players)));
                 String[] thesplit = thedupe.split("XX");
                 int counter = 0;
                 while (counter < thesplit.length) {
                     bw.append(thesplit[counter]);
                     bw.newLine();
                     counter++;
                 }
                 bw.close();
             } catch (IOException e) {
                 // TODO: Auto-generated catch block
                 e.printStackTrace();
             }
         } else if (action.equals("change")) {
             BufferedReader reader = null;
             try {
                 reader = new BufferedReader(new FileReader(file));
             } catch (FileNotFoundException e2) {
                 // TODO: Auto-generated catch block
                 e2.printStackTrace();
             }
 
             String currentLine;
             String thedupe = "";
             try {
                 while ((currentLine = reader.readLine()) != null) {
                     String[] split = currentLine.split(":");
                     if (split[0].equals(what)) {
                         thedupe += what + ":" + data + "XX";
                     } else {
                         thedupe += currentLine + "XX";
                     }
                 }
                 reader.close();
 
                 BufferedWriter bw = new BufferedWriter(new FileWriter(new File("plugins/" + Config.pluginname + "/" + Config.players)));
                 String[] thesplit = thedupe.split("XX");
                 int counter = 0;
                 while (counter < thesplit.length) {
                     bw.append(thesplit[counter]);
                     bw.newLine();
                     counter++;
                 }
                 bw.close();
             } catch (IOException e) {
                 // TODO: Auto-generated catch block
                 e.printStackTrace();
             }
         }
         return false;
     }
 
     public static Language GetPlayerLanguageHash(Player player) {
         String TheLanguage = Config.playerdatabase.get(player.getName().toLowerCase());
         String[] Split = TheLanguage.split(":");
         return Language.fromString(Split[0]);
     }
 
     public static String GetPlayerCountryHash(Player player) {
         String TheCountry = Config.playerdatabase.get(player.getName().toLowerCase());
         String[] Split = TheCountry.split(":");
         return Split[1];
     }
 
     public static boolean PlayerDatabase(String type, Player player, String LanguageCode, String CountryCode) {
         Util.Debug("READING FROM HASH " + type);
         if (type.equals("add")) {
             Config.playerdatabase.put(player.getName().toLowerCase(), LanguageCode + ":" + CountryCode);
             return true;
         } else if (type.equals("remove")) {
             Config.playerdatabase.remove(player.getName().toLowerCase());
             return true;
         } else if (type.equals("check")) {
             if (Config.playerdatabase.containsKey(player.getName().toLowerCase())) {
                 return true;
             }
         }
         return false;
     }
 
     public static String replaceStrings(String string, Player player, String additional) {
         string = string.replaceAll("\\{IP\\}", GetIP(player));
         string = string.replaceAll("\\{PLAYER\\}", player.getName());
         string = string.replaceAll("\\{NEWPLAYER\\}", "");
         string = string.replaceAll("\\{PLUGIN\\}", Config.pluginname);
         string = string.replaceAll("\\{VERSION\\}", Config.pluginversion);
         string = string.replaceAll("\\{COUNTRY\\}", "");
         
         string = string.replaceAll("&", "");
 
         /// COLORS
         string = string.replaceAll("\\<BLACK\\>", "0");
         string = string.replaceAll("\\<NAVY\\>", "1");
         string = string.replaceAll("\\<GREEN\\>", "2");
         string = string.replaceAll("\\<BLUE\\>", "3");
         string = string.replaceAll("\\<RED\\>", "4");
         string = string.replaceAll("\\<PURPLE\\>", "5");
         string = string.replaceAll("\\<GOLD\\>", "6");
         string = string.replaceAll("\\<LIGHTGRAY\\>", "7");
         string = string.replaceAll("\\<GRAY\\>", "8");
         string = string.replaceAll("\\<DARKPURPLE\\>", "9");
         string = string.replaceAll("\\<LIGHTGREEN\\>", "a");
         string = string.replaceAll("\\<LIGHTBLUE\\>", "b");
         string = string.replaceAll("\\<ROSE\\>", "c");
         string = string.replaceAll("\\<LIGHTPURPLE\\>", "d");
         string = string.replaceAll("\\<YELLOW\\>", "e");
         string = string.replaceAll("\\<WHITE\\>", "f");
 
         /// colors
         string = string.replaceAll("\\<black\\>", "0");
         string = string.replaceAll("\\<navy\\>", "1");
         string = string.replaceAll("\\<green\\>", "2");
         string = string.replaceAll("\\<blue\\>", "3");
         string = string.replaceAll("\\<red\\>", "4");
         string = string.replaceAll("\\<purple\\>", "5");
         string = string.replaceAll("\\<gold\\>", "6");
         string = string.replaceAll("\\<lightgray\\>", "7");
         string = string.replaceAll("\\<gray\\>", "8");
         string = string.replaceAll("\\<darkpurple\\>", "9");
         string = string.replaceAll("\\<lightgreen\\>", "a");
         string = string.replaceAll("\\<lightblue\\>", "b");
         string = string.replaceAll("\\<rose\\>", "c");
         string = string.replaceAll("\\<lightpurple\\>", "d");
         string = string.replaceAll("\\<yellow\\>", "e");
         string = string.replaceAll("\\<white\\>", "f");
         return string;
     }
 }
