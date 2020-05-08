 package pincrack;
 
 import com.pincrack.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 
 public class ResultActivity extends Activity {
     /** Called when the activity is first created. */
     int counter;
     Intent viewLogs;
 
     @Override
     public void onCreate (Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.result);
         viewLogs = new Intent(ResultActivity.this, ViewLogsActivity.class);
 
     }
         
     /**
      * Helper method to restart the application
      * @param view
      */
     public void startOver (View view) {
         Intent startOver = new Intent(ResultActivity.this, InputActivity.class);
         ResultActivity.this.startActivity(startOver);
         finish();
     }
     
     public void view25 (View view) {
     	viewLogs.putExtra("size", 25);
         ResultActivity.this.startActivity(viewLogs);
     }
     
     public void view50 (View view) {
     	viewLogs.putExtra("size", 50);
         ResultActivity.this.startActivity(viewLogs);
     }
     
     public void view100 (View view) {
     	viewLogs.putExtra("size", 100);
         ResultActivity.this.startActivity(viewLogs);
     }
 }
