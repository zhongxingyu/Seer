 package de.mad.seminarapp.activity;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.Window;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import de.mad.seminarapp.R;
 import de.mad.seminarapp.utility.AutoResizeTextView;
 
 /**
  * An example App for the Seminar
  *
  * @author Patrick Na
  */
 public class MainActivity extends Activity {
 	private AutoResizeTextView arTextView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE); 
         setContentView(R.layout.activity_main);
         
         // example for programatically adding a TextView
         LinearLayout myLInearLayout =(LinearLayout) findViewById(R.id.LinearLayout1);
         LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         params.gravity=Gravity.CENTER;
         params.topMargin=20;
         myLInearLayout.setOrientation(LinearLayout.VERTICAL);
         
         arTextView=new AutoResizeTextView(getApplicationContext());
         arTextView.setText("Programmatisch hinzugefgt");
         arTextView.setId(5);
         arTextView.setLayoutParams(params);
         myLInearLayout.addView(arTextView);
         
         final SeekBar mySeekBar = (SeekBar) findViewById(R.id.mainViewSeekBar1);
         
         mySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
  
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                 genPassword(progress);
             }
  
             public void onStartTrackingTouch(SeekBar seekBar) {
                 // TODO Auto-generated method stub
             }
  
             public void onStopTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
             }
         });
 
 
     }
 
 
     
     public void goToGameView(View view) {
     	// start new intent
     	Intent nextScreen = new Intent(getApplicationContext(), GameActivity.class); 
     	// send password to next view
     	final TextView text = (TextView)findViewById(R.id.mainViewTextView1);
     	nextScreen.putExtra("password", text.getText().toString());
     	// Log for debug
     	Log.e("goToGameView: ", text.getText().toString()+".");
     	
     	startActivity(nextScreen);
     
     }
 
 
 
 
     private void genPassword (int progress) {
         int zeichenAnzahl = progress;
         StringBuilder passwordKette = new StringBuilder();
         
         String safety = "UUpps!";
         int safetyColor;
         
         for (int i=0; i<zeichenAnzahl; i++) {
             int randNumber = 0;
             int randNumberSwitch = new Random().nextInt()%3;
             switch (randNumberSwitch) {
                 case 0: //Zahl
                     randNumber = new Random().nextInt()%10;
                     randNumber += 48;
                    passwordKette.append((char)randNumber);
                     break;
                 case 1: //Buchstabe
                     if (!((CheckBox)findViewById(R.id.mainViewCheckBox1)).isChecked()) {
                         i--;
                         break;
                     }
                     randNumber = new Random().nextInt()%26;
                     randNumber += 65;
                     passwordKette.append((char)randNumber);
                    
                     break;
                 case 2: //Sonderzeichen
                     if (!((CheckBox)findViewById(R.id.mainViewCheckBox2)).isChecked()) {
                         i--;
                         break;
                     }
                     randNumber = new Random().nextInt()%14;
                     randNumber += 33;
                     passwordKette.append((char)randNumber);
                     break;
                 default:
                     break;
             }
         }
         
         if(((CheckBox)findViewById(R.id.mainViewCheckBox1)).isChecked() && ((CheckBox)findViewById(R.id.mainViewCheckBox1)).isChecked()){
             if(zeichenAnzahl < 6){
                 safety = "unsicher";
                 safetyColor = Color.RED;
             }else if(zeichenAnzahl < 9){
                 safety = "sicher";
                 safetyColor = Color.GREEN;
             }else if(zeichenAnzahl < 12){
                 safety = "sehr sicher";
                 safetyColor = Color.GREEN;
             }else{
                 safety = "extrem sicher";
                 safetyColor = Color.GREEN;
             }
         }else if(((CheckBox)findViewById(R.id.mainViewCheckBox1)).isChecked() || ((CheckBox)findViewById(R.id.mainViewCheckBox2)).isChecked()){
             if(zeichenAnzahl < 6){
                 safety = "unsicher";
                 safetyColor = Color.RED;
             }else if(zeichenAnzahl < 9){
                 safety = "normal";
                 safetyColor = Color.YELLOW;
             }else if(zeichenAnzahl < 12){
                 safety = "sicher";
                 safetyColor = Color.GREEN;
             }else{
                 safety = "sicher";
                 safetyColor = Color.GREEN;
             }
         }else{
             if(zeichenAnzahl < 6){
                 safety = "unsicher";
                 safetyColor = Color.RED;
             }else if(zeichenAnzahl < 9){
                 safety = "unsicher";
                 safetyColor = Color.RED;
             }else if(zeichenAnzahl < 12){
                 safety = "normal";
                 safetyColor = Color.YELLOW;
             }else{
                 safety = "normal";
                 safetyColor = Color.YELLOW;
             }
         }
         
         ((TextView)findViewById(R.id.mainViewTextView4)).setText(safety);
         ((TextView)findViewById(R.id.mainViewTextView4)).setTextColor(safetyColor);
         ((TextView)findViewById(R.id.mainViewTextView1)).setText(passwordKette);
 
     }
 
 }
