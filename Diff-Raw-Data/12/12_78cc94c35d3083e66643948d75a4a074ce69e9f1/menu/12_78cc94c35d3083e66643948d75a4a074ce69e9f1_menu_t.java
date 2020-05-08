 package com.example.gkaakash;
 
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.RoundingMode;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import com.gkaakash.controller.Account;
 import com.gkaakash.controller.Organisation;
 import com.gkaakash.controller.Preferences;
 import com.gkaakash.controller.Report;
 import com.gkaakash.controller.Startup;
 import com.gkaakash.controller.User;
 
 import android.R.drawable;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.Preference;
 import android.text.InputType;
 import android.text.SpannableString;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class menu extends ListActivity{
     //adding a class property to hold a reference to the controls
     String  voucherTypeFlag;
     private int group1Id = 1;
     int Edit = Menu.FIRST;
     int Delete = Menu.FIRST +1;
     int Finish = Menu.FIRST +2;
     AlertDialog dialog;
     final Context context = this;
 	static String fromday, frommonth, fromyear, today, tomonth, toyear; 
     private Integer client_id;
     private Account account;
     private Preferences preferences;
     private Organisation organisation;
     private Report report;
     AlertDialog help_dialog;
     static String financialFromDate;
 	static String financialToDate;
 	static String givenfromDateString;
 	static String givenToDateString;
 	DecimalFormat mFormat;
 	static boolean validateDateFlag;
 	static String selectedAccount;
 	static boolean cleared_tran_flag;
 	static boolean narration_flag;
 	static ArrayList<String> accdetailsList;
 	boolean reportmenuflag;
     static String orgtype,userrole;
     static String OrgName,rolloverFlag;
     TextView tvWarning;
     module m;
     String[] menuOptions;
     static String rollover;
     private User user;
     RadioButton rbmanager,rbRoleChecked,rboperator;
 	RadioButton rbGenderChecked,rbMale;
     String gender,username,password,confpassword;
     RadioGroup  radiogender ,radiorole ;
     EditText eusername,epassword,econfpassword;
     String login_time;
     String logout_time;
     EditText oldpass,newpass,confirmpass;
     CharSequence[] items;
     boolean reset_password_flag = false;
     static String IPaddr;
     /*
     //adding options to the options menu
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     menu.add(group1Id, Edit, Edit, "Edit");
     menu.add(group1Id, Delete, Delete, "Delete");
     menu.add(group1Id, FinishAlertDialog help_dialog;, Finish, "Finish");
     return super.onCreateOptionsMenu(menu); 
     }
      
     //code for the actions to be performed on clicking options menu goes here ...
      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case 1:
         Toast msg = Toast.makeText(menu.this, "Menu 1", Toast.LENGTH_LONG);
         msg.show();
         return true;
         }
         return super.onOptionsItemSelected(item);
     }
     */
     
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onBackPressed()
      * send the users login and logout timings to the backend
      * and pass on the activity to the main page
      */
      @Override
      public void onBackPressed() {
     	 AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder.setMessage("Do you want to logout?")
 			.setCancelable(false)
 			
 			.setPositiveButton("Yes",
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 					Date date = new Date();
 					logout_time = dateFormat.format(date);
 					System.out.println("date"+login_time+"and"+logout_time+"  username"+username+"  userrole"+userrole);
 					
 					Object[] params = new Object[]{username,userrole,login_time,logout_time};
 			        user.setLoginLogoutTiming(params, client_id);
 					
 					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 			        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			        startActivity(intent); 
 				}
 			}).setNegativeButton("NO", 
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					//do nothing
 				}
 			});
 
 			AlertDialog alert = builder.create();
 			alert.show();     
     	 
     	 
      }
      
     //on load...getfinancialFromDate
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         IPaddr = MainActivity.IPaddr;
     	System.out.println("in createorg"+IPaddr);
         account = new Account(IPaddr);
         preferences = new Preferences(IPaddr);
         organisation = new Organisation(IPaddr);
         report = new Report(IPaddr);
         client_id= Startup.getClient_id();
         m= new module();
         reportmenuflag = MainActivity.reportmenuflag;
         
 		// create instance of user class to call setUser method
 		user = new User(IPaddr);
 		// get the client_id from startup
 		
 		
       
         
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
 	   	
 	   	//for two digit format date for dd and mm
 	  	mFormat= new DecimalFormat("00");
 	  	mFormat.setRoundingMode(RoundingMode.DOWN);
 	  	
 	  	//reportmenuflag will tell you from which page u are came createOrg or selectOtg
 	    if (reportmenuflag == true) {
 
 			OrgName = createOrg.organisationName;
 			orgtype=createOrg.orgTypeFlag;
 			userrole = createOrg.user_role;
 			username = createOrg.username;
 
 		} else {
 			OrgName = selectOrg.selectedOrgName;
 			userrole = selectOrg.user_role;
 			username = selectOrg.login_user;
 			Object[] params = new Object[]{OrgName};
 	        orgtype = (String) organisation.getorgTypeByname(params, client_id);
 	        reset_password_flag = selectOrg.reset_password_flag;
 		}
 	    
 	    reset_password(reset_password_flag);
 	    
 	    
 	    //set the login timing of user in the database
 	    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 		Date date = new Date();
 		login_time = dateFormat.format(date);
 		
         user.getUserNemeOfOperatorRole(client_id);
      // call getPrefernece to get set preference related to account code flag   
         rolloverFlag = preferences.getPreferences(new Object[]{"2"},client_id);
 	    //adding list items to the newly created menu list
 	    if(userrole.equalsIgnoreCase("guest"))
         {
 	    	if(rolloverFlag.equalsIgnoreCase("manually"))
 	    	{
         	menuOptions = new String[] { "Create account", "Transaction", "Reports",
                     "Bank Reconciliation", "Preferences","RollOver","Export organisation","Help","About"};
 	    	}else
 	    	{
 	    		menuOptions = new String[] { "Create account", "Transaction", "Reports",
 	                    "Bank Reconciliation", "Preferences","Export organisation","Help","About"};
 	    	}
         }else if(userrole.equalsIgnoreCase("admin")){
         	if(rolloverFlag.equalsIgnoreCase("manually"))
 	    	{
         	menuOptions = new String[] { "Create account", "Transaction", "Reports",
                     "Bank Reconciliation", "Preferences","RollOver","Export organisation","Account Settings","Help","About" };
 	    	}else
 	    	{
 	    		menuOptions = new String[] { "Create account", "Transaction", "Reports",
 	                    "Bank Reconciliation", "Preferences","Export organisation","Account Settings","Help","About" };
 	    	}
         }else if(userrole.equalsIgnoreCase("manager")){
         	menuOptions = new String[] { "Create account", "Transaction", "Reports",
                     "Bank Reconciliation","Preferences", "Export organisation","Account Settings","Help","About" };
         }else{//operator
             menuOptions = new String[] { "Create account", "Transaction","Export organisation","Account Settings","Help","About" };
             
         }
         //calling menu.xml and adding menu list into the page
         setListAdapter(new ArrayAdapter<String>(this, R.layout.menu,menuOptions));
  
         //getting the list view and setting background
         final ListView listView = getListView();
         listView.setTextFilterEnabled(true);
         listView.setBackgroundColor(R.drawable.dark_gray_background);
         listView.setCacheColorHint(Color.TRANSPARENT);
         
         
         
         //when menu list items are clicked, code for respective actions goes here ...
         listView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
             	if(position == 0)
                 {
                     MainActivity.tabFlag = true;
                     Intent intent = new Intent(context, account_tab.class);
                     // To pass on the value to the next page
                     startActivity(intent);
                 }
                 
                 //for "transaction"
                 if(position == 1)
                 {
                     Intent intent = new Intent(context, voucherMenu.class);
                     // To pass on the value to the next page
                     startActivity(intent);
                 }
             	if(userrole.equalsIgnoreCase("guest") || userrole.equalsIgnoreCase("admin") || userrole.equalsIgnoreCase("manager"))
                 {
             		 //for "reports"
                     if(position == 2)
                     {
                         Intent intent = new Intent(context, reportMenu.class);
                         // To pass on the value to the next page
                         startActivity(intent);                     
                     }
                     //bank reconciliation 
                     if(position == 3){
                     	
                     	bankrecon();
     					
                     }
                     //for "adding project", adding popup menu ...
                 	if(position == 4)
                 	{                	
                 		addPreferences();
        	
                 	}
                     if(userrole.equalsIgnoreCase("guest")){ 
                     	if(rolloverFlag.equals("manually"))
                     	{
 	                        // for rollOver
 	        				if (position == 5) {
 	        					rollover();
 	        				}
 	        				if(position == 6){
         						export();
         					}
                     		//for help
         	                if(position == 7){
         	                    Intent intent = new Intent(context, Help.class);
         						// To pass on the value to the next page
         						startActivity(intent);
         	                }
         	                
         	                //for about
         	                if(position == 8){
         	                    about();
         	                }
 	        				
                     	}else{
                     		
                     		if(position == 5){
         						export();
         					}
                     		//for help
         	                if(position == 6){
         	                    Intent intent = new Intent(context, Help.class);
         						// To pass on the value to the next page
         						startActivity(intent);
         	                }
         	                
         	                //for about
         	                if(position == 7){
         	                    about();
         	                }
                     	}
                     }
                     else if(userrole.equalsIgnoreCase("admin")){
                     	if(rolloverFlag.equals("manually"))
         				{
                     		 // for rollOver
 	        				if (position == 5) {
 	        					rollover();
 	        				}
	        				exportToAbout(position,6);
         					
         				}else{//for admin only
         					exportToAbout(position,5);
         				}
                     }else{ //for manager only
         				exportToAbout(position,5);
                     }
                     
                 }else{//for operator only
                 	exportToAbout(position,2);
                 }
 
             }
 
 			private void exportToAbout(int position, int i) {
 				//export organisation
 				if(position == i){
 					
 					export();
 				}
 				//for settings
                 if(position == i+1){
                 	settings();  
                 }
                 //for help
                 if(position == i+2){
                     Intent intent = new Intent(context, Help.class);
 					// To pass on the value to the next page
 					startActivity(intent);
                 }
                 
                 //for about
                 if(position == i+3){
                     about();
                 }
 			
 			}
         });
     }
 
 	private void reset_password(boolean reset_password_flag) {
 		if(reset_password_flag){
 			/* false for u don't want to close the dialog on clicking cancel button
 			 * if u want to close pass true as a param
 			*/
 			m.resetPassword(context,username,userrole,false,client_id);
 		}		
 	}
 
 	protected void bankrecon() {
     	//call the getAllBankAccounts method to get all bank account names
 		Object[] accountnames = (Object[]) account.getAllBankAccounts(client_id);
 		// create new array list of type String to add account names
 		List<String> accountnamelist = new ArrayList<String>();
 		for(Object an : accountnames)
 		{	
 			accountnamelist.add((String) an); 
 		}	
 		
 		if(accountnamelist.size() <= 0){
 			String message = "Bank reconciliation statement cannot be displayed, Please create bank account!";
 			m.toastValidationMessage(menu.this,message);
 			}
 		else{
     	
         	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layout = inflater.inflate(R.layout.bank_recon_index, (ViewGroup) findViewById(R.id.layout_root));
 			//Building DatepPcker dialog
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder.setView(layout);
 			builder.setTitle("Bank reconcilition");
 			
 			//populate all bank account names in accountname dropdown(spinner)
 			final Spinner sBankAccounts = (Spinner)layout.findViewById(R.id.sBankAccounts);
 			ArrayAdapter<String> da = new ArrayAdapter<String>(menu.this, 
 										android.R.layout.simple_spinner_item,accountnamelist);
 	  	   	da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	  	   	sBankAccounts.setAdapter(da);
 			
 			final DatePicker ReconFromdate = (DatePicker) layout.findViewById(R.id.dpsetReconFromdate);
 			ReconFromdate.init(Integer.parseInt(fromyear),(Integer.parseInt(frommonth)-1),Integer.parseInt(fromday), null);
 		   	
 		   	final DatePicker ReconT0date = (DatePicker) layout.findViewById(R.id.dpsetReconT0date);
 		   	ReconT0date.init(Integer.parseInt(toyear),(Integer.parseInt(tomonth)-1),Integer.parseInt(today), null);
 			
 		   	final CheckBox cbClearedTransaction = (CheckBox)layout.findViewById(R.id.cbClearedTransaction);
 		   	final CheckBox cbNarration = (CheckBox)layout.findViewById(R.id.cbReconNarration);
 		   	
 		   	tvWarning = (TextView)layout.findViewById(R.id.tvBankReconWarning);
 			Button btnView = (Button)layout.findViewById(R.id.btnView);
 		   	Button btnCancel = (Button)layout.findViewById(R.id.btnCancel);
 		   	
 		   	btnView.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					if(cbClearedTransaction.isChecked()){
 				   		cleared_tran_flag = true;
 				   	}
 				   	else{
 				   		cleared_tran_flag = false;
 				   	}
 				   	
 				   	if(cbNarration.isChecked()){
 				   		narration_flag = true;
 				   	}
 				   	else{
 				   		narration_flag = false;
 				   	}
 					
 					selectedAccount = sBankAccounts.getSelectedItem().toString();
 					
 					System.out.println("i am account"+selectedAccount);
 					validateDate(ReconFromdate, ReconT0date, "validatebothFromToDate",tvWarning);
 					
 					
 					if(validateDateFlag){
 						Intent intent = new Intent(context, bankReconciliation.class);
 						// To pass on the value to the next page
 						startActivity(intent);
 					}
 				}
 			});
 		   	
 		   	btnCancel.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					
 					dialog.dismiss();
 				}
 			});
 			/*builder.setPositiveButton("View",new  DialogInterface.OnClickListener(){
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 				
 					if(cbClearedTransaction.isChecked()){
 				   		cleared_tran_flag = true;
 				   	}
 				   	else{
 				   		cleared_tran_flag = false;
 				   	}
 				   	
 				   	if(cbNarration.isChecked()){
 				   		narration_flag = true;
 				   	}
 				   	else{
 				   		narration_flag = false;
 				   	}
 					
 					selectedAccount = sBankAccounts.getSelectedItem().toString();
 					
 					System.out.println("i am account"+selectedAccount);
 					validateDate(ReconFromdate, ReconT0date, "validatebothFromToDate");
 					
 					
 					if(validateDateFlag){
 						Intent intent = new Intent(context, bankReconciliation.class);
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
 				
 			});*/
 			dialog=builder.create();
     		dialog.show();
     		
     		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 			//customizing the width and location of the dialog on screen 
 			lp.copyFrom(dialog.getWindow().getAttributes());
 			lp.width = 700;
 			dialog.getWindow().setAttributes(lp);
 		}
 		
 	}
 
 	protected void rollover() {
     	Object[] rollover_exist_params = new Object[] {OrgName,financialFromDate,financialToDate};
 		Boolean existRollOver = report.existRollOver(rollover_exist_params);
 		
 		if (existRollOver.equals(false)) {
 					Object[] rollover_params = new Object[] {OrgName, financialFromDate,financialToDate,
 orgtype };
 					rollover = report.rollOver(rollover_params,client_id);
 					if(rollover.equalsIgnoreCase("false"))
 					{
 						m.toastValidationMessage(context,"can not rollover , since financial year is not completed !!");
 					}
 		}
 
 	}
 
 	protected void export() {
 //		Intent email = new Intent(Intent.ACTION_SEND);
 //        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"holyantony1492@gmail.com"});
 //        //email.putExtra(Intent.EXTRA_CC, new String[]{ to});
 //        //email.putExtra(Intent.EXTRA_BCC, new String[]{to});
 //        email.putExtra(Intent.EXTRA_SUBJECT, "testing email");
 //        email.putExtra(Intent.EXTRA_TEXT, "hello world");
 //
 //        //File root = Environment.getExternalStorageDirectory();
 //        File file = new File("/opt/abt/export/bckp.xml");
 //        if (!file.exists() || !file.canRead()) {
 //            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
 //            finish();
 //            return;
 //        }
 //        Uri uri = Uri.parse("file://" + file);
 //        email.putExtra(Intent.EXTRA_STREAM, uri);
 //        
 //        
 //        //need this to prompts email client only
 //        email.setType("message/rfc822");
 //        startActivity(Intent.createChooser(email, "Choose an Email client :"));
 		 AlertDialog.Builder builder = new AlertDialog.Builder(
                  context);
          //builder.setTitle("Aakash Business Tool");
          builder.setMessage("Do you want to export organisation");
          builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
             	 Object[] export = new Object[] {OrgName, financialFromDate,financialToDate};
          		//call back-end to export organisation 
          		String encrypted_db = organisation.Export(export,client_id);
          		Toast.makeText(menu.this, encrypted_db, Toast.LENGTH_SHORT).show();
          		//copy export dir from /opt/abt/ to sdcard
                  String[] command = {"rm -r /mnt/sdcard/export", "busybox cp /data/local/abt/opt/abt/export/ /mnt/sdcard/ -R"};
                  module.RunAsRoot(command);
                  m.toastValidationMessage(context, "organisation "+OrgName+" has been exported to /mnt/sdcard/export/");
                  MainActivity.menuOptionFlag = true;
                  
              }
            
          });
          builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
         	 public void onClick(DialogInterface dialog, int id) {
 
  			}
           
          });
          
         AlertDialog about_dialog = builder.create();
         about_dialog.show();
 		
     	
 		
 	}
 
 	protected void about() {
     	AlertDialog about_dialog;
         final SpannableString s = new SpannableString(context.getText(R.string.about_para));
                 Linkify.addLinks(s, Linkify.WEB_URLS);
                 
 
                 // Building DatepPcker dialog
                 AlertDialog.Builder builder = new AlertDialog.Builder(
                         context);
                 builder.setTitle("Aakash Business Tool");
                 builder.setMessage( s );
                 builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         // TODO Auto-generated method stub
                         
                     }
                   
                 });
                 
                 about_dialog = builder.create();
                 about_dialog.show();
                 
                 ((TextView)about_dialog.findViewById(android.R.id.message))
                 .setMovementMethod(LinkMovementMethod.getInstance());
                 
                 WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                 // customizing the width and location of the dialog on screen
                 lp.copyFrom(about_dialog.getWindow().getAttributes());
                 lp.width = 600;
                 
                 about_dialog.getWindow().setAttributes(lp);
 		
 	}
 
 	protected void settings() {
 		if(userrole.equalsIgnoreCase("operator")){
 			items = new CharSequence[]{ "Change Username","Change Password"};
 		}else{
 			items = new CharSequence[]{ "Change Username","Change Password", "Add user"};
 		}
 		
 		//creating a dialog box for popup
 		AlertDialog.Builder builder = new AlertDialog.Builder(menu.this);
 		//setting title
 		builder.setTitle("User settings");
 		//adding items
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog,
 					int pos) {
 				if(pos == 0){
 					final View layout1 = m.builder_with_inflater(menu.this, "",
 							R.layout.change_password);
 					LinearLayout l1 = (LinearLayout) layout1
 							.findViewById(R.id.changepassword);
 					l1.setVisibility(View.GONE);
 
 					final TextView error_msg = (TextView) layout1
 							.findViewById(R.id.tverror_msg1);
 					Button save = (Button) layout1.findViewById(R.id.btnSave);
 					TextView header = (TextView) layout1
 							.findViewById(R.id.tvheader1);
 					header.setText("Change username");
 
 					Button cancel = (Button) layout1
 							.findViewById(R.id.btnCancel);
 					cancel.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 							m.dialog.cancel();
 						}
 					});
 
 					save.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 
 							
 							EditText olduser_name = (EditText) layout1
 									.findViewById(R.id.etOld_User_Name);
 							String olduserName = olduser_name.getText()
 									.toString();
 							System.out.println("olduserName:" + olduserName);
 							EditText newuser_name = (EditText) layout1
 									.findViewById(R.id.etNewUsername);
 							String newusername = newuser_name.getText()
 									.toString();
 							System.out.println("new username:" + newusername);
 
 							EditText password = (EditText) layout1
 									.findViewById(R.id.etPassword);
 							String password1 = password.getText().toString();
 							System.out.println("password:" + password1);
 							// System.out.println("username:"+username);
 							// System.out.println("userrole:"+userrole1);
 
 							if (!"".equals(olduserName)
 									& !"".equals(newusername)
 									& !"".equals(password1)) {
 								Boolean username_result = user.changeUserName( 
 										new Object[] { olduserName,
 												newusername, password1,
 												userrole }, client_id);
 								System.out.println("r:" + username_result);
 								if (username_result == true) {
 									error_msg.setVisibility(TextView.VISIBLE);
 									error_msg
 											.setText("Username updated successully");
 									
 									olduser_name.setText("");
 									newuser_name.setText("");
 									password.setText("");
 
 								} else {
 									error_msg.setVisibility(TextView.VISIBLE);
 									error_msg
 											.setText("Invalid username or password");
 									
 									olduser_name.setText("");
 									newuser_name.setText("");
 									password.setText("");
 								}
 							} else {
 								error_msg.setVisibility(TextView.VISIBLE);
 								error_msg.setText("Fill the empty fields");
 
 							}
 						}
 					});
 					
 				}
 				if(pos == 1){
 					final View layout=m.builder_with_inflater(menu.this, "",R.layout.change_password);
 					
 					LinearLayout l1=(LinearLayout) layout.findViewById(R.id.changeusername);
 					l1.setVisibility(View.GONE);
 					Button cancel = (Button) layout
 							.findViewById(R.id.btnCancel);
 					TextView header = (TextView) layout.findViewById(R.id.tvheader1);
 					header.setText("Change password");
 					
 					final TextView error_msg = (TextView) layout.findViewById(R.id.tverror_msg1);
 					cancel.setOnClickListener(new OnClickListener() {
 						
 						@Override
 						public void onClick(View arg0) {
 						m.dialog.cancel();
 							
 						}
 					});
 					
 				   Button save = (Button) layout
 							.findViewById(R.id.btnSave);
 					
 				   
 				   save.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View arg0) {
 						oldpass = (EditText) layout
 								.findViewById(R.id.etOldPass);
 						String old_pass=oldpass.getText().toString(); 
 						System.out.println("oldpass:"+old_pass);
 						newpass = (EditText) layout
 								.findViewById(R.id.etNewPass);
 	 					String new_pass=newpass.getText().toString();
 						System.out.println("newpass:"+new_pass);
 						
 						confirmpass = (EditText) layout
 								.findViewById(R.id.etconfirmPass);
 						String confirm_pass=confirmpass.getText().toString();
 						System.out.println("confirm_pass:"+confirm_pass);
 						
 						String username;
 						
 						Toast.makeText(context, "lll:"+MainActivity.username_flag, Toast.LENGTH_SHORT).show();
 						Boolean flag = MainActivity.username_flag;
 						if(flag==true){
 							username=selectOrg.login_user;
 							System.out.println("username1:"+username);
 						
 						}else {
 							username=createOrg.login_user;
 							System.out.println("username2:"+username);
 						}
 						
 						if(!"".equals(old_pass)&!"".equals(new_pass)&!"".equals(confirm_pass)){
 							if(new_pass.equals(confirm_pass)){
 								
 								Boolean result= user.changePassword(new Object[]{username,old_pass,new_pass,userrole}, client_id);
 								System.out.println("r:"+result);
 								if(result==false){
 									error_msg.setVisibility(TextView.VISIBLE);
 
 									error_msg.setText("Invalid password");
 									oldpass.setText("");
 									newpass.setText("");
 									confirmpass.setText(""); 
 								}else {
 									error_msg.setVisibility(TextView.VISIBLE);
 
 									error_msg.setText( "Password updated successully");
 									oldpass.setText("");
 									newpass.setText("");
 									confirmpass.setText(""); 
 								}
 							}else {
 								error_msg.setVisibility(TextView.VISIBLE);
 
 								error_msg.setText( "New password and confirm password fields doesnot match!");
 								newpass.setText("");
 								confirmpass.setText(""); 
 							}
 							
 						}else {
 							error_msg.setVisibility(TextView.VISIBLE);
 							error_msg.setText("Fill the empty fields");	
 						}
 					}
 				});
 					
 
 				}
 				if(pos==2){
 					Intent intent = new Intent(context, User_table.class);
 					startActivity(intent);
 
 				}
 			}				        	
 		});
 		dialog=builder.create();
 		((Dialog) dialog).show();
 	}
 
 	protected void addPreferences() {
     	final CharSequence[] items = { "Edit/Delete organisation", "Add/Edit/Delete project" };
     	//creating a dialog box for popup
     	AlertDialog.Builder builder = new AlertDialog.Builder(context);
     	//setting title
     	builder.setTitle("Select preference");
     	//adding items
     	builder.setItems(items, new DialogInterface.OnClickListener() {
     		public void onClick(DialogInterface dialog1, int pos) {
     			//code for the actions to be performed on clicking popup item goes here ...
     			switch (pos) {
    	        case 0:
    	        {
    	        	
    	        	MainActivity.editDetails=true;
    	        	Object[] editDetails = (Object[])organisation.getOrganisation(client_id);
    	        	accdetailsList = new ArrayList<String>();
    	        	for(Object row2 : editDetails){
    	        		Object[] a2=(Object[])row2;
    	        		ArrayList<String> accdetails = new ArrayList<String>();
                      for(int i=0;i<a2.length;i++){
                      	accdetails.add((String) a2[i].toString());
                      }
                      accdetailsList.addAll(accdetails);
    	        	}
                           
    	        	//System.out.println("details:"+accdetailsList);
                         
    	        	Intent intent = new Intent(context, orgDetails.class);
    	        	// To pass on the value to the next page
    	        	startActivity(intent);
    	        }break;
    	        case 1:
    	        {
    	        	Intent intent = new Intent(context, addProject.class);
    	        	// To pass on the value to the next page
    	        	startActivity(intent);
    	        	
    	        }break;
     			}
     		}
     	});
     	//building a complete dialog
     	dialog=builder.create();
     	dialog.show();
 		
 	}
 
 	private void reset() {
 		eusername.setText("");
 		epassword.setText("");
 		econfpassword.setText("");
 		rbMale.setChecked(true);
 	}
     private boolean validateDate(DatePicker fromdate, DatePicker todate, String flag,TextView warning){
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
 	        		//m.toastValidationMessage(menu.this,message);
 	        		tvWarning.setVisibility(View.VISIBLE);
 	        		tvWarning.setText(message);
 	        		validateDateFlag = false;
 	        	}
 	    	}
 	    	else if("rollover".equals(flag)) // check for the roll over flag
 	    	{   // if yes, then selected To-date must be after financial-todate and not equal financial-todate
 	    		if(cal4.after(cal2)&& !cal4.equals(cal2)) 
 	    		{
 	    			validateDateFlag = true;
 	    		}
 	    		else
 	    		{
 	    			String message = "Please enter proper date";
 	        		m.toastValidationMessage(menu.this,message);
 	    			validateDateFlag = false;
 	    		}
 	    	}else{
 	    		if((cal4.after(cal1) && cal4.before(cal2)) || cal4.equals(cal1) || cal4.equals(cal2) ){
 					
 	    			validateDateFlag = true;
 	        	}
 	        	else{
 	        		String message = "Please enter proper date";
 	        		//m.toastValidationMessage(menu.this,message);
 	        		tvWarning.setVisibility(View.VISIBLE);
 	        		tvWarning.setText(message);
 	        		validateDateFlag = false;
 	        	}
 	    	}
     	
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		return validateDateFlag;
 	}
     
    
    
     
 }
