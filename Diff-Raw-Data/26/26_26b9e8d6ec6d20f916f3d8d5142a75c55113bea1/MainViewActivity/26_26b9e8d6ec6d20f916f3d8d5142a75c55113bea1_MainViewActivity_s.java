 package great.team;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.GridView;
 
 // removed SystemUIHider you can revert this link 
 // http://stackoverflow.com/questions/15186111/fullscreen-activity-wizard-activity-how-do-i-stop-actionbar-from-showing-when-i
 public class MainViewActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main_view);
 
         final View contentView = findViewById(R.id.catalogGridView);
         GridView gv = (GridView)(contentView);
        
        gv.setAdapter(new CatalogAdapter(getApplicationContext()));
         gv.setNumColumns(2);
     }
 
 }
