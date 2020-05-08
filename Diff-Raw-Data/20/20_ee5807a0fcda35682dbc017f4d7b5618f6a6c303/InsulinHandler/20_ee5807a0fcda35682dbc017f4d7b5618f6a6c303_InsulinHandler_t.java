 package com.umd.app.diabekare;
 
 import java.awt.Toolkit;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.apache.log4j.Logger;
 
 
 public class InsulinHandler {
 	Toolkit toolkit;
 	Timer insulintimer;
 	Timer insulinlevel;
 	Logger log; 
 	private Insulin insulin;
     private DiabeKareBean dkBean = new DiabeKareBean();	  
     int current_insulin = dkBean.getCurrentInsulinLevel();
     
  
    // System.out.print("I'm alive.new current.."+current_battery);
     
 	public InsulinHandler() {
 		// checking if the battery life is more than  100, then set it to 100. 
 	    if(current_insulin > 100) {
 	    	current_insulin = 100;
 	    }	   
 	    // Setting initial battery life
 	    insulin  = new Insulin();
 		toolkit = Toolkit.getDefaultToolkit();
 		insulintimer = new Timer();
 		insulinlevel = new Timer();
 		log =  Logger.getLogger("DiabeKarelogger");  
 		log.debug("Current insulin from constructor "+current_insulin);		
 		insulintimer.scheduleAtFixedRate(new insulinProcess(), 0, 10000);		//scheduling the timer
 		insulinlevel.scheduleAtFixedRate(new insulinLevelProcess(), 0, 10000);
 	}
 	
 	       
 	    class insulinLevelProcess extends TimerTask {
 	    	public void run() {
 	    		current_insulin = dkBean.getCurrentInsulinLevel();
 	    		//setting the given life
 	    		insulin.givenInsulinLife(current_insulin);
 		    	insulin.use(1);
 		    	int new_current_insulin = insulin.insulinLife();
 		    	System.out.println("new_current_insulin "+new_current_insulin);	    	
 		    	
 		    		dkBean.setCurrentInsulinLevel(new_current_insulin);
 		    	
 		        if(new_current_insulin == 20 || new_current_insulin == 10 ){
 		        	toolkit.beep();
 		        }
 		        System.out.println("I'm alive.new current insulin.."+dkBean.getCurrentInsulinLevel());
		        if(dkBean.getBatteryPause() == 0){
		        	insulinlevel.cancel();
		        	insulinlevel.purge();
		        	System.out.println("timer cancelled..please work!");
		        	//shutdown = false;
		        	
		        }
 		        if(new_current_insulin <= 0){
 					System.out.println("current insulin is 0");			    	
 					insulinlevel.cancel();
 					//System.exit(0);		//system exit if the battery is dead!
 					System.out.println("I'm DEAD!! Please refill me!");
 			    }
 		        
 		            
 		            
 		    	}
 	    }	
 	    
 	    class insulinProcess extends TimerTask {
 	    	public void run() {	   
 	    		// get current hour
 	    		SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
 	    		String processedHour = "";
 	    	    Date now = new Date();
 	    	    String strHour = sdfHour.format(now);
 	    	    System.out.println(strHour); 
 	    	   
 	    	    //Process the column name with regards to our db structure
 	    	   int hour = Integer.parseInt(strHour);
 	    	   if(hour < 10){
 	    		   	processedHour = strHour.substring(1,2);
 	    	    	 System.out.println("processedHour = " +processedHour);
 	    	   }else {
 	    		   processedHour = strHour;
 	    	   }
 	    		//get current basal rate
 		    	float basalRate = dkBean.getCurrentBasalRate(processedHour); 
 		    	System.out.println(basalRate);
 		    	if(dkBean.testAirBubble()==0 || dkBean.ecustatus() ==0 ||dkBean.hardwarestatus() ==0||dkBean.clockstatus()==0){
 		    		insulintimer.cancel();
 		    		log.debug("Insulin Injection Cancelled due to CRITICAL condition ");
 		    	}
 		    	
 	    	}
 	    	
 	    }
 	    
 	           /* public static void main(String args[]) {
 	                new InsulinHandler();
 	                System.out.format("Task scheduled.%n");
 	            
 	        }*/
 	    }
 
 		
 
