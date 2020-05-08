 package pl.byd.promand.Team2.otherActivities;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Spinner;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.promand.Team2.R;
 
 public class SearchEditActivity extends SherlockActivity {
     Spinner spinner;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.search);
         spinner = (Spinner)findViewById(R.id.sp_type);
         spinner.setOnItemSelectedListener(spinnerListener);
     }
     private  AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener(){
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
             Object item = parent.getItemAtPosition(pos);
             if(item.equals("Visit")){
                 Button btn = (Button)findViewById(R.id.btn_add);
                 btn.setEnabled(false);
             }
             else {
                 Button btn = (Button)findViewById(R.id.btn_add);
                 btn.setEnabled(true);
             }
         }
         public void onNothingSelected(AdapterView<?> parent) {
         }
 
     };
 }
