 package com.ad.cow.library;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.ad.cow.ExperienceActivity;
 import com.ad.cow.R;
 
 public class RatingAdapter extends ArrayAdapter {
 
 	private LayoutInflater mInflater;
 	private ArrayList items;
 	private Context context;
 
 	public RatingAdapter(Context context, int textViewResourceId, ArrayList items) {
 		super(context, textViewResourceId, items);
 		mInflater = LayoutInflater.from(context);
 		this.items = items;
 		this.context = context;
 	}
 
 	@Override
 	public int getCount() {
 		return items.size();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.list_row, parent, false);
 			holder = new ViewHolder();
 			holder.txtName = (TextView) convertView.findViewById(R.id.name);
 			holder.txtLevel = (TextView) convertView.findViewById(R.id.level);
 			holder.txtExp = (TextView) convertView.findViewById(R.id.exp);
 			holder.txtPbExp = (ProgressBar) convertView.findViewById(R.id.pb_exp);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		// Fill in the actual story info
 		Rating s = (Rating) items.get(position);
 		if (s.getName().length() > 35)
 			holder.txtName.setText(s.getName().substring(0, 32) + "...");
 		else
 			holder.txtName.setText(s.getName());
 		
 		int level = s.getLevel();
 		ExperienceActivity activity = new ExperienceActivity();
 		double currentXP = s.getExp() - activity.summedUpXpNeededForLevel(level);
 		double nettoXP = activity.nettoXpNeededForLevel(level + 1);
 		
 		holder.txtLevel.setText(Integer.toString(s.getLevel()));
		holder.txtExp.setText(Double.toString((int)currentXP)+"/"+Double.toString(nettoXP));
 
 		double percentByExp = activity.nettoXpNeededForLevel(level + 1) / 100;
 		double currentPercent = currentXP / percentByExp;
 		
 		holder.txtPbExp.setProgress((int) currentPercent);
 
   		return convertView;
  	}
 
   	static class ViewHolder {
  		public ProgressBar txtPbExp;
 		public TextView txtLevel;
 		public TextView txtExp;
 		public TextView txtName;
  	}
  }
