 package ojag.toilet.okinawa;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
 
 public class Info extends Activity {
 
     private Bundle ex;
 
     public class click implements OnClickListener {
 
         public void onClick(View v) {
             switch (v.getId()) {
            case R.id.img_url:
                 Intent i = new Intent();
                 i.setAction(Intent.ACTION_VIEW);
                 i.setData(Uri.parse("http://untikun.heroku.com/"));
                 startActivityForResult(i, 0);
                 break;
 
            case R.id.img_start:
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
 
        ImageView img_url = (ImageView) findViewById(R.id.img_url);
        img_url.setOnClickListener(new click());
 
        ImageView img_start = (ImageView) findViewById(R.id.img_start);
        img_start.setOnClickListener(new click());
     }
 }
