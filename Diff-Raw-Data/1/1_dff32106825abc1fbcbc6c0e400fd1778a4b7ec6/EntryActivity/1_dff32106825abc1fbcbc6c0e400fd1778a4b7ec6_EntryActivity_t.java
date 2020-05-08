 package de.thiemonagel.vegdroid;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public class EntryActivity extends Activity {
     private Venue mVenue;
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:  // no idea what this is for
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             case R.id.menu_about:
                 Intent intent = new Intent(this, AboutActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_entry);
 
         Intent i = getIntent();
         int VenueId = i.getIntExtra( "VenueId", -1 );
         mVenue = Global.getInstance(this).venues.get(VenueId);
         if ( mVenue == null ) {
             // TODO: implement error message
            return;
         }
 
         {
             TextView tv = (TextView) findViewById(R.id.name);
             tv.setText( mVenue.name );
         }{
             RatingBar rb = (RatingBar) findViewById(R.id.ratingBar2);
             rb.setRating( mVenue.rating );
         }{
 //            TextView tv = (TextView) findViewById(R.id.veg_level_description);
 //            tv.setText( mVenue.get("veg_level_description") );
         }{
             Button but = (Button) findViewById(R.id.phone);
             if ( mVenue.phone.equals("") )
                 but.setVisibility( View.GONE );
             else
                 but.setText( "Dial " + mVenue.phone );
         }{
             Button but = (Button) findViewById(R.id.website);
             if ( mVenue.website.equals("") )
                 but.setVisibility( View.GONE );
             else
                 //but.setText( "Visit " + fData.get("website") );
                 but.setText( "Visit web site" );
         }{
             TextView tv = (TextView) findViewById(R.id.address);
             String addressBlock = mVenue.locHtml();
             if ( addressBlock.equals("") )
                 tv.setVisibility( View.GONE );
             else
                 tv.setText( Html.fromHtml(addressBlock) );
         }{
             TextView tv = (TextView) findViewById(R.id.long_description);
             tv.setMovementMethod( LinkMovementMethod.getInstance() );
             if ( mVenue.longDescription.equals("") )
                 tv.setVisibility( View.GONE );
             else
                 tv.setText( Html.fromHtml( mVenue.longDescription ) );
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_entry, menu);
         return true;
     }
 
     public void clickMap( View view ) {
         // the name of the venue is not included in the query string because
         // it seems to cause problems when Google isn't aware of the specific venue
         String uri = "geo:0,0?q=" + mVenue.locString();
         Intent intent = new Intent( Intent.ACTION_VIEW );
         intent.setData( Uri.parse(uri) );
         startActivity(intent);
     }
 
     public void clickPhone( View view ) {
         String uri = "tel:" + mVenue.phone;
         Intent intent = new Intent( Intent.ACTION_DIAL );
         intent.setData( Uri.parse(uri) );
         startActivity(intent);
     }
 
     public void clickWebsite( View view ) {
         Intent intent = new Intent( Intent.ACTION_VIEW );
         intent.setData( Uri.parse(mVenue.website) );
         startActivity(intent);
     }
 }
