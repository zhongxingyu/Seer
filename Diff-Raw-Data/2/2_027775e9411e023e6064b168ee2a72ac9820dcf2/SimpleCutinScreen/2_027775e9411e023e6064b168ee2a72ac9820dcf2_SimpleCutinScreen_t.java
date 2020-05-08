 package com.garlicg.cutinlib;
 
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewStub;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class SimpleCutinScreen{
 	public final static int STATE_VIEW = 0;
 	public final static int STATE_PICK = 1;
 	private int mState = STATE_VIEW;
 	private View mViewParent;
 	private PickListener mListener;
 	private Demo mDemo;
 	private View mGetView;
 	private ListView mListView;
 	private Context mContext;
 	
 	public SimpleCutinScreen(Context context , Intent intent){
 		mContext = context;
 		mViewParent = LayoutInflater.from(context).inflate(R.layout.cutin_simple_screen, null);
 		mDemo = new Demo(context);
 		
 		String action = intent.getAction();
 		if(!TextUtils.isEmpty(action) && action.equals(CutinInfo.ACTION_PICK_CUTIN)){
 			// Call from official cut-in app
 			mState = STATE_PICK;
 		}
 		else{
 			mState = STATE_VIEW;
 		}
 		
 		// setupListView
 		mListView = (ListView)mViewParent.findViewById(R.id.__cutin_simple_ListView);
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				Object item = arg0.getItemAtPosition(arg2);
 				if(item instanceof CutinItem){
 					CutinItem ci = (CutinItem)item;
 					mDemo.play(ci.serviceClass);
 				}
 				else if(arg2 == 0 && mGetView != null){
 					Intent intent = new Intent(
 							Intent.ACTION_VIEW,
 							Uri.parse("market://details?id=com.garlicg.cutin"));
 					mContext.startActivity(intent);
 				}
 			}
 		});
 		
 		if(existManager(context)){
 			mListView.addHeaderView(newPaddingView(context));
 		}
 		else{
 			mGetView = LayoutInflater.from(context).inflate(R.layout.cutin_get_manager,null);
 			mListView.addHeaderView(mGetView);
 		}
 		mListView.addFooterView(newPaddingView(context));
 	}
 	
 	private View newPaddingView(Context context){
 		View padding = new View(context);
 		ListView.LayoutParams padding8 = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,dpToPx(context.getResources(),8));
 		padding.setLayoutParams(padding8);
 		return padding;
 	}
 	
 	private int dpToPx(Resources res , int dp){
     	return (int)(res.getDisplayMetrics().density * dp + 0.5f);
 	}
 	
 	private boolean existManager(Context context){
 		PackageManager pm = context.getPackageManager();
 		Intent intent = pm.getLaunchIntentForPackage("com.garlicg.cutin");
 		return intent != null;
 	}
 	
 	public View getView(){
 		return mViewParent;
 	}
 	
 	public int getState(){
 		return mState;
 	}
 	
 	public interface PickListener{
 		public void ok(Intent intent);
 		public void cancel();
 	}
 	
 	public void setListener(PickListener listener){
 		mListener = listener;
 	}
 	
 	public void resume(){
 		// remove view after get the manager app from this.
 		if(existManager(mContext) && mGetView != null){
 			mListView.removeHeaderView(mGetView);
 		}
 	}
 	
 	public void pause(){
 		mDemo.forceStop();
 	}
 	
 	public void setCutinList(ArrayList<CutinItem> list){
 		mListView = (ListView)mViewParent.findViewById(R.id.__cutin_simple_ListView);
 		
 		// launched from launcher ,etc
 		if(mState == STATE_VIEW){
 			ArrayAdapter<CutinItem> adapter = new ArrayAdapter<CutinItem>(mViewParent.getContext(), R.layout.cutin_list_item_1,android.R.id.text1, list);
 			mListView.setAdapter(adapter);
 		}
 		
 		// launched from manage app
 		else if(mState == STATE_PICK){
 			// Set ListView with SingleChoiceMode.
 			ArrayAdapter<CutinItem> adapter = new ArrayAdapter<CutinItem>(mViewParent.getContext(), R.layout.cutin_list_item_single_choice, android.R.id.text1,list);
 			mListView.setAdapter(adapter);
 			mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			
 			// inflate footer
 			ViewStub stub = (ViewStub)mViewParent.findViewById(R.id.__cutin_simple_PickerFrame);
 			View bottomFrame = stub.inflate();
 			
 			// OK button
 			View okButton = bottomFrame.findViewById(R.id.__cutin_okButton);
 			okButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					if(mListener != null){
 						int position = mListView.getCheckedItemPosition();
 						Object item = mListView.getItemAtPosition(position);
 						if(item != null && item instanceof CutinItem){
 							CutinItem ci = (CutinItem)item;
 							mListener.ok(CutinInfo.buildPickedIntent(ci));
 						}
 						else {
 							// no  selected item
 						}
 					}
 				}
 			});
 			
 			// Cancel button
 			View cancel = bottomFrame.findViewById(R.id.__cutin_cancelButton);
 			cancel.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					if(mListener != null){
 						mListener.cancel();
 					}
 				}
 			});
 		}
 	}
 }
