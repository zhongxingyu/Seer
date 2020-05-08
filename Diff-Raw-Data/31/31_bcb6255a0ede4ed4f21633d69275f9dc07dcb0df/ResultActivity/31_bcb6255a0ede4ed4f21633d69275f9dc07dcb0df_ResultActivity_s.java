 package com.thirsty.view;
 
 import com.thirsty.R;
 import com.thirsty.controller.Controller;
 import com.thirsty.controller.OnShakeListener;
 
 import android.app.Activity;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class ResultActivity extends Activity {
 	private static final String TAG = "ResultActivity";
 	private Controller _application;
 	private int resultColorNumber;
 	private RelativeLayout infoView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.result_view);
 
 		this._application = (Controller)getApplication();
 
 
 		this._application.setOnShakeListener(shakeListener);    
 		this._application.startListeningForShake();
 
 		resultColorNumber = this._application.colorNumber;
 
 		TextView ruleTextView = (TextView) this.findViewById(R.id.rule_text);
 		Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/itc-lubalin-graph-lt-demi.ttf");             
 		ruleTextView.setTypeface(tf);
 		ruleTextView.setText(this._application.tippsyRuleList[resultColorNumber].get_ruleTitle());
 
 		ImageView colorImage = (ImageView) this.findViewById(R.id.color_image);
 		ImageView ruleImage = (ImageView) this.findViewById(R.id.rule_image);
 		ImageView messagebgImage = (ImageView) this.findViewById(R.id.messagebg_image);
 
 		colorImage.setImageResource(this._application.tippsyRuleList[resultColorNumber].get_colorImage());
 		ruleImage.setImageResource(this._application.tippsyRuleList[resultColorNumber].get_message());
 		messagebgImage.setImageResource(this._application.tippsyRuleList[resultColorNumber].get_messagebg());
 		
 		infoView = (RelativeLayout) this.findViewById(R.id.info_view);
 		infoView.setVisibility(View.GONE);
 		
 		ImageButton imageButton = (ImageButton) this.findViewById(R.id.info_button);
 		imageButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				infoView.setVisibility(View.VISIBLE);
 				
 			}});
 		
 		if(this._application.tippsyRuleList[resultColorNumber].get_hasInfo())
 		{
 			imageButton.setVisibility(View.VISIBLE);
 		}
 		else
 		{
 			imageButton.setVisibility(View.GONE);
 		}
 		
 		TextView ruleTitleTextView = (TextView) this.findViewById(R.id.info_rule_title);        
 		ruleTitleTextView.setTypeface(tf);
 		ruleTitleTextView.setText(this._application.tippsyRuleList[resultColorNumber].get_ruleTitle());
 		
 		TextView ruleDescriptioinTextView = (TextView) this.findViewById(R.id.info_rule_description);         
 		ruleDescriptioinTextView.setTypeface(tf);
 		ruleDescriptioinTextView.setText(this._application.tippsyRuleList[resultColorNumber].get_ruleDescription());
 		
 		Resources res = getResources();
 		TextView passTextView = (TextView) this.findViewById(R.id.pass_text);         
 		passTextView.setTypeface(tf);
 		
 		if(resultColorNumber == 0 && this._application.getCountEverybodyDrinks() == 3)
 		{
 			passTextView.setText(res.getString(R.string.start_again));
 			_application.resetCountEverybodyDrinks();
 		}
 		else
 		{
 			passTextView.setText(res.getString(R.string.pass_shake));
 		}
 		
 		if(resultColorNumber == 0)
 		{
 			this._application.incrementCountEverybodyDrinks();
 		}
 
 	}
 
 
 	OnShakeListener shakeListener = new OnShakeListener()
 	{
 		@Override
 		public void onShake() {
 			_application.stopListeningForShake();
 			_application.nextActivityFromResultActivity(ResultActivity.this);		
 		}        	
 	};
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();    	
 		this._application.removeOnShakeListener(shakeListener);
 	}
 
 
 	public void onBackPressed() 
 	{        
 		if(infoView.getVisibility() == View.GONE)
 		{
 			this._application.disconnectRobot();        
 			super.onBackPressed();  
 		}
 		else
 		{
 			infoView.setVisibility(View.GONE);
 		}
 			
 	}
 }
