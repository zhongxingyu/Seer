 package com.brunocascio.expensis;
 
 import java.util.Calendar;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements ActionBar.OnNavigationListener {
 
     /**
      * The serialization (saved instance state) Bundle key representing the
      * current dropdown position.
      */
     private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
     private ExpensiDataSource datasource;
     private EditText description, price;
     private DatePicker calendar;
     private String totalExpensis;
     private TextView capitalActual;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Set up the action bar to show a dropdown list.
         final ActionBar actionBar = getActionBar();
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         // Set up the dropdown list navigation in the action bar.
         actionBar.setListNavigationCallbacks(
                 // Specify a SpinnerAdapter to populate the dropdown list.
                 new ArrayAdapter<String>(
                         actionBar.getThemedContext(),
                         android.R.layout.simple_list_item_1,
                         android.R.id.text1,
                         new String[] {
                         		getString(R.string.nav_add_exp),
                                 getString(R.string.nav_show_exp),
                                 getString(R.string.nav_exit),
                         }),
                 this);
         
         datasource = new ExpensiDataSource(this);
         datasource.open();
         
         totalExpensis = datasource.getExpensesFromMonth(Calendar.getInstance().get(Calendar.MONTH)).toString();
         
         capitalActual = (TextView) findViewById(R.id.total_cap);
         capitalActual.setText("$ "+totalExpensis);
         
         calendar = (DatePicker) findViewById(R.id.fecha);
         
         description = (EditText) findViewById(R.id.text_description);
         price = (EditText) findViewById(R.id.text_price);
 
     }
     
     private void updateAll(){
     	//System.out.println("updateALL -> "+Calendar.getInstance().get(Calendar.MONTH) );
         totalExpensis = datasource.getExpensesFromMonth(Calendar.getInstance().get(Calendar.MONTH)).toString();
         capitalActual.setText("$ "+totalExpensis);
     }
     
     private int resetMonth(int month){
     	month += 1;
     	if (month > 12 || month <= 0) month = 1;
     	return month;
     }
     
     
     private String dateFromPicker(DatePicker c){
     	String cad = "";
     	int month = resetMonth(c.getMonth());
     	cad += c.getDayOfMonth()+"-"+month+"-"+c.getYear();
     	return cad;
     }
     
     public void onClick(View view) {
 	    
 	    if (view.getId() == R.id.button_add){	 
 	      if (description.getText() != null 
 	    	&& !description.getText().toString().equals("")
 	    	&& price.getText() != null 
 	    	&& !price.getText().toString().equals("")
 	    	&& !price.getText().toString().equals("."))
 	      {
 		      Expensi expensi = new Expensi(
 		    		  description.getText().toString(),
 		    		  Float.parseFloat(price.getText().toString()),
 		    		  dateFromPicker(calendar)
 		    		  );	      
 		      datasource.createExpensi(expensi);
 		      
 		      Toast.makeText(this, "Guardado: "+description.getText().toString(), Toast.LENGTH_SHORT).show();
 		      
 		      if ( (resetMonth(calendar.getMonth()) == resetMonth(Calendar.getInstance().get(Calendar.MONTH))) ){
 		    	  //System.out.println("Se ejecuta UpdateAll porque concuerda el mes");
 		    	  this.updateAll();
 		      }
 	      }else Toast.makeText(this, "Campos Incompletos", Toast.LENGTH_SHORT).show();
 	    }
     }
     
     @Override
     protected void onResume() {
       datasource.open();
       this.updateAll();
       super.onResume();
     }
 
     @Override
     protected void onPause() {
       datasource.close();
       super.onPause();
     }
 
 
 	@Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         // Restore the previously serialized current dropdown position.
         if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
             getActionBar().setSelectedNavigationItem(
                     savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         // Serialize the current dropdown position.
         outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                 getActionBar().getSelectedNavigationIndex());
     }
 
    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
    }
     
     @Override
     public boolean onNavigationItemSelected(int position, long id) {
     	if (position == 1){
     		Intent intent = new Intent(this, ListExpensisActivity.class);
     		startActivity(intent);
     	}
     	if (position == 2){
     		finish();
     	}
         return true;
     }
 
 }
