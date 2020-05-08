 package info.ss12.audioalertsystem;
 
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.CompoundButton;
 import android.widget.Switch;
 
 public class ButtonController implements OnClickListener, OnTouchListener
 {
 	private final String TAG = "Button Controller";
 
 	@Override
 	public void onClick(View v)
 	{
 		handleButtonToggle(v);
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event)
 	{
 		int eventId = event.getAction();
 
 		if (eventId == MotionEvent.ACTION_UP)
 		{
 			//For UI purposes
 			if(v instanceof Switch)
 			{
 				Switch temp = (Switch) v;
 				if(temp.isChecked())
 					temp.setChecked(false);
 				else
 					temp.setChecked(true);
 			}
 			handleButtonToggle(v);
 			return true;
 		}
 		return false;
 	}
 
 	public void handleButtonToggle(View v)
 	{
 		int id = v.getId();
		switch (id)
		{
		case R.id.mic_switch:
 			Log.d(TAG, "press");
 			Switch micS = (Switch) v;
 
 			if (micS.isChecked())
 				Log.d(TAG, "switch on");
 			else
 				Log.d(TAG, "switch off");

			break;

		
 			
		default:
 			Log.d("swtich button test", "default");
			break;
 		}
 	}
 
 }
