 package ca.dreamteam.newrecipebook;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import ca.dreamteam.newrecipebook.Helpers.IngredientDatabaseHelper;
 import ca.dreamteam.newrecipebook.Models.Ingredient;
 
 /**
  * Allows the user to input a String value of a ingredient name and then 
  * on click that String is then added to the ingredient list and the date
  * is updated accordingly.
  * 
  * @version RecipeBook Project 4
  * @author Conner Bilec, David James, Steve Eckert and Maciej Ogrocki
  * @date Monday 01 April 2013
  */
 
 public class AddIngredientForRecipeActivity extends ListActivity{
 	
 	/**
 	 * @var ingredient: Keeps track of the current ingredient being modified. 
 	 * @var ingredientList: Arraylist of Strings of the added ingredient.  
 	 * @var adapter: ArrayAdapter used to move the String ingredients into the database
 	 */
 	private Ingredient ingredient = null;
 	private ArrayList<String> ingredientList = new ArrayList<String>();
 	private ArrayAdapter<String> adapter;
 	private ListView listView; 
 	
 	@Override	
 	/**
 	 * As the page is created causes the add button to make the file add the current
 	 * ingredient file into the ingredientList if something has been entered into the
 	 * text box. If nothing was entered nothing will happen. Returns back to previous 
 	 * page if the "Done Adding" Button is pressed. 
 	 * 
 	 * @param savedInstanceState
 	 */
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_ingredient_view);
         ArrayList<String> passedIngredients = new ArrayList<String>();
         passedIngredients = getIntent().getExtras().getStringArrayList("alreadyAddedIngredients");
     	if (!passedIngredients.isEmpty())
     		ingredientList = passedIngredients;
         listView = (ListView)findViewById(android.R.id.list);
         adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                 ingredientList);
         listView.setAdapter(adapter);
        TextView deletions = (TextView)findViewById(R.id.ingredientDeleteInstructions);
        deletions.setVisibility(View.GONE);
  
         //ingredient = (Ingredient)getIntent().getSerializableExtra("ingredient");
         
         if (ingredient == null)
         {
 			Button buttonView = (Button)findViewById(R.id.ingredientAdd_multiPurposeButton);
 			buttonView.setText("Add to Recipe");	
 			buttonView.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					EditText iNameET = (EditText) findViewById(R.id.ingredientAdd_nameEdit);
 					String ingredientName = iNameET.getText().toString();
 					
 					if(!ingredientName.equals(""))
 					{
 						ingredientList.add(ingredientName);
 						iNameET.setText("");
 						adapter.notifyDataSetChanged();
 						/*Intent intent = new Intent();
 						intent.putExtra("newingredient", new Ingredient(iName.getText().toString()));
 						setResult(RESULT_OK, intent);
 						finish();*/
 					}
 				}
 			});
 			
 			buttonView = (Button)findViewById(R.id.ingredientDelete_multiPurposeButton);
 			buttonView.setText("Done Adding");
 			buttonView.setOnClickListener(new View.OnClickListener(){
 			    
 			    @Override
 			    public void onClick(View v) {
 			    	Intent intent = new Intent();
 					intent.putStringArrayListExtra("newIngredients", ingredientList);
 					setResult(RESULT_OK, intent);
 					finish();
 			    }
 			});
         }
 		
         else {
         	this.ingredient = (Ingredient)getIntent().getSerializableExtra("ingredient");
 			((EditText)findViewById(R.id.ingredientAdd_nameEdit)).setText(this.ingredient.getName());
 		}
     }
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		//Ingredient i = (Ingredient)data.getSerializableExtra("newingredient");
 		this.ingredientList = (ArrayList<String>)data.getStringArrayListExtra("includedIngredients");
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		ingredientList.remove(position);
 		adapter.notifyDataSetChanged();
 	}
 }
