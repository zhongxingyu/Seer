 package com.taiwan.imageload;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.kosbrother.lyric.R;
 import com.kosbrother.lyric.entity.Album;
 
 public class ListCollectAlbumAdapter extends BaseAdapter {
 
     private final Activity         activity;
     private final ArrayList<Album> data;
     private static LayoutInflater  inflater = null;
 
     public ListCollectAlbumAdapter(Activity a, ArrayList<Album> d) {
         activity = a;
         data = d;
         inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     public int getCount() {
         return data.size();
     }
 
     public Object getItem(int position) {
         return position;
     }
 
     public long getItemId(int position) {
         return position;
     }
 
     public View getView(final int position, View convertView, ViewGroup parent) {
         // View vi= new View();
         View vi = inflater.inflate(R.layout.item_album_list, null);
         TextView text_name = (TextView) vi.findViewById(R.id.text_album_name);
         TextView text_time = (TextView) vi.findViewById(R.id.text_album_time);
         TextView singer_name = (TextView) vi.findViewById(R.id.singer_name);
        singer_name.setText(data.get(position).getSingerName());
 
         text_name.setText(data.get(position).getName());
         try {
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy MMM");
             text_time.setText(formatter.format(data.get(position).getDate()));
         } catch (Exception e) {
 
         }
 
         return vi;
     }
 }
