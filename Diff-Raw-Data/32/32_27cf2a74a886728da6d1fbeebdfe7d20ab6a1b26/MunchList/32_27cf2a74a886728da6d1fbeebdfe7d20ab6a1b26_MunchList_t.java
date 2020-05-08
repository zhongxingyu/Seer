 package csci498.strupper.munchlist;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MunchList extends ListActivity {
 
   RestaurantAdapter adapter;
   private RestaurantHelper helper;
 
   static class RestaurantHolder {
     private final TextView name, address;
 
     RestaurantHolder(View row) {
       name = (TextView)row.findViewById(R.id.title);
       address = (TextView)row.findViewById(R.id.address);
     }
 
     void populateFrom(Cursor c) {
       Restaurant r = RestaurantHelper.restaurantOf(c);
       name.setText(r.getName());
       address.setText(r.getAddress());
     }
   }
 
   class RestaurantAdapter extends CursorAdapter {
     public RestaurantAdapter(Cursor c) {
       super(MunchList.this, c, true);
     }
 
     @Override
     public void bindView(View row, Context context, Cursor c) {
       ((RestaurantHolder)row.getTag()).populateFrom(c);
 
     }
 
     @Override
     public View newView(Context context, Cursor c, ViewGroup parent) {
       LayoutInflater inflater = MunchList.this.getLayoutInflater();
       View row = inflater.inflate(R.layout.restaurant_row, null);
       RestaurantHolder holder = new RestaurantHolder(row);
       row.setTag(holder);
       return row;
     }
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     helper = new RestaurantHelper(this);
     adapter = new RestaurantAdapter(helper.getAll());
     setListAdapter(adapter);
   }
 
   @Override protected void onDestroy() {
     super.onDestroy();
     helper.close();
   }
 
  @Override protected void onResume() {
    super.onResume();
    // refresh list
    adapter = new RestaurantAdapter(helper.getAll());
    setListAdapter(adapter);
  }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     getMenuInflater().inflate(R.menu.activity_munch_list, menu);
     return super.onCreateOptionsMenu(menu);
   }
 
   @Override public boolean onOptionsItemSelected(MenuItem item) {
     if (item.getItemId() == R.id.add_restaurant) {
       startActivity(new Intent(this, EditRestaurant.class));
       return true;
     }
 
     return super.onOptionsItemSelected(item);
   }
 
   public final static String ID_EXTRA = "apt.tutorial._ID";
 
   @Override
   public void onListItemClick(ListView list, View view,
                               int position, long id) {
     Intent i = new Intent(this, EditRestaurant.class);
     i.putExtra(ID_EXTRA, String.valueOf(id));
     startActivity(i);
   }
 
 }
