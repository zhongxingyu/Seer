 package com.underdusken.kulturekalendar.ui.adapters;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.underdusken.kulturekalendar.R;
 import com.underdusken.kulturekalendar.data.EventItem;
 import com.underdusken.kulturekalendar.utils.SimpleTimeFormat;
 
 import java.util.List;
 
 import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
 
 public class AdapterEventsItem extends ArrayAdapter<EventItem> implements StickyListHeadersAdapter {
 
     private LayoutInflater layoutInflater = null;
 
     private Typeface roboto;
 
     public AdapterEventsItem(Context context, int textViewResourceId, List<EventItem> objects) {
         super(context, textViewResourceId, objects);
         this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         roboto = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-li.ttf");
     }
 
     @Override
     public View getHeaderView(int i, View view, ViewGroup viewGroup) {
         HeaderHolder holder;
         if (view == null) {
             holder = new HeaderHolder();
             view = layoutInflater.inflate(R.layout.list_header, null);
             holder.date = (TextView) view.findViewById(R.id.header_text);
             view.setTag(holder);
         } else {
             holder = (HeaderHolder) view.getTag();
         }
         SimpleTimeFormat dateNow = new SimpleTimeFormat(getItem(i).getDateStart());
         holder.date.setText(dateNow.getUserHeaderDate());
 
         return view;
     }
 
 
     @Override
     public View getView(int position, View view, ViewGroup parent) {
         ViewHolder viewHolder;
         if (view == null) {
             view = layoutInflater.inflate(R.layout.events_item, null);
             viewHolder = new ViewHolder();
             viewHolder.name = (TextView) view.findViewById(R.id.event_title);
             viewHolder.price = (TextView) view.findViewById(R.id.event_price);
             viewHolder.place = (TextView) view.findViewById(R.id.event_place);
             viewHolder.time = (TextView) view.findViewById(R.id.event_time);
             viewHolder.icon = (ImageView) view.findViewById(R.id.event_image);
             view.setTag(viewHolder);
         } else {
             viewHolder = (ViewHolder) view.getTag();
         }
         EventItem item = getItem(position);
 
         /**
          * Sets the icon.
          */
         int iconRes = 0;
         String category = item.getCategoryID();
         if (category.equals("SPORT"))
             iconRes = (R.drawable.category_sport);
         else if (category.equals("PERFORMANCES"))
             iconRes = (R.drawable.category_performances);
         else if (category.equals("MUSIC"))
             iconRes = (R.drawable.category_music);
         else if (category.equals("EXHIBITIONS"))
             iconRes = (R.drawable.category_exhibitions);
         else if (category.equals("NIGHTLIFE"))
             iconRes = (R.drawable.category_nightlife);
         else if (category.equals("PRESENTATIONS"))
             iconRes = (R.drawable.category_presentations);
         else if (category.equals("DEBATE"))
             iconRes = (R.drawable.category_debate);
         else if (category.equals("OTHER"))
             iconRes = (R.drawable.category_other);
         viewHolder.icon.setImageResource(iconRes);
 
         /**
          * Sets the rest of the fields.
          */
         viewHolder.name.setText(item.getTitle());
         viewHolder.place.setText(item.getPlaceName());
         SimpleTimeFormat time = new SimpleTimeFormat(item.getDateStart());
         viewHolder.time.setText(time.getUserTimeDate());
        String cost = item.getPrice() <= 0 ? "Free" : ((int) item.getPrice()) + " kr";
         viewHolder.price.setText(cost);
 
         return view;
     }
 
     @Override
     public long getHeaderId(int i) {
         return getItem(i).getDateStartDay().hashCode();
     }
 
     private static class HeaderHolder {
         TextView date;
     }
 
     private static class ViewHolder {
         TextView name;
         TextView time;
         TextView price;
         TextView place;
         ImageView icon;
     }
 }
