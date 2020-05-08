 package com.github.cmput301w13t04.food.view;
 
 import java.io.File;
 
 import com.github.cmput301w13t04.food.R;
 import com.github.cmput301w13t04.food.controller.Cache;
 import com.github.cmput301w13t04.food.model.Ingredient;
 import com.github.cmput301w13t04.food.model.Photo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * View an ingredient and all of its properties
  * 
  * @author W13T04
  * 
  */
 public class ActivityViewIngredient extends Activity {
 
 	private Ingredient ingredient;
 	private int position;
 
 	private static final int ACTION_MANAGE_INGREDIENT = 1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_ingredient);
 
 		// Get Ingredient ID
 		position = getIntent().getIntExtra("POSITION", -1);
 		ingredient = getIntent().getParcelableExtra("INGREDIENT");
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		// Populate Current Ingredient
 		if (ingredient == null) {
 
			Toast.makeText(getApplicationContext(),
					"Sorry Ingredient not found", Toast.LENGTH_SHORT).show();
 			finish();
 
 		} else {
 
 			ImageView photoView = (ImageView) findViewById(R.id.ingredient_image);
 			if (ingredient.getPhoto() != null) {
 				photoView.setImageURI(Uri
 						.parse(ingredient.getPhoto().getPath()));
 			}
 
 			TextView quantity = (TextView) findViewById(R.id.ingQuantity);
 			if (quantity != null) {
 				quantity.setText(String.valueOf(ingredient.getQuantity()));
 			}
 
 			TextView name = (TextView) findViewById(R.id.ingName);
 			if (name != null) {
 				name.setText(ingredient.getName());
 			}
 
 			TextView description = (TextView) findViewById(R.id.ingDescription);
 			if (name != null) {
 				description.setText(ingredient.getDescription());
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_ingredient, menu);
 		return true;
 	}
 
 	/**
 	 * The button listener for editing Ingredients
 	 */
 	public void editIngredient(View view) {
 		Intent intent = new Intent(ActivityViewIngredient.this,
 				ActivityManageIngredient.class);
 
 		intent.putExtra("INGREDIENT", ingredient);
 		intent.putExtra("POSITION", position);
 
 		startActivityForResult(intent, ACTION_MANAGE_INGREDIENT);
 	}
 
 	/**
 	 * The button listener for deleting ingredients
 	 */
 	public void deleteIngredient(View view) {
 		Cache cache = new Cache();
 		cache.load(this);
 
 		Photo p = cache.getIngredient(position).getPhoto();
		if (p != null) {
			File file = new File(p.getAbsolutePath());
 
			if (file.exists())
				file.delete();
 
			// clear devices cache
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory())));
		}
 		cache.removeIngredient(position);
 		cache.save(this);
 
 		Toast.makeText(getApplicationContext(), "Ingredient entry deleted!",
 				Toast.LENGTH_SHORT).show();
 
 		finish();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ACTION_MANAGE_INGREDIENT: {
 			if (resultCode == RESULT_OK) {
 				// Update Ingredient
 				Cache cache = new Cache();
 				cache.load(this);
 
 				position = data.getIntExtra("POSITION", -1);
 				ingredient = data.getParcelableExtra("INGREDIENT");
 
 				if (position != -1) {
 					cache.updateIngredient(ingredient, position);
 					cache.save(this);
				}
 			} else if (resultCode == ActivityManageIngredient.RESULT_DELETE) {
 				// Remove Ingredient
 				Cache cache = new Cache();
 				cache.load(this);
 
 				position = data.getIntExtra("POSITION", -1);
 
 				if (position != -1) {
 					cache.removeIngredient(position);
 				}
 
 				cache.save(this);
 
 				// Go back to recipe list
 				finish();
 			}
 		}
 		}
 		;
 	}
 }
