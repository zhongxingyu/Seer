 package com.suresh.whereismycash;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.github.johnpersano.supertoasts.SuperActivityToast;
 import com.github.johnpersano.supertoasts.SuperToast;
 import com.github.johnpersano.supertoasts.SuperToast.Duration;
 import com.github.johnpersano.supertoasts.SuperToast.Type;
 import com.suresh.whereismycash.DbHelper.PaymentType;
 import com.suresh.whereismycash.SwipeListener.DeleteRowListener;
 
 public class MainAdapter extends CursorAdapter implements OnClickListener, DeleteRowListener {
 	
 	private DbHelper dbHelper;
 	private Context context;
 	private ListView listView;
 	private OnClickListener parentActivity;
 
 	public MainAdapter(Context context, ListView listView, Cursor c, int flags, DbHelper dbHelper) {
 		super(context, c, flags);
 		this.context = context;
 		this.listView = listView;
 		parentActivity = (OnClickListener) context;
 		this.dbHelper = dbHelper;
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		view.findViewById(R.id.btDelete).setOnClickListener(this);
 		view.setOnClickListener(parentActivity);
 		float amount = cursor.getFloat(cursor.getColumnIndex(DbHelper.KEY_AMOUNT));
 		amount = (float) (Math.round(amount*100.0)/100.0);
 		String name = cursor.getString(cursor.getColumnIndex(DbHelper.KEY_NAME));
 		
 		TextView tvName = (TextView) view.findViewById(R.id.tvName);
 		tvName.setText(name);
 		
 		TextView tvAmount = (TextView) view.findViewById(R.id.tvAmount);
 		DbHelper.setTextandColor(context, tvAmount, amount);
 		
 		view.findViewById(R.id.btDelete).setTag(name);
 		//int id = cursor.getInt(cursor.getColumnIndex(DbHelper.KEY_ID));
 		view.setTag(name);
 		if (MiscUtil.phoneSupportsSwipe()) view.setOnTouchListener(new SwipeListener(listView, this.context, this));
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View v = inflater.inflate(R.layout.main_list_item, null);
 		bindView(v, context, cursor);
 		return v;
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btDelete:
 			displayDialog(v);
 			break;
 		}
 	}
 	
 	public void displayDialog(final View v) {
 		View parent = (View) v.getParent();
 		TextView tvName = (TextView) parent.findViewById(R.id.tvName);
 		AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 		builder.setMessage("Delete entry for " + tvName.getText() + "?");
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				deleteRow(v);
 			}
 		});
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {}
 		});
 		builder.show();
 	}
 	
 	public void updateParentTotal(View v) {
 		View grandParent = (View) v.getParent().getParent().getParent();
 		
 		float netSum = dbHelper.getNetSum();
 		TextView tvNetAmount = (TextView) grandParent.findViewById(R.id.tvNetAmount);
 		DbHelper.setTextandColor(grandParent.getContext(), tvNetAmount, netSum);
 	}
 
 	@Override public void deleteRow(View v) {
 		String name = (String)v.getTag();
 		ArrayList<HashMap<String, Object>> deletedEntries = dbHelper.delete(name);
 		swapCursor(dbHelper.getAllLoans());
 		updateParentTotal(v);
 		
 		SuperActivityToast toast = new SuperActivityToast(context, Type.BUTTON);
 		toast.setText("Delete all entries for " + name);
 		toast.setDuration(Duration.MEDIUM);
 		toast.setButtonText("UNDO");
 		toast.setButtonResource(SuperToast.Icon.Dark.UNDO);
         toast.setTextSize(SuperToast.TextSize.LARGE);
         toast.setButtonOnClickListener(new UndoAction(deletedEntries, name));
         
         toast.show();
 	}
 	
 	public class UndoAction implements OnClickListener {
 		
 		private ArrayList<HashMap<String, Object>> deletedEntries;
 		private String name;
 		
 		public UndoAction(ArrayList<HashMap<String, Object>> deletedEntries, String name) {
 			this.deletedEntries = deletedEntries;
 			this.name = name;
 		}
 
 		@Override public void onClick(View v) {
 			for (HashMap<String, Object> e : deletedEntries) {
 				float amount = (Float) e.get(DbHelper.KEY_AMOUNT);
 				PaymentType type = DbHelper.getPaymentType(amount);
 				String note = (String) e.get(DbHelper.KEY_NOTE);
 				long dateMillis = (Long) e.get(DbHelper.KEY_DATE);
 				
 				dbHelper.addEntry(type, amount, name, note, dateMillis);
 			}
 		}
 		
 	}
 
 }
