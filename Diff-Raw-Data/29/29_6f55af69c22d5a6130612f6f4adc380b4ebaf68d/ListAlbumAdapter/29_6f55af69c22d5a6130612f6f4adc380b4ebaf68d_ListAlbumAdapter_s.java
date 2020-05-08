 package com.taiwan.imageload;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.kosbrother.lyric.AlbumActivity;
 import com.kosbrother.lyric.R;
 import com.kosbrother.lyric.entity.Album;
 
 public class ListAlbumAdapter extends BaseAdapter {
 
     private final Activity         activity;
     private final ArrayList<Album> data;
     private static LayoutInflater  inflater = null;
 
     public ListAlbumAdapter(Activity a, ArrayList<Album> d) {
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
         TextView text_singer = (TextView) vi.findViewById(R.id.singer_name);
         text_singer.setText(data.get(position).getSingerName());
         String name = data.get(position).getSingerName();
         if (name.indexOf("(") != -1) {
             String name2 = name.substring(0, name.indexOf("("));
             name = name2;
         }
         text_singer.setText(name);
         text_name.setText(data.get(position).getName());
         try {
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy MMM");
             text_time.setText(formatter.format(data.get(position).getDate()));
         } catch (Exception e) {
 
         }
 
         vi.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(activity, AlbumActivity.class);
                 Bundle bundle = new Bundle();
                 bundle.putInt("AlbumId", data.get(position).getId());
                 bundle.putString("AlbumName", data.get(position).getName());
                 bundle.putString("SingerNmae", data.get(position).getSingerName());
                 intent.putExtras(bundle);
                 activity.startActivity(intent);
             }
         });
 
         return vi;
     }
 }
