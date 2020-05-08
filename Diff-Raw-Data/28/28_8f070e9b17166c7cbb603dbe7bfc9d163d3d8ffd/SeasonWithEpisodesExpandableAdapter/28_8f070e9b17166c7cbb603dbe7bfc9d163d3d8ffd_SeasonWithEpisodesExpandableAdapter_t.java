 package com.steto.diaw.adapter;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import com.steto.diaw.model.Episode;
 import com.steto.diaw.model.Season;
 import com.steto.diaw.view.ListItemEpisodeExpendable;
 import com.steto.diaw.view.ListItemSeasonGroup;
 
 import java.util.List;
 
 public class SeasonWithEpisodesExpandableAdapter extends BaseExpandableListAdapter {
 
 	private Context mContext;
 	private List<Season> mData;
 
 	public SeasonWithEpisodesExpandableAdapter(Context ctx, List<Season> data) {
 		mContext = ctx;
 		mData = data;
 	}
 
 	@Override
 	public int getGroupCount() {
		return mData == null ? 0 : mData.size();
 	}
 
 	@Override
 	public int getChildrenCount(int pos) {
 		return getGroup(pos).getEpisodes().size();
 	}
 
 	@Override
 	public Season getGroup(int pos) {
 		return mData.get(pos);
 	}
 
 	@Override
 	public Episode getChild(int posGroup, int posChild) {
 		return getGroup(posGroup).getEpisodes().get(posChild);
 	}
 
 	@Override
 	public long getGroupId(int pos) {
 		return getGroup(pos).getNumber();
 	}
 
 	//see http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.2.2_r1/android/widget/SimpleExpandableListAdapter.java#SimpleExpandableListAdapter.getChildId%28int%2Cint%29
 	@Override
 	public long getChildId(int posGroup, int posChild) {
 		return posChild;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public View getGroupView(int posGroup, boolean b, View view, ViewGroup viewGroup) {
 		ListItemSeasonGroup current;
 		if (view == null || !(view instanceof ListItemSeasonGroup)) {
 			current = new ListItemSeasonGroup(mContext);
 		} else {
 			current = (ListItemSeasonGroup) view;
 		}
 		current.setData(getGroup(posGroup));
 		return current;
 	}
 
 	@Override
 	public View getChildView(int posGroup, int posChild, boolean b, View view, ViewGroup viewGroup) {
 		ListItemEpisodeExpendable current;
 		if (view == null || !(view instanceof ListItemEpisodeExpendable)) {
 			current = new ListItemEpisodeExpendable(mContext);
 		} else {
 			current = (ListItemEpisodeExpendable) view;
 		}
 		current.setData(getChild(posGroup, posChild));
 		return current;
 	}
 
 	@Override
 	public boolean isChildSelectable(int posGroup, int posChild) {
 		return true;
 	}
 }
