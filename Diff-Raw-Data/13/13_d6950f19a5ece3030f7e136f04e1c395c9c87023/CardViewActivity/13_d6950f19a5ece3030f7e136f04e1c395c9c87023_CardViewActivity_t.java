 package edu.mit.mobile.android.livingpostcards;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.view.View.OnClickListener;
 import edu.mit.mobile.android.livingpostcards.data.Card;
 
 public class CardViewActivity extends FragmentActivity implements OnClickListener {
 
     private Uri mCard;
    private CardMediaViewFragment mCardMediaFragment;
 
     @Override
     protected void onCreate(Bundle arg0) {
 

         super.onCreate(arg0);
         setContentView(R.layout.activity_card_view);
 
         mCard = getIntent().getData();
 
         findViewById(R.id.add_frame).setOnClickListener(this);
 
         final FragmentManager fm = getSupportFragmentManager();
 
         final FragmentTransaction ft = fm.beginTransaction();
 
         final Fragment f = fm.findFragmentById(R.id.card_media_viewer);
         if (f != null) {
            mCardMediaFragment = (CardMediaViewFragment) f;
         } else {
            mCardMediaFragment = CardMediaViewFragment.newInstance(Card.MEDIA.getUri(mCard));
            ft.replace(R.id.card_media_viewer, mCardMediaFragment);
         }

         ft.commit();
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.add_frame:
                 startActivity(new Intent(CameraActivity.ACTION_ADD_PHOTO, mCard));
                 break;
         }
 
     }
 
 }
