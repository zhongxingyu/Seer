 package se.kth.csc.iprog.dinnerplanner;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import se.kth.csc.iprog.dinnerplanner.model.DinnerModel;
 import se.kth.csc.iprog.dinnerplanner.model.Ingredient;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class Ingredients extends Activity {
 	
 	ListView ingredients;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_ingredients);
 		DinnerModel model = ((DinnerPlannerApplication) this.getApplication()).getModel();
 		ingredients = (ListView) findViewById(R.id.listingredients);
 		
 		Set<Ingredient> ingredient = new HashSet<Ingredient>();
 		ingredient = model.getAllIngredients();
 		ArrayList<String> ingredientArray = new ArrayList<String>();
 		for(Ingredient d : ingredient){
<<<<<<< HEAD
 			ingredientArray.add(d.getQuantity()*model.getNumberOfGuests()+" "+d.getUnit()+" "+d.getName());
=======
			ingredientArray.add(d.getQuantity()*model.getNumberOfGuests()+d.getUnit()+" "+d.getName());
>>>>>>> Anooying
 		}
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, ingredientArray);
    
 		ingredients.setAdapter(adapter);
 		
 
 	}
 
 	
 	
 	
 
 
 }
 
