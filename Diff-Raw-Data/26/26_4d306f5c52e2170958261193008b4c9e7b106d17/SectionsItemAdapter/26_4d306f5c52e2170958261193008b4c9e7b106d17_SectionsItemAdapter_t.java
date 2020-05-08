 package com.quizz.places.adapters;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
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
 	}
 
 	static class ViewHolder {
 		TextView name;
 		TextView levels;
 		ImageView buttonEnter;
 		SectionProgressView progress;
 		
 		// In order to bring the background in top of the view when section is locked
 		View background;
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
 			holder.progress = (SectionProgressView) convertView.findViewById(R.id.sectionProgress);
 			holder.background = convertView.findViewById(R.id.section_content_bg);
 			
 			holder.buttonEnter.setAlpha(225);
 
 			convertView.setTag(holder);
 
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		Section section = getItem(position);
 		holder.name.setText(section.name);
 		holder.progress.setProgressDrawable(mProgressDrawables[position
 				% mProgressDrawables.length]);
		
		if (section.levels != null && section.levels.size() > 0) {
			int nbLevelsCleared = 0;
			for (Level lvl : section.levels) {
				nbLevelsCleared += (lvl.status == Level.STATUS_LEVEL_CLEAR) ? 1 : 0;
			}
			holder.progress.setProgressValue((int) (nbLevelsCleared * 100.0f)  / section.levels.size());
		} else {
			holder.progress.setProgressValue(0);
		}
		
 		int verticalPadding = (int) ConvertUtils.convertDpToPixels(2.5f, getContext());
 		int horizontalPadding = (int) ConvertUtils.convertDpToPixels(3f, getContext());
 		holder.progress.setPaddingProgress(horizontalPadding, verticalPadding,
 				horizontalPadding, verticalPadding);
 
 		// Level locked management
 		if (section.status == Section.SECTION_LOCKED) {
 			holder.name.setCompoundDrawablesWithIntrinsicBounds(null, null, mLockDrawable, null);
 		} else {
 			holder.name.setCompoundDrawables(null, null, null, null);
 		}
 		
 		return convertView;
 	}
 }
