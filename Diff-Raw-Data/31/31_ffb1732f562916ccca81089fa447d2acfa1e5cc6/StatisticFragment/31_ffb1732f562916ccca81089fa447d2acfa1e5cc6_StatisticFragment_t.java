 package main.activities;
 
 import statisticPageView.StatisticPageView;
 import statisticPageView.analysis.AnalysisDrunkView;
 import statisticPageView.analysis.AnalysisProgressView;
 import statisticPageView.analysis.AnalysisRatingView;
 import statisticPageView.analysis.AnalysisRestView;
 import statisticPageView.analysis.AnalysisSavingView;
 import statisticPageView.analysis.AnalysisSuccessView;
 import statisticPageView.statistics.StatisticPagerAdapter;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 
 public class StatisticFragment extends Fragment {
 	private View view;
 	private Activity activity;
 	private ViewPager statisticView;
 	private StatisticPagerAdapter statisticViewAdapter;
 	private RelativeLayout statisticLayout,dots_layout;
 	private ImageView[] dots;
 	private LinearLayout analysisLayout;
 	private StatisticPageView[] analysisViews;
 	private static Point statistic_px,analysis_px;
 	private ScrollView analysisView;
 	private Bitmap dot_on, dot_off;
 	private LoadingHandler loadHandler;
 	private StatisticFragment statisticFragment;
 	
 	
 	
 	private static final int[] DOT_ID={0xFF0,0xFF1,0xFF2};
 	
 	private ProgressDialog loadDialog;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("Statistic Fragment","onCreate");
         activity = this.getActivity(); 
         
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	view = inflater.inflate(R.layout.statistic_fragment, container,false);
     	if (view == null){
     		Log.d("STATISTIC FRAGMENT","VIEW NULL");
     	}
         return view;
     }
     
     public void onResume(){
     	super.onResume();
     	statisticFragment = this;
     	
     	// Eric - add  
    	analysisViews = new StatisticPageView[3];
     	analysisViews[0] = new AnalysisProgressView(activity, statisticFragment);
     	analysisViews[1] = new AnalysisSavingView(activity, statisticFragment);
		//analysisViews[2] = new AnalysisDrunkView(activity,statisticFragment);
		analysisViews[2] = new AnalysisRatingView(activity,statisticFragment);
 		// Eric end
     	
 		statisticViewAdapter = new StatisticPagerAdapter(activity,statisticFragment);
 		
 		loadDialog = LoadingBox.loading(this.getActivity());
 		loadDialog.show();
 		if (loadHandler==null)
 			loadHandler = new LoadingHandler();
 		loadHandler.sendEmptyMessage(0);
     }
     
     public void onPause(){
     	if (loadHandler!=null)
     		loadHandler.removeMessages(0);
     	clear();
     	super.onPause();
     }
     
     private void clear(){
     	if (dot_on!=null && !dot_on.isRecycled()){
     		dot_on.recycle();
     		dot_on=null;
     	}
     	if (dot_off!=null && !dot_off.isRecycled()){
     		dot_off.recycle();
     		dot_off=null;
     	}
     	statisticViewAdapter.clear();
     	for (int i=0;i<analysisViews.length;++i){
     		analysisViews[i].clear();
     	}
     	if (analysisLayout!=null)
     		analysisLayout.removeAllViews();
     	System.gc();
     }
     
     public static Point getStatisticPx(){
     	if (statistic_px!=null)
     		return statistic_px;
     	else
     		return null;
     }
     public static Point getAnalysisPx(){
     	if (analysis_px!=null)
     		return analysis_px;
     	else
     		return null;
     }
     
     private class StatisticOnPageChangeListener implements OnPageChangeListener{
 
 		@Override
 		public void onPageScrollStateChanged(int arg0) {
 			
 		}
 
 		@Override
 		public void onPageScrolled(int arg0, float arg1, int arg2) {
 		}
 
 		@Override
 		public void onPageSelected(int arg0) {
 			for (int i=0;i<3;++i)
 				dots[i].setImageBitmap(dot_off);
 			dots[arg0].setImageBitmap(dot_on);
 		}
     	
     }
     
 	@SuppressLint("HandlerLeak")
 	private class LoadingHandler extends Handler{
 		public void handleMessage(Message msg){
 			
         	Point screen = FragmentTabs.getSize();
         	statistic_px = new Point(screen.x,(int) (screen.x*467.0/720.0));
         	
         	statisticLayout = (RelativeLayout) view.findViewById(R.id.brac_statistics_layout);
         	statisticView = (ViewPager) view.findViewById(R.id.brac_statistics);
         	statisticView.setAdapter(statisticViewAdapter);
         	statisticView.setOnPageChangeListener(new StatisticOnPageChangeListener());
         	statisticView.setSelected(true);
         	analysisLayout  = (LinearLayout)  view.findViewById(R.id.brac_analysis_layout);
         	analysisLayout.removeAllViews();
         	analysisView = (ScrollView) view.findViewById(R.id.brac_analysis);
         	dots_layout = (RelativeLayout) view.findViewById(R.id.brac_statistics_dots);
         	
         	for (int i=0;i<analysisViews.length;++i)
     			analysisLayout.addView(analysisViews[i].getView());
     		
     		statisticViewAdapter.onPreTask();
     		for (int i=0;i<analysisViews.length;++i)
     			analysisViews[i].onPreTask();
     		
 			LayoutParams statisticViewLayoutParam =  statisticLayout.getLayoutParams();
         	statisticViewLayoutParam.height = statistic_px.y;
 			
         	LayoutParams analysisViewLayoutParam =  analysisView.getLayoutParams();
         	analysisViewLayoutParam.height = screen.y - statistic_px.y;
     		LayoutParams analysisViewParam0 =  analysisViews[0].getView().getLayoutParams();
     		analysisViewParam0.width = screen.x;
     		analysisViewParam0.height = (int) (screen.x*370.0/720.0);
     		
     		LayoutParams analysisViewParam1 =  analysisViews[1].getView().getLayoutParams();
     		analysisViewParam1.width = screen.x;
     		analysisViewParam1.height = (int) (screen.x*500.0/720.0);
         	
     		LayoutParams analysisViewParam2 =  analysisViews[2].getView().getLayoutParams();
     		analysisViewParam2.width = screen.x;
    		analysisViewParam2.height = (int) (screen.x*500.0/720.0);
    		
 	    	dot_on = BitmapFactory.decodeResource(activity.getResources(), R.drawable.drunk_record_dot_on);
 	    	dot_off = BitmapFactory.decodeResource(activity.getResources(), R.drawable.drunk_record_dot_off);
 			
 	    	RelativeLayout.LayoutParams dotsLayoutParam = (android.widget.RelativeLayout.LayoutParams) dots_layout.getLayoutParams();
 	    	dotsLayoutParam.topMargin = (int) (statistic_px.y*378.0/467.0); 
 	    	
 			statisticViewAdapter.onInBackground();
     		for (int i=0;i<analysisViews.length;++i)
     			analysisViews[i].onInBackground();
 			
 			int dot_size = (int) (screen.x*12.0/720.0);
 	    	int dot_gap = (int) (dot_size*4/3);
 			
 	    	dots = new ImageView[3];
 	    	for (int i=0;i<3;++i){
 	    		dots[i] = new ImageView(dots_layout.getContext());
 	    		dots[i].setScaleType(ScaleType.FIT_XY);
 	    		dots_layout.addView(dots[i]);
 	    		RelativeLayout.LayoutParams dotParam = (android.widget.RelativeLayout.LayoutParams) dots[i].getLayoutParams();
 	    		dotParam.width = dot_size;
 	    		dotParam.height = dot_size;
 	    		dots[i].setId(DOT_ID[i]);
 	    	}
 	    	
 	    	RelativeLayout.LayoutParams dot1Param = (android.widget.RelativeLayout.LayoutParams) dots[1].getLayoutParams();
 	    	dot1Param.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
 	    	RelativeLayout.LayoutParams dot0Param = (android.widget.RelativeLayout.LayoutParams) dots[0].getLayoutParams();
 	    	dot0Param.addRule(RelativeLayout.LEFT_OF,DOT_ID[1]);
 	    	dot0Param.rightMargin = dot_gap;
 	    	RelativeLayout.LayoutParams dot2Param = (android.widget.RelativeLayout.LayoutParams) dots[2].getLayoutParams();
 	    	dot2Param.addRule(RelativeLayout.RIGHT_OF,DOT_ID[1]);
 	    	dot2Param.leftMargin = dot_gap;
 	    	
 			statisticViewAdapter.onPostTask();
     		for (int i=0;i<analysisViews.length;++i)
     			analysisViews[i].onPostTask();
     		
 	    	statisticView.setCurrentItem(0);
 	    	for (int i=0;i<3;++i)
 				dots[i].setImageBitmap(dot_off);
 			dots[0].setImageBitmap(dot_on);
 			
 			loadDialog.dismiss();
 			
 		}
 	}
     
 }
