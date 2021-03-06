 /*
  * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.tulskiy.musique.util;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.channels.ClosedChannelException;
 import java.util.Formatter;
 import java.util.Locale;
 
 /**
  * @Author: Denis Tulskiy
  * @Date: 21.11.2008
  */
 public class Util {
     public static String htmlToString(String string) {
         String ans = string.replaceAll("&quot;", "\"");
         ans = ans.replaceAll("&amp;", "&");
         ans = ans.replaceAll("&lt;", "<");
         ans = ans.replaceAll("&gt;", ">");
         ans = ans.replaceAll("<.+?>", "");
 
         return ans;
     }
 
    public static String formatSeconds(double seconds, int precision) {
        int min = (int) ((Math.round(seconds)) / 60);
         int hrs = min / 60;
         if (min > 0) seconds -= min * 60;
        if (seconds < 0) seconds = 0;
         if (hrs > 0) min -= hrs * 60;
         int days = hrs / 24;
         if (days > 0) hrs -= days * 24;
         int weeks = days / 7;
         if (weeks > 0) days -= weeks * 7;
 
         StringBuilder builder = new StringBuilder();
         if (weeks > 0) builder.append(weeks).append("wk ");
         if (days > 0) builder.append(days).append("d ");
         if (hrs > 0) builder.append(hrs).append(":");
         if (hrs > 0 && min < 10) builder.append("0");
         builder.append(min).append(":");
         int n = precision + ((precision == 0) ? 2 : 3);
         String fmt = "%0" + n + "." + precision + "f";
         builder.append(new Formatter().format(Locale.US, fmt, seconds));
         return builder.toString();
     }
 
     public static String samplesToTime(long samples, int sampleRate, int precision) {
         if (samples <= 0)
             return "-:--";
        double seconds = AudioMath.samplesToMillis(samples, sampleRate) / 1000f;
        return formatSeconds(seconds, precision);
     }
 
     public static String getFileExt(File file) {
         return getFileExt(file.getName());
     }
 
     public static String removeExt(String s) {
         int index = s.lastIndexOf(".");
         if (index == -1) index = s.length();
         return s.substring(0, index);
     }
 
     public static String getFileExt(String fileName) {
         int pos = fileName.lastIndexOf(".");
         if (pos == -1) return "";
         return fileName.substring(pos + 1).toLowerCase();
     }
 
     public static String longest(String... args) {
         if (args.length == 0) return "";
         String longest = args[0] == null ? "" : args[0];
         for (String s : args)
             if (s != null && s.length() > longest.length())
                 longest = s;
         return longest;
     }
 
     public static String firstNotEmpty(String... values) {
         for (String value : values) {
             if (value != null && !value.isEmpty())
                 return value;
         }
 
         return null;
     }
 
     public static boolean isEmpty(String s) {
         return s == null || s.trim().isEmpty();
     }
 
     public static String humanize(String property) {
         String s = property.replaceAll("(?=\\p{Upper})", " ");
         return s.substring(0, 1).toUpperCase() + s.substring(1);
     }
 
     public static String capitalize(String str) {
         str = str.replaceAll("&", "and");
         String[] strings = str.split(" ");
         final StringBuilder sb = new StringBuilder();
 
         for (String s : strings) {
             s = s.toLowerCase();
             sb.append(s.substring(0, 1).toUpperCase());
             sb.append(s.substring(1)).append("_");
         }
         sb.deleteCharAt(sb.length() - 1);
 
         return sb.toString();
     }
 
     public static Color getContrastColor(Color bg) {
         int threshold = 105;
         int delta = (int) (bg.getRed() * 0.299 + bg.getGreen() * 0.587 + bg.getBlue() * 0.114);
         return (255 - delta < threshold) ? Color.black : Color.white;
     }
 
     public static void fixIconTextGap(JComponent menu) {
         if (UIManager.getLookAndFeel().getName().contains("Nimbus")) {
             Component[] components = menu.getComponents();
             for (Component component : components) {
                 if (component instanceof AbstractButton) {
                     AbstractButton b = (AbstractButton) component;
                     b.setIconTextGap(0);
                 }
 
                 if (component instanceof JMenu)
                     fixIconTextGap(((JMenu) component).getPopupMenu());
             }
         }
     }
 
     public static boolean copy(FileInputStream from, FileInputStream to) {
         try {
             long transferred = 0;
             long length = from.getChannel().size();
             int chunkSize = 10000000;
             while (transferred < length) {
                 transferred += from.getChannel().transferTo(
                         transferred, chunkSize, to.getChannel());
             }
             from.close();
             to.close();
             return true;
         } catch (ClosedChannelException ignore) {
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return false;
     }
 }
