 package net.swankwiki.ldshymns;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import net.swankwiki.ldshymns.R;
 
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.XmlResourceParser;
 import android.net.Uri;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Filter;
 import android.widget.ListView;
 
 public class LDSHymns extends Activity {
     HymnAdapter adapt;
     
    private final static String FILE_CONFIG_URL = "https://github.com/sharkey3/LDS-Hymns-Android/raw/master/LDSHymns/downloads/ldshymns.download.xml";
     //private final static String FILE_CONFIG_URL = "http://swankwiki.net/downloadtest2/attach/ldshymns.download.xml";
     //private final static String FILE_CONFIG_URL = "http://172.16.14.111:8005/downloadtest2/attach/ldshymns.download.xml";
     private final static String DOWNLOAD_VERSION="1.3";
     private final static String DATA_PATH = "/sdcard/data/LDSHymns";
     private final static String USER_AGENT = "Droid Data Downloader";
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (!DownloaderActivity.ensureDownloaded(this, getString(R.string.download_text), FILE_CONFIG_URL, DOWNLOAD_VERSION, DATA_PATH, USER_AGENT)) {
         	return;
         }
         
         setContentView(R.layout.main);
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
         ListView lv = (ListView)findViewById(R.id.list);
         try {
         	adapt = new HymnAdapter(this,
 					R.xml.hymnlist,
 					android.R.layout.simple_list_item_1);
 			lv.setAdapter(adapt);
 		} catch (Exception e1) {
 			ErrorDialog(e1.getMessage());
 		}
         
         // setup click handler
         lv.setClickable(true);
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
         		Intent intent = new Intent(Intent.ACTION_VIEW);
         		Uri uri = Uri.withAppendedPath( Uri.parse("file://"+DATA_PATH),
         				((HymnHash)(parentView.getItemAtPosition(position))).get("file") );
         		intent.setDataAndType(uri,"application/pdf");
         		try {
         			startActivity(intent);
         		} catch (ActivityNotFoundException e) {
         			// no pdf viewer installed?
         			ErrorDialog("A PDF viewer is required.\nI recommend Adobe's free Acrobat viewer.");
         		}				
 			};
 		});
         
         lv.setTextFilterEnabled(true);
 
         EditText box = (EditText)findViewById(R.id.editText1);
         TextWatcher watcher = new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				adapt.getFilter().filter(s.toString());
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 			}
         	
         };
         box.addTextChangedListener(watcher);
         
     }
     
     public void ErrorDialog (String msg) {
     	AlertDialog.Builder err = new AlertDialog.Builder(this);
     	// err.setIcon(R.drawable.???);
     	// err.setTitle("Error");
     	err.setMessage(msg);
     	// err.setButton??
     	err.show();
     }
 
     class HymnList extends ArrayList<HymnHash> {
 		private static final long serialVersionUID = -8892815016320096908L;
 
 		public HymnList() {}
 		public HymnList(int resource) throws Exception {
 			XmlResourceParser xml = getResources().getXml(resource);
 			int et = xml.getEventType();
 			while (et != XmlPullParser.END_DOCUMENT) {
 				if (et == XmlPullParser.START_TAG && xml.getName().equals("item")) {
 					add(new HymnHash(xml));
 				}
 				et = xml.next();
 			}
 		}    	
     }
     
     class HymnHash extends HashMap<String,String> {
 		private static final long serialVersionUID = 1L;
 
 		public HymnHash(XmlResourceParser xml) throws Exception {
     		int eventType = xml.getEventType();
     		while (eventType != XmlPullParser.END_TAG) {
     			if (eventType == XmlPullParser.START_TAG) {
     				for(int a=0 ; a<xml.getAttributeCount() ; a++ ) {
     					put(xml.getAttributeName(a),xml.getAttributeValue(a));
     				}
     			} else if (eventType == XmlPullParser.TEXT) {
     				put("text",xml.getText());
     			}
     			eventType = xml.next();
     		}
     		put("bynumber", get("number")+" - "+get("text"));
     	}
     	
     	public String toString() {
     		//return (String)get("text");
     		return (String)get("bynumber");
     	}
     }
     
     class HymnAdapter extends ArrayAdapter<HymnHash> {
     	Filter thefilter;
     	public HymnList allhymns;
     	ListView myList;
     	
 		public HymnAdapter(Context context, int resource, int textViewResourceId) throws Exception {
 			super(context, textViewResourceId);
 			myList = (ListView)findViewById(R.id.list);
 			allhymns = new HymnList(resource);
 			setNotifyOnChange(false);
 			clear();
 			for (int i=0 ; i<allhymns.size(); i++ ) {
 				add((HymnHash)allhymns.get(i));
 			}
 			notifyDataSetChanged();
 		}
     	
 		@Override
 		public Filter getFilter() {
 			if (thefilter == null) {
 				thefilter = new HymnFilter();
 			}
 			return thefilter;
 		}
 		
 		@Override
 		public void notifyDataSetChanged() {
 			super.notifyDataSetChanged();
 	        myList.setFastScrollEnabled( getCount()>=50 );
 		}
     }
     
     class HymnFilter extends Filter {
 
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint) {
 			HymnList filtered = new HymnList();
 			for (int i=0 ; i<adapt.allhymns.size(); i++ ) {
 				HymnHash hh = adapt.allhymns.get(i);
 				StringBuffer reb = new StringBuffer(constraint);
 				if (constraint.length()==1) {
 					// single character matches start of title, modulo articles
 					reb.insert(0, "(?is)^[\\d\\s-]*(a |an |the )?");
 				} else {
 					reb.insert(0, "(?is).*");
 					//replace ' ' with '\s+'
 				}
 				reb.append(".*");
 				String re = new String(reb);
 				if (hh.toString().matches(re)) {
 					filtered.add(hh);
 				}
 			}
 			
 			// nothing is too trivial for a poorly documented class
 			FilterResults results = new FilterResults();
 			results.values = filtered;
 			results.count = filtered.size();
 			return results;
 		}
 
 		@Override
 		protected void publishResults(CharSequence constraint, FilterResults results) {
 			adapt.setNotifyOnChange(false);
 			HymnList list = (HymnList)results.values;
 			adapt.clear();
 			for (int i=0 ; i<list.size(); i++ ) {
 				adapt.add((HymnHash)list.get(i));
 			}
 			adapt.notifyDataSetChanged();
 		}
     	
     }
 }
 
