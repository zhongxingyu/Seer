 package com.examples.gg;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.costum.android.widget.LoadMoreListView;
 import com.examples.gg.VideoArrayAdapter.ViewHolder;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
 import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
 import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
 
 public class MatchArrayAdapter extends ArrayAdapter<Match> {
 	private final Context context;
 
 	private LayoutInflater inflater;
 
 	DisplayImageOptions options;
 	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
 	private ImageLoader imageLoader;
 	
 	private ArrayList<Match> matches;
 
 	public MatchArrayAdapter(Context context, ArrayList<Match> matches,
 			ImageLoader imageLoader) {
 
 		super(context, R.layout.matchtable, matches);
 
 		this.context = context;
 
 		this.imageLoader = imageLoader;
 		inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		this.imageLoader.init(ImageLoaderConfiguration.createDefault(context));
 		// imageLoader=new ImageLoader(context.getApplicationContext());
 
 		options = new DisplayImageOptions.Builder()
 				.showStubImage(R.drawable.loading)
 				.showImageForEmptyUri(R.drawable.loading)
 				.showImageOnFail(R.drawable.loading).cacheInMemory(true)
 				.cacheOnDisc(true)
 				.displayer(new RoundedBitmapDisplayer(20))
 				.build();
 		
 		this.matches = matches;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		ViewHolder holder;
 
 		if (convertView == null) {
 			convertView = inflater.inflate(R.layout.matchtable, parent, false);
 
 			holder = new ViewHolder();
 
 			holder.time = (TextView) convertView.findViewById(R.id.matchTime);
 			
 			holder.teamName1 = (TextView) convertView.findViewById(R.id.teamName1);
 			holder.teamName2 = (TextView) convertView.findViewById(R.id.teamName2);
 			
			holder.teamIcon1 = (ImageView) convertView.findViewById(R.id.teamIcon1);
 			holder.teamIcon2 = (ImageView) convertView.findViewById(R.id.teamIcon2);
 			
 			convertView.setTag(holder);
 			
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		holder.time.setText(matches.get(position).getTime());
 		
 		holder.teamName1.setText(matches.get(position).getTeamName1());
 		holder.teamName2.setText(matches.get(position).getTeamName2());
 		
 		imageLoader.displayImage(matches.get(position).getTeamIcon1(),
 				holder.teamIcon1, options, animateFirstListener);
 		
 		imageLoader.displayImage(matches.get(position).getTeamIcon2(),
 				holder.teamIcon2, options, animateFirstListener);
 
 		return convertView;
 	}
 
 	static class ViewHolder {
 		TextView time;
 		
 		TextView teamName1;
 		TextView teamName2;
 		ImageView teamIcon1;
 		ImageView teamIcon2;
 
 
 	}
 
 
 }
