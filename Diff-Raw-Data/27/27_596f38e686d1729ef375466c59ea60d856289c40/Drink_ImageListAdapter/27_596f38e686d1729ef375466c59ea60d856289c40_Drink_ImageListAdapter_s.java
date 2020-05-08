 package com.wikispaces.es1011.gamemenu;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Drink_ImageListAdapter extends BaseAdapter {
 	private Context mC;
 	private Cursor cur;
	private String[] sFrom;
	private int[] iTo;
 	private int iLen;
 
 	public Drink_ImageListAdapter(Context cx, int len, Cursor c) {
 		mC = cx;
 		cur = c;
 		iLen = len;
 	}
 
 	@Override
 	public int getCount() {
 		return iLen;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// Ai nostri fini, è sufficiente.
 		return position + 1;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		LinearLayout vOut;
 
 		if (convertView == null) {
 			vOut = (LinearLayout) View.inflate(mC, R.layout.drink_listitem,
 					null);
 		} else {
 			vOut = (LinearLayout) convertView;
 		}
 
 		cur.moveToPosition(position);
 
 		((ImageView) vOut.findViewById(R.id.drink_listitem_img))
 				.setImageResource(mC.getResources().getIdentifier(
 						"com.wikispaces.es1011.gamemenu:drawable/drink_"
 								+ cur.getString(0), null, null));
 
 		((TextView) vOut.findViewById(R.id.drink_listitem_tx1)).setText(cur
 				.getString(1));
 		((TextView) vOut.findViewById(R.id.drink_listitem_tx2)).setText(cur
 				.getString(2));
 		return vOut;
 	}
 
 }
