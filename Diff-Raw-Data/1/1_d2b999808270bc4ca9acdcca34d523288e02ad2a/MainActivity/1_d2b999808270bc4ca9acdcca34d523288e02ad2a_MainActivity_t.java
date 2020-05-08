 package edu.mit.mobile.android.livingpostcards;
 
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.format.DateUtils;
 
 import com.actionbarsherlock.ActionBarSherlock;
 import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
 import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.mit.mobile.android.livingpostcards.data.Card;
 
 public class MainActivity extends FragmentActivity implements OnCreateOptionsMenuListener,
         OnOptionsItemSelectedListener {
 
     private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
        setTitle("");
         super.onCreate(savedInstanceState);
         mSherlock.setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         mSherlock.getMenuInflater().inflate(R.menu.activity_main, menu);
 
         return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(android.view.Menu menu) {
         return mSherlock.dispatchCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(android.view.MenuItem item) {
         return mSherlock.dispatchOptionsItemSelected(item);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.new_card:
                 createNewCard();
 
                 return true;
         }
         return false;
     }
 
     @Override
     protected void onTitleChanged(CharSequence title, int color) {
         mSherlock.dispatchTitleChanged(title, color);
         super.onTitleChanged(title, color);
     }
 
     private void createNewCard() {
 
         final ContentResolver cr = getContentResolver();
 
         final Uri card = Card.createNewCard(cr,
                 DateUtils.formatDateTime(this, System.currentTimeMillis(),
                         DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));
 
         final Intent intent = new Intent(CameraActivity.ACTION_ADD_PHOTO, card);
 
         startActivity(intent);
     }
 
 }
