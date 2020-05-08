 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package clientgroup22;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import Read_Serial.Junix;
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
 
 //        GetDataSumm(s);
 
 //        UploadData(s);
 
         QueryData(s);
 
 //        ClientGroup22 client = new ClientGroup22();
 
     }
 
     public ClientGroup22() {
     }
 
     public static void DisplayMenu() {
         System.out.println("Hello!");
     }
 
     public static void Ping(Socket s) {
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 
             JSONObject j = new JSONObject();
             j.put("method", "ping");
             j.put("group_id", "22");
 
             out.write(j.toString());
             out.println();
 
             JSONObject returned = new JSONObject(in.readLine()); //Object to store returning string from server
 
             System.out.println("Reply from Server:" + returned.get("result") + " Time(ms):" + returned.getInt("elapsed"));
         } catch (IOException e) {
             System.out.println(e);
         }
     }
 
     public static void UploadData(Socket s) throws FileNotFoundException, IOException {
         ArrayList<String> dataArr = ReadFromFile("MoteDump.txt");
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
 
         JSONObject params = new JSONObject();
 
         params.put("readings", readings);
 
         JSONObject finalSend = new JSONObject();
 
         finalSend.put("group_id", "22");
         finalSend.put("params", params);
         finalSend.put("method", "new_readings");
 
 //        System.out.println(finalSend.toString());
 
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 
             out.write(finalSend.toString());
             out.println();
 
 //            JSONObject returned = new JSONObject(in.readLine()); //Object to store returning string from server
 
 //            System.out.println("Reply from Server:" + returned.getString("result"));
             System.out.println("Reply from Server:" + in.readLine());
 
         } catch (IOException e) {
             System.out.println(e);
         }
     }
 
     public static void QueryData(Socket s) {
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
         params.put("time_from", "2013-01-01 01:01:01");
        
         /*Time to*/
         System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_to", new Scanner(System.in).nextLine());
         params.put("time_to", "2013-04-09 23:55:01");
 
         /*Types*/
         System.out.println("Please enter which types of readings you want to query, separated by a space (\"light\", \"temperature\", \"humidity\")");
 //        String[] typesArr = ((new Scanner(System.in)).nextLine()).split(" ");
         String[] typesArr = {"light", "temperature"};
         for (String i : typesArr) {
             types.put(i);
         }
         params.put("types", types);
 
         query.put("method", "query_readings");
         query.put("group_id", 22);
         query.put("params", params);
 
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 
             System.out.println("Querying server...");
             out.write(query.toString());
             out.println();
            
             System.out.println("Retrieving server reply...");
            returned = new JSONObject(in.readLine());
            System.out.println("Server reply received.");
         } catch (IOException e) {
             System.out.println(e);
         }
        
        DisplayQueryResults(returned);
     }
    
     public static void GetDataSumm(Socket s) {
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 
             JSONObject j = new JSONObject();
             j.put("method", "data_summary");
             j.put("group_id", "22");
 
             out.write(j.toString());
             out.println();
 
             JSONObject returned = new JSONObject(in.readLine()); //Object to store returning string from server
 
             JSONArray dataArr = returned.getJSONArray("result"); //array of JSONobjects
 
             System.out.println("============");
 
             for (int i = 0; i < dataArr.length(); i++) {
                 JSONObject temp = new JSONObject();
                 temp = dataArr.getJSONObject(i);
                 System.out.println("Type: " + temp.get("type") + "\nType ID: " + temp.get("type_id") + "\nMean: " + temp.get("mean")
                         + "\nMin: " + temp.get("min") + "\nMax: " + temp.get("max") + "\nStandard dev: " + temp.get("stddev") + "\n");
                 System.out.println("============");
 
             }
 
             //System.out.println("Reply from Server:" + returned.get("result") + " Time(ms):" + returned.getInt("elapsed"));
 
         } catch (IOException e) {
             System.out.println(e);
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
 //            System.out.println(sc.next() + "FIRST");
 //            sc.next();
 //            System.out.println(sc.next() + "SECOND");
 //            System.out.println(sc.next() + "THIRD");
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
 
     public static boolean isInteger(String s) {
         try {
             Integer.parseInt(s);
         } catch (NumberFormatException e) {
             return false;
         }
         // only got here if we didn't return false
         return true;
     }
    
     public static void DisplayQueryResults(JSONObject j) {
         System.out.println(j.toString());
     }

    
 }
