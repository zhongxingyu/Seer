 package de.team06.psychoapp;
 
 import android.content.Context;
 import android.os.Environment;
 
import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.List;
 
 /**
  * Created by Kevin on 29.06.13. It´s fucking fantastic.
  */
 
 
 public class CSVExporter {
 
     Context context;
     DatabaseModel database = null;
     List<SocialInteraction> interactions = null;
 
     public CSVExporter(Context context) {
         //database = new DatabaseModel(context);
         //interactions = database.getAllSocialInteractions();
         this.context = context;
     }
 
     public boolean exportCSV(List<SocialInteraction> list) {
 
         List <SocialInteraction> interactions = list;
         String csvHeader = "Code;Datum;Alarmzeit;Antwortzeit;Abbruch;Kontakte;Stunden;Minuten \n";
         String csvValues = "";
 
         for (SocialInteraction s : interactions) {
             csvValues = s.getCode() + ";" + s.getAlarmTimeCSV() + ";" + s.getResponseTimeCSV() + ";" + s.isSkippedCSV() + ";" + s.getNumberOfContacts() + ";" + s.getHours() + ";" + s.getMinutes() + "\n";
         }
 
         if (isExternalStorageWritable()) {
 
             try {
             String filename = interactions.get(0).getCode() + ".csv";
             String values = csvHeader + csvValues;
 
            File csv = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
             FileWriter fileWriter = new FileWriter(csv);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(values);
            out.close();
             }
             catch (Exception e) {
                 e.printStackTrace();
                 return false;
             }
             return true;
         }
        return false;
     }
 
     public boolean isExternalStorageWritable() {
         String state = Environment.getExternalStorageState();
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             return true;
         }
         return false;
     }
 
 
 }
