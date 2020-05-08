 package winning.pwnies.recreativity;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class ContentView extends View {
 
 	public ContentView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public ContentView(Context context) {
 		super(context);
 	}
 
 	private Content data;
 
 	public void setContent(Content c) {
 		data = c;
 	}
 	
 	@Override
 	public void onDraw(Canvas canvas) {
		canvas.drawARGB(255, 42, 154, 110);
 		if (data != null) {
 			data.draw(canvas);
 		}
 	}
 	
 }
