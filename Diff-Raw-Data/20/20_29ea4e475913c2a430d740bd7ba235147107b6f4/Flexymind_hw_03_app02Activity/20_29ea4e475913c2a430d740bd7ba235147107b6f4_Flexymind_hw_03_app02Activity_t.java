 package com.flexymind.hw03app02;
 
import java.util.Random;

 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 public class Flexymind_hw_03_app02Activity extends Activity implements OnClickListener{
 	
 	Button btn;
	LinearLayout linLayout;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
        btn = new Button(this);
         LayoutParams btnParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        linLayout = (LinearLayout)findViewById(R.id.lin);
         btn.setText("!!! Jump !!!");
         linLayout.addView(btn, btnParam);
         btn.setOnClickListener(this);
     }
     
     public void onClick(View v) {
       // TODO Auto-generated method stub
    	int max1 = 100;
    	int max2 = 200;
    	
    	Random r = new Random();
    	
    	int left = r.nextInt(max1);
    	int top = r.nextInt(max2);
    	linLayout.setPadding(left, top, 0, 0);
     }
     
 }
