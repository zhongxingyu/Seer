 package ca.dreamteam.newrecipebook;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import ca.dreamteam.newrecipebook.Helpers.RecipeSQLite;
 import ca.dreamteam.newrecipebook.Helpers.ElasticSearch.ESClient;
 import ca.dreamteam.newrecipebook.Models.Ingredient;
 import ca.dreamteam.newrecipebook.Models.Recipe;
 
 @TargetApi(14)
 public class CreateRecipeActivity extends ListActivity {
 
 	private RecipeSQLite recipeCache = new RecipeSQLite(this);
 
	private Recipe newRecipe = new Recipe();
 	private ArrayList<String> tempIngredientList = new ArrayList<String>();
 	private ArrayAdapter<String> adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_recipe);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		//Ingredients list view.
 		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
 				tempIngredientList);
 		setListAdapter(adapter);
 
 		try {
 			//Set Author's name.
 			Cursor c = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
 			c.moveToFirst();
 			String nameString = c.getString(c.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
 
 			TextView tvTextView = (TextView)findViewById(R.id.recipeAuthor);
 			tvTextView.setText(nameString);
			c.close();
 		} catch (Exception e) {
 			//Meh.
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_create_recipe, menu);
 		return true;
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void addIngredient(View view) {
 		Intent intent = new Intent(this, AddIngredientForRecipeActivity.class);
 		startActivityForResult(intent, 1);
 		//tempIngredientList.add(ingredientName);    	
 		//adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		if (requestCode == 1) //Ensure THIS activity requested the result.
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Ingredient i = (Ingredient)data.getSerializableExtra("newingredient");
 				this.tempIngredientList.add(i.getName());
 				this.adapter.notifyDataSetChanged();
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
	public void submitRecipe(View view) {
 
 		EditText recipeNameET = (EditText)findViewById(R.id.recipeName);
 		EditText authorNameET = (EditText)findViewById(R.id.recipeAuthor);
 		EditText recipeInstructionsET = (EditText)findViewById(R.id.recipeInstructions);
 
 		String recipeName = recipeNameET.getText().toString();
 		String authorName = authorNameET.getText().toString();
 		String recipeInstructions = recipeInstructionsET.getText().toString();
 
		
		
 		newRecipe.setName(recipeName);
 		newRecipe.setAuthor(authorName);
 		newRecipe.addInstructions(recipeInstructions);
 
		/*recipeCache.open();
 		recipeCache.createRecipe(newRecipe);
		recipeCache.close();*/
 
 		//DO NOT TOUCH THIS. David's got this.
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					ESClient.getInstance().insertRecipe(newRecipe);
 				} catch (IllegalStateException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}).start();
 
 		Intent submitIntent = new Intent();
 		startActivity(submitIntent);
 
 
 	}
 
 
 
 
 
 
 
 }
