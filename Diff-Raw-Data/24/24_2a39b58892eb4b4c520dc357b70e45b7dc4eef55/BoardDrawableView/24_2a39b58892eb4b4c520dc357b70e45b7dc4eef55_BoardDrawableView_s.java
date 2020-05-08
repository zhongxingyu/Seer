 package is.ru.app.puzzle;
 
 import is.ru.app.db.PuzzleAdapter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.crypto.spec.IvParameterSpec;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapShader;
 import android.graphics.Canvas;
 import android.graphics.RadialGradient;
 import android.graphics.Shader;
 import android.graphics.SweepGradient;
 
 import android.graphics.Rect;
 import android.graphics.drawable.ShapeDrawable;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class BoardDrawableView extends View {
 
 	private List<ShapeDrawable> shapes = new ArrayList<ShapeDrawable>();
 	private PuzzleAdapter mPuzzlesAdapter = new PuzzleAdapter( this.getContext() );
 	
 	private int id = 0;
 	private int ratio;
 
 	// Blue colors
 	private Integer[] m_colors = { 0xffCC0000, 0xff0099CC, 0xff3399CC,
 			0xff6699CC, 0xff6633FF, 0xff0000CC, 0xff0033CC, 0xff6633CC,
 			0xff663366, 0xff33FFFF, 0xff339999, 0xff33CCFF, 0xff9999CC, 
 			0xff0066FF, 0xff3300FF, 0xff0066CC, 0xff333399, 0xff330066 };
 
 	private Integer[] m_green_colors = { 0xff33CC99, 0xff66CC99, 0xff99CC99,
 			0xff009966, 0xff339966, 0xff669966, 0xff999966, 0xff999933,
 			0xff999900, 0xff669933, 0xff666633, 0xff666600, 0xff336633,
 			0xff336600, 0xff006633, 0xff006600, 0xff006666, 0xff336666 };
 
 	Toast toast = Toast.makeText(getContext(), "BOING", Toast.LENGTH_SHORT);
 
 	private int heightScreen = 0;
 	private int widthScreen = 0;
 	private boolean moving = false;
 	private int startX = 0, startY = 0, endX = 0, endY = 0;
 	private int deltaX = 0, deltaY = 0;
 
 	private static final String puzzleFile = "challenge_classic40.xml";
 
 	// <setup>(H 1 2 2), (V 0 1 3), (H 0 0 2), (V 3 1 3), (H 2 5 3), (V 0 4 2),
 	// (H 4 4 2), (V 5 0 3)</setup>
 	Map<String, LinkedList<Integer>> boxes = new LinkedHashMap<String, LinkedList<Integer>>();
 
 	public BoardDrawableView(Context context, int id) {
 		super(context);
 		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 
 		widthScreen = metrics.widthPixels;
 		heightScreen = metrics.heightPixels;
 		if(widthScreen > heightScreen){
 			widthScreen = heightScreen;
 			
 		} else {
 			heightScreen = widthScreen;

 		}
 		this.id = id;
 		//Read puzzle from db
 		readInPuzzle(id);
 		init();
 	}
 	
 	public void readInPuzzle(int id){
 		Cursor cursor = mPuzzlesAdapter.queryPuzzle(id);
 		System.out.println("id " + id);
 		while(cursor.moveToNext())
 		  {
 			int s_id = cursor.getInt(1);
 			String s_setup = cursor.getString(2);
 			String s_level = String.valueOf(cursor.getInt(3));
 			String s_length = String.valueOf(cursor.getInt(4));
 			boolean s_solved = cursor.getInt(4) == 0 ? false : true;
 			Puzzle puzzle = new Puzzle(String.valueOf(id), s_setup, s_level, s_length, s_solved, true);
 		//	mPuzzlesAdapter.updatePuzzleRestPlaying();
 			mPuzzlesAdapter.updatePuzzle(s_id, true);
 			createBoxes(puzzle);
 		  }
 		cursor.close();
 	}
 
 
 	// <setup>(H 1 2 2), (V 0 1 3), (H 0 0 2), (V 3 1 3), (H 2 5 3), (V 0 4 2),
 	// (H 4 4 2), (V 5 0 3)</setup>
 	public void createBoxes(Puzzle puzzle) {
 		Pattern pattern = Pattern.compile("\\d+");
 		String setup = puzzle.setup;
 		LinkedList<String> items = new LinkedList<String>(Arrays.asList(setup.split(",")));
 
 		for (String s : items) {
 			LinkedList<Integer> box = new LinkedList<Integer>();
 			Matcher m = pattern.matcher(s);
 			while (m.find()) {
 				String v = m.group();
 				box.add(Integer.valueOf(v));
 			}
 			boxes.put(s.trim(), box);
 		}
 	}
 	
 
 	protected void init() {
 		int width = widthScreen / 6;
 		int height = width;
 		int x = 0, y = 0;
 		int xOffset = width;
 		int yOffset = width;
 		int count = 0;
 
 		for (Map.Entry<String, LinkedList<Integer>> entry : boxes.entrySet()) {
 			String key = entry.getKey();
 			LinkedList<Integer> values = entry.getValue();
 			x = values.get(0);
 			y = values.get(1);
 			int span = values.get(2);
 			span = span * width;
 			Rect shape = new Rect(); // Or other Shape
 
 			ShapeDrawable shapeD = new ShapeDrawable();
 			y = y * yOffset;
 			x = x * xOffset;
 
 			if (key.startsWith("(H")) {
 				shape.set(x, y, x + span, y + height);
 				shapeD.setBounds(shape);
 			} else if (key.startsWith("(V")) {
 				shape.set(x, y, x + width, y + span);
 				shapeD.setBounds(shape);
 			}
 			//shapeD.getPaint().setStyle(Style.STROKE);
 			//shapeD.getPaint().setShader(makeLinear(shape.centerX(), shape.centerY()));
 		
 			shapeD.getPaint().setColor(m_colors[count++]);
 			// shapeD.setPadding(20, 20, 20, 20);
 			shapes.add(shapeD);
 		}
 	}
 
      private static Shader makeLinear(int x, int y) {
          return new RadialGradient (x, y, 50, new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF },
                            null, Shader.TileMode.MIRROR);
      }
 
 	private static Shader makeLinear() {
 		return new LinearGradient(0, 0, 50, 50,
 				new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF },
 				null, Shader.TileMode.CLAMP);
 	}
 
 	private static Shader makeTiling() {
 		int[] pixels = new int[] { 0xFF00FF00, 0xFF0000FF,0xFFFF0000, 0};
 		Bitmap bm = Bitmap.createBitmap(pixels, 4, 4,
 				Bitmap.Config.ARGB_8888);
 
 		return new BitmapShader(bm, Shader.TileMode.REPEAT,
 				Shader.TileMode.REPEAT);
 	}
 
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		for (ShapeDrawable shape : shapes) {
 			//shape.getBounds().inset( (int)(shape.getBounds().width() * 0.01), (int)(shape.getBounds().height() * 0.01) );
 			shape.draw(canvas);
 		}
 		/*GradientDrawable g = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { Color.GREEN, Color.RED, Color.BLUE });
 		g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
 		g.setGradientRadius(140.0f);
 		g.setGradientCenter(0.0f, 0.45f);
 		g.draw(canvas);*/
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		final int x = (int) event.getX();
 		final int y = (int) event.getY();
 		if(isHitBlockTrue(x, y)){
 			final ShapeDrawable bounds = isHitBlock(x, y);
 			int range[] = maxRange(bounds);
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				if (bounds != null) {
 					//	Rect rect = new Rect();
 					//	rect.set(bounds.getBounds().left, bounds.getBounds().top,
 					//			bounds.getBounds().right, bounds.getBounds().bottom);
 					moving = (bounds.getBounds().intersects(x, y, x + 1, y + 1));
 					invalidate();
 
 				}
 				return true;
 			case MotionEvent.ACTION_MOVE:
 				int old_left = bounds.getBounds().left;
 				int old_right = bounds.getBounds().right;
 				int old_top = bounds.getBounds().top;
 				int old_bottom = bounds.getBounds().bottom;
 
 				if( bounds != null && moving ){
 					final int x_new = (int)event.getX();
 					final int y_new = (int)event.getY();
 					if(bounds != null ){
 						if(bounds.getBounds() != null){
 							int width = Math.abs(bounds.getBounds().left - bounds.getBounds().right);
 							int height = Math.abs(bounds.getBounds().bottom - bounds.getBounds().top);
 							//TODO: fix bounds
 
 							boolean moveable = true;
 							if(horizontal(bounds)){
 								//System.out.println(range[0] +" , "+ range[1]);
 
 								if((x_new - width/2 > x_new - width/2- range[0]) && (x_new + width/2 <x_new + width/2 + range[1]) && moveable){
 									bounds.setBounds(x_new - width/2,
 											old_top,
 											x_new + width/2,
 											old_bottom);
 									invalidate();
 								} if (x_new - width/2 == x_new - width/2- range[0])  {
 									moving = false;
 									moveable = false;
 									bounds.setBounds(old_left+2,
 											old_top,
 											old_right+2,
 											old_bottom);
 									
 									moveable = true;
 									invalidate();
 									
 
 								} else if((x_new + width/2 ==x_new + width/2 + range[1])){
 									moving =false;
 									moveable = false;
 									bounds.setBounds(old_left-2,
 											old_top,
 											old_right-2,
 											old_bottom);
 									
 									moveable = true;
 									invalidate();
 
 								}
 
 
 
 							}else{
 
 								if((y_new - height/2 > y_new - height/2 - range[0]) &&(y_new + height/2<  y_new + height/2 + range[1])){
 									
 
 									bounds.setBounds(old_left,
 											y_new - height/2, 
 											old_right, 
 											y_new+ height/2);
 									invalidate();
 								}
 								else if((y_new + height/2 ==  y_new + height/2 + range[1])){
 									moving =false;
 									bounds.setBounds(old_left,
 											old_top-2, 
 											old_right, 
 											old_bottom-2);
 									invalidate();
 
 
 
 
 								} else if((y_new - height/2 > y_new - height/2 - range[0])) {
 									moving =false;
 									bounds.setBounds(old_left,
 											y_new + height/2, 
 											old_right, 
 											y_new+ height/2);
 									invalidate();
 
 
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
 		else{
 			return false;
 		}
 	}      
 
 	private boolean collision(ShapeDrawable shape1){
 		Rect rect1 = shape1.getBounds();
 		for (ShapeDrawable shape2 : shapes){
 			if(!shape1.equals(shape2)){
 				Rect rect2 = shape2.getBounds();
 				System.out.println(rect2.top + " " + rect1.bottom);
 				//				if(rect1.contains(rect2.left, rect2.top) ||
 				//						rect1.contains(rect2.right, rect2.top)||
 				//						rect1.contains(rect2.right, rect2.bottom)||
 				//						rect1.contains(rect2.left, rect2.bottom)){
 				//					return true;
 				//				}
 
 				return Rect.intersects(rect2, rect1);
 			}
 		}
 		return false;
 
 	}
 
 	private int [] maxRange(ShapeDrawable shape){
 		//First entry is left/up moving range, second entry is right/down moving range.
 		if(shape != null){
 			int maxleft = 100000;
 			int maxright = 10000;
 			int [] range = new int[2];
 			for (ShapeDrawable shape2 : shapes){
 				if(!shape.equals(shape2)){
 
 					if(horizontal(shape) == true){
 						maxleft = shape.getBounds().left;
 						maxright = widthScreen = shape.getBounds().right;
 						if(((shape.getBounds().top < shape2.getBounds().top) && (shape2.getBounds().top < shape.getBounds().bottom))|| 
 								((shape.getBounds().top < shape2.getBounds().bottom) && (shape2.getBounds().bottom <shape.getBounds().bottom))||
 								(((shape2.getBounds().top <= shape.getBounds().top) && (shape.getBounds().bottom <= shape2.getBounds().bottom)))){
 							if((shape2.getBounds().left - shape.getBounds().right) < (shape.getBounds().left - shape2.getBounds().right)){
 								//It's to the right from our current brick
 								maxleft = Math.min(maxleft,Math.abs((shape.getBounds().left-shape2.getBounds().right)));
 								//System.out.println("horizontal right bound detected");
 							} else {
 								// It's to the left obviously
 
 								maxright = Math.min(maxright, Math.abs((shape2.getBounds().left-shape.getBounds().right)));
 							}
 
 						}
 
 
 					} else {
 						maxleft = shape.getBounds().top;
 						maxright = widthScreen = shape.getBounds().bottom;
 						if(((shape.getBounds().left < shape2.getBounds().left) && (shape2.getBounds().left <shape.getBounds().right))||
 								((shape.getBounds().left < shape2.getBounds().right) && (shape2.getBounds().right <shape.getBounds().right))
 								||((shape2.getBounds().left <= shape.getBounds().left)&&(shape.getBounds().right <= shape2.getBounds().right))){
 							if((shape2.getBounds().bottom -shape.getBounds().top)<(shape.getBounds().bottom -shape2.getBounds().top)){
 								//It's above from our current brick
 								maxright = Math.min(maxright, Math.abs((shape2.getBounds().bottom-shape.getBounds().top)));
 
 							} else{
 
 								// It's below
 								maxleft = Math.min(maxleft, Math.abs((shape.getBounds().bottom-shape2.getBounds().top)));
 
 							}
 
 						}
 
 					}
 					range [0] = maxright;
 					range [1] = maxleft;
 				}
 
 			}
 			return range;
 		}
 		return null;
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
 
 	private boolean isHitBlockTrue(int x, int y) {
 		for (ShapeDrawable shape : shapes) {
 			Rect bounds = shape.getBounds();
 			if (bounds.contains(x, y)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * Simple method to see if a block is horizontally positioned or vertically.
 	 * returns true if it is horizontal.
 	 */
 	private boolean horizontal(ShapeDrawable rect1) {
 		int diffh = Math.abs(rect1.getBounds().left - rect1.getBounds().right);
 		int diffv = Math.abs(rect1.getBounds().bottom - rect1.getBounds().top);
 		if (diffv > diffh) {
 			return false;
 		} else {
 			return true;
 		}
 
 	}
 
 	public String toString(){
 		StringBuilder s = new StringBuilder();
 		for(ShapeDrawable shape : shapes){
 			s.append(shape.getBounds().left);
 			s.append(":");
 			s.append(shape.getBounds().top);
 			s.append(":");
 			s.append(shape.getBounds().right);
 			s.append(":");
 			s.append(shape.getBounds().bottom);
 			s.append(",");
 		}
 
 		return s.toString();
 
 	}
 
 	public void resetShapes (String s){
 
 		//ArrayList<ShapeDrawable> sh = new ArrayList<ShapeDrawable>();
 		String[] temp1 = s.split(",");
 		shapes.clear();
 		
 		int count = 0;
 		for(int j = 0; j < temp1.length; j++){
 			System.out.println(temp1[j]);
 			String[] temp2 = temp1[j].split(":");
 			for (int i = 0; i < temp2.length; i = i+4) {
 				ShapeDrawable tempshape = new ShapeDrawable();
 				tempshape.setBounds(Integer.parseInt(temp2[i]), Integer.parseInt(temp2[i+1]), Integer.parseInt(temp2[i+2]),Integer.parseInt(temp2[i+3]));
 				System.out.println("Left : " + temp2[i]);
 				System.out.println("top : " + temp2[i+1]);
 				System.out.println("right : " + temp2[i+2]);
 				System.out.println("Bottom : " + temp2[i+3]);
 				tempshape.getPaint().setColor(m_colors[count++]);
 				System.out.println(i);
 				System.out.println(count);
 				shapes.add(tempshape);
 				//temp2 = null;
 				
 			}
 		}
 
 		
 		invalidate();
 
 		
 	}
 	@Override
 	 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 	        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
 	        widthScreen = size;
 	        heightScreen = size;
 	        setMeasuredDimension(size, size);
 	    }
 	
 
 
 }
