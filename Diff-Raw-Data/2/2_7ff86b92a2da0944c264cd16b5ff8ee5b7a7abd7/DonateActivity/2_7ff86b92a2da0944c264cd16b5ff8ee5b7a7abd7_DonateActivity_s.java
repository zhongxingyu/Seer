 package uk.co.brightec.alphaconferences.more;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import uk.co.brightec.alphaconferences.AlphaAdapter;
 import uk.co.brightec.alphaconferences.Constants;
 import uk.co.brightec.alphaconferences.Row;
 import uk.co.brightec.alphaconferences.data.Conference;
 import uk.co.brightec.alphaconferences.data.DataStore;
 import uk.co.brightec.alphaconferences.rows.ButtonBarRow;
 import uk.co.brightec.alphaconferences.rows.HTMLRow;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 
 public class DonateActivity extends SherlockListActivity {
     
     private ActionBar mActionBar;
     
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mActionBar = getSupportActionBar(); 
         mActionBar.setTitle("Donate");
         mActionBar.setDisplayHomeAsUpEnabled(true);
 
         AlphaAdapter adapter = new AlphaAdapter();
         setListAdapter(adapter);
     }
     
 
     @Override
     protected void onResume() {
         super.onResume();
         populate();
     }
 
 
     private void populate() {
        final Conference conference = DataStore.conference(this);
 
         List<Row> rows = new ArrayList<Row>();
         rows.add(new HTMLRow(conference.donationDescription, this));
 
         View.OnClickListener donateBySmsHandler = null;
         if (StringUtils.isNotBlank(conference.donationTelephoneNumber)) {
             donateBySmsHandler = new OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:"+conference.donationTelephoneNumber));
                     //intent.putExtra("sms_body", "HLCG12 £");
                     startActivity(intent);
                 }
             };
         }
 
         View.OnClickListener donateOnlineHandler = null;
         if (StringUtils.isNotBlank(conference.donationUrl)) {
             donateOnlineHandler = new View.OnClickListener() {
                 public void onClick(View v) {
                     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(conference.donationUrl)));
                 }
             };
         }
 
         ButtonBarRow buttons = new ButtonBarRow(this);
         buttons.setButton1("Donate by SMS", donateBySmsHandler);
         buttons.setButton2("Donate online", donateOnlineHandler);
         rows.add(buttons);
 
         ((AlphaAdapter) getListAdapter()).setRows(rows, this);
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             finish();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
 
 }
