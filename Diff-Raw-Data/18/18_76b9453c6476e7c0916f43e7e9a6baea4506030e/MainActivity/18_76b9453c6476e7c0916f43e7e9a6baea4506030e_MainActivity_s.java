 package budgetapp.main;
 
 
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.graphics.Color;
 import android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.content.ClipData.Item;
 import android.content.Context;
 import android.database.sqlite.*;
 
 public class MainActivity extends Activity implements OnItemSelectedListener{
 
 	public BudgetDataSource datasource;
 	int currentBudget = 0;
 	private String currentBudgetFileName = "current_budget"; // Internal file for current budget
 	private boolean logData = true; // If transactions should be logged
 	public ArrayList<String> allCategories = new ArrayList<String>();
 	int min(int a,int b) 
 	{
 		if(a<b)
 			return a;
 		return b;
 	}
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
          try
         {
         	 DataInputStream in = new DataInputStream(openFileInput(currentBudgetFileName));
              try
              {
             	 String strLine = in.readUTF();
             	 currentBudget = Integer.parseInt(strLine);
            	
            	  //Close the input stream
            	  in.close();
            	  
              	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
              	//EditText resultText = (EditText)findViewById(R.id.editTextSubtract);
              	//	resultText.requestFocus();
              	newBudget.setText(""+currentBudget);
              	if(currentBudget<0)
                		newBudget.setTextColor(Color.rgb(255,255-min(255,Math.abs(currentBudget/5)),255-min(255,Math.abs(currentBudget/5))));
                	else
                		newBudget.setTextColor(Color.rgb(255-min(255,Math.abs(currentBudget/5)),255,255-min(255,Math.abs(currentBudget/5))));
            	  
             	 
              }
              catch(IOException e)
              {
             	 currentBudget=0;
              }
         }
         catch(NumberFormatException e)
         {
         	currentBudget=0;
         }
         catch(FileNotFoundException e)
         {
         	currentBudget=0;
         }
       
         	  
         datasource = new BudgetDataSource(this);
         datasource.open();
         
         // Get the categories for the Spinner
         List<CategoryEntry> categories = datasource.getAllCategories();
         // Put the category names in an ArrayList to get them into the spinner
         for(int i=0;i<categories.size();i++)
         {
         	allCategories.add(categories.get(i).getCategory());
         }
         
         
         Spinner spinner = (Spinner) findViewById(R.id.categories_spinner);
      // Create an ArrayAdapter using the string array and a default spinner layout
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, allCategories);
      //ArrayAdapter.createFromResource(this,allCategories, android.R.layout.simple_spinner_item);
      // Specify the layout to use when the list of choices appears
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      // Apply the adapter to the spinner
      spinner.setAdapter(adapter);
      spinner.setOnItemSelectedListener(this);
         //List all budget entries
         updateLog();
        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void updateLog()
     {
     	List<BudgetEntry> entries = datasource.getSomeTransactions(10);
         TextView temp = (TextView)findViewById(R.id.textViewLog);
         temp.setText("");
        for(int i=entries.size()-1;i>=0;i--)
         {	
         	if(i>=0)
         		temp.append(entries.get(i).getDate() + ":    " + entries.get(i).getValue() + "\t\t\t" + entries.get(i).getCategory() +  "\n");
         }
         /*
         for(int i=0;i<allCategories.size();i++)
         {	
         		temp.append(allCategories.get(i)+ "\n");
         }
     	*/
         List<CategoryEntry> categories = datasource.getAllCategories();
         temp.append("\n\n");
         for(int i=1;i<categories.size();i++)
         {	
         //	if(categories.get(i).getNum()>0)
         		temp.append(categories.get(i)+ ": "+ categories.get(i).getNum() + "\t\t\t\t");
         		temp.append("Sum: "+categories.get(i).getTotal()+"\n");
         }
     }
     
     
     public void subtractFromBudget(View view,String theCategory) {
        
     	EditText resultText = (EditText)findViewById(R.id.editTextSubtract);
     	String result = resultText.getText().toString();
     	
     	try
     	{
     		 
     		int resultInt = Integer.parseInt(result);
         	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
         	currentBudget-=resultInt;
         	newBudget.setText(""+currentBudget);
         	
         	// Add to database if logging is set
         	if(logData)
         	{
 	        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 	        	Calendar cal = Calendar.getInstance();
 	        	
 	        	BudgetEntry entry = new BudgetEntry(resultInt*-1, dateFormat.format(cal.getTime()),theCategory);
 	        	datasource.createTransactionEntry(entry);
 	        	datasource.updateCategory(theCategory,resultInt*-1);
         	}
         	//Set color
         	if(currentBudget<0)
         		newBudget.setTextColor(Color.rgb(255,255-min(255,Math.abs(currentBudget/5)),255-min(255,Math.abs(currentBudget/5))));
         	else
         		newBudget.setTextColor(Color.rgb(255-min(255,Math.abs(currentBudget/5)),255,255-min(255,Math.abs(currentBudget/5))));
     	  
         	resultText.setText("");
         	updateLog();
     		DataOutputStream out = new DataOutputStream(openFileOutput(currentBudgetFileName,Context.MODE_PRIVATE));
     		out.writeUTF(""+currentBudget);
     		
     	}
     	catch(IOException e)
     	{
     		System.out.println("Error: "+e);
     	}
     	catch(NumberFormatException e)
     	{
     		System.out.println("Error: "+e);
     	}
     	
     		
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.menu_logdata: // Change logging data status
                 logData=!logData;
                 item.setChecked(logData);
                 return true;
            // case R.id.menu_addcategory:
            // 	DialogFragment newFragment = new CategoryDialogFragment();
            //     newFragment.show(getFragmentManager(), "add_category");
             //    return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 	{
 		//Toast.makeText(parent.getContext(), "The planet is " +parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
 		String theCategory = parent.getItemAtPosition(pos).toString();
 		if(pos!=0)
 		{
 			subtractFromBudget(parent,theCategory);
 		}
 		parent.setSelection(0);
 		
 	}
 
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		
 		
 	
 	}
     
 }
