 package com.example.ipcplayer.activity;
 
 import java.util.ArrayList;
 
 import com.example.ipcplayer.R;
 import com.example.ipcplayer.widget.CellLayout;
 import com.example.ipcplayer.widget.DocIndicator;
 import com.example.ipcplayer.widget.Workspace;
 import com.example.ipcplayer.widget.Workspace.IWorkspaceListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.view.View.OnClickListener;
 
 public class HelpActivity extends Activity implements IWorkspaceListener{
 	private static final String TAG = HelpActivity.class.getSimpleName();
 	private static final int CELL_NUMBER = 4;
 	private Workspace mWorkspace;
 	private DocIndicator mDocIndicator;
 	private TextView mStartTV;
 	
 	private boolean mExited;
 	private ArrayList<CellLayout> mCellList = new ArrayList<CellLayout>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.help);
 		
 		setupViews();
 		updateIndicatorFromWorkspace();
 	}
 
 	private void setupViews(){
 		mWorkspace = (Workspace) findViewById(R.id.help_workspace);
 		mDocIndicator = (DocIndicator) findViewById(R.id.help_indicator);
 		mStartTV = (TextView) findViewById(R.id.start);
 		mStartTV.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				mExited = true;
 				doStartActivity();
 			}
 			
 		});
 		
 		mWorkspace.setWorkspaceListener(this);
 		initCells();
 	}
 	
 	private void initCells(){
 		mWorkspace.addView(this.createCellLayout(R.drawable.welcome_1));
 		mWorkspace.addView(this.createCellLayout(R.drawable.welcome_2));
 		mWorkspace.addView(this.createCellLayout(R.drawable.welcome_3));
 		mWorkspace.addView(this.createCellLayout(R.drawable.welcome_4));
 	}
 	
 	private void updateIndicatorFromWorkspace(){
 		mDocIndicator.setTotal(mWorkspace.getChildCount());
		mDocIndicator.setCurrent(mWorkspace.getCurrentScreen());
 	}
 	
 	private void doStartActivity(){
 		Intent intent = new Intent(this, MainActivity.class);
 		this.startActivity(intent);
 		finish();
 	}
 	
 	private CellLayout createCellLayout(int resId){
 		CellLayout cellLayout = (CellLayout) getLayoutInflater().from(this).inflate(R.layout.help_item, null);
 		ImageView image = (ImageView) cellLayout.findViewById(R.id.help_item_img);
 		image.setImageResource(resId);
 		mCellList.add(cellLayout);
 		return cellLayout;
 	}
 	
 	private void hideStartButton(){
 		mStartTV.setVisibility(View.GONE);
 	}
 	
 	private void showStartButton(){
 		mStartTV.setVisibility(View.VISIBLE);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	@Override
 	public void onUpdateTotalNum(int total) {
 		if(mDocIndicator != null){
 			mDocIndicator.setTotal(total);
 		}
 	}
 
 	@Override
 	public void onUpdateCurrent(int current) {
 		if(mDocIndicator != null){
 			mDocIndicator.setCurrent(current);
 		}
 		if(current >= CELL_NUMBER -1){
 			showStartButton();
 		}else{
 			hideStartButton();
 		}
 	}
 	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {// 屏蔽回退键
			mExited = true;
			doStartActivity();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
 }
