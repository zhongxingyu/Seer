 package be.drupalcamp.leuven;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Session list adapter.
  */
 public class SessionListAdapter extends BaseAdapter implements OnClickListener {
     private final Context context;
     private final List<Session> sessions;
     private LayoutInflater mInflater;
 
     private static final int NORMAL = 0;
     private static final int SPECIAL = 1;
 
     public SessionListAdapter(Context context, List<Session> sessions) {
         this.context = context;
         this.sessions = sessions;
         this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     public int getCount() {
         return sessions.size();
     }
 
     public Session getItem(int position) {
         return sessions.get(position);
     }
 
     public long getItemId(int position) {
         return position;
     }
 
     public void onClick(View view) {
     }
 
     @Override
     public int getViewTypeCount() {
         return 2;
     }
 
     @Override
     public int getItemViewType(int position) {
         return sessions.get(position).getSpecial() == 0 ? NORMAL : SPECIAL;
     }
 
     public static class ViewHolder {
         public int sessionId;
         public TextView title;
         public TextView speaker;
         public TextView time;
         public ImageButton favorite;
         public LinearLayout session_item;
     }
 
     public View getView(final int position, View convertView, ViewGroup parent) {
         ViewHolder holder;
         int type = getItemViewType(position);
         Session session = sessions.get(position);
 
         if (convertView == null) {
             holder = new ViewHolder();
             holder.sessionId = session.getId();
 
             switch (type) {
                 case NORMAL:
                     convertView = mInflater.inflate(R.layout.session_normal_item, null);
                     holder.title = (TextView) convertView.findViewById(R.id.session_title);
                     holder.speaker = (TextView) convertView.findViewById(R.id.session_speakers);
                     holder.time = (TextView) convertView.findViewById(R.id.session_time);
                     holder.favorite = (ImageButton) convertView.findViewById(R.id.session_favorite);
                     holder.session_item = (LinearLayout) convertView.findViewById(R.id.session_item);
                     break;
                 case SPECIAL:
                     convertView = mInflater.inflate(R.layout.session_special_item, null);
                     holder.title = (TextView) convertView.findViewById(R.id.session_title);
                     holder.time = (TextView) convertView.findViewById(R.id.session_time);
                     break;
             }
             convertView.setTag(holder);
         }
         else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         if (session != null) {
 
             // Title.
             holder.title.setText(session.getTitle());
 
             // Normal sessions get speakers, hour and favorite button.
             if (session.getSpecial() == 0) {
 
                 String speakers = "";
                 List<Speaker> speakerList = session.getSpeakers();
                 for (int i = 0; i < speakerList.size(); i++) {
                     Speaker speaker = speakerList.get(i);
                    if (i > 1) {
                        speakers += "\n";
                    }
                    speakers += speaker.getFirstName() + " " + speaker.getLastName();
                 }
                 holder.speaker.setText(speakers);
 
                 // Session time.
                 int from = session.getStartDate();
                 int to = session.getEndDate();
                 DateFormat sdf = new SimpleDateFormat("kk:mm");
                 Date startHour = new Date((long)from * 1000);
                 Date endHour = new Date((long)to * 1000);
                 holder.time.setText(sdf.format(startHour) + " - " + sdf.format(endHour));
 
                 // Favorite image.
                 if (session.getFavorite() == 0) {
                     holder.favorite.setImageResource(R.drawable.non_favorited_session);
                 }
                 else {
                     holder.favorite.setImageResource(R.drawable.favorited_session);
                 }
 
                 // Set touch listener.
                 convertView.setOnTouchListener(sessionTouch);
             }
             else {
                 // Special session has start time.
                 int from = session.getStartDate();
                 DateFormat sdf = new SimpleDateFormat("kk:mm");
                 Date startHour = new Date((long)from * 1000);
                 holder.time.setText(sdf.format(startHour));
             }
         }
 
         return convertView;
     }
 
     /**
      * onTouchListener for session.
      */
     View.OnTouchListener sessionTouch = new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent motionEvent) {
             ViewHolder holder = (ViewHolder)v.getTag();
             int action = motionEvent.getAction();
             switch (action) {
                 case MotionEvent.ACTION_DOWN:
                     holder.title.setTextColor(context.getResources().getColor(R.color.white));
                     holder.speaker.setTextColor(context.getResources().getColor(R.color.white));
                     holder.time.setTextColor(context.getResources().getColor(R.color.white));
                     holder.session_item.setBackgroundColor(context.getResources().getColor(R.color.session_blue));
                     break;
                 case MotionEvent.ACTION_CANCEL:
                 case MotionEvent.ACTION_UP:
                     holder.title.setTextColor(context.getResources().getColor(R.color.text_dark));
                     holder.speaker.setTextColor(context.getResources().getColor(R.color.dark_grey));
                     holder.time.setTextColor(context.getResources().getColor(R.color.dark_grey));
                     holder.session_item.setBackgroundColor(context.getResources().getColor(R.color.white));
                     if (action == MotionEvent.ACTION_UP) {
                         int sessionId = holder.sessionId;
                         Intent intent = new Intent(context, SessionDetail.class);
                         intent.putExtra("sessionId", sessionId);
                         context.startActivity(intent);
                     }
                     break;
             }
             return true;
         }
     };
 
 }
