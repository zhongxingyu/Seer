 /*
  * Copyright (C) 2013 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.morphoss.jumble.frontend;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.morphoss.jumble.R;
 import com.morphoss.jumble.database.JumbleCategoryTable;
 import com.morphoss.jumble.database.JumbleProvider;
 import com.morphoss.jumble.models.Category;
 
 public class CategoryGridAdapter extends BaseAdapter {
 
 	/**
 	 * This class is an adapter for the category gridview
 	 */
 	private final Context context;
 	private static ArrayList<Category> categories = null;
 	private int buttonWidth;
 	private int buttonHeight;
 	private final int width;
 	private final int height;
 	private static ImageView categoryImage;
 	private static TextView categoryLabel;
 	private Category category;
	public static boolean newLevel = false;
 	private static final String TAG = "CategoryGridAdapter";
 
 	public CategoryGridAdapter(Context context, int width, int height) {
 		super();
 		this.context = context;
 		this.width = width;
 		this.height = height;
 
 	}
 
 	/**
 	 * This method get the view of a specific category
 	 */
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		category = categories.get(position);
 		View categoryLayout = inflater
 				.inflate(R.layout.activity_category, null);
 		categoryLabel = (TextView) categoryLayout
 				.findViewById(R.id.category_label);
 		categoryImage = (ImageView) categoryLayout
 				.findViewById(R.id.category_image);
 		if (category.unlocked()) {
 			categoryImage.setImageDrawable(category.getImage(context));
 		}
		for(int i=1; i<categories.size(); i++){
			if(categories.get(i).unlocked()){
				newLevel = true;
			}
		}
 		categoryLayout.setTag(category.getId());
 		categoryLabel.setText(category.getLocalisedName());
 		return categoryLayout;
 	}
 
 	public void setLayout(View v) {
 		ViewGroup.LayoutParams lp = v.getLayoutParams();
 		lp.height = buttonHeight;
 		lp.width = buttonWidth;
 		v.setLayoutParams(lp);
 	}
 
 	/**
 	 * This method sets 8 categories on the screen
 	 * 
 	 * @param newCategories
 	 */
 	public void setCategories(Collection<Category> newCategories) {
 
 		CategoryGridAdapter.categories = new ArrayList<Category>(newCategories.size());
 		CategoryGridAdapter.categories.addAll(newCategories);
 
 		ContentResolver resolver = context.getContentResolver();
 		HashSet<String> unlockedSet = new HashSet<String>();
 		Cursor cursor = resolver
 				.query(JumbleProvider.CONTENT_URI_CATEGORIES,
 						new String[] { JumbleCategoryTable.CATEGORY },
 						JumbleCategoryTable.UNLOCK + " = '1'",
 						null, null);
 		try {
 			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
 					.moveToNext()) {
 				unlockedSet.add(cursor.getString(0));
 				Log.d(TAG,
 						"found an unlocked category : " + cursor.getString(0));
 			}
 		} catch (Exception e) {
 			Log.d(TAG, Log.getStackTraceString(e));
 		} finally {
 			if (cursor != null)
 				cursor.close();
 		}
 		Category cat = null;
 		for (int i = 0; i < categories.size(); i++) {
 			cat = categories.get(i);
 			Log.d(TAG, "category label :" + cat.getLocalisedName());
 			if( i == 0 || unlockedSet.contains(cat.getLocalisedName())) {
 				cat.setUnlocked(true);
 			}
 		}
 		if (width > height) {
 			buttonWidth = width / 4;
 			buttonHeight = height / 2;
 		} else {
 			buttonWidth = width / 2;
 			buttonHeight = height / 4;
 		}
 
 		this.notifyDataSetInvalidated();
 		this.notifyDataSetChanged();
 
 	}
 
 	@Override
 	public int getCount() {
 		if (categories == null)
 			return 0;
 		return categories.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return categories.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return categories.get(position).getId();
 	}
 
 	/**
 	 * this method gets the current category
 	 * 
 	 * @param position
 	 * @return the category selected
 	 */
 	public static Category getCategory(int position) {
 		return categories.get(position);
 	}
 }
