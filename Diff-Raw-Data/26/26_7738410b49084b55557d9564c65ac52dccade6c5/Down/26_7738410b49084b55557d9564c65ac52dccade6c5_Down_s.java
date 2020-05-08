 package com.ids.controllers;
 
 import java.io.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFFont;
 import org.apache.poi.hssf.usermodel.HSSFHeader;
 import org.apache.poi.hssf.usermodel.HSSFRichTextString;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.Header;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.LoggerFactory;
 
 public class Down extends HttpServlet {
 	
 	private final static Logger logger = Logger.getLogger(Down.class.getName()); 
 	
   public void doGet(HttpServletRequest request,
                     HttpServletResponse resp)
       throws ServletException, IOException {
       
     // Use "request" to read incoming HTTP headers (e.g. cookies)
     // and HTML form data (e.g. data the user entered and submitted)
     
     // Use "response" to specify the HTTP response line and headers
     // (e.g. specifying the content type, setting cookies).
     
 	  JSONObject myData = null;
 	  JSONObject myTotals = null;
 	  try {
 		 myData =  new JSONObject(request.getParameter("jsonStuff"));
 		 myTotals = new JSONObject(request.getParameter("jsonTotals"));
 	} catch (JSONException e) {
 		logger.warning("it failed: " +e.getMessage() );
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	  
 	  
 	  
 	  
 	  resp.setContentType("application/octet-stream");
       resp.setHeader("Content-Disposition","attachment;filename=temp.xls");
 	  
 		ServletOutputStream out = resp.getOutputStream();
 	//	StringBuffer sb = generateCsvFileBuffer();
 
 		HSSFWorkbook workbook = new HSSFWorkbook();
 			HSSFSheet worksheet = workbook.createSheet("Off-Highway Research");
 
 			worksheet.setColumnWidth(0, 7000);
 			
 			Header header = worksheet.getHeader();
 		    header.setCenter("Center Header");
 		    header.setLeft("Left Header");
 		    header.setRight(HSSFHeader.font("Stencil-Normal", "Italic") +
 		                    HSSFHeader.fontSize((short) 16) + "Right w/ Stencil-Normal Italic font and size 16");
 		    
 	
 			
 
 
 		    HSSFRow rowhead00 = worksheet.createRow((short)0);
 		    
 			  HSSFCell cellHead00 = rowhead00.createCell((short) 1); 
			  cellHead00.setCellValue(new HSSFRichTextString("Off-Highway Research"));
 			   HSSFCellStyle cellStyle1 = workbook.createCellStyle();  
 		        cellStyle1 = workbook.createCellStyle();  
 		        HSSFFont hSSFFont1 = workbook.createFont();  
 		        hSSFFont1.setFontName(HSSFFont.FONT_ARIAL);  
 		        hSSFFont1.setFontHeightInPoints((short) 20);  
 		        hSSFFont1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
 		        hSSFFont1.setColor(HSSFColor.BLACK.index);  
 		        cellStyle1.setFont(hSSFFont1);
 		        cellHead00.setCellStyle(cellStyle1);
 			  
 			  
 	        
 			   HSSFRow rowhead = worksheet.createRow((short) 2);
 			   
 			   Date d = new Date();
 
 				  String myString = DateFormat.getDateInstance(DateFormat.LONG).format(d);
 				  HSSFCell cellHead0 = rowhead.createCell((short) 0); 
 				  cellHead0.setCellValue(new HSSFRichTextString(myString));
 				  
 			   HSSFCell cellHead = rowhead.createCell((short) 4);
  
 
			   cellHead.setCellValue(new HSSFRichTextString(request.getParameter("title1") + " " + request.getParameter("title2") + " "+
 						  request.getParameter("title3") + " " + request.getParameter("title4")));
     		   
 			   
 			   HSSFCellStyle cellStyle = workbook.createCellStyle();  
 		        cellStyle = workbook.createCellStyle();  
 		        HSSFFont hSSFFont = workbook.createFont();  
 		        hSSFFont.setFontName(HSSFFont.FONT_ARIAL);  
 		        hSSFFont.setFontHeightInPoints((short) 16);  
 		        hSSFFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
 		        hSSFFont.setColor(HSSFColor.BLACK.index);  
 		        cellStyle.setFont(hSSFFont);
 			   
 
 		        
 		        cellHead.setCellStyle(cellStyle);
 			   
 			   HSSFRow row = worksheet.createRow((short) 4);
     		   
     		   
 			   NumberFormat nf = NumberFormat.getInstance();
 	  
 	  
 		if(myData.has("tabData")){
 			try {
 				logger.warning("it has tabData");
 
 				JSONArray clobArray = myData.getJSONArray("tabData");
 				
 				
 				JSONObject jo1 = clobArray.getJSONObject(1);
 				logger.warning("size1 is: "+clobArray.length());
 
 				
 				
 				List<String> columnHeaders = new ArrayList<String>();
 				if (jo1.has("columns")){
 					logger.warning("it has myData");
 					JSONArray myArray = jo1.getJSONArray("columns");
 					logger.warning("myArray size: "+myArray.length());
 					   HSSFCellStyle cellStyleH = workbook.createCellStyle();  
 				        cellStyleH = workbook.createCellStyle();  
 				        HSSFFont hSSFFontH = workbook.createFont();  
 				        hSSFFontH.setFontName(HSSFFont.FONT_ARIAL);  
 				        hSSFFontH.setFontHeightInPoints((short) 12);  
 				        hSSFFontH.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
 				        hSSFFontH.setColor(HSSFColor.BLACK.index);  
 				        cellStyleH.setFont(hSSFFontH);
 					for (int i = 0; i < myArray.length();i++){
 						   HSSFCell cellA = row.createCell((short) i);
 						   
 
 					        cellA.setCellStyle(cellStyleH);
 					        
 			    		   cellA.setCellValue(myArray.getString(i));
 						columnHeaders.add(myArray.getString(i));
 
 					}
 				}
 				
 				
 				
 				JSONObject jo2 = clobArray.getJSONObject(2);
 				logger.warning("size2 is: "+jo2.length());
 
 				if (jo2.has("myData")){
 					logger.warning("it has myData");
 					JSONArray myDataArray = jo2.getJSONArray("myData");
 					logger.warning("myData size: "+myDataArray.length());
 					for (int i = 0; i < myDataArray.length();i++){
 						
 						 HSSFRow nextRow = worksheet.createRow((short) i+5);
 						 int j=0;
 						for (String s : columnHeaders){
 							
 							   HSSFCell cellA = nextRow.createCell((short) j);
 							   j+=1;
 						   try{
 							   if (j!=1) {
 							    //  cellA.setCellType(Cell.CELL_TYPE_NUMERIC);
 								   cellA.setCellType(Cell.CELL_TYPE_STRING);
 							      cellA.setCellValue(" "+nf.format(Integer.parseInt(myDataArray.getJSONObject(i).getString(s).trim().replaceAll(",",""))));
 							      HSSFCellStyle cellStyleH = workbook.createCellStyle();  
 							        cellStyleH = workbook.createCellStyle(); 
 							        cellStyleH.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
 							        cellA.setCellStyle(cellStyleH);
 							   }else {
 							//	   cellA.setCellType(Cell.CELL_TYPE_STRING);
 								   cellA.setCellType(Cell.CELL_TYPE_STRING);
 								   cellA.setCellValue(myDataArray.getJSONObject(i).getString(s).trim().replaceAll(",",""));
 							   }  
 
 						   }catch(Exception e){
 							   cellA.setCellValue(0);
 						   }
 						}
 					}
 					
 
 					JSONArray totals = myTotals.getJSONArray("myTotals");
 				//	JSONObject jo3 =  totals1.getJSONObject(0);
 					//	JSONObject jo3 = totals.getJSONObject(0);
 					HSSFRow nextRow = worksheet.createRow((short) myDataArray.length()+6);
 					
 					 int j=0;
 						for (String s : columnHeaders){
 							
 							   HSSFCell cellA = nextRow.createCell((short) j);
 							   j+=1;
 						   try{
 							   
 
 							   if (j!=1) {
 								   //   cellA.setCellType(Cell.CELL_TYPE_NUMERIC);
 								   cellA.setCellType(Cell.CELL_TYPE_STRING);
 								      cellA.setCellValue(" "+nf.format(Integer.parseInt(totals.getJSONObject(0).getString(s).trim().replaceAll(",",""))));
 								
 									   HSSFCellStyle cellStyleH = workbook.createCellStyle();  
 								        cellStyleH = workbook.createCellStyle();  
 								        HSSFFont hSSFFontH = workbook.createFont();  
 								        hSSFFontH.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
 								        cellStyleH.setFont(hSSFFontH);
 								        cellStyleH.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
 								        cellA.setCellStyle(cellStyleH);
 								        
 								        
 							   }else {
 									//   cellA.setCellType(Cell.CELL_TYPE_STRING);
 									   cellA.setCellType(Cell.CELL_TYPE_STRING);
 									   cellA.setCellValue(totals.getJSONObject(0).getString(s).trim().replaceAll(",",""));
 									   HSSFCellStyle cellStyleH = workbook.createCellStyle();  
 								        cellStyleH = workbook.createCellStyle();  
 								        HSSFFont hSSFFontH = workbook.createFont();  
 								        hSSFFontH.setFontName(HSSFFont.FONT_ARIAL);  
 								        hSSFFontH.setFontHeightInPoints((short) 12);  
 								        hSSFFontH.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
 								        hSSFFontH.setColor(HSSFColor.BLACK.index);  
 								        
 								        cellStyleH.setFont(hSSFFontH);
 								        cellA.setCellStyle(cellStyleH);
 								   }
 						   }catch(Exception e){
 							   e.printStackTrace();
 							   logger.warning(e.getMessage());
 							   cellA.setCellValue(0);
 						   }
 						}
 	
 					
 				}
 				
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	
 	  
 	  
 	  logger.warning("got in here");
 
     		   
 			   
     		   
     		  
     		   
     		   
 			workbook.write(out);
 			out.flush();
 			out.close();
 		
 
   }
   
   
   public void doPost(HttpServletRequest request,
           HttpServletResponse response) throws ServletException, IOException{
 	  
 	  doGet(request, response);
   }
   
 	
   
 }
