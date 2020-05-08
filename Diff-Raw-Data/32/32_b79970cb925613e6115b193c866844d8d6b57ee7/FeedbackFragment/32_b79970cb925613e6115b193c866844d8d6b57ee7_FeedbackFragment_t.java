 package us.happ.android.fragment;
 
 import us.happ.android.R;
 import us.happ.android.activity.MainActivity;
 import us.happ.android.utils.Happ;
 import us.happ.android.utils.Storage;
 import us.happ.android.utils.Happ.KeyboardListener;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v7.app.ActionBar;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnLayoutChangeListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 
 public class FeedbackFragment extends HappFragment{
 
 	private MainActivity mContext;
 	private ActionBar actionbar;
 	private MenuItem mActionSubmit;
 	private EditText feedbackET;
 	private int mKeyboardHeight;
 	private int mContentHeight;
 	private EditText nameET;
 //	private View mView;
 	private EditText emailET;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		mContext = (MainActivity) getActivity();
 		setHasOptionsMenu(true);
 		
 		// Action bar
 		actionbar = mContext.getSupportActionBar();
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 
 		View v = inflater.inflate(R.layout.fragment_feedback, null, false);
 		feedbackET = (EditText) v.findViewById(R.id.feedback_text);
 		nameET = (EditText) v.findViewById(R.id.feedback_name);
 		emailET = (EditText) v.findViewById(R.id.feedback_email);
 
 		return v;
 	}
 	
 	@Override
 	public void onViewCreated(final View v, Bundle bundle){
 		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.showSoftInput(feedbackET, 0);
 		
 		mKeyboardHeight = Storage.getKeyboardHeight(mContext);
 		if (mKeyboardHeight != 0){
 			keyboardMeasured();
 		} else {
 			Happ.setKeyboardMeasurer(mContext, new KeyboardListener(){
 
 				@Override
 				public void onKeyboardMeasured(int keyboardHeight) {
 					mKeyboardHeight = keyboardHeight;
 					keyboardMeasured();
 				}
 				
 			});
 		}
 		
 		v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
 
 			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 			@Override
 			public void onGlobalLayout() {
 				feedbackET.setMaxHeight(mContentHeight - nameET.getHeight() - emailET.getHeight());
 				if (!Happ.hasJellyBean){
 					v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
 				} else {
 					v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
 				}
 			}
 
 			
 		});
 		
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		if (actionbar != null){
 			actionbar.setTitle(getResources().getString(R.string.title_feedback));
 			actionbar.setDisplayShowTitleEnabled(true);
 		}
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
 		menu.clear();
 		inflater.inflate(R.menu.feedback, menu);
 		mActionSubmit = menu.findItem(R.id.action_submit);
 		mActionSubmit.setEnabled(false);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {   
     	switch (item.getItemId()) {        
     	case R.id.action_submit:
 	    		submitFeedback();
     			mContext.returnToBoard();
     	  	  return true;
           default:            
         	  return super.onOptionsItemSelected(item);
     	}
     }
 	
 	private void submitFeedback(){
 		mContext.startFeedbackService();
 	}
 	
 	private void keyboardMeasured(){
 		int actionbarHeight = Happ.getActionBarHeight(mContext);
 		int statusbarHeight = Happ.getStatusBarHeight(mContext);
 		
 		View contentView = mContext.findViewById(android.R.id.content);
         
         mContentHeight = contentView.getRootView().getHeight() - statusbarHeight - actionbarHeight - mKeyboardHeight;
 
 	}
 	
 }
