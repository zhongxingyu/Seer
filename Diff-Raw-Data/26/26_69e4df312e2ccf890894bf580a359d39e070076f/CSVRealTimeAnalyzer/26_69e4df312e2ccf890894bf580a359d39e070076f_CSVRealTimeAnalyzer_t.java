 package com.lt.loadtest.loadtestagent.domain.bl.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.lt.loadtest.loadtestagent.domain.model.LogAnalysisReport;
 import com.lt.loadtest.loadtestagent.infrastructure.common.file.FileUtil;
 
 public class CSVRealTimeAnalyzer {
 	private static final Log logger = LogFactory.getLog(CSVRealTimeAnalyzer.class);
 
 	public static char getPosChar(File file, long pos){
 		FileInputStream fis = null;
 		FileChannel fc = null;
 		long fileSize = 0;
 		char ch = '1';
 		try {
 			fis = new FileInputStream (file);
 			fc = fis.getChannel();
 			fileSize = fc.size();
 			int BACKOFF_COUNT = 1024;
 			
 			MappedByteBuffer filedata = fc.map (MapMode.READ_ONLY, 0, fileSize);
 
 			byte[] buffer = new byte[BACKOFF_COUNT];
 			int anchor = 0;
 			while(filedata.hasRemaining() && anchor<pos){
 				int len = buffer.length;
 				if(filedata.remaining() < BACKOFF_COUNT){
 					len = filedata.remaining();
 				}
 				filedata.get(buffer, 0, len);
 				for(int i=0;i<len;i++){
 					if(anchor==pos){
 						ch = (char) buffer[i];
 					    break;
 					}
 					anchor++;
 				}
 			}
 			return ch;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return 'a';
 	}
 	
 	public static String getAnalyzableLines(File file, long startPos, long endPos){
         byte[] buffer = new byte[1024];
         FileInputStream fis = null;
 		FileChannel fc = null;
 		long fileSize = 0;
 		long lastEofPos = 0;
 		
 		StringBuilder sb = new StringBuilder();
 		try {
 			fis = new FileInputStream (file);
 			fc = fis.getChannel();
 			fileSize = fc.size();
 			
 			if (startPos<endPos && (endPos<= fileSize)){
 				lastEofPos = getLastEOFPos(file, startPos, endPos-startPos);
 				System.out.println("lastEofPos="+lastEofPos);
 				long size = lastEofPos - startPos;
 				if(size>0){
 					MappedByteBuffer filedata = fc.map (MapMode.READ_ONLY, startPos, size);
 					
 					while(filedata.hasRemaining()){
 						int len = buffer.length;
 						if(filedata.remaining() < 1024){
 							len = filedata.remaining();
 						}
 						filedata.get(buffer, 0, len);
 						String str = new String(buffer);
 						sb.append(str);
 					}
 				}
 			}
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return sb.toString();
 	}
 	public static long getLastEOFPos(File file, long startPos, long size){
 		//compute the last EOF between startPos and startPos+Size
 		long lastEOFPos = 0L;
 		FileInputStream fis = null;
 		FileChannel fc = null;
 		long fileSize = 0;
 		
 		try {
 			fis = new FileInputStream (file);
 			fc = fis.getChannel();
 			fileSize = fc.size();
 			int BACKOFF_COUNT = 1024;
 			long mapStartPos = startPos;
 			long mapSize = size;
 			if(size > BACKOFF_COUNT){
 			    mapStartPos = startPos + size - BACKOFF_COUNT;
 			    mapSize = BACKOFF_COUNT;
 			}
 
 			MappedByteBuffer filedata = fc.map (MapMode.READ_ONLY, mapStartPos, mapSize);
 
 			byte[] buffer = new byte[BACKOFF_COUNT];
 			while(filedata.hasRemaining()){
 				int len = buffer.length;
 				if(filedata.remaining() < BACKOFF_COUNT){
 					len = filedata.remaining();
 				}
 				filedata.get(buffer, 0, len);
 				for(int i=len-1;i>0;i--){
 					char c = (char)buffer[i];
					//System.out.println("==========i="+i+"==ch="+c);
 					if(c == '\n'){
 						lastEOFPos = mapStartPos + i;
 						System.out.println("lastEOFPosTmp="+lastEOFPos);
 						if(lastEOFPos!=(mapStartPos+mapSize-1)){
 							System.out.println("mapStartPos+mapSize="+(mapStartPos+mapSize));
 							System.out.println("==========lastEOFPos="+lastEOFPos+" is EOF");
 						    break;
 						}
 					}
 				}
 			}
 		}catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		return lastEOFPos;
 	}
 	
 	public static void analzyeCSV(){
 		//./jmeter -n -t ~/Blur/cloudpi/118_http_200_30000_20130516102132.jmx
 		String logFilePath = "/tmp/118_http_C200_L30000_20130516102132.csv";
 		//logFilePath = "/Users/marsliutao/Blur/cloudpi/sample1.csv";
 		File file= new File(logFilePath);
 		long startPos = 0;
 		long endPos = 0;
 
 		ExecutorService executorService = Executors.newCachedThreadPool();
 		List<Future<Integer>> resultList = new ArrayList<Future<Integer>>();
 		
 		FileInputStream fis = null;
 		FileChannel fc = null;
 		long fileSize = 0;
 		
 		try {
 			fis = new FileInputStream (file);
 			fc = fis.getChannel();
 			fileSize = fc.size();
 			endPos = fileSize;
 			System.out.println("fileSize="+fileSize);
 			while(true && startPos<endPos){
 				try {
 					//Step1: Get current analayzable lines
 					//1.1 Get lastEofPos
 					//1.2 Get lines from start pos to lastEofPos
 					
 					long lastEOFPos = getLastEOFPos(file, startPos, endPos - startPos);
 					long size = lastEOFPos - startPos;
 					
 					System.out.println(String.format("start=%s,endPos=%s,lastEOFPos=%s,size=%s", startPos, endPos, lastEOFPos, size));
 					
 					String analyzableLines = getAnalyzableLines(file, startPos, lastEOFPos);
 					String strFilePath = "/tmp/" + System.currentTimeMillis()+".txt";
 					try {
 						FileUtil.saveContentToFile(analyzableLines, strFilePath);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 					startPos = lastEOFPos+1;
 					
 					Thread.sleep(10000);
 					
 					fis = new FileInputStream (file);
 					fc = fis.getChannel();
 					fileSize = fc.size();
 					endPos = fileSize;
 					System.out.println(String.format("start=%s,endPos=%s", startPos, endPos));
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 					logger.error("Can not find jmeter log file", e);
 				} catch (IOException e) {
 					e.printStackTrace();
 					logger.error("IO Exception when read jmeter log file", e);
 				} 
 				
 			}
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.println("======================");
 		Integer totalCount = 0;
 		for (Future<Integer> future : resultList) {
 		      try {
 		    	  Integer count = future.get();
 		    	  totalCount+=count;
 		      } catch (InterruptedException e) {
 		        e.printStackTrace();
 		      } catch (ExecutionException e) {
 		        e.printStackTrace();
 		      }
 		}
 		System.out.println("totalCount="+totalCount);
 		executorService.shutdown();
 	}
 	
 	
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		analzyeCSV();
 	}
 }
 
  
 
 class LogAnalyzeThread implements Callable<Integer> {
     
 	private MappedByteBuffer filedata;
 	public LogAnalyzeThread(MappedByteBuffer filedata){
 		this.filedata = filedata;
 	}
 	
 	public Integer call() {
 		// TODO Auto-generated method stub
 		byte[] buffer = new byte[1024];
 		
 		int count = 0;
 		StringBuilder sb = new StringBuilder();
 		while(this.filedata.hasRemaining()){
 			int len = buffer.length;
 			if(filedata.remaining() < 1024){
 				len = filedata.remaining();
 			}
 			this.filedata.get(buffer, 0, len);
 			String str = new String(buffer);
 			sb.append(str);
 		}
 		String strFilePath = "/tmp/" + System.currentTimeMillis()+".txt";
 		try {
 			FileUtil.saveContentToFile(sb.toString(), strFilePath);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("line count="+count);
 		return count;
 	}
 	
 }
 
 class LogAnalyzeCallableTask implements Callable<LogAnalysisReport> {
 	private static final Log logger = LogFactory.getLog(LogAnalyzeCallableTask.class);
 	
     private MappedByteBuffer filedata;
 	public LogAnalyzeCallableTask(MappedByteBuffer filedata){
 		this.filedata = filedata;
 	}
 	
 	public LogAnalysisReport call() {
 		byte[] buffer = new byte[1024];
 		LogAnalysisReport analysisReport = new LogAnalysisReport();
 		
 		SortedMap<String, Integer> tpsReport= new TreeMap<String, Integer>();
 		HashMap<String, Integer> secTotalResponseTimeMap = new HashMap<String, Integer>();
 		SortedMap<String, Integer> responseTimeReport= new TreeMap<String, Integer>(); //average response time of the requests in the sec
 		
 		//for every sec's average response time, we first get every sec's request number and every sec all requests's response time
 		while(this.filedata.hasRemaining()){
 			int len = buffer.length;
 			if(filedata.remaining() < 1024){
 				len = filedata.remaining();
 			}
 			this.filedata.get(buffer, 0, len);
 			String str = new String(buffer);
 			String[] logLines = str.split("\n");
 			if(logLines!=null && logLines.length>0){
 				for(int i=0;i<logLines.length;i++){
 					String logLine = logLines[i];
 					String[] fields = logLine.split(",");
 					if(fields!=null && fields.length==10){
 						String strMSTimePoint = fields[0];
 	            	    String strReqResponseTime = fields[1];
 	            	    if(strMSTimePoint!=null && strMSTimePoint.length()==13){
 		            	    long timePoint = Long.parseLong(strMSTimePoint);
 		            	    long secTimePoint = timePoint/1000;
 		            	    
 		            	    Integer requestNumbersOfTheSec = tpsReport.get(String.valueOf(secTimePoint));
 		            	    if(requestNumbersOfTheSec == null){
 		            	    	tpsReport.put(String.valueOf(secTimePoint), Integer.valueOf(1));
 		            	    }else {
 		            	    	requestNumbersOfTheSec++;
 		            	    	tpsReport.put(String.valueOf(secTimePoint), Integer.valueOf(requestNumbersOfTheSec));
 		            	    }
 		            	    
 		            	    Integer requestResponseTime = secTotalResponseTimeMap.get(String.valueOf(secTimePoint));
 		            	    if(requestResponseTime == null){
 		            	    	secTotalResponseTimeMap.put(String.valueOf(secTimePoint), Integer.parseInt(strReqResponseTime));
 		            	    }else{
 		            	    	requestResponseTime+=Integer.parseInt(strReqResponseTime);
 		            	    	secTotalResponseTimeMap.put(String.valueOf(secTimePoint), requestResponseTime);
 		            	    }
 	            	    }
 					}
 				}
 			}
 			
 			//Compute average response time of a sec
 			for(Entry<String,Integer> tps : tpsReport.entrySet()){
 				String secPoint = tps.getKey();
 				Integer requestNum = tps.getValue();
 				Integer responseTime = secTotalResponseTimeMap.get(secPoint);
 				if(responseTime!=null && responseTime>0){
 					Integer averageResponseTime = responseTime/requestNum;
 					//logger.debug(String.format("secPoint=%s,requestNum=%s,responseTime=%s,averageResponseTime=%s", secPoint, requestNum, responseTime, averageResponseTime));
 					responseTimeReport.put(secPoint, averageResponseTime);
 				}
 			}
 			
 		}
 		
 		analysisReport.setTPSReport(tpsReport);
 		analysisReport.setResponseTimeReport(responseTimeReport);
 
 		return analysisReport;
 	}
 	
 }
