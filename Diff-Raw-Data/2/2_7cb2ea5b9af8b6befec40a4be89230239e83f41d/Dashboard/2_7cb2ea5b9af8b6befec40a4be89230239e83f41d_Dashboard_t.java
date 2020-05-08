 package com.voc4u.activity.dashboard;
 
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 import com.voc4u.R;
 import com.voc4u.activity.BaseActivity;
 import com.voc4u.activity.DialogInfo;
 import com.voc4u.activity.dictionary.Dictionary;
 import com.voc4u.activity.listener.Listener;
 import com.voc4u.activity.speaker.Speaker;
 import com.voc4u.activity.train.Train;
 import com.voc4u.setting.CommonSetting;
 
 public class Dashboard extends BaseActivity
 {
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dashboard);
 		CommonSetting.restore(this);
 		
 //		Intent train = new Intent(this, Train.class);
 //		startActivity(train);
 	}
 	
 	public void onTrainButton(View view)
 	{
 		Intent it = new Intent(this, Train.class);
 		startActivity(it);
 	}
 	
 	public void onSpeakerButton(View view)
 	{
 		Intent it = new Intent(this, Speaker.class);
 		startActivity(it);
 	}
 	
 	public void onSpeechButton(View view)
 	{
 		Intent it = new Intent(this, Listener.class);
 		startActivity(it);
 	}
 	
 	public void onSettingButton(View view)
 	{
 		Intent it = new Intent(this, Dictionary.class);
 		startActivity(it);
 	}
 	
 	@Override
 	protected String GetShowInfoType()
 	{
		return DialogInfo.TYPE_DASHBOARD;
 	}
 }
