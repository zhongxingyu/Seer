 package za.lolo.www;
 
 import java.util.List;
import java.util.Locale;
 
 import android.content.Context;
import android.text.Layout;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.AnalogClock;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class StationAdapter extends ArrayAdapter<Station>{
 
 	
 	
 
 		int resource;
 	    android.content.Context context;
 	    List<Station> data = null; 
 		public StationAdapter(Context context, int resource,List<Station> objects) {
 			super(context, resource, objects);
 			// TODO Auto-generated constructor stub
 			this.context = context;
 			this.resource = resource;
 			this.data = objects;
 
 		}
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return data.size();
 		}
 		@Override
 		public Station getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return data.get(arg0);
 		}
 		@Override
 		public long getItemId(int arg0) {
 			// TODO Auto-generated method stub
 			return arg0;
 		}
 		@Override
 	    public View getView(int position, View convertView, ViewGroup parent)
 	    {
 	        LinearLayout Stationview;
 	        Station al = getItem(position);
 	 
 	        //Inflate the view
 	        if(convertView==null)
 	        {
 	        	Stationview = new LinearLayout(getContext());
 	            String inflater = android.content.Context.LAYOUT_INFLATER_SERVICE;
 	            LayoutInflater vi;
 	            vi = (LayoutInflater)getContext().getSystemService(inflater);
 	            vi.inflate(resource, Stationview, true);
 	        }
 	        else
 	        {
 	        	Stationview = (LinearLayout) convertView;
 	        }
 
 	        TextView name =(TextView)Stationview.findViewById(R.id.name);
 	        TextView status =(TextView)Stationview.findViewById(R.id.status);
 	        TextView longitude =(TextView)Stationview.findViewById(R.id.longitude);
 	        TextView latitude = (TextView)Stationview.findViewById(R.id.latitude);
	        RelativeLayout ly = (RelativeLayout)Stationview.findViewById(R.id.mylayout);
 
 	        
 	        name.setText(al.name);
 	        status.setText(al.status);
 	        longitude.setText(al.location.lon);
 	        latitude.setText(al.location.lat);
 	        
	        if(al.status.equalsIgnoreCase("good"))
	        {
	        	//status.setBackgroundResource(R.color.green);
	        	ly.setBackgroundResource(R.color.green);
	        }
	        else
	        {
	        	if(al.status.equalsIgnoreCase("bad"))
	        		ly.setBackgroundResource(R.color.red);
	        	else
	        		ly.setBackgroundResource(R.color.orange);
	        }
 	        
 	        
 	        return Stationview;
 	    }
 
 
 	}
 
