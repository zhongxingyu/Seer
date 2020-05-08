 package com.twobytes.util;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.twobytes.master.service.DocRunningService;
 import com.twobytes.model.DocRunning;
 
 @Component("docRunningUtil")
 public class DocRunningUtil {
 
 	@Autowired
 	private DocRunningService docRunningService;
 	
 	public String genDoc(String document){
 		if(document.equals("SO")){
 			Calendar now = Calendar.getInstance(new Locale ( "US" ));
 			String docNo = "";
 			Integer nMonth = now.get(Calendar.MONTH) + 1;
 			Integer nYear = now.get(Calendar.YEAR);
 			Integer nDate = now.get(Calendar.DATE);
 			DocRunning docRunning = docRunningService.getDocByYearMonthDate("SO", nYear, nMonth, nDate);
 			if(null == docRunning){
 				// save new docrunning
 				docRunning = new DocRunning();
 				docRunning.setDocument("SO");
 				docRunning.setPrefix("so");
 				docRunning.setMonth(now.get(Calendar.MONTH) + 1);
 				docRunning.setYear(now.get(Calendar.YEAR));
 				docRunning.setDate(now.get(Calendar.DATE));
 				docRunning.setRunningNumber(1);
 				docRunningService.createNewDocRunning(docRunning);
 			}
 			String month = docRunning.getMonth().toString();
 			if(month.length() == 1){
 				month = "0"+month;
 			}
 			String year = docRunning.getYear().toString();
 			year = year.substring(2, 4);
 			String date = docRunning.getDate().toString();
 			if(date.length() == 1){
 				date = "0"+date;
 			}
 //			docNo = docRunning.getPrefix()+"-"+year+month+"-"+docRunning.getRunningNumber();
 			docNo = year+month+date+"-"+docRunning.getRunningNumber();
 			
 			// increse running no
 			
 			docRunning.setRunningNumber(docRunning.getRunningNumber()+1);
 			docRunningService.updateDocRunning(docRunning);
 			
 			return docNo;
 		}else if(document.equals("customer")){
 			Calendar now = Calendar.getInstance(new Locale ( "US" ));
 			Integer nYear = now.get(Calendar.YEAR);
 			String docNo = "";
 			DocRunning docRunning = docRunningService.getDocByYear("customer", nYear);
 			if(null == docRunning){
 				// save new docrunning
 				docRunning = new DocRunning();
 				docRunning.setDocument("customer");
 				docRunning.setYear(now.get(Calendar.YEAR));
 				docRunning.setRunningNumber(1);
 				docRunningService.createNewDocRunning(docRunning);
 			}
 			Integer year = docRunning.getYear();
 			// convert to thai
 			year = year+543;
 			docNo = year.toString().substring(2,4)+StringUtils.leftPad(docRunning.getRunningNumber().toString(),8,"0");
 			
 			docRunning.setRunningNumber(docRunning.getRunningNumber()+1);
 			docRunningService.updateDocRunning(docRunning);
 			
 			return docNo;
 		}else if(document.equals("computer")){
 			Calendar now = Calendar.getInstance(new Locale ( "US" ));
 			Integer nYear = now.get(Calendar.YEAR);
 			Integer nMonth = now.get(Calendar.MONTH) + 1;
 			String docNo = "";
			DocRunning docRunning = docRunningService.getDocByYearMonth("computer", nYear, nMonth);
 			if(null == docRunning){
 				// save new docrunning
 				docRunning = new DocRunning();
 				docRunning.setDocument("computer");
 				docRunning.setPrefix("COM");
 				docRunning.setYear(now.get(Calendar.YEAR));
 				docRunning.setMonth(now.get(Calendar.MONTH) + 1);
 				docRunning.setRunningNumber(1);
 				docRunningService.createNewDocRunning(docRunning);
 			}
 			String month = docRunning.getMonth().toString();
 			if(month.length() == 1){
 				month = "0"+month;
 			}
 			Integer year = docRunning.getYear();
 			// convert to thai
 			year = year+543;
 			docNo = docRunning.getPrefix()+"-"+year.toString().substring(2,4)+month+"-"+docRunning.getRunningNumber();
 			
 			docRunning.setRunningNumber(docRunning.getRunningNumber()+1);
 			docRunningService.updateDocRunning(docRunning);
 			
 			return docNo;
 		}else{
 			return "Not support document";
 		}
 		
 	}
 }
