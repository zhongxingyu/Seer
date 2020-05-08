 package cam.pdftab;
 import cam.pdftab.PdfActivity;
 
 import android.app.*;
 import android.os.*;
 import android.content.*;
 import android.content.res.*;
 import android.graphics.*;
 import android.graphics.drawable.*;
 import android.util.*;
 import android.view.*;
 import android.view.animation.*;
 import android.widget.*;
 import java.net.*;
 import java.io.*;
 import java.util.*;
 
 public class PixmapView extends SurfaceView
 	implements SurfaceHolder.Callback, cam.pdftab.GestureDetector.OnGestureListener,
 	ScaleGestureDetector.OnScaleGestureListener
 {
 	protected final static int MODE_SINGLE_PAGE = 1;
 	protected final static int MODE_CONTINUOUS = 2;
 	protected final static String TAG = "PdfTab";
 	protected final static int pageBorderSize = 10;
 
 	protected final cam.pdftab.GestureDetector gestureDetector;
 	protected final ScaleGestureDetector scaleGestureDetector;
 	private PdfActivity activity;
 	private SurfaceHolder holder;
 	private PdfThread thread = null;
 	private PdfCore doc;
 	private boolean isScaling = false;
 	private int screenWidth, screenHeight, screenFormat;
 	private int threadInitialPage = 0;
 	private int mode;
 
 	public PixmapView(PdfActivity activity, PdfCore doc)
 	{
 		super(activity);
 
 		this.doc = doc;
 		this.activity = activity;
 		this.gestureDetector = new cam.pdftab.GestureDetector(activity, this);
 		this.scaleGestureDetector = new ScaleGestureDetector(activity, this);
 
 		holder = getHolder();
 		holder.addCallback(this);
 		setFocusable(true);
 
 		pageStyleChanged();
 	}
 
 	public void setPage(int pageIndex)
 	{
 		Log.v(TAG, "setPage(" + pageIndex + ")");
 		if(thread == null)
 			threadInitialPage = pageIndex;
 		else
 			thread.setPage(pageIndex);
 	}
 
 	public int getPage()
 	{
 		return thread.getCurrentPage();
 	}
 
 	public int getNumPages()
 	{
 		return doc.numPages;
 	}
 
 	public void back()
 	{
 		thread.back();
 	}
 
 	@Override public boolean onTouchEvent(final MotionEvent event)
 	{
 		if(!activity.canAcceptPageActions())
 			return false;
 
 		if(!thread.onTouchEvent(event))
 		{
 			scaleGestureDetector.onTouchEvent(event);
 			if(scaleGestureDetector.isInProgress() || isScaling)
 				return true;
 			if(gestureDetector.onTouchEvent(event))
 				return true;
 			return super.onTouchEvent(event);
 		}
 
 		return true;
 	}
 
 	@Override public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
 	{
 		return super.onKeyUp(keyCode, event);
 	}
 
 	@Override public boolean onDown(MotionEvent e)
 	{
 		return true;
 	}
 
 	@Override public boolean onScaleBegin(ScaleGestureDetector detector)
 	{
 		if(!activity.canAcceptPageActions())
 			return false;
 
 		Log.d(TAG, "onScaleBegin()");
 		isScaling = true;
 		return true;
 	}
 
 	@Override public boolean onScale(ScaleGestureDetector detector)
 	{
 		if(!activity.canAcceptPageActions())
 			return false;
 
 		Log.d(TAG, "onScale()");
 		return true;
 	}
 
 	@Override public void onScaleEnd(ScaleGestureDetector detector)
 	{
 		if(!activity.canAcceptPageActions())
 			return;
 
 		Log.d(TAG, "onScaleEnd()");
 		isScaling = false;
 	}
 
 	@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
 	{
 		if(!activity.canAcceptPageActions())
 			return false;
 		if(isScaling)
 			return false;
 
 		thread.fling(velocityX, velocityY);
 		return true;
 	}
 
 	@Override public void onLongPress(MotionEvent e)
 	{
 		if(!activity.canAcceptPageActions())
 			return;
 		if(isScaling)
 			return;
 
 		Log.d(TAG, "onLongPress(): ignoring!");
 	}
 
 	@Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
 	{
 		if(!activity.canAcceptPageActions())
 			return false;
 		if(isScaling)
 			return false;
 
 		thread.scroll(distanceX, distanceY);
 		return true;
 	}
 
 	@Override public void onShowPress(MotionEvent e)
 	{
 		if(!activity.canAcceptPageActions())
 			return;
 		if(isScaling)
 			return;
 
 		Log.d(TAG, "onShowPress(): ignoring!");
 	}
 
 	@Override public boolean onSingleTapUp(MotionEvent e)
 	{
 		if(!activity.canAcceptPageActions())
 			return false;
 		if(isScaling)
 			return false;
 
 		thread.onTap(e.getX(), e.getY());
 		return true;
 	}
 
 	public void surfaceCreated(SurfaceHolder holder)
 	{
 		thread = new PdfThread(holder);
 		thread.start();
 	}
 
 	public void pageStyleChanged()
 	{
 		SharedPreferences settings = activity.getSharedPreferences("options", Activity.MODE_PRIVATE);
 		mode = settings.getInt("page_style", MODE_CONTINUOUS);
 
 		if(thread != null)
 			thread.screenChanged();
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
 	{
 		Log.v(TAG, String.format("surface = %dx%d, format %d", width, height, format));
 		screenWidth = width;
 		screenHeight = height;
 		screenFormat = format;
 		thread.screenChanged();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder)
 	{
 		boolean retry = true;
 
 		thread.stopRunning();
 		while(retry)
 		{
 			try
 			{
 				thread.join();
 				retry = false;
 			}
 			catch(InterruptedException e)
 			{
 			}
 		}
 
 		thread = null;
 		Log.v(TAG, "surfaceDestroyed");
 	}
 
 	class PdfThread extends Thread
 	{
 		private final int pageTurnDuration = 500;
 		private final int pageTurnSpacing = 50;
 		private final OverScroller scroller;
 		private SurfaceHolder holder;
 		private boolean running = false;
 		private boolean isWaiting = false;
 		private long pageTurnAnimationStart = -1;
 		private int pageTurnOffset;
 		private boolean pageTurnRight = false;
 		private int scrollingOffsetX = 0;
 		private java.util.Stack<Integer> stack = new java.util.Stack<Integer>();
 		private PdfPageLayout[] pages;
 		private int currentPage = -1;
 
 		public PdfThread(SurfaceHolder holder)
 		{
 			running = true;
 			this.holder = holder;
 			this.scroller = new OverScroller(activity);
 			pages = new PdfPageLayout[4];
 		}
 
 		public OverScroller getScroller() { return scroller; }
 		public int getCurrentPage() { return currentPage + 1; }
 
 		private PdfPageLayout getPage(int page)
 		{
 			if(screenWidth <= 0 && screenHeight <= 0)
 				return null;
 
 			//already have the page cached?
 			for(int i = 0; i < pages.length; i++)
 			{
 				if(pages[i] != null && pages[i].getPageNum() == page)
 					return pages[i];
 			}
 
 			//do we have a free spot for the page?
 			for(int i = 0; i < pages.length; i++)
 			{
 				if(pages[i] == null)
 				{
 					pages[i] = new PdfPageLayout(page);
 					dumpPageList();
 					return pages[i];
 				}
 			}
 
 			int chosen = 0;
 
 			//no free spots so we have to overwrite somebody
 			for(int i = 1; i < pages.length; i++)
 			{
 				int dist0 = page - pages[chosen].getPageNum();
 				int dist1 = page - pages[i].getPageNum();
 
 				if(dist0 < 0) dist0 = -dist0;
 				if(dist1 < 0) dist1 = -dist1;
 
 				//if this has a larget diff than the chosen one choose this one
 				if(dist1 > dist0)
 				{
 					chosen = i;
 				}
 				//if the diff are the same choose the one with the lower page number
 				else if(dist0 == dist1)
 				{	
 					if(pages[i].getPageNum() < pages[chosen].getPageNum())
 						chosen = i;
 				}
 			}
 
 			Log.v(TAG, String.format("no free pages, evicting %d for new page %d",
 				pages[chosen].getPageNum(), page));
 
 			//load the new page
 			pages[chosen] = new PdfPageLayout(page);
 			dumpPageList();
 			return pages[chosen];
 		}
 
 		private void dumpPageList()
 		{
 			String dump = "page list: ";
 
 			for(int i = 0; i < pages.length; i++)
 			{
 				if(i != 0)
 					dump = dump + ",";
 
 				if(pages[i] == null)
 					dump = dump + "F";
 				else
 				{
 					dump = dump + pages[i].getPageNum();
 					if(pages[i].getPageNum() == currentPage)
 						dump = dump + "*";
 				}
 			}
 
 			Log.v(TAG, dump);
 		}
 
 		private void cacheAdditionalPages()
 		{
 			if(mode == MODE_SINGLE_PAGE)
 			{
 				if(currentPage < doc.numPages)
 					getPage(currentPage + 1);
 				if(currentPage > 0)
 					getPage(currentPage - 1);
 			}
 			else
 			{
 				if(currentPage > 0)
 					getPage(currentPage - 1);
 				if(currentPage < doc.numPages)
 					getPage(currentPage + 1);
 			}
 		}
 
 		public void setPage(int page)
 		{
 			if(page != currentPage)
 			{
 				if(currentPage >= 0)
 					stack.push(currentPage);
 
 				currentPage = page;
 				getPage(currentPage);
 				updateActivityCurrentPage();
 			}
 			redraw();
 		}
 
 		public void stopRunning()
 		{
 			running = false;
 			interrupt();
 		}
 
 		public void redraw()
 		{
 			interrupt();
 		}
 
 		public void back()
 		{
 			if(stack.size() > 0)
 			{
 				//can't use set page since it adds to history
 				int newPage = (int)stack.pop();
 				currentPage = newPage;
 				getPage(currentPage);
 				updateActivityCurrentPage();
 				redraw();
 			}
 		}
 
 		private void updateActivityCurrentPage()
 		{
 			activity.showCurrentPage();
 		}
 
 		public void screenChanged()
 		{
 			for(PdfPageLayout p : pages)
 			{
 				if(p != null)
 				{
 					//make sure we set this before the setScreenSize, so the page
 					//	can take it into account
 					if(mode == MODE_CONTINUOUS)
 						p.setScreenPadding(pageBorderSize);
 
 					p.setScreenInfo(screenWidth, screenHeight, screenFormat);
 				}
 			}
 
 			redraw();
 		}
 
 		public boolean onTouchEvent(MotionEvent event)
 		{
 			switch(event.getAction() & MotionEvent.ACTION_MASK)
 			{
 				case MotionEvent.ACTION_DOWN:
 				{
 					if(!scroller.isFinished())
 					{
 						// stop the current scroll animation, but if this is
 						// the start of a fling, allow it to add to the current
 						// fling's velocity
 						scroller.abortAnimation();
 
 						//each the tap
 						return true;
 					}
 					break;
 				}
 				case MotionEvent.ACTION_UP:
 				{
 					springBack();
 					break;
 				}
 			}
 
 			return false;
 		}
 
 		private void springBack()
 		{
 			if(!isAnimating())
 			{
 				if(mode == MODE_SINGLE_PAGE)
 					springBackSinglePage();
 			}
 		}
 
 		private void springBackSinglePage()
 		{
 			PdfPageLayout cp = getPage(currentPage);
 
 			if(screenHeight >= cp.getPageHeight() && screenWidth >= cp.getPageWidth())
 				return;
 
 			scroller.springBack(
 				cp.offsetX, cp.offsetY,
 				0, 0,
 				0, cp.getPageHeight() - screenHeight);
 			redraw();
 		}
 
 		public void scroll(float distanceX, float distanceY)
 		{
 			if(!isAnimating())
 			{
 				if(mode == MODE_SINGLE_PAGE)
 					scrollSinglePage(distanceX, distanceY);
 				else
 					scrollContinuous(distanceX, distanceY);
 			}
 		}
 
 		private void scrollSinglePage(float distanceX, float distanceY)
 		{
 			PdfPageLayout cp = getPage(currentPage);
 
 			if(screenHeight >= cp.getPageHeight() && screenWidth >= cp.getPageWidth())
 				return;
 
 			synchronized(this)
 			{
          		cp.offsetY += (int)distanceY;
 			}
 			redraw();
 		}
 
 		private void scrollContinuous(float distanceX, float distanceY)
 		{
 			PdfPageLayout cp = getPage(currentPage);
 			synchronized(this)
 			{
          		cp.offsetY += (int)distanceY;
 			}
 			redraw();
 		}
 
 		public void fling(float velocityX, float velocityY)
 		{
 			//only fling if we aren't already moving
 			if(!isAnimating())
 			{
 				if(mode == MODE_SINGLE_PAGE)
 					verticalFlingSinglePage(velocityY);
 				else
 					verticalFlingContinuous(velocityY);
 			}
 		}
 
 		private void verticalFlingSinglePage(float velocityY)
 		{
 			PdfPageLayout cp = getPage(currentPage);
 
 			if(screenHeight >= cp.getPageHeight() && screenWidth >= cp.getPageWidth())
 				return;
 
 			Log.d(TAG, "verticalFlingSinglePage(" + velocityY + ")");
 			scroller.fling(
 				cp.offsetX, cp.offsetY,
 				0, -(int)(velocityY * 1.25),
 				0, 0,
 				0, cp.getPageHeight() - screenHeight,
 				0, 50);
 			redraw();
 		}
 
 		private int flingContinuousPrevious = 0;
 		private void verticalFlingContinuous(float velocityY)
 		{
 			PdfPageLayout cp = getPage(currentPage);
 
 			Log.d(TAG, "verticalFlingContinuous(" + velocityY + ")");
 			flingContinuousPrevious = 0;
 			scroller.fling(
 				0, 0,
 				0, -(int)(velocityY * 1.25),
 				0, 0,
 				-32768, 32768,
 				0, 50);
 			redraw();
 		}
 
 		private void startPageTurnAnimation()
 		{
 			if(!isAnimating())
 			{
 				pageTurnAnimationStart = AnimationUtils.currentAnimationTimeMillis();
 				redraw();
 			}
 		}
 
 		private boolean isAnimating()
 		{
 			return scroller.computeScrollOffset() || (pageTurnAnimationStart >= 0);
 		}
 
 		public void onTap(float x, float y)
 		{
 			if(!isAnimating())
 			{
 				if(mode == MODE_SINGLE_PAGE)
 					onTapSinglePage(x, y);
 				else
 					onTapContinuous(x, y);
 			}
 		}
 
 		private void onTapSinglePage(float x, float y)
 		{
 			PdfPageLayout cp = getPage(currentPage);
 
 			int pageLink = cp.findLink((int)x, (int)y);
 			if(pageLink >= 0)
 			{
 				setPage(pageLink);
 			}
 			else if(x >= screenWidth - screenWidth / 4 && cp.getPageNum() < doc.numPages)
 			{
 				startPageTurnAnimation();
 				pageTurnRight = true;
 			}
 			else if(x <= screenWidth / 4 && cp.getPageNum() > 0)
 			{
 				startPageTurnAnimation();
 				pageTurnRight = false;
 			}
 			else if(x >= screenWidth / 4 && x <= screenWidth * 3 / 4 &&
 				y >= screenHeight / 4 && y <= screenHeight * 3 / 4)
 			{
 				activity.clickInControlRegion(); 
 			}
 		}
 
 		private void onTapContinuous(float x, float y)
 		{
 			activity.clickInControlRegion(); 
 		}
 
 		public void run()
 		{
 			setPage(threadInitialPage);
 
 			while(running)
 			{
 				boolean doSleep = true;
 				Canvas c = null;
 
 				//long begin = System.currentTimeMillis();
 				try
 				{
 					c = holder.lockCanvas(null);
 					c.drawRGB(0x9C, 0x9C, 0x9C);
 
 					if(pageTurnAnimationStart > 0)
 					{
 						doSleep = false;
 
 						final long time = AnimationUtils.currentAnimationTimeMillis();
 						final long animDiff = time - pageTurnAnimationStart;
 
 						if(animDiff >= pageTurnDuration)
 						{
 							scrollingOffsetX = 0;
 							pageTurnAnimationStart = -1;
 
 							if(pageTurnRight)
 								setPage(currentPage + 1);
 							else
 								setPage(currentPage - 1);
 						}
 						else
 						{
 							pageTurnOffset = (int)(((screenWidth + pageTurnSpacing) *
 								animDiff) / pageTurnDuration);
 						}
 					}
 					else
 					{
 						if(!scroller.isFinished())
 						{
 							doSleep = false;
 
 							if(scroller.computeScrollOffset())
 							{
 								scrollingOffsetX = scroller.getCurrX();
 								if(mode == MODE_SINGLE_PAGE)
 									scrollerSinglePage();
 								else
 									scrollerContinuous();
 							}
 							else
 							{
 								scroller.abortAnimation();
 							}
 						}
 					}
 
 					if(mode == MODE_SINGLE_PAGE)
 						drawSinglePage(c);
 					else
 						drawContinuousPage(c);
 				}
 				finally
 				{
 					if(c != null)
 						holder.unlockCanvasAndPost(c);
 				}
 
 				//long duration = System.currentTimeMillis() - begin;
 				//Log.v(TAG, String.format("frame : %d ms", duration));
 
 				if(doSleep)
 				{
 					cacheAdditionalPages();
 
 					try { sleep(3600000); }
 					catch(Exception e) { }
 				}
 			}
 		}
 
 		private void scrollerSinglePage()
 		{
 			PdfPageLayout cp = getPage(currentPage);
 			cp.offsetY = scroller.getCurrY();
 		}
 
 		private void scrollerContinuous()
 		{
 			PdfPageLayout cp = getPage(currentPage);
 
 			int deltaY = scroller.getCurrY() - flingContinuousPrevious;
 			flingContinuousPrevious = scroller.getCurrY();
 
 			cp.offsetY += deltaY;
 		}
 
 		protected void drawSinglePage(Canvas c)
 		{
 			PdfPageLayout cp = getPage(currentPage);
 			if(cp == null) return;
 
 			if(pageTurnAnimationStart > 0)
 			{
 				if(pageTurnRight)
 				{
 					PdfPageLayout np = getPage(currentPage + 1);
 					cp.blit(c, -pageTurnOffset);
 					np.blit(c, screenWidth - pageTurnOffset + pageTurnSpacing);
 				}
 				else
 				{
 					PdfPageLayout pp = getPage(currentPage - 1);
 
 					cp.blit(c, pageTurnOffset);
 					pp.blit(c, -screenWidth - pageTurnSpacing + pageTurnOffset);
 				}
 			}
 			else if(scrollingOffsetX != 0)
 			{
 				/*if(scrollingOffsetX > 0)
 				{
 					currentPage.blit(c, -scrollingOffsetX);
 					nextPage.blit(c, screenWidth - scrollingOffsetX + pageTurnSpacing);
 				}
 				else
 				{
 					currentPage.blit(c, -scrollingOffsetX);
 					prevPage.blit(c, -screenWidth - pageTurnSpacing - scrollingOffsetX);
 				}
 
 				if(Math.abs(scrollingOffsetX) > screenWidth / 5)
 				{
 					scroller.abortAnimation();
 
 					pageTurnRight = scrollingOffsetX > 0;
 					startPageTurnAnimation();
 					scrollingOffsetX = 0;
 				}*/
 			}
 			else
 			{
 				cp.blit(c, 0);
 			}
 		}
 
 		protected void drawContinuousPage(Canvas c)
 		{
 			Paint paintLine = new Paint();
 			paintLine.setStrokeWidth(3);
 			paintLine.setColor(0xFFAAAAAA);
 
 			int cpOffsetY;
 			PdfPageLayout cp = getPage(currentPage);
 
 			//we need to sync while we are reading and changing cp.offsetY
 			synchronized(this)
 			{
 				//negative cp offset means that the previous page is really the current page
 				if(cp.offsetY < 0)
 				{
 					int oldCurrentPageOffsetY = cp.offsetY;
 
 					//change the page
 					setPage(currentPage - 1);
 
 					//get the new page as the current page
 					cp = getPage(currentPage);
 	
 					//the new page's offset is how much of the previous page is not shown
 					cp.offsetY = cp.getPageHeight() + oldCurrentPageOffsetY;
 				}
 
 				//we know we have the correct current page with a positive offsetY now, so
 				//	if we are over the offset for the current page height, then the current page
 				//	is no longer visible
 				if(cp.offsetY >= cp.getPageHeight())
 				{
 					//the new page's offset is how much we went over on the current page
 					int newPageOffset = cp.offsetY - cp.getPageHeight();
 
 					//change the page
 					setPage(currentPage + 1);
 
 					//get the new page as the current page
 					cp = getPage(currentPage);
 					cp.offsetY = newPageOffset;
 				}
 
 				//get a local copy
 				cpOffsetY = cp.offsetY;
 			}
 
 			//draw the "current page" (using our saved offset Y)
 			cp.blit(c, cp.offsetX, cpOffsetY);
 
 			//how much of the screen have we used with the current page?
 			int screenHeightUsed = -cpOffsetY + cp.getPageHeight();
 
 			//do we see any next pages?
 			if(screenHeightUsed < screenHeight)
 			{
 				//adjust the "next" pages
 				for(int i = 1; i < 10; i++)
 				{
 					PdfPageLayout np = getPage(currentPage + i);
 					np.offsetY = -screenHeightUsed;
 					np.blit(c, 0);
 					//c.drawLine(0, -np.offsetY - 1, getWidth(), -np.offsetY - 1, paintLine);
 
 					screenHeightUsed += np.getPageHeight();
 					if(screenHeightUsed >= screenHeight)
 						break;
 				}
 			}
 		}
 
 		private class PdfPageLayout extends PdfPage
 		{
 			public int offsetX, offsetY;
 
 			public PdfPageLayout(int pageNum)
 			{
 				super(activity, doc, pageNum);
 
 				//make sure we set this before the setScreenSize, so the page
 				//	can take it into account
 				if(mode == MODE_CONTINUOUS)
 					setScreenPadding(pageBorderSize);
 
 				setScreenInfo(screenWidth, screenHeight, screenFormat);
 
 				//set the initial offset to the border size
 				offsetY = -pageBorderSize;
 			}
 
 			public void blit(Canvas c, int x, int y)
 			{	
 				super.blit(c, x, -y);
 			}
 
 			public void blit(Canvas c, int startX)
 			{
 				super.blit(c, offsetX + startX, -offsetY);
 			}
 		}
 	}
 }
