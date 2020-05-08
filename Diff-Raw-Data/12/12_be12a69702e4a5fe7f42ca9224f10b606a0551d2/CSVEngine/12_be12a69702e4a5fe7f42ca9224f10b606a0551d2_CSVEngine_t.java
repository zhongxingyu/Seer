 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package io;
 
 import java.io.*;
 import java.util.*;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import org.joda.time.DateTime;
 
 /**
  *
  * @author jonathanrainer
  */
 public class CSVEngine {
     
     public CSVEngine()
     {
         
     }
     
     public boolean exportToCSVFile(File fileToExport, ArrayList<JLabel> 
             labels, ArrayList<JTextField> textFields)
     {
         try
         {
             FileWriter fw = new FileWriter(fileToExport);
             PrintWriter pw = new PrintWriter(fw);
 
             Iterator it1 = labels.iterator();
             Iterator it2 = textFields.iterator();
             while(it1.hasNext() && it2.hasNext())
             {
                 JLabel labelBuffer = (JLabel) it1.next();
                 JTextField textFieldBuffer = (JTextField) 
                         it2.next();
                 String parameter = labelBuffer.getText();
                 String value = textFieldBuffer.getText();
                 pw.print(parameter);
                 pw.print(",");
                 pw.println(value);
             }
             pw.flush();
             pw.close();
             fw.close();
             return true;
         }
         catch (IOException ioe)
         {
             return false;
         }
     }
     
     public HashMap<String, String> importCSVFile(File file)
     {
         HashMap<String, String> configs = new HashMap<String, String>();
         try
         {
             // Create a buffered reader object to load in each new line of the .csv
             // file 
             BufferedReader bufRdr = new BufferedReader(new FileReader(file));
             String line;
             // Set up the two ArrayLists to hold the values for combination later
             ArrayList<String> config_names = new ArrayList<String>();
             ArrayList<String> config_values = new ArrayList<String>();
             // While there is still data to read read in each new line, using ',' as
             // a seperator. Then from this tokenise the line into a tuple of tokens,
             // the identifier and the value. Add them to their respective ArrayLists
             while((line = bufRdr.readLine()) != null)
             {
                 StringTokenizer st = new StringTokenizer(line, ",");
                 boolean first = true;
                 while(st.hasMoreTokens())
                 {
                     String input = (String) st.nextToken();
                     if(first)
                     {
                         config_names.add(input);
                     }
                    else
                     {
                         config_values.add(input);
                     }
                     first = false;
                 }
             }
             bufRdr.close();
             // After closing the bufferedReader iterate over the two lists in sync 
             // and store each of the pairs of values in the HashMap
             Iterator it1 = config_names.iterator();
             Iterator it2 = config_values.iterator();
             while(it1.hasNext() && it2.hasNext())
             {
                 configs.put((String) it1.next(), (String) it2.next());
             }
             
         }
         catch (Exception e)
         {
             
         }
         return configs;
     }
     
     public HashMap<String, DateTime> importDTFiles(File file)
     {
         // Set up the ArrayLists to later be combined
         ArrayList<String> talks = new ArrayList<String>();
         ArrayList<String> dates = new ArrayList<String>();
         ArrayList<String> times = new ArrayList<String>();
         // Set up HashMap to store the results within.
         HashMap<String, DateTime> talksDAndT = new HashMap<String,DateTime>();
         try
         {
             // Create a buffered reader object to load in each new line of the .csv
             // file 
             BufferedReader bufRdr = new BufferedReader(new FileReader(file));
             String line;
             while((line = bufRdr.readLine()) != null)
             {
                 StringTokenizer st = new StringTokenizer(line, ",");
                 while (st.hasMoreTokens())
                 {
                     boolean first = true;
                     boolean second = false;
                     boolean third = false;
                     if(first)
                     {
                         String fileName = st.nextToken();
                         talks.add(fileName);
                         first = false;
                         second = true;
                         third = false;
                     }
                     if(!first && second)
                     {
                         String date = st.nextToken();
                         dates.add(date);
                         first = false;
                         second = false;
                         third = true;
                     }
                     if(!first && !second && third)
                     {
                         String time = st.nextToken();
                         times.add(time);
                         first = true;
                         second = false;
                         third = false;
                     }
                 }
             }
             
             bufRdr.close();
         }
         catch(Exception e)
         {
             
         }
         Iterator<String> it1 = talks.iterator();
         Iterator<String> it2 = dates.iterator();
         Iterator<String> it3 = times.iterator();
         while(it1.hasNext() && it2.hasNext() && it3.hasNext())
         {
             String fullDate = it2.next();
             int year = Integer.parseInt(fullDate.substring(6));
             int month = Integer.parseInt(fullDate.substring(3,5));
             int day = Integer.parseInt(fullDate.substring(0,2));
             String fullTime = it3.next();
             int hours = Integer.parseInt(fullTime.substring(0, 2));
             int minutes = Integer.parseInt(fullTime.substring(3,5));
             DateTime dateTime = new DateTime(year, month, day, hours, minutes, 0, 0);
             talksDAndT.put(it1.next(), dateTime);
         }
         
         
         return talksDAndT;
     }
 }
