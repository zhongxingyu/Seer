 package com.android.icecave.gui;
 
import android.view.Window;

 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.graphics.Point;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.guiLogic.GUIBoardManager;
 import com.android.icecave.guiLogic.PlayerGUIManager;
 import com.android.icecave.guiLogic.TileImageView;
 
 public class GameActivity extends Activity implements ISwipeDetector
 {
 	private static GUIBoardManager sGBM;
 	private PlayerGUIManager mPGM;
 	private Point mPlayerPosition; // FIXME Where to initialize?
 	private TileImageView mPlayer;
 
 	private final String POSITION_X = "posX";
 	private final String POSITION_Y = "posY";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		// Create once
 		if (sGBM == null)
 		{
 			sGBM = new GUIBoardManager();
 
 			// TODO Create game? Send this somewhere anyways
 			if (getIntent().getExtras() != null)
 				getIntent().getExtras().get(Consts.LEVEL_SELECT_TAG);
 		}
 
 		// Set up player position
 		if (savedInstanceState != null) {
 			mPlayerPosition = new Point(savedInstanceState.getInt(POSITION_X), savedInstanceState.getInt(POSITION_Y));
 		} else {
 			mPlayerPosition = new Point(Consts.START_POS);
 		}
 		
 		// Create player
 		if (getIntent().getExtras() != null)
 		{
 			mPGM = new PlayerGUIManager((String) getIntent().getExtras().get(Consts.PLAYER_SELECT_TAG));
 		}
 		
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 
 		// Put position data
 		outState.putInt(POSITION_X, mPlayerPosition.x);
 		outState.putInt(POSITION_Y, mPlayerPosition.y);
 	}
 
 	@Override
 	public void bottom2top(View v)
 	{
 		mPlayer = mPGM.getPlayerImage(mPlayerPosition.x, mPlayerPosition.y, EDirection.UP, true);
 	}
 
 	@Override
 	public void left2right(View v)
 	{
 		mPlayer = mPGM.getPlayerImage(mPlayerPosition.x, mPlayerPosition.y, EDirection.RIGHT, true);
 	}
 
 	@Override
 	public void right2left(View v)
 	{
 		mPlayer = mPGM.getPlayerImage(mPlayerPosition.x, mPlayerPosition.y, EDirection.LEFT, true);
 	}
 
 	@Override
 	public void top2bottom(View v)
 	{
 		mPlayer = mPGM.getPlayerImage(mPlayerPosition.x, mPlayerPosition.y, EDirection.DOWN, true);
 	}
 }
