 package com.sixteen.tetrisbattleandroid;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Tetris extends Activity {
         
         public TetrisTileView mTetrisView;
      
        final Context context =this;
         String fontPath1;
        TextView lv, score;
         private Button mStartButton;
         private Button mPauseButton;
         private Button mResumeButton;
         private Button mResetButton;
         
         
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 	
 		//set Fullscreen
 				requestWindowFeature(Window.FEATURE_NO_TITLE);
 				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 				
 		setContentView(R.layout.activity_tetris);
 		
 		fontPath1 = "SFSlapstickComicShaded.ttf";
 		Typeface txt = Typeface.createFromAsset(getAssets(), fontPath1);
 		
 		lv = (TextView) findViewById(R.id.tvLv);
 		score = (TextView) findViewById(R.id.tvScore);
 		
 		lv.setTypeface(txt);
 		score.setTypeface(txt);
		
 	
 
 		 mTetrisView = (TetrisTileView) findViewById(R.id.tetris_tile_view);
 	       
 	        
 	        mStartButton = (Button) findViewById(R.id.start_button);
 	        mStartButton.setFocusable(false);
 	        mPauseButton = (Button) findViewById(R.id.pause_button);
 	        mPauseButton.setFocusable(false);
 	        mPauseButton.setVisibility(View.INVISIBLE);
 	        mResumeButton = (Button) findViewById(R.id.resume_button);
 	        mResumeButton.setFocusable(false);
 	        mResumeButton.setVisibility(View.INVISIBLE);
 	        mResetButton = (Button) findViewById(R.id.reset_button);
 	        mResetButton.setFocusable(false);
 	        mResetButton.setVisibility(View.INVISIBLE);
 	        
 	       
 	        
 	        mStartButton.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                view.setVisibility(View.INVISIBLE);
 	                mPauseButton.setVisibility(View.VISIBLE);
 	                mResetButton.setVisibility(View.VISIBLE);
 	                
 	                mTetrisView.setMode(TetrisTileView.RUNNING);
 	             
 	            }
 	        });
 	        mPauseButton.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                view.setVisibility(View.INVISIBLE);
 	                mResumeButton.setVisibility(View.VISIBLE);
 	                mResetButton.setVisibility(View.VISIBLE);
 	                
 	              //  mTetrisView.setMode(TetrisTileView.PAUSE);
 	                
 	             // custom dialog
 	                final Dialog dialog = new Dialog(context);
 	                dialog.setContentView(R.layout.activity_dialogbox);
 	           	    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 	 			    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
 	    			  		
 	    			
 	    			dialog.show();
 	    			
 	            }
 	        });
 	        mResumeButton.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                view.setVisibility(View.INVISIBLE);
 	                mPauseButton.setVisibility(View.VISIBLE);
 	                
 	                mTetrisView.setMode(TetrisTileView.RUNNING);
 	              
 	            }
 	        });
 	        mResetButton.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                view.setVisibility(View.INVISIBLE);
 	                mStartButton.setVisibility(View.VISIBLE);
 	                mResumeButton.setVisibility(View.INVISIBLE);
 	                mPauseButton.setVisibility(View.INVISIBLE);
 
 	                TetrisTileView.mPlayerCount = 0;
 	                TetrisTileView.mFinishedPlayerCount = 0;
 	                
 	                mTetrisView.setMode(TetrisTileView.READY);
 	          
 	            }
 	        });
 	        
 	  //     TetrisTileView.setTextView(score);
 	        
 	       
 	        
 	        if (savedInstanceState == null) {
 	            // We were just launched -- set up a new game
 	                mTetrisView.setMode(TetrisTileView.READY);
 	                mTetrisView.setControlMethod(TetrisTileView.CONTROL_TOUCH);
 	             
 	        } else {
 	            // We are being restored
 	 
 	        }
 	    }
 	
 	
 
 	}
