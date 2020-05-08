 package it.workspace.prova;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	
 	int counter;
 	Button add, sub;
 	TextView display;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		counter=0;
 		add = (Button) findViewById(R.id.add);
 		sub = (Button) findViewById(R.id.subtract);
 		display = (TextView) findViewById(R.id.textView1);
 		
 		add.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				counter++;
				setCounter();
 			}
 			
 		});
 		sub.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				counter--;
				setCounter();
 			}
 		});
	}
	
	public void setCounter(){
		display.setText("Totale: " + counter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
