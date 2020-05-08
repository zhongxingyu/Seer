 /*	
 	NOTICE for Luggage & Suitcase Checklist, an Android app:
     Copyright (C) 2012 EBAK Mobile
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
     */
 package com.lugcheck;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ItemActivity extends Activity {
 
 	private SQLiteDatabase db;
 	private int suitcaseId;
 	private int limit;
 	private float density;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		density = this.getResources().getDisplayMetrics().density;
 		setContentView(R.layout.activity_item);
 		db = openOrCreateDatabase("data.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
 		db.setVersion(1);
 		db.setLocale(Locale.getDefault());
 		limit = 0;
 
 		Bundle extras = getIntent().getExtras();
 		suitcaseId = extras.getInt("suitcase_id"); // receiving suitcase_id from previous activity
 
 		/* code below is to set the activity title to the trip_name */
 		String GET_TRIP_NAME = "select * from Suitcase where suitcase_id = '" + suitcaseId + "'";
 		Cursor c = db.rawQuery(GET_TRIP_NAME, null);
 		c.moveToFirst();
 		String suitcaseName = c.getString(c.getColumnIndex("suitcase_name"));
 		setTitle("Displaying items for " + suitcaseName);
 
 		LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 		View ruler = new View(this);
 		ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 		tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT, 2));
 		createLayoutsFromDB();
 		c.close();
 
 	}// end onCreate
 
 	protected void onStart() {//when you come back to this activity class
 		super.onStart(); // Always call the superclass method first
 		/* code below is to set the activity title to the trip_name */
 		String GET_TRIP_NAME = "select * from Suitcase where suitcase_id = '" + suitcaseId + "'";
 		Cursor c = db.rawQuery(GET_TRIP_NAME, null);
 		c.moveToFirst();
 		String suitcaseName = c.getString(c.getColumnIndex("suitcase_name"));
 		setTitle("Displaying items for " + suitcaseName);
 
 		LinearLayout addTrip = (LinearLayout) findViewById(R.id.add_item);
 
 		LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 		tripContainer.removeAllViews();
 		tripContainer.addView(addTrip);
 
 		View ruler = new View(this);
 		ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 		tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT, 2));
 		createLayoutsFromDB();
 		c.close();
 
 	}
 
 	public void createLayoutsFromDB() {
 		/* Code Below fetches trips from item_table and creates a layout */
 		Cursor c = db.rawQuery("SELECT * from Item where suitcase_id = '" + suitcaseId + "'", null);
 		c.moveToFirst();
 		while (c.isAfterLast() == false) {
 
 			int isSlashed = c.getInt(c.getColumnIndex("is_slashed"));
 			TextView hw = new TextView(this);
 			String text = c.getString(c.getColumnIndex("item_name"));
 			int ITEM_ID = c.getInt(c.getColumnIndex("item_id"));
 			String quant = c.getString(c.getColumnIndex("quantity"));
 			hw.setText(" x " + text);
 			hw.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
 			TextView qtext = new TextView(this);
 			qtext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
 			qtext.setText(quant);
 
 			if (isSlashed == 1) { // omg this is dirty. But I just copied and pasted the code from the else, with slight revisions. This section draws a slashed out item tab
 				hw.setPaintFlags(hw.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
 
 				ImageView checkmarkImage = new ImageView(ItemActivity.this);
 				checkmarkImage.setImageResource(R.drawable.checkmark); // creating a checkmark
 				// FROM STACKOVERFLOW!
 				int width = (int) (58 * density);
 				int height = (int) (50 * density);
 				checkmarkImage.setLayoutParams(new LayoutParams(width, height));
 				int pad = (int) (5 * density);
 				checkmarkImage.setPadding(pad, pad, 0, 0);
 
 				ImageView im = new ImageView(this);
 				im.setImageResource(R.drawable.opensuitcase);
 				// FROM STACKOVERFLOW!
 				im.setLayoutParams(new LayoutParams(width, height));
 				im.setPadding(pad, pad, 0, 0);
 				// END
 				int txtPadding = (int) (20 * density);
 				hw.setPadding(0, txtPadding, 0, 0);
 
 				LinearLayout newTab = new LinearLayout(this);
 				newTab.setOrientation(LinearLayout.HORIZONTAL);
 				newTab.addView(im);
 				newTab.addView(qtext);
 				newTab.addView(hw);
 				newTab.addView(checkmarkImage);
 				newTab.setBackgroundColor(Color.WHITE);
 				LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 
 				tripContainer.addView(newTab);
 				View ruler = new View(this);
 				ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 				tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 						ViewGroup.LayoutParams.MATCH_PARENT, 2));
 				c.moveToNext();
 
 				/* Code Below handles the delete situation */
 				final String text2 = text;
 				newTab.setOnLongClickListener(new OnLongClickListener() { // code to delete a list
 					public boolean onLongClick(View v) {
 
 						AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
 						builder.setMessage("Are you sure you want delete?").setCancelable(false)
 								.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int id) {
 										deleteFromDB(text2, suitcaseId);
 									}
 								}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int id) {
 										dialog.cancel();
 									}
 								});
 
 						AlertDialog alert = builder.create();
 						alert.show();
 						return true;
 					}
 				});
 
 				/* Code below handles the situation where u click a item */
 
 				final int item_id2 = ITEM_ID;
 
 				newTab.setOnClickListener(new Button.OnClickListener() {
 					public void onClick(View view) {
 
 						LinearLayout ll = (LinearLayout) view;
 						TextView textBox = (TextView) ll.getChildAt(2);
 
 						ImageView im = new ImageView(ItemActivity.this);
 						im.setImageResource(R.drawable.checkmark); // creating a checkmark
 						// FROM STACKOVERFLOW!
 						float d = ItemActivity.this.getResources().getDisplayMetrics().density;
 						int width = (int) (58 * d);
 						int height = (int) (50 * d);
 						im.setLayoutParams(new LayoutParams(width, height));
 						int pad = (int) (5 * d);
 						im.setPadding(pad, pad, 0, 0);
 
 						if ((textBox.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) // removes slash
 						{
 							textBox.setPaintFlags(textBox.getPaintFlags()
 									& (~Paint.STRIKE_THRU_TEXT_FLAG));
 							ll.removeViewAt(3); // removes the checkmark icon
 
 							String UPDATE_STATEMENT = "UPDATE Item SET is_slashed = '0' WHERE item_id ='"
 									+ item_id2 + "'";
 							db.execSQL(UPDATE_STATEMENT);
 
 						}
 
 						else // adds the slash
 						{
 							textBox.setPaintFlags(textBox.getPaintFlags()
 									| Paint.STRIKE_THRU_TEXT_FLAG);
 							ll.addView(im);
 							String UPDATE_STATEMENT = "UPDATE Item SET is_slashed = '1' WHERE item_id='"
 									+ item_id2 + "'";
 							db.execSQL(UPDATE_STATEMENT);
 						}
 					}
 				});
 
 			}
 
 			else { //add a non-slashed out item tab
 				ImageView im = new ImageView(this);
 				im.setImageResource(R.drawable.opensuitcase);
 				// FROM STACKOVERFLOW!
 				int width = (int) (58 * density);
 				int height = (int) (50 * density);
 				im.setLayoutParams(new LayoutParams(width, height));
 				int pad = (int) (5 * density);
 				im.setPadding(pad, pad, 0, 0);
 				// END
 				int txtPadding = (int) (20 * density);
 				hw.setPadding(0, txtPadding, 0, 0);
 
 				LinearLayout newTab = new LinearLayout(this);
 				newTab.setOrientation(LinearLayout.HORIZONTAL);
 				newTab.addView(im);
 				newTab.addView(qtext);
 				newTab.addView(hw);
 				newTab.setBackgroundColor(Color.WHITE);
 				LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 
 				tripContainer.addView(newTab);
 				View ruler = new View(this);
 				ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 				tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 						ViewGroup.LayoutParams.MATCH_PARENT, 2));
 				c.moveToNext();
 
 				/* Code Below handles the delete situation */
 				final String text2 = text;
 				newTab.setOnLongClickListener(new OnLongClickListener() { // code to delete a list
 					public boolean onLongClick(View v) {
 
 						AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
 						builder.setMessage("Are you sure you want delete?").setCancelable(false)
 								.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int id) {
 										deleteFromDB(text2, suitcaseId);
 									}
 								}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int id) {
 										dialog.cancel();
 									}
 								});
 
 						AlertDialog alert = builder.create();
 						alert.show();
 						return true;
 					}
 				});
 
 				/* Code below handles the situation where u click a item */
 
 				final int item_id2 = ITEM_ID;
 
 				newTab.setOnClickListener(new Button.OnClickListener() {
 					public void onClick(View view) {
 
 						LinearLayout ll = (LinearLayout) view;
 						TextView textBox = (TextView) ll.getChildAt(2);
 
 						ImageView im = new ImageView(ItemActivity.this);
 						im.setImageResource(R.drawable.checkmark); // creating a checkmark
 						// FROM STACKOVERFLOW!
 						int width = (int) (58 * density);
 						int height = (int) (50 * density);
 						im.setLayoutParams(new LayoutParams(width, height));
 						int pad = (int) (5 * density);
 						im.setPadding(pad, pad, 0, 0);
 
 						if ((textBox.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) // removes slash
 						{
 							textBox.setPaintFlags(textBox.getPaintFlags()
 									& (~Paint.STRIKE_THRU_TEXT_FLAG));
 							ll.removeViewAt(3); // removes the checkmark icon
 
 							String UPDATE_STATEMENT = "UPDATE Item SET is_slashed = '0' WHERE item_id ='"
 									+ item_id2 + "'";
 							db.execSQL(UPDATE_STATEMENT);
 
 						}
 
 						else // adds the slash
 						{
 							textBox.setPaintFlags(textBox.getPaintFlags()
 									| Paint.STRIKE_THRU_TEXT_FLAG);
 							ll.addView(im);
 							String UPDATE_STATEMENT = "UPDATE Item SET is_slashed = '1' WHERE item_id='"
 									+ item_id2 + "'";
 							db.execSQL(UPDATE_STATEMENT);
 						}
 					}
 				});
 
 			}//end else 		
 		}// end while*/
 		c.close();
 	} // end method
 
 	public void deleteFromDB(String i, int suitcase_id) {
 
 		String deleteFromDB = "delete from Item where item_name = '" + i + "' and suitcase_id = '"
 				+ suitcase_id + "'";
 		db.execSQL(deleteFromDB);
 
 		LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 		LinearLayout addTrip = (LinearLayout) findViewById(R.id.add_item);
 		tripContainer.removeAllViews();
 		tripContainer.addView(addTrip);
 		View ruler = new View(this);
 		ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 		tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT, 2));
 		createLayoutsFromDB();
 
 	}
 
 	public void addItem(View view) {// go to add activity screen
 
 		/*Intent intent = new Intent(ItemActivity.this, AddItemActivity.class);
 		intent.putExtra("suitcase_id", suitcaseId);
 		startActivity(intent);*/
 
 		if (limit == 0)// checking to make sure there is no open layouts
 		{
 			limit = 1;
 
 			LayoutParams lp = new LayoutParams(-1, -2);
 			final LinearLayout newTab = new LinearLayout(this);
 			newTab.setOrientation(LinearLayout.VERTICAL);
 			final EditText itemNameText = new EditText(this);
 			itemNameText.setHint("Enter item name");
 			itemNameText.setHeight((int) (50 * density));
 			final EditText quantity = new EditText(this);
 			quantity.setHint("Enter Quantity");
 			quantity.setHeight((int) (50 * density));
 			final Button okButton = new Button(this);
 			okButton.setText("Add");
 			Button cancelButton = new Button(this);
 			cancelButton.setText("Cancel");
 			Button quickAddButton = new Button(this);
 			quickAddButton.setText("Quick Add...");
 			LinearLayout ll = new LinearLayout(this);
 			LinearLayout horizontalButtons = new LinearLayout(this);
 			horizontalButtons.setOrientation(LinearLayout.HORIZONTAL);// used to make button horizontal
 			LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
 					LayoutParams.MATCH_PARENT, 1.0f); //param sets it so the width is 50% horizontally. Looked nicer!
 			horizontalButtons.addView(okButton, param);
 			horizontalButtons.addView(cancelButton, param);
 			ll.setOrientation(LinearLayout.VERTICAL);
 			ll.addView(itemNameText);
 			ll.addView(quantity);
 			ll.addView(horizontalButtons);
 			ll.addView(quickAddButton);
 			newTab.addView(ll);
 
 			View ruler = new View(this);
 			ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 			newTab.addView(ruler,
 					new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
 
 			LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 			tripContainer.addView(newTab, 0, lp);
 
 			quickAddButton.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					Intent intent = new Intent(ItemActivity.this, AddItemActivity.class);
 					intent.putExtra("suitcase_id", suitcaseId);
 					Log.w("sending over suitcase id: ", " " + suitcaseId);
 					startActivity(intent);
					ItemActivity.this.finish();
 				}
 			});
 
 			//Code below is for clicking add or cancel button
 			okButton.setOnClickListener(new View.OnClickListener() {
 				@SuppressWarnings("deprecation")
 				public void onClick(View v) {
 
 					LinearLayout buttonParent = (LinearLayout) okButton.getParent().getParent(); // had to do this twice cos of the horizontal layer addition
 					EditText textBoxItemName = (EditText) buttonParent.getChildAt(0);// gets value of textbox
 					EditText textBoxQuantity = (EditText) buttonParent.getChildAt(1);
 					String itemName = textBoxItemName.getText().toString();
 					String quantity = textBoxQuantity.getText().toString();
 
 					if (quantity.equals("") && itemName.equals("")) {
 						AlertDialog dupe = new AlertDialog.Builder(ItemActivity.this).create();
 						dupe.setMessage("Please enter a valid name and quantity");
 						dupe.setButton("Ok", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 							}
 						});
 
 						dupe.show();
 
 					}
 
 					else if (itemName.equals("")) // if they try to add a null
 													// trip to database
 					{
 						AlertDialog dupe = new AlertDialog.Builder(ItemActivity.this).create();
 						dupe.setMessage("You cannot enter a blank item name. Please enter a item name");
 						dupe.setButton("Ok", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 							}
 						});
 
 						dupe.show();
 
 					}
 
 					else if (quantity.equals("")) {
 						AlertDialog dupe = new AlertDialog.Builder(ItemActivity.this).create();
 						dupe.setMessage("You cannot enter a blank quantity. Please enter a quantity");
 						dupe.setButton("Ok", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 							}
 						});
 
 						dupe.show();
 
 					}
 
 					else if (!quantity.matches("\\d+")) {
 						AlertDialog dupe = new AlertDialog.Builder(ItemActivity.this).create();
 						dupe.setMessage("Please enter a numeric value for 'Quantity'");
 						dupe.setButton("Ok", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 							}
 						});
 
 						dupe.show();
 
 					}
 					// code below checks for duplicates in database
 					else {
 
 						Cursor c = db.rawQuery("SELECT * from Item where suitcase_id='"
 								+ suitcaseId + "'", null);
 						c.moveToFirst();
 
 						boolean isDupe = false;
 						while (c.isAfterLast() == false) {
 							String text = c.getString(c.getColumnIndex("item_name"));
 							c.moveToNext();
 							if (text.equals(itemName)) {
 								isDupe = true;
 								break;
 							}
 						}// end while
 
 						c.close();
 
 						if (isDupe == false) {// it will successfully insert item into db table
 							String INSERT_STATEMENT = "INSERT INTO Item (item_name, quantity, suitcase_id, is_slashed) Values ('"
 									+ itemName + "', '" + quantity + "','" + suitcaseId + "','0')";
 							db.execSQL(INSERT_STATEMENT); // insert into
 															// item_table db
 							limit = 0; // allow to recreate a new trip
 							LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 							LinearLayout addTrip1 = (LinearLayout) findViewById(R.id.add_item);
 							tripContainer.removeAllViews();
 							tripContainer.addView(addTrip1);
 							View ruler = new View(ItemActivity.this);
 							ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 							tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 									ViewGroup.LayoutParams.MATCH_PARENT, 2));
 							createLayoutsFromDB();
 						}
 
 						else {
 
 							AlertDialog dupe = new AlertDialog.Builder(ItemActivity.this).create();
 							dupe.setTitle("Duplicate Found");
 							dupe.setMessage("Item already exists. Please use that item instead");
 							dupe.setButton("Ok", new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int which) {
 								}
 							});
 
 							dupe.show();
 
 						}
 					}
 				}
 			});
 
 			cancelButton.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					limit = 0;
 					LinearLayout tripContainer = (LinearLayout) findViewById(R.id.item_container);
 					LinearLayout addTrip = (LinearLayout) findViewById(R.id.add_item);
 					tripContainer.removeAllViews();
 					tripContainer.addView(addTrip);
 					View ruler = new View(ItemActivity.this);
 					ruler.setBackgroundColor(Color.BLACK); // this code draws
 															// the black lines
 					tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 							ViewGroup.LayoutParams.MATCH_PARENT, 2));
 					createLayoutsFromDB();
 
 				}
 			});
 
 		}
 
 	}
 
 	/* method below determines if a string is a integer. Used for Quanity */
 	public boolean isInteger(String s) {
 		boolean result = false;
 		try {
 			Integer.parseInt("-1234");
 			result = true;
 		} catch (NumberFormatException nfe) {
 			// no need to handle the exception
 		}
 		return result;
 	}
 }
