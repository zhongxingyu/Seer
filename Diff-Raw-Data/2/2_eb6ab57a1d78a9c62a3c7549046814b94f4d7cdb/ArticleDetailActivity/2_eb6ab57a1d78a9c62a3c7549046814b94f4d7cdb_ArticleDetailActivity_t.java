 package edu.grinnell.sandb;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.grinnell.grinnellsandb.R;
 
 public class ArticleDetailActivity extends SherlockFragmentActivity {
 	
 	public static final String DETAIL_ARGS = "detail_args";
 	public static final String TAG = "ArticleDetailActivity";
 	
 	@Override
 	public void onCreate(Bundle ofJoy) {
 		super.onCreate(ofJoy);
 		setContentView(R.layout.activity_article_detail);
 		
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         Intent i = getIntent();
         
         Fragment fragment = new ArticleDetailFragment();;
         if (i != null) {
         	Bundle arguments = new Bundle();
         	arguments.putInt(ArticleDetailFragment.ARTICLE_ID_KEY,
         		i.getIntExtra(ArticleDetailFragment.ARTICLE_ID_KEY, 0));
         	fragment.setArguments(arguments);
         } else {
         	Log.e(TAG, "no bundle for fragment..");
         }
         
         getSupportFragmentManager().beginTransaction()
        .replace(R.id.article_detail_container, fragment)
         .commit();
 	}   
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home) {
         	
         	Intent upIntent = new Intent(this, MainActivity.class);
         	upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP 
         			| Intent.FLAG_ACTIVITY_SINGLE_TOP);
             NavUtils.navigateUpTo(this, upIntent);
             
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 }
