 package com.olo.reporter;
 
 import static com.olo.util.PropertyReader.configProp;
 
 import java.io.File;
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.testng.ISuite;
 import org.testng.ITestContext;
 import org.testng.ITestResult;
 import org.testng.internal.Utils;
 
 import com.olo.util.Commons;
 import com.olo.util.VerificationErrors;
 
 public class Utility {
 	
 	public static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss, z");
 	public static final SimpleDateFormat sdfTests = new SimpleDateFormat("HH:mm:ss");
 	public static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss.SSS");
 	public static final NumberFormat formatter = new DecimalFormat("#,##0.0");
 	
 	public static final LinkedHashMap<String, String> testCaseReportColumns = new LinkedHashMap<String, String>(){
 		private static final long serialVersionUID = 1L;
 
 			{
 				put("action", "Action");
 		        put("propertyName", "Web Element");
 		        put("propertyValue", "Element Locator");
 		        put("value", "Value");
 		        put("actualValue", "Actual Value");
 		        put("options", "Options");
 		        put("startTime", "Start Time");
 		        put("endTime", "End Time");
 		        put("timeTaken", "Time Taken");
 		        put("status", "Status");
 		        put("exception", "Exception");
 		        put("screenShot", "ScreenShot");
 
 			}
 	};
 	
 	public static String getStatusString(int testResultStatus) {
 		switch (testResultStatus) {
 			case ITestResult.SUCCESS:
 				return "PASS";
 			case ITestResult.FAILURE:
 				return "FAIL";
 			case ITestResult.SKIP:
 				return "SKIP";
 			case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
 				return "SUCCESS_PERCENTAGE_FAILURE";
 		}
 		return null;
 	}
 	
 	public static Comparator<ISuite> suiteStartComp = new Comparator<ISuite>() {
 		public int compare(ISuite o1, ISuite o2) {
 			if (Long.valueOf(o1.getAttribute("suiteStartTime_sort").toString()) > Long.valueOf(o2.getAttribute("suiteStartTime_sort").toString()))
 				return 1;
 			else if (Long.valueOf(o1.getAttribute("suiteStartTime_sort").toString()) < Long.valueOf(o2.getAttribute("suiteStartTime_sort").toString()))
 				return -1;
 			else	
 				return 0;
 		}
 	};
 	
 	public static String timeTaken(long timeDifference) {
 
 		String timeTaken = "";
 		if (timeDifference < 1000) {
 			timeTaken = timeDifference + " ms";
 		} else if (timeDifference < 60000) {
 			timeTaken = formatter.format((double) timeDifference / 1000)
 					+ " sec";
 		} else if (timeDifference < 3600000) {
 			timeTaken = timeDifference
 					/ 60000
 					+ " mins "
 					+ formatter
 							.format((double) (timeDifference % 60000) / 1000)
 					+ " sec";
 		} else {
 			timeTaken = (timeDifference / 3600000)
 					+ " hrs "
 					+ ((timeDifference % 3600000) / 60000)
 					+ " mins "
 					+ formatter
 							.format((double) (timeDifference % 60000) / 1000)
 					+ " sec";
 		}
 		return timeTaken;
 	}
 	
 	public static String getBootstrapCss(){
 		return "<link href='http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.min.css' rel='stylesheet'>";
 	}
 	
 	public static String getBootstrapJs(){
 		return "<script src='http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js'></script>";
 	}
 	
 	public static String getJqueryJs(){
 		return "<script src='http://code.jquery.com/jquery-1.10.1.min.js'></script>";
 	}
 	
 	public static String getInlineCss(){
 		return "<style type='text/css'>.ifskipped{background-color: #d6e1c9;} th.success,td.success{background-color: #dff0d8;} th.error,td.error{background-color: #f2dede;} th.warning,td.warning{background-color: #fcf8e3;}</style>";
 	}
 	
 	public static String getMailCss(){
 		return "<style type='text/css'>table{border:1px solid #808080;border-collapse:collapse;} table td{border:1px solid #808080} table th{border:1px solid #808080;background-color:#C0C0C0;} tr.success{background-color: #dff0d8;} tr.error{background-color: #f2dede;} tr.warning{background-color: #fcf8e3;}</style>";
 	}
 	
 	public static String getGoogleChartsJs(){
 		return "<script type='text/javascript' src='http://www.google.com/jsapi'></script>";
 	}
 	
 	public static String getGooglePieChart(){
 		return "<script type='text/javascript'>google.load('visualization', '1', {packages: ['corechart']}); </script>";
 	}
 	
 	public static String getMetaInfo(){
 		return "<meta name='viewport' content='width=device-width, initial-scale=1.0'>";
 	}
 	
 	public static String getDescriptionTooltipJs(){
 		return "<script type='text/javascript'>$(document).ready(function() { $('.testNameToolTip').tooltip({html: true}); });</script>";
 	}
 	
 	public static StringBuffer getHtmlToHead(){
 		return new StringBuffer().append("<!DOCTYPE html><html><head>");
 	}
 	
 	public static StringBuffer suitesSummaryHead(String title,int totalPassedTests,int totalFailedTests, int totalSkippedTests){
 		StringBuffer headPart = new StringBuffer();
 		headPart.append("<meta charset='utf-8'><title>" + title + "</title><meta name='viewport' content='width=device-width, initial-scale=1.0'>"+Utility.getBootstrapCss());
 		headPart.append(getGoogleChartsJs());
 		headPart.append(getGooglePieChart());
 		headPart.append("<script type='text/javascript'>function drawVisualization() { var data = new google.visualization.DataTable(); data.addColumn('string', 'Topping'); data.addColumn('number', 'Slices'); data.addRows([['Passed', "+totalPassedTests+"],['Failed', "+totalFailedTests+"],['Skipped', "+totalSkippedTests+"]]); new google.visualization.PieChart(document.getElementById('visualization')).draw(data,{'width':300,'height':200,slices: [{color: '#109618'}, {color:'#dc3912'}, {color: '#ff9900'}]});} google.setOnLoadCallback(drawVisualization); </script> ");
 	    return headPart;
 	}
 	
 	public static StringBuffer mailSuiteSummaryHead(){
 		return new StringBuffer().append(getMailCss());
 	}
 	
 	public static StringBuffer endHeadAndStartBody(){
 		return new StringBuffer().append("</head><body>");
 	}
 	
 	public static StringBuffer startContainer(){
 		return new StringBuffer().append("<div class='container'>");
 	}
 	
 	public static StringBuffer headerTitle(String title){
 		return new StringBuffer().append("<h3 align='center'>"+title+"</h3>");
 	}
 	
 	public static StringBuffer startRow(){
 		return new StringBuffer().append("<div class='row'>");
 	}
 	
 	public static StringBuffer configTableDiv(){
 		StringBuffer summaryTable = new StringBuffer();
 		summaryTable.append("<div class='span4'>");
 		summaryTable.append("<table align='center' class='table table-bordered span4'>");
 		summaryTable.append("<tr><th colspan='2'><p class='text-center'>Configuration</p></th></tr>");
 	    if(configProp.containsKey("url")){
 	    	summaryTable.append("<tr><th>URL</th><td>"+configProp.getProperty("url")+"</td></tr>");
 	    }
 	    summaryTable.append("<tr><th>Browser</th><td>"+configProp.getProperty("browser")+"</td></tr>");
 	    
 	    summaryTable.append("</table>");
 	    summaryTable.append("</div>");
 	    return summaryTable;
 	}
 	
 	public static StringBuffer spaceDiv(){
 		return new StringBuffer().append("<div class='span1'></div>");
 	}
 	
 	public static StringBuffer chartDiv(){
 		return new StringBuffer().append("<div id='visualization' class='span4'></div>");
 	}
 	
 	public static StringBuffer endRow(){
 		return new StringBuffer().append("</div>");
 	}
 	
 	public static StringBuffer startTable(){
 		return new StringBuffer().append("<table class='table table-bordered' align='center'>");
 	}
 	
 	public static StringBuffer suiteListTableHeaderRow(){
 		StringBuffer suiteListHeader = new StringBuffer();
 		suiteListHeader.append("<tr>");
 		suiteListHeader.append("<th>Suite</th>");
 		suiteListHeader.append("<th>Start Time</th>");
 		suiteListHeader.append("<th>End Time</th>");
 		suiteListHeader.append("<th>Time Taken</th>");
 		suiteListHeader.append("<th>Total Tests</th>");
 		suiteListHeader.append("<th>Passed</th>");
 		suiteListHeader.append("<th>Failed</th>");
 		suiteListHeader.append("<th>Skipped</th>");
 		suiteListHeader.append("</tr>");
 		return suiteListHeader;
 	}
 	
 	public static StringBuffer suiteListTableDetailsRow(boolean isMail,String suiteName, long suiteStartTime, long suiteEndTime, int suitePassedTests, int suiteFailedTests, int suiteSkippedTests){
 		int suiteTotalTests = suitePassedTests+suiteFailedTests+suiteSkippedTests;
 		String cls = suiteFailedTests > 0 ? "error" : ( (suitePassedTests > 0 && suiteSkippedTests==0 )  ? "success" :  "error"  );
 		StringBuffer suiteDetailRow = new StringBuffer();
 		suiteDetailRow.append("<tr class='"+cls+"'>");
 		
 		suiteDetailRow.append("<td>"+suiteName+"</td>");
 		suiteDetailRow.append("<td>"+sdf.format(suiteStartTime)+"</td>");
 		suiteDetailRow.append("<td>"+sdf.format(suiteEndTime)+"</td>");
 		suiteDetailRow.append("<td>"+timeTaken(suiteEndTime-suiteStartTime)+"</td>");
 		String passPercentage = Commons.percentageCalculator(suiteTotalTests,suitePassedTests);
 		String failPercentage = Commons.percentageCalculator(suiteTotalTests,suiteFailedTests);
 		String skipPercentage = Commons.percentageCalculator(suiteTotalTests,suiteSkippedTests);
 		if(!isMail){
 			suiteDetailRow.append("<td><a href='"+suiteName+File.separator+"suite-"+suiteName+"-index.html'>"+suiteTotalTests+"</a></td>");
 			suiteDetailRow.append("<td>"+(suitePassedTests > 0 ? "<a href='"+suiteName+File.separator+"suite-"+suiteName+"-passed.html'>"+suitePassedTests+"</a> ("+passPercentage+"%)" : suitePassedTests)+"</td>");
 			suiteDetailRow.append("<td>"+(suiteFailedTests > 0 ? "<a href='"+suiteName+File.separator+"suite-"+suiteName+"-failed.html'>"+suiteFailedTests+"</a> ("+failPercentage+"%)" : suiteFailedTests)+"</td>");
 			suiteDetailRow.append("<td>"+(suiteSkippedTests > 0 ? "<a href='"+suiteName+File.separator+"suite-"+suiteName+"-skipped.html'>"+suiteSkippedTests+"</a> ("+skipPercentage+"%)" : suiteSkippedTests)+"</td>");
 		}else{
 			suiteDetailRow.append("<td>"+suiteTotalTests+"</td>");
 			suiteDetailRow.append("<td>"+suitePassedTests+" ("+passPercentage+"%)</td>");
 			suiteDetailRow.append("<td>"+suiteFailedTests+" ("+failPercentage+"%)</td>");
 			suiteDetailRow.append("<td>"+suiteSkippedTests+" ("+skipPercentage+"%)</td>");
 		}
 		
 		suiteDetailRow.append("</tr>");
 		return suiteDetailRow;
 	}
 	
 	public static StringBuffer suitesSummaryRow(long startTimeOfSuites, long endTimeOfSuites, int totalPassedTests,int totalFailedTests, int totalSkippedTests){
 		int totalTests = totalPassedTests+totalFailedTests+totalSkippedTests;
 		StringBuffer suiteListHeader = new StringBuffer();
 		suiteListHeader.append("<tr>");
 		suiteListHeader.append("<th>Total</th>");
 		suiteListHeader.append("<th>"+sdf.format(startTimeOfSuites)+"</th>");
 		suiteListHeader.append("<th>"+sdf.format(endTimeOfSuites)+"</th>");
 		suiteListHeader.append("<th>"+timeTaken(endTimeOfSuites-startTimeOfSuites)+"</th>");
 		suiteListHeader.append("<th>"+totalTests+"</th>");
 		suiteListHeader.append("<th>"+totalPassedTests+(totalPassedTests > 0 ? " ("+Commons.percentageCalculator(totalTests,totalPassedTests)+"%)" : "")+"</th>");
 		suiteListHeader.append("<th>"+totalFailedTests+(totalFailedTests > 0 ? " ("+Commons.percentageCalculator(totalTests,totalFailedTests)+"%)" : "")+"</th>");
 		suiteListHeader.append("<th>"+totalSkippedTests+(totalSkippedTests > 0 ? " ("+Commons.percentageCalculator(totalTests,totalSkippedTests)+"%)" : "")+"</th>");
 		suiteListHeader.append("</tr>");
 		return suiteListHeader;
 	}
 	
 	public static StringBuffer endTable(){
 		return new StringBuffer().append("</table>");
 	}
 	
 	public static StringBuffer endContainerToHtml(){
 		return new StringBuffer().append("</div></body></html>");
 	}
 	
 	public static final Comparator<ITestResult> TIME_COMPARATOR= new TimeComparator();
 	  
 	private static class TimeComparator implements Comparator<ITestResult>, Serializable {
 		private static final long serialVersionUID = 381775815838366907L;
 		public int compare(ITestResult o1, ITestResult o2) {
 		  return (int) (o1.getStartMillis() - o2.getStartMillis());
 		}
 	} 
 	
 	public static StringBuffer suiteContextSummaryHeader(){
 		return new StringBuffer().append("<tr><th>Test</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>Total</th></tr>");
 	}
 	
 	public static StringBuffer suiteContextSummaryFooter(int suiteTotalTests, int suitePassedTests, int suiteFailedTests, int suiteSkippedTests){
 		return new StringBuffer().append("<tr><th>Total</th><th class='success'>"+suitePassedTests+(suitePassedTests > 0 ? " ("+Commons.percentageCalculator(suiteTotalTests,suitePassedTests)+"%)" : "")+"</th><th class='error'>"+suiteFailedTests+(suiteFailedTests > 0 ? " ("+Commons.percentageCalculator(suiteTotalTests,suiteFailedTests)+"%)" : "")+"</th><th class='warning'>"+suiteSkippedTests+(suiteSkippedTests > 0 ? " ("+Commons.percentageCalculator(suiteTotalTests,suiteSkippedTests)+"%)" : "")+"</th><th>"+suiteTotalTests+"</th></tr>");
 	}
 	
 	public static StringBuffer testDetailReport(List<ITestResult> testResults){
 		StringBuffer resultsStringBuffer = new StringBuffer();
 		int i=1;
 		for (ITestResult eachTestResult : testResults) {
 			String testName = eachTestResult.getName();
 	    	String testCasePath=null;
 	    	if(eachTestResult.getAttribute("reporterFilePath")!=null){
 	    		testCasePath=eachTestResult.getAttribute("reporterFilePath").toString();
 	    	}
 	    	resultsStringBuffer.append("<tr class='"+((eachTestResult.getStatus()==ITestResult.SUCCESS) ? "success" : (eachTestResult.getStatus()==ITestResult.FAILURE ? "error" : "warning") )+"'>");
 	    	
 	    	resultsStringBuffer.append("<td>"+i+"</td>");
 	    	String testDescription = eachTestResult.getMethod().getDescription();
 	    	if(testCasePath==null){
 	    		resultsStringBuffer.append("<td><div "+(testDescription != null ? "class='testNameToolTip' data-toggle='tooltip' data-placement='top' title='"+testDescription+"'" : "")+" >"+testName+"</div></td>");
 	    	}else{
 	    		resultsStringBuffer.append("<td><a href='"+testCasePath+"' "+(testDescription != null ? "class='testNameToolTip' data-toggle='tooltip' data-placement='top' title='"+testDescription+"'" : "")+" >"+testName+"</a></td>");
 	    	}
 	    	resultsStringBuffer.append("<td>"+startTimeForResult(eachTestResult)+"</td>");
 	    	resultsStringBuffer.append("<td>"+endTimeForResult(eachTestResult)+"</td>");
 	    	resultsStringBuffer.append("<td>"+tikeTakenForResult(eachTestResult)+"</td>");
 	    	String testStatus = statusForResult(eachTestResult);
 	    	String errorMessage = "";
 	    	if(eachTestResult.getStatus() != ITestResult.SUCCESS){
 	    		if(eachTestResult.getThrowable()!= null) {
 	    			if(eachTestResult.getThrowable().getMessage().equals(Commons.verificationFailuresMessage)){
 	    				List<HashMap<String, Object>> verificationErrors = VerificationErrors.getTestErrors(eachTestResult);
 	    				Iterator<HashMap<String, Object>> iter = verificationErrors.iterator();
 	    				errorMessage = "Verification Failures <br>";
 	    				while(iter.hasNext()){
 							HashMap<String, Object> errorDetails = iter.next();
 							errorMessage+="<div>"+errorDetails.get("stackTrace")+"</div><br>";
 							errorMessage+="<a href=\"screenshots"+File.separator+errorDetails.get("screenshot")+"\">Screenshot</a><br>";
 	    				}
 	    			}else{
 	    				boolean hasVerificationFailures = false;
 	    				if(VerificationErrors.hasVerificationErrors(eachTestResult)){
 	    					hasVerificationFailures = true;
 	    					List<HashMap<String, Object>> verificationErrors = VerificationErrors.getTestErrors(eachTestResult);
 		    				Iterator<HashMap<String, Object>> iter = verificationErrors.iterator();
 		    				errorMessage = "Verification Failures <br>";
 		    				while(iter.hasNext()){
 								HashMap<String, Object> errorDetails = iter.next();
 								errorMessage+="<div>"+errorDetails.get("stackTrace")+"</div><br>";
 								errorMessage+="<a href=\"screenshots"+File.separator+errorDetails.get("screenshot")+"\">Screenshot</a><br>";
 		    				}
 	    				}/*else{
 	    					if(eachTestResult.getThrowable()!=null){
 	    						errorMessage+="<hr>";
 	    						String[] stackTraces = Utils.stackTrace(eachTestResult.getThrowable(), true);
 	    						errorMessage+="<div>"+stackTraces[1]+"</div><br>";
 	    					}
 	    				}
 	    				*/
 	    				if(eachTestResult.getThrowable()!=null){
 	    					if(hasVerificationFailures){
 	    						errorMessage+="<hr>";
 	    					}
     						String[] stackTraces = Utils.stackTrace(eachTestResult.getThrowable(), true);
     						errorMessage+="<div>"+stackTraces[1]+"</div><br>";
     					}
 	    			}
 	    			
 				}
 	    	}
 	    	resultsStringBuffer.append("<td>"+(eachTestResult.getStatus()== ITestResult.SUCCESS ? testStatus : "<a href='#myModal' role='button' class='openDialog btn btn-small' data-toggle='modal' data-showthismessage='"+(errorMessage!=null ? errorMessage : "")+" "+(eachTestResult.getAttribute("screenshot")!=null? "<a href=\"screenshots"+File.separator+eachTestResult.getAttribute("screenshot").toString()+"\">Screenshot</a>" : "")+" '>"+testStatus+"</a>") +"</td>");
 	    	resultsStringBuffer.append("</tr>");
     		i++;
     	}
 		return resultsStringBuffer;
 	}
 	
 	public static StringBuffer mailTestDetailReport(List<ITestResult> testResults){
 		StringBuffer resultsStringBuffer = new StringBuffer();
 		int i=1;
 		for (ITestResult eachTestResult : testResults) {
 			String testName = eachTestResult.getName();
 	    	resultsStringBuffer.append("<tr class='"+((eachTestResult.getStatus()==ITestResult.SUCCESS) ? "success" : (eachTestResult.getStatus()==ITestResult.FAILURE ? "error" : "warning") )+"'>");
 	    	resultsStringBuffer.append("<td>"+i+"</td>");
 	    	resultsStringBuffer.append("<td>"+testName+"</td>");
 	    	resultsStringBuffer.append("<td>"+startTimeForResult(eachTestResult)+"</td>");
 	    	resultsStringBuffer.append("<td>"+endTimeForResult(eachTestResult)+"</td>");
 	    	resultsStringBuffer.append("<td>"+tikeTakenForResult(eachTestResult)+"</td>");
 	    	String testStatus=statusForResult(eachTestResult);
 	    	resultsStringBuffer.append("<td>"+testStatus+"</td>");
 	    	resultsStringBuffer.append("</tr>");
     		i++;
     	}
 		return resultsStringBuffer;
 	}
 	
 	public static String startTimeForResult(ITestResult eachTestResult){
 		return Utility.sdfTests.format(eachTestResult.getStartMillis());
 	}
 	
 	public static String endTimeForResult(ITestResult eachTestResult){
 		return Utility.sdfTests.format(eachTestResult.getEndMillis());
 	}
 	
 	public static String tikeTakenForResult(ITestResult eachTestResult){
 		return Utility.timeTaken(eachTestResult.getEndMillis()-eachTestResult.getStartMillis());
 	}
 	
 	public static String statusForResult(ITestResult eachTestResult){
 		return Utility.getStatusString(eachTestResult.getStatus());
 	}
 	
 	public static StringBuffer headerContextDetailedReport(){
 		StringBuffer testResultsHeader = new StringBuffer();
 		testResultsHeader.append("<tr>");
 		testResultsHeader.append("<th>S.No</th>");
 		testResultsHeader.append("<th>Test Case</th>");
 		testResultsHeader.append("<th>Start Time</th>");
 		testResultsHeader.append("<th>End Time</th>");
 		testResultsHeader.append("<th>Time Taken</th>");
 		testResultsHeader.append("<th>Status</th>");
 		testResultsHeader.append("</tr>");
 		return testResultsHeader;
 	}
 	
 	public static List<ITestResult> sortResults(Set<ITestResult> results){
 		List<ITestResult> testResults = new ArrayList<ITestResult>();
 		testResults.addAll(results);
     	Collections.sort(testResults, TIME_COMPARATOR);
 	    return testResults;
 	}
 	
 	private static StringBuffer getContextDetailedReport(String contextName,Set<ITestResult> results, boolean isMail){
 		StringBuffer contextReport = new StringBuffer();
		contextReport.append("<table class='table table-bordered' id='"+contextName+"' >");
 		contextReport.append("<caption>Detailed report of "+contextName+" Tests</caption>");
 		contextReport.append(headerContextDetailedReport());
 		if(isMail){
 			contextReport.append(mailTestDetailReport(sortResults(results)));
 		}else{
 			contextReport.append(testDetailReport(sortResults(results)));
 		}
 	    
 	    contextReport.append("</table><hr/>");
 	    return contextReport;
 	}
 	
 	public static StringBuffer skippedContextDetailedReport(ITestContext ctx){
 		return getContextDetailedReport(ctx.getName(),ctx.getSkippedTests().getAllResults(), false);
 	}
 	
 	public static StringBuffer failedContextDetailedReport(ITestContext ctx){
 		return getContextDetailedReport(ctx.getName(),ctx.getFailedTests().getAllResults(), false);
 	}
 	
 	public static StringBuffer passedContextDetailedReport(ITestContext ctx){
 		return getContextDetailedReport(ctx.getName(),ctx.getPassedTests().getAllResults(), false);
 	}
 	
 	public static StringBuffer contextDetailedReport(ITestContext ctx, boolean isMail){
 		Set<ITestResult> testResults = new HashSet<ITestResult>();
 		testResults.addAll(ctx.getPassedTests().getAllResults());
 		testResults.addAll(ctx.getFailedTests().getAllResults());
 		testResults.addAll(ctx.getSkippedTests().getAllResults());
 		return getContextDetailedReport(ctx.getName(), testResults, isMail);
 	}
 	
 }
