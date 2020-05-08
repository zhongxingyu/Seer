 package com.example.ThreadEx;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements CharacterSource, CharacterListener {
     protected RandomCharacterGenerator mProducer = new RandomCharacterGenerator();
     private CharacterEventHandler mHandler = new CharacterEventHandler();
     
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         layoutSetup();
     }
 
     public void layoutSetup() {
         ((Button)findViewById(R.id.enter)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                String inputString = ((EditText)findViewById(R.id.input)).toString();
                 newCharacter(new CharacterEvent(MainActivity.this, inputString));
             }
         });
 
         mProducer.addCharacterListener(this);
         this.addCharacterListener(this);
     }
 
    public synchronized void processInput(CharacterEvent ce) {
        TextView textBox = (TextView)findViewById(R.id.output);
        String originalText = textBox.toString();
         textBox.setText(originalText + "\n" + ce.c);
     }
 
     public void addCharacterListener(CharacterListener cl) {
         mHandler.addCharacterListener(cl);
     }
 
     public void removeCharacterListener(CharacterListener cl) {
         mHandler.removeCharacterListener(cl);
     }
 
     public void nextCharacter() {
         throw new IllegalStateException("input is produced by user");
     }
 
     public void newCharacter(CharacterEvent ce) {
         processInput(ce);
     }
 }
