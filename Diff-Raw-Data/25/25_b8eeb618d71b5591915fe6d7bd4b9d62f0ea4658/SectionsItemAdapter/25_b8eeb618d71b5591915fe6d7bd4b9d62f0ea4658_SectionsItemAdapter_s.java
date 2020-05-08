 package com.quizz.places.adapters;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 import com.quizz.core.models.Level;
 import com.quizz.core.models.Section;
 import com.quizz.core.utils.ConvertUtils;
 import com.quizz.core.widgets.SectionProgressView;
 import com.quizz.places.R;
 
 public class SectionsItemAdapter extends ArrayAdapter<Section> {
 
 	private static final int[] PROGRESS_DRAWABLES = new int[] { 
 		R.drawable.fg_section_progress_blue,
 		R.drawable.fg_section_progress_green,
 		R.drawable.fg_section_progress_orange,
 		R.drawable.fg_section_progress_pink,
 		R.drawable.fg_section_progress_purple,
 		R.drawable.fg_section_progress_yellow,
 		R.drawable.fg_section_progress_red
 	};
 	
 	private int mLineLayout;
 	private LayoutInflater mInflater;
 	private Drawable[] mProgressDrawables = new Drawable[PROGRESS_DRAWABLES.length];
 	private Drawable mLockDrawable;
 	private Drawable mCompleteDrawable;
 	
 	public SectionsItemAdapter(Context context, int lineLayout) {
 		super(context, lineLayout);
 
 		mLineLayout = lineLayout;
 		mInflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		// Preload Drawables to gain performance inside getView()
 		for (int i = 0; i < PROGRESS_DRAWABLES.length; i++) {
 			mProgressDrawables[i] = context.getResources().getDrawable(PROGRESS_DRAWABLES[i]);
 		}
 		mLockDrawable = context.getResources().getDrawable(R.drawable.ic_lock);
 		mCompleteDrawable = context.getResources().getDrawable(R.drawable.ic_complete);
 	}
 
 	static class ViewHolder {
 		TextView name;
 		TextView levels;
 		ImageView buttonEnter;
 		SectionProgressView progress;
 		ViewSwitcher progressSwitcher;
 	}
 
 	private int nbDoneLevels(Section section) {
 		int clearedLevels = 0;
 		for (Level level : section.levels) {
 			if (level.status == Level.STATUS_LEVEL_CLEAR)
 				clearedLevels++;
 		}
 		return clearedLevels;
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 		if (convertView == null) {
 
 			convertView = this.mInflater.inflate(this.mLineLayout, null);
 
 			holder = new ViewHolder();
 			holder.name = (TextView) convertView.findViewById(R.id.sectionName);
 			holder.levels = (TextView) convertView.findViewById(R.id.sectionNbLevels);
 			holder.buttonEnter = (ImageView) convertView.findViewById(R.id.sectionEnterButton);
 			holder.progressSwitcher = (ViewSwitcher) convertView.findViewById(R.id.sectionProgressSwitcher);
 			holder.progress = (SectionProgressView) convertView.findViewById(R.id.sectionProgress);
 			
 			holder.buttonEnter.setAlpha(225);
 
 			convertView.setTag(holder);
 
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		Section section = getItem(position);
 		holder.name.setText(section.name);
 		holder.progress.setProgressDrawable(mProgressDrawables[position
 				% mProgressDrawables.length]);
 		
 		holder.levels.setText(this.nbDoneLevels(section) + " / " + section.levels.size());
 		
 		if (section.levels != null && section.levels.size() > 0) {
 			int nbLevelsCleared = 0;
 			for (Level lvl : section.levels) {
 				nbLevelsCleared += (lvl.status == Level.STATUS_LEVEL_CLEAR) ? 1 : 0;
 			}
 			float progressValue = (nbLevelsCleared * 100.0f)  / section.levels.size();
 			holder.progress.setProgressValue(progressValue);
 		} else {
 			holder.progress.setProgressValue(1);
 		}
 		
 		int verticalPadding = (int) ConvertUtils.convertDpToPixels(2.5f, getContext());
 		int horizontalPadding = (int) ConvertUtils.convertDpToPixels(3f, getContext());
 		holder.progress.setPaddingProgress(horizontalPadding, verticalPadding,
 				horizontalPadding, verticalPadding);
 
 		// Section locked management
 		boolean sectionLocked = (section.status == Section.SECTION_LOCKED);
 		Drawable sectionDrawable = null;
 		if (sectionLocked) {
 			sectionDrawable = mLockDrawable;
 		} else if (section.isComplete()) {
 			sectionDrawable = mCompleteDrawable;
 		}
 		
 		holder.name.setCompoundDrawablesWithIntrinsicBounds(sectionDrawable, null, null, null);
 		holder.progress.setDisplayInitialProgressIfEmpty((sectionLocked) ? false : true);
 		holder.buttonEnter.setVisibility((sectionLocked) ? View.GONE : View.VISIBLE);
 		holder.levels.setVisibility((sectionLocked) ? View.GONE : View.VISIBLE);
 		holder.name.setTextColor((sectionLocked) ? 0xee666666 : 0xff666666);
 		
 		if (sectionLocked) {
 			if (holder.progressSwitcher.getNextView() != holder.progress) {
 				holder.progressSwitcher.showNext();
 			}
 		} else if (holder.progressSwitcher.getNextView() == holder.progress) {
 			holder.progressSwitcher.showNext();
 		}
 		return convertView;
 	}
 }
