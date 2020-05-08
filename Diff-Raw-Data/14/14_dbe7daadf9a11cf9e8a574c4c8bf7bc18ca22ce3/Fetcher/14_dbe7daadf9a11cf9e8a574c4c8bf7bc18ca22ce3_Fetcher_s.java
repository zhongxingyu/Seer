 package com.orange.groupbuy.fetcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.mongodb.DBObject;
 import com.orange.common.mongodb.MongoDBClient;
 import com.orange.common.processor.CommonProcessor;
 import com.orange.common.utils.FileUtils;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.manager.FetchTaskManager;
 
 public class Fetcher extends CommonProcessor {
 
 	public static MongoDBClient mongoClient = new MongoDBClient(DBConstants.D_GROUPBUY);
 	
 	@Override
 	public MongoDBClient getMongoDBClient() {
 		return mongoClient;
 	}
 	
 	private void putReadTaskRequest(DBObject task){
 		FetchGroupBuyDataRequest request = new FetchGroupBuyDataRequest();
 		request.setTask(task);
 		putRequest(request);
 	}
 
 	// read task from DB
 	static class ReadTaskTimerTask extends java.util.TimerTask{
 		
 		Fetcher dataFetcher;
 		
 		public ReadTaskTimerTask(Fetcher dataFetcher){
 			this.dataFetcher = dataFetcher;
 		}
 		
         @Override
         public void run() {
 
         	// fetch active task and put into queue
         	try {
 	    		DBObject task = FetchTaskManager.obtainOneTask(dataFetcher.getMongoDBClient());
 	    		if (task == null)
 	    			return;
 	    		
 	    		dataFetcher.putReadTaskRequest(task);
         	}
         	catch (Exception e){
         		log.error("<ReadTaskTimerTask> but catch exception " + e.toString(), e);
         	}
         }
     }
 	
 	static class ActivateAllTaskTimer extends TimerTask {
 
 		public static final int TIMER_INTERVAL = 1000 * 60 * 120;		// 120 minutes
 		
 		Fetcher dataFetcher;
 		
 		public ActivateAllTaskTimer(Fetcher dataFetcher){
 			this.dataFetcher = dataFetcher;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				log.info("<ActivateAllTaskTimer> running now");			
 				FetchTaskManager.activateAllFinishTask(dataFetcher.getMongoDBClient());
 			}
 			catch (Exception e) {
 				log.error("<ActivateAllTaskTimer> catch exception ", e);
 			}
 		}
 		
 	}
 	
 	// reset and activate all task in DB 
 	static class ResetTaskTimer extends java.util.TimerTask{
 						
 		Fetcher dataFetcher;
 		
 		public ResetTaskTimer(Fetcher dataFetcher){
 			this.dataFetcher = dataFetcher;
 		}
 		
         @Override
         public void run() {
         	
         	try {
 	        	log.info("<ResetTaskTimer> timer fired");
 	
 	        	// fetch active task and put into queue
 	    		FetchTaskManager.resetAllTask(dataFetcher.getMongoDBClient());
 	    		
 	    		// set next timer
 				Timer newResetTaskTimer = new Timer();
 				newResetTaskTimer.schedule(new ResetTaskTimer(dataFetcher), ResetTaskTimer.getTaskDate());
 				
 				deleteOldData(new File(FetchGroupBuyDataRequest.DEFAULT_FILE_PATH));
         	}
         	catch (Exception e){
         		log.error("<ResetTaskTimer> but catch exception ", e);
         	}
         }
         
         static public Date getTaskDate(){
         	
         	int scheduleHour = 3;		// 3 AM of the day
         	
     		TimeZone timeZone = TimeZone.getTimeZone("GMT+0800");
     		Calendar now = Calendar.getInstance(timeZone);
     		now.setTime(new Date());
     		
     		if (now.get(Calendar.HOUR_OF_DAY) >= scheduleHour){
     			now.add(Calendar.DAY_OF_MONTH, 1);
     		}
     		
     		now.set(Calendar.HOUR_OF_DAY, scheduleHour);
     		now.set(Calendar.MINUTE, 0);
     		now.set(Calendar.SECOND, 0);
     		now.set(Calendar.MILLISECOND, 0);    			
     		
         	log.info("<ResetTaskTimer> next timer set to "+now.getTime().toString());
     		
     		return now.getTime();
         }
     }
 	
 	   public static void deleteOldData(File file) throws IOException{
 	        
 	        if (file.exists() && file.isDirectory()) {
 	            for (File subf : file.listFiles()) {
 	                if (subf.isDirectory()) {
 	                    checkDateAndDelete(new File(subf.getAbsolutePath()));
 	                }
 	            }
 	        }
 	    }
 	    
 		private static final int SAVE_DAYS = 3;	// 3 days
 		
 	    public static void checkDateAndDelete(File file) throws IOException{
 	        Calendar calnow = Calendar.getInstance();
 	        Calendar cal=Calendar.getInstance();
 	        
 	        Date datenow = new Date();
 	        calnow.setTime(datenow);
 	        
 	        Date date_modify = new Date(file.lastModified());
 	        cal.setTime(date_modify);
 	        
 	        int dec = calnow.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR);
 	        if (dec > SAVE_DAYS) {
 	        	log.info("Clean directory " + file.getAbsolutePath());
 	            FileUtils.delFileOrDir(file);
 	        }
 	    }
 	
 	public static void main(String[] args){		
 						
 		final int MAX_THREAD_NUM = 5;
 		final int READ_TASK_INTERVAL = 1000;		// 1 second
 		
 		boolean reset = false;
 		String resetString = System.getProperty("reset");
 		if (resetString != null)
 			reset = true;
 		
 		for (int i=0; i<MAX_THREAD_NUM; i++){
 			
 			Fetcher dataFetcher = new Fetcher();
 			Thread thread = new Thread(dataFetcher);
 			thread.start();
 			
 			// use the first dataFetcher for timer initlization
 			if (i == 0){
 				if (reset){
 					log.info("<ResetTaskTimer> immediately");
 					FetchTaskManager.resetAllTask(dataFetcher.getMongoDBClient());
 				}
 				
 				Timer readTaskTimer = new Timer();
 				readTaskTimer.schedule(new ReadTaskTimerTask(dataFetcher), READ_TASK_INTERVAL, READ_TASK_INTERVAL);
 
 				Timer resetTaskTimer = new Timer();
 				resetTaskTimer.schedule(new ResetTaskTimer(dataFetcher), ResetTaskTimer.getTaskDate());
 				
				Timer hourlyTimer = new Timer();
				hourlyTimer.schedule(new ActivateAllTaskTimer(dataFetcher),   //0, 10000);
						ActivateAllTaskTimer.TIMER_INTERVAL, ActivateAllTaskTimer.TIMER_INTERVAL);
 				
 			}
 		}
 		
 	}
 
 }
