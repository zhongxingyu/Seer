 package lazygames.trainyoureye;
 
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.PathShape;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public class GameOneCanvas extends View{
 	private static final int alpha = 255;
 	int widthRectangle;
 	int heightRectangle;
 	
 	RandomShape[] shapes;
 	ShapeDrawable[] drawableShapes;
 	public GameOneCanvas (Context context) {
 		super(context);
 		setup();
 	}
 	public GameOneCanvas (Context context, AttributeSet attributes) {
 		super(context, attributes);
 		setup();
 	}
 	public GameOneCanvas (Context context, AttributeSet attributes, int style) {
 		super(context, attributes, style);
 		setup();
 	}
 	private void setup(){
 		
 		 /* commented part needs testing, replaces the long list of array initializations */
 
 	/*
 		int difficulty = 4;
 		int width = 100;
 		int height = 100;
 		*/
 		
 		shapes = new RandomShape[6];
 		drawableShapes = new ShapeDrawable[6];
 		shapes[0] = new RandomShape(0, 0, 100, 100, 4);
 		shapes[1] = new RandomShape(0, 100, 100, 100, 4);
 		shapes[2] = new RandomShape(100, 0, 100, 100, 4);
 		shapes[3] = new RandomShape(100, 100, 100, 100, 4);
 		shapes[4] = new RandomShape(200, 0, 100, 100, 4);
 		shapes[5] = new RandomShape(200, 100, 100, 100, 4);
		getWidth();
 	/*
 		for(int i = 0; i < shapes.length; i++)
 			shapes[i] = new RandomShape(((int)((double)i/2))*width, (i%2)*height, width, height, difficulty);
 	*/
		for(int i = 0; i < drawableShapes.length; i++)
 			drawableShapes[i] = new ShapeDrawable(shapes[i].getShape());
		drawableShapes[0].getPaint().setColor(Color.argb(alpha, 0, 0, 255));
		for(int i = 1; i < drawableShapes.length; i++)
 			drawableShapes[i].getPaint().setColor(Color.argb(alpha, 255, 0, 0));
 	
 	}
 	@Override protected void onDraw(Canvas canvas) {
 		//this.blue = new Paint();
 		//this.blue.setARGB(255, 0, 0, 255);
 		
 		super.onDraw(canvas);
 
 		//RandomShape random_shape = new RandomShape(0, 0, 100, 100, 4);
 		//ShapeDrawable shape = new ShapeDrawable(random_shape.getShape());
 
 		//drawableShapes[0].draw(canvas);
 		Paint paint = new Paint();
 		paint.setARGB(255, 255, 0, 0);
 		
 		Path p = new Path();
 		p.moveTo(50, 50);
 		p.lineTo(60, 50);
 		p.lineTo(60, 60);
 		p.lineTo(50, 60);
 		p.close();
 	    
 <<<<<<< HEAD
 		PathShape test = new PathShape(p, 100, 100);
 =======
 		PathShape test = new PathShape(p, canvas.getWidth(), canvas.getHeight());
 >>>>>>> d83c792965b0c7c93917ead520f3f4c50679df39
 		ShapeDrawable test2 = new ShapeDrawable(test);
 		test2.getPaint().set(paint);
 		test2.setBounds(0, 0, 100, 100);
 		test2.draw(canvas);
 	
 		canvas.save();
 		canvas.restore();
 
 	}
 }
