 /*
     LED tester for Android platform
     Copyright (C) 2009 Rafał Rzepecki
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package pl.blip.divide.ledtester;
 
 import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
 import android.app.Notification;
 import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class LedTester extends Activity {
     public class LabelUpdatingOnSeekBarChangeListener implements
 			OnSeekBarChangeListener {
 		private TextView myLabel;
 		public LabelUpdatingOnSeekBarChangeListener(TextView label) {
 			myLabel = label;
 		}
 
 		@Override
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			myLabel.setText(Integer.toString(progress));
 			
 			updateLedColor();
 		}
 
 		@Override
 		public void onStartTrackingTouch(SeekBar seekBar) {
 		}
 
 		@Override
 		public void onStopTrackingTouch(SeekBar seekBar) {
 		}
 	}
 
 	private SeekBar redBar;
 	private SeekBar greenBar;
 	private SeekBar blueBar;
 	private TextView redLabel;
 	private TextView greenLabel;
 	private TextView blueLabel;
 	private NotificationManager notificationManager;
 	private Notification notification;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         redBar = (SeekBar) findViewById(R.id.red);
         greenBar = (SeekBar) findViewById(R.id.green);
         blueBar = (SeekBar) findViewById(R.id.blue);
         
         redLabel = (TextView) findViewById(R.id.red_label);
         greenLabel = (TextView) findViewById(R.id.green_label);
         blueLabel = (TextView) findViewById(R.id.blue_label);
         
         redBar.setOnSeekBarChangeListener(new LabelUpdatingOnSeekBarChangeListener(redLabel));
         greenBar.setOnSeekBarChangeListener(new LabelUpdatingOnSeekBarChangeListener(greenLabel));
         blueBar.setOnSeekBarChangeListener(new LabelUpdatingOnSeekBarChangeListener(blueLabel));

         notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notification = new Notification();
         notification.ledOffMS = 0;
         notification.ledOnMS = 1000;
         notification.flags = Notification.FLAG_SHOW_LIGHTS;
         
         printSystemInfo();

        showDialog(0);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	return new AlertDialog.Builder(this)
    		.setMessage(R.string.welcome_text)
    		.setPositiveButton(android.R.string.ok, new OnClickListener() {
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
	    			dialog.dismiss();
	    		}
    	}).create();
     }
 
 	private void printSystemInfo() {
 		Log.i("LedTester", "System info:\n" +
 				"\tBoard: " + Build.BOARD +
 				"\n\tBrand: " + Build.BRAND +
 				"\n\tDevice: " + Build.DEVICE +
 				"\n\tDisplay: " + Build.DISPLAY +
 				"\n\tFingerprint: " + Build.FINGERPRINT +
 				"\n\tHost: " + Build.HOST +
 				"\n\tID: " + Build.ID +
 				"\n\tModel: " + Build.MODEL +
 				"\n\tProduct: " + Build.PRODUCT + 
 				"\n\tTags: " + Build.TAGS + 
 				"\n\tType: " + Build.TYPE +
 				"\n\tUser: " + Build.USER
 		);
 	}
 
 	public void updateLedColor() {
 		int red = redBar.getProgress();
 		int green = greenBar.getProgress();
 		int blue = blueBar.getProgress();
 		
 		int color = 0xff000000 | (red << 16) | (green << 8) | blue;
 		notification.ledARGB = color;
 		notificationManager.notify(0, notification);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		notificationManager.cancel(0);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updateLedColor();
 	}
 }
