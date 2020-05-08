 package com.ubikod.urbantag;
 
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.ubikod.urbantag.layout.FlowLayout;
 import com.ubikod.urbantag.model.DatabaseHelper;
 import com.ubikod.urbantag.model.Place;
 import com.ubikod.urbantag.model.PlaceManager;
 import com.ubikod.urbantag.model.Tag;
 
 public class PlaceListActivity extends SherlockListActivity
 {
   public static final String MODE = "mode";
   public static final int MODE_PLACES_IDS = 0;
   public static final int MODE_TAGS_IDS = 1;
   public static final String PLACES_IDS = "places_ids";
   public static final String TAGS_IDS = "tags_ids";
 
   private DatabaseHelper mDbHelper;
   private PlaceManager mPlaceManager;
   private LayoutInflater mInflater;
 
   private List<Place> mPlaces;
 
   private static class ViewHolder
   {
     FlowLayout tagContainer;
     TextView nameView;
   }
 
   private static class TagBundle
   {
     ViewHolder viewHolder;
     Place place;
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     setTitle(R.string.menu_select_place);
     com.actionbarsherlock.app.ActionBar actionBar = this.getSupportActionBar();
     actionBar.setDisplayHomeAsUpEnabled(true);
 
     mDbHelper = new DatabaseHelper(this, null);
     mPlaceManager = new PlaceManager(mDbHelper);
     mInflater = LayoutInflater.from(this);
 
     Bundle extras = getIntent().getExtras();
 
     if (extras != null && extras.getInt(MODE, -1) == MODE_PLACES_IDS)
     {
       int[] ids;
       if ((ids = extras.getIntArray(PLACES_IDS)) != null)
       {
         this.mPlaces = mPlaceManager.get(ids);
       }
       else
       {
         Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
         finish();
         return;
       }
 
     }
     else if (extras != null && extras.getInt(MODE, -1) == MODE_TAGS_IDS)
     {
       int[] ids;
       if ((ids = extras.getIntArray(TAGS_IDS)) != null)
       {
         this.mPlaces = mPlaceManager.getAllForTags(ids);
       }
       else
       {
         Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
         finish();
         return;
       }
     }
     else
     {
       Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
       finish();
       return;
     }
 
     if (this.mPlaces.size() == 0)
     {
       Toast.makeText(this, R.string.no_matching_place, Toast.LENGTH_SHORT).show();
       finish();
       return;
     }
     setListAdapter(createAdapter());
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
     switch (item.getItemId())
     {
       case android.R.id.home:
         finish();
         break;
     }
     return false;
   }
 
   private BaseAdapter createAdapter()
   {
     return new BaseAdapter()
     {
       @Override
       public View getView(int position, View convertView, ViewGroup parent)
       {
         ViewHolder holder;
         TagBundle tagBundle;
 
         Place p = mPlaces.get(position);
 
         if (convertView == null)
         {
           convertView = mInflater.inflate(R.layout.place_row, null);
 
           holder = new ViewHolder();
           holder.nameView = (TextView) convertView.findViewById(R.id.name);
           holder.tagContainer = (FlowLayout) convertView.findViewById(R.id.tag_container);
 
           tagBundle = new TagBundle();
           tagBundle.viewHolder = holder;
 
           convertView.setTag(tagBundle);
         }
         else
         {
           tagBundle = (TagBundle) convertView.getTag();
           holder = tagBundle.viewHolder;
         }
 
         tagBundle.place = p;
 
         holder.nameView.setText(p.getName());
 
         for (Tag t : p.getAllTags())
         {
           TextView tag = new TextView(getApplicationContext());
           tag.setTextColor(Color.WHITE);
           tag.setBackgroundColor(t.getColor());
           tag.setText(t.getValue());
           tag.setPadding(5, 5, 5, 5);
           holder.tagContainer.addView(tag, new FlowLayout.LayoutParams(10, 10));
         }
         return convertView;
       }
 
       @Override
       public long getItemId(int position)
       {
         return position;
       }
 
       @Override
       public Object getItem(int position)
       {
         return mPlaces.get(position);
       }
 
       @Override
       public int getCount()
       {
         return mPlaces.size();
       }
     };
   }
 
   @Override
   public void onResume()
   {
     super.onResume();
     Common.onResume(this);
   }
 
   @Override
   public void onPause()
   {
     super.onPause();
     Common.onPause(this);
   }
 
   @Override
   public void onListItemClick(ListView l, View v, int position, long id)
   {
     Intent intent = new Intent(this, ContentsListActivity.class);
    intent.putExtra(ContentsListActivity.MODE, ContentsListActivity.MODE_PLACE);
     intent.putExtra(ContentsListActivity.PLACE_ID, ((TagBundle) v.getTag()).place.getId());
     startActivity(intent);
   }
 }
