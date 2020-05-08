 package csci498.csmyth.lunchlist;
 
import android.R;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.RemoteViewsService;
 import android.widget.RemoteViews;
 
 public class ListViewsFactory implements RemoteViewsService.RemoteViewsFactory {
 	private Context ctxt = null;
 	private RestaurantHelper helper = null;
 	private Cursor restaurants = null;
 	
 	public ListViewsFactory(Context ctxt, Intent intent) {
 		this.ctxt = ctxt;
 	}
 	
	@Override
 	public void onCreate() {
 		helper = new RestaurantHelper(ctxt);
 		restaurants = helper
 				.getReadableDatabase()
 				.rawQuery("SELECT _ID, name FROM restaurants", null);
 	}
 	
	@Override
 	public void onDestroy() {
 		restaurants.close();
 		helper.close();
 	}
 	
	@Override
 	public int getCount() {
 		return restaurants.getCount();
 	}
 	
	@Override
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
 	
	@Override
 	public RemoteViews getLoadingView() {
 		return null;
 	}
 	
	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 	
	@Override
 	public long getItemId(int position) {
 		restaurants.moveToPosition(position);
 		return restaurants.getInt(0);
 	}
 	
	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 	
	@Override
 	public void onDataSetChanged() {
 		// no-op
 	}
 }
