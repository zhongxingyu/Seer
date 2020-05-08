 
 package hk.hku.cs.srli.factfinder;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import hk.hku.cs.srli.factfinder.DummyData.Category;
 import hk.hku.cs.srli.factfinder.DummyData.FactItem;
 
 public class DetailActivity extends SherlockActivity {
     
     private FactItem mFact;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_detail);
         
         // get intent data
         Bundle b = getIntent().getExtras();
  
         // Selected image id
         int id = b.getInt("id", -1);
         int section = b.getInt("section");
         mFact = DummyData.getInstance(getResources()).getItem(Category.of(section), id);
         
         setTitle(mFact.title);
         TextView text = (TextView) findViewById(R.id.content);
         text.setText(mFact.content);
        ImageButton image = (ImageButton) findViewById(R.id.image_view);
         int thumbId = getResources().getIdentifier(mFact.thumb, "drawable", getPackageName());
         image.setImageResource(thumbId);
         image.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 ActionBar ab = getSupportActionBar();
                 if (ab.isShowing()) {
                     ab.hide();
                 } else {
                     ab.show();
                 }
             }
         });
 
         // Show the Up button in the action bar.
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getSupportMenuInflater().inflate(R.menu.detail, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 Intent upIntent = getParentActivityIntent();
                 // return to the exisiting parent activity instead of creating a new one.
                 upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                 navigateUpTo(upIntent);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
