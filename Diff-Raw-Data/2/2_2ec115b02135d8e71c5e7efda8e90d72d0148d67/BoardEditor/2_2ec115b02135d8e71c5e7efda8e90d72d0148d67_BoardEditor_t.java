 package fi.mikuz.boarder.gui;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.ListIterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Vibrator;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.util.TypedValue;
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
 import fi.mikuz.boarder.app.BoarderActivity;
 import fi.mikuz.boarder.component.Slot;
 import fi.mikuz.boarder.component.soundboard.BoardHistory;
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboardHolder;
 import fi.mikuz.boarder.util.AutoArrange;
 import fi.mikuz.boarder.util.FileProcessor;
 import fi.mikuz.boarder.util.IconUtils;
 import fi.mikuz.boarder.util.ImageDrawing;
 import fi.mikuz.boarder.util.OrientationUtil;
 import fi.mikuz.boarder.util.SoundPlayerControl;
 import fi.mikuz.boarder.util.XStreamUtil;
 import fi.mikuz.boarder.util.dbadapter.MenuDbAdapter;
 import fi.mikuz.boarder.util.editor.BoardHistoryProvider;
 import fi.mikuz.boarder.util.editor.GraphicalSoundboardProvider;
 import fi.mikuz.boarder.util.editor.GraphicalSoundboardProvider.OverridePage;
 import fi.mikuz.boarder.util.editor.Joystick;
 import fi.mikuz.boarder.util.editor.PageDrawer;
 import fi.mikuz.boarder.util.editor.PageDrawer.SwipingDirection;
 import fi.mikuz.boarder.util.editor.Pagination;
 import fi.mikuz.boarder.util.editor.PanelSize;
 import fi.mikuz.boarder.util.editor.SoundNameDrawing;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class BoardEditor extends BoarderActivity { //TODO destroy god object
 	private static final String TAG = BoardEditor.class.getSimpleName();
 	
 	private Vibrator vibrator;
 	
 	private int mCurrentOrientation;
 	
 	public GraphicalSoundboard mGsb;
 	private GraphicalSoundboardProvider mGsbp;
 	private BoardHistory mBoardHistory;
 	private BoardHistoryProvider mBoardHistoryProvider;
 	
 	private Pagination mPagination;
 	private PageDrawer mPageDrawer;
 	
 	private Joystick mJoystick = null;
 	private Timer mJoystickTimer = null;
 	
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
 	
 	/**
 	 * PRESS_BLANK: initial state before swipe gestures
 	 * PRESS_BOARD: initial state before tap, drag and swipe gestures
 	 * DRAG: sound is being dragged
 	 * SWIPE: swiping to other page
 	 */
 	private enum TouchGesture {PRESS_BLANK, PRESS_BOARD, DRAG, SWIPE, TAP};
 	private TouchGesture mCurrentGesture = null;
 	private final int DRAG_SWIPE_TIME = 300;
 	
 	private GraphicalSound mPressedSound = null;
 	private float mInitialNameFrameX = 0;
 	private float mInitialNameFrameY = 0;
 	private float mInitialImageX = 0;
 	private float mInitialImageY = 0;
 	private float mNameFrameDragDistanceX = -1;
 	private float mNameFrameDragDistanceY = -1;
 	private float mImageDragDistanceX = -1;
 	private float mImageDragDistanceY = -1;
 	private long mLastTrackballEvent = 0;
 	private DrawingThread mThread;
 	private Menu mMenu;
 	private DrawingPanel mPanel;
 	boolean mCanvasInvalidated = false;
 	
 	Thread mResolutionConverterThread;
 	AlertDialog mResolutionAlert;
 	
 	private boolean mMoveBackground = false;
 	private float mBackgroundLeftDistance = 0;
 	private float mBackgroundTopDistance = 0;
 	
 	private GraphicalSound mFineTuningSound = null;
 	
 	final Handler mHandler = new Handler();
 	ProgressDialog mWaitDialog;
 	boolean mClearBoardDir = false;
 	
 	private File mSbDir = SoundboardMenu.mSbDir;
 	private String mBoardName = null;
 	
 	private AlertDialog mSoundImageDialog;
 	private TextView mSoundImageWidthText;
 	private TextView mSoundImageHeightText;
 	private EditText mSoundImageWidthInput;
 	private EditText mSoundImageHeightInput;
 	
 	private AlertDialog mBackgroundDialog;
 	private TextView mBackgroundWidthText;
 	private TextView mBackgroundHeightText;
 	private EditText mBackgroundWidthInput;
 	private EditText mBackgroundHeightInput;
 	private float mWidthHeightScale;
 	
 	int mNullCanvasCount = 0;
 	
 	@Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
         super.mContext = (Context) this;
         ImageDrawing.registerCache(super.mContext);
         vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         
         mBoardHistoryProvider = new BoardHistoryProvider();
         
         Bundle extras = getIntent().getExtras();
         mBoardName = extras.getString(MenuDbAdapter.KEY_TITLE);
         setTitle(mBoardName);
 
         mGsbp = new GraphicalSoundboardProvider(mBoardName);
         mPagination = new Pagination(mGsbp);
         mPagination.restorePaginationInstance(savedInstanceState);
         initEditorBoard();
 
         if (mGsb.getSoundList().isEmpty()) {
         	mMode = EDIT_BOARD;
         }
 
         File icon = new File(mSbDir, mBoardName + "/icon.png");
         if (icon.exists()) {
 			Bitmap bitmap = ImageDrawing.decodeFile(BoardEditor.super.mContext, icon);
             Drawable drawable = new BitmapDrawable(getResources(), IconUtils.resizeIcon(this, bitmap, (40/6)));
         	this.getActionBar().setLogo(drawable);
         }
         
         mPanel = new DrawingPanel(this);
 	}
 	
 	public void initEditorBoard() {
 		
 		Configuration config = getResources().getConfiguration();
 		int orientation = OrientationUtil.getBoarderOrientation(config);
 		
 		if (mGsbp.getOrientationMode() == GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_PORTRAIT) {
 			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 			orientation = GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT;
 		} else if (mGsbp.getOrientationMode() == GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_LANDSCAPE) {
 			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			orientation = GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE;
 		}
 		
 		
 		this.mCurrentOrientation = orientation;
 		GraphicalSoundboard newGsb = mPagination.getBoard(BoardEditor.super.mContext, orientation);
 			
 		mPageDrawer = new PageDrawer(super.mContext);
 		changeBoard(newGsb, false);
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		menu.clear();
 		mMenu = menu;
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.board_editor_bottom, menu);
     	
     	if (mMode == EDIT_BOARD) {
 	    	menu.setGroupVisible(R.id.listen_mode, false);
 	    } else {
 	    	menu.setGroupVisible(R.id.edit_mode, false);
 	    }
 	    
 	    if (!mPagination.isMovePageMode()) {
 	    	menu.setGroupVisible(R.id.move_page_mode, false);
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
         		GraphicalSoundboard undoGsb = mBoardHistory.undo(BoardEditor.super.mContext);
         		if (undoGsb != null) {
         			loadBoard(undoGsb, SwipingDirection.NO_ANIMATION, OverridePage.OVERRIDE_NEW);
             		overrideBoard(undoGsb);
             		issueResolutionConversion(undoGsb.getScreenOrientation());
             		
         		}
         		mFineTuningSound = null;
         		removeJoystick();
         		return true;
         	
         	case R.id.menu_redo:
         		GraphicalSoundboard redoGsb = mBoardHistory.redo(BoardEditor.super.mContext);
         		if (redoGsb != null) {
         			loadBoard(redoGsb, SwipingDirection.NO_ANIMATION, OverridePage.OVERRIDE_NEW);
             		overrideBoard(redoGsb);
         		}
         		removeJoystick();
         		mFineTuningSound = null;
         		return true;
         		
         	case R.id.menu_add_sound:
         		Intent i = new Intent(this, FileExplorer.class);
         		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_ADD_GRAPHICAL_SOUND);
         		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
             	startActivityForResult(i, EXPLORE_SOUND);
             	return true;
             	
         	case R.id.menu_paste_sound:
         		GraphicalSound pasteSound = SoundboardMenu.mCopiedSound;
         		if (pasteSound == null) {
         			Toast.makeText(super.mContext, "Nothing copied", Toast.LENGTH_LONG).show();
         		} else {
         			if (mGsb.getAutoArrange()) {
         				placeToFreeSlot(pasteSound);
         			} else {
         				placeToFreeSpace(pasteSound);
         			}
         		}
             	return true;
             	
             case R.id.menu_save_board:
             	save();
                 return true;
 
             case R.id.menu_page_options:
 
             	CharSequence[] pageItems = {"Pagination settings", "Add page", "Delete this page", "Move this page"};
             	
             	Configuration config = getResources().getConfiguration();
             	final int currentOrientation = OrientationUtil.getBoarderOrientation(config);
 
             	AlertDialog.Builder pageBuilder = new AlertDialog.Builder(BoardEditor.this);
             	pageBuilder.setTitle("Page options");
             	pageBuilder.setItems(pageItems, new DialogInterface.OnClickListener() {
             	    public void onClick(DialogInterface dialog, int item) {
             	    	if (item == 0) {
             	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
             	    				getSystemService(LAYOUT_INFLATER_SERVICE);
             	    		View layout = inflater.inflate(R.layout.
             	    				graphical_soundboard_editor_alert_pagination_settings,
             	    				(ViewGroup) findViewById(R.id.alert_settings_root));
 
             	    		final CheckBox checkPaginationSynchronizedBetweenOrientations = 
             	    				(CheckBox) layout.findViewById(R.id.paginationSynchronizedBetweenOrientations);
             	    		checkPaginationSynchronizedBetweenOrientations.setChecked(mGsbp.isPaginationSynchronizedBetweenOrientations());
             	    		
             	    		AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
                       	  	builder.setView(layout);
                       	  	builder.setTitle("Pagination settings");
                   	  	
             	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	          		public void onClick(DialogInterface dialog, int whichButton) {
             	          			mGsbp.setPaginationSynchronizedBetweenOrientations(checkPaginationSynchronizedBetweenOrientations.isChecked());
             	          		}
             	          	});
 
             	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             		          	public void onClick(DialogInterface dialog, int whichButton) {
             	          	    }
             	          	});
             	          	
             	          	builder.show();
             	    	} else if (item == 1) {
             	    		GraphicalSoundboard swapGsb = mGsbp.addBoardPage(currentOrientation);
             	    		changeBoard(swapGsb, true);
             	    	} else if (item == 2) {
             	    		GraphicalSoundboard deleteGsb = mGsb;
             	    		mGsbp.deletePage(BoardEditor.super.mContext, deleteGsb);
             	    		GraphicalSoundboard gsb = mGsbp.getPage(BoardEditor.super.mContext, deleteGsb.getScreenOrientation(), deleteGsb.getPageNumber());
             	    		if (gsb == null) gsb = mPagination.getBoard(BoardEditor.super.mContext, deleteGsb.getScreenOrientation());
             	    		changeBoard(gsb, SwipingDirection.NO_DIRECTION, false, true);
             	    	} else if (item == 3) {
             	    		mPagination.initMove(mGsb);
             	    		BoardEditor.this.onCreateOptionsMenu(mMenu);
             	    	}
             	    }
             	});
             	AlertDialog pageAlert = pageBuilder.create();
             	pageAlert.show();
 
             	return true;
             
             case R.id.menu_move_page_here:
             	GraphicalSoundboard currentGsb = mGsb;
             	int toPageNumber = currentGsb.getPageNumber();
             	overrideBoard(currentGsb);
             	mPagination.movePage(BoardEditor.super.mContext, mGsb);
             	GraphicalSoundboard gsb = mGsbp.getPage(BoardEditor.super.mContext, currentGsb.getScreenOrientation(), toPageNumber);
            	changeBoard(gsb, SwipingDirection.NO_DIRECTION, false, true);
 	    		BoardEditor.this.onCreateOptionsMenu(mMenu);
             	return true;
                 
             case R.id.menu_convert_board:
             	
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
             	SoundPlayerControl.togglePlayPause(super.mContext);
             	return true;
             	
             case R.id.menu_notification:
             	SoundboardMenu.updateNotification(this, mBoardName, mBoardName);
             	return true;
             	
             case R.id.menu_take_screenshot:
             	
             	GraphicalSoundboard scPage = mGsb;
             	PanelSize panelSize = new PanelSize(BoardEditor.this);
             	Bitmap bitmap = Bitmap.createBitmap(panelSize.getWidth(), panelSize.getHeight(), Bitmap.Config.ARGB_8888);
             	Canvas canvas = new Canvas(bitmap);
             	mPanel.onDraw(canvas);
 	            FileProcessor.saveScreenshot(super.mContext, bitmap, 
 	            		mBoardName + "-" + scPage.getScreenOrientation() + "_" + 
 	            		(mPagination.getPageIndexForOrientation(scPage.getScreenOrientation()) + 1));
 				
             	return true;
             	
             case R.id.menu_board_settings:
             	
             	final CharSequence[] items = {"Sound", "Background", "Icon", "Screen orientation", "Auto-arrange", "Reset position"};
 
             	AlertDialog.Builder setAsBuilder = new AlertDialog.Builder(BoardEditor.this);
             	setAsBuilder.setTitle("Board settings");
             	setAsBuilder.setItems(items, new DialogInterface.OnClickListener() {
             	    public void onClick(DialogInterface dialog, int item) {
             	    	if (item == 0) {
             	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
             	    			getSystemService(LAYOUT_INFLATER_SERVICE);
                         	View layout = inflater.inflate(R.layout.graphical_soundboard_editor_alert_board_sound_settings,
                         	    (ViewGroup) findViewById(R.id.alert_settings_root));
                         	
                         	final CheckBox checkPlaySimultaneously = 
                       	  		(CheckBox) layout.findViewById(R.id.playSimultaneouslyCheckBox);
                       	  	checkPlaySimultaneously.setChecked(mGsb.getPlaySimultaneously());
                       	  	
                       	  	final EditText boardVolumeInput = (EditText) layout.findViewById(R.id.boardVolumeInput);
                   	  			boardVolumeInput.setText(mGsb.getBoardVolume()*100 + "%");
                   	  			
                   	  		AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
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
             	          				Toast.makeText(BoardEditor.super.mContext, "Incorrect value", 
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
             	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
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
                 					Intent i = new Intent(BoardEditor.this, ColorChanger.class);
                 			    	i.putExtra("parentKey", "changeBackgroundColor");
                 			    	i.putExtras(XStreamUtil.getSoundboardBundle(BoardEditor.super.mContext, mGsb));
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
                       	  	
                       	  	mBackgroundWidthText = (TextView) layout.findViewById(R.id.backgroundWidthText);
                       	  	mBackgroundHeightText = (TextView) layout.findViewById(R.id.backgroundHeightText);
                       	  	
                       	  	if (mGsb.getBackgroundImage() != null) {
 	                      	  	mBackgroundWidthText.setText("Width (" + mGsb.getBackgroundImage().getWidth() + ")");
 	            				mBackgroundHeightText.setText("Height (" + mGsb.getBackgroundImage().getHeight() + ")");
                       	  	}
                       	  	
                       	  	mBackgroundWidthInput = (EditText) layout.findViewById(R.id.backgroundWidthInput);
                       	  	mBackgroundWidthInput.setText(Float.toString(mGsb.getBackgroundWidth()));
                       	  	
                       	  	mBackgroundHeightInput = (EditText) layout.findViewById(R.id.backgroundHeightInput);
                     	  	mBackgroundHeightInput.setText(Float.toString(mGsb.getBackgroundHeight()));
 
                     	  	final CheckBox scaleWidthHeight = 
                     	  			(CheckBox) layout.findViewById(R.id.scaleWidthHeightCheckBox);
                     	  	scaleWidthHeight.setChecked(true);
                     	  	
                     	  	scaleWidthHeight.setOnClickListener(new OnClickListener() {
                     	  		public void onClick(View v) {
                     	  			try {
                     	  				// Calculate a new scale
 		              	  				mWidthHeightScale = Float.valueOf(mBackgroundWidthInput.getText().toString()).floatValue() 
 		              	  									/ Float.valueOf(mBackgroundHeightInput.getText().toString()).floatValue();
 		              	  			} catch(NumberFormatException nfe) {Log.e(TAG, "Unable to calculate width and height scale", nfe);}
                     	  		}
                     	  	});
                     	  	mWidthHeightScale = mGsb.getBackgroundWidth() / mGsb.getBackgroundHeight();
 
                       	  	mBackgroundWidthInput.setOnKeyListener(new OnKeyListener() {
             					public boolean onKey(View v, int keyCode, KeyEvent event) {
             						if (scaleWidthHeight.isChecked()) {
             							try {
             								float value = Float.valueOf(
             										mBackgroundWidthInput.getText().toString()).floatValue();
             								mBackgroundHeightInput.setText(
             										Float.valueOf(value/mWidthHeightScale).toString());
             							} catch(NumberFormatException nfe) {}
             						}
             						return false;
             					}
                       	  	});
                       	  	
                       	 	mBackgroundHeightInput.setOnKeyListener(new OnKeyListener() {
             					public boolean onKey(View v, int keyCode, KeyEvent event) {
             						if (scaleWidthHeight.isChecked()) {
             							try {
             								float value = Float.valueOf(
             										mBackgroundHeightInput.getText().toString()).floatValue();
             								mBackgroundWidthInput.setText(
             										Float.valueOf(value*mWidthHeightScale).toString());
             							} catch(NumberFormatException nfe) {}
             						}
             						return false;
             					}
                     	  	});
                       	 	
                       	 	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
                       	  	builder.setView(layout);
                       	  	builder.setTitle("Board settings");
                   	  	
             	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	          		public void onClick(DialogInterface dialog, int whichButton) {
             	          			
             	          			boolean notifyIncorrectValue = false;
             	          			mGsb.setUseBackgroundImage(checkUseBackgroundImage.isChecked());
             	          			
             	          			try {
             	          				mGsb.setBackgroundWidthHeight(BoardEditor.super.mContext, 
             	          						Float.valueOf(mBackgroundWidthInput.getText().toString()).floatValue(),
             	          						Float.valueOf(mBackgroundHeightInput.getText().toString()).floatValue());
             	          			} catch(NumberFormatException nfe) {
             	          				notifyIncorrectValue = true;
             	          			}
             	          			
             	          			if (notifyIncorrectValue == true) {
             	          				Toast.makeText(BoardEditor.super.mContext, "Incorrect value", 
             	          						Toast.LENGTH_SHORT).show();
             	          			}
             	          			mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
             	          		}
             	          	});
 
             	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             		          	public void onClick(DialogInterface dialog, int whichButton) {
             	          	    }
             	          	});
             	          	
             	          	builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
 								public void onCancel(DialogInterface dialog) {
 									mBackgroundDialog = null;
 								}
 							});
             	          	
             	          	mBackgroundDialog = builder.create();
             	          	mBackgroundDialog.show();
             	    	} else if (item == 2) {
             	    		AlertDialog.Builder resetBuilder = new AlertDialog.Builder(
 		                			BoardEditor.this);
 		                	resetBuilder.setTitle("Change board icon");
 		                	resetBuilder.setMessage("You can change icon for this board.\n\n" +
 		                			"You need a png image:\n " + mSbDir + "/" + mBoardName + "/" + "icon.png\n\n" +
 		                			"Recommended size is about 80x80 pixels.");
 		                	AlertDialog resetAlert = resetBuilder.create();
 		                	resetAlert.show();
             	    	} else if (item == 3) {
             	    		final CharSequence[] items = {"Portrait", "Landscape", "Hybrid (beta)"};
 
 		                	AlertDialog.Builder orientationBuilder = new AlertDialog.Builder(BoardEditor.this);
 		                	orientationBuilder.setTitle("Select orientation");
 		                	orientationBuilder.setItems(items, new DialogInterface.OnClickListener() {
 		                		public void onClick(DialogInterface dialog, int item) {
 		                			if (item == 0 && mGsbp.getOrientationMode() != GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_PORTRAIT) {
 		                				if (mGsbp.getOrientationMode() == GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID) {
 		                					useOrientationAndAskToRemoveUnusedAlert(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT);
 		                				} else {
 		                					if (mGsbp.boardWithOrientationExists(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT)) {
 		                						orientationTurningConflictActionAlert(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT);
 		                					} else {
 		                						orientationTurningAlert(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT);
 		                					}
 		                				}
 		                			} else if (item == 1 && mGsbp.getOrientationMode() != GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_LANDSCAPE) {
 		                				if (mGsbp.getOrientationMode() == GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID) {
 		                					useOrientationAndAskToRemoveUnusedAlert(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE);
 		                				} else {
 		                					if (mGsbp.boardWithOrientationExists(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE)) {
 		                						orientationTurningConflictActionAlert(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE);
 		                					} else {
 		                						orientationTurningAlert(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE);
 		                					}
 		                				}
 		                			} else if (item == 2 && mGsbp.getOrientationMode() != GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID) {
 		                				hybridAlert();
 		                			}
 		                		}
 		                	});
 		                	
 		                	AlertDialog orientationAlert = orientationBuilder.create();
 		                	orientationAlert.show();
             	    	} else if (item == 4) {
             	    	//Auto-arrange
             	    	LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
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
                   	 	
                   	 	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
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
         	          					Toast.makeText(BoardEditor.super.mContext, "Not enought slots", 
             	          						Toast.LENGTH_SHORT).show();
         	          				}
         	          				mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
         	          			} catch(NumberFormatException nfe) {
         	          				Toast.makeText(BoardEditor.super.mContext, "Incorrect value", 
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
             	    		
             	    		final int extraItemCount = 1;
             	    		itemArray.add("> Background image");
             	    		
 		    	    		for (GraphicalSound sound : mGsb.getSoundList()) {
 		    	    			itemArray.add(sound.getName());
 		    	    		}
 		    	    		CharSequence[] items = itemArray.toArray(new CharSequence[itemArray.size()]);
 		    	    		
 		    	    		AlertDialog.Builder resetBuilder = new AlertDialog.Builder(
 		                			BoardEditor.this);
 		                	resetBuilder.setTitle("Reset position");
 		                	resetBuilder.setItems(items, new DialogInterface.OnClickListener() {
 		                	    public void onClick(DialogInterface dialog, int item) {
 		                	    	if (item == 0) { // Background
 		                	    		mGsb.setBackgroundX(0);
 		                	    		mGsb.setBackgroundY(0);
 		                	    		mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 		                	    	} else { // Sound
 		                	    		GraphicalSound sound = mGsb.getSoundList().get(item - extraItemCount);
 			                	    	sound.setNameFrameX(50);
 			        	    			sound.setNameFrameY(50);
 			        	    			sound.generateImageXYFromNameFrameLocation();
 			        	    			mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 		                	    	}
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
 	
 	public void changeBoard(GraphicalSoundboard gsb, boolean overrideCurrentBoard) {
 		changeBoard(gsb, SwipingDirection.NO_DIRECTION, overrideCurrentBoard, false);
 	}
 
 	public void changeBoard(GraphicalSoundboard gsb, SwipingDirection direction, boolean overrideCurrentBoard, boolean forceChange) {
 		GraphicalSoundboard lastGsb = mGsb;
 		boolean samePage = lastGsb != null && 
 				(gsb.getPageNumber() == lastGsb.getPageNumber() && gsb.getScreenOrientation() == lastGsb.getScreenOrientation()); 
 		
 		if (!samePage || forceChange) {
 			refreshPageTitle(gsb.getPageNumber());
 			
 			// Put drawing thread to animation speed
 			mPageDrawer.startInitializingAnimation();
 			mCanvasInvalidated = true;
 			if (mThread != null) mThread.interrupt();
 			
 			OverridePage override = (overrideCurrentBoard) ? OverridePage.OVERRIDE_CURRENT : OverridePage.NO_OVERRIDE;
 			loadBoard(gsb, direction, override);
 			
 			int boardId = gsb.getId();
 			BoardHistory boardHistory = mBoardHistoryProvider.getBoardHistory(boardId);
 			
 			if (boardHistory == null) {
 				boardHistory = mBoardHistoryProvider.createBoardHistory(BoardEditor.super.mContext, boardId, gsb);
 			}
 			
 			this.mBoardHistory = boardHistory;
 			
 			issueResolutionConversion(gsb.getScreenOrientation());
 		} else {
 			Log.v(TAG, "Won't change page to same page.");
 		}
 	}
 	
 	public void refreshPageTitle(int pageNumber) {
 		setTitle(mBoardName + " - " + (pageNumber+1));
 	}
 	
 	public void loadBoard(GraphicalSoundboard gsb, SwipingDirection direction, OverridePage override) {
 		GraphicalSoundboard.loadImages(super.mContext, gsb);
 		
 		if (override == OverridePage.OVERRIDE_CURRENT) {
 			overrideBoard(mGsb);
 		} else if (override == OverridePage.OVERRIDE_NEW) {
 			overrideBoard(gsb);
 		}
 		
 		mGsb = gsb;
 		mPageDrawer.switchPage(gsb, direction);
 	}
 	
 	private void orientationTurningConflictActionAlert(final int screenOrientation) {
 		String orientationName = GraphicalSoundboard.getOrientationName(screenOrientation);
 		String oppositeOrientationName = GraphicalSoundboard.getOppositeOrientationName(screenOrientation);
 		
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(BoardEditor.this);
 		orientationWarningBuilder.setTitle("Conflicting board");
 			orientationWarningBuilder.setMessage(
 		  			"A board for " + orientationName + " orientation already exists. You can either use it or remove it.\n\n" +
 		  			"By removing it you can turn " + oppositeOrientationName + " board to " + orientationName + " orientation.\n\n");
 	  	orientationWarningBuilder.setPositiveButton("Remove board", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.deleteBoardWithOrientation(screenOrientation);
 				orientationTurningAlert(screenOrientation);
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("Use board", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.setOrientationMode(screenOrientation);
 				useOrientationAndAskToRemoveUnusedAlert(screenOrientation);
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void orientationTurningAlert(final int screenOrientation) {
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(
 			BoardEditor.this);
 		orientationWarningBuilder.setTitle("Changing orientation");
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
 	    		mGsbp.setOrientationMode(screenOrientation);
 	    		mGsb.setScreenOrientation(screenOrientation);
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
 				mGsbp.setOrientationMode(screenOrientation);
 				mGsb.setScreenOrientation(screenOrientation);
 				finishBoard();
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void hybridAlert() {
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(
 			BoardEditor.this);
 		orientationWarningBuilder.setTitle("Hybrid mode");
 			orientationWarningBuilder.setMessage(
 		  			"Hybrid mode allows you to switch between portrait and landscape by turning the screen.\n\n" +
 		  			"However both orientations must be created and maintained separately.\n\n" +
 		  			"Proceed?");
 	  	orientationWarningBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.setOrientationMode(GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID);
 	          	finishBoard();
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void useOrientationAndAskToRemoveUnusedAlert(final int screenOrientation) {
 		final int oppositeOrientation = GraphicalSoundboard.getOppositeOrientation(screenOrientation);
 		
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(
 			BoardEditor.this);
 		orientationWarningBuilder.setTitle("Unused board");
 			orientationWarningBuilder.setMessage(
 		  			"Do you want to delete unused board in " +  GraphicalSoundboard.getOrientationName(oppositeOrientation) + " orientation?\n\n");
 	  	orientationWarningBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				//mThread.setRunning(false); // TODO handle board deleting better
 				mGsb = mPagination.getBoard(BoardEditor.super.mContext, screenOrientation);
 				mGsbp.deleteBoardWithOrientation(oppositeOrientation);
 				mGsbp.setOrientationMode(screenOrientation);
 	    		finishBoard();
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.setOrientationMode(screenOrientation);
 	    		finishBoard();
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void finishBoard() {
 		try {
 			BoardEditor.this.finish();
 		} catch (Throwable e) {
 			Log.e(TAG, "Error closing board", e);
 		}
 	}
 	
 	private void selectBackgroundFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_SELECT_BACKGROUND_FILE);
 		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
     	startActivityForResult(i, EXPLORE_BACKGROUD);
 	}
 	
 	private void selectImageFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_SELECT_SOUND_IMAGE_FILE);
 		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
     	startActivityForResult(i, EXPLORE_SOUND_IMAGE);
 	}
 	
 	private void selectActiveImageFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_SELECT_SOUND_ACTIVE_IMAGE_FILE);
 		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
     	startActivityForResult(i, EXPLORE_SOUND_ACTIVE_IMAGE);
 	}
 	
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 
 		switch(requestCode) {
 		case EXPLORE_SOUND:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				XStream xstream = XStreamUtil.graphicalBoardXStream();
 
 				GraphicalSound sound = (GraphicalSound) xstream.fromXML(extras.getString(FileExplorer.ACTION_ADD_GRAPHICAL_SOUND));
 				sound.setDefaultImage(BoardEditor.super.mContext);
 				sound.setAutoArrangeColumn(0);
 				sound.setAutoArrangeRow(0);
 				if (mGsb.getAutoArrange()) {
 					placeToFreeSlot(sound);
 				} else {
 					placeToFreeSpace(sound);
 				}
 			}
 			break;
 
 		case EXPLORE_BACKGROUD:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				File background = new File(extras.getString(FileExplorer.ACTION_SELECT_BACKGROUND_FILE));
 				mGsb.setBackgroundImagePath(background);
 				mGsb.setBackgroundWidthHeight(BoardEditor.super.mContext,
 						mGsb.getBackgroundImage().getWidth(),
 						mGsb.getBackgroundImage().getHeight());
 				mGsb.loadBackgroundImage(BoardEditor.super.mContext);
 				mGsb.setBackgroundX(0);
 				mGsb.setBackgroundY(0);
 				mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 			}
 			if (mBackgroundDialog != null && mGsb.getBackgroundImage() != null) {
 				mBackgroundWidthText.setText("Width (" + mGsb.getBackgroundImage().getWidth() + ")");
 				mBackgroundHeightText.setText("Height (" + mGsb.getBackgroundImage().getHeight() + ")");
 				mBackgroundWidthInput.setText(Float.toString(mGsb.getBackgroundWidth()));
 				mBackgroundHeightInput.setText(Float.toString(mGsb.getBackgroundHeight()));
 			}
 			break;
 
 		case EXPLORE_SOUND_IMAGE:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				File image = new File(extras.getString(FileExplorer.ACTION_SELECT_SOUND_IMAGE_FILE));
 				mPressedSound.setImagePath(image);
 				mPressedSound.loadImages(BoardEditor.super.mContext);
 			}
 			if (mSoundImageDialog != null) {
 				mSoundImageWidthText.setText("Width (" + mPressedSound.getImage().getWidth() + ")");
 				mSoundImageHeightText.setText("Height (" + mPressedSound.getImage().getHeight() + ")");
 				mSoundImageWidthInput.setText(Float.toString(mPressedSound.getImage().getWidth()));
 				mSoundImageHeightInput.setText(Float.toString(mPressedSound.getImage().getHeight()));
 			}
 			break;
 
 		case EXPLORE_SOUND_ACTIVE_IMAGE:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				File image = new File(extras.getString(FileExplorer.ACTION_SELECT_SOUND_ACTIVE_IMAGE_FILE));
 				mPressedSound.setActiveImagePath(image);
 				mPressedSound.loadImages(BoardEditor.super.mContext);
 			}
 			break;
 
 		case CHANGE_NAME_COLOR:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				if (extras.getBoolean("copyKey")) {
 					mCopyColor = CHANGE_NAME_COLOR;
 				} else {
 					mPressedSound.setNameTextColorInt(extras.getInt("colorKey"));
 				}
 			}
 			break;
 
 		case CHANGE_INNER_PAINT_COLOR:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				if (extras.getBoolean("copyKey")) {
 					mCopyColor = CHANGE_INNER_PAINT_COLOR;
 				} else {
 					mPressedSound.setNameFrameInnerColorInt(extras.getInt("colorKey"));
 				}
 			}
 			break;
 
 		case CHANGE_BORDER_PAINT_COLOR:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				if (extras.getBoolean("copyKey")) {
 					mCopyColor = CHANGE_BORDER_PAINT_COLOR;
 				} else {
 					mPressedSound.setNameFrameBorderColorInt(extras.getInt("colorKey"));
 				}
 			}
 			break;
 
 		case CHANGE_BACKGROUND_COLOR:
 
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				mGsb.setBackgroundColor(extras.getInt("colorKey"));
 				mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 			}
 			break;
 
 		case CHANGE_SOUND_PATH:
 			if (resultCode == RESULT_OK) {
 
 				LayoutInflater removeInflater = (LayoutInflater) 
 						BoardEditor.this.getSystemService(LAYOUT_INFLATER_SERVICE);
 				View removeLayout = removeInflater.inflate(
 						R.layout.graphical_soundboard_editor_alert_remove_sound,
 						(ViewGroup) findViewById(R.id.alert_remove_sound_root));
 
 				final CheckBox removeFileCheckBox = 
 						(CheckBox) removeLayout.findViewById(R.id.removeFile);
 				removeFileCheckBox.setText(" DELETE " + mPressedSound.getPath().getAbsolutePath());
 
 				AlertDialog.Builder removeBuilder = new AlertDialog.Builder(
 						BoardEditor.this);
 				removeBuilder.setView(removeLayout);
 				removeBuilder.setTitle("Changing sound");
 
 				removeBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						if (removeFileCheckBox.isChecked() == true) {
 							mPressedSound.getPath().delete();
 						}
 						Bundle extras = intent.getExtras();
 						mPressedSound.setPath(new File(extras.getString(FileExplorer.ACTION_CHANGE_SOUND_PATH)));
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
 		mWaitDialog = ProgressDialog.show(BoardEditor.this, "", "Please wait", true);
 		
 		Thread t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				try {
 					if (mClearBoardDir) {
 						cleanDirectory(new File(mSbDir, mBoardName).listFiles());
 					}
 					
 					FileProcessor.convertGraphicalBoard(BoardEditor.this, mBoardName, mGsb);
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
     public void onConfigurationChanged(Configuration newConfig) {
     	int newOrientation = OrientationUtil.getBoarderOrientation(newConfig);
 
     	if (newOrientation != this.mCurrentOrientation) {
     		this.mCurrentOrientation = newOrientation;
     		GraphicalSoundboard newOrientationGsb = mPagination.getBoard(BoardEditor.super.mContext, newOrientation);
     		
     		changeBoard(newOrientationGsb, true);
     		
     		removeJoystick();
     		this.mFineTuningSound = null;
     		this.mPressedSound = null;
     	}
     	
     	super.onConfigurationChanged(newConfig);
     }
     
     @Override
     protected void onRestart() {
     	ImageDrawing.registerCache(super.mContext);
     	super.onRestart();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	setContentView(mPanel);
     }
 	
 	@Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     	mPagination.savePaginationInstance(outState);
     }
     
     @Override
     protected void onPause() {
     	save();
     	super.onPause();
     }
     
     @Override
     protected void onDestroy() {
     	GraphicalSoundboard.unloadImages(mGsb);
     	super.onDestroy();
     }
     
     private void save() {
     	if (mBoardName != null) {
     		try {
     			overrideBoard(mGsb);
     			mGsbp.saveBoard(BoardEditor.super.mContext, mBoardName);
         		Log.v(TAG, "Board " + mBoardName + " saved");
     		} catch (IOException e) {
     			Log.e(TAG, "Unable to save " + mBoardName, e);
     		}
     	}
     }
     
     private void overrideBoard(GraphicalSoundboard gsb) {
     	GraphicalSoundboard overrideGsb = GraphicalSoundboard.copy(BoardEditor.super.mContext, gsb);
 		if (mPressedSound != null && mCurrentGesture == TouchGesture.DRAG) overrideGsb.getSoundList().add(mPressedSound); // Sound is being dragged
 		mGsbp.overrideBoard(BoardEditor.super.mContext, overrideGsb);
     }
     
     private void playTouchedSound(GraphicalSound sound) {
     	vibrator.vibrate(10);
     	if (sound.getPath().getAbsolutePath().equals(SoundboardMenu.mPauseSoundFilePath)) { 
 			SoundPlayerControl.togglePlayPause(super.mContext);
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
     }
     
     private void initializeDrag(float initTouchEventX, float initTouchEventY, GraphicalSound sound) {
     	vibrator.vibrate(20);
     	mPressedSound = sound;
     	mCurrentGesture = TouchGesture.DRAG;
     	mInitialNameFrameX = sound.getNameFrameX();
     	mInitialNameFrameY = sound.getNameFrameY();
     	mInitialImageX = sound.getImageX();
     	mInitialImageY = sound.getImageY();
     	mGsb.getSoundList().remove(sound);
 
     	mNameFrameDragDistanceX = initTouchEventX - sound.getNameFrameX();
     	mNameFrameDragDistanceY = initTouchEventY - sound.getNameFrameY();
     	mImageDragDistanceX = initTouchEventX - sound.getImageX();
     	mImageDragDistanceY = initTouchEventY - sound.getImageY();
     }
     
     private void copyColor(GraphicalSound sound) {
 		switch(mCopyColor) {
 			case CHANGE_NAME_COLOR:
 				mPressedSound.setNameTextColorInt(sound.getNameTextColor());
 				break;
 			case CHANGE_INNER_PAINT_COLOR:
 				mPressedSound.setNameFrameInnerColorInt(sound.getNameFrameInnerColor());
 				break;
 			case CHANGE_BORDER_PAINT_COLOR:
 				mPressedSound.setNameFrameBorderColorInt(sound.getNameFrameBorderColor());
 				break;
 		}
 		mCopyColor = 0;
 		mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 	}
     
     void moveSound(float X, float Y) {
     	if (mPressedSound != null) {
     		if (mPressedSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
     			mPressedSound.setNameFrameX(X-mNameFrameDragDistanceX);
     			mPressedSound.setNameFrameY(Y-mNameFrameDragDistanceY);
     		}
     		if (mPressedSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
     			mPressedSound.setImageX(X-mImageDragDistanceX);
     			mPressedSound.setImageY(Y-mImageDragDistanceY);
     		}
     	} else {
     		Log.w(TAG, "Tried to move null sound"); // Orientation changed while dragging?
     	}
 	}
 	
 	public void moveSoundToSlot(GraphicalSound sound, int column, int row, float imageX, float imageY, float nameX, float nameY) {
 		
 		PanelSize panelSize = new PanelSize(BoardEditor.this);
 		int width = panelSize.getWidth();
 		int height = panelSize.getHeight();
 		
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
 		mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 	}
 	
 	public void placeToFreeSlot(final GraphicalSound placedSound) {
 		Thread t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				
 				GraphicalSound sound = placedSound;
 				try {
 					Slot slot = AutoArrange.getFreeSlot(mGsb.getSoundList(), mGsb.getAutoArrangeColumns(), mGsb.getAutoArrangeRows());
 					moveSoundToSlot(sound, slot.getColumn(), slot.getRow(), sound.getImageX(), sound.getImageY(), sound.getNameFrameX(), sound.getNameFrameY());
 
 					mGsb.getSoundList().add(sound);
 					mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 				} catch (NullPointerException e) {
 					Toast.makeText(BoardEditor.super.mContext, "No slot available", Toast.LENGTH_SHORT).show();
 				}
 			}
 		};
 		t.start();
 	}
 	
 	public void placeToFreeSpace(final GraphicalSound placedSound) {
 		Thread t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				
 				GraphicalSound sound = placedSound;
 				boolean spaceAvailable = true;
 
 				float freeSpaceX = 0;
 				float freeSpaceY = 0;
 
 				PanelSize panelSize = new PanelSize(BoardEditor.this);
 				int width = panelSize.getWidth();
 				int height = panelSize.getHeight();
 
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
 
 				mGsb.getSoundList().add(sound);
 				mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 			}
 		};
 		t.start();
 	}
 	
 	public boolean onTrackballEvent (MotionEvent event) {
 		if (mMode == EDIT_BOARD && event.getAction() == MotionEvent.ACTION_MOVE && mPressedSound != null && 
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
 			
 			if (mPressedSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
 				mPressedSound.setNameFrameX(mPressedSound.getNameFrameX() + movementX);
 				mPressedSound.setNameFrameY(mPressedSound.getNameFrameY() + movementY);
 			}
 			if (mPressedSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
 				mPressedSound.setImageX(mPressedSound.getImageX() + movementX);
 				mPressedSound.setImageY(mPressedSound.getImageY() + movementY);
 			}
 			mBoardHistory.setHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	private void removeJoystick() {
 		if (mJoystickTimer != null) mJoystickTimer.cancel();
 		mJoystickTimer = null;
 		mJoystick = null;
 		mPageDrawer.giveJoystick(null);
 	}
 	
 	
 	
 	public void issueResolutionConversion(final int orientation) {
 		if (mResolutionAlert != null) {
 			mResolutionAlert.dismiss();
 		}
 		
 		Thread t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				
 				final float allowedResolutionDifference = 0.03f;
 				
 				PanelSize panelSize = new PanelSize(BoardEditor.this);
 				int panelWindowWidth = panelSize.getWidth();
 				int panelWindowHeight = panelSize.getHeight();
 				int i = 0, orientationMaxTries = 200;
 				
 				i = 0;
 				while (i < orientationMaxTries) {
 					if ((orientation == GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT && panelWindowHeight < panelWindowWidth) ||
 							(orientation == GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE && panelWindowWidth < panelWindowHeight)) {
 						try {
 							Thread.sleep(20);
 							panelSize = new PanelSize(BoardEditor.this);
 							panelWindowWidth = panelSize.getWidth();
 							panelWindowHeight = panelSize.getHeight();
 						} catch (InterruptedException e) {
 							Log.e(TAG, "Unable to sleep while waiting for orientation to change", e);
 						}
 					} else {
 						break;
 					}
 					if (i == orientationMaxTries - 1) {
 						Log.e(TAG, "Resolution conversion cancelled because of orientation change timeout");
 						return;
 					} else if (orientation != mCurrentOrientation) {
 						Log.v(TAG, "Resolution conversion cancelled since orientation is no longer valid");
 						return;
 					}
 					i++;
 				}
 				
 				final int windowWidth = panelWindowWidth;
 				final int windowHeight = panelWindowHeight;
 
 				if (mGsb.getScreenHeight() == 0 || mGsb.getScreenWidth() == 0) {
 					mGsb.setScreenWidth(windowWidth);
 					mGsb.setScreenHeight(windowHeight);
 				} else if ((Math.abs(mGsb.getScreenHeight() - windowHeight) > mGsb.getScreenHeight()*allowedResolutionDifference) || 
 						(Math.abs(mGsb.getScreenWidth() - windowWidth) > mGsb.getScreenWidth()*allowedResolutionDifference)) {
 					// Small resolution changes are not noticed since those can constantly occur on same device
 					
 					Log.v(TAG, "Soundoard resolution has changed. X: " + mGsb.getScreenWidth() + " -> " + windowWidth + " - Y: " + mGsb.getScreenHeight() + " -> " + windowHeight);
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 					builder.setTitle("Display size");
 					builder.setMessage("Display size used to make this board differs from your display size.\n\n" +
 							"You can resize this board to fill your display or " +
 							"fit this board to your display. Fit looks accurately like the original one.\n\n" +
 							"(Ps. In 'Edit board' mode you can undo this.)");
 
 					builder.setPositiveButton("Resize", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							Log.v(TAG, "Resizing board");
 							
 							clearBlackBars();
 							float xScale = (float) windowWidth/(float) (mGsb.getScreenWidth());
 							float yScale = (float) windowHeight/(float) (mGsb.getScreenHeight());
 
 							float avarageScale = xScale+(yScale-xScale)/2;
 							Log.v(TAG, "X scale: \"" + xScale + "\"" + ", old width: \""+mGsb.getScreenWidth() + "\", new width: \"" + windowWidth + "\"");
 							Log.v(TAG, "Y scale: \"" + yScale + "\"" + ", old height: \""+mGsb.getScreenHeight() + "\", new height: \"" + windowHeight + "\"");
 							Log.v(TAG, "Avarage scale: \"" + avarageScale + "\"");
 
 							mGsb.setBackgroundX(mGsb.getBackgroundX()*xScale);
 							mGsb.setBackgroundY(mGsb.getBackgroundY()*yScale);
 							mGsb.setBackgroundWidthHeight(BoardEditor.super.mContext, 
 									mGsb.getBackgroundWidth()*xScale, 
 									mGsb.getBackgroundHeight()*yScale);
 
 							for (GraphicalSound sound : mGsb.getSoundList()) {
 
 								sound = SoundNameDrawing.getScaledTextSize(sound, avarageScale);
 
 								sound.setNameFrameX(sound.getNameFrameX()*xScale);
 								sound.setNameFrameY(sound.getNameFrameY()*yScale);
 
 								sound.setImageX(sound.getImageX()*xScale);
 								sound.setImageY(sound.getImageY()*yScale);
 								sound.setImageWidthHeight(BoardEditor.super.mContext, sound.getImageWidth()*avarageScale, sound.getImageHeight()*avarageScale);
 
 								if (sound.getLinkNameAndImage()) sound.generateNameFrameXYFromImageLocation();
 							}
 
 							mGsb.setScreenWidth(windowWidth);
 							mGsb.setScreenHeight(windowHeight);
 						}
 					});
 
 					builder.setNeutralButton("Fit", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							Log.v(TAG, "Fitting board");
 							
 							clearBlackBars();
 							float xScale = (float) (windowWidth)/(float) (mGsb.getScreenWidth());
 							float yScale = (float) (windowHeight)/(float) (mGsb.getScreenHeight());
 
 							boolean xFillsDisplay = xScale < yScale;
 							float applicableScale = (xScale < yScale) ? xScale : yScale;
 
 							float hiddenAreaSize;
 
 							if (xFillsDisplay) {
 								hiddenAreaSize = ((float) windowHeight-(float) mGsb.getScreenHeight()*applicableScale)/2;
 							} else {
 								hiddenAreaSize = ((float) windowWidth-(float) mGsb.getScreenWidth()*applicableScale)/2;
 							}
 
 							Log.v(TAG, "X scale: \"" + xScale + "\"" + ", old width: \""+mGsb.getScreenWidth() + "\", new width: \"" + windowWidth + "\"");
 							Log.v(TAG, "Y scale: \"" + yScale + "\"" + ", old height: \""+mGsb.getScreenHeight() + "\", new height: \"" + windowHeight + "\"");
 							Log.v(TAG, "Applicable scale: \"" + applicableScale + "\"");
 							Log.v(TAG, "Hidden area size: \"" + hiddenAreaSize + "\"");
 
 							mGsb.setBackgroundWidthHeight(BoardEditor.super.mContext,
 									mGsb.getBackgroundWidth()*applicableScale,
 									mGsb.getBackgroundHeight()*applicableScale);
 
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
 
 								sound.setImageWidthHeight(BoardEditor.super.mContext, sound.getImageWidth()*applicableScale, sound.getImageHeight()*applicableScale);
 
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
 								blackBar2.setNameFrameY((float) windowHeight-hiddenAreaSize);
 							} else {
 								blackBar1.setName("I hide left side of the board.");
 								blackBar2.setName("I hide right side of the board.");
 								blackBar1.setPath(new File(SoundboardMenu.mLeftBlackBarSoundFilePath));
 								blackBar2.setPath(new File(SoundboardMenu.mRightBlackBarSoundFilePath));
 								blackBar1.setNameFrameX(hiddenAreaSize);
 								blackBar2.setNameFrameX((float) windowWidth-hiddenAreaSize);
 							}
 
 							mGsb.addSound(blackBar1);
 							mGsb.addSound(blackBar2);
 
 							mGsb.setScreenWidth(windowWidth);
 							mGsb.setScreenHeight(windowHeight);
 						}
 					});
 
 					builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							mGsb.setScreenWidth(windowWidth);
 							mGsb.setScreenHeight(windowHeight);
 						}
 					});
 					
 					mResolutionAlert = builder.create();
 					mResolutionAlert.setOnDismissListener(new OnDismissListener() {
 						public void onDismiss(DialogInterface dialog) {
 							mResolutionAlert = null;
 							mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 						}
 					});
 
 					mResolutionAlert.show();
 				}
 				Looper.loop();
 				Looper.myLooper().quit();
 			}
 		};
 		t.start();
 	}
 	
 	private void clearBlackBars() {
 		ListIterator<GraphicalSound> iterator = mGsb.getSoundList().listIterator();
 		while (iterator.hasNext()) {
 			GraphicalSound sound = iterator.next();
 			if (sound.getPath().getAbsolutePath().equals(SoundboardMenu.mTopBlackBarSoundFilePath) ||
 					sound.getPath().getAbsolutePath().equals(SoundboardMenu.mBottomBlackBarSoundFilePath)) {
 				mGsb.setScreenHeight(mGsb.getScreenHeight() - Float.valueOf(sound.getImageHeight()).intValue());
 				iterator.remove();
 			} else if (sound.getPath().getAbsolutePath().equals(SoundboardMenu.mLeftBlackBarSoundFilePath) ||
 					sound.getPath().getAbsolutePath().equals(SoundboardMenu.mRightBlackBarSoundFilePath)) {
 				mGsb.setScreenWidth(mGsb.getScreenWidth() - Float.valueOf(sound.getImageWidth()).intValue());
 				iterator.remove();
 			}
 		}
 	}
 	
 	public DrawingPanel getPanel() {
 		return this.mPanel;
 	}
 	
 	public class DrawingPanel extends SurfaceView implements SurfaceHolder.Callback {
 		
 		private float mInitTouchEventX = 0;
 		private float mInitTouchEventY = 0;
 		private long mClickTime = 0;
 		
 		private float mLatestEventX = 0;
 		private float mLatestEventY = 0;
 		
 		Object mGestureLock = new Object();
 		
 		public DrawingPanel(Context context) {
 			super(context);
             getHolder().addCallback(this);
             mThread = new DrawingThread(getHolder(), this);
 		}
 		
 		private GraphicalSound findPressedSound(MotionEvent pressInitEvent) {
 			GraphicalSound pressedSound = null;
 			
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
 				if (pressInitEvent.getX() >= sound.getImageX() && 
 						pressInitEvent.getX() <= sound.getImageWidth() + sound.getImageX() &&
 						pressInitEvent.getY() >= sound.getImageY() &&
 						pressInitEvent.getY() <= sound.getImageHeight() + sound.getImageY())  {
 					
 					mDragTarget = DRAG_IMAGE;
 					return sound;
 					
 				} else if ((pressInitEvent.getX() >= sound.getNameFrameX() && 
 						pressInitEvent.getX() <= nameFrameWidth + sound.getNameFrameX() &&
 						pressInitEvent.getY() >= sound.getNameFrameY() && 
 						pressInitEvent.getY() <= nameFrameHeight + sound.getNameFrameY()) ||
 						
 						soundPath.equals(SoundboardMenu.mTopBlackBarSoundFilePath) && pressInitEvent.getY() <= nameFrameY ||
 						soundPath.equals(SoundboardMenu.mBottomBlackBarSoundFilePath) && pressInitEvent.getY() >= nameFrameY ||
 						soundPath.equals(SoundboardMenu.mLeftBlackBarSoundFilePath) && pressInitEvent.getX() <= nameFrameX ||
 						soundPath.equals(SoundboardMenu.mRightBlackBarSoundFilePath) && pressInitEvent.getX() >= nameFrameX) {
 					
 					mDragTarget = DRAG_TEXT;
 					return sound;
 				}
 			}
 			
 			return pressedSound;
 		}
 		
 		private long holdTime() {
 			return Calendar.getInstance().getTimeInMillis()-mClickTime;
 		}
 		
 		private void updateClickTime() {
 			mClickTime = Calendar.getInstance().getTimeInMillis();
 		}
 		
 		private void dragEvent(float eventX, float eventY) {
 			if (mFineTuningSound != null) {
 				eventX = mJoystick.dragDistanceX(eventX);
 				eventY = mJoystick.dragDistanceY(eventY);
 			}
 
 			moveSound(eventX, eventY);
 		}
 
 		class DragInitializeTimer extends TimerTask {
 			public void run() {
 				synchronized (mGestureLock) {
 					if (mCurrentGesture == TouchGesture.PRESS_BOARD && mMode == EDIT_BOARD) {
 						initializeDrag(mInitTouchEventX, mInitTouchEventY, mPressedSound);
 						vibrator.vibrate(60);
 					}
 				}
 			}
 		}
 		
 		class JoystickTimer extends TimerTask {
 			public void run() {
 				dragEvent(mLatestEventX, mLatestEventY);
 			}
 		}
 		
 		private void addJoystick(MotionEvent event) {
 			mJoystickTimer = new Timer();
 			mJoystickTimer.schedule(new JoystickTimer(), 210, 50);
 			mJoystick = new Joystick(BoardEditor.super.mContext, event);
 			mPageDrawer.giveJoystick(mJoystick);
 		}
 
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			mLatestEventX = event.getX();
 			mLatestEventY = event.getY();
 			
 			if (mThread == null) return false;
 			synchronized (mThread.getSurfaceHolder()) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					if (mMoveBackground) {
 						mBackgroundLeftDistance = event.getX() - mGsb.getBackgroundX();
 						mBackgroundTopDistance = event.getY() - mGsb.getBackgroundY();
 					} else if (mFineTuningSound != null) {
 						mInitTouchEventX = event.getX();
 						mInitTouchEventY = event.getY();
 						
 						mPressedSound = mFineTuningSound;
 						updateClickTime();
 						mCurrentGesture = TouchGesture.PRESS_BOARD;
 						new Timer().schedule(new DragInitializeTimer(), 200);
 						
 						addJoystick(event);
 					} else {
 						mInitTouchEventX = event.getX();
 						mInitTouchEventY = event.getY();
 
 						mPressedSound = findPressedSound(event);
 						updateClickTime();
 
 						if (mPressedSound == null) {
 							mCurrentGesture = TouchGesture.PRESS_BLANK;
 						} else {
 							mCurrentGesture = TouchGesture.PRESS_BOARD;
 							new Timer().schedule(new DragInitializeTimer(), DRAG_SWIPE_TIME);
 						}
 
 						if (mCopyColor != 0) {
 							copyColor(mPressedSound);
 							mPressedSound = null;
 						}
 					}
 				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					if (mMoveBackground) {
 						mGsb.setBackgroundX(event.getX() - mBackgroundLeftDistance);
 						mGsb.setBackgroundY(event.getY() - mBackgroundTopDistance);
 					} else if ((mCurrentGesture == TouchGesture.PRESS_BOARD ||  // PRESS_BOARD has timed gesture changing
 							(mCurrentGesture == TouchGesture.PRESS_BLANK && holdTime() < DRAG_SWIPE_TIME))
 							&& mFineTuningSound == null) {
 
 						float swipeTriggerDistance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
 						double distanceFromInit = Math.abs(mInitTouchEventX - event.getX());
 
 						synchronized (mGestureLock) {
 							if (distanceFromInit > swipeTriggerDistance) {
 								mCurrentGesture = TouchGesture.SWIPE;
 								
 								GraphicalSoundboard swapGsb = null;
 								SwipingDirection direction;
 								
 								if (event.getX() < mInitTouchEventX) {
 									direction = SwipingDirection.LEFT;
 									swapGsb = mPagination.getNextBoardPage(BoardEditor.super.mContext, mGsb);
 								} else {
 									direction = SwipingDirection.RIGHT;
 									swapGsb = mPagination.getPreviousPage(BoardEditor.super.mContext, mGsb);
 								}
 								
 								if (swapGsb == null) {
 									Toast.makeText(BoardEditor.super.mContext, "No page there", Toast.LENGTH_SHORT).show();
 								} else {
 									changeBoard(swapGsb, direction, true, false);
 								}
 							}
 						}
 
 					} else if (mCurrentGesture == TouchGesture.DRAG) {
 						if (mFineTuningSound == null) dragEvent(event.getX(), event.getY());
 					}
 					
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					synchronized (mGestureLock) {
 						if (mMoveBackground) {
 							mGsb.setBackgroundX(event.getX() - mBackgroundLeftDistance);
 							mGsb.setBackgroundY(event.getY() - mBackgroundTopDistance);
 							mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 						} else if ((mCurrentGesture == TouchGesture.PRESS_BOARD || mFineTuningSound != null) 
 								&& mMode == EDIT_BOARD && holdTime() < 200) {
 							mCurrentGesture = TouchGesture.TAP;
 							mClickTime = 0;
 				  			invalidate();
 							
 				  			String fineTuningText = (mFineTuningSound == null) ? "on" : "off";
 				  			
 							final CharSequence[] items = {"Fine tuning "+fineTuningText, "Info", "Name settings", "Image settings", "Sound settings",
 									"Copy sound", "Remove sound", "Set as..."};
 	
 					    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(BoardEditor.this);
 					    	optionsBuilder.setTitle("Options");
 					    	optionsBuilder.setItems(items, new DialogInterface.OnClickListener() {
 					    	    public void onClick(DialogInterface dialog, int item) {
 					    	    	
 					    	    	if (item == 0) {
 					    	    		if (mFineTuningSound == null) {
 					    	    			mFineTuningSound = mPressedSound;
 					    	    		} else {
 					    	    			mFineTuningSound = null;
 					    	    		}
 					    	    	} else if (item == 1) {
 					    	    		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(mPressedSound);
 					    	    		AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 					    	    		builder.setTitle("Sound info");
 					                	builder.setMessage("Name:\n"+mPressedSound.getName()+
 					                					"\n\nSound path:\n"+mPressedSound.getPath()+
 					                					"\n\nImage path:\n"+mPressedSound.getImagePath()+
 					                					"\n\nActive image path:\n"+mPressedSound.getActiveImagePath()+
 					                					"\n\nName and image linked:\n"+mPressedSound.getLinkNameAndImage()+
 					                					"\n\nHide image or text:\n"+mPressedSound.getHideImageOrText()+
 					                					"\n\nImage X:\n"+mPressedSound.getImageX()+
 					                					"\n\nImage Y:\n"+mPressedSound.getImageY()+
 					                					"\n\nImage width:\n"+mPressedSound.getImageWidth()+
 					                					"\n\nImage height:\n"+mPressedSound.getImageHeight()+
 					                					"\n\nName frame X:\n"+mPressedSound.getNameFrameX()+
 					                					"\n\nName frame Y:\n"+mPressedSound.getNameFrameY()+
 					                					"\n\nName frame width:\n"+soundNameDrawing.getNameFrameRect().width()+
 					                					"\n\nName frame height:\n"+soundNameDrawing.getNameFrameRect().height()+
 					                					"\n\nAuto arrange column:\n"+mPressedSound.getAutoArrangeColumn()+
 					                					"\n\nAuto arrange row:\n"+mPressedSound.getAutoArrangeRow()+
 					                					"\n\nSecond click action:\n"+mPressedSound.getSecondClickAction()+
 					                					"\n\nLeft volume:\n"+mPressedSound.getVolumeLeft()+
 					                					"\n\nRight volume:\n"+mPressedSound.getVolumeRight()+
 					                					"\n\nName frame border color:\n"+mPressedSound.getNameFrameBorderColor()+
 					                					"\n\nName frame inner color:\n"+mPressedSound.getNameFrameInnerColor()+
 					                					"\n\nName text color:\n"+mPressedSound.getNameTextColor()+
 					                					"\n\nName text size:\n"+mPressedSound.getNameSize()+
 					                					"\n\nShow name frame border paint:\n"+mPressedSound.getShowNameFrameBorderPaint()+
 					                					"\n\nShow name frame inner paint:\n"+mPressedSound.getShowNameFrameBorderPaint());
 					                					
 					                	builder.show();
 					    	    	} else if (item == 2) {
 					    	    		
 						    	    	LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
 					    	    			getSystemService(LAYOUT_INFLATER_SERVICE);
 					                	View layout = inflater.inflate(
 					                			R.layout.graphical_soundboard_editor_alert_sound_name_settings, 
 					                			(ViewGroup) findViewById(R.id.alert_settings_root));
 					                	
 					                	final EditText soundNameInput = 
 					              	  		(EditText) layout.findViewById(R.id.soundNameInput);
 					              	  	soundNameInput.setText(mPressedSound.getName());
 					              	  	
 					              	  	final EditText soundNameSizeInput = 
 					              	  		(EditText) layout.findViewById(R.id.soundNameSizeInput);
 					              	  	soundNameSizeInput.setText(Float.toString(mPressedSound.getNameSize()));
 					              	  	
 					              	  	final CheckBox checkShowSoundName = 
 					              	  		(CheckBox) layout.findViewById(R.id.showSoundNameCheckBox);
 					              	  	checkShowSoundName.setChecked(mPressedSound.getHideImageOrText() != GraphicalSound.HIDE_TEXT);
 					              	  	
 					              	  	final CheckBox checkShowInnerPaint = 
 					              	  		(CheckBox) layout.findViewById(R.id.showInnerPaintCheckBox);
 					              	  	checkShowInnerPaint.setChecked(mPressedSound.getShowNameFrameInnerPaint());
 					              	  	
 					              	  	final CheckBox checkShowBorderPaint = 
 					              	  		(CheckBox) layout.findViewById(R.id.showBorderPaintCheckBox);
 					              	  	checkShowBorderPaint.setChecked(mPressedSound.getShowNameFrameBorderPaint());
 					              	  	
 					              	  	final Button nameColorButton = 
 					              	  		(Button) layout.findViewById(R.id.nameColorButton);
 					              	  	nameColorButton.setOnClickListener(new OnClickListener() {
 					    					public void onClick(View v) {
 					    						mPressedSound.setName(soundNameInput.getText().toString());
 					    			  			mPressedSound.generateImageXYFromNameFrameLocation();
 					    			  			
 					    			  			Intent i = new Intent(BoardEditor.this, ColorChanger.class);
 					    		        		i.putExtra("parentKey", "changeNameColor");
 					    		        		i.putExtras(XStreamUtil.getSoundBundle(BoardEditor.super.mContext, mPressedSound, mGsb));
 					    		            	startActivityForResult(i, CHANGE_NAME_COLOR);
 					    					}
 					              	  	});
 					              	  	
 					              	  	final Button innerPaintColorButton = 
 					              	  		(Button) layout.findViewById(R.id.innerPaintColorButton);
 					              	  	innerPaintColorButton.setOnClickListener(new OnClickListener() {
 					    					public void onClick(View v) {
 					    						mPressedSound.setName(soundNameInput.getText().toString());
 					    			  			mPressedSound.generateImageXYFromNameFrameLocation();
 					    			  			
 					    			  			Intent i = new Intent(BoardEditor.this, ColorChanger.class);
 					    		        		i.putExtra("parentKey", "changeinnerPaintColor");
 					    		        		i.putExtras(XStreamUtil.getSoundBundle(BoardEditor.super.mContext, mPressedSound, mGsb));
 					    		            	startActivityForResult(i, CHANGE_INNER_PAINT_COLOR);
 					    					}
 					              	  	});
 					              	  	
 					              	  	final Button borderPaintColorButton = 
 					              	  		(Button) layout.findViewById(R.id.borderPaintColorButton);
 					              	  	borderPaintColorButton.setOnClickListener(new OnClickListener() {
 					    					public void onClick(View v) {
 					    						mPressedSound.setName(soundNameInput.getText().toString());
 					    			  			mPressedSound.generateImageXYFromNameFrameLocation();
 					    			  			
 					    			  			Intent i = new Intent(BoardEditor.this, ColorChanger.class);
 					    		        		i.putExtra("parentKey", "changeBorderPaintColor");
 					    		        		i.putExtras(XStreamUtil.getSoundBundle(BoardEditor.super.mContext, mPressedSound, mGsb));
 					    		            	startActivityForResult(i, CHANGE_BORDER_PAINT_COLOR);
 					    					}
 					              	  	});
 					              	  	
 					              	  	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 					              	  	builder.setView(layout);
 					              	  	builder.setTitle("Name settings");
 					          	  	
 					    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					    	          		public void onClick(DialogInterface dialog, int whichButton) {
 					    	          			boolean notifyIncorrectValue = false;
 					    	          			
 					    	          			if (checkShowSoundName.isChecked() == false) {
 					    	          				mPressedSound.setHideImageOrText(GraphicalSound.HIDE_TEXT);
 					    	          			} else if (checkShowSoundName.isChecked() && 
 					    	          				mPressedSound.getHideImageOrText() == GraphicalSound.HIDE_TEXT) {
 					    	          				mPressedSound.setHideImageOrText(GraphicalSound.SHOW_ALL);
 					    	          				mPressedSound.generateImageXYFromNameFrameLocation();
 					    	          			}
 					    	          			mPressedSound.setShowNameFrameInnerPaint(checkShowInnerPaint.isChecked());
 					    	          			mPressedSound.setShowNameFrameBorderPaint(checkShowBorderPaint.isChecked());
 					    	          			
 					    	          			mPressedSound.setName(soundNameInput.getText().toString());
 					    			  			
 					    			  			try {
 					    			  				mPressedSound.setNameSize(Float.valueOf(
 					    			  						soundNameSizeInput.getText().toString()).floatValue());
 					    	          			} catch(NumberFormatException nfe) {
 					    	          				notifyIncorrectValue = true;
 					    	          			}
 					    	          			
 					    	          			if (mPressedSound.getLinkNameAndImage()) {
 					    			  				mPressedSound.generateImageXYFromNameFrameLocation();
 					    			  			}
 					    	          			
 					    	          			if (notifyIncorrectValue == true) {
 					    	          				Toast.makeText(BoardEditor.super.mContext, "Incorrect value", 
 					    	          						Toast.LENGTH_SHORT).show();
 					    	          			}
 					    	          			
 					    	          			mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 					    	          		}
 					    	          	});
 		
 					    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					    		          	public void onClick(DialogInterface dialog, int whichButton) {
 					    	          	    }
 					    	          	});
 					    	          	
 					    	          	builder.show();
 			    						
 					    	    	} else if (item == 3) {
 					    	    		
 					    	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
 					    	    			getSystemService(LAYOUT_INFLATER_SERVICE);
 					                	View layout = inflater.inflate(
 					                			R.layout.graphical_soundboard_editor_alert_sound_image_settings, 
 					                			(ViewGroup) findViewById(R.id.alert_settings_root));
 	
 					              	  	
 					              	  	final CheckBox checkShowSoundImage = 
 					              	  		(CheckBox) layout.findViewById(R.id.showSoundImageCheckBox);
 					              	  	checkShowSoundImage.setChecked(mPressedSound.getHideImageOrText() != GraphicalSound.HIDE_IMAGE);
 					              	  	
 					              	  	mSoundImageWidthText = (TextView) layout.findViewById(R.id.soundImageWidthText);
 					              	  	mSoundImageWidthText.setText("Width (" + mPressedSound.getImage().getWidth() + ")");
 					            	  	
 					            	  	mSoundImageHeightText = (TextView) layout.findViewById(R.id.soundImageHeightText);
 					            	  	mSoundImageHeightText.setText("Height (" + mPressedSound.getImage().getHeight() + ")");
 					              	  	
 					              	  	mSoundImageWidthInput = (EditText) layout.findViewById(R.id.soundImageWidthInput);
 					              	  	mSoundImageWidthInput.setText(Float.toString(mPressedSound.getImageWidth()));  	
 					              	  	
 					              	  	mSoundImageHeightInput = (EditText) layout.findViewById(R.id.soundImageHeightInput);
 					              	  	mSoundImageHeightInput.setText(Float.toString(mPressedSound.getImageHeight()));
 					              	  	
 	
 					              	  	final CheckBox scaleWidthHeight = 
 					              	  			(CheckBox) layout.findViewById(R.id.scaleWidthHeightCheckBox);
 					              	  	scaleWidthHeight.setChecked(true);
 	
 					              	  	scaleWidthHeight.setOnClickListener(new OnClickListener() {
 					              	  		public void onClick(View v) {
 					              	  			try {
 					              	  			// Calculate a new scale
 					              	  				mWidthHeightScale = Float.valueOf(mSoundImageWidthInput.getText().toString()).floatValue() 
 					              	  									/ Float.valueOf(mSoundImageHeightInput.getText().toString()).floatValue();
 					              	  			} catch(NumberFormatException nfe) {Log.e(TAG, "Unable to calculate width and height scale", nfe);}
 					              	  		}
 					              	  	});
 					              	  	mWidthHeightScale = mPressedSound.getImageWidth() / mPressedSound.getImageHeight();
 	
 					              	  	mSoundImageWidthInput.setOnKeyListener(new OnKeyListener() {
 					              	  		public boolean onKey(View v, int keyCode, KeyEvent event) {
 					              	  			if (scaleWidthHeight.isChecked()) {
 					              	  				try {
 					              	  					float value = Float.valueOf(mSoundImageWidthInput.getText().toString()).floatValue();
 					              	  					mSoundImageHeightInput.setText(Float.valueOf(value/mWidthHeightScale).toString());
 					              	  				} catch(NumberFormatException nfe) {}
 					              	  			}
 					              	  			return false;
 					              	  		}
 					              	  	});
 					              	  	
 					              	  	mSoundImageHeightInput.setOnKeyListener(new OnKeyListener() {
 											public boolean onKey(View v, int keyCode, KeyEvent event) {
 												if (scaleWidthHeight.isChecked()) {
 													try {
 														float value = Float.valueOf(mSoundImageHeightInput.getText().toString()).floatValue();
 														mSoundImageWidthInput.setText(Float.valueOf(value*mWidthHeightScale).toString());
 													} catch(NumberFormatException nfe) {}
 												}
 												return false;
 											}
 					              	  	});
 					              	  	
 					              	  	final Button revertSizeButton = (Button) layout.findViewById(R.id.revertImageSizeButton);
 					              	  	revertSizeButton.setOnClickListener(new OnClickListener() {
 					              	  		public void onClick(View v) {
 					              	  			mSoundImageWidthInput.setText(Float.valueOf(mPressedSound.getImageWidth()).toString());
 					              	  			mSoundImageHeightInput.setText(Float.valueOf(mPressedSound.getImageHeight()).toString());
 					              	  			mWidthHeightScale = mPressedSound.getImageWidth() / mPressedSound.getImageHeight();
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
 					    						mPressedSound.setDefaultImage(BoardEditor.super.mContext);
 					    						String soundWidth = Integer.toString(mPressedSound.getImage().getWidth());
 					    						String soundHeight = Integer.toString(mPressedSound.getImage().getHeight());
 					    						mPressedSound.setImagePath(null);
 					    						mSoundImageWidthInput.setText(soundWidth);
 					              	  			mSoundImageHeightInput.setText(soundHeight);
 					              	  			mSoundImageWidthText.setText("Width (" + soundWidth + ")");
 					              	  			mSoundImageHeightText.setText("Height (" + soundHeight + ")");
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
 					    						mPressedSound.setDefaultActiveImage();
 					    						mPressedSound.setActiveImagePath(null);
 					    					}
 					              	  	});
 					              	  	
 					              	  	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 					              	  	builder.setView(layout);
 					              	  	builder.setTitle("Image settings");
 					          	  	
 					    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					    	          		public void onClick(DialogInterface dialog, int whichButton) {
 					    	          			boolean notifyIncorrectValue = false;
 					    	          			
 					    	          			if (checkShowSoundImage.isChecked() == false) {
 					    	          				mPressedSound.setHideImageOrText(GraphicalSound.HIDE_IMAGE);
 					    	          			} else if (checkShowSoundImage.isChecked() && 
 					    	          				mPressedSound.getHideImageOrText() == GraphicalSound.HIDE_IMAGE) {
 					    	          				mPressedSound.setHideImageOrText(GraphicalSound.SHOW_ALL);
 					    	          			}
 					    	          			
 					    	          			try {
 					    	          				mPressedSound.setImageWidthHeight(BoardEditor.super.mContext,
 					    	          						Float.valueOf(mSoundImageWidthInput.getText().toString()).floatValue(),
 					    	          						Float.valueOf(mSoundImageHeightInput.getText().toString()).floatValue());	
 					    	          			} catch(NumberFormatException nfe) {
 					    	          				notifyIncorrectValue = true;
 					    	          			}
 					    	          			mPressedSound.generateImageXYFromNameFrameLocation();
 					    	          			
 					    	          			if (notifyIncorrectValue == true) {
 					    	          				Toast.makeText(BoardEditor.super.mContext, "Incorrect value", Toast.LENGTH_SHORT).show();
 					    	          			}
 					    	          			mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 					    	          		}
 					    	          	});
 	
 					    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					    		          	public void onClick(DialogInterface dialog, int whichButton) {
 					    	          	    }
 					    	          	});
 					    	          	
 					    	          	builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
 											@Override
 											public void onCancel(DialogInterface dialog) {
 												mSoundImageDialog = null;
 											}
 										});
 					    	          	
 					    	          	mSoundImageDialog = builder.create();
 					    	          	mSoundImageDialog.show();
 					    	    	} else if (item == 4) {
 					    	    		
 					    	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
 				    	    				getSystemService(LAYOUT_INFLATER_SERVICE);
 					    	    		View layout = inflater.inflate(
 				                			R.layout.graphical_soundboard_editor_alert_sound_settings, 
 				                			(ViewGroup) findViewById(R.id.alert_settings_root));
 					    	    		
 					    	    		final CheckBox linkNameAndImageCheckBox = 
 					              	  		(CheckBox) layout.findViewById(R.id.linkNameAndImageCheckBox);
 					    	    		linkNameAndImageCheckBox.setChecked(mPressedSound.getLinkNameAndImage());
 					    	    		
 					    	    		final Button changeSoundPathButton = 
 					              	  		(Button) layout.findViewById(R.id.changeSoundPathButton);
 					    	    		changeSoundPathButton.setOnClickListener(new OnClickListener() {
 					    					public void onClick(View v) {
 					    						Intent i = new Intent(BoardEditor.this, FileExplorer.class);
 					    						i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_CHANGE_SOUND_PATH);
 					    						i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
 					    						startActivityForResult(i, CHANGE_SOUND_PATH);
 					    					}
 					              	  	});
 					    	    		
 					    	    		final Button secondClickActionButton = 
 					              	  		(Button) layout.findViewById(R.id.secondClickActionButton);
 					    	    		secondClickActionButton.setOnClickListener(new OnClickListener() {
 					    					public void onClick(View v) {
 					    						final CharSequence[] items = {"Play new", "Pause", "Stop"};
 							                	AlertDialog.Builder secondClickBuilder = new AlertDialog.Builder(
 							                			BoardEditor.this);
 							                	secondClickBuilder.setTitle("Action");
 							                	secondClickBuilder.setItems(items, new DialogInterface.OnClickListener() {
 							                	    public void onClick(DialogInterface dialog, int item) {
 							                	    	if (item == 0) {
 							                	    		mPressedSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_PLAY_NEW);
 							                	    	} else if (item == 1) {
 							                	    		mPressedSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_PAUSE);
 							                	    	} else if (item == 2) {
 							                	    		mPressedSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_STOP);
 							                	    	}
 							                	    }
 							                	});
 							                	AlertDialog secondClickAlert = secondClickBuilder.create();
 							                	secondClickAlert.show();
 					    					}
 					              	  	});
 					    	    		
 					    	    		final EditText leftVolumeInput = (EditText) layout.findViewById(R.id.leftVolumeInput);
 					            	  	leftVolumeInput.setText(Float.toString(mPressedSound.getVolumeLeft()*100) + "%");
 					            	  	final EditText rightVolumeInput = (EditText) layout.findViewById(R.id.rightVolumeInput);
 					            	  	rightVolumeInput.setText(Float.toString(mPressedSound.getVolumeRight()*100) + "%");
 					    	    		
 					    	    		AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 					              	  	builder.setView(layout);
 					              	  	builder.setTitle("Sound settings");
 					          	  	
 					    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					    	          		public void onClick(DialogInterface dialog, int whichButton) {
 					    	          			mPressedSound.setLinkNameAndImage(linkNameAndImageCheckBox.isChecked());
 					    	          			if (mPressedSound.getLinkNameAndImage()) {
 						    	          			mPressedSound.generateImageXYFromNameFrameLocation();
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
 					    	          					mPressedSound.setVolumeLeft(leftVolumeValue);
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
 					    	          					mPressedSound.setVolumeRight(rightVolumeValue);
 					    		          			} else {
 					    		          				notifyIncorrectValue = true;
 					    		          			}
 					    	          			} catch(NumberFormatException nfe) {
 					    	          				notifyIncorrectValue = true;
 					    	          			}
 					    	          			
 					    	          			if (notifyIncorrectValue == true) {
 					    	          				Toast.makeText(BoardEditor.super.mContext, "Incorrect value", Toast.LENGTH_SHORT).show();
 					    	          			}
 					    	          			mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 					    	          		}
 					    	          	});
 	
 					    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					    		          	public void onClick(DialogInterface dialog, int whichButton) {
 					    	          	    }
 					    	          	});
 					    	          	
 					    	          	builder.show();
 					    	    	} else if (item ==5) {
 					    	    		SoundboardMenu.mCopiedSound = (GraphicalSound) mPressedSound.clone();
 					    	    		
 					    	    	} else if (item == 6) {
 					                	
 					                	LayoutInflater removeInflater = (LayoutInflater) 
 					                			BoardEditor.this.getSystemService(LAYOUT_INFLATER_SERVICE);
 					                	View removeLayout = removeInflater.inflate(
 					                			R.layout.graphical_soundboard_editor_alert_remove_sound,
 					                	        (ViewGroup) findViewById(R.id.alert_remove_sound_root));
 					              	  	
 					              	  	final CheckBox removeFileCheckBox = 
 					              	  		(CheckBox) removeLayout.findViewById(R.id.removeFile);
 					              	  	removeFileCheckBox.setText(" DELETE " + mPressedSound.getPath().getAbsolutePath());
 					              	  	
 					              	  	AlertDialog.Builder removeBuilder = new AlertDialog.Builder(
 					              	  		BoardEditor.this);
 					              	  	removeBuilder.setView(removeLayout);
 					              	  	removeBuilder.setTitle("Removing " + mPressedSound.getName());
 					          	  	
 					              	  	removeBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					    	          	  	public void onClick(DialogInterface dialog, int whichButton) {
 					    	          	  		if (removeFileCheckBox.isChecked() == true) {
 					    	          	  			mPressedSound.getPath().delete();
 					    	          	  		}
 					    	          	  		mGsb.getSoundList().remove(mPressedSound);
 					    	          	  		mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 					    	          	    }
 					    	          	});
 	
 					              	  	removeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					    		          	public void onClick(DialogInterface dialog, int whichButton) {
 					    	          	    }
 					    	          	});
 					    	          	
 					              	  	removeBuilder.show();
 					    	    	} else if (item == 7) {
 					    	    		final CharSequence[] items = {"Ringtone", "Notification", "Alerts"};
 	
 					                	AlertDialog.Builder setAsBuilder = new AlertDialog.Builder(
 					                			BoardEditor.this);
 					                	setAsBuilder.setTitle("Set as...");
 					                	setAsBuilder.setItems(items, new DialogInterface.OnClickListener() {
 					                	    public void onClick(DialogInterface dialog, int item) {
 					                	    	String filePath = mPressedSound.getPath().getAbsolutePath();
 	
 					                	    	ContentValues values = new ContentValues();
 					                	    	values.put(MediaStore.MediaColumns.DATA, filePath);
 					                        	values.put(MediaStore.MediaColumns.TITLE, mPressedSound.getName());
 					                        	
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
 					                        	Uri newUri = BoardEditor.this.getContentResolver().insert(uri, values);
 					                        	
 					                        	RingtoneManager.setActualDefaultRingtoneUri(BoardEditor.this, selectedAction, newUri);
 					                        	
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
 						} else if (mCurrentGesture == TouchGesture.DRAG) {
 							if (mGsb.getAutoArrange()) {
 								
 								PanelSize panelSize = new PanelSize(BoardEditor.this);
 								int width = panelSize.getWidth();
 								int height = panelSize.getHeight();
 	      						
 	      						int column = -1, i = 0;
 	      						while (column == -1) {
 	      							if (event.getX() >= i*(width/mGsb.getAutoArrangeColumns()) && event.getX() <= (i+1)*(width/(mGsb.getAutoArrangeColumns()))) {
 	      								column = i;
 	        						}
 	      							if (i > 1000) {
 	      								Log.e(TAG, "column fail");
 	      								mPressedSound.getAutoArrangeColumn();
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
 	      								mPressedSound.getAutoArrangeRow();
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
 	      						
 	      						if (column == mPressedSound.getAutoArrangeColumn() && row == mPressedSound.getAutoArrangeRow()) {
 	      							moveSound(event.getX(), event.getY());
 	      						} else {
 	      							try {
 	      								moveSoundToSlot(swapSound, mPressedSound.getAutoArrangeColumn(), mPressedSound.getAutoArrangeRow(), 
 	      										swapSound.getImageX(), swapSound.getImageY(), swapSound.getNameFrameX(), swapSound.getNameFrameY());
 	      							} catch (NullPointerException e) {}
 	      							moveSoundToSlot(mPressedSound, column, row, mInitialImageX, mInitialImageY, mInitialNameFrameX, mInitialNameFrameY);
 	      							mGsb.addSound(mPressedSound);
 	      						}
 	  							
 							} else {
 								mGsb.getSoundList().add(mPressedSound);
 							}
 							mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 							
 						} else if (mCurrentGesture == TouchGesture.PRESS_BOARD && mMode == LISTEN_BOARD) {
 							playTouchedSound(mPressedSound);
 						}
 						
 						if (mJoystick != null) {
 							removeJoystick();
 							mBoardHistory.createHistoryCheckpoint(BoardEditor.super.mContext, mGsb);
 						}
 						
 						mCurrentGesture = null;
 					}
 				}
 				
 				return true;
 			}
 		}
 		
 		@Override
         public void onDraw(Canvas canvas) {
 			if (canvas == null) {
 				Log.w(TAG, "Got null canvas");
 				mNullCanvasCount++;
 				
 				// Drawing thread is still running while the activity is destroyed (surfaceCreated was probably called after surfaceDestroyed).
 				// Reproduce by killing the editor immediately after it is created.
 				// It's difficult to kill the thread properly while supporting different orientations and closing of screen.
 				if (mNullCanvasCount > 5) {
 			    	Log.e(TAG, "Drawing thread was not destroyed properly");
 			    	mThread.setRunning(false);
 			    	mThread = null;
 				}
 			} else {
 				mNullCanvasCount = 0;
 				super.dispatchDraw(canvas);
 				
 				GraphicalSound pressedSound = null;
 				if (mCurrentGesture == TouchGesture.DRAG) pressedSound = mPressedSound;
 				
 				mPageDrawer.drawSurface(canvas, pressedSound);
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
 			Log.d(TAG, "Surface created");
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder holder) {
             mThread.setRunning(false);
             mThread = null;
             Log.d(TAG, "Surface destroyed");
 		}
 		
 	}
 	
 	class DrawingThread extends Thread {
         private SurfaceHolder mSurfaceHolder;
         private boolean mRun = false;
         
         private long mAnimationLastUpdate = 0;
 		
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
                 	if (mPageDrawer.needAnimationRefreshSpeed()) {
                 		
                 		int animationUpdateInterval = 20;
                 		long currentTime = System.currentTimeMillis();
                 		
                 		long timeToNextUpdate = (mAnimationLastUpdate + animationUpdateInterval) - currentTime;
                 		if (timeToNextUpdate > 0) {
                 			mAnimationLastUpdate = currentTime;
                 			Thread.sleep(animationUpdateInterval);
                 		} else {
                 			mAnimationLastUpdate = currentTime;
                 		}
                 		
                 	} else if (mMode == EDIT_BOARD && (mCurrentGesture == TouchGesture.DRAG || mMoveBackground )) {
             			Thread.sleep(10);
             		} else if (mMode == EDIT_BOARD && mCurrentGesture != TouchGesture.DRAG && mMoveBackground == false) {
             			
             			for (int i = 0; i <= 5; i++) {
             				Thread.sleep(100);
             				if (mCurrentGesture == TouchGesture.DRAG || mMoveBackground) {
             					break;
             				}
             			}
             		} else if (mMode == LISTEN_BOARD) {
             			for (int i = 0; i <= 300; i++) {
             				Thread.sleep(20);
             				if (mMode == EDIT_BOARD || mCanvasInvalidated == true) {
             					mCanvasInvalidated = false;
             					break;
             				}
             			}
             		} else {
             			Log.e(TAG, "Undefined redraw rate state");
             			Thread.sleep(1000);
             		}
 				} catch (InterruptedException e) {}
             }
         }
     }
 
 }
