 package com.example.gkaakash;
 
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import com.example.gkaakash.R.layout;
 import com.gkaakash.controller.Account;
 import com.gkaakash.controller.Startup;
 import com.gkaakash.controller.Transaction;
 
 import android.animation.ArgbEvaluator;
 import android.animation.ObjectAnimator;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ActionBar.LayoutParams;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Layout;
 import android.text.SpannableString;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchVoucher extends Activity {
 	int textlength=0;
 	Context context = SearchVoucher.this;
 	AlertDialog dialog;
 	DecimalFormat mFormat;
 	private Transaction transaction;
 	static Integer client_id;
 	static ArrayList<ArrayList<String>> searchedVoucherGrid;
 	static ArrayList<String> searchedVoucherList;
 	TableLayout vouchertable;
 	TableRow tr;
 	TextView label;
 	static String financialFromDate;
 	static String financialToDate;
 	int rowid=0;
 	static String vouchertypeflag;
 	static ArrayList<String> value;
 	static String name,deleted_vouchercode;
 	static Boolean cloneflag=false,deletedflag=false;
 	String vouchercode;
 	LinearLayout.LayoutParams params;
 	static int searchVoucherBy = 2; // by date
 	protected Boolean deleteVoucher;
 	static String searchByNarration;
 	static String searchByRefNumber;
 	DecimalFormat formatter = new DecimalFormat("#,##,##,###.00");
 	String colValue;
 	int oneTouch = 1;
 	TableLayout floating_heading_table;
 	module m;
 	String[] ColumnNameList;
 	static String IPaddr;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.search_voucher);
 		IPaddr = MainActivity.IPaddr;
 		System.out.println("in createorg"+IPaddr);
 		client_id = Startup.getClient_id();
 		transaction = new Transaction(IPaddr);
 		m= new module();
 
 
 		//for two digit format date for dd and mm
 		mFormat= new DecimalFormat("00");
 		mFormat.setRoundingMode(RoundingMode.DOWN);
 		
 		
 		vouchertable = (TableLayout)findViewById(R.id.maintable);
 		floating_heading_table = (TableLayout) findViewById(R.id.floating_heading_table);
 		floating_heading_table.setVisibility(TableLayout.GONE);
 
 
 		financialFromDate =Startup.getfinancialFromDate(); 
 		financialToDate = Startup.getFinancialToDate();
 
 		TextView tvVFromdate = (TextView) findViewById( R.id.tvVFromdate );
 		TextView tvVTodate = (TextView) findViewById( R.id.tvVTodate );
 
 		tvVFromdate.setText("Financial from date: " +financialFromDate);
 		tvVTodate.setText("Financial to date: " +financialToDate);
 
 		System.out.println("VOUCHER TYPE"+vouchertypeflag);
 		vouchertypeflag = createVoucher.vouchertypeflag;
 
 
 
 
 
 		try {
 			setOnSearchButtonClick();
 			//floatingHeader();
 
 
 		} catch (Exception e) {
 			m.toastValidationMessage(context, "Please try again");
 		}
 	}
 
 	private void floatingHeader() {
 		 System.out.println("we are in floating function");
 		 
 		 vouchertable.setOnTouchListener(new OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
                 if (oneTouch == 1) {
                     floating_heading_table.setVisibility(TableLayout.VISIBLE);
 
                     // System.out.println("we are in if");
 
                     int rowcount = vouchertable.getChildCount();
                     View row = vouchertable.getChildAt(rowcount - 1);
 
                     final SpannableString rsSymbol = new SpannableString(
                             SearchVoucher.this.getText(R.string.Rs));
                     /** Create a TableRow dynamically **/
                     String[] ColumnNameList = new String[] { "V. No.","Reference No","Date","Voucher Type","Account Name","Particular",rsSymbol+"Amount","Narration"};
 
 
                     tr = new TableRow(SearchVoucher.this);
 
                     for (int k = 0; k < ColumnNameList.length; k++) {
                         /** Creating a TextView to add to the row **/
                         addRow(ColumnNameList[k], k);
                         label.setBackgroundColor(Color.parseColor("#348017"));
                         label.setGravity(Gravity.CENTER);
 
                         LinearLayout l = (LinearLayout) ((ViewGroup) row)
                                 .getChildAt(k);
                         label.setWidth(l.getWidth());
                         tr.setClickable(false);
                         params.height = LayoutParams.WRAP_CONTENT;
                         // System.out.println("size is"+l.getWidth());
                     }
 
                     // Add the TableRow to the TableLayout
                     floating_heading_table.addView(tr,
                             new TableLayout.LayoutParams(
                                     LayoutParams.FILL_PARENT,
                                     LayoutParams.WRAP_CONTENT));
                     // ledgertable.removeViewAt(0);
                     vouchertable.getChildAt(0).setVisibility(View.INVISIBLE);
 
                     View firstrow = vouchertable.getChildAt(0);
                     for (int k = 0; k < ColumnNameList.length; k++) {
                         LinearLayout l = (LinearLayout) ((ViewGroup) firstrow)
                                 .getChildAt(k);
                         TextView tv = (TextView) l.getChildAt(0);
                         tv.setHeight(0);
 
                         l.getLayoutParams().height = 0;
                     }
                     // ledgertable.getChildAt(0).setLayoutParams(new
                     // TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                     // 0));
 
                 }
                 oneTouch++;
                 
                 /*
                  * make table rows clikable now
                  */
                 int count = vouchertable.getChildCount();
                 for (int i = 0; i < count; i++) {
                     TableRow row = (TableRow) vouchertable.getChildAt(i);
                     row.setClickable(true);    
                 }
                 return false;
 			}
 		});
 	       
 	}
 
 	private void setOnSearchButtonClick() {
 		Button btnSearchVoucher = (Button)findViewById(R.id.btnSearchVoucher);
 		btnSearchVoucher.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 				View layout = inflater.inflate(R.layout.search_voucher_by, (ViewGroup) findViewById(R.id.timeInterval));
 				//Building DatepPcker dialog
 				AlertDialog.Builder builder = new AlertDialog.Builder(context);
 				builder.setView(layout);
 				builder.setTitle("Search voucher by,");
 
 
 				String dateParts[] = financialFromDate.split("-");
 				String setfromday  = dateParts[0];
 				String setfrommonth = dateParts[1];
 				String setfromyear = dateParts[2];
 
 
 				String dateParts1[] = financialToDate.split("-");
 				String settoday  = dateParts1[0];
 				String settomonth = dateParts1[1];
 				String settoyear = dateParts1[2];
 
 				DatePicker SearchVoucherFromdate = (DatePicker) layout.findViewById(R.id.dpSearchVoucherFromdate);
 				SearchVoucherFromdate.init(Integer.parseInt(setfromyear),(Integer.parseInt(setfrommonth)-1),Integer.parseInt(setfromday), null);
 
 				DatePicker SearchVoucherTodate = (DatePicker) layout.findViewById(R.id.dpSearchVoucherTodate);
 				SearchVoucherTodate.init(Integer.parseInt(settoyear),(Integer.parseInt(settomonth)-1),Integer.parseInt(settoday), null);
 
 				final EditText etVoucherCode = (EditText)layout.findViewById(R.id.searchByVCode);
 				etVoucherCode.setVisibility(EditText.GONE);
 
 				final EditText etNarration = (EditText)layout.findViewById(R.id.searchByNarration);
 				etNarration.setVisibility(EditText.GONE);
 
 				final LinearLayout timeInterval = (LinearLayout)layout.findViewById(R.id.timeInterval);
 				timeInterval.setVisibility(LinearLayout.GONE);
 
 				final Spinner searchBy = (Spinner) layout.findViewById(R.id.sSearchVoucherBy);
 				searchBy.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 					@Override
 					public void onItemSelected(AdapterView<?> parent, View v, int position,long id) {
 						if(position == 0){
 							etNarration.setVisibility(EditText.GONE);
 							timeInterval.setVisibility(LinearLayout.GONE);
 							etVoucherCode.setVisibility(EditText.VISIBLE);
 						}
 						if(position == 1){
 							etVoucherCode.setVisibility(EditText.GONE);
 							etNarration.setVisibility(EditText.GONE);
 							timeInterval.setVisibility(LinearLayout.VISIBLE);
 						}
 						if(position == 2){
 							etVoucherCode.setVisibility(EditText.GONE);
 							timeInterval.setVisibility(LinearLayout.GONE);
 							etNarration.setVisibility(EditText.VISIBLE);
 						}
 
 					}
 
 					@Override
 					public void onNothingSelected(AdapterView<?> arg0) {
 						// TODO Auto-generated method stub
 
 					}
 				});
 
 				builder.setPositiveButton("View",new  DialogInterface.OnClickListener(){
 
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 						int pos = searchBy.getSelectedItemPosition();
 
 						if(pos == 0){
 							searchByRefNumber = etVoucherCode.getText().toString();
 							if(searchByRefNumber.length() < 1){
 								m.toastValidationMessage(context,"Please enter voucher reference number");
 							}
 							else{
 								searchVoucherBy = 1; //by reference no
 								Object[] params = new Object[]{1,searchByRefNumber,financialFromDate,financialToDate,""};
 								getallvouchers(params);
 
 							}
 						}
 						else if(pos == 1){
 							final   DatePicker dpSearchVoucherFromdate = (DatePicker) dialog.findViewById(R.id.dpSearchVoucherFromdate);
 							int SearchVoucherFromDay = dpSearchVoucherFromdate.getDayOfMonth();
 							int SearchVoucherFromMonth = dpSearchVoucherFromdate.getMonth();
 							int SearchVoucherFromYear = dpSearchVoucherFromdate.getYear();
 
 							String SearchVoucherFromdate = mFormat.format(Double.valueOf(SearchVoucherFromDay))+ "-" 
 									+(mFormat.format(Double.valueOf(Integer.parseInt((mFormat.format(Double.valueOf(SearchVoucherFromMonth))))+ 1))) + "-" 
 									+ SearchVoucherFromYear;
 
 							final   DatePicker dpSearchVoucherTodate = (DatePicker) dialog.findViewById(R.id.dpSearchVoucherTodate);
 							int SearchVoucherToDay = dpSearchVoucherTodate.getDayOfMonth();
 							int SearchVoucherToMonth = dpSearchVoucherTodate.getMonth();
 							int SearchVoucherToYear = dpSearchVoucherTodate.getYear();
 
 							String SearchVoucherTodate = mFormat.format(Double.valueOf(SearchVoucherToDay))+ "-" 
 									+(mFormat.format(Double.valueOf(Integer.parseInt((mFormat.format(Double.valueOf(SearchVoucherToMonth))))+ 1))) + "-" 
 									+ SearchVoucherToYear;
 
 							try {
 								SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
 								Date date1 = sdf.parse(financialFromDate);
 								Date date2 = sdf.parse(financialToDate);
 								Date date3 = sdf.parse(SearchVoucherFromdate);
 								Date date4 = sdf.parse(SearchVoucherTodate);
 								/*
 					        	System.out.println("all dates are...........");
 					        	System.out.println(financialFromDate+"---"+financialToDate+"---"+SearchVoucherFromdate+"---"+SearchVoucherTodate);
 								 */
 								Calendar cal1 = Calendar.getInstance(); //financial from date
 								Calendar cal2 = Calendar.getInstance(); //financial to date
 								Calendar cal3 = Calendar.getInstance(); //from date
 								Calendar cal4 = Calendar.getInstance(); //to date
 								cal1.setTime(date1);
 								cal2.setTime(date2);
 								cal3.setTime(date3);
 								cal4.setTime(date4);  
 
 								if(((cal3.after(cal1)&&(cal3.before(cal2))) || (cal3.equals(cal1) || (cal3.equals(cal2)))) 
 										&& ((cal4.after(cal1) && (cal4.before(cal2))) || (cal4.equals(cal2)) || (cal4.equals(cal1)))){
 									searchVoucherBy = 2; // by date
 									Object[] params = new Object[]{2,"",SearchVoucherFromdate,SearchVoucherTodate,""};
 									getallvouchers(params);
 								}
 								else{
 									m.toastValidationMessage(context,"Please enter proper date");
 								}
 							} catch (Exception e) {
 								// TODO: handle exception
 							}
 
 						}
 						else if(pos == 2){
 							searchByNarration = etNarration.getText().toString();
 							if(searchByNarration.length() < 1){
 								m.toastValidationMessage(context,"Please enter narration");
 							}
 							else{
 								searchVoucherBy = 3; //by narration
 								Object[] params = new Object[]{3,"",financialFromDate,financialToDate,searchByNarration};
 								getallvouchers(params);
 							}
 						}
 
 					}
 				});
 
 				builder.setNegativeButton("Cancel",new  DialogInterface.OnClickListener(){
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 					}	
 				});
 				dialog=builder.create();
 				dialog.show();
 				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 				//customizing the width and location of the dialog on screen 
 				lp.copyFrom(dialog.getWindow().getAttributes());
 				lp.width = 700;
 				dialog.getWindow().setAttributes(lp);
 
 			}
 		});
 	}
 
 
 	public void addTable() {
 
 		if(searchedVoucherGrid.size() > 0){
             addHeader();
         }
 
 
 
 		/** Create a TableRow dynamically **/
 		for(int i=0;i<searchedVoucherGrid.size();i++){
 			ArrayList<String> columnValue = new ArrayList<String>();
 			columnValue.addAll(searchedVoucherGrid.get(i));
 			tr = new TableRow(SearchVoucher.this);
 
 			for(int j=0;j<columnValue.size();j++){
 				/** Creating a TextView to add to the row **/
 				addRow(columnValue.get(j),i); 
 				if ((i + 1) % 2 == 0)
 					label.setBackgroundColor(Color.parseColor("#2f2f2f"));
 				else
 					label.setBackgroundColor(Color.parseColor("#085e6b")); //blue theme
 				/*
 				 * set center aligned gravity for amount and for others set center gravity
 				 */
 				if(j==6){
 
 					label.setGravity(Gravity.CENTER|Gravity.RIGHT);
 
 					if(columnValue.get(j).length() > 0){
 
 						colValue=columnValue.get(j);
 						if(!"".equals(colValue)){
 							//System.out.println("m in ");
 							if(!"0.00".equals(colValue)){
 								// for checking multiple \n and pattern matching
 								Pattern pattern = Pattern.compile("\\n");
 								Matcher matcher = pattern.matcher(colValue);
 								boolean found = matcher.find();
 								//System.out.println("value:"+found);
 								if(found==false){
 									double amount = Double.parseDouble(colValue);	
 									label.setText(formatter.format(amount));
 								}else {
 									label.setText(colValue);
 								}
 
 							}else {
 								label.setText(colValue);
 							}
 
 						}
 
 					}
 
 				}
 				else{
 					label.setGravity(Gravity.CENTER);
 				}
 				// tr.setId(i);
 
 
 
 			}
 
 			// Add the TableRow to the TableLayout
 			vouchertable.addView(tr, new TableLayout.LayoutParams(
 					LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 		}
 	}
 
 
 	/*
 	 * add column heads to the table
 	 */
 	public void addHeader() {
 
 		/** Create a TableRow dynamically **/
 		final SpannableString rsSymbol = new SpannableString(SearchVoucher.this.getText(R.string.Rs)); 
 		ColumnNameList = new String[] { "V. No.","Reference No","Date","Voucher Type","Account Name","Particular",rsSymbol+"Amount","Narration"};
 
 		tr = new TableRow(SearchVoucher.this);
 
 		for(int k=0;k<ColumnNameList.length;k++){
 			/** Creating a TextView to add to the row **/
 
 			addRow(ColumnNameList[k],k);
 			params.height=LayoutParams.WRAP_CONTENT;
 			label.setTextColor(Color.parseColor("#085e6b")); //blue theme
 			label.setGravity(Gravity.CENTER);
 			tr.setClickable(false);
 		}
 
 		// Add the TableRow to the TableLayout
 		vouchertable.addView(tr, new TableLayout.LayoutParams(
 				LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 
 	}
 
 
 	/*
 	 * this function add the value to the row
 	 */
 	public void addRow(String string,final int i) {
 		tr.setClickable(true);
 
 		tr.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// Toast.makeText(SearchVoucher.this, tr.getId(), Toast.LENGTH_SHORT).show(); 
 				//fade the row color(black/gray to orange) when clicked
 				View row = vouchertable.getChildAt(i+1);
 				
 				for (int j = 0; j < ColumnNameList.length; j++) {
 					LinearLayout l = (LinearLayout) ((ViewGroup) row)
 							.getChildAt(j);
 					TextView t = (TextView) l.getChildAt(0);
					ObjectAnimator colorFade = ObjectAnimator.ofObject(t, "backgroundColor", new ArgbEvaluator(), Color.parseColor("#FBB117"), Color.BLACK);
 					colorFade.setDuration(100);
 					colorFade.start();
 				}
 
 
 				try {
 					final CharSequence[] items = { "Edit voucher", "Copy voucher","Delete voucher"};
 					//creating a dialog box for popup
 					AlertDialog.Builder builder = new AlertDialog.Builder(SearchVoucher.this);
 					//setting title
 					builder.setTitle("Edit/Delete Voucher");
 					//adding items
 					builder.setItems(items, new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog,
 								int pos) {
 							if(pos == 0){
 								MainActivity.nameflag=true;
 								name="Edit voucher";
 								//Toast.makeText(context,"name"+name,Toast.LENGTH_SHORT).show();
 								cloneflag=false;
 
 								//System.out.println("in addrow"+i); 
 								value=searchedVoucherGrid.get(i);
 								//Toast.makeText(SearchVoucher.this,"result"+value, Toast.LENGTH_SHORT).show();
 
 								MainActivity.searchFlag=true;
 								Intent intent = new Intent(context, transaction_tab.class);
 								// To pass on the value to the next page
 								startActivity(intent);
 							}
 							if(pos==1){
 								MainActivity.nameflag=true;
 								cloneflag=true;
 								name="Copy voucher";
 								//Toast.makeText(context,"name"+name,Toast.LENGTH_SHORT).show();
 								//System.out.println("in addrow"+i); 
 								value=searchedVoucherGrid.get(i);
 								//Toast.makeText(SearchVoucher.this,"result"+value, Toast.LENGTH_SHORT).show(); 
 								MainActivity.searchFlag=true;
 								Intent intent = new Intent(context, transaction_tab.class);
 								// To pass on the value to the next page
 								startActivity(intent);
 								//Toast.makeText(context,"name"+name,Toast.LENGTH_SHORT).show();
 							}
 
 							if(pos==2){
 								AlertDialog.Builder builder = new AlertDialog.Builder(SearchVoucher.this);
 								builder.setMessage("Are you sure you want to detete the Voucher?")
 								.setCancelable(false)
 								.setPositiveButton("Yes",
 										new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int id) {
 										value=searchedVoucherGrid.get(i);
 										vouchercode=value.get(0);
 										Object[] params = new Object[]{vouchercode};
 										deleteVoucher = (Boolean) transaction.deleteVoucher(params,client_id);
 										if(deleteVoucher.equals(true))
 										{
 											deletedflag = true;
 											deleted_vouchercode = vouchercode;
 										}
 										Object[] allvouchersparams = new Object[]{2,"",financialFromDate,financialToDate,""};
 										getallvouchers(allvouchersparams);
 
 										m.toastValidationMessage(context,"Voucher deleted successfully");
 									}
 								})
 								.setNegativeButton("No", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int id) {
 										dialog.cancel();
 									}
 								});
 								AlertDialog alert = builder.create();
 								alert.show();
 							}
 						}				        	
 					});
 					dialog=builder.create();
 					((Dialog) dialog).show();
 					WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 					//customizing the width and location of the dialog on screen 
 					lp.copyFrom(dialog.getWindow().getAttributes());
 					lp.height = 600;
 					lp.width = 400;
 					dialog.getWindow().setAttributes(lp);		
 
 				} catch (Exception e) {
 					//System.out.println(e);
 				}
 			}
 		});
 
 		label = new TextView(SearchVoucher.this);
 		label.setText(string);
 		label.setTextSize(18);
 		label.setTextSize(15);
 		label.setTextColor(Color.WHITE);
 		label.setGravity(Gravity.CENTER_VERTICAL);
 		label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 				LayoutParams.WRAP_CONTENT));
 		label.setPadding(2, 2, 2, 2);
 		LinearLayout Ll = new LinearLayout(SearchVoucher.this);
 		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
 				35);
 		params.setMargins(1, 1, 1, 1);
 		//Ll.setPadding(10, 5, 5, 5);
 		Ll.addView(label,params);
 		tr.addView((View)Ll); // Adding textView to tablerow.
 	}
 
 
 	public void getallvouchers(Object[] params){
 		vouchertypeflag = createVoucher.vouchertypeflag;
 		Object[] searchedVoucher = (Object[])transaction.searchVoucher(params,client_id);
 		searchedVoucherGrid = new ArrayList<ArrayList<String>>();
 		for(Object voucherRow : searchedVoucher){
 			Object[] v = (Object[]) voucherRow;
 			searchedVoucherList = new ArrayList<String>();
 			for(int i=0;i<v.length;i++){
 				if(((String) v[3].toString()).equalsIgnoreCase(vouchertypeflag)){
 					searchedVoucherList.add((String) v[i].toString());
 				}
 			}
 			if(searchedVoucherList.size() != 0){
 				searchedVoucherGrid.add(searchedVoucherList);
 			}
 			
 		}
 		vouchertable.removeAllViews();
         System.out.println("grid in resume"+searchedVoucherGrid);
         if(searchedVoucherGrid.size() > 0){
                 addTable();
         }
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 * to execute code when tab is changed because 
 	 * when the tab is clicked onResume is called for that activity
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		System.out.println("ON this RESUME");
         if(searchVoucherBy == 1){ // by reference number
                 Object[] params = new Object[]{1,searchByRefNumber,financialFromDate,financialToDate,""};
                 getallvouchers(params);
         }
         else if(searchVoucherBy == 2){ // by date
                 Object[] params = new Object[]{2,"",financialFromDate,financialToDate,""};
                 getallvouchers(params);
         }
         else if(searchVoucherBy == 3){ // narration
                 Object[] params = new Object[]{3,"",financialFromDate,financialToDate,searchByNarration};
                 getallvouchers(params);
         }
 
         /*
          * hide header row from voucher table if floating header is present
          */
 //        if(searchedVoucherGrid.size() > 0){
 //        if(oneTouch > 1){
 //                View firstrow = vouchertable.getChildAt(0);
 //                for (int k = 0; k < 8; k++) {
 //                        LinearLayout l = (LinearLayout) ((ViewGroup) firstrow)
 //                                        .getChildAt(k);
 //                        TextView tv = (TextView) l.getChildAt(0);
 //                        tv.setHeight(0);
 //
 //                        l.getLayoutParams().height = 0;
 //                }
 //        }
 //        }
 
 	}
 
 
 	
 
 
 	public void onBackPressed() {
 		MainActivity.nameflag=false;
 		Intent intent = new Intent(getApplicationContext(), menu.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 	}
 }
