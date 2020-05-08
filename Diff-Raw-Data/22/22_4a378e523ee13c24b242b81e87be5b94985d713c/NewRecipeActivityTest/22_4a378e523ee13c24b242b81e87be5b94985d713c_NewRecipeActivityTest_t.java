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
import android.support.v4.view.ViewPager;
 import android.test.ActivityInstrumentationTestCase2;
import com.cmput301.recipebot.R;
 import com.cmput301.recipebot.ui.EditRecipeActivity;
 import com.squareup.spoon.Spoon;
 
 import static org.fest.assertions.api.ANDROID.assertThat;
 
 public class NewRecipeActivityTest extends ActivityInstrumentationTestCase2<EditRecipeActivity> {
 
     protected Instrumentation instrumentation;
     protected EditRecipeActivity activity;
 
     /**
      * Create test for {@link com.cmput301.recipebot.ui.EditRecipeActivity}
      */
     public NewRecipeActivityTest() {
         super(EditRecipeActivity.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         instrumentation = getInstrumentation();
         activity = getActivity();
     }
 
     /**
      * Test that new recipe activity can be started.
      */
     public void testNewRecipeActivityExists() {
         assertThat(activity).isNotNull();
         Spoon.screenshot(activity, "initial_state");
     }

    /**
     * Test that new recipe activity can be started.
     */
    public void testTwoAddImageButtonsShown() {
        Spoon.screenshot(activity, "initial_state");
        final ViewPager pager = (ViewPager) activity.findViewById(R.id.pager_recipe_images);
        assertThat(pager.getAdapter()).hasCount(2);
    }
 }
