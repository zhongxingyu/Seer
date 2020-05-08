 /*
 *Chris Card
 *Nathan harvey
 *10/26/12
 *This class manages content displayed on the widget
 */
 package csci422.CandN.to_dolist;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.RemoteViews;
 import android.widget.RemoteViewsService.RemoteViewsFactory;
 
 public class ListViewsFactory implements RemoteViewsFactory {
 
 	private Context ctxt = null;
 	private ToDoHelper helper = null;
 	private	Cursor tasks = null;
 
 	public ListViewsFactory(Context ctxt, Intent intent)
 	{
 		this.ctxt = ctxt;
 	}
 
 	public int getCount() 
 	{
 		return tasks.getCount();
 	}
 
 	public long getItemId(int position) 
 	{
 		tasks.moveToPosition(position);
 
 		return tasks.getInt(0);
 	}
 
 	public RemoteViews getLoadingView() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@TargetApi(11)
 	public RemoteViews getViewAt(int position) 
 	{
 		RemoteViews row = new RemoteViews(ctxt.getPackageName(), R.layout.widget_row);
 		tasks.moveToPosition(position);
 		row.setTextViewText(android.R.id.text1, tasks.getString(1));
 		
		if(tasks.getInt(2) < 95)
 		{
 			row.setImageViewResource(R.id.checkImage, R.drawable.checkbox_on_background);
 		}
 		else
 		{
 			row.setImageViewResource(R.id.checkImage, R.drawable.checkbox_off_background);
 		}
 		
 		
 		Intent i = new Intent();
 		Bundle extras = new Bundle();
 
 		extras.putString(DetailForm.DETAIL_EXTRA, String.valueOf(tasks.getInt(0)));
 		i.putExtras(extras);
 		row.setOnClickFillInIntent(android.R.id.text1, i);
 		return row;
 	}
 
 	public int getViewTypeCount() 
 	{
 		return 1;
 	}
 
 	public boolean hasStableIds() 
 	{
 		return true;
 	}
 
 	public void onCreate() 
 	{
 		helper = new ToDoHelper(ctxt);
 		tasks = helper.getReadableDatabase().rawQuery("SELECT _ID, title, state FROM todos", null);
 	}
 
 	public void onDataSetChanged() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onDestroy() 
 	{
 		tasks.close();
 		helper.close();
 	}
 
 }
