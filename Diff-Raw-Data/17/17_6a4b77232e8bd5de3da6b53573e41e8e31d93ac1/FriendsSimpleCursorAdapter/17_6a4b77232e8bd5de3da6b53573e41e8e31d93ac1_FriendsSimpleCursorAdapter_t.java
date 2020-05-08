 package me.rohitmishra.groupbanker;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckedTextView;
import android.widget.Filterable;
 import android.widget.SimpleCursorAdapter;
 
 /* Using the tutorial at 
  * http://www.vogella.de/articles/AndroidListView/article.html
  * for creating a custom SimpleCursorAdapter for SelectFriends.java
  */
public class FriendsSimpleCursorAdapter extends SimpleCursorAdapter implements Filterable {
 
 	private static final String TAG = "FriendsSimpleCursorAdapter";
 	private final Context context ;
 	private final String[] values ;
 	private final int layout ;
	private final Cursor cursor ;
 	
 	static class ViewHolder	{
 		public CheckedTextView checkedText ;
 	}
 	
 	public FriendsSimpleCursorAdapter(Context context, int layout, Cursor c,
 			String[] from, int[] to) {
 		super(context, layout, c, from, to);
 		this.context = context ;
 		this.values = from ;
 		this.layout = layout ;
		this.cursor = c ;
 		Log.d(TAG, "At the end of the constructor") ;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)	{
 		Log.d(TAG, "At the start of rowView. position = " + position) ;
 		View rowView = convertView ;
 		if(rowView == null)	{
 			Log.d(TAG, "rowView = null");
 			try {
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(layout, parent, false);
 			Log.d(TAG, "rowView inflated. rowView = " + rowView);
 			ViewHolder viewHolder = new ViewHolder() ;
 			viewHolder.checkedText = (CheckedTextView) rowView.findViewById(R.id.text1) ;
 			rowView.setTag(viewHolder);
 			}
 			catch (Exception e)	{
 				Log.e(TAG, "exception = " + e);
 			}
 		}
 		
 		ViewHolder holder = (ViewHolder) rowView.getTag();
 		
		int nameCol = cursor.getColumnIndex(FriendsDbAdapter.KEY_NAME) ;
		String name = cursor.getString(nameCol);
		holder.checkedText.setText(name);
 		
 		Log.d(TAG, "At the end of rowView");
 		return rowView;
 		
 	}
 
 }
