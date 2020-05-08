 package dps.Assignment2.WorkoutTracker;
 
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.os.Bundle;
 import dps.Assignment2.WorkoutTracker.GraphView.GraphViewSeries;
 import dps.Assignment2.WorkoutTracker.GraphView.GraphViewData;
 import android.view.Gravity;
 import android.view.animation.AnimationUtils;
 import android.view.animation.RotateAnimation;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 public class WorkoutChartActivity extends Activity{
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		LineGraphView graphView = new LineGraphView(
 		this
 		, "Body Weight Graph - Weight(lbs) vs Days(#)"
 		);
 		
 		WorkoutDbHelper handler = new WorkoutDbHelper(WorkoutChartActivity.this);
 		ArrayList<Workout> workoutList = handler.getAllWorkouts();
 		ListIterator<Workout> ss = workoutList.listIterator();
 		WorkoutDbHelper handler2 = new WorkoutDbHelper(WorkoutChartActivity.this);
 		ArrayList<Workout> workoutList2 = handler2.getAllWorkouts();
 		ListIterator<Workout> tt = workoutList2.listIterator();
 		
 		int maxO=0;
 		while (ss.hasNext())
 		{
 			maxO = ss.nextIndex();
 			ss.next();
 		}
 		GraphViewData[] GVD = new GraphViewData[maxO+1];
 		int indexkeeper = 0;
 		while (tt.hasNext())
 		{
 			Workout wot = (Workout) tt.next();
 			String testd = "BW : " + wot.getBodyWeight() + "  DOY : " +wot.getDayOfYear();
 			Toast.makeText(getBaseContext(), testd, Toast.LENGTH_SHORT).show();
 			if (wot.getBodyWeight()>0)
 			{
 				//GraphViewData newgvd =new GraphViewData(wot.getDayOfYear(), wot.getBodyWeight());
 				//newgvd.
 				GVD[indexkeeper]=new GraphViewData(wot.getDayOfYear(), wot.getBodyWeight());
				indexkeeper++;
 			}
 		}
 		
 		graphView.addSeries(new GraphViewSeries(GVD));
 		
 		
 		/*graphView.addSeries(new GraphViewSeries(new GraphViewData[] {
 		new GraphViewData(25, 90)
 		, new GraphViewData(26, 80)
 		, new GraphViewData(39, 84)
 		, new GraphViewData(50, 82)
 		, new GraphViewData(55, 78)
 		, new GraphViewData(56, 77)
 		}));*/
 		setContentView(graphView);
 		/*
 		setContentView(R.layout.weight_graph); 
 		TextView text = (TextView)findViewById(R.id.tVGraph1);      
 		  RotateAnimation rotate= (RotateAnimation)AnimationUtils.loadAnimation(this,R.anim.rotateanimation);
 		  text.setAnimation(rotate);
 		LinearLayout LL = (LinearLayout)findViewById(R.id.lLgraph);
 		LL.setGravity(Gravity.CENTER|Gravity.LEFT);
 		LL.addView(graphView);
 		*/
 	}
 }
