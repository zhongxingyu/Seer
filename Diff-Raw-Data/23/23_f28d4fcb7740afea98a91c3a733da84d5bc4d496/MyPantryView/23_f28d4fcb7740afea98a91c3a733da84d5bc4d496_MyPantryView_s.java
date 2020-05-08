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
 
 public class MyPantryView extends Activity {
 
 	private ListAdapter ingredientListAdapter;
 	private ListView ingredientListView;
 	private int dialogNumber;
 	private IngredientController ingredientController;
 	private Button addIngredientButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_pantry_view);
 
 		ingredientController = new IngredientController(this);
		ingredientController.loadFromFile();
 
 		addIngredientButton = (Button) findViewById(R.id.myPantryAddIngredientButton);
 
//		 TODO Remove this stuff it's garbage
		 ingredientController.add(new IngredientModel("Cat", "Smells bad"));
		 ingredientController.add(new IngredientModel("Fish", "Eats fish"));
		 ingredientController.add(new IngredientModel("CatFish",
		 "Smells bad and eats fish"));
 		ingredientController.saveToFile();
 
 		ingredientListView = (ListView) findViewById(R.id.myPantryIngredientList);
 		ingredientListAdapter = new ArrayAdapter<IngredientModel>(this,
 				android.R.layout.simple_list_item_1,
 				ingredientController.getIngredientList());
 		ingredientListView.setAdapter(ingredientListAdapter);
 
 		ingredientListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				dialogNumber = position;
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						MyPantryView.this);
 				String title = ingredientController
 						.getIngredientListName(position);
 				String message = ingredientController.getIngredient(position)
 						.toDialogString();
 				builder.setMessage(message);
 				builder.setTitle(title);
 
 				builder.setNegativeButton("Cancle",
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
 								ingredientController.remove(dialogNumber);
 								dialog.dismiss();
 								updateList();
 
 							}
 						});
 
 				AlertDialog dialog = builder.create();
 				dialog.show();
 			}
 		});
 
 		addIngredientButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				try {
 					Intent addIngredient = new Intent(
 							"activities.AddIngredient");
 					startActivity(addIngredient);
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 	}
 
 	protected void updateList() {
 		ingredientListAdapter = new ArrayAdapter<IngredientModel>(this,
 				android.R.layout.simple_list_item_1,
 				ingredientController.getIngredientList());
 		ingredientListView.setAdapter(ingredientListAdapter);
 	}
 }
