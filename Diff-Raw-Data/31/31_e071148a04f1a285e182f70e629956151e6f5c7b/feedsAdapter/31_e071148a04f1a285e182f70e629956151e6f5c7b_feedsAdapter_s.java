 package com.libcorp.shootmaniacenter.adapters;
 
 import android.app.Activity;
 import android.os.Handler;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.libcorp.shootmaniacenter.R;
 import com.libcorp.shootmaniacenter.structures.RSS.FeedMessage;
 import com.libcorp.shootmaniacenter.utilities.DynamicImageView;
 import com.libcorp.shootmaniacenter.utilities.HtmlFormatter;
 
 import java.util.ArrayList;
 
 
 /**
  * Created by artum on 20/05/13.
  *
  * Adapter per i news Feed
  */
 public class feedsAdapter extends BaseAdapter{
 
     private Activity activity;
     private ArrayList<FeedMessage> data;
     private static LayoutInflater inflater=null;
 
     public feedsAdapter(Activity a, ArrayList<FeedMessage> d) {
         activity = a;
         data=d;
         inflater = activity.getLayoutInflater();
     }
     @Override
     public int getCount() {
         return data.size();
     }
 
     @Override
     public Object getItem(int position) {
         return position;
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View vi=convertView;
 
         if(convertView==null)
         {
 
             inflater = activity.getLayoutInflater();
             vi = inflater.inflate(R.layout.news_element, null);
         }
 
         final Handler mHandler = new Handler();
         HtmlFormatter formatter = new HtmlFormatter();
 
        TextView title = (TextView)vi.findViewById(R.id.feed_title);                   // Title
        TextView description = (TextView)vi.findViewById(R.id.feed_description);       // Description
        TextView author = (TextView)vi.findViewById(R.id.feed_author);                 // Author
        TextView date = (TextView)vi.findViewById(R.id.feed_date);                     // Date
        DynamicImageView imageView = (DynamicImageView)vi.findViewById(R.id.feed_image);             // Image
 
 
         final FeedMessage feedMessage = data.get(position);
 
 
         /*
 
                 Display di tutti i campi del feedMessage (FeedMessage)
 
          */
 
         // Setting all values in listview
         title.setText(feedMessage.getTitle());
         description.setText(Html.fromHtml(feedMessage.getDescription()));
         author.setText(feedMessage.getAuthor());
         date.setText(feedMessage.getPubdate());
         imageView.setImageBitmap(feedMessage.getImage());
 
         return vi;
     }
 
 }
