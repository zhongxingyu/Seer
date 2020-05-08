 package com.cmput301.recipebot.ui;
 
 
 //import android.R;
 
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.provider.MediaStore;
 import android.widget.ImageButton;
 import com.cmput301.recipebot.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Button;
 import android.net.Uri;
 import android.database.Cursor;
 import android.widget.ShareActionProvider;
 import android.view.MenuItem;
 import android.view.Menu;
 
 /*
  *     btrinh
  *
  *     For Ethan/Adam
  *     If you guys are looking for the stubs to add your work simply Ctrl+F and search for STUB
  *
  *     ToDo:
  *     Horizontal Scroll On Image Button
  *     Multiple Images
 *     Context Menus for clicks of ImageClick
  *     Save Author, Date
  *
  */
 public class AddRecipe extends Activity {
 
     private static int RESULT_LOAD_IMAGE = 1;
     private ShareActionProvider mShareActionProvider;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.add_recipe);
 
         //Buttons
         Button publishButton = (Button) findViewById(R.id.PublishButton);
         Button saveButton = (Button) findViewById(R.id.SaveButton);
        Button imageButton = (Button) findViewById(R.id.ImageButton);
 
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
          */
         publishButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //To change body of implemented methods use File | Settings | File Templates.
 
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
 
                     /*
                     STUB
                      */
 
             }
         });
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
 
             ImageButton myImage = (ImageButton) findViewById(R.id.ImageButton);
             myImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
 
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate menu resource file.
         getMenuInflater().inflate(R.menu.recipe_share, menu);
 
         // Locate MenuItem with ShareActionProvider
         MenuItem item = menu.findItem(R.id.menu_item_share);
 
         // Fetch and store ShareActionProvider
         mShareActionProvider = (ShareActionProvider) item.getActionProvider();
 
         //Return true to display menu
         return true;
     }
 
     //Call to update the share intent
     private void setShareIntent (Intent shareIntent){
         if(mShareActionProvider != null){
             mShareActionProvider.setShareIntent(shareIntent);
         }
     }
 
 
 }
