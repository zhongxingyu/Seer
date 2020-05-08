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
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.graphics.Color;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.*;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import no.opentech.shoppinglist.R;
 import no.opentech.shoppinglist.ShoppingListApp;
 import no.opentech.shoppinglist.adapters.ViewShoppingListAdapter;
 import no.opentech.shoppinglist.entities.ShoppingList;
 import no.opentech.shoppinglist.models.ViewShoppingListsModel;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 /**
  * Created by: Christian Lønaas
  * Date: 08.04.11
  * Time: 21:34
  */
 public class ViewShoppingListsActivity extends ListActivity {
     private Context context = ShoppingListApp.getContext();
     private ViewShoppingListsModel model;
     private static final int ITEMLISTACTIVITY = 98;
     private static final int SHOPPINGLISTACTIVITY = 99;
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitle("Shopping lists");
         model = new ViewShoppingListsModel();
 
         setListAdapter(new ViewShoppingListAdapter(context, R.layout.list_shoppinglists, model.getShoppingLists()));
         ListView lv = getListView();
         registerForContextMenu(lv);
         lv.setTextFilterEnabled(true);
         lv.setBackgroundResource(R.drawable.paper);
        lv.setCacheColorHint(Color.parseColor("#00000000")); // transparent, to fix scrolling bug
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 ShoppingList selectedItem = model.getShoppingList(position);
                 long shoppingListId = selectedItem.getId();
                 if (!selectedItem.isDefaultList()) {
                     openShoppingList(shoppingListId);
                 } else {
                     createNewShoppingList();
                 }
                 update();
             }
         });
     }
 
     public void update() {
         ((ViewShoppingListAdapter) getListAdapter()).notifyDataSetChanged();
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case ITEMLISTACTIVITY:
                 if (resultCode == Activity.RESULT_CANCELED) {
                     Toast.makeText(context, "Canceled..", Toast.LENGTH_SHORT).show();
                     long id = data.getLongExtra("shoppingListId", 0);
                     model.deleteShoppingListById(id);
                     update();
                 } else {
                     update();
                 }
                 break;
             case SHOPPINGLISTACTIVITY:
                 if(resultCode == Activity.RESULT_OK) {
                     Toast.makeText(context, "Finished, removing list", Toast.LENGTH_SHORT).show();
                     model.refresh();
                     update();
                 } else {
                     update();
                 }
                 break;
         }
     }
 
     /* create the menu */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.menu.shoppinglists_options, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.newlist:
                 createNewShoppingList();
                 break;
             case R.id.manageitems:
                 manageItems();
                 break;
         }
         return true;
     }
 
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.menu.viewshoppinglists_context_menu, menu);
         menu.setHeaderTitle(R.string.context_menu_title);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         ShoppingList selectedItem = model.getShoppingList(info.position);
         
         switch(item.getItemId()) {
             case R.id.viewshoppinglistdetails:
                 Toast.makeText(context, "Not implemented yet..", Toast.LENGTH_SHORT).show();
                 break;
             case R.id.deleteshoppinglist:
                 if(selectedItem.isDefaultList())
                     Toast.makeText(context, "Can't delete this", Toast.LENGTH_SHORT).show();
                 else
                     deleteShoppingList(selectedItem);
                 break;
         }
         return true;
     }
 
     public void openShoppingList(long id) {
         Intent intent = new Intent(this, ShoppingListActivity.class);
         intent.putExtra("shoppingListId", id);
         this.startActivityForResult(intent, SHOPPINGLISTACTIVITY);
     }
 
     public void goToItemSelection(long id) {
         Intent intent = new Intent(this, ItemListActivity.class);
         intent.putExtra("shoppingListId", id);
         this.startActivityForResult(intent, ITEMLISTACTIVITY);
     }
     
     public void manageItems() {
         Intent intent = new Intent(this, ItemListActivity.class);
         intent.putExtra("noList", true);
         this.startActivity(intent);
     }
 
     public void createNewShoppingList() {
         final AlertDialog.Builder alert = new AlertDialog.Builder(this);
         final EditText input = new EditText(this);
         input.setWidth(200); // TODO: hard coded width is bad
         input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
         alert.setTitle("Type a name");
         DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
         String text = "My List " + f.format(new Date());
         input.setText(text);
         input.setSelection(0, text.length());
         alert.setView(input);
         alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 String newList = input.getText().toString().trim();
                 ShoppingList sl = new ShoppingList();
                 if(!newList.equals("")) {
                     sl.setName(newList);
                     long id = model.addNewShoppingList(sl);
                     update();
                     goToItemSelection(id);
                 }
             }
         });
 
         alert.setNegativeButton("Cancel",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         dialog.cancel();
                     }
                 });
         alert.show();
 
     }
     
     public void deleteShoppingList(ShoppingList sl) {
         model.deleteShoppingList(sl);
         update();
     }
 }
