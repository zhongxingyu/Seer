 package com.tobbentm.higreader;
 
 import android.util.Log;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * Created by Tobias on 26.08.13.
  * Welcome to my halv-assed attempt at an parser
  */
 public class TimeParser {
 
     public static String[][] timetable(String csv, boolean room){
         String csv2 = csv.split("\n", 5)[4];
         CSVReader reader = new CSVReader(new StringReader(csv2), ',', '"');
         String[] line;
         String currentdate = "", startTime, endTime = "0";
         ArrayList<ArrayList<String>> master = new ArrayList<ArrayList<String>>();
         //Log.d("CSV", csv2);
 
         try {
             while((line = reader.readNext()) != null){
                 if(currentdate.length() == 0){
                     endTime = "0800";
                     currentdate = line[0];
                     master.add(dateEntry(line[0]));
                 }
                 if(!line[0].contains(currentdate)){
                     endTime = "0800";
                     master.add(dateEntry(line[0]));
                }
                if(room){
                    startTime = line[1].replace(":", "").replace(" ", "");
                    if( Integer.parseInt(startTime) >= 815 &&
                            Integer.parseInt(startTime) > Integer.parseInt(endTime)+16 &&
                            Integer.parseInt(endTime) < 1600 ){
                        master.add(clearEntry(currentdate, timeString(endTime, startTime)));
                     }
                    endTime = line[3].replace(":", "").replace(" ", "");
                 }
                 ArrayList<String> inner = new ArrayList<String>();
 
                 inner.add(line[0]);
                 inner.add(line[1].replaceAll(" ", "")+"\n-\n"+line[3].replaceAll(" ", ""));
                 inner.add(line[4]);
                 inner.add(line[5]);
                 inner.add(line[6]);
                 master.add(inner);
                 currentdate = line[0];
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return dimensionalPortal(master);
     }
 
     public static String[][] search(String html, String term){
         //Log.d("PARSING", "Starting parser");
         List<String> id = new ArrayList<String>();
         List<String> name = new ArrayList<String>();
         List<String> ids = new ArrayList<String>();
         List<String> names = new ArrayList<String>();
 
         //The reason for splitting at data-id="-1" is that there is no valuable data
         // after it, and it would just cause problems.
         //Still not bug-free though, and should be replaced with a better regex-based
         // solution.
         String[] split = html.split("data-id=\"-1\"");
 
         ids.addAll(Arrays.asList(split[0].split("data-id=\"")));
         ids.remove(0);
         names.addAll(Arrays.asList(split[0].split("data-name=\"")));
         names.remove(0);
 
 
         for (String s : ids){
             id.add(s.split("\"")[0]);
         }
         for (String s : names){
             name.add(s.split("\"")[0]);
         }
 
         String[][] result = new String[id.size()][2];
         Log.d("HIG.SEARCH.ARRAY", name.toString() + id.toString());
 
         //Join the two arrays into one 2D array
         for(int i=0; i < id.size(); i++){
             result[i][0] = id.get(i);
             result[i][1] = name.get(i);
         }
         return result;
     }
 
     private static String[][] dimensionalPortal(ArrayList<ArrayList<String>> arraylist){
         final int size = arraylist.size();
         String[][] sarr = new String[size][];
         for(int i = 0; i < size; i++) {
             ArrayList<String> innerlist = arraylist.get(i);
             final int innerSize = innerlist.size();
             sarr[i] = new String[innerSize];
             for(int j = 0; j < innerSize; j++) {
                 sarr[i][j] = innerlist.get(j);
             }
         }
         return sarr;
     }
 
     private static ArrayList<String> dateEntry(String date){
         // Adding a db entry consisting of date and tag to generate new date entries
         ArrayList<String> arr = new ArrayList<String>();
         arr.add(date);
         arr.add("");
         arr.add("HIGREADER.newDate");
         arr.add("");
         arr.add("");
         arr.add("");
         return arr;
     }
 
     private static ArrayList<String> clearEntry(String date, String time){
         // Adding a db entry consisting of date, time and tag to generate clear entries
         ArrayList<String> arr = new ArrayList<String>();
         arr.add(date);
         arr.add(time);
         arr.add("HIGREADER.clear");
         arr.add("");
         arr.add("");
         arr.add("");
         return arr;
     }
 
     private static String timeString(String start, String end){
         return start.replaceFirst("([0-9]{2})", "$1:") + " - " + end.replaceFirst("([0-9]{2})", "$1:");
     }
 
 }
