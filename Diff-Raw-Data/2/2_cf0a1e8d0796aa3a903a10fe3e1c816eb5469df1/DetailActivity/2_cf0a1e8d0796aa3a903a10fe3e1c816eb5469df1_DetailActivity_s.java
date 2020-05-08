 
 package hk.hku.cs.srli.factfinder;
 
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import java.io.IOException;
 
 import hk.hku.cs.srli.factfinder.DataSet.DataItem;
 
 public class DetailActivity extends SherlockActivity {
     
     private DataItem mFact;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_detail);
         
         // get intent data
         Bundle b = getIntent().getExtras();
  
         // Selected image id
         int id = b.getInt("id", -1);
         int section = b.getInt("section");
         mFact = FFApp.getData(this).getItem(section, id);
         
         setTitle(FFApp.getData(this).getCategoryAt(section).getName());
         TextView title = (TextView) findViewById(R.id.textTitle);
         title.setText(mFact.title);
         
         TextView text = (TextView) findViewById(R.id.content);
         text.setText(mFact.content);
         final ImageButton image = (ImageButton) findViewById(R.id.image_view);
 
         try {
             // Load image from assets
             image.setImageDrawable(
                     Drawable.createFromResourceStream(getResources(), null, 
                             getAssets().open(mFact.thumb), null));
         } catch (IOException e) {
             // Image loading failed, use placeholder instead.
             image.setImageResource(R.drawable.placeholder);
         }
         image.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 ActionBar ab = getSupportActionBar();
                 if (ab.isShowing()) {
                     ab.hide();
                     image.setContentDescription(getString(R.string.hint_fullscreen_exit));
                 } else {
                     ab.show();
                     image.setContentDescription(getString(R.string.hint_fullscreen));
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
             case R.id.action_add_with_edit:
                 AlertDialog dialog = new AlertDialog.Builder(this)
                         .setTitle("Customize a dish")
                         .setMessage("Add this dish:" + mFact.title)
                         .create();
                 dialog.show();
                break;
             case R.id.action_add:
                 FFApp.getOrder(DetailActivity.this).add(mFact);
                 Toast.makeText(DetailActivity.this, "Added to order", Toast.LENGTH_SHORT).show();
                 // continue below and return to home
             case android.R.id.home:
                 Intent upIntent = getParentActivityIntent();
                 // return to the exisiting parent activity instead of creating a new one.
                 upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                 navigateUpTo(upIntent);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
