 package com.example.ota;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 
 import jxl.Cell;
 import jxl.Sheet;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 import android.os.Bundle;
 import android.R.color;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.support.v4.view.MotionEventCompat;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.HorizontalScrollView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EditOrderList extends Activity {
 	ScrollView svEditOL = null;
 	HorizontalScrollView hvEditOL = null;
 	TableLayout tlEditOL = null;
 	TableRow trEditOL = null;
 	TextView tvEditShopName = null, tvEditOrderNo = null, tvEditBeatName = null;
 	Button btnEditUpdateOrder = null, btnEditGoToDashBoard =  null, btnEditLogout = null;
 	TextView lbl_TotalNetAmount = null;
 	SessionManagement session;
 	final DecimalFormat dcf = new DecimalFormat("0.00");
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_order_list);
 		
 		try {
 			// Session class instance
 			session = new SessionManagement(getApplicationContext());
 			//Toast.makeText(getApplicationContext(), "User Login Status: "+ session.isLoggedIn(), Toast.LENGTH_LONG).show();
 			/**
 	         * Call this function whenever you want to check user login
 	         * This will redirect user to LoginActivity is he is not
 	         * logged in
 	         * */
 			session.checkLogin();
 			// Get the message from the intent
 			Intent getIntentMessage = getIntent();
 			final String messageReceivedShopName = getIntentMessage.getStringExtra(ListOfOrders.EXTRA_MESSAGE_SHOPNAME);
 			//String messageReceivedOrderNo = getIntentMessage.getStringExtra(ListOfOrders.EXTRA_MESSAGE_ORDERNO);
 			//Toast.makeText(getApplicationContext(), messageReceivedShopName, 2).show();
 			Log.d("RECEIVED MESSAGE FROM LIST OF ORDER LIST PAGE", messageReceivedShopName);
 			//Log.d("RECEIVED MESSAGE FROM LIST OF ORDER LIST PAGE", messageReceivedOrderNo);
 			tvEditShopName = (TextView)findViewById(R.id.tv_edit_ShopName);
 			tvEditShopName.setText(messageReceivedShopName);
 			
 			/*tvEditOrderNo = (TextView)findViewById(R.id.tv_edit_OrderNo);
 			tvEditOrderNo.setText(messageReceivedOrderNo);*/
 			
 			fngetProducts(messageReceivedShopName, FilePath.getExternalPath());
 			
 			btnEditUpdateOrder = (Button)findViewById(R.id.btnEditOrderListUpdateOrder);
 			btnEditUpdateOrder.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(session.isLoggedIn() == true){
 						fnUpdateTableRowValues(FilePath.getExternalPath(), messageReceivedShopName);
 					}else{
 						session.checkLogin();
 					}
 					
 				}
 			});
 			
 			btnEditGoToDashBoard = (Button)findViewById(R.id.btnEditOrderListDashboard);
 			btnEditGoToDashBoard.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(session.isLoggedIn() == true){
 						Intent myDash = new Intent(getApplicationContext(), MainActivity.class);
 						startActivity(myDash);
 					}else{
 						session.checkLogin();
 					}
 				}
 			});
 			
 			btnEditLogout = (Button)findViewById(R.id.btnEditOrderListLogOut);
 			btnEditLogout.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					// Clear the session data
 		            // This will clear all session data and
 		            // redirect user to LoginActivity
 					session.logoutUser();
 				}
 			});
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.edit_order_list, menu);
 		return true;
 	}
 
 	
 	private void fngetProducts(String SheetName, String fileFullPath){
 		if(session.isLoggedIn() == true){
 			File f = new File(fileFullPath);
 			Workbook w;
 			Double TotalNetAmountCalc = 0.0;
 				try {
 					w = Workbook.getWorkbook(f);
 					Sheet sheet = w.getSheet(SheetName);
 					
 					tlEditOL = (TableLayout)findViewById(R.id.tblEditOrderList);
 					TableRow row = null;
 					// load values from excel to tablelayout
 					for(int i=0;i<sheet.getRows()-1;i++){
 						if(i==0){
 							Cell cellOrderNo = sheet.getCell(1,i);
 							tvEditOrderNo = (TextView)findViewById(R.id.tv_edit_OrderNo);
 							tvEditOrderNo.setText(cellOrderNo.getContents().toString());
 							
 							Cell cellBeatName = sheet.getCell(5,i);
 							tvEditBeatName = (TextView)findViewById(R.id.tv_edit_BeatName);
 							tvEditBeatName.setText(cellBeatName.getContents().toString());
 						}
 						if(i>1 && i<sheet.getRows()-1){
 							Cell cellProductName = sheet.getCell(1, i);
 							Cell cellInStock = sheet.getCell(2, i);
 							Cell cellOrderedQty = sheet.getCell(3, i);
 							Cell cellAmount = sheet.getCell(4, i);
 							Cell cellNetAmount = sheet.getCell(5,i);
 							Log.d("Values of Products in loading tables"+i, cellProductName.getContents() +"@"+ cellInStock.getContents()+"@"+cellAmount.getContents());
 							if(cellProductName.getContents().toString() != ""){
 								// Create the table from the source code without xml:
 								row = new TableRow(this);
 								row.setBackgroundColor(color.darker_gray);
 								tlEditOL.addView(row);
 								// number of rows
 								TextView nr = new TextView(this);
 								nr.setBackgroundColor(color.darker_gray);
 								nr.setTextColor(Color.BLUE);
 								nr.setText(String.valueOf(i-1));
 								row.addView(nr);
 								LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) nr.getLayoutParams();
 								llp.setMargins(0, 0, 0, 1);
 								nr.setLayoutParams(llp);
 								nr.setPadding(10, 10, 40, 3);
 								
 								// product name
 								//final String name = String.valueOf(cellProductName.getContents().toString());
 								TextView prdName = new TextView(this);
 								prdName.setBackgroundColor(color.darker_gray);
 								prdName.setTextColor(Color.BLUE);
 								prdName.setText(String.valueOf(cellProductName.getContents().toString()));
 								prdName.setHorizontalFadingEdgeEnabled(true);
 								row.addView(prdName);
 								llp = (LinearLayout.LayoutParams) prdName.getLayoutParams();
 								llp.setMargins(0, 0, 0, 1);
 								prdName.setLayoutParams(llp);
 								prdName.setPadding(10, 10, 40, 3);
 												
 								// instock
 								TextView inStock = new TextView(this);
 								inStock.setBackgroundColor(color.darker_gray);
 								inStock.setTextColor(Color.BLUE);
 								inStock.setText(String.valueOf(cellInStock.getContents().toString()));
 								row.addView(inStock);
 								llp = (LinearLayout.LayoutParams) inStock.getLayoutParams();
 								llp.setMargins(0, 0, 0, 1);
 								inStock.setLayoutParams(llp);
 								inStock.setPadding(10, 10, 40, 3);
 								
 								// orderqty
 								final EditText orderQty = new EditText(this);
 								orderQty.setBackgroundColor(Color.YELLOW);
 								orderQty.setTextColor(Color.BLACK);
 								orderQty.setText(String.valueOf(cellOrderedQty.getContents().toString()));
 								row.addView(orderQty);
 								llp = (LinearLayout.LayoutParams) orderQty.getLayoutParams();
 								llp.setMargins(0, 0, 0, 1);
 								orderQty.setLayoutParams(llp);
 								orderQty.setPadding(10, 10, 40, 3);
 								
 								// amount
 								final TextView amount = new TextView(this);
 								amount.setBackgroundColor(color.darker_gray);
 								amount.setTextColor(Color.BLUE);
 								amount.setText(String.valueOf(cellAmount.getContents().toString()));
 								row.addView(amount);
 								llp = (LinearLayout.LayoutParams) amount.getLayoutParams();
 								llp.setMargins(0, 0, 0, 1);
 								amount.setLayoutParams(llp);
 								amount.setPadding(10, 10, 40, 3);
 								//calculate
 			
 								// netamount
 								final TextView netAmount = new TextView(this);
 								netAmount.setBackgroundColor(color.darker_gray);
 								netAmount.setTextColor(Color.BLUE);
 								netAmount.setText(String.valueOf(cellNetAmount.getContents().toString()));
 								row.addView(netAmount);
 								llp = (LinearLayout.LayoutParams) netAmount.getLayoutParams();
 								llp.setMargins(0, 0, 0, 1);
 								netAmount.setLayoutParams(llp);
 								netAmount.setPadding(10, 10, 40, 3);
 								
 								orderQty.addTextChangedListener(new TextWatcher() {
 									
 									@Override
 									public void onTextChanged(CharSequence s, int start, int before, int count) {
 										// TODO Auto-generated method stub
 										
 									}
 									
 									@Override
 									public void beforeTextChanged(CharSequence s, int start, int count,
 											int after) {
 										// TODO Auto-generated method stub
 										
 									}
 									
 									@Override
 									public void afterTextChanged(Editable s) {
 										// TODO Auto-generated method stub
 										//v.setBackgroundColor(Color.DKGRAY);
 										Double etOrderQty=0.0;
 										if(orderQty.getText().length() > 0){
 									    	etOrderQty = Double.valueOf(orderQty.getText().toString());
 									    }
 									    Double tvAmount = Double.valueOf(amount.getText().toString());
 									    final Double tvNetAmount = (etOrderQty * tvAmount);
 									    
 									    //v.setBackgroundColor(color.darker_gray);
 									    netAmount.setText(String.valueOf(dcf.format(tvNetAmount)));
 									    lbl_TotalNetAmount = (TextView)findViewById(R.id.lblEditTotalNetAmount);
 									    lbl_TotalNetAmount.setText(Html.fromHtml("<b>Total Net Amount : "+ dcf.format(getTotalNetAmountAfterChangedOrder()) +"</b>"));
 									}
 								});
 								if(netAmount.getText().length()>0){
 									TotalNetAmountCalc += Double.valueOf(netAmount.getText().toString());
 									lbl_TotalNetAmount = (TextView)findViewById(R.id.lblEditTotalNetAmount);
 									lbl_TotalNetAmount.setText(Html.fromHtml("<b>Total Net Amount : "+ dcf.format(TotalNetAmountCalc) +"</b>"));
 								}
 								/*row.setOnClickListener(new View.OnClickListener() {
 									
 									@Override
 									public void onClick(View v) {
 										
 										v.setBackgroundColor(Color.DKGRAY);
 										
 										final TableRow t = (TableRow) v;
 									    TextView textViewSno = (TextView) t.getChildAt(0);
 									    TextView textViewProductName = (TextView) t.getChildAt(1);
 									    TextView textViewInStock = (TextView) t.getChildAt(2);
 									    EditText editTextOrderQty = (EditText) t.getChildAt(3);
 									    TextView textViewAmount = (TextView) t.getChildAt(4);
 									    TextView textViewNetAmount = (TextView) t.getChildAt(5);
 									    
 									    String tvSno = textViewSno.getText().toString();
 									    String tvProductName = textViewProductName.getText().toString();
 									    Double tvInStock = Double.valueOf(textViewInStock.getText().toString());
 									    Double etOrderQty = 0.0;
 									    if(editTextOrderQty.getText().length() > 0){
 									    	etOrderQty = Double.valueOf(editTextOrderQty.getText().toString());
 									    }
 									    Double tvAmount = Double.valueOf(textViewAmount.getText().toString());
 									    final Double tvNetAmount = (etOrderQty * tvAmount);
 									    
 									    v.setBackgroundColor(color.darker_gray);
 									    netAmount.setText(String.valueOf(tvNetAmount));
 									   
 									    
 									}
 								});*/
 								
 							} // end of if statement
 						} // end of i>1 if statement
 					} // end of for loop
 					
 				} catch (BiffException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		
 		}else{
 			session.checkLogin();
 		}
 	}
 	
 // Save order list to Excel file
 	private void fnUpdateTableRowValues(String filename, String sheetName){
 		try {
 			HSSFWorkbook workbook = null;
 			HSSFSheet sheet = null;
 			HSSFRow row = null;
 			HSSFCell cell = null;
 			HSSFCellStyle cellStyle = null;
 			
 			File fp = new File(filename);
 			FileInputStream is = new FileInputStream(fp);
 			//String sheetName = spnShopName.getSelectedItem().toString();
 			// generate current time
 			Date now = Calendar.getInstance().getTime();
 			SimpleDateFormat df = new SimpleDateFormat("MMddyyyy");
 			String theDate = df.format(now);
 			
 			if(fp.exists() == true){
 				workbook = new HSSFWorkbook(is);
 				String st=null;
 				boolean status = false;
 				for(int s=0;s<workbook.getNumberOfSheets();s++){
 					st = workbook.getSheetName(s);
 					if(st.equals(sheetName)){
 						status=true;
 						Log.d("Got Sheet Names"+s+"/"+sheetName, st);
 					}else{
 						Log.d("No Action made on sheet"+s+"/"+sheetName, st);
 					}
 				}
 				if(status == false){
 					sheet = workbook.createSheet(sheetName);
 					Log.d("Status of boolean", String.valueOf(status));
 				}else{
 					sheet = workbook.getSheet(sheetName);
 					Log.d("Status of boolean", String.valueOf(status));
 				}
 				//Toast.makeText(getApplicationContext(), "Total Child Count is "+tblLoadProductList.getChildCount(), Toast.LENGTH_SHORT).show();
 				// getting values from tablelayout and place to excel
 				Double totalOrderQty=0.0, totalNetAmount=0.0;
 				for(int i = 0; i<tlEditOL.getChildCount();i++){
 					String iRow,inStock,orderQty,productNames,amount,netAmount;
 					if(i==0){
 							row = sheet.createRow(i);
 							Log.d("Orderno, BeatName row values", String.valueOf(i));
 							cell = row.createCell((short)0);
 							cell.setCellValue("Order No");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)1);
 							//cell.setCellValue(theDate+"/"+iRow);
 							cell.setCellValue(tvEditOrderNo.getText().toString());
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)2);
 							cell.setCellValue("Date");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)3);
 							cell.setCellValue(theDate);
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)4);
 							cell.setCellValue("Beat Name");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)5);
 							cell.setCellValue(tvEditBeatName.getText().toString());
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 					}
 					if(i==1){
 							row = sheet.createRow(i);
 							Log.d("Table Header Row value", String.valueOf(i));
 							cell = row.createCell((short)0);
 							cell.setCellValue("S.No");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)1);
 							cell.setCellValue("Product Name");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)2);
 							cell.setCellValue("Stock Qty");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)3);
 							cell.setCellValue("Ordered Qty");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)4);
 							cell.setCellValue("Amount");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)5);
 							cell.setCellValue("Net Amount");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 						}
 					if(i>=2 &&  i < tlEditOL.getChildCount()-1){
 						Log.d("Product details rows", String.valueOf(i));
 						iRow=String.valueOf(i-1);
 						productNames = String.valueOf(((TextView)((TableRow)tlEditOL.getChildAt(i+1)).getChildAt(1)).getText());
 						inStock = String.valueOf(((TextView)((TableRow)tlEditOL.getChildAt(i+1)).getChildAt(2)).getText());
 						Log.d("productNames & inStock", productNames.toString() + " & "+inStock.toString());
 						Log.d("Test EditText",String.valueOf(((EditText)((TableRow)tlEditOL.getChildAt(i+1)).getChildAt(3)).getText().toString()));
 						orderQty = String.valueOf(((EditText)((TableRow)tlEditOL.getChildAt(i+1)).getChildAt(3)).getText().toString());
 						if(orderQty.length()==0){ 
 							orderQty="0";
 						}
 						amount = String.valueOf(((TextView)((TableRow)tlEditOL.getChildAt(i+1)).getChildAt(4)).getText());
 						if(amount.length()==0){
 							amount = "0";
 						}
 						netAmount = String.valueOf(((TextView)((TableRow)tlEditOL.getChildAt(i+1)).getChildAt(5)).getText());
 						if(netAmount.length()==0){
 							netAmount ="0";
 						}
 						totalOrderQty += Double.valueOf(orderQty);
 						totalNetAmount += Double.valueOf(netAmount);
 						row = sheet.createRow(i);
 						Log.d("Details rows", String.valueOf(i));
 						cell = row.createCell((short)0);
 						cell.setCellValue(Integer.valueOf(iRow));
 						// set border to cell
 						cellStyle = workbook.createCellStyle();
 						cellStyle.setBorderTop((short)1);
 						cellStyle.setBorderBottom((short)1);
 						cellStyle.setBorderLeft((short)1);
 						cellStyle.setBorderRight((short)1);
 						cell.setCellStyle(cellStyle);
 												
 						cell = row.createCell((short)1);
 						cell.setCellValue(productNames);
 						// set border to cell
 						cellStyle = workbook.createCellStyle();
 						cellStyle.setBorderTop((short)1);
 						cellStyle.setBorderBottom((short)1);
 						cellStyle.setBorderLeft((short)1);
 						cellStyle.setBorderRight((short)1);
 						cell.setCellStyle(cellStyle);
 						
 						cell = row.createCell((short)2);
 						cell.setCellValue(Double.valueOf(inStock));
 						// set border to cell
 						cellStyle = workbook.createCellStyle();
 						cellStyle.setBorderTop((short)1);
 						cellStyle.setBorderBottom((short)1);
 						cellStyle.setBorderLeft((short)1);
 						cellStyle.setBorderRight((short)1);
 						cell.setCellStyle(cellStyle);
 						
 						cell = row.createCell((short)3);
 						cell.setCellValue(Double.valueOf(orderQty));
 						// set border to cell
 						cellStyle = workbook.createCellStyle();
 						cellStyle.setBorderTop((short)1);
 						cellStyle.setBorderBottom((short)1);
 						cellStyle.setBorderLeft((short)1);
 						cellStyle.setBorderRight((short)1);
 						cell.setCellStyle(cellStyle);
 						
 						cell = row.createCell((short)4);
 						cell.setCellValue(Double.valueOf(amount));
 						// set border to cell
 						cellStyle = workbook.createCellStyle();
 						cellStyle.setBorderTop((short)1);
 						cellStyle.setBorderBottom((short)1);
 						cellStyle.setBorderLeft((short)1);
 						cellStyle.setBorderRight((short)1);
 						cell.setCellStyle(cellStyle);
 						
 						cell = row.createCell((short)5);
 						//formule_NetAmount = "SUM(D"+(i+1)+",E"+(i+1)+")";
 						//cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
						cell.setCellValue(Double.valueOf(dcf.format(netAmount)));
 						//cell.setCellFormula(formule_NetAmount);
 						// set border to cell
 						cellStyle = workbook.createCellStyle();
 						cellStyle.setBorderTop((short)1);
 						cellStyle.setBorderBottom((short)1);
 						cellStyle.setBorderLeft((short)1);
 						cellStyle.setBorderRight((short)1);
 						cell.setCellStyle(cellStyle);
 					
 						Log.d("List of products"+iRow, String.valueOf(iRow+"@"+productNames+"@"+inStock+"@"+orderQty+"@"+amount+"@"+netAmount));
 					}
 						
 						// create total qty and netamount row
 						if(i == tlEditOL.getChildCount()-1){
 							Log.d("create total qty and netamount row number", String.valueOf(tlEditOL.getChildCount()-1));
 							row = sheet.createRow(i);
 							cell = row.createCell((short)0);
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 													
 							cell = row.createCell((short)1);
 							cell.setCellValue("Total");
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)2);
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)3);
 							cell.setCellValue(totalOrderQty);
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)4);
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							
 							cell = row.createCell((short)5);
 							cell.setCellValue(dcf.format(totalNetAmount));
 							// set border to cell
 							cellStyle = workbook.createCellStyle();
 							cellStyle.setBorderTop((short)1);
 							cellStyle.setBorderBottom((short)1);
 							cellStyle.setBorderLeft((short)1);
 							cellStyle.setBorderRight((short)1);
 							cell.setCellStyle(cellStyle);
 							Log.d("Total ordered qty & net amount", String.valueOf(totalOrderQty)+"/"+String.valueOf(totalNetAmount));
 						}
 					
 				}
 				
 			}
 			is.close();
 			FileOutputStream out = new FileOutputStream(new File(filename));
 			workbook.write(out);
 			out.close();
 			
 			Toast.makeText(getApplicationContext(), sheetName+" is successfully updated", Toast.LENGTH_SHORT).show();
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			Log.d("File Not Found Exception", e.getMessage());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.d("IO Exception", e.getMessage());
 		}
 		
 	}
 	
 	private Double getTotalNetAmountAfterChangedOrder(){
 		Double Calc_TotalNetAmount = 0.0;
 		String strNetAmount = null;
 			tlEditOL = (TableLayout)findViewById(R.id.tblEditOrderList);
 			for(int i = 0;i<tlEditOL.getChildCount();i++){
 				if(i>=2 &&  i < tlEditOL.getChildCount()-1){
 					strNetAmount = String.valueOf(((TextView)((TableRow)tlEditOL.getChildAt(i+1)).getChildAt(5)).getText());
 					if(strNetAmount.length()==0){
 						strNetAmount ="0";
 					}
 					Calc_TotalNetAmount += Double.valueOf(strNetAmount);
 				}
 			}
 		return Calc_TotalNetAmount;
 	}
 }
