 package fr.ybo.ybotv.android.activity;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.widget.DatePicker;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.adapter.CeSoirViewFlowAdapter;
 import fr.ybo.ybotv.android.modele.Channel;
 import fr.ybo.ybotv.android.util.AdMobUtil;
 import org.taptwo.android.widget.TitleFlowIndicator;
 import org.taptwo.android.widget.ViewFlow;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 public class CeSoirActivity extends MenuManager.AbstractSimpleActivity implements DatePickerDialog.OnDateSetListener {
 
     private Calendar currentDate;
 
     private CeSoirViewFlowAdapter adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.flow);
         createMenu();
         currentDate = Calendar.getInstance();
 
         ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
         adapter = new CeSoirViewFlowAdapter(this, currentDate);
         viewFlow.setAdapter(adapter);
         TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
         indicator.setTitleProvider(adapter);
         viewFlow.setFlowIndicator(indicator);
 
         AdMobUtil.manageAds(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.cesoirmenu, menu);
         return true;
     }
 
 
     @Override
     public int getMenuIdOfClass() {
         return R.id.menu_cesoir;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_choixDate) {
             showDialog(R.id.dialog_choixDate);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         if (id == R.id.dialog_choixDate) {
             return new DatePickerDialog(this, this, currentDate.get(Calendar.YEAR),
                     currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
         }
         return super.onCreateDialog(id);
     }
 
     @Override
     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
         currentDate.set(Calendar.YEAR, year);
         currentDate.set(Calendar.MONTH, monthOfYear);
         currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         adapter.changeCurrentDate(currentDate);
     }
 }
 
