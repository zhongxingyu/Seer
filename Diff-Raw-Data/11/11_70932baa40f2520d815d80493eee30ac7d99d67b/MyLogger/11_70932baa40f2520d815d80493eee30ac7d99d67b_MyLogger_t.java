 
 package com.Lab973.GreenSmartphone;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.Thread;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author hhf
  *
  */
 public abstract class MyLogger extends Thread {
 	public String logFileName;
 	public int logInterval; // ms
 	public boolean stopFlag;
 	public String log="";
 	public static final String TAG="MyLogger";
	public static final int FLUSH_COUNT = 100;
 	public MyLogger(String logFileName, int interval){
 		super();
 		this.logFileName = logFileName;
 		this.logInterval = interval;
 		this.stopFlag =true;
 	}
 	public void startLog()
 	{
 		this.start();
 	}
 	public void stopLog()
 	{
 		this.stopFlag = false;
 	}
 	
 	public abstract String getLogValue();
 	
 	public void run() {
 		try {
 			this.stopFlag = true;
 		    BufferedWriter out = new BufferedWriter(new FileWriter(this.logFileName));
 		    SimpleDateFormat bartDateFormat =  
 			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
		    int count = 0;
 			while(stopFlag){
 				try {
					if(count++ == FLUSH_COUNT)
					{
						out.write(log);
						out.flush();
						log = "";
						count = 0;
					}
 					Thread.sleep(this.logInterval);
 					long timeInMillis = System.currentTimeMillis();
 					Calendar cal = Calendar.getInstance();
 					cal.setTimeInMillis(timeInMillis);
 					Date date = cal.getTime();
 					log+=bartDateFormat.format(date)+"\t"+ this.getLogValue() + "\r\n";
 					
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 			}
 			if(!stopFlag)
 			{
 				out.write(log);
 				out.flush();
 				out.close();
 			}
 		} catch (IOException e) {//open file failed!
 			e.printStackTrace();
 		}
 
 	}
 
 }
