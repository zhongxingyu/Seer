 
 package jp.co.fuller.christmas_cake;
 
 import org.taptwo.android.widget.TitleProvider;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 
 public class ImageAdapter extends BaseAdapter implements TitleProvider {
 
     private LayoutInflater mInflater;
 
     private static final String[] titles = {
            "version 0.1",
            "version 0.2",
            "version 0.3",
            "version 0.4",
            "version 0.5",
            "version 0.6",
            "version 0.7",
            "version 0.8",
            "version 0.9"
     };
     private static final int[] images = {
             R.drawable.image01,
             R.drawable.image02,
             R.drawable.image03,
             R.drawable.image04,
             R.drawable.image05,
             R.drawable.image06,
             R.drawable.image07,
             R.drawable.image08,
             R.drawable.image09
     };
 
     public ImageAdapter(Context context) {
         mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public int getCount() {
         return images.length;
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
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.flow_item, null);
         }
         int drawable = images[position];
         ((ImageView) convertView.findViewById(R.id.imageView)).setImageResource(drawable);
         return convertView;
     }
 
     @Override
     public String getTitle(int position) {
         return titles[position];
     }
 
 }
