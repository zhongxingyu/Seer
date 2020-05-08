 package com.goodhearted.smokebegone;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	SmokeDataSource DAO;
 	TextView tvDays, tvHours, tvMinutes, tvSeconds, tvDaysSuffix, tvHoursSuffix, tvMinutesSuffix, tvSecondsSuffix, tvSaving;
 	Button plus, info;
 
 	MenuHandler handler;
 	TextUpdate tu;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		setContentView(R.layout.activity_main);
 
 		readyMenu();
 
 		DAO = new SmokeDataSource(this);
 		
 		plus = (Button) findViewById(R.id.btStillSmoked);
 		plus.setOnClickListener(this);
 		
 		tvDays = (TextView) findViewById(R.id.tvDayCount);
 		tvHours = (TextView) findViewById(R.id.tvHourCount);
 		tvMinutes = (TextView) findViewById(R.id.tvMinuteCount);
 		tvSeconds = (TextView) findViewById(R.id.tvSecondCount);
 		tvSaving = (TextView) findViewById(R.id.tvMASavings);
 		updateTV();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		tu = new TextUpdate(this);
 		tu.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		tu.cancel(true);
 		tu = null;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		SlideHolder x = (SlideHolder) findViewById(R.id.bla);
 		x.toggle();
 		return true;
 
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		switch (arg0.getId()) {
 		case R.id.btStillSmoked:
 			Smoke x = DAO.getLastSmoke();
 			if(x != null) {
 				Period p = new Period(x.getDateInt(), (new Date()).getTime());
 				if(p.getPeriod() > 1000) DAO.createSmoke();
 			} else {
 				DAO.createSmoke();
 			}
 			break;
 		}
 
 		updateTV();
 	}
 
 	public void updateTV() {
 		Period d, e;
 
 		Smoke x = DAO.getLastSmoke();
 
 		long quitsmoketime = PreferenceProvider.readLong(this,
 				PreferenceProvider.keyQD, -1);
 		if(x != null){
 		e = new Period(x.getDateInt(), (new Date()).getTime());
 		} else {
 			e = new Period(quitsmoketime, (new Date()).getTime());
 		}
 		
 		tvDays.setText(Integer.toString(e.getDays()));
 		tvHours.setText(Integer.toString(e.getHours()));
 		tvMinutes.setText(Integer.toString(e.getMinutes()));
 		tvSeconds.setText(Integer.toString(e.getSeconds()));
 		
 		
 		d = new Period(quitsmoketime, (new Date().getTime()));
 		float savings = d.getSave(this, DAO.getTotalSmokes());
 		int scale = 0;
 		if (savings < 100) {
 			scale = 2;
 		}
 		BigDecimal z = new BigDecimal(String.valueOf(savings)).setScale(scale,
 				BigDecimal.ROUND_HALF_UP);
 		tvSaving.setText("En daarmee heb je \u20ac" + z.toString() + " bespaard!");
 		
 	}
 
 	private void readyMenu() {
 		handler = new MenuHandler(this);
 		for (int i = 0; i < MenuHandler.allMenuItems.length; i++) {
 			findViewById(MenuHandler.allMenuItems[i]).setOnClickListener(handler);
 		}
 	}
 
 }
 
 class TextUpdate extends AsyncTask<Void, Void, Void> {
 
 	MainActivity act;
 	TextView tv;
 	TextUpdater tu;
 
 	private class TextUpdater implements Runnable {
 
 		MainActivity act;
 
 		public TextUpdater(MainActivity z) {
 			this.act = z;
 		}
 
 		@Override
 		public void run() {
 			this.act.updateTV();
 		}
 
 	}
 
 	public TextUpdate(MainActivity act) {
 		this.act = act;
 		tu = new TextUpdater(this.act);
 		this.act.runOnUiThread(this.tu);
 	}
 
 	@Override
 	protected Void doInBackground(Void... arg0) {
 		while (true) {
 			try {
 				Thread.sleep(1000);
 			} catch (Exception e) {
 				Log.d("SMB_THREAD", "MainActivity info update interrupted!");
 			} finally {
 				this.act.runOnUiThread(this.tu);
 			}
 		}
 	}
 
 }
