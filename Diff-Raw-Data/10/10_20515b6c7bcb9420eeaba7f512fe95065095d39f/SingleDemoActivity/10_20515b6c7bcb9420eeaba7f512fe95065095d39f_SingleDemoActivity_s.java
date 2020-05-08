 
 package hk.hku.cs.srli.monkeydemo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 
 import hk.hku.cs.srli.monkeydemo.demo.DemoContent;
 import hk.hku.cs.srli.monkeydemo.demo.DemoFragmentBase;
 import hk.hku.cs.srli.monkeydemo.demo.DemoContent.DemoItem;
 
 /**
  * An activity representing a single demo screen. This activity is only
  * used on handset devices. On tablet-size devices, item details are presented
  * side-by-side with a list of items in a {@link DemoListActivity}.
  * <p>
  * This activity is mostly just a 'shell' activity containing nothing more than
  * a {@link DemoFragmentBase}.
  */
 public class SingleDemoActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_single_demo);
 
         // Show the Up button in the action bar.
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
         // savedInstanceState is non-null when there is fragment state
         // saved from previous configurations of this activity
         // (e.g. when rotating the screen from portrait to landscape).
         // In this case, the fragment will automatically be re-added
         // to its container so we don't need to manually add it.
         if (savedInstanceState == null) {
             // Create the detail fragment and add it to the activity
             // using a fragment transaction.
             final String demoId = getIntent().getStringExtra(DemoFragmentBase.ARG_ITEM_ID);
             DemoItem demo = DemoContent.ITEM_MAP.get(demoId);
             setTitle(demo.title);
             DemoFragmentBase fragment = demo.fragment;
             Bundle arguments = new Bundle();
             arguments.putString(DemoFragmentBase.ARG_ITEM_ID, demoId);
             fragment.setArguments(arguments);
             getFragmentManager().beginTransaction()
                    .add(R.id.single_demo_container, fragment)
                     .commit();
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 NavUtils.navigateUpTo(this, new Intent(this, DemoListActivity.class));
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 }
