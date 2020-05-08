 package com.revib.revib;
 
 import com.revib.revib.dialog.AboutDialog;
 import com.revib.revib.dialog.ExitDialog;
 import com.revib.revib.dialog.LocaleDialog;
 import com.revib.revib.locale.LocaleFunctions;
 import com.revib.revib.session.SessionVariables;
 
 import android.media.AudioManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.support.v4.app.NavUtils;
 
 public class AgeActivity extends Activity implements OnClickListener {
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         // Audio buttons changes multimedia volume
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		Context context		=	this.getBaseContext();
 		String locale_code	=	LocaleFunctions.getLocaleCodeVariable(context);
 		LocaleFunctions.changeCurrentLocale(context, locale_code);
 
 		initView();
 		
 		// Show the Up button in the action bar.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.revib_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 
 		case R.id.menu_language:
 			LocaleDialog ld	=	new LocaleDialog(this);
 			ld.startDialog();
 			return true;
 		case R.id.menu_about:
 			AboutDialog ad	=	new AboutDialog(this);
 			ad.startDialog();
 		    return true;
 		case R.id.menu_exit:
 			ExitDialog ed	=	new ExitDialog(this);
 			ed.startDialog();
 		    return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onClick(View v) {
 		int	age	=	-1;
 		Intent intent	=	null;
 		int viewId	=	v.getId();
 		switch (viewId) {
 		case R.id.age_adult_btn:
 			age=SessionVariables.ADULT;
 			intent = new Intent(this, StateActivity.class);
 			break;
 		case R.id.age_child_btn:
 			age=SessionVariables.CHILD;
 			intent = new Intent(this, StateActivity.class);
 			break;
 		case R.id.age_baby_btn:
 			age=SessionVariables.BABY;
 			intent = new Intent(this, StateActivity.class);
 			break;
 		}
 		if(age!=-1){
 			SessionVariables	sv	=	SessionVariables.getInstance();
 			sv.setAge(age);
 		    startActivity(intent);
 		}
 	}
 	
 	public void initView(){
		setContentView(R.layout.activity_age);
 		// Add button listeners
 		Button button = (Button) findViewById(R.id.age_adult_btn);
 		button.setOnClickListener(this);
 		button = (Button) findViewById(R.id.age_child_btn);
 		button.setOnClickListener(this);
 		button = (Button) findViewById(R.id.age_baby_btn);
 		button.setOnClickListener(this);
 		
 		setTitle(getResources().getString(R.string.title_activity_age));
 	}
 }
