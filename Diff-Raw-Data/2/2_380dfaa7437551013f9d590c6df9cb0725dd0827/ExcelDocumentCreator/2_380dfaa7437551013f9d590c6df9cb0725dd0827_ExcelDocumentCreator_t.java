 package com.sff.report_performance;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.DataFormat;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import com.borland.dx.dataset.VariantException;
 import com.borland.dx.sql.dataset.Database;
 import com.borland.dx.sql.dataset.QueryDataSet;
 import com.borland.dx.sql.dataset.QueryDescriptor;
 
 /*
  * This class creates the excel document in another thread, using a swing worker
  * to publish the process progress.
  */
 @SuppressWarnings("rawtypes")
 public class ExcelDocumentCreator extends SwingWorker<String, Integer> {
 	private XSSFWorkbook workbook;
 	private File output;
 	private InputStream template;
 	private XSSFSheet sheetTable;
 	private XSSFSheet sheetProject;
 	private List<List> customerData;
 	private List<List> projectData;
 	private List<List> statusData;
 	private JTextField publishedOutput;
 	private JTextField progressField;
 	private FileOutputStream out;
 
 	public ExcelDocumentCreator(List<List> customerData, List<List> projectData, List<List> statusData, JTextField publishedOutput, JTextField progressField, FileOutputStream out, File output){
 		try {
 			this.customerData = customerData;
 			this.projectData = projectData;
 			this.statusData = statusData;
 			this.publishedOutput = publishedOutput;
 			this.progressField = progressField;
 			this.out = out;
 			this.output = output;
 
 			//TODO: relative path
 			template = new FileInputStream("C:/Users/hbs/workspace/SelectionGUI/DeliveryPerformance/template/template.xlsx");
 			workbook = (XSSFWorkbook) WorkbookFactory.create(template);
 
 			sheetTable = workbook.getSheet("Table");
 			sheetTable.setZoom(70);
 			sheetProject = workbook.getSheet("Project");
 
 		} catch (IOException | InvalidFormatException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void saveWorkbook() {
 		try {
 			workbook.setActiveSheet(0);
 			workbook.write(out);
 			out.close();
 			Desktop.getDesktop().open(output);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected String doInBackground() {
 		try{
 			
 			boolean allCustSelected = customerData.size()==0 ? true : false;
 			boolean allProjSelected = projectData.size()==0 ? true : false;
 			boolean allStatSelected = statusData.size()==0 ? true : false;
 
 			StringBuilder query = generateQuery(allCustSelected,allProjSelected, 
 					allStatSelected, customerData, projectData, statusData);
 
 			Database db = DatabaseConnection.getDatabase();
 			QueryDataSet dataSet = new QueryDataSet();
 			dataSet.setQuery(new QueryDescriptor(db, query.toString()));
 			dataSet.open();
 
 			int rowCount = dataSet.getRowCount();
 			int processed = 0;
 			
 			CellStyle decimalStyle = workbook.createCellStyle();
 			CellStyle sixDigitStyle = workbook.createCellStyle();
 			DataFormat format = workbook.createDataFormat();
 			decimalStyle.setDataFormat(format.getFormat("##00.00"));
 			sixDigitStyle.setDataFormat(format.getFormat("000000"));
 
 			publishedOutput.setText("Generating Excel Document");
 			while(!isCancelled()){
 				Set<String> currencySet = new HashSet<String>();
 				while(dataSet.next()){
 					setProgress(100 * processed++ / rowCount);
 					progressField.setText("Adding row: " + processed);
 
 					sheetTable.createRow(dataSet.getRow());
 					for(int column = 0; column < dataSet.getColumnCount(); column++){
 						Cell cell = sheetTable.getRow(dataSet.getRow()).createCell(column);
 						if(dataSet.getColumn(column).equals(dataSet.getColumn("Unit Price"))){
 							cell.setCellStyle(decimalStyle);
 						}
 						if(dataSet.getColumn(column).equals(dataSet.getColumn("Vendor nr."))){
 							cell.setCellStyle(sixDigitStyle);
 						}
 						if(dataSet.getColumn(column).equals(dataSet.getColumn("currency"))){
 							currencySet.add(dataSet.getString(column));
 						}
 						try{
 							String s = dataSet.getString(column);
 							cell.setCellValue(s);
 						}catch(VariantException e){
 							try{
 								Double d = dataSet.getDouble(column);
 								cell.setCellValue(d);
 							}catch(VariantException v){
 								int i = dataSet.getInt(column);
 								cell.setCellValue(i);
 							}
 						}
 					}
 				}
 				
 				// TODO: Does not account for the currency exchange rates.
 				int last = sheetTable.getLastRowNum() + 1;
 				int rowPosition = 4;
 				CellStyle cellStyle = sheetProject.getRow(5).getCell(1).getCellStyle();
 				
				if(currencySet.size()!=0)sheetProject.shiftRows(4, 8, currencySet.size()); 
 				
 				for(String currency : currencySet){
 					Row row = sheetProject.createRow(rowPosition++);
 					
 					Cell description = row.createCell(0);
 					description.setCellValue("Total Value [" + currency + "]:");
 					description.setCellStyle(cellStyle);
 					
 					Cell sum = row.createCell(1);
 					sum.setCellFormula("SUMIF(Table!$N$2:$N$" + last + ",\""+ currency + "\"," + "Table!$M$2:$M$" + last + ")");
 					sum.setCellStyle(cellStyle);
 					
 				}
 				
 				publishedOutput.setText("Opening Excel Document");
 
 				saveWorkbook();
 				dataSet.close();
 				cancel(true);
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private StringBuilder generateQuery(boolean allCustSelected,
 			boolean allProjSelected, boolean allStatSelected,
 			List<List> temp_cust, List<List> temp_proj, List<List> temp_stat) {
 		
 		/*
 		 * The base statement is used no matter the user selection
 		 */
 		StringBuilder query = new StringBuilder(5000);
 		String basicStatement = "select \"Project\" = Project.pr_name,"
 				+ " \"Client\" = customerList.assoc_name,"
 				+ " \"Client Ref.\" = Tr_hdr.assoc_ref,"
 				+ " \"Order Nr.\" = Tr_hdr.tr_no, "
 				+ " \"Order Registration Date\" = convert(varchar(20), Tr_hdr.reg_date, 104),"		   
 				+ " \"Item nr.\" = clientItemList.item_no,"
 				+ " \"Client Art. code\" = clientItemList.local_id,"
 				+ " \"Vendor nr.\" = clientItemList.vnd_no, "
 				+ " \"Description\" = clientItemList.description,"
 				+ " \"Supplier\" = supplierList.assoc_name ,"
 				+ " \"QTY\" = clientItemList.qnt,"
 				+ " \"Unit Price\" = clientItemList.price,"
 				+ " \"Total Price\" = clientItemList.qnt*clientItemList.price,"
 				+ " \"currency\" = Exchange.curr_name,"
 				+ " \"CDD\" = convert(varchar(20), clientItemList.contract_date, 104),"
 				+ " \"EDD\" = convert(varchar(20), clientItemList.estimate_date, 104),"
 				+ " \"RFI\" = convert(varchar(20), clientItemList.rfi_date, 104)," 
 				+ " \"CCD\" = convert(varchar(20), supplierItemList.contract_date, 104),"
 				+ " \"ECD\" = convert(varchar(20), supplierItemList.estimate_date, 104),"
 				+ " \"Item Status\" = Tr_dtl_status.tr_dtl_stname"
 				+ " from vendor.dbo.Tr_hdr," 
 				+ " vendor.dbo.Tr_dtl clientItemList left join vendor.dbo.Tr_dtl supplierItemList" 
 				+ " on (clientItemList.vnd_no = supplierItemList.vnd_no"           
 				+ " and clientItemList.item_no = supplierItemList.item_no"           
 				+ " and clientItemList.suppl_tr_id = supplierItemList.tr_no" 
 				+ " and supplierItemList.tr_dtl_status>0" 
 				+ " and supplierItemList.vnd_no > 1"
 				+ " )," 
 				+ " vendor.dbo.Assoc customerList," 
 				+ " vendor.dbo.Assoc supplierList," 
 				+ " vendor.dbo.Project," 
 				+ " vendor.dbo.Exchange," 
 				+ " vendor.dbo.Tr_dtl_status"
 				+ " where Tr_hdr.tr_status = 2"
 				+ " and Tr_hdr.tr_no = clientItemList.tr_no" 
 				+ " and Tr_hdr.assoc_id = customerList.assoc_id"
 				+ " and Tr_hdr.active_id = Project.project_id"
 				+ " and clientItemList.suppl_id = supplierList.assoc_id" 
 				+ " and clientItemList.currency_id = Exchange.currency_id"
 				+ " and clientItemList.tr_dtl_status = Tr_dtl_status.tr_dtl_status";
 		
 		/*
 		 * If the user have NOT selected all items in the list the method will
 		 * specify the search to only include the selected items.
 		 */
 		query.append(basicStatement);
 		if(!allCustSelected){
 			query.append(" and customerList.assoc_id in (");
 			for(List l : temp_cust){
 				int id =  (int) l.get(1);
 				query.append(id + ", ");
 			}
 			query.delete(query.length()-2, query.length());
 			query.append(")");
 		}
 		if(!allProjSelected){
 			query.append(" and Project.pr_name in (");
 			for(List l : temp_proj){
 				String name = (String) l.get(1);
 				query.append("'" + name + "', ");
 			}
 			query.delete(query.length()-2, query.length());
 			query.append(")");
 		}
 		if(!allStatSelected){
 			query.append(" and Tr_dtl_status.tr_dtl_stname in (");
 			for(List l : temp_stat){
 				String status = (String) l.get(1);
 				query.append("'" + status + "', ");
 			}
 			query.delete(query.length()-2, query.length());
 			query.append(")");
 		}
 		return query;
 	}
 }
