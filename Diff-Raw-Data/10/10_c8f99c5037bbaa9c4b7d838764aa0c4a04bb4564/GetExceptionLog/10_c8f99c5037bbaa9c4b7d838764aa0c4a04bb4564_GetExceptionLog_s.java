 package helper;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.sql.Timestamp;
 
 public class GetExceptionLog {
 	
 	public void writeLog(String s) {		
 		try {
			FileInputStream fstream = new FileInputStream("/var/lib/tomcat7/webapps/SenecaBBB/WEB-INF/exceptionlog.txt");  
 	        // Get the object of DataInputStream  
 	        DataInputStream in = new DataInputStream(fstream);  
 	        BufferedReader br = new BufferedReader(new InputStreamReader(in));  
 	        String strLine;
 	        StringBuilder sb = new StringBuilder("");
 	        java.util.Date date= new java.util.Date();
 	        s = new Timestamp(date.getTime()) + " : " + s;
 	        //Read File Line By Line  
 	        while ((strLine = br.readLine()) != null) {
 	              sb.append(strLine + "\n");
 	        }  
 	        //Close the input stream  
 	        in.close();       
 	        sb.append(s + "\n");
 	        
 	        Writer output = null;  
	        File file = new File("/var/lib/tomcat7/webapps/SenecaBBB/WEB-INF/exceptionlog.txt");  
 	        output = new BufferedWriter(new FileWriter(file));  
 	        output.write(sb.toString());
 	        output.close();  
 		} catch (Exception e2) {
 	        try {
 				Writer output = null;  
	            File file = new File("/var/lib/tomcat7/webapps/SenecaBBB/WEB-INF/exceptionlog.txt");  
 	            output = new BufferedWriter(new FileWriter(file));  
 				output.write(s);
 				output.close();
 			} catch (Exception e) { // the file could not be accessed
 				System.out.println("Could not access exceptionlog.txt, if developing ignore message");
 	
 			}
 		}
 	}
 }
