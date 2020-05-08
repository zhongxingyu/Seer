 package com.joessy.medcatalog.adapter;
 
 import java.util.List;
 
 import com.joessy.medcatalog.activity.R;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ListViewAdapter extends BaseAdapter {
 	private List<String> items;
 	private LayoutInflater inflater;
 	private static Context actContext;
 
 	public ListViewAdapter(Context context, List<String> items) {
 		this.items = items;
 		ListViewAdapter.actContext = context;
 		inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public int getCount() {
 
 		return items.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return items.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View view, ViewGroup parent) {
 		if (view == null) {
 			view = inflater.inflate(R.layout.list_item, null);
 		}
 		if (items.size() != 0) {
 			TextView text = (TextView) view.findViewById(R.id.list_item_text);
 			Button bt = (Button) view.findViewById(R.id.view_btn);
 			bt.setTag(items.get(position));
 			text.setText(items.get(position));
 			bt.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
					Log.d("BUTTONTAG",(String)v.getTag());
 					showInfo((String) v.getTag());
 
 				}
 			});
 		}
 		return view;
 	}
 
 	/**
 	 * б
 	 * 
 	 * @param item
 	 */
 	public void addItem(String item) {
 		items.add(item);
 	}
 
 	/**
 	 * б
 	 * 
 	 * @param item
 	 */
 	public void removeItems() {
 		this.items.clear();
 	}
 
 	/**
 	 * listviewеԻ
 	 */
 	public void showInfo(String str) {
 		StringBuilder tmp = new StringBuilder();
		tmp.append("select * from \"201209\" where ҩƷ = \"").append(str)
 				.append("\"");
 		Log.d("SQL", tmp.toString());
 		Cursor cursor = SQLiteDBAdapter.getCursor(tmp.toString());
 		tmp.delete(0, tmp.length());
        
 		cursor.moveToFirst();
		String name=cursor.getString(0);
		tmp.append(cursor.getString(1)).append(" \n")
				.append(cursor.getString(2)).append(" \n")
				.append(cursor.getString(3)).append(" \n")
				.append(cursor.getString(4)).append(" \n")
				.append(cursor.getString(5));
		
 		cursor.close();// رս
 
 		new AlertDialog.Builder(ListViewAdapter.actContext).setTitle(name)
 				.setMessage(tmp.toString())
 				.setPositiveButton("ȷ", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				}).show();
 		tmp.delete(0, tmp.length());
 
 	}
 
 }
