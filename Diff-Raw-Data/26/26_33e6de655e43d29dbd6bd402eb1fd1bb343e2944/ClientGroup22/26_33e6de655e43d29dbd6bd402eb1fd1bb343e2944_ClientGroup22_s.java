 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package clientgroup22;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.Timestamp;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 //chart imports
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartFrame;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.CombinedDomainCategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.ApplicationFrame;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author matthew
  */
 public class ClientGroup22 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws FileNotFoundException, IOException {
 
         Socket s = Connect();
 
         Ping(s);
 
 //        GraphStatData(s);
 
 //        AggregatedData(s);
 
 //        GetDataSumm(s);
 
 //        UploadData(s);
 
         DisplayQueryResults(QueryData(s));
 
 //        QueryLogs(s);
 
 //        ClientGroup22 client = new ClientGroup22();
 
         s.close();
 
     }
 
     public ClientGroup22() {
     }
 
     public static void DisplayMenu() {
         System.out.println("Hello!");
     }
 
     public static void QueryLogs(Socket s) {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
         JSONArray group_ids = new JSONArray();
 
         /*Group ids*/
         System.out.println("Please enter in the groups to query by their group ids separated by a space (Enter to confirm):");
 //        String[] group_idArr = ((new Scanner(System.in)).nextLine()).split(" ");
         String[] group_idArr = {"22"};
         for (String i : group_idArr) {
             if (isInteger(i)) {
                 group_ids.put(Integer.parseInt(i));
             } else {
                 System.out.println(i + " is not an integer, ignoring.");
             }
         }
         params.put("group_ids", group_ids);
 
         /*Time from*/
         System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_from", new Scanner(System.in).nextLine());
         params.put("time_from", "2013-01-01 01:01:01");
 
         /*Time to*/
         System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_to", new Scanner(System.in).nextLine());
         params.put("time_to", "2013-04-09 23:55:01");
 
         /*Limit number of logs*/
         System.out.println("Please enter the limit of the number of logs you want returned:");
 //        params.put("limit", new Scanner(System.in).nextLine());
         params.put("limit", 30);
 
         /*Construct query JSONObject*/
         query.put("method", "query_logs");
         query.put("group_id", 22);
         query.put("params", params);
 
         System.out.println(query.toString());
 
         JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
         System.out.println(reply.toString());
     }
 
     public static void Ping(Socket s) {
         JSONObject query = new JSONObject();
         query.put("method", "ping");
         query.put("group_id", "22");
 
         JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
 
         System.out.println("Reply from Server:" + reply.get("result") + " Time(ms):" + reply.getInt("elapsed"));
     }
 
     public static void UploadData(Socket s) throws FileNotFoundException, IOException {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
 
         ArrayList<String> dataArr = ReadFromFile("MoteDump1.txt");
         JSONObject reading;
         JSONArray readings = new JSONArray();
         String type = "";
         String value = "";
         long time = 0;
         for (int i = 0; i < dataArr.size(); i++) {
             reading = new JSONObject();
 
             type = dataArr.get(i).split(" ")[0];
             value = dataArr.get(i).split(" ")[1];
             time = Long.parseLong(dataArr.get(i).split(" ")[2]);

            reading.put("type", type);
            reading.put("value", value);
            reading.put("time", time);

            readings.put(reading);
         }
 
         params.put("readings", readings);
 
         query.put("group_id", "22");
         query.put("params", params);
         query.put("method", "new_readings");
 
 //        System.out.println(finalSend.toString());
 
         String reply = GetReplyFromServer(query, s);
 
         System.out.println("Reply from Server: " + reply);
     }
 
     public static JSONObject QueryData(Socket s) {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
         JSONObject returned = new JSONObject();
 //        JSONObject time_from = new JSONObject();  //Probably don't need...
 //        JSONObject time_to = new JSONObject();
         JSONArray group_ids = new JSONArray();
         JSONArray types = new JSONArray();
 
         /*Group ids*/
         System.out.println("Please enter in the groups to query by their group ids separated by a space (Enter to confirm):");
 //        String[] group_idArr = ((new Scanner(System.in)).nextLine()).split(" ");
         String[] group_idArr = {"22"};
         for (String i : group_idArr) {
             if (isInteger(i)) {
                 group_ids.put(Integer.parseInt(i));
             } else {
                 System.out.println(i + " is not an integer, ignoring.");
             }
         }
         params.put("group_ids", group_ids);
 
         /*Time from*/
         System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_from", new Scanner(System.in).nextLine());
         params.put("time_from", "2012-01-01 01:01:01");
 
         /*Time to*/
         System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_to", new Scanner(System.in).nextLine());
         params.put("time_to", "2013-04-07 19:42:09.0");
 
         /*Types*/
         System.out.println("Please enter which types of readings you want to query, separated by a space (\"light\", \"temperature\", \"humidity\"):");
 //        String[] typesArr = ((new Scanner(System.in)).nextLine()).split(" ");
         String[] typesArr = {"light"};
         for (String i : typesArr) {
             types.put(i);
         }
         params.put("types", types);
 
         query.put("method", "query_readings");
         query.put("group_id", 22);
         query.put("params", params);
 
         JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
 
         return reply;
     }
 
     public static JSONObject AggregatedData(Socket s, String aggrigation, String readingType) {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
         JSONObject returned = new JSONObject();
 
         /*Group id if given*/
         //System.out.println("Please enter in the group to query by their group ID. Enter 0 for all data. (Enter to confirm):");
         //Scanner scan = new Scanner(System.in);
         //groupNo = scan.nextLine();
 //        if (isInteger(groupNo)) {
 //            if (Integer.parseInt(groupNo) == 0) {
 //                //do nothing
 //            } else {
 //                params.put("group_id", Integer.parseInt(groupNo));
         //params.put("group_id", Integer.parseInt(groupNo)); //hardcoded version
 //            }
 //        } else {
 //            System.out.println("Not an integer, assuming 0");
 //        }
 
         //test
         //params.put("aggregation", "mean");
 //        System.out.println("mean put");
         //params.put("type", "temperature");
 //        System.out.println("temp put");
 
         /*Aggregation*/
         //System.out.println("Please enter which types of statistic you want to query (\"light\", \"temperature\", \"humidity\"):");
         //readingType = new Scanner(System.in).nextLine();
         params.put("type", readingType);
 
         /*Types*/
         //System.out.println("Please enter which types of reading you want to query(\"count\", \"average\", \"min\", \"max\", \"stddev\", \"mode\", \"median\"):");
         //aggrigator = new Scanner(System.in).nextLine();
         params.put("aggregation", aggrigation);
 
         /*Time from*/
         //System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_from", new Scanner(System.in).nextLine());
         //params.put("time_from", "2013-01-01 01:01:01");
 //
         /*Time to*/
         //System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_to", new Scanner(System.in).nextLine());
         //params.put("time_to", "2013-04-10 23:55:01");
 
         query.put("method", "aggregate");
         query.put("group_id", 22);
         query.put("params", params);
 
         returned = new JSONObject(GetReplyFromServer(query, s));
 
         return returned;
     }
 
     public static void GetDataSumm(Socket s) {
         JSONObject query = new JSONObject();
         query.put("method", "data_summary");
         query.put("group_id", "22");
 
         JSONObject returned = new JSONObject(GetReplyFromServer(query, s));
 
 
         JSONArray dataArr = returned.getJSONArray("result"); //array of JSONobjects
 
         for (int i = 0; i < dataArr.length(); i++) {
             if (i == 0) {
                 System.out.println("============");
             }
             JSONObject temp = new JSONObject();
             temp = dataArr.getJSONObject(i);
             System.out.println("Type: " + temp.get("type") + "\nType ID: " + temp.get("type_id") + "\nMean: " + temp.get("mean")
                     + "\nMin: " + temp.get("min") + "\nMax: " + temp.get("max") + "\nStandard dev: " + temp.get("stddev") + "\n");
             System.out.println("============");
 
         }
     }
 
     public static ArrayList<String> ReadFromFile(String fileName) throws FileNotFoundException {    //Works! Don't change!
         ArrayList<String> temp = new ArrayList<String>();
         File file = new File(fileName);
         Scanner sc = new Scanner(file);
 
         while (sc.hasNext()) {
             String reading = "";
             reading += sc.next() + " ";
             sc.next();
             reading += sc.next() + " ";
             reading += sc.next();
             temp.add(reading);
         }
         return temp;
     }
 
     public static Socket Connect() {
         Socket sock = new Socket();
         try {
             sock = new Socket("197.85.191.195", 3000);
         } catch (IOException ex) {
             System.out.println(ex);
         }
         return sock;
     }
 
     public static String GetReplyFromServer(JSONObject query, Socket s) {
         String reply = "";
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 
             System.out.println("Querying server...");
             out.write(query.toString());
             out.println();
 
             reply = in.readLine();
         } catch (IOException e) {
             System.out.println(e);
         }
         return reply;
     }
 
     public static boolean isInteger(String s) {
         try {
             Integer.parseInt(s);
         } catch (NumberFormatException e) {
             return false;
         }
         // only got here if we didn't return false
         return true;
     }
 
     public static void DisplayQueryResults(JSONObject j) {  //TODO format the information stored in the JSONObject returned by the server
         //System.out.println(j.toString()); //{"time":"2013-04-07 19:42:09.0","group_id":22,"value":28,"type":"Light"}
         JSONArray results = (JSONArray) j.get("result");
 
         JSONArray light = new JSONArray();
         JSONArray temperature = new JSONArray();
         JSONArray humidity = new JSONArray();
 
         JSONObject temp = new JSONObject();
 
         for (int i = 0; i < results.length(); i++) {
             temp = results.getJSONObject(i);
 // System.out.println(temp.toString());
 // System.out.println(temp.get("type"));
             if (temp.get("type").equals("light")) {
                 light.put(temp);
             } else if (temp.get("type").equals("temperature")) {
                 temperature.put(temp);
             } else if (temp.get("type").equals("humidity")) {
                 humidity.put(temp);
             } else {
             }
         }
 
         System.out.println(light.toString());
         System.out.println(temperature.toString());
 
         /*Creating the query graph*/
 
         int lightValue;
 
         XYSeriesCollection dataset = new XYSeriesCollection();
         XYSeries data = new XYSeries("Data");
 
         for (int i = 0; i < light.length(); i++) {
             lightValue = (int) light.getJSONObject(i).get("value");
             //time = (String)light.getJSONObject(i).get("time");
             data.add(lightValue, (i + 1));
         }
 
         dataset.addSeries(data);
 
         JFreeChart chart = ChartFactory.createScatterPlot(
                 "Query results", // chart title
                 "Time", // x axis label
                 "Frequency", // y axis label
                 dataset, // data
                 PlotOrientation.VERTICAL,
                 true, // include legend
                 true, // tooltips
                 false // urls
                 );
                 XYPlot plot = (XYPlot) chart.getPlot();
         XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
         renderer.setSeriesLinesVisible(0, true);
         plot.setRenderer(renderer);
         
         ChartPanel chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
         ApplicationFrame frame = new ApplicationFrame("Title");
         frame.setContentPane(chartPanel);
         frame.pack();
         frame.setVisible(true);
 
     }
 
     public static void GraphStatData(Socket s) {
 
         /*Chart display*/
         //System.out.println(AggregatedData(s, "mean", "light").toString());
         double lightMean = (double) AggregatedData(s, "mean", "light").get("result");
         int lightMin = (int) AggregatedData(s, "min", "light").get("result");
         int lightMax = (int) AggregatedData(s, "max", "light").get("result");
         double lightStddev = (double) AggregatedData(s, "stddev", "light").get("result");
 
         double temperatureMean = (double) AggregatedData(s, "mean", "temperature").get("result");
         double temperatureMin = (double) AggregatedData(s, "min", "temperature").get("result");
         double temperatureMax = (double) AggregatedData(s, "max", "temperature").get("result");
         double temperatureStddev = (double) AggregatedData(s, "stddev", "temperature").get("result");
 
         double humidityMean = (double) AggregatedData(s, "mean", "humidity").get("result");
         int humidityMin = (int) AggregatedData(s, "min", "humidity").get("result");
         double humidityMax = (double) AggregatedData(s, "max", "humidity").get("result");
         double humidityStddev = (double) AggregatedData(s, "stddev", "humidity").get("result");
 
         //DisplayQueryResults(QueryData(s));
 
         /*Light Chart*/
         DefaultCategoryDataset lightds = new DefaultCategoryDataset();
         lightds.addValue(lightMean, "Mean", "");
         lightds.addValue(lightMin, "Min", "");
         lightds.addValue(lightMax, "Max", "");
         lightds.addValue(lightStddev, "Standard Deviation", "");
 
         JFreeChart lightbc = ChartFactory.createBarChart("Light Statistics", "Key", "Value", lightds, PlotOrientation.VERTICAL, true, false, false);
 
         CategoryPlot lightmainPlot = lightbc.getCategoryPlot();
 
         NumberAxis lightmainAxis = (NumberAxis) lightmainPlot.getRangeAxis();;
         lightmainAxis.setLowerBound(0);
         lightmainAxis.setUpperBound(500);
 
         ChartFrame lightcf = new ChartFrame("Data", lightbc);
         lightcf.setSize(800, 600);
         lightcf.setVisible(true);
 
         /*temperature Chart*/
         DefaultCategoryDataset tempds = new DefaultCategoryDataset();
         tempds.addValue(temperatureMean, "Mean", "");
         tempds.addValue(temperatureMin, "Min", "");
         tempds.addValue(temperatureMax, "Max", "");
         tempds.addValue(temperatureStddev, "Standard Deviation", "");
 
         JFreeChart tempbc = ChartFactory.createBarChart("Temperature Statistics", "Key", "Value", tempds, PlotOrientation.VERTICAL, true, false, false);
 
         CategoryPlot tempmainPlot = tempbc.getCategoryPlot();
 
         NumberAxis tempmainAxis = (NumberAxis) tempmainPlot.getRangeAxis();;
         tempmainAxis.setLowerBound(0);
         tempmainAxis.setUpperBound(50);
 
         ChartFrame tempcf = new ChartFrame("Data", tempbc);
         tempcf.setSize(800, 600);
         tempcf.setVisible(true);
 
         /*humidity Chart*/
         DefaultCategoryDataset humidityds = new DefaultCategoryDataset();
         humidityds.addValue(humidityMean, "Mean", "");
         humidityds.addValue(humidityMin, "Min", "");
         humidityds.addValue(humidityMax, "Max", "");
         humidityds.addValue(humidityStddev, "Standard Deviation", "");
 
         JFreeChart humiditybc = ChartFactory.createBarChart("Humidity Statistics", "Key", "Value", humidityds, PlotOrientation.VERTICAL, true, false, false);
 
         CategoryPlot humiditymainPlot = humiditybc.getCategoryPlot();
 
         NumberAxis humiditymainAxis = (NumberAxis) humiditymainPlot.getRangeAxis();;
         humiditymainAxis.setLowerBound(0);
         humiditymainAxis.setUpperBound(100);
 
         ChartFrame humiditycf = new ChartFrame("Data", humiditybc);
         humiditycf.setSize(800, 600);
         humiditycf.setVisible(true);
     }
 }
