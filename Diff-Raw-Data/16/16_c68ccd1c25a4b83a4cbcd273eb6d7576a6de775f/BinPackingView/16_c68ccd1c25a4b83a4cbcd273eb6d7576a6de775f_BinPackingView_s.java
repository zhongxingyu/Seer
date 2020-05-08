 package edu.upenn.cis350.algoviz;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import edu.upenn.cis350.algoviz.R;
 
 /**
  * View where user packs objects into bins.
  */
 public class BinPackingView extends View {
 	
 	///// Static variables /////
 	private static BinPackingProblemFactory factory;
 	private static ArrayList<BinObject> objects = new ArrayList<BinObject>();
 	private static ArrayList<Bin> bins = new ArrayList<Bin>();
 	private static BinObject objToMove = null;
 	private static final int TEXT_X_OFFSET = 10;
 	private static final int TEXT_Y_OFFSET = 25;
 	
 	///// Instance variables /////
 	private boolean reset;
 	private int current_value;
 
 	private BinObjectPaginator _currentPaginator, _unallocatedObjectsPaginator;
 	private int binWidth;
 	private int binHeight;
 	private int objWidth;
 	private int objHeight;
 
 	
 	///// Constructors /////
 	public BinPackingView(Context c) {
 		super(c);
 		BinPackingView.factory = new BinPackingProblemFactory(c);
 		reset=true;
 	}
 	
 	public BinPackingView(Context c, AttributeSet a) {
 		super(c,a);
 		BinPackingView.factory = new BinPackingProblemFactory(c);
 		reset=true;
 	}
 	
 	
 	///// Public methods /////
 	
 	/**
 	 * Initialize the app by fetching the list of Bins and BinObjects from
 	 * the BinPackingProblemFactory instantiated in the constructor.  Also,
 	 * set the size of each object to some percentage of the screen size.
 	 */
 	public void initialize() {
 		
 		objects = new ArrayList<BinObject>();
 		bins = new ArrayList<Bin>();
 		
 		bins.addAll(factory.getBins(((BinPackingActivity) this.getContext()).getProblemName()));
 		objects.addAll(factory.getBinObjects(((BinPackingActivity) this.getContext()).getProblemName()));
 		
 		// Location of center of View
 		int mid = this.getWidth() / 2;
 		
 		// Specify sizes of Bins and BinObjects relative to size of screen
 		binWidth = (int)(0.30 * this.getWidth());
 		binHeight = (int)(0.15 * this.getHeight());
 		objWidth = (int)(0.15 * this.getWidth());
 		objHeight = objWidth;
 		
 		int bin_loc_offset=100;
 		
 		// Set instances of Bins and BinObjects to appropriate size
 		for (Bin bin : bins) {
 			bin.setWidth(binWidth);
 			bin.setHeight(binHeight);
 		}
 		for (BinObject obj : objects) {
 			obj.setWidth(objWidth);
 			obj.setHeight(objHeight);
 		}
 		
 		// Initialize the paginator and set it to be the current one
 		_unallocatedObjectsPaginator = new BinObjectPaginator(mid, objWidth, objHeight, "Unallocated Objects");
 		_unallocatedObjectsPaginator.addAll(objects);		
 		_currentPaginator = _unallocatedObjectsPaginator;
 				
 		// Set locations for all Bins
 		Bin b1, b2, b3;
 		switch (BinPackingView.bins.size()) {
 		case 1:
 			b1 = BinPackingView.bins.get(0);
 			b1.instantiatePaginator(mid, "Middle Bin");
 			b1.setX(mid - binWidth / 2);
 			b1.setY(this.getHeight() - binHeight-bin_loc_offset);
 			break;
 		case 2:
 			b1 = BinPackingView.bins.get(0);
 			b1.instantiatePaginator(mid, "Left Bin");
 			b2 = BinPackingView.bins.get(1);
 			b2.instantiatePaginator(mid, "Right Bin");
 			b1.setX((int)(mid - binWidth - 0.015 * this.getWidth()));
			b1.setY(this.getHeight() - binHeight);
 			b2.setX((int)(mid + 0.015 * this.getWidth()));
 			b2.setY(this.getHeight() - binHeight-bin_loc_offset);
 			break;
 		case 3:
 			b1 = BinPackingView.bins.get(0);
 			b1.instantiatePaginator(mid, "Left Bin");
 			b2 = BinPackingView.bins.get(1);
 			b2.instantiatePaginator(mid, "Middle Bin");
 			b3 = BinPackingView.bins.get(2);
 			b3.instantiatePaginator(mid, "Right Bin");
 			b1.setX((int)(mid - binWidth / 2 - binWidth - 0.03 * this.getWidth()));
 			b1.setY(this.getHeight() - binHeight-bin_loc_offset);
 			b2.setX(mid - binWidth / 2);
 			b2.setY(this.getHeight() - binHeight-bin_loc_offset);
 			b3.setX((int)(mid + binWidth / 2 + 0.03 * this.getWidth()));
 			b3.setY(this.getHeight() - binHeight-bin_loc_offset);
 			break;
 		default:
 			Log.v("Bin number error", "Number of bins must be 1, 2, or 3!");
 			break;
 		} 
 				
 		// Initialize the total value to 0
 		current_value = 0;
 				
 	}
 	
 	protected void onDraw(Canvas c) {
 		// If drawing for the first time, get bins and objects from factory
 		// and set their locations using initialize()		
 		if (reset==true) {
 			initialize();
 			reset=false;
 		}
 		drawLevel(c);
 	}
 	
 	protected void drawLevel(Canvas canvas){
 		drawBins(canvas);
 		drawPaginator(canvas);
 		drawObjects(canvas);
 	}
 	
 	private void drawBins(Canvas canvas) {
 		Paint paint = new Paint();
 		paint.setTextSize(30);
 		for (Bin bin : bins) {
 			paint.setColor(bin.getColor());
 			canvas.drawRect(bin.getX(), bin.getY(), bin.getX() + binWidth, bin.getY() + binHeight, paint);
 			paint.setColor(Color.WHITE);
 			if (bin.getText().indexOf('C') == -1)
 				throw new UnsupportedOperationException("Unexpected Bin Text");
 			String text1 = bin.getText().substring(0,bin.getText().indexOf('C'));
 			String text2 = bin.getText().substring(bin.getText().indexOf('C'));
 			canvas.drawText(text1, bin.getX() + TEXT_X_OFFSET, bin.getY() + binHeight/2, paint);
 			canvas.drawText(text2, bin.getX() + TEXT_X_OFFSET, bin.getY() + binHeight/2 + TEXT_Y_OFFSET, paint);
 		}
 	}
 	
 	private void drawPaginator(Canvas canvas) {
 		Paint paint = new Paint();
 		paint.setColor(_currentPaginator.getColor());	
 		int xPos = _currentPaginator.getX(), yPos = _currentPaginator.getY(),
 				width =  _currentPaginator.getWidth(), height = _currentPaginator.getHeight();		
 		canvas.drawRect(xPos, yPos, xPos + width, yPos + height, paint);
 		
 		paint.setColor(Color.WHITE);
 		String title = _currentPaginator.getTitle();
 		canvas.drawText(title, xPos + width/2 - title.length()/2 * 6, yPos, paint);
 		
 		String currentPage = _currentPaginator.getText();
 		if (!currentPage.endsWith("1")) {
 			canvas.drawText(currentPage, xPos + width/2 - currentPage.length()/2 * 6, yPos+height-10, paint);
 			canvas.drawText("Next Page >>", width/2 + 80, yPos+height-10, paint);
 			canvas.drawText("<< Previous Page", width/2 - 80 - 16*6, yPos+height-10, paint);
 			if (!_currentPaginator.equals(_unallocatedObjectsPaginator)) canvas.drawText("X", width-50, yPos, paint);
 		}
 	}
 	
 	private void drawObjects(Canvas canvas) {
 		Paint paint = new Paint();
 		paint.setTextSize(30);
 		for (BinObject obj : _currentPaginator.getCurrentPageObjects()) {
 			paint.setColor(obj.getColor());
 			canvas.drawRect(obj.getX(), obj.getY(), obj.getX() + objWidth, obj.getY() + objHeight, paint);
 			paint.setColor(Color.WHITE);
 			if (obj.getText().indexOf('$') == -1)
 				throw new UnsupportedOperationException("Unexpected Object Text");
 			String text1 = obj.getText().substring(0,obj.getText().indexOf('$'));
 			String text2 = obj.getText().substring(obj.getText().indexOf('$'));
 			canvas.drawText(text1, obj.getX() + TEXT_X_OFFSET, obj.getY() + objHeight/2, paint);
 			canvas.drawText(text2, obj.getX() + TEXT_X_OFFSET, obj.getY() + objHeight/2 + TEXT_Y_OFFSET, paint);
 		}
 	}
 	
 	public void reset(){
 		reset = true;
 		BinObject[] objsInBin;
 		for (Bin bin : bins) {
 			objsInBin = new BinObject[bin.getContents().size()];
 			for (int i = 0; i < objsInBin.length; i++) {
 				objsInBin[i] = bin.getContents().get(i);
 			}
 			for (int i = 0; i < objsInBin.length; i++) {
 				bin.remove(objsInBin[i]);
 			}
 		}
 		invalidate();
 	}
 	
 	public boolean onTouchEvent(MotionEvent event) {
 		float x = event.getX(), y = event.getY();
 		int action = event.getAction();
 		
 		if (action == MotionEvent.ACTION_DOWN) return handleClick(x, y);
 		else if (action == MotionEvent.ACTION_MOVE) return handleDrag(x, y);
 		else if (action == MotionEvent.ACTION_UP) return handleDrop(x, y);
 		else return false;	
 	}
 	
 	/**
 	 * Handles ACTION_DOWN motion events.
 	 * @param x The x-coordinate of the touch event.
 	 * @param y The y-coordinate of the touch event.
 	 * @return True if object or bin is clicked, false otherwise.
 	 */
 	private boolean handleClick(float x, float y) {
 		for (BinObject obj : _currentPaginator.getCurrentPageObjects()) {
 			if (obj.containsPoint(x, y)) {
 				obj.setXDiff(x - obj.getX());
 				obj.setYDiff(y - obj.getY());
 				obj.setColor(Color.YELLOW);
 				objToMove = obj;
 				invalidate();
 				return true;
 			}
 		}
 		objToMove = null;
 		
 		for (Bin bin : bins) {
 			if (bin.containsPoint(x, y)) {
 				_currentPaginator = bin.getPaginator();
 				invalidate();
 				return true;
 			}
 		}
 		
 		int xPos = _currentPaginator.getX(), yPos = _currentPaginator.getY(),
 				width =  _currentPaginator.getWidth(), height = _currentPaginator.getHeight();
 		
 		ShapeObject prev = new ShapeObject(Color.BLACK, 12, 16*6, width/2 - 80 - 16*6, yPos+height-24, "");
 		ShapeObject next = new ShapeObject(Color.BLACK, 12, 12*6, width/2 + 80, yPos+height-24, "");
 		ShapeObject exit = new ShapeObject(Color.BLACK, 16, 12, width - 52, yPos-14, "");
 		
 		if (prev.containsPoint(x, y)) {
 			_currentPaginator.previousPage();
 			invalidate();
 		} else if (next.containsPoint(x, y)) {
 			_currentPaginator.nextPage();
 			invalidate();
 		} else if (exit.containsPoint(x,y)) {
 			_currentPaginator = _unallocatedObjectsPaginator;
 			invalidate();
 		}
 		
 		return false;
 	}
 	
 	private boolean handleDrag(float x, float y) {
 		if (objToMove == null) return true;
 		objToMove.setX((int)(x - objToMove.getXDiff()));
 		objToMove.setY((int)(y - objToMove.getYDiff()));
 		invalidate();
 		return true;
 	}
 	
 	/**
 	 * Handles ACTION_UP motion events.
 	 * @param x The x-coordinate of the touch event.
 	 * @param y The y-coordinate of the touch event.
 	 * @return
 	 */
 	private boolean handleDrop(float x, float y) {
 		if (objToMove == null) return true;
 		else objToMove.setColor(Color.BLUE);
 		
 		Bin currentBin = _currentPaginator.getBin();
 		
 		// If object collides with current paginator or current bin
 		if (objToMove.collidesWith(_currentPaginator) || objToMove.collidesWith(currentBin)) { 
 			objToMove.setX(objToMove.oldx);
 			objToMove.setY(objToMove.oldy);
 			invalidate();
 			return true;
 		}
 		
 		// If object collides with a different bin
 		for (Bin bin : bins) {
 			if (objToMove.collidesWith(bin)) {
 				boolean inserted = bin.insert(objToMove);
 				if (inserted) {
 					if (currentBin != null) currentBin.remove(objToMove);
 					else _unallocatedObjectsPaginator.remove(objToMove);
 				} else {
 					objToMove.setX(objToMove.oldx);
 					objToMove.setY(objToMove.oldy);
 				}
 				invalidate();
 				//TODO check if the solution is right
 				return true;
 			}
 		}
 		
 		// If object was dropped outside current paginator and did not collide with a bin, remove it and
 		// add to unallocatedObjectsPaginator
 		if (currentBin != null) currentBin.remove(objToMove);
 		else _unallocatedObjectsPaginator.remove(objToMove);
 		_unallocatedObjectsPaginator.add(objToMove);
 		invalidate();
 		return true;
 	}
 
 	
 	
 	
 	
 	
 	//when the user press "done" button
 	public double submit(){
 		//calculate the current value
 		current_value=0;
 		for (Bin bin : bins) 
 			current_value += bin.getValue(); 	
 
 			
 		Double optimalSol=BinPackingView.factory.getOptimalSolution(((BinPackingActivity) this.getContext()).getProblemName());
 		
 		if (optimalSol==current_value){
 			return 1.1;
 		}
 		else{
 			//supposedly if the user get a close answer, should show a toast
 			double percent=current_value/optimalSol;
 			if (percent>=0.9)
 				return 0.9;
 			else 
 				if (percent>=0.75)
 				return 0.75;
 			else 
 				if (percent>=0.5)
 				return 0.5;
 			else 
 				if (percent>=0.3)
 					return 0.3;
 			else
 				return 0;
 		}
 		
 	}
 	
 	public BinPackingProblemFactory getFactory() {
 		return factory;
 	}
 		
 } 
