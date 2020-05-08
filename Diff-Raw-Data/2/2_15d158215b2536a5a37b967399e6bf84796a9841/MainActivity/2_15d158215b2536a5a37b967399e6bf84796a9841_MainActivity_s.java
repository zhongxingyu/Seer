 package com.example.buggycalculator;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 @SuppressLint("NewApi")
 public class MainActivity extends Activity {
 	
 	private float currentNumber = 0;
 	private boolean fAdd = false;
 	private boolean fSub = false;
 	private boolean fMul = false;
 	private boolean fDiv = false;
 	private boolean fEq = false;
 	
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     private void putText(String s)
     {
     	EditText tScreen = (EditText)findViewById(R.id.Screen);
     	if (fEq)
     	{
     		this.fEq = false;
     		clearScreen();
     		tScreen.append(s);
     	}
     	else if (tScreen.getText().toString().equals("0"))
     		tScreen.setText(s); 
     	else if (!tScreen.getText().toString().equals("0"))
     		tScreen.append(s); 
     }
     
     /** Called when the user clicks the 1 button */
     public void b1(View view) {
     	putText("1");
     }
     /** Called when the user clicks the 2 button */
     public void b2(View view) {
     	putText("2");
     }
     /** Called when the user clicks the 3 button */
     public void b3(View view) {
     	putText("3");
     }
     /** Called when the user clicks the 4 button */
     public void b4(View view) {
     	putText("4");
     }
     /** Called when the user clicks the 5 button */
     public void b5(View view) {
     	putText("5");
     }
     /** Called when the user clicks the 6 button */
     public void b6(View view) {
     	putText("6");
     }
     /** Called when the user clicks the 7 button */
     public void b7(View view) {
     	putText("7");
     }
     /** Called when the user clicks the 8 button */
     public void b8(View view) {
     	putText("8");
     }
     /** Called when the user clicks the 9 button */
     public void b9(View view) {
     	putText("9");
     }
     /** Called when the user clicks the 0 button */
     public void b0(View view) {
     	EditText tScreen = (EditText)findViewById(R.id.Screen);
     	if (!tScreen.getText().toString().equals("0"))
     	{
     		putText("0");
     	}
     }
     /** Called when the user clicks the Dot button */
     @SuppressLint("NewApi")
 	public void bDot(View view) {
     	EditText tScreen = (EditText)findViewById(R.id.Screen);
    	if (tScreen.getText().toString().isEmpty())
     		putText("0.");
     	else if (!tScreen.getText().toString().contains("."))
     	{
     		putText(".");
     	}
     }
     public void bEqual(View view) {
     	EditText tScreen = (EditText)findViewById(R.id.Screen);
     	if(this.fAdd)
     		tScreen.setText(Float.toString( this.currentNumber + Float.parseFloat(tScreen.getText().toString())));
     	else if (this.fSub)
     		tScreen.setText(Float.toString( this.currentNumber - Float.parseFloat(tScreen.getText().toString())));
     	else if (this.fMul)
     		tScreen.setText(Float.toString( this.currentNumber * Float.parseFloat(tScreen.getText().toString())));
     	else if (this.fDiv)
     		tScreen.setText(Float.toString( this.currentNumber / Float.parseFloat(tScreen.getText().toString())));
     	
     	this.fAdd = false;
     	this.fSub = false;
     	this.fMul = false;
     	this.fDiv = false;
     	this.fEq = true;
     }
     public void bAdd(View view) {
     	if (!(fAdd || fSub || fDiv || fMul))
             saveCurrentNumber();
     	this.fAdd = true;
     	this.fSub = false;
     	this.fMul = false;
     	this.fDiv = false;
     }
     public void bSub(View view) {
     	if (!(fAdd || fSub || fDiv || fMul))
             saveCurrentNumber();
     	this.fAdd = false;
     	this.fSub = true;
     	this.fMul = false;
     	this.fDiv = false;
     }
     public void bMul(View view) {
     	if (!(fAdd || fSub || fDiv || fMul))
             saveCurrentNumber();
     	this.fAdd = false;
     	this.fSub = false;
     	this.fMul = true;
     	this.fDiv = false;
     }
     public void bDiv(View view) {
     	if (!(fAdd || fSub || fDiv || fMul))
             saveCurrentNumber();
     	this.fAdd = false;
     	this.fSub = false;
     	this.fMul = false;
     	this.fDiv = true;
     }
     private void saveCurrentNumber()
     {
     	EditText tScreen = (EditText)findViewById(R.id.Screen);
     	if (!tScreen.getText().toString().isEmpty())
     	{
     		this.currentNumber = Float.parseFloat(tScreen.getText().toString());
     		clearScreen();
     	}
     	else 
     		this.currentNumber = 0;
     }
     private void clearScreen()
     {
     	EditText tScreen = (EditText)findViewById(R.id.Screen);
     	tScreen.setText("");
     }
     public void bC(View view)
     {
     	clearScreen();
     }
     public void bCE(View view)
     {
     	this.currentNumber = 0;
     	clearScreen();
     }
 }
