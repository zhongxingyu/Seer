 package lazygames.trainyoureye;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.drawable.ShapeDrawable;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class GameOneCanvas extends View{
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
 		shapes = new RandomShape[6];
 		shapes[0] = new RandomShape(0, 0, 100, 100, 4);
		shapes[1] = new RandomShape(100, 0, 100, 100, 4);
		shapes[2] = new RandomShape(0, 100, 100, 100, 4);
 		shapes[3] = new RandomShape(100, 100, 100, 100, 4);
 		shapes[4] = new RandomShape(200, 0, 100, 100, 4);
 		shapes[5] = new RandomShape(200, 100, 100, 100, 4);
		drawableShapes = new ShapeDrawable[6];
		for(int i = 0; i < drawableShapes.length; i++){
 			drawableShapes[i] = new ShapeDrawable(shapes[i].GetShape());
		}
 	}
 	@Override protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		drawableShapes[0].draw(canvas);
 	}
 }
