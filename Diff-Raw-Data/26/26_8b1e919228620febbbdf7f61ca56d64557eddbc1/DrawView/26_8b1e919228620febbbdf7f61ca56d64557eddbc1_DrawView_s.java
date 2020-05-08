 package lukianol.tictactoe.in2d.drawing;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import lukianol.tictactoe.Position;
 import lukianol.tictactoe.StrokeKind;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Cap;
 import android.graphics.Paint.Style;
 import android.graphics.RectF;
 import android.view.MotionEvent;
 import android.view.View;
 
 public final class DrawView extends View {
 
 	public DrawView(Context context, int playgroundSize) {
 		this(context, playgroundSize, false);
 	}
 	
 	public DrawView(Context context, int playgroundSize, Boolean debugMode) {
 		super(context);	
 		
 		_debugMode = debugMode;
 		_size = playgroundSize;	
 		_horizontalAreaMarginPercent = _verticalAreaMarginPercent = 0.05f;		
 		initializePaints();
 		_tileFactory = new StrokeTileFactory(_strokesPaint, 0.2f, 0.2f);
 		_textTileFactory = new TextStrokeTileFactory(_jokesPaint, 0.1f, 0.1f);
 	}
 
 	public void initSurface(){
 		_positionToStrokeKindMap.clear();
 		_wonPositions = null;	
 		_isInited = true;
 		invalidate();
 	}
 	
 	public void addStroke(Position position, StrokeKind strokeKind){
 		_positionToStrokeKindMap.put(position, strokeKind);
 		this.invalidate();
 	}
 	
 	public void setWonStrokePositions(Position[] positions){
 		_wonPositions = positions;
 		invalidate();
 	}
 	
 	public void setOnPositionTouchEventListener(OnPositionTouchEventListener listener){
 		_onPositionTouchListener = listener;
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		
 		switch(event.getAction()){
 		
 		case MotionEvent.ACTION_DOWN:
 			Position position = getPosition(event);			
 			if (position != null)
 				return invokeOnPositionTouchEventListener(position);
 			break;
 			
 		default:
 			return super.onTouchEvent(event);			
 		}
 		
 		return super.onTouchEvent(event);		
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {		
 			
 		initSurfaceBox();
 		drawBackground(canvas);
 		debugDrawView(canvas);
 		debugDrawSurfaceBox(canvas);
 		drawVerticalLines(canvas);		
 		drawHorizontalLines(canvas);
 		
 		if (this._isInited){
 			drawStrokes(canvas);
 			drawWonPositions(canvas);
 		} else {
 									
 			for (int i  = 0; i < Math.min(joke.length, _size); i++){
 				RectF rect = this._positionToFieldMap.get(Position.Create(i, i));
 				_textTileFactory.get(joke[i]).Draw(canvas, rect);
 			}
 			
 		}
 		
 		
 	}
 	
 	private void initSurfaceBox(){
 		
 		float hMargin = getHorizontalAreaMargin();
 		float vMargin = getVerticalAreaMargin();
 		
 		_surfaceBox = new RectF();
 		_surfaceBox.left = hMargin;
 		_surfaceBox.right =  (this.getWidth() - hMargin);
 		_surfaceBox.top =  vMargin;
 		_surfaceBox.bottom = (this.getHeight() - vMargin);
 		
 		float fieldHeight = getFieldHeight();
 		float fieldWidth = getFieldWidth();
 		Map<Position, RectF> map = new HashMap<Position, RectF>();
 		
 		for(int c = 0; c < _size; c++)
 			for(int r = 0; r < _size; r++){
 				Position position = Position.Create(c, r);
 				RectF field = new RectF();
 				field.left = _surfaceBox.left + fieldWidth * c;
 				field.right = field.left + fieldWidth;
 				field.top = _surfaceBox.top + fieldHeight * r;
 				field.bottom = field.top + fieldHeight;
 				map.put(position, field);
 			}
 		
 		_positionToFieldMap = Collections.unmodifiableMap(map);
 	}
 		
 	private void drawBackground(Canvas canvas){		
 		canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), this._backgroundPaint);
 	}
 	
 	private void debugDrawView(Canvas canvas){
 		if (this._debugMode)
 			canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), this._debugBorderPaint);
 	}
 	
 	private void debugDrawSurfaceBox(Canvas canvas){
 		if (_debugMode)
 			canvas.drawRect(_surfaceBox, this._debugBorderPaint);
 	}
 	
 	private void drawVerticalLines(Canvas canvas) {
 		
 		float widthInterval = getFieldWidth();
 		float startY = _surfaceBox.top, stopY = _surfaceBox.bottom;
 		
 		for(int i = 1; i < _size; i++){
 			float startX, stopX;
 			startX = stopX = widthInterval * i + _surfaceBox.left;						
 			canvas.drawLine(startX, startY, stopX, stopY, _linesPaint);			
 		}
 		
 	}
 	
 	private void drawHorizontalLines(Canvas canvas) {
 			
 		float heightInterval = getFieldHeight();
 		float startX = _surfaceBox.left, stopX = _surfaceBox.right;
 		
 		for(int i = 1; i < _size; i++){
 			float startY, stopY;
 			startY = stopY = heightInterval * i + _surfaceBox.top;
 						
 			canvas.drawLine(startX, startY, stopX, stopY, _linesPaint);			
 		}
 		
 	}
 	
 	private void drawStrokes(Canvas canvas) {
 		
 		for(Map.Entry<Position, StrokeKind> item : _positionToStrokeKindMap.entrySet()){
 			
 			IStrokeTile tile = this._tileFactory.get(item.getValue());
 			tile.Draw(canvas, _positionToFieldMap.get(item.getKey()));			
 		}
 		
 	}
 
 	private void drawWonPositions(Canvas canvas){
 		
 		if (hasWonPositions()){
 
			RectF start = _positionToFieldMap.get(_wonPositions[0]);
			RectF end = _positionToFieldMap.get(_wonPositions[_wonPositions.length - 1]);
 			
			RectF finalRect = new RectF(start);
			finalRect.union(end);
 			float finalHeight = finalRect.height();
 			float finalWidth = finalRect.width();
 			float startX, startY, stopX, stopY;
 			 
			if ( finalHeight == finalWidth ) 							
				if (start.centerX() < end.centerX()){
 					startX = finalRect.left;
 					startY = finalRect.top;
 					stopX = finalRect.right;
 					stopY = finalRect.bottom;
 				} else {
 					startX = finalRect.right;
 					startY = finalRect.top;
 					stopX = finalRect.left;
 					stopY = finalRect.bottom;
 				}
 			else if (finalHeight > finalWidth){
 				startX = finalRect.centerX();
 				startY = finalRect.top;
 				stopX = startX;
 				stopY = finalRect.bottom;
 			}
 			else {
 				startX = finalRect.left;
 				startY = finalRect.centerY();
 				stopX = finalRect.right;
 				stopY = startY;
 			}
 			
 			canvas.drawLine(startX, startY, stopX, stopY, this._wonPaint);
 			
 		}
 		
 	}
 	
 	private void initializePaints() {
 		_strokesPaint = _linesPaint = new Paint();
 		_linesPaint.setColor(Color.WHITE);
 		_linesPaint.setStrokeWidth(10);
 		_linesPaint.setStrokeCap(Cap.ROUND);
 		_linesPaint.setStyle(Style.STROKE);
 		_linesPaint.setAntiAlias(true);
 		
 		_jokesPaint = new Paint(_strokesPaint);
 		_jokesPaint.setTextSize(90);
 		_jokesPaint.setTextAlign(Align.CENTER);
 		_jokesPaint.setColor(Color.YELLOW);
 		_jokesPaint.setTextAlign(Align.CENTER);
 				
 		_wonPaint = new Paint();
 		_wonPaint.setARGB(225, 255, 0, 0);
 		_wonPaint.setStrokeWidth(20);
 		_wonPaint.setStrokeCap(Cap.ROUND);
 		_wonPaint.setStyle(Style.STROKE);
 		_wonPaint.setAntiAlias(true);
 		
 		_backgroundPaint = new Paint();
 		_backgroundPaint.setColor(Color.BLACK);
 		_backgroundPaint.setStyle(Style.FILL);		
 		
 		if (_debugMode) {
 			_debugBorderPaint = new Paint();
 			_debugBorderPaint.setColor(Color.RED);
 			_debugBorderPaint.setStrokeWidth(2);
 			_debugBorderPaint.setStyle(Style.STROKE);
 			_debugBorderPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 1));
 		}
 		else 
 			_debugBorderPaint = null;
 	}
 	
 	private Boolean invokeOnPositionTouchEventListener(Position position){
 		if (_onPositionTouchListener != null)
 		{
 			return  _onPositionTouchListener.OnPositionTouch(this, position);	
 			
 		}
 			
 		return false;
 	}
 		
 	private Position getPosition(MotionEvent event) {
 		
 		if (_positionToFieldMap != null)
 			for(Map.Entry<Position, RectF> field : _positionToFieldMap.entrySet())			
 				if (field.getValue().contains(event.getX(), event.getY()))
 					return field.getKey();
 		
 		return null;		
 	}
 		
 	private float getHorizontalAreaMargin(){
 		int width = this.getWidth();
 		int height = this.getHeight();
 		int squarizer = ((width > height) ? width - height : 0); 
 		return (Math.min(height, width) * _horizontalAreaMarginPercent) + (squarizer / 2);
 	}
 	
 	private float getVerticalAreaMargin(){
 		int width = this.getWidth();
 		int height = this.getHeight();
 		int squarizer = ((height > width) ? height - width : 0);
 		return (Math.min(height, width) * _verticalAreaMarginPercent) + (squarizer / 2);
 	}
 	
 	private float getFieldWidth(){
 		return this._surfaceBox.width() / this._size;
 	}
 	
 	private float getFieldHeight(){
 		return this._surfaceBox.height() / this._size;
 	}
 	
 	private Boolean hasWonPositions(){
 		return _wonPositions != null;
 	}
 	
 	private Paint _debugBorderPaint;
 	private Paint _backgroundPaint;
 	private Paint _linesPaint;
 	private Paint _strokesPaint;
 	private Paint _jokesPaint;
 	private Paint _wonPaint;
 	private final int _size;
 	private final float _horizontalAreaMarginPercent;
 	private final float _verticalAreaMarginPercent;
 	private RectF _surfaceBox;
 	private Map<Position, RectF> _positionToFieldMap;
 	private final Boolean _debugMode;
 	private OnPositionTouchEventListener _onPositionTouchListener;
 	private final Map<Position, StrokeKind> _positionToStrokeKindMap = new HashMap<Position, StrokeKind>();
 	private Position[] _wonPositions;
 	private boolean _isInited = false;
 	private final StrokeTileFactory _tileFactory;
 	private final TextStrokeTileFactory _textTileFactory;
 	
 	private final String[] joke = {"tic", "tac", "toe"};
 		
 	public interface OnPositionTouchEventListener {
 	public abstract Boolean OnPositionTouch(View view, Position position);
 	
 	}
 	
 }
