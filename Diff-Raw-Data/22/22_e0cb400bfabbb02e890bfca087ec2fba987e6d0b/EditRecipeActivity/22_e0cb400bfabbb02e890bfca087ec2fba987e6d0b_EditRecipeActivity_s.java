 package ca.ualberta.team2recipefinder;
 
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class EditRecipeActivity extends Activity {
 
 	EditText nameEdit = (EditText) findViewById(R.id.edit_name);;
 	EditText procedureEdit = (EditText) findViewById(R.id.edit_procedure);
 	Recipe currentRecipe = new Recipe();
 	int recipeIndex = -1;
 	ListView ingredientList = (ListView) findViewById(R.id.ingredient_list);
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_recipe);
 		
 		Controller c = RecipeFinderApplication.getController();
 		
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			if (extras != null) {
 				recipeIndex = extras.getInt("recipeIndex");
 				currentRecipe = c.getRecipe(recipeIndex);
 			}
 		}
 		
 		nameEdit.setText(currentRecipe.getName());
 		procedureEdit.setText(currentRecipe.getProcedure());
 		
 		List<Ingredient> ingredients =currentRecipe.getIngredients();
		final ArrayAdapter<Recipe> adapter = new ArrayAdapter<Recipe>(this, R.layout.list_item, ingredients);
 		ingredientList.setAdapter(adapter);
 		
 		Button doneButton = (Button) findViewById(R.id.button_done);
 		doneButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				currentRecipe.setName(nameEdit.getText().toString());
 				currentRecipe.setProcedure(procedureEdit.getText().toString());
 				
 				Controller c = RecipeFinderApplication.getController();
 				if (recipeIndex == -1) {
 					c.addRecipe(currentRecipe);
 				}
 				else {
 					c.replaceRecipe(currentRecipe, recipeIndex);
 				}
 			}		
 		});
 	}
 
 
 }
