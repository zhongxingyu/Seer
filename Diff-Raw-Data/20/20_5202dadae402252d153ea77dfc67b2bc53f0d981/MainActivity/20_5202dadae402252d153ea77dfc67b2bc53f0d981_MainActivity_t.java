 package pt.uminho.myenergy;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
import android.widget.Toast;
 
import com.actionbarsherlock.app.SherlockActivity;
 
 import java.util.List;
 
 
public class MainActivity extends SherlockActivity {
 
     private static final String TAG = MainActivity.class.getName();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
 
 
 
        RestClient client = new RestClient();

        List<FoodCategory> categories = client.getFoodCategories();
 
        Toast.makeText(this, "working", Toast.LENGTH_LONG).show();
         Log.d(TAG, "break");
     }
 
 
     void crashMe(View b) {
         Integer integers[] = new Integer[Integer.MAX_VALUE];
     }
 }
