 /*
  * Copyright 2013 Adam Saturna
  * Copyright 2013 Brian Trinh
  * Copyright 2013 Ethan Mykytiuk
  * Copyright 2013 Prateek Srivastava (@f2prateek)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cmput301.recipebot.ui.adapters;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.cmput301.recipebot.R;
 import com.cmput301.recipebot.model.beans.Recipe;
 import com.cmput301.recipebot.util.BitmapUtils;
 import com.nostra13.universalimageloader.core.ImageLoader;
 
 import java.util.List;
 
 public class RecipeGridAdapter extends BaseAdapter {
 
     private List<Recipe> mRecipes;
     private Context mContext;
 
     /**
      * Construct the Adapter
      *
      * @param context everything needs a context =(
      * @param recipes List of {@link Recipe} to display
      */
     public RecipeGridAdapter(Context context, List<Recipe> recipes) {
         mContext = context;
         mRecipes = recipes;
     }
 
     @Override
     public int getCount() {
         return mRecipes.size();
     }
 
     @Override
     public Object getItem(int position) {
         return mRecipes.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         Recipe recipe = mRecipes.get(position);
         if (recipe.getPhotos() != null && recipe.getPhotos().size() != 0) {
             for (String s : recipe.getPhotos()) {
                if (s.startsWith("http:")) {
                     final ImageView imageView = (ImageView) layoutInflater.inflate(R.layout.item_recipe_image, parent, false);
                     ImageLoader.getInstance().displayImage(recipe.getPhotos().get(0), imageView);
                     imageView.setTag(recipe);
                     return imageView;
                 }
             }

             final ImageView imageView = (ImageView) layoutInflater.inflate(R.layout.item_recipe_image, parent, false);
             new BitmapUtils.DecodeBitmapTask(imageView).execute(recipe.getPhotos().get(0));
             imageView.setTag(recipe);
             return imageView;
         } else {
             // If no photos, simply display some text.
             final TextView textView = (TextView) layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
             textView.setText(recipe.getName());
             textView.setTag(recipe);
             return textView;
         }
     }
 
     public void swapData(List<Recipe> recipes) {
         mRecipes = recipes;
         notifyDataSetChanged();
     }
 }
