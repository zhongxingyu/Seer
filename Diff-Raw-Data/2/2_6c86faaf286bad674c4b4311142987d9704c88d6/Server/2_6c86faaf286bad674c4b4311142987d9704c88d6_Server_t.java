 package com.ben.service;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.xml.ws.Endpoint;
  
 public class Server {
  
     public static void main(String[] args) throws InterruptedException, UnknownHostException {
     	 Server S = new Server();
     	 
     	String IP = InetAddress.getLocalHost().getHostAddress();
     	
    	if (IP.equals("127.0.1.1"))	IP="192.168.0.6";
     	
     	 S.WriteLog("IP Address : "+IP);
     	
         Endpoint.publish("http://"+IP+":5123/web", new TestService());
  
        
        S.WriteLog("Server is up!");
       
  
     }
  
     public void WriteLog(String Message)
     {
     	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		DateFormat dateFormat_log = new SimpleDateFormat("yyyy.MM.dd");
 		Date date = new Date();
 		System.out.println(dateFormat.format(date)+" : "+Message);
  		//System.out.printf("%D %R : ",date + Message);
 	
 		try {
 			 
 		
  
 			File file = new File("/home/pi/logs/dataservice/"+dateFormat_log.format(date)+".dataservice.txt");
 			//File file = new File("c:\\"+dateFormat_log.format(date)+".PiFinance.log.txt");
 			// if file doesnt exists, then create it
 			if (!file.exists()) {
 				file.createNewFile();
 			}
  
 			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
 			BufferedWriter bw = new BufferedWriter(fw);
 			bw.write(dateFormat.format(date)+" : "+Message+"\n");
 			bw.close();
  
 		
  
 		} catch (IOException e) {
 			System.out.println(e.toString());
 		}
     	
     	
     	
     	
     	
     }
     
     
     
 }
