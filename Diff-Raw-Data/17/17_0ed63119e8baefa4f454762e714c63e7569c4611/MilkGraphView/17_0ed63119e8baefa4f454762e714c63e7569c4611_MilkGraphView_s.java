 package graphviews;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import utils.BabyEvent;
 import utils.ScheduleDatabase;
 import android.content.Context;
 import android.util.Log;
 import android.widget.LinearLayout;
 
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 
 import fi.vincit.babyschedule.R;
 import fi.vincit.babyschedule.activities.Settings;
 
 public class MilkGraphView extends LinearLayout {
 
 	LinearLayout mLayout;
 	Context mContext;
 	
 	private GraphViewData[] mMilkData = null;
 	private double mMaxMilk = 0.0;
 	private int mMilkDays = 0;
 	
 	public MilkGraphView(Context context) {
 		super(context);			
 		mContext = context;		
 	
 		initializeGraphData();
 		GraphView gv = StatisticsGraphViewUtils.createGraphViewFromData(
 				mContext, mMilkData, mMaxMilk, mMilkDays, mContext.getString(R.string.milk_per_day));
 		addView(gv);
 	}
 
 	public void initializeGraphData() {
 		ArrayList<Date> dates = ScheduleDatabase.getActionDatesForAction(Settings.getCurrentBabyName(), 
 																		 mContext.getString(R.string.milk_activity));
 		if( dates.size() == 0 ) {
 			return;
 		}
 		Date oldest = dates.get(0);
 		Date now = new Date();
 		long numberOfDays = (now.getTime() - oldest.getTime());
		Log.i("BabySchedule", "BarStatistics: how many ms ago: " + numberOfDays);
		numberOfDays /= 24*60*60*1000;
		Log.i("BabySchedule", "BarStatistics: how many days ago: " + numberOfDays);
 		numberOfDays++;
 		
 		mMilkDays = (int)numberOfDays;
 		if( mMilkDays < 7 )
 			mMilkDays = 7;
 		mMilkData = new GraphViewData[mMilkDays];
 		mMaxMilk = 0;
 		// Acquire milk data for milk / day
 		int indexForValueArray = 0;
 		
 		for( int i = (int)mMilkDays-1; i >= 0; i-- ) {
 			ArrayList<BabyEvent> events = ScheduleDatabase.getAllDbActionsFromNumberOfDaysAgo(Settings.getCurrentBabyName(), 
 																mContext.getString(R.string.milk_activity), i);
 			int daysCombined = 0;
 			for(BabyEvent event : events ) {
				daysCombined += event.getFreeValue();
				if( mMaxMilk < event.getFreeValue() ) 
					mMaxMilk = event.getFreeValue();
 			}
 			
 			mMilkData[indexForValueArray] = new GraphViewData(i, daysCombined);
 			
 			indexForValueArray++;
 		}			
 		
 	}	
 
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 		// TODO Auto-generated method stub
 		super.onLayout(changed, l, t, r, b);
 	}
 }
