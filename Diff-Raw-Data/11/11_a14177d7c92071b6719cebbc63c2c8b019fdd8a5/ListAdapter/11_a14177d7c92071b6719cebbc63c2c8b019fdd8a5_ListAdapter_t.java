 package info.vanderkooy.ucheck;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class ListAdapter extends SimpleAdapter {
 
 	private Context context;
 	private List<HashMap<String, String>> data;
 	private int resource;
 	private String[] from;
 	private int[] to;
 	
 	public ListAdapter(Context context, List<HashMap<String, String>> data,
 			int resource, String[] from, int[] to) {
 		super(context, data, resource, from, to);
 		this.context = context;
 		this.data = data;
 		this.resource = resource;
 		this.from = from;
 		this.to = to;
 	}
 	
 	public View getView(int position, View convertView, ViewGroup viewGroup) {
 		HashMap<String, String> entry = data.get(position);
		//if (convertView == null) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = inflater.inflate(resource, null);
		//}
 		
 		TextView[] tvs = new TextView[to.length];
 		for(int i = 0; i < to.length; i++) {
 			tvs[i] = (TextView) convertView.findViewById(to[i]);
 			tvs[i].setText(entry.get(from[i]));
 			tvs[i].setTypeface(null, Typeface.NORMAL);
 			if(entry.get("gehaald") != null)
 				tvs[i].setTextColor(Color.RED);
             else
                 tvs[i].setTextColor(Color.BLACK);
 
         }
 
         tvs[1].setTypeface(null, Typeface.BOLD);
 		
 		if(position == 0 && tvs[0].getText().equals("Vak")) {
 			for(int i = 0; i < to.length; i++) {
 				tvs[i].setTypeface(null, Typeface.BOLD);
                 tvs[i].setTextColor(Color.BLACK);
             }
 		}
 
         if(position % 2 == 1) {
             convertView.setBackgroundResource(R.color.light_gray);
        } else {
             convertView.setBackgroundColor(Color.WHITE);
         }
 		
 		return convertView;		
 	}
 
 }
