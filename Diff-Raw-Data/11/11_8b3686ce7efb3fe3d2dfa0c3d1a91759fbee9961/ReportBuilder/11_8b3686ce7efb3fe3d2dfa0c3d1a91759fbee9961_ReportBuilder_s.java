 package model.reports;
 
 import common.Result;
 
 public interface ReportBuilder {
 	
 	public void addHeader(String header);
 	
 	public void addHeading(String heading);
 
 	public void addTextBlock(String text);
 
 	public void startTable(int columns);
 	
 	public Result addRow(String[] row);
 	
 	public Result endTable();
 	
 	public void endFile();
 	
 	public String returnReport();
	
	public IPrintObject getReportObject();
	
 }
