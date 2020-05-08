 package dps.Assignment2.WorkoutTracker;
 
 import android.app.Activity;
 import android.os.Bundle;
 import dps.Assignment2.WorkoutTracker.GraphView.GraphViewSeries;
 import dps.Assignment2.WorkoutTracker.GraphView.GraphViewData;
 import android.widget.LinearLayout;
 public class WorkoutChartActivity extends Activity{
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		LineGraphView graphView = new LineGraphView(
 		this
 		, "Body Weight Graph"
 		);
 		
 		graphView.addSeries(new GraphViewSeries(new GraphViewData[] {
 		new GraphViewData(25, 90)
 		, new GraphViewData(26, 80)
 		, new GraphViewData(39, 84)
 		, new GraphViewData(50, 82)
 		, new GraphViewData(55, 78)
 		, new GraphViewData(56, 77)
 		}));
 		setContentView(R.layout.weight_graph); 
 		LinearLayout LL = (LinearLayout)findViewById(R.id.lLgraph);
		//LL.addView(graphView);
 		
 	}
 }
