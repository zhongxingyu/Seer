 /*
 Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)
 
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 package eu.cassandra.training.entities;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.jfree.chart.ChartPanel;
 import org.joda.time.DateTime;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import eu.cassandra.training.utils.ChartUtils;
 
 public class Installation
 {
   /**
    * This variable provides the name of the Installation model.
    */
   String name = "";
 
   /**
    * This variable provides the type of the installation Model.
    */
   String type = "";
 
   /**
    * This variable is a list of the appliances that are installed within the
    * Installation.
    */
   ArrayList<Appliance> appliances;
 
   /**
    * This variable presents the equivalent Person Model that is occupying the
    * installation.
    */
   Person person;
 
   /**
    * This variable contains the file name of the measurements file corresponding
    * to this installation as imported by the user.
    */
   String measurementsFile = "";
 
   /**
    * This variable contains the start date of the measurements.
    */
   DateTime startDate;
 
   /**
    * This variable contains the start date of the measurements.
    */
   DateTime endDate;
 
   /**
    * This variable states if the installation measurements contain both active
    * and reactive activeOnly or not.
    */
   boolean activeOnly = true;
 
   /**
    * This is an array of the active power measurements of the installation as
    * provided by the user.
    */
   double[] activePower = null;
 
   /**
    * This is an array of the reactive power measurements of the installation as
    * provided by the user.
    */
   double[] reactivePower = null;
 
   /**
    * This variable provides the id of the Appliance model as sent by the
    * Cassandra Platform.
    */
   String installationID = "";
 
   public Installation ()
   {
     appliances = new ArrayList<Appliance>();
     person = null;
   }
 
   /**
    * The constructor of an Installation Model.
    * 
    * @param filename
    *          The name of the file containing the power measurements of the
    *          installation.
    * @param power
    *          The flag of the type of power measurements available.
    * @throws IOException
    */
   public Installation (String filename, boolean power) throws IOException
   {
     File file = new File(filename);
     name = file.getName().substring(0, file.getName().length() - 4);
     type = "";
     measurementsFile = filename;
     appliances = new ArrayList<Appliance>();
     person = new Person("Person", this);
     this.activeOnly = power;
     parseMeasurementsFile();
   }
 
   /**
    * This function is used for adding a new appliance in the installation.
    * 
    * @param appliance
    *          The appliance in need for addition.
    */
   public void addAppliance (Appliance appliance)
   {
     appliances.add(appliance);
   }
 
   /**
    * This function is searching for an appliance in the installation given a
    * certain name.
    * 
    * @param name
    *          The name of the appliance in search of.
    */
   public Appliance findAppliance (String name)
   {
     Appliance result = null;
 
     for (Appliance appliance: appliances) {
 
       if (appliance.getName().equalsIgnoreCase(name)) {
         result = appliance;
         break;
       }
     }
     return result;
   }
 
   /**
    * This is a getter function of the Installation model name.
    * 
    * @return the name of the Installation model.
    */
   public String getName ()
   {
 
     return name;
   }
 
   /**
    * This is a getter function of the Installation model id.
    * 
    * @return the id of the Installation model.
    */
   public String getInstallationID ()
   {
 
     return installationID;
   }
 
   /**
    * This is a getter function of the start date of the measurements.
    * 
    * @return the start date of the measurements.
    */
   public DateTime getStartDate ()
   {
 
     return startDate;
   }
 
   /**
    * This is a getter function of the start date of the measurements.
    * 
    * @return the start date of the measurements.
    */
   public DateTime getEndDate ()
   {
 
     return endDate;
   }
 
   /**
    * This is a getter function of the Installation model's appliances.
    * 
    * @return a list with the appliances of the Installation model.
    */
   public ArrayList<Appliance> getAppliances ()
   {
     return appliances;
   }
 
   /**
    * This is a getter function of the Installation model person.
    * 
    * @return the person within the Installation model.
    */
   public Person getPerson ()
   {
 
     return person;
   }
 
   /**
    * This is a setter function of the Installation model name.
    * 
    * @param the
    *          id of the Installation model.
    */
   public void setName (String name)
   {
     this.name = name;
   }
 
   /**
    * This is a setter function of the Installation model id.
    * 
    * @param the
    *          id of the Installation model.
    */
   public void setInstallationID (String id)
   {
     installationID = id;
   }
 
   /**
    * This is the parser for the measurement file. It parses through the file and
    * creates the arrays of the active and reactive power consumptions.
    */
   public void parseMeasurementsFile () throws IOException
   {
 
     ArrayList<Double> temp = new ArrayList<Double>();
     ArrayList<Double> temp2 = new ArrayList<Double>();
 
     String extension =
       measurementsFile.substring(measurementsFile.length() - 3,
                                  measurementsFile.length());
 
     switch (extension) {
 
     case "csv":
 
       boolean startFlag = true;
 
       File file = new File(measurementsFile);
       Scanner scanner = new Scanner(file);
 
       int counter = 0;
 
       while (scanner.hasNext()) {
 
         String line = scanner.nextLine();
         // System.out.println(line);
 
         if (startFlag) {
           if (line.split(",")[0].equalsIgnoreCase("1")) {
 
             startDate = new DateTime(2012, 01, 01, 00, 00);
 
           }
           else {
 
             int year = Integer.parseInt(line.split(",")[0].substring(0, 4));
             int month = Integer.parseInt(line.split(",")[0].substring(4, 6));
             int day = Integer.parseInt(line.split(",")[0].substring(6, 8));
            int hour = Integer.parseInt(line.split(",")[0].substring(8, 10));
            int minute = Integer.parseInt(line.split(",")[0].substring(10, 12));
 
             startDate = new DateTime(year, month, day, hour, minute);
 
           }
 
           // System.out.println(startDate.toString());
           startFlag = false;
         }
 
         temp.add(Double.parseDouble(line.split(",")[1]));
 
         if (!activeOnly)
           temp2.add(Double.parseDouble(line.split(",")[2]));
 
         counter++;
       }
 
       endDate = startDate.plusMinutes(counter);
 
       // System.out.println(endDate.toString());
 
       scanner.close();
       break;
 
     case "xls":
 
       HSSFWorkbook workbook =
         new HSSFWorkbook(new FileInputStream(measurementsFile));
 
       // Get the first sheet.
       HSSFSheet sheet = workbook.getSheetAt(0);
       for (int i = 0; i < sheet.getLastRowNum(); i++) {
 
         // Set value of the first cell.
         HSSFRow row = sheet.getRow(i + 1);
         temp.add(row.getCell(1).getNumericCellValue());
         if (!activeOnly)
           temp2.add(row.getCell(2).getNumericCellValue());
       }
 
       break;
 
     }
 
     activePower = new double[temp.size()];
 
     for (int i = 0; i < temp.size(); i++)
       activePower[i] = temp.get(i);
 
     if (!activeOnly) {
       reactivePower = new double[temp2.size()];
       for (int i = 0; i < temp2.size(); i++)
         reactivePower[i] = temp2.get(i);
     }
 
   }
 
   /**
    * This function is utilized to be graphically represented the installation
    * consumption measurements in the Training Module.
    * 
    * @return a chart panel with the installation consumption measurements'
    *         graph.
    */
 
   public ChartPanel measurementsChart () throws IOException
   {
 
     if (activeOnly)
       return ChartUtils.createLineDiagram(name + " Measurements", "Time Step",
                                           "Power", activePower);
     else
 
       return ChartUtils.createLineDiagram(name + " Measurements", "Time Step",
                                           "Power", activePower, reactivePower);
   }
 
   /**
    * This function is used to remove all the appliances from an installation.
    */
   public void clear ()
   {
     appliances.clear();
   }
 
   @Override
   public String toString ()
   {
     return name;
   }
 
   /**
    * Creating a JSON object out of the Installation model.
    * 
    * @return the JSON object created from Installation model.
    */
   public DBObject toJSON (String userID)
   {
 
     DBObject temp = new BasicDBObject();
 
     temp.put("name", name);
     temp.put("type", type);
     temp.put("description", name + " " + type);
     temp.put("scenario_id", userID);
     temp.put("belongsToInstallation", "");
     temp.put("location", "");
     temp.put("x", 0.0);
     temp.put("y", 0.0);
 
     return temp;
 
   }
 
   /**
    * This function is used to present the basic information of the Installation
    * Model on the console.
    */
   public void status ()
   {
     System.out.println("Name: " + name);
     System.out.println("Measurement File: " + measurementsFile);
     System.out.println("Appliances: " + appliances.toString());
     System.out.println("Person: " + person.toString());
     System.out.println("Active Power:" + activePower.toString());
     System.out.println("Reactive Power:" + reactivePower.toString());
   }
 }
