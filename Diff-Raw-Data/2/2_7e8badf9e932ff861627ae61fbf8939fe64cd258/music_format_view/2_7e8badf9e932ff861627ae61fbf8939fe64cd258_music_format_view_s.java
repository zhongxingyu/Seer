 package com.music.utility;
 
 import java.util.List;
 
 import com.music.Application.SingletonApp;
 import com.music.model.MediaFile;
 
 import android.R;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class music_format_view extends ArrayAdapter<MediaFile> {
 
 	Context context;
 	List<MediaFile> lst_mediaFile;
 	
 	int musicView = com.example.musicapplication.R.layout.music_view;
 	int artist_Music_View = com.example.musicapplication.R.id.txt_artist_music_view;
 	int title_Music_View = com.example.musicapplication.R.id.txt_title_music_view;
 	
 	protected String TAG = "music_format_view";
 	
 	public music_format_view(Context context, int textViewResourceId,List<MediaFile> objects) {
		super(context, com.example.musicapplication.R.layout.music_view, SingletonApp.getList_media());
 		// TODO Auto-generated constructor stub
 		this.context = context;
 		this.lst_mediaFile = objects;
 		
 	}
 	
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		Log.d(TAG,"Start Here");
 		LayoutInflater inflat =  (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
 		View view = inflat.inflate(musicView, parent, false); 
 		
 		MediaFile mediaFile;
 		mediaFile =  lst_mediaFile.get(position);
 		TextView artistView =  (TextView)view.findViewById(artist_Music_View);
 		TextView titleView =  (TextView)view.findViewById(title_Music_View);
 		
 		artistView.setText(mediaFile.getArtist());
 		titleView.setText(mediaFile.getTitle());
 		Log.d(TAG,"End Here");
 		return view;
 	}
 	
 
 	
 }
