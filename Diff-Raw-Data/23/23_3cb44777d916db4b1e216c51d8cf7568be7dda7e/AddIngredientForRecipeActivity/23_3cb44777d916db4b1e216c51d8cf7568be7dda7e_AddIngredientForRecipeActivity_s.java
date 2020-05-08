 package ca.dreamteam.newrecipebook;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
import ca.dreamteam.newrecipebook.Helpers.IngredientDatabaseHelper;
 import ca.dreamteam.newrecipebook.Models.Ingredient;
 
 public class AddIngredientForRecipeActivity extends Activity{
 	private Ingredient ingredient = null;
 	
 	@Override	
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_ingredient_view);
  
         ingredient = (Ingredient)getIntent().getSerializableExtra("ingredient");
         
         if (ingredient == null)
         {
 			Button buttonView = (Button)findViewById(R.id.ingredientAdd_multiPurposeButton);
 			buttonView.setText("Add to Recipe");	
 			buttonView.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					EditText iName = (EditText) findViewById(R.id.ingredientAdd_nameEdit);
 					
 					if(!iName.getText().toString().equals(""))
 					{
 						Intent intent = new Intent();
 						intent.putExtra("newingredient", new Ingredient(iName.getText().toString()));
 						setResult(RESULT_OK, intent);
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
 }
