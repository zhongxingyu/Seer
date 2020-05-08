 package com.conica.DailyCapture;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Set;
 
 import android.app.Activity;
 import android.content.res.ColorStateList;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewTreeObserver;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.Gallery;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class PhotoPreviewActivity extends Activity implements AdapterView.OnItemSelectedListener{
 	private static final String TAG = "capture";
 	private static final int BYPOSITION = 2;
 	private PhotoFlingView mPhotoFlingView;	
 	private Gallery mGallery;
 	private RecordsDBAdapter mRecordsDBAdapter;
 	private Cursor mRecordsCursor;
 	private int mPosition = 0;
 	private int mPhotoCt = 0;
 	private Bitmap mCurrentBitmap;
 	private GestureDetector mGalleryGestureDetector;
 	private ImageButton mInfo_bt;
 	private TextView mInfoMsg_textview;
 	public CachedBitmap mCachedBitmap;
 	private DecodeBitmapTask mDecodeTask;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setContentView(R.layout.photopreview);
         Bundle extras = getIntent().getExtras();
         long photoId = extras != null ? extras.getLongArray(RecordsDBAdapter.KEY_PHOTO_ROWID)[0]: 0;
         long tableId = extras != null ? extras.getLongArray(RecordsDBAdapter.KEY_RECORD_ROWID)[1]: 0;
         
 	    mRecordsDBAdapter = MainApp.gRecordsDBAdapter; 
         
         mRecordsCursor = mRecordsDBAdapter.fetchAllPhoto(tableId);
         startManagingCursor(mRecordsCursor);
         mRecordsCursor.moveToFirst();
         
         mPhotoCt = mRecordsCursor.getCount();
         for(int i =0; i< mPhotoCt; i++){
         	long id = mRecordsCursor.getLong(mRecordsCursor.getColumnIndexOrThrow(RecordsDBAdapter.KEY_PHOTO_ROWID));
         	if(id == photoId){
         		break;
         	}
         	mPosition ++;
         	if(mRecordsCursor.isLast())
         		break;
         	mRecordsCursor.moveToNext();
         }
 
         mCachedBitmap = new CachedBitmap(mPhotoCt);
         mDecodeTask = new DecodeBitmapTask();
         mDecodeTask.setSharedCahcedBitmap(mCachedBitmap);
         mDecodeTask.initial();
         mDecodeTask.execute(""); 
   
         mGallery = (Gallery) findViewById(R.id.gallery);
         PhotoSwitcherAdapter im = new PhotoSwitcherAdapter(this);
         im.setActivity(this);
         im.setCount(mRecordsCursor.getCount());
         mGallery.setAdapter(im);
         mGallery.setSelection(mPosition);
         mGallery.setUnselectedAlpha(0.5f);
         mGallery.setOnTouchListener(new OnTouchListener() {		
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mGalleryGestureDetector.onTouchEvent(event);
 				return false;
 			}
 		});
         mGallery.setOnItemSelectedListener(this);
         
         mPhotoFlingView = (PhotoFlingView) findViewById(R.id.flingview);
         mPhotoFlingView.setActivity(this);
     	mPhotoFlingView.setImageBitmap(getBitmap(-1,mPosition));
         ViewTreeObserver vto = mPhotoFlingView.getViewTreeObserver();
         vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
             public boolean onPreDraw() {
             	mPhotoFlingView.center(true, true);
                 return true;
             }
         });
         
         mGalleryGestureDetector = new GestureDetector(new GalleryGestureListener());
                
         mInfoMsg_textview = (TextView)findViewById(R.id.photoinfomsg);
         mInfoMsg_textview.setVisibility(View.GONE);
         mInfoMsg_textview.setCursorVisible(false);
         Drawable ibk = getResources().getDrawable(R.drawable.infomsgbk);
         ibk.setAlpha(180);
         mInfoMsg_textview.setBackgroundDrawable(ibk);
         mInfoMsg_textview.setTextColor(ColorStateList.valueOf(Color.BLACK));
            
         Drawable i = getResources().getDrawable(R.drawable.info);
         i.setAlpha(150);
         mInfo_bt = (ImageButton)findViewById(R.id.photoinfobt);
         mInfo_bt.setImageDrawable(i);      
         
         Drawable tmp = getResources().getDrawable(R.drawable.icon);
         tmp.setAlpha(0);
         mInfo_bt.setBackgroundDrawable(tmp);
         mInfo_bt.setOnClickListener(new OnClickListener() {		
 			@Override
 			public void onClick(View v) {
 				if(mInfoMsg_textview.isShown()){
 					mInfoMsg_textview.setVisibility(View.GONE);
 				}else{
 					mInfoMsg_textview.setVisibility(View.VISIBLE);
 					updateInfoMsg();
 				}
 			}
 		});     
     }
   
 	@Override
 	protected void onDestroy() {
 		Log.i(TAG,"PhotoPreviewActivity onPause mPhotoCt = " + mPhotoCt);
 		//decodeTask.cancel(true);
 		//mCachedBitmap.updateWaitPos(mPhotoCt + 3);
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// gitview gesture will handle photo scale, translate, fling to next photo effect;
 		mPhotoFlingView.onTouchEvent(event);
 		// will handle show image which is chosen by gallery
 		mGalleryGestureDetector.onTouchEvent(event);
 		return super.onTouchEvent(event);
 	}
     	
 	/*
 	public Bitmap getBitmap(int direction, int p) {
 		switch(direction){
 		case PhotoFlingView.LEFT:
 			position ++;
 			if(position > photoCt -1)
 				position = 0;
 			break;
 		case PhotoFlingView.RIGHT:
 			position --;
 			if(position < 0)
 				position = photoCt -1;
 			break;
 		case BYPOSITION:
 			position = p;
 			break;
 		default:
 				break;
 		}
 		mCursor.moveToPosition(position);
 		gallery.setSelection(position);
 		
 		String photopath = mCursor.getString(mCursor.getColumnIndex(RecordsDBAdapter.KEY_PHOTO_PATH));
 
 		Bitmap oldBitmap = null;
 		if(currentBitmap != null)
 			oldBitmap = Bitmap.createBitmap(currentBitmap);
 		
 		if(photopath.length() > 0)
 			currentBitmap = BitmapFactory.decodeFile(photopath);
 		else
 			currentBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
 
 		
 		
 		if(oldBitmap != null)
 			oldBitmap.recycle();
 		return currentBitmap;		 
 	}
 	*/
 	
 	public Bitmap getBitmap(int direction, int p) {
 		switch(direction){
 		case PhotoFlingView.LEFT:
 			mPosition ++;
 			if(mPosition > mPhotoCt -1)
 				mPosition = 0;		
 			break;
 		case PhotoFlingView.RIGHT:
 			mPosition --;
 			if(mPosition < 0)
 				mPosition = mPhotoCt -1;
 			break;
 		case BYPOSITION:
 			mPosition = p;
 			break;
 		default:
 				break;
 		}
 
 		mGallery.setSelection(mPosition);
 
 		while(mDecodeTask.mBitmapQueue.size() > 0){
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	
 		Bitmap c = mCachedBitmap.findBmpByPos(mPosition);		
 		if(c == null){	
 			mCachedBitmap.updateWaitPos(mPosition);
 			mRecordsCursor.moveToPosition(mPosition);
 			String photopath = mRecordsCursor.getString(mRecordsCursor.getColumnIndex(RecordsDBAdapter.KEY_PHOTO_PATH));
 			mCurrentBitmap = BitmapFactory.decodeFile(photopath);	
 			Log.i(TAG,"- self create " + mPosition + " bitmap = " + mCurrentBitmap);
 			mCachedBitmap.addBitmap(mPosition, mCurrentBitmap);
 		}else{
 			mCurrentBitmap = c;
 		}
 	
 		mCachedBitmap.updateWaitPos(mPosition);
 		// update waitPos and decode bitmaps in waitPos
 		while(true){
 			int tmp = mCachedBitmap.getWaitPos();
 			if(tmp != -1){
 				//Log.i(TAG,"update position " + tmp);
 				mRecordsCursor.moveToPosition(tmp);
 				String path = mRecordsCursor.getString(mRecordsCursor.getColumnIndex(RecordsDBAdapter.KEY_PHOTO_PATH));
 		        mDecodeTask.addBitmapQueue(tmp, path);
 			}else{
 				break;
 			}
 		}
 
 		return mCurrentBitmap;		 
 	}
 	
 	
 	public Bitmap getThumbBitmap(int p) {
 		int oldp = mRecordsCursor.getPosition();
 		mRecordsCursor.moveToPosition(p);
 		String photopath = mRecordsCursor.getString(mRecordsCursor.getColumnIndex(RecordsDBAdapter.KEY_PHOTO_THUNBPATH));
 		final Bitmap currBitmap;	
 		if(photopath.length() > 0)
 			currBitmap = BitmapFactory.decodeFile(photopath);
 		else
 			currBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
 		mRecordsCursor.moveToPosition(oldp);
 		return currBitmap;		 
 	}
 	
 	public class GalleryGestureListener extends GestureDetector.SimpleOnGestureListener{
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			mPhotoFlingView.setImageBitmap(getBitmap(BYPOSITION,mPosition));
 			updateInfoMsg();
 			return super.onSingleTapConfirmed(e);
 		}
 	}
 	
 	public void toggleGallery() {
 		if(mGallery.isShown())
 			mGallery.setVisibility(View.GONE);
 		else
 			mGallery.setVisibility(View.VISIBLE);
 	}
 	
 	public void updateInfoMsg() {
 		int pos = mRecordsCursor.getPosition();
 		mRecordsCursor.moveToPosition(mPosition);	
 		mInfoMsg_textview.setText("Name: " + mRecordsCursor.getString(mRecordsCursor.getColumnIndex(RecordsDBAdapter.KEY_PHOTO_NAME)) + "\nDescription: " + mRecordsCursor.getString(mRecordsCursor.getColumnIndex(RecordsDBAdapter.KEY_PHOTO_DESP)));
 		mRecordsCursor.moveToPosition(pos);
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View v, int p, long id) {
 		mPosition = p;	
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 	}
 	
 	
 	public class CachedBitmap{
 		private ArrayList<Integer> cachedPos;
 		private ArrayList<Integer> waitPos;
 		private ArrayList<Integer> removePos;
 		private ArrayList<Bitmap> cachedBmp;
 		private int photoCnt;
 		
 		public CachedBitmap(int c){
 			cachedPos = new ArrayList<Integer>();
 			cachedBmp = new ArrayList<Bitmap>();
 			waitPos = new ArrayList<Integer>();
 			removePos = new ArrayList<Integer>();
 			photoCnt = c;
 		}
 		
 		public void addBitmap(int pos, Bitmap bmp){
 			cachedPos.add(pos);
 			cachedBmp.add(bmp);
 		}
 				
 		public Bitmap findBmpByPos(int pos){
 			if(cachedPos.contains(pos)){
 				return cachedBmp.get(cachedPos.indexOf(pos));
 			}else{
 				return null;
 			}
 		}
 		
 		public void updateWaitPos(int pos){
 			int newlp = pos - 1 < 0 ? photoCnt -1: pos-1;
 			int newrp = pos + 1 > photoCnt - 1 ? 0:pos+1;
 			waitPos.clear();
 			waitPos.add(pos);
 			waitPos.add(newlp);
 			waitPos.add(newrp);
 			
 			for(int i = 0; i< cachedPos.size(); i++){
 				// remove bitmap no longer useful
 				if(!(cachedPos.get(i) == pos || cachedPos.get(i) == newlp || cachedPos.get(i) == newrp)){
 					removePos.add(cachedPos.get(i));
 				}else{
 					// if already has the bitmap, remove it from waitPos
 					int p = cachedPos.get(i);
 					if(waitPos.indexOf(p) > 0)
 						waitPos.remove(waitPos.indexOf(p));
 				}
 			}
 			removeBitmap();
 		}
 		
 		public int getWaitPos(){
 			if(waitPos.size() > 0){
 				int a = waitPos.get(0);
 				waitPos.remove(0);
 				return a;		
 			}else{
 				return -1;
 			}
 		}
 		
 		public void removeBitmap(){
 			for(int i = 0; i < removePos.size(); i++){
 				int pos = removePos.get(i);
 				int index = cachedPos.indexOf(pos);
 				cachedPos.remove(index);
 				Log.i(TAG,"- release " + pos);
 				cachedBmp.get(index).recycle();
 				cachedBmp.remove(index);
 			}
 			removePos.clear();
 		}		
 	}
 }
 
