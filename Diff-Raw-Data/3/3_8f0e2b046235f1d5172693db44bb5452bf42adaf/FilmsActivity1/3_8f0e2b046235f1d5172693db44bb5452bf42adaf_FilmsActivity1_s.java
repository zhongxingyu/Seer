 package edu.sjsu.cinequest;
 
 import java.util.List;
 import java.util.Vector;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import edu.sjsu.cinequest.comm.cinequestitem.CommonItem;
 import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
 
 /**
  * Films tab of the app, showing the films for a given date.
  * @author Prabhjeet Ghuman
  * @author Chao
  */
 public class FilmsActivity1 extends CinequestTabActivity {	
 	private String target;
 	private String tab;
 	//private static Vector<Filmlet> mFilms_byTitle;
 	private static Vector<CommonItem> mFilms_byTitle;
 	private static Vector<CommonItem> mSchedule_byDate;
 
 	//unique id's for menu options
 	// private static final int SORT_MENUOPTION_ID = Menu.FIRST;
 	private static final int ADD_CONTEXTMENU_ID = Menu.FIRST + 1;
 
 	private boolean isByDate() { return !target.equals(FilmsActivity.ALPHA); }
 
 	public void onCreate(Bundle savedInstanceState) {    	
 		target = getIntent().getExtras().getString("target");
 		tab = getIntent().getExtras().getString("tab");		
 		super.onCreate(savedInstanceState);
 	}
 
 	/**
 	 * Gets called when user returns to this tab. Also gets called once after the 
 	 * onCreate() method too.
 	 */
 
 	@Override
 	public void onResume(){
 		super.onResume();
 
 		//refresh the listview
 		if(target.equals(FilmsActivity.ALPHA)) {
 			refreshListContents(mFilms_byTitle);      		  		
 		} else {
 			refreshListContents(mSchedule_byDate);
 		}    	    		
 	}
 
 	@Override
 	protected void fetchServerData() {
 		if(isByDate()) {
 			if (tab.equalsIgnoreCase("films")){
 				//HomeActivity.getQueryManager().getSchedulesDay(target, new ProgressMonitorCallback(this) {
 				HomeActivity.getQueryManager().getFilmsByDate(target, new ProgressMonitorCallback(this) {
 					@Override
 					public void invoke(Object result) {
 						super.invoke(result);
 						mSchedule_byDate = (Vector<CommonItem>) result;
 						refreshListContents(mSchedule_byDate);
 					}
 				});
 			}
 			else if(tab.equalsIgnoreCase("events")){
 				HomeActivity.getQueryManager().getEventsByDate(target, new ProgressMonitorCallback(this) {
 					@Override
 					public void invoke(Object result) {
 						super.invoke(result);
 						mSchedule_byDate = (Vector<CommonItem>) result;
 						refreshListContents(mSchedule_byDate);
 					}
 				});
 			}
 			else if(tab.equalsIgnoreCase("forums"))
 			{
 				HomeActivity.getQueryManager().getForumsByDate(target, new ProgressMonitorCallback(this) {
 					@Override
 					public void invoke(Object result) {
 						super.invoke(result);
 						mSchedule_byDate = (Vector<CommonItem>) result;
 						refreshListContents(mSchedule_byDate);
 					}
 				});
 			}
 		}
 		else
 		{
 			if (tab.equalsIgnoreCase("films")){				
 				HomeActivity.getQueryManager().getAllFilms (new ProgressMonitorCallback(this) {           		 
 					public void invoke(Object result) {
 						super.invoke(result);
 						mFilms_byTitle = (Vector<CommonItem>) result;
 						refreshListContents(mFilms_byTitle);
 					}
 				});      
 			}
 			else if(tab.equalsIgnoreCase("events"))
 			{
 				HomeActivity.getQueryManager().getAllEvents (new ProgressMonitorCallback(this) {           		 
 					public void invoke(Object result) {
 						super.invoke(result);
 						mFilms_byTitle = (Vector<CommonItem>) result;
 						refreshListContents(mFilms_byTitle);
 					}
 				});
 			}
 			else if(tab.equalsIgnoreCase("forums"))
 			{
 				HomeActivity.getQueryManager().getAllForums (new ProgressMonitorCallback(this) {           		 
 					public void invoke(Object result) {
 						super.invoke(result);
 						mFilms_byTitle = (Vector<CommonItem>) result;
 						refreshListContents(mFilms_byTitle);
 					}
 				});
 			}
 		}     			
 	}
 
 	@Override
 	protected void refreshListContents(List<?> listItems) {
 		if (listItems == null) return;
 		if(isByDate()) {
			setListViewAdapter(createScheduleList((List<Schedule>) listItems));
 		}
 		else {
 			setListViewAdapter(createFilmletList((List<CommonItem>) listItems));
 		}
 	}
 
 	/*
 
 	private void toggleSortAndRedisplayList(){
     	if(mListSortType == SortType.BYDATE){
     		mListSortType = SortType.BYTITLE;
 
     		if(mFilms_byTitle == null)
     			fetchServerData();
     		else
     			refreshListContents(mFilms_byTitle);
 
     	} else if(mListSortType == SortType.BYTITLE){
     		mListSortType = SortType.BYDATE;
     		if(mSchedule_byDate == null)
     			fetchServerData();
     		else
     			refreshListContents(mSchedule_byDate);
     	} 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {        
 
         menu.add(0, SORT_MENUOPTION_ID, 0,"Sort by Title").setIcon(R.drawable.sort);
 
 
         //Home and About menu options will be added here
         super.onCreateOptionsMenu(menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
 
 	        case SORT_MENUOPTION_ID:
 	        	toggleSortAndRedisplayList();
 	            return true;
 
 	        default:
 	            return super.onOptionsItemSelected(item);
         }
 
     }
 	 */   	
 
 	/**
 	 * Called when creating the context menu (for our list items)
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		if(isByDate())
 			menu.add(0, ADD_CONTEXTMENU_ID, 0, "Add to Schedule");      
 	}
 
 	// TODO: Do we really want add in context menu???
 
 	/**
 	 * Called when an item in context menu is selected
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 
 
 		switch (item.getItemId()) {
 		case ADD_CONTEXTMENU_ID:
 			Object result = getListview().getItemAtPosition(info.position);
 			if(!isByDate())
 				return false;
 
 			//add this schedule to schedule 
 			HomeActivity.getUser().getSchedule().add( (Schedule)result);
 			return true;	      
 
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 }
