 package com.suresh.whereismycash;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.suresh.whereismycash.DbHelper.PaymentType;
 import com.suresh.whereismycash.SwipeListener.DeleteRowListener;
 
 public class EditAdapter extends BaseAdapter implements OnClickListener, DeleteRowListener {
 	
 	public static final int TYPE_ITEM = 0;
     public static final int TYPE_SEPARATOR = 1;
     private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
 	
 	private ArrayList<HashMap<String, Object>> items;
 	private Context context;
 	private ListView listView;
 	private LayoutInflater inflater;
 	private String name;
 	private DbHelper dbHelper;
 	
 	public EditAdapter(Context context, ListView listView, ArrayList<HashMap<String, Object>> loans, String name, DbHelper dbHelper) {
 		this.context = context;
 		this.listView = listView;
 		this.items = loans;
 		this.name = name;
 		this.dbHelper = dbHelper;
 		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
 	public int getItemViewType(int position) {
 		return (Integer) items.get(position).get("viewType");
 	}
 	
 	@Override
 	public int getViewTypeCount() {
 		return TYPE_MAX_COUNT;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		HashMap<String, Object> map = items.get(position);
 		
 		switch ((Integer) map.get("viewType")) {
 		case TYPE_ITEM:
 			convertView = generateItemView(map, convertView);
 			break;
 		case TYPE_SEPARATOR:
 			convertView = generateSeparatorView(map, convertView);
 			break;
 		}
 		
 		return convertView;
 	}
 	
 	private View generateSeparatorView(HashMap<String, Object> map, View view) {
 		if (view == null) view = inflater.inflate(R.layout.edit_list_section_header, null);
 		String header = (String) map.get("header");
 		TextView tvSectionName = (TextView) view.findViewById(R.id.tvSectionName);
 		tvSectionName.setText(header);
 		return view;
 	}
 	
 	private View generateItemView(HashMap<String, Object> map, View view) {
 		if (view == null) view = inflater.inflate(R.layout.edit_list_item, null); 
 		float amount = (Float) map.get(DbHelper.KEY_AMOUNT);
 		amount = (float) (Math.round(amount*100.0)/100.0);
 		TextView tvAmount = (TextView) view.findViewById(R.id.tvAmount);
 		DbHelper.setTextandColor(context, tvAmount, amount);
 		
 		String note = (String) map.get(DbHelper.KEY_NOTE);
 		TextView tvNote = (TextView) view.findViewById(R.id.tvNote);
 		if (note != null) {
 			tvNote.setVisibility(View.VISIBLE);
 			tvNote.setText(note);
 		} else {
 			tvNote.setVisibility(View.GONE);
 			tvNote.setText(null);
 		}
 		
 		//Setting the date
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(Long.parseLong((String) map.get(DbHelper.KEY_DATE)));
 
 		((TextView) view.findViewById(R.id.tvDate))
 			.setText(DateFormat.format(context.getString(R.string.date_view_format), cal.getTime()));
 		((TextView) view.findViewById(R.id.tvDate))
 			.setTag(cal.getTimeInMillis()); //Setting the actual time value in milliseconds
 		
 		//Setting tags and click listeners
 		int id = (Integer) map.get(DbHelper.KEY_ID);
 		view.setTag(id);
 		view.findViewById(R.id.btDelete).setTag(id);
 		view.findViewById(R.id.btEdit).setTag(id);
 		view.findViewById(R.id.btDelete).setOnClickListener(this);
 		view.findViewById(R.id.btEdit).setOnClickListener(this);
 		
 		if (MiscUtil.phoneSupportsSwipe()) view.setOnTouchListener(new SwipeListener(listView, context, this)); //Swipe to delete
 		
 		return view;
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btDelete:
 			displayDialog(v);
 			break;
 		case R.id.btEdit:
 			View parent = (View) v.getParent();
 			Intent i = new Intent(v.getContext(), CreateActivity.class);
 			i.putExtra("name", name);
 			int id = (Integer)v.getTag();
 			i.putExtra("id", id);
 			TextView tvAmount = (TextView) parent.findViewById(R.id.tvAmount);
 			i.putExtra("paymentType", (String)tvAmount.getTag());
 			i.putExtra("amount", (String)tvAmount.getText());
 			TextView tvNote = (TextView) parent.findViewById(R.id.tvNote);
 			i.putExtra("note", (String)tvNote.getText());
 			i.putExtra("date", (Long) parent.findViewById(R.id.tvDate).getTag());
 			v.getContext().startActivity(i);
 			break;
 		}
 	}
 	
 	public void displayDialog(final View v) {
 		View parent = (View) v.getParent();
 		TextView tvAmount = (TextView) parent.findViewById(R.id.tvAmount);
 		AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 		builder.setMessage("Delete entry for " + tvAmount.getText() + "?");
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
 		TextView tvTotal = (TextView) grandParent.findViewById(R.id.tvTotal);
 		float amount = dbHelper.getLoanAmountByName(name);
 		DbHelper.setTextandColor(v.getContext(), tvTotal, amount);
 	}
 
 	@Override
 	public void deleteRow(View v) {
 		int tag = (Integer)v.getTag();
 		dbHelper.delete(tag);
 		items = dbHelper.getLoansByNameForDisplay(name);
 		notifyDataSetChanged();
 		updateParentTotal(v);
 		
		TextView tvAmount = (TextView) v.findViewById(R.id.tvAmount);
		String amount = (String) tvAmount.getText();
		String paymentType = (String) tvAmount.getTag();
 		
 		String toastMsg = "Deleted entry to" + ((paymentType.equals(PaymentType.GET.toString())) ? " get " : " pay " ) + amount;
 		Toast.makeText(context, (CharSequence) toastMsg, Toast.LENGTH_SHORT).show();
 	}
 
 }
