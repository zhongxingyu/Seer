 package pl.byd.promand.Team1;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import com.promand.Team1.R;
 
public class ColorChange extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.color_selector);
 
         final SeekBar redSeek = (SeekBar) findViewById(R.id.seekBarRed);
         final SeekBar greenSeek = (SeekBar) findViewById(R.id.seekBarGreen);
         final SeekBar blueSeek = (SeekBar) findViewById(R.id.seekBarBlue);
 
         redSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 TextView redText = (TextView) findViewById(R.id.redInNumbers);
                 redText.setText(String.valueOf(progress));
 
                 EditText hexColor = (EditText) findViewById(R.id.colorInHex);
                 String r = Integer.toHexString(progress).length() < 2 ? 0 + Integer.toHexString(progress) : Integer.toHexString(progress);
                 String g = Integer.toHexString(greenSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(greenSeek.getProgress()) :
                         Integer.toHexString(greenSeek.getProgress());
                 String b = Integer.toHexString(blueSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(blueSeek.getProgress()) :
                         Integer.toHexString(blueSeek.getProgress());
                 String hexCode = r + g + b;
                 hexColor.setText("#" + hexCode.toUpperCase());
             }
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
         greenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 TextView greenText = (TextView) findViewById(R.id.greenInNumbers);
                 greenText.setText(String.valueOf(progress));
 
                 EditText hexColor = (EditText) findViewById(R.id.colorInHex);
                 String r = Integer.toHexString(redSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(redSeek.getProgress()) :
                         Integer.toHexString(redSeek.getProgress());
                 String g = Integer.toHexString(progress).length() < 2 ? 0 + Integer.toHexString(progress) : Integer.toHexString(progress);
                 String b = Integer.toHexString(blueSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(blueSeek.getProgress()) :
                         Integer.toHexString(blueSeek.getProgress());
                 String hexCode = r + g + b;
                 hexColor.setText("#" + hexCode.toUpperCase());
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
         blueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 TextView blueText = (TextView) findViewById(R.id.blueInNumbers);
                 blueText.setText(String.valueOf(progress));
 
                 EditText hexColor = (EditText) findViewById(R.id.colorInHex);
                 String r = Integer.toHexString(redSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(redSeek.getProgress()) :
                         Integer.toHexString(redSeek.getProgress());
                 String g = Integer.toHexString(greenSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(greenSeek.getProgress()) :
                         Integer.toHexString(greenSeek.getProgress());
                 String b = Integer.toHexString(progress).length() < 2 ? 0 + Integer.toHexString(progress) :
                         Integer.toHexString(progress);
                 String hexCode = r + g + b;
                 hexColor.setText("#" + hexCode.toUpperCase());
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
