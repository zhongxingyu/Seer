 package com.rampantmonk3y.p0ng;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.view.Gravity;
 
 
 public class p0ng_hs extends Activity {
 	
 	private dbmanager db;
 	TableLayout dataTable;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.highscores);
         try{db = new dbmanager(this);
         }catch (Exception e){}
     	dataTable = (TableLayout)findViewById(R.id.dataTable);
         displayTable();
     }
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.hsmenu, menu);
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
     	//handle item selection
     	switch (item.getItemId()){
     	case R.id.hs_home:
     		Intent myIntent = new Intent(this, p0ng_main.class);
     		startActivityForResult(myIntent, 0);
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
     private void displayTable(){
     	while(dataTable.getChildCount() > 1){
     		dataTable.removeViewAt(1);
     	}
     	
     	ArrayList<ArrayList<Object>> data = db.getAllRowsAsArrays();
     	
     	//build table headers
         TableRow tableHeader = new TableRow(this);
         TextView positionHeader = new TextView(this);
         positionHeader.setText("");
         positionHeader.setGravity(Gravity.CENTER);
         positionHeader.setPadding(5, 2, 5, 2);
         tableHeader.addView(positionHeader);
         TextView userHeader = new TextView(this);
         userHeader.setText("User Score");
         userHeader.setGravity(Gravity.CENTER);
         userHeader.setPadding(5, 2, 5, 2);
         tableHeader.addView(userHeader);
         TextView aiHeader = new TextView(this);
         aiHeader.setText("AI Score");
         aiHeader.setGravity(Gravity.CENTER);
         aiHeader.setPadding(5, 2, 5, 2);
         tableHeader.addView(aiHeader);
         dataTable.addView(tableHeader);
         
     	
     	for(int position=0; position<data.size(); ++position){
     		TableRow tableRow = new TableRow(this);
     		ArrayList<Object> row = data.get(position);
     		
     		TextView textPosition = new TextView(this);
    		int temp = position+1;
    		textPosition.setText(Integer.toString(temp));
     		textPosition.setGravity(Gravity.CENTER);
     		textPosition.setPadding(5, 2, 5, 2);
     		tableRow.addView(textPosition);
     		
     		TextView textOne = new TextView(this);
     		textOne.setText(row.get(1).toString());
     		textOne.setGravity(Gravity.CENTER);
     		textOne.setPadding(5, 2, 5, 2);
     		tableRow.addView(textOne);
     		
     		TextView textTwo = new TextView(this);
     		textTwo.setText(row.get(2).toString());
     		textTwo.setGravity(Gravity.CENTER);
     		textTwo.setPadding(5, 2, 5, 2);
     		tableRow.addView(textTwo);
     		
     		dataTable.addView(tableRow);
     	}
     }
     
 }
