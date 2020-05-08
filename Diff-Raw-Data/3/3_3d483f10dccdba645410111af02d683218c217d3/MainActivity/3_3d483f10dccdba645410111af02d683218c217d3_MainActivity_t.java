 package com.gec.rq;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.text.style.QuoteSpan;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.widget.Toast;
 import android.widget.Button;
 import android.widget.EditText;
 import android.content.Context;
 
 public class MainActivity extends Activity
 {
 	DBAdapter db = new DBAdapter(this);
 	EditText quote;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
 		Button setButton = (Button) findViewById(R.id.go);
 		Button getButton = (Button) findViewById(R.id.genRan);
 
 		setButton.setOnClickListener(mAddListener);
 		getButton.setOnClickListener(mAddListener);
     }
 
 	private OnClickListener mAddListener = new OnClickListener()
 	{
 		public void onClick(View view)
 		{
 			switch (view.getId())
 			{
 				case R.id.go:
 
 					db.open();
 					long id = 0;
 
 					try
 					{
 						quote = (EditText)findViewById(R.id.Quote);
                        db.insertQuote(quote.getText().toString());
 						id = db.getAllEntries();
 
 						Context context = getApplicationContext();
 						CharSequence text = "The quote '" + quote.getText() + "' was added successfully!\n" +
 							"Quotes Total = " + id;
 						int duration = Toast.LENGTH_LONG;
 						Toast toast = Toast.makeText(context, text, duration);
 						toast.show();
 						quote.setText("");
 					}
 					catch (Exception ex)
 					{
 						Context context = getApplicationContext();
 						CharSequence text = "OnClick:\n" + ex.toString() + " ID = " + id;
 						int duration = Toast.LENGTH_LONG;
 						Toast toast = Toast.makeText(context, text, duration);
 						toast.show();
 					}
 					db.close();
 
 					break;
 
 				case R.id.genRan:
 					db.open();
 					try
 					{
 						String quote = "";
 						quote = db.getRandomEntry();
 
 						Context context = getApplicationContext();
 						CharSequence text = quote;
 						int duration = Toast.LENGTH_LONG;
 						Toast toast = Toast.makeText(context, text, duration);
 						toast.show();	
 					}
 					catch (Exception ex)
 					{
 						Context context = getApplicationContext();
 						CharSequence text = "OnClick::genRan\n" + ex.toString();
 						int duration = Toast.LENGTH_LONG;
 						Toast toast = Toast.makeText(context, text, duration);
 						toast.show();
 					}
 					db.close();
 
 					break;
 
 			}
 		}
 	};
 }
