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
 import android.os.Environment;
 import android.text.InputType;
 import android.view.*;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import no.opentech.shoppinglist.R;
 import no.opentech.shoppinglist.ShoppingListApp;
 import no.opentech.shoppinglist.adapters.ItemAdapter;
 import no.opentech.shoppinglist.entities.Item;
 import no.opentech.shoppinglist.external.NumberPickerDialog;
 import no.opentech.shoppinglist.models.ItemListModel;
 import no.opentech.shoppinglist.utils.Logger;
 import no.opentech.shoppinglist.utils.Utils;
 
 import java.util.ArrayList;
 
 /**
  * Created by: Christian Lønaas
  * Date: 08.04.11
  * Time: 20:45
  */
 public class ItemListActivity extends ListActivity
 {
     private Context context = ShoppingListApp.getContext();
     private ItemListModel model;
     private ItemAdapter adapter;
     private long shoppingListId;
     private boolean noList = false;
     public static Logger log;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         log = Logger.getLogger(ItemListActivity.class);
         model = new ItemListModel();
         shoppingListId = getIntent().getLongExtra("shoppingListId", 0);
         noList = getIntent().getBooleanExtra("noList", false);
         setTitle((!noList) ? "Select items" : "Manage items");
         adapter = new ItemAdapter(context, R.layout.list_item, model);
         setListAdapter(adapter);
         model.setAdapter(adapter);
         ListView lv = getListView();
         lv.setBackgroundResource(R.drawable.paper);
        lv.setCacheColorHint(Color.parseColor("#00000000")); // transparent, to fix scrolling bug
         registerForContextMenu(lv);
         lv.setTextFilterEnabled(true);
 
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 // toggle checked
                 Item selectedItem = adapter.getItem(position);
                 log.debug("Toggling item " + selectedItem.getName() + " at position " + position);
                 selectedItem.setChecked(!selectedItem.isChecked());
                 update();
             }
         });
     }
     
     public void update() {
         adapter.notifyDataSetChanged();
     }
     
     @Override
     public void onBackPressed() {
         Intent resultIntent = new Intent();
         resultIntent.putExtra("shoppingListId", shoppingListId);
         setResult(Activity.RESULT_CANCELED, resultIntent);
         finish();
     }
     
     @Override
     public boolean onSearchRequested() {
         log.debug("Search requested");
         InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         mgr.showSoftInput(getListView(), InputMethodManager.SHOW_IMPLICIT);
         return true;
     }
     
     /* create the menu */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inf = getMenuInflater();
         if(!noList)
             inf.inflate(R.menu.itemlist_options, menu);
         else inf.inflate(R.menu.itemlist_options_nolist, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.newitem:
                 addNewItem();
                 break;
             case R.id.savelist:
                 Toast.makeText(context, "Saving list..", Toast.LENGTH_SHORT).show();
                 saveList();
                 break;
             case R.id.clearlist:
                 clearList();
                 break;
             case R.id.deleteselected:
                 deleteSelected();
                 break;
             case R.id.exportitems:
                 exportItems();
                 break;
             case R.id.importitems:
                 importItems();
                 break;
         }
         return true;
     }
 
     @Override
     public void onUserInteraction() {
         
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inf = getMenuInflater();
         if(!noList) inf.inflate(R.menu.items_context_menu, menu);
         else inf.inflate(R.menu.items_context_menu_nolist, menu);
         menu.setHeaderTitle(R.string.context_menu_title);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         Item selectedItem = adapter.getItem(info.position);
 
         switch(item.getItemId()) {
             case R.id.setitemamount:
                 setItemAmount(selectedItem);
                 break;
             case R.id.removeitem:
                 removeItem(selectedItem);
                 break;
             case R.id.showitemdetails:
                 Intent intent = new Intent(this, ItemDetailsActivity.class);
                 intent.putExtra("itemId", selectedItem.getId());
                 this.startActivity(intent);
                 break;
         }
         return true;
     }
     
     public void saveList() {
         model.save(shoppingListId);
 
         Intent resultIntent = new Intent();
         setResult(Activity.RESULT_OK, resultIntent);
         this.finish();
     }
 
     /* show dialog with input box, for adding item to shopping list */
     public void addNewItem() {
         final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
         input.setWidth(200); // TODO: hard coded width is bad
         input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
         input.setHint("Enter name for the item");
         alert.setTitle("Item name");
 		alert.setView(input);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String newItem = input.getText().toString().trim();
                 Item item = new Item();
                 if(!newItem.equals("")) {
                     item.setName(newItem);
                     model.addItem(item);
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
     
     public void setItemAmount(final Item item) {
         NumberPickerDialog picker = new NumberPickerDialog(this, android.R.style.Theme_Dialog, 1);
         picker.setTitle("Select amount");
         picker.setOnNumberSetListener(new NumberPickerDialog.OnNumberSetListener() {
             public void onNumberSet(int selectedNumber) {
                 for(int i=0; i<model.getItemList().size(); i++) {
                     if(item.getId() == model.getItem(i).getId()) {
                         model.getItem(i).setAmount(selectedNumber);
                        model.getItem(i).setChecked(true); // assume user wants the item checked when selecting amount
                         update();
                     }
                 }
             }
         });
         picker.show();
     }
 
     public void removeItem(Item item) {
         model.removeItem(item);
     }
     
     public void deleteSelected() {
         ArrayList<Item> itemsToDelete = new ArrayList<Item>();
         for (Item shoppingItem : model.getCheckedItems())
             itemsToDelete.add(shoppingItem);
 
         int deleted=itemsToDelete.size();
         model.removeItems(itemsToDelete);
         Toast.makeText(context,((deleted != 0) ? "Deleted " + deleted + " items" : "No items deleted"), Toast.LENGTH_SHORT).show();
     }
 
     public void exportItems() {
         if(!model.exportItems())
             Toast.makeText(context, "Couldn't write export file", Toast.LENGTH_SHORT);
         else Toast.makeText(context, "Export written to " + Environment.getExternalStorageDirectory().getAbsolutePath() + Utils.FILEROOT + 
                 Utils.BACKUPFILE, Toast.LENGTH_SHORT);
     }
 
     public void importItems() {
         if(model.importItems()) {
             Toast.makeText(context, "Items imported", Toast.LENGTH_SHORT);
         } else Toast.makeText(context, "Error importing", Toast.LENGTH_SHORT);
     }
 
     public void clearList() {
         model.clearCheckedItems();
     }
 }
