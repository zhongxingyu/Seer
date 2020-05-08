 /*
  * ShoppingList for Android
  * (C)2011 by Christian Lønaas
  *    <christian dot lonaas at discombobulator dot org>
  *
  * This file is part of ShoppingList.
  *
  * ShoppingList is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ShoppingList is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with ShoppingList.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package no.opentech.shoppinglist.activities;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import no.opentech.shoppinglist.R;
 import no.opentech.shoppinglist.ShoppingListApp;
 import no.opentech.shoppinglist.adapters.ShoppingListAdapter;
 import no.opentech.shoppinglist.entities.Item;
 import no.opentech.shoppinglist.models.ShoppingListModel;
 import no.opentech.shoppinglist.utils.Logger;
 
 import java.util.ArrayList;
 
 /**
  * Created by: Christian Lønaas
  * Date: 11.12.11
  * Time: 01:05
  */
 @SuppressWarnings("unchecked")
 public class ShoppingListActivity extends ListActivity {
     private Context context = ShoppingListApp.getContext();
     private SensorManager mSensorManager;
     private float mAccel; // acceleration apart from gravity
     private float mAccelCurrent; // current acceleration including gravity
     private float mAccelLast; // last acceleration including gravity
     private ShoppingListModel model;
     private ArrayList<Item> visibleItems;
     private static Logger log = Logger.getLogger(ShoppingListActivity.class);
     private static final int ITEMLISTACTIVITY = 98;
     private int numInLine;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
         mAccel = 0.00f;
         mAccelCurrent = SensorManager.GRAVITY_EARTH;
         mAccelLast = SensorManager.GRAVITY_EARTH;
         
         long id = getIntent().getLongExtra("shoppingListId", 0);
         model = new ShoppingListModel(id);
         numInLine = 0;
         setTitle(model.getName());
         visibleItems = new ArrayList<Item>();
         visibleItems.addAll(model.getShoppingListItems());
         ShoppingListAdapter adapter = new ShoppingListAdapter(context, R.layout.list_item, visibleItems);
         setListAdapter(adapter);
         model.setAdapter(adapter);
         ListView lv = getListView();
         lv.setBackgroundResource(R.drawable.paper);
        lv.setCacheColorHint(Color.parseColor("#00000000")); // transparent, to fix scrolling bug
         registerForContextMenu(lv);
         lv.setTextFilterEnabled(true);
 
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Item selectedItem = visibleItems.get(position);
                 if(!selectedItem.isChecked()) { // this could be better, but OK for now..
                     numInLine++;
                     selectedItem.setChecked(true);
                     selectedItem.setNumberInLine(numInLine);
                 } else {
                     selectedItem.setChecked(false);
                     selectedItem.setNumberInLine(0);
                     numInLine--;
                 }
                 update();
             }
         });        
     }
 
     public void update() {
         ((ShoppingListAdapter)getListAdapter()).notifyDataSetChanged();
     }
 
     @Override
     public void onBackPressed() {
         Intent resultIntent = new Intent();
         if(model.allItemsChecked()) {
             model.updateNumbersAndDeleteList();
             log.debug("Shoppinglist '" + model.getName() + "' finished, removing.");
             setResult(Activity.RESULT_OK, resultIntent);
         } else {
             model.save();
             log.debug("Shoppinglist '" + model.getName() + " cancelled.");
             setResult(Activity.RESULT_CANCELED, resultIntent);
         }
         this.finish();
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.menu.shoppinglist_context_menu, menu);
         menu.setHeaderTitle(R.string.context_menu_title);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         Item selectedItem = visibleItems.get(info.position);
 
         switch(item.getItemId()) {
             case R.id.showshoppinglistitemdetails:
                 Intent intent = new Intent(this, ItemDetailsActivity.class);
                 intent.putExtra("itemId", selectedItem.getId());
                 this.startActivity(intent);
                 break;
             case R.id.removefromshoppinglist:
                 visibleItems.remove(selectedItem);
                 model.removeItem(selectedItem);
                 update();
                 break;
         }
         return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.menu.shoppinglist_options, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.additem:
                 addItems();
                 break;
         }
         return true;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case ITEMLISTACTIVITY:
                 if (resultCode == Activity.RESULT_OK) {
                     log.debug("Returned from ItemListActivity");
                     model.refresh();
                     visibleItems.clear();
                     visibleItems.addAll(model.getShoppingListItems());
                     update();
                 }
                 break;
         }
     }
 
     public void addItems() {
         Intent intent = new Intent(this, ItemListActivity.class);
         intent.putExtra("shoppingListId", model.getId());
         this.startActivityForResult(intent, ITEMLISTACTIVITY);
         update();
     }
 
     private final SensorEventListener mSensorListener = new SensorEventListener() {
 
         public void onSensorChanged(SensorEvent se) {
             float x = se.values[0];
             float y = se.values[1];
             float z = se.values[2];
             mAccelLast = mAccelCurrent;
             mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
             float delta = mAccelCurrent - mAccelLast;
             mAccel = mAccel * 0.9f + delta; // perform low-cut filter
             
             if(mAccel > 5) {
                 if(model.allItemsChecked()) {
                     Intent resultIntent = new Intent();
                     model.updateNumbersAndDeleteList();
                     setResult(Activity.RESULT_OK, resultIntent);
                     finish();
                 } else {
                     model.hideCheckedItems();
                     visibleItems.removeAll(model.getHiddenShoppingListItems());
                     update();
                 }
             }
         }
 
         public void onAccuracyChanged(Sensor sensor, int accuracy) {
         }
     };
 
     @Override
     protected void onResume() {
         super.onResume();
         mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
     }
 
     @Override
     protected void onStop() {
         mSensorManager.unregisterListener(mSensorListener);
         super.onStop();
     }
 }
