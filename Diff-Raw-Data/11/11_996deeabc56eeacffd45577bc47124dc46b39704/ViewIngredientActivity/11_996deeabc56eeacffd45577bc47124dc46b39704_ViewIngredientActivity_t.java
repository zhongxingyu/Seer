 package ca.dreamteam.newrecipebook;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.TextView;
 import ca.dreamteam.newrecipebook.Helpers.IngredientDatabaseHelper;
 import ca.dreamteam.newrecipebook.Models.Ingredient;
 
 /**
  * Allows the user to view the ingredients currently saved in his or her 
  * pantry as well as on button click allows the user to add ingredients to his 
  * panty
  * 
  * @version RecipeBook Project 4
  * @author Conner Bilec, David James, Steve Eckert and Maciej Ogrocki
  * @date Monday 01 April 2013
  */
 
 public class ViewIngredientActivity extends Activity {
 	/**
 	 * @var db Database connection helper
 	 * @var ingredient temp ingredient object defults to null
 	 */
 	private IngredientDatabaseHelper db = new IngredientDatabaseHelper(this);
 	private Ingredient ingredient = null;
 	
 	/**
 	 * Allows the user to view the ingredients as well as waits for the user to press a button 
 	 * at which point it will add the temp ingredients to the database
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_ingredient_view);
        TextView deletions = (TextView)findViewById(R.id.ingredientDeleteInstructions);
        deletions.setVisibility(View.GONE);
  
         ingredient = (Ingredient)getIntent().getSerializableExtra("ingredient");
         
         if (ingredient == null)
         {
 			Button buttonView = (Button)findViewById(R.id.ingredientAdd_multiPurposeButton);
 			buttonView.setText("Add to Pantry");	
 			buttonView.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					EditText iName = (EditText) findViewById(R.id.ingredientAdd_nameEdit);
 					
 					if(!iName.getText().toString().equals(""))
 					{
 						IngredientDatabaseHelper datasource = new IngredientDatabaseHelper(getApplicationContext());
 						datasource.createIngredient(iName.getText().toString());
 					
 						finish();
 					}
 				}
 			});
 			
 			buttonView = (Button)findViewById(R.id.ingredientDelete_multiPurposeButton);
 			buttonView.setText("Cancel");
 			buttonView.setOnClickListener(new View.OnClickListener(){
 			    
 			    @Override
 			    public void onClick(View v) {
 			        finish();
 			    }
 			});
         }
 		
         else {
         	this.ingredient = (Ingredient)getIntent().getSerializableExtra("ingredient");
 			((EditText)findViewById(R.id.ingredientAdd_nameEdit)).setText(this.ingredient.getName());
 		}
     }
 	/**
 	 * opens the database when page is resumed 
 	 */
 	@Override
 	public void onResume()
 	{
 		db.open();
 		super.onResume();
 	}
 	/**
 	 * Closes the database when the use leaves the page
 	 */
 	@Override
 	public void onPause()
 	{
 		db.close();
 		super.onPause();
 	}
 	/**
 	 * deletes the ingredient from the database
 	 * 
 	 * @param view
 	 */
 	public void deleteIngredient(View view)
 	{
 		db.deleteIngredient(this.ingredient);
 		super.finish();
 	}
 	/**
 	 * Saves ingredient to the database
 	 * @param view
 	 */
 	public void saveIngredient(View view)
 	{
 		this.ingredient.setIngredient(((EditText)findViewById(R.id.ingredientAdd_nameEdit)).getText().toString());
 		db.updateIngredient(this.ingredient);
 		super.finish();
 	}
 }
