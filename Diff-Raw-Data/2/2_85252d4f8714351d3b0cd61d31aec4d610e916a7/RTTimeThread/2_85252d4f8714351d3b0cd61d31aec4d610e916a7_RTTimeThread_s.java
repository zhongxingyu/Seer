 package com.bukkit.YurijWare.RealTime;
 
 import java.util.Calendar;
 import java.util.logging.Logger;
 
 /**
  * RealTime for Bukkit
  * Class for the time thread
  * 
  * @author Yurij
  */
 public class RTTimeThread implements Runnable {
 	private final Logger log = Logger.getLogger("Minecraft");
 	private boolean active = true;
 	private Thread loop;
 	private Calendar cal;
 	
 	public RTTimeThread(String threadName) {
 		try{
 			loop = new Thread(this, threadName);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public void start(){
 		loop.start();
 	}
 	
 	@Override
 	public void run() {
 		log.info("[" + RealTime.pdfFile.getName() + "] "
 				+ "Started realistic time");
 		while(active){
 			SetTime();
 			try {
				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 		}
 		log.info("[" + RealTime.pdfFile.getName() + "] "
 				+ "Stopped realistic time");
 	}
 	
 	private void SetTime(){
 		cal = Calendar.getInstance();
 		int hour = cal.get(Calendar.HOUR_OF_DAY);
 		long time = (hour*1000-8000+24000)%24000;
 		RealTime.world.setTime(time);
 	}
 	
 	protected void stop(){
 		active = false;
 	}
 	
 	protected boolean isAlive(){
 		return loop.isAlive();
 	}
 	
 }
