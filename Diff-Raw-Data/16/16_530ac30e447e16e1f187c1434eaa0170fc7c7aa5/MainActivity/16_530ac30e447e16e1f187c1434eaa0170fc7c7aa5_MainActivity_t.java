 package com.rc.ui;
 
 import static com.rc.base.Output.print;
 import static com.rc.base.Output.printError;
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.rc.ConfigExecutor;
 import com.rc.MainExecutor;
 import com.rc.base.Executor;
 import com.rc.base.Output;
 import com.rc.base.OutputFunc;
 import com.rc.util.IntentFor;
 import com.rc.util.Keys;
 
 public class MainActivity extends Activity {
 	
 	public static final String TAG = MainActivity.class.getSimpleName();
 	
 	private RelativeLayout mLayout;
 	private EditText mInput;
 	private TextView mOutput;
 	private ScrollView mScroll;
 	private GestureDetector mGestureDetector;
 	private OnGestureListener mGestureListener = new OnGestureListener() {
 		
 		@Override
 		public boolean onSingleTapUp(MotionEvent e) {
 			return true;
 		}
 		
 		@Override
 		public void onShowPress(MotionEvent e) {
 		}
 		
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 				float distanceY) {
 			return true;
 		}
 		
 		@Override
 		public void onLongPress(MotionEvent e) {
 		}
 		
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			return true;
 		}
 		
 		@Override
 		public boolean onDown(MotionEvent e) {
 			return true;
 		}
 	};
 	
 	private boolean mResizeRequired;
 	
 	private Executor mConfigExec;
 	private Executor mMainExec;
 	
 	private int mLinesCount;
 	
 	private OutputFunc mOutputCallback = new OutputFunc() {
 		
 		@Override
 		public void write(final String text) {
 			mOutput.post(new Runnable() {
 				
 				@Override
 				public void run() {
 					writeOutput(text);
 				}
 			});
 		}
 	};
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     	
     	mLayout = (RelativeLayout) findViewById(R.id.layout);
     	mInput = (EditText) findViewById(R.id.input);
         mOutput = (TextView) findViewById(R.id.output);
         mScroll = (ScrollView) findViewById(R.id.scroll);
         mGestureDetector = new GestureDetector(this, mGestureListener);
         
         mGestureDetector.setOnDoubleTapListener(onDoubleTap());
         mInput.setOnKeyListener(onInputKey());
         mLayout.setOnCreateContextMenuListener(this);
         mOutput.setOnTouchListener(onOutputTouch());
         
		mMainExec = MainExecutor.getInstance(this);
         mConfigExec = new ConfigExecutor(this);
         
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     }
     
     @Override
     protected void onRestoreInstanceState(Bundle savedState) {
     	super.onRestoreInstanceState(savedState);
     	
 		CharSequence outputText = 
 				savedState.getCharSequence(Keys.OUTPUT_TEXT);
 		mOutput.setText(outputText);
     }
 	
     @Override
     protected void onResume() {
     	super.onResume();
     	
     	mResizeRequired = false;
     	
 		Output.registerOutput(mOutputCallback);
     	
 		scrollToEnd();
     	setInputFocus();
 	}
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	// Output text state
     	outState.putCharSequence(Keys.OUTPUT_TEXT, mOutput.getText());
     	
     	super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	
 		Output.unregisterOutput(mOutputCallback);
     }
 	
 	private OnKeyListener onInputKey() {
 		return new OnKeyListener() {
 			@Override
 			public boolean onKey(View view, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					String text = mInput.getText().toString();
 					mInput.setText("");
 					
 		    		executeCommand(text);
 		    		resizeControls();
 		    		scrollToEnd();
 		    		setInputFocus();
 					return true;
 				}
 				return false;
 			}
 		};
 	}
 
 	private OnDoubleTapListener onDoubleTap() {
 		return new GestureDetector.OnDoubleTapListener() {
 			
 			private boolean mSingleTapFired;
 			
 			@Override
 			public boolean onSingleTapConfirmed(MotionEvent e) {
 				if (mSingleTapFired)
 					return false;
 				
 				mSingleTapFired = true;
     			resizeControls();
 	    		scrollToEnd();
     	    	setInputFocus();
 				return true;
 			}
 			
 			@Override
 			public boolean onDoubleTap(MotionEvent e) {
     			resizeControls();
 	    		scrollToEnd();
     	    	setInputFocus();
 				return true;
 			}
 			
 			@Override
 			public boolean onDoubleTapEvent(MotionEvent e) {
 				return false;
 			}
 		};
 	}
 
 	private OnTouchListener onOutputTouch() {
 		return new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent e) {
     			return mGestureDetector.onTouchEvent(e);
 			}
 		};
 	}
 
 	private void executeCommand(String commandText) {
 		commandText = commandText.trim();
 		if (TextUtils.isEmpty(commandText))
 			return;
 		
 		print(commandText);
 		
 		try {
 			if (!mConfigExec.execute(commandText)) {
 				
 				if (!mMainExec.execute(commandText)) {
 					print("wrong input");
 				}
 			}
 		} catch (Exception e) {
 			printError(e);
 		}
 		
 		print();
 	}
 
 	public void runService() {
 		startService(IntentFor.commandsService(this));
 	}
 
 	public void shutDownService() {
 		stopService(IntentFor.commandsService(this));
 	}
 
     public void writeOutput(String text) {
     	mOutput.append(text);
     	
     	// Shorten the output text if it is too big
     	if (mLinesCount++ >= 2000) {
     		int length = mOutput.getText().length();
 			mOutput.setText(mOutput.getText()
 					.subSequence(length / 2, length - 1));
     		mLinesCount = 0;
     	}
     	
 		scrollToEnd();
     }
     
     public void clearOutput() {
     	mOutput.setText("");
     	mResizeRequired = false;
 		setScrollHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
     }
     
     private void scrollToEnd() {
     	mScroll.post(new Runnable() {
 			@Override
 			public void run() {
 				mScroll.fullScroll(View.FOCUS_DOWN);
 			}
 		});
     }
     
     private void setInputFocus() {
     	mInput.post(new Runnable() {
     		@Override
     		public void run() {
 				mInput.requestFocus();
     		}
     	});
     }
 	
 	private void resizeControls() {
 		if (!mResizeRequired)
 			mResizeRequired = mInput.getHeight() < mOutput.getLineHeight();
 		
 		if (mResizeRequired)
     		setScrollHeight(mLayout.getHeight() - (mLayout.getHeight() / 5));
 	}
     
     private void setScrollHeight(int height) {
     	ViewGroup.LayoutParams lp = mScroll.getLayoutParams();
     	
     	if (height == 0 || lp.height == height)
     		return;
     	
     	lp.height = height;
     	mScroll.setLayoutParams(lp);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         createMenu(menu);
         return true;
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
     		ContextMenuInfo menuInfo) {
     	super.onCreateContextMenu(menu, v, menuInfo);
         createMenu(menu);
     }
 
 	private void createMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 	}
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	return onMenuItemSelected(item);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	return onMenuItemSelected(item);
     }
 
 	private boolean onMenuItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_exit) {
 			exit();
 		} else if (item.getItemId() == R.id.menu_video_detector) {
 			startActivity(IntentFor.videoDetectorActivity(this));
 		}
 		return true;
 	}
 
 	public void exit() {
 		finish();
 	}
 }
