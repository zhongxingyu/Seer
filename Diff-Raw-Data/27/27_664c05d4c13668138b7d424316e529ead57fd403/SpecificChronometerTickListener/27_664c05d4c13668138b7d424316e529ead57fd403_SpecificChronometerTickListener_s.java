 package app.timer;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.os.SystemClock;
 import android.util.Log;
 import android.widget.Chronometer;
 
 public class SpecificChronometerTickListener implements Chronometer.OnChronometerTickListener {
 
 	int beepInterval;
 	boolean sound;
 	int formats;
	static MediaPlayer soundPlayer; 
 	Context context;
 
 	public SpecificChronometerTickListener(Context context, int beepInterval, boolean sound, int formats) {
 		this.beepInterval = beepInterval;
 		this.sound = sound;
 		this.formats = formats;
 		this.context = context;
		SpecificChronometerTickListener.soundPlayer = MediaPlayer.create(context, R.raw.silvaclock);
 	}
 
 	public void onChronometerTick(Chronometer chronometer) {
 		CharSequence text = chronometer.getText();
 		switch(formats) {		       
 		case 1 : chronometer.setText(text.subSequence(text.length()-2, text.length()));
 		chronometer.setTextSize(120);
 		break;
 		case 2 : chronometer.setText(text.subSequence(text.length()-5, text.length()-3));
 		chronometer.setTextSize(120);
 		break;
 		case 3 : chronometer.setText(text.subSequence(0, text.length()-6));
 		chronometer.setTextSize(120);
 		break;
 		default : chronometer.setText(text);
 		chronometer.setTextSize(80);
 		break;
 		}
 
 		if(sound && (((SystemClock.elapsedRealtime() - chronometer.getBase())/1000) + 5) % beepInterval == 0) {
			soundPlayer.start();
 		}      
 	}

	public static boolean releaseMedia() {
		if(soundPlayer != null) {
			soundPlayer.release();
			return true;
		}
		else return false;
	}
 }
