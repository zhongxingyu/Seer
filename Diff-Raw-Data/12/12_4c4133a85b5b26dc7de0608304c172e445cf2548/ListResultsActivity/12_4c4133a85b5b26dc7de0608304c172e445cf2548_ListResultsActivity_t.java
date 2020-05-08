 package edu.umn.pinkpanthers.beerfinder.activities;
 
 import java.util.List;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import edu.umn.pinkpanthers.beerfinder.R;
 import edu.umn.pinkpanthers.beerfinder.data.Venue;
 import edu.umn.pinkpanthers.beerfinder.network.BeerFinderWebService;
 
 /**
  * Displays a list of Venues to select.
  */
 public class ListResultsActivity extends ListActivity {
 
     private VenueAdapter venueAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.list_results_screen);
         initListView();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         venueAdapter.setList(getSearchableVenueList());
         venueAdapter.notifyDataSetChanged();
     }
 
     private void initListView() {
         if (venueAdapter == null) {
             venueAdapter = new VenueAdapter(this, R.layout.list_results_item, getSearchableVenueList());
             setListAdapter(venueAdapter);
         }
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
 
         lv.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 ListResultsActivity.this.startVenueActivity(((VenueAdapter) getListAdapter()).getItem(position));
             }
 
         });
     }
 
     private List<Venue> getSearchableVenueList() {
         List<Venue> searchableVenus = BeerFinderWebService.getInstance().getSearchableVenueList();
         return searchableVenus;
     }
 
     public class VenueAdapter extends ArrayAdapter<Venue> {
 
         public VenueAdapter(Context context, int textViewResourceId, List<Venue> Venues) {
             super(context, textViewResourceId, Venues);
         }
 
         public void setList(List<Venue> VenueList) {
             clear();
             for (Venue Venue : VenueList) {
                 add(Venue);
             }
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             Venue venue = getItem(position);
             View item = getLayoutInflater().inflate(R.layout.list_results_item, parent, false);
             ((TextView) item.findViewById(R.id.list_results_item_name)).setText(venue.getName());
             ((TextView) item.findViewById(R.id.list_results_item_address)).setText(venue.getAddress());
 
             return item;
         }
     }
 
     public void homeClicked(View view) {
         finish();
     }
 
     private void startVenueActivity(Venue selectedVenue) {
         Intent venueIntent = new Intent(getApplicationContext(), VenueActivity.class);
         venueIntent.putExtra(Venue.SELECTED_VENUE, selectedVenue);
         startActivity(venueIntent);
     }
 
 }
