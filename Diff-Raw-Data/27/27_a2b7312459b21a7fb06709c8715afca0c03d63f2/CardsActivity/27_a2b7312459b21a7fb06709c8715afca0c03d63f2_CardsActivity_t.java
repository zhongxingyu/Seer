 package com.fawna.dumbo;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.DataSetObserver;
import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 public class CardsActivity extends ListActivity {
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.cardsview);
     CardsAdapter adapter = new CardsAdapter();
     setListAdapter(adapter);
   }
 
   public class CardsAdapter implements ListAdapter {
 
    private int size = 4;
     private View headerView;
     private View description;
     private View actor;
 
     public CardsAdapter() {
       headerView = getLayoutInflater().inflate(R.layout.show_header, null);
       ImageButton imdbButton = (ImageButton) headerView.findViewById(R.id.imdb_button);
       imdbButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
           Intent openImdb = new Intent(Intent.ACTION_VIEW);
           openImdb.setData(Uri.parse("http://www.imdb.com/title/tt0120737/"));
           startActivity(openImdb);
         }
       });
 
       description = getLayoutInflater().inflate(R.layout.plot_point, null);
       actor = getLayoutInflater().inflate(R.layout.actor_card, null);
 
       TextView tv = (TextView) headerView.findViewById(R.id.show_title);
       Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/avenir_next.ttc");
       tv.setTypeface(tf);
 
       layoutCover();
     }
 
     private void layoutCover() {
       Display display = getWindowManager().getDefaultDisplay();
       int screenWidth = display.getWidth();
 
       ImageView cover = (ImageView)headerView.findViewById(R.id.lotr_cover);
       Drawable d = cover.getDrawable();
       int intendedWidth = screenWidth;
       int originalWidth = d.getIntrinsicWidth();
       int originalHeight = d.getIntrinsicHeight();
       float scale = (float)intendedWidth / originalWidth;
       int newHeight = Math.round(originalHeight * scale);
      cover.setLayoutParams(new RelativeLayout.LayoutParams(
              RelativeLayout.LayoutParams.WRAP_CONTENT,
              RelativeLayout.LayoutParams.WRAP_CONTENT));
       cover.getLayoutParams().width = intendedWidth;
       cover.getLayoutParams().height = newHeight;
     }
 
     @Override
     public boolean areAllItemsEnabled() {
       return true;
     }
 
     @Override
     public boolean isEnabled(int position) {
       return true;
     }
 
     @Override
     public void registerDataSetObserver(DataSetObserver observer) {
 
     }
 
     @Override
     public void unregisterDataSetObserver(DataSetObserver observer) {
     }
 
     @Override
     public int getCount() {
       return size;
     }
 
     @Override
     public Object getItem(int position) {
       return null;
     }
 
     @Override
     public long getItemId(int position) {
       return 0;
     }
 
     @Override
     public boolean hasStableIds() {
       return true;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
      if (position == 0) {
         return headerView;
      } else if (position == 1) {
          TextView padding = new TextView(getBaseContext());
          padding.setBackgroundColor(Color.argb(255, 196, 196, 196));
          padding.setHeight(30);
          return padding;
      } else if (position == 2) {
         return description;
      } else {
        return actor;
      }
     }
 
     @Override
     public int getItemViewType(int position) {
       return 0;
     }
 
     @Override
     public int getViewTypeCount() {
       return 1;
     }
 
     @Override
     public boolean isEmpty() {
       return false;
     }
   }
 
 
 
 }
