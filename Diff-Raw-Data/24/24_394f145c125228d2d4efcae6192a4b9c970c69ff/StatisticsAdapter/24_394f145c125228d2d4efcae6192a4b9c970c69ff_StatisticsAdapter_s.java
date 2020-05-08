 package de.saschahlusiak.hrw.dienststatus.statistic;
 
 import java.util.ArrayList;
 
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.model.Statistic;
 
 import android.content.Context;
 import android.graphics.drawable.BitmapDrawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 public class StatisticsAdapter extends BaseAdapter {
 	LayoutInflater inflater;
 	ArrayList<Statistic> items;
 	int loading;
 	
 	private class ViewHolder {
 		ImageView image;
 		ProgressBar progress;
 	}
 	
 	public StatisticsAdapter(Context context, String urls[]) {
 		int i;
 		inflater = LayoutInflater.from(context);
 		items = new ArrayList<Statistic>();
 		loading = -1;
 		for (i = 0; i < urls.length; i++) {
 			items.add(new Statistic(i, urls[i]));
 		}
 	}
 
 	@Override
 	public int getCount() {
 		return items.size();
 	}
 	
 	public void clear() {
 		items.clear();
 	}
 	
 	public void invalidate() {
 		for (Statistic s: items) {
 			s.setValid(false);
 		}
 		loading = -1;
 	}
 	
 	public void setLoading(int i) {
 		loading = i;
 	}
 		
 	public void update(int index, BitmapDrawable bitmap) {
 		Statistic s;
 		s = items.get(index);
 		
 		s.setBitmap(bitmap);
 		if (bitmap != null) {
			/* width is 463px */
			bitmap.getBitmap().setDensity(128);
			/* with 128dpi the image is 3.7" = 9,18cm */ 
			/* rendering it at 256dpi, the image will be scaled up by 2 to match 9.18cm */ 
			bitmap.setTargetDensity(256);
			bitmap.setFilterBitmap(true);
 			s.setValid(true);
 		} else {
 			s.setValid(false);
 		}
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return items.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 		Statistic s;
 		if (convertView == null) {
 			convertView = inflater.inflate(R.layout.statistics_item, parent,
 					false);
 
 			holder = new ViewHolder();
 			holder.image = (ImageView) convertView.findViewById(R.id.image);
 			holder.progress = (ProgressBar) convertView.findViewById(R.id.progressBar);
 
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 		
 		s = items.get(position);
 
 //		convertView.setMinimumHeight(500);
 		if (s.getBitmap() != null) {
 //			b.setTargetDensity(b.getBitmap().getDensity());
			holder.image.setImageDrawable(s.getBitmap());
 //			holder.image.setMaxHeight(10000);
 //			holder.image.setMinimumWidth(500);
 //			holder.image.setMinimumHeight(0);
 		} else {
 			holder.image.setImageResource(R.drawable.hrw_logo);
 //			holder.image.setMinimumHeight(150);
 //			holder.image.setMaxHeight(150);
 		}
 		if (s.getValid())
 			holder.image.setAlpha(255);
 		else
 			holder.image.setAlpha(90);
 		holder.progress.setVisibility((position == loading) ? View.VISIBLE : View.GONE);
 		
 		return convertView;
 	}
 
 }
