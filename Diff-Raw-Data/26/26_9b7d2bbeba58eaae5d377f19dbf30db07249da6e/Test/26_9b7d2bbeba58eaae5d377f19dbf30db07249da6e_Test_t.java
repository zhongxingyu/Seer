 package com.testterra.main;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.Display;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Test extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		// gradient
 		GradientDrawable gd = (GradientDrawable) getApplicationContext()
 				.getResources().getDrawable(R.drawable.grad);
 		Display display = getWindowManager().getDefaultDisplay();
 		int width = display.getWidth();
 		int height = display.getHeight();
 		gd.setGradientRadius((float) (Math.max(width, height) * 0.5 + 20));
 
 		// letters
 		String[] Letters = { "", "", "", "", "" };
 		setContentView(R.layout.test);
 		final Intent myIntent = getIntent();
 		final String a = myIntent.getStringExtra("com.testterra.main.a");
 		TextView tv = (TextView) findViewById(R.id.textView1);
 		tv.setText(getString(R.string.test_title) + a);
 		final String corr = Letters[Singleton.getInstance().test.correct_answer];
 
 		GlobalState gs = (GlobalState) getApplication();
 
 		gs.setcorr(corr);
 		gs.setnumb(Integer.parseInt(a));
 
 		setTest();
 		/* START buttons configuration */
 		Button ABut = (Button) findViewById(R.id.ABut);
 		if (!gs.getsubm())
 			ABut.setEnabled(false);
 		ABut.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				GlobalState gs = (GlobalState) getApplication();
 				gs.setansw("");
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 
 		Button BBut = (Button) findViewById(R.id.BBut);
 		if (!gs.getsubm())
 			BBut.setEnabled(false);
 		BBut.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				GlobalState gs = (GlobalState) getApplication();
 				gs.setansw("");
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 
 		Button CBut = (Button) findViewById(R.id.CBut);
 		if (!gs.getsubm())
 			CBut.setEnabled(false);
 		CBut.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				GlobalState gs = (GlobalState) getApplication();
 				gs.setansw("");
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 
 		Button DBut = (Button) findViewById(R.id.DBut);
 		if (!gs.getsubm())
 			DBut.setEnabled(false);
 		DBut.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				GlobalState gs = (GlobalState) getApplication();
 				gs.setansw("");
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 
 		Button EBut = (Button) findViewById(R.id.EBut);
		EBut.setEnabled(Singleton.getInstance().test.Eans);
		
 		EBut.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				GlobalState gs = (GlobalState) getApplication();
 				gs.setansw("");
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 
 		/* END buttons configuration */
 
 	}
 
 	private void setTest() {
 		TextView tv = (TextView) findViewById(R.id.textView1);
 		tv.setText(getString(R.string.test_title)
 				+ Integer.toString(Singleton.getInstance().ID + 1));
 		tv = (TextView) findViewById(R.id.Context);
 		tv.setText(Singleton.getInstance().test.statement);
 		tv.setText(Html.fromHtml(tv.getText() + ""));
 		tv = (TextView) findViewById(R.id.ansA);
 		tv.setText("A) " + Singleton.getInstance().test.A_answer);
 		tv.setText(Html.fromHtml(tv.getText() + ""));
 		tv = (TextView) findViewById(R.id.ansB);
 		tv.setText(") " + Singleton.getInstance().test.B_answer);
 		tv.setText(Html.fromHtml(tv.getText() + ""));
 		tv = (TextView) findViewById(R.id.ansC);
 		tv.setText(") " + Singleton.getInstance().test.C_answer);
 		tv.setText(Html.fromHtml(tv.getText() + ""));
 		tv = (TextView) findViewById(R.id.ansD);
 		tv.setText(") " + Singleton.getInstance().test.D_answer);
 		tv.setText(Html.fromHtml(tv.getText() + ""));
 		tv = (TextView) findViewById(R.id.ansE);
		if(Singleton.getInstance().test.Eans) {
			tv.setText(") " + Singleton.getInstance().test.E_answer);
			tv.setText(Html.fromHtml(tv.getText() + ""));
		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		GlobalState gs = (GlobalState) getApplication();
 		gs.setback(false);
 		setResult(RESULT_OK);
 		finish();
 	}
 
 }
