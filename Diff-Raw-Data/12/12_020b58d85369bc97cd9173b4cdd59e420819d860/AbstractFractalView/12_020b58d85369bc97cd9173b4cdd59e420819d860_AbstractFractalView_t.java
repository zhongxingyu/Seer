 package uk.ac.ed.inf.mandelbrotmaps;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import uk.ac.ed.inf.mandelbrotmaps.RenderThread.FractalSection;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.View;
 
 abstract class AbstractFractalView extends View {
    
 	private final String TAG = "MMaps";
 	
 	public enum RenderMode{
 		NEW,
 		JUST_DRAGGED,
 		JUST_ZOOMED
 	}
 	
 	private RenderMode renderMode = RenderMode.NEW;
 	
 	
 	public enum ControlMode {
 		ZOOMING,
 		DRAGGING,
 		STATIC
 	}
 	
 	private ControlMode controlmode = ControlMode.STATIC;
 	
 	
 	public enum RenderStyle{
 		SINGLE_THREAD,
 		DUAL_THREAD
 	}
 	
 	private RenderStyle renderStyle;
 	
 	public int LINES_TO_DRAW_AFTER = 20;
 	
    	// How many different, discrete zoom and contrast levels?
 	public final int ZOOM_SLIDER_SCALING = 300;
 	public final int CONTRAST_SLIDER_SCALING = 200;
    
 	// Default "crude rendering" pixel block size?
 	int INITIAL_PIXEL_BLOCK = 3;
    
 	//Default pixel size
 	int DEFAULT_PIXEL_SIZE = 1;
    
    	// How much of a zoom, on each increment?
 	public final int zoomPercent = 1;
    
 	protected String viewName;
 	
 	//Upper and lower half rendering queues/threads
 	LinkedBlockingQueue<Rendering> upperRenderQueue = new LinkedBlockingQueue<Rendering>();	
 	RenderThread upperRenderThread = new RenderThread(this, FractalSection.UPPER);
 	
 	LinkedBlockingQueue<Rendering> lowerRenderQueue = new LinkedBlockingQueue<Rendering>();	
 	RenderThread lowerRenderThread = new RenderThread(this, FractalSection.LOWER);
 	
 	
 	// What zoom range do we allow? Expressed as ln(pixelSize).
 	double MINZOOM_LN_PIXEL = -3;
 	double MAXZOOM_LN_PIXEL;
 	
 	// How many iterations, at the very fewest, will we do?
 	int MIN_ITERATIONS = 10;
 	
 	// Constants for calculating maxIterations()
 	double ITERATION_BASE;
 	double ITERATION_CONSTANT_FACTOR;
 	
 	// Scaling factor for maxIterations() calculations
 	double iterationScaling = 0.3;
 	double ITERATIONSCALING_DEFAULT = 0.3;
 	double ITERATIONSCALING_MIN = 0.01;
 	double ITERATIONSCALING_MAX = 100;
 	
 	// Mouse dragging state.
 	int dragLastX = 0;
 	int dragLastY = 0;
 	
 	// Graph Area on the Complex Plane? new double[] {x_min, y_max, width}
 	double[] graphArea;
 	double[] homeGraphArea;
 	
 	// Fractal image data
 	int[] fractalPixels;
 	int[] pixelSizes;
 	Bitmap fractalBitmap;
 	Bitmap movingBitmap;
 	
 	// Where to draw the image onscreen
 	public float bitmapX = 0;
 	public float bitmapY = 0;	
 	
 	private float totalDragX = 0;
 	private float totalDragY = 0;
 	
 	public float scaleFactor = 1.0f;
 	public float midX = 0.0f;
 	public float midY = 0.0f;
 	
 	boolean pauseRendering = false;
 	boolean zoomingFractal = false;
 	boolean hasZoomed = false;
 	
 	private Matrix matrix;
 	
 	boolean crudeRendering = true;//false;
 	
 	int bitmapCreations = 0;
 	
 /*-----------------------------------------------------------------------------------*/
 /*Constructor*/
 /*-----------------------------------------------------------------------------------*/
 	public AbstractFractalView(Context context, RenderStyle style) {
 		super(context);
 		setFocusable(true);
 		setFocusableInTouchMode(true);
 		setBackgroundColor(Color.BLACK);
       	setId(0); 
       	
       	renderStyle = style;
       
       	setOnTouchListener((FractalActivity)context);
       
       	matrix = new Matrix();
       	matrix.reset();
       	
       	upperRenderThread.start();
       	if(renderStyle != RenderStyle.SINGLE_THREAD)
       		{
       			lowerRenderThread.start();
       		}
    }
 
 	
 	
 /*-----------------------------------------------------------------------------------*/
 /*Android life-cycle handling*/   
 /*-----------------------------------------------------------------------------------*/
 	
    @Override
    protected Parcelable onSaveInstanceState() { 
 	   super.onSaveInstanceState();
 	   Log.d(TAG, "onSaveInstanceState");
 	   Bundle bundle = new Bundle();
 	   return bundle;
    }
     
    @Override
    protected void onRestoreInstanceState(Parcelable state) { 
 	   Log.d(TAG, "onRestoreInstanceState");
 	   super.onRestoreInstanceState(state);
    }
      
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 	   super.onSizeChanged(w, h, oldw, oldh);
 	   
 	   LINES_TO_DRAW_AFTER = getHeight()/12;
 	   Log.d(TAG, "Drawing every " + LINES_TO_DRAW_AFTER + " lines.");
    }
    
    
    
 /*-----------------------------------------------------------------------------------*/
 /* Graphics */
 /*-----------------------------------------------------------------------------------*/
    
    // What to draw on the screen
    @Override
    protected void onDraw(Canvas canvas) {	   
 	// (Re)create pixel grid, if not initialised - or if wrong size.
 	if ((fractalPixels == null) || (fractalPixels.length != getWidth()*getHeight())) {
 		fractalPixels = new int[getWidth() * getHeight()];
 		pixelSizes = new int[getWidth() * getHeight()];
 		clearPixelSizes();
 		scheduleNewRenders();
 	}
 	
 	//Translation
 	if (controlmode == ControlMode.DRAGGING)
 	{
 		matrix.postTranslate(bitmapX, bitmapY);
 		bitmapX = 0;
 		bitmapY = 0;
 	}
 	
 	//Scaling
 	matrix.postScale(scaleFactor, scaleFactor, midX, midY);
 	scaleFactor = 1.0f;
 	midX = 0.0f;
 	midY = 0.0f;
 	
 	//Create new image only if not dragging or zooming
 	if(controlmode == ControlMode.STATIC) 
 		{
 			bitmapCreations++;
 			Log.d(TAG, "Create a new bitmap! " + bitmapCreations);
 			fractalBitmap = Bitmap.createBitmap(fractalPixels, 0, getWidth(), getWidth(), getHeight(), Bitmap.Config.RGB_565);
 		}
 	
 	//Draw image on screen
 	canvas.drawBitmap(fractalBitmap, matrix, new Paint());
    }
 	
    
    // Adds renders to the queue for processing by render thread
 	void scheduleNewRenders() {		
 		Log.d(TAG, "scheduleNewRenders");
 		
 		//Abort future rendering queue.
 		stopAllRendering();
 		
 		//Schedule a crude rendering
 		if(crudeRendering)
 			scheduleRendering(INITIAL_PIXEL_BLOCK);
 		
 		// Schedule a high-quality rendering
 		scheduleRendering(DEFAULT_PIXEL_SIZE);
 	}
 	
 	
 	// Computes all necessary pixels (run by render thread)
 	public void computeAllPixels(final int pixelBlockSize, FractalSection half) {
 		// Nothing to do - stop if called before layout has been sanely set...
 		if (getWidth() <= 0 || graphArea == null || pauseRendering)
 			return;
 		
 		int yStart = 0;
 		int yEnd = getHeight();
 		boolean showRenderProgress = true;
 		
 		
 		if(renderStyle != RenderStyle.SINGLE_THREAD){
 			if (half == FractalSection.UPPER) {
 				yStart = 0;
 				yEnd = getHeight() - 1;
 			}
 			else if (half == FractalSection.LOWER) {
 				yStart = pixelBlockSize;
 				yEnd = getHeight();
 				showRenderProgress = false;
 			}
 		}
 		else
 			half = FractalSection.ALL;
 			
 		if (pixelSizes == null)
 			pixelSizes = new int[getWidth() * getHeight()];
 		
 		computePixels(
 			fractalPixels,
			new int[getWidth()*getHeight()],//pixelSizes,
 			pixelBlockSize,
 			showRenderProgress,
 			0, 
 			getWidth(),
 			yStart,
 			yEnd,
 			graphArea[0],
 			graphArea[1],
 			getPixelSize(),
 			true,
 			renderMode,
 			half
 		);
 		
 		postInvalidate();
 	}
 	
 	
 	
 /*-----------------------------------------------------------------------------------*/
 /* Movement */
 /*-----------------------------------------------------------------------------------*/
 		
 		// Set new graph area
 		public void moveFractal(int dragDiffPixelsX, int dragDiffPixelsY) {
 			Log.d(TAG, "moveFractal()");
 			
 			// What does each pixel correspond to, on the complex plane?
 			double pixelSize = getPixelSize();
 			
 			// Adjust the Graph Area
 			double[] newGraphArea = getGraphArea();
 			newGraphArea[0] -= (dragDiffPixelsX * pixelSize);
 			newGraphArea[1] -= -(dragDiffPixelsY * pixelSize);
 			setGraphArea(newGraphArea, false);
 		}
 		
 		
 		// Begin translating the image relative to the users finger
 		public void startDragging()
 		{			
 			controlmode = ControlMode.DRAGGING;
 			
 			//Stop current rendering (to not render areas that are offscreen afterwards)
 			stopAllRendering();
 			
 			//Clear translation variables
 			bitmapX = 0;
 			bitmapY = 0;
 			totalDragX = 0;
 			totalDragY = 0;
 			
 			hasZoomed = false;
 		}
 		
 		
 		// Update the position of the image on screen as finger moves
 		public void dragFractal(float dragDiffPixelsX, float dragDiffPixelsY) {		
 			bitmapX = dragDiffPixelsX;
 			bitmapY = dragDiffPixelsY;
 			
 			totalDragX += dragDiffPixelsX;
 			totalDragY += dragDiffPixelsY;
 			
 			invalidate();
 		}
 		
 		
 		// Stop moving the image around, calculate new area. Run when finger lifted.
 		public void stopDragging(boolean stoppedOnZoom)
 		{		
 			controlmode = ControlMode.STATIC;
 			
 			Log.d(TAG, "Stopped on zoom: " + stoppedOnZoom);
 			
 			// If no zooming's occured, keep the remaining pixels
 			if(!hasZoomed && !stoppedOnZoom) 
 			{
 				renderMode = RenderMode.JUST_DRAGGED;
 				shiftPixels((int)totalDragX, (int)totalDragY);
 			}
 			else 
 				renderMode = RenderMode.NEW;
 			
 			//Set the new location for the fractals
 			moveFractal((int)totalDragX, (int)totalDragY);
 			
 			if(!stoppedOnZoom) scheduleNewRenders();
 			
 			// Reset all the variables (possibly paranoid)
 			if(!hasZoomed && !stoppedOnZoom) matrix.reset();
 			
 			
 			hasZoomed = false;
 			
 			invalidate();
 		}
 
 		
 		// Take the current pixel value array and adjust it to keep pixels that have already been calculated
 		public void shiftPixels(int shiftX, int shiftY)
 		{
 			Log.d(TAG, "Shifting pixels");
 			
 			int height = getHeight();
 			int width = getWidth();
 			int[] newPixels = new int[height * width];
 			int[] newSizes = new int[height * width];
 			for (int i = 0; i < newSizes.length; i++) newSizes[i] = 1000;
 			
 			//Choose rows to copy from
 			int rowNum = height - Math.abs(shiftY);
 			int origStartRow = (shiftY < 0 ? Math.abs(shiftY) : 0);
 			
 			//Choose columns to copy from
 			int colNum = width - Math.abs(shiftX);
 			int origStartCol = (shiftX < 0 ? Math.abs(shiftX) : 0);
 			
 			//Choose columns to copy to
 			int destStartCol = (shiftX < 0 ? 0 : shiftX);
 			
 			//Copy useful parts into new array
 			for (int origY = origStartRow; origY < origStartRow + rowNum; origY++)
 			{
 				int destY = origY + shiftY;
 				System.arraycopy(fractalPixels, (origY * width) + origStartCol, 
 								 newPixels, (destY * width) + destStartCol,
 								 colNum);
 				System.arraycopy(pixelSizes, (origY * width) + origStartCol, 
 						 newSizes, (destY * width) + destStartCol,
 						 colNum);
 			}
 			
 			//Set values
 			fractalPixels = newPixels;
 			pixelSizes = newSizes;
 		}
 		
 		
 		
 /*-----------------------------------------------------------------------------------*/
 /* Zooming */	
 /*-----------------------------------------------------------------------------------*/
 		
 	// Adjust zoom, centred on pixel (xPixel, yPixel)
 	public void zoomChange(int xPixel, int yPixel, float scale) { //int zoomAmount) {
 		renderMode = RenderMode.JUST_ZOOMED;
 		stopAllRendering();
 		
 		double pixelSize = getPixelSize();
 		
 		double[] oldGraphArea = getGraphArea();
 		double[] newGraphArea = new double[3];
 		
 		double zoomPercentChange = (double)scale; //= (double)(100 + (zoomAmount)) / 100;
 		
 		// What is the zoom centre?
 		double mousedOverX = oldGraphArea[0] + ( (double)xPixel * pixelSize );
 		double mousedOverY = oldGraphArea[1] - ( (double)yPixel * pixelSize );
 		
 		// Since we're zooming in on a point (the "zoom centre"),
 		// let's now shrink each of the distances from the zoom centre
 		// to the edges of the picture by a constant percentage.
 		double newMinX = mousedOverX - (zoomPercentChange * (mousedOverX-oldGraphArea[0]));
 		double newMaxY = mousedOverY - (zoomPercentChange * (mousedOverY-oldGraphArea[1]));
 		
 		double oldMaxX = oldGraphArea[0] + oldGraphArea[2];
 		double newMaxX = mousedOverX - (zoomPercentChange * (mousedOverX-oldMaxX));
 		
 		double leftWidthDiff = newMinX - oldGraphArea[0];
 		double rightWidthDiff = oldMaxX - newMaxX;
 		
 		newGraphArea[0] = newMinX;
 		newGraphArea[1] = newMaxY;
 		newGraphArea[2] = oldGraphArea[2] - leftWidthDiff - rightWidthDiff;
 		
 		//clearPixelSizes();
 		
 		setGraphArea(newGraphArea, false);
 	}
 
 	// Returns zoom level, in range 0..ZOOM_SLIDER_SCALING	(logarithmic scale)
 	public int getZoomLevel() {
 		double lnPixelSize = Math.log(getPixelSize());
 		double zoomLevel = (double)ZOOM_SLIDER_SCALING * (lnPixelSize-MINZOOM_LN_PIXEL) / (MAXZOOM_LN_PIXEL-MINZOOM_LN_PIXEL);
 		return (int)zoomLevel;
 	}
 	
 	boolean saneZoomLevel() {
 		int zoomLevel = getZoomLevel();
 		if (
 			(zoomLevel >= 1) &&
 			(zoomLevel <= ZOOM_SLIDER_SCALING)
 		) return true;
 		return false;
 	}
 	
 	// Sets zoom, given number in range 0..1000 (logarithmic scale)
 	public void setZoomLevel(int zoomLevel) {
 		double lnPixelSize = MINZOOM_LN_PIXEL + (zoomLevel * (MAXZOOM_LN_PIXEL-MINZOOM_LN_PIXEL) / (double)ZOOM_SLIDER_SCALING);
 		double newPixelSize = Math.exp(lnPixelSize);
 		setPixelSize(newPixelSize);
 	}
 	
 	// Given a desired new pixel size, sets - keeping current image centre
 	void setPixelSize(double newPixelSize) {
 		double[] oldGraphArea = getGraphArea();
 		double[] newGraphArea = new double[3];
 		
 		double centerX = oldGraphArea[0] + (getPixelSize() * getWidth() * 0.5);
 		double centerY = oldGraphArea[1] - (getPixelSize() * getWidth() * 0.5);
 		
 		newGraphArea[0] = centerX - (getWidth() * newPixelSize * 0.5);
 		newGraphArea[1] = centerY + (getWidth() * newPixelSize * 0.5);
 		newGraphArea[2] = newPixelSize * getWidth();
 		
 		setGraphArea(newGraphArea, true);
 	}
 	
 	
 	public void startZooming(float initialMidX, float initialMidY)
 	{
 		controlmode = ControlMode.ZOOMING;
 		hasZoomed = true;
 		clearPixelSizes();
 	}
 	
 	
 	// Zoom in on the displayed bitmap
 	public void zoomImage(float focusX, float focusY, float newScaleFactor) {
 		midX = focusX;
 		midY = focusY;
 		scaleFactor = newScaleFactor;
 		
 		zoomChange((int)focusX, (int)focusY, 1/newScaleFactor);
 		
 		invalidate();
 	}
 	
 	
 	// After pinch gesture stops, crop bitmap to image on screen
 	public void stopZooming()
 	{		
 		controlmode = ControlMode.DRAGGING;
 		
 		stopAllRendering();
 		
 		setDrawingCacheEnabled(true);
 		fractalBitmap = Bitmap.createBitmap(getDrawingCache());
 		fractalBitmap.getPixels(fractalPixels, 0, getWidth(), 0, 0, getWidth(), getHeight());
 		setDrawingCacheEnabled(false);
 		
 		bitmapX = 0;
 		bitmapY = 0;
 		totalDragX = 0;
 		totalDragY = 0;
 		matrix.reset();
 	}
 	
 	
 
 /*-----------------------------------------------------------------------------------*/
 /* Iteration variables */
 /*-----------------------------------------------------------------------------------*/
 
 	/* Get the iteration scaling factor.
 	// Log scale, with values ITERATIONSCALING_MIN .. ITERATIONSCALING_MAX
 	// represented by values in range 0..CONTRAST_SLIDER_SCALING*/
 	public int getScaledIterationCount() {
 		return (int)(
 			CONTRAST_SLIDER_SCALING *
 			( Math.log(iterationScaling) - Math.log(ITERATIONSCALING_MIN) ) /
 			( Math.log(ITERATIONSCALING_MAX) - Math.log(ITERATIONSCALING_MIN) )
 		);
 	}
 	
 	/* Set the iteration scaling factor.
 	// Log scale, with values ITERATIONSCALING_MIN .. ITERATIONSCALING_MAX
 	// represented by values in range 0..CONTRAST_SLIDER_SCALING */
 	public void setScaledIterationCount(int scaledIterationCount) {
 		if (
 			(scaledIterationCount >= 0) &&
 			(scaledIterationCount <= CONTRAST_SLIDER_SCALING)
 		) {
 			iterationScaling = Math.exp(
 				Math.log(ITERATIONSCALING_MIN) + (
 				(scaledIterationCount * (Math.log(ITERATIONSCALING_MAX) - Math.log(ITERATIONSCALING_MIN))) /
 				CONTRAST_SLIDER_SCALING)
 			);
 			scheduleNewRenders();
 		}
 	}
 	
 	/* How many iterations to perform?
 	// Empirically determined to be generally exponentially rising, as a function of x = |ln(pixelSize)|
 	// ie, maxIterations ~ a(b^x)
 	// a, b determined empirically for Mandelbrot/Julia curves
 	// The contrast slider allows adjustment of the magnitude of a, with a log scale. */
 	int getMaxIterations() {
 		// How many iterations to perform?
 		double absLnPixelSize = Math.abs(Math.log(getPixelSize()));
 		double dblIterations = iterationScaling * ITERATION_CONSTANT_FACTOR * Math.pow(ITERATION_BASE, absLnPixelSize);
 		int iterationsToPerform = (int)dblIterations;
 		Log.d(TAG, "Performing " + iterationsToPerform + " iterations.");
 		Log.d(TAG, "Iteration scaling is " + iterationScaling + ".");
 		return Math.max(iterationsToPerform, MIN_ITERATIONS);
 	}
 	
 	
 	
 /*-----------------------------------------------------------------------------------*/
 /* Graph area */
 /*-----------------------------------------------------------------------------------*/
 	
 	//Set a new graph area, or 
 	void setGraphArea(double[] newGraphArea, boolean newRender) {
 		// We have a predefined graphArea, so we can be picky with newGraphArea.
 		if (graphArea != null) {
 			double[] initialGraphArea = graphArea;
 			graphArea = newGraphArea;
 			
 			// Zoom level is sane - let's allow this!
 			if (saneZoomLevel()) {
 				if(newRender)scheduleNewRenders();
 			// Zoom level is out of bounds; let's just roll back.
 			} else {
 				graphArea = initialGraphArea;
 			}
 		// There is no predefined graphArea; we'll have to accept whatever newGraphArea is.
 		} else {
 			graphArea = newGraphArea;
 			if(newRender) scheduleNewRenders();
 		}
 	}
 	
 	// On the complex plane, what is the current length of 1 pixel?
 	double getPixelSize() {
 		// Nothing to do - cannot compute a sane pixel size
 		if (getWidth() == 0) return 0.0;
 		if (graphArea == null) return 0.0;
 		// Return the pixel size
 		return (graphArea[2] / (double)getWidth());
 	}
 	
 	// Restore default canvas
 	public void canvasHome() {
 		// Default max iterations scaling
 		iterationScaling = ITERATIONSCALING_DEFAULT;
 		
 		// Default graph area
 		double[] newGraphArea = new double[homeGraphArea.length];
 		for (int i=0; i<homeGraphArea.length; i++) newGraphArea[i] = homeGraphArea[i];
 		setGraphArea(newGraphArea, true);
 	}
 	
 	
 	
 /*-----------------------------------------------------------------------------------*/
 /*Utilities*/
 /*-----------------------------------------------------------------------------------*/
 	
 	//Fill the pixel sizes array with a number larger than any reasonable block size
 	private void clearPixelSizes() {
 		Log.d(TAG, "Clearing pixel sizes");
 		//pixelSizes = new int[getWidth() * getHeight()];
 		
 		for (int i = 0; i < pixelSizes.length; i++)
 		{
 			pixelSizes[i] = 1000;
 		}
 	   }
 	
 	
 	//Stop current rendering and return to "home"
 	public void reset(){
 		pauseRendering = false;
 		
 		bitmapCreations = 0;
 
 		matrix.reset();
 		stopAllRendering();
 		fractalPixels = new int[getWidth() * getHeight()];
 		clearPixelSizes();
 		canvasHome();
 		
 		renderMode = RenderMode.NEW;
 		
 		postInvalidate();
 	}
 	
 	
 	//Return current graph area
 	public double[] getGraphArea() {
 		return graphArea;
 	}
 	
 	
 	//Stop all rendering, including planned and current
 	void stopAllRendering() {
 		//Stop planned renders
 		//renderingQueue.clear();
 		upperRenderQueue.clear();
 		lowerRenderQueue.clear();
 		
 		//Stop current render
 		upperRenderThread.abortRendering();
 		lowerRenderThread.abortRendering();
 	}
 	
 	
 	//Add a rendering of a particular pixel size to the queue
 	void scheduleRendering(int pixelBlockSize) {
 		upperRenderThread.allowRendering();
 		lowerRenderThread.allowRendering();
 		upperRenderQueue.add( new Rendering(pixelBlockSize) );
 		lowerRenderQueue.add( new Rendering(pixelBlockSize) );
 	}
 	
 	
 	//Retrieve the next rendering from the queue (used by render thread)
 	public Rendering getNextRendering(FractalSection section) throws InterruptedException {
 		if (section == FractalSection.UPPER)
 			return upperRenderQueue.take();
 		else //if (section == FractalSection.LOWER)
 			return lowerRenderQueue.take();
 		//else
 			//return renderingQueue.take();
 	}
 	
 	
 	public void setToBookmark()
 	{	
 		stopAllRendering();
 		
 		clearPixelSizes();
 		
 		double[] bookmark = new double[3];
 		
 		bookmark[0] = -1.631509065569354;
 		bookmark[1] = 0.0008548063308817164;
 		bookmark[2] = 0.0027763525271276013;
 		
 		Log.d(TAG, "Jumping to bookmark");
 		
 		setGraphArea(bookmark, true);
 	}
 	
 	
 	public void interruptThreads(){
 		upperRenderThread.interrupt();
 		lowerRenderThread.interrupt();
 	}
 	
 	
 	// Abstract methods
 	abstract void loadLocation(MandelbrotJuliaLocation mjLocation);
 	abstract void computePixels(
 			int[] outputPixelArray,  // Where pixels are output
 			int[] pixelSizeArray, //The size of each pixel's associated block
 			int pixelBlockSize,  // Pixel "blockiness"
 			final boolean showRenderingProgress,  // Call newPixels() on outputMIS as we go?
 			final int xPixelMin,
 			final int xPixelMax,
 			final int yPixelMin,
 			final int yPixelMax,
 			final double xMin,
 			final double yMax,
 			final double pixelSize,
 			final boolean allowInterruption,  // Shall we abort if renderThread signals an abort?
 			RenderMode currentRenderMode,
 			FractalSection section
 		);
 }
 
 
