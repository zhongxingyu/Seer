 package com.dima.tkswidget;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProviderInfo;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.PeriodicSync;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.RemoteViews;
 
 import com.dima.tkswidget.activity.ActionSelect;
 import com.dima.tkswidget.provider.BaseProvider;
 import com.google.api.services.tasks.model.Task;
 import com.google.api.services.tasks.model.TaskList;
 
 
 public class WidgetController {
 	public static String ACCOUNT_TYPE = "com.google";
 	
     //private static final String WIDGET_IDS_EXTRA = "widgetIds";
     //private static final String SILENT_MODE_EXTRA = "silent";
     private final static String NEW_LINE = System.getProperty("line.separator");
     
     private static final String LIST_CLICK_ACTION = "com.dima.taskwidget.OPEN_TASKS";
     private static final String TASKS_CLICK_ACTION = "com.dima.taskwidget.OPEN_CFG";
     
     public static final String TASKS_SYNC_STATE = "com.dima.taskwidget.TASKS_SYNC_STATE";
     public static final int SYNC_STATE_STARTED = 1;
     public static final int SYNC_STATE_LISTS_UPDATED = 2;
     public static final int SYNC_STATE_TASKS_UPDATED = 3;
     public static final int SYNC_STATE_FINISHED = 4;
 
     public static final Uri GOOGLE_TASKS_URI = Uri.parse("https://mail.google.com/tasks/android");
 
 	private static class AccountWidgets {			
 		public String accountName;
 		public List<Integer> widgetsIds;
 	}
 	
 	
 	protected final Context m_context;
     protected final AppWidgetManager m_widgetManager;
     protected final TaskProvider m_taskSource;
 	protected final SettingsController m_settings;
 	
 	
 	public WidgetController(Context context, AppWidgetManager widgetManager) {
 		m_context = context;
         m_widgetManager = widgetManager == null
             ? AppWidgetManager.getInstance(m_context)
             : widgetManager;
 		m_settings = new SettingsController(m_context);
 		m_taskSource = new TaskProvider(m_context);
 	}
 
 
     public void setSyncFreq(long freq) {
         int[] ids = getWidgetsIds();
         setSyncFreq(ids, freq);
     }
 
     public void setSyncFreq(int[] widgetIds, long freq) {
         List<AccountWidgets> aw = getGroupedAccounts(widgetIds);
 
         for (AccountWidgets accountWidgets : aw) {
             Account acc = getAccount(accountWidgets.accountName);
             ContentResolver.addPeriodicSync(acc, TaskMetadata.AUTHORITY, Bundle.EMPTY, freq);
         }
     }
     
     public long getSyncFreq(int widgetId) {
 		String accName = m_settings.loadWidgetAccount(widgetId);
 		if (accName == null) {
 			return 0;
 		}
 
 		Account acc = getAccount(accName);
     	List<PeriodicSync> sync = ContentResolver.getPeriodicSyncs(acc, TaskMetadata.AUTHORITY);
     	if (sync.size() <= 0)
     		return 0;
     	
     	return sync.get(0).period;
     }
     
 	public void startSync() {
 		int[] ids = getWidgetsIds();
 		startSync(ids);
 	}
 	
 	public void startSync(int[] widgetIds) {
 		List<AccountWidgets> aw = getGroupedAccounts(widgetIds);
 		
 		for (AccountWidgets accountWidgets : aw) {
 			Account acc = getAccount(accountWidgets.accountName);
 			ContentResolver.requestSync(acc, TaskMetadata.AUTHORITY, Bundle.EMPTY);
 		}
 	}
 	
 	public boolean isSyncInProgress(int widgetId) {
 		String accName = m_settings.loadWidgetAccount(widgetId);
 		if (accName == null) {
 			return false;
 		}
 
 		Account acc = getAccount(accName);
 		boolean r = ContentResolver.isSyncActive(acc, TaskMetadata.AUTHORITY);
 		return r;
 	}
 
     public void updateWidgetsAsync() {
         int[] ids = getWidgetsIds();
         updateWidgetsAsync(ids);
     }
 
     public void updateWidgetsAsync(int[] widgetIds) {
         WidgetControllerService.updateWidgets(m_context, widgetIds);
     }
 
 	public void updateWidgets() {
         int[] ids = getWidgetsIds();
         updateWidgets(ids);
 	}
 
     public void updateWidgets(int[] widgetIds) {
         for (int id : widgetIds) {
             updateWidget(id);
         }
     }
 
     public void updateWidget(int widgetId) {
         LogHelper.d("Updating widget with id:");
         LogHelper.d(String.valueOf(widgetId));
 
         RemoteViews views = prepareWidget(widgetId);
         m_widgetManager.updateAppWidget(widgetId, views);
     }
 
	public void notifySyncState(int state) {
 		Intent intent = new Intent(m_context, BaseProvider.class);
 
 		intent.setAction(TASKS_SYNC_STATE);
		intent.putExtra(TASKS_SYNC_STATE, state);
 		
 		m_context.sendBroadcast(intent);	
 	}
 	
 	public void performAction(String actionName, Intent intent) {
         if (actionName == null) {
             return;
         }
 
         if (actionName.equals(LIST_CLICK_ACTION)) {     
         	int wId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        	openCfgGUI(wId);
         	
         } else if (actionName.equals(TASKS_CLICK_ACTION)) {
         	openTasksGUI();
         	
         } else if (actionName.equals(TASKS_SYNC_STATE)) {
        	int flag = intent.getIntExtra(TASKS_SYNC_STATE, -1);
         	
         	if (flag == SYNC_STATE_STARTED) {
         		setUpdateState(true);
         		
         	} else if (flag == SYNC_STATE_FINISHED) {
                 updateWidgetsAsync(); //to restore remote view state
         	}
         }
 	}
 
 
     protected RemoteViews getWidgetViews(int widgetId){
         String packName = m_context.getPackageName();
         LogHelper.d(packName);
         AppWidgetProviderInfo appWidgetInfo = m_widgetManager.getAppWidgetInfo(widgetId);
         return new RemoteViews(packName, appWidgetInfo.initialLayout);
     }
 
     protected RemoteViews prepareWidget(int widgetId) {
         LogHelper.d("Preparing widget with id:");
         LogHelper.d(String.valueOf(widgetId));
 
         RemoteViews views = getWidgetViews(widgetId);
 
         setupEvents(views, widgetId);
         updateWidget(views, widgetId);
         applySettings(views, widgetId);
         setUpdateState(views, widgetId, false);
 
         return views;
     }
 
     protected void updateWidget(RemoteViews views, int widgetId) {
         String listId = m_settings.loadWidgetList(widgetId);
         if (listId == null) {
             return;
         }
         TaskList list = m_taskSource.getList(listId);
         List<Task> tasks = m_taskSource.getListTasks(listId);
 
         updateWidget(views, list, tasks);
     }
 
     protected void applySettings(RemoteViews views, int widgetId) {
         Boolean margin = m_settings.loadWidgetMargin(widgetId);
         setMargin(views, margin);
     }
 
     protected void setMargin(RemoteViews views, Boolean margin) {
         int v = margin ? View.VISIBLE : View.GONE;
         views.setViewVisibility(R.id.spacer_bottom, v);
         views.setViewVisibility(R.id.spacer_top, v);
         views.setViewVisibility(R.id.spacer_left, v);
         views.setViewVisibility(R.id.spacer_right, v);
     }
 
     protected void setupEvents(RemoteViews views, int widgetId) {
 		try {
 			AppWidgetProviderInfo appWidgetInfo = m_widgetManager.getAppWidgetInfo(widgetId);
 			Class<?> providerClass = Class.forName(appWidgetInfo.provider.getClassName());
 
 	        PendingIntent actionPendingIntent = setupEvent(widgetId, providerClass, LIST_CLICK_ACTION);
 	        views.setOnClickPendingIntent(R.id.textViewList, actionPendingIntent);
 
 	        actionPendingIntent = setupEvent(widgetId, providerClass, TASKS_CLICK_ACTION);
 	        views.setOnClickPendingIntent(R.id.textViewTasks, actionPendingIntent);        
 		} catch (ClassNotFoundException e) {
 			LogHelper.e("Provider class not found", e);
 		}
 	}
 
     protected PendingIntent setupEvent(int widgetId, Class<?> providerClass, String action) {
         Intent intent = new Intent(m_context, providerClass);
         intent.setAction(action);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
         intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
         
         return PendingIntent.getBroadcast(m_context, 0, intent, 0);
     }
     
 	protected void setUpdateState(Boolean updating) {
         int[] widgetIds = getWidgetsIds();
 
 		for (int id : widgetIds) {
 			RemoteViews views = getWidgetViews(id);
             setUpdateState(views, id, updating);
 			m_widgetManager.updateAppWidget(id, views);
 		}
 	}
 
     protected void setUpdateState(RemoteViews views, int widgetId, Boolean updating) {
         int v = updating ? View.VISIBLE : View.GONE;
         views.setViewVisibility(R.id.imageRefresh, v);
     }
 
 	protected void openTasksGUI() {
     	LogHelper.i("widget textViewList clicked");
         Intent openBrowser = new Intent(Intent.ACTION_VIEW, GOOGLE_TASKS_URI);
         openBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         m_context.startActivity(openBrowser);    	
     }
     
 	protected void openCfgGUI( final int widgetId) {
     	LogHelper.i("widget textViewTasks clicked");
 
         Intent openCfg = new Intent(m_context, ActionSelect.class);
         openCfg.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
         openCfg.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     	
         m_context.startActivity(openCfg);
     }
     
     protected int[] getWidgetsIds() {
     	List<AppWidgetProviderInfo> installedProviders = getMyProviders();
     	if (installedProviders.size() == 1) {
     		return m_widgetManager.getAppWidgetIds(installedProviders.get(0).provider);
     	}
     	
     	List<int[]> result = new ArrayList<int[]>();
     	for (AppWidgetProviderInfo appWidgetProviderInfo : installedProviders) {
     		int[] appWidgetIds = m_widgetManager.getAppWidgetIds(appWidgetProviderInfo.provider);
     		result.add(appWidgetIds);
 		}
     	
         return combine(result);
     }
     
     protected List<AppWidgetProviderInfo> getMyProviders() {
     	String myPackName = m_context.getPackageName();
     	List<AppWidgetProviderInfo> installedProviders = m_widgetManager.getInstalledProviders();
     	
     	for (int i = installedProviders.size() - 1; i >= 0; i--) {
     		AppWidgetProviderInfo info = installedProviders.get(i);
     		String pack = info.provider.getPackageName();
     		if (!pack.equals(myPackName))
     			installedProviders.remove(i);
 		}
     	
     	return installedProviders;
     }
     
     private static int[] combine(Collection<int[]> arrays){
         int length = 0;
         for (int[] arr : arrays) {
         	length += arr.length;
 		}
         
         int[] result = new int[length];
         int pos = 0;
         for (int[] arr : arrays) {
         	System.arraycopy(arr, 0, result, pos, arr.length);
         	pos += arr.length;
 		}
 
         return result;
     }
 
 	protected boolean updateWidget(
 		RemoteViews views,
 		TaskList list,
 		List<Task> tasks) {
 	    	
     	try
     	{
 	    	StringBuffer bufferTasks = new StringBuffer();
 	    	StringBuffer bufferCompletedTasks = new StringBuffer();
 	    	for (int i = 0; i < tasks.size(); i++) {
 	    		Task tsk = tasks.get(i);
 	    		
 	    		if (tsk.getStatus().equals("completed")) {
 	    			bufferCompletedTasks.append(tsk.getTitle());
 	    			bufferCompletedTasks.append(NEW_LINE);
 	    		} else {
 		    		bufferTasks.append(tsk.getTitle());
 		    		bufferTasks.append(NEW_LINE);	    			
 	    		}
 			}
 	    	
 	    	if (bufferTasks.length() > 0)
 	    		bufferTasks.delete(bufferTasks.length() - NEW_LINE.length(), bufferTasks.length());
 
 	    	if (bufferCompletedTasks.length() > 0)
 	    		bufferCompletedTasks.delete(bufferCompletedTasks.length() - NEW_LINE.length(), bufferCompletedTasks.length());
 	    	
 	    	LogHelper.i("widget update successful");
 	    	views.setTextViewText(R.id.textViewList, list.getTitle());
 	    	views.setTextViewText(R.id.textViewTasks, bufferTasks);
 	    	views.setTextViewText(R.id.textViewCompletedTasks, bufferCompletedTasks);
     	}
     	catch(Exception ex)
     	{
     		LogHelper.w("failed to update tasks list");
     		String msg = ex.toString();
     		if (msg != null && msg.length() != 0) {
     			LogHelper.w(msg);
     		}
     		ex.printStackTrace();
     		return false;
     	}			
     	
     	return true;
     }	
 	
 	protected List<AccountWidgets> getGroupedAccounts(int[] widgetIds) {
 		List<AccountWidgets> result = new ArrayList<AccountWidgets>();
 		
 		for (int wId : widgetIds) {	
 			String accName = m_settings.loadWidgetAccount(wId);
 			if (accName == null) {
 				continue;
 			}
 			
 			AccountWidgets widgets = findAccountWidget(result, accName);
 			
 			if (widgets == null) {
 				widgets = new AccountWidgets();
 				widgets.accountName = accName;
 				widgets.widgetsIds = new ArrayList<Integer>();
 				result.add(widgets);
 			}
 			
 			widgets.widgetsIds.add(wId);
 		}		
 		
 		return result;
 	}
 	
 	protected AccountWidgets findAccountWidget(List<AccountWidgets> src, String accountName) {
 		for (AccountWidgets aw : src) {
 			if (aw.accountName.equals(accountName)) {
 				return aw;
 			}
 		}
 		return null;
 	}
     
 	protected Account getAccount(String accountName) {
 		Account[] accounts = AccountManager.get(m_context).getAccountsByType(ACCOUNT_TYPE);
 
 		if (accounts.length <= 0)
 			return null;
 
 		for (int i = 0; i < accounts.length; i++) {
 			if (accountName.equals(accounts[i].name)) {
 				return accounts[i];
 			}
 		}
 		
 		return null;
 	}
 }
