 package tk.zeryter.PlaySheetAndroid;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 /**
  * Owen Holloway
  * ZerytSoft
  * Date: 25/11/13
  */
 
 public class BasicDetailsFragment extends Fragment {
 
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.basicdetails, container, false);
     }
 
     TextView setAgePrompt;
 
     SeekBar setAge;
 
     public void onStart() {
 
         super.onStart();
 
         setAgePrompt = (TextView) getView().findViewById(R.id.agePrompt);
 
         setAge = (SeekBar) getView().findViewById(R.id.charAge);
 
 
         setAge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 int currentProgress = seekBar.getProgress();
                 setAgePrompt.setText("Age: " + (currentProgress/2 + 10) );
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
     }
 }
 
