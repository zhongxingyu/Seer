 package com.josephblough.fluchallenge.activities;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import com.josephblough.fluchallenge.data.FeedEntry;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class FeedListActivity extends ListActivity implements OnItemSelectedListener, OnItemClickListener {
 
     private final String TAG = "FeedListActivity";
     
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 
 	getListView().setOnItemSelectedListener(this);
 	getListView().setOnItemClickListener(this);
     }
 
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 	FeedEntry entry = ((RssFeedEntryAdapter)getListAdapter()).getItem(position);
 	visitLink(entry);
     }
     
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 	FeedEntry entry = ((RssFeedEntryAdapter)getListAdapter()).getItem(position);
 	visitLink(entry);
     }
     
     public void onNothingSelected(AdapterView<?> parent) {
 	// TODO Auto-generated method stub
     }
 
     private void visitLink(final FeedEntry entry) {
 	Log.d(TAG, "Selected: " + entry.link);
 	final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(entry.link));
 	startActivity(intent);
     }
 
 
     protected class RssFeedEntryAdapter extends ArrayAdapter<FeedEntry> {
	SimpleDateFormat formatter = new SimpleDateFormat();
 	RssFeedEntryAdapter(List<FeedEntry> entries) {
 	    super(FeedListActivity.this, android.R.layout.simple_list_item_2, entries);
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 	    View row = convertView;
 
 	    if (row == null) {
 		LayoutInflater inflater = getLayoutInflater();
 		row = inflater.inflate(android.R.layout.simple_list_item_2, null);
 	    }
 	    FeedEntry entry = super.getItem(position);
 	    ((TextView)row.findViewById(android.R.id.text1)).setText(entry.title);
 	    synchronized (this) {
 		try {
		    Date date = formatter.parse(entry.date);
		    ((TextView) row.findViewById(android.R.id.text2)).setText(formatter.format(date));
 		}
		catch (Exception e) {
 		    ((TextView) row.findViewById(android.R.id.text2)).setText(entry.date);
 		}
 	    }
 	    return row;
 	}
     }
 }
