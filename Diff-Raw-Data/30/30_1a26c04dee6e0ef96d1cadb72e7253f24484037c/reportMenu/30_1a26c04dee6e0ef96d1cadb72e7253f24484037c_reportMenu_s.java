 package com.example.gkaakash;
 
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.w3c.dom.Text;
 
 import com.gkaakash.controller.Account;
 import com.gkaakash.controller.Organisation;
 import com.gkaakash.controller.Startup;
 import com.gkaakash.controller.Transaction;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class reportMenu extends ListActivity{
 	//adding report list items
 	
 	final Context context = this;
 	AlertDialog dialog;
 	DecimalFormat mFormat;
 	static String financialFromDate;
 	static String financialToDate;
 	static String givenfromDateString;
 	static String givenToDateString;
 	static Integer client_id;
 	private Account account;
 	private Organisation organisation;
 	static String selectedAccount;
 	static String selectedProject;
 	static String fromday, frommonth, fromyear, today, tomonth, toyear; 
 	static boolean validateDateFlag;
 	static String trialbalancetype;
 	String[] reportType;
 	static String orgtype;
 	boolean reportmenuflag;
 	String orgname;
 	static String reportTypeFlag;
 	static String balancetype;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		account = new Account();
 		organisation = new Organisation();
 		
        	client_id= Startup.getClient_id();
        	
        	
        	/*
        	 * get org type from create page or select org page
        	 */
        	if(reportmenuflag==true){
             orgtype=createOrg.orgTypeFlag;
         }
        	else {
 	         orgname=selectOrg.selectedOrgName;
 	         System.out.println("org name in selett "+orgname);
 	         Object[] params = new Object[]{orgname};
 	         orgtype = (String) organisation.getorgTypeByname(params, client_id);;
 	         System.out.println("org type in select"+orgtype);
        	}
         if("NGO".equals(orgtype))
         {
            reportType = new String[] { "Ledger","Trial balance","Project statement",
                 "Cash flow","Balance sheet","Income and Expenditure" };
         }
         else{
            reportType = new String[] { "Ledger","Trial balance","Project statement",
                 "Cash flow","Balance sheet","Profit and Loss account" };
         }
        	
        	//get financial from and to date, split and store day, month and year in seperate variable
        	financialFromDate =Startup.getfinancialFromDate();  	   	
 	   	String dateParts[] = financialFromDate.split("-");
 	   	fromday  = dateParts[0];
 	   	frommonth = dateParts[1];
 	   	fromyear = dateParts[2];
 	   	
 	   	financialToDate = Startup.getFinancialToDate();
 	   	String dateParts1[] = financialToDate.split("-");
 	   	today  = dateParts1[0];
 	   	tomonth = dateParts1[1];
 	   	toyear = dateParts1[2];
 	   	
 		
 		//calling report.xml page
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.report,reportType));
 		
 		//getting the list view and setting background
 		final ListView listView = getListView();
 		listView.setTextFilterEnabled(true);
 		listView.setBackgroundColor(R.drawable.dark_gray_background);
 		listView.setCacheColorHint(Color.TRANSPARENT);
 		
 		//for two digit format date for dd and mm
 		mFormat= new DecimalFormat("00");
 		mFormat.setRoundingMode(RoundingMode.DOWN);
 		
 		//when report list items are clicked, code for respective actions goes here ...
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				
 				//for "Ledger"
 				if(position == 0)
 				{
 					
 					//call the allAccountNames method to get all account names
 					Object[] accountnames = (Object[]) account.getAllAccountNames(client_id);
 					// create new array list of type String to add account names
 					List<String> accountnamelist = new ArrayList<String>();
 					for(Object an : accountnames)
 					{	
 						accountnamelist.add((String) an); 
 					}	
 
 					
 					if(accountnamelist.size() <= 0){
 						String message = "Ledger cannot be displayed, Please create account!";
 						toastValidationMessage(message);
 						}
 					else{
 						//call the getAllProjects method to get all projects
 						Object[] projectnames = (Object[]) organisation.getAllProjects(client_id);
 						// create new array list of type String to add projectnames
 						List<String> projectnamelist = new ArrayList<String>();
 						projectnamelist.add("No Project");
 						for(Object pn : projectnames)
 						{	
 							Object[] p = (Object[]) pn;
 							projectnamelist.add((String) p[1]); //p[0] is project code & p[1] is projectname
 						}	
 						
 						LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 						View layout = inflater.inflate(R.layout.ledger, (ViewGroup) findViewById(R.id.layout_root));
 						//Building DatepPcker dialog
 						AlertDialog.Builder builder = new AlertDialog.Builder(context);
 						builder.setView(layout);
 						builder.setTitle("Ledger");
 						
 						//populate all account names in accountname dropdown(spinner)
 						final Spinner accountNames = (Spinner)layout.findViewById(R.id.sAccountNameinLedger);
 						ArrayAdapter<String> da = new ArrayAdapter<String>(reportMenu.this, 
 													android.R.layout.simple_spinner_item,accountnamelist);
 				  	   	da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				  	   	accountNames.setAdapter(da);
 				  	   	
 				  	   	
 				  	   	//populate all project names in project dropdown(spinner)
 						final Spinner projectNames = (Spinner)layout.findViewById(R.id.sLedgerProject);
 						ArrayAdapter<String> da1 = new ArrayAdapter<String>(reportMenu.this, 
 													android.R.layout.simple_spinner_item,projectnamelist);
 				  	   	da1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				  	   	projectNames.setAdapter(da1);
 				  	   	
 				  	   	
 					   	
 				  	   	CheckBox cbNarration = (CheckBox)layout.findViewById(R.id.cbNarration);
 				  	   	cbNarration.setVisibility(CheckBox.GONE);
 				  	   	TextView tvcbNarration= (TextView)layout.findViewById(R.id.tvcbNarration);
 				  	   	tvcbNarration.setVisibility(TextView.GONE);
 					   	
 					   	final DatePicker LedgerFromdate = (DatePicker) layout.findViewById(R.id.dpsetLedgerFromdate);
 					   	LedgerFromdate.init(Integer.parseInt(fromyear),(Integer.parseInt(frommonth)-1),Integer.parseInt(fromday), null);
 					   	
 					   	final DatePicker LedgerT0date = (DatePicker) layout.findViewById(R.id.dpsetLedgerT0date);
 					   	LedgerT0date.init(Integer.parseInt(toyear),(Integer.parseInt(tomonth)-1),Integer.parseInt(today), null);
 					   	
 						builder.setPositiveButton("View",new  DialogInterface.OnClickListener(){
 
 							@Override
 							public void onClick(DialogInterface arg0, int arg1) {
 								
 								selectedAccount = accountNames.getSelectedItem().toString();
 								selectedProject = projectNames.getSelectedItem().toString();
 								
 								validateDate(LedgerFromdate, LedgerT0date, "validatebothFromToDate");
 								
 								if(validateDateFlag){
 									Intent intent = new Intent(context, ledger.class);
 									// To pass on the value to the next page
 									startActivity(intent);
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
 					
 					
 				}
 				//trial balance
 				if(position == 1)
 				{
 					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 					View layout = inflater.inflate(R.layout.trial_balance, (ViewGroup) findViewById(R.id.layout_root));
 					//Building DatepPcker dialog
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 					builder.setView(layout);
					builder.setTitle("Trial balance");	
 					
 					
 					final DatePicker trialtodate = (DatePicker) layout.findViewById(R.id.dpTrialsetT0date);
 					trialtodate.init(Integer.parseInt(toyear),(Integer.parseInt(tomonth)-1),Integer.parseInt(today), null);
 					
 					final Spinner strialBalanceType = (Spinner)layout.findViewById(R.id.strialBalanceType);
 					
 					builder.setPositiveButton("View",new  DialogInterface.OnClickListener(){
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							
 							trialbalancetype=strialBalanceType.getSelectedItem().toString();
 							
 							validateDate(null, trialtodate, null);
 							
 						   	if(validateDateFlag){
 								Intent intent = new Intent(context, trialBalance.class);
 								// To pass on the value to the next page
 								startActivity(intent);
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
 				}
 				//project statement
 				if(position == 2)
 				{
 					
 					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 					View layout = inflater.inflate(R.layout.project_statement, (ViewGroup) findViewById(R.id.layout_root));
 					//Building DatepPcker dialog
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 					builder.setView(layout);
 					builder.setTitle("Project Statement");
 					
 					
 					final DatePicker dpProjectStatementSetT0date = (DatePicker) layout.findViewById(R.id.dpProjectStatementSetT0date);
 					dpProjectStatementSetT0date.init(Integer.parseInt(toyear),(Integer.parseInt(tomonth)-1),Integer.parseInt(today), null);
 					
 					//call the getAllProjects method to get all projects
 					Object[] projectnames = (Object[]) organisation.getAllProjects(client_id);
 					// create new array list of type String to add projectnames
 					List<String> projectnamelist = new ArrayList<String>();
 					projectnamelist.add("No Project");
 					for(Object pn : projectnames)
 					{	
 						Object[] p = (Object[]) pn;
 						projectnamelist.add((String) p[1]); //p[0] is project code & p[1] is projectname
 					}	
 					
 					//populate all project names in project dropdown(spinner)
 					final Spinner projectNames = (Spinner)layout.findViewById(R.id.sProject);
 					ArrayAdapter<String> da1 = new ArrayAdapter<String>(reportMenu.this, 
 												android.R.layout.simple_spinner_item,projectnamelist);
 			  	   	da1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			  	   	projectNames.setAdapter(da1);
 					
 					builder.setPositiveButton("Set",new  DialogInterface.OnClickListener(){
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							selectedProject = projectNames.getSelectedItem().toString();
 							
 							validateDate(null, dpProjectStatementSetT0date, null);
 							
 						   	if(validateDateFlag){
 								Intent intent = new Intent(context, projectStatement.class);
 								// To pass on the value to the next page
 								startActivity(intent);
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
 	        		
 				}
 				if(position == 3)
 				{
 					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 					View layout = inflater.inflate(R.layout.cash_flow, (ViewGroup) findViewById(R.id.layout_root));
 					//Building DatepPcker dialog
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 					builder.setView(layout);
 					builder.setTitle("Cash Flow");
 					
 					final DatePicker CashFlowFromdate = (DatePicker) layout.findViewById(R.id.dpsetCashFlowFromdate);
 					CashFlowFromdate.init(Integer.parseInt(fromyear),(Integer.parseInt(frommonth)-1),Integer.parseInt(fromday), null);
 				   	
 				   	final DatePicker CashFlowT0date = (DatePicker) layout.findViewById(R.id.dpsetCashFlowT0date);
 				   	CashFlowT0date.init(Integer.parseInt(toyear),(Integer.parseInt(tomonth)-1),Integer.parseInt(today), null);
 					
 					 builder.setPositiveButton("View",new  DialogInterface.OnClickListener(){
 		
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							
 							validateDate(CashFlowFromdate, CashFlowT0date, "validatebothFromToDate");
 							
 							if(validateDateFlag){
 								Intent intent = new Intent(context, cashFlow.class);
 								// To pass on the value to the next page
 								startActivity(intent);
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
 						lp.width = 600;
 						dialog.getWindow().setAttributes(lp);
 					
 				}
 				if(position == 4)
 				{
 					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 					View layout = inflater.inflate(R.layout.balance_sheet, (ViewGroup) findViewById(R.id.layout_root));
 					final DatePicker dpBalanceSheetsetT0date = (DatePicker) layout.findViewById(R.id.dpBalanceSheetsetT0date);
 					dpBalanceSheetsetT0date.init(Integer.parseInt(toyear),(Integer.parseInt(tomonth)-1),Integer.parseInt(today), null);
 					
 					final Spinner sbalanceSheetType = (Spinner)layout.findViewById(R.id.sbalanceSheetType);
 					
 					TextView tvbalanceSheetType = (TextView)layout.findViewById(R.id.tvbalanceSheetType);
 					
 					//Building DatepPcker dialog
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 					builder.setView(layout);
 					builder.setTitle("Balance Sheet");
 					builder.setPositiveButton("View",new  DialogInterface.OnClickListener(){
 						
 					
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 
 							validateDate(null, dpBalanceSheetsetT0date, null);
 							
 						   	if(validateDateFlag){
 								balancetype=sbalanceSheetType.getSelectedItem().toString();
 								Intent intent = new Intent(context, balanceSheet.class);
 								// To pass on the value to the next page
 								startActivity(intent);
 						   	}
 						}
 						
 					});
 					
 					builder.setNegativeButton("Cancel",new  DialogInterface.OnClickListener(){
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 						}
 						
 					});
 					dialog=builder.create();
 	        		dialog.show();
 	        		
 				}
 				if(position == 5)
 				{
 				    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 					View layout = inflater.inflate(R.layout.income_expenditure, (ViewGroup) findViewById(R.id.layout_root));
 					//Building DatepPcker dialog
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 					builder.setView(layout);
 					if(orgtype.equalsIgnoreCase("NGO")){
 						builder.setTitle("Income and Expenditure");
 						reportTypeFlag = "Income and Expenditure";
 					}
 					else{
 						builder.setTitle("Profit and Loss");
 						reportTypeFlag = "Profit and Loss";
 					}
 					
 					
 					final DatePicker dpIEPLT0date = (DatePicker) layout.findViewById(R.id.dpIEPLSetT0date);
 					dpIEPLT0date.init(Integer.parseInt(toyear),(Integer.parseInt(tomonth)-1),Integer.parseInt(today), null);
 					
 					builder.setPositiveButton("Set",new  DialogInterface.OnClickListener(){
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							
 							validateDate(null, dpIEPLT0date, null);
 							
 						   	if(validateDateFlag){
 								Intent intent = new Intent(context, incomeExpenditure.class);
 								// To pass on the value to the next page
 								startActivity(intent);
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
 	        	//*/
 				}
 			} 
 		});
 	}
 	
 	
 	public boolean validateDate(DatePicker fromdate, DatePicker todate, String flag) {
 		try {
 			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
 			Date date1 = sdf.parse(financialFromDate);
 	    	Date date2 = sdf.parse(financialToDate);
 			
 	    	Calendar cal1 = Calendar.getInstance(); //financial from date
 	    	Calendar cal2 = Calendar.getInstance(); //financial to date
 	    	Calendar cal3 = Calendar.getInstance(); //from date
 	    	Calendar cal4 = Calendar.getInstance(); //to date
 	    	
 	    	cal1.setTime(date1);
 	    	cal2.setTime(date2);
 	    	
 			if("validatebothFromToDate".equals(flag)){
 				int FromDay = fromdate.getDayOfMonth();
 			   	int FromMonth = fromdate.getMonth();
 			   	int FromYear = fromdate.getYear();
 			   	
 			   	givenfromDateString = mFormat.format(Double.valueOf(FromDay))+ "-" 
 			   	+(mFormat.format(Double.valueOf(Integer.parseInt((mFormat.format(Double.valueOf(FromMonth))))+ 1))) + "-" 
 			   	+ FromYear;
 			   	
 			   	Date date3 = sdf.parse(givenfromDateString);
 			   	cal3.setTime(date3);
 			}
 			
 			int T0Day = todate.getDayOfMonth();
 		   	int T0Month = todate.getMonth();
 		   	int T0Year = todate.getYear();
 		   	
 		   	givenToDateString = mFormat.format(Double.valueOf(T0Day))+ "-" 
 		   	+(mFormat.format(Double.valueOf(Integer.parseInt((mFormat.format(Double.valueOf(T0Month))))+ 1))) + "-" 
 		   	+ T0Year;
 		   	
 		   	Date date4 = sdf.parse(givenToDateString);
 		   	cal4.setTime(date4);  
 			
 	    	//System.out.println("all dates are...........");
 	    	//System.out.println(financialFromDate+"---"+financialToDate+"---"+givenfromDateString+"---"+givenToDateString);
 	    	
 	    	if("validatebothFromToDate".equals(flag)){
 	    		if(((cal3.after(cal1)&&(cal3.before(cal2))) || (cal3.equals(cal1) || (cal3.equals(cal2)))) 
 	        			&& ((cal4.after(cal1) && (cal4.before(cal2))) || (cal4.equals(cal2)) || (cal4.equals(cal1)))){
 	        		
 	        		validateDateFlag = true;
 	        	}
 	        	else{
 	        		String message = "Please enter proper date";
 	        		toastValidationMessage(message);
 	        		validateDateFlag = false;
 	        	}
 	    	}
 	    	else {
 	    		if((cal4.after(cal1) && cal4.before(cal2)) || cal4.equals(cal1) || cal4.equals(cal2) ){
 					
 	    			validateDateFlag = true;
 	        	}
 	        	else{
 	        		String message = "Please enter proper date";
 	        		toastValidationMessage(message);
 	        		validateDateFlag = false;
 	        	}
 	    	}
     	
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		return validateDateFlag;
 		
 	} 
 	
 	
 	public void toastValidationMessage(String message) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setMessage(message)
                 .setCancelable(false)
                 .setPositiveButton("Ok",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                             	
                             }
                         });
                 
         AlertDialog alert = builder.create();
         alert.show();
 		
 	} 
 }
