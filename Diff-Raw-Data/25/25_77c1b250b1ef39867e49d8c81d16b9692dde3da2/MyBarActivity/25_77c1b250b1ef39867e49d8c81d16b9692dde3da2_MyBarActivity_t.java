 package se.turbotorsk.mybar;
 
 import se.turbotorsk.mybar.model.Data;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.widget.Toast;
 
 public class MyBarActivity extends ListActivity {
 
 	IngredientAdapter adapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
		//Temp solution below...
		String[] ingredients = this.getResources().getStringArray(R.array.ingredient_list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 	android.R.layout.simple_list_item_1, ingredients);
 /*		Data data = new Data();
 		adapter = new IngredientAdapter(this, R.layout.rowlayout, data.getAllIngredients());
 
*/		// Sets the adapter that we just did
 		setListAdapter(adapter);

 	}
	//Enable below after created new ingredient activity with getintents.
 	/*@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// String item = (String) getListAdapter().getItem(position);
 		// We will replace this to start another activity instead. Dont know how
 		// to do that yet.
 		// Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
 
 		Intent intent = new Intent(this, View_Drink_Activity.class);
 		intent.putExtra("ingredientname", adapter.getIngredientName(position));
 		intent.putExtra("ingredientdescrip", adapter.getDescription(position));
 		startActivity(intent);
 	}*/
	//Temp solution below
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	  String item = (String) getListAdapter().getItem(position);
	  Toast.makeText(this, "Deleted " + item, Toast.LENGTH_LONG).show();
	}
 
 }
