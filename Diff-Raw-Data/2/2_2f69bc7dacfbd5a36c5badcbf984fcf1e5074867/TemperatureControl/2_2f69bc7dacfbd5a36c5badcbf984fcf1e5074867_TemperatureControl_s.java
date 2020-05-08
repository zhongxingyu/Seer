 package kb.apps.palinkathermobox;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class TemperatureControl extends View {
 
 
 	public final static int MINIMUM_TEMPERATURE = 8;
 	public final static int MAXIMUM_TEMPERATURE = 24; //TODO : Confirm values
 	public final static double PERCENTAGE_TO_ANGLE =  1.8;
 	
 	public static final String PREFERENCE_NAME = "PALINCAR_REFERENCE";    /** prefernece name */
 	private static final String SAVED_PERCENTAGE_KEY = "PathPercentage";  /** stored temperature key */
   
 	private static final double NO_INPUT_TEMP = -99;                /** there is no input temperature */
 	private static final String NO_INPUT_TEMP_STRING = "-.-- C";   /** until the device is not connected this value is shown */
 
 	private Paint paint;
 	private int pathPercentage; 
 	private double blackControlDegrees;
 	
 	private float meterXPosition;
 	private float meterYPosition;
 	private int height;
 	private int width;
 	private int referenceSize;
 	private boolean isMeterPressed;
 	private ApplicationEvents event;
 	private Context context;
 	
   private volatile double temperatureLevel = -99;
 	
 	public TemperatureControl(Context context, AttributeSet atSet)
 	{
 		super(context,atSet);
 		paint = new Paint();
 		pathPercentage = 50;
 		meterXPosition = 0;
 		meterYPosition = 0;
 		isMeterPressed = false;
 		referenceSize = 160;
 		
     // load the stored temperature data
 		this.context = context;
 		loadPreferences(this.context);
 	}
 	
 	//public void onDestroy() {
 	  //savePrefrences(context);
 	//}
 	
 	public void setEventHandler(ApplicationEvents event) {
 	  this.event = event;
 	}
 	
 	public void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		paint.setAntiAlias(true);
 		
 		//Pre-calculate sizes
 		this.height = this.getHeight();
 		this.width = this.getWidth();
 		
 		//Support for small screens, re-scaling
		if(this.width > 400 && referenceSize == 160 )
 		{
 			referenceSize *= 0.7;
 		}
 		canvas.drawARGB(255, 20, 20, 20);
 		
 		//Main, big yellow circle.
 		paint.setARGB(255, 240, 230, 20);
 		canvas.drawCircle(this.width/2, this.height/2, referenceSize ,paint);
 		
 		//inner black circle, at 0.8125 of reference size
 		paint.setARGB(255, 0, 0, 0);
 		canvas.drawCircle(this.width/2, this.height/2, (float) (referenceSize*0.8125) ,paint);
 		canvas.drawLine(this.width/2 - referenceSize, this.height/2, this.width/2 + referenceSize, this.height/2, paint);
 		canvas.drawLine(this.width/2, this.height/2, this.width/2, this.height/2 + referenceSize, paint);
 		paint.setARGB(255, 240, 230, 20);
 		//Inner yellow area, at 0.7875 of reference size
 		canvas.drawCircle(this.width/2, this.height/2, (float) (referenceSize*0.7875),paint);
 		
 		
 		blackControlDegrees = (pathPercentage * PERCENTAGE_TO_ANGLE) * Math.PI/180;
 		paint.setTextAlign(Align.CENTER);
 		paint.setTextSize(45);
 		
 		//Draw small black circle
 		//depends on temeperatures, radius is set at 0.90625 of reference size 
 		double meterXDifference = (referenceSize * 0.90625) * Math.cos(blackControlDegrees);
 		double meterYDifference = (referenceSize * 0.90625) * Math.sin(blackControlDegrees);
 		meterXPosition = (float) (this.width/2 + meterXDifference); 
 		meterYPosition = (float) (this.height/2 + meterYDifference);
 		// Set color of the meter, depending on user interaction
 		if(isMeterPressed)
 		{
 			paint.setARGB(255, 190, 40, 40);
 			
 		}
 		else
 		{
 			paint.setARGB(255, 40, 40, 40);
 		}
 		//Meter size is set at 0.09375 of reference size
 		canvas.drawCircle(meterXPosition, meterYPosition, (float) (referenceSize * 0.09375), paint);
 		
 		//Text-Drawing here
 		paint.setARGB(255, 40, 40, 40);
 		String temperature = String.format("%.01f C", temperatureLevel);
 		if (temperatureLevel == NO_INPUT_TEMP) {
 		  temperature = NO_INPUT_TEMP_STRING;
 		}
 		canvas.drawText(temperature, this.width/2, (this.height/2) + ((paint.getTextSize() / 2) - 4), paint);
 		
 		//Meter values drawing
 		// Values are outside at 1.15625 of reference isze
 		paint.setARGB(255, 240, 230, 20);
 		paint.setTextSize(18);
 		int meterTemperature = MINIMUM_TEMPERATURE;
 		float textDegrees = 0;
 		String currenttemp;
 		while(meterTemperature <= MAXIMUM_TEMPERATURE)
 		{
 			currenttemp = String.valueOf(meterTemperature);
 			currenttemp += "C";
 			canvas.drawText(currenttemp, 
 							(float) (this.width/2 + (referenceSize * 1.15625) * Math.cos(textDegrees * Math.PI/180)), 
 							(float) ( (this.height/2 + 10) + (referenceSize * 1.15625) * Math.sin(textDegrees * Math.PI/180)), 
 							paint);
 			meterTemperature += 2;
 			textDegrees += 22.5;
 		}
 		
 		
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		
 		//Hit slack is set at X2 radius of meter ball
 		if(event.getAction() == MotionEvent.ACTION_DOWN)
 		{		
 			double hitRadius = Math.sqrt( Math.pow( (event.getX() - meterXPosition), 2) + Math.pow( (event.getY() - meterYPosition), 2) );
 			if(hitRadius < (referenceSize * 0.1875) )
 			{
 				isMeterPressed = true;
 				invalidate();
 				return true;
 			}
 
 		}
 		else if(event.getAction() == MotionEvent.ACTION_MOVE)
 		{
 			double angleDegrees = Math.atan2(event.getY() - this.height/2, event.getX() - this.width/2 );
 			angleDegrees = Math.abs(angleDegrees) * 180/Math.PI;
 			pathPercentage = (int) (angleDegrees * (1/PERCENTAGE_TO_ANGLE));
 			invalidate();
 			return true;
 		}
 		else if(event.getAction() == MotionEvent.ACTION_UP)
 		{
 			isMeterPressed = false;
 			invalidate();
 			//Here, call the BT service and send the new temperature!!
 			if (this.event != null) {			  
 			  this.event.onTemperatureChanged(this.getTemperatureLevel());
 			  savePrefrences(context);
 			}
 			return true;
 		}
 		
 		return super.onTouchEvent(event);
 	}
 	
 	
 	public double getTemperatureLevel()
 	{
 		int range = MAXIMUM_TEMPERATURE - MINIMUM_TEMPERATURE;
 		return (MINIMUM_TEMPERATURE + range * ( (double)pathPercentage/100 )); 
 		
 	}
 	
 	public void setCurrentTemperature(double temperature)
 	{
 	  temperatureLevel = temperature;
 	  invalidate();
 	}
 	
 	private void loadPreferences(Context context) {
 	   SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
 	   pathPercentage = Integer.valueOf(sharedPreferences.getInt(SAVED_PERCENTAGE_KEY, 75));	    
 	}
 	
 	private void savePrefrences(Context context) {
     SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
     SharedPreferences.Editor editor = sharedPreferences.edit();
     editor.putInt(SAVED_PERCENTAGE_KEY, pathPercentage);
     editor.commit();
 	}
 }
