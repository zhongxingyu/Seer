 package org.minyanmate.minyanmate;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.widget.ExpandableListView;
 
 import org.minyanmate.minyanmate.adapters.ScheduleExpandableListAdapter;
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.models.ContactSchedule;
 import org.minyanmate.minyanmate.models.MinyanSchedule;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * This class acts in two purposes. Its first purpose is to allow direct management of an
  * individual contact that has already been added to the database. In this case, the contact
  * will be queried from the database and pre-populated into the expandable list. Its second purpose
  * is to act as the final step in a "wizard" to add a new contact to multiple schedules at once.
  *
  * To this end, the class should check its Intent bundles for information about whether it's
  * acting in the first capacity or the second.
  *
  * Created by Jeff on 3/17/14.
  */
 public class ContactManagerActivity extends FragmentActivity
     implements LoaderManager.LoaderCallbacks<Cursor> {
 
     public ContactManagerActivity() {
         super();
     }
 
     // Expandable ListView and adapter
     private ExpandableListView expListView;
     private ScheduleExpandableListAdapter listAdapter;
     // Variables for expandablelistviewadapter
     private List<String> listDataHeader;
     private HashMap<String, List<MinyanSchedule>> listDataChild;
 
     // other variables
     public static final String PHONE_ID = "phoneNumberId";
     private int phoneNumberId;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.fragment_minyan_list);
 
         // Restore state
         phoneNumberId = getIntent().getIntExtra(PHONE_ID, 0);
 
         // phoneNumberId should *always* have a positive value
         assert (phoneNumberId != 0);
 
         // Loader Callbacks
         getSupportLoaderManager().initLoader(0, null, this);
 
         // Adapter
         expListView = (ExpandableListView) findViewById(R.id.minyanList);
 
         listDataHeader = new ArrayList<String>();
         listDataChild = new HashMap<String, List<MinyanSchedule>>();
         listAdapter = new ScheduleExpandableListAdapter(this, listDataHeader, listDataChild,
                 new ScheduleExpandableListAdapter.ContactScheduleAdapterCallbacks(phoneNumberId));
 
         expListView.setAdapter(listAdapter);
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
         CursorLoader cursorLoader = new CursorLoader(this,
                 Uri.parse(MinyanMateContentProvider.CONTENT_URI_CONTACT_SCHEDULES + "/" + phoneNumberId),
                 null, null, null, null);
         return cursorLoader;
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 
         List<ContactSchedule> prayerTimes = ContactSchedule.contactScheduleListFromCursor(data);
         listDataChild = new HashMap<String, List<MinyanSchedule>>();
         listDataHeader = new ArrayList<String>();
         for(MinyanSchedule prayer : prayerTimes) {
             // if new Day, add it to headers and create a new map entry
             if( !listDataHeader.contains(prayer.getDay())) {
                 listDataHeader.add(prayer.getDay());
                 List<MinyanSchedule> temp = new ArrayList<MinyanSchedule>();
                 temp.add(prayer);
                 listDataChild.put(prayer.getDay(), temp);
 
             } else // else it already exists and just add to entry
             {
                 listDataChild.get(prayer.getDay()).add(prayer);
             }
         }
         listAdapter.setDataChildren(listDataChild);
         listAdapter.setListDataHeader(listDataHeader);
         listAdapter.notifyDataSetChanged();
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         loader = null;
     }
 }
