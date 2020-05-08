 package com.example.projectbat;
 
 import android.util.Log;
 
 public class BeepTimer implements BeepInterface
 {
	private boolean isListener = true, timing = true;
 	// the time in samples that accounts consecutive onBeep calls to the same beep signal
 	private long gracePeriod = BeepGenerator.beepPeriod * 10;
 	private long time;
 	private boolean awaitFirst = true;
 	private BeepTimerListener listener = null;
 	
 	@Override
 	public void onBeep(long timeInSamples)
 	{
		if(!timing)
			return;
		
 		if(isListener) MainActivity.beepGenerator.play();
 		
 		if(awaitFirst)
 		{
 			awaitFirst = false;
 			time = timeInSamples;
 		}
 		else
 		{
 			if(timeInSamples - time > gracePeriod)
 			{
				timing = false;
 				awaitFirst = true;
 				time = timeInSamples - time;
 				
 				Log.i("TIMER", Long.toBinaryString(time));
 				if(listener != null) listener.timeMeasured(time, isListener);
 			}
 		}
 	}
 	
 	public void start(boolean isListener)
 	{	
 		this.isListener = isListener;
 		awaitFirst = true;
		timing = true;
 	}
 	
 	public void registerListener(BeepTimerListener listener)
 	{
 		this.listener = listener;
 	}
 }
