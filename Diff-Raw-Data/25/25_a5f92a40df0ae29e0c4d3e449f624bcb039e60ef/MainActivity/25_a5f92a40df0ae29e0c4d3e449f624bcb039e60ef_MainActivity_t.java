 package com.yuniz.mealtaxcalculator;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.math.BigDecimal;
 
 import com.yuniz.mealtaxcalculator.R;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Menu;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	public int screenWidth = 0;
 	public int screenHeight = 0;
 	
 	private RelativeLayout mainCanvas;
 	private EditText singleCost;
 	private EditText disCounted;
 	private EditText serviceTax;
 	private EditText governmentTax;
 	private TextView finalCost;
 	private ImageView calculateBtn;
 	
 	private TextView textView1;
 	private TextView textView2;
 	private TextView textView3;
 	private TextView textView4;
 	private TextView textView6;
 	private TextView textView7;
 	private TextView textView8;
 	private TextView textView9;
 	
 	private LinearLayout linearLayout2;
 	private LinearLayout linearLayout3;
 	private LinearLayout linearLayout4;
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		int sdk = android.os.Build.VERSION.SDK_INT;
 		
 		//----------detect device setting and adapt environment
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		
 		boolean smallScreen = false;
 		try
 		{ 
 			display.getSize(size); 
 			screenWidth = size.x; 
 			screenHeight = size.y; 
 			smallScreen = false;
 		} 
 		catch (NoSuchMethodError e) 
 		{ 
 			screenWidth = display.getWidth(); 
 			screenHeight = display.getHeight(); 
 			smallScreen = true;
 		} 
 	
 	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 	    StrictMode.setThreadPolicy(policy);
 		//----------detect device setting and adapt environment
 
 	    double setNewHeight = screenHeight;
 		double setNewWidth = screenWidth;
 		
 		mainCanvas = (RelativeLayout) findViewById(R.id.mainCanvas);
 		
 		singleCost = (EditText) findViewById(R.id.editText1);
 		serviceTax = (EditText) findViewById(R.id.editText2);
 		disCounted = (EditText) findViewById(R.id.editText3);
 		governmentTax = (EditText) findViewById(R.id.editText4);
 		
 		finalCost = (TextView) findViewById(R.id.textView5);
 		calculateBtn = (ImageView) findViewById(R.id.imageView1);
 		
 		textView1 = (TextView) findViewById(R.id.textView1);
 		textView2 = (TextView) findViewById(R.id.textView2);
 		textView3 = (TextView) findViewById(R.id.textView3);
 		textView4 = (TextView) findViewById(R.id.textView4);
 		textView6 = (TextView) findViewById(R.id.textView6);
 		textView7 = (TextView) findViewById(R.id.textView7);
 		textView8 = (TextView) findViewById(R.id.textView8);
 		textView9 = (TextView) findViewById(R.id.textView9);
 		
 		linearLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
 		linearLayout3 = (LinearLayout) findViewById(R.id.linearLayout3);
 		linearLayout4 = (LinearLayout) findViewById(R.id.linearLayout4);
 		
 		try 
 		{
 		    InputStream ims = getAssets().open("bg.jpg");
 		    Drawable d = Drawable.createFromStream(ims, null);
 
 		    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
 		    	mainCanvas.setBackgroundDrawable(d);
 		    } else {
 		    	mainCanvas.setBackground(d);
 		    }
 		    
 		    InputStream ims1 = getAssets().open("calculate_btn.png");
 		    Drawable d1 = Drawable.createFromStream(ims1, null);
 		    calculateBtn.setImageDrawable(d1);
 		}
 		catch(IOException ex) 
 		{
 		    return;
 		}
 		
 		singleCost.setText("0.00");
 		serviceTax.setText("6");
 		disCounted.setText("0");
 		governmentTax.setText("10");
 		
 		//----------auto Adjust UI Elements size----------
 		if(smallScreen == true){
 			calculateBtn.setAdjustViewBounds(true);
 			calculateBtn.setScaleType( ImageView.ScaleType.FIT_CENTER);
 		}
 		
 		setNewHeight = screenHeight * 0.04;
 		textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		setNewHeight = screenHeight * 0.10;
 		textView1.setPadding(0, (int)setNewHeight, 0, 0);
 		
 		setNewHeight = screenHeight * 0.08;
 		textView6.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		singleCost.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		
 		setNewHeight = screenHeight * 0.01;
 		linearLayout2.setPadding(0, (int)setNewHeight, 0, 0);
 		linearLayout3.setPadding(0, (int)setNewHeight, 0, 0);
 		linearLayout4.setPadding(0, (int)setNewHeight, 0, 0);
 		
		setNewHeight = screenHeight * 0.02;
 		linearLayout2.setPadding(0, (int)setNewHeight, 0, 0);
		setNewHeight = screenHeight * 0.03;
 		calculateBtn.setPadding(0, (int)setNewHeight, 0, 0);
 		
 		setNewHeight = screenHeight * 0.01;
 		finalCost.setPadding(0, 0, 0, (int)setNewHeight);
 		setNewHeight = screenHeight * 0.1;
 		finalCost.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		
 		setNewWidth = screenWidth * 0.5;
 		setNewHeight = screenHeight * 0.17;
 		calculateBtn.setMinimumHeight((int)setNewHeight);
 		calculateBtn.setMaxHeight((int)setNewHeight);
 		calculateBtn.setMinimumWidth((int)setNewWidth);
 		calculateBtn.setMaxWidth((int)setNewWidth);
 
 		
 		
		setNewHeight = screenHeight * 0.04;
 		textView2.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		disCounted.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		textView7.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		
 		textView3.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		serviceTax.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		textView8.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		
 		textView4.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		governmentTax.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		textView9.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
 		
 		/*setNewWidth = screenWidth * 0.3;
 		disCounted.setWidth((int)setNewWidth);
 		disCounted.setMinimumWidth((int)setNewWidth);
 		disCounted.setMaxWidth((int)setNewWidth);
 		serviceTax.setWidth((int)setNewWidth);
 		serviceTax.setMinimumWidth((int)setNewWidth);
 		serviceTax.setMaxWidth((int)setNewWidth);
 		governmentTax.setWidth((int)setNewWidth);
 		governmentTax.setMinimumWidth((int)setNewWidth);
 		governmentTax.setMaxWidth((int)setNewWidth);*/
 		//----------auto Adjust UI Elements size----------
 	}
 	
 	public void openURL(View v) {
 		Uri uri = Uri.parse("http://www.yuniz.com");
 		 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 		 startActivity(intent);
 	}
 	
 	public void calculateNow(View v) {
 		float totalCost = (float)0;
 		
 		if(singleCost.getText().toString().trim().length() == 0){
 			singleCost.setText("0.00");
 		}else{
 			if(Float.parseFloat(singleCost.getText().toString()) > 0){
 				totalCost = Float.parseFloat(singleCost.getText().toString());
 			}
 		}
 		
 		if(disCounted.getText().toString().trim().length() == 0){
 			disCounted.setText("0");
 		}else{
 			if(Float.parseFloat(disCounted.getText().toString()) > 0){
 				totalCost = totalCost - totalCost * (Float.parseFloat(disCounted.getText().toString()) / 100);
 			}
 		}
 		
 		if(serviceTax.getText().toString().trim().length() == 0){
 			serviceTax.setText("6");
 		}else{
 			if(Float.parseFloat(serviceTax.getText().toString()) > 0){
 				totalCost = totalCost + totalCost * (Float.parseFloat(serviceTax.getText().toString()) / 100);
 			}
 		}
 		
 		if(governmentTax.getText().toString().trim().length() == 0){
 			governmentTax.setText("10");
 		}else{
 			if(Float.parseFloat(governmentTax.getText().toString()) > 0){
 				totalCost = totalCost + totalCost * (Float.parseFloat(governmentTax.getText().toString()) / 100);
 			}
 		}
 		
 		if(totalCost == (float)0){
 			Toast.makeText(getApplicationContext(), "Please touch on the numbers to insert your meal cost." , Toast.LENGTH_LONG).show();
 		}
 		
		BigDecimal result;
        result=round(totalCost,2);
		
		finalCost.setText("$" + result);
 		
 	}
 	
	public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);       
        return bd;
    }
	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
