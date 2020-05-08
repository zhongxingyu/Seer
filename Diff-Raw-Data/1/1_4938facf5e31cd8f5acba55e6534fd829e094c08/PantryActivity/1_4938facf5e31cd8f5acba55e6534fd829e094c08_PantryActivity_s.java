 package ca.dreamteam.newrecipebook;
 import java.util.List;
 
import ca.dreamteam.newrecipebook.Helpers.IngredientManipulator;
 import ca.dreamteam.newrecipebook.Helpers.IngredientSQLite;
 import ca.dreamteam.newrecipebook.Models.Ingredient;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.view.Menu;
 import android.content.Intent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.*;
 import android.widget.ArrayAdapter;
 
 public class PantryActivity extends ListActivity {
     private IngredientSQLite datasource;
     private ArrayAdapter<Ingredient> adapter;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         datasource = new IngredientSQLite(this);
         datasource.open();
         
         List<Ingredient> values = datasource.getAllIngredients();
         
         // Use the SimpleCursorAdapter to show the
         // elements in a ListView
         adapter = new ArrayAdapter<Ingredient>(this,
             android.R.layout.simple_list_item_1, values);
         setListAdapter(adapter);
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_pantry);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_pantry, menu);
         return true;
     }
     
     public void addClicked(View view){
         EditText editText = (EditText) findViewById(R.id.editText1);
         String[] ingredients = editText.getText().toString().split(System.getProperty("line.separator"));
         
         for (String ingredient: ingredients)
         {
         	Ingredient i = datasource.createIngredient(ingredient);
            	adapter.add(i);
         }
         adapter.notifyDataSetChanged();
     }
     
     @Override
     protected void onResume() {
       datasource.open();
       super.onResume();
     }
 
     @Override
     protected void onPause() {
       datasource.close();
       super.onPause();
     }
 }
