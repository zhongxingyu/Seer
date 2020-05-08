 package com.core.util;
 
 
 import javax.swing.*;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.Date;
 import java.util.Calendar;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 /**
  * The model for the Lesson component, which tracks the text, icons, and enabled state
  * of each of the buttons, as well as the current page that is displayed. Note that
  * the model, in its current form, is not intended to be subclassed.
  */
 
 
 public class Utils {
     public static JRadioButton getSelection(ButtonGroup group) {
         for (Enumeration e=group.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            if (b.getModel() == group.getSelection()) {
                return b;
            }
         }
         return null;
     }
     public static String getToday() {
         DateFormat df = new SimpleDateFormat("yyyyMMdd");
         // Get the date today using Calendar object.
         Date today = Calendar.getInstance().getTime();
         // Using DateFormat format method we can create a string
         // representation of a date with the defined format.
         return df.format(today);
     }
 
     public static String Hash2String(HashMap submit) {
         String str = "";
         int i = 0;
         for (Object key : submit.keySet()) {
              i++;
             if (key.equals("linebreak"))
                  str += "\n";
             else
                 str += key +":"+submit.get(key) +"\n";
         }
         return str;
     }
 
     public static String Hash2CSV(HashMap submit) {
         int i = 0;
         String row = "";
         for (Object key : submit.keySet()) {
              i++;
              row += "\"" + key + "\"";
              if (i < submit.size()) {
                  row += ",";
              }
         }
         row = row + "\n";
         i=0;
         for (Object key : submit.keySet()) {
             row += "\"" + (String)submit.get(key) + "\"";
             i++;
             if (i < submit.size()) {
                row += ",";
             }
         }
         row += "\n";
         return row;
     }
 
     public static void WriteFile(String fName, String content) {
         try {
             FileWriter fstream = new FileWriter(fName);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(content);
             out.close();
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
     }
 }
