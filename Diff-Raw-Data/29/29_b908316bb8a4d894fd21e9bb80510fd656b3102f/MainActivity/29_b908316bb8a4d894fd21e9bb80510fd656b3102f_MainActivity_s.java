 package com.example.gkaakash;
 
 import java.io.BufferedInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.RoundingMode;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.tools.tar.TarEntry;
 import org.apache.tools.tar.TarInputStream;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StatFs;
 import android.text.Html;
 import android.text.InputType;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.gkaakash.controller.Organisation;
 import com.gkaakash.controller.Startup;
 import com.gkaakash.controller.User;
 
 
 public class MainActivity extends Activity{
 	//Add a class property to hold a reference to the button
 	Button create_org;
 	private View select_org;
 	private Object[] orgNameList;
 	Spinner getOrgNames;
 	final Context context = this;
 	static Boolean tabFlag = false;
 	static Boolean searchFlag=false;
 	static Boolean nameflag=false;
 	static boolean reportmenuflag;
 	static boolean editDetails=false;
 	static AlertDialog help_dialog;
 	private ProgressDialog mProgressDialog, progressBar;
 	static String checkFlag;
 	int help_option_menu_flag = 0;
 	String result;
 	boolean fstab_flag = true;
 	File checkImg;
 	File checkImgextsd;
 	File checkTar;
 	File checkTarExtsd;
 	File help_flag;
 	static boolean no_dailog=false;
 	module m;
 	private AlertDialog dialog;
 	protected RadioButton radioButtonValue;
 	static Boolean username_flag=false,flagIP = false,remoteflag = true , menuOptionFlag = true;
 	static String IPaddr;
 
 	private Spinner getFinancialyear;
 	private Startup startup;
 	private User user;
 	private Button bProceed,btnSelLogIn;
 	Object[] financialyearList;
 	private Button btnDeleteOrg;
 	protected AdapterView<SpinnerAdapter> parent;
 	protected Object selectedFinancialYear;
 	//static String existingOrgFlag;
 	protected static Integer client_id;
 	protected static String fromDate;
 	protected static String  toDate;
 	RadioGroup radioUserGroup,radioUserAdminGroup;
 	RadioButton rb_admin,rb_guest,rb_manager,rb_operator;
 	private EditText eloginPassword;
 	private EditText eloginUsername;
 	static String user_role;
 	String loginUsername,login_password;
 	TextView tvwarning,tvLoginWarning,tvSignUp,tvuserrole,link;
 	String get_extra_flag;
 	static boolean reset_password_flag = false;
 
 	TextView tvDisplayFromDate, tvDisplayToDate;
 	Button btnChangeFromDate, btnChangeToDate, btnCreate,btnLogin,btnNext;
 	static int year, month, day, toYear, toMonth, toDay;
 	static final int FROM_DATE_DIALOG_ID = 0;
 	static final int TO_DATE_DIALOG_ID = 1;
 	Spinner orgType, sQuestions; 
 	RadioGroup radioGender;
 	String org;
 	static String organisationName,orgTypeFlag,selectedOrgType,todate;
 	static String fromdate;
 	final Calendar c = Calendar.getInstance();
 	private EditText orgName;
 	Object[] deployparams;
 	private boolean setOrgDetails;
 	DecimalFormat mFormat;
 	boolean orgExistFlag,createorgflag;
 	int genderid, selectedQuestion;
 	static String answer,lastname,username ,password ,confpassword,loginPassword;
 	private EditText eUserName ,ePassword , eConfPassword, eAnswer;
 	boolean adminflag=false;
 	protected Object[] orgparams;
 	private Organisation orgnisation;
 	private module module;
 	private SlideHolder mSlideHolder;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// 'Help' menu to 66666main page options menu
 
 		menu.add(1,1,1,"Help");
 		if(menuOptionFlag == true)
 		{ 
 			menu.add(1,2,2,"Import");
 			menu.add(1,3,3,"Set IP");
 		}
 
 		return super.onCreateOptionsMenu(menu);	
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		/*
 		 * when user clicks help menu on main page,
 		 * help_option_menu_flag is set to 1
 		 */
 		if(item.getItemId() == 1){
 			//Toast.makeText(context, "help_flag_option is set to 1", Toast.LENGTH_SHORT).show();
 			help_option_menu_flag = 1;
 			//if running this app on emulator, comment the below line
 			startApp();
 		}
 		if(item.getItemId() == 2){
 			//Toast.makeText(context, "help_flag_option is set to 2", Toast.LENGTH_SHORT).show();
 			if(flagIP.equals(true)){
 				importorganisation();
 			}
 		}
 		if(item.getItemId() == 3){
 			//Toast.makeText(context, "help_flag_option is set to 3"
 			setRemoteLocation();
 		}
 
 		return super.onOptionsItemSelected(item);
 	}//if running this app on emulator, comment the below line
 
 
 	private void importorganisation() {
 		try {
 			File export = new File("/mnt/sdcard/export");
 			if(export.exists()){
 				//copy export dir from /opt/abt/ to sdcard
 				String[] command = {"rm -r /data/local/abt/opt/abt/export/","busybox cp /mnt/sdcard/export/ /data/local/abt/opt/abt/ -R"};
 				module.RunAsRoot(command);
 
 				final Organisation organisation = new Organisation(IPaddr);
 				//get list of all exported organisations
 				Object[] result = (Object[]) organisation.getAllExportedOrganisations();
 				final ArrayList<ArrayList> Grid = new ArrayList<ArrayList>();
 				for(Object tb : result)
 				{
 					Object[] t = (Object[]) tb;
 					ArrayList<String> resultList = new ArrayList<String>();
 					for(int i=0;i<t.length;i++){
 
 						resultList.add((String) t[i].toString());
 
 					}
 					Grid.add(resultList);
 				}
 
 				System.out.println("grid1:"+Grid);
 				//call xml for creating layout inflater(customised dialog) 
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 				final View layout = inflater.inflate(R.layout.import_organisation, (ViewGroup) findViewById(R.id.layout_login));
 				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
 				builder.setView(layout);
 				TextView tvalertHead1 =(TextView) layout.findViewById(R.id.tvalertHead1);
 				tvalertHead1.setText("Import organisation");
 
 				Button btnImport =  (Button) layout.findViewById(R.id.btnImport);
 				Button btnCancel =  (Button) layout.findViewById(R.id.btnCancel);
 				final Spinner sOrganisation =  (Spinner) layout.findViewById(R.id.sOrganisation);
 				final Spinner sYear =  (Spinner) layout.findViewById(R.id.sYear);
 
 				//lets find duplicate words in java array and return array with unique words 
 				ArrayList<String> org = new ArrayList<String>();
 				Set<String> set = new HashSet<String>(Grid.get(0));
 				//Converting a set to an array for String representation
 				String[] array = set.toArray(new String[0]);
 
 				ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(context,
 						android.R.layout.simple_spinner_item, array);
 				dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				sOrganisation.setAdapter(dataAdapter1);
 
 				sOrganisation.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 					@Override
 					public void onItemSelected(AdapterView<?> arg0, View arg1,
 							int pos, long arg3) {
 						ArrayList<String> year = new ArrayList<String>();
 						for (int i = 0; i < Grid.get(0).size(); i++) {
 							if(sOrganisation.getSelectedItem().toString()
 									.equalsIgnoreCase((String) Grid.get(0).get(i))){
 								year.add((String) Grid.get(1).get(i));
 
 							}
 						}
 						//add financial year
 						ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
 								android.R.layout.simple_spinner_item, year);
 						dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 						sYear.setAdapter(dataAdapter);
 					}
 
 					@Override
 					public void onNothingSelected(AdapterView<?> arg0) {
 						// TODO Auto-generated method stub
 
 					}
 				});
 
 				btnImport.setOnClickListener(new View.OnClickListener(){
 					public void onClick(View v) {
 						System.out.println(sOrganisation.getSelectedItem());
 						System.out.println(sYear.getSelectedItem().toString().substring(0,10));
 						System.out.println(sYear.getSelectedItem().toString().substring(14,24));
 						Object[] params = new Object[]{sOrganisation.getSelectedItem(),
 								sYear.getSelectedItem().toString().substring(0,10),
 								sYear.getSelectedItem().toString().substring(14,24),
 								Grid.get(2).get(sOrganisation.getSelectedItemPosition()),
 								Grid.get(3).get(sOrganisation.getSelectedItemPosition())};
 						organisation.Import(params);
 						TextView warning =  (TextView) layout.findViewById(R.id.tvWarning);
 						warning.setVisibility(TextView.VISIBLE);
 						warning.setText(Html.fromHtml("Organisation <b>"+sOrganisation.getSelectedItem()+
 								"</b> for the financial year <b>"+sYear.getSelectedItem().toString()+
 								"</b> imported successfully!"));
 
 					}
 				});
 
 				btnCancel.setOnClickListener(new View.OnClickListener(){
 					public void onClick(View v) {
 						dialog.dismiss();
 					}
 				});
 
 
 				dialog = builder.create();
 				dialog.show();
 				dialog.setCanceledOnTouchOutside(true);
 				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 				//customizing the width and location of the dialog on screen 
 				lp.copyFrom(dialog.getWindow().getAttributes());
 				lp.width = 750;
 				dialog.getWindow().setAttributes(lp);
 			}else{
 				m.toastValidationMessage(context, "No backup file in /mnt/sdcard/export to import organisation");
 			}
 		} catch (Exception e) {
 			m.toastValidationMessage(context, "Please try again!");
 		}
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		//Calling activity_main.xml which is first page of GNUKhata
 		setContentView(R.layout.activity_main);
 		
 		
 		
 		/*
 		 * toggleView can actually be any view you want.
 		 * Here, for simplicity, we're using TextView, but you can
 		 * easily replace it with button.
 		 * 
 		 * Note, when menu opens our textView will become invisible, so
 		 * it quite pointless to assign toggle-event to it. In real app
 		 * consider using UP button instead. In our case toggle() can be
 		 * replaced with open().
 		 */
 		mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
 		View toggleView = findViewById(R.id.content_layout);
 		toggleView.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mSlideHolder.toggle();
 			}
 		});
 		
 		//create object of Startup to access connection
 		//startup = new Startup();
 		m=new module();
 
 		
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras == null) {
 			System.out.println("don hav xtra");
 		} else {  
 
 			get_extra_flag = extras.getString("flag");
 			System.out.println("hav xtra");
 			System.out.println("content:"+get_extra_flag);
 
 		}
 
 		// call the getOrganisationName method from startup
 		//orgNameList = startup.getOrgnisationName(); // return lists of existing organisations
 
 		//Request a reference to the button from the activity by calling “findViewById”
 		//and assign the retrieved button to an instance variable
 		create_org = (Button) findViewById(R.id.bcreateOrg);
 		select_org =(Button) findViewById(R.id. bselectOrg);
 		Button btnImport = (Button) findViewById(R.id.btnImport);
 		
 		btnImport.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(flagIP.equals(true)){
 					importorganisation();
 				}
 				
 			}
 		});
 		
 		Button btnSetIP = (Button) findViewById(R.id.btnSetIP);
 		btnSetIP.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				setRemoteLocation();
 			}
 		});
 		Button btnHelp = (Button) findViewById(R.id.btnHelp);
 		btnHelp.setOnClickListener(new OnClickListener() {
 			  
 			@Override
			public void onClick(View v) {  
 				help_option_menu_flag = 1;
 				startApp();
 			}
 		});
 		//Request a reference to the spinner from the activity by calling “findViewById”
 		//and assign the retrieved spinner to an instance variable
 		getOrgNames = (Spinner)findViewById(R.id.sGetOrgNames);
 		//creating new method do event on button
 		addListenerOnButton();
 
 		// copy 'aakash.sh and 'preinstall.sh to their respective paths'
 		File path = new File("/data/data/com.example.gkaakash/files/copyFilesFlag.txt");
 		if(!path.exists()){
 			loadDataFromAsset();
 		}
 		else {
 			//Toast.makeText(context, "not c       opying files from asset", Toast.LENGTH_SHORT).show();
 			System.out.println("NOT copying files from asset");
 		}
 		if(no_dailog==false){
 			//if running this app on emulator, comment the below line
 			startApp(); 
 		}    
 		else{
 			if(checkFlag=="false"){
 				help_dialog.dismiss();
 			}
 
 		}
 	}
 	//Attach a listener to the click event for the button
 	private void addListenerOnButton() {
 
 		//Create a class implementing “OnClickListener”
 		//and set it as the on click listener for the button
 		create_org.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View arg0) {
 				reportmenuflag = true;
 				System.out.println("ip on click create"+flagIP);
 				if(flagIP.equals(true)){
 					System.out.println("in create  button listner"+IPaddr);
 					remoteflag=false;
 					menuOptionFlag = false;
 
 					LinearLayout content_layout = (LinearLayout)findViewById(R.id.content_layout);
 					LayoutInflater inflater = ((Activity)MainActivity.this).getLayoutInflater();
 					View layout = inflater.inflate(R.layout.create_org, null);
 
 					if(content_layout.getChildCount() == 0){
 						content_layout.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 					}else{
 						content_layout.removeAllViews();
 						content_layout.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 					}
 
 					user = new User(IPaddr);
 					startup = new Startup(IPaddr);
 					System.out.println("in createorg2");
 
 					//for two digit format date for dd and mm
 					mFormat= new DecimalFormat("00");
 					mFormat.setRoundingMode(RoundingMode.DOWN);
 
 					//Declaring new method for setting date into "from date" and "to date" textview
 					setDateOnLoad(layout);
 					/*
 					 * creating a new interface for showing a date picker dialog that
 					 * allows the user to select financial year start date and to date
 					 */
 					addListeneronDateButton(layout);
 					addListeneronCreateButton(layout);
 					orgType = (Spinner) layout.findViewById(R.id.sOrgType);
 					org  = (String) orgType.getSelectedItem();
 					//addListeneronCreateButton();
 					//creating interface to listen activity on Item 
 					addListenerOnOrgTypeSpinner();
 
 					//					//To pass on the activity to the next page
 					//					Intent intent = new Intent(context,createOrg.class);
 					//					startActivity(intent);
 				}else
 				{
 					m.toastValidationMessage(context, "Please set proper IP again or check server is running!!");
 					remoteflag = true;
 					menuOptionFlag = true;
 				}
 
 			}// end of onClick
 		});// end of create_org.setOnClickListener
 		select_org.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View arg0) {
 				reportmenuflag = false;
 				if(flagIP.equals(true)){
 					System.out.println("in button listner"+IPaddr);
 					startup = new Startup(IPaddr);
 					remoteflag=false;
 					// check existing organisation name list is null
 					try{
 						// call the getOrganisationName method from startup
 						orgNameList = startup.getOrgnisationName(); // return lists of existing organisations
 						if(orgNameList.length<1)
 						{
 							AlertDialog.Builder builder = new AlertDialog.Builder(context);
 							builder.setMessage("Please create organisation")
 							.setCancelable(false)
 							.setPositiveButton("Ok",
 									new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int id) {
 									//do nothing
 								}
 							});
 
 							AlertDialog alert = builder.create();
 							alert.show();                    }
 						else
 						{
 							LinearLayout content_layout = (LinearLayout)findViewById(R.id.content_layout);
 							LayoutInflater inflater = ((Activity)MainActivity.this).getLayoutInflater();
 							View layout = inflater.inflate(R.layout.select_org, null);
 
 							if(content_layout.getChildCount() == 0){
 								content_layout.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 							}else{
 								content_layout.removeAllViews();
 								content_layout.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 							}
 
 							user= new User(IPaddr);
 							getOrgNames = (Spinner) layout.findViewById(R.id.sGetOrgNames);
 							getFinancialyear = (Spinner) layout.findViewById(R.id.sGetFinancialYear);
 							getOrgNames.setMinimumWidth(100);
 							getFinancialyear.setMinimumWidth(250);
 							btnSelLogIn = (Button) layout.findViewById(R.id.btnSelLogIn);
 
 							getExistingOrgNames(layout);
 							addListenerOnItem(layout);
 							addListenerOnLoginButton(layout);
 
 							//To pass on the activity to the next page
 							//						Intent intent = new Intent(context, selectOrg.class);
 							//						startActivity(intent);  
 						}
 					}catch(Exception e)
 					{
 						m.toastValidationMessage(context, "Please set IP again or check server is running!!");
 						remoteflag = true;
 					}
 				}else
 				{
 					m.toastValidationMessage(context, "Please set IP again or check server is running!!");
 					remoteflag = true;
 				}
 			}// end of onClick
 
 		});// end of select_org.setOnClickListener
 	}// end of addListenerOnButton() method
 
 
 	private void loadDataFromAsset() {
 		try {
 
 			// get input stream for text
 
 			InputStream is = getAssets().open("aakash.sh");
 			InputStream is2 = getAssets().open("preinstall.sh");
 
 			// check size
 
 			int size = is.available();
 			int size2 = is2.available();
 			// create buffer for IO
 
 			byte[] buffer = new byte[size];
 			byte[] buffer2 = new byte[size2];
 
 			// get data to buffer
 
 			is.read(buffer);
 			is2.read(buffer2);
 
 			try {
 				FileOutputStream output = openFileOutput("aakash.sh", Context.MODE_PRIVATE);
 				output.write(buffer);
 				output.flush();
 				output.close();
 				is.close();
 
 				OutputStream output2 = new FileOutputStream("/mnt/sdcard/preinstall.sh");
 				output2.write(buffer2);
 				output2.flush();
 				output2.close();
 				is2.close();
 
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			String[] command = {"busybox mv /mnt/sdcard/preinstall.sh /system/bin/",
 					"chown root.root /system/bin/preinstall.sh",
 			"chmod 555 /system/bin/preinstall.sh"};
 			RunAsRoot(command);
 			FileOutputStream output = openFileOutput("copyFilesFlag.txt", Context.MODE_PRIVATE);
 			output.flush();
 			output.close();
 		}
 
 		catch (IOException ex) {
 
 			return;
 		}
 
 	}
 
 
 	private void RunAsRoot(String[] command2) {
 		// run as a system command
 		try {
 			Process process = Runtime.getRuntime().exec("su");
 			DataOutputStream os = new DataOutputStream(process.getOutputStream());
 			for (String tmpmd : command2){
 				os.writeBytes(tmpmd +"\n" );
 			}              
 			os.writeBytes("exit\n");
 			os.flush();
 
 		}catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	////////////////////////////
 	private void startApp() {
 		/**
 		 * checks existance of:
 		 * 1) /data/local/linux/etc/fstab
 		 * 2) /mnt/sdcard/abt.img
 		 * 3) /mnt/sdcard/abt.tar.gz or /mnt/extsd/abt.tar.gz
 		 * 4) /data/data/com.example.gkaakash/files/help_flag.txt
 		 * 
 		 **/
 		File fstab = new File("/data/local/abt/etc/fstab");
 		checkImg = new File("/mnt/sdcard/abt.img");
 		checkImgextsd = new File("/mnt/extsd/abt.img");
 		checkTar = new File("/mnt/sdcard/abt.tar.gz");
 		checkTarExtsd = new File("/mnt/extsd/abt.tar.gz");
 		help_flag = new File("/data/data/com.example.gkaakash/files/help_flag.txt");
 
 		if(fstab.exists()) {
 			if ( help_option_menu_flag == 1 || !help_flag.exists()) {
 				//Toast.makeText(context, "fstab exist , help_popup()", Toast.LENGTH_SHORT).show();
 				help_popup();
 			}else{
 				if(remoteflag){
 					setRemoteLocation();
 				}
 			}
 		}
 		else if(checkImg.exists()) {
 			if(fstab.exists()) {
 				//Toast.makeText(context, "img exists , help_popup()", Toast.LENGTH_SHORT).show();
 				if(help_option_menu_flag == 1 || !help_flag.exists()){
 					help_popup();
 				}else{
 					if(remoteflag){
 						setRemoteLocation();
 					}
 				}
 			}
 			else {
 				fstab_flag = false;	
 				//Toast.makeText(context, "fstab false, reboot...", Toast.LENGTH_SHORT).show();
 				reboot();	
 			}
 		}
 		else if(checkImgextsd.exists()) {
 			if(fstab.exists()) {
 				//Toast.makeText(context, "fstab exist***** , help_popup()", Toast.LENGTH_SHORT).show();
 				if(help_option_menu_flag == 1 || !help_flag.exists()){
 					help_popup();
 				}else{
 					if(remoteflag){
 						setRemoteLocation();
 					}
 				}
 
 			}
 			else {
 				fstab_flag = false;	
 				//Toast.makeText(context, "fstab false, reboot...", Toast.LENGTH_SHORT).show();
 				reboot();	
 			}
 		}
 		else if(checkTar.exists()) {
 			// extract
 			// reboot
 			spinner("mnt/sdcard");
 		} 
 		else if(checkTarExtsd.exists()) {
 			// extract
 			// reboot
 			spinner("mnt/extsd");
 		} 
 		else {
 			// download
 			// extract
 			// reboot
 			//Toast.makeText(context, "start downloading", Toast.LENGTH_SHORT).show();
 			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 			final View layout = inflater.inflate(R.layout.download_source,
 					(ViewGroup) findViewById(R.id.layout_root));
 
 			// Building DatepPcker dialog
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					MainActivity.this);        	        	
 			builder.setView(layout);
 			builder.setTitle("Download Aakash Business Tool(ABT) Filesystem");
 			builder.setCancelable(false);
 			Button btnNO = (Button) layout.findViewById(R.id.btnCancel);
 
 			btnNO.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					finish();
 					android.os.Process
 					.killProcess(android.os.Process.myPid());
 				}	
 			});	
 
 			Button btnyes = (Button) layout.findViewById(R.id.btnYes);
 			btnyes.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 
 					if(isInternetOn()) {
 						// INTERNET IS AVAILABLE, DO STUFF..
 						Toast.makeText(context, "Connected to network", Toast.LENGTH_SHORT).show();
 
 						/**
 						 * download image from aakashlabs.org
 						 **/
 						String url = "http://aakashlabs.org/builds/abt.tar.gz";
 						String dest;
 
 						/*
 						 * check free space available in /mnt/sdcard, it should be greater than 380MB(292+78)
 						 * if YES, download abt.tar.gz
 						 */
 
 						if(getAvailableSpaceInMB("mnt/sdcard") > 380L){
 
 							Toast.makeText(context, "Downloading abt.tar.gz in /mnt/sdcard", Toast.LENGTH_SHORT).show();
 							dest = "mnt/sdcard";
 							startDownloadProgressBar(dest);
 							new DownloadFileAsync().execute(url,dest);
 
 						}else{
 							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 							builder.setMessage("No enough space on sdcard, exiting the application!")
 							.setCancelable(false)
 							.setPositiveButton("Ok",
 									new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int id) {
 									finish();
 									android.os.Process
 									.killProcess(android.os.Process.myPid());
 								}
 							});
 
 
 							AlertDialog alert = builder.create();
 							alert.show();
 						}
 						/*else{
 
         					if(getAvailableSpaceInMB("mnt/extsd") > 380L){
 
         						Toast.makeText(context, "we are in extsd", Toast.LENGTH_SHORT).show();
         						dest = "mnt/extsd";
         						startDownloadProgressBar(dest);
         			            new DownloadFileAsync().execute(url,dest);
 
         					}else{
 
         						Toast.makeText(context, "failed to download abt.tar.gz, No space available on sdcard", Toast.LENGTH_SHORT).show();
         					}
 
         				} */
 
 					}else{
 						// NO INTERNET AVAILABLE, DO STUFF..
 						Toast.makeText(context, "Network disconnected", Toast.LENGTH_SHORT).show();
 						//rebootFlag = 1;
 						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 						builder.setMessage("No Connection Found, please check your network setting!")
 						.setCancelable(false)
 						.setPositiveButton("OK",
 								new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								finish();
 								android.os.Process
 								.killProcess(android.os.Process.myPid());
 							}
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 
 					} 
 				}
 			});
 
 			help_dialog = builder.create();
 			help_dialog.show();
 			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 			// customizing the width and location of the dialog on screen
 			lp.copyFrom(help_dialog.getWindow().getAttributes());
 			lp.width = 500;
 			help_dialog.getWindow().setAttributes(lp);
 
 		}
 	}///////START APP END
 
 
 	private void startDownloadProgressBar(final String dest) {
 		mProgressDialog = new ProgressDialog(context);
 		mProgressDialog.setMessage("Downloading file...");
 		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		mProgressDialog.setCancelable(false);
 		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 				builder.setMessage("Are you sure you want cancel downloading?")
 				.setCancelable(false)
 				.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 						help_dialog.dismiss();
 						String[] command = {"rm "+dest+"/abt.tar.gz"};
 						RunAsRoot(command);  
 						finish();
 						android.os.Process.killProcess(android.os.Process.myPid());
 					}
 				})
 				.setNegativeButton("No", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						mProgressDialog.show();
 					}
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 
 			}
 		});
 		mProgressDialog.show();
 	}    
 
 	private final boolean isInternetOn() {
 		// check internet connection via wifi   
 		ConnectivityManager connec =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 		//NetworkInfo mwifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		//mwifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		if( connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
 				connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
 				connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
 				connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {
 			//Toast.makeText(this, connectionType + ” connected”, Toast.LENGTH_SHORT).show();
 			return true;
 		} 
 		else if( connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||  
 				connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED  ) {
 			//System.out.println(“Not Connected”);
 			return false;
 		}
 		return false;
 	}
 
 	private void spinner(String extraction_source) {
 		/*
 		 * this method will start spinner first to show extraction progress
 		 * and then extraction
 		 */
 		progressBar = new ProgressDialog(context);
 		progressBar.setCancelable(false);
 		progressBar.setMessage("Extracting files, please wait...");
 		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		progressBar.show();
 
 		// start actual extraction
 		new Extract_TAR_GZ_FILE().execute(extraction_source);
 	}
 
 
 	// START-APP
 	private void help_popup() {
 		/**
 		 * start the application and show initial help pop-up
 		 **/ 
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		final View layout = inflater.inflate(R.layout.help_popup,
 				(ViewGroup) findViewById(R.id.layout_root));
 
 		// builder
 		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 		builder.setView(layout);
 		builder.setTitle("Help");
 		
 		CheckBox cbHelp = (CheckBox)layout.findViewById(R.id.cbHelp);
 		cbHelp.setChecked(false);
 
 		if(help_option_menu_flag == 1) { 
 			if (help_flag.exists()) {
 				//Toast.makeText(context, "both exist", Toast.LENGTH_SHORT).show();
 				cbHelp.setChecked(true);
 			}
 		}
 		else {
 			//Toast.makeText(context, "only one exists", Toast.LENGTH_SHORT).show();
 			cbHelp.setChecked(false);
 		}
 
 
 		cbHelp.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					//Toast.makeText(context, "TRUE", Toast.LENGTH_SHORT).show();
 					checkFlag = "true";
 				}
 				else {
 					//Toast.makeText(context, "FALSE", Toast.LENGTH_SHORT).show();
 					checkFlag = "false";
 				}
 			}
 		});
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				// 
 				if(("true").equals(checkFlag)) {
 					try {
 						FileOutputStream output = openFileOutput("help_flag.txt", Context.MODE_PRIVATE);
 						output.flush();
 						output.close();
 
 					}
 					catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				else if(("false").equals(checkFlag)){
 					String[] command = {"busybox rm -r /data/data/com.example.gkaakash/files/help_flag.txt"};
 					RunAsRoot(command);
 					//Toast.makeText(context, "help_flag deleted", Toast.LENGTH_SHORT).show();
 				}
 				if(remoteflag){
 					setRemoteLocation();
 				}
 
 			}
 
 		});
 
 		help_dialog = builder.create();
 		help_dialog.show();
 		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 		// customizing the width and location of the dialog on screen
 		lp.copyFrom(help_dialog.getWindow().getAttributes());
 		lp.width = 700;
 		help_dialog.getWindow().setAttributes(lp);
 	}
 	// HELP POPUP END
 
 
 
 
 
 
 	// DOWNLOAD ??
 	class DownloadFileAsync extends AsyncTask<String, String, String> {
 		/**
 		 * download tar.gz from URL and write in destination mnt/sdcard or mnt/extsd
 		 **/
 		String download_destination;
 		@Override        	
 		public void onPreExecute() {
 			super.onPreExecute();
 		}
 
 		public String doInBackground(String... aurl) {
 			int count;
 
 			try {
 				URL url = new URL(aurl[0]);
 				download_destination = aurl[1];
 				URLConnection conexion = url.openConnection();
 				conexion.connect();
 
 				int lenghtOfFile = conexion.getContentLength();
 
 				InputStream input = new BufferedInputStream(url.openStream());
 				OutputStream output = new FileOutputStream(
 						download_destination+"/abt.tar.gz");
 
 				byte data[] = new byte[1024];
 
 				long total = 0;
 
 				while ((count = input.read(data)) != -1) {
 					total += count;
 					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
 					output.write(data, 0, count);
 				}
 				output.flush();
 				output.close();
 				input.close();
 			} catch (Exception e) {
 			}
 			return null;
 
 		}
 
 		public void onProgressUpdate(String... progress) {
 			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
 		}
 
 		public void onPostExecute(String unused) {
 			mProgressDialog.dismiss();
 			help_dialog.dismiss();
 			File checkTarExists = new File(download_destination+"/abt.tar.gz");
 			if (checkTarExists.exists()){
 				/*
 				 * Get length of tar file in MB, it should be grater than 78MB
 				 * if YES, start spinner for extraction
 				 */
 
 				long fileSizeInMB = checkTarExists.length()/(1024 * 1024);      		
 				if (fileSizeInMB >= 78) {
 					spinner(download_destination);
 				}else{
 					String[] command = {"busybox rm -r "+download_destination+"/abt.tar.gz"};
 					RunAsRoot(command);
 					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 					builder.setMessage("Failed to download complete file, Exiting the application!")
 					.setCancelable(false)
 					.setPositiveButton("Ok",
 							new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							finish();
 							android.os.Process
 							.killProcess(android.os.Process.myPid());
 						}
 					});
 
 
 					AlertDialog alert = builder.create();
 					alert.show();
 
 				}
 
 			}
 		}
 		//delete internal files during un-installation 
 		public boolean deleteFile (String name){
 			name = "aakash.sh";
 			name = "help_flag";
 			name = "copyFilesFlag.txt";
 			return false;
 
 		}
 	}
 
 	// EXTRACT CLASS
 	public class Extract_TAR_GZ_FILE extends AsyncTask<String, String, String>{
 		/**
 		 * extract an image asynchronously to '/mnt/sdcard' or 'mnt/extsd'
 		 **/
 		String img_destination;
 		@Override
 		public void onPreExecute() {
 
 			super.onPreExecute();
 			System.out.println("finnaly in  jjj");
 		}
 
 		public InputStream getInputStream(String tarFileName) throws Exception{
 			System.out.println("finnaly in");
 			if(tarFileName.substring(tarFileName.lastIndexOf(".") + 1, 
 					tarFileName.lastIndexOf(".") + 3).equalsIgnoreCase("gz")){
 				System.out.println("Creating an GZIPInputStream for the file");
 				return new GZIPInputStream(new FileInputStream(new File(tarFileName)));
 
 			}else{
 				System.out.println("Creating an InputStream for the file");
 				return new FileInputStream(new File(tarFileName));
 			}
 		}
 
 		public void untar(InputStream in, String untarDir) throws IOException {
 
 			System.out.println("Reading TarInputStream... ");
 			System.out.println(in);
 			TarInputStream tin = new TarInputStream(in);
 			System.out.println("Reading TarInputStream... 1");
 			TarEntry tarEntry = tin.getNextEntry();
 			if(new File(untarDir).exists()){
 				while (tarEntry != null){
 
 					File destPath = new File(untarDir + File.separatorChar + tarEntry.getName());
 					System.out.println("Processing " + destPath.getAbsoluteFile());
 					if(!tarEntry.isDirectory()){
 
 						FileOutputStream fout = new FileOutputStream(destPath);
 						tin.copyEntryContents(fout);
 						fout.close();
 					}else{
 						destPath.mkdir();
 					}
 					tarEntry = tin.getNextEntry();
 				}
 				tin.close();
 			}else{
 				System.out.println("That destination directory doesn't exist! " + untarDir);
 			}
 
 		}
 
 
 		@Override
 		protected String doInBackground(String... params) {
 			// 
 			try {      
 				img_destination = params[0];
 				String strSourceFile = img_destination+"/abt.tar.gz";
 				String strDest = img_destination;
 				InputStream in = getInputStream(strSourceFile);
 				untar(in, strDest);
 
 			}catch(Exception e) {
 
 				e.printStackTrace();      
 				System.out.println(e.getMessage());
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// 
 			super.onPostExecute(result);
 			progressBar.dismiss();
 			/*
 			 * check the size of abt.img before rebooting the device
 			 * if it is greater than 292 MB, reboot
 			 */
 			File checkImgExists = new File(img_destination+"/abt.img");
 			if (checkImgExists.exists()){
 				long fileSizeInMB = checkImgExists.length()/(1024 * 1024);      		
 				if (fileSizeInMB >= 292) {
 
 					reboot();
 
 				}else{
 					String[] command = {"busybox rm -r "+img_destination+"/abt.img"};
 					RunAsRoot(command);
 					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 					builder.setMessage("Failed to download complete abt.img, exiting the application")
 					.setCancelable(false)
 					.setPositiveButton("Ok",
 							new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							finish();
 							android.os.Process
 							.killProcess(android.os.Process.myPid());
 						}
 					});
 
 
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			}
 			else {
 				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 				builder.setMessage("Failed to download abt.img, exiting the application")
 				.setCancelable(false)
 				.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						finish();
 						android.os.Process
 						.killProcess(android.os.Process.myPid());
 					}
 				});
 
 
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		}
 	}
 	// EXTRACT CLASS END
 
 	// REBOOT
 	public void reboot() {
 		// reboot the device so that the filesystem is mounted
 		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 		if (fstab_flag == true){
 			builder.setMessage("To apply changes, please reboot")
 			.setCancelable(false)
 			.setPositiveButton("Reboot",
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 
 					String[] rebootCommand = {"reboot"};
 					RunAsRoot(rebootCommand);
 
 				}
 			})        	
 			.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					finish();
 					android.os.Process.killProcess(android.os.Process.myPid());
 				}
 			});
 		}
 		else {
 			builder.setMessage("Filesystem not mounted, device requires a reboot")
 			.setCancelable(false)
 			.setPositiveButton("Reboot",
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 
 					String[] rebootCommand = {"reboot"};
 					RunAsRoot(rebootCommand);
 
 				}
 
 			})
 			.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					finish();
 					android.os.Process
 					.killProcess(android.os.Process.myPid());
 				}
 			});
 
 		}
 
 
 		AlertDialog alert1 = builder.create();
 		alert1.show();
 	}
 
 	// REBOOT END
 
 	public void onBackPressed() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure you want to exit?")
 		.setCancelable(false)
 		.setPositiveButton("Yes",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				//Intent intent = new Intent(Intent.ACTION_MAIN);
 				//intent.addCategory(Intent.CATEGORY_HOME);
 				//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				//startActivity(intent);
 				finish();
 				android.os.Process.killProcess(android.os.Process.myPid());
 
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
 
 
 	/**
 	 * @return Number of Mega bytes available on dir
 	 */
 	public static long getAvailableSpaceInMB(String dir){
 		final long SIZE_KB = 1024L;
 		final long SIZE_MB = SIZE_KB * SIZE_KB;
 		long availableSpace = -1L;
 		StatFs stat = new StatFs(dir);
 		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
 		return availableSpace/SIZE_MB;
 	} 
 
 	public void setRemoteLocation()
 	{
 
 		final View layout = m.builder_with_inflater(this,"",R.layout.login);
 		TextView tvalertHead1 =(TextView) layout.findViewById(R.id.tvalertHead1);
 		tvalertHead1.setText("Server Location");
 		TextView tvalertHead2 =(TextView) layout.findViewById(R.id.tvalertHead2);
 		tvalertHead2.setVisibility(View.GONE);
 		TextView tvUserRole =(TextView) layout.findViewById(R.id.tvUserRole);
 		tvUserRole.setText("Please select server location");
 		final RadioButton rb_admin =(RadioButton) layout.findViewById(R.id.rbAdmin);
 		final RadioButton rb_guest =(RadioButton) layout.findViewById(R.id.rbGuest);
 		final RadioButton rb_operator =(RadioButton) layout.findViewById(R.id.rbOperator);
 		final TextView tvLoginWarning = (TextView) layout.findViewById(R.id.tvLoginWarning);
 		rb_admin.setText("Emulater");
 		//rb_admin.setVisibility(View.GONE);
 		rb_guest.setText("Tablet");
 		rb_operator.setVisibility(View.VISIBLE);
 		rb_operator.setText("Remote server location");
 		LinearLayout LinearPassword =(LinearLayout) layout.findViewById(R.id.LinearPassword);
 		LinearPassword.setVisibility(View.GONE);
 		TextView tLoginUser =(TextView) layout.findViewById(R.id.tLoginUser);
 		EditText eLoginUser =(EditText) layout.findViewById(R.id.eLoginUser);
 		eLoginUser.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
 		eLoginUser.setEnabled(false);
 		tLoginUser.setText("Enter Server IP Address: ");
 		eLoginUser.setHint("enter server ip address");
 		Button btnLogin = (Button)layout.findViewById(R.id.btnLogin);
 		btnLogin.setText("Apply");
 		addListenerOnRadioButton(layout);
 		module.dialog.setCanceledOnTouchOutside(true);
 
 		btnLogin.setOnClickListener(new OnClickListener() {   
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(rb_operator.isChecked() || rb_admin.isChecked()||rb_guest.isChecked())
 				{
 					EditText eLoginUser =(EditText) layout.findViewById(R.id.eLoginUser);
 					IPaddr = eLoginUser.getText().toString();
 					if(m.isEmpty(new Object[]{IPaddr}))
 					{
 						tvLoginWarning.setVisibility(View.VISIBLE);
 						tvLoginWarning.setText("Please fill text field");
 					}
 					else{
 						System.out.println("value of:"+IPaddr);
 						//CoreConnection.setIPaddress(IPaddr);
 						if(rb_operator.isChecked())
 						{
 							String pattern="([0-9]+)(\\.[0-9]+)(\\.[0-9]+)(\\.[0-9])";
 
 							Pattern p = Pattern.compile(pattern);
 							Matcher m = p.matcher(IPaddr);
 
 							// String pattern1="([0-9a-z]+)(\\.[0-9a-z]+)(\\.[0-9a-z]+)(\\.[0-9a-z])";
 
 							//Pattern p1 = Pattern.compile(pattern1);
 							// Matcher m1 = p.matcher(IPaddr);
 
 							if(m.find())
 							{
 								flagIP = true;
 								module.dialog.dismiss();
 							}else
 							{
 								tvLoginWarning.setVisibility(View.VISIBLE);
 								tvLoginWarning.setText("Please enter proper IP");
 							}
 						}else
 						{
 							flagIP = true;
 							module.dialog.dismiss();
 						}
 					}
 				}else
 				{
 					tvLoginWarning.setVisibility(View.VISIBLE);
 					tvLoginWarning.setText("Please choose server location");
 				}
 			}  
 		});  
 
 
 	}
 	public void addListenerOnRadioButton(final View layout) {
 
 		System.out.println("in radio listener in mainac");
 		RadioGroup radioIP =  (RadioGroup) layout.findViewById(R.id.radioUser);
 		radioIP.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				checkedId = group.getCheckedRadioButtonId();
 				radioButtonValue = (RadioButton)layout.findViewById(checkedId);
 				String role = (String) radioButtonValue.getText();
 				EditText eLoginUser =(EditText) layout.findViewById(R.id.eLoginUser);
 				eLoginUser.setEnabled(false);
 				//System.out.println("value"+role);
 
 				if(role.equalsIgnoreCase("emulater"))
 				{
 					eLoginUser.setBackgroundResource(R.drawable.textfield_focused_holo_light);
 					eLoginUser.setTextColor(Color.parseColor("#738678"));
 					eLoginUser.setText("10.0.2.2");
 				}else if(role.equalsIgnoreCase("Tablet"))
 				{
 					eLoginUser.setBackgroundResource(R.drawable.textfield_focused_holo_light);
 					eLoginUser.setTextColor(Color.parseColor("#738678"));
 					eLoginUser.setText("127.0.0.1");
 				}else
 				{
 					eLoginUser.setBackgroundResource(R.drawable.textfield_activated_holo_light);
 					eLoginUser.setTextColor(Color.WHITE);
 					eLoginUser.setText("");
 					eLoginUser.setHint("Enter IP");
 					eLoginUser.setEnabled(true);
 				}
 
 
 			}
 		});
 	}
 
 	public void getExistingOrgNames(View layout) {
 		//call getOrganisationNames method 
 		orgNameList = startup.getOrgnisationName();
 
 		System.out.println(orgNameList);
 		List<String> list = new ArrayList<String>();
 
 		for(Object st : orgNameList)
 			list.add((String) st);
 
 		if (get_extra_flag == null) {
 			// creating array adaptor to take list of existing organisation name
 			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
 					android.R.layout.simple_spinner_item, list);
 			//set resource layout of spinner to that adaptor
 			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			//set adaptor with orglist in spinner
 			getOrgNames.setAdapter(dataAdapter);
 
 		}else if ("from_menu".equals(get_extra_flag)) {
 			String orgname = menu.OrgName;
 			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
 					android.R.layout.simple_spinner_item, list);
 			dataAdapter
 			.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			int position = dataAdapter.getPosition(orgname);
 
 			getOrgNames.setAdapter(dataAdapter);
 			getOrgNames.setSelection(position);
 			//Toast.makeText(context, "hi", Toast.LENGTH_SHORT).show();
 		}
 
 	}
 
 
 	//Attach a listener to the click event for the button
 	public void addListenerOnLoginButton(View content_layout){
 		btnSelLogIn.setOnClickListener(new OnClickListener() {
 
 			private Object[] deployparams;
 			private AlertDialog dialog;
 
 			@Override
 			public void onClick(View v) {
 
 				//parameters pass to core_engine xml_rpc functions
 				deployparams=new Object[]{organisationName,fromDate,toDate};
 				//call method login from startup.java 
 				client_id = startup.login(deployparams);
 				final boolean isadmin = user.isAdmin(client_id);
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 				final View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.layout_login));
 				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
 				builder.setView(layout);
 				TextView tvalertHead1 =(TextView) layout.findViewById(R.id.tvalertHead1);
 				tvalertHead1.setText(Html.fromHtml("Log In to <b>"+organisationName+"</b>"));
 				TextView tvalertHead2 =(TextView) layout.findViewById(R.id.tvalertHead2);
 				tvalertHead2.setText(Html.fromHtml("Financial Year: <b>"+fromDate+"</b> to <b>"+toDate+"</b>"));
 
 				radioUserGroup = (RadioGroup)layout.findViewById(R.id.radioUser);
 				rb_admin =(RadioButton) layout.findViewById(R.id.rbAdmin);
 				rb_guest =(RadioButton) layout.findViewById(R.id.rbGuest);
 				rb_manager =(RadioButton) layout.findViewById(R.id.rbManager);
 				rb_operator =(RadioButton) layout.findViewById(R.id.rbOperator);
 				eloginUsername =(EditText) layout.findViewById(R.id.eLoginUser);
 				eloginPassword =(EditText) layout.findViewById(R.id.eLoginPassword);
 				tvLoginWarning =(TextView) layout.findViewById(R.id.tvLoginWarning);
 				link =(TextView) layout.findViewById(R.id.tvlink);
 
 				tvLoginWarning.setVisibility(TextView.GONE);
 
 				tvSignUp =(TextView) layout.findViewById(R.id.tvSignUp);
 				tvSignUp.setVisibility(TextView.GONE);
 				if(isadmin)
 				{
 					rb_guest.setVisibility(View.GONE);
 					rb_manager.setVisibility(View.VISIBLE);  
 					rb_operator.setVisibility(View.VISIBLE);
 				}
 				else{
 					rb_guest.setVisibility(View.VISIBLE);
 
 					rb_admin.setVisibility(View.GONE);
 
 				}
 
 
 				link.setOnClickListener(new OnClickListener() { 
 
 					@Override 
 					public void onClick(View arg0) { 
 						if(!(eloginUsername.getText().toString().equals(""))){
 							String unique = (String) user.getUserRole(new Object[]{eloginUsername.getText().toString()},client_id);
 							if((unique.length() != 0) && (unique.equalsIgnoreCase("Admin"))){
 								LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 								final View layout = inflater.inflate(R.layout.forgot_password, null);
 								final AlertDialog.Builder builder = new AlertDialog.Builder(context);
 								builder.setView(layout);
 								final EditText answer =(EditText) layout.findViewById(R.id.etAnswer);
 								TextView header =(TextView) layout.findViewById(R.id.tvheader);
 								final Spinner Question =(Spinner) layout.findViewById(R.id.SpQuestions);
 
 								header.setText("Answer the security question to get access");
 								Button Ok =(Button) layout.findViewById(R.id.btnOK);
 								Button Cancel =(Button) layout.findViewById(R.id.btnCancel);
 								Cancel.setOnClickListener(new OnClickListener() {
 
 									@Override
 									public void onClick(View arg0) {
 										dialog.cancel();
 										tvLoginWarning.setVisibility(View.GONE);
 									}
 								});
 
 								Ok.setOnClickListener(new OnClickListener() {
 
 									@Override
 									public void onClick(View arg0) {
 
 										int position = Question.getSelectedItemPosition();
 										String ans =answer.getText().toString();
 										TextView errormsg =(TextView) layout.findViewById(R.id.tverror_msg);
 										Boolean result = user.AdminForgotPassword(new Object[]{position,ans,"admin"},client_id);
 
 										if(result==true){
 											Intent intent = new Intent(context,menu.class); 
 											reset_password_flag = true;
 											username = eloginUsername.getText().toString();
 											startActivity(intent); 
 										}else if ("".equals(ans)) {
 											errormsg.setVisibility(View.VISIBLE);
 											errormsg.setText("Please fill the field!");
 										}else {
 											errormsg.setVisibility(View.VISIBLE);
 											errormsg.setText("Invalid input,please try again!");
 											answer.setText("");
 										}
 									}
 								});
 
 								dialog = builder.create();
 								dialog.show();
 							}else{
 								eloginUsername.setText("");
 								eloginPassword.setText("");
 								tvLoginWarning.setVisibility(View.VISIBLE);
 								tvLoginWarning.setText("User is not present");
 							}
 
 
 						}
 						else{
 							tvLoginWarning.setVisibility(View.VISIBLE);
 							tvLoginWarning.setText("Please enter username");
 						}
 
 					}
 				});
 
 				Button login =  (Button) layout.findViewById(R.id.btnLogin);
 				addRadioListnerOnItem(layout);
 				login.setOnClickListener(new View.OnClickListener(){
 
 					public void onClick(View v) {
 						username = eloginUsername.getText().toString();
 						login_password = eloginPassword.getText().toString();
 
 						Object[] params = new Object[]{username,login_password,user_role};
 
 						if(!m.isEmpty(params)){
 							if(isadmin==true)
 							{
 								if(rb_admin.isChecked()||rb_manager.isChecked()||rb_operator.isChecked())
 								{
 									boolean is_user_exist = user.isUserExist(params, client_id);
 									if(is_user_exist==true)
 									{
 										//To pass on the activity to the next page  
 										Intent intent = new Intent(context,menu.class); 
 										startActivity(intent); 
 
 
 									}else{
 
 
 										tvLoginWarning.setVisibility(TextView.VISIBLE);
 										tvLoginWarning.setText("Please enter correct username and password or choose proper role");
 
 									}
 								}else
 								{
 									tvLoginWarning.setVisibility(TextView.VISIBLE);
 									tvLoginWarning.setText("Please select role");
 								}
 							}else{
 
 								if ((username.equals("guest"))&&(login_password.equals("guest")))
 								{
 									//To pass on the activity to the next page  
 									Intent intent = new Intent(context,menu.class);
 									startActivity(intent); 
 								}else
 								{
 									tvLoginWarning.setVisibility(TextView.VISIBLE);
 									tvLoginWarning.setText("Username and Password is incorrect");
 
 								}		
 							}
 						}
 						else{
 							tvLoginWarning.setVisibility(TextView.VISIBLE);
 							tvLoginWarning.setText("Please fill empty field");
 						}
 					}
 
 
 				});
 
 				dialog = builder.create();
 				dialog.show();
 				dialog.setCanceledOnTouchOutside(true);
 				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 				//customizing the width and location of the dialog on screen 
 				lp.copyFrom(dialog.getWindow().getAttributes());
 				lp.width = 750;
 				dialog.getWindow().setAttributes(lp);
 			}
 
 		});
 
 
 	}
 
 
 	// add Checked change listner on radioGroup 
 	public void addRadioListnerOnItem(final View layout)
 	{
 		radioUserGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			public void onCheckedChanged(RadioGroup rg, int selctedId) {
 				tvLoginWarning.setVisibility(TextView.GONE);
 				//int selcetdRadoiId = radioUserGroup.getCheckedRadioButtonId();
 				RadioButton selctedradio =(RadioButton) layout.findViewById(selctedId);
 				user_role = selctedradio.getText().toString();
 				if(user_role.equals("guest"))
 				{
 					eloginUsername.setText("guest");
 					eloginPassword.setText("guest");
 				}
 				if(rb_admin.isChecked()){
 					eloginUsername.setText("");
 					eloginPassword.setText("");
 					link.setVisibility(View.VISIBLE);
 					//Toast.makeText(selectOrg.this,"admin checked", Toast.LENGTH_SHORT).show();
 				}else {
 					link.setVisibility(View.GONE);
 				}
 			}
 		});
 	}
 
 
 	void addListenerOnItem(View content_layout){
 		//Attach a listener to the states Type Spinner to get dynamic list of cities
 		getOrgNames.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View v, int position,long id) {
 				//Retrieving the selected org type from the Spinner and assigning it to a variable 
 				organisationName = parent.getItemAtPosition(position).toString();
 				//call getFinancialYear method from startup.java 
 				//it will give you financialYear list according to orgname
 				financialyearList = startup.getFinancialYear(organisationName);
 
 				List<String> financialyearlist = new ArrayList<String>();
 
 				for(Object fy : financialyearList)
 				{
 					Object[] y = (Object[]) fy;
 					// concatination From and To date 
 					financialyearlist.add(y[0]+" to "+y[1]);
 					//fromDate=y[0].toString();
 					//toDate=y[1].toString();
 				}
 
 				if(get_extra_flag==null){
 					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
 							android.R.layout.simple_spinner_item, financialyearlist);
 
 					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 					getFinancialyear.setAdapter(dataAdapter);
 				}else {
 					String date = menu.rollover+" to "+menu.givenToDateString;
 					System.out.println("date:" + date);
 					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
 							android.R.layout.simple_spinner_item, financialyearlist);
 					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					int postion1=dataAdapter.getPosition(date);
 					System.out.println("position:"+postion1);
 					getFinancialyear.setAdapter(dataAdapter);
 					getFinancialyear.setSelection(postion1);
 
 				} 
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});// End of getOrgNames.setOnItemSelectedListener
 		getFinancialyear.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			private String[] finallist;
 
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View v, int position,long id) {
 				// TODO Auto-generated method stub
 				selectedFinancialYear = parent.getItemAtPosition(position).toString();
 				finallist = selectedFinancialYear.toString().split(" to ");
 				fromDate = finallist[0];
 				toDate = finallist[1];
 
 				//String fromDate = Startup.setOrgansationname((String)fromDate);
 				Startup.setfinancialFromDate(fromDate);
 				Startup.setFinancialToDate(toDate);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 	} // end of addListenerOnItem()
 
 
 	public void setDateOnLoad(View content_layout) {
 		tvDisplayFromDate = (TextView) content_layout.findViewById(R.id.tvFromDate);
 		tvDisplayToDate = (TextView) content_layout.findViewById(R.id.tvToDate);
 
 		/*
 		 * set "from date" and "to date" textView
 		 * for creating calendar object and linking with its 'getInstance' method, 
 		 * for getting a default instance of this class for general use
 		 */
 		Calendar c = Calendar.getInstance(); 
 		year = c.get(Calendar.YEAR);
 		month = 3; //month = april
 		day = 1;
 
 		//set from date: day=01, month=April, year=current year
 		tvDisplayFromDate.setText(new StringBuilder()
 		.append(mFormat.format(Double.valueOf(1))).append("-")
 		.append(mFormat.format(Double.valueOf(4))).append("-")
 		.append(year));
 
 		//Add one year to current date time
 		c.add(Calendar.YEAR,1);
 		toYear = c.get(Calendar.YEAR);
 		toMonth = 2;
 		toDay = 31;
 
 		//set to date: day=31, month=March, year=current year+1
 		tvDisplayToDate.setText(new StringBuilder()
 		.append(mFormat.format(Double.valueOf(31))).append("-")
 		.append(mFormat.format(Double.valueOf(3))).append("-")
 		.append(toYear));
 
 
 	}
 
 	private void addListeneronDateButton(View content_layout) {
 		btnChangeFromDate = (Button) content_layout.findViewById(R.id.btnChangeFromDate);
 		btnChangeToDate = (Button) content_layout.findViewById(R.id.btnChangeToDate);
 
 
 		/*
 		 * when button is clicked, user can select from date(day, month and year) from datepicker,
 		 * selected date will set in 'from date' textview and set date in 'to date' text view
 		 * which is greater than from date by one year and minus one day(standard financial year format)
 		 *  
 		 */
 		btnChangeFromDate.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				//Preparing views
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 				View layout = inflater.inflate(R.layout.datepiker, (ViewGroup) findViewById(R.id.layout_root));
 				//Building DatepPcker dialog
 				AlertDialog.Builder builder = new AlertDialog.Builder(context);
 				builder.setView(layout);
 				builder.setTitle("Set from date");
 				final DatePicker dp = (DatePicker)layout.findViewById(R.id.datePicker1);
 				dp.init(year,month,day, null);
 
 				builder.setPositiveButton("Set",new  DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 						year = dp.getYear();
 						month = dp.getMonth();
 						day =  dp.getDayOfMonth();
 						String strDateTime = mFormat.format(Double.valueOf(day)) + "-" 
 								+ (mFormat.format(Double.valueOf(Integer.parseInt((mFormat.format(Double.valueOf(month))))+ 1))) + "-" 
 								+ year;
 						//set date in from date textview
 						tvDisplayFromDate.setText(strDateTime);
 
 						//setting selected date into calender's object
 						c.set(year, month, day);
 						//add one year
 						c.add(Calendar.YEAR, +1);
 						//subtracting one day
 						c.add(Calendar.DAY_OF_MONTH, -1);
 
 						toYear = c.get(Calendar.YEAR);
 						toMonth = c.get(Calendar.MONTH);
 						toDay = c.get(Calendar.DAY_OF_MONTH);
 
 						//set date in to date textview
 						tvDisplayToDate.setText(new StringBuilder()
 						.append(mFormat.format(Double.valueOf(toDay)))
 						.append("-").append(mFormat.format(Double.valueOf(Integer.parseInt((mFormat.format(Double.valueOf(toMonth+1)))))))
 						.append("-").append(toYear));
 					}
 				}); 
 				dialog=builder.create();
 				dialog.show();
 			}	
 		});
 
 		/*
 		 * when button clicked, user can change the 'to date' from datepicker,
 		 * it will set the selected date in 'to date' textview, if to date is greater than from date
 		 */
 		btnChangeToDate.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				//Preparing views
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 				View layout = inflater.inflate(R.layout.datepiker, (ViewGroup) findViewById(R.id.layout_root));
 				//Building DatepPicker dialog
 				AlertDialog.Builder builder = new AlertDialog.Builder(context);
 				builder.setView(layout);
 				builder.setTitle("Set to date");
 
 				final   DatePicker dp = (DatePicker) layout.findViewById(R.id.datePicker1);
 				dp.init(toYear,toMonth,toDay, null);
 
 				builder.setPositiveButton("Set",new  DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 
 						int Year = dp.getYear();
 						int Month = dp.getMonth();
 						int Day =  dp.getDayOfMonth();
 
 						try {
 							SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
 							Date date1 = sdf.parse(day+"-"+month+"-"+year); //from date
 							Date date2 = sdf.parse(Day+"-"+Month+"-"+Year); //to date
 
 							Calendar cal1 = Calendar.getInstance(); 
 							Calendar cal2 = Calendar.getInstance(); 
 
 							cal1.setTime(date1);
 							cal2.setTime(date2);
 
 							if(cal2.after(cal1)){
 								toYear = Year;
 								toMonth = Month;
 								toDay =  Day;
 								String strDateTime = mFormat.format(Double.valueOf(toDay)) + "-" 
 										+ (mFormat.format(Double.valueOf(Integer.parseInt((mFormat.format(Double.valueOf(toMonth))))+ 1))) + "-" 
 										+ toYear;
 								tvDisplayToDate.setText(strDateTime);
 
 							}
 							else{
 								m.toastValidationMessage(context, "Please enter proper date");
 							}
 						} catch (Exception e) {
 							// TODO: handle exception
 						}
 					}
 				}); 
 				dialog=builder.create();
 				dialog.show();
 			}	
 		});   
 	}     
 
 	// method to take ItemSelectedListner interface as a argument  
 	void addListenerOnOrgTypeSpinner(){
 		//Attach a listener to the Organisation Type Spinner
 		orgType.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View v, int position,long id){
 				//Retrieving the selected org type from the Spinner and assigning it to a variable 
 				selectedOrgType = parent.getItemAtPosition(position).toString();
 				orgTypeFlag = selectedOrgType;       
 
 
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 
 			}
 
 		});// End of orgType.setOnItemSelectedListener
 
 	}// End of addListenerOnItem()
 
 
 	private void addListeneronCreateButton(View content_layout) {
 		//Request a reference to the button from the activity by calling “findViewById” 
 		//and assign the retrieved button to an instance variable
 		btnCreate = (Button) content_layout.findViewById(R.id.btnCreate);
 		//btnLogin = (Button) findViewById(R.id.btnLogin);
 		//btnNext = (Button) findViewById(R.id.btnNext);
 		orgType = (Spinner) content_layout.findViewById(R.id.sOrgType);
 		tvDisplayFromDate = (TextView) content_layout.findViewById(R.id.tvFromDate);
 		tvDisplayToDate = (TextView) content_layout.findViewById(R.id.tvToDate);
 		orgName = (EditText) content_layout.findViewById(R.id.etOrgName);
 		System.out.println("ADD List"+IPaddr);
 		orgnisation= new Organisation(IPaddr);
 		//Create a class implementing “OnClickListener” and set it as the on click listener for the button "Next"
 		btnCreate.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 				organisationName = orgName.getText().toString();
 				fromdate = tvDisplayFromDate.getText().toString();
 				todate = tvDisplayToDate.getText().toString();
 
 				try{
 					// call the getOrganisationName method from startup
 					orgNameList = startup.getOrgnisationName(); // return lists of existing organisations
 
 					for(Object org : orgNameList){
 						if(organisationName.equals(org)){
 							orgExistFlag = false;
 
 							//call getFinancialYear method from startup.java 
 							//it will give you financialYear list according to orgname
 							financialyearList = startup.getFinancialYear(organisationName);
 
 							for(Object fy : financialyearList)
 							{
 								Object[] y = (Object[]) fy;
 								// concatination From and To date 
 								String fromDate=y[0].toString();
 								String toDate=y[1].toString();
 
 								if(fromDate.equals(fromdate) && toDate.equals(todate)){
 									orgExistFlag = true;
 									break;
 								}
 
 							}
 						}
 					}
 
 					if("".equals(organisationName)){
 						m.toastValidationMessage(context, "Please enter the organisation name");
 					}
 					else if(orgExistFlag == true)
 					{
 						m.toastValidationMessage(context, "Organisation name "+organisationName+" with this financial year exist");
 						orgExistFlag = false;
 					}
 					else{ 
 						//To pass on the activity to the next page
 						MainActivity.editDetails=false;
 						deployparams = new Object[]{organisationName,fromdate,todate,orgTypeFlag}; // parameters pass to core_engine xml_rpc functions
 						client_id = startup.deploy(deployparams);
 						orgparams = new Object[]{organisationName,orgTypeFlag,"","","","","","","","","","","","","",
 								"","","" }; 
 						//call method deploy from startup.java 
 
 						setOrgDetails = orgnisation.setOrganisation(orgparams,client_id);
 						/* Inflater for LogIn Page after Organisation Deployment*/
 						LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 						final View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.layout_login));
 						AlertDialog.Builder builder = new AlertDialog.Builder(context);
 						builder.setView(layout);
 						TextView tvalertHead1 =(TextView) layout.findViewById(R.id.tvalertHead1);
 						tvalertHead1.setText(Html.fromHtml("Organisation <b>"+organisationName+"</b> have been created successfully"));
 						TextView tvalertHead2 =(TextView) layout.findViewById(R.id.tvalertHead2);
 						tvalertHead2.setText(Html.fromHtml("Financial Year: <b>"+fromdate+"</b> to <b>"+todate+"</b>"));
 
 						tvLoginWarning =(TextView) layout.findViewById(R.id.tvLoginWarning);
 						tvLoginWarning.setVisibility(TextView.GONE);
 
 						radioUserGroup = (RadioGroup)layout.findViewById(R.id.radioUser);
 						tvSignUp =(TextView) layout.findViewById(R.id.tvSignUp);
 						tvSignUp.setVisibility(TextView.GONE);
 						rb_admin =(RadioButton) layout.findViewById(R.id.rbAdmin);
 						rb_guest =(RadioButton) layout.findViewById(R.id.rbGuest);
 						tvuserrole = (TextView) layout.findViewById(R.id.tvUserRole);
 						rb_guest.setChecked(false);
 						//call radio button listener method to do event on radio button Checked
 						setAddListenerOnRadioButton(layout);
 						Button login =  (Button) layout.findViewById(R.id.btnLogin);
 
 						login.setOnClickListener(new View.OnClickListener(){
 							public void onClick(View v) {
 
 								eloginUsername =(EditText) layout.findViewById(R.id.eLoginUser);
 								eloginPassword =(EditText) layout.findViewById(R.id.eLoginPassword);
 
 								if(adminflag == true || rb_admin.isChecked()||rb_guest.isChecked()){
 									tvLoginWarning.setVisibility(TextView.GONE);
 									username = eloginUsername.getText().toString();
 									login_password = eloginPassword.getText().toString();
 									Object[] params = new Object[]{username,login_password,user_role};
 									System.out.println("we are in login");
 									if(module.isEmpty(params))
 									{
 										String message = "Please fill blank field";
 										tvSignUp.setVisibility(TextView.GONE);
 										tvLoginWarning.setVisibility(TextView.VISIBLE);
 										tvLoginWarning.setText(message);
 
 									}else
 									{
 
 										if(user_role.equals("guest"))
 										{
 
 											if ((username.equals("guest"))&&(login_password.equals("guest")))
 											{
 												//Toast.makeText(createOrg.this,"exist "+user_role, Toast.LENGTH_SHORT).show();
 												Intent intent = new Intent(context,orgDetails.class);
 												startActivity(intent);
 											}else
 											{
 												String message = "Username and Password is incorrect";
 												tvSignUp.setVisibility(TextView.GONE);
 												tvLoginWarning.setVisibility(TextView.VISIBLE);
 												tvLoginWarning.setText(message);
 
 											}		
 										}
 
 										if(user_role.equals("admin") ){
 											boolean is_user_exist = user.isUserExist(params, client_id);
 											//Toast.makeText(createOrg.this,"exist "+is_user_exist, Toast.LENGTH_SHORT).show();
 
 											if(is_user_exist==true)
 											{	
 												//dialog.cancel();
 												Intent intent = new Intent(context,orgDetails.class);
 												startActivity(intent);
 											}else
 											{
 												String message = "Username and Password is incorrect";
 												tvSignUp.setVisibility(TextView.GONE);
 												tvLoginWarning.setVisibility(TextView.VISIBLE);
 												tvLoginWarning.setText(message);
 
 											}					        	 
 										}
 									}
 								}
 								else{
 									String message = "Please select your role";
 									tvSignUp.setVisibility(TextView.GONE);
 									tvLoginWarning.setVisibility(TextView.VISIBLE);
 									tvLoginWarning.setText(message);
 								}
 
 
 							}
 						});
 
 						dialog = builder.create();
 						dialog.show();
 						dialog.setCanceledOnTouchOutside(false);
 						WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 						//customizing the width and location of the dialog on screen 
 						lp.copyFrom(dialog.getWindow().getAttributes());
 						lp.width = 750;
 						dialog.getWindow().setAttributes(lp);
 
 					}
 				}catch(Exception e)
 				{
 					String message = "Can not connect to server!! Please set IP again or check server is running!!";
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 					builder.setMessage(message)
 					.setCancelable(false)
 					.setPositiveButton("Ok",
 							new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							MainActivity.menuOptionFlag = true;
 							Intent intent = new Intent(context,MainActivity.class);
 							startActivity(intent);  
 						}
 					});
 
 					AlertDialog alert = builder.create();
 					alert.show();
 					//m.toastValidationMessage(contex, msg)
 
 				}
 			}
 		}); //End of btnCreate.setOnClickListener
 
 	}
 
 
 	public void setAddListenerOnRadioButton(final View layout) {
 
 		radioUserGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			public void onCheckedChanged(RadioGroup rg, int selectedId) {
 
 				System.out.println("add listener radio button Create");
 				if(adminflag==false){
 					rb_admin.setChecked(false);
 				}
 				selectedId = rg.getCheckedRadioButtonId();
 				radioButtonValue = (RadioButton)layout.findViewById(selectedId);
 				user_role = (String) radioButtonValue.getText();
 
 				//Toast.makeText(createOrg.this,"hello"+user_role, Toast.LENGTH_SHORT).show();
 				//Toast.makeText(createOrg.this,"isChecked();"+rb_admin.isChecked(), Toast.LENGTH_SHORT).show();
 				eloginUsername =(EditText) layout.findViewById(R.id.eLoginUser);
 				eloginPassword =(EditText) layout.findViewById(R.id.eLoginPassword);
 
 				//Toast.makeText(createOrg.this,user_role, Toast.LENGTH_SHORT).show();
 				if(user_role.equals("admin"))
 				{
 					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 					final View layout = inflater.inflate(R.layout.sign_up, (ViewGroup) findViewById(R.id.layout_signup));
 					AlertDialog.Builder builder = new AlertDialog.Builder(context);
 
 					builder.setView(layout);
 					TextView tvalertHead1 =(TextView) layout.findViewById(R.id.tvalertHead1);
 					tvalertHead1.setText("Sign Up");
 					builder.setCancelable(true);
 					Button done =  (Button) layout.findViewById(R.id.btnSignUp);
 					Button cancel =  (Button) layout.findViewById(R.id.btnCancel);
 					// call setOnClickListner on s
 					done.setOnClickListener(new View.OnClickListener(){
 						private RadioButton rbgender;
 						private String gender;
 
 						public void onClick(View v) {
 
 							radioGender =(RadioGroup) layout.findViewById(R.id.radioGender);
 
 							eUserName =(EditText) layout.findViewById(R.id.eUserName);
 							ePassword =(EditText)layout.findViewById(R.id.ePassword);
 							eConfPassword =(EditText) layout.findViewById(R.id.eConfPassword);
 							sQuestions = (Spinner) layout.findViewById(R.id.sQuestions);
 							eAnswer = (EditText) layout.findViewById(R.id.eAnswer);
 							tvwarning =(TextView) layout.findViewById(R.id.tvWarning);
 
 							genderid = radioGender.getCheckedRadioButtonId();
 							rbgender =(RadioButton)layout.findViewById(genderid);
 							gender = rbgender.getText().toString();
 							answer = eAnswer.getText().toString(); 
 							username = eUserName.getText().toString();
 							password = ePassword.getText().toString();
 							confpassword= eConfPassword.getText().toString();
 							selectedQuestion = sQuestions.getSelectedItemPosition();
 
 							//System.out.println("we are in signup");
 							Object[] params = new Object[]{username,password,gender,user_role,selectedQuestion, answer};
 							//Toast.makeText(createOrg.this,selectedQuestion, Toast.LENGTH_SHORT).show();
 							System.out.println("we are in signup1"+username+password+gender+user_role+selectedQuestion+answer);
 							if(module.isEmpty(params)||module.isEmpty(new Object[]{confpassword}))
 							{
 								String message = "please fill blank field";
 								tvwarning.setVisibility(TextView.VISIBLE);
 								tvwarning.setText(message);
 								//Toast.makeText(createOrg.this,user_role, Toast.LENGTH_SHORT).show();
 								// dialog.getLayoutInflater();
 							}else if(!password.equals(confpassword))
 							{
 								//Toast.makeText(createOrg.this,"password check", Toast.LENGTH_SHORT).show();
 								ePassword.setText("");
 								eConfPassword.setText("");
 								String message = "Please enter correct password";
 								tvwarning.setVisibility(TextView.VISIBLE);
 								tvwarning.setText(message);
 							}else
 							{
 								//Toast.makeText(createOrg.this,"else", Toast.LENGTH_SHORT).show();
 								boolean unique = user.isUserUnique(new Object[]{username},client_id);
 								// Toast.makeText(createOrg.this,"user"+unique, Toast.LENGTH_SHORT).show();
 								if(unique==true)
 								{	 
 									String setuser = user.setUser(params, client_id);
 									//String message = "Sign up successfully";
 									rb_guest.setVisibility(View.GONE);
 									rb_admin.setVisibility(View.GONE);
 									tvuserrole.setVisibility(View.GONE);
 
 									adminflag = true;  
 									eloginUsername.setText(username);
 									eloginPassword.setText("");
 									tvLoginWarning.setVisibility(TextView.GONE);
 									tvSignUp.setVisibility(TextView.VISIBLE);
 									tvSignUp.setText("Signed up successfully as a Admin, Please Log In!!!");
 
 									dialog.dismiss();
 								}
 								else{
 									String message = "User already exist";
 									tvwarning.setText(message);
 								}
 
 
 							} 
 
 						}
 					});
 
 					cancel.setOnClickListener(new View.OnClickListener(){
 						public void onClick(View v) {
 							adminflag = false;
 							tvLoginWarning.setVisibility(TextView.GONE);
 							eloginUsername.setText("");
 							eloginPassword.setText("");
 							dialog.dismiss();
 
 
 						}
 					});
 
 					dialog = builder.create();
 					dialog.show();
 					WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 					//customizing the width and location of the dialog on screen 
 					lp.copyFrom(dialog.getWindow().getAttributes());
 					lp.width = 800;
 					dialog.getWindow().setAttributes(lp);
 
 
 				}else
 				{
 					tvLoginWarning.setVisibility(TextView.GONE);
 					eloginPassword.setText("guest");
 					eloginUsername.setText("guest");
 
 				}
 			}// end of onCheckedChanged() method of setOnCheckedChangeListener()
 		}); // end of setOnCheckedChangeListener
 	}// end of addListenerOnRadioButton()
 }
