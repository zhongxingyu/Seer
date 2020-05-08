 package io.github.axxis.places.sample;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.Toast;
 
 import io.github.axxiss.places.model.Place;
 
 /**
  * Created by alexis on 2/23/14.
  */
 public class PlaceActivity extends ListActivity {
 
     private static final String KEY_PLACES = "KEY_PLACES";
 
     public static void newInstance(Context source, Place[] places) {
         Intent intent = new Intent(source, PlaceActivity.class);
         intent.putExtra(KEY_PLACES, places);
         source.startActivity(intent);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Bundle extras = getIntent().getExtras();
 
        Place[] places = (Place[]) extras.getParcelableArray("places");
 
         if (places == null) {
             Toast.makeText(this, "I have no places...", Toast.LENGTH_LONG).show();
             return;
         }
 
         setListAdapter(new PlaceAdapter(this, places));
 
     }
 
 
     static class PlaceAdapter extends ArrayAdapter<Place> implements ListAdapter {
         public PlaceAdapter(Context context, Place[] places) {
             super(context, android.R.layout.simple_list_item_1, places);
         }
     }
 }
