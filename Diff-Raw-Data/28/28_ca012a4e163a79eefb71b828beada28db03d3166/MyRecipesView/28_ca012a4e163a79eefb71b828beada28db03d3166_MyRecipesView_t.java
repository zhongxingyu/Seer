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
 
 		recipeController = new RecipeController();
 		
		recipeController.addRecipe(new RecipeModel("Cat Stew", "Cat + stewpot + water", 
				new IngredientController().add(new IngredientModel("cat", "Eats Fish")).getIngredientList()));

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
 				AlertDialog.Builder builder = new AlertDialog.Builder(MyRecipesView.this);
 				String title = recipeController.getRecipeListName(position);
 				String message = "Needs to be fixed ";//recipeController.getRecipe(position).toDialogString();
 				builder.setMessage(message);
 				builder.setTitle(title);
 
 				builder.setNegativeButton("Cancle",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 
 							}
 						});
 				builder.setNeutralButton("Edit",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 
 							}
 						});
 				builder.setPositiveButton("Delete",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
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
					Intent addNewRecipe = new Intent("activities.CreateNewRecipe"); 
					startActivity(addNewRecipe);
				} catch (Throwable e){
					e.printStackTrace();
				}
				
			}
		});
		
 	}

 	protected void updateList() {
 		recipeListAdapter = new ArrayAdapter<RecipeModel>(this,
 				android.R.layout.simple_list_item_1,
 				recipeController.getRecipeList());
 		recipeListView.setAdapter(recipeListAdapter);
 	}
 }
