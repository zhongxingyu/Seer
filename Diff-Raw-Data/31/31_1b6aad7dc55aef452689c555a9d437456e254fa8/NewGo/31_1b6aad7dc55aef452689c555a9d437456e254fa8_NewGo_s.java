 package ocept.gogo;
 
 import ocept.gogo.db.*;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.NumberPicker;
 
 
 public class NewGo extends Activity {
 	private static final int MIN_BOUNTY = 0;
 	private static final int MAX_BOUNTY = 10;
 	goDB dba;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_go);
 		
         //init db
         dba = new goDB(this);
 
         //connect up view
         Button okButton = (Button) findViewById(R.id.newGoOkButton);
         okButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				try{
 					saveGo();
 				}
 				catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
         
         ((Button) findViewById(R.id.newGoCancelButton)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				closeActivity();
 			}
 		});
         
         NumberPicker np = (NumberPicker) findViewById(R.id.bountyPicker);
         np.setMaxValue(MAX_BOUNTY);
         np.setMinValue(MIN_BOUNTY);
 	}
 	private void saveGo(){
 		//get go data from View
 		Go newGo = new Go();
 		EditText name = (EditText) findViewById(R.id.newGoName);
 		newGo.Name = name.getText().toString();
 		newGo.Desc = ((EditText) findViewById(R.id.newGoDesc)).getText().toString();
 		newGo.LastChecked = (long) 0; //unchecked on creation so set date to 0
 		newGo.Bounty = ((NumberPicker) findViewById(R.id.bountyPicker)).getValue();
 		
 		//open and enter into db
         dba.open();
 		dba.insertGo(newGo);
 		dba.close();
 		
 		//close activity
 		//GoList.this.myAdapter.notifyDataSetChanged();
		GoList.myAdapter.notifyDataSetInvalidated();
 		this.finish();
 	}
 	private void closeActivity()
 	{
 		this.finish();
 	}
 }
 
