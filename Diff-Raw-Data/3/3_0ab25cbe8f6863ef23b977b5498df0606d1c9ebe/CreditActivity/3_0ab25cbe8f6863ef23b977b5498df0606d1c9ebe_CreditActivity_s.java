 /*
 	Copyright (C) 2010 Ben Van Daele (vandaeleben@gmail.com)
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.benvd.mvforandroid;
 
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import be.benvd.mvforandroid.data.DatabaseHelper;
 import be.benvd.mvforandroid.data.MVDataHelper;
 import be.benvd.mvforandroid.data.MVDataService;
 
 import com.commonsware.cwac.sacklist.SackOfViewsAdapter;
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 
 public class CreditActivity extends Activity {
 
 	public static final String ACTION_REFRESH = "be.benvd.mvforandroid";
 	private DatabaseHelper helper;
 	private SharedPreferences prefs;
 
 	private BroadcastReceiver successReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			updateView();
 			CreditActivity.this.getParent().setProgressBarIndeterminateVisibility(false);
 		}
 	};
 
 	private BroadcastReceiver exceptionReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Toast.makeText(context, getString(R.string.exception_message), Toast.LENGTH_SHORT).show();
 			CreditActivity.this.getParent().setProgressBarIndeterminateVisibility(false);
 		}
 	};
 
 	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.v("DEBUG", "executing broadcastReceiver");
 			((Button) findViewById(R.id.update_button)).performClick();
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.credit);
 
 		helper = new DatabaseHelper(this);
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		updateView();
 
 		Button updateButton = (Button) findViewById(R.id.update_button);
 		updateButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				CreditActivity.this.getParent().setProgressBarIndeterminateVisibility(true);
 				Intent i = new Intent(CreditActivity.this, MVDataService.class);
 				i.setAction(MVDataService.UPDATE_CREDIT);
 				WakefulIntentService.sendWakefulWork(CreditActivity.this, i);
 			}
 		});
 
 	}
 
 	private void updateView() {
 		ListView list = (ListView) findViewById(R.id.credit_list);
 		list.setAdapter(new CreditAdapter());
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(successReceiver, new IntentFilter(MVDataService.CREDIT_UPDATED));
 		registerReceiver(exceptionReceiver, new IntentFilter(MVDataService.EXCEPTION));
 		registerReceiver(refreshReceiver, new IntentFilter(ACTION_REFRESH));
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(successReceiver);
 		unregisterReceiver(exceptionReceiver);
 		unregisterReceiver(refreshReceiver);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		helper.close();
 	}
 
 	private DecimalFormat currencyFormat = new DecimalFormat("#.##");
 	private SimpleDateFormat validUntilFormat;
 
 	private String formatCurrency(double amount) {
 		return currencyFormat.format(amount) + "€";
 
 	}
 
 	public String formatValidUntilDate(long validUntil) {
 		if (validUntilFormat == null)
 			validUntilFormat = new SimpleDateFormat("dd-MM-yyyy '" + getString(R.string.at_hour) + "' HH:mm");
 		return validUntilFormat.format(new Date(validUntil));
 	}
 
 	class CreditAdapter extends SackOfViewsAdapter {
 
 		private static final int NUM_ROWS = 4;
 		private static final int REMAINING_CREDIT = 0;
 		private static final int REMAINING_SMS = 1;
 		private static final int REMAINING_DATA = 2;
 		private static final int VALID_UNTIL = 3;
 		private static final double RATIO_THRESHOLD = 0.10;
 
 		public CreditAdapter() {
 			super(NUM_ROWS);
 		}
 
 		@Override
 		protected View newView(int position, ViewGroup parent) {
 			switch (position) {
 			case REMAINING_CREDIT: {
 				double remainingCredit = helper.credit.getRemainingCredit();
 				View view = getLayoutInflater().inflate(R.layout.credit_credit, parent, false);
 				TextView text = (TextView) view.findViewById(R.id.credit_text);
 				text.setText(formatCurrency(remainingCredit) + " " + getString(R.string.remaining));
 
 				float ratio = ((float) remainingCredit / prefs.getFloat(MVDataHelper.PRICE_PLAN_TOPUP_AMOUNT, 15));
 				view.setBackgroundDrawable(getProgressBackground(ratio));
 
 				if (ratio < RATIO_THRESHOLD)
 					text.setTextColor(0xffa51d1d);
 
 				return view;
 			}
 			case REMAINING_SMS: {
 				int remainingSms = helper.credit.getRemainingSms();
 				View view = getLayoutInflater().inflate(R.layout.credit_sms, parent, false);
 				TextView text = (TextView) view.findViewById(R.id.sms_text);
 				text.setText(remainingSms + " " + getString(R.string.sms_remaining));
 
 				float ratio = ((float) remainingSms / prefs.getInt(MVDataHelper.PRICE_PLAN_SMS_AMOUNT, 1000));
 				view.setBackgroundDrawable(getProgressBackground(ratio));
 
 				if (ratio < RATIO_THRESHOLD)
 					text.setTextColor(0xffa51d1d);
 
 				return view;
 			}
 			case REMAINING_DATA: {
 				long remainingBytes = helper.credit.getRemainingData();
 				View view = getLayoutInflater().inflate(R.layout.credit_data, parent, false);
 				TextView text = (TextView) view.findViewById(R.id.data_text);
 				text.setText((remainingBytes / 1048576) + " " + getString(R.string.megabytes_remaining));
 
 				double ratio = ((double) remainingBytes / ((long) prefs.getInt(MVDataHelper.PRICE_PLAN_DATA_AMOUNT,
 						1024) * 1024 * 1024));
 				view.setBackgroundDrawable(getProgressBackground(ratio));
 
 				if (ratio < RATIO_THRESHOLD)
 					text.setTextColor(0xffa51d1d);
 
 				return view;
 			}
 			case VALID_UNTIL: {
 				View view = getLayoutInflater().inflate(R.layout.credit_extra, parent, false);
 				TextView validText = (TextView) view.findViewById(R.id.valid_until);
 				validText.setText(getString(R.string.valid_until) + " "
 						+ formatValidUntilDate(helper.credit.getValidUntil()));
 
 				long remainingTime = helper.credit.getValidUntil() - System.currentTimeMillis();
 				long oneMonthInMillis = 30 * 24 * 3600000;
				double ratio = remainingTime / oneMonthInMillis;
 				if (ratio < RATIO_THRESHOLD)
 					validText.setTextColor(0xffa51d1d);
 
 				TextView planText = (TextView) view.findViewById(R.id.price_plan);
 				String planName = prefs.getString(MVDataHelper.PRICE_PLAN_NAME, null);
 				if (planName == null)
 					planText.setVisibility(View.GONE);
 				planText.setText(getString(R.string.price_plan) + ": " + planName);
 
 				return view;
 			}
 			}
 			return null;
 		}
 
 		private BitmapDrawable getProgressBackground(double ratio) {
 			// Setup bitmap and corresponding canvas
 			int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
 			Bitmap result = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888);
 			Canvas c = new Canvas();
 			c.setBitmap(result);
 
 			// Draw background
 			c.drawColor(0xffeeeeee);
 
 			// Draw progress rectangle
 			Paint paint = new Paint();
 			paint.setAntiAlias(true);
 			paint.setColor(Color.LTGRAY);
 			c.drawRect(0, 0, (float) (ratio * width), 1, paint);
 
 			return new BitmapDrawable(result);
 		}
 
 	}
 
 }
