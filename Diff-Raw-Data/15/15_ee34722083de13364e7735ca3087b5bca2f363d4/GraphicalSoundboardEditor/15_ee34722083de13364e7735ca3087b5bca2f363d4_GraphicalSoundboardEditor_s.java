 package fi.mikuz.boarder.gui;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.ConcurrentModificationException;
 import java.util.List;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.webkit.MimeTypeMap;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.thoughtworks.xstream.XStream;
 
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.component.Slot;
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.util.AutoArrange;
 import fi.mikuz.boarder.util.FileProcessor;
 import fi.mikuz.boarder.util.IconUtils;
 import fi.mikuz.boarder.util.SoundPlayerControl;
 import fi.mikuz.boarder.util.XStreamUtil;
 import fi.mikuz.boarder.util.dbadapter.BoardsDbAdapter;
 import fi.mikuz.boarder.util.editor.GraphicalSoundboardProvider;
 import fi.mikuz.boarder.util.editor.SoundNameDrawing;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class GraphicalSoundboardEditor extends Activity { //TODO destroy god object
 	private String TAG = "GraphicalSoundboardEditor";
 	
 	private GraphicalSoundboard mGsb;
 	
 	private List<GraphicalSoundboard> mHistory;
 	private int mCurrentHistoryIndex;
 	
 	private static final int LISTEN_BOARD = 0;
 	private static final int EDIT_BOARD = 1;
 	private int mMode = LISTEN_BOARD;
 	
 	private static final int DRAG_TEXT = 0;
 	private static final int DRAG_IMAGE = 1;
 	private int mDragTarget = DRAG_TEXT;
 	
 	private static final int EXPLORE_SOUND = 0;
 	private static final int EXPLORE_BACKGROUD = 1;
 	private static final int EXPLORE_SOUND_IMAGE = 2;
 	private static final int CHANGE_NAME_COLOR = 3;
 	private static final int CHANGE_INNER_PAINT_COLOR = 4;
 	private static final int CHANGE_BORDER_PAINT_COLOR = 5;
 	private static final int CHANGE_BACKGROUND_COLOR = 6;
 	private static final int CHANGE_SOUND_PATH = 7;
 	private static final int EXPLORE_SOUND_ACTIVE_IMAGE = 8;
 	
 	private int mCopyColor = 0;
 	
 	private Paint mSoundImagePaint;
 	private GraphicalSound mDragSound = null;
 	private boolean mDrawDragSound = false;
 	private float mInitialNameFrameX = 0;
 	private float mInitialNameFrameY = 0;
 	private float mInitialImageX = 0;
 	private float mInitialImageY = 0;
 	private float mNameFrameDragDistanceX = -1;
 	private float mNameFrameDragDistanceY = -1;
 	private float mImageDragDistanceX = -1;
 	private float mImageDragDistanceY = -1;
 	private long mClickTime = 0;
 	private long mLastTrackballEvent = 0;
 	private DrawingThread mThread;
 	private Menu mMenu;
 	private DrawingPanel mPanel;
 	boolean mCanvasInvalidated = false;
 	boolean mFirstDraw = true;
 	
 	int mWindowHeight;
 	int mWindowWidth;
 	
 	private boolean mMoveBackground = false;
 	private float mBackgroundLeftDistance = 0;
 	private float mBackgroundTopDistance = 0;
 	
 	final Handler mHandler = new Handler();
 	ProgressDialog mWaitDialog;
 	boolean mClearBoardDir = false;
 	
 	private File mSbDir = SoundboardMenu.mSbDir;
 	private String mBoardName = null;
 	
 	TextView soundImageWidthText;
 	TextView soundImageHeightText;
 	
 	TextView backgroundWidthText;
 	TextView backgroundHeightText;
 	EditText backgroundWidthInput;
 	EditText backgroundHeightInput;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
         
         mHistory = new ArrayList<GraphicalSoundboard>();
 		mCurrentHistoryIndex = -1;
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			
 			mBoardName = extras.getString(BoardsDbAdapter.KEY_TITLE);
 			setTitle(mBoardName);
 			
 			loadBoard(GraphicalSoundboardProvider.getBoard(mBoardName, GraphicalSoundboard.SCREEN_ORIENTATION_PORTAIT));
 			
 			if (mGsb.getSoundList().isEmpty()) {
 				mMode = EDIT_BOARD;
 			}	
 		} else {
 			mMode = EDIT_BOARD;
 			
 			mGsb = new GraphicalSoundboard();
 			
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		  	alert.setTitle("Set board name");
 
 		  	final EditText input = new EditText(this);
 		  	alert.setView(input);
 
 		  	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		  		public void onClick(DialogInterface dialog, int whichButton) {
 		  				String inputText = input.getText().toString();
 		  				
 		  				if (inputText.contains("\n")) {
 		  					mBoardName = inputText.substring(0, inputText.indexOf("\n"));
 		  				} else if (inputText.equals("")) {
 		  					finish();
 		  				}else {
 		  					mBoardName = inputText;
 		  				}
 		  				
 		  				setTitle(mBoardName);
 		  				save();
 		  		}
 		  	});
 		  	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  		@Override
 		  		public void onClick(DialogInterface dialog, int whichButton) {
 		  			finish();
 	  		}
 		  	});
 		  	alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					finish();
 				}
 		  	});
 		  	
 		  	alert.show();
 		  	
 		}
         
         mSoundImagePaint = new Paint();
         mSoundImagePaint.setColor(Color.WHITE);
         mSoundImagePaint.setAntiAlias(true);
         mSoundImagePaint.setTextAlign(Align.LEFT);
         
         setRequestedOrientation(mGsb.getScreenOrientation());
         createHistoryCheckpoint();
         
         setContentView(new DrawingPanel(this));
         
         File icon = new File(mSbDir, mBoardName + "/icon.png");
         if (icon.exists()) {
 			Bitmap bitmap = BitmapFactory.decodeFile(icon.getAbsolutePath());
             Drawable drawable = new BitmapDrawable(getResources(), IconUtils.resizeIcon(this, bitmap, (40/6)));
         	this.getActionBar().setLogo(drawable);
         }
 	}
 	
     @Override
     protected void onResume() {
     	super.onResume();
     	setContentView(mPanel);
     }
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		menu.clear();
 		mMenu = menu;
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.graphical_soundboard_editor_bottom, menu);
 	    
 	    if (mMode == EDIT_BOARD) {
 	    	menu.setGroupVisible(R.id.edit_mode, false);
 	    } else {
 	    	menu.setGroupVisible(R.id.listen_mode, false);
 	    }
 	    
 	    return super.onCreateOptionsMenu(menu);
     }
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
 		
         switch(item.getItemId()) {
         	case R.id.menu_listen_mode:
         		mMode = LISTEN_BOARD;
         		this.onCreateOptionsMenu(mMenu);
         		return true;
         		
         	case R.id.menu_edit_mode:
         		mMode = EDIT_BOARD;
         		this.onCreateOptionsMenu(mMenu);
         		return true;
         		
         	case R.id.menu_undo:
         		undo();
         		return true;
         	
         	case R.id.menu_redo:
         		redo();
         		return true;
         		
         	case R.id.menu_add_sound:
         		Intent i = new Intent(this, FileExplorer.class);
         		i.putExtra("parentKey", "addGraphicalSound");
         		i.putExtra("projectNameKey", mBoardName);
             	startActivityForResult(i, EXPLORE_SOUND);
             	return true;
             	
             case R.id.menu_save_project:
             	save();
                 return true;
                 
             case R.id.menu_convert_project:
             	
             	AlertDialog.Builder convertBuilder = new AlertDialog.Builder(this);
           	  	convertBuilder.setTitle("Convert");
           	  	convertBuilder.setMessage("Clear board directory?");
       	  	
 	          	convertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						mClearBoardDir = true;
 						initializeConvert();
 					}
 	          		
 	          	});
 
 	          	convertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 		          	public void onClick(DialogInterface dialog, int whichButton) {
 		          		mClearBoardDir = false;
 		          		initializeConvert();
 	          	    }
 	          	});
 	          	convertBuilder.setCancelable(false);
 	          	convertBuilder.show();
 			    
             	return true;
             
             case R.id.menu_play_pause:
             	SoundPlayerControl.togglePlayPause();
             	return true;
             	
             case R.id.menu_notification:
             	SoundboardMenu.updateNotification(this, mBoardName, mBoardName);
             	return true;
             	
             case R.id.menu_take_screenshot:
             	
             	Bitmap bitmap = Bitmap.createBitmap(mPanel.getWidth(), mPanel.getHeight(), Bitmap.Config.ARGB_8888);
             	Canvas canvas = new Canvas(bitmap);
             	mPanel.onDraw(canvas);
 	            Toast.makeText(getApplicationContext(), FileProcessor.saveScreenshot(bitmap, mBoardName), Toast.LENGTH_LONG).show();
 				
             	return true;
             	
             case R.id.menu_board_settings:
             	
             	final CharSequence[] items = {"Sound", "Background", "Icon", "Screen orientation", "Auto-arrange", "Reset position"};
 
             	AlertDialog.Builder setAsBuilder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
             	setAsBuilder.setTitle("Board settings");
             	setAsBuilder.setItems(items, new DialogInterface.OnClickListener() {
             	    public void onClick(DialogInterface dialog, int item) {
             	    	if (item == 0) {
             	    		LayoutInflater inflater = (LayoutInflater) GraphicalSoundboardEditor.this.
             	    			getSystemService(LAYOUT_INFLATER_SERVICE);
                         	View layout = inflater.inflate(R.layout.graphical_soundboard_editor_alert_board_sound_settings,
                         	    (ViewGroup) findViewById(R.id.alert_settings_root));
                         	
                         	final CheckBox checkPlaySimultaneously = 
                       	  		(CheckBox) layout.findViewById(R.id.playSimultaneouslyCheckBox);
                       	  	checkPlaySimultaneously.setChecked(mGsb.getPlaySimultaneously());
                       	  	
                       	  	final EditText boardVolumeInput = (EditText) layout.findViewById(R.id.boardVolumeInput);
                   	  			boardVolumeInput.setText(mGsb.getBoardVolume()*100 + "%");
                   	  			
                   	  		AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
                       	  	builder.setView(layout);
                       	  	builder.setTitle("Board settings");
                   	  	
             	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	          		public void onClick(DialogInterface dialog, int whichButton) {
             	          			boolean notifyIncorrectValue = false;
             	          			
             	          			mGsb.setPlaySimultaneously(checkPlaySimultaneously.isChecked());
             	          			
             	          			Float boardVolumeValue = null;
             	          			try {
             	          				String boardVolumeString = boardVolumeInput.getText().toString();
             	          				if (boardVolumeString.contains("%")) {
             	          					boardVolumeValue = Float.valueOf(boardVolumeString.substring(0, 
             	          							boardVolumeString.indexOf("%"))).floatValue()/100;
             	          				} else {
             	          					boardVolumeValue = Float.valueOf(boardVolumeString).floatValue()/100;
             	          				}
             	          				
             	          				
             	          				if (boardVolumeValue >= 0 && boardVolumeValue <= 1 && boardVolumeValue != null) {
             	          					mGsb.setBoardVolume(boardVolumeValue);
             		          			} else {
             		          				notifyIncorrectValue = true;
             		          			}
             	          			} catch(NumberFormatException nfe) {
             	          				notifyIncorrectValue = true;
             	          			}
             	          			
             	          			if (notifyIncorrectValue == true) {
             	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
             	          						Toast.LENGTH_SHORT).show();
             	          			}
             	          		}
             	          	});
 
             	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             		          	public void onClick(DialogInterface dialog, int whichButton) {
             	          	    }
             	          	});
             	          	
             	          	builder.show();
                         	
             	    	} else if (item == 1) {
             	    		LayoutInflater inflater = (LayoutInflater) GraphicalSoundboardEditor.this.
         	    				getSystemService(LAYOUT_INFLATER_SERVICE);
             	    		View layout = inflater.inflate(R.layout.
             	    			graphical_soundboard_editor_alert_board_background_settings,
                     	        (ViewGroup) findViewById(R.id.alert_settings_root));
             	    		
             	    		final CheckBox checkUseBackgroundImage = 
                       	  		(CheckBox) layout.findViewById(R.id.useBackgroundFileCheckBox);
                   	  		checkUseBackgroundImage.setChecked(mGsb.getUseBackgroundImage());
                       	
                       		final Button backgroundColorButton =
                       			(Button) layout.findViewById(R.id.backgroundColorButton);
                       		backgroundColorButton.setOnClickListener(new OnClickListener() {
                 				public void onClick(View v) {
                 					Intent i = new Intent(GraphicalSoundboardEditor.this, ColorChanger.class);
                 			    	i.putExtra("parentKey", "changeBackgroundColor");
                 			    	i.putExtra("backgroundColorKey", mGsb.getBackgroundColor());
                 			    	startActivityForResult(i, CHANGE_BACKGROUND_COLOR);
                 				}
                       		});
                       		
                       		final Button toggleMoveBackgroundFileButton =
                       			(Button) layout.findViewById(R.id.toggleMoveBackgroundFileButton);
                       		if (mMoveBackground) {
                       			toggleMoveBackgroundFileButton.setText("Move Background (file) : Yes");
                       		} else {
                       			toggleMoveBackgroundFileButton.setText("Move Background (file) : No");
                       		}
                       		toggleMoveBackgroundFileButton.setOnClickListener(new OnClickListener() {
                 				public void onClick(View v) {
                 					mMoveBackground = mMoveBackground ? false : true;
                 					if (mMoveBackground) {
                 	          			toggleMoveBackgroundFileButton.setText("Move Background (file) : Yes");
                 	          		} else {
                 	          			toggleMoveBackgroundFileButton.setText("Move Background (file) : No");
                 	          		}
                 				}
                       		});
                       	  	
                       	  	final Button backgroundFileButton = 
                       	  		(Button) layout.findViewById(R.id.backgroundFileButton);
                       	  	backgroundFileButton.setOnClickListener(new OnClickListener() {
             					public void onClick(View v) {
             						selectBackgroundFile();
             					}
                       	  	});
                       	  	
                       	  	backgroundWidthText = (TextView) layout.findViewById(R.id.backgroundWidthText);
                       	  	backgroundHeightText = (TextView) layout.findViewById(R.id.backgroundHeightText);
                       	  	
                       	  	if (mGsb.getBackgroundImage() != null) {
 	                      	  	backgroundWidthText.setText("Width (" + mGsb.getBackgroundImage().getWidth() + ")");
 	            				backgroundHeightText.setText("Height (" + mGsb.getBackgroundImage().getHeight() + ")");
                       	  	}
                       	  	
                       	  	backgroundWidthInput = (EditText) layout.findViewById(R.id.backgroundWidthInput);
                       	  	backgroundWidthInput.setText(Float.toString(mGsb.getBackgroundWidth()));
                       	  	
                       	  	backgroundHeightInput = (EditText) layout.findViewById(R.id.backgroundHeightInput);
                     	  	backgroundHeightInput.setText(Float.toString(mGsb.getBackgroundHeight()));
                     	  	
                     	  	final float widthHeightScale = mGsb.getBackgroundWidth() / mGsb.getBackgroundHeight();
                       	  	
                       	  	final CheckBox scaleWidthHeight = 
                       	  		(CheckBox) layout.findViewById(R.id.scaleWidthHeightCheckBox);
                       	  	scaleWidthHeight.setChecked(true);
                       	  	
                       	  	backgroundWidthInput.setOnKeyListener(new OnKeyListener() {
             					public boolean onKey(View v, int keyCode, KeyEvent event) {
             						if (scaleWidthHeight.isChecked()) {
             							try {
             								float value = Float.valueOf(
             										backgroundWidthInput.getText().toString()).floatValue();
             								backgroundHeightInput.setText(
             										Float.valueOf(value/widthHeightScale).toString());
             							} catch(NumberFormatException nfe) {}
             						}
             						return false;
             					}
                       	  	});
                       	  	
                       	 	backgroundHeightInput.setOnKeyListener(new OnKeyListener() {
             					public boolean onKey(View v, int keyCode, KeyEvent event) {
             						if (scaleWidthHeight.isChecked()) {
             							try {
             								float value = Float.valueOf(
             										backgroundHeightInput.getText().toString()).floatValue();
             								backgroundWidthInput.setText(
             										Float.valueOf(value*widthHeightScale).toString());
             							} catch(NumberFormatException nfe) {}
             						}
             						return false;
             					}
                     	  	});
                       	 	
                       	 	AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
                       	  	builder.setView(layout);
                       	  	builder.setTitle("Board settings");
                   	  	
             	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	          		public void onClick(DialogInterface dialog, int whichButton) {
             	          			
             	          			boolean notifyIncorrectValue = false;
             	          			mGsb.setUseBackgroundImage(checkUseBackgroundImage.isChecked());
             	          			
             	          			try {
             	          				mGsb.setBackgroundWidth(Float.valueOf(backgroundWidthInput.getText().toString()).floatValue());
             	          				mGsb.setBackgroundHeight(Float.valueOf(backgroundHeightInput.getText().toString()).floatValue());
             	          			} catch(NumberFormatException nfe) {
             	          				notifyIncorrectValue = true;
             	          			}
             	          			
             	          			if (notifyIncorrectValue == true) {
             	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
             	          						Toast.LENGTH_SHORT).show();
             	          			}
             	          			createHistoryCheckpoint();
             	          		}
             	          	});
 
             	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             		          	public void onClick(DialogInterface dialog, int whichButton) {
             	          	    }
             	          	});
             	          	
             	          	builder.show();
             	    	} else if (item == 2) {
             	    		AlertDialog.Builder resetBuilder = new AlertDialog.Builder(
 		                			GraphicalSoundboardEditor.this);
 		                	resetBuilder.setTitle("Change board icon");
 		                	resetBuilder.setMessage("You can change icon for this board.\n\n" +
 		                			"You need a png image:\n " + mSbDir + "/" + mBoardName + "/" + "icon.png\n\n" +
 		                			"Recommended size is about 80x80 pixels.");
 		                	AlertDialog resetAlert = resetBuilder.create();
 		                	resetAlert.show();
             	    	} else if (item == 3) {
            	    		final CharSequence[] items = {"Portait", "Landscape (BETA)"};
 
 		                	AlertDialog.Builder orientationBuilder = new AlertDialog.Builder(
 		                			GraphicalSoundboardEditor.this);
 		                	orientationBuilder.setTitle("Select orientation");
 		                	orientationBuilder.setItems(items, new DialogInterface.OnClickListener() {
 		                	    public void onClick(DialogInterface dialog, int item) {
 		                	    	if (item == 0 && mGsb.getScreenOrientation() != GraphicalSoundboard.SCREEN_ORIENTATION_PORTAIT) {
 				                	    screenOrientationWarning(GraphicalSoundboard.SCREEN_ORIENTATION_PORTAIT);
 		                	    	} else if (item == 1 && mGsb.getScreenOrientation() != GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE) {
 		                	          	screenOrientationWarning(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE);
 		                	    	}
 		                	    }
 		                	});
 		                	
 		                	AlertDialog orientationAlert = orientationBuilder.create();
 		                	orientationAlert.show();
             	    	} else if (item == 4) {
             	    	//Auto-arrange
             	    	LayoutInflater inflater = (LayoutInflater) GraphicalSoundboardEditor.this.
     	    				getSystemService(LAYOUT_INFLATER_SERVICE);
         	    		View layout = inflater.inflate(R.layout.
         	    			graphical_soundboard_editor_alert_auto_arrange,
                 	        (ViewGroup) findViewById(R.id.alert_settings_root));
         	    		
         	    		final CheckBox checkEnableAutoArrange = 
                   	  		(CheckBox) layout.findViewById(R.id.enableAutoArrange);
         	    		checkEnableAutoArrange.setChecked(mGsb.getAutoArrange());
                   	  	
         	    		final EditText columnsInput = (EditText) layout.findViewById(R.id.columnsInput);
         	    		columnsInput.setText(Integer.toString(mGsb.getAutoArrangeColumns()));
                   	  	
         	    		final EditText rowsInput = (EditText) layout.findViewById(R.id.rowsInput);
         	    		rowsInput.setText(Integer.toString(mGsb.getAutoArrangeRows()));
                   	 	
                   	 	AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
                   	  	builder.setView(layout);
                   	  	builder.setTitle("Board settings");
               	  	
         	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         	          		public void onClick(DialogInterface dialog, int whichButton) {
         	          			try {
         	          				int columns = Integer.valueOf(
         	          						columnsInput.getText().toString()).intValue();
         	          				int rows = Integer.valueOf(
         	          						rowsInput.getText().toString()).intValue();
         	          				
         	          				if (mGsb.getSoundList().size() <= columns*rows || !checkEnableAutoArrange.isChecked()) {
         	          					if (mGsb.getAutoArrange() != checkEnableAutoArrange.isChecked() ||
         	          							mGsb.getAutoArrangeColumns() != columns ||
         	          							mGsb.getAutoArrangeRows() != rows) {
         	          						
         	          						mGsb.setAutoArrange(checkEnableAutoArrange.isChecked());
 	            	          				mGsb.setAutoArrangeColumns(columns);
 	            	          				mGsb.setAutoArrangeRows(rows);
         	          					}
         	          				} else {
         	          					Toast.makeText(getApplicationContext(), "Not enought slots", 
             	          						Toast.LENGTH_SHORT).show();
         	          				}
         	          				createHistoryCheckpoint();
         	          			} catch(NumberFormatException nfe) {
         	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
         	          						Toast.LENGTH_SHORT).show();
         	          			}
         	          		}
         	          	});
 
         	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         		          	public void onClick(DialogInterface dialog, int whichButton) {
         	          	    }
         	          	});
         	          	
         	          	builder.show();
             	    	} else if (item == 5) {
             	    		ArrayList<String> itemArray = new ArrayList<String>();
 		    	    		for (GraphicalSound sound : mGsb.getSoundList()) {
 		    	    			itemArray.add(sound.getName());
 		    	    		}
 		    	    		CharSequence[] items = itemArray.toArray(new CharSequence[itemArray.size()]);
 		    	    		
 		    	    		AlertDialog.Builder resetBuilder = new AlertDialog.Builder(
 		                			GraphicalSoundboardEditor.this);
 		                	resetBuilder.setTitle("Reset sound position");
 		                	resetBuilder.setItems(items, new DialogInterface.OnClickListener() {
 		                	    public void onClick(DialogInterface dialog, int item) {
 		                	    	GraphicalSound sound = mGsb.getSoundList().get(item);
 		                	    	sound.setNameFrameX(50);
 		        	    			sound.setNameFrameY(50);
 		        	    			sound.generateImageXYFromNameFrameLocation();
 		        	    			createHistoryCheckpoint();
 		                	    }
 		                	});
 		                	AlertDialog resetAlert = resetBuilder.create();
 		                	resetAlert.show();
 		    	    		
             	    	}
             	    }
             	});
             	AlertDialog setAsAlert = setAsBuilder.create();
             	setAsAlert.show();
             	
             	return true;
             	
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 	
 	private void createHistoryCheckpoint() {
 		for (int i = mHistory.size()-mCurrentHistoryIndex; i > 1; i--) {
 			// User has undone and then creates a checkpoint
 			mHistory.remove(mHistory.size()-1);
 			Log.v(TAG, "removing history from end, size is " + mHistory.size());
 		}
 		GraphicalSoundboard hGsb = GraphicalSoundboard.copy(mGsb);
 		GraphicalSoundboard.unloadImages(hGsb);
 		mCurrentHistoryIndex++;
 		mHistory.add(hGsb);
 		while (mHistory.size() >= 30) {
 			Log.v(TAG, "removing history from start, size is " + mHistory.size());
 			mCurrentHistoryIndex--;
 			// Remove the second last save, keep the originally loaded board
 			mHistory.remove(1);
 		}
 		//StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		//Log.d(TAG, "create: index is " + mCurrentHistoryIndex + " size is " + mHistory.size() + " caller: " + stack[3].getMethodName() + " - " + stack[3].getLineNumber());
 	}
 	
 	private void undo() {
 		if (mCurrentHistoryIndex <= 0) {
 			Toast.makeText(getApplicationContext(), "Unable to undo", Toast.LENGTH_SHORT).show();
 		} else {
 			mCurrentHistoryIndex--;
 			loadBoard(GraphicalSoundboard.copy(mHistory.get(mCurrentHistoryIndex)));
 			mFirstDraw = true;
 		}
 		Log.v(TAG, "undo: index is " + mCurrentHistoryIndex + " size is " + mHistory.size());
 	}
 	
 	private void redo() {
 		if (mCurrentHistoryIndex+1 >= mHistory.size()) {
 			Toast.makeText(getApplicationContext(), "Unable to redo", Toast.LENGTH_SHORT).show();
 		} else {
 			mCurrentHistoryIndex++;
 			loadBoard(GraphicalSoundboard.copy(mHistory.get(mCurrentHistoryIndex)));
 		}
 		Log.v(TAG, "redo: index is " + mCurrentHistoryIndex + " size is " + mHistory.size());
 	}
 	
 	private void loadBoard(GraphicalSoundboard board) {
 		if (board.getBackgroundImagePath() != null) {
 			board.setBackgroundImage(BitmapFactory.decodeFile(board.getBackgroundImagePath().getAbsolutePath()));
 		}
 		for (GraphicalSound sound : board.getSoundList()) {
 			if (sound.getImagePath() == null) {
 				sound.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.sound));
 			} else {
 				sound.setImage(BitmapFactory.decodeFile(sound.getImagePath().getAbsolutePath()));
 			}
 			if (sound.getActiveImagePath() != null) {
 				sound.setActiveImage(BitmapFactory.decodeFile(sound.getActiveImagePath().getAbsolutePath()));
 			}
 		}
 		mGsb = board;
 	}
 	
 	private void screenOrientationWarning(final int orientation) {
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(
 			GraphicalSoundboardEditor.this);
 		orientationWarningBuilder.setTitle("Select orientation");
 			orientationWarningBuilder.setMessage(
 		  			"Changing screen orientation will delete all position data if you don't " +
 		  			"select deny.\n\n" +
 		  			"Proceed?");
 	  	orientationWarningBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsb.setBackgroundX(0);
 				mGsb.setBackgroundY(0);
 	    		for(GraphicalSound sound : mGsb.getSoundList()) {
 	    			sound.setNameFrameX(50);
 	    			sound.setNameFrameY(50);
 	    			sound.generateImageXYFromNameFrameLocation();
 	    		}
 	    		mGsb.setScreenOrientation(orientation);
 	    		finishBoard();
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 	  	});
 	  	orientationWarningBuilder.setNeutralButton("Deny", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsb.setScreenOrientation(orientation);
 				finishBoard();
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void finishBoard() {
 		try {
 			GraphicalSoundboardEditor.this.finish();
 		} catch (Throwable e) {
 			Log.e(TAG, "Error closing board", e);
 		}
 	}
 	
 	private void selectBackgroundFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra("parentKey", "selectBackgroundFile");
 		i.putExtra("projectNameKey", mBoardName);
     	startActivityForResult(i, EXPLORE_BACKGROUD);
 	}
 	
 	private void selectImageFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra("parentKey", "selectSoundImageFile");
 		i.putExtra("projectNameKey", mBoardName);
     	startActivityForResult(i, EXPLORE_SOUND_IMAGE);
 	}
 	
 	private void selectActiveImageFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra("parentKey", "selectSoundActiveImageFile");
 		i.putExtra("projectNameKey", mBoardName);
     	startActivityForResult(i, EXPLORE_SOUND_ACTIVE_IMAGE);
 	}
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         
         switch(requestCode) {
 	        case EXPLORE_SOUND:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	XStream xstream = XStreamUtil.graphicalBoardXStream();
 		        	
 		        	GraphicalSound sound = (GraphicalSound) xstream.fromXML(extras.getString("soundKey"));
 		        	sound.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.sound));
 		        	sound.setAutoArrangeColumn(0);
 		        	sound.setAutoArrangeRow(0);
 		        	if (mGsb.getAutoArrange()) {
 		        		if (placeToFreeSlot(sound)) {
 		        			mGsb.getSoundList().add(sound);
    	    			 	}
 		        	} else {
 		        		placeToFreeSpace(sound);
 		        		mGsb.getSoundList().add(sound);
 		        	}
 		        	createHistoryCheckpoint();
 	        	}
 	        	break;
 	        	
 	        case EXPLORE_BACKGROUD:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	File background = new File(extras.getString("backgroundKey"));
 		        	mGsb.setBackgroundImagePath(background);
 		        	mGsb.setBackgroundImage(BitmapFactory.decodeFile(mGsb.getBackgroundImagePath().getAbsolutePath()));
 		        	mGsb.setBackgroundWidth(mGsb.getBackgroundImage().getWidth());
 		        	mGsb.setBackgroundHeight(mGsb.getBackgroundImage().getHeight());
 		        	mGsb.setBackgroundX(0);
 					mGsb.setBackgroundY(0);
 					createHistoryCheckpoint();
 	        	}
 	        	backgroundWidthText.setText("Width (" + mGsb.getBackgroundImage().getWidth() + ")");
 				backgroundHeightText.setText("Height (" + mGsb.getBackgroundImage().getHeight() + ")");
 				backgroundWidthInput.setText(Float.toString(mGsb.getBackgroundWidth()));
 				backgroundHeightInput.setText(Float.toString(mGsb.getBackgroundHeight()));
 	        	break;
 	        
 	        case EXPLORE_SOUND_IMAGE:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	File image = new File(extras.getString("soundImageKey"));
 		        	mDragSound.setImagePath(image);
 		        	mDragSound.setImage(BitmapFactory.decodeFile(mDragSound.getImagePath().getAbsolutePath()));
 	        	}
 	        	soundImageWidthText.setText("Width (" + mDragSound.getImage().getWidth() + ")");
 				soundImageHeightText.setText("Height (" + mDragSound.getImage().getHeight() + ")");
 	        	break;
 	        	
 	        case EXPLORE_SOUND_ACTIVE_IMAGE:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	File image = new File(extras.getString("soundActiveImageKey"));
 		        	mDragSound.setActiveImagePath(image);
 		        	mDragSound.setActiveImage(BitmapFactory.decodeFile(mDragSound.getActiveImagePath().getAbsolutePath()));
 	        	}
 	        	break;
 	        	
 	        case CHANGE_NAME_COLOR:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	if (extras.getBoolean("copyKey")) {
 		        		mCopyColor = CHANGE_NAME_COLOR;
 		        	} else {
 		        		mDragSound.setNameTextColorInt(extras.getInt("colorKey"));
 		        	}
 	        	}
 	        	break;
 	        	
 	        case CHANGE_INNER_PAINT_COLOR:
 	
 				if (resultCode == RESULT_OK) {
 					Bundle extras = intent.getExtras();
 		        	if (extras.getBoolean("copyKey")) {
 		        		mCopyColor = CHANGE_INNER_PAINT_COLOR;
 		        	} else {
 		        		mDragSound.setNameFrameInnerColorInt(extras.getInt("colorKey"));
 		        	}
 				}
 				break;
 				
 	        case CHANGE_BORDER_PAINT_COLOR:
 	        	
 	        	if (resultCode == RESULT_OK) {
 	        		Bundle extras = intent.getExtras();
 		        	if (extras.getBoolean("copyKey")) {
 		        		mCopyColor = CHANGE_BORDER_PAINT_COLOR;
 		        	} else {
 		        		mDragSound.setNameFrameBorderColorInt(extras.getInt("colorKey"));
 		        	}
 	        	}
 	        	break;
 	        	
 	        case CHANGE_BACKGROUND_COLOR:
 	        	
 	        	if (resultCode == RESULT_OK) {
 	        		Bundle extras = intent.getExtras();
 		        	mGsb.setBackgroundColor(extras.getInt("colorKey"));
 		        	createHistoryCheckpoint();
 	        	}
 	        	break;
 	        	
 	        case CHANGE_SOUND_PATH:
 	        	if (resultCode == RESULT_OK) {
                 	
                 	LayoutInflater removeInflater = (LayoutInflater) 
                 			GraphicalSoundboardEditor.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                 	View removeLayout = removeInflater.inflate(
                 			R.layout.graphical_soundboard_editor_alert_remove_sound,
                 	        (ViewGroup) findViewById(R.id.alert_remove_sound_root));
               	  	
               	  	final CheckBox removeFileCheckBox = 
               	  		(CheckBox) removeLayout.findViewById(R.id.removeFile);
               	  	removeFileCheckBox.setText(" DELETE " + mDragSound.getPath().getAbsolutePath());
               	  	
               	  	AlertDialog.Builder removeBuilder = new AlertDialog.Builder(
               	  		GraphicalSoundboardEditor.this);
               	  	removeBuilder.setView(removeLayout);
               	  	removeBuilder.setTitle("Changing sound");
           	  	
               	  	removeBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
     	          	  	public void onClick(DialogInterface dialog, int whichButton) {
     	          	  		if (removeFileCheckBox.isChecked() == true) {
     	          	  			mDragSound.getPath().delete();
     	          	  		}
 	    	          	  	Bundle extras = intent.getExtras();
 				        	mDragSound.setPath(new File(extras.getString("soundPathKey")));
     	          	    }
     	          	});
 
               	  	removeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
     		          	public void onClick(DialogInterface dialog, int whichButton) {
     	          	    }
     	          	});
     	          	
               	  	removeBuilder.setCancelable(false);
               	  	removeBuilder.show();
 	        	}
 	        	break;
 	        
 	        default:
 	        	break;
         }
     }
 	
 	private void initializeConvert() {
 		mWaitDialog = ProgressDialog.show(GraphicalSoundboardEditor.this, "", "Please wait", true);
 		
 		Thread t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				try {
 					if (mClearBoardDir) {
 						cleanDirectory(new File(mSbDir, mBoardName).listFiles());
 					}
 					
 					FileProcessor.convertGraphicalBoard(GraphicalSoundboardEditor.this, mBoardName, mGsb);
 					save();
 				} catch (IOException e) {
 					Log.e(TAG, "Error converting board", e);
 				}
 				mHandler.post(mUpdateResults);
 	        }
 	    }; 
 	    t.start();
 	}
 	
 	private void cleanDirectory(File[] files) {
 		for (File file : files) {
 			
 			if (file.isDirectory()) {
 				cleanDirectory(file.listFiles());
 				if (file.listFiles().length == 0) {
 					Log.d(TAG, "Deleting empty directory " + file.getAbsolutePath());
 					file.delete();
 				}
 			} else {
 				
 				boolean boardUsesFile = false;
 
 				if (file.getName().equals("graphicalBoard") == true || file.getName().equals("icon.png") == true) {
 					boardUsesFile = true;
 				}
 
 				try {
 					if (file.getName().equals(mGsb.getBackgroundImagePath().getName())) boardUsesFile = true;
 				} catch (NullPointerException e) {}
 
 				for (GraphicalSound sound : mGsb.getSoundList()) {
 					if (boardUsesFile) break;
 
 					try {
 						if (sound.getPath().getAbsolutePath().equals(file.getAbsolutePath())) boardUsesFile = true;
 					} catch (NullPointerException e) {}
 
 					try {
 						if (sound.getImagePath().getAbsolutePath().equals(file.getAbsolutePath())) boardUsesFile = true;
 					} catch (NullPointerException e) {}
 
 					try {
 						if (sound.getActiveImagePath().getAbsolutePath().equals(file.getAbsolutePath())) boardUsesFile = true;
 					} catch (NullPointerException e) {}
 				}
 
 				if (boardUsesFile == false) {
 					Log.d(TAG, "Deleting unused file " + file.getAbsolutePath());
 					file.delete();
 				}
 			}
 		}
 	}
 	
 	final Runnable mUpdateResults = new Runnable() {
         public void run() {
         	mWaitDialog.dismiss();
         }
     };
 	
 	@Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onPause() {
     	save();
     	super.onPause();
     }
     
     private void save() {
     	if (mBoardName != null) {
     		try {
     			GraphicalSoundboard gsb = GraphicalSoundboard.copy(mGsb);
     			if (mDragSound != null && mDrawDragSound == true) gsb.getSoundList().add(mDragSound);
         		GraphicalSoundboardProvider.saveBoard(mBoardName, gsb);
         		Log.v(TAG, "Board " + mBoardName + " saved");
     		} catch (IOException e) {
     			Log.e(TAG, "Unable to save " + mBoardName, e);
     		}
     	}
     }
     
     private void initializeDrag(MotionEvent event, GraphicalSound sound) {
     	if (mMode == LISTEN_BOARD) {
     		if (sound.getPath().getAbsolutePath().equals(SoundboardMenu.mPauseSoundFilePath)) { 
     			SoundPlayerControl.togglePlayPause();
     		} else {
     			if (sound.getSecondClickAction() == GraphicalSound.SECOND_CLICK_PLAY_NEW) {
     				SoundPlayerControl.playSound(mGsb.getPlaySimultaneously(), sound.getPath(), sound.getVolumeLeft(), 
                 			sound.getVolumeRight(), mGsb.getBoardVolume());
     			} else if (sound.getSecondClickAction() == GraphicalSound.SECOND_CLICK_PAUSE) {
     				SoundPlayerControl.pauseSound(mGsb.getPlaySimultaneously(), sound.getPath(), sound.getVolumeLeft(), 
                 			sound.getVolumeRight(), mGsb.getBoardVolume());
     			} else if (sound.getSecondClickAction() == GraphicalSound.SECOND_CLICK_STOP) {
     				SoundPlayerControl.stopSound(mGsb.getPlaySimultaneously(), sound.getPath(), sound.getVolumeLeft(), 
                 			sound.getVolumeRight(), mGsb.getBoardVolume());
     			}
     			mCanvasInvalidated = true;
     		}
 		} else {
 			mDragSound = sound;
 			mDrawDragSound = true;
 			mInitialNameFrameX = sound.getNameFrameX();
 			mInitialNameFrameY = sound.getNameFrameY();
 			mInitialImageX = sound.getImageX();
 			mInitialImageY = sound.getImageY();
 			mGsb.getSoundList().remove(sound);
 			
 			mNameFrameDragDistanceX = event.getX() - sound.getNameFrameX();
 			mNameFrameDragDistanceY = event.getY() - sound.getNameFrameY();
 			mImageDragDistanceX = event.getX() - sound.getImageX();
 			mImageDragDistanceY = event.getY() - sound.getImageY();
 		}
     }
     
     private void copyColor(GraphicalSound sound) {
 		switch(mCopyColor) {
 			case CHANGE_NAME_COLOR:
 				mDragSound.setNameTextColorInt(sound.getNameTextColor());
 				break;
 			case CHANGE_INNER_PAINT_COLOR:
 				mDragSound.setNameFrameInnerColorInt(sound.getNameFrameInnerColor());
 				break;
 			case CHANGE_BORDER_PAINT_COLOR:
 				mDragSound.setNameFrameBorderColorInt(sound.getNameFrameBorderColor());
 				break;
 		}
 		mCopyColor = 0;
 		createHistoryCheckpoint();
 	}
     
     void moveSound(float X, float Y) {
 		if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
 			mDragSound.setNameFrameX(X-mNameFrameDragDistanceX);
 			mDragSound.setNameFrameY(Y-mNameFrameDragDistanceY);
 		}
 		if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
 			mDragSound.setImageX(X-mImageDragDistanceX);
 			mDragSound.setImageY(Y-mImageDragDistanceY);
 		}
 		mGsb.getSoundList().add(mDragSound);
 		mDrawDragSound = false;
 	}
 	
 	public void moveSoundToSlot(GraphicalSound sound, int column, int row, float imageX, float imageY, float nameX, float nameY) {
 		
 		int width = mPanel.getWidth();
 		int height = mPanel.getHeight();
 		
 		float middlePointX = width/mGsb.getAutoArrangeColumns()/2;
 		float middlePointY = height/mGsb.getAutoArrangeRows()/2;
 			
 		float lowPointX;
 		float highPointX;
 		float lowPointY;
 		float highPointY;
 		
 		boolean moveName = false;
 		boolean moveImage = false;
 		
 		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(sound);
 		float nameFrameWidth = soundNameDrawing.getNameFrameRect().width();
 		float nameFrameHeight = soundNameDrawing.getNameFrameRect().height();
 		
 		if (sound.getHideImageOrText() == GraphicalSound.HIDE_TEXT) {
 			lowPointX = imageX;
 			highPointX = imageX+sound.getImageWidth();
 			lowPointY = imageY;
 			highPointY = imageY+sound.getImageHeight();
 			moveImage = true;
 		} else if (sound.getHideImageOrText() == GraphicalSound.HIDE_IMAGE) {
 			lowPointX = nameX;
 			highPointX = nameX+nameFrameWidth;
 			lowPointY = nameY;
 			highPointY = nameY+nameFrameHeight;
 			moveName = true;
 		} else {
 			lowPointX = imageX < nameX ? imageX : nameX;
 			highPointX = imageX+sound.getImageWidth() > nameX+nameFrameWidth ? 
 					imageX+sound.getImageWidth() : nameX+nameFrameWidth;
 			lowPointY = imageY < nameY ? imageY : nameY;
 			highPointY = imageY+sound.getImageHeight() > nameY+nameFrameHeight ? 
 					imageY+sound.getImageHeight() : nameY+nameFrameHeight;
 			moveImage = true;
 			moveName = true;
 		}
 		
 		float xPoint = (highPointX-lowPointX)/2;
 		float imageDistanceX = imageX-(lowPointX+xPoint);
 		float nameDistanceX =  nameX-(lowPointX+xPoint);
 		
 		float yPoint = (highPointY-lowPointY)/2;
 		float imageDistanceY = imageY-(lowPointY+yPoint);
 		float nameDistanceY =  nameY-(lowPointY+yPoint);
 		
 		float slotX = column*(width/mGsb.getAutoArrangeColumns());
 		float slotY = row*(height/mGsb.getAutoArrangeRows());
 		
 		if (moveImage) {
 			sound.setImageX(slotX+middlePointX+imageDistanceX);
 			sound.setImageY(slotY+middlePointY+imageDistanceY);
 		}
 		
 		if (moveName) {
 			sound.setNameFrameX(slotX+middlePointX+nameDistanceX);
 			sound.setNameFrameY(slotY+middlePointY+nameDistanceY);
 		}
 		
 		sound.setAutoArrangeColumn(column);
 		sound.setAutoArrangeRow(row);
 		createHistoryCheckpoint();
 	}
 	
 	public boolean placeToFreeSlot(GraphicalSound sound) {
 		try {
     		Slot slot = AutoArrange.getFreeSlot(mGsb.getSoundList(), mGsb.getAutoArrangeColumns(), mGsb.getAutoArrangeRows());
     		moveSoundToSlot(sound, slot.getColumn(), slot.getRow(), sound.getImageX(), sound.getImageY(), sound.getNameFrameX(), sound.getNameFrameY());
     		return true;
     	} catch (NullPointerException e) {
     		Toast.makeText(getApplicationContext(), "No slot available", Toast.LENGTH_SHORT).show();
     		return false;
     	}
 	}
 	
 	public void placeToFreeSpace(GraphicalSound sound) {
 		boolean spaceAvailable = true;
 		
 		float freeSpaceX = 0;
 		float freeSpaceY = 0;
 		
 		int width = mPanel.getWidth();
 		int height = mPanel.getHeight();
 		
 		while (freeSpaceY + sound.getImageHeight() < height) {
 			spaceAvailable = true;
 			for (GraphicalSound spaceEater : mGsb.getSoundList()) {
 				if (((freeSpaceX >= spaceEater.getImageX() && freeSpaceX <= spaceEater.getImageX()+spaceEater.getImageWidth()) || 
 						freeSpaceX+sound.getImageWidth() >= spaceEater.getImageX() && freeSpaceX+sound.getImageWidth() <= spaceEater.getImageX()+spaceEater.getImageWidth()) &&
 						(freeSpaceY >= spaceEater.getImageY() && freeSpaceY <= spaceEater.getImageY()+spaceEater.getImageHeight() ||
 								freeSpaceY+sound.getImageHeight() >= spaceEater.getImageY() && freeSpaceY+sound.getImageHeight() <= spaceEater.getImageY()+spaceEater.getImageHeight())) {
 					spaceAvailable = false;
 					break;
 				}
 			}
 			if (spaceAvailable) {
 				sound.setImageX(freeSpaceX);
 				sound.setImageY(freeSpaceY);
 				sound.generateNameFrameXYFromImageLocation();
 				break;
 			}
 			freeSpaceX = freeSpaceX + 5;
 			if (freeSpaceX + sound.getImageWidth() >= width) {
 				freeSpaceX = 0;
 				freeSpaceY = freeSpaceY + 5;
 			}
 		}
 		if (!spaceAvailable) {
 			sound.setNameFrameX(10);
 			sound.setNameFrameY(sound.getImageHeight()+10);
 			sound.generateImageXYFromNameFrameLocation();
 		}
 	}
 	
 	public boolean onTrackballEvent (MotionEvent event) {
 		if (mMode == EDIT_BOARD && event.getAction() == MotionEvent.ACTION_MOVE && mDragSound != null && 
 				(mLastTrackballEvent == 0 || System.currentTimeMillis() - mLastTrackballEvent > 500)) {
 			mLastTrackballEvent = System.currentTimeMillis();
 			
 			int movementX = 0;
 			int movementY = 0;
 			
 			if (event.getX() > 0) {
 				movementX = 1;
 			} else if (event.getX() < 0) {
 				movementX = -1;
 			}else if (event.getY() > 0) {
 				movementY = 1;
 			} else if (event.getY() < 0) {
 				movementY = -1;
 			}
 			
 			if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
 				mDragSound.setNameFrameX(mDragSound.getNameFrameX() + movementX);
 				mDragSound.setNameFrameY(mDragSound.getNameFrameY() + movementY);
 			}
 			if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
 				mDragSound.setImageX(mDragSound.getImageX() + movementX);
 				mDragSound.setImageY(mDragSound.getImageY() + movementY);
 			}
 			createHistoryCheckpoint();
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	final Runnable mResolutionConverter = new Runnable() {
         public void run() {
 			
 	        if (mGsb.getScreenHeight() == 0 || mGsb.getScreenWidth() == 0) {
 	        	mGsb.setScreenHeight(mWindowHeight);
 				mGsb.setScreenWidth(mWindowWidth);
 	        } else if (mGsb.getScreenHeight() != mWindowHeight || mGsb.getScreenWidth() != mWindowWidth) {
 	        	
                 	AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
                 	builder.setTitle("Display size");
                 	builder.setMessage("Display size used to make this board differs from your display size.\n\n" +
                 			"You can resize this board to fill your display or " +
                 			"fit this board to your display.");
                 	
                 	builder.setPositiveButton("Resize", new DialogInterface.OnClickListener() {
                 		public void onClick(DialogInterface dialog, int whichButton) {
                 			Log.v(TAG, "Resizing board");
                 			float xScale = (float) (mWindowWidth)/(float) (mGsb.getScreenWidth());
                 			float yScale = (float) (mWindowHeight)/(float) (mGsb.getScreenHeight());
     						
     						float avarageScale = xScale+(yScale-xScale)/2;
     						Log.v(TAG, "X scale: \"" + xScale + "\"" + ", old width: \""+mGsb.getScreenWidth() + "\", new width: \"" + mWindowWidth + "\"");
     						Log.v(TAG, "Y scale: \"" + yScale + "\"" + ", old height: \""+mGsb.getScreenHeight() + "\", new height: \"" + mWindowHeight + "\"");
     						Log.v(TAG, "Avarage scale: \"" + avarageScale + "\"");
     						
     						mGsb.setBackgroundX(mGsb.getBackgroundX()*xScale);
     						mGsb.setBackgroundY(mGsb.getBackgroundY()*yScale);
     						mGsb.setBackgroundWidth(mGsb.getBackgroundWidth()*xScale);
     						mGsb.setBackgroundHeight(mGsb.getBackgroundHeight()*yScale);
     						
     						for (GraphicalSound sound : mGsb.getSoundList()) {
     							
     							sound = SoundNameDrawing.getScaledTextSize(sound, avarageScale);
     							
     							sound.setNameFrameX(sound.getNameFrameX()*xScale);
     							sound.setNameFrameY(sound.getNameFrameY()*yScale);
     							
     							sound.setImageX(sound.getImageX()*xScale);
     							sound.setImageY(sound.getImageY()*yScale);
     							sound.setImageWidth(sound.getImageWidth()*avarageScale);
     							sound.setImageHeight(sound.getImageHeight()*avarageScale);
     							
     							if (sound.getLinkNameAndImage()) sound.generateNameFrameXYFromImageLocation();
     						}
     						
     						mGsb.setScreenHeight(mWindowHeight);
     						mGsb.setScreenWidth(mWindowWidth);
     						createHistoryCheckpoint();
                 		}
                 	});
                 	
                 	builder.setNeutralButton("Fit", new DialogInterface.OnClickListener() {
                 		public void onClick(DialogInterface dialog, int whichButton) {
                 			Log.v(TAG, "Fitting board");
                 			
                 			float xScale = (float) (mWindowWidth)/(float) (mGsb.getScreenWidth());
                 			float yScale = (float) (mWindowHeight)/(float) (mGsb.getScreenHeight());
                 			
                 			boolean xFillsDisplay = xScale < yScale;
     						float applicableScale = (xScale < yScale) ? xScale : yScale;
     						
     						float hiddenAreaSize;
     						
     						if (xFillsDisplay) {
     							hiddenAreaSize = ((float) mWindowHeight-(float) mGsb.getScreenHeight()*applicableScale)/2;
     						} else {
     							hiddenAreaSize = ((float) mWindowWidth-(float) mGsb.getScreenWidth()*applicableScale)/2;
     						}
     						
     						Log.v(TAG, "X scale: \"" + xScale + "\"" + ", old width: \""+mGsb.getScreenWidth() + "\", new width: \"" + mWindowWidth + "\"");
     						Log.v(TAG, "Y scale: \"" + yScale + "\"" + ", old height: \""+mGsb.getScreenHeight() + "\", new height: \"" + mWindowHeight + "\"");
     						Log.v(TAG, "Applicable scale: \"" + applicableScale + "\"");
     						Log.v(TAG, "Hidden area size: \"" + hiddenAreaSize + "\"");
     						
     						mGsb.setBackgroundWidth(mGsb.getBackgroundWidth()*applicableScale);
     						mGsb.setBackgroundHeight(mGsb.getBackgroundHeight()*applicableScale);
     						
     						if (xFillsDisplay) {
     							mGsb.setBackgroundX(mGsb.getBackgroundX()*applicableScale);
     							mGsb.setBackgroundY(hiddenAreaSize+mGsb.getBackgroundY()*applicableScale);
     						} else {
     							mGsb.setBackgroundX(hiddenAreaSize+mGsb.getBackgroundX()*applicableScale);
         						mGsb.setBackgroundY(mGsb.getBackgroundY()*applicableScale);
     						}
     						
     						for (GraphicalSound sound : mGsb.getSoundList()) {
     							
     							sound = SoundNameDrawing.getScaledTextSize(sound, applicableScale);
     							
     							if (xFillsDisplay) {
     								sound.setNameFrameX(sound.getNameFrameX()*applicableScale);
         							sound.setNameFrameY(hiddenAreaSize+sound.getNameFrameY()*applicableScale);
         							sound.setImageX(sound.getImageX()*applicableScale);
         							sound.setImageY(hiddenAreaSize+sound.getImageY()*applicableScale);
         						} else {
         							Log.w(TAG, "sound: " + sound.getName());
         							Log.w(TAG, "hiddenAreaSize: " + hiddenAreaSize + " sound.getNameFrameX(): " + sound.getNameFrameX() + " applicableScale: " + applicableScale);
         							Log.w(TAG, "hiddenAreaSize+sound.getNameFrameX()*applicableScale: " + (hiddenAreaSize+sound.getNameFrameX()*applicableScale));
         							sound.setNameFrameX(hiddenAreaSize+sound.getNameFrameX()*applicableScale);
         							sound.setNameFrameY(sound.getNameFrameY()*applicableScale);
             						sound.setImageX(hiddenAreaSize+sound.getImageX()*applicableScale);
         							sound.setImageY(sound.getImageY()*applicableScale);
         						}
     							
     							sound.setImageWidth(sound.getImageWidth()*applicableScale);
     							sound.setImageHeight(sound.getImageHeight()*applicableScale);
     							
     							if (sound.getLinkNameAndImage()) sound.generateNameFrameXYFromImageLocation();
     						}
     						
     						GraphicalSound blackBar1 = new GraphicalSound();
     						blackBar1.setNameFrameInnerColor(255, 0, 0, 0);
     						
     						GraphicalSound blackBar2 = new GraphicalSound();
     						blackBar2.setNameFrameInnerColor(255, 0, 0, 0);
     						
     						if (xFillsDisplay) {
     							blackBar1.setName("I hide top of the board.");
     							blackBar2.setName("I hide bottom of the board.");
     							blackBar1.setPath(new File(SoundboardMenu.mTopBlackBarSoundFilePath));
     							blackBar2.setPath(new File(SoundboardMenu.mBottomBlackBarSoundFilePath));
     							blackBar1.setNameFrameY(hiddenAreaSize);
     							blackBar2.setNameFrameY((float) mWindowHeight-hiddenAreaSize);
     						} else {
     							blackBar1.setName("I hide left side of the board.");
     							blackBar2.setName("I hide right side of the board.");
     							blackBar1.setPath(new File(SoundboardMenu.mLeftBlackBarSoundFilePath));
     							blackBar2.setPath(new File(SoundboardMenu.mRightBlackBarSoundFilePath));
     							blackBar1.setNameFrameX(hiddenAreaSize);
     							blackBar2.setNameFrameX((float) mWindowWidth-hiddenAreaSize);
     						}
     						
     						mGsb.addSound(blackBar1);
     						mGsb.addSound(blackBar2);
     						
     						mGsb.setScreenHeight(mWindowHeight);
     						mGsb.setScreenWidth(mWindowWidth);
     						createHistoryCheckpoint();
                 		}
                 	});
                 	
                 	builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                 		public void onClick(DialogInterface dialog, int whichButton) {
                 			mGsb.setScreenHeight(mWindowHeight);
     						mGsb.setScreenWidth(mWindowWidth);
     						createHistoryCheckpoint();
                 		}
                 	});
                 	
                 	builder.show();
 	        }
         }
     };
 	
 	class DrawingPanel extends SurfaceView implements SurfaceHolder.Callback {
 		
 		public DrawingPanel(Context context) {
 			super(context);
             getHolder().addCallback(this);
             mThread = new DrawingThread(getHolder(), this);
 		}
 		
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			if (mThread == null) return false;
 			synchronized (mThread.getSurfaceHolder()) {
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					
 					if (mMoveBackground) {
 						mBackgroundLeftDistance = event.getX() - mGsb.getBackgroundX();
 						mBackgroundTopDistance = event.getY() - mGsb.getBackgroundY();
 					} else {
 						ListIterator<GraphicalSound> iterator = mGsb.getSoundList().listIterator();
 						while (iterator.hasNext()) {iterator.next();}
 						while (iterator.hasPrevious()) {
 							GraphicalSound sound = iterator.previous();
 							String soundPath = sound.getPath().getAbsolutePath();
 							SoundNameDrawing soundNameDrawing = new SoundNameDrawing(sound);
 							float nameFrameX = sound.getNameFrameX();
 							float nameFrameY = sound.getNameFrameY();
 							float nameFrameWidth = soundNameDrawing.getNameFrameRect().width();
 							float nameFrameHeight = soundNameDrawing.getNameFrameRect().height();
 							if (event.getX() >= sound.getImageX() && 
 									event.getX() <= sound.getImageWidth() + sound.getImageX() &&
 									event.getY() >= sound.getImageY() &&
 									event.getY() <= sound.getImageHeight() + sound.getImageY())  {
 								if (mCopyColor != 0) {
 									copyColor(sound);
 								} else {
 									mDragTarget = DRAG_IMAGE;
 									initializeDrag(event, sound);
 								}
 								break;
 							} else if ((event.getX() >= sound.getNameFrameX() && 
 									event.getX() <= nameFrameWidth + sound.getNameFrameX() &&
 									event.getY() >= sound.getNameFrameY() && 
 									event.getY() <= nameFrameHeight + sound.getNameFrameY()) ||
 									
 									soundPath.equals(SoundboardMenu.mTopBlackBarSoundFilePath) && event.getY() <= nameFrameY ||
 									soundPath.equals(SoundboardMenu.mBottomBlackBarSoundFilePath) && event.getY() >= nameFrameY ||
 									soundPath.equals(SoundboardMenu.mLeftBlackBarSoundFilePath) && event.getX() <= nameFrameX ||
 									soundPath.equals(SoundboardMenu.mRightBlackBarSoundFilePath) && event.getX() >= nameFrameX) {
 								if (mCopyColor != 0) {
 									copyColor(sound);
 								} else {
 									mDragTarget = DRAG_TEXT;
 									initializeDrag(event, sound);
 								}
 								break;
 							}
 						}
 						
 						mClickTime = Calendar.getInstance().getTimeInMillis();
 					}
 						
 				} else if(event.getAction() == MotionEvent.ACTION_MOVE){
 					if (mMoveBackground) {
 						mGsb.setBackgroundX(event.getX() - mBackgroundLeftDistance);
 						mGsb.setBackgroundY(event.getY() - mBackgroundTopDistance);
 					} else if (mDrawDragSound == true) {
 						if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
 							mDragSound.setNameFrameX(event.getX()-mNameFrameDragDistanceX);
 							mDragSound.setNameFrameY(event.getY()-mNameFrameDragDistanceY);
 						}
 						if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
 							mDragSound.setImageX(event.getX() - mImageDragDistanceX);
 							mDragSound.setImageY(event.getY() - mImageDragDistanceY);
 						}
 					}
 					
 				} else if(event.getAction() == MotionEvent.ACTION_UP){
 					if (mMoveBackground) {
 						mGsb.setBackgroundX(event.getX() - mBackgroundLeftDistance);
 						mGsb.setBackgroundY(event.getY() - mBackgroundTopDistance);
 					} else if (mDrawDragSound == true && Calendar.getInstance().getTimeInMillis()-mClickTime < 200) {
 						mClickTime = 0;
 						mDragSound.setNameFrameX(mInitialNameFrameX);
 			  			mDragSound.setNameFrameY(mInitialNameFrameY);
 			  			mDragSound.setImageX(mInitialImageX);
 			  			mDragSound.setImageY(mInitialImageY);
 			  			mGsb.getSoundList().add(mDragSound);
 			  			mDrawDragSound = false;
 			  			invalidate();
 						
 						final CharSequence[] items = {"Info", "Name settings", "Image settings", "Sound settings",
 								"Duplicate sound", "Remove sound", "Set as..."};
 
 				    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
 				    	optionsBuilder.setTitle("Options");
 				    	optionsBuilder.setItems(items, new DialogInterface.OnClickListener() {
 				    	    public void onClick(DialogInterface dialog, int item) {
 				    	    	
 				    	    	if (item == 0) {
 				    	    		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(mDragSound);
 				    	    		AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
 				    	    		builder.setTitle("Sound info");
 				                	builder.setMessage("Name:\n"+mDragSound.getName()+
 				                					"\n\nSound path:\n"+mDragSound.getPath()+
 				                					"\n\nImage path:\n"+mDragSound.getImagePath()+
 				                					"\n\nActive image path:\n"+mDragSound.getActiveImagePath()+
 				                					"\n\nName and image linked:\n"+mDragSound.getLinkNameAndImage()+
 				                					"\n\nHide image or text:\n"+mDragSound.getHideImageOrText()+
 				                					"\n\nImage X:\n"+mDragSound.getImageX()+
 				                					"\n\nImage Y:\n"+mDragSound.getImageY()+
 				                					"\n\nImage width:\n"+mDragSound.getImageWidth()+
 				                					"\n\nImage height:\n"+mDragSound.getImageHeight()+
 				                					"\n\nName frame X:\n"+mDragSound.getNameFrameX()+
 				                					"\n\nName frame Y:\n"+mDragSound.getNameFrameY()+
 				                					"\n\nName frame width:\n"+soundNameDrawing.getNameFrameRect().width()+
 				                					"\n\nName frame height:\n"+soundNameDrawing.getNameFrameRect().height()+
 				                					"\n\nAuto arrange column:\n"+mDragSound.getAutoArrangeColumn()+
 				                					"\n\nAuto arrange row:\n"+mDragSound.getAutoArrangeRow()+
 				                					"\n\nSecond click action:\n"+mDragSound.getSecondClickAction()+
 				                					"\n\nLeft volume:\n"+mDragSound.getVolumeLeft()+
 				                					"\n\nRight volume:\n"+mDragSound.getVolumeRight()+
 				                					"\n\nName frame border color:\n"+mDragSound.getNameFrameBorderColor()+
 				                					"\n\nName frame inner color:\n"+mDragSound.getNameFrameInnerColor()+
 				                					"\n\nName text color:\n"+mDragSound.getNameTextColor()+
 				                					"\n\nName text size:\n"+mDragSound.getNameSize()+
 				                					"\n\nShow name frame border paint:\n"+mDragSound.getShowNameFrameBorderPaint()+
 				                					"\n\nShow name frame inner paint:\n"+mDragSound.getShowNameFrameBorderPaint());
 				                					
 				                	builder.show();
 				    	    	} else if (item == 1) {
 				    	    		
 					    	    	LayoutInflater inflater = (LayoutInflater) GraphicalSoundboardEditor.this.
 				    	    			getSystemService(LAYOUT_INFLATER_SERVICE);
 				                	View layout = inflater.inflate(
 				                			R.layout.graphical_soundboard_editor_alert_sound_name_settings, 
 				                			(ViewGroup) findViewById(R.id.alert_settings_root));
 				                	
 				                	SoundNameDrawing soundNameDrawing = new SoundNameDrawing(mDragSound);
 				                	Bundle colorChangerBundle = new Bundle();
 				                	colorChangerBundle.putString("nameKey", mDragSound.getName());
 				                	colorChangerBundle.putFloat("nameFrameWidthKey", soundNameDrawing.getNameFrameRect().width());
 				                	colorChangerBundle.putFloat("nameFrameHeightKey", soundNameDrawing.getNameFrameRect().height());
 				                	colorChangerBundle.putInt("nameTextColorKey", mDragSound.getNameTextColor());
 				                	colorChangerBundle.putInt("nameFrameInnerColorKey", mDragSound.getNameFrameInnerColor());
 				                	colorChangerBundle.putInt("nameFrameBorderColorKey", mDragSound.getNameFrameBorderColor());
 				                	final Bundle colorChangerBundleFinal = colorChangerBundle;
 				                	
 				                	final EditText soundNameInput = 
 				              	  		(EditText) layout.findViewById(R.id.soundNameInput);
 				              	  	soundNameInput.setText(mDragSound.getName());
 				              	  	
 				              	  	final EditText soundNameSizeInput = 
 				              	  		(EditText) layout.findViewById(R.id.soundNameSizeInput);
 				              	  	soundNameSizeInput.setText(Float.toString(mDragSound.getNameSize()));
 				              	  	
 				              	  	final CheckBox checkShowSoundName = 
 				              	  		(CheckBox) layout.findViewById(R.id.showSoundNameCheckBox);
 				              	  	checkShowSoundName.setChecked(mDragSound.getHideImageOrText() != GraphicalSound.HIDE_TEXT);
 				              	  	
 				              	  	final CheckBox checkShowInnerPaint = 
 				              	  		(CheckBox) layout.findViewById(R.id.showInnerPaintCheckBox);
 				              	  	checkShowInnerPaint.setChecked(mDragSound.getShowNameFrameInnerPaint());
 				              	  	
 				              	  	final CheckBox checkShowBorderPaint = 
 				              	  		(CheckBox) layout.findViewById(R.id.showBorderPaintCheckBox);
 				              	  	checkShowBorderPaint.setChecked(mDragSound.getShowNameFrameBorderPaint());
 				              	  	
 				              	  	final Button nameColorButton = 
 				              	  		(Button) layout.findViewById(R.id.nameColorButton);
 				              	  	nameColorButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setName(soundNameInput.getText().toString());
 				    			  			mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			
 				    			  			Intent i = new Intent(GraphicalSoundboardEditor.this, ColorChanger.class);
 				    		        		i.putExtra("parentKey", "changeNameColor");
 				    		        		i.putExtras(colorChangerBundleFinal);
 				    		            	startActivityForResult(i, CHANGE_NAME_COLOR);
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button innerPaintColorButton = 
 				              	  		(Button) layout.findViewById(R.id.innerPaintColorButton);
 				              	  	innerPaintColorButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setName(soundNameInput.getText().toString());
 				    			  			mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			
 				    			  			Intent i = new Intent(GraphicalSoundboardEditor.this, ColorChanger.class);
 				    		        		i.putExtra("parentKey", "changeinnerPaintColor");
 				    		        		i.putExtras(colorChangerBundleFinal);
 				    		            	startActivityForResult(i, CHANGE_INNER_PAINT_COLOR);
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button borderPaintColorButton = 
 				              	  		(Button) layout.findViewById(R.id.borderPaintColorButton);
 				              	  	borderPaintColorButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setName(soundNameInput.getText().toString());
 				    			  			mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			
 				    			  			Intent i = new Intent(GraphicalSoundboardEditor.this, ColorChanger.class);
 				    		        		i.putExtra("parentKey", "changeBorderPaintColor");
 				    		        		i.putExtras(colorChangerBundleFinal);
 				    		            	startActivityForResult(i, CHANGE_BORDER_PAINT_COLOR);
 				    					}
 				              	  	});
 				              	  	
 				              	  	AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
 				              	  	builder.setView(layout);
 				              	  	builder.setTitle("Name settings");
 				          	  	
 				    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          		public void onClick(DialogInterface dialog, int whichButton) {
 				    	          			boolean notifyIncorrectValue = false;
 				    	          			
 				    	          			if (checkShowSoundName.isChecked() == false) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.HIDE_TEXT);
 				    	          			} else if (checkShowSoundName.isChecked() && 
 				    	          				mDragSound.getHideImageOrText() == GraphicalSound.HIDE_TEXT) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.SHOW_ALL);
 				    	          				mDragSound.generateImageXYFromNameFrameLocation();
 				    	          			}
 				    	          			mDragSound.setShowNameFrameInnerPaint(checkShowInnerPaint.isChecked());
 				    	          			mDragSound.setShowNameFrameBorderPaint(checkShowBorderPaint.isChecked());
 				    	          			
 				    	          			mDragSound.setName(soundNameInput.getText().toString());
 				    			  			
 				    			  			try {
 				    			  				mDragSound.setNameSize(Float.valueOf(
 				    			  						soundNameSizeInput.getText().toString()).floatValue());
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			
 				    	          			if (mDragSound.getLinkNameAndImage()) {
 				    			  				mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			}
 				    	          			
 				    	          			if (notifyIncorrectValue == true) {
 				    	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
 				    	          						Toast.LENGTH_SHORT).show();
 				    	          			}
 				    	          			
 				    	          			createHistoryCheckpoint();
 				    	          		}
 				    	          	});
 	
 				    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				    	          	builder.show();
 		    						
 				    	    	} else if (item == 2) {
 				    	    		
 				    	    		LayoutInflater inflater = (LayoutInflater) GraphicalSoundboardEditor.this.
 				    	    			getSystemService(LAYOUT_INFLATER_SERVICE);
 				                	View layout = inflater.inflate(
 				                			R.layout.graphical_soundboard_editor_alert_sound_image_settings, 
 				                			(ViewGroup) findViewById(R.id.alert_settings_root));
 
 				              	  	
 				              	  	final CheckBox checkShowSoundImage = 
 				              	  		(CheckBox) layout.findViewById(R.id.showSoundImageCheckBox);
 				              	  	checkShowSoundImage.setChecked(mDragSound.getHideImageOrText() != GraphicalSound.HIDE_IMAGE);
 				              	  	
 				              	  	soundImageWidthText = (TextView) layout.findViewById(R.id.soundImageWidthText);
 				              	  	soundImageWidthText.setText("Width (" + mDragSound.getImage().getWidth() + ")");
 				            	  	
 				            	  	soundImageHeightText = (TextView) layout.findViewById(R.id.soundImageHeightText);
 				            	  	soundImageHeightText.setText("Height (" + mDragSound.getImage().getHeight() + ")");
 				              	  	
 				              	  	final EditText soundImageWidthInput = 
 				              	  		(EditText) layout.findViewById(R.id.soundImageWidthInput);
 				              	  	soundImageWidthInput.setText(Float.toString(mDragSound.getImageWidth()));  	
 				              	  	
 				              	  	final EditText soundImageHeightInput = 
 				              	  		(EditText) layout.findViewById(R.id.soundImageHeightInput);
 				              	  	soundImageHeightInput.setText(Float.toString(mDragSound.getImageHeight()));
 				              	  	
 				              	  	final float widthHeightScale = 
 				              	  		mDragSound.getImageWidth() / mDragSound.getImageHeight();
 				              	  	
 				              	  	final CheckBox scaleWidthHeight = 
 				              	  		(CheckBox) layout.findViewById(R.id.scaleWidthHeightCheckBox);
 				              	  	scaleWidthHeight.setChecked(true);
 				              	  	
 				              	  	soundImageWidthInput.setOnKeyListener(new OnKeyListener() {
 										public boolean onKey(View v, int keyCode, KeyEvent event) {
 											if (scaleWidthHeight.isChecked()) {
 												try {
 													float value = Float.valueOf(soundImageWidthInput.getText().toString()).floatValue();
 													soundImageHeightInput.setText(Float.valueOf(value/widthHeightScale).toString());
 												} catch(NumberFormatException nfe) {}
 											}
 											return false;
 										}
 				              	  	});
 				              	  	
 				              	  	soundImageHeightInput.setOnKeyListener(new OnKeyListener() {
 										public boolean onKey(View v, int keyCode, KeyEvent event) {
 											if (scaleWidthHeight.isChecked()) {
 												try {
 													float value = Float.valueOf(soundImageHeightInput.getText().toString()).floatValue();
 													soundImageWidthInput.setText(Float.valueOf(value*widthHeightScale).toString());
 												} catch(NumberFormatException nfe) {}
 											}
 											return false;
 										}
 				              	  	});
 				              	  	
 				              	  	final Button revertSizeButton = (Button) layout.findViewById(R.id.revertImageSizeButton);
 				              	  	revertSizeButton.setOnClickListener(new OnClickListener() {
 				              	  		public void onClick(View v) {
 				              	  			soundImageWidthInput.setText(Float.valueOf(mDragSound.getImageWidth()).toString());
 				              	  			soundImageHeightInput.setText(Float.valueOf(mDragSound.getImageHeight()).toString());
 				              	  		}
 				              	  	});
 				              	  	
 				              	  	final Button setSoundImageButton = (Button) layout.findViewById(R.id.setSoundImageButton);
 				              	  	setSoundImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						selectImageFile();
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button resetSoundImageButton = (Button) layout.findViewById(R.id.resetSoundImageButton);
 				              	  	resetSoundImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.sound));
 				    						mDragSound.setImagePath(null);
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button setSoundActiveImageButton = (Button) layout.findViewById(R.id.setSoundActiveImageButton);
 				              	  	setSoundActiveImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						selectActiveImageFile();
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button resetSoundActiveImageButton = (Button) layout.findViewById(R.id.resetSoundActiveImageButton);
 				              	  	resetSoundActiveImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setActiveImage(null);
 				    						mDragSound.setActiveImagePath(null);
 				    					}
 				              	  	});
 				              	  	
 				              	  	AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
 				              	  	builder.setView(layout);
 				              	  	builder.setTitle("Image settings");
 				          	  	
 				    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          		public void onClick(DialogInterface dialog, int whichButton) {
 				    	          			boolean notifyIncorrectValue = false;
 				    	          			
 				    	          			if (checkShowSoundImage.isChecked() == false) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.HIDE_IMAGE);
 				    	          			} else if (checkShowSoundImage.isChecked() && 
 				    	          				mDragSound.getHideImageOrText() == GraphicalSound.HIDE_IMAGE) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.SHOW_ALL);
 				    	          			}
 				    	          			
 				    	          			try {
 				    	          				mDragSound.setImageWidth(Float.valueOf(
 				    	          						soundImageWidthInput.getText().toString()).floatValue());
 				    	          				mDragSound.setImageHeight(Float.valueOf(
 				    	          						soundImageHeightInput.getText().toString()).floatValue());	
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			mDragSound.generateImageXYFromNameFrameLocation();
 				    	          			
 				    	          			if (notifyIncorrectValue == true) {
 				    	          				Toast.makeText(getApplicationContext(), "Incorrect value", Toast.LENGTH_SHORT).show();
 				    	          			}
 				    	          			createHistoryCheckpoint();
 				    	          		}
 				    	          	});
 
 				    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				    	          	builder.show();
 				    	          	
 				    	    	} else if (item == 3) {
 				    	    		
 				    	    		LayoutInflater inflater = (LayoutInflater) GraphicalSoundboardEditor.this.
 			    	    				getSystemService(LAYOUT_INFLATER_SERVICE);
 				    	    		View layout = inflater.inflate(
 			                			R.layout.graphical_soundboard_editor_alert_sound_settings, 
 			                			(ViewGroup) findViewById(R.id.alert_settings_root));
 				    	    		
 				    	    		final CheckBox linkNameAndImageCheckBox = 
 				              	  		(CheckBox) layout.findViewById(R.id.linkNameAndImageCheckBox);
 				    	    		linkNameAndImageCheckBox.setChecked(mDragSound.getLinkNameAndImage());
 				    	    		
 				    	    		final Button changeSoundPathButton = 
 				              	  		(Button) layout.findViewById(R.id.changeSoundPathButton);
 				    	    		changeSoundPathButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						Intent i = new Intent(GraphicalSoundboardEditor.this, FileExplorer.class);
 				    						i.putExtra("parentKey", "changeSoundPath");
 				    						i.putExtra("projectNameKey", mBoardName);
 				    						startActivityForResult(i, CHANGE_SOUND_PATH);
 				    					}
 				              	  	});
 				    	    		
 				    	    		final Button secondClickActionButton = 
 				              	  		(Button) layout.findViewById(R.id.secondClickActionButton);
 				    	    		secondClickActionButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						final CharSequence[] items = {"Play new", "Pause", "Stop"};
 						                	AlertDialog.Builder secondClickBuilder = new AlertDialog.Builder(
 						                			GraphicalSoundboardEditor.this);
 						                	secondClickBuilder.setTitle("Action");
 						                	secondClickBuilder.setItems(items, new DialogInterface.OnClickListener() {
 						                	    public void onClick(DialogInterface dialog, int item) {
 						                	    	if (item == 0) {
 						                	    		mDragSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_PLAY_NEW);
 						                	    	} else if (item == 1) {
 						                	    		mDragSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_PAUSE);
 						                	    	} else if (item == 2) {
 						                	    		mDragSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_STOP);
 						                	    	}
 						                	    }
 						                	});
 						                	AlertDialog secondClickAlert = secondClickBuilder.create();
 						                	secondClickAlert.show();
 				    					}
 				              	  	});
 				    	    		
 				    	    		final EditText leftVolumeInput = (EditText) layout.findViewById(R.id.leftVolumeInput);
 				            	  	leftVolumeInput.setText(Float.toString(mDragSound.getVolumeLeft()*100) + "%");
 				            	  	final EditText rightVolumeInput = (EditText) layout.findViewById(R.id.rightVolumeInput);
 				            	  	rightVolumeInput.setText(Float.toString(mDragSound.getVolumeRight()*100) + "%");
 				    	    		
 				    	    		AlertDialog.Builder builder = new AlertDialog.Builder(GraphicalSoundboardEditor.this);
 				              	  	builder.setView(layout);
 				              	  	builder.setTitle("Sound settings");
 				          	  	
 				    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          		public void onClick(DialogInterface dialog, int whichButton) {
 				    	          			mDragSound.setLinkNameAndImage(linkNameAndImageCheckBox.isChecked());
 				    	          			if (mDragSound.getLinkNameAndImage()) {
 					    	          			mDragSound.generateImageXYFromNameFrameLocation();
 				    	          			}
 				    	          			
 				    	          			boolean notifyIncorrectValue = false;
 				    	          			Float leftVolumeValue = null;
 				    	          			try {
 				    	          				String leftVolumeString = leftVolumeInput.getText().toString();
 				    	          				if (leftVolumeString.contains("%")) {
 				    	          					leftVolumeValue = Float.valueOf(leftVolumeString.substring(0, 
 				    	          							leftVolumeString.indexOf("%"))).floatValue()/100;
 				    	          				} else {
 				    	          					leftVolumeValue = Float.valueOf(leftVolumeString).floatValue()/100;
 				    	          				}
 				    	          				
 				    	          				
 				    	          				if (leftVolumeValue >= 0 && leftVolumeValue <= 1 && leftVolumeValue != null) {
 				    	          					mDragSound.setVolumeLeft(leftVolumeValue);
 				    		          			} else {
 				    		          				notifyIncorrectValue = true;
 				    		          			}
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			
 				    	          			Float rightVolumeValue = null;
 				    	          			try {
 				    	          				String rightVolumeString = rightVolumeInput.getText().toString();
 				    	          				if (rightVolumeString.contains("%")) {
 				    	          				rightVolumeValue = Float.valueOf(rightVolumeString.substring(0, 
 				    	          						rightVolumeString.indexOf("%"))).floatValue()/100;
 				    	          				} else {
 				    	          					rightVolumeValue = Float.valueOf(rightVolumeString).floatValue()/100;
 				    	          				}
 				    	          				
 				    	          				if (rightVolumeValue >= 0 && rightVolumeValue <= 1 && rightVolumeValue != null) {
 				    	          					mDragSound.setVolumeRight(rightVolumeValue);
 				    		          			} else {
 				    		          				notifyIncorrectValue = true;
 				    		          			}
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			
 				    	          			if (notifyIncorrectValue == true) {
 				    	          				Toast.makeText(getApplicationContext(), "Incorrect value", Toast.LENGTH_SHORT).show();
 				    	          			}
 				    	          			createHistoryCheckpoint();
 				    	          		}
 				    	          	});
 
 				    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				    	          	builder.show();
 				    	    	} else if (item == 4) {
 				    	    		GraphicalSound duplicate = (GraphicalSound) mDragSound.clone();
 				    	    		if (mGsb.getAutoArrange()) {
 				    	    			 if (placeToFreeSlot(duplicate)) {
 				    	    				 mGsb.getSoundList().add(duplicate);
 				    	    			 }
 				    	    		} else {
 				    	    			placeToFreeSpace(duplicate);
 				    	    			mGsb.getSoundList().add(duplicate);
 				    	    		}
 				    	    		createHistoryCheckpoint();
 				    	    		
 				    	    	} else if (item == 5) {
 				                	
 				                	LayoutInflater removeInflater = (LayoutInflater) 
 				                			GraphicalSoundboardEditor.this.getSystemService(LAYOUT_INFLATER_SERVICE);
 				                	View removeLayout = removeInflater.inflate(
 				                			R.layout.graphical_soundboard_editor_alert_remove_sound,
 				                	        (ViewGroup) findViewById(R.id.alert_remove_sound_root));
 				              	  	
 				              	  	final CheckBox removeFileCheckBox = 
 				              	  		(CheckBox) removeLayout.findViewById(R.id.removeFile);
 				              	  	removeFileCheckBox.setText(" DELETE " + mDragSound.getPath().getAbsolutePath());
 				              	  	
 				              	  	AlertDialog.Builder removeBuilder = new AlertDialog.Builder(
 				              	  		GraphicalSoundboardEditor.this);
 				              	  	removeBuilder.setView(removeLayout);
 				              	  	removeBuilder.setTitle("Removing " + mDragSound.getName());
 				          	  	
 				              	  	removeBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          	  	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	  		if (removeFileCheckBox.isChecked() == true) {
 				    	          	  			mDragSound.getPath().delete();
 				    	          	  		}
 				    	          	  		mGsb.getSoundList().remove(mDragSound);
 				    	          	  		createHistoryCheckpoint();
 				    	          	    }
 				    	          	});
 
 				              	  	removeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				              	  	removeBuilder.show();
 				    	    	} else if (item == 6) {
 				    	    		final CharSequence[] items = {"Ringtone", "Notification", "Alerts"};
 
 				                	AlertDialog.Builder setAsBuilder = new AlertDialog.Builder(
 				                			GraphicalSoundboardEditor.this);
 				                	setAsBuilder.setTitle("Set as...");
 				                	setAsBuilder.setItems(items, new DialogInterface.OnClickListener() {
 				                	    public void onClick(DialogInterface dialog, int item) {
 				                	    	String filePath = mDragSound.getPath().getAbsolutePath();
 
 				                	    	ContentValues values = new ContentValues();
 				                	    	values.put(MediaStore.MediaColumns.DATA, filePath);
 				                        	values.put(MediaStore.MediaColumns.TITLE, mDragSound.getName());
 				                        	
 				                        	values.put(MediaStore.MediaColumns.MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(
 				                        			filePath.substring(filePath.lastIndexOf('.'+1))));
 				                        	values.put(MediaStore.Audio.Media.ARTIST, "Artist");
 				                	    	
 				                	    	int selectedAction = 0;
 				                	    	if (item == 0) {
 				                	    		selectedAction = RingtoneManager.TYPE_RINGTONE;
 				                	    		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
 				                            	values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
 				                            	values.put(MediaStore.Audio.Media.IS_ALARM, false);
 				                            	values.put(MediaStore.Audio.Media.IS_MUSIC, false);
 				                	    	} else if (item == 1) {
 				                	    		selectedAction = RingtoneManager.TYPE_NOTIFICATION;
 				                	    		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
 				                            	values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
 				                            	values.put(MediaStore.Audio.Media.IS_ALARM, false);
 				                            	values.put(MediaStore.Audio.Media.IS_MUSIC, false);
 				                	    	} else if (item == 2) {
 				                	    		selectedAction = RingtoneManager.TYPE_ALARM;
 				                	    		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
 				                            	values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
 				                            	values.put(MediaStore.Audio.Media.IS_ALARM, true);
 				                            	values.put(MediaStore.Audio.Media.IS_MUSIC, false);
 				                	    	}
 				                        	
 				                        	Uri uri = MediaStore.Audio.Media.getContentUriForPath(filePath);
 				                        	getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + filePath + "\"", null);
 				                        	Uri newUri = GraphicalSoundboardEditor.this.getContentResolver().insert(uri, values);
 				                        	
 				                        	RingtoneManager.setActualDefaultRingtoneUri(GraphicalSoundboardEditor.this, selectedAction, newUri);
 				                        	
 				                	    }
 				                	});
 				                	AlertDialog setAsAlert = setAsBuilder.create();
 				                	setAsAlert.show();
 				    	    	}
 				            	
 				    	    }
 				    	});
 				    	AlertDialog optionsAlert = optionsBuilder.create();
 				    	optionsAlert.show();
 				    	
 				    	invalidate();
 					} else if (mDrawDragSound == true) {
 						if (mGsb.getAutoArrange()) {
 							
 							int width = mPanel.getWidth();
 							int height = mPanel.getHeight();
       						
       						int column = -1, i = 0;
       						while (column == -1) {
       							if (event.getX() >= i*(width/mGsb.getAutoArrangeColumns()) && event.getX() <= (i+1)*(width/(mGsb.getAutoArrangeColumns()))) {
       								column = i;
         						}
       							if (i > 1000) {
       								Log.e(TAG, "column fail");
       								mDragSound.getAutoArrangeColumn();
       								break;
       							}
       							i++;
       						}
       						i = 0;
       						int row = -1;
       						while (row == -1) {
       							if (event.getY() >= i*(height/mGsb.getAutoArrangeRows()) && event.getY() <= (i+1)*(height/(mGsb.getAutoArrangeRows()))) {
       								row = i;
         						}
       							if (i > 1000) {
       								Log.e(TAG, "row fail");
       								mDragSound.getAutoArrangeRow();
       								break;
       							}
       							i++;
       						}
       						
       						GraphicalSound swapSound = null;
       						for (GraphicalSound sound : mGsb.getSoundList()) {
       							if (sound.getAutoArrangeColumn() == column && sound.getAutoArrangeRow() == row) {
       								swapSound = sound;
       							}
       						}
       						
       						if (column == mDragSound.getAutoArrangeColumn() && row == mDragSound.getAutoArrangeRow()) {
       							moveSound(event.getX(), event.getY());
       						} else {
       							try {
       								moveSoundToSlot(swapSound, mDragSound.getAutoArrangeColumn(), mDragSound.getAutoArrangeRow(), 
       										swapSound.getImageX(), swapSound.getImageY(), swapSound.getNameFrameX(), swapSound.getNameFrameY());
       							} catch (NullPointerException e) {}
       							moveSoundToSlot(mDragSound, column, row, mInitialImageX, mInitialImageY, mInitialNameFrameX, mInitialNameFrameY);
       							mGsb.addSound(mDragSound);
       							mDrawDragSound = false;
       						}
   							
 						} else {
 							moveSound(event.getX(), event.getY());
 						}
 						createHistoryCheckpoint();
 						
 					}
 				}
 				
 				return true;
 			}
 		}
 		
 		@Override
         public void onDraw(Canvas canvas) {
 			if (canvas == null) {
 				Log.w(TAG, "Got null canvas");
 			} else {
 				super.dispatchDraw(canvas);
 				
 				if (mFirstDraw) {
 					mFirstDraw = false;
 					mWindowHeight = mPanel.getHeight();
 					mWindowWidth = mPanel.getWidth();
 					mHandler.post(mResolutionConverter);
 				}
 				
 				Paint bgColor = new Paint();
 				bgColor.setColor(mGsb.getBackgroundColor());
 				canvas.drawPaint(bgColor);
 				
 				if (mGsb.getUseBackgroundImage() == true && mGsb.getBackgroundImagePath().exists()) {
 					RectF bitmapRect = new RectF();
 					bitmapRect.set(mGsb.getBackgroundX(), mGsb.getBackgroundY(), 
 							mGsb.getBackgroundWidth() + mGsb.getBackgroundX(), mGsb.getBackgroundHeight() + mGsb.getBackgroundY());
 					
 					Paint bgImage = new Paint();
 					bgImage.setColor(mGsb.getBackgroundColor());
 					
 					try {
 						canvas.drawBitmap(mGsb.getBackgroundImage(), null, bitmapRect, bgImage);
 					} catch(NullPointerException npe) {
 						Log.e(TAG, "Unable to draw image " + mGsb.getBackgroundImagePath().getAbsolutePath());
 						mGsb.setUseBackgroundImage(false);
 					}
 				}
 				
 				try {
 					ArrayList<GraphicalSound> drawList = new ArrayList<GraphicalSound>();
 					drawList.addAll(mGsb.getSoundList());
 					if (mDrawDragSound) drawList.add(mDragSound);
 					
 					for (GraphicalSound sound : drawList) {
 						Paint barPaint = new Paint();
 						barPaint.setColor(sound.getNameFrameInnerColor());
 						String soundPath = sound.getPath().getAbsolutePath();
 						if (soundPath.equals(SoundboardMenu.mTopBlackBarSoundFilePath)) {
 							canvas.drawRect(0, 0, canvas.getWidth(), sound.getNameFrameY(), barPaint);
 						} else if (soundPath.equals(SoundboardMenu.mBottomBlackBarSoundFilePath)) {
 							canvas.drawRect(0, sound.getNameFrameY(), canvas.getWidth(), canvas.getHeight(), barPaint);
 						} else if (soundPath.equals(SoundboardMenu.mLeftBlackBarSoundFilePath)) {
 							canvas.drawRect(0, 0, sound.getNameFrameX(), canvas.getHeight(), barPaint);
 						} else if (soundPath.equals(SoundboardMenu.mRightBlackBarSoundFilePath)) {
 							canvas.drawRect(sound.getNameFrameX(), 0, canvas.getWidth(), canvas.getHeight(), barPaint);
 						} else {
 							if (sound.getHideImageOrText() != GraphicalSound.HIDE_TEXT) {
 								float NAME_LOCATION_SCALE = SoundNameDrawing.NAME_LOCATION_SCALE;
 								
 								canvas.scale(1/NAME_LOCATION_SCALE, 1/NAME_LOCATION_SCALE);
 								SoundNameDrawing soundNameDrawing = new SoundNameDrawing(sound);
 								
 								Paint nameTextPaint = soundNameDrawing.getBigCanvasNameTextPaint();
 								Paint borderPaint = soundNameDrawing.getBorderPaint();
 								Paint innerPaint = soundNameDrawing.getInnerPaint();
 								
 								RectF bigCanvasNameFrameRect = soundNameDrawing.getBigCanvasNameFrameRect();
 								
 								if (sound.getShowNameFrameInnerPaint() == true) {
 							    	canvas.drawRoundRect(bigCanvasNameFrameRect, 2*NAME_LOCATION_SCALE, 2*NAME_LOCATION_SCALE, innerPaint);
 							    }
 								
 								if (sound.getShowNameFrameBorderPaint()) {
 									canvas.drawRoundRect(bigCanvasNameFrameRect, 2*NAME_LOCATION_SCALE, 2*NAME_LOCATION_SCALE, borderPaint);
 								}
 							    
 								int i = 0;
 							    for (String row : sound.getName().split("\n")) {
 						    		canvas.drawText(row, (sound.getNameFrameX()+2)*NAME_LOCATION_SCALE, 
 						    				sound.getNameFrameY()*NAME_LOCATION_SCALE+(i+1)*sound.getNameSize()*NAME_LOCATION_SCALE, nameTextPaint);
 						    		i++;
 							    }
 							    canvas.scale(NAME_LOCATION_SCALE, NAME_LOCATION_SCALE);
 							}
 						    
 						    if (sound.getHideImageOrText() != GraphicalSound.HIDE_IMAGE) {
 							    RectF imageRect = new RectF();
 							    imageRect.set(sound.getImageX(), 
 										sound.getImageY(), 
 										sound.getImageWidth() + sound.getImageX(), 
 										sound.getImageHeight() + sound.getImageY());
 								
 							    try {
 							    	if (SoundPlayerControl.isPlaying(sound.getPath()) && sound.getActiveImage() != null) {
 							    		try {
 							    			canvas.drawBitmap(sound.getActiveImage(), null, imageRect, mSoundImagePaint);
 							    		} catch(NullPointerException npe) {
 							    			Log.e(TAG, "Unable to draw active image " + sound.getActiveImagePath().getAbsolutePath());
 											sound.setActiveImage(null);
 							    			canvas.drawBitmap(sound.getImage(), null, imageRect, mSoundImagePaint);
 							    		}
 							    		
 							    	} else {
 							    		canvas.drawBitmap(sound.getImage(), null, imageRect, mSoundImagePaint);
 							    	}
 								} catch(NullPointerException npe) {
 									Log.e(TAG, "Unable to draw image " + sound.getImagePath().getAbsolutePath());
 									sound.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.sound));
 								}
 						    }
 						    
 						    if (mGsb.getAutoArrange() && sound == mDragSound) {
 						    	int width = mPanel.getWidth();
 								int height = mPanel.getHeight();
 								
 								Paint linePaint = new Paint();
 								Paint outerLinePaint = new Paint(); {
 								linePaint.setColor(Color.WHITE);
 								outerLinePaint.setColor(Color.YELLOW);
 								outerLinePaint.setStrokeWidth(3);
 								}
 								
 						    	for (int i = 1; i < mGsb.getAutoArrangeColumns(); i++) {
 						    		float X = i*(width/mGsb.getAutoArrangeColumns());
 						    		canvas.drawLine(X, 0, X, height, outerLinePaint);
 						    		canvas.drawLine(X, 0, X, height, linePaint);
 						    	}
 						    	for (int i = 1; i < mGsb.getAutoArrangeRows(); i++) {
 						    		float Y = i*(height/mGsb.getAutoArrangeRows());
 						    		canvas.drawLine(0, Y, width, Y, outerLinePaint);
 						    		canvas.drawLine(0, Y, width, Y, linePaint);
 						    	}
 						    }
 						}
 					}
 				} catch(ConcurrentModificationException cme) {
 					Log.w(TAG, "Sound list modification while iteration");
 				}
 			}
 
 		}
 		
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 								   int height) {
 		}
 		
 		public void surfaceCreated(SurfaceHolder holder) {
 			try {
 				mThread.setRunning(true);
 				mThread.start();
 			} catch (NullPointerException e) {
 				mThread = new DrawingThread(getHolder(), this);
 				mThread.setRunning(true);
 				mThread.start();
 			}
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder holder) {
             mThread.setRunning(false);
             mThread = null;
 		}
 		
 	}
 	
 	class DrawingThread extends Thread {
         private SurfaceHolder mSurfaceHolder;
         private boolean mRun = false;
 		
         public DrawingThread(SurfaceHolder surfaceHolder, DrawingPanel panel) {
             mSurfaceHolder = surfaceHolder;
             mPanel = panel;
         }
 		
         public void setRunning(boolean run) {
             mRun = run;
         }
 		
         public SurfaceHolder getSurfaceHolder() {
             return mSurfaceHolder;
         }
 		
         @Override
         public void run() {
             while (mRun) {
             	Canvas c;
                 c = null;
                 try {
                     c = mSurfaceHolder.lockCanvas(null);
                     synchronized (mSurfaceHolder) {
                         mPanel.onDraw(c);
                     }
                 } finally {
                     if (c != null) {
                         mSurfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
                 try {
            		if (mMode == EDIT_BOARD && mDrawDragSound) {
             			Thread.sleep(10);
            		} else if (mMode == EDIT_BOARD && mDrawDragSound == false) {
             			
             			for (int i = 0; i <= 5; i++) {
             				Thread.sleep(100);
            				if (mDrawDragSound) {
             					break;
             				}
             			}
             		} else if (mMode == LISTEN_BOARD) {
             			for (int i = 0; i <= 30; i++) {
             				Thread.sleep(20);
             				if (mMode == EDIT_BOARD || mCanvasInvalidated == true) {
             					mCanvasInvalidated = false;
             					break;
             				}
             			}
             		}
 				} catch (InterruptedException e) {}
             }
         }
     }
 
 }
