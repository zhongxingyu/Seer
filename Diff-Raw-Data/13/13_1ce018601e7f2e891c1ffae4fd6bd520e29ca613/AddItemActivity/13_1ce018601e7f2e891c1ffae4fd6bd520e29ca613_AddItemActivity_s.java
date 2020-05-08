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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Set;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 public class AddItemActivity extends Activity {
 	private int suitcaseId;
 	private SQLiteDatabase db;
 	private ArrayList<String> insertList; //inserts this list into QuickAdd table
 	private float density;
 	private AdView adView;
 
 	
 	ArrayList<String> wantedList; // I realized the 2nd parameter of this hasshMap (Quantity) is not neede. Oh well 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		wantedList=new ArrayList<String>();
 		density = this.getResources().getDisplayMetrics().density;
 		setContentView(R.layout.activity_add_item);
 		db = openOrCreateDatabase("data.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
 		db.setVersion(1);
 		db.setLocale(Locale.getDefault());
 		insertList = new ArrayList<String>();
 		Bundle extras = getIntent().getExtras();
 		suitcaseId = extras.getInt("suitcase_id");
 		setTitle("Suggested Items");
		Cursor c = db.rawQuery("SELECT * from QuickAdd", null);
 		if (c.getCount() <= 0) {// if there is nothing in the QuickAdd Table
 
 			addIntoArrayList(); // add all the items into InserList. Then we shove it into the DB
 
 			for (int i = 0; i < insertList.size(); i++) {
 				String tempName = insertList.get(i);
				String INSERT_STATEMENT = "INSERT INTO QuickAdd (name) Values (\"" + tempName + "\")";
 				db.execSQL(INSERT_STATEMENT); // insert into trip_table db
 			}
 
 		}
 		String myAdmobPublisherID="a1508d762ede868";
 		adView = new AdView(this, AdSize.SMART_BANNER, myAdmobPublisherID);  
 		LinearLayout layout = (LinearLayout)findViewById(R.id.add_item_container);
 		adView.loadAd(new AdRequest());
 		layout.addView(adView,0);
 		createLayoutsFromDB();
 		c.close();
 
 	}
 
 	public boolean isAlreadyInSuitcase(String itemString)
 	{
 		
 		Cursor c = db.rawQuery("SELECT * from Item where suitcase_id=\""
 				+ suitcaseId + "\"", null);
 		c.moveToFirst();
 		while (c.isAfterLast() == false) {
 			String itemName = c.getString(c.getColumnIndex("item_name"));
 			c.moveToNext();
 			if (itemString.equals(itemName)) {
 				return true;
 			}
 		}
 		c.close();
 		return false;
 	}
 	public void createLayoutsFromDB() {
 
 		/* Code Below fetches trips from trip_table and creates a layout*/
		Cursor c = db.rawQuery("SELECT * from QuickAdd ORDER BY name", null);
 		c.moveToFirst();
 		while (c.isAfterLast() == false) {
 
 			TextView hw = new TextView(this);
 			final String text = c.getString(c.getColumnIndex("name"));
 			if(isAlreadyInSuitcase(text)==true) // dont show items that are already in suitcas
 			{
 			c.moveToNext();
 			}
 			 
 			else //its not in suitcase already so we can display it
 			{
 			hw.setText(text);
 			hw.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
 			final CheckBox checkBox=new CheckBox(this);
 			int width = (int) (58 * density);
 			int height = (int) (50 * density);
 			checkBox.setLayoutParams(new LayoutParams(width, height));//set dimensions of checkbox
 				
 			int txtPadding = (int) (20 * density);
 			hw.setPadding(0, txtPadding, 0, 0);
 			RelativeLayout relativeLayoutAdd = new RelativeLayout(this); // put the quantity edittext on this relative layout to push to the right
 			RelativeLayout.LayoutParams paramRight = new RelativeLayout.LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 			paramRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
 		
 			LinearLayout newTab = new LinearLayout(this);
 			newTab.setOrientation(LinearLayout.HORIZONTAL);
 			newTab.addView(checkBox);
 			newTab.addView(hw);	
 			newTab.addView(relativeLayoutAdd);
 			newTab.setBackgroundColor(Color.WHITE);
 
 			LinearLayout tripContainer = (LinearLayout) findViewById(R.id.add_item_container);
 			tripContainer.addView(newTab);
 			View ruler = new View(this);
 			ruler.setBackgroundColor(Color.BLACK); // this code draws the black lines
 			tripContainer.addView(ruler, new ViewGroup.LayoutParams(
 					ViewGroup.LayoutParams.MATCH_PARENT, 2));
 			c.moveToNext();
 
 			
 			newTab.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					if(checkBox.isChecked())
 					checkBox.setChecked(false);
 					else 
 						checkBox.setChecked(true);	
 				}
 			});
 			}
 		}
 		c.close();
 	}
 	
 
 	
 	public void addSuggestedItem(View view)
 	{
 		
 		boolean checkedSomething=false;
 		LinearLayout iterateMe = (LinearLayout)findViewById(R.id.add_item_container);
 		
 		for(int i=1; i<iterateMe.getChildCount(); i++)
 		{
 		
 		if(i %2==0) // aka if it is even, then its a linear layout
 		{
 			LinearLayout ll= (LinearLayout)iterateMe.getChildAt(i);
 			CheckBox checkBox= (CheckBox)ll.getChildAt(0);
 			if(checkBox.isChecked())
 			{
 				TextView itemName= (TextView) ll.getChildAt(1); 
 				String itemString= (String)itemName.getText();
 				wantedList.add(itemString);
 				checkedSomething=true;
 									
 			}
 		}
 		
 		else continue;// its odd so continue
 		}
 	if(checkedSomething==true)//they had something checked 
 	{
 	 performInsertStuff();
 		 wantedList.clear();
 	 
 	}
 	
 	else // they didnt checmark any items
 	{
 		AlertDialog dupe = new AlertDialog.Builder(
 				AddItemActivity.this).create();
 		dupe.setMessage("Please select some items that you want to add");
 		dupe.setButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		});
 
 		dupe.show();
 
 	}
 		
 		
 	}
 		
 		
 	public void performInsertStuff()
 	{
 		/*ProgressBar pb=new ProgressBar(this);
 			AlertDialog dupe = new AlertDialog.Builder(AddItemActivity.this).create();
 							dupe.setTitle("Adding selected item(s) to your suitcase");
 							dupe.setView(pb);
 							dupe.setCancelable(false);
 							dupe.setMessage("Tip: Default quantity is set to 1. You can edit the quantity by pressing and holding the item");
 							dupe.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int which) {
 								}
 							});
 							
 			dupe.show();	*/
 			
 	for (int i=0; i < wantedList.size(); i++) {
 			String itemString1 = (String)wantedList.get(i);
 			 Log.w("a","Wanted List has the following item,: " + itemString1 );
 		
 							String INSERT_STATEMENT = new StringBuilder(
 										"INSERT INTO Item (item_name, quantity, suitcase_id, is_slashed) Values (\"")
 										.append(itemString1).append("\", \"").append("1")
 										.append("\",\"").append(suitcaseId)
 										.append("\",\"0\")").toString();
 								db.execSQL(INSERT_STATEMENT);
 								Intent resultIntent = new Intent();
 								resultIntent.putExtra("suitcase_id", suitcaseId);
 								setResult(RESULT_OK, resultIntent);
 								finish();
 				}
 			 
 		}	
 	
 	public boolean canInsertPrompt()
 	{	ProgressBar pb=new ProgressBar(this);
 		AlertDialog.Builder builder = new AlertDialog.Builder(AddItemActivity.this);
 		builder.setMessage("Adding ").setCancelable(false).setView(pb)
 				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 				
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
 	
 	public void addIntoArrayList() {
 		insertList.add("Shoes");
 		insertList.add("Underwear");
 		insertList.add("Jacket");
 		insertList.add("Sweater");
 		insertList.add("Pajama");
 		insertList.add("Swimsuit");
 		insertList.add("T-Shirt");
 		insertList.add("Socks");
 		insertList.add("Dresses");
 		insertList.add("Blouse");
 		insertList.add("Umbrella");
 		insertList.add("Scarf");
 		insertList.add("Gloves");
 		insertList.add("Hat");
 		insertList.add("Cap");
 		insertList.add("Pants");
 		insertList.add("Boots");
 		insertList.add("Sandals");
 		insertList.add("Slippers");
 		insertList.add("Belt");
 		insertList.add("Toilet Paper");
 		insertList.add("Sunscreen");
 		insertList.add("Lip Balm");
 		insertList.add("First Aid Kit");
 		insertList.add("Sunglasses");
 		insertList.add("Maps");
 		insertList.add("Computer");
 		insertList.add("Tablet");
 		insertList.add("Phone");
 		insertList.add("Toothbrush");
 		insertList.add("Toothpaste");
 		insertList.add("Comb");
 		insertList.add("Shampoo");
 		insertList.add("Nail Clippers");
 		insertList.add("Towel");
 		insertList.add("Camera");
 		insertList.add("Passport");
 		insertList.add("Travel Ticket");
 		insertList.add("Fanny Pack");
 	}
 
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
