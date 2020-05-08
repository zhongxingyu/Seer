 package com.example.tb;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 public class ViewBar extends LinearLayout implements View.OnClickListener {
 	private boolean DEBUG = true;
 	private String TAG = "ViewBar";
 	
 	private Context mContext;
 	private TacticBoard mTacticBoard;
 	private ColorPaletteDialog mColorDialog;
 
 	private ImageView mUndo;
 	private ImageView mMove;
 	private ImageView mSolidLine;
 	private ImageView mShortDashLine;
 	private ImageView mLongDashLine;
 	private ImageView mColorSetting;
 	private ImageView mPlusText;
 	private ImageView mSave;
 	private ImageView mNew;
 	private ImageView mShare;
 	
 	private boolean mMoving = true;
 		
 	private int mColor = 0xff000000;
 	private int mSize = 2;
 	
 	public ViewBar(Context context, TacticBoard tb) {
 		super(context);
 		this.mContext = context;
 		this.mTacticBoard = tb;
 		
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.view_bar, this, true);
 		
 		mUndo = (ImageView) findViewById(R.id.undo);
 		mMove = (ImageView) findViewById(R.id.move);
 		mSolidLine = (ImageView) findViewById(R.id.solid_line);
 		mShortDashLine = (ImageView) findViewById(R.id.short_dash_line);
 		mLongDashLine = (ImageView) findViewById(R.id.long_dash_line);
 		mColorSetting = (ImageView) findViewById(R.id.color_setting);
 		mPlusText = (ImageView) findViewById(R.id.plus_text);
 		mSave = (ImageView) findViewById(R.id.save);
 		mNew = (ImageView) findViewById(R.id.new_file);
 		mShare = (ImageView) findViewById(R.id.share);
 		
 		mUndo.setOnClickListener(this);
 		mMove.setOnClickListener(this);		
 		mSolidLine.setOnClickListener(this);
 		mShortDashLine.setOnClickListener(this);
 		mLongDashLine.setOnClickListener(this);
 		mColorSetting.setOnClickListener(this);
 		mPlusText.setOnClickListener(this);
 		mSave.setOnClickListener(this);
 		mNew.setOnClickListener(this);
 		mShare.setOnClickListener(this);
 		
 		mColorSetting.setBackgroundColor(Color.BLACK);
 		((MainActivity) mContext).setMoving(mMoving);
 		mMove.setBackgroundColor(Color.LTGRAY);
 		
 		mColorDialog = new ColorPaletteDialog(mContext);
 		mColorDialog.setCanceledOnTouchOutside(true);
 
 		ColorPaletteDialog.sListener = new ColorPaletteDialog.OnColorSelectedListener() {
 			@Override
 			public void onColorSelected(int color) {
 				mColor = color;
 				mTacticBoard.updatePaintProperty(mColor, mSize);
 				mColorSetting.setBackgroundColor(mColor);
 			}
 		};
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.undo:
 			mTacticBoard.undo();
 			break;
 		
 		case R.id.solid_line:
 			mTacticBoard.setDefaultPaint();
 			mSolidLine.setBackgroundColor(Color.LTGRAY);
 			mShortDashLine.setBackgroundColor(Color.WHITE);
 			mLongDashLine.setBackgroundColor(Color.WHITE);
 			break;
 		
 		case R.id.move:
 			if (mMoving) {
 				mMove.setBackgroundColor(Color.WHITE);
 				mMoving = false;
 			} else {
 				mMove.setBackgroundColor(Color.LTGRAY);
 				mMoving = true;
 			}
 			
 			((MainActivity) mContext).setMoving(mMoving);
 			break;
 			
 		case R.id.short_dash_line:
 			mTacticBoard.setShortDashPaint();
 			mSolidLine.setBackgroundColor(Color.WHITE);
 			mShortDashLine.setBackgroundColor(Color.LTGRAY);
 			mLongDashLine.setBackgroundColor(Color.WHITE);
 			break;
 			
 		case R.id.long_dash_line:
 			mTacticBoard.setLongDashPaint();
 			mSolidLine.setBackgroundColor(Color.WHITE);
 			mShortDashLine.setBackgroundColor(Color.WHITE);
 			mLongDashLine.setBackgroundColor(Color.LTGRAY);
 			break;	
 		
 		case R.id.color_setting:
 			mColorDialog.show();
 			break;
 		
 		case R.id.plus_text:
 			((MainActivity) mContext).showPlusTextDialog();
 			break;
 		
 		case R.id.save:
 			((MainActivity) mContext).saveImgToGallery();
 			break;	
 		
 		case R.id.new_file:
			((MainActivity) mContext).reset();
 			break;	
 		
 		case R.id.share:
 			((MainActivity) mContext).share();
 			break;	
 			
 		default:
 			Log.e(TAG, "ERROR: switch default clided");
 			break;
 		}
 	}
 	
 }
