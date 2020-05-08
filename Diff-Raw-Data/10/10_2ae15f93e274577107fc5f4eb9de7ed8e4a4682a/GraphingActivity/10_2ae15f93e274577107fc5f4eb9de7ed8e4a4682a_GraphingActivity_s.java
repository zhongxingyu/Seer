 package au.edu.uwa.csp.respreport;
 
 import org.achartengine.GraphicalView;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 /*
  * Activity to display the graph for each patient.
  * Call getPatientGraphView() function to display the graph.
  */
 public class GraphingActivity extends Activity {
 	
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_graphing);
 		
 		int intentInt;
 		
 		//Get the saved information about patientId
 		if(savedInstanceState != null){
 			intentInt = savedInstanceState.getInt("PatientId");
 		}else intentInt = -1;
 		
 		if(intentInt == -1) {
 			Bundle extras = getIntent().getExtras();
 			if(extras != null){
 				intentInt = extras.getInt("PatientId");
 			}
			else intentInt = -1;
 		}
 		
 	
 		getPatientGraphView(intentInt);
 	}
 	
 	/*
 	 * Display the graph for each patient.
 	 */
 	public void getPatientGraphView(int patientId) {
 		PatientGraph gp = new PatientGraph(GraphingActivity.this);
 
 		GraphicalView gpView = gp.getView(this,patientId);		
 		LinearLayout gpLayout = (LinearLayout) findViewById(R.id.graphLayout);
 		//adding the view to the layout.
 		gpLayout.addView(gpView);
 	}
 	
 	
 
 }
