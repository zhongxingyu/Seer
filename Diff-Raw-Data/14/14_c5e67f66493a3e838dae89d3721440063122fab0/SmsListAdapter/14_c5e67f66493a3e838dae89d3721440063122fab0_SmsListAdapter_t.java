 package studio.reno.SmsFilter;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 	class SmsListAdapter extends BaseAdapter {
 		LayoutInflater inflater;
 		List<HashMap<String,String>> data = new LinkedList<HashMap<String,String>>();
 		
 		public SmsListAdapter(Context context){
 			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
 		}
 		
 		public void update(final List<HashMap<String,String>> list){
 			data = list;
 			notifyDataSetChanged();
 		}
 		
     	@Override
 		public View getView(int position, View convertView, ViewGroup parent){
     		Log.d("T","getView "+position);
            if (convertView == null) convertView = inflater.inflate(R.layout.trashitem, null);
            HashMap<String,String> msg=data.get(position);
            ((TextView)convertView.findViewById(R.id.smsfrom)).setText(msg.get("from"));
            ((TextView)convertView.findViewById(R.id.smscontent)).setText(msg.get("content"));
            ((TextView)convertView.findViewById(R.id.smstime)).setText(formatTime(Long.parseLong(msg.get("time"))));

             return convertView;
     	}
 
 		@Override
 		public int getCount() {
 			Log.d("T","getCount="+data.size());
 			return data.size();
 		}
 
 		@Override
 		public Object getItem(int pos) {
 			return data.get(pos);
 		}
 
 		@Override
 		public long getItemId(int pos) {
 			return pos;
 		}
 		
 		private static CharSequence formatTime(long ms) {
 			Time time = new Time();
 			time.set(ms);
 			if (System.currentTimeMillis()-ms<12*3600000)
 				return time.format("%H:%M");
 			else
 				return time.format("%m-%d");
 		};
     }
