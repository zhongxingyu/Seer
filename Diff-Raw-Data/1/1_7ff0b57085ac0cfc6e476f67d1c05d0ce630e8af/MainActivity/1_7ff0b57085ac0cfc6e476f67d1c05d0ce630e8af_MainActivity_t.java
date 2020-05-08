 package net.zanzapla.diceroller;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 import net.zanzapla.diceroller.core.*;
 
 public class MainActivity extends Activity {
 	
 public final static String EXTRA_MESSAGE = "net.zanzapla.diceroller.MESSAGE";	
 public static Dice dice = new Dice();
 private TextView resultText, resultValue;
 private String text;
 private String result;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		resultText = (TextView) findViewById(R.id.textResultTextView);
 		resultValue = (TextView) findViewById(R.id.numberResultTextView);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	public void onClickRoll(View btn){
 		int dieFaces = Integer.parseInt((String) btn.getTag());		
 		dice.setDieFaces(dieFaces);
 		dice.roll();
 		result = dice.getStrValue();
 		switch (dieFaces) {
 		case 6: text = getResources().getString(R.string.dice_six);
 		break;
 		case 10: text = getResources().getString(R.string.dice_ten);
 		break;
 		case 20: text = getResources().getString(R.string.dice_twenty);
 		break;
 		case 100: text = getResources().getString(R.string.dice_hundred);
 		break;
 
 		default: text = "HALP!";
 		break;
 
 		}
		drawResults();
 		
 	}
 	
 	public void drawResults() {
 		resultText.setText(text);
 		resultText.setWidth(90);
 		resultValue.setText(result);
 		resultValue.setTextSize(100);
 	}
 }
