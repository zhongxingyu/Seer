 package com.example.tb;
 
 import android.content.ClipData;
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.DragShadowBuilder;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 public class ViewBar extends LinearLayout 
 implements View.OnClickListener, View.OnTouchListener {
 	private boolean DEBUG = true;
 	private String TAG = "ViewBar";
 	
 	private Context mContext;
 	private TacticBoard mTacticBoard;
 	private ColorPaletteDialog mColorDialog;
 
 	private ImageView mO;
 	private ImageView mX;
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
 		
 		mO = (ImageView) findViewById(R.id.img_o);
 		mX = (ImageView) findViewById(R.id.img_x);
 		mPlusText = (ImageView) findViewById(R.id.plus_text);
 
 		mUndo = (ImageView) findViewById(R.id.undo);
 		mMove = (ImageView) findViewById(R.id.move);
 		mSolidLine = (ImageView) findViewById(R.id.solid_line);
 		mShortDashLine = (ImageView) findViewById(R.id.short_dash_line);
 		mLongDashLine = (ImageView) findViewById(R.id.long_dash_line);
 		mColorSetting = (ImageView) findViewById(R.id.color_setting);
 		mSave = (ImageView) findViewById(R.id.save);
 		mNew = (ImageView) findViewById(R.id.new_file);
 		mShare = (ImageView) findViewById(R.id.share);
 
 		mO.setOnTouchListener(this);
 		mX.setOnTouchListener(this);		
 		mPlusText.setOnTouchListener(this);
 		
 		mUndo.setOnClickListener(this);
 		mMove.setOnClickListener(this);		
 		mSolidLine.setOnClickListener(this);
 		mShortDashLine.setOnClickListener(this);
 		mLongDashLine.setOnClickListener(this);
 		mColorSetting.setOnClickListener(this);
 		mSave.setOnClickListener(this);
 		mNew.setOnClickListener(this);
 		mShare.setOnClickListener(this);
 		
 		// set default color to black
 		mColorSetting.setBackgroundColor(Color.BLACK);
 		
 		// by default, view moving is enabled, drawing is not
 		((MainActivity) mContext).setMoving(mMoving);
 		mTacticBoard.setMoving(false);
 		mMove.setBackgroundColor(Color.LTGRAY);
 		
 		mColorDialog = new ColorPaletteDialog(mContext);
 		mColorDialog.setCanceledOnTouchOutside(true);
 
 		ColorPaletteDialog.sListener = new ColorPaletteDialog.OnColorSelectedListener() {
 			@Override
 			public void onColorSelected(int color) {
 				mColor = color;
 				((MainActivity) mContext).setTextColor(mColor);
 				mTacticBoard.updatePaintColor(mColor);
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
 		
 		case R.id.move:
 			mMove.setBackgroundColor(Color.LTGRAY);	
 			mSolidLine.setBackgroundColor(Color.WHITE);
 			mShortDashLine.setBackgroundColor(Color.WHITE);
 			mLongDashLine.setBackgroundColor(Color.WHITE);
 			
 			((MainActivity) mContext).setMoving(true);
 			mTacticBoard.setMoving(false);
 
 			break;
 		
 		case R.id.solid_line:
			mTacticBoard.setSolidLinePaint();
 			mMove.setBackgroundColor(Color.WHITE);	
 			mSolidLine.setBackgroundColor(Color.LTGRAY);
 			mShortDashLine.setBackgroundColor(Color.WHITE);
 			mLongDashLine.setBackgroundColor(Color.WHITE);	
 			
 			((MainActivity) mContext).setMoving(false);
 			mTacticBoard.setMoving(true);
 			
 			break;
 				
 		case R.id.short_dash_line:
 			mTacticBoard.setShortDashPaint();
 			mMove.setBackgroundColor(Color.WHITE);	
 			mSolidLine.setBackgroundColor(Color.WHITE);
 			mShortDashLine.setBackgroundColor(Color.LTGRAY);
 			mLongDashLine.setBackgroundColor(Color.WHITE);
 			
 			((MainActivity) mContext).setMoving(false);
 			mTacticBoard.setMoving(true);
 			
 			break;
 			
 		case R.id.long_dash_line:
 			mTacticBoard.setLongDashPaint();
 			mMove.setBackgroundColor(Color.WHITE);	
 			mSolidLine.setBackgroundColor(Color.WHITE);
 			mShortDashLine.setBackgroundColor(Color.WHITE);
 			mLongDashLine.setBackgroundColor(Color.LTGRAY);
 			
 			((MainActivity) mContext).setMoving(false);
 			mTacticBoard.setMoving(true);
 			
 			break;	
 		
 		case R.id.color_setting:
 			mColorDialog.show();
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
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		ClipData data = ClipData.newPlainText("", "");
 		
 		DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
 		v.startDrag(data, shadowBuilder, v, 0);
 		//v.setVisibility(View.INVISIBLE);
 		return true;
 	}
 
 }
