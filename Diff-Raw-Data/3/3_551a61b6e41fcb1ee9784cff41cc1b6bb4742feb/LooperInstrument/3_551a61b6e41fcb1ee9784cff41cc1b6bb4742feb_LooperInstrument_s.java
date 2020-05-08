 package com.davidjennes.ElectroJam;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ToggleButton;
 
 public class LooperInstrument extends Activity {
 	private SoundManager m_soundManager;
 	private Map<Integer, Integer> m_buttonSound, m_buttonProgress;
 	private ProgressDialog m_progressDialog;
     
     public LooperInstrument() {
         m_buttonSound = new HashMap<Integer, Integer>();
         m_buttonProgress = new HashMap<Integer, Integer>();
         
         // Connect buttons to sounds
         m_buttonSound.put(R.id.LooperDrum1, R.raw.drum1);
         m_buttonSound.put(R.id.LooperDrum2, R.raw.drum2);
         m_buttonSound.put(R.id.LooperDrum3, R.raw.drum3);
         m_buttonSound.put(R.id.LooperDrum4, R.raw.drum4);
         
         m_buttonSound.put(R.id.LooperSnare1, R.raw.snare1);
         m_buttonSound.put(R.id.LooperSnare2, R.raw.snare2);
         m_buttonSound.put(R.id.LooperSnare3, R.raw.snare3);
         m_buttonSound.put(R.id.LooperSnare4, R.raw.snare4);
         
         m_buttonSound.put(R.id.LooperBass1, R.raw.bass1);
         m_buttonSound.put(R.id.LooperBass2, R.raw.bass2);
         m_buttonSound.put(R.id.LooperBass3, R.raw.bass3);
         m_buttonSound.put(R.id.LooperBass4, R.raw.bass4);
         
         m_buttonSound.put(R.id.LooperRythmic1, R.raw.rythmic1);
         m_buttonSound.put(R.id.LooperRythmic2, R.raw.rythmic2);
         m_buttonSound.put(R.id.LooperRythmic3, R.raw.rythmic3);
         m_buttonSound.put(R.id.LooperRythmic4, R.raw.rythmic4);
         
         m_buttonSound.put(R.id.LooperLead1, R.raw.lead1);
         m_buttonSound.put(R.id.LooperLead2, R.raw.lead2);
         m_buttonSound.put(R.id.LooperLead3, R.raw.lead3);
         m_buttonSound.put(R.id.LooperLead4, R.raw.lead4);
         
         m_buttonSound.put(R.id.LooperFX1, R.raw.fx1);
         m_buttonSound.put(R.id.LooperFX2, R.raw.fx2);
         m_buttonSound.put(R.id.LooperFX3, R.raw.fx3);
         m_buttonSound.put(R.id.LooperFX4, R.raw.fx4);
         
         m_buttonSound.put(R.id.TriggerButton1, R.raw.trigger1);
         m_buttonSound.put(R.id.TriggerButton2, R.raw.trigger2);
         m_buttonSound.put(R.id.TriggerButton3, R.raw.trigger3);
         m_buttonSound.put(R.id.TriggerButton4, R.raw.trigger4);
         m_buttonSound.put(R.id.TriggerButton5, R.raw.trigger5);
         m_buttonSound.put(R.id.TriggerButton6, R.raw.trigger6);
 
         // Connect buttons to progress bars
         m_buttonProgress.put(R.id.LooperDrum1, R.id.ProgressDrum1);
         m_buttonProgress.put(R.id.LooperDrum2, R.id.ProgressDrum2);
         m_buttonProgress.put(R.id.LooperDrum3, R.id.ProgressDrum3);
         m_buttonProgress.put(R.id.LooperDrum4, R.id.ProgressDrum4);
         
         m_buttonProgress.put(R.id.LooperSnare1, R.id.ProgressSnare1);
         m_buttonProgress.put(R.id.LooperSnare2, R.id.ProgressSnare2);
         m_buttonProgress.put(R.id.LooperSnare3, R.id.ProgressSnare3);
         m_buttonProgress.put(R.id.LooperSnare4, R.id.ProgressSnare4);
         
         m_buttonProgress.put(R.id.LooperBass1, R.id.ProgressBass1);
         m_buttonProgress.put(R.id.LooperBass2, R.id.ProgressBass2);
         m_buttonProgress.put(R.id.LooperBass3, R.id.ProgressBass3);
         m_buttonProgress.put(R.id.LooperBass4, R.id.ProgressBass4);
 
         m_buttonProgress.put(R.id.LooperRythmic1, R.id.ProgressRythmic1);
         m_buttonProgress.put(R.id.LooperRythmic2, R.id.ProgressRythmic2);
         m_buttonProgress.put(R.id.LooperRythmic3, R.id.ProgressRythmic3);
         m_buttonProgress.put(R.id.LooperRythmic4, R.id.ProgressRythmic4);
         
         m_buttonProgress.put(R.id.LooperLead1, R.id.ProgressLead1);
         m_buttonProgress.put(R.id.LooperLead2, R.id.ProgressLead2);
         m_buttonProgress.put(R.id.LooperLead3, R.id.ProgressLead3);
         m_buttonProgress.put(R.id.LooperLead4, R.id.ProgressLead4);
         
         m_buttonProgress.put(R.id.LooperFX1, R.id.ProgressFX1);
         m_buttonProgress.put(R.id.LooperFX2, R.id.ProgressFX2);
         m_buttonProgress.put(R.id.LooperFX3, R.id.ProgressFX3);
         m_buttonProgress.put(R.id.LooperFX4, R.id.ProgressFX4);
     }
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.instrument_looper);
         
         // show progress dialog during loading
         m_progressDialog = ProgressDialog.show(this, getString(R.string.working), getString(R.string.loading_sounds), true, false);
         
         // initialize and load sounds
         new AsyncTask<Void, Void, Void>() {
     		protected Void doInBackground(Void... params) {
 				m_soundManager = new SoundManager(getBaseContext());
 				
 				// load sounds and find progressbars
 		        for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet()) {
 		        	int sound = m_soundManager.loadSound(entry.getValue());
 		        	entry.setValue(sound);
 		        	
 		        	if (m_buttonProgress.containsKey(entry.getKey()))
 		        		m_soundManager.setProgressBar(sound, findViewById(m_buttonProgress.get(entry.getKey())));
 		        }
     			return null;
     		}
     		
     		protected void onPostExecute(Void param) {
     			m_progressDialog.dismiss();
     			m_progressDialog = null;
     		}
     	}.execute();
     }
     
     /**
      * Do not reload sounds on screen rotation 
      */
     public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		setContentView(R.layout.instrument_looper);
 		
 		for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet()) {
 			int sound = entry.getValue();
 			
 			// light up buttons for playing sounds
 			if (m_soundManager.isPlaying(sound))
 				((ToggleButton) findViewById(entry.getKey())).setChecked(true);
 			
 			// re-associate progress bars
			m_soundManager.setProgressBar(sound, findViewById(m_buttonProgress.get(entry.getKey())));
 		}
     }
     
     public void onDestroy() {
         super.onDestroy();
         
         for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet())
     		m_soundManager.unloadSound(entry.getValue());
     }
     
     /**
      * Called on click of one of the buttons
      * @param view The clicked button
      */
     public void buttonClick(View view) {
     	if (view == null)
     		return;
     	
     	// looped is stored in tag field
     	boolean looped = view.getTag().equals("1");
     	
     	// play/stop depending on ToggleButton state, otherwise just play
     	boolean play = true;
     	if (view instanceof ToggleButton && !((ToggleButton) view).isChecked())
     		play = false;
     	
     	// either play or stop
     	if (play)
     		m_soundManager.playSound(m_buttonSound.get(view.getId()), looped);
 	    else
 	    	m_soundManager.stopSound(m_buttonSound.get(view.getId()));
     }
 }
