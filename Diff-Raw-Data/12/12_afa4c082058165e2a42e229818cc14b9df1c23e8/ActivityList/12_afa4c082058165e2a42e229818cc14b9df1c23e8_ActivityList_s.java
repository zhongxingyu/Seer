 package com.incidences;
 
 import java.util.List;
 
 import com.google.android.gcm.demo.app.R;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ActivityList extends ListActivity {
 
 	public static final String INDEX_ELEMENT = "index_element";
 	private static List<Incidence> list;
 	private Context mContext;
 	private ProgressDialog pd;
 	private Integer listType = ActivityMain.PENDING;

 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.incidences);
 		mContext = this;
 		Intent intent = getIntent();
 		if (intent!=null) {
 			listType = intent.getIntExtra(ActivityMain.LIST_TYPE_ARG, ActivityMain.PENDING);
 		}
 		TextView textViewTitle = (TextView)findViewById(R.id.incicences_list_title);
 		String newTitle;
 		if (listType == ActivityMain.PENDING) {
 			newTitle = this.getString(R.string.title_pending);
 		} else if (listType == ActivityMain.ORPHAN){
 			newTitle = this.getString(R.string.title_orphans);
 		} else {
 			newTitle = this.getString(R.string.title_closed);
 		}
 		textViewTitle.setText(newTitle);
 		pd = ProgressDialog.show(this, "incidencias", "Cargando datos...", true, false);
		Thread myThread = new ThreadLoadList(myHandler, listType);
 		myThread.start();
 	}
 
 	// Declaracion de la clase Handler.
 	Handler myHandler = new Handler(new Callback() {
 		@Override
 		public boolean handleMessage(Message msg) {
 			pd.dismiss();
 			TextView textView = (TextView)findViewById(R.id.incicences_list_msg);
 			if (msg.what == 1) {
 				if (list!=null && list.size()>0) {
 					setListAdapter(new ComplexListAdapter(mContext));
 					((ViewGroup)textView.getParent()).removeView(textView);
 				} else {
 					String text = getString(R.string.list_empty_pending);
 					if (listType==ActivityMain.CLOSED){
 						text = getString(R.string.list_empty_closed);
 					} else if (listType==ActivityMain.ORPHAN) {
 						text = getString(R.string.list_empty_orphans);						
 					}					
 					textView.setText(text);
 				}
 			} else {
 				Toast.makeText(mContext, getString(R.string.error_cargando_datos), Toast.LENGTH_SHORT).show();
 			}
 			return true;
 		}
 	});
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent intent = new Intent (this, ActivityDetail.class);
 		intent.putExtra(INDEX_ELEMENT, position);
 		intent.putExtra(ActivityMain.LIST_TYPE_ARG, listType);
 		startActivity(intent);
 	}
 
 	public List<Incidence> getList() {
 		return list;
 	}
 
 	public void setList(List<Incidence> list) {
 		ActivityList.list = list;
 	}
 
 	public static class ComplexListAdapter extends BaseAdapter {
 
 		private Context mContext;
 
 		public ComplexListAdapter(Context context) {
 			mContext = context;
 		}
 
 		@Override
 		public int getCount() {
 			return list.size();
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup viewGroup) {
 			View view = null;
 			if (convertView == null) {
 				/* Make up a new view */
 				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = inflater.inflate(R.layout.list_item, null);
 			} else {
 				/* Use convertView if it is available */
 				view = convertView;
 			}
 			TextView textView = (TextView) view.findViewById(R.id.incidence_number);
 			textView.setText(list.get(position).getIdAux().toString());
 			textView = (TextView) view.findViewById(R.id.contact_name);
 			textView.setText(list.get(position).getContactName());
 			textView = (TextView) view.findViewById(R.id.contact_phone);
 			textView.setText(list.get(position).getIncidenceAddressNoGPS());
 			return view;
 		}
 	}
 	
 	public static List<Incidence> getIncidences(){
 		return ActivityList.list;
 	}
 	public static void setIncidences(List<Incidence> list){
 		ActivityList.list = list ;
 	}
 	
 }
