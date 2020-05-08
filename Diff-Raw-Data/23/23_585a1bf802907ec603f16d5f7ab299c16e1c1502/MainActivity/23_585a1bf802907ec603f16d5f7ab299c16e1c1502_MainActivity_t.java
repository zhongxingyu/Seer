 package com.weirdapparatus.mosquito;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.TransitionDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import com.weirdapparatus.mosquito.common.EnergySavingType;
 import com.weirdapparatus.mosquito.common.PlaybackCommand;
 import com.weirdapparatus.mosquito.common.SoundType;
 import com.weirdapparatus.mosquito.service.PlaybackService;
 
 /**
  * Main class for application
  *
  * @author memphis
  */
 public class MainActivity extends Activity {
 
     private static boolean isPlaying = false;
 
     private static SoundType selectedSound = SoundType.DRAGON_FLY; // Default sound
     private static EnergySavingType selectedEnergyType = EnergySavingType.ENDLESS; // Default energy
 
     private static Intent serviceIntent;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         initEnergyOptions(selectedEnergyType);
         initSoundOptions(selectedSound);
         initHandlers();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
        if (serviceIntent != null && isPlaying) {
             final Button playStopButton = (Button) findViewById(R.id.playStopButton);
             playStopButton.setBackgroundResource(R.drawable.stop_button);
         }
     }
 
     private void initEnergyOptions(EnergySavingType selectedEnergyType) {
         final Spinner energyOptions = (Spinner) findViewById(R.id.energyType);
 
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.energy_array, android.R.layout.simple_spinner_item);
 
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         energyOptions.setAdapter(adapter);
 
         energyOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                 MainActivity.selectedEnergyType = EnergySavingType.getTypeById(pos);
                 energyOptions.setSelection(pos);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                 // Do nothing here
             }
         });
         // Select default option or restore selected
         if (selectedEnergyType != null) {
             energyOptions.setSelection(selectedEnergyType.ordinal());
         } else {
             energyOptions.setSelection(3);
         }
     }
 
     private void initSoundOptions(SoundType selectedSound) {
         final Spinner soundOptions = (Spinner) findViewById(R.id.soundType);
 
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.sound_array, android.R.layout.simple_spinner_item);
 
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         soundOptions.setAdapter(adapter);
 
         soundOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                 SoundType newSound = SoundType.getSoundTypeById(pos);
                 soundOptions.setSelection(pos);
 
                 // If we are playing sound, than stop playing
                if (newSound != MainActivity.selectedSound && isPlaying) {
                     final Button playStopButton = (Button) findViewById(R.id.playStopButton);
                     playStopButton.setBackgroundResource(R.drawable.play_button);
                     isPlaying = false;
                    animateBackgroundInReverse(true, 1000);
 
                     // stop playback
                     sentPlaybackServiceCommand(PlaybackCommand.STOP, null, null);
                     MainActivity.selectedSound = newSound;
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                 // Do nothing here
             }
         });
         // Select default option or restore selected
         if (selectedSound != null) {
             soundOptions.setSelection(selectedSound.ordinal());
         } else {
             soundOptions.setSelection(0);
         }
     }
 
     private void initHandlers() {
         final Button playStopButton = (Button) findViewById(R.id.playStopButton);
 
         playStopButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (isPlaying) {
                     // Than stop and put text back to Play
                     playStopButton.setBackgroundResource(R.drawable.play_button);
                     sentPlaybackServiceCommand(PlaybackCommand.STOP, null, null);
                     isPlaying = false;
                    animateBackgroundInReverse(true, 1000);
 
                 } else {
 
                     // Play and set text to stop
                     playStopButton.setBackgroundResource(R.drawable.stop_button);
                     sentPlaybackServiceCommand(PlaybackCommand.START, selectedSound, selectedEnergyType);
                     isPlaying = true;
                    animateBackgroundInReverse(false, 1000);
                 }
             }
         });
     }
 
     private void sentPlaybackServiceCommand(PlaybackCommand command, SoundType soundType, EnergySavingType energySavingType) {
         Intent intent = new Intent(this, PlaybackService.class);
         intent.setAction("com.weirdapparatus.mosquito.service.PlaybackService");
         intent.putExtra("action", command);
         intent.putExtra("soundType", soundType);
         intent.putExtra("playbackType", energySavingType);
 
         serviceIntent = intent;
         startService(intent);
     }
 
    private void animateBackgroundInReverse(boolean reverse, int length) {
         View mainView = findViewById(R.id.mainBackground);
         TransitionDrawable transition = (TransitionDrawable) mainView.getBackground();
 
         if (Boolean.TRUE.equals(reverse)) {
            transition.reverseTransition(length);
         } else {
            transition.startTransition(length);
         }
     }
 }
