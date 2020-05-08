 package com.taiwan.imageload;
 
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
 
 import com.kosbrother.lyric.R;
 import com.kosbrother.lyric.SongActivity;
 import com.kosbrother.lyric.entity.Song;
 
 public class ListSongAdapter extends BaseAdapter {
 
     private final Activity        activity;
     private final ArrayList<Song> data;
     private static LayoutInflater inflater = null;
 
     public ListSongAdapter(Activity a, ArrayList<Song> d) {
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
         text_name.setText(data.get(position).getName());
 
         TextView text_singer = (TextView) vi.findViewById(R.id.singer_name);
         String name = data.get(position).getSingerName();
         if (name.indexOf("(") != -1) {
             String name2 = name.substring(0, name.indexOf("("));
             // String bracket = name.substring(name.indexOf("("), name.length());
             // String space = "";
             // for (int i = 0; i < bracket.length() / 2 - 1; i++) {
             // space += " ";
             // }
             name = name2;
         }
         text_singer.setText(name);
 
         vi.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(activity, SongActivity.class);
                 Bundle bundle = new Bundle();
                 bundle.putInt("SongId", data.get(position).getId());
                 bundle.putString("SongName", data.get(position).getName());
                 intent.putExtras(bundle);
                 activity.startActivity(intent);
             }
         });
 
         // if (enableLongClick){
         // vi.setOnLongClickListener(new OnLongClickListener() {
         // @Override
         // public boolean onLongClick(View v) {
         // // TODO Auto-generated method stub
         // final String[] ListStr = { "欣賞歌曲", "刪除", "取消" };
         // AlertDialog.Builder builder = new AlertDialog.Builder(activity);
         // builder.setTitle(data.get(position).getName());
         // builder.setItems(ListStr, new DialogInterface.OnClickListener() {
         // public void onClick(DialogInterface dialog, int item) {
         // if (item == 0 ) {
         // // to song activity
         // Intent intent = new Intent(activity, SongActivity.class);
         // Bundle bundle = new Bundle();
         // bundle.putInt("SongId", data.get(position).getId());
         // bundle.putString("SongName", data.get(position).getName());
         // intent.putExtras(bundle);
         // activity.startActivity(intent);
         // } else if(item == 1){
         // // delete
         // SQLiteLyric db = new SQLiteLyric(activity);
         // db.deleteSong(data.get(position));
         //
         // } else if(item == 2){
         // dialog.cancel();
         // }
         // }
         // });
         // alert = builder.create();
         // if(!alert.isShowing()){
         // alert.show();
         // }
         // return false;
         // }
         // });
         // }
 
         return vi;
     }
 }
