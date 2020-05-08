 package ca.ryerson.scs.rus.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.messenger.objects.Message;
 import ca.ryerson.scs.rus.socialite.objects.User;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class MessageListAdapter extends ArrayAdapter<Message> {
 
 	private LayoutInflater inflater;
 	ArrayList<Message> messages = new ArrayList<Message>();
 	Context context;
 	
 	public MessageListAdapter(Context context, int textViewResourceId,
 			ArrayList<Message> message) {
 		super(context, textViewResourceId, message);
 		this.messages = messages;
 		this.inflater = LayoutInflater.from(context);
 		this.context = context;
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Message message = getItem(position);
		//View v = convertView;
	    //if (v == null) {
	      //      LayoutInflater vi = (LayoutInflater).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //    v = vi.inflate(R.layout.listview_msgs, null);
	    //}
 
 		// TODO: uncomment when layout is done
 		/*
 		if (convertView==null){
 			convertView =  inflater.inflate(R.layout.bulletin_row_top, null);
 		}
 		
 		TextView username = (TextView) convertView.findViewById(R.id.topTitle);
 		username.setText(user.getUsername());
 		
 		TextView description = (TextView) convertView.findViewById(R.id.topTitle);
 		description.setText(user.getDescription());
 		
 		TextView distance = (TextView) convertView.findViewById(R.id.topTitle);
 		distance.setText(user.getDistance());
 		*/
 		return convertView;
 	}
 
 }
