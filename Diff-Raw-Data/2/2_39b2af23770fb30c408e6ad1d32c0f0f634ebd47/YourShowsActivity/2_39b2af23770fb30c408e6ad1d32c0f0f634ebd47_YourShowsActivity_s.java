 package com.showrss.activitys;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.showrss.AllShows;
 import com.showrss.R;
 import com.showrss.YourShows;
 
 import android.app.Activity;
 import android.app.ExpandableListActivity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.SimpleExpandableListAdapter;
 
 public class YourShowsActivity extends ExpandableListActivity{
 	
 	private static final String TAG = "YourShowActivity";
 	
 	static final String headings[] = {
 		  "Subscribed Shows"
 		};
 	
 	 /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.yourshows);
         
         AllShows.populateAllShows();
         
         YourShows shows = new YourShows();
         shows.getShows();
         
 		SimpleExpandableListAdapter expListAdapter =
 				new SimpleExpandableListAdapter(
 					this,
 					createGroupList(),	// groupData describes the first-level entries
 					R.layout.child_row,	// Layout for the first-level entries
 					new String[] { "headingName" },	// Key in the groupData maps to display
 					new int[] { R.id.showname },		// Data under "colorName" key goes into this TextView
 					createChildList(),	// childData describes second-level entries
 					R.layout.child_row,	// Layout for second-level entries
 					new String[] { "showName" },	// Keys in childData maps to display
 					new int[] { R.id.showname }	// Data under the keys above go into these TextViews
 				);
 			setListAdapter( expListAdapter );
         
         Log.d(TAG, shows.shows.toString());
     }
 
 
 
 /**
  * Creates the group list out of the headings[] array according to
  * the structure required by SimpleExpandableListAdapter. The resulting
  * List contains Maps. Each Map contains one entry with key "colorName" and
  * value of an entry in the headings[] array.
  */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private List createGroupList() 
 	{
 		ArrayList result = new ArrayList();
 		for( int i = 0 ; i < headings.length ; ++i )
 		{
 			HashMap m = new HashMap();
 			m.put( "headingName",headings[i] );
 			result.add( m );
 		}
 		return (List)result;
 	}
 
 /**
  * Creates the child list out of the shades[] array according to the
  * structure required by SimpleExpandableListAdapter. The resulting List
  * contains one list for each group. Each such second-level group contains
  * Maps. Each such Map contains two keys: "shadeName" is the name of the
  * shade and "rgb" is the RGB value for the shade.
  */
  @SuppressWarnings({ "rawtypes", "unchecked" })
 private List createChildList() {
 	ArrayList result = new ArrayList();
 	String children[][]={YourShows.showsAsArray()};
 	for( int i = 0 ; i < children.length ; ++i ) {
 		//Second-level lists
 	  ArrayList secList = new ArrayList();
	  for( int n = 0 ; n < children[i].length ; n += 2 ) {
 	    HashMap child = new HashMap();
 		child.put( "showName", children[i][n] );
 		secList.add( child );
 	  }
 	  result.add( secList );
 	}
 	return result;
  }
 }
