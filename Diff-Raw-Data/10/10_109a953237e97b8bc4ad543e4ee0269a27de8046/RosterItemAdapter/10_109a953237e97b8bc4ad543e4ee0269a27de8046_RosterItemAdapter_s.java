 package directi.androidteam.training.chatclient.Roster;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import directi.androidteam.training.chatclient.R;
 
 import java.util.ArrayList;
 
 public class RosterItemAdapter extends ArrayAdapter<RosterItem> {
     private ArrayList<RosterItem> rosterItems;
     private Context context;
 
     public RosterItemAdapter(Context context, int textViewResourceID, ArrayList<RosterItem> items) {
         super(context, textViewResourceID, items);
         this.rosterItems = items;
         this.context = context;
     }
 
     public void setRosterItems(ArrayList<RosterItem> rosterItems){
         this.rosterItems = rosterItems;
     }
 
     @Override
     public int getCount() {
         return this.rosterItems.size();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View view = convertView;
         if(view == null) {
             LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             view = vi.inflate(R.layout.rosterlistitem,null);
         }
         RosterItem rosterItem = rosterItems.get(position);
         if (rosterItem != null) {
             TextView JIDTextView = (TextView)view.findViewById(R.id.roster_jid);
             JIDTextView.setText(rosterItem.getBareJID());
             TextView statusTextView = (TextView)view.findViewById(R.id.roster_status);
             statusTextView.setText(rosterItem.getStatus());
             ImageView avatarImageView = (ImageView)view.findViewById(R.id.roster_image);
             avatarImageView.setImageBitmap(rosterItem.getAvatar());
             ImageView availabilityImageView = (ImageView)view.findViewById(R.id.list_availability_image);
             this.setAvailabilityImage(availabilityImageView, rosterItem.getPresence());
         }
         return view;
     }
 
     public void setAvailabilityImage(ImageView imageView, String presenceAvailability) {
         if (presenceAvailability.equals("chat")) {
             imageView.setImageResource(R.drawable.green);
         } else if (presenceAvailability.equals("dnd")) {
             imageView.setImageResource(R.drawable.red);
         } else if (presenceAvailability.equals("away") || presenceAvailability.equals("xa")) {
             imageView.setImageResource(R.drawable.yellow);
         } else if (presenceAvailability.equals("unavailable")) {
             imageView.setImageResource(R.drawable.gray);
         }
     }
 }
