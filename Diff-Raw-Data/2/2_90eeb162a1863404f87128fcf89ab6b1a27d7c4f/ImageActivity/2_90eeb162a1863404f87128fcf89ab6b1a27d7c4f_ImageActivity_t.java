 package com.zoeetrope.lineupcamera;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.util.FloatMath;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Gallery;
 import android.widget.ImageSwitcher;
 import android.widget.ImageView;
 import android.widget.ViewSwitcher.ViewFactory;
 
 import com.zoeetrope.lineupcamera.model.Album;
 
 public class ImageActivity extends Activity implements OnTouchListener,
 		ViewFactory {
 
 	private ImageView mImageView;
 	private ImageSwitcher mSwitcher;
 	private Gallery mGallery;
 	private Album mAlbum;
 	private int mPosition;
 	private Bitmap mBitmap;
 
 	private Point mStart = new Point();
 	private Point mPrevious = new Point();
 	private Point mMidPoint = new Point();
 	private Matrix mMatrix = new Matrix();
 	private Matrix mSavedMatrix = new Matrix();
 	private float mOldDistance = 0;
 
 	private static final int NONE = 0;
 	private static final int DRAG = 1;
 	private static final int ZOOM = 2;
 	private int mMode = NONE;
 	private boolean mSwipeReset = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.image);
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			mAlbum = new Album(extras.getString("ALBUM"));
 			mPosition = extras.getInt("POSITION");
 
 			mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
 			mSwitcher.setFactory(this);
 
 			mGallery = (Gallery) findViewById(R.id.gallery);
 			mGallery.setAdapter(new ImageGalleryAdapter(this,
 					R.layout.image_gallery_item, mAlbum));
 			mGallery.setSelection(mPosition);
 
 			mGallery.setOnItemClickListener(new OnItemClickListener() {
 				public void onItemClick(AdapterView parent, View v,
 						int position, long id) {
 					mPosition = position;
 					loadImage();
 				}
 			});
 		}
 	}
 
 	@Override
 	public View makeView() {
 		LayoutInflater inflater = (LayoutInflater) this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		ImageView imageView = (ImageView) inflater.inflate(R.layout.image_view,
 				null, false);
		imageView.setOnTouchListener(this);
 
 		return imageView;
 	}
 
 	private void loadImage() {
 		Display display = getWindowManager().getDefaultDisplay();
 		int bitmapHeight = Math.min(display.getHeight(), display.getWidth());
 		mBitmap = mAlbum.getImages().get(mPosition).getBitmap(bitmapHeight);
 
 		mSwitcher.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
 
 		ImageView imageView = (ImageView) mSwitcher.getCurrentView();
 		imageView.setImageMatrix(mMatrix);
 		mImageView = imageView;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		loadImage();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		mBitmap.recycle();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN:
 			mSavedMatrix.set(mMatrix);
 			mStart.set((int) event.getX(), (int) event.getY());
 			mMode = DRAG;
 			mSwipeReset = false;
 			break;
 		case MotionEvent.ACTION_POINTER_DOWN:
 			mOldDistance = getPointDistance(event);
 
 			if (mOldDistance > 10f) {
 				mSavedMatrix.set(mMatrix);
 				updateMidPoint(mMidPoint, event);
 				mMode = ZOOM;
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (!mSwipeReset) {
 				float newDistance = 0;
 				float swipeDistanceX = event.getX(0) - mPrevious.x;
 				float swipeDistanceY = event.getY(0) - mPrevious.y;
 
 				if ((swipeDistanceX > 100 && (swipeDistanceY < 50 && swipeDistanceY > -50))
 						|| (swipeDistanceX < -100 && (swipeDistanceY < 50 && swipeDistanceY > -50))) {
 					float totalDistanceX = event.getX(0) - mStart.x
 							- swipeDistanceX;
 					float totalDistanceY = event.getY(0) - mStart.y
 							- swipeDistanceY;
 					totalDistanceX *= -1;
 					totalDistanceY *= -1;
 
 					mMatrix.postTranslate(totalDistanceX, totalDistanceY);
 
 					if (swipeDistanceX > 0) {
 						showPreviousImage();
 					} else {
 						showNextImage();
 					}
 					mSwipeReset = true;
 					break;
 				}
 
 				if (mMode == ZOOM) {
 					newDistance = getPointDistance(event);
 				}
 
 				if (mMode == DRAG || (mMode == ZOOM && newDistance > 10f)) {
 					mMatrix.set(mSavedMatrix);
 
 					// Extract all the info we need to limit the panning.
 					float[] matrixValues = new float[9];
 					mMatrix.getValues(matrixValues);
 					float translateX = event.getX(0) - mStart.x;
 					float translateY = event.getY(0) - mStart.y;
 					float currentY = matrixValues[Matrix.MTRANS_Y];
 					float currentX = matrixValues[Matrix.MTRANS_X];
 					float currentScale = matrixValues[Matrix.MSCALE_X];
 					float currentHeight = mBitmap.getHeight() * currentScale;
 					float currentWidth = mBitmap.getWidth() * currentScale;
 					float newX = currentX + translateX;
 					float newY = currentY + translateY;
 					RectF drawingRect = new RectF(newX, newY, newX
 							+ currentWidth, newY + currentHeight);
 					Rect viewRect = new Rect();
 
 					mImageView.getLocalVisibleRect(viewRect);
 
 					// Calculate if we have exceeded the panning limits.
 					float diffUp = Math.min(viewRect.bottom
 							- drawingRect.bottom, viewRect.top
 							- drawingRect.top);
 					float diffDown = Math.max(viewRect.bottom
 							- drawingRect.bottom, viewRect.top
 							- drawingRect.top);
 					float diffLeft = Math.min(viewRect.left - drawingRect.left,
 							viewRect.right - drawingRect.right);
 					float diffRight = Math.max(
 							viewRect.left - drawingRect.left, viewRect.right
 									- drawingRect.right);
 
 					// Apply a correction to keep the image within the limits.
 					if (diffUp > 0) {
 						translateY += diffUp;
 					}
 					if (diffDown < 0) {
 						translateY += diffDown;
 					}
 					if (diffLeft > 0) {
 						translateX += diffLeft;
 					}
 					if (diffRight < 0) {
 						translateX += diffRight;
 					}
 
 					mMatrix.postTranslate(translateX, translateY);
 
 					if (mMode == ZOOM) {
 						float scale = newDistance / mOldDistance;
 						mMatrix.postScale(scale, scale, mMidPoint.x,
 								mMidPoint.y);
 					}
 				}
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_POINTER_UP:
 			mMode = NONE;
 			break;
 		}
 
 		mPrevious.set((int) event.getX(), (int) event.getY());
 		mImageView.setImageMatrix(mMatrix);
 
 		return true;
 	}
 
 	private void resetMatrix() {
 		Matrix m = mImageView.getImageMatrix();
 		RectF drawableRect = new RectF(0, 0, mBitmap.getWidth(),
 				mBitmap.getHeight());
 		RectF viewRect = new RectF(0, 0, mImageView.getWidth(),
 				mImageView.getHeight());
 
 		m.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL);
 		m.setScale(1, 1);
 
 		mImageView.setImageMatrix(m);
 		mMatrix.set(m);
 		mSavedMatrix.set(m);
 	}
 
 	private void showNextImage() {
 		mPosition += 1;
 
 		if (mPosition >= mAlbum.getImages().size()) {
 			mPosition = 0;
 		}
 
 		mBitmap.recycle();
 		loadImage();
 	}
 
 	private void showPreviousImage() {
 		mPosition -= 1;
 
 		if (mPosition < 0) {
 			mPosition = mAlbum.getImages().size() - 1;
 		}
 
 		mBitmap.recycle();
 		loadImage();
 	}
 
 	private float getPointDistance(MotionEvent event) {
 		float x = event.getX(0) - event.getX(1);
 		float y = event.getY(0) - event.getY(1);
 
 		return FloatMath.sqrt(x * x + y * y);
 	}
 
 	private void updateMidPoint(Point point, MotionEvent event) {
 		float x = event.getX(0) + event.getX(1);
 		float y = event.getY(0) + event.getY(1);
 		point.set(((int) x / 2), ((int) y / 2));
 	}
 
 }
