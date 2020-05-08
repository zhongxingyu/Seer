 package com.cmput301w13t09.cmput301project.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 import com.cmput301w13t09.cmput301project.IngredientController;
 import com.cmput301w13t09.cmput301project.IngredientModel;
 import com.cmput301w13t09.cmput301project.R;
 import com.cmput301w13t09.cmput301project.RecipeController;
 import com.cmput301w13t09.cmput301project.RecipeModel;
 import com.cmput301w13t09.cmput301project.R.id;
 import com.cmput301w13t09.cmput301project.R.layout;
 
 /**
  * @author Kyle, Marcus, and Landre
  * 
  * Class: MyRecipesView
  * MyRecipesView is class that extends an Activity. This class shows all the recipes stored in the
  * Recipe.data file and loads this with the RecipeController and displays it in a ListView. Also, My
  * RecipesView provides a button of getting into CreateNewRecipeView where you can add recipes to RecipeList
  * 
  */
 public class MyRecipesView extends Activity {
 
 	private ListAdapter recipeListAdapter;
 	private ListView recipeListView;
 	private int dialogNumber;
 	private RecipeController recipeController;
 	private Button addRecipeButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_recipes_view);
 
 		recipeController = new RecipeController(this);
 		recipeListView = (ListView) findViewById(R.id.myRecipesList);
 		recipeListAdapter = new ArrayAdapter<RecipeModel>(this,
 				android.R.layout.simple_list_item_1,
 				recipeController.getRecipeList());
 
 		recipeListView.setAdapter(recipeListAdapter);
 
 		addRecipeButton = (Button) findViewById(R.id.createRecipeButton);
 
 		recipeListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				dialogNumber = position;
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						MyRecipesView.this);
 				String title = recipeController.getRecipeListName(position);
 				String message = recipeController.getRecipeList().get(position).getRecipeDesc();
 				builder.setMessage(message);
 				builder.setTitle(title);
 
 				builder.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.dismiss();
 
 							}
 						});
 				builder.setNeutralButton("Edit",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.dismiss();
 
 							}
 						});
 				builder.setPositiveButton("Delete",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								recipeController.remove(dialogNumber);
 								dialog.dismiss();
 								updateList();
 
 							}
 						});
 
 				AlertDialog dialog = builder.create();
 				dialog.show();
 			}
 		});
 
 		addRecipeButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				try {
 					Intent addNewRecipe = new Intent(
 							"activities.CreateNewRecipe");
 					startActivity(addNewRecipe);
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
 
 			}
 		});
 
 	}
 
 	protected void updateList() {
 		recipeController.loadFromFile();
 		recipeListAdapter = new ArrayAdapter<RecipeModel>(this,
 				android.R.layout.simple_list_item_1,
 				recipeController.getRecipeList());
 		recipeListView.setAdapter(recipeListAdapter);
 	}
 	protected void onPause() {
 		super.onPause();
 		recipeController.saveToFile();
 	}
 
 	protected void onResume() {
 		super.onResume();
		recipeController.loadFromFile();
 		updateList();
 	}
 
 }
