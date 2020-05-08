 /*
  * Copyright 2013 Adam Saturna
  *  Copyright 2013 Brian Trinh
  *  Copyright 2013 Ethan Mykytiuk
  *  Copyright 2013 Prateek Srivastava (@f2prateek)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package com.cmput301.recipebot.ui;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import com.actionbarsherlock.view.Window;
 import com.cmput301.recipebot.R;
 import com.cmput301.recipebot.client.ESClient;
 import com.cmput301.recipebot.model.Recipe;
 import com.cmput301.recipebot.ui.adapters.RecipeGridAdapter;
 import roboguice.inject.InjectExtra;
 import roboguice.inject.InjectView;
 
 import java.io.IOException;
 import java.util.List;
 
 import static com.cmput301.recipebot.util.LogUtils.makeLogTag;
 
 /**
  * Ideally we would re-use our fragment, but search menu button conflicts.
  */
 public class SearchRecipeActivity extends BaseActivity implements AdapterView.OnItemClickListener {
 
     public static final String EXTRA_RECIPE_NAME = "EXTRA_RECIPE_NAME";
     private static final String LOGTAG = makeLogTag(SearchRecipeActivity.class);
 
     @InjectView(R.id.gridview)
     GridView gridView;
     @InjectExtra(EXTRA_RECIPE_NAME)
     String query;
 

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.saved_recipes_grid);
 
         gridView.setOnItemClickListener(this);
         gridView.setEmptyView(new ProgressBar(this));
         new GetRecipesTask().execute(query);
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         Toast.makeText(this, "clicked recipe # " + position, Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(this, RecipeActivity.class);
         intent.putExtra(RecipeActivity.EXTRA_RECIPE, (Recipe) view.getTag());
         startActivity(intent);
     }
 
     private class GetRecipesTask extends AsyncTask<String, Void, List<Recipe>> {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected List<Recipe> doInBackground(String... params) {
             String query = params[0];
             ESClient client = new ESClient();
             try {
                 List<Recipe> recipes = client.searchRecipes(query);
                 return recipes;
             } catch (IOException e) {
                 Log.e(LOGTAG, e.toString());
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(List<Recipe> recipes) {
             if (recipes != null) {
                 gridView.setAdapter(new RecipeGridAdapter(SearchRecipeActivity.this, recipes));
             }
             setProgressBarIndeterminateVisibility(false);
             super.onPostExecute(recipes);
         }
     }
 
 }
