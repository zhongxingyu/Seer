 /**
  * at.mkweb.android.simpleshoppinglist.Lists
  * 
  * LICENSE:
  *
  * This file is part of SimpleShoppingList, an Android app to create very simple shopping lists (http://android.mk-web.at/app/simpleshoppinglist.html).
  *
  * SimpleShoppingList is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
  * later version.
  *
 * NabDroi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
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
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class List extends TableRow {
 
 	Context context;
 	
 	int id;
 	String name;
 	boolean active = false;
 	
 	TextView textView;
 	
 	SQLiteDatabase db;
 	
 	public List(Context context) {
 		super(context);
 		
 		setGravity(Gravity.CENTER_VERTICAL);
 		
 		db = ((SQLiteDatabase) Registry.get(Registry.DATABASE));
 		
 		this.context = context;
 	}
 
 	public void setActive(boolean active) {
 		
 		this.active = active;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void create() {
 	
 		removeAllViews();
 		addViews();
 	}
 	
 	private void addViews() {
 		
 		textView = new TextView(context);
 		textView.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				((ManageLists) context).goBack(id);
 			}
 		});
 		
 		addView(textView);
 	    
 		textView.setText(name);
 		textView.setId(1000000 + getId());
 		textView.setTextColor((active == true ? Color.BLACK : Color.GRAY));
 		textView.setTextSize(26);
 	    
     	textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
     	textView.setPadding(4, 0, 0, 0);
     	
     	Button renameButton = new Button(context);
     	renameButton.setText(" ");
     	renameButton.setTextSize(8);
     	renameButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_edit));
     	renameButton.setPaintFlags(renameButton.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
     	renameButton.setGravity(FOCUS_RIGHT);
     	
     	renameButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				showRenameListDialog();
 			}
 		});
     	
     	addView(renameButton);
     	
     	if(id != 1) {
 	    	Button removeButton = new Button(context);
 	    	removeButton.setText(" ");
 	    	removeButton.setTextSize(8);
 	    	removeButton.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.ic_delete));
 	    	removeButton.setPaintFlags(removeButton.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
 	    	removeButton.setGravity(FOCUS_RIGHT);
 	    	
 	    	removeButton.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					
 					showRemoveListDialog();
 				}
 			});
 	    	
 	    	addView(removeButton);
     	}
 	}
 	
     public void showRenameListDialog() {
     	
     	final EditText et = new EditText(context);
     	et.setText(name);
 		
 		AlertDialog.Builder b = new AlertDialog.Builder(context);
 		b.setView(et);
 		b.setPositiveButton(((ManageLists) context).getText(R.string.button_ok), new AlertDialog.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				String value = et.getText().toString().trim();
 				
 				if(value.length() > 0) {
 					
 					((ManageLists) context).showToast(((ManageLists) context).getText(R.string.renamed_to) + ": " + value);
 					db.execSQL("UPDATE categories SET name = '" + value + "' WHERE id = " + id);
 					((ManageLists) context).updateView();
 				} else {
 					
 					((ManageLists) context).showToast(((ManageLists) context).getText(R.string.err_elem_not_entered).toString());
 				}
 				dialog.cancel();
 			}
 		});
 		
 		b.setNegativeButton(((ManageLists) context).getText(R.string.button_cancel), new AlertDialog.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.cancel();
 			}
 		}); 
 		
 		AlertDialog dialog = b.create();
 		
 		dialog.setTitle(((ManageLists) context).getText(R.string.dialog_rename_list_title));
 		dialog.setMessage(((ManageLists) context).getText(R.string.dialog_rename_list_message));
 		
 		dialog.show();
     }
 	
 	private void showRemoveListDialog() {
 		
 		AlertDialog.Builder b = new AlertDialog.Builder(context);
 		b.setPositiveButton(((ManageLists) context).getText(R.string.button_ok), new AlertDialog.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				db.execSQL("DELETE FROM items WHERE category_id = '" + id + "';");
 				db.execSQL("DELETE FROM categories WHERE id = '" + id + "';");
 				((ManageLists) context).updateView();
 				dialog.cancel();
 			}
 		});
 		b.setNegativeButton(((ManageLists) context).getText(R.string.button_cancel), new AlertDialog.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.cancel();
 			}
 		});
 		
 		AlertDialog d = b.create();
 		d.setTitle(((ManageLists) context).getText(R.string.dialog_remove_title));
 		d.setMessage(((ManageLists) context).getText(R.string.dialog_remove_message));
 		
 		d.show();
 	}
 }
