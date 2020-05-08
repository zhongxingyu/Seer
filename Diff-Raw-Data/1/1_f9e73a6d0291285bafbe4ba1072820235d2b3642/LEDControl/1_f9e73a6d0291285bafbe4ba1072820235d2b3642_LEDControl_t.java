 package jp.rt_net.android.RTADKminiDemo;
 
import com.example.igum2013project.R;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 
 
 public class LEDControl extends ImageButton implements OnClickListener{
 	/* Constant value sent to the GUI handler in the "what" feild */
 	public static final int UPDATE_OUTPUTPIN_SETTING		 	= 1;
 	public static final int PUSHBUTTON_STATUS_CHANGE	= 2;
 	public static final int POT0_STATUS_CHANGE			= 3;
 	public static final int POT1_STATUS_CHANGE			= 4;
 	public static final int POT2_STATUS_CHANGE			= 5;
 	public static final int POT3_STATUS_CHANGE			= 6;
 	public static final int SERVO_01 					= 7;
 	public static final int SERVO_02					= 8;
 	
 	/* Boolean indicating the current LED status */
 	private boolean on;
 	/* Reference to the user interface handler */
 	private Handler uiHandler;
 	
 	/** Creates instance of the LED button
 	 * 
 	 * @param context The associated context
 	 */
 	public LEDControl(Context context) {
 		super(context);
 		init();
 	}
 
 	/** Creates instance of the LED button
 	 * 
 	 * @param context The associated context
 	 * @param attrs The associated AttributeSet for this item
 	 */
 	public LEDControl(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	/** Initialized the internal state variables and registers the
 	 *  click listener for this button
 	 */
 	private void init() {
 		on = false;
 		this.setOnClickListener(this);
 		uiHandler = null;
 	}
 	
 	/** Set the user interface handler where this class should
 	 * send messages to when a press event occurs
 	 * @param handler If a GUI wants to be notified of this event, it can 
 	 *                send it's handler here (only supports one handler).
 	 */
 	public void setHandler(Handler handler) {
 		uiHandler = handler;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 	}
 	
 	public void onClick(View view) {
 		setState(!on);
 	}
 	
 	/** Gets the current state of the button
 	 * 
 	 * @return boolean that indicates the current button state, true = on
 	 */
 	public boolean getState() {
 		return on;
 	}
 	
 	/** Sets the current state of the button
 	 * 
 	 * @param state true = on, false = off
 	 */
 	public void setState(boolean state) {
 		
 		//save the new value for the LED from the parameter passed in.
 		on = state;
 		
 		if(state == false) {
 			//If the user is turning off the LED, then
 			//  set the image resource for the button to the "led_off" image
 	        this.setImageResource(R.drawable.led_off);
 		} else {
 			//If the user is turning on the LED, then
 			//  set the image resource for the button to the "led_on" image
 			this.setImageResource(R.drawable.led_on);
 		}
 		
 
 		Message ledUpdate = Message.obtain(uiHandler, UPDATE_OUTPUTPIN_SETTING);
 		if(uiHandler != null) {
 			uiHandler.sendMessage(ledUpdate);
 		}
 		
 	
 	}
 }
