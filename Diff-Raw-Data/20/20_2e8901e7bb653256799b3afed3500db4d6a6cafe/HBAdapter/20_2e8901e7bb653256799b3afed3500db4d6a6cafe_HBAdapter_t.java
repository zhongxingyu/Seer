 package us.happ.android.adapter;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import us.happ.android.R;
 import us.happ.android.model.Mood;
 import us.happ.android.utils.ContactsManager;
 import us.happ.android.utils.Media;
 import android.content.Context;
 import android.graphics.BitmapFactory;
 
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class HBAdapter extends ArrayAdapter<Mood> {
 	
 	private ArrayList<Mood> data;
 	private LayoutInflater inflater;
 	private ContactsManager mContactsManager;
 	private Context mContext;
 	
 	public HBAdapter(Context context, int resource, ContactsManager contactsManager) {
 		super(context, resource);
 		inflater = LayoutInflater.from(context);
 		data = new ArrayList<Mood>();
 		mContactsManager = contactsManager;
 		mContext = context;
 	}
 	
 	@Override
 	public Mood getItem(int position){
 		return data.get(position);
 	}
 	
 	@Override
 	public long getItemId (int position){
 		return position;
 	}
 	
 	@Override
 	public int getCount(){
 		return data.size();
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v;
 		ViewHolder holder;
 		if (convertView == null){
 			v = inflater.inflate(R.layout.list_item_board, parent, false);
 			holder = new ViewHolder();
 			holder.avatar = (ImageView) v.findViewById(R.id.board_avatar);
 			holder.name = (TextView) v.findViewById(R.id.board_name);
 			holder.message = (TextView) v.findViewById(R.id.board_message);
 			v.setTag(holder);
 		} else {
 			v = convertView;
 			holder = (ViewHolder) v.getTag();
 		}
 		
 		Mood m = data.get(position);
 		
 		// TODO
 		// lazy load
 		float decay = ((float) (m.getTimestamp().getTime() + m.getDuration()*1000 - new Date().getTime()))/(m.getDuration()*1000);
		
 		if (decay < 0) decay = 0;
 		holder.avatar.setImageBitmap(Media.getRoundedCornerBitmap(mContext, mContactsManager.getAvatar(m.getNumber()), decay));
 		
 		holder.name.setText(mContactsManager.getName(m.getNumber()));
 		holder.message.setText(m.getMessage());
 		
 		return v;
 		
 	}
 	
 	class ViewHolder {
 		ImageView avatar;
 		TextView name;
 		TextView message;
 	}
 	
 	public void updateData(ArrayList<Mood> moods){
 		data = moods;
 		notifyDataSetChanged();
 	}
 
 }
