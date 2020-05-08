 package net.thiagoalz.hermeto.panel.controls.listeners;
 
 import net.thiagoalz.hermeto.panel.GameManager;
 import android.content.Context;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class BPMBarListener implements SeekBar.OnSeekBarChangeListener  {
 	private static final int BPM_SCALE = 4;	
 	
 	private GameManager gameManager;
 	private TextView bpmView;
 	private Context context;
 	private int progress;
 	
 	public BPMBarListener(GameManager gameManager, TextView bpmView, Context context) {
 		this.context = context;
 		this.gameManager = gameManager;
 		this.bpmView = bpmView;
 	}
 	
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
 		this.progress = progress;
 		bpmView.setText("BPM: " + progress * BPM_SCALE);
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		if (gameManager.isPlaying()) {
 			gameManager.setBPM(progress * BPM_SCALE);
 		} else {
 			gameManager.updateBPM(progress * BPM_SCALE);
 		}
 		
 	}
 
 }
