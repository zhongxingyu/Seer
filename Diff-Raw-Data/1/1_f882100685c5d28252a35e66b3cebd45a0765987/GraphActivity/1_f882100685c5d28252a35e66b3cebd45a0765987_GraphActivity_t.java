 package edu.colorado.trackers.HealthMetrics;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 
 import edu.colorado.trackers.R;
 import edu.colorado.trackers.db.Database;
 import edu.colorado.trackers.db.ResultSet;
 import edu.colorado.trackers.db.Selector;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.PopupWindow;
 
 import edu.colorado.trackers.graph.LineGraph;
 
 public class GraphActivity extends Activity {
 	
 	private Database db;
 	List<Integer> yDB = new ArrayList<Integer>();
 	List<String> dateDB = new ArrayList<String>();
 	int yMin = 0, yMax = 0, xMax = 0;
 
 	String title;
 	PopupWindow popupWindow;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_graph);
         db = new Database(this, "healthmetric15.db");
     }
     
     public void CholesterolGraph (View view)
     {
     	LineGraph line = new LineGraph();
     	title = "Cholesterol";
     	if(getDBValues() == 1)
 		{
     		//display graph
 			Intent lineIntent = line.getIntent(this, title, dateDB, yMin, yMax, xMax,  yDB );
 			startActivity(lineIntent);
 		}
 		else
 		{
 			//no data popup
 			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
 		    View popupView = layoutInflater.inflate(R.layout.activity_popup, null);  
 		    popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		    popupWindow.showAtLocation(this.findViewById(R.id.hrGraph), Gravity.CENTER, 0, 0); 
 			Button b = (Button) findViewById(R.id.chGraph);
 			b.setEnabled(false);
 		}
     }
     
     public void BloodPressureGraph (View view)
     {
     	LineGraph line = new LineGraph();
     	title = "BloodPressure";
 		if(getDBValues() == 1)
 		{
     		//display graph
 			Intent lineIntent = line.getIntent(this, title, dateDB, yMin, yMax, xMax,  yDB );
 			startActivity(lineIntent);
 		}
 		else
 		{
 			//no data popup
 			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
 		    View popupView = layoutInflater.inflate(R.layout.activity_popup, null);  
 		    popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		    popupWindow.showAtLocation(this.findViewById(R.id.hrGraph), Gravity.CENTER, 0, 0); 
 			Button b = (Button) findViewById(R.id.bpGraph);
 			b.setEnabled(false);
 		}
     }
     
     public void SugarGraph (View view)
     {
     	LineGraph line = new LineGraph();
     	title = "Sugar";
     	if(getDBValues() == 1)
 		{
     		//display graph
     		Intent lineIntent = line.getIntent(this, title, dateDB, yMin, yMax, xMax,  yDB );
 			startActivity(lineIntent);
 		}
 		else
 		{
 			//no data popup
 			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
 		    View popupView = layoutInflater.inflate(R.layout.activity_popup, null);  
 		    popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		    popupWindow.showAtLocation(this.findViewById(R.id.hrGraph), Gravity.CENTER, 0, 0); 
 			Button b = (Button) findViewById(R.id.suGraph);
 			b.setEnabled(false);
 		}
     }
     public void TemperatureGraph (View view)
     {
     	LineGraph line = new LineGraph();
     	title = "Temperature";
     	if(getDBValues() == 1)
 		{
     		//display graph
     		Intent lineIntent = line.getIntent(this, title, dateDB, yMin, yMax, xMax,  yDB );
 			startActivity(lineIntent);
 		}
 		else
 		{
 			//no data popup
 			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
 		    View popupView = layoutInflater.inflate(R.layout.activity_popup, null);  
 		    popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		    popupWindow.showAtLocation(this.findViewById(R.id.hrGraph), Gravity.CENTER, 0, 0); 
 			Button b = (Button) findViewById(R.id.tmGraph);
 			b.setEnabled(false);
 		}
     }
     public void HeartRateGraph (View view)
     {
     	LineGraph line = new LineGraph();
     	title = "HeartRate";
     	if(getDBValues() == 1)
     	{
     		//display graph
     		Intent lineIntent = line.getIntent(this, title, dateDB, yMin, yMax, xMax,  yDB );
 			startActivity(lineIntent);
 		}
 		else
 		{
 			//no data popup
 			Button b = (Button) findViewById(R.id.hrGraph);
 			b.setEnabled(false);
 			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
 		    View popupView = layoutInflater.inflate(R.layout.activity_popup, null);  
 		    popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		    popupWindow.showAtLocation(this.findViewById(R.id.hrGraph), Gravity.CENTER, 0, 0); 
 		}
     }
     /** Called when the user clicks the done button */   
     public void done(View view) {
     	Intent intent = new Intent(this, HealthMetrics.class);
         String message = "more data";
        	intent.putExtra("from Graph", message);
        	startActivity(intent);
     }
     /** Called when the user clicks the close button on popup window*/
     public void popupClose(View view) {
     	popupWindow.dismiss();
     }
     
     /*get db value to be displayed on graph*/
 	public int getDBValues() 
 	{
 		yDB.clear();
     	yMin = 0; yMax = 0; xMax = 0;
     	dateDB.clear();
     	Selector selector = db.selector("healthMetrics15");       //give your table name here
     	selector.addColumns(new String[] { "reading", "date"});
     	if(!title.equals(null))
     		selector.where("type = ?", new String[] {title}); 
     	int count = selector.execute();
     	System.out.println("Selected (" + count + ") items");
     	ResultSet cursor = selector.getResultSet();
 
     	if(cursor.getCount() != 0)
     	{
     		cursor.moveToLast();
     		while (!cursor.isBeforeFirst()) 
     		{
     			Integer reading = cursor.getInt(0); 
     			String date = cursor.getString(1);
     			if(yMin == 0)							//set the ymin, ymax values to be displayed on graph
     				yMin = reading;
     			if(reading < yMin)
     				yMin = reading;
     			if(reading > yMax)
     				yMax = reading;
     			yDB.add(reading);					//add reading and date value to the dataset   
     			dateDB.add(date);
     			System.out.println("GraphActivity:   Data: "+ reading);
     			cursor.moveToPrevious();
     		}
     		cursor.close();
     		return 1;
     	}
     	else
     		return 0;
     }
     
 }
