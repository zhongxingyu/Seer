 package com.eolwral.osmonitor.processes;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActivityManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.TabActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.webkit.WebView;
 import android.widget.ListView;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 
 import com.eolwral.osmonitor.*;
 import com.eolwral.osmonitor.messages.DebugBox;
 import com.eolwral.osmonitor.preferences.Preferences;
 
 public class ProcessList extends ListActivity implements OnGestureListener, OnTouchListener
 {
 	// JNILibrary Interface
 	private JNIInterface JNILibrary = JNIInterface.getInstance();
 	private ProcStat ProcStat;
 	private ProcList ProcSnapshot;
 
 	// ProcessInfoQuery Object 
 	private ProcessInfoQuery ProcessInfo = null;
 	 
 	// ProcessList Object
 	private ProcessListAdapter UpdateInterface = null;
 	 
 	// View Statistics TextView
 	private TextView mCPUUsageView = null;
 	private TextView mProcessCountView = null;
 	private TextView mMemoryTotalView = null;
 	private TextView mMemoryFreeView = null;
 
 	private static DecimalFormat MemoryFormat = new DecimalFormat(",000");
 
 	// Short & Click
 	private int longClick = 2;
 	private int shortClick = 3;
 	private boolean shortTOlong = false;
 	private boolean longTOshort = false;
 	
 	// Selected item
 	private int selectedPosition = 0;
 	private String selectedPackageName = null;
 	private int selectedPackagePID = 0;
 	
 	// MultiSelect
 	private CheckBox MultiSelect = null;
 	private Button MultiKill = null;
 
 	// Freeze
 	private CheckBox Freeze = null;
 	private boolean FreezeIt =  false;
 	private boolean FreezeTask = false;
 	
 	// Root
 	private boolean Rooted = false;
 	
 	// Gesture
 	private GestureDetector gestureScanner = new GestureDetector(this);;
 	
 	private boolean GestureLong = false;
 	private boolean GestureSingleTap = false;
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent me)
 	{
 		return gestureScanner.onTouchEvent(me);
 	}
 	
 	@Override
 	public boolean onDown(MotionEvent e)
 	{
 		return false;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
 	{
 		try {
 			if (Math.abs(e1.getY() - e2.getY()) > CommonUtil.SWIPE_MAX_OFF_PATH)
 				return false;
 			else if (e1.getX() - e2.getX() > CommonUtil.SWIPE_MIN_DISTANCE && 
 						Math.abs(velocityX) > CommonUtil.SWIPE_THRESHOLD_VELOCITY) 
 				((TabActivity) this.getParent()).getTabHost().setCurrentTab(1);
 			else if (e2.getX() - e1.getX() > CommonUtil.SWIPE_MIN_DISTANCE &&
 						Math.abs(velocityX) > CommonUtil.SWIPE_THRESHOLD_VELOCITY) 
 				((TabActivity) this.getParent()).getTabHost().setCurrentTab(4);
 			else
 				return false;
 		} catch (Exception e) {
 			// nothing
 		}
 
 		GestureLong = false;
 
 		return true;
 	}
 	
 	@Override
 	public void onLongPress(MotionEvent e)
 	{
 		GestureLong = true;
 		return;
 	}
 	
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
 	{
 		return false;
 	}
 	
 	@Override
 	public void onShowPress(MotionEvent e)
 	{
 		return;
 	} 
 	
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		GestureSingleTap = true;
 		return true;
 	}
 
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		// avoid exception - https://review.source.android.com/#/c/21318/
 		try {
 			event.getY();
 		}
 		catch (Exception e) { 
 			return false;
 		}
 
 		GestureSingleTap = false;
 		
 		if(gestureScanner.onTouchEvent(event))
 		{
 			GestureLong = false;
 			
 			if(GestureSingleTap == true)
 				v.onTouchEvent(event);
 			
 			return true;
 		}
 		else
 		{
 			
 			if(v.onTouchEvent(event))
 				return true;
 			return false;
 		}
 	}
 
 	private Runnable uiRunnable = new Runnable() {
 		public void run() {
 
 			if (JNILibrary.doDataLoad() == 1) {
 				//Multiply the overall CPU usage if we are on the LP cluster
 				int cpuLoad = ProcStat.GetCPUUsageValue();
 				if (JNILibrary.GetTegra3IsTegra3())
 				{
 					if (JNILibrary.GetTegra3ActiveCpuGroup() != null)
 					{
 						if (JNILibrary.GetTegra3IsLowPowerGroupActive())
 						{
 							cpuLoad = (int)(
 									ProcStat.GetCPUUsageValueFloat() * JNILibrary.GetTegra3EnabledCoreCount() * 100);
 						}
 					}
 				}
 				
 				mCPUUsageView.setText(cpuLoad + "%");
 				mProcessCountView.setText(ProcSnapshot.GetProcessCounts() + "");
 				mMemoryTotalView.setText(MemoryFormat.format(JNILibrary.GetMemTotal()) + "K");
 				mMemoryFreeView.setText(MemoryFormat.format(JNILibrary.GetMemBuffer()
 						+ JNILibrary.GetMemCached()
 						+ JNILibrary.GetMemFree()) + "K");
 
 				JNILibrary.doDataSwap();
 				ProcStat.Update();
 				ProcSnapshot = ProcList.Collect();
 				UpdateInterface.notifyDataSetChanged();
 			}
 			else
 			{
 				if(FreezeIt)
 				{
 					if(!FreezeTask) {
 						JNILibrary.doTaskStop();
 						FreezeTask = true;
 					} else {
 						mCPUUsageView.setText(ProcStat.GetCPUUsageValue() + "%");
 					}
 				} else {
 					if (FreezeTask) {
 						JNILibrary.doTaskStart(JNILibrary.doTaskProcess);
 						FreezeTask = false;
     				}
     			}
 
      		}
 
 	        uiHandler.postDelayed(this, 500);
 		}
 	};   
 	
 	private Handler uiHandler = new Handler();
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         ProcStat = new ProcStat();
         ProcSnapshot = ProcList.Empty();
 
         // Use a custom layout file
         setContentView(R.layout.processlayout);
 
         mCPUUsageView = (TextView) findViewById(R.id.CPUUsage);
         mMemoryTotalView = (TextView) findViewById(R.id.MemTotalText);
         mMemoryFreeView = (TextView) findViewById(R.id.MemFreeText);
         mProcessCountView = (TextView) findViewById(R.id.RunProcessText);
         
         // Tell the list view which view to display when the list is empty
         getListView().setEmptyView(findViewById(R.id.empty));
 
         // Use our own list adapter
         getListView().setOnTouchListener(this);
         setListAdapter(new ProcessListAdapter());
         UpdateInterface = (ProcessListAdapter) getListAdapter();
         ProcessInfo = ProcessInfoQuery.getInstance(this);
         
         // MultiKill
         MultiKill = (Button) findViewById(R.id.MultiKill);
         MultiKill.setOnClickListener(
           	new OnClickListener(){
            		public void onClick(View v) {
            			
            			String KillCmd = ""; 
            			ActivityManager ActivityMan = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            			ArrayList<String> KillList = ProcessInfo.getSelected();
            			for(String pid:KillList)
            			{
            				int tPID = Integer.parseInt(pid);
            				
            	   	        if(Rooted)
            	   	        {
            	   	        	if(KillCmd.length() == 0)
            	   	        		KillCmd += "kill -9 "+tPID;
            	   	        	else
            	   	        		KillCmd += ";kill -9 "+tPID;
            	   	        }
            	   	        else
            	   	        {
      	   	        		android.os.Process.killProcess(tPID);
        	   	        		ActivityMan.restartPackage(ProcSnapshot.GetProcessName(tPID));
            	   	        }
            			}
            			
            			if(Rooted)
            				CommonUtil.execCommand(KillCmd+"\n");
 
          	        ProcessInfo.clearSelected();
 
          	        JNILibrary.doDataRefresh();
          	        ProcSnapshot = ProcList.Collect();
          	        
          	        UpdateInterface.notifyDataSetChanged();
 
          	        // Display message
          	        String KillMsg = getResources().getString(R.string.process_killmsg);
          	        Toast.makeText(getApplication(), KillMsg.replace("$COUNT$", KillList.size()+"") ,
            									Toast.LENGTH_SHORT).show();
     			}
            	}
         );
         
         // Freeze
         Freeze = (CheckBox) findViewById(R.id.Freeze);
         Freeze.setOnClickListener(
         	new OnClickListener(){
         		public void onClick(View v) {
         			if(FreezeIt)
         				FreezeIt = false;
         			else
         				FreezeIt = true;
 				}
         	}
         );
         
         // MultiSelect
         MultiSelect = (CheckBox) findViewById(R.id.MultiSelect);
         MultiSelect.setOnCheckedChangeListener(
         	new CompoundButton.OnCheckedChangeListener() {
         		@Override
         		public void onCheckedChanged( CompoundButton buttonView, boolean isChecked) 
         		{
         			if(isChecked)
         			{
         				MultiKill.setEnabled(true);
         			}
         			else
         			{
         				MultiKill.setEnabled(false);
         				ProcessInfo.clearSelected();
         				UpdateInterface.notifyDataSetChanged();
         			}
         		}
         	}
         );
         
         // Change Order
         TextView OrderBy =(TextView) findViewById(R.id.OrderType) ;
         OrderBy.setOnClickListener(
           	new OnClickListener(){
                	
           		public void onClick(View v) {
           		    String[] Sortby = getResources().getStringArray(R.array.entries_list_sort);
 
           		 	AlertDialog.Builder builder = new AlertDialog.Builder(ProcessList.this);
            	    	//builder.setTitle(getApplication().getResources().getString(R.string.pref_sortbytitle_text));
            	    	builder.setSingleChoiceItems(Sortby, (int) UpdateInterface.OrderBy-1, new DialogInterface.OnClickListener(){
 
            	    		@Override
            	    		public void onClick(DialogInterface dialog, int which) {
            	    			ProcList.SetProcessSort(which+1);
            	    	        
            	    			// change display
            	    	        TextView OrderType = (TextView) findViewById(R.id.OrderType);
            	    	        
            	    	        switch(which+1)
            	    	        {
            	    	        case 1:
            	    	        case 2:
            	    	        case 5:
            	    	        	OrderType.setText(getResources().getString(R.string.process_load));
            	    	        	break;
            	    	        case 3:
            	    	        	OrderType.setText(getResources().getString(R.string.process_mem));
            	    	        	break; 
            	    	        case 4:
            	    	        	OrderType.setText(getResources().getString(R.string.process_thread));
            	    	        	break;
            	    	        }
            	    	        
            	    	        UpdateInterface.OrderBy = which+1;
            	    	        
            	    			dialog.dismiss();
            	    		}
 
            	    	});
            	    	builder.show();   	    	               		
           		}
           	}        		
     	);
     
         // restore
         registerForContextMenu(getListView());
     }
     
 	private void restorePrefs()
     {
 		boolean ExcludeSystem = false;
 		boolean SortIn = false;
 		int OrderBy = 0;
 		int Algorithm = 1;
 
 		// load settings
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 
 		try {
 			longClick = Integer.parseInt(settings.getString(Preferences.PREF_LONGBEHAVIOR, "2"));
 			shortClick = Integer.parseInt(settings.getString(Preferences.PREF_SHORTBEHAVIOR, "3"));
 			JNILibrary.doDataTime(Integer.parseInt(settings.getString(Preferences.PREF_UPDATE, "2")));
 			OrderBy =  Integer.parseInt(settings.getString(Preferences.PREF_ORDER, "2"));
 		} catch(Exception e) {}
 
 	    SortIn = settings.getBoolean(Preferences.PREF_SORT, true);
 	    ExcludeSystem = settings.getBoolean(Preferences.PREF_EXCLUDE, true);
 	    
 	    // change options
 	    ProcList.SetProcessSort(OrderBy);
    		ProcList.SetProcessAlgorithm(Algorithm);
    		
         if(ExcludeSystem)
     		ProcList.SetProcessFilter(1);
         else
         	ProcList.SetProcessFilter(0);
         
         if(SortIn)
         	ProcList.SetProcessOrder(0);
         else 
         	ProcList.SetProcessOrder(1);
         
         // change display
         TextView OrderType = (TextView) findViewById(R.id.OrderType);
         
         switch(OrderBy)
         {
         case 1:
         case 2:
         case 5:
         	OrderType.setText(getResources().getString(R.string.process_load));
         	break;
         case 3:
         	OrderType.setText(getResources().getString(R.string.process_mem));
         	break;
         case 4:
         	OrderType.setText(getResources().getString(R.string.process_thread));
         	break;
         }
         
         UpdateInterface.OrderBy = OrderBy;
         
         // Display extra area
     	TableLayout Msv = (TableLayout) findViewById(R.id.MultiSelectView);
         if(settings.getBoolean(Preferences.PREF_HIDEMULTISELECT, false))
         	Msv.setVisibility(View.GONE);
         else
         	Msv.setVisibility(View.VISIBLE);
                 
         // Status Bar
         if(settings.getBoolean(Preferences.PREF_STATUSBAR, false))
         {
         	if(OSMonitorService.getInstance() == null)
         		startService(new Intent(this, OSMonitorService.class));
         	else
         		OSMonitorService.getInstance().Notify();
         }
         else
         	if(OSMonitorService.getInstance() != null)
         		OSMonitorService.getInstance().stopSelf();
 
         // Root
 		Rooted = settings.getBoolean(Preferences.PREF_ROOTED, false);
     }
 
     public boolean onCreateOptionsMenu(Menu optionMenu) 
     {
      	optionMenu.add(0, 1, 0, getResources().getString(R.string.menu_options));
        	optionMenu.add(0, 4, 0, getResources().getString(R.string.menu_help));
        	optionMenu.add(0, 5, 0, getResources().getString(R.string.menu_forceexit));
     	return true;
     }
     
     
     @Override
     protected Dialog onCreateDialog(int id) 
     {
     	switch (id)
     	{
     	case 0:
     		AlertDialog.Builder HelpWindows = new AlertDialog.Builder(this);
     		HelpWindows.setTitle(R.string.app_name);
 			HelpWindows.setMessage(R.string.help_info);
 			HelpWindows.setPositiveButton(R.string.button_close,
 			   new DialogInterface.OnClickListener() {
 				   public void onClick(DialogInterface dialog, int whichButton) { }
 				}
 			);
 
    	        WebView HelpView = new WebView(this);
             HelpView.loadUrl("http://wiki.android-os-monitor.googlecode.com/hg/phonehelp.html?r=b1c196ee43855882e59ad5b015b953d62c95729d");
             HelpWindows.setView(HelpView);
 
         	return HelpWindows.create(); 
         	
     	case 1:
     		return null;
     	}
     	
     	return null;
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
     	restorePrefs();
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	      
         super.onOptionsItemSelected(item);
         switch(item.getItemId())
         {
         case 1:
             Intent launchPreferencesIntent = new Intent().setClass( this, Preferences.class);
             startActivityForResult(launchPreferencesIntent, 0);
         	break;
         	
         case 4:
         	this.showDialog(0);
         	break;
         	
         case 5:
         	if(OSMonitorService.getInstance() != null)
         		OSMonitorService.getInstance().stopSelf();
 
         	CommonUtil.killSelf(this);
 
         	break;
         }
         
         return true;
     }
 
     @Override
     public void onPause() 
     {
     	uiHandler.removeCallbacks(uiRunnable);
     	JNILibrary.doTaskStop();
     	
     	if(MultiSelect.isChecked())
     	{
 			MultiSelect.setChecked(false);
 			MultiKill.setEnabled(false);
 			ProcessInfo.clearSelected();
     	}
     	
      	super.onPause();
     }
 
     @Override
     protected void onResume() 
     {    
         restorePrefs();
         
         if(!FreezeIt)
             JNILibrary.doTaskStart(JNILibrary.doTaskProcess);
     	uiHandler.post(uiRunnable);
     	super.onResume();
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 
     	
     	boolean useMenu = false; 
     	ProcessDetailView selectedItemView =  (ProcessDetailView) 
     							((AdapterContextMenuInfo)menuInfo).targetView;
 
     	if(!GestureLong)
     	{
     		selectedItemView.setSelected(true);
     		return;
     	}
 
     	String selectedProcessName;
     	selectedPosition = (int) ((AdapterContextMenuInfo)menuInfo).position;
     	selectedPackagePID = ProcSnapshot.GetProcessPID(selectedPosition);
     	selectedProcessName = ProcSnapshot.GetProcessName(selectedPackagePID);
     	selectedPackageName = ProcessInfo.getPacakge(selectedProcessName);
  
     	if(shortTOlong)
     	{
     		useMenu = true;
     		shortTOlong = false;
     	}
     	else
     	{
     		if(longClick == 1)
     			((ProcessListAdapter)getListAdapter()).toggle(selectedItemView,
     														  selectedPosition,
     														  false,
     														  false);
     		else if(longClick == 2)
         		useMenu = true;
     		else if(longClick == 3)
         		if(!((ProcessListAdapter)getListAdapter()).toggle(selectedItemView,
         														 selectedPosition,
         														 true,
         														 false))
         			useMenu = true;
 
     	}
 
 
     	if(useMenu)
       	{
     		String ProcessName = ProcSnapshot.GetProcessName(selectedPackagePID);
        		menu.setHeaderTitle(ProcessInfo.getPackageName(ProcessName));
        		menu.add(0, 1, 0, getResources().getString(R.string.process_kill));
        		menu.add(0, 2, 0, getResources().getString(R.string.process_switch));
        		menu.add(0, 3, 0, getResources().getString(R.string.process_watchlog));
        		
        		if(Rooted)
        			menu.add(0, 4, 0, getResources().getString(R.string.process_nice));
        		
        		menu.add(0, 5, 0, getResources().getString(R.string.button_cancel));
     	}
     	else
     	{
     		menu.clear();
     		longTOshort = true;
     	}
     	
 
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) 
     {
         switch(item.getItemId()) 
    	    {
    	    case 1:
    	    	ActivityManager ActivityMan = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    	    	if(Rooted)
    	        {
    	    		CommonUtil.execCommand("kill -9 "+selectedPackagePID+"\n");
    	        }
    	        else
    	        {
    	        	android.os.Process.killProcess(selectedPackagePID);
    	        	ActivityMan.restartPackage(selectedPackageName);
    	        }
    	        
    	        if(FreezeIt && FreezeTask)
    	        {
    	        	JNILibrary.doTaskStart(JNILibrary.doTaskProcess);
    	        	JNILibrary.doDataRefresh();
    	        	JNILibrary.doTaskStop();
    	        }
    	        else 
    	        {
    	        	JNILibrary.doDataRefresh();
    	        }
 
    	        ProcSnapshot = ProcList.Collect();
    	        UpdateInterface.notifyDataSetChanged();
    	        
    	        return true;
    	        
    	    case 2:
 
    	    	String ClassName = null;
    	        
    	    	// find ClassName
    	    	PackageManager QueryPackage = this.getPackageManager();
    	        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
    	        List<ResolveInfo> appList = QueryPackage.queryIntentActivities(mainIntent, 0);
    	        for(int i=0; i<appList.size(); i++)
    	        {
    	        	if(appList.get(i).activityInfo.applicationInfo.packageName.equals(selectedPackageName))
    	        		ClassName = appList.get(i).activityInfo.name;
    	        }
    	        
    	        if(ClassName != null)
    	        {
    	   	        Intent switchIntent = new Intent();
    	   	        switchIntent.setAction(Intent.ACTION_MAIN);
    	   	        switchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	   	        switchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
    	   	        		   			  Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
    	   	        		   			  Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
    	   	        switchIntent.setComponent(new ComponentName(selectedPackageName, ClassName));
    	   	        startActivity(switchIntent);
    	   	        finish();
    	        }
    	        return true;
    	        
    	    case 3:
    	    	Intent WatchLog =  new Intent(this, DebugBox.class);
    	    	WatchLog.putExtra("targetPID", selectedPackagePID);
    	    	startActivity(WatchLog);
    	    	return true;
    	    	
    	    case 4:
    	    	CharSequence[] NiceValue = {"-20", "-19", "-18", "-17", "-16", "-15", "-14", "-13", "-12", "-11",
    	    								"-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1",
    	    								"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
    	    								"10", "11", "12", "13", "14", "15", "16", "17", "18", "19"};
 
    	    	AlertDialog.Builder builder = new AlertDialog.Builder(ProcessList.this);
    	    	builder.setTitle(ProcessList.this.getResources().getString(R.string.process_nice));
 
    	    	builder.setSingleChoiceItems(NiceValue, (int) (ProcSnapshot.GetProcessNice(selectedPackagePID)+20),
    	    			new DialogInterface.OnClickListener(){
 
    	    		@Override
    	    		public void onClick(DialogInterface dialog, int which) {
    	    			CommonUtil.execCommand(CommonUtil.NiceCMD+" "+selectedPackagePID+" "+(which-20));
    	    			dialog.dismiss();
    	    		}
 
    	    	});
    	    	builder.show();   	    	
    	    	return true;
    	    }
    	    return super.onContextItemSelected(item);
  	}    
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id)
     {
     	if(!GestureSingleTap && !GestureLong)
     	{
     		((ProcessDetailView) v).setSelected(false);
     		return;
     	}
     	
     	if(GestureLong)
     	{
     		v.performLongClick();
     		return;
     	}
     	
     	if(longTOshort)
     	{
     		longTOshort = false;
     		return;
     	}
     	
     	if(MultiSelect.isChecked())
     	{
     		((ProcessListAdapter)getListAdapter()).toggle((ProcessDetailView) v,
 					position, false, MultiSelect.isChecked());
     		return;
     	}
     	
     		
     	if(shortClick == 1)
     		((ProcessListAdapter)getListAdapter()).toggle((ProcessDetailView) v,
 											position, false, false);
     	else if(shortClick == 2)
 		{
 			shortTOlong = true;
 			GestureLong = true;
 			v.performLongClick();
 		}
     	else if(shortClick == 3)
     		if(!((ProcessListAdapter)getListAdapter()).toggle((ProcessDetailView) v,
     		   								position, true, false))
     		{
     			shortTOlong = true;
     			GestureLong = true;
     			if(v != null)
     				v.performLongClick();
     		}
     }
     
     private class ProcessListAdapter extends BaseAdapter {
     	
     	public int OrderBy = ProcList.doSortPID;
     	
         public int getCount() {
             return ProcSnapshot.GetProcessCounts();
         }
 
         public Object getItem(int position) {
             return position;
         }
      
         public long getItemId(int position) {
             return position;
         }
  
         public View getView(int position, View convertView, ViewGroup parent) {
 
         	int ProcessID = ProcSnapshot.GetProcessPID(position);
         	
             ProcessDetailView sv = null;
             ProcessInfo.doCacheInfo(ProcSnapshot, position);
 
         	String OrderValue = "";
         	 
         	switch(OrderBy)
         	{
         	case 1:
         	case 2:
         	case 5:
         		OrderValue = ProcSnapshot.GetProcessLoad(ProcessID)+"%";
         		break;
         	case 3:
         		if(ProcSnapshot.GetProcessRSS(ProcessID) > 1024) 
         			OrderValue = (ProcSnapshot.GetProcessRSS(ProcessID)/1024)+"M";
         		else
         			OrderValue = ProcSnapshot.GetProcessRSS(ProcessID)+"K";
         		break;
         	case 4:
         		OrderValue = ProcSnapshot.GetProcessThreads(ProcessID)+"";
         		break;
         	}
         	
     		Drawable DetailIcon = null;
     		if(!ProcessInfo.getExpaned(ProcessID))
         		DetailIcon = getApplication().getResources().getDrawable(R.drawable.dshow);
     		else
     			DetailIcon = getApplication().getResources().getDrawable(R.drawable.dclose);
 
     		
    		String ProcessName = ProcSnapshot.GetProcessName(ProcessID);
     		if (convertView == null) {
                 sv = new ProcessDetailView(getApplication(), ProcessInfo.getAppIcon(ProcessName),
                 							ProcessID,
                 							ProcessInfo.getPackageName(ProcessName),
                 							OrderValue,
         	        						ProcessInfo.getAppInfo(ProcSnapshot, ProcessID), 
         	        						ProcessInfo.getExpaned(ProcessID),
         	        						position,
 	               							DetailIcon);
             } 
             else
             {
                 sv = (ProcessDetailView)convertView;
                	sv.setView( ProcessInfo.getAppIcon(ProcessName), 
                				ProcessID,
                				ProcessInfo.getPackageName(ProcessName),
                				OrderValue,
                				position,
                				DetailIcon);
                 
                	if(ProcessInfo.getExpaned(ProcessID))
                		sv.setContext(ProcessInfo.getAppInfo(ProcSnapshot, ProcessID));
                	
                 sv.setExpanded(ProcessInfo.getExpaned(ProcessID));
                 sv.setMultiSelected(ProcessInfo.getSelected(ProcessID));
         	}
             
            	return sv;
         }
         
         public boolean toggle(ProcessDetailView v, int position, boolean split, boolean multi) {
     		int ProcessID = ProcSnapshot.GetProcessPID(position);
 
     		if(multi)
     		{
     			if(ProcessInfo.getSelected(ProcessID))
     				ProcessInfo.setSelected(ProcessID, false);
     			else
     				ProcessInfo.setSelected(ProcessID, true);
     			
             	notifyDataSetChanged();
             	
             	return false;
     		}
 
         	if(v.checkClick() != 1 && split == true) 
         	{
         		return false;
         	}
         	else
         	{
             	if(ProcessInfo.getExpaned(ProcessID))
             		ProcessInfo.setExpaned(ProcessID, false);
             	else
             		ProcessInfo.setExpaned(ProcessID, true);
         	}
 
         	notifyDataSetChanged();
         	
         	return true;
         }
     }
     
     private class ProcessDetailView extends TableLayout {
     	
     	private TableRow TitleRow;
     	private TextView PIDField;
     	private ImageView IconField;
     	private TextView NameField;
     	private ImageView DetailField;
     	private TextView ValueField;
     	private TextView AppInfoField;
     	
     	private boolean Expanded = false;
     	
         public ProcessDetailView(Context context, Drawable Icon, int PID, String Name,
         						 String Value, String AppInfo, boolean expanded, int position,
         						 Drawable DetailIcon) {
             super(context);
             this.setColumnStretchable(2, true);
             
             PIDField = new TextView(context);
             IconField = new ImageView(context);  
             NameField = new TextView(context);
 
             ValueField = new TextView(context);
             AppInfoField = new TextView(context);
             DetailField = new ImageView(context);
 
             DetailField.setImageDrawable(DetailIcon);
             DetailField.setPadding(3, 3, 3, 3);
             
             PIDField.setText(""+PID);
 
            	IconField.setImageDrawable(Icon);
            	IconField.setPadding(8, 3, 3, 3);
             
             NameField.setText(Name);
 	     	ValueField.setText(Value);
 
             PIDField.setGravity(Gravity.LEFT);
             PIDField.setPadding(3, 3, 3, 3);
             if(CommonUtil.getScreenSize() == 2)
             	PIDField.setWidth(90);
             else if(CommonUtil.getScreenSize() == 0)
             	PIDField.setWidth(35);
             else
             	PIDField.setWidth(55);
 
             NameField.setPadding(3, 3, 3, 3);
             NameField.setGravity(Gravity.LEFT);
             NameField.setWidth(getWidth()- IconField.getWidth()
             						- DetailField.getWidth() - 115);
 
             ValueField.setPadding(3, 3, 8, 3);
 
             if(CommonUtil.getScreenSize() == 2)
             	ValueField.setWidth(80);
             else if (CommonUtil.getScreenSize() == 0)
             	ValueField.setWidth(35);
             else
             	ValueField.setWidth(50);
             
             TitleRow = new TableRow(context);
             TitleRow.addView(PIDField);
             TitleRow.addView(IconField);
             TitleRow.addView(NameField);
             TitleRow.addView(ValueField);
             TitleRow.addView(DetailField);
             addView(TitleRow);
 
 	     	AppInfoField.setText(AppInfo);
             addView(AppInfoField);
             AppInfoField.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT
             	     									, LayoutParams.MATCH_PARENT) );
 	     	AppInfoField.setVisibility(expanded ? VISIBLE : GONE);
 	     	
 	     	if(position % 2 == 0)
 	     		setBackgroundColor(0x80444444);
 	     	else
 	     		setBackgroundColor(0x80000000);
 
         }
  
         public void setContext(String AppInfo) {
        		AppInfoField.setText(AppInfo);
 		}
 
 		public void setView( Drawable Icon, int PID, String Name, 
 							String Value, int position, Drawable DetailIcon) {
 
 			IconField.setImageDrawable(Icon);
 			DetailField.setImageDrawable(DetailIcon);
 			PIDField.setText(""+PID);
 			NameField.setText(Name);
 			ValueField.setText(Value);
 
 			if(position % 2 == 0)
 				setBackgroundColor(0x80444444);
 			else
 				setBackgroundColor(0x80000000);
     	}
 
         /**
          * Convenience method to expand or hide the dialogue
          */
         public void setExpanded(boolean expanded) {
         	AppInfoField.setVisibility(expanded ? VISIBLE : GONE);
         }
         
         public void setMultiSelected(boolean selected) {
         	if(selected)
         		setBackgroundColor(0x803CC8FF);
         }
         
 		public boolean onTouchEvent(MotionEvent event)
 		{
 			if(event.getX() > getWidth()/3*2 )
 				Expanded = true;
 			else if (event.getX() <= getWidth()/3*2 )
 				Expanded = false;
 
 			return super.onTouchEvent(event);
 		}
         
         public int checkClick()
         {
         	if(Expanded == true)
         	{ 
         		Expanded = false;
         		return 1;
         	} 
 			return 0;
 		}
 
 	}
 
 }
