 package il.ac.tau.team3.shareaprayer;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.Pray;
 import il.ac.tau.team3.common.SPGeoPoint;
 import il.ac.tau.team3.common.SPUtils;
 import il.ac.tau.team3.common.UnknownLocationException;
 import il.ac.tau.team3.shareaprayer.FindPrayer.StringArray;
 import il.ac.tau.team3.spcomm.ACommHandler;
 import android.accounts.Account;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.text.Editable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.PopupWindow.OnDismissListener;
 import android.widget.QuickContactBadge;
 import android.widget.RemoteViews.ActionException;
 import android.widget.SimpleAdapter;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TimePicker;
  
 public class UIUtils {
  
 	static String _sNewPlaceQues = "Do you want to create a public praying place?";
 	static String _sNoPraySelected = "Please select at least one pray before creating a new place.";
 	static String _sAlreadyRegisterAlertMsg = "You are already registered to this place.";
 	static String _sWantToRegisterQues = "Would you like to register to this place?";
 	static String _sUserNotRegisterMsg = "You are not register to this place.";
 	static String _sUserNotOwnerMsg = "You can't delete this place, because you are not the owner.";
 	static String _sWelcomeMsg = "Welcome to Share-A-Prayer!";
 
 	static class UpdateUI<T> extends ACommHandler<T> {
 		FindPrayer activity;
  
 		public UpdateUI(FindPrayer a_activity) {
 			activity = a_activity;
 		}
   
 		@Override
 		public void onRecv(T Obj) {
 			synchronized (activity.getRefreshTask()) {
 				activity.getRefreshTask().notify();
 			
 			}
 		}
 
 		@Override
 		public void onError(T Obj) {
 			synchronized (activity.getRefreshTask()) {
 				activity.getRefreshTask().notify();
 			}
 		}
 	}
 	
 	
 	
 	static String[] HandleFirstTimeDialog(Account[] accounts, FindPrayer activity){
 		if (accounts.length == 0){
 			CreateNoAccountsDialog(activity);
 			return null;
 		}else{
 			return CreateChooseAccountsDialog(accounts, activity);
 		}
 	}
 	
 	static void CreateNoAccountsDialog(final FindPrayer activity){
 		final Dialog dialog = new Dialog(activity);
 		  dialog.setContentView(R.layout.dialog_startup_async);
           dialog.setTitle(_sWelcomeMsg);
           Button exitButton = (Button) dialog.findViewById(R.id.dsa_button_exit);
          // Button syncButton = (Button) dialog.findViewById(R.id.dsa_button_sync);
           
           exitButton.setOnClickListener(new OnClickListener()
           {                
               public void onClick(View v)
               {
                  dialog.dismiss();
                  activity.onDestroy();
               }
           });
           
 //          syncButton.setOnClickListener(new OnClickListener()
 //          {                
 //              public void onClick(View v)
 //              {
 //                  // TODO Open Sync Center.
 //              }
 //          });
           dialog.show();
 		
 	}
 	
 	static String[] CreateChooseAccountsDialog(final Account[] accounts, final FindPrayer activity){
 		final Dialog dialog = new Dialog(activity);
 		 dialog.setContentView(R.layout.dialog_startup_sync);
 		 final EditText editTextFirstName = (EditText)dialog.findViewById(R.id.startup_name_first);
          final EditText editTextLastName = (EditText)dialog.findViewById(R.id.startup_name_last);
          dialog.setTitle(_sWelcomeMsg);
          Button exitButton = (Button) dialog.findViewById(R.id.startup_button_exit);
          Button startButton = (Button) dialog.findViewById(R.id.startup_button_start);
          final int accountId[] = new int[1];
          final String names[] = new String[3];
          
          exitButton.setOnClickListener(new OnClickListener()
          {                
              public void onClick(View v)
              {
                 dialog.dismiss();
                 activity.onDestroy();
              }
          });
          
          startButton.setOnClickListener(new OnClickListener()
          {                
              public void onClick(View v)
              {
             	 if(editTextFirstName.getText() == null || editTextFirstName.getText().toString() == null ||
             			 editTextFirstName.getText().toString() == "" || 
             			 editTextLastName.getText() == null || editTextLastName.getText().toString() == null ||
             			 editTextLastName.getText().toString() == ""){
             		 createAlertDialog("Please enter your first and last name", activity, "OK");
             	 }else{
             	 names[0] = editTextFirstName.getText().toString();
             	 names[1] = editTextLastName.getText().toString();
             	 names[2] = accounts[accountId[0]].name;
             	 activity.setUser(names);
             	 
             	 dialog.dismiss();
             	 }
              }
          });
          
          RadioGroup  accountsRadioGroup = (RadioGroup) dialog.findViewById(R.id.startup_accounts_radios);
          RadioButton tempRadioButton;
          
          for (int i = 0; i < accounts.length; i++)
          {
              tempRadioButton = new RadioButton(activity);
              tempRadioButton.setId(i);
              tempRadioButton.setText(accounts[i].name);
                      
              accountsRadioGroup.addView(tempRadioButton);
          }
          
           
          
          accountsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
          {
              //@Override
              public void onCheckedChanged(RadioGroup group, int checkedId)
              {
                  accountId[0] = checkedId;
              }
 			
          });
          
              
      
      
      
      dialog.show();
 
      return names;
 		
 	}
 	
 	
 	
 	static void RegisterClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay, boolean praysWishes[]) {
 		GeneralUser user = placeOverlay.getThisUser();
 		if (user == null) {
 			Log.d("UIUtils:createRegisterDialog", "Error: user is null");
 			return;
 		} else {
 			String name = user.getName();
 			if (name == null || name == "") {
 				Log.d("UIUtils:createRegisterDialog",
 						"Error: name is null or empty.");
 				return;
 			}
 		}
 
 		
 		placeOverlay
 				.getActivity()
 				.getSPComm()
 				.requestPostRegister(place, user, praysWishes,
 						new UpdateUI<String>(placeOverlay
 								.getActivity()));
 
 		
 
 	}
 
 	static void DeleteClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay) {
 		if (place.getOwner().getName().equals(placeOverlay.getThisUser().getName())) {
 		
 			placeOverlay
 					.getActivity()
 					.getSPComm()
 					.deletePlace(place,
 							new UpdateUI<String>(placeOverlay
 									.getActivity()));
 		} else {
 			createAlertDialog(_sUserNotOwnerMsg, placeOverlay
 					.getActivity(), "Close");
 		}
 	}
 
 	static void UnregisterClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay, boolean praysWishes[]) {
 		GeneralUser user = placeOverlay.getThisUser();
 		if (user == null) {
 			Log.d("UIUtils:createRegisterDialog", "Error: user is null");
 			return;
 		} else {
 			String name = user.getName();
 			if (name == null || name == "") {
 				Log.d("UIUtils:createRegisterDialog",
 						"Error: name is null or empty.");
 				return;
 			}
 		}
 	
 		placeOverlay
 				.getActivity()
 				.getSPComm()
 				.removeJoiner(place, user, praysWishes,
 						new UpdateUI<Void>(placeOverlay
 								.getActivity()));
 
 	
 
 	}
 	
 	static class PrayUIObj	{
 		public TextView prayTime;
 		public CheckBox prayCheckBox;
 		public TextView prayNumOfUsers;
 		public boolean	wish = false;
 		public boolean  exists = false;
 		public PrayUIObj(TextView prayTime, CheckBox prayCheckBox) {
 			super();
 			this.prayTime = prayTime;
 			this.prayCheckBox = prayCheckBox;
 		}
 		public PrayUIObj(TextView prayTime, CheckBox prayCheckBox, TextView prayNumOfUsers) {
 			super();
 			this.prayTime = prayTime;
 			this.prayCheckBox = prayCheckBox;
 			this.prayNumOfUsers = prayNumOfUsers;
 		}
 		
 	}
 	
 	private static boolean[] toPrayerWishes(Map<String, PrayUIObj> ui)	{
 		return new boolean[]{ui.get("Shaharit").wish, ui.get("Minha").wish, ui.get("Arvit").wish};
 	}
 	//
 	//
 	//
 	//
 	//
 	//
 	//
 	//
 	
 	
 	
 	
 	
 	/*package*/ static void createRegisterDialog(final GeneralPlace place, final PlaceArrayItemizedOverlay placeOverlay)
 	{
 		if (placeOverlay == null || placeOverlay.getThisUser() == null || place == null) 
 		{
 			Log.d("UIUtils::createRegisterDialog",
 					"placeOverlay == null || placeOverlay.getThisUser() == null || place == null");
 			return;
 		} 
  
 		final Context activity = placeOverlay.getActivity();
 		final Dialog dialog = new Dialog(activity);
 		dialog.setContentView(R.layout.dialog_place_registration);
 		dialog.setTitle(place.getName());
 		
 		// Address and Dates
 		TextView placeAddress = (TextView) dialog.findViewById(R.id.DPRaddress);
 		placeAddress.setText(place.getAddress());
 
 		TextView placeDates = (TextView) dialog.findViewById(R.id.DPRdates);
 		placeDates.setText(printDateFromDate(place.getEndDate(),1900));
 		
 		final Map<String, PrayUIObj> JoinersUI = new HashMap<String, PrayUIObj>();
 		
 		JoinersUI.put("Shaharit", new PrayUIObj((TextView) dialog.findViewById(R.id.DPRtimeShaharit), 
 												(CheckBox) dialog.findViewById(R.id.DPRcheckboxShaharit),
 												(Button) dialog.findViewById(R.id.DPRnumberOfUsersShaharit)));
 		
 		JoinersUI.put("Minha", 	  new PrayUIObj((TextView) dialog.findViewById(R.id.DPRtimeMinha), 
 											    (CheckBox) dialog.findViewById(R.id.DPRcheckboxMinha),
 											    (Button) dialog.findViewById(R.id.DPRnumberOfUsersMinha)));
 		
 		JoinersUI.put("Arvit",    new PrayUIObj((TextView) dialog.findViewById(R.id.DPRtimeArvit), 
 											    (CheckBox) dialog.findViewById(R.id.DPRcheckboxArvit),
 											    (Button) dialog.findViewById(R.id.DPRnumberOfUsersArvit)));
 		
 		for (final Pray p : place.getPraysOfTheDay())	{
 			if (null != p)	{
 				final PrayUIObj uiObj = JoinersUI.get(p.getName());
 				if (uiObj == null)	{
 					continue;
 				}
 				uiObj.exists = true;
 				uiObj.prayTime.setText(printTimeFromCalendar(p.getStartTime()));
 				uiObj.prayTime.setTextColor(Color.WHITE);
 				uiObj.prayCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						uiObj.wish = isChecked;
 					}
 				});
 				Boolean isSigned = p.isJoinerSigned(placeOverlay.getThisUser());
 				uiObj.prayCheckBox.setChecked(isSigned);
 				uiObj.prayCheckBox.setClickable(true);
 				uiObj.prayCheckBox.setTextColor(Color.WHITE);
 				uiObj.prayNumOfUsers.setText(Integer.toString(p.numberOfJoiners()));
 				uiObj.prayNumOfUsers.setTextColor(Color.BLACK);
 				uiObj.prayNumOfUsers.setOnClickListener(new OnClickListener(){
 					public void onClick(View view) {
 						final Dialog listDialog = new Dialog(placeOverlay.getActivity());
 						listDialog.setCancelable(true);
 						listDialog.setCanceledOnTouchOutside(true);
 						listDialog.setContentView(R.layout.dialog_registered_users_list);
 						if(p.getJoiners()== null || p.getJoiners().size() == 0 ){
 							listDialog.setTitle("No prayers registered for this pray.");
 						}else{
 							listDialog.setTitle("Currently registered for " + p.getName() + ":");
 						}
 						ListView lv = (ListView) listDialog.findViewById(R.id.DRUList);
 						
 						lv.setTextFilterEnabled(true);
 						lv.setOnItemClickListener(new OnItemClickListener() {
 						    public void onItemClick(AdapterView<?> parent, View view,
 						        int position, long id) {
 						      createAlertDialog(((String) ((TextView) view).getText()) + "\n\n We should really have a Profile dialog for each user...", activity, "Close");
 						    }
 						  });
 						
 						Button closebutton = (Button) listDialog.findViewById(R.id.DRUCloseButton);
 						closebutton.setOnClickListener(new OnClickListener(){ public void onClick(View view){ listDialog.dismiss();	};});
 						
 						ArrayList<HashMap<String, String>> prayersList = new ArrayList<HashMap<String, String>>();
 						
 						for (GeneralUser joiner : p.getJoiners()){
 							HashMap<String, String> tempmap = new HashMap<String, String>();
 							// TODO Change back to Full name once it's not buggy
 							String userName = (joiner.getFullName()==null || joiner.getFullName()=="" ? joiner.getName() : joiner.getFullName());
 							tempmap.put("User", userName);
 							prayersList.add(tempmap);
 						}
 						
 						SimpleAdapter mPrays = new SimpleAdapter(placeOverlay.getActivity(), prayersList, R.layout.dialog_registered_users_row,
 						            new String[] {"User"}, new int[] {R.id.DRUUsername});
 						try{
 							lv.setAdapter(mPrays);
 						}catch (NullPointerException npe){}
 						listDialog.show();
 					}
 				});
 			}
 		}
 		
 		// Delete Set and Cancel Buttons:
 		Button setButton = (Button) dialog.findViewById(R.id.DPRSetButton);
 		Button closeButton = (Button) dialog.findViewById(R.id.DPRCloseButton);
 		Button deleteButton = (Button) dialog.findViewById(R.id.DPRDeleteButton);
 		 
 		deleteButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 				builder.setMessage("Are you sure you want to delete this Minyan place?");
 				builder.setCancelable(true);
 				builder.setNegativeButton("No",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								//dialog.dismiss();
 							}
 						});
 				builder.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								DeleteClick(place, placeOverlay);
 								//dialog.dismiss();
 							}
 						});
 				AlertDialog alert = builder.create();
 				alert.show();
 				dialog.dismiss();
 			};
 		});
 		if (place.getOwner() != null && placeOverlay.getThisUser() != null) {
			if (!(place.getOwner().getId() == placeOverlay.getThisUser().getId())) {
 				deleteButton.setVisibility(View.INVISIBLE);
 			}
 		}
 		final boolean allTrues[] = new boolean[3];
 		for (int i=0; i<3; i++){ allTrues[i] = true;}
 		setButton.setOnClickListener(new OnClickListener() 
 		{
 			public void onClick(View view) 
 			{
 				UnregisterClick(place, placeOverlay, allTrues);
 				RegisterClick(place, placeOverlay, toPrayerWishes(JoinersUI));
 				dialog.dismiss();
 			};
 		});
 		
 		closeButton.setOnClickListener(new OnClickListener() 
 		{
 			public void onClick(View view) 
 			{
 				dialog.dismiss();
 			};
 		});
 
 		dialog.show();
 	}
 
 	
 	static class ListDialog extends ListActivity
 	{
 		private Map<String, StringArray> map;
 		private Activity activity;
 		
 		public ListDialog(Map<String, StringArray> map, Activity activity ){
 			super();
 			this.map = map;
 			this.activity = activity;
 		}
 
 	}
 			
 	
 
 
 	static void createAlertDialog(String msg, Context context, String buttonText) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage(msg);
 		builder.setCancelable(true);
 		builder.setNegativeButton(buttonText,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 					}
 				});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	
 	
 	
 	static class CreatePlaceDialog	{		
 		private Dialog dialog;
 		private EditText editAddress;
 		private Calendar startDate = new GregorianCalendar(); 
 		private Calendar endDate = new GregorianCalendar(); 
 		private TextView fromDate;
         private TextView toDate;
         private FindPrayer activity;
         private Button changeStartDate;
         private Button changeEndDate;
         private final int NUMBER_OF_PRAYS = 3;
         private boolean prays[] = new boolean[NUMBER_OF_PRAYS];
         private CheckBox[] checkBoxes = new CheckBox[NUMBER_OF_PRAYS];
         private TextView[] timeTextViews = new TextView[NUMBER_OF_PRAYS];
         private Calendar[] prayTimes = new GregorianCalendar[NUMBER_OF_PRAYS];
       
         
         private class DatePickerClickListener implements OnClickListener	{
         	
         	private Calendar cal;
         	private TextView textStr;
         	
         	public DatePickerClickListener(Calendar a_cal, TextView a_textStr)	{
         		cal = a_cal;
         		textStr = a_textStr;
         	}
 
 			public void onClick(View v) {
 				DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
                 {
                     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                     {
                     	cal.set(year, monthOfYear, dayOfMonth);
                     	textStr.setText(printDateFromCalendar(cal,0));
                     }
                 };
                 DatePickerDialog datePickerDialog = 
                 	new DatePickerDialog(
                 			CreatePlaceDialog.this.activity, 
                 			mDateSetListener, 
                 			cal.get(Calendar.YEAR), 
                 			cal.get(Calendar.MONTH), 
                 			cal.get(Calendar.DAY_OF_MONTH));
                 datePickerDialog.show();
 			}
         	
         }
         
         class PrayTimePickDialog extends TimePickerDialog {
 
         	private CheckBox checkBox;
         	private int prayIndex;
         	
         	public PrayTimePickDialog(final TextView a_timeStr, int defHour, int defMin, 
         			CheckBox a_checkBox, final int a_prayIndex, int a_resIcon)	{
         		super(activity, 
         				new OnTimeSetListener() {
         			
         			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
     					SPUtils.debugFuncStart("timePickerDialog.onTimeSet", view, hourOfDay, minute);
     					CreatePlaceDialog.this.prays[a_prayIndex] = true;
     					prayTimes[a_prayIndex].set(2000, 1, 1, hourOfDay, minute, 0);
     					Date time = new Date(0,0,0,hourOfDay, minute);
     					a_timeStr.setText(printTimeFromDate(time));
     					
     				}
         			
         		}, defHour, defMin, true);
         		
                 this.setIcon(a_resIcon);
                 this.setInverseBackgroundForced(true);          
                 this.setCancelable(true);              //
                 this.setCanceledOnTouchOutside(true);  //
         		checkBox = a_checkBox;
         	}
         	
         	@Override
 			public void cancel()	{
         		SPUtils.debugFuncStart("timePickerDialog.onCancel", dialog);
         		super.cancel();
         	}
         	
         	@Override
         	public void dismiss()	{
         		SPUtils.debugFuncStart("timePickerDialog.onDismiss", dialog);
                 SPUtils.debug("--> prays["+prayIndex+"] = " + "prays["+prayIndex+"]");
                 checkBox.setChecked(prays[prayIndex]);
                 super.dismiss();
         	}
         	
         	
         	
         }
         
         class CheckBoxListener implements OnCheckedChangeListener
         {              
         	private TextView timeTextView;
         	private int		 index;
         	private CheckBox checkBox;
         	int defHour;
         	int defMinutes;
         	int resIcon;
         	
         	
             public CheckBoxListener(TextView timeTextView, int index, CheckBox checkBox, 
             		int defHour, int defMinutes, int resIcon) {
 				super();
 				this.timeTextView = timeTextView;
 				this.index = index;
 				this.checkBox = checkBox;
 				this.defHour = defHour;
 				this.defMinutes = defMinutes;
 				this.resIcon = resIcon;
 				
 			}
 
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
             {
                 SPUtils.debugFuncStart("pray1.onCheckedChanged", buttonView, isChecked);
                 if (isChecked)
                 {
                     (new PrayTimePickDialog(timeTextView, defHour, defMinutes, checkBox, index, resIcon)).show();
                 }
                 else
                 {
                     prays[index] = false;
                     timeTextView.setText("");
                 }                
             }
         };
 		
 		public CreatePlaceDialog(final SPGeoPoint point, final FindPrayer a_activity, final GeneralUser user)	{
 			if (point == null || a_activity == null || user == null)
 			{
 				
 				Log.d("UIUtils::createRegisterDialog", "point == null || activity == null || user == null");
 				// TODO: change to checked exception
 				throw new NullPointerException("CreatePlaceDialog: executed with NULL!!!!");
 				//return;
 			}
 			
 			for (int i = 0; i < prayTimes.length; prayTimes[i] = new GregorianCalendar(),i++);
 			  
 			activity = a_activity;
 			
 			dialog = new Dialog(activity);
 			dialog.setContentView(R.layout.dialog_place_create);
 			dialog.setTitle(R.string.create_place_title);
 			
 			editAddress = (EditText) dialog.findViewById(R.id.CPDeditText1);
 			
 			fromDate = (TextView) dialog.findViewById(R.id.CPDFromDatetextView);
 			toDate   = (TextView) dialog.findViewById(R.id.CPDToDatetextView);
 			fromDate.setText(printDateFromCalendar(startDate,0)); 
 	        toDate.setText(printDateFromCalendar(endDate,0)); 
 	        
 	        changeStartDate = (Button) dialog.findViewById(R.id.CPDChange1button);
 	        changeEndDate = (Button) dialog.findViewById(R.id.CPDChange2button);
 	        
 			changeStartDate.setOnClickListener(new DatePickerClickListener(startDate, fromDate));
 			changeEndDate.setOnClickListener(new DatePickerClickListener(endDate, toDate));
 			
 			
 			checkBoxes[0] = (CheckBox) dialog.findViewById(R.id.CPDcheckBox1);
 			timeTextViews[0] = (TextView) dialog.findViewById(R.id.CPDshahritTime);
 			checkBoxes[0].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[0], 
 					0, checkBoxes[0], 7, 0, R.drawable.shaharit_small));
 			// TODO find out what's working, checkBoxes[0] or checkBoxes[1]
 			checkBoxes[1] = (CheckBox) dialog.findViewById(R.id.CPDcheckBox2);
 			timeTextViews[1] = (TextView) dialog.findViewById(R.id.CPDminhaTime);
 			checkBoxes[1].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[1], 
 					1, checkBoxes[0], 15, 0, R.drawable.minha_small));
 			// TODO find out what's working, checkBoxes[0] or checkBoxes[2]
 			checkBoxes[2] = (CheckBox) dialog.findViewById(R.id.CPDcheckBox3);
 			timeTextViews[2] = (TextView) dialog.findViewById(R.id.CPDarvitTime);
 			checkBoxes[2].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[2], 
 					2, checkBoxes[0], 19, 0, R.drawable.arvit_small));
 			
 			Button createButton = (Button) dialog.findViewById(R.id.CPDCreateButton);
 	        Button cancelButton = (Button) dialog.findViewById(R.id.CPDCancelButton);
 	        
 	    
 			createButton.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View view) {
 					if(!prays[0] && !prays[1] && !prays[2]){
 						createAlertDialog("You must choose at least one pray", activity, "Cancel");
 					}
 					else{
 						final Date finalstartDate = new Date(startDate.get(Calendar.YEAR)-1900,startDate.get(Calendar.MONTH),startDate.get(Calendar.DAY_OF_MONTH));
 						final Date finalendDate = new Date(endDate.get(Calendar.YEAR)-1900,endDate.get(Calendar.MONTH),endDate.get(Calendar.DAY_OF_MONTH));
 						CreateNewPlace_YesClick(prays, user, activity, point, finalstartDate, finalendDate, prayTimes);
 						dialog.dismiss();
 					}
 					
 
 				};
 
 			});
 
 			cancelButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View view) {
 					dialog.dismiss();
 				};
 			});
 			dialog.show();
 		}
 	};
 
 	static void createNewPlaceDialog(final SPGeoPoint point, final FindPrayer activity, final GeneralUser user) 
 	{
 		try {
 			new CreatePlaceDialog(point, activity, user);
 		} catch (NullPointerException e)	{
 		}
      }
 	
 	static void CreateNewPlace_YesClick(boolean prays[], GeneralUser user,
 			FindPrayer activity, SPGeoPoint point, Date startDate, Date endDate , Calendar[] prayTimes) {
 		String placeName = (user.getFullName()==null || user.getFullName()=="" ? user.getName() : user.getFullName()) + "'s Place";
 		GeneralPlace newMinyan = new GeneralPlace(user, placeName, "", point, startDate,endDate);
 		Calendar c = new GregorianCalendar();
 		List<GeneralUser> j = new ArrayList<GeneralUser>();
 		j.add(user);
 		if (prays[0]) {
 			
 			Pray pray = new Pray(prayTimes[0], c, "Shaharit", j);
 			newMinyan.setPraysOfTheDay(0, pray);
 		}
 		if (prays[1]) {
 			Pray pray = new Pray(prayTimes[1], c, "Minha", j);
 			newMinyan.setPraysOfTheDay(1, pray);
 		}
 		if (prays[2]) {
 			Pray pray = new Pray(prayTimes[2], c, "Arvit", j);
 			newMinyan.setPraysOfTheDay(2, pray);
 		}
 
 		activity.getSPComm().requestPostNewPlace(newMinyan,
 				new UpdateUI<Long>(activity));
 
 		
 	}
 
 	
 	public static String printDateFromCalendar(Calendar c, int yearAddon) {
 		int day = c.get(Calendar.DAY_OF_MONTH);
 		int month = (c.get(Calendar.MONTH)+1);
 		// Because of server issues, needs to add 1900
 		int year = c.get(Calendar.YEAR) + yearAddon;
 		return ((day < 10 ? "0" : "") + day + "/" + (month < 10 ? "0" : "") + month + "/" + year);
 	}
 
 	public static String printDateFromDate(Date d, int yearAddon) {
 		int day = d.getDate();
 		int month = d.getMonth()+1;
 		// Because of server issues, needs to add 1900
 		int year = d.getYear()+yearAddon;
 		return ((day < 10 ? "0" : "") + day + "/" + (month < 10 ? "0" : "") + month + "/" + year);
 	}
     
 	public static String printTimeFromCalendar(Calendar cal) {
 		int hour = cal.getTime().getHours();
 		int minutes = cal.getTime().getMinutes();
 		return ((hour < 10 ? "0" : "") + hour + ":" + (minutes < 10 ? "0" : "") + minutes + " ");
 	}
 
 	public static String printTimeFromDate(Date time) {
 		int hour = time.getHours();
 		int minutes = time.getMinutes();
 		return ((hour < 10 ? "0" : "") + hour + ":" + (minutes < 10 ? "0" : "") + minutes + " ");
 	} 
 	
 	public static int getPrayerIconID(String prayer){
 		if (prayer.equals("Shaharit")) return R.drawable.shaharit_small;
 		if (prayer.equals("Minha")) return R.drawable.minha_small;
 		if (prayer.equals("Arvit")) return R.drawable.arvit_small;
 		return 0;
 	}
 	
 	
 	
 	
 /////////////////////////////////////////////////////////////////////////////////////////////////////
 ///////// Menu: /////////////////////////////////////////////////////////////////////////////////////
 /////////////////////////////////////////////////////////////////////////////////////////////////////
 
 
 	public static int getContextWidth(Context context)
 	{
 	    return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
 	}
 	
 	
 	
 	
 	
 	public interface ISPMenuItem
 	{
 	    abstract public int    getItemId();
 	    abstract public int    getResIconId();
 	    abstract public String getTitle();
 	}
 	
 	
 	public interface ISPOnMenuItemSelectedListener<E_MenuItem extends Enum<E_MenuItem> & ISPMenuItem>
 	{
 	    abstract public void onMenuItemSelected(E_MenuItem item, View view);
 	}
 	
 	
 	static public class SPMenu<  E_MenuItem  extends  Enum<E_MenuItem>  &  ISPMenuItem  >
 	{
 	    public static boolean isShowing(SPMenu<?> menu)
 	    {
 	        return null != menu && menu.isInitialized() && menu.isShowing();
 	    }
 	    
 	    private static final int SP_MENU_RES_ROOT    = R.layout.menu_options_main;
 	    private static final int SP_MENU_ITEM_RES_ID = R.id.mom_items_row;
 	    
 	    
 	    private final E_MenuItem[] items;
 	    private final ISPOnMenuItemSelectedListener<E_MenuItem> menuListener;
 	    private       PopupWindow  menuWindow;
 	    
 	    
 	    /** @constructor */
 	    public SPMenu(E_MenuItem[] items, ISPOnMenuItemSelectedListener<E_MenuItem> menuListener)
 	    {
 	        this.items        = items;
 	        this.menuListener = menuListener;
 	        this.menuWindow   = null;
 	    }
 	    
 	    
 	    
 
         /**
 	     * @pre    this.isInitialized()
 	     * @return true:  There a Window/Layout allocated and inflated.
 	     *         false: Not even allocated (counting on Garbage-Collector).
 	     */
 	    private boolean isShowing()
 	    {
 	        return null != this.menuWindow && this.menuWindow.isShowing();
 	    }
 	    
 	    public boolean isInitialized()
 	    {
 	        return null != this.items && 0 != this.items.length && null != this.menuListener;
 	    }
 	    
 	    /** @main */
 	    public void handleMenuButtonClick(Activity activity, int buttomViewResId)
 	    {
 	        /*
 	         * Second push on menu button will hide.
 	         */
 	        if (isShowing())
 	        {
 	            this.hide();
 	        }
 	        else
 	        {
 	            this.show(activity, buttomViewResId);
 	        }
 	    }
 	    
 	    
 	    
 	    public synchronized void show(Activity activity, int buttomViewResId)
 	    {
 	        if (! this.isInitialized())
 	        {
 	            Log.w("*SPMenu*", "Trying to show() an uninitialized one.");
 	            return;
 	        }
 	        
 	        TableLayout menu = (TableLayout) activity.getLayoutInflater().inflate(R.layout.menu_options_main, null);
 	        
 	        menuWindow = new PopupWindow(menu, LayoutParams.FILL_PARENT,  LayoutParams.WRAP_CONTENT, false);
 	        menuWindow.setAnimationStyle(android.R.style.Animation_Dialog);
 	        menuWindow.setWidth(getContextWidth(activity));
 	        
 	        //menuWindow.setOutsideTouchable(true);
 	        menuWindow.setTouchInterceptor(new OnTouchListener()
 	        {                
 	            public boolean onTouch(View v, MotionEvent event)
 	            {
 	                SPUtils.debugFuncStart("**!!** menuWindow.OnTouchListener.onTouch", v, event);
 	                // This method should invoke only if the menu isShowing(), so no check.
 	                SPMenu.this.hide(); // For any Event
 	                
 	                // Tell the world that we took care of the touch event.
 	                return true;
 	            }
 
 	        });
 	        
 	        
 	        
 	        menuWindow.showAtLocation(activity.findViewById(buttomViewResId), Gravity.BOTTOM, 0, 0);
 	        
 	        
 	        TableRow itemTableRow = (TableRow) menu.findViewById(R.id.mom_items_row);
 	        itemTableRow.removeAllViews();
 	        
 //	        View      itemLayout;
 	        TextView  itemTitle  = null;
 //	        ImageView itemIcon   = null;
 	        
 	        for (final E_MenuItem item : this.items)
 	        {
 //	            itemLayout = activity.getLayoutInflater().inflate(R.layout.menu_item, null);
 //	            itemLayout.setMinimumWidth(getContextWidth(activity) / this.items.length);
 	            
 	            
 	            
 //	            itemTitle = (TextView) itemLayout.findViewById(R.id.moi_caption);
 	            itemTitle = new TextView(activity);
 	            
 	            
 	            
 	            itemTitle.setClickable(true);
 	            itemTitle.setGravity(Gravity.CENTER);
 	            itemTitle.setWidth(getContextWidth(activity) / this.items.length);
 	            
 	            itemTitle.setText(item.getTitle());
 	            
 	            itemTitle.setCompoundDrawablesWithIntrinsicBounds(0, item.getResIconId(), 0, 0);
 	            itemTitle.setBackgroundResource(R.drawable.selector_menu_item);
 	            
 //	            itemIcon = (ImageView) itemLayout.findViewById(R.id.moi_icon);
 //	            itemIcon.setImageResource(item.getResIconId());
 	            
 	            
 	            
 	            itemTitle.setOnClickListener(new OnClickListener()
 	            {                    
 	                public void onClick(View v)
 	                {
 	                    SPMenu.this.menuListener.onMenuItemSelected(item, v);
 	                }
 	            });
 	            
 	            itemTableRow.addView(itemTitle);
 	      
 	        
 	        }
 	        
 	    }
 	    
 	    
 	    
 	    /**
 	     * @post !this.isShowing()
 	     * @imp  Garbage-Collector will finish the job.
 	     *       It's good to free, because menu is not often pressed.
 	     *       The rest doesn't sum to a lot of memory. 
 	     */
 	    public synchronized void hide()
 	    {
 	        if (SPMenu.this.isShowing())
 	        {                
 	            SPMenu.this.menuWindow.dismiss();
 	            SPMenu.this.menuWindow = null;
 	        }
 	        
 	    }
 	    
 	    
 	    
 	}//@END: class SPMenu
 	
 	
 	
 	
 	
 }
