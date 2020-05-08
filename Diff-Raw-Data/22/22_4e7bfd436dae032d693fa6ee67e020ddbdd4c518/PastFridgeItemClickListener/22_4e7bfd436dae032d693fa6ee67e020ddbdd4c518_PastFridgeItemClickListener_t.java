 package com.openfridge;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class PastFridgeItemClickListener implements OnItemClickListener {
     public void onItemClick(AdapterView<?> parent, View view, int position, 
                             long id) {
         Log.d("Fridge debug", "list item clicked");
         if (parent.getClass() == ListView.class) {
             ListView parentList = (ListView) parent;
             
            FridgeFood food = (FridgeFood) parentList.getItemAtPosition(position);
            Toast.makeText(parentList.getContext(), 
                    "Expiration Date: " + food.getExpirationDateString(), 
                    Toast.LENGTH_SHORT).show();
             
         }
     }
 
 }
