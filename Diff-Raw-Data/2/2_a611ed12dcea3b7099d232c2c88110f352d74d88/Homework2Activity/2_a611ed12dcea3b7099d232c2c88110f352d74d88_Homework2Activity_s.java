 package edu.washington.cleveb2;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OptionalDataException;
 import java.io.StreamCorruptedException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.view.LayoutInflater;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import edu.washington.cleveb2.R;
 
 public class Homework2Activity extends ListActivity {
 
 	private ListView listview;
 	private Button add_text_button;
 	private ratingListAdapter list_adapter;
 	private ArrayList<HashMap<String, Object>> list_array = new ArrayList<HashMap<String, Object>>();
 
 	// handler for adding text
 	private View.OnClickListener addTextListener = new View.OnClickListener() {
 
 		public void onClick(View v) {
 			// get text from the text field
 			EditText text = (EditText) findViewById(R.id.edit_text);
 			
 			// add the text to the list
 			// the initial rating should be 0
 			add_to_list(text.getText().toString());
 
 			// notify the listview that the list has been changed
 			list_adapter.notifyDataSetChanged();
 
 			// clear the text box
 			text.setText("");
 
 			// hide the keyboard now that the user is done
 			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
 		
 		// get the list view
 		//listview = (ListView) findViewById(R.id.list);
 		listview = getListView();
 		
 		// create the list adapter
 		list_adapter = new ratingListAdapter( list_array, this);
 		
 		// set the list view adapter
 		//listview.setAdapter(list_adapter);
 		this.setListAdapter( list_adapter );
 		
 		// setup the add text button
 		add_text_button = (Button) findViewById(R.id.add_text_button);
 		add_text_button.setOnClickListener(addTextListener);
 	}
 	
 	// update the rating when the user clicks on the list item
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		HashMap<String,Object> hm = list_array.get(position);
 		
 		hm.put("rating", (float)3.5);
 		
 		list_adapter.notifyDataSetChanged();
 	}
 	
 	// override to handle saving things that the user added
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		
 		// save the state of all the strings and ratings
 		SharedPreferences.Editor editor = getPreferences( MODE_PRIVATE ).edit();
 		
 		// save the count of the array
 		editor.putInt("count", list_array.size());
 		
 		// finally commit the count
 		editor.commit();
 		
 		// this is kindof of lame but is should work
 				
 		FileOutputStream fos = null;
 		try {
 			fos = openFileOutput( "saved_ratings", MODE_PRIVATE );
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		ObjectOutputStream oos = null;
 		try {
 			oos = new ObjectOutputStream( fos );
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// save all the items in the list array
 		for(int i = 0; i < list_array.size(); ++i)
 		{
 			try {
 				oos.writeObject( list_array.get(i) );
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 		}
 		
 		try {
 			oos.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			fos.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	// override to handle saving things that the user added
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		
 		SharedPreferences prefs = getPreferences( MODE_PRIVATE ); 
 		int count = prefs.getInt("count", 0);
 		
 		// we don't have anything saved - get the default from the strings.xml
 		if( count == 0 )
 		{
 			// get the string array
 			for( String str : getResources().getStringArray( R.array.text_string_list ) ) 
 			{
 				// initialize the array with values
 				add_to_list( str );
 			}
 		}
 		else
 		{
 		
 
 			FileInputStream fis = null;
 			try {
 				fis = openFileInput("saved_ratings");
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			ObjectInputStream ois = null;
 			try {
 				ois = new ObjectInputStream( fis );
 			} catch (StreamCorruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				// get all the items in the list array
 				for(int i = 0; i < count; ++i )
 				{
 					// add the item to the list array
 					list_array.add((HashMap<String, Object>) ois.readObject());
 				}
 			} catch (OptionalDataException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		// notify the listview that the list has been changed
 		list_adapter.notifyDataSetChanged();
 	}
 
 	// adds a string to the list view
 	private void add_to_list(String str) {
 		// make sure there is text before trying to add it (we don't want to add
 		// blank lines!)
 		if (str.length() > 0) {
 			
 			// create a hash map for the object
 			HashMap<String, Object> hm;
 			hm = new HashMap<String, Object>();
 			hm.put("text", str);
 			hm.put("rating", (float) 0);
 			
 			// add this to the array
 			list_array.add(hm);	
 		}
 	}
 
 	private class ratingListAdapter extends BaseAdapter{
 		private ArrayList<HashMap<String, Object>> List;
 
 		private LayoutInflater mInflater;
 
 		
 		public ratingListAdapter(ArrayList<HashMap<String, Object>> list, Context context) {
 			List = list;
 			mInflater = LayoutInflater.from(context);
 		}
 
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			ViewHolder holder;
 
 			// When convertView is not null, we can reuse it directly, there is
 			// no need
 			// to reinflate it. We only inflate a new View when the convertView
 			// supplied
 			// by ListView is null.
 			if (convertView == null) {
 				convertView = mInflater.inflate(R.layout.item_rating, null);
 
 				// Creates a ViewHolder and store references to the two children
 				// views
 				// we want to bind data to.
 				holder = new ViewHolder();
 				holder.text = (TextView) convertView.findViewById(R.id.textView1);
 				holder.rating = (RatingBar) convertView.findViewById(R.id.ratingBar1);
 				
 				convertView.setTag(holder);
 			} else {
 				// Get the ViewHolder back to get fast access to the TextView
 				// and the ImageView.
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			// Bind the data efficiently with the holder.
 			holder.text.setText((String) List.get(position).get("text"));
 			holder.rating.setRating( (Float) List.get(position).get("rating"));
 
 			return convertView;
 
 		}
 
 		class ViewHolder {
 			TextView text;
 			RatingBar rating;
 		}
 
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return List.size();
 		}
 
 		public Object getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return List.get(arg0);
 		}
 
 		public long getItemId(int arg0) {
 			// TODO Auto-generated method stub
 			return arg0;
 		}
 	}
 
 }
