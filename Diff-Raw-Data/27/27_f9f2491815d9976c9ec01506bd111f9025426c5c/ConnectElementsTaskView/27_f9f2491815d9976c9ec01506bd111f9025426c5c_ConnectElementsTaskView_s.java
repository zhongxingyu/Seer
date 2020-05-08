 package app.tascact.manual.task;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.view.MotionEvent;
 import app.tascact.manual.CResources;
 import app.tascact.manual.view.TaskView;
 
 public class ConnectElementsTaskView extends TaskView
 {
 	// размеры экрана
 	private int mWidth = 0;
 	private int mHeight = 0;
 	
 	private CTask mTask = null;
 	private Paint mPaint = null;
 	private Canvas mCanvas = null;
 	private Bitmap mBitmap = null;
 	private Bitmap mPrevBitmap = null;
 	private PointF mPrevTouchPoint;
 	private AlertDialog mAlertDialog = null;
 	private int[] mTaskResources = null;
 	private int[][] mTaskAnswers = null;
 	private Answer[] mAnswers = null;
 	private boolean mAnswer = true;
 	private boolean isSetLine = false;
 	private final CResources mResources = new CResources();
 	private int mTouchedImageId = 0;
 	
     public ConnectElementsTaskView(Context context, int PageNumber, int TaskNumber)
     {
 		super(context, PageNumber, TaskNumber);
 		mPrevTouchPoint = new PointF();
 		mAlertDialog = new AlertDialog.Builder(context).create();
 		mTaskResources = mResources.GetTaskResources(PageNumber, TaskNumber);
 		mTaskAnswers = mResources.GetTaskAnswer(PageNumber, TaskNumber);
 		
 		mAnswers = new Answer[mTaskAnswers.length];
 		for(int i = 0; i < mTaskAnswers.length; ++i)
 		{
 			mAnswers[i] = new Answer(mTaskAnswers[i][0], mTaskAnswers[i][1]);
 		}
 		
 		mTask = new CTask(context, mTaskResources);
 
 		mPaint = new Paint(Paint.DITHER_FLAG);
 		mPaint.setAntiAlias(true);
 		mPaint.setARGB(255, 100, 25, 55);
 		setBackgroundColor(Color.WHITE);
 	}
     
     // получение размеров экрана
     @Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
 	{
         super.onSizeChanged(w, h, oldw, oldh);
         mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         mWidth = w;
         mHeight = h;
         mCanvas = new Canvas(mBitmap);
     }
     
     // отрисовка страницы
     @Override protected void onDraw(Canvas canvas) 
 	{
     	Point[] set = mTask.getSetCoord(mWidth, mHeight);
     	
     	canvas.drawBitmap(mBitmap, 0, 0, mPaint);
     	
     	for(int i = 0; i < set.length; ++i)
     	{
     		canvas.drawBitmap(mTask.mTaskResBitmaps[i], set[i].x, set[i].y, mPaint);
     	}
 	}
     
     @Override public boolean onTouchEvent(MotionEvent event)
 	{
 		// получаем action
 	    int eventAction = event.getAction(); 
 	    
 	    // получаем координаты прикасания
 	    float X = event.getX(); 
 	    float Y = event.getY(); 
 	    		
 	    switch (eventAction)
 	    { 
 	    	// action - нажали на экран
 		    case MotionEvent.ACTION_DOWN:
 		    {
 		    	mTouchedImageId = mTask.getTouchedImgId((int)X, (int)Y);
 		    	if (mTouchedImageId > 0)
 	    		{	   			
 		    		isSetLine = true;
 		    		mPrevTouchPoint.set(X, Y);
 	    			mPrevBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
 	    		}
 	    		else
 	    		{
 	    			isSetLine = false;
 	    		}
 	    		
 	    		invalidate(); 
 	    		break; 
 		    }
 		    
 		    // action - ведем палец по экрану
 		    case MotionEvent.ACTION_MOVE:
 		    {
 		    	if(!isSetLine)
 		    	{
 		    		invalidate(); 
 		    		break;
 		    	}
 		    	
 		    	mCanvas.drawCircle(X, Y, 4, mPaint);
 		    	DrawCurveLine(mPrevTouchPoint.x, mPrevTouchPoint.y, X, Y, 10);
 		    	mPrevTouchPoint.set(X, Y);
 		    	
 		    	invalidate(); 
 		        break; 
 		    }
 		    
 		    // action - убрали палец
 		    case MotionEvent.ACTION_UP:
 		    {
 		    	if(!isSetLine)
 		    	{
 		    		invalidate();
 		    		break;
 		    	}
 		    	
 		    	
 	    		if (mTask.getTouchedImgId((int)X, (int)Y) > 0)
 	    		{
 	    			mCanvas.drawCircle(X, Y, 5, mPaint);
 	    			DrawCurveLine(mPrevTouchPoint.x, mPrevTouchPoint.y, X, Y, 10);
 	    			
	    			for(int i = 0; i < mTaskAnswers.length; ++i)
 	    			{
	    				if(mAnswers[i].setAnswer(mTouchedImageId, mTask.getTouchedImgId((int)X, (int)Y)))
	    				{
	    					mAnswer = true;
	    					break;
	    				}
	    				mAnswer = false;
	    			}		
 	    		}
 	    		else
 	    		{
 	    			mBitmap = mPrevBitmap;
 	    			mCanvas = new Canvas(mBitmap);
 	    		}
 	    		
 	    		invalidate(); 
 		    	break;
 		    }
 	    } 
     
 	    return true;
 	}
     
     public void RestartTask()
     {
     	mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
 		mCanvas = new Canvas(mBitmap);
 		invalidate();
     }
     
     public void CheckTask()
     {
     	boolean result = true;
     	for(int i = 0; i < mTaskAnswers.length; ++i)
     	{
     		if(!mAnswers[i].getResult())
     		{
     			result = false;
     			break;
     		}
     	}
     	mAlertDialog.setMessage(Boolean.toString(result && mAnswer)) ;
 		mAlertDialog.show();
     }
     
     private void DrawCurveLine(float firstX, float firstY, float secondX, float secondY, float lineWidth)
 	{
 		// запоминаем предыдущую ширину линии
 		float lPrevWidth = mPaint.getStrokeWidth();
 		mPaint.setStrokeWidth(lineWidth);
 		mCanvas.drawLine(firstX, firstY, secondX, secondY, mPaint);
 		mPaint.setStrokeWidth(lPrevWidth);		
 	}
     
     private class Answer
     {
     	private int mFirstId = -1;
     	private int mSecondId = -1;
     	private boolean mResult = false;
     	
     	public Answer(int firstId, int secondId)
     	{
     		mFirstId = firstId;
     		mSecondId = secondId;
     	}
     	
     	public boolean setAnswer(int firstId, int secondId)
     	{
     		if((firstId == mFirstId && secondId == mSecondId) || 
     		   (firstId == mSecondId && secondId == mFirstId))
     		{
     			mResult = true;
     			return true;
     		}
     		return false;
     	}
     	
     	public boolean getResult()
     	{
     		return mResult;
     	}
     }
     
     private class CTask
     {
     	public Bitmap mTaskResBitmaps[] = null;
     	public CTaskResourcesCoordinateSet mTaskSet[] = null;
     	
     	private Point mSetCoord[];
     	
     	public CTask(Context context, int TaskResId[])
     	{    		
     		mTaskResBitmaps = new Bitmap[TaskResId.length];
     		mTaskSet = new CTaskResourcesCoordinateSet[TaskResId.length];
     		
     		mSetCoord = new Point[TaskResId.length];
     		
     		for(int i = 0; i < TaskResId.length; ++i)
     		{
     			mSetCoord[i] = new Point();
     		}
     		
     		// получаем массив размещения элементов
     		for(int i = 0; i < TaskResId.length; ++i)
     		{
     			mTaskResBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), TaskResId[i]);
     			CTaskResourcesCoordinateSet elem = new CTaskResourcesCoordinateSet(TaskResId[i], mTaskResBitmaps[i].getWidth(), mTaskResBitmaps[i].getHeight());
     			mTaskSet[i] = elem;
     		}
     	}
     	
     	public Point[] getSetCoord(int width, int height)
     	{		
     		//int avgW = width / mSetCoord.length;
     		int avgH = (int)(height * 1.5 / mSetCoord.length);
     		
     		for(int i = 0; i < mSetCoord.length; ++i)
     		{	
     			int picWidth = mTaskSet[i].width;
     			int picHeight = mTaskSet[i].height;
     			
     			// волшебная формула по вставке картинки посередине блока
     			
     			/*
     			 *  	 _________________
     			 *  	 |
     			 *       |
     			 * avgH  |
     			 *   	 |
     			 *  	 |
     			 *  	 |________________
     			 * 		 |
     			 * 		 |
     			 * avgH  |
     			 * 		 |
     			 * 	 	 ..................
     			 * 				________
     			 * 				|
     			 * picHeight 	|
     			 * 				|_______
     			 */
     			mSetCoord[i].y = avgH * (i + 1) / 2 - picHeight / 2;
     			mSetCoord[i].x = (i % 2) == 0 ? 40 : width - picWidth - 40;
     			
     			mTaskSet[i].top = mSetCoord[i].y;
     			mTaskSet[i].left = mSetCoord[i].x;
     		}
     		return mSetCoord;
     	}
     	
     	public int getTouchedImgId(int X, int Y)
     	{
     		for(int i = 0; i < mTaskSet.length; ++i)
     		{
     			if(mTaskSet[i].isInto(X, Y))
     			{
     				return mTaskSet[i].resId;
     			}
     		}		
     		return 0;
     	}
     	
     	private class CTaskResourcesCoordinateSet
         {
         	public int resId;
         	public int height;
         	public int width;
         	public int left;
         	public int top;
         	
         	CTaskResourcesCoordinateSet(int resourceId, int w, int h)
         	{
         		resId = resourceId;
         		height = h;
         		width = w;
         	}
         	
         	public boolean isInto(int X, int Y)
         	{
         		return ((X > left) && (X < left + width) && 
         				(Y > top) && (Y < top + height));
         	}
         	
         }
     }  
 }
