 package csci498.csmyth.lunchlist;
 
import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
import android.os.Build;
 import android.os.Bundle;
 import android.widget.RemoteViewsService;
 import android.widget.RemoteViews;
 
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class ListViewsFactory implements RemoteViewsService.RemoteViewsFactory {
 	private Context ctxt = null;
 	private RestaurantHelper helper = null;
 	private Cursor restaurants = null;
 	
 	public ListViewsFactory(Context ctxt, Intent intent) {
 		this.ctxt = ctxt;
 	}
 	
 	public void onCreate() {
 		helper = new RestaurantHelper(ctxt);
 		restaurants = helper
 				.getReadableDatabase()
 				.rawQuery("SELECT _ID, name FROM restaurants", null);
 	}
 	
 	public void onDestroy() {
 		restaurants.close();
 		helper.close();
 	}
 	
 	public int getCount() {
 		return restaurants.getCount();
 	}
 	
 	public RemoteViews getViewAt(int position) {
 		RemoteViews row = new RemoteViews(ctxt.getPackageName(), R.layout.widget_row);
 		Intent intent = new Intent();
 		Bundle extras = new Bundle();
 		
 		restaurants.moveToPosition(position);
 		row.setTextViewText(android.R.id.text1, restaurants.getString(1));
 		extras.putString(LunchList.ID_EXTRA, String.valueOf(restaurants.getInt(0)));
 		intent.putExtras(extras);
 		row.setOnClickFillInIntent(android.R.id.text1, intent);
 		return row;
 	}
 	
 	public RemoteViews getLoadingView() {
 		return null;
 	}
 	
 	public int getViewTypeCount() {
 		return 1;
 	}
 	
 	public long getItemId(int position) {
 		restaurants.moveToPosition(position);
 		return restaurants.getInt(0);
 	}
 	
 	public boolean hasStableIds() {
 		return true;
 	}
 	
 	public void onDataSetChanged() {
 		// no-op
 	}
 }
