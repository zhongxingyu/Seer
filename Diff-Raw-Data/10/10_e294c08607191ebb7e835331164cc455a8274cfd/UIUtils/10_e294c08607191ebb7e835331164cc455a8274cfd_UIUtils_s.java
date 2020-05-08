 package il.ac.tau.team3.uiutils;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.mapsforge.android.maps.MapView.TextField;
 
 import il.ac.tau.team3.addressQuery.MapsQueryLocation;
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.Pray;
 import il.ac.tau.team3.common.SPGeoPoint;
 import il.ac.tau.team3.common.SPUtils;
 import il.ac.tau.team3.common.UnknownLocationException;
 import il.ac.tau.team3.shareaprayer.FacebookConnector;
 import il.ac.tau.team3.shareaprayer.FindPrayer;
 import il.ac.tau.team3.shareaprayer.PlaceArrayItemizedOverlay;
 import il.ac.tau.team3.shareaprayer.R;
 import il.ac.tau.team3.shareaprayer.R.drawable;
 import il.ac.tau.team3.shareaprayer.R.id;
 import il.ac.tau.team3.shareaprayer.R.layout;
 import il.ac.tau.team3.shareaprayer.R.string;
 import il.ac.tau.team3.shareaprayer.ServiceNotConnected;
 import il.ac.tau.team3.shareaprayer.UserNotFoundException;
 import il.ac.tau.team3.spcomm.ACommHandler;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.TimePickerDialog;
 import android.app.AlertDialog.Builder;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.provider.Settings;
 import android.text.Editable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
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
 
 	
 	
 	static class UpdateUI<T> extends ACommHandler<T> {
 		FindPrayer activity;
  
 		public UpdateUI(FindPrayer a_activity) {
 			activity = a_activity;
 		}
   
 		@Override
 		public void onRecv(T Obj) {
 			synchronized (activity.getRefreshTask()) {
 				activity.getRefreshTask().notify();
 				postSuccessStatus(activity, Obj);
 			
 			}
 		}
 
 		@Override
 		public void onError(T Obj) {
 			synchronized (activity.getRefreshTask()) {
 				activity.getRefreshTask().notify();
 				postSuccessFailed(activity, Obj);
 			}
 			
 		}
 		
 		protected void postSuccessStatus(FindPrayer activity, T Obg)	{
 			
 		}
 		
 		protected void postSuccessFailed(FindPrayer activity, T Obg)	{
 			
 		}
 	}
 
 	
 	public static void initSearchBar(EditText searchBar)
 	{
 	    searchBar.setMaxLines(1);
 	    searchBar.setHorizontallyScrolling(true);
 	    searchBar.setHorizontalFadingEdgeEnabled(true);
 	    
 	    searchBar.setHintTextColor(Color.LTGRAY);	    
         
 	    searchBar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.menu_item_new_edit_find_replace, 0);
 	    searchBar.setCompoundDrawablePadding(4);
 	}
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	
 	public static String[] HandleFirstTimeDialog(Account[] accounts, FindPrayer activity){
 		if (accounts.length == 0){
 			CreateNoAccountsDialog(activity);
 			return null;
 		}else{
 			return createChooseAccountsDialog(accounts, activity);
 		}
 	}
 	
 	static void CreateNoAccountsDialog(final FindPrayer activity){
 		final Dialog dialog = new Dialog(activity);
 		  dialog.setContentView(R.layout.dialog_startup_async);
           dialog.setTitle(activity.getString(R.string.WelcomeMsg));
           Button exitButton = (Button) dialog.findViewById(R.id.dsa_button_exit);
           Button syncButton = (Button) dialog.findViewById(R.id.dsa_button_sync);
           
           exitButton.setOnClickListener(new OnClickListener()
           {                
               public void onClick(View v)
               {
                  dialog.dismiss();
                  activity.finish();
               }
           });
           
           syncButton.setOnClickListener(new OnClickListener()
           {                
               public void onClick(View v)
               {
             	  activity.startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS));
             	  dialog.dismiss();
                   activity.finish();
               }
           });
           dialog.show();
 		
 	}
 	
 	public static String[] createChooseAccountsDialog(final Account[] accounts, final FindPrayer activity){
 		final Dialog dialog = new Dialog(activity);
 		 dialog.setContentView(R.layout.dialog_startup_sync);
 		 final EditText editTextFirstName = (EditText)dialog.findViewById(R.id.startup_name_first);
          final EditText editTextLastName = (EditText)dialog.findViewById(R.id.startup_name_last);
          dialog.setTitle(activity.getString(R.string.WelcomeMsg));
          Button exitButton = (Button) dialog.findViewById(R.id.startup_button_exit);
          Button startButton = (Button) dialog.findViewById(R.id.startup_button_start);
          final int accountId[] = new int[1];
          final String names[] = new String[3];
          
          exitButton.setOnClickListener(new OnClickListener()
          {                
              public void onClick(View v)
              {
                 dialog.dismiss();
                 activity.finish();
              }
          });
          
          startButton.setOnClickListener(new OnClickListener()
          {                
              public void onClick(View v)
              {
             	 if(editTextFirstName.getText() == null || editTextFirstName.getText().toString() == null ||
             			 editTextFirstName.getText().toString().equals("") || 
             			 editTextLastName.getText() == null || editTextLastName.getText().toString() == null ||
             			 editTextLastName.getText().toString().equals("")){
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
 	
 	private static GeneralUser getThisUser(FindPrayer activity)	{
 		try {
 			return activity.getSvcGetter().getService().getUser();
 		} catch (UserNotFoundException e) {
 			return null;
 		} catch (ServiceNotConnected e) {
 			return null;
 		}
 	}
 	
 	static void RegisterClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay, boolean praysWishes[]) {
 		
 		GeneralUser user = getThisUser(placeOverlay.getActivity());
 		
 		if (user == null) {
 			
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
 				new UpdateUI<Integer>(placeOverlay.getActivity()) {
 			protected void postSuccessStatus(FindPrayer activity, Integer Obg)	{
 				if(activity.getStatusBar()!=null){
 					activity.getStatusBar().write("Accepted your changes to " + place.getName(),R.drawable.status_bar_accept_icon, 2000);
 				}
 			}
 
 			protected void postSuccessFailed(FindPrayer activity, Integer Obg)	{
 				if(activity.getStatusBar()!=null){
 					activity.getStatusBar().write("Couldn't update your registrations in " + place.getName(),R.drawable.status_bar_error_icon, 2000);
 				}
 			}
 		}
 		);
 
 		boolean[] changedPlus = new boolean[praysWishes.length];
 		boolean[] changedMinus = new boolean[praysWishes.length];
 		final String[] praysNames = new String[]{"Shaharit", "Minha", "Arvit"};
 		boolean anyChange = false;
 		for (int i = 0; i < praysWishes.length; i++)	{
 			try	{
 				changedPlus[i] = (!(place.getPrayByName(praysNames[i]).isJoinerSigned(user)) && praysWishes[i]);
 				changedMinus[i] = (place.getPrayByName(praysNames[i]).isJoinerSigned(user) && !(praysWishes[i]));
 				anyChange |= changedPlus[i];
 			} catch (Exception e)	{
 				changedPlus[i] = false;
 				changedMinus[i] = false;
 			}
 			
 		}
 		
 		
 		if (!anyChange)	{
 			return;
 		}
 		
 		FacebookConnector fc = placeOverlay.getActivity().getFacebookConnector();
 		fc.publishOnFacebook(formatFacebookHeadear_Register(place,changedPlus) ,
 				formatFacebookDesc_Register(place, changedPlus, changedMinus));
 	}
 
 	
 	
 	
 	static String formatFacebookDesc_Register(GeneralPlace place, boolean[] changedPlus, boolean[] changedMinus ){
 		return 10 - getMin(place , changedPlus, changedMinus) + " are still missing for a minyan in one of the prays. <br>" + "Sign as well and help to fill a minyan!";
 	}
 	
 	static String formatFacebookHeadear_Register(GeneralPlace place, boolean praysWishes[]){
 		
 		return "Just signed to " + place.getName() + " for " + 
 		(praysWishes[0] ? "Shaharit" : "" ) + (praysWishes[0]&&(praysWishes[1] || praysWishes[2]) ? " and ": "")+ (praysWishes[1] ? "Minha" : "") + (praysWishes[1] && praysWishes[2] ? " and ": "") +  
 		(praysWishes[2] ? "Arvit" : "") + ".";
 	}
 	
 	static int getMin(GeneralPlace place, boolean[] changedPlus, boolean[] changedMinus){
 		int nums[] = new int[3];
 		int nowCounts[] = new int[3];
 		for(int i=0; i<3 ;++i){
 			if(!changedPlus[i] && !changedMinus[i]){
 				nowCounts[i] = 0;
 			}else if (changedPlus[i]){
 				nowCounts[i] = 1;
 			}else if (changedMinus[i]){
 				nowCounts[i] = -1;
 			}
 		}
 		final String[] praysNames = new String[]{"Shaharit", "Minha", "Arvit"};
 		
 				
 		for(int i=0; i<3 ;++i){
 			try{
 			nums[i] = (int) (place.getPrayByName(praysNames[i]).getJoiners().size()  + nowCounts[i]);
 			}catch (Exception e) {
 				nums[i] = (int) SPUtils.INFINITY;
 			}
 		}
 		return Math.min(Math.min(nums[0],nums[1]), nums[2]) ;
 		
 	}
 
 	static void DeleteClick(final GeneralPlace place,
 			final PlaceArrayItemizedOverlay placeOverlay) {
 		
 		GeneralUser user = getThisUser(placeOverlay.getActivity());
 		
 		if (null == user)	{
 			return;
 		}
 		
 		if (place.getOwner().getName().equals(user.getName())) {
 		
 			placeOverlay
 			.getActivity()
 			.getSPComm()
 			.deletePlace(place,
 					new UpdateUI<Long>(placeOverlay
 							.getActivity())	{
 				protected void postSuccessStatus(FindPrayer activity, Long Obg)	{
 					if(activity.getStatusBar()!=null){
 						activity.getStatusBar().write("Place deleted", R.drawable.status_bar_accept_icon, 2000);
 					}
 				}
 
 				protected void postSuccessFailed(FindPrayer activity, Long Obg)	{
 					if(activity.getStatusBar()!=null){
 						activity.getStatusBar().write("Couldn't delete place" , R.drawable.status_bar_error_icon, 2000);
 					}
 				}
 			});
 		} else {
 			createAlertDialog(placeOverlay.getActivity().getString(R.string.UserNotOwnerMsg), placeOverlay
 					.getActivity(), "Close");
 		}
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
 	
 	
 	
 	/*package*/ public static void createRegisterDialog(final GeneralPlace place, final PlaceArrayItemizedOverlay placeOverlay)
 	{
 		GeneralUser user = getThisUser(placeOverlay.getActivity()); 
 		
 		if (placeOverlay == null || user == null || place == null) 
 		{
 			Log.d("UIUtils::createRegisterDialog",
 					"placeOverlay == null || placeOverlay.getThisUser() == null || place == null");
 			return;
 		} 
  
 		final Context activity = placeOverlay.getActivity();
 		final NoTitleDialog dialog = new NoTitleDialog(activity);
 		dialog.setContentView(R.layout.dialog_place_registration);
 		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		
 		// Title
 		TextView title = (TextView) dialog.findViewById(R.id.DPRTitle);
 		title.setText(place.getName());
 		
 		// Address and Dates
 		TextView placeAddress = (TextView) dialog.findViewById(R.id.DPRaddress);
 		placeAddress.setText(place.getAddress());
 
 		TextView placeEndDate = (TextView) dialog.findViewById(R.id.DPRdatesUntil);
 		placeEndDate.setText(printDateFromDate(place.getEndDate(),1900));
 		
 		TextView placeStartDate = (TextView) dialog.findViewById(R.id.DPRdatesFrom);
 		placeStartDate.setText(printDateFromDate(place.getStartDate(),1900));
 		
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
 				Boolean isSigned = p.isJoinerSigned(user);
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
 							listDialog.setTitle("No prayers registered.");
 						}else{
 							listDialog.setTitle("Registered for " + p.getName() + ":");
 						}
 						ListView lv = (ListView) listDialog.findViewById(R.id.DRUList);
 						
 						lv.setTextFilterEnabled(true);
 						lv.setOnItemClickListener(new OnItemClickListener() {
 						    public void onItemClick(AdapterView<?> parent, View view,
 						        int position, long id) {
 						    	try{
 						    	String clickedUserName = ((String) ((TextView) view).getText());
 						    	GeneralUser clickedUser = getUserByName(p.getJoiners(), clickedUserName);
 						    	if (clickedUser != null) new UserDetailsDialog(clickedUser, (FindPrayer) activity);
 						    	}
 						    	catch (NullPointerException npe){}
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
 		if (place.getOwner() != null && user != null) {
 			if (!(place.getOwner().getId().equals(user.getId()))) {
 				deleteButton.setVisibility(View.INVISIBLE);
 			}
 		}
 		
 		
 		setButton.setOnClickListener(new OnClickListener() 
 		{
 			public void onClick(View view) 
 			{
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
 	
 
 	
 	///////////////////////////////////////////////////////////////////////////////////////////////////////
     //// CreatePlaceDialog ////////////////////////////////////////////////////////////////////////////////
     ///////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	
 	private static class CreatePlaceDialog	
 	{		
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
         private Button createButton;
         private Button cancelButton;
         private SPGeoPoint location;
         private String lastEditText ="";
         private int regularTextColor ;
         private boolean datesWrong = false;
         private boolean addressIncorrect = true;
         
         private final String createPlaceInfoMsg[] = new String[]{"You must choose at least one pray.\n", "Your end date ends before the begin date."};
         
         private void SetTimeToZero(Date date){
         	date.setHours(0);
         	date.setMinutes(0);
         	date.setSeconds(0);
         }
         
         private class DatePickerClickListener implements OnClickListener	
         {        	
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
 //                    	
 //                    	Date newStartDate = new Date(startDate.get(Calendar.YEAR) - 1900,startDate.get(Calendar.MONTH),startDate.get(Calendar.DAY_OF_MONTH));
 //                    	SetTimeToZero(newStartDate);
 //                    	Date newEndDate = new Date(endDate.get(Calendar.YEAR) - 1900,endDate.get(Calendar.MONTH),endDate.get(Calendar.DAY_OF_MONTH));
 //                    	SetTimeToZero(newEndDate);
                     	
                     	Date newDate = new Date(year-1900,monthOfYear,dayOfMonth);
                     	Date today = new Date();
                     	SetTimeToZero(today);
                     	
                   	if((long)(today.getTime()/1000) > ((long)(newDate.getTime()/1000))){
                     		textStr.setTextColor(activity.getResources().getColor(R.color.red));
                     		createButton.setEnabled(false);
                   	}else{
                   			textStr.setTextColor(regularTextColor);
                   			if(toDate.getCurrentTextColor() != regularTextColor || fromDate.getCurrentTextColor() != regularTextColor){
                   				createButton.setEnabled(false);
                   			}else{
                   				if(!addressIncorrect){
                   					createButton.setEnabled(true);
                   				}
                   			}
                   			
                   	}
                   	   
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
         
 
         
         
         private class SPOnTimeSetListener 
         implements OnTimeSetListener
         {
             private int prayIndex;
             
             private SPOnTimeSetListener(int prayIndex)
             {
                 this.prayIndex = prayIndex;
             }
             
             
             //@Override
             public void onTimeSet(TimePicker view, int hourOfDay, int minute)
             {
                 CreatePlaceDialog.this.prays[this.prayIndex] = true;
                 prayTimes[this.prayIndex].set(2000, 1, 1, hourOfDay, minute, 0);
                 Date time = new Date(0, 0, 0, hourOfDay, minute);
                 CreatePlaceDialog.this.timeTextViews[this.prayIndex].setText(printTimeFromDate(time));
             }                
         }
         
         
         
         private class PrayTimePickerDialog 
         extends TimePickerDialog
         {            
             private CheckBox checkBox;
             private int      prayIndex;
 
             
             private PrayTimePickerDialog(int defHour, int defMin, CheckBox a_checkBox, final int a_prayIndex, int a_resIcon)
             {
                 super(activity, new SPOnTimeSetListener(a_prayIndex) , defHour, defMin, true);
                 
                 this.setIcon(a_resIcon);
                 this.setInverseBackgroundForced(true);
                 this.setCancelable(true);
                 this.setCanceledOnTouchOutside(true);
                 this.checkBox  = a_checkBox;
                 this.prayIndex = a_prayIndex;                   ////   THIS WAS THE MAIN PROBLAM:  !!!!!!!!!!!
             }
     	
         	
 
             //TODO REMOVE
             /**
              * @imp      No need for this one because: onDismiss() gets invoked any way.
              * @override For no good reason.
              */
             @Override
 			public void cancel()	
         	{
         		
         		super.cancel();
         	}
         	
         	
         	@Override
         	public void dismiss()
         	{
         		
                 this.checkBox.setChecked(CreatePlaceDialog.this.prays[this.prayIndex]);
                 super.dismiss();
         	}
         }
         
         
         private class CheckBoxListener 
         implements OnCheckedChangeListener
         {              
         	private     TextView timeTextView;
         	private     int		 index;
         	private     CheckBox checkBox;
         	/*package*/ int      defHour;
         	/*package*/ int      defMinutes;
         	/*package*/ int      resIcon;
         	
             public CheckBoxListener(TextView timeTextView, int index, CheckBox checkBox, int defHour, int defMinutes, int resIcon)
             {
 				super();
                 this.timeTextView = timeTextView;
                 this.index        = index;
                 this.checkBox     = checkBox;
                 this.defHour      = defHour;
                 this.defMinutes   = defMinutes;
                 this.resIcon      = resIcon;
 			}
             
             
             /**
              * @post Setting prays[index] to false (& removing time text) if we got false,
              *       otherwise...
              *       Calling a PrayTimePickDialog which will:
              *           if (time was SET)    
              *               update prays[index] for us.
              *           if (time was CANCEL) 
              *               check the box again (this must be to false).
              *               Then we will be invoked again, but the last if(...) will happen.
              * @imp  Note: that the "true case" job is completed by the PrayTimePickerDialog.
              */
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
             {
            
                 if (isChecked)
                 {
                     PrayTimePickerDialog goodPractice = new PrayTimePickerDialog(defHour, defMinutes, checkBox, index, resIcon);
                     goodPractice.show();
                 }
                 else
                 {
                     timeTextView.setText("");
                     prays[index] = isChecked;    /** @imp ... = false; */  
                 }                    
             }
         }
         
         
         
         private class MapsQueryAddress extends ACommHandler<MapsQueryLocation>	{
         	private String typed_address;
         	public MapsQueryAddress(String address)	{
         		this.typed_address = address;
         	}
         
         	@Override
 			public void onRecv(final MapsQueryLocation Obj) {
 					activity.runOnUiThread(new Runnable()	{
 
 						public void run() {
 							try	{
 							 if (! editAddress.getText().toString().equals(typed_address))	
 							 {
 								 return;
 							 }
 							 location = new SPGeoPoint(Obj.getResults()[0].getGeometry().getLocation().getLat(), 
 									 Obj.getResults()[0].getGeometry().getLocation().getLng());
 							 editAddress.setBackgroundResource(R.drawable.selector_edittext_green);
 							 addressIncorrect = false;
 							 if (!datesWrong){
 								 createButton.setEnabled(true);
 							 }
 							
 							 
 							} catch (Exception e)	{
 								if (!editAddress.getText().toString().equals(typed_address))	{
 									 return;
 								 }
 								editAddress.setBackgroundResource(R.drawable.selector_edittext_red);
 								addressIncorrect = true;
 								createButton.setEnabled(false);
 							}
 						}
 						
 					});
 			}
         	
         	@Override
         	public void onError(final MapsQueryLocation Obj)	{
         		if (!editAddress.getText().toString().equals(typed_address))	{
 					 return;
 				 }
         		editAddress.setBackgroundResource(R.drawable.selector_edittext_red);
         		addressIncorrect = true;
 				createButton.setEnabled(false);
         	}
         	
         }
         
         private  void verifyAddress(String address)	{
             editAddress.setBackgroundResource(R.drawable.selector_edittext_yellow);
             createButton.setEnabled(false);
         	activity.getSPComm().searchForAddress(address, new MapsQueryAddress(address));
         }
         
         private boolean validateParams(final SPGeoPoint point, final FindPrayer a_activity, final GeneralUser user)	{
         	if (a_activity == null || user == null)
 			{
 				
 			
 				// TODO: change to checked exception
				throw new NullPointerException("CreatePlaceDialog: executed with NULL!!!!");
 				//return;
 			}
         	
         	try {
 				if (user.getSpGeoPoint() == null)	{
 					return false;
 				}
 			} catch (UnknownLocationException e) {
				throw new NullPointerException("CreatePlaceDialog: executed with Unknown Location!!!!");
 			}
         	
         	return true;
         }
 		
         
 		public CreatePlaceDialog(final SPGeoPoint point, final FindPrayer a_activity, final GeneralUser user)	
 		{
 			if (!validateParams(point, a_activity, user))	
 			{
 				return;
 			}
 			
 			for (int i = 0; i < prayTimes.length; prayTimes[i++] = new GregorianCalendar()/*,i++*/);
 			  
 			activity = a_activity;
 			
 			dialog = new NoTitleDialog(activity);
 			dialog.setCancelable(true);
 			dialog.setContentView(R.layout.dialog_place_create);
 			dialog.setTitle(R.string.create_place_title);
 			
 			createButton  = (Button) dialog.findViewById(R.id.CPDCreateButton);
 			cancelButton  = (Button) dialog.findViewById(R.id.CPDCancelButton);
 			
 			createButton.setEnabled(false);
 			
 			editAddress = (EditText) dialog.findViewById(R.id.CPDeditText1);
 			initSearchBar(editAddress);
 			
 			
 			editAddress.setBackgroundResource(R.drawable.selector_edittext_yellow);
 			
 			location = point;
 			
 			editAddress.setOnKeyListener(new OnKeyListener()	{
 
 				public boolean onKey(View v, int keyCode, KeyEvent event) {
 					if (lastEditText.equals(editAddress.getText().toString()))	{
 						return false;
 					}
 					lastEditText = editAddress.getText().toString();
 					verifyAddress(editAddress.getText().toString());
 					return false;
 				}
 				
 			});
 			
 			if (null != location)	{
 				activity.getSPComm().getAddressObj(location.getLatitudeInDegrees(), location.getLongitudeInDegrees(), new ACommHandler<MapsQueryLocation>() {
 					@Override
 					public void onRecv(final MapsQueryLocation Obj) {
 							activity.runOnUiThread(new Runnable()	{
 	
 								public void run() {
 									// TODO Auto-generated method stub
 									try	{
									 editAddress.setText(Obj.getResults()[0].getFormatted_address());
 									 editAddress.setBackgroundResource(R.drawable.selector_edittext_green);
 									 addressIncorrect = false;
 									 if(!datesWrong){
 										 createButton.setEnabled(true);
 									 }
 									 
 									} catch (Exception e)	{
 										addressIncorrect = true;
 										editAddress.setBackgroundResource(R.drawable.selector_edittext_red);
 									}
 								}
 								
 							});
 					}
 					
 					@Override
 					public void onError(MapsQueryLocation Obj) {
 						editAddress.setText("Error fetching address");
 					}
 				});
 			} else	{
 				editAddress.setText("");
 			}
 			
 			fromDate = (TextView) dialog.findViewById(R.id.CPDFromDatetextView);
 			toDate   = (TextView) dialog.findViewById(R.id.CPDToDatetextView);
 			
 			regularTextColor = fromDate.getCurrentTextColor();
 			fromDate.setText(printDateFromCalendar(startDate,0)); 
 	        toDate.setText(printDateFromCalendar(endDate,0)); 
 	        
 	        changeStartDate = (Button) dialog.findViewById(R.id.CPDChange1button);
 	        changeEndDate   = (Button) dialog.findViewById(R.id.CPDChange2button);
 	        
 			changeStartDate.setOnClickListener(new DatePickerClickListener(startDate, fromDate));
 			changeEndDate.setOnClickListener(new DatePickerClickListener(endDate, toDate));
 			
 			
 			
 			checkBoxes[0]    = (CheckBox) dialog.findViewById(R.id.CPDcheckBox1);
 			timeTextViews[0] = (TextView) dialog.findViewById(R.id.CPDshahritTime);
 			checkBoxes[0].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[0], 0, checkBoxes[0], 7, 0, R.drawable.shaharit_small));
 			
 			checkBoxes[1]    = (CheckBox) dialog.findViewById(R.id.CPDcheckBox2);
 			timeTextViews[1] = (TextView) dialog.findViewById(R.id.CPDminhaTime);
 			checkBoxes[1].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[1], 1, checkBoxes[1], 15, 0, R.drawable.minha_small));
 			
 			checkBoxes[2]    = (CheckBox) dialog.findViewById(R.id.CPDcheckBox3);
 			timeTextViews[2] = (TextView) dialog.findViewById(R.id.CPDarvitTime);
 			checkBoxes[2].setOnCheckedChangeListener(new CheckBoxListener(timeTextViews[2], 2, checkBoxes[2], 19, 0, R.drawable.arvit_small));           ////
 	        
 			
 	    
             createButton.setOnClickListener(new OnClickListener()
             {
                 public void onClick(View view)
                 {
                 	final Date finalstartDate = new Date(startDate.get(Calendar.YEAR) - 1900, startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
                     final Date finalendDate   = new Date(endDate.get(Calendar.YEAR) - 1900, endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH));
                     boolean mixedEndStartDates = false;
                     boolean noPrays1 = false;
                     if (!prays[0] && !prays[1] && !prays[2])
                     {
                     	noPrays1 = true;
                                         
                     }
                     if( finalstartDate.compareTo(finalendDate) > 0){
                     	mixedEndStartDates = true;
                     }
                     if(noPrays1 || mixedEndStartDates){
                     	String msg = (noPrays1 ? createPlaceInfoMsg[0] : "") + (mixedEndStartDates ? createPlaceInfoMsg[1] : "");
                     	createAlertDialog(msg, activity, "Cancel");
                     }
                     else
                     {
                         
                         CreateNewPlace_YesClick(prays, user, activity, location, finalstartDate, finalendDate, prayTimes, editAddress.getText().toString());
                         dialog.dismiss();
                     }
                 };
             });
             
             
 			cancelButton.setOnClickListener(new OnClickListener()
 			{
 				public void onClick(View view) 
 				{
 					dialog.dismiss();
 				};
 			});
 			
 			
 			dialog.show();
 		}
 	}
 	
 	
 	
 	public static void createNewPlaceDialog(final SPGeoPoint point, final FindPrayer activity, final GeneralUser user) 
 	{
 		try {
 			new CreatePlaceDialog(point, activity, user);
 		} catch (NullPointerException e)	{
 			createUnknownUserDialog(activity);
 		}
     }
 	
 	
 	
 	static void CreateNewPlace_YesClick(boolean prays[], GeneralUser user, FindPrayer activity, SPGeoPoint point, Date startDate, Date endDate , Calendar[] prayTimes, String address)
 	{
 		String placeName = (user.getFullName()==null || user.getFullName()=="" ? user.getName() : user.getFullName()) + "'s Place";
 		GeneralPlace newMinyan = new GeneralPlace(user, placeName, address, point, startDate,endDate);
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
 				new UpdateUI<Long>(activity) {
 			@Override
 			protected void postSuccessStatus(FindPrayer activity, Long Obg)	{
 				if(activity.getStatusBar()!=null){
 					activity.getStatusBar().write("Place was created successfully!", R.drawable.status_bar_accept_icon, 2000);
 				}
 			}
 			
 			@Override
 			protected void postSuccessFailed(FindPrayer activity, Long Obg)	{
 				if(activity.getStatusBar()!=null){
 					activity.getStatusBar().write("Failed to create a new place.", R.drawable.status_bar_error_icon, 2000);
 				}
 			}
 			
 		});
 		
 		FacebookConnector fc = activity.getFacebookConnector();
 		fc.publishOnFacebook(formatFacebookHeader_NewPlace(newMinyan.getAddress()) , formatFacebookDesc_NewPlace(placeName,prays));
 		
 		
 	}
 	
 	static String formatFacebookHeader_NewPlace(String address){
 		return "Just created a new Minyan place!" + (null == address? "" : " (" + address + ")");
 	}
 	
 	static String formatFacebookDesc_NewPlace(String placeName, boolean prays[]){
 		return "Come and sign to " + placeName + " for " + 
 		(prays[0] ? "Shaharit" : "" ) + (prays[0]&&(prays[1] || prays[2]) ? " or ": "")+ (prays[1] ? "Minha" : "") + (prays[1] && prays[2] ? " or ": "") +  
 		(prays[2] ? "Arvit" : "") + ".";
 	}
 	
 	public static void createUnknownUserDialog(Activity activity)	{
 		Builder builder = new AlertDialog.Builder(activity);
 		builder.setIcon(android.R.drawable.ic_menu_info_details);
 		builder.setTitle(R.string.userUnknownTitle);
 		builder.setMessage(R.string.userUnknownMessage);
 		builder.setPositiveButton(android.R.string.ok, null);
 		builder.show();
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
 	
 	
 	
 	public static int getPrayerIconID(String prayer)
 	{
 		if (prayer.equals("Shaharit")) 
 		    return R.drawable.shaharit_small;
 		
 		if (prayer.equals("Minha")) 
 		    return R.drawable.minha_small;
 		
 		if (prayer.equals("Arvit")) 
 		    return R.drawable.arvit_small;
 		
 		return 0;
 	}
 	
 	
 	
 	
 /////////////////////////////////////////////////////////////////////////////////////////////////////
 ///////// Menu: /////////////////////////////////////////////////////////////////////////////////////
 /////////////////////////////////////////////////////////////////////////////////////////////////////
 
 
 	public static int getContextWidth(Context context)
 	{
 	    return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
 	}
 	
 	
 	public static void setPadding(View view, int padding)
 	{
 	    view.setPadding(padding, padding, padding, padding);
 	}
 	
 	private static GeneralUser getUserByName(List<GeneralUser> joiners, String clickedUserName) {
 		if (joiners == null) return null;
 		for (GeneralUser user : joiners){
 			if (user.getFullName().equals(clickedUserName) ||
 					user.getName().equals(clickedUserName)) return user;
 				
 		}
 		return null;
 	}
 	
 }
