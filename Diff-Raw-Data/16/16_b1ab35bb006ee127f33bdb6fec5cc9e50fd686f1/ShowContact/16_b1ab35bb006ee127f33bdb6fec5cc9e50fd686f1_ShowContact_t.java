 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parse.GetCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ShowContact extends Activity {
 
 	String contactId;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.show_contact);
 		Parse.initialize(this, "FWyFNrvpkliSb7nBNugCNttN5HWpcbfaOWEutejH", "SZoWtHw28U44nJy8uKtV2oAQ8suuCZnFLklFSk46");
 
 		Bundle extras = this.getIntent().getExtras();
 
 		if (extras == null){
 			Toast.makeText(this, "Could not load contact.", Toast.LENGTH_LONG);
 			return;
 		} else {
 			final ProgressDialog dialog = ProgressDialog.show(this, "", 
 					"Loading. Please wait...", true);
 			dialog.setCancelable(true);
 			contactId = extras.getString("contactID");
 			ParseQuery query = new ParseQuery("_User");
			query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
 
 			query.getInBackground(contactId, new GetCallback() {
 
 				@SuppressWarnings({ "rawtypes", "unchecked" })
 				/* Unchecked types required for dynamic list dividers */
 				@Override
 				public void done(ParseObject contact, ParseException e) {
 					if (e == null) {
 						TextView temp = (TextView) findViewById(R.id.contactHeader);
 						String tempString = contact.getString("fullName");
 						temp.setText(tempString);
 
 						ListView listView = (ListView) findViewById(R.id.phoneList);
 						ArrayList items = new ArrayList();
 						items.add(new ListItem("Phone", true));
 
 						String info = contact.getString("phone1");
 						String toSave = "".equals(info) ? "None" : info;
 						items.add(new ListItem("Primary: " + toSave, false));
 						info = contact.getString("phone2");
 						toSave = "".equals(info) ? "None" : info;
 						items.add(new ListItem("Secondary: " + toSave, false));
 
 						items.add(new ListItem("Email", true));
 						info = contact.getString("email1");
 						toSave = "".equals(info) ? "None" : info;
 						items.add(new ListItem("Primary: " + toSave, false));
 						info = contact.getString("email2");
 						toSave = "".equals(info) ? "None" : info;
 						items.add(new ListItem("Secondary: " + toSave, false));
 						listView.setAdapter(new ContactAdapter(getApplicationContext(), items));
 
 					} else {
 						Toast.makeText(getApplicationContext(), "Could not load contact.", Toast.LENGTH_LONG);
 					}
 					dialog.cancel();
 				}
 
 			});
 		}
 	}
 
 	private class ContactAdapter extends ArrayAdapter<ListItem> {
 
 		private List<ListItem> contacts;
 
 		public ContactAdapter(Context context, List<ListItem> contacts) {
 			super(context, 0, contacts);
 			this.contacts = contacts;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			final ListItem item = contacts.get(position);
 
 			if (item != null) {
 				if (item.isSection()) {
 					/* This is a section header */
 					String title = (String) item.getData();
 					v = vi.inflate(R.layout.list_divider, null);
 
 					v.setOnClickListener(null);
 					v.setOnLongClickListener(null);
 					v.setLongClickable(false);
 
 					final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
 					sectionView.setText(title);
 
 				} else {
 					/* This is a real list item */
 					final String contactData = (String) item.getData();
 					v = vi.inflate(R.layout.list_item, null);
 					TextView temp = (TextView) v;
 					temp.setText(contactData);
 					Linkify.addLinks(temp, Linkify.ALL);
 					temp.setLinkTextColor(temp.getTextColors().getDefaultColor());
 				}
 			}
 			return v;
 		}
 	}
 
 }
