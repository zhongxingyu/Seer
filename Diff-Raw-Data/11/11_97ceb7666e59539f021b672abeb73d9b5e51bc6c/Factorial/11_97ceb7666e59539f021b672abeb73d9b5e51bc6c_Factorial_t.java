 package com.webs.samirapplications.factorial;
 

 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.Toast;
 import android.view.View;
 
 
 
 public class Factorial extends Activity {
     /** Called when the activity is first created. */
 	
 	private EditText in;
 	private int num;
 	private int ans;
 	private TextView txtanswer;
 	private Button button;
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         initControls();
     }
     
     private void initControls()
    	{	
         button = (Button)findViewById(R.id.button);
         txtanswer = (TextView)findViewById(R.id.txtanswer);
         in = (EditText)findViewById(R.id.text);
       	button.setOnClickListener(new Button.OnClickListener() 
       	{ 
       		public void onClick (View v)
       		{ calculate();} 
       		{Toast.makeText(Factorial.this, R.string.short_notification_text, Toast.LENGTH_SHORT).show();}
        	});
     }
 
 	private void calculate() 
 	{
		try{
 		num=Integer.parseInt(in.getText().toString());
 			
 		for (int i = num - 1; i > 0; i--)
 		{
 			ans = num * i;
 			num = ans;
 		}
 				
 		txtanswer.setText(Integer.toString(ans));
		}
		catch(NumberFormatException e){
			error();
		}
	}
	public void error(){
		{Toast.makeText(Factorial.this, R.string.error, Toast.LENGTH_LONG).show();}	
 	}
 		
 }
