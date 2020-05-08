 package com.scarredions.stab;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.TextView;
 
 public class STPersonListAdapter extends BaseAdapter implements DialogInterface.OnClickListener {
 
 	//private final String[] items = {"1", "2", "3"};
     private ArrayList<String> people;
 	private Context mContext;
 	private STMenuListAdapter mla;
 	private Button addButton;
 	
 	public STPersonListAdapter(Context c) {
 		mContext = c;
 		
 		people = new ArrayList<String>();
 		people.add("You");
 	}
 	
 	public int getCount() {
 	    return people.size();
 	}
 
 	public Object getItem(int position) {
 	    return people.get(position);
 	}
 
 	public long getItemId(int position) {
 	    return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 	    if (convertView == null) {
 	        TextView tv = new TextView(mContext);
            tv.setText(people.get(position));
            tv.setHeight(100);    
            tv.setWidth(100);
            tv.setGravity(android.view.Gravity.CENTER);
            return tv;
 	    } else {
 	        return (TextView) convertView;
 	    }
 	}
 	
 	public void setMenuListAdapter(STMenuListAdapter mla) {
 	    this.mla = mla;
 	}
 	
 	public STMenuListAdapter getMenuListAdapter() {
 	    return mla;
 	}
 	
 	// TODO: implement
 	public boolean currentPersonHasSelected(int listPosition) {
 	    return false;
 	}
 	
 	public void onClick(DialogInterface dialog, int whichButton) {
 	    if (whichButton == DialogInterface.BUTTON_POSITIVE) {
 	        AlertDialog d = (AlertDialog) dialog;
 	        people.add(((EditText) d.findViewById(R.id.username_edit)).getText().toString());
 	        this.notifyDataSetChanged();
 	    }
 	}
 	
 	public void addPersonByDialog() {
 	    LayoutInflater factory = LayoutInflater.from(mContext);
 	    final View textEntryView = factory.inflate(R.layout.dialog_person_entry, null);
 	    
 	    new AlertDialog.Builder(mContext)
 	        .setView(textEntryView)
 	        .setPositiveButton("OK", this)
 	        .setNegativeButton("Cancel", this)
 	        .create().show();
 	}
 
 }
