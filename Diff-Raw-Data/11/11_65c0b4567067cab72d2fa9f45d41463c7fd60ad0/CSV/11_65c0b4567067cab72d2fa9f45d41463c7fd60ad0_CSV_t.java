 
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 public class CSV {
 	static int idCount = 0;
 	ArrayList<String> rows;
     ArrayList<ArrayList<String>> data;
 
 	CSV(String fileName) {
 	    rows = new ArrayList<String>();
         data = new ArrayList<ArrayList<String>>();
         FileInputStream fstream = null;
         
         //opening input stream
         try {
             fstream = new FileInputStream(fileName);
         } catch (FileNotFoundException e) {
             System.err.println("File Not Found.");
             System.exit(1);
         }
 
         //init streams
         DataInputStream in = new DataInputStream(fstream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String strLine = null;
 
         //reading and parsing file
         try {
             while ((strLine = br.readLine()) != null) {
                 ArrayList<String> tempAL = stringToStringAL(strLine);
                 if (tempAL != null){
                     data.add(tempAL);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.err.println("Exception while reading file.");
         }
         
         for(int i = 0; i < data.size(); i++){
             String temp = data.get(i).get(0);
             boolean newValue = true;
             for(int j = 0; j < rows.size(); j++){
                 if(temp.compareTo(rows.get(j)) == 0){
                     newValue = false;
                 }
             }
             if(newValue){
                 rows.add(temp);
             }
            
            temp = data.get(i).get(2);
            newValue = true;
            for(int j = 0; j < rows.size(); j++){
                if(temp.compareTo(rows.get(j)) == 0){
                    newValue = false;
                }
            }
            if(newValue){
                rows.add(temp);
            }
         }
         
     }
     
     private static ArrayList<String> stringToStringAL(String s){
         s = s.replaceAll("\"", "");
         boolean empyString = true;
         ArrayList<String> answer = new ArrayList<String>();
 
         StringTokenizer st = new StringTokenizer(s,",");
 
         while (st.hasMoreTokens()) {
             try{
             	String temp = st.nextToken();
             	temp = temp.replaceAll(" $", "");
             	temp = temp.replaceAll("^ ", "");
             	
                 answer.add(temp);
             }catch(Exception e){
             	System.err.println("stringToStringAL() parse error!");
             }
         }
         return answer;
     }
     
     
     public String toString(){
     	String answer = "";
     	
     	for(int i = 0; i < data.size(); i++){
     		for(int j = 0; j < data.get(i).size(); j++){
         		answer += data.get(i).get(j) + "\t";
         	}
     		answer += "\n";
     	}
     	
     	return answer;
     }
     
 }
