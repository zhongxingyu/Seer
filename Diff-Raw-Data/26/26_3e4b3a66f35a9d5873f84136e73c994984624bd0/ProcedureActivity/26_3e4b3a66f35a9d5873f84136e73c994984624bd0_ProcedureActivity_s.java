 package edu.cmu.hcii.novo.kadarbra;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.Button;
 import edu.cmu.hcii.novo.kadarbra.page.ExecNotesPage;
 import edu.cmu.hcii.novo.kadarbra.page.MenuPage;
 import edu.cmu.hcii.novo.kadarbra.page.PageAdapter;
 import edu.cmu.hcii.novo.kadarbra.page.StepPage;
 import edu.cmu.hcii.novo.kadarbra.page.StepPageScrollView;
 import edu.cmu.hcii.novo.kadarbra.page.StowagePage;
 import edu.cmu.hcii.novo.kadarbra.page.TitlePage;
 import edu.cmu.hcii.novo.kadarbra.structure.ExecNote;
 import edu.cmu.hcii.novo.kadarbra.structure.Procedure;
 import edu.cmu.hcii.novo.kadarbra.structure.Step;
 
 public class ProcedureActivity extends Activity {
 	private static final String TAG = "ProcedureActivity";	// used for logging purposes
 	public final static String PROCEDURE = "edu.cmu.hcii.novo.kadarbra.PROCEDURE";
 	public final static String CURRENT_STEP = "edu.cmu.hcii.novo.kadarbra.CURRENT_STEP";
 
 	public final static int PREPARE_PAGES = 3; // number of pages in prepare stage (before steps are shown)
 	public final static int OPEN_MENU = 0; // startActivitForResult call identifier
 	
 	private Procedure procedure;
 	private ViewPager viewPager;
 	private Breadcrumb breadcrumb;
 	private StepPreviewWidget stepPreviewWidget;
 	private DataUpdateReceiver dataUpdateReceiver; 
 	
 	private List<Integer> procedureIndex;
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		Intent intent = getIntent();
 		procedure = (Procedure)intent.getSerializableExtra(MainActivity.PROCEDURE);
 		
 		setContentView(R.layout.activity_procedure);
 		
 		initViewPager(); // initializes ViewPager (the horizontal swiping UI element)
 		initBreadcrumb(); // initializes the breadcrumb (the step numbers at the top)
 		procedureIndex=getPageIndex();
 		
 		initMenuButton();
 		initStepPreviewWidget();
 	}
 	
 	private void initStepPreviewWidget(){
 		stepPreviewWidget = (StepPreviewWidget) findViewById(R.id.stepPreviewWidget);
 		stepPreviewWidget.setCurrentStep(procedure,0);
 
 	}
 	
 	
 	private void initMenuButton(){
 		Button menuButton = (Button) findViewById(R.id.menuButton);
 		menuButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getBaseContext(), MenuPage.class);
 		    	intent.putExtra(PROCEDURE, procedure);
 		    	
 		    	/**
 		    	 * Also passes highest level step to MenuPage
		    	 * 
		    	 * TODO: how to get highest level step needs to change (use procedureIndex)
 		    	 */
 		    	if (viewPager.getCurrentItem()>=PREPARE_PAGES){
			    	//StepPage curStepPage = (StepPage) viewPager.findViewWithTag(viewPager.getCurrentItem());
			    	//Step curStep = curStepPage.getStep();
			    	//if (curStepPage.getStepParent() != null){
			    	//	curStep = (Step) curStepPage.getStepParent();
			    	//}
			    	//TextView tv = ((TextView) curStepPage.getChildAt(0)).getText().toString().substring(0,1)
			    	//int curStepNum = Integer.parseInt(curStep.getNumber().substring(0,1));
			    	//intent.putExtra(CURRENT_STEP, curStepNum-1);
		    	
		    		intent.putExtra(CURRENT_STEP, -1);
 		    	}else
 		    		intent.putExtra(CURRENT_STEP, -1);
 		    	
 		    	startActivityForResult(intent, OPEN_MENU);
 		    	
 			}
 			
 		});
 	}
 	
 	
 	
 	/**
 	 * Called when child activity returns some result
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 				
 		/**
 		 * For messages sent by the menu
 		 */
 		if (requestCode == OPEN_MENU){
 			if (resultCode == Activity.RESULT_OK){
 				/**
 				 * For navigate commands
 				 */
 				if (data.getStringExtra("command").equals("navigate")){
 					final int navigate = data.getIntExtra("navigate",0);
 					Log.v(TAG,""+navigate);
 					
 					if (navigate != 0){
 						runOnUiThread(new Runnable() {
 		            	      public void run() { 
 		            	    	  int page = procedureIndex.get(navigate);
 		            	    	  viewPager.setCurrentItem(page,true);
 		            	      }
 		            	});
 					}
 					
 				}
 			}
 		}
 	}
 	
 	
 	
 	// initializes ViewPager (the horizontal swiping UI element)
 	private void initViewPager(){
 		viewPager = (ViewPager) findViewById(R.id.viewpager);	// gets the ViewPager UI object from its XML id
 		List<ViewGroup> sp = setupPages();
 		//List<ViewGroup> scrollViewPages = setupScrollViewPages(sp);
 		
 		
 		PagerAdapter pagerAdapter = new PageAdapter(this, sp); // the PagerAdapter is used to popuplate the ViewPager
 		
 		
 		viewPager.setAdapter(pagerAdapter);
 		viewPager.setCurrentItem(0);
 		
 		
 		// sets a listener for whenever the page in the ViewPager changes.
 		viewPager.setOnPageChangeListener(new OnPageChangeListener(){
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				//Log.v("viewPager","onPageScrollStateChanged");
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				//Log.v("viewPager","onPageScrolled");
 			}
 
 			@Override
 			public void onPageSelected(int arg0) {
 				Log.v("viewPager","onPageSelected "+arg0);
 				breadcrumb.setCurrentStep(arg0+1); // updates breadcrumb when a new page is selected
 				stepPreviewWidget.setCurrentStep(procedure,arg0);
 				
 				/*if (!(viewPager.getChildAt(viewPager.getCurrentItem()).getClass() == StepPage.class)) {
 
 					Log.v(TAG, "Removing breadcrumb");
 					breadcrumb.setVisibility(View.INVISIBLE);
 				} else {
 					Log.v(TAG, "Removing breadcrumb");
 					breadcrumb.setVisibility(View.VISIBLE);
 				}*/
 			}
 			
 		});
 	}
 	
 	
 	/**
 	 * Puts procedure step pages within scroll views
 	 */
 	private List<ViewGroup> setupScrollViewPages(List<ViewGroup> stepPages){
 		List<ViewGroup> result = new ArrayList<ViewGroup>();
 		for (int i = 0; i < stepPages.size(); i++){
 			StepPageScrollView sv = new StepPageScrollView(getBaseContext(),stepPages.get(i));
 			result.add(sv);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Setup the procedure steps as a list of step pages.
 	 * 
 	 * @return
 	 */
 	private List<ViewGroup> setupPages() {
 		List<ViewGroup> result = new ArrayList<ViewGroup>();
 		
 		result.add(new TitlePage(this, procedure.getNumber(), procedure.getTitle(), procedure.getObjective(), procedure.getDuration()));
 		
 		result.add(new StowagePage(this, procedure.getStowageItems()));
 		
 		result.add(new ExecNotesPage(this, procedure.getExecNotes()));
 		
 		for (int i = 0; i < procedure.getNumSteps(); i++){
 			result.addAll(setupStepPage(procedure.getStep(i), null));
 		}
 		
 		return result;
 	}
 	
 	
 	
 	/**
 	 * Setup the given step as a list of step pages.  First checks for
 	 * and sets any execution notes which may exist for the given step.
 	 * Recursively loops through any substeps to get all children.
 	 * 
 	 * TODO: redo how step pages get their parents. this is dumb
 	 * 
 	 * @param step
 	 * @return
 	 */
 	private List<ViewGroup> setupStepPage(Step step, Step parent) {
 		List<ViewGroup> result = new ArrayList<ViewGroup>();
 		//if (parent != null) step.setNumber(parent.getNumber() + "." + step.getNumber());
 		
 		int execNoteIndex = getExecNoteIndex(step.getNumber());
 		if (execNoteIndex > -1) step.setExecNote(procedure.getExecNotes().get(execNoteIndex));
 		
 		if (step.getNumSubsteps() > 0) {
 			for (int i = 0; i < step.getNumSubsteps(); i++) {
 				result.addAll(setupStepPage(step.getSubstep(i), step));
 			}
 		} else {
 			result.add(new StepPage(this, step, parent));
 		}
 		
 		return result;
 	}
 
 	
 	
 	/**
 	 * Get the index of the execution note for the given step number.
 	 * If no execution note exists, return -1.
 	 * 
 	 * @param stepNumber the step number to check for notes
 	 * @return the index of the corresponding execution note
 	 */
 	private int getExecNoteIndex(String stepNumber) {
 		List<ExecNote> notes = procedure.getExecNotes();
 		for (int i = 0; i < notes.size(); i++) {
 			if (stepNumber.equals(notes.get(i).getNumber())) return i;
 		}
 		
 		return -1;
 	}
 	
 	
 	
 	// initalizes the Breadcrumb (currently just step numbers)
 	private void initBreadcrumb(){
 		breadcrumb = (Breadcrumb) findViewById(R.id.breadcrumb);
 		breadcrumb.setTotalSteps(viewPager.getAdapter().getCount());
 		breadcrumb.setCurrentStep(1);
 		//breadcrumb.setVisibility(View.INVISIBLE);
 	}
 	
 	
 	
 	// The activity is about to become visible.
     @Override
     protected void onStart() {
         super.onStart();
         Log.v(TAG, "onStart");   
     }
 	
     
     
     // The activity has become visible (it is now "resumed").
     @Override
     protected void onResume() {
         super.onResume();
         //edu.cmu.hcii.novo.kadarbra.MainApp.setCurrentActivity(this);
         Log.v(TAG, "onResume");
 
         if (dataUpdateReceiver == null) 
         	dataUpdateReceiver = new DataUpdateReceiver();
         IntentFilter intentFilter = new IntentFilter("command");
         registerReceiver(dataUpdateReceiver, intentFilter);
     }
     
     
     
     // The activity is paused
     @Override
     protected void onPause(){
     	super.onPause();
     	//clearReferences();
     	Log.v(TAG, "onPause");
     	if (dataUpdateReceiver != null) 
     		unregisterReceiver(dataUpdateReceiver);
     }
     
     
     
     // The activity is no longer visible (it is now "stopped")
     @Override
     protected void onStop() {
         super.onStop();
         //clearReferences();
         Log.v(TAG, "onStop");
     }
     
     
     
     // The activity is about to be destroyed.
     @Override
     protected void onDestroy() {
         super.onDestroy();
         //clearReferences();
         Log.v(TAG, "onDestroy");
     }
     
     
     
     /*
     private void clearReferences() {
     	Activity currActivity = edu.cmu.hcii.novo.kadarbra.MainApp.getCurrentActivity();
     	if (currActivity != null && currActivity.equals(this))
     		edu.cmu.hcii.novo.kadarbra.MainApp.setCurrentActivity(null);
     }
     */
     
     
     
 	// Listens to broadcast messages
     private class DataUpdateReceiver extends BroadcastReceiver {
     	@Override
         public void onReceive(Context context, Intent intent) {
         	Log.v(TAG, "on receive: " +intent.getAction());
 
         	if (intent.getAction().equals("command")) {
             	Bundle b = intent.getExtras();
             	String msg = b.getString("msg");
             	
             	Log.v(TAG, msg);
             	
             	// For testing test input 
             	if (msg.equals("Back")){
             		prevPage();
             	}else if (msg.equals("Next")){
             		nextPage();
             	}
             	
           }
           
         }
     }
     
 	
 	/**
 	 * Gets index of pages, where each item in the returned list corresponds to a starting page of a step
 	 * @return pageIndex
 	 */ 
     private ArrayList<Integer> getPageIndex(){
     	ArrayList<Integer> index = new ArrayList<Integer>();
     	List<Step> steps = procedure.getSteps();
     	
     	int stepCounter = 0;
     	for (int i = 0; i < steps.size()+1; i++) {
     		if (i==0){
     			stepCounter += PREPARE_PAGES;
     			index.add(stepCounter);
     		}else{
 	    		Step s = steps.get(i-1);
 				int substeps = s.getNumSubsteps();
 				index.add(stepCounter);
 				if (substeps > 0)
 					stepCounter += substeps;
 				else
 					stepCounter++;
     		}
     	}
     	
 		return index;
     	
     }
 
     
     
     private void prevPage(){
     	if (viewPager.getCurrentItem()>0)
     		viewPager.setCurrentItem(viewPager.getCurrentItem()-1,true);
     	else
     		finish();
     }
     
     
     
     private void nextPage(){
     	if (viewPager.getCurrentItem()<viewPager.getChildCount());
     		viewPager.setCurrentItem(viewPager.getCurrentItem()+1,true);
     }
 }
