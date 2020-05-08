 package com.memelabs.iqratrack;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import android.content.Context;
 
 public class Tracker {
 	private static String TRACKER_DATA_FILE = "progress.dat"; 
 	private String[] ayasReadCount = new String[6237];//6236 aya of quran + lastline for last sura index
 	
 	public Tracker(Context ctx) {
 		super();
 		try {
 			System.out.println("instantiate tracker");
 			loadProgress(ctx);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 	}
 	private void createTrackerFile(Context ctx){
 		System.out.println("create tracker file");
 		try {
 			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
 					ctx.openFileOutput(TRACKER_DATA_FILE,Context.MODE_PRIVATE)));
 			for (int j = 0; j < 6236; j++){
 				writer.write("0\r\n");
 			}
 			writer.write("1\r\n");//default sura
 			//writer.write("1\r\n");//default aya
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}				
 	}
 	private void loadProgress(Context ctx) throws IOException{
 		 
 		System.out.println("load progress using tracker");
 		File file = ctx.getFileStreamPath(TRACKER_DATA_FILE);
 		if (!file.exists()){
 			createTrackerFile(ctx);
 		}
 				
 		BufferedReader br = new BufferedReader(new InputStreamReader(ctx.openFileInput(TRACKER_DATA_FILE),"UTF-8"));           
 			for (int j = 0; j < 6237; j++){
 				ayasReadCount[j] = br.readLine();
 				if (ayasReadCount[j]==null){
 					ayasReadCount[j]="0";
 				}
 			}
 			if (ayasReadCount[6236]=="0"){
 				ayasReadCount[6236]="1";
 			}
 				
 		br.close();
 	}
 	public String[] getAyasReadCount(){
 		return ayasReadCount;
 		
 	}
 	public void updateTracker(Context ctx) {
 		System.out.println("update tracker data");
 		IqraTrackApp app = (IqraTrackApp) ctx.getApplicationContext();
 		File file = ctx.getFileStreamPath(TRACKER_DATA_FILE);
 		if (!file.exists()){
 			file.delete();
 		}
 		try {
 			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
 					ctx.openFileOutput(TRACKER_DATA_FILE,Context.MODE_PRIVATE)));
 			for (int j = 0; j < 6236; j++){
 				writer.write(ayasReadCount[j]+"\r\n");
 			}
 			writer.write(app.getCurrentSura().getIdx()+"\r\n");
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}	
 	}
 	public int getAyaReadCountBySura(SuraEntity sura){
 		System.out.println("get aya read count for "+sura.getTname());
 		int totcnt=0;
 		int readcount;
 		int i;
 		for (i=0;i <sura.getAyas();i++){
 			readcount = Integer.parseInt(ayasReadCount[i+sura.getStart()]); 
 			if(readcount>0){
 				totcnt++;
 			}
 		}
 		return totcnt;		
 	}
 
 	public void resetTracker(Context ctx) {
 		System.out.println("reset tracker file");
 		File file = ctx.getFileStreamPath(TRACKER_DATA_FILE);
 		if (!file.exists()){
 			file.delete();
 		}
 		createTrackerFile(ctx);
 		try {
 			loadProgress(ctx);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
