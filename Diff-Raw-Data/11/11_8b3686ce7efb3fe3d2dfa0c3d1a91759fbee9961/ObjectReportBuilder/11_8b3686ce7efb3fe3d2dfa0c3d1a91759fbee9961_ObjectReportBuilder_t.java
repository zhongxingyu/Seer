 package model.reports;
 
 import common.Result;
 
 import model.reports.IPrintObject.IPrintObjectTable;
 import model.reports.PrintObject.PrintObjectTable;
 
 public class ObjectReportBuilder implements ReportBuilder {
 	private PrintObject printObject;
 	
 	public ObjectReportBuilder(){
 		printObject = new PrintObject();
 	}
 	
 	public void addHeader(String header) {
 		printObject.addHeader(header);
 	}
 
 	public void startTable(int columns) {
 		printObject.addTable();
 	}
 
 	public Result addRow(String[] row) {
 		int currentTable = -1 + printObject.getTablesSize();
 		if(currentTable >= 0){
 			IPrintObjectTable table = printObject.getTable(currentTable);
 			table.addRow(row);
 		}
 		return new Result(true);
 	}
 
 
 	public void addHeading(String heading) {
 		this.addHeader(heading);
 	}
 
 	@Override
 	public Result endTable() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void addTextBlock(String text) {
 		printObject.addTextBlock(text);
 	}
 
 	@Override
 	public void endFile() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String returnReport() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public IPrintObject returnObject(){
 		return this.printObject;
 	}
 


 }
