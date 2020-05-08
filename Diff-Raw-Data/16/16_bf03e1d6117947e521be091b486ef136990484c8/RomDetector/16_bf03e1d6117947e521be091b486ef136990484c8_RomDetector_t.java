 package com.github.chenxiaolong.dualbootswitcher;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Context;
 
 public class RomDetector {
     private static ArrayList<String> mRoms;
 
     public static boolean isExistsRom(String path) {
         File rom = new File(path);
         if (rom.exists() && rom.isDirectory() && rom.canRead()) {
             return true;
         } else {
             Commands.CommandResult result = null;
             try {
                 result = Commands.runCommand("ls " + path);
             } catch (Exception e) {
             }
             return result != null && result.exitCode == 0;
         }
     }
 
     public static String[] getRoms() {
         if (mRoms == null) {
             mRoms = new ArrayList<String>();
 
            // Primary - either:
            // - No /raw-system and have /system/build.prop
            // - Have /raw-system/build.prop
            if (!new File("/raw-system").exists()
                    && new File("/system/build.prop").exists()) {
                 mRoms.add("/system");
            } else if (new File("/raw-system/build.prop").exists()) {
                mRoms.add("/raw-system");
             }
 
             if (isExistsRom("/raw-system/dual")) {
                 mRoms.add("/raw-system/dual");
             } else if (isExistsRom("/system/dual")) {
                 mRoms.add("/system/dual");
             }
 
             int max = 10;
             for (int i = 0; i < max; i++) {
                 if (isExistsRom("/raw-cache/multi-slot-" + i + "/system")) {
                     mRoms.add("/raw-cache/multi-slot-" + i + "/system");
                 } else if (isExistsRom("/cache/multi-slot-" + i + "/system")) {
                     mRoms.add("/cache/multi-slot-" + i + "/system");
                 }
             }
         }
 
         return mRoms.toArray(new String[mRoms.size()]);
     }
 
     private static String getDefaultName(Context context, String rom) {
        if (rom.equals("/system") || rom.equals("/raw-system")) {
             return context.getString(R.string.primary);
         } else if (rom.contains("system/dual")) {
             return context.getString(R.string.secondary);
         } else if (rom.contains("cache/multi-slot-")) {
             Pattern p = Pattern
                     .compile("^/(?:raw-)?cache/multi-slot-([^/]+)/system$");
             Matcher m = p.matcher(rom);
             String num = "UNKNOWN";
             if (m.find()) {
                 num = m.group(1);
             }
             return String.format(context.getString(R.string.multislot), num);
         } else {
             return "UNKNOWN";
         }
     }
 
     public static String getName(Context context, String rom) {
         // If no user input
         return getDefaultName(context, rom);
     }
 
     public static String getId(String rom) {
        if (rom.equals("/system") || rom.equals("/raw-system")) {
             return "primary";
         } else if (rom.contains("system/dual")) {
             return "secondary";
         } else if (rom.contains("cache/multi-slot-")) {
             Pattern p = Pattern
                     .compile("^/(?:raw-)?cache/(multi-slot-[^/]+)/system$");
             Matcher m = p.matcher(rom);
             if (m.find()) {
                 return m.group(1);
             } else {
                 return null;
             }
         } else {
             return null;
         }
     }
 
     public static String getVersion(String rom) {
         File buildprop = new File(rom + "/build.prop");
 
         // Make sure build.prop exists
         if (!buildprop.exists()) {
             try {
                 if (Commands.runCommand("ls " + buildprop.getPath()).exitCode != 0) {
                     return null;
                 }
             } catch (Exception e) {
                 return null;
             }
         }
 
         Properties prop = new Properties();
 
         if (!buildprop.canRead()) {
             try {
                 Commands.CommandResult result = Commands.runCommand("cat "
                         + buildprop.getPath());
                 if (result.exitCode != 0) {
                     return null;
                 }
                 prop.load(new StringReader(result.output));
             } catch (Exception e) {
                 return null;
             }
         } else {
             FileInputStream fis = null;
             try {
                 fis = new FileInputStream(buildprop.getPath());
                 prop.load(fis);
             } catch (Exception e) {
                 return null;
             } finally {
                 try {
                     if (fis != null) {
                         fis.close();
                     }
                 } catch (Exception e) {
                 }
             }
         }
 
         if (prop.containsKey("ro.modversion")) {
             return prop.getProperty("ro.modversion");
         } else if (prop.containsKey("ro.cm.version")) {
             return prop.getProperty("ro.cm.version");
         } else if (prop.containsKey("ro.omni.version")) {
             return prop.getProperty("ro.omni.version");
         } else if (prop.containsKey("ro.build.display.id")) {
             return prop.getProperty("ro.build.display.id");
         } else {
             return null;
         }
     }
 }
