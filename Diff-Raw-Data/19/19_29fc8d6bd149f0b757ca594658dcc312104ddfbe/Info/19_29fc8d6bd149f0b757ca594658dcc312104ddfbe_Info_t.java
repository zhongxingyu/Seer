 package ojag.toilet.okinawa;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.LinearLayout;
 
 public class Info extends Activity {
 
     private Bundle ex;
 
     public class click implements OnClickListener {
 
         public void onClick(View v) {
             switch (v.getId()) {
            case R.id.l02:
                 Intent i = new Intent();
                 i.setAction(Intent.ACTION_VIEW);
                 i.setData(Uri.parse("http://untikun.heroku.com/"));
                 startActivityForResult(i, 0);
                 break;
 
            case R.id.l03:
                 if (ex.getString("FROM").equals("From_StartActivity")) {
                     Intent i2 = new Intent(getApplicationContext(),
                             ToileActivity.class);
                     startActivity(i2);
                 } else {
                     finish();
                 }
                 break;
             }
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.info);
         ex = getIntent().getExtras();
 
        LinearLayout l02 = (LinearLayout)findViewById(R.id.l02);
        l02.setOnClickListener(new click());
 
        LinearLayout l03 = (LinearLayout)findViewById(R.id.l03);
        l03.setOnClickListener(new click());
     }
 }
