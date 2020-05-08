 package com.sparc.BoozeChoose.Activity;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.widget.ListView;
 import com.sparc.BoozeChoose.Adapter.DrinkAdapter;
 import com.sparc.BoozeChoose.Adapter.IngredientAdapter;
 import com.sparc.BoozeChoose.Model.Drink;
 import com.sparc.BoozeChoose.Model.Ingredient;
 import com.sparc.BoozeChoose.R;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Whitney Champion
  * Date: 8/23/13
  * Time: 11:01 PM
  * Description:
  */
 public class ListDrinks extends BoozeChoose {
 
 
     public Drink[] drinkData;
     public DrinkAdapter adapter;
     public ListView drinks;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.ingredient_list);
 
         Bundle extras = getIntent().getExtras();
         List<Drink> drinks = null;
         if (extras.getParcelableArrayList("drinks") != null) {
            drinks = extras.getParcelableArrayList("drinks");
         }
         // populate drinks
 
         if (!(drinks.size() < 1)) {
 
             drinkData = drinks.toArray(new Drink[drinks.size()]);
 
             adapter = new DrinkAdapter(this, ListDrinks.this, R.layout.drink_list_item, drinkData);
 
             this.drinks = (ListView) findViewById(R.id.drinkListView);
 
             this.drinks.setAdapter(adapter);
         }
     }
 
 
 
 }
