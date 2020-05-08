 package se.chalmers.watchme.activity;
 
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.database.DatabaseAdapter;
 import se.chalmers.watchme.ui.MovieListFragment;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.NavUtils;
import android.view.Menu;
 import android.view.MenuItem;
 
 public class TagMovieListActivity extends FragmentActivity {
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         //Recieve tagId sent from TagListFragment
         Intent intent = getIntent();
         Long tagId = intent.getLongExtra(MainActivity.TAG_ID, -1);
         
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction ft = fragmentManager.beginTransaction();
         
         ft.add(android.R.id.content, new MovieListFragment(tagId));
         ft.commit();
 	}
 	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        MenuItem mailItem = menu.findItem(R.id.menu_send_email_button);
		mailItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }
	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 	
 	@Override
 	public void onBackPressed() {
 	    this.finish();
 	    System.out.println("--- HR ----");
 	    overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
 	    return;
 	}
 	
 }
