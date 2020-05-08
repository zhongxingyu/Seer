 /**
  * at.mkweb.android.simpleshoppinglist.SimpleShoppingList
  * 
  * LICENSE:
  *
  * This file is part of SimpleShoppingList, an Android app to create very simple shopping lists (http://android.mk-web.at/app/simpleshoppinglist.html).
  *
  * SimpleShoppingList is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
  * later version.
  *
  * SimpleShoppingList is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with software.  If not, see
  * <http://www.gnu.org/licenses/>.
  *
  * @author Mario Klug <mario.klug@mk-web.at>
  * @package at.mkweb.android.simpleshoppinglist
  * 
  * @license http://www.gnu.org/licenses/gpl.html
  */
 
 package at.mkweb.android.simpleshoppinglist;
 
 import android.app.Activity;
 import android.app.AlertDialog;
import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Shader;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SimpleShoppingList extends Activity {
 	
 	LinearLayout linear;
 	SQLiteDatabase db;
 	
 	int categoryId;
 	String categoryName;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
     	
     	((Button) findViewById(R.id.button_add)).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				showAddElementDialog();
 			}
 		});
         
         start();
     }
     
     @Override
     protected void onActivityResult(int requestCode,int resultCode,Intent data) 
     {
         start(resultCode);
     }
     
     public void start() {
     	
     	start(1);
     }
     
     public void start(int id) {
     	
     	categoryId = id;
     	
     	db = openOrCreateDatabase("items", MODE_PRIVATE, null);
     	
         db.execSQL("CREATE TABLE IF NOT EXISTS items (id INTEGER PRIMARY KEY AUTOINCREMENT, category_id INTEGER, name VARCHAR(255), active BOOLEAN);");
         db.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255));");
         
         // New table structure for Version 1.2
         try {
         	db.rawQuery("SELECT `category_id` FROM items LIMIT 1;", null);
         } catch(Exception e) {
         	
         	// http://www.sqlite.org/faq.html - (11) How do I add or delete columns from an existing table in SQLite.
         	db.execSQL("BEGIN TRANSACTION;");
         	db.execSQL("CREATE TABLE items_backup (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255), active BOOLEAN);");
         	db.execSQL("INSERT INTO items_backup SELECT id, name, active FROM items;");
         	db.execSQL("DROP TABLE items;");
         	db.execSQL("CREATE TABLE items (id INTEGER PRIMARY KEY AUTOINCREMENT, category_id INTEGER, name VARCHAR(255), active BOOLEAN);");
         	db.execSQL("INSERT INTO items SELECT id, '1', name, active FROM items_backup");
         	db.execSQL("DROP TABLE items_backup;");
         	db.execSQL("COMMIT;");
         }
         
         // Create default category if not existing
         Cursor c = db.rawQuery("SELECT name FROM categories WHERE id = 1;", null);
         if(c.getCount() == 0) {
         	
         	db.execSQL("INSERT INTO categories (id) VALUES (null);");
         }
         c.close();
     	
     	Registry.add(Registry.DATABASE, db);
     	
         c = db.rawQuery("SELECT * FROM categories WHERE id = " + categoryId, null);
         if(c.getCount() > 0) {
         	
         	c.moveToFirst();
         	categoryName = c.getString(c.getColumnIndex("name"));
         	
         	if(categoryId == 1 && categoryName == null) {
         		
         		categoryName = getText(R.string.app_name).toString();
         	}
         } else {
         	
         	showToast(getText(R.string.err_cat_notfound).toString());
         	start(1);
         }
         c.close();
         
         ((TextView) findViewById(R.id.Headline)).setText(categoryName);
     	
         updateList();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
 
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
         
     	return true;
     }
     
     private void addElement(String name) {
 
         try {
        	ContentValues cv = new ContentValues();
        	cv.put("category_id", categoryId);
        	cv.put("name", name);
        	cv.put("active", 1);
        	
        	db.insert("items", null, cv);
         } catch (Exception e) {
         	showToast(e.getMessage());
         }
         
         updateList();
     }
     
     public boolean onOptionsItemSelected (MenuItem item){
 
     	if(item.getItemId() ==  R.id.menu_add_item) {
     		
     		showAddElementDialog();
     	}
     	
     	if(item.getItemId() == R.id.menu_manage_lists) {
     		
     		Intent manageListsIntent = new Intent(getApplicationContext(), ManageLists.class);
     		manageListsIntent.putExtra("category_id", new String("" + categoryId));
     		
     		startActivityForResult(manageListsIntent, 0);
     	}
     	
     	if(item.getItemId() == R.id.menu_clear) {
     		
     		showListClearDialog();
     	}
 
     	if(item.getItemId() == R.id.menu_exit) {
 
     		showExitDialog();
     	}
 
     	return false;
     }
     
     public void showToast(String message) {
     	
     	showToast(message, Toast.LENGTH_SHORT);
     }
     
     private void showToast(String message, int duration) {
     	
     	Toast toast = Toast.makeText(this, message, duration);
     	toast.show();
     }
     
     public void updateList() {
     	
     	if(linear != null) {
     		
     		linear.removeAllViews();
     	} else {
     		
     		linear = (LinearLayout) findViewById(R.id.ContentLinearLayout);
     	}
         
         Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.paper);
         BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
         bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
         
         ((ScrollView) findViewById(R.id.ScrollView)).setBackgroundDrawable(bitmapDrawable);
     	
     	Cursor c = db.rawQuery("SELECT id, name, active FROM items WHERE category_id = " + categoryId + ";", null);
         
         if(c.getCount() < 1) {
         	
         	showToast(getText(R.string.err_no_items_yet).toString(), Toast.LENGTH_LONG);
         } else {
         	
         	c.moveToFirst();
         	
         	do {
 	        	String name = c.getString(c.getColumnIndex("name"));
 	        	boolean active = (c.getString(c.getColumnIndex("active")).equals("1") ? true : false);
 	        	
 	            Item item = new Item(this);
 	            item.setId(new Integer(c.getString(c.getColumnIndex("id"))));
 	            item.setName(name);
 	            item.setActive(active);
 	            item.create();
 	            
 	            linear.addView(item);
             
         	} while(c.moveToNext());
         }
     }
 
     public void showAddElementDialog() {
     	
     	final EditText et = new EditText(this);
 		
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setView(et);
 		b.setPositiveButton(getText(R.string.button_ok), new AlertDialog.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				String value = et.getText().toString().trim();
 				
 				if(value.length() > 0) {
 					
 					showToast(getText(R.string.added) + ": " + value);
 					addElement(value);
 				} else {
 					
 					showToast(getText(R.string.err_elem_not_entered).toString());
 				}
 				dialog.cancel();
 			}
 		});
 		
 		b.setNegativeButton(getText(R.string.button_cancel), new AlertDialog.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.cancel();
 			}
 		}); 
 		
 		AlertDialog dialog = b.create();
 		
 		dialog.setTitle(getText(R.string.dialog_new_item_title));
 		dialog.setMessage(getText(R.string.dialog_new_item_message));
 		
 		dialog.show();
     }
     
     public void showListClearDialog() {
     	
     	AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setPositiveButton(getText(R.string.button_ok), new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				db.execSQL("DELETE FROM items WHERE category_id = " + categoryId + ";");
 				updateList();
 				dialog.cancel();
 			}
 		});
 		b.setNegativeButton(getText(R.string.button_cancel), new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.cancel();
 			}
 		});
 		
 		AlertDialog d = b.create();
 		d.setTitle(getText(R.string.dialog_clear_title));
 		d.setMessage(getText(R.string.dialog_clear_message));
 		
 		d.show();
     }
     
     public void showExitDialog() {
     	
     	AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setPositiveButton(getText(R.string.button_ok), new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.cancel();
 				System.exit(0);
 			}
 		});
 		b.setNegativeButton(getText(R.string.button_cancel), new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.cancel();
 			}
 		});
 		
 		AlertDialog d = b.create();
 		d.setTitle(getText(R.string.dialog_exit_title));
 		d.setMessage(getText(R.string.dialog_exit_message));
 		
 		d.show();
     }
 }
