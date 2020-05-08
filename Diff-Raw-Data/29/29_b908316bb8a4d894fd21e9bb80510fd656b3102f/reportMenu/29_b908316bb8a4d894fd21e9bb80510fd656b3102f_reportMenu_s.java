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
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class reportMenu extends Activity{
 	//adding report list items
 	
 	Context context = this;
 	static AlertDialog dialog;
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
 	static boolean cheched = false;
 	static String fromday, frommonth, fromyear, today, tomonth, toyear; 
 	static boolean validateDateFlag;
 	static String input;
 	String[] reportType;
 	static String orgtype;
 	boolean reportmenuflag;
 	String orgname;
 	static String reportTypeFlag;
 	module m;
 	TextView tvLedgerWarning;
 	static String IPaddr;
 	static final int FROM_DATE_DIALOG_ID = 0, TO_DATE_DIALOG_ID = 1;
 	Button from_btn_ID, to_btn_ID;
 	boolean result = false;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.report);
 		IPaddr = MainActivity.IPaddr;
 		System.out.println("in createorg"+IPaddr);
 		account = new Account(IPaddr);
 		organisation = new Organisation(IPaddr);
 		m= new module();
 		
        	client_id= Startup.getClient_id();
        	
        	/*
        	 * get org type 
        	 */
        	orgtype = menu.orgtype;
        	
         if("NGO".equals(orgtype))
         {
             reportType = new String[] { "Ledger","Trial Balance","Project Statement",
                  "Cash Flow","Balance Sheet","Income and Expenditure" };
         }
         else{
             reportType = new String[] { "Ledger","Trial Balance","Project Statement",
                  "Cash Flow","Balance Sheet","Profit and Loss account" };
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
 		
 	   	//set title
 	   	String OrgName = MainActivity.organisationName;
 		TextView org = (TextView)findViewById(R.id.org_name);
 		org.setText(OrgName + ", "+orgtype);
 		TextView tvdate = (TextView)findViewById(R.id.date);
 		tvdate.setText(m.changeDateFormat(financialFromDate)+" To "+m.changeDateFormat(financialToDate));
 	   	
 		Button btn_optionsMenu= (Button) findViewById(R.id.btn_optionsMenu);
 		btn_optionsMenu.setVisibility(View.GONE);
 		Button btn_changeInputs= (Button) findViewById(R.id.btn_changeInputs);
 		btn_changeInputs.setVisibility(View.GONE);
 		
 		//getting the list view and setting background
 		final ListView listView = (ListView)findViewById(R.id.ListReportType);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, reportType);
 		 
 		listView.setAdapter(adapter);
 		listView.setTextFilterEnabled(true);
 		listView.setBackgroundColor(Color.parseColor("#2f2f2f")); //gray theme
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
 					callLedgerOrCashFlowOrBankRecon(reportMenu.this,"L",ledger.class);
 				}
 				//trial balance
 				if(position == 1)
 				{
 					callReport(reportMenu.this,"T",trialBalance.class);
 				}
 				//project statement
 				if(position == 2)
 				{
 					callReport(reportMenu.this,"P", projectStatement.class);
 				}
 				if(position == 3)
 				{
 					callLedgerOrCashFlowOrBankRecon(reportMenu.this,"C",cashFlow.class);
 				}
 				if(position == 4)
 				{
 					callReport(reportMenu.this,"B", balanceSheet.class);
 					
 				}
 				if(position == 5)
 				{
 					callReport(reportMenu.this,"I", incomeExpenditure.class);
 				}
 			} 
 		});
 	}  
 	
 	
 	
 	Spinner spinner_input;
 	public void callReport(Context ctxt, String flag, final Class report_class) {
 		organisation = new Organisation(IPaddr);
 		m= new module();		
 		context = ctxt;
 		
 		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
 		View layout = inflater.inflate(R.layout.trial_balance, null);
 		//Building DatepPcker dialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setView(layout);
 		
 		spinner_input = (Spinner)layout.findViewById(R.id.spinner_input);
 		TableRow spinner_row = (TableRow)layout.findViewById(R.id.spinner_row);
 		TextView tv_spinner = (TextView)layout.findViewById(R.id.tv_spinner);
 		
 		if (flag.equalsIgnoreCase("T")) {
 			builder.setTitle("Trial Balance");
 		}else if (flag.equalsIgnoreCase("P")) {
 			builder.setTitle("Project Statement");
 			tv_spinner.setText("Select project name:");
 			
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
 			ArrayAdapter<String> da1 = new ArrayAdapter<String>(context, 
 										android.R.layout.simple_spinner_item,projectnamelist);
 	  	   	da1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	  	   	spinner_input.setPrompt(context.getString(R.string.projectName_prompt));
 	  	   	spinner_input.setAdapter(da1);
 		}else if (flag.equalsIgnoreCase("B")) {
 			builder.setTitle("Balance Sheet");
 			tv_spinner.setText("Select Balance Sheet type:");
 			
 			String BalanceSheet_array[] = {"Conventional Balance Sheet","Sources and Application of Funds"};
 			ArrayAdapter<String> da1 = new ArrayAdapter<String>(context, 
 					android.R.layout.simple_spinner_item,BalanceSheet_array);
 			da1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinner_input.setPrompt(context.getString(R.string.BalanceSheet_prompt));
 			spinner_input.setAdapter(da1);
 		}else if (flag.equalsIgnoreCase("I")) {
 			spinner_row.setVisibility(View.GONE);
 			if(orgtype.equalsIgnoreCase("NGO")){
 				builder.setTitle("Income and Expenditure");
 				reportTypeFlag = "Income and Expenditure";
 			}
 			else{
 				builder.setTitle("Profit and Loss");
 				reportTypeFlag = "Profit and Loss";
 			}
 		}
 		
 		
 		tvLedgerWarning = (TextView)layout.findViewById(R.id.tvTrialWarning);
 		
 		to_btn_ID= (Button)layout.findViewById(R.id.btnsetLedgerTodate);
   	   	to_btn_ID.setText(today+"-"+tomonth+"-"+toyear);
   	   	to_btn_ID.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				showDateDialog(context, 1); //1 for 'to' date picker
 			}
 		});
 		
 		
 		
 		Button btnView = (Button)layout.findViewById(R.id.btnView);
 	   	Button btnCancel = (Button)layout.findViewById(R.id.btnCancel);
 	   	btnView.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				input = spinner_input.getSelectedItem().toString();
 				validateDateFlag = m.validateDate(financialFromDate, financialToDate, null, to_btn_ID.getText().toString(), null,tvLedgerWarning);
 				givenToDateString = m.givenToDateString;
 				
 			   	if(validateDateFlag){
 					Intent intent = new Intent(context, report_class);
 					// To pass on the value to the next page
 					context.startActivity(intent);
 				}
 			}
 		});
 	   	btnCancel.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				dialog.dismiss();
 			}
 		});
 		
 		dialog=builder.create();
 		dialog.show();
 		
 	}
 
 	List<String> accountnamelist;
 	boolean callingLedger=false;
 	Spinner accountNames, projectNames;
 	CheckBox cbNarration, cbClearedTransaction;
 	static boolean cleared_tran_flag;
 	public void callLedgerOrCashFlowOrBankRecon(final Context ctxt, final String report_flag, final Class report_class) {
 		IPaddr = MainActivity.IPaddr;
 		Account account = new Account(IPaddr);
 		context = ctxt;
 		Organisation organisation = new Organisation(IPaddr);
 		final module m= new module();
 		if (report_flag.equalsIgnoreCase("L") || report_flag.equalsIgnoreCase("BR")) {
			//call the allAccountNames method to get all account names
			Object[] accountnames = (Object[]) account.getAllAccountNames(Startup.getClient_id());
 			// create new array list of type String to add account names
 			accountnamelist = new ArrayList<String>();
 			for(Object an : accountnames)
 			{	
 				accountnamelist.add((String) an); 
 			}
 			
 			if(accountnamelist.size() <= 0){
 				String message;
 				if (report_flag.equalsIgnoreCase("L")) {
 					message = "Ledger cannot be displayed, Please create account!";
 				}else {
 					message = "Bank reconciliation statement cannot be displayed, Please create bank account!";
 				}
 				m.toastValidationMessage(context,message);
 			}
 			else{
 				callingLedger = true;
 			}
 		}
 		
 		
 		if(report_flag.equalsIgnoreCase("C") || callingLedger == true){
 			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
 			View layout = inflater.inflate(R.layout.ledger, null);
 			//Building DatepPcker dialog
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder.setView(layout);
 			
 			tvLedgerWarning = (TextView)layout.findViewById(R.id.tvLedgerWarning);
 			
 			TableRow acc_row = (TableRow)layout.findViewById(R.id.acc_row);
 			TableRow narration_row = (TableRow)layout.findViewById(R.id.narration_row);
 			TableRow project_row = (TableRow)layout.findViewById(R.id.project_row);
 			TableRow cleared_row = (TableRow)layout.findViewById(R.id.cleared_row);
 			
 			if(report_flag.equalsIgnoreCase("L") || report_flag.equalsIgnoreCase("BR")){
 				if(report_flag.equalsIgnoreCase("L")){
 					builder.setTitle("Ledger");
 					cleared_row.setVisibility(View.GONE);
 					
 					//call the getAllProjects method to gaet all projects
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
 					projectNames = (Spinner)layout.findViewById(R.id.sLedgerProject);
 					ArrayAdapter<String> da1 = new ArrayAdapter<String>(context, 
 												android.R.layout.simple_spinner_item,projectnamelist);
 			  	   	da1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			  	   	projectNames.setAdapter(da1);
 			  	   	
 			  	   	
 				}else {
 					builder.setTitle("Bank reconcilition");
 					project_row.setVisibility(View.GONE);
 					cbClearedTransaction = (CheckBox)layout.findViewById(R.id.cbClearedTransaction);
 				}
 				
 				//populate all account names in accountname dropdown(spinner)
 				accountNames = (Spinner)layout.findViewById(R.id.sAccountNameinLedger);
 				ArrayAdapter<String> da = new ArrayAdapter<String>(context, 
 											android.R.layout.simple_spinner_item,accountnamelist);
 		  	   	da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		  	   	accountNames.setAdapter(da);
 		  	   	
 		  	   	cbNarration = (CheckBox)layout.findViewById(R.id.cbNarrations);
 			}else{
 				builder.setTitle("Cash Flow");
 				acc_row.setVisibility(View.GONE);
 				narration_row.setVisibility(View.GONE);
 				project_row.setVisibility(View.GONE);
 				cleared_row.setVisibility(View.GONE);
 			}
 			
 	  	   	from_btn_ID = (Button)layout.findViewById(R.id.btnsetLedgerFromdate);
 	  	   	from_btn_ID.setText(menu.fromday+"-"+menu.frommonth+"-"+menu.fromyear);
 	  	   	from_btn_ID.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
                     showDateDialog(context, 0); // 0 for 'from' date picker
                     
 				}
 			});
 	  	   	to_btn_ID= (Button)layout.findViewById(R.id.btnsetLedgerTodate);
 	  	   	to_btn_ID.setText(menu.today+"-"+menu.tomonth+"-"+menu.toyear);
 	  	   	to_btn_ID.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					showDateDialog(context, 1); //1 for 'to' date picker
 				}
 			});
 		   	
 		   	Button btnView = (Button)layout.findViewById(R.id.btnView);
 		   	Button btnCancel = (Button)layout.findViewById(R.id.btnCancel);
 		   	btnView.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					if (callingLedger) {
 						if(report_flag.equalsIgnoreCase("L")){
 							selectedProject = projectNames.getSelectedItem().toString();
 						}else if(report_flag.equalsIgnoreCase("BR")){
 							cleared_tran_flag = cbClearedTransaction.isChecked();
 						}
 						selectedAccount = accountNames.getSelectedItem().toString();
 						
 						cheched = cbNarration.isChecked();
 					}
 					
 					validateDateFlag = m.validateDate(menu.financialFromDate, menu.financialToDate, from_btn_ID.getText().toString(), 
 							to_btn_ID.getText().toString(), "validatebothFromToDate",tvLedgerWarning);
 					givenfromDateString = m.givenfromDateString;
 					givenToDateString = m.givenToDateString;
 					
 					//date validation
 					if(validateDateFlag){
 							Intent intent = new Intent(context, report_class);
 							// To pass on the value to the next page
 							context.startActivity(intent);
 					}
 				}
 			});
 		   	btnCancel.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					dialog.dismiss();
 				}
 			});
 			dialog=builder.create();
 			dialog.show();						
 		}		
 	}
 
 
 	public void showDateDialog(Context context, final int flag) {
 		/*
          * Building DatepPicker dialog
          */
         AlertDialog dialog1;
        
         //Preparing views
         LayoutInflater inflater = ((Activity)context).getLayoutInflater();
         View layout = inflater.inflate(R.layout.datepiker, null);
        
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setView(layout);
         builder.setTitle("Select date");
        
         final   DatePicker dp = (DatePicker) layout.findViewById(R.id.datePicker1);
         if (flag == 0) {
         	dp.init(Integer.parseInt(menu.fromyear), Integer.parseInt(menu.frommonth)-1,Integer.parseInt(menu.fromday), null);
 		}else if (flag == 1) {
 			
 			dp.init(Integer.parseInt(menu.toyear), Integer.parseInt(menu.tomonth)-1,Integer.parseInt(menu.today), null);
 		}
         
         builder.setNegativeButton("Cancel",new  DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface arg0, int arg1) {
                //do nothing
             }
         });
         
         /*
          * 'set' button will get the date from datepicker dialog and 
          * will set into the clearance date textview
          */
         builder.setPositiveButton("Set",new  DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface arg0, int arg1) {
             	int year = dp.getYear();
                 int month = dp.getMonth();
                 int day =  dp.getDayOfMonth();
                 
                 if (flag == 0) {
                 	from_btn_ID.setText(new StringBuilder()
     				// Month is 0 based, just add 1
     				.append(String.format("%02d", day)).append("-").append(String.format("%02d", month+1)).append("-")
     				.append(year));
     				givenfromDateString = String.format("%02d", day)+"-"+String.format("%02d", month+1)+"-"+(year-1);
 				}else if (flag == 1) {
 					to_btn_ID.setText(new StringBuilder()
 					// Month is 0 based, just add 1
 					.append(String.format("%02d", day)).append("-").append(String.format("%02d", month+1)).append("-")
 					.append(year));
 					givenToDateString = String.format("%02d", day)+"-"+String.format("%02d", month+1)+"-"+(year-1);
 				}
                 
             }
         });
         
         dialog1=builder.create();
         dialog1.show();
 	}	
 }
