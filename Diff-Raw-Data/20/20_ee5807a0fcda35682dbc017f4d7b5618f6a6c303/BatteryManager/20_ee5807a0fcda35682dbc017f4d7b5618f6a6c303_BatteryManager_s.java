 package com.umd.app.diabekare;
 
 
 import java.awt.Toolkit;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.apache.log4j.Logger;
 
 
 public class BatteryManager {
 	Toolkit toolkit;
 	static Timer timer;
 	Logger log; 
 	private Battery battery;
     private DiabeKareBean dkBean = new DiabeKareBean();	  
     int current_battery = dkBean.getCurrentBatteryStatus();    
  
    
     
     
 	public BatteryManager() {
 		// checking if the battery life is more than  100, then set it to 100. 
 	    if(current_battery > 100) {
 	    	current_battery = 100;
 	    }	   
 	   
 	    battery  = new Battery();
 		toolkit = Toolkit.getDefaultToolkit();
 		timer = new Timer();
 		log =  Logger.getLogger("DiabeKarelogger");  
 		log.debug("current battery from constructor "+current_battery);		
         timer.scheduleAtFixedRate(new batteryProcess(), 0, 10000);
        //scheduling the timer        
         
 	}
 	static boolean shutdown = false;
 	public static void cancelTimer(){
 		timer.cancel();
 		timer.purge();
 		
 		shutdown = true;
 		System.out.println("Cancelled");
 		return;
 		
     }
 	
 	       
 	    class batteryProcess extends TimerTask {
 	    	public void run() {	
 	    		
 	    		current_battery = dkBean.getCurrentBatteryStatus();
 	    		//setting the given life
 	    		battery.givenBatteryLife(current_battery);
 	    		// reducing 1 life of the battery
 		    	battery.use(1);
 		    	int new_current_battery = battery.batteryLife();
 		    	System.out.println("new_current_battery "+new_current_battery);	    	
 		    	
 		    		dkBean.setCurrentBatteryStatus(new_current_battery);
 		    	
 		        if(new_current_battery == 20 || new_current_battery == 10 ){
 		        	log.debug("ALARM: BATTERY IS LOW "+current_battery);	
 		        	toolkit.beep();
 		        }
 		        System.out.println("I'm alive.new current battery.."+dkBean.getCurrentBatteryStatus());
 		        System.out.println("getBatteryPause.."+dkBean.getBatteryPause());
		        if(dkBean.getBatteryPause() == 0){
		        	timer.cancel();
		        	timer.purge();
		        	System.out.println("timer cancelled..please work!");
		        	//shutdown = false;
		        	
		        }
 		        //exit system if battery is dead
 		        if(new_current_battery <= 0){
 					System.out.println("current battery is 0");			    	
 					//timer.cancel();
 					System.out.println("I'm dead!! Please recharge me");
 					System.exit(0);		//system exit if the battery is dead!
 					 
 						
 					}
 					
 			    }
 		        
 		            
 		            
 		    	
 	    	
 	    }			
 	    
 	            /*public static void main(String args[]) {
 	                new Main();
 	                System.out.format("Task scheduled.%n");
 	            
 	        }*/
 	    }
 
 		
 
