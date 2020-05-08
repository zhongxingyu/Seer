 package name.kropp.catan;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MyMainActivity extends Activity
 {
     private Dice myDice = new Dice();
     private EventDice myEventDice = new EventDice(myDice);
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         final Button button = (Button) findViewById(R.id.button_id);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 RollDice();
             }
         });
     }
 
     private void RollDice() {
         int white = myDice.roll();
         int red = myDice.roll();
         Event event = myEventDice.roll();
 
         final TextView textView = (TextView) findViewById(R.id.text);
 
        CharSequence text = textView.getText();
        CharSequence newText = white + " [" + red + "] = " + (white+red) + " " + event + "\n" + text;
         textView.setText(newText);
     }
 }
