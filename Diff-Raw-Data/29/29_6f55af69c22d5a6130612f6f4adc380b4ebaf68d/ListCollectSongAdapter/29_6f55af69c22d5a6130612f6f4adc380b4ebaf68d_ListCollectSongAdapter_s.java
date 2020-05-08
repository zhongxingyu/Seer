 package com.taiwan.imageload;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.kosbrother.lyric.R;
 import com.kosbrother.lyric.entity.Song;
 
 public class ListCollectSongAdapter extends BaseAdapter {
 
     private final Activity        activity;
     private final ArrayList<Song> data;
     private static LayoutInflater inflater = null;
 
     public ListCollectSongAdapter(Activity a, ArrayList<Song> d) {
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
 
         View vi = inflater.inflate(R.layout.item_song_list, null);
         TextView text_name = (TextView) vi.findViewById(R.id.text_song_name);
         TextView singer_name = (TextView) vi.findViewById(R.id.singer_name);
        singer_name.setText(data.get(position).getSingerName());
         text_name.setText(data.get(position).getName());
 
         return vi;
     }
 }
