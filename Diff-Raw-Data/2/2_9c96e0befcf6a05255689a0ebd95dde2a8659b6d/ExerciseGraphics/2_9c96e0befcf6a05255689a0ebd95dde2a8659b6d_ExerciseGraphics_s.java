 package cornell.eickleapp;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import cornell.eickleapp.R;
 import cornell.eickleapp.R.drawable;
 import android.app.ActionBar.LayoutParams;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Picture;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.view.View.OnClickListener;
 
 public class ExerciseGraphics extends View {
 
 	Bitmap chicken, badCircle;
 	public int drinkCount = 0;
 	public int chickenCount = 0;
 	public int month = 0, day = 0;
 	// initiated at an offset to see the horizontal and verticle axis
 	public float posX = 0, posY = 0, zoomVal = 0.75f;
 	public float plusX, plusY;
 	public float minusX, minusY;
 	public Bitmap plus = BitmapFactory.decodeResource(getResources(),
 			R.drawable.plus);
 	public Bitmap minus = BitmapFactory.decodeResource(getResources(),
			R.drawable.minus);
 	// Bitmap BufferBitmap = Bitmap.createBitmap(1000, 1000,
 	// Bitmap.Config.ARGB_8888);
 	// Canvas BufferCanvas = new Canvas(BufferBitmap);
 	ArrayList<Integer> daysDrinkList = new ArrayList<Integer>();
 	ArrayList<Integer> daysExercisedList = new ArrayList<Integer>();
 	ArrayList<Double> averageExerciseQualityList = new ArrayList<Double>();
 
 	static int xBoundMax = 0;
 	static int yBoundMax = 0;
 	
 	public ExerciseGraphics(Context context, ArrayList<Integer> d,
 			ArrayList<Integer> e,
 			ArrayList<Double> q) {
 		super(context);
 		// TODO Auto-generated constructor stub
 		daysDrinkList=d;
 		daysExercisedList=e;
 		averageExerciseQualityList=q;
 		
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		super.onDraw(canvas);
 
 		canvas.save();
 		canvas.scale(zoomVal, zoomVal, canvas.getWidth() / 2,
 				canvas.getHeight() / 2);
 		int yAxisMax = canvas.getHeight();
 		int xAxisMax = canvas.getWidth();
 		Paint axis = new Paint();
 		axis.setColor(Color.BLACK);
 		axis.setStrokeWidth(10);
 
 		Paint text = new Paint();
 		text.setTextSize(15);
 		// draws the x-axis
 		canvas.drawLine(20 + posX, yAxisMax + posY,
 				xAxisMax * daysDrinkList.size()/2 + posX, yAxisMax + posY, axis);
 		
 		xBoundMax=xAxisMax * daysDrinkList.size()/2;
 		yBoundMax=yAxisMax;
 		int yScale = 7;
 		float yAxisIncrement = (yAxisMax / yScale)-10;
 		// draws the increments on Y axis
 		Paint thinLines = new Paint();
 		thinLines.setStrokeWidth(5);
 		//draws Y axis
 		canvas.drawLine(20+posX, yAxisMax + posY, 20+posX, 0, axis);
 
 		// draw legends
 		Paint legendPaint = new Paint();
 		Rect legendsRect = new Rect();
 		// drink legend
 		legendsRect.set(canvas.getWidth() - 125, 25, canvas.getWidth() - 100,
 				50);
 		legendPaint.setColor(Color.rgb(247, 144, 30));
 		canvas.drawRect(legendsRect, legendPaint);
 		
 		legendPaint.setTextSize(20);
 		legendPaint.setColor(Color.BLACK);
 		canvas.drawText("Drinking", legendsRect.right,
 				legendsRect.centerY(), legendPaint);
 
 		// exercise legend
 		legendPaint.setColor(Color.rgb(0, 153, 204));
 		legendsRect.set(canvas.getWidth() - 125, 50, canvas.getWidth() - 100,
 				75);
 		canvas.drawRect(legendsRect, legendPaint);
 		legendPaint.setColor(Color.BLACK);
 		legendPaint.setTextSize(20);
 		canvas.drawText("Exercise", legendsRect.right, legendsRect.centerY(),
 				legendPaint);
 		// grade legend
 		Paint gradeLegendPaint=new Paint();
 		Rect gradeLegendBound=new Rect();
 		gradeLegendPaint.setTextSize(20);
 		gradeLegendPaint.setTextAlign(Align.CENTER);
 		gradeLegendPaint.setColor(Color.RED);
 		String gradeLegend="A-F";
 		gradeLegendPaint.getTextBounds(gradeLegend, 0, gradeLegend.length(), gradeLegendBound);
 		canvas.drawText(gradeLegend, legendsRect.centerX(), legendsRect.centerY()+legendsRect.height()/2+gradeLegendBound.height()+5,
 				gradeLegendPaint);
 		gradeLegendPaint.setColor(Color.BLACK);
 		gradeLegendPaint.setTextAlign(Align.LEFT);
 		canvas.drawText("Quality",legendsRect.centerX()+gradeLegendBound.width()/2, legendsRect.centerY()+legendsRect.height()/2+gradeLegendBound.height()+5,
 				gradeLegendPaint);
 
 		text.setTextSize(20);
 		text.setColor(Color.BLACK);
 		for (int n = 0; n < yScale; n++) {
 			canvas.drawLine(10 + posX, n * yAxisIncrement+yAxisIncrement , 50 + posX, n
 					* yAxisIncrement+yAxisIncrement , thinLines);
 
 			Rect bounds = new Rect();
 			String scaleData = "" + (yScale - n);
 			text.getTextBounds(scaleData, 0, scaleData.length(), bounds);
 
 			canvas.drawText("" + (yScale - n), -10 + posX, n * yAxisIncrement+yAxisIncrement
 					+ posY + (bounds.width() / 2), text);
 		}
 		canvas.rotate(90);
 		text.setTextSize(30);
 		canvas.drawText("Days", canvas.getHeight() / 2 + posY, 35 - posX, text);
 		canvas.rotate(-90);
 
 		axis.setColor(Color.RED);
 		JSONArray tempList = null;
 		float xPosition = 100;
 		float widthOfBar = 50;
 		float spaceBetweenWeeks = 50;
 			// tempList=%%%%.getJSONArray("week");
 			for (int n = 0; n < daysDrinkList.size(); n++) {
 				String weekNumber = ""+(n+1);
 				int daysDrink = daysDrinkList.get(n);
 				int daysExercise = daysExercisedList.get(n);
 				double rawGrade = averageExerciseQualityList.get(n);
 				String grade="";
 				if (rawGrade>=90)
 					grade="A";
 				else if (rawGrade>=80&&rawGrade<90)
 					grade="B";
 				else if (rawGrade>=70&&rawGrade<80)
 					grade="C";
 				else if (rawGrade>=60&&rawGrade<70)
 					grade="D";
 				else 
 					grade="F";
 
 				// draws days drank
 				Rect alcBarRect = new Rect();
 				alcBarRect.set((int) (xPosition + posX),
 						(int) ((yScale - daysDrink) * yAxisIncrement + posY+yAxisIncrement),
 						(int) (xPosition + widthOfBar + posX),
 						(int) (yAxisMax + posY));
 
 				Paint alcBarPaint=new Paint();
 				alcBarPaint.setColor(Color.rgb(247, 144, 30));
 				canvas.drawRect(alcBarRect, alcBarPaint);
 
 
 
 				// draws days exercised
 				Paint exerciseBarPaint=new Paint();
 				exerciseBarPaint.setColor(Color.rgb(0, 153, 204));
 				// gets the bounds of the text for centering
 				
 				Rect bounds = new Rect();
 				String weekText = "Week " + weekNumber;
 				text.getTextBounds(weekText, 0, weekText.length(), bounds);
 				RectF exerciseBarRect=new RectF();
 				exerciseBarRect.set(xPosition + widthOfBar + posX+5,
 						(yScale - daysExercise) * yAxisIncrement + posY+yAxisIncrement,
 						xPosition + (widthOfBar * 2) + posX, yAxisMax + posY);
 				canvas.drawRect(exerciseBarRect,exerciseBarPaint);
 				// draws week
 				axis.setColor(Color.BLACK);
 				axis.setTextSize(20);
 				canvas.drawText(weekText, xPosition + widthOfBar + posX
 						- (bounds.width() / 2), yAxisMax + 50 + posY, axis);
 				// draws grade
 
 				Rect textBound = new Rect();
 				text.setColor(Color.BLACK);
 				text.setTextSize(10);
 				text.setColor(Color.RED);
 				text.setTextSize(20);
 				canvas.drawText(grade, xPosition + widthOfBar +widthOfBar/4+ posX, (yScale - daysExercise)
 						* yAxisIncrement + posY - 10+yAxisIncrement, text);
 				xPosition += 200;
 			}
 
 		plusX = canvas.getWidth() - plus.getWidth() - 50;
 		plusY = canvas.getHeight() - plus.getHeight();
 		minusX = canvas.getWidth() - minus.getWidth();
 		minusY = canvas.getHeight() - minus.getHeight();
 
 		canvas.restore();
 		// canvas.drawBitmap(BufferBitmap, (float) -posX, (float) -posY, null);
 		// canvas.drawColor(Color.parseColor("#7b9aad"));
 
 		requestLayout();
 
 	}
 	public void setPosX(float input){
 		if (-input<0)
 			input=0;
 		if (-input>xBoundMax)
 			input=-xBoundMax;
 		posX=input;
 		
 	}
 	public void setPosY(float input){
 
 		posY=input;
 	}
 
 }
