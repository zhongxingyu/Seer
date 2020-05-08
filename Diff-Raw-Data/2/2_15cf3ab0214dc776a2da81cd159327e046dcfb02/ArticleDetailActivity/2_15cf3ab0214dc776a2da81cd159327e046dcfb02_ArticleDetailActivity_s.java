	package edu.grinnell.sandb;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 import edu.grinnell.grinnellsandb.R;
 
 public class ArticleDetailActivity extends FragmentActivity {
 	
 	@Override
 	public void onCreate(Bundle ofJoy) {
 		super.onCreate(ofJoy);
 		setContentView(R.layout.activity_article_detail);
 		
         getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		if (ofJoy == null) {
             Bundle arguments = new Bundle();
             arguments.putInt(ArticleDetailFragment.ARTICLE_ID_KEY,
                     getIntent().getIntExtra(ArticleDetailFragment.ARTICLE_ID_KEY, 0));
             ArticleDetailFragment fragment = new ArticleDetailFragment();
             fragment.setArguments(arguments);
             getSupportFragmentManager().beginTransaction()
                     .add(R.id.article_detail_container, fragment)
                     .commit();
         }
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
