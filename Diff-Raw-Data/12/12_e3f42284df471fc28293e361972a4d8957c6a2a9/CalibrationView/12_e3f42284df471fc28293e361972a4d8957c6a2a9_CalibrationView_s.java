 package edu.tamu.csce470.mir;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.net.Uri;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class CalibrationView extends View
 {
 	private float sampleRowY;
 	private float leftBoundaryX;
 	private float rightBoundaryX;
 	private float leftWavelengthX;
 	private float rightWavelengthX;
 	
 	private Paint samplePaint;
 	private Paint boundaryPaint;
 	private Paint wavelengthPaint;
 	
 	private Uri imageUri;
 	private Bitmap imageBitmap;
 	
 	private int height;
 	private int width;
 	
 	public enum CalibrationState
 	{
 		SampleRow,
 		LeftBoundary,
 		RightBoundary,
 		LeftWavelength,
 		RightWavelength
 	}
 	
 	public CalibrationState state;
 	
 	public CalibrationView(Context context, AttributeSet attrs)
 	{
 		super(context, attrs);
 		
 		sampleRowY = -1;
 		leftBoundaryX = -1;
 		rightBoundaryX = -1;
 		leftWavelengthX = -1;
 		rightWavelengthX = -1;
 		
 		samplePaint = new Paint();
 		samplePaint.setColor(Color.RED);
 		
 		boundaryPaint = new Paint();
 		boundaryPaint.setColor(Color.GREEN);
 		
 		wavelengthPaint = new Paint();
 		wavelengthPaint.setColor(Color.CYAN);
 		
 		imageUri = null;
 		imageBitmap = null;
 		
 		state = CalibrationState.SampleRow;
 	}
 	
 	public void setImage(Uri uri)
 	{
 		imageUri = uri;
 	}
 	
 	public void releaseImage()
 	{
 		if (imageBitmap != null)
 		{
 			imageBitmap.recycle();
 			imageBitmap = null;
 		}
 	}
 	
 	public int getImageWidth()
 	{
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(imageUri.getPath(), options);
 		
 		return options.outWidth;
 	}
 	
 	public int getImageHeight()
 	{
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(imageUri.getPath(), options);
 		
 		return options.outHeight;
 	}
 	
 	public int getSampleRow()
 	{
 		int imageHeight = getImageHeight();
 		
 		return Math.round((imageHeight - 1) * (sampleRowY / height));
 	}
 	
 	public int getLeftBoundaryPixel()
 	{
 		int imageWidth = getImageWidth();
 		
 		return Math.round((imageWidth - 1) * (leftBoundaryX / width));
 	}
 	
 	public int getRightBoundaryPixel()
 	{
 		int imageWidth = getImageWidth();
 		
 		return Math.round((imageWidth - 1) * (rightBoundaryX / width));
 	}
 	
 	public int getLeftWavelengthPixel()
 	{
 		int imageWidth = getImageWidth();
 		
 		return Math.round((imageWidth - 1) * (leftWavelengthX / width));
 	}
 	
 	public int getRightWavelengthPixel()
 	{
 		int imageWidth = getImageWidth();
 		
 		return Math.round((imageWidth - 1) * (rightWavelengthX / width));
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh)
 	{
 		super.onSizeChanged(w, h, oldw, oldh);
 		
 		height = h;
 		width = w;
 		
 		assert(imageUri != null);
 		
 		if (imageBitmap != null)
 		{
 			imageBitmap.recycle();
 			imageBitmap = null;
 		}
 		
 		try
 		{
 			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
 			Bitmap fullImage = BitmapFactory.decodeFile(imageUri.getPath(), options);
 			imageBitmap = Bitmap.createScaledBitmap(fullImage, w, h, false);
 			fullImage.recycle();
 		}
 		catch (Exception e)
 		{
 			assert(false);
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		
 		int width = canvas.getWidth();
 		int height = canvas.getHeight();
 		
 		if (imageBitmap != null)
 		{
 			canvas.drawBitmap(imageBitmap, 0, 0, null);
 		}
 		
 		if (sampleRowY >= 0)
 		{
 			canvas.drawLine(0, sampleRowY, width, sampleRowY, samplePaint);
 		}
 		
 		if (leftBoundaryX >= 0)
 		{
 			canvas.drawLine(leftBoundaryX, 0, leftBoundaryX, height, boundaryPaint);
 		}
 		
 		if (rightBoundaryX >= 0)
 		{
 			canvas.drawLine(rightBoundaryX, 0, rightBoundaryX, height, boundaryPaint);
 		}
 		
 		if (leftWavelengthX >= 0)
 		{
 			canvas.drawLine(leftWavelengthX, 0, leftWavelengthX, height, wavelengthPaint);
 		}
 		
 		if (rightWavelengthX >= 0)
 		{
 			canvas.drawLine(rightWavelengthX, 0, rightWavelengthX, height, wavelengthPaint);
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		float x = event.getX();
 		float y = event.getY();
 		
 		switch (state)
 		{
 		case SampleRow:
 			sampleRowY = y;
 			break;
 		case LeftBoundary:
 			leftBoundaryX = x;
 			break;
 		case RightBoundary:
 			rightBoundaryX = x;
 			break;
 		case LeftWavelength:
 			leftWavelengthX = x;
 			break;
 		case RightWavelength:
 			rightWavelengthX = x;
 			break;
 		default:
 			assert(false);
 		}
 		
 		invalidate();
 		
 		return true;
 	}
 }
