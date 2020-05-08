 package api.festival;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class FestivalAndroidActivity extends Activity {
 	/** Called when the activity is first created. */
 	static final String[] EVENTS = new String[] { "LeFag Event" };
 	static final int size = 10;
 	private ListView list;
 	private Button ok;
	private EditText entry;
 	private API api;
 	private ProgressDialog dialog;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		api = new API("3FPE9X151AMKIqrv", "V6KLOjmYaz8r_cYWTKIfVPfkHIiIj7Ha");
 
 		list = (ListView) findViewById(R.id.list);
 		list.setAdapter(new ArrayAdapter<String>(this, R.layout.list, EVENTS));
 
 		ok = (Button) findViewById(R.id.ok);
		entry = (EditText) findViewById(R.id.entry);
 		ok.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View arg0) {
				if (entry.getText().length() != 0) {
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(entry.getWindowToken(), 0);
					dialog = ProgressDialog.show(FestivalAndroidActivity.this,
							"", "Loading. Please wait...", true);
					new EventThread(list).start();
				}
 			}
 
 		});
 	}
 
 	private class EventThread extends Thread {
 
 		private ListView listView;
 		private ArrayAdapter<String> adapter;
 
 		public EventThread(ListView listView) {
 			this.listView = listView;
 		}
 
 		public void run() {
 			String entryText = entry.getText().toString();
 
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("festival", entryText);
 			map.put("size", Integer.toString(size));
 			int from = 0;
 			map.put("from", Integer.toString(from));
 			Event[] events = api.getEvents(map);
 
 			ArrayList<String> names = new ArrayList<String>();

 			while (events.length != 0) {
 				for (Event event : events) {
 					names.add(event.getTitle());
 				}
 				from += size;
 				map.put("from", Integer.toString(from));
 				events = api.getEvents(map);
 			}

			adapter = new ArrayAdapter<String>(list.getContext(),
					R.layout.list, names);
 			handler.sendEmptyMessage(0);
 		}
 
 		private Handler handler = new Handler() {

 			public void handleMessage(Message msg) {
 				listView.setAdapter(adapter);
 				dialog.dismiss();
 			}
 		};
 	}
 }
