 /*
  * Copyright 2011 Colin Paton - cozzarp@googlemail.com
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus.ui;
 
 import org.redbus.R;
 import org.redbus.ui.arrivaltime.ArrivalTimeActivity;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CreateShortcutActivity extends ListActivity {
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitle("Select bookmark");
         setContentView(R.layout.shortcutlayout);
     }
     
 	@Override
 	protected void onStart() 
 	{
 		super.onStart();
 		Common.updateBookmarksListAdaptor(this);
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		TextView tv = (TextView)v.findViewById(R.id.stopbookmarks_name);
 		setupShortcut(tv.getText().toString(),id);
 		finish();
 	}
 
 	// See http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/app/LauncherShortcuts.html
 	
     private void setupShortcut(String name, long stopCode) {
     	Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
     	shortcutIntent.setClass(this, ArrivalTimeActivity.class);
         shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         shortcutIntent.putExtra("StopCode", stopCode);
 
         // Then, set up the container intent (the response to the launcher)
         Intent intent = new Intent();
         intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
         intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
         Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this,  R.drawable.icon72);
         intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
 
         // Now return the result to the launcher
         setResult(RESULT_OK, intent);
     }
 }
