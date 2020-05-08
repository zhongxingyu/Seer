 package ua.avtopoisk.activites;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import parsers.AvtopoiskParser;
 import ua.avtopoisk.R;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Start activity. Search params located here
  *
  * @author ibershadskiy <a href="mailto:iBersh20@gmail.com">Ilya Bershadskiy</a>
  * @since 12.10.12
  */
 @EActivity(R.layout.search)
 public class SearchActivity extends Activity{
 
     @Override
    protected void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         AvtopoiskParser parser = new AvtopoiskParser();
        Spinner brands = (Spinner) findViewById(R.id.brands);
        ArrayAdapter<String> adapter = null;
         ArrayList<String> brandsList = null;
         try {
             brandsList = new ArrayList<String>(parser.getBrands().keySet());
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
        adapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, brandsList) ;
         brands.setAdapter(adapter);
     }
 
     @Click(R.id.btn_find)
     public void btnFindOnClick(View view) {
         final Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
         startActivity(intent);
     }
 }
