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
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.text.TextUtils;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.cmput301.recipebot.R;
 import com.cmput301.recipebot.model.Ingredient;
 import com.cmput301.recipebot.model.Recipe;
 import com.cmput301.recipebot.model.RecipeModel;
 import com.cmput301.recipebot.model.User;
 import com.cmput301.recipebot.ui.adapters.EditableImagePagerAdapter;
 import com.cmput301.recipebot.util.AppConstants;
 import roboguice.inject.InjectView;
 
 import java.io.File;
 import java.util.List;
 import java.util.UUID;
 
 import static com.cmput301.recipebot.util.LogUtils.makeLogTag;
 
 /**
  * An Activity that allows user to add a recipe.
  */
 public class EditRecipeActivity extends AbstractRecipeActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
 
     private static final String LOGTAG = makeLogTag(EditRecipeActivity.class);
     private static final int RESULT_LOAD_IMAGE = 458;
     private static final int TAKE_PICTURE = 531;
     private Uri cameraImageUri;
 
     private ActionMode mActionMode;
     private CompoundButton selectedCheckBox;
 
     @InjectView(R.id.widget_direction)
     LinearLayout mWidgetDirection;
     @InjectView(R.id.widget_ingredient)
     LinearLayout mWidgetIngredient;
     @InjectView(R.id.widget_tag)
     LinearLayout mWidgetTag;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mWidgetDirection.setVisibility(View.VISIBLE);
         mWidgetIngredient.setVisibility(View.VISIBLE);
         mWidgetTag.setVisibility(View.VISIBLE);
         mButtonAddDirection.setOnClickListener(this);
         mButtonAddIngredient.setOnClickListener(this);
         mButtonAddTag.setOnClickListener(this);
     }
 
     @Override
     protected void fillView() {
         if (mRecipe == null) {
             mRecipe = new Recipe();
        } else {
            super.fillView();
         }
     }
 
     @Override
     public void setPhotos() {
         mViewPhotos.setAdapter(new EditableImagePagerAdapter(this, mRecipe.getPhotos(),
                 addImageFromGalleryListener, addImageFromCameraListener));
     }
 
     private View.OnClickListener addImageFromGalleryListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent i = new Intent(
                     Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
             startActivityForResult(i, RESULT_LOAD_IMAGE);
         }
     };
 
     private View.OnClickListener addImageFromCameraListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
             i.putExtra(MediaStore.EXTRA_OUTPUT,
                     Uri.fromFile(photo));
             cameraImageUri = Uri.fromFile(photo);
             startActivityForResult(i, TAKE_PICTURE);
         }
     };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, R.id.menu_save, Menu.NONE, R.string.menu_save)
                 .setIcon(R.drawable.ic_action_save)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         return super.onCreateOptionsMenu(menu);
     }
 
     private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
 
         public boolean onCreateActionMode(ActionMode mode, Menu menu) {
             MenuInflater inflater = mode.getMenuInflater();
             inflater.inflate(R.menu.activity_recipe_cab, menu);
             return true;
         }
 
         public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
             return false;
         }
 
         public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
             switch (item.getItemId()) {
                 case R.id.menu_delete_item_from_list:
                     deleteSelectedItem();
                     mode.finish();
                     return true;
                 default:
                     return false;
             }
         }
 
         // Called when the user exits the action mode
         public void onDestroyActionMode(ActionMode mode) {
             if (selectedCheckBox != null) {
                 selectedCheckBox.setChecked(false);
             }
             mActionMode = null;
         }
     };
 
     /**
      * This deletes an item from our list.
      */
     private void deleteSelectedItem() {
         TaggedItem item = (TaggedItem) selectedCheckBox.getTag();
         switch (item.type) {
             case TYPE_DIRECTION:
                 String data = (String) item.data;
                 deleteSelectedDirection(data);
                 break;
             case TYPE_TAG:
                 String tag = (String) item.data;
                 deleteSelectedTag(tag);
                 break;
             case TYPE_INGREDIENT:
                 Ingredient ingredient = (Ingredient) item.data;
                 deleteSelectedIngredient(ingredient);
                 break;
         }
 
     }
 
     private void deleteSelectedDirection(String direction) {
         if (direction != null) {
             mRecipe.getDirections().remove(direction);
             fillListLayout(mListViewDirections, mRecipe.getDirections(), TYPE_DIRECTION);
         }
     }
 
     private void deleteSelectedIngredient(Ingredient ingredient) {
         if (ingredient != null) {
             mRecipe.getIngredients().remove(ingredient);
             fillListLayout(mListViewIngredients, mRecipe.getIngredients(), TYPE_INGREDIENT);
         }
     }
 
     private void deleteSelectedTag(String tag) {
         if (tag != null) {
             mRecipe.getTags().remove(tag);
             fillListLayout(mListViewTags, mRecipe.getTags(), TYPE_TAG);
         }
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         if (!isChecked) {
             if (mActionMode != null) {
                 // Unchecked, so stop the action mode
                 mActionMode.finish();
             }
         } else {
             if (selectedCheckBox != null) {
                 // Disable the old checkbox
                 selectedCheckBox.setChecked(false);
             }
             selectedCheckBox = buttonView;
             if (mActionMode != null) {
                 mActionMode.finish();
             }
             mActionMode = startActionMode(mActionModeCallback);
             mActionMode.setTitle(getResources().getString(R.string.count_items_selected, 1));
         }
 
     }
 
     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.button_add_tag:
                 addTag();
                 break;
             case R.id.button_add_direction:
                 addDirection();
                 break;
             case R.id.button_add_ingredient:
                 addIngredient();
                 break;
         }
     }
 
     /**
      * Add a {@link Ingredient} to the recipe.
      */
     private void addIngredient() {
         if (isEditTextEmpty(mEditTextIngredientName)) {
             // Name should not be empty.
             mEditTextIngredientName.setError(getResources().getString(R.string.blank_field));
             return;
         } else {
             mEditTextIngredientName.setError(null);
         }
 
         String name = mEditTextIngredientName.getText().toString();
         // Don't parse the float if the field is empty.
         float quantity = isEditTextEmpty(mEditTextIngredientQuantity) ?
                 0.0f : Float.parseFloat(mEditTextIngredientQuantity.getText().toString());
         String unit = mEditTextIngredientUnit.getText().toString();
         Ingredient item = new Ingredient(name, unit, quantity);
 
         //Sanitize the input
         mEditTextIngredientName.setText(null);
         mEditTextIngredientQuantity.setText(null);
         mEditTextIngredientUnit.setText(null);
         mRecipe.getIngredients().add(item);
 
         //Update UI
         fillListLayout(mListViewIngredients, mRecipe.getIngredients(), TYPE_INGREDIENT);
     }
 
     /**
      * Add a Direction to the recipe.
      */
     private void addDirection() {
         if (isEditTextEmpty(mEditTextDirection)) {
             // Field should not be empty
             mEditTextDirection.setError(getResources().getString(R.string.blank_field));
             return;
         } else {
             mEditTextDirection.setError(null);
         }
 
         String direction = mEditTextDirection.getText().toString();
         mRecipe.getDirections().add(direction);
 
         //Sanitize the input
         mEditTextDirection.setText(null);
 
         //Update UI
         fillListLayout(mListViewDirections, mRecipe.getDirections(), TYPE_DIRECTION);
     }
 
     /**
      * Add a tag to the recipe.
      */
     private void addTag() {
         if (isEditTextEmpty(mEditTextTag)) {
             // Field should not be empty
             mEditTextTag.setError(getResources().getString(R.string.blank_field));
             return;
         } else {
             mEditTextTag.setError(null);
         }
 
         String tag = mEditTextTag.getText().toString();
         mRecipe.getTags().add(tag);
 
         //Sanitize the input
         mEditTextTag.setText(null);
 
         //Update UI
         fillListLayout(mListViewTags, mRecipe.getTags(), TYPE_TAG);
     }
 
     /**
      * Checks if an {@link EditText} field is empty.
      *
      * @param editText {@link EditText} to check.
      * @return true if editText is empty.
      */
     private boolean isEditTextEmpty(EditText editText) {
         return editText.getText().toString().isEmpty();
     }
 
     /**
      * Allows for an image to be selected from the phone's Gallery
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
             Uri selectedImage = data.getData();
             String[] filePathColumn = {MediaStore.Images.Media.DATA};
             Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
             cursor.moveToFirst();
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             String picturePath = cursor.getString(columnIndex);
             cursor.close();
             mRecipe.getPhotos().add("file:///" + picturePath);
             ((EditableImagePagerAdapter) mViewPhotos.getAdapter()).swapData(mRecipe.getPhotos());
         } else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
             Uri selectedImage = cameraImageUri;
             mRecipe.getPhotos().add("file:///" + selectedImage);
             ((EditableImagePagerAdapter) mViewPhotos.getAdapter()).swapData(mRecipe.getPhotos());
         }
 
     }
 
     @Override
     public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_save:
                 save();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void save() {
         boolean update = mRecipe.getId() != null;
         if (!updateRecipeFromUI()) {
             return;
         }
         if (update) {
             RecipeModel.getInstance(this).updateRecipe(mRecipe);
         } else {
             RecipeModel.getInstance(this).insertRecipe(mRecipe);
         }
         new WriteRecipeToFileTask().execute(mRecipe);
     }
 
     /**
      * Update mRecipe from the UI fields.
      *
      * @return false if some required fields are empty.
      */
     private boolean updateRecipeFromUI() {
         boolean hasError = false;
         if (TextUtils.isEmpty(mEditTextRecipeName.getText())) {
             mEditTextRecipeName.setError(getString(R.string.blank_field));
             hasError = true;
         } else {
             mEditTextRecipeName.setError(null);
         }
 
         if (isListEmpty(mRecipe.getIngredients())) {
             mEditTextIngredientName.setError(getString(R.string.at_least_one_ingredient_required));
             hasError = true;
         } else {
             mEditTextIngredientName.setError(null);
         }
 
         if (isListEmpty(mRecipe.getDirections())) {
             mEditTextDirection.setError(getString(R.string.at_least_one_direction_required));
             hasError = true;
         } else {
             mEditTextDirection.setError(null);
         }
 
         if (hasError) {
             return false;
         }
 
         if (mRecipe.getId() == null) {
             // generate an id.
             mRecipe.setId(UUID.randomUUID().toString());
             SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
             String email = sharedPref.getString(AppConstants.KEY_USER_EMAIL, null);
             String name = sharedPref.getString(AppConstants.KEY_USER_NAME, null);
             mRecipe.setUser(new User(email, name));
         }
         mRecipe.setName(getEditTextString(mEditTextRecipeName));
         mRecipe.setDescription(getEditTextString(mEditTextRecipeDescription));
         // We're already keeping track of mRecipeDirections, mRecipeIngredients and mRecipeTags.
         return true;
     }
 
     /**
      * Return true if list is empty or null.
      *
      * @param list List to check
      * @return true if list is empty or null.
      */
     private boolean isListEmpty(List list) {
         return list == null || list.size() == 0;
     }
 
     private static String getEditTextString(EditText editText) {
         return editText.getText().toString();
     }
 
 
 }
 
 
