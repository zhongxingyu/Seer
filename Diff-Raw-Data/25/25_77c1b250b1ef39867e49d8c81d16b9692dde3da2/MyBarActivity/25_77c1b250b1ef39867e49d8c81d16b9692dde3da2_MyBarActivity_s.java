 package se.turbotorsk.mybar;
 
 import se.turbotorsk.mybar.model.Data;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 public class MyBarActivity extends ListActivity {
 
 	IngredientAdapter adapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);

 		
 /*		Data data = new Data();
 		adapter = new IngredientAdapter(this, R.layout.rowlayout, data.getAllIngredients());
 
		// Sets the adapter that we just did
 		setListAdapter(adapter);
*/
 	}
	
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
 
 }
