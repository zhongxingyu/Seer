 package is.ru.app.puzzle;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.drawable.ShapeDrawable;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class BoardDrawableView extends View {
 	private List<ShapeDrawable> shapes = new ArrayList<ShapeDrawable>();
 	private ShapeDrawable m_shape = new ShapeDrawable();
 	private Rect m_rect = new Rect(); // Or other Shape
 	private Integer[] m_colors = { 0xff74AC23, 0xff74AB73, 0xff74DD26,
 			0xff74AC13, 0xff84AC13, 0xffffBC13, 0xff74AC00, 0xffAAAC73 };
 	Toast toast = Toast.makeText(getContext(), "BOING", Toast.LENGTH_SHORT);
 
 	//private int[] m_colors = { Color.RED, Color.GREEN, Color.BLUE,
 	//		Color.LTGRAY, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.WHITE };
 	int heightScreen = 0;
 	int widthScreen = 0;
 	boolean moving = false;
 	int startX = 0, startY = 0, endX = 0, endY = 0;
 	int deltaX = 0, deltaY = 0;
 	// <setup>(H 1 2 2), (V 0 1 3), (H 0 0 2), (V 3 1 3), (H 2 5 3), (V 0 4 2),
 	// (H 4 4 2), (V 5 0 3)</setup>
 	Map<String, ArrayList<Integer>> boxes = new HashMap<String, ArrayList<Integer>>();
 
 	private int[][] board = { { 1, 1, 1, 1, 1, 1 }, { 1, 0, 2, 0, 0, 0 },
 			{ 0, 0, 2, 0, 0, 1 }, { 0, 1, 1, 1, 0, 1 }, { 0, 0, 0, 0, 1, 1 },
 			{ 1, 1, 1, 0, 1, 0 } };
 
 	public BoardDrawableView(Context context) {
 		super(context);
 		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 		widthScreen = metrics.widthPixels;
 		heightScreen = metrics.heightPixels;
 		createBoxes();
 		init2();
 	}
 
 	// <setup>(H 1 2 2), (V 0 1 3), (H 0 0 2), (V 3 1 3), (H 2 5 3), (V 0 4 2),
 	// (H 4 4 2), (V 5 0 3)</setup>
 	public void createBoxes() {
 		ArrayList<Integer> box1 = new ArrayList<Integer>();
 		box1.add(1);
 		box1.add(2);
 		box1.add(2);
 		boxes.put("H122", box1);
 
 		ArrayList<Integer> box2 = new ArrayList<Integer>();
 		box2.add(0);
 		box2.add(1);
 		box2.add(3);
 		boxes.put("V013", box2);
 
 		ArrayList<Integer> box3 = new ArrayList<Integer>();
 		box3.add(0);
 		box3.add(0);
 		box3.add(2);
 		boxes.put("H002", box3);
 
 		ArrayList<Integer> box4 = new ArrayList<Integer>();
 		box4.add(3);
 		box4.add(1);
 		box4.add(3);
 		boxes.put("V313", box4);
 
 		ArrayList<Integer> box5 = new ArrayList<Integer>();
 		box5.add(2);
 		box5.add(5);
 		box5.add(3);
 		boxes.put("H253", box5);
 
 		ArrayList<Integer> box6 = new ArrayList<Integer>();
 		box6.add(0);
 		box6.add(4);
 		box6.add(2);
 		boxes.put("V042", box6);
 
 		ArrayList<Integer> box7 = new ArrayList<Integer>();
 		box7.add(4);
 		box7.add(4);
 		box7.add(2);
 		boxes.put("V442", box7);
 
 		ArrayList<Integer> box8 = new ArrayList<Integer>();
 		box8.add(5);
 		box8.add(0);
 		box8.add(3);
 		boxes.put("V503", box8);
 	}
 
 	protected void init() {
 		int width = widthScreen / 6;
 		int height = width;
 		int x = 0;
 		int y = 0;
 		int xOffset = width;
 		int yOffset = width;
 		for (int i = 0; i < board.length; i++) {
 
 			for (int j = 0; j < board[i].length; j++) {
 				if (board[i][j] == 1 || board[i][j] == 2) {
 
 					m_rect.set(x, y, x + width, y + height);
 
 					m_shape.setBounds(m_rect);
 					m_shape.getPaint().setColor(m_colors[i]);
 					m_shape.setPadding(20, 20, 20, 20);
 					shapes.add(m_shape);
 				}
 				y = +y + yOffset;
 			}
 			x = +x + xOffset;
 			y = 0;
 		}
 	}
 
 	protected void init2() {
 		int width = widthScreen / 6;
 		int height = width;
 		int x = 0;
 		int y = 0;
 		int xOffset = width;
 		int yOffset = width;
 		int count = 0;
 		for (Map.Entry<String, ArrayList<Integer>> entry : boxes.entrySet()) {
 			String key = entry.getKey();
 			ArrayList<Integer> values = entry.getValue();
 			x = values.get(0);
 			y = values.get(1);
 			int span = values.get(2);
 			span = span * width;
 			Rect shape = new Rect(); // Or other Shape
 			ShapeDrawable shapeD = new ShapeDrawable();
 			y = y * yOffset;
 			x = x * xOffset;
 			if (key.startsWith("H")) {
 				shape.set(x, y, x + span, y + height);
 				shapeD.setBounds(shape);
 			} else if (key.startsWith("V")) {
 				shape.set(x, y, x + width, y + span);
 				shapeD.setBounds(shape);
 			}
 			shapeD.getPaint().setColor(m_colors[count]);
 			// shapeD.setPadding(20, 20, 20, 20);
 			shapes.add(shapeD);
 
 			count++;
 		}
 	}
 
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		for (ShapeDrawable shape : shapes) {
 			shape.draw(canvas);
 		}
 	}
 
 	@Override
 	/*	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			int x = (int) event.getX();
 			int y = (int) event.getY();
 
 			if(startX > 0 && startY > 0){
 				deltaX = startX - x;
 				deltaY = startY - y;
 			}
 
 			startX = x;
 			startY = y;
 
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 			//	m_shape.getPaint().setColor(Color.RED);
 			//	m_shape.draw(canvas);
 				isHitBlock(x + deltaX, y + deltaY);
 				break;
 			case MotionEvent.ACTION_MOVE:
 				//m_shape.getPaint().setColor(Color.BLUE);
 				//m_shape.draw(canvas);
 				isHitBlock(x + deltaX, y + deltaY);
 				break;
 			default:
 				break;
 			}
 			invalidate();
 			return true;
 		}
 		return false;
 	}_*/
 
 	public boolean onTouchEvent(MotionEvent event) {
 		final int x = (int)event.getX();
 		final int y = (int)event.getY();
 		final ShapeDrawable bounds = isHitBlock(x, y);
 		switch( event.getAction() ){
 		case MotionEvent.ACTION_DOWN:
 			if(bounds != null){
 				Rect rect = new Rect();
 				rect.set(bounds.getBounds().left, bounds.getBounds().top, bounds.getBounds().right, bounds.getBounds().bottom);
 				moving = rect.intersects(x, y, x+1, y+1);
 				invalidate();
 			}
 			return true;
 		case MotionEvent.ACTION_MOVE:
 			if( bounds != null && moving ){
 				final int x_new = (int)event.getX();
 				final int y_new = (int)event.getY();
 				if(bounds != null ){
 					if(bounds.getBounds() != null){
 						int with = Math.abs(bounds.getBounds().left - bounds.getBounds().right);
 						int height = Math.abs(bounds.getBounds().bottom - bounds.getBounds().top);
 						//TODO: fix bounds
 						for (ShapeDrawable shape  : shapes){
 							if(collision(bounds, shape)){
 								moving = false;
								bounds.invalidateSelf();
 							}
 							else{	
 								if(horizontal(bounds)){
 								bounds.setBounds(
 										x_new - with/2,
 										bounds.getBounds().top,
 										x_new + with/2,
 										bounds.getBounds().bottom);
 								invalidate();
 								}
 								else{
 									bounds.setBounds(
 											bounds.getBounds().left,
 											y_new - height/2,
 											bounds.getBounds().right,
 											y_new + height/2);
 									invalidate();
 									
 								}
 							}
 
 						}
 					} 
 				}
 			}
 			return true;
 		case MotionEvent.ACTION_UP:
 			moving = false;
 			return true;
 		}
 		return false;
 	}        
	private boolean collision(ShapeDrawable rect1, ShapeDrawable rect2){
		if(rect1.getBounds() != rect2.getBounds()){
			int [] coor1 = {rect1.getBounds().bottom, rect1.getBounds().top, rect1.getBounds().right, rect1.getBounds().left};
			int [] coor2 = {rect2.getBounds().bottom, rect2.getBounds().top, rect2.getBounds().right, rect2.getBounds().left};
 			

 		}
 		return false;
 
 	}
 
 	private ShapeDrawable isHitBlock(int x, int y) {
 		for (ShapeDrawable shape : shapes) {
 			Rect bounds = shape.getBounds();
 			if (bounds.contains(x, y)) {
 				return shape;
 			}
 		}
 		return null;
 	}
 	
 	/*
 	 * Simple method to see if a block is horizontally positioned or vertically.
 	 * returns true if it is horizontal.
 	 */
 	private boolean horizontal(ShapeDrawable rect1){
 		int diffh = Math.abs(rect1.getBounds().left - rect1.getBounds().right);
 		int diffv = Math.abs(rect1.getBounds().bottom - rect1.getBounds().top);
 		if(diffv > diffh){
 			return false;
 		} else{
 			return true;
 		}
 		
 	}
 }
