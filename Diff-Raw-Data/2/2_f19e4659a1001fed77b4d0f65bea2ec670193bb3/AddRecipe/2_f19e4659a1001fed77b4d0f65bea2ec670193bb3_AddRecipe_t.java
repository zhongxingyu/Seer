 package com.cmput301.recipebot.ui;
 
 
 //import android.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.method.ScrollingMovementMethod;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.cmput301.recipebot.R;
 
 //import com.actionbarsherlock.view.MenuItem;
 
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
 /**
  *     This class handles users creating a new recipe. It currently takes an uploaded user image
  *     and two edit texts for Instruction and Ingredients. The Save button would when implemented
  *     saved the Recipe for offline viewing. Users can share the recipe by uploading it online or
  *     publishing onto the database for online viewing/saving by other users.
  *
  *     ToDo:
  *     Horizontal Scroll On Images
  *     Multiple Images
  *     Waiting on DB for Publish/Save/Email
  *
  *     Save Title,Author, Date (Not sure how to implement this.. context menu on save?)
  *
  *
  *     Completed:
  *     User can publish photo to recipe
  *
  *     Attach Photo to Downloaded Recipe should use logic from previous.
  *
  *     Publish photo/recipe is stubbed and ready for database implementation
  *
  *     Save recipe is stubbed and also ready for cacheing
  *
  *     Share recipe needs menu button fixed.
  *
  *     Dialog for email appears, just need to implement a button for the send
  *
  *
  */
 public class AddRecipe extends Activity {
 
 
     private static int RESULT_LOAD_IMAGE = 1;
     private ShareActionProvider mShareActionProvider;
 
     /** To be Implemented after **/
 //
 //    private int[] Images = new int[] { R.drawable.buttonimage};
 //
 //    // mainLayout is the child of the HorizontalScrollView
 //    private LinearLayout mainLayout;
 //
 //    // Array that holds our image button/future drawables
 //    private int[] images = {R.drawable.buttonimage, R.drawable.buttonimage,
 //            R.drawable.buttonimage, R.drawable.buttonimage, R.drawable.buttonimage, R.drawable.buttonimage, R.drawable.buttonimage};
 //
 //    private View cell;
 //    private TextView text;
 //
 //    private ViewPager viewPager;
     //@InjectView(R.id.viewPager)
    // ViewPager viewPager;
 
 //    @Override
 //    public void onBackPressed() {
 //
 //        if(viewPager != null && viewPager.isShown()){
 //
 //            viewPager.setVisibility(View.GONE);
 //        }
 //        else{
 //
 //            super.onBackPressed();
 //        }
 //    }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.add_recipe);
 
         /**Move here later**/
         //Buttons
         Button publishButton = (Button) findViewById(R.id.PublishButton);
         Button saveButton = (Button) findViewById(R.id.SaveButton);
 
 
         //requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         /* Allows for Edit Texts to be Scrollable
          *
          * Get the string from these edit texts simply by
          * IngredientEditText.toString();
          * InstructionEditText.toString();
          *
          */
         final EditText IngredientEditText = (EditText) findViewById(R.id.IngredientID);
         IngredientEditText.setMovementMethod(new ScrollingMovementMethod());
         final EditText InstructionEditText = (EditText) findViewById(R.id.InstructionID);
         InstructionEditText.setMovementMethod(new ScrollingMovementMethod());
 
         /*
         This is the publish button's on click listener.
         The data from this recipe should be uploaded to the database.
 
         Until menu's share button works this button opens a context menu
         that asks the user whether they wish to publish or share...
         if we intend to keep this way we should change "Publish" to "Share"
          */
         publishButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 // Context menu example. Possibly use for save/share if needed
                 final CharSequence[] items = {"Publish","Email"};
 
                 AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipe.this);
                 builder.setTitle("How would you like to share?");
                 builder.setItems(items, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int item) {
                         Toast.makeText(getApplicationContext(),items[item], Toast.LENGTH_SHORT).show();
 
                         System.out.println("ITEM IS: "+item);
 
                         if(item == 1){
                             AlertDialog.Builder ebuilder = new AlertDialog.Builder(AddRecipe.this);
                             LayoutInflater inflater = getLayoutInflater();
                             final View myView = inflater.inflate(R.layout.email_entry, null);
                             ebuilder.setTitle("Who would you like to email?");
                             ebuilder.setView(myView);
                             AlertDialog alert = ebuilder.create();
                             alert.show();
 
 
                         }
                     }
 
                 });
                 AlertDialog alert = builder.create();
 
                 alert.show();
                     /*
                     STUB
                      */
 
             }
         });
 
          /*
         This is the Save button's on click listener.
         The data from this recipe should be stored for the user to view if needed.
          */
         saveButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 //onCreateContextMenu();
 
                     /*
                     STUB
                      */
 
             }
         });
 
    ImageButton imageButton = (ImageButton) findViewById(R.id.ImageButton);
          /*
         This is the Image button's on click listener.
         A context menu should appear and ask what the user wishes to do with images.
          */
         imageButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 Intent i = new Intent(
                         Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 
                 startActivityForResult(i, RESULT_LOAD_IMAGE);
 
                     /*
                     STUB
                      */
 
             }
         });
     }
 
     /**
      *  Allows for an image to be selected from the phone's Gallery
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data){
         super.onActivityResult(requestCode, resultCode, data);
 
         if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
             Uri selectedImage = data.getData();
 
             String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
             Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null,null,null);
             cursor.moveToFirst();
 
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             String picturePath = cursor.getString(columnIndex);
             cursor.close();
 
            ImageView myImage = (ImageView) findViewById(R.id.ImageButton);
             myImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
 
         }
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate menu resource file.
         getMenuInflater().inflate(R.menu.recipe_share, menu);
 
         // Locate MenuItem with ShareActionProvider
         MenuItem item = menu.findItem(R.id.menu_item_share);
 
         mShareActionProvider = (ShareActionProvider) item.getActionProvider();
 
 //        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
 
 
         mShareActionProvider.setShareIntent(getShareIntent());
         this.setShareIntent(getShareIntent());
 
         //Return true to display menu
         return true;
     }
 
 
     //Call to update the share intent
 
     private void setShareIntent (Intent shareIntent){
         //if(mShareActionProvider != null){
             mShareActionProvider.setShareIntent(shareIntent);
         //}
     }
     // Gets the share intent
     private Intent getShareIntent(){
         Intent myShareIntent = new Intent();
         myShareIntent.setAction(Intent.ACTION_SEND);
         myShareIntent.setType("text/plain");
         myShareIntent.putExtra(Intent.EXTRA_TEXT,"TEST");
 
        // myShareIntent.setType("image/jpeg");
         return myShareIntent;
     }
 
 
 
 }
 
 
 /**
  //        viewPager = (ViewPager) findViewById(R.id.viewPager);
  //
  //        mainLayout = (LinearLayout) findViewById(R.id.linearLayout);
  //
  //            for (int i = 0; i < images.length; i++) {
  //
  //                cell = getLayoutInflater().inflate(R.layout.cell, null);
  //
  //                ImageView imageButton = (ImageView) cell.findViewById(R.id.buttonimage);
  //                imageButton.setOnClickListener(new View.OnClickListener() {
  //
  //                    @Override
  //                    public void onClick(View v) {
  //
  //                        viewPager.setVisibility(View.VISIBLE);
  //                        viewPager.setAdapter
  //                                (new RecipePagerAdapter(AddRecipe.this, images));
  //                        viewPager.setCurrentItem(v.getId());
  //
  //
  //                        // Allows for image upload from gallery
  //                         Intent i = new Intent(
  //                         Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
  //
  //                         startActivityForResult(i, RESULT_LOAD_IMAGE);
  //                    }
  //                });
  //
  //                imageButton.setId(i);
  //
  //                text = (TextView) cell.findViewById(R.id._imageName);
  //
  //                imageButton.setImageResource(images[i]);
  //                text.setText("Image#"+(i+1));
  //
  //                mainLayout.addView(cell);
  //     }
  */
