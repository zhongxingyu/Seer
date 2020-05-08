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
 
 package com.cmput301.recipebot.test;
 
 import android.app.Instrumentation;
 import android.content.Intent;
import android.net.Uri;
 import android.support.v4.view.ViewPager;
 import android.test.ActivityInstrumentationTestCase2;
 import com.cmput301.recipebot.R;
 import com.cmput301.recipebot.model.Ingredient;
 import com.cmput301.recipebot.model.Recipe;
 import com.cmput301.recipebot.model.User;
 import com.cmput301.recipebot.ui.RecipeActivity;
 import com.squareup.spoon.Spoon;
 
 import java.util.ArrayList;
import java.util.UUID;
 
 import static org.fest.assertions.api.ANDROID.assertThat;
 
 public class RecipeActivityTest extends ActivityInstrumentationTestCase2<RecipeActivity> {
 
     protected Instrumentation instrumentation;
     protected RecipeActivity activity;
 
     /**
      * Create test for {@link com.cmput301.recipebot.ui.RecipeActivity}
      */
     public RecipeActivityTest() {
         super(RecipeActivity.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         instrumentation = getInstrumentation();
         setActivityIntent(getTestIntent()); // Set intent first
         activity = getActivity();
     }
 
     private Intent getTestIntent() {
         String id = "testing_id";
         String name = "Kentucky Fried Chicken";
         String description = "Fried Chicken";
         User user = new User("colonel@kfc.com", "Colonel Sanders");
         ArrayList<String> directions = new ArrayList<String>();
         directions.add("1. Mix");
         directions.add("2. Bake");
         directions.add("3. Eat");
         ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
         ingredients.add(new Ingredient("Chicken", "lb", 2f));
         ingredients.add(new Ingredient("Secret Spice #1", "tbsp.", 1f));
         ingredients.add(new Ingredient("Secret Spice #2", "tsp.", 1f));
         ingredients.add(new Ingredient("Buttermilk", "ml", 50f));
         ArrayList<String> photos = new ArrayList<String>();
         photos.add("http://www.kraftrecipes.com/assets/recipe_images/SHAKE_N_BAKE_Honey_Drummies.jpg");
         photos.add("http://images.media-allrecipes.com/userphotos/250x250/00/68/33/683349.jpg");
         ArrayList<String> tags = new ArrayList<String>();
         tags.add("chicken, fried, southern");
         Recipe recipe = new Recipe(id, name, description, user, ingredients, directions, photos, tags);
 
         Intent intent = new Intent(instrumentation.getContext(), RecipeActivity.class);
         intent.putExtra(RecipeActivity.EXTRA_RECIPE, recipe);
         return intent;
     }
 
     /**
      * Verify that {@link RecipeActivity} exists
      */
     public void testRecipeActivityExists() {
         assertThat(activity).isNotNull();
         Spoon.screenshot(activity, "initial_state");
     }
 
     /**
     * Verify that {@link RecipeActivity} exists
      */
     public void testVPShowsAllContent() {
         assertThat(activity).isNotNull();
         Spoon.screenshot(activity, "initial_state");
 
         final ViewPager viewPager = (ViewPager) activity.findViewById(R.id.pager_recipe_images);
 
         assertThat(viewPager.getAdapter()).hasCount(activity.mRecipe.getPhotos().size() + 1);
 
         for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
             final int count = i;
             instrumentation.runOnMainSync(new Runnable() {
                 @Override
                 public void run() {
                     viewPager.setCurrentItem(count);
                 }
             });
             instrumentation.waitForIdleSync();
             Spoon.screenshot(activity, "image_" + count);
         }
     }
 }
