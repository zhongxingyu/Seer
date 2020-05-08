 /*
  * Copyright (C) 2013 Geometer Plus <contact@geometerplus.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  */
 
 package org.geometerplus.fbreader.widget;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.ListActivity;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.widget.*;
 
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
 
 public class MainActivity extends ListActivity {
 	List<Book> booksList = new ArrayList<Book>();
	private final IBookCollection myCollection = new BookCollectionShadow();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Here should be smth about absolutely path
 		String path = "/files/";
 		booksList.add(new Book("The first book", new File(path + "1.fb2")));
 		booksList.add(new Book("The second book", new File(path + "2.fb2")));
 		booksList.add(new Book("The third book", new File(path + "3.fb2")));
 		
 		ListView llv = new ListView(this);		
 		llv.setId(android.R.id.list);
 		for(Book book: booksList) {
 			TextView tv = new TextView(this);
 			tv.setText(book.getTitle());
 			
 			ImageView iv = new ImageView(this);
 			iv.setAdjustViewBounds(true);
 			iv.setMaxHeight(100);
 			
 			iv.setImageDrawable(Drawable.createFromPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/res/drawable/1.jpeg"));
 			LinearLayout ll = new LinearLayout(this);
 			ll.addView(iv);
 			ll.addView(tv);
 
 			llv.addHeaderView(ll);
 		}
 		
 		ArrayAdapter<ListView> adapter = new ArrayAdapter<ListView>(this, android.R.layout.simple_list_item_1);
 	    this.setContentView(llv);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		System.err.println("MainActivity bindToService");
 		((BookCollectionShadow)myCollection).bindToService(this, new Runnable() {
 			public void run() {
 				// TODO: write here any code you want to execute when collection is ready to use
 				System.err.println("Hooorray! Widget activity is connected to the FBReader library service!");
 				for (org.geometerplus.fbreader.book.Book book : myCollection.books()) {
					if (book != null) {
						System.err.println(book.getTitle());
					} else {
						System.err.println("NULL :(");
					}
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onStop() {
 		((BookCollectionShadow)myCollection).unbind();
 		super.onStop();
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 	    // temporary
 	    Toast.makeText(this, "Hello from " + l.indexOfChild(v), Toast.LENGTH_LONG).show();
 	    finish();
 	}
 }
