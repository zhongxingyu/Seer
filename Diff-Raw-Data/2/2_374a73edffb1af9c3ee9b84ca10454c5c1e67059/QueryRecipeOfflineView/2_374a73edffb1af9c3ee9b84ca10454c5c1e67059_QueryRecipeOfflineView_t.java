 package com.cmput301w13t09.cmput301project.activities;
 
 import com.cmput301w13t09.cmput301project.R;
 import com.cmput301w13t09.cmput301project.controllers.IngredientController;
 import com.cmput301w13t09.cmput301project.controllers.RecipeController;
 import com.cmput301w13t09.cmput301project.models.RecipeListModel;
 import com.cmput301w13t09.cmput301project.models.RecipeModel;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class QueryRecipeOfflineView extends Activity {
 	private ListAdapter recipeListAdapter;
 	private ListView recipeListView;
 	private IngredientController ingredController;
 	private int dialogNumber;
 	private RecipeListModel QuertRecipeList;
 	private RecipeController recipeController;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_query_recipe_offline_view);
 		recipeController = new RecipeController(this);
 		ingredController = new IngredientController(this);
 		QuertRecipeList = recipeController.getQueryRecipeList(ingredController);
 		recipeListView = (ListView) findViewById(R.id.queryRecipeOfflinelistView);
 		recipeListAdapter = new ArrayAdapter<RecipeModel>(this,
 				android.R.layout.simple_list_item_1, QuertRecipeList);
 
 		recipeListView.setAdapter(recipeListAdapter);
 		recipeListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				dialogNumber = position;
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						QueryRecipeOfflineView.this);
 				String title = QuertRecipeList.get(position).getRecipeName();
 				String message = QuertRecipeList.get(position).getRecipeDesc();
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
 				builder.setNeutralButton("View",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								try {
 									Intent viewRecipe = new Intent(
											"activities.ViewRecipe");
 									viewRecipe.putExtra("Recipe",
 											QuertRecipeList.get(dialogNumber));
 									startActivity(viewRecipe);
 								} catch (Throwable throwable) {
 									throwable.printStackTrace();
 								}
 								dialog.dismiss();
 
 							}
 						});
 				builder.setPositiveButton("Delete",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								recipeController.remove(recipeController
 										.findRecipe(QuertRecipeList.get(
 												dialogNumber).getRecipeName()));
 								recipeController.saveToFile();
 								dialog.dismiss();
 								updateList();
 
 							}
 						});
 				AlertDialog dialog = builder.create();
 				dialog.show();
 			}
 		});
 	}
 
 	protected void updateList() {
 		recipeController.loadFromFile();
 		QuertRecipeList = recipeController.getQueryRecipeList(ingredController);
 		recipeListAdapter = new ArrayAdapter<RecipeModel>(this,
 				android.R.layout.simple_list_item_1, QuertRecipeList);
 		recipeListView.setAdapter(recipeListAdapter);
 	}
 
 }
