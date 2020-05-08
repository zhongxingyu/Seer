 package com.rtt_ku.pos;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.database.pos.Database;
 import com.database.pos.DatabaseController;
 import com.database.pos.DatabaseReader;
 import com.rtt_store.pos.StoreController;
 
 import Inventory.Product;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.database.sqlite.SQLiteDatabase;
 import android.view.Menu;
 import android.widget.Toast;
 
 
 public class main_activity extends Activity {
 
 //	SQLiteDatabase db;
 	DatabaseReader databaseReader;
 	ArrayList<Product> productList = new ArrayList<Product>();
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         Database myDb = new Database(this);
         myDb.getWritableDatabase();
         StoreController sCT = new StoreController(myDb);
         
         
        
         Toast.makeText(main_activity.this,"Fuck You All",
         		Toast.LENGTH_LONG).show();
 
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.pos__ui, menu);
         return true;
     }
 
     
     
 }
