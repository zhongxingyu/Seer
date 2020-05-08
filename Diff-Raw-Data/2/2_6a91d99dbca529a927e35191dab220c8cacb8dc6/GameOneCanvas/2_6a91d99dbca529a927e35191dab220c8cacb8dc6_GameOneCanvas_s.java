 package lazygames.trainyoureye;
 
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.drawable.ShapeDrawable;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class GameOneCanvas extends View{
 	private static final int alpha = 255;
 	int widthRectangle;
 	int heightRectangle;
 	
 	RandomShape[] shapes;
 	ShapeDrawable[] drawableShapes;
 	public GameOneCanvas (Context context) {
 		super(context);
 	}
 	public GameOneCanvas (Context context, AttributeSet attributes) {
 		super(context, attributes);
 	}
 	public GameOneCanvas (Context context, AttributeSet attributes, int style) {
 		super(context, attributes, style);
 	}
 	private void setup(){
 		
 		 /* commented part needs testing, replaces the long list of array initializations */
 
 	
 		
 		if(RandomShape.getCanvasWidth() !=0 && RandomShape.getCanvasHeight() != 0){
 			int difficulty = 6;
 			int width, height;
 			if(RandomShape.getCanvasWidth()*2<=RandomShape.getCanvasHeight()*3){
 				width = RandomShape.getCanvasWidth()/2;
 				height = width;
 			} else {
 				height = RandomShape.getCanvasHeight()/3;
 				width = height;
 			}
 			shapes = new RandomShape[6];
 			drawableShapes = new ShapeDrawable[6];
 			/*
 			shapes[0] = new RandomShape(0, 0, 100, 100, 4);
 			shapes[1] = new RandomShape(0, 100, 100, 100, 4);
 			shapes[2] = new RandomShape(100, 0, 100, 100, 4);
 			shapes[3] = new RandomShape(100, 100, 100, 100, 4);
 			shapes[4] = new RandomShape(200, 0, 100, 100, 4);
 			shapes[5] = new RandomShape(200, 100, 100, 100, 4);
 			*/
 		
 			for(int i = 1; i < shapes.length; i++)
 				shapes[i] = new RandomShape((i%2)*height, ((int)((double)i/2))*width, width, height, difficulty);
 			Random r = new Random();
			int correctshape = r.nextInt(5+1); // shapes[1 tot 5]
 			shapes[0] = new RandomShape(0, 0, shapes[correctshape]);
 		
 			for(int i = 0; i < drawableShapes.length; i++){
 				drawableShapes[i] = new ShapeDrawable(shapes[i].getShape());
 				drawableShapes[i].getPaint().setColor(Color.argb(alpha, 255, 0, 0));
 				drawableShapes[i].setBounds(0, 0, RandomShape.getCanvasWidth(), RandomShape.getCanvasHeight());
 			}
 			drawableShapes[0].getPaint().setColor(Color.argb(alpha, 0, 230, 230));
 		}
 	}
 	@Override protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		if(RandomShape.getCanvasHeight() == 0 && RandomShape.getCanvasWidth() == 0){
 			RandomShape.setCanvasHeight(canvas.getHeight());
 			RandomShape.setCanvasWidth(canvas.getWidth());
 		}
 		setup();
 		
 
 		//RandomShape random_shape = new RandomShape(0, 0, 100, 100, 4);
 		//ShapeDrawable shape = new ShapeDrawable(random_shape.getShape());
 		if(drawableShapes != null){
 			for(int i = 0; i < drawableShapes.length; i++){
 				drawableShapes[i].draw(canvas);
 			}
 		}
 		
 		canvas.save();
 		canvas.restore();
 
 	}
 }
