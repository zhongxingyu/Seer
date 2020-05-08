 package com.voc4u.activity.speaker;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.voc4u.R;
 import com.voc4u.activity.BaseWordActivity;
 import com.voc4u.activity.train.LastListAdapter;
 import com.voc4u.controller.PublicWord;
 
 public class Speaker extends BaseWordActivity implements OnTouchListener
 {
 	private static final int REPEAT_TIMEMILIS = 7000;
 	private Button mDontKnowButton;
 	private TextView txtActual;
 	private ListView lvLastItems;
 	
 	private int mNum;
 	private Button mKnowButton;
 	
 	SService mService = null;
 	//private Button btnShowHelp;
 	private PublicWord mActualPW;
 	private Intent mIService = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		mDontKnowButton = (Button) findViewById(R.id.dontKnowButton);
 		
 		
 		mKnowButton = (Button) findViewById(R.id.nextButton);
 		mKnowButton.setEnabled(false);
 		mKnowButton.setVisibility(View.GONE);
 		
 		txtActual = (TextView) findViewById(R.id.wordTextView);
 		txtActual.setOnTouchListener(this);
 		visibleSpokenWord(false);
 		
 		lvLastItems = (ListView) findViewById(R.id.lastList);
 		lvLastItems.setAdapter(new LastListAdapter(this));
 
 		//btnShowHelp = (Button) findViewById(R.id.btnHelp);
 		
 		
 		mNum = 0;
 		
 		SService.setMainActivity(this);
 		//mActualPW = mWCtrl.getFirstPublicWord();
 	}
 
 	
 	@Override
 	protected int getContentView()
 	{
 		return R.layout.train;
 	}
 
 	public void onBtnHelp(View v)
 	{
 		visibleSpokenWord(true);
 	}
 	
 	@Override
 	public void onResumeSuccess() 
 	{
 		super.onResumeSuccess();
 		onDontKnowButton(null);
 	}
 	
 	@Override
 	protected void onStop() 
 	{
 		super.onStop();
 		onNextButton(null);
 	}
 	
 	public void onDontKnowButton(View v)
 	{
 		if(mKnowButton.isEnabled())
 		{
 			onPlay(mActualPW.getLern());
 			return;
 		}
 		
 		//onNext(true);
 		mIService = new Intent(this, SService.class);
 		startService(mIService);
 		//mService = new SService();
 		//mService.start();
 		//mHandler.removeCallbacks(mUpdateTimeTask);
 		//mHandler.postDelayed(mUpdateTimeTask, REPEAT_TIMEMILIS);
 		//btnShowHelp.setEnabled(true);
 		mDontKnowButton.setText(R.string.btnRepeat);
 		mKnowButton.setEnabled(true);
 	}
 
 	public void onNextInUiThread()
 	{
 		mActualPW = mWCtrl.getFirstPublicWord();
 		onPlay(mActualPW.getLern());
 		
 		runOnUiThread(new Runnable()
 		{
 			
 			@Override
 			public void run()
 			{
 				setupUI(false);
 			}
 		});
 		
 	}
 	
 	public void onNextButtonUI()
 	{
 		runOnUiThread(new Runnable()
 		{
 			
 			@Override
 			public void run()
 			{
 				onNextButton(null);
 			}
 		});
 	}
 	
 	
 	public void onNext(boolean play)
 	{
 		mActualPW = mWCtrl.getFirstPublicWord();
 		if(play)
 			onPlay(mActualPW.getLern());
 		setupUI(true);
 		
 		
 		// lvLastItems.setAdapter(new LastListAdapter(this));
 		
 		
 	}
 
 	private void setupUI(boolean visibleShowWord)
 	{
 		//txtActual.setText(mActualPW.getLern());
 		visibleSpokenWord(visibleShowWord);
 		lvLastItems.invalidateViews();
 	}
 
 	private void visibleSpokenWord(boolean visible)
 	{	
 		String str = visible ? 
 				mActualPW.getLern()
 				: getResources().getString(R.string.btnShowSpoke);
 		txtActual.setText(str);
 		txtActual.setClickable(!visible);
 	}
 
 	public void onNextButton(View v)
 	{
 		//if (mHandler != null)
 		//	mHandler.removeCallbacks(mUpdateTimeTask);
 		//mService.stop();
 		mDontKnowButton.setText(R.string.btnStart);
 		//mDontKnowButton.setEnabled(true);
 		mKnowButton.setEnabled(false);
 		
 		if(mIService != null)
 			stopService(mIService);
 		//if(mService != null)
 		//	mService.stop();
 	}
 	
 	@Override
 	public void onDestroy()
 	{
 		if(mIService != null)
 			stopService(mIService);
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) 
 	{
 		if(v == txtActual)
 			visibleSpokenWord(true);
 		return false;
 	}
 }
