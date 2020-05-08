 package com.thoughtworks.pumpkin.adapter;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.View;
 import android.widget.AlphabetIndexer;
 import android.widget.ImageView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import com.fedorvlasov.lazylist.ImageLoader;
 import com.thoughtworks.pumpkin.R;
 
 public class BooksCursor extends SimpleCursorAdapter {
     AlphabetIndexer alphaIndexer;
     ImageLoader imageLoader;
 
     public BooksCursor(Context context, int layout, Cursor c, String[] from, int[] to) {
         super(context, layout, c, from, to);
         alphaIndexer = new AlphabetIndexer(c, 1, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
         imageLoader = new ImageLoader(context);
 
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
       ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);
         imageLoader.DisplayImage(cursor.getString(cursor.getColumnIndex("bookImage")), imageView);
        TextView textView = (TextView) view.findViewById(R.id.title) ;
         setViewText(textView, cursor.getString(cursor.getColumnIndex("Title")));
     }
 }
