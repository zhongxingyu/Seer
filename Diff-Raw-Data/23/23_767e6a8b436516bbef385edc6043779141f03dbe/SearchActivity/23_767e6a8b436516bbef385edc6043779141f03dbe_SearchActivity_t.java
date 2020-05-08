 package ua.avtopoisk.activites;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
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
 
    @ViewById(R.id.brands)
    protected Spinner brands;

    private ArrayAdapter<String> adapter;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         AvtopoiskParser parser = new AvtopoiskParser();
         ArrayList<String> brandsList = null;
         try {
             brandsList = new ArrayList<String>(parser.getBrands().keySet());
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, brandsList) ;
    }

    @AfterViews
    protected void init() {
         brands.setAdapter(adapter);
     }
 
     @Click(R.id.btn_find)
     public void btnFindOnClick(View view) {
         final Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
         startActivity(intent);
     }
 }
